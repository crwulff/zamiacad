---- Pay attention to these ERROR and WARNING lines - expanded names are supported unsufficiently.
--entity ALIAS_ENTITY is 
-- port (
--		I: bit;
--		O1, O2: out  bit := '0'
--	);
--end;
--	   
--package P is 
--	constant C: bit := '0';
--end P;
 
entity SELECTED_NAMES is
end entity;

--use work.P.all;


architecture ARCH of SELECTED_NAMES is
constant C : bit := '1';
----constant I_C: integer := C; -- type mistmatch detected
--alias A_I_C: integer is C; -- ERROR: must mismatch not detected
----alias ALbit: bit is <<constant C: bit>>; -- ok, unnecessry type detected
--alias AL is <<constant C: bit>>; -- ERROR: external name not elaborated
--signal ES: bit := <<constant .A:bit>>; -- ERROR: external not elaborated
--signal ES: bit := <<constant a.A:bit>>; -- ERROR: external not elaborated
--signal ES: bit := <<constant .a.A:bit>>; -- ERROR: external not elaborated

--signal S: bit := A;
constant BV: bit_vector(3 downto 0) := "1111";

begin 


	B: block 
		constant C: string := "B.C";
		constant T: bit := BV(1); 
	begin
		B: block 
			constant C: string := "B.B.C";
		begin
			B: block 
				constant C: string := "B.B.B.C";
			begin
				B1: block 
					constant B1_C: string := "B.B.B.B1.C";
				begin
					B2: block
						constant B2_C: string := "B2_C"; 
						alias A_B1_C is B1_C; -- OK
						alias A_B1_B1_C is B1.B1_C; -- <<< The ROOT ERROR is here. B1 must be resolved
						function FUNC_A_B1_B1_C return boolean is begin
							report B1.B1_C;
							--report "A_B1_B1_C =" & A_B1_B1_C;
							assert A_B1_B1_C = "B.B.B.B1.C" report "A_B1_B1_C must equal B.B.B.B1.C" severity failure;
							return true;
						end function;  
						constant EXEC_A_B1_B1_C : boolean := FUNC_A_B1_B1_C;
--						alias A_B1_B2_C is B1.B2.B2_C; -- ERROR: must be ok
--						--alias A_B_B2_C is B.B2.B2_C; -- Must be: "unknown B2"	 
--						
--						alias B_B_C is B.C; -- this works in Active-HDL
--						alias B_B1_C is B.B1.B1_C; -- this works in Active-HDL
--						--alias B_B_C is B.B.C; -- "Unknown identifier 'B'" in Active-HDL, Cannot find expanded name "b.b" in Modelsim
--						alias B_B_B_C is B.B.B.C; -- "Unknown identifier 'B'" in Active-HDL, Cannot find expanded name "b.b" in Modelsim
--						
--						-- this means that it locates the expanded name root, B, going upwards. Then, it goes downstairs, 
--						-- ensuring that suffixes match. Therefore, only innermost root is attempted.

					begin
					end block;
				end block;
			end block;
		end block;
	end block;
		
	D: block 
		signal C: bit;
	begin
	end block;
	
--	ALIAS_TB.S <=ALIAS_TB.C; -- ok
--	
--	--B1: block begin end block;
--	
--	B2: entity unisim.BUF port map(I => '1', O => open);
--
--	B1: block -- ERROR: must be identifier conflict (B1 is already declared in this scope)
--		constant C: bit := '1';
----		alias A_ALIAS: bit is 
----			--ALIAS_TB.C1 -- WARNING: "couldn't resolve ALIAS_TB.B1" -- must be "couldn't resolve B1"
----			-- ALIAS_TB1.C -- WARNING: "couldn't resolve ALIAS_TB1.B" -- must be "couldn't resolve ALIAS_TB1"
----			C
----			;
--		signal A: bit := ALIAS_TB.C; -- ok
--	begin
--		S <= B1.C; -- ERROR: couldn't resolve B1.A -- must be OK
--		S <= B1.B;-- ERROR: counld'n resolve B1.B -- must be 'unknown identifier B'
--		S <= B2.B;-- ERROR: counld'n resolve B2.B -- must be 'unknown identifier B2'
--		S <= work.ALIAS_TB.B1.A; -- ERROR: couldn't resolve work.ALIAS_TB.B1.A -- must be OK
--	end block;
--	
--	G: for I in 1 to 2 generate
--		constant A, B: bit_vector(1 downto 0) := "11";
--		alias G_A is 
--			G.A-- ERROR: counld'n resolve G.A -- Must be OK.
--			; 
--		alias A_E is <<constant ^.^.ALIAS_TB.C: bit>>;
--		constant A_E2 : bit := <<constant ^.^.ALIAS_TB.C: bit>>;
----		constant G1_STRING : string := 
----			--ALIAS_TB.G.A
----			<<constant .ALIAS_TB.C: bit_vector(1 downto 0)>>
----			'path_name; -- ERROR (with respect to AVHDL, but this does not seem demamded in VHDL synthax): not implemented
----			--'path_name1; -- OK: unknown attrubute
----		constant G1_INDEXED : bit := 
----			--ALIAS_TB.G.A
----			<<constant .ALIAS_TB.G(2).A: bit_vector(1 downto 0)>>
----			(1); -- ERROR (with respect to AVHDL, but this does not seem demanded in VHDL synthax): not implemented
----			--'path_name1; -- OK: unknown attrubute
--	begin
--	end generate;
	
end architecture;

