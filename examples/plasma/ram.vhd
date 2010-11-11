---------------------------------------------------------------------
-- TITLE: Random Access Memory
-- AUTHOR: Steve Rhoads (rhoadss@yahoo.com)
-- DATE CREATED: 4/21/01
-- FILENAME: ram.vhd
-- PROJECT: Plasma CPU core
-- COPYRIGHT: Software placed into the public domain by the author.
--    Software 'as is' without warranty.  Author liable for nothing.
-- DESCRIPTION:
--    Implements the RAM, reads the executable from either "code.txt",
--    or for Altera "code[0-3].hex".
--    Modified from "The Designer's Guide to VHDL" by Peter J. Ashenden
---------------------------------------------------------------------
library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_misc.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
use ieee.std_logic_textio.all;
use std.textio.all;
use work.mlite_pack.all;

entity ram is
   port(clk          : in std_logic;
        mem_byte_sel : in std_logic_vector(3 downto 0);
        mem_write    : in std_logic;
        mem_address  : in std_logic_vector(31 downto 0);
        mem_data_w   : in std_logic_vector(31 downto 0);
        mem_data_r   : out std_logic_vector(31 downto 0));
end; --entity ram

architecture logic of ram is
   constant ADDRESS_WIDTH   : natural := 13;
   signal clk_inv           : std_logic;
   signal mem_sel           : std_logic;
   signal read_enable       : std_logic;
   signal write_byte_enable : std_logic_vector(3 downto 0);
begin
   clk_inv <= not clk;
   mem_sel <= '1' when mem_address(30 downto ADDRESS_WIDTH) = ZERO(30 downto ADDRESS_WIDTH) else
              '0';
   read_enable <= mem_sel and not mem_write;
   write_byte_enable <= mem_byte_sel when mem_sel = '1' else
                        "0000";

   ram_proc: process(clk, mem_byte_sel, mem_write, 
         mem_address, mem_data_w, mem_sel)
      variable mem_size : natural := 2 ** ADDRESS_WIDTH;
      variable data : std_logic_vector(31 downto 0); 
      subtype word is std_logic_vector(mem_data_w'length-1 downto 0);
      type storage_array is
         array(natural range 0 to mem_size/4 - 1) of word;
      variable storage : storage_array;
      variable index : natural := 0;
   begin
      index := conv_integer(mem_address(ADDRESS_WIDTH-1 downto 2));
      data := storage(index);

      if mem_sel = '1' then
         if mem_write = '0' then
            mem_data_r <= data;
         end if;
         if mem_byte_sel(0) = '1' then
            data(7 downto 0) := mem_data_w(7 downto 0);
         end if;
         if mem_byte_sel(1) = '1' then
            data(15 downto 8) := mem_data_w(15 downto 8);
         end if;
         if mem_byte_sel(2) = '1' then
            data(23 downto 16) := mem_data_w(23 downto 16);
         end if;
         if mem_byte_sel(3) = '1' then
            data(31 downto 24) := mem_data_w(31 downto 24);
         end if;
      end if;
      
      if (clk'event and clk='1') then
         if mem_write = '1' then
            storage(index) := data;
         end if;
      end if;
   end process;

end; --architecture logic

