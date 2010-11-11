entity typeTest is
  port( a, b : IN bit; z : OUT bit);
end entity typeTest;

architecture RTL of typeTest is 

  type tr is record f1 : integer; f2 : bit ; end record;

  SIGNAL tr : tr;
  signal i : integer;

begin

  i <= tr.f1;

end architecture RTL;

