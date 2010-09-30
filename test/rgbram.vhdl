library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity bloc is
       port(
       	a : in std_logic;
       	b : in std_logic;
       	c : out std_logic
       );
end bloc;

architecture Behavioral of bloc is

begin
       c <= a and b;
end Behavioral;

-- memory 256 x 8 Bit
-- sync write, async read

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.NUMERIC_STD.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;


entity mem is
 port ( wena, clk: in std_logic;
       waddr, wdata, raddr: in std_logic_vector(7 downto 0);
       rdata: out std_logic_vector(7 downto 0));
end entity mem;

architecture behav of mem is
 type mem_type is array(0 to 2**8-1) of std_logic_vector(7 downto 0);
 signal memory: mem_type := (others => (others =>'0')); 

begin
read: process(raddr) is
     begin
      rdata <= memory(to_integer(unsigned(raddr)));
     end process;
write: process(clk) is
      begin
       if clk'event and clk='1' and wena='1' then
        memory(to_integer(unsigned(waddr))) <= wdata;
       end if;
      end process;
end architecture behav;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;


entity top is
       port(
               a : in std_logic;
               b : in std_logic;
               c : out std_logic;
               clk : in std_logic;
               adress : in std_logic_vector(9 downto 0);
               data_in : in std_logic_vector(7 downto 0);
               data_out : out std_logic_vector(7 downto 0);
               write : in std_logic
               );
end top;

architecture Behavioral of top is

component bloc
       port(
       a : in std_logic;
       b : in std_logic;
       c : out std_logic
       );
end component;

signal a_d, b_d, temp : std_logic;
type my_ram_type is array(0 to 1023) of std_logic_vector(7 downto 0);
signal my_ram : my_ram_type := (OTHERS => "10101010");

begin

       bloc_inst : bloc
       port map(
               a => a_d,
               b => b_d,
               c => temp
               );

--      temp <= a_d and b_d;

       process(clk)
       begin
               if(clk'event and clk = '1') then

                       a_d <= a;
                       b_d <= b;
                       c <= temp;
               end if;
       end process;

       process(clk)
       begin
               if(clk'event and clk = '1') then
                       if(write = '1') then
                               my_ram(CONV_INTEGER(adress)) <= data_in;
                       else
                               data_out <=   my_ram(CONV_INTEGER(adress));
                       end if;
               end if;
       end process;

end Behavioral;

