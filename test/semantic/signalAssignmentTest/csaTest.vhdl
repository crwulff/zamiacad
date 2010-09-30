
entity csaTest is
	port( a, b : IN bit; z : OUT bit);
end entity csaTest;

architecture RTL of csaTest is 
  signal c     : bit := '0';
  signal s1,s2 : bit := '0';
  signal guard : bit := '1';
begin

  csa1: s1 <= '0', '1' after 20 ns when c else '1', '0' after 50 ns, '1' after 100ns; 
  
--  csa2: postponed s2 <= GUARDED REJECT 10 ns INERTIAL b when c else d;
  
  testbench: process is
  
  begin

  	wait for 1ns;
  	
  	assert s1 = '1' report "CSA fail 1";
  	
  	wait for 50ns;
  	
  	assert s1 = '0' report "CSA fail 2";
  	
  	wait for 50ns;
  	
  	assert s1 = '1' report "CSA fail 3";
  
    c <= '1';
    
    wait for 1ns;
    
    assert s1 = '0' report "CSA fail 4";
    
    wait for 20ns;
    
    assert s1 = '1' report "CSA fail 5";
  
  	
  end process testbench;

  
end architecture RTL;

