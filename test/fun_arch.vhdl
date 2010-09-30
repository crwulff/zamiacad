library ieee;
use ieee.std_LOGIC_1164.all;
use WORK.libcache.all;
USE     IEEE.VITAL_PRIMITIVES.ALL;

entity FUNE  is
  generic (
	icen      : integer range 0 to 1  := 0;
	irepl     : integer range 0 to 2  := 0;
	isets     : integer range 1 to 4  := 1;
	ilinesize : integer range 4 to 8  := 4;
	isetsize  : integer range 1 to 256 := 1;
	isetlock  : integer range 0 to 1  := 0;
	lram      : integer range 0 to 1 := 0;
	lramsize  : integer range 1 to 512 := 1;
	lramstart : integer range 0 to 255 := 16#8e#
  );    
  port (
	CLOCK   : in    std_logic;                     
	RSTN    : in    std_logic;                     
	BR      : in    std_logic_vector(3 downto 0);  
	ID      : in    std_logic_vector(15 downto 0); 
	BRC     : out   std_logic_vector(7 downto 0)
  );
end FUNE;

architecture a1 of FUNE is
 constant prop_delay:  time := 1ns;

constant icfg : std_logic_vector(31 downto 0) := 
	cache_cfg(irepl, isets, ilinesize, isetsize, isetlock, 0, lram, lramsize, lramstart, 0);


  signal foo : std_ulogic;

begin

  foo <= orv(ID);

end architecture;
