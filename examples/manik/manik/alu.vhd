-------------------------------------------------------------------------------
-- Title      : alu.vhd
-- Project    : MANIK-2
-------------------------------------------------------------------------------
-- File       : alu.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-10-04
-- Last update: 2006-10-08
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Arithmetic & logic unit. Puts together four modules
--                      a) addsub
--                      b) szextend
--                      c) logicop
--                      d) shifter
--              op = operation to be performed for individual modules
--         grp_sel = used to mux the output
--                   00 - selcet addsub   output
--                   01 - select logicop  output
--                   10 - select szextend output
--                   11 - select shifter  output
--
--              zf = '1' when output = (others => '0') valid for all ops
--              nf = '1' when output < 0 valid for all ops
--              co = carry out valid only for addsub
--              ov = overflow flag valid only for addsub
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-10-04  1.0      sandeep	Created
-------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;
use UNISIM.vcomponents.all;
library UNISIM;


entity alu is
    generic (WIDTH : integer := 32);
    Port ( a       : in  std_logic_vector (WIDTH-1 downto 0);
           b       : in  std_logic_vector (WIDTH-1 downto 0);
           op      : in  std_logic_vector (1       downto 0);
           grp_sel : in  std_logic_vector (1       downto 0);
           ci      : in  std_logic;
           c       : out std_logic_vector (WIDTH-1 downto 0);
           ao      : out std_logic_vector (WIDTH-1 downto 0);
           co      : out std_logic;
           nf      : out std_logic;
           zf      : out std_logic;
           ov      : out std_logic);
end alu;

architecture Behavioral of alu is
    component addsub
        generic (WIDTH : integer;
                 SLICE : integer);
        port (add   : in    std_logic;
              a     : in    std_logic_vector (WIDTH-1 downto 0);
              b     : in    std_logic_vector (WIDTH-1 downto 0);
              ci    : in    std_logic;
              c     : out   std_logic_vector (WIDTH-1 downto 0);
              co    : out   std_logic;
              ov    : out   std_logic);
    end component;

    component szextend
        generic (WIDTH      : integer;
                 SLICE      : integer);
        Port ( b    : in  std_logic_vector  (WIDTH-1 downto 0);
               op   : in  std_logic_vector  (1       downto 0);
               c    : out std_logic_vector  (WIDTH-1 downto 0));
    end component;

    component logicop
        generic (WIDTH   : integer;
                 SLICE   : integer);
        Port ( op   : in std_logic_vector (1 downto 0);
               a    : in std_logic_vector (WIDTH-1 downto 0);
               b    : in std_logic_vector (WIDTH-1 downto 0);
               c    : out std_logic_vector(WIDTH-1 downto 0));
    end component;

    signal c_addsub     : std_logic_vector (WIDTH-1 downto 0);
    signal c_szextend   : std_logic_vector (WIDTH-1 downto 0);
    signal c_logicop    : std_logic_vector (WIDTH-1 downto 0);
    signal c_temp       : std_logic_vector (WIDTH-1 downto 0);
begin
    generic_tech: if Technology /= "XILINX" generate
        addsub_inst : addsub
            generic map (WIDTH => WIDTH, SLICE => 1)
            port map (a => a, b => b, ci => ci, add => op(0),
                      c => c_addsub, co => co, ov => ov);

        szextend_inst : szextend
            generic map (WIDTH => WIDTH, SLICE => 0)
            port map (b  => b ,
                      op => op, c  => c_szextend);

        logicop_inst : logicop
            generic map (WIDTH => WIDTH, SLICE => 1)
            port map (a => a, b => b, op => op, c => c_logicop);

        c_temp <= c_addsub       when grp_sel = "00" else
                  c_logicop      when grp_sel = "01" else
                  c_szextend     when grp_sel = "10" else
                  ALLZEROS ;
        zf  <= '1' when (c_temp = CONV_STD_LOGIC_VECTOR(0,WIDTH)) else '0';           
    end generate generic_tech;

    xilinx_tech: if Technology = "XILINX" generate
        signal zftemp       : std_logic;
    
--        attribute RLOC of addsub_inst     : label is rloc_string(0, 1, true, 0, -1);
        attribute RLOC of logicop_inst    : label is rloc_string(0, 7, true, 0, 5);
        attribute RLOC of szextend_inst   : label is rloc_string(0, 0, true, 0, -2);
        attribute RLOC of mux4_1e_inst    : label is rloc_string(0, 4, true, 0, 3);
        attribute RLOC of nor_vector_inst : label is rloc_string(5, 2, true, 3, 0);
    begin
        addsub_inst : addsub
            generic map (WIDTH => WIDTH, SLICE => 1)
            port map (a => a, b => b, ci => ci, add => op(0),
                      c => c_addsub, co => co, ov => ov);

        logicop_inst : logicop
            generic map (WIDTH => WIDTH, SLICE => 1)
            port map (a => a, b => b, op => op, c => c_logicop);
        
        szextend_inst : szextend
            generic map (WIDTH => WIDTH, SLICE => 0)
            port map (b  => b ,
                      op => op, c  => c_szextend);
        
        mux4_1e_inst : MUX4_1E_VECTOR_SS
            generic map (WIDTH => WIDTH)
            port map (S0 => grp_sel(0), S1 => grp_sel(1),
                      V0 => c_addsub,   V1 => c_logicop,
                      V2 => c_szextend, V3 => open,
                      O  => c_temp,     EN => '1');

        nor_vector_inst : NOR_VECTOR
            generic map (WIDTH => WIDTH, SLICE => 0)
            port map (a => c_addsub, o => zftemp);

        zf  <= zftemp;
    end generate xilinx_tech;
    
    ao  <= c_addsub;
    c   <= c_temp;
    nf  <= c_temp(WIDTH-1);

end Behavioral;
