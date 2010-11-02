--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------

-- Designers :  Dirk Allmendinger 
-- Group     :  CTRL
--------------------------------------------------------------------------

-- Design Unit Name : Serielle Ein- / Ausgabe
-- Purpose :
-- 
-- File Name : serio.vhd
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

entity SERIO is
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
end SERIO;

architecture SCHEMATIC of SERIO is

   signal   SERSEL : std_logic_vector(2 downto 0);
   signal    MODUS : std_logic_vector(1 downto 0);
   signal     N_10 : std_logic;
   signal      SEN : std_logic;
   signal SCHIEBEN : std_logic;
   signal      N_9 : std_logic;
   signal      N_7 : std_logic;
   signal      N_8 : std_logic;
   signal      N_2 : std_logic;
   signal      N_3 : std_logic;

   component FESTWERTMUX
      Port (    REIN : In    std_logic;
                 SEL : In    std_logic;
                   Q : Out   std_logic );
   end component;

   component STARTSTOPDETECT
      Port (     CLK : In    std_logic;
               RESET : In    std_logic;
               S_RES : In    std_logic;
               SCLIN : In    std_logic;
               SDAIN : In    std_logic;
               START : Out   std_logic;
                STOP : Out   std_logic );
   end component;

   component SERCTRL
      Port (     CLK : In    std_logic;
             DATAVALID : In    std_logic;
               RESET : In    std_logic;
                SCLK : In    std_logic;
               SDAIN : In    std_logic;
             SOUTEMPTY : In    std_logic;
                SSEL : In    std_logic_vector (2 downto 0);
               START : In    std_logic;
                STOP : In    std_logic;
                MODE : Out   std_logic_vector (1 downto 0);
             SADDROUT : Out   std_logic_vector (2 downto 0);
              SCLOUT : Out   std_logic;
               SEREN : Out   std_logic;
               SHIFT : Out   std_logic;
               SREAD : Out   std_logic;
             SREGINEN : Out   std_logic;
             SREGOUTEN : Out   std_logic;
             STARTSTOPRES : Out   std_logic;
              SWRITE : Out   std_logic );
   end component;

   component SERSHIFTREG
      Port (     CLK : In    std_logic;
                MODE : In    std_logic;
               RESET : In    std_logic;
               SDAIN : In    std_logic;
             SERINEN : In    std_logic;
               SHIFT : In    std_logic;
              SPARIN : In    std_logic_vector (7 downto 0);
             SREGINEN : In    std_logic;
             SREGOUTEN : In    std_logic;
              SDAOUT : Out   std_logic;
             SPAROUT : Out   std_logic_vector (7 downto 0);
                SSEL : Out   std_logic_vector (2 downto 0) );
   end component;

begin

   I_5 : FESTWERTMUX
      Port Map ( REIN=>N_9, SEL=>MODUS(1), Q=>SDAOUT );
   I_2 : STARTSTOPDETECT
      Port Map ( CLK=>CLK, RESET=>RESET, S_RES=>N_10, SCLIN=>SCLIN,
                 SDAIN=>SDAIN, START=>N_8, STOP=>N_7 );
   I_3 : SERCTRL
      Port Map ( CLK=>CLK, DATAVALID=>DATAVALID, RESET=>RESET,
                 SCLK=>SCLIN, SDAIN=>SDAIN, SOUTEMPTY=>SOUTEMPTY,
                 SSEL(2 downto 0)=>SERSEL(2 downto 0), START=>N_8,
                 STOP=>N_7, MODE(1 downto 0)=>MODUS(1 downto 0),
                 SADDROUT(2 downto 0)=>SERADDRSEL(2 downto 0),
                 SCLOUT=>SCLOUT, SEREN=>SEN, SHIFT=>SCHIEBEN,
                 SREAD=>SREAD, SREGINEN=>N_3, SREGOUTEN=>N_2,
                 STARTSTOPRES=>N_10, SWRITE=>SWRITE );
   I_4 : SERSHIFTREG
      Port Map ( CLK=>CLK, MODE=>MODUS(0), RESET=>RESET, SDAIN=>SDAIN,
                 SERINEN=>SEN, SHIFT=>SCHIEBEN,
                 SPARIN(7 downto 0)=>SERIN(7 downto 0), SREGINEN=>N_3,
                 SREGOUTEN=>N_2, SDAOUT=>N_9,
                 SPAROUT(7 downto 0)=>SEROUT(7 downto 0),
                 SSEL(2 downto 0)=>SERSEL(2 downto 0) );

end SCHEMATIC;

configuration CFG_SERIO_SCHEMATIC of SERIO is

   for SCHEMATIC
      for I_5: FESTWERTMUX
         use configuration WORK.CFG_FESTWERTMUX_BEHAVIORAL;
      end for;
      for I_2: STARTSTOPDETECT
         use configuration WORK.CFG_STARTSTOPDETECT_SCHEMATIC;
      end for;
      for I_3: SERCTRL
         use configuration WORK.CFG_SERCTRL_SCHEMATIC;
      end for;
      for I_4: SERSHIFTREG
         use configuration WORK.CFG_SERSHIFTREG_SCHEMATIC;
      end for;
   end for;

end CFG_SERIO_SCHEMATIC;
