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


entity MONT_128HIGHSLICE is port(
    CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
    AI, QI                 : in std_logic;
    B                      : in std_logic_vector (767 downto 643);
    M                      : in std_logic_vector (767 downto 644);
    C1_643, C2_643         : in std_logic;
    RS_643, RC_643, RC_642 : in std_logic;
    QiMINUS, QiPLUS : out std_logic;
    RSAVE           : out std_logic_vector (768 downto 644);
    RCARRY          : out std_logic_vector (767 downto 644)
    );
end MONT_128HIGHSLICE;


architecture RTL of MONT_128HIGHSLICE is
    constant NumBits : integer := 768;
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

    component MONT_HIGH30 port(
        CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
        AI, QI                 : in std_logic;
        B                      : in std_logic_vector (767 downto 739);
        M                      : in std_logic_vector (767 downto 740);
        C3_739, C4_739         : in std_logic;
        RS_739, RC_739, RC_738 : in std_logic;
        QiMINUS, QiPLUS : out std_logic;
        RSAVE           : out std_logic_vector (768 downto 740);
        RCARRY          : out std_logic_vector (767 downto 740)
        );
    end component;

    signal RC : std_logic_vector (NumBits-1 downto NumBits-SliceBits+2);
    signal RS : std_logic_vector (NumBits downto NumBits-SliceBits+3);
    signal C1, C2 : std_logic_vector(NumSlices-1 downto 0);
    
begin

    C1(0) <= C1_643;
    C2(0) <= C2_643;
    RS(NumBits-SliceBits+3) <= RS_643;
    RC(NumBits-SliceBits+3) <= RC_643;
    RC(NumBits-SliceBits+2) <= RC_642;

    RCARRY(NumBits-1 downto NumBits-SliceBits+4) <= RC(NumBits-1 downto NumBits-SliceBits+4);
    RSAVE(NumBits downto NumBits-SliceBits+4) <= RS(NumBits downto NumBits-SliceBits+4);
    
slices : for i in 0 to NumSlices-2 generate
        UM32S: MONT_32SLICE_P port map (
            CLK => CLK,
            ENA_MONT_N => ENA_MONT,
            RESET_MONT_N => RESET_MONT,
            SRTREM_N => SRTREM,
            AI_N => AI,
            QI_N => QI,
            Bi_P(32 downto 1) => B(((i*32)+674) downto ((i*32)+643)),
            Mi_P(33 downto 2) => M(((i*32)+675) downto ((i*32)+644)),
            C1i_P1 => C1(i),
            C2i_P1 => C2(i),
            RSi_P34 => RS((i*32)+676),
            RSi_P1 => RS((i*32)+643),
            RCi_P1 => RC((i*32)+643),
            RCi_P0 => RC((i*32)+642),
            C1i_P33 => C1(i+1),
            C2i_P33 => C2(i+1),
            RSi_P(33 downto 2) => RS(((i*32)+675) downto ((i*32)+644)),
            RCi_P(33 downto 2) => RC(((i*32)+675) downto ((i*32)+644))
            );
end generate slices;


UMH30: MONT_HIGH30 port map(
    CLK => CLK,
    ENA_MONT => ENA_MONT,
    RESET_MONT => RESET_MONT,
    SRTREM => SRTREM,
    AI => AI,
    QI => QI,
    B(767 downto 739) => B(767 downto 739),
    M(767 downto 740) => M(767 downto 740),
    C3_739 => C1(NumSlices-1),
    C4_739 => C2(NumSlices-1),
    RS_739 => RS(739),
    RC_739 => RC(739),
    RC_738 => RC(738),
    QiMINUS => QiMINUS,
    QiPLUS => QiPLUS,
    RSAVE(768 downto 740) => RS(768 downto 740),
    RCARRY(767 downto 740) => RC(767 downto 740)
    );

         
end RTL;


configuration CFG_MONT_128HIGHSLICE of MONT_128HIGHSLICE is
    for RTL
        for slices
            for all : MONT_32SLICE_P
                use configuration WORK.CFG_MONT_32SLICE_P;
            end for;
        end for;
        for all: MONT_HIGH30
            use configuration WORK.CFG_MONT_HIGH30;
        end for;
    end for;
end CFG_MONT_128HIGHSLICE;



