--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designer  : Thomas Stanka              
-- Group     : DES
--------------------------------------------------------------------
-- Design Unit Name : XOR64 
-- Purpose : Part of the DES-module-core for the cryptochip "pg99"
-- 
-- File Name :  xor64.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date 13.11.98   | Changes
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  contents BEHAVIORAL-View of an 64-Bit-XOR
--  
--------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;

entity XOR64 is
  port( A :in STD_LOGIC_VECTOR(63 downto 0);
	B :in STD_LOGIC_VECTOR(63 downto 0);
	O :out STD_LOGIC_VECTOR(63 downto 0));
end XOR64;

architecture BEHAVIORAL of XOR64 is
begin
	process(A,B)
	begin
	O<=(A XOR B);
	end process;
end BEHAVIORAL;



