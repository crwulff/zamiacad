library ieee;
use ieee.std_logic_1164.all;
use ieee.VITAL_Primitives.all;
use IEEE.VITAL_Timing.all;

entity vitalstate is
   port(
      Z        : out std_logic;
      canrxv   : in std_logic_vector(0 to 1);
       );
end;

architecture rtl of vitalstate is
   CONSTANT L : VitalTableSymbolType := '0';
   CONSTANT H : VitalTableSymbolType := '1';
   CONSTANT x : VitalTableSymbolType := '-';
   CONSTANT S : VitalTableSymbolType := 'S';
   CONSTANT R : VitalTableSymbolType := '/';
   CONSTANT U : VitalTableSymbolType := 'X';
   CONSTANT V : VitalTableSymbolType := 'B'; -- valid clock signal (non-rising)

-- CLR_ipd, CLK_delayed, Q_zd, D, E_delayed, PRE_ipd, CLK_ipd
CONSTANT DFEG_Q_tab : VitalStateTableType := (
( L,  x,  x,  x,  x,  x,  x,  x,  L ),
( H,  L,  H,  H,  x,  x,  H,  x,  H ),
( H,  L,  H,  x,  H,  x,  H,  x,  H ),
( H,  L,  x,  H,  L,  x,  H,  x,  H ),
( H,  H,  x,  x,  x,  H,  x,  x,  S ),
( H,  x,  x,  x,  x,  L,  x,  x,  H ),
( H,  x,  x,  x,  x,  H,  L,  x,  S ),
( x,  L,  L,  L,  x,  H,  H,  x,  L ),
( x,  L,  L,  x,  H,  H,  H,  x,  L ),
( x,  L,  x,  L,  L,  H,  H,  x,  L ),
( U,  x,  L,  x,  x,  H,  x,  x,  L ),
( H,  x,  H,  x,  x,  U,  x,  x,  H ));

begin

 process
  begin
    Z <= canrxv(0 to 0);
    
  end process;

end;
