entity counter_tb_entity_inst is
end entity counter_tb_entity_inst;


architecture RTL of counter_tb_entity_inst is
  constant width : integer := 4;

  signal clk   : bit := '0';
  signal reset : bit := '1';
  signal z     : bit_vector (width-1 downto 0);
begin
  
  counter0: entity WORK.counter_entity_inst(RTL_entity_inst) generic map (width => width) port map(reset => reset, clk => clk, z => z);
  
  clkgen: clk <= not clk after 10 ns;
  
  tester : process is
  begin
  	
  	reset <= '1';
  	
  	wait for 1 ns;
  	
  	assert z = "0000" report "1: Reset test failed.";  	
  	reset <= '0';
  	
  	wait for 100 ns;
  	
  	assert z = "0101" report "2: count test failed.";

  	reset <= '1';
  	
  	wait for 50 ns;
  	
  	assert z = "0000" report "3: Reset test failed.";  	
  
    wait;	
  end process tester;
  
end architecture RTL;
