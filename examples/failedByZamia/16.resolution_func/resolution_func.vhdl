-- Modelsim "vcom -2008" and "vsim -novopt"
-- uncomment the lines with "resolution must fail"

-- Zamia does not support user defined resolution functions
-- If there are no drivers, signal has initial value. Otherwise, a resolutioin function is called for 
-- every driver, initialized to the signal initial value. Therefore, if we have a summing resolution
-- then signal s3: sum integer := 3 will have initial value of 6 if there are two drivers, 3 if there is 
-- one driver and 3 if there is no drivers. 


package P is
	function STR(vector: bit_vector) return string;
end package;

use std.textio.all;
package body P is
	function STR(vector: bit_vector) return string is
		variable l: line;
	begin
		for I in vector'range loop
			write (l, bit'image(vector(i)));
		end loop;
		return l.all;		
	end function;
end package body;


use WORK.P.all;
architecture ARCH of TOP is


	type int_vector is array (integer range<>) of integer;
	function driver_counter( values : int_vector ) return integer is
		variable result : integer := 0;
		variable l: line;
	begin
		for index in values'range loop
			if values(index) /= 0 then
				result := result + 1;
				write (l, integer'image(values(index)) & ",");
			end if;
		end loop;
		--report l.all & " count resolved => " & integer'image(result);
		deallocate(l);
		return result;
	end function;
	
	function sum( values : int_vector ) return integer is
		variable result : integer := 0;
		variable l: line;
	begin
		write (l, string'("0"));
		for index in values'range loop
			result := result + values(index);
			write (l, "+" & integer'image(values(index)));
		end loop;
		--report l.all & " sum resolved => " & integer'image(result);
		deallocate(l);
		return result;
	end function;

	function nor_bits( values : in bit_vector )	return	bit is
		variable	result : bit := '0';
		variable l: line;
	begin
		for index in values'range loop
			write (l, bit'image(values(index)) & " ");
			result := result or values(index);
		end loop;
		result := not result;
		--report integer'image(values'length) & " bits," & l.all & "resolved as " & bit'image(result); 
		deallocate(l);
		return result;
	end function;
	
	subtype nor_resolved_bit is nor_bits  bit;
	subtype nor_resolved_bvector is (nor_bits) bit_vector; 	
	type bstrings is array(natural range<>) of bit_vector; -- vcom -2008
	
	function or_bvectors( values : bstrings) return bit_vector is
		variable	result : bit_vector(values'element'range)	:= (others=> '0');
		variable l: line;
	begin
		for index in values'range loop
			write (l, "(" & str(values(index)) & ")");
			result := result or values(index);
		end loop;
		--report l.all & " resolved as " & str(result);
		deallocate(l);
		return result;
	end function;
	
	subtype or_resolved_vector_of_nor_resolved_bits is or_bvectors nor_resolved_bvector; -- at which level will this be resolved?
	
	
	type donePositions is (IntDone, BitVecDone, RecDone, RecFieldDone);
	type DoneType is array (donePositions) of boolean;
	signal Done: DoneType := (others => false); 
	
begin 

	INT_RESOLUTION: block
		-- resolution function is also invoked during initialization (if there are drivers, https://groups.google.com/forum/#!topic/comp.lang.vhdl/vdiA7YTpZJI). 

		constant c1: driver_counter  integer := 3;
		signal s2: sum  integer := 4;
		subtype resolved_integer is sum integer;
		signal s3: resolved_integer := 3;
	begin
	
		process begin
			wait for 10 ns; s3 <= 10; s3 <= 20; wait;
		end process;
			
		process begin
			wait for 20 ns; s3 <= 30; s3 <= 40;
			wait;
		end process;
		
		CHECKER: process begin
			wait for 5 ns; assert s3 = 6 report "it is expected that s3 = 6 at " &time'image(now)& ", yet got s3=" & integer'image(s3) & " instead";
			wait for 10 ns; assert s3 = 23 report "it is expected that s3 = 23 at " &time'image(now)& ", yet got s3=" & integer'image(s3) & " instead";
			wait for 10 ns; assert s3 = 60 report "it is expected that s3 = 60 at " &time'image(now)& ", yet got s3=" & integer'image(s3) & " instead";
			Done(intDone) <= true;
			wait; 
		end process;
		
	end block;
	
--	Resolved_composite_signal_does_not_have_every_subelement_driven: block
--		subtype or_resolved_bvector is or_bvectors bit_vector;
--		signal ResOfUnres : or_resolved_bvector(1 to 4) := "0001";
--	begin
--		ResOfUnres(1 to 2) <= "11"; -- resolution must fail at sim time
--	end block;
	
	URES_OF_RES: block
	
		-- ResOfRes is resolved by the top-level resolution function, OR, instead of bit-level, NOR
		subtype or_resolved_vector_of_nor_resolved_bits is or_bvectors nor_resolved_bvector;
		signal ResOfRes: or_resolved_vector_of_nor_resolved_bits (1 to 3) := "011";
		
		
		signal UResOfRes: nor_resolved_bvector(1 to 3) := "011";
		procedure CHECK(t: time; expected: bit_vector(1 to 3)) is begin
			wait for t;
			-- Since ResOfRes and UResOfRes are resolved by opposite logic function, or and nor, 
			-- they must have the opposite values unless there is at least one driver (as it is our case)
			assert UResOfRes = expected report "at time "&time'image(now)&", UResOfRes = "& str(UResOfRes)& " fails to equal " & str(expected) severity ERROR;
			assert ResOfRes = not expected report "at time "&time'image(now)&", ResOfRes = "& str(ResOfRes)& " fails to equal " & str(not expected) severity ERROR;
		end procedure;
		
	begin
	 
		ResOfRes <= "001" after 10 ns;
		ResOfRes <= "111" after 30 ns;
		
		UResOfRes <= "001" after 10 ns;
		UResOfRes <= "111" after 30 ns;
		
		CHECKER: process begin
			CHECK(0 ns, "100");
			CHECK(11 ns, "100");
			ResOfRes <= "100"; UResOfRes(1) <= '1'; 
			CHECK(1 ns, "000");
			ResOfRes <= "000"; UResOfRes(1) <= '0';
			CHECK(1 ns, "100");
			CHECK(20 ns, "000");
			done(BitVecDone) <= true;
			wait;
		end process;
	end block;
	
	RESOLVED_RECORD: block
	
		type UREC is record
			sum: integer;
			b: bit;
		end record;
		type UREC_ARRAY is array(natural range<>) of UREC;
		
		impure function format(rec: UREC) return string is begin
			return "(" & integer'image(rec.sum) & bit'image(rec.b) & ")";
		end function;
		
		-- impure, resolution cannot be impure
		function resolve_record( drivers : UREC_ARRAY ) return UREC is 
			variable result : UREC := (0, '0');
			variable drv: UREC;
			variable CNT: integer := 0;
			variable L: line;
		begin
			for index in drivers'range loop
				drv := drivers(index);
				if drv.sum /= 0 then
					result.sum := result.sum + drv.sum;
					CNT := CNT + 1;
				end if;
				result.b := result.b or drv.b;
				write(l, format(drv));
			end loop;
			report "array of " & l.all & " (" & integer'image(cnt) & " active) resolved => " & format(result);
			return result;
		end function;	
		
		-- here we attach the resolution to the type. 
		-- Mithg be it is worth to try a test where we resolve a value, e.g. `signal sREC: resolve_record  UREC;`
		subtype resolved_record is resolve_record  UREC; 
		signal sREC: resolved_record := (-1,'1');
		
		procedure CHECK(i: integer; b: bit) is 
			constant expected: UREC := (i, b);
		begin
			assert sREC = expected report "record " & format(expected) & " was expected but " & format(sREC) & " was recieved instead";
		end procedure;
		
	begin
		sRec <= (100, '0') after 0 ns, (500, '0') after 30 ns;
		sRec <= (200, '0') after 10 ns, (0, '1') after 30 ns;
		
		CHECKER: process
		 
		begin
			CHECK(-2, '1');
			wait for 1 ns - now; CHECK(100-1, '1'); -- at 1 ns
			wait for 11 ns - now; CHECK(300, '0'); -- at 11 ns
			wait for 31 ns - now; CHECK(500, '1'); -- at 31 ns
			done(RecDone) <= true;
			wait;
		end process;
		
	end block;

	RECORD_FIELD_RESOLVED: block
	
		subtype t3nor is or_bvectors bit_vector(1 to 3);
		type UREC is record
			sum: integer;
			b: bit;
			bv: t3nor;
		end record;

				
		impure function format(rec: UREC) return string is begin
			return "(" & integer'image(rec.sum) & bit'image(rec.b) & "["& str(rec.bv) & "]" & ")";
		end function;
		
		-- RES_COUNTER: block -- TODO: what if we field and resolutin function have the same name?
		subtype REC is (b nor_bits) UREC;
		
		signal sREC: REC := (-1,'1', "111");
		signal sUREC: UREC := (-1,'1', "111");
		
		procedure CHECK(i: integer; b: bit; bv: bit_vector) is 
			constant expected: UREC := (i, b, bv);
		begin
			assert sREC = expected report "record " & format(expected) & " was expected but " & format(sREC) & " was recieved instead";
		end procedure;
		
	begin
		
		--sRec <= (100, '0', "111"); -- resolution must fail: we can resolve individual fields but not the whole record
		--sRec.bv(1) <= '1'; -- resolution must fail: array resolution cannot handle slices
		--sURec.b <= '1'; sURec.b <= '0'; resolution must fail: two drivers for unresolved fileds

		sRec <= (200, '0', "000"), (2, '1', "101") after 40 ns;
		sRec.b <= '0' after 10 ns;
		sRec.b <= '0' after 20 ns;
		sRec.bv <= "000" after 30 ns; 
		
		CHECKER: process
		 
		begin
			CHECK(-1, '0', "111");
			wait for 1 ns; CHECK(200,  '0', "111"); -- at 1 ns
			wait for 10 ns; CHECK(200, '0', "111"); -- at 11 ns
			wait for 10 ns; CHECK(200, '1', "111"); -- at 21 ns, 3rd driver of b is off
			wait for 10 ns; CHECK(200, '1', "000"); -- at 31 ns, second driver of bv is off  
			wait for 10 ns; CHECK(2, '0', "101"); -- at 41 ns, first driver makes b and some bv bits on
			done(RecFieldDone) <= true;
			wait;
		end process;
		
	end block;

	process begin
		wait on done;
		if Done = doneType'(others => true) then
			report "done"; 
			wait; 
		end if;
	end process;
	
end architecture;




-- test events here
	-- For non-resolved buses, other elements are not active if one of them is active.
	-- For resolved bus, all elements are active if one of them is active.
	-- Yet, separately resolved elements are not active, as if bus is not resolved.
-- Later we could consider the null-transactions

--entity submodule is port (inv: inout bit); end entity;
--architecture ARCH of submodule is begin
--	inv <= not inv;
--end architecture;

--use WORK.P.all;
--entity Module is
--	port( unresBit: inout bit; res_bit : inout resolved_bit; res_bit_2 : inout wired_nor bit; 
--		ResOfUnresBus: inout resolved_array_of_ubits; ResOfResBus: inout resolved_array_of_ubits;
--		resolvedBus: inout array_of_resolved_bits);
--end entity Module;
--
--architecture Arch of Module is
--begin
--	unresBit <= unresBit; 
--	--unresBit <= '0'; -- resolution must fail
--
--	u1: entity WORK.submodule port map (res_bit);
--	u2: entity WORK.submodule port map (res_bit);
--	
--	res_bit <= res_bit;
--	res_bit <= '1' after 1 ns, '0' after 3 ns;
--	
--	res_bit_2 <= res_bit_2;
--	res_bit_2 <= '1' after 1 ns, '0' after 3 ns;
--	
--	resolvedBus <= resolvedBus;
--	resolvedBus <= (resolvedBus'range => '1') after 1 ns, (resolvedBus'range => '0') after 2 ns;
--	resolvedBus(1) <= '1'; -- must be permitted
--	
--	ResOfUnresBus <= ResOfUnresBus;
--	ResOfUnresBus <= (resolvedBus'range => '1') after 1 ns, (resolvedBus'range => '0') after 2 ns;
--	ResOfUnresBus(1) <= '1'; -- error is expected
--	
--	ResOfResBus <= ResOfResBus;
--	ResOfResBus <= (resolvedBus'range => '1') after 1 ns, (resolvedBus'range => '0') after 2 ns;
--	ResOfResBus(1) <= '1'; -- error is expected
--end architecture;
--	
--	
