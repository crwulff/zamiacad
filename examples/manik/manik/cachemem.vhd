-------------------------------------------------------------------------------
-- Title      : Cache Memory Module
-- Project    : MANIK-II
-------------------------------------------------------------------------------
-- File       : cachemem.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech Inc
-- Created    : 2005-08-05
-- Last update: 2006-10-12
-- Platform   : 
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: Implements Cache Memory Module
-------------------------------------------------------------------------------
-- Copyright (c) 2005 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2005-08-05  1.0      sdutta	Created
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

entity cachemem_s is
    
    generic (WIDTH             : integer := 32;
             CACHE_ENABLED    : boolean;
             CACHE_ADDR_WIDTH : integer);

    port (cmem_out  : out std_logic_vector(WIDTH-1 downto 0);
          cmem_addr : in  std_logic_vector(CACHE_ADDR_WIDTH-1 downto 0);
          cmem_in   : in  std_logic_vector(WIDTH-1 downto 0);
          cmem_enb  : in  std_logic;
          cmem_rst  : in  std_logic;
          cmem_we   : in  std_logic;
          clk       : in  std_logic);

end cachemem_s;

architecture RTL of cachemem_s is
    constant CACHE_SIZE : integer := 2**(CACHE_ADDR_WIDTH);
begin  -- RTL

    assert (Technology = "XILINX" or Technology = "ALTERA" or Technology = "ACTEL" or Technology = "LATTICE")
    report "cachemem supports XILINX & ALTERA Technologies only"
    severity failure;

    ---------------------------------------------------------------------------
    -- 	                        NO CACHE                                    ---
    ---------------------------------------------------------------------------    
    -- if cache memory size is zero, create memory that is one deep
    NO_CACHE_MEM: if CACHE_ENABLED = false generate
        signal cache_mem : std_logic_vector(WIDTH-1 downto 0) := (others => '0');
    begin
        cmem_out <= cache_mem;
        process (clk)
        begin
            if rising_edge(clk) then
                if cmem_enb = '1' then
                    if cmem_rst = '1' then
                        cache_mem <= (others => '0');
                    elsif cmem_we = '1' then
                        cache_mem <= cmem_in;
                    end if;
                end if;
            end if;
        end process ;
    end generate NO_CACHE_MEM;

    ---------------------------------------------------------------------------
    -- 	                        XILINX                                      ---
    ---------------------------------------------------------------------------
    --
    -- Xilinx memory has registers on the output, with synchronous reset
    -- the design depeneds on this behaviour
    --
    xilinx_tech: if Technology = "XILINX" and CACHE_ENABLED = true generate
        --  cache memory
        v2_mem: if FPGA_Family = "Virtex2" generate
            cache_mem : xilspram_v2
                generic map (MEM_DATA_WIDTH => WIDTH,
                             MEM_ADDR_WIDTH => CACHE_ADDR_WIDTH)
                port map   (clk    => clk,
                            addr   => cmem_addr,
                            data_i => cmem_in,
                            enb    => cmem_enb,
                            rst    => cmem_rst,
                            we     => cmem_we,
                            data_o => cmem_out);                
        end generate v2_mem;
        v_mem: if FPGA_Family /= "Virtex2" generate
            cache_mem : xilspram
                generic map (MEM_DATA_WIDTH => WIDTH,
                             MEM_ADDR_WIDTH => CACHE_ADDR_WIDTH)
                port map (clk    => clk,
                          addr   => cmem_addr,
                          data_i => cmem_in,
                          enb    => cmem_enb,
                          rst    => cmem_rst,
                          we     => cmem_we,
                          data_o => cmem_out);                
        end generate v_mem;
    end generate xilinx_tech;

    ---------------------------------------------------------------------------
    -- 	                        ALTERA                                      ---
    ---------------------------------------------------------------------------
    altera_tech: if Technology = "ALTERA" and CACHE_ENABLED = true generate
    begin
        cache_mem : altsyncram
            GENERIC MAP (ram_block_type         => "AUTO",
                         operation_mode         => "SINGLE_PORT",
                         width_a                => WIDTH,
                         widthad_a              => CACHE_ADDR_WIDTH,
                         numwords_a             => CACHE_SIZE,
                         lpm_type               => "altsyncram",
                         width_byteena_a        => 1,
                         outdata_reg_a          => "UNREGISTERED",
                         outdata_aclr_a         => "NONE",
                         indata_aclr_a          => "NONE",
                         wrcontrol_aclr_a       => "NONE",
                         address_aclr_a         => "NONE",
                         power_up_uninitialized => "FALSE",
                         init_file              => "",
--                       lpm_hint               => "ENABLE_RUNTIME_MOD=YES,INSTANCE_NAME=CACHE",
                         intended_device_family => Altera_Family)
            PORT MAP (wren_a    => cmem_we,
                      clock0    => clk,
                      clocken0  => cmem_enb,
                      address_a => cmem_addr,
                      data_a    => cmem_in,
                      q_a       => cmem_out);
    end generate altera_tech;
    
    ---------------------------------------------------------------------------
    -- 	                        LATTICE                                      ---
    ---------------------------------------------------------------------------
    lattice_tech: if Technology = "LATTICE" and CACHE_ENABLED = true generate
        ecp_mem: if Lattice_Family = "ECP" generate
            cache_mem : latspram_ecp
                generic map (MEM_DATA_WIDTH => WIDTH,
                             MEM_ADDR_WIDTH => CACHE_ADDR_WIDTH)
                port map (clk    => clk,
                          addr   => cmem_addr,
                          data_i => cmem_in,
                          enb    => cmem_enb,
                          rst    => cmem_rst,
                          we     => cmem_we,
                          data_o => cmem_out);                
        end generate ecp_mem;
    end generate lattice_tech;
    
end RTL;
