--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 16-Bit Register Test Bench
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : reg16_tb.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 17.11.98        | 17.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Testbench fuer das 16-Bit Register
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;
  use IEEE.math_real.all;


entity REG_16_TB is     			--keine Ein-/Ausgangssignale
end REG_16_TB;



architecture SCHEMATIC of REG_16_TB is

  signal  D_IN	:  std_logic_vector(15 downto 0);
  signal  CLK	:  std_logic := '0';
  signal  ENABLE:  std_logic := '0';
  signal  D_OUT :  std_logic_vector(15 downto 0);

  constant TPW  :  time := 5 ns ;


  component REG_16
    port (DIN   : In 	std_logic_vector (15 downto 0); 
	  ENABLE: In    std_logic;
          CLK   : In	std_logic;
	  DOUT  : Out	std_logic_vector (15 downto 0) 
         );
  end component;


begin

  I_1 : REG_16
    Port Map (DIN => D_IN, CLK => CLK, ENABLE => ENABLE, DOUT => D_OUT);

  CLK <= not CLK after TPW;       -- Taktgenerator mit Periodendauer 2*TPW (hier 10 ns)


  testmuster : process


   begin

     for i in 0 to 200 loop
       D_IN   <= conv_std_logic_vector(rand, 16);
       ENABLE <= conv_std_logic_vector(rand, 1)(0);
       wait for 2.5*TPW;
     end loop;

   end  process testmuster;

end SCHEMATIC;



configuration CFG_REG16_TB of REG_16_TB is
  for SCHEMATIC
    for I_1 : REG_16
      use entity WORK.REG_16(BEHAVIOR);
    end for;
  end for;

end CFG_REG16_TB;