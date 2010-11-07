---------------------------------------------------------
-- 4-bit-ripple-carry-adder made up from four full adders
---------------------------------------------------------

entity add4 is 
  port (A, B    : in  bit_vector (3 downto 0);
        C_in    : in  bit;
        S       : out bit_vector (3 downto 0);
        C       : out bit);
end;

architecture STRUCTURE of add4 is
	component full_adder
		port (A, B, C_in : in bit;
			  S, C_out   : out bit);
	end component;
	signal C1, C2, C3 : bit;
begin
	va0: full_adder port map (A => A(0), B => B(0), C_in => C_in, S => S(0), C_out => C1);
	va1: full_adder port map (A => A(1), B => B(1), C_in => C1, S => S(1), C_out => C2);
	va2: full_adder port map (A => A(2), B => B(2), C_in => C2, S => S(2), C_out => C3);
	va3: full_adder port map (A => A(3), B => B(3), C_in => C3, S => S(3), C_out => C);
end;
