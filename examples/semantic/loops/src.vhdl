entity LOOPS is
end entity;

use std.textio.all;
architecture Arch of LOOPS is
 
     
	subtype STR_3_to_5 is string (3 to 5);
	--subtype T2 is string (3 to 1); constant C2 : T2 := "321";
	subtype STR_3_downto_1 is string (3 downto 1);
	
		procedure exec_body(index : integer; variable counter: inout integer) is begin
			report integer'image(index);
			counter := counter +1;
		end procedure;
	
		function assertion(actual: integer; expected:integer) return boolean is begin
			assert actual = expected report "must execute the body " & integer'image(expected) & " times, actually executed " & integer'image(actual);
			return false; 
		end function;

		function iterate_array(str: string) return boolean is 
		
		function iterate_to (low: integer; high: integer; expected: integer) return boolean is
			variable counter: integer := 0;
		 begin
			report "Iterating from " & integer'image(low) & " to " & integer'image(high);
			for idx in low to high loop  
				exec_body(idx, counter); 
			end loop;
			return assertion(counter, expected);
		end function;
		
		function iterate_downto (left: integer; right: integer; expected: integer) return boolean is
			variable counter: integer := 0;
		 begin
			report "Iterating from " & integer'image(left) & " downto " & integer'image(right);
			for idx in left downto right loop
				exec_body(idx, counter);
			end loop;
			return assertion(counter, expected);
		end function;
		
	begin
		report "iterating str left=" & integer'image(str'left) & " to "  & integer'image(str'right);
		return false  
			xor iterate_to(str'low, str'high, 3)    
			xor iterate_to(str'high, str'low, 0)
			xor iterate_downto(str'low, str'high, 0)
			xor iterate_downto(str'high, str'low, 3)
			;
	end function;
	constant ARR_call : boolean := false
		xor iterate_array(STR_3_to_5'("345"))
		--xor iterate_array(C2)
		xor iterate_array(STR_3_downto_1'("321"))  
		;

	function LEFT_TO_RIGHT(str: string; expected: integer) return boolean is 
			variable counter: integer := 0;
		 begin
			report "LEFT_TO_RIGHT: iterating str left=" & integer'image(str'left) & " to "  & integer'image(str'right);
			for idx in str'left to str'right loop
				exec_body(idx, counter);
			end loop;
			return assertion(counter, expected);
	end function;
	constant LEFT_TO_RIGHT_call : boolean := false
		xor LEFT_TO_RIGHT(STR_3_to_5'("345"), 3)
		--xor iterate_array(C2)
		xor LEFT_TO_RIGHT(STR_3_downto_1'("321"), 0)
		;
		
	function DYNAMIC_RANGE(str: string; expected: integer) return boolean is 
		variable counter: integer := 0;
	 begin
		report "DYNAMIC_RANGE: iterating str left=" & integer'image(str'left) & " to "  & integer'image(str'right);
		for idx in str'range loop
			exec_body(idx, counter);
		end loop;
		return assertion(counter, expected);
	end function;
	constant DYNAMIC_RANGE_call : boolean := false
		xor DYNAMIC_RANGE(STR_3_to_5'("345"), 3)
		--xor iterate_array(C2)
		xor DYNAMIC_RANGE(STR_3_downto_1'("321"), 3)
		;
		
	type families_type is (nofamily, spartan2, virtex);
	
	function iterate_fam return boolean is 
		procedure title(low, high : families_type; direction: string) is begin
			report "from " & families_type'image(low) & direction & families_type'image(high);
		end procedure;
		procedure exec_body(fam : families_type; variable counter: inout integer) is begin
			report integer'image(families_type'pos(fam)) & " => " & families_type'image(fam);
			counter := counter +1;
		end procedure;
		function iterate_to (low: families_type; high: families_type; expected: integer) return boolean is
			variable counter: integer := 0;
		 begin
			title(low, high, " to ");
			for fam in low to high loop 
				exec_body(fam, counter);
			end loop;
			return assertion(counter, expected);
		end function;
		
		function iterate_downto (low: families_type; high: families_type; expected: integer) return boolean is
			variable counter: integer := 0;
		 begin
			title(low, high, " downto ");
			for fam in low downto high loop
				exec_body(fam, counter);
			end loop;
			return assertion(counter, expected);
		end function;
		

		function SINGLETONE_LOOP return boolean is
			type T2 is (singleton);
			variable B: boolean;
			variable counter : integer := 0;
		begin
			report "enumerating single element";
			for fam in singleton to singleton loop
				report T2'image(singleton);
				counter := counter +1;
			end loop;
			b := assertion(counter, 1);
			for fam in T2'range loop
				 report "T2 = " & T2'image(fam);
				 counter := counter + 1;
			end loop;
			b := assertion(counter, 2);
			for fam in 1 to 1 loop
				exec_body(fam, counter);
			end loop;
			return assertion(counter, 3);
		end function;
		
	begin
		report "iterating the enumerator ";
		return false 
			xor iterate_to(families_type'low, families_type'high, 3)
			xor iterate_to(families_type'high, families_type'low, 0)
			xor iterate_downto(families_type'low, families_type'high, 0)
			xor iterate_downto(families_type'high, families_type'low, 3)
			
			xor SINGLETONE_LOOP 
			;
			
	end function;
	constant FAM_call : boolean := iterate_fam;

begin

end architecture;