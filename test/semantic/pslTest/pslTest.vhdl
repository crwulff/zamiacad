entity pslTest is
  port( a, b : IN bit; z : OUT bit);
end entity pslTest;

architecture RTL of pslTest is 

begin

  cover {start_trans[*]; not end_trans[*];start_trans and end_trans}
        report "Transactions overlapping by one cycle covered" ;

 cr0:cover { a_b0 = to_unsigned(0,v'length) } report "report1";

 cover { a AND b } report "report2";
 cover { a AND NOT b } report "report3";
 cover { a AND NOT b; c } report "report4";


end architecture RTL;

