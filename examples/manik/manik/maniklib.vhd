-------------------------------------------------------------------------------
-- Title      : maniklib
-- Project    : MANIK-2
-------------------------------------------------------------------------------
-- File       : maniklib.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-10-28
-- Last update: 2006-10-07
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Contains XILINX specific optimized library
-------------------------------------------------------------------------------
-- Copyright (c) 2002-2005 Niktech Inc
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-10-28  1.0      sandeep	Created
-------------------------------------------------------------------------------


-------------------------------------------------------------------------------
-- NOR_VECTOR : given a vector of a width creates a rlocced set of LUTS to do
-- 		a NOR of all the elements
-------------------------------------------------------------------------------    
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.manikconfig.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

entity NOR_VECTOR is
    generic (WIDTH : integer := 32;
             SLICE : integer := 0);

    port (a : in  std_logic_vector (WIDTH-1 downto 0);
          o : out std_logic);

end NOR_VECTOR;

architecture XILINX_virtex of NOR_VECTOR is
    
    component LUT4_L
        generic(INIT : bit_vector);
        port (LO : out STD_ULOGIC;
              I0 : in  STD_ULOGIC; I1 : in STD_ULOGIC;
              I2 : in  STD_ULOGIC; I3 : in STD_ULOGIC);
    end component;

    component MUXCY
        port(O  : out STD_ULOGIC; CI : in STD_ULOGIC;
             DI : in  STD_ULOGIC; S  : in STD_ULOGIC);
    end component;

    signal stemp : std_logic_vector ((WIDTH/4) downto 0);
    signal ctemp : std_logic_vector ((WIDTH/4) downto 0);
    signal otemp : std_logic;
    signal rtemp : std_logic;

-------------------------------------------------------------------------------
--                            Input mapping for LUTs
-------------------------------------------------------------------------------
--              I0      -> a(i)
--              I1      -> a(i+1)
--              I2      -> a(i+2)
--              I3      -> a(i+3)
--              O       -> stemp(i)
-------------------------------------------------------------------------------
--                              Truth table for LUT
-------------------------------------------------------------------------------
--      a(i)    a(i+1)  a(i+2)  a(i+3)                  O -> output
-------------------------------------------------------------------------------
--      0       0       0       0                       INIT[0] -> 1
--      X       X       X       X
--                                       INIT[2,3,4,5,6,7,8,9,A,B,C,D,E,F] -> 0        
-------------------------------------------------------------------------------
--      INIT -> X'0001'
-------------------------------------------------------------------------------        
    constant wlimit : integer := (WIDTH/4) - 1;
    constant remain : integer := WIDTH mod 4;
    attribute BEL : string;
begin  -- XILINX_virtex


    nor_inst: for i in 0 to wlimit generate
        constant row  : integer := ((wlimit/2)-i/2-1);
        constant base : natural := i*4;
        attribute RLOC of norlut_inst : label is rloc_string(row,0,SLICE,(wlimit/2));
        attribute INIT of norlut_inst : label is "0001";
        attribute BEL  of norlut_inst : label is bel_string(i mod 2);
    begin
        norlut_inst : LUT4_L
            generic map (INIT => X"0001")
            port map (I0 => a(base),   I1 => a(base+1),
                      I2 => a(base+2), I3 => a(base+3), LO => stemp(i));
        
        normuxcy_wlimit: if i = wlimit generate
            attribute RLOC of normuxcy_inst : label is rloc_string(row,0,SLICE,(wlimit/2));
        begin
            normuxcy_inst : MUXCY
                port map (S  => stemp(i), O => otemp,
                          DI => '0',     CI => ctemp(i-1));
        end generate normuxcy_wlimit;

        normuxcy_0: if i = 0 generate
            attribute RLOC of normuxcy_inst : label is rloc_string(row,0,SLICE,(wlimit/2));
        begin
            normuxcy_inst : MUXCY
                port map (S  => stemp(i), O => ctemp(i),
                          DI => '0',     CI => '1');
        end generate normuxcy_0;

        normuxcy_i: if i > 0 and i < wlimit generate
            attribute RLOC of normuxcy_inst : label is rloc_string(row,0,SLICE,(wlimit/2));
        begin
            normuxcy_inst : MUXCY
                port map (S  => stemp(i), O => ctemp(i),
                          DI => '0',     CI => ctemp(i-1));
        end generate normuxcy_i;

    end generate nor_inst;

    remain_0: if remain = 0 generate
        rtemp <= otemp;
    end generate remain_0;
    
    remain_1: if remain = 1 generate
        signal rltemp : std_logic;
        attribute RLOC of RLUT_1 : label is rloc_string(-1,0, SLICE,(wlimit/2));
        attribute INIT of RLUT_1 : label is "0001";
        attribute BEL  of RLUT_1 : label is bel_string((wlimit+1) mod 2);
        attribute RLOC of RMCY_1 : label is rloc_string(-1,0, SLICE,(wlimit/2));
    begin
        RLUT_1: LUT4_L
            generic map (INIT => X"0001")
            port map (LO => rltemp,
                      I0 => '0', I1 => '0',
                      I2 => '0', I3 => a((wlimit+1)*4));
        RMCY_1: MUXCY
            port map (O  => rtemp, CI => otemp,
                      DI => '0',   S  => rltemp);
    end generate remain_1;
    
    remain_2: if remain = 2 generate
        signal rltemp : std_logic;
        attribute RLOC of RLUT_2 : label is rloc_string(-1,0, SLICE,(wlimit/2));
        attribute INIT of RLUT_2 : label is "0001";
        attribute BEL  of RLUT_2 : label is bel_string((wlimit+1) mod 2);
        attribute RLOC of RMCY_2 : label is rloc_string(-1,0, SLICE,(wlimit/2));
    begin
        RLUT_2: LUT4_L
            generic map (INIT => X"0001")
            port map (LO => rltemp,
                      I0 => '0', I1 => '0',
                      I2 => a(((wlimit+1)*4)+1),
                      I3 => a(((wlimit+1)*4)));
        RMCY_2: MUXCY
            port map (O  => rtemp, CI => otemp,
                      DI => '0',   S  => rltemp);
    end generate remain_2;

    remain_3: if remain = 3 generate
        signal rltemp : std_logic;
        attribute RLOC of RLUT_3 : label is rloc_string(-1,0, SLICE,(wlimit/2));
        attribute INIT of RLUT_3 : label is "0001";
        attribute BEL  of RLUT_3 : label is bel_string((wlimit+1) mod 2);
        attribute RLOC of RMCY_3 : label is rloc_string(-1,0, SLICE,(wlimit/2));
    begin
        RLUT_3: LUT4_L
            generic map (INIT => X"0001")
            port map (LO => rltemp,
                      I0 => '0',
                      I1 => a(((wlimit+1)*4)+2),
                      I2 => a(((wlimit+1)*4)+1),
                      I3 => a((wlimit+1)*4));
        RMCY_3: MUXCY
            port map (O  => rtemp, CI => otemp,
                      DI => '0',   S  => rltemp);
    end generate remain_3;

    o <= rtemp;
end XILINX_virtex;


-------------------------------------------------------------------------------
-- MUX4_1E_VECTOR - creates a rlocced 4-1 Multiplexer.
-------------------------------------------------------------------------------    
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.manikconfig.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

-------------------------------------------------------------------------------
--                              Structure of M4_1E                           --                      
-------------------------------------------------------------------------------
--        +------+
--   V0 --|      |
--   V1 --| LUT0 |  LO
--   S0 --|      |--------+    ____
--    E --|      |        |   |    \
--        +------+        +---|     \
--                            |      \
--   S1 ----------------------+MUXF5 +------> O
--        +------+            |      /
--   V2 --|      |        +---|     /
--   V3 --| LUT1 |  LO    |   |____/
--   S0 --|      |--------+
--    E --|      |        
--        +------+        
-------------------------------------------------------------------------------
--      S0      S1      ->      O
--      0       0               V0
--      0       1               V1
--      1       0               V2
--      1       1               V3
--      EN = 0                  all 0s
-------------------------------------------------------------------------------    
entity MUX4_1E_VECTOR is
    generic (WIDTH  : integer := 32;
             ROWDIV : integer := 2;
             SLICE  : integer := 1);
    port (S0 : in  std_logic; S1 : in  std_logic; EN : in  std_logic;
          V0 : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
          V1 : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
          V2 : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
          V3 : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
          O  : out std_logic_vector (WIDTH-1 downto 0));
end MUX4_1E_VECTOR;

architecture XILINX_virtex of MUX4_1E_VECTOR is
    component LUT4_L
        generic(INIT : bit_vector);
        port(LO : out STD_ULOGIC;
             I0 : in  STD_ULOGIC; I1 : in STD_ULOGIC;
             I2 : in  STD_ULOGIC; I3 : in STD_ULOGIC);
    end component;

    component MUXF5
        port(O  : out STD_ULOGIC; I0 : in STD_ULOGIC;
             I1 : in  STD_ULOGIC; S  : in STD_ULOGIC);
    end component;

    signal gsel_0X      : std_logic_vector (WIDTH-1 downto 0);
    signal gsel_1X      : std_logic_vector (WIDTH-1 downto 0);
    signal otemp        : std_logic_vector (WIDTH-1 downto 0);

