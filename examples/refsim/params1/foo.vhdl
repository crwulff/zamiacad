entity foo is
end; 

architecture RTL of foo is

begin

  tm: process is
  
    FUNCTION bar ( l : bit_vector ) RETURN bit_vector IS
        ALIAS lv : bit_vector ( 1 TO l'LENGTH ) IS l;
        VARIABLE result : bit_vector ( 1 TO l'LENGTH ) := (OTHERS => '0');
    BEGIN
        FOR i IN result'RANGE LOOP
            result(i) := not lv(i);
        END LOOP;
        RETURN result;
    END;
  
    variable v,v2 : bit_vector (31 downto 0);
  
  begin
    v := (others => '0');
      
    v2 := bar(v);

    assert v2 = X"FFFFFFFF" report "inversion failed.";
    
    -- array "not" operator was not implemented until Dec 2012 patch 
    assert (not v = X"FFFFFFFF") report "inversion 2 failed.";
    
    wait;
  end process;

   
end;
