-- VHDL Model Created from SGE Schematic mainctrl.sch -- Jan  4 19:44:42 1999

--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : MAINCTRL
-- Purpose : Zentrale Steuerung des Kryptochip
-- 
-- File Name : mainctrl.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date  18.11.98  | Changes
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Strukturelle Beschreibung des des Datenpfads
--  um Protocol zu realisieren und KEYs zu speichern
--------------------------------------------------------------------------

library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity MAINCTRL is
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
end MAINCTRL;

architecture SCHEMATIC of MAINCTRL is

   signal  RAM_ADR : std_logic_vector(7 downto 0);
   signal WRITE_REG : std_logic_vector(2 downto 0);
   signal READ_REG : std_logic_vector(2 downto 0);
   signal    IN_EN : std_logic_vector(7 downto 0);
   signal   OUT_EN : std_logic_vector(7 downto 0);
   signal       HI : std_logic_vector(31 downto 0);
   signal       LO : std_logic_vector(31 downto 0);
   signal      N_3 : std_logic;
   signal DES_IN_EN : std_logic;
   signal      N_1 : std_logic;
   signal      N_2 : std_logic;
   signal   EQ_REG : std_logic;
   signal    EQUAL : std_logic;
   signal  RAM_SEL : std_logic;
   signal DES_BUFFER_FREE_DUMMY : std_logic;
   signal DES_DATA_READY_DUMMY : std_logic;

   component SYNC_FF
      Port (     CLK : In    std_logic;
               RESET : In    std_logic;
               SET_A : In    std_logic;
               SET_B : In    std_logic;
                   Q : Out   std_logic );
   end component;

   component REG_CTRL
      Port (     CLK : In    std_logic;
                   J : In    std_logic;
                   K : In    std_logic;
               RESET : In    std_logic;
                   O : Out   std_logic );
   end component;

   component NAME_ROM
      Port (       A : In    std_logic_vector (7 downto 0);
                  CS : In    std_logic;
                 OEN : In    std_logic;
                   Q : Out   std_logic_vector (31 downto 0) );
   end component;

   component FSM
      Port ( CARDCHANGE : In    std_logic;
                 CLK : In    std_logic;
                CTRL : In    std_logic_vector (7 downto 0);
             CTRL_CHANGE : In    std_logic;
             DATA_VALID : In    std_logic;
             DES_BUFFER_FREE : In    std_logic;
             DES_DATA_READY : In    std_logic;
             DES_ERR : In    std_logic;
             DES_PARITY : In    std_logic;
             DOUT_EMPTY : In    std_logic;
               EQUAL : In    std_logic;
               RESET : In    std_logic;
             RSA_NEXTEXP : In    std_logic;
             RSA_READY : In    std_logic;
                BUSY : Out   std_logic;
             DES_BUFFER_FREE_SET : Out   std_logic;
             DES_DATA_IS_KEY : Out   std_logic;
             DES_DATA_READY_SET : Out   std_logic;
             DES_IN_EN : Out   std_logic;
             DES_MODE : Out   std_logic_vector (4 downto 0);
             DEST_REG : Out   std_logic_vector (2 downto 0);
             ENABLE_TEST : Out   std_logic;
              EQ_REG : Out   std_logic;
                 INT : Out   std_logic;
             RAM_ADR : Out   std_logic_vector (7 downto 0);
             RAM_SEL : Out   std_logic;
              RSA_GO : Out   std_logic;
             RSA_SEL : Out   std_logic_vector (1 downto 0);
             RSA_VAL_ACC : Out   std_logic;
             SOURCE_REG : Out   std_logic_vector (2 downto 0);
             STATE_EN : Out   std_logic;
              STATUS : Out   std_logic_vector (7 downto 0) );
   end component;

   component RIP
      Port (       A : In    std_logic;
                   B : Out   std_logic );
   end component;

   component DEMUX
      Port (       A : In    std_logic_vector (2 downto 0);
                 SEL : Out   std_logic_vector (7 downto 0) );
   end component;

   component SPS2_256X32M4
      Port (       A : In    std_logic_vector (7 downto 0);
                  CK : In    std_logic;
                 CSN : In    std_logic;
                   D : In    std_logic_vector (31 downto 0);
                 OEN : In    std_logic;
                 WEN : In    std_logic;
                   Q : Out   std_logic_vector (31 downto 0) );
   end component;

   component D_REG
      Port (     CLK : In    std_logic;
                  EN : In    std_logic;
                   I : In    std_logic_vector (31 downto 0);
               RESET : In    std_logic;
                   O : Out   std_logic_vector (31 downto 0) );
   end component;

   component BIDI_BUF
      Port (      EN : In    std_logic;
                   I : In    std_logic_vector (31 downto 0);
                   O : InOut std_logic_vector (31 downto 0) );
   end component;

   component LFSR
      Port (     CLK : In    std_logic;
             RD_CONST : In    std_logic;
               RD_HI : In    std_logic;
               RD_LO : In    std_logic;
               WR_HI : In    std_logic;
               WR_LO : In    std_logic;
                DATA : InOut std_logic_vector (31 downto 0) );
   end component;

   component EQ
      Port (     CLK : In    std_logic;
                  EN : In    std_logic;
                   I : In    std_logic_vector (31 downto 0);
               RESET : In    std_logic;
                  EQ : Out   std_logic );
   end component;

   for all: EQ use entity WORK.EQ(BEHAVIORAL);

