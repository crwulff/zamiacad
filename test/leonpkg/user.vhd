--
-- the only purpose of this file is to use as many
-- packages as possible
--

library IEEE;
use STD.TEXTIO.ALL;
use IEEE.std_logic_1164.ALL;
use IEEE.std_logic_arith.ALL;
use IEEE.std_logic_misc.ALL;
use IEEE.STD_LOGIC_TEXTIO.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.VITAL_Primitives.ALL;
use IEEE.VITAL_Timing.ALL;
library GRLIB;
use GRLIB.amba.ALL;

entity user is 
  port (A, B    : in  bit_vector (3 downto 0);
        C_in    : in  bit;
        S       : out bit_vector (3 downto 0);
        C       : out bit);
end;

architecture RTL of user is
begin
  c <= a(0) or b(1) or C_in;
  s <= a;
end;


