entity counter is
        generic(width : integer := 8);
	port( reset, clk : IN bit;
	      z          : OUT bit_vector (width-1 downto 0)
	);
end entity counter;


architecture RTL of counter is 
	
	signal s, s_next : bit_vector (width-1 downto 0);
	signal co        : bit;
	
begin
  
  reg : process is
  begin
    wait until clk'event and clk='1';
    
    if reset = '1' then
      s <= (others => '0');
    else
      s <= s_next;
    end if;
      	
  end process reg;
  
  addg: entity WORK.addg generic map (width => width) port map (a => s, b => (0=>'1', others=>'0'), c_in => '0', s => s_next, c => co);

  z <= s;

end architecture RTL;
