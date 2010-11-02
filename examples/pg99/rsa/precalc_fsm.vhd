--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : Precalc_FSM
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : precalc_fsm.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 17.12.98        | 17.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Dieser Automat fuehrt die SRT Division durch
--------------------------------------------------------------------------


library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;
  use IEEE.std_logic_misc.all;


entity Precalc_FSM is
    port (CLK, RESET				: in std_logic;
	  PreCalc_Go, B768RDY			: in std_logic;
	  SRTREM, RESET_MONT, B768RESET,
	  PreCalc_RDY, En_MONT			: out std_logic);
end Precalc_FSM;




-- purpose: Synthetisierbarer Automat fuer das Exponentenbit
architecture RTL of Precalc_FSM is
    
    type TState is (Precalc_Ready, Precalc_Reset1, Precalc_Quot1,
		    Precalc_Add1, Precalc_Reset2, Precalc_Quot2,
		    Precalc_Add2, Precalc_Quot3, Precalc_Add3,
		    Precalc_Quot4, Precalc_Add4, Precalc_Final);
    signal state, nextState : TState;  	-- Zustandsvariable
    attribute state_vector: string;
    attribute state_vector of RTL : architecture is "state";

begin  -- RTL
 
    -- purpose: Zustandsregister
    Zustandsuebergang : process
    begin  -- process Zustandsuebergang
	wait until CLK'event and CLK = '1';
	state <= nextState;
    end process Zustandsuebergang;


    Uebergangsfunktion: process(state, RESET, PreCalc_Go, B768RDY)
    begin  -- process Uebergangsfunktion
	nextState <= state;
	PreCalc_RDY <= '0';
	B768RESET <= '0';
	RESET_MONT <= '0';
	SRTREM <= '0';
	En_MONT <= '0';
	
	if RESET='1' then
	    nextState <= Precalc_Ready;
	    PreCalc_RDY <= '-';
	    B768RESET <= '-';
	    RESET_MONT <= '-';
	    SRTREM <= '-';
	    En_MONT <= '-';
	else
	    case state is
		when Precalc_Ready =>
		    PreCalc_RDY <= '1';
		    if PreCalc_Go = '1' then
			nextState <= Precalc_Reset1;
		    end if;
		when Precalc_Reset1 =>
		    SRTREM <= '1';
		    RESET_MONT <= '1';
		    B768RESET <= '1';
		    nextState <= Precalc_Quot1;
		when Precalc_Quot1 =>
		    SRTREM <= '1';
		    if B768RDY = '0' then
			nextState <= Precalc_Add1;
		    elsif B768RDY = '1' then
			nextState <= Precalc_Reset2;  
		    end if;			    
		when Precalc_Add1 =>
		    SRTREM <= '1';
		    En_MONT <= '1';
		    nextState <= Precalc_Quot1;
		when Precalc_Reset2 =>
		    SRTREM <= '1';
		    B768RESET <= '1';
		    nextState <= Precalc_Add2;
		when Precalc_Quot2 =>
		    SRTREM <= '1';
		    nextState <= Precalc_Add2;
		when Precalc_Add2 =>
		    SRTREM <= '1';
		    En_MONT <= '1';
		    RESET_MONT <= '1';
		    if B768RDY = '0' then
			nextState <= Precalc_Quot2;
		    elsif B768RDY = '1' then
			nextState <= Precalc_Quot3;  
		    end if;
		when Precalc_Quot3 =>
		    SRTREM <= '1';
		    nextState <= Precalc_Add3;
		when Precalc_Add3 =>
		    SRTREM <= '1';
		    En_MONT <= '1';
		    nextState <= Precalc_Quot4;
		when Precalc_Quot4 =>
		    SRTREM <= '1';
		    nextState <= Precalc_Add4;
		when Precalc_Add4 =>
		    SRTREM <= '1';
		    En_MONT <= '1';
		    RESET_MONT <= '1';
		    nextState <= Precalc_Final;		    
		when Precalc_Final =>
		    SRTREM <= '1';
		    En_MONT <= '1';
		    nextState <= Precalc_Ready;
	    end case;
	end if;
    end process Uebergangsfunktion;
    
end RTL;
