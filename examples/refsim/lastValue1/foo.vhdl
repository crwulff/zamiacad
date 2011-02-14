library IEEE;
use IEEE.STD_LOGIC_1164.all;

entity foo is
end;

architecture RTL of foo is

  signal clk : std_logic := '0';

begin

  clk <= not clk after 50 ns;

  tm: process is
  begin

    assert clk = '0' report "initialisation failed.";
    assert clk'LAST_VALUE = '0' report "LAST_VALUE failed on initialisation.";

    wait for 50 ns;

    assert clk = '1' report "1st transition failed.";
    assert clk'LAST_VALUE = '0' report "1st LAST_VALUE failed.";

    wait for 50 ns;

    assert clk = '0' report "2nd transition failed.";
    assert clk'LAST_VALUE = '1' report "2nd LAST_VALUE failed.";

    wait for 50 ns;

    assert clk = '1' report "3rd transition failed.";
    assert clk'LAST_VALUE = '0' report "3rd LAST_VALUE failed.";

    wait for 50 ns;

    assert clk = '0' report "4th transition failed.";
    assert clk'LAST_VALUE = '1' report "4th LAST_VALUE failed.";

    wait for 1 ns;

    assert clk = '0' report "4.5th value preservation failed.";
    assert clk'LAST_VALUE = '1' report "4.5th LAST_VALUE preservation failed.";

    wait;
  end process;


end;
