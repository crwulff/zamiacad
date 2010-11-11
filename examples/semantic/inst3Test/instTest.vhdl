
package mypkg is

   type tr is record
      f1 : bit;
      f2 : bit;
   end record;
end package;

use WORK.mypkg.ALL;

entity e1 is

  port (a : in tr);

end entity e1;

architecture RTL of e1 is
begin
end architecture RTL;

use WORK.mypkg.ALL;

entity instTest is
end entity instTest;

architecture RTL of instTest is 

begin

  foo: entity WORK.e1 port map (a => (f1 =>'1', f2 => '0'));

end architecture RTL;

