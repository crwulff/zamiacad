------------------------------------------------------------------------------
--  This file is a part of the GRLIB VHDL IP LIBRARY
--  Copyright (C) 2003, Gaisler Research
--
--  This program is free software; you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation; either version 2 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program; if not, write to the Free Software
--  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
-----------------------------------------------------------------------------
-- Entity: 	test_1x2
-- File:	test_1x2.vhd
-- Author:	Mats Olsson - Gaisler Research
-- Description:	Interface for Synplicity TEST_1x2 daughter board
------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
library grlib;
use grlib.amba.all;
use grlib.stdlib.all;
use grlib.devices.all;
library gaisler;
use gaisler.haps.all;
--pragma translate_off
use std.textio.all;
--pragma translate_on

entity test_1x2 is
  generic (
    pindex : integer := 0;
    paddr : integer := 0;
    pmask : integer := 16#fff#;
    fdiv  : integer := 1000000
    );
  port (
    rst : in std_logic;
    clk : in std_logic;
    apbi : in apb_slv_in_type;
    apbo : out apb_slv_out_type;
    test_1x2i : in test_1x2_in_type;
    test_1x2o : out test_1x2_out_type
    );
end;

architecture rtl of test_1x2 is

  constant REVISION : integer := 0;

  constant pconfig : apb_config_type := (
    0 => ahb_device_reg (VENDOR_GAISLER, GAISLER_TEST_1X2, 0, REVISION, 0),
    1 => apb_iobar(paddr, pmask));

  type registers is
    record
      din1 : std_logic_vector(31 downto 0);  -- Buttons
      din2 : std_logic_vector(31 downto 0);
      dout1 : std_logic_vector(31 downto 0);  -- LCD
      dout2 : std_logic_vector(31 downto 0);  -- LEDs
    end record;

  signal r, rin : registers;

  signal lcd_clk : std_logic;

begin

  comb : process(rst, r, apbi, test_1x2i, lcd_clk)
    variable dout1, dout2, din, readdata : std_logic_vector(31 downto 0);
    variable v : registers;

  begin

    din := (others => '0');
    din(11 downto 0) := test_1x2i.button_n;

    v := r;
    v.din2 := r.din1;
    v.din1 := din;
    dout1 := (others => '0');
    dout2 := (others => '0');
    dout1 := r.dout1;
    dout2 := r.dout2;

    -- read registers

    readdata := (others => '0');
    case apbi.paddr(3 downto 2) is
      when "00" => readdata := r.din2;   -- buttons
      when "01" => readdata := r.dout1;  -- LCD
      when "10" => readdata := r.dout2;  -- LEDs
      when others =>
    end case;

    -- write registers

    if (apbi.psel(pindex) and apbi.penable and apbi.pwrite) = '1' then
      case apbi.paddr(3 downto 2) is
        when "00" => null;                                 -- buttons
        when "01" => v.dout1 := apbi.pwdata(31 downto 0);  -- LCD
        when "10" => v.dout2 := apbi.pwdata(31 downto 0);  -- LEDs
        when others =>
      end case;
    end if;


    -- reset operation

    if rst = '0' then
      v.dout1 := (others => '0');
      v.dout2 := (others => '0');
    end if;

    rin <= v;

    apbo.prdata <= readdata; 	-- drive apb read bus

    test_1x2o.green_led_n <= dout2(11 downto 0);
    test_1x2o.red_led_n <= dout2(23 downto 12);
    test_1x2o.lcd_seg <= (dout1 or not rst) xor lcd_clk;
    test_1x2o.lcd_clk(0) <= lcd_clk;
    test_1x2o.lcd_clk(1) <= lcd_clk;


  end process;

  apbo.pindex <= pindex;
  apbo.pconfig <= pconfig;

  -- registers

  regs : process(clk, rst)
    variable lcd_clk_counter : Integer;
  begin
    if rst = '0' then
      lcd_clk <= '0';
      lcd_clk_counter := fdiv;
    elsif rising_edge(clk) then
      -- create lcd clock with f = f(clk)/fdiv
      lcd_clk_counter := lcd_clk_counter - 1;
      if lcd_clk_counter = 0 then
        lcd_clk <= not lcd_clk;
        lcd_clk_counter := fdiv;
      end if;
      r <= rin;
    end if;
  end process;

  -- boot message

-- pragma translate_off
  bootmsg : report_version
    generic map ("test_1x2" & tost(pindex) & ": " &  "Synplicity HAPS TEST_1x2 Unit rev " & tost(REVISION));
-- pragma translate_on

end;
