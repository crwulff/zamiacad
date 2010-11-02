-------------------------------------------------------------------------------
-- Crypto Chip
-- Copyright (C) 1999, Projektgruppe WS98/99
-- University of Stuttgart / Department of Computer Science / IFI-RA
-------------------------------------------------------------------------------
-- Designers:        Joerg Holzhauer, Thomas Stanka
-- Group    :        DES
-------------------------------------------------------------------------------
-- Design Unit Name: DESBLOCK_JH
-- Purpose:          
--
-- File Name:        desblock_jh.vhd
-------------------------------------------------------------------------------
-- Simulator :       SYNOPSIS VHDL System Simulator (VSS) Version 3.2.a
-------------------------------------------------------------------------------
-- Date   28.12.98  |  Changes
--        19.01.99  |  Inserted a new multiplexer MUX7 to increase the
--                  |  DES overall speed T.S.
--                  |
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- contents :        structure of the DES-core
-------------------------------------------------------------------------------


   library IEEE;
   library LIB_des;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity DESBLOCK_JH is
      Port (     CLK : In    std_logic;
             CONTROL : In    std_logic_vector (0 to 1);
             DATA_IN : In    std_logic_vector (1 to 64);
             DECRYPT : In    std_logic;
             EN_DATA_REG : In    std_logic;
             EN_SHIFT_REG : In    std_logic;
              KEY_IN : In    std_logic_vector (1 to 56);
             MUX_SEL_5 : In    std_logic;
             MUX_SEL_6 : In    std_logic;
	     MUX_SEL_7 : in    STD_LOGIC;	
               RESET : In    std_logic;
             DATA_OUT : Out   std_logic_vector (1 to 64) );
end DESBLOCK_JH;

architecture SCHEMATIC of DESBLOCK_JH is

   signal    E_OUT : std_logic_vector(1 to 48);
   signal  PC2_OUT : std_logic_vector(1 to 48);
   signal  LREG_IN : std_logic_vector(1 to 32);
   signal  RREG_IN : std_logic_vector(1 to 32);
   signal S_BOX_IN : std_logic_vector(1 to 48);
   signal IP_INV_IN : std_logic_vector(1 to 64);
   signal   IP_OUT : std_logic_vector(1 to 64);
   signal XOR32_MUX : std_logic_vector(1 to 32);
   signal   PC2_IN : std_logic_vector(1 to 56);
   signal  PC1_OUT : std_logic_vector(1 to 56);
   signal S_BOX_OUT : std_logic_vector(1 to 32);
   signal    P_XOR : std_logic_vector(1 to 32);
   signal MUXin7 : STD_LOGIC_VECTOR(1 to 32);

   component DES_INVERSEINITIALPERMUTATION
      Port (       I : In    std_logic_vector (1 to 64);
                   O : Out   std_logic_vector (1 to 64) );
   end component;

   component WORD_MUX32
      Port (     IN0 : In    std_logic_vector (1 to 32);
                 IN1 : In    std_logic_vector (1 to 32);
                 SEL : In    std_logic;
                   Y : Out   std_logic_vector (1 to 32) );
   end component;

   component XOR48
      Port (       A : In    std_logic_vector (1 to 48);
                   B : In    std_logic_vector (1 to 48);
                   O : Out   std_logic_vector (1 to 48) );
   end component;

   component SHIFT_REG_28
      Port (     CLK : In    std_logic;
             CONTROL : In    std_logic_vector (0 to 1);
                D_IN : In    std_logic_vector (1 to 28);
             EN_DECRYPT : In    std_logic;
               RESET : In    std_logic;
             WRITE_EN : In    std_logic;
               D_OUT : Out   std_logic_vector (1 to 28) );
   end component;

   component DES_KeyPermutation2
      Port (       I : In    std_logic_vector (1 to 56);
                   O : Out   std_logic_vector (1 to 48) );
   end component;

   component DES_KeyPermutation1
      Port (       I : In    std_logic_vector (1 to 56);
                   O : Out   std_logic_vector (1 to 56) );
   end component;

   component WORD_REG_32
      Port (     CLK : In    std_logic;
                   D : In    std_logic_vector (1 to 32);
             LATCH_EN : In    std_logic;
               RESET : In    std_logic;
                   Q : Out   std_logic_vector (1 to 32) );
   end component;

   component DES_INITIALPERMUTATION
      Port (       I : In    std_logic_vector (1 to 64);
                   O : Out   std_logic_vector (1 to 64) );
   end component;

   component DES_ExpansionGate
      Port (       I : In    std_logic_vector (1 to 32);
                   O : Out   std_logic_vector (1 to 48) );
   end component;

   component XOR32
      Port (       A : In    std_logic_vector (1 to 32);
                   B : In    std_logic_vector (1 to 32);
                   O : Out   std_logic_vector (1 to 32) );
   end component;

   component DES_Permutation
      Port (       I : In    std_logic_vector (1 to 32);
                   O : Out   std_logic_vector (1 to 32) );
   end component;

   component DES_S8_Box
      Port (       I : In    std_logic_vector (1 to 6);
                   O : Out   std_logic_vector (1 to 4) );
   end component;

   component DES_S7_Box
      Port (       I : In    std_logic_vector (1 to 6);
                   O : Out   std_logic_vector (1 to 4) );
   end component;

   component DES_S6_Box
      Port (       I : In    std_logic_vector (1 to 6);
                   O : Out   std_logic_vector (1 to 4) );
   end component;

   component DES_S5_Box
      Port (       I : In    std_logic_vector (1 to 6);
                   O : Out   std_logic_vector (1 to 4) );
   end component;

   component DES_S4_Box
      Port (       I : In    std_logic_vector (1 to 6);
                   O : Out   std_logic_vector (1 to 4) );
   end component;

   component DES_S3_Box
      Port (       I : In    std_logic_vector (1 to 6);
                   O : Out   std_logic_vector (1 to 4) );
   end component;

   component DES_S2_Box
      Port (       I : In    std_logic_vector (1 to 6);
                   O : Out   std_logic_vector (1 to 4) );
   end component;

   component DES_S1_Box
      Port (       I : In    std_logic_vector (1 to 6);
                   O : Out   std_logic_vector (1 to 4) );
   end component;