-------------------------------------------------------------------------------        
--      Truth table for LUT0 - select V0 if S0=1 else select V1
--      Truth table for LUT1 - select V2 if S0=1 else select V3
-------------------------------------------------------------------------------
--      A       B       S0      E               LO->(output)
-------------------------------------------------------------------------------
--      ENABLE = 0 then output => 0
--      X       X       X       0               INIT[0,2,4,6,8,A,C,E]   -> 0
--      0       X       1       1               INIT[3,7]               -> 0
--      1       X       1       1               INIT[B,F]               -> 1
--      X       0       0       1               INIT[1,9]               -> 0
--      X       1       0       1               INIT[5,D]               -> 1
-------------------------------------------------------------------------------
--      INIT            F E D C - B A 9 8 - 7 6 5 4 - 3 2 1 0
--                      1 0 1 0 - 1 0 0 0 - 0 0 1 0 - 0 0 0 0  --> X'A820'
begin  -- XILINX_virtex

    RD2: if ROWDIV = 2 generate      
        mux4_1e_inst: for i in 0 to (WIDTH-1) generate
            constant row    : integer := (WIDTH/2)-(i/2)-1;
            constant lslice : natural := i mod 2;
            attribute INIT of muxlut0_inst  : label is "A820";
            attribute INIT of muxlut1_inst  : label is "A820";
            attribute RLOC of muxlut0_inst  : label is rloc_string(row,0,lslice,(WIDTH/2));
            attribute RLOC of muxlut1_inst  : label is rloc_string(row,0,lslice,(WIDTH/2));
            attribute RLOC of muxf5_inst    : label is rloc_string(row,0,lslice,(WIDTH/2));
        begin
            muxlut0_inst : LUT4_L
                generic map (INIT => X"A820")
                port map (I0 => EN,    I1 => S0,
                          I2 => V0(i), I3 => V1(i),
                          LO => gsel_0X(i));
            
            muxlut1_inst : LUT4_L
                generic map (INIT => X"A820")
                port map (I0 => EN,    I1 => S0,
                          I2 => V2(i), I3 => V3(i),
                          LO => gsel_1X(i));

            muxf5_inst : MUXF5
                port map (I0 => gsel_0X(i), I1 => gsel_1X(i),
                          S  => S1, O  =>  otemp(i));
            
        end generate mux4_1e_inst;
    end generate RD2;

    RD1: if ROWDIV = 1 generate      
        mux4_1e_inst: for i in 0 to (WIDTH-1) generate
            constant row    : integer := (WIDTH/2)-i-1;
            constant lslice : natural := SLICE;
            attribute INIT of muxlut0_inst  : label is "A820";
            attribute INIT of muxlut1_inst  : label is "A820";
            attribute RLOC of muxlut0_inst  : label is rloc_string(row,0,lslice,(WIDTH/2));
            attribute RLOC of muxlut1_inst  : label is rloc_string(row,0,lslice,(WIDTH/2));
            attribute RLOC of muxf5_inst    : label is rloc_string(row,0,lslice,(WIDTH/2));
        begin
            muxlut0_inst : LUT4_L
                generic map (INIT => X"A820")
                port map (I0 => EN,    I1 => S0,
                          I2 => V0(i), I3 => V1(i),
                          LO => gsel_0X(i));
            
            muxlut1_inst : LUT4_L
                generic map (INIT => X"A820")
                port map (I0 => EN,    I1 => S0,
                          I2 => V2(i), I3 => V3(i),
                          LO => gsel_1X(i));

            muxf5_inst : MUXF5
                port map (I0 => gsel_0X(i), I1 => gsel_1X(i),
                          S  => S1, O  =>  otemp(i));
            
        end generate mux4_1e_inst;
    end generate RD1;
    O <= otemp;
end XILINX_virtex;

-------------------------------------------------------------------------------
-- MUX4_1E_VECTOR_SS - creates a rlocced 4-1 Multiplexer. with split Slice
-------------------------------------------------------------------------------    
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.manikconfig.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

-------------------------------------------------------------------------------
--                              Structure of M4_1E                           --                      
-------------------------------------------------------------------------------
--        +------+
--   V0 --|      |
--   V1 --| LUT0 |  LO
--   S0 --|      |--------+    ____
--    E --|      |        |   |    \
--        +------+        +---|     \
--                            |      \
--   S1 ----------------------+MUXF5 +------> O
--        +------+            |      /
--   V2 --|      |        +---|     /
--   V3 --| LUT1 |  LO    |   |____/
--   S0 --|      |--------+
--    E --|      |        
--        +------+        
-------------------------------------------------------------------------------
--      S0      S1      ->      O
--      0       0               V0
--      0       1               V1
--      1       0               V2
--      1       1               V3
--      EN = 0                  all 0s
-------------------------------------------------------------------------------    
entity MUX4_1E_VECTOR_SS is
    generic (WIDTH      : integer := 32);
    port    (S0         : in  std_logic;
             S1         : in  std_logic;
             EN         : in  std_logic;
             V0         : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
             V1         : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
             V2         : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
             V3         : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
             O          : out std_logic_vector (WIDTH-1 downto 0));
end MUX4_1E_VECTOR_SS;

architecture XILINX_virtex of MUX4_1E_VECTOR_SS is
    component LUT4_L
        generic(INIT : bit_vector);
        port(LO : out STD_ULOGIC;
             I0 : in  STD_ULOGIC; I1 : in STD_ULOGIC;
             I2 : in  STD_ULOGIC; I3 : in STD_ULOGIC);
    end component;

    component MUXF5
        port(O  : out STD_ULOGIC; I0 : in STD_ULOGIC;
             I1 : in  STD_ULOGIC; S  : in STD_ULOGIC);
    end component;

    signal gsel_0X      : std_logic_vector (WIDTH-1 downto 0);
    signal gsel_1X      : std_logic_vector (WIDTH-1 downto 0);
    signal otemp        : std_logic_vector (WIDTH-1 downto 0);

-------------------------------------------------------------------------------        
--      Truth table for LUT0 - select V0 if S0=1 else select V1
--      Truth table for LUT1 - select V2 if S0=1 else select V3
-------------------------------------------------------------------------------
--      A       B       S0      E               LO->(output)
-------------------------------------------------------------------------------
--      ENABLE = 0 then output => 0
--      X       X       X       0               INIT[0,2,4,6,8,A,C,E]   -> 0
--      0       X       1       1               INIT[3,7]               -> 0
--      1       X       1       1               INIT[B,F]               -> 1
--      X       0       0       1               INIT[1,9]               -> 0
--      X       1       0       1               INIT[5,D]               -> 1
-------------------------------------------------------------------------------
--      INIT            F E D C - B A 9 8 - 7 6 5 4 - 3 2 1 0
--                      1 0 1 0 - 1 0 0 0 - 0 0 1 0 - 0 0 0 0  --> X'A820'
begin  -- XILINX_virtex

    V2_F: if FPGA_Family /= "Virtex2" generate
        mux4_1e_inst: for i in 0 to (WIDTH-1) generate
            constant row    : integer := (WIDTH/2)-(i/2)-1;
            constant slice  : natural := i mod 2;
            constant col    : integer := i mod 2;
            attribute INIT of muxlut0_inst  : label is "EC20";
            attribute INIT of muxlut1_inst  : label is "EC20";
            attribute RLOC of muxlut0_inst  : label is rloc_string(row,col,slice,(WIDTH/2));
            attribute RLOC of muxlut1_inst  : label is rloc_string(row,col,slice,(WIDTH/2));
            attribute RLOC of muxf5_inst    : label is rloc_string(row,col,slice,(WIDTH/2));
        begin
            muxlut0_inst : LUT4_L
                generic map (INIT => X"A820")
                port map (I0 => EN,    I1 => S0,
                          I2 => V0(i), I3 => V1(i),
                          LO => gsel_0X(i));
            
            muxlut1_inst : LUT4_L
                generic map (INIT => X"A820")
                port map (I0 => EN,    I1 => S0,
                          I2 => V2(i), I3 => V3(i),
                          LO => gsel_1X(i));

            muxf5_inst : MUXF5
                port map (I0 => gsel_0X(i), I1 => gsel_1X(i),
                          S  => S1, O  =>  otemp(i));
            
        end generate mux4_1e_inst;        
    end generate V2_F;

    V2_T: if FPGA_Family = "Virtex2" generate        
        mux4_1e_inst: for i in 0 to (WIDTH-1) generate
            constant row    : integer := (WIDTH/2)-(i/2)-1;
            constant slice  : natural := i mod 2;
            attribute INIT of muxlut0_inst  : label is "EC20";
            attribute INIT of muxlut1_inst  : label is "EC20";
            attribute RLOC of muxlut0_inst  : label is rloc_string(row,0,slice,(WIDTH/2));
            attribute RLOC of muxlut1_inst  : label is rloc_string(row,0,slice,(WIDTH/2));
            attribute RLOC of muxf5_inst    : label is rloc_string(row,0,slice,(WIDTH/2));
        begin
            muxlut0_inst : LUT4_L
                generic map (INIT => X"A820")
                port map (I0 => EN,    I1 => S0,
                          I2 => V0(i), I3 => V1(i),
                          LO => gsel_0X(i));
            
            muxlut1_inst : LUT4_L
                generic map (INIT => X"A820")
                port map (I0 => EN,    I1 => S0,
                          I2 => V2(i), I3 => V3(i),
                          LO => gsel_1X(i));

            muxf5_inst : MUXF5
                port map (I0 => gsel_0X(i), I1 => gsel_1X(i),
                          S  => S1, O  =>  otemp(i));
            
        end generate mux4_1e_inst;        
    end generate V2_T;
    O <= otemp;
end XILINX_virtex;

-------------------------------------------------------------------------------
-- MUX4DO_1E_VECTOR - creates a rlocced 4-1 Multiplexer.
-------------------------------------------------------------------------------    
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.manikconfig.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

-------------------------------------------------------------------------------
--                              Structure of M4_1E                           --                      
-------------------------------------------------------------------------------
--        +------+
--   V0 --|      |
--   V1 --| LUT0 |  LO
--   S0 --|      |--------+    ____
--    E --|      |        |   |    \
--        +------+        +---|     \
--                            |      \
--   S1 ----------------------+MUXF5 +------> O
--        +------+            |      /
--   V2 --|      |        +---|     /
--   V3 --| LUT1 |  LO    |   |____/
--   S0 --|      |--------+
--    E --|      |--------------------------> DO        
--        +------+        
-------------------------------------------------------------------------------
--      S0      S1      ->      O
--      0       0               V0
--      0       1               V1
--      1       0               V2
--      1       1               V3
--      EN = 0                  all 0s
-------------------------------------------------------------------------------    
entity MUX4DO_1E_VECTOR is
    generic (WIDTH : integer := 32;
             ADJ   : integer := 0);
    port (S0   : in  std_logic;
          S0_D : in  std_logic;
          S1   : in  std_logic;
          EN   : in  std_logic;
          V0   : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
          V1   : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
          V2   : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
          V3   : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
          O    : out std_logic_vector (WIDTH-1 downto 0);
          DO   : out std_logic_vector (WIDTH-1 downto 0));
end MUX4DO_1E_VECTOR;

architecture XILINX_virtex of MUX4DO_1E_VECTOR is
    component LUT4_L
        generic(INIT : bit_vector);
        port(LO : out STD_ULOGIC;
             I0 : in  STD_ULOGIC; I1 : in STD_ULOGIC;
             I2 : in  STD_ULOGIC; I3 : in STD_ULOGIC);
    end component;

    component LUT4_D
        generic(INIT : bit_vector);
        port(LO : out STD_ULOGIC; O  : out STD_ULOGIC;
             I0 : in  STD_ULOGIC; I1 : in  STD_ULOGIC;
             I2 : in  STD_ULOGIC; I3 : in  STD_ULOGIC);
    end component;

    component MUXCY
        port(O  : out STD_ULOGIC; CI : in STD_ULOGIC;
             DI : in  STD_ULOGIC; S  : in STD_ULOGIC);
    end component;

    component MUXF5
        port(O  : out STD_ULOGIC; I0 : in STD_ULOGIC;
             I1 : in  STD_ULOGIC; S  : in STD_ULOGIC);
    end component;

    component XORCY
        port(O  : out STD_ULOGIC; CI : in  STD_ULOGIC; LI : in  STD_ULOGIC);
    end component;
    
    signal gsel_0X      : std_logic_vector (WIDTH-1 downto 0);
    signal gsel_1X      : std_logic_vector (WIDTH-1 downto 0);
    signal otemp        : std_logic_vector (WIDTH-1 downto 0);    
    signal dotemp       : std_logic_vector (WIDTH-1 downto 0);    
    
