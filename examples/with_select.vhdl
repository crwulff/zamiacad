library IEEE;
use IEEE.std_logic_1164.all;

entity new_file is
    Port (
        a1: in  std_logic;
        a2: in  std_logic;
        b1: in  std_logic_vector(1 downto 0);
        c1: out std_logic
    );
end new_file;

architecture behavioral of new_file is
begin
    with b1 select
        c1 <= a1 when "00",
              a2 when "01",
              '0' when others;
end behavioral;
