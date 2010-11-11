entity pslTest is
  port( a, b : IN bit; z : OUT bit);
end entity pslTest;

architecture RTL of pslTest is 

begin

     assert  
        ( always (NOT a AND b(0) AND c(1))
                -> next((NOT d(0) OR NOT e(1)) until (f)))
      report "r";


end architecture RTL;

