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

     assert  -- label would be nice, but I forgot the trick
        ( always
          {  NOT a;
            (NOT b AND NOT c(1))[*2];
            ( d(0)    OR      e(1));                
            ( f(0)    AND     g(1))                
          }
          |=> next_e[2 to 50](h(0)  AND    i(1)) 
        ) @ j
       report "k";


end architecture RTL;

