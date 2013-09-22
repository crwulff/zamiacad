entity BINARY_IO is end;

architecture ARCH of BINARY_IO is


	impure function BINARY_READ return boolean is
	
-- The standard VHDL functions are:
--binary read, implicitly declared for every  FT: file of TM:
--    procedure READ (file F: FT; VALUE: out TM); //
--    procedure READ (file F: FT; VALUE: out TM; LENGTH: out Natural); // for unconstrained arrays
--    
--textio
--    type TEXT is file of STRING;
--    procedure READ (file F: TEXT; VALUE: out STRING); //
--    procedure READ (L: inout LINE; VALUE: out BIT; GOOD:  out BOOLEAN);

	-- NPE because read does not have the L argument. It seems that built in is 
	-- overloaded only for reading text filetext
	subtype TSMALL is integer range -10 to 20;
    type TIFILE is file of integer ; 		file IFILE: TIFILE open read_mode is "demo.vhdl"; 			variable I: integer;
    type TRFILE is file of real; 			file RFILE: TRFILE open read_mode is "demo.vhdl";			variable R: real;
    type TSMALLFILE is file of TSMALL;		file SMALLFILE: TSMALLFILE open read_mode is "demo.vhdl";	variable SMALL: TSMALL;	
    type TBOOLFILE is file of boolean;		file BOOLFILE: TBOOLFILE open read_mode is "demo.vhdl";		variable Bool: boolean;
    type TBITFILE is file of bit;			file BITFILE: TBITFILE open read_mode is "demo.vhdl";		variable b: bit;	
    type TCHARFILE is file of character;	file CHARFILE: TCHARFILE open read_mode is "demo.vhdl";		variable CH: character;
    type TTIMEFILE is file of time;			file TIMEFILE: TTIMEFILE open read_mode is "demo.vhdl";		variable T: time;
    type TTEXTFILE is file of string;		file STRFILE: TTEXTFILE open read_mode is "demo.vhdl";    	variable STR: string(1 to 3);
    
	begin
 		read(ifile, I); report "I = " & integer'image(I);
 		read(RFILE, r); report "r = " & real'image(r);
 		--read(SMALLFILE, small); report "small = " & TSMALL'image(small); -- modelsim reads a very large value, out of the range -10 to 20.
 		read(BOOLFILE, bool); report "bool = " & boolean'image(bool);
 		read(BITFILE, B); report "bit = " & bit'image(B);
 		read(CHARFILE, CH); report "char = " & CH;
 		read(TIMEFILE, T); report "time = " & time'image(T);
 		-- read(STRFILE, STR); report "str(1 to 3) = " & STR; not suppported in modelsim
		return true;
	end function;
	constant BIN_EXEC: boolean := binary_read;



begin 

end architecture;