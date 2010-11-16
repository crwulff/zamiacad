entity foo is
end; 

architecture RTL of foo is

  function TO_01 (S: bit_vector; XMAP: bit := '0') return bit_vector is
    variable RESULT: bit_vector(S'LENGTH-1 downto 0);
    variable BAD_ELEMENT: BOOLEAN := FALSE;
    alias XS: bit_vector(S'LENGTH-1 downto 0) is S;
  begin
    for I in RESULT'RANGE loop
      case XS(I) is
        when '0' | '0' => RESULT(I) := '0';
        when '1' | '1' => RESULT(I) := '1';
        when others => BAD_ELEMENT := TRUE;
      end case;
    end loop;
    if BAD_ELEMENT then
      for I in RESULT'RANGE loop
        RESULT(I) := XMAP; -- standard fixup
      end loop;
    end if;
    return RESULT;
  end TO_01;

  function bar (L: bit_vector) return BOOLEAN is
  
    constant L_LEFT: INTEGER := L'LENGTH-1;
    
    alias XL: bit_vector(L_LEFT downto 0) is L;
    
    variable L01 : bit_vector(L_LEFT downto 0);
    
  begin
    L01 := TO_01(XL, '0');
    
    return true;
  end bar;

  constant v : bit_vector(7 downto 0) := "00101010";
  signal b : boolean := bar(v);

begin

end;
