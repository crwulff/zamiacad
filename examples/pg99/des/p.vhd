-------------------------------------------------------------------------------
-- Crypto Chip
-- Copyright (C) 1999, Projektgruppe WS98/99
-- University of Stuttgart / Department of Computer Science / IFI-RA
-------------------------------------------------------------------------------
-- Designers:        Joerg Holzhauer
-- Group    :        DES
-------------------------------------------------------------------------------
-- Design Unit Name: DES_Permutation
-- Purpose:          Gate for the DES-module-core for the cryptochip "pg99"
--
-- File Name:        p.vhd
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
entity DES_Permutation is
  port (i :in STD_LOGIC_VECTOR(1 to 32);
        o :out STD_LOGIC_VECTOR(1 to 32));
end DES_Permutation;

architecture behavorial of DES_Permutation is
begin
  process(i)
  begin
	o(1)<=i(16);
	o(2)<=i(7);
	o(3)<=i(20);
	o(4)<=i(21);
	o(5)<=i(29);
	o(6)<=i(12);
	o(7)<=i(28);
	o(8)<=i(17);
	o(9)<=i(1);
	o(10)<=i(15);
	o(11)<=i(23);
	o(12)<=i(26);
	o(13)<=i(5);
	o(14)<=i(18);
	o(15)<=i(31);
	o(16)<=i(10);
	o(17)<=i(2);
	o(18)<=i(8);
	o(19)<=i(24);
	o(20)<=i(14);
	o(21)<=i(32);
	o(22)<=i(27);
	o(23)<=i(3);
	o(24)<=i(9);
	o(25)<=i(19);
	o(26)<=i(13);
	o(27)<=i(30);
	o(28)<=i(6);
	o(29)<=i(22);
	o(30)<=i(11);
	o(31)<=i(4);
	o(32)<=i(25);
  end process;
end behavorial;
