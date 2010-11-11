entity typeTest is
  port( a, b : IN bit; z : OUT bit);
end entity typeTest;

architecture RTL of typeTest is 

  type b4v is array (natural range <>) of bit_vector;

  SIGNAL v1 : b4v(0 to 1);
  SIGNAL v2 : b4v(0 to 1);
  SIGNAL v3 : bit_vector(0 to 3);


begin

  v1 <= v3 & v2(0);

end architecture RTL;

