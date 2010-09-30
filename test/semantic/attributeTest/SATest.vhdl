
-- signal attributes test

entity saTest is
	port( a, b : IN bit; z : OUT bit);
end entity saTest;

architecture RTL of saTest is 
  signal clk : bit := '0';
  signal s1,s2 : bit := '0';
  signal finished : bit := '0';
begin
  
  clkgen: clk <= not clk after 10ns;
  
  clkout: z <= clk;
  
  testbench: process is
  
  begin

	-- check all predefined signal attributes

	wait for 19ns;
	
	assert clk'stable(3ns) report "CLK stable check 1 failed.";

	assert clk'quiet(3ns) report "CLK quiet check 2 failed.";

	assert clk'stable(11ns)=FALSE report "CLK stable check 3 failed.";

	assert clk'quiet(11ns)=FALSE report "CLK quiet check 4 failed.";

	assert clk'delayed(4ns)='1' report "CLK delayed check 5 failed.";

	assert clk'delayed(14ns)='0' report "CLK delayed check 6 failed.";
	
	assert clk'transaction='0' report "CLK transaction check 7 failed.";

	assert clk'event=FALSE report "CLK event check 8 failed.";

	assert clk'active=FALSE report "CLK active check 9 failed.";

	assert clk'last_event=9ns report "CLK last_event check 10 failed.";

	assert clk'last_active=9ns report "CLK last_active check 11 failed.";

	assert clk'last_value='0' report "CLK last_value check 12 failed.";

	assert clk'driving=FALSE report "CLK driving check 12 failed.";

	wait on clk ;  
	
	assert clk'event=TRUE report "CLK event check 13 failed.";

	assert clk'active=TRUE report "CLK active check 14 failed.";

	-- check transaction vs event

	-- create events on both signals
	
	s1 <= not s1 after 10 ns;
    s2 <= not s2 after 10 ns;
    wait on s1;
    
    assert s1'event  report "S1 event check 15 failed.";
    assert s2'event  report "S2 event check 16 failed.";
    assert s1'active report "S1 active check 17 failed.";
    assert s2'active report "S2 active check 18 failed.";
    
	-- create transactions on both signals
	-- but only s1 has an event
	
	s1 <= not s1 after 10 ns;
    s2 <= s2 after 10 ns;
    wait on s1;
    
    assert s1'event      report "S1 event check 19 failed.";
    assert not s2'event  report "S2 event check 20 failed.";
    assert s1'active     report "S1 active check 21 failed.";
    assert s2'active     report "S2 active check 22 failed.";

    finished <= '1';

  	wait;
  	
  end process testbench;

  fincheck: process is
  begin
    wait for 50ns;
    assert finished = '1' report "testbench did not finish in time";
  end process fincheck;
  
end architecture RTL;

