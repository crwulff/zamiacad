-------------------------------------------------------------------------------
-- Title      : Serial port with 16 bytes input & out buffer
-- Project    : MANIK-II
-------------------------------------------------------------------------------
-- File       : serial.vhd
-- Author     : Sandeep Dutta 
-- Company    : NikTech
-- Last update: 2006-08-07
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Serial port with 16 bytes input & output buffer. The divisor
--              specified will be used to generate baud X 16 clock from the
--              system clock.
--
--              CLK_DIVISOR = ((CLK in MHZ)*1000*1000)/(baud * 2 * 16)
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2004/08/13  1.0      Sandeep	Created
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-- Description: RS232 - Receive unit with 16 byte input buffer
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2004/08/02  1.0      Sandeep	Created
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity rxunit is
    port (rxpin        : in  std_logic;
          baudx16_clk  : in  std_logic;
          rxread       : in  std_logic := '0';
          rclk	       : in  std_logic;
          reset_i      : in  std_logic;
          r_frame_err  : out std_logic;
          r_overr_err  : out std_logic;
          r_data_avail : out std_logic;
          r_full       : out std_logic;          
          r_data       : out std_logic_vector (7 downto 0));
end rxunit;

architecture Behavioral of rxunit is
    
    signal in_fmiddle, frame_error, stop_bit : std_logic := '0';
    signal shift_in, clk_in, data_avail      : std_logic := '0';
    
    signal read_done, read_done_prev   : std_logic := '0';
    signal fempty      : std_logic := '0';
    signal ffull       : std_logic := '0';
    
    signal start_detected : std_logic := '0';
    signal recv_active    : std_logic := '0';
    signal rx_sync 	  : std_logic_vector(1 downto 0) := "00";
    
    signal clk_count : std_logic_vector(15 downto 0) := (others => '0');
    signal bit_count : std_logic_vector(8 downto 0)  := (others => '0');
    signal fmiddle   : std_logic_vector(7 downto 0)  := (others => '0');
    signal data_reg  : std_logic_vector(7 downto 0)  := (others => '0');

    type rx_states is (rx_idle, rx_start, rx_bits, rx_sin, rx_stop);
    signal recv_state, recv_nstate : rx_states := rx_idle;

    signal bcnt_reg, bcnt_x : std_logic_vector(3 downto 0) := x"0";
    signal ccnt_reg, ccnt_x : std_logic_vector(7 downto 0) := x"00";
    signal bcnt_inc, bcnt_rst  : std_logic := '0';
    signal ccnt_inc, ccnt_rst  : std_logic := '0';
    signal ccnt_int, bcnt_int : integer := 0;

    signal data_rst, data_sin   : std_logic := '0';
    signal frame_err, overr_err : std_logic := '0';
    
