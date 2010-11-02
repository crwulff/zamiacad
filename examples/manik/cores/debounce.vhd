-------------------------------------------------------------------------------
-- Title      : Debounce inputs (selectable active high or low)
-- Project    : MANIK-II
-------------------------------------------------------------------------------
-- File       : debounce.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech Inc
-- Created    : 2006-05-14
-- Last update: 2006-05-14
-- Platform   : 
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: Used to debounce inputs
-------------------------------------------------------------------------------
-- Copyright (c) 2006 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2006-05-14  1.0      Sandeep	Created
-------------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.manikconfig.all;

entity debounce is
    
    generic (ACTIVE_HIGH : boolean := true;
             DEB_COUNT   : integer := 32);

    port (clk        : in  std_logic;
          reset      : in  std_logic;
          in_signal  : in  std_logic;
          out_signal : out std_logic);

end debounce;

architecture rtl of debounce is
    signal in_tmp, out_tmp : std_logic := '0';
    signal counter : integer := 0;
begin  -- rtl

    a_high: if ACTIVE_HIGH generate
        in_tmp     <= in_signal;
        out_signal <= out_tmp;
    end generate a_high;

    a_low: if ACTIVE_HIGH = false generate
        in_tmp     <= not in_signal;
        out_signal <= not out_tmp;
    end generate a_low;
    
    process (clk, reset)
    begin
        if reset = '1' then
            out_tmp <= '0';            
            counter <= 0;
        elsif rising_edge(clk) then
            if in_tmp = '0' then
                counter <= 0;
            elsif counter /= DEB_COUNT then                
                counter <= counter + 1;
            end if;

            if counter = DEB_COUNT and in_tmp ='1' then
                out_tmp <= '1';
            else
                out_tmp <= '0';
            end if;
        end if;
    end process;

end rtl;
