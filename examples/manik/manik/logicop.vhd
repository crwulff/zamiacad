-------------------------------------------------------------------------------
-- Title      : logicop
-- Project    : Manik-2
-------------------------------------------------------------------------------
-- File       : logicop.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-09-07
-- Last update: 2006-01-05
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Performs bitwise logic operations on two input vectors of eq
--              length.
--              op -> "00"  move
--              op -> "01"  a & b
--              op -> "10"  a | b
--              op -> "11"  a ^ b
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

entity logicop is
    generic (WIDTH   : integer := 32 ;
             SLICE   : integer := 1);
    Port ( op   : in std_logic_vector (1 downto 0);
           a    : in std_logic_vector (WIDTH-1 downto 0);
           b    : in std_logic_vector (WIDTH-1 downto 0);
           c    : out std_logic_vector(WIDTH-1 downto 0));
end logicop;

architecture Behavioral of logicop is
begin

    generic_tech: if Technology /= "XILINX" generate
        -- purpose: perform bitwise logic operations
        -- type   : combinational
        -- inputs : a,b,op
        -- outputs: c
        logic_ops: process (a,b,op)
        begin  -- process logic_ops
            case op is
                when "00"  =>
                    c <= b;
                when "01"  =>
                    c <= a and b;
                when "10"  =>
                    c <= a or  b;
                when "11" =>
                    c <= a xor b;
                when others => null;
            end case;
        end process logic_ops;        
    end generate generic_tech;
    
    xilinx_tech: if Technology = "XILINX" generate
            
        signal cout  : std_logic_vector (WIDTH-1 downto 0);
    begin
--
--      input mapping for LUT
--              I0 => op<0>
--              I1 => op<1>
--              I2 => a<n>
--              I3 => b<n>
--              O  => c<n>
--
--      Truth table for the LUT
--              a       b       op<1>   op<0>           INIT (O->output)
-------------------------------------------------------------------------------
--                                   MOVE
--              0       0          0       0            INIT[0] -> 0
--              0       1          0       0            INIT[4] -> 1
--              1       0          0       0            INIT[8] -> 0
--              1       1          0       0            INIT[C] -> 1
--                                   AND
--              0       0          0       1            INIT[1] -> 0
--              0       1          0       1            INIT[5] -> 0
--              1       0          0       1            INIT[9] -> 0
--              1       1          0       1            INIT[D] -> 1
--                                   OR
--              0       0          1       0            INIT[2] -> 0
--              0       1          1       0            INIT[6] -> 1
--              1       0          1       0            INIT[A] -> 1
--              1       1          1       0            INIT[E] -> 1
--                                   XOR
--              0       0          1       1            INIT[3] -> 0
--              0       1          1       1            INIT[7] -> 1
--              1       0          1       1            INIT[B] -> 1
--              1       1          1       1            INIT[F] -> 0
-------------------------------------------------------------------------------
-- INIT                         F E D C - B A 9 8 - 7 6 5 4 - 3 2 1 0
--                              0 1 1 1 - 1 1 0 0 - 1 1 0 1 - 0 0 0 0 -> X'7CD0'
        logic_ops_inst: for i  in 0 to WIDTH-1 generate
            constant row : natural := (WIDTH/2)- (i/2) -1;

            attribute BEL : string;
            attribute RLOC of logicop_lut : label is rloc_string(row,0,SLICE,(WIDTH/2));
            attribute BEL  of logicop_lut : label is bel_string(i);
            attribute INIT of logicop_lut : label is "7CD0";
        begin
            logicop_lut : LUT4
                generic map (INIT => X"7CD0")
                port map (O  => cout(i), I0 => op(0),
                          I1 => op(1), I2 => b(i), I3 => a(i));
        end generate logic_ops_inst;

        c <= cout;
    end generate xilinx_tech;

end Behavioral;
