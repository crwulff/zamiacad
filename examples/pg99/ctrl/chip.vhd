-- VHDL Model Created from SGE Schematic chip.sch -- Jan 21 12:12:17 1999

--------------------------------------------------------------------------
-------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : 
-- Purpose : Der komplette chip ohne Pad-zellen.
-- 
-- File Name : chip.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date  30.11.98  | Changes Created bzw Kopfkommentar added
--       29.12.98  | 'TEST' signal at IO removed
--       04.01.99  | 'ENABLE_TEST' signal added
--       19.01.99  | 'ENABLE_TEST' port added
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Hierin werden die Module RSA DES CTRL(mainctrl und io) zu einem
--  ganzem zusammengefuegt.
--------------------------------------------------------------------------

library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity CHIP is
      Port (       A : In    std_logic_vector (2 downto 0);
             CARDCHANGE : In    std_logic;
                  CE : In    std_logic;
                 CLK : In    std_logic;
                D_IN : In    std_logic_vector (31 downto 0);
             IO_MODE : In    std_logic_vector (1 downto 0);
                  OE : In    std_logic;
                 RES : In    std_logic;
              SCL_IN : In    std_logic;
              SDA_IN : In    std_logic;
                  WR : In    std_logic;
                BUSY : Out   std_logic;
               D_OUT : Out   std_logic_vector (31 downto 0);
             D_OUT_EN : Out   std_logic;
             ENABLE_TEST : Out   std_logic;
                 INT : Out   std_logic;
             SCL_OUT : Out   std_logic;
             SDA_OUT : Out   std_logic );
end CHIP;

architecture SCHEMATIC of CHIP is

   signal  RSA_SEL : std_logic_vector(1 downto 0);
   signal    RSA_W : std_logic_vector(31 downto 0);
   signal    RSA_R : std_logic_vector(31 downto 0);
   signal     DATA : std_logic_vector(31 downto 0);
   signal   STATUS : std_logic_vector(7 downto 0);
   signal DES_MODE : std_logic_vector(4 downto 0);
   signal     CTRL : std_logic_vector(7 downto 0);
   signal    DES_W : std_logic_vector(63 downto 0);
   signal    DES_R : std_logic_vector(63 downto 0);
   signal TEST_MODE : std_logic;
   signal     N_27 : std_logic;
   signal     N_24 : std_logic;
   signal     N_25 : std_logic;
   signal     N_26 : std_logic;
   signal      N_1 : std_logic;
   signal      N_2 : std_logic;
   signal      N_4 : std_logic;
   signal     N_11 : std_logic;
   signal     N_12 : std_logic;
   signal     N_13 : std_logic;
   signal     N_14 : std_logic;
   signal     N_16 : std_logic;
   signal     N_17 : std_logic;
   signal     N_18 : std_logic;
   signal     N_19 : std_logic;
   signal     N_21 : std_logic;
   signal     N_22 : std_logic;
   signal     N_23 : std_logic;

   component RSA_RESET_GEN
      Port ( CTRL_CHANGE : In    std_logic;
               RESET : In    std_logic;
             TEST_MODE : In    std_logic;
             RSA_RESET : Out   std_logic );
   end component;

   component IO
      Port (    ADDR : In    std_logic_vector (2 downto 0);
                  CE : In    std_logic;
                 CLK : In    std_logic;
                 DIN : In    std_logic_vector (31 downto 0);
             DREGINEN : In    std_logic;
             DREGOUTEN : In    std_logic;
             MODESEL : In    std_logic_vector (1 downto 0);
                  OE : In    std_logic;
               RESET : In    std_logic;
                  RW : In    std_logic;
               SCLIN : In    std_logic;
               SDAIN : In    std_logic;
              STATUS : In    std_logic_vector (7 downto 0);
             STATUSINEN : In    std_logic;
                DATA : InOut std_logic_vector (31 downto 0);
                CTRL : Out   std_logic_vector (7 downto 0);
             CTRLCHANGE : Out   std_logic;
             DATAVALID : Out   std_logic;
                DOUT : Out   std_logic_vector (31 downto 0);
             DOUTEMPTY : Out   std_logic;
               OE_IO : Out   std_logic;
              SCLOUT : Out   std_logic;
              SDAOUT : Out   std_logic );
   end component;

   component DES
      Port ( BUFFER_FREE : In    std_logic;
                 CLK : In    std_logic;
             DATA_IN : In    std_logic_vector (63 downto 0);
             DATA_IS_KEY : In    std_logic;
             DATA_READY : In    std_logic;
               MODUS : In    std_logic_vector (4 downto 0);
               RESET : In    std_logic;
                TEST : In    std_logic;
             DATA_ACK : Out   std_logic;
             DATA_OUT : Out   std_logic_vector (63 downto 0);
             DES_READY : Out   std_logic;
               ERROR : Out   std_logic;
             KEY_PARITY : Out   std_logic );
   end component;

   component MAINCTRL
      Port ( CARDCHANGE : In    std_logic;
                 CLK : In    std_logic;
                CTRL : In    std_logic_vector (7 downto 0);
             CTRL_CHANGE : In    std_logic;
             DATA_VALID : In    std_logic;
             DES_ACK : In    std_logic;
             DES_ERR : In    std_logic;
              DES_IN : In    std_logic_vector (63 downto 0);
             DES_PARITY : In    std_logic;
             DES_READY : In    std_logic;
             DOUT_EMPTY : In    std_logic;
               RESET : In    std_logic;
              RSA_IN : In    std_logic_vector (31 downto 0);
             RSA_NEXTEXP : In    std_logic;
             RSA_READY : In    std_logic;
                   D : InOut std_logic_vector (31 downto 0);
                BUSY : Out   std_logic;
             DES_BUFFER_FREE : Out   std_logic;
             DES_DATA_IS_KEY : Out   std_logic;
             DES_DATA_READY : Out   std_logic;
             DES_MODE : Out   std_logic_vector (4 downto 0);
             DES_OUT : Out   std_logic_vector (63 downto 0);
             DREGINEN : Out   std_logic;
             DREGOUTEN : Out   std_logic;
             ENABLE_TEST : Out   std_logic;
                 INT : Out   std_logic;
              RSA_GO : Out   std_logic;
             RSA_OUT : Out   std_logic_vector (31 downto 0);
             RSA_SEL : Out   std_logic_vector (1 downto 0);
             RSA_VAL_ACC : Out   std_logic;
               STATE : Out   std_logic_vector (7 downto 0);
             STATE_EN : Out   std_logic );
   end component;

   component RSA
      Port (     CLK : In    std_logic;
              DATAIN : In    std_logic_vector (31 downto 0);
                  GO : In    std_logic;
               RESET : In    std_logic;
             SELDATA : In    std_logic_vector (1 downto 0);
              VALACC : In    std_logic;
             DATAOUT : Out   std_logic_vector (31 downto 0);
             NEXTEXP : Out   std_logic;
               READY : Out   std_logic );
   end component;

   for all : DES use entity WORK.DES(RTL);
   for all : RSA use entity WORK.RSA(RTL);

