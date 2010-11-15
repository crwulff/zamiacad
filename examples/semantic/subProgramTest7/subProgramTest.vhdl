entity foo is
end; 

architecture RTL of foo is

	function todec(i:integer) return character is
	begin
	  case i is
	  when 0 => return('0');
	  when 1 => return('1');
	  when 2 => return('2');
	  when 3 => return('3');
	  when 4 => return('4');
	  when 5 => return('5');
	  when 6 => return('6');
	  when 7 => return('7');
	  when 8 => return('8');
	  when 9 => return('9');
	  when others => return('0');
	  end case;
	end;

	function tost(i : integer) return string is
	variable s, x : string(1 to 128);
	variable n, tmp : integer := 0;
	begin
	  tmp := i;
	  loop
	    s(128-n) := todec(tmp mod 10);
	    tmp := tmp / 10;
	    n := n+1;
	    if tmp = 0 then exit; end if;
	  end loop;
	  x(1 to n) := s(129-n to 128);
	  return(x(1 to n));
	end;

  signal str : string := tost(42);

begin

   
end;
