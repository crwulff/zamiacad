--LRM:
--The shift operators sll, srl, sla, sra, rol, and rorare defined for any one-dimensional array type whose 
--element type is either of the predefined types BIT or BOOLEAN.


package SRA_PKG is 

			function SRA_FUNC(L: bit_vector; R: integer) return bit_vector;
			function SLA_FUNC(L: bit_vector; R: integer) return bit_vector; 
			function ROL_FUNC(L: bit_vector; R: integer) return bit_vector;
			function ROR_FUNC(L: bit_vector; R: integer) return bit_vector; 
			function SLL_FUNC(L: bit_vector; R: integer) return bit_vector;
			function SRL_FUNC(L: bit_vector; R: integer) return bit_vector; 
			
end package;

package body SRA_PKG is

			function ROR_FUNC(L: bit_vector; R: integer) return bit_vector is
			begin
				if R < 0 then return ROL_FUNC(L, -R); else
					return L(R-1 downto L'low) & L(L'high downto R);
				end if;
			end function;
			
			function ROL_FUNC(L: bit_vector; R: integer) return bit_vector is
			begin
				if R < 0 then return ROR_FUNC(L, -R); else
					return L(L'high-R downto L'low) & L(L'high downto L'length-R);
				end if;
			end function;
			
			function SLL_FUNC(L: bit_vector; R: integer) return bit_vector is 
				subtype TZERO_FILLER is bit_vector(abs(R)-1 downto 0);
				constant ZEROES: TZERO_FILLER := (others => '0');
			begin
				if R < 0 then return SRL_FUNC(L, -R); else
					return L(L'high-R downto L'low) & ZEROES;
				end if;
			end function;
			
			function SRL_FUNC(L: bit_vector; R: integer) return bit_vector is 
				subtype TZERO_FILLER is bit_vector(abs(R)-1 downto 0);
				constant ZEROES: TZERO_FILLER := (others => '0');
			begin
				if R < 0 then return SLL_FUNC(L, -R); else
					return ZEROES & L(L'high downto R);
				end if;
			end function;
			
			
			function SRA_FUNC(L: bit_vector; R: integer) return bit_vector is
				subtype SA_FILLER is bit_vector(abs(R)-1 downto L'low); 
			begin
				if R < 0 then return SLA_FUNC(L, -R); else
					return SA_FILLER'(others => L(L'high)) & L(L'high downto R);
				end if;
			end function;
			function SLA_FUNC(L: bit_vector; R: integer) return bit_vector is
				subtype SA_FILLER is bit_vector(abs(R)-1 downto L'low); 
			begin
				if R < 0 then return SRA_FUNC(L, -R); else
					return L(L'high-R downto L'low) & SA_FILLER'(others => L(L'low));
				end if;
			end function;
end package body;

--library ieee;
--use ieee.std_logic_1164.all;
use WORK.SRA_PKG.all; 



entity TEST_BV is
	port (vector_spec: bit_vector
		(8 downto 0) -- this range must be included because of the bug, see in the instantiation-port failing tests
	);
end TEST_BV;

