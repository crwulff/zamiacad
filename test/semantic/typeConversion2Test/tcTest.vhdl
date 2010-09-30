library ieee;
use ieee.std_logic_1164.all;

entity tcTest is
	port( a, b : IN bit; z : OUT bit);
end entity tcTest;

architecture RTL of tcTest is

function tc ( s : std_ulogic_vector ) return integer is
begin
   return 42;
end tc;

function tc ( s : std_ulogic_vector ) return real is
begin
   return 42.0;
end tc;

signal s : bit;
signal v : std_ulogic_vector(0 to 7);
signal v2: std_ulogic_vector(0 to 2) := "111";

begin

  s <= '1' when v(tc(v2)) = '1' else '0';

end architecture RTL;

