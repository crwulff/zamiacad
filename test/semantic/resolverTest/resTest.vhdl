entity resTest is
  port (a : in bit; y : out bit);
end entity resTest;

architecture RTL of resTest is 

begin

  y <= RESTEST.a;

end architecture RTL;

