--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : B48 
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  bcounter48.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 14.12.98        | 14.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Es wird ein binaer Zaehler implementiert. der Zaehler zaehlt von 0 bis 48
--  (49 Zyklen). Sobald die 49 Zyklen abgelaufen sind wird die READY Ltg auf
--  Eins gezogen. Mit RESET wird er wieder zurueckgesetzt.
--------------------------------------------------------------------------



library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;
  use IEEE.math_real.all;


entity B48_TB is                            --keine Ein-/Ausgangssignale
end B48_TB;



architecture SCHEMATIC of B48_TB is

  signal  D_OUT_BEHAV :  std_logic_vector(5 downto 0);
  signal  D_OUT_RTL   :  std_logic_vector(5 downto 0);
  signal  RESET	      :  std_logic;
  signal  CLK         :  std_logic := '0';
  signal  READY_BEHAV : std_logic;
  signal  READY_RTL : std_logic;
  

  constant TPW  :  time := 5 ns ;


  component B48
    port ( CLK, RESET	: In     std_logic;
	   READY	: out	 std_logic;
           D_OUT	: Out    std_logic_vector (5 downto 0)
         );
  end component;

  for I_1: B48 use entity WORK.B48(BEHAVIOUR);
  for I_2: B48 use entity WORK.B48(RTL);

begin

  I_1 : B48
    Port Map (CLK, RESET, READY_BEHAV, D_OUT_BEHAV);
  I_2 : B48
    Port Map (CLK, RESET, READY_RTL, D_OUT_RTL);

  CLK <= not CLK after TPW;       -- Taktgenerator mit Periodendauer 2*TPW


  testmuster : process
   begin
       RESET <= '1';
       wait until clk'event and clk ='1';
       RESET <= '0';	-- aktiv high RESET
       for i in 0 to 50 loop
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
       for i in 0 to 100 loop
	   wait until clk'event and clk ='1';
	   assert D_OUT_RTL = D_OUT_BEHAV report "Zaehler-Ausgang stimmt nicht ueberein!" severity error;
	   assert READY_RTL = READY_BEHAV report "Ready-Ausgang stimmt nicht ueberein!" severity error;   
       end loop;  -- i      
       assert FALSE report "Test-Ende" severity failure;
       wait;  				-- forever
   end  process testmuster;
 
   
end SCHEMATIC;
