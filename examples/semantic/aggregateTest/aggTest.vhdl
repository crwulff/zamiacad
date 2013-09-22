
entity e1 is
   port (a : out bit; b: in bit_vector (3 downto 0));
end entity e1;

architecture RTL of e1 is

begin
end architecture RTL;

library ieee;
use ieee.std_logic_1164.all;

entity aggTest is
  port( a, b : IN bit; z : OUT bit);
end entity aggTest;

architecture RTL of aggTest is 

begin

  foo: entity WORK.E1 port map (a => open, b => (others => '0'));

  
	B2: if true generate
		type rt2 is record
			f1: bit;
			f2: string(1 to 2);
		end record rt2;

		type rt1 is record
			f1, f2: natural;
			f3: rt2;
		end record rt1;
		constant c2: rt2 := ('1', "11");
		constant c1 : rt1 := (23, 42, c2);
		
		type RAT is array (1 to 3) of RT1;
		constant RA: RAT := (c1, c1, (23, 42, ('1', "22")));
		
		function CHECK return boolean is begin
			-- This comparison fails during simulation. Check failing tests
--			assert c1 = (23, 42, ('1', "11")) report "c1 must equal (23, 42, ('1', ""11""))" severity error;
--			assert RA = RAT'(1 to 2 => C1, others => (23, 42, ('1', "22"))) report "RA must be (C1, C1, (23, 42, ('1', ""22"")))" severity error;
			return true;
		end function;
		constant B: boolean := CHECK;
			
	begin
	
	end generate;
	
	B3: if true generate
		function foo (a,b : std_ulogic ; v : std_ulogic_vector) return 			std_ulogic_vector is
		
			variable result : std_ulogic_vector (0 to v'length-1);
		begin
			result :=  ( ( 0 to v'length-1 => a ) and v ) or ( 0 to v'length-1 => b ) ;
			assert result = "1111111111111111" report "result must equal 1111111111111111" severity error;
			return result ;
		end foo ;

		CONSTANT cV      : STD_ULOGIC_VECTOR := "1111111111111111";
		CONSTANT res     : STD_ULOGIC_VECTOR := foo ('0', '1', cV);
	begin
	end generate;
	
	-- Some tests fail, check Doulos
	Doulos: if true generate
		function init return boolean is  
	    	variable V : string(8 downto 1);
	    	variable V2 : string(1 to 8);
	  	begin
	  		-- http://www.doulos.com/knowhow/vhdl_designers_guide/vhdl_2008/vhdl_200x_ease/
		    V := (others => '0');                    -- "00000000"
		    assert V = "00000000" report "Test1: V must be 00000000, got " & V & " instead" severity error;
		    
		    V := ('1', '0', others => '0');          -- "10000000"
		    
		    -- Check reports 00000001 instead of 10000000
		    --assert V = "10000000" report "TEst21: V must be 10000000, got " & V & " instead" severity error;
		    
		    V2 := ('1', '0', others => '0');          -- "10000000"
		    
		    -- Check reports 00000001 instead of 10000000
		    assert V2 = "10000000" report "TEst21 v2: V must be 10000000, got " & V2 & " instead" severity error;
		    
		    --2008, fails in Zamia. vcom -2008 in modelsim
--		    V := ("10", others => '0');              -- "10000000" 
--		    assert V = "10000000" report "Test22: V must be 10000000, got " & V & " instead" severity error;
		    
		    V := (4 downto 1 => '0', others => '1'); -- "11110000"
		    assert V = "11110000" report "Test3: V must be 11110000, got " & V & " instead" severity error;
		    
		    report "initialized";
		    return true;
		end function;
		constant B: boolean := init;
		 
		signal S: bit_vector(7 downto 0) := "11110000";
	begin
	
		-- Examples copypasted from
		--	http://www.doulos.com/knowhow/vhdl_designers_guide/vhdl_2008/vhdl_200x_ease/
		
	-----------------------
	--Vectors in aggregates
	-----------------------
	
	--VHDL aggregates allow a value to be made up from a collection individual array or record elements. For arrays, VHDL up to 1076-2002 allows syntax like this:
		
		-- failed in both Modelsim and zamia
	--	( S(3 downto 0), S(7 downto 4)) <= S after 10 ns;      -- swap nibbles
	--	( 3 downto 0 => S, 7 downto  4 => S) <= S; -- using named association
	end generate Doulos;
	
end architecture RTL;

