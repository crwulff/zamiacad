library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_std.all;

entity foo is

  constant Nk	: NATURAL := 23;

end; 

architecture RTL of foo is

	signal i			 : UNSIGNED(5 downto 0);  -- register

begin

  g1 : for i in 0 to Nk-1 generate
    g2: if i /= Nk-1 generate
    end generate g2;
  end generate g1;
   
end;
