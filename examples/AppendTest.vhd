library IEEE;
use IEEE.std_logic_1164.all;

entity AppendTest is
    port (
        A: in std_logic_vector(31 downto 0);
        B: in std_logic_vector(0 to 31);
        C: out std_logic_vector(31 downto 0);
        D: out std_logic_vector(0 to 31);
        E: out std_logic_vector(31 downto 0);
        F: out std_logic_vector(31 downto 0)
        );
end AppendTest;        

architecture Test of AppendTest is
    constant ZERO: std_logic_vector(31 downto 0):= "00000000000000000000000000000000";
    
begin
   process(A,B)
   begin
      C <= ZERO(31 downto 10) & A(9 downto 0);
      D <= ZERO(31 downto 10) & B(0 to 9);
      E <= ZERO(31) & A(30 downto 0);
      F <= A(31 downto 5) & "00000";
   end process; 
end;


