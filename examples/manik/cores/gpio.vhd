-------------------------------------------------------------------------------
-- Title      : General Purpose I/O - Wishbone compliant
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : gpio.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech Inc.
-- Created    : 2005-09-23
-- Last update: 2006-08-28
-- Platform   : 
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: Wishbone compliant GPIO module. The Input & output width cannot
--              exceed Bus width (32). The GPIO should be assigned a word aligned
--              address, it ignores the _SEL_I signals.
-------------------------------------------------------------------------------
-- Copyright (c) 2005 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2005-09-23  1.0      sdutta	Created
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

use work.manikconfig.all;
use work.manikpackage.all;

entity gpio is

    generic (WIDTH    : integer := 32;
             I_WIDTH  : integer := 32;
             O_WIDTH  : integer := 32;
             T_WIDTH  : integer := 32;
             DEBOUNCE : boolean := false;
             GENIRQ   : boolean := false;
             I_TRI    : boolean := false;  -- use gp_inout for input else use gp_input
             -- 1 Level; 2 pos edge; 3 neg edge; 4 either edge;
             I_TYPE   : integer := 1);  

    port (clk   : in std_logic;
          reset : in std_logic;

          -- Wishbone slave interface
          WBS_ADR_I : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
          WBS_SEL_I : in  std_logic_vector (3 downto 0);
          WBS_DAT_I : in  std_logic_vector (WIDTH-1 downto 0);
          WBS_WE_I  : in  std_logic;
          WBS_STB_I : in  std_logic;
          WBS_CYC_I : in  std_logic;
          WBS_CTI_I : in  std_logic_vector (2 downto 0);
          WBS_BTE_I : in  std_logic_vector (1 downto 0);
          WBS_DAT_O : out std_logic_vector (WIDTH-1 downto 0);
          WBS_ACK_O : out std_logic;
          WBS_ERR_O : out std_logic;

          gp_irq    : out   std_logic;  -- interrupt signal
          gp_inout  : inout std_logic_vector(T_WIDTH-1 downto 0);
          gp_input  : in    std_logic_vector(I_WIDTH-1 downto 0) := (others => '0');
          gp_output : out   std_logic_vector(O_WIDTH-1 downto 0));  -- Output to outside world        
        
end gpio;

architecture RTL of gpio is

    constant iwzero  : std_logic_vector(WIDTH-1 downto 0) := (others => '0');

    signal irq_detect : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal irq_mask   : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal tri_mask   : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal tmp_in     : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal tmp_tri    : std_logic_vector (T_WIDTH-1 downto 0);
    
    signal lout : std_logic_vector(WIDTH-1 downto 0) := (others => '0');
    
    signal istage0 : std_logic_vector(WIDTH-1 downto 0) := (others => '0');
    signal istage1 : std_logic_vector(WIDTH-1 downto 0) := (others => '0');
    signal istage2 : std_logic_vector(WIDTH-1 downto 0) := (others => '0');

    signal pedge : std_logic_vector(WIDTH-1 downto 0) := (others => '0');
    signal nedge : std_logic_vector(WIDTH-1 downto 0) := (others => '0');

    signal lout_sel, imsk_sel, tri_sel, we : std_logic;
    
    signal ack_o : std_logic := '0';
