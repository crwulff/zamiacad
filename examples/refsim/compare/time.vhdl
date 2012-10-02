 
entity TIMER is
end entity TIMER;

architecture ARCH of TIMER is

begin

  process
   	
	procedure check(T1, T2: time; op: string, expected, actual: boolean) is
		constant title : string := time'image(T1) & " " & op & " " & time'image(T2)  & " evaluates to " & boolean'image(actual);
	begin
		assert actual = expected report title & ", which is wrong" severity error;
		report title & ", which is correct";
	end procedure;
 
  	procedure greater(T1, T2: time; expected: boolean) is begin
  		check(T1, T2, ">", expected, T1 > T2);
	end procedure;
  		
  	procedure less(T1, T2: time; expected: boolean) is begin
  		check(T1, T2, "<", expected, T1 < T2);
	end procedure;
  		
  	procedure eq(T1, T2: time; expected: boolean) is begin
  		check(T1, T2, "=", expected, T1 = T2);
	end procedure;
  		
  	procedure greaterEQ(T1, T2: time; expected: boolean) is begin
  		check(T1, T2, ">=", expected, T1 >= T2);
	end procedure;
  		
  	procedure lessEQ(T1, T2: time; expected: boolean) is begin
  		check(T1, T2, "<=", expected, T1 <= T2);
	end procedure;
  		
  	procedure neq(T1, T2: time; expected: boolean) is begin
  		check(T1, T2, "/=", expected, T1 /= T2);
	end procedure;
  		
  	constant long : time := 1 hr;
  	constant short : time := 1 fs;
  	
	procedure checkNow(expected: time) is begin
		assert now = expected report "current time must be " & time'image(expected) & " but it is " & time'image(now) & " actially" severity error;
	end procedure;
  begin
	checkNow(0 ns);
  	 
  	greater(short, short, false); greater(long, short, true); greater(short, long, false);
  	greaterEQ(short, short, true); greaterEQ(long, short, true); greaterEQ(short, long, false);
  	less(short, short, false); less(long, short, false); less(short, long, true);  
  	lessEQ(short, short, true); lessEQ(long, short, false); lessEQ(short, long, true);
  	eq(short, short, true); eq(long, short, false); eq(short, long, false); 
  	neq(short, short, false); neq(long, short, true); neq(short, long, true);
  	wait for 10 ns;
	checkNow(10 ns);
  	wait;
  end process;
end architecture ARCH;

