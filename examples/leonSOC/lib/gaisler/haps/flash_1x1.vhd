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
-- Entity:        flash_1x1
-- file:          flash_1x1.vhd
-- Description:   16-bit Flash PROM memory controller for HAPS FLASH_1x1
------------------------------------------------------------------------------

library  ieee;
use      ieee.std_logic_1164.all;

library  grlib;
use      grlib.amba.all;
use      grlib.stdlib.all;

library  techmap;
use      techmap.gencomp.all;

library  gaisler;
use      gaisler.memctrl.all;

entity flash_1x1 is
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
      rst     : in  Std_Logic;
      clk     : in  Std_Logic;
      ahbsi   : in  ahb_slv_in_type;
      ahbso   : out ahb_slv_out_type;
      apbi    : in  apb_slv_in_type;
      apbo    : out apb_slv_out_type;
      sri     : in  memory_in_type;
      sro     : out memory_out_type);
end entity;

architecture wrapper of flash_1x1 is

   component ssrctrl_net is
   generic (
      tech:                   Integer := 0;
      bus16:                  Integer := 1);
   port (
      rst:              in    Std_Logic;
      clk:              in    Std_Logic;
      n_ahbsi_hsel:     in    Std_Logic_Vector(0 to 15);
      n_ahbsi_haddr:    in    Std_Logic_Vector(31 downto 0);
      n_ahbsi_hwrite:   in    Std_Logic;
      n_ahbsi_htrans:   in    Std_Logic_Vector(1 downto 0);
      n_ahbsi_hsize:    in    Std_Logic_Vector(2 downto 0);
      n_ahbsi_hburst:   in    Std_Logic_Vector(2 downto 0);
      n_ahbsi_hwdata:   in    Std_Logic_Vector(31 downto 0);
      n_ahbsi_hprot:    in    Std_Logic_Vector(3 downto 0);
      n_ahbsi_hready:   in    Std_Logic;
      n_ahbsi_hmaster:  in    Std_Logic_Vector(3 downto 0);
      n_ahbsi_hmastlock:in    Std_Logic;
      n_ahbsi_hmbsel:   in    Std_Logic_Vector(0 to 3);
      n_ahbsi_hcache:   in    Std_Logic;
      n_ahbsi_hirq:     in    Std_Logic_Vector(31 downto 0);
      n_ahbso_hready:   out   Std_Logic;
      n_ahbso_hresp:    out   Std_Logic_Vector(1 downto 0);
      n_ahbso_hrdata:   out   Std_Logic_Vector(31 downto 0);
      n_ahbso_hsplit:   out   Std_Logic_Vector(15 downto 0);
      n_ahbso_hcache:   out   Std_Logic;
      n_ahbso_hirq:     out   Std_Logic_Vector(31 downto 0);
      n_apbi_psel:      in    Std_Logic_Vector(0 to 15);
      n_apbi_penable:   in    Std_Logic;
      n_apbi_paddr:     in    Std_Logic_Vector(31 downto 0);
      n_apbi_pwrite:    in    Std_Logic;
      n_apbi_pwdata:    in    Std_Logic_Vector(31 downto 0);
      n_apbi_pirq:      in    Std_Logic_Vector(31 downto 0);
      n_apbo_prdata:    out   Std_Logic_Vector(31 downto 0);
      n_apbo_pirq:      out   Std_Logic_Vector(31 downto 0);
      n_sri_data:       in    Std_Logic_Vector(31 downto 0);
      n_sri_brdyn:      in    Std_Logic;
      n_sri_bexcn:      in    Std_Logic;
      n_sri_writen:     in    Std_Logic;
      n_sri_wrn:        in    Std_Logic_Vector(3 downto 0);
      n_sri_bwidth:     in    Std_Logic_Vector(1 downto 0);
      n_sri_sd:         in    Std_Logic_Vector(63 downto 0);
      n_sri_cb:         in    Std_Logic_Vector(7 downto 0);
      n_sri_scb:        in    Std_Logic_Vector(7 downto 0);
      n_sri_edac:       in    Std_Logic;
      n_sro_address:    out   Std_Logic_Vector(31 downto 0);
      n_sro_data:       out   Std_Logic_Vector(31 downto 0);
      n_sro_sddata:     out   Std_Logic_Vector(63 downto 0);
      n_sro_ramsn:      out   Std_Logic_Vector(7 downto 0);
      n_sro_ramoen:     out   Std_Logic_Vector(7 downto 0);
      n_sro_ramn:       out   Std_Logic;
      n_sro_romn:       out   Std_Logic;
      n_sro_mben:       out   Std_Logic_Vector(3 downto 0);
      n_sro_iosn:       out   Std_Logic;
      n_sro_romsn:      out   Std_Logic_Vector(7 downto 0);
      n_sro_oen:        out   Std_Logic;
      n_sro_writen:     out   Std_Logic;
      n_sro_wrn:        out   Std_Logic_Vector(3 downto 0);
      n_sro_bdrive:     out   Std_Logic_Vector(3 downto 0);
      n_sro_vbdrive:    out   Std_Logic_Vector(31 downto 0);
      n_sro_svbdrive:   out   Std_Logic_Vector(63 downto 0);
      n_sro_read:       out   Std_Logic;
      n_sro_sa:         out   Std_Logic_Vector(14 downto 0);
      n_sro_cb:         out   Std_Logic_Vector(7 downto 0);
      n_sro_scb:        out   Std_Logic_Vector(7 downto 0);
      n_sro_vcdrive:    out   Std_Logic_Vector(7 downto 0);
      n_sro_svcdrive:   out   Std_Logic_Vector(7 downto 0);
      n_sro_ce:         out   Std_Logic);
   end component;

   signal   n_ahbsi_hsel:       Std_Logic_Vector(0 to 15);
   signal   n_ahbsi_haddr:      Std_Logic_Vector(31 downto 0);
   signal   n_ahbsi_hwrite:     Std_Logic;
   signal   n_ahbsi_htrans:     Std_Logic_Vector(1 downto 0);
   signal   n_ahbsi_hsize:      Std_Logic_Vector(2 downto 0);
   signal   n_ahbsi_hburst:     Std_Logic_Vector(2 downto 0);
   signal   n_ahbsi_hwdata:     Std_Logic_Vector(31 downto 0);
   signal   n_ahbsi_hprot:      Std_Logic_Vector(3 downto 0);
   signal   n_ahbsi_hready:     Std_Logic;
   signal   n_ahbsi_hmaster:    Std_Logic_Vector(3 downto 0);
   signal   n_ahbsi_hmastlock:  Std_Logic;
   signal   n_ahbsi_hmbsel:     Std_Logic_Vector(0 to 3);
   signal   n_ahbsi_hcache:     Std_Logic;
   signal   n_ahbsi_hirq:       Std_Logic_Vector(31 downto 0);

   signal   n_ahbso_hready:     Std_Logic;
   signal   n_ahbso_hresp:      Std_Logic_Vector(1 downto 0);
   signal   n_ahbso_hrdata:     Std_Logic_Vector(31 downto 0);
   signal   n_ahbso_hsplit:     Std_Logic_Vector(15 downto 0);
   signal   n_ahbso_hcache:     Std_Logic;
   signal   n_ahbso_hirq:       Std_Logic_Vector(31 downto 0);
