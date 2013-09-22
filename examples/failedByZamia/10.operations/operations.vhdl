
entity OP_DEMO is end;

architecture ARCH of OP_DEMO is 

	function computeRecord return boolean is
	 	type RT is record
			a, b: integer; 
		end record;     
		constant R: RT := (1,1);  
	begin
		assert R /= (2,2) report "Record must not equal (2,2)" severity error;  
		assert R = (1,1) report "Record must not equal (2,2)" severity error;
		report "done";  
		return true;
	end function;
	constant REC: boolean := computeRecord;

	
begin
end architecture ARCH;

