-- Here instance generics are tested (add procedure generics)
-- Instance ports are tested elsewhere


-- Component generics are ingored as described below.

package checker is
	function check(title: string; actual: integer; expected: integer) return boolean;
end package;

package body checker is
	function check(title: string; actual: integer; expected: integer) return boolean is
	begin
 		assert actual = expected report title & ": " & integer'image(actual) & " must equal " & integer'image(expected) severity ERROR;
		return true;
	end function; 
end package body;
	 
entity ENTITY_INSTANCE_MANDATORY is
  generic (title: string; actual: integer; expected: integer);
end ENTITY_INSTANCE_MANDATORY;

entity ENTITY_INSTANCE_OPTIONAL is
  generic (title: string; expected: integer; actual: integer := 10);
end ENTITY_INSTANCE_OPTIONAL;

use work.checker.check;
architecture arch of ENTITY_INSTANCE_MANDATORY is
	constant DUMMY: boolean := work.checker.check(title, actual, expected);
begin end architecture;

use work.checker.check;
architecture arch of ENTITY_INSTANCE_OPTIONAL is
	constant DUMMY: boolean := work.checker.check(title, actual, expected);
begin end architecture;

entity ENTITY_INSTANTIATOR is end; 

architecture RTL of ENTITY_INSTANTIATOR is begin

   u1 : entity work.ENTITY_INSTANCE_MANDATORY
	      generic map(title => "entity: argument supplied to mandatory",
		     actual => 1, expected => 1
	      );
 
   u2 : entity work.ENTITY_INSTANCE_OPTIONAL
	      generic map(title => "entity: argument supplied to optinal",
		     actual => 1, expected => 1
	      );
	      
   u22 : entity work.ENTITY_INSTANCE_OPTIONAL
	      generic map(title => "planned error: intentionally wrong expected,2, is specified instead of correct 1",
		     actual => 1, expected => 2
	      );
  
 -- This fails at the check but it should fail at the generic association
--   u3 : entity work.ENTITY_INSTANCE_MANDATORY
--	      generic map(title => "entity: mandatory is not specified -- error is expected",
--		     expected => 10 --actual => 1, 
--	      );
	      
   u4 : entity work.ENTITY_INSTANCE_OPTIONAL
	      generic map(title => "entity: optional is not specified",
		     expected => 10 --actual => 1, 
	      );
 
end;

 
entity COMP_INSTANTIATOR is end entity;

architecture Arch of COMP_INSTANTIATOR is
	component ENTITY_INSTANCE_MANDATORY is
	  generic (title: string; actual: integer := 55; expected: integer);
	end component ;
 
	component ENTITY_INSTANCE_OPTIONAL is
	  generic (title: string; actual: integer := 45; expected: integer);
	end component ;
begin

   u1 : ENTITY_INSTANCE_MANDATORY
	      generic map(title => "component: argument supplied to mandatory",
		     actual => 1, expected => 1
	      );
 
 -- This test fails. Find it in the list of failing tests to be fixed. 
--   u2 : ENTITY_INSTANCE_MANDATORY
--	      generic map(title => "component: argument not supplied to mandatory",
--		     expected => 55 -- there must be no error but zamia says "no error when expected = 0"  
--	      );
 
   u3 : ENTITY_INSTANCE_OPTIONAL
	      generic map(title => "component: argument supplied to optinal",
		     actual => 1, expected => 1
	      );

-- This test fails. Find it in the list of failing tests to be fixed. 
--   u4 : ENTITY_INSTANCE_OPTIONAL
--	      generic map(title => "component: argument not supplied to optinal",
--		     expected => 45 -- there must be no error buts zamia says "no error when expected = 10"
--	      );

end architecture Arch;

entity TOP_INSTANTIATOR is end entity;

architecture ARCH of TOP_INSTANTIATOR is begin
	u2: entity work.ENTITY_INSTANTIATOR;
	u1: entity work.COMP_INSTANTIATOR;
		
	
end architecture ARCH;
