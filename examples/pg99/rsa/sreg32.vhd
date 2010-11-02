--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 32-Bit Schiebe Register
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 12.11.98        | 17.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  32-Bit Schiebe Register
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;


entity REG_32 is
  port(	DIn   : In 	std_logic_vector (31 downto 0); 
	Shift : In	STD_LOGIC;
	enable: In      STD_LOGIC;
        clk   : In	STD_LOGIC;
	DOut  : Out	std_logic 
       );
end REG_32;



architecture BEHAV of REG_32 is
  
  signal save : std_logic_vector(31 downto 0);

  begin

    DOut <= save(0);

    operate_REG: process(clk)
    begin
      if clk = '1' and clk'event  then
	if enable = '1' then
	  if Shift = '1' then
            save(30 downto 0) <= save(31 downto 1); save(31) <= '0';
          else
	    save <= DIn;
          end if;
	end if;
      end if;
    end process operate_REG;

  end BEHAV;





