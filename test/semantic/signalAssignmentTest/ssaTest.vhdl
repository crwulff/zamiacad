
entity ssaTest is
	port( a, b : IN bit; z : OUT bit);
end entity ssaTest;

architecture RTL of ssaTest is 
  signal c,d   : integer := 0;
  signal s1    : bit := '0';
begin

  ssa1: with c + d select
  	s1 <= '0', '1' after 20 ns, '0' after 50ns when 0,
  	'1' when 1,
  	'0' when others;
  
  testbench: process is
  
  begin

  	wait for 1ns;
  	
  	assert s1 = '0' report "SSA fail 1";
  	
  	wait for 20ns;
  	
  	assert s1 = '1' report "SSA fail 2";
  	
  	wait for 50ns;
  	
  	assert s1 = '0' report "SSA fail 3";
  
    c <= 1;
    
    wait for 1ns;
    
    assert s1 = '1' report "SSA fail 4";
    
    wait for 200ns;
    
    assert s1 = '1' report "SSA fail 5";
  	
  	d <= 1;

    wait for 1ns;
    
    assert s1 = '0' report "SSA fail 6";
    
    wait for 200ns;
    
    assert s1 = '0' report "SSA fail 7";
  	
  	
  end process testbench;

  
end architecture RTL;

