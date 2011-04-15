library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity foo is
end entity;

architecture rtl of foo is
    signal a : std_logic_vector(3 downto 0) := "0001";
    signal b : std_logic_vector(3 downto 0) := "0000";
begin
    process
    begin

        assert (a > b) report "'0001 > 0000' failed";

        assert (b < a) report "'0000 < 0001' failed";

        a <= "UUUU";
        wait for 0 ns;
        assert not (a > b) report "'not (UUUU > 0000)' failed";
        assert not (b < a) report "'not (0000 < UUUU)' failed";

        a <= "0001";
        b <= "0010";
        wait for 0 ns;
        assert not (a > b) report "'not (0001 > 0010)' failed";
        assert not (b < a) report "'not (0010 < 0001)' failed";
        assert b > a report "0010 > 0001' failed";

        a <= "0011";
        b <= "1010";
        wait for 0 ns;
        assert not (a > b) report "'not (0001 > 0010)' failed";
        assert not (b < a) report "'not (0010 < 0001)' failed";
        assert b > a report "0010 > 0001' failed";

        wait;
    end process;
end architecture;