entity foo is
  port( a, b : IN bit; z : OUT bit);
end entity foo;

architecture RTL of foo is 

begin

  p: process

    variable v : bit;

  begin

    v := a xor b;

    z <= v;

  end process ;


end architecture RTL;

