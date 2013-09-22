library ieee;
use ieee.std_logic_1164.all;

entity ccTest is
  generic (width : positive := 3);
  port(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y : IN bit; z: out bit );
end entity ccTest;

architecture RTL of ccTest is 

    CONSTANT c2: STD_ULOGIC_VECTOR := 
    	"1111111111111111" & "1111111111111111" & "1111111111111111" & "1111111110111111" ;

	CONSTANT c7: bit_vector      := 
		"1111111111111111" & "1111111111111111" & "1111111111111111" & "1111111110111111" ;
		
	function check return boolean is begin
		assert c2 = "1111111111111111111111111111111111111111111111111111111110111111" report "c2 has wrong value" severity error;
		assert c7 = "1111111111111111111111111111111111111111111111111111111110111111" report "c7 has wrong value" severity error;
		return true;
	end function;
	constant c27: boolean := check;
	
	function ccTEST3 return boolean is
		constant v       : std_ulogic_vector := "";
		constant w       : positive range 1 to 65536 := 1;
		constant v2      : std_ulogic_vector (0 to (v'length + w-1)) := v & (0 to w-1 => '0');
	begin
		assert v2 = "0" report "v2 must be empty" severity error;
		return true;
	end function;
	constant B3: boolean := ccTEST3;

	function ccTest45 return boolean is
		constant w       : positive := 8;
		constant v2      : bit_vector(2 to 5) := "0101";
		constant v       : bit_vector(0 to w-1) := v2 & (v2'length to w-1 => '0');
		
		function f(s : bit_vector) return bit_vector is
		begin
		  return s;
		end f;

		constant v4: bit_vector(1 to 2) := "11";
		CONSTANT v5: bit_vector(0 to width-1) := v4 & (v4'length to width-1 => '0');
		

	begin
		assert v = "01010000" report "v must equal 01010000" severity error;
		assert f(v5(0 to width-1)) = "110" report "f(v2) must equal 110" severity error; 
		return true;
	end function;
	constant B45: boolean := ccTEST45;
	
	
begin

	ccTEST6: if true generate
		procedure myproc (p1 : bit_vector) is
			constant I: integer := p1'length;
		begin
			report "p1'len = " & integer'image(I);
			assert I = 25 
				report "p1'length must be 25 rather than" & integer'image(i) 
				severity error;
		end procedure;
	begin
		  myproc(a&b&c&d&e&f&g&h&i&j&k&l&m&n&o&p&q&r&s&t&u&v&w&x&y);
	end generate;
end architecture RTL;

