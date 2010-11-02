-------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Thomas Stanka
-- Group     : DES
--------------------------------------------------------------------
-- Design Unit Name : DES
-- Purpose : The DES-module-core for the cryptochip "pg99"
--          
-- File Name :  des_ENT.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date 09.11.98   | Changes 12.11.98 more necessary ports and coments included 
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  contents :  port-description of the DES-module
--  
--------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;

entity DES is
  port (DATA_IN    : in  STD_LOGIC_VECTOR(63 downto 0);  -- Eingang fuer Daten und Key
	DATA_OUT   : out STD_LOGIC_VECTOR(63 downto 0);  -- Ausgang fuer Daten
	MODUS	   : in  STD_LOGIC_VECTOR(4 downto 0);   -- Steuerleitung zur Auswahl der 
							 -- versch. DES-Modi
	DATA_IS_KEY: in  STD_LOGIC;	 -- data_is_key=1 <=> es kommt ein Schluessel
	DATA_READY : in  STD_LOGIC;	 -- Eingabepuffer voll
	BUFFER_FREE: in  STD_LOGIC;      -- Ausgabebuffer frei, wir duerfen daten senden 
	DATA_ACK   : out STD_LOGIC;	 -- Wir haben die Daten von data_in uebernommen
	DES_READY  : out STD_LOGIC;      -- des_ready=1  <=> gueltiges datum liegt am 
					 -- data_out an
	KEY_PARITY : out STD_LOGIC;      -- key_parity=1 <=> key_parity falsch
	ERROR      : out STD_LOGIC;	 -- error=1 <=> BIST bringt Fehler
	TEST	   : in  STD_LOGIC;	 -- testmode aktive
	CLK	   : in  STD_LOGIC;      -- der Takt
	RESET	   : in  STD_LOGIC      -- DES-Modul zuruecksetzen

	-- TEST_SE	   : in  STD_LOGIC;
	-- TEST_SI    : in  STD_LOGIC;
	-- TEST_SO    : out STD_LOGIC
       );
end DES;



