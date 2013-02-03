-- Execute 20 ns minimum

entity GEN is
	port (O1, O2: buffer bit);
end;

architecture ARCH of GEN is
begin

	process (O1) begin
		report "o1 at " & time'image(now);
		O1 <= not O1 after 10 ns;
	end process;
	
	process (O1) begin
		report "o2 at " & time'image(now);
		O2 <= not O1;
	end process;

end architecture;

entity POSTPONED_DEMO is end;

architecture ARCH of POSTPONED_DEMO is
begin

	B2: block
		signal s1, s2: bit;
	begin
		
		I1: entity WORK.GEN port map (S1, S2);
		
	-- http://computer-programming-forum.com/42-vhdl/3edcb21668330593.htm
	-- As I read the LRM, the postponed process will be executed twice at
	--start time 0 ns.
	--1 - termination of initialization
	--2 - termination of first cycle
	--which makes sense.
	--
	--If this is a problem for your implementation, use a guard variable
	--to way around this:
	--
	--postponed process (...)
	-- variable first_flag : boolean := true;
	--begin
	-- if first_flag then
	--  first_flag := false;
	-- else
	--  -- your process activities
	--  ...
	-- end if;
	--end postponed process; 
		CHECK: postponed process (S1, S2) 
			variable FIRST_TIME: boolean := true;
		begin
			if FIRST_TIME then
				report "0 delta cycle has not finished yet, bypassing the check";
				FIRST_TIME := false;
			else 
				report "check at " & time'image(now);
				assert S1 = not S2 report "S1 must be not S2" severity failure;
			end if;
		end process;
		
	end block B2;



--	B1: block
--		constant burst_len : integer := 2;
--		signal A: integer := 0;
--		signal B: integer := 0;
--	begin
--
--		PPONED: postponed process (A) begin
--			report "postponed A = " & integer'image(A) & " at " & time'image(now);
--			-- if now < 10 ns then
--		end process;
--		
--		process begin 
--			b <= b + 1 after 25 ns, b + 2 after 27 ns; -- TODO that PROC1 receives events from postponed
--			wait;
--		end process;
--		
--		PROC1: process (A,b)
--			variable burst: integer := BURST_LEN;
--		begin
--			-- first time called by increment by our proc,
--			-- another by postponed
--			if burst = 0 then
--				report "proc1: burst finished";
--				burst := BURST_LEN;
--			else
--				burst := burst - 1;
--				report "proc1: incrementing A to " & integer'image(a+1);
--				A <= A + 1;
--			end if;
--			
--			-- -- simulate cycle limit exceeded
--			-- if now > 1 ns then
--			-- a <= a+1;
--			-- end if;
--		end process;
--	end block;
--
 
end architecture;

