entity foo is
  port( a, b : IN bit_vector(3 downto 0); z : OUT bit_vector(3 downto 0));
end entity foo;

architecture RTL of foo is 

begin

  z(0) <= a(0) and b(0);
  z(1) <= a(1) and b(1);
  z(2) <= a(2) and b(2);
  z(3) <= a(3) and b(3);
  

end architecture RTL;

