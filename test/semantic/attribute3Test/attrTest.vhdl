entity attrTest is
  port (a, b : in bit; z : out bit);
end entity attrTest;

architecture RTL of attrTest is 

begin
  
  z <= '1' when a'event  and (b'delayed /= a) else '0'; 

end architecture RTL;

