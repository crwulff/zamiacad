library ieee;
use ieee.std_logic_1164.all; 
use work.mlite_pack.all; 
 
entity alu is
   port(a_in         : in  std_logic_vector(2 downto 0);
        b_in         : in  std_logic_vector(2 downto 0);
        alu_function : in  alu_function_type;
        c_alu        : out std_logic_vector(2 downto 0));
end; --alu

architecture logic of alu is 
   signal aa, bb, sum : std_logic_vector(3 downto 0);
   signal do_add      : std_logic;
begin

alu_proc: process(a_in, b_in, alu_function, sum) 
   variable sign_ext  : std_logic;
begin
   aa <= (a_in(2) ) & a_in;
   bb <= (b_in(2) ) & b_in;

   case alu_function is
   when alu_add => --c=a+b
      c_alu <= sum(2 downto 0);
   when others =>                 --alu_function = alu_nothing
      c_alu <= "000";
   end case;

end process;

summation: process(aa,bb)
	variable carry_in:std_logic;
begin
   carry_in := '0';
   for index in 0 to 3 loop
      sum(index) <= aa(index) xor bb(index) xor carry_in;
      carry_in := (carry_in and (aa(index) or bb(index))) or
                  (aa(index) and bb(index));
   end loop;
end process; --function

end; --architecture logic

