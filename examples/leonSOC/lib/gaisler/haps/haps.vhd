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
-- Package: 	haps
-- File:	haps.vhd
-- Author:	Mats Olsson - Gaisler Research
-- Description:	Synplicity's HAPS component and type declarations
------------------------------------------------------------------------------
library ieee;
use ieee.std_logic_1164.all;

library grlib;
use grlib.amba.all;
library gaisler;
use gaisler.memctrl.all;

package haps is

  subtype hapstrak_type is std_logic_vector(127 downto 0);

  type hapstrak_in_type is
    record
      din : hapstrak_type;
    end record;

  type hapstrak_out_type is
    record
      dout : hapstrak_type;
      oen : hapstrak_type;
      val : hapstrak_type;
      sig_out : hapstrak_type;
    end record;

  type test_1x2_in_type is
    record
      button_n : std_logic_vector(11 downto 0);
    end record;

  type test_1x2_out_type is
    record
      lcd_clk : std_logic_vector(1 downto 0);
      lcd_seg : std_logic_vector(31 downto 0);
      green_led_n : std_logic_vector(11 downto 0);
      red_led_n : std_logic_vector(11 downto 0);
    end record;

  component hapstrak
    generic (
      pindex      :     integer := 0;
      paddr       :     integer := 0;
      pmask       :     integer := 16#fff#);
    port (
      rst         : in  std_logic;
      clk         : in  std_logic;
      apbi        : in  apb_slv_in_type;
      apbo        : out apb_slv_out_type;
      hapstraki   : in  hapstrak_in_type;
      hapstrako   : out hapstrak_out_type);
  end component;

  component test_1x2
    generic (
      pindex      :     integer := 0;
      paddr       :     integer := 0;
      pmask       :     integer := 16#fff#;
      fdiv        :     integer := 1000000);
    port (
      rst         : in  std_logic;
      clk         : in  std_logic;
      apbi        : in  apb_slv_in_type;
      apbo        : out apb_slv_out_type;
      test_1x2i   : in  test_1x2_in_type;
      test_1x2o   : out test_1x2_out_type);
  end component;

  component sram_1x1 is
    generic (
      hindex:        integer := 0;
      pindex:        integer := 0;
      romaddr:       integer := 16#000#;
      rommask:       integer := 16#E00#;
      ioaddr:        integer := 16#200#;
      iomask:        integer := 16#E00#;
      ramaddr:       integer := 16#400#;
      rammask:       integer := 16#C00#;
      paddr:         integer := 0;
      pmask:         integer := 16#fff#;
      bus16:         integer := 0;
      tech:          integer := 0;
      netlist:       integer := 0);
    port (
      rst     : in  std_ulogic;
      clk     : in  std_ulogic;
      ahbsi   : in  ahb_slv_in_type;
      ahbso   : out ahb_slv_out_type;
      apbi    : in  apb_slv_in_type;
      apbo    : out apb_slv_out_type;
      sri     : in  memory_in_type;
      sro     : out memory_out_type);
  end component;
  component flash_1x1 is
    generic (
      hindex:        integer := 0;
      pindex:        integer := 0;
      romaddr:       integer := 16#000#;
      rommask:       integer := 16#E00#;
      ioaddr:        integer := 16#200#;
      iomask:        integer := 16#E00#;
      ramaddr:       integer := 16#400#;
      rammask:       integer := 16#C00#;
      paddr:         integer := 0;
      pmask:         integer := 16#fff#;
      bus16:         integer := 0;
      tech:          integer := 0;
      netlist:       integer := 0);
    port (
      rst     : in  std_ulogic;
      clk     : in  std_ulogic;
      ahbsi   : in  ahb_slv_in_type;
      ahbso   : out ahb_slv_out_type;
      apbi    : in  apb_slv_in_type;
      apbo    : out apb_slv_out_type;
      sri     : in  memory_in_type;
      sro     : out memory_out_type);
  end component;

  -- performs xor operation on all bits of vector v with signal s
  function "xor"(v : std_logic_vector; s : std_logic) return std_logic_vector;

  -- performs or operation on all bits of vector v with signal s
  function "or"(v : std_logic_vector; s : std_logic) return std_logic_vector;

end package;


package body haps is

  -- performs xor operation on all bits of vector v with signal s
  function "xor"(v : std_logic_vector; s : std_logic) return std_logic_vector is
    variable result : std_logic_vector(v'range);
  begin
    for i in v'range loop
      result(i) := v(i) xor s;
    end loop;
    return result;
  end "xor";

  -- performs or operation on all bits of vector v with signal s
  function "or"(v : std_logic_vector; s : std_logic) return std_logic_vector is
    variable result : std_logic_vector(v'range);
  begin
    for i in v'range loop
      result(i) := v(i) or s;
    end loop;
    return result;
  end "or";

end package body;



