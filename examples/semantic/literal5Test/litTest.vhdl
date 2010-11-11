
entity litTest is
end entity litTest;

architecture RTL of litTest is 

  signal s1 : bit_vector (7 downto 0);

begin

   assert s1 = "00000100" report "PSA fail 1(1)";

end architecture RTL;

