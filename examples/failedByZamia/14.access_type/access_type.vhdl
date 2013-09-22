entity ACCESS_DEMO is end;

library std;
use std.textio.all;

architecture ARCH of ACCESS_DEMO is

	impure function ALLOCATORS return boolean is
			type pinteger is access integer;
			variable P1: pinteger := new integer;
			-- constant P2: pinteger := new integer'(-10); -- not supported in Modelsim
			variable p2: pinteger;

			type tr is record
				f1       :  bit_vector;
				f2       :  string;
			end record tr; -- end record R1 label mismatch is passed unnoticed
			 
			type PR is access TR;
			
			--constant R1: PR := new TR'(f1 => bit_vector'("111"), f2 => string'("111")); -- not supported in modelsim
			variable R1: PR;
			
			variable L: line;
	begin
		assert p1 /= null report "p1 must be not null" severity error;
		p1.all := 10;
		assert p1.all = 10 report "p1.all must be 10" severity error;
		
		assert p2 = null report "p2 must be null" severity error;
		p2 :=  new integer'(-10);
		assert p2.all = -10 report "p2.all must be -10" severity error;
		
		R1 := new TR'(f1 => bit_vector'("111"), f2 => string'("111"));
		assert R1.all = ("111", "111") report "r1.all must be  (""111"", ""111"")" severity error;
		
		L := new string'("abc");
		assert l.all = "abc" report "l.all must be  ""abc"")" severity error;
		return true;
	end function;
	constant ALLOC_EXEC: boolean := ALLOCATORS;
	
	
	impure function STD_READ_WRITE return boolean is
		variable L, L2: line; 
	begin
		write(l, string'("123")); -- good
		l2 := l;
		report "before write: l = " & l.all & ", l2 = " & l2.all;
		write(l, string'("456")); -- good
		report "after write: l = " & l.all & ", l2 = " & l2.all;
		assert l2.all = l.all report "strings must equal but (l = " & (l.all) & ") != (l2 = " & (l2.all) & ")"; 
		return true;
	end function;
	constant STDRW_EXEC: boolean := STD_READ_WRITE;
	
	
	
begin 

end architecture;