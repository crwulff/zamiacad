------------------------------------------------------------------------------
-- Title      : RFUnit
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : rfunit.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-12-27
-- Last update: 2006-08-01
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Register File read / write and some decode
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-12-27  1.0      sandeep	Created
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

entity RFunit is
    generic (WIDTH      : integer := 32;
             USER_INST  : Boolean := True;
             HW_BPENB   : boolean := False);

    port (DE_load      : in std_logic;
          DE_store     : in std_logic;
          DE_half      : in std_logic;
          DE_byte      : in std_logic;
          DE_word      : in std_logic;
          DE_iscond    : in std_logic;
          DE_condT     : in std_logic;
          DE_Br        : in std_logic;
          DE_BrUnc     : in std_logic;
          DE_imm       : in std_logic_vector (DE_IMM_WIDTH-1 downto 0);  -- immediate value
          DE_immSigned : in std_logic;  -- immediate is signed
          DE_immSHL    : in std_logic_vector (1 downto 0);  -- left shift imm amount
          DE_immAlign4 : in std_logic;  -- immediate needs to ne aligned on 4
          DE_rd        : in std_logic_vector (3 downto 0);  -- rd register number
          DE_rb        : in std_logic_vector (3 downto 0);  -- rb register number
          DE_asel      : in std_logic_vector (1 downto 0);  -- A operand select
          DE_bsel      : in std_logic_vector (1 downto 0);  -- B operand select
          DE_flagop    : in std_logic_vector (2 downto 0);  -- flag operation
          DE_pc        : in std_logic_vector (ADDR_WIDTH-1 downto 0);
          DE_rfwe      : in std_logic;
          DE_sfrwe     : in std_logic;
          DE_swi       : in std_logic;
          DE_cywe      : in std_logic;
          DE_tfwe      : in std_logic;
          DE_BrLink    : in std_logic;
          DE_aluop     : in std_logic_vector (3 downto 0);  -- alu operation
          DE_alu       : in std_logic;
          DE_uinst     : in std_logic;
          DE_sstep     : in std_logic;
          DE_ivalid    : in std_logic;
          DE_uiop      : in std_logic_vector (1 downto 0);

          SFR_bp_enb : in std_logic_vector (1 downto 0);
          SFR_bp0    : in std_logic_vector (ADDR_WIDTH-1 downto 0);
          SFR_bp1    : in std_logic_vector (ADDR_WIDTH-1 downto 0);
          
          IAGU_anull   : in std_logic;
          IAGU_deregbr : in std_logic;

          PCT_stall : in std_logic;

          WB_data_out : in std_logic_vector (WIDTH-1 downto 0);
          WB_rd       : in std_logic_vector (3 downto 0);
          WB_rdeq     : in std_logic;
          WB_rbeq     : in std_logic;
          WB_rfwe     : in std_logic;

          clk : in std_logic;

          RF_load   : out std_logic;
          RF_store  : out std_logic;
          RF_half   : out std_logic;
          RF_byte   : out std_logic;
          RF_word   : out std_logic;
          RF_iscond : out std_logic;
          RF_condT  : out std_logic;
          RF_Br     : out std_logic;
          RF_BrUnc  : out std_logic;
          RF_imm    : out std_logic_vector (RF_IMM_WIDTH-1 downto 0);
          RF_asel   : out std_logic_vector (1 downto 0);
          RF_bsel   : out std_logic_vector (1 downto 0);
          RF_rdval  : out std_logic_vector (WIDTH-1 downto 0);
          RF_rbval  : out std_logic_vector (WIDTH-1 downto 0);
          RF_pc     : out std_logic_vector (ADDR_WIDTH-1 downto 0);
          RF_rfwe   : out std_logic;
          RF_sfrwe  : out std_logic;
          RF_swi    : out std_logic;
          RF_swinum : out std_logic_vector(SWINUM_WIDTH-1 downto 0);
          RF_cywe   : out std_logic;
          RF_tfwe   : out std_logic;
          RF_aluop  : out std_logic_vector (3 downto 0);
          RF_rd     : out std_logic_vector (3 downto 0);
          RF_rb     : out std_logic_vector (3 downto 0);
          RF_flagop : out std_logic_vector (2 downto 0);
          RF_alu    : out std_logic;
          RF_lsu    : out std_logic;
          RF_msu    : out std_logic;
          RF_rfi    : out std_logic;
          RF_uinst  : out std_logic;
          RF_sstep  : out std_logic;
          RF_ivalid : out std_logic;
          RF_uiop   : out std_logic_vector (1 downto 0);
          RF_shift  : out std_logic;
          RF_lsmsu  : out std_logic;
          RF_dreg   : out std_logic;
          RF_bphit  : out std_logic);

