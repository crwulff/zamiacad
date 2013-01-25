use std.textio.all; 

library IEEE;
use IEEE.std_logic_1164.all;

package SimPackage is

  function chr(sl: std_logic) return character;
  function chr(b: bit) return character;
  
  function str(slv: std_logic_vector) return string;
  function str(bits: bit_vector) return string;

end package SimPackage;

package body SimPackage is

-- ##############################################
-- ############    STD_LOGIC_VECTOR   ###########
-- ##############################################

    function chr(sl: std_logic) return character is
     variable c: character;
     begin
       case sl is
          when 'U' => c:= 'U';
          when 'X' => c:= 'X';
          when '0' => c:= '0';
          when '1' => c:= '1';
          when 'Z' => c:= 'Z';
          when 'W' => c:= 'W';
          when 'L' => c:= 'L';
          when 'H' => c:= 'H';
          when '-' => c:= '-';
       end case;
     return c;
    end chr;
    
   -- converts std_logic_vector into a string (binary base)
   -- (this also takes care of the fact that the range of
   --  a string is natural while a std_logic_vector may
   --  have an integer range)

    function str(slv: std_logic_vector) return string is
      variable result : string (1 to slv'length);
      variable r : integer;
    begin
      r := 1;
      for i in slv'range loop
         result(r) := chr(slv(i));
         r := r + 1;
      end loop;
      return result;
    end str;

-- ##############################################
-- ############       BIT_VECTOR      ###########
-- ##############################################

    function chr(b: bit) return character is
     variable c: character;
     begin
       case b is
          when '0' => c:= '0';
          when '1' => c:= '1';
       end case;
     return c;
    end chr;
    
    function str(bits: bit_vector) return string is
      variable result : string (1 to bits'length);
      variable r : integer;
    begin
      r := 1;
      for i in bits'range loop
         result(r) := chr(bits(i));
         r := r + 1;
      end loop;
      return result;
    end str;
    
end package body SimPackage;
