
entity CHILD is 
-- We submit "1111" to a 3-bit port but it is not detected during
-- association elaboration. Only if we read the port (that is, start
-- simulation) this manifests.
	port (PORTA : in bit_vector (1 to 3));
end entity;

architecture ARCH of CHILD is 

	-- this check does not catch the problem as the 
	-- the unconstrained portA in the other test
	--	since now portA is constrained.
	impure function check2 return boolean is 
		constant S: string := "actual = " & integer'image(PORTA'length) & ", expected_length = 3";
	begin	
		report S;
		assert PORTA'length = 3 report S severity ERROR;
		return true;
	end function;
	constant B1: boolean := check2; 
	
	
begin 
	process
		variable S: string (1 to 3);
	begin
		for I in s'range loop
			 if porta(I) = '1' then 
			 	s(I) := '1';
			 else 
			 	s(I) := '0';
			 end if;
		end loop;
		report "port A = 111" & s; 
	end process;

end architecture;

entity UNMATCHED_PORT_WIDTH is end;

architecture bench of UNMATCHED_PORT_WIDTH is

	-- we need to simulate because port value is uncertain until 
	-- after the elaboration ends.
	--constant A: bit_vector(1 to 2) := "1111"; -- this is noticed
begin

	u1: entity WORK.CHILD port map ("1111");
	
end bench;

