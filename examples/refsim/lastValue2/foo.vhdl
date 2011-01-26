library IEEE;
use IEEE.STD_LOGIC_1164.all;

entity foo is
end; 

architecture RTL of foo is

  signal clk : std_logic := '0';
  signal v   : std_logic := '0';

begin
  
  clk <= not clk after 50 ns;
  
  process (clk) is
    
  begin
    if rising_edge(clk) then
      v <= not v;
    end if;
  end process;
  
  tm: process is  
  begin

    assert v = '0' report "initialisation failed.";

    wait for 100 ns;

    assert v = '1' report "1st rising_edge failed.";

    wait for 100 ns;

    assert v = '0' report "2nd rising_edge failed.";

    wait for 100 ns;

    assert v = '1' report "3rd rising_edge failed.";

    wait;
  end process;

   
end;
