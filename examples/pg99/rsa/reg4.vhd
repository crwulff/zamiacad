--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : reg4 
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  reg4.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 11.01.99        | 11.01.99
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Ein einfaches 4-Bit Register. Ein Teil des Z32 Zaehlers
--------------------------------------------------------------------------

library ieee;
  use ieee.std_logic_1164.all;
  use ieee.std_logic_unsigned.all;

  
entity reg4 is
 port(clk,reset	: in  std_logic;
      d_in	: in  std_logic_vector(4 downto 0);
      d_out	: out std_logic_vector(4 downto 0)
      );
end reg4;


architecture BEHAV of reg4 is

begin
  -- purpose: 4-Bit Register mit synchronem Takteingang
  -- type:    memorizing
  -- inputs:  clk, reset, d_in
  -- outputs: d_out
  process (clk, reset)
      
  begin  -- process
      if clk'event and clk = '1' then
	  if reset = '0' then
	      d_out <= d_in;
	  else
	      d_out <= (others => '0');
	  end if;
      end if;
  end process;
end BEHAV;
