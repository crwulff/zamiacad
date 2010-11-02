-------------------------------------------------------------------------------
-- Title      : maniklatlib
-- Project    : MANIK-2
-------------------------------------------------------------------------------
-- File       : maniklatlib.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2006-01-19
-- Last update: 2006-10-02
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Contains LATTICE specific library
-------------------------------------------------------------------------------
-- Copyright (c) 2002-2005 Niktech Inc
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2006-01-19  1.0      sandeep	Created
-------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library ecp;
use ecp.components.all;
use work.manikconfig.all;
use work.manikpackage.all;

library UNISIM;
use UNISIM.vcomponents.all;


-------------------------------------------------------------------------------
--                                    ECP Related 
-------------------------------------------------------------------------------
entity ECP_SP8K_S36 is    
    port (do   : out std_logic_vector (35 downto 0);
          addr : in  std_logic_vector (7 downto 0);
          di   : in  std_logic_vector (35 downto 0);
          clk  : in  std_logic;
          en   : in  std_logic;
          we   : in  std_logic;
          rst  : in  std_logic);
end ECP_SP8K_S36;

architecture LATTICE_ecp of ECP_SP8K_S36 is

begin  -- LATTICE_ecp

    -- half addressed thru port a other half thru port b
    DP8KA_1: DP8KA
        generic map (CSDECODE_B   => "000",    CSDECODE_A  => "000",
                     WRITEMODE_B  => "WRITETHROUGH", WRITEMODE_A => "WRITETHROUGH", GSR => "DISABLED",
                     RESETMODE    => "ASYNC",   REGMODE_B => "NOREG", REGMODE_A => "NOREG",
                     DATA_WIDTH_B => 18, DATA_WIDTH_A => 18)
        port map (CEA   => en,  CLKA  => clk, WEA   => we,
                  CSA0  => '0', CSA1  => '0', CSA2  => '0',
                  RSTA  => rst,
                  CEB   => en,  CLKB  => clk, WEB   => we,
                  CSB0  => '0', CSB1  => '0', CSB2  => '0',
                  RSTB  => rst,                  
                  DIA0  => di(0), DIA1  => di(1), DIA2  => di(2), DIA3  => di(3),
                  DIA4  => di(4), DIA5  => di(5), DIA6  => di(6), DIA7  => di(7),
                  DIA8  => di(8), DIA9  => di(9), DIA10 => di(10), DIA11 => di(11),
                  DIA12 => di(12),DIA13 => di(13),DIA14 => di(14), DIA15 => di(15),
                  DIA16 => di(16),DIA17 => di(17),
                  ADA0  => '1',   ADA1 => '1', ADA2 => '1', ADA3 => '1',
                  ADA4  => addr(0), ADA5 => addr(1), ADA6 => addr(2), ADA7 => addr(3),
                  ADA8  => addr(4), ADA9 => addr(5), ADA10=> addr(6), ADA11=> addr(7),
                  ADA12 => '0',
                  DIB0  => di(18), DIB1  => di(19), DIB2  => di(20), DIB3  => di(21),
                  DIB4  => di(22), DIB5  => di(23), DIB6  => di(24), DIB7  => di(25),
                  DIB8  => di(26), DIB9  => di(27), DIB10 => di(28), DIB11 => di(29),
                  DIB12 => di(30), DIB13 => di(31), DIB14 => di(32), DIB15 => di(33),
                  DIB16 => di(34), DIB17 => di(35),
                  DOA0  => do(0), DOA1  => do(1), DOA2  => do(2), DOA3  => do(3),
                  DOA4  => do(4), DOA5  => do(5), DOA6  => do(6), DOA7  => do(7),
                  DOA8  => do(8), DOA9  => do(9), DOA10 => do(10), DOA11 => do(11),
                  DOA12 => do(12),DOA13 => do(13),DOA14 => do(14), DOA15 => do(15),
                  DOA16 => do(16),DOA17 => do(17),
                  ADB0  => '1',     ADB1 => '1',     ADB2 => '1',     ADB3 => '1',
                  ADB4  => addr(0), ADB5 => addr(1), ADB6 => addr(2), ADB7 => addr(3),
                  ADB8  => addr(4), ADB9 => addr(5), ADB10=> addr(6), ADB11=> addr(7),
                  ADB12 => '1',
                  DOB0  => do(18), DOB1  => do(19), DOB2  => do(20), DOB3  => do(21),
                  DOB4  => do(22), DOB5  => do(23), DOB6  => do(24), DOB7  => do(25),
                  DOB8  => do(26), DOB9  => do(27), DOB10 => do(28), DOB11 => do(29),
                  DOB12 => do(30), DOB13 => do(31), DOB14 => do(32), DOB15 => do(33),
                  DOB16 => do(34), DOB17 => do(35));
end LATTICE_ecp;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library ecp;
use ecp.components.all;
use work.manikconfig.all;
use work.manikpackage.all;

-------------------------------------------------------------------------------
entity ECP_SP8K_S18 is    
    port (do   : out std_logic_vector (17 downto 0);
          addr : in  std_logic_vector (8 downto 0);
          di   : in  std_logic_vector (17 downto 0);
          clk  : in  std_logic;
          en   : in  std_logic;
          we   : in  std_logic;
          rst  : in  std_logic);
end ECP_SP8K_S18;

architecture LATTICE_ecp of ECP_SP8K_S18 is

begin  -- LATTICE_ecp

    SP8KA_1: SP8KA
        generic map (CSDECODE=> "000", GSR=> "ENABLED", WRITEMODE=> "WRITETHROUGH", 
                     RESETMODE=> "ASYNC", REGMODE=> "OUTREG", DATA_WIDTH=>  18)
        port map (CE  => en, CLK => clk, WE  => we,
                  CS0 => '0',CS1 => '0', CS2 => '0',
                  RST => rst,
                  DI0  => di(0), DI1  => di(1), DI2  => di(2), DI3  => di(3),
                  DI4  => di(4), DI5  => di(5), DI6  => di(6), DI7  => di(7),
                  DI8  => di(8), DI9  => di(9), DI10 => di(10),DI11 => di(11),
                  DI12 => di(12),DI13 => di(13),DI14 => di(14),DI15 => di(15),
                  DI16 => di(16),DI17 => di(17),
                  AD0  => '1', AD1  => '1', AD2  => '0', AD3  => '0',
                  AD4  => addr(0), AD5  => addr(1), AD6  => addr(2),
                  AD7  => addr(3), AD8  => addr(4), AD9  => addr(5),
                  AD10 => addr(6), AD11 => addr(7), AD12 => addr(8),
                  DO0  => do(0), DO1  => do(1), DO2  => do(2), DO3  => do(3),
                  DO4  => do(4), DO5  => do(5), DO6  => do(6), DO7  => do(7),
                  DO8  => do(8), DO9  => do(9), DO10 => do(10),DO11 => do(11),
                  DO12 => do(12),DO13 => do(13),DO14 => do(14),DO15 => do(15),
                  DO16 => do(16),DO17 => do(17));

end LATTICE_ecp;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library ecp;
use ecp.components.all;
use work.manikconfig.all;
use work.manikpackage.all;

-------------------------------------------------------------------------------
entity ECP_SP8K_S9 is    
    port (do   : out std_logic_vector (8 downto 0);
          addr : in  std_logic_vector (9 downto 0);
          di   : in  std_logic_vector (8 downto 0);
          clk  : in  std_logic;
          en   : in  std_logic;
          we   : in  std_logic;
          rst  : in  std_logic);
end ECP_SP8K_S9;

architecture LATTICE_ecp of ECP_SP8K_S9 is

begin  -- LATTICE_ecp

    ale9_0_0_3 : SP8KA
        generic map (CSDECODE=> "000", GSR=> "DISABLED", WRITEMODE=> "WRITETHROUGH", 
                     RESETMODE=> "ASYNC", REGMODE=> "NOREG", DATA_WIDTH=>  9)
        port map (CE=>en, CLK=>clk, WE=>we,
                  CS0=>'0', CS1=>'0', CS2=>'0', RST=>rst,
                  DI0=>di(0), DI1=>di(1), DI2=>di(2), DI3=>di(3),
                  DI4=>di(4), DI5=>di(5), DI6=>di(6), DI7=>di(7),
                  DI8=>di(8), DI9=>'0',   DI10=>'0',  DI11=>'0', 
                  DI12=>'0',  DI13=>'0',  DI14=>'0',  DI15=>'0',
                  DI16=>'0',  DI17=>'0', 
                  AD0=>'0', AD1=>'0', AD2=>'0', 
                  AD3=>addr(0), AD4=>addr(1), AD5=>addr(2), 
                  AD6=>addr(3), AD7=>addr(4), AD8=>addr(5), 
                  AD9=>addr(6), AD10=>addr(7), AD11=>addr(8), 
                  AD12=>addr(9),
                  DO0=>do(0), DO1=>do(1), DO2=>do(2), DO3=>do(3), 
                  DO4=>do(4), DO5=>do(5), DO6=>do(6), DO7=>do(7),
                  DO8=>do(8), DO9=>open,  DO10=>open, DO11=>open,
                  DO12=>open, DO13=>open, DO14=>open, DO15=>open,
                  DO16=>open, DO17=>open);

end LATTICE_ecp;


library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library ecp;
use ecp.components.all;
use work.manikconfig.all;
use work.manikpackage.all;

-------------------------------------------------------------------------------
entity ECP_SP8K_S4 is    
    port (do   : out std_logic_vector (3 downto 0);
          addr : in  std_logic_vector (10 downto 0);
          di   : in  std_logic_vector (3 downto 0);
          clk  : in  std_logic;
          en   : in  std_logic;
          we   : in  std_logic;
          rst  : in  std_logic);
end ECP_SP8K_S4;

architecture LATTICE_ecp of ECP_SP8K_S4 is

begin  -- LATTICE_ecp

    ale9 : SP8KA
        generic map (CSDECODE=> "000", GSR=> "DISABLED", WRITEMODE=> "WRITETHROUGH", 
                     RESETMODE=> "ASYNC", REGMODE=> "NOREG", DATA_WIDTH=> 4)
        port map (CE=>en, CLK=>clk, WE=> we,
                  CS0=>'0', CS1=>'0', CS2=>'0', RST=>rst,
                  DI0=>di(0), DI1=>di(1), DI2=>di(2), DI3=>di(3),
                  DI4=>'0', DI5=>'0', DI6=>'0', DI7=>'0', 
                  DI8=>'0', DI9=>'0', DI10=>'0',DI11=>'0',
                  DI12=>'0', DI13=>'0', DI14=>'0', DI15=>'0',
                  DI16=>'0', DI17=>'0',
                  AD0=>'0', AD1=>'0', AD2=>addr(0), AD3=>addr(1),
                  AD4=>addr(2), AD5=>addr(3), AD6=>addr(4), AD7=>addr(5), 
                  AD8=>addr(6), AD9=>addr(7), AD10=>addr(8), 
                  AD11=>addr(9), AD12=>addr(10),
                  DO0=>do(0), DO1=>do(1), DO2=>do(2), DO3=>do(3),
                  DO4=>open, DO5=>open, DO6=>open, DO7=>open,
                  DO8=>open, DO9=>open, DO10=>open, DO11=>open, 
                  DO12=>open, DO13=>open, DO14=>open, DO15=>open,
                  DO16=>open, DO17=>open);

end LATTICE_ecp;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library ecp;
use ecp.components.all;
use work.manikconfig.all;
use work.manikpackage.all;

-------------------------------------------------------------------------------
entity ECP_SP8K_S2 is    
    port (do   : out std_logic_vector (1 downto 0);
          addr : in  std_logic_vector (11 downto 0);
          di   : in  std_logic_vector (1 downto 0);
          clk  : in  std_logic;
          en   : in  std_logic;
          we   : in  std_logic;
          rst  : in  std_logic);
end ECP_SP8K_S2;

