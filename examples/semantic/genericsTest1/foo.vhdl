
-- an entity which cannot be instantiated with default generics (because there are none)

entity bar is

  generic (w : integer);

  port (a : in bit_vector (0 to w-1));
end bar;

architecture arch of bar is
begin
end;


entity foo is

end; 

architecture RTL of foo is

  signal s : bit_vector (7 downto 0);

begin

   u1 : entity work.bar
	      generic map(
		      w => 8
	      )
	      port map(a => s);
 
end;
