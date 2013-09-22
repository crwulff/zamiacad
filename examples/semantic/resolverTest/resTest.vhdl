entity resTest is
  port (a : in bit; y : out bit);
end entity resTest;

architecture RTL of resTest is 

	function OP_OVERRIDE return boolean is
		variable executed : boolean := false;
		impure function "<=" (L, R: integer) return BOOLEAN is
		begin
			report "executing";
			executed := true;
			return true;
		end "<=";
		
		constant A: integer := 2;
		constant B: boolean := A <= 3;
	begin
		assert executed report "operator must be overriden" severity error;
		return true; 
	end function;
	constant OP_OVERRIDE_EXECUTOR: boolean := OP_OVERRIDE;

begin

  y <= RESTEST.a;

end architecture RTL;

