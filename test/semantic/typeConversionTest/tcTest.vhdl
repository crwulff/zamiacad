library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity e1 is
  generic (foo : std_logic_vector);
end;

architecture beh of e1 is
begin
end;


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity tcTest is
	port( a, b : IN bit; z : OUT bit);
end entity tcTest;

architecture RTL of tcTest is
  constant vendor : integer := 42;
begin

  x : e1 generic map (foo => std_logic_vector(to_unsigned(vendor, 8)));
	
end architecture RTL;

