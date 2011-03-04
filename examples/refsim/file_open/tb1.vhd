LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
use STD.textio.all; --Dont forget to include this library for file operations.
use IEEE.std_logic_unsigned.all;

ENTITY read_file IS
END read_file;
 
ARCHITECTURE beha OF read_file IS 

    signal  bin_value : std_logic_vector(3 downto 0):="0000";

BEGIN

    --Read process
   process
      file file_pointer : text;
      variable line_content : string(1 to 4);
      variable line_num : line;
      variable j : integer := 0;
      variable k : integer := 0;
      variable char : character:='0';
   begin
        --Open the file read.txt from the specified location for reading(READ_MODE).
      file_open(file_pointer,"read.txt",READ_MODE);
      while not endfile(file_pointer) loop --till the end of file is reached continue.
      readline (file_pointer,line_num);  --Read the whole line from the file
        --Read the contents of the line from  the file into a variable.
      READ (line_num,line_content);
        --For each character in the line convert it to binary value.
        --And then store it in a signal named 'bin_value'.
        for j in 1 to 4 loop
            char := line_content(j);
            if(char = '0') then
                bin_value(4-j) <= '0';
            else
                bin_value(4-j) <= '1';
            end if;
        end loop;
        wait for 10 ns; --after reading each line wait for 10ns.
        assert k = CONV_INTEGER(bin_value);
        k := k + 1;
      end loop;
      file_close(file_pointer);  --after reading all the lines close the file.
      wait;
    end process;

end beha;

architecture ERRORS1 of read_file is 
begin
    process is
        file file_pointer : text;
        variable line : line;
        variable status : file_open_status;
    begin

        file_open(status, file_pointer, "read.txt", READ_MODE);
        assert status = open_ok;
        file_open(status, file_pointer, "read.txt", READ_MODE);
        assert status = status_error;
        file_close(file_pointer);

        file_open(status, file_pointer, "blablabla.txt", READ_MODE);
        assert status = name_error;
        file_close(file_pointer);

        -- this part of the test only works from JUnit tests
        -- (no external simulator can prove this test).
        file_open(status, file_pointer, "blocked.txt", WRITE_MODE);
        assert status = name_error;
        file_close(file_pointer);

        wait;
    end process ;
end architecture ERRORS1;

architecture ERRORS2 of read_file is 
begin
    process is
        file file_pointer : text;
        variable line : line;
    begin

        readline(file_pointer, line);

        wait;
    end process;
end architecture ERRORS2;

architecture ERRORS3 of read_file is 
begin
    process is
        file file_pointer : text open WRITE_MODE is "read.txt";
        variable line : line;
    begin

        readline(file_pointer, line);

        wait;
    end process;
end architecture ERRORS3;

architecture ERRORS4 of read_file is 
begin
    process is
        file file_pointer : text open WRITE_MODE is "blabla_write.txt";
        variable line : line;
    begin
        file_close(file_pointer);

        writeline(file_pointer, line);

        wait;
    end process ;
end architecture ERRORS4;

architecture ERRORS5 of read_file is 
begin
    process is
        file file_pointer : text open READ_MODE is "blabla_read.txt";
        variable line : line;
    begin

        writeline(file_pointer, line);

        wait;
    end process ;
end architecture ERRORS5;

architecture ERRORS6 of read_file is 
begin
    process is
        file file_pointer : text;
    begin

        assert endfile(file_pointer) = false;

        wait;
    end process ;
end architecture ERRORS6;

architecture ENDF of read_file is 
begin
    process is
        file file_pointer : text;
    begin

        file_open(file_pointer, "blabla_write.txt", WRITE_MODE);
        assert endfile(file_pointer) = true;

        file_close(file_pointer);

        file_open(file_pointer, "blabla_write.txt", APPEND_MODE);
        assert endfile(file_pointer) = true;

        file_close(file_pointer);

        wait;
    end process ;
end architecture ENDF;