begin  -- Behavioral
   
    process (baudx16_clk)
    begin
        if rising_edge(baudx16_clk) then
            rx_sync(0) <= rxpin;
            rx_sync(1) <= rx_sync(0);
        end if;
    end process;

    -- the receive statemachine
    process (baudx16_clk, reset_i)        
    begin
        if reset_i = '1' then
            recv_state <= rx_idle;
            ccnt_reg   <= x"00";
            bcnt_reg   <= x"0";
            data_reg   <= x"00";
        elsif rising_edge(baudx16_clk) then
            recv_state <= recv_nstate;
            
            -- clock counter
            if ccnt_rst = '1' then
                ccnt_reg <= x"00";
            elsif ccnt_inc = '1' then
                ccnt_reg <= ccnt_reg + 1;
            end if;

            -- bit counter
            if bcnt_rst = '1' then
                bcnt_reg <= x"0";
            elsif bcnt_inc = '1' then
                bcnt_reg <= bcnt_reg + 1;
            end if;

            -- data reg
            if data_rst = '1' then
                data_reg <= x"00";
            elsif data_sin = '1' then
                data_reg <= rx_sync(0) & data_reg(7 downto 1);
            end if;
        end if;
    end process ;

    bcnt_int <= conv_integer(bcnt_reg);
    ccnt_int <= conv_integer(ccnt_reg);
    
    process (recv_state, ccnt_reg, bcnt_reg, rx_sync, ccnt_int, bcnt_int, data_avail)
    begin

        recv_nstate <= recv_state;
        ccnt_x      <= ccnt_reg;
        bcnt_x      <= bcnt_reg;
        bcnt_rst    <= '0';
        ccnt_rst    <= '0';
        bcnt_inc    <= '0';
        ccnt_inc    <= '0';
        data_rst    <= '0';
        data_sin    <= '0';
        read_done   <= '0';
        stop_bit    <= '0';
        frame_err   <= '0';
        overr_err   <= '0';
        
        case recv_state is
            when rx_idle =>
                -- wait for start bit
                if rx_sync(1) = '1' and rx_sync(0) = '0' then  -- falling edge
                    ccnt_inc    <= '1';
                    recv_nstate <= rx_start;
                    if data_avail = '1' then  -- previous data not read
                        overr_err <= '1';
                    end if;
                else
                    bcnt_rst <= '1';
                    ccnt_rst <= '1';
                    data_rst <= '1';
                    recv_nstate <= rx_idle;
                end if;

            when rx_start =>
                -- wait for 8 clock counts
                if ccnt_int = 8 then
                    ccnt_rst    <= '1';
                    recv_nstate <= rx_bits;
                elsif rx_sync(0) = '1' then
                    ccnt_rst    <= '1';
                    recv_nstate <= rx_idle;
                else
                    ccnt_inc <= '1';
                    bcnt_rst <= '1';
                    recv_nstate <= rx_start;
                end if;

            when rx_bits =>
                -- shift in every 16 clocks, for 8 bits
                if bcnt_int = 8 then
                    ccnt_rst    <= '1';
                    recv_nstate <= rx_stop;  -- 8 bits done goto stop
                elsif ccnt_int = 14 then
                    ccnt_rst    <= '1';
                    data_sin    <= '1';
                    recv_nstate <= rx_sin;
                else
                    ccnt_inc <= '1';
                    recv_nstate <= rx_bits;
                end if;

            when rx_sin =>
                bcnt_inc    <= '1';      -- increment bit count
                recv_nstate <= rx_bits;  -- next bit

            when rx_stop =>
                ccnt_inc <= '1';
                if ccnt_int = 15 then   -- middle of stop bit
                    if rx_sync(0) /= '1' then
                        frame_err <= '1';
                    end if;
                    recv_nstate <= rx_idle;
                    read_done   <= '1';
                else
                    recv_nstate <= rx_stop;
                end if;
            when others => null;
        end case;
    end process;

    -- data_avail goes high when read_done
    -- transitions from 0 to 1 (rising_edge)
    -- reset when read complete
    process (rclk, reset_i)
    begin
        if reset_i = '1' then
           data_avail <= '0';
		   read_done_prev <= '0';
           r_data     <= (others => '0');
        elsif rising_edge(rclk) then
            read_done_prev <= read_done ;
            if read_done = '1' and read_done_prev = '0' then
                data_avail <= '1';
                r_data     <= data_reg;
            elsif rxread = '1' then
                data_avail <= '0';
            end if;            
        end if;
    end process;

    r_data_avail <= data_avail;
    r_full       <= data_avail;
    r_frame_err  <= frame_err;
    r_overr_err  <= overr_err; --data_avail and start_detected;
end Behavioral;
-------------------------------------------------------------------------------
-- Description: Output character out thru Serial port.
--      inputs - DATA - 8bit data to be sent
--               LOAD - 1 when DATA is Valid
--               BAUDX16_CLK - 16X BaudRate clock
--
--      outputs- TXBUSY - transmit unit is busy.
--               TXPIN  - should be wired to the TX pin of the serial port
-------------------------------------------------------------------------------
-- Copyright (c) 2003 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-12-04  1.0      sandeep	Created
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- - div16 - divide a clock by 16
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library UNISIM;
use UNISIM.vcomponents.all;

