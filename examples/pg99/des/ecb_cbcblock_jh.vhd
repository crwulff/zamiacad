-------------------------------------------------------------------------------
-- Crypto Chip
-- Copyright (C) 1999, Projektgruppe WS98/99
-- University of Stuttgart / Department of Computer Science / IFI-RA
-------------------------------------------------------------------------------
-- Designers:        Joerg Holzhauer, Thomas Stanka
-- Group    :        DES
-------------------------------------------------------------------------------
-- Design Unit Name: ECB_CBCBLOCK_JH
-- Purpose:          
--
-- File Name:        ecb_cbcblock_jh.vhd
-------------------------------------------------------------------------------
-- Simulator :       SYNOPSIS VHDL System Simulator (VSS) Version 3.2.a
-------------------------------------------------------------------------------
-- Date   28.12.98  |  Changes
--        19.01.99  |  Inserted a new port MUX7 to increase the
--                  |  DES overall speed T.S.
--                  |
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- contents :       the complete Datapath of the DES
-------------------------------------------------------------------------------

   library IEEE;
   library LIB_des;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity ECB_CBCBLOCK_JH is
      Port (     CLK : In    std_logic;
             CONTROL : In    std_logic_vector (0 to 1);
             DATA_IN : In    std_logic_vector (1 to 64);
             DECRYPT : In    std_logic;
             EN_DATA_REG : In    std_logic;
             EN_SHIFT_REG : In    std_logic;
             KEY_SEL : In    std_logic_vector (1 to 2);
             KEYSTORE_EN : In    std_logic;
             LATCH_ENIV : In    std_logic;
                MUX1 : In    std_logic;
                MUX2 : In    std_logic;
                MUX3 : In    std_logic;
                MUX4 : In    std_logic;
                MUX5 : In    std_logic;
                MUX6 : In    std_logic;
		MUX7 : in    std_logic;
             RANDOM_KEY : In    std_logic;
             REG1_ENABLE : In    std_logic;
               RESET : In    std_logic;
             DATA_OUT : Out   std_logic_vector (1 to 64);
              PARITY : Out   std_logic );
end ECB_CBCBLOCK_JH;

architecture SCHEMATIC of ECB_CBCBLOCK_JH is

   signal XOR1_MUX1 : std_logic_vector(1 to 64);
   signal MUX1_DES : std_logic_vector(1 to 64);
   signal DES_MUX2_MUX3_XOR2 : std_logic_vector(1 to 64);
   signal XOR2_MUX3 : std_logic_vector(1 to 64);
   signal MUX4_KEYV : std_logic_vector(1 to 64);
   signal KEYV_KEYS : std_logic_vector(1 to 56);
   signal  KEY_OUT : std_logic_vector(1 to 56);
   signal  IV_MUX2 : std_logic_vector(1 to 64);
   signal MUX2_REG1_XOR1 : std_logic_vector(1 to 64);
   signal REG1_XOR2 : std_logic_vector(1 to 64);
   signal DATA_OUT_DUMMY : std_logic_vector (1 to 64);

   component WORD_MUX2
      Port (     IN0 : In    std_logic_vector (1 to 64);
                 IN1 : In    std_logic_vector (1 to 64);
                 SEL : In    std_logic;
                   Y : Out   std_logic_vector (1 to 64) );
   end component;

   component XOR64
      Port (       A : In    std_logic_vector (1 to 64);
                   B : In    std_logic_vector (1 to 64);
                   O : Out   std_logic_vector (1 to 64) );
   end component;

   component DESBLOCK_JH
      Port (     CLK : In    std_logic;
             CONTROL : In    std_logic_vector (0 to 1);
             DATA_IN : In    std_logic_vector (1 to 64);
             DECRYPT : In    std_logic;
             EN_DATA_REG : In    std_logic;
             EN_SHIFT_REG : In    std_logic;
              KEY_IN : In    std_logic_vector (1 to 56);
             MUX_SEL_5 : In    std_logic;
             MUX_SEL_6 : In    std_logic;
	     MUX_SEL_7 : In    std_logic;
               RESET : In    std_logic;
             DATA_OUT : Out   std_logic_vector (1 to 64) );
   end component;

   component KEYSTORE
      Port (     CLK : In    std_logic;
              ENABLE : In    std_logic;
              KEY_IN : In    std_logic_vector (1 to 56);
               RESET : In    std_logic;
                 SEL : In    std_logic_vector (1 to 2);
             KEY_OUT : Out   std_logic_vector (1 to 56) );
   end component;

   component KEYVALID
      Port (    CTRL : In    std_logic;
              KEY_IN : In    std_logic_vector (1 to 64);
             KEY_OUT : Out   std_logic_vector (1 to 56);
              PARITY : Out   std_logic );
   end component;

   component WORD_REG_64
      Port (     CLK : In    std_logic;
                   D : In    std_logic_vector (1 to 64);
             LATCH_EN : In    std_logic;
               RESET : In    std_logic;
                   Q : Out   std_logic_vector (1 to 64) );
   end component;

