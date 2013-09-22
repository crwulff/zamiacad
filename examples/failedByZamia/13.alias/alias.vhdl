-- procedure aliases are important because 2008 textio depends on them.

-- I have implemented them in one of the brances by migrating all the 
-- codebase to relay on interfaces. This way, I could proxy objects 
-- with aliases. I was stopped by the fact that in some cases we need
-- more complex aliases, that involve conversions. But, check shows that
-- this is necessary only for the arrays that where you can have another
-- object on top of the aliased object that performs the conversion.

--use std.textio.all; 


entity ALIAS_DEMO is end entity;   
architecture ARCH of ALIAS_DEMO is


	procedure PROC1 is begin  
		report "hello from proc 2";  
	end procedure;
	alias PROC1_ALIAS is PROC1; -- finally, implemented
	
	alias BSHORT is natural; -- type aliases are not implemented
	--alias BT is bit_vector(1 to 10); -- "non-obj alias requires a name" in modelsim

	-- must display 3 errors:
		-- 1. subtype ranges do not match
		-- 2. -2 is out of natural range
		-- 3. -2 is out of range -5 to 5 
  	constant XX: natural := -2;
  	alias YY : natural range -1 to 5 is XX;
  
   --  Type definitions for text I/O: 
--   procedure SREAD is begin 
--   	report "SREAD(no arg)";              
--   end procedure;
--   alias SREAD_ALIAS is SREAD;
   
--   procedure SREAD (VALUE:  integer, S: string) is begin
--   	report "SREAD";              
--   end procedure;
   --alias STRING_READ is SREAD [integer, string];
  


--	--alias ALI is time'image(now) (1)  ; -- MODELSIM: alias must refer an object
--	constant TIME_NOW: time := now;
--	--alias TIME_NOW_ALIAS is time'image(TIME_NOW) (1); -- MODELSIM: alias must refer an object
--	--alias TIME_NOW_ALIAS is time'image(TIME_NOW) ; -- MODELSIM: Cannot create alias of attribute name "image"
--	
--	constant STR1: string := "string1";
--	--alias STR1_CHAR_ALIAS is STR1(1);	-- MODELSIM: OK
--	alias STR1_CHAR_ALIAS2 is STR1(1 to 2);	-- MODELSIM: OK
--	alias STR1_CHAR_ALIAS3 : string(31 to 30+STR1'length) is STR1(1 to STR1'length);	-- MODELSIM: OK
--	--alias STR1_LEAN_ALIAS is STR1'length;	-- MODELSIM: Cannot create alias of attribute name "length"
--
--	attribute ATTR1 : integer ; 
--	attribute ATTR1 of str1_char_alias3: constant is 1 + 1; -- modelsim OK
--	alias ATTR1_ALIAS is ATTR1; -- Modelsim: Unknown identifier "attr1"
	
	-- VHDL LRM: A subtype indication shall not appear in a nonobject alias.
	-- Modelsim: Syntax error; nonobject alias requires a name.
	--alias TYPE_ALIAS is BIT_VECTOR (1 to 3);
	
	--constant A: bit_vector := '1' and "1111"; -- Molesim OK
		
	-- alias BIT_ALIAS is bit; -- not supported
begin 

	--SREAD(2, "str1");   

	process
--		variable SREAD: bit_vector(1 to 5); 
--   		alias SREAD_ALIAS is SREAD         (2 to 4);    
--   	begin
--		SREAD_ALIAS <= "111";
		--variable VAR1 : string := "hello";
		--alias VAR1_ALIAS is VAR1;
		--constant C2: boolean := VAR1(1) = 'c';
		--constant B: bit_vector := "1111";
		--constant bc1: bit := b(0);
		
	begin 
		--report VAR1;
		--VAR1 := "new string ";
		
		
--		report str1_char_alias3 -- reports string1, low = left = 31  
--			& ", low = " & integer'image(str1_char_alias3'low)
--			& ", left = " & integer'image(str1_char_alias3'left);
		wait;
	end process; 
		

end architecture;