architecture LATTICE_ecp of ECP_SP8K_S2 is

begin  -- LATTICE_ecp
    ale9_0_0_15: SP8KA
        generic map (CSDECODE=> "000", GSR=> "DISABLED", WRITEMODE=> "WRITETHROUGH", 
                     RESETMODE=> "ASYNC", REGMODE=> "NOREG", DATA_WIDTH=>  2)
        port map (CE=>en, CLK=>clk, WE=>we,
                  CS0=>'0', CS1=>'0', CS2=>'0',
                  RST=>rst,
                  DI0=>'0', DI1=>di(1), DI2=>'0', DI3=>'0', DI4=>'0', 
                  DI5=>'0', DI6=>'0', 	  DI7=>'0', DI8=>'0', DI9=>'0',
                  DI10=>'0',DI11=>di(0),DI12=>'0',DI13=>'0',DI14=>'0',
                  DI15=>'0',DI16=>'0',    DI17=>'0',
                  AD0=>'0',
                  AD1=>addr(0), AD2=>addr(1), AD3=>addr(2),
                  AD4=>addr(3), AD5=>addr(4), AD6=>addr(5),
                  AD7=>addr(6), AD8=>addr(7), AD9=>addr(8),
                  AD10=>addr(9),AD11=>addr(10),AD12=>addr(11),
                  DO0=>do(0), DO1=>do(1), 
                  DO2=>open,  DO3=>open, DO4=>open,  DO5=>open,  DO6=>open, 
                  DO7=>open,  DO8=>open, DO9=>open,  DO10=>open, DO11=>open, 
                  DO12=>open, DO13=>open,DO14=>open, DO15=>open, DO16=>open, 
                  DO17=>open);
end LATTICE_ecp;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library ecp;
use ecp.components.all;
use work.manikconfig.all;
use work.manikpackage.all;

-------------------------------------------------------------------------------
entity ECP_SP8K_S1 is    
    port (do   : out std_logic_vector (0 downto 0);
          addr : in  std_logic_vector (12 downto 0);
          di   : in  std_logic_vector (0 downto 0);
          clk  : in  std_logic;
          en   : in  std_logic;
          we   : in  std_logic;
          rst  : in  std_logic);
end ECP_SP8K_S1;

architecture LATTICE_ecp of ECP_SP8K_S1 is

begin  -- LATTICE_ecp
    ale9_0_0_31: SP8KA
        generic map (CSDECODE=> "000", GSR=> "DISABLED", WRITEMODE=> "WRITETHROUGH", 
                     RESETMODE=> "ASYNC", REGMODE=> "NOREG", DATA_WIDTH=>  1)
        port map (CE=>en, CLK=>clk, WE=>we,
                  CS0=>'0', CS1=>'0', CS2=>'0', RST=>rst,
                  DI0=>'0', DI1=>'0', DI2=>'0', DI3=>'0', 
                  DI4=>'0', DI5=>'0', DI6=>'0', DI7=>'0',
                  DI8=>'0', DI9=>'0', DI10=>'0', DI11=>di(0), DI12=>'0', 
                  DI13=>'0', DI14=>'0', DI15=>'0', DI16=>'0', DI17=>'0',
                  AD0=>addr(0), AD1=>addr(1), AD2=>addr(2), AD3=>addr(3), 
                  AD4=>addr(4), AD5=>addr(5), AD6=>addr(6), AD7=>addr(7),
                  AD8=>addr(8), AD9=>addr(9), AD10=>addr(10), AD11=>addr(11),
                  AD12=>addr(12), 
                  DO0=>do(0),
                  DO1=>open, DO2=>open, DO3=>open, DO4=>open, 
                  DO5=>open, DO6=>open, DO7=>open, DO8=>open, DO9=>open, 
                  DO10=>open, DO11=>open, DO12=>open, DO13=>open, DO14=>open, 
                  DO15=>open, DO16=>open, DO17=>open);
end LATTICE_ecp;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library ecp;
use ecp.components.all;
use work.manikconfig.all;
use work.manikpackage.all;

entity latspram_ecp is
    
    generic (MEM_DATA_WIDTH : integer := 32;
             MEM_ADDR_WIDTH : integer := 10);

    port (clk    : in  std_logic;
          addr   : in  std_logic_vector(MEM_ADDR_WIDTH-1 downto 0);
          data_i : in  std_logic_vector(MEM_DATA_WIDTH-1 downto 0);
          enb    : in  std_logic;
          rst    : in  std_logic;
          we     : in  std_logic;
          data_o : out std_logic_vector(MEM_DATA_WIDTH-1 downto 0));

end latspram_ecp;

architecture LATTICE_ecp of latspram_ecp is

    component ECP_SP8K_S36
        port (do   : out std_logic_vector (35 downto 0);
              addr : in  std_logic_vector (7 downto 0);
              di   : in  std_logic_vector (35 downto 0);
              clk  : in  std_logic; en   : in  std_logic;
              we   : in  std_logic; rst  : in  std_logic);
    end component;

    component ECP_SP8K_S18
        port (do   : out std_logic_vector (17 downto 0);
              addr : in  std_logic_vector (8 downto 0);
              di   : in  std_logic_vector (17 downto 0);
              clk  : in  std_logic; en   : in  std_logic;
              we   : in  std_logic; rst  : in  std_logic);
    end component;

    component ECP_SP8K_S9
        port (do   : out std_logic_vector (8 downto 0);
              addr : in  std_logic_vector (9 downto 0);
              di   : in  std_logic_vector (8 downto 0);
              clk  : in  std_logic; en   : in  std_logic;
              we   : in  std_logic; rst  : in  std_logic);
    end component;

    component ECP_SP8K_S4
        port (do   : out std_logic_vector (3 downto 0);
              addr : in  std_logic_vector (10 downto 0);
              di   : in  std_logic_vector (3 downto 0);
              clk  : in  std_logic; en   : in  std_logic;
              we   : in  std_logic; rst  : in  std_logic);
    end component;

    component ECP_SP8K_S2
        port (do   : out std_logic_vector (1 downto 0);
              addr : in  std_logic_vector (11 downto 0);
              di   : in  std_logic_vector (1 downto 0);
              clk  : in  std_logic; en   : in  std_logic;
              we   : in  std_logic; rst  : in  std_logic);
    end component;

    component ECP_SP8K_S1
        port (do   : out std_logic_vector (0 downto 0);
              addr : in  std_logic_vector (12 downto 0);
              di   : in  std_logic_vector (0 downto 0);
              clk  : in  std_logic; en   : in  std_logic;
              we   : in  std_logic; rst  : in  std_logic);
    end component;
    
begin  -- LATTICE_ecp

    ALE8: if MEM_ADDR_WIDTH = 8 generate
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,36);
        signal   tdatai, tdatao : std_logic_vector((nblks*36)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(7 downto 0)            := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);

        iblks: for i in 0 to nblks-1 generate
            constant low  : integer := i*36;
            constant high : integer := ((i+1)*36)-1;
        begin
            mem_inst : ECP_SP8K_S36
                port map (DO   => tdatao (high downto low),
                          ADDR => taddr, CLK => clk,
                          DI   => tdatai (high downto low),
                          EN   => enb, rst => rst, WE => we);            
        end generate iblks;
    end generate ALE8;

    ALE9: if MEM_ADDR_WIDTH = 9 generate
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,18);
        signal   tdatai, tdatao : std_logic_vector((nblks*18)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(8 downto 0)            := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);
        
        mem_inst: for i in 0 to nblks-1 generate
            constant low  : integer := i*18;
            constant high : integer := ((i+1)*18)-1;
        begin
            ECP_SP8K_S18_1: ECP_SP8K_S18
                port map (do   => tdatao(high downto low),
                          addr => taddr, clk  => clk,
                          di   => tdatai(high downto low),
                          en   => enb, we => we, rst  => rst);
        end generate mem_inst;
    end generate ALE9;

    ALE10: if MEM_ADDR_WIDTH = 10 generate
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,9);
        signal   tdatai, tdatao : std_logic_vector((nblks*9)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(9 downto 0)           := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);
        
        mem_inst: for i in 0 to nblks-1 generate
            constant low  : integer := i*9;
            constant high : integer := ((i+1)*9)-1;
        begin
            ECP_SP8K_S9_1: ECP_SP8K_S9
                port map (do   => tdatao(high downto low),
                          addr => taddr, clk  => clk,
                          di   => tdatai(high downto low),
                          en   => enb, we => we, rst  => rst);
        end generate mem_inst;        
    end generate ALE10;
    
    ALE11: if MEM_ADDR_WIDTH = 11 generate
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,4);
        signal   tdatai, tdatao : std_logic_vector((nblks*4)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(10 downto 0)           := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);
        
        mem_inst: for i in 0 to nblks-1 generate
            constant low  : integer := i*4;
            constant high : integer := ((i+1)*4)-1;
        begin
            ECP_SP8K_S4_1: ECP_SP8K_S4
                port map (do   => tdatao(high downto low),
                          addr => taddr, clk  => clk,
                          di   => tdatai(high downto low),
                          en   => enb, we => we, rst  => rst);
        end generate mem_inst;        
    end generate ALE11;
    
    ALE12: if MEM_ADDR_WIDTH = 12 generate
        constant nblks          : integer  := round_div(MEM_DATA_WIDTH,2);
        signal   tdatai, tdatao : std_logic_vector((nblks*2)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(11 downto 0)           := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);
        
        mem_inst: for i in 0 to nblks-1 generate
            constant low  : integer := i*2;
            constant high : integer := ((i+1)*2)-1;
        begin
            ECP_SP8K_S2_1: ECP_SP8K_S2
                port map (do   => tdatao(high downto low),
                          addr => taddr, clk  => clk,
                          di   => tdatai(high downto low),
                          en   => enb, we => we, rst  => rst);
        end generate mem_inst;        
    end generate ALE12;
    
    ALE13: if MEM_ADDR_WIDTH = 13 generate
        constant nblks          : integer := MEM_DATA_WIDTH;
        signal   tdatai, tdatao : std_logic_vector((nblks*1)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(12 downto 0)           := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);
        
        mem_inst: for i in 0 to nblks-1 generate
            constant low  : integer := i*1;
            constant high : integer := ((i+1)*1)-1;
        begin
            ECP_SP8K_S1_1: ECP_SP8K_S1
                port map (do   => tdatao(high downto low),
                          addr => taddr, clk  => clk,
                          di   => tdatai(high downto low),
                          en   => enb, we => we, rst  => rst);
        end generate mem_inst;        
    end generate ALE13;
    
end LATTICE_ecp;



library IEEE;
use IEEE.std_logic_1164.all;
-- synopsys translate_off
library ecp;
use ecp.components.all;
-- synopsys translate_on

entity adsu_ecp is
    port (
        DataA: in  std_logic_vector(31 downto 0); 
        DataB: in  std_logic_vector(31 downto 0); 
        Cin: in  std_logic; 
        Add_Sub: in  std_logic; 
        Result: out  std_logic_vector(31 downto 0); 
        Cout: out  std_logic);
end adsu_ecp;

architecture Structure of adsu_ecp is

    -- internal signal declarations
    signal co0: std_logic;
    signal co1: std_logic;
    signal co2: std_logic;
    signal co3: std_logic;
    signal co4: std_logic;
    signal co5: std_logic;
    signal co6: std_logic;
    signal co7: std_logic;
    signal co8: std_logic;
    signal co9: std_logic;
    signal co10: std_logic;
    signal co11: std_logic;
    signal co12: std_logic;
    signal co13: std_logic;
    signal co14: std_logic;

    -- local component declarations
    component FADSU2
        port (A1: in  std_logic; A0: in  std_logic; B1: in  std_logic; 
            B0: in  std_logic; BCI: in  std_logic; CON: in  std_logic; 
            BCO: out  std_logic; S1: out  std_logic; S0: out  std_logic);
    end component;

