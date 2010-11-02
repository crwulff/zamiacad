-------------------------------------------------------------------------------
-- Title      : Write back and register scoreboarding related logic
-- Project    : MANIK-2
-------------------------------------------------------------------------------
-- File       : wback.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2003-08-30
-- Last update: 2006-08-07
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Implements writeback and register score boarding related logic
-------------------------------------------------------------------------------
-- Copyright (c) 2003 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2003-08-30  1.0      sandeep	Created
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

entity WBSBLogic is
    
    generic (USER_INST : boolean := True;
             WIDTH     : integer := 32);

    port (clk          : in  std_logic;
          MEM_ld_done  : in  std_logic;
          MEM_Nbusy    : in  std_logic;

          MULT_mdone   : in  std_logic;
          MULT_rd      : in  std_logic_vector(3 downto 0);

          UINST_uip    : in  std_logic;
          
          RF_load      : in  std_logic;
          RF_store     : in  std_logic;
          RF_rb        : in  std_logic_vector(3 downto 0);
          RF_rd        : in  std_logic_vector(3 downto 0);
          RF_msu       : in  std_logic;
          RF_uinst     : in  std_logic;
          
          PCT_stall    : in  std_logic;
          
          load         : out std_logic;
          store        : out std_logic;
          lsu          : out std_logic;
          ldreg        : out std_logic_vector(3 downto 0);
          ldwbreg      : out std_logic_vector(3 downto 0);
          
          mult_ip      : out std_logic;
          mwb_complete : out std_logic;
          mult_done    : out std_logic;
          
          uinst_ip     : out std_logic;
          uwb_complete : out std_logic;
          uinst_done   : out std_logic;
          uireg        : out std_logic_vector(3 downto 0));
end WBSBLogic;

architecture Behavioral of WBSBLogic is

    signal wbload  : std_logic;
    signal wbstore : std_logic;
    signal wbmdone : std_logic;
    signal wbudone : std_logic;
    
    -- registers
    signal wbldreg   : std_logic_vector(3 downto 0) := "0000";
    signal wbuireg   : std_logic_vector(3 downto 0) := "0000";
    signal wbmultip  : std_logic                    := '0';
    signal wbmwbc    : std_logic                    := '0';
    signal wbuinstip : std_logic                    := '0';
    signal wbuwbc    : std_logic                    := '0';

    attribute BEL : string;
