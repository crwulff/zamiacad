--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 16 Bit XOR
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : xor16_1.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 17.11.98        | 17.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Es wird ein 16 Bit XOR implementiert. Ein Eingang wird allerdings
--  nur 1 Bit breit sein. Die hat den Sinn diesen eingang als Steuerung zu verwenden
--  um den anderen Eingang invertiert, bzw. nicht invertiert durchzuschicken.
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;



entity XOR16_1 is
  port(	A     : In 	std_logic_vector (15 downto 0); 
	B     : In	std_logic;
	Q     : Out     std_logic_vector (15 downto 0));
end XOR16_1;



architecture BEHAV of XOR16_1 is

  begin

    Q <= A when B='0' else
         not A; -- B='1'


  end BEHAV;
