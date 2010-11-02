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
-- Purpose : Adapter um testbench passend zu machen.
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

library IEEE;
use IEEE.std_logic_1164.all;

entity OUTHER_CHIP is
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
             SDA_OUT : Out   std_logic;
	     TEST_SE : In    std_logic;
	     TEST_SI : in    std_logic;
	     TEST_SO : Out   std_logic );
end OUTHER_CHIP;


architecture BEHAVPADS of OUTHER_CHIP is

   component PADS
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
                 INT : Out   std_logic;
	 ENABLE_TEST : Out   std_logic );
   end component;

   signal SDA : std_logic;
   signal SCL  : std_logic;
   signal D : std_logic_vector(31 downto 0);
       
begin

   I_1 : PADS
      Port Map ( A(2 downto 0)=>A(2 downto 0),
		 CARDCHANGE=>CARDCHANGE,
                 CE=>CE,
		 CLK=>CLK,
                 IO_MODE(1 downto 0)=>IO_MODE(1 downto 0),
		 OE=>OE,
                 RES=>RES,
		 D=>D,
		 SDA=>SDA,
		 SCL=>SCL,
		 WR=>WR,
		 BUSY=>BUSY,
		 INT=>INT);

   SDA_OUT <= TO_X01(SDA);
   SCL_OUT <= TO_X01(SCL);
   D_OUT   <= D;
   SDA <= '0' when SDA_IN ='0' else 'H';
   SCL <= '0' when SCL_IN = '0' else 'H';
   D <= D_IN when WR='0' else (others => 'Z');

end BEHAVPADS;
