--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : strg
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : strg.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 01.12.98        | 17.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Diers ist das globale Steuerwerk des RSA-Moduls
--------------------------------------------------------------------------


library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;


entity STRG is
    
    port (CLK,RESET,
	  VALACC, GO, MSB, E0			: in std_logic;
	  SELDATA				: in std_logic_vector(1 downto 0);
	  READY, IN_LOHI, Vz_M, RESET_MONT,
	  En_MONT, SRTREM, ASel, CARRY_IN,
	  C_M_OUT_SEL, NEXTEXP			: out std_logic;
	  EnSh_M, EnSh_A, EnSh_B, Ena_E,
	  Shift_E				: out std_logic;  -- Shift Steuerung
	  C_OUT, C_CLA_M, C_CLA_B, C_CLACARRY,			  
	  C_REG_M, C_REG_A, C_AB_CLA		: out std_logic;  -- Kontrollpunkte
	  C_REG_IN				: out std_logic_vector(1 downto 0);
	  SEL_RI				: out std_logic_vector(5 downto 0);
	  BitSel				: out std_logic_vector(9 downto 0));

end STRG;




-- purpose: Synthetisierbares Steuerwerk
architecture RTL of STRG is

    signal Z48RDY, Z24RDY, B48RDY, B48RDY47, B768RDY	: std_logic;
    signal PreCalc_RDY, GetNextExp_RDY, FINISHED	: std_logic;
    signal Z48RESET, Z48_INC, B48RESET, B768RESET	: std_logic;
    signal GETNEXTEXP_GO, GETNEXTEXP_BIT, PreCalc_Go	: std_logic;
    signal Z32RDY, Z32RESET, Z32_INC			: std_logic;
    signal B768RESET_MAIN, Z48RESET_MAIN, Z48_INC_MAIN,
	En_MONT_MAIN, RESET_MONT_MAIN			: std_logic;
    signal B768RESET_Pre, Z48RESET_GNE, Z48_INC_GNE,
	En_MONT_Pre, RESET_MONT_Pre			: std_logic;
    
component MAIN_FSM
    port (CLK, RESET				: in std_logic;
	  VALACC, GO, Z48RDY, B48RDY, B48RDY47,
	  B768RDY, PrecalcRDY, MSB,
	  GetNextExp_RDY, E0, FINISHED		: in std_logic;
	  SELDATA				: in std_logic_vector(1 downto 0);
	  Z48RESET, Z48_INC, B48RESET,
	  B768RESET				: out std_logic;  -- Zaehlersteuerung
	  IN_LOHI, C_REG_M, C_OUT, C_REG_A ,ASel,
	  C_CLA_M, C_CLA_B, C_CLACARRY, Vz_M,
	  C_M_OUT_SEL, CARRY_IN, C_AB_CLA	: out std_logic;  -- Kontrollpunkte
	  EnSh_A, EnSh_B, EnSh_M		: out std_logic;  -- Register Enable-Ltg.
	  RESET_MONT, En_MONT			: out std_logic;  -- Montgomery Steuerung
	  Precalc_GO				: out std_logic;  -- SRT-Division
	  READY					: out std_logic;  -- Schnittstelle nach aussen
	  GetNextExp_GO, GetNextExp_Bit		: out std_logic;  -- Schnittstelle zu GetNextExp_fsm
	  C_REG_IN				: out std_logic_vector(1 downto 0)
	  );    
end component;

component GetNextExp_FSM
    port (CLK, RESET				: in std_logic;
	  GetNextExp_Go, VALACC, Z32RDY, 
	  GetNextExp_Bit, Z24RDY		: in std_logic;
	  FINISHED, Z32RESET, Z48RESET, NextExp,
	  Shift_E, Ena_E, Z32_INC, Z48_INC,
	  GetNextExp_RDY			: out std_logic);
end component;

component Precalc_FSM
    port (CLK, RESET				: in std_logic;
	  PreCalc_Go, B768RDY			: in std_logic;
	  SRTREM, RESET_MONT, B768RESET,
	  PreCalc_RDY, En_MONT			: out std_logic);
end component;

component B48
    port(CLK,RESET: in  std_logic;
	 READY    : out std_logic;
	 READY47  : out std_logic;
	 D_OUT    : out std_logic_vector(5 downto 0));
end component;

component B768
    port(CLK,RESET: in  std_logic;
	 READY    : out std_logic;
	 D_OUT    : out std_logic_vector(9 downto 0));
end component;

component Z32
    port(CLK,RESET: in  std_logic;
	 INC      : in  std_logic;
	 READY    : out std_logic);
end component;

component Z48
    port(CLK,RESET: in  std_logic;
	 INC      : in  std_logic;
	 READY24  : out std_logic;
	 READY48  : out std_logic);
end component;

  for M		 : MAIN_FSM use entity WORK.main_FSM(RTL);
  for G		 : GetNextExp_FSM use entity WORK.GetNextExp_FSM(RTL);
  for P		 : Precalc_FSM use entity WORK.Precalc_FSM(RTL);
  for BCounter48 : B48 use entity WORK.B48(RTL);
  for BCounter768: B768 use entity WORK.B768(RTL);
  for ZCounter32 : Z32 use entity WORK.Z32(RTL);
  for ZCounter48 : Z48 use entity WORK.Z48(RTL);

begin  -- RTL

    M   : MAIN_FSM port map (CLK, RESET, VALACC, GO, Z48RDY, B48RDY, B48RDY47, B768RDY,
			     PreCalc_RDY, MSB, GetNextExp_RDY, E0, FINISHED,
			     SELDATA, Z48RESET_Main, Z48_INC_Main, B48RESET, B768RESET_Main,
			     IN_LOHI, C_REG_M, C_OUT, C_REG_A, ASel, C_CLA_M,
			     C_CLA_B, C_CLACARRY, Vz_M, C_M_OUT_SEL, CARRY_IN,
			     C_AB_CLA, EnSh_A, EnSh_B, EnSh_M,
			     RESET_MONT_MAIN, En_MONT_MAIN, Precalc_GO, READY,
			     GetNextExp_Go, GetNextExp_Bit, C_REG_IN);
    G   : GetNextExp_FSM port map (CLK, RESET, GetNextExp_Go, VALACC, Z32RDY,
				   GetNextExp_Bit, Z24RDY, FINISHED, Z32RESET,
				   Z48RESET_GNE, NextExp, Shift_E, Ena_E, Z32_INC,
				   Z48_INC_GNE, GetNextExp_RDY);
    P   : Precalc_FSM port map (CLK, RESET, Precalc_Go, B768RDY, SRTREM, RESET_MONT_Pre,
				B768RESET_Pre, PreCalc_RDY, En_MONT_Pre);
    BCounter48 : B48 port map (CLK, B48RESET, B48RDY, B48RDY47, SEL_RI);
    BCounter768: B768 port map (CLK, B768RESET, B768RDY, BitSel);
    ZCounter32 : Z32 port map (CLK, Z32RESET, Z32_INC, Z32RDY);
    ZCounter48 : Z48 port map (CLK, Z48RESET, Z48_INC, Z24RDY, Z48RDY);

    Z48RESET	<= Z48RESET_GNE or Z48RESET_Main;
    Z48_INC	<= Z48_INC_GNE or Z48_INC_Main;
    B768RESET	<= B768RESET_Pre or B768RESET_Main;
    RESET_MONT	<= RESET_MONT_Pre or RESET_MONT_MAIN;
    En_MONT	<= En_MONT_Pre or En_MONT_MAIN;
    
end RTL;
