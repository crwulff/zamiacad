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


entity MONT_128LOWSLICE is port(
    CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
    AI, QI : in std_logic;
    B      : in std_logic_vector (130 downto 0);
    M      : in std_logic_vector (131 downto 0);
    RS_132 : in std_logic;
    C1_132, C2_132 : out std_logic;
    QiMONT         : out std_logic;
    RSAVE          : out std_logic_vector (131 downto 1);
    RCARRY         : out std_logic_vector (131 downto 1)
    );
end MONT_128LOWSLICE;


architecture RTL of MONT_128LOWSLICE is
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

    component MONT_LOW4 port(
        CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
        AI, QI     : in std_logic;
        B          : in std_logic_vector (2 downto 0);
        M          : in std_logic_vector (3 downto 0);
        RS_4       : in std_logic;
        C1_3, C2_3 : out std_logic;
        RSAVE      : out std_logic_vector (3 downto 1);
        RCARRY     : out std_logic_vector (3 downto 1);
        QiMONT     : out std_logic
        );
    end component;

    signal RC : std_logic_vector (SliceBits+3 downto 1);
    signal RS : std_logic_vector (SliceBits+4 downto 1);
    signal C1, C2 : std_logic_vector(NumSlices downto 0);
    
begin

    RS(SliceBits+4) <= RS_132;

    C1_132 <= C1(NumSlices);
    C2_132 <= C2(NumSlices);
    RCARRY(SliceBits+3 downto 1) <= RC(SliceBits+3 downto 1);
    RSAVE(SliceBits+3 downto 1) <= RS(SliceBits+3 downto 1);
    

UML4: MONT_LOW4 port map (
    CLK => CLK,
    ENA_MONT => ENA_MONT,
    RESET_MONT => RESET_MONT,
    SRTREM => SRTREM,
    AI => AI,
    QI => QI,
    B (2 downto 0) => B(2 downto 0),
    M (3 downto 0) => M(3 downto 0),
    RS_4 => RS(4),
    C1_3 => C1(0),
    C2_3 => C2(0),
    RSAVE (3 downto 1) => RS(3 downto 1),
    RCARRY(3 downto 1) => RC(3 downto 1),
    QiMONT => QiMONT
    );


slices : for i in 0 to NumSlices-1 generate
        UM32S: MONT_32SLICE_P port map (
            CLK => CLK,
            ENA_MONT_N => ENA_MONT,
            RESET_MONT_N => RESET_MONT,
            SRTREM_N => SRTREM,
            AI_N => AI,
            QI_N => QI,
            Bi_P(32 downto 1) => B(((i*32)+34) downto ((i*32)+3)),
            Mi_P(33 downto 2) => M(((i*32)+35) downto ((i*32)+4)),
            C1i_P1 => C1(i),
            C2i_P1 => C2(i),
            RSi_P34 => RS((i*32)+36),
            RSi_P1 => RS((i*32)+3),
            RCi_P1 => RC((i*32)+3),
            RCi_P0 => RC((i*32)+2),
            C1i_P33 => C1(i+1),
            C2i_P33 => C2(i+1),
            RSi_P(33 downto 2) => RS(((i*32)+35) downto ((i*32)+4)),
            RCi_P(33 downto 2) => RC(((i*32)+35) downto ((i*32)+4))
            );
end generate slices;

         
end RTL;


configuration CFG_MONT_128LOWSLICE of MONT_128LOWSLICE is
    for RTL
        for slices
            for all : MONT_32SLICE_P
                use configuration WORK.CFG_MONT_32SLICE_P;
            end for;
        end for;
        for all : MONT_LOW4
            use configuration WORK.CFG_MONT_LOW4;
        end for;
    end for;
end CFG_MONT_128LOWSLICE;



