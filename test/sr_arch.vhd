library ieee;
use ieee.std_logic_1164.all;
use WORK.sr_p1.ALL;
use WORK.sr_p2.ALL;

entity sr_arch is
   port(
      a        : in real;
      b		   : in std_logic_vector(7 downto 0);
      c        : in integer;
      z        : out std_ulogic_vector;
       );
end;

architecture rtl of sr_arch is

begin

	z <= to_std_ulogic_vector(b);

end;
