-- VHDL Model Created from SGE Symbol demux.sym -- Dec 21 12:40:53 1998

--------------------------------------------------------------------------
-----------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : DEMUX
-- Purpose : fuer main_ctrl
-- 
-- File Name : demux.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date  18.11.98  | Changes
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  1 zu 8 Demultiplexer ohne ausgang fuer 0
--  
--------------------------------------------------------------------------

library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;

entity DEMUX is
      Port (       A : In    std_logic_vector (2 downto 0);
                 SEL : Out   std_logic_vector (7 downto 0) );
end DEMUX;

architecture BEHAVIORAL of DEMUX is
begin

  SEL <= "00000101" WHEN A="000" ELSE
         "00000110" WHEN A="001" ELSE
	 "00000000" WHEN A="010" ELSE
	 "00001100" WHEN A="011" ELSE
	 "00010100" WHEN A="100" ELSE
	 "00100100" WHEN A="101" ELSE
	 "01000100" WHEN A="110" ELSE
	 "10000100";  	 --"111"

end BEHAVIORAL;
