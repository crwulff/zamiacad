library ieee;
use ieee.std_logic_1164.all;

entity varloop is
   port(b, clk : in  std_logic;
        s : out std_logic);
end; 

architecture logic of varloop is
begin
	process
		variable	v : std_logic;
	begin
		if b then
			v := '0';
		end if;
		if clk'event and clk='1' then
			s <= v;
		end if;
		v := not v;
	end process ;
end;
	

