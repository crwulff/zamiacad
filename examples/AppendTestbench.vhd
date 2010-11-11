library IEEE;
use IEEE.std_logic_1164.all;

entity AppendtestBench is
end AppendTestbench;

architecture Testbench of AppendTestbench is
    
    signal A: std_logic_vector(31 downto 0);
    signal B: std_logic_vector(0 to 31);
    signal C: std_logic_vector(31 downto 0);
    signal D: std_logic_vector(0 to 31);
    signal E: std_logic_vector(31 downto 0);
    signal F: std_logic_vector(31 downto 0);
    
    component AppendTest port (
        A: in std_logic_vector(31 downto 0);
        B: in std_logic_vector(0 to 31);
        C: out std_logic_vector(31 downto 0);
        D: out std_logic_vector(0 to 31);
        E: out std_logic_vector(31 downto 0);
        F: out std_logic_vector(31 downto 0)
    );
   end component;
       
   begin
       
       dut: AppendTest port map (
          A => A,
          B => B,
          C => C,
          D => D,
          E => E,
          F => F
       );
       
       tests:process
       begin
           
           A <= X"00000000";
           B <= X"00000000";
           
           wait for 100 ns;
           
           A <= X"AED1F775";
           B <= X"A612AFE4";
           
           wait for 100 ns;
           
           assert D=X"00000298" report "The append operation does not work in aggregates (to)" severity ERROR;
           assert C=X"00000375" report "The append operation does not work in aggregates (downto)" severity ERROR;      
           assert F=X"AED1F760" report "The append operation does not work in aggregates (downto, literal)" severity Error;
           wait;
           
           A <= (others =>'1');
           wait for 100 ns;
           assert E=X"7FFFFFFF";
           
           
          
       end process;
   end;

    
