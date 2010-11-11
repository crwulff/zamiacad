library ieee; 
use ieee.std_logic_1164.all;

entity type3Test is
  port( a, b : IN bit; z : OUT bit);
end entity type3Test;

architecture RTL of type3Test is 

  subtype single is std_logic;
  signal clk : single;

begin

end architecture RTL;

