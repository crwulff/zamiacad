--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Markus Busch
-- Group     : DES
--------------------------------------------------------------------
-- Design Unit Name : Multiplexer 2:1 64 Bit
-- Purpose :
-- 
-- File Name :  word_mux2.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
--   10.11.98      | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  64 Bit 2:1 Multiplexer
--  
--------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;

entity word_mux2 is
	port (IN0, IN1 : in std_logic_vector(63 downto 0);
    	Y   : out std_logic_vector(63 downto 0);
	SEL : in std_logic);
end word_mux2;


architecture behaviour of word_mux2 is

signal OUT_Y : std_logic_vector(63 downto 0);
begin

process (IN0, IN1, SEL)
 begin
   
   if (SEL = '0') then
       OUT_Y <= IN0;
   else
       OUT_Y <= IN1;
   end if;
    
 end process;  
  
  Y <= OUT_Y;      
end behaviour;




