------------------------------------------------------------------------------
-- Title      : MANIK Generic Testbench has some peripherals
-- Project    : MANIK-II
-------------------------------------------------------------------------------
-- File       : manik_soc.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech
-- Last update: 2006-10-08
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: This is an example SOC using SRAM or SDRAM, UART and OnChip Sync
-- 		RAM + GPIO + ETHERNET MAC
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2004/09/06  1.0      Sandeep	Created
-------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;
use work.manikaltera.all;
use work.manikactel.all;
use work.maniklattice.all;

entity manik_soc is
    port (      
        -- System clock 
        clk_i     : in    std_logic;
        reset_i   : in    std_logic;
        
        -- signals to SRAM 
        sram_data : inout std_logic_vector (31 downto 0);
        sram_addr : out   std_logic_vector (CONFIG_RAM_ADDR_W-1 downto 0);
        sram_wen  : out   std_logic;
        sram_benN : out   std_logic_vector (3 downto 0);
        sram_csn  : out   std_logic_vector (1 downto 0);
        sram_oen  : out   std_logic;

        -- signals for FLASH
        flash_data : inout std_logic_vector (CONFIG_FLASH_DATA_W-1 downto 0);
        flash_addr : out   std_logic_vector (CONFIG_FLASH_ADDR_W-1 downto 0);
        flash_csn  : out   std_logic;
        flash_oen  : out   std_logic;
        flash_wen  : out   std_logic;
        flash_rpn  : out   std_logic;
        flash_bytn : out   std_logic;
        flash_a0   : out   std_logic;
        
        -- UART pins
        txpin     : out   std_logic;
        rxpin     : in    std_logic;

        -- ETHERNET MDII (Phy) Interface
        phy_resetn : out   std_logic;
        phy_mdio   : inout std_logic;
        phy_tx_clk : in    std_logic;
        phy_rx_clk : in    std_logic;
        phy_rxd    : in    std_logic_vector(3 downto 0);
        phy_rx_dv  : in    std_logic;
        phy_rx_er  : in    std_logic;
        phy_rx_col : in    std_logic;
        phy_rx_crs : in    std_logic;
        phy_txd    : out   std_logic_vector(3 downto 0);
        phy_tx_en  : out   std_logic;
        phy_tx_er  : out   std_logic;
        phy_mdc    : out   std_logic;
              
        -- LED Outputs
        seg_led   : out   std_logic_vector (7 downto 0));

end manik_soc;

