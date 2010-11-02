-------------------------------------------------------------------------------
-- Title      : UnifiedData & Instruction cache
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : ucache.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2003-03-22
-- Last update: 2006-10-12
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Unfified Data & Instruction cache. Implemented using Block rams
--              PortA of blockrams used for data (read/write) Port B used for
--              instruction fetch.
--              The data will flow through the cache even if uncacheable region
--              is accessed, or if bypass cache is set. The cache is used destructively
-------------------------------------------------------------------------------
-- Copyright (c) 2003 
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library UNISIM;
use UNISIM.vcomponents.all;

use work.manikconfig.all;
use work.manikpackage.all;

entity ucache is
    
    generic (WIDTH             : integer := 32;
             ICACHE_ENABLED    : boolean;
             DCACHE_ENABLED    : boolean;
             ICACHE_LINE_WORDS : integer;
             DCACHE_LINE_WORDS : integer;
             ICACHE_SETS       : integer;
             DCACHE_SETS       : integer;
             ICACHE_ADDR_WIDTH : integer;
             DCACHE_ADDR_WIDTH : integer);

    port (daddr         : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
          data_in       : in  std_logic_vector (WIDTH-1 downto 0);
          EXM_data_in   : in  std_logic_vector (WIDTH-1 downto 0);
          EXM_rd_done   : in  std_logic;
          EXM_wr_done   : in  std_logic;
          EXM_buserr    : in  std_logic;
          benA          : in  std_logic_vector (3 downto 0);
          EX_load       : in  std_logic;
          EX_store      : in  std_logic;
          EX_mispred    : in  std_logic;
          RF_lsu        : in  std_logic;
          RF_load	: in  std_logic;
          clk           : in  std_logic;
          iaddr         : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
          IAGU_ifclr    : in  std_logic;
          IAGU_Nxif     : in  std_logic;
          IAGU_syncif   : in  std_logic;
          IAGU_pc       : in  std_logic_vector (ADDR_WIDTH-1 downto 0);

          SFR_bdflag    : in  std_logic;
          SFR_iiflag    : in  std_logic;
          
          INTR_pend     : in  std_logic;
          INTR_reset    : in  std_logic;
          
          PCT_stall     : in  std_logic;
          
          EXM_data_out  : out std_logic_vector (WIDTH-1 downto 0);
          EXM_ben       : out std_logic_vector (3 downto 0);
          EXM_rd        : out std_logic;
          EXM_wr        : out std_logic;
          EXM_rwreq     : out std_logic;
          EXM_daddr     : out std_logic_vector (ADDR_WIDTH-1 downto 0);

          MEM_dfetch_ip : out std_logic;
          MEM_ifetch_ip : out std_logic;
          MEM_id_buserr : out std_logic_vector(1 downto 0);
          ld_done       : out std_logic;
          st_done       : out std_logic;
          MEM_Nbusy     : out std_logic;
          data_out      : out std_logic_vector (WIDTH-1 downto 0);
          ni_get_done   : out std_logic;
          instr         : out std_logic_vector (INST_WIDTH-1 downto 0));

end ucache;

