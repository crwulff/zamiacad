--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 16-Bit Register
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 17.11.98        | 17.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  16-Bit Register
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;


entity REG_16 is
  port(	DIn   : In 	std_logic_vector (15 downto 0); 
        clk   : In	STD_LOGIC;
	DOut  : Out	std_logic_vector (15 downto 0) 
       );
end REG_16;



architecture BEHAV of REG_16 is
  
  signal save : std_logic_vector(15 downto 0);

  begin

    DOut <= save;

    operate_REG: process(clk)
    begin
      if clk = '1' and clk'event then
	  save <= DIn;
      end if;
    end process operate_REG;

  end BEHAV;
