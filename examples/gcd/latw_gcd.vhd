entity gcd is port (
			reset, clock: in bit;
			in1, in2: in integer range 0 to 127;
			ready: out bit;
			output: out integer range 0 to 127;
			a_out, b_out: out integer range 0 to 127
		);
end gcd;


architecture BEHAV of gcd is

  signal a, b:  integer range 0 to 127;          
  signal ready_sig:  bit;          

  begin

  process(clock,reset)

  variable state: integer range 5 downto 0;
  constant S0 : integer := 0;
  constant S1 : integer := 1;
  constant S2 : integer := 2;
  constant S3 : integer := 3;
  constant S4 : integer := 4;
  constant S5 : integer := 5;

  begin

  if    reset = '1' then
		state := S0;

  elsif clock'event and clock='1' then
	case state is

	when S0
	=>    state := S1;
		a <= in1;   
		b <= in2;
            ready_sig <= '0';  

	when S1 
	=>      if  a /= b	then   
			state := S2;
		else    
			state := S5;
		end if;

	when S2 
	=>      if  a > b	then   
			state := S3;
		else    
			state := S4;
		end if;

	when S3 
	=>	a <= a-b;
		state := S1;

	when S4 
	=>	b <= b-a;
		state := S1;


	when S5 
	=>	ready_sig <= '1';
		state := S5;

	
	end case;
  end if;
  end process;

  ready <= ready_sig;
  output <= a;
  a_out <= a;
  b_out <= b;
end BEHAV;




