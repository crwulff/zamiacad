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


entity MONTGOMERY_P is
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
end MONTGOMERY_P;


architecture RTL of MONTGOMERY_P is
    constant NumBits : integer := 768;
    constant Num32Slices : integer := (NumBits-32)/32;

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

    component MONT_32SLICE_P port(
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

    component MONT_LOWEND port(
	CLK, ENA_MONT, RESET_MONT, SRTREM : in std_logic;
	RS2                : in std_logic;
	Ai, B0, Qi, M0, M1 : in std_logic;
	C11, C21           : out std_logic;
	RS0, RS1, RC1      : out std_logic;
	QiMONT             : out std_logic
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

    signal RC : std_logic_vector (NumBits downto 0);
    signal RS : std_logic_vector(NumBits+1 downto 0);
    signal C1, C2 : std_logic_vector(Num32Slices+3 downto 1);
    signal C3, C4 : std_logic_vector (NumBits downto NumBits-29);
    signal Ai1, Ai2, AiDist, AiDist_N : std_logic;
    signal Qi1, Qi2, QiDist, QiDist_N : std_logic;
    signal QiMINUS, QiPLUS, QiMONT : std_logic;
    signal zero : std_logic;
    signal BMAX_M1 : std_logic;
    signal ENA_MONT_N, RESET_MONT_N, SRTREM_N : std_logic;
    
begin
    zero <= '0';
    RC(0) <= zero;
    RSAVE(NumBits-1 downto 0) <= RS(NumBits downto 1);
    RCARRY(NumBits-1 downto 0) <= RC(NumBits-1 downto 0);
    BMAX_M1 <= B(NumBits-1) or SRTREM;
    AiDist_N <= AiDist;
    QiDist_N <= QiDist;
    ENA_MONT_N <= ENABLE;
    RESET_MONT_N <= RESET;
    SRTREM_N <= SRTREM;
    C3(NumBits-29) <= C1(Num32Slices+3);
    C4(NumBits-29) <= C2(Num32Slices+3);
    
-- Bit-Slices 0 und 1
UMLE: MONT_LOWEND port map (
    CLK => CLK,
    ENA_MONT => ENABLE,
    RESET_MONT => RESET,
    SRTREM => SRTREM,
    RS2 => RS(2),
    Ai => AiDist,
    B0 => B(0),
    Qi => QiDist,
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
    ENA_MONT => ENABLE,
    RESET_MONT => RESET,
    SRTREM => SRTREM,
    RSi_P1 => RS(3),
    RSi_M1 => RS(1),
    RCi_M2 => zero,
    Ai => AiDist,
    Bi_M1 => B(1),
    Qi => QiDist,
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
    ENA_MONT => ENABLE,
    RESET_MONT => RESET,
    SRTREM => SRTREM,
    RSi_P1 => RS(4),
    RSi_M1 => RS(2),
    RCi_M2 => RC(1),
    Ai => AiDist,
    Bi_M1 => B(2),
    Qi => QiDist,
    Mi => M(3),
    C1i_M1 => C1(2),
    C2i_M1 => C2(2),
    C1i => C1(3),
    C2i => C2(3),
    RSi => RS(3),
    RCi => RC(3)
    );

bigslices : for i in 0 to Num32Slices-1 generate
    UM32S: MONT_32SLICE_P port map (
	CLK                 => CLK,
	ENA_MONT_N          => ENA_MONT_N,
	RESET_MONT_N        => RESET_MONT_N,
        SRTREM_N            => SRTREM_N,
	AI_N                => AiDist_N,
	QI_N                => QiDist_N,
	Bi_P (32 downto 1)  => B((i*32)+34 downto (i*32)+3),
	Mi_P (33 downto 2)  => M((i*32)+35 downto (i*32)+4),
	C1i_P1              => C1(i+3),
	C2i_P1              => C2(i+3),
	RSi_P34             => RS((i*32)+36),
	RSi_P1              => RS((i*32)+3),
	RCi_P1              => RC((i*32)+3),
	RCi_P0              => RC((i*32)+2),
	C1i_P33             => C1(i+4),
	C2i_P33             => C2(i+4),
	RSi_P (33 downto 2) => RS((i*32)+35 downto (i*32)+4),
	RCi_P (33 downto 2) => RC((i*32)+35 downto (i*32)+4)
	);
end generate bigslices;

    
slices : for i in NumBits-28 to NumBits generate
    usual: if (i /= NumBits-3) and (i < NumBits) generate
	UMBS: MONT_BITSLICE port map (
	    CLK => CLK,
	    ENA_MONT => ENABLE,
	    RESET_MONT => RESET,
	    SRTREM => SRTREM,
	    RSi_P1 => RS(i+1),
	    RSi_M1 => RS(i-1),
	    RCi_M2 => RC(i-2),
	    Ai => AiDist,
	    Bi_M1 => B(i-1),
	    Qi => QiDist,
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
	    ENA_MONT => ENABLE,
	    RESET_MONT => RESET,
	    SRTREM => SRTREM,
	    RSi_P1 => RS(i+1),
	    RSi_M1 => RS(i-1),
	    RCi_M2 => RC(i-2),
	    Ai => AiDist,
	    Bi_M1 => B(i-1),
	    Qi => QiDist,
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
	    ENA_MONT => ENABLE,
	    RESET_MONT => RESET,
	    SRTREM => SRTREM,
	    RSi_P1 => RS(i+1),
	    RSi_M1 => RS(i-1),
	    RCi_M2 => RC(i-2),
	    Ai => AiDist,
	    Bi_M1 => BMAX_M1,
	    Qi => QiDist,
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

--UMHE: MONT_HIGHEND port map (
--    CLK => CLK,
--    ENA_MONT => ENABLE,
--    RESET_MONT => RESET,
--    SRTREM => SRTREM,
--    RSMAX_M1 => RS(NumBits-1),
--    RCMAX_M2 => RC(NumBits-2),
--    Ai => AiDist,
--    BMAX_M1 => B(NumBits),
--    C1MAX_M1 => Carry1(NumBits-1),
--    C2MAX_M1 => Carry2(NumBits-1),
--    RSMAX => RS(NumBits)
--    );
UMHE: MONT_HIGHEND port map (
    CLK => CLK,
    ENA_MONT => ENABLE,
    RESET_MONT => RESET,
    SRTREM => SRTREM,
    RSMAX_M1 => RS(NumBits),
    RCMAX_M2 => RC(NumBits-2),
    Ai => AiDist,
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

    Ai1 <= '0' when ENABLE = '1' else
	   QiMINUS;
    Ai2 <= Ai1 when SRTREM = '1' else
	   AI;
    Qi1 <= QiPLUS when SRTREM = '1' else
	   QiMONT;
    Qi2 <= '0' when RESET='1' else
	   Qi1;

-- purpose: erzeugt Pipeline-Register fuer Qi und Ai
-- type:    memorizing
-- inputs:  CLK, Ai2, Qi2
-- outputs: AiDist, QiDist
operatereg : process (CLK )
    
begin  -- process operatereg
    -- activities triggered by rising edge of clock
    if CLK'event and CLK = '1' then
	AiDist <= Ai2;
	QiDist <= Qi2;
    end if;
end process operatereg;
	 
end RTL;


configuration CFG_MONTGOMERY_P of MONTGOMERY_P is
    for RTL
	for bigslices
	    for all : MONT_32SLICE_P
		use entity WORK.MONT_32SLICE_P(RTL);
	    end for;
	end for;
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
	for all : MONT_LOWEND
	    use entity WORK.MONT_LOWEND(RTL);
	end for;
	for all : MONT_HIGHEND
	    use entity WORK.MONT_HIGHEND(RTL);
	end for;
	for all : CALC_Qi
	    use entity WORK.CALC_Qi(RTL);
	end for;
    end for;
end CFG_MONTGOMERY_P;



