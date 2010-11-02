--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Markus Busch, Thomas Stanka
-- Group     : DES
--------------------------------------------------------------------
-- Design Unit Name : WORD_REG_32
-- Purpose :
-- 
-- File Name : word_reg_32.vhd 
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date  10.11.98  | Changes 
--                 |
--        3.12.98  |  cut OUT_EN and tristate output T.S.
--       20.01.99  |  changed from MS to clock-triggered
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  declaration for M-S-register (32-bit) 
--  
--------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;

entity WORD_REG_32 is
  port (D  : in std_logic_vector(31 downto 0);
	Q  : out std_logic_vector(31 downto 0);
	LATCH_EN : in std_logic;
	RESET    : in std_logic;
	CLK	 : in std_logic);
end WORD_REG_32;


architecture behaviour of WORD_REG_32 is

signal OUT_Y : std_logic_vector(31 downto 0);

begin

  process (CLK, RESET)
    begin   
      if (CLK'event) and (CLK ='1') then
	  if LATCH_EN='1' then
	        OUT_Y <= to_ux01(D);
	  end if;   
      end if;
      if (RESET = '1') then
	OUT_Y <= (others => '0');
      end if;  	
  end process;   
  Q  <= OUT_Y;   
end behaviour;
