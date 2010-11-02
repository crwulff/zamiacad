-------------------------------------------------------------------------------
-- Title      : Data Bus <-> Core multiplexer
-- Project    : MANIK2
-------------------------------------------------------------------------------
-- File       : dbusmux.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2003-03-26
-- Last update: 2006-10-10
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Multiplexer used for reading & writing from/to Memory
-------------------------------------------------------------------------------
-- Copyright (c) 2003 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2003-03-26  1.0      sandeep	Created
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
--    doutmux :- used for multiplexing data from bus to core during a read cycle
-------------------------------------------------------------------------------
  
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library UNISIM;
use UNISIM.vcomponents.all;

use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;

entity doutmux is
    
    generic (WIDTH : integer := 32);

    port (ben  : in  std_logic_vector (3 downto 0);
          d0   : in  std_logic_vector ((WIDTH/4)-1 downto 0);
          d1   : in  std_logic_vector ((WIDTH/4)-1 downto 0);
          d2   : in  std_logic_vector ((WIDTH/4)-1 downto 0);
          d3   : in  std_logic_vector ((WIDTH/4)-1 downto 0);
          dout : out std_logic_vector (WIDTH-1 downto 0));

end doutmux;

architecture Behavioral of doutmux is
    constant bytezero  : std_logic_vector ((WIDTH/4)-1 downto 0)     := (others => '0');
    constant hwordzero : std_logic_vector ((WIDTH/2)-1 downto 0) := (others => '0');

    signal d0_d1      : std_logic_vector ((WIDTH/2)-1 downto 0);
    signal dout23_sel : std_logic;
    signal dout1_sel  : std_logic_vector (1 downto 0);
    signal dout0_sel  : std_logic_vector (1 downto 0);
    attribute BEL : string;
begin  -- Behavioral

  d0_d1 <= d0 & d1;

        
  dout23_sel <= '1'   when ben = "1111" else '0';
  dout1_sel  <= "10"  when ben = "1100" else
                "11"  when ben = "0011" or ben = "1111" else
                "00";
  dout0_sel  <= "00"  when ben = "1000" else
                "01"  when ben = "0100" or ben = "1100" else
                "10"  when ben = "0010" else
                "11"  ;
        
  dout (WIDTH-1 downto (WIDTH/4)*2)       <= d0_d1 when dout23_sel = '1' else
                                             hwordzero;
  dout (((WIDTH/4)*2)-1 downto (WIDTH/4)) <= bytezero when dout1_sel(1) = '0' else
                                             d2       when dout1_sel(0) = '1' else
                                             d0;
  dout ((WIDTH/4)-1 downto 0)             <= d0       when dout0_sel = "00" else
                                             d1       when dout0_sel = "01" else
                                             d2       when dout0_sel = "10" else
                                             d3;
end Behavioral;

-------------------------------------------------------------------------------
-- dimux :- used to multiplex data from core to memory bus during a write cycle
-------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

--library unisim;
--use unisim.vcomponents.all;
use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;

entity dinmux is
    
    generic (WIDTH : integer := 32);

    port (ben : in  std_logic_vector (3 downto 0);
          din : in  std_logic_vector (WIDTH-1 downto 0);
          d0  : out std_logic_vector ((WIDTH/4)-1 downto 0);
          d1  : out std_logic_vector ((WIDTH/4)-1 downto 0);
          d2  : out std_logic_vector ((WIDTH/4)-1 downto 0);
          d3  : out std_logic_vector ((WIDTH/4)-1 downto 0));

end dinmux;

architecture Behavioral of dinmux is
    constant bytezero : std_logic_vector (7 downto 0) := "00000000";

    signal di0 : std_logic_vector ((WIDTH/4)-1 downto 0);
    signal di1 : std_logic_vector ((WIDTH/4)-1 downto 0);
    signal di2 : std_logic_vector ((WIDTH/4)-1 downto 0);
    signal di3 : std_logic_vector ((WIDTH/4)-1 downto 0);

    attribute BEL : string;
    
begin  -- Behavioral
    di0 <= din ((WIDTH/4)-1     downto 0);
    di1 <= din (((WIDTH/4)*2)-1 downto (WIDTH/4));
    di2 <= din (((WIDTH/4)*3)-1 downto (WIDTH/4)*2);
    di3 <= din (WIDTH-1         downto (WIDTH/4)*3);

    d3 <= di0 ;
    d2 <= di0 when ben = "0010" else
          di1 ;
    d1 <= di0 when ben = "0100" or ben = "1100" else
          di2;
    d0 <= di0 when ben = "1000" else
          di1 when ben = "1100" else
          di3 ;    
end Behavioral;
-------------------------------------------------------------------------------
-- genben - generate the ben signals given addr[1..0] & half,byte & word
-------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;

entity genben is
    generic (SLICE : integer := 0);
    port (addr : in  std_logic_vector (1 downto 0);
          half : in  std_logic;
          byte : in  std_logic;
          word : in  std_logic;
          ben  : out std_logic_vector (3 downto 0));

end genben;

architecture Behavioral of genben is
    signal addr_bhw     : std_logic_vector (4 downto 0);
    attribute BEL : string;

begin  -- Behavioral

    addr_bhw <= addr(1) & addr(0) & half & byte & word;

    ben_proc : process ( addr_bhw )
    begin
        case addr_bhw is
            -- byte
            when "00010" => ben <= "1000";
            when "01010" => ben <= "0100";
            when "10010" => ben <= "0010";
            when "11010" => ben <= "0001";
                            -- half , unaligned ignored
            when "00100" => ben <= "1100";
            when "01100" => ben <= "1100";
            when "10100" => ben <= "0011";
            when "11100" => ben <= "0011";
                            -- word , unaligned ignored
            when "00001" => ben <= "1111";
            when "01001" => ben <= "1111";
            when "10001" => ben <= "1111";
            when "11001" => ben <= "1111";
            when others  => ben <= "0000";
        end case;
    end process ben_proc;        
end Behavioral;
