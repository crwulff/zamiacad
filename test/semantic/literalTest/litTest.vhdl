
entity litTest is
  port( v : IN bit_vector(0 to 3) := "0101"; z : OUT bit);
end entity litTest;

architecture RTL of litTest is 

  signal s: bit_vector(v'range) := ('1', others => '0');

begin

end architecture RTL;

