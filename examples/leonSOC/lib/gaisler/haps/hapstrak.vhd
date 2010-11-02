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
-- Entity: 	hapstrak
-- File:	hapstrak.vhd
-- Author:	Mats Olsson - Gaisler Research
-- Description:	HAPS general-purpose 119 pin I/O port
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


entity hapstrak is
  generic (
    pindex   : integer := 0;
    paddr    : integer := 0;
    pmask    : integer := 16#fff#
    );
  port (
    rst    : in  std_ulogic;
    clk    : in  std_ulogic;
    apbi   : in  apb_slv_in_type;
    apbo   : out apb_slv_out_type;
    hapstraki  : in  hapstrak_in_type;
    hapstrako  : out hapstrak_out_type
    );
end;


architecture rtl of hapstrak is

  constant REVISION : integer := 0;

  constant pconfig : apb_config_type := (
    0 => ahb_device_reg (VENDOR_GAISLER, GAISLER_HAPSTRAK, 0, REVISION, 0),
    1 => apb_iobar(paddr, pmask));


  type register_array is array(0 to 3) of std_logic_vector(31 downto 0);

  type registers is
    record
      din1,
        din2,
        dout,
        ilat,
        dir    	:  register_array;
    end record;

  signal r, rin : registers;

begin

  comb : process(rst, r, apbi, hapstraki)
    variable dout, dir, pval, din : register_array;
    variable readdata : std_logic_vector(31 downto 0);
    variable v : registers;
    
  begin

    din := (others => (others => '0'));

    for i in 0 to 3 loop
      din(i) := hapstraki.din((i+1)*32-1 downto i*32);
    end loop;
    
    v := r;
    v.din2 := r.din1;
    v.din1 := din;
    v.ilat := r.din2;
    dout := (others => (others => '0'));
    dir := (others => (others => '0'));
    dir := r.dir;
    dout := r.dout;

-- read registers

    readdata := (others => '0');
    case apbi.paddr(5 downto 2) is
      when "0000" => readdata := r.din2(0);
      when "0001" => readdata := r.din2(1);
      when "0010" => readdata := r.din2(2);
      when "0011" => readdata := r.din2(3);
      when "0100" => readdata := r.dout(0);
      when "0101" => readdata := r.dout(1);
      when "0110" => readdata := r.dout(2);
      when "0111" => readdata := r.dout(3);                       
      when "1000" => readdata := r.dir(0);
      when "1001" => readdata := r.dir(1);
      when "1010" => readdata := r.dir(2);
      when "1011" => readdata := r.dir(3);
      when others =>
    end case;

    -- write registers

    if (apbi.psel(pindex) and apbi.penable and apbi.pwrite) = '1' then
      case apbi.paddr(5 downto 2) is
        when "0000" | "0001" | "0010" | "0011" => null;
        when "0100" => v.dout(0) := apbi.pwdata(31 downto 0);
        when "0101" => v.dout(1) := apbi.pwdata(31 downto 0);
        when "0110" => v.dout(2) := apbi.pwdata(31 downto 0);
        when "0111" => v.dout(3) := apbi.pwdata(31 downto 0);                
        when "1000" => v.dir(0) := apbi.pwdata(31 downto 0);
        when "1001" => v.dir(1) := apbi.pwdata(31 downto 0);
        when "1010" => v.dir(2) := apbi.pwdata(31 downto 0);
        when "1011" => v.dir(3) := apbi.pwdata(31 downto 0);
        when others =>
      end case;
    end if;

    -- drive filtered inputs on the output record

    pval := (others => (others => '0'));
    pval := r.din2;

-- reset operation

    if rst = '0' then
      v.dout := (others => (others => '0'));
    end if;

    rin <= v;

    apbo.prdata <= readdata; 	-- drive apb read bus

    for i in 0 to 3 loop
      hapstrako.dout((i+1)*32-1 downto i*32) <= dout(i);
      hapstrako.oen((i+1)*32-1 downto i*32) <= dir(i);
      hapstrako.val((i+1)*32-1 downto i*32) <= pval(i);
      hapstrako.sig_out((i+1)*32-1 downto i*32) <= din(i);
    end loop;

  end process;

  apbo.pindex <= pindex;
  apbo.pconfig <= pconfig;

  -- registers

  regs : process(clk, rst)
  begin
    if rst = '0' then
      r.dir <= (others => (others => '0'));
    elsif rising_edge(clk) then
      r <= rin;
    end if;
  end process;

  -- boot message

-- pragma translate_off
  bootmsg : report_version
    generic map ("hapstrak" & tost(pindex) & ": " &  "HapsTrak Unit rev " & tost(REVISION));
-- pragma translate_on

end;


