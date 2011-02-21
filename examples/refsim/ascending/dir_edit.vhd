use std.textio.all; 

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned."+";
use IEEE.std_logic_unsigned."-";

entity dir_test is
end dir_test;
     
architecture behaviour of dir_test is
begin

  process

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

    variable l : line;
    variable counter : std_logic_vector(5 downto 0) := (others => '0');
    variable counter2 : std_logic_vector(0 to 5) := (others => '0');
  begin

    -- TO
    counter2(5) := '1';
    assert counter2 = "000001" report "(1) to: write index failed for counter(5). expected = ""000001"", actual = " & str(counter2);
    assert counter2(5) = '1' report "(1) to: read index failed for counter(5). expected = '1', actual = " & chr(counter2(5));
    write (l, "(1) to: counter = " & str(counter2));
    writeline (output, l);

    counter2(5) := counter2(5);
    assert counter2 = "000001" report "(2) to: read index failed for counter(5). expected = '1', actual = " & chr(counter2(5));
    write (l, "(2) to: counter = " & str(counter2));
    writeline (output, l);

    counter2 := counter2 + "000001";
    assert counter2 = "000010" report "(3) to: addition failed. expected = ""000010"", actual = " & str(counter2);
    assert counter2(4) = '1' report "(3) to: read index failed for counter(4). expected = '1', actual = " & chr(counter2(4));
    write (l, "(3) to: counter = " & str(counter2));
    writeline (output, l);
    write (l, "(3) to: counter(4) = " & chr(counter2(4)));
    writeline (output, l);

    -- DOWNTO
    counter(0) := '1';
    assert counter = "000001" report "(1) downto: write index failed for counter(0). expected = ""000001"", actual = " & str(counter);
    assert counter(0) = '1' report "(1) downto: read index failed for counter(0). expected = '1', actual = " & chr(counter(0));
    write (l, "(1) downto: counter = " & str(counter));
    writeline (output, l);

    counter(0) := counter(0);
    assert counter = "000001" report "(2) downto: read index failed for counter(0). expected = '1', actual = " & chr(counter(0));
    write (l, "(2) downto: counter = " & str(counter));
    writeline (output, l);

    counter := counter + "000001";
    assert counter = "000010" report "(3) downto: addition failed. expected = ""000010"", actual = " & str(counter);
    assert counter(1) = '1' report "(3) downto: read index failed for counter(1). expected = '1', actual = " & chr(counter(1));
    write (l, "(3) downto: counter = " & str(counter));
    writeline (output, l);
    write (l, "(3) downto: counter(1) = " & chr(counter(1)));
    writeline (output, l);

    wait;
  end process;
end behaviour;

