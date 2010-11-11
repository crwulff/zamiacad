entity litTest is
  port (v2 : IN bit_vector(0 to 31));
end entity litTest;

architecture RTL of litTest is 

--  signal v  : bit_vector(0 to 15);

  type gbenum is (a,b,c,d);
  type gbarray is array (gbenum) of bit;

  constant c1 : gbarray := x"A";

begin

end architecture RTL;

