library ieee;
use ieee.std_logic_1164.all;
use ieee.VITAL_Primitives.all;

entity vitmux is
   port(
      A        : in std_logic;
      Z        : out std_logic;
      FENCEN   : in std_logic;
      canrxv   : in std_logic_vector(0 to 1);
      cantxv   : out std_logic_vector(0 to 1)
       );
end;

architecture rtl of vitmux is

begin

 process
  begin
    Z <= VitalMUX (data => (A, '0'), dselect => (0 => FENCEN));
    
  end process;

end;
