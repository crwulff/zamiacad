entity attrTest is
  port (a, b : in bit; z : out bit);
end entity attrTest;

architecture RTL of attrTest is 

  type ta is array (31 downto 0) of bit;
  constant c1 : integer := ta'length;
  type t2 is array (ta'reverse_range) of integer;

begin
  
end architecture RTL;

