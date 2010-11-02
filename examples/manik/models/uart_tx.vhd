-------------------------------------------------------------------------------
-- Title      : UART Transmitter model
-- Project    : MANIK-II
-------------------------------------------------------------------------------
-- File       : uart_tx.vhd
-- Author     : Sandeep (sandeep@niktech.com)
-- Company    : NikTech Inc
-- Created    : 2006-02-12
-- Last update: 2006-09-26
-- Platform   : 
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: Will wait for a initial time and send a string
-------------------------------------------------------------------------------
-- Copyright (c) 2006 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2006-02-12  1.0      Sandeep	Created
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-- - div16 - divide a clock by 16
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity div16_sim is
    generic (XILINX_virtex : boolean := FALSE);
    port (iclk : in  std_logic;
          oclk : out std_logic);

end div16_sim;

architecture Behavioral of div16_sim is
    signal vccc     : std_logic;
    signal bbclk    : std_logic := '0';
    signal divisor : std_logic_vector (3 downto 0) := (others => '0');
    signal pulse   : std_logic;
    
begin
    oclk <= bbclk;

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
end Behavioral;
-------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity txunit_sim is
    generic (XILINX_virtex : boolean := TRUE);
    Port (baudx16_clk : in  std_logic;
          wclk        : in  std_logic;
          txwrite     : in  std_logic;
          t_full      : out std_logic;
          t_empty     : out std_logic;
          TxPin       : out std_logic;
          t_data      : in  std_logic_vector(7 downto 0);
          reset_i     : in  std_logic);
end txunit_sim;

architecture Behavioral of txunit_sim is

    signal shiftReg : std_logic_vector (7 downto 0) := "00000000";
    signal state    : std_logic                     := '0';
    signal sv       : std_logic                     := '0';
    signal srle     : std_logic_vector (8 downto 0) := "000000000";
    signal bbclk    : std_logic                     := '0';
    signal TxPinL   : std_logic                     := '0';

    component div16_sim
        generic (XILINX_virtex : boolean);
        port (iclk : in  std_logic;
              oclk : out std_logic);
    end component;

    signal state_prev : std_logic := '0';
    signal send_next  : std_logic := '0';
    signal data_out   : std_logic_vector(7 downto 0) := (others => '0');
begin  -- Behavioral

    TxPin     <= not TxPinL;

    div16_inst : div16_sim
        generic map (XILINX_virtex => false)
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

library std;
use std.textio.all;
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity uart_tx is
    
    generic (BAUD_RATE    : integer := 115200;
             CORE_CLK_MHZ : integer := 50;
             WIDTH        : integer := 32);

    port (clk        : in  std_logic;
          reset      : in  std_logic;
          txpin      : out std_logic;
          init_delay : in  time;
          sstr       : in  string);

end uart_tx;

architecture model of uart_tx is

    component txunit_sim
        generic (XILINX_virtex : boolean);
        port (baudx16_clk : in  std_logic;
              wclk        : in  std_logic;
              txwrite     : in  std_logic;
              t_full      : out std_logic;
              t_empty     : out std_logic;
              TxPin       : out std_logic;
              t_data      : in  std_logic_vector(7 downto 0);
              reset_i     : in  std_logic);
    end component;

    signal baudx16_clk : std_logic := '0';
    signal txwrite     : std_logic := '0';
    signal t_full      : std_logic;
    signal t_empty     : std_logic;
    signal t_data      : std_logic_vector(7 downto 0) := x"00";
    signal data_int    : integer := 0;
    signal curr_char   : character := '0';
    signal tdelay      : std_logic := '0';

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

    txunit_1: txunit_sim
        generic map (XILINX_virtex => false)
        port map (baudx16_clk => baudx16_clk,
                  wclk        => clk,
                  txwrite     => txwrite,
                  t_full      => t_full,
                  t_empty     => t_empty,
                  TxPin       => txpin,
                  t_data      => t_data,
                  reset_i     => reset);

    t_data <= conv_std_logic_vector(character'pos(curr_char),8);
    tdelay <= '1' after init_delay;
    
    tx_process: process
        variable i : integer := 1;        
    begin
        wait until tdelay = '1';
        while i <= sstr'length loop
            if (sstr(i) = '@') then
                wait for 1 ms;
                i := i + 1;
                next;
            end if;
            if (sstr(i) = '^') then
                wait for 5 ms;
                i := i + 1;
                next;
            end if;
            
            curr_char <= sstr(i);
            txwrite <= '1';
            wait until t_empty = '0';
            txwrite <= '0';
            wait until t_empty = '1';
            i := i + 1;            
        end loop;
        wait;                           -- forver            
    end process tx_process;
end model;
