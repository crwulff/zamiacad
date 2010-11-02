-------------------------------------------------------------------------------
-- Title      : Multipler & Shifter Unit
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : multshift.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-12-31
-- Last update: 2006-05-23
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: 2*WIDTH-1 pipelined multiplier with early finish.
--              and shift by arbitrary values
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-12-31  1.0      sandeep	Created
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
use work.manikactel.all;

entity multshift is
  
    generic (WIDTH        : integer := 32;
             SHIFT_SWIDTH : integer := 3;
             MULT_BWIDTH  : integer := 32);

    port (RF_msu     : in  std_logic;
          RF_shift   : in  std_logic;
          RF_shcode  : in  std_logic_vector (1 downto 0);
          clk        : in  std_logic;
          INTR_reset : in  std_logic;
          msopa      : in  std_logic_vector (WIDTH-1 downto 0);
          msopb      : in  std_logic_vector (WIDTH-1 downto 0);
          stall      : in  std_logic;
          RF_rd      : in  std_logic_vector (3 downto 0);
          MULT_mdone : out std_logic;
          MULT_out   : out std_logic_vector (WIDTH-1 downto 0);
          MULT_rd    : out std_logic_vector (3 downto 0));

end multshift;

architecture RTL of multshift is
    attribute BEL : string;

        component funnel
            generic (WIDTH    : integer; SELWIDTH : integer);
            port (b           : in  std_logic_vector (WIDTH-1 downto 0);
                  right_shift : in  std_logic;
                  right_sign  : in  std_logic;
                  sel         : in  std_logic_vector (SELWIDTH-1 downto 0);
                  c           : out std_logic_vector (WIDTH-1 downto 0));
        end component;

        constant subval      : integer := (2**SHIFT_SWIDTH)-1;
        constant mult_cycles : integer := WIDTH/MULT_BWIDTH;
        
        signal ropa      : std_logic_vector (WIDTH-1 downto 0)     := (others => '0');
        signal ropb      : std_logic_vector (WIDTH-1 downto 0)     := (others => '0');
        signal shcode    : std_logic_vector (1 downto 0)           := "00";
        signal multrd    : std_logic_vector (3 downto 0)           := "0000";
        signal shift     : std_logic                               := '0';
        signal shopb_reg : std_logic_vector (4 downto 0)           := "00000";        
        signal shopb     : std_logic_vector (4 downto 0)           := "00000";        
        signal opa       : std_logic_vector (WIDTH-1 downto 0)     := (others => '0');
        
        signal mout     : std_logic_vector ((MULT_BWIDTH+WIDTH)-1 downto 0) := (others => '0');
        signal mout_reg : std_logic_vector(WIDTH-1 downto 0)                := (others => '0');

        signal mult_ccount : std_logic_vector(15 downto 0) := (others => '0'); 
        
        signal shout     : std_logic_vector (WIDTH-1 downto 0)     := (others => '0');
        signal shin      : std_logic_vector (WIDTH-1 downto 0)     := (others => '0');
        signal fsel      : std_logic_vector (SHIFT_SWIDTH-1 downto 0)  := (others => '0');
        
        signal right_shift : std_logic := '0';
        signal right_sign  : std_logic := '0';
        signal opa_ce      : std_logic := '0';

        signal sbz     : std_logic := '0';
        signal sbz_reg : std_logic := '0';
        
        signal shamt      : std_logic_vector (4 downto 0) := "00000";
        signal shamt_next : std_logic_vector (5 downto 0) := "000000";
        signal shamt_reg  : std_logic_vector (4 downto 0) := "00000";        
        signal shamt_zx   : std_logic_vector (5 downto 0);
        signal fsel_sbz   : std_logic := '0'; 
        signal uzero      : std_logic := '0';
        
        signal ms_state : std_logic_vector (7 downto 0) := "00000000";
        signal ms_next  : std_logic_vector (7 downto 0) := "00000000";
        
        constant ms_idle        : std_logic_vector (7 downto 0) := "00000000";
        constant ms_shift_start : std_logic_vector (7 downto 0) := "10000010";
        constant ms_shift_cont  : std_logic_vector (7 downto 0) := "10000100";
        constant ms_mult_start  : std_logic_vector (7 downto 0) := "10001000";
        constant ms_mult_cont   : std_logic_vector (7 downto 0) := "10010000";
        
