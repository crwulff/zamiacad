-------------------------------------------------------------------------------
-- Title      : Data cache using single ported RAM
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : dcache_sp.vhd
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


entity dcache_sp is
    generic (WIDTH             : integer;
             DCACHE_ENABLED    : boolean;
             ADDR_WIDTH        : integer;
             DCACHE_SETS       : integer;
             DTAG_WIDTH        : integer;
             DCACHE_ADDR_WIDTH : integer;
             DCACHE_WPL        : integer;
             CAWIDTH           : integer);
    port (clk            : in  std_logic;
          INTR_reset     : in  std_logic;
          df_done        : in  std_logic;
          sbh            : in  std_logic;
          drst           : in  std_logic;
          bdflag         : in  std_logic;
          store_stb      : in  std_logic;
          exm_buserr     : in  std_logic;
          dline_we_r     : in  std_logic_vector (DCACHE_WPL-1 downto 0);
          data_address   : in  std_logic_vector (DCACHE_ADDR_WIDTH-1 downto 0);
          daddr_reg      : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
          cache_data_in  : in  std_logic_vector (WIDTH-1 downto 0);
          dtag_cmp       : out std_logic;
          cache_data_out : out std_logic_vector (WIDTH-1 downto 0));
end dcache_sp;

architecture rtl of dcache_sp is
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
    
    type dtago_type   is array (DCACHE_SETS-1 downto 0) of std_logic_vector (DTAG_WIDTH-1 downto 0);
    type dtagval_type is array (DCACHE_SETS-1 downto 0) of std_logic_vector (0 downto 0);
    type dcout_type   is array (DCACHE_SETS-1 downto 0) of std_logic_vector (WIDTH-1 downto 0);
    type dmemw_type   is array (DCACHE_SETS-1 downto 0) of std_logic_vector (DCACHE_WPL-1 downto 0);
        
    signal dtval_sets_o        : dtagval_type;
    signal dtval_sets_i        : dtagval_type;
    signal dtag_sets_o         : dtago_type;
    signal dcache_out          : dcout_type;
    signal dcmem_we, udline_we : dmemw_type;
    signal exm_data_reg        : std_logic_vector (WIDTH-1 downto 0)             := (others => '0');
    signal da                  : std_logic_vector (ADDR_WIDTH-1 downto 0)        := (others => '0');
    signal tagi_d              : std_logic_vector (DTAG_WIDTH-1 downto 0)        := (others => '0');
    signal write_hit           : std_logic_vector (DCACHE_SETS-1 downto 0)       := (others => '0');
    signal dtcmp               : std_logic_vector (DCACHE_SETS-1 downto 0)       := (others => '0');
    signal dset_sel, dtmem_we  : std_logic_vector (DCACHE_SETS-1 downto 0)       := (others => '0');

    constant dset_zero : std_logic_vector (DCACHE_SETS-1 downto 0) := (others => '0');        
    constant duzero : std_logic_vector(ADDR_WIDTH-DTAG_WIDTH-DCACHE_ADDR_WIDTH+2 downto 0):=(others=>'0');

