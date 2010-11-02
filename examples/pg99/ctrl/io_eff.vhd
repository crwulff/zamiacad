--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
------------------------------------------------------------------------
------------------------------------------------------------------------
------------------------------------------------------------------------
------------------------------------------------------------------------
------------------------------------------------------------------------
------------------------------------------------------------------------
-- Designers :  Dirk Allmendinger 
-- Group     :  CTRL
--------------------------------------------------------------------
-- Design Unit Name : synchrone Abtaststufe
-- Purpose :
-- 
-- File Name : io_eff.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 98
--------------------------------------------------------------------
-- Date		   | Changes 
--	17.11.1998 | 	27.01.1999
--                 | 
--                 |
-----------------------------------------------------------------------


library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity IO_EFF is
      Port (     CLK : In    std_logic;
                 EIN : In    std_logic_vector (2 downto 0);
                EIN1 : In    std_logic;
                EIN2 : In    std_logic;
                EIN3 : In    std_logic;
                EIN4 : In    std_logic;
                EIN5 : In    std_logic;
               RESET : In    std_logic;
                 AUS : Out   std_logic_vector (2 downto 0);
                AUS1 : Out   std_logic;
                AUS2 : Out   std_logic;
                AUS3 : Out   std_logic;
                AUS4 : Out   std_logic;
                AUS5 : Out   std_logic );
end IO_EFF;

architecture BEHAVIORAL of IO_EFF is

signal ZW1,ZW2,ZW3,ZW4,ZW5 : std_logic;  -- Register
signal ZW : std_logic_vector(2 downto 0);  -- Register


    
begin

-- purpose: Zwischenspeicherung der Eingaenge
-- type:    memorizing
-- inputs:  CLK, RESET, ein1..5
-- outputs: AUS1..5
SAVE : process (CLK, RESET)
    
begin  -- process SAVE
    -- activities triggered by asynchronous reset (active high)
    if RESET = '1' then
	ZW1 <= '1';
	ZW2 <= '1';
	ZW3 <= '1';
	ZW4 <= '1';
	ZW5 <= '1';
	ZW <= "000";
    -- activities triggered by rising edge of clock
    elsif CLK'event and CLK = '1' then

	ZW1 <= EIN1;
	ZW2 <= EIN2;
	ZW3 <= EIN3;
	ZW4 <= EIN4;
	ZW5 <= EIN5;
	ZW  <= EIN;
    end if;
end process SAVE;


AUS  <= ZW;
AUS1 <= ZW1;
AUS2 <= ZW2;
AUS3 <= ZW3;
AUS4 <= ZW4;
AUS5 <= ZW5;

end BEHAVIORAL;

configuration CFG_IO_EFF_BEHAVIORAL of IO_EFF is
   for BEHAVIORAL

   end for;

end CFG_IO_EFF_BEHAVIORAL;