-------------------------------------------------------------------------------        
--      Truth table for LUT0 - select V0 if S0=1 else select V1
--      Truth table for LUT1 - select V2 if S0=1 else select V3
-------------------------------------------------------------------------------
--      A       B       S0      E               LO->(output)
-------------------------------------------------------------------------------
--      ENABLE = 0 then output => 0
--      X       X       X       0               INIT[0,2,4,6,8,A,C,E]   -> 0
--      0       X       1       1               INIT[3,7]               -> 0
--      1       X       1       1               INIT[B,F]               -> 1
--      X       0       0       1               INIT[1,9]               -> 0
--      X       1       0       1               INIT[5,D]               -> 1
-------------------------------------------------------------------------------
--      INIT            F E D C - B A 9 8 - 7 6 5 4 - 3 2 1 0
--                      1 0 1 0 - 1 0 0 0 - 0 0 1 0 - 0 0 0 0  --> X'A820'
    attribute BEL : string;
begin  -- XILINX_virtex

    
    mux4_1e_inst: for i in 0 to (WIDTH-1) generate
        constant row    : integer := (WIDTH/2)-(i/2)-1;
        constant slice  : natural := i mod 2;
        constant v2adj : integer := boolnot(i mod 2)*ADJ;
        attribute INIT of muxlut0_inst  : label is "A820";
        attribute INIT of muxlut1_inst  : label is "A820";
        attribute RLOC of muxlut0_inst  : label is rloc_string(row,0,slice,(WIDTH/2),true,0,v2adj);
        attribute RLOC of muxlut1_inst  : label is rloc_string(row,0,slice,(WIDTH/2),true,0,v2adj);
        attribute BEL  of muxlut0_inst  : label is bel_string(0);
        attribute BEL  of muxlut1_inst  : label is bel_string(1);
        attribute RLOC of muxf5_inst    : label is rloc_string(row,0,slice,(WIDTH/2),true,0,v2adj);
    begin
        muxlut0_inst : LUT4_L
            generic map (INIT => X"A820")
            port map (I0 => EN,    I1 => S0,
                      I2 => V0(i), I3 => V1(i),
                      LO => gsel_0X(i));
        
        muxlut1_inst : LUT4_D
            generic map (INIT => X"A820")
            port map (I0 => EN,    I1 => S0_D,
                      I2 => V2(i), I3 => V3(i),
                      LO => gsel_1X(i), O => dotemp(i));

        muxf5_inst : MUXF5
            port map (I0 => gsel_1X(i), I1 => gsel_0X(i),
                      S  => S1, O =>  otemp(i));

    end generate mux4_1e_inst;

    O  <= otemp;
    DO <= dotemp;
end XILINX_virtex;


-------------------------------------------------------------------------------
-- ADD_MUX_VECTOR - Multiplex b1 and b2 - selected with "sel" and add to "a"
--                  add b2 when sel=1 else b1
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.manikconfig.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

entity ADD_MUX_VECTOR is
        
    generic (WIDTH : integer := 32;
             SLICE : integer := 0);
    
    port (a   : in  std_logic_vector (WIDTH-1 downto 0);
          b1  : in  std_logic_vector (WIDTH-1 downto 0);
          b2  : in  std_logic_vector (WIDTH-1 downto 0);
          sel : in  std_logic;
          c   : out std_logic_vector (WIDTH-1 downto 0));
    
end ADD_MUX_VECTOR;    

architecture XILINX_virtex of ADD_MUX_VECTOR is

    component LUT4_L
        generic(INIT    :  bit_vector);
        port   (LO      :  out   STD_ULOGIC;
                I0      :  in    STD_ULOGIC;
                I1      :  in    STD_ULOGIC;
                I2      :  in    STD_ULOGIC;
                I3      :  in    STD_ULOGIC);
    end component;

    component MUXCY_L
        port(LO         :  out   STD_ULOGIC;
             CI         :  in    STD_ULOGIC;
             DI         :  in    STD_ULOGIC;
             S          :  in    STD_ULOGIC);
    end component;

    component XORCY
        port(O          :  out   STD_ULOGIC;
             CI         :  in    STD_ULOGIC;      
             LI         :  in    STD_ULOGIC);
    end component;

    signal stemp        : std_logic_vector (WIDTH-1 downto 0);
    signal ctemp        : std_logic_vector (WIDTH   downto 0);
    
-------------------------------------------------------------------------------
--                            Input mapping for LUTs
-- The basic function of the LUT here is to generate the following function
--          if I3 = '0' LO = I0 ^ I1 else LO = I0 ^ I2
-------------------------------------------------------------------------------
--              I0      -> a (i)
--              I1      -> b1(i)
--              I2      -> b2(i)
--              I3      -> sel
--              LO      -> stemp(i)
-------------------------------------------------------------------------------
--      sel    b2(i)  b1(i)    a(i)                     LO -> output
-------------------------------------------------------------------------------
--      0       X       0       0                       INIT[0,4] -> 0
--      0       X       0       1                       INIT[1,5] -> 1
--      0       X       1       0                       INIT[2,6] -> 1
--      0       X       1       1                       INIT[3,7] -> 0
--      1       0       X       0                       INIT[8,A] -> 0
--      1       0       X       1                       INIT[9,B] -> 1
--      1       1       X       0                       INIT[C,E] -> 1
--      1       1       X       1                       INIT[D,F] -> 0
-------------------------------------------------------------------------------
--      INIT            F E D C - B A 9 8 - 7 6 5 4 - 3 2 1 0
--                      0 1 0 1   1 0 1 0   0 1 1 0   0 1 1 0 --> X'5A66'
-------------------------------------------------------------------------------
    attribute BEL : string;
begin  -- XILINX_virtex
    ctemp(0) <= '0';
    
    add_mux_inst: for i in 0 to WIDTH-1 generate
        constant row : integer := (WIDTH/2) - i/2 - 1;
        attribute RLOC of am_lut4_inst : label is rloc_string(row,0,SLICE,WIDTH/2);
        attribute INIT of am_lut4_inst : label is "5A66";
        attribute BEL  of am_lut4_inst : label is bel_string(i);
        attribute RLOC of am_mxcy_inst : label is rloc_string(row,0,SLICE,WIDTH/2);
        attribute RLOC of am_xorc_inst : label is rloc_string(row,0,SLICE,WIDTH/2);
    begin
        am_lut4_inst : LUT4_L
            generic map (INIT => X"5A66")
            port map (I0 => a(i), I1 => b1(i),
                      I2 => b2(i), I3 => sel,
                      LO => stemp(i));

        am_xorc_inst : XORCY
            port map (CI => ctemp(i),
                      LI => stemp(i),
                      O  => c(i));
        
        am_mxcy_inst : MUXCY_L
            port map (LO => ctemp(i+1),
                      S  => stemp(i),
                      DI => a(i),
                      CI => ctemp(i));
    end generate add_mux_inst;

end XILINX_virtex;

-------------------------------------------------------------------------------
-- CMPEQ_4C - passthru CI if two 4bit vectors are equal
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

entity CMPEQ_4C is
    generic (SLICE : integer := 0);
    port (A : in  std_logic_vector (3 downto 0);
          B : in  std_logic_vector (3 downto 0);
          C : in  std_logic;
          O : out std_logic);
end CMPEQ_4C;

architecture XILINX_virtex of CMPEQ_4C is
    
    component LUT4_L
        generic(INIT : bit_vector);
        port(LO : out STD_ULOGIC;
             I0 : in  STD_ULOGIC; I1 : in  STD_ULOGIC;
             I2 : in  STD_ULOGIC; I3 : in  STD_ULOGIC);
    end component;

    component MUXCY_L
        port(LO : out STD_ULOGIC; CI : in  STD_ULOGIC;
             DI : in  STD_ULOGIC; S  : in  STD_ULOGIC);
    end component;

    component MUXCY
        port(O  : out STD_ULOGIC; CI : in  STD_ULOGIC;
             DI : in  STD_ULOGIC; S  : in  STD_ULOGIC);
    end component;
-------------------------------------------------------------------------------
-- Input mapping for LUT_A
--              I0  =>  A(0)
--              I1  =>  B(0)
--              I2  =>  A(1)
--              I3  =>  B(1)
--              O   =>  DI (of MUXCY_LA)
-- Input mapping for LUT_B
--              I0  =>  A(2)
--              I1  =>  B(2)
--              I2  =>  A(3)
--              I3  =>  B(3)
--              O   =>  DI (of MUXCY_LB)
-------------------------------------------------------------------------------
--      Truth table for LUT
-------------------------------------------------------------------------------
--      I3      I2      I1      I0                      O
-------------------------------------------------------------------------------
--      0       0       0       0                       INIT[0] => 1
--      0       0       1       1                       INIT[3] => 1
--      1       1       0       0                       INIT[C] => 1
--      1       1       1       1                       INIT[F] => 1
-------------------------------------------------------------------------------
--      INIT            F E D C - B A 9 8 - 7 6 5 4 - 3 2 1 0
--                      1 0 0 1 - 0 0 0 0 - 0 0 0 0 - 1 0 0 1  - X'9009'
-------------------------------------------------------------------------------

    signal CO : std_logic;
    signal S0 : std_logic;
    signal S1 : std_logic;
    
    attribute BEL : string;

    attribute INIT of CMPEQ_4_LUTA   : label is "9009";
    attribute INIT of CMPEQ_4_LUTB   : label is "9009";
    attribute RLOC of CMPEQ_4_LUTA   : label is rloc_string(0, 0, SLICE);
    attribute RLOC of CMPEQ_4_LUTB   : label is rloc_string(0, 0, SLICE);
    attribute RLOC of CMPEQ_4_MUXCYA : label is rloc_string(0, 0, SLICE);
    attribute RLOC of CMPEQ_4_MUXCYB : label is rloc_string(0, 0, SLICE);
    attribute BEL  of CMPEQ_4_LUTA   : label is bel_string(0);
    attribute BEL  of CMPEQ_4_LUTB   : label is bel_string(1);
    
begin  -- XILINX_virtex

    CMPEQ_4_LUTA : LUT4_L
        generic map (INIT => X"9009")
        port map (I0 => A(0), I1 => B(0), I2 => A(1), I3 => B(1), LO => S0);
    
    CMPEQ_4_LUTB : LUT4_L
        generic map (INIT => X"9009")
        port map (I0 => A(2), I1 => B(2), I2 => A(3), I3 => B(3), LO => S1);

    CMPEQ_4_MUXCYA : MUXCY_L
        port map (DI => '0', CI => C, S  => S0, LO => CO);

    CMPEQ_4_MUXCYB : MUXCY
        port map (DI => '0', CI => CO, S  => S1, O  => O);
    
end XILINX_virtex;

