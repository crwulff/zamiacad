library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_misc.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
use ieee.std_logic_textio.all;
use std.textio.all;
use work.mlite_pack.all;

entity sram2mlite is
   port(clk             : in std_logic;
        -- communication pins with mlite cpu
        mem_byte_sel    : out std_logic_vector(3 downto 0);
        mem_write       : out std_logic;
        mem_address     : out std_logic_vector(31 downto 0);
        mem_data_w      : out std_logic_vector(31 downto 0);
        mem_data_r      : in  std_logic_vector(31 downto 0);
	-- communication pins with SRAM on xsv300 board
        sram_we_hi      : in std_logic;
        sram_we_lo      : in std_logic;
        sram_ce_hi      : in std_logic;
        sram_ce_lo      : in std_logic;
        sram_oe_hi      : in std_logic;
        sram_oe_lo      : in std_logic;
        sram_address_hi : in std_logic_vector(18 downto 0);
        sram_address_lo : in std_logic_vector(18 downto 0);
        sram_data_hi    : inout std_logic_vector(15 downto 0);
        sram_data_lo    : inout std_logic_vector(15 downto 0));
end; --entity ram

architecture logic of sram2mlite is
-- The purpose of this module is to verify the sram communication. It
-- supplies an interface to the generic ram supplied with the plasma
-- cpu core.
begin
   -- select bytes for writing
   mem_byte_sel <= NOT(sram_we_hi & sram_we_hi & sram_we_lo & sram_we_lo);

   -- chose weather to write to ram or to read from it
   mem_write <= sram_oe_hi AND sram_oe_lo;

   -- set memory address
   mem_address <= "0000000000000" & sram_address_hi;

   -- set write bus
   mem_data_w <= sram_data_hi & sram_data_lo when sram_oe_hi='1' else 
                 "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ";

   -- set read bus
   sram_data_hi <= mem_data_r(31 downto 16) when sram_oe_hi='0' else
                   "ZZZZZZZZZZZZZZZZ";
   sram_data_lo <= mem_data_r(15 downto 0) when sram_oe_hi='0' else
                   "ZZZZZZZZZZZZZZZZ";
end;
