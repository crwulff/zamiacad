-- Demonstrates normalization and alias over alias

entity ALIAS_DEMO is end;

architecture ARCH of ALIAS_DEMO is

  	type bit_vector is array (integer range <>) of bit;
  
		function TO_STR(a: BIT_VECTOR) return string is 
			alias norm : bit_vector(1 to a'length) is a;
			variable RESULT: string(norm'range);
		begin
			for I in norm'range loop
				if norm(I) = '1' then
					RESULT(I) := '1';
				else 
					RESULT(I) := '0';
				end if;
			end loop;
			return RESULT; 
		end function;
		
	function STR_range(BV: bit_vector) return string is begin
	  if BV'left > BV'right then
	  	return to_str(bv) & "(" & integer'image(bv'left) & " to " & integer'image(bv'right) & ")";
	  else 
	  	return to_str(bv) & "(" & integer'image(bv'left) & " downto " & integer'image(bv'right)  & ")";
	  end if;
	 end function;
	 
	function "+" ( bv1, bv2 : bit_vector ) return bit_vector is
		alias norm1 : bit_vector(1 to bv1'length) is bv1; -- normalization
		alias norm2 : bit_vector(1 to bv2'length) is bv2;
		variable result : bit_vector(1 to bv1'length);
		variable carry : bit := '0';
	begin
		if bv1'length /= bv2'length then
			report"arguments of different length" severity failure;
		else
			for index in norm1'reverse_range loop
				result(index) := norm1(index) xor norm2(index) xor carry;
				carry := ( norm1(index) and norm2(index) )
					or( carry and( norm1(index) or norm2(index) ) );
			end loop;
		end if;
		report STR_RANGE(bv1) & " + " & STR_RANGE(bv2) & " = " & STR_RANGE(RESULT);
		return result;
	end function;

	function INIT return boolean is
		constant a: bit_vector(1 to 4) := "1100";
		constant b: bit_vector(1 to 4) := "0011";
		constant twisted_range: bit_vector(-2 to 1) := "0011";
		alias b_alias is b;
		alias b_alias_twisted : bit_vector (1 downto -2) is b; 
		alias b_alias_twisted_alias : bit_vector (11 to 14) is b_alias_twisted; -- alias over alias
		constant range2: bit_vector(1 downto -2) := "0011";
		variable SUM: bit_vector(1 to 4);
	begin
		sum := a + b; assert SUM = "1111" report "b: 1100+0011 must equal 1111 but we have got " & TO_STR(sum) & " instead";
		sum := a + twisted_range; assert SUM = "1111" report "twisted range: 1100+0011 must equal 1111 but we have got " & TO_STR(sum) & " instead";
		sum := a + b_alias; assert SUM = "1111" report "b_alias: 1100+0011 must equal 1111 but we have got " & TO_STR(sum) & " instead";
		sum := a + b_alias_twisted; assert SUM = "1111" report "b_alias_twisted: 1100+0011 must equal 1111 but we have got " & TO_STR(sum) & " instead";
		sum := a + b_alias_twisted_alias; assert SUM = "1111" report "b_alias_twisted_alias: 1100+0011 must equal 1111 but we have got " & TO_STR(sum) & " instead";
		sum := a + range2; assert SUM = "1111" report "range2: 1100+0011 must equal 1111 but we have got " & TO_STR(sum) & " instead";
		report "done";
		return true;
	end function;
	constant B: boolean := init;
	
begin
  
end architecture ARCH;
