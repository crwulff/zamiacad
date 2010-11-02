----------------------------------------------------------------------
----                                                              ----
---- ATARI MCU compatible IP Core					              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- Memory management controller with all features to reach      ----
---- ATARI STE compatibility.                                     ----
----                                                              ----
---- This part of software is the DMA address counter.            ----
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
---- Copyright (C) 2005 Wolfgang Foerster                         ----
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
-- Initial Release.
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25912IP_DMA_CTRL is
port (  CLK		: in bit;
		RESETn	: in bit;
		RWn		: in bit; -- Read write control.
		
		DMA_BASE_HI_CS	: in bit; -- Register control signal.
		DMA_BASE_MID_CS	: in bit; -- Register control signal.
		DMA_BASE_LOW_CS	: in bit; -- Register control signal.

		DMA_COUNT_EN	: in bit; -- counter enable

		DMA_ADR	: out bit_vector(23 downto 1);
		
		DATA_IN			: in std_logic_vector(7 downto 0);
		DATA_OUT		: out bit_vector(7 downto 0);
		DATA_EN			: out bit
      );
end WF25912IP_DMA_CTRL;

architecture BEHAVIOR of WF25912IP_DMA_CTRL is
signal DMAADR	: std_logic_vector(23 downto 1);
begin
	DMA_REG: process(RESETn, CLK)
	begin
		if RESETn = '0' then
			DMAADR <= (others => '0');
		elsif CLK = '1' and CLK' event then
			if DMA_BASE_HI_CS = '1' and RWn = '0' then
				DMAADR(23 downto 16) <= DATA_IN; -- Write to register.
			elsif DMA_BASE_MID_CS = '1' and RWn = '0' then
				DMAADR(15 downto 8) <= DATA_IN; -- Write to register.
			elsif DMA_BASE_LOW_CS = '1' and RWn = '0' then
				DMAADR(7 downto 1) <= DATA_IN(7 downto 1); -- Write to register.
			elsif DMA_COUNT_EN = '1' then
				DMAADR <= DMAADR + '1';
			end if;
		end if;
	end process DMA_REG;
	-- Read registers:
	DATA_OUT <=	To_BitVector(DMAADR(23 downto 16)) when DMA_BASE_HI_CS = '1' and RWn = '1' else
				To_BitVector(DMAADR(15 downto 8)) when DMA_BASE_MID_CS = '1' and RWn = '1' else
				To_BitVector(DMAADR(7 downto 1)) & '0' when DMA_BASE_LOW_CS = '1' and RWn = '1' else (others => '0');
	
	DATA_EN <= (DMA_BASE_HI_CS or DMA_BASE_MID_CS or DMA_BASE_LOW_CS) and RWn;

	DMA_ADR <= To_BitVector(DMAADR);
end architecture BEHAVIOR;
