library ieee;
use ieee.std_logic_1164.all;
use ieee.VITAL_Primitives.all;

entity searray is
   port(
      Z        : out std_logic;
      canrxv   : in std_logic_vector(0 to 1);
       );
end;

architecture rtl of searray is

begin

 process
  begin
    Z <= canrxv(0 to 0);
    
  end process;

end;
