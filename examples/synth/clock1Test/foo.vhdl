entity foo is
  port( a, b, clk : IN bit; z : OUT bit);
end entity foo;

architecture RTL of foo is 

begin

  p: process

  begin

    if clk'event and clk='1' then
      z <= a and b;
    end if;

  end process ;


end architecture RTL;

