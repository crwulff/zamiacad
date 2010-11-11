library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_misc.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
use ieee.std_logic_textio.all;
use std.textio.all;
use work.mlite_pack.all;

entity mlite2sram is
   port(clk             : in std_logic;
        -- communication pins with mlite cpu
        mem_byte_sel    : in std_logic_vector(3 downto 0);
        mem_write       : in std_logic;
        mem_address     : in std_logic_vector(31 downto 0);
        mem_data_w      : in std_logic_vector(31 downto 0);
        mem_data_r      : out std_logic_vector(31 downto 0);
	mem_pause       : out std_logic;
	-- communication pins with SRAM on xsv300 board
	sram_we_hi      : out std_logic;
	sram_we_lo      : out std_logic;
	sram_ce_hi      : out std_logic;
	sram_ce_lo      : out std_logic;
	sram_oe_hi      : out std_logic;
	sram_oe_lo      : out std_logic;
	sram_address_hi : out std_logic_vector(18 downto 0);
	sram_address_lo : out std_logic_vector(18 downto 0);
	sram_data_hi    : inout std_logic_vector(15 downto 0);
	sram_data_lo    : inout std_logic_vector(15 downto 0));
end; --entity ram

architecture logic of mlite2sram is

   signal data   : std_logic_vector (31 downto 0); -- prefeched data from sram
   signal output : std_logic_vector (31 downto 0); -- data sent to memory bus

   type STATE_TYPE is (
      READ,
      WRITE
   );

   signal state, next_state : STATE_TYPE;

begin

   set_state: process(clk) --, state, next_state)
   begin
      if clk'event and clk = '1' then
         state <= next_state;
      end if;
   end process;

   work: process(state, mem_byte_sel, mem_write, mem_address, mem_data_w,
                 sram_data_lo, sram_data_hi, data)
   begin
      -- set defaults
      sram_address_hi <= mem_address(18 downto 2) & "00"; -- ram is accessed by word only
      sram_address_lo <= mem_address(18 downto 2) & "00"; -- hence 0002 would be illegal
      sram_ce_hi      <= '0'; -- chip enable can be set at all time (ce is active at 0)
      sram_ce_lo      <= '0';
      sram_we_hi      <= '1'; -- don't write
      sram_we_lo      <= '1';
      sram_oe_hi      <= '1'; -- don't read either
      sram_oe_lo      <= '1';
      sram_data_hi    <= "ZZZZZZZZZZZZZZZZ"; -- don't write anything to sram
      sram_data_lo    <= "ZZZZZZZZZZZZZZZZ";
      mem_pause       <= '0'; -- don't pause cpu
      next_state      <= READ;
      output          <= "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ";

      -- check if this component is supposed to be active
      if mem_address (31 downto 19) = "0000000000000" then

         case state is
            -- always start reading
	    when READ =>
	       -- enable sram output
	       sram_oe_hi <= '0';
	       sram_oe_lo <= '0';
	       
	       -- check if boot memory is read
	       case mem_address(31 downto 2) is

                  when "000000000000000000000000000000" => output <= x"00004021"; -- main: move    t0,zero
                  when "000000000000000000000000000001" => output <= x"00004821"; --       move    t1,zero
                  when "000000000000000000000000000010" => output <= x"25290001"; --       addiu   t1,t1,1
                  when "000000000000000000000000000011" => output <= x"3c010000"; -- loop: lui     at,0x0
                  when "000000000000000000000000000100" => output <= x"ac280024"; --       sw      t0,36(at)
                  when "000000000000000000000000000101" => output <= x"01095020"; --       add     t2,t0,t1
                  when "000000000000000000000000000110" => output <= x"01204021"; --       move    t0,t1
                  when "000000000000000000000000000111" => output <= x"08000003"; --       j       c <loop>
                  when "000000000000000000000000001000" => output <= x"01404821"; --       move    t1,t2
                  when "000000000000000000000000001001" => output <= x"00000000"; --       nop

                  -- address not part of boot rom so give back sram content
                  when others => 
                     output <= sram_data_hi & sram_data_lo;
               end case;
	       
               -- recognize write cycle
               if mem_write = '1' then
		 mem_pause  <= '1';
		 next_state <= WRITE;
	       end if;

	       data <= sram_data_hi & sram_data_lo;

	    when WRITE =>
	       -- write correct data into memory
	       sram_we_hi <= mem_byte_sel(3) nor mem_byte_sel(2);
	       sram_we_lo <= mem_byte_sel(1) nor mem_byte_sel(0);

               if mem_byte_sel(3) = '1' then
	          sram_data_hi(15 downto 8) <= mem_data_w(31 downto 24);
	       else
	          sram_data_hi(15 downto 8) <= data(31 downto 24);
	       end if;

               if mem_byte_sel(2) = '1' then
                  sram_data_hi(7 downto 0) <= mem_data_w(23 downto 16);
               else
                  sram_data_hi(7 downto 0) <= data(23 downto 16);
               end if;

               if mem_byte_sel(1) = '1' then
                  sram_data_lo(15 downto 8) <= mem_data_w(15 downto 8);
               else
                  sram_data_lo(15 downto 8) <= data(15 downto 8);
               end if;

               if mem_byte_sel(0) = '1' then
                 sram_data_lo(7 downto 0) <= mem_data_w(7 downto 0);
               else
                  sram_data_lo(7 downto 0) <= data(7 downto 0);
               end if;

            when others => NULL;
	 end case;
      end if;
   end process; -- work

   mem_data_r   <= output;
end;


