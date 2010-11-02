--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 32-Bit Schiebe Register Test Bench
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : reg32_tb.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 14.11.98        | 17.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Testbench fuer das 32-Bit Schiebe Register
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;
  use IEEE.math_real.all;


entity REG_32_TB is     			--keine Ein-/Ausgangssignale
end REG_32_TB;



architecture SCHEMATIC of REG_32_TB is

  signal  D_IN	:  std_logic_vector(31 downto 0);
  signal  CLK	:  std_logic := '0';
  signal  SHIFT :  std_logic := '0';
  signal  ENABLE:  std_logic := '0';
  signal  D_OUT :  std_logic;

  constant TPW  :  time := 2.5 ns ;


  component REG_32
    port (DIN   : In 	std_logic_vector (31 downto 0); 
	  SHIFT : In	std_logic;
	  ENABLE: In    std_logic;
          CLK   : In	std_logic;
	  DOUT  : Out	std_logic 
         );
  end component;


begin

  I_1 : REG_32
    Port Map (DIN => D_IN, CLK => CLK, SHIFT => SHIFT, ENABLE => ENABLE, DOUT => D_OUT);

  CLK <= not CLK after TPW;       -- Taktgenerator mit Periodendauer 2*TPW (hier 5 ns)


  testmuster : process


   begin

     D_IN   <= "01101101101101101101101101101101";
     ENABLE <= '1';
     SHIFT <= '0';
     wait for 2.5*TPW;
     assert false
     report "D_IN wurde geladen"
     severity note;
     

     ENABLE <= '0';
     SHIFT <= '1';
     D_IN  <= conv_std_logic_vector(0, 32);
     wait for 2.5*TPW;
     assert false
     report "ENABLE ist inaktiv, Shift aktiv"
     severity note;

     ENABLE <= '1';
     assert false
     report "Shiftmodus aktiv"
     severity note;

     wait for 128*TPW;
     assert d_out='0'
     report "Register wurde nicht vollstaendig durchgeschoben"
     severity error;

     wait for TPW;
     assert d_out='0'
     report "Register wurde nicht vollstaendig durchgeschoben"
     severity error;

     wait for TPW;
     assert d_out='0'
     report "Register wurde nicht vollstaendig durchgeschoben"
     severity error;

     wait for TPW;
     assert d_out='0'
     report "Register wurde nicht vollstaendig durchgeschoben"
     severity error;
              

   end  process testmuster;

end SCHEMATIC;



configuration CFG_REG32_TB of REG_32_TB is
  for SCHEMATIC
    for I_1 : REG_32
      use entity WORK.REG_32(BEHAVIOR);
    end for;
  end for;

end CFG_REG32_TB;