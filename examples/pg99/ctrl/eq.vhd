--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designer  : Jens Kuenzer, Markus Busch              
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : eq
-- Purpose : Part of the CTRL-module-core for the cryptochip "pg99"
-- 
-- File Name :  eq.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
--     ????	   |    Created with Symboleditor by Jens Kuenzer
--   18 Nov 98     |    Interface Canged Code written
--   07.Dec 98     |    Rewritten with old Interface by Jens Kuenzer
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  contents: Comperator fuer 32 Bit Vergleiche
--  
--------------------------------------------------------------------------

-- library HAPRA_GATE;
--   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;

entity EQ is
      Port (     CLK : In    std_logic;
                   I : In    std_logic_vector (31 downto 0);
               RESET : In    std_logic;
                  EN : In    std_logic;
                  EQ : Out   std_logic );
end EQ;

architecture BEHAVIORAL of EQ is
    signal A,B : std_logic_vector(31 downto 0);
begin
    process (CLK, RESET)
    begin  -- process
	-- activities triggered by asynchronous reset (active high)
	if RESET = '1' then
	    A <= (others => '0');
	    B <= (others => '0');
	    -- activities triggered by rising edge of clock
	elsif CLK'event and CLK = '1' then
	    if  EN = '1' then
		A <= I;
	    end if;
	    B <= I;
	end if;
    end process;

    EQ <= '1' when A = B else '0';
    
end BEHAVIORAL;

architecture OLD_BEHAVIORAL of EQ is
    signal SPEICHER : std_logic_vector(31 downto 0);
begin
    process (CLK, RESET)
    begin  -- process
	-- activities triggered by asynchronous reset (active high)
	if RESET = '1' then
	    SPEICHER <= (others => '0');
	    -- activities triggered by rising edge of clock
	elsif CLK'event and CLK = '1' then
	    if  EN = '1' then
		SPEICHER <= I;
	    end if;
	end if;
    end process;

    -- flipflop for EQ <= '1' when I = SPEICHER else '0';
    process (CLK, RESET)
    begin  -- process
	-- activities triggered by asynchronous reset (active high)
	if RESET = '1' then
	    EQ <= '0';
	-- activities triggered by rising edge of clock
	elsif CLK'event and CLK = '1' then
	    if I = SPEICHER then
		EQ <= '1';
	    else
		EQ <= '0';	
	    end if;
	end if;
    end process;
   
end OLD_BEHAVIORAL;
