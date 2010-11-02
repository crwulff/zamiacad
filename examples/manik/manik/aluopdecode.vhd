-------------------------------------------------------------------------------
-- Title      : aluopdecode
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : aluopdecode.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-12-28
-- Last update: 2006-01-05
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Decode the alu operations
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

entity aluopdecode is
    generic (WIDTH      : integer := 32;
             INST_WIDTH : integer := 16);
    port (instr  : in  std_logic_vector (INST_WIDTH-1 downto 0);
          iscond : in  std_logic;
          aluop  : out std_logic_vector (3 downto 0));

end aluopdecode;

architecture Behavioral of aluopdecode is

    signal aluopA   : std_logic_vector (3 downto 0);
    signal aluopB   : std_logic_vector (3 downto 0);
    signal aluopC   : std_logic_vector (3 downto 0);
    signal aluopD   : std_logic_vector (3 downto 0);
    signal aluopD_a : std_logic_vector (3 downto 0);
    signal aluopD_b : std_logic_vector (3 downto 0);
    signal aluopSel : std_logic_vector (2 downto 0);

    attribute BEL : string;
    
begin  -- Behavioral

    -- load / store and branch
    aluopA <= ALUOP_ADD;

    -- special instructions
    generic_tech: if Technology /= "XILINX" generate
        aluopB_proc : process (instr)
        begin
            case instr (15 downto 12) is
                when "0100" => aluopB <= ALUOP_ADD;
                when "0101" => aluopB <= ALUOP_AND;
                when "0110" => aluopB <= ALUOP_ADD;
                when others => aluopB <= ALUOP_MOV;
            end case;
        end process aluopB_proc;              
    end generate generic_tech;

    xilinx_tech: if Technology = "XILINX" generate
        attribute INIT of aluopB_0 : label is "70";
        attribute RLOC of aluopB_0 : label is rloc_string((WIDTH/2)+1, 2, 1, (WIDTH/2), true,0,0);
        attribute BEL  of aluopB_0 : label is bel_string(1);
        
        attribute INIT of aluopB_2 : label is "A0";
        attribute RLOC of aluopB_2 : label is rloc_string((WIDTH/2)+1, 2, 1, (WIDTH/2), true,0,0);
        attribute BEL  of aluopB_2 : label is bel_string(0);
    begin
        aluopB_0 : ROM16X1
            generic map (INIT => X"0070")
            port map (O  => aluopB(0),
                      A3 => instr(15), A2 => instr(14),
                      A1 => instr(13), A0 => instr(12));
        aluopB_2 : ROM16X1
            generic map (INIT => X"00A0")
            port map (O  => aluopB(2),
                      A3 => instr(15), A2 => instr(14),
                      A1 => instr(13), A0 => instr(12));

        aluopB(1) <= '0';
        aluopB(3) <= '0';        
    end generate xilinx_tech;

    -- data processing instructions
    aluopC <= instr (11 downto 8);

    generic_tech1: if Technology /= "XILINX" generate
        -- conditional instructions    
        opD_a_proc: process (instr)
        begin
            case instr (10 downto 8) is
                when "000" => aluopD_a <= ALUOP_MOV;
                when "001" => aluopD_a <= ALUOP_SUB;
                when "010" => aluopD_a <= ALUOP_ADD;
                when "011" => aluopD_a <= ALUOP_ADD;
                when "100" => aluopD_a <= ALUOP_MOV;
                when "101" => aluopD_a <= ALUOP_SUB;
                when "110" => aluopD_a <= ALUOP_ADD;
                when "111" => aluopD_a <= ALUOP_ADD;
                when others => null;
            end case;
        end process opD_a_proc;

        -- br & bri instructions
        aluopD_b <= ALUOP_MOV;
        aluopD   <= aluopD_a when iscond = '1' else aluopD_b;        
    end generate generic_tech1;

    xilinx_tech1: if Technology = "XILINX" generate
        attribute INIT of opD_a_0  : label is "CC";
        attribute INIT of opD_a_2  : label is "11";
    begin
        -- conditional instructions
        opD_a_0 : ROM16X1
            generic map (INIT => X"00CC")
            port map (O  => aluopD_a(0),
                      A3 => '0'     , A2 => instr(10),
                      A1 => instr(9), A0 => instr(8));
        opD_a_2 : ROM16X1
            generic map (INIT => X"0011")
            port map (O  => aluopD_a(2),
                      A3 => '0'     , A2 => instr(10),
                      A1 => instr(9), A0 => instr(8));
        
        aluopD_a(1) <= '0';
        aluopD_a(3) <= '0';

        -- br & bri instructions
        aluopD_b <= ALUOP_MOV;              -- 0100

        aluopD(1) <= '0';
        aluopD(3) <= '0';
        aluopD(0) <= aluopD_a(0) when iscond = '1' else aluopD_b(0);
        aluopD(2) <= aluopD_a(2) when iscond = '1' else aluopD_b(2);        
    end generate xilinx_tech1;
    
    aluopSel_proc : process (instr)
    begin
        case instr (15 downto 11) is
            -- opA
            when "10000" => aluopSel <= "100";
            when "10001" => aluopSel <= "100";
            when "10010" => aluopSel <= "100";
            when "10011" => aluopSel <= "100";
            when "10100" => aluopSel <= "100";
            when "10101" => aluopSel <= "100";
            when "10110" => aluopSel <= "100";
            when "10111" => aluopSel <= "100";
            when "11000" => aluopSel <= "100";
            when "11001" => aluopSel <= "100";
            when "11010" => aluopSel <= "100";
            when "11011" => aluopSel <= "100";
            when "11100" => aluopSel <= "100";
            when "11101" => aluopSel <= "100";
            when "11110" => aluopSel <= "100";
            when "11111" => aluopSel <= "100";  

            -- opB
            when "01000" => aluopSel <= "101";
            when "01001" => aluopSel <= "101";
            when "01010" => aluopSel <= "101";
            when "01011" => aluopSel <= "101";
            when "01100" => aluopSel <= "101";
            when "01101" => aluopSel <= "101";
            when "01110" => aluopSel <= "101";
            when "01111" => aluopSel <= "101";

            -- opC
            when "00100" => aluopSel <= "110";
            when "00101" => aluopSel <= "110";
            when "00001" => aluopSel <= "110";

            -- disable means subtract
            when "00110" => aluopSel <= "000";

            -- opD
            when "00111" => aluopSel <= "111";
            when "00011" => aluopSel <= "111";
            when "00010" => aluopSel <= "111";
            when others  => aluopSel <= "000";
        end case;
    end process aluopSel_proc;

    aluop       <= "0000" when aluopSel(2) = '0'   else
                   aluopA when aluopSel    = "100" else
                   aluopB when aluopSel    = "101" else
                   aluopC when aluopSel    = "110" else
                   aluopD ;
end Behavioral;
