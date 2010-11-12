entity litTest is
end entity litTest;

architecture RTL of litTest is 


begin

  process
   variable v : bit_vector(31 downto 0);
  begin

    v <= x"3c010000";

    assert v = "00111100000000010000000000000000" report "Hex literal parsing failed.";

  end process;

end architecture RTL;

