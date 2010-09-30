-- when aggregate expression

library ieee;
use ieee.std_logic_1164.all;

entity whenamp is
   port(
      g      : in std_logic_vector(6 downto 0);
      z      : out std_logic
       );
end;

architecture rtl of whenamp is

signal bar : std_logic;

begin

  with g(5 downto 0) select

    bar <= '1' when "00" & x"0", '0' when others;
  

end;



