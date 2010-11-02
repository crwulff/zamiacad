------------------------------------------------------------------------------
-- Title : MANIK2TOP Project : MANIK2
-------------------------------------------------------------------------------
-- File       : manik2top.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-12-28
-- Last update: 2006-10-08
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Top Level module for MANIK2
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-12-28  1.0      sandeep	Created
-------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

use UNISIM.vcomponents.all;
library UNISIM;


use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;
use work.manikaltera.all;
use work.manikactel.all;

entity manik2top is
    
    generic (WIDTH             : integer := 32;
             UINST_WIDTH       : integer := 32;
             USREG_ENABLED     : boolean := true;
             TIMER_WIDTH       : integer := 32;
             TIMER_CLK_DIV     : integer := 0;
             INTR_VECBASE      : integer := 0;
             INTR_SWIVEC       : integer := 4;
             INTR_TMRVEC       : integer := 8;
             INTR_EXTVEC       : integer := 12;
             INTR_BERVEC       : integer := 16;
             BASE_ROW          : integer := 0;
             BASE_COL          : integer := 2;
             USER_INST         : Boolean := true;
             ICACHE_ENABLED    : Boolean := true;
             DCACHE_ENABLED    : Boolean := true;
             ICACHE_LINE_WORDS : integer := 1;
             DCACHE_LINE_WORDS : integer := 1;
             ICACHE_SETS       : integer := 1;
             DCACHE_SETS       : integer := 1;
             ICACHE_ADDR_WIDTH : integer := 10;
             DCACHE_ADDR_WIDTH : integer := 10;
             SHIFT_SWIDTH      : integer := 3;
             MULT_BWIDTH       : integer := 32;
             HW_WPENB	       : boolean := false;
             HW_BPENB	       : boolean := false);

    port (clk       : in  std_logic;
          EXTRN_int : in  std_logic_vector(NUM_INTRS-1 downto 0)   := (others => '0');
          RESET_int : in  std_logic                                := '0';
          INTR_ack  : out std_logic;

          -- Wishbone Master interface
          WBM_DAT_I    : in  std_logic_vector (WIDTH-1 downto 0)      := (others => '0');
          WBM_ACK_I    : in  std_logic                                := '0';
          WBM_ERR_I    : in  std_logic				      := '0';
          WBM_DAT_O    : out std_logic_vector (WIDTH-1 downto 0);
          WBM_SEL_O    : out std_logic_vector (3 downto 0);
          WBM_WE_O     : out std_logic;
          WBM_STB_O    : out std_logic;
          WBM_CYC_O    : out std_logic;
          WBM_LOCK_O   : out std_logic;
          WBM_ADR_O    : out std_logic_vector (ADDR_WIDTH-1 downto 0) := (others => '0');
          WBM_CTI_O    : out std_logic_vector (2 downto 0);
          WBM_BTE_O    : out std_logic_vector (1 downto 0);
          
          UINST_uiop   : out std_logic_vector(1 downto 0);
          UINST_uinst  : out std_logic;
          UINST_Nce    : out std_logic;
          UINST_wbc    : out std_logic;
          UINST_uiopA  : out std_logic_vector(UINST_WIDTH-1 downto 0);
          UINST_uiopB  : out std_logic_vector(UINST_WIDTH-1 downto 0);
          UINST_uip    : in  std_logic                                := '0';
          UINST_out    : in  std_logic_vector(UINST_WIDTH-1 downto 0) := (others => '0'));
   
end manik2top;

