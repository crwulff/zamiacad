--
-- a simple test case for the syntactic construct
--
-- VARIABLE <id> : <type> IS <name> ;
--

entity vdTest is
	port( a, b : IN bit; z : OUT bit);
end entity vdTest;

architecture RTL of vdTest is 

   function f1(p0: bit_vector) return bit is

     -- next line contains the possible illegal "IS <name>" construct
     VARIABLE v    : bit_vector(0 to 2) IS p0;

   begin
     v := "010";
     return v(0);
   end f1;

   constant c1 : bit := f1("101");

begin
  -- make sure c1 is used and f1 gets actually called
  z <= c1;
end architecture RTL;

