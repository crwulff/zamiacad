entity litTest is
  port (v2 : IN bit_vector(0 to 31));
end entity litTest;

architecture RTL of litTest is 

  constant str : string := "abc 'a'";

begin

end architecture RTL;

