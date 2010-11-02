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
-- Package: 	gencomp
-- File:	gencomp.vhd
-- Author:	Jiri Gaisler - Gaisler Research
-- Description:	Delcation of portable memory modules
------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
library grlib;
use grlib.amba.all;

package gencomp is

---------------------------------------------------------------------------
-- BASIC DECLARATIONS
---------------------------------------------------------------------------

-- technologies and libraries

constant NTECH : integer := 30;
type tech_ability_type is array (0 to NTECH) of integer;

constant inferred    : integer := 0;
constant virtex      : integer := 1;
constant virtex2     : integer := 2;
constant memvirage   : integer := 3;
constant axcel       : integer := 4;
constant proasic     : integer := 5;
constant atc18s      : integer := 6;
constant altera      : integer := 7;
constant umc         : integer := 8;
constant rhumc       : integer := 9;
constant apa3        : integer := 10;
constant spartan3    : integer := 11;
constant ihp25       : integer := 12; 
constant rhlib18t    : integer := 13;
constant virtex4     : integer := 14; 
constant lattice     : integer := 15;
constant ut25        : integer := 16;
constant spartan3e   : integer := 17;
constant peregrine   : integer := 18;
constant memartisan  : integer := 19;
constant virtex5     : integer := 20;
constant custom1     : integer := 21;
constant ihp25rh     : integer := 22; 
constant stratix1    : integer := 23;
constant stratix2    : integer := 24;
constant eclipse     : integer := 25;
constant stratix3    : integer := 26;
constant cyclone3    : integer := 27;
constant memvirage90 : integer := 28;
constant tsmc90      : integer := 29;
constant easic90    : integer := 30;

constant DEFMEMTECH  : integer := inferred; 
constant DEFPADTECH  : integer := inferred; 
constant DEFFABTECH  : integer := inferred; 

constant is_fpga : tech_ability_type :=
	(inferred => 1, virtex => 1, virtex2 => 1, axcel => 1, 
	 proasic => 1, altera => 1, apa3 => 1, spartan3 => 1,
         virtex4 => 1, lattice => 1, spartan3e => 1, virtex5 => 1,
	 stratix1 => 1, stratix2 => 1, eclipse => 1, 
	 stratix3 => 1, cyclone3 => 1, others => 0);

constant infer_mul : tech_ability_type := is_fpga;

constant syncram_2p_write_through : tech_ability_type :=
	(inferred => 0, virtex => 0, virtex2 => 1, memvirage => 1, 
	 axcel => 0, proasic => 0, atc18s => 0, altera => 0, 
	 umc => 0, rhumc => 1, apa3 => 0, spartan3 => 1,
         ihp25 => 0, rhlib18t => 0, virtex4 => 1, lattice => 0,
	 ut25 => 0, spartan3e => 1, virtex5 => 1, eclipse => 1,         
	 memvirage90 => 0, others => 0);

constant regfile_3p_write_through : tech_ability_type :=
	(inferred => 0, virtex => 0, virtex2 => 1, memvirage => 1, 
	 axcel => 0, proasic => 0, atc18s => 0, altera => 0, 
	 umc => 0, rhumc => 1, apa3 => 0, spartan3 => 1,
         ihp25 => 1, rhlib18t => 0, virtex4 => 1, lattice => 0,
	 ut25 => 0, spartan3e => 1, virtex5 => 1, ihp25rh => 1,
	 eclipse => 1, memvirage90 => 0, others => 0);

constant regfile_3p_infer : tech_ability_type :=
	(inferred => 1, rhumc => 1, ihp25 => 1, rhlib18t => 0,
	 peregrine => 1, ihp25rh => 1, umc => 1, others => 0);

constant syncram_2p_dest_rw_collision : tech_ability_type :=
        (memartisan => 1, others => 0);

constant syncram_dp_dest_rw_collision : tech_ability_type :=
        (memartisan => 1, others => 0);

constant has_sram : tech_ability_type :=
	(inferred => 1, virtex => 1, virtex2 => 1, memvirage => 1, 
	 axcel => 1, proasic => 1, atc18s => 0, altera => 1, 
	 umc => 1, rhumc => 1, apa3 => 1, spartan3 => 1,
         ihp25 => 1, rhlib18t => 1, virtex4 => 1, lattice => 1,
	 ut25 => 1, spartan3e => 1, virtex5 => 1, eclipse => 1,
	 memvirage90 => 1, others => 1);

constant has_2pram : tech_ability_type :=
	( atc18s => 0, umc => 0, rhumc => 0, ihp25 => 0, others => 1);

constant has_dpram : tech_ability_type :=
	(virtex => 1, virtex2 => 1, memvirage => 1, axcel => 1,
	 altera => 1, apa3 => 1, spartan3 => 1, virtex4 => 1, 
	 lattice => 1, spartan3e => 1, memartisan => 1, virtex5 => 1,
	 custom1 => 1, stratix1 => 1, stratix2 => 1, stratix3 => 1,
	 cyclone3 => 1, memvirage90 => 1, others => 0);

constant has_sram64 : tech_ability_type :=
	(inferred => 0, virtex2 => 1, spartan3 => 1, virtex4 => 1,
	 spartan3e => 1, memartisan => 1, virtex5 => 1,
	 custom1 => 0, others => 0);

constant padoen_polarity : tech_ability_type :=
        (inferred => 0, virtex => 0, virtex2 => 0, memvirage => 0,
	 axcel => 1, proasic => 1, atc18s => 0, altera => 0,
	 umc => 0, rhumc => 1, spartan3 => 0, apa3 => 1,
         ihp25 => 1, rhlib18t => 0, virtex4 => 0, lattice => 0,
	 ut25 => 1, spartan3e => 0, peregrine => 1, others => 0);

constant has_pads : tech_ability_type :=
	(inferred => 0, virtex => 1, virtex2 => 1, memvirage => 0, 
	 axcel => 1, proasic => 1, atc18s => 1, altera => 0, 
	 umc => 1, rhumc => 1, apa3 => 1, spartan3 => 1,
         ihp25 => 1, rhlib18t => 1, virtex4 => 1, lattice => 0,
	 ut25 => 1, spartan3e => 1, peregrine => 1, virtex5 => 1,
	 others => 0);

constant has_ds_pads : tech_ability_type :=
	(inferred => 0, virtex => 1, virtex2 => 1, memvirage => 0, 
	 axcel => 1, proasic => 0, atc18s => 0, altera => 0, 
	 umc => 0, rhumc => 0, apa3 => 0, spartan3 => 1,
         ihp25 => 0, rhlib18t => 1, virtex4 => 1, lattice => 0,
	 ut25 => 1, spartan3e => 1, virtex5 => 1, others => 0);

constant has_ds_combo : tech_ability_type :=
	( rhumc => 1, ut25 => 1, others => 0);

constant has_clkand : tech_ability_type :=
	( virtex2 => 1, spartan3 => 1, spartan3e => 1, virtex4 => 1,
	  virtex5 => 1, ut25 => 1, others => 0);

constant has_clkmux : tech_ability_type :=
	( virtex2 => 1, spartan3 => 1, spartan3e => 1, virtex4 => 1,
	  virtex5 => 1, others => 0);

constant has_techbuf : tech_ability_type :=
        ( virtex => 1, virtex2 => 1, virtex4 => 1, virtex5 => 1,
          spartan3 => 1, spartan3e => 1, axcel => 1, ut25 => 1,
	  apa3 => 1, others => 0);
 
constant has_tapsel : tech_ability_type :=
        ( virtex => 1, virtex2 => 1, virtex4 => 1, virtex5 => 1,
          spartan3 => 1, spartan3e => 1, others => 0);
 
constant need_extra_sync_reset : tech_ability_type :=
	(ut25 => 1, rhumc => 1, others => 0);

