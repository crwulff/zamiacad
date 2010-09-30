
entity type2Test is
  port( a, b : IN bit; z : OUT bit);
end entity type2Test;

architecture RTL of type2Test is 

  subtype verysmall_int is integer range 1 to 32;
  type a1 is array(verysmall_int'range) of bit;

begin

end architecture RTL;