begin
    -- component instantiation statements
    addsub_0: FADSU2
        port map (A1=>DataA(1), A0=>DataA(0), B1=>DataB(1), B0=>DataB(0), 
            BCI=>Cin, CON=>Add_Sub, BCO=>co0, S1=>Result(1), 
            S0=>Result(0));

    addsub_1: FADSU2
        port map (A1=>DataA(3), A0=>DataA(2), B1=>DataB(3), B0=>DataB(2), 
            BCI=>co0, CON=>Add_Sub, BCO=>co1, S1=>Result(3), 
            S0=>Result(2));

    addsub_2: FADSU2
        port map (A1=>DataA(5), A0=>DataA(4), B1=>DataB(5), B0=>DataB(4), 
            BCI=>co1, CON=>Add_Sub, BCO=>co2, S1=>Result(5), 
            S0=>Result(4));

    addsub_3: FADSU2
        port map (A1=>DataA(7), A0=>DataA(6), B1=>DataB(7), B0=>DataB(6), 
            BCI=>co2, CON=>Add_Sub, BCO=>co3, S1=>Result(7), 
            S0=>Result(6));

    addsub_4: FADSU2
        port map (A1=>DataA(9), A0=>DataA(8), B1=>DataB(9), B0=>DataB(8), 
            BCI=>co3, CON=>Add_Sub, BCO=>co4, S1=>Result(9), 
            S0=>Result(8));

    addsub_5: FADSU2
        port map (A1=>DataA(11), A0=>DataA(10), B1=>DataB(11), 
            B0=>DataB(10), BCI=>co4, CON=>Add_Sub, BCO=>co5, 
            S1=>Result(11), S0=>Result(10));

    addsub_6: FADSU2
        port map (A1=>DataA(13), A0=>DataA(12), B1=>DataB(13), 
            B0=>DataB(12), BCI=>co5, CON=>Add_Sub, BCO=>co6, 
            S1=>Result(13), S0=>Result(12));

    addsub_7: FADSU2
        port map (A1=>DataA(15), A0=>DataA(14), B1=>DataB(15), 
            B0=>DataB(14), BCI=>co6, CON=>Add_Sub, BCO=>co7, 
            S1=>Result(15), S0=>Result(14));

    addsub_8: FADSU2
        port map (A1=>DataA(17), A0=>DataA(16), B1=>DataB(17), 
            B0=>DataB(16), BCI=>co7, CON=>Add_Sub, BCO=>co8, 
            S1=>Result(17), S0=>Result(16));

    addsub_9: FADSU2
        port map (A1=>DataA(19), A0=>DataA(18), B1=>DataB(19), 
            B0=>DataB(18), BCI=>co8, CON=>Add_Sub, BCO=>co9, 
            S1=>Result(19), S0=>Result(18));

    addsub_10: FADSU2
        port map (A1=>DataA(21), A0=>DataA(20), B1=>DataB(21), 
            B0=>DataB(20), BCI=>co9, CON=>Add_Sub, BCO=>co10, 
            S1=>Result(21), S0=>Result(20));

    addsub_11: FADSU2
        port map (A1=>DataA(23), A0=>DataA(22), B1=>DataB(23), 
            B0=>DataB(22), BCI=>co10, CON=>Add_Sub, BCO=>co11, 
            S1=>Result(23), S0=>Result(22));

    addsub_12: FADSU2
        port map (A1=>DataA(25), A0=>DataA(24), B1=>DataB(25), 
            B0=>DataB(24), BCI=>co11, CON=>Add_Sub, BCO=>co12, 
            S1=>Result(25), S0=>Result(24));

    addsub_13: FADSU2
        port map (A1=>DataA(27), A0=>DataA(26), B1=>DataB(27), 
            B0=>DataB(26), BCI=>co12, CON=>Add_Sub, BCO=>co13, 
            S1=>Result(27), S0=>Result(26));

    addsub_14: FADSU2
        port map (A1=>DataA(29), A0=>DataA(28), B1=>DataB(29), 
            B0=>DataB(28), BCI=>co13, CON=>Add_Sub, BCO=>co14, 
            S1=>Result(29), S0=>Result(28));

    addsub_15: FADSU2
        port map (A1=>DataA(31), A0=>DataA(30), B1=>DataB(31), 
            B0=>DataB(30), BCI=>co14, CON=>Add_Sub, BCO=>Cout, 
            S1=>Result(31), S0=>Result(30));

end Structure;

-- synopsys translate_off
library ecp;
configuration Structure_CON of adsu_ecp is
    for Structure
        for all:FADSU2 use entity ecp.FADSU2(V); end for;
    end for;
end Structure_CON;

-- synopsys translate_on

library IEEE;
use IEEE.std_logic_1164.all;
-- synopsys translate_off
library ecp;
use ecp.components.all;
-- synopsys translate_on

entity ecp_pll is
    generic (IN_FREQ_MHZ   : integer    := 25;
             CORE_FREQ_MHZ : integer    := 25;
             CLK_DIVBY     : integer := 1;
             CLK_MULBY     : integer := 1);
    port (CLK   : in  std_logic;
          RESET : in  std_logic;
          CLKOP : out std_logic;
          LOCK  : out std_logic);
end ecp_pll;

architecture Structure of ecp_pll is

    -- internal signal declarations
    signal scuba_vlo : std_logic;
    signal CLKOP_t   : std_logic;
    signal CLK_t     : std_logic;

    -- local component declarations
    component EHXPLLB
        -- synopsys translate_off
        generic (DUTY : in String; PHASEADJ : in String;
        DELAY_CNTL    : in String; CLKOK_DIV : in String;
        FDEL          : in String; CLKFB_DIV : in String;
        CLKOP_DIV     : in String; CLKI_DIV : in String);
        -- synopsys translate_on
        port (CLKI : in  std_logic; CLKFB : in std_logic; RST : in std_logic;
        DDAMODE    : in  std_logic; DDAIZR : in std_logic; DDAILAG : in std_logic;
        DDAIDEL0   : in  std_logic; DDAIDEL1 : in std_logic; DDAIDEL2 : in std_logic;
        CLKOP      : out std_logic; CLKOS : out std_logic; CLKOK : out std_logic;
        LOCK       : out std_logic; DDAOZR : out std_logic; DDAOLAG : out std_logic;
        DDAODEL0   : out std_logic; DDAODEL1 : out std_logic; DDAODEL2 : out std_logic);
    end component;
    component VLO
        port (Z : out std_logic);
    end component;
    attribute DELAY_CNTL                        : string;
    attribute FDEL                              : string;
    attribute DUTY                              : string;
    attribute PHASEADJ                          : string;
    attribute FB_MODE                           : string;
    attribute FREQUENCY_PIN_CLKOS               : string;
    attribute FREQUENCY_PIN_CLKOP               : string;
    attribute FREQUENCY_PIN_CLKI                : string;
    attribute FREQUENCY_PIN_CLKOK               : string;
    attribute CLKOK_DIV                         : string;
    attribute CLKOP_DIV                         : string;
    attribute CLKFB_DIV                         : string;
    attribute CLKI_DIV                          : string;
    attribute FIN                               : string;
    attribute DELAY_CNTL of PLLBInst_0          : label is "STATIC";
    attribute FDEL of PLLBInst_0                : label is "0";
    attribute DUTY of PLLBInst_0                : label is "4";
    attribute PHASEADJ of PLLBInst_0            : label is "0";
    attribute FB_MODE of PLLBInst_0             : label is "CLOCKTREE";
    attribute FREQUENCY_PIN_CLKOS of PLLBInst_0 : label is integer'image(CORE_FREQ_MHZ) & ".0000";
    attribute FREQUENCY_PIN_CLKOP of PLLBInst_0 : label is integer'image(CORE_FREQ_MHZ) & ".0000";
    attribute FREQUENCY_PIN_CLKI of PLLBInst_0  : label is integer'image(IN_FREQ_MHZ)   & ".0000";
    attribute FREQUENCY_PIN_CLKOK of PLLBInst_0 : label is integer'image(CORE_FREQ_MHZ) & ".0000";
    attribute CLKOK_DIV of PLLBInst_0           : label is "2";
    attribute CLKOP_DIV of PLLBInst_0           : label is "16";
    attribute CLKFB_DIV of PLLBInst_0           : label is integer'image(CLK_MULBY);
    attribute CLKI_DIV of PLLBInst_0            : label is integer'image(CLK_DIVBY);
    attribute FIN of PLLBInst_0                 : label is integer'image(IN_FREQ_MHZ)   & ".0000";
    attribute syn_keep                          : boolean;

