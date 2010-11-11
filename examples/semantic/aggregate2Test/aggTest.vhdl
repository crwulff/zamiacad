
entity aggTest is
  port( a, b : IN bit; z : OUT bit);
end entity aggTest;

architecture RTL of aggTest is 

 type rt2 is record
   f4:bit;
   f5:bit;
   f6:bit;
 end record rt2;

 type rt1 is record
   f1:natural;
   f2:natural;
   f3:rt2;
 end record rt1;

 constant c1 : rt1 := (23, 42, rt2);

begin

end architecture RTL;

