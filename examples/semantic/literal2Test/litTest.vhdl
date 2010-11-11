entity litTest is
  port (v2 : IN bit_vector(0 to 31));
end entity litTest;

architecture RTL of litTest is 

  signal v : bit_vector(0 to 15);

  type gbenum is (a,b,c);
  type gbarray is array (gbenum) of bit;

  function "<=" (L, R: gbarray) return BOOLEAN is
  begin
    return true;
  end "<=";


begin

  process
  begin
    if ((x"0000" & v(0 to 15)) <= v2(0 to 31)) then
    end if;

  end process;

end architecture RTL;

