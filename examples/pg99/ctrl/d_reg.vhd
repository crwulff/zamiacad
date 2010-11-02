-- VHDL Model Created from SGE Symbol d_reg.sym -- Nov 17 20:24:24 1998
------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : D_REG
-- Purpose : 32 Bit Datenregister
-- 
-- File Name : d_reg.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date  17.11.98  | Changes
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Einfaches Datenregister mit enable und clk
--  
--------------------------------------------------------------------------

library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity D_REG is
      Port (     CLK : In    std_logic;
                  EN : In    std_logic;
                   I : In    std_logic_vector (31 downto 0);
               RESET : In    std_logic;
                   O : Out   std_logic_vector (31 downto 0) );
end D_REG;


architecture BEHAVIORAL of D_REG is
  signal y : STD_LOGIC_VECTOR(31 downto 0);
begin

  process(CLK,RESET)
  begin
     if RESET='0' then
	if CLK='1' and CLK'event then
	    if EN='1' then
		y <= i;
	    end if;
	end if;
     else
	y <= (others => '0');
     end if;
  end process;

  o <= y;

end BEHAVIORAL;