--   signal   n_ahbso_hconfig0:   Std_Logic_Vector(31 downto 0);
--   signal   n_ahbso_hconfig1:   Std_Logic_Vector(31 downto 0);
--   signal   n_ahbso_hconfig2:   Std_Logic_Vector(31 downto 0);
--   signal   n_ahbso_hconfig3:   Std_Logic_Vector(31 downto 0);
--   signal   n_ahbso_hconfig4:   Std_Logic_Vector(31 downto 0);
--   signal   n_ahbso_hconfig5:   Std_Logic_Vector(31 downto 0);
--   signal   n_ahbso_hconfig6:   Std_Logic_Vector(31 downto 0);
--   signal   n_ahbso_hconfig7:   Std_Logic_Vector(31 downto 0);
--   signal   n_ahbso_hindex:     Integer range 0 to 15;

   signal   n_apbi_psel:        Std_Logic_Vector(0 to 15);
   signal   n_apbi_penable:     Std_Logic;
   signal   n_apbi_paddr:       Std_Logic_Vector(31 downto 0);
   signal   n_apbi_pwrite:      Std_Logic;
   signal   n_apbi_pwdata:      Std_Logic_Vector(31 downto 0);
   signal   n_apbi_pirq:        Std_Logic_Vector(31 downto 0);

   signal   n_apbo_prdata:      Std_Logic_Vector(31 downto 0);
   signal   n_apbo_pirq:        Std_Logic_Vector(31 downto 0);
