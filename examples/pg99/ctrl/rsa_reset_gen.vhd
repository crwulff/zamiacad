-- VHDL Model Created from SGE Symbol rsa_reset_gen.sym -- Jan  8 14:26:46 1999

library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity RSA_RESET_GEN is
      Port ( CTRL_CHANGE : In    std_logic;
             RESET : In    std_logic;
             TEST_MODE : In    std_logic;
             RSA_RESET : Out   std_logic );
end RSA_RESET_GEN;

architecture BEHAVIORAL of RSA_RESET_GEN is

begin

    RSA_RESET <= RESET or CTRL_CHANGE when TEST_MODE = '0' else
		 RESET;
    
end BEHAVIORAL;
