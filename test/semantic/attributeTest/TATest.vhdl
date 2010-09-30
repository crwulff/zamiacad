
-- type attributes test

entity taTest is
	port( a, b : IN bit; z : OUT bit);
end entity taTest;

architecture RTL of taTest is 

  type alu_function is (disable, pass, add, subtract, multiply, divide);

  constant c1 : integer := alu_function'pos(pass);

begin
  
  
end architecture RTL;

