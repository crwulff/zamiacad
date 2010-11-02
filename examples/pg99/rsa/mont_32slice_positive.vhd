--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Thomas Schwarz
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : MONT_32SLICE
-- Purpose : 32 bits wide slice of the montgomery multiplier and
--           SRT-division circuit
-- 
-- File Name :  mont_32slice.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 04.01.99        | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  32 1-Bit-Slices zusammengeschaltet, fuer eine genaue Beschreibung
--  der Funktionsweise siehe mont_bitslice.vhd.
--  Zweck: 32 Bitslices sind einfacher zu synthetisieren (und schneller)
--         als alle 770 Stueck auf einmal.
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;


entity MONT_32SLICE_P is port(
    CLK, ENA_MONT_N, RESET_MONT_N, SRTREM_N : in std_logic;
    AI_N, QI_N     : in std_logic;
    Bi_P      : in std_logic_vector (32 downto 1);
    Mi_P      : in std_logic_vector (33 downto 2);
    C1i_P1, C2i_P1 : in std_logic;
    RSi_P34, RSi_P1, RCi_P1, RCi_P0 : in std_logic;
    C1i_P33, C2i_P33 : out std_logic;
    RSi_P : out std_logic_vector (33 downto 2);
    RCi_P : out std_logic_vector (33 downto 2)
    );
end MONT_32SLICE_P;


architecture RTL of MONT_32SLICE_P is
    constant SliceBits : integer := 32;
    
    component MONT_BITSLICE port(
	CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
	RSi_P1, RSi_M1, RCi_M2: in std_logic;
	Ai, Bi_M1, Qi, Mi     : in std_logic;
	C1i_M1, C2i_M1        : in std_logic;
	C1i, C2i              : out std_logic;
	RSi, RCi              : out std_logic
	);
    end component;

    signal RC : std_logic_vector (SliceBits+1 downto 0);
    signal RS : std_logic_vector (SliceBits+2 downto 1);
    signal C1, C2 : std_logic_vector(SliceBits+1 downto 1);
    signal ENA_MONT, RESET_MONT, SRTREM : std_logic;
    signal AI, QI : std_logic;
    
begin
    ENA_MONT <= ENA_MONT_N;
    RESET_MONT <= RESET_MONT_N;
    SRTREM <= SRTREM_N;
    AI <= AI_N;
    QI <= QI_N;

    C1(1) <= C1i_P1;
    C2(1) <= C2i_P1;
    RS(SliceBits+2) <= RSi_P34;
    RS(1) <= RSi_P1;
    RC(1) <= RCi_P1;
    RC(0) <= RCi_P0;

    C1i_P33 <= C1(SliceBits+1);
    C2i_P33 <= C2(SliceBits+1);
    RCi_P(SliceBits+1 downto 2) <= RC(SliceBits+1 downto 2);
    RSi_P(SliceBits+1 downto 2) <= RS(SliceBits+1 downto 2);
    
slices : for i in 2 to SliceBits+1 generate
	UMBS: MONT_BITSLICE port map (
	    CLK => CLK,
	    ENA_MONT => ENA_MONT,
	    RESET_MONT => RESET_MONT,
	    SRTREM => SRTREM,
	    RSi_P1 => RS(i+1),
	    RSi_M1 => RS(i-1),
	    RCi_M2 => RC(i-2),
	    Ai => AI,
	    Bi_M1 => Bi_P(i-1),
	    Qi => QI,
	    Mi => Mi_P(i),
	    C1i_M1 => C1(i-1),
	    C2i_M1 => C2(i-1),
	    C1i => C1(i),
	    C2i => C2(i),
	    RSi => RS(i),
	    RCi => RC(i)
	    );
end generate slices;

	 
end RTL;


configuration CFG_MONT_32SLICE_P of MONT_32SLICE_P is
    for RTL
	for slices
	    for all : MONT_BITSLICE
		use entity WORK.MONT_BITSLICE(RTL);
	    end for;
	end for;
    end for;
end CFG_MONT_32SLICE_P;