--   signal   n_apbo_pconfig0:    Std_Logic_Vector(31 downto 0);
--   signal   n_apbo_pconfig1:    Std_Logic_Vector(31 downto 0);

   signal   n_sri_data:         Std_Logic_Vector(31 downto 0);
   signal   n_sri_brdyn:        Std_Logic;
   signal   n_sri_bexcn:        Std_Logic;
   signal   n_sri_writen:       Std_Logic;
   signal   n_sri_wrn:          Std_Logic_Vector(3 downto 0);
   signal   n_sri_bwidth:       Std_Logic_Vector(1 downto 0);
   signal   n_sri_sd:           Std_Logic_Vector(63 downto 0);
   signal   n_sri_cb:           Std_Logic_Vector(7 downto 0);
   signal   n_sri_scb:          Std_Logic_Vector(7 downto 0);
   signal   n_sri_edac:         Std_Logic;

   signal   n_sro_address:      Std_Logic_Vector(31 downto 0);
   signal   n_sro_data:         Std_Logic_Vector(31 downto 0);
   signal   n_sro_sddata:       Std_Logic_Vector(63 downto 0);
   signal   n_sro_ramsn:        Std_Logic_Vector(7 downto 0);
   signal   n_sro_ramoen:       Std_Logic_Vector(7 downto 0);
   signal   n_sro_ramn:         Std_Logic;
   signal   n_sro_romn:         Std_Logic;
   signal   n_sro_mben:         Std_Logic_Vector(3 downto 0);
   signal   n_sro_iosn:         Std_Logic;
   signal   n_sro_romsn:        Std_Logic_Vector(7 downto 0);
   signal   n_sro_oen:          Std_Logic;
   signal   n_sro_writen:       Std_Logic;
   signal   n_sro_wrn:          Std_Logic_Vector(3 downto 0);
   signal   n_sro_bdrive:       Std_Logic_Vector(3 downto 0);
   signal   n_sro_vbdrive:      Std_Logic_Vector(31 downto 0);
   signal   n_sro_svbdrive:     Std_Logic_Vector(63 downto 0);
   signal   n_sro_read:         Std_Logic;
   signal   n_sro_sa:           Std_Logic_Vector(14 downto 0);
   signal   n_sro_cb:           Std_Logic_Vector(7 downto 0);
   signal   n_sro_scb:          Std_Logic_Vector(7 downto 0);
   signal   n_sro_vcdrive:      Std_Logic_Vector(7 downto 0);
   signal   n_sro_svcdrive:     Std_Logic_Vector(7 downto 0);
   signal   n_sro_ce:           Std_Logic;

   constant VERSION:          amba_version_type := 0;
   constant hconfig:          ahb_config_type := (
     0 => ahb_device_reg(1, 16#00A#, 0, VERSION, 0),
     4 => ahb_membar(romaddr, '1', '1', rommask),
     5 => ahb_membar(ramaddr, '1', '1', rammask),
     6 => ahb_membar(ioaddr,  '0', '0', iomask),
     others => zero32);

   constant pconfig:          apb_config_type := (
     0 => ahb_device_reg (1, 16#00A#, 0, VERSION, 0),
     1 => apb_iobar(paddr, pmask));

begin

   -- source code instantiation
   rtl : if netlist = 0 or tech=0 generate
      wrp: ssrctrl
          generic map (
             hindex        => hindex,
             pindex        => pindex,
             romaddr       => romaddr,
             rommask       => rommask,
             ioaddr        => ioaddr,
             iomask        => iomask,
             ramaddr       => ramaddr,
             rammask       => rammask,
             paddr         => paddr,
             pmask         => pmask,
             oepol         => padoen_polarity(tech),
             bus16         => bus16)
         port map(
            rst            => rst,
            clk            => clk,
            ahbsi          => ahbsi,
            ahbso          => ahbso,
            apbi           => apbi,
            apbo           => apbo,
            sri            => sri,
            sro            => sro);
   end generate;

   -- netlist code instantiation
   struct : if netlist = 1 generate
      -- Plug&Play information
      apbo.pconfig   <= pconfig;
      apbo.pindex    <= pindex;

      ahbso.hconfig  <= hconfig;
      ahbso.hindex   <= hindex;

-- pragma translate_off
     bootmsg : report_version
     generic map ("flash_1x1" & tost(hindex) &
      ": 32-bit SSRAM/PROM controller rev " & tost(VERSION));
-- pragma translate_on

      wrp: ssrctrl_net
      generic map(
         tech              => tech,
         bus16             => bus16)
      port map(
         rst               => rst,
         clk               => clk,
         n_ahbsi_hsel      => n_ahbsi_hsel,
         n_ahbsi_haddr     => n_ahbsi_haddr,
         n_ahbsi_hwrite    => n_ahbsi_hwrite,
         n_ahbsi_htrans    => n_ahbsi_htrans,
         n_ahbsi_hsize     => n_ahbsi_hsize,
         n_ahbsi_hburst    => n_ahbsi_hburst,
         n_ahbsi_hwdata    => n_ahbsi_hwdata,
         n_ahbsi_hprot     => n_ahbsi_hprot,
         n_ahbsi_hready    => n_ahbsi_hready,
         n_ahbsi_hmaster   => n_ahbsi_hmaster,
         n_ahbsi_hmastlock => n_ahbsi_hmastlock,
         n_ahbsi_hmbsel    => n_ahbsi_hmbsel,
         n_ahbsi_hcache    => n_ahbsi_hcache,
         n_ahbsi_hirq      => n_ahbsi_hirq,
         n_ahbso_hready    => n_ahbso_hready,
         n_ahbso_hresp     => n_ahbso_hresp,
         n_ahbso_hrdata    => n_ahbso_hrdata,
         n_ahbso_hsplit    => n_ahbso_hsplit,
         n_ahbso_hcache    => n_ahbso_hcache,
         n_ahbso_hirq      => n_ahbso_hirq,
         n_apbi_psel       => n_apbi_psel,
         n_apbi_penable    => n_apbi_penable,
         n_apbi_paddr      => n_apbi_paddr,
         n_apbi_pwrite     => n_apbi_pwrite,
         n_apbi_pwdata     => n_apbi_pwdata,
         n_apbi_pirq       => n_apbi_pirq,
         n_apbo_prdata     => n_apbo_prdata,
         n_apbo_pirq       => n_apbo_pirq,
         n_sri_data        => n_sri_data,
         n_sri_brdyn       => n_sri_brdyn,
         n_sri_bexcn       => n_sri_bexcn,
         n_sri_writen      => n_sri_writen,
         n_sri_wrn         => n_sri_wrn,
         n_sri_bwidth      => n_sri_bwidth,
         n_sri_sd          => n_sri_sd,
         n_sri_cb          => n_sri_cb,
         n_sri_scb         => n_sri_scb,
         n_sri_edac        => n_sri_edac,
         n_sro_address     => n_sro_address,
         n_sro_data        => n_sro_data,
         n_sro_sddata      => n_sro_sddata,
         n_sro_ramsn       => n_sro_ramsn,
         n_sro_ramoen      => n_sro_ramoen,
         n_sro_ramn        => n_sro_ramn,
         n_sro_romn        => n_sro_romn,
         n_sro_mben        => n_sro_mben,
         n_sro_iosn        => n_sro_iosn,
         n_sro_romsn       => n_sro_romsn,
         n_sro_oen         => n_sro_oen,
         n_sro_writen      => n_sro_writen,
         n_sro_wrn         => n_sro_wrn,
         n_sro_bdrive      => n_sro_bdrive,
         n_sro_vbdrive     => n_sro_vbdrive,
         n_sro_svbdrive    => n_sro_svbdrive,
         n_sro_read        => n_sro_read,
         n_sro_sa          => n_sro_sa,
         n_sro_cb          => n_sro_cb,
         n_sro_scb         => n_sro_scb,
         n_sro_vcdrive     => n_sro_vcdrive,
         n_sro_svcdrive    => n_sro_svcdrive,
         n_sro_ce          => n_sro_ce);

         -- map hindex always on hsel zero
         n_ahbsi_hsel(0)       <= ahbsi.hsel(hindex);
         n_ahbsi_hsel(1 to 15) <= (others => '0');

         n_ahbsi_haddr     <= ahbsi.haddr;
         n_ahbsi_hwrite    <= ahbsi.hwrite;
         n_ahbsi_htrans    <= ahbsi.htrans;
         n_ahbsi_hsize     <= ahbsi.hsize;
         n_ahbsi_hburst    <= ahbsi.hburst;
         n_ahbsi_hwdata    <= ahbsi.hwdata;
         n_ahbsi_hprot     <= ahbsi.hprot;
         n_ahbsi_hready    <= ahbsi.hready;
         n_ahbsi_hmaster   <= ahbsi.hmaster;
         n_ahbsi_hmastlock <= ahbsi.hmastlock;
         n_ahbsi_hmbsel    <= ahbsi.hmbsel;
         n_ahbsi_hcache    <= ahbsi.hcache;
         n_ahbsi_hirq      <= ahbsi.hirq;

         ahbso.hready      <= n_ahbso_hready;
         ahbso.hresp       <= n_ahbso_hresp;
         ahbso.hrdata      <= n_ahbso_hrdata;
         ahbso.hsplit      <= n_ahbso_hsplit;
         ahbso.hcache      <= n_ahbso_hcache;
         ahbso.hirq        <= n_ahbso_hirq;

         -- map pindex always on psel zero
         n_apbi_psel(0)       <= apbi.psel(pindex);
         n_apbi_psel(1 to 15) <= (others => '0');

         n_apbi_penable    <= apbi.penable;
         n_apbi_paddr      <= apbi.paddr;
         n_apbi_pwrite     <= apbi.pwrite;
         n_apbi_pwdata     <= apbi.pwdata;
         n_apbi_pirq       <= apbi.pirq;

         apbo.prdata       <= n_apbo_prdata;
         apbo.pirq         <= n_apbo_pirq;

         n_sri_data        <= sri.data;
         n_sri_brdyn       <= sri.brdyn;
         n_sri_bexcn       <= sri.bexcn;
         n_sri_writen      <= sri.writen;
         n_sri_wrn         <= sri.wrn;
         n_sri_bwidth      <= sri.bwidth;
         n_sri_sd          <= sri.sd;
         n_sri_cb          <= sri.cb;
         n_sri_scb         <= sri.scb;
         n_sri_edac        <= sri.edac;

         sro.address       <= n_sro_address;
         sro.data          <= n_sro_data;
         sro.sddata        <= n_sro_sddata;
         sro.ramsn         <= n_sro_ramsn;
         sro.ramoen        <= n_sro_ramoen;
         sro.ramn          <= n_sro_ramn;
         sro.romn          <= n_sro_romn;
         sro.mben          <= n_sro_mben;
         sro.iosn          <= n_sro_iosn;
         sro.romsn         <= n_sro_romsn;
         sro.oen           <= n_sro_oen;
         sro.writen        <= n_sro_writen;
         sro.wrn           <= n_sro_wrn;
         sro.bdrive        <= n_sro_bdrive;
         sro.vbdrive       <= n_sro_vbdrive;
         sro.svbdrive      <= n_sro_svbdrive;
         sro.read          <= n_sro_read;
         sro.sa            <= n_sro_sa;
         sro.cb            <= n_sro_cb;
         sro.scb           <= n_sro_scb;
         sro.vcdrive       <= n_sro_vcdrive;
         sro.svcdrive      <= n_sro_svcdrive;
         sro.ce            <= n_sro_ce;
   end generate;

end architecture wrapper;