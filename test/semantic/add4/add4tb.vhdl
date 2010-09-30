--  A testbench has no ports.
entity add4tb is
end add4tb;

architecture behav of add4tb is
  --  Declaration of the component that will be instantiated.
  component add4
    port (A, B    : in  bit_vector (3 downto 0);
          C_in    : in  bit;
          S       : out bit_vector (3 downto 0);
          C       : out bit);
  end component;
  --  Specifies which entity is bound with the component.
  for adder_0: add4 use entity work.add4;
  signal a,b,s       :  bit_vector (3 downto 0);
  signal c,co,done   :  bit;
begin
  --  Component instantiation.
  adder_0: add4 port map (a => a, b => b, C_in => c,
			   s => s, c => co);

  --  This process does the real job.
  testbench: process
  begin
  	 done <= '0';
  
     a <= "0000";
     b <= "0001";
     c <= '0';
     wait for 1 ns;
     assert s = "0001" report "assert #1 failed.";
     
     a <= "0011";
     b <= "0001";
     c <= '0';
     wait for 1 ns;
     assert s = "0100" report "assert #2 failed.";
     
     a <= "0011";
     b <= "1001";
     c <= '0';
     wait for 1 ns;
     assert s = "1100" report "assert #3 failed.";
     

     a <= "1111";
     b <= "0001";
     c <= '0';
     wait for 1 ns;
     assert s = "0000" report "assert #4 failed.";
     
     done <= '1';
     wait for 1 ns;
     
     wait;
  end process;
  
  supervisor: process
  begin
  	wait for 500 ns;
  	
  	assert done='1' report "failed to finish testbench";
  	
  	wait;
  end process;
  
end behav;

