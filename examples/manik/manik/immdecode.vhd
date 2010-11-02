-------------------------------------------------------------------------------
-- Title      : Immediate decode logic
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : immdecode.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Last update: 2006-07-25
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Immediate Decode Logic
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2003/04/03  1.0      sandeep	Created
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

entity immdecode is
    generic (WIDTH : integer := 32);
    port (instr       : in  std_logic_vector (INST_WIDTH-1 downto 0);
          clk         : in  std_logic;
          rst         : in  std_logic;
          stall       : in  std_logic;
          deimm       : out std_logic_vector (DE_IMM_WIDTH-1 downto 0);
          deimmSigned : out std_logic;
          deimmSHL    : out std_logic_vector (1 downto 0);
          deimmAlign4 : out std_logic);

end immdecode;

architecture Behavioral of immdecode is

    signal imm_sel     : std_logic_vector (2 downto 0);
    signal ldst_imm    : std_logic_vector (DE_IMM_WIDTH-1 downto 0);
    signal brch_imm    : std_logic_vector (DE_IMM_WIDTH-1 downto 0);
    signal spec_immS   : std_logic_vector (DE_IMM_WIDTH-1 downto 0);
    signal spec_immU   : std_logic_vector (DE_IMM_WIDTH-1 downto 0);
    signal spec_imm    : std_logic_vector (DE_IMM_WIDTH-1 downto 0);
    signal cond_immS   : std_logic_vector (DE_IMM_WIDTH-1 downto 0);
    signal cond_immU   : std_logic_vector (DE_IMM_WIDTH-1 downto 0);
    signal cond_imm    : std_logic_vector (DE_IMM_WIDTH-1 downto 0);
    signal spec_immsel : std_logic;
    signal cond_immsel : std_logic;
    signal imm         : std_logic_vector (DE_IMM_WIDTH-1 downto 0);
    signal immSigned   : std_logic;
    signal immSHL      : std_logic_vector (1 downto 0);
    signal immAlign4   : std_logic;
    signal Nstall      : std_logic;
    
    signal imm_r       : std_logic_vector (DE_IMM_WIDTH-1 downto 0) := (others => '0');
    signal immSigned_r : std_logic                                  := '0';
    signal immSHL_r    : std_logic_vector (1 downto 0)              := "00";
    signal immAlign4_r : std_logic                                  := '0';

    attribute BEL : string;
