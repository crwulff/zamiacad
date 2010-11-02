----------------------------------------------------------------------------
--  This file is a part of the GRLIB VHDL IP LIBRARY
--  Copyright (C) 2004 GAISLER RESEARCH
--
--  This program is free software; you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation; either version 2 of the License, or
--  (at your option) any later version.
--
--  See the file COPYING for the full details of the license.
--
----------------------------------------------------------------------------
-- Package:     ocrcomp
-- File:    ocrcomp.vhd
-- Description: Declaration of Opencores ethernet MAC and CAN cores
-- Author:      Jiri Gaisler - Gaisler Research
------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;

library grlib;
use grlib.amba.all;

package ocrcomp is

  component ac97_top
    port(
      clk_i               : in  std_logic;
      rst_i               : in  std_logic;
      wb_data_i           : in  std_logic_vector(31 downto 0);
      wb_data_o           : out std_logic_vector(31 downto 0);
      wb_addr_i           : in  std_logic_vector(31 downto 0);
      wb_sel_i            : in  std_logic_vector(3 downto 0);
      wb_we_i             : in  std_logic;
      wb_cyc_i            : in  std_logic;
      wb_stb_i            : in  std_logic;
      wb_ack_o            : out std_logic;
      wb_err_o            : out std_logic;
      int_o               : out std_logic;
      dma_req_o           : out std_logic_vector(8 downto 0);
      dma_ack_i           : in  std_logic_vector(8 downto 0);
      suspended_o         : out std_logic;
      bit_clk_pad_i       : in  std_logic;
      sync_pad_o          : out std_logic;
      sdata_pad_o         : out std_logic;
      sdata_pad_i         : in  std_logic;
      ac97_resetn_pad_o   : out std_logic
      );
  end component;

end;
