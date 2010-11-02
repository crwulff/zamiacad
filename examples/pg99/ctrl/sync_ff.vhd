-- VHDL Model Created from SGE Symbol sync_ff.sym -- Dec 23 00:42:10 1998
--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : SYNC_FF
-- Purpose : erzeugung des BUSY Signal
-- 
-- File Name : sync_ff.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date  23.11.98  | Changes
--                 | 
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  FlipFlop mit 2 asyncronen set Eingaengen
--  Reset asyncron und bei jeder pos. Taktflanke
--------------------------------------------------------------------------

library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity SYNC_FF is
      Port (     CLK : In    std_logic;
               RESET : In    std_logic;
               SET_A : In    std_logic;
               SET_B : In    std_logic;
                   Q : Out   std_logic );
end SYNC_FF;


architecture BEHAVIORAL of SYNC_FF is

    signal Q_I : std_logic;

begin    
    -- purpose: stretch BUSY
    -- type:    memorizing
    -- inputs:  CLK, RESET, SET_A, SET_B
    -- outputs: Q_I
    process (CLK, RESET)
    begin
	-- asynchronous reset (active high)
	if RESET = '1' then
	    Q_I <= '0';
	-- reset triggered by rising edge of clock
	elsif CLK'event and CLK = '1' then
	    if SET_A = '1' or SET_B = '1' then
		Q_I <= '1';
	    else
		Q_I <= '0';
	    end if;
	end if;
    end process;

    Q <= Q_I or SET_A or SET_B;

end BEHAVIORAL;