-------------------------------------------------------------------------------
-- CMPEQ_4ALT -  '1' if two 4bit vectors are equal does not use carry chain, the
--               result is anded with two other signals C & D
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

entity CMPEQ_4ALT is
    generic (SPLIT_SLICE : boolean := False;
             AND_CD      : boolean := True);
    port (A : in  std_logic_vector (3 downto 0);
          B : in  std_logic_vector (3 downto 0);
          C : in  std_logic := '1';
          D : in  std_logic := '1';
          O : out std_logic);
end CMPEQ_4ALT;

architecture XILINX_virtex of CMPEQ_4ALT is

    component LUT4
        generic(INIT    :  bit_vector);
        port(O          :  out   STD_ULOGIC;
             I0         :  in    STD_ULOGIC;
             I1         :  in    STD_ULOGIC;
             I2         :  in    STD_ULOGIC;
             I3         :  in    STD_ULOGIC);
    end component;

-------------------------------------------------------------------------------
-- Input mapping for LUT_A
--              I0  =>  A(0)
--              I1  =>  B(0)
--              I2  =>  A(1)
--              I3  =>  B(1)
--              O   =>  OA
-- Input mapping for LUT_B
--              I0  =>  A(2)
--              I1  =>  B(2)
--              I2  =>  A(3)
--              I3  =>  B(3)
--              O   =>  OB
-------------------------------------------------------------------------------
--      Truth table for LUT
-------------------------------------------------------------------------------
--      I3      I2      I1      I0                      O
-------------------------------------------------------------------------------
--      0       0       0       0                       INIT[0] => 1
--      0       0       1       1                       INIT[3] => 1
--      1       1       0       0                       INIT[C] => 1
--      1       1       1       1                       INIT[F] => 1
-------------------------------------------------------------------------------
--      INIT            F E D C - B A 9 8 - 7 6 5 4 - 3 2 1 0
--                      1 0 0 1 - 0 0 0 0 - 0 0 0 0 - 1 0 0 1  - X'9009'
-------------------------------------------------------------------------------

    signal OA : std_logic;
    signal OB : std_logic;
    attribute BEL : string;

begin  -- XILINX_virtex

    NSS_INST: if SPLIT_SLICE = false generate
        attribute INIT of LUT4_A : label is "9009";
        attribute RLOC of LUT4_A : label is rloc_string(0,0,1);
        attribute BEL  of LUT4_A : label is bel_string(1);

        attribute INIT of LUT4_B : label is "9009";
        attribute RLOC of LUT4_B : label is rloc_string(0,0,1);
        attribute BEL  of LUT4_B : label is bel_string(0);        
    begin
        LUT4_A: LUT4
            generic map (INIT => X"9009")
            port map (O  => OA,
                      I0 => A(0), I1 => B(0),
                      I2 => A(1), I3 => B(1));
        LUT4_B: LUT4
            generic map (INIT => X"9009")
            port map (O  => OB,
                      I0 => A(2), I1 => B(2),
                      I2 => A(3), I3 => B(3));

        CANDD: if AND_CD = True generate
            attribute INIT of AND_LUT : label is "8000";
            attribute RLOC of AND_LUT : label is rloc_string(0,0,0);
            attribute BEL  of AND_LUT : label is bel_string(0);
        begin
            AND_LUT: LUT4
                generic map (INIT => X"8000")
                port map (O => O, I0 => OA, I1 => OB, I2 => C, I3 => D);          
        end generate CANDD;
        
        CORD: if AND_CD = False generate
            attribute INIT of OR_LUT : label is "8880";
            attribute RLOC of OR_LUT : label is rloc_string(0,0,0);
            attribute BEL  of OR_LUT : label is bel_string(0);
        begin
            OR_LUT: LUT4
                generic map (INIT => X"8880")
                port map (O => O, I0 => OA, I1 => OB, I2 => C, I3 => D);          
        end generate CORD;
        
    end generate NSS_INST;

    SS_INST: if SPLIT_SLICE = true generate
        attribute INIT of LUT4_A : label is "9009";
        attribute RLOC of LUT4_A : label is rloc_string(0,0,1);
        attribute BEL  of LUT4_A : label is bel_string(0);

        attribute INIT of LUT4_B : label is "9009";
        attribute RLOC of LUT4_B : label is rloc_string(1,0,1,1);
        attribute BEL  of LUT4_B : label is bel_string(1);
        
    begin
        LUT4_A: LUT4
            generic map (INIT => X"9009")
            port map (O  => OA,
                      I0 => A(0), I1 => B(0),
                      I2 => A(1), I3 => B(1));
        LUT4_B: LUT4
            generic map (INIT => X"9009")
            port map (O  => OB,
                      I0 => A(2), I1 => B(2),
                      I2 => A(3), I3 => B(3));
        CANDD: if AND_CD = true generate
            attribute INIT of AND_LUT : label is "8000";
            attribute RLOC of AND_LUT : label is rloc_string(1,0,0,1);
            attribute BEL  of AND_LUT : label is bel_string(1);
        begin
            AND_LUT: LUT4
                generic map (INIT => X"8000")
                port map (O => O, I0 => OA, I1 => OB, I2 => C, I3 => D);            
        end generate CANDD;
        CORD: if AND_CD = False generate
            attribute INIT of OR_LUT : label is "8880";
            attribute RLOC of OR_LUT : label is rloc_string(1,0,0,1);
            attribute BEL  of OR_LUT : label is bel_string(1);
        begin
            OR_LUT: LUT4
                generic map (INIT => X"8880")
                port map (O => O, I0 => OA, I1 => OB, I2 => C, I3 => D);            
        end generate CORD;
    end generate SS_INST;
    
end XILINX_virtex;

-------------------------------------------------------------------------------
-- SPECMUX_NOTOR_VECTOR - Select  B when ((not SEL(0)) or SEL(1)) else A
-------------------------------------------------------------------------------    
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

entity SPECMUX_NOTOR_VECTOR is
    
    generic (WIDTH : integer := 32;
             SLICE : integer := 0);

    port (SEL0 : in  std_logic;
          SEL1 : in  std_logic;
          A    : in  std_logic_vector (WIDTH-1 downto 0);
          B    : in  std_logic_vector (WIDTH-1 downto 0);
          O    : out std_logic_vector (WIDTH-1 downto 0));

end SPECMUX_NOTOR_VECTOR;

architecture XILINX_virtex of SPECMUX_NOTOR_VECTOR is

    component LUT4
        generic(INIT    :  bit_vector);
        port(O          :  out   STD_ULOGIC;
             I0         :  in    STD_ULOGIC;
             I1         :  in    STD_ULOGIC;
             I2         :  in    STD_ULOGIC;
             I3         :  in    STD_ULOGIC);
    end component;
    
-------------------------------------------------------------------------------
-- Input mapping for LUT
--              I0  =>  SEL0
--              I1  =>  SEL1
--              I2  =>  B
--              I3  =>  A
--              O   =>  O(i)
-------------------------------------------------------------------------------
--      Truth table for LUT
-------------------------------------------------------------------------------
--
--      I3(B)   I2(A)   I1(S1)  I0(S0)                  O -> O
-------------------------------------------------------------------------------
--      X       0       0       0               INIT[0,8] -> 0
--      X       1       0       0               INIT[4,C] -> 1
--      0       X       0       1               INIT[1,5] -> 0
--      1       X       0       1               INIT[9,D] -> 1
--      0       X       1       0               INIT[2,6] -> 0
--      1       X       1       0               INIT[A,E] -> 1
--      0       X       1       1               INIT[3,7] -> 0
--      1       X       1       1               INIT[B,F] -> 1
-------------------------------------------------------------------------------
--      INIT            F E D C - B A 9 8 - 7 6 5 4 - 3 2 1 0
--                      1 1 1 1 - 1 1 1 0 - 0 0 0 1 - 0 0 0 0 - X"FE10"
    attribute BEL : string;
begin  -- XILINX_virtex

    SPECMUX_NOTOR: for i in 0 to WIDTH-1 generate
        constant row    : integer := (WIDTH/2)-(i/2)-1;
        attribute RLOC of SPECMUX_NOTOR_LUT : label is rloc_string(row,0,SLICE);
        attribute BEL  of SPECMUX_NOTOR_LUT : label is bel_string(i);
        attribute INIT of SPECMUX_NOTOR_LUT : label is "FE10";
    begin
        SPECMUX_NOTOR_LUT : LUT4
            generic map (INIT => X"FE10")
            port map (I0 => SEL0, I1 => SEL1,
                      I2 => A(i), I3 => B(i),                      
                      O  => O(i));
    end generate SPECMUX_NOTOR;

end XILINX_virtex;

-------------------------------------------------------------------------------
-- MUX_ADD_VECTOR - when ADD = '1' add O = A + B else O = C
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

entity MUX_ADD_VECTOR is
    
    generic (WIDTH : integer := 32;
             SLICE : integer := 1);

    port (A   : in  std_logic_vector (WIDTH-1 downto 0);
          B   : in  std_logic_vector (WIDTH-1 downto 0);
          C   : in  std_logic_vector (WIDTH-1 downto 0);
          ADD : in  std_logic;
          O   : out std_logic_vector (WIDTH-1 downto 0));

end MUX_ADD_VECTOR;

architecture XILINX_virtex of MUX_ADD_VECTOR is
    component LUT4_L
        generic(INIT : bit_vector);
        port   (LO   : out STD_ULOGIC;
                I0   : in  STD_ULOGIC; I1 : in STD_ULOGIC;
                I2   : in  STD_ULOGIC; I3 : in STD_ULOGIC);
    end component;

    component MUXCY_L
        port(LO : out STD_ULOGIC; CI : in STD_ULOGIC;
             DI : in  STD_ULOGIC; S  : in STD_ULOGIC);
    end component;

    component XORCY
        port(O  : out STD_ULOGIC;
             CI : in  STD_ULOGIC; LI : in  STD_ULOGIC);
    end component;

    component MULT_AND
        port(LO : out STD_ULOGIC;
             I0 : in  STD_ULOGIC; I1 : in  STD_ULOGIC);
    end component;
    
-------------------------------------------------------------------------------
--                            Input mapping for LUTs
-------------------------------------------------------------------------------
--              I0      -> A (i)
--              I1      -> ADD
--              I2      -> B(i)
--              I3      -> C(i)        
--              LO      -> otemp(i)
-------------------------------------------------------------------------------
--      C(i)    B(i)    ADD     A(i)            LO -> Output
-------------------------------------------------------------------------------
--      X       0       1       0               INIT[2,A] -> 0
--      X       0       1       1               INIT[3,B] -> 1
--      X       1       1       0               INIT[6,E] -> 1
--      X       1       1       1               INIT[7,F] -> 0
--      0       X       0       X               INIT[0,1,4,5] -> 0
--      1       X       0       X               INIT[8,9,C,D] -> 1
-------------------------------------------------------------------------------        
--      INIT            F E D C - B A 9 8 - 7 6 5 4 - 3 2 1 0
--                      0 1 1 1   1 0 1 1   0 1 0 0   1 0 0 0   --> X"7B48"
-------------------------------------------------------------------------------

    signal otemp : std_logic_vector (WIDTH-1 downto 0);
    signal mando : std_logic_vector (WIDTH-1 downto 0);
    signal carry : std_logic_vector (WIDTH   downto 0);
    
    attribute BEL : string;
    
