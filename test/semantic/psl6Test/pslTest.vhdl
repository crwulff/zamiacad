entity pslTest is
  port( a, s1,s2,s3,s4 : IN bit; z : OUT bit);
end entity pslTest;

architecture RTL of pslTest is 

begin

l:assert (always rose(a)
               -> next (s1)) @ rose(s2)
      report "request";
no_ack_wo_req:assert (always (not s3)
            -> next((not s1) until (s3))) @ rose(s2)
   report "n";
r1:assert (always (s1 AND s4=0)
            -> next((not (s1 AND s4=0)) until (s1 AND s4=1))) @ rose(s2)
   report "r";
r2:assert (always (s1 AND s4=1)
            -> next((not (s1 AND s4=1)) until (s1 AND s4=2))) @ rose(s2)
   report "r";
r3:assert (always (s1 AND s4=2)
            -> next((not (s1 AND s4=2)) until (s1 AND s4=3))) @ rose(s2)
   report "r";
r0:assert (always (s1 AND s4=3)
            -> next((not (s1 AND s4=3)) until (s1 AND s4=0))) @ rose(s2)
   report "r";
cover  {s1 AND s4=0};
cover  {s1 AND s4=1};
cover  {s1 AND s4=2};
cover  {s1 AND s4=3};



end architecture RTL;