begin

   DES_INVERSEINITIALPERMUTATION1 : DES_INVERSEINITIALPERMUTATION
      Port Map ( I(1 to 64)=>IP_INV_IN(1 to 64),
                 O(1 to 64)=>DATA_OUT(1 to 64) );
   
   MUX7 : WORD_MUX32
      Port Map ( IN0(1 to 32)=>MUXin7(1 to 32),
                 IN1(1 to 32)=>IP_INV_IN(33 to 64), SEL=>MUX_SEL_7,
                 Y(1 to 32)=>RREG_IN(1 to 32) );
   
   MUX6 : WORD_MUX32
      Port Map ( IN0(1 to 32)=>IP_OUT(33 to 64),
                 IN1(1 to 32)=>XOR32_MUX(1 to 32), SEL=>MUX_SEL_6,
                 Y(1 to 32)=>MUXin7(1 to 32));
   MUX5 : WORD_MUX32
      Port Map ( IN0(1 to 32)=>IP_OUT(1 to 32),
                 IN1(1 to 32)=>IP_INV_IN(1 to 32), SEL=>MUX_SEL_5,
                 Y(1 to 32)=>LREG_IN(1 to 32) );
   XOR481: XOR48
      Port Map ( A(1 to 48)=>E_OUT(1 to 48),
                 B(1 to 48)=>PC2_OUT(1 to 48),
                 O(1 to 48)=>S_BOX_IN(1 to 48) );
   SHIFT_REG_D : SHIFT_REG_28
      Port Map ( CLK=>CLK, CONTROL(0 to 1)=>CONTROL(0 to 1),
                 D_IN(1 to 28)=>PC1_OUT(29 to 56), EN_DECRYPT=>DECRYPT,
                 RESET=>RESET, WRITE_EN=>EN_SHIFT_REG,
                 D_OUT(1 to 28)=>PC2_IN(29 to 56) );
   SHIFT_REG_C : SHIFT_REG_28
      Port Map ( CLK=>CLK, CONTROL(0 to 1)=>CONTROL(0 to 1),
                 D_IN(1 to 28)=>PC1_OUT(1 to 28), EN_DECRYPT=>DECRYPT,
                 RESET=>RESET, WRITE_EN=>EN_SHIFT_REG,
                 D_OUT(1 to 28)=>PC2_IN(1 to 28) );
   DES_KeyPermutation21 : DES_KeyPermutation2
      Port Map ( I(1 to 56)=>PC2_IN(1 to 56),
                 O(1 to 48)=>PC2_OUT(1 to 48) );
   DES_KeyPermutation11 : DES_KeyPermutation1
      Port Map ( I(1 to 56)=>KEY_IN(1 to 56),
                 O(1 to 56)=>PC1_OUT(1 to 56) );
   DATA_REG_LEFT : WORD_REG_32
      Port Map ( CLK=>CLK, D(1 to 32)=>LREG_IN(1 to 32),
                 LATCH_EN=>EN_DATA_REG, RESET=>RESET,
                 Q(1 to 32)=>IP_INV_IN(33 to 64) );
   DATA_REG_RIGHT : WORD_REG_32
      Port Map ( CLK=>CLK, D(1 to 32)=>RREG_IN(1 to 32),
                 LATCH_EN=>EN_DATA_REG, RESET=>RESET,
                 Q(1 to 32)=>IP_INV_IN(1 to 32) );
   DES_INITIALPERMUTATION1 : DES_INITIALPERMUTATION
      Port Map ( I(1 to 64)=>DATA_IN(1 to 64),
                 O(1 to 64)=>IP_OUT(1 to 64) );
   EXPANSION : DES_ExpansionGate

      Port Map ( I(1 to 32)=>IP_INV_IN(1 to 32),
                 O(1 to 48)=>E_OUT(1 to 48) );
   XOR321 : XOR32
      Port Map ( A(1 to 32)=>P_XOR(1 to 32),
                 B(1 to 32)=>IP_INV_IN(33 to 64),
                 O(1 to 32)=>XOR32_MUX(1 to 32) );
   PERMUTATION : DES_Permutation
      Port Map ( I(1 to 32)=>S_BOX_OUT(1 to 32),
                 O(1 to 32)=>P_XOR(1 to 32) );
   DES_S8_BOX1 : DES_S8_BOX
      Port Map ( I(1 to 6)=>S_BOX_IN(43 to 48),
                 O(1 to 4)=>S_BOX_OUT(29 to 32) );
   DES_S7_Box1 : DES_S7_Box
      Port Map ( I(1 to 6)=>S_BOX_IN(37 to 42),
                 O(1 to 4)=>S_BOX_OUT(25 to 28) );
   DES_S6_Box1 : DES_S6_Box
      Port Map ( I(1 to 6)=>S_BOX_IN(31 to 36),
                 O(1 to 4)=>S_BOX_OUT(21 to 24) );
   DES_S5_Box1 : DES_S5_Box
      Port Map ( I(1 to 6)=>S_BOX_IN(25 to 30),
                 O(1 to 4)=>S_BOX_OUT(17 to 20) );
   DES_S4_Box1 : DES_S4_Box
      Port Map ( I(1 to 6)=>S_BOX_IN(19 to 24),
                 O(1 to 4)=>S_BOX_OUT(13 to 16) );
   DES_S3_Box1 : DES_S3_Box
      Port Map ( I(1 to 6)=>S_BOX_IN(13 to 18),
                 O(1 to 4)=>S_BOX_OUT(9 to 12) );
   DES_S2_Box1 : DES_S2_Box
      Port Map ( I(1 to 6)=>S_BOX_IN(7 to 12),
                 O(1 to 4)=>S_BOX_OUT(5 to 8) );
   DES_S1_Box1 : DES_S1_Box
      Port Map ( I(1 to 6)=>S_BOX_IN(1 to 6),
                 O(1 to 4)=>S_BOX_OUT(1 to 4) );

