
entity subProgramTest is
	port( a, b : IN bit; z : OUT integer);
end entity subProgramTest;

architecture RTL of subProgramTest is 
  signal s : integer;
begin
  
  testbench: process is
  
  	function addF(p1, p2 : IN integer) return integer is
  	begin
  	   return p1+p2;
  	end addF;
  
  begin

	s <= addF(21,21);
    
    wait for 1ns;
    
    assert s=42 report "addF failed";
  	
  	wait;
  	
  end process testbench;
  
  z <= s;
  
end architecture RTL;

