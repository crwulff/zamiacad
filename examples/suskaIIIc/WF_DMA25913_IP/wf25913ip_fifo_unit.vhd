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
---- This is the DMA FIF0 cell. For further information see       ----
---- also the files dma_fifo.vhd and dma_fifo_unit.vhd.           ----
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
--   No changes.
-- 

entity WF25913IP_FIFO_UNIT is
	port(
		CLK		: in bit;
		CLRn	: in bit;
		IN_A	: in bit_vector(15 downto 0);
		IN_B	: in bit_vector(15 downto 0);
		SEL 	: in bit;
		ENA		: in bit;
		D_OUT 	: out bit_vector(15 downto 0)
	);
end WF25913IP_FIFO_UNIT;

architecture BEHAVIOR of WF25913IP_FIFO_UNIT is
signal REG_IN	: bit_vector(15 downto 0);
begin
	IN_MUX: process (SEL, IN_A, IN_B)
	begin
		case SEL is
			when '0' => REG_IN <= IN_A;
			when '1' => REG_IN <= IN_B;
		end case;
	end process IN_MUX;

	MEM_UNIT: process(CLK, ENA, CLRn)
		variable FIFOCELL : bit_vector(15 DOWNTO 0);
	begin
		if CLRn = '0' then
			FIFOCELL := (others => '0');
		elsif CLK = '1' and CLK' event and ENA = '1' then
			FIFOCELL := REG_IN;
		end if; 	
		D_OUT <= FIFOCELL;
	end process MEM_UNIT;
end architecture BEHAVIOR;
