-------------------------------------------------------------------------------
-- Title      : IAGUnit.vhd (Instruction Address Generation Unit)
-- Project    : MANIK-2
-------------------------------------------------------------------------------
-- File       : iagunit.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-10-27
-- Last update: 2006-10-11
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Generates next instruction address
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-10-27  1.0      sandeep Created
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

entity iagunit is
    
    generic (WIDTH      : integer := 32);

    port (IF_RegBr      : in std_logic;
          IF_Br         : in std_logic;
          IF_BrUnc      : in std_logic;
          IF_BrShort    : in std_logic;
          IF_BrOffset   : in std_logic_vector (IF_IMM_WIDTH-1 downto 0);
          IF_BrOffset_n : in std_logic_vector (INST_WIDTH-1 downto 0);
          EX_addr       : in std_logic_vector (ADDR_WIDTH-1 downto 0);
          EX_opb        : in std_logic_vector (ADDR_WIDTH-1 downto 0);
          EX_mispred    : in std_logic;
          EX_mip        : in std_logic;
          EX_uip        : in std_logic;
          EX_dbghit     : in std_logic;
          DE_pc         : in std_logic_vector (ADDR_WIDTH-1 downto 0);
          DE_rstregs    : in std_logic;
          DE_swi        : in std_logic;
          DE_sstep      : in std_logic;
          DE_ivalid	: in std_logic;
          RF_swi        : in std_logic;
          RF_sstep      : in std_logic;
          INTR_pend     : in std_logic;
          INTR_reset    : in std_logic;
          INTR_vector   : in std_logic_vector (ADDR_WIDTH-1 downto 0);
          PCT_sdly      : in std_logic;
          MEM_ifetch_ip : in std_logic;
          stall         : in std_logic;
          clk           : in std_logic;

          IAGU_debug : out std_logic_vector(3 downto 0);

          IAGU_pc      : out std_logic_vector (ADDR_WIDTH-1 downto 0);
          IAGU_ni_addr : out std_logic_vector (ADDR_WIDTH-1 downto 0);
          IAGU_anull   : out std_logic;
          IAGU_ifclr   : out std_logic;
          IAGU_Nxif    : out std_logic;
          IAGU_intr    : out std_logic;
          IAGU_deregbr : out std_logic;
          IAGU_rfregbr : out std_logic;
          IAGU_exbr    : out std_logic;
          IAGU_exbrlong: out std_logic;
          IAGU_exregbr : out std_logic;
          IAGU_syncif  : out std_logic);

end iagunit;


architecture Behavioral of iagunit is
    constant LSLICE     : integer := 1;
    constant AMPC_COL   : integer := 0;
    constant AMPC_SLICE : integer := 0;
    constant BROW       : integer := ((WIDTH-ADDR_WIDTH)/2)+(ADDR_WIDTH mod 2);
    
    signal PC          : std_logic_vector (ADDR_WIDTH-1 downto 0) := (others => '0');
    signal tOffset     : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal Offset      : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal sextOffset  : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal lsextOffset : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal ssextOffset : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal add_zero    : std_logic                                := '0';
    signal add_sext    : std_logic                                := '0';
    signal TPC_sel     : std_logic_vector (1 downto 0);
    signal FPC         : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal TPC         : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal anull       : std_logic;
    signal ifclr       : std_logic;
    signal Nstall      : std_logic;
    signal PC_ce       : std_logic;
    signal nxif        : std_logic;
    
    signal case_I    : std_logic;
    signal case_II   : std_logic;
    signal case_III  : std_logic;
    signal case_IV   : std_logic;
    signal case_V    : std_logic;
    

    signal deregbr   : std_logic := '0';
    signal debr      : std_logic := '0';
    signal debrcond  : std_logic := '0';
    signal debrshort : std_logic := '0';
    signal debrlong  : std_logic := '0';
    signal rfregbr   : std_logic := '0';
    signal rfbr      : std_logic := '0';
    signal rfbrcond  : std_logic := '0';
    signal rfbrshort : std_logic := '0';
    signal exregbr   : std_logic := '0';
    signal exbr      : std_logic := '0';
    signal exbrcond  : std_logic := '0';
    signal exbrshort : std_logic := '0';
    signal exbrlong  : std_logic := '0';

    signal intr       : std_logic                                  := '0';
    signal debroffset : std_logic_vector (IF_IMM_WIDTH-1 downto 0) := (others => '0');

    attribute BEL : string;
