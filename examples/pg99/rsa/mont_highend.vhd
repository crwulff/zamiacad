--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Thomas Schwarz
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : MONT_HIGHEND
-- Purpose :  Upper end of the montgomery multiplier and SRT-division
--            circuit
--            Optimized considering not needed Carry and upper end
-- 
-- File Name : mont_highend.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 23.12.98        | 23.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Oberes Ende des Montgomery-Multiplizierers
--  Carry wird nicht generiert, bei der Division wird BMAX (bei
--  768 Bit Wortbreite also B(767)) auf '1' gesetzt (Vorzeichen
--  negativ).
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



entity MONT_HIGHEND is
  port( CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
        RSMAX_M1, RCMAX_M2 : in std_logic;
	Ai, BMAX_M1        : in std_logic;
	C1MAX_M1, C2MAX_M1 : in std_logic;
	RSMAX              : out std_logic
      );
end MONT_HIGHEND;



architecture RTL of MONT_HIGHEND is

    signal RC1 : std_logic;
    signal RS1, RS2, RS3, RS4 : std_logic;
    signal C1 : std_logic;
    signal A1, A2, A3 : std_logic;
    signal aB, BX : std_logic;
    
begin
    BX  <= BMAX_M1 or SRTREM;
    RS1 <= SRTREM and RSMAX_M1;  	-- MUX zwischen 0 und RSMAX_M1
    aB  <= Ai and BX;
    RC1 <= SRTREM and RCMAX_M2;  	-- MUX zwischen 0 und RCMAX_M2
    A1  <= RS1 xor aB;
    C1  <= RC1 or C1MAX_M1;
    A2  <= A1 xor C1;
    A3  <= A2 xor C2MAX_M1;
    RS4 <= RS3 and (not RESET_MONT);  -- MUX zwischen RS3 und 0
    RS2 <= A3 when ENA_MONT='1' else
	   RS4;

    operate_REG: process(clk)
    begin
      if CLK='1' and CLK'event then
	  RS3 <= RS2;
      end if;
    end process operate_REG;

    RSMAX <= RS3;

end RTL;

