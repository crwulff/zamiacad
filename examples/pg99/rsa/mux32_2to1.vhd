--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 32 Bit 2-zu-1 Multiplexer
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : mux32_2to1.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 21.12.98        | 21.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Es wird ein 32 Bit 2  zu 1 Multiplexer implementiert
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;



entity MUX32_2to1 is
  port(	A     : In 	std_logic_vector (31 downto 0); 
	B     : In	std_logic_vector (31 downto 0);
        Sel   : In	STD_LOGIC;
	Q     : Out     std_logic_vector (31 downto 0));
end MUX32_2to1;



architecture BEHAV of MUX32_2to1 is

  begin

    Q <= A when Sel='0' else
         B ;  				-- Sel = '1'

  end BEHAV;
