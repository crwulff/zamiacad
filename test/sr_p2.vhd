--
-- package 1/2 for subprogram resolution test
--
library ieee;
use ieee.numeric_std.all;
use ieee.std_logic_1164.all;

package sr_p2 is
 
 function to_std_ulogic_vector(param : string) return std_ulogic_vector;
 
end sr_p2;

package body sr_p2 is

 subtype string7 is std_ulogic_vector(6 downto 0);
 
 function to_std_ulogic_vector(param : string) return std_ulogic_vector is
 
 begin
 	return string7("1110000");
 end;
 
end sr_p2;
