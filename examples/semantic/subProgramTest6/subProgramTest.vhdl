entity SPT is
end SPT;

architecture RTL of SPT is

  function decode(v : bit_vector) return bit_vector is
  variable res : bit_vector((2**v'length)-1 downto 0);
  variable i : integer range res'range;
  begin
    res := (others => '0'); 
    return res ;
  end;

begin
end RTL;




