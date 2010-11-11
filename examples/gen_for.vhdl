library ieee;
use ieee.std_logic_1164.all;

entity TERM_A is
   port(
      a      : in std_logic;
      z      : out std_logic
       );
end;

architecture rtl of TERM_A is

begin
	z <= not a;
end;

library ieee;
use ieee.std_logic_1164.all;

entity gen_for is
   port(
   	  reset_vector_d : in std_logic_vector (16 downto 0);
      inv_reset		 : out std_ulogic_vector (16 downto 0)
       );
end;

architecture rtl of gen_for is

begin

   reset_bus_term3 : for i in (1*8 + 2) to (1*8 + 5) generate
     term : entity TERM_A port map(A => reset_vector_d(i), Z => inv_reset(i));
   end generate reset_bus_term3;
end;
