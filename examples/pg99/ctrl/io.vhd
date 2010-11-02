--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
------------------------------------------------------------------------
------------------------------------------------------------------------
------------------------------------------------------------------------
------------------------------------------------------------------------
------------------------------------------------------------------------
------------------------------------------------------------------------
-- Designers :  Dirk Allmendinger 
-- Group     :  CTRL
--------------------------------------------------------------------
-- Design Unit Name : synchrone Ausgabe (8,16,32 Bit auf 32 bit)
-- Purpose :
-- 
-- File Name : io.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 98
--------------------------------------------------------------------
-- Date		   | Changes 
--	17.11.1998 | 	27.01.1999
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

entity IO is
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
end IO;

architecture SCHEMATIC of IO is

   signal    SERIN : std_logic_vector(7 downto 0);
   signal SERADDRSEL : std_logic_vector(2 downto 0);
   signal   SEROUT : std_logic_vector(7 downto 0);
   signal      AFF : std_logic_vector(2 downto 0);
   signal      N_9 : std_logic;
   signal      N_4 : std_logic;
   signal      N_5 : std_logic;
   signal      N_6 : std_logic;
   signal      N_8 : std_logic;
   signal      N_3 : std_logic;
   signal      N_1 : std_logic;
   signal      N_2 : std_logic;
   signal DATAVALID_DUMMY : std_logic;

   component IO_EFF
      Port (     CLK : In    std_logic;
                 EIN : In    std_logic_vector (2 downto 0);
                EIN1 : In    std_logic;
                EIN2 : In    std_logic;
                EIN3 : In    std_logic;
                EIN4 : In    std_logic;
                EIN5 : In    std_logic;
               RESET : In    std_logic;
                 AUS : Out   std_logic_vector (2 downto 0);
                AUS1 : Out   std_logic;
                AUS2 : Out   std_logic;
                AUS3 : Out   std_logic;
                AUS4 : Out   std_logic;
                AUS5 : Out   std_logic );
   end component;

   component SERIO
      Port (     CLK : In    std_logic;
             DATAVALID : In    std_logic;
               RESET : In    std_logic;
               SCLIN : In    std_logic;
               SDAIN : In    std_logic;
               SERIN : In    std_logic_vector (7 downto 0);
             SOUTEMPTY : In    std_logic;
              SCLOUT : Out   std_logic;
              SDAOUT : Out   std_logic;
             SERADDRSEL : Out   std_logic_vector (2 downto 0);
              SEROUT : Out   std_logic_vector (7 downto 0);
               SREAD : Out   std_logic;
              SWRITE : Out   std_logic );
   end component;

   component PARIO
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
             SERADDRSEL : In    std_logic_vector (2 downto 0);
               SERIN : In    std_logic_vector (7 downto 0);
               SREAD : In    std_logic;
              STATUS : In    std_logic_vector (7 downto 0);
             STATUSINEN : In    std_logic;
              SWRITE : In    std_logic;
                DATA : InOut std_logic_vector (31 downto 0);
                CTRL : Out   std_logic_vector (7 downto 0);
             CTRLCHANGE : Out   std_logic;
             DATAVALID : Out   std_logic;
                DOUT : Out   std_logic_vector (31 downto 0);
             DOUTEMPTY : Out   std_logic;
               OE_IO : Out   std_logic;
              SEROUT : Out   std_logic_vector (7 downto 0);
             SOUTEMPTY : Out   std_logic );
   end component;

begin

   DATAVALID <= DATAVALID_DUMMY;

   I_3 : IO_EFF
      Port Map ( CLK=>CLK, EIN(2 downto 0)=>ADDR(2 downto 0), EIN1=>OE,
                 EIN2=>CE, EIN3=>RW, EIN4=>SCLIN, EIN5=>SDAIN,
                 RESET=>RESET, AUS(2 downto 0)=>AFF(2 downto 0),
                 AUS1=>N_6, AUS2=>N_9, AUS3=>N_8, AUS4=>N_4, AUS5=>N_5 );
   I_2 : SERIO
      Port Map ( CLK=>CLK, DATAVALID=>DATAVALID_DUMMY, RESET=>RESET,
                 SCLIN=>N_4, SDAIN=>N_5,
                 SERIN(7 downto 0)=>SEROUT(7 downto 0), SOUTEMPTY=>N_3,
                 SCLOUT=>SCLOUT, SDAOUT=>SDAOUT,
                 SERADDRSEL(2 downto 0)=>SERADDRSEL(2 downto 0),
                 SEROUT(7 downto 0)=>SERIN(7 downto 0), SREAD=>N_1,
                 SWRITE=>N_2 );
   I_1 : PARIO
      Port Map ( ADDR(2 downto 0)=>AFF(2 downto 0), CE=>N_9, CLK=>CLK,
                 DIN(31 downto 0)=>DIN(31 downto 0), DREGINEN=>DREGINEN,
                 DREGOUTEN=>DREGOUTEN,
                 MODESEL(1 downto 0)=>MODESEL(1 downto 0), OE=>N_6,
                 RESET=>RESET, RW=>N_8,
                 SERADDRSEL(2 downto 0)=>SERADDRSEL(2 downto 0),
                 SERIN(7 downto 0)=>SERIN(7 downto 0), SREAD=>N_1,
                 STATUS(7 downto 0)=>STATUS(7 downto 0),
                 STATUSINEN=>STATUSINEN, SWRITE=>N_2,
                 DATA(31 downto 0)=>DATA(31 downto 0),
                 CTRL(7 downto 0)=>CTRL(7 downto 0),
                 CTRLCHANGE=>CTRLCHANGE, DATAVALID=>DATAVALID_DUMMY,
                 DOUT(31 downto 0)=>DOUT(31 downto 0),
                 DOUTEMPTY=>DOUTEMPTY, OE_IO=>OE_IO,
                 SEROUT(7 downto 0)=>SEROUT(7 downto 0), SOUTEMPTY=>N_3 );

end SCHEMATIC;

configuration CFG_IO_SCHEMATIC of IO is

   for SCHEMATIC
      for I_3: IO_EFF
         use configuration WORK.CFG_IO_EFF_BEHAVIORAL;
      end for;
      for I_2: SERIO
         use configuration WORK.CFG_SERIO_SCHEMATIC;
      end for;
      for I_1: PARIO
         use configuration WORK.CFG_PARIO_SCHEMATIC;
      end for;
   end for;

end CFG_IO_SCHEMATIC;