begin  -- XILINX_virtex

    carry(0) <= '0';
    
    mux_add: for i in 0 to WIDTH-1 generate
        constant row : integer := (WIDTH/2) - i/2 - 1;
        attribute RLOC of mux_add_mxcy_inst : label is rloc_string(row,0,SLICE,WIDTH/2);
        attribute RLOC of mux_add_xorc_inst : label is rloc_string(row,0,SLICE,WIDTH/2);
        attribute RLOC of mux_add_mand_inst : label is rloc_string(row,0,SLICE,WIDTH/2);
        attribute RLOC of mux_add_lut4_inst : label is rloc_string(row,0,SLICE,WIDTH/2);
        attribute BEL  of mux_add_lut4_inst : label is bel_string(i);
        attribute INIT of mux_add_lut4_inst : label is "7B48";
    begin

        mux_add_lut4_inst : LUT4_L
            generic map (INIT => X"7B48")
            port map (I0 => A(i), I1 => ADD,
                      I2 => B(i), I3 => C(i),
                      LO => otemp(i));

        mux_add_mand_inst : MULT_AND
            port map (I0 => A(i), I1 => ADD, LO => mando(i));

        mux_add_mxcy_inst : MUXCY_L
            port map (DI => mando(i), CI => carry(i),
                      S  => otemp(i), LO => carry(i+1));

        mux_add_xorc_inst : XORCY
            port map (CI => carry(i), LI => otemp(i), O  => O(i));
    end generate mux_add;

end XILINX_virtex;


-------------------------------------------------------------------------------
-- FD_ALT_VECTOR_SS Vector of FDE (FF) . Placed one per slice (X or Y),
--                  Alternate columns
-------------------------------------------------------------------------------    
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.manikconfig.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

entity FD_ALT_VECTOR_SS is
    
    generic (WIDTH : integer := 32;
             XORY  : integer := 0);

    port (CLK : in  std_logic;
          D   : in  std_logic_vector (WIDTH-1 downto 0);
          Q   : out std_logic_vector (WIDTH-1 downto 0));

end FD_ALT_VECTOR_SS;

architecture XILINX_virtex of FD_ALT_VECTOR_SS is

    component FD
        generic (INIT : bit );
        port (Q : out STD_ULOGIC;
              C : in STD_ULOGIC; D : in STD_ULOGIC);
    end component;

    attribute BEL : string;

begin  -- XILINX_virtex

    V2_F: if FPGA_Family /= "Virtex2" generate
        FDINST: for i in 0 to WIDTH-1 generate
            constant row   : integer := (WIDTH/2) - i/2 - 1;
            constant slice : integer := (i mod 2);
            constant col   : integer := (i mod 2);
            attribute RLOC of FDn : label is rloc_string(row,col,slice);
            attribute BEL  of FDn : label is bel_string_ff(XORY);
            attribute INIT of FDn : label is "0";
        begin
            FDn: FD
                generic map (INIT => '0')
                port map (Q  => Q(i), C  => CLK, D  => D(i));
        end generate FDINST;
    end generate V2_F;

    V2_T: if FPGA_Family = "Virtex2" generate
        FDINST: for i in 0 to WIDTH-1 generate
            constant row   : integer := (WIDTH/2) - i/2 - 1;
            constant slice : integer := (i mod 2);
            attribute RLOC of FDn : label is rloc_string(row,0,slice,WIDTH/2);
            attribute BEL  of FDn : label is bel_string_ff(XORY);
            attribute INIT of FDn : label is "0";
        begin
            FDn: FD
                generic map (INIT => '0')
                port map (Q  => Q(i), C  => CLK, D  => D(i));
        end generate FDINST;        
    end generate V2_T;
end XILINX_virtex;

-------------------------------------------------------------------------------
-- FDE_VECTOR Vector of FDE (FF With CE)
-------------------------------------------------------------------------------    
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

entity FDE_VECTOR is
    
    generic (WIDTH : integer := 32;
             SLICE : integer := 0);

    port (CLK : in  std_logic; CE  : in  std_logic;
          D   : in  std_logic_vector (WIDTH-1 downto 0);
          Q   : out std_logic_vector (WIDTH-1 downto 0));

end FDE_VECTOR;

architecture XILINX_virtex of FDE_VECTOR is

    component FDE
        generic (INIT : bit );
        port (Q  : out STD_ULOGIC; C  : in  STD_ULOGIC;
              CE : in  STD_ULOGIC; D  : in  STD_ULOGIC);
    end component;

    attribute BEL : string;

begin  -- XILINX_virtex

    FDEINST: for i in 0 to WIDTH-1 generate
        constant row : integer := (WIDTH/2) - i/2 - 1;
        attribute RLOC of FDEn : label is rloc_string(row,0,SLICE,WIDTH/2);
        attribute BEL  of FDEn : label is bel_string_ff(i);
        attribute INIT of FDEn : label is "0";
    begin
        FDEn: FDE
            generic map (INIT => '0')
            port map (Q  => Q(i), C  => CLK,
                      CE => CE  , D  => D(i));
    end generate FDEINST;

end XILINX_virtex;

-------------------------------------------------------------------------------
-- FDC_VECTOR Vector of FDE (FF With Async CLR)
-------------------------------------------------------------------------------    
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

entity FDC_VECTOR is
    
    generic (WIDTH : integer := 32;
             SLICE : integer := 0);

    port (CLK : in  std_logic;
          CLR : in  std_logic;
          D   : in  std_logic_vector (WIDTH-1 downto 0);
          Q   : out std_logic_vector (WIDTH-1 downto 0));

end FDC_VECTOR;

architecture XILINX_virtex of FDC_VECTOR is

    component FDC
        generic (INIT : bit );
        port (Q   : out STD_ULOGIC; C   : in  STD_ULOGIC;
             CLR  : in  STD_ULOGIC; D   : in  STD_ULOGIC);
    end component;

    attribute BEL : string;

begin  -- XILINX_virtex

    FDCINST: for i in 0 to WIDTH-1 generate
        constant row : integer := (WIDTH/2) - i/2 - 1;
        attribute RLOC of FDCn : label is rloc_string(row,0,SLICE,WIDTH/2);
        attribute BEL  of FDCn : label is bel_string_ff(i);
        attribute INIT of FDCn : label is "0";
    begin
        FDCn: FDC
            generic map (INIT => '0')
            port map (Q  => Q(i), C  => CLK,
                      D  => D(i), CLR => CLR);
    end generate FDCINST;

end XILINX_virtex;

-------------------------------------------------------------------------------
-- FDCE_ALT_VECTOR Vector of FDE (FF With CE & CLR).Placed one per slice (X or Y)
-------------------------------------------------------------------------------    
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

entity FDCE_ALT_VECTOR is
    
    generic (WIDTH : integer := 32;
             XORY  : integer := 0;
             SPLIT : boolean := False);

    port (CLK : in  std_logic;
          CE  : in  std_logic;
          CLR : in  std_logic;
          D   : in  std_logic_vector (WIDTH-1 downto 0);
          Q   : out std_logic_vector (WIDTH-1 downto 0));

end FDCE_ALT_VECTOR;

architecture XILINX_virtex of FDCE_ALT_VECTOR is

    component FDCE
        generic (INIT : bit );
        port (Q  : out STD_ULOGIC; C  : in  STD_ULOGIC;
              CLR: in  STD_ULOGIC;
              CE : in  STD_ULOGIC; D  : in  STD_ULOGIC);
    end component;

    attribute BEL : string;

begin  -- XILINX_virtex

    SPLIT_FALSE_INST: if SPLIT = False generate
        FDCEINST: for i in 0 to WIDTH-1 generate
            constant row   : integer := (WIDTH/2) - i/2 - 1;
            constant slice : integer := (i mod 2);
            attribute RLOC of FDCEn : label is rloc_string(row,0,slice,WIDTH/2);
            attribute BEL  of FDCEn : label is bel_string_ff(XORY);
            attribute INIT of FDCEn : label is "0";
        begin
            FDCEn: FDCE
                generic map (INIT => '0')
                port map (Q   => Q(i), C  => CLK,
                          CLR => CLR,
                          CE  => CE  , D  => D(i));
        end generate FDCEINST;        
    end generate SPLIT_FALSE_INST;

    SPLIT_TRUE_INST: if SPLIT = True generate
        FDCEINST: for i in 0 to WIDTH-1 generate
            constant row   : integer := (WIDTH/2) - i/2 - 1;
            constant slice : integer := (i mod 2);
            constant col   : integer := (i mod 2);
            attribute RLOC of FDCEn : label is rloc_string(row,col,slice,WIDTH/2,true,0,-col);
            attribute BEL  of FDCEn : label is bel_string_ff(XORY);
            attribute INIT of FDCEn : label is "0";
        begin
            FDCEn: FDCE
                generic map (INIT => '0')
                port map (Q   => Q(i), C  => CLK,
                          CLR => CLR,
                          CE  => CE  , D  => D(i));
        end generate FDCEINST;                
    end generate SPLIT_TRUE_INST;
    
end XILINX_virtex;


-------------------------------------------------------------------------------
-- Xilinx PLL
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.manikconfig.all;
use work.MANIKPACKAGE.all;
use work.manikxilinx.all;

entity xpll is
    port (iclk : in  std_logic;
          oclk : out std_logic);
end xpll;

architecture XILINX_virtex of xpll is
    
begin  -- XILINX_virtex


    Non_V2: if FPGA_Family /= "Virtex2" or USE_DCM = FALSE generate
        attribute CLKDV_DIVIDE          : real;
        attribute DUTY_CYCLE_CORRECTION : boolean;
        attribute STARTUP_WAIT          : boolean;
        
        signal CLKIN_IBUFG : std_logic;
        signal CLKFB_IN    : std_logic;
        signal CLK0_BUF    : std_logic;
        signal CLKFX_BUF   : std_logic;
        signal LOCKED_OUT  : std_logic;
        constant DLL_DIV   : real := real(CLK_DIVBY/CLK_MULBY);
        signal clkby2, clkby4, tclk : std_logic := '0';
    begin
        -- synopsys translate_off
        check: if (CLK_MULBY /= 1 or (CLK_DIVBY /= 2 and CLK_DIVBY /= 4)) and CLK_MULBY /= CLK_DIVBY generate
            assert True  report "Clock scaling requires DCM; available in Virtex2 family only" severity failure;
        end generate check;
        -- synopsys translate_on
        
        divsysclk2_proc : process (iclk)
        begin
            if rising_edge(iclk) then
                clkby2 <= not clkby2;
            end if;
        end process divsysclk2_proc;

        divsysclk4_proc : process (clkby2)
        begin
            if rising_edge(clkby2) then
                clkby4 <= not clkby4;
            end if;
        end process divsysclk4_proc;

        divsysclk_4: if CLK_DIVBY/CLK_MULBY = 4 generate        
            tclk <= clkby4;
        end generate divsysclk_4;

        divsysclk_2: if CLK_DIVBY/CLK_MULBY = 2 generate
            tclk <= clkby2;
        end generate divsysclk_2;
    
        ndivclk: if CLK_DIVBY = CLK_MULBY generate
            tclk <=iclk;
        end generate ndivclk;
        BUFG_I : BUFG port map (I => tclk, O => oclk);
