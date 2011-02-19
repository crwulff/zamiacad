entity foo is
end entity foo;
architecture one of foo is
	
	component comp is   
	generic ( 
		a : string );
	port ( 
		b : in string;
		c : in string;
		d : in bit_vector;
		e : out bit_vector);
	end component comp ;
	
	signal b : string (1 to 4);
	signal d : bit_vector (19 downto 8);
	signal e : bit_vector (19 downto 8);
	
begin
	
	my_comp : comp
	generic map ( "0001" )
	port map ( b, "0010", d, e);
	
end architecture one;

