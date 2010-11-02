-- File>>> rsa_RTL.vhd
--
-- Date:   Tue Nov 10 19:11:33 MET 1998
-- Author: wackerao
--
-- Revision history:
--
-- $Source$
-- $Revision$
-- $Log$
--
--
--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : RSA
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : rsa_RTL.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 18.12.98        | 18.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Die globale Strukturbeschreibung des RSA-Moduls. Die Configuration dazu
--  befindet sich in CFG_rsa.vhd
--------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;

library LIB_rsa;
-- use LIB_rsa.PCK_rsa.all;

architecture RTL of rsa is

    -- Signale von der Steuereinheit
    signal IN_LOHI, C_REG_M, C_REG_A, C_CLA_M, C_CLA_B, C_CLACARRY,
	EnSh_M, EnSh_A, EnSh_B, Ena_E, Shift_E,
	ASel, SRTREM, RESET_MONT, EnMONT, VZ_M, E0,
	C_M_OUT_SEL, CARRY_IN, C_OUT, C_AB_CLA	 : std_logic;
    signal C_REG_IN				 : std_logic_vector(1 downto 0);
    signal SEL_RI				 : std_logic_vector(5 downto 0);
    signal SEL					 : std_logic_vector(9 downto 0);

    -- Datenpfad
    signal ABi, ABi_MONT, Carry_OUT_CLA, Carry_REG, Carry_IN_CLA    : std_logic;
    signal DIN16, REG_OUT, M_IN, A_IN, B_IN, MONT_OUT1_16, MONT_OUT2_16,
	SM_DATA, MONT_REG1, MONT_REG2, MONT_CLA1, X_M_DATA,
	MONT_CLA2, CLA_OUT, AB_DATA		: std_logic_vector(15 downto 0);
           -- Dummy Signal Fuer die Null
    signal Null16	     : std_logic_vector(15 downto 0);
           -- Dummy Signal fuer die Eins
    signal Eins16  : std_logic_vector(15 downto 0);
    signal A_DATA, B_DATA, M_DATA, MONT_OUT1,
	MONT_OUT2			       : std_logic_vector(767 downto 0);

    -- Komponenten
    component MUX16_2to1
	port(	A	: in  	std_logic_vector (15 downto 0); 
		B	: in 	std_logic_vector (15 downto 0);
		Sel	: in 	STD_LOGIC;
		Q	: out	std_logic_vector (15 downto 0)
	    );
    end component;

    component MUX16_4to1
	port(	A	: in  	std_logic_vector (15 downto 0); 
		B	: in 	std_logic_vector (15 downto 0);
		C	: in 	std_logic_vector (15 downto 0);
		D	: in 	std_logic_vector (15 downto 0);
		Sel	: in 	std_logic_vector (1 downto 0);
		Q	: out	std_logic_vector (15 downto 0)
	    );
    end component;

    component MUX32_2to1
	port(	A     : In 	std_logic_vector (31 downto 0); 
		B     : In	std_logic_vector (31 downto 0);
		Sel   : In	STD_LOGIC;
		Q     : Out     std_logic_vector (31 downto 0)
	    );
    end component;

    component REG_768
	port(	DIn	: in  	std_logic_vector (15 downto 0); 
		ENABLE	: in	STD_LOGIC;
		CLK	: in 	STD_LOGIC;
		DOut	: out 	std_logic_vector (767 downto 0)
	    );
    end component;

    component REG_32
	port(	DIn	: in  	std_logic_vector (31 downto 0); 
		Shift	: in 	STD_LOGIC;
		enable	: in    STD_LOGIC;
		clk	: in 	STD_LOGIC;
		DOut	: out 	std_logic 
	    );
    end component;

    component REG_16
	port(	DIn	: in  	std_logic_vector (15 downto 0); 
		CLK	: in 	STD_LOGIC;
		DOut	: out 	std_logic_vector (15 downto 0) 
	    );
    end component;
    
    component REG_1
	port(	DIn   : In 	std_logic; 
		CLK   : In	STD_LOGIC;
		DOut  : Out	std_logic 
	    );
    end component;
    
    component MUX_1536to1
	port(	A	: in  	std_logic_vector (767 downto 0); 
		B	: in 	std_logic_vector (767 downto 0);
		Sel     : in 	std_logic_vector (9 downto 0);
		ASel	: in 	STD_LOGIC;
		Q	: out	STD_LOGIC
	    );
    end component;

    component MONTGOMERY
	port(	AI	: in	std_logic;
		B	: in	std_logic_vector (767 downto 0);
		M	: in	std_logic_vector (767 downto 0);
		SRTREM	: in	std_logic;
		CLK	: in	std_logic;
		RESET	: in	std_logic;
		ENABLE	: in	std_logic;
		RSAVE	: out	std_logic_vector (767 downto 0);
		RCARRY	: out	std_logic_vector (767 downto 0)
	      );
    end component;

    component XOR16_1
	port(	A	: in  	std_logic_vector (15 downto 0); 
		B	: in 	std_logic;
		Q	: out   std_logic_vector (15 downto 0)
	    );
    end component;

    component MUX768to16
	port(	A	: in  	std_logic_vector (767 downto 0); 
		Sel	: in 	std_logic_vector (5 downto 0);
		Q	: out   std_logic_vector (15 downto 0)
	    );
    end component;

    component CLA_16
	port(	A	: in  	std_logic_vector (15 downto 0); 
		B	: in 	std_logic_vector (15 downto 0);
		Q	: out 	std_logic_vector (15 downto 0);
		C_OUT	: out	STD_LOGIC;
		C_IN	: in	STD_LOGIC);
    end component;

    component MUX1_2to1
	port(	A	: in  	std_logic; 
		B	: in 	std_logic;
		Sel	: in 	std_logic;
		Q	: out   std_logic
	    );
    end component;

    component STRG
	port (	CLK,RESET,
		VALACC, GO, MSB, E0		: in std_logic;
		SELDATA				: in std_logic_vector(1 downto 0);
		READY, IN_LOHI, Vz_M, RESET_MONT,
		En_MONT, SRTREM, ASel, CARRY_IN,
		C_M_OUT_SEL, NEXTEXP		: out std_logic;
		EnSh_M, EnSh_A, EnSh_B, Ena_E,
		Shift_E				: out std_logic;  -- Shift Steuerung
		C_OUT, C_CLA_M, C_CLA_B, C_CLACARRY,			  
		C_REG_M, C_REG_A, C_AB_CLA	: out std_logic;  -- Kontrollpunkte
		C_REG_IN			: out std_logic_vector(1 downto 0);
		SEL_RI				: out std_logic_vector(5 downto 0);
		BitSel				: out std_logic_vector(9 downto 0)
	    );	
    end component;
   
    for all : REG_768 use entity WORK.REG_768(BEHAV);
    for all : MONTGOMERY use entity WORK.MONTGOMERY(RTL);
    for all : CLA_16 use entity WORK.CLA_16(RTL);
    
 