begin
    -- component instantiation statements
    scuba_vlo_inst : VLO
        port map (Z => scuba_vlo);

    PLLBInst_0 : EHXPLLB
        -- synopsys translate_off
        generic map (DELAY_CNTL => "STATIC", FDEL => "0", DUTY => "4",
                     PHASEADJ   => "0", CLKOK_DIV => "2", CLKOP_DIV => "16", CLKFB_DIV => integer'image(CLK_MULBY),
                     CLKI_DIV   => integer'image(CLK_DIVBY))
        -- synopsys translate_on
        port map (CLKI     => CLK_t, CLKFB => CLKOP_t, RST => RESET,
                  DDAMODE  => scuba_vlo, DDAIZR => scuba_vlo, DDAILAG => scuba_vlo,
                  DDAIDEL0 => scuba_vlo, DDAIDEL1 => scuba_vlo,
                  DDAIDEL2 => scuba_vlo, CLKOP => CLKOP_t, CLKOS => open,
                  CLKOK    => open, LOCK => LOCK, DDAOZR => open, DDAOLAG => open,
                  DDAODEL0 => open, DDAODEL1 => open, DDAODEL2 => open);

    CLKOP <= CLKOP_t;
    CLK_t <= CLK;
end Structure;

-- synopsys translate_off
library ecp;
configuration Structure_CON of ecp_pll is
    for Structure
        for all : EHXPLLB use entity ecp.EHXPLLB(V); end for;
        for all : VLO use entity ecp.VLO(V); end for;
    end for;
end Structure_CON;

-- synopsys translate_on

-------------------------------------------------------------------------------
--                                    ECP2 Related 
-------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
-- synopsys translate_off
library ecp2;
use ecp2.components.all;
-- synopsys translate_on

entity ECP2_SP16K_S36 is
    port (do   : out std_logic_vector (35 downto 0);
          addr : in  std_logic_vector (8 downto 0);
          di   : in  std_logic_vector (35 downto 0);
          clk  : in  std_logic;
          en   : in  std_logic;
          we   : in  std_logic;
          rst  : in  std_logic);
end ECP2_SP16K_S36;

architecture rtl of ECP2_SP16K_S36 is
    component DP16KB
        generic (GSR          : in String; WRITEMODE_B  : in String;
                 CSDECODE_B   : in std_logic_vector(2 downto 0);
                 CSDECODE_A   : in std_logic_vector(2 downto 0);
                 WRITEMODE_A  : in String; RESETMODE    : in String;
                 REGMODE_B    : in String; REGMODE_A    : in String;
                 DATA_WIDTH_B : in Integer; DATA_WIDTH_A : in Integer);
        port (DIA0  : in  std_logic; DIA1  : in  std_logic; DIA2  : in  std_logic; DIA3  : in  std_logic;
              DIA4  : in  std_logic; DIA5  : in  std_logic; DIA6  : in  std_logic; DIA7  : in  std_logic;
              DIA8  : in  std_logic; DIA9  : in  std_logic; DIA10 : in  std_logic; DIA11 : in  std_logic;
              DIA12 : in  std_logic; DIA13 : in  std_logic; DIA14 : in  std_logic; DIA15 : in  std_logic;
              DIA16 : in  std_logic; DIA17 : in  std_logic;
              ADA0  : in  std_logic; ADA1  : in  std_logic; ADA2  : in  std_logic; ADA3  : in  std_logic;
              ADA4  : in  std_logic; ADA5  : in  std_logic; ADA6  : in  std_logic; ADA7  : in  std_logic;
              ADA8  : in  std_logic; ADA9  : in  std_logic; ADA10 : in  std_logic; ADA11 : in  std_logic;
              ADA12 : in  std_logic; ADA13 : in  std_logic;
              CEA   : in  std_logic; CLKA  : in  std_logic; WEA   : in  std_logic;
              CSA0  : in  std_logic; CSA1  : in  std_logic; CSA2  : in  std_logic;
              RSTA  : in  std_logic;
              DIB0  : in  std_logic; DIB1  : in  std_logic; DIB2  : in  std_logic; DIB3  : in  std_logic;
              DIB4  : in  std_logic; DIB5  : in  std_logic; DIB6  : in  std_logic; DIB7  : in  std_logic;
              DIB8  : in  std_logic; DIB9  : in  std_logic; DIB10 : in  std_logic; DIB11 : in  std_logic;
              DIB12 : in  std_logic; DIB13 : in  std_logic; DIB14 : in  std_logic; DIB15 : in  std_logic;
              DIB16 : in  std_logic; DIB17 : in  std_logic;
              ADB0  : in  std_logic; ADB1  : in  std_logic; ADB2  : in  std_logic; ADB3  : in  std_logic;
              ADB4  : in  std_logic; ADB5  : in  std_logic; ADB6  : in  std_logic; ADB7  : in  std_logic;
              ADB8  : in  std_logic; ADB9  : in  std_logic; ADB10 : in  std_logic; ADB11 : in  std_logic;
              ADB12 : in  std_logic; ADB13 : in  std_logic;
              CEB   : in  std_logic; CLKB  : in  std_logic; WEB   : in  std_logic;
              CSB0  : in  std_logic; CSB1  : in  std_logic; CSB2  : in  std_logic;
              RSTB  : in  std_logic;
              DOA0  : out std_logic; DOA1  : out std_logic; DOA2  : out std_logic; DOA3  : out std_logic;
              DOA4  : out std_logic; DOA5  : out std_logic; DOA6  : out std_logic; DOA7  : out std_logic;
              DOA8  : out std_logic; DOA9  : out std_logic; DOA10 : out std_logic; DOA11 : out std_logic;
              DOA12 : out std_logic; DOA13 : out std_logic; DOA14 : out std_logic; DOA15 : out std_logic;
              DOA16 : out std_logic; DOA17 : out std_logic;
              DOB0  : out std_logic; DOB1  : out std_logic; DOB2  : out std_logic; DOB3  : out std_logic;
              DOB4  : out std_logic; DOB5  : out std_logic; DOB6  : out std_logic; DOB7  : out std_logic;
              DOB8  : out std_logic; DOB9  : out std_logic; DOB10 : out std_logic; DOB11 : out std_logic;
              DOB12 : out std_logic; DOB13 : out std_logic; DOB14 : out std_logic; DOB15 : out std_logic;
              DOB16 : out std_logic; DOB17 : out std_logic);
    end component;
begin  -- rtl

    ecp2_mem_0_0_0: DP16KB
        generic map (CSDECODE_B=> "000", CSDECODE_A=> "000", WRITEMODE_B=> "WRITETHROUGH", 
                     WRITEMODE_A=> "WRITETHROUGH", GSR=> "DISABLED", RESETMODE=> "ASYNC", 
                     REGMODE_B=> "NOREG", REGMODE_A=> "NOREG", DATA_WIDTH_B=>  18, 
                     DATA_WIDTH_A=>  18)
        -- synopsys translate_on
        port map (DIA0=>di(0), DIA1=>di(1), DIA2=>di(2), DIA3=>di(3), DIA4=>di(4), DIA5=>di(5), DIA6=>di(6), 
                  DIA7=>di(7), DIA8=>di(8), DIA9=>di(9), DIA10=>di(10), DIA11=>di(11), DIA12=>di(12),
                  DIA13=>di(13), DIA14=>di(14), DIA15=>di(15), DIA16=>di(16), DIA17=>di(17),
                  ADA0=>'1', ADA1=>'1', ADA2=>'0', ADA3=>'0', ADA4=>addr(0), ADA5=>addr(1), ADA6=>addr(2),
                  ADA7=>addr(3), ADA8=>addr(4), ADA9=>addr(5), ADA10=>addr(6), ADA11=>addr(7), ADA12=>addr(8),
                  ADA13=>'0', 
                  CEA=>en, CLKA=>clk, WEA=>we, CSA0=>'0', CSA1=>'0', CSA2=>'0', RSTA=>rst, 
                  DIB0=>di(18), DIB1=>di(19), DIB2=>di(20), DIB3=>di(21), DIB4=>di(22), DIB5=>di(23), 
                  DIB6=>di(24), DIB7=>di(25), DIB8=>di(26), DIB9=>di(27), DIB10=>di(28), DIB11=>di(29), 
                  DIB12=>di(30), DIB13=>di(31), DIB14=>di(32), DIB15=>di(33), DIB16=>di(34), DIB17=>di(35), 
                  ADB0=>'1', ADB1=>'1', ADB2=>'0', ADB3=>'0',
                  ADB4=>addr(0), ADB5=>addr(1), ADB6=>addr(2), ADB7=>addr(3), ADB8=>addr(4), ADB9=>addr(5),
                  ADB10=>addr(6), ADB11=>addr(7), ADB12=>addr(8), ADB13=>'1',
                  CEB=>en, 
                  CLKB=>clk, WEB=>we, CSB0=>'0', CSB1=>'0', CSB2=>'0', RSTB=>rst,
                  DOA0=>do(0), DOA1=>do(1), DOA2=>do(2), DOA3=>do(3), DOA4=>do(4), DOA5=>do(5), DOA6=>do(6), 
                  DOA7=>do(7), DOA8=>do(8), DOA9=>do(9), DOA10=>do(10), DOA11=>do(11), DOA12=>do(12),
                  DOA13=>do(13), DOA14=>do(14), DOA15=>do(15), DOA16=>do(16), DOA17=>do(17), DOB0=>do(18), 
                  DOB1=>do(19), DOB2=>do(20), DOB3=>do(21), DOB4=>do(22), DOB5=>do(23), DOB6=>do(24),
                  DOB7=>do(25), DOB8=>do(26), DOB9=>do(27), DOB10=>do(28), DOB11=>do(29), DOB12=>do(30), 
                  DOB13=>do(31), DOB14=>do(32), DOB15=>do(33), DOB16=>do(34), DOB17=>do(35));
end rtl;

library IEEE;
use IEEE.std_logic_1164.all;
-- synopsys translate_off
library ecp2;
use ecp2.components.all;
-- synopsys translate_on

entity ECP2_SP16K_S18 is
    port (do   : out std_logic_vector (17 downto 0);
          addr : in  std_logic_vector (9 downto 0);
          di   : in  std_logic_vector (17 downto 0);
          clk  : in  std_logic;
          en   : in  std_logic;
          we   : in  std_logic;
          rst  : in  std_logic);
end ECP2_SP16K_S18;

architecture rtl of ECP2_SP16K_S18 is
    component DP16KB
        generic (GSR          : in String; WRITEMODE_B  : in String;
                 CSDECODE_B   : in std_logic_vector(2 downto 0);
                 CSDECODE_A   : in std_logic_vector(2 downto 0);
                 WRITEMODE_A  : in String; RESETMODE    : in String;
                 REGMODE_B    : in String; REGMODE_A    : in String;
                 DATA_WIDTH_B : in Integer; DATA_WIDTH_A : in Integer);
        port (DIA0  : in  std_logic; DIA1  : in  std_logic; DIA2  : in  std_logic; DIA3  : in  std_logic;
              DIA4  : in  std_logic; DIA5  : in  std_logic; DIA6  : in  std_logic; DIA7  : in  std_logic;
              DIA8  : in  std_logic; DIA9  : in  std_logic; DIA10 : in  std_logic; DIA11 : in  std_logic;
              DIA12 : in  std_logic; DIA13 : in  std_logic; DIA14 : in  std_logic; DIA15 : in  std_logic;
              DIA16 : in  std_logic; DIA17 : in  std_logic;
              ADA0  : in  std_logic; ADA1  : in  std_logic; ADA2  : in  std_logic; ADA3  : in  std_logic;
              ADA4  : in  std_logic; ADA5  : in  std_logic; ADA6  : in  std_logic; ADA7  : in  std_logic;
              ADA8  : in  std_logic; ADA9  : in  std_logic; ADA10 : in  std_logic; ADA11 : in  std_logic;
              ADA12 : in  std_logic; ADA13 : in  std_logic;
              CEA   : in  std_logic; CLKA  : in  std_logic; WEA   : in  std_logic;
              CSA0  : in  std_logic; CSA1  : in  std_logic; CSA2  : in  std_logic;
              RSTA  : in  std_logic;
              DIB0  : in  std_logic; DIB1  : in  std_logic; DIB2  : in  std_logic; DIB3  : in  std_logic;
              DIB4  : in  std_logic; DIB5  : in  std_logic; DIB6  : in  std_logic; DIB7  : in  std_logic;
              DIB8  : in  std_logic; DIB9  : in  std_logic; DIB10 : in  std_logic; DIB11 : in  std_logic;
              DIB12 : in  std_logic; DIB13 : in  std_logic; DIB14 : in  std_logic; DIB15 : in  std_logic;
              DIB16 : in  std_logic; DIB17 : in  std_logic;
              ADB0  : in  std_logic; ADB1  : in  std_logic; ADB2  : in  std_logic; ADB3  : in  std_logic;
              ADB4  : in  std_logic; ADB5  : in  std_logic; ADB6  : in  std_logic; ADB7  : in  std_logic;
              ADB8  : in  std_logic; ADB9  : in  std_logic; ADB10 : in  std_logic; ADB11 : in  std_logic;
              ADB12 : in  std_logic; ADB13 : in  std_logic;
              CEB   : in  std_logic; CLKB  : in  std_logic; WEB   : in  std_logic;
              CSB0  : in  std_logic; CSB1  : in  std_logic; CSB2  : in  std_logic;
              RSTB  : in  std_logic;
              DOA0  : out std_logic; DOA1  : out std_logic; DOA2  : out std_logic; DOA3  : out std_logic;
              DOA4  : out std_logic; DOA5  : out std_logic; DOA6  : out std_logic; DOA7  : out std_logic;
              DOA8  : out std_logic; DOA9  : out std_logic; DOA10 : out std_logic; DOA11 : out std_logic;
              DOA12 : out std_logic; DOA13 : out std_logic; DOA14 : out std_logic; DOA15 : out std_logic;
              DOA16 : out std_logic; DOA17 : out std_logic;
              DOB0  : out std_logic; DOB1  : out std_logic; DOB2  : out std_logic; DOB3  : out std_logic;
              DOB4  : out std_logic; DOB5  : out std_logic; DOB6  : out std_logic; DOB7  : out std_logic;
              DOB8  : out std_logic; DOB9  : out std_logic; DOB10 : out std_logic; DOB11 : out std_logic;
              DOB12 : out std_logic; DOB13 : out std_logic; DOB14 : out std_logic; DOB15 : out std_logic;
              DOB16 : out std_logic; DOB17 : out std_logic);
    end component;
begin  -- rtl

    ecp2_mem_0_0_0: DP16KB
        generic map (CSDECODE_B=> "111", CSDECODE_A=> "000", WRITEMODE_B=> "NORMAL", 
                     WRITEMODE_A=> "WRITETHROUGH", GSR=> "DISABLED", RESETMODE=> "ASYNC", 
                     REGMODE_B=> "NOREG", REGMODE_A=> "NOREG", DATA_WIDTH_B=>  18, 
                     DATA_WIDTH_A=>  18)
        port map (DIA0=>di(0), DIA1=>di(1), DIA2=>di(2), DIA3=>di(3), DIA4=>di(4), DIA5=>di(5),
                  DIA6=>di(6), DIA7=>di(7), DIA8=>di(8), DIA9=>di(9), DIA10=>di(10), DIA11=>di(11),
                  DIA12=>di(12), DIA13=>di(13), DIA14=>di(14), DIA15=>di(15), DIA16=>di(16), DIA17=>di(17),
                  ADA0=>'1', ADA1=>'1', ADA2=>'0', ADA3=>'0', ADA4=>addr(0), ADA5=>addr(1), ADA6=>addr(2),
                  ADA7=>addr(3), ADA8=>addr(4), ADA9=>addr(5), ADA10=>addr(6), ADA11=>addr(7), ADA12=>addr(8),
                  ADA13=>addr(9), CEA=>en, CLKA=>clk, WEA=>we, CSA0=>'0', CSA1=>'0', CSA2=>'0', RSTA=>rst,
                  DIB0=>di(0), DIB1=>di(1), DIB2=>di(2), DIB3=>di(3), DIB4=>di(4), DIB5=>di(5), DIB6=>di(6),
                  DIB7=>di(7), DIB8=>di(8), DIB9=>di(9), DIB10=>di(10), DIB11=>di(11), DIB12=>di(12), DIB13=>di(13),
                  DIB14=>di(14), DIB15=>di(15), DIB16=>di(16), DIB17=>di(17), 
                  ADB0=>'0', ADB1=>'0', ADB2=>'0', ADB3=>'0', ADB4=>'0', ADB5=>'0', 
                  ADB6=>'0', ADB7=>'0', ADB8=>'0', ADB9=>'0', ADB10=>'0', ADB11=>'0', ADB12=>'0', ADB13=>'0', CEB=>'1', 
                  CLKB=>'0', WEB=>'0', CSB0=>'0', CSB1=>'0', CSB2=>'0', RSTB=>'0',                  
                  DOA0=>do(0), DOA1=>do(1), DOA2=>do(2), DOA3=>do(3), DOA4=>do(4), 
                  DOA5=>do(5), DOA6=>do(6), DOA7=>do(7), DOA8=>do(8), DOA9=>do(9), 
                  DOA10=>do(10), DOA11=>do(11), DOA12=>do(12), DOA13=>do(13), 
                  DOA14=>do(14), DOA15=>do(15), DOA16=>do(16), DOA17=>do(17), 
                  DOB0=>open, DOB1=>open, DOB2=>open, DOB3=>open, DOB4=>open, DOB5=>open, DOB6=>open, DOB7=>open,
                  DOB8=>open, DOB9=>open, DOB10=>open, DOB11=>open, DOB12=>open, DOB13=>open, DOB14=>open, DOB15=>open,
                  DOB16=>open, DOB17=>open);

end rtl;

library IEEE;
use IEEE.std_logic_1164.all;
-- synopsys translate_off
library ecp2;
use ecp2.components.all;
-- synopsys translate_on

entity ECP2_SP16K_S9 is
    port (do   : out std_logic_vector (8 downto 0);
          addr : in  std_logic_vector (10 downto 0);
          di   : in  std_logic_vector (8 downto 0);
          clk  : in  std_logic;
          en   : in  std_logic;
          we   : in  std_logic;
          rst  : in  std_logic);
end ECP2_SP16K_S9;

architecture rtl of ECP2_SP16K_S9 is
    component DP16KB
        generic (GSR          : in String; WRITEMODE_B  : in String;
                 CSDECODE_B   : in std_logic_vector(2 downto 0);
                 CSDECODE_A   : in std_logic_vector(2 downto 0);
                 WRITEMODE_A  : in String; RESETMODE    : in String;
                 REGMODE_B    : in String; REGMODE_A    : in String;
                 DATA_WIDTH_B : in Integer; DATA_WIDTH_A : in Integer);
        port (DIA0  : in  std_logic; DIA1  : in  std_logic; DIA2  : in  std_logic; DIA3  : in  std_logic;
              DIA4  : in  std_logic; DIA5  : in  std_logic; DIA6  : in  std_logic; DIA7  : in  std_logic;
              DIA8  : in  std_logic; DIA9  : in  std_logic; DIA10 : in  std_logic; DIA11 : in  std_logic;
              DIA12 : in  std_logic; DIA13 : in  std_logic; DIA14 : in  std_logic; DIA15 : in  std_logic;
              DIA16 : in  std_logic; DIA17 : in  std_logic;
              ADA0  : in  std_logic; ADA1  : in  std_logic; ADA2  : in  std_logic; ADA3  : in  std_logic;
              ADA4  : in  std_logic; ADA5  : in  std_logic; ADA6  : in  std_logic; ADA7  : in  std_logic;
              ADA8  : in  std_logic; ADA9  : in  std_logic; ADA10 : in  std_logic; ADA11 : in  std_logic;
              ADA12 : in  std_logic; ADA13 : in  std_logic;
              CEA   : in  std_logic; CLKA  : in  std_logic; WEA   : in  std_logic;
              CSA0  : in  std_logic; CSA1  : in  std_logic; CSA2  : in  std_logic;
              RSTA  : in  std_logic;
              DIB0  : in  std_logic; DIB1  : in  std_logic; DIB2  : in  std_logic; DIB3  : in  std_logic;
              DIB4  : in  std_logic; DIB5  : in  std_logic; DIB6  : in  std_logic; DIB7  : in  std_logic;
              DIB8  : in  std_logic; DIB9  : in  std_logic; DIB10 : in  std_logic; DIB11 : in  std_logic;
              DIB12 : in  std_logic; DIB13 : in  std_logic; DIB14 : in  std_logic; DIB15 : in  std_logic;
              DIB16 : in  std_logic; DIB17 : in  std_logic;
              ADB0  : in  std_logic; ADB1  : in  std_logic; ADB2  : in  std_logic; ADB3  : in  std_logic;
              ADB4  : in  std_logic; ADB5  : in  std_logic; ADB6  : in  std_logic; ADB7  : in  std_logic;
              ADB8  : in  std_logic; ADB9  : in  std_logic; ADB10 : in  std_logic; ADB11 : in  std_logic;
              ADB12 : in  std_logic; ADB13 : in  std_logic;
              CEB   : in  std_logic; CLKB  : in  std_logic; WEB   : in  std_logic;
              CSB0  : in  std_logic; CSB1  : in  std_logic; CSB2  : in  std_logic;
              RSTB  : in  std_logic;
              DOA0  : out std_logic; DOA1  : out std_logic; DOA2  : out std_logic; DOA3  : out std_logic;
              DOA4  : out std_logic; DOA5  : out std_logic; DOA6  : out std_logic; DOA7  : out std_logic;
              DOA8  : out std_logic; DOA9  : out std_logic; DOA10 : out std_logic; DOA11 : out std_logic;
              DOA12 : out std_logic; DOA13 : out std_logic; DOA14 : out std_logic; DOA15 : out std_logic;
              DOA16 : out std_logic; DOA17 : out std_logic;
              DOB0  : out std_logic; DOB1  : out std_logic; DOB2  : out std_logic; DOB3  : out std_logic;
              DOB4  : out std_logic; DOB5  : out std_logic; DOB6  : out std_logic; DOB7  : out std_logic;
              DOB8  : out std_logic; DOB9  : out std_logic; DOB10 : out std_logic; DOB11 : out std_logic;
              DOB12 : out std_logic; DOB13 : out std_logic; DOB14 : out std_logic; DOB15 : out std_logic;
              DOB16 : out std_logic; DOB17 : out std_logic);
    end component;

begin  -- rtl

    ecp2_mem_0_0_0: DP16KB
        generic map (CSDECODE_B=> "111", CSDECODE_A=> "000", WRITEMODE_B=> "NORMAL", 
                     WRITEMODE_A=> "WRITETHROUGH", GSR=> "DISABLED", RESETMODE=> "ASYNC", 
                     REGMODE_B=> "NOREG", REGMODE_A=> "NOREG", DATA_WIDTH_B=>  9, 
                     DATA_WIDTH_A=>  9)
        port map (DIA0=>di(0), DIA1=>di(1), DIA2=>di(2), DIA3=>di(3), DIA4=>di(4), DIA5=>di(5), DIA6=>di(6), 
                  DIA7=>di(7), DIA8=>di(8), DIA9=>'0', DIA10=>'0', DIA11=>'0', DIA12=>'0', DIA13=>'0', DIA14=>'0',
                  DIA15=>'0', DIA16=>'0', DIA17=>'0',
                  ADA0=>'0', ADA1=>'0', ADA2=>'0', ADA3=>addr(0), ADA4=>addr(1), ADA5=>addr(2), ADA6=>addr(3), 
                  ADA7=>addr(4), ADA8=>addr(5), ADA9=>addr(6), ADA10=>addr(7), ADA11=>addr(8), ADA12=>addr(9), 
                  ADA13=>addr(10), CEA=>en, CLKA=>clk, WEA=>we, CSA0=>'0', CSA1=>'0', CSA2=>'0', RSTA=>rst,
                  DIB0=>di(0), DIB1=>di(1), DIB2=>di(2), DIB3=>di(3), DIB4=>di(4), DIB5=>di(5), DIB6=>di(6), 
                  DIB7=>di(7), DIB8=>di(8), DIB9=>'0', DIB10=>'0', DIB11=>'0', DIB12=>'0', DIB13=>'0', DIB14=>'0',
                  DIB15=>'0', DIB16=>'0', DIB17=>'0', ADB0=>'0', ADB1=>'0', ADB2=>'0', ADB3=>'0', ADB4=>'0', ADB5=>'0',
                  ADB6=>'0', ADB7=>'0', ADB8=>'0', ADB9=>'0', ADB10=>'0', ADB11=>'0', ADB12=>'0', ADB13=>'0', CEB=>'1',
                  CLKB=>'0', WEB=>'0', CSB0=>'0', CSB1=>'0', CSB2=>'0', RSTB=>'0', DOA0=>do(0), DOA1=>do(1), 
                  DOA2=>do(2), DOA3=>do(3), DOA4=>do(4), DOA5=>do(5), DOA6=>do(6), DOA7=>do(7), DOA8=>do(8),
                  DOA9=>open, DOA10=>open, DOA11=>open, DOA12=>open, DOA13=>open, DOA14=>open, DOA15=>open, 
                  DOA16=>open, DOA17=>open, DOB0=>open, DOB1=>open, DOB2=>open, DOB3=>open, DOB4=>open,
                  DOB5=>open, DOB6=>open, DOB7=>open, DOB8=>open, DOB9=>open, DOB10=>open, DOB11=>open, 
                  DOB12=>open, DOB13=>open, DOB14=>open, DOB15=>open, DOB16=>open, DOB17=>open);
end rtl;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

-- synopsys translate_off
library ecp2;
use ecp2.components.all;
-- synopsys translate_on
use work.manikconfig.all;
use work.manikpackage.all;

-------------------------------------------------------------------------------
entity ECP2_SP16K_S4 is    
    port (do   : out std_logic_vector (3 downto 0);
          addr : in  std_logic_vector (11 downto 0);
          di   : in  std_logic_vector (3 downto 0);
          clk  : in  std_logic;
          en   : in  std_logic;
          we   : in  std_logic;
          rst  : in  std_logic);
end ECP2_SP16K_S4;

architecture rtl of ECP2_SP16K_S4 is
    component DP16KB
        generic (GSR          : in String; WRITEMODE_B  : in String;
                 CSDECODE_B   : in std_logic_vector(2 downto 0);
                 CSDECODE_A   : in std_logic_vector(2 downto 0);
                 WRITEMODE_A  : in String; RESETMODE    : in String;
                 REGMODE_B    : in String; REGMODE_A    : in String;
                 DATA_WIDTH_B : in Integer; DATA_WIDTH_A : in Integer);
        port (DIA0  : in  std_logic; DIA1  : in  std_logic; DIA2  : in  std_logic; DIA3  : in  std_logic;
              DIA4  : in  std_logic; DIA5  : in  std_logic; DIA6  : in  std_logic; DIA7  : in  std_logic;
              DIA8  : in  std_logic; DIA9  : in  std_logic; DIA10 : in  std_logic; DIA11 : in  std_logic;
              DIA12 : in  std_logic; DIA13 : in  std_logic; DIA14 : in  std_logic; DIA15 : in  std_logic;
              DIA16 : in  std_logic; DIA17 : in  std_logic;
              ADA0  : in  std_logic; ADA1  : in  std_logic; ADA2  : in  std_logic; ADA3  : in  std_logic;
              ADA4  : in  std_logic; ADA5  : in  std_logic; ADA6  : in  std_logic; ADA7  : in  std_logic;
              ADA8  : in  std_logic; ADA9  : in  std_logic; ADA10 : in  std_logic; ADA11 : in  std_logic;
              ADA12 : in  std_logic; ADA13 : in  std_logic;
              CEA   : in  std_logic; CLKA  : in  std_logic; WEA   : in  std_logic;
              CSA0  : in  std_logic; CSA1  : in  std_logic; CSA2  : in  std_logic;
              RSTA  : in  std_logic;
              DIB0  : in  std_logic; DIB1  : in  std_logic; DIB2  : in  std_logic; DIB3  : in  std_logic;
              DIB4  : in  std_logic; DIB5  : in  std_logic; DIB6  : in  std_logic; DIB7  : in  std_logic;
              DIB8  : in  std_logic; DIB9  : in  std_logic; DIB10 : in  std_logic; DIB11 : in  std_logic;
              DIB12 : in  std_logic; DIB13 : in  std_logic; DIB14 : in  std_logic; DIB15 : in  std_logic;
              DIB16 : in  std_logic; DIB17 : in  std_logic;
              ADB0  : in  std_logic; ADB1  : in  std_logic; ADB2  : in  std_logic; ADB3  : in  std_logic;
              ADB4  : in  std_logic; ADB5  : in  std_logic; ADB6  : in  std_logic; ADB7  : in  std_logic;
              ADB8  : in  std_logic; ADB9  : in  std_logic; ADB10 : in  std_logic; ADB11 : in  std_logic;
              ADB12 : in  std_logic; ADB13 : in  std_logic;
              CEB   : in  std_logic; CLKB  : in  std_logic; WEB   : in  std_logic;
              CSB0  : in  std_logic; CSB1  : in  std_logic; CSB2  : in  std_logic;
              RSTB  : in  std_logic;
              DOA0  : out std_logic; DOA1  : out std_logic; DOA2  : out std_logic; DOA3  : out std_logic;
              DOA4  : out std_logic; DOA5  : out std_logic; DOA6  : out std_logic; DOA7  : out std_logic;
              DOA8  : out std_logic; DOA9  : out std_logic; DOA10 : out std_logic; DOA11 : out std_logic;
              DOA12 : out std_logic; DOA13 : out std_logic; DOA14 : out std_logic; DOA15 : out std_logic;
              DOA16 : out std_logic; DOA17 : out std_logic;
              DOB0  : out std_logic; DOB1  : out std_logic; DOB2  : out std_logic; DOB3  : out std_logic;
              DOB4  : out std_logic; DOB5  : out std_logic; DOB6  : out std_logic; DOB7  : out std_logic;
              DOB8  : out std_logic; DOB9  : out std_logic; DOB10 : out std_logic; DOB11 : out std_logic;
              DOB12 : out std_logic; DOB13 : out std_logic; DOB14 : out std_logic; DOB15 : out std_logic;
              DOB16 : out std_logic; DOB17 : out std_logic);
    end component;

begin 

    ecp2_mem_0_0_0: DP16KB
        generic map (CSDECODE_B=> "111", CSDECODE_A=> "000", WRITEMODE_B=> "NORMAL", 
                     WRITEMODE_A=> "WRITETHROUGH", GSR=> "DISABLED", RESETMODE=> "ASYNC", 
                     REGMODE_B=> "NOREG", REGMODE_A=> "NOREG", DATA_WIDTH_B=>  4, 
                     DATA_WIDTH_A=>  4)
        port map (DIA0=>di(0), DIA1=>di(1), DIA2=>di(2), DIA3=>di(3), DIA4=>'0', DIA5=>'0', 
                  DIA6=>'0', DIA7=>'0', DIA8=>'0', DIA9=>'0', DIA10=>'0', DIA11=>'0', 
                  DIA12=>'0', DIA13=>'0', DIA14=>'0', DIA15=>'0', DIA16=>'0', DIA17=>'0', 
                  ADA0=>'0', ADA1=>'0', ADA2=>addr(0), ADA3=>addr(1), ADA4=>addr(2), ADA5=>addr(3), 
                  ADA6=>addr(4), ADA7=>addr(5), ADA8=>addr(6), ADA9=>addr(7), ADA10=>addr(8), ADA11=>addr(9), 
                  ADA12=>addr(10), ADA13=>addr(11), CEA=>en, CLKA=>clk, WEA=>WE, CSA0=>'0', CSA1=>'0', 
                  CSA2=>'0', RSTA=>rst, DIB0=>di(0), DIB1=>di(1), DIB2=>di(2), DIB3=>di(3), DIB4=>'0', 
                  DIB5=>'0', DIB6=>'0', DIB7=>'0', DIB8=>'0', DIB9=>'0', DIB10=>'0', DIB11=>'0', DIB12=>'0',
                  DIB13=>'0', DIB14=>'0', DIB15=>'0', DIB16=>'0', DIB17=>'0', ADB0=>'0', ADB1=>'0', 
                  ADB2=>'0', ADB3=>'0', ADB4=>'0', ADB5=>'0', ADB6=>'0', ADB7=>'0', ADB8=>'0', ADB9=>'0',
                  ADB10=>'0', ADB11=>'0', ADB12=>'0', ADB13=>'0', CEB=>'1', CLKB=>'0', WEB=>'0', 
                  CSB0=>'0', CSB1=>'0', CSB2=>'0', 
                  RSTB=>'0', DOA0=>do(0), DOA1=>do(1), DOA2=>do(2), DOA3=>do(3), DOA4=>open, DOA5=>open,
                  DOA6=>open, DOA7=>open, DOA8=>open, DOA9=>open, DOA10=>open, DOA11=>open, 
                  DOA12=>open, DOA13=>open, DOA14=>open, DOA15=>open, DOA16=>open, DOA17=>open, DOB0=>open,
                  DOB1=>open, DOB2=>open, DOB3=>open, DOB4=>open, DOB5=>open, DOB6=>open, DOB7=>open, 
                  DOB8=>open, DOB9=>open, DOB10=>open, DOB11=>open, DOB12=>open, DOB13=>open, DOB14=>open,
                  DOB15=>open, DOB16=>open, DOB17=>open);
end rtl;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

-- synopsys translate_off
library ecp2;
use ecp2.components.all;
-- synopsys translate_on
use work.manikconfig.all;
use work.manikpackage.all;

-------------------------------------------------------------------------------
entity ECP2_SP16K_S2 is    
    port (do   : out std_logic_vector (1 downto 0);
          addr : in  std_logic_vector (12 downto 0);
          di   : in  std_logic_vector (1 downto 0);
          clk  : in  std_logic;
          en   : in  std_logic;
          we   : in  std_logic;
          rst  : in  std_logic);
end ECP2_SP16K_S2;

architecture rtl of ECP2_SP16K_S2 is
    component DP16KB
        generic (GSR          : in String; WRITEMODE_B  : in String;
                 CSDECODE_B   : in std_logic_vector(2 downto 0);
                 CSDECODE_A   : in std_logic_vector(2 downto 0);
                 WRITEMODE_A  : in String; RESETMODE    : in String;
                 REGMODE_B    : in String; REGMODE_A    : in String;
                 DATA_WIDTH_B : in Integer; DATA_WIDTH_A : in Integer);
        port (DIA0  : in  std_logic; DIA1  : in  std_logic; DIA2  : in  std_logic; DIA3  : in  std_logic;
              DIA4  : in  std_logic; DIA5  : in  std_logic; DIA6  : in  std_logic; DIA7  : in  std_logic;
              DIA8  : in  std_logic; DIA9  : in  std_logic; DIA10 : in  std_logic; DIA11 : in  std_logic;
              DIA12 : in  std_logic; DIA13 : in  std_logic; DIA14 : in  std_logic; DIA15 : in  std_logic;
              DIA16 : in  std_logic; DIA17 : in  std_logic;
              ADA0  : in  std_logic; ADA1  : in  std_logic; ADA2  : in  std_logic; ADA3  : in  std_logic;
              ADA4  : in  std_logic; ADA5  : in  std_logic; ADA6  : in  std_logic; ADA7  : in  std_logic;
              ADA8  : in  std_logic; ADA9  : in  std_logic; ADA10 : in  std_logic; ADA11 : in  std_logic;
              ADA12 : in  std_logic; ADA13 : in  std_logic;
              CEA   : in  std_logic; CLKA  : in  std_logic; WEA   : in  std_logic;
              CSA0  : in  std_logic; CSA1  : in  std_logic; CSA2  : in  std_logic;
              RSTA  : in  std_logic;
              DIB0  : in  std_logic; DIB1  : in  std_logic; DIB2  : in  std_logic; DIB3  : in  std_logic;
              DIB4  : in  std_logic; DIB5  : in  std_logic; DIB6  : in  std_logic; DIB7  : in  std_logic;
              DIB8  : in  std_logic; DIB9  : in  std_logic; DIB10 : in  std_logic; DIB11 : in  std_logic;
              DIB12 : in  std_logic; DIB13 : in  std_logic; DIB14 : in  std_logic; DIB15 : in  std_logic;
              DIB16 : in  std_logic; DIB17 : in  std_logic;
              ADB0  : in  std_logic; ADB1  : in  std_logic; ADB2  : in  std_logic; ADB3  : in  std_logic;
              ADB4  : in  std_logic; ADB5  : in  std_logic; ADB6  : in  std_logic; ADB7  : in  std_logic;
              ADB8  : in  std_logic; ADB9  : in  std_logic; ADB10 : in  std_logic; ADB11 : in  std_logic;
              ADB12 : in  std_logic; ADB13 : in  std_logic;
              CEB   : in  std_logic; CLKB  : in  std_logic; WEB   : in  std_logic;
              CSB0  : in  std_logic; CSB1  : in  std_logic; CSB2  : in  std_logic;
              RSTB  : in  std_logic;
              DOA0  : out std_logic; DOA1  : out std_logic; DOA2  : out std_logic; DOA3  : out std_logic;
              DOA4  : out std_logic; DOA5  : out std_logic; DOA6  : out std_logic; DOA7  : out std_logic;
              DOA8  : out std_logic; DOA9  : out std_logic; DOA10 : out std_logic; DOA11 : out std_logic;
              DOA12 : out std_logic; DOA13 : out std_logic; DOA14 : out std_logic; DOA15 : out std_logic;
              DOA16 : out std_logic; DOA17 : out std_logic;
              DOB0  : out std_logic; DOB1  : out std_logic; DOB2  : out std_logic; DOB3  : out std_logic;
              DOB4  : out std_logic; DOB5  : out std_logic; DOB6  : out std_logic; DOB7  : out std_logic;
              DOB8  : out std_logic; DOB9  : out std_logic; DOB10 : out std_logic; DOB11 : out std_logic;
              DOB12 : out std_logic; DOB13 : out std_logic; DOB14 : out std_logic; DOB15 : out std_logic;
              DOB16 : out std_logic; DOB17 : out std_logic);
    end component;

begin
    ecp2_mem_0_0_0: DP16KB
        generic map (CSDECODE_B=> "111", CSDECODE_A=> "000", WRITEMODE_B=> "NORMAL", 
                     WRITEMODE_A=> "WRITETHROUGH", GSR=> "DISABLED", RESETMODE=> "ASYNC", 
                     REGMODE_B=> "NOREG", REGMODE_A=> "NOREG", DATA_WIDTH_B=>  2, DATA_WIDTH_A=>  2)
        port map (DIA0=>'0', DIA1=>di(1), DIA2=>'0', DIA3=>'0', DIA4=>'0', DIA5=>'0', 
                  DIA6=>'0', DIA7=>'0', DIA8=>'0', DIA9=>'0', DIA10=>'0', DIA11=>di(0), 
                  DIA12=>'0', DIA13=>'0', DIA14=>'0', DIA15=>'0', DIA16=>'0', DIA17=>'0', 
                  ADA0=>'0', ADA1=>addr(0), ADA2=>addr(1), ADA3=>addr(2), ADA4=>addr(3), ADA5=>addr(4), 
                  ADA6=>addr(5), ADA7=>addr(6), ADA8=>addr(7), ADA9=>addr(8), ADA10=>addr(9), ADA11=>addr(10), 
                  ADA12=>addr(11), ADA13=>addr(12), CEA=>en, CLKA=>clk, WEA=>we, CSA0=>'0', CSA1=>'0', 
                  CSA2=>'0', RSTA=>rst, DIB0=>'0', DIB1=>di(1), DIB2=>'0', DIB3=>'0', DIB4=>'0', 
                  DIB5=>'0', DIB6=>'0', DIB7=>'0', DIB8=>'0', DIB9=>'0', DIB10=>'0', DIB11=>di(0), DIB12=>'0',
                  DIB13=>'0', DIB14=>'0', DIB15=>'0', DIB16=>'0', DIB17=>'0', ADB0=>'0', ADB1=>'0', 
                  ADB2=>'0', ADB3=>'0', ADB4=>'0', ADB5=>'0', ADB6=>'0', ADB7=>'0', ADB8=>'0', ADB9=>'0', ADB10=>'0', 
                  ADB11=>'0', ADB12=>'0', ADB13=>'0', CEB=>'1', CLKB=>'0', WEB=>'0', 
                  CSB0=>'0', CSB1=>'0', CSB2=>'0', RSTB=>'0', DOA0=>do(0), DOA1=>do(1), DOA2=>open, 
                  DOA3=>open, DOA4=>open, DOA5=>open, DOA6=>open, DOA7=>open, DOA8=>open, DOA9=>open,
                  DOA10=>open, DOA11=>open, DOA12=>open, DOA13=>open, DOA14=>open, DOA15=>open, 
                  DOA16=>open, DOA17=>open, DOB0=>open, DOB1=>open, DOB2=>open, DOB3=>open, DOB4=>open,
                  DOB5=>open, DOB6=>open, DOB7=>open, DOB8=>open, DOB9=>open, DOB10=>open, DOB11=>open,
                  DOB12=>open, DOB13=>open, DOB14=>open, DOB15=>open, DOB16=>open, DOB17=>open);
end rtl;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

-- synopsys translate_off
library ecp2;
use ecp2.components.all;
-- synopsys translate_on
use work.manikconfig.all;
use work.manikpackage.all;

-------------------------------------------------------------------------------
entity ECP2_SP16K_S1 is    
    port (do   : out std_logic_vector (0 downto 0);
          addr : in  std_logic_vector (13 downto 0);
          di   : in  std_logic_vector (0 downto 0);
          clk  : in  std_logic;
          en   : in  std_logic;
          we   : in  std_logic;
          rst  : in  std_logic);
end ECP2_SP16K_S1;

architecture rtl of ECP2_SP16K_S1 is
    component DP16KB
        generic (GSR          : in String; WRITEMODE_B  : in String;
                 CSDECODE_B   : in std_logic_vector(2 downto 0);
                 CSDECODE_A   : in std_logic_vector(2 downto 0);
                 WRITEMODE_A  : in String; RESETMODE    : in String;
                 REGMODE_B    : in String; REGMODE_A    : in String;
                 DATA_WIDTH_B : in Integer; DATA_WIDTH_A : in Integer);
        port (DIA0  : in  std_logic; DIA1  : in  std_logic; DIA2  : in  std_logic; DIA3  : in  std_logic;
              DIA4  : in  std_logic; DIA5  : in  std_logic; DIA6  : in  std_logic; DIA7  : in  std_logic;
              DIA8  : in  std_logic; DIA9  : in  std_logic; DIA10 : in  std_logic; DIA11 : in  std_logic;
              DIA12 : in  std_logic; DIA13 : in  std_logic; DIA14 : in  std_logic; DIA15 : in  std_logic;
              DIA16 : in  std_logic; DIA17 : in  std_logic;
              ADA0  : in  std_logic; ADA1  : in  std_logic; ADA2  : in  std_logic; ADA3  : in  std_logic;
              ADA4  : in  std_logic; ADA5  : in  std_logic; ADA6  : in  std_logic; ADA7  : in  std_logic;
              ADA8  : in  std_logic; ADA9  : in  std_logic; ADA10 : in  std_logic; ADA11 : in  std_logic;
              ADA12 : in  std_logic; ADA13 : in  std_logic;
              CEA   : in  std_logic; CLKA  : in  std_logic; WEA   : in  std_logic;
              CSA0  : in  std_logic; CSA1  : in  std_logic; CSA2  : in  std_logic;
              RSTA  : in  std_logic;
              DIB0  : in  std_logic; DIB1  : in  std_logic; DIB2  : in  std_logic; DIB3  : in  std_logic;
              DIB4  : in  std_logic; DIB5  : in  std_logic; DIB6  : in  std_logic; DIB7  : in  std_logic;
              DIB8  : in  std_logic; DIB9  : in  std_logic; DIB10 : in  std_logic; DIB11 : in  std_logic;
              DIB12 : in  std_logic; DIB13 : in  std_logic; DIB14 : in  std_logic; DIB15 : in  std_logic;
              DIB16 : in  std_logic; DIB17 : in  std_logic;
              ADB0  : in  std_logic; ADB1  : in  std_logic; ADB2  : in  std_logic; ADB3  : in  std_logic;
              ADB4  : in  std_logic; ADB5  : in  std_logic; ADB6  : in  std_logic; ADB7  : in  std_logic;
              ADB8  : in  std_logic; ADB9  : in  std_logic; ADB10 : in  std_logic; ADB11 : in  std_logic;
              ADB12 : in  std_logic; ADB13 : in  std_logic;
              CEB   : in  std_logic; CLKB  : in  std_logic; WEB   : in  std_logic;
              CSB0  : in  std_logic; CSB1  : in  std_logic; CSB2  : in  std_logic;
              RSTB  : in  std_logic;
              DOA0  : out std_logic; DOA1  : out std_logic; DOA2  : out std_logic; DOA3  : out std_logic;
              DOA4  : out std_logic; DOA5  : out std_logic; DOA6  : out std_logic; DOA7  : out std_logic;
              DOA8  : out std_logic; DOA9  : out std_logic; DOA10 : out std_logic; DOA11 : out std_logic;
              DOA12 : out std_logic; DOA13 : out std_logic; DOA14 : out std_logic; DOA15 : out std_logic;
              DOA16 : out std_logic; DOA17 : out std_logic;
              DOB0  : out std_logic; DOB1  : out std_logic; DOB2  : out std_logic; DOB3  : out std_logic;
              DOB4  : out std_logic; DOB5  : out std_logic; DOB6  : out std_logic; DOB7  : out std_logic;
              DOB8  : out std_logic; DOB9  : out std_logic; DOB10 : out std_logic; DOB11 : out std_logic;
              DOB12 : out std_logic; DOB13 : out std_logic; DOB14 : out std_logic; DOB15 : out std_logic;
              DOB16 : out std_logic; DOB17 : out std_logic);
    end component;

begin
    ecp2_mem_0_0_0: DP16KB
        generic map (CSDECODE_B=> "111", CSDECODE_A=> "000", WRITEMODE_B=> "NORMAL", 
                     WRITEMODE_A=> "WRITETHROUGH", GSR=> "DISABLED", RESETMODE=> "ASYNC", 
                     REGMODE_B=> "NOREG", REGMODE_A=> "NOREG", DATA_WIDTH_B=>  1, 
                     DATA_WIDTH_A=>  1)
        port map (DIA0=>'0', DIA1=>'0', DIA2=>'0', DIA3=>'0', DIA4=>'0', DIA5=>'0', 
                  DIA6=>'0', DIA7=>'0', DIA8=>'0', DIA9=>'0', DIA10=>'0', DIA11=>di(0), 
                  DIA12=>'0', DIA13=>'0', DIA14=>'0', DIA15=>'0', DIA16=>'0', DIA17=>'0', 
                  ADA0=>addr(0), ADA1=>addr(1), ADA2=>addr(2), ADA3=>addr(3), ADA4=>addr(4), ADA5=>addr(5), 
                  ADA6=>addr(6), ADA7=>addr(7), ADA8=>addr(8), ADA9=>addr(9), ADA10=>addr(10), ADA11=>addr(11), 
                  ADA12=>addr(12), ADA13=>addr(13), CEA=>en, CLKA=>clk, WEA=>we, CSA0=>'0', CSA1=>'0', 
                  CSA2=>'0', RSTA=>rst, DIB0=>'0', DIB1=>'0', DIB2=>'0', DIB3=>'0', 
                  DIB4=>'0', DIB5=>'0', DIB6=>'0', DIB7=>'0', DIB8=>'0', DIB9=>'0', 
                  DIB10=>'0', DIB11=>di(0), DIB12=>'0', DIB13=>'0', DIB14=>'0', DIB15=>'0', 
                  DIB16=>'0', DIB17=>'0', ADB0=>'0', ADB1=>'0', ADB2=>'0', ADB3=>'0', 
                  ADB4=>'0', ADB5=>'0', ADB6=>'0', ADB7=>'0', ADB8=>'0', ADB9=>'0', 
                  ADB10=>'0', ADB11=>'0', ADB12=>'0', ADB13=>'0', CEB=>'1', CLKB=>'0', 
                  WEB=>'0', CSB0=>'0', CSB1=>'0', CSB2=>'0', RSTB=>'0', DOA0=>do(0), DOA1=>open, 
                  DOA2=>open, DOA3=>open, DOA4=>open, DOA5=>open, DOA6=>open, 
                  DOA7=>open, DOA8=>open, DOA9=>open, DOA10=>open, DOA11=>open, 
                  DOA12=>open, DOA13=>open, DOA14=>open, DOA15=>open, 
                  DOA16=>open, DOA17=>open, DOB0=>open, DOB1=>open, DOB2=>open, 
                  DOB3=>open, DOB4=>open, DOB5=>open, DOB6=>open, DOB7=>open, 
                  DOB8=>open, DOB9=>open, DOB10=>open, DOB11=>open, 
                  DOB12=>open, DOB13=>open, DOB14=>open, DOB15=>open, 
                  DOB16=>open, DOB17=>open);
end rtl;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

-- synopsys translate_off
library ecp2;
use ecp2.components.all;
-- synopsys translate_on
use work.manikconfig.all;
use work.manikpackage.all;

entity latspram_ecp2 is
    
    generic (MEM_DATA_WIDTH : integer := 32;
             MEM_ADDR_WIDTH : integer := 10);

    port (clk    : in  std_logic;
          addr   : in  std_logic_vector(MEM_ADDR_WIDTH-1 downto 0);
          data_i : in  std_logic_vector(MEM_DATA_WIDTH-1 downto 0);
          enb    : in  std_logic;
          rst    : in  std_logic;
          we     : in  std_logic;
          data_o : out std_logic_vector(MEM_DATA_WIDTH-1 downto 0));

end latspram_ecp2;

architecture LATTICE_ecp2 of latspram_ecp2 is

    component ECP2_SP16K_S36
        port (do   : out std_logic_vector (35 downto 0);
              addr : in  std_logic_vector (8 downto 0);
              di   : in  std_logic_vector (35 downto 0);
              clk  : in  std_logic; en   : in  std_logic;
              we   : in  std_logic; rst  : in  std_logic);
    end component;

    component ECP2_SP16K_S18
        port (do   : out std_logic_vector (18 downto 0);
              addr : in  std_logic_vector (9 downto 0);
              di   : in  std_logic_vector (18 downto 0);
              clk  : in  std_logic; en   : in  std_logic;
              we   : in  std_logic; rst  : in  std_logic);
    end component;

    component ECP2_SP16K_S9
        port (do   : out std_logic_vector (8 downto 0);
              addr : in  std_logic_vector (10 downto 0);
              di   : in  std_logic_vector (8 downto 0);
              clk  : in  std_logic; en   : in  std_logic;
              we   : in  std_logic; rst  : in  std_logic);
    end component;

    component ECP2_SP16K_S4
        port (do   : out std_logic_vector (3 downto 0);
              addr : in  std_logic_vector (11 downto 0);
              di   : in  std_logic_vector (3 downto 0);
              clk  : in  std_logic; en   : in  std_logic;
              we   : in  std_logic; rst  : in  std_logic);
    end component;

    component ECP2_SP16K_S2
        port (do   : out std_logic_vector (1 downto 0);
              addr : in  std_logic_vector (12 downto 0);
              di   : in  std_logic_vector (1 downto 0);
              clk  : in  std_logic; en   : in  std_logic;
              we   : in  std_logic; rst  : in  std_logic);
    end component;

    component ECP2_SP16K_S1
        port (do   : out std_logic_vector (0 downto 0);
              addr : in  std_logic_vector (13 downto 0);
              di   : in  std_logic_vector (0 downto 0);
              clk  : in  std_logic; en   : in  std_logic;
              we   : in  std_logic; rst  : in  std_logic);
    end component;
    
begin  -- LATTICE_ecp2

    ALE9: if MEM_ADDR_WIDTH = 9 generate
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
            mem_inst : ECP2_SP16K_S36
                port map (DO   => tdatao (high downto low),
                          ADDR => taddr, CLK => clk,
                          DI   => tdatai (high downto low),
                          EN   => enb, rst => rst, WE => we);            
        end generate iblks;
    end generate ALE9;

    ALE10: if MEM_ADDR_WIDTH = 10 generate
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,18);
        signal   tdatai, tdatao : std_logic_vector((nblks*18)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(9 downto 0)            := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);
        
        mem_inst: for i in 0 to nblks-1 generate
            constant low  : integer := i*18;
            constant high : integer := ((i+1)*18)-1;
        begin
            ECP2_mem: ECP2_SP16K_S18
                port map (do   => tdatao(high downto low),
                          addr => taddr, clk  => clk,
                          di   => tdatai(high downto low),
                          en   => enb, we => we, rst  => rst);
        end generate mem_inst;
    end generate ALE10;

    ALE11: if MEM_ADDR_WIDTH = 11 generate
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,9);
        signal   tdatai, tdatao : std_logic_vector((nblks*9)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(10 downto 0)          := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);
        
        mem_inst: for i in 0 to nblks-1 generate
            constant low  : integer := i*9;
            constant high : integer := ((i+1)*9)-1;
        begin
            ECP2_mem: ECP2_SP16K_S9
                port map (do   => tdatao(high downto low),
                          addr => taddr, clk  => clk,
                          di   => tdatai(high downto low),
                          en   => enb, we => we, rst  => rst);
        end generate mem_inst;        
    end generate ALE11;
    
    ALE12: if MEM_ADDR_WIDTH = 12 generate
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,4);
        signal   tdatai, tdatao : std_logic_vector((nblks*4)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(11 downto 0)           := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);
        
        mem_inst: for i in 0 to nblks-1 generate
            constant low  : integer := i*4;
            constant high : integer := ((i+1)*4)-1;
        begin
            ECP2_mem: ECP2_SP16K_S4
                port map (do   => tdatao(high downto low),
                          addr => taddr, clk  => clk,
                          di   => tdatai(high downto low),
                          en   => enb, we => we, rst  => rst);
        end generate mem_inst;        
    end generate ALE12;
    
    ALE13: if MEM_ADDR_WIDTH = 13 generate
        constant nblks          : integer := round_div(MEM_DATA_WIDTH,2);
        signal   tdatai, tdatao : std_logic_vector((nblks*2)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(12 downto 0)           := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);
        
        mem_inst: for i in 0 to nblks-1 generate
            constant low  : integer := i*2;
            constant high : integer := ((i+1)*2)-1;
        begin
            ECP2_mem : ECP2_SP16K_S2
                port map (do   => tdatao(high downto low),
                          addr => taddr, clk  => clk,
                          di   => tdatai(high downto low),
                          en   => enb, we => we, rst  => rst);
        end generate mem_inst;        
    end generate ALE13;
    
    ALE14: if MEM_ADDR_WIDTH = 14 generate
        constant nblks          : integer := MEM_DATA_WIDTH;
        signal   tdatai, tdatao : std_logic_vector((nblks*1)-1 downto 0) := (others => '0');
        signal   taddr          : std_logic_vector(13 downto 0)           := (others => '0');
    begin
        taddr (MEM_ADDR_WIDTH-1 downto 0) <= addr;
        tdatai(MEM_DATA_WIDTH-1 downto 0) <= data_i;
        data_o <= tdatao(MEM_DATA_WIDTH-1 downto 0);
        
        mem_inst: for i in 0 to nblks-1 generate
            constant low  : integer := i*1;
            constant high : integer := ((i+1)*1)-1;
        begin
            ECP2_mem : ECP2_SP16K_S1
                port map (do   => tdatao(high downto low),
                          addr => taddr, clk  => clk,
                          di   => tdatai(high downto low),
                          en   => enb, we => we, rst  => rst);
        end generate mem_inst;        
    end generate ALE14;
    