-- pragma translate_off

  subtype tech_description is string(1 to 10);

  type tech_table_type is array (0 to NTECH) of tech_description;

  constant tech_table : tech_table_type := (
  inferred  => "inferred  ", virtex    => "virtex    ", 
  virtex2   => "virtex2   ", memvirage => "virage    ", 
  axcel     => "axcel     ", proasic   => "proasic   ",
  atc18s    => "atc18s    ", altera    => "altera    ",
  umc       => "umc18     ", rhumc     => "rhumc     ",
  apa3      => "proasic3  ", spartan3  => "spartan3  ",
  ihp25     => "ihp25     ", rhlib18t  => "rhlib18t  ",
  virtex4   => "virtex4   ", lattice   => "lattice   ",
  ut25      => "ut025crh  ", spartan3e => "spartan3e ",
  peregrine => "peregrine ", memartisan => "artisan   ",
  virtex5   => "virtex5   ", custom1   => "custom1   ",
  ihp25rh   => "ihp25rh   ", stratix1  => "stratix   ",
  stratix2  => "stratixii ", eclipse   => "eclipse   ",
  stratix3  => "stratixiii", cyclone3  => "cycloneiii",
  memvirage90 => "virage90  ", tsmc90 =>  "tsmc90    ",
  easic90  =>  "nextreme  "
);

-- pragma translate_on

-- input/output voltage

constant x18v      : integer := 1;
constant x25v      : integer := 2;
constant x33v      : integer := 3;
constant x50v      : integer := 5;

-- input/output levels

constant ttl      : integer := 0;
constant cmos     : integer := 1;
constant pci33    : integer := 2;
constant pci66    : integer := 3;
constant lvds     : integer := 4;
constant sstl2_i  : integer := 5;
constant sstl2_ii : integer := 6;
constant sstl3_i  : integer := 7;
constant sstl3_ii : integer := 8;
constant sstl18_i : integer := 9;
constant sstl18_ii: integer := 10;

-- pad types

constant normal   : integer := 0;
constant pullup   : integer := 1;
constant pulldown : integer := 2;
constant opendrain: integer := 3;
constant schmitt  : integer := 4;
constant dci      : integer := 5;

---------------------------------------------------------------------------
-- MEMORY
---------------------------------------------------------------------------

-- synchronous single-port ram
  component syncram
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8);
  port (
    clk      : in std_ulogic;
    address  : in std_logic_vector((abits -1) downto 0);
    datain   : in std_logic_vector((dbits -1) downto 0);
    dataout  : out std_logic_vector((dbits -1) downto 0);
    enable   : in std_ulogic;
    write    : in std_ulogic); 
  end component;

-- synchronous two-port ram (1 read, 1 write port)
  component syncram_2p
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8; sepclk : integer := 0;
           wrfst : integer := 0);
  port (
    rclk     : in std_ulogic;
    renable  : in std_ulogic;
    raddress : in std_logic_vector((abits -1) downto 0);
    dataout  : out std_logic_vector((dbits -1) downto 0);
    wclk     : in std_ulogic;
    write    : in std_ulogic;
    waddress : in std_logic_vector((abits -1) downto 0);
    datain   : in std_logic_vector((dbits -1) downto 0));
  end component;

-- synchronous dual-port ram (2 read/write ports)
  component syncram_dp
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8);
  port (
    clk1     : in std_ulogic;
    address1 : in std_logic_vector((abits -1) downto 0);
    datain1  : in std_logic_vector((dbits -1) downto 0);
    dataout1 : out std_logic_vector((dbits -1) downto 0);
    enable1  : in std_ulogic;
    write1   : in std_ulogic;
    clk2     : in std_ulogic;
    address2 : in std_logic_vector((abits -1) downto 0);
    datain2  : in std_logic_vector((dbits -1) downto 0);
    dataout2 : out std_logic_vector((dbits -1) downto 0);
    enable2  : in std_ulogic;
    write2   : in std_ulogic); 
  end component;

-- synchronous 3-port regfile (2 read, 1 write port)
  component regfile_3p
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8;
           wrfst : integer := 0; numregs : integer := 64);
  port (
    wclk   : in  std_ulogic;
    waddr  : in  std_logic_vector((abits -1) downto 0);
    wdata  : in  std_logic_vector((dbits -1) downto 0);
    we     : in  std_ulogic;
    rclk   : in  std_ulogic;
    raddr1 : in  std_logic_vector((abits -1) downto 0);
    re1    : in  std_ulogic;
    rdata1 : out std_logic_vector((dbits -1) downto 0);
    raddr2 : in  std_logic_vector((abits -1) downto 0);
    re2    : in  std_ulogic;
    rdata2 : out std_logic_vector((dbits -1) downto 0));
  end component;

-- 64-bit synchronous single-port ram with 32-bit write strobe
  component syncram64
  generic (tech : integer := 0; abits : integer := 6);
  port (
    clk     : in  std_ulogic;
    address : in  std_logic_vector (abits -1 downto 0);
    datain  : in  std_logic_vector (63 downto 0);
    dataout : out std_logic_vector (63 downto 0);
    enable  : in  std_logic_vector (1 downto 0);
    write   : in  std_logic_vector (1 downto 0));
  end component;

  component syncramft
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8;
	ft : integer range 0 to 2 := 0 );
  port (
    clk      : in std_ulogic;
    address  : in std_logic_vector((abits -1) downto 0);
    datain   : in std_logic_vector((dbits -1) downto 0);
    dataout  : out std_logic_vector((dbits -1) downto 0);
    write    : in std_ulogic; 
    enable   : in std_ulogic;
    error    : out std_logic_vector((dbits + 7) / 8 downto 0));
  end component;

  component syncram_2pft
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8;
	sepclk : integer := 0; wrfst : integer := 0; ft : integer := 0);
  port (
    rclk     : in std_ulogic;
    renable  : in std_ulogic;
    raddress : in std_logic_vector((abits -1) downto 0);
    dataout  : out std_logic_vector((dbits -1) downto 0);
    wclk     : in std_ulogic;
    write    : in std_ulogic;
    waddress : in std_logic_vector((abits -1) downto 0);
    datain   : in std_logic_vector((dbits -1) downto 0);
    error    : out std_logic_vector(((dbits + 7) / 8)-1 downto 0));
  end component;

  component syncfifo
  generic (tech : integer := 0; abits : integer := 6; dbits : integer := 8;
	sepclk : integer := 0; wrfst : integer := 0);
  port (
    rst      : in std_ulogic;
    rclk     : in std_ulogic;
    renable  : in std_ulogic;
    dataout  : out std_logic_vector((dbits -1) downto 0);
    wclk     : in std_ulogic;
    write    : in std_ulogic;
    datain   : in std_logic_vector((dbits -1) downto 0);
    full     : out std_ulogic;
    empty    : out std_ulogic
  );
  end component;


---------------------------------------------------------------------------
-- PADS
---------------------------------------------------------------------------

component inpad 
  generic (tech : integer := 0; level : integer := 0; 
	voltage : integer := x33v; filter : integer := 0;
	strength : integer := 0);
  port (pad : in std_ulogic; o : out std_ulogic);
end component; 

component inpadv 
  generic (tech : integer := 0; level : integer := 0; 
	   voltage : integer := x33v; width : integer := 1);
  port (
    pad : in  std_logic_vector(width-1 downto 0); 
    o   : out std_logic_vector(width-1 downto 0));
end component; 

component iopad 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	   voltage : integer := x33v; strength : integer := 12; 
	   oepol : integer := 0);
  port (pad : inout std_ulogic; i, en : in std_ulogic; o : out std_ulogic);
end component;

component iopadv 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	voltage : integer := x33v; strength : integer := 12; width : integer := 1; 
	   oepol : integer := 0);
  port (
    pad : inout std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0);
    en  : in  std_ulogic;
    o   : out std_logic_vector(width-1 downto 0));
end component;

component iopadvv is
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	voltage : integer := x33v; strength : integer := 12; width : integer := 1;
	oepol : integer := 0);
  port (
    pad : inout std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0);
    en  : in  std_logic_vector(width-1 downto 0);
    o   : out std_logic_vector(width-1 downto 0));
