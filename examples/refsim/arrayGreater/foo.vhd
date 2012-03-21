library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity foo is
end entity;

architecture rtl of foo is
    signal a : std_logic_vector(3 downto 0) := "0001";
    signal b : std_logic_vector(3 downto 0) := "0000";
    signal c : std_logic_vector(1 downto 0);
    signal br : std_logic_vector(0 to 3);
begin
    process
    begin

        assert (a > b) report "'0001 > 0000' failed";
        assert (a >= b) report "'0001 >= 0000' failed";

        assert (b < a) report "'0000 < 0001' failed";
        assert (b <= a) report "'0000 <= 0001' failed";

        a <= "UUUU";
        wait for 0 ns;
        assert not (a > b) report "'not (UUUU > 0000)' failed";
        assert not (b < a) report "'not (0000 < UUUU)' failed";
        assert (a >= b) report "'(UUUU >= 0000)' failed";
        assert (b <= a) report "'(0000 <= UUUU)' failed";

        a <= "0001";
        b <= "0010";
        wait for 0 ns;
        assert not (a > b) report "'not (0001 > 0010)' failed";
        assert not (b < a) report "'not (0010 < 0001)' failed";
        assert b > a report "0010 > 0001' failed";
        assert not (a >= b) report "'not (0001 >= 0010)' failed";
        assert not (b <= a) report "'not (0010 <= 0001)' failed";
        assert b >= a report "0010 >= 0001' failed";

        a <= "0011";
        b <= "1010";
        wait for 0 ns;
        assert not (a > b) report "'not (0001 > 0010)' failed";
        assert not (b < a) report "'not (0010 < 0001)' failed";
        assert b > a report "0010 > 0001' failed";
        assert not (a >= b) report "'not (0001 >= 0010)' failed";
        assert not (b <= a) report "'not (0010 <= 0001)' failed";
        assert b >= a report "0010 >= 0001' failed";

        -- different length
        a <= "0010";
        c <= "11";
        wait for 0 ns;
        assert (c > a) report "'11 > 0010' failed";
        assert (a < c) report "'0010 < 11' failed";
        assert (c >= a) report "'11 >= 0010' failed";
        assert (a <= c) report "'0010 <= 11' failed";

        -- DOWNTO vs TO
        a <= "0010";
        br <= "0010";
        wait for 0 ns;
        assert not (a > br) report "'not (0010(DOWN) > 0010(TO))' failed";
        assert (a >= br) report "'not (0010(DOWN) >= 0010(TO))' failed";
        assert not (br < a) report "'0010(TO) < 0010(DOWN)' failed";
        assert (br <= a) report "'0010(TO) <= 0010(DOWN)' failed";

        wait;
    end process;
end architecture;