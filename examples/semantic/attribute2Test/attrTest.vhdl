entity attrTest is
end entity attrTest;

architecture RTL of attrTest is 

    function f(v : bit_vector) return integer is
      variable count : integer;
    begin
      count := 0;
      for i in v'reverse_range loop
         count := count + 1;
      end loop;
      return count;
    end f;

    signal v : bit_vector(31 downto 0);
    signal c : integer := f(v);

begin
  


end architecture RTL;

