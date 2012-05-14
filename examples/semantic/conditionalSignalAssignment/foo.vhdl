entity foo is
end entity foo;

architecture bar of foo is

	signal a, b : bit;

begin

	a <= '1'
	when a = b and a /= b
	or a = b
	else '0'
	when a /= b
	or a /= b
	or a = b
	else '1';

end architecture bar;
