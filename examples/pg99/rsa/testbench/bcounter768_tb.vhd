--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : B768_TB 
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  bcounter768.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 14.12.98        | 14.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Die Testbench zu dem 768 Binaer-Zaehler
--------------------------------------------------------------------------



library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;
  use IEEE.math_real.all;


entity B768_TB is                            --keine Ein-/Ausgangssignale
end B768_TB;



architecture SCHEMATIC of B768_TB is

  signal  D_OUT_BEHAV :  std_logic_vector(9 downto 0);
  signal  D_OUT_RTL   :  std_logic_vector(9 downto 0);
  signal  RESET	      :  std_logic;
  signal  CLK         :  std_logic := '0';
  signal  READY_BEHAV : std_logic;
  signal  READY_RTL : std_logic;

  constant TPW  :  time := 5 ns ;


  component B768
    port ( CLK, RESET	: In     std_logic;
	   READY	: out	 std_logic;
           D_OUT	: Out    std_logic_vector (9 downto 0)
         );
  end component;

  for I_1: B768 use entity WORK.B768(RTL);
  for I_2: B768 use entity WORK.B768(BEHAVIOUR);

begin

  I_1 : B768
    Port Map (CLK, RESET, READY_RTL, D_OUT_RTL);
  I_2 : B768
    Port Map (CLK, RESET, READY_BEHAV, D_OUT_BEHAV);  

  CLK <= not CLK after TPW;       -- Taktgenerator mit Periodendauer 2*TPW


  testmuster : process
   begin
       RESET <= '1';
       wait until clk'event and clk ='1';
       RESET <= '0';	-- aktiv high RESET
       for i in 0 to 770 loop
	   wait until clk'event and clk ='1';
	   assert D_OUT_RTL = D_OUT_BEHAV report "Zaehler-Ausgang stimmt nicht ueberein!" severity error;
	   assert READY_RTL = READY_BEHAV report "Ready-Ausgang stimmt nicht ueberein!" severity error; 
       end loop;  -- i
       RESET <= '1';
       wait until clk'event and clk ='1';
       RESET <= '0';
       wait until clk'event and clk ='1';
       assert D_OUT_RTL = D_OUT_BEHAV report "Zaehler-Ausgang stimmt nicht ueberein!" severity error;
       assert READY_RTL = READY_BEHAV report "Ready-Ausgang stimmt nicht ueberein!" severity error; 
       for i in 0 to 2000 loop
	   wait until clk'event and clk ='1';
	   assert D_OUT_RTL = D_OUT_BEHAV report "Zaehler-Ausgang stimmt nicht ueberein!" severity error;
	   assert READY_RTL = READY_BEHAV report "Ready-Ausgang stimmt nicht ueberein!" severity error; 
       end loop;  -- i      
       assert FALSE report "Test-Ende" severity failure;
       wait;  				-- forever
   end  process testmuster;
 
   
end SCHEMATIC;
