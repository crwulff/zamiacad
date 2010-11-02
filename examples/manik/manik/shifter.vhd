-------------------------------------------------------------------------------
-- Title      : shifter
-- Project    : MANIK-2
-------------------------------------------------------------------------------
-- File       : shifter.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-09-14
-- Last update: 2006-01-05
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: performs left/right/arithmetic right shift
--                      right_shift if '1' else left shift
--                      signed right shift if '1' (valid)
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-09-14  1.0      sandeep	Created
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library UNISIM;
use UNISIM.vcomponents.all;

use work.manikconfig.all;
use work.manikpackage.all;

entity funnel is
    
    generic (WIDTH    : integer := 32;
             SELWIDTH : integer := 3);

    port (b           : in  std_logic_vector (WIDTH-1 downto 0);  -- input vector
          right_shift : in  std_logic;
          right_sign  : in  std_logic;
          sel         : in  std_logic_vector (SELWIDTH-1 downto 0);
          c           : out std_logic_vector (WIDTH-1    downto 0));
end funnel;

architecture Behavioral of funnel is
    constant swidth : integer := 2**SELWIDTH;
    constant zsw : std_logic_vector (swidth-2 downto 0) := (others => '0');
    signal sign  : std_logic_vector (swidth-1 downto 0) := (others => '0');
    signal isel  : std_logic_vector ((swidth+WIDTH)-1 downto 0) := (others => '0');
    
begin  -- Behavioral

    signext: for i in 0 to swidth-1 generate
        sign(i) <= b(WIDTH-1);
    end generate signext;
    
    isel <= '0'  & b(WIDTH-1 downto 0) & zsw when right_shift = '0' else
            sign & b       	    	     when right_sign  = '1' else
            '0'  & zsw  & b ;

    sw_1: if SELWIDTH = 1 generate
        resgen: for i in 0 to WIDTH-1 generate
            c(i) <= isel(i) when sel = "0" else isel(i+1);
        end generate resgen;
    end generate sw_1;
    
    sw_2: if SELWIDTH = 2 generate
        resgen: for i in 0 to WIDTH-1 generate
            c(i) <= isel(i+0) when sel = "00" else 
                    isel(i+1) when sel = "01" else
                    isel(i+2) when sel = "10" else
                    isel(i+3) when sel = "11";
        end generate resgen;
    end generate sw_2;
    
    sw_3: if SELWIDTH = 3 generate
        resgen: for i in 0 to WIDTH-1 generate
        begin
            c(i) <= isel(i+0) when sel = "000" else
                    isel(i+1) when sel = "001" else
                    isel(i+2) when sel = "010" else
                    isel(i+3) when sel = "011" else
                    isel(i+4) when sel = "100" else
                    isel(i+5) when sel = "101" else
                    isel(i+6) when sel = "110" else
                    isel(i+7);
        end generate resgen;        
    end generate sw_3;

    sw_4: if SELWIDTH = 4 generate
        resgen: for i in 0 to WIDTH-1 generate
        begin
            c(i) <= isel(i+0)  when sel = "0000" else
                    isel(i+1)  when sel = "0001" else
                    isel(i+2)  when sel = "0010" else
                    isel(i+3)  when sel = "0011" else
                    isel(i+4)  when sel = "0100" else
                    isel(i+5)  when sel = "0101" else
                    isel(i+6)  when sel = "0110" else
                    isel(i+7)  when sel = "0111" else
                    isel(i+8)  when sel = "1000" else
                    isel(i+9)  when sel = "1001" else
                    isel(i+10) when sel = "1010" else
                    isel(i+11) when sel = "1011" else
                    isel(i+12) when sel = "1100" else
                    isel(i+13) when sel = "1101" else
                    isel(i+14) when sel = "1110" else
                    isel(i+15);
        end generate resgen;
    end generate sw_4;
end Behavioral;

