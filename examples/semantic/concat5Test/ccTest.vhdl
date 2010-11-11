
entity ccTest is
  generic (width : positive := 3);
  port( a, b : IN bit; z : OUT bit);
end entity ccTest;

architecture RTL of ccTest is 

   function f(s : bit_vector) return bit_vector is
   begin
      return s;
   end f;

constant v1 : bit_vector(1 to 2) := "11";

CONSTANT v2 : bit_vector(0 to width-1) := v1 & (v1'length to width-1 => '0');

CONSTANT v3 : bit_vector(0 to width-1) := f(v2(0 to width-1));

begin

end architecture RTL;