begin  -- Behavioral

    Nstall <= not stall;
    
    prop_IF_to_DE : process (clk)
    begin
        if rising_edge(clk) then        -- rising clock edge
            if anull = '1' then
                deregbr   <= '0';
                debr      <= '0';
                debrcond  <= '0';
                debrshort <= '0';
                debrlong  <= '0';
            elsif stall = '0' then                
                deregbr   <= IF_RegBr;
                debr      <= IF_Br;
                debrcond  <= IF_Br and not IF_BrUnc;
                debrshort <= IF_Br and IF_BrShort;
                debrlong  <= IF_Br and not IF_BrShort;
            end if;
        end if;
    end process prop_IF_to_DE;

    -- offsets do not need reset
    prop_IF_to_DE_nrst : process (clk)
    begin
        if rising_edge(clK) then
            if stall = '0' then         -- clock enable
                debroffset <= IF_BrOffset;
            end if;
        end if;
    end process prop_IF_to_DE_nrst;
    
    -- propagate DE_Reg_Br and debr to RF stage registers
    prop_DE_to_RF : process (clk)
    begin
        if rising_edge(clk) then        -- rising clock edge
            if anull = '1' then         -- sync reset
                rfregbr   <= '0';
                rfbr      <= '0';
                rfbrcond  <= '0';
                rfbrshort <= '0';
            elsif stall = '0' then      -- clock enable
                rfregbr   <= deregbr and DE_ivalid;
                rfbr      <= debr and DE_ivalid;
                rfbrcond  <= debrcond and DE_ivalid;
                rfbrshort <= debrshort and DE_ivalid;
            end if;
        end if;
    end process prop_DE_to_RF;

    -- propagate RF_Reg_Br and rfbr to EX stage registers
    prop_RF_to_EX : process (clk)
    begin
        if rising_edge(clk) then        -- rising clock edge
            if anull = '1' then         -- sync reset
                exregbr   <= '0';
                exbr      <= '0';
                exbrcond  <= '0';
                exbrshort <= '0';
                exbrlong  <= '0';
            elsif stall = '0' then      -- clock enable
                exregbr   <= rfregbr;
                exbr      <= rfbr;
                exbrcond  <= rfbrcond;
                exbrshort <= rfbrshort;
                exbrlong  <= rfbr and not rfbrshort;
            end if;
        end if;
    end process prop_RF_to_EX;

    -- interrupt related code
    IAGU_intr <= intr;

    -- program counter related code

    -- offset selection . Offset can have one of Three values 0, 2 or sext(BrOffset).
    tOffset_set : for i in 0 to ADDR_WIDTH-1 generate
        i_1 : if i = 1 generate
            tOffset(i) <= (not add_zero) and PCT_sdly;
        end generate i_1;
        i_o : if i /= 1 generate
            tOffset(i) <= '0';
        end generate i_o;
    end generate tOffset_set;

    -- sign extend long branch offset
    lsextOffset_set : for i in 0 to ADDR_WIDTH-1 generate
        i_0 : if i = 0 generate
            lsextOffset(i) <= '0';
        end generate i_0;
        i_1_16 : if i <= INST_WIDTH and i /= 0 generate
            lsextOffset(i) <= IF_BrOffset_n(i-1);
        end generate i_1_16;
        i_gt16 : if i > INST_WIDTH and i <= BR_OFFSET_WIDTH generate
            lsextOffset(i) <= debroffset(i-INST_WIDTH-1);
        end generate i_gt16;
        i_rest : if i > BR_OFFSET_WIDTH generate
            lsextOffset(i) <= debroffset(IF_IMM_WIDTH-1);
        end generate i_rest;
    end generate lsextOffset_set;

    -- sign extend short branch offset
    ssextOffset_set: for i in 0 to ADDR_WIDTH-1 generate
        i_0 : if i = 0 generate
            ssextOffset(i) <= '0';
        end generate i_0;
        i_1_11 : if i > 0 and i <= IF_IMM_WIDTH generate
            ssextOffset(i) <= IF_BrOffset(i-1);
        end generate i_1_11;
        i_rest: if i > IF_IMM_WIDTH generate
            ssextOffset(i) <= IF_BrOffset(IF_IMM_WIDTH-1);
        end generate i_rest;
    end generate ssextOffset_set;

    sextOffset <= lsextOffset when (debrlong = '1' and DE_ivalid = '1') else ssextOffset;

    
    -- select TPC depending on TPC_sel signal
    generic_tech2: if Technology /= "XILINX" and Technology /= "ALTERA" and Technology /= "ACTEL" generate        
        TPC <= PC            when TPC_sel = "00" else
               INTR_vector   when TPC_sel = "01" else
               EX_addr       when TPC_sel = "10" else
               EX_opb	     when TPC_sel = "11" else
               conv_std_logic_vector(0,ADDR_WIDTH);
    end generate generic_tech2;

    xilinx_tech2: if Technology = "XILINX" generate
        attribute RLOC of TPC_mux_inst     : label is rloc_string(BROW,AMPC_COL+1,true,0,1);
    begin        
        TPC_mux_inst : MUX4_1E_VECTOR
            generic map (WIDTH => ADDR_WIDTH, ROWDIV => 2, SLICE => 1)
            port map (S0 => TPC_sel(0), S1 => TPC_sel(1),
                      V0 => PC,         V1 => INTR_vector,
                      V2 => EX_addr,    V3 => EX_opb,
                      O  => TPC, 	EN => '1');
    end generate xilinx_tech2;

    altera_tech2: if Technology = "ALTERA" generate
    begin
        TPC_mux : mux41
            port map (data3x => EX_opb,
                      data2x => EX_addr,
                      data1x => INTR_vector,
                      data0x => PC,
                      sel    => TPC_sel,
                      result => TPC);
    end generate altera_tech2;

    actel_tech2: if Technology = "ACTEL" generate
        APA_Family: if Actel_Family = "APA" generate
            TPC_mux : mux41_actel
                port map (Data0_port => PC,
                          Data1_port => INTR_vector,
                          Data2_port => EX_addr,
                          Data3_port => EX_opb,
                          Sel0       => TPC_sel(0),
                          Sel1       => TPC_sel(1),
                          Result     => TPC);            
        end generate APA_Family;
        APA3_Family: if Actel_Family = "APA3" generate
            TPC_mux : mux41_actel_apa3
                port map (Data0_port => PC,
                          Data1_port => INTR_vector,
                          Data2_port => EX_addr,
                          Data3_port => EX_opb,
                          Sel0       => TPC_sel(0),
                          Sel1       => TPC_sel(1),
                          Result     => TPC);            
        end generate APA3_Family;
    end generate actel_tech2;
    
    generic_tech1: if Technology /= "XILINX" generate        
        Offset <= sextOffset when add_sext = '1' else tOffset;
        FPC    <= TPC + Offset;
    end generate generic_tech1;

    xilinx_tech1: if Technology = "XILINX" generate
        attribute RLOC of TPC_add_mux_inst : label is rloc_string(BROW,AMPC_COL);
    begin
        TPC_add_mux_inst : ADD_MUX_VECTOR
            generic map (WIDTH => ADDR_WIDTH, SLICE => AMPC_SLICE)
            port map (a   => TPC,
                      b1  => tOffset,
                      b2  => sextOffset,
                      sel => add_sext,
                      c   => FPC);        
    end generate xilinx_tech1;
    
    IAGU_ni_addr  <= FPC;
    IAGU_anull    <= anull;
    IAGU_pc       <= PC;
    IAGU_ifclr    <= ifclr;
    IAGU_deregbr  <= deregbr and DE_ivalid;
    IAGU_rfregbr  <= rfregbr;
    IAGU_exbr     <= exbr;
    IAGU_exbrlong <= exbrlong;
    IAGU_exregbr  <= exregbr;
    IAGU_Nxif 	  <= nxif;
    
    -- don't issue a instruction fetch from external memory (icache miss)
    -- in the following situation
    nxif      <= ((ifclr or rfregbr) and not (DE_sstep or RF_sstep)) or EX_mispred;

    -- if we are not going to be fetching then no need to stall
    -- the pipeline
    IAGU_syncif <= not nxif; 
    
    
    -- update PC (Program Counter) ?
    PC_ce  <= not PCT_sdly when stall = '1' or
                      (MEM_ifetch_ip = '1' and INTR_pend = '0' and EX_mispred = '0' and nxif = '0')
                else '1';
    
    case_I   <= EX_mispred and not EX_dbghit;
    case_II  <= INTR_pend or not PCT_sdly;
    case_III <= (deregbr and DE_ivalid) and not rfbrshort;
    case_IV  <= rfregbr;
    case_V   <= (debrlong and DE_ivalid) or (IF_Br and IF_BrShort);

    generic_tech: if Technology /= "XILINX-" generate
        signal case_vals : std_logic_vector(4 downto 0);
    begin
        update_pc: process (clk)
        begin  -- process update_pc
            if rising_edge(clk) then  -- rising clock edge
                if PC_ce = '1' then  -- clock enable
                    PC <= FPC;
                end if;
            end if;
        end process update_pc;
        
        case_vals <= case_I & case_II & case_III & case_IV & case_V;

        TPC_sel_proc: process (case_vals)
        begin
            case case_vals is
                                -- case I
                when "10000" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "10001" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "10010" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "10011" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "10100" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "10101" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "10110" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "10111" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "11000" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "11001" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "11010" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "11011" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "11100" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "11101" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "11110" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                when "11111" => TPC_sel <= "10"; add_sext <= '0'; anull <= '1';
                                -- case II
                when "01000" => TPC_sel <= "01"; add_sext <= '0'; anull <= '1';
                when "01001" => TPC_sel <= "01"; add_sext <= '0'; anull <= '1';
                when "01010" => TPC_sel <= "01"; add_sext <= '0'; anull <= '1';
                when "01011" => TPC_sel <= "01"; add_sext <= '0'; anull <= '1';
                when "01100" => TPC_sel <= "01"; add_sext <= '0'; anull <= '1';
                when "01101" => TPC_sel <= "01"; add_sext <= '0'; anull <= '1';
                when "01110" => TPC_sel <= "01"; add_sext <= '0'; anull <= '1';
                when "01111" => TPC_sel <= "01"; add_sext <= '0'; anull <= '1';
                                -- case III
                when "00100" => TPC_sel <= "00"; add_sext <= '0'; anull <= '0';
                when "00101" => TPC_sel <= "00"; add_sext <= '0'; anull <= '0';
                when "00110" => TPC_sel <= "00"; add_sext <= '0'; anull <= '0';
                when "00111" => TPC_sel <= "00"; add_sext <= '0'; anull <= '0';
                                -- case IV
                when "00010" => TPC_sel <= "11"; add_sext <= '0'; anull <= '0';
                when "00011" => TPC_sel <= "11"; add_sext <= '0'; anull <= '0';
                                -- case V
                when "00001" => TPC_sel <= "00"; add_sext <= '1'; anull <= '0';
                when others  => TPC_sel <= "00"; add_sext <= '0'; anull <= '0';
            end case;
        end process TPC_sel_proc;
    end generate generic_tech;

    xilinx_tech: if Technology = "XILINX-" generate
        signal add_sextA : std_logic;
        attribute INIT of TPC_Sel0_ROM : label is "00F2";
        attribute INIT of TPC_Sel1_ROM : label is "FF02";
        attribute RLOC of TPC_Sel0_ROM : label is rloc_string(WIDTH/2,AMPC_COL,0, WIDTH/2, true, 0,0);    
        attribute RLOC of TPC_Sel1_ROM : label is rloc_string(WIDTH/2,AMPC_COL+1,0, WIDTH/2,true,0,-1);
        attribute INIT of SEXT_LUT     : label is "4";
        attribute INIT of SEXTA_LUT    : label is "FFFE";
        
        attribute RLOC of SEXTA_LUT : label is rloc_string((WIDTH/2)-3,AMPC_COL+2,1,(WIDTH/2)-3,true,3,2);
        attribute BEL  of SEXTA_LUT : label is bel_string(0);
        attribute RLOC of SEXT_LUT  : label is rloc_string((WIDTH/2)-3,AMPC_COL+2,1,(WIDTH/2)-3,true,3,2);
        attribute BEL  of SEXT_LUT  : label is bel_string(1);

        attribute INIT of ANULL_LUT : label is "E";
        attribute RLOC of ANULL_LUT : label is rloc_string(WIDTH/2, AMPC_COL+2, 1, WIDTH/2, true,0,2);
        attribute BEL  of ANULL_LUT : label is bel_string(0);

    begin    
        TPC_Sel0_ROM : ROM16X1
            generic map (INIT => X"00F2")
            port map (O  => TPC_Sel(0),
                      A0 => case_IV, A1 => case_III,
                      A2 => case_II, A3 => case_I);
        TPC_Sel1_ROM : ROM16X1
            generic map (INIT => X"FF02")
            port map (O  => TPC_Sel(1),
                      A0 => case_IV, A1 => case_III,
                      A2 => case_II, A3 => case_I);
        SEXTA_LUT : LUT4
            generic map (INIT => X"FFFE")
            port map (O => add_sextA,
                      I0 => case_I,  I1 => case_II,
                      I2 => case_III,I3 => case_IV);        
        SEXT_LUT: LUT2
            generic map (INIT => X"4")
            port map (O  => add_sext, I0 => add_sextA, I1 => case_V);
        ANULL_LUT: LUT2
            generic map (INIT => X"E")
            port map (O  => anull,
                      I0 => case_I, I1 => case_II);
        
        update_pc: for i  in 0 to ADDR_WIDTH-1 generate
            constant row : integer := BROW+((ADDR_WIDTH/2) - i/2 - 1);
            -- should have the col & slice as add_mux4_vector
            attribute RLOC of pc_reg : label is rloc_string(row,AMPC_COL,AMPC_SLICE,ADDR_WIDTH/2,true,0,0);
            attribute INIT of pc_reg : label is "0";
        begin
            pc_reg : FDCE
                generic map (INIT => '0')
                port map (Q => PC(i), C => clk, CE => PC_ce, CLR => '0', D => FPC(i));
        end generate update_pc;    
    end generate xilinx_tech;

    add_zero   <= anull  or case_III or case_IV;
    IAGU_debug <= TPC_sel & case_I & case_II;
    
    -- generate next instruction address
    gen_pc : process (case_I, case_II, case_III, case_IV, case_V)
    begin  -- process gen_pc
--        TPC_sel         <= "00";
--        anull           <= '0' after 1 ns;
        intr            <= '0';
--        add_zero        <= '0';
--        add_sext        <= '0';
        ifclr           <= '0';
        -- I) check if we have mis predicted anything.
        if case_I = '1' then
--            anull           <= '1' after 1 ns;
--            TPC_sel         <= "10";
--            add_zero        <= '1';
        -- II) if interrupt pending then anull and start with ISR.
        elsif case_II = '1' then
--            anull           <= '1' after 1 ns;
--            TPC_sel         <= "01";
            intr            <= PCT_sdly ;
--            add_zero        <= '1' ;
        -- III) check if DE stage has RegBr then don't fetch
        elsif case_III = '1' then
            ifclr           <= '1';
--            add_zero        <= '1';
        -- IV) When it reach the RF stage then pick up the PC 
        elsif case_IV = '1' then
--            TPC_sel         <= "11";
--            add_zero        <= '1';
        -- V) Predict branch taken
        elsif case_V = '1' then
--            add_sext        <= '1';
        end if;
    end process gen_pc;
end Behavioral;