architecture Behavioral of manik2top is

    component icache
        generic (INST_WIDTH  : integer);
        
        port (ni_addr     : in  std_logic_vector(ADDR_WIDTH-1 downto 0);
              stall       : in  std_logic;
              clk         : in  std_logic;
              rst         : in  std_logic;
              ni_get_done : out std_logic;
              n_instr     : out std_logic_vector(INST_WIDTH-1 downto 0));
    end component;
    
    component ucache
        generic (WIDTH             : integer;
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
              RF_load       : in  std_logic;
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
              MEM_id_buserr : out std_logic_vector (1 downto 0);
              ld_done       : out std_logic;
              st_done       : out std_logic;
              MEM_Nbusy     : out std_logic;
              data_out      : out std_logic_vector (WIDTH-1 downto 0);
              ni_get_done   : out std_logic;
              instr         : out std_logic_vector (INST_WIDTH-1 downto 0)); 
    end component;

    component iagunit
        generic (WIDTH : integer);
        port (IF_RegBr      : in  std_logic;
              IF_Br         : in  std_logic;
              IF_BrUnc      : in  std_logic;
              IF_BrShort    : in  std_logic;
              IF_BrOffset   : in  std_logic_vector (IF_IMM_WIDTH-1 downto 0);
              IF_BrOffset_n : in  std_logic_vector (INST_WIDTH-1 downto 0);
              EX_addr       : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              EX_opb        : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              EX_mispred    : in  std_logic;
              EX_mip        : in  std_logic;
              EX_uip        : in  std_logic;
              EX_dbghit     : in  std_logic;
              DE_pc         : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              DE_rstregs    : in  std_logic;
              DE_swi        : in  std_logic;
              DE_sstep      : in  std_logic;
              DE_ivalid     : in  std_logic;
              RF_swi        : in  std_logic;
              RF_sstep      : in  std_logic;
              INTR_pend     : in  std_logic;
              INTR_reset    : in  std_logic;
              INTR_vector   : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              PCT_sdly      : in  std_logic;
              MEM_ifetch_ip : in  std_logic;
              stall         : in  std_logic;
              clk           : in  std_logic;
              IAGU_debug    : out std_logic_vector (3 downto 0);
              IAGU_pc       : out std_logic_vector (ADDR_WIDTH-1 downto 0);
              IAGU_ni_addr  : out std_logic_vector (ADDR_WIDTH-1 downto 0);
              IAGU_anull    : out std_logic;
              IAGU_ifclr    : out std_logic;
              IAGU_Nxif     : out std_logic;
              IAGU_intr     : out std_logic;
              IAGU_deregbr  : out std_logic;
              IAGU_rfregbr  : out std_logic;
              IAGU_exbr     : out std_logic;
              IAGU_exbrlong : out std_logic;
              IAGU_exregbr  : out std_logic;
              IAGU_syncif   : out std_logic);
    end component;

    component IFDEunit
        generic (WIDTH     : integer;
                 USER_INST : Boolean);
        port (ni_get_done  : in  std_logic;
              n_instr      : in  std_logic_vector (INST_WIDTH-1 downto 0);
              IAGU_anull   : in  std_logic;
              IAGU_deregbr : in  std_logic;
              IAGU_rfregbr : in  std_logic;
              IAGU_pc      : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              PCT_stall    : in  std_logic;
              SFR_ssflag   : in  std_logic;
              clk          : in  std_logic;
              IF_RegBr     : out std_logic;
              IF_Br        : out std_logic;
              IF_BrUnc     : out std_logic;
              IF_BrShort   : out std_logic;
              IF_BrOffset  : out std_logic_vector (IF_IMM_WIDTH-1 downto 0);
              DE_load      : out std_logic;
              DE_store     : out std_logic;
              DE_half      : out std_logic;
              DE_byte      : out std_logic;
              DE_word      : out std_logic;
              DE_iscond    : out std_logic;
              DE_condT     : out std_logic;
              DE_Br        : out std_logic;
              DE_BrUnc     : out std_logic;
              DE_BrShort   : out std_logic;
              DE_imm       : out std_logic_vector (DE_IMM_WIDTH-1 downto 0);
              DE_immSigned : out std_logic;
              DE_immSHL    : out std_logic_vector (1 downto 0);
              DE_immAlign4 : out std_logic;
              DE_rd        : out std_logic_vector (3 downto 0);
              DE_rb        : out std_logic_vector (3 downto 0);
              DE_asel      : out std_logic_vector (1 downto 0);
              DE_bsel      : out std_logic_vector (1 downto 0);
              DE_flagop    : out std_logic_vector (2 downto 0);
              DE_pc        : out std_logic_vector (ADDR_WIDTH-1 downto 0);
              DE_rfwe      : out std_logic;
              DE_sfrwe     : out std_logic;
              DE_swi       : out std_logic;
              DE_cywe      : out std_logic;
              DE_tfwe      : out std_logic;
              DE_BrLink    : out std_logic;
              DE_aluop     : out std_logic_vector (3 downto 0);
              DE_alu       : out std_logic;
              DE_rstregs   : out std_logic;
              DE_uinst     : out std_logic;
              DE_sstep     : out std_logic;
              DE_ivalid    : out std_logic;
              DE_uiop      : out std_logic_vector (1 downto 0));
    end component;
    
    component RFunit
        generic (WIDTH     : integer;
                 USER_INST : Boolean;
                 HW_BPENB  : boolean);

        port (DE_load      : in  std_logic;
              DE_store     : in  std_logic;
              DE_half      : in  std_logic;
              DE_byte      : in  std_logic;
              DE_word      : in  std_logic;
              DE_iscond    : in  std_logic;
              DE_condT     : in  std_logic;
              DE_Br        : in  std_logic;
              DE_BrUnc     : in  std_logic;
              DE_imm       : in  std_logic_vector (DE_IMM_WIDTH-1 downto 0);
              DE_immSigned : in  std_logic;
              DE_immSHL    : in  std_logic_vector (1 downto 0);
              DE_immAlign4 : in  std_logic;
              DE_rd        : in  std_logic_vector (3 downto 0);
              DE_rb        : in  std_logic_vector (3 downto 0);
              DE_asel      : in  std_logic_vector (1 downto 0);
              DE_bsel      : in  std_logic_vector (1 downto 0);
              DE_flagop    : in  std_logic_vector (2 downto 0);
              DE_pc        : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              DE_rfwe      : in  std_logic;
              DE_sfrwe     : in  std_logic;
              DE_swi       : in  std_logic;
              DE_cywe      : in  std_logic;
              DE_tfwe      : in  std_logic;
              DE_BrLink    : in  std_logic;
              DE_aluop     : in  std_logic_vector (3 downto 0);  -- alu operation
              DE_alu       : in  std_logic;
              DE_uinst     : in  std_logic;
              DE_sstep     : in  std_logic;
              DE_ivalid    : in  std_logic;
              DE_uiop      : in  std_logic_vector (1 downto 0);
              SFR_bp_enb   : in  std_logic_vector (1 downto 0);
              SFR_bp0      : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              SFR_bp1      : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              IAGU_anull   : in  std_logic;
              IAGU_deregbr : in  std_logic;
              PCT_stall    : in  std_logic;
              WB_data_out  : in  std_logic_vector (WIDTH-1 downto 0);
              WB_rd        : in  std_logic_vector (3 downto 0);
              WB_rdeq      : in  std_logic;
              WB_rbeq      : in  std_logic;
              WB_rfwe      : in  std_logic;
              clk          : in  std_logic;
              RF_load      : out std_logic;
              RF_store     : out std_logic;
              RF_half      : out std_logic;
              RF_byte      : out std_logic;
              RF_word      : out std_logic;
              RF_iscond    : out std_logic;
              RF_condT     : out std_logic;
              RF_Br        : out std_logic;
              RF_BrUnc     : out std_logic;
              RF_imm       : out std_logic_vector (RF_IMM_WIDTH-1 downto 0);
              RF_asel      : out std_logic_vector (1 downto 0);
              RF_bsel      : out std_logic_vector (1 downto 0);
              RF_rdval     : out std_logic_vector (WIDTH-1 downto 0);
              RF_rbval     : out std_logic_vector (WIDTH-1 downto 0);
              RF_pc        : out std_logic_vector (ADDR_WIDTH-1 downto 0);
              RF_rfwe      : out std_logic;
              RF_sfrwe     : out std_logic;
              RF_swi       : out std_logic;
              RF_swinum    : out std_logic_vector (SWINUM_WIDTH-1 downto 0);
              RF_cywe      : out std_logic;
              RF_tfwe      : out std_logic;
              RF_aluop     : out std_logic_vector (3 downto 0);
              RF_rd        : out std_logic_vector (3 downto 0);
              RF_rb        : out std_logic_vector (3 downto 0);
              RF_flagop    : out std_logic_vector (2 downto 0);
              RF_alu       : out std_logic;
              RF_lsu       : out std_logic;
              RF_msu       : out std_logic;
              RF_rfi       : out std_logic;
              RF_uinst     : out std_logic;
              RF_sstep     : out std_logic;
              RF_ivalid    : out std_logic;
              RF_uiop      : out std_logic_vector (1 downto 0);
              RF_shift     : out std_logic;
              RF_lsmsu     : out std_logic;
              RF_dreg      : out std_logic;
              RF_bphit	   : out std_logic);
    end component;

    component EXunit
        generic (WIDTH     : integer;
                 HW_WPENB  : boolean;
                 HW_BPENB  : boolean;
                 USER_INST : boolean);
        port (RF_load     : in  std_logic;
              RF_store    : in  std_logic;
              RF_half     : in  std_logic;
              RF_byte     : in  std_logic;
              RF_word     : in  std_logic;
              RF_iscond   : in  std_logic;
              RF_condT    : in  std_logic;
              RF_Br       : in  std_logic;
              RF_BrUnc    : in  std_logic;
              RF_imm      : in  std_logic_vector (RF_IMM_WIDTH-1 downto 0);
              RF_asel     : in  std_logic_vector (1 downto 0);
              RF_bsel     : in  std_logic_vector (1 downto 0);
              RF_rdval    : in  std_logic_vector (WIDTH-1 downto 0);
              RF_rbval    : in  std_logic_vector (WIDTH-1 downto 0);
              RF_pc       : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              RF_rfwe     : in  std_logic;
              RF_sfrwe    : in  std_logic;
              RF_swi      : in  std_logic;
              RF_cywe     : in  std_logic;
              RF_tfwe     : in  std_logic;
              RF_aluop    : in  std_logic_vector (3 downto 0);
              RF_rd       : in  std_logic_vector (3 downto 0);
              RF_rb       : in  std_logic_vector (3 downto 0);
              RF_flagop   : in  std_logic_vector (2 downto 0);
              RF_alu      : in  std_logic;
              RF_msu      : in  std_logic;
              RF_uinst    : in  std_logic;
              RF_sstep    : in  std_logic;
              RF_ivalid   : in  std_logic;
              RF_bphit	  : in  std_logic;
              PCT_stall   : in  std_logic;
              PCT_ustall  : in  std_logic;
              IAGU_anull  : in  std_logic;
              INTR_reset  : in  std_logic;
              MULT_mdone  : in  std_logic;
              MULT_out    : in  std_logic_vector (WIDTH-1 downto 0);
              MULT_rd     : in  std_logic_vector (3 downto 0);
              UINST_uip   : in  std_logic;
              UINST_out   : in  std_logic_vector (WIDTH-1 downto 0);
              SFR_tfflag  : in  std_logic;
              SFR_cyflag  : in  std_logic;
              SFR_sfrval  : in  std_logic_vector (WIDTH-1 downto 0);
              SFR_wp_enb  : in std_logic_vector (1 downto 0);
              SFR_wp0     : in std_logic_vector (ADDR_WIDTH-1 downto 0);
              SFR_wp1     : in std_logic_vector (ADDR_WIDTH-1 downto 0);
              clk         : in  std_logic;
              MEM_dbus    : in  std_logic_vector (WIDTH-1 downto 0);
              MEM_ld_done : in  std_logic;
              MEM_st_done : in  std_logic;
              MEM_Nbusy   : in  std_logic;
              EX_wphit    : out std_logic_vector (1 downto 0);
              EX_bphit    : out std_logic;
              EX_load     : out std_logic;
              EX_store    : out std_logic;
              EX_lsu      : out std_logic;
              EX_rdval    : out std_logic_vector (WIDTH-1 downto 0);
              EX_rbval    : out std_logic_vector (WIDTH-1 downto 0);
              EX_opb      : out std_logic_vector (WIDTH-1 downto 0);
              EX_out      : out std_logic_vector (WIDTH-1 downto 0);
              EX_sfrwe    : out std_logic;
              EX_cyflag   : out std_logic;
              EX_tfflag   : out std_logic;
              EX_cywe     : out std_logic;
              EX_tfwe     : out std_logic;
              EX_addr     : out std_logic_vector (ADDR_WIDTH-1 downto 0);
              EX_st_data  : out std_logic_vector (WIDTH-1 downto 0);
              EX_sfrreg   : out std_logic_vector (3 downto 0);
              EX_sfrval   : out std_logic_vector (WIDTH-1 downto 0);
              EX_mip      : out std_logic;
              EX_uip      : out std_logic;
              EX_ben      : out std_logic_vector (3 downto 0);
              EX_uwbc     : out std_logic;
              EX_mispred  : out std_logic;
              EX_dbghit   : out std_logic;
              EX_ivalid   : out std_logic;
              EX_addout   : out std_logic_vector (WIDTH-1 downto 0);
              WB_data_out : out std_logic_vector (WIDTH-1 downto 0);
              WB_rd       : out std_logic_vector (3 downto 0);
              WB_rfwe     : out std_logic;
              WB_stall    : out std_logic;
              WB_ldwbreg  : out std_logic_vector (3 downto 0);
              WB_ldreg    : out std_logic_vector (3 downto 0);
              WB_uireg    : out std_logic_vector (3 downto 0);
              WB_rdeq     : out std_logic;
              WB_rbeq     : out std_logic);
    end component;

    component pipectrl
        generic (WIDTH     : integer;
                 USER_INST : Boolean);
        port (WB_stall      : in  std_logic;
              WB_ldwbreg    : in  std_logic_vector (3 downto 0);
              WB_ldreg      : in  std_logic_vector (3 downto 0);
              WB_uireg      : in  std_logic_vector (3 downto 0);
              EX_mip        : in  std_logic;
              EX_uip        : in  std_logic;
              RF_load       : in  std_logic;
              RF_store      : in  std_logic;
              RF_rfwe       : in  std_logic;
              RF_dreg       : in  std_logic;
              RF_breg       : in  std_logic;
              RF_rd         : in  std_logic_vector (3 downto 0);
              RF_rb         : in  std_logic_vector (3 downto 0);
              RF_msu        : in  std_logic;
              RF_lsu        : in  std_logic;
              RF_uinst      : in  std_logic;
              RF_lsmsu      : in  std_logic;
              MULT_rd       : in  std_logic_vector (3 downto 0);
              MEM_Nbusy     : in  std_logic;
              INTR_pend     : in  std_logic;
              SFR_pdflag    : in  std_logic;
              ni_get_done   : in  std_logic;
              IAGU_syncif   : in  std_logic;
              MEM_dfetch_ip : in  std_logic;
              MEM_ld_done   : in  std_logic;
              clk           : in  std_logic;
              PCT_sdly      : out std_logic;
              PCT_msustall  : out std_logic;
              PCT_stall     : out std_logic;
              PCT_ustall    : out std_logic);
    end component;
    
    component sfrs
        generic (WIDTH         : integer;
                 INTR_VECBASE  : integer;
                 HW_WPENB      : boolean;
                 HW_BPENB      : boolean;
                 USREG_ENABLED : boolean;
                 TIMER_WIDTH   : integer); 
        port (EX_sfrreg     : in  std_logic_vector (3 downto 0);
              EX_sfrval     : in  std_logic_vector (WIDTH-1 downto 0);
              EX_sfrwe      : in  std_logic;
              EX_cyflag     : in  std_logic;
              EX_tfflag     : in  std_logic;
              EX_cywe       : in  std_logic;
              EX_tfwe       : in  std_logic;
              EX_mispred    : in  std_logic;
              EX_dbghit     : in  std_logic;
              EX_wphit	    : in  std_logic_vector (1 downto 0);
              EX_bphit	    : in  std_logic;
              IAGU_pc       : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              IAGU_deregbr  : in  std_logic;
              IAGU_exbr     : in  std_logic;
              IAGU_exbrlong : in  std_logic;
              IAGU_exregbr  : in  std_logic;
              IAGU_intr     : in  std_logic;
              IAGU_anull    : in  std_logic;
              PCT_sdly	    : in  std_logic;
              PCT_stall     : in  std_logic;
              RF_pc         : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              RF_rfi        : in  std_logic;
              RF_swinum     : in  std_logic_vector (SWINUM_WIDTH-1 downto 0);
              DE_pc         : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              DE_BrLink     : in  std_logic;
              DE_rb         : in  std_logic_vector (3 downto 0);
              DE_sstep      : in  std_logic;
              clk           : in  std_logic;
              INTR_swi      : in  std_logic;
              INTR_sstep    : in  std_logic;
              INTR_timer    : in  std_logic;
              INTR_extrn    : in  std_logic;
              INTR_reset    : in  std_logic;
              INTR_buserr   : in  std_logic;
              INTR_eistat   : in  std_logic_vector (NUM_INTRS-1 downto 0);
              TIMER_exp     : in  std_logic;
              TIMER_undf    : in  std_logic;
              TIMER_val     : in  std_logic_vector (TIMER_WIDTH-1 downto 0);
              MEM_id_buserr : in  std_logic_vector (1 downto 0);
              SFR_tstart    : out std_logic;
              SFR_tfflag    : out std_logic;
              SFR_cyflag    : out std_logic;
              SFR_ieflag    : out std_logic;
              SFR_ipflag    : out std_logic;
              SFR_teflag    : out std_logic;
              SFR_swflag    : out std_logic;
              SFR_tiflag    : out std_logic;
              SFR_eiflag    : out std_logic;
              SFR_pdflag    : out std_logic;
              SFR_bdflag    : out std_logic;
              SFR_iiflag    : out std_logic;
              SFR_trload    : out std_logic;
              SFR_ssflag    : out std_logic;
              SFR_sspsw     : out std_logic;
              SFR_wp_enb    : out std_logic_vector(1 downto 0);
              SFR_bp_enb    : out std_logic_vector(1 downto 0);
              SFR_wp0	    : out std_logic_vector (ADDR_WIDTH-1 downto 0);
              SFR_wp1	    : out std_logic_vector (ADDR_WIDTH-1 downto 0);
              SFR_bp0	    : out std_logic_vector (ADDR_WIDTH-1 downto 0);
              SFR_bp1	    : out std_logic_vector (ADDR_WIDTH-1 downto 0);
              SFR_ibase	    : out std_logic_vector (ADDR_WIDTH-1 downto 0);
              SFR_eienb     : out std_logic_vector (NUM_INTRS-1 downto 0);
              SFR_sfrval    : out std_logic_vector (WIDTH-1 downto 0);
              SFR_trval     : out std_logic_vector (TIMER_WIDTH-1 downto 0)); 
    end component;

    component multshift
        generic (WIDTH        : integer;
                 SHIFT_SWIDTH : integer;
                 MULT_BWIDTH  : integer);
        port (RF_msu     : in  std_logic;
              RF_shift   : in  std_logic;
              RF_shcode  : in  std_logic_vector (1 downto 0);
              clk        : in  std_logic;
              INTR_reset : in  std_logic;
              msopa      : in  std_logic_vector (WIDTH-1 downto 0);
              msopb      : in  std_logic_vector (WIDTH-1 downto 0);
              stall      : in  std_logic;
              RF_rd      : in  std_logic_vector (3 downto 0);
              MULT_mdone : out std_logic;
              MULT_out   : out std_logic_vector (WIDTH-1 downto 0);
              MULT_rd    : out std_logic_vector (3 downto 0));
    end component;

    component Timer
        generic (WIDTH         : integer;
                 TIMER_WIDTH   : integer;
                 TIMER_CLK_DIV : integer); 
        port (SFR_teflag : in  std_logic;
              SFR_tiflag : in  std_logic;
              SFR_tstart : in  std_logic;
              SFR_trload : in  std_logic;
              SFR_trval  : in  std_logic_vector (TIMER_WIDTH-1 downto 0);
              clk        : in  std_logic;
              TIMER_val  : out std_logic_vector (TIMER_WIDTH-1 downto 0);
              TIMER_exp  : out std_logic;
              TIMER_undf : out std_logic);
    end component;

    component IntrCntrl
        generic (WIDTH       : integer;                 
                 INTR_SWIVEC : integer;
                 INTR_TMRVEC : integer;
                 INTR_EXTVEC : integer;
                 INTR_BERVEC : integer);
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
              SFR_ibase	    : in  std_logic_vector (WIDTH-1 downto 0);
              SFR_eienb     : in  std_logic_vector (NUM_INTRS-1 downto 0);
              INTR_pend     : out std_logic;
              INTR_swi      : out std_logic;
              INTR_sstep    : out std_logic;
              INTR_extrn    : out std_logic;
              INTR_timer    : out std_logic;
              INTR_reset    : out std_logic;
              INTR_buserr   : out std_logic;
              INTR_eistat   : out std_logic_vector(NUM_INTRS-1 downto 0);
              INTR_vector   : out std_logic_vector(ADDR_WIDTH-1 downto 0));
    end component;


    signal n_instr     : std_logic_vector (INST_WIDTH-1 downto 0);
    signal ni_get_done : std_logic;

    signal IF_RegBr    : std_logic;
    signal IF_Br       : std_logic;
    signal IF_BrUnc    : std_logic;
    signal IF_BrShort  : std_logic;
    signal IF_BrOffset : std_logic_vector (IF_IMM_WIDTH-1 downto 0);

    signal INTR_pend   : std_logic;
    signal INTR_swi    : std_logic;
    signal INTR_sstep  : std_logic;
    signal INTR_extrn  : std_logic;
    signal INTR_timer  : std_logic;
    signal INTR_reset  : std_logic;
    signal INTR_buserr : std_logic;
    signal INTR_eistat : std_logic_vector(NUM_INTRS-1 downto 0);
    signal INTR_vector : std_logic_vector (ADDR_WIDTH-1 downto 0);

    signal IAGU_pc       : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal IAGU_ni_addr  : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal IAGU_anull    : std_logic;
    signal IAGU_ifclr    : std_logic;
    signal IAGU_intr     : std_logic;
    signal IAGU_deregbr  : std_logic;
    signal IAGU_rfregbr  : std_logic;
    signal IAGU_exbr     : std_logic;
    signal IAGU_exbrlong : std_logic;
    signal IAGU_exregbr  : std_logic;
    signal IAGU_syncif   : std_logic;
    signal IAGU_Nxif     : std_logic;
    signal IAGU_debug    : std_logic_vector(3 downto 0);

    signal PCT_sdly     : std_logic;
    signal PCT_msustall : std_logic;
    signal PCT_stall    : std_logic;
    signal PCT_ustall   : std_logic;

    signal DE_load      : std_logic;
    signal DE_store     : std_logic;
    signal DE_half      : std_logic;
    signal DE_byte      : std_logic;
    signal DE_word      : std_logic;
    signal DE_iscond    : std_logic;
    signal DE_condT     : std_logic;
    signal DE_Br        : std_logic;
    signal DE_BrUnc     : std_logic;
    signal DE_BrShort   : std_logic;
    signal DE_imm       : std_logic_vector (DE_IMM_WIDTH-1 downto 0);
    signal DE_immSigned : std_logic;
    signal DE_immSHL    : std_logic_vector (1 downto 0);
    signal DE_immAlign4 : std_logic;
    signal DE_rd        : std_logic_vector (3 downto 0);
    signal DE_rb        : std_logic_vector (3 downto 0);
    signal DE_asel      : std_logic_vector (1 downto 0);
    signal DE_bsel      : std_logic_vector (1 downto 0);
    signal DE_flagop    : std_logic_vector (2 downto 0);
    signal DE_pc        : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal DE_rfwe      : std_logic;
    signal DE_sfrwe     : std_logic;
    signal DE_swi       : std_logic;
    signal DE_cywe      : std_logic;
    signal DE_tfwe      : std_logic;
    signal DE_BrLink    : std_logic;
    signal DE_aluop     : std_logic_vector (3 downto 0);
    signal DE_alu       : std_logic;
    signal DE_rstregs   : std_logic;
    signal DE_uinst     : std_logic;
    signal DE_sstep     : std_logic;
    signal DE_ivalid    : std_logic;
    signal DE_uiop      : std_logic_vector (1 downto 0);

    signal RF_load   : std_logic;
    signal RF_store  : std_logic;
    signal RF_half   : std_logic;
    signal RF_byte   : std_logic;
    signal RF_word   : std_logic;
    signal RF_iscond : std_logic;
    signal RF_condT  : std_logic;
    signal RF_Br     : std_logic;
    signal RF_BrUnc  : std_logic;
    signal RF_imm    : std_logic_vector (RF_IMM_WIDTH-1 downto 0);
    signal RF_asel   : std_logic_vector (1 downto 0);
    signal RF_bsel   : std_logic_vector (1 downto 0);
    signal RF_rdval  : std_logic_vector (WIDTH-1 downto 0);
    signal RF_rbval  : std_logic_vector (WIDTH-1 downto 0);
    signal RF_pc     : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal RF_rfwe   : std_logic;
    signal RF_sfrwe  : std_logic;
    signal RF_swi    : std_logic;
    signal RF_swinum : std_logic_vector (SWINUM_WIDTH-1 downto 0);
    signal RF_cywe   : std_logic;
    signal RF_tfwe   : std_logic;
    signal RF_aluop  : std_logic_vector (3 downto 0);
    signal RF_rd     : std_logic_vector (3 downto 0);
    signal RF_rb     : std_logic_vector (3 downto 0);
    signal RF_flagop : std_logic_vector (2 downto 0);
    signal RF_alu    : std_logic;
    signal RF_lsu    : std_logic;
    signal RF_msu    : std_logic;
    signal RF_rfi    : std_logic;
    signal RF_uinst  : std_logic;
    signal RF_sstep  : std_logic;
    signal RF_ivalid : std_logic;
    signal RF_uiop   : std_logic_vector (1 downto 0);
    signal RF_shift  : std_logic;
    signal RF_lsmsu  : std_logic;
    signal RF_dreg   : std_logic;
    signal RF_bphit  : std_logic;

    signal SFR_tstart    : std_logic;
    signal SFR_tfflag    : std_logic;
    signal SFR_cyflag    : std_logic;
    signal SFR_ieflag    : std_logic;
    signal SFR_ipflag    : std_logic;
    signal SFR_teflag    : std_logic;
    signal SFR_swflag    : std_logic;
    signal SFR_tiflag    : std_logic;
    signal SFR_eiflag    : std_logic;
    signal SFR_pdflag    : std_logic;
    signal SFR_bdflag    : std_logic;
    signal SFR_iiflag    : std_logic;
    signal SFR_trload    : std_logic;
    signal SFR_ssflag    : std_logic;
    signal SFR_sspsw     : std_logic;
    signal SFR_wp_enb    : std_logic_vector(1 downto 0);
    signal SFR_bp_enb    : std_logic_vector(1 downto 0);
    signal SFR_wp0       : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal SFR_wp1       : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal SFR_bp0       : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal SFR_bp1       : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal SFR_ibase     : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal SFR_eienb     : std_logic_vector (NUM_INTRS-1 downto 0);
    signal SFR_sfrval    : std_logic_vector (WIDTH-1 downto 0);
    signal SFR_trval     : std_logic_vector (TIMER_WIDTH-1 downto 0);

    signal EX_wphit   : std_logic_vector (1 downto 0);
    signal EX_bphit   : std_logic;
    signal EX_load    : std_logic;
    signal EX_store   : std_logic;
    signal EX_lsu     : std_logic;
    signal EX_rdval   : std_logic_vector (WIDTH-1 downto 0);
    signal EX_rbval   : std_logic_vector (WIDTH-1 downto 0);
    signal EX_opb     : std_logic_vector (WIDTH-1 downto 0);
    signal EX_out     : std_logic_vector (WIDTH-1 downto 0);
    signal EX_sfrwe   : std_logic;
    signal EX_cyflag  : std_logic;
    signal EX_tfflag  : std_logic;
    signal EX_cywe    : std_logic;
    signal EX_tfwe    : std_logic;
    signal EX_addr    : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal EX_st_data : std_logic_vector (WIDTH-1 downto 0);
    signal EX_sfrreg  : std_logic_vector (3 downto 0);
    signal EX_sfrval  : std_logic_vector (WIDTH-1 downto 0);
    signal EX_mip     : std_logic;
    signal EX_uip     : std_logic;
    signal EX_ben     : std_logic_vector (3 downto 0);
    signal EX_mispred : std_logic;
    signal EX_dbghit  : std_logic;
    signal EX_ivalid  : std_logic;
    signal EX_addout  : std_logic_vector (WIDTH-1 downto 0);

    signal MEM_dbus      : std_logic_vector (WIDTH-1 downto 0);
    signal MEM_ld_done   : std_logic;
    signal MEM_st_done   : std_logic;
    signal MEM_dfetch_ip : std_logic;
    signal MEM_ifetch_ip : std_logic;
    signal MEM_Nbusy     : std_logic;
    signal MEM_id_buserr : std_logic_vector(1 downto 0);

    signal MULT_mdone : std_logic;
    signal MULT_out   : std_logic_vector (WIDTH-1 downto 0);
    signal MULT_rd    : std_logic_vector (3 downto 0);

    signal WB_data_out : std_logic_vector (WIDTH-1 downto 0);
    signal WB_rd       : std_logic_vector (3 downto 0);
    signal WB_rfwe     : std_logic;
    signal WB_stall    : std_logic;
    signal WB_ldwbreg  : std_logic_vector (3 downto 0);
    signal WB_ldreg    : std_logic_vector (3 downto 0);
    signal WB_uireg    : std_logic_vector (3 downto 0);
    signal WB_rdeq     : std_logic;
    signal WB_rbeq     : std_logic;

    signal EXM_en_debug    : std_logic;
    signal EXM_valid_debug : std_logic;
    signal EXM_data_in     : std_logic_vector (WIDTH-1 downto 0)      := (others => '0');
    signal EXM_rd_done     : std_logic                                := '0';
    signal EXM_wr_done     : std_logic                                := '0';
    signal EXM_data_out    : std_logic_vector (WIDTH-1 downto 0)      := (others => '0');
    signal EXM_ben         : std_logic_vector (3 downto 0)            := (others => '0');
    signal EXM_rd          : std_logic                                := '0';
    signal EXM_wr          : std_logic                                := '0';
    signal EXM_rwreq       : std_logic                                := '0';
    signal EXM_daddr       : std_logic_vector (ADDR_WIDTH-1 downto 0) := (others => '0');

    signal UINST_lout : std_logic_vector(WIDTH-1 downto 0);

    signal TIMER_exp  : std_logic;
    signal TIMER_undf : std_logic;
    signal TIMER_val  : std_logic_vector (TIMER_WIDTH-1 downto 0);

    for all : multshift use entity work.multshift(RTL);
    
