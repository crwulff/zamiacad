entity TYPE_OVERRIDE is
end entity;

use std.textio.all;

--Demonstrates the problem that we do not have true
-- elaboration for built-in types. Every time, we obtain them
-- by string name rather than mantain the java-references
-- in the system.

architecture Arch of TYPE_OVERRIDE is

	function TYPE_OVERRIDE return boolean is
		-- overriding system types will cause all findXXXType to fail in zamia. 
		--, like IF, range(direction) and ASSERT to fail.
		type boolean is (b1, b2, b32); -- this will cause a fail below
		type string is (s1, s2, s3); -- this also causes the same failure
	begin
		assert false report "ok " severity NOTE; -- failure due to overriden boolean type
		return true;
	end function;

	-- this does not fail because readline requests integer in
	-- the std.readline context, which contains the build-in types.
	impure function INT_OVERRIDE return boolean is
			variable L: line;
		type INTEGER is (i1, i2, i3);
		file INPUT : TEXT open READ_MODE is "BuildPath.txt";--"STD_INPUT";
	begin
        readline(INPUT, l);
		return true;
	end function;

	constant TYPE_RESOL_CONST: boolean := TYPE_OVERRIDE or INT_OVERRIDE;

begin

	process
		type time is (t1, t2, t3); -- causes wait for below to fail.
	begin
		report "time override";
		wait for 0 ns; -- this fails because time type was overriden
		wait;
	end process;
end architecture;