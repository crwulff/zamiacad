library ieee;
use ieee.std_logic_1164.all;

entity tc3Test is
	port( a, b : IN bit; z : OUT bit);
end entity tc3Test;

architecture RTL of tc3Test is

function tc ( s : std_ulogic_vector ) return integer is
begin
	report "tc int executed";
   return 6;
end tc;

function tc ( s : std_ulogic_vector ) return real is
begin
	report "tc real executed";
   return 6.0;
end tc;


--constant vA : std_ulogic_vector(0 to 7) := "11111111";
--constant BB: bit := vA(3); -- this does not work even in Modelsim

signal s : bit;
signal v : std_ulogic_vector(0 to 7);
signal v2: std_ulogic_vector(0 to 2) := "111";

begin

  s <= '1' when v(tc(v2)) = '1' else '0';

end architecture RTL;

