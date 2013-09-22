-- Tests two failures: 
	-- component generic must override the entity generic
	-- open entity generic must not fail silently
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

entity COMP_INSTANTIATOR is end entity;

architecture Arch of COMP_INSTANTIATOR is
	component ENTITY_INSTANCE_MANDATORY is
	  generic (title: string; actual: integer := 55; expected: integer);
	end component ;
 
	component ENTITY_INSTANCE_OPTIONAL is
	  generic (title: string; actual: integer := 45; expected: integer);
	end component ;
begin

	-- Error behaviour: fails at check function instead of at generic association. 
	-- Associates 0 to the open generic. 
 	u1 : entity ENTITY_INSTANCE_MANDATORY
	      generic map(title => "instantiating mandatory component",
		     expected => 35 -- actual is considered 0 in zamiacad
	      );
	      
	-- Error behaviour: fails at check function after associated 0 to the 
	-- component that has defualt = 55.
   u2 : ENTITY_INSTANCE_MANDATORY
	      generic map(title => "instantiating mandatory component",
		     expected => 55 -- actual is considered 0 in zamiacad
	      );

	-- Error behaviour: fails at check function after associated entity default of 10  
	-- to the component that overrides it to 55.
   u4 : ENTITY_INSTANCE_OPTIONAL
	      generic map(title => "instantiating optional component",
		     expected => 45 -- actual is considered 10 (entity default) in zamiacad
	      );

end architecture Arch;

entity NO_GENERIC is end;
architecture ARCH of NO_GENERIC is begin end;

entity TOP_INSTANTIATOR is end entity;

architecture ARCH of TOP_INSTANTIATOR is begin
	u1: entity work.COMP_INSTANTIATOR;
	
	-- This is actually entity instantiation problem rather than component instantiation problem
	u2: entity work.NO_GENERIC generic map(1); -- It has not generic, must fail!
	u3: entity work.NO_GENERIC generic map(1,1); -- It has not generic, must fail!
	-- Modelsim says "Number of positional association elements (1) exceeds number of formals"
	
end architecture ARCH;
