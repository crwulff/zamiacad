--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------

-- Designers :  Dirk Allmendinger 
-- Group     :  CTRL
--------------------------------------------------------------------------

-- Design Unit Name : Parallele Ein- / Ausgabe
-- Purpose :
-- 
-- File Name : pario.vhd
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

entity PARIO is
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
end PARIO;

architecture SCHEMATIC of PARIO is

   signal    REGEN : std_logic_vector(3 downto 0);
   signal  ADRESSE : std_logic_vector(2 downto 0);
   signal      N_1 : std_logic;

   component PARCTRL
      Port (    ADDR : In    std_logic_vector (2 downto 0);
                  CE : In    std_logic;
                 CLK : In    std_logic;
             DREGINEN : In    std_logic;
             DREGOUTEN : In    std_logic;
             MODESEL : In    std_logic_vector (1 downto 0);
                  OE : In    std_logic;
               RESET : In    std_logic;
                  RW : In    std_logic;
                SADR : In    std_logic_vector (2 downto 0);
               SREAD : In    std_logic;
              SWRITE : In    std_logic;
                 ADR : Out   std_logic_vector (2 downto 0);
             CTRLCHANGE : Out   std_logic;
             CTRLINEN : Out   std_logic;
             DATAVALID : Out   std_logic;
              DOUTEN : Out   std_logic;
              OE_OUT : Out   std_logic;
             REGINEN : Out   std_logic_vector (3 downto 0);
              SOUTEN : Out   std_logic );
   end component;

   component PAREIN
      Port (     CLK : In    std_logic;
             CTRLINEN : In    std_logic;
             MODESEL : In    std_logic_vector (1 downto 0);
                  OE : In    std_logic;
               PARIN : In    std_logic_vector (31 downto 0);
               REGEN : In    std_logic_vector (3 downto 0);
               RESET : In    std_logic;
               SERIN : In    std_logic_vector (7 downto 0);
                   Y : InOut std_logic_vector (31 downto 0);
             CTRLOUT : Out   std_logic_vector (7 downto 0) );
   end component;

   component PARAUS
      Port (     ADR : In    std_logic_vector (2 downto 0);
                 CLK : In    std_logic;
                DATA : In    std_logic_vector (31 downto 0);
                MODE : In    std_logic_vector (1 downto 0);
             REGINEN : In    std_logic;
               RESET : In    std_logic;
              STATUS : In    std_logic_vector (7 downto 0);
             STATUSINEN : In    std_logic;
              PAROUT : Out   std_logic_vector (31 downto 0);
              SEROUT : Out   std_logic_vector (7 downto 0) );
   end component;

begin

   I_3 : PARCTRL
      Port Map ( ADDR(2 downto 0)=>ADDR(2 downto 0), CE=>CE, CLK=>CLK,
                 DREGINEN=>DREGINEN, DREGOUTEN=>DREGOUTEN,
                 MODESEL(1 downto 0)=>MODESEL(1 downto 0), OE=>OE,
                 RESET=>RESET, RW=>RW,
                 SADR(2 downto 0)=>SERADDRSEL(2 downto 0), SREAD=>SREAD,
                 SWRITE=>SWRITE, ADR(2 downto 0)=>ADRESSE(2 downto 0),
                 CTRLCHANGE=>CTRLCHANGE, CTRLINEN=>N_1,
                 DATAVALID=>DATAVALID, DOUTEN=>DOUTEMPTY, OE_OUT=>OE_IO,
                 REGINEN(3 downto 0)=>REGEN(3 downto 0),
                 SOUTEN=>SOUTEMPTY );
   I_1 : PAREIN
      Port Map ( CLK=>CLK, CTRLINEN=>N_1,
                 MODESEL(1 downto 0)=>MODESEL(1 downto 0), OE=>DREGINEN,
                 PARIN(31 downto 0)=>DIN(31 downto 0),
                 REGEN(3 downto 0)=>REGEN(3 downto 0), RESET=>RESET,
                 SERIN(7 downto 0)=>SERIN(7 downto 0),
                 Y(31 downto 0)=>DATA(31 downto 0),
                 CTRLOUT(7 downto 0)=>CTRL(7 downto 0) );
   I_2 : PARAUS
      Port Map ( ADR(2 downto 0)=>ADRESSE(2 downto 0), CLK=>CLK,
                 DATA(31 downto 0)=>DATA(31 downto 0),
                 MODE(1 downto 0)=>MODESEL(1 downto 0),
                 REGINEN=>DREGOUTEN, RESET=>RESET,
                 STATUS(7 downto 0)=>STATUS(7 downto 0),
                 STATUSINEN=>STATUSINEN,
                 PAROUT(31 downto 0)=>DOUT(31 downto 0),
                 SEROUT(7 downto 0)=>SEROUT(7 downto 0) );

end SCHEMATIC;

configuration CFG_PARIO_SCHEMATIC of PARIO is

   for SCHEMATIC
      for I_3: PARCTRL
         use configuration WORK.CFG_PARCTRL_SCHEMATIC;
      end for;
      for I_1: PAREIN
         use configuration WORK.CFG_PAREIN_SCHEMATIC;
      end for;
      for I_2: PARAUS
         use configuration WORK.CFG_PARAUS_SCHEMATIC;
      end for;
   end for;

end CFG_PARIO_SCHEMATIC;
