-- Shows that assigning A(1 to 3) does not affect A(4)
-- nor A(4 to 5) is affected.
 
entity SUBDRIVER is
	port (A: out bit_vector);
end entity;

architecture SUBDRIVER of SUBDRIVER is
subtype T is bit_vector (A'range);
begin
	A <= T'(others => '1') after 10 ns;
end architecture;

entity EVENTS_DEMO is end entity EVENTS_DEMO;


architecture RTL of EVENTS_DEMO is
begin 

------------------------------------------------------

	B0: if true generate
		signal A: bit_vector (1 to 10);
	begin
		A(1 to 2) <= "11" after 10 ns;
		
		SD: entity WORK.SUBDRIVER port map (A(4 to 5));
		
	  process (A'transaction) begin
	  	if (now > 1 ns) then
		report "A transaction because A(1-to-2 and 4-to-5) were assigned:";
		
		report "A(1) = " & bit'image(A(1));
	  
	  	report " A'active = " & boolean'image(A'active);
	  	assert A'active report "A must be active" severity error;
	  	
	  	report " A(1)'active = " & boolean'image(A(1)'active);
	  	assert A(1)'active report "A(1) must be active" severity error;
	  	
	  	report " A(2 to 3)'active = " & boolean'image(A(2 to 3)'active);
	  	assert A(2 to 3)'active report "A(2 to 3) must be active" severity error;
	  	
	  	report " A(5 to 6)'active = " & boolean'image(A(5 to 6)'active);
	  	assert A(5 to 6)'active report "A(5 to 6) must be driven by port" severity error;
	  	
	  	report " A(6)'active = " & boolean'image(A(6)'active);
	  	assert not A(6)'active report "A(6) must not be driven" severity error;
	  	
	  	report " A(10)'active = " & boolean'image(A(10)'active);
	  	assert not A(10)'active report "A(9 to 10) must not be active" severity error;
	  	
	  	report " A(9 to 10)'active = " & boolean'image(A(9 to 10)'active);
	  	assert not A(9 to 10)'active report "A(9 to 10) must not be active" severity error;
	  	
	  end if;
	 end process;
	end generate;

 
 --------------------------------------------------------------

 
 	B1: if true generate
		--Event firing when driving an element of an array
		signal s: string(1 to 2) := "00";
		alias s2 is s(2);
		alias s1 is s(1);
		alias as is s;
	begin

-- Causes s, s(1) and alias(1) events at 0 ns 
		s(1) <= '2';
		
		process begin 
			wait on s; report("s has changed to " & s);
		end process;
		
		process begin 
			wait on s(1); report("s(1) has changed to " & s(1)); 
		end process;
		
		process begin 
			wait on s(2); -- Zamia false event here
			report("s(2) has changed to " & s(2)); 
			assert false report "s(2) is active" severity error; 
		end process;
		
		
		process begin 
			wait on s2; -- zamia false event here
			report("alias(2) has changed to " & s2); 
			assert false report "alias(2) is active" severity error; 
		end process;
		
		process begin 
			wait on s1;
			report("alias(1) has changed to " & s1); 
		end process;
	end generate;
	
 	
 	
--------------------------------------------------------------

	
	B2: if true generate
		type RT is record
				a, b: integer; 
			end record;     
		signal R: RT := (1,1);  
	begin
	
	-- only R and R.a are active after
		r.a <= 3;
		
		process begin
		
			wait on R;  
			
			--check that we are active only in the cycle we release
			report "R is active = " & boolean'image(R'active); -- attribute "active" is not implemented
			wait for 0 ns;
			report "R is active on the next delta cycle = " & boolean'image(R'active);
			wait;
			
		end process;
		
		process begin 
			wait on R.a; report("R.a has changed to " & integer'image(r.a)); 
		end process;
		
		process begin
			wait on R.b;
			report("R.a has changed to " & integer'image(r.b)); 
			assert false report "R.b is active" severity error; 
		end process;
	
	end generate;		
 	
 	
--------------------------------------------------------------

 	
 	 B3: if true generate
		--Event firing when driving an element of an array
		signal s: string(1 to 2) := "00";
		alias s2 is s(2);
		alias s1 is s(1);
		alias as is s;
		type RT is record
				a, b: integer; 
			end record;     
		signal R: RT := (1,1);
	begin

		s <= "00", "11" after 1 ns;
		
		process begin 
			wait on s; report("s has changed to " & s); 
			assert s = "11" report "s != 11" severity error;
		end process;
		
		process begin 
			wait on as; report("s has changed to " & s); 
			assert as = "11" report "ss != 11" severity error;
		end process;
		
		process begin 
			wait on s(1); report("s(1) has changed to " & s(1)); 
			assert s(1) = '1' report "s(1) != '1'" severity error;
		end process;
		
		process begin 
			wait on s(2); 
			assert s(2) = '1' report "s(2) != '1'" severity error;
		end process;
		
		process begin 
			wait on s1; report("alias(1) has changed to " & s1); 
			assert s1 = '1' report "s1 != '1'" severity error;
		end process;
		
		process begin 
			wait on s2; 
			report("alias(2) has changed to " & s2); 
			assert s2 = '1' report "s2 != '1'" severity error;
		end process;
		
		r <= (1,1), (2,2) after 10 ns;
		
		process begin
			wait on R; report "R is active ";
			assert R = (2,2) report "R != (2,2)" severity error;
		end process;
		
		process begin 
			wait on R.a; report("R.a has changed to " & integer'image(r.a)); 
			assert R.a = 2 report "R.a != 2" severity error;
		end process;
		
		process begin
			wait on R.b; report("R.a has changed to " & integer'image(r.b)); 
			assert R.b = 2 report "R.b != 2" severity error;
		end process;
	
	end generate;
	
	
--------------------------------------------------------------

	
	B4: if true generate
	-- can we have record fields driven in different processes?
		signal s: string (1 to 2);
--		subtype BV is bit_vector(1 to 1000000); 
--		signal A: BV;
	begin

		--s'driving); -- not implemented
		process

			-- Modelsim wants A to be input but if I make it input then it will tell that 
			-- 'driving is inapplicable to inputs. For inout, it says that 
			--		No driver in this region for 'DRIVING attribute of signal "a"
			-- without ever trying to do the recursion.
--			procedure isDriving(signal a: inout BV; I: integer; result: out boolean) is begin
--				a <= (others => '0');
--				if i = a'high then 
--					result := a'driving;
--				else
--					isDriving(a, i+1, result);
--				end if;
--			end procedure;
			impure function formatDriving return string is
			variable result: boolean; 
			begin
				--isDriving(a, BV'low, result);
				return "S'drv = " & boolean'image(s'driving) & 
					", S(1)'drv = " & boolean'image(s(1)'driving) &
					", S(2)'drv = " & boolean'image(s(1)'driving) 
					--& ", a'driving = " & boolean'image(isDriving(a))
					;
					
			end function;
		begin
		
			-- all are 'driving are true in Modelsim but 
			-- not implemented in zamia
			s <= "11";
			report "after s <= 11, " & formatDriving;
			wait on s;
			report "after wait on s, " & formatDriving;
			s <= "11";
			wait for 10 ns;
			report "after s <= 11 and wait 0 ns, " & formatDriving;
	  		wait;
		end process;
		
	end generate;
end architecture RTL;