end RFunit;     

architecture Behavioral of RFunit is

    component regfile
        generic (WIDTH : integer);
        port(DE_rd    : in  std_logic_vector(3 downto 0);
             rb       : in  std_logic_vector(3 downto 0);
             rdw      : in  std_logic_vector(3 downto 0);
             rfwe     : in  std_logic;
             rdstallu : in  std_logic;
             rbstallu : in  std_logic;
             rd_in    : in  std_logic_vector(WIDTH-1 downto 0);
             clk      : in  std_logic;
             rd_out   : out std_logic_vector(WIDTH-1 downto 0);
             rb_out   : out std_logic_vector(WIDTH-1 downto 0));
    end component;
    
    constant two : std_logic_vector (RF_IMM_WIDTH-1 downto 0) := conv_std_logic_vector(2,RF_IMM_WIDTH);

    
    signal extImm   : std_logic_vector (RF_IMM_WIDTH-1 downto 0);
    signal shftImm  : std_logic_vector (RF_IMM_WIDTH-1 downto 0);
    signal finImm   : std_logic_vector (RF_IMM_WIDTH-1 downto 0);
    signal rd_out   : std_logic_vector (WIDTH-1 downto 0);
    signal rdmout   : std_logic_vector (WIDTH-1 downto 0);
    signal rb_out   : std_logic_vector (WIDTH-1 downto 0);
    signal rbmout   : std_logic_vector (WIDTH-1 downto 0);
    signal alu      : std_logic;
    signal msu      : std_logic;
    signal rfwe     : std_logic;
    signal asel_1   : std_logic;
    signal rfi      : std_logic;
    signal rd_updt  : std_logic;
    signal rb_updt  : std_logic;
    signal rdstallu : std_logic;
    signal rbstallu : std_logic;
    signal rbr      : std_logic_vector (3 downto 0);
    signal shift    : std_logic;
    signal Nstall   : std_logic;
    signal bp_hit   : std_logic := '0';
    
    -- registers
    signal load     : std_logic                                  := '0';
    signal store    : std_logic                                  := '0';
    signal half     : std_logic                                  := '0';
    signal byte     : std_logic                                  := '0';
    signal word     : std_logic                                  := '0';
    signal rfimm    : std_logic_vector (RF_IMM_WIDTH-1 downto 0) := (others => '0');
    signal iscond   : std_logic                                  := '0';
    signal condT    : std_logic                                  := '0';
    signal Br       : std_logic                                  := '0';
    signal BrUnc    : std_logic                                  := '0';
    signal asel     : std_logic_vector (1 downto 0)              := "00";
    signal bsel     : std_logic_vector (1 downto 0)              := "00";
    signal pc       : std_logic_vector (ADDR_WIDTH-1 downto 0)   := (others => '0');
    signal rfrfwe   : std_logic                                  := '0';
    signal sfrwe    : std_logic                                  := '0';
    signal swi      : std_logic                                  := '0';
    signal swinum   : std_logic_vector (SWINUM_WIDTH-1 downto 0) := (others => '0');
    signal cywe     : std_logic                                  := '0';
    signal tfwe     : std_logic                                  := '0';
    signal aluop    : std_logic_vector (3 downto 0)              := "0000";
    signal rd       : std_logic_vector (3 downto 0)              := "0000";
    signal rb       : std_logic_vector (3 downto 0)              := "0000";
    signal flagop   : std_logic_vector (2 downto 0)              := "000";
    signal rdval    : std_logic_vector (WIDTH-1 downto 0)        := (others => '0');
    signal rbval    : std_logic_vector (WIDTH-1 downto 0)        := (others => '0');
    signal rfmsu    : std_logic                                  := '0';
    signal rfrfi    : std_logic                                  := '0';
    signal rfuinst  : std_logic                                  := '0';
    signal rfivalid : std_logic                                  := '0';
    signal rfuiop   : std_logic_vector (1 downto 0)              := "00";
    signal rfshift  : std_logic                                  := '0';
    signal lsmsu    : std_logic                                  := '0';
    signal dreg     : std_logic                                  := '0';
    signal rfsstep  : std_logic                                  := '0';
    signal rfbphit  : std_logic                                  := '0';
    
    attribute BEL : string;
    attribute RLOC of INST_REGFILE : label is rloc_string(0,1,true,0,4);

