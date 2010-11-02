--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------

-- Designers :  Dirk Allmendinger 
-- Group     :  CTRL
--------------------------------------------------------------------------

-- Design Unit Name : Start- / Stopdetect (serial module)
-- Purpose :
-- 
-- File Name : startstopdetect.vhd
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


entity STARTSTOPDETECT is
      Port (     CLK : In    std_logic;
               RESET : In    std_logic;
               S_RES : In    std_logic;
               SCLIN : In    std_logic;
               SDAIN : In    std_logic;
               START : Out   std_logic;
                STOP : Out   std_logic );
end STARTSTOPDETECT;

architecture SCHEMATIC of STARTSTOPDETECT is


type STATES is 

    (
	-- INIT & CO
	RESET_1,
	TESTEN_1,
	STARTTEST_1, STARTTEST_2, STARTTEST_3,
	STOPTEST_1, STOPTEST_2, STOPTEST_3 
	);



signal STATE : STATES;
signal NEWSTATE : STATES;
signal STARTOUT : std_logic;
signal STOPOUT : std_logic;
signal NEW_STARTOUT : std_logic;
signal NEW_STOPOUT : std_logic;

begin 
   
 -- purpose: speicherung des zustands, reset ,comando
    process(CLK,RESET)
    begin
	if RESET='1' then
	    STATE <= RESET_1;
	elsif CLK'event AND CLK='1' then
            if S_RES = '1' then
		STATE <= RESET_1;
	    else
	    STATE <= NEWSTATE;
	    end if;
	end if;
    end process;


detect : process(STATE, SDAIN, SCLIN, STARTOUT, STOPOUT)
    
begin  -- process detect

NEW_STARTOUT <= STARTOUT;
NEW_STOPOUT <= STOPOUT;
    
case state is 
   
    when RESET_1  =>
-- 	STARTOUT <= '0';
-- 	STOPOUT  <= '0';
	NEW_STARTOUT <= '0';
	NEW_STOPOUT  <= '0';
	NEWSTATE <= TESTEN_1;

    when TESTEN_1 =>

        if SDAIN='1' then
	    NEWSTATE <= STARTTEST_1;
	else
	    NEWSTATE <= STOPTEST_1;	
	end if;

    when STARTTEST_1 =>

	if SDAIN ='0' then
	    NEWSTATE <= STARTTEST_2;
	else		       
	    NEWSTATE <= STARTTEST_1;	     
	end if;

    when STARTTEST_2 =>
	if SCLIN = '1' then
	    NEWSTATE <= STARTTEST_3;
	else
	    NEWSTATE <= TESTEN_1;
	end if;

    when STARTTEST_3 =>
	NEW_STARTOUT <= '1';
	NEWSTATE <= TESTEN_1;

    when STOPTEST_1 =>
	if SDAIN = '1' then
	    NEWSTATE <= STOPTEST_2;
	else
	    NEWSTATE <= STOPTEST_1;
	end if;

    when STOPTEST_2 =>
	if SCLIN = '1' then
	    NEWSTATE <= STOPTEST_3;
	else
	    NEWSTATE <= TESTEN_1;
	end if;

    when STOPTEST_3 =>
	NEW_STOPOUT <= '1';
	NEWSTATE <= TESTEN_1;

    when others =>
	NEWSTATE <= RESET_1;
	
end case;
    
end process detect;

DETECTFF : process (CLK, RESET)
    
begin  -- process DETECTFF
    -- activities triggered by asynchronous reset (active high)
    if RESET = '1' then
	STARTOUT <= '0';
	STOPOUT <= '0';	    
    -- activities triggered by rising edge of clock
    elsif CLK'event and CLK = '1' then
	STARTOUT <= NEW_STARTOUT;
	STOPOUT  <= NEW_STOPOUT;
    end if;
    
end process DETECTFF;

START <= NEW_STARTOUT;
STOP <= NEW_STOPOUT;


end SCHEMATIC;

configuration CFG_STARTSTOPDETECT_SCHEMATIC of STARTSTOPDETECT is

   for SCHEMATIC
   end for;

end CFG_STARTSTOPDETECT_SCHEMATIC;
