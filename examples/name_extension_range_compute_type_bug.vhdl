-- test.vhdl created on 5:43  2006.3.20
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_misc.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;

entity test is
  port (
        irq_activate : out std_logic_vector(1 downto 0)
        );
end;

architecture behavioral of test is
  signal ctrl0     : std_logic_vector(30 downto 0);
  constant pcdebug_com_select_bit : integer := 0;
begin
  irq_activate (1) <= ctrl0(pcdebug_com_select_bit) = '1';
end;