begin

   I_7 : RSA_RESET_GEN
      Port Map ( CTRL_CHANGE=>N_25, RESET=>RES, TEST_MODE=>TEST_MODE,
                 RSA_RESET=>N_27 );
   I_IO : IO
      Port Map ( ADDR(2 downto 0)=>A(2 downto 0), CE=>CE, CLK=>CLK,
                 DIN(31 downto 0)=>D_IN(31 downto 0), DREGINEN=>N_2,
                 DREGOUTEN=>N_1,
                 MODESEL(1 downto 0)=>IO_MODE(1 downto 0), OE=>OE,
                 RESET=>RES, RW=>WR, SCLIN=>SCL_IN, SDAIN=>SDA_IN,
                 STATUS(7 downto 0)=>STATUS(7 downto 0), STATUSINEN=>N_4,
                 DATA(31 downto 0)=>DATA(31 downto 0),
                 CTRL(7 downto 0)=>CTRL(7 downto 0), CTRLCHANGE=>N_25,
                 DATAVALID=>N_26, DOUT(31 downto 0)=>D_OUT(31 downto 0),
                 DOUTEMPTY=>N_24, OE_IO=>D_OUT_EN, SCLOUT=>SCL_OUT,
                 SDAOUT=>SDA_OUT );
   I_DES : DES
      Port Map ( BUFFER_FREE=>N_23, CLK=>CLK,
                 DATA_IN(63 downto 0)=>DES_W(63 downto 0),
                 DATA_IS_KEY=>N_21, DATA_READY=>N_22,
                 MODUS(4 downto 0)=>DES_MODE(4 downto 0), RESET=>RES,
                 TEST=>TEST_MODE, DATA_ACK=>N_19,
                 DATA_OUT(63 downto 0)=>DES_R(63 downto 0),
                 DES_READY=>N_18, ERROR=>N_16, KEY_PARITY=>N_17 );
   I_MAINCTRL : MAINCTRL
      Port Map ( CARDCHANGE=>CARDCHANGE, CLK=>CLK,
                 CTRL(7 downto 0)=>CTRL(7 downto 0), CTRL_CHANGE=>N_25,
                 DATA_VALID=>N_26, DES_ACK=>N_19, DES_ERR=>N_16,
                 DES_IN(63 downto 0)=>DES_R(63 downto 0),
                 DES_PARITY=>N_17, DES_READY=>N_18, DOUT_EMPTY=>N_24,
                 RESET=>RES, RSA_IN(31 downto 0)=>RSA_R(31 downto 0),
                 RSA_NEXTEXP=>N_14, RSA_READY=>N_13,
                 D(31 downto 0)=>DATA(31 downto 0), BUSY=>BUSY,
                 DES_BUFFER_FREE=>N_23, DES_DATA_IS_KEY=>N_21,
                 DES_DATA_READY=>N_22,
                 DES_MODE(4 downto 0)=>DES_MODE(4 downto 0),
                 DES_OUT(63 downto 0)=>DES_W(63 downto 0), DREGINEN=>N_2,
                 DREGOUTEN=>N_1, ENABLE_TEST=>ENABLE_TEST, INT=>INT,
                 RSA_GO=>N_12, RSA_OUT(31 downto 0)=>RSA_W(31 downto 0),
                 RSA_SEL(1 downto 0)=>RSA_SEL(1 downto 0),
                 RSA_VAL_ACC=>N_11,
                 STATE(7 downto 0)=>STATUS(7 downto 0), STATE_EN=>N_4 );
   I_RSA : RSA
      Port Map ( CLK=>CLK, DATAIN(31 downto 0)=>RSA_W(31 downto 0),
                 GO=>N_12, RESET=>N_27,
                 SELDATA(1 downto 0)=>RSA_SEL(1 downto 0), VALACC=>N_11,
                 DATAOUT(31 downto 0)=>RSA_R(31 downto 0), NEXTEXP=>N_14,
                 READY=>N_13 );

end SCHEMATIC;


