--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Thomas Schwarz
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : MONT_BITSLICE
-- Purpose :  Bit-Slice of the montgomery multiplier and SRT-division
--            circuit
-- 
-- File Name : mont_bitslice_setone.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 27.12.98        | 27.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Bit-Slice des Montgomery-Multiplizierers
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
--  ACHTUNG: Bei einem RESET wird eine '1' in das Register R geschrieben,
--           falls SRTREM auf '1' ist!
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;



entity MONT_BITSLICE_SETONE is
  port( CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
        RSi_P1, RSi_M1, RCi_M2: in std_logic;
	Ai, Bi_M1, Qi, Mi     : in std_logic;
	C1i_M1, C2i_M1        : in std_logic;
	C1i, C2i              : out std_logic;
	RSi, RCi              : out std_logic
      );
end MONT_BITSLICE_SETONE;



architecture RTL of MONT_BITSLICE_SETONE is

    signal RC1, RC2, RC3, RC4 : std_logic;
    signal RS1, RS2, RS3, RS4 : std_logic;
    signal C1, C2, C3, C4 : std_logic;
    signal A1, A2, A3, A4 : std_logic;
    signal aB, qM : std_logic;
    
  begin
      RS1 <= RSi_M1 when SRTREM='1' else
	     RSi_P1;
      aB  <= Ai and Bi_M1;
      C1i <= RS1 and aB;
      A1  <= RS1 xor aB;
      qM  <= Qi and Mi;
      RC1 <= RCi_M2 when SRTREM='1' else
	     RC3;
      C1  <= A1 and qM;
      A2  <= A1 xor qM;
      C2  <= RC1 or C1i_M1;
      C3  <= A2 and C2;
      A3  <= A2 xor C2;
      C2i <= C1 or C3;
      C4  <= A3 and C2i_M1;
      A4  <= A3 xor C2i_M1;
      RC4 <= RC3 and (not RESET_MONT);  -- MUX zwischen RC3 und 0
      RS4 <= SRTREM when RESET_MONT='1' else
	     RS3;                       -- MUX zwischen RS3 und SRTREM
      RC2 <= C4 when ENA_MONT='1' else
	     RC4;
      RS2 <= A4 when ENA_MONT='1' else
	     RS4;

    operate_REG: process(clk)
    begin
      if CLK='1' and CLK'event then
	  RC3 <= RC2;
	  RS3 <= RS2;
      end if;
    end process operate_REG;

      RCi <= RC3;
      RSi <= RS3;
      
  end RTL;

