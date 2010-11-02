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
--  Verwendet 32-Bit-Slices, damit die Synthese besser klappt!  
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;


entity MONT_LOW4 is port(
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
end MONT_LOW4;


architecture RTL of MONT_LOW4 is
    constant NumBits : integer := 768;

    component MONT_BITSLICE port(
        CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
        RSi_P1, RSi_M1, RCi_M2: in std_logic;
        Ai, Bi_M1, Qi, Mi     : in std_logic;
        C1i_M1, C2i_M1        : in std_logic;
        C1i, C2i              : out std_logic;
        RSi, RCi              : out std_logic
        );
    end component;


    component MONT_LOWEND port(
        CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
        RS2                : in std_logic;
        Ai, B0, Qi, M0, M1 : in std_logic;
        C11, C21           : out std_logic;
        RS0, RS1, RC1      : out std_logic;
        QiMONT             : out std_logic
      );
    end component;

    signal RC : std_logic_vector(3 downto 0);
    signal RS : std_logic_vector(4 downto 0);
    signal C1, C2 : std_logic_vector(3 downto 1);
    signal zero : std_logic;
    
begin
    zero <= '0';
    RC(0) <= zero;
    RS(4) <= RS_4;
    RSAVE(3 downto 1) <= RS(3 downto 1);
    RCARRY(3 downto 1) <= RC(3 downto 1);
    C1_3 <= C1(3);
    C2_3 <= C2(3);

    
-- Bit-Slices 0 und 1
UMLE: MONT_LOWEND port map (
    CLK => CLK,
    ENA_MONT => ENA_MONT,
    RESET_MONT => RESET_MONT,
    SRTREM => SRTREM,
    RS2 => RS(2),
    Ai => Ai,
    B0 => B(0),
    Qi => Qi,
    M0 => M(0),
    M1 => M(1),
    C11 => C1(1),
    C21 => C2(1),
    RS0 => RS(0),
    RS1 => RS(1),
    RC1 => RC(1),
    QiMONT => QiMONT
    );

-- Bit-Slice 2
UMBS2: MONT_BITSLICE port map (
    CLK => CLK,
    ENA_MONT => ENA_MONT,
    RESET_MONT => RESET_MONT,
    SRTREM => SRTREM,
    RSi_P1 => RS(3),
    RSi_M1 => RS(1),
    RCi_M2 => RC(0),
    Ai => Ai,
    Bi_M1 => B(1),
    Qi => Qi,
    Mi => M(2),
    C1i_M1 => C1(1),
    C2i_M1 => C2(1),
    C1i => C1(2),
    C2i => C2(2),
    RSi => RS(2),
    RCi => RC(2)
    );

-- Bit-Slice 3
UMBS3: MONT_BITSLICE port map (
    CLK => CLK,
    ENA_MONT => ENA_MONT,
    RESET_MONT => RESET_MONT,
    SRTREM => SRTREM,
    RSi_P1 => RS(4),
    RSi_M1 => RS(2),
    RCi_M2 => RC(1),
    Ai => Ai,
    Bi_M1 => B(2),
    Qi => Qi,
    Mi => M(3),
    C1i_M1 => C1(2),
    C2i_M1 => C2(2),
    C1i => C1(3),
    C2i => C2(3),
    RSi => RS(3),
    RCi => RC(3)
    );

         
end RTL;


configuration CFG_MONT_LOW4 of MONT_LOW4 is
    for RTL
        for all : MONT_LOWEND
            use entity WORK.MONT_LOWEND(RTL);
        end for;
        for all : MONT_BITSLICE
            use entity WORK.MONT_BITSLICE(RTL);
        end for;
    end for;
end CFG_MONT_LOW4;



