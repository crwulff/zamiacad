--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 768-Bit Register Test Bench
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : reg768_tb.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 14.11.98        | 14.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Testbench fuer 768-Bit Register
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;
  use IEEE.math_real.all;


entity REG_768_TB is     			--keine Ein-/Ausgangssignale
end REG_768_TB;



architecture SCHEMATIC of REG_768_TB is

  signal  D_IN	:  std_logic_vector(15 downto 0);
  signal  CLK	:  std_logic := '0';
  signal  ENABLE:  std_logic := '0';
  signal  D_OUT :  std_logic_vector(767 downto 0);
  signal test_se, test_si : std_logic;

  constant TPW  :  time := 7.5 ns ;


  component REG_768
    port (DIN   : In 	std_logic_vector (15 downto 0); 
	  ENABLE: In    std_logic;
          CLK   : In	std_logic;
	  DOUT  : Out	std_logic_vector (767 downto 0);
	  test_se, test_si : in std_logic
         );
  end component;


begin

  I_1 : REG_768
    Port Map (DIN => D_IN, CLK => CLK, ENABLE => ENABLE, DOUT => D_OUT,
	      test_se => test_se, test_si => test_si);

  CLK <= not CLK after TPW;       -- Taktgenerator mit Periodendauer 2*TPW (hier 15 ns)
  

  testmuster : process

   begin
     test_se <= '0';
     test_si <= '0';
     for i in 0 to 200 loop
       D_IN   <= conv_std_logic_vector(rand, 16);
       ENABLE <= conv_std_logic_vector(rand, 1)(0);
       wait until CLK'event and CLK = '1';
     end loop;
     ENABLE <= '1';
     for i in 0 to 200 loop
       D_IN   <= (others => '0');
       wait until CLK'event and CLK = '1';
     end loop;
     for i in 0 to 200 loop
       D_IN   <= conv_std_logic_vector(rand, 16);
       wait until CLK'event and CLK = '1';
     end loop;
     ENABLE <= '0';
     test_se <= '1';
     wait until CLK'event and CLK = '1';
     test_si <= '1';
     wait until CLK'event and CLK = '1';
     wait until CLK'event and CLK = '1';
     test_si <= '0';
     wait until CLK'event and CLK = '1';
     wait for 780*2*TPW;
     wait until CLK'event and CLK = '1';
     assert false report "Test Ende!" severity failure;
     wait;
   end  process testmuster;

end SCHEMATIC;



configuration CFG_REG768_TB of REG_768_TB is
  for SCHEMATIC
    for I_1 : REG_768
      use entity WORK.REG_768_TEST(RTL);
    end for;
  end for;

end CFG_REG768_TB;




