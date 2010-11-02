--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 768-Bit Register
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : reg768.vhd 
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 16.11.98        | 17.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  768-Bit Register
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;


entity REG_768 is
  port(	DIn   : In 	std_logic_vector (15 downto 0); 
	ENABLE: In      STD_LOGIC;
        CLK   : In	STD_LOGIC;
	DOut  : Out	std_logic_vector (767 downto 0) 
       );
end REG_768;



architecture BEHAV of REG_768 is
  
  signal save : std_logic_vector(767 downto 0);

  begin

    DOut <= save;

    operate_REG: process(clk)
    begin
      if clk = '1' and CLK'event then
	if enable = '1' then
            save(751 downto 0) <= save(767 downto 16); save(767 downto 752) <= DIn;
	end if;
      end if;
    end process operate_REG;

  end BEHAV;
