--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : GetNextExp_FSM
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : GetNextExp_fsm.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 10.12.98        | 10.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Ein Automat mit der Aufgabe das naechste Exponenten-Bit zur Verfuegung zu stellen
--  und falls notwendig auch die naechsten 32 Bit des Exponenten anzufordern
--------------------------------------------------------------------------


library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;
  use IEEE.std_logic_misc.all;


entity GetNextExp_FSM is
    
    port (CLK, RESET				: in std_logic;
	  GetNextExp_Go, VALACC, Z32RDY, 
	  GetNextExp_Bit, Z24RDY		: in std_logic;
	  FINISHED, Z32RESET, Z48RESET, NextExp,
	  Shift_E, Ena_E, Z32_INC, Z48_INC,
	  GetNextExp_RDY			: out std_logic);

end GetNextExp_FSM;




-- purpose: Synthetisierbarer Automat fuer das Exponentenbit
architecture RTL of GetNextExp_FSM is
    
    type TState is (idle, init, GetNext, NextReady, Waiting, Shift);
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


    Uebergangsfunktion: process(state, RESET,GetNextExp_Go, VALACC, Z32RDY, GetNextExp_Bit, Z24RDY)
	variable CounterRDY : std_logic_vector(1 downto 0);
    begin  -- process Uebergangsfunktion
	nextState <= state;	-- alle Signale auf Null ziehen
	FINISHED <= '0';
	Z32RESET <= '0';
	Z48RESET <= '0';
	Shift_E <= '0';
	Ena_E <= '0';
	Z32_INC <= '0';
	Z48_INC <= '0';
	NextExp <= '0';
	GetNextExp_RDY <= '0';
	CounterRDY := Z32RDY&Z24RDY;
	
	if RESET='1' then
	    nextState<=idle;
	    FINISHED <= '-';
	    Z32RESET <= '-';
	    Z48RESET <= '-';
	    Shift_E <= '-';
	    Ena_E <= '-';
	    Z32_INC <= '-';
	    Z48_INC <= '-';
	    NextExp <= '-';
	    GetNextExp_RDY <= '-';
	else
	    case state is
		when idle =>
		    FINISHED <= '1';
		    if GetNextExp_Go = '1' then
			nextState <= init;
		    end if;
		when init =>
		    Z32RESET <= '1';
		    Z48RESET <= '1';
		    nextState <= GetNext;
		when GetNext =>
		    NextExp <= '1';
		    Z32RESET <= '1';
		    if VALACC = '1' then
			nextState <= NextReady;
		    end if;
		when NextReady =>
		    Ena_E <= '1';
--		    Z32_INC <= '1';
		    Z48_INC <= '1';
		    nextState <= Waiting;
		when Waiting =>
		    GetNextExp_RDY <= '1';
		    if GetNextExp_Bit = '1' then
			case CounterRDY is
			    when "00"  =>
				nextState <= Shift;
			    when "10"  =>
				nextState <= GetNext;
			    when "11"  =>
				nextState <= idle;
			    when "01"  =>
				nextState <= Shift;
			    when others => NULL;
			end case;
		    end if;
		when Shift =>
		    Ena_E <= '1';
		    Shift_E <= '1';
		    Z32_INC <= '1';
		    nextState <= Waiting;
	    end case;
	end if;
    end process Uebergangsfunktion;
    
end RTL;
