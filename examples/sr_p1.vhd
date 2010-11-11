--
-- package 1/2 for subprogram resolution test
--
library ieee;
use ieee.numeric_std.all;
use ieee.std_logic_1164.all;

package sr_p1 is
 
 function to_std_ulogic_vector(param : integer) return std_ulogic_vector;
 function to_std_ulogic_vector(param : real) return std_ulogic_vector;
 
end sr_p1;

package body sr_p1 is
 
 subtype string7 is std_ulogic_vector(6 downto 0);
 
 function to_std_ulogic_vector(param : integer) return std_ulogic_vector is
 
 begin
 	return string7("1110000");
 end;

 function to_std_ulogic_vector(param : real) return std_ulogic_vector is
 
 begin
 	return string7("1110000");
 end;
 
end sr_p1;

