library ieee;
use ieee.std_logic_1164.all;

package miscellaneous is

  component ClockGenerator
    port (
      Clk     : in  std_ulogic;
      Reset   : in  std_ulogic;
      oMCLK   : out std_ulogic;
      oBCLK   : out std_ulogic;
      oSCLK   : out std_ulogic;
      oLRCOUT : out std_ulogic);
  end component;

  component vgaclkgen
	PORT
	(
		areset		: IN STD_LOGIC  := '0';
		inclk0		: IN STD_LOGIC  := '0';
		c0		: OUT STD_LOGIC ;
		locked		: OUT STD_LOGIC 
        );
  end component;

  component Clk100MhzTo40MHz
	PORT
	(
		inclk0		: IN STD_LOGIC  := '0';
		pllena		: IN STD_LOGIC  := '1';
		areset		: IN STD_LOGIC  := '0';
		c0		: OUT STD_LOGIC ;
		locked		: OUT STD_LOGIC 
	);
  end component;
  
  -- Gleichmann board types
  constant compact_v1    : integer := 1;
  constant compact_v2    : integer := 2;
  constant mini_altera   : integer := 3;
  constant mini_lattice  : integer := 4;
  constant mini_lattice2 : integer := 5;
  constant midi          : integer := 6;

end miscellaneous;
