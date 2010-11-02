-------------------------------------------------------------------------------
-- Title      : Uart Receiver model
-- Project    : MANIK-II
-------------------------------------------------------------------------------
-- File       : uart_rx.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Last update: 2006-10-02
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Receives characters & displays them on  stdout
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2004/08/02  1.0      Sandeep	Created
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity rxunit_sim is
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
end rxunit_sim;

architecture Behavioral of rxunit_sim is
    
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
-------------------------------------------------------------------------------
library std;
use std.textio.all;
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use work.manikpackage.all;

entity uart_rx is
    
    generic (BAUD_RATE    : integer := 115200;
             CORE_CLK_MHZ : integer := 50;
             WIDTH        : integer := 32);

    port (clk   : in std_logic;
          reset : in std_logic;
          rxpin : in std_logic);
end uart_rx;

architecture model of uart_rx is

    signal baudx16_clk  : std_logic := '0';
    signal rxread       : std_logic := '0';
    signal rclk         : std_logic;
    signal reset_i      : std_logic;
    signal r_data_avail : std_logic;
    signal r_data       : std_logic_vector (7 downto 0);
    signal recv_data    : character := ' ';

    component rxunit_sim
        port (rxpin        : in  std_logic;
              baudx16_clk  : in  std_logic;
              rxread       : in  std_logic := '0';
              rclk         : in  std_logic;
              reset_i      : in  std_logic;
              r_data_avail : out std_logic;
              r_full       : out std_logic;
              r_data       : out std_logic_vector (7 downto 0));
    end component;

    constant real_div    : real    := (real(CORE_CLK_MHZ*1000*1000)/real(BAUD_RATE*16))+0.5;
    constant clk_divisor : integer := integer(real_div);

    signal divcount    : std_logic_vector(WIDTH-1 downto 0) := (others => '0');
    signal divisor     : std_logic_vector(WIDTH-1 downto 0) := (others => '0');

    constant ACC_WIDTH : integer := 31;
    -- ((Baud<<(BaudGeneratorAccWidth-4))+(ClkFrequency>>5))/(ClkFrequency>>4);
    constant baud_scale  : real := (real(BAUD_RATE*16)*real(2**(ACC_WIDTH-4)));
    constant clk_rsh_5   : integer := (CORE_CLK_MHZ*1000000/(2**5));
    constant clk_rsh_4   : integer := (CORE_CLK_MHZ*1000000/(2**4));
    constant gen_inc_val : integer := integer((real(baud_scale)+real(clk_rsh_5))/real(clk_rsh_4));

    signal baud_accum : std_logic_vector (ACC_WIDTH downto 0) := (others => '0');
    signal baud_inc   : std_logic_vector (ACC_WIDTH downto 0) := (others => '0');
begin  -- model

    process (clk, reset)
    begin
        if reset = '1' then
            baud_inc <= conv_std_logic_vector(gen_inc_val,ACC_WIDTH+1);
            baud_accum <= (others => '0');            
        elsif rising_edge(clk) then
            if baud_inc = conv_std_logic_vector(0,WIDTH+1) then
                baud_inc <= conv_std_logic_vector(gen_inc_val,ACC_WIDTH+1);
            end if;

            baud_accum <= ('0' & baud_accum(ACC_WIDTH-1 downto 0)) + baud_inc;
        end if;
    end process ;
    baudx16_clk <= baud_accum(ACC_WIDTH);
    

    rxunit_1: rxunit_sim
        port map (rxpin        => rxpin,
                  baudx16_clk  => baudx16_clk,
                  rxread       => rxread,
                  rclk         => clk,
                  reset_i      => reset,
                  r_data_avail => r_data_avail,
                  r_full       => open,
                  r_data       => r_data);

    -- convert the data into character
    recv_data <= CONV(r_data);
    
    write_out_proc : process
        variable L : line ;
    begin
        -- wait for data to arrive
        wait until r_data_avail = '1';

        -- write to stdout
        write(L,recv_data);
        if r_data = x"A" or isexit = '1' then
            writeline(output, L);       -- output when newline
        end if;
        
        -- strobe the read
        rxread <= '1';
        wait until r_data_avail = '0';
        rxread <= '0';
        
    end process ;
end model;
