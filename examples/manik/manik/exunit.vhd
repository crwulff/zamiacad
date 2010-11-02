-------------------------------------------------------------------------------
-- Title      : Execute unit
-- Project    : MANIK-2
-------------------------------------------------------------------------------
-- File       : exunit.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-11-24
-- Last update: 2006-10-10
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: implements the execute unit for MANIK-2
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-11-24  1.0      sandeep	Created
-------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library UNISIM;
use UNISIM.vcomponents.all;

--library lpm;
--use lpm.lpm_components.all;
use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;
use work.manikaltera.all;
use work.manikactel.all;

entity EXunit is
    
    generic (WIDTH         : integer := 32;
             HW_WPENB	   : boolean := false;
             HW_BPENB	   : boolean := false;
             USER_INST     : boolean := True);

    port (RF_load    : in std_logic;
          RF_store   : in std_logic;
          RF_half    : in std_logic;
          RF_byte    : in std_logic;
          RF_word    : in std_logic;
          RF_iscond  : in std_logic;
          RF_condT   : in std_logic;
          RF_Br      : in std_logic;
          RF_BrUnc   : in std_logic;
          RF_imm     : in std_logic_vector (RF_IMM_WIDTH-1 downto 0);
          RF_asel    : in std_logic_vector (1 downto 0);
          RF_bsel    : in std_logic_vector (1 downto 0);
          RF_rdval   : in std_logic_vector (WIDTH-1 downto 0);
          RF_rbval   : in std_logic_vector (WIDTH-1 downto 0);
          RF_pc      : in std_logic_vector (ADDR_WIDTH-1 downto 0);
          RF_rfwe    : in std_logic;
          RF_sfrwe   : in std_logic;
          RF_swi     : in std_logic;
          RF_cywe    : in std_logic;
          RF_tfwe    : in std_logic;
          RF_aluop   : in std_logic_vector (3 downto 0);
          RF_rd      : in std_logic_vector (3 downto 0);
          RF_rb      : in std_logic_vector (3 downto 0);
          RF_flagop  : in std_logic_vector (2 downto 0);
          RF_alu     : in std_logic;
          RF_msu     : in std_logic;
          RF_uinst   : in std_logic;
          RF_sstep   : in std_logic;
          RF_ivalid  : in std_logic;
          RF_bphit   : in std_logic;
          PCT_stall  : in std_logic;
          PCT_ustall : in std_logic;
          IAGU_anull : in std_logic;
          INTR_reset : in std_logic;

          MULT_mdone : in std_logic;
          MULT_out   : in std_logic_vector (WIDTH-1 downto 0);
          MULT_rd    : in std_logic_vector (3 downto 0);

          UINST_uip : in std_logic;
          UINST_out : in std_logic_vector (WIDTH-1 downto 0);

          SFR_tfflag : in std_logic;
          SFR_cyflag : in std_logic;
          SFR_sfrval : in std_logic_vector (WIDTH-1 downto 0);
          SFR_wp_enb : in std_logic_vector (1 downto 0);
          SFR_wp0    : in std_logic_vector (ADDR_WIDTH-1 downto 0);
          SFR_wp1    : in std_logic_vector (ADDR_WIDTH-1 downto 0);
          
          clk : in std_logic;

          MEM_dbus    : in std_logic_vector (WIDTH-1 downto 0);
          MEM_ld_done : in std_logic;
          MEM_st_done : in std_logic;
          MEM_Nbusy   : in std_logic;

          EX_wphit   : out std_logic_vector (1 downto 0);
          EX_bphit   : out std_logic;
          EX_load    : out std_logic;
          EX_store   : out std_logic;
          EX_lsu     : out std_logic;
          EX_rdval   : out std_logic_vector (WIDTH-1 downto 0);
          EX_rbval   : out std_logic_vector (WIDTH-1 downto 0);
          EX_opb     : out std_logic_vector (WIDTH-1 downto 0);
          EX_out     : out std_logic_vector (WIDTH-1 downto 0);
          EX_sfrwe   : out std_logic;
          EX_cyflag  : out std_logic;
          EX_tfflag  : out std_logic;
          EX_cywe    : out std_logic;
          EX_tfwe    : out std_logic;
          EX_addr    : out std_logic_vector (ADDR_WIDTH-1 downto 0);
          EX_st_data : out std_logic_vector (WIDTH-1 downto 0);
          EX_sfrreg  : out std_logic_vector (3 downto 0);
          EX_sfrval  : out std_logic_vector (WIDTH-1 downto 0);
          EX_mip     : out std_logic;
          EX_uip     : out std_logic;
          EX_ben     : out std_logic_vector (3 downto 0);
          EX_uwbc    : out std_logic;
          EX_mispred : out std_logic;
          EX_dbghit  : out std_logic;
          EX_ivalid  : out std_logic;
          EX_addout  : out std_logic_vector(WIDTH-1 downto 0);

          WB_data_out : out std_logic_vector (WIDTH-1 downto 0);
          WB_rd       : out std_logic_vector (3 downto 0);
          WB_rfwe     : out std_logic;
          WB_stall    : out std_logic;
          WB_ldwbreg  : out std_logic_vector (3 downto 0);
          WB_ldreg    : out std_logic_vector (3 downto 0);
          WB_uireg    : out std_logic_vector (3 downto 0);
          WB_rdeq     : out std_logic;
          WB_rbeq     : out std_logic);
