library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_misc.all;
use IEEE.std_logic_unsigned.all;
use IEEE.std_logic_arith.all;

entity alu is
  port (
    s : in  std_logic_vector(2 downto 0);
    a : in  std_logic_vector(31 downto 0);
    b : in  std_logic_vector(31 downto 0);
    q : out std_logic_vector(31 downto 0);
    zero : out std_logic
    );
end alu;

architecture behavioral of alu is
begin
  process (s, a, b)
  begin

    zero   <= '0';
    if b(31 downto 0) = "00000000000000000000000000000000" then
      zero <= '1';
    end if;

    case s is
      when "000"  =>                    -- ADD
        q              <= a + b;
      when "001"  =>                    -- SUB
        q              <= a - b;
      when "010"  =>                    -- AND
        q              <= (a and b);
      when "011"  =>                    -- OR
        q              <= (a or b);
      when "100"  =>                    -- CP
        q              <= a;
      when "101"  =>                    -- NOT
        q              <= not a;
      when "110"  =>                    -- SAL
        q(31 downto 1) <= a(30 downto 0);
        q(0)           <= '0';
      when "111"  =>                    -- SAR
        q(30 downto 0) <= a(31 downto 1);
        q(31)          <= a(31);
      when others => null;
    end case;
  end process;
end behavioral;
