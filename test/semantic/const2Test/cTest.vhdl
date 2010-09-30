entity e2 is
end entity e2;

architecture RTL of e2 is

begin
end architecture RTL;

entity e1 is
	generic(w : integer; id : string := "");
	port( a : IN bit; 
              z : OUT bit);
end entity e1;

architecture RTL of e1 is

begin

  blubb: if id = "" generate
    foo: entity WORK.E2;
  end generate;
  
end architecture RTL;

entity tcTest is
	port( a : IN bit; 
              z : OUT bit);
end entity tcTest;

architecture RTL of tcTest is

begin

  foo: entity WORK.E1 generic map (w => 23) port map (a => a, z => z);

end architecture RTL;

