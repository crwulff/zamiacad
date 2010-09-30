
entity waitTest is
end entity waitTest;

architecture RTL of waitTest is 
  signal s1, s2, s3, s4, s5, s6 : bit := '0';
begin
  
  waiter: process is
  begin
  
    wait on s1;
  
    s4 <= '1';
  
    wait on s2 until s2 or s3 for 10ns;
  
    s5 <= '1';

    wait on s1 until s2 or s3 for 10ns;
  
    s6 <= '1';
  
    wait;
  end process waiter;
  
  testbench: process is
  begin
  
    wait for 1ns;
    
    -- trigger s1 change
    
    s1 <= '1';
    
    wait for 1ns;
    
    assert s4='1' and s5='0' and s6='0' report "wait on signal change failed";
    
    -- trigger s2 or s3
    
    s2 <= '1';
    
    wait for 1ns;
    
    assert s4='1' and s5='1' and s6='0' report "wait for condition failed";
    
    -- wait for timeout
    
    s3 <= '1';
    
    wait for 11ns;
    
    assert s4='1' and s5='1' and s6='1' report "wait timeout failed";
  	
  	wait;
  	
  end process testbench;
  
  
end architecture RTL;

