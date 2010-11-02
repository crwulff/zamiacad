--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : inc4i 
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  inc4i.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 11.01.99        | 11.01.99
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Ein einfacher 4-Bit Inkrementer. Ein Teil des Z32 Zaehlers. Mit
--  Inkrement-Eingang
--------------------------------------------------------------------------


library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_unsigned.all; 



entity inc4i is
  port (a   : in  std_logic_vector(4 downto 0);
	b   : out std_logic_vector(4 downto 0);
	inc : in std_logic
	);
end inc4i;

architecture RTL of inc4i is

    signal ltgof, ltg1, ltg2, ltg3, ltg4 : std_logic;
    
    component HA
	port ( a,b	   : In     std_logic;
	       S,cout	   : Out    std_logic
	       );
    end component;  

    for I_0, I_1, I_2, I_3, I_4 : HA use entity WORK.HA(RTL);
    
begin

    
    I_0 : HA
	Port Map (inc, a(0), b(0), ltg1);
    I_1 : HA
	Port Map (ltg1, a(1), b(1), ltg2);
    I_2 : HA
	Port Map (ltg2, a(2), b(2), ltg3);
    I_3 : HA
	Port Map (ltg3, a(3), b(3), ltg4);
    I_4 : HA
	Port Map (ltg4, a(4), b(4), ltgof);

    
end RTL;


