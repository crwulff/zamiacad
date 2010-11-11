-- counter.vhdl

library IEEE;
use IEEE.std_logic_1164.all;

entity COUNTER is
      Port (     CLK : In    STD_LOGIC;
              ENABLE : In    STD_LOGIC;
                MODE : In    STD_LOGIC;
              REG_IN : In    std_logic_vector (3 downto 0);
                 RES : In    STD_LOGIC;
              PC_OUT : Out   std_logic_vector (3 downto 0) );
end COUNTER;

architecture BEHAVIORAL of COUNTER is
  signal pc_state: std_logic_vector (3 downto 0);
begin

  PC_OUT <= pc_state;
  
  process
  begin
    wait until (clk'event and clk = '1');
    if (RES = '1') then pc_state <= "0000"; end if;
    if (ENABLE = '1') then
      if (MODE = '1') then pc_state <= REG_IN; end if;
      if (MODE = '0') then pc_state <= pc_state + "0001"; end if;
    end if;
  end process;

end BEHAVIORAL;

