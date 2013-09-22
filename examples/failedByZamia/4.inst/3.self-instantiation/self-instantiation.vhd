-- Test 3 -- component instantiation

-- Elaborated but hangs to count nodes

-- Test 3 : SELF INSTANCE -- 
entity SELF_INSTANCE is end entity;
architecture ARCH of SELF_INSTANCE is
component SELF_INSTANCE end component;  
begin
	U1: entity SELF_INSTANCE; -- It must be an error, I think, but Zamia reports no error
	U2: SELF_INSTANCE;
end architecture;

architecture ARCH3 of SELF_INSTANCE is begin
	U1: entity SELF_INSTANCE(ARCH4);
end architecture;

architecture ARCH4 of SELF_INSTANCE is begin
	U1: entity SELF_INSTANCE(ARCH3);
end architecture;



entity UNIMPLEMENTED_ENTITY is end;

-- Commented architecture to highlight that entity is not implemented
--architecture ARCH of UNIMPLEMENTED_ENTITY is begin end architecture;

entity TOP is end entity;

architecture TEST4_UNIMPLEMENTED of TOP is
	component UNIMPLEMENTED_ENTITY is end component; 
	component UNEXISTING_COMPONENT is end component; 
begin

	-- error is expected in elaboration but does not cause errors 
	-- in node counting
	U2: entity UNIMPLEMENTED_ENTITY;
	
	-- Elaborated but exception (missed module) when counting nodes	
	U4: UNIMPLEMENTED_ENTITY; -- expected: Architecture not found for WORK.UNIMPLEMENTED_ENTITY
	
end architecture;
