-------------------------------------------------------------------------------
-- Title      : Timer
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : timer.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2003-01-07
-- Last update: 2006-01-05
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Implements the Timer for MANIK2
-------------------------------------------------------------------------------
-- Copyright (c) 2003 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2003-01-07  1.0      sandeep	Created
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

entity Timer is
  
    generic (WIDTH         : integer := 32;
             TIMER_WIDTH   : integer := 32;
             TIMER_CLK_DIV : integer := 0);

    port (SFR_teflag  : in  std_logic;
          SFR_tiflag  : in  std_logic;
          SFR_tstart  : in  std_logic;
          SFR_trload  : in  std_logic;
          SFR_trval   : in  std_logic_vector (TIMER_WIDTH-1 downto 0);
          clk         : in  std_logic;
          TIMER_val   : out std_logic_vector(TIMER_WIDTH-1 downto 0);
          TIMER_exp   : out std_logic;
          TIMER_undf  : out std_logic);

end Timer;

architecture Behavioral of Timer is
    signal tcounter : std_logic_vector (TIMER_WIDTH-1 downto 0) := (others => '0');
    signal tc_clk   : std_logic_vector (15 downto 0)            := conv_std_logic_vector(1, 16);
    
    signal texp  : std_logic := '0';
    signal tundf : std_logic := '0';
    signal ttype : std_logic := '0';
    
    signal tc_reload : std_logic;
    signal tc_clk_d  : std_logic;
    signal tc_iszero : std_logic;

    attribute BEL : string;
    
begin  -- Behavioral

    generic_tech: if Technology /= "XILINX" generate        
        -- timer clock - divides the system clock
        tc_clk_proc : process (clk)
        begin
            if rising_edge(clk) then
                if tc_clk = conv_std_logic_vector(0,16) then
                    tc_clk <= conv_std_logic_vector(1,16) after 1 ns;
                else                
                    tc_clk <= tc_clk(14 downto 0) & tc_clk(TIMER_CLK_DIV) after 1 ns;
                end if;
            end if;
        end process tc_clk_proc;

        tc_clk_d <= tc_clk(TIMER_CLK_DIV);

        -- timer counter loads the SFR_trval and counts
        -- downto 0 , stops at zero till SFR_tstart or SFR_trload
        tc_reload <= (SFR_tstart and tc_iszero) or SFR_trload;
        tcounter_proc : process (clk)
        begin
            if rising_edge(clk) then
                if tc_reload = '1' then
                    tcounter <= SFR_trval after 1 ns;
                elsif tc_iszero = '0' and tc_clk_d = '1' then
                    tcounter <= tcounter - 1 after 1 ns;
                end if;
            end if;
        end process tcounter_proc;

        -- load the type of timer reload
        ttype_proc : process (clk)
        begin
            if rising_edge(clk) then
                if tc_reload = '1' then
                    ttype <= SFR_trload after 1 ns;
                end if;
            end if;
        end process ttype_proc;
        
        tc_iszero <= '1' when tcounter = conv_std_logic_vector(0,TIMER_WIDTH) else '0';

        texp_undf_proc : process (clk)
        begin
            if rising_edge(clk) then
                texp  <= (SFR_teflag and not SFR_tiflag) and tc_iszero after 1 ns;
                tundf <= tc_iszero and ttype after 1 ns;
            end if;
        end process texp_undf_proc;
    end generate generic_tech;

    xilinx_tech: if Technology = "XILINX" generate
        constant clkdiv : std_logic_vector (3 downto 0)             := conv_std_logic_vector(TIMER_CLK_DIV, 4);
        constant mone   : std_logic_vector (TIMER_WIDTH-1 downto 0) := conv_std_logic_vector(-1, TIMER_WIDTH);
        
        signal texp_c    : std_logic;
        signal tc_nload  : std_logic;
        signal tc_dec    : std_logic;
        signal tc_ce     : std_logic;    
        signal tcmux     : std_logic_vector (TIMER_WIDTH-1 downto 0);

        attribute INIT of tc_clk_proc : label is "0001";

        attribute RLOC of tcmux_inst    : label is rloc_string(0, 0);
        attribute RLOC of tcounter_inst : label is rloc_string(0, 0);
        attribute RLOC of texp_reg      : label is rloc_string((TIMER_WIDTH/2), -1, 1,TIMER_WIDTH/2,true,0,-1);
        attribute RLOC of tciszero_inst : label is rloc_string((TIMER_WIDTH/2)-3, 0,true,-(TIMER_WIDTH/2)+3,0);
        
    begin  -- XILINX_virtex

        -- timer clock - divides the system clock

        tc_clk_proc: SRL16
            generic map (INIT  => X"0001")
            port map (Q   => tc_clk_d,
                      A0  => clkdiv(0), A1  => clkdiv(1),
                      A2  => clkdiv(2), A3  => clkdiv(3),
                      CLK => clk,       D   => tc_clk_d);

        tc_nload <= not ((SFR_tstart and tc_iszero) or SFR_trload);
        
        tc_ce <= (tc_clk_d and not tc_iszero) or (not tc_nload);
        tcmux_inst: MUX_ADD_VECTOR
            generic map (WIDTH => TIMER_WIDTH, SLICE => 1)
            port map (A => tcounter,  B   => mone,
                      C => SFR_trval, ADD => tc_nload, O => tcmux);
        
        -- timer counter loads the SFR_trval and counts -- downto 0 ,
        -- stops at zero till SFR_tstart
        tcounter_inst: FDE_VECTOR
            generic map (WIDTH => TIMER_WIDTH, SLICE => 1)
            port map (CLK => clk, CE  => tc_ce,
                      D   => tcmux, Q => tcounter);
        
        tciszero_inst: NOR_VECTOR
            generic map (WIDTH => TIMER_WIDTH, SLICE => 0)
            port map (a => tcounter, o => tc_iszero);

        texp_c <= ((SFR_teflag and not SFR_tiflag) and tc_iszero) after 1 ns;
        texp_reg: FD
            generic map (INIT => '0')
            port map (Q => texp, C => clk, D => texp_c);

        -- load the type of timer reload
        ttype_proc : process (clk)
        begin
            if rising_edge(clk) then
                if tc_ce = '1' then
                    ttype <= SFR_trload after 1 ns;
                end if;
            end if;
        end process ttype_proc;

        texp_undf_proc : process (clk)
        begin
            if rising_edge(clk) then
                tundf <= tc_iszero and ttype after 1 ns;
            end if;
        end process texp_undf_proc;
        
    end generate xilinx_tech;
    
    TIMER_val  <= tcounter;
    TIMER_exp  <= texp;
    TIMER_undf <= tundf;
end Behavioral;
