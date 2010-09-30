
entity counter_tb is
end entity counter_tb;


architecture RTL of counter_tb is
  constant width : integer := 4;

  signal clk   : bit := '0';
  signal reset : bit := '1';
  signal z     : natural;
begin
  
  clkgen: clk <= not clk after 10 ns;
 
  counter : process (reset, clk) is
  begin
     if reset = '1' then
        z <= 0;
     else
        if clk'event and clk='1' then
           z <= z + 1;
        end if;
     end if;
  end process counter;
 
  tester : process is
  begin
  	
  	reset <= '1';
  	
  	wait for 1 ns;
  	
  	assert z = 0 report "1: Reset test failed.";  	
  	reset <= '0';
  	
  	wait for 100 ns;
  	
  	assert z = 5 report "2: count test failed.";

  	reset <= '1';
  	
  	wait for 50 ns;
  	
  	assert z = 0 report "3: Reset test failed.";  	

  	reset <= '0';
  
        wait;	
  end process tester;
  
end architecture RTL;
