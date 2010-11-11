library ieee;   use ieee.std_logic_1164.all;
                use ieee.vital_timing.all;
                use ieee.vital_primitives.all;

entity arrayTest is
  port( a, b, c, d : in std_logic; z : out bit);
end entity arrayTest;

architecture RTL of arrayTest is 

   constant sTable : VitalStateTableType := (
     ( '-', '-', '-', '-', '-', 'S' ),
     ( '-', '-', '0', '-', '-', '-' ),
     ( '-', '-', '0', '0', '-', '1' ),
     ( '-', '-', '-', '-', '-', '-' ),
     ( '-', '-', '-', '1', '1', '0' ),
     ( '0', '-', '-', '-', '-', '-' ),
     ( '0', '1', '-', '-', '-', '1' ),
     ( '-', '-', '-', '-', '-', '-' ),
     ( '-', '1', '-', '-', '0', '0' ),
     ( '0', '-', '1', '-', '-', '-' ),
     ( '1', '1', '0', '0', '-', '0' ),
     ( '-', '-', '-', '-', '-', '-' ),
     ( '-', '1', '-', '1', '0', '0' ));


begin

  foo: process

  variable pData : std_logic_vector(0 to 3);
  variable r     : std_logic := 'X';

  begin
     VitalStateTable(
        Result => r,
        PreviousDataIn => pData,
        StateTable => sTable,
        DataIn => (a, b, c, d));
  end process;

end architecture RTL;

