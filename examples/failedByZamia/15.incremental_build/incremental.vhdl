package PKG is 
	procedure PROC;
	function FUNK return string;
end package;

package body PKG is
	procedure PROC is begin
		report "HELLO FROM ORIGINAL PKG";
	end procedure;
	function FUNK return string is 
	begin
		return "HELLO FROM ORIGINAL PKG";
	end function;
end package body;

entity INCREMENTAL is end;

use WORK.PKG.all;

architecture ORIGINAL of INCREMENTAL is
	
	impure function HELLO return boolean is
	begin
		report "HELLO FROM ORIGINAL ARCH";
		--report FUNK;
		PROC;
		return true;
	end function;
	constant EXECUTOR: boolean := HELLO;
	
	
	
begin 

end architecture;