begin

   DES_BUFFER_FREE <= DES_BUFFER_FREE_DUMMY;
   DES_DATA_READY <= DES_DATA_READY_DUMMY;

   I_BUSY : SYNC_FF
      Port Map ( CLK=>CLK, RESET=>RESET, SET_A=>N_3, SET_B=>DATA_VALID,
                 Q=>BUSY );
   I_DES_IN : REG_CTRL
      Port Map ( CLK=>CLK, J=>N_1, K=>DES_READY, RESET=>RESET,
                 O=>DES_BUFFER_FREE_DUMMY );
   I_DES_OUT : REG_CTRL
      Port Map ( CLK=>CLK, J=>N_2, K=>DES_ACK, RESET=>RESET,
                 O=>DES_DATA_READY_DUMMY );
   I_ROM : NAME_ROM
      Port Map ( A(7 downto 0)=>RAM_ADR(7 downto 0), CS=>RAM_SEL,
                 OEN=>OUT_EN(2), Q(31 downto 0)=>D(31 downto 0) );
   I_FSM : FSM
      Port Map ( CARDCHANGE=>CARDCHANGE, CLK=>CLK,
                 CTRL(7 downto 0)=>CTRL(7 downto 0),
                 CTRL_CHANGE=>CTRL_CHANGE, DATA_VALID=>DATA_VALID,
                 DES_BUFFER_FREE=>DES_BUFFER_FREE_DUMMY,
                 DES_DATA_READY=>DES_DATA_READY_DUMMY, DES_ERR=>DES_ERR,
                 DES_PARITY=>DES_PARITY, DOUT_EMPTY=>DOUT_EMPTY,
                 EQUAL=>EQUAL, RESET=>RESET, RSA_NEXTEXP=>RSA_NEXTEXP,
                 RSA_READY=>RSA_READY, BUSY=>N_3,
                 DES_BUFFER_FREE_SET=>N_1,
                 DES_DATA_IS_KEY=>DES_DATA_IS_KEY,
                 DES_DATA_READY_SET=>N_2, DES_IN_EN=>DES_IN_EN,
                 DES_MODE(4 downto 0)=>DES_MODE(4 downto 0),
                 DEST_REG(2 downto 0)=>WRITE_REG(2 downto 0),
                 ENABLE_TEST=>ENABLE_TEST, EQ_REG=>EQ_REG, INT=>INT,
                 RAM_ADR(7 downto 0)=>RAM_ADR(7 downto 0),
                 RAM_SEL=>RAM_SEL, RSA_GO=>RSA_GO,
                 RSA_SEL(1 downto 0)=>RSA_SEL(1 downto 0),
                 RSA_VAL_ACC=>RSA_VAL_ACC,
                 SOURCE_REG(2 downto 0)=>READ_REG(2 downto 0),
                 STATE_EN=>STATE_EN,
                 STATUS(7 downto 0)=>STATE(7 downto 0) );
   I_RIP2 : RIP
      Port Map ( A=>IN_EN(1), B=>DREGOUTEN );
   I_RIP1 : RIP
      Port Map ( A=>OUT_EN(1), B=>DREGINEN );
   I_DEMUX2 : DEMUX
      Port Map ( A(2 downto 0)=>READ_REG(2 downto 0),
                 SEL(7 downto 0)=>OUT_EN(7 downto 0) );
   I_DEMUX1 : DEMUX
      Port Map ( A(2 downto 0)=>WRITE_REG(2 downto 0),
                 SEL(7 downto 0)=>IN_EN(7 downto 0) );
   I_RAM : SPS2_256X32M4
      Port Map ( A(7 downto 0)=>RAM_ADR(7 downto 0), CK=>CLK,
                 CSN=>RAM_SEL, D(31 downto 0)=>D(31 downto 0),
                 OEN=>OUT_EN(2), WEN=>IN_EN(2),
                 Q(31 downto 0)=>D(31 downto 0) );
   I_REG3 : D_REG
      Port Map ( CLK=>CLK, EN=>IN_EN(4), I(31 downto 0)=>D(31 downto 0),
                 RESET=>RESET, O(31 downto 0)=>DES_OUT(63 downto 32) );
   I_REG4 : D_REG
      Port Map ( CLK=>CLK, EN=>IN_EN(3), I(31 downto 0)=>D(31 downto 0),
                 RESET=>RESET, O(31 downto 0)=>DES_OUT(31 downto 0) );
   I_REG5 : D_REG
      Port Map ( CLK=>CLK, EN=>IN_EN(5), I(31 downto 0)=>D(31 downto 0),
                 RESET=>RESET, O(31 downto 0)=>RSA_OUT(31 downto 0) );
   I_REG1 : D_REG
      Port Map ( CLK=>CLK, EN=>DES_IN_EN,
                 I(31 downto 0)=>DES_IN(63 downto 32), RESET=>RESET,
                 O(31 downto 0)=>HI(31 downto 0) );
   I_REG2 : D_REG
      Port Map ( CLK=>CLK, EN=>DES_IN_EN,
                 I(31 downto 0)=>DES_IN(31 downto 0), RESET=>RESET,
                 O(31 downto 0)=>LO(31 downto 0) );
   I_BUFF3 : BIDI_BUF
      Port Map ( EN=>OUT_EN(5), I(31 downto 0)=>RSA_IN(31 downto 0),
                 O(31 downto 0)=>D(31 downto 0) );
   I_BUFF2 : BIDI_BUF
      Port Map ( EN=>OUT_EN(3), I(31 downto 0)=>LO(31 downto 0),
                 O(31 downto 0)=>D(31 downto 0) );
   I_BUFF1 : BIDI_BUF
      Port Map ( EN=>OUT_EN(4), I(31 downto 0)=>HI(31 downto 0),
                 O(31 downto 0)=>D(31 downto 0) );
   I_LFSR : LFSR
      Port Map ( CLK=>CLK, RD_CONST=>OUT_EN(0), RD_HI=>OUT_EN(7),
                 RD_LO=>OUT_EN(6), WR_HI=>IN_EN(7), WR_LO=>IN_EN(6),
                 DATA(31 downto 0)=>D(31 downto 0) );
   I_EQ : EQ
      Port Map ( CLK=>CLK, EN=>EQ_REG, I(31 downto 0)=>D(31 downto 0),
                 RESET=>RESET, EQ=>EQUAL );

end SCHEMATIC;
