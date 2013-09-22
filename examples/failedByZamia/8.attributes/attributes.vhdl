
entity ATTRIBUTES is end ;

use std.textio.all;

architecture Arch of ATTRIBUTES is

	
begin

Block Attributes


Return Boolean values (TRUE or FALSE)
 --- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

-- Block attributes : structure and behavior 
-- block_name'BEHAVIOR
   -- The attribute returns true if there are no component instantiation
   -- statements in the block or architecture.
   -- Stated more simply: If the block is coded in a purely behavioural
   -- manner this attribute returns true. A component instantiation
   -- statement is a structural coding statement.

-- block_name'STRUCTURE
   -- The attribute returns true if, within the block or architecture, all
   -- process statements or equivalent process statements are passive
   -- (i.e. do not contain signal assignments).
   
--After going through the '87 and '93 VHDL LRM, I could not find where the
--block attributes are defined.  Does anyone know in what industry standard
--block attributes were created? Nevermind.  I just realized that they were indeed created in the '87 LRM,
--but removed for the the '93 LRM.
   
   
	TEST1: block
		subtype CONSTRAINED_BV is bit_vector(1 to 3);
		type BV_2D_UNCONSTRAINED is array (natural range <>) of BIT_VECTOR;
		type BV_2D_CONSTRAINED is array (natural range <>) of CONSTRAINED_BV;
		SIGNAL v1, v2 : BV_2D_CONSTRAINED(0 to 1);
			
		function INIT return boolean is begin
		
	-- 		ERROR: must throw "Attribute IMAGE requires a scalar type mark prefix"
			-- instead of unsupported attribute IMAGE
			report BV_2D_CONSTRAINED'image(v1);
			
			report BV_2D_CONSTRAINED'path_name; -- Internal error: attribute PATH_NAME not implemented for types.
			
			return true;
			
			
		end function;
		constant b: boolean := INIT; 
	begin
	end block TEST1;
	
	TEST2: block
		subtype ST is INTEGER range 2 to 8;
		subtype SST is ST range 3 to 7;
		signal U:  SST;
		constant I: integer := SST'base'low; -- base is not implemented
		
	begin
	end block TEST2;
	
	
	
	TEST3: block
		type RT is record 
			A: bit;
		end record;
		
		signal RS: RT;
		
		function INIT return boolean is begin
			 report "rs rs rs rs rs rs rs rs rs rs rs rs ";     
		     report RS'SIMPLE_NAME; -- not implemented for values
		     report RS'PATH_NAME; -- not implemented for values
		     report RS'instance_name; -- not implemented for values
		     --Modelsim report
		--		Note: rs
		--		Note: :path_demo:rs
		--		Note: :path_demo(rtl):rs
		
			 report "rs.a rs.a rs.a rs.a rs.a rs.a rs.a rs.a rs.a rs.a rs.a rs.a ";     
		     report RS.a'SIMPLE_NAME; -- not implemented for values
		     report RS.a'PATH_NAME; -- not implemented for values
		     report RS.a'instance_name; -- not implemented for values
		     --Modelsim report
			--Note: rs, :path_demo:rs, :path_demo(rtl):rs
		end function;
		constant B: boolean := INIT;		
	begin 


		PROC: process begin
			report "proc proc proc proc proc proc proc proc proc proc ";     
			--     report PROC'SIMPLE_NAME; -- could not resolve PROC
			--     report PROC'PATH_NAME; -- could not resolve PROC
			--     report PROC'instance_name; -- could not resolve PROC
			-- Modelsim report:
			-- proc, path_demo:proc:, path_demo(rtl):proc:
			wait;
		end process;
		

	end block TEST3;
	
	TEST2: block
		signal S: boolean;
		constant B: boolean := s'driving; -- driving is not implemented
	begin
		s <= true;
		process (S) begin
			report "a'driving = " & boolean'image(S'driving); -- not implemented
		end process;
	
	end block;
end architecture;