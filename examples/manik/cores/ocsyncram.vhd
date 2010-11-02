-------------------------------------------------------------------------------
-- Title      : On Chip Sync RAM
-- Project    : MANIK-II
-------------------------------------------------------------------------------
-- File       : ocsyncram.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech Inc.
-- Created    : 2005-07-14
-- Last update: 2006-08-25
-- Platform   : 
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: On chip Syncronous ram , with Wishbone interface, initialized
-- with gdbstub code, or elf loader code. Supports only XILINX & Altera
-- for Xilinx-Virtex2,Spartan-3/E,Virtex-4 series instantiates 8K (4 RAMB16s)
-- for the Virtex & Spartan-II/E family instantiates 4k (8 RAMB4s) [elf loader
-- only]. For other configs contact NikTech Inc.
-------------------------------------------------------------------------------
-- Copyright (c) 2005 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2005-07-14  1.0      Sandeep	Created
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;
use work.manikaltera.all;
use std.textio.all;

entity ocsyncram is
    
    generic (WIDTH      : integer := 32;
             ADDR_WIDTH : integer := 32;
             RAM_AWIDTH : integer := 11;   -- default allocation 8K
             RAM_INITFILE : string := "../../gdbstub/gdbstub.mem");

    port (clk   : std_logic;
          reset : std_logic;
          
          -- Wishbone slave interface
          WBS_ADR_I : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
          WBS_SEL_I : in  std_logic_vector (3 downto 0);
          WBS_DAT_I : in  std_logic_vector (WIDTH-1 downto 0);
          WBS_WE_I  : in  std_logic;
          WBS_STB_I : in  std_logic;
          WBS_CYC_I : in  std_logic;
          WBS_CTI_I : in  std_logic_vector (2 downto 0);
          WBS_BTE_I : in  std_logic_vector (1 downto 0);
          WBS_DAT_O : out std_logic_vector (WIDTH-1 downto 0);
          WBS_ACK_O : out std_logic;
          WBS_ERR_O : out std_logic);
    
end ocsyncram;

architecture RTL of ocsyncram is

    signal ben : std_logic_vector (3 downto 0) := "0000";
    signal wen : std_logic_vector (3 downto 0) := "0000";    
    signal ack : std_logic := '0';
    
