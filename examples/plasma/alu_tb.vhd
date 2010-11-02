-- Description: Testbench for the alu in the plasma cpu
-- Author: Alejandro Cook
-- Date: 16-01-07

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
use std.textio.all;
use work.mlite_pack.all;

entity alu_test is
end alu_test;

architecture alu_testbench of alu_test is
    
constant seq1: std_logic_vector(31 downto 0):= "10101010101010101010101010101010";
constant seq2: std_logic_vector(31 downto 0):= "01010101010101010101010101010101";

constant CYCLE: time := 250 ns;
 
component alu
   port (
      a_in         : in  std_logic_vector(31 downto 0);
      b_in         : in  std_logic_vector(31 downto 0);
      alu_function : in  alu_function_type;
      c_alu        : out std_logic_vector(31 downto 0)
      );
end component;    
   
signal a_in            : std_logic_vector(31 downto 0);
signal b_in            : std_logic_vector(31 downto 0);
signal alu_function    : alu_function_type;
signal c_alu           : std_logic_vector(31 downto 0);
  
begin
   behavior : alu port map (
      a_in => a_in,
      b_in => b_in,
      alu_function => alu_function,
      c_alu => c_alu
   );
      
   tests : process
   begin
      a_in <= conv_std_logic_vector(0,32);
      b_in <= conv_std_logic_vector(0,32);
      alu_function <= alu_add;
      wait for  CYCLE;          
      assert c_alu=ZERO report "Addition/Initialization problem" severity error;
      
      a_in <= seq1;
      b_in <= seq2;
      alu_function <= alu_or;
      wait for CYCLE;          
      assert c_alu=ONES report "OR is not working (1)" severity error;
      
      b_in<=seq1;
      wait for CYCLE;
      assert c_alu=seq1 report "OR is not working (2)" severity error;
      
      b_in<=seq2;
      alu_function <= alu_nor;
      wait for CYCLE;
      assert c_alu=ZERO report "NOR is not working (1)" severity error;
      
      b_in<=seq1;
      wait for CYCLE;
      assert c_alu=seq2 report "NOR is not working (2)" severity error;
      
      b_in<=seq2;
      alu_function <= alu_xor;
      wait for CYCLE;
      assert c_alu=ONES report "XOR is not working (1)" severity error;
      
      b_in <=seq1;
      wait for CYCLE;
      assert c_alu=ZERO report "XOR is not working (2)" severity error;
      
      b_in <= seq2;
      alu_function <= alu_and;
      assert c_alu=ZERO report "AND is not working (1)" severity error;
      
      b_in <= seq1;
      wait for CYCLE;
      assert c_alu=seq1 report "AND is not working (2)" severity error;
      
      
-- Addition tests.

	   alu_function <= alu_add;
      a_in <= (others => '1');
	   b_in <= conv_std_logic_vector (1, 32); -- 
	   wait for CYCLE;
	   assert c_alu = conv_std_logic_vector(0,32) report "Addition does not work (1)" severity error;

	   for i in 0 to 100 loop
		   a_in <= conv_std_logic_vector(i,32);
		   b_in <= conv_std_logic_vector(100-i,32);
		   wait for CYCLE;
		   assert c_alu = conv_std_logic_vector(100,32) report "Addition does not work (2)"  severity error;
	   end loop;
	
-- Subtraction tests

	   alu_function <= alu_subtract;
	   a_in <= "10000000000000000000000000000000";
	   b_in <= conv_std_logic_vector (1,32);
	   wait for CYCLE;
	   assert c_alu = "01111111111111111111111111111111" report "Subtraction does not work (1)" severity error;

	   a_in <= (others => '1');
	   b_in <= (others => '1');
	   wait for CYCLE;
	   assert c_alu = conv_std_logic_vector(0,32) report "Subtraction does not work (2)" severity error;
      
      a_in <= X"80000001";
      b_in <= X"0FFFFFFF";
      alu_function <= alu_less_than;
      wait for CYCLE;
      assert c_alu = conv_std_logic_vector(0,32) report "Less_than does not work (1)" severity error;
      
      b_in <= X"80000010";
      wait for CYCLE;
      assert c_alu = conv_std_logic_vector(1,32) report "Less_than does not work (2)" severity error;
      
      a_in <= X"80000010";
      wait for CYCLE;
      assert c_alu = conv_std_logic_vector(0,32) report "Less_than does not work (3)" severity error;
      
      a_in <= X"80000001";
      b_in <= X"0FFFFFFF";
      alu_function <= alu_less_than_signed;
      wait for CYCLE;
      assert c_alu = conv_std_logic_vector(1,32) report "Less_than_signed does not work (1)" severity error;
      
      b_in <= X"80000000";
      wait for CYCLE;
      assert c_alu = conv_std_logic_vector(0,32) report "Less_than_signed does not work (2)" severity error;
      
      a_in <= X"80000000";
      wait for CYCLE;
      assert c_alu = conv_std_logic_vector(0,32) report "Less_than_signed does not work (3)" severity error;
      
      b_in <= X"80000001";
      wait for CYCLE;
      assert c_alu = conv_std_logic_vector(1,32) report "Less_than_signed does not work (3)" severity error;
      
      
      
      wait;
             
             
   end process;
end;      
         
      
           
