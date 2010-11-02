-------------------------------------------------------------------------------
-- Title      : Pipeline control
-- Project    : MANIK-2
-------------------------------------------------------------------------------
-- File       : pipectrl.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-12-07
-- Last update: 2006-10-10
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Asserts pipeline control signal PCT_stall.
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-12-07  1.0      sandeep	Created
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

entity pipectrl is
    
    generic (WIDTH     : integer := 32;
             USER_INST : Boolean := True);

    port (WB_stall     : in  std_logic;
          WB_ldwbreg   : in  std_logic_vector (3 downto 0);
          WB_ldreg     : in  std_logic_vector (3 downto 0);
          WB_uireg     : in  std_logic_vector (3 downto 0);
          EX_mip       : in  std_logic;
          EX_uip       : in  std_logic;
          RF_load      : in  std_logic;
          RF_store     : in  std_logic;
          RF_rfwe      : in  std_logic;
          RF_dreg      : in  std_logic;
          RF_breg      : in  std_logic;
          RF_rd        : in  std_logic_vector (3 downto 0);
          RF_rb        : in  std_logic_vector (3 downto 0);
          RF_msu       : in  std_logic;
          RF_uinst     : in  std_logic;
          RF_lsu       : in  std_logic;
          RF_lsmsu     : in  std_logic;
          MULT_rd      : in  std_logic_vector (3 downto 0);
          MEM_Nbusy    : in  std_logic;
          INTR_pend    : in  std_logic;
          SFR_pdflag   : in  std_logic;
          ni_get_done  : in  std_logic;
          IAGU_syncif  : in  std_logic;
          MEM_dfetch_ip: in  std_logic;
          MEM_ld_done  : in  std_logic;
          clk          : in  std_logic;
          PCT_sdly     : out std_logic;
          PCT_msustall : out std_logic;
          PCT_stall    : out std_logic;
          PCT_ustall   : out std_logic);

end pipectrl;

architecture Behavioral of pipectrl is

    signal lsu_dreg   : std_logic := '0';
    
    signal stall_1 : std_logic := '0';
    signal stall_2 : std_logic := '0';
    signal stall_3 : std_logic := '0';
    signal stall_4 : std_logic := '0';
    signal stall_5 : std_logic := '0';
    signal stall_6 : std_logic := '0';
    signal stall_7 : std_logic := '0';
    signal stall_8 : std_logic := '0';
    signal stall_9 : std_logic := '0';
    signal stall_A : std_logic := '0';
    signal stall_B : std_logic := '0';
    
    signal stall_2or3 : std_logic := '0';
    signal stall_6or7 : std_logic := '0';
    signal stall_9orA : std_logic := '0';
    
    signal sdly      : std_logic                     := '0';
    signal stall_F   : std_logic_vector (2 downto 0) := "000";
    signal sdly_comb : std_logic                     := '0';

    attribute BEL : string;
begin  -- Behavioral

    sdly_comb <= '0' when stall_F /= "101" else '1';
    process (clk)
    begin
        if rising_edge(clk) then                
            if sdly_comb = '0' then
                stall_F <= stall_F + 1;
            end if;
            sdly <= sdly_comb;
        end if;
    end process;
    
    UI_swait: if USER_INST = True generate
        lsu_dreg <= (RF_lsmsu and (EX_mip or EX_uip)) or RF_dreg;    
    end generate UI_swait;

    NOUI_swait: if USER_INST = False generate
        lsu_dreg <= (RF_lsmsu and EX_mip) or RF_dreg;
    end generate NOUI_swait;
    
    -- I) if RF has load/store and load/store unit is busy
    stall_1 <= '1' when MEM_Nbusy = '0' and RF_lsu = '1' else '0' after 1 ns;

    -- II) if load in progress and WB_ldreg == either RF_rd or RF_rb 
    stall_2 <= '1' when (MEM_dfetch_ip & RF_dreg & WB_ldreg) = ("11" & RF_rd) else '0' after 1 ns;
    stall_3 <= '1' when (MEM_dfetch_ip & RF_breg & WB_ldreg) = ("11" & RF_rb) else '0' after 1 ns;
    stall_4 <= '0';--'1' when MEM_ld_done    = '1' and RF_breg = '1' and WB_ldwbreg = RF_rb else '0' after 1 ns;
    stall_2or3 <= stall_2 or stall_3 or stall_4;
    
    -- III) if RF Unit has a mult and mult unit is busy then
    stall_5 <= '1' when RF_msu = '1' and EX_mip = '1' else '0';

    -- IV) if multiply in progress and any of the registers used by the
    --     RF stage is the one being defined by multiply then stall
    stall_6 <= '1' when (EX_mip & lsu_dreg & MULT_rd) = ("11" & RF_rd) else '0' after 1 ns;
    stall_7 <= '1' when (EX_mip & RF_breg & MULT_rd)  = ("11" & RF_rb) else '0' after 1 ns;
    stall_6or7 <= stall_6 or stall_7;
    
    -- V) If RF has user inst and user unit busy then
    UI_s8: if USER_INST = True generate
        stall_8 <= '1' when RF_uinst = '1' and EX_uip = '1' else '0' after 1 ns;
    end generate UI_s8;
    
    NOUI_s8: if USER_INST = False generate
        stall_8 <= '0';
    end generate NOUI_s8;

    -- VI) if User instruction in progress and any of the registers require the
    --     register value.
    UI_s9A: if USER_INST = True generate
        stall_9 <= '1' when (EX_uip & lsu_dreg & WB_uireg) = ("11" & RF_rd) else '0' after 1 ns;
        stall_A <= '1' when (EX_uip & RF_breg  & WB_uireg) = ("11" & RF_rb) else '0' after 1 ns;
    end generate UI_s9A;
    
    NOUI_s9A: if USER_INST = False generate
        stall_9 <= '0';
        stall_A <= '0';
    end generate NOUI_s9A;
    stall_9orA <= stall_9 or stall_A;
    
    
    -- VII) if DE stage has a Branch and second half of address fetch not
    --      complete yet and we are in mispredicted branch & fetch not complete
    stall_B <= '1' when IAGU_syncif = '1' and ni_get_done = '0' else '0';
    
    -- stall_[1..B] should not effect interrupt handling.
    PCT_stall    <= ((stall_1 or stall_2or3 or stall_5 or stall_6or7 or
                      stall_8 or stall_9orA or stall_B or SFR_pdflag) and
                     (not INTR_pend)) or WB_stall or (not sdly_comb) after 1 ns;
    PCT_msustall <= stall_2or3 or stall_1 or stall_5 or stall_9orA or WB_stall or (not sdly_comb) after 1 ns;
    PCT_ustall   <= stall_1 or stall_5 or stall_8 or stall_B;
    
    PCT_sdly     <= sdly    after 1 ns;
end Behavioral;
