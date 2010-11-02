-------------------------------------------------------------------------------
-- Title      : Instruction cache using single ported RAM
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : icache_sp.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2003-03-22
-- Last update: 2006-10-12
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Uses single ported rams to implement instruction cache
-------------------------------------------------------------------------------
-- Copyright (c) 2003 
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library UNISIM;
use UNISIM.vcomponents.all;

use work.manikconfig.all;
use work.manikpackage.all;

entity icache_sp is
    generic (WIDTH             : integer;
             ICACHE_ENABLED    : boolean;
             ICACHE_ADDR_WIDTH : integer;
             ICACHE_WPL        : integer;
             ICACHE_SETS       : integer;
             ITAG_WIDTH        : integer;             
             CAWIDTH           : integer;
             ADDR_WIDTH        : integer);
    port (clk            : in  std_logic;
          INTR_reset     : in  std_logic;
          if_done        : in  std_logic;
          irst           : in  std_logic;
          ienb           : in  std_logic;
          EXM_data_in    : in  std_logic_vector (WIDTH-1 downto 0);
          inv_ic         : in  std_logic;
          IAGU_pc        : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
          itag_address   : in  std_logic_vector (ICACHE_ADDR_WIDTH-1 downto 0);
          inst_address   : in  std_logic_vector (ICACHE_ADDR_WIDTH-1 downto 0);
          iline_we_r     : in  std_logic_vector (ICACHE_WPL-1 downto 0);
          cache_inst_out : out std_logic_vector (WIDTH-1 downto 0);
          itag_cmp       : out std_logic);      
end icache_sp;

architecture rtl of icache_sp is

    component tagmem_s
        generic (CACHE_ENABLED    : boolean;
                 TAG_WIDTH        : integer;
                 CACHE_ADDR_WIDTH : integer);
        port (tagi        : in  std_logic_vector (TAG_WIDTH-1 downto 0);
              tag_address : in  std_logic_vector (CACHE_ADDR_WIDTH-1 downto 0);
              tag_val     : in  std_logic_vector (0 downto 0);
              tag_enb     : in  std_logic;
              tag_rst     : in  std_logic;
              tag_we      : in  std_logic;
              clk         : in  std_logic;
              tvalid      : out std_logic_vector (0 downto 0);
              tago        : out std_logic_vector (TAG_WIDTH-1 downto 0));
    end component;

    component cachemem_s
        generic (WIDTH            : integer;
                 CACHE_ENABLED    : boolean;
                 CACHE_ADDR_WIDTH : integer);
        port (cmem_out  : out std_logic_vector(WIDTH-1 downto 0);
              cmem_addr : in  std_logic_vector(CACHE_ADDR_WIDTH-1 downto 0);
              cmem_in   : in  std_logic_vector(WIDTH-1 downto 0);
              cmem_enb  : in  std_logic;
              cmem_rst  : in  std_logic;
              cmem_we   : in  std_logic;
              clk       : in  std_logic);
    end component;
    
    type itagval_type is array (ICACHE_SETS-1 downto 0) of std_logic_vector (0 downto 0);
    type itago_type   is array (ICACHE_SETS-1 downto 0) of std_logic_vector (ITAG_WIDTH-1 downto 0);
    type icout_type   is array (ICACHE_SETS-1 downto 0) of std_logic_vector (WIDTH-1 downto 0);
    type imemw_type   is array (ICACHE_SETS-1 downto 0) of std_logic_vector (ICACHE_WPL-1 downto 0);
    
    signal itval_sets_o       : itagval_type;
    signal itag_sets_o        : itago_type;
    signal icache_out         : icout_type;
    signal icmem_we           : imemw_type;
    signal tagi_i             : std_logic_vector (ITAG_WIDTH-1 downto 0)        := (others => '0');
    signal itag_val           : std_logic_vector (0 downto 0)                   := "0";
    signal itcmp              : std_logic_vector (ICACHE_SETS-1 downto 0)       := (others => '0');
    signal iset_sel, itmem_we : std_logic_vector (ICACHE_SETS-1 downto 0)       := (others => '0');

    constant iuzero    : std_logic_vector(ADDR_WIDTH-ITAG_WIDTH-ICACHE_ADDR_WIDTH+2 downto 0) := (others => '0');
    constant iset_zero : std_logic_vector (ICACHE_SETS-1 downto 0)                            := (others => '0');

