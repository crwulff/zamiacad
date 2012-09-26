library ieee;
use ieee.std_logic_1164.all;
use std.textio.all;

entity FILE_READ is
end FILE_READ;

architecture read_from_file of FILE_READ is
  
  constant stim_file : string  := "sim.dat";
  file stimulus      : TEXT open read_mode is stim_file;

begin

  receive_data: process
    variable l, l2    : line;
    variable ok   : boolean := false;
    variable ok_2 : boolean := false;

    variable v_bit       : bit;
    variable v_bit_vec   : bit_vector(2 downto 0);
    variable v_bit_vec2  : bit_vector(0 to 7);
    variable v_bit_vec2r : bit_vector(7 downto 0);
    variable v_bool      : boolean;
    variable v_char      : character;
    variable v_string    : string(1 to 11);
    variable v_string_r  : string(11 downto 7);
    variable v_int       : integer;
	
	-- This caused NPE because procedure variables, as opposed to precess variables
	--	are not initialized at simulation start and line was null causing NPE in write(line, val).
	procedure WRITE_PROC is
		variable LINE1: LINE;
	begin
		report "555";
		write(LINE1, string'("aaa"));
		report "222";
	end procedure;
	
  begin

     assert not endfile(stimulus) report "BIT: EOF reached unexpectedly, or ENDFILE() failed";
     readline(stimulus, l);
     -- '110 10021'
     read(l, v_bit);
     assert v_bit = '1' report "1 Read failed for: BIT";
     read(l, v_bit);
     assert v_bit = '1' report "2 Read failed for: BIT";
     read(l, v_bit);
     assert v_bit = '0' report "3 Read failed for: BIT";
     read(l, v_bit);
     assert v_bit = '1' report "4 Read failed for: BIT";
     read(l, v_bit);
     assert v_bit = '0' report "5 Read failed for: BIT";
     read(l, v_bit, ok);
     assert v_bit = '0' report "6 Read failed for: BIT";
     assert ok = true report "7 Read OK failed for: BIT";
     read(l, v_bit, ok);
     assert ok = false report "8 Read OK failed for: BIT";
     read(l, v_char, ok_2); -- skip unreadable char ('2')
     assert v_char = '2';
     assert ok_2 = true;
     read(l, v_bit, ok);
     assert ok = true report "9 Read OK failed for: BIT";
     assert v_bit = '1' report "10 Read failed for: BIT";

     assert not endfile(stimulus) report "BIT_VECTOR: EOF reached unexpectedly, or ENDFILE() failed";
     readline(stimulus, l);
     -- '110 10021110 1101_0001 1101_0001 10_00_11_01    _110_1   1__1_1_1_1_'
     ok := false;
     ok_2 := false;
     read(l, v_bit_vec);
     assert v_bit_vec = "110" report "1 Read failed for: BIT_VECTOR";
     read(l, v_bit_vec);
     assert v_bit_vec = "100" report "2 Read failed for: BIT_VECTOR";
     read(l, v_bit_vec, ok);
     assert ok = false report "3 Read OK failed for: BIT_VECTOR";
     read(l, v_char, ok_2); -- skip unreadable char ('2')
     assert v_char = '2';
     assert ok_2 = true;
     read(l, v_bit_vec, ok);
     assert ok = true report "4 Read OK failed for: BIT_VECTOR";
     assert v_bit_vec = "111" report "5 Read failed for: BIT_VECTOR";
     read(l, v_bit_vec, ok);
     assert ok = false report "6 Read OK failed for: BIT_VECTOR";
     read(l, v_bit_vec(1 downto 1), ok);
     assert ok = true report "7 Read OK failed for: BIT_VECTOR";
     assert v_bit_vec(1 downto 1) = "0" report "8 Read failed for: BIT_VECTOR";
     assert v_bit_vec = "101";
     read(l, v_bit_vec2, ok);
     assert ok = true report "9 Read OK failed for: BIT_VECTOR";
     assert v_bit_vec2 = "11010001" report "10 Read failed for: BIT_VECTOR";
     read(l, v_bit_vec2r, ok);
     assert ok = true report "11 Read OK failed for: BIT_VECTOR";
     assert v_bit_vec2r = "11010001" report "12 Read failed for: BIT_VECTOR";
     read(l, v_bit_vec2r, ok);
     assert ok = true report "13 Read OK failed for: BIT_VECTOR";
     assert v_bit_vec2r = "10001101" report "14 Read failed for: BIT_VECTOR";
     read(l, v_bit_vec2r(5 downto 2), ok);
     assert ok = false report "15 Read OK failed for: BIT_VECTOR";
     ok_2 := false;
     read(l, v_string(1 to 5), ok_2); -- skip unreadable char ('_')
     assert v_string(1 to 5) = "    _";
     assert ok_2 = true;
     read(l, v_bit_vec2r(5 downto 2), ok);
     assert ok = true report "16 Read OK failed for: BIT_VECTOR";
     assert v_bit_vec2r(5 downto 2) = "1101" report "17 Read failed for: BIT_VECTOR";
     read(l, v_bit_vec2(2 to 5), ok);
     assert ok = false report "18 Read OK failed for: BIT_VECTOR";
     read(l, v_bit_vec2(2 to 2), ok);
     assert ok = true report "19 Read OK failed for: BIT_VECTOR";
     assert v_bit_vec2(2 to 2) = "1" report "20 Read failed for: BIT_VECTOR";
     read(l, v_string(10 to 11), ok_2); -- skip unreadable char ('_')
     assert v_string(10 to 11) = "__";
     assert ok_2 = true;
     read(l, v_bit_vec2(2 to 5), ok);
     assert ok = true report "21 Read OK failed for: BIT_VECTOR";
     assert v_bit_vec2(2 to 5) = "1111" report "22 Read failed for: BIT_VECTOR";

     assert not endfile(stimulus) report "BOOLEAN: EOF reached unexpectedly, or ENDFILE() failed";
     readline(stimulus, l);
     -- 'TRUE TRUEFALSE   TRUE FALSEFALSE TTTTT TRUE'
     ok := false;
     ok_2 := false;
     read(l, v_bool);
     assert v_bool = true report "1 Read failed for: BOOLEAN";
     read(l, v_bool);
     assert v_bool = true report "2 Read failed for: BOOLEAN";
     read(l, v_bool);
     assert v_bool = false report "3 Read failed for: BOOLEAN";
     read(l, v_bool);
     assert v_bool = true report "4 Read failed for: BOOLEAN";
     read(l, v_bool);
     assert v_bool = false report "5 Read failed for: BOOLEAN";
     read(l, v_bool, ok);
     assert v_bool = false report "6 Read failed for: BOOLEAN";
     assert ok = true report "7 Read OK failed for: BOOLEAN";
     read(l, v_bool, ok);
     assert ok = false report "8 Read OK failed for: BOOLEAN";
     read(l, v_string(1 to 6), ok_2); -- skip unreadable string (' TTTTT')
     assert v_string(1 to 6) = " TTTTT";
     assert v_string(1 to 1) = " ";
     assert v_string(2 to 2) = "T";
     assert v_string(6 to 6) = "T";
     assert ok_2 = true;
     read(l, v_bool, ok);
     assert ok = true report "9 Read OK failed for: BOOLEAN";
     assert v_bool = true report "10 Read failed for: BOOLEAN";

     assert not endfile(stimulus) report "CHARACTER: EOF reached unexpectedly, or ENDFILE() failed";
     readline(stimulus, l);
     -- ' BRWN	JMP HE_91'
     ok := false;
     read(l, v_char);
     assert v_char = ' ' report "1 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = 'B' report "2 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = 'R' report "3 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = 'W' report "4 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = 'N' report "5 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = HT report "6 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = 'J' report "7 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = 'M' report "8 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = 'P' report "9 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = ' ' report "10 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = 'H' report "11 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = 'E' report "12 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = '_' report "13 Read failed for: CHARACTER";
     read(l, v_char);
     assert v_char = '9' report "14 Read failed for: CHARACTER";
     read(l, v_char, ok);
     assert v_char = '1' report "15 Read failed for: CHARACTER";
     assert ok = true report "16 Read OK failed for: CHARACTER";

     assert not endfile(stimulus) report "INTEGER: EOF reached unexpectedly, or ENDFILE() failed";
     readline(stimulus, l);
     -- ' 214748	 3647 7H2231 2147483647 2147483648 -113'
     ok := false;
     ok_2 := false;
     read(l, v_int);
     assert v_int = 214748 report "1 Read failed for: INTEGER";
     read(l, v_int);
     assert v_int = 3647 report "2 Read failed for: INTEGER";
     read(l, v_int, ok);
     assert v_int = 7 report "3 Read failed for: INTEGER";
     assert ok = true report "4 Read OK failed for: INTEGER";
     read(l, v_int, ok);
     assert ok = false report "5 Read OK failed for: INTEGER";
     read(l, v_char, ok_2); -- skip unreadable char ('H')
     assert v_char = 'H';
     assert ok_2 = true;
     read(l, v_int, ok);
     assert ok = true report "6 Read OK failed for: INTEGER";
     assert v_int = 2231 report "7 Read failed for: INTEGER";
     read(l, v_int, ok);
     assert ok = true report "8 Read OK failed for: INTEGER";
     assert v_int = 2147483647 report "9 Read failed for: INTEGER";
     read(l, v_int, ok);
     assert ok = false report "10 Read OK failed for: INTEGER";
     ok_2 := false;
     read(l, v_string, ok_2); -- skip unreadable string (' 2147483648')
     assert v_string = " 2147483648";
     assert ok_2 = true;
     read(l, v_int, ok);
     assert ok = true report "11 Read OK failed for: INTEGER";
     assert v_int = -113 report "12 Read failed for: INTEGER";

     -- TODO: REAL

     assert not endfile(stimulus) report "STRING: EOF reached unexpectedly, or ENDFILE() failed";
     readline(stimulus, l);
     -- '1UXZW'
     -- '1UXZQ'
     ok := false;
     read(l, v_string(7 to 11));
     assert v_string(7 to 11) = "1UXZW" report "1 Read failed for: STRING";
     assert v_string(7 to 7) = "1" report "2 Read failed for: STRING"; 
     readline(stimulus, l);
     read(l, v_string_r(11 downto 7), ok);
     assert ok = true report "3 Read OK failed for: STRING";
     assert v_string_r(11 downto 7) = "1UXZQ" report "4 Read failed for: STRING";
     assert v_string_r(11 downto 11) = "1" report "5 Read failed for: STRING"; 
     assert v_string_r = "1UXZQ" report "6 Read failed for: STRING";

     -- TODO: TIME

     assert endfile(stimulus) report "ENDFILE() failed. Expected = true, actual = false.";
     
    -- TESET READ_AFTER_WRITE
    WRITE(l, character'('d')); 
     writeline(output, l); -- drain the line
     
     --WARNING: D is read instead of d
 	WRITE(l, character'('d')); READ(l, v_char); --assert v_char = '1' report "got char = " & v_char & " instead of 'd'";
	WRITE(l, integer'(-3)); READ(l, v_int); assert v_int = -3 report "got int = " & integer'image(v_int) & " instead of -3";
	WRITE(l, character'('-')); WRITE(l, character'('4')); READ(l, v_int); assert v_int = -4 report "got int = " & integer'image(v_int) & " instead of -3";
	WRITE(l, boolean'(true)); READ(l, v_bool); assert v_bool = true report "got " & boolean'image(v_bool) & " instead of true";
	WRITE(l, boolean'(false)); READ(l, v_bool); assert v_bool = false report "got " & boolean'image(v_bool) & " instead of false";
	WRITE(l, string'("abc")); READ(l, v_string(1 to 3)); assert v_string(1 to 3) = "abc" report "got string = " & v_string(1 to 3) & " instead of abc";
	 
	report "str len = " & integer'image(l'length)  & 
		" : " & integer'image(l'low) & 
		" to " & integer'image(l'high);
 
	assert l'length = 0 report "line len must be 0, actual = " & integer'image(l'length); 
	assert l'low = 1 report "line low must be 1, actual = " & integer'image(l'low); 
	assert l'high = 0 report "line high must be 0, actual = " & integer'image(l'high);
	 
	write(l, string'("ABC"));
	l2 := l;
	flush(output);
	assert l2'length = 2 report "another ref to the same lime must decrease len" severity NOTE;

	WRITE_PROC;
    wait;
  end process receive_data;
end read_from_file;
