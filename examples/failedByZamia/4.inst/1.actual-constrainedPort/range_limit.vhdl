-- Instance elaboration says that PortA is unconstrained
-- Despite spec says that constraint must be derived from 
-- the port association actual when formal in is not specified

-- LRM: 
--For an interface object or subelement whose mode is in, inoutor linkage, if the actual part 
--includes a conversion function or a type conversion, then the result type of that function or 
--the type mark of the type conversion shall define a constraint for the index range corresponding to the index range of the object, and the index range of the object is obtained 
--from that constraint; otherwise, the index range is obtained from the object or value 
--denoted by the actual designator.
--— For an interface object or subelement whose mode is out, buffer, inout, or linkage, if the 
--formal part includes a conversion function or a type conversion, then the parameter subtype of that function or the type mark of the type conversion shall define a constraint for 
--the index range corresponding to the index range of the object, and the index range is 
--obtained from that constraint; otherwise, the index range is obtained from the object 
--denoted by the actual designator.

-- Zamia can constrain locals in the procedures. Borrow 
-- implementation from there.

entity RANGE_LIMIT is
end entity;

entity ELEMENT is
	port (portA: in bit_vector 
		 -- (1 to 3) -- Errors are gone if I uncomment this line
	);
end entity;
 
--entity subelement is port (portB: in bit_vector); end entity;
--architecture ARCH of subelement is 
--	constant I: integer:= portB'length; -- error in zamia
--	function INIT return boolean is begin
--		assert i = 3 report "portB length must be 3, got " & integer'image(I) & " instead" severity failure;
--		report "sub-sub has initialized, length = " & integer'image(I);
--		return true;
--	end function;
--	constant b: boolean := init;
--
----	-- Here is a Modelsim bug. It does not raise errors fired 
----	-- during submodule elaboration.	
----	function INIT2 return boolean is begin
----		assert false report "failure" severity failure;
----		assert false report "error" severity error;
----		assert false report "warning" severity warning;
----		assert false report "note" severity note;
----		report "main initialized";
----		return true;
----	end function;
----	constant b2: boolean := init2;	
--	
--begin end architecture;
        
architecture ARCH of ELEMENT is

	-- Evaluating portA manifests the fact that
	-- association fails 
	constant I: integer:= portA'length; 
	
	
--	function INIT return boolean is begin
--		assert i = 3 report "portA length must be 3, got " & integer'image(I) & " instead" severity failure;
--		report "sub has initialized, length = " & integer'image(I);
--		return true;
--	end function;
--	constant b: boolean := init;
--	signal C_signal: bit_vector(portA'range) :=
--		-- portA; -- is not allowed since portA signal value is uncertain until after design is elaborated.   
--		(others => '0'); -- reproduces the errors
--	--signal C_signal: bit_vector(1 to 3) := (others => '0'); -- does not reproduce all errors
begin 
--	C_signal <= portA after 10 ns; -- TODO: make simulator test, it used to be unconstrained
-- 	sub1: entity work.subelement port map (portB =>porta);
end architecture;

architecture ARCH of RANGE_LIMIT is
--	function TT (paramA: bit_vector) return integer is
--		constant I: integer := paramA'length;
--	begin  
--		return I;
--	end function;
--	constant B: integer := TT("111");

--	subtype bt is bit_vector(1 to 3);
--	constant c2 : bt := "111";    
	--type T1 is range 1 to 3; constant B: t1 := 4; 
	--constant B2 : t1 := -1;
begin
	-- Error 1: IGMapStmt: actual is unitialized.
	U1: entity WORK.ELEMENT port map("101");
--	U2: entity WORK.ELEMENT port map("111"); -- lets see if another instantiation causes two block/entity elaborations
            
--	B1: block 
--		signal A: bit_vector(1 to 3) := "010";
--		signal A2: bit_vector(1 to 3);
--	begin
--		 --Error 2 in simulator at 10 ns:
--		 --Tried to assign object from incompatible array value 000: 
--		 --ARRAY  0 to 2147483647 OF BIT vs ARRAY  1 to 3 OF BIT
--		U2: entity WORK.ELEMENT port map(A);
--		U3: entity WORK.ELEMENT port map(A2);
--	end block;
end architecture;