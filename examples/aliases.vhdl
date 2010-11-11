entity ch_09_02 is

   port (PSW : IN bit_vector(31 downto 0));

end entity ch_09_02;


----------------------------------------------------------------


architecture test of ch_09_02 is
begin


  process_09_2_a : process is

    -- code from book:

    alias binary_string is bit_vector;

    variable s1, s2 : binary_string(0 to 7);
    -- . . .

    -- end of code from book

    alias interrupt_level : bit_vector(4 downto 0) is PSW(30 downto 26);

    variable meinBit: bit;

  begin

    meinBit := interrupt_level(2);

    s1 := "10101010";
    s2 := "11110000";

    -- code from book:

    s1 := s1 and not s2;

    -- end of code from book

    wait;
  end process process_09_2_a;


end architecture test;
