-----------------------------------------------
-- half adder - first test design for zamia
-----------------------------------------------

entity half_adder is
  port (A, B : in  bit;;
        S, C : out bit);
end;

architecture behavioral of half_adder is
begin

  S <= A XOR B;
  C <= A AND B;

end;