begin  -- Behavioral

    Nstall <= not PCT_stall;

    process (clk)
    begin
        if rising_edge(clk) then
            if IAGU_anull = '1' then
                rfivalid <= '0' after 1 ns;
                rfbphit  <= '0' after 1 ns;
            elsif Nstall = '1' then
                rfivalid <= DE_ivalid after 1 ns;
                rfbphit  <= bp_hit    after 1 ns;
            end if;
        end if;
    end process ;
        
    --
    -- Hardware breakpoint hit when RF_pc compares
    --
    hwbp_true: if HW_BPENB = true generate
        bp_hit <= DE_ivalid when (SFR_bp_enb(0) = '1' and DE_pc = SFR_bp0) or
                                 (SFR_bp_enb(1) = '1' and DE_pc = SFR_bp1) else '0';
    end generate hwbp_true;

    -- update RF stage registers
    RF_reg_proc: process (clk,IAGU_anull)
    begin
        if IAGU_anull = '1' then    -- async reset
            load     <= '0'    after 1 ns;
            half     <= '0'    after 1 ns;
            byte     <= '0'    after 1 ns;
            word     <= '0'    after 1 ns;
            rfrfwe   <= '0'    after 1 ns;
            rfmsu    <= '0'    after 1 ns;
            rfrfi    <= '0'    after 1 ns;
            rfuiop   <= "00"   after 1 ns;
            lsmsu    <= '0'    after 1 ns;
            dreg     <= '0'    after 1 ns;
            rfsstep  <= '0'    after 1 ns;
            rfimm    <= conv_std_logic_vector(0, RF_IMM_WIDTH) after 1 ns;
        elsif rising_edge(clk) then
            if Nstall = '1' then   -- clock enable                
                load     <= DE_load  after 1 ns;
                half     <= DE_half  after 1 ns;
                byte     <= DE_byte  after 1 ns;
                word     <= DE_word  after 1 ns;
                rfrfwe   <= rfwe     after 1 ns;
                rfmsu    <= msu      after 1 ns;
                rfrfi    <= rfi	     after 1 ns;
                rfuiop   <= DE_uiop  after 1 ns;
                lsmsu    <= (DE_load or DE_store or msu or rfwe or DE_uinst) after 1 ns;
                dreg     <= DE_asel(1) or DE_store after 1 ns;
                rfsstep  <= DE_sstep after 1 ns;
                rfimm    <= finImm   after 1 ns;
            end if;
        end if;
    end process RF_reg_proc;

    process (clk, IAGU_anull)
    begin
        if IAGU_anull = '1' then    -- async reset
            store   <= '0'                                    after 1 ns;
            iscond  <= '0'                                    after 1 ns;
            condT   <= '0'                                    after 1 ns;
            Br      <= '0'                                    after 1 ns;
            BrUnc   <= '0'                                    after 1 ns;
            asel    <= "00"                                   after 1 ns;
            bsel    <= "00"                                   after 1 ns;
            sfrwe   <= '0'                                    after 1 ns;
            cywe    <= '0'                                    after 1 ns;
            tfwe    <= '0'                                    after 1 ns;
            rd      <= "0000"                                 after 1 ns;
            rb      <= "0000"                                 after 1 ns;
            flagop  <= "000"                                  after 1 ns;
            rfuinst <= '0'                                    after 1 ns;
            rfshift <= '0'                                    after 1 ns;
            aluop    <= "0000" after 1 ns;
        elsif rising_edge(clk) then
            if Nstall = '1' then    -- clock enable                
                store   <= DE_store                             after 1 ns;
                iscond  <= DE_iscond                            after 1 ns;
                condT   <= DE_condT                             after 1 ns;
                Br      <= DE_Br                                after 1 ns;
                BrUnc   <= DE_BrUnc                             after 1 ns;
                asel    <= (DE_asel(1) and asel_1) & DE_asel(0) after 1 ns;
                bsel    <= DE_bsel                              after 1 ns;
                sfrwe   <= DE_sfrwe                             after 1 ns;
                cywe    <= DE_cywe                              after 1 ns;
                tfwe    <= DE_tfwe                              after 1 ns;
                rd      <= DE_rd                                after 1 ns;
                rb      <= DE_rb                                after 1 ns;
                flagop  <= DE_flagop                            after 1 ns;
                rfuinst <= DE_uinst                             after 1 ns;
                rfshift <= shift                                after 1 ns;
                aluop    <= DE_aluop after 1 ns;
            end if;
        end if;
    end process;
    
    -- Some RF stage registers need sync reset
    RF_pc_proc : process (clk)
    begin
        if rising_edge(clk) then
            if IAGU_anull = '1' then
                swi    <= '0'                                    after 1 ns;
                swinum <= conv_std_logic_vector(0, SWINUM_WIDTH) after 1 ns;
            elsif Nstall = '1' then
                swi    <= DE_swi   after 1 ns;
                swinum <= DE_rb    after 1 ns;
            end if;
        end if;
    end process RF_pc_proc;
        
    process (clk)
    begin
        if rising_edge(clk) then
            if IAGU_anull = '1' then
                pc <= conv_std_logic_vector(0, ADDR_WIDTH) after 1 ns;
            elsif Nstall = '1' then
                pc <= DE_pc after 1 ns;
            end if;
        end if;
    end process;
    
    RF_load   <= load;
    RF_store  <= store;
    RF_half   <= half;
    RF_byte   <= byte;
    RF_word   <= word;
    RF_imm    <= rfimm;
    RF_iscond <= iscond;
    RF_condT  <= condT;
    RF_Br     <= Br;
    RF_BrUnc  <= BrUnc;
    RF_asel   <= asel;
    RF_bsel   <= bsel;
    RF_pc     <= pc;
    RF_rfwe   <= rfrfwe;
    RF_cywe   <= cywe;
    RF_tfwe   <= tfwe;
    RF_swi    <= swi;
    RF_swinum <= swinum after 1 ns;
    RF_aluop  <= aluop;
    RF_sfrwe  <= sfrwe;
    RF_rd     <= rd;
    RF_rb     <= rb;
    RF_flagop <= flagop;
    RF_rdval  <= rdval;
    RF_rbval  <= rbval;
    RF_alu    <= '0';
    RF_lsu    <= load or store;
    RF_msu    <= rfmsu  after 1 ns;
    RF_rfi    <= rfrfi;
    RF_shift  <= rfshift;
    RF_lsmsu  <= lsmsu;
    RF_dreg   <= dreg;
    RF_sstep  <= rfsstep;
    RF_ivalid <= rfivalid;
    RF_bphit  <= rfbphit;
    
    UI_UNIT: if USER_INST = True generate
        RF_uinst    <= rfuinst;
        RF_uiop     <= rfuiop;        
    end generate UI_UNIT;
    NOUI_UNIT: if USER_INST = False generate
        RF_uinst    <= '0';
        RF_uiop     <= "00";                
    end generate NOUI_UNIT;
    
    -- sign/zero extend the imm to WIDTH
    szext_imm: for i in 0 to RF_IMM_WIDTH-1 generate
        szext_9_0: if i < DE_IMM_WIDTH generate
            extImm (i) <= DE_imm (i);
        end generate szext_9_0;
        szext_rest: if i >= DE_IMM_WIDTH generate
            extImm (i) <= DE_imm (DE_IMM_WIDTH-1) and DE_immSigned;
        end generate szext_rest;
    end generate szext_imm;

    -- shift it up as required
    shftimm_proc : process (extImm, DE_immSHL,DE_immAlign4, DE_pc)
    begin
        -- no shift required
        if DE_immSHL = IMM_SHL_0 then
            for i in 0 to RF_IMM_WIDTH-1 loop
                shftImm (i) <= extImm (i);                
            end loop;  -- i
        -- left shift by 1
        elsif DE_immSHL = IMM_SHL_1 then
            shftImm (0) <= '0';
            for i in 1 to RF_IMM_WIDTH-1 loop
                shftImm (i) <= extImm(i-1);
            end loop;  -- i
        -- left shift by 2
        elsif DE_immSHL = IMM_SHL_2 then
            shftImm (0) <= '0';
            shftImm (1) <= DE_immAlign4 and DE_pc(1);
            for i in 2 to RF_IMM_WIDTH-1 loop
                shftImm (i) <= extImm (i-2);
            end loop;  -- i
        -- zero immediate
        else
            shftImm <= conv_std_logic_vector(0,RF_IMM_WIDTH);
        end if;        
    end process shftimm_proc;

    finImm <= shftImm after 1 ns;
    
    inst_regfile : regfile
        generic map (WIDTH => WIDTH)
        port map (DE_rd    => DE_rd,
                  rb       => rbr,
                  rdw      => WB_rd,
                  rfwe     => WB_rfwe,
                  rdstallu => rdstallu,
                  rbstallu => rbstallu,
                  rd_in    => WB_data_out,
                  clk      => clk,
                  rd_out   => rd_out,
                  rb_out   => rb_out);

    -- we need to update rd/rb values even if core is stalled if there
    -- is a register file update and the rd/rb register numbers match the write
    -- back register
    rbstallu  <= PCT_stall and WB_rbeq;
    rdstallu  <= PCT_stall and WB_rdeq;
    
    rd_updt   <= (not PCT_stall) or rdstallu after 1 ns;
    rb_updt   <= (not PCT_stall) or rbstallu after 1 ns;
    
    -- need to mux wback data if they are being written in this cycle
    generic_tech2: if Technology /= "XILINX" generate
    begin
        rbr 	 <= DE_rb;

        rdval_proc : process (clk)
        begin
            if rising_edge(clk) then
                if rd_updt = '1' then
                    rdval    <= rd_out after 1 ns;                
                end if;
            end if;
        end process rdval_proc;

        rbval_proc : process (clk)
        begin
            if rising_edge(clk) then
                if rb_updt = '1' then
                    rbval    <= rb_out after 1 ns;
                end if;
            end if;
        end process rbval_proc;
        
    end generate generic_tech2;

    xilinx_tech2: if Technology = "XILINX" generate
        signal rbvalmux : std_logic_vector (WIDTH-1 downto 0);
        
        attribute RLOC of RDVAL_REGS   : label is rloc_string(0,1,true,0,4);
        attribute RLOC of RBVAL_REGS   : label is rloc_string(0,1,true,0,4);
    begin
        rbr <= DE_rb;
        rbvalmux <= WB_data_out when rbstallu = '1' else rb_out ;
        
        RDVAL_REGS : FDE_VECTOR
            generic map (WIDTH => WIDTH, SLICE  => 0)
            port map (CLK => clk,   CE  => rd_updt,
                      D   => rd_out,Q   => rdval);
        
        RBVAL_REGS : FDE_VECTOR
            generic map (WIDTH => WIDTH, SLICE => 1)
            port map (CLK => clk,   CE  => rb_updt,
                      D   => rbvalmux, Q  => rbval);                
    end generate xilinx_tech2;
    
    -- mult/shifter unit if aluop = "11xx"
    msu <= '1' when DE_alu = '1' and DE_aluop(3 downto 2) = "11" else '0';
    alu  <= (not msu) and (not DE_uinst);

    -- rfwe not set for multiply
    rfwe <= DE_rfwe and (not (msu or DE_uinst));

    -- some alu operations only require one operand
    asel_1_proc: process (DE_aluop)
    begin
        case DE_aluop is
            when "1011" => asel_1 <= '0';  -- zxh
            when "1010" => asel_1 <= '0';  -- sxh
            when "1001" => asel_1 <= '0';  -- zxb
            when "1000" => asel_1 <= '0';  -- sxb
            when "0100" => asel_1 <= '0';  -- mov
            when others => asel_1 <= '1';
        end case;
    end process asel_1_proc;

    shift_proc: process (DE_aluop)
    begin
        case DE_aluop is
            when "1111" => shift <= '1';  -- asr
            when "1101" => shift <= '1';  -- lsr  
            when "1100" => shift <= '1';  -- lsl
            when others => shift <= '0';
        end case;
    end process shift_proc;
        
    -- return from interrupt instruction
    generic_tech3: if Technology /= "XILINX" generate
        rfi <= '1' when IAGU_deregbr = '1' and DE_bsel = ALU_BSEL_SFR and DE_rb = SFR_IPC else '0';
    end generate generic_tech3;

    xilinx_tech3: if Technology = "XILINX" generate
        signal isipc    : std_logic;
        attribute INIT of ISIPC_INST : label is "0004";
        attribute INIT of RFI_INST   : label is "0800";

    begin
        ISIPC_INST : LUT4
            generic map (INIT => X"0004")
            port map (O  => isipc,
                      I3 => DE_rb(3), I2 => DE_rb(2),
                      I1 => DE_rb(1), I0 => DE_rb(0));
        RFI_INST : LUT4
            generic map (INIT => X"0800")
            port map (O  => rfi,
                      I3 => IAGU_deregbr, I2 => DE_bsel(1),
                      I1 => DE_bsel(0),   I0 => isipc);        
    end generate xilinx_tech3;
end Behavioral;
