library IEEE;
use IEEE.std_logic_1164.all;
use std.textio.all;

entity TB_GCD is
end TB_GCD;

architecture BEH of TB_GCD is

   component GCD
      port(RESET    : in bit ;
           CLOCK    : in bit ;
           IN1      : in integer range 0 to 127 ;
           IN2      : in integer range 0 to 127 ;
           READY    : out bit ;
           OUTPUT   : out integer range 0 to 127; 
		   a_out, b_out: out integer range 0 to 127);

   end component;


   constant PERIOD : time := 10 ns;
   type T_DATA is array (0 to 7) of integer range 0 to 127 ;
   constant DATA_IN1 : T_DATA := 
 			(121, 43, 76, 98, 90, 45, 34, 83);	
   constant DATA_IN2 : T_DATA := 
 			(82, 43, 53, 98, 92, 73, 114, 109);	
   
   signal W_RESET    : bit ;
   signal W_CLOCK    : bit  := '0';
   signal W_IN1      : integer range 0 to 127 ;
   signal W_IN2      : integer range 0 to 127 ;
   signal W_READY    : bit ;
   signal W_OUTPUT   : integer range 0 to 127 ;
   signal W_A_OUT, W_B_OUT : integer range 0 to 127 ;

begin

   DUT : GCD
      port map(RESET    => W_RESET,
               CLOCK    => W_CLOCK,
               IN1      => W_IN1,
               IN2      => W_IN2,
               READY    => W_READY,
               OUTPUT   => W_OUTPUT,
			   A_OUT	=> W_A_OUT,
			   B_OUT	=> W_B_OUT);

   W_CLOCK <= not W_CLOCK after PERIOD/2;

	STIMULI: process(W_CLOCK, W_READY)
    variable count 	: integer := 0;
	variable i 		: integer := 0;
	begin
        if(W_CLOCK'event and W_CLOCK='1') then

			if count = 1 then 
				W_RESET    <= '1';
				W_IN1      <= DATA_IN1(i);
				W_IN2      <= DATA_IN2(i);
				i := i+1;
				if i > 7 then 
					i:= 0; 
				end if;
			-- elsif count = 2 then 
				-- W_RESET    <= '1';
			else 
				W_RESET    <= '0';
				if count >4 and W_READY = '1' then 
					count := 0;
				end if;

			end if;
		    count := count + 1;
        end if;
    end process STIMULI;   

	PRINT_STIMULI: process(W_CLOCK)
		file STIMULI_OUT: text open WRITE_MODE is "stimuli.txt";
		variable L		: line;
	begin
        if(W_CLOCK'event and W_CLOCK='1') then
			write (L, W_IN1);	
			write (L, ' ');
			write (L, W_IN2); 	
			write (L, ' ');
			write (L, W_RESET);
			writeline (STIMULI_OUT, L);
        end if;
    end process PRINT_STIMULI;   
	
	NOSTOP: process
    begin
		wait;
	end process NOSTOP;	
	
end BEH;

configuration CFG_TB_GCD of TB_GCD is
   for BEH
   end for;
end CFG_TB_GCD;