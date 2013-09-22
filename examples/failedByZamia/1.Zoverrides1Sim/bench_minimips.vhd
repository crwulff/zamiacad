
-- Fails to set data line to 11. The problem vanishes   
-- if you the comment the comment_me process.

-- I see that first '1' is assigned at 20 ns and, 
-- at the next delta-cycle, z is assigned because 
-- "comment me" is executed upon data sig takes 
-- new value. This seems to be ok but this new value
-- is Z and it should not override the 1 driven by the 
-- first process.

library IEEE;
use IEEE.std_logic_1164.all;

entity sim_minimips is end;

-- This is ok since I have renamed std_vector=>std_logic
architecture bench of sim_minimips is

	signal data : std_logic;
	signal clk, loaded: boolean;
    
begin

    loaded <= true;
    data <= 'L';
	data <= '1' after 20 ns when loaded else 'Z'; 

	-- comment the assignment and the problem is gone
    comment_me: process (data) begin
        data <= 'Z';
    end process;

	-- clock to stimulate the viewform (it does not appear  
	-- without signal activity)
    clk <= not clk after 11 ns;    

    process begin
        wait for 25 ns;
        assert data = '1' report "It must be that 'data'=1, yet it is " & std_logic'image(data) & " actually" severity error;
        wait;
    end process;

end bench;


--architecture bench of sim_minimips is
--
--	signal data : std_logic_vector(1 downto 0);
--	signal clk, loaded: boolean;
--    
--begin
--
--    loaded <= true;
--    data <= "LL";
--	data <= "11" after 20 ns when loaded else "ZZ"; 
--
--    comment_me_and_problem_disappears: process (data) begin 
--        data <= "ZZ";
--    end process;
--
--	-- clock to stimulate the viewform (it does not appear  
--	-- without signal activity)
--    clk <= not clk after 11 ns;    
--
--    process begin
--        wait for 25 ns;
--        assert data = "11" report "Signal 'data' must equal 11" severity error;
--        wait;
--    end process;
--
--end bench;



