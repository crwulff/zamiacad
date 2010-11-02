-- File>>> CFG_rsa_RTL.vhd
--
-- Date:   Tue Nov 10 19:11:35 MET 1998
-- Author: wackerao
--
-- Revision history:
--
-- $Source$
-- $Revision$
-- $Log$
--
--

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;

library LIB_rsa;
-- use LIB_rsa.PCK_rsa.all;

configuration CFG_rsa_RTL of rsa is
  for RTL
  end for;
end CFG_rsa_RTL;