architecture RTL of ucache is

    -- purpose: determine number of words per cache line
    function cwpl (cache_enb : boolean; cache_wpl : integer)
        return integer is
    begin  -- cwpl
        if cache_enb then
            return cache_wpl;
        end if;
        return 1;        
    end cwpl;

    -- purpose find out the tag width
    function tagw (width       : integer;
                   addr_width  : integer;
                   line_words  : integer) return integer is
        -- don't store highest order 2 bits
        -- and two of the lowest order bits
        variable rv : integer := width - addr_width - 5;
    begin
        assert line_words = 1 or line_words = 2 or line_words = 4 or line_words = 8
            report "Words per cache line is incorrect"
            severity failure;
        return rv;
    end tagw;

    -- caw determine the cache address width
    function caw (width  : integer; line_words : integer) return integer is
        variable rv : integer := width;
    begin
        assert line_words = 1 or line_words = 2 or line_words = 4 or line_words = 8
            report "Words per cache line is incorrect"
            severity failure;
        if line_words = 2 then
            return rv - 1;
        elsif line_words = 4 then
            return rv - 2;
        elsif line_words = 8 then
            return rv - 3;
        end if;                
        return rv;
    end caw;
        
    component dinmux
        generic (WIDTH : integer);
        port (ben : in  std_logic_vector (3 downto 0);
              din : in  std_logic_vector (WIDTH-1 downto 0);
              d0  : out std_logic_vector ((WIDTH/4)-1 downto 0);
              d1  : out std_logic_vector ((WIDTH/4)-1 downto 0);
              d2  : out std_logic_vector ((WIDTH/4)-1 downto 0);
              d3  : out std_logic_vector ((WIDTH/4)-1 downto 0));
    end component;

    component doutmux
        generic (WIDTH : integer);
        port (ben  : in  std_logic_vector (3 downto 0);
              d0   : in  std_logic_vector ((WIDTH/4)-1 downto 0);
              d1   : in  std_logic_vector ((WIDTH/4)-1 downto 0);
              d2   : in  std_logic_vector ((WIDTH/4)-1 downto 0);
              d3   : in  std_logic_vector ((WIDTH/4)-1 downto 0);
              dout : out std_logic_vector (WIDTH-1 downto 0));
    end component;

    constant DCACHE_WPL : integer := cwpl(DCACHE_ENABLED,DCACHE_LINE_WORDS);
    constant ICACHE_WPL : integer := cwpl(ICACHE_ENABLED,ICACHE_LINE_WORDS);
    
    signal enb        : std_logic := '0';
    signal Nstall     : std_logic := '0';
    signal ienb       : std_logic := '0';
    signal denb       : std_logic := '0';
    signal irst       : std_logic := '0';
    signal drst       : std_logic := '0';
    signal icache_hit : std_logic := '0';
    signal dcache_hit : std_logic := '0';
    signal df_done    : std_logic := '0';
    signal if_done    : std_logic := '0';
    signal dtag_cmp   : std_logic := '0';
    signal itag_cmp   : std_logic := '0';
    signal if_ip      : std_logic := '0';
    signal dl_ip      : std_logic := '0';
    signal ds_ip      : std_logic := '0';
    signal if_ce      : std_logic := '0';
    signal if_tc      : std_logic := '0';
    signal inv_ic     : std_logic := '0';
    

    signal in0   : std_logic_vector (WIDTH/4-1 downto 0) := (others => '0');
    signal in1   : std_logic_vector (WIDTH/4-1 downto 0) := (others => '0');
    signal in2   : std_logic_vector (WIDTH/4-1 downto 0) := (others => '0');
    signal in3   : std_logic_vector (WIDTH/4-1 downto 0) := (others => '0');
    signal in0_3 : std_logic_vector (WIDTH-1 downto 0)   := (others => '0');

    signal cache_data_out, cache_inst_out : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal cache_data_in                  : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    
    signal instr_1, instr_0  : std_logic_vector (INST_WIDTH-1 downto 0) := (others => '0');
    
    signal iw_address  : std_logic_vector(ADDR_WIDTH-1 downto 0) := (others => '0');
    
    signal Nmbusy      : std_logic := '0';
    signal llatch_ce   : std_logic := '0';
    signal llatch      : std_logic := '0';
    signal slatch      : std_logic := '0';
    signal store_stb   : std_logic := '0';
    signal ifetch      : std_logic := '0';
    signal bdflag      : std_logic := '0';
    signal duncached   : std_logic := '0';
    signal iuncached   : std_logic := '0';
    signal iunflag     : std_logic := '0';
    signal dcbyp       : std_logic := '0';
    signal if_start    : std_logic := '0';
    signal sbh         : std_logic := '0';
    
    signal benA_latch : std_logic_vector (3 downto 0)            := "0000";
    signal benA_ldl   : std_logic_vector (3 downto 0)            := "0000";
    signal daddr_reg  : std_logic_vector (ADDR_WIDTH-1 downto 0) := (others => '0');
    signal ddata_reg  : std_logic_vector (WIDTH-1 downto 0)      := (others => '0');

    --
    signal da_inc_r, da_inc_c : std_logic_vector (2 downto 0) := "000";
    signal ia_inc_r, ia_inc_c : std_logic_vector (2 downto 0) := "000";

    signal icycle_r, dcycle_r : integer range 0 to 8;
    signal icycle_c, dcycle_c : integer range 0 to 8;

    signal iline_we_r, iline_we_c : std_logic_vector (ICACHE_WPL-1 downto 0);
    signal dline_we_r, dline_we_c : std_logic_vector (DCACHE_WPL-1 downto 0);
    
    --
    signal xmem_dop    : std_logic := '0';
    signal xmem_iop    : std_logic := '0';
    signal xmem_rd     : std_logic := '0';
    signal xmem_wr     : std_logic := '0';
    signal xmem_strobe : std_logic := '0';

    signal xmem_state : std_logic_vector (5 downto 0) := (others => '0');
    signal xmem_ns    : std_logic_vector (5 downto 0) := (others => '0');
    
    constant xmem_idle     : std_logic_vector(5 downto 0) := "000000";
    constant xmem_if_start : std_logic_vector(5 downto 0) := "010111";
    constant xmem_if_cwait : std_logic_vector(5 downto 0) := "010101";
    constant xmem_ld_start : std_logic_vector(5 downto 0) := "100111";
    constant xmem_ld_cwait : std_logic_vector(5 downto 0) := "100101";
    constant xmem_st_start : std_logic_vector(5 downto 0) := "101011";
    constant xmem_st_cwait : std_logic_vector(5 downto 0) := "101001";

