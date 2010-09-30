entity pslTest is
  port( a, b : IN bit; z : OUT bit);
end entity pslTest;

architecture RTL of pslTest is 

begin

 foo: assert 
   {  NOT a;
     (NOT v(0) AND NOT v(1))[*2];
     ( v(0)    OR      v(1));         
     ( v(0)    AND     v(1))   
   }
 report "r1";

  sync: block
    signal a : bit_vector(0 to 1) := "00";
  begin
  end block;


end architecture RTL;

