--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Thomas Schwarz
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : Montgomery Multiplizierer
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  montgomery.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Modulare Multiplikation nach Montgomery und SRT-Division.
--  Das Schaltnetz fuehrt jeweils einen Iterationsschritt der beiden
--  Algorithmen durch, und speichert das Zwischenergenis in einer
--  Carry-Save Darstellung. In JEDEM Takt wird ein Iterationsschritt
--  berechnet.
--
--  
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;


entity MONTGOMERY is
  port( AI     : in std_logic;
        B      : in std_logic_vector (767 downto 0);
        M      : in std_logic_vector (767 downto 0);
        SRTREM : in std_logic;
        CLK    : in std_logic;
        RESET  : in std_logic;
	ENABLE : in std_logic;
	RSAVE  : out std_logic_vector (767 downto 0);
	RCARRY : out std_logic_vector (767 downto 0)
      );
end MONTGOMERY;


architecture BEHAVIOURAL of MONTGOMERY is
constant NumBits : integer := 768;
signal RINTERN : std_logic_vector(NumBits+1 downto 0);
signal QPLUS : std_logic;
signal QMINUS : std_logic;
signal COUNTER : std_logic_vector(15 downto 0);
signal AISAVE : std_logic;
begin

  calcproc : process (CLK)
  variable R: std_logic_vector(NumBits+1 downto 0);
  variable Q: std_logic_vector(3 downto 0);
  begin
      if CLK'event and (CLK = '1') then
	  if (ENABLE='1') then
	      if (SRTREM='0') then
		  -- Montgomery Multiplikation
		  -- NumBits Schleifendurchlaeufe noetig
		  -- B ist mit r=2 skaliert
		  -- M ist maximal 2^NumBits - 1 gross
		  -- M ist mindestens 2^(NumBits-1) gross
		  -- M hat das NumBits-2 -te Bit gesetzt
		  R := '0'&RINTERN(NumBits+1 downto 1);
		  if (R(0) = '1') then
		      R := R + ("00"&M);
		  end if;
		  if (AISAVE = '1') then
		      R := R + ("0"&B&'0');
		  end if;
		  RINTERN <= R(NumBits+1 downto 0); 
		  RSAVE <= R(NumBits downto 1);
		  AISAVE <= AI;
		  COUNTER <= COUNTER+1;
	      else
		  -- SRT Divisionsrest berechnen:
		  -- 2^(2*(NumBits-1)) mod M
		  -- 2 mal (NumBits+1) Schleifendurchlaeufe noetig
		  -- alternierend wird ein Quotientenbit berechnet und
		  -- ein Additions-/Subtraktionszyklus durchgefuehrt.
		  -- M hat die gleichen Einschraenkungen wie oben

		  -- B enthaelt M um ein Bit nach rechts verschoben
		  -- -M(0) entspricht B(-1), und ist immer '1'
		  -- da M ungerade (deshalb -M(0) auch '1')
		  -- B(767) ist immer '1', da negatives Vorzeichen
		  -- RSAVE enthaelt Ergebnis um ein Bit nach rechts
		  -- verschoben, deshalb ein zusaetzlicher Takt mit
		  -- QPLUS='0' und QMINUS='0' noetig, um Ergebnis nach
		  -- links zu schieben. Dies wird erreicht, indem RESET
		  -- einen Takt vorher gesetzt wird

		  -- Divisionsschritt durchfuehren
		  R := '0'&RINTERN(NumBits-1 downto 0)&'0';  -- R schieben
		  if (QPLUS = '1') then
		      R := R + ("00"&M);
		  end if;
		  if (QMINUS = '1') then
		      R := R + ("01"&B(NumBits-2 downto 0)&'1');
		  end if;
		  RINTERN <= R(NumBits+1 downto 0);
		  RSAVE <= R(NumBits downto 1);
		  COUNTER <= COUNTER+1;
	      end if;
	      if (RESET='1') then
		  QPLUS <= '0';
		  QMINUS <= '0';
	      end if;
          elsif (ENABLE='0') then
              if (RESET='1') then
		  RSAVE  <= conv_std_logic_vector(0,NumBits);
		  RCARRY <= conv_std_logic_vector(0,NumBits);
		  COUNTER <= conv_std_logic_vector(0,16);
		  RINTERN <= conv_std_logic_vector(0,NumBits+2);
		  if (SRTREM = '1') then
		      RINTERN(NumBits-3) <= '1';
		      RSAVE(NumBits-2) <= '1';
		  else
		      AISAVE <= AI;
		  end if;
		  QPLUS <= '0';
		  QMINUS <= '0';
	      elsif RESET='0' then
		  -- Quotientenbit QI berechnen
		  if SRTREM='1' then
		      Q := RINTERN(NumBits-1 downto NumBits-4);
		      if (Q = "0000") or (Q = "1111") then
			  QPLUS <= '0';
			  QMINUS <= '0';
		      elsif Q(3) = '0' then
			  QPLUS <= '0';
			  QMINUS <= '1';
		      else
			  QPLUS <= '1';
			  QMINUS <= '0';
		      end if;
		  end if;
	      end if;
	  end if;
      end if;
  end process;

end BEHAVIOURAL;




