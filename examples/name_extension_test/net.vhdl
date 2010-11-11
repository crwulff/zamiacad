package libcache is
  
type arr1dim is array(3 downto 0) of integer;

type arrOff is array(42 downto 39) of bit;

type arr2dim is array(3 downto 0, 3 downto 0) of integer;

type rtype is record
	bar: integer;
	mil: arr1dim;
	mil2: arr2dim;
end record;

end;

use work.libcache.all;

entity NET2 is 
  generic (g1 : rtype; g2: arr1dim; g3: arrOff);
  port (A : in bit;
        S : out rtype);
end;

architecture STRUCTURE of NET2 is
begin

end;

use work.libcache.all;

entity NET is 
  port (A : in bit;
        S : out rtype);
end;

architecture STRUCTURE of NET is

signal s1 : integer;
signal s2 : arr1dim;
signal s3 : arr2dim;
signal s4 : arrOff;
signal s5 : integer;

begin
   s5 <= s3(2 downto 1,2 downto 1)(1,2);

   inst1: entity WORK.NET2 generic map (g1.bar => 0, g1.mil => (1,2,3,4),  g1.mil2 => ((1,2,3,4),(1,2,3,4),(1,2,3,4),(1,2,3,4)), g2(0) => 1,
	g2(1) => 2, g2(2) => 3, g2(3) => 4, g3(42) => '0', g3(41) => '1', g3(40) => '0', g3(39) => '1') 
        port map (a => A, s.bar => s1, s.mil => s2, s.mil2 => s3);

   s.bar <= 42;
   s.mil(3) <= 1;
   s.mil <= (1,2,3,4);
   s.mil2(1)(2) <= 23;

   s4 <= "0101";
   s4(42 downto 41) <= "11";
   s4(39) <= '1';

end;

