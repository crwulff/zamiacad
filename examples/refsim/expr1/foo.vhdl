entity foo is
end; 

architecture RTL of foo is

signal a,b,c,d : bit;
signal z : bit;

begin

  z <= (a or b) or (c or d);

  tester : process is
  begin
  	
    a <= '0';
    b <= '0';
    c <= '0';
    d <= '0';
  	
    wait for 10 ns;
  
    a <= '0';
    b <= '1';
    c <= '0';
    d <= '0';
    
    wait for 1ns;
    
    assert z = '1' report "OR operation failed";
  	
    wait for 10 ns;
  
    a <= '0';
    b <= '0';
    c <= '0';
    d <= '0';
  	
    wait for 10 ns;
  
    wait;	
  end process tester;
   
end;
