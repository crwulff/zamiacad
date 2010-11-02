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
---- The video access is done by the counter in this file.        ----
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
-- Revision 2K7A  2007/01/02 WF
--   Changes to the clock system and related
--   hardware as sound or video control.
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25912IP_VIDEO_COUNTER is
port (  CLK_x2	: in bit;
		RESETn	: in bit;
		RWn		: in bit; -- Read write control.
		
		VIDEO_BASE_HI_CS	: in bit; -- Register control signal.
		VIDEO_BASE_MID_CS	: in bit; -- Register control signal.
		VIDEO_BASE_LOW_CS	: in bit; -- Register control signal.

		VIDEO_COUNT_HI_CS	: in bit; -- Register control signal.
		VIDEO_COUNT_MID_CS	: in bit; -- Register control signal.
		VIDEO_COUNT_LOW_CS	: in bit; -- Register control signal.
		
		DE					: in bit; -- SHIFTER's data enable.
		VIDEO_COUNT_EN		: in bit; -- Counter enable.
		VIDEO_COUNT_LOAD	: in bit; -- Load control.

		LINEWIDTH_CS		: in bit; -- Select for the STEs linewidth register.
		
		VIDEO_ADR			: out bit_vector(23 downto 1);
		
		DATA_IN			: in std_logic_vector(7 downto 0);
		DATA_OUT		: out bit_vector(7 downto 0);
		DATA_EN			: out bit
      );
end WF25912IP_VIDEO_COUNTER;

architecture BEHAVIOR of WF25912IP_VIDEO_COUNTER is
signal VADR				: std_logic_vector(23 downto 1);
signal VIDEO_BASE_HI	: std_logic_vector(7 downto 0);
signal VIDEO_BASE_MID	: std_logic_vector(7 downto 0);
signal VIDEO_BASE_LOW	: std_logic_vector(7 downto 0);
signal LINEWIDTH		: std_logic_vector(7 downto 0);
begin
	LINEWIDTH_REG: process(RESETn, CLK_x2)
	-- Line width register.
	begin
		if RESETn = '0' then
			LINEWIDTH <= (others => '0');
		elsif CLK_x2 = '1' and CLK_x2' event then
			if LINEWIDTH_CS = '1' and RWn = '0' then
				LINEWIDTH <= DATA_IN; -- Write to register.
			end if;
		end if;
	end process LINEWIDTH_REG;

	BASE_REGS: process(CLK_x2, RESETn)
	-- Video base register.
	begin
		if RESETn = '0' then
			VIDEO_BASE_HI <= (others => '0');
			VIDEO_BASE_MID <= (others => '0');
			VIDEO_BASE_LOW <= (others => '0');
		elsif CLK_x2 = '1' and CLK_x2' event then
			if VIDEO_BASE_HI_CS = '1' and RWn = '0' then
				VIDEO_BASE_HI <= DATA_IN; -- Write to register.
			elsif VIDEO_BASE_MID_CS = '1' and RWn = '0' then
				VIDEO_BASE_MID <= DATA_IN; -- Write to register.
			elsif VIDEO_BASE_LOW_CS = '1' and RWn = '0' then
				VIDEO_BASE_LOW <= DATA_IN; -- Write to register.
			end if;
		end if;
	end process BASE_REGS;

	VIDEO_CNT: process(CLK_x2, RESETn, LINEWIDTH)
	-- Video address counter process.
	variable LOCK : boolean;
	begin
		if RESETn = '0' then
			VADR <= (others => '0');
			LOCK := false;
		elsif CLK_x2 = '1' and CLK_x2' event then
			-- Due to this placement in the code, the write access to the video
			-- counter via the data bus has priority over VIDEO_COUNT_LOAD
			-- and VIDEO_COUNT_EN.
			-- Write access to video counter as in STEs (not in STs).
			if VIDEO_COUNT_HI_CS = '1' and RWn = '0' then
				VADR(23 downto 16) <= DATA_IN; -- Write to register.
			elsif VIDEO_COUNT_MID_CS = '1' and RWn = '0' then
				VADR(15 downto 8) <= DATA_IN; -- Write to register.
			elsif VIDEO_COUNT_LOW_CS = '1' and RWn = '0' then
				VADR(7 downto 1) <= DATA_IN(7 downto 1); -- Write to register.
			elsif VIDEO_COUNT_LOAD = '1' then -- Load has priority over count_en.
				 -- Begin the first line with n bytes delayed.
				VADR <= VIDEO_BASE_HI & VIDEO_BASE_MID & VIDEO_BASE_LOW (7 downto 1) + LINEWIDTH(3 downto 0);
			elsif VIDEO_COUNT_EN = '1' then
				VADR <= VADR + '1'; -- Count normally.
				LOCK := false;
			elsif DE = '0' and LOCK = false then
				VADR <= VADR + LINEWIDTH(3 downto 0); -- Increment once during HSYNC pulse.
				LOCK := true; -- Locks the incrementation until next DE falling edge.
			end if;
		end if;
	end process VIDEO_CNT;

	-- Read registers.
	DATA_OUT <= To_BitVector(LINEWIDTH) when  LINEWIDTH_CS = '1' and RWn = '1' else
				To_BitVector(VIDEO_BASE_HI) when VIDEO_BASE_HI_CS = '1' and RWn = '1' else
				To_BitVector(VIDEO_BASE_MID) when VIDEO_BASE_MID_CS = '1' and RWn = '1' else
				To_BitVector(VIDEO_BASE_LOW) when VIDEO_BASE_LOW_CS = '1' and RWn = '1' else
				To_BitVector(VADR(23 downto 16)) when VIDEO_COUNT_HI_CS = '1' and RWn = '1' else
				To_BitVector(VADR(15 downto 8)) when VIDEO_COUNT_MID_CS = '1' and RWn = '1' else
				To_BitVector(VADR(7 downto 1)) & '0' when VIDEO_COUNT_LOW_CS = '1' and RWn = '1' else x"00";

	DATA_EN <= 	(LINEWIDTH_CS or VIDEO_BASE_HI_CS or VIDEO_BASE_MID_CS or VIDEO_BASE_LOW_CS or
				VIDEO_COUNT_HI_CS or VIDEO_COUNT_MID_CS or VIDEO_COUNT_LOW_CS) and RWn;

	VIDEO_ADR <= To_BitVector(VADR);
end architecture BEHAVIOR;

