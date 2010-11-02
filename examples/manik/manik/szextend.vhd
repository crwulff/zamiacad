-------------------------------------------------------------------------------
-- Title      : szextend
-- Project    : Manik-2
-------------------------------------------------------------------------------
-- File       : szextend.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-09-07
-- Last update: 2006-03-21
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: does sign on zero extend byte or half word on a given vector
--                      op = "00" sign extend byte
--                      op = "01" exchange lower & upper half words
--                      op = "10" sign extend half
--                      op = "11" zero extend half
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-09-07  1.0      sandeep	Created
-- 2002-09-14  1.1      sandeep added rloc_string    
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

entity szextend is
    generic (WIDTH      : integer := 32;
             BYTE_WIDTH : integer := 8;
             HALF_WIDTH : integer := 16;
             SLICE      : integer := 1);
    Port ( b    : in  std_logic_vector  (WIDTH-1 downto 0);  -- input
           op   : in  std_logic_vector  (1       downto 0);  -- op 2b performed
           c    : out std_logic_vector  (WIDTH-1 downto 0));  -- output
end szextend;

architecture Behavioral of szextend is
    attribute BEL : string;
begin    

    generic_tech: if Technology /= "XILINX" generate
        szext: process (b,op)
        begin  -- process szext
            if op = "00" then               -- sextb
                for i  in 0 to WIDTH-1 loop
                    if i < (BYTE_WIDTH-1) then
                        c(i) <= b(i) ;
                    else
                        c(i) <= b(BYTE_WIDTH-1);
                    end if;
                end loop;  -- i
            elsif op =  "01" then            -- xhw
                for i  in 0 to WIDTH-1 loop
                    if i < HALF_WIDTH then
                        c(HALF_WIDTH+i) <= b(i);
                    else
                        c(i-HALF_WIDTH) <= b(i);
                    end if;
                end loop;  -- i
            elsif op =  "10" then            -- sexth
                for i  in 0 to WIDTH-1 loop
                    if i < (HALF_WIDTH-1) then
                        c(i) <= b(i);
                    else
                        c(i) <= b(HALF_WIDTH-1);
                    end if;
                end loop;  -- i
            elsif  op = "11" then            -- zexth
                for i in 0 to WIDTH-1 loop
                    if i <= (HALF_WIDTH-1) then
                        c(i) <= b(i);
                    else
                        c(i) <= '0';
                    end if;
                end loop;  -- i
            else
                c <= ALLZEROS;
            end if;
        end process szext;        
    end generate generic_tech;

    xilinx_tech: if Technology = "XILINX" generate
    -- bits 0-(BYTE_WIDTH-1) --
-------------------------------------------------------------------------------
--                            Input mapping for LUTs
-------------------------------------------------------------------------------
--              I0      -> b(HALF_WIDTH+i)
--              I1      -> b(i)
--              I2      -> op(0)
--              I3      -> op(1)
--              O       -> c(i)
-------------------------------------------------------------------------------
--                              Truth table for LUT
-------------------------------------------------------------------------------
--      I3      I2      I1      I0                      Output -> O
-------------------------------------------------------------------------------
--      0       1       X       0                       INIT[4,6] => 0
--      0       1       X       1                       INIT[5,7] => 1
--      0       0       0       X                       INIT[0,1] => 0
--      0       0       1       X                       INIT[2,3] => 1
--      1       0       0       X                       INIT[8,9] => 0
--      1       0       1       X                       INIT[A,B] => 1
--      1       1       0       X                       INIT[C,D] => 0
--      1       1       1       X                       INIT[E,F] => 1
-------------------------------------------------------------------------------
--      INIT            F E D C - B A 9 8 - 7 6 5 4 - 3 2 1 0
--                      1 1 0 0   1 1 0 0   1 0 1 0   1 1 0 0 -> X'CCAC'
-------------------------------------------------------------------------------
        signal btmp0 : std_logic_vector (BYTE_WIDTH-1 downto 0);
        signal btmp1 : std_logic_vector (BYTE_WIDTH-1 downto 0);
        signal btmp2 : std_logic_vector (HALF_WIDTH-1 downto 0);
        signal btmp3 : std_logic_vector (HALF_WIDTH-1 downto 0);
        
        attribute RLOC of szext_16_31_mux4 : label is rloc_string(0,0, true, 8, 0);
        attribute RLOC of szext_8_15_mux4  : label is rloc_string(8,0, true,-4, 0);

    begin  -- XILINX_virtex

        szext_8_15: for i in 0 to BYTE_WIDTH-1 generate
            btmp0(i) <= b(BYTE_WIDTH-1);
            btmp1(i) <= b(HALF_WIDTH+BYTE_WIDTH+i);
        end generate szext_8_15;
        
        sxbh_16_32: for i in 0 to HALF_WIDTH-1 generate
            btmp2(i) <= b(HALF_WIDTH-1);
            btmp3(i) <= b(BYTE_WIDTH-1);
        end generate sxbh_16_32;

        szext_0_7: for i in 0 to BYTE_WIDTH-1 generate
            constant row    : integer := (BYTE_WIDTH/2)-(i/4)-1+10;
            constant slice  : natural := i mod 2;        
--            attribute RLOC of szextlut_0_7 : label is
--                rloc_string(row,0,slice,(BYTE_WIDTH/2),true,12,0);
            attribute INIT of szextlut_0_7 : label is "CCAC";
            attribute BEL  of szextlut_0_7 : label is bel_string(i/2);
        begin
            szextlut_0_7 : LUT4 generic map (INIT => X"CCAC")
                port map (I0 => b(HALF_WIDTH+i),    I1 => b(i),
                          I2 => op(0), I3 => op(1), O  => c(i));
        end generate szext_0_7;
        
        szext_8_15_mux4 : MUX4_1E_VECTOR
            generic map (WIDTH => 8, ROWDIV => 2, SLICE => 1)
            port map (S0 => op(0), S1 => op(1), EN => '1',
                      V0 => btmp0, V1 => btmp1,
                      V2 => b(HALF_WIDTH-1 downto BYTE_WIDTH),
                      V3 => b(HALF_WIDTH-1 downto BYTE_WIDTH),
                      O  => c(HALF_WIDTH-1 downto BYTE_WIDTH));
        
        szext_16_31_MUX4: MUX4_1E_VECTOR
            generic map (WIDTH => 16, ROWDIV => 2, SLICE => 1)
            port map (S0 => op(0), S1 => op(1), EN => '1',
                      V0 => btmp3, V1 => b(HALF_WIDTH-1 downto 0),
                      V2 => btmp2, V3 => open, O  => c(WIDTH-1 downto HALF_WIDTH));        
        
    end generate xilinx_tech;

end Behavioral;
