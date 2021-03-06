library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_std.all;

use work.sc_pack.all;

entity lru is
generic (
	index_bits : integer := 5;
	line_cnt : integer := 24);
port (
	clk, reset:	    in std_logic;

	inval:			in std_logic;

	cpu_out:		in sc_out_type;
	cpu_in:			out sc_in_type;

	mem_out:		out sc_out_type;
	mem_in:			in sc_in_type);
end lru;

architecture rtl of lru is

	type cache_line_type is record
		index	: std_logic_vector(index_bits-1 downto 0);
		tag		: std_logic_vector(SC_ADDR_SIZE-1 downto 0);
		valid	: std_logic;
	end record;

	type cache_line_array is array (integer range <>) of cache_line_type;

	signal ram_data : std_logic_vector(31 downto 0);
	signal ram_wraddress: std_logic_vector(index_bits-1 downto 0);
	signal ram_rdaddress: std_logic_vector(index_bits-1 downto 0);
	signal ram_wren : std_logic;
	signal ram_dout : std_logic_vector(31 downto 0);
	
	signal int_reset : std_logic;
	
	-- what state we're in
	type STATE_TYPE is (idle,
						wr0, wr1,
						rd0, rd1, rd2,
						ncrd0, ncrd1);
	signal state, next_state : state_type;
	
	-- enabling data shifting
	signal ena :		std_logic_vector(0 to line_cnt-1);
	-- connecting cache lines
	signal data :		cache_line_array(-1 to line_cnt-1);
    -- signaling hits
	signal hit :		std_logic_vector(0 to line_cnt-1);

	-- register data from CPU
	signal cpu_out_reg, next_cpu_out : sc_out_type;
	-- register data to CPU
	signal rd_data_reg, next_rd_data : std_logic_vector(31 downto 0);
	-- register data to memory
	signal next_mem_out : sc_out_type;
	
	signal fetch_reg, next_fetch : std_logic;
	signal crd_reg, next_crd : std_logic;
	
	signal hit_cnt, next_hit_cnt : integer := 0;
	signal miss_cnt, next_miss_cnt : integer := 0;
	signal write_cnt, next_write_cnt : integer := 0;
	signal nc_cnt, next_nc_cnt : integer := 0;

begin

	int_reset <= reset or inval;
	
	lines: for i in 0 to line_cnt-1 generate
		cache_line: block
			generic (
				index : unsigned(index_bits-1 downto 0));
			generic map (
				index => to_unsigned(i, index_bits));
			port (
				clk, reset:	in std_logic;				
				enable: 	in std_logic;
				data_in: 	in cache_line_type;
				data_out:	out cache_line_type;
				address: 	in std_logic_vector(SC_ADDR_SIZE-1 downto 0);
				hit: 		out std_logic
				);
			port map (
				clk		 => clk,
				reset	 => int_reset,
				enable	 => ena(i),
				data_in	 => data(i-1),
				data_out => data(i),
				address	 => cpu_out_reg.address,
				hit		 => hit(i));			
			signal data: cache_line_type;
		begin
			sync: process(clk, reset)
			begin
				if reset = '1' then
					data <= (std_logic_vector(index), (others => '0'), '0');
				elsif rising_edge(clk) then
					if enable = '1' then
						data <= data_in;
					end if;			
				end if;
			end process;
			async: process (data, address)
			begin
				data_out <= data;
				if data.tag = address and data.valid='1' then
					hit <= '1';
				else
					hit <= '0';
				end if;
			end process;
		end block cache_line;
	end generate lines;

	cache_ram: entity work.sdpram
		generic map (
			width	   => 32,
			addr_width => index_bits)
		port map (
			wrclk	   => clk,
			data	   => ram_data,
			wraddress  => ram_wraddress,
			wren	   => ram_wren,
			
			rdclk	   => clk,
			rdaddress  => ram_rdaddress,
			rden	   => '1',
			dout	   => ram_dout);

	sync: process (clk, int_reset)
	begin  -- process sync
		if int_reset = '1' then  -- asynchronous reset (active low)
			cpu_out_reg <= ((others => '0'), (others => '0'), '0', '0', '0', '0');
			rd_data_reg <= (others => '0');
			fetch_reg <= '0';
			crd_reg <= '0';
			state <= idle;

