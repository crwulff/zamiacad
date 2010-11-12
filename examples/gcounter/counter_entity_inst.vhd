entity counter_entity_inst is
        generic(width : integer := 8);
	port( reset, clk : IN bit;
	      z          : OUT bit_vector (width-1 downto 0)
	);
end entity counter_entity_inst;


architecture RTL of counter_entity_inst is 
begin
  
  assert true = false report "Wrong architecture used: RTL";
  
end architecture RTL;

architecture RTL_entity_inst of counter_entity_inst is 
begin
end architecture RTL_entity_inst;
