
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




-- Detects port mapping errors (mismatched types and directions) 

entity PORTCHECK is
end entity;

entity PORTCHECK_EL is
	port (portA: in bit_vector 
		   (1 to 3) 
	);
end entity;
 
entity PORTCHECK_SUBEL is 
	port (portB: out bit_vector); 
end entity;

architecture ARCH of PORTCHECK_SUBEL is begin end architecture;
        
architecture ARCH of PORTCHECK_EL is
begin 
	sub_TypeErr: entity PORTCHECK_SUBEL port map (1); -- must be error
	sub_TypeErr2: entity PORTCHECK_SUBEL port map (porta => 1); -- must be error
 	sub_DirErr: entity PORTCHECK_SUBEL port map (porta); -- must be error
 	sub_DirErr: entity PORTCHECK_SUBEL port map (portB =>porta); -- must be error
end architecture;
       
architecture ARCH of PORTCHECK is
begin

	U1: entity WORK.PORTCHECK_EL port map("101");
            
end architecture;

