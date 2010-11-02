-- VHDL Model Created from SGE Symbol reg_ctrl.sym -- Dec 22 23:57:24 1998
--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : REG_CTRL
-- Purpose : Speicherung des Zustands der Register von und zu DES
-- 
-- File Name : reg_ctrl.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date  23.12.98  | Changes
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  JK - Flipflop mit asyncronem Reset
--  
--------------------------------------------------------------------------

library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity REG_CTRL is
      Port (     CLK : In    std_logic;
                   J : In    std_logic;
                   K : In    std_logic;
               RESET : In    std_logic;
                   O : Out   std_logic );
end REG_CTRL;


architecture BEHAVIORAL of REG_CTRL is
    signal O_I : STD_LOGIC;
begin
    
    DES_PUT : process (CLK, RESET)
    begin  -- process DES_PUT
	-- activities triggered by asynchronous reset (active high)
	if RESET = '1' then
	    O_I<='0';
	-- activities triggered by rising edge of clock
	elsif CLK'event and CLK = '1' then
	    if K = '1' then
		O_I <= '0';
	    end if;
	    if J = '1' then
		O_I <= '1';
	    end if;
	end if;
    end process DES_PUT;

    O <= O_I;

end BEHAVIORAL;