--    attribute RLOC of IntrCntrl_inst : label is rloc_string(BASE_ROW,BASE_COL+2);
    attribute RLOC of timer_inst     : label is rloc_string(BASE_ROW,BASE_COL+2,true,0,4);
    attribute RLOC of ifdeunit_inst  : label is rloc_string(BASE_ROW,BASE_COL+2,true,0,6);
    attribute RLOC of iagunit_inst   : label is rloc_string(BASE_ROW,BASE_COL+3,true,0,5);
--    attribute RLOC of rfunit_inst    : label is rloc_string(BASE_ROW,BASE_COL+4,true,0,3);
    attribute RLOC of exunit_inst    : label is rloc_string(BASE_ROW,BASE_COL+7,true,0,13);
--    attribute RLOC of pipectrl_inst  : label is rloc_string((WIDTH/2),BASE_COL+9,true,-(WIDTH/2)-1,12);
--    attribute RLOC of multshift_inst : label is rloc_string(BASE_ROW,BASE_COL+12,true,0,12);
    attribute RLOC of sfrs_inst      : label is rloc_string((WIDTH/2),BASE_COL+12,true,0,5);    
--    attribute RLOC of ucache_inst    : label is rloc_string(BASE_ROW,BASE_COL);
    
begin  -- Behavioral

    EXM_rwreq <= EXM_valid_debug;
    INTR_ack  <= SFR_eiflag ;
    