end component; 

component iodpad 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	   voltage : integer := x33v; strength : integer := 12; 
	   oepol : integer := 0);
  port (pad : inout std_ulogic; i : in std_ulogic; o : out std_ulogic);
end component;

component iodpadv 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	voltage : integer := x33v; strength : integer := 12; width : integer := 1; 
	   oepol : integer := 0);
  port (
    pad : inout std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0);
    o   : out std_logic_vector(width-1 downto 0));
end component;

component outpad 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	   voltage : integer := x33v; strength : integer := 12);
  port (pad : out std_ulogic; i : in std_ulogic);
end component;

component outpadv 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0; 
	   voltage : integer := x33v; strength : integer := 12; width : integer := 1);
  port (
    pad : out std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0));
end component; 

component odpad 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	   voltage : integer := x33v; strength : integer := 12; 
	   oepol : integer := 0);
  port (pad : out std_ulogic; i : in std_ulogic);
end component;

component odpadv 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	voltage : integer := x33v; strength : integer := 12; width : integer := 1; 
	   oepol : integer := 0);
  port (
    pad : out std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0));
end component; 

component toutpad 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	   voltage : integer := x33v; strength : integer := 12; 
	   oepol : integer := 0);
  port (pad : out std_ulogic; i, en : in std_ulogic);
end component;

component toutpadv 
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	voltage : integer := x33v; strength : integer := 12; width : integer := 1; 
	   oepol : integer := 0);
  port (
    pad : out std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0);
    en  : in  std_ulogic);
end component;

component toutpadvv is
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	voltage : integer := x33v; strength : integer := 12; width : integer := 1;
	oepol : integer := 0);
  port (
    pad : out std_logic_vector(width-1 downto 0); 
    i   : in  std_logic_vector(width-1 downto 0);
    en  : in  std_logic_vector(width-1 downto 0));
end component;

component clkpad 
  generic (tech : integer := 0; level : integer := 0; 
	   voltage : integer := x33v; arch : integer := 0);
  port (pad : in std_ulogic; o : out std_ulogic);
end component; 

component inpad_ds
  generic (tech : integer := 0; level : integer := lvds; voltage : integer := x33v);
  port (padp, padn : in std_ulogic; o : out std_ulogic);
end component; 

component clkpad_ds
  generic (tech : integer := 0; level : integer := lvds; voltage : integer := x33v);
  port (padp, padn : in std_ulogic; o : out std_ulogic);
end component; 

component inpad_dsv
  generic (tech : integer := 0; level : integer := lvds; 
	   voltage : integer := x33v; width : integer := 1);
  port (
    padp : in  std_logic_vector(width-1 downto 0); 
    padn : in  std_logic_vector(width-1 downto 0); 
    o   : out std_logic_vector(width-1 downto 0));
end component; 

component iopad_ds
  generic (tech : integer := 0; level : integer := 0; slew : integer := 0;
	   voltage : integer := x33v; strength : integer := 12; 
	   oepol : integer := 0);
  port (padp, padn : inout std_ulogic; i, en : in std_ulogic; o : out std_ulogic);
end component;

component outpad_ds 
  generic (tech : integer := 0; level : integer := lvds; 
	voltage : integer := x33v; oepol : integer := 0);
  port (padp, padn : out std_ulogic; i, en : in std_ulogic);
end component;

component outpad_dsv
  generic (tech : integer := 0; level : integer := lvds;
	voltage : integer := x33v; width : integer := 1);
  port (
    padp : out std_logic_vector(width-1 downto 0); 
    padn : out std_logic_vector(width-1 downto 0); 
    i, en: in  std_logic_vector(width-1 downto 0));
end component;

component lvds_combo  is
  generic (tech : integer := 0; voltage : integer := 0; width : integer := 1;
		oepol : integer := 0);
  port (odpadp, odpadn, ospadp, ospadn : out std_logic_vector(0 to width-1); 
        odval, osval, en : in std_logic_vector(0 to width-1); 
	idpadp, idpadn, ispadp, ispadn : in std_logic_vector(0 to width-1);
	idval, isval : out std_logic_vector(0 to width-1);
	lvdsref : in std_logic := '1'
  );
end component;

---------------------------------------------------------------------------
-- BUFFERS
---------------------------------------------------------------------------

  component techbuf is
    generic(
      buftype  :  integer range 0 to 4 := 0;
      tech     :  integer range 0 to NTECH := inferred);
    port(
      i        :  in  std_ulogic;
      o        :  out std_ulogic
    );
  end component;

---------------------------------------------------------------------------
-- CLOCK GENERATION
---------------------------------------------------------------------------

type clkgen_in_type is record
  pllref  : std_logic;			-- optional reference for PLL
  pllrst  : std_logic;			-- optional reset for PLL
  pllctrl : std_logic_vector(1 downto 0);  -- optional control for PLL
  clksel  : std_logic_vector(1 downto 0);  -- optional clock select
end record;

type clkgen_out_type is record
  clklock : std_logic;
  pcilock : std_logic;
end record;

component clkgen 
  generic (
    tech     : integer := DEFFABTECH; 
    clk_mul  : integer := 1; 
    clk_div  : integer := 1;
    sdramen  : integer := 0;
    noclkfb  : integer := 1;
    pcien    : integer := 0;
    pcidll   : integer := 0;
    pcisysclk: integer := 0;
    freq     : integer := 25000;
    clk2xen  : integer := 0;
    clksel   : integer := 0;             -- enable clock select         
    clk_odiv : integer := 0);             -- Proasic3 output divider
port (
    clkin   : in  std_logic;
    pciclkin: in  std_logic;
    clk     : out std_logic;			-- main clock
    clkn    : out std_logic;			-- inverted main clock
    clk2x   : out std_logic;			-- 2x clock
    sdclk   : out std_logic;			-- SDRAM clock
    pciclk  : out std_logic;			-- PCI clock
    cgi     : in clkgen_in_type;
    cgo     : out clkgen_out_type;
    clk4x   : out std_logic;			-- 4x clock
    clk1xu  : out std_logic;			-- unscaled 1X clock
    clk2xu  : out std_logic);			-- unscaled 2X clock
end component;

component clkand 
  generic( tech : integer := 0;
           ren  : integer range 0 to 1 := 0); -- registered enable           
  port(
    i      :  in  std_ulogic;
    en     :  in  std_ulogic;
    o      :  out std_ulogic
  );
end component;

component clkmux 
  generic( tech : integer := 0;
           rsel : integer range 0 to 1 := 0); -- registered sel           
  port(
    i0, i1  :  in  std_ulogic;
    sel     :  in  std_ulogic;
    o       :  out std_ulogic;
    rst     :  in  std_ulogic := '1'    
  );
end component;




---------------------------------------------------------------------------
-- TAP controller   
---------------------------------------------------------------------------

component tap
  generic (
    tech   : integer := 0;    
    irlen  : integer range 2 to 8 := 4;
    idcode : integer range 0 to 255 := 9;    
    manf   : integer range 0 to 2047 := 804;
    part   : integer range 0 to 65535 := 0;
    ver    : integer range 0 to 15 := 0;
    trsten : integer range 0 to 1 := 1;    
    scantest : integer := 0);
  port (
    trst         : in std_ulogic;
    tck         : in std_ulogic;
    tms         : in std_ulogic;
    tdi         : in std_ulogic;
    tdo         : out std_ulogic;
    tapo_tck    : out std_ulogic;
    tapo_tdi    : out std_ulogic;
    tapo_inst   : out std_logic_vector(7 downto 0);
    tapo_rst    : out std_ulogic;
    tapo_capt   : out std_ulogic;
    tapo_shft   : out std_ulogic;
    tapo_upd    : out std_ulogic;
    tapo_xsel1  : out std_ulogic;
    tapo_xsel2  : out std_ulogic;
    tapi_en1    : in std_ulogic;
    tapi_tdo1   : in std_ulogic;
    tapi_tdo2   : in std_ulogic;
    testen      : in std_ulogic := '0';
    testrst     : in std_ulogic := '1';
    tdoen       : out std_ulogic
    );