begin  -- Behavioral

    ---------------------------------------------------------------------------
    -- LOAD/STORE write back related LOGIC
    ---------------------------------------------------------------------------
    lsu     <= (RF_load or RF_store) and not PCT_stall;
        
    wbload  <= RF_load  and not PCT_stall;
    wbstore <= RF_store and not PCT_stall;
        
    latch_ldreg : process (clk)
    begin
        if rising_edge(clk) then
            if wbload = '1' then
                wbldreg <= RF_rd after 1 ns;
            end if;
            ldwbreg <= wbldreg after 1 ns;
        end if;
    end process latch_ldreg;
        
    ldreg <= wbldreg;
    load  <= wbload;
    store <= wbstore;

    ---------------------------------------------------------------------------
    -- MULTIPLY write back relates logic
    ---------------------------------------------------------------------------
    generic_tech1: if Technology /= "XILINX" generate        
        -- latch in multiply in progress . Hold it till multiply
        -- result has been written back
        mstart_proc : process (clk)
        begin
            if (rising_edge(clk)) then
                if (wbmultip = '0' and PCT_stall = '0') or wbmwbc = '1' then                
                    wbmultip <= RF_msu and not PCT_stall;
                end if;
            end if;
        end process mstart_proc;

        wbmdone <= wbmultip  and MULT_mdone;
        -- load write back has highest priority, then multiply unit
        -- then UINST then EXunit
        mwb_proc : process (clk)
        begin
            if rising_edge(clk) then
                if wbmwbc = '1' then
                    wbmwbc <= '0';
                else                
                    wbmwbc  <= wbmdone and (not MEM_ld_done);
                end if;
            end if;
        end process mwb_proc;    
    end generate generic_tech1;

    xilinx_tech1: if Technology = "XILINX" generate
        signal mip_ce      : std_logic;
        signal wbmultip_c0 : std_logic;
        signal mip_comb    : std_logic;
        signal mwbc_comb   : std_logic;
        
        attribute INIT of mwbc_comb_lut : label is "2";
        attribute INIT of MIPCE_INST    : label is "AB";
        attribute INIT of WBMDONE_LUT   : label is "8";
        attribute INIT of mwbc_reg      : label is "0";
        attribute RLOC of mwbc_reg      : label is rloc_string((WIDTH/2)+1, -1, 0, (WIDTH/2)+1);
        attribute BEL  of mwbc_reg      : label is bel_string_ff(1);
        attribute RLOC of MIPCE_INST    : label is rloc_string((WIDTH/2), 0, 1, (WIDTH/2), true, 1, 1);
        attribute BEL  of MIPCE_INST    : label is bel_string(0);
        attribute RLOC of WBMDONE_LUT   : label is rloc_string((WIDTH/2)+1, 0, 0, (WIDTH/2)+1, true, 0, 1);
        attribute BEL  of WBMDONE_LUT   : label is bel_string(1);
    begin
        MIPCE_INST: LUT3
            generic map (INIT => X"AB")
            port map (O  => mip_ce,   I0 => wbmwbc,
                      I1 => PCT_stall,I2 => wbmultip);

        mip_comb <= RF_msu and not PCT_stall;
        WBMIP_REG : FDE
            generic map (INIT => '0')
            port map (Q  => wbmultip,C  => clk,
                      CE => mip_ce,  D  => mip_comb);
        WBMIP_REGC0 : FDE
            generic map (INIT => '0')
            port map (Q  => wbmultip_c0,C  => clk,
                      CE => mip_ce,     D  => mip_comb);
        
        WBMDONE_LUT : LUT2
            generic map (INIT => X"8")
            port map (O => wbmdone, I1 => wbmultip, I0 => MULT_mdone );
        
        mwbc_reg: FDR
            generic map (INIT => '0')
            port map (Q => wbmwbc,    C => clk,
                      D => mwbc_comb, R => wbmwbc);
        mwbc_comb_lut: LUT2
            generic map (INIT => X"2")
            port map (O  => mwbc_comb, I0 => wbmdone, I1 => MEM_ld_done);
    end generate xilinx_tech1;

    mult_ip      <= wbmultip;
    mwb_complete <= wbmwbc;
    mult_done    <= wbmdone;

    ---------------------------------------------------------------------------
    -- USER Instruction related logic
    ---------------------------------------------------------------------------
    UI_UNIT: if USER_INST = True generate
        generic_tech2: if Technology /= "XILINX" generate
            -- latch in RF_rd and RF_uinst , and hold till the
            -- write back is complete
            ui_proc : process (clk)
            begin
                if rising_edge(clk) then
                    if (wbuinstip = '0' and PCT_stall = '0') or wbuwbc = '1' then 
                        wbuinstip <= RF_uinst;
                        wbuireg   <= RF_rd;
                    end if;
                end if;
            end process ui_proc;

            wbudone <= wbuinstip and (not UINST_uip);

            uwb_proc : process (clk)
            begin
                if rising_edge(clk) then
                    if wbuwbc = '1' then
                        wbuwbc <= '0';
                    else
                        wbuwbc <= wbudone  and (not wbmdone) and (not MEM_ld_done);                
                    end if;
                end if;
            end process uwb_proc;

            uinst_ip     <= wbuinstip and (not wbuwbc);
        end generate generic_tech2;

        xilinx_tech2: if Technology = "XILINX" generate
            signal uice : std_logic;
            signal uwbc_comb : std_logic;
            
            attribute INIT of UICE_LUT : label is "AB";
            attribute INIT of UIP_INST : label is "4";

            attribute RLOC of UWBC_REG : label is rloc_string((WIDTH/2)+1,2,0,(WIDTH/2)+1,true,0,3);
            attribute BEL  of UWBC_REG : label is bel_string_ff(1);
            attribute RLOC of UIP_INST : label is rloc_string((WIDTH/2)+1,2,1,(WIDTH/2)+1,true,0,3);
            attribute BEL  of UIP_INST : label is bel_string(1);
            attribute RLOC of WBUIREG0 : label is rloc_string(WIDTH/2,3,true,-(WIDTH/2),4);
            attribute RLOC of WBUIREG1 : label is rloc_string(WIDTH/2,4,true,-(WIDTH/2),5);
            
        begin
            
            UICE_LUT : LUT3
                generic map (INIT => X"AB")
                port map (O  => uice,     I0 => wbuwbc,
                          I1 => PCT_stall,I2 => wbuinstip);
            UIPREG : FDE
                generic map (INIT => '0')
                port map (Q  => wbuinstip, C  => clk,
                          CE => uice, D  => RF_uinst);
            WBUIREG0 : FDE_VECTOR
                generic map (WIDTH => 2, SLICE => 0)
                port map (CLK => clk, CE  => uice,
                          D   => RF_rd  (1 downto 0), Q   => wbuireg(1 downto 0));
            WBUIREG1 : FDE_VECTOR
                generic map (WIDTH => 2, SLICE => 1)
                port map (CLK => clk, CE  => uice,
                          D   => RF_rd  (3 downto 2), Q   => wbuireg(3 downto 2));
            
            wbudone   <= wbuinstip and (not UINST_uip);
            uwbc_comb <= wbuinstip and (not UINST_uip) and (not wbmdone) and (not MEM_ld_done) after 1 ns;

            UWBC_REG  : FDR
                generic map (INIT => '0')
                port map (Q => wbuwbc,   C => clk,
                          D => uwbc_comb,R => wbuwbc);
            UIP_INST : LUT2
                generic map (INIT => X"4")
                port map (O => uinst_ip, I1 => wbuinstip, I0 => wbuwbc);
            
        end generate xilinx_tech2;
        
        uinst_done   <= wbudone;
        uwb_complete <= wbuwbc;
        uireg        <= wbuireg;
    end generate UI_UNIT;

    NOUI_UNIT: if USER_INST = False generate
        uinst_done   <= '0';
        uinst_ip     <= '0';
        uwb_complete <= '0';
        uireg        <= "0000";
    end generate NOUI_UNIT;

end Behavioral;