begin  -- RTL

    lout_sel <= '1' when WBS_ADR_I(3 downto 2) = "00" else '0';
    imsk_sel <= '1' when WBS_ADR_I(3 downto 2) = "01" else '0';
    tri_sel  <= '1' when WBS_ADR_I(3 downto 2) = "10" else '0';
    
    we 	     <=  WBS_STB_I and WBS_WE_I ;
    
    -- latch outputs when CPU writes
    lout_proc : process (clk, reset)
    begin
        if reset = '1' then
            lout <= (others => '0');
            irq_mask <= (others => '0');
        elsif rising_edge(clk) then
            if WBS_STB_I = '1' and WBS_WE_I = '1' then
                if lout_sel = '1' then
                    lout <= WBS_DAT_I ;
                elsif imsk_sel = '1' then
                    irq_mask <= WBS_DAT_I;
                elsif tri_sel = '1' then
                    tri_mask <= WBS_DAT_I;
                end if;                
            end if;
        end if;
    end process lout_proc;

    -- drive the tristate output
    -- assume that O_WIDTH >= T_WIDTH
    tmp_tri <= extend_vect(lout,T_WIDTH);
    dtri: for i in 0 to T_WIDTH-1 generate
        gp_inout(i) <= tmp_tri(i) when tri_mask(i) = '1' else 'Z';
    end generate dtri;
    
    -- assume O_WIDTH <= WIDTH
    gp_output <= lout(O_WIDTH-1 downto 0);

    -- synchronize input
    sync_i : process (clk, reset)
    begin
        if reset ='1' then
            istage0 <= (others => '0');
            istage1 <= (others => '0');
            istage2 <= (others => '0');
        elsif rising_edge(clk) then
            if I_TRI then
                istage0 <= extend_vect(gp_inout,WIDTH);
            else
                istage0 <= extend_vect(gp_input,WIDTH);
            end if;
            istage1 <= istage0 ;
            istage2 <= istage1 ;            
        end if;
    end process sync_i;

    -- detect positive & negative edges
    -- write to mask will clear it
    nedge_proc : process (clk, reset)
    begin
        if reset = '1' then
            nedge <= (others => '0');
            pedge <= (others => '0');
        elsif rising_edge(clk) then
            for i in 0 to WIDTH-1 loop
                -- negative edge detect
                if istage1(i) = '0' and istage2(i) = '1' then
                    nedge (i) <= '1';
                elsif we = '1' and imsk_sel = '1' then
                    nedge (i) <= nedge(i) and WBS_DAT_I(i);
                end if;
                
                -- positive edge detect
                if istage1(i) = '1' and istage2(i) = '0' then
                    pedge (i) <= '1';
                elsif we = '1' and imsk_sel = '1' then                    
                    pedge (i) <= pedge(i) and WBS_DAT_I(i);
                end if;
            end loop;  -- i
        end if;
    end process nedge_proc;

    get_input: for i in 0 to WIDTH-1 generate
        -- level
        lvl: if I_TYPE = 1 generate
            tmp_in(i) <= istage1(i);
        end generate lvl;
        -- positive edge
        pe: if I_TYPE = 2 generate
            tmp_in(i) <= pedge(i);
        end generate pe;
        -- negative edge
        ne: if I_TYPE = 3 generate
            tmp_in(i) <= nedge(i);
        end generate ne;
        -- any edge
        ae: if I_TYPE = 4 generate
            tmp_in(i) <= pedge(i) or nedge(i);
        end generate ae;
    end generate get_input;

    irq_and: for i in 0 to WIDTH-1 generate        
        irq_detect(i) <= tmp_in(i) and irq_mask(i);
    end generate irq_and;
    
    -- ack one cycle later.
    ack_proc : process (clk, reset)
    begin
        if reset = '1' then
            ack_o <= '0';
        elsif rising_edge(clk) then
            if ack_o = '1' then
                ack_o <= '0';
            else
                ack_o <= WBS_STB_I;                
            end if;
        end if;
    end process ack_proc;

    WBS_ACK_O <= ack_o;
    WBS_DAT_O <= extend_vect(irq_mask,WIDTH) when imsk_sel = '1' else
                 extend_vect(tmp_in,WIDTH)   when lout_sel = '1' else
                 extend_vect(tri_mask,WIDTH) when tri_sel  = '1' else
                 extend_vect(irq_detect,WIDTH);
    
    WBS_ERR_O <= '0';
    
    -- generate interrupt if configured .
    irqgen: if GENIRQ = true generate
        gp_irq  <= '1' when irq_detect /= iwzero else '0';        
    end generate irqgen;
    noirq: if GENIRQ = false generate
        gp_irq <= '0';
    end generate noirq;
end RTL;