use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;

entity div16 is
    generic (XILINX_virtex : boolean := FALSE);
    port (iclk : in  std_logic;
          oclk : out std_logic);

end div16;

architecture Behavioral of div16 is
    signal vccc     : std_logic;
    signal bbclk    : std_logic := '0';
    
begin
    oclk <= bbclk;

    -- SRL16 Implementation
    div16x: if Technology = "XILINX" generate
        attribute INIT of srl16_inst : label is "0001";
        signal vccc     : std_logic;         
    begin	 
        vcc_inst : VCC port map (p => vccc);
        srl16_inst: SRL16
            generic map (INIT => X"0001")
            port map (D  => bbclk, Q  => bbclk,
                      A0 => vccc,  A1 => vccc,
                      A2 => vccc,  A3 => vccc, CLK => iclk);
    end generate div16x;

    -- Behavioral implementation
    div16b : if Technology /= "XILINX" generate
        signal divisor : std_logic_vector (3 downto 0) := (others => '0');
        signal pulse   : std_logic;
    begin
        pulse <= '1' when divisor = "0000" else '0';
        process (iclk)
        begin  -- process            
            if iclk'event and iclk = '1' then  -- rising clock edge
                divisor <= divisor + 1;
                if pulse = '1' then
                    bbclk <= not bbclk;
                elsif bbclk = '1' then
                    bbclk <= '0';
                end if;
            end if;
        end process;
    end generate div16b;        
end Behavioral;

-------------------------------------------------------------------------------
-- txunit - implementation of the UART transmit unit
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity txunit is
    generic (XILINX_virtex : boolean := TRUE);
    Port (baudx16_clk : in  std_logic;
          wclk        : in  std_logic;
          txwrite     : in  std_logic;
          t_full      : out std_logic;
          t_empty     : out std_logic;
          TxPin       : out std_logic;
          t_data      : in  std_logic_vector(7 downto 0);
          reset_i     : in  std_logic);
end TxUnit;

architecture Behavioral of TxUnit is

    signal shiftReg : std_logic_vector (7 downto 0) := "00000000";
    signal state    : std_logic                     := '0';
    signal sv       : std_logic                     := '0';
    signal srle     : std_logic_vector (8 downto 0) := "000000000";
    signal bbclk    : std_logic                     := '0';
    signal TxPinL   : std_logic                     := '0';

    component div16
        generic (XILINX_virtex : boolean);
        port (iclk : in  std_logic;
              oclk : out std_logic);
    end component;

    signal state_prev : std_logic := '0';
    signal send_next  : std_logic := '0';
    signal data_out   : std_logic_vector(7 downto 0) := (others => '0');
begin  -- Behavioral

    TxPin     <= not TxPinL;

    div16_inst : div16
        generic map (XILINX_virtex => True)
        port map (iclk => baudx16_clk, oclk => bbclk);
    
    txprocess: process (bbclk, reset_i)
    begin  -- process txprocess
        if reset_i = '1' then
            TxPinL   <= '0';
            shiftReg <= (others => '0');
            state    <= '0';
            sv       <= '0';
        elsif rising_edge(bbclk) then  -- rising clock edge
            case state is
                when '0' =>
                    TxPinL <= send_next after 1 ns;
                    state  <= send_next after 1 ns;
                    if send_next = '1' then
                        shiftReg <= data_out after 1 ns;
                    end if;
                    sv     <= '1' after 1 ns;

                when '1' =>
                    TxPinL   <= not shiftReg (0) after 1 ns;
                    shiftReg <= '1' & shiftReg (7 downto 1) after 1 ns;
                    state    <= not srle(8) after 1 ns;
                    sv       <= '0' after 1 ns;
                when others => null;
            end case;
        end if;
    end process txprocess;

    srle_proc : process (bbclk, reset_i)
    begin
        if reset_i = '1' then
            srle <= (others => '0');
        elsif rising_edge(bbclk) then
            if state = '1' then
                srle <= srle (7 downto 0) & sv after 1 ns;
            end if;            
        end if;
    end process srle_proc;

    process (wclk, reset_i)
    begin
        if reset_i = '1' then
            data_out   <= (others => '0');
            send_next  <= '0';
            state_prev <= '0';
        elsif rising_edge(wclk) then
            -- latch incoming data when strobed
            if txwrite = '1' then
                 data_out  <= t_data ;
            end if;
            state_prev <= state ;
            
            -- send_next is reset when state transitions
            -- from 1 to 0 (falling edge). set with write strobe
            if state_prev = '1' and state = '0' then
                send_next <= '0';
            elsif txwrite = '1' then
                send_next <= '1';
            end if;
      end if;
    end process ;

    t_full  <= send_next;
    t_empty <= not send_next ;
    
