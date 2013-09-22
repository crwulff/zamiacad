library ieee;
use ieee.std_logic_1164.all;
use std.textio.all;

entity TYPETEST is
  port( a, b : IN bit; z : OUT bit);
end entity TYPETEST;

architecture RTL of TYPETEST is

begin

	B1: block 
	  subtype single is std_logic;
	  signal clk : single;
	  
	  subtype verysmall_int is integer range 1 to 32;
	  type at is array(verysmall_int) of bit;
	  signal as: at  := (others => '0');
	begin
	end block;
		
	B2: block
	
	  constant c1   : natural := 24;    
	  constant c2   : natural := 3;  
	  subtype  c1_range   is natural range 0 to c1-1;
	  subtype  c2_range   is natural range 0 to c2-1;
	
	  type tr is record
			f1       :  std_ulogic_vector(c1_range);
			f2       :  std_ulogic_vector(c2_range);
			f3 : integer; 
			f4 : bit ; 
	  end record tr; -- end record R1 label mismatch is passed unnoticed 
	
	  SIGNAL r1 : tr;
	  constant cr: tr := ((others => '0'), (others => '0'), 1 , '1');
	  signal i : integer := cr.f3;
 
	begin	 	
	end block;
	

	B3: block
	 	
--		-- This example finally failed on Dec 2012
--		-- IGObjectDriver: Internal error: tried to assign unconstrained array value to an array
--
--		-- Modelsim: Element subtype (std.STANDARD.BIT_VECTOR) of array type cannot 
--		-- be unconstrained array type.
--		type b4v is array (natural range <>) of bit_vector;
--		
--		SIGNAL v1 : b4v(0 to 1)(1 to 3);
--		SIGNAL v2 : b4v(0 to 1)(1 to 3);
--		SIGNAL v3 : bit_vector(0 to 3);
--
--	begin
--
--		  v1 <= v3 & v2(0);
		


--		 I had to constrain the test
		subtype CONSTRAINED_BV is bit_vector(1 to 3);
		type BV_2D_UNCONSTRAINED is array (natural range <>) of BIT_VECTOR; -- compile with vcom -2008
		type BV_2D_CONSTRAINED is array (natural range <>) of CONSTRAINED_BV;
		
		SIGNAL v1, v2 : BV_2D_CONSTRAINED(0 to 1);
		SIGNAL v3 : CONSTRAINED_BV := (others => '1');
	begin
	-- minor ERROR: must take value 111000 AFTER simulation starts,
	-- not during elaboration.
	v1 <= v3 & v2(0);
	--	 Achtung! this is evaluated only during simulation
	end block;
	
	ACCESS_TYPE: block
		impure function CHECK return boolean is
			variable L: line;
		begin
			write (L, string'("abc"));
			assert L.all = "abc" report "Line.all must be ""abc""" severity error;
			return true;
		end function;
		constant B: boolean := CHECK;
	begin
	end block;
end architecture RTL;

