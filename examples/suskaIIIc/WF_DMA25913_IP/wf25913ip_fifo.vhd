----------------------------------------------------------------------
----                                                              ----
---- ATARI DMA compatible IP Core					              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- ATARI ST and STE compatible DMA controller IP core.          ----
----                                                              ----
---- The DMA FIFO consists of three parts: this file contains the ----
---- iterative instantiation of the FIFO on the base of the FIFO  ----
---- units (the second part). Thirdly the FIFO requires a control ----
---- mechanism which can be found in the FIFO control file.       ----
----                                                              ----
----                                                              ----
---- To Do:                                                       ----
---- -                                                            ----
----                                                              ----
---- Author(s):                                                   ----
---- - Wolfgang Foerster, wf@experiment-s.de; wf@inventronik.de   ----
----                                                              ----
----------------------------------------------------------------------
----                                                              ----
---- Copyright (C) 2006 - 2008 Wolfgang Foerster                  ----
----                                                              ----
---- This source file may be used and distributed without         ----
---- restriction provided that this copyright statement is not    ----
---- removed from the file and that any derivative work contains  ----
---- the original copyright notice and the associated disclaimer. ----
----                                                              ----
---- This source file is free software; you can redistribute it   ----
---- and/or modify it under the terms of the GNU Lesser General   ----
---- Public License as published by the Free Software Foundation; ----
---- either version 2.1 of the License, or (at your option) any   ----
---- later version.                                               ----
----                                                              ----
---- This source is distributed in the hope that it will be       ----
---- useful, but WITHOUT ANY WARRANTY; without even the implied   ----
---- warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR      ----
---- PURPOSE. See the GNU Lesser General Public License for more  ----
---- details.                                                     ----
----                                                              ----
---- You should have received a copy of the GNU Lesser General    ----
---- Public License along with this source; if not, download it   ----
---- from http://www.gnu.org/licenses/lgpl.html                   ----
----                                                              ----
----------------------------------------------------------------------
-- 
-- Revision History
-- 
-- Revision 2K6A  2006/06/03 WF
--   Initial Release.
-- Revision 2K6B  2006/11/06 WF
--   Modified Source to compile with the Xilinx ISE.
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- 

library work;
use work.WF25913IP_PKG.all;

library ieee;
use ieee.std_logic_1164.all;

entity WF25913IP_FIFO is
	port(
		CLK			: in bit;
		-- Left side (input):
		CLRn 		: in bit;
		WR_ENA 		: in bit;
		DATA_IN 	: in bit_vector(15 downto 0);
		-- Right side (output):
		DATA_OUT 	: out bit_vector(15 downto 0);
		RD_ENA 		: in bit;
		-- FIFO status:
		FIFO_FULL	: out bit;
		FIFO_HI		: out bit;
		FIFO_LOW	: out bit;
		FIFO_EMPTY	: out bit;
		ERR			: out bit
	);
end entity WF25913IP_FIFO;

architecture STRUCTURE of WF25913IP_FIFO is
type DATA_ARRAY is array (16 downto 0) of bit_vector(15 downto 0);
signal DATA		: DATA_ARRAY;
signal SEL_CELL	: bit_vector(16 downto 1);
signal ENA_CELL	: bit_vector(16 downto 1);
begin
	FIFO: for CELL in 16 downto 1 generate 
	begin
		I_DMA_FIFO_UNIT: WF25913IP_FIFO_UNIT
			port map(
				CLK => CLK,
				CLRn => CLRn,
				IN_A => DATA_IN,
				IN_B => DATA(CELL),
				D_OUT => DATA(CELL-1),
				SEL => SEL_CELL(CELL),
				ENA => ENA_CELL(CELL)
			);
	end generate FIFO;

	DATA(16) <= DATA_IN;

	I_DATA_OUT: WF25913IP_FIFO_UNIT
	-- The data output of theFIFO is registered.
		port map(
			CLK => CLK,
			CLRn => CLRn,
			IN_A => DATA(0),
			IN_B => DATA(0),
			D_OUT => DATA_OUT,
			SEL => '1',
			ENA => RD_ENA
		);

	CONTROL: WF25913IP_FIFO_CTRL
		port map(
			CLK => CLK,
			CLRn => CLRn,
			WR_ENA => WR_ENA,
			RD_ENA => RD_ENA,
			FIFO_FULL => FIFO_FULL,
			FIFO_HI => FIFO_HI,
			FIFO_LOW => FIFO_LOW,
			FIFO_EMPTY => FIFO_EMPTY,
			FIFO_ERR => ERR,
			SEL => SEL_CELL,
			ENA => ENA_CELL
		);
end architecture STRUCTURE;