--        CLKIN_IBUFG_INST : IBUFG
--            port map (I => iclk, O => CLKIN_IBUFG);

--        DIVBY1: if CLK_DIVBY = CLK_MULBY generate
----            attribute CLKDV_DIVIDE of CLKDLL_INST          : label is 2.0;
----            attribute DUTY_CYCLE_CORRECTION of CLKDLL_INST : label is "TRUE";
----            attribute STARTUP_WAIT of CLKDLL_INST          : label is "FALSE";
--        begin
--            CLKDLL_INST : CLKDLL
--                generic map (CLKDV_DIVIDE          => 2.0,
--                             DUTY_CYCLE_CORRECTION => true,
--                             STARTUP_WAIT          => false)
--                port map (CLKIN  => CLKIN_IBUFG,
--                          CLKFB  => CLKFB_IN,
--                          LOCKED => LOCKED_OUT,
--                          CLKDV  => open);
--            CLKFX_BUF <= CLKIN_IBUFG;
--        end generate DIVBY1;

--        DIVBYnot1: if CLK_DIVBY /= CLK_MULBY generate
----            attribute CLKDV_DIVIDE of CLKDLL_INST          : label is DLL_DIV;
----            attribute DUTY_CYCLE_CORRECTION of CLKDLL_INST : label is "TRUE";
----            attribute STARTUP_WAIT of CLKDLL_INST          : label is "FALSE";
--        begin
--            CLKDLL_INST : CLKDLL
--                generic map (CLKDV_DIVIDE          => DLL_DIV,
--                             DUTY_CYCLE_CORRECTION => true,
--                             STARTUP_WAIT          => false)
--                port map (CLKIN  => CLKIN_IBUFG,
--                          CLKFB  => CLKFB_IN,
--                          LOCKED => LOCKED_OUT,
--                          CLKDV  => CLKFX_BUF);
--        end generate DIVBYnot1;
        
--        CLK0_BUFG_INST : BUFG
--            port map (I => CLK0_BUF,
--                      O => CLKFB_IN);

--        CLKFX_BUFG_INST : BUFG
--            port map (I => CLKFX_BUF,
--                      O => oclk);
        
    end generate Non_V2;

    V2: if FPGA_Family = "Virtex2" and USE_DCM = True generate
        signal CLKIN_IBUFG : std_logic;
        signal CLKFB_IN    : std_logic;
        signal CLK0_BUF    : std_logic;
        signal CLKFX_BUF   : std_logic;
        signal LOCKED_OUT  : std_logic;
        signal GND         : std_logic;
    begin
        CLKIN_IBUFG_INST : IBUFG
            port map (I => iclk, O => CLKIN_IBUFG);

        DCM_INST : DCM
            Generic map (CLK_FEEDBACK          => "1X",
                         CLKDV_DIVIDE          => 2.0,
                         CLKFX_DIVIDE          => CLK_DIVBY,
                         CLKFX_MULTIPLY        => CLK_MULBY,
                         CLKIN_DIVIDE_BY_2     => FALSE,
                         CLKIN_PERIOD          => CLKIN_PERIOD,
                         CLKOUT_PHASE_SHIFT    => "NONE",
                         DESKEW_ADJUST         => "SYSTEM_SYNCHRONOUS",
                         DSS_MODE              => "NONE",
                         DFS_FREQUENCY_MODE    => "LOW",
                         DLL_FREQUENCY_MODE    => "LOW",
                         DUTY_CYCLE_CORRECTION => TRUE,
                         FACTORY_JF            => X"C080",
                         PHASE_SHIFT           => 0,
                         STARTUP_WAIT          => FALSE)
            port map (CLKIN    => CLKIN_IBUFG,
                      CLKFB    => CLKFB_IN,
                      RST      => GND,
                      PSEN     => GND,
                      PSINCDEC => GND,
                      PSCLK    => GND,
                      DSSEN    => '1',
                      CLK0     => CLK0_BUF,
                      CLKFX    => CLKFX_BUF,
                      LOCKED   => LOCKED_OUT);

        CLK0_BUFG_INST : BUFG
            port map (I => CLK0_BUF,
                      O => CLKFB_IN);

        CLKFX_BUFG_INST : BUFG
            port map (I => CLKFX_BUF,
                      O => oclk);
        GND <= '0';
    end generate V2;
end XILINX_virtex;

-------------------------------------------------------------------------------
-- Xilinx single ported memory for VIRTEX & Spartan2
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.MANIKPACKAGE.all;
--use work.manikxilinx.all;

entity xilspram is
    
    generic (MEM_DATA_WIDTH : integer := 32;
             MEM_ADDR_WIDTH : integer := 10);

    port (clk    : in  std_logic;
          addr   : in  std_logic_vector (MEM_ADDR_WIDTH-1 downto 0);
          data_i : in  std_logic_vector (MEM_DATA_WIDTH-1 downto 0);
          enb    : in  std_logic;
          rst    : in  std_logic;
          we     : in  std_logic;
          data_o : out std_logic_vector (MEM_DATA_WIDTH-1 downto 0));

end xilspram;

architecture XILINX_virtex of xilspram is

