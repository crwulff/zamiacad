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


entity MONT_128SLICE is port(
    CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
    AI, QI    : in std_logic;
    Bi_P      : in std_logic_vector (128 downto 1);
    Mi_P      : in std_logic_vector (129 downto 2);
    C1i_P1, C2i_P1 : in std_logic;
    RSi_P130, RSi_P1, RCi_P1, RCi_P0 : in std_logic;
    C1i_P129, C2i_P129 : out std_logic;
    RSi_P : out std_logic_vector (129 downto 2);
    RCi_P : out std_logic_vector (129 downto 2)
    );
end MONT_128SLICE;


architecture RTL of MONT_128SLICE is
    constant SliceBits : integer := 128;
    constant NumSlices : integer := 4;

    component MONT_32SLICE_P port (
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
    end component;

    signal RC : std_logic_vector (SliceBits+1 downto 0);
    signal RS : std_logic_vector (SliceBits+2 downto 1);
    signal C1, C2 : std_logic_vector(NumSlices downto 0);
    
begin

    C1(0) <= C1i_P1;
    C2(0) <= C2i_P1;
    RS(SliceBits+2) <= RSi_P130;
    RS(1) <= RSi_P1;
    RC(1) <= RCi_P1;
    RC(0) <= RCi_P0;

    C1i_P129 <= C1(NumSlices);
    C2i_P129 <= C2(NumSlices);
    RCi_P(SliceBits+1 downto 2) <= RC(SliceBits+1 downto 2);
    RSi_P(SliceBits+1 downto 2) <= RS(SliceBits+1 downto 2);
    
slices : for i in 0 to NumSlices-1 generate
        UM32S: MONT_32SLICE_P port map (
            CLK => CLK,
            ENA_MONT_N => ENA_MONT,
            RESET_MONT_N => RESET_MONT,
            SRTREM_N => SRTREM,
            AI_N => AI,
            QI_N => QI,
            Bi_P(32 downto 1) => Bi_P(((i*32)+32) downto ((i*32)+1)),
            Mi_P(33 downto 2) => Mi_P(((i*32)+33) downto ((i*32)+2)),
            C1i_P1 => C1(i),
            C2i_P1 => C2(i),
            RSi_P34 => RS((i*32)+34),
            RSi_P1 => RS((i*32)+1),
            RCi_P1 => RC((i*32)+1),
            RCi_P0 => RC((i*32)+0),
            C1i_P33 => C1(i+1),
            C2i_P33 => C2(i+1),
            RSi_P(33 downto 2) => RS(((i*32)+33) downto ((i*32)+2)),
            RCi_P(33 downto 2) => RC(((i*32)+33) downto ((i*32)+2))
            );
end generate slices;

         
end RTL;


configuration CFG_MONT_128SLICE of MONT_128SLICE is
    for RTL
        for slices
            for all : MONT_32SLICE_P
                use configuration WORK.CFG_MONT_32SLICE_P;
            end for;
        end for;
    end for;
end CFG_MONT_128SLICE;