end component;

---------------------------------------------------------------------------
-- DDR registers and PHY
---------------------------------------------------------------------------

component ddr_ireg is
generic ( tech : integer);
port ( Q1 : out std_ulogic;
         Q2 : out std_ulogic;
         C1 : in std_ulogic;
         C2 : in std_ulogic;
         CE : in std_ulogic;
         D : in std_ulogic;
         R : in std_ulogic;
         S : in std_ulogic);
end component;

component ddr_oreg is generic ( tech : integer);
  port
    ( Q : out std_ulogic;
      C1 : in std_ulogic;
      C2 : in std_ulogic;
      CE : in std_ulogic;
      D1 : in std_ulogic;
      D2 : in std_ulogic;
      R : in std_ulogic;
      S : in std_ulogic);
end component;

component ddrphy
  generic (tech : integer := virtex2; MHz : integer := 100; 
	rstdelay : integer := 200; dbits : integer := 16; 
	clk_mul : integer := 2 ; clk_div : integer := 2;
	rskew : integer :=0);
  port (
    rst       : in  std_ulogic;
    clk       : in  std_logic;          	-- input clock
    clkout    : out std_ulogic;			-- system clock
    clkread   : out std_ulogic;			-- read clock
    lock      : out std_ulogic;			-- DCM locked
    ddr_clk 	: out std_logic_vector(2 downto 0);
    ddr_clkb	: out std_logic_vector(2 downto 0);
    ddr_clk_fb_out  : out std_logic;
    ddr_clk_fb  : in std_logic;
    ddr_cke  	: out std_logic_vector(1 downto 0);
    ddr_csb  	: out std_logic_vector(1 downto 0);
    ddr_web  	: out std_ulogic;                       -- ddr write enable
    ddr_rasb  	: out std_ulogic;                       -- ddr ras
    ddr_casb  	: out std_ulogic;                       -- ddr cas
    ddr_dm   	: out std_logic_vector (dbits/8-1 downto 0);    -- ddr dm
    ddr_dqs  	: inout std_logic_vector (dbits/8-1 downto 0);    -- ddr dqs
    ddr_ad      : out std_logic_vector (13 downto 0);   -- ddr address
    ddr_ba      : out std_logic_vector (1 downto 0);    -- ddr bank address
    ddr_dq    	: inout  std_logic_vector (dbits-1 downto 0); -- ddr data
 
    addr  	: in  std_logic_vector (13 downto 0); -- data mask
    ba    	: in  std_logic_vector ( 1 downto 0); -- data mask
    dqin  	: out std_logic_vector (dbits*2-1 downto 0); -- ddr input data
    dqout 	: in  std_logic_vector (dbits*2-1 downto 0); -- ddr input data
    dm    	: in  std_logic_vector (dbits/4-1 downto 0); -- data mask
    oen       	: in  std_ulogic;
    dqs       	: in  std_ulogic;
    dqsoen     	: in  std_ulogic;
    rasn      	: in  std_ulogic;
    casn      	: in  std_ulogic;
    wen       	: in  std_ulogic;
    csn       	: in  std_logic_vector(1 downto 0);
    cke       	: in  std_logic_vector(1 downto 0));
end component;

component ddr2phy 
  generic (tech : integer := virtex5; MHz : integer := 100; 
	rstdelay : integer := 200; dbits : integer := 16; 
	clk_mul : integer := 2; clk_div : integer := 2;
	ddelayb0 : integer := 0; ddelayb1 : integer := 0; ddelayb2 : integer := 0;
	ddelayb3 : integer := 0; ddelayb4 : integer := 0; ddelayb5 : integer := 0;
	ddelayb6 : integer := 0; ddelayb7 : integer := 0;
   numidelctrl : integer := 4; norefclk : integer := 0);
  port (
    rst       : in  std_ulogic;
    clk       : in  std_logic;          	-- input clock
    clkref200 : in  std_logic;			-- input 200MHz clock
    clkout    : out std_ulogic;			-- system clock
    lock      : out std_ulogic;			-- DCM locked

    ddr_clk 	: out std_logic_vector(2 downto 0);
    ddr_clkb	: out std_logic_vector(2 downto 0);
    ddr_cke  	: out std_logic_vector(1 downto 0);
    ddr_csb  	: out std_logic_vector(1 downto 0);
    ddr_web  	: out std_ulogic;                       -- ddr write enable
    ddr_rasb  	: out std_ulogic;                       -- ddr ras
    ddr_casb  	: out std_ulogic;                       -- ddr cas
    ddr_dm   	: out std_logic_vector (dbits/8-1 downto 0);    -- ddr dm
    ddr_dqs  	: inout std_logic_vector (dbits/8-1 downto 0);    -- ddr dqs
    ddr_dqsn  	: inout std_logic_vector (dbits/8-1 downto 0);    -- ddr dqsn
    ddr_ad      : out std_logic_vector (13 downto 0);   -- ddr address
    ddr_ba      : out std_logic_vector (1 downto 0);    -- ddr bank address
    ddr_dq    	: inout  std_logic_vector (dbits-1 downto 0); -- ddr data
    ddr_odt     : out std_logic_vector(1 downto 0);

    addr  	: in  std_logic_vector (13 downto 0); -- data mask
    ba    	: in  std_logic_vector ( 1 downto 0); -- data mask
    dqin  	: out std_logic_vector (dbits*2-1 downto 0); -- ddr input data
    dqout 	: in  std_logic_vector (dbits*2-1 downto 0); -- ddr input data
    dm    	: in  std_logic_vector (dbits/4-1 downto 0); -- data mask
    oen       	: in  std_ulogic;
    dqs       	: in  std_ulogic;
    dqsoen     	: in  std_ulogic;
    rasn      	: in  std_ulogic;
    casn      	: in  std_ulogic;
    wen       	: in  std_ulogic;
    csn       	: in  std_logic_vector(1 downto 0);
    cke       	: in  std_logic_vector(1 downto 0);
    cal_en	: in  std_logic_vector(dbits/8-1 downto 0);
    cal_inc	: in  std_logic_vector(dbits/8-1 downto 0);
    cal_rst	: in  std_logic);
end component;

---------------------------------------------------------------------------
--  61x61 Multiplier
---------------------------------------------------------------------------  

component mul_61x61
  generic (multech : integer := 0);
    port(A       : in std_logic_vector(60 downto 0);  
         B       : in std_logic_vector(60 downto 0);
         EN      : in std_logic;         
         CLK     : in std_logic;
         PRODUCT : out std_logic_vector(121 downto 0));
end component;

---------------------------------------------------------------------------
-- netlists ---------------------------------------------------------------
---------------------------------------------------------------------------

