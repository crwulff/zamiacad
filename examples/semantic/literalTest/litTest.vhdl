
entity litTest is
end entity litTest;

architecture RTL of litTest is 

	signal p: bit_vector(0 to 3);

	type gbenum is (a,b,c,d);
	type gbarray is array (gbenum) of bit;

	procedure check(failed:boolean; msg: string) is begin
		assert failed report msg severity error;
	end procedure;

	function "<=" (L, R: gbarray) return BOOLEAN is
	begin
		check(false, "should this ever be called?");
		return true; 
	end "<=";
	
	signal s1: bit_vector(p'range) := ('1', others => '0'); 
	constant p2: bit_vector(0 to 31) := (others => '1');
	constant v2 : bit_vector(0 to 15) := (others => '0');
	constant str3 : string := "abc 'a'";
	constant c4 : gbarray := x"A";	
	signal s5 : bit_vector (7 downto 0) := "00000100";
	
	type DURATION is range -2000000 to 2000000
		units fs; ps = 1000 fs; ns = 1000 ps; end units;
	constant time6: DURATION := 1.2 ns;
	
	impure function test145 return boolean is 
	begin
		-- how do we read these signal values during elaboration?
		check(s1 = "1000", "Test 1: aggregate port range failed");
		check(c4 = x"A", "test 4 failed: array value is wrong");
		check(s5 = "00000100", "Test 5 has failed: aggregate initialization mismateches the literal");
		
		-- There was an error considering physical literal as real
		-- which resulted in 1.2 ns parsed as 1199 ps.
		check(time6 = 1200 ps, "time must be 1.2 ns = 1200 ps, yet we got " & DURATION'image(time6) );
		
		-- I have spotted this silently failind in Guenter's literal2Test
		-- along with declaration of operator "<=" above.
		-- I have no idea what this return assignment is about.
		--return ((x"0000" & v2(0 to 15)) <= p2(0 to 31));
		
		report "Test complete";
		return true;
		
	end function;
	constant result145: boolean := test145;
	
begin
	
end architecture RTL;

