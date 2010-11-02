-------------------------------------------------------------------------------
-- Crypto Chip
-- Copyright (C) 1999, Projektgruppe WS98/99
-- University of Stuttgart / Department of Computer Science / IFI-RA
-------------------------------------------------------------------------------
-- Designers:        Joerg Holzhauer
-- Group    :        DES
-------------------------------------------------------------------------------
-- Design Unit Name: DES_ExpansionGate
-- Purpose:          Gate for the DES-module-core for the cryptochip "pg99"
--
-- File Name:        e.vhd
-------------------------------------------------------------------------------
-- Simulator :       SYNOPSIS VHDL System Simulator (VSS) Version 3.2.a
-------------------------------------------------------------------------------
-- Date    09.11.98 |  Changes
--                  |
--                  |
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- contents :        port-description of one Gate of the DES-Module
--
-------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.all;
entity DES_ExpansionGate is
  port (i :in STD_LOGIC_VECTOR(1 to 32);
        o :out STD_LOGIC_VECTOR(1 to 48));
end DES_ExpansionGate;

architecture behavorial of DES_ExpansionGate is
begin
  process(i)
  begin
	o(1)<=i(32);
	o(2)<=i(1);
	o(3)<=i(2);
	o(4)<=i(3);
	o(5)<=i(4);
	o(6)<=i(5);
	o(7)<=i(4);
	o(8)<=i(5);
	o(9)<=i(6);
	o(10)<=i(7);
	o(11)<=i(8);
	o(12)<=i(9);
	o(13)<=i(8);
	o(14)<=i(9);
	o(15)<=i(10);
	o(16)<=i(11);
	o(17)<=i(12);
	o(18)<=i(13);
	o(19)<=i(12);
	o(20)<=i(13);
	o(21)<=i(14);
	o(22)<=i(15);
	o(23)<=i(16);
	o(24)<=i(17);
	o(25)<=i(16);
	o(26)<=i(17);
	o(27)<=i(18);
	o(28)<=i(19);
	o(29)<=i(20);
	o(30)<=i(21);
	o(31)<=i(20);
	o(32)<=i(21);
	o(33)<=i(22);
	o(34)<=i(23);
	o(35)<=i(24);
	o(36)<=i(25);
	o(37)<=i(24);
	o(38)<=i(25);
	o(39)<=i(26);
	o(40)<=i(27);
	o(41)<=i(28);
	o(42)<=i(29);
	o(43)<=i(28);
	o(44)<=i(29);
	o(45)<=i(30);
	o(46)<=i(31);
	o(47)<=i(32);
	o(48)<=i(1);
  end process;
end behavorial;