-- synopsys translate_off    
    debug_gen_True : if DEBUGCORE = True generate
    begin
        debug_data_addr <= EXM_daddr;
        debug_data_wr   <= EXM_wr;
        debug_data_rd   <= EXM_rd;
        debug_data 	<= EXM_data_out;
        debug_rf_data   <= WB_data_out;
        debug_rf_rd	<= WB_rd;
        debug_rf_wr     <= WB_rfwe;        

        DW_80: if DEBUG_WIDTH = 80 generate
            debug_out (WIDTH-1 downto 0)                          <= WB_data_out;
            debug_out (WIDTH + 3 downto WIDTH)                    <= WB_rd;
            debug_out (WIDTH + 4)                                 <= WB_rfwe;
            debug_out (WIDTH + 5)                                 <= PCT_stall;
            debug_out (WIDTH + 6)                                 <= IAGU_anull;
            debug_out (WIDTH + 7)                                 <= INTR_pend;
            debug_out (WIDTH + 8)                                 <= INTR_extrn;
            debug_out (WIDTH + 9)                                 <= INTR_timer;
            debug_out (WIDTH + 10)                                <= INTR_swi;
            debug_out (WIDTH + ADDR_WIDTH + 10 downto WIDTH + 11) <= sfr_expc;  -- from manikpackage
            debug_out (WIDTH + ADDR_WIDTH + 11)                   <= EX_load;
            debug_out (WIDTH + ADDR_WIDTH + 12)                   <= EX_store;
            debug_out (WIDTH + ADDR_WIDTH + 13)                   <= EXM_rd_done;
            debug_out (WIDTH + ADDR_WIDTH + 14)                   <= EX_tfwe;
            debug_out (WIDTH + ADDR_WIDTH + 15)                   <= EX_cywe;
        end generate DW_80;
        DW_63: if DEBUG_WIDTH = 63 generate
            constant ADDR_WIDTH : integer := 15;
        begin
            debug_out (WIDTH-1 downto 0)                          <= WB_data_out; --32            
            debug_out (WIDTH + 3 downto WIDTH)                    <= WB_rd;       --4
            debug_out (WIDTH + 4)                                 <= WB_rfwe;     --1
            debug_out (WIDTH + 5)                                 <= PCT_stall;   --1
            debug_out (WIDTH + 6)                                 <= IAGU_anull;  --1
            debug_out (WIDTH + 7)                                 <= INTR_pend;   --1
            debug_out (WIDTH + 8)                                 <= INTR_extrn;  --1
            debug_out (WIDTH + 9)                                 <= INTR_timer;  --1
            debug_out (WIDTH + 10)                                <= INTR_swi;    --1
            debug_out (WIDTH + ADDR_WIDTH + 10 downto WIDTH + 11) <= sfr_expc(ADDR_WIDTH-1 downto 0);  -- from manikpackage
            debug_out (WIDTH + ADDR_WIDTH + 11)                   <= EX_load;     --1
            debug_out (WIDTH + ADDR_WIDTH + 12)                   <= EX_store;    --1
            debug_out (WIDTH + ADDR_WIDTH + 13)                   <= EXM_rd_done; --1
            debug_out (WIDTH + ADDR_WIDTH + 14)                   <= EX_tfwe;     --1
            debug_out (WIDTH + ADDR_WIDTH + 15)                   <= EX_cywe;     --1       
        end generate DW_63;
    end generate debug_gen_True;

    debug_gen_false: if DEBUGCORE = False generate
        debug_out <= (others => '0');
    end generate debug_gen_false;
