library ieee;
use ieee.std_logic_1164.all;

entity ccTest is
  port( a, b : IN bit; z : OUT bit);
end entity ccTest;

architecture RTL of ccTest is 

   constant v       : std_ulogic_vector := "";
   constant w       : positive range 1 to 65536 := 1;

   constant v2      : std_ulogic_vector (0 to (v'length + w-1)) := v & (0 to w-1 => '0');

begin

end architecture RTL;

