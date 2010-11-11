entity exprTest is
  port( a, b : IN bit; z : OUT bit);
end entity exprTest;

architecture RTL of exprTest is 

   type gbrecord is record
      a : integer;
      b : bit;
   end record gbrecord;

   signal s1, s2 : gbrecord;

begin

   assert s1=s2 report "difference on " severity error;

end architecture RTL;

