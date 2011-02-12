entity foo is
  port( a, b, c1, c2 : IN bit; z : OUT bit);
end entity foo;

architecture RTL of foo is 

begin

  p: process

  begin

    if c1='1' then
      if c2='1' then
        z <= a xor b;
      end if;
    else
      z <= a and b;
    end if;

  end process ;


end architecture RTL;

