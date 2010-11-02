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
---- Control logic for the DMA FIF0. For further information see  ----
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

library ieee;
use ieee.std_logic_1164.all;

entity WF25913IP_FIFO_CTRL is
	port(
		CLK 		: in bit; 
		CLRn 		: in bit;
		WR_ENA 		: in bit;
		RD_ENA 		: in bit;
		FIFO_FULL	: out bit;
		FIFO_HI 	: out bit;
		FIFO_LOW	: out bit;
		FIFO_EMPTY	: out bit;
		FIFO_ERR	: out bit;
		SEL 		: out bit_vector(16 downto 1);
		ENA 		: out bit_vector(16 downto 1)
	);
end WF25913IP_FIFO_CTRL;

architecture BEHAVIOR of WF25913IP_FIFO_CTRL is
signal WR_PNT 		: natural;
begin
	WRITELOGIC: process (CLK, CLRn)
	subtype T_01 is natural range 0 to 1; 
	variable WRITE : T_01;
	variable READ : T_01;
	begin
		if CLRn ='0' then 
			WR_PNT <= 0;
		elsif CLK = '1' and CLK' event then
			if WR_ENA = '1' then
				WRITE := 1;
			elsif WR_ENA = '0' then
				WRITE := 0;
			end if;
			if RD_ENA = '1' then
				READ := 1;
			elsif RD_ENA = '0' then
				READ := 0;
			end if;
			if WR_PNT = 16 and WRITE = 1 and READ = 0 then
				FIFO_ERR <= '1'; -- FIFO full, no further write.
			elsif WR_PNT = 0 and WRITE = 0 and READ = 1 then
				FIFO_ERR <= '1'; -- FIFO empty, no further read.
			else
				WR_PNT <= WR_PNT + WRITE - READ;
				FIFO_ERR <= '0';
			end if;
		end if;
	end process;

	SELECTLINES: process (WR_PNT, RD_ENA)
	begin
		for i in 16 downto 1 loop
			if i < WR_PNT then 
				SEL(i) <= '1';
			else
				SEL(i) <= '0';
			end if;
			if i > WR_PNT then 
				ENA(i) <= '1';
			else
				ENA(i) <= RD_ENA;
			end if;
		end loop;
	end process;

	FIFO_FULL <= '1' when WR_PNT = 16 else '0';
	FIFO_HI <= '1' when WR_PNT >= 8 else '0';
	FIFO_LOW <= '1' when WR_PNT <= 8 else '0';
	FIFO_EMPTY <= '1' when WR_PNT = 0 else '0';
end architecture BEHAVIOR;
