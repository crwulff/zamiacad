--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Joerg Holzhauer, Thomas Stanka
-- Group     : DES
--------------------------------------------------------------------
-- Design Unit Name : DES_GESAMT_JH 
-- Purpose : Structure of the DES
-- 
-- File Name :  des_gesamt_jh.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date   28.12.98 | Changes
--        27.01.99 | Inserted a new controll-port (Thomas Stanka)
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  contents Controll-Unit and Datapath of the DES
--  
--------------------------------------------------------------------------


library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   library LIB_des;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity DES_GESAMT_JH is
      Port ( BUFFER_FREE : In    std_logic;
                 CLK : In    std_logic;
             DATA_IN : In    std_logic_vector (1 to 64);
             DATA_IS_KEY : In    std_logic;
             DATA_READY : In    std_logic;
               MODUS : In    std_logic_vector (4 downto 0);
               RESET : In    std_logic;
                TEST : In    std_logic;
             DATA_ACK : Out   std_logic;
             DATA_OUT : Out   std_logic_vector (1 to 64);
             DES_READY : Out   std_logic;
               ERROR : Out   STD_LOGIC;
             KEY_PARITY : Out   std_logic );
end DES_GESAMT_JH;

architecture SCHEMATIC of DES_GESAMT_JH is

   signal  CONTROL : std_logic_vector(0 to 1);
   signal  KEY_SEL : std_logic_vector(1 to 2);
   signal     MUX7 : std_logic;
   signal     MUX6 : std_logic;
   signal     MUX5 : std_logic;
   signal     MUX4 : std_logic;
   signal     MUX3 : std_logic;
   signal     MUX2 : std_logic;
   signal     MUX1 : std_logic;
   signal EN_IV_REG : std_logic;
   signal  EN_REG1 : std_logic;
   signal KEYSTORE_EN : std_logic;
   signal  DECRYPT : std_logic;
   signal EN_DATA_REG : std_logic;
   signal EN_SHIFT_REG : std_logic;

   component STEUERWERK
      Port ( BUFFER_FREE : In    std_logic;
                 CLK : In    std_logic;
             DATA_IS_KEY : In    std_logic;
             DATA_READY : In    std_logic;
               MODUS : In    std_logic_vector (4 downto 0);
               RESET : In    std_logic;
                TEST : In    std_logic;
             CONTROL_1 : Out   std_logic_vector (1 downto 0);
             DATA_ACK1 : Out   std_logic;
             DES_READY1 : Out   std_logic;
             EN_DECRYPT_1 : Out   std_logic;
             KEYSTEEN : Out   std_logic;
             KEYSTSEL : Out   std_logic_vector (1 downto 0);
             LATCH_EN164 : Out   std_logic;
             LATCH_EN32RL : Out   std_logic;
             LATCH_ENIV : Out   std_logic;
             MUX_SEL_1 : Out   std_logic;
             MUX_SEL_2 : Out   std_logic;
             MUX_SEL_3 : Out   std_logic;
             MUX_SEL_4 : Out   std_logic;
             MUX_SEL_5 : Out   std_logic;
             MUX_SEL_6 : Out   std_logic;
	     MUX_SEL_7 : Out   std_logic;
             WRITE_EN_1 : Out   std_logic );
   end component;

   component ECB_CBCBLOCK_JH
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
   end component;

begin

   I_1 : STEUERWERK
      Port Map ( BUFFER_FREE=>BUFFER_FREE, CLK=>CLK,
                 DATA_IS_KEY=>DATA_IS_KEY, DATA_READY=>DATA_READY,
                 MODUS(4 downto 0)=>MODUS(4 downto 0), RESET=>RESET,
                 TEST=>TEST, CONTROL_1(1 downto 0)=>CONTROL(0 to 1),
                 DATA_ACK1=>DATA_ACK, DES_READY1=>DES_READY,
                 EN_DECRYPT_1=>DECRYPT, KEYSTEEN=>KEYSTORE_EN,
                 KEYSTSEL(1 downto 0)=>KEY_SEL(1 to 2),
                 LATCH_EN164=>EN_REG1, LATCH_EN32RL=>EN_DATA_REG,
                 LATCH_ENIV=>EN_IV_REG, MUX_SEL_1=>MUX1, MUX_SEL_2=>MUX2,
                 MUX_SEL_3=>MUX3, MUX_SEL_4=>MUX4, MUX_SEL_5=>MUX5,
                 MUX_SEL_6=>MUX6, MUX_SEL_7=>MUX7, WRITE_EN_1=>EN_SHIFT_REG );
   DES : ECB_CBCBLOCK_JH
      Port Map ( CLK=>CLK, CONTROL(0 to 1)=>CONTROL(0 to 1),
                 DATA_IN(1 to 64)=>DATA_IN(1 to 64), DECRYPT=>DECRYPT,
                 EN_DATA_REG=>EN_DATA_REG, EN_SHIFT_REG=>EN_SHIFT_REG,
                 KEY_SEL(1 to 2)=>KEY_SEL(1 to 2),
                 KEYSTORE_EN=>KEYSTORE_EN, LATCH_ENIV=>EN_IV_REG,
                 MUX1=>MUX1, MUX2=>MUX2, MUX3=>MUX3, MUX4=>MUX4,
                 MUX5=>MUX5, MUX6=>MUX6, MUX7=>MUX7, RANDOM_KEY=>MODUS(4),
                 REG1_ENABLE=>EN_REG1, RESET=>RESET,
                 DATA_OUT(1 to 64)=>DATA_OUT(1 to 64),
                 PARITY=>KEY_PARITY );

end SCHEMATIC;

configuration CFG_DES_GESAMT_JH_SCHEMATIC of DES_GESAMT_JH is

   for SCHEMATIC
      for I_1: STEUERWERK
         use entity LIB_des.STEUERWERK(RTL);
      end for;
      for DES: ECB_CBCBLOCK_JH
         use configuration LIB_des.CFG_ECB_CBCBLOCK_JH_SCHEMATIC;
      end for;
   end for;

end CFG_DES_GESAMT_JH_SCHEMATIC;
