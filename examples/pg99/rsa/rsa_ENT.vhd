-- File>>> rsa_ENT.vhd
--
-- Date:   Tue Nov 10 19:11:30 MET 1998
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

entity rsa is
  port (    	 CLK : In    std_logic;
              DATAIN : In    std_logic_vector (31 downto 0);
                  GO : In    std_logic;
               RESET : In    std_logic;
             SELDATA : In    std_logic_vector (1 downto 0);
              VALACC : In    std_logic;
             DATAOUT : Out   std_logic_vector (31 downto 0);
             NEXTEXP : Out   std_logic;
               READY : Out   std_logic);
end rsa;

