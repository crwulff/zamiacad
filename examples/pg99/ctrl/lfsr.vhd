-- VHDL Model Created from SGE Symbol lfsr.sym -- Dec 21 16:15:16 1998

--------------------------------------------------------------------------
-----------------------------------------------------------------------
-----------------------------------------------------------------------
-----------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : LFSR
-- Purpose : Erzeugung von Pseudozufallszahlen
-- 
-- File Name : lfsr.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date  17.11.98  | Changes
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  64 Bitiges Linear Rueckgekoppeltes Schieberegister
--  mit 32 Bit bidirektionalem Datenbus
-- 
--  x = x^64 + x^4 + x^3 + x^1 + 1
--  (primitives polynom)
--------------------------------------------------------------------------

library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity LFSR is
      Port (     CLK : In    std_logic;
             RD_CONST : In    std_logic;
               RD_HI : In    std_logic;
               RD_LO : In    std_logic;
               WR_HI : In    std_logic;
               WR_LO : In    std_logic;
                DATA : InOut std_logic_vector (31 downto 0) );
end LFSR;

architecture BEHAVIORAL of LFSR is
  signal X : STD_LOGIC_VECTOR(63 downto 0);
  signal DATA_I : STD_LOGIC_VECTOR(31 downto 0);
begin

  process
  begin
    wait until CLK='1' and CLK'event;
      if WR_HI='1' then
	X <= X(63 downto 32) & DATA;
      elsif WR_LO='1' then
	X <= DATA & X(31 downto 0);
      else
	X(63 downto 5) <= X(62 downto 4);
	X(4) <= X(3) xor X(63);
	X(3) <= X(2) xor X(63);
	X(2) <= X(1);
	X(1) <= X(0) xor X(63);
	X(0) <= X(63);
      end if;
  end process;
  
  DATA_I <= "00" & X(61 downto 32) when RD_CONST='1' else
	    X(63 downto 32) when RD_HI='1' else
	    X(31 downto 0) when RD_LO='1' else
	    (others => '-');

  DATA <= DATA_I when (RD_LO or RD_HI or RD_CONST) = '1' else (others => 'Z');
  
end BEHAVIORAL;
