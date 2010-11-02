-------------------------------------------------------------------------------
-- Title      : SRAM Controller
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : sram_core.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2003-12-17
-- Last update: 2006-06-13
-- Platform   : 
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: Generates signals for controlling an Async SRAM
-- has a Wishbone compliant system interface
-------------------------------------------------------------------------------
-- Copyright (c) 2003 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2003-12-17  1.0      sandeep	Created
-- 2005-06-23  2.0	sandeep Made it Wishbone compliant system interface
-------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;

entity sram_core is
    
    generic (WIDTH      : integer := 32;
             ADDR_WIDTH : integer := 32;
             RAM_ADDR_W : integer := 23;
             RAM_DATA_W : integer := 32;
             COUNT_READ : integer := 9;
             COUNT_WRITE: integer := 8;
             COUNT_WREC : integer := 2);

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
          
          -- SRAM and FLASH Signals
          sram_data_o  : out std_logic_vector (RAM_DATA_W-1 downto 0);
          sram_data_i  : in  std_logic_vector (RAM_DATA_W-1 downto 0);
          sram_data_oe : out std_logic;
          sram_addr    : out std_logic_vector (RAM_ADDR_W-1 downto 0);
          sram_oen     : out std_logic;
          sram_wen     : out std_logic;
          sram_ben     : out std_logic_vector ((RAM_DATA_W/8)-1 downto 0);
          sram_csn     : out std_logic);
end sram_core;

architecture rtl of sram_core is

    signal rcount_sreg : std_logic_vector(15 downto 0) := (others => '0');
    signal wcount_sreg : std_logic_vector(15 downto 0) := (others => '0');
    signal wrecov_sreg : std_logic_vector(COUNT_WREC-1 downto 0) := (others => '0');
    
    signal rdone  : std_logic;
    signal wdone  : std_logic;
    signal rstart : std_logic;
    signal wstart : std_logic;    
    
begin  -- rtl

    --
    -- read wait state counted by shift register
    -- 
    rsreg_proc : process (clk)
    begin
        if rising_edge(clk) then
            rcount_sreg <= rcount_sreg(14 downto 0) & rstart;
        end if;
    end process rsreg_proc;
        
    rdone <= rcount_sreg(COUNT_READ);

    --
    -- write wait states counted by shift register
    --
    wsreg_proc : process (clk)
    begin
        if rising_edge(clk) then
            wcount_sreg <= wcount_sreg(14 downto 0) & wstart;
        end if;
    end process wsreg_proc;
        
    wdone <= wcount_sreg(COUNT_WRITE+1);

    --
    -- write recovery counter
    --
    process (clk, reset)
    begin
        if reset = '1' then
            wrecov_sreg <= (others => '0');
        elsif rising_edge(clk) then
            wrecov_sreg <= wrecov_sreg(COUNT_WREC-2 downto 0) & wdone;
        end if;
    end process ;
    
    WBS_ERR_O <= '0';
        
    ram_data_w_32: if RAM_DATA_W = 32 generate
        signal   seq            : std_logic_vector (6 downto 0) := (others => '0');
        signal   next_seq       : std_logic_vector (6 downto 0) := (others => '0');
        
        constant seq_idle       : std_logic_vector (6 downto 0) := "0000000";
        constant seq_oen_assert : std_logic_vector (6 downto 0) := "0100001";
        constant seq_rack       : std_logic_vector (6 downto 0) := "0100011";
        constant seq_wen_assert : std_logic_vector (6 downto 0) := "0101100";
        constant seq_wack       : std_logic_vector (6 downto 0) := "0110100";
        constant seq_host_wack  : std_logic_vector (6 downto 0) := "1100000";

        signal sram_en : std_logic;

        signal ben_regs : std_logic_vector(3 downto 0) := "0000";
        
        signal sram_data_i_reg : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
        signal sram_data_o_reg : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
        signal sram_addr_o_reg : std_logic_vector (ADDR_WIDTH-1 downto 0) := (others => '0');        
    begin
        sram_en <= WBS_STB_I and WBS_CYC_I  and not seq(5);
        rstart  <= sram_en and not WBS_WE_I and not seq(5);
        wstart  <= sram_en and WBS_WE_I     and not seq(5);

        -- process sram_flash_state_machine
        sram_seq_update : process (clk, reset)
        begin
            if reset = '1' then
                seq <= seq_idle ;
            elsif rising_edge(clk) then
                seq <= next_seq;
            end if;
        end process sram_seq_update;
        
        sram_flash_state_mach: process (seq, rstart, wstart, rdone, wdone)
        begin  
            case seq is
                when seq_idle  =>
                    -- read request
                    if rstart = '1' then
                        -- sram_oen = '0'
                        next_seq <= seq_oen_assert after 1 ns;
                        -- write request
                    elsif wstart = '1' then
                        next_seq <= seq_host_wack  after 1 ns;
                    else
                        next_seq <= seq_idle       after 1 ns;
                    end if;

                when seq_host_wack =>
                    -- sram_data_oe = '0'
                    next_seq <= seq_wen_assert after 1 ns;
                    
                when seq_oen_assert =>
                    -- wait for time to complete read (detemined by counter)
                    if rdone = '1' then
                        -- sram_oen = '0'
                        -- system_rackn   = '0'
                        next_seq <= seq_idle after 1 ns;
                    else
                        next_seq <= seq_oen_assert after 1 ns;
                    end if;
                    
                when seq_wen_assert =>
                    if wdone = '1' then
                        -- deassert sram_wen 
                        next_seq <= seq_wack after 1 ns;
                    else
                        -- keep asserting sram_wen for sram time
                        next_seq <= seq_wen_assert after 1 ns;
                    end if;
                    
                when seq_wack => next_seq <= seq_idle after 1 ns;
                                 
                when others   => next_seq <= seq_idle after 1 ns;
            end case;
            
        end process sram_flash_state_mach;

        process (clk, reset)
        begin
            if reset = '1' then
                ben_regs        <= "0000"          after 1 ns;
                sram_data_o_reg <= (others => '0') after 1 ns;
                sram_addr_o_reg <= (others => '0') after 1 ns;
            elsif rising_edge(clk) then
                if sram_en = '1' then
                    ben_regs        <= WBS_SEL_I after 1 ns;
                    sram_data_o_reg <= WBS_DAT_I after 1 ns;
                    sram_addr_o_reg <= WBS_ADR_I after 1 ns;
                end if;
            end if;
        end process;

        process (clk, reset)
        begin
            if reset = '1' then
                sram_data_i_reg <= (others => '0') after 1 ns;
            elsif rising_edge(clk) then
                sram_data_i_reg <= sram_data_i after 1 ns;            
            end if;
        end process;

