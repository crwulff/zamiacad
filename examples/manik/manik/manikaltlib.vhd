-------------------------------------------------------------------------------
-- Title      : maniklib
-- Project    : MANIK-2
-------------------------------------------------------------------------------
-- File       : maniklib.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-10-28
-- Last update: 2006-01-05
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Contains ALTERA Specific optimized Library
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-10-28  1.0      sandeep	Created
-------------------------------------------------------------------------------


library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library lpm;
use lpm.lpm_components.all;
use work.manikconfig.all;
use work.manikpackage.all;

library UNISIM;
use UNISIM.vcomponents.all;

ENTITY mux41 IS
	PORT (data3x		: IN STD_LOGIC_VECTOR (31 DOWNTO 0);
              data2x		: IN STD_LOGIC_VECTOR (31 DOWNTO 0);
              data1x		: IN STD_LOGIC_VECTOR (31 DOWNTO 0);
              data0x		: IN STD_LOGIC_VECTOR (31 DOWNTO 0);
              sel		: IN STD_LOGIC_VECTOR (1 DOWNTO 0);
              result		: OUT STD_LOGIC_VECTOR (31 DOWNTO 0));
END mux41;


ARCHITECTURE SYN OF mux41 IS

--	type STD_LOGIC_2D is array (NATURAL RANGE <>, NATURAL RANGE <>) of STD_LOGIC;

	SIGNAL sub_wire0	: STD_LOGIC_VECTOR (31 DOWNTO 0);
	SIGNAL sub_wire1	: STD_LOGIC_VECTOR (31 DOWNTO 0);
	SIGNAL sub_wire2	: STD_LOGIC_2D (3 DOWNTO 0, 31 DOWNTO 0);
	SIGNAL sub_wire3	: STD_LOGIC_VECTOR (31 DOWNTO 0);
	SIGNAL sub_wire4	: STD_LOGIC_VECTOR (31 DOWNTO 0);
	SIGNAL sub_wire5	: STD_LOGIC_VECTOR (31 DOWNTO 0);

	COMPONENT lpm_mux
            GENERIC (lpm_size		: NATURAL;
                     lpm_widths		: NATURAL;
                     lpm_width		: NATURAL;
                     lpm_type		: STRING);
            PORT (sel	: IN STD_LOGIC_VECTOR (1 DOWNTO 0);
                  data	: IN STD_LOGIC_2D (3 DOWNTO 0, 31 DOWNTO 0);
                  result	: OUT STD_LOGIC_VECTOR (31 DOWNTO 0));
	END COMPONENT;

BEGIN
	sub_wire5    <= data0x(31 DOWNTO 0);
	sub_wire4    <= data1x(31 DOWNTO 0);
	sub_wire3    <= data2x(31 DOWNTO 0);
	result       <=sub_wire0(31 DOWNTO 0);
	sub_wire1    <= data3x(31 DOWNTO 0);
        wire3: for i in 0 to 31 generate
            sub_wire2(3,i) <= sub_wire1(i);
        end generate wire3;
        wire2: for i in 0 to 31 generate
            sub_wire2(2,i) <= sub_wire3(i);
        end generate wire2;
        wire1: for i in 0 to 31 generate
            sub_wire2(1,i) <= sub_wire4(i);
        end generate wire1;
        wire0: for i in 0 to 31 generate
            sub_wire2(0,i) <= sub_wire5(i);
        end generate wire0;
        lpm_mux_component : lpm_mux
            GENERIC MAP (lpm_size   => 4,
                         lpm_widths => 2,
                         lpm_width  => 32,
                         lpm_type   => "LPM_MUX")
            PORT MAP (sel    => sel,
                      data   => sub_wire2,
                      result => sub_wire0);

END SYN;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library lpm;
use lpm.lpm_components.all;
use work.manikconfig.all;
use work.manikpackage.all;

ENTITY apll IS
    PORT (inclk0	: IN STD_LOGIC  := '0';
          c0		: OUT STD_LOGIC);
END apll;


ARCHITECTURE SYN OF apll IS

    SIGNAL sub_wire0	: STD_LOGIC_VECTOR (5 DOWNTO 0);
    SIGNAL sub_wire1	: STD_LOGIC ;
    SIGNAL sub_wire2	: STD_LOGIC ;
    SIGNAL sub_wire3	: STD_LOGIC_VECTOR (1 DOWNTO 0);
    SIGNAL sub_wire4_bv	: BIT_VECTOR (0 DOWNTO 0);
    SIGNAL sub_wire4	: STD_LOGIC_VECTOR (0 DOWNTO 0);



    COMPONENT altpll
        GENERIC (clk0_duty_cycle        : NATURAL;
                 lpm_type               : STRING;
                 clk0_multiply_by       : NATURAL;
                 inclk0_input_frequency : NATURAL;
                 clk0_divide_by         : NATURAL;
                 pll_type               : STRING;
                 intended_device_family : STRING;
                 operation_mode         : STRING;
                 compensate_clock       : STRING;
                 clk0_phase_shift       : STRING);
        PORT (inclk : IN  STD_LOGIC_VECTOR (1 DOWNTO 0);
              clk   : OUT STD_LOGIC_VECTOR (5 DOWNTO 0));
    END COMPONENT;

BEGIN
    sub_wire4_bv(0 DOWNTO 0) <= "0";
    sub_wire4                <= To_stdlogicvector(sub_wire4_bv);
    sub_wire1                <= sub_wire0(0);
    c0                       <= sub_wire1;
    sub_wire2                <= inclk0;
    sub_wire3                <= sub_wire4(0 DOWNTO 0) & sub_wire2;

    altpll_component : altpll
        GENERIC MAP (clk0_duty_cycle        => 50,
                     lpm_type               => "altpll",
                     clk0_multiply_by       => CLK_MULBY,
                     inclk0_input_frequency => CLKIN_FREQ,
                     clk0_divide_by         => CLK_DIVBY,
                     pll_type               => "FAST",
                     intended_device_family => Altera_Family,
                     operation_mode         => "NORMAL",
                     compensate_clock       => "CLK0",
                     clk0_phase_shift       => "0")
        PORT MAP (inclk => sub_wire3,
                  clk   => sub_wire0);

END SYN;
