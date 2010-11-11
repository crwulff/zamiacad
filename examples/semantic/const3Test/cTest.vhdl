


entity tcTest is
	port( a : IN bit; 
              z : OUT bit);
end entity tcTest;

architecture RTL of tcTest is

  constant w : integer := 16;

  SUBTYPE st IS bit_vector(0 TO w - 1);

  CONSTANT c1 : st := (0 TO 3 => '1', 4 TO st'high => '0');

begin


end architecture RTL;

