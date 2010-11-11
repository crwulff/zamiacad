entity SPT is
end SPT;

architecture RTL of SPT is

   function "SRL" (constant p1 : integer; constant p2 : integer) return integer is
   begin
      return 42;
   end;

   function f (constant L  : integer;
               constant CNT : INTEGER) return integer is
   begin
    return "srl"(L,-cnt);
   end;

begin
end RTL;




