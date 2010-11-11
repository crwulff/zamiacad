library ieee;
use ieee.std_logic_1164.all;

entity exprTest is
  port( a, b : IN bit; z : OUT bit);
end entity exprTest;

architecture RTL of exprTest is 

    CONSTANT c1      : STD_ULOGIC_VECTOR := "1111111111111111";
    CONSTANT c2      : STD_ULOGIC_VECTOR := not c1;

begin

end architecture RTL;