component usbhc_net is
  generic (
    tech        : integer := 0;
    nports      : integer range 1 to 15 := 1;
    ehcgen      : integer range 0 to 1 := 1;
    uhcgen      : integer range 0 to 1 := 1;
    n_cc        : integer range 1 to 15 := 1;
    n_pcc       : integer range 1 to 15 := 1;
    prr         : integer range 0 to 1 := 0;
    portroute1  : integer := 0;
    portroute2  : integer := 0;
    endian_conv : integer range 0 to 1 := 1;
    be_regs     : integer range 0 to 1 := 0;
    be_desc     : integer range 0 to 1 := 0;
    uhcblo      : integer range 0 to 255 := 2;
    bwrd        : integer range 1 to 256 := 16;
    utm_type    : integer range 0 to 2 := 2;
    vbusconf    : integer range 0 to 3 := 3;
    ramtest     : integer range 0 to 1 := 0
    );
  port (
    clk   : in std_ulogic;
    uclk  : in std_ulogic;
    rst   : in std_ulogic;
    ursti : in std_ulogic;
    -- EHC apb_slv_in_type unwrapped
    ehc_apbsi_psel    : in std_ulogic;
    ehc_apbsi_penable : in std_ulogic;
    ehc_apbsi_paddr   : in std_logic_vector(31 downto 0);
    ehc_apbsi_pwrite  : in std_ulogic;
    ehc_apbsi_pwdata  : in std_logic_vector(31 downto 0);
    ehc_apbsi_testen  : in std_ulogic;
    ehc_apbsi_testrst : in std_ulogic;
    ehc_apbsi_scanen  : in std_ulogic;
    -- EHC apb_slv_out_type unwrapped
    ehc_apbso_prdata : out std_logic_vector(31 downto 0);
    ehc_apbso_pirq   : out std_ulogic;
    -- EHC/UHC ahb_mst_in_type unwrapped
    ahbmi_hgrant  : in std_logic_vector(n_cc*uhcgen downto 0);
    ahbmi_hready  : in std_ulogic;
    ahbmi_hresp   : in std_logic_vector(1 downto 0);
    ahbmi_hrdata  : in std_logic_vector(31 downto 0);
    ahbmi_hcache  : in std_ulogic;
    ahbmi_testen  : in std_ulogic;
    ahbmi_testrst : in std_ulogic;
    ahbmi_scanen  : in std_ulogic;
    -- UHC ahb_slv_in_type unwrapped
    uhc_ahbsi_hsel    : in std_logic_vector(n_cc*uhcgen downto 1*uhcgen);
    uhc_ahbsi_haddr   : in std_logic_vector(31 downto 0);
    uhc_ahbsi_hwrite  : in std_ulogic;
    uhc_ahbsi_htrans  : in std_logic_vector(1 downto 0);
    uhc_ahbsi_hsize   : in std_logic_vector(2 downto 0);
    uhc_ahbsi_hwdata  : in std_logic_vector(31 downto 0);
    uhc_ahbsi_hready  : in std_ulogic;
    uhc_ahbsi_testen  : in std_ulogic;
    uhc_ahbsi_testrst : in std_ulogic;
    uhc_ahbsi_scanen  : in std_ulogic;
    -- EHC ahb_mst_out_type_unwrapped 
    ehc_ahbmo_hbusreq : out std_ulogic;
    ehc_ahbmo_hlock   : out std_ulogic;
    ehc_ahbmo_htrans  : out std_logic_vector(1 downto 0);
    ehc_ahbmo_haddr   : out std_logic_vector(31 downto 0);
    ehc_ahbmo_hwrite  : out std_ulogic;
    ehc_ahbmo_hsize   : out std_logic_vector(2 downto 0);
    ehc_ahbmo_hburst  : out std_logic_vector(2 downto 0);
    ehc_ahbmo_hprot   : out std_logic_vector(3 downto 0);
    ehc_ahbmo_hwdata  : out std_logic_vector(31 downto 0);
    -- UHC ahb_mst_out_vector_type unwrapped
    uhc_ahbmo_hbusreq : out std_logic_vector(n_cc*uhcgen downto 1*uhcgen);
    uhc_ahbmo_hlock   : out std_logic_vector(n_cc*uhcgen downto 1*uhcgen);
    uhc_ahbmo_htrans  : out std_logic_vector((n_cc*2)*uhcgen downto 1*uhcgen);
    uhc_ahbmo_haddr   : out std_logic_vector((n_cc*32)*uhcgen downto 1*uhcgen);
    uhc_ahbmo_hwrite  : out std_logic_vector(n_cc*uhcgen downto 1*uhcgen);
    uhc_ahbmo_hsize   : out std_logic_vector((n_cc*3)*uhcgen downto 1*uhcgen);
    uhc_ahbmo_hburst  : out std_logic_vector((n_cc*3)*uhcgen downto 1*uhcgen);
    uhc_ahbmo_hprot   : out std_logic_vector((n_cc*4)*uhcgen downto 1*uhcgen);
    uhc_ahbmo_hwdata  : out std_logic_vector((n_cc*32)*uhcgen downto 1*uhcgen);
    -- UHC ahb_slv_out_vector_type unwrapped
    uhc_ahbso_hready : out std_logic_vector(n_cc*uhcgen downto 1*uhcgen);
    uhc_ahbso_hresp  : out std_logic_vector((n_cc*2)*uhcgen downto 1*uhcgen);
    uhc_ahbso_hrdata : out std_logic_vector((n_cc*32)*uhcgen downto 1*uhcgen);
    uhc_ahbso_hsplit : out std_logic_vector((n_cc*16)*uhcgen downto 1*uhcgen);
    uhc_ahbso_hcache : out std_logic_vector(n_cc*uhcgen downto 1*uhcgen);
    uhc_ahbso_hirq   : out std_logic_vector(n_cc*uhcgen downto 1*uhcgen);
    -- usbhc_out_type_vector unwrapped
    xcvrsel  : out std_logic_vector(((nports*2)-1) downto 0);
    termsel  : out std_logic_vector((nports-1) downto 0);
    suspendm : out std_logic_vector((nports-1) downto 0);
    opmode   : out std_logic_vector(((nports*2)-1) downto 0);
    txvalid  : out std_logic_vector((nports-1) downto 0);
    drvvbus  : out std_logic_vector((nports-1) downto 0);
    dataho   : out std_logic_vector(((nports*8)-1) downto 0); 
    validho  : out std_logic_vector((nports-1) downto 0);
    host     : out std_logic_vector((nports-1) downto 0);
    stp      : out std_logic_vector((nports-1) downto 0);
    datao    : out std_logic_vector(((nports*8)-1) downto 0);   
    utm_rst  : out std_logic_vector((nports-1) downto 0);
    dctrlo   : out std_logic_vector((nports-1) downto 0);
    -- usbhc_in_type_vector unwrapped
    linestate : in std_logic_vector(((nports*2)-1) downto 0);
    txready   : in std_logic_vector((nports-1) downto 0);
    rxvalid   : in std_logic_vector((nports-1) downto 0);
    rxactive  : in std_logic_vector((nports-1) downto 0);
    rxerror   : in std_logic_vector((nports-1) downto 0);
    vbusvalid : in std_logic_vector((nports-1) downto 0);
    datahi    : in std_logic_vector(((nports*8)-1) downto 0);
    validhi   : in std_logic_vector((nports-1) downto 0);
    hostdisc  : in std_logic_vector((nports-1) downto 0);
    nxt       : in std_logic_vector((nports-1) downto 0);
    dir       : in std_logic_vector((nports-1) downto 0);
    datai     : in std_logic_vector(((nports*8)-1) downto 0);
    -- EHC transaction buffer signals
    mbc20_tb_addr : out std_logic_vector(8 downto 0);
    mbc20_tb_data : out std_logic_vector(31 downto 0);
    mbc20_tb_en   : out std_ulogic;
    mbc20_tb_wel  : out std_ulogic;
    mbc20_tb_weh  : out std_ulogic;
    tb_mbc20_data : in std_logic_vector(31 downto 0);
    pe20_tb_addr  : out std_logic_vector(8 downto 0);
    pe20_tb_data  : out std_logic_vector(31 downto 0);
    pe20_tb_en    : out std_ulogic;
    pe20_tb_wel   : out std_ulogic;
    pe20_tb_weh   : out std_ulogic;
    tb_pe20_data  : in std_logic_vector(31 downto 0);
    -- EHC packet buffer signals
    mbc20_pb_addr : out std_logic_vector(8 downto 0);
    mbc20_pb_data : out std_logic_vector(31 downto 0);
    mbc20_pb_en   : out std_ulogic;
    mbc20_pb_we   : out std_ulogic;
    pb_mbc20_data : in std_logic_vector(31 downto 0);
    sie20_pb_addr : out std_logic_vector(8 downto 0);
    sie20_pb_data : out std_logic_vector(31 downto 0);
    sie20_pb_en   : out std_ulogic;
    sie20_pb_we   : out std_ulogic;
    pb_sie20_data : in std_logic_vector(31 downto 0);
    -- UHC packet buffer signals
    sie11_pb_addr : out std_logic_vector((n_cc*9)*uhcgen downto 1*uhcgen);
    sie11_pb_data : out std_logic_vector((n_cc*32)*uhcgen downto 1*uhcgen);
    sie11_pb_en   : out std_logic_vector(n_cc*uhcgen downto 1*uhcgen);
    sie11_pb_we   : out std_logic_vector(n_cc*uhcgen downto 1*uhcgen);
    pb_sie11_data : in std_logic_vector((n_cc*32)*uhcgen downto 1*uhcgen);
    mbc11_pb_addr : out std_logic_vector((n_cc*9)*uhcgen downto 1*uhcgen);
    mbc11_pb_data : out std_logic_vector((n_cc*32)*uhcgen downto 1*uhcgen);
    mbc11_pb_en   : out std_logic_vector(n_cc*uhcgen downto 1*uhcgen);
    mbc11_pb_we   : out std_logic_vector(n_cc*uhcgen downto 1*uhcgen);
    pb_mbc11_data : in std_logic_vector((n_cc*32)*uhcgen downto 1*uhcgen);
    bufsel        : out std_ulogic);
  end component;