architecture Behavioral of manik_soc is
    constant USE_SRAM : boolean := true;
    
    constant WIDTH         : integer := 32;
    
    constant ZDATA : std_logic_vector(WIDTH-1 downto 0) := (others => 'Z');
    
    component manik2top
        generic (WIDTH             : integer;
                 UINST_WIDTH       : integer;
                 TIMER_WIDTH       : integer;
                 TIMER_CLK_DIV     : integer;
                 INTR_VECBASE      : integer;
                 INTR_SWIVEC       : integer;
                 INTR_TMRVEC       : integer;
                 INTR_EXTVEC       : integer;
                 BASE_ROW          : integer;
                 BASE_COL          : integer;
                 USER_INST         : Boolean;
                 ICACHE_ENABLED    : Boolean;
                 DCACHE_ENABLED    : Boolean;
                 ICACHE_LINE_WORDS : integer;
                 DCACHE_LINE_WORDS : integer;
                 ICACHE_ADDR_WIDTH : integer;
                 DCACHE_ADDR_WIDTH : integer;
                 SHIFT_SWIDTH	   : integer;
                 MULT_BWIDTH	   : integer;
                 HW_WPENB	   : boolean;
                 HW_BPENB	   : boolean);

        port (clk         : in  std_logic;
              EXTRN_int   : in  std_logic_vector (NUM_INTRS-1 downto 0)  := (others => '0');
              RESET_int   : in  std_logic                                := '0';
              INTR_ack	  : out std_logic;

              -- WishBone Bus interface (Master)
              WBM_DAT_I   : in  std_logic_vector (WIDTH-1 downto 0)      := (others => '0');
              WBM_ACK_I   : in  std_logic                                := '0';
              WBM_ERR_I   : in  std_logic                                := '0';
              WBM_DAT_O   : out std_logic_vector (WIDTH-1 downto 0);
              WBM_SEL_O   : out std_logic_vector (3 downto 0);
              WBM_WE_O    : out std_logic;
              WBM_STB_O   : out std_logic;
              WBM_CYC_O   : out std_logic;
              WBM_LOCK_O  : out std_logic;
              WBM_ADR_O   : out std_logic_vector (ADDR_WIDTH-1 downto 0) := (others => '0');
              WBM_CTI_O   : out std_logic_vector (2 downto 0);
              WBM_BTE_O   : out std_logic_vector (1 downto 0);

              -- User Instruction Logic interface
              UINST_uiop  : out std_logic_vector(1 downto 0);
              UINST_uinst : out std_logic;
              UINST_Nce   : out std_logic;
              UINST_wbc   : out std_logic;
              UINST_uiopA : out std_logic_vector(CONFIG_UINST_WIDTH-1 downto 0);
              UINST_uiopB : out std_logic_vector(CONFIG_UINST_WIDTH-1 downto 0);
              UINST_uip   : in  std_logic                                := '0';
              UINST_out   : in  std_logic_vector(CONFIG_UINST_WIDTH-1 downto 0) := (others => '0'));
    end component;

    component sram_core
        generic (WIDTH       : integer;
                 ADDR_WIDTH  : integer;
                 RAM_ADDR_W  : integer;
                 RAM_DATA_W  : integer := 32;
                 COUNT_READ  : integer;
                 COUNT_WRITE : integer);
        port (clk          :     std_logic;
              reset        :     std_logic;

              -- WishBone Bus interface (slave)
              WBS_ADR_I    : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              WBS_SEL_I    : in  std_logic_vector (3 downto 0);
              WBS_DAT_I    : in  std_logic_vector (WIDTH-1 downto 0);
              WBS_WE_I     : in  std_logic;
              WBS_STB_I    : in  std_logic;
              WBS_CYC_I    : in  std_logic;
              WBS_CTI_I    : in  std_logic_vector (2 downto 0);
              WBS_BTE_I    : in  std_logic_vector (1 downto 0);
              WBS_DAT_O    : out std_logic_vector (WIDTH-1 downto 0);
              WBS_ACK_O    : out std_logic;
              WBS_ERR_O    : out std_logic;

              -- sram interface
              sram_data_o  : out std_logic_vector (RAM_DATA_W-1 downto 0);
              sram_data_i  : in  std_logic_vector (RAM_DATA_W-1 downto 0);
              sram_data_oe : out std_logic;
              sram_addr    : out std_logic_vector (RAM_ADDR_W-1 downto 0);
              sram_oen     : out std_logic;
              sram_wen     : out std_logic;
              sram_ben     : out std_logic_vector (RAM_DATA_W/8 downto 0);
              sram_csn     : out std_logic);
    end component;
    
    component serial
        generic (WIDTH         : integer;
                 BAUD_RATE     : integer;
                 CORE_FREQ_MHZ : integer);
        port (clk         :     std_logic;
              reset       :     std_logic;

              -- WishBone Bus interface (slave)
              WBS_ADR_I   : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
              WBS_SEL_I   : in  std_logic_vector (3 downto 0);
              WBS_DAT_I   : in  std_logic_vector (WIDTH-1 downto 0);
              WBS_WE_I    : in  std_logic;
              WBS_STB_I   : in  std_logic;
              WBS_CYC_I   : in  std_logic;
              WBS_CTI_I   : in  std_logic_vector (2 downto 0);
              WBS_BTE_I   : in  std_logic_vector (1 downto 0);
              WBS_DAT_O   : out std_logic_vector (WIDTH-1 downto 0);
              WBS_ACK_O   : out std_logic;
              WBS_ERR_O   : out std_logic;
              
              serial_intr : out std_logic;
              txpin       : out std_logic;
              rxpin       : in  std_logic);
    end component;

    component manikremote
        generic (WIDTH      : integer;
                 ADDR_WIDTH : integer);
        port (clk       :     std_logic;
              reset     :     std_logic;
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
              WBS_ERR_O : out std_logic);
    end component;

    component gpio
        generic (WIDTH    : integer;
                 I_WIDTH  : integer;
                 O_WIDTH  : integer;                 
                 T_WIDTH  : integer;                 
                 DEBOUNCE : boolean;
                 GENIRQ   : boolean;
                 I_TRI    : boolean;                 
                 I_TYPE   : integer);
        port (clk       : in  std_logic;
              reset     : in  std_logic;
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
              gp_irq    : out std_logic;
              gp_inout  : inout std_logic_vector(T_WIDTH-1 downto 0);
              gp_input  : in  std_logic_vector(I_WIDTH-1 downto 0) := (others => '0');
              gp_output : out std_logic_vector(O_WIDTH-1 downto 0));
    end component;

    component eth_mac
        generic (ETH_HALF_DUPLEX  : boolean;
                 ETH_ADDR_WIDTH   : integer;
                 ADDR_WIDTH       : integer;
                 WIDTH            : integer;
                 DEFAULT_MAC_ADDR : std_logic_vector (47 downto 0));
        port (reset      : in    std_logic;
              clk        : in    std_logic;
              WBS_ADR_I  : in    std_logic_vector (ADDR_WIDTH-1 downto 0);
              WBS_SEL_I  : in    std_logic_vector (3 downto 0);
              WBS_DAT_I  : in    std_logic_vector (WIDTH-1 downto 0);
              WBS_WE_I   : in    std_logic;
              WBS_STB_I  : in    std_logic;
              WBS_CYC_I  : in    std_logic;
              WBS_CTI_I  : in    std_logic_vector (2 downto 0);
              WBS_BTE_I  : in    std_logic_vector (1 downto 0);
              WBS_DAT_O  : out   std_logic_vector (WIDTH-1 downto 0);
              WBS_ACK_O  : out   std_logic;
              WBS_ERR_O  : out   std_logic;
              eth_intr   : out   std_logic;
              phy_resetn : out   std_logic;
              phy_mdio   : inout std_logic;
              phy_tx_clk : in    std_logic;
              phy_rx_clk : in    std_logic;
              phy_rxd    : in    std_logic_vector(3 downto 0);
              phy_rx_dv  : in    std_logic;
              phy_rx_er  : in    std_logic;
              phy_rx_col : in    std_logic;
              phy_rx_crs : in    std_logic;
              phy_txd    : out   std_logic_vector(3 downto 0);
              phy_tx_en  : out   std_logic;
              phy_tx_er  : out   std_logic;
              phy_mdc    : out   std_logic);
    end component;
    
    signal coreclk     : std_logic;
    signal EXTRN_int   : std_logic_vector(NUM_INTRS-1 downto 0) := (others => '0');
    signal INTR_ack    : std_logic;

    signal MANIK_DAT_I   : std_logic_vector (WIDTH-1 downto 0)      := (others => '0');
    signal MANIK_ACK_I   : std_logic                                := '0';
    signal MANIK_ERR_I   : std_logic                                := '0';
    signal MANIK_DAT_O   : std_logic_vector (WIDTH-1 downto 0);
    signal MANIK_SEL_O   : std_logic_vector (3 downto 0);
    signal MANIK_WE_O    : std_logic;
    signal MANIK_STB_O   : std_logic;
    signal MANIK_CYC_O   : std_logic;
    signal MANIK_LOCK_O  : std_logic;
    signal MANIK_ADR_O   : std_logic_vector (ADDR_WIDTH-1 downto 0) := (others => '0');
    signal MANIK_CTI_O   : std_logic_vector (2 downto 0);
    signal MANIK_BTE_O   : std_logic_vector (1 downto 0);
    
    signal UINST_uiop  : std_logic_vector(1 downto 0)             := "00";
    signal UINST_uinst : std_logic                                := '0';
    signal UINST_Nce   : std_logic                                := '0';
    signal UINST_wbc   : std_logic                                := '0';
    signal UINST_uiopA : std_logic_vector(CONFIG_UINST_WIDTH-1 downto 0) := (others => '0');
    signal UINST_uiopB : std_logic_vector(CONFIG_UINST_WIDTH-1 downto 0) := (others => '0');
    signal UINST_uip   : std_logic                                := '0';
    signal UINST_out   : std_logic_vector(CONFIG_UINST_WIDTH-1 downto 0) := (others => '0');

    signal MREM_ADR_I : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal MREM_SEL_I : std_logic_vector (3 downto 0);
    signal MREM_DAT_I : std_logic_vector (WIDTH-1 downto 0);
    signal MREM_WE_I  : std_logic;
    signal MREM_STB_I : std_logic;
    signal MREM_CYC_I : std_logic;
    signal MREM_CTI_I : std_logic_vector (2 downto 0);
    signal MREM_BTE_I : std_logic_vector (1 downto 0);
    signal MREM_DAT_O : std_logic_vector (WIDTH-1 downto 0);
    signal MREM_ACK_O : std_logic;
    signal MREM_ERR_O : std_logic;
    signal MREM_en    : std_logic := '0';

    signal SRAM_ADR_I   : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal SRAM_SEL_I   : std_logic_vector (3 downto 0);
    signal SRAM_DAT_I   : std_logic_vector (WIDTH-1 downto 0);
    signal SRAM_WE_I    : std_logic;
    signal SRAM_STB_I   : std_logic;
    signal SRAM_CYC_I   : std_logic;
    signal SRAM_CTI_I   : std_logic_vector (2 downto 0);
    signal SRAM_BTE_I   : std_logic_vector (1 downto 0);
    signal SRAM_DAT_O   : std_logic_vector (WIDTH-1 downto 0);
    signal SRAM_ACK_O   : std_logic := '0';
    signal SRAM_ERR_O   : std_logic;
    signal SRAM_en      : std_logic := '0';
    signal sram_ben     : std_logic_vector (3 downto 0);
    signal sram_data_oe : std_logic;
    signal sram_data_o  : std_logic_vector (WIDTH-1 downto 0);
    signal sram_csn_l   : std_logic;

    signal FLASH_ADR_I   : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal FLASH_SEL_I   : std_logic_vector (3 downto 0);
    signal FLASH_DAT_I   : std_logic_vector (WIDTH-1 downto 0);
    signal FLASH_WE_I    : std_logic;
    signal FLASH_STB_I   : std_logic;
    signal FLASH_CYC_I   : std_logic;
    signal FLASH_CTI_I   : std_logic_vector (2 downto 0);
    signal FLASH_BTE_I   : std_logic_vector (1 downto 0);
    signal FLASH_DAT_O   : std_logic_vector (WIDTH-1 downto 0);
    signal FLASH_ACK_O   : std_logic := '0';
    signal FLASH_ERR_O   : std_logic;
    signal FLASH_en      : std_logic := '0';
    signal flash_data_oe : std_logic;
    signal flash_data_o  : std_logic_vector (CONFIG_FLASH_DATA_W-1 downto 0);
    
    signal UART_ADR_I : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal UART_SEL_I : std_logic_vector (3 downto 0);
    signal UART_DAT_I : std_logic_vector (WIDTH-1 downto 0);
    signal UART_WE_I  : std_logic;
    signal UART_STB_I : std_logic;
    signal UART_CYC_I : std_logic;
    signal UART_CTI_I : std_logic_vector (2 downto 0);
    signal UART_BTE_I : std_logic_vector (1 downto 0);
    signal UART_DAT_O : std_logic_vector (WIDTH-1 downto 0);
    signal UART_ACK_O : std_logic;
    signal UART_ERR_O : std_logic;
    signal UART_en    : std_logic := '0';
    signal UART_intr  : std_logic;

    signal GPIO0_ADR_I : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal GPIO0_SEL_I : std_logic_vector (3 downto 0);
    signal GPIO0_DAT_I : std_logic_vector (WIDTH-1 downto 0);
    signal GPIO0_WE_I  : std_logic;
    signal GPIO0_STB_I : std_logic;
    signal GPIO0_CYC_I : std_logic;
    signal GPIO0_CTI_I : std_logic_vector (2 downto 0);
    signal GPIO0_BTE_I : std_logic_vector (1 downto 0);
    signal GPIO0_DAT_O : std_logic_vector (WIDTH-1 downto 0);
    signal GPIO0_ACK_O : std_logic;
    signal GPIO0_ERR_O : std_logic;
    signal GPIO0_en    : std_logic := '0';
    signal GPIO0_intr  : std_logic;

    signal EEMAC_ADR_I : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal EEMAC_SEL_I : std_logic_vector (3 downto 0);
    signal EEMAC_DAT_I : std_logic_vector (WIDTH-1 downto 0);
    signal EEMAC_WE_I  : std_logic;
    signal EEMAC_STB_I : std_logic;
    signal EEMAC_CYC_I : std_logic;
    signal EEMAC_CTI_I : std_logic_vector (2 downto 0);
    signal EEMAC_BTE_I : std_logic_vector (1 downto 0);
    signal EEMAC_DAT_O : std_logic_vector (WIDTH-1 downto 0);
    signal EEMAC_ACK_O : std_logic;
    signal EEMAC_ERR_O : std_logic;
    signal EEMAC_en    : std_logic := '0';
    signal EEMAC_intr  : std_logic;

    signal stb_o : std_logic := '0';
    
    signal sysclk,clk : std_logic := '0';
    signal reset_in   : std_logic;
    signal io_address : std_logic := '0';
    
