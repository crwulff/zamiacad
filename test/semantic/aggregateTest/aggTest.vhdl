library ieee;
use ieee.std_logic_1164.all;

entity aggTest is
  port( a, b : IN bit; z : OUT bit);
end entity aggTest;

architecture RTL of aggTest is 

  function foo (a,b : std_ulogic ; v : std_ulogic_vector) return std_ulogic_vector is
    variable result : std_ulogic_vector (0 to v'length-1);
  begin
    result :=  ( ( 0 to v'length-1 => a ) and v ) or ( 0 to v'length-1 => b ) ;
    return result ;
  end foo ;


    CONSTANT cV      : STD_ULOGIC_VECTOR := "1111111111111111";
    CONSTANT res     : STD_ULOGIC_VECTOR := foo ('0', '1', cV);

begin

end architecture RTL;

