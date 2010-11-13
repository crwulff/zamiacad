entity comp is
end entity comp;

architecture WRONGONE of comp is 
begin
  
  assert true = false report "Wrong architecture used.";
  
end architecture WRONGONE;

architecture RIGHTONE of comp is 
begin
  
end architecture RIGHTONE;

