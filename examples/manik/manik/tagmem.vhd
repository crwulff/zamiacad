-------------------------------------------------------------------------------
-- Title      : Cache Tag Memory module
-- Project    : MANIK-II
-------------------------------------------------------------------------------
-- File       : tagmem.vhd
-- Author     : Sandeep Dutta 
-- Company    : NikTech Inc
-- Created    : 2005-07-24
-- Last update: 2006-10-09
-- Platform   : 
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: Cache Tag Memory module
-------------------------------------------------------------------------------
-- Copyright (c) 2005 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2005-07-24  1.0      Sandeep	Created
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library UNISIM;
use UNISIM.vcomponents.all;

use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;
use work.manikaltera.all;
use work.maniklattice.all;
use work.manikactel.all;

entity tagmem_s is
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

end tagmem_s;

architecture RTL of tagmem_s is
    constant CACHE_SIZE : integer := 2**CACHE_ADDR_WIDTH;
begin  -- tagmem_s
    
    -- synopsys translate_off
    assert (Technology = "XILINX" or Technology = "ALTERA" or Technology = "LATTICE")
        report "tagmem module supports XILINX & ALTERA Technology only" severity FAILURE;
    -- synopsys translate_on
    ---------------------------------------------------------------------------
    -- 	                        NO CACHE                                    ---
    ---------------------------------------------------------------------------
    -- if cache memory size is zero, create memory that is one deep
    NO_CACHE_MEM: if CACHE_ENABLED = false generate
        signal tag_mem : std_logic_vector(TAG_WIDTH downto 0) := (others => '0');
    begin
        tag_proc : process (clk)
        begin
            if rising_edge(clk) then
                if tag_enb = '1' then
                    if tag_rst = '1' then
                        tag_mem <= (others => '0');
                    elsif tag_we = '1' then
                        tag_mem <= tag_val(0) & tagi;
--                    else
--                        tag_mem <= '0' & tag_mem(TAG_WIDTH-1 downto 0);
                    end if;
                end if;
            end if;
        end process tag_proc;
        tago      <= tag_mem(TAG_WIDTH-1 downto 0);
        tvalid(0) <= tag_mem(TAG_WIDTH);
    end generate NO_CACHE_MEM;

    ---------------------------------------------------------------------------
    -- 	                        XILINX                                      ---
    ---------------------------------------------------------------------------
    xilinx_tech: if Technology = "XILINX" and CACHE_ENABLED = true generate
        signal tag_i, tag_o  : std_logic_vector(TAG_WIDTH downto 0);
    begin
        tag_i <= tag_val(0) & tagi;
        v2_mem: if FPGA_Family = "Virtex2" generate
            tag_mem : xilspram_v2
                generic map (MEM_DATA_WIDTH => TAG_WIDTH+1,
                             MEM_ADDR_WIDTH => CACHE_ADDR_WIDTH)
                port map (clk    => clk,
                          addr   => tag_address,
                          data_i => tag_i,
                          enb    => tag_enb,
                          rst    => tag_rst,
                          we     => tag_we,
                          data_o => tag_o);                
        end generate v2_mem;

        v_mem: if FPGA_Family /= "Virtex2" generate
            tag_mem : xilspram
                generic map (MEM_DATA_WIDTH => TAG_WIDTH+1,
                             MEM_ADDR_WIDTH => CACHE_ADDR_WIDTH)
                port map (clk    => clk,
                          addr   => tag_address,
                          data_i => tag_i,
                          enb    => tag_enb,
                          rst    => tag_rst,
                          we     => tag_we,
                          data_o => tag_o);                                
            end generate v_mem;
            
            tvalid(0) <= tag_o(TAG_WIDTH);
            tago      <= tag_o(TAG_WIDTH-1 downto 0);
    end generate xilinx_tech;

    ---------------------------------------------------------------------------
    -- 	                        ALTERA                                      ---
    ---------------------------------------------------------------------------
    altera_tech: if Technology = "ALTERA" and CACHE_ENABLED = true generate
        signal data    : std_logic_vector(TAG_WIDTH downto 0);
        signal q       : std_logic_vector(TAG_WIDTH downto 0);
        signal rst_dly : std_logic := '0';
    begin
        -- delay reset by a cycle
        process (clk)
        begin
            if rising_edge(clk) then
                rst_dly <= tag_rst;
            end if;
        end process;        
        
        data <= tag_val(0) & tagi(TAG_WIDTH-1 downto 0);
        tag_mem : altsyncram
            GENERIC MAP (ram_block_type         => "AUTO",
                         operation_mode         => "SINGLE_PORT",
                         width_a                => TAG_WIDTH+1,
                         widthad_a              => CACHE_ADDR_WIDTH,
                         numwords_a             => CACHE_SIZE/4,
                         lpm_type               => "altsyncram",
                         width_byteena_a        => 1,
                         outdata_reg_a          => "UNREGISTERED",
                         outdata_aclr_a         => "NONE",
                         indata_aclr_a          => "NONE",
                         wrcontrol_aclr_a       => "NONE",
                         address_aclr_a         => "NONE",
                         power_up_uninitialized => "FALSE",
                         init_file              => "",
--                       lpm_hint               => "ENABLE_RUNTIME_MOD=YES,INSTANCE_NAME=ITAG",
                         intended_device_family => Altera_Family)
            PORT MAP (wren_a    => tag_we,
                      clock0    => clk,
                      clocken0  => tag_enb,
                      address_a => tag_address,
                      data_a    => data,
                      q_a       => q);        
        tago <= ALLZEROS(TAG_WIDTH-1 downto 0) when rst_dly = '1' else q(TAG_WIDTH-1 downto 0);
        tvalid(0) <= '0' when rst_dly = '1' else q(TAG_WIDTH);                    
    end generate altera_tech;

    ---------------------------------------------------------------------------
    -- 	                        LATTICE                                      ---
    ---------------------------------------------------------------------------
    lattice_tech: if Technology = "LATTICE" and CACHE_ENABLED = true generate
        signal rst_dly : std_logic := '0';
        signal tag_i, tag_o  : std_logic_vector(TAG_WIDTH downto 0);
    begin
        -- delay reset by a cycle
        process (clk)
        begin
            if rising_edge(clk) then
                rst_dly <= tag_rst;
            end if;
        end process;        

        tag_i <= tag_val(0) & tagi;
        ecp_mem: if Lattice_Family = "ECP" generate
            tag_mem : latspram_ecp
                generic map (MEM_DATA_WIDTH => TAG_WIDTH+1,
                             MEM_ADDR_WIDTH => CACHE_ADDR_WIDTH)
                port map (clk    => clk,
                          addr   => tag_address,
                          data_i => tag_i,
                          enb    => tag_enb,
                          rst    => '0',
                          we     => tag_we,
                          data_o => tag_o);
        end generate ecp_mem;
            
        tago <= ALLZEROS(TAG_WIDTH-1 downto 0) when rst_dly = '1' else tag_o(TAG_WIDTH-1 downto 0);
        tvalid(0) <= '0' when rst_dly = '1' else tag_o(TAG_WIDTH);            
        
    end generate lattice_tech;
    
end RTL;

