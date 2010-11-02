-------------------------------------------------------------------------------
-- Title      : Interrupt
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : intr.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2003-01-07
-- Last update: 2006-07-31
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Minimalist interrupt controller.
-------------------------------------------------------------------------------
-- Copyright (c) 2003 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2003-01-07  1.0      sandeep	Created
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

entity IntrCntrl is
    
    generic (WIDTH       : integer := 32;
             INTR_SWIVEC : integer := 4;
             INTR_TMRVEC : integer := 8;
             INTR_EXTVEC : integer := 12;
             INTR_BERVEC : integer := 16);

    port (TIMER_exp     : in  std_logic;
          EXTRN_int     : in  std_logic_vector(NUM_INTRS-1 downto 0);
          RESET_int     : in  std_logic;
          BUSERR_int    : in  std_logic;
          IAGU_anull    : in  std_logic;
          MEM_ifetch_ip : in  std_logic;
          EX_mispred    : in  std_logic;
          EX_dbghit     : in  std_logic;
          RF_swi        : in  std_logic;
          RF_ivalid     : in  std_logic;
          RF_pc         : in  std_logic_vector(ADDR_WIDTH-1 downto 0);
          clk           : in  std_logic;
          SFR_ieflag    : in  std_logic;
          SFR_ipflag    : in  std_logic;
          SFR_pdflag    : in  std_logic;
          SFR_sspsw     : in  std_logic;
          SFR_ibase	: in  std_logic_vector(ADDR_WIDTH-1 downto 0);
          SFR_eienb	: in  std_logic_vector(NUM_INTRS-1 downto 0);
          INTR_pend     : out std_logic;
          INTR_swi      : out std_logic;
          INTR_sstep    : out std_logic;
          INTR_extrn    : out std_logic;
          INTR_timer    : out std_logic;
          INTR_reset    : out std_logic;
          INTR_buserr   : out std_logic;          
          INTR_eistat   : out std_logic_vector(NUM_INTRS-1 downto 0);
          INTR_vector   : out std_logic_vector(ADDR_WIDTH-1 downto 0));

end IntrCntrl;

architecture Behavioral of IntrCntrl is
    signal gie     : std_logic;
    signal combin  : std_logic_vector(3 downto 0);
    signal combout : std_logic_vector(2 downto 0);

    signal reset  : std_logic := '0';
    signal extrn  : std_logic := '0';
    signal extrnr : std_logic := '0';
    signal timer  : std_logic := '0';
    signal buserr : std_logic := '0';
    
    signal reset_sync0 : std_logic := '0';
    signal extrn_sync0 : std_logic_vector(NUM_INTRS-1 downto 0) := (others => '0');
    signal extrn_sync1 : std_logic_vector(NUM_INTRS-1 downto 0) := (others => '0');
    signal extrn_comb  : std_logic_vector(NUM_INTRS-1 downto 0) := (others => '0');
    signal extrn_cout  : std_logic_vector(NUM_INTRS-1 downto 0) := (others => '0');
    signal eistat      : std_logic_vector(NUM_INTRS-1 downto 0) := (others => '0');
    
    signal timer_intr : std_logic;
    signal swint_intr : std_logic;
    signal extrn_intr : std_logic;
    signal sstep_intr : std_logic;
    signal swi_in     : std_logic;
