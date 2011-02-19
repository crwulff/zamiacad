entity comp is
	generic ( 
		a : string );
	port (
		b : in string;
		c : in string;
		d : in bit_vector;
		e : out bit_vector );	
end entity comp;

architecture one of comp is 
begin
	process is
	begin
		wait for 1 ns;
		
		assert a = "0001";
		assert a'low = 1;
		assert a'left = 1;
		assert a'high = 4;
		assert a'right = 4;
		
		assert b'low = 1;
		assert b'left = 1;
		assert b'high = 4;
		assert b'right = 4;

		assert c = "0010";
		assert c'low = 1;
		assert c'left = 1;
		assert c'high = 4;
		assert c'right = 4;
		
		assert d = "000000000000";
		assert d'low = 8;
		assert d'left = 19;
		assert d'high = 19;
		assert d'right = 8;
		
		wait;
	end process;
end architecture one;