begin  -- Behavioral

    sysclk    <= clk_i;
    
    ---------------------------------------------------------------------------
    --                  SysClock generation
    ---------------------------------------------------------------------------
    xilinx_clk: if Technology = "XILINX" generate
    begin    
        xpll_n: xpll port map (iclk => sysclk, oclk => clk);
    end generate xilinx_clk;

    altera_clk: if Technology = "ALTERA" generate
    begin
        apll_ndiv: apll port map (inclk0 => sysclk, c0 => clk);
    end generate altera_clk;

    actel_clk: if Technology = "ACTEL" generate
      actpll_inst : actpll
        port map (POWERDOWN => '0',
                  CLKA      => sysclk,
                  LOCK      => open,
                  GLA       => clk);
    end generate actel_clk;

    lattice_clk: if Technology = "LATTICE"  generate
        ecp_clk: if Lattice_Family = "ECP" generate
            ecp_pll_1: ecp_pll
                generic map (IN_FREQ_MHZ   => IN_FREQ_MHZ,
                             CORE_FREQ_MHZ => CORE_FREQ_MHZ,
                             CLK_DIVBY     => CLK_DIVBY,
                             CLK_MULBY     => CLK_MULBY)
                port map (CLK   => sysclk,
                          RESET => '0',
                          CLKOP => clk,
                          LOCK  => open);
        end generate ecp_clk;
    end generate lattice_clk;

    reset_high: if RESET_POS = true generate
        reset_in  <= reset_i;        
    end generate reset_high;
    reset_low: if RESET_POS = false generate
        reset_in  <= not reset_i;        
    end generate reset_low;

    ---------------------------------------------------------------------------
    -- I/O address space is uncacheable data space 
    ---------------------------------------------------------------------------
    io_address <= MANIK_ADR_O(ADDR_WIDTH-1); 
    ---------------------------------------------------------------------------

    manik : manik2top
        generic map (WIDTH             => WIDTH,
                     UINST_WIDTH       => CONFIG_UINST_WIDTH,
                     TIMER_WIDTH       => CONFIG_TIMER_WIDTH,
                     TIMER_CLK_DIV     => CONFIG_TIMER_CLK_DIV,
                     INTR_VECBASE      => CONFIG_INTR_VECBASE,
                     INTR_SWIVEC       => CONFIG_INTR_SWIVEC,
                     INTR_TMRVEC       => CONFIG_INTR_TMRVEC,
                     INTR_EXTVEC       => CONFIG_INTR_EXTVEC,
                     BASE_ROW          => CONFIG_BASE_ROW,
                     BASE_COL          => CONFIG_BASE_COL,
                     USER_INST         => CONFIG_USER_INST,
                     ICACHE_ENABLED    => CONFIG_ICACHE_ENABLED,
                     DCACHE_ENABLED    => CONFIG_DCACHE_ENABLED,
                     ICACHE_LINE_WORDS => CONFIG_ICACHE_LINE_WORDS,
                     DCACHE_LINE_WORDS => CONFIG_DCACHE_LINE_WORDS,
                     ICACHE_ADDR_WIDTH => CONFIG_ICACHE_ADDR_WIDTH,
                     DCACHE_ADDR_WIDTH => CONFIG_DCACHE_ADDR_WIDTH,
                     SHIFT_SWIDTH      => CONFIG_SHIFT_SWIDTH,
                     MULT_BWIDTH       => CONFIG_MULT_BWIDTH,
                     HW_WPENB 	       => CONFIG_HW_WPENB,
                     HW_BPENB	       => CONFIG_HW_BPENB)
        port map (clk         => clk,
                  EXTRN_int   => EXTRN_int,
                  RESET_int   => reset_in,
                  INTR_ack    => INTR_ack,
                  WBM_DAT_I   => MANIK_DAT_I,
                  WBM_ACK_I   => MANIK_ACK_I,
                  WBM_ERR_I   => MANIK_ERR_I,
                  WBM_DAT_O   => MANIK_DAT_O,
                  WBM_SEL_O   => MANIK_SEL_O,
                  WBM_WE_O    => MANIK_WE_O,
                  WBM_STB_O   => MANIK_STB_O,
                  WBM_CYC_O   => MANIK_CYC_O,
                  WBM_LOCK_O  => MANIK_LOCK_O,
                  WBM_ADR_O   => MANIK_ADR_O,
                  WBM_CTI_O   => MANIK_CTI_O,
                  WBM_BTE_O   => MANIK_BTE_O,
                  
                  UINST_uiop  => UINST_uiop,
                  UINST_uinst => UINST_uinst,
                  UINST_Nce   => UINST_Nce,
                  UINST_wbc   => UINST_wbc,
                  UINST_uiopA => UINST_uiopA,
                  UINST_uiopB => UINST_uiopB,
                  UINST_uip   => UINST_uip,
                  UINST_out   => UINST_out);

    MANIK_ACK_I <= MREM_ACK_O  when MREM_en  = '1' else
                   SRAM_ACK_O  when SRAM_en  = '1' else
                   UART_ACK_O  when UART_en  = '1' else
                   GPIO0_ACK_O when GPIO0_en = '1' else
                   EEMAC_ACK_O when EEMAC_en = '1' else
                   FLASH_ACK_O when FLASH_en = '1' else '0';
    
    MANIK_DAT_I <= MREM_DAT_O  when MREM_en  = '1' else
                   SRAM_DAT_O  when SRAM_en  = '1' else
                   UART_DAT_O  when UART_en  = '1' else
                   GPIO0_DAT_O when GPIO0_en = '1' else
                   EEMAC_DAT_O when EEMAC_en = '1' else
                   FLASH_DAT_O when FLASH_en = '1' else (others => '0');

    -- error if address is outof address range
    process (clk)
    begin        
        if rising_edge(clk) then
            if MANIK_ACK_I = '1' then
                stb_o <= '0';
            else
                stb_o <= MANIK_STB_O;             
            end if;
        end if;
    end process;
        
    MANIK_ERR_I  <= stb_o and not (MREM_en or SRAM_en or UART_en or GPIO0_en or EEMAC_en or FLASH_en);
    EXTRN_int(0) <= UART_intr;
    EXTRN_int(1) <= EEMAC_intr;
    
    ---------------------------------------------------------------------------
    -- 			OnChip Sync ram Address 0-0x1FFF                     --
    --			reset vector points here			     --
    ---------------------------------------------------------------------------    
    MREM_ADR_I <= MANIK_ADR_O;
    MREM_SEL_I <= MANIK_SEL_O;
    MREM_DAT_I <= MANIK_DAT_O;
    MREM_CYC_I <= MREM_STB_I;
    MREM_CTI_I <= MANIK_CTI_O;
    MREM_BTE_I <= MANIK_BTE_O;
    MREM_WE_I  <= MANIK_WE_O;
    MREM_STB_I <= MANIK_STB_O when MANIK_ADR_O(ADDR_WIDTH-1 downto 13) = conv_std_logic_vector(0,ADDR_WIDTH-13) else '0';

    process (clk)
    begin
        if rising_edge(clk) then
            if MREM_ACK_O = '1' then
                MREM_en <= '0' after 1 ns;
            else
                MREM_en <=  MREM_STB_I  after 1 ns;
            end if;
        end if;
    end process ;
    
    debugrom : manikremote
        generic map (WIDTH      => WIDTH,
                     ADDR_WIDTH => ADDR_WIDTH)
        port map (clk       => clk,
                  reset     => reset_in,                  
                  WBS_ADR_I => MREM_ADR_I,
                  WBS_SEL_I => MREM_SEL_I,
                  WBS_DAT_I => MREM_DAT_I,
                  WBS_WE_I  => MREM_WE_I,
                  WBS_STB_I => MREM_STB_I,
                  WBS_CYC_I => MREM_CYC_I,
                  WBS_CTI_I => MREM_CTI_I,
                  WBS_BTE_I => MREM_BTE_I,
                  WBS_DAT_O => MREM_DAT_O,
                  WBS_ACK_O => MREM_ACK_O,
                  WBS_ERR_O => MREM_ERR_O);

    ---------------------------------------------------------------------------
    -- 		Off-Chip Sram Controller 1MB Address 0x100000-0x1fffff       --
    ---------------------------------------------------------------------------
    SRAM_ADR_I <= MANIK_ADR_O;
    SRAM_SEL_I <= MANIK_SEL_O;
    SRAM_DAT_I <= MANIK_DAT_O;
    SRAM_CYC_I <= SRAM_STB_I;
    SRAM_CTI_I <= MANIK_CTI_O;
    SRAM_BTE_I <= MANIK_BTE_O;
    SRAM_WE_I  <= MANIK_WE_O;
    SRAM_STB_I <= MANIK_STB_O when MANIK_ADR_O(ADDR_WIDTH-1 downto 20) =
                  conv_std_logic_vector(1,ADDR_WIDTH-20) else '0';
    sram_benn(0) <= not sram_ben(0);
    sram_benn(1) <= not sram_ben(1);
    sram_benn(2) <= not sram_ben(2);
    sram_benn(3) <= not sram_ben(3);
    sram_data    <= sram_data_o when sram_data_oe = '0' else ZDATA;
    
    process (clk)
    begin
        if rising_edge(clk) then
            if SRAM_ACK_O = '1' then
                SRAM_en <= '0' after 1 ns;
            else
                SRAM_en <=  SRAM_STB_I after 1 ns;
            end if;
        end if;
    end process ;
    
    sram_controller : sram_core
        generic map (WIDTH       => WIDTH,
                     ADDR_WIDTH  => ADDR_WIDTH,
                     RAM_ADDR_W  => CONFIG_RAM_ADDR_W,
                     COUNT_READ  => CONFIG_COUNT_READ,
                     COUNT_WRITE => CONFIG_COUNT_WRITE)
        port map (clk          => clk,
                  reset        => reset_in,
                  WBS_ADR_I    => SRAM_ADR_I,
                  WBS_SEL_I    => SRAM_SEL_I,
                  WBS_DAT_I    => SRAM_DAT_I,
                  WBS_WE_I     => SRAM_WE_I,
                  WBS_STB_I    => SRAM_STB_I,
                  WBS_CYC_I    => SRAM_CYC_I,
                  WBS_CTI_I    => SRAM_CTI_I,
                  WBS_BTE_I    => SRAM_BTE_I,
                  WBS_DAT_O    => SRAM_DAT_O,
                  WBS_ACK_O    => SRAM_ACK_O,
                  WBS_ERR_O    => SRAM_ERR_O,
                  sram_data_o  => sram_data_o,
                  sram_data_i  => sram_data,
                  sram_data_oe => sram_data_oe,
                  sram_addr    => sram_addr,
                  sram_oen     => sram_oen,
                  sram_wen     => sram_wen,
                  sram_ben     => sram_ben,
                  sram_csn     => sram_csn_l);
    sram_csn <= sram_csn_l & sram_csn_l;
    
    ---------------------------------------------------------------------------
    -- 		Off-Chip Flash Controller 16MB Address 0x8000000-0x8ffffff   --
    ---------------------------------------------------------------------------
    FLASH_ADR_I <= MANIK_ADR_O;
    FLASH_SEL_I <= MANIK_SEL_O;
    FLASH_DAT_I <= MANIK_DAT_O;
    FLASH_CYC_I <= SRAM_STB_I;
    FLASH_CTI_I <= MANIK_CTI_O;
    FLASH_BTE_I <= MANIK_BTE_O;
    FLASH_WE_I  <= MANIK_WE_O;
    FLASH_STB_I <= MANIK_STB_O when MANIK_ADR_O(ADDR_WIDTH-1 downto 24) =
                  conv_std_logic_vector(8,ADDR_WIDTH-24) else '0';
    flash_data  <= flash_data_o when flash_data_oe = '0' else ZDATA(CONFIG_FLASH_DATA_W-1 downto 0);
    flash_a0    <= '0';
    flash_bytn  <= '1';
    flash_rpn   <= '1';
    
    process (clk)
    begin
        if rising_edge(clk) then
            if FLASH_ACK_O = '1' then
                FLASH_en <= '0' after 1 ns;
            else
                FLASH_en <=  FLASH_STB_I after 1 ns;
            end if;
        end if;
    end process ;
    
    flash_controller : sram_core
        generic map (WIDTH       => WIDTH,
                     ADDR_WIDTH  => ADDR_WIDTH,
                     RAM_ADDR_W  => CONFIG_FLASH_ADDR_W,
                     RAM_DATA_W  => CONFIG_FLASH_DATA_W,
                     COUNT_READ  => CONFIG_FLASH_COUNT_READ,
                     COUNT_WRITE => CONFIG_FLASH_COUNT_WRITE)
        port map (clk          => clk,
                  reset        => reset_in,
                  WBS_ADR_I    => FLASH_ADR_I,
                  WBS_SEL_I    => FLASH_SEL_I,
                  WBS_DAT_I    => FLASH_DAT_I,
                  WBS_WE_I     => FLASH_WE_I,
                  WBS_STB_I    => FLASH_STB_I,
                  WBS_CYC_I    => FLASH_CYC_I,
                  WBS_CTI_I    => FLASH_CTI_I,
                  WBS_BTE_I    => FLASH_BTE_I,
                  WBS_DAT_O    => FLASH_DAT_O,
                  WBS_ACK_O    => FLASH_ACK_O,
                  WBS_ERR_O    => FLASH_ERR_O,
                  sram_data_o  => flash_data_o,
                  sram_data_i  => flash_data,
                  sram_data_oe => flash_data_oe,
                  sram_addr    => flash_addr,
                  sram_oen     => flash_oen,
                  sram_wen     => flash_wen,
                  sram_ben     => open,
                  sram_csn     => flash_csn);

    ---------------------------------------------------------------------------
    --            UART - address 0x80000000 - 0x80000001
    ---------------------------------------------------------------------------
    UART_ADR_I <= MANIK_ADR_O;
    UART_SEL_I <= MANIK_SEL_O;
    UART_DAT_I <= MANIK_DAT_O;
    UART_CYC_I <= UART_STB_I;
    UART_CTI_I <= MANIK_CTI_O;
    UART_BTE_I <= MANIK_BTE_O;
    UART_WE_I  <= MANIK_WE_O;
    UART_STB_I <= MANIK_STB_O when io_address = '1' and
                                   MANIK_ADR_O(19 downto 16) = "0000" else '0';
                                       
    process (clk)
    begin
        if rising_edge(clk) then
            if UART_ACK_O = '1' then
                UART_en <= '0' after 1 ns;
            else
                UART_en <=  UART_STB_I after 1 ns;
            end if;
        end if;
    end process ;
    
    serial_inst: serial
        generic map (WIDTH         => WIDTH,
                     BAUD_RATE     => CONFIG_BAUD_RATE,
                     CORE_FREQ_MHZ => CORE_FREQ_MHZ)
        port map (clk         => clk,
                  reset       => reset_in,
                  WBS_ADR_I   => UART_ADR_I,
                  WBS_SEL_I   => UART_SEL_I,
                  WBS_DAT_I   => UART_DAT_I,
                  WBS_WE_I    => UART_WE_I,
                  WBS_STB_I   => UART_STB_I,
                  WBS_CYC_I   => UART_CYC_I,
                  WBS_CTI_I   => UART_CTI_I,
                  WBS_BTE_I   => UART_BTE_I,
                  WBS_DAT_O   => UART_DAT_O,
                  WBS_ACK_O   => UART_ACK_O,
                  WBS_ERR_O   => UART_ERR_O,
                  serial_intr => UART_intr,
                  txpin       => txpin,
                  rxpin       => rxpin);

    ---------------------------------------------------------------------------
    --            GPIO0 - address 0x80010000 - 0x80010003
    ---------------------------------------------------------------------------
    GPIO0_ADR_I <= MANIK_ADR_O;
    GPIO0_SEL_I <= MANIK_SEL_O;
    GPIO0_DAT_I <= MANIK_DAT_O;
    GPIO0_CYC_I <= UART_STB_I;
    GPIO0_CTI_I <= MANIK_CTI_O;
    GPIO0_BTE_I <= MANIK_BTE_O;
    GPIO0_WE_I  <= MANIK_WE_O;
    GPIO0_STB_I <= MANIK_STB_O when io_address = '1' and
                                   MANIK_ADR_O(19 downto 16) = "0001" else '0';
                                       
    process (clk)
    begin
        if rising_edge(clk) then
            if GPIO0_ACK_O = '1' then
                GPIO0_en <= '0' after 1 ns;
            else
                GPIO0_en <=  GPIO0_STB_I after 1 ns;
            end if;
        end if;
    end process ;

    segment_display : gpio
        generic map (WIDTH    => WIDTH,
                     I_WIDTH  => WIDTH,
                     O_WIDTH  => 8,
                     T_WIDTH  => 0,
                     DEBOUNCE => false,
                     GENIRQ   => false,
                     I_TRI    => false,
                     I_TYPE   => 1)
        port map (clk       => clk,
                  reset     => reset_in,
                  WBS_ADR_I => GPIO0_ADR_I,
                  WBS_SEL_I => GPIO0_SEL_I,
                  WBS_DAT_I => GPIO0_DAT_I,
                  WBS_WE_I  => GPIO0_WE_I,
                  WBS_STB_I => GPIO0_STB_I,
                  WBS_CYC_I => GPIO0_CYC_I,
                  WBS_CTI_I => GPIO0_CTI_I,
                  WBS_BTE_I => GPIO0_BTE_I,
                  WBS_DAT_O => GPIO0_DAT_O,
                  WBS_ACK_O => GPIO0_ACK_O,
                  WBS_ERR_O => GPIO0_ERR_O,
                  gp_irq    => open,
                  gp_inout  => open,
                  gp_input  => open,                  
                  gp_output => seg_led);

    ---------------------------------------------------------------------------
    --            ETHERNET MAC - address 0x80020000 - 0x80020008
    ---------------------------------------------------------------------------
    eth_mac_gen: if USE_ETH = true generate
        
        EEMAC_ADR_I <= MANIK_ADR_O;
        EEMAC_SEL_I <= MANIK_SEL_O;
        EEMAC_DAT_I <= MANIK_DAT_O;
        EEMAC_CYC_I <= EEMAC_STB_I;
        EEMAC_CTI_I <= MANIK_CTI_O;
        EEMAC_BTE_I <= MANIK_BTE_O;
        EEMAC_WE_I  <= MANIK_WE_O;
        EEMAC_STB_I <= MANIK_STB_O when io_address = '1' and
                       MANIK_ADR_O(19 downto 16) = "0010" else '0';
        
        process (clk)
        begin
            if rising_edge(clk) then
                if EEMAC_ACK_O = '1' then
                    EEMAC_en <= '0' after 1 ns;
                else
                    EEMAC_en <=  EEMAC_STB_I after 1 ns;
                end if;
            end if;
        end process ;

        easy_eth_mac : eth_mac
            generic map (ETH_HALF_DUPLEX  => false,
                         ETH_ADDR_WIDTH   => 11,
                         ADDR_WIDTH       => ADDR_WIDTH,
                         WIDTH            => WIDTH,
                         DEFAULT_MAC_ADDR => x"001122334455")
            port map (reset      => reset_in,
                      clk        => clk,
                      WBS_ADR_I  => EEMAC_ADR_I,
                      WBS_SEL_I  => EEMAC_SEL_I,
                      WBS_DAT_I  => EEMAC_DAT_I,
                      WBS_WE_I   => EEMAC_WE_I,
                      WBS_STB_I  => EEMAC_STB_I,
                      WBS_CYC_I  => EEMAC_CYC_I,
                      WBS_CTI_I  => EEMAC_CTI_I,
                      WBS_BTE_I  => EEMAC_BTE_I,
                      WBS_DAT_O  => EEMAC_DAT_O,
                      WBS_ACK_O  => EEMAC_ACK_O,
                      WBS_ERR_O  => EEMAC_ERR_O,
                      eth_intr   => EEMAC_intr,
                      phy_resetn => phy_resetn,
                      phy_mdio   => phy_mdio,
                      phy_tx_clk => phy_tx_clk,
                      phy_rx_clk => phy_rx_clk,
                      phy_rxd    => phy_rxd,
                      phy_rx_dv  => phy_rx_dv,
                      phy_rx_er  => phy_rx_er,
                      phy_rx_col => phy_rx_col,
                      phy_rx_crs => phy_rx_crs,
                      phy_txd    => phy_txd,
                      phy_tx_en  => phy_tx_en,
                      phy_tx_er  => phy_tx_er,
                      phy_mdc    => phy_mdc);
    end generate eth_mac_gen;
end Behavioral;