end SCHEMATIC;

library LIB_des;
configuration CFG_DESBLOCK_JH_SCHEMATIC of DESBLOCK_JH is

   for SCHEMATIC
      for DES_INVERSEINITIALPERMUTATION1: DES_INVERSEINITIALPERMUTATION
         use entity LIB_des.DES_INVERSEINITIALPERMUTATION(BEHAVORIAL);
      end for;
      for MUX7, MUX6, MUX5: WORD_MUX32
         use entity LIB_des.WORD_MUX32(BEHAVIOUR);
      end for;
      for XOR481: XOR48
         use entity LIB_des.XOR48(BEHAVIORAL);
      end for;
      for SHIFT_REG_D, SHIFT_REG_C: SHIFT_REG_28
         use entity LIB_des.SHIFT_REG_28(BEHAV);
      end for;
      for DES_KeyPermutation21: DES_KeyPermutation2
         use entity LIB_des.DES_KeyPermutation2(BEHAVORIAL);
      end for;
      for DES_KeyPermutation11: DES_KeyPermutation1
         use entity LIB_des.DES_KeyPermutation1(BEHAVORIAL);
      end for;
      for DATA_REG_LEFT, DATA_REG_RIGHT: WORD_REG_32
         use entity LIB_des.WORD_REG_32(BEHAVIOUR);
      end for;
      for DES_INITIALPERMUTATION1: DES_INITIALPERMUTATION
         use entity LIB_des.DES_INITIALPERMUTATION(BEHAVORIAL);
      end for;
      for EXPANSION: DES_ExpansionGate
         use entity LIB_des.DES_ExpansionGate(BEHAVORIAL);
      end for;
      for XOR321: XOR32
         use entity LIB_des.XOR32(BEHAVIORAL);
      end for;
      for PERMUTATION: DES_Permutation
         use entity LIB_des.DES_Permutation(BEHAVORIAL);
      end for;
      for DES_S8_BOX1: DES_S8_BOX
         use entity LIB_des.DES_S8_BOX(BEHAVORIAL);
      end for;
      for DES_S7_Box1: DES_S7_Box
         use entity LIB_des.DES_S7_Box(BEHAVORIAL);
      end for;
      for DES_S6_Box1: DES_S6_Box
         use entity LIB_des.DES_S6_Box(BEHAVORIAL);
      end for;
      for DES_S5_Box1: DES_S5_Box
         use entity LIB_des.DES_S5_Box(BEHAVORIAL);
      end for;
      for DES_S4_Box1: DES_S4_Box
         use entity LIB_des.DES_S4_Box(BEHAVORIAL);
      end for;
      for DES_S3_Box1: DES_S3_Box
         use entity LIB_des.DES_S3_Box(BEHAVORIAL);
      end for;
      for DES_S2_Box1: DES_S2_Box
         use entity LIB_des.DES_S2_Box(BEHAVORIAL);
      end for;
      for DES_S1_Box1: DES_S1_Box
         use entity LIB_des.DES_S1_Box(BEHAVORIAL);
      end for;
   end for;

end CFG_DESBLOCK_JH_SCHEMATIC;
