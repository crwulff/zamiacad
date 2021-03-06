-------------------------------------------------------------------------------
-- Crypto Chip
-- Copyright (C) 1999, Projektgruppe WS98/99
-- University of Stuttgart / Department of Computer Science / IFI-RA
-------------------------------------------------------------------------------
-- Designers:        Joerg Holzhauer
-- Group    :        DES
-------------------------------------------------------------------------------
-- Design Unit Name: DES_S6_Box
-- Purpose:          Gate for the DES-module-core for the cryptochip "pg99"
--
-- File Name:        s6.vhd
-------------------------------------------------------------------------------
-- Simulator :       SYNOPSIS VHDL System Simulator (VSS) Version 3.2.a
-------------------------------------------------------------------------------
-- Date    09.11.98 |  Changes
--                  |
--                  |
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- contents :        port- and behaviour-description of
--                   one Gate of the DES-Module
--
--
-------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.all;
entity DES_S6_Box is
  port (i :in STD_LOGIC_VECTOR(0 to 5);
        o :out STD_LOGIC_VECTOR(0 to 3));
end DES_S6_Box;

architecture behavorial of DES_S6_Box is
begin
with i select
o(0) <= '1' when "000000"|
"000100"|
"000110"|
"001000"|
"001110"|
"010010"|
"011000"|
"011110"|
"000001"|
"000011"|
"001011"|
"001101"|
"010101"|
"010111"|
"011011"|
"011111"|
"100000"|
"100010"|
"100100"|
"101010"|
"101100"|
"110110"|
"111010"|
"111100"|
"100111"|
"101001"|
"101101"|
"101111"|
"110001"|
"110011"|
"111101"|
"111111",
'0' when others;
with i select
o(1) <= '1' when "000000"|
"000110"|
"001100"|
"010010"|
"010110"|
"011000"|
"011010"|
"011100"|
"000011"|
"000101"|
"001001"|
"001011"|
"001111"|
"010001"|
"010101"|
"010111"|
"100010"|
"100100"|
"100110"|
"101100"|
"110000"|
"110100"|
"111010"|
"111110"|
"100001"|
"100111"|
"101011"|
"101101"|
"110011"|
"110111"|
"111001"|
"111111",
'0' when others;
with i select
o(2) <= '1' when "000100"|
"000110"|
"001010"|
"001100"|
"010100"|
"011000"|
"011010"|
"011110"|
"000001"|
"000011"|
"000111"|
"001001"|
"010001"|
"010111"|
"011011"|
"011101"|
"100010"|
"100100"|
"101000"|
"101110"|
"110000"|
"110110"|
"111100"|
"111110"|
"100011"|
"100101"|
"101101"|
"101111"|
"110001"|
"110011"|
"110111"|
"111001",
'0' when others;
with i select
o(3) <= '1' when "000010"|
"000110"|
"001000"|
"010010"|
"010100"|
"011010"|
"011100"|
"011110"|
"000011"|
"001001"|
"001101"|
"001111"|
"010011"|
"010101"|
"011011"|
"011101"|
"100000"|
"100100"|
"100110"|
"101110"|
"110000"|
"111000"|
"111010"|
"111100"|
"100011"|
"101001"|
"101011"|
"101101"|
"110001"|
"110101"|
"110111"|
"111111",
'0' when others;
end behavorial;