begin  -- rtl

    -----------------------------------------------------------------------
    -- Data
    -----------------------------------------------------------------------

    -- data side
    process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            dset_sel     <= (others => '0');
            exm_data_reg <= (others => '0');
        elsif rising_edge(clk) then
            if dset_sel = dset_zero then
                dset_sel <= conv_std_logic_vector(1,DCACHE_SETS);
            elsif df_done = '1' and dline_we_r(DCACHE_WPL-1) = '1' then
                if DCACHE_SETS > 1 then
                    dset_sel <= dset_sel(DCACHE_SETS-2 downto 0) & dset_sel(DCACHE_SETS-1);
                else
                    dset_sel(0) <= '1';
                end if;                    
            end if;

            if df_done = '1' then
                exm_data_reg <= cache_data_in;
            end if;
        end if;
    end process ;

    tagi_d <= daddr_reg(DTAG_WIDTH+DCACHE_ADDR_WIDTH-1 downto DCACHE_ADDR_WIDTH);

    -- or tags from different sets
    dtag_cmp <= or_vect(dtcmp);
    
    -----------------------------------------------------------------------
    -- data tags are updated for stores and when a data load completes
    -----------------------------------------------------------------------
    dtag_we: for i in 0 to DCACHE_SETS-1 generate
        write_hit(i)   	<= store_stb and dtcmp(i);
        ddl_1: if DCACHE_WPL = 1 generate
            dtmem_we(i)    <= (df_done and not EXM_buserr and dset_sel(i)) or store_stb or bdflag;
            dcmem_we(i)(0) <= (df_done and not EXM_buserr and dset_sel(i)) or (store_stb and not sbh) ;
            -- if one word per line then we will update cache line with the
            -- write.
            dtval_sets_i(i)(0)  <= not (bdflag or sbh);
        end generate ddl_1;
        ddl_gt1: if DCACHE_WPL > 1 generate
            -- if more than one word per line then we will update iff write_hit
            dtval_sets_i(i)(0)  <= not (bdflag or sbh or (store_stb and not dtcmp(i)));
            dtmem_we(i) <= (df_done and not EXM_buserr and dset_sel(i) and dline_we_r(DCACHE_WPL-1)) or
                           store_stb or bdflag;
            ddl_gen: for j in 0 to DCACHE_WPL - 1 generate
                dcmem_we(i)(j) <= (df_done and not EXM_buserr and dset_sel(i) and dline_we_r(j)) or
                                  (write_hit(i) and udline_we(i)(j) and not sbh);
            end generate ddl_gen;
        end generate ddl_gt1;
    end generate dtag_we;

    -----------------------------------------------------------------------
    -- depending on sets generate the output mux
    -----------------------------------------------------------------------
    dsets_1: if DCACHE_SETS = 1 generate
        -- when only one cache line then we get the data from
        -- from the cache
        dlw_1: if DCACHE_WPL = 1 generate
            cache_data_out <= dcache_out(0) ;
        end generate dlw_1;
        -- if more than one then we get it from bypass registers
        dlw_gt1: if DCACHE_WPL > 1 generate
            cache_data_out <= dcache_out(0) when dtcmp(0) = '1' else
                              exm_data_reg;                
        end generate dlw_gt1;
    end generate dsets_1;

    dsets_2: if DCACHE_SETS = 2 generate
        cache_data_out <= dcache_out(0) when dtcmp(0) = '1' else
                          dcache_out(1) when dtcmp(1) = '1' else
                          exm_data_reg;
    end generate dsets_2;

    dsets_4: if DCACHE_SETS = 4 generate
        cache_data_out <= dcache_out(0) when dtcmp(0) = '1' else
                          dcache_out(1) when dtcmp(1) = '1' else
                          dcache_out(2) when dtcmp(2) = '1' else
                          dcache_out(3) when dtcmp(3) = '1' else
                          exm_data_reg;
    end generate dsets_4;
    
    dmem_dtag: for i in 0 to DCACHE_SETS-1 generate
        type cmem_out_type is array (0 to DCACHE_WPL-1) of std_logic_vector (WIDTH-1 downto 0);

        signal cmem_out : cmem_out_type;
        signal dmaddr : std_logic_vector (CAWIDTH-1 downto 0) := (others => '0');
    begin
        dmem: for j in 0 to DCACHE_WPL-1 generate
            dcachemem : cachemem_s
                generic map (WIDTH => WIDTH,
                             CACHE_ENABLED => DCACHE_ENABLED,
                             CACHE_ADDR_WIDTH => CAWIDTH)
                port map (cmem_out => cmem_out(j),  cmem_addr => dmaddr,
                          cmem_in  => cache_data_in,cmem_enb  => '1',
                          cmem_rst => '0', 		cmem_we   => dcmem_we(i)(j),
                          clk      => clk);
        end generate dmem;

        dlw_1: if DCACHE_WPL = 1 generate
            dmaddr        <= data_address;
            dcache_out(i) <= cmem_out(0);
            udline_we(i)(0) <= '0';
        end generate dlw_1;

        dlw_2: if DCACHE_WPL = 2 generate
            udline_we(i)(0) <= not daddr_reg(2);
            udline_we(i)(1) <= daddr_reg(2);
            dmaddr <= data_address(DCACHE_ADDR_WIDTH-1 downto 1);
            dcache_out(i) <= cmem_out(1) when daddr_reg(2) = '1' else cmem_out(0);
        end generate dlw_2;

        dlw_4: if DCACHE_WPL = 4 generate
            udline_we(i)(0) <= '1' when daddr_reg(3 downto 2) = "00" else '0';
            udline_we(i)(1) <= '1' when daddr_reg(3 downto 2) = "01" else '0';
            udline_we(i)(2) <= '1' when daddr_reg(3 downto 2) = "10" else '0';
            udline_we(i)(3) <= '1' when daddr_reg(3 downto 2) = "11" else '0';
            
            dmaddr <= data_address(DCACHE_ADDR_WIDTH-1 downto 2);
            dcache_out(i) <= cmem_out(3) when daddr_reg(3 downto 2) = "11" else
                             cmem_out(2) when daddr_reg(3 downto 2) = "10" else
                             cmem_out(1) when daddr_reg(3 downto 2) = "01" else cmem_out(0);
        end generate dlw_4;
        
        dlw_8: if DCACHE_WPL = 8 generate
            udline_we(i)(0) <= '1' when daddr_reg(4 downto 2) = "000" else '0';
            udline_we(i)(1) <= '1' when daddr_reg(4 downto 2) = "001" else '0';
            udline_we(i)(2) <= '1' when daddr_reg(4 downto 2) = "010" else '0';
            udline_we(i)(3) <= '1' when daddr_reg(4 downto 2) = "011" else '0';
            udline_we(i)(4) <= '1' when daddr_reg(4 downto 2) = "100" else '0';
            udline_we(i)(5) <= '1' when daddr_reg(4 downto 2) = "101" else '0';
            udline_we(i)(6) <= '1' when daddr_reg(4 downto 2) = "110" else '0';
            udline_we(i)(7) <= '1' when daddr_reg(4 downto 2) = "111" else '0';
            
            dmaddr <= data_address(DCACHE_ADDR_WIDTH-1 downto 3);
            dcache_out(i) <= cmem_out(7) when daddr_reg(4 downto 2) = "111" else
                             cmem_out(6) when daddr_reg(4 downto 2) = "110" else
                             cmem_out(5) when daddr_reg(4 downto 2) = "101" else
                             cmem_out(4) when daddr_reg(4 downto 2) = "100" else
                             cmem_out(3) when daddr_reg(4 downto 2) = "011" else
                             cmem_out(2) when daddr_reg(4 downto 2) = "010" else
                             cmem_out(1) when daddr_reg(4 downto 2) = "001" else cmem_out(0);                
        end generate dlw_8;

        no_cache: if DCACHE_ENABLED = false generate
            signal dtago : std_logic_vector (WIDTH-3 downto 0);
        begin
            dtagmem : tagmem_s
                generic map (CACHE_ENABLED    => DCACHE_ENABLED,
                             TAG_WIDTH        => WIDTH-2,
                             CACHE_ADDR_WIDTH => CAWIDTH)
                port map (tagi    => daddr_reg(WIDTH-1 downto 2),
                          tag_address => dmaddr,
                          tag_val => dtval_sets_i(i), tag_enb => '1',
                          tag_rst => drst,     tag_we  => dtmem_we(i),
                          clk     => clk,      tvalid  => dtval_sets_o(i),
                          tago    => dtago);

            -- compare tags
            dtcmp(i) <= dtval_sets_o(i)(0) when daddr_reg (WIDTH-1 downto 2) = dtago else '0';                            
        end generate no_cache;

        with_cache: if DCACHE_ENABLED = true generate
            dtagmem : tagmem_s
                generic map (CACHE_ENABLED    => DCACHE_ENABLED,
                             TAG_WIDTH        => DTAG_WIDTH,
                             CACHE_ADDR_WIDTH => CAWIDTH)
                port map (tagi    => tagi_d,   tag_address => dmaddr,
                          tag_val => dtval_sets_i(i), tag_enb => '1',
                          tag_rst => drst,     tag_we  => dtmem_we(i),
                          clk     => clk,      tvalid  => dtval_sets_o(i),
                          tago    => dtag_sets_o(i));

            -- compare tags
            dtcmp(i) <= dtval_sets_o(i)(0) when
                        daddr_reg (DTAG_WIDTH+DCACHE_ADDR_WIDTH-1 downto DCACHE_ADDR_WIDTH) = dtag_sets_o(i)
                        else '0';                            
        end generate with_cache;
    end generate dmem_dtag;

end rtl;