begin  -- Behavioral

    Nstall <= not stall;
    ---------------------------------------------------------------------------
    --                  Immedate operand related logic  --
    ---------------------------------------------------------------------------
    -- Four types of immediates
    imm_sel_proc : process (instr)
    begin
        case instr (15 downto 12) is
            -- ldst_imm
            when "1000" => imm_sel <= "100"; 
            when "1001" => imm_sel <= "100";
            when "1010" => imm_sel <= "100";
            when "1011" => imm_sel <= "100";
            when "1100" => imm_sel <= "100";
            when "1101" => imm_sel <= "100";
            -- brch_imm                 
            when "1110" => imm_sel <= "101";  
            when "1111" => imm_sel <= "101";
            -- spec_imm
            when "0100" => imm_sel <= "110";  -- special
            when "0101" => imm_sel <= "110";
            when "0110" => imm_sel <= "110";
            when "0111" => imm_sel <= "110";
            -- cond_imm
            when "0011" => imm_sel <= "111";  -- compare imm
            when "0000" => imm_sel <= "111";  -- shift immediates
            when others => imm_sel <= "000";
        end case;
    end process imm_sel_proc;

    -------------------------------------------------
    -- spec_imm can be signed movi, addi or unsigned
    -- ldrpc, andi spec_immsel = '1' when unsigned 
    -------------------------------------------------
    spec_immsel_proc: process (instr)
    begin
        case instr (15 downto 12) is
            when "0100" => spec_immsel <= '1';
            when "0101" => spec_immsel <= '1';
            when others => spec_immsel <= '0';
        end case;
    end process spec_immsel_proc;
    
    spec_immU <= conv_std_logic_vector(0,DE_IMM_WIDTH-8) &
                 instr(11 downto 8) & instr(3 downto 0);
    
    spec_iSigned: for i in 0 to DE_IMM_WIDTH-1 generate
        spec_sx: if i > SPEC_IMM_WIDTH-1 generate
            spec_immS (i) <= instr(11);
        end generate spec_sx;
        spec_sx_11_0: if i <= SPEC_IMM_WIDTH-1 generate
            spec_immS (i) <= spec_immU(i);
        end generate spec_sx_11_0;
    end generate spec_iSigned;
    
    spec_imm <= spec_immU when spec_immsel = '1' else
                spec_immS;

    ----------------------------------------------------------
    -- cond_imm can be signed cmp<eq,lt,gt>i or unsigned
    -- <lsr,asr,lsl>i . cond_immsel = '1' when unsigned
    -----------------------------------------------------------
    cond_immsel_proc : process (instr)
    begin
        case instr(15 downto 12) is
            when "0000" => cond_immsel <= '1';
            when others => cond_immsel <= '0';
        end case;
    end process cond_immsel_proc;
        
    cond_sximm : for i in 0 to DE_IMM_WIDTH-1 generate
        cond_sx: if i > COND_IMM_WIDTH-1 generate
            cond_immS(i) <= instr(3);
        end generate cond_sx;
        ci_rest: if i <= COND_IMM_WIDTH-1 generate
            cond_immS(i) <= instr(i);
        end generate ci_rest;
    end generate cond_sximm;

    cond_immU <= conv_std_logic_vector(0,DE_IMM_WIDTH-4) & instr(3 downto 0);
    cond_imm  <= cond_immU when cond_immsel = '1' else
                 cond_immS;
    
    ldst_imm  <= conv_std_logic_vector(0,DE_IMM_WIDTH-4) & instr(11 downto 8);
    -- branch immediate : value to add to PC
    -- to get the following instruction 2 for Long 1 for short
    brch_imm  <= conv_std_logic_vector(1,DE_IMM_WIDTH) when instr(10) = '1' else
                 conv_std_logic_vector(2,DE_IMM_WIDTH);

    --------------------------------
    -- immediate selection
    --------------------------------
    imm  <= conv_std_logic_vector(0,DE_IMM_WIDTH) when imm_sel(2) = '0' else
            ldst_imm                              when imm_sel = "100"  else
            brch_imm                              when imm_sel = "101"  else
            spec_imm                              when imm_sel = "110"  else
            cond_imm;

    ---------------------------
    -- immediate is signed
    ---------------------------
    immsigned_proc : process (instr)
    begin
        case instr (15 downto 12) is
            when "1110" => immSigned <= '1';
            when "1111" => immSigned <= '1';
            when "0110" => immSigned <= '1';
            when "0111" => immSigned <= '1';
            when "0011" => immSigned <= '1';
            when others => immSigned <= '0';
        end case;
    end process immsigned_proc;

    --------------------------------------
    -- immediate needs to be shifted left
    --------------------------------------
    immSHL_proc : process (instr)
    begin
        case instr (15 downto 12) is
            -- load/store word shift left 2 bits
            when "1000" => immSHL  <= IMM_SHL_2;
            when "1001" => immSHL  <= IMM_SHL_2;
            -- load/store half word shift left 1 bits
            when "1010" => immSHL  <= IMM_SHL_1;
            when "1011" => immSHL  <= IMM_SHL_1;
            -- load/store byte no shift
            when "1100" => immSHL  <= IMM_SHL_0;
            when "1101" => immSHL  <= IMM_SHL_0;
            -- branches align on half word boundary
            when "1110" => immSHL  <= IMM_SHL_1;
            when "1111" => immSHL  <= IMM_SHL_1;
            -- ldrpc : literal pools aligned on 4
            when "0100" => immSHL  <= IMM_SHL_2;
            -- andi/addi/movi .. etc
            when "0101" => immSHL  <= IMM_SHL_0;
            when "0110" => immSHL  <= IMM_SHL_0;
            when "0111" => immSHL  <= IMM_SHL_0;
            when "0011" => immSHL  <= IMM_SHL_0;                           
            when "0000" => immSHL  <= IMM_SHL_0;
            when others => immSHL  <= IMM_ZERO;
        end case;
    end process immSHL_proc;

    immAlign4 <= '1' when instr(15 downto 12) = "0100" else '0';

    process (clk)
    begin
        if rising_edge(clk) then
            if rst = '1' then
                immSHL_r    <= "00";
            elsif Nstall = '1' then
                immSHL_r    <= immSHL;
            end if;
        end if;
    end process;

    generic_tech: if Technology /= "XILINX" generate
        process (clk)
        begin
            if rising_edge(clk) then
                if rst = '1' then
                    imm_r       <= (others => '0');
                    immAlign4_r <= '0';
                    immSigned_r <= '0';
                elsif Nstall = '1' then
                    imm_r       <= imm;
                    immAlign4_r <= immAlign4; 
                    immSigned_r <= immSigned;
                end if;
            end if;
        end process;
    end generate generic_tech;
    
    xilinx_tech: if Technology = "XILINX" generate
        attribute RLOC of IMMALIGN4_REG : label is rloc_string((WIDTH/2)-1,-1,1,(WIDTH/2)-1,true,-3,-1);
        attribute BEL  of IMMALIGN4_REG : label is bel_string_ff(1);
