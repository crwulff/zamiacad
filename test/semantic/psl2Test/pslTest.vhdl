entity pslTest is
  port( a, b : IN bit; z : OUT bit);
end entity pslTest;

architecture RTL of pslTest is 

begin

 foo: assert ( always
   {  NOT a;
     (NOT v(0) AND NOT v(1))[*2];
     ( v(0)    OR      v(1));         
     ( v(0)    AND     v(1))   
   }
   |=> next_e[2 to 50](v(0) AND v(1)) 
 ) @ clk report "r1";

foo2:assert
       ( always
         { f1;
NOT f2 }
         |=> {(NOT f3)[*1 to inf]}
       )
       report "hubba bubba";



end architecture RTL;

