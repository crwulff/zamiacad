--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : reg10 
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  reg10.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 11.01.99        | 11.01.99
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Ein einfaches 10-Bit Register. Ein Teil des B768 Zaehlers
--------------------------------------------------------------------------

library ieee;
  use ieee.std_logic_1164.all;
  use ieee.std_logic_unsigned.all;

  
entity reg10 is
 port(clk,reset	: in  std_logic;
      d_in	: in  std_logic_vector(9 downto 0);
      d_out	: out std_logic_vector(9 downto 0)
      );
end reg10;


architecture BEHAV of reg10 is

begin
  -- purpose: 10-Bit Register mit synchronem Takteingang
  -- type:    memorizing
  -- inputs:  clk, reset, d_in
  -- outputs: d_out
  process (clk, reset)
      
  begin  -- proces
      if clk'event and clk = '1' then
	  if reset = '0' then
	      d_out <= d_in;
	  else
	      d_out <= (others => '0');
	  end if;
      end if;
  end process;
end BEHAV;
