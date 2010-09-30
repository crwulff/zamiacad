
entity ccTest is
  port( a, b : IN bit; z : OUT bit);
end entity ccTest;

architecture RTL of ccTest is 

   constant w       : positive := 8;
   constant v2      : bit_vector(2 to 5) := "0101";

   constant v       : bit_vector(0 to w-1) := v2 & (v2'length to w-1 => '0');

begin

end architecture RTL;