end EXunit;

architecture Behavioral of EXunit is
    
    component alu
        generic (WIDTH : integer);
        port (a       : IN  std_logic_vector(WIDTH-1 downto 0);
              b       : IN  std_logic_vector(WIDTH-1 downto 0);
              op      : IN  std_logic_vector(1 downto 0);
              grp_sel : IN  std_logic_vector(1 downto 0);
              ci      : IN  std_logic;
              c       : OUT std_logic_vector(WIDTH-1 downto 0);
              ao      : OUT std_logic_vector(WIDTH-1 downto 0);
              co      : OUT std_logic;
              nf      : OUT std_logic;
              zf      : OUT std_logic;
              ov      : OUT std_logic);
    end component;

    component genben
        generic (SLICE : integer ) ;
        port (addr : in  std_logic_vector (1 downto 0);
              half : in  std_logic;
              byte : in  std_logic;
              word : in  std_logic;
              ben  : out std_logic_vector (3 downto 0)); 
    end component;

    component tfflagmod
        generic (SLICE : integer);
        port (flagop : in  std_logic_vector (2 downto 0);
              alu_zf : in  std_logic;
              alu_co : in  std_logic;
              alu_nf : in  std_logic;
              tfflag : out std_logic);
    end component;

    component WBSBLogic
        generic (USER_INST : boolean;
                 WIDTH     : integer);
        port (clk          : in  std_logic;
              MEM_ld_done  : in  std_logic;
              MEM_Nbusy    : in  std_logic;
              MULT_mdone   : in  std_logic;
              MULT_rd      : in  std_logic_vector(3 downto 0);
              UINST_uip    : in  std_logic;
              RF_load      : in  std_logic;
              RF_store     : in  std_logic;
              RF_rb        : in  std_logic_vector(3 downto 0);
              RF_rd        : in  std_logic_vector(3 downto 0);
              RF_msu       : in  std_logic;
              RF_uinst     : in  std_logic;
              PCT_stall    : in  std_logic;
              load         : out std_logic;
              store        : out std_logic;
              lsu          : out std_logic;
              ldreg        : out std_logic_vector(3 downto 0);
              ldwbreg      : out std_logic_vector(3 downto 0);
              mult_ip      : out std_logic;
              mwb_complete : out std_logic;
              mult_done    : out std_logic;
              uinst_ip     : out std_logic;
              uwb_complete : out std_logic;
              uinst_done   : out std_logic;
              uireg        : out std_logic_vector(3 downto 0));
    end component;

    signal wp_hit 	: std_logic_vector (1 downto 0) := "00";
    
    signal extpc        : std_logic_vector (WIDTH-1 downto 0) := (others => '0');  -- zero extented PC
    signal op_a         : std_logic_vector (WIDTH-1 downto 0) := (others => '0');  -- operand a to ALU
    signal op_b         : std_logic_vector (WIDTH-1 downto 0) := (others => '0');  -- operand b to ALU
    signal alu_out      : std_logic_vector (WIDTH-1 downto 0) := (others => '0');  -- out put from ALU
    signal add_out      : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal rega_temp    : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal regb_temp    : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal extimm       : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal wb_data      : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal alu_ci       : std_logic := '0';
    signal alu_co       : std_logic := '0';
    signal alu_nf       : std_logic := '0';
    signal alu_zf       : std_logic := '0';
    signal alu_ov       : std_logic := '0';
    signal rd           : std_logic_vector (3 downto 0) := (others => '0');
    signal wbstall_case : std_logic_vector (3 downto 0) := (others => '0');
    signal wbstall      : std_logic := '0';
    signal tfflag       : std_logic := '0';
    signal rfwe         : std_logic := '0';
    signal wbrfwe       : std_logic := '0';
    signal btaken       : std_logic := '0';
    signal cyflag       : std_logic := '0';
    signal fwd_tfflag   : std_logic := '0';
    signal load         : std_logic := '0';
    signal store        : std_logic := '0';
    signal lsu          : std_logic := '0';      
    signal mult_done    : std_logic := '0';
    signal uinst_done   : std_logic := '0';
    signal ben          : std_logic_vector (3 downto 0) := (others => '0');
    signal ldreg        : std_logic_vector (3 downto 0) := (others => '0');
    signal Nstall 	: std_logic := '0';

    -- registers
    signal exout     : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal exrfwe    : std_logic                           := '0';
    signal exrd      : std_logic_vector (3 downto 0)       := "0000";
    signal exrd_c0   : std_logic_vector (3 downto 0)       := "0000";
    signal sfrwe     : std_logic                           := '0';
    signal excyflag  : std_logic                           := '0';
    signal extfflag  : std_logic                           := '0';
    signal cywe      : std_logic                           := '0';
    signal extfwe    : std_logic                           := '0';
    signal exload    : std_logic                           := '0';
    signal exstore   : std_logic                           := '0';
    signal mult_ip   : std_logic                           := '0';
    signal uinst_ip  : std_logic                           := '0';
    signal ldwbreg   : std_logic_vector (3 downto 0)       := "0000";
    signal uireg     : std_logic_vector (3 downto 0)       := "0000";
    signal sfrreg    : std_logic_vector (3 downto 0)       := "0000";
    signal regaluout : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal mispred   : std_logic                           := '0';
    signal exdbghit  : std_logic                           := '0';
    signal exivalid  : std_logic                           := '0';
    signal exwphit   : std_logic_vector (1 downto 0) 	   := "00";
    signal mwb_complete  : std_logic := '0';
    signal uwb_complete  : std_logic := '0';
    
    signal rdeq    : std_logic := '0';
    signal rbeq    : std_logic := '0';