begin  -- rtl

    -----------------------------------------------------------------------
    -- pick which set to write the data tofor now it is a simple switch
    -- between the sets.
    -----------------------------------------------------------------------
    -- instruction side
    process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            iset_sel     <= (others => '0');
        elsif rising_edge(clk) then
            if iset_sel = iset_zero then
                iset_sel <= conv_std_logic_vector(1,ICACHE_SETS);
            elsif if_done = '1' and iline_we_r(ICACHE_WPL-1) = '1' then
                if ICACHE_SETS > 1 then
                    iset_sel <= iset_sel(ICACHE_SETS-2 downto 0) & iset_sel(ICACHE_SETS-1);
                else
                    iset_sel(0) <= '1';
                end if;                    
            end if;
        end if;
    end process ;

    -----------------------------------------------------------------------
    -- instruction tags are updated when instruction fetch
    -- completes or instruction is invalidated
    -----------------------------------------------------------------------
    itag_we: for i in 0 to ICACHE_SETS-1 generate
        idl_1: if ICACHE_WPL = 1 generate
            itmem_we(i) <= (if_done and iset_sel(i)) or inv_ic;
            icmem_we(i)(0) <= (if_done and iset_sel(i));                
        end generate idl_1;
        
        -- write when set is selected & line is selected
        idl_gt1: if ICACHE_WPL > 1 generate
            -- tag updated for last line update
            itmem_we(i) <= (if_done and iset_sel(i) and iline_we_r(ICACHE_WPL - 1)) or inv_ic;
            
            idl_gen: for j in 0 to ICACHE_WPL-1 generate
                icmem_we(i)(j) <= (if_done and iset_sel(i) and iline_we_r(j));
            end generate idl_gen;                
        end generate idl_gt1;
    end generate itag_we;

    -- itag is invalidated when requested
    itag_val(0) <= not (inv_ic);
    
    tagi_i <= IAGU_pc  (ITAG_WIDTH+ICACHE_ADDR_WIDTH-1 downto ICACHE_ADDR_WIDTH);

    -- or the tag compares
    itag_cmp <= or_vect(itcmp);

    -----------------------------------------------------------------------
    -- depending on sets generate the output mux
    -----------------------------------------------------------------------
    isets_1: if ICACHE_SETS = 1 generate
        cache_inst_out <= icache_out(0);
    end generate isets_1;

    isets_2: if ICACHE_SETS = 2 generate
        cache_inst_out <= icache_out(0) when itcmp(0) = '1' else
                          icache_out(1);
    end generate isets_2;

    isets_4: if ICACHE_SETS = 4 generate
        cache_inst_out <= icache_out(0) when itcmp(0) = '1' else
                          icache_out(1) when itcmp(1) = '1' else
                          icache_out(2) when itcmp(2) = '1' else
                          icache_out(3);
    end generate isets_4;

    -----------------------------------------------------------------------
    -- Instruction
    -----------------------------------------------------------------------
    imem_itag: for i in 0 to ICACHE_SETS-1 generate
        type cmem_out_type is array (0 to ICACHE_WPL-1) of std_logic_vector (WIDTH-1 downto 0);

        signal itag_enb : std_logic;
        signal cmem_out : cmem_out_type;
        signal imaddr, itaddr : std_logic_vector (CAWIDTH-1 downto 0) := (others => '0');
        
    begin
        imem: for j in 0 to ICACHE_WPL-1 generate
            signal imem_enb : std_logic;
        begin
            imem_enb <= ienb or icmem_we(i)(j);
            icachemem : cachemem_s
                generic map (WIDTH            => WIDTH,
                             CACHE_ENABLED    => ICACHE_ENABLED,
                             CACHE_ADDR_WIDTH => CAWIDTH)
                port map (cmem_out  => cmem_out(j), cmem_addr => imaddr,
                          cmem_in   => EXM_data_in, cmem_enb  => imem_enb,
                          cmem_rst  => irst, 	cmem_we   => icmem_we(i)(j), clk => clk);                
        end generate imem;
        
        ilw_1: if ICACHE_WPL = 1 generate
            imaddr        <= inst_address;
            itaddr        <= itag_address;
            icache_out(i) <= cmem_out(0);
        end generate ilw_1;

        ilw_2: if ICACHE_WPL = 2 generate
            imaddr <= inst_address(ICACHE_ADDR_WIDTH-1 downto 1);
            itaddr <= itag_address(ICACHE_ADDR_WIDTH-1 downto 1);
            icache_out(i) <= cmem_out(1) when IAGU_pc(2) = '1' else cmem_out(0);
        end generate ilw_2;

        ilw_4: if ICACHE_WPL = 4 generate
            imaddr <= inst_address(ICACHE_ADDR_WIDTH-1 downto 2);
            itaddr <= itag_address(ICACHE_ADDR_WIDTH-1 downto 2);
            icache_out(i) <= cmem_out(3) when IAGU_pc(3 downto 2) = "11" else
                             cmem_out(2) when IAGU_pc(3 downto 2) = "10" else
                             cmem_out(1) when IAGU_pc(3 downto 2) = "01" else cmem_out(0);
        end generate ilw_4;

        ilw_8: if ICACHE_WPL = 8 generate
            imaddr <= inst_address(ICACHE_ADDR_WIDTH-1 downto 3);
            itaddr <= itag_address(ICACHE_ADDR_WIDTH-1 downto 3);
            icache_out(i) <= cmem_out(7) when IAGU_pc(4 downto 2) = "111" else
                             cmem_out(6) when IAGU_pc(4 downto 2) = "110" else
                             cmem_out(5) when IAGU_pc(4 downto 2) = "101" else
                             cmem_out(4) when IAGU_pc(4 downto 2) = "100" else
                             cmem_out(3) when IAGU_pc(4 downto 2) = "011" else
                             cmem_out(2) when IAGU_pc(4 downto 2) = "010" else
                             cmem_out(1) when IAGU_pc(4 downto 2) = "001" else cmem_out(0);                
        end generate ilw_8;
        
        itag_enb <= ienb or inv_ic or itmem_we(i);

        -- no cache then tags have to be wide
        no_icache: if ICACHE_ENABLED = false generate
            signal itago : std_logic_vector (ADDR_WIDTH-3 downto 0);
        begin
            itagmem : tagmem_s
                generic map (CACHE_ENABLED    => ICACHE_ENABLED,
                             TAG_WIDTH        => ADDR_WIDTH-2,
                             CACHE_ADDR_WIDTH => CAWIDTH)
                port map (tagi        => IAGU_pc(ADDR_WIDTH-1 downto 2),
                          tag_address => itaddr,
                          tag_val     => itag_val, tag_enb     => itag_enb,
                          tag_rst     => irst,     tag_we      => itmem_we(i),
                          clk         => clk,      tvalid      => itval_sets_o(i),
                          tago        => itago);                    

            -- compare tags 
            itcmp(i) <= '1' when itval_sets_o(i)(0) & IAGU_pc (ADDR_WIDTH-1 downto 2) = '1' & itago else '0'; 
        end generate no_icache;

        -- cache enabled
        with_icache: if ICACHE_ENABLED = true generate                
            itagmem : tagmem_s
                generic map (CACHE_ENABLED    => ICACHE_ENABLED,
                             TAG_WIDTH        => ITAG_WIDTH,
                             CACHE_ADDR_WIDTH => CAWIDTH)
                port map (tagi        => tagi_i,   tag_address => itaddr,
                          tag_val     => itag_val, tag_enb     => itag_enb,
                          tag_rst     => irst,     tag_we      => itmem_we(i),
                          clk         => clk,      tvalid      => itval_sets_o(i),
                          tago        => itag_sets_o(i));                    

            -- compare tags 
            itcmp(i) <= '1' when itval_sets_o(i)(0) & IAGU_pc (ITAG_WIDTH+ICACHE_ADDR_WIDTH-1 downto ICACHE_ADDR_WIDTH) =
                        '1' & itag_sets_o(i) else '0';
        end generate with_icache;
        
    end generate imem_itag;

end rtl;
