entity blockTest is
  port (a : in bit;
        pz : out bit);
end entity blockTest;

architecture test of blockTest is

  constant GLOBAL_WIDTH : positive := 42;

  signal sz : bit;

begin

  b1 : block is
         generic ( width: positive := 23);
         generic map (width => GLOBAL_WIDTH);
         port ( z : out bit );
         port map ( z => sz );
  begin
    z <= a;
  end block b1;

  pz <= sz;
  
end architecture test;
