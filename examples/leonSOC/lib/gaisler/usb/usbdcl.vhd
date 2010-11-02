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
-- Entity: 	usbdcl
-- File:	usbdcl.vhd
-- Author:	Andreas Hansen
-- Modified:    Marko Isomaki
-- Description:	USB Debug Communication Link
------------------------------------------------------------------------------
library ieee;
use ieee.std_logic_1164.all;
library grlib;
use grlib.stdlib.all;
use grlib.amba.all;
use grlib.devices.all;
library gaisler;
use gaisler.misc.all;
use gaisler.usb.all;
library techmap;
use techmap.gencomp.all;

entity usbdcl is
  generic (
    hindex     : integer := 0;
    memtech    : integer := DEFMEMTECH
    );
  port (
    uclk       : in  std_ulogic;
    usbi       : in  usb_in_type;
    usbo       : out usb_out_type;
    hclk       : in  std_ulogic;
    hrst       : in  std_ulogic;
    ahbi       : in  ahb_mst_in_type;
    ahbo       : out ahb_mst_out_type
   );
end;

architecture bhv of usbdcl is
  constant REVISION   : amba_version_type := 0;

  constant memibits  : integer := 11;
  constant memobits  : integer := 11;

  signal iri       : usb_memi_in_type;
  signal ori       : usb_memo_in_type;
  signal oraddr    : std_logic_vector(memobits-1 downto 0);
  signal iraddr    : std_logic_vector(memibits-1 downto 0);
  signal ordata    : std_logic_vector(31 downto 0);
  signal irdata    : std_logic_vector(31 downto 0);
  signal vcc       : std_ulogic;
  signal dmai      : ahb_dma_in_type;
  signal dmao      : ahb_dma_out_type;

  component usbdclc is
  port (
    uclk       : in  std_ulogic;
    usbi       : in  usb_in_type;
    usbo       : out usb_out_type;
    hclk       : in  std_ulogic;
    hrst       : in  std_ulogic;
    iri        : out usb_memi_in_type;
    ori        : out usb_memo_in_type;
    dmai       : out ahb_dma_in_type;
    dmao       : in  ahb_dma_out_type;
    oraddr     : out std_logic_vector(10 downto 0);
    iraddr     : out std_logic_vector(10 downto 0);
    ordata     : in  std_logic_vector(31 downto 0);
    irdata     : in  std_logic_vector(31 downto 0)
   );
  end component;

begin

  vcc <= '1';

  u0 : usbdclc
    port map ( uclk, usbi, usbo, hclk, hrst, iri,
    ori, dmai, dmao, oraddr, iraddr, ordata, irdata);

  inram : syncram_2p
  generic map (memtech, memibits, 32, 1)
  port map (
    uclk, vcc, iraddr, irdata, hclk,
    iri.wenable, iri.address, iri.din);
  
  outram : syncram_2p
  generic map (memtech, memobits, 32, 1)
  port map (
    hclk, vcc, oraddr, ordata, uclk,
    ori.wenable, ori.address, ori.din);

  ahbmst0 : ahbmst
    generic map (incaddr => 0, hindex => hindex, 
      venid => VENDOR_GAISLER, devid => GAISLER_USBDCL)
    port map (hrst, hclk, dmai, dmao, ahbi, ahbo);

  -- pragma translate_off
  bootmsg : report_version 
  generic map (
    "grusb" & tost(hindex) & ": USB 2.0 DCL rev " & tost(REVISION)
    & " " & tost(2**(memobits+2)) & " B Out Buffer " & tost(2**(memibits+2)) & " B In Buffer");
-- pragma translate_on
end;