--    attribute RLOC of ALU_INST     : label is rloc_string(0, 2);--
--    attribute RLOC of EXFLAGMOD    : label is rloc_string(4, 5, true, 7, 1);--
    attribute RLOC of WBSBLogic_1  : label is rloc_string(0,2,true,-1,-3);

    attribute BEL : string;
begin  -- Behavioral

    Nstall <= not PCT_stall;
    
    -- zero extend RF_program counter
    extend_pc: for i in 0 to WIDTH-1 generate
        a: if i <= ADDR_WIDTH-1 generate
            extpc(i) <= RF_pc (i);
        end generate a;

        b: if i > ADDR_WIDTH-1 generate
            extpc(i) <= '0';
        end generate b;
    end generate extend_pc;

    -- extend the MSB of RF_imm
    extend_imm: for i in 0 to WIDTH-1 generate
        a: if i < RF_IMM_WIDTH-1 generate
            extimm(i) <= RF_imm (i);
        end generate a;
        b: if i >= RF_IMM_WIDTH-1 generate
            extimm(i) <= RF_imm (RF_IMM_WIDTH-1);
        end generate b;
    end generate extend_imm;
    
    -- update EX state register
    EX_reg_proc : process (clk)
    begin
        if rising_edge(clk) then
            if IAGU_anull = '1' then  -- sync reset
                mispred  <= '0' after 1 ns;
            elsif PCT_stall = '0' then                   -- clock enable
                mispred  <= RF_Br and not btaken after 1 ns;
            end if;
        end if;
    end process EX_reg_proc;

    --
    -- Hardware watchpoint hit when load/store Address compares
    --
    hwwp_true: if HW_WPENB = true generate
        wp_hit(0) <= ((RF_load or RF_store) and RF_ivalid) when
                     (SFR_wp_enb(0) = '1' and add_out(ADDR_WIDTH-1 downto 2) = SFR_wp0(ADDR_WIDTH-1 downto 2)) else '0';
        wp_hit(1) <= ((RF_load or RF_store) and RF_ivalid) when        
                     (SFR_wp_enb(1) = '1' and add_out(ADDR_WIDTH-1 downto 2) = SFR_wp1(ADDR_WIDTH-1 downto 2)) else '0';
    end generate hwwp_true;
    
    -- write back registers behave differently from others
    -- they are to be updated even if irrespective of stall
    -- once an instruction passes the RF stage it will complete
    -- i.e. it cannot be stalled.
    EX_wb_proc : process (clk, INTR_reset)
    begin
        if INTR_reset = '1' then
            exload   <= '0';
            exstore  <= '0';
            cywe     <= '0';
            extfflag <= '0';
            exdbghit <= '0';
            exivalid <= '0';
            exwphit  <= "00";
            exrd     <= (others => '0');
        elsif rising_edge(clk) then
            exload     <= load                                             after 1 ns;
            exstore    <= store                                            after 1 ns;
            cywe       <= RF_cywe and not PCT_stall                        after 1 ns;
            extfflag   <= tfflag                                           after 1 ns;
            exdbghit   <= (RF_sstep or wp_hit(0) or wp_hit(1)) and not PCT_stall after 1 ns;
            exwphit(0) <= wp_hit(0) and not PCT_stall                      after 1 ns;
            exwphit(1) <= wp_hit(1) and not PCT_stall                      after 1 ns;
            exrd       <= rd	    after 1 ns;
            exivalid   <= RF_ivalid                                        after 1 ns;
        end if;
    end process EX_wb_proc;

    generic_tech: if Technology /= "XILINX" generate
        process (clk, INTR_reset)
        begin
            if INTR_reset = '1' then
                exrfwe    <= '0';
                exout     <= (others => '0');
                regaluout <= (others => '0');
                sfrreg    <= (others => '0');
                sfrwe     <= '0';
                extfwe    <= '0';
                excyflag  <= '0';
            elsif rising_edge(clk) then
                exrfwe    <= wbrfwe     after 1 ns;
                exout     <= wb_data    after 1 ns;
                regaluout <= alu_out    after 1 ns;
                sfrreg    <= RF_rd      after 1 ns;
                sfrwe     <= RF_sfrwe and not PCT_stall after 1 ns;
                extfwe    <= RF_tfwe  and not PCT_stall after 1 ns;
                excyflag  <= alu_co     after 1 ns;
            end if;
        end process;

        EX_mip      <= mult_ip  and (not mwb_complete);
    end generate generic_tech;

    xilinx_tech: if Technology = "XILINX" generate
        signal sfrwe_comb, tfwe, std_ce   : std_logic;
       
        attribute RLOC of CYF_REG : label is rloc_string(2, 5, 1, 2, true, 14, 1);
        attribute BEL  of CYF_REG : label is bel_string(1);
        attribute RLOC of SFRWE_REG  : label is rloc_string(WIDTH/2, 7, 0, WIDTH/2, true, 0, 3);
        attribute BEL  of SFRWE_REG  : label is bel_string(1);
        attribute RLOC of EXRFWE_REG : label is rloc_string((WIDTH/2)+1, -1, 1, ((WIDTH/2)+1), true, -1, -5);--
        attribute BEL  of EXRFWE_REG : label is bel_string_ff(0);--
        attribute RLOC of SFRREG0 : label is rloc_string(WIDTH/2, 8, true, -((WIDTH/2)+1),4);--
        attribute RLOC of SFRREG1 : label is rloc_string(WIDTH/2, 8, true, -((WIDTH/2)+1), 4);--
        attribute RLOC of EXRD_C0REGS : label is rloc_string((WIDTH/2),   -2, true, -((WIDTH/2)+2), -6);    
        attribute INIT of EXTFWE_LUT : label is "2";
        attribute RLOC of EXTFWE_LUT : label is rloc_string(8, 4, 0, 8, true,8,0);
        attribute BEL  of EXTFWE_LUT : label is bel_string(0);
        attribute RLOC of EXTFWE_REG : label is rloc_string(8, 4, 0, 8, true,8,0);
        attribute BEL  of EXTFWE_REG : label is bel_string_ff(0);
        attribute INIT of EXMIP_INST : label is "4";
        attribute RLOC of EXMIP_INST : label is rloc_string((WIDTH/2)+1, 5, 1, (WIDTH/2)+1, true, -1, 1);
        attribute BEL  of EXMIP_INST : label is bel_string(0);
    begin
        CYF_REG : FDC
            generic map (INIT => '0')
            port map (Q => excyflag, C => clk, D => alu_co, CLR => INTR_reset);
    
        sfrwe_comb <= RF_sfrwe and not PCT_stall;
        SFRWE_REG : FDC
            generic map (INIT => '0')
            port map (Q => sfrwe, C => clk, D => sfrwe_comb, CLR => INTR_reset);

        EXRFWE_REG : FDC
            generic map (INIT => '0')
            port map (Q => exrfwe, C => clk, D => wbrfwe, CLR => INTR_reset);

        SFRREG0 : FDC_VECTOR
            generic map (WIDTH => 2, SLICE => 0)
            port map (CLK => clk, CLR => INTR_reset, D => RF_rd(1 downto 0), Q => sfrreg(1 downto 0));
        SFRREG1 : FDC_VECTOR
            generic map (WIDTH => 2, SLICE => 1)
            port map (CLK => clk, CLR => INTR_reset, D => RF_rd(3 downto 2), Q => sfrreg(3 downto 2));

        EXRD_C0REGS : FDC_VECTOR
            generic map (WIDTH => 4, SLICE  => 1)
            port map (CLK => clk, CLR => INTR_reset, D   => rd, Q   => exrd_c0);
    
        EXTFWE_LUT : LUT2
            generic map (INIT => X"2")
            port map (O  => tfwe, I0 => RF_tfwe, I1 => PCT_stall);
        EXTFWE_REG : FDC
            generic map (INIT => '0')
            port map (Q => extfwe, C => clk, D => tfwe, CLR => INTR_reset);

        xv2_a: if FPGA_Family = "Virtex2" generate
            attribute RLOC of REGALUOUT_FD : label is rloc_string(0, 6, true, 0, 6);
        begin
            REGALUOUT_FD : FD_ALT_VECTOR_SS
                generic map (WIDTH => WIDTH, XORY  => 1)
                port map (CLK => clk, D => alu_out, Q => regaluout);            
        end generate xv2_a;
        nxv2_a: if FPGA_Family /= "Virtex2" generate
            attribute RLOC of REGALUOUT_FD : label is rloc_string(0, 6, true, 0, 6);
        begin
            REGALUOUT_FD : FD_ALT_VECTOR_SS
                generic map (WIDTH => WIDTH, XORY  => 0)
                port map (CLK => clk, D => alu_out, Q => regaluout);                        
        end generate nxv2_a;

        xv2_b: if FPGA_Family = "Virtex2" generate
            attribute RLOC of EXOUT_REGS   : label is rloc_string(0, 6, true, 0, 6);  --
        begin
            EXOUT_REGS : FD_ALT_VECTOR_SS
                generic map (WIDTH => WIDTH, XORY  => 0)
                port map (CLK => clk, D   => wb_data, Q   => exout);            
        end generate xv2_b;
        nxv2_b: if FPGA_Family /= "Virtex2" generate
            attribute RLOC of EXOUT_REGS   : label is rloc_string(0, 6, true, 0, 6);  --
        begin
            EXOUT_REGS : FD_ALT_VECTOR_SS
                generic map (WIDTH => WIDTH, XORY  => 1)
                port map (CLK => clk, D   => wb_data, Q   => exout);            
        end generate nxv2_b;

        std_ce <= store;
        EXMIP_INST : LUT2
            generic map (INIT => X"4")
            port map (O  => EX_mip, I1 => mult_ip, I0 => mwb_complete);    

    end generate xilinx_tech;

    EX_wphit	<= exwphit;
    EX_bphit	<= RF_bphit and not mispred;
    EX_load     <= load; 
    EX_store    <= store;
    EX_lsu      <= lsu;
    EX_out      <= exout;
    EX_sfrwe    <= sfrwe after 1 ns;
    EX_cyflag   <= excyflag;
    EX_tfflag   <= extfflag;
    EX_cywe     <= cywe;
    EX_tfwe     <= extfwe;
    EX_addr     <= regaluout (ADDR_WIDTH-1 downto 0);
    EX_st_data  <= rega_temp;
    EX_sfrreg   <= sfrreg after 1 ns;
    EX_sfrval   <= regaluout after 1 ns;

    EX_ben      <= ben;
    EX_mispred  <= mispred;
    EX_addout   <= add_out;
    EX_dbghit   <= exdbghit or (RF_bphit and not mispred);
    EX_ivalid   <= exivalid;
    
    rdeq      <= '1' when (exrfwe & RF_rd) = ('1' & exrd) else '0';    
    rbeq      <= '1' when (exrfwe & RF_rb) = ('1' & exrd) else '0';
    
    generic_tech1: if Technology /= "XILINX"  generate
        signal rdsel1, rbsel1 : std_logic;
    begin
        -- select operand a forwarding value
        rega_temp <= exout when rdeq = '1' else RF_rdval;

        -- select operand b forwarding value
        regb_temp <= exout when rbeq = '1' else RF_rbval;

        op_a      <= extimm    when RF_asel = ALU_ASEL_IMM else
                     extpc     when RF_asel = ALU_ASEL_PC  else
                     rega_temp when RF_asel = ALU_ASEL_RD  else
                     conv_std_logic_vector(0, WIDTH);

        op_b      <= extimm     when RF_bsel = ALU_BSEL_IMM  else
                     SFR_sfrval when RF_bsel = ALU_BSEL_SFR  else
                     regb_temp  when RF_bsel = ALU_BSEL_RB   else
                     conv_std_logic_vector(0,WIDTH);    
        alu_ci      <= RF_aluop(1) and cyflag;
    end generate generic_tech1;

    xilinx_tech1: if Technology = "XILINX" generate
        signal rdsel1, rbsel1 : std_logic;
        attribute RLOC of ASEL_MUX   : label is rloc_string(0, 0, true, 0, -6); --
        attribute RLOC of BSEL_MUX   : label is rloc_string(0, 1, true, 0, -3); --
        attribute INIT of ALUCI_LUT : label is "E400";
        attribute RLOC of ALUCI_LUT : label is rloc_string((WIDTH/2)+1, 1, 1, ((WIDTH/2)+1), true, -1, -3);
        attribute BEL  of ALUCI_LUT : label is bel_string(1);
    begin    
        rdsel1 <= not RF_asel(1);
        ASEL_MUX: MUX4DO_1E_VECTOR
            generic map (WIDTH => WIDTH, ADJ => 1)
            port map (S0 => RF_asel(0), S0_D => rdeq,   S1 => rdsel1,
                      EN => '1',          V0 => extimm, V1 => extpc,
                      V2 => RF_rdval,     V3 => exout,  O  => op_a, DO => rega_temp);

        rbsel1 <= not RF_bsel(1);
        BSEL_MUX: MUX4DO_1E_VECTOR
            generic map (WIDTH => WIDTH, ADJ => 0)
            port map (S0 => RF_bsel(0), S0_D => rbeq,   S1 => rbsel1,
                      EN => '1',          V0 => extimm, V1 => SFR_sfrval,
                      V2 => RF_rbval,     V3 => exout,  O  => op_b, DO => regb_temp);

        ALUCI_LUT : LUT4
            generic map (INIT => X"E400")
            port map (O  => alu_ci, I0 => cywe, I1 => SFR_cyflag,
                      I2 => excyflag, I3 => RF_aluop(1));    

    end generate xilinx_tech1;
    
    EX_rbval  <= regb_temp;
    EX_rdval  <= rega_temp;
    EX_opb    <= op_b;
    
    -- forward the carry flag
    cyflag      <= excyflag when cywe = '1'  else SFR_cyflag;
    
    ALU_INST : alu generic map (WIDTH => WIDTH)
        port map (a       => op_a,
                  b       => op_b,
                  op      => RF_aluop(1 downto 0),
                  grp_sel => RF_aluop(3 downto 2),
                  ci      => alu_ci,
                  c       => alu_out,
                  ao      => add_out,
                  co      => alu_co,
                  nf      => alu_nf,
                  zf      => alu_zf,
                  ov      => alu_ov);

    ---------------------------------------------------------------------------
    --                    compute byte enable array
    ---------------------------------------------------------------------------
    genben_inst : genben
        generic map (SLICE => 0)
        port map (addr => alu_out(1 downto 0),
                  half => RF_half,
                  byte => RF_byte,
                  word => RF_word,
                  ben  => ben);
    
    ---------------------------------------------------------------------------
    --                  True/False flag related logic
    ---------------------------------------------------------------------------    
    exflagmod: tfflagmod
        generic map (SLICE => 0)
        port map (flagop => RF_flagop,
                  alu_zf => alu_zf,
                  alu_co => alu_co,
                  alu_nf => alu_nf,
                  tfflag => tfflag);

    generic_tech2: if Technology /= "XILINX" generate
        -- forward T(rue)/F(alse) flag
        fwd_tfflag <= extfflag when extfwe = '1' else SFR_tfflag;
        
        -- compute rfwe. if conditional instruction then it depends
        -- on the instruction else pass thru RF_rfwe
        -- type   : combinational
        -- inputs : RF_iscond, RF_condT, SFR_tfflag, RF_rfwe
        -- outputs: rfwe
        rfwe_proc: process (RF_iscond, RF_condT, fwd_tfflag, RF_rfwe)
        begin  -- process rfwe_proc
            if RF_iscond = '1' then
                if RF_condT = '1' then
                    rfwe <= RF_rfwe and fwd_tfflag;
                else
                    rfwe <= RF_rfwe and not fwd_tfflag;
                end if;
            else
                rfwe <= RF_rfwe;
            end if;
        end process rfwe_proc;

        -- purpose: compute branch taken
        -- type   : combinational
        -- inputs : RF_Br, RF_BrUnc, RF_condT, SFR_tfflag
        -- outputs: btaken
        btaken_proc: process (RF_Br, RF_BrUnc, RF_condT, fwd_tfflag)
        begin  -- process btaken_proc
            if RF_Br = '1' and RF_BrUnc = '0' then
                if RF_condT = '1' then
                    btaken <= fwd_tfflag;
                else
                    btaken <= not fwd_tfflag;
                end if;
            else
                btaken <= RF_BrUnc;
            end if;
        end process btaken_proc;
        
    end generate generic_tech2;

    xilinx_tech2: if Technology = "XILINX" generate
        attribute INIT of btaken_rom     : label is "F9F0";
        attribute INIT of fwd_tfflag_rom : label is "E2";
        attribute INIT of rfwe_rom       : label is "82AA";
        
        attribute RLOC of fwd_tfflag_rom : label is rloc_string(8, 5, 1, 8, true, 8, 1);
        attribute BEL  of fwd_tfflag_rom : label is bel_string(1);
        attribute RLOC of rfwe_rom   : label is rloc_string(8, 4, 0, 8, true,8,0);
        attribute BEL  of rfwe_rom   : label is bel_string(1);

    begin
        -- forward T(rue)/F(alse) flag
        fwd_tfflag_rom: LUT3
            generic map (INIT => X"E2")
            port map (O  => fwd_tfflag, I0 => SFR_tfflag, I1 => extfwe, I2 => extfflag);
        
        -- compute rfwe. if conditional instruction then it depends
        -- on the instruction else pass thru RF_rfwe
        rfwe_rom: LUT4
            generic map (INIT => X"82AA")
            port map (O  => rfwe,
                      I0 => RF_rfwe, I1 => fwd_tfflag, I2 => RF_condT, I3 => RF_iscond);

        -- compute branch taken
        btaken_rom : LUT4
            generic map (INIT => X"F9F0")
            port map (O  => btaken,
                      I0 => fwd_tfflag, I1 => RF_condT, I2 => RF_BrUnc, I3 => RF_Br);
        
    end generate xilinx_tech2;
    ---------------------------------------------------------------------------
    -- Write back related logic
    ---------------------------------------------------------------------------

    WBSBLogic_1: WBSBLogic
        generic map (USER_INST => USER_INST,
                     WIDTH     => WIDTH)
        port map (clk          => clk,
                  MEM_ld_done  => MEM_ld_done,
                  MEM_Nbusy    => MEM_Nbusy,
                  MULT_mdone   => MULT_mdone,
                  MULT_rd      => MULT_rd,
                  UINST_uip    => UINST_uip,
                  RF_load      => RF_load,
                  RF_store     => RF_store,
                  RF_rb        => RF_rb,
                  RF_rd        => RF_rd,
                  RF_msu       => RF_msu,
                  RF_uinst     => RF_uinst,
                  PCT_stall    => PCT_stall,
                  load         => load,
                  store        => store,
                  lsu          => lsu,
                  ldreg        => ldreg,
                  ldwbreg      => ldwbreg,
                  mult_ip      => mult_ip,
                  mwb_complete => mwb_complete,
                  mult_done    => mult_done,
                  uinst_ip     => EX_uip,
                  uwb_complete => uwb_complete,
                  uinst_done   => uinst_done,
                  uireg        => uireg);

    ---------------------------------------------------------------------------
    -- Multiplex write back data and write back register and rfwe signals
    -- priority is 1) load, 2) multiply, 3) user instruction then 4) ex stage
    ---------------------------------------------------------------------------

    wb_stall_block : block
        signal wb_sel : std_logic_vector (1 downto 0) := "00";
    begin
        
        -- WB_stall is set when multiple units finish at the same time
        -- the write back needs to be serialized. So it will be set if
        -- if any two of the units finish together.
        wbstall_case <= MEM_ld_done 			&
                        (mult_done and (not mwb_complete))  &
                        (uinst_done and (not mwb_complete)) &
                        rfwe;

        wbsel_proc: process (wbstall_case)
        begin
            case wbstall_case is
                when "1000" => wb_sel <= "00";
                when "1001" => wb_sel <= "00";
                when "1010" => wb_sel <= "00";
                when "1011" => wb_sel <= "00";
                when "1100" => wb_sel <= "00";
                when "1101" => wb_sel <= "00";
                when "1110" => wb_sel <= "00";
                when "1111" => wb_sel <= "00";
                when "0100" => wb_sel <= "01";
                when "0101" => wb_sel <= "01";
                when "0110" => wb_sel <= "01";
                when "0111" => wb_sel <= "01";
                when "0010" => wb_sel <= "10";
                when "0011" => wb_sel <= "10";
                when "0001" => wb_sel <= "11";
                when "0000" => wb_sel <= "11";
                when others => null;
            end case;
        end process wbsel_proc;

        wb_data <= MEM_dbus  when wb_sel = "00" else
                   MULT_out  when wb_sel = "01" else
                   UINST_out when wb_sel = "10" else
                   alu_out;            
                   
        rd     <= ldreg    when wb_sel = "00" else
                  MULT_rd  when wb_sel = "01" else
                  uireg    when wb_sel = "10" else
                  RF_rd;
        
        wbrfwe  <= '1' when (MEM_ld_done = '1')		     or
                   (mult_done  = '1' and mwb_complete = '0') or
                   (uinst_done = '1' and uwb_complete = '0') or
                   (rfwe       = '1' and PCT_stall = '0')    else '0';

        wb_stall_proc : process (wbstall_case)
        begin
            case wbstall_case is
                when "0000" => wbstall <= '0';
                when "0001" => wbstall <= '0';
                when "0010" => wbstall <= '0';
                when "0100" => wbstall <= '0';
                when "1000" => wbstall <= '0';
                when others => wbstall <= '1';
            end case;
        end process wb_stall_proc;
        
    end block wb_stall_block;
    
    WB_data_out <= exout;
    WB_rd       <= exrd;
    WB_rbeq 	<= rbeq and RF_bsel(1);
    WB_rdeq     <= rdeq and (RF_asel(1) or RF_store);
    WB_rfwe     <= exrfwe;
    WB_stall    <= wbstall;
    WB_uireg    <= uireg;
    WB_ldwbreg  <= ldwbreg;
    WB_ldreg    <= ldreg;
    EX_uwbc     <= uwb_complete;
end Behavioral;