begin  -- XILINX_virtex

    -- if depth less that 256 then instantiate RAMB4_S16
    LTEQ_256: if MEM_ADDR_WIDTH <= 8 generate
        constant nblks          : integer                                  := (MEM_DATA_WIDTH/16);
        signal   tdatai, tdatao : std_logic_vector ((nblks*16)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector (7 downto 0)            := (others => '0');
    begin
        taddr  (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao (MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low : integer := i*16;
            constant high : integer := ((i+1)*16)-1;
        begin
            RAMB4_S16_1: RAMB4_S16
                port map (DO   => tdatao(high downto low),
                          ADDR => taddr, CLK  => clk,
                          DI   => tdatai(high downto low),
                          EN   => enb, RST  => rst, WE   => we);
        end generate iblks;
    end generate LTEQ_256;

    -- if depth less that 512 then instantiate RAMB4_S8
    LTEQ_512: if MEM_ADDR_WIDTH = 9 generate
        constant nblks          : integer                                  := (MEM_DATA_WIDTH/8);
        signal   tdatai, tdatao : std_logic_vector ((nblks*8)-1 downto 0)  := (others => '0');
        signal   taddr          : std_logic_vector (8 downto 0)            := (others => '0');
    begin
        taddr  (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao (MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low : integer := i*8;
            constant high : integer := ((i+1)*8)-1;
        begin
            RAMB4_S8_1: RAMB4_S8
                port map (DO   => tdatao(high downto low),
                          ADDR => taddr, CLK  => clk,
                          DI   => tdatai(high downto low),
                          EN   => enb, RST  => rst, WE   => we);
        end generate iblks;
    end generate LTEQ_512;

    -- if depth less that 1024 then instantiate RAMB4_S4
    LTEQ_1024: if MEM_ADDR_WIDTH = 10  generate        
        constant nblks          : integer                                  := (MEM_DATA_WIDTH/4);
        signal   tdatai, tdatao : std_logic_vector ((nblks*4)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector (9 downto 0)           := (others => '0');
    begin
        taddr  (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao (MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low : integer := i*4;
            constant high : integer := ((i+1)*4)-1;
        begin
            RAMB4_S4_1: RAMB4_S4
                port map (DO   => tdatao(high downto low),
                          ADDR => taddr, CLK  => clk,
                          DI   => tdatai(high downto low),
                          EN   => enb, RST  => rst, WE   => we);
        end generate iblks;
    end generate LTEQ_1024;

    -- if depth less that 2048 then instantiate RAMB4_S2
    LTEQ_2048: if MEM_ADDR_WIDTH = 11 generate
        constant nblks          : integer                                  := (MEM_DATA_WIDTH/2);
        signal   tdatai, tdatao : std_logic_vector ((nblks*2)-1 downto 0)  := (others => '0');
        signal   taddr          : std_logic_vector (10 downto 0)           := (others => '0');
    begin
        taddr  (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao (MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low : integer := i*2;
            constant high : integer := ((i+1)*2)-1;
        begin
            RAMB4_S2_1: RAMB4_S2
                port map (DO   => tdatao(high downto low),
                          ADDR => taddr, CLK  => clk,
                          DI   => tdatai(high downto low),
                          EN   => enb, RST  => rst, WE   => we);
        end generate iblks;
    end generate LTEQ_2048;

    -- if depth less that 4096 then instantiate RAMB4_S1
    LTEQ_4096: if MEM_ADDR_WIDTH <= 12 and MEM_ADDR_WIDTH > 11 generate
        constant nblks          : integer                               := (MEM_DATA_WIDTH/2);
        signal   tdatai, tdatao : std_logic_vector ((nblks)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector (11 downto 0)        := (others => '0');
    begin
        taddr  (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao (MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low : integer := i;
            constant high : integer := (i+1)-1;
        begin
            RAMB4_S1_1: RAMB4_S1
                port map (DO   => tdatao(high downto low),
                          ADDR => taddr, CLK  => clk,
                          DI   => tdatai(high downto low),
                          EN   => enb, RST  => rst, WE   => we);
        end generate iblks;
    end generate LTEQ_4096;

    
end XILINX_virtex;

-------------------------------------------------------------------------------
-- Xilinx single ported memory for VIRTEX2 & Spartan-3 
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.MANIKPACKAGE.all;
--use work.manikxilinx.all;

entity xilspram_v2 is
    
    generic (MEM_DATA_WIDTH : integer := 32;
             MEM_ADDR_WIDTH : integer := 10);

    port (clk    : in  std_logic;
          addr   : in  std_logic_vector(MEM_ADDR_WIDTH-1 downto 0);
          data_i : in  std_logic_vector(MEM_DATA_WIDTH-1 downto 0);
          enb    : in  std_logic;
          rst    : in  std_logic;
          we     : in  std_logic;
          data_o : out std_logic_vector(MEM_DATA_WIDTH-1 downto 0));

end xilspram_v2;

architecture XILINX_virtex of xilspram_v2 is
    
begin  -- XILINX_virtex

    -- if depth required is less than 512 then instantiate RAMB16_S36
    LTEQ_512: if MEM_ADDR_WIDTH <= 9 generate
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,36);
        signal   tdatai, tdatao : std_logic_vector((nblks*36)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(8 downto 0)            := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);
        
        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i*36;
            constant high : integer := ((i+1)*36)-1;
        begin
            RAMB16_S36_1 : RAMB16_S36
                port map (DO   => tdatao (high-4 downto low),
                          DOP  => tdatao (high   downto high-3),
                          ADDR => taddr, CLK => clk,
                          DI   => tdatai (high-4 downto low),
                          DIP  => tdatai (high   downto high-3),
                          EN   => enb, SSR => rst, WE => we);            
        end generate iblks;
    end generate LTEQ_512;

    -- if depth required less than eq 1K then RAMB16_S18
    LTEQ_1K: if MEM_ADDR_WIDTH = 10 generate        
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,18);
        signal   tdatai, tdatao : std_logic_vector((nblks*18)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(9 downto 0)            := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i*18;
            constant high : integer := ((i+1)*18)-1;
        begin
            RAMB16_S18_1 : RAMB16_S18
                port map (DO   => tdatao (high-2 downto low),
                          DOP  => tdatao (high   downto high-1),
                          ADDR => taddr, CLK => clk,
                          DI   => tdatai (high-2 downto low),
                          DIP  => tdatai (high   downto high-1),
                          EN   => enb, SSR => rst, WE => we);            
        end generate iblks;        
    end generate LTEQ_1K;

    -- if depth required is less then eq 2K then RAMB16_S9
    LTEQ_2K: if MEM_ADDR_WIDTH <= 11 and MEM_ADDR_WIDTH > 10 generate
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,9);
        signal   tdatai, tdatao : std_logic_vector((nblks*9)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(10 downto 0)          := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i*9;
            constant high : integer := ((i+1)*9)-1;
        begin
            RAMB16_S9_1 : RAMB16_S9
                port map (DO   => tdatao (high-1 downto low),
                          DOP  => tdatao (high   downto high),
                          ADDR => taddr, CLK => clk,
                          DI   => tdatai (high-1 downto low),
                          DIP  => tdatai (high   downto high),
                          EN   => enb, SSR => rst, WE => we);            
        end generate iblks;        
    end generate LTEQ_2K;

    -- if depth is less than eq 4K then RAMB16_S4
    LTEQ_4K: if MEM_ADDR_WIDTH <= 12 and MEM_ADDR_WIDTH > 11 generate
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,4);
        signal   tdatai, tdatao : std_logic_vector((nblks*4)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(11 downto 0)          := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i*4;
            constant high : integer := ((i+1)*4)-1;
        begin
            RAMB16_S4_1 : RAMB16_S4
                port map (DO   => tdatao (high downto low),
                          ADDR => taddr, CLK => clk,
                          DI   => tdatai (high downto low),
                          EN   => enb, SSR => rst, WE => we);            
        end generate iblks;                
    end generate LTEQ_4K;

    -- if depth is less than eq 8K then RAMB16_S2
    LTEQ_8K: if MEM_ADDR_WIDTH <= 13 and MEM_ADDR_WIDTH > 12 generate
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,2);
        signal   tdatai, tdatao : std_logic_vector((nblks*2)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(12 downto 0)      	 := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i*2;
            constant high : integer := ((i+1)*2)-1;
        begin
            RAMB16_S2_1 : RAMB16_S2
                port map (DO   => tdatao (high downto low),
                          ADDR => taddr, CLK => clk,
                          DI   => tdatai (high downto low),
                          EN   => enb, SSR => rst, WE => we);            
        end generate iblks;                
    end generate LTEQ_8K;

    -- if depth is less than eq 16K then RAMB16_S1
    LTEQ_16K: if MEM_ADDR_WIDTH <= 14 and MEM_ADDR_WIDTH > 13 generate
        constant nblks          : integer                            := MEM_DATA_WIDTH;
        signal   tdatai, tdatao : std_logic_vector(nblks-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(13 downto 0)      := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i;
            constant high : integer := i;
        begin
            RAMB16_S1_1 : RAMB16_S1
                port map (DO   => tdatao (high downto low),
                          ADDR => taddr, CLK => clk,
                          DI   => tdatai (high downto low),
                          EN   => enb, SSR => rst, WE => we);            
        end generate iblks;                
    end generate LTEQ_16K;
    
end XILINX_virtex;

-------------------------------------------------------------------------------
-- Xilinx dual ported memory for VIRTEX & Spartan2
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.MANIKPACKAGE.all;

entity xildpram is
    
    generic (MEM_DATA_WIDTH : integer := 32;
             MEM_ADDR_WIDTH : integer := 10);

    port (rclk   : in  std_logic;
          wclk   : in  std_logic;
          raddr  : in  std_logic_vector (MEM_ADDR_WIDTH-1 downto 0);
          waddr  : in  std_logic_vector (MEM_ADDR_WIDTH-1 downto 0);
          data_i : in  std_logic_vector (MEM_DATA_WIDTH-1 downto 0);
          enb    : in  std_logic;
          rst    : in  std_logic;
          we     : in  std_logic;
          data_o : out std_logic_vector (MEM_DATA_WIDTH-1 downto 0));

end xildpram;

architecture XILINX_virtex of xildpram is

begin  -- XILINX_virtex

    -- if depth less that 256 then instantiate RAMB4_S16
    LTEQ_256: if MEM_ADDR_WIDTH <= 8 generate
        constant nblks              : integer                                  := (MEM_DATA_WIDTH/16)+1;
        signal   dz, tdatai, tdatao : std_logic_vector ((nblks*16)-1 downto 0) := (others => '0');
        signal   twaddr, traddr     : std_logic_vector (7 downto 0)            := (others => '0');
    begin
        twaddr  (MEM_ADDR_WIDTH-1 downto 0) <= waddr;
        traddr  (MEM_ADDR_WIDTH-1 downto 0) <= raddr;
        tdatai  (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao (MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low : integer := i*16;
            constant high : integer := ((i+1)*16)-1;
        begin
            -- port A for read & port B for write
            RAMB4_S16_S16_1: RAMB4_S16_S16
                port map (DOA   => tdatao(high downto low),
                          DOB   => open,
                          ADDRA => traddr, ADDRB => twaddr,
                          CLKA  => rclk,   CLKB  => wclk,
                          DIA   => dz,
                          DIB   => tdatai(high downto low),
                          ENA   => enb,    ENB   => enb,
                          RSTA  => rst,    RSTB  => rst,
                          WEA   => '0',    WEB   => we);
        end generate iblks;
    end generate LTEQ_256;

    -- if depth less that 512 then instantiate RAMB4_S8
    LTEQ_512: if MEM_ADDR_WIDTH <= 9 and MEM_ADDR_WIDTH > 8 generate
        constant nblks              : integer                                 := (MEM_DATA_WIDTH/8)+1;
        signal   dz, tdatai, tdatao : std_logic_vector ((nblks*8)-1 downto 0) := (others => '0');
        signal   twaddr, traddr     : std_logic_vector (8 downto 0)           := (others => '0');
    begin
        twaddr  (MEM_ADDR_WIDTH-1 downto 0) <= waddr;
        traddr  (MEM_ADDR_WIDTH-1 downto 0) <= raddr;
        tdatai  (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao (MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low : integer := i*8;
            constant high : integer := ((i+1)*8)-1;
        begin
            -- port A for read & port B for write
            RAMB4_S8_S8_1: RAMB4_S8_S8
                port map (DOA   => tdatao(high downto low),
                          DOB   => open,
                          ADDRA => traddr, ADDRB => twaddr,
                          CLKA  => rclk,   CLKB  => wclk,
                          DIA   => dz,
                          DIB   => tdatai(high downto low),
                          ENA   => enb,    ENB   => enb,
                          RSTA  => rst,    RSTB  => rst,
                          WEA   => '0',    WEB   => we);
        end generate iblks;
    end generate LTEQ_512;

    -- if depth less that 1024 then instantiate RAMB4_S4
    LTEQ_1024: if MEM_ADDR_WIDTH <= 10 and MEM_ADDR_WIDTH > 9 generate
        constant nblks              : integer                                 := (MEM_DATA_WIDTH/4)+1;
        signal   dz, tdatai, tdatao : std_logic_vector ((nblks*4)-1 downto 0) := (others => '0');
        signal   twaddr, traddr     : std_logic_vector (9 downto 0)           := (others => '0');
    begin
        twaddr  (MEM_ADDR_WIDTH-1 downto 0) <= waddr;
        traddr  (MEM_ADDR_WIDTH-1 downto 0) <= raddr;
        tdatai  (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao (MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low : integer := i*4;
            constant high : integer := ((i+1)*4)-1;
        begin
            -- port A for read & port B for write
            RAMB4_S4_S4_1: RAMB4_S4_S4
                port map (DOA   => tdatao(high downto low),
                          DOB   => open,
                          ADDRA => traddr, ADDRB => twaddr,
                          CLKA  => rclk,   CLKB  => wclk,
                          DIA   => dz,
                          DIB   => tdatai(high downto low),
                          ENA   => enb,    ENB   => enb,
                          RSTA  => rst,    RSTB  => rst,
                          WEA   => '0',    WEB   => we);
        end generate iblks;
    end generate LTEQ_1024;

    -- if depth less that 2048 then instantiate RAMB4_S2
    LTEQ_2048: if MEM_ADDR_WIDTH <= 11 and MEM_ADDR_WIDTH > 10 generate
        constant nblks              : integer                                 := (MEM_DATA_WIDTH/2)+1;
        signal   dz, tdatai, tdatao : std_logic_vector ((nblks*2)-1 downto 0) := (others => '0');
        signal   twaddr, traddr     : std_logic_vector (10 downto 0)          := (others => '0');
    begin
        twaddr  (MEM_ADDR_WIDTH-1 downto 0) <= waddr;
        traddr  (MEM_ADDR_WIDTH-1 downto 0) <= raddr;
        tdatai  (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao (MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low : integer := i*2;
            constant high : integer := ((i+1)*2)-1;
        begin
            -- port A for read & port B for write
            RAMB4_S2_S2_1: RAMB4_S2_S2
                port map (DOA   => tdatao(high downto low),
                          DOB   => open,
                          ADDRA => traddr, ADDRB => twaddr,
                          CLKA  => rclk,   CLKB  => wclk,
                          DIA   => dz,
                          DIB   => tdatai(high downto low),
                          ENA   => enb,    ENB   => enb,
                          RSTA  => rst,    RSTB  => rst,
                          WEA   => '0',    WEB   => we);
        end generate iblks;
    end generate LTEQ_2048;

    -- if depth less that 4096 then instantiate RAMB4_S1
    LTEQ_4096: if MEM_ADDR_WIDTH <= 12 and MEM_ADDR_WIDTH > 11 generate
        constant nblks              : integer                               := (MEM_DATA_WIDTH/2)+1;
        signal   dz, tdatai, tdatao : std_logic_vector ((nblks)-1 downto 0) := (others => '0');
        signal   twaddr, traddr     : std_logic_vector (11 downto 0)        := (others => '0');
    begin
        twaddr  (MEM_ADDR_WIDTH-1 downto 0) <= waddr;
        traddr  (MEM_ADDR_WIDTH-1 downto 0) <= raddr;
        tdatai  (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao (MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low : integer := i;
            constant high : integer := (i+1)-1;
        begin
            -- port A for read & port B for write
            RAMB4_S1_S1_1: RAMB4_S1_S1
                port map (DOA   => tdatao(high downto low),
                          DOB   => dz,
                          ADDRA => traddr, ADDRB => twaddr,
                          CLKA  => rclk,   CLKB  => wclk,
                          DIA   => dz,
                          DIB   => tdatai(high downto low),
                          ENA   => enb,    ENB   => enb,
                          RSTA  => rst,    RSTB  => rst,
                          WEA   => '0',    WEB   => we);
        end generate iblks;
    end generate LTEQ_4096;

    
end XILINX_virtex;

-------------------------------------------------------------------------------
-- Xilinx dual ported memory for VIRTEX2 & Spartan-3 
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library UNISIM;
use UNISIM.vcomponents.all;
use work.MANIKPACKAGE.all;

entity xildpram_v2 is
    
    generic (MEM_DATA_WIDTH : integer := 32;
             MEM_ADDR_WIDTH : integer := 10);

    port (rclk   : in  std_logic;
          wclk   : in  std_logic;
          raddr  : in  std_logic_vector(MEM_ADDR_WIDTH-1 downto 0);
          waddr  : in  std_logic_vector(MEM_ADDR_WIDTH-1 downto 0);
          data_i : in  std_logic_vector(MEM_DATA_WIDTH-1 downto 0);
          enb    : in  std_logic;
          rst    : in  std_logic;
          we     : in  std_logic;
          data_o : out std_logic_vector(MEM_DATA_WIDTH-1 downto 0));

end xildpram_v2;

architecture XILINX_virtex of xildpram_v2 is
    
begin  -- XILINX_virtex

    -- if depth required is less than 512 then instantiate RAMB16_S36
    LTEQ_512: if MEM_ADDR_WIDTH <= 9 generate
        constant nblks              : integer                                 := (MEM_DATA_WIDTH/36)+1;
        signal   dz, tdatai, tdatao : std_logic_vector((nblks*36)-1 downto 0) := (others => '0');
        signal   twaddr, traddr     : std_logic_vector(8 downto 0)            := (others => '0');
    begin
        twaddr (MEM_ADDR_WIDTH-1 downto 0) <= raddr;
        traddr (MEM_ADDR_WIDTH-1 downto 0) <= waddr;
        tdatai (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);
        
        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i*36;
            constant high : integer := ((i+1)*36)-1;
        begin
            -- port A is read port & Port B is write port
            RAMB16_S36_S36_1: RAMB16_S36_S36
                port map (DOA   => tdatao (high-4 downto low),
                          DOPA  => tdatao (high   downto high-3),
                          DOB   => open,   DOPB  => open,  -- write port (output open)
                          ADDRA => traddr, ADDRB => twaddr,
                          CLKA  => rclk,   CLKB  => wclk,
                          DIA   => dz,     DIPA  => dz(high downto high-3),  -- read port (input open)
                          DIB   => tdatai (high-4 downto low),
                          DIPB  => tdatai (high   downto high-3),
                          ENA   => enb,    ENB   => enb,
                          SSRA  => rst,    SSRB  => rst,
                          WEA   => '0',    WEB   => we);
        end generate iblks;
    end generate LTEQ_512;

    -- if depth required less than eq 1K then RAMB16_S18
    LTEQ_1K: if MEM_ADDR_WIDTH <= 10 and MEM_ADDR_WIDTH > 9 generate
        constant nblks              : integer                                 := (MEM_DATA_WIDTH/18)+1;
        signal   dz, tdatai, tdatao : std_logic_vector((nblks*18)-1 downto 0) := (others => '0');
        signal   twaddr, traddr     : std_logic_vector(9 downto 0)            := (others => '0');
    begin
        traddr (MEM_ADDR_WIDTH-1 downto 0) <= raddr;
        twaddr (MEM_ADDR_WIDTH-1 downto 0) <= waddr;
        tdatai (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i*18;
            constant high : integer := ((i+1)*18)-1;
        begin
            -- port A is read port & Port B is write port
            RAMB16_S18_S18_1: RAMB16_S18_S18
                port map (DOA   => tdatao (high-2 downto low),
                          DOPA  => tdatao (high   downto high-1),
                          DOB   => open,   DOPB  => open,  -- write port (output open)
                          ADDRA => traddr, ADDRB => twaddr,
                          CLKA  => rclk,   CLKB  => wclk,
                          DIA   => dz(high-2 downto low),     
								  DIPA  => dz(high downto high-1),  -- read port (input open)
                          DIB   => tdatai (high-2 downto low),
                          DIPB  => tdatai (high   downto high-1),
                          ENA   => enb,    ENB   => enb,
                          SSRA  => rst,    SSRB  => rst,
                          WEA   => '0',    WEB   => we);
        end generate iblks;        
    end generate LTEQ_1K;

    -- if depth required is less then eq 2K then RAMB16_S9
    LTEQ_2K: if MEM_ADDR_WIDTH <= 11 and MEM_ADDR_WIDTH > 10 generate
        constant nblks              : integer                                := (MEM_DATA_WIDTH/9)+1;
        signal   dz, tdatai, tdatao : std_logic_vector((nblks*9)-1 downto 0) := (others => '0');
        signal   twaddr, traddr     : std_logic_vector(10 downto 0)          := (others => '0');
    begin
        traddr (MEM_ADDR_WIDTH-1 downto 0) <= raddr;
        twaddr (MEM_ADDR_WIDTH-1 downto 0) <= waddr;
        tdatai (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i*9;
            constant high : integer := ((i+1)*9)-1;
        begin
            -- port A is read port & Port B is write port
            RAMB16_S9_S9_1: RAMB16_S9_S9
                port map (DOA   => tdatao (high-1 downto low),
                          DOPA  => tdatao (high   downto high),
                          DOB   => open,   DOPB  => open,  -- write port (output open)
                          ADDRA => traddr, ADDRB => twaddr,
                          CLKA  => rclk,   CLKB  => wclk,
                          DIA   => dz,     DIPA  => dz(high downto high),  -- read port (input open)
                          DIB   => tdatai (high-1 downto low),
                          DIPB  => tdatai (high   downto high),
                          ENA   => enb,    ENB   => enb,
                          SSRA  => rst,    SSRB  => rst,
                          WEA   => '0',    WEB   => we);
        end generate iblks;        
    end generate LTEQ_2K;

    -- if depth is less than eq 4K then RAMB16_S4
    LTEQ_4K: if MEM_ADDR_WIDTH <= 12 and MEM_ADDR_WIDTH > 11 generate
        constant nblks              : integer                                := (MEM_DATA_WIDTH/4);
        signal   dz, tdatai, tdatao : std_logic_vector((nblks*4)-1 downto 0) := (others => '0');
        signal   twaddr, traddr     : std_logic_vector(11 downto 0)          := (others => '0');
    begin
        traddr (MEM_ADDR_WIDTH-1 downto 0) <= raddr;
        twaddr (MEM_ADDR_WIDTH-1 downto 0) <= waddr;
        tdatai (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i*4;
            constant high : integer := ((i+1)*4)-1;
        begin
            -- port A is read port & Port B is write port
            RAMB16_S4_S4_1: RAMB16_S4_S4
                port map (DOA   => tdatao (high downto low),
                          DOB   => open,     -- write port (output open)
                          ADDRA => traddr, ADDRB => twaddr,
                          CLKA  => rclk,   CLKB  => wclk,
                          DIA   => dz,       -- read port (input open)
                          DIB   => tdatai (high downto low),
                          ENA   => enb,    ENB   => enb,
                          SSRA  => rst,    SSRB  => rst,
                          WEA   => '0',    WEB   => we);
        end generate iblks;                
    end generate LTEQ_4K;

    -- if depth is less than eq 8K then RAMB16_S2
    LTEQ_8K: if MEM_ADDR_WIDTH <= 13 and MEM_ADDR_WIDTH > 12 generate
        constant nblks              : integer                                := (MEM_DATA_WIDTH/2);
        signal   dz, tdatai, tdatao : std_logic_vector((nblks*2)-1 downto 0) := (others => '0');
        signal   twaddr, traddr     : std_logic_vector(12 downto 0)          := (others => '0');
    begin
        traddr (MEM_ADDR_WIDTH-1 downto 0) <= raddr;
        twaddr (MEM_ADDR_WIDTH-1 downto 0) <= waddr;
        tdatai (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i*2;
            constant high : integer := ((i+1)*2)-1;
        begin
            -- port A is read port & Port B is write port
            RAMB16_S2_S2_1: RAMB16_S2_S2
                port map (DOA   => tdatao (high downto low),
                          DOB   => open,   -- write port (output open)
                          ADDRA => traddr, ADDRB => twaddr,
                          CLKA  => rclk,   CLKB  => wclk,
                          DIA   => dz,       -- read port (input open)
                          DIB   => tdatai (high-1 downto low),
                          ENA   => enb,    ENB   => enb,
                          SSRA  => rst,    SSRB  => rst,
                          WEA   => '0',    WEB   => we);
        end generate iblks;                
    end generate LTEQ_8K;

    -- if depth is less than eq 16K then RAMB16_S1
    LTEQ_16K: if MEM_ADDR_WIDTH <= 14 and MEM_ADDR_WIDTH > 13 generate
        constant nblks              : integer                            := MEM_DATA_WIDTH;
        signal   dz, tdatai, tdatao : std_logic_vector(nblks-1 downto 0) := (others => '0');
        signal   twaddr, traddr     : std_logic_vector(13 downto 0)      := (others => '0');
    begin
        traddr (MEM_ADDR_WIDTH-1 downto 0) <= raddr;
        twaddr (MEM_ADDR_WIDTH-1 downto 0) <= waddr;
        tdatai (MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i;
            constant high : integer := i;
        begin
            -- port A is read port & Port B is write port
            RAMB16_S1_S1_1: RAMB16_S1_S1
                port map (DOA   => tdatao (high downto low),
                          DOB   => open,   -- write port (output open)
                          ADDRA => traddr, ADDRB => twaddr,
                          CLKA  => rclk,   CLKB  => wclk,
                          DIA   => dz,     -- read port (input open)
                          DIB   => tdatai (high-1 downto low),
                          ENA   => enb,    ENB   => enb,
                          SSRA  => rst,    SSRB  => rst,
                          WEA   => '0',    WEB   => we);
        end generate iblks;                
    end generate LTEQ_16K;
    
end XILINX_virtex;

