----------------------------------------------------------------------------
--
-- PROJECT         DDR-SDRAM Controller Core
--
-- AUTHOR          Markus Lemke
--                 markus@opencores.org
--                 www.array-electronics.de
--
-- SIMULATOR       Model Technology ModelSim 5.4
-- COMPILER        Exemplar Leonardo Spectrum 2001.1d
--
-- DATE            $Date: 2006/10/10 01:01:16 $
--
-- LANGUAGE        VHDL 93
--
-- LIBRARY         ieee, unisim
--
--
----------------------------------------------------------------------------
--
-- Copyright (C) 2002 Markus Lemke, www.array-electronics.de
--  
-- Everyone is permitted to copy and distribute  and  modify 
-- this  document  under  the  terms of the OpenIPCore Hardware
-- General  Public License "OHGPL" which can  be  read  in  the
-- file LICENSE.
--  
-- The  License  grants  you  the right to copy, modify  and
-- redistribute this file,  but only under  certain  conditions 
-- described in the License.  Among other things,  the  License
-- requires that  this  copyright  notice  and  the  associated
-- disclaimer  be preserved on  all copies.  Every copy of this
-- file must include a copy of the License, normally in a plain
-- ASCII text file named LICENSE.
--  
-- 
-- DISCLAIMER
-- 
-- THIS SOFTWARE  IS  PROVIDED  ``AS IS''  AND  WITHOUT ANY  
-- EXPRESS  OR  IMPLIED  WARRANTIES, INCLUDING, BUT NOT LIMITED 
-- TO, THE  IMPLIED  WARRANTIES OF  MERCHANTABILITY AND FITNESS
-- FOR A  PARTICULAR  PURPOSE.  IN NO EVENT SHALL THE AUTHOR OR 
-- CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
-- SPECIAL,  EXEMPLARY,  OR CONSEQUENTIAL  DAMAGES  (INCLUDING,
-- BUT NOT  LIMITED TO,  PROCUREMENT  OF  SUBSTITUTE  GOODS  OR 
-- SERVICES;  LOSS  OF  USE,  DATA,  OR  PROFITS;  OR  BUSINESS 
-- INTERRUPTION) HOWEVER CAUSED  AND ON ANY THEORY O LIABILITY,
-- WHETHER  IN CONTRACT, STRICT  LIABILITY, OR  TORT (INCLUDING 
-- NEGLIGENCE OR OTHERWISE)  ARISING IN ANY WAY OUT OF THE  USE 
-- OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH 
-- DAMAGE.                              
----------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- 
-------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;

package ddr_sdr_conf_pkg is
   
   -- User Interface Commands
   constant NOP_CMD     : std_logic_vector(1 downto 0) := "00";
   constant RD_CMD      : std_logic_vector(1 downto 0) := "01";
   constant WR_CMD      : std_logic_vector(1 downto 0) := "10";
   constant RFSH_CMD    : std_logic_vector(1 downto 0) := "11";
   
end ddr_sdr_conf_pkg;

package body ddr_sdr_conf_pkg is
end;
-------------------------------------------------------------------------------
-- 
-------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
entity reset is
    port (clk    : in  std_logic;
          rst_in : in  std_logic := '0';
          srst_q : out std_logic);
end;

-- pragma translate_off
library unisim;
-- pragma translate_on
architecture reset of reset is
    component fdp
        port (d    : in  std_logic;
               c   : in  std_logic;
               pre : in  std_logic;
               q   : out std_logic);
    end component;
    -- pragma translate_off
    for all: fdp use entity unisim.fdp;
    -- pragma translate_on

   
    signal shift_q : std_logic_vector(3 downto 0);
    attribute preserve_signal : boolean;
    attribute preserve_signal of shift_q : signal is true;

begin
   
    fdp0: fdp port map ( d => rst_in, c => clk,
                        pre => '0', q => shift_q(3) );
    
   fdp1: fdp port map ( d => shift_q(3), c => clk,
                        pre => '0', q => shift_q(2) );
    
   fdp2: fdp port map ( d => shift_q(2), c => clk,
                        pre => '0', q => shift_q(1) );
    
   fdp3: fdp port map ( d => shift_q(1), c => clk,
                        pre => '0', q => shift_q(0) );
   
   srst_q <= shift_q(0);
end;
-------------------------------------------------------------------------------
-- 
-------------------------------------------------------------------------------
library ieee, work;
use ieee.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;
use work.ddr_sdr_conf_pkg.all;

entity user_if is
    generic (U_ADDR_WIDTH   : integer;
             DDR_ADDR_WIDTH : integer;
             DDR_BANK_WIDTH : integer;
             REFRESH_CLOCKS : natural;
             REFRESH_WINDOW : positive);
    port (rst_int_n : in std_logic;     -- async reset, lo-active
          sys_clk   : in std_logic;

          -- user interface
          cmd     : in  std_logic_vector(1 downto 0);  -- command: read, write, nop
          cmd_vld : in  std_logic;
          addr    : in  std_logic_vector(U_ADDR_WIDTH-1 downto 0);  -- ROW, BANK, COLUMN
          busy_q  : out std_logic;  -- busy flag, when active commands are ignored

          -- Datenaustausch mit Controller
          init_finished : in  boolean;
          new_cmd_q     : out boolean;
          new_cmd_ack   : in  boolean;
          do_prech_q    : out boolean;  -- precharge followed by activate
          do_wait_q     : out boolean;  -- additional delay requested

          cmd_q  : out    std_logic_vector(1 downto 0);
          addr_q : out    std_logic_vector(U_ADDR_WIDTH -1 downto 0)
          );
end;

architecture behave of user_if is

    type   IN_STATE_TYPE is (IDLE, READY, BUSY, REFRESH);
    signal state_q : IN_STATE_TYPE;

    signal addr_int_q : std_logic_vector(U_ADDR_WIDTH -1 downto 0);
    signal cmd_int_q  : std_logic_vector(1 downto 0);
    signal busy_int_q : boolean;

    signal row_q : std_logic_vector(DDR_ADDR_WIDTH + DDR_BANK_WIDTH downto 0);

    -- Refresh flags
    signal do_rfsh_q       : boolean;
    signal rfsh_now_q      : boolean;
    signal rfsh_req_q      : boolean;
    signal rfsh_ena_q      : boolean;
    signal internal_rfsh_q : boolean;

