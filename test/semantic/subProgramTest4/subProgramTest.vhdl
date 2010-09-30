entity SPT is
end SPT;

architecture RTL of SPT is

  function f (p1 : string := ""; p2 : string := "") return bit is
  begin
    return '1';
  end f;

  constant c : bit := f;

  signal s: bit ;

begin
   s <= f;
end RTL;




