--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 16-Bit 2-zu-1 MUX Test Bench
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : mux16_2to1_tb.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 17.11.98        | 17.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Testbench fuer den 16-Bit 2 zu 1 MUX
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;
  use IEEE.math_real.all;


entity MUX16_2to1_TB is     			--keine Ein-/Ausgangssignale
end MUX16_2to1_TB;



architecture SCHEMATIC of MUX16_2to1_TB is

  signal  A	:  std_logic_vector(15 downto 0);
  signal  B	:  std_logic_vector(15 downto 0);
  signal  SEL	:  std_logic := '0';
  signal  Q     :  std_logic_vector(15 downto 0);
  signal  CLK   :  std_logic := '0';

  constant TPW  :  time := 5 ns ;


  component MUX16_2to1
    port ( A   : In 	std_logic_vector (15 downto 0); 
           B   : In 	std_logic_vector (15 downto 0); 
           SEL : In	std_logic;
	   Q   : Out	std_logic_vector (15 downto 0) 
         );
  end component;


begin

  I_1 : MUX16_2to1
    Port Map (A, B, SEL, Q);

  CLK <= not CLK after TPW;       -- Taktgenerator mit Periodendauer 2*TPW


  testmuster : process


   begin

     for i in 0 to 200 loop
       A   <= conv_std_logic_vector(rand, 16);
       B   <= conv_std_logic_vector(rand, 16);
       SEL <= conv_std_logic_vector(rand, 1)(0);
       wait for TPW;
     end loop;

   end  process testmuster;

end SCHEMATIC;



configuration CFG_MUX16_2to1_TB of MUX16_2to1_TB is
  for SCHEMATIC
    for I_1 : MUX16_2to1
      use entity WORK.MUX16_2to1(BEHAVIOR);
    end for;
  end for;

end CFG_MUX16_2to1_TB;