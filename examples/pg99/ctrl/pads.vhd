-- VHDL Model Created from SGE Schematic pads.sch -- Jan 19 12:39:50 1999

--------------------------------------------------------------------------
-------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : PADS
-- Purpose : Die Tristatepads fuer unseren chip.
-- 
-- File Name : pads.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date  28.12.98  | Changes  Created
--                 | 
--                 |
-----------------------------------------------------------------------

library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity PADS is
      Port (       A : In    std_logic_vector (2 downto 0);
             CARDCHANGE : In    std_logic;
                  CE : In    std_logic;
                 CLK : In    std_logic;
             IO_MODE : In    std_logic_vector (1 downto 0);
                  OE : In    std_logic;
                 RES : In    std_logic;
                  WR : In    std_logic;
                   D : InOut std_logic_vector (31 downto 0);
                 SCL : InOut std_logic;
                 SDA : InOut std_logic;
                BUSY : Out   std_logic;
                 INT : Out   std_logic );
end PADS;

architecture SCHEMATIC of PADS is

   signal    D_OUT : std_logic_vector(31 downto 0);
   signal ENABLE_TEST : std_logic;
   signal      N_3 : std_logic;
   signal      N_4 : std_logic;
   signal      N_5 : std_logic;
   signal      N_6 : std_logic;
   signal      N_2 : std_logic;

   component I2C_BUF
      Port (       O : In    std_logic;
                   B : InOut std_logic;
                   I : Out   std_logic );
   end component;

   component BIDI_BUF
      Port (      EN : In    std_logic;
                   I : In    std_logic_vector (31 downto 0);
                   O : InOut std_logic_vector (31 downto 0) );
   end component;

   component CHIP
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
   end component;

begin

   I_3 : I2C_BUF
      Port Map ( O=>N_3, B=>SCL, I=>N_4 );
   I_4 : I2C_BUF
      Port Map ( O=>N_5, B=>SDA, I=>N_6 );
   I_1 : BIDI_BUF
      Port Map ( EN=>N_2, I(31 downto 0)=>D_OUT(31 downto 0),
                 O(31 downto 0)=>D(31 downto 0) );
   I_2 : CHIP
      Port Map ( A(2 downto 0)=>A(2 downto 0), CARDCHANGE=>CARDCHANGE,
                 CE=>CE, CLK=>CLK, D_IN(31 downto 0)=>D(31 downto 0),
                 IO_MODE(1 downto 0)=>IO_MODE(1 downto 0), OE=>OE,
                 RES=>RES, SCL_IN=>N_4, SDA_IN=>N_6, WR=>WR, BUSY=>BUSY,
                 D_OUT(31 downto 0)=>D_OUT(31 downto 0), D_OUT_EN=>N_2,
                 ENABLE_TEST=>ENABLE_TEST, INT=>INT, SCL_OUT=>N_3,
                 SDA_OUT=>N_5 );

end SCHEMATIC;
