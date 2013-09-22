
-- type attributes test

entity taTest is
	port( a, b : IN bit; z : OUT bit);
end entity taTest;

architecture RTL of taTest is 

	type alu_function is (disable, pass, add, subtract, multiply, divide);
	
	function check(title: string; actual: integer; expected:integer) return boolean is begin
		assert actual = expected report title & ": got " & integer'image(actual) & " while expected " & integer'image(expected);
		return false; 
	end function;
			
	function POS return boolean is begin
		report "computing position";
		return check("computing alu_function'pos(pass)", alu_function'pos(pass), 1);
	end function;
	constant POS_call: boolean := POS;

	 function succAttrCheck return boolean is 
		 type families_type is (nofamily, spartan2, virtex);
		 variable b: boolean;
	 begin
		 assert families_type'pos(nofamily) = 0 report "nofamily'pos must be 0" severity error;
		 assert families_type'pos(spartan2) = 1 report "spartan2'pos must be 1" severity error;
		 assert families_type'val(0) = nofamily report "families_type'val(0) must be nofamily" severity error;
		 assert families_type'val(1) = spartan2 report "families_type'val(1) must be spartan2" severity error;
		 assert families_type'succ(spartan2) = virtex report "spartan2'successor must be virtex" severity error;
		 assert families_type'pred(spartan2) = nofamily report "spartan2'pred must be nofamily" severity error;
		 b := check("1'succ", integer'succ(1), 2);
		 b := check("0'pred", integer'pred(0), -1);
		 return true;
	 end function;
	 constant SUCC_call : boolean := succAttrCheck;

	subtype TSTR_3_to_5 is string (3 to 5); 
	--subtype T2 is string (3 to 1); constant C2 : T2 := "321";
	subtype TSTR_3_downto_1 is string (3 downto 1); 

	 function leftRightTest(test_Str: string; expected: integer) return boolean is 
		 type families_type is (nofamily, spartan2, virtex);
		 variable b: boolean;
	 begin 
		 assert families_type'rightof(nofamily) = spartan2 report "nofamily'pos must be 0" severity error;
		 b := check("rightof(1)", integer'rightof(1), 2);
		 b := check("leftof(-1)", integer'leftof(-1), -2);
		 b := check("leftof(-1)", integer'leftof(-1), -2);
		 --b := check("leftof(-1)", test_Str'leftof(-1), -2);
		 return true;
	 end function;
	 constant LEFT_call : boolean := leftRightTest(TSTR_3_to_5'("abc"), 1);

begin
  
  
end architecture RTL;

