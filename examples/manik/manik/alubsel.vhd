-------------------------------------------------------------------------------
-- Title      : Operand B selection code
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : alubsel.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Last update: 2006-01-05
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: decode operand B selection code
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2003/04/03  1.0      sandeep	Created
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

entity alubsel is
    
    port (instr : in  std_logic_vector (INST_WIDTH-1 downto 0);
          bsel  : out std_logic_vector (1 downto 0));

end alubsel;

architecture Behavioral of alubsel is

    constant bselimm : std_logic_vector (1 downto 0) := ALU_BSEL_IMM;
    constant bselsfr : std_logic_vector (1 downto 0) := ALU_BSEL_SFR;
    
    signal bsel1     : std_logic_vector (1 downto 0);
    signal bmuxsfr   : std_logic;
    signal bmuximm   : std_logic;
    signal bimm_cond : std_logic;
--    signal bimm_br   : std_logic;
    signal bimm_shi  : std_logic;
    
begin  -- Behavioral

    bsel_proc : process (instr)
    begin
        case instr (15 downto 11) is
            when "11100" => bsel1 <= ALU_BSEL_IMM;
            when "11101" => bsel1 <= ALU_BSEL_IMM;
            when "11110" => bsel1 <= ALU_BSEL_IMM;
            when "01000" => bsel1 <= ALU_BSEL_IMM;
            when "01001" => bsel1 <= ALU_BSEL_IMM;
            when "01010" => bsel1 <= ALU_BSEL_IMM;
            when "01011" => bsel1 <= ALU_BSEL_IMM;
            when "01100" => bsel1 <= ALU_BSEL_IMM;
            when "01101" => bsel1 <= ALU_BSEL_IMM;
            when "01110" => bsel1 <= ALU_BSEL_IMM;
            when "01111" => bsel1 <= ALU_BSEL_IMM;
            when others  => bsel1 <= ALU_BSEL_RB;
        end case;
    end process bsel_proc;

    bmuxsfr <= '1' when (instr(15 downto 8) = "00011110" or
                         instr(15 downto 8) = "00010010") else '0';
   
    -- immediate for conditionals and compares
    bimm_cond_proc: process (instr)
    begin
        case instr (11 downto 8) is
            when "1110" => bimm_cond <= '1';  -- addit
            when "1010" => bimm_cond <= '1';  -- addif
            when "0000" => bimm_cond <= '1';  -- cmpeqi
            when "0110" => bimm_cond <= '1';  -- cmplti
            when "0111" => bimm_cond <= '1';  -- cmpgti
            when others => bimm_cond <= '0';
        end case;
    end process bimm_cond_proc;

    -- immediate for lsli, lsri & asri
    bimm_shi_proc : process (instr)
    begin
        case instr (11 downto 8) is
            when "1111" => bimm_shi <= '1';  -- asri
            when "1101" => bimm_shi <= '1';  -- lsri
            when "1100" => bimm_shi <= '1';  -- lsli
            when "1110" => bimm_shi <= '1';  -- multi 
            when others => bimm_shi <= '0';
        end case;
    end process bimm_shi_proc;
    
    bmuximm <= '1' when (instr(15 downto 12) = "0011" and bimm_cond = '1') or
                        (instr(15 downto 12) = "0000" and bimm_shi  = '1') else '0';
    
    bsel    <= bselsfr when bmuxsfr = '1'  else
               bselimm when bmuximm = '1'  else
               bsel1;

end Behavioral;