--			hit_cnt <= 0;
--			miss_cnt <= 0;
--			write_cnt <= 0;
--			nc_cnt <= 0;
			
		elsif clk'event and clk = '1' then  -- rising clock edge
			cpu_out_reg <= next_cpu_out;				
			rd_data_reg <= next_rd_data;
			mem_out <= next_mem_out;
			fetch_reg <= next_fetch;
			crd_reg <= next_crd;
			state <= next_state;

			hit_cnt <= next_hit_cnt;
			miss_cnt <= next_miss_cnt;
			write_cnt <= next_write_cnt;
			nc_cnt <= next_nc_cnt;
			
		end if;
	end process sync;
	
	async: process (cpu_out, cpu_out_reg, mem_in,
					ram_dout, rd_data_reg, fetch_reg, crd_reg,
					state, data, hit,
					hit_cnt, miss_cnt, write_cnt, nc_cnt)

		variable merged_index : std_logic_vector(index_bits-1 downto 0);
		variable write_hit, read_hit : std_logic;
		
	begin  -- process async

		next_hit_cnt <= hit_cnt;
		next_miss_cnt <= miss_cnt;
		next_write_cnt <= write_cnt;
		next_nc_cnt <= nc_cnt;
		
		-- gate data, depending on hit
		merged_index := (others => '0');
		for i in 0 to line_cnt-1 loop
			if hit(i) = '1' then
				merged_index := merged_index or data(i).index;
			end if;
		end loop;  -- i

		-- hit data that goes to cache line 0
		data(-1) <= (merged_index, cpu_out_reg.address, '0');
		-- set enable signals
		for i in 0 to line_cnt-1 loop
			ena(i) <= '0';
		end loop;  -- i

		-- register data from CPU
		if cpu_out.rd = '1' or cpu_out.wr = '1' then
			next_cpu_out <= cpu_out;
		else
			next_cpu_out <= cpu_out_reg;
		end if;

		-- default values to CPU
		cpu_in.rdy_cnt <= mem_in.rdy_cnt;
		if fetch_reg = '1' then
			cpu_in.rd_data <= mem_in.rd_data;
			next_rd_data <= mem_in.rd_data;
		elsif crd_reg = '1' then
			cpu_in.rd_data <= ram_dout;
			next_rd_data <= ram_dout;
		else
			cpu_in.rd_data <= rd_data_reg;
			next_rd_data <= rd_data_reg;
		end if;
		next_fetch <= '0';
		next_crd <= '0';
		
		-- default outputs to memory
		next_mem_out.rd <= '0';
		next_mem_out.wr <= '0';
		next_mem_out.wr_data <= cpu_out_reg.wr_data;
		next_mem_out.address <= cpu_out_reg.address;
		next_mem_out.atomic <= cpu_out_reg.atomic;
		next_mem_out.nc <= cpu_out_reg.nc;
		
		-- signals for ram block
		ram_data <= cpu_out_reg.wr_data;
		ram_wraddress <= merged_index;
		ram_rdaddress <= merged_index;
		ram_wren <= '0';
		
		-- we're idle unless we know better
		next_state <= state;

		case state is

			when idle => null; 			-- handled below

			-- the write sequence, updating cache
			when wr0 =>  				-- pass on data to main memory
				cpu_in.rdy_cnt <= "11";
				next_state <= wr1;

			when wr1 =>  				-- wait for memory
				cpu_in.rdy_cnt <= mem_in.rdy_cnt;
				if mem_in.rdy_cnt <= 1 then
					next_state <= idle;
				else
					next_state <= wr1;
				end if;

			-- memory read sequence, updating cache
			when rd0 =>  				-- pass on data to main memory
				cpu_in.rdy_cnt <= "11";
				next_state <= rd1;
				
			when rd1 =>  				-- wait for memory
				if mem_in.rdy_cnt <= 1 then
					cpu_in.rdy_cnt <= "10";
					next_state <= rd2;
				else
					cpu_in.rdy_cnt <= "11";
					next_state <= rd1;
				end if;

			when rd2 =>  				-- write back data to cache
				-- shift in new data
				ena <= (others => '1');

				data(-1) <= (data(line_cnt-1).index, cpu_out_reg.address, '1');				
				ram_data <= mem_in.rd_data;
				ram_wraddress <= data(line_cnt-1).index;
				ram_wren <= '1';
				
				cpu_in.rdy_cnt <= "01";
				next_fetch <= '1';
				next_state <= idle;

			-- memory read sequence, no caching
			when ncrd0 =>  				-- pass on data to main memory
				cpu_in.rdy_cnt <= "11";
				next_state <= ncrd1;

			when ncrd1 =>  				-- wait for memory
				cpu_in.rdy_cnt <= mem_in.rdy_cnt;
				if mem_in.rdy_cnt <= 1 then
					next_fetch <= '1';
					next_state <= idle;
				else
					next_state <= ncrd1;
				end if;

			when others => null;
		end case;

		-- start a new transaction
		if state = idle or state = wr1 or state = rd2 or state = ncrd1 then

			if cpu_out_reg.wr = '1' then

				next_cpu_out.wr <= cpu_out.wr;
				
				-- set enables for shifting lines
				write_hit := '0';
				for i in 0 to line_cnt-1 loop
					if hit(i) = '1' then
						write_hit := '1';
						for k in 0 to i loop
							ena(k) <= '1';
						end loop;  -- k
					end if;
				end loop;  -- i
					
				-- fixup enables
				if write_hit = '0' then
					ena <= (others => '1');
				end if;

				-- shift in new data
				if write_hit = '1' then
					data(-1) <= (merged_index, cpu_out_reg.address, '1');
					ram_data <= cpu_out_reg.wr_data;
					ram_wraddress <= merged_index;
					ram_wren <= '1';
				else
					data(-1) <= (data(line_cnt-1).index, cpu_out_reg.address, '1');
					ram_data <= cpu_out_reg.wr_data;
					ram_wraddress <= data(line_cnt-1).index;
					ram_wren <= '1';
				end if;

				next_write_cnt <= write_cnt+1;
				
				-- trigger a write
				next_mem_out.wr <= '1';
				cpu_in.rdy_cnt <= "11";
				next_state <= wr0;

			elsif cpu_out_reg.rd = '1' then

				next_cpu_out.rd <= cpu_out.rd;

				-- check for hits
				read_hit := '0';
				for i in 0 to line_cnt-1 loop
					if hit(i) = '1' then
						read_hit := '1';
						for k in 0 to i loop
							ena(k) <= '1';
						end loop;  -- k
					end if;
				end loop;  -- i

				if read_hit = '1' then

					next_hit_cnt <= hit_cnt+1;
						
					-- shift in new data
					data(-1) <= (merged_index, cpu_out_reg.address, '1');
					ram_rdaddress <= merged_index;
					-- read from cache
					cpu_in.rdy_cnt <= "11";
					next_crd <= '1';
					next_state <= idle;

				else

					next_miss_cnt <= miss_cnt+1;
						
					-- trigger a read
					next_mem_out.rd <= '1';
					cpu_in.rdy_cnt <= "11";
					next_state <= rd0;
				end if;
					
			end if;
		end if;

		-- uncached transactions go straight to the memory
		if state = idle or state = wr1 or state = rd2 or state = ncrd1 then

			if cpu_out.rd = '1' and cpu_out.nc = '1' then

				next_nc_cnt <= nc_cnt+1;
				
				next_cpu_out.rd <= '0';
				-- trigger a read
				next_mem_out <= cpu_out;
				next_state <= ncrd0;
				
			elsif cpu_out.wr = '1' and cpu_out.nc = '1' then

				next_nc_cnt <= nc_cnt+1;

				next_cpu_out.wr <= '0';
				-- trigger a write
				next_mem_out <= cpu_out;
				next_state <= wr0;
			end if;

		end if;
				
	end process async;
	
end rtl;
