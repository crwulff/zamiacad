library ieee;
use ieee.std_logic_1164.all;

entity api is
   port(
      data     : in std_logic_vector(0 to 31);
      z        : out std_ulogic_vector (3 downto 0)
       );
end;

architecture rtl of api is

  constant w : integer := 8;

begin

blubb : process is

 function xnor_reduce
   (in0   : std_ulogic_vector;
    btr   : string := "" ;
    blkdata : string := "" ) return std_ulogic
 is
   variable block_data : string(1 to 1) ;
   attribute dynamic_block_data of block_data : variable is
     "CUE_BTR=/" & BTR & "/" &
     BLKDATA ;
   variable result     : std_ulogic ;
 begin
   result := in0(in0'low) ;
   for i in in0'low+1 to in0'high loop
     result := result xor in0(i);
   end loop;
   result := not result ;
   return result ;
 end xnor_reduce   ;
 
begin

  for i in 0 to data'length/w -1 loop
       z(i) := xnor_reduce(data(w*i to w*(i+1)-1));
  end loop;

end process;

end;
