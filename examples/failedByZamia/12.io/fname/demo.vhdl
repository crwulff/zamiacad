library ieee;


entity FNAME is
end FNAME;

architecture ARCH of FNAME is

	-- the problem appears only during function elaboration
	-- but not in simulation
	function INIT return boolean is
	
		constant FILE_NAME : string := "demo.vhdl";
		
		type TIFILE is file of integer ; 		
		file IFILE:  TIFILE open read_mode is "demo.vhdl"; -- OK
		file IFILE2: TIFILE open read_mode is FILE_NAME; -- failure
	begin
		return true; 
	end function;
	constant RESULT : boolean := INIT;
begin

	process 
	
		-- also error here
--		function INIT return boolean is
--	
--			constant FILE_NAME : string := "demo.vhdl";
--			
--			type TIFILE is file of integer ; 		
--			file IFILE:  TIFILE open read_mode is "demo.vhdl"; -- OK
--			file IFILE2: TIFILE open read_mode is FILE_NAME; -- failure
--		begin
--			return true; 
--		end function;
		
		constant RESULT : boolean := INIT;
	
		constant FILE_NAME : string := "demo.vhdl";
		
		-- no error here !!!!
		type TIFILE is file of integer ; 		
		file IFILE:  TIFILE open read_mode is "demo.vhdl"; -- ok
		file IFILE2: TIFILE open read_mode is FILE_NAME; -- OK!!!!!
	begin
		report "done";
	end process;
end architecture; 