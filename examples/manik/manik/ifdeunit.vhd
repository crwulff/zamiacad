-------------------------------------------------------------------------------
-- Title      : Decode Unit
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : ifdeunit.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-12-07
-- Last update: 2006-08-08
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Generates decode signals given the instruction
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-12-07  1.0      sandeep	Created
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

entity IFDEunit is
  
    generic (WIDTH      : integer := 32;
             USER_INST  : Boolean := True);

    port (ni_get_done  : in std_logic;
          n_instr      : in std_logic_vector (INST_WIDTH-1 downto 0);
          IAGU_anull   : in std_logic;
          IAGU_deregbr : in std_logic;
          IAGU_rfregbr : in std_logic;
          IAGU_pc      : in std_logic_vector (ADDR_WIDTH-1 downto 0);
          PCT_stall    : in std_logic;
          SFR_ssflag   : in std_logic;

          clk         : in  std_logic;
          IF_RegBr    : out std_logic;
          IF_Br       : out std_logic;
          IF_BrUnc    : out std_logic;
          IF_BrShort  : out std_logic;
          IF_BrOffset : out std_logic_vector (IF_IMM_WIDTH-1 downto 0);

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

end IFDEunit;

architecture Behavioral of IFDEunit is

    component aluopdecode
        generic (WIDTH : integer; INST_WIDTH : integer);
	port(instr  : in  std_logic_vector(INST_WIDTH-1 downto 0);
             iscond : in  std_logic;
             aluop  : out std_logic_vector(3 downto 0));
    end component;

    component immdecode
        generic (WIDTH : integer);
        port (instr       : in  std_logic_vector (INST_WIDTH-1 downto 0);
              clk         : in  std_logic;
              rst         : in  std_logic;
              stall       : in  std_logic;
              deimm       : out std_logic_vector (DE_IMM_WIDTH-1 downto 0);
              deimmSigned : out std_logic;
              deimmSHL    : out std_logic_vector (1 downto 0);
              deimmAlign4 : out std_logic);
    end component;
    
    component alubsel
        port (instr : in  std_logic_vector (INST_WIDTH-1 downto 0);
              bsel  : out std_logic_vector (1 downto 0));
    end component;

    attribute BEL : string;
    
    signal load    : std_logic;
    signal store   : std_logic;
    signal half    : std_logic;
    signal byte    : std_logic;
    signal word    : std_logic;
    signal iscond  : std_logic;
    signal condt   : std_logic;
    signal swi     : std_logic;
    signal Br      : std_logic;
    signal BrUnc   : std_logic;
    signal BrShort : std_logic;
    signal asel    : std_logic_vector (1 downto 0);
    signal bsel    : std_logic_vector (1 downto 0);
    signal rfwe    : std_logic;
    signal rfwe1   : std_logic;
    signal sfrwe   : std_logic;
    signal cywe    : std_logic;
    signal tfwe    : std_logic;
    signal brlink  : std_logic;
    signal aluop   : std_logic_vector (3 downto 0);
    signal alu     : std_logic;
    signal lsu     : std_logic;
    signal uinst   : std_logic;
    signal RegBr   : std_logic;
    signal lreset  : std_logic;
    signal lanull  : std_logic;
    signal lanull1 : std_logic;
    
    signal deimm       : std_logic_vector (DE_IMM_WIDTH-1 downto 0);
    signal deimmSigned : std_logic;
    signal deimmSHL    : std_logic_vector (1 downto 0);
    signal deimmAlign4 : std_logic;

    signal Nstall   : std_logic;
    signal de32biti : std_logic;
    
    -- registers    
    signal deload    : std_logic                                := '0';
    signal destore   : std_logic                                := '0';
    signal dehalf    : std_logic                                := '0';
    signal debyte    : std_logic                                := '0';
    signal deword    : std_logic                                := '0';
    signal deiscond  : std_logic                                := '0';
    signal decondT   : std_logic                                := '0';
    signal debr      : std_logic                                := '0';
    signal debrunc   : std_logic                                := '0';
    signal debrshort : std_logic                                := '0';
    signal debrlong  : std_logic                                := '0';
    signal rd        : std_logic_vector (3 downto 0)            := "0000";
    signal rb        : std_logic_vector (3 downto 0)            := "0000";
    signal deasel    : std_logic_vector (1 downto 0)            := "00";
    signal debsel    : std_logic_vector (1 downto 0)            := "00";
    signal deflagop  : std_logic_vector (2 downto 0)            := "000";
    signal depc      : std_logic_vector (ADDR_WIDTH-1 downto 0) := (others => '0');
    signal derfwe    : std_logic                                := '0';
    signal desfrwe   : std_logic                                := '0';
    signal deswi     : std_logic                                := '0';
    signal decywe    : std_logic                                := '0';
    signal detfwe    : std_logic                                := '0';
    signal debrlink  : std_logic                                := '0';
    signal dealuop   : std_logic_vector (3 downto 0)            := "0000";
    signal dealu     : std_logic                                := '0';
    signal deuinst   : std_logic                                := '0';
    signal deuiop    : std_logic_vector (1 downto 0)            := "00";
    signal rstregs   : std_logic                                := '0';
    signal desstep   : std_logic                                := '0';
    signal deivalid  : std_logic                                := '0';
    
    attribute RLOC of immdecode_inst : label is rloc_string(0, 0, true, +4, -2);
    attribute RLOC of aluop_inst     : label is rloc_string(0, 0);

    constant BROW       : integer := ((WIDTH-ADDR_WIDTH)/2)+(ADDR_WIDTH mod 2);
    
begin  -- Behavioral

    Nstall   <= not PCT_stall after 1 ns;
    de32biti <= '1' when (IAGU_deregbr = '1' or IAGU_rfregbr = '1' or debrlong = '1') else '0';
    
    ivalid_proc: process (clk)
    begin  -- process ivalid_proc
        if rising_edge(clk) then
            if IAGU_anull = '1' then
                deivalid <= '0' after 1 ns;
            elsif Nstall = '1' then
                deivalid <= ni_get_done and not de32biti ;
            end if;
        end if;
    end process ivalid_proc;
    
    -- update registers
    DE_reg_proc : process (clk)
    begin
        if rising_edge(clk) then
            if lanull = '1' then        -- sync reset
                debrlong <= '0' after 1 ns;
            elsif Nstall = '1' then     -- clock enable
                debrlong <= Br and not BrShort after 1 ns;
            end if;

            if IAGU_anull = '1' then
                deload    <= '0'             after 1 ns;
                destore   <= '0'             after 1 ns;
                dehalf    <= '0'             after 1 ns;
                debyte    <= '0'             after 1 ns;
                deword    <= '0'             after 1 ns;
                deiscond  <= '0'             after 1 ns;
                decondT   <= '0'             after 1 ns;
                debr      <= '0'             after 1 ns;
                debrunc   <= '0'             after 1 ns;
                debrshort <= '0'             after 1 ns;
                deswi     <= '0'             after 1 ns;
                detfwe    <= '0'             after 1 ns;
                debrlink  <= '0'             after 1 ns;
                dealu     <= '0'             after 1 ns;
                deuinst   <= '0'             after 1 ns;
                desstep   <= '0'             after 1 ns;
                derfwe    <= '0'             after 1 ns;
                desfrwe   <= '0'             after 1 ns;
                decywe    <= '0'             after 1 ns;
                debsel    <= "00"            after 1 ns;
                deflagop  <= "000"           after 1 ns;
                rd        <= "0000"          after 1 ns;
                rb        <= "0000"          after 1 ns;
                deasel    <= "00"            after 1 ns;
                depc      <= (others => '0') after 1 ns;
                dealuop   <= "0000"          after 1 ns;
                deuiop    <= "00"            after 1 ns;
            elsif Nstall = '1' then
                deload    <= load                       after 1 ns;
                destore   <= store                      after 1 ns;
                dehalf    <= half                       after 1 ns;
                debyte    <= byte                       after 1 ns;
                deword    <= word                       after 1 ns;
                deiscond  <= iscond                     after 1 ns;
                decondT   <= condt                      after 1 ns;
                debr      <= Br                         after 1 ns;
                debrunc   <= BrUnc                      after 1 ns;
                debrshort <= BrShort                    after 1 ns;
                deswi     <= swi                        after 1 ns;
                detfwe    <= tfwe                       after 1 ns;
                debrlink  <= brlink                     after 1 ns;
                dealu     <= (not lsu) and (not uinst)  after 1 ns;
                deuinst   <= uinst                      after 1 ns;
                desstep   <= SFR_ssflag and ni_get_done after 1 ns;
                derfwe    <= rfwe                       after 1 ns;
                desfrwe   <= sfrwe                      after 1 ns;
                decywe    <= cywe                       after 1 ns;
                debsel    <= bsel                       after 1 ns;
                deflagop  <= n_instr (10 downto 8)      after 1 ns;
                rd        <= n_instr (7 downto 4)       after 1 ns;
                rb        <= n_instr (3 downto 0)       after 1 ns;
                deasel    <= asel                       after 1 ns;
                depc      <= IAGU_pc                    after 1 ns;
                dealuop   <= aluop                      after 1 ns;
                deuiop    <= n_instr(9 downto 8)        after 1 ns;
            end if;
        end if;
    end process DE_reg_proc;
    
    -- Reset registers if ...
    --          a) DE or RF stage has register branch
    --          b) If instruction fetch is not complete
    --          c) Or if second half of a long branch
    lanull  <= IAGU_anull or lreset ;    
    lanull1 <= IAGU_anull or lreset ;    
    lreset  <= '1' when (IAGU_deregbr = '1' or IAGU_rfregbr = '1' or ni_get_done = '0' or
                         (ni_get_done = '1' and debrlong = '1')) and Nstall = '1' else '0';
    
    -- IF stage combinatorials
    swi         <= '1' when n_instr (15 downto 8) = "00010000" else '0';
    RegBr       <= '1' when n_instr (15 downto 11) = "00011"   else '0';
    
    IF_Br       <= Br after 1 ns;
    IF_BrUnc    <= BrUnc;
    IF_BrShort  <= BrShort;
    IF_BrOffset <= n_instr (IF_IMM_WIDTH-1 downto 0);
    IF_RegBr    <= RegBr;
    
    -- DE stage combinatorials
    load_proc : process (n_instr)
    begin
        case n_instr (15 downto 12) is
            when "1000" => load <= '1';
            when "1010" => load <= '1';
            when "1100" => load <= '1';
            when "0100" => load <= '1';
            when others => load <= '0';
        end case;
    end process load_proc;

    store_proc : process (n_instr)
    begin
        case n_instr (15 downto 12) is
            when "1001"  => store <= '1';
            when "1011"  => store <= '1';
            when "1101"  => store <= '1';
            when others  => store <= '0';
        end case;
    end process store_proc;

    half_proc: process (n_instr)
    begin
        case n_instr (15 downto 12) is
            when "1010" => half <= '1';
            when "1011" => half <= '1';
            when others => half <= '0';
        end case;
    end process half_proc;

    byte_proc: process (n_instr)
    begin
        case n_instr (15 downto 12) is
            when "1100" => byte <= '1';
            when "1101" => byte <= '1';
            when others => byte <= '0';
        end case;
    end process byte_proc;

    word_proc : process (n_instr)
    begin
        case n_instr(15 downto 12) is
            when "1000" => word <= '1';
            when "1001" => word <= '1';
            when "0100" => word <= '1';
            when "0001" => word <= '1';
            when others => word <= '0';
        end case;
    end process word_proc;
    
    iscond  <= '1'         when n_instr (15 downto 11) = "00111" else '0';
    condt   <= n_instr(11) when n_instr(15 downto 14) = "11"     else n_instr(10);
    Br      <= '1'         when n_instr (15 downto 13) = "111"   else '0';
    BrUnc   <= '1'         when n_instr (15 downto 12) = "1111"  else '0';
    BrShort <= n_instr(10);
    lsu     <= load or store;

    ---------------------------------------------------------------------------
    --                       Immediate operand decode
    ---------------------------------------------------------------------------
    immdecode_inst : immdecode
        generic map (WIDTH => WIDTH)        
        port map (instr       => n_instr,
                  clk         => clk,
                  rst         => IAGU_anull, --lanull,
                  stall       => PCT_stall,
                  deimm       => deimm,
                  deimmSigned => deimmSigned,
                  deimmSHL    => deimmSHL,
                  deimmAlign4 => deimmAlign4);
    
    ---------------------------------------------------------------------------
    --                       ALU Operand selection logic
    ---------------------------------------------------------------------------

    -------------------------------
    -- alu operand a selection code
    -------------------------------
    generic_tech4: if Technology /= "XILINX" generate        
        asel_proc : process (n_instr)
        begin
            case n_instr (15 downto 12) is
                -- imm
                when "1000" => asel <= ALU_ASEL_IMM;
                when "1001" => asel <= ALU_ASEL_IMM;
                when "1010" => asel <= ALU_ASEL_IMM;
                when "1011" => asel <= ALU_ASEL_IMM;
                when "1100" => asel <= ALU_ASEL_IMM;
                when "1101" => asel <= ALU_ASEL_IMM;
                               -- pc
                when "1110" => asel <= ALU_ASEL_PC;
                when "1111" => asel <= ALU_ASEL_PC;
                when "0100" => asel <= ALU_ASEL_PC;
                when "0001" => asel <= ALU_ASEL_PC;
                               -- rd
                when others => asel <= ALU_ASEL_RD;
            end case;
        end process asel_proc;
    end generate generic_tech4;

    xilinx_tech4: if Technology = "XILINX" generate
        attribute INIT of ASEL_0       : label is "C012";
        attribute INIT of ASEL_1       : label is "00ED";
    begin
        ASEL_0 : ROM16X1
            generic map (INIT => X"C012")
            port map (O  => asel(0),
                      A3 => n_instr(15), A2 => n_instr(14),
                      A1 => n_instr(13), A0 => n_instr(12));
        ASEL_1 : ROM16X1
            generic map (INIT => X"00ED")
            port map (O  => asel(1),
                      A3 => n_instr(15), A2 => n_instr(14),
                      A1 => n_instr(13), A0 => n_instr(12));
    end generate xilinx_tech4;
    
    -------------------------------
    -- alu operand b selection code
    -------------------------------
    alubsel_inst: alubsel
        port map (instr => n_instr,
                  bsel  => bsel);
    
    ---------------------------------------------------------------------------
    -- sfrwe - is this an "mtsfr"
    ---------------------------------------------------------------------------
    sfrwe <= '1' when n_instr(15 downto 8) = "00010011" else '0';        
    
    ---------------------------------------------------------------------------
    --          rfwe - if this instruction will update the register file
    ---------------------------------------------------------------------------
    rfwe_proc : process (n_instr)
    begin
        case n_instr (15 downto 11) is
            -- load/store rfwe is done by the write back stage
            when "10000" => rfwe1 <= '0';
            when "10001" => rfwe1 <= '0';
            when "10010" => rfwe1 <= '0';
            when "10011" => rfwe1 <= '0';
            when "10100" => rfwe1 <= '0';
            when "10101" => rfwe1 <= '0';
            when "10110" => rfwe1 <= '0';
            when "10111" => rfwe1 <= '0';                             
            when "11000" => rfwe1 <= '0';
            when "11001" => rfwe1 <= '0';
            when "11010" => rfwe1 <= '0';
            when "11011" => rfwe1 <= '0';
            when "11100" => rfwe1 <= '0';
            when "11101" => rfwe1 <= '0';
            when "11110" => rfwe1 <= '0';
            when "11111" => rfwe1 <= '0';
                            -- ldrpc rfwe is done by write back stage
            when "01000" => rfwe1 <= '0';
            when "01001" => rfwe1 <= '0';
                                -- others
            when "01010" => rfwe1 <= '1';  -- andi
            when "01011" => rfwe1 <= '1';
            when "01100" => rfwe1 <= '1';  -- addi
            when "01101" => rfwe1 <= '1';
            when "01110" => rfwe1 <= '1';  -- movi
            when "01111" => rfwe1 <= '1';
            when "00100" => rfwe1 <= '1';  -- alu ops
            when "00101" => rfwe1 <= '1';
            when "00110" => rfwe1 <= '0';
            when "00111" => rfwe1 <= '1';  -- conditionals
            when "00010" => rfwe1 <= '1';  -- mfsfr
            when "00001" => rfwe1 <= '1';  -- user instructions
            when others  => rfwe1 <= '0';
        end case;
    end process rfwe_proc;

    rfwe  <= rfwe1 and (not sfrwe) and (not swi);
    
    ---------------------------------------------------------------------------
    --       uinst - is this instruction an user defined instruction
    ---------------------------------------------------------------------------
    -- user intruction
    UI_UNIT: if USER_INST = True generate
        uinst <= '1' when n_instr(15 downto 10) = "000010" else '0';        
    end generate UI_UNIT;
    NOUI_UNIT: if USER_INST = False generate
        uinst <= '0';
    end generate NOUI_UNIT;

    ---------------------------------------------------------------------------
    -- cywe - will this instruction update the carry flag
    ---------------------------------------------------------------------------
    generic_tech1: if Technology /= "XILINX" generate
        cywe  <= '1' when  n_instr(15 downto 12) = "0010" and
                 (n_instr(11 downto 8)  = "0000" or  -- sub
                  n_instr(11 downto 8)  = "0001" or  -- add
                  n_instr(11 downto 8)  = "0010" or  -- subc
                  n_instr(11 downto 8)  = "0011") else '0';  -- addc        
    end generate generic_tech1;
    
    xilinx_tech1: if Technology = "XILINX" generate
        signal cywe_01 : std_logic;
        signal cywe_02 : std_logic;
        signal cywe_03 : std_logic;
        
        attribute INIT of CYWE_LUT1 : label is "000F";
        attribute INIT of CYWE_LUT2 : label is "0004";
        attribute RLOC of CYWE_LUT1 : label is rloc_string((WIDTH/2)+1, 0, 0, (WIDTH/2)+1, true, -1, -2);
        attribute RLOC of CYWE_LUT2 : label is rloc_string((WIDTH/2)+1, 0, 0, (WIDTH/2)+1, true, -1, -2);
        attribute BEL  of CYWE_LUT1 : label is bel_string(0);
        attribute BEL  of CYWE_LUT2 : label is bel_string(1);
    begin
        CYWE_LUT1: LUT4
            generic map (INIT => X"000F")
            port map (O  => cywe_01,
                      I0 => n_instr(8), I1 => n_instr(9),
                      I2 => n_instr(10),I3 => n_instr(11));
        CYWE_MUXCY1 : MUXCY_L
            port map (S  => cywe_01,
                      DI => '0', CI => '1',
                      LO => cywe_02);
        CYWE_LUT2: LUT4
            generic map (INIT => X"0004")
            port map (O  => cywe_03,
                      I0 => n_instr(12), I1 => n_instr(13),
                      I2 => n_instr(14), I3 => n_instr(15));        
        CYWE_MUXCY2 : MUXCY
            port map (S  => cywe_03, DI => '0',
                      CI => cywe_02, O  => cywe);        
    end generate xilinx_tech1;
    
    ---------------------------------------------------------------------------
    -- tfwe - will this instruction update the True/False flag
    ---------------------------------------------------------------------------
    tfwe   <= '1' when n_instr(15 downto 11) = "00110" else '0';

    ---------------------------------------------------------------------------
    -- brlink - is this a branch & link instruction
    ---------------------------------------------------------------------------    
    brlink <= '1' when (n_instr(15 downto 11) = "11111" ) or
                       (n_instr(15 downto 11) = "00011" and n_instr(8) = '1')
                  else '0';
    
    ---------------------------------------------------------------------------
    --                                  aluop
    ---------------------------------------------------------------------------
    aluop_inst : aluopdecode
        generic map (WIDTH => WIDTH, INST_WIDTH => INST_WIDTH)
        port    map (instr => n_instr, iscond => iscond, aluop => aluop);

    DE_load      <= deload and deivalid;
    DE_store     <= destore and deivalid;
    DE_half      <= dehalf and deivalid;
    DE_byte      <= debyte and deivalid;
    DE_word      <= deword and deivalid;
    DE_iscond    <= deiscond and deivalid;
    DE_condT     <= decondT and deivalid;
    DE_Br        <= debr and deivalid;
    DE_BrUnc     <= debrunc and deivalid;
    DE_BrShort   <= debrshort and deivalid;
    DE_imm       <= and_vect(deimm,deivalid);
    DE_immSigned <= deimmsigned and deivalid;
    DE_immSHL    <= and_vect(deimmshl,deivalid);
    DE_immAlign4 <= deimmalign4 and deivalid;
    DE_rd        <= and_vect(rd,deivalid);
    DE_rb        <= and_vect(rb,deivalid);
    DE_asel      <= and_vect(deasel,deivalid);
    DE_pc        <= depc;

    DE_swi       <= deswi and deivalid;
    DE_tfwe      <= detfwe and deivalid;
    DE_BrLink    <= debrlink and deivalid;
    DE_alu       <= dealu and deivalid;
    DE_uinst     <= deuinst and deivalid;
    DE_sstep     <= desstep and deivalid;
    DE_rfwe      <= derfwe and deivalid;
    DE_sfrwe     <= desfrwe and deivalid;
    DE_cywe      <= decywe and deivalid;
    DE_bsel      <= and_vect(debsel,deivalid);
    DE_flagop    <= and_vect(deflagop,deivalid);

    DE_aluop     <= dealuop;
    DE_rstregs   <= lanull1;
    DE_uiop      <= deuiop;
    DE_ivalid    <= deivalid;
end Behavioral;
