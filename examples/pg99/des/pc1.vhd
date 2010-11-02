-------------------------------------------------------------------------------
-- Crypto Chip
-- Copyright (C) 1999, Projektgruppe WS98/99
-- University of Stuttgart / Department of Computer Science / IFI-RA
-------------------------------------------------------------------------------
-- Designers:        Joerg Holzhauer+Thomas Stanka
-- Group    :        DES
-------------------------------------------------------------------------------
-- Design Unit Name: DES_KeyPermutation1
-- Purpose:          Gate for the DES-module-core for the cryptochip "pg99"
--
-- File Name:        pc1_neu.vhd
-------------------------------------------------------------------------------
-- Simulator :       SYNOPSIS VHDL System Simulator (VSS) Version 3.2.a
-------------------------------------------------------------------------------
-- Date    09.11.98 |  Changes
--         05.02.   |  changed it and dont ask why
--                  |
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- contents :        port-description of one Gate of the DES-Module
--
-------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.all;
entity DES_KeyPermutation1 is
  port (i :in STD_LOGIC_VECTOR(1 to 56);
        o :out STD_LOGIC_VECTOR(1 to 56));
end DES_KeyPermutation1;

architecture behavorial of DES_KeyPermutation1 is
begin
  process(i)
  begin
	o(1)<=i(50);
	o(2)<=i(43);
	o(3)<=i(36);
	o(4)<=i(29);
	o(5)<=i(22);
	o(6)<=i(15);
	o(7)<=i(8);
	o(8)<=i(1);
	o(9)<=i(51);
	o(10)<=i(44);
	o(11)<=i(37);
	o(12)<=i(30);
	o(13)<=i(23);
	o(14)<=i(16);
	o(15)<=i(9);
	o(16)<=i(2);
	o(17)<=i(52);
	o(18)<=i(45);
	o(19)<=i(38);
	o(20)<=i(31);
	o(21)<=i(24);
	o(22)<=i(17);
	o(23)<=i(10);
	o(24)<=i(3);
	o(25)<=i(53);
	o(26)<=i(46);
	o(27)<=i(39);
	o(28)<=i(32);
	o(29)<=i(56);
	o(30)<=i(49);
	o(31)<=i(42);
	o(32)<=i(35);
	o(33)<=i(28);
	o(34)<=i(21);
	o(35)<=i(14);
	o(36)<=i(7);
	o(37)<=i(55);
	o(38)<=i(48);
	o(39)<=i(41);
	o(40)<=i(34);
	o(41)<=i(27);
	o(42)<=i(20);
	o(43)<=i(13);
	o(44)<=i(6);
	o(45)<=i(54);
	o(46)<=i(47);
	o(47)<=i(40);
	o(48)<=i(33);
	o(49)<=i(26);
	o(50)<=i(19);
	o(51)<=i(12);
	o(52)<=i(5);
	o(53)<=i(25);
	o(54)<=i(18);
	o(55)<=i(11);
	o(56)<=i(4);
  end process;
end behavorial;