-- synopsys translate_on

    ila_inst: if Technology = "XILINX-" generate
        component icon
            port (control0 : out std_logic_vector(35 downto 0)); 
        end component;
        component ila
            port (control : in std_logic_vector(35 downto 0);
                  clk     : in std_logic;
                  trig0   : in std_logic_vector(143 downto 0));
        end component;
        
        signal control0 : std_logic_vector(35 downto 0);
        signal trig0   : std_logic_vector(143 downto 0);
        
        attribute syn_black_box         : boolean;
        attribute syn_noprune           : boolean;
        attribute syn_black_box of ila  : component is TRUE;
        attribute syn_noprune of ila    : component is TRUE;
        attribute syn_black_box of icon : component is TRUE;
        attribute syn_noprune of icon   : component is TRUE;
    begin
        icon_1: icon
            port map (control0 => control0);
        ila_1: ila
            port map (control => control0, clk => clk,
                      trig0   => trig0);
        
        trig0 (WIDTH-1 downto 0)                          <= WB_data_out;
        trig0 (WIDTH + 3 downto WIDTH)                    <= WB_rd;
        trig0 (WIDTH + 4)                                 <= WB_rfwe;
        trig0 (WIDTH + 5)                                 <= PCT_stall;
        trig0 (WIDTH + 6)                                 <= IAGU_anull;
        trig0 (WIDTH + 7)                                 <= INTR_pend;
        trig0 (WIDTH + 8)                                 <= WBM_ERR_I;
        trig0 (WIDTH + 9)                                 <= INTR_sstep;
        trig0 (WIDTH + 10)                                <= SFR_ipflag;
        trig0 (WIDTH + ADDR_WIDTH + 10 downto WIDTH + 11)<= IAGU_pc;
        trig0 (WIDTH + ADDR_WIDTH + 11)                   <= INTR_reset;
        trig0 (WIDTH + ADDR_WIDTH + 12)                   <= TIMER_exp;
        trig0 (WIDTH + ADDR_WIDTH + 13)                   <= INTR_timer;
        trig0 (WIDTH + ADDR_WIDTH + 14)                   <= INTR_swi;
        trig0 (WIDTH + ADDR_WIDTH + 15)                   <= SFR_tiflag;
        trig0 (2*WIDTH + ADDR_WIDTH + 15 downto WIDTH + ADDR_WIDTH + 16) <= SFR_trval;
        trig0 (2*WIDTH + 2*ADDR_WIDTH + 15 downto 2*WIDTH + ADDR_WIDTH + 16) <= EXM_data_in;
    end generate ila_inst;

    ---------------------------------------------------------------------------
    --                             USER Instruction 
    ---------------------------------------------------------------------------
    UI_UNIT: if USER_INST = True generate
        UINST_uinst     <= RF_uinst;
        UINST_uiop      <= RF_uiop;
        UINST_uiopA     <= EX_rdval(UINST_WIDTH-1 downto 0);
        UINST_uiopB     <= EX_rbval(UINST_WIDTH-1 downto 0);
        UINST_Nce       <= PCT_stall;
        UINST_outassign: for i in 0 to WIDTH-1 generate
            gt: if i > UINST_WIDTH-1 generate
                UINST_lout(i) <= '0';
            end generate gt;
            lteq: if i <= UINST_WIDTH-1 generate
                UINST_lout(i) <= UINST_out(i);
            end generate lteq;
        end generate UINST_outassign;
    end generate UI_UNIT;
    NOUI_UNIT: if USER_INST = False generate
        UINST_uinst     <= '0';
        UINST_uiop      <= (others => '0');
        UINST_uiopA     <= (others => '0');
        UINST_uiopB     <= (others => '0');
        UINST_Nce       <= '0';
    end generate NOUI_UNIT;

    ---------------------------------------------------------------------------
    --  Translate internal signal to Wishbone compliant signal
    ---------------------------------------------------------------------------

    EXM_data_in <= WBM_DAT_I;
    EXM_wr_done <= (WBM_ACK_I or WBM_ERR_I) and EXM_wr;
    EXM_rd_done <= (WBM_ACK_I or WBM_ERR_I) and EXM_rd;

    WBM_DAT_O  <= EXM_data_out;
    WBM_SEL_O  <= EXM_ben;
    WBM_WE_O   <= EXM_wr;
    WBM_STB_O  <= EXM_rwreq;
    WBM_CYC_O  <= EXM_rwreq;
    WBM_LOCK_O <= '0';
    WBM_ADR_O  <= EXM_daddr;
    WBM_CTI_O  <= "000";
    WBM_BTE_O  <= "00";
        
    ucache_inst: ucache
        generic map (WIDTH             => WIDTH,
                     ICACHE_ENABLED    => ICACHE_ENABLED,
                     DCACHE_ENABLED    => DCACHE_ENABLED,
                     ICACHE_LINE_WORDS => ICACHE_LINE_WORDS,
                     DCACHE_LINE_WORDS => DCACHE_LINE_WORDS,
                     ICACHE_SETS       => ICACHE_SETS,
                     DCACHE_SETS       => DCACHE_SETS,
                     ICACHE_ADDR_WIDTH => ICACHE_ADDR_WIDTH,
                     DCACHE_ADDR_WIDTH => DCACHE_ADDR_WIDTH)
        port map (daddr         => EX_addout,
                  data_in       => EX_st_data,
                  EXM_data_in   => EXM_data_in,
                  EXM_rd_done   => EXM_rd_done,
                  EXM_wr_done   => EXM_wr_done,
                  EXM_buserr	=> WBM_ERR_I,
                  benA          => EX_ben,
                  EX_load       => EX_load,
                  EX_store      => EX_store,
                  EX_mispred    => EX_mispred,
                  RF_lsu        => RF_lsu,
                  RF_load       => RF_load,
                  clk           => clk,
                  iaddr         => IAGU_ni_addr,
                  IAGU_ifclr    => IAGU_ifclr,
                  IAGU_Nxif     => IAGU_Nxif,
                  IAGU_syncif   => IAGU_syncif,
                  IAGU_pc       => IAGU_pc,                      
                  SFR_bdflag    => SFR_bdflag,
                  SFR_iiflag    => SFR_iiflag,
                  INTR_pend     => INTR_pend,
                  INTR_reset    => INTR_reset,
                  PCT_stall     => PCT_stall,
                  EXM_data_out  => EXM_data_out,
                  EXM_ben       => EXM_ben,
                  EXM_rd        => EXM_rd,
                  EXM_wr        => EXM_wr,
                  EXM_rwreq     => EXM_valid_debug,
                  EXM_daddr     => EXM_daddr,
                  MEM_dfetch_ip => MEM_dfetch_ip,
                  MEM_ifetch_ip => MEM_ifetch_ip,
                  MEM_id_buserr => MEM_id_buserr,
                  ld_done       => MEM_ld_done,
                  st_done       => MEM_st_done,
                  MEM_Nbusy     => MEM_Nbusy,
                  data_out      => MEM_dbus,
                  ni_get_done   => ni_get_done,
                  instr         => n_instr);

    iagunit_inst : iagunit
        generic map (WIDTH => WIDTH)
        port map (IF_RegBr      => IF_RegBr,
                  IF_Br         => IF_Br,
                  IF_BrUnc      => IF_BrUnc,
                  IF_BrShort    => IF_BrShort,
                  IF_BrOffset   => IF_BrOffset,
                  IF_BrOffset_n => n_instr,
                  EX_addr       => EX_addr,
                  EX_opb        => EX_opb(ADDR_WIDTH-1 downto 0),
                  EX_mispred    => EX_mispred,
                  EX_mip        => EX_mip,
                  EX_uip        => EX_uip,
                  EX_dbghit     => EX_dbghit,
                  DE_pc         => DE_pc,
                  DE_rstregs    => DE_rstregs,
                  DE_swi        => DE_swi,
                  DE_sstep      => DE_sstep,
                  DE_ivalid     => DE_ivalid,
                  RF_swi        => RF_swi,
                  RF_sstep      => RF_sstep,
                  INTR_pend     => INTR_pend,
                  INTR_reset    => INTR_reset,
                  INTR_vector   => INTR_vector,
                  PCT_sdly      => PCT_sdly,
                  MEM_ifetch_ip => MEM_ifetch_ip,
                  stall         => PCT_stall,
                  clk           => clk,
                  IAGU_debug    => IAGU_debug,
                  IAGU_pc       => IAGU_pc,
                  IAGU_ni_addr  => IAGU_ni_addr,
                  IAGU_anull    => IAGU_anull,
                  IAGU_ifclr    => IAGU_ifclr,
                  IAGU_Nxif     => IAGU_Nxif,
                  IAGU_intr     => IAGU_intr,
                  IAGU_deregbr  => IAGU_deregbr,
                  IAGU_rfregbr  => IAGU_rfregbr,
                  IAGU_exbr     => IAGU_exbr,
                  IAGU_exbrlong => IAGU_exbrlong,
                  IAGU_exregbr  => IAGU_exregbr,
                  IAGU_syncif   => IAGU_syncif);
    
    IFDEunit_inst : IFDEunit
        generic map (WIDTH     => WIDTH,
                     USER_INST => USER_INST)
        port map (ni_get_done  => ni_get_done,
                  n_instr      => n_instr,
                  IAGU_anull   => IAGU_anull,
                  IAGU_deregbr => IAGU_deregbr,
                  IAGU_rfregbr => IAGU_rfregbr,
                  IAGU_pc      => IAGU_pc,
                  PCT_stall    => PCT_stall,
                  SFR_ssflag   => SFR_ssflag,
                  clk          => clk,
                  IF_RegBr     => IF_RegBr,
                  IF_Br        => IF_Br,
                  IF_BrUnc     => IF_BrUnc,
                  IF_BrShort   => IF_BrShort,
                  IF_BrOffset  => IF_BrOffset,
                  DE_load      => DE_load,
                  DE_store     => DE_store,
                  DE_half      => DE_half,
                  DE_byte      => DE_byte,
                  DE_word      => DE_word,
                  DE_iscond    => DE_iscond,
                  DE_condT     => DE_condT,
                  DE_Br        => DE_Br,
                  DE_BrUnc     => DE_BrUnc,
                  DE_BrShort   => DE_BrShort,
                  DE_imm       => DE_imm,
                  DE_immSigned => DE_immSigned,
                  DE_immSHL    => DE_immSHL,
                  DE_immAlign4 => DE_immAlign4,
                  DE_rd        => DE_rd,
                  DE_rb        => DE_rb,
                  DE_asel      => DE_asel,
                  DE_bsel      => DE_bsel,
                  DE_flagop    => DE_flagop,
                  DE_pc        => DE_pc,
                  DE_rfwe      => DE_rfwe,
                  DE_sfrwe     => DE_sfrwe,
                  DE_swi       => DE_swi,
                  DE_cywe      => DE_cywe,
                  DE_tfwe      => DE_tfwe,
                  DE_BrLink    => DE_BrLink,
                  DE_aluop     => DE_aluop,
                  DE_alu       => DE_alu,
                  DE_rstregs   => DE_rstregs,
                  DE_uinst     => DE_uinst,
                  DE_sstep     => DE_sstep,
                  DE_ivalid    => DE_ivalid,
                  DE_uiop      => DE_uiop);
    
    RFunit_inst : RFunit
        generic map (WIDTH     => WIDTH,
                     USER_INST => USER_INST,
                     HW_BPENB  => HW_BPENB)
        port map (DE_load      => DE_load,
                  DE_store     => DE_store,
                  DE_half      => DE_half,
                  DE_byte      => DE_byte,
                  DE_word      => DE_word,
                  DE_iscond    => DE_iscond,
                  DE_condT     => DE_condT,
                  DE_Br        => DE_Br,
                  DE_BrUnc     => DE_BrUnc,
                  DE_imm       => DE_imm,
                  DE_immSigned => DE_immSigned,
                  DE_immSHL    => DE_immSHL,
                  DE_immAlign4 => DE_immAlign4,
                  DE_rd        => DE_rd,
                  DE_rb        => DE_rb,
                  DE_asel      => DE_asel,
                  DE_bsel      => DE_bsel,
                  DE_flagop    => DE_flagop,
                  DE_pc        => DE_pc,
                  DE_rfwe      => DE_rfwe,
                  DE_sfrwe     => DE_sfrwe,
                  DE_swi       => DE_swi,
                  DE_cywe      => DE_cywe,
                  DE_tfwe      => DE_tfwe,
                  DE_BrLink    => DE_BrLink,
                  DE_aluop     => DE_aluop,
                  DE_alu       => DE_alu,
                  DE_uinst     => DE_uinst,
                  DE_sstep     => DE_sstep,
                  DE_ivalid    => DE_ivalid,
                  DE_uiop      => DE_uiop,
                  SFR_bp_enb   => SFR_bp_enb,
                  SFR_bp0      => SFR_bp0,
                  SFR_bp1      => SFR_bp1,
                  IAGU_anull   => IAGU_anull,
                  IAGU_deregbr => IAGU_deregbr,
                  PCT_stall    => PCT_stall,
                  WB_data_out  => WB_data_out,
                  WB_rd        => WB_rd,
                  WB_rdeq      => WB_rdeq,
                  WB_rbeq      => WB_rbeq,
                  WB_rfwe      => WB_rfwe,
                  clk          => clk,
                  RF_load      => RF_load,
                  RF_store     => RF_store,
                  RF_half      => RF_half,
                  RF_byte      => RF_byte,
                  RF_word      => RF_word,
                  RF_iscond    => RF_iscond,
                  RF_condT     => RF_condT,
                  RF_Br        => RF_Br,
                  RF_BrUnc     => RF_BrUnc,
                  RF_imm       => RF_imm,
                  RF_asel      => RF_asel,
                  RF_bsel      => RF_bsel,
                  RF_rdval     => RF_rdval,
                  RF_rbval     => RF_rbval,
                  RF_pc        => RF_pc,
                  RF_rfwe      => RF_rfwe,
                  RF_sfrwe     => RF_sfrwe,
                  RF_swi       => RF_swi,
                  RF_swinum    => RF_swinum,
                  RF_cywe      => RF_cywe,
                  RF_tfwe      => RF_tfwe,
                  RF_aluop     => RF_aluop,
                  RF_rd        => RF_rd,
                  RF_rb        => RF_rb,
                  RF_flagop    => RF_flagop,
                  RF_alu       => RF_alu,
                  RF_lsu       => RF_lsu,
                  RF_msu       => RF_msu,
                  RF_rfi       => RF_rfi,
                  RF_uinst     => RF_uinst,
                  RF_sstep     => RF_sstep,
                  RF_ivalid    => RF_ivalid,
                  RF_uiop      => RF_uiop,
                  RF_shift     => RF_shift,
                  RF_lsmsu     => RF_lsmsu,
                  RF_dreg      => RF_dreg,
                  RF_bphit     => RF_bphit);

    EXunit_inst : EXunit
        generic map (WIDTH     => WIDTH,
                     HW_BPENB  => HW_BPENB,
                     HW_WPENB  => HW_WPENB,
                     USER_INST => USER_INST)
        port map (RF_load     => RF_load,
                  RF_store    => RF_store,
                  RF_half     => RF_half,
                  RF_byte     => RF_byte,
                  RF_word     => RF_word,
                  RF_iscond   => RF_iscond,
                  RF_condT    => RF_condT,
                  RF_Br       => RF_Br,
                  RF_BrUnc    => RF_BrUnc,
                  RF_imm      => RF_imm,
                  RF_asel     => RF_asel,
                  RF_bsel     => RF_bsel,
                  RF_rdval    => RF_rdval,
                  RF_rbval    => RF_rbval,
                  RF_pc       => RF_pc,
                  RF_rfwe     => RF_rfwe,
                  RF_sfrwe    => RF_sfrwe,
                  RF_swi      => RF_swi,
                  RF_cywe     => RF_cywe,
                  RF_tfwe     => RF_tfwe,
                  RF_aluop    => RF_aluop,
                  RF_rd       => RF_rd,
                  RF_rb       => RF_rb,
                  RF_flagop   => RF_flagop,
                  RF_alu      => RF_alu,
                  RF_msu      => RF_msu,
                  RF_uinst    => RF_uinst,
                  RF_sstep    => RF_sstep,
                  RF_ivalid   => RF_ivalid,
                  RF_bphit    => RF_bphit,
                  PCT_stall   => PCT_stall,
                  PCT_ustall  => PCT_ustall,
                  IAGU_anull  => IAGU_anull,
                  INTR_reset  => INTR_reset,
                  MULT_mdone  => MULT_mdone,
                  MULT_out    => MULT_out,
                  MULT_rd     => MULT_rd,
                  UINST_uip   => UINST_uip,
                  UINST_out   => UINST_lout,
                  SFR_tfflag  => SFR_tfflag,
                  SFR_cyflag  => SFR_cyflag,
                  SFR_sfrval  => SFR_sfrval,
                  SFR_wp_enb  => SFR_wp_enb,
                  SFR_wp0     => SFR_wp0,
                  SFR_wp1     => SFR_wp1,
                  clk         => clk,
                  MEM_dbus    => MEM_dbus,
                  MEM_ld_done => MEM_ld_done,
                  MEM_st_done => MEM_st_done,
                  MEM_Nbusy   => MEM_Nbusy,
                  EX_wphit    => EX_wphit,                  
                  EX_bphit    => EX_bphit,                  
                  EX_load     => EX_load,
                  EX_store    => EX_store,
                  EX_lsu      => EX_lsu,
                  EX_rdval    => EX_rdval,
                  EX_rbval    => EX_rbval,
                  EX_opb      => EX_opb,
                  EX_out      => EX_out,
                  EX_sfrwe    => EX_sfrwe,
                  EX_cyflag   => EX_cyflag,
                  EX_tfflag   => EX_tfflag,
                  EX_cywe     => EX_cywe,
                  EX_tfwe     => EX_tfwe,
                  EX_addr     => EX_addr,
                  EX_st_data  => EX_st_data,
                  EX_sfrreg   => EX_sfrreg,
                  EX_sfrval   => EX_sfrval,
                  EX_mip      => EX_mip,
                  EX_uip      => EX_uip,
                  EX_ben      => EX_ben,
                  EX_uwbc     => UINST_wbc,
                  EX_mispred  => EX_mispred,
                  EX_dbghit   => EX_dbghit,
                  EX_ivalid   => EX_ivalid,
                  EX_addout   => EX_addout,
                  WB_data_out => WB_data_out,
                  WB_rd       => WB_rd,
                  WB_rfwe     => WB_rfwe,
                  WB_stall    => WB_stall,
                  WB_ldwbreg  => WB_ldwbreg,
                  WB_ldreg    => WB_ldreg,
                  WB_uireg    => WB_uireg,
                  WB_rdeq     => WB_rdeq,
                  WB_rbeq     => WB_rbeq);

    sfrs_inst : sfrs
        generic map (WIDTH         => WIDTH,
                     INTR_VECBASE  => INTR_VECBASE,
                     HW_WPENB	   => HW_WPENB,
                     HW_BPENB	   => HW_BPENB,
                     USREG_ENABLED => USREG_ENABLED,
                     TIMER_WIDTH   => TIMER_WIDTH)
        port map (EX_sfrreg     => EX_sfrreg,
                  EX_sfrval     => EX_sfrval,
                  EX_sfrwe      => EX_sfrwe,
                  EX_cyflag     => EX_cyflag,
                  EX_tfflag     => EX_tfflag,
                  EX_cywe       => EX_cywe,
                  EX_tfwe       => EX_tfwe,
                  EX_mispred    => EX_mispred,
                  EX_dbghit     => EX_dbghit,
                  EX_wphit	=> EX_wphit,
                  EX_bphit	=> EX_bphit,
                  IAGU_pc       => IAGU_pc,
                  IAGU_deregbr  => IAGU_deregbr,
                  IAGU_exbr     => IAGU_exbr,
                  IAGU_exbrlong => IAGU_exbrlong,
                  IAGU_exregbr  => IAGU_exregbr,
                  IAGU_intr     => IAGU_intr,
                  IAGU_anull    => IAGU_anull,
                  PCT_sdly      => PCT_sdly,
                  PCT_stall     => PCT_stall,
                  RF_pc         => RF_pc,
                  RF_rfi        => RF_rfi,
                  RF_swinum     => RF_swinum,
                  DE_pc         => DE_pc,
                  DE_BrLink     => DE_BrLink,
                  DE_rb         => DE_rb,
                  DE_sstep      => DE_sstep,
                  clk           => clk,
                  INTR_swi      => INTR_swi,
                  INTR_sstep    => INTR_sstep,
                  INTR_timer    => INTR_timer,
                  INTR_extrn    => INTR_extrn,
                  INTR_reset    => INTR_reset,
                  INTR_buserr   => INTR_buserr,
                  INTR_eistat   => INTR_eistat,
                  TIMER_exp     => TIMER_exp,
                  TIMER_undf    => TIMER_undf,
                  TIMER_val     => TIMER_val,
                  MEM_id_buserr => MEM_id_buserr,
                  SFR_tstart    => SFR_tstart,
                  SFR_tfflag    => SFR_tfflag,
                  SFR_cyflag    => SFR_cyflag,
                  SFR_ieflag    => SFR_ieflag,
                  SFR_ipflag    => SFR_ipflag,
                  SFR_teflag    => SFR_teflag,
                  SFR_swflag    => SFR_swflag,
                  SFR_tiflag    => SFR_tiflag,
                  SFR_eiflag    => SFR_eiflag,
                  SFR_pdflag    => SFR_pdflag,
                  SFR_bdflag    => SFR_bdflag,
                  SFR_iiflag    => SFR_iiflag,
                  SFR_trload    => SFR_trload,
                  SFR_ssflag    => SFR_ssflag,
                  SFR_sspsw     => SFR_sspsw,
                  SFR_wp_enb    => SFR_wp_enb,
                  SFR_bp_enb    => SFR_bp_enb,
                  SFR_wp0       => SFR_wp0,
                  SFR_wp1       => SFR_wp1,
                  SFR_bp0       => SFR_bp0,
                  SFR_bp1       => SFR_bp1,
                  SFR_ibase	=> SFR_ibase,
                  SFR_eienb     => SFR_eienb,
                  SFR_sfrval    => SFR_sfrval,
                  SFR_trval     => SFR_trval);

    multshift_inst : multshift
        generic map (WIDTH        => WIDTH,
                     SHIFT_SWIDTH => SHIFT_SWIDTH,
                     MULT_BWIDTH  => MULT_BWIDTH)
        port map (RF_msu     => RF_msu,
                  RF_shift   => RF_shift,
                  RF_shcode  => RF_aluop(1 downto 0),
                  clk        => clk,
                  INTR_reset => INTR_reset,
                  msopa      => EX_rdval,
                  msopb      => EX_opb,
                  stall      => PCT_msustall,
                  RF_rd      => RF_rd,
                  MULT_mdone => MULT_mdone,
                  MULT_out   => MULT_out,
                  MULT_rd    => MULT_rd);

    pipectrl_inst : pipectrl
        generic map (WIDTH     => WIDTH,
                     USER_INST => USER_INST)
        port map (WB_stall      => WB_stall,
                  WB_ldwbreg    => WB_ldwbreg,
                  WB_ldreg      => WB_ldreg,
                  WB_uireg      => WB_uireg,
                  EX_mip        => EX_mip,
                  EX_uip        => EX_uip,
                  RF_load       => RF_load,
                  RF_store      => RF_store,
                  RF_rfwe       => RF_rfwe,
                  RF_dreg       => RF_dreg,
                  RF_breg       => RF_bsel(1),
                  RF_rd         => RF_rd,
                  RF_rb         => RF_rb,
                  RF_msu        => RF_msu,
                  RF_uinst      => RF_uinst,
                  RF_lsmsu      => RF_lsmsu,
                  RF_lsu        => RF_lsu,
                  MULT_rd       => MULT_rd,
                  MEM_Nbusy     => MEM_Nbusy,
                  INTR_pend     => INTR_pend,
                  SFR_pdflag    => SFR_pdflag,
                  ni_get_done   => ni_get_done,
                  IAGU_syncif   => IAGU_syncif,
                  MEM_dfetch_ip => MEM_dfetch_ip,
                  MEM_ld_done   => MEM_ld_done,
                  clk           => clk,
                  PCT_sdly      => PCT_sdly,
                  PCT_msustall  => PCT_msustall,
                  PCT_stall     => PCT_stall,
                  PCT_ustall    => PCT_ustall);

    Timer_inst : Timer
        generic map (WIDTH         => WIDTH,
                     TIMER_WIDTH   => TIMER_WIDTH,
                     TIMER_CLK_DIV => TIMER_CLK_DIV)
        port map (SFR_teflag => SFR_teflag,
                  SFR_tiflag => SFR_tiflag,
                  SFR_tstart => SFR_tstart,
                  SFR_trload => SFR_trload,
                  SFR_trval  => SFR_trval,
                  clk        => clk,
                  TIMER_exp  => TIMER_exp,
                  TIMER_undf => TIMER_undf,
                  TIMER_val  => TIMER_val);

    IntrCntrl_inst : IntrCntrl
        generic map (WIDTH       => WIDTH,
                     INTR_SWIVEC => INTR_SWIVEC,
                     INTR_TMRVEC => INTR_TMRVEC,
                     INTR_EXTVEC => INTR_EXTVEC,
                     INTR_BERVEC => INTR_BERVEC)
        port map (TIMER_exp     => TIMER_exp,
                  EXTRN_int     => EXTRN_int,
                  RESET_int     => RESET_int,
                  BUSERR_int    => WBM_ERR_I,
                  IAGU_anull    => IAGU_anull,
                  MEM_ifetch_ip => MEM_ifetch_ip,
                  EX_mispred    => EX_mispred,
                  EX_dbghit     => EX_dbghit,
                  RF_swi        => RF_swi,
                  RF_ivalid     => RF_ivalid,
                  RF_pc         => RF_pc,
                  clk           => clk,
                  SFR_ieflag    => SFR_ieflag,
                  SFR_ipflag    => SFR_ipflag,
                  SFR_pdflag    => SFR_pdflag,
                  SFR_sspsw     => SFR_sspsw,
                  SFR_ibase	=> SFR_ibase,
                  SFR_eienb     => SFR_eienb,
                  INTR_pend     => INTR_pend,
                  INTR_swi      => INTR_swi,
                  INTR_sstep    => INTR_sstep,
                  INTR_extrn    => INTR_extrn,
                  INTR_timer    => INTR_timer,
                  INTR_reset    => INTR_reset,
                  INTR_eistat   => INTR_eistat,
                  INTR_buserr   => INTR_buserr,
                  INTR_vector   => INTR_vector);
end Behavioral;
