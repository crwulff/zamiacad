entity foo is
end; 

architecture RTL of foo is

  signal clk : bit := '0';
  signal clkn : bit := '0';

begin

  tm: process is
  
    FUNCTION rising_edge  (SIGNAL s : bit) RETURN BOOLEAN IS
    BEGIN
        RETURN s'EVENT AND s = '1';
    END;
  
  begin
  
    if rising_edge(clk) then
      clkn <= not clkn;
    end if;
  
  end process;

  clk <= not clk after 10ns;

  process is
  begin

    assert clk = '0' report "clk initialisation failed";
    assert clkn = '0' report "clkn initialisation failed";

    wait for 10 ns;
    wait for 1 ns;

    assert clk = '1' report "(1) clk toggle failed";
    assert clkn = '1' report "(1) clkn toggle failed";

    wait for 10 ns;

    assert clk = '0' report "(2) clk toggle failed";
    assert clkn = '1' report "(2) clkn toggle failed (parameter's event fired)";

    wait for 10 ns;

    assert clk = '1' report "(3) clk toggle failed";
    assert clkn = '0' report "(3) clkn toggle failed (parameter's event failed)";

    wait for 10 ns;

    assert clk = '0' report "(4) clk toggle failed";
    assert clkn = '0' report "(4) clkn toggle failed (parameter's event fired)";

    wait for 10 ns;

    assert clk = '1' report "(5) clk toggle failed";
    assert clkn = '1' report "(5) clkn toggle failed (parameter's event failed)";

    wait;
  end process;
end;
