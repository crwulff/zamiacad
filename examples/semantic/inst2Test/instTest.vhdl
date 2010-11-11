
entity e1 is
end entity e1;

architecture RTL of e1 is
begin
end architecture RTL;


entity instTest is
end entity instTest;

architecture RTL of instTest is 

  component e1 is
  end component;

begin

  foo: e1;

end architecture RTL;