component grspwc_net 
  generic(
    tech         : integer := 0;
    sysfreq      : integer := 40000;
    usegen       : integer range 0 to 1  := 1;
    nsync        : integer range 1 to 2  := 1; 
    rmap         : integer range 0 to 1  := 0;
    rmapcrc      : integer range 0 to 1  := 0;
    fifosize1    : integer range 4 to 32 := 32;
    fifosize2    : integer range 16 to 64 := 64;
    rxunaligned  : integer range 0 to 1 := 0;
    rmapbufs     : integer range 2 to 8 := 4;
    scantest     : integer range 0 to 1 := 0
  );
  port(
    rst          : in  std_ulogic;
    clk          : in  std_ulogic;
    txclk        : in  std_ulogic;
    --ahb mst in
    hgrant       : in  std_ulogic;
    hready       : in  std_ulogic;   
    hresp        : in  std_logic_vector(1 downto 0);
    hrdata       : in  std_logic_vector(31 downto 0); 
    --ahb mst out
    hbusreq      : out  std_ulogic;        
    hlock        : out  std_ulogic;
    htrans       : out  std_logic_vector(1 downto 0);
    haddr        : out  std_logic_vector(31 downto 0);
    hwrite       : out  std_ulogic;
    hsize        : out  std_logic_vector(2 downto 0);
    hburst       : out  std_logic_vector(2 downto 0);
    hprot        : out  std_logic_vector(3 downto 0);
    hwdata       : out  std_logic_vector(31 downto 0);
    --apb slv in 
    psel	 : in   std_ulogic;
    penable	 : in   std_ulogic;
    paddr	 : in   std_logic_vector(31 downto 0);
    pwrite	 : in   std_ulogic;
    pwdata	 : in   std_logic_vector(31 downto 0);
    --apb slv out
    prdata	 : out  std_logic_vector(31 downto 0);
    --spw in
    di 		 : in std_logic_vector(1 downto 0);
    si 		 : in std_logic_vector(1 downto 0);
    --spw out
    do 		 : out std_logic_vector(1 downto 0);
    so 		 : out std_logic_vector(1 downto 0);
    --time iface
    tickin       : in   std_ulogic;
    tickout      : out  std_ulogic;
    --irq
    irq          : out  std_logic;
    --misc     
    clkdiv10     : in   std_logic_vector(7 downto 0);
    dcrstval     : in   std_logic_vector(9 downto 0);
    timerrstval  : in   std_logic_vector(11 downto 0);
    --rmapen
    rmapen       : in   std_ulogic;
    --clk bufs
    rxclki       : in std_logic_vector(1 downto 0);
    nrxclki      : in std_logic_vector(1 downto 0);
    rxclko       : out std_logic_vector(1 downto 0);
    --rx ahb fifo
    rxrenable    : out  std_ulogic;
    rxraddress   : out  std_logic_vector(4 downto 0);
    rxwrite      : out  std_ulogic;
    rxwdata      : out  std_logic_vector(31 downto 0);
    rxwaddress   : out  std_logic_vector(4 downto 0);
    rxrdata      : in   std_logic_vector(31 downto 0);    
    --tx ahb fifo
    txrenable    : out  std_ulogic;
    txraddress   : out  std_logic_vector(4 downto 0);
    txwrite      : out  std_ulogic;
    txwdata      : out  std_logic_vector(31 downto 0);
    txwaddress   : out  std_logic_vector(4 downto 0);
    txrdata      : in   std_logic_vector(31 downto 0);    
    --nchar fifo
    ncrenable    : out  std_ulogic;
    ncraddress   : out  std_logic_vector(5 downto 0);
    ncwrite      : out  std_ulogic;
    ncwdata      : out  std_logic_vector(8 downto 0);
    ncwaddress   : out  std_logic_vector(5 downto 0);
    ncrdata      : in   std_logic_vector(8 downto 0);
    --rmap buf
    rmrenable    : out  std_ulogic;
    rmraddress   : out  std_logic_vector(7 downto 0);
    rmwrite      : out  std_ulogic;
    rmwdata      : out  std_logic_vector(7 downto 0);
    rmwaddress   : out  std_logic_vector(7 downto 0);
    rmrdata      : in   std_logic_vector(7 downto 0);
    linkdis      : out  std_ulogic;
    testclk      : in   std_ulogic := '0';
    testrst      : in   std_ulogic := '0';
    testen       : in   std_ulogic := '0'
  );
