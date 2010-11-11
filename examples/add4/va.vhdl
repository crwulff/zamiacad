-----------------------------------------------
-- full adder made up from two half adders
-----------------------------------------------

entity full_adder is 
  port (A, B, C_in : in  bit;
        S, C_out   : out bit);
end;

architecture STRUCTURE of full_adder is
	component half_adder
		port (A, B : in bit;
			  C, S : out bit);
	end component;
	signal S1, S2, S3 : bit;
begin
	ha1: half_adder port map (A => A, B => B, S => S1, C => S2);
	ha2: half_adder port map (A => S1, B => C_in, S => S, C=> S3);
	C_out <= S2 xor S3;
end;
