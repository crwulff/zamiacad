-- VHDL Model Created from SGE Symbol i2c_buf.sym -- Dec 28 15:27:48 1998

library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity I2C_BUF is
      Port (       O : In    std_logic;
                   B : InOut std_logic;
                   I : Out   std_logic );
end I2C_BUF;

architecture BEHAVIORAL of I2C_BUF is
begin

    B <= '0' when O = '0' else 'Z';
    I <= B;

end BEHAVIORAL;
