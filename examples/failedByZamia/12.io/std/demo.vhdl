entity STD_IO is end;

library std;
use std.textio.all;

architecture ARCH of STD_IO is



	-- https://groups.google.com/forum/#!topic/comp.lang.vhdl/Poe6MBD8pZ4
	-- The problem is that read, write and eof use fileName -> 
	-- -> java.io.File but there is no such mapping for std streams.
	-- This can be resolved after we implement proper I/O where file
	-- is an open stream that can be closed rather than file that 
	-- opened/closed at every access.
	impure function STD_READ_WRITE return boolean is
		variable L: line; 
		variable command: line; 
		variable str13 : string(1 to 3);
		file INPUT2: TEXT is in "STD_INPUT";
		
		procedure read(title: string; file T: TEXT) is begin
		       
			report "------------------------------Reading from " & title & ". Enter and string longer than 3 characters";
			--write (output, s); -- bad
	        readline(input, command);
	        read(command, str13);
	
			report "the first 3 chars are";
			write(l, str13); -- good
			writeline(output, l); -- good

		end procedure;
	begin
		read("textio.input", input);
		read("input2 = STD_INPUT", input2);
		return true;
	end function;
	constant STDRW_EXEC: boolean := STD_READ_WRITE;
	
	
	
begin 

end architecture;