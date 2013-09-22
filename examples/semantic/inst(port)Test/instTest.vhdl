
package mypkg is

   type tr is record
      f1 : bit;
      f2 : bit;
   end record;
end package;

-- Testing must include 
--	generic and port associations (for procedures test them elsewhere)
--	entity and component instantiation
--		(thereby defaults can be specified in the entity as well in component declarations)
--	check that unconstrained array port range is derived from the actual (this function fails in zamia)  
--	type, range and direction matching
--	duplicate labels
--	error instantiating unimplemented
--	infinite descent (self-instantiation) detection 


-- TEST 1 --
-- Detects port mapping errors (mismatched types and directions) 

entity EL is
	port (
		portA: in bit_vector; --(1 to 3)
		port3In: in bit_vector(1 to 3);
		port3out: out bit_vector(1 to 3)
	);
end entity;
 
use WORK.mypkg.ALL;
entity SUBEL is
	port (portB: out bit_vector; 
		portTR: in tr;
		portI: in integer := 1
		); 
end entity;


architecture ARCH of SUBEL is begin end architecture;
        
architecture ARCH of EL is
	constant TR1: WORK.mypkg.tr := (f1 =>'1', f2 => '0');

	--ACHTUNG! "PortA is unconstrained reported" See unconstrained port in the failing tests.	
--	function RANGE_CHECK return boolean is begin
--		report "----------------------port a length is " & integer'image(PORTA'length);
--		assert PORTA'length = 3 report "portA length must be 3" severity error;
--		return true;
--	end function;
--	constant B: boolean := RANGE_CHECK;


begin

	-- number of arguments and unknown actuals/formals
  	foo: entity WORK.e1 port map ("111", portA => tr1); -- Design unit expected here.
  	foo2: entity WORK.SUBEL port map ("111", (f1 =>'1', f2 => '0')); -- ok
  	foo2: entity WORK.SUBEL port map ("111", portTR =>  (f1 =>'1', f2 => '0')); -- ok
  	foo4: entity WORK.SUBEL port map ("111", portTR => tr2); -- Couldn't resolve TR2
  	foo3: entity WORK.SUBEL port map ("111", portTR => tr1); -- ok
  	foo4: entity WORK.SUBEL port map ("111", portTR1 => tr1); -- Couldn't resolve PORTTR1
  	foo5: entity WORK.SUBEL port map ("111", portTR => tr1, portI => 1); -- ok
  	foo6: entity WORK.SUBEL port map ("111", tr1, 1); -- ok
  	foo7: entity WORK.SUBEL port map ("111", tr1, 1, 2); -- Too many positional parameters.
  	foo8: entity WORK.SUBEL port map ("111", portTR => tr1, 2); -- Illegal mix of named and poistional

	sub_TypeErr: entity SUBEL port map (1); -- Type mismatch
	sub_TypeErr2: entity SUBEL port map (porta => 1); -- type mismatch
 	sub_DirErr: entity SUBEL port map (porta); -- Direction mismatch in positional mapping
 	sub_DirErr: entity SUBEL port map (portB =>porta); -- Direction mismatch in named mapping formal
end architecture;

entity PORTCHECK is
end entity;

architecture ARCH of PORTCHECK is
begin
	-- ACHTUNG we pass a wrong port length (4) but it is passed 
	-- unnoticed
	U1: entity WORK.EL port map("101", "0011", "0101");
end architecture;

-- TEST 2 -- generic test
entity GENERIC_CHILD is
	generic (G1: integer;
		LEN :integer := 3);
	port (PORT_A: in bit_vector(1 to LEN));
end entity;
architecture ARCH of GENERIC_CHILD is begin end architecture ARCH;

entity GENERIC_CHECK is end entity;
architecture ARCH of GENERIC_CHECK is begin

	-- Achtung, Zamia fails to detect open generic G1 and 
	-- PortA length!
  	oo2: entity WORK.SUBEL port map ("111", (f1 =>'1', f2 => '0')); -- ok in Zamia, Formal generic "G1" has OPEN or no actual associated with it in modelsim

end architecture;



-- Test 3 -- component instantiation

-- Elaborated but hangs to count nodes

-- Test 3 : SELF INSTANCE -- 
--entity SELF_INSTANCE is end entity;
--architecture ARCH of SELF_INSTANCE is
--component SELF_INSTANCE end component;  
--begin
--	U1: entity SELF_INSTANCE; -- It must be an error, I think, but Zamia reports no error
--	U2: SELF_INSTANCE;
--	U3: SELF_INSTANCE1; -- expected: Couldn't resolve SELF_INSTANCE1
--end architecture;
--
--architecture ARCH3 of SELF_INSTANCE is begin
--	U1: entity SELF_INSTANCE(ARCH4);
--	U2: entity SELF_INSTANCE(abc); -- expected Couldn't find 'SELF_INSTANCE(ABC)'
--end architecture;
--
--architecture ARCH4 of SELF_INSTANCE is begin
--	U1: entity SELF_INSTANCE(ARCH3);
--end architecture;

-- TEST 4 --
entity UNIMPLEMENTED_ENTITY is end;

-- Commented architecture - entity is not implemented
--architecture ARCH of UNIMPLEMENTED_ENTITY is begin end architecture;

entity TOP is end entity;

architecture TEST4_UNIMPLEMENTED of TOP is
	component UNIMPLEMENTED_ENTITY is end component; 
	component UNEXISTING_COMPONENT is end component; 
begin
	U1: entity UNEXISTING; -- expected: Couldn't resolve UNEXISTING
	U2: entity UNIMPLEMENTED_ENTITY; -- expected: EntityInstantiation: Couldn't find 'UNIMPLEMENTED_ENTITY'
	U3: entity TOP(UNEXISTING); -- expected: EntityInstantiation: Couldn't find 'TOP(UNEXISTING)'
	
	-- Elaborated but exception (missed module) when counting nodes	
	--U4: UNIMPLEMENTED_ENTITY; -- expected: Architecture not found for WORK.UNIMPLEMENTED_ENTITY
	
	U5: UNEXISTING_COMPONENT; -- expected: Couldn't resolve UNEXISTING_COMPONENT
	
end architecture;

library LIBA;
architecture LIB_ENTITY of TOP is
begin 
	U1: entity LIBA.E6; -- expected: Design unit expected here
	U2: entity LIBA.E5; -- ok
end architecture;