end LATTICE_ecp2;

library IEEE;
use IEEE.std_logic_1164.all;
-- synopsys translate_off
library ecp2;
use ecp2.components.all;
-- synopsys translate_on

entity ecp2_pll is
    generic (IN_FREQ_MHZ   : integer    := 25;
             CORE_FREQ_MHZ : integer    := 25;
             CLK_DIVBY     : integer := 1;
             CLK_MULBY     : integer := 1);
    port (CLK   : in  std_logic;
          RESET : in  std_logic;
          CLKOP : out std_logic;
          LOCK  : out std_logic);
end ecp2_pll;

architecture Structure of ecp2_pll is

    -- internal signal declarations
    signal CLKOP_t   : std_logic;
    signal scuba_vlo : std_logic;
    signal CLK_t     : std_logic;

    -- local component declarations
    component VLO port (Z : out std_logic); end component;
    
    component EPLLD
        -- synopsys translate_off
        generic (CLKOK_BYPASS : in String; CLKOS_BYPASS : in String;
        CLKOP_BYPASS  : in String; DUTY : in Integer;
        PHASEADJ      : in String; PHASE_CNTL : in String;
        CLKOK_DIV     : in Integer; CLKFB_DIV : in Integer;
        CLKOP_DIV     : in Integer; CLKI_DIV : in Integer);
        -- synopsys translate_on
        port (CLKI : in  std_logic; CLKFB : in std_logic; RST : in std_logic;
        RSTK   : in  std_logic; DPAMODE : in std_logic; DRPAI0 : in std_logic;
        DRPAI1 : in  std_logic; DRPAI2 : in std_logic; DRPAI3 : in std_logic;
        DFPAI0 : in  std_logic; DFPAI1 : in std_logic; DFPAI2 : in std_logic;
        DFPAI3 : in  std_logic; CLKOP : out std_logic; CLKOS : out std_logic;
        CLKOK  : out std_logic; LOCK : out std_logic; CLKINTFB : out std_logic);
    end component;
    attribute CLKOK_BYPASS                      : string;
    attribute CLKOS_BYPASS                      : string;
    attribute CLKOP_BYPASS                      : string;
    attribute DELAY_CNTL                        : string;
    attribute PHASE_CNTL                        : string;
    attribute DUTY                              : string;
    attribute PHASEADJ                          : string;
    attribute FREQUENCY_PIN_CLKOP               : string;
    attribute FREQUENCY_PIN_CLKI                : string;
    attribute FREQUENCY_PIN_CLKOK               : string;
    attribute CLKOK_DIV                         : string;
    attribute CLKOP_DIV                         : string;
    attribute CLKFB_DIV                         : string;
    attribute CLKI_DIV                          : string;
    attribute FIN                               : string;
    attribute CLKOK_BYPASS of PLLDInst_0        : label is "DISABLED";
    attribute CLKOS_BYPASS of PLLDInst_0        : label is "DISABLED";
    attribute CLKOP_BYPASS of PLLDInst_0        : label is "DISABLED";
    attribute DELAY_CNTL of PLLDInst_0          : label is "STATIC";
    attribute PHASE_CNTL of PLLDInst_0          : label is "STATIC";
    attribute DUTY of PLLDInst_0                : label is "8";
    attribute PHASEADJ of PLLDInst_0            : label is "0.0";
    attribute FREQUENCY_PIN_CLKOP of PLLDInst_0 : label is integer'image(CORE_FREQ_MHZ) & ".0000";
    attribute FREQUENCY_PIN_CLKI of PLLDInst_0  : label is integer'image(IN_FREQ_MHZ)   & ".0000";
    attribute FREQUENCY_PIN_CLKOK of PLLDInst_0 : label is integer'image(CORE_FREQ_MHZ) & ".0000";
    attribute CLKOK_DIV of PLLDInst_0           : label is "2";
    attribute CLKOP_DIV of PLLDInst_0           : label is "16";
    attribute CLKFB_DIV of PLLDInst_0           : label is integer'image(CLK_MULBY);
    attribute CLKI_DIV of PLLDInst_0            : label is integer'image(CLK_DIVBY);
    attribute FIN of PLLDInst_0                 : label is integer'image(IN_FREQ_MHZ)   & ".0000";
    attribute syn_keep                          : boolean;