end Behavioral;

-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

use UNISIM.vcomponents.all;
library UNISIM;

use work.manikconfig.all;
use work.manikpackage.all;

entity serial is
    
    generic (WIDTH         : integer := 32;
             BAUD_RATE     : integer := 115200;
             CORE_FREQ_MHZ : integer := 50);

    port (clk   : std_logic;
          reset : std_logic;
          
          -- Wishbone slave interface
          WBS_ADR_I : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
          WBS_SEL_I : in  std_logic_vector (3 downto 0);
          WBS_DAT_I : in  std_logic_vector (WIDTH-1 downto 0);
          WBS_WE_I  : in  std_logic;
          WBS_STB_I : in  std_logic;
          WBS_CYC_I : in  std_logic;
          WBS_CTI_I : in  std_logic_vector (2 downto 0);
          WBS_BTE_I : in  std_logic_vector (1 downto 0);
          WBS_DAT_O : out std_logic_vector (WIDTH-1 downto 0);
          WBS_ACK_O : out std_logic;
          WBS_ERR_O : out std_logic;
        
          serial_intr    : out std_logic;

          -- Serial port Interface
          txpin          : out std_logic;
          rxpin          : in  std_logic);
end serial;

architecture Behavioral of serial is

    component txunit
        generic (XILINX_virtex : boolean);
        port (baudx16_clk : in  std_logic; wclk   : in  std_logic;
              txwrite     : in  std_logic; t_full : out std_logic;
              t_empty     : out std_logic; TxPin  : out std_logic;
              t_data      : in  std_logic_vector(7 downto 0);
              reset_i     : in  std_logic);
    end component;

    component rxunit
        port (rxpin        : in  std_logic;
              baudx16_clk  : in  std_logic;
              rxread       : in  std_logic;
              rclk         : in  std_logic;
              reset_i      : in  std_logic;
              r_frame_err  : out std_logic;
              r_overr_err  : out std_logic;
              r_data_avail : out std_logic;
              r_full       : out std_logic;
              r_data       : out std_logic_vector (7 downto 0));
    end component;
    
    signal baudx16_clk : std_logic := '0';
    signal divreload   : std_logic;
    signal divcount    : std_logic_vector(WIDTH-1 downto 0) := (others => '0');
    signal divisor     : std_logic_vector(WIDTH-1 downto 0) := (others => '0');

    signal serial_en    : std_logic;
    signal r_full       : std_logic;
    signal t_full       : std_logic;
    signal t_empty      : std_logic;
    signal txwrite      : std_logic;
    signal r_data_avail : std_logic;
    signal Npolled_ce   : std_logic;
    signal data_out     : std_logic_vector(WIDTH-1 downto 0);
    signal r_data       : std_logic_vector(7 downto 0);
    signal r_frame_err  : std_logic;
    signal r_overr_err  : std_logic;

    signal i_data     : std_logic_vector(7 downto 0) := (others => '0');
    signal r_data_reg : std_logic_vector(7 downto 0) := (others => '0');
    signal rxread     : std_logic                    := '0';
    signal Npolled    : std_logic                    := '0';
    signal frame_err  : std_logic                    := '0';
    signal overr_err  : std_logic                    := '0';
    
    signal serial_state : std_logic_vector(5 downto 0) := (others => '0');

    constant serial_idle  : std_logic_vector(5 downto 0) := "000000";
    constant serial_rdata : std_logic_vector(5 downto 0) := "000001";
    constant serial_wdata : std_logic_vector(5 downto 0) := "000010";
    constant serial_done  : std_logic_vector(5 downto 0) := "000100";
    constant serial_wstat : std_logic_vector(5 downto 0) := "010000";
    constant serial_wdiv  : std_logic_vector(5 downto 0) := "100000";
    
    constant real_div    : real    := (real(CORE_FREQ_MHZ*1000*1000)/real(BAUD_RATE*16))+0.5;
    constant clk_divisor : integer := integer(real_div);

    constant ACC_WIDTH : integer := 31;
    -- ((Baud<<(BaudGeneratorAccWidth-4))+(ClkFrequency>>5))/(ClkFrequency>>4);
    constant baud_scale  : real := (real(BAUD_RATE*16)*real(2**(ACC_WIDTH-4)));
    constant clk_rsh_5   : integer := (CORE_FREQ_MHZ*1000000/(2**5));
    constant clk_rsh_4   : integer := (CORE_FREQ_MHZ*1000000/(2**4));
    constant gen_inc_val : integer := integer((real(baud_scale)+real(clk_rsh_5))/real(clk_rsh_4));
    
    signal baud_accum : std_logic_vector (ACC_WIDTH downto 0) := (others => '0');
    signal baud_inc   : std_logic_vector (ACC_WIDTH downto 0) := (others => '0');
