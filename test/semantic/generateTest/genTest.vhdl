
entity genTest is
  port( a, b : IN bit; z : OUT bit);
end entity genTest;

architecture RTL of genTest is 

begin

  gloop: for i in boolean'low to boolean'high generate
  end generate;

end architecture RTL;