begin

    Null16 <= (others => '0');
    Eins16 <= (0 => '1', others => '0');
    
    DATAIN_MUX	 : MUX16_2to1 port map	(A	=> DATAIN(15 downto 0),
					 B	=> DATAIN(31 downto 16),
					 Sel	=> IN_LOHI,
					 Q	=> DIN16);

    MUX_C_REG_IN : MUX16_4to1 port map	(A	=> DIN16,
					 B	=> REG_OUT,
					 C	=> Null16,
					 D	=> Eins16,
					 Sel	=> C_REG_IN,
					 Q	=> B_IN);

    MUX_C_REG_M  : MUX16_2to1 port map	(A	=> DIN16,
					 B	=> M_DATA(15 downto 0),
					 Sel	=> C_REG_M,
					 Q	=> M_IN);

    MUX_C_REG_A	 : MUX16_2to1 port map	(A	=> B_IN,
					 B	=> A_DATA(15 downto 0),
					 Sel	=> C_REG_A,
					 Q	=> A_IN);

    MUX_C_AB_CLA : MUX16_2to1 port map	(A	=> B_DATA(15 downto 0),
					 B	=> A_DATA(15 downto 0),
					 Sel	=> C_AB_CLA,
					 Q	=> AB_DATA);

    M_REG768     : REG_768 port map(DIn	=> M_IN,
					 ENABLE => EnSh_M,
					 CLK	=> CLK,
					 DOut	=> M_DATA);

    A_REG768	 : REG_768 port map(DIn	=> A_IN,
					 ENABLE => EnSh_A,
					 CLK	=> CLK,
					 DOut	=> A_DATA);

    B_REG768     : REG_768 port map(DIn	=> B_IN,
					 ENABLE => EnSh_B,
					 CLK	=> CLK,
					 DOut	=> B_DATA);
    
    E_REG32      : REG_32 port map	(DIn	=> DATAIN,
					 SHIFT	=> Shift_E,
					 ENABLE => Ena_E,
					 CLK	=> CLK,
					 DOut	=> E0);

    MUX_C_OUT    : MUX32_2to1 port map	(A	=> B_DATA(31 downto 0),
					 B	=> A_DATA(31 downto 0),
					 Sel	=> C_OUT,
					 Q	=> DATAOUT);

    MUX_C_M_OUT_SEL : MUX16_2to1
	port map			(A	=> M_DATA(15 downto 0),
					 B	=> M_DATA(16 downto 1),
					 Sel	=> C_M_OUT_SEL,
					 Q	=> SM_DATA);

    BitSel_MUX	 : MUX_1536to1 port map	(A	=> A_DATA,
					 B	=> B_DATA,
					 SEL	=> SEL,
					 ASel	=> ASel,
					 Q	=> ABi_MONT);

    MONT_MUL	 : MONTGOMERY port map	(AI	=> ABi_MONT,
					 B	=> B_DATA,
					 M	=> M_DATA,
					 SRTREM => SRTREM,
					 CLK	=> CLK,
					 RESET	=> RESET_MONT,
					 ENABLE => EnMONT,
					 RSAVE  => MONT_OUT1,
					 RCARRY => MONT_OUT2);
    
    XOR_16	 : XOR16_1 port map	(A	=> SM_DATA,
					 B	=> VZ_M,
					 Q	=> X_M_DATA);

    SEL_RI1_MUX  : MUX768to16 port map (A	=> MONT_OUT1,
					 Sel	=> SEL_RI,
					 Q	=> MONT_OUT1_16);
    
    SEL_RI2_MUX  : MUX768to16 port map (A	=> MONT_OUT2,
					 Sel	=> SEL_RI,
					 Q	=> MONT_OUT2_16);

    MUX_C_CLA_B  : MUX16_2to1 port map	(A	=> MONT_OUT1_16,
					 B	=> AB_DATA,
					 Sel	=> C_CLA_B,
					 Q	=> MONT_REG1);

    MUX_C_CLA_M  : MUX16_2to1 port map	(A	=> MONT_OUT2_16,
					 B	=> X_M_DATA,
					 Sel	=> C_CLA_M,
					 Q	=> MONT_REG2);

    REG16_CLA_B  : REG_16 port map	(DIn	=> MONT_REG1,
					 CLK	=> CLK,
					 DOut	=> MONT_CLA1);

    REG16_CLA_M  : REG_16 port map	(DIn	=> MONT_REG2,
					 CLK	=> CLK,
					 DOut	=> MONT_CLA2);

    CLA16	 : CLA_16 port map	(A	=> MONT_CLA1,
					 B	=> MONT_CLA2,
					 Q	=> CLA_OUT,
					 C_OUT	=> Carry_OUT_CLA,
					 C_IN	=> Carry_IN_CLA);

    Carry_REG1	 : REG_1 port map	(DIn	=> Carry_OUT_CLA,
					 CLK	=> CLK,
					 DOut	=> Carry_REG);

    CLA_REG16	 : REG_16 port map	(DIn	=> CLA_OUT,
					 CLK	=> CLK,
					 DOut	=> REG_OUT);

    MUX_C_CLACARRY : MUX1_2to1 port map	(A	=> Carry_REG,
					 B	=> CARRY_IN,
					 Sel	=> C_CLACARRY,
					 Q	=> Carry_IN_CLA);

    STEUERWERK : STRG port map		(CLK	=> CLK,
					 RESET	=> RESET,
					 VALACC => VALACC,
					 GO	=> GO,
					 MSB	=> REG_OUT(15),
					 E0	=> E0,
					 SELDATA => SELDATA,
					 READY	=> READY,
					 NEXTEXP => NEXTEXP,
					 IN_LOHI => IN_LOHI,
					 Vz_M	=> VZ_M,
					 RESET_MONT => RESET_MONT,
					 En_MONT => EnMONT,
					 SRTREM => SRTREM,
					 ASel   => ASel,
					 CARRY_IN => CARRY_IN,
					 C_M_OUT_SEL => C_M_OUT_SEL,
					 C_AB_CLA => C_AB_CLA,
					 EnSh_M => EnSh_M,
					 EnSh_A => EnSh_A,
					 EnSh_B => EnSh_B,
					 Ena_E  => Ena_E,
					 Shift_E => Shift_E,
					 C_OUT  => C_OUT,
					 C_CLA_M => C_CLA_M,
					 C_CLA_B => C_CLA_B,
					 C_CLACARRY => C_CLACARRY,	  
					 C_REG_M => C_REG_M,
					 C_REG_A => C_REG_A,
					 C_REG_IN => C_REG_IN,
					 SEL_RI => SEL_RI,
					 BitSel => SEL);
    
end RTL;