begin  -- Behavioral

    sync_proc : process (clk)
    begin
        if rising_edge(clk) then
            extrn_sync0 <= EXTRN_int;
            reset_sync0 <= RESET_int;
            extrn_sync1 <= extrn_sync0;
            reset       <= reset_sync0;
            buserr      <= BUSERR_int and not buserr;
        end if;
    end process sync_proc;

    eicomb: for i in 0 to NUM_INTRS-1 generate
        extrn_comb(i) <= SFR_eienb(i) and extrn_sync1(i);
    end generate eicomb;

    --
    -- priority encoding of interrupts
    -- 0 has highest priority NUM_INTRS-1 least
    process (extrn_comb)
    begin
        if extrn_comb(0) = '1' then
            extrn_cout <= "000001";
        elsif extrn_comb(1) = '1' then
            extrn_cout <= "000010";
        elsif extrn_comb(2) = '1' then
            extrn_cout <= "000100";
        elsif extrn_comb(3) = '1' then
            extrn_cout <= "001000";
        elsif extrn_comb(4) = '1' then
            extrn_cout <= "010000";            
        elsif extrn_comb(5) = '1' then
            extrn_cout <= "100000";            
        else
            extrn_cout <= "000000";
        end if;
    end process;

    extrnr <= extrn_cout(0) or extrn_cout(1) or extrn_cout(2) or extrn_cout(3) or extrn_cout(4) or extrn_cout(5);
    
    combin <= (reset or buserr) & extrnr & '0' & TIMER_exp;
    swi_in <= (RF_swi or EX_dbghit);
    
    comb_proc : process (combin)
    begin
        case combin is
            -- reset has highest priority
            when "1000"  => combout <= "000";
            when "1001"  => combout <= "000";
            when "1010"  => combout <= "000";
            when "1011"  => combout <= "000";
            when "1100"  => combout <= "000";
            when "1101"  => combout <= "000";
            when "1110"  => combout <= "000";
            when "1111"  => combout <= "000";
            -- then extern
            when "0100"  => combout <= "100";
            when "0101"  => combout <= "100";
            when "0110"  => combout <= "100";
            when "0111"  => combout <= "100";
            -- then swi
            when "0010"  => combout <= "010";
            when "0011"  => combout <= "010";
            -- then timer
            when "0001"  => combout <= "001";
            when others  => combout <= "000";
        end case;
    end process comb_proc;        
    
    -- not allowed to take an interrupt when RF stage instruction is invalid,
    -- or single step operation in progress or a branch has been mispredicted or intruction
    -- fetch is in progress.
    gie    <= '0' when (RF_ivalid = '0' and SFR_pdflag = '0') or SFR_sspsw = '1' or
                       EX_mispred = '1' or MEM_ifetch_ip = '1' else (SFR_ieflag and not SFR_ipflag);

    intr_proc: process (clk)
    begin
        if rising_edge(clk) then
            if IAGU_anull = '1' then        -- sync reset
                extrn  <= '0';
                timer  <= '0';
                eistat <= (others => '0');
            else
                extrn  <= combout(2) and gie and not reset;
                timer  <= combout(0) and gie and not reset;
                eistat <= extrn_cout ;
            end if;
        end if;
    end process intr_proc;

    INTR_vector <= SFR_ibase(ADDR_WIDTH-1 downto 8) & conv_std_logic_vector(INTR_SWIVEC,8) when swi_in = '1' else
                   SFR_ibase(ADDR_WIDTH-1 downto 8) & conv_std_logic_vector(INTR_EXTVEC,8) when extrn  = '1' else
                   SFR_ibase(ADDR_WIDTH-1 downto 8) & conv_std_logic_vector(INTR_TMRVEC,8) when timer  = '1' else
                   SFR_ibase(ADDR_WIDTH-1 downto 8) & conv_std_logic_vector(INTR_BERVEC,8) when buserr = '1' else
                   SFR_ibase(ADDR_WIDTH-1 downto 8) & x"00";

    INTR_reset  <= reset;
    extrn_intr  <= (extrn      and not SFR_ipflag and not EX_mispred and not swi_in);
    timer_intr  <= (timer      and not SFR_ipflag and not EX_mispred and not swi_in);
    swint_intr  <= (swi_in     and not EX_mispred and not EX_dbghit);
    sstep_intr  <= (swi_in     and EX_dbghit);
    INTR_extrn  <= extrn_intr;
    INTR_timer  <= timer_intr;   
    INTR_swi    <= swint_intr;
    INTR_sstep  <= sstep_intr;
    INTR_buserr <= buserr;
    INTR_eistat <= eistat;
    INTR_pend   <= extrn_intr or timer_intr or swint_intr or sstep_intr or reset or buserr;
    
end Behavioral;