begin

   DATA_OUT <= DATA_OUT_DUMMY;

   I_18 : WORD_MUX2
      Port Map ( IN0(1 to 64)=>DATA_IN(1 to 64),
                 IN1(1 to 64)=>XOR1_MUX1(1 to 64), SEL=>MUX1,
                 Y(1 to 64)=>MUX1_DES(1 to 64) );
   I_19 : WORD_MUX2
      Port Map ( IN0(1 to 64)=>DATA_IN(1 to 64),
                 IN1(1 to 64)=>DATA_OUT_DUMMY(1 to 64), SEL=>MUX4,
                 Y(1 to 64)=>MUX4_KEYV(1 to 64) );
   I_20 : WORD_MUX2
      Port Map ( IN0(1 to 64)=>DES_MUX2_MUX3_XOR2(1 to 64),
                 IN1(1 to 64)=>XOR2_MUX3(1 to 64), SEL=>MUX3,
                 Y(1 to 64)=>DATA_OUT_DUMMY(1 to 64) );
   I_21 : WORD_MUX2
      Port Map ( IN0(1 to 64)=>DES_MUX2_MUX3_XOR2(1 to 64),
                 IN1(1 to 64)=>IV_MUX2(1 to 64), SEL=>MUX2,
                 Y(1 to 64)=>MUX2_REG1_XOR1(1 to 64) );
   I_22 : XOR64
      Port Map ( A(1 to 64)=>REG1_XOR2(1 to 64),
                 B(1 to 64)=>DES_MUX2_MUX3_XOR2(1 to 64),
                 O(1 to 64)=>XOR2_MUX3(1 to 64) );
   I_23 : XOR64
      Port Map ( A(1 to 64)=>DATA_IN(1 to 64),
                 B(1 to 64)=>MUX2_REG1_XOR1(1 to 64),
                 O(1 to 64)=>XOR1_MUX1(1 to 64) );
   I_17 : DESBLOCK_JH
      Port Map ( CLK=>CLK, CONTROL(0 to 1)=>CONTROL(0 to 1),
                 DATA_IN(1 to 64)=>MUX1_DES(1 to 64), DECRYPT=>DECRYPT,
                 EN_DATA_REG=>EN_DATA_REG, EN_SHIFT_REG=>EN_SHIFT_REG,
                 KEY_IN(1 to 56)=>KEY_OUT(1 to 56), MUX_SEL_5=>MUX5,
                 MUX_SEL_6=>MUX6, MUX_SEL_7 => MUX7, RESET=>RESET,
                 DATA_OUT(1 to 64)=>DES_MUX2_MUX3_XOR2(1 to 64) );
   I_8 : KEYSTORE
      Port Map ( CLK=>CLK, ENABLE=>KEYSTORE_EN,
                 KEY_IN(1 to 56)=>KEYV_KEYS(1 to 56), RESET=>RESET,
                 SEL(1 to 2)=>KEY_SEL(1 to 2),
                 KEY_OUT(1 to 56)=>KEY_OUT(1 to 56) );
   I_9 : KEYVALID
      Port Map ( CTRL=>RANDOM_KEY, KEY_IN(1 to 64)=>MUX4_KEYV(1 to 64),
                 KEY_OUT(1 to 56)=>KEYV_KEYS(1 to 56), PARITY=>PARITY );
   I_16 : WORD_REG_64
      Port Map ( CLK=>CLK, D(1 to 64)=>MUX2_REG1_XOR1(1 to 64),
                 LATCH_EN=>REG1_ENABLE, RESET=>RESET,
                 Q(1 to 64)=>REG1_XOR2(1 to 64) );
   I_6 : WORD_REG_64
      Port Map ( CLK=>CLK, D(1 to 64)=>DATA_IN(1 to 64),
                 LATCH_EN=>LATCH_ENIV, RESET=>RESET,
                 Q(1 to 64)=>IV_MUX2(1 to 64) );

end SCHEMATIC;

configuration CFG_ECB_CBCBLOCK_JH_SCHEMATIC of ECB_CBCBLOCK_JH is

   for SCHEMATIC
      for I_18, I_19, I_20, I_21: WORD_MUX2
         use entity LIB_des.WORD_MUX2(BEHAVIOUR);
      end for;
      for I_22, I_23: XOR64
         use entity LIB_des.XOR64(BEHAVIORAL);
      end for;
      for I_17: DESBLOCK_JH
         use configuration LIB_des.CFG_DESBLOCK_JH_SCHEMATIC;
      end for;
      for I_8: KEYSTORE
         use entity LIB_des.KEYSTORE(BEHAVIORAL);
      end for;
      for I_9: KEYVALID
         use entity LIB_des.KEYVALID(BEHAVIORAL);
      end for;
      for I_16, I_6: WORD_REG_64
         use entity LIB_des.WORD_REG_64(BEHAVIOUR);
      end for;
   end for;

end CFG_ECB_CBCBLOCK_JH_SCHEMATIC;