begin
    -- component instantiation statements
    scuba_vlo_inst : VLO
        port map (Z => scuba_vlo);

    PLLDInst_0 : EPLLD
        -- synopsys translate_off
        generic map (CLKOK_BYPASS => "DISABLED", CLKOS_BYPASS => "DISABLED",
                     CLKOP_BYPASS              => "DISABLED", PHASE_CNTL => "STATIC", DUTY => 8,
                     PHASEADJ                  => "0.0", CLKOK_DIV => 2, CLKOP_DIV => 16,
                     CLKFB_DIV => CLK_MULBY,
                     CLKI_DIV  => CLK_DIVBY)
        -- synopsys translate_on
        port map (CLKI => CLK_t, CLKFB => CLKOP_t, RST => RESET,
                  RSTK       => scuba_vlo, DPAMODE => scuba_vlo, DRPAI0 => scuba_vlo,
                  DRPAI1     => scuba_vlo, DRPAI2 => scuba_vlo, DRPAI3 => scuba_vlo,
                  DFPAI0     => scuba_vlo, DFPAI1 => scuba_vlo, DFPAI2 => scuba_vlo,
                  DFPAI3     => scuba_vlo, CLKOP => CLKOP_t, CLKOS => open, CLKOK => open,
                  LOCK       => LOCK, CLKINTFB => open);

    CLKOP <= CLKOP_t;
    CLK_t <= CLK;
end Structure;

-- synopsys translate_off
library ecp2;
configuration Structure_CON of ecp2_pll is
    for Structure
        for all : VLO use entity ecp2.VLO(V); end for;
        for all : EPLLD use entity ecp2.EPLLD(V); end for;
    end for;
end Structure_CON;

-- synopsys translate_on
