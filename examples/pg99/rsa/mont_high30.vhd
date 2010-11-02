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


entity MONT_HIGH30 is port(
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
end MONT_HIGH30;


architecture RTL of MONT_HIGH30 is
    constant NumBits : integer := 768;

    component CALC_Qi port(
        signal RC : in std_logic_vector(766 downto 763);
        signal RS : in std_logic_vector(767 downto 764);
        signal QiMINUS, QiPLUS : out std_logic
        );
    end component;

    component MONT_BITSLICE port(
        CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
        RSi_P1, RSi_M1, RCi_M2: in std_logic;
        Ai, Bi_M1, Qi, Mi     : in std_logic;
        C1i_M1, C2i_M1        : in std_logic;
        C1i, C2i              : out std_logic;
        RSi, RCi              : out std_logic
        );
    end component;

    component MONT_BITSLICE_SETONE port(
        CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
        RSi_P1, RSi_M1, RCi_M2: in std_logic;
        Ai, Bi_M1, Qi, Mi     : in std_logic;
        C1i_M1, C2i_M1        : in std_logic;
        C1i, C2i              : out std_logic;
        RSi, RCi              : out std_logic
        );
    end component;

    component MONT_HIGHEND port(
        CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
        RSMAX_M1, RCMAX_M2 : in std_logic;
        Ai, BMAX_M1        : in std_logic;
        C1MAX_M1, C2MAX_M1 : in std_logic;
        RSMAX              : out std_logic
      );
    end component;

    signal RC : std_logic_vector (NumBits downto NumBits-30);
    signal RS : std_logic_vector(NumBits+1 downto NumBits-29);
    signal C3, C4 : std_logic_vector (NumBits downto NumBits-29);
    signal zero : std_logic;
    signal BMAX_M1 : std_logic;
    
begin
    zero <= '0';
    BMAX_M1 <= B(NumBits-1) or SRTREM;
    RS(NumBits-29) <= RS_739;
    RC(NumBits-29) <= RC_739;
    RC(NumBits-30) <= RC_738;
    C3(NumBits-29) <= C3_739;
    C4(NumBits-29) <= C4_739;

    RSAVE(NumBits downto NumBits-28) <= RS(NumBits downto NumBits-28);
    RCARRY(NumBits-1 downto NumBits-28) <= RC(NumBits-1 downto NumBits-28);

    
    
slices : for i in NumBits-28 to NumBits generate
    usual: if (i /= NumBits-3) and (i < NumBits) generate
        UMBS: MONT_BITSLICE port map (
            CLK => CLK,
            ENA_MONT => ENA_MONT,
            RESET_MONT => RESET_MONT,
            SRTREM => SRTREM,
            RSi_P1 => RS(i+1),
            RSi_M1 => RS(i-1),
            RCi_M2 => RC(i-2),
            Ai => AI,
            Bi_M1 => B(i-1),
            Qi => QI,
            Mi => M(i),
            C1i_M1 => C3(i-1),
            C2i_M1 => C4(i-1),
            C1i => C3(i),
            C2i => C4(i),
            RSi => RS(i),
            RCi => RC(i)
            );
    end generate usual;
    setone: if i = NumBits-3 generate
        UMBSSO: MONT_BITSLICE_SETONE port map (
            CLK => CLK,
            ENA_MONT => ENA_MONT,
            RESET_MONT => RESET_MONT,
            SRTREM => SRTREM,
            RSi_P1 => RS(i+1),
            RSi_M1 => RS(i-1),
            RCi_M2 => RC(i-2),
            Ai => AI,
            Bi_M1 => B(i-1),
            Qi => QI,
            Mi => M(i),
            C1i_M1 => C3(i-1),
            C2i_M1 => C4(i-1),
            C1i => C3(i),
            C2i => C4(i),
            RSi => RS(i),
            RCi => RC(i)
            );
    end generate setone;
    high: if (i = NumBits) generate
        UMBS: MONT_BITSLICE port map (
            CLK => CLK,
            ENA_MONT => ENA_MONT,
            RESET_MONT => RESET_MONT,
            SRTREM => SRTREM,
            RSi_P1 => RS(i+1),
            RSi_M1 => RS(i-1),
            RCi_M2 => RC(i-2),
            Ai => AI,
            Bi_M1 => BMAX_M1,
            Qi => QI,
            Mi => zero,
            C1i_M1 => C3(i-1),
            C2i_M1 => C4(i-1),
            C1i => C3(i),
            C2i => C4(i),
            RSi => RS(i),
            RCi => RC(i)
            );
    end generate high;
end generate slices;

UMHE: MONT_HIGHEND port map (
    CLK => CLK,
    ENA_MONT => ENA_MONT,
    RESET_MONT => RESET_MONT,
    SRTREM => SRTREM,
    RSMAX_M1 => RS(NumBits),
    RCMAX_M2 => RC(NumBits-2),
    Ai => AI,
    BMAX_M1 => zero,
    C1MAX_M1 => C3(NumBits),
    C2MAX_M1 => C4(NumBits),
    RSMAX => RS(NumBits+1)
    );

UCQi: CALC_Qi port map (
    RC(766 downto 763) => RC(NumBits-2 downto NumBits-5),
    RS(767 downto 764) => RS(NumBits-1 downto NumBits-4),
    QiMINUS => QiMINUS,
    QiPLUS => QiPLUS
    );

end RTL;


configuration CFG_MONT_HIGH30 of MONT_HIGH30 is
    for RTL
        for slices
            for usual
                for all : MONT_BITSLICE
                    use entity WORK.MONT_BITSLICE(RTL);
                end for;
            end for;
            for setone
                for all : MONT_BITSLICE_SETONE
                    use entity WORK.MONT_BITSLICE_SETONE(RTL);
                end for;
            end for;
        end for;
        for all : MONT_HIGHEND
            use entity WORK.MONT_HIGHEND(RTL);
        end for;
        for all : CALC_Qi
            use entity WORK.CALC_Qi(RTL);
        end for;
    end for;
end CFG_MONT_HIGH30;



