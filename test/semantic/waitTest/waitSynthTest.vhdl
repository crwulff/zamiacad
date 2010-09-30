entity waitSynthTest is
	port( a, clk : IN bit;
	      z      : OUT bit
	);
end entity waitSynthTest;


architecture RTL of waitSynthTest is 
	-- decl
begin

	flipflop : process is
	begin
		wait until clk'event and clk='1';
		--wait until raising_edge(clk);
		z <= a;
	end process flipflop;
	
	
  
end architecture RTL;