-- Control signals

        WBS_DAT_O <= sram_data_i_reg;
        WBS_ACK_O <= rdone or seq(6) ;
        
        sram_oen       <= not (seq(0) or rstart);
        sram_data_oe   <= not seq(2);
        sram_wen       <= not seq(3);
        sram_ben       <= ben_regs when seq(2) = '1' else WBS_SEL_I;
        sram_csn       <= not (seq(5) or sram_en);
        sram_addr      <= sram_addr_o_reg(RAM_ADDR_W+1 downto 2) when seq(2) = '1' else 
                          WBS_ADR_I(RAM_ADDR_W+1 downto 2);
        sram_data_o    <= sram_data_o_reg;
    end generate ram_data_w_32;

    ram_data_w_16 : if RAM_DATA_W = 16 generate
        type sram_states is (sr_idle, sr_read_high, sr_read_low,
                             sr_rmw_high, sr_rmw_low,
                             sr_write_high, sr_write_low, sr_write_done);

        signal sram_cstate, sram_nstate : sram_states := sr_idle;

        signal oe_comb, oe_reg             : std_logic                                  := '0';
        signal we_comb, we_reg             : std_logic                                  := '0';
        signal cs_comb, cs_reg             : std_logic                                  := '0';
        signal data_oen_comb, data_oen_reg : std_logic                                  := '0';
        signal ben_comb, ben_reg           : std_logic_vector (RAM_DATA_W/8-1 downto 0) := (others => '0');
        signal ack_comb, ack_reg           : std_logic                                  := '0';
        signal data_o_comb, data_o_reg     : std_logic_vector (RAM_DATA_W-1 downto 0)   := (others => '0');
        signal data_i_comb, data_i_reg     : std_logic_vector (WIDTH-1 downto 0)        := (others => '0');
        signal addr_reg, addr_comb         : std_logic_vector (RAM_ADDR_W-1 downto 0)   := (others => '0');
        signal wlow_reg, wlow_comb 	   : std_logic := '0';
    begin
        sram_data_o  <= data_o_reg;
        sram_data_oe <= not data_oen_reg;
        sram_addr    <= addr_reg;
        sram_oen     <= not oe_reg;
        sram_wen     <= not we_reg;
        sram_csn     <= not cs_reg;
        sram_ben     <= ben_reg;

        WBS_ACK_O    <= ack_reg;
        WBS_DAT_O    <= data_i_reg;
        
        -- update the state machine
        process (clk, reset)
        begin
            if reset = '1' then
                sram_cstate  <= sr_idle;
                oe_reg       <= '0';
                data_oen_reg <= '0';
                we_reg       <= '0';
                cs_reg       <= '0';
                ben_reg      <= (others => '0');
                data_o_reg   <= (others => '0');
                data_i_reg   <= (others => '0');
                ack_reg      <= '0';
                addr_reg     <= (others => '0');
                wlow_reg     <= '0';
            elsif rising_edge(clk) then
                sram_cstate  <= sram_nstate;
                oe_reg       <= oe_comb;
                data_oen_reg <= data_oen_comb;
                we_reg       <= we_comb;
                cs_reg       <= WBS_STB_I;
                ben_reg      <= ben_comb;
                data_o_reg   <= data_o_comb;
                data_i_reg   <= data_i_comb;
                ack_reg      <= ack_comb;
                addr_reg     <= addr_comb;
                wlow_reg     <= wlow_comb;
            end if;
        end process;

        process (sram_cstate, rdone, wdone, wlow_reg, WBS_STB_I, WBS_WE_I, WBS_SEL_I)
        begin
            sram_nstate   <= sram_cstate;
            oe_comb       <= oe_reg;
            data_oen_comb <= data_oen_reg;
            we_comb       <= we_reg;
            ben_comb      <= ben_reg;
            data_o_comb   <= data_o_reg;
            data_i_comb   <= data_i_reg;
            addr_comb     <= addr_reg;
            ack_comb      <= '0';
            wstart	  <= '0';
            rstart	  <= '0';
            wlow_comb     <= wlow_reg;
            
            case sram_cstate is
                when sr_idle =>
                    wlow_comb <= '0';

                    if WBS_STB_I = '1' and ack_reg = '0' then
                        if WBS_WE_I = '1' then
                            if WBS_SEL_I(3 downto 2) /= "00" then
                                addr_comb     <= WBS_ADR_I(RAM_ADDR_W downto 2) & '0';
                                sram_nstate <= sr_write_high;
                            elsif WBS_SEL_I(1 downto 0) /= "00" then
                                addr_comb     <= WBS_ADR_I(RAM_ADDR_W downto 2) & '1';                                
                                sram_nstate <= sr_write_low;
                            end if;
                        elsif wrecov_sreg = conv_std_logic_vector(0,COUNT_WREC) then                            
                            rstart        <= '1';
                            oe_comb       <= '1';
                            we_comb       <= '0';
                            data_oen_comb <= '0';
                            -- read high 
                            if WBS_SEL_I(3 downto 2) /= "00" then
                                ben_comb      <= WBS_SEL_I(3 downto 2);
                                addr_comb     <= WBS_ADR_I(RAM_ADDR_W downto 2) & '0';
                                sram_nstate   <= sr_read_high;
                            -- read low
                            elsif WBS_SEL_I(1 downto 0) /= "00" then
                                ben_comb      <= WBS_SEL_I(1 downto 0);
                                addr_comb     <= WBS_ADR_I(RAM_ADDR_W downto 2) & '1';
                                sram_nstate   <= sr_read_low;
                            end if;
                        end if;
                    end if;

                when sr_read_high =>
                    if rdone = '1' then
                        if WBS_SEL_I(3) = '1' then
                            data_i_comb(WIDTH-1 downto 3*WIDTH/4) <=
                                sram_data_i(RAM_DATA_W-1 downto RAM_DATA_W/2);
                        end if;
                        if WBS_SEL_I(2) = '1' then
                            data_i_comb((3*WIDTH/4)-1 downto 2*WIDTH/4) <=
                                sram_data_i(RAM_DATA_W/2-1 downto 0);
                        end if;
                        if WBS_SEL_I(1 downto 0) /= "00" then
                            ben_comb    <= WBS_SEL_I(1 downto 0);
                            rstart      <= '1';
                            we_comb       <= '0';
                            data_oen_comb <= '0';
                            sram_nstate <= sr_read_low;
                            addr_comb   <= WBS_ADR_I(RAM_ADDR_W downto 2) & '1';
                        else
                            sram_nstate <= sr_idle;
                            oe_comb     <= '0';
                            ack_comb    <= '1';
                        end if;
                    end if;

                when sr_read_low =>
                    if rdone = '1' then
                        if WBS_SEL_I(1) = '1' then
                            data_i_comb(2*WIDTH/4-1 downto WIDTH/4) <=
                                sram_data_i(RAM_DATA_W-1 downto RAM_DATA_W/2);
                        end if;
                        if WBS_SEL_I(0) = '1' then
                            data_i_comb(WIDTH/4-1 downto 0) <=
                                sram_data_i(RAM_DATA_W/2-1 downto 0);
                        end if;
                        sram_nstate <= sr_idle;
                        oe_comb     <= '0';
                        ack_comb    <= '1';
                    end if;

                when sr_write_high =>
                    -- write both bytes 
                    if WBS_SEL_I(3 downto 2) = "11" then
                        wstart        <= '1';
                        we_comb       <= '1';
                        data_oen_comb <= '1';
                        data_o_comb   <= WBS_DAT_I(WIDTH-1 downto WIDTH/2);
                        sram_nstate   <= sr_write_done;
                    else
                        -- else start a read
                        rstart        <= '1';
                        we_comb       <= '0';
                        oe_comb       <= '1';
                        data_oen_comb <= '0';
                        sram_nstate   <= sr_rmw_high;
                    end if;
                    
                    if WBS_SEL_I(1 downto 0) /= "00" then
                        wlow_comb <= '1';                        
                    end if;
                    
                when sr_rmw_high =>
                    if rdone = '1' then
                        wstart        <= '1';
                        oe_comb       <= '0';
                        we_comb       <= '1';
                        data_oen_comb <= '1';

                        -- highest byte
                        if WBS_SEL_I(3) = '1' then
                            data_o_comb  <= WBS_DAT_I(4*WIDTH/4-1  downto 3*WIDTH/4) &
                                            sram_data_i(RAM_DATA_W/2-1 downto 0);
                        else
                        -- assume lower byte
                            data_o_comb  <= sram_data_i(RAM_DATA_W-1 downto RAM_DATA_W/2) &
                                            WBS_DAT_I(4*WIDTH/4-1  downto 3*WIDTH/4);
                        end if;
                        sram_nstate <= sr_write_done;
                    end if;
                    
                when sr_write_low  =>
                    -- write both bytes 
                    if WBS_SEL_I(1 downto 0) = "11" then
                        wstart        <= '1';
                        we_comb       <= '1';
                        data_oen_comb <= '1';
                        data_o_comb   <= WBS_DAT_I(WIDTH/2-1 downto 0);
                        sram_nstate   <= sr_write_done;
                    else
                        -- else start a read
                        rstart        <= '1';
                        oe_comb       <= '1';
                        we_comb       <= '0';
                        data_oen_comb <= '0';
                        sram_nstate   <= sr_rmw_low;
                    end if;
                    wlow_comb <= '0';
                    
                when sr_rmw_low =>
                    if rdone = '1' then
                        wstart        <= '1';
                        oe_comb       <= '0';
                        we_comb       <= '1';
                        data_oen_comb <= '1';

                        -- highest byte
                        if WBS_SEL_I(1) = '1' then
                            data_o_comb  <= WBS_DAT_I(2*WIDTH/4-1  downto WIDTH/4) &
                                            sram_data_i(RAM_DATA_W/2-1 downto 0);
                        else
                        -- assume lower byte
                            data_o_comb  <= sram_data_i(RAM_DATA_W-1 downto RAM_DATA_W/2) &
                                            WBS_DAT_I(WIDTH/4-1  downto 0);
                        end if;
                        sram_nstate <= sr_write_done;
                    end if;
                    
                when sr_write_done =>
                    if wdone = '1' then
                        if wlow_reg = '1' then
                            sram_nstate <= sr_write_low;
                        else
                            we_comb     <= '0';
                            ack_comb    <= '1';
                            sram_nstate <= sr_idle;
                        end if;
                    end if;
                    
                when others => null;
            end case;
        end process;
            
    end generate ram_data_w_16;
end rtl;

