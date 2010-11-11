library ieee;
use ieee.std_logic_1164.all;

entity typeTest is
  port( a, b : IN bit; z : OUT bit);
end entity typeTest;

architecture RTL of typeTest is 

  constant c1   : natural := 24;    
  constant c2   : natural := 3;  
  subtype  c1_range   is natural range 0 to c1-1;
  subtype  c2_range   is natural range 0 to c2-1;


  type r1 is record
    f1       :  std_ulogic_vector(c1_range);
    f2       :  std_ulogic_vector(c2_range);
  end record r1;

begin

end architecture RTL;

