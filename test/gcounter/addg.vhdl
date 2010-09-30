---------------------------------------------------------
-- generic ripple-carry-adder made up from full adders
---------------------------------------------------------

entity addg is 
  generic(width : natural := 8);
  port (A, B    : in  bit_vector (width-1 downto 0);
        C_in    : in  bit;
        S       : out bit_vector (width-1 downto 0);
        C       : out bit);
end;

architecture STRUCTURE of addg is
	component full_adder
		port (A, B, C_in : in bit;
		      S, C_out   : out bit);
	end component;
	signal CARRY : bit_vector(width downto 0);
begin
	carry(0) <= C_in;
	gen1: for i in 0 to width-1 generate
	  vai: full_adder port map (A => A(i), B => B(i), C_in => carry(i), C_out => carry(i+1), S => S(i));
        end generate;
	c <= carry(width);
end;