architecture ARCH of TEST_BV is

	signal B_ROL, B_SLA, B_SLL, B_ROL_ref, B_SLA_ref, B_SLL_ref: bit_vector(vector_spec'range);
	signal B_ROR, B_SRA, B_SRL, B_ROR_ref, B_SRA_ref, B_SRL_ref: bit_vector(vector_spec'range);
	signal R: integer;
	 --sll | srl | sla | sra | rol | ror
begin

	process

		procedure TEST(L: bit_vector; R: integer) is
		begin
			B_SLA 	  <= L;	B_SRA 	  <= L;	B_ROL 	  <= L; B_ROR 	  <= L; B_SLL 	  <= L; B_SRL 	  <= L;
			B_SLA_ref <= L; B_SRA_ref <= L;	B_ROL_ref <= L; B_ROR_ref <= L; B_SLL_ref <= L; B_SRL_ref <= L;  
			wait for 2 ns;
			for I in L'low to l'high + 1 loop
			
				B_SLL <= B_SLL sll R; -- not simulated in Zamia
				B_SLL_ref <= SLL_FUNC(B_SLL_ref, R);
				 
				B_SRL <= B_SRL srl R; -- not simulated in Zamia
				B_SRL_ref <= SRL_FUNC(B_SRL_ref, R);

				B_SLA <= B_SLA sla R;
				B_SLA_ref <= SLA_FUNC(B_SLA_ref, R);
				
				B_ROR <= B_ROR ror R;
				B_ROR_ref <= ROR_FUNC(B_ROR_ref, R);
				
				B_ROL <= B_ROL rol R;
				B_ROL_ref <= ROL_FUNC(B_ROL_ref, R);
				
				B_SRA <= B_SRA sra R;
				B_SRA_ref <= SRA_FUNC(B_SRA_ref, R);
				wait for 1 ns;
				
				assert B_SLL = B_SLL_ref report "B_SLL is different from B_SLL_ref" severity error;
				assert B_SRL = B_SRL_ref report "B_SRL is different from B_SRL_ref" severity error;
				assert B_SRA = B_SRA_ref report "B_SRA is different from B_SRA_ref" severity error;
				assert B_SLA = B_SLA_ref report "B_SLA is different from B_SLA_ref" severity error;
				assert B_ROL = B_ROL_ref report "b_ROL is different from b_ROL_ref" severity error;
				assert B_ROR = B_ROR_ref report "B_ROR is different from B_ROR_ref" severity error;
			end loop;
		end procedure;
		type TII is array (1 to 6) of integer;
		constant II: TII := (1, 2, 5, -2, -5, -1);
		
	begin
		for I in II'range loop
			R <= II(I);
			TEST("111100000", II(I));
			TEST("100000000", II(I));
			TEST("000011111", II(I));
			TEST("000000001", II(I));
		end loop;
		report "ROL/ROR simulation done";
		wait;
	end process;
--	assert B_SLA = B_SLA_ref report "B_SLA is different from B_SLA_ref" severity error;
	
end architecture; 

entity ROL_TB is 
end ROL_TB;

use WORK.SRA_PKG.all; 
architecture ARCH of ROL_TB is


	signal downto_spec: bit_vector (8 downto 0);
	signal to_spec: bit_vector (0 to 8);
	
	
  	type boolean_vector is array (integer range <>) of boolean;
	function BOOL_TEST return boolean is
		function TO_BOOL(a: bit_vector) return BOOLEAN_VECTOR is 
			variable RESULT: BOOLEAN_VECTOR(a'range);
		begin
			for I in a'range loop
				if a(I) = '1' then
					RESULT(I) := true;
				else 
					RESULT(I) := false;
				end if;
			end loop;
			return RESULT; 
		end function;
		function TO_STR(a: BOOLEAN_VECTOR) return string is 
			variable RESULT: string(a'range);
		begin
			for I in a'range loop
				if a(I) then
					RESULT(I) := '1';
				else 
					RESULT(I) := '0';
				end if;
			end loop;
			return RESULT; 
		end function;
		procedure check(actual, expected: boolean_vector) is begin
			assert actual = expected report "B_to must equal "&to_str(expected)&" but it is " & to_str(actual) severity error;
		end procedure;
		
		procedure check(b: boolean_vector) is begin
			check(B sll 1, TO_BOOL("111100000"));
			check(B rol 1, TO_BOOL("111100001"));
			check(B sla 1, TO_BOOL("111100000"));
			check(B srl 1, TO_BOOL("011111000"));
			check(B sra 1, TO_BOOL("111111000"));
			check(B ror 1, TO_BOOL("011111000"));
			
			check(B sll 2, TO_BOOL("111000000"));
			check(B rol 2, TO_BOOL("111000011"));
			check(B sla 2, TO_BOOL("111000000"));
			check(B srl 2, TO_BOOL("001111100"));
			check(B sra 2, TO_BOOL("111111100"));
			check(B ror 2, TO_BOOL("001111100"));
			
			check(B sll 200, TO_BOOL("000000000"));
			check(B sla 200, TO_BOOL("000000000"));
			check(B srl 200, TO_BOOL("000000000"));
			check(B sra 200, TO_BOOL("111111111"));
			check(B ror 200, TO_BOOL("001111100"));
			check(B rol 200, TO_BOOL("111000011"));
			
			check(B sll -1, TO_BOOL("011111000"));
			check(B sla -1, TO_BOOL("111111000"));
			check(B rol -1, TO_BOOL("011111000"));
			check(B srl -1, TO_BOOL("111100000"));
			check(B sra -1, TO_BOOL("111100000"));
			check(B ror -1, TO_BOOL("111100001"));
		end procedure;
		
		constant BB: boolean_vector(0 to 8) := TO_BOOL("111110000");
		
		constant pos_to: boolean_vector(0 to 8) := BB;
		constant pos_downto: boolean_vector(8 downto 0) := BB;
		constant down_9_1: boolean_vector(9 downto 1) := BB;
		constant to_1_9: boolean_vector(1 to 9) := BB;
		constant mid_to: boolean_vector(-4 to 4) := BB;
		constant mid_downto: boolean_vector(4 downto -4) := BB;
		constant neg_to: boolean_vector(-8 to 0) := BB;
		constant neg_downto: boolean_vector(-0 downto -8) := BB;
	begin
		check(pos_to);
		check(pos_downto);          
		check(to_1_9);
		check(down_9_1);
		check(mid_to);
		check(mid_downto);
		check(neg_to);
		check(neg_downto);
		report "elaboration has finished";
		return true;
	end function;
	constant B: boolean := BOOL_TEST;
		
	
	signal S: string(1 to 8); -- should not be defined
	 
begin
	 
	
	--S <= S sll 1 after 1 ns;
	TO_TEST: entity WORK.TEST_BV port map(downto_spec);
	DOWNTO_TEST: entity WORK.TEST_BV port map(to_spec);
	
end architecture;
