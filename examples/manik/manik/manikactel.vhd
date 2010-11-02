library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_1164.all;

library UNISIM;
use UNISIM.vcomponents.all;

use work.manikconfig.all;
use work.manikpackage.all;

package manikactel is

    component adsu
        port (DataA       : in  std_logic_vector(31 downto 0);
              DataB       : in  std_logic_vector(31 downto 0);
              Cin, Addsub : in  std_logic;
              Sum         : out std_logic_vector(31 downto 0);
              Cout        : out std_logic);
    end component;

    component adsu_apa3
        port (
            DataA       : in  std_logic_vector(31 downto 0);
            DataB       : in  std_logic_vector(31 downto 0);
            Cin, Addsub : in  std_logic;
            Sum         : out std_logic_vector(31 downto 0);
            Cout        : out std_logic);
    end component;
    
    component adsu_fast_apa3
      port (DataA       : in  std_logic_vector(31 downto 0);
            DataB       : in  std_logic_vector(31 downto 0);
            Cin, Addsub : in  std_logic;
            Sum         : out std_logic_vector(31 downto 0);
            Cout        : out std_logic);
    end component;
    
    component ramdp
        port (DO     : out std_logic_vector (31 downto 0);
              WCLOCK : in  std_logic;
              DI     : in  std_logic_vector (31 downto 0);
              PO     : out std_logic_vector (3 downto 0);
              PI     : in  std_logic_vector (3 downto 0);
              WRB    : in  std_logic;
              RDB    : in  std_logic;
              WADDR  : in  std_logic_vector (3 downto 0);
              RADDR  : in  std_logic_vector (3 downto 0);
              WPE    : out std_logic;
              RPE    : out std_logic);
    end component;

    component syncram8
        port (DO     : out std_logic_vector (7 downto 0);
              RCLOCK : in  std_logic;
              WCLOCK : in  std_logic;
              DI     : in  std_logic_vector (7 downto 0);
              PO     : out std_logic;
              PI     : in  std_logic;
              WRB    : in  std_logic;
              RDB    : in  std_logic;
              WADDR  : in  std_logic_vector (7 downto 0);
              RADDR  : in  std_logic_vector (7 downto 0);
              WPE    : out std_logic;
              RPE    : out std_logic);
    end component;

    component mux41_actel
        port (Data0_port : in  std_logic_vector(31 downto 0);
              Data1_port : in  std_logic_vector(31 downto 0);
              Data2_port : in  std_logic_vector(31 downto 0);
              Data3_port : in  std_logic_vector(31 downto 0);
              Sel0, Sel1 : in  std_logic;
              Result     : out std_logic_vector(31 downto 0));
    end component;

    component ramdp_apa3
        port (DATAA      : in  std_logic_vector(31 downto 0);
              QA         : out std_logic_vector(31 downto 0);
              DATAB      : in  std_logic_vector(31 downto 0);
              QB         : out std_logic_vector(31 downto 0);
              ADDRESSA   : in  std_logic_vector(3 downto 0);
              ADDRESSB   : in  std_logic_vector(3 downto 0);
              RWA, RWB,
              BLKA, BLKB,
              CLKA, CLKB : in  std_logic);
    end component;

    component mux41_actel_apa3
        port (
            Data0_port : in  std_logic_vector(31 downto 0);
            Data1_port : in  std_logic_vector(31 downto 0);
            Data2_port : in  std_logic_vector(31 downto 0);
            Data3_port : in  std_logic_vector(31 downto 0);
            Sel0, Sel1 : in  std_logic;
            Result     : out std_logic_vector(31 downto 0));
    end component;

    component mux31_apa3
      port (Data0_port : in  std_logic_vector(31 downto 0);
            Data1_port : in  std_logic_vector(31 downto 0);
            Data2_port : in  std_logic_vector(31 downto 0);
            Sel0, Sel1 : in  std_logic;
            Result     : out std_logic_vector(31 downto 0));
    end component;
    
    component mult_apa3
        port (DataA : in  std_logic_vector(31 downto 0);
              DataB : in  std_logic_vector(1 downto 0);
              Mult  : out std_logic_vector(33 downto 0));
    end component;

    component actpll
      port (POWERDOWN, CLKA : in  std_logic;
            LOCK, GLA       : out std_logic);
    end component;
    
end manikactel;
