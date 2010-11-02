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
--  
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;


--entity MONTGOMERY is
--  port( AI     : in std_logic;
--        B      : in std_logic_vector (767 downto 0);
--        M      : in std_logic_vector (767 downto 0);
--        SRTREM : in std_logic;
--        CLK    : in std_logic;
--        RESET  : in std_logic;
--	ENABLE : in std_logic;
--	RSAVE  : out std_logic_vector (767 downto 0);
--	RCARRY : out std_logic_vector (767 downto 0)
--      );
--end MONTGOMERY;


architecture RTL_1 of MONTGOMERY is
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
    signal Carry1, Carry2 : std_logic_vector(NumBits downto 1);
    signal Ai1, Ai2, AiDist : std_logic;
    signal Qi1, Qi2, QiDist : std_logic;
    signal QiMINUS, QiPLUS, QiMONT : std_logic;
    signal zero : std_logic;
    signal BMAX_M1 : std_logic;
    
begin
    zero <= '0';
    RC(0) <= zero;
    RSAVE(NumBits-1 downto 0) <= RS(NumBits downto 1);
    RCARRY(NumBits-1 downto 0) <= RC(NumBits-1 downto 0);
    BMAX_M1 <= B(NumBits-1) or SRTREM;
    
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
    C11 => Carry1(1),
    C21 => Carry2(1),
    RS0 => RS(0),
    RS1 => RS(1),
    RC1 => RC(1),
    QiMONT => QiMONT
    );
        
slices : for i in 2 to NumBits generate
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
	    C1i_M1 => Carry1(i-1),
	    C2i_M1 => Carry2(i-1),
	    C1i => Carry1(i),
	    C2i => Carry2(i),
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
	    C1i_M1 => Carry1(i-1),
	    C2i_M1 => Carry2(i-1),
	    C1i => Carry1(i),
	    C2i => Carry2(i),
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
	    C1i_M1 => Carry1(i-1),
	    C2i_M1 => Carry2(i-1),
	    C1i => Carry1(i),
	    C2i => Carry2(i),
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
    C1MAX_M1 => Carry1(NumBits),
    C2MAX_M1 => Carry2(NumBits),
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
	 
end RTL_1;


configuration CFG_MONTGOMERY of MONTGOMERY is
    for RTL_1
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
end CFG_MONTGOMERY;



