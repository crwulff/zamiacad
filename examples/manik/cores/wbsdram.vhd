-------------------------------------------------------------------------------
-- Title      : WishBone Compliant SDRAM Controller
-- Project    : MANIK-II
-------------------------------------------------------------------------------
-- File       : wbsdram.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech Inc.
-- Created    : 2005-10-04
-- Last update: 2006-08-09
-- Platform   : 
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: WishBone Company SDRAM Controller. Adapted from design by
--		XESS Corp. See Notice below.
-------------------------------------------------------------------------------
-- Copyright (c) 2005 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2005-10-04  1.0      sdutta	Created
-------------------------------------------------------------------------------

--------------------------------------------------------------------
-- Company : XESS Corp.
-- Engineer : Dave Vanden Bout
-- Creation Date : 05/17/2005
-- Copyright : 2005, XESS Corp
-- Tool Versions : WebPACK 6.3.03i
--
-- Description:
-- SDRAM controller
--
-- Revision:
-- 1.4.0
--
-- Additional Comments:
-- 1.4.0:
-- Added generic parameter to enable/disable independent active rows in each bank.
-- 1.3.0:
-- Modified to allow independently active rows in each bank.
-- 1.2.0:
-- Modified to allow pipelining of read/write operations.
-- 1.1.0:
-- Initial release.
--
-- License:
-- This code can be freely distributed and modified as long as
-- this header is not removed.
--------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use IEEE.numeric_std.all;
use work.manikconfig.all;
use WORK.manikpackage.all;

entity sdramCntl is
    generic(FREQ           : natural := 50_000; -- operating frequency in KHz
            PIPE_EN        : boolean := false;  -- if true, enable pipelined read operations
            MAX_NOP        : natural := 10000;  -- number of NOPs before entering self-refresh
            MULTI_ACT_ROWS : boolean := false;  -- if true, allow an active row in each bank
            WIDTH     	   : natural := 32;   	-- host & SDRAM data width
            CAS_LATENCY    : integer := 3;  	-- CAS_LATENCY of SDRAM
            NROWS          : natural := 4096;  	-- number of rows in SDRAM array
            NCOLS          : natural := 256;  	-- number of columns in SDRAM array
            ADDR_WIDTH     : natural := 32;   	-- host-side address width            
            RAM_ADDR_W     : natural := 12;  	-- SDRAM-side address width
            SDRAM_CKES	   : integer := 1);     -- # of cke lines
    port(clk          : in  std_logic;   -- master clock
         lock         : in  std_logic;   -- true if clock is stable
         rst          : in  std_logic;   -- reset
         
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
        
          -- SDRAM side
          cke     : out   std_logic_vector (SDRAM_CKES-1 downto 0);      -- clock-enable to SDRAM
          ce_n    : out   std_logic;      -- chip-select to SDRAM
          ras_n   : out   std_logic;      -- SDRAM row address strobe
          cas_n   : out   std_logic;      -- SDRAM column address strobe
          we_n    : out   std_logic;      -- SDRAM write enable
          ba      : out   std_logic_vector(1 downto 0);  	    -- SDRAM bank address
          sAddr   : out   std_logic_vector(RAM_ADDR_W-1 downto 0);  -- SDRAM row/column address
          sDInOut : inout std_logic_vector(WIDTH-1 downto 0);  	    -- data from SDRAM
          dqm     : out   std_logic_vector(3 downto 0);  	    -- enable bytes of SDRAM databus
          sclk    : out   std_logic     			    -- clock going to SDRAM
         );
end sdramCntl;