begin  -- Behavioral

    serial_en <= WBS_STB_I;
    divreload <= serial_state(5);
    

    process (clk, reset)
    begin
        if reset = '1' then
            baud_inc <= conv_std_logic_vector(gen_inc_val,ACC_WIDTH+1);
            baud_accum <= (others => '0');            
        elsif rising_edge(clk) then
            if baud_inc = conv_std_logic_vector(0,WIDTH+1) then
                baud_inc <= conv_std_logic_vector(gen_inc_val,ACC_WIDTH+1);
            elsif divreload = '1' then
                baud_inc <=  WBS_DAT_I(ACC_WIDTH downto 0);
            end if;

            baud_accum <= ('0' & baud_accum(ACC_WIDTH-1 downto 0)) + baud_inc;
        end if;
    end process ;
    baudx16_clk <= baud_accum(ACC_WIDTH);
    
    --
    -- data_out (WIDTH-1 downto WIDTH-8) contains rx data
    --	   	(WIDTH-10)		 r_frame_err
    --		(WIDTH-11)		 r_overr_err
    -- 	        (WIDTH-12)               Npolled flag
    --          (WIDTH-13)               data available
    --          (WIDTH-14)               receive  buffer full
    --          (WIDTH-15)               transmit buffer full
    --          (WIDTH-16)		 transmit buffer empty
    
    data_out (WIDTH-1 downto WIDTH-8)  <= r_data_reg;
    data_out (WIDTH-9 downto WIDTH-9) <= (others => '0');
    data_out (WIDTH-10)                <= frame_err;
    data_out (WIDTH-11)                <= overr_err;
    data_out (WIDTH-12)                <= Npolled;
    data_out (WIDTH-13)                <= r_data_avail;
    data_out (WIDTH-14)                <= r_full;
    data_out (WIDTH-15)                <= t_full;
    data_out (WIDTH-16)		       <= t_empty;
    data_out (WIDTH-17 downto 0)       <= (others => '0');

    --
    -- Npolled mode flag
    --
    Npolled_ce <= serial_state(4);    
    Npolled_proc : process (clk, reset)
    begin
        if reset = '1' then
            Npolled <= '0' after 1 ns;
        elsif rising_edge(clk) then
            if Npolled_ce = '1' then
                Npolled <= i_data(4) after 1 ns;
            end if;
        end if;
    end process Npolled_proc;
    
    WBS_DAT_O <= data_out;
    --
    -- latch in input data for a write request
    --
    idata_proc : process (clk, reset)
    begin
        if reset = '1' then
            i_data <= (others => '0');
        elsif rising_edge(clk) then
            if serial_en = '1' and WBS_WE_I = '1' then
                i_data <= WBS_DAT_I(7 downto 0) after 1 ns;
            end if;
        end if;
    end process idata_proc;

    --
    -- latch in data to be read
    --
    rdata_proc : process (clk, reset)
    begin
        if reset = '1' then
            r_data_reg <= (others => '0');
        elsif rising_edge(clk) then
            if rxread = '1' then
                r_data_reg <= r_data after 1 ns;
            end if;
        end if;
    end process rdata_proc;

    --
    -- error flags : reset when read/write completes
    --
    process (clk, reset)
    begin
        if reset = '1' then
            frame_err <= '0';
            overr_err <= '0';
        elsif rising_edge(clk) then
			if serial_state(2) = '1' then
    	        frame_err <= '0';
        	    overr_err <= '0';
        	else
            	frame_err <= r_frame_err;
            	overr_err <= r_overr_err;
        	end if;
	   end if;
    end process;
        
    rxread      <= serial_state(0) and r_data_avail;
    txwrite     <= serial_state(1) and not t_full;
    serial_intr <= r_data_avail and not Npolled;
    WBS_ACK_O   <= serial_state(2); -- or serial_state(3);
    WBS_ERR_O	<= '0';	
    --
    -- serial state machine generates signals for transmit
    -- receive control and for read/write ack on the system
    -- interface
    --
    serial_smach_proc : process (clk, reset)                
    begin
        if reset = '1' then
            serial_state <= serial_idle ;
        elsif rising_edge(clk) then
            case serial_state is

                when serial_idle =>
                    if serial_en = '1' then
                        -- read request
                        if WBS_WE_I = '0' then
                            -- read data from receive buffer
                            if WBS_SEL_I = "1000" then
                                serial_state <= serial_rdata after 1 ns;
                            -- assume read status request
                            else
                                serial_state <= serial_done after 1 ns;
                            end if;
                        -- write request
                        elsif WBS_WE_I = '1' then
                            -- load divisor
                            if WBS_ADR_I(2) = '1' then
                                serial_state <= serial_wdiv  after 1 ns;
                            -- write data request
                            elsif WBS_SEL_I = "1000" then
                                serial_state <= serial_wdata after 1 ns;                            
                            elsif WBS_SEL_I ="0100" then
                            -- write control information
                                serial_state <= serial_wstat after 1 ns;
                            else
                                serial_state <= serial_done after 1 ns;
                            end if;
                        end if;
                    end if;

                when serial_rdata =>
                    -- wait for data to arrive
                    if r_data_avail = '1' then
                        serial_state <= serial_done after 1 ns;                        
                    end if;

                when serial_wdata =>
                    -- wait for transmit buffer to have space
                    if t_full = '0' then
                        serial_state <= serial_done after 1 ns;                        
                    end if;
                    
                when serial_done =>
                    serial_state <= serial_idle after 1 ns;

                when serial_wstat =>
                    serial_state <= serial_done after 1 ns;
                    
                when serial_wdiv =>
                    serial_state <= serial_done after 1 ns;
                    
                when others => null;
            end case;
        end if;
    end process serial_smach_proc;

    tx_inst: txunit
        generic map (XILINX_virtex => False)
        port map (baudx16_clk => baudx16_clk,
                  wclk        => clk,
                  txwrite     => txwrite,
                  t_full      => t_full,
                  t_empty     => t_empty,
                  TxPin       => txpin,
                  t_data      => i_data,
                  reset_i     => reset);
    rx_inst: rxunit
        port map (rxpin        => rxpin,
                  baudx16_clk  => baudx16_clk,
                  rxread       => rxread,
                  rclk         => clk,
                  reset_i      => reset,
                  r_frame_err  => r_frame_err,
                  r_overr_err  => r_overr_err,
                  r_data_avail => r_data_avail,
                  r_full       => r_full,
                  r_data       => r_data);
end Behavioral;
