
entity e2 is
   port (a : in bit);
end entity e2;

entity e1 is
   port (a : in bit);
end entity e1;

architecture RTL of e1 is
begin
end architecture RTL;


entity instTest is
end entity instTest;

architecture RTL of instTest is 

  signal s : integer;

begin

  foo: entity WORK.e2 port map (a => s);


end architecture RTL;

