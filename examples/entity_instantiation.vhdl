library ieee;
use ieee.std_logic_1164.all;

entity e1 is
   port(
      sel      : in std_logic_vector(3 downto 0);
      canrx    : out std_logic
       );
end;

architecture rtl of e1 is
begin
end;

library ieee;
use ieee.std_logic_1164.all;

entity e2 is
   port(
      a        : in std_logic;
      z        : out std_logic
       );
end;

architecture rtl of e2 is
begin
  rgc:entity work.e1(rtl) port map (sel(3 downto 1) => "000", sel(0) => a, canrx => z);
end;
