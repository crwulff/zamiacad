library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_misc.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
use ieee.std_logic_textio.all;
use std.textio.all;
use work.mlite_pack.all;

entity mlite2uart is
   port(clk             : in std_logic;
        reset           : in std_logic;
        -- communication pins with mlite cpu
        mem_byte_sel    : in std_logic_vector(3 downto 0);
        mem_write       : in std_logic;
        mem_address     : in std_logic_vector(31 downto 0);
        mem_data_w      : in std_logic_vector(31 downto 0);
        mem_data_r      : out std_logic_vector(31 downto 0);
	mem_pause       : out std_logic;
	-- communication with board
        TXD             : out std_logic;
	RTS             : out std_logic;
	CTS             : in std_logic;
	RXD             : in std_logic;
	-- some debug output
	dbg		: out std_logic_vector(7 downto 0));
end; --entity ram

architecture logic of mlite2uart is
   
   signal txdata  : std_logic_vector (7 downto 0);  -- transmission data
   signal te      : std_logic;                      -- transmitter enable
   signal txdone  : std_logic;                      -- data transfer finished
   signal txnd    : std_logic;                      -- transmitter new data (=enable input)
   signal rdata   : std_logic_vector (7 downto 0);  -- received date
   signal rdready : std_logic;                      -- read data ready
   signal rdack   : std_logic;                      -- read acknowledge
   signal void    : std_logic_vector (7 downto 0);  -- debug output
   signal data    : std_logic_vector (31 downto 0); -- data which is send to mem_data_r

   type STATE_TYPE is (
      IDLE,
      TRANS1,
      TRANS2,
      TRANS3,
      WAIT_DATA,
      RECEIVE,
      STATUS
   );

   signal state, next_state : STATE_TYPE;
   
begin

   -- include the hapra uart
   uart_module : uart
   PORT MAP (clk        => clk,
             reset      => reset,
	     -- connections to board
	     uarto_rtsn => RTS,
	     uarto_txd  => TXD,
	     uarti_ctsn => CTS,
	     uarti_rxd  => RXD,
	     -- connections for controlling the uart
	     txdata     => txdata,
	     te         => te,
	     txdone     => txdone,
	     txnd       => txnd,
	     rdata      => rdata,
	     rdready    => rdready,
	     rdack      => rdack,
	     dbg        => void);

   -- change state
   set_state: process (clk, reset, next_state)
   begin
      if reset = '1' then
         state <= IDLE;
      elsif clk'event and clk='1' then
         state <= next_state;
      end if;
   end process set_state;

   -- detect next state
   set_next_state: process (state, mem_write, mem_address, txdone, rdready)
   begin
      case state is
         -- wait for start of new operation
         when IDLE =>
	    -- recognize send request
	    if mem_address = "00000000000010000000000000000000" and mem_write = '1' then
	       next_state <= TRANS1;
	    -- recognize read request
	    elsif mem_address = "00000000000010000000000000000100" and mem_write = '0' then
	       next_state <= WAIT_DATA;
	    -- recognize status request
	    elsif mem_address = "00000000000010000000000000001000" and mem_write = '0' then
	       next_state <= STATUS;
	    -- otherwise stay idle
	    else
	       next_state <= IDLE;
	    end if;

	 -- copy data to UART
	 when TRANS1 =>
	    next_state <= TRANS2;

	 -- transmit data
	 when TRANS2 =>
	    if txdone = '1' then
	       next_state <= TRANS3;
	    else
	       next_state <= TRANS2;
	    end if;

         -- finish write cycle
	 when TRANS3 =>
	    next_state <= IDLE;

	 -- wait until data was received by UART
	 when WAIT_DATA =>
	    if rdready = '1' then
	       next_state <= RECEIVE;
	    else
	       next_state <= WAIT_DATA;
	    end if;

	 -- copy data from UART to memory bus
	 when RECEIVE =>
	    next_state <= IDLE;

	 when STATUS =>
	    next_state <= IDLE;

	 -- default state is IDLE
	 when others =>
	    next_state <= IDLE;
      end case;
   end process set_next_state;
	
   -- drive outputs according to state and inputs
   set_output: process (state, mem_write, mem_address, mem_data_w, txdone, rdready, rdata)
   begin
      -- set default outputs
      mem_pause  <= '0';
      txdata     <= "--------";
      txnd       <= '0';
      data       <= "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ";
      te         <= '0';
      rdack      <= '0';
   
      case state is
         when IDLE =>
	    -- pause CPU if any request is detected
	    if (mem_address = "00000000000010000000000000000000" and mem_write = '1') or
	       (mem_address = "00000000000010000000000000000100" and mem_write = '0') or
	       (mem_address = "00000000000010000000000000001000" and mem_write = '0') then
	       mem_pause  <= '1';
	    else
	       mem_pause <= '0'; 
	    end if;
         
	 -- copy data to uart
	 when TRANS1 =>
	    mem_pause  <= '1';
	    txdata     <= mem_data_w(7 downto 0);
	    txnd       <= '1';
      
         -- enable transmitter
         when TRANS2 =>
	    mem_pause  <= '1';
	    te         <= '1';

	 -- unpause CPU
	 when TRANS3 =>
	    NULL;

	 -- pause CPU (until data is received)
	 when WAIT_DATA =>
	    mem_pause  <= '1';

	 -- copy data to memory bus and unpause cpu
	 when RECEIVE =>
	    rdack      <= '1';
	    dbg        <= rdata;
	    data       <= rdata & rdata & rdata & rdata;

	 -- set flag if new data is available
	 when STATUS =>
	    data       <= "0000000000000000000000000000000" & rdready;
	    
	 when others =>
	    NULL;
      end case;
   end process set_output;

   mem_data_r <= data;
   
end logic;
