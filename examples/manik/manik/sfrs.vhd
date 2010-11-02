-------------------------------------------------------------------------------
-- Title      : SFRs
-- Project    : MANIK-2
-------------------------------------------------------------------------------
-- File       : sfrs.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-11-25
-- Last update: 2006-10-07
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Implements the Special Function Registers of MANIK-2
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-11-25  1.0      sandeep Created
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
use work.manikaltera.all;
use work.manikactel.all;

library UNISIM;
use UNISIM.vcomponents.all;


entity sfrs is
    
    generic (WIDTH         : integer := 32;
             INTR_VECBASE  : integer := 0;
             HW_WPENB	   : boolean := false;
             HW_BPENB	   : boolean := false;
             USREG_ENABLED : boolean := true;
             TIMER_WIDTH   : integer := 32);

    port (EX_sfrreg     : in  std_logic_vector (3 downto 0);
          EX_sfrval     : in  std_logic_vector (WIDTH-1 downto 0);
          EX_sfrwe      : in  std_logic;
          EX_cyflag     : in  std_logic;
          EX_tfflag     : in  std_logic;
          EX_cywe       : in  std_logic;
          EX_tfwe       : in  std_logic;
          EX_mispred    : in  std_logic;
          EX_dbghit     : in  std_logic;
          EX_wphit	: in  std_logic_vector (1 downto 0);
          EX_bphit	: in  std_logic;
          IAGU_pc       : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
          IAGU_deregbr  : in  std_logic;
          IAGU_exbr     : in  std_logic;
          IAGU_exbrlong : in  std_logic;
          IAGU_exregbr  : in  std_logic;
          IAGU_intr     : in  std_logic;
          IAGU_anull    : in  std_logic;
          PCT_sdly	: in  std_logic;
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
          SFR_wp_enb	: out std_logic_vector(1 downto 0);
          SFR_bp_enb	: out std_logic_vector(1 downto 0);
          SFR_wp0	: out std_logic_vector (ADDR_WIDTH-1 downto 0);
          SFR_wp1	: out std_logic_vector (ADDR_WIDTH-1 downto 0);
          SFR_bp0	: out std_logic_vector (ADDR_WIDTH-1 downto 0);
          SFR_bp1	: out std_logic_vector (ADDR_WIDTH-1 downto 0);
          SFR_ibase	: out std_logic_vector (ADDR_WIDTH-1 downto 0);
          SFR_eienb     : out std_logic_vector (NUM_INTRS-1 downto 0);
          SFR_sfrval    : out std_logic_vector (WIDTH-1 downto 0);
          SFR_trval     : out std_logic_vector (TIMER_WIDTH-1 downto 0));

end sfrs;

architecture Behavioral of sfrs is
    constant ADDR_TWO  : std_logic_vector(ADDR_WIDTH-1 downto 0) := conv_std_logic_vector(2, ADDR_WIDTH);
    constant ADDR_FOUR : std_logic_vector(ADDR_WIDTH-1 downto 0) := conv_std_logic_vector(4, ADDR_WIDTH);
    constant BROW      : integer := (((WIDTH-ADDR_WIDTH)/2)+(ADDR_WIDTH mod 2))-(WIDTH/2);
    
    signal sfrval      : std_logic_vector (WIDTH-1 downto 0);
    signal psw_ext     : std_logic_vector (WIDTH-1 downto 0);
    signal timer_ext   : std_logic_vector (WIDTH-1 downto 0);
    signal ra_ext      : std_logic_vector (WIDTH-1 downto 0);
    signal ipc_ext     : std_logic_vector (WIDTH-1 downto 0);
    signal ibase_ext   : std_logic_vector (WIDTH-1 downto 0);
    signal ipc_mux     : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal ra_mux      : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal psw_we      : std_logic;
    signal ipc_we      : std_logic;
    signal ra_we       : std_logic;
    signal ibase_we    : std_logic;
    signal usreg_we    : std_logic;
    signal cyflag_comb : std_logic;
    signal tfflag_comb : std_logic;
    signal ipflag_comb : std_logic;
    signal bipflag_comb: std_logic;
    signal tstart_en   : std_logic;
    signal hw_bpav, hw_wpav : std_logic;
    signal hw_dbg : std_logic_vector (WIDTH-1 downto 0);
    
    signal ipc_selin : std_logic_vector (3 downto 0);
    signal ipc_sel   : std_logic_vector (1 downto 0);
    
    -- registers
    signal ibase  : std_logic_vector (ADDR_WIDTH-1 downto 0)   := (others => '0');
    signal ipc    : std_logic_vector (ADDR_WIDTH-1 downto 0)   := (others => '0');
    signal ra     : std_logic_vector (ADDR_WIDTH-1 downto 0)   := (others => '0');
    signal depc   : std_logic_vector (ADDR_WIDTH-1 downto 0)   := (others => '0');
    signal rfpc   : std_logic_vector (ADDR_WIDTH-1 downto 0)   := (others => '0');
    signal expc   : std_logic_vector (ADDR_WIDTH-1 downto 0)   := (others => '0');
    signal timer  : std_logic_vector (TIMER_WIDTH-1 downto 0)  := (others => '0');
    signal swinum : std_logic_vector (SWINUM_WIDTH-1 downto 0) := (others => '0');
    signal eienb  : std_logic_vector (NUM_INTRS-1 downto 0)    := (others => '0');
    signal eistat : std_logic_vector (NUM_INTRS-1 downto 0)    := (others => '0');
    signal usreg  : std_logic_vector (WIDTH-1 downto 0)        := (others => '0');
    
    signal hw_wp0, hw_wp1 : std_logic_vector (ADDR_WIDTH-1 downto 0) := (others => '0');
    signal hw_bp0, hw_bp1 : std_logic_vector (ADDR_WIDTH-1 downto 0) := (others => '0');
    signal hw_bphit       : std_logic                                := '0';
    signal hw_wphit       : std_logic_vector (1 downto 0)            := "00";
    
    signal idbuserr : std_logic_vector (1 downto 0) 	       := "00";
    
    signal iiflag  : std_logic := '0';
    signal bdflag  : std_logic := '0';
    signal cyflag  : std_logic := '0';
    signal tfflag  : std_logic := '0';
    signal ieflag  : std_logic := '0';
    signal teflag  : std_logic := '0';
    signal ipflag  : std_logic := '0';
    signal swflag  : std_logic := '0';
    signal tiflag  : std_logic := '0';
    signal eiflag  : std_logic := '0';
    signal bipflag : std_logic := '0';
    signal pdflag  : std_logic := '0';
    signal ssflag  : std_logic := '0';
    signal ss_psw  : std_logic := '0';
    signal ss_hit  : std_logic := '0';

    signal hw_wp0_enb, hw_wp1_enb : std_logic  := '0';
    signal hw_bp0_enb, hw_bp1_enb : std_logic  := '0';
    
    attribute BEL : string;
