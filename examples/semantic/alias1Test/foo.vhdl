entity foo is
end; 

architecture RTL of foo is

  function bar (L: bit_vector) return BOOLEAN is
  
    constant L_LEFT: INTEGER := L'LENGTH-1;
    
    alias XL: bit_vector(L_LEFT downto 0) is L;
    
    variable L01 : bit_vector(L_LEFT downto 0);
    
  begin
    L01 := XL;
    
    return true;
  end bar;

  signal str : boolean := bar("101010");

begin

   
end;
