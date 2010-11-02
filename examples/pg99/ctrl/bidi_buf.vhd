--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : BIDI_BUF
-- Purpose : Tristate treiber
-- 
-- File Name : bidi_buf.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date   17.11.98 | Changes
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Einfacher tristate buffer wenn en=1 transparent wenn en=0 Hochohmig
--  
---------------------------------------------------------------------------
-- VHDL Model Created from SGE Symbol bidi_buf.sym -- Nov 17 14:46:56 1998

library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity BIDI_BUF is
      Port (      EN : In    std_logic;
                   I : In    std_logic_vector (31 downto 0);
                   O : InOut std_logic_vector (31 downto 0) );
end BIDI_BUF;


architecture BEHAVIORAL of BIDI_BUF is
begin
    O <= I when EN='1' else (others => 'Z');
end BEHAVIORAL;

