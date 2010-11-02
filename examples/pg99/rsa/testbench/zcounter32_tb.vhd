--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : Z32_TB 
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  zcounter32_tb.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 11.01.99        | 11.01.99
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Die Testbench zu dem Z32-Zaehler
--------------------------------------------------------------------------



library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;
  use IEEE.math_real.all;


entity Z32_TB is                            --keine Ein-/Ausgangssignale
end Z32_TB;



architecture SCHEMATIC of Z32_TB is

  signal  RESET		:  std_logic;
  signal  CLK		:  std_logic := '0';
  signal  READY_BEHAV	: std_logic;
  signal  READY_RTL	: std_logic;
  signal  INC		: std_logic := '0';

  constant TPW  :  time := 5 ns ;


  component Z32
    port ( CLK, RESET	: In     std_logic;
	   INC		: in     std_logic;
	   READY	: out	 std_logic
         );
  end component;

  for I_1: Z32 use entity WORK.Z32(BEHAVIOUR);
  for I_2: Z32 use entity WORK.Z32(RTL);

begin

  I_1 : Z32
    Port Map (CLK, RESET, INC, READY_BEHAV);
  I_2 : Z32
    Port Map (CLK, RESET, INC, READY_RTL);

  CLK <= not CLK after TPW;       -- Taktgenerator mit Periodendauer 2*TPW


  testmuster : process
   begin
       RESET <= '1';
       wait until clk'event and clk ='1';
       RESET <= '0';
       for i in 0 to 100 loop
	   wait until clk'event and clk ='1';
	   assert READY_RTL = READY_BEHAV report "Ready-Ausgang stimmt nicht ueberein!" severity error;
       end loop;  -- i
       for i in 0 to 100 loop
	   inc <= '1';
	   wait until clk'event and clk ='1';
	   assert READY_RTL = READY_BEHAV report "Ready-Ausgang stimmt nicht ueberein!" severity error;
       end loop;  -- i       
       RESET <= '1';
       wait until clk'event and clk ='1';
       assert READY_RTL = READY_BEHAV report "Ready-Ausgang stimmt nicht ueberein!" severity error;
       RESET <= '0';
       for i in 0 to 2000 loop
	   inc <='1';
	   wait until clk'event and clk ='1';
	   assert READY_RTL = READY_BEHAV report "Ready-Ausgang stimmt nicht ueberein!" severity error;
	   wait until clk'event and clk ='1';
	   inc <= '0';
	   wait until clk'event and clk ='1';
	   assert READY_RTL = READY_BEHAV report "Ready-Ausgang stimmt nicht ueberein!" severity error;
       end loop;  -- i      
       assert FALSE report "Test-Ende" severity failure;
       wait;  				-- forever
   end  process testmuster;
 
   
end SCHEMATIC;
