entity foo is
  port( a, b, c, n, r : IN bit; z : OUT bit);
end entity foo;

architecture RTL of foo is 

begin

  process
  begin
    if (n='1' and (c'event and c='1')) or r='1' then
      if r='1' then
	z <= '0';
      else
	if n='1' and (c'event and c='1') then
	  z <= a and b;
	end if;
      end if;
    end if;
  end process;

end architecture RTL;

