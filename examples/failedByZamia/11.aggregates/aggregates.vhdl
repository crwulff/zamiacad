-- Modelsim does not support aggregate tartets
entity AGGTEST is end;

architecture Arch of AGGTEST is
	
	function init return boolean is  
    	variable V : string(8 downto 1);
    	variable V2 : string(1 to 8);
  	begin
	  		-- http://www.doulos.com/knowhow/vhdl_designers_guide/vhdl_2008/vhdl_200x_ease/
		    V := (others => '0');                    -- "00000000"
		    assert V = "00000000" report "Test1: V must be 00000000, got " & V & " instead" severity error;
		    
		    V := ('1', '0', others => '0');          -- "10000000"
		    
		    -- Check reports 00000001 instead of 10000000
		    assert V = "10000000" report "TEst21: V must be 10000000, got " & V & " instead" severity error;
		    
		    V2 := ('1', '0', others => '0');          -- "10000000"
		    
		    -- Check reports 00000001 instead of 10000000
		    assert V2 = "10000000" report "TEst21 v2: V must be 10000000, got " & V2 & " instead" severity error;
		    
		    --2008, fails in Zamia. vcom -2008 in modelsim
		    V := ("10", others => '0');              -- "10000000" 
		    assert V = "10000000" report "Test22: V must be 10000000, got " & V & " instead" severity error;
		    
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
	
	-- not supported in Modelsim 6.6d
	( S(3 downto 0), S(7 downto 4)) <= S after 10 ns;      -- swap nibbles
	( 3 downto 0 => S, 7 downto  4 => S) <= S; -- using named association
	
end architecture;