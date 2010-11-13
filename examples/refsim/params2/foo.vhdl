entity foo is
end; 

architecture RTL of foo is

  signal clk : bit := '0';
  signal clkn : bit;

begin

  tm: process is
  
    FUNCTION rising_edge  (SIGNAL s : bit) RETURN BOOLEAN IS
    BEGIN
        RETURN s'EVENT AND s = '1';
    END;
  
  begin
  
    if rising_edge(clk) then
      clkn <= not clk;
    end if;
  
  end process;

  clk <= not clk after 10ns;
   
end;
