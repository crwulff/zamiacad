
-- The problem aReport=null in IGType.getEnumLiteral. 
-- I believe this NPE may appear elsewhere.

-- I've also noteced that this evaluation is deferred. 
-- Literals must be evaluated sooner.

entity TEST1_LOCAL_LITERAL_NPE is end;

architecture bench of TEST1_LOCAL_LITERAL_NPE is

	-- The following produces NPE whereas "22" cannot be assigned 
	-- to bitvector is expected.
	constant A: bit_vector(1 to 2) := "22";
    
begin

	u1: entity TEST1_LOCAL_LITERAL_NPE generic map ("111", 1); -- redicously supplying generics to nowhere does not fire anything
end bench;


entity TEST2_CHILD is 
	generic (
		a, b: bit_vector(1 to 3)
	);
end entity;

architecture ARCH of TEST2_CHILD is
	-- NB! BECAUSE OF NPE this is not called an error is not noticed
	function CHECK return boolean is begin
		report "GENERIC_A must be 111";
		assert A = B report "GENERIC_A must be 111" severity error;
		return true;
	end function;
	constant B: boolean := CHECK; 
begin end architecture;

entity TEST2_INST_GENERIC is end entity ;

architecture bench of TEST2_INST_GENERIC is begin

	-- the problem is that this causes no failure
	u1: entity TEST2_CHILD generic map ("fff", "111");
	
	-- Neither this causes any failure 
	--u2: entity TEST2_CHILD generic map ("ffff"); 
end bench;