begin   -- RTL

    Nstall <= not PCT_stall;
    
    ---------------------------------------------------------------------------
    --                         PORT A - data port related logic
    ---------------------------------------------------------------------------

    -- input always right justified. rearrange input to the
    -- correct byte lanes
    dinmux_1: dinmux
        generic map (WIDTH => WIDTH)
        port map (ben => benA , din => data_in,
                  d0  => in0, d1  => in1,
                  d2  => in2, d3  => in3);
    in0_3   <= in0 & in1 & in2 & in3;

    -- input from outside world is arranged by byte lanes
    -- make it right justified.
    doutmux_1: doutmux
        generic map (WIDTH => WIDTH)
        port map (ben  => benA_latch,
                  d0   => cache_data_out((4*(WIDTH/4))-1 downto 3*(WIDTH/4)),
                  d1   => cache_data_out((3*(WIDTH/4))-1 downto 2*(WIDTH/4)),
                  d2   => cache_data_out((2*(WIDTH/4))-1 downto 1*(WIDTH/4)),
                  d3   => cache_data_out((1*(WIDTH/4))-1 downto 0*(WIDTH/4)),
                  dout => data_out);
    -- address & ben latched from output of alu
    daddr_regs_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            daddr_reg  <= (others => '0');
            benA_latch <= (others => '0');
        elsif rising_edge(clk) then
            if RF_lsu = '1' and PCT_stall = '0' then
                daddr_reg   <= daddr    after 1 ns;
                benA_latch  <= benA     after 1 ns;
            end if;
        end if;
    end process daddr_regs_proc;

    -- data latched after ex stage, because data has to
    -- go thru additional logic (byte lane mapping above)
    data_regs_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            ddata_reg <= (others => '0');
        elsif rising_edge(clk) then
            if EX_store = '1' then
                ddata_reg   <= in0_3  after 1 ns;                
            end if;
        end if;
    end process data_regs_proc;

    -- benA follower flops, used after load completes
    benA_ldl_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            benA_ldl <= (others => '0');
        elsif rising_edge(clk) then
            benA_ldl <= benA_latch after 1 ns;
        end if;
    end process benA_ldl_proc;

    -- store signal is latched : reset if write complete
    slatch_proc: process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            slatch <= '0';
        elsif rising_edge(clk) then
            if EX_store = '1' then
                slatch <= '1' after 1 ns;
            elsif EXM_wr_done = '1' then
                slatch <= '0' after 1 ns;
            end if;
        end if;
    end process slatch_proc;

    -- store strobe : goes high for One cycle
    -- will stay high for back to back stores
    process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            store_stb <= '0';
        elsif rising_edge(clk) then
            if EX_store = '1' then
                store_stb <= '1';
            else
                store_stb <= '0';
            end if;
        end if;
    end process ;
    
    -- load signal is latched : reset when there
    -- is a dcache hit.
    llatch_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            llatch <= '0';
        elsif rising_edge(clk) then
            if EX_load = '1' then
                llatch <= '1' after 1 ns;                
            elsif dcache_hit = '1' or EXM_buserr = '1' then
                llatch <= '0' after 1 ns;
            end if;
        end if;
    end process llatch_proc;

    MEM_Nbusy     <= Nmbusy;    
    MEM_dfetch_ip <= llatch;

    -- dcache hit when tags compare, tag is valid & bypass dcache is zero
    -- and accessing cacheable region
    dcache_hit <= not bdflag when ((dtag_cmp = '1' and llatch = '1') or dcbyp = '1')
                  else '0';

    -- tag memory's output is held in reset unless needed
    drst <= SFR_bdflag;
    denb <= (RF_load or store_stb or llatch or slatch)    after 1 ns;

   -- store size < word
    sbh  <= '1' when store_stb = '1' and benA_latch /= "1111" else '0';        
    
    ld_done <=  (dcache_hit or EXM_buserr);
    st_done <=  EXM_wr_done;
    df_done <=  xmem_dop and EXM_rd_done  after 1 ns;

    -- data cache bypass is latched in, reset
    -- when read is complete. Used for load only
    -- store is write through.
    bdflag_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            bdflag <= '0';
        elsif rising_edge(clk) then
            if EX_load = '1' then
                bdflag   <= SFR_bdflag or duncached after 1 ns;            
            elsif df_done = '1' then
                bdflag   <= '0' after 1 ns;
            end if;
        end if;
    end process bdflag_proc;

    
    -- follower for bd used instead of dtag_valid
    dcbyp_proc: process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            dcbyp <= '0';
        elsif rising_edge(clk) then
            if EX_load = '1' then
                dcbyp <= '0' after 1 ns;
            else                
                dcbyp <= bdflag after 1 ns;
            end if;
        end if;
    end process dcbyp_proc;

    -- data into to cache
    cache_data_in <= EXM_data_in when df_done = '1' else ddata_reg;

    ---------------------------------------------------------------------------
    --                  PORT B - Instruction related logic
    ---------------------------------------------------------------------------

    --  instruction fetch is complete when tags match & tag is valid
    --  and instruction cache bypass is zero and invalidate icache is zero
    --  and accessing cacheable reagion.
    icache_hit <= '1' when itag_cmp = '1' and inv_ic = '0' and iunflag = '0' else '0';

    if_ip         <= (ifetch or xmem_iop) and (not icache_hit);
    MEM_ifetch_ip <= if_ip;
    
    instr_1  <= cache_inst_out((WIDTH/2)-1 downto 0)   after 1 ns ;
    instr_0  <= cache_inst_out(WIDTH-1 downto WIDTH/2) after 1 ns ;

    instr       <= instr_1  when IAGU_pc(1) = '1' else instr_0;
    ni_get_done <= icache_hit;
    
    if_done  <=  xmem_iop and EXM_rd_done after 1 ns;
    irst     <= IAGU_ifclr and not if_done after 1 ns;  
    ienb     <= (Nstall or INTR_pend or EX_mispred) ;
    inv_ic   <= (slatch or llatch) and SFR_iiflag;  -- invalidate icache
    
    -- latch fetch only if no data/instruction fetch already in
    -- progress. We can also latch in the last cycle of fetch.
    --
    if_tc <= Nstall or IAGU_syncif ;    -- instruction fetch to be issued in this cycle
    if_ce <= if_done or (not ifetch);   -- if completed or not doing anything
    ifetch_proc: process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            ifetch <= '0';
        elsif rising_edge(clk) then
            if irst = '1' and xmem_iop = '0' then
                ifetch <= '0' after 1 ns;
            elsif if_ce = '1'  then                
                ifetch <= if_tc after 1 ns;
            end if;
        end if;
    end process ifetch_proc;

    -- instruction cache bypass
    biflag_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
           iunflag <= '0'; 
        elsif rising_edge(clk) then
            if if_done = '1' then       -- reset when instruction read completes
                iunflag <= '0' after 1 ns;
            elsif if_tc = '1' then     -- clock enable
                iunflag <= iuncached  after 1 ns;
            end if;
        end if;
    end process biflag_proc;

    -- instruction fetch can start when
    if_start <= '1' when ifetch    = '1' and icache_hit = '0' and
                         IAGU_Nxif = '0' and INTR_pend  = '0' and inv_ic = '0' else '0';
    
    -- if instruction fetch in progress then address is
    -- PC else address of next instruction
    iw_address <= IAGU_pc when xmem_iop = '1' else iaddr;

    ---------------------------------------------------------------------------
    --       State Machine to drive the external memory interface
    ---------------------------------------------------------------------------
    update_xmem : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            xmem_state <= xmem_idle;
            ia_inc_r   <= (others => '0');
            da_inc_r   <= (others => '0');
            icycle_r   <= 0;
            dcycle_r   <= 0;
            iline_we_r <= (others => '0');
            dline_we_r <= (others => '0');
        elsif rising_edge(clk) then
            xmem_state <= xmem_ns after 1 ns;
            ia_inc_r   <= ia_inc_c;
            da_inc_r   <= da_inc_c;
            icycle_r   <= icycle_c;
            dcycle_r   <= dcycle_c;
            iline_we_r <= iline_we_c;
            dline_we_r <= dline_we_c;
        end if;
    end process update_xmem;
    
    xmem_sm : process (xmem_state, if_start, llatch, dcache_hit, slatch,
                       EXM_rd_done, EXM_wr_done, icycle_r, dcycle_r)
    begin  -- process xmem_sm

        icycle_c   <= icycle_r;
        dcycle_c   <= dcycle_r;
        ia_inc_c   <= ia_inc_r;
        da_inc_c   <= da_inc_r;
        iline_we_c <= iline_we_r;
        dline_we_c <= dline_we_r;

        case xmem_state is
            when xmem_idle =>
                iline_we_c <= (others => '0');
                dline_we_c <= (others => '0');
                ia_inc_c   <= (others => '0');
                da_inc_c   <= (others => '0');

                -- instruction fetch
                if if_start = '1' then
                    xmem_ns <= xmem_if_start after 1 ns;
                    -- data fetch
                elsif llatch = '1' and dcache_hit = '0' then
                    xmem_ns <= xmem_ld_start after 1 ns;
                    if bdflag = '1' then
                        da_inc_c <= daddr_reg(4 downto 2);
                    end if;
                    -- data store
                elsif slatch = '1' then
                    da_inc_c <= daddr_reg(4 downto 2);
                    xmem_ns <= xmem_st_start after 1 ns;
                else
                    xmem_ns <= xmem_idle after 1 ns;
                end if;

                -- instruction fetch strobe 
            when xmem_if_start =>
                icycle_c <= ICACHE_WPL - 1;
                if ICACHE_WPL > 1 then
                    iline_we_c <= replicate_bit('0', ICACHE_WPL-1) & '1';
                else
                    iline_we_c <= replicate_bit('1', ICACHE_WPL);
                end if;
                xmem_ns <= xmem_if_cwait after 1 ns;

                -- wait for read to finish
            when xmem_if_cwait =>
                if EXM_rd_done = '1' then
                    if ICACHE_WPL = 1 or icycle_r = 0 then
                        xmem_ns <= xmem_idle after 1 ns;
                    else
                        icycle_c   <= icycle_r - 1;
                        ia_inc_c   <= ia_inc_r + 1;
                        xmem_ns    <= xmem_if_cwait after 1 ns;
                        iline_we_c <= iline_we_r(ICACHE_WPL-2 downto 0) & '0';
                    end if;
                else
                    xmem_ns <= xmem_if_cwait after 1 ns;
                end if;

                -- load strobe
            when xmem_ld_start =>
                dcycle_c <= DCACHE_WPL - 1;
                if DCACHE_WPL > 1 then
                    dline_we_c <= replicate_bit('0', DCACHE_WPL-1) & '1';
                else
                    dline_we_c <= replicate_bit('1', DCACHE_WPL);
                end if;
                xmem_ns <= xmem_ld_cwait after 1 ns;

                -- wait for load to complete
            when xmem_ld_cwait =>
                if EXM_rd_done = '1' then
                    if DCACHE_WPL = 1 or dcycle_r = 0 or bdflag = '1' then
                        xmem_ns <= xmem_idle after 1 ns;
                    else
                        dcycle_c   <= dcycle_r - 1;
                        da_inc_c   <= da_inc_r + 1;
                        xmem_ns    <= xmem_ld_cwait after 1 ns;
                        dline_we_c <= dline_we_r(DCACHE_WPL-2 downto 0) & '0';
                    end if;
                else
                    xmem_ns <= xmem_ld_cwait after 1 ns;
                end if;

                -- store strobe
            when xmem_st_start =>
                xmem_ns <= xmem_st_cwait after 1 ns;

                -- wait for store to complete
            when xmem_st_cwait =>
                if EXM_wr_done = '1' then
                    xmem_ns <= xmem_idle after 1 ns;
                else
                    xmem_ns <= xmem_st_cwait after 1 ns;
                end if;
            when others => xmem_ns <= xmem_state after 1 ns;
        end case;
    end process xmem_sm;

    dl_ip	 <= (llatch and not dcache_hit)  ;
    ds_ip	 <= (slatch and not EXM_wr_done) ;
    Nmbusy       <= not (xmem_iop or dl_ip or ds_ip);
    xmem_strobe  <= xmem_state(0);
    xmem_rd      <= xmem_state(2);
    xmem_wr	 <= xmem_state(3);
    xmem_iop 	 <= xmem_state(4);
    xmem_dop	 <= xmem_state(5);

    -- I/D was in progress when bus err happened    
    process (clk)
    begin
        if rising_edge(clk) then
            MEM_id_buserr <= xmem_iop & xmem_dop;
        end if;
    end process ;
    
    EXM_data_out <= ddata_reg;
    EXM_ben      <= benA_latch when xmem_wr = '1' or
                    (xmem_dop = '1' and bdflag = '1') else "1111";
    EXM_rd       <= xmem_rd;
    EXM_wr       <= xmem_wr;
    EXM_rwreq    <= xmem_strobe;

    out_addr: block
        signal data_address, inst_address : std_logic_vector (ADDR_WIDTH-1 downto 0);
    begin  -- block out_addr
        -- data address depending on number of cache line words
        dcw_1: if DCACHE_WPL = 1 generate
            data_address <= daddr_reg(ADDR_WIDTH-1 downto 2) & "00";
        end generate dcw_1;
        dcw_2: if DCACHE_WPL = 2 generate
            data_address <= daddr_reg(ADDR_WIDTH-1 downto 3) & da_inc_r(0) & "00"; 
        end generate dcw_2;
        dcw_4: if DCACHE_WPL = 4 generate
            data_address <= daddr_reg(ADDR_WIDTH-1 downto 4) & da_inc_r(1 downto 0) & "00";            
        end generate dcw_4;
        dcw_8: if DCACHE_WPL = 8 generate
            data_address <= daddr_reg(ADDR_WIDTH-1 downto 5) & da_inc_r(2 downto 0) & "00";
        end generate dcw_8;        

        -- instruction address depending on number of cache line words
        icw_1: if ICACHE_WPL = 1 generate
            inst_address <= IAGU_pc (ADDR_WIDTH-1 downto 2) & "00";
        end generate icw_1;
        icw_2: if ICACHE_WPL = 2 generate
            inst_address <= IAGU_pc (ADDR_WIDTH-1 downto 3) & ia_inc_r(0) & "00"; 
        end generate icw_2;
        icw_4: if ICACHE_WPL = 4 generate
            inst_address <= IAGU_pc (ADDR_WIDTH-1 downto 4) & ia_inc_r(1 downto 0) & "00";            
        end generate icw_4;
        icw_8: if ICACHE_WPL = 8 generate
            inst_address <= IAGU_pc (ADDR_WIDTH-1 downto 5) & ia_inc_r(2 downto 0) & "00";
        end generate icw_8;        

        EXM_daddr    <= data_address when xmem_dop = '1' else inst_address;  
    end block out_addr;
    

    ---------------------------------------------------------------------------
    --                  instantiations of RAMS for CACHE & TAG
    ---------------------------------------------------------------------------
    CACHE_MEM : block
        constant ITAG_WIDTH : integer := tagw(WIDTH, ICACHE_ADDR_WIDTH, ICACHE_WPL);
        constant ICAWIDTH   : integer := caw(ICACHE_ADDR_WIDTH,ICACHE_WPL);

        constant DTAG_WIDTH : integer := tagw(WIDTH,DCACHE_ADDR_WIDTH,DCACHE_WPL);
        constant DCAWIDTH   : integer := caw(DCACHE_ADDR_WIDTH,DCACHE_WPL);

        component icache_sp
          generic (WIDTH             : integer;
                   ICACHE_ENABLED    : boolean;
                   ICACHE_ADDR_WIDTH : integer;
                   ICACHE_WPL        : integer;
                   ICACHE_SETS       : integer;
                   ITAG_WIDTH        : integer;
                   CAWIDTH           : integer;
                   ADDR_WIDTH        : integer);
          port (clk            : in  std_logic;
                INTR_reset     : in  std_logic;
                if_done        : in  std_logic;
                irst           : in  std_logic;
                ienb           : in  std_logic;
                EXM_data_in    : in  std_logic_vector (WIDTH-1 downto 0);
                inv_ic         : in  std_logic;
                IAGU_pc        : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
                itag_address   : in  std_logic_vector (ICACHE_ADDR_WIDTH-1 downto 0);
                inst_address   : in  std_logic_vector (ICACHE_ADDR_WIDTH-1 downto 0);
                iline_we_r     : in  std_logic_vector (ICACHE_WPL-1 downto 0);
                cache_inst_out : out std_logic_vector (WIDTH-1 downto 0);
                itag_cmp       : out std_logic);
        end component;

        component dcache_sp
            generic (WIDTH             : integer;
                     DCACHE_ENABLED    : boolean;
                     ADDR_WIDTH        : integer;
                     DCACHE_SETS       : integer;
                     DTAG_WIDTH        : integer;
                     DCACHE_ADDR_WIDTH : integer;
                     DCACHE_WPL        : integer;
                     CAWIDTH           : integer);
            port (clk            : in  std_logic;
                  INTR_reset     : in  std_logic;
                  df_done        : in  std_logic;
                  sbh            : in  std_logic;
                  drst           : in  std_logic;
                  bdflag         : in  std_logic;
                  store_stb      : in  std_logic;
                  exm_buserr     : in  std_logic;
                  dline_we_r     : in  std_logic_vector (DCACHE_WPL-1 downto 0);
                  data_address   : in  std_logic_vector (DCACHE_ADDR_WIDTH-1 downto 0);
                  daddr_reg      : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
                  cache_data_in  : in  std_logic_vector (WIDTH-1 downto 0);
                  dtag_cmp       : out std_logic;
                  cache_data_out : out std_logic_vector (WIDTH-1 downto 0));
        end component;
        
        signal inst_address : std_logic_vector (ICACHE_ADDR_WIDTH-1 downto 0) := (others => '0');
        signal itag_address : std_logic_vector (ICACHE_ADDR_WIDTH-1 downto 0) := (others => '0');
        signal data_address : std_logic_vector (DCACHE_ADDR_WIDTH-1 downto 0) := (others => '0');
        signal da           : std_logic_vector (ADDR_WIDTH-1 downto 0)        := (others => '0');

        constant iuzero : std_logic_vector(ADDR_WIDTH-ITAG_WIDTH-ICACHE_ADDR_WIDTH+2 downto 0) := (others => '0');
        constant duzero : std_logic_vector(ADDR_WIDTH-DTAG_WIDTH-DCACHE_ADDR_WIDTH+2 downto 0) := (others => '0');
    begin

        iuncached <= '0' when iw_address(ADDR_WIDTH-1 downto ITAG_WIDTH+ICACHE_ADDR_WIDTH+1) = iuzero else '1';

        inst_address <= iw_address(ICACHE_ADDR_WIDTH+1 downto 2);
        
        -- if Invalidate Icache then write '0' to the tag valid bit
        -- at the store address
        itag_address <= daddr_reg  (ICACHE_ADDR_WIDTH+1 downto 2) when inv_ic ='1' else
                        iw_address (ICACHE_ADDR_WIDTH+1 downto 2);
        
        icache_sp_inst: icache_sp
            generic map (WIDTH             => WIDTH,
                         ICACHE_ENABLED    => ICACHE_ENABLED,
                         ICACHE_ADDR_WIDTH => ICACHE_ADDR_WIDTH,
                         ICACHE_WPL        => ICACHE_WPL,
                         ICACHE_SETS       => ICACHE_SETS,
                         ITAG_WIDTH        => ITAG_WIDTH,
                         CAWIDTH           => ICAWIDTH,
                         ADDR_WIDTH        => ADDR_WIDTH)
            port map (clk            => clk,
                      INTR_reset     => INTR_reset,
                      if_done        => if_done,
                      irst           => irst,
                      ienb           => ienb,
                      EXM_data_in    => EXM_data_in,
                      inv_ic         => inv_ic,
                      IAGU_pc        => IAGU_pc,
                      itag_address   => itag_address,
                      inst_address   => inst_address,
                      iline_we_r     => iline_we_r,
                      cache_inst_out => cache_inst_out,
                      itag_cmp       => itag_cmp);

        -- first cycle of load use the address from alu, otherwise use from registers.
        da <= daddr_reg when store_stb = '1' or (llatch = '1' and dcache_hit = '0') else daddr;
        data_address <= da  (DCACHE_ADDR_WIDTH+1 downto 2);
        duncached <= '0' when daddr (ADDR_WIDTH-1 downto DTAG_WIDTH+DCACHE_ADDR_WIDTH+1) = duzero else '1';
        
        dcache_sp_inst: dcache_sp
            generic map (WIDTH             => WIDTH,
                         DCACHE_ENABLED    => DCACHE_ENABLED,
                         ADDR_WIDTH        => ADDR_WIDTH,
                         DCACHE_SETS       => DCACHE_SETS,
                         DTAG_WIDTH        => DTAG_WIDTH,
                         DCACHE_ADDR_WIDTH => DCACHE_ADDR_WIDTH,
                         DCACHE_WPL        => DCACHE_WPL,
                         CAWIDTH           => DCAWIDTH)
            port map (clk            => clk,
                      INTR_reset     => INTR_reset,
                      df_done        => df_done,
                      sbh            => sbh,
                      drst           => drst,
                      bdflag         => bdflag,
                      store_stb      => store_stb,
                      exm_buserr     => exm_buserr,
                      dline_we_r     => dline_we_r,
                      data_address   => data_address,
                      daddr_reg      => daddr_reg,
                      cache_data_in  => cache_data_in,
                      dtag_cmp       => dtag_cmp,
                      cache_data_out => cache_data_out);
        
                
    end block CACHE_MEM;
    
end RTL;    
