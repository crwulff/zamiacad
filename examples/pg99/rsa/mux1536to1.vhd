--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 1536-zu-1 Multiplexer
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : mux1536to1.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 10.11.98        | 10.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Es wird ein 1536 zu 1 Multiplexer implementiert
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;



entity MUX_1536to1 is
  port(	A     : In 	std_logic_vector (767 downto 0); 
	B     : In	std_logic_vector (767 downto 0);
	Sel   : In	std_logic_vector (9 downto 0);
        ASel  : In	STD_LOGIC;
	Q     : Out     STD_LOGIC);
end MUX_1536to1;



architecture BEHAV of MUX_1536to1 is

begin

      Q <= '0' when conv_integer(Sel) = 768 or conv_integer(Sel) = 769 else
	   '-' when conv_integer(Sel) > 769 else
	   B(conv_integer(Sel)) when (ASel = '1') else
	   A(conv_integer(Sel));

  end BEHAV;
