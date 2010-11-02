--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : inc10 
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  inc10.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 11.01.99        | 11.01.99
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Ein einfacher 10-Bit Inkrementer. Ein Teil des B768 Zaehlers
--------------------------------------------------------------------------


library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_unsigned.all; 



entity inc10 is
  port (a: in  std_logic_vector(9 downto 0);
	b: out std_logic_vector(9 downto 0));
end inc10;

architecture RTL of inc10 is

    signal ltgof, ltg1, ltg2, ltg3, ltg4, ltg5, ltg6, ltg7, ltg8, ltg9 : std_logic;
    signal Eins : std_logic := '1';
    
    component HA
	port ( a,b	   : In     std_logic;
	       S,cout	   : Out    std_logic
	       );
    end component;  

    for I_0, I_1, I_2, I_3, I_4, I_5, I_6, I_7, I_8, I_9 : HA use entity WORK.HA(RTL);
    
begin

    Eins <= '1';
    
    I_0 : HA
	Port Map (Eins, a(0), b(0), ltg1);
    I_1 : HA
	Port Map (ltg1, a(1), b(1), ltg2);
    I_2 : HA
	Port Map (ltg2, a(2), b(2), ltg3);
    I_3 : HA
	Port Map (ltg3, a(3), b(3), ltg4);
    I_4 : HA
	Port Map (ltg4, a(4), b(4), ltg5);
    I_5 : HA
	Port Map (ltg5, a(5), b(5), ltg6);
    I_6 : HA
	Port Map (ltg6, a(6), b(6), ltg7);
    I_7 : HA
	Port Map (ltg7, a(7), b(7), ltg8);
    I_8 : HA
	port map (ltg8, a(8), b(8), ltg9);
    I_9 : HA
	port map (ltg9, a(9), b(9), ltgof);
    
end RTL;