begin
    
    process (sys_clk, rst_int_n) is
        variable new_row : std_logic_vector(DDR_ADDR_WIDTH + DDR_BANK_WIDTH downto 0);
    begin 
        if rst_int_n = '0' then  

            addr_int_q      <= (others=>'0');
            cmd_int_q       <= (others=>'0');
            state_q         <= IDLE;
            new_cmd_q       <= false;
            do_prech_q      <= false;
            busy_int_q      <= true;
            do_wait_q       <= false;
            row_q           <= (others =>'1');
            do_rfsh_q       <= false;
            rfsh_ena_q      <= false;
            internal_rfsh_q <= false;
            rfsh_now_q      <= false;
            
        elsif rising_edge(sys_clk) then

            -- extract ROW from user address
            new_row := '0' & addr(addr_int_q'LEFT downto DDR_ADDR_WIDTH);
            
            internal_rfsh_q <= false;

            case state_q is
                when IDLE =>
                    busy_int_q   <= true; -- as long as controller is in initialization state
                    new_cmd_q    <= false;
                    rfsh_ena_q   <= false;
                    do_rfsh_q    <= false;
                    rfsh_now_q   <= false;
                    
                    if init_finished then -- SDRAM initialization finished
                        -- now allow external commands
                        state_q     <= READY;
                        busy_int_q  <= false;
                        rfsh_ena_q  <= true;
                    end if;
                    
                when READY =>
                    rfsh_now_q <= rfsh_req_q;
                    -- Refresh has highest priority
                    if cmd_vld='1' then -- new command requested
                        busy_int_q    <= true;
                        new_cmd_q     <= true;
                        state_q       <= BUSY;
                    elsif rfsh_now_q then
                        busy_int_q    <= true;
                        new_cmd_q     <= true;
                        do_rfsh_q     <= true; -- Switch CMD-MUX to Refresh Command
                        state_q       <= REFRESH;
                    end if;
                    
                when BUSY =>
                    if new_cmd_ack then
                        if rfsh_now_q then
                            new_cmd_q     <= true;
                            do_rfsh_q     <= true; -- Switch CMD-MUX to Refresh Command
                            state_q       <= REFRESH;
                        else
                            new_cmd_q  <= false;
                            state_q    <= READY;
                            busy_int_q <= false;
                        end if;
                        
                    end if;
                    
                when REFRESH =>
                    rfsh_now_q      <= false;
                    internal_rfsh_q <= true;
                    if new_cmd_ack then
                        internal_rfsh_q <= false;
                        new_cmd_q <= false;
                        state_q       <= READY;
                        busy_int_q    <= false;
                        do_rfsh_q     <= false; -- Refresh command
                    end if;
            end case;
            
            -- Store command and address by control of handshake signals
            if cmd_vld='1' and not busy_int_q then
                cmd_int_q    <= cmd;
                addr_int_q   <= addr;
                do_wait_q    <= false; -- default

                if cmd = WR_CMD or cmd = RD_CMD then
                    
                    if row_q /= new_row then -- new ROW requested, do precharge followed by activate
                        do_prech_q  <= true;    -- Precharge, Activate
                        do_wait_q   <= true;    -- tWR !
                        row_q       <= new_row; -- store this row and clear the 'first access' bit
                    else
                        do_prech_q  <= false;
                        do_wait_q   <= false; -- tWR !
                    end if;
                    
                    -- changing the command should cause a delay
                    -- the same is true for changing the ROW (see above)
                    if (cmd_int_q /= cmd) then
                        do_wait_q <= true;
                    end if;
                    
                end if;
                
            end if;
        end if;
    end process;
    
    ------------------------------------------------------------------------------------------
    -- REFRESH COUNTER : every 64 ms a complete refresh cycle is needed
    -- with every refresh the ref_cnt_q is loaded
    ------------------------------------------------------------------------------------------
    ref_cnt: block
        signal ref_cnt_q : integer range 0 to 9 * (REFRESH_CLOCKS -1); -- for Multiple Refresh
    begin 
        process (sys_clk, rst_int_n) is
        begin 
            if rst_int_n = '0' then 
                
                ref_cnt_q  <= REFRESH_CLOCKS -1;
                rfsh_req_q <= false;
                
            elsif rising_edge(sys_clk) then  
                
                if rfsh_ena_q then -- erst nach DDR_SDRAM_INIT AUTO-Refresh zulassen
                    
                    if do_rfsh_q then -- Internal Auto Refresh
                        ref_cnt_q <= REFRESH_CLOCKS -1;
                    else
                        ref_cnt_q <= ref_cnt_q -1;
                    end if;
                    
                end if;

                assert ref_cnt_q >= 0 report "ref_cnt_q error!!!" severity failure;
                
                -- a refresh window is used rather than comparing the counter to zero
                -- to guarantee the maximum refresh period in all cases
                rfsh_req_q  <= (ref_cnt_q < REFRESH_WINDOW);

            end if;
        end process;
        
    end block ref_cnt;       

    -- Mapping internl signals -> ouputs
    addr_q    <= addr_int_q;
    cmd_q     <= cmd_int_q when not do_rfsh_q else RFSH_CMD;
    busy_q    <= '1' when busy_int_q else '0';
end;

------------------------------------------------------------------------------------------------------------
-- DDR-SDRAM Controller Core
------------------------------------------------------------------------------------------------------------
library ieee, work;
use ieee.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;
use work.ddr_sdr_conf_pkg.all;

entity ddr_sdr is
    generic (FREQ_KHZ       : positive := 50_000;  -- frequency in KHz
             U_ADDR_WIDTH   : positive := 28;  -- (2 * DDR_ADDR_WIDTH) + DDR_BANK_WIDTH
             U_DATA_WIDTH   : positive := 32;  -- (2 * DDR_DATA_WIDTH)
             U_DM_WIDTH     : positive := 4;  --  (2 * DDR_DM_WIDTH)
             DDR_DM_WIDTH   : positive := 2;  --  (DDR_DATA_WIDTH/8) 
             DDR_DQS_WIDTH  : positive := 2;  --  (DDR_DATA_WIDTH/8)
             DDR_DATA_WIDTH : positive := 16;  -- External DDR-SDRAM Module data bus width
             DDR_ADDR_WIDTH : positive := 13;  -- number of address lines to DDR-SDRAM Device/Module
             DDR_BANK_WIDTH : positive := 2;   -- Number of BANK address lines of external DDR-SDRAM
             AUTO_PRECHARGE : positive := 10   -- bit-position in column address for auto precharge (see Data Sheet)
             ); 

    port (-- Clocks and Reset
        rst_n : in std_logic;           -- external async reset, low active
        clk   : in std_logic;  -- system clock (e.g. 100MHz), from fpga pad

        sys_rst_qn  : out std_logic;  -- sync reset low active, released after dcms locked
        sys_clk_out : out std_logic;    -- system clock, dcm output
        clk_fb      : in  std_logic;    -- feedback clock

        -- User Interface
        cmd     : in  std_logic_vector(1 downto 0);  -- command: read, write, nop
        cmd_vld : in  std_logic;        -- '1' when command valid
        addr    : in  std_logic_vector(U_ADDR_WIDTH-1 downto 0);  -- (ROW & BANK & COL)
        busy_q  : out std_logic;  -- busy flag, when active commands are ignored

        -- Input Data
        data_in    : in  std_logic_vector(U_DATA_WIDTH-1 downto 0);
        dm_in      : in  std_logic_vector(U_DM_WIDTH-1 downto 0);
        data_req_q : out std_logic;  -- '1' two clocks before data is clocked in
        data_out_q : out std_logic_vector(U_DATA_WIDTH -1 downto 0);  -- read data
        data_vld_q : out std_logic;     -- data_out_q is valid when '1'

        -- DDR SDRAM Signals
        sdr_clk   : out   std_logic;    -- ddr_sdram_clock
        sdr_clk_n : out   std_logic;    -- /ddr_sdram_clock
        cke_q     : out   std_logic;    -- clock enable
        cs_qn     : out   std_logic;    -- /chip select
        ras_qn    : out   std_logic;    -- /ras
        cas_qn    : out   std_logic;    -- /cas
        we_qn     : out   std_logic;    -- /write enable
        dm_q      : out   std_logic_vector(DDR_DM_WIDTH-1 downto 0);  -- data mask bits, set to "00"
        dqs_q     : out   std_logic_vector(DDR_DQS_WIDTH-1 downto 0);  -- data strobe, only for write
        ba_q      : out   std_logic_vector(DDR_BANK_WIDTH-1 downto 0);  -- bank select
        a_q       : out   std_logic_vector(DDR_ADDR_WIDTH-1 downto 0);  -- address bus 
        data      : inout std_logic_vector(DDR_DATA_WIDTH-1 downto 0);  -- bidir data bus

        -- Status
        dcm_error_q : out std_logic     -- indicates DCM Errors
        );
    
    attribute buffer_sig : string;
    attribute buffer_sig of clk    : signal is "IBUFG";
    attribute buffer_sig of clk_fb : signal is "IBUFG";
end;

library unisim;

architecture behave of ddr_sdr is

    constant CLKIN_PERIOD : real := (real(1000_000)/real(FREQ_KHZ));
    constant NORM : natural := 1_000_000;  -- normalize ns * KHz
    
    -- These values are for MT46V32M16 - DataSheet (-6T)
    constant TRPns  : integer := 15;
    constant TRASns : integer := 42;
    constant TRFCns : integer := 72;
    constant TMRDns : integer := 12;
    constant TRCDns : integer := 15;
    constant TWRns  : integer := 15;
    
    -- Values is CYCLES derived from (FREQ in KHz)
    constant TRP  : positive := integer(1.0+(real(TRPns*FREQ_KHZ)/real(NORM)));  -- precharge command period
    constant TRAS : positive := integer(1.0+(real(TRASns*FREQ_KHZ)/real(NORM)));  -- active to precharge delay 
    constant TRFC : positive := integer(1.0+(real(TRFCns*FREQ_KHZ)/real(NORM)));  -- auto refresh command period
    constant TMRD : positive := integer(1.0+(real(TMRDns*FREQ_KHZ)/real(NORM)));  -- load mode register command cylce time
    constant TRCD : positive := integer(1.0+(real(TRCDns*FREQ_KHZ)/real(NORM)));  -- active to read or write delay !
    constant TWR  : positive := integer(1.0+(real(TWRns*FREQ_KHZ)/real(NORM)));  -- write recovery time

     -- After REFRESH_CLOCKS a refresh cycle , 64ms / 8192 = max every 7.8125 us refesh 
    constant TRefresh_ns    : integer  := 7813;
    constant REFRESH_CLOCKS : positive := integer(real(TRefresh_ns*FREQ_KHZ)/real(NORM));
    constant REFRESH_WINDOW : positive := 25;  -- refresh window
   
   
   -----------------------------------------------------------------------------------------
   --  MODE Register settings
   constant MODE_REG_DLL_RST : std_logic_vector(DDR_ADDR_WIDTH-1 downto 0) := "0000100100001"; -- CAS 2, BL=2, BT=Seq
   constant MODE_REG_DLL_ENA : std_logic_vector(DDR_ADDR_WIDTH-1 downto 0) := "0000000100001";
   constant CAS_LAT : integer := 2; -- fixed value for synthesis

   -- EXT_MODE_REG: dll enabled, reduced drive strength - all from datasheet
   constant EXT_MODE_REG       : std_logic_vector(DDR_ADDR_WIDTH -1 downto 0) := "00000000000" & '1' & '0';
   constant EXT_MODE_REG_BANK  : std_logic_vector(DDR_BANK_WIDTH -1 downto 0) := "01";
   
   -- BASE_MODE_REG
   constant BASE_MODE_REG_BANK : std_logic_vector(DDR_BANK_WIDTH -1 downto 0) := "00";

   -- Table of Timing Parameters as a function of operation
   type SDR_CMDS_TYPE is (NOP, MODE, EXT_MODE, ACTIVE, PRECHARGE, BURST_RD, BURST_WR, REFRESH, WAIT_WR);

   type PART_IF_TYPE is
   record
       cs_n    : std_logic;
       ras_n   : std_logic;
       cas_n   : std_logic;
       we_n    : std_logic;
       dqmb    : std_logic_vector(7 downto 0);
       ct      : integer range 0 to 10;  
   end record PART_IF_TYPE;
   
   type PART_IF_ARRAY_TYPE is array (SDR_CMDS_TYPE) of PART_IF_TYPE;
   
   constant COMMAND : PART_IF_ARRAY_TYPE :=
       -- command                 cs_n     ras_qn      cas_qn      we_qn        dqmb          ct
       (NOP              =>   (    '1',      '1',        '1',        '1',       x"00",        0        ),
        MODE             =>   (    '0',      '0',        '0',        '0',       x"00",        TMRD-1   ),
        EXT_MODE         =>   (    '0',      '0',        '0',        '0',       x"00",        TMRD-1   ),
        ACTIVE           =>   (    '0',      '0',        '1',        '1',       x"00",        TRCD-1   ),
        PRECHARGE        =>   (    '0',      '0',        '1',        '0',       x"00",        TRP-1    ),
        BURST_RD         =>   (    '0',      '1',        '0',        '1',       x"00",        0        ),  
        BURST_WR         =>   (    '0',      '1',        '0',        '0',       x"00",        0        ),  
        REFRESH          =>   (    '0',      '0',        '0',        '1',       x"00",        TRFC-1   ),
        WAIT_WR          =>   (    '1',      '1',        '1',        '1',       x"00",        TWR+TRP-1 )  
        );


    -- User Interface
    component user_if is
        generic (U_ADDR_WIDTH   : integer;
                 DDR_ADDR_WIDTH : integer;
                 DDR_BANK_WIDTH : integer;
                 REFRESH_CLOCKS : natural;
                 REFRESH_WINDOW : positive);
        port (rst_int_n             : in std_logic; -- async reset, lo-active
              sys_clk               : in std_logic;
            
              -- user interface
              cmd                   : in std_logic_vector(1 downto 0); -- command: read, write, nop
              cmd_vld               : in std_logic;
              addr                  : in std_logic_vector(U_ADDR_WIDTH-1 downto 0); -- ROW, BANK, COLUMN
              busy_q                : out std_logic; -- busy flag, when active commands are ignored
            
              -- Datenaustausch mit Controller
              init_finished         : in boolean;
              new_cmd_q             : out boolean;
              new_cmd_ack           : in boolean;
              do_prech_q            : out boolean; -- precharge followed by activate
              do_wait_q             : out boolean; -- additional delay requested
            
              cmd_q                 : out std_logic_vector(1 downto 0);
              addr_q                : out std_logic_vector(U_ADDR_WIDTH -1 downto 0));
    end component;
    
    -- Global Buffer BUFG
    component bufg
        port ( i : in std_ulogic;
               o : out std_ulogic );
    end component;

    -- synopsys translate_off
    for all: bufg use entity unisim.bufg;
    -- synopsys translate_on

    component DCM
        generic (CLKDV_DIVIDE          : real      := 2.0;
                 CLKFX_DIVIDE          : integer   := 4;                      
                 CLKFX_MULTIPLY        : integer   := 4;                      
                 CLKIN_DIVIDE_BY_2     : boolean   := false;                  
                 CLKIN_PERIOD          : real      := 0.0;                    
                 CLKOUT_PHASE_SHIFT    : string    := "NONE";                 
                 CLK_FEEDBACK          : string    := "1X";                   
                 DESKEW_ADJUST         : string    := "SYSTEM_SYNCHRONOUS";   
                 DFS_FREQUENCY_MODE    : string    := "LOW";                  
                 DLL_FREQUENCY_MODE    : string    := "LOW";                  
                 DSS_MODE              : string    := "NONE";                 
                 DUTY_CYCLE_CORRECTION : boolean   := true;                   
                 FACTORY_JF            : bit_vector:= X"C080";
                 PHASE_SHIFT           : integer   := 0;                      
                 STARTUP_WAIT          : boolean   := false);  --non-simulatable
        port (CLK0     : out std_ulogic                   := '0';
              CLK180   : out std_ulogic                   := '0';
              CLK270   : out std_ulogic                   := '0';
              CLK2X    : out std_ulogic                   := '0';
              CLK2X180 : out std_ulogic                   := '0';
              CLK90    : out std_ulogic                   := '0';
              CLKDV    : out std_ulogic                   := '0';
              CLKFX    : out std_ulogic                   := '0';
              CLKFX180 : out std_ulogic                   := '0';
              LOCKED   : out std_ulogic                   := '0';
              PSDONE   : out std_ulogic                   := '0';
              STATUS   : out std_logic_vector(7 downto 0) := "00000000";
              CLKFB    : in  std_ulogic                   := '0';
              CLKIN    : in  std_ulogic                   := '0';
              DSSEN    : in  std_ulogic                   := '0';
              PSCLK    : in  std_ulogic                   := '0';
              PSEN     : in  std_ulogic                   := '0';
              PSINCDEC : in  std_ulogic                   := '0';
              RST      : in  std_ulogic                   := '0');
    end component;
    
    -- synopsys translate_off
    for all: DCM use entity unisim.DCM;
    -- synopsys translate_on

    component fddrrse
        port (d0 : in  std_logic;
              d1 : in  std_logic;
              ce : in  std_logic;
              c0 : in  std_logic;
              c1 : in  std_logic;
              r  : in  std_logic;
              s  : in  std_logic;
              q  : out std_logic);
    end component;

    -- synopsys translate_off
    for all: fddrrse use entity unisim.fddrrse;
    -- synopsys translate_on

    signal ld_init_cnt_q : boolean;
    signal init_cnt0_q   : boolean;
    signal init_cnt1_q   : boolean;
    signal locked        : std_logic_vector(1 downto 0);
    signal data_lo1_q    : std_logic_vector(DDR_DATA_WIDTH-1 downto 0);
    signal data_lo2_q    : std_logic_vector(DDR_DATA_WIDTH-1 downto 0);
    signal data_hi_q     : std_logic_vector(DDR_DATA_WIDTH-1 downto 0);
    signal sys_clk       : std_logic;
    signal n_sys_clk     : std_logic;
    signal rst_int_n     : std_logic;
    signal wr_ena_q      : std_logic;
    signal wr_ena2_q     : std_logic;
    signal wr_ena3_q     : std_logic;
    signal wr_ena3_qn    : std_logic;
    signal dcm_rst_q     : std_logic;
    signal srst_sys_q    : std_logic;

    type RD_ENA_DEL_TYPE is array(4 downto 0) of boolean;
    signal rd_ena_del_q  : RD_ENA_DEL_TYPE;
    
    type MUX_TYPE is (NORMAL, DELAYED);
    signal mux_q : MUX_TYPE;    

begin 
    -----------------------------------------------------------------------
    -- Generate synchronous reset for DDR-SDRAM-Controller and DCM's
    -----------------------------------------------------------------------
    init: block
        signal rst_in : std_logic;
        signal srst_clk_q, srst_q : std_logic;
        signal sys_reset_q, sys_reset_1_q : std_logic;
        signal dcm1_locked_q, dcm2_locked_q : std_logic_vector(2 downto 0);
        signal cnt_q : unsigned(4 downto 0);
        type STATE_TYPE is (s0, s1, s2, s3, s4);
        signal state_q : STATE_TYPE;
    begin
        rst_in <= not rst_n;

        -- make synchronous reset 'srst_q' from asynchronous external 'rst_n'
        -- 'srst_q' is synchronous to clock 'clk'
        reset1: entity work.reset
            port map (clk    => clk,
                      rst_in => rst_in,
                      srst_q => srst_clk_q );

        -- make synchronous reset signal 'srst_sys_q' which is synchronous to
        -- clock 'sys_clk'
        reset2: entity work.reset
            port map (clk    => sys_clk,
                      rst_in => sys_reset_1_q,
                      srst_q => srst_q );

        srst_sys_q <= srst_q;

        process(clk)
        begin
            if rising_edge(clk) then
                case state_q is
                    when s0 =>
                        state_q <= s1;
                        sys_reset_q <= '1' after 1 ns;
                        dcm_rst_q <= '0';
                        cnt_q <= "11111";
                        
                    when s1 =>
                        cnt_q <= cnt_q - 1;
                        if cnt_q = 0 then
                            dcm_rst_q <= '1';
                            state_q <= s2;
                        end if;

                    when s2 =>
                        cnt_q <= cnt_q - 1;
                        if cnt_q = 0 then
                            dcm_rst_q <= '0';
                            state_q <= s3;
                        end if;

                    when s3 =>
                        if dcm1_locked_q(2)='1' and dcm2_locked_q(2)='1' then
                            state_q <= s4;
                            dcm_error_q <= '0';
                        end if;
                        
                    when s4 =>
                        sys_reset_q <= '0' after 1 ns;   -- release system reset
                        if dcm1_locked_q(2)='0' or dcm2_locked_q(2)='0' then
                            dcm_error_q <= '1';
                        end if;
                        
                end case;
                sys_reset_1_q <= sys_reset_q;

                -- synchronize 'dcm1_locked'
                dcm1_locked_q <= dcm1_locked_q(1 downto 0) & locked(0);
                dcm2_locked_q <= dcm2_locked_q(1 downto 0) & locked(1);
                
                if srst_clk_q='1' then
                    state_q <= s0;
                end if;
            end if;
        end process;
    end block;
    -------------------------------------------------------------------   

    -------------------------------------------------------------------------------------------------------
    -- DDR-SDRAM Control 
    -------------------------------------------------------------------------------------------------------
    ctrl: block 
        type STATE_TYPE is (IDLE, WAIT_200US, INIT1_PRECHARGE, INIT2_PRECHARGE,
                            INIT_REFRESH, INIT_EXT_MODE, INIT1_MODE, WAIT_DLL, WR_PRECH, RD_PRECH,
                            INIT2_MODE, ST_WAIT_CMD, ST_RFSH_0, ST_RFSH_1, ST_RFSH_2, ST_RFSH_3, ST_RFSH_4,
                            ST_READ1, ST_WRITE1, ST_WR_ACTIVATE, ST_WAIT_WR, ST_RD_ACTIVATE, ST_WAIT_RD);
        
        signal state_q : STATE_TYPE;

        signal sdr_if_q  : PART_IF_TYPE;
        signal sdr_if2_q : PART_IF_TYPE;
        signal ld_ct_cnt_q : boolean;
        signal ct0_q : boolean;
        signal second_refresh_q : boolean;
        signal ct_cnt_q : integer range 0 to 9;
        signal ld_dll_cnt_q : boolean;
        signal dll_cnt0_q : boolean;
        type TRISTATE_TYPE is array (0 to DDR_DATA_WIDTH-1) of boolean;
        signal tristate_q : TRISTATE_TYPE;
        signal dqsz_q    : std_logic_vector(DDR_DQS_WIDTH-1 downto 0);
        signal dqs       : std_logic_vector(DDR_DQS_WIDTH-1 downto 0);
        signal a_int_q   : std_logic_vector(DDR_ADDR_WIDTH-1 downto 0);  
        signal a_int2_q  : std_logic_vector(DDR_ADDR_WIDTH-1 downto 0);  
        signal cs_int_qn : std_logic;
        signal ba_int_q  : std_logic_vector(DDR_BANK_WIDTH-1 downto 0);
        signal ba_int2_q : std_logic_vector(DDR_BANK_WIDTH-1 downto 0);
        
        signal bus_switch_q : std_logic_vector(2 downto 0); -- delay for switching the data bus
        signal z_q : std_logic; -- Tristate signal, derived from bus_switch_q

        signal clk_int, n_clk_int, n_sys_clk270, sys_clk270, fpga_clk, fpga_clk270 : std_logic;

        signal rst : std_logic;

        signal cke_int_q  : std_logic;
        signal cke_int2_q : std_logic;

        signal d2sdr    : std_logic_vector(DDR_DATA_WIDTH-1 downto 0);
        signal ddr_data : std_logic_vector(U_DATA_WIDTH-1 downto 0);
        signal ddr_dm   : std_logic_vector(U_DM_WIDTH-1 downto 0);
        signal rd_ena_q : boolean;
        
        signal rst_qn   : std_logic;

        attribute preserve_signal : boolean;
        attribute preserve_signal of dqsz_q : signal is true;
        attribute preserve_signal of tristate_q : signal is true;
        attribute preserve_signal of z_q : signal is true;
        attribute preserve_signal of rst_qn : signal is true;
        
        signal init_finished_q : boolean;
        signal new_cmd_ack_q   : boolean;
        signal do_prech_q      : boolean;
        signal cmd_ui_q        : std_logic_vector(1 downto 0);
        signal addr_ui_q       : std_logic_vector(U_ADDR_WIDTH-1 downto 0);
        signal new_cmd_q       : boolean;
        
        -- Data Output
        type SHIFT_TYPE is array (1 downto 0) of std_logic_vector(U_DATA_WIDTH-1 downto 0);
        signal shift_q : SHIFT_TYPE;

        -- Dm Output
        type DM_SHIFT_TYPE is array (1 downto 0) of std_logic_vector(U_DM_WIDTH-1 downto 0);
        signal dm_shift_q : DM_SHIFT_TYPE;
        
        signal row_adr_q    : std_logic_vector(DDR_ADDR_WIDTH-1 downto 0);
        signal col_adr_q    : std_logic_vector(DDR_ADDR_WIDTH-1 downto 0);
        signal do_wait_q    : boolean;
        signal del_cnt_q    : integer range 0 to 7;
        signal del0_q       : boolean;
        signal ld_del_cnt_q : boolean;
        signal w_srg_q      : std_logic_vector(5 downto 0);
        signal tras_cnt_q   : integer range 0 to TRAS-1;
        
        signal clk133   : std_logic;
        signal clk133_o : std_logic;
        
        -- 200 us Counter (Initialization)
        function get_init_max return unsigned is
            variable max : unsigned(14 downto 0);
        begin
            max := "111111111111111"; -- >= 200 us
            -- pragma translate_off
            max := "000000000010100";  -- 20 for simulation only -> you cannot wait 200us!
            -- pragma translate_on
            return max;
        end ;

        -------------------------------------------------------------------------------------------------
        -- phase shift definition
        -- clkin_clkfb_skew = phase_shift/256 x period_clkin
        -------------------------------------------------------------------------------------------------
        function func_phase_shift return integer
        is
            variable ps : integer;
        begin
            ps := +30;   -- for synthesis only, values between 0 and +60  are OK for XILINX VirtexII Development Board
            -- and CESYS XV2DDR board
            -- pragma translate_off
            ps := +30;    -- for simulation only
            -- pragma translate_on
            return ps;
        end;

    begin
        
        rst_int_n  <= not srst_sys_q;
        sys_rst_qn <= not srst_sys_q;

        sys_clk_out <= sys_clk; -- for other modules inside the FPGA
        
        dcm0 : DCM
            generic map (CLK_FEEDBACK       => "1X",
                         CLKIN_PERIOD       => CLKIN_PERIOD,
                         CLKOUT_PHASE_SHIFT => "FIXED",
                         PHASE_SHIFT        => func_phase_shift)
            port map (clkin  => clk,
                      clkfb  => clk_fb,
                      clk0   => clk_int,
                      clkdv  => open,
                      clk2x  => open,
                      clk90  => open,
                      clk180 => open,
                      clk270 => open,
                      rst    => dcm_rst_q,
                      locked => locked(0));

        dcm1 : DCM
            generic map (CLK_FEEDBACK       => "1X",
                         CLKIN_PERIOD       => CLKIN_PERIOD,
                         CLKOUT_PHASE_SHIFT => "FIXED",
                         PHASE_SHIFT        => 0)            
            port map (clkin   => clk,
                      clkfb  => sys_clk,
                      clk0   => fpga_clk,
                      clkdv  => open,
                      clk2x  => open,
                      clk90  => open,
                      clk180 => open,
                      clk270 => fpga_clk270,
                      rst    => dcm_rst_q,
                      locked => locked(1));

        bufg11: bufg
            port map ( o => sys_clk, i => fpga_clk);

        bufg12: bufg
            port map ( o => sys_clk270, i => fpga_clk270);

        -------------------------------------------------------
        -- User-Interface 
        -------------------------------------------------------
        user_if_inst : user_if
            generic map (U_ADDR_WIDTH   => U_ADDR_WIDTH,
                         DDR_ADDR_WIDTH => DDR_ADDR_WIDTH,
                         DDR_BANK_WIDTH => DDR_BANK_WIDTH,
                         REFRESH_CLOCKS => REFRESH_CLOCKS,
                         REFRESH_WINDOW => REFRESH_WINDOW)
            port map (rst_int_n     => rst_int_n,
                      sys_clk       => sys_clk,
                      cmd           => cmd,
                      addr          => addr,
                      busy_q        => busy_q,
                      cmd_vld       => cmd_vld,
                      init_finished => init_finished_q,
                      new_cmd_q     => new_cmd_q,
                      new_cmd_ack   => new_cmd_ack_q,
                      do_prech_q    => do_prech_q,
                      do_wait_q     => do_wait_q,
                      cmd_q         => cmd_ui_q,
                      addr_q        => addr_ui_q);

        ct0_q <= ct_cnt_q=0;
        
        process (sys_clk, rst_int_n) is
            variable start_wr : boolean;
            variable start_rd : boolean;
        begin 
            if rst_int_n = '0' then  
                state_q       <= IDLE;
                sdr_if_q      <= COMMAND(NOP); 
                sdr_if2_q     <= COMMAND(NOP);
                ld_init_cnt_q <= false;
--                ct0_q         <= false;
                ct_cnt_q      <= 0;
                ld_ct_cnt_q   <= false;
                ld_dll_cnt_q  <= false;
                second_refresh_q <= false;

                a_int_q    <= (others => '0');
                a_int2_q   <= (others => '0');
                cs_int_qn  <= '1';
                ba_int_q   <= (others=>'0');
                ba_int2_q  <= (others=>'0');
                cke_int_q  <= '0';
                cke_int2_q <= '0';
                
                z_q          <= '0';
                bus_switch_q <= (others => '0');
                
                rd_ena_q     <= false;
                wr_ena_q     <= '0';
                wr_ena2_q    <= '0';
                wr_ena3_q    <= '0';
                del_cnt_q    <= 0;
                del0_q       <= true;
                ld_del_cnt_q <= false;
                w_srg_q      <= (others=>'0');
                mux_q        <= NORMAL;                
                shift_q(0)   <= (others=>'0');
                dm_shift_q(0)<= (others => '0');
                row_adr_q    <= (others=>'0');
                col_adr_q    <= (others=>'0');
                tras_cnt_q    <= 0;
                init_finished_q <= false;
                new_cmd_ack_q   <= false;
                
            elsif rising_edge(sys_clk) then
                
                -- Default's for variables
                start_wr := false;
                start_rd := false;
                
                -- default CMD
                sdr_if_q  <= COMMAND(NOP);
                a_int_q   <= (others => '0');
                cs_int_qn <= '1';

                ld_ct_cnt_q     <= false;
                ld_dll_cnt_q    <= false;
                ld_init_cnt_q   <= false;
                init_finished_q <= false;
                new_cmd_ack_q   <= false;
                ld_del_cnt_q    <= false;
                
                -- MAIN FSM to generate Controller commands and timing
                case state_q is

                    when IDLE =>
                        mux_q <= NORMAL;
                        if locked = "11" then
                            -- start of ddr-sdram initialization when dcms are locked
                            assert cke_int_q='0' report "cke_q error!" severity failure;
                            ld_init_cnt_q <= true;
                            state_q       <= WAIT_200us;
                        end if;
                        
                    when WAIT_200us =>
                        if init_cnt1_q then
                            cke_int_q <= '1';                     
                        end if;
                        
                        if init_cnt0_q and not ld_init_cnt_q then
                            ld_ct_cnt_q <= true;
                            sdr_if_q    <= COMMAND(PRECHARGE);
                            a_int_q(AUTO_PRECHARGE) <= '1';  -- precharge all
                            state_q     <= INIT1_PRECHARGE;
                        end if;

                    when INIT1_PRECHARGE =>
                        if ct0_q and not ld_ct_cnt_q then
                            sdr_if_q    <= COMMAND(EXT_MODE);  
                            a_int_q     <= EXT_MODE_REG;
                            ba_int_q    <= EXT_MODE_REG_BANK;
                            ld_ct_cnt_q <= true;
                            state_q     <= INIT_EXT_MODE;
                        end if;

                    when INIT_EXT_MODE =>
                        if ct0_q and not ld_ct_cnt_q then
                            -- set MODE Register
                            a_int_q     <= MODE_REG_DLL_RST;
                            sdr_if_q    <= COMMAND(MODE);
                            state_q     <= INIT1_MODE;
                            ba_int_q    <= BASE_MODE_REG_BANK;
                            ld_ct_cnt_q <= true;
                        end if;

                    when INIT1_MODE =>
                        if ct0_q and not ld_ct_cnt_q then
                            ld_dll_cnt_q <= true;
                            state_q      <= WAIT_DLL;
                        end if;

                    when WAIT_DLL =>
                        -- wait 200 clk cycles
                        if dll_cnt0_q then
                            state_q     <= INIT2_PRECHARGE;
                            sdr_if_q    <= COMMAND(PRECHARGE);
                            ld_ct_cnt_q <= true;
                            a_int_q(AUTO_PRECHARGE) <= '1';  -- precharge all
                        end if;

                    when INIT2_PRECHARGE =>
                        if ct0_q and not ld_ct_cnt_q then
                            sdr_if_q    <= COMMAND(REFRESH);
                            ld_ct_cnt_q <= true;
                            state_q     <= INIT_REFRESH;
                        end if;
                        
                    when INIT_REFRESH =>
                        if ct0_q and not ld_ct_cnt_q then
                            sdr_if_q         <= COMMAND(REFRESH);
                            ld_ct_cnt_q      <= true;
                            second_refresh_q <= true;
                            
                            if second_refresh_q then
                                state_q     <= INIT2_MODE;
                                sdr_if_q    <= COMMAND(MODE);
                                ld_ct_cnt_q <= true;
                                -- set MODE Reg again (DLL enabled)
                                a_int_q  <= MODE_REG_DLL_ENA;
                                ba_int_q <= BASE_MODE_REG_BANK;
                            end if;
                        end if;
                        
                    when INIT2_MODE =>
                        if ct0_q and not ld_ct_cnt_q then
                            state_q         <= ST_WAIT_CMD;
                            init_finished_q <= true;
                        end if; -- End of Initialization
                        
                        -- Main State for COMMAND processing ------------------------------------------------------
                    when ST_WAIT_CMD =>
                        ba_int_q <= addr_ui_q(addr_ui_q'LEFT-DDR_ADDR_WIDTH  downto addr_ui_q'LEFT -DDR_ADDR_WIDTH - (DDR_BANK_WIDTH-1));

                        -- Store Row and Column address as long as the command is processed
                        row_adr_q <= addr_ui_q(addr_ui_q'LEFT downto addr_ui_q'LEFT -DDR_ADDR_WIDTH+1); 
                        col_adr_q <= addr_ui_q(addr_ui_q'RIGHT+DDR_ADDR_WIDTH-1 downto 0);
                        
                        if new_cmd_q then

                            if do_wait_q and w_srg_q(0)='1'then -- Read2Write or Write2Read
                                null;
                            else
                                -- Acknowledge the command immediately
                                new_cmd_ack_q <= true;
                                
                                case cmd_ui_q is
                                    
                                    when NOP_CMD =>
                                        null;
                                        
                                    when RD_CMD => -- READ -------------------------
                                        mux_q     <= NORMAL;
                                        
                                        if do_prech_q then
                                            state_q <= RD_PRECH;
                                        else
                                            start_rd  := true;
                                            a_int_q   <= addr_ui_q(addr_ui_q'RIGHT+DDR_ADDR_WIDTH-1 downto 0) ; -- COL
                                            sdr_if_q  <= COMMAND(BURST_RD); -- Kommando setzen
                                            state_q   <= ST_READ1;
                                        end if;
                                        
                                    when WR_CMD => -- WRITE ------------------------
                                        mux_q <= DELAYED;
                                        
                                        if do_prech_q then
                                            state_q     <= WR_PRECH;
                                        else
                                            start_wr := true;
                                            -- hier direkt von addr_ui_q laden
                                            a_int_q     <= addr_ui_q(addr_ui_q'RIGHT+DDR_ADDR_WIDTH-1 downto 0) ; -- COL
                                            sdr_if_q    <= COMMAND(BURST_WR); -- Kommando setzen
                                            state_q     <= ST_WRITE1;
                                        end if;
                                        
                                    when RFSH_CMD => -- FORCED REFRESH -------------------
                                        mux_q          <= NORMAL;
                                        state_q        <= ST_RFSH_0;
                                        ld_del_cnt_q   <= true; -- wait some clocks before Refresh
                                        
                                    when others =>
                                        assert FALSE report "Wrong Command !" severity error;
                                end case;
                            end if;
                            
                        end if;
                        
                        -- in allen States nach WAIT_CMD die Adresse aus den Registern row_adr und col_adr laden
                    when ST_WRITE1 =>
                        state_q <= ST_WAIT_CMD;
                        
                    when WR_PRECH =>
                        sdr_if_q    <= COMMAND(PRECHARGE);
                        ld_ct_cnt_q <= true;
                        state_q     <= ST_WR_ACTIVATE;
                        a_int_q(AUTO_PRECHARGE) <= '1';  -- precharge all

                    when ST_WR_ACTIVATE =>
                        if ct0_q and not ld_ct_cnt_q then -- Counter direkt abfragen, damit min. Delay erreicht wird
                            sdr_if_q    <= COMMAND(ACTIVE);
                            a_int_q     <= row_adr_q; -- set ROW address
                            ld_ct_cnt_q <= true;
                            state_q     <= ST_WAIT_WR;
                        end if;
                        
                    when ST_WAIT_WR =>
                        if ct0_q and not ld_ct_cnt_q then
                            start_wr := true;
                            a_int_q  <= col_adr_q; -- set COL address  
                            sdr_if_q <= COMMAND(BURST_WR); -- Kommando setzen
                            state_q  <= ST_WRITE1;
                        end if;

                        -- Auto Refresh
                        -- Do a precharge all, followed by refresh and by an activate for last ROW
                    when ST_RFSH_0 =>
                        a_int_q(AUTO_PRECHARGE) <= '1';  -- precharge all, bank-bits are don't care
                        if del0_q then
                            -- All banks must be precharged before Refresh
                            sdr_if_q     <= COMMAND(PRECHARGE);
                            ld_ct_cnt_q  <= true;
                            state_q      <= ST_RFSH_1;
                        end if;

                    when ST_RFSH_1 =>
                        if ct0_q and not ld_ct_cnt_q then
                            state_q      <= ST_RFSH_2;
                            sdr_if_q     <= COMMAND(REFRESH);
                            ld_ct_cnt_q  <= true;
                        end if;
                        
                    when ST_RFSH_2 =>
                        if ct0_q and not ld_ct_cnt_q then
                            ld_ct_cnt_q <= true;
                            a_int_q     <= row_adr_q; -- set current ROW address
                            sdr_if_q    <= COMMAND(ACTIVE);
                            -- Active to RD/WR or Active to Precharge?
                            if do_prech_q then
                                tras_cnt_q <= TRAS-1;
                                state_q   <= ST_RFSH_4;
                            else
                                state_q   <= ST_RFSH_3;
                            end if;
                        end if;
                        
                    when ST_RFSH_3 =>
                        if ct0_q and not ld_ct_cnt_q then
                            state_q    <= ST_WAIT_CMD;
                        end if;
                        
                    when ST_RFSH_4 =>
                        if tras_cnt_q = 0 then
                            state_q <= ST_WAIT_CMD;
                        else
                            tras_cnt_q <= tras_cnt_q -1;
                        end if;
                        
                    when RD_PRECH => 
                        sdr_if_q    <= COMMAND(PRECHARGE);
                        ld_ct_cnt_q <= true;
                        state_q     <= ST_RD_ACTIVATE;
                        a_int_q(AUTO_PRECHARGE) <= '1';  -- precharge all

                    when ST_READ1 =>
                        state_q <= ST_WAIT_CMD;
                        
                    when ST_RD_ACTIVATE =>
                        if ct0_q and not ld_ct_cnt_q then
                            ld_ct_cnt_q <= true;
                            sdr_if_q <= COMMAND(ACTIVE);
                            a_int_q  <= row_adr_q;  -- ROW address
                            state_q <= ST_WAIT_RD;                                             
                        end if;
                        
                    when ST_WAIT_RD =>
                        if ct0_q and not ld_ct_cnt_q then
                            start_rd := true;
                            a_int_q       <= col_adr_q;  -- Col Address
                            sdr_if_q      <= COMMAND(BURST_RD); -- set burst read command
                            state_q <= ST_READ1;
                        end if;
                end case;
                
                sdr_if2_q  <= sdr_if_q;
                ba_int2_q  <= ba_int_q;
                a_int2_q   <= a_int_q;
                cke_int2_q <= cke_int_q;
                
                -- Write Control Signals
                if start_wr then 
                    wr_ena_q    <= '1';
                else
                    wr_ena_q    <= '0';
                end if;
                
                wr_ena2_q    <= wr_ena_q;
                wr_ena3_q    <= wr_ena2_q;
                
                -- Data Input Shiftreg 
                if wr_ena2_q='1' then
                    shift_q(0)    <= data_in;  -- 2 clocks delayed MUX signal
                    dm_shift_q(0) <= dm_in;
                end if;
                
                -- Read Control Signals
                if start_rd then
                    rd_ena_q <= true;
                    bus_switch_q(0) <= '1';
                else
                    rd_ena_q <= false;
                    bus_switch_q(0) <= '0';
                end if;
                -- On and off-switching for Tristate buffer 
                -- is controlled by bus_switch_q
                bus_switch_q(bus_switch_q'LEFT downto 1) <= bus_switch_q(bus_switch_q'LEFT-1 downto 0);
                z_q  <= bus_switch_q(bus_switch_q'LEFT) or bus_switch_q(bus_switch_q'LEFT-1); 
                ----------------------------------------------------
                
                -- cycle time counter
                if ld_ct_cnt_q then
                    ct_cnt_q <= sdr_if_q.ct;
                elsif ct_cnt_q /= 0 then
                    ct_cnt_q <= ct_cnt_q -1;
                end if;

                --ct0_q <= ct_cnt_q=0;
                
                -- delay time counter
                if ld_del_cnt_q then
                    del_cnt_q <= 3;
                elsif del_cnt_q /= 0 then
                    del_cnt_q <= del_cnt_q -1;
                end if;
                del0_q <= del_cnt_q=1;
                
                -- Wait time shift register for Read2Write and Write2Read delay
                -- Note that there is 1 more clock needed for a change from WRITE to READ
                -- due to pipeline delay, compared to READ to WRITE
                if start_rd or start_wr then 
                    w_srg_q <= "000111";
                else
                    w_srg_q <= '0' & w_srg_q(w_srg_q'LEFT downto 1);
                end if;

            end if;
        end process;

        process(sys_clk, rst_int_n)
        begin
            if rst_int_n = '0' then
                shift_q(1)    <= (others => '0');
                dm_shift_q(1) <= (others => '0');
            elsif falling_edge(sys_clk) then
                shift_q(1)    <= shift_q(0);
                dm_shift_q(1) <= dm_shift_q(0);
            end if;
        end process;
        
        -- Create iob FFs without reset
        process (sys_clk) is
        begin
            if rising_edge(sys_clk) then  
                for n in 0 to DDR_DQS_WIDTH-1 loop
                    if wr_ena2_q='1' or wr_ena3_q='1' then -- ML: verlaengert + noch 1 clock
                        dqsz_q(n) <= '0';
                    else
                        dqsz_q(n) <= '1';
                    end if;
                end loop;  
            end if;
        end process;

        process (sys_clk270)
        begin  
            if rising_edge(sys_clk270) then  
                for n in 0 to DDR_DATA_WIDTH-1 loop
                    tristate_q(n) <= z_q='1';
                end loop;
            end if;
        end process ;

        --------------------------------------------------------------------------------------------------
        -- ctrl signals on negative edge, all FFs without Async Reset
        --------------------------------------------------------------------------------------------------
        process (sys_clk) is
        begin  
            if falling_edge(sys_clk) then
                case mux_q is
                    when NORMAL => -- Read Command
                        ras_qn       <= sdr_if_q.ras_n;
                        cas_qn       <= sdr_if_q.cas_n; 
                        we_qn        <= sdr_if_q.we_n; 
                        cs_qn        <= sdr_if_q.cs_n; 
                        ba_q         <= ba_int_q; 
                        a_q          <= a_int_q;
                        cke_q        <= cke_int_q;
                    when DELAYED => -- Write Command
                        ras_qn       <= sdr_if2_q.ras_n;
                        cas_qn       <= sdr_if2_q.cas_n; 
                        we_qn        <= sdr_if2_q.we_n; 
                        cs_qn        <= sdr_if2_q.cs_n; 
                        ba_q         <= ba_int2_q; 
                        a_q          <= a_int2_q;
                        cke_q        <= cke_int2_q;
                end case;
            end if;
        end process;

        -- Data MSBs, e.g. (31 downto 16)  
        ddr_data(U_DATA_WIDTH-1 downto U_DATA_WIDTH/2) <= shift_q(0)(U_DATA_WIDTH/2-1 downto 0);
        -- Data LSBs, e.g. (15 downto 0)  
        ddr_data(U_DATA_WIDTH/2-1 downto 0)            <= shift_q(1)(U_DATA_WIDTH-1 downto U_DATA_WIDTH/2);

        -- DM MSBs i.e. (3 downto 2)
        ddr_dm(U_DM_WIDTH-1 downto U_DM_WIDTH/2) <= dm_shift_q(0)(U_DM_WIDTH/2-1 downto 0);
        -- DM LSBs i.e. (1 downto 0)
        ddr_dm(U_DM_WIDTH/2-1 downto 0)		 <= dm_shift_q(1)(U_DM_WIDTH-1 downto U_DM_WIDTH/2);
        
        --------------------------------------------------------------------------------------------------
        -- DDR-FFs and Tristate Buffers
        --------------------------------------------------------------------------------------------------
        n_clk_int    <= not clk_int;
        n_sys_clk270 <= not sys_clk270;
        n_sys_clk    <= not sys_clk;
        
        -- SDRAM Clock
        fddr1: fddrrse
            port map (d0 => '0',
                      d1 => '1',
                      ce => '1',
                      c0 => n_clk_int,
                      c1 => clk_int,
                      r  => '0',
                      s  => '0',
                      q  => sdr_clk);
        fddr2: fddrrse
            port map (d0 => '1',
                      d1 => '0',
                      ce => '1',
                      c0 => n_clk_int,
                      c1 => clk_int,
                      r  => '0',
                      s  => '0',
                      q  => sdr_clk_n);
        
        -- Data OUT FFs
        gen_d1: for n in 0 to DDR_DATA_WIDTH-1 generate
        begin 
            fddr3 : fddrrse
                port map( d0 => ddr_data(n),
                          d1 => ddr_data(n + DDR_DATA_WIDTH),
                          ce => '1',
                          c0 => n_sys_clk270,
                          c1 => sys_clk270,
                          r  => '0',
                          s  => '0',
                          q  => d2sdr(n));
        end generate gen_d1;

        -- DM OUT FFs
        gen_dm: for n in 0 to DDR_DM_WIDTH-1 generate
        begin 
            fddr_dm3 : fddrrse
                port map( d0 => ddr_dm(n),
                          d1 => ddr_dm(n + DDR_DM_WIDTH),
                          ce => '1',
                          c0 => n_sys_clk270,
                          c1 => sys_clk270,
                          r  => '0',
                          s  => '0',
                          q  => dm_q(n));
        end generate gen_dm;

        -- DQS OUT FFs
        gen_d2: for n in 0 to DDR_DQS_WIDTH-1 generate
        begin 
            fddr4 : fddrrse
                port map( d0 => '0',
                          d1 => '1',
                          ce => '1',
                          c0 => n_sys_clk,
                          c1 => sys_clk,
                          r  => wr_ena3_qn,
                          s  => '0',
                          q  => dqs(n));
        end generate gen_d2;
        
        -- Usage of DM signals not supported in this version
        -- dm_q <= (others=>'0');
        
        gen3: for n in 0 to DDR_DATA_WIDTH-1 generate
            data(n) <= 'Z' when tristate_q(n) else d2sdr(n);
        end generate;

        wr_ena3_qn <= not wr_ena3_q;
        
        -- Tristate-Buffer fuer dqs_q      
        gen_dqs : for n in 0 to DDR_DQS_WIDTH-1 generate
            dqs_q(n) <= 'Z' when dqsz_q(n)='1' else dqs(n);
        end generate gen_dqs;

        --------------------------------------------------------------------------------------------------
        -- READ DATA Processing
        --------------------------------------------------------------------------------------------------
        process (sys_clk)
        begin  
            if rising_edge(sys_clk) then
                
                -- sample HI-data word with rising edge
                data_hi_q <= data;
                
                -- store HI- und LO- data word  in 32bit output register
                data_out_q <= data_hi_q & data_lo2_q;
                
            end if;
        end process;
        
        process(sys_clk)
        begin
            if rising_edge(sys_clk) then
                -- derive window for valid data from rd_ena_q
                rd_ena_del_q <= rd_ena_del_q(rd_ena_del_q'LEFT-1 downto 0) & rd_ena_q;
            end if;
        end process;

        data_vld_q <= '1' when rd_ena_del_q(rd_ena_del_q'LEFT) else '0';
        
        process (sys_clk)
        begin  
            if falling_edge(sys_clk) then
                
                -- sample LO- word with falling edge
                data_lo1_q <= data;
                
                -- 1 clock additional delay to store HI- and LO-word
                -- with the next rising edge as 32bit word
                data_lo2_q <= data_lo1_q;
            end if;
        end process;

        -----------------------------------------------      
        -- Write Data Processing
        -----------------------------------------------      
        data_req_q <= '1' when wr_ena_q='1' else '0';
        
        ------------------------------------------------------------------------------
        -- wait counter for first 200 us
        ------------------------------------------------------------------------------
        init_cnt: block
            signal init_cnt_q : unsigned (0 to 13); 
        begin
            process (sys_clk, rst_int_n) is
            begin 
                if rst_int_n = '0' then  
                    init_cnt_q <= (others => '0');
                    init_cnt0_q <= false;
                    init_cnt1_q <= false;
                elsif rising_edge(sys_clk) then
                    
                    init_cnt0_q <= init_cnt_q=0;
                    init_cnt1_q <= init_cnt_q=1;
                    
                    if ld_init_cnt_q then
                        init_cnt_q <= conv_unsigned(get_init_max -1, init_cnt_q'length);
                        init_cnt0_q <= false;  
                        init_cnt1_q <= false;  
                    elsif init_cnt_q /= 0 then
                        init_cnt_q <= init_cnt_q -1;
                    end if;
                    
                end if;
            end process;
        end block init_cnt;

        ------------------------------------------------------------------------------
        -- wait counter for 200 clk cycles for dll
        ------------------------------------------------------------------------------
        dll_cnt: block
            constant DLL_DELAY : integer := 200 -1;
            signal dll_cnt_q : integer range 0 to DLL_DELAY;
        begin
            process (sys_clk, rst_int_n) is
            begin 
                if rst_int_n='0' then
                    dll_cnt0_q <= false;
                    dll_cnt_q  <= DLL_DELAY;
                elsif rising_edge(sys_clk) then  
                    if ld_dll_cnt_q then
                        dll_cnt_q  <= DLL_DELAY;
                    elsif dll_cnt_q > 0 then
                        dll_cnt_q <= dll_cnt_q -1;
                    end if;
                    dll_cnt0_q <= dll_cnt_q = 1;
                    
                end if;
            end process;
        end block dll_cnt;
    end block ctrl;

end architecture behave;

library ieee, work;
use ieee.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;
use work.ddr_sdr_conf_pkg.all;

entity wb_ddr is
    generic (WIDTH          : integer := 32;
             ADDR_WIDTH     : integer := 32;
             FREQ_KHZ       : positive := 50_000;  -- frequency in KHz 
             DDR_DM_WIDTH   : positive := 2;    --  (DDR_DATA_WIDTH/8)                                                 
             DDR_DQS_WIDTH  : positive := 2;    --  (DDR_DATA_WIDTH/8)                                                 
             DDR_DATA_WIDTH : positive := 16;   -- External DDR-SDRAM Module data bus width                           
             DDR_ADDR_WIDTH : positive := 13;   -- number of address lines to DDR-SDRAM Device/Module                 
             DDR_BANK_WIDTH : positive := 2;    -- Number of BANK address lines of external DDR-SDRAM                 
             AUTO_PRECHARGE : positive := 10);  -- bit-position in column address for auto precharge (see Data Sheet);

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

          -- DDR SDRAM Signals
          sdr_clk   : out   std_logic;  -- ddr_sdram_clock
          sdr_clk_n : out   std_logic;  -- /ddr_sdram_clock
          clk_fb    : in    std_logic;  -- clock feed back
          cke_q     : out   std_logic;  -- clock enable
          cs_qn     : out   std_logic;  -- /chip select
          ras_qn    : out   std_logic;  -- /ras
          cas_qn    : out   std_logic;  -- /cas
          we_qn     : out   std_logic;  -- /write enable
          dm_q      : out   std_logic_vector(DDR_DM_WIDTH-1 downto 0);  -- data mask bits, set to "00"
          dqs_q     : out   std_logic_vector(DDR_DQS_WIDTH-1 downto 0);  -- data strobe, only for write
          ba_q      : out   std_logic_vector(DDR_BANK_WIDTH-1 downto 0);  -- bank select
          a_q       : out   std_logic_vector(DDR_ADDR_WIDTH-1 downto 0);  -- address bus 
          data      : inout std_logic_vector(DDR_DATA_WIDTH-1 downto 0));  -- bidir data bus

end wb_ddr;

architecture rtl of wb_ddr is

    constant U_ADDR_WIDTH : positive := (2 * DDR_ADDR_WIDTH) + DDR_BANK_WIDTH;
    constant U_DATA_WIDTH : positive := (2 * DDR_DATA_WIDTH);
    constant U_DM_WIDTH   : positive := 2 * DDR_DM_WIDTH;
    
    component ddr_sdr
        generic (FREQ_KHZ       : positive;
                 U_ADDR_WIDTH   : positive;
                 U_DATA_WIDTH   : positive;
                 U_DM_WIDTH     : positive;
                 DDR_DM_WIDTH   : positive;
                 DDR_DQS_WIDTH  : positive;
                 DDR_DATA_WIDTH : positive;
                 DDR_ADDR_WIDTH : positive;
                 DDR_BANK_WIDTH : positive;
                 AUTO_PRECHARGE : positive);
        port (rst_n       : in    std_logic;
              clk         : in    std_logic;
              sys_rst_qn  : out   std_logic;
              sys_clk_out : out   std_logic;
              clk_fb      : in    std_logic;
              cmd         : in    std_logic_vector(1 downto 0);
              cmd_vld     : in    std_logic;
              addr        : in    std_logic_vector(U_ADDR_WIDTH-1 downto 0);
              busy_q      : out   std_logic;
              data_in     : in    std_logic_vector(U_DATA_WIDTH-1 downto 0);
              dm_in       : in    std_logic_vector(U_DM_WIDTH-1 downto 0);
              data_req_q  : out   std_logic;
              data_out_q  : out   std_logic_vector(U_DATA_WIDTH -1 downto 0);
              data_vld_q  : out   std_logic;
              sdr_clk     : out   std_logic;
              sdr_clk_n   : out   std_logic;
              cke_q       : out   std_logic;
              cs_qn       : out   std_logic;
              ras_qn      : out   std_logic;
              cas_qn      : out   std_logic;
              we_qn       : out   std_logic;
              dm_q        : out   std_logic_vector(DDR_DM_WIDTH-1 downto 0);
              dqs_q       : out   std_logic_vector(DDR_DQS_WIDTH-1 downto 0);
              ba_q        : out   std_logic_vector(DDR_BANK_WIDTH-1 downto 0);
              a_q         : out   std_logic_vector(DDR_ADDR_WIDTH-1 downto 0);
              data        : inout std_logic_vector(DDR_DATA_WIDTH-1 downto 0);
              dcm_error_q : out   std_logic);
    end component;

    signal sdr_clk_tmp : std_logic := '0';
    signal rst_n       : std_logic;
    signal sys_rst_qn  : std_logic;
    signal sys_clk_out : std_logic;
    signal cmd         : std_logic_vector(1 downto 0);
    signal cmd_vld     : std_logic;
    signal addr        : std_logic_vector(U_ADDR_WIDTH-1 downto 0);
    signal busy_q      : std_logic;
    signal data_in     : std_logic_vector(U_DATA_WIDTH-1 downto 0);
    signal dm_in       : std_logic_vector(U_DM_WIDTH-1 downto 0);
    signal data_req_q  : std_logic;
    signal data_out_q  : std_logic_vector(U_DATA_WIDTH -1 downto 0);
    signal data_vld_q  : std_logic;
    signal dcm_error_q : std_logic;
    
begin  -- rtl

    rst_n <= not reset;
    
    ddr_sdr_inst : ddr_sdr
        generic map (FREQ_KHZ       => FREQ_KHZ,
                     U_ADDR_WIDTH   => U_ADDR_WIDTH,
                     U_DATA_WIDTH   => U_DATA_WIDTH,
                     U_DM_WIDTH     => U_DM_WIDTH,
                     DDR_DM_WIDTH   => DDR_DM_WIDTH,
                     DDR_DQS_WIDTH  => DDR_DQS_WIDTH,
                     DDR_DATA_WIDTH => DDR_DATA_WIDTH,
                     DDR_ADDR_WIDTH => DDR_ADDR_WIDTH,
                     DDR_BANK_WIDTH => DDR_BANK_WIDTH,
                     AUTO_PRECHARGE => AUTO_PRECHARGE)
        port map (rst_n       => rst_n,
                  clk         => clk,
                  sys_rst_qn  => sys_rst_qn,
                  sys_clk_out => sys_clk_out,
                  clk_fb      => clk_fb,
                  cmd         => cmd,
                  cmd_vld     => cmd_vld,
                  addr        => addr,
                  busy_q      => busy_q,
                  data_in     => data_in,
                  dm_in       => dm_in,
                  data_req_q  => data_req_q,
                  data_out_q  => data_out_q,
                  data_vld_q  => data_vld_q,
                  sdr_clk     => sdr_clk_tmp,
                  sdr_clk_n   => sdr_clk_n,
                  cke_q       => cke_q,
                  cs_qn       => cs_qn,
                  ras_qn      => ras_qn,
                  cas_qn      => cas_qn,
                  we_qn       => we_qn,
                  dm_q        => dm_q,
                  dqs_q       => dqs_q,
                  ba_q        => ba_q,
                  a_q         => a_q,
                  data        => data,
                  dcm_error_q => dcm_error_q);
    
    -- purpose: Block contains the WishBone statemachine
    wb_block : block
        type wb_states is (wb_idle, wb_read_start, wb_read, wb_write_start, wb_write, wb_wait_end);

        signal wb_nstate, wb_cstate : wb_states                     := wb_idle;
        signal ack_o                : std_logic                     := '0';
        signal wdelay               : std_logic_vector (2 downto 0) := (others => '0');
        
    begin  -- block wb_block

        process (clk, reset)
        begin
            if reset = '1' then
                wb_cstate <= wb_idle;
            elsif rising_edge(clk) then
                wb_cstate <= wb_nstate;
            end if;

            -- delay data_req_q by 2 cycles
            if reset = '1' then
                wdelay <= "000";
            elsif rising_edge(clk) then
                wdelay <= wdelay(1 downto 0) & data_req_q;
            end if;
        end process;

        -- the actual state machine
        process (wb_cstate, busy_q, wdelay, data_vld_q, WBS_STB_I, WBS_WE_I)
        begin
            wb_nstate <= wb_cstate;
            ack_o     <= '0';
            cmd       <= NOP_CMD;
            cmd_vld   <= '0' after 1 ns;

            case wb_cstate is

                when wb_idle =>
                    if busy_q = '0' then
                        if WBS_STB_I = '1' then
                            if WBS_WE_I = '0' then
                                cmd       <= RD_CMD;
                                cmd_vld   <= '1' after 1 ns;
                                wb_nstate <= wb_read_start;
                            else
                                cmd       <= WR_CMD;
                                cmd_vld   <= '1' after 1 ns;
                                wb_nstate <= wb_write_start;
                            end if;
                        end if;
                    end if;

                when wb_read_start =>
                    -- wait for command to register
                    if busy_q = '1' then
                        wb_nstate <= wb_read;
                    else
                        cmd <= RD_CMD;
                        cmd_vld <= '1';
                    end if;
                    
                when wb_read =>
                    -- wait for it to complete
                    if data_vld_q = '1' then
                        ack_o     <= '1';
                        wb_nstate <= wb_wait_end;
                    end if;

                when wb_write_start =>
                    -- wait for the command to register
                    if busy_q = '1' then
                        wb_nstate <= wb_write;
                    else                            
                        cmd     <= WR_CMD;
                        cmd_vld <= '1' after 1 ns;
                        wb_nstate <= wb_write;
                    end if;

                when wb_write   =>
                    if wdelay(2) = '1' then
                        ack_o <= '1';
                        wb_nstate <= wb_wait_end;                        
                    end if;
                    
                when wb_wait_end =>
                    -- wait for it to complete                    
                    if busy_q = '0' then
                        wb_nstate <= wb_idle;
                    end if;

                when others => null;
            end case;
        end process;

        sdr_clk   <= sdr_clk_tmp;
        addr      <= WBS_ADR_I(U_ADDR_WIDTH-5 downto 9) & "0000" & WBS_ADR_I(8 downto 2) & "00";
        data_in   <= WBS_DAT_I;
        WBS_DAT_O <= data_out_q;
        WBS_ACK_O <= ack_o;
        WBS_ERR_O <= '0';
        dm_in     <= not WBS_SEL_I;

        
    end block wb_block;
end rtl;
