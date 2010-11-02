-- VHDL Model Created from SGE Symbol rip.sym -- Nov 17 22:51:29 1998

--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : RIP
-- Purpose : Verbindet Leitungen mit unterschiedlichem Namen
-- 
-- File Name : rip.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date  18.11.98  | Changes
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Nur fur eine Leitung in eine Richtung
--------------------------------------------------------------------------

library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity RIP is
      Port (       A : In    std_logic;
                   B : Out   std_logic );
end RIP;

architecture BEHAVIORAL of RIP is
begin
  B <= A;
end BEHAVIORAL;