begin  -- Behavioral

    pc_proc : process (clk)
    begin
        if rising_edge(clk) then
            if IAGU_anull = '1' then
                depc <= (others => '0') after 1 ns;
                rfpc <= (others => '0') after 1 ns;
                expc <= (others => '0') after 1 ns;
            elsif PCT_stall = '0' then
                depc <= IAGU_pc after 1 ns;
                rfpc <= depc    after 1 ns;
                expc <= rfpc    after 1 ns;
            end if;
        end if;
    end process pc_proc;

    -- assign debug signals
    -- synopsys translate_off
    debug_core: if DEBUGCORE = true generate
        sfr_expc <= expc;
    end generate debug_core;
    -- synopsys translate_on

      
    -- ipc .. is the address of the instruction that would
    -- have executed.
    
    -- if EX stage has a branch or BUSerr and not mispredicted
    -- or single stepping pick up EX stage PC. If mispredicted
    -- or single stepping we want the address of the branch target
    ipc_selin(3) <= (IAGU_exbr or INTR_buserr) and not (EX_mispred or EX_dbghit or INTR_swi);

    -- if exstage has a register branch and not BUSerror or single stepping
    -- also pick up EX stage PC. For single stepping we want the address
    -- of the branch target
    ipc_selin(2) <= IAGU_exregbr and not (EX_dbghit or INTR_buserr);

    -- if Software interrupt or EX stage has a taken branch & we are
    -- single-stepping then pick up the DE stage pc. Only for long branches
    -- for shorts we pick the RF pc.
    ipc_selin(1) <= INTR_swi or (IAGU_exbrlong and (EX_dbghit and not EX_mispred));

    -- if other than software interrupt and not single stepping a
    -- register branch pick up RF stage PC            
    ipc_selin(0) <= IAGU_intr and not EX_mispred and not(EX_dbghit and IAGU_exregbr);

    -- in all other cases pick up the result produced by
    -- the EX (ALUnit) (EX_sfrval)

    ipc_sel_proc: process (ipc_selin)
    begin
      case ipc_selin is
        -- select EX_pc
        when "1000" => ipc_sel <= "00";
        when "1001" => ipc_sel <= "00";
        when "1010" => ipc_sel <= "00";
        when "1011" => ipc_sel <= "00";
        when "1100" => ipc_sel <= "00";
        when "1101" => ipc_sel <= "00";
        when "1110" => ipc_sel <= "00";
        when "1111" => ipc_sel <= "00";
        when "0100" => ipc_sel <= "00";
        when "0101" => ipc_sel <= "00";
        when "0110" => ipc_sel <= "00";
        when "0111" => ipc_sel <= "00";
        -- select DE_pc
        when "0010" => ipc_sel <= "01";
        when "0011" => ipc_sel <= "01";
        -- select RF_pc
        when "0001" => ipc_sel <= "10";
        -- select EX_sfrval            
        when others => ipc_sel <= "11";
      end case;
    end process ipc_sel_proc;        


    generic_tech: if Technology /= "XILINX" generate
        -- capture PC of instruction that would have executed next
        not_altera: if Technology /= "ALTERA" and Technology /= "ACTEL" generate
            ipc_mux <= expc when ipc_sel = "00" else
                       depc when ipc_sel = "01" else
                       rfpc when ipc_sel = "10" else
                       EX_sfrval after 1 ns;            
        end generate not_altera;

        altera_tech: if Technology = "ALTERA" generate
            IPC_MUX_INST : mux41
                port map (data3x => EX_sfrval,
                          data2x => rfpc,
                          data1x => depc,
                          data0x => expc,
                          sel    => ipc_sel,
                          result => ipc_mux);
        end generate altera_tech;

        actel_tech: if Technology = "ACTEL" generate
            APA3_Family: if Actel_Family = "APA3" generate
                IPC_MUX_INST: mux41_actel_apa3
                    port map (Data0_port => expc,
                              Data1_port => depc,
                              Data2_port => rfpc,
                              Data3_port => EX_sfrval,
                              Sel0       => ipc_sel(0),
                              Sel1       => ipc_sel(1),
                              Result     => ipc_mux);                
            end generate APA3_Family;
            APA_Family: if Actel_Family = "APA" generate
                IPC_MUX_INST: mux41_actel
                    port map (Data0_port => expc,
                              Data1_port => depc,
                              Data2_port => rfpc,
                              Data3_port => EX_sfrval,
                              Sel0       => ipc_sel(0),
                              Sel1       => ipc_sel(1),
                              Result     => ipc_mux);                
            end generate APA_Family;
        end generate actel_tech;

        -- IPC is updated if mtsfr ipc,rn or when interrupt happens
        ipc_we  <= '1' when (EX_sfrwe = '1' and EX_sfrreg = SFR_IPC) or
                   (IAGU_intr = '1') else '0';    
        ipc_proc : process (clk, INTR_reset)
        begin
            if INTR_reset = '1' then
                ipc <= (others => '0');
            elsif rising_edge(clk) then
                if ipc_we = '1' then     -- clock enable
                    -- save the address of the instr that would have executed next
                    ipc <= ipc_mux;
                end if;
            end if;
        end process ipc_proc;
        ra_mux  <= DE_pc + conv_std_logic_vector(4,ADDR_WIDTH) when DE_BrLink = '1' else EX_sfrval;

        -- flags can be updated in two ways
        -- a) mtsfr        PSW,rx
        -- b) any instruction that updates the flag.
        psw_we <= '1' when EX_sfrwe = '1' and EX_sfrreg = SFR_PSW else '0';

        -- T(rue)/F(alse) flag
        tfflag_comb <= EX_tfflag when EX_tfwe = '1' else EX_sfrval (TF_FLAG);
        tfflag_proc : process (clk, INTR_reset)
        begin
            if INTR_reset = '1' then
                    tfflag <= '0';                
            elsif rising_edge(clk) then
                if psw_we = '1' or EX_tfwe = '1' then
                    tfflag <= tfflag_comb;
                end if;
            end if;
        end process tfflag_proc;

        -- update ie flag
        ieus_proc : process (clk, INTR_reset)
        begin
            if INTR_reset = '1' then
                ieflag <= '0';
            elsif rising_edge(clk) then
                if psw_we = '1' then
                    ieflag <= EX_sfrval (IE_FLAG);
                end if;
            end if;
        end process ieus_proc;

        -- carry flag
        cyflag_comb <= EX_cyflag when EX_cywe = '1' else EX_sfrval (CY_FLAG);
        cyflag_proc : process (clk, INTR_reset)
        begin
            if INTR_reset = '1' then
                cyflag <= '0';
            elsif rising_edge(clk) then
                if psw_we = '1' or EX_cywe = '1' then
                    cyflag <= cyflag_comb;
                end if;
            end if;
        end process cyflag_proc;

    end generate generic_tech;

    xilinx_tech: if Technology = "XILINX" generate
        attribute RLOC of IPC_MUX_INST : label is rloc_string(-(WIDTH/2), 6,true,0,15);
        attribute INIT of ISIPC_INST : label is "0004";
        attribute INIT of IPCWE_INST : label is "EA";
        attribute RLOC of ISIPC_INST : label is rloc_string(0, 6, 0,-WIDTH/2,true,0,15);
        attribute BEL  of ISIPC_INST : label is bel_string(0);    
        attribute RLOC of IPCWE_INST : label is rloc_string(0, 6, 0,-WIDTH/2,true,0,15);
        attribute BEL  of IPCWE_INST : label is bel_string(1);
        attribute RLOC of IPC_REGS   : label is rloc_string(-(WIDTH/2), 6,true,0,15);
        attribute RLOC of RA_ADDER   : label is rloc_string(BROW, -9);
        attribute INIT of ISPSW_INST : label is "0001";
        attribute INIT of PSWWE_INST : label is "8";
        attribute INIT of TFFLAG_INST : label is "CA";
        attribute INIT of OR2_1       : label is "E";
        attribute RLOC of TFFLAG_INST : label is rloc_string(-11,0,1,-11,true,-(WIDTH/2)+11,9);
        attribute BEL  of TFFLAG_INST : label is bel_string(1);
        attribute RLOC of TFFLAG_REG  : label is rloc_string(-8,0,1,-8,true,-(WIDTH/2)+8,9);
        attribute BEL  of TFFLAG_REG  : label is bel_string_ff(1);
        attribute RLOC of OR2_1 : label is rloc_string(-11,0,1,-11,true,-(WIDTH/2)+11,9);
        attribute BEL  of OR2_1 : label is bel_string(0);
        attribute RLOC of ISPSW_INST : label is rloc_string(0, 4, 1,-WIDTH/2,true,0,13);
        attribute BEL  of ISPSW_INST : label is bel_string(0);    
        attribute RLOC of PSWWE_INST : label is rloc_string(0, 4, 1,-WIDTH/2,true,0,13);
        attribute BEL  of PSWWE_INST : label is bel_string(1);
        attribute RLOC of IEREG : label is rloc_string(0, 6, 1,-WIDTH/2,true,0,15);
        attribute BEL  of IEREG : label is bel_string(1);
        attribute RLOC of CYFLAG_REG : label is rloc_string(1, 2, 0,-WIDTH/2,true,0,11);
        attribute BEL  of CYFLAG_REG : label is bel_string_ff(1);

        signal ispsw : std_logic;
        signal ipce  : std_logic;
        signal tfwe  : std_logic;
        signal isipc : std_logic;
        signal cyflag_ce : std_logic;
    begin

        IPC_MUX_INST : MUX4_1E_VECTOR
            generic map (WIDTH => ADDR_WIDTH, ROWDIV => 2, SLICE => 1)
            port map (S0 => ipc_sel(0), S1 => ipc_sel(1), EN => '1',
                      V0 => expc,       V1 => depc,       V2 => rfpc,
                      V3 => EX_sfrval,  O  => ipc_mux);
        ISIPC_INST : LUT4
            generic map (INIT => X"0004")
            port map (O => isipc,
                      I0 => EX_sfrreg(0), I1 => EX_sfrreg(1),
                      I2 => EX_sfrreg(2), I3 => EX_sfrreg(3));
        IPCWE_INST : LUT3
            generic map (INIT => X"EA")
            port map (O  => ipc_we, I0 => IAGU_intr,
                      I1 => isipc,  I2 => EX_sfrwe);
    
        IPC_REGS : FDCE_ALT_VECTOR
            generic map (WIDTH => ADDR_WIDTH, XORY  => 0, SPLIT => False)
            port map (CLK => clk, CE  => ipc_we,
                      CLR => INTR_reset,
                      D   => ipc_mux, Q => ipc);
    
        RA_ADDER: MUX_ADD_VECTOR
            generic map (WIDTH => ADDR_WIDTH, SLICE => 1)
            port map (A   => DE_pc,     B   => ADDR_FOUR,
                      C   => EX_sfrval, ADD => DE_BrLink, O   => ra_mux);

        ISPSW_INST : LUT4
            generic map (INIT => X"0001")
            port map (O  => ispsw,
                      I0 => EX_sfrreg(0), I1 => EX_sfrreg(1),
                      I2 => EX_sfrreg(2), I3 => EX_sfrreg(3));
        PSWWE_INST : LUT2
            generic map (INIT => X"8")
            port map (O  => psw_we, I0 => EX_sfrwe, I1 => ispsw);

        TFFLAG_INST : LUT3
            generic map (INIT => X"CA")
            port map (O  => tfflag_comb, I0 => EX_sfrval(TF_FLAG),
                      I1 => EX_tfflag,   I2 => EX_tfwe);
        OR2_1 : LUT2
            generic map (INIT => X"E")
            port map (O  => tfwe, I0 => psw_we, I1 => EX_tfwe);
        TFFLAG_REG : FDCE
            generic map (INIT => '0')
            port map (Q => tfflag, C => clk, CE => tfwe,
                      CLR => INTR_reset, D => tfflag_comb);    

        IEREG : FDCE
            generic map (INIT => '0')
            port map (Q => ieflag, C => clk, CE => psw_we,
                      CLR => INTR_reset, D => EX_sfrval (IE_FLAG));
    
        -- carry flag
        cyflag_comb <= EX_cyflag when EX_cywe = '1' else EX_sfrval (CY_FLAG);
        cyflag_ce   <= psw_we or EX_cywe;
        
        CYFLAG_REG : FDCE
            generic map (INIT => '0')
            port map (Q  => cyflag,   C  => clk, CLR => INTR_reset,
                      CE => cyflag_ce,D  => cyflag_comb);
          
    end generate xilinx_tech;

    
    -- backup ip flag    
    bipflag_comb <= ipflag  when IAGU_intr = '1' else EX_sfrval(BIP_FLAG);
    
    bip_proc: process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            bipflag <= '0';
        elsif rising_edge(clk) then
            if RF_rfi = '1' then
                bipflag <= '0';
            elsif IAGU_intr = '1' or psw_we = '1' then
                bipflag <= bipflag_comb;
            end if;            
        end if;
    end process bip_proc;
        
    -- update ip_flag
    ip_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            ipflag <= '0' ;
        elsif rising_edge(clk) then
            if RF_rfi = '1' then
                ipflag <= bipflag;      -- copy from bip flag
            elsif IAGU_intr = '1' or psw_we = '1' then  -- clock enable
                ipflag <= ipflag_comb;                
            end if;
        end if;
    end process ip_proc;


    -- swint number is set to all 1s when single step completes
    swinum_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            swinum <= (others => '0');
        elsif rising_edge(clk) then
            if INTR_sstep = '1' then    -- sync set
                swinum <= (others => '1') after 1 ns;
            elsif IAGU_intr = '1' and INTR_swi = '1' then
                swinum <= RF_swinum after 1 ns;
            end if;
        end if;
    end process swinum_proc;
        
    -- external interrupt enable flags
    eienb_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            eienb <= (others => '0');
        elsif rising_edge(clk) then            
            if psw_we = '1' then
                eienb <= EX_sfrval(SWINUM_HI+NUM_INTRS-1 downto SWINUM_HI) after 1 ns;
            end if;
        end if;
    end process eienb_proc;

    -- external interrupt status flag
    eistat_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
           eistat <= (others => '0'); 
        elsif rising_edge(clk) then
            if RF_rfi = '1' then
                eistat <= conv_std_logic_vector(0,NUM_INTRS) after 1 ns;
            elsif INTR_extrn = '1' and IAGU_intr = '1' then
                eistat <= INTR_eistat after 1 ns;
            end if;
        end if;
    end process eistat_proc;

    -- register bus error
    idbuserr_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            idbuserr <= "00";
        elsif rising_edge(clk) then
            if RF_rfi = '1' then
                idbuserr <= "00";
            elsif IAGU_intr = '1' and INTR_buserr = '1' then
                idbuserr <= MEM_id_buserr;
            end if;            
        end if;
    end process idbuserr_proc;
        
    -- capture Return address into RA. Here DE_xxx signals are used because
    -- the brlink could be a short branch in which case the very next instruction
    -- might read the RA register. By capturing the DE stage PC this dependency
    -- hazard is avoided.
    -- Can be updated two ways
    --  a) executing a jrl or jl instruction
    --  b) mtsfr     RA,rx
    -- assumes sizeof jrl & jl are the same (assembler inserts NOP after jrl)
    ra_we   <= '1' when (EX_sfrwe = '1' and EX_sfrreg = SFR_RA) or DE_BrLink = '1' else '0';
    ra_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            ra <= (others => '0');
        elsif rising_edge(clk) then
            if ra_we = '1' then
                ra <= ra_mux after 1 ns;
            end if;
        end if;
    end process ra_proc;
    
    
    -- data cache bypass flags
    -- updated by mtsfr PSW,rb instruction only
    bidflag_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            iiflag <= '0';
            bdflag <= '0';
        elsif rising_edge(clk) then
            if psw_we = '1' then
                iiflag <= EX_sfrval(II_FLAG) after 1 ns;
                bdflag <= EX_sfrval(BD_FLAG) after 1 ns;
            end if;
        end if;
    end process bidflag_proc;
    
    -- Power Down flag,
    --  set      with mtsfr  psw,rx
    --  cleared  when interrupt
    pdflag_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            pdflag <= '0';
        elsif rising_edge(clk) then
            if IAGU_intr = '1' then          -- sync reset
                pdflag <= '0' after 2 ns;
            elsif psw_we = '1' then                
                pdflag <= EX_sfrval (PD_FLAG) after 2 ns;
            end if;
        end if;
    end process pdflag_proc;
          
    -- update te flag. te flag is set with a mtsfr cleared
    -- when the processor start processing a timer interrupt
    te_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
           teflag <= '0'; 
        elsif rising_edge(clk) then
            if INTR_timer = '1' then
                teflag <= '0';
            elsif psw_we = '1' then
                teflag <= EX_sfrval (TE_FLAG);
            end if;
        end if;
    end process te_proc;
    
    -- IP (Interrupt in process flag) can be updated either
    -- by the mtsfr instruction or when IAGU_intr is high
    -- the flag is cleared when power down flag is set, setting
    -- them together will put the CPU in a permanent sleep state
    ipflag_proc: process (EX_sfrval, psw_we, IAGU_intr)
    begin
        if (IAGU_intr = '1') then
            ipflag_comb <= '1';
        elsif psw_we = '1' then
            ipflag_comb <= EX_sfrval(IP_FLAG) and not EX_sfrval(PD_FLAG);
        else
            ipflag_comb <= '0';
        end if;
    end process ipflag_proc;
    
    sw_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            swflag <= '0';
        elsif rising_edge(clk) then
            if RF_rfi = '1' and swflag = '1' then
                swflag <= '0';
            elsif IAGU_intr = '1' then
                swflag <= INTR_swi;
            end if;
        end if;
    end process sw_proc;

    -- single step flag in the PSW is set only with
    -- mtsfr. reset when we enter an interrupt. 
    ss_psw_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            ss_psw <= '0';
        elsif rising_edge(clk) then
            if IAGU_intr = '1' then
                ss_psw <= '0' after 1 ns;
            elsif psw_we = '1' then
                ss_psw <= EX_sfrval(SS_FLAG) after 1 ns;
            end if;
        end if;
    end process ss_psw_proc;
            
    -- single step flag is updated when RF_rfi &
    -- PSW SSflag is set
    ss_proc : process (clk, DE_sstep, INTR_reset)
    begin
        if DE_sstep = '1' or INTR_reset = '1' then
            ssflag <= '0' after 1 ns;            
        elsif rising_edge(clk) then
            if RF_rfi = '1' then
                ssflag <= ss_psw after 1 ns;
            end if;
        end if;
    end process ss_proc;

    -- single stepped instruction just executed
    -- reset when rfi
    ss_hit_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            ss_hit <= '0';
        elsif rising_edge(clk) then
            if RF_rfi = '1' and ss_hit = '1' then
                ss_hit <= '0' after 1 ns;
            elsif IAGU_intr = '1' then
                ss_hit <= INTR_sstep after 1 ns; 
            end if;
        end if;
    end process ss_hit_proc;

    -- external interrupt flag
    -- reset when rfi
    ei_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            eiflag <= '0';
        elsif rising_edge(clk) then
            if RF_rfi = '1' and eiflag = '1' then  -- sync reset
                eiflag <= bipflag;
            elsif INTR_extrn = '1' then
                eiflag <= '1';
            end if;
        end if;
    end process ei_proc;

    -- timer interrupt flag
    -- reset when rfi
    ti_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            tiflag <= '0';
        elsif rising_edge(clk) then
            if RF_rfi = '1' and tiflag = '1' then  -- sync reset
                tiflag <= bipflag;                
            elsif INTR_timer = '1' then
                tiflag <= '1';                
            end if;
        end if;
    end process ti_proc;
    
    -- timer reload value can be written only by mtsfr
    generic_tech1: if Technology /= "XILINX" generate        
        timer_proc: process (clk, INTR_reset)
        begin
            if INTR_reset = '1' then
                timer <= (others => '0');
            elsif rising_edge(clk) then
                if EX_sfrwe = '1' and EX_sfrreg = SFR_TIMER then
                    timer <= EX_sfrval (TIMER_WIDTH-1 downto 0);
                end if;
            end if;
        end process timer_proc;
    end generate generic_tech1;

    xilinx_tech1: if Technology = "XILINX" generate
        attribute RLOC of TIMER_REGS  : label is rloc_string(-(WIDTH/2), -3, true, 0, 6);
        signal timerce : std_logic;
    begin
        timerce <= '1' when EX_sfrwe = '1' and EX_sfrreg = SFR_TIMER else '0';
        TIMER_REGS : FDCE_ALT_VECTOR
            generic map (WIDTH => TIMER_WIDTH, XORY  => 1, SPLIT => False)
            port map (CLK => clk, CE  => timerce, Q => timer, CLR => INTR_reset,
                      D   => EX_sfrval(TIMER_WIDTH-1 downto 0));                
    end generate xilinx_tech1;

    -- interrupt base address. can be written to by software
    ibase_we <= '1' when EX_sfrwe = '1' and EX_sfrreg = SFR_VBASE else '0';
    ibase_proc: process (clk)
    begin
        if rising_edge(clk) then
            if PCT_sdly = '0' then
                ibase <= conv_std_logic_vector(INTR_VECBASE,ADDR_WIDTH);
            elsif ibase_we = '1' then
                ibase <= EX_sfrval(ADDR_WIDTH-1 downto 0);
            end if;
        end if;
    end process ibase_proc;

    -- user special function register
    usreg_we <= '1' when EX_sfrwe = '1' and EX_sfrreg = SFR_USREG else '0';
    usreg_proc: process (clk)
    begin  -- process usreg_proc
        if rising_edge(clk) then
            if usreg_we = '1' then
                usreg <= EX_sfrval;
            end if;
        end if;
    end process usreg_proc;

    -- hardware debug registers - watch point
    hwwp_true: if HW_WPENB = true generate
    begin
        hw_wpav <= '1';
        process (clk, INTR_reset)
        begin
            if INTR_reset = '1' then
                hw_wp0_enb <= '0';
                hw_wp1_enb <= '0';
                hw_wphit   <= "00";
                hw_wp0	   <= (others => '0');
                hw_wp1	   <= (others => '0');
            elsif rising_edge(clk) then
                if EX_sfrwe = '1' and EX_sfrreg = SFR_HWDBG then
                    hw_wp0_enb <= EX_sfrval(4);
                    hw_wp1_enb <= EX_sfrval(5);
                end if;
                if EX_sfrwe = '1' and EX_sfrreg = SFR_HWWP0 then
                    hw_wp0 <= EX_sfrval;
                end if;
                if EX_sfrwe = '1' and EX_sfrreg = SFR_HWWP1 then
                    hw_wp1 <= EX_sfrval;
                end if;
                if RF_rfi = '1' then
                    hw_wphit(0) <= '0';
                elsif hw_wphit(0) = '0' then
                    hw_wphit(0) <= EX_wphit(0);
                end if;
                if RF_rfi = '1' then
                    hw_wphit(1) <= '0';
                elsif hw_wphit(1) = '0' then
                    hw_wphit(1) <= EX_wphit(1);
                end if;
            end if;
        end process ;
    end generate hwwp_true;
    
    -- hardware debug registers - break point
    hwbp_true: if HW_BPENB = true generate
    begin
        hw_bpav <= '1';
        process (clk, INTR_reset)
        begin
            if INTR_reset = '1' then
                hw_bp0_enb <= '0';
                hw_bp1_enb <= '0';
                hw_bphit   <= '0';
                hw_bp0	   <= (others => '0');
                hw_bp1	   <= (others => '0');
            elsif rising_edge(clk) then
                if EX_sfrwe = '1' and EX_sfrreg = SFR_HWDBG then
                    hw_bp0_enb <= EX_sfrval(2);
                    hw_bp1_enb <= EX_sfrval(3);
                end if;
                if EX_sfrwe = '1' and EX_sfrreg = SFR_HWBP0 then
                    hw_bp0 <= EX_sfrval;
                end if;
                if EX_sfrwe = '1' and EX_sfrreg = SFR_HWBP1 then
                    hw_bp1 <= EX_sfrval;
                end if;
                if RF_rfi = '1' then
                    hw_bphit <= '0';
                elsif hw_bphit = '0' then
                    hw_bphit <= EX_bphit;
                end if;
            end if;
        end process ;
    end generate hwbp_true;

    hwwp_false: if HW_WPENB = false generate
    begin
        hw_wp0_enb <= '0';
        hw_wp1_enb <= '0';
        hw_wphit   <= "00";
        hw_wpav    <= '0';
        hw_wp0	   <= (others => '0');
        hw_wp1	   <= (others => '0');
    end generate hwwp_false;
    
    hwbp_false: if HW_BPENB = false generate
    begin
        hw_bp0_enb <= '0';
        hw_bp1_enb <= '0';
        hw_bpav    <= '0';
        hw_bphit   <= '0';
        hw_bp0	   <= (others => '0');
        hw_bp1	   <= (others => '0');
    end generate hwbp_false;

    hw_dbg <= X"00000" & "000" &
    -- bit positions   8             7          6            5	           4  
              hw_wphit(1) & hw_wphit(0) & hw_bphit & hw_wp1_enb & hw_wp0_enb &
    -- bit positions   3            2		1	0
              hw_bp1_enb & hw_bp0_enb & hw_wpav & hw_bpav;
    
    -- outputs
    SFR_cyflag <= cyflag;
    SFR_tfflag <= tfflag;
    SFR_ieflag <= ieflag;
    SFR_ipflag <= ipflag;
    SFR_teflag <= teflag;
    SFR_eiflag <= eiflag;
    SFR_swflag <= swflag;
    SFR_tiflag <= tiflag;
    SFR_pdflag <= pdflag;
    SFR_bdflag <= bdflag or (psw_we and EX_sfrval(BD_FLAG));
    SFR_iiflag <= iiflag;
    SFR_ssflag <= ssflag;
    SFR_ibase  <= ibase;
    SFR_sspsw  <= ss_psw;
    SFR_trval  <= timer;
    SFR_eienb  <= eienb;
    SFR_wp_enb <= (hw_wp1_enb and not hw_wphit(1)) & (hw_wp0_enb and not hw_wphit(0));
    SFR_bp_enb <= hw_bp1_enb & hw_bp0_enb;
    SFR_wp0    <= hw_wp0;
    SFR_wp1    <= hw_wp1;
    SFR_bp0    <= hw_bp0;
    SFR_bp1    <= hw_bp1;

    -- timer can be restarted only in normal mode or in the timer isr    
    tstart_en  <= '1' when ipflag = '0' or (ipflag = '1' and tiflag = '1') else '0';    
    SFR_tstart <= '1' when tstart_en = '1' and psw_we = '1' and EX_sfrval(TE_FLAG) = '1' else '0';

    -- timer can be reloaded counter mode by writting to the TR flag
    SFR_trload <= psw_we and EX_sfrval(TR_FLAG);

    
    -- make all of them equal size    
    ext_ibase: for i in 0 to WIDTH-1 generate
        a: if i < ADDR_WIDTH generate
            ibase_ext (i) <= ibase(i);
        end generate a;
        b: if i >= ADDR_WIDTH generate
            ibase_ext (i) <= '0';
        end generate b;
    end generate ext_ibase;
    
    ext_psw : for i in 0 to WIDTH-1 generate
        eistat_l: if i >= EI0_STAT and i <= EI5_STAT generate
            psw_ext(i) <= eistat(i-EI0_STAT);  -- interrupt status
        end generate eistat_l;
        eienb_l: if i >= EI0_ENB and i <= EI5_ENB generate
            psw_ext(i) <= eienb(i-EI0_ENB);  -- eienb
        end generate eienb_l;
        swinum_l: if i >= SWINUM_LO and i < SWINUM_HI generate 
            psw_ext(i) <= swinum(i-SWINUM_LO);  -- swinum
        end generate swinum_l;        
        ssflag_l : if i = SS_FLAG generate
            psw_ext (SS_FLAG) <= ss_hit;
        end generate ssflag_l;
        tuflag_l : if i = TU_FLAG generate
            psw_ext (TU_FLAG) <= TIMER_undf;
        end generate tuflag_l;
        iiflag_l : if i = II_FLAG generate
            psw_ext (II_FLAG) <= iiflag; -- iiflag;
        end generate iiflag_l;        
        bdflag_l : if i = BD_FLAG generate
            psw_ext (BD_FLAG) <= bdflag; -- bdflag;
        end generate bdflag_l;
        cyflag_l : if i = CY_FLAG generate
            psw_ext (CY_FLAG) <= cyflag; -- cyflag;
        end generate cyflag_l;
        tfflag_l : if i = TF_FLAG generate
            psw_ext (TF_FLAG) <= tfflag; -- tfflag;
        end generate tfflag_l;
        ieflag_l : if i = IE_FLAG generate
            psw_ext (IE_FLAG) <= ieflag; -- ieflag;
        end generate ieflag_l;
        ipflag_l : if i = IP_FLAG generate
            psw_ext (IP_FLAG) <= ipflag; -- ipflag;
        end generate ipflag_l;
        teflag_l : if i = TE_FLAG generate
            psw_ext (TE_FLAG) <= teflag; -- teflag;
        end generate teflag_l;        
        swflag_l : if i = SW_FLAG generate
            psw_ext (SW_FLAG) <= swflag; -- swflag;
        end generate swflag_l;        
        eiflag_l : if i = EI_FLAG generate
            psw_ext (EI_FLAG) <= eiflag; -- eiflag;
        end generate eiflag_l;        
        tiflag_l : if i = TI_FLAG generate
            psw_ext (TI_FLAG) <= tiflag; -- tiflag;
        end generate tiflag_l;
        bipflag_1 : if i = BIP_FLAG generate
            psw_ext (BIP_FLAG) <= bipflag;  -- bipflag
        end generate bipflag_1;
        pdflag_1: if i = PD_FLAG generate
            psw_ext (PD_FLAG) <= pdflag;  -- pdflag
        end generate pdflag_1;
        dbeflag_1 : if i = DBE_FLAG generate
            psw_ext (i) <=  idbuserr(0);
        end generate dbeflag_1;
        ibeflag_1 : if i = IBE_FLAG generate
            psw_ext (i) <=  idbuserr(1);
        end generate ibeflag_1;
    end generate ext_psw;

    ext_ra: for i in 0 to WIDTH-1 generate
        a: if i < ADDR_WIDTH generate
            ra_ext (i) <= ra(i);
        end generate a;
        b: if i >= ADDR_WIDTH generate
            ra_ext (i) <= '0';
        end generate b;
    end generate ext_ra;

    ext_ipc: for i in 0 to WIDTH-1 generate
        a: if i < ADDR_WIDTH generate
            ipc_ext (i) <= ipc(i);
        end generate a;
        b: if i >= ADDR_WIDTH generate
            ipc_ext (i) <= '0';
        end generate b;
    end generate ext_ipc;
    
    ext_timer: for i in 0 to WIDTH-1 generate
        a: if i < TIMER_WIDTH generate
            timer_ext (i) <= TIMER_val(i);
        end generate a;
        b: if i >= TIMER_WIDTH generate
            timer_ext (i) <= '0';
        end generate b;
    end generate ext_timer;

    ---------------------------------------------------------------------------
    -- Final - 	  mux for output
    ---------------------------------------------------------------------------
    sfrval <= psw_ext   when DE_rb = SFR_PSW    else
              ra_ext    when DE_rb = SFR_RA     else
              ipc_ext   when DE_rb = SFR_IPC    else
              timer_ext when DE_rb = SFR_TIMER  else
              ibase_ext when DE_rb = SFR_VBASE  else
              usreg     when DE_rb = SFR_USREG  else
              hw_dbg	when DE_rb = SFR_HWDBG  else
              hw_wp0	when DE_rb = SFR_HWWP0	else
              hw_wp1	when DE_rb = SFR_HWWP1	else
              hw_bp0	when DE_rb = SFR_HWBP0	else
              hw_bp1;

    sfrval_proc: process (clk, IAGU_anull)
    begin
        if IAGU_anull = '1' then
            SFR_sfrval <= (others => '0');
        elsif rising_edge(clk) then
            if PCT_stall = '0' then
                SFR_sfrval <= sfrval;                
            end if;
        end if;
    end process sfrval_proc;

    -- debug stuff
    -- synopsys translate_off
    debug_gen: if DEBUGCORE = true generate
        process (clk)
        begin
            if rising_edge(clk) then
                if INTR_swi = '1' and RF_swinum = x"0" then
                    isexit <= '1' after 1 ms;  -- allows for last printing to finish
                end if;
            end if;
        end process ;
    end generate debug_gen;
    -- synopsys translate_on
end Behavioral;
