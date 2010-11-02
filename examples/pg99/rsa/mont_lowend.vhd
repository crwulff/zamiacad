--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Thomas Schwarz
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : MONT_LOWEND
-- Purpose :  Lower end of the montgomery multiplier and SRT-division
--            circuit.
--            Optimized considering lack of carries at lower end.
--            Provides next quotient digit for montgomery mult.
-- 
-- File Name : mont_lowend.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 23.12.98        | 23.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Unteres Ende des Montgomery-Multiplizierers,
--  die unteren beiden Bit-Slices 0 und 1 sind zusammengefasst, nicht-
--  existierende Carrys wurden zum Optimieren benutzt.
--  Fuer die Division wird das unterste Bit von -M (welches nicht mehr
--  in B ist) konstant auf 1 gesetzt (da M ungerade ist).
--  QiMONT enthaelt das Quotienten-Bit fuer die naechste
--  Montgomery-Iteration.
--
--  addiert in einem Takt:  alter Wert des Registers R
--                        + alte Uebertraege von vorheriger Addition
--                        + 1-Bit-Produkt von ai * Bi-1
--                        + 1-Bit-Produkt von qi * Mi
--  speichert das Ergebnis in Redundanter Darstellung, jede Stelle kann
--  die Werte 0, 1 oder 2 annehmen.
--  Bei der Montgomery-Multiplikation wird in jedem Takt der alte Wert
--  des Registers R um ein Bit nach RECHTS geschoben.
--  Bei der SRT-Division wird in jedem Takt der alte Wert des Registers
--  R um ein Bit nach LINKS geschoben.
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;



entity MONT_LOWEND is
  port( CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
	RS2                : in std_logic;
	Ai, B0, Qi, M0, M1 : in std_logic;
	C11, C21           : out std_logic;
	RS0, RS1, RC1      : out std_logic;
	QiMONT             : out std_logic
      );
end MONT_LOWEND;



architecture RTL of MONT_LOWEND is

    signal RS10, RS20, RS30, RS40 : std_logic;
    signal aB0, qM0, A10, A20 : std_logic;
    signal C10, C20 : std_logic;

    signal RC11, RC21, RC31, RC41 : std_logic;
    signal RS11, RS21, RS31, RS41 : std_logic;
    signal C1, C2, C3, C4 : std_logic;
    signal A11, A21, A31, A41 : std_logic;
    signal aB1, qM1 : std_logic;

begin
    RS10 <= RS31 and (not SRTREM);  	-- MUX zwischen RS31 und 0
    aB0  <= Ai and SRTREM;
    C10  <= RS10 and aB0;
    A10  <= RS10 xor aB0;
    qM0  <= Qi and M0;
    C20  <= A10 and qM0;
    A20  <= A10 xor qM0;
    RS40 <= RS30 and (not RESET_MONT);  -- MUX zwischen RS30 und 0
    RS20 <= A20 when ENA_MONT='1' else
	    RS40;

    RS11 <= RS30 when SRTREM='1' else
	    RS2;
    aB1  <= Ai and B0;
    C11  <= RS11 and aB1;
    A11  <= RS11 xor aB1;
    qM1  <= Qi and M1;
    RC11 <= RC31 and (not SRTREM);  	-- MUX zwischen RC31 und 0
    C1   <= A11 and qM1;
    A21  <= A11 xor qM1;
    C2   <= RC11 or C10;
    C3   <= A21 and C2;
    A31  <= A21 xor C2;
    C21  <= C1 or C3;
    C4   <= A31 and C20;
    A41  <= A31 xor C20;
    RC41 <= RC31 and (not RESET_MONT);  -- MUX zwischen RC31 und 0
    RS41 <= RS31 and (not RESET_MONT);  -- MUX zwischen RS31 und 0
    RC21 <= C4 when ENA_MONT='1' else
	    RC41;
    RS21 <= A41 when ENA_MONT='1' else
	    RS41;

    operate_REG: process(clk)
    begin
      if CLK='1' and CLK'event then
	  RS30 <= RS20;
	  RC31 <= RC21;
	  RS31 <= RS21;
      end if;
    end process operate_REG;

    RS0    <= RS30;
    RC1    <= RC31;
    RS1    <= RS31;
    QiMONT <= A41;
    
end RTL;