--        attribute RLOC of IMMSHL_REG    : label is rloc_string(WIDTH/2, -2, true, -((WIDTH/2)+5),-2);
    begin
        
        IMM_INST: for i in 0 to DE_IMM_WIDTH-1 generate
        begin
            LT4: if i < 4 generate
                constant row : integer := (WIDTH/2)-(WIDTH/8)-1-i;
                attribute RLOC of IMM_0 : label is rloc_string(row,0,0,(WIDTH/2)-(WIDTH/8));
                attribute BEL  of IMM_0 : label is bel_string_ff(0);
            begin
                IMM_0: FDRE generic map (INIT => '0')
                    port map (Q => imm_r(i), C => clk, CE => Nstall,
                              D => imm(i),   R => rst);
            end generate LT4;

            GT4: if i >= 4 generate
                constant row : integer := (WIDTH/2)-(WIDTH/8)-3-(i/2);
                attribute RLOC of IMM_0 : label is rloc_string(row,0,0,(WIDTH/2)-(WIDTH/8));
                attribute BEL  of IMM_0 : label is bel_string_ff(i mod 2);
            begin
                IMM_0: FDRE generic map (INIT => '0')
                    port map (Q => imm_r(i), C => clk, CE => Nstall,
                              D => imm(i),   R => rst);
            end generate GT4;
        end generate IMM_INST;
--         IMMSHL_REG : FDRE_VECTOR
--             generic map (WIDTH => 2, SLICE => 1)
--             port map (CLK => clk, CE => Nstall, R => rst,
--                       D   => immSHL, Q => immSHL_r);
        IMMALIGN4_REG : FDRE
            generic map (INIT => '0')
            port map (Q  => immAlign4_r, C  => clk,
                      CE => Nstall, D  => immAlign4, R  => rst);
        IMMSIGNED_REG : FDRE
            generic map (INIT => '0')
            port map (Q  => immSigned_r, C  => clk,
                      CE => Nstall, D  => immSigned, R  => rst);
    end generate xilinx_tech;

    deimm       <= imm_r;
    deimmSHL    <= immSHL_r;
    deimmSigned <= immSigned_r;
    deimmAlign4 <= immAlign4_r;
end Behavioral;
