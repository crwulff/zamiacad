
entity genTest is
  port( a, b : IN bit; z : OUT bit);
end entity genTest;

architecture RTL of genTest is 

  constant c3 : bit_vector (1 to 4) := "1111";

  constant w  : positive := 4;

  constant c  : bit_vector := c3 & (w to 63 => '0');

begin

  gloop: for i in 0 to 63 generate

    gi: if c(i) = '1' generate
    end generate;

  end generate;

end architecture RTL;

