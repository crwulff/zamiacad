--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------

-- Designers :  Dirk Allmendinger 
-- Group     :  CTRL
--------------------------------------------------------------------------

-- Design Unit Name : synchrone parallele Ausgabe (8,16,32 Bit auf 32 bit)
-- Purpose :
-- 
-- File Name : paraus.vhd
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

entity PARAUS is
      Port (    ADR : In    std_logic_vector(2 downto 0);
                 CLK : In    std_logic;
                MODE : In    std_logic_vector (1 downto 0);
              
             REGINEN : In    std_logic;
               RESET : In    std_logic;
              STATUS : In    std_logic_vector (7 downto 0);
          STATUSINEN : In    std_logic;
                DATA : in    std_logic_vector (31 downto 0);
              PAROUT : Out   std_logic_vector (31 downto 0);
              SEROUT : Out   std_logic_vector (7 downto 0) );
end PARAUS;

architecture SCHEMATIC of PARAUS is

signal y, par_out, reg :  std_logic_vector (31 downto 0);
signal STAT : std_logic_vector(7 downto 0);

begin

REGOUT : process (clk, RESET)
    
begin  -- process reg
    -- activities triggered by asynchronous reset (active high)
    if RESET = '1' then
	reg <= "00000000000000000000000000000000";
    -- activities triggered by rising edge of clock
    elsif clk'event and clk = '1' then
	if REGINEN = '1' then
	    reg <= DATA;
	end if;
    end if;
end process REGOUT;

STATUSREG : process (CLK, RESET)
    
begin  -- process STATUSREG
    -- activities triggered by asynchronous reset (active high)
    if RESET = '1' then
	STAT <= "00000000";
    -- activities triggered by rising edge of clock
    elsif CLK'event and CLK = '1' then
	if STATUSINEN = '1' then
	    STAT <= STATUS;
	end if;
    end if;
end process STATUSREG;

PAR_OUT <= stat & stat & stat & stat when adr(2) = '1' else
	   reg(31 downto 8) & reg(15 downto 8) when (mode(1)='0' and adr = "001") else
	   reg(31 downto 8) & reg(23 downto 16)  when (mode(1)='0' and adr = "010") else
    	   reg(31 downto 8) & reg(31 downto 24) when (mode(1)='0' and adr = "011") else
	   reg(31 downto 16) & reg(31 downto 16) when (mode= "10" and adr = "001") else
	   reg(31 downto 0);

PAROUT <= PAR_OUT;
SEROUT <= PAR_OUT(7 downto 0);

end SCHEMATIC;


configuration CFG_PARAUS_SCHEMATIC of PARAUS is
    
   for SCHEMATIC
   end for;

end CFG_PARAUS_SCHEMATIC;