begin  -- RTL

    MULT_out   <= mout_reg when shift = '0' else opa;
    MULT_mdone <= not ms_state(7) ;
    MULT_rd    <= multrd;
    
    shopb <= msopb(4 downto 0);
    sbz   <= '1'   when shopb = "0000"     else '0';
    
    -- register incoming signals
    regin_proc : process (clk)
    begin
        if rising_edge(clk) then
            -- register only when idle , not stalled and
            --  RF stage has shift or multiply
            if ms_state(7) = '0' and stall = '0' and RF_msu = '1' then
                ropa      <= msopa     after 1 ns;
                ropb      <= msopb     after 1 ns;
                shcode    <= RF_shcode after 1 ns;
                multrd    <= RF_rd     after 1 ns;
                shift     <= RF_shift  after 1 ns;
                shopb_reg <= shopb     after 1 ns;
                sbz_reg   <= sbz       after 1 ns;
            end if;
        end if;
    end process regin_proc;

    -- shift amount
    shamt      <= shopb_reg when ms_state(1) = '1' or sbz_reg = '1' else shamt_reg;
    shamt_zx   <= '0' & shamt;
    shamt_next <= shamt_zx - subval;

    fsel_sbz <= shcode(0);
    uzero    <= '1'  when shamt(4 downto SHIFT_SWIDTH) = ALLZEROS(4 downto SHIFT_SWIDTH) else '0';
    fsel     <= not shamt(SHIFT_SWIDTH-1 downto 0) when uzero = '1' and shcode(0) = '0' else
                shamt(SHIFT_SWIDTH-1 downto 0) when uzero = '1' else
                replicate_bit(fsel_sbz,SHIFT_SWIDTH);

    shin <= ropa when ms_state(1) = '1' else opa;
    
    -- shifter stuff
    funnel_1: funnel
        generic map (WIDTH    => WIDTH, SELWIDTH => SHIFT_SWIDTH)
        port map (b           => shin,
                  right_shift => shcode(0),
                  right_sign  => shcode(1),
                  sel         => fsel,
                  c           => shout);

    opa_ce <= '1' when  ms_state(7) = '1' and
              (sbz_reg = '1' or shamt_next(5) = '0' or
               (shamt_next(5) = '1' and ms_state(7) = '1')) else '0';
    out_regs : process (clk)
    begin
        if rising_edge(clk) then
            if opa_ce = '1' then
                opa   <= shout after 1 ns;
            end if;
        end if;
    end process out_regs;

    process (clk)
    begin
        if rising_edge(clk) then
            if shamt_next(5) = '0' then
                shamt_reg <= shamt_next(4 downto 0) after 1 ns;
            end if;
        end if;
    end process;
    
    -- multiplier / shifter state machine
    ms_smach: process (clk,INTR_reset)
    begin
        if INTR_reset = '1' then
            ms_state    <= ms_idle;
            mult_ccount <= (others => '0');
        elsif rising_edge(clk) then
            case ms_state is
                when ms_idle =>
                    if stall = '0' and RF_msu = '1' then
                        if RF_shift = '1' then
                            ms_state    <= ms_shift_start after 1 ns;
                            mult_ccount <= (others => '0');
                        elsif mult_cycles > 0 then
                            ms_state    <= ms_mult_start after 1 ns;
                            mult_ccount <= "0000000000000001" after 1 ns;
                        else
                            ms_state <= ms_idle after 1 ns;
                            mult_ccount <= (others => '0');
                        end if;
                    else
                        ms_state    <= ms_idle after 1 ns;
                        mult_ccount <= (others => '0');
                    end if;

                when ms_shift_start =>
                    if (shamt_next(5) = '1' or sbz_reg = '1') then
                        ms_state <= ms_idle after 1 ns;
                    else                            
                        ms_state <= ms_shift_cont after 1 ns;
                    end if;

                when ms_shift_cont =>
                    if (shamt_next(5) = '1' or sbz_reg = '1')  then
                        ms_state <= ms_idle after 1 ns;
                    else
                        ms_state <= ms_shift_cont after 1 ns;
                    end if;

                when ms_mult_start =>
                    if mult_ccount(mult_cycles-1) = '1' then
                        ms_state <= ms_idle after 1 ns;
                    else
                        ms_state <= ms_mult_cont after 1 ns;
                    end if;
                    mult_ccount <= mult_ccount(14 downto 0) & '0';

                when ms_mult_cont => 
                    if mult_ccount(mult_cycles-1) = '1' then
                        ms_state <= ms_idle after 1 ns;
                    else
                        ms_state <= ms_mult_cont after 1 ns;
                    end if;
                    mult_ccount <= mult_ccount(14 downto 0) & '0';
                when others => null;
            end case;
        end if;
    end process ms_smach;

    -- multiplier is simple, just let synthesis handle it
--        full_mult: if mult_cycles = 1 generate
--            mout     <= ropa * ropb(MULT_BWIDTH-1 downto 0);
--            mout_reg <= mout (WIDTH-1 downto 0);
--        end generate full_mult;

    -- bit sliced multiplication
    part_mult: if mult_cycles > 0 generate
        signal mopa, mopb    : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
        signal addoreg, addo : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    begin
        mout  <= mopa * mopb(MULT_BWIDTH-1 downto 0);
        no_add: if mult_cycles = 1 generate
            addo  <= mout(WIDTH-1 downto 0);
        end generate no_add;
        add_req: if mult_cycles > 1 generate
            addo  <= mout(WIDTH-1 downto 0) + addoreg;                
        end generate add_req;

        process (clk)
        begin
            if rising_edge(clk) then
                if ms_state(7) = '0' then
                    addoreg <= (others => '0');
                else
            	    addoreg <= addo ;
                end if;
            end if;
            
            if rising_edge(clk) then
                if ms_state(7) = '0' then
                    mopa <= msopa;
                    mopb <= msopb;
                else
                    mopa <= mopa(WIDTH-MULT_BWIDTH-1 downto 0) & replicate_bit('0',MULT_BWIDTH);
                    mopb <= replicate_bit('0',MULT_BWIDTH) & mopb(WIDTH-1 downto MULT_BWIDTH);
                end if;
            end if;

            if rising_edge(clk) then
                if ms_state(3) = '1' or ms_state(4) = '1' then
                    mout_reg <= addo;
                end if;
            end if;
        end process ;
    end generate part_mult;            
end RTL;