architecture arch of sdramCntl is

    constant OUTPUT : std_logic := '1';   -- direction of dataflow w.r.t. this controller
    constant INPUT  : std_logic := '0';
    constant NOP    : std_logic := '0';   -- no operation
    constant READ   : std_logic := '1';   -- read operation
    constant WRITE  : std_logic := '1';   -- write operation

    -- SDRAM timing parameters - For Micron 128MbSDRAM
    constant Tinit : natural := 10;       -- min initialization interval (us)
    constant Tras  : natural := 45;       -- min interval between active to precharge commands (ns)
    constant Trcd  : natural := 20;       -- min interval between active and R/W commands (ns)
    constant Tref  : natural := 64_000_000;  -- maximum refresh interval (ns)
    constant Trfc  : natural := 70;       -- duration of refresh operation (ns)
    constant Trp   : natural := 20;       -- min precharge command duration (ns)
    constant Twr   : natural := 15;       -- write recovery time (ns)
    constant Txsr  : natural := 75;       -- exit self-refresh time (ns)

    -- SDRAM timing parameters converted into clock cycles (based on FREQ)
    constant NORM        : natural := 1_000_000;  -- normalize ns * KHz
    constant INIT_CYCLES : natural := 1+((Tinit*FREQ)/1000);  -- SDRAM power-on initialization interval
    constant RAS_CYCLES  : natural := 1+((Tras*FREQ)/NORM);  -- active-to-precharge interval
    constant RCD_CYCLES  : natural := 1+((Trcd*FREQ)/NORM);  -- active-to-R/W interval
    constant REF_CYCLES  : natural := 1+(((Tref/NROWS)*FREQ)/NORM);  -- interval between row refreshes
    constant RFC_CYCLES  : natural := 1+((Trfc*FREQ)/NORM);  -- refresh operation interval
    constant RP_CYCLES   : natural := 1+((Trp*FREQ)/NORM);  -- precharge operation interval
    constant WR_CYCLES   : natural := 1+((Twr*FREQ)/NORM);  -- write recovery time
    constant XSR_CYCLES  : natural := 1+((Txsr*FREQ)/NORM);  -- exit self-refresh time
    constant MODE_CYCLES : natural := 2;  -- mode register setup time
    constant RFSH_OPS    : natural := 8;  -- number of refresh operations needed to init SDRAM

    signal hDOut,hDIn : std_logic_vector(WIDTH-1 downto 0) := (others => '0');
    signal hAddr      : std_logic_vector(ADDR_WIDTH-3 downto 0) := (others => '0');
    
    -- timer registers that count down times for various SDRAM operations
    signal timer_r, timer_x       : natural range 0 to INIT_CYCLES := 0;  -- current SDRAM op time
    signal rasTimer_r, rasTimer_x : natural range 0 to RAS_CYCLES  := 0;  -- active-to-precharge time
    signal wrTimer_r, wrTimer_x   : natural range 0 to WR_CYCLES   := 0;  -- write-to-precharge time
    signal refTimer_r, refTimer_x : natural range 0 to REF_CYCLES  := 0;  -- time between row refreshes
    signal rfshCntr_r, rfshCntr_x : natural range 0 to NROWS	   := 0;  -- counts refreshes that are neede
    signal nopCntr_r, nopCntr_x   : natural range 0 to MAX_NOP	   := 0;  -- counts consecutive NOP operations

    signal doSelfRfsh : std_logic;        -- active when the NOP counter hits zero and self-refresh can start

    -- states of the SDRAM controller state machine
    signal state_r, state_x : std_logic_vector(2 downto 0) := "000";
    constant INITWAIT    : std_logic_vector(2 downto 0) := "000";  -- init - wait for power-on initialization to complete
    constant INITPCHG    : std_logic_vector(2 downto 0) := "001";  -- init - initial precharge of SDRAM banks
    constant INITSETMODE : std_logic_vector(2 downto 0) := "010";  -- initialization - set SDRAM mode
    constant INITRFSH    : std_logic_vector(2 downto 0) := "011";  -- initialization - do initial refreshes
    constant RW          : std_logic_vector(2 downto 0) := "100";  -- read/write/refresh the SDRAM
    constant ACTIVATE    : std_logic_vector(2 downto 0) := "101";  -- open a row of the SDRAM for reading/writing
    constant REFRESHROW  : std_logic_vector(2 downto 0) := "110";  -- refresh a row of the SDRAM
    constant SELFREFRESH : std_logic_vector(2 downto 0) := "111";  -- keep SDRAM in self-refresh mode with CKE low
    
    -- commands that are sent to the SDRAM to make it perform certain operations
    -- commands use these SDRAM input pins (ce_n,ras_n,cas_n,we_n)
    subtype sdramCmd is unsigned(3 downto 0);
    constant NOP_CMD    : sdramCmd := "0111";
    constant ACTIVE_CMD : sdramCmd := "0011";
    constant READ_CMD   : sdramCmd := "0101";
    constant WRITE_CMD  : sdramCmd := "0100";
    constant PCHG_CMD   : sdramCmd := "0010";
    constant MODE_CMD   : sdramCmd := "0000";
    constant RFSH_CMD   : sdramCmd := "0001";

    -- SDRAM mode register
    -- the SDRAM is placed in a non-burst mode (burst length = 1) with CAS_LATENCY
    subtype sdramMode is std_logic_vector(RAM_ADDR_W-1 downto 0);
    constant MODE : sdramMode := replicate_bit('0',RAM_ADDR_W-10) & "0" & "00" & to_slv(CAS_LATENCY,3) & "0" & "000";

    -- the host address is decomposed into these sets of SDRAM address components
    constant ROW_LEN : natural := log2(NROWS);  -- number of row address bits
    constant COL_LEN : natural := log2(NCOLS);  -- number of column address bits
    signal bank : std_logic_vector(ba'range)             := (others => '0');  -- bank address bits
    signal row  : std_logic_vector(ROW_LEN - 1 downto 0) := (others => '0');  -- row address within bank
    signal col  : std_logic_vector(sAddr'range)          := (others => '0');  -- column address within row

    -- registers that store the currently active row in each bank of the SDRAM
    constant NUM_ACTIVE_ROWS          : integer := int_select(MULTI_ACT_ROWS = false, 1, 2**ba'length);
    constant ARTYPEZERO               : std_logic_vector(row'range) := (others => '0');
    type     activeRowType is array(0 to NUM_ACTIVE_ROWS-1) of std_logic_vector(row'range);
    signal   activeRow_r, activeRow_x : activeRowType               := (others => ARTYPEZERO);
    -- indicates that some row is active
    signal   activeFlag_r, activeFlag_x : std_logic_vector(0 to NUM_ACTIVE_ROWS-1) := (others => '0');
    signal   bankIndex                  : natural range 0 to NUM_ACTIVE_ROWS-1 := 0;  -- bank address bits
     -- indicates the bank with the active row
    signal   activeBank_r, activeBank_x : std_logic_vector(ba'range) := (others => '0'); 
    signal   doActivate                 : std_logic;  -- indicates when a new row in a bank needs to be activated

    -- there is a command bit embedded within the SDRAM column address
    constant CMDBIT_POS    : natural   := 10;  -- position of command bit
    constant AUTO_PCHG_ON  : std_logic := '1';  -- CMDBIT value to auto-precharge the bank
    constant AUTO_PCHG_OFF : std_logic := '0';  -- CMDBIT value to disable auto-precharge
    constant ONE_BANK      : std_logic := '0';  -- CMDBIT value to select one bank
    constant ALL_BANKS     : std_logic := '1';  -- CMDBIT value to select all banks

    -- status signals that indicate when certain operations are in progress
    signal wrInProgress       : std_logic;  -- write operation in progress
    signal rdInProgress       : std_logic;  -- read operation in progress
    signal activateInProgress : std_logic;  -- row activation is in progress

    -- these registers track the progress of read and write operations
    -- pipeline of read ops in progress
    signal rdPipeline_r, rdPipeline_x : std_logic_vector(CAS_LATENCY+1 downto 0) := (others => '0');
    -- pipeline of write ops (only need 1 cycle)
    signal wrPipeline_r, wrPipeline_x : std_logic_vector(0 downto 0) := (others => '0'); 

    -- registered outputs to host
    -- holds data read from SDRAM and sent to the host
    signal hDOut_r, hDOut_x       : std_logic_vector(hDOut'range) := (others => '0');  
    -- holds data read from SDRAM   on opposite clock edge
    signal hDOutOppPhase_r, hDOutOppPhase_x : std_logic_vector(hDOut'range) := (others => '0');  

    -- registered outputs to SDRAM
    signal cke_r, cke_x           : std_logic;  -- clock enable 
    signal cmd_r, cmd_x           : sdramCmd;  -- SDRAM command bits
    signal ba_r, ba_x             : std_logic_vector(ba'range);  -- SDRAM bank address bits
    signal sAddr_r, sAddr_x       : std_logic_vector(sAddr'range);  -- SDRAM row/column address
    signal sData_r, sData_x       : std_logic_vector(sDInOut'range);  -- SDRAM out databus
    signal sDataDir_r, sDataDir_x : std_logic;  -- SDRAM databus direction control bit

    signal rd, wr : std_logic := '0';
    signal done   : std_logic := '0';

    -- registered wish bone input signals
    signal adr_i : std_logic_vector(ADDR_WIDTH-1 downto 0) := (others => '0');
    signal sel_i : std_logic_vector(3 downto 0)            := (others => '0');
    signal dat_i : std_logic_vector (WIDTH-1 downto 0)     := (others => '0');
    signal we_i  : std_logic                               := '0';
    signal stb_i : std_logic                               := '0';
begin

    -----------------------------------------------------------
    -- Translate from/to WishBone interface
    -----------------------------------------------------------
    -- resgister wishbone input signals
    process (clk, rst)
    begin
        if rst = '1' then
            adr_i <= (others => '0');
            sel_i <= (others => '0');
            dat_i <= (others => '0');
            stb_i <= '0';
            we_i  <= '0';
        elsif rising_edge(clk) then
            if done = '1' and stb_i = '1' then
                stb_i <= '0';
            else
                stb_i <= WBS_STB_I;
            end if;
            if WBS_STB_I = '1' then
                adr_i <= WBS_ADR_I;
                sel_i <= WBS_SEL_I;
                dat_i <= WBS_DAT_I;
                we_i  <= WBS_WE_I;
            end if;
        end if;
    end process ;
    
    rd    <= stb_i and not we_i;
    wr    <= stb_i and we_i;
    hDIn  <= dat_i;
    hAddr <= adr_i(ADDR_WIDTH-1 downto 2);
    dqm   <= not sel_i;

    WBS_ACK_O <= done and stb_i;
    WBS_ERR_O <= '0';
    WBS_DAT_O <= hDOut;
    -----------------------------------------------------------
    -- attach some internal signals to the I/O ports 
    -----------------------------------------------------------

    -- attach registered SDRAM control signals to SDRAM input pins
    (ce_n, ras_n, cas_n, we_n) <= cmd_r;    -- SDRAM operation control bits
    cke                        <= replicate_bit(cke_r,SDRAM_CKES);    -- SDRAM clock enable
    ba                         <= ba_r;     -- SDRAM bank address
    sAddr                      <= sAddr_r;  -- SDRAM address
    sDInOut                    <= sData_r when sDataDir_r = OUTPUT else (others => 'Z');  -- SDRAM output data bus

    -- attach some port signals
    hDOut   <= hDOut_r;                   -- data back to host

    -- extract bank field from host address
    ba_x      <= hAddr(ba'length + ROW_LEN + COL_LEN - 1 downto ROW_LEN + COL_LEN);
    bank      <= ba_x when MULTI_ACT_ROWS = false else (others => '0');
    bankIndex <= 0    when MULTI_ACT_ROWS = false else conv_integer(ba_x);

    -- invert clock 
    sclk <= not clk;                    -- should use DLL here to stabilize
    
    -----------------------------------------------------------
    -- compute the next state and outputs 
    -----------------------------------------------------------

    combinatorial : process(rd, wr, hAddr, hDIn, hDOut_r, sDInOut, state_r, activeBank_r,
                            ba_x, col, bank, row, bankIndex, activateInProgress,
                            wrInProgress, rdInProgress, ba_r, doActivate, doSelfRfsh, 
                            activeFlag_r, activeRow_r, rdPipeline_r, wrPipeline_r,
                            hDOutOppPhase_r, nopCntr_r, lock, rfshCntr_r, timer_r, rasTimer_r,
                            wrTimer_r, refTimer_r, cmd_r, cke_r)
    begin

        -----------------------------------------------------------
        -- setup default values for signals 
        -----------------------------------------------------------

        cke_x        <= '1';                -- enable SDRAM clock
        cmd_x        <= NOP_CMD;            -- set SDRAM command to no-operation
        sDataDir_x   <= INPUT;              -- accept data from the SDRAM
        sData_x      <= hDIn(sData_x'range);  -- output data from host to SDRAM
        state_x      <= state_r;            -- reload these registers and flags
        activeFlag_x <= activeFlag_r;       --              with their existing values
        activeRow_x  <= activeRow_r;
        activeBank_x <= activeBank_r;
        rfshCntr_x   <= rfshCntr_r;

        -----------------------------------------------------------
        -- setup default value for the SDRAM address 
        -----------------------------------------------------------

        
        -- extract row, column fields from host address
        row                     <= hAddr(ROW_LEN + COL_LEN - 1 downto COL_LEN);
        -- extend column (if needed) until it is as large as the (SDRAM address bus - 1)
        col                     <= (others => '0');  -- set it to all zeroes
        col(COL_LEN-1 downto 0) <= hAddr(COL_LEN-1 downto 0);

        -- by default, set SDRAM address to the column address with interspersed
        -- command bit set to disable auto-precharge
        sAddr_x <= col(col'high-1 downto CMDBIT_POS) & AUTO_PCHG_OFF & col(CMDBIT_POS-1 downto 0);

        -----------------------------------------------------------
        -- manage the read and write operation pipelines
        -----------------------------------------------------------

        -- determine if read operations are in progress by the presence of
        -- READ flags in the read pipeline 
        if rdPipeline_r(rdPipeline_r'high downto 0) /= 0 then
            rdInProgress <= '1';
        else
            rdInProgress <= '0';
        end if;

        -- enter NOPs into the read and write pipeline shift registers by default
        rdPipeline_x    <= NOP & rdPipeline_r(rdPipeline_r'high downto 1);
        wrPipeline_x(0) <= NOP;

        -- transfer data from SDRAM to the host data register if a read flag has exited the pipeline
        -- (the transfer occurs 1 cycle before we tell the host the read operation is done)
        if rdPipeline_r(1) = READ then
            hDOutOppPhase_x <= sDInOut(hDOut'range);  -- gets value on the SDRAM databus on the opposite phase
            -- get the SDRAM data that was gathered on the previous opposite clock edge
            hDOut_x       <= hDOutOppPhase_r(hDOut'range);
        else
            -- retain contents of host data registers if no data from the SDRAM has arrived yet
            hDOutOppPhase_x <= hDOutOppPhase_r;
            hDOut_x         <= hDOut_r;
        end if;

        done   <= rdPipeline_r(0) or wrPipeline_r(0);  -- a read or write operation is done
        --rdDone <= rdPipeline_r(0);          -- SDRAM data available when a READ flag exits the pipeline 

        -----------------------------------------------------------
        -- manage row activation
        -----------------------------------------------------------

        -- request a row activation operation if the row of the current address
        -- does not match the currently active row in the bank, or if no row
        -- in the bank is currently active
        if (bank /= activeBank_r) or (row /= activeRow_r(bankIndex)) or (activeFlag_r(bankIndex) = '0') then
            doActivate <= '1';
        else
            doActivate <= '0';
        end if;

        -----------------------------------------------------------
        -- manage self-refresh
        -----------------------------------------------------------

        -- enter self-refresh if neither a read or write is requested for MAX_NOP consecutive cycles.
        if (rd = '1') or (wr = '1') then
            -- any read or write resets NOP counter and exits self-refresh state
            nopCntr_x  <= 0;
            doSelfRfsh <= '0';
        elsif nopCntr_r /= MAX_NOP then
            -- increment NOP counter whenever there is no read or write operation 
            nopCntr_x  <= nopCntr_r + 1;
            doSelfRfsh <= '0';
        else
            -- start self-refresh when counter hits maximum NOP count and leave counter unchanged
            nopCntr_x  <= nopCntr_r;
            doSelfRfsh <= '1';
        end if;

        -----------------------------------------------------------
        -- update the timers 
        -----------------------------------------------------------

        -- row activation timer
        if rasTimer_r /= 0 then
            -- decrement a non-zero timer and set the flag
            -- to indicate the row activation is still inprogress
            rasTimer_x         <= rasTimer_r - 1;
            activateInProgress <= '1';
        else
            -- on timeout, keep the timer at zero     and reset the flag
            -- to indicate the row activation operation is done
            rasTimer_x         <= rasTimer_r;
            activateInProgress <= '0';
        end if;

        -- write operation timer            
        if wrTimer_r /= 0 then
            -- decrement a non-zero timer and set the flag
            -- to indicate the write operation is still inprogress
            wrTimer_x    <= wrTimer_r - 1;
            wrInPRogress <= '1';
        else
            -- on timeout, keep the timer at zero and reset the flag that
            -- indicates a write operation is in progress
            wrTimer_x    <= wrTimer_r;
            wrInPRogress <= '0';
        end if;

        -- refresh timer            
        if refTimer_r /= 0 then
            refTimer_x <= refTimer_r - 1;
        else
            -- on timeout, reload the timer with the interval between row refreshes
            -- and increment the counter for the number of row refreshes that are needed
            refTimer_x <= REF_CYCLES;
            rfshCntr_x <= rfshCntr_r + 1;
        end if;

        -- main timer for sequencing SDRAM operations               
        if timer_r /= 0 then
            -- decrement the timer and do nothing else since the previous operation has not completed yet.
            timer_x <= timer_r - 1;
        else
            -- the previous operation has completed once the timer hits zero
            timer_x <= timer_r;               -- by default, leave the timer at zero

            -----------------------------------------------------------
            -- compute the next state and outputs 
            -----------------------------------------------------------
            case state_r is

                -----------------------------------------------------------
                -- let clock stabilize and then wait for the SDRAM to initialize 
                -----------------------------------------------------------
                when INITWAIT =>
                    if lock = '1' then
                        -- wait for SDRAM power-on initialization once the clock is stable
                        timer_x <= INIT_CYCLES;     -- set timer for initialization duration
                        state_x <= INITPCHG;
                    else
                        -- disable SDRAM clock and return to this state if the clock is not stable
                        -- this insures the clock is stable before enabling the SDRAM
                        -- it also insures a clean startup if the SDRAM is currently in self-refresh mode
                        cke_x   <= '0';
                    end if;

                    -----------------------------------------------------------
                    -- precharge all SDRAM banks after power-on initialization 
                    -----------------------------------------------------------
                when INITPCHG =>
                    cmd_x               <= PCHG_CMD;
                    sAddr_x(CMDBIT_POS) <= ALL_BANKS;  -- precharge all banks
                    timer_x             <= RP_CYCLES;  -- set timer for precharge operation duration
                    rfshCntr_x          <= RFSH_OPS;  -- set counter for refresh ops needed after precharge
                    state_x             <= INITRFSH;

                    -----------------------------------------------------------
                    -- refresh the SDRAM a number of times after initial precharge 
                    -----------------------------------------------------------
                when INITRFSH =>
                    cmd_x      <= RFSH_CMD;
                    timer_x    <= RFC_CYCLES;     -- set timer to refresh operation duration
                    rfshCntr_x <= rfshCntr_r - 1;  -- decrement refresh operation counter
                    if rfshCntr_r = 1 then
                        state_x  <= INITSETMODE;    -- set the SDRAM mode once all refresh ops are done
                    end if;

                    -----------------------------------------------------------
                    -- set the mode register of the SDRAM 
                    -----------------------------------------------------------
                when INITSETMODE =>
                    cmd_x   <= MODE_CMD;
                    sAddr_x <= MODE;              -- output mode register bits on the SDRAM address bits
                    timer_x <= MODE_CYCLES;       -- set timer for mode setting operation duration
                    state_x <= RW;

                    -----------------------------------------------------------
                    -- process read/write/refresh operations after initialization is done 
                    -----------------------------------------------------------
                when RW =>
                    -----------------------------------------------------------
                    -- highest priority operation: row refresh 
                    -- do a refresh operation if the refresh counter is non-zero
                    -----------------------------------------------------------
                    if rfshCntr_r /= 0 then
                        -- wait for any row activations, writes or reads to finish before doing a precharge
                        if (activateInProgress = '0') and (wrInProgress = '0') and (rdInProgress = '0') then
                            cmd_x               <= PCHG_CMD;  -- initiate precharge of the SDRAM
                            sAddr_x(CMDBIT_POS) <= ALL_BANKS;  -- precharge all banks
                            timer_x             <= RP_CYCLES;  -- set timer for this operation
                            activeFlag_x        <= (others => '0');  -- all rows are inactive after a precharge op.
                            state_x             <= REFRESHROW;  -- refresh the SDRAM after the precharge
                        end if;
                        -----------------------------------------------------------
                        -- do a host-initiated read operation 
                        -----------------------------------------------------------
                    elsif rd = '1' then
                        -- Wait one clock cycle if the bank address has just changed and
                        -- each bank has its own active row.
                        -- This gives extra time for the row activation circuitry.
                        if (ba_x = ba_r) or (MULTI_ACT_ROWS=false) then
                            -- activate a new row if the current read is outside the active row or bank
                            if doActivate = '1' then
                                -- activate new row only if all previous activations, writes, reads are done
                                if (activateInProgress = '0') and (wrInProgress = '0') and (rdInProgress = '0') then
                                    cmd_x                   <= PCHG_CMD;  -- initiate precharge of the SDRAM
                                    sAddr_x(CMDBIT_POS)     <= ONE_BANK;  -- precharge this bank
                                    timer_x                 <= RP_CYCLES;  -- set timer for this operation
                                    activeFlag_x(bankIndex) <= '0';  -- rows in this bank are inactive after prechrg
                                    state_x                 <= ACTIVATE;  -- activate the new row after prechrg done
                                end if;
                                -- read from the currently active row if no previous read operation
                                -- is in progress or if pipeline reads are enabled
                                -- we can always initiate a read even if a write is already in progress
                            elsif (rdInProgress = '0') or PIPE_EN then
                                cmd_x        <= READ_CMD;  -- initiate a read of the SDRAM
                                -- insert a flag into the pipeline shift register that will exit the end
                                -- of the shift register when the data from the SDRAM is available
                                rdPipeline_x <= READ & rdPipeline_r(rdPipeline_r'high downto 1);
                            end if;
                        end if;
                        -----------------------------------------------------------
                        -- do a host-initiated write operation 
                        -----------------------------------------------------------
                    elsif wr = '1' then
                        -- Wait one clock cycle if the bank address has just
			-- changed and each bank has its own active row.
                        -- This gives extra time for the row activation circuitry.
                        if (ba_x = ba_r) or (MULTI_ACT_ROWS=false) then
                                        -- activate a new row if the current write is outside the active row or bank
                            if doActivate = '1' then
                                -- activate new row only if all previous activations, writes, reads are done
                                if (activateInProgress = '0') and (wrInProgress = '0') and (rdInProgress = '0') then
                                    cmd_x                   <= PCHG_CMD;  -- initiate precharge of the SDRAM
                                    sAddr_x(CMDBIT_POS)     <= ONE_BANK;  -- precharge this bank
                                    timer_x                 <= RP_CYCLES;  -- set timer for this operation
                                    -- rows in this bank are inactive after a precharge operation
                                    activeFlag_x(bankIndex) <= '0';
                                    -- activate the new row after the precharge is done
                                    state_x                 <= ACTIVATE;
                                end if;
                                -- write to the currently active row if no previous read operations are in progress
                            elsif rdInProgress = '0' and wrInProgress = '0' then
                                cmd_x           <= WRITE_CMD;  -- initiate the write operation
                                sDataDir_x      <= OUTPUT;  -- turn on drivers to send data to SDRAM
                                -- set timer so precharge doesn't occur too soon after write operation
                                wrTimer_x       <= WR_CYCLES;
                                -- insert a flag into the 1-bit pipeline shift register that will exit on the
                                -- next cycle.  The write into SDRAM is not actually done by that time, but
                                -- this doesn't matter to the host
                                wrPipeline_x(0) <= WRITE;
                            end if;
                        end if;
                        -----------------------------------------------------------
                        -- do a host-initiated self-refresh operation 
                        -----------------------------------------------------------
                    elsif doSelfRfsh = '1' then
                        -- wait until all previous activations, writes, reads are done
                        if (activateInProgress = '0') and (wrInProgress = '0') and (rdInProgress = '0') then
                            cmd_x               <= PCHG_CMD;  -- initiate precharge of the SDRAM
                            sAddr_x(CMDBIT_POS) <= ALL_BANKS;  -- precharge all banks
                            timer_x             <= RP_CYCLES;  -- set timer for this operation
                            activeFlag_x        <= (others => '0');  -- all rows are inactive after a prechrg op
                            state_x             <= SELFREFRESH;  -- self-refresh the SDRAM after the precharge
                        end if;
                        -----------------------------------------------------------
                        -- no operation
                        -----------------------------------------------------------
                    else
                        state_x <= RW;  -- continue to look for SDRAM operations to execute
                    end if;

                    -----------------------------------------------------------
                    -- activate a row of the SDRAM 
                    -----------------------------------------------------------
                when ACTIVATE                        =>
                    cmd_x                   <= ACTIVE_CMD;
                    sAddr_x                 <= (others => '0');  -- output the address for the row to be activated
                    sAddr_x(row'range)      <= row;
                    activeBank_x            <= bank;
                    activeRow_x(bankIndex)  <= row;  -- store the new active SDRAM row address
                    activeFlag_x(bankIndex) <= '1';  -- the SDRAM is now active
                    rasTimer_x              <= RAS_CYCLES;  -- minimum time before another precharge can occur 
                    timer_x                 <= RCD_CYCLES;  -- minimum time before a read/write operation can occur
                    state_x                 <= RW;  -- return to do read/write operation that initiated this activation

                    -----------------------------------------------------------
                    -- refresh a row of the SDRAM         
                    -----------------------------------------------------------
                when REFRESHROW =>
                    cmd_x      <= RFSH_CMD;
                    timer_x    <= RFC_CYCLES;     -- refresh operation interval
                    rfshCntr_x <= rfshCntr_r - 1; -- decrement the number of needed row refreshes
                    state_x    <= RW;             -- process more SDRAM operations after refresh is done

                    -----------------------------------------------------------
                    -- place the SDRAM into self-refresh and keep it there until further notice           
                    -----------------------------------------------------------
                when SELFREFRESH            =>
                    if (doSelfRfsh = '1') or (lock = '0') then
                        -- keep the SDRAM in self-refresh mode as long as requested and until there is a stable clock
                        cmd_x <= RFSH_CMD;  -- output the refresh command; this is only needed on the first clock cycle
                        cke_x <= '0';    -- disable the SDRAM clock
                    else
                        -- else exit self-refresh mode and start processing read and write operations
                        cke_x        <= '1';        -- restart the SDRAM clock
                        rfshCntr_x   <= 0;          -- no refreshes are needed immediately after leaving self-refresh
                        activeFlag_x <= (others => '0');  -- self-refresh deactivates all rows
                        timer_x      <= XSR_CYCLES;  -- wait this long until read and write operations can resume
                        state_x      <= RW;
                    end if;

                    -----------------------------------------------------------
                    -- unknown state
                    -----------------------------------------------------------
                when others =>
                    state_x <= INITWAIT;          -- reset state if in erroneous state

            end case;
        end if;
    end process combinatorial;


    -----------------------------------------------------------
    -- update registers on the appropriate clock edge     
    -----------------------------------------------------------

    update : process(rst, clk)
    begin

        if rst = '1' then
            -- asynchronous reset
            state_r      <= INITWAIT;
            activeFlag_r <= (others => '0');
            rfshCntr_r   <= 0;
            timer_r      <= 0;
            refTimer_r   <= REF_CYCLES;
            rasTimer_r   <= 0;
            wrTimer_r    <= 0;
            nopCntr_r    <= 0;
            rdPipeline_r <= (others => '0');
            wrPipeline_r <= (others => '0');
            cke_r        <= '0';
            cmd_r        <= NOP_CMD;
            ba_r         <= (others => '0');
            sAddr_r      <= (others => '0');
            sData_r      <= (others => '0');
            sDataDir_r   <= INPUT;
            hDOut_r      <= (others => '0');
        elsif rising_edge(clk) then
            state_r      <= state_x;
            activeBank_r <= activeBank_x;
            activeRow_r  <= activeRow_x;
            activeFlag_r <= activeFlag_x;
            rfshCntr_r   <= rfshCntr_x;
            timer_r      <= timer_x;
            refTimer_r   <= refTimer_x;
            rasTimer_r   <= rasTimer_x;
            wrTimer_r    <= wrTimer_x;
            nopCntr_r    <= nopCntr_x;
            rdPipeline_r <= rdPipeline_x;
            wrPipeline_r <= wrPipeline_x;
            cke_r        <= cke_x;
            cmd_r        <= cmd_x;
            ba_r         <= ba_x;
            sAddr_r      <= sAddr_x;
            sData_r      <= sData_x;
            sDataDir_r   <= sDataDir_x;
            hDOut_r      <= hDOut_x;
        end if;

        -- the register that gets data from the SDRAM and holds it for the host.
        -- is clocked on the opposite edge.  
        if rst = '1' then
            hDOutOppPhase_r <= (others => '0');
        elsif falling_edge(clk) then
            hDOutOppPhase_r <= hDOutOppPhase_x;
        end if;

    end process update;

end arch;