begin  -- RTL

    ben(0) <= WBS_STB_I and WBS_SEL_I(3);
    ben(1) <= WBS_STB_I and WBS_SEL_I(2);
    ben(2) <= WBS_STB_I and WBS_SEL_I(1);
    ben(3) <= WBS_STB_I and WBS_SEL_I(0);

    wen(0) <= WBS_WE_I and WBS_STB_I and WBS_SEL_I(3);
    wen(1) <= WBS_WE_I and WBS_STB_I and WBS_SEL_I(2);
    wen(2) <= WBS_WE_I and WBS_STB_I and WBS_SEL_I(1);
    wen(3) <= WBS_WE_I and WBS_STB_I and WBS_SEL_I(0);
    
    -- takes 1 cycle to read/write
    ack_proc : process (clk, reset)
    begin
        if reset = '1' then
            ack <= '0';
        elsif rising_edge(clk) then
            if ack = '1' then
                ack <= '0' after 1 ns;
            else
                ack <= WBS_STB_I after 1 ns;
            end if;
        end if;
    end process ack_proc;

    WBS_ACK_O <= ack;
    WBS_ERR_O <= '0';
    
    xilinx_tech: if Technology = "XILINX" generate
        constant mem_size    : positive := 2**RAM_AWIDTH;

        type RamType is array(mem_size-1 downto 0) of bit_vector(WIDTH-1 downto 0);

        -- function to initialize ram from a file
        function InitRamFromFile (RamFileName : in String) return RamType is
	-- synopsys translate_off
            file RamFile         : text is in RamFileName;
            variable RamFileLine : line;
            variable RamWord     : bit_vector (WIDTH-1 downto 0);
	-- synopsys translate_on
            variable vRam        : RamType;        
        begin
            -- synopsys translate_off
            for i in 0 to mem_size-1 loop
                readline (RamFile, RamFileLine);
                exit when endfile (RamFile);
                read (RamFileLine, RamWord);
                vRam(i) := RamWord;
            end loop;  -- i
	    -- synopsys translate_on
            return vRam;
        end function;
    
        signal raddr,waddr : std_logic_vector (RAM_AWIDTH-1 downto 0) := (others => '0');
        signal data_int    : std_logic_vector (WIDTH-1 downto 0);
        signal data_w      : std_logic_vector (WIDTH-1 downto 0);
        signal  we         : std_logic := '0' ;

        signal memory    : RamType  := InitRamFromFile(RAM_INITFILE);

    begin
        -- register read address
        process (clk)
        begin
            if rising_edge(clk) then
                raddr <= WBS_ADR_I(RAM_AWIDTH+1 downto 2);
            end if;
        end process;
        waddr <= WBS_ADR_I(RAM_AWIDTH+1 downto 2);
    
        process (clk, reset)
        begin
            if reset = '1' then
                we    <= '0';
            elsif rising_edge(clk) then
                if WBS_STB_I = '1' and WBS_WE_I = '1'  and ack = '0' then
                    we <= '1';
                else
                    we <= '0';
                end if;      
            end if;
        end process ;
        
        process (clk)
        begin
            if rising_edge(clk) then
                if we = '1' then            
                    memory(conv_integer(waddr)) <= to_bitvector(data_w);
                end if;
            end if;
        end process ;
        
        data_int   <= to_stdlogicvector(memory(conv_integer(raddr)));
        data_w(WIDTH-1 downto 3*WIDTH/4) <= WBS_DAT_I(WIDTH-1 downto 3*WIDTH/4) when WBS_SEL_I(3) = '1' else
                                            data_int(WIDTH-1 downto 3*WIDTH/4); 
        data_w((3*WIDTH/4)-1 downto 2*WIDTH/4) <= WBS_DAT_I((3*WIDTH/4)-1 downto 2*WIDTH/4) when WBS_SEL_I(2) = '1' else
                                            data_int((3*WIDTH/4)-1 downto 2*WIDTH/4); 
        data_w((2*WIDTH/4)-1 downto 1*WIDTH/4) <= WBS_DAT_I((2*WIDTH/4)-1 downto 1*WIDTH/4) when WBS_SEL_I(1) = '1' else
                                            data_int((2*WIDTH/4)-1 downto 1*WIDTH/4); 
        data_w((1*WIDTH/4)-1 downto 0*WIDTH/4) <= WBS_DAT_I((1*WIDTH/4)-1 downto 0*WIDTH/4) when WBS_SEL_I(0) = '1' else
                                            data_int((1*WIDTH/4)-1 downto 0*WIDTH/4); 
        WBS_DAT_O <= data_int;

    end generate xilinx_tech;

    altera_tech: if Technology = "ALTERA" generate
        signal we : std_logic;
    begin        
        we <= WBS_STB_I and WBS_WE_I;
        OCSRAM : altsyncram
            generic map(byte_size                          => 8,
                        init_file                          => RAM_INITFILE,
                        lpm_type                           => "altsyncram",
                        numwords_a                         => 2**RAM_AWIDTH,
                        operation_mode                     => "SINGLE_PORT",
                        outdata_reg_a                      => "UNREGISTERED",
                        ram_block_type                     => "M4K",
                        read_during_write_mode_mixed_ports => "DONT_CARE",
--                        lpm_hint 	                   => "ENABLE_RUNTIME_MOD=YES,INSTANCE_NAME=OCSR",
                        width_a                            => WIDTH,
                        width_byteena_a                    => WIDTH/8,
                        widthad_a                          => RAM_AWIDTH)
            port map(address_a => WBS_ADR_I(RAM_AWIDTH+1 downto 2),
                     byteena_a => WBS_SEL_I,
                     clock0    => clk,
                     data_a    => WBS_DAT_I,
                     q_a       => WBS_DAT_O,
                     wren_a    => we);
    end generate altera_tech;
end RTL;
