-------------------------------------------------------------------------------
-- Title      : addsub
-- Project    : Manik-2
-------------------------------------------------------------------------------
-- File       : addsub.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-09-07
-- Last update: 2006-09-17
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: add or subtract two equal length vectors
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

library UNISIM;
use UNISIM.vcomponents.all;

use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;
use work.manikaltera.all;
use work.maniklattice.all;
use work.manikactel.all;

entity addsub is
    generic (WIDTH      : integer := 32;
             SLICE      : integer := 1) ;
    Port ( add  : in    std_logic;                              -- '1' perform add else subtract
           a    : in    std_logic_vector (WIDTH-1 downto 0);    -- input a 
           b    : in    std_logic_vector (WIDTH-1 downto 0);    -- input b
           ci   : in    std_logic;                              -- carry in 
           c    : out   std_logic_vector (WIDTH-1 downto 0);    -- output (result)         
           co   : out   std_logic;                              -- carry out
           ov   : out   std_logic);                             -- overflow flag
end addsub;

architecture Behavioral of addsub is
    signal ctemp  : std_logic_vector (WIDTH   downto 0);
    signal itemp  : std_logic_vector (WIDTH-1 downto 0);
    signal cout   : std_logic_vector (WIDTH-1 downto 0);
    signal cot    : std_logic;
    signal ovt    : std_logic;
    attribute BEL : string;
begin

    generic_tech: if Technology /= "XILINX"  and Technology /= "ALTERA"  and
                     Technology /= "LATTICE" and Technology /= "ACTEL" generate        
        signal atmp, btmp : std_logic_vector (WIDTH downto 0);
    begin
        adsu_behav: for i in 0 to (WIDTH-1) generate
            itemp(i)        <= a(i) xor b(i) xor (not add);
            cout(i)         <= ctemp(i) xor itemp(i);
            ctemp(i+1)      <= a(i) when itemp(i) = '0' else ctemp(i);
        end generate adsu_behav;        
        ctemp(0) <= ci xnor add;
        c           <= cout;
        ov          <= '0';
        co          <= ctemp(WIDTH) xnor add;
    end generate generic_tech;
    
    xilinx_tech: if Technology = "XILINX" generate
        signal notadd : std_logic;
        signal cw     : std_logic;

        attribute INIT of xnor_lut1 : label is "9";
--        attribute RLOC of xnor_lut1 : label is rloc_string(WIDTH/2,-2,0,WIDTH/2,true,0,-2);
        attribute BEL  of xnor_lut1 : label is bel_string(1);
    
        attribute INIT of xnor_lut2 : label is "9";
    begin
        notadd      <= not add;

        xnor_lut1: LUT2 generic map (INIT => X"9")
            port map (I0 => ci, I1 => add, O  => ctemp(0));
        xnor_lut2: LUT2 generic map (INIT => X"9")
            port map (I0 => ctemp(WIDTH), I1 => add, O  => co);
        
        addsub_x: for i in 0 to WIDTH-1 generate
            constant row    : natural := (WIDTH/2) - i/2 - 1;
--            attribute RLOC of adsuXORCY   : label is rloc_string(row,0,SLICE,(WIDTH/2));
        begin
            adsuXOR3 : XOR3 port map (I0 => notadd, I1 => a(i), I2 => b(i), O  => itemp(i));

            adsuMUXCY_L: if i <= WIDTH-3 generate            
--                attribute RLOC of adsuMUXCY_Linst : label is rloc_string(row,0,SLICE,(WIDTH/2));
            begin
                adsuMUXCY_Linst : MUXCY_L port map (S  => itemp(i), DI => a(i),
                                                    CI => ctemp(i), LO => ctemp(i+1));
            end generate adsuMUXCY_L;

            adsuMUXCY_D: if i = (WIDTH-2) generate
--                attribute RLOC of adsuMUXCY_Dinst : label is rloc_string(row,0,SLICE,(WIDTH/2));
            begin
                -- FIXME gb: don't have this component adsuMUXCY_Dinst : MUXCY_D port map (S  => itemp(i), DI => a(i), CI => ctemp(i),
                -- FIXME gb: don't have this component                                  LO => ctemp(i+1), O  => cw);            
            end generate adsuMUXCY_D;

            adsuMUXCY: if i = (WIDTH-1) generate            
--                attribute RLOC of adsuMUXCY_inst   : label is rloc_string(row,0,SLICE,(WIDTH/2));
            begin
                adsuMUXCY_inst : MUXCY port map (S  => itemp(i), DI => a(i),
                                                 CI => ctemp(i), O  => ctemp(i+1));
            end generate adsuMUXCY;
            
            adsuXORCY : XORCY port map (LI => itemp(i), CI => ctemp(i), O  => cout(i));
        end generate addsub_x;
        c           <= cout;
        ov          <= '0';
    end generate xilinx_tech;


    altera_tech: if Technology = "ALTERA" generate
        signal ci_tmp, co_tmp : std_logic;
    begin
        ci_tmp <= ci xnor add;
        co     <= co_tmp xnor add;
        altera_adsu : lpm_add_sub
            port map (dataa   => a,
                      add_sub => add,
                      datab   => b,
                      cin     => ci_tmp,
                      cout    => co_tmp,
                      result  => c);        
    end generate altera_tech;

    lattice_tech: if Technology = "LATTICE" generate
        signal ci_tmp, co_tmp : std_logic;
    begin        
        ci_tmp <= ci xnor add;
        co     <= co_tmp xnor add;
        ECP_FAM: if Lattice_Family = "ECP" generate
            adsu_ecp_1: adsu_ecp
                port map (DataA   => a,
                          DataB   => b,
                          Cin     => ci_tmp,
                          Add_Sub => add,
                          Result  => c,
                          Cout    => co_tmp);            
        end generate ECP_FAM;
    end generate lattice_tech;
    
    actel_tech: if Technology = "ACTEL" generate
        signal ci_tmp, co_tmp : std_logic;
    begin
        ci_tmp <= ci xnor add;
        co     <= co_tmp xnor add;
        APA_Family: if Actel_Family = "APA" generate
            actel_adsu: adsu
                port map (DataA  => a,
                          DataB  => b,
                          Cin    => ci_tmp,
                          Addsub => add,
                          Sum    => c,
                          Cout   => co_tmp);            
        end generate APA_Family;

        APA3_Family: if Actel_Family = "APA3" generate
            actel_adsu: adsu_apa3
                port map (DataA  => a,
                          DataB  => b,
                          Cin    => ci_tmp,
                          Addsub => add,
                          Sum    => c,
                          Cout   => co_tmp);            
        end generate APA3_Family;
        
    end generate actel_tech;
end Behavioral;

