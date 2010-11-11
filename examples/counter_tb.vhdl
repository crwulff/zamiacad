-- counter_tb.vhd

library IEEE;
use IEEE.std_logic_1164.all;

entity COUNTER_TB is
end COUNTER_TB;

architecture test of COUNTER_TB is

  component COUNTER
      Port (     CLK : In    STD_LOGIC;
              ENABLE : In    STD_LOGIC;
                MODE : In    STD_LOGIC;
              REG_IN : In    std_logic_vector (3 downto 0);
                 RES : In    STD_LOGIC;
              PC_OUT : Out   std_logic_vector (3 downto 0) );
  end component;

  --for implementation : prog_count use configuration work.CFG_PROG_COUNT_BEHAVIORAL;

  signal clk: std_logic := '0';
  constant period: time := 100 ns;
  signal REG_IN, PC_OUT: std_logic_vector (3 downto 0);
  signal ENABLE, MODE, res: std_logic;
begin

  implementation: COUNTER port map (REG_IN => REG_IN, ENABLE => ENABLE, MODE => MODE, PC_OUT => PC_OUT, res => res, clk => clk);
  
  clk <= not clk after period / 2;
  
  process
  begin

    -- Steuerleitungen zuruecksetzen & Reset ueberpruefen
    wait until clk'event and clk = '0';
    ENABLE <= '0';
    MODE <= '0';
    res <= '1';
    wait until clk'event and clk = '0';
    res <= '0';
    assert PC_OUT = "0000";

    -- ENABLE = '0' pruefen
    MODE <= '1';
    wait until clk'event and clk = '0';
    assert PC_OUT = "0000";
    MODE <= '0';
    wait until clk'event and clk = '0';
    assert PC_OUT = "0000";
    
    -- Zaehlen (kleine werte)
    ENABLE <= '1';
    for i in 1 to 20 loop
      wait until clk'event and clk = '0';
      --assert PC_OUT = conv_std_logic_vector (i, 16);
    end loop;

    -- Wert halten
    ENABLE <= '0';
    for i in 1 to 20 loop
      wait until clk'event and clk = '0';
      --assert PC_OUT = conv_std_logic_vector (20, 16);
    end loop;

    -- Laden und grosse Werte zaehlen
    ENABLE <= '1';
    MODE <= '1';
    --REG_IN <= conv_std_logic_vector (32760, 16);
    wait until clk'event and clk = '0';
    MODE <= '0';
    for i in 1 to 20 loop
      wait until clk'event and clk = '0';
      --assert PC_OUT = conv_std_logic_vector (32760 + i, 16);
    end loop;
    
    -- Ende
    wait;
  end process;
    
end test;
