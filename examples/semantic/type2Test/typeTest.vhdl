entity typeTest is
  port( a, b : IN bit; z : OUT bit);
end entity typeTest;

architecture RTL of typeTest is 

-- this example finally failed on Dec 2012
-- IGObjectDriver: Internal error: tried to assign unconstrained array value to an array

--  type b4v is array (natural range <>) of bit_vector;
--
--  SIGNAL v1 : b4v(0 to 1);
--  SIGNAL v2 : b4v(0 to 1);
--  SIGNAL v3 : bit_vector(0 to 3);
--
--begin
--
--  v1 <= v3 & v2(0);

-- I had to constrain the test
	subtype CONSTRAINED_BV is bit_vector(1 to 3);
	type BV_2D_UNCONSTRAINED is array (natural range <>) of BIT_VECTOR;
	type BV_2D_CONSTRAINED is array (natural range <>) of CONSTRAINED_BV;

	SIGNAL v1, v2 : BV_2D_CONSTRAINED(0 to 1);
	SIGNAL v3 : CONSTRAINED_BV := (others => '1');

begin

	-- minor ERROR: must take value 111000 AFTER simulation starts,
	-- not during elaboration.
	v1 <= v3 & v2(0);

	process begin
-- 		ERROR: must throw "Attribute IMAGE requires a scalar type mark prefix"
		-- instead of unsupported attribute IMAGE
		report BV_2D_CONSTRAINED'image(v1);
	end process;
	
end architecture RTL;


