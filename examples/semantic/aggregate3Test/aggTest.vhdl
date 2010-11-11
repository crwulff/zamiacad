entity e1 is
   port (a : out bit; b: in bit_vector (3 downto 0));
end entity e1;

architecture RTL of e1 is

begin
end architecture RTL;


entity aggTest is
  port( a, b : IN bit; z : OUT bit);
end entity aggTest;

architecture RTL of aggTest is 

begin

  foo: entity WORK.E1 port map (a => open, b => (others => '0'));

end architecture RTL;

