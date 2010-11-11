--
-- very mean piece of Leon3 code
--
library ieee;
use ieee.std_logic_1164.all;
library grlib;
use grlib.stdlib.all;

entity report_version is
  generic (msg1, msg2, msg3, msg4 : string := ""; mdel : integer := 4);
end;

architecture beh of report_version is
begin

  x : process
  
  begin
    wait for mdel * 1 ns;
    if (msg1 /= "") then print(msg1); end if;
    if (msg2 /= "") then print(msg2); end if;
    if (msg3 /= "") then print(msg3); end if;
    if (msg4 /= "") then print(msg4); end if;
    wait;
  end process;
end;



library ieee;
use ieee.std_logic_1164.all;
library grlib;
use grlib.stdlib.all;

entity ccTest is
  generic (
    fabtech       : integer := 42;
    memtech       : integer := 23;
    padtech       : integer := 1;
    clktech       : integer := 2;
    disas         : integer := 3;
    dbguart       : integer := 4;
    pclow         : integer := 5
  );
  port( a, b : IN bit; z : OUT bit);
end entity ccTest;

architecture RTL of ccTest is 

begin

  x : report_version 
  generic map (
      msg1 => "LEON3 GR-XC3S-1500 Demonstration design",
      msg2 => "GRLIB Version " & tost(LIBVHDL_VERSION/1000) & "." & tost((LIBVHDL_VERSION mod 1000)/100)
        & "." & tost(LIBVHDL_VERSION mod 100) & ", build " & tost(LIBVHDL_BUILD),
      msg3 => "Target technology: " & "HAL" & ",  memory library: " & "HAL",
      mdel => 1
  );

  
end architecture RTL;

