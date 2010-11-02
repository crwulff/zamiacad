--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 16 Bit 4-zu-1 Multiplexer
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : mux16_4to1.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 21.12.98        | 21.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Es wird ein 16 Bit 4  zu 1 Multiplexer implementiert
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;



entity MUX16_4to1 is
  port(	A     : in  	std_logic_vector (15 downto 0); 
	B     : in 	std_logic_vector (15 downto 0);
	C     : in 	std_logic_vector (15 downto 0);
	D     : in 	std_logic_vector (15 downto 0);
        Sel   : in 	std_logic_vector (1 downto 0);
	Q     : out	std_logic_vector (15 downto 0));
end MUX16_4to1;



architecture BEHAV of MUX16_4to1 is

  begin

    Q <= A when Sel = "00" else
         B when Sel = "01" else
	 C when Sel = "10" else
	 D ;  				-- Sel='11'

  end BEHAV;