end component;

  component grlfpw_net 
  generic (tech     : integer := 0;
           pclow    : integer range 0 to 2 := 2;
           dsu      : integer range 0 to 1 := 1;           
           disas    : integer range 0 to 2 := 0;
           pipe     : integer range 0 to 2 := 0
           );
  port (
    rst    : in  std_ulogic;			-- Reset
    clk    : in  std_ulogic;
    holdn  : in  std_ulogic;			-- pipeline hold
    cpi_flush  	: in std_ulogic;			  -- pipeline flush
    cpi_exack    	: in std_ulogic;			  -- FP exception acknowledge
    cpi_a_rs1  	: in std_logic_vector(4 downto 0);
    cpi_d_pc    : in std_logic_vector(31 downto 0);
    cpi_d_inst  : in std_logic_vector(31 downto 0);
    cpi_d_cnt   : in std_logic_vector(1 downto 0);
    cpi_d_trap  : in std_ulogic;
    cpi_d_annul : in std_ulogic;
    cpi_d_pv    : in std_ulogic;
    cpi_a_pc    : in std_logic_vector(31 downto 0);
    cpi_a_inst  : in std_logic_vector(31 downto 0);
    cpi_a_cnt   : in std_logic_vector(1 downto 0);
    cpi_a_trap  : in std_ulogic;
    cpi_a_annul : in std_ulogic;
    cpi_a_pv    : in std_ulogic;
    cpi_e_pc    : in std_logic_vector(31 downto 0);
    cpi_e_inst  : in std_logic_vector(31 downto 0);
    cpi_e_cnt   : in std_logic_vector(1 downto 0);
    cpi_e_trap  : in std_ulogic;
    cpi_e_annul : in std_ulogic;
    cpi_e_pv    : in std_ulogic;
    cpi_m_pc    : in std_logic_vector(31 downto 0);
    cpi_m_inst  : in std_logic_vector(31 downto 0);
    cpi_m_cnt   : in std_logic_vector(1 downto 0);
    cpi_m_trap  : in std_ulogic;
    cpi_m_annul : in std_ulogic;
    cpi_m_pv    : in std_ulogic;
    cpi_x_pc    : in std_logic_vector(31 downto 0);
    cpi_x_inst  : in std_logic_vector(31 downto 0);
    cpi_x_cnt   : in std_logic_vector(1 downto 0);
    cpi_x_trap  : in std_ulogic;
    cpi_x_annul : in std_ulogic;
    cpi_x_pv    : in std_ulogic;    
    cpi_lddata        : in std_logic_vector(31 downto 0);     -- load data
    cpi_dbg_enable : in std_ulogic;
    cpi_dbg_write  : in std_ulogic;
    cpi_dbg_fsr    : in std_ulogic;                            -- FSR access
    cpi_dbg_addr   : in std_logic_vector(4 downto 0);
    cpi_dbg_data   : in std_logic_vector(31 downto 0);
    cpo_data          : out std_logic_vector(31 downto 0); -- store data
    cpo_exc  	        : out std_logic;			 -- FP exception
    cpo_cc           : out std_logic_vector(1 downto 0);  -- FP condition codes
    cpo_ccv  	       : out std_ulogic;			 -- FP condition codes valid
    cpo_ldlock       : out std_logic;			 -- FP pipeline hold
    cpo_holdn         : out std_ulogic;
    cpo_dbg_data     : out std_logic_vector(31 downto 0);

    rfi1_rd1addr 	: out std_logic_vector(3 downto 0); 
    rfi1_rd2addr 	: out std_logic_vector(3 downto 0); 
    rfi1_wraddr 	: out std_logic_vector(3 downto 0); 
    rfi1_wrdata 	: out std_logic_vector(31 downto 0);
    rfi1_ren1        : out std_ulogic;			   
    rfi1_ren2        : out std_ulogic;			   
    rfi1_wren        : out std_ulogic;			   
    
    rfi2_rd1addr 	: out std_logic_vector(3 downto 0); 
    rfi2_rd2addr 	: out std_logic_vector(3 downto 0); 
    rfi2_wraddr 	: out std_logic_vector(3 downto 0); 
    rfi2_wrdata 	: out std_logic_vector(31 downto 0);
    rfi2_ren1        : out std_ulogic;
    rfi2_ren2        : out std_ulogic;			    
    rfi2_wren        : out std_ulogic;

    rfo1_data1    	: in std_logic_vector(31 downto 0);
    rfo1_data2    	: in std_logic_vector(31 downto 0);
    rfo2_data1    	: in std_logic_vector(31 downto 0);
    rfo2_data2    	: in std_logic_vector(31 downto 0)        
    );
  end component;

  component grfpw_net 
  generic (tech     : integer := 0;
           pclow    : integer range 0 to 2 := 2;
           dsu      : integer range 0 to 2 := 1;           
           disas    : integer range 0 to 2 := 0;
           pipe     : integer range 0 to 2 := 0
           );
  port (
    rst    : in  std_ulogic;			-- Reset
    clk    : in  std_ulogic;
    holdn  : in  std_ulogic;			-- pipeline hold
    cpi_flush  	: in std_ulogic;			  -- pipeline flush
    cpi_exack    	: in std_ulogic;			  -- FP exception acknowledge
    cpi_a_rs1  	: in std_logic_vector(4 downto 0);
    cpi_d_pc    : in std_logic_vector(31 downto 0);
    cpi_d_inst  : in std_logic_vector(31 downto 0);
    cpi_d_cnt   : in std_logic_vector(1 downto 0);
    cpi_d_trap  : in std_ulogic;
    cpi_d_annul : in std_ulogic;
    cpi_d_pv    : in std_ulogic;
    cpi_a_pc    : in std_logic_vector(31 downto 0);
    cpi_a_inst  : in std_logic_vector(31 downto 0);
    cpi_a_cnt   : in std_logic_vector(1 downto 0);
    cpi_a_trap  : in std_ulogic;
    cpi_a_annul : in std_ulogic;
    cpi_a_pv    : in std_ulogic;
    cpi_e_pc    : in std_logic_vector(31 downto 0);
    cpi_e_inst  : in std_logic_vector(31 downto 0);
    cpi_e_cnt   : in std_logic_vector(1 downto 0);
    cpi_e_trap  : in std_ulogic;
    cpi_e_annul : in std_ulogic;
    cpi_e_pv    : in std_ulogic;
    cpi_m_pc    : in std_logic_vector(31 downto 0);
    cpi_m_inst  : in std_logic_vector(31 downto 0);
    cpi_m_cnt   : in std_logic_vector(1 downto 0);
    cpi_m_trap  : in std_ulogic;
    cpi_m_annul : in std_ulogic;
    cpi_m_pv    : in std_ulogic;
    cpi_x_pc    : in std_logic_vector(31 downto 0);
    cpi_x_inst  : in std_logic_vector(31 downto 0);
    cpi_x_cnt   : in std_logic_vector(1 downto 0);
    cpi_x_trap  : in std_ulogic;
    cpi_x_annul : in std_ulogic;
    cpi_x_pv    : in std_ulogic;    
    cpi_lddata        : in std_logic_vector(31 downto 0);     -- load data
    cpi_dbg_enable : in std_ulogic;
    cpi_dbg_write  : in std_ulogic;
    cpi_dbg_fsr    : in std_ulogic;                            -- FSR access
    cpi_dbg_addr   : in std_logic_vector(4 downto 0);
    cpi_dbg_data   : in std_logic_vector(31 downto 0);
    cpo_data          : out std_logic_vector(31 downto 0); -- store data
    cpo_exc  	        : out std_logic;			 -- FP exception
    cpo_cc           : out std_logic_vector(1 downto 0);  -- FP condition codes
    cpo_ccv  	       : out std_ulogic;			 -- FP condition codes valid
    cpo_ldlock       : out std_logic;			 -- FP pipeline hold
    cpo_holdn         : out std_ulogic;
    cpo_dbg_data     : out std_logic_vector(31 downto 0);

    rfi1_rd1addr 	: out std_logic_vector(3 downto 0); 
    rfi1_rd2addr 	: out std_logic_vector(3 downto 0); 
    rfi1_wraddr 	: out std_logic_vector(3 downto 0); 
    rfi1_wrdata 	: out std_logic_vector(31 downto 0);
    rfi1_ren1        : out std_ulogic;			   
    rfi1_ren2        : out std_ulogic;			   
    rfi1_wren        : out std_ulogic;			   
    
    rfi2_rd1addr 	: out std_logic_vector(3 downto 0); 
    rfi2_rd2addr 	: out std_logic_vector(3 downto 0); 
    rfi2_wraddr 	: out std_logic_vector(3 downto 0); 
    rfi2_wrdata 	: out std_logic_vector(31 downto 0);
    rfi2_ren1        : out std_ulogic;
    rfi2_ren2        : out std_ulogic;			    
    rfi2_wren        : out std_ulogic;

    rfo1_data1    	: in std_logic_vector(31 downto 0);
    rfo1_data2    	: in std_logic_vector(31 downto 0);
    rfo2_data1    	: in std_logic_vector(31 downto 0);
    rfo2_data2    	: in std_logic_vector(31 downto 0)        
    );
  end component;

  component leon3ft_net
  generic (
    hindex    : integer               := 0;
    fabtech   : integer range 0 to NTECH  := DEFFABTECH;
    memtech   : integer range 0 to NTECH  := DEFMEMTECH;
    nwindows  : integer range 2 to 32 := 8;
    dsu       : integer range 0 to 1  := 0;
    fpu       : integer range 0 to 31 := 0;
    v8        : integer range 0 to 2  := 0;
    cp        : integer range 0 to 1  := 0;
    mac       : integer range 0 to 1  := 0;
    pclow     : integer range 0 to 2  := 2;
    notag     : integer range 0 to 1  := 0;
    nwp       : integer range 0 to 4  := 0;
    icen      : integer range 0 to 1  := 0;
    irepl     : integer range 0 to 2  := 2;
    isets     : integer range 1 to 4  := 1;
    ilinesize : integer range 4 to 8  := 4;
    isetsize  : integer range 1 to 256 := 1;
    isetlock  : integer range 0 to 1  := 0;
    dcen      : integer range 0 to 1  := 0;
    drepl     : integer range 0 to 2  := 2;
    dsets     : integer range 1 to 4  := 1;
    dlinesize : integer range 4 to 8  := 4;
    dsetsize  : integer range 1 to 256 := 1;
    dsetlock  : integer range 0 to 1  := 0;
    dsnoop    : integer range 0 to 6  := 0;
    ilram      : integer range 0 to 1 := 0;
    ilramsize  : integer range 1 to 512 := 1;
    ilramstart : integer range 0 to 255 := 16#8e#;
    dlram      : integer range 0 to 1 := 0;
    dlramsize  : integer range 1 to 512 := 1;
    dlramstart : integer range 0 to 255 := 16#8f#;
    mmuen     : integer range 0 to 1  := 0;
    itlbnum   : integer range 2 to 64 := 8;
    dtlbnum   : integer range 2 to 64 := 8;
    tlb_type  : integer range 0 to 1  := 1;
    tlb_rep   : integer range 0 to 1  := 0;
    lddel     : integer range 1 to 2  := 2;
    disas     : integer range 0 to 1  := 0;
    tbuf      : integer range 0 to 64 := 0;
    pwd       : integer range 0 to 2  := 2;     -- power-down
    svt       : integer range 0 to 1  := 1;     -- single vector trapping
    rstaddr   : integer               := 0;
    smp       : integer range 0 to 15 := 0;    -- support SMP systems
    iuft      : integer range 0 to 4  := 0;
    fpft      : integer range 0 to 4  := 0;
    cmft      : integer range 0 to 1  := 0;
    cached    : integer               := 0;
    scantest  : integer               := 0
  );

   port (
      clk     : in  std_ulogic;
      rstn    : in  std_ulogic;
      ahbi    : in  ahb_mst_in_type;
      ahbo    : out ahb_mst_out_type;
      ahbsi   : in  ahb_slv_in_type;
      ahbso   : in  ahb_slv_out_vector;
      irqi_irl:         in    std_logic_vector(3 downto 0);
      irqi_rst:         in    std_ulogic;
      irqi_run:         in    std_ulogic;

      irqo_intack:      out   std_ulogic;
      irqo_irl:         out   std_logic_vector(3 downto 0);
      irqo_pwd:         out   std_ulogic;

      dbgi_dsuen:       in    std_ulogic;                               -- DSU enable
      dbgi_denable:     in    std_ulogic;                               -- diagnostic register access enable
      dbgi_dbreak:      in    std_ulogic;                               -- debug break-in
      dbgi_step:        in    std_ulogic;                               -- single step
      dbgi_halt:        in    std_ulogic;                               -- halt processor
      dbgi_reset:       in    std_ulogic;                               -- reset processor
      dbgi_dwrite:      in    std_ulogic;                               -- read/write
      dbgi_daddr:       in    std_logic_vector(23 downto 2);            -- diagnostic address
      dbgi_ddata:       in    std_logic_vector(31 downto 0);            -- diagnostic data
      dbgi_btrapa:      in    std_ulogic;                               -- break on IU trap
      dbgi_btrape:      in    std_ulogic;                               -- break on IU trap
      dbgi_berror:      in    std_ulogic;                               -- break on IU error mode
      dbgi_bwatch:      in    std_ulogic;                               -- break on IU watchpoint
      dbgi_bsoft:       in    std_ulogic;                               -- break on software breakpoint (TA 1)
      dbgi_tenable:     in    std_ulogic;
      dbgi_timer:       in    std_logic_vector(30 downto 0);

      dbgo_data:        out   std_logic_vector(31 downto 0);
      dbgo_crdy:        out   std_ulogic;
      dbgo_dsu:         out   std_ulogic;
      dbgo_dsumode:     out   std_ulogic;
      dbgo_error:       out   std_ulogic;
      dbgo_halt:        out   std_ulogic;
      dbgo_pwd:         out   std_ulogic);

  end component;

  component ringosc
   generic (tech : integer := 0);
   port (
      roen  :  in    Std_ULogic;
      roout :  out   Std_ULogic);
  end component;

