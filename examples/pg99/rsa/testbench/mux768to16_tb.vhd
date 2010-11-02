--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 16-Bit 48-zu-1 MUX Test Bench
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : mux768to16_tb.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 17.11.98        | 17.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Testbench fuer den 16-Bit 48 zu 1 MUX
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;
  use IEEE.math_real.all;


entity MUX768to16_TB is     			--keine Ein-/Ausgangssignale
end MUX768to16_TB;



architecture SCHEMATIC of MUX768to16_TB is

  signal  A	:  std_logic_vector(767 downto 0);
  signal  SEL	:  std_logic_vector(5 downto 0);
  signal  Q     :  std_logic_vector(15 downto 0);
  signal  CLK   :  std_logic := '0';

  constant TPW  :  time := 5 ns ;


  component MUX768to16
    port ( A   : In 	std_logic_vector (767 downto 0); 
           SEL : In	std_logic_vector (5 downto 0);
	   Q   : Out	std_logic_vector (15 downto 0) 
         );
  end component;


begin

  I_1 : MUX768to16
    Port Map (A, SEL, Q);

  CLK <= not CLK after TPW;       -- Taktgenerator mit Periodendauer 2*TPW


  testmuster : process


   begin

     for n in 0 to 47 loop
       A(31 downto 0)  <= conv_std_logic_vector(rand, 32);
       A(767 downto 32) <= A(735 downto 0);
       wait for TPW;
     end loop;

     for i in 0 to 49 loop
       SEL <= conv_std_logic_vector(i, 6);
       wait for TPW;
     end loop;

   end  process testmuster;

end SCHEMATIC;



configuration CFG_MUX768to16_TB of MUX768to16_TB is
  for SCHEMATIC
    for I_1 : MUX768to16
      use entity WORK.MUX768to16(BEHAVIOR);
    end for;
  end for;

end CFG_MUX768to16_TB;