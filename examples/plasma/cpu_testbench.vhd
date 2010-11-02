---------------------------------------------------------------------
--
--  Testbench fuer gesamte CPU
--
---------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use work.mlite_pack.all;

entity cpu_tb is
end;

architecture logic of cpu_tb is
   signal tb_clk          : std_logic;
   signal tb_reset        : std_logic;
   signal tb_intr         : std_logic;
   signal tb_mem_address  : std_logic_vector(31 downto 0);
   signal tb_mem_data_w   : std_logic_vector(31 downto 0);
   signal tb_mem_data_r   : std_logic_vector(31 downto 0);
   signal tb_mem_byte_sel : std_logic_vector(3 downto 0);
   signal tb_mem_write    : std_logic;
   signal tb_mem_pause    : std_logic;
   constant PERIOD        : time := 10 ns;
begin -- architecture logic of cpu_tb

   generate_clock: process
   begin
      L1: loop
         tb_clk <= '0';
	 wait for PERIOD / 2;
	 tb_clk <= '1';
	 wait for PERIOD / 2;
      end loop L1;
   end process;

   generate_reset: process
   begin
      tb_intr       <= '0';
      tb_mem_pause  <= '0';
      tb_mem_data_r <= "00000000000000000000000000000000";
      tb_reset      <= '1','0' after 4*PERIOD;
      wait;
   end process;

   tb_mlite_cpu: mlite_cpu PORT MAP (
         clk          => tb_clk,
	 reset_in     => tb_reset,
	 intr_in      => tb_intr,
	 mem_address  => tb_mem_address,
	 mem_data_w   => tb_mem_data_w,
	 mem_data_r   => tb_mem_data_r,
	 mem_byte_sel => tb_mem_byte_sel,
	 mem_write    => tb_mem_write,
	 mem_pause    => tb_mem_pause
   );

end; -- architecture logic of cpu_tb