component ftmctrl_net
   generic (
    hindex    : integer := 0;
    pindex    : integer := 0;
    romaddr   : integer := 16#000#;
    rommask   : integer := 16#E00#;
    ioaddr    : integer := 16#200#;
    iomask    : integer := 16#E00#;
    ramaddr   : integer := 16#400#;
    rammask   : integer := 16#C00#;
    paddr     : integer := 0;
    pmask     : integer := 16#fff#;
    wprot     : integer := 0;
    invclk    : integer := 0;
    fast      : integer := 0;
    romasel   : integer := 28;
    sdrasel   : integer := 29;
    srbanks   : integer := 4;
    ram8      : integer := 0;
    ram16     : integer := 0;
    sden      : integer := 0;
    sepbus    : integer := 0;
    sdbits    : integer := 32;
    sdlsb     : integer := 2;          -- set to 12 for the GE-HPE board
    oepol     : integer := 0;
    edac      : integer := 0;
    syncrst   : integer := 0;
    pageburst : integer := 0;
    scantest  : integer := 0;
    writefb   : integer := 0;
    tech      : integer := 0
  );
   port (
      rst:     in    Std_ULogic;
      clk:     in    Std_ULogic;
      ahbsi:   in    ahb_slv_in_type;
      ahbso:   out   ahb_slv_out_type;
      apbi:    in    apb_slv_in_type;
      apbo:    out   apb_slv_out_type;
      memi_data:        in    Std_Logic_Vector(31 downto 0);
      memi_brdyn:       in    Std_Logic;
      memi_bexcn:       in    Std_Logic;
      memi_writen:      in    Std_Logic;
      memi_wrn:         in    Std_Logic_Vector(3 downto 0);
      memi_bwidth:      in    Std_Logic_Vector(1 downto 0);
      memi_sd:          in    Std_Logic_Vector(63 downto 0);
      memi_cb:          in    Std_Logic_Vector(7 downto 0);
      memi_scb:         in    Std_Logic_Vector(7 downto 0);
      memi_edac:        in    Std_Logic;
      memo_address:     out   Std_Logic_Vector(31 downto 0);
      memo_data:        out   Std_Logic_Vector(31 downto 0);
      memo_sddata:      out   Std_Logic_Vector(63 downto 0);
      memo_ramsn:       out   Std_Logic_Vector(7 downto 0);
      memo_ramoen:      out   Std_Logic_Vector(7 downto 0);
      memo_ramn:        out   Std_ULogic;
      memo_romn:        out   Std_ULogic;
      memo_mben:        out   Std_Logic_Vector(3 downto 0);
      memo_iosn:        out   Std_Logic;
      memo_romsn:       out   Std_Logic_Vector(7 downto 0);
      memo_oen:         out   Std_Logic;
      memo_writen:      out   Std_Logic;
      memo_wrn:         out   Std_Logic_Vector(3 downto 0);
      memo_bdrive:      out   Std_Logic_Vector(3 downto 0);
      memo_vbdrive:     out   Std_Logic_Vector(31 downto 0);
      memo_svbdrive:    out   Std_Logic_Vector(63 downto 0);
      memo_read:        out   Std_Logic;
      memo_sa:          out   Std_Logic_Vector(14 downto 0);
      memo_cb:          out   Std_Logic_Vector(7 downto 0);
      memo_scb:         out   Std_Logic_Vector(7 downto 0);
      memo_vcdrive:     out   Std_Logic_Vector(7 downto 0);
      memo_svcdrive:    out   Std_Logic_Vector(7 downto 0);
      memo_ce:          out   Std_ULogic;
      sdo_sdcke:        out   Std_Logic_Vector( 1 downto 0);
      sdo_sdcsn:        out   Std_Logic_Vector( 1 downto 0);
      sdo_sdwen:        out   Std_ULogic;
      sdo_rasn:         out   Std_ULogic;
      sdo_casn:         out   Std_ULogic;
      sdo_dqm:          out   Std_Logic_Vector( 7 downto 0);
      wpo_wprothit:     in    Std_ULogic);

end component;

end;
