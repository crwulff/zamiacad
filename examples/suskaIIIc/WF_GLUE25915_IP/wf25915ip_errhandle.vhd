----------------------------------------------------------------------
----                                                              ----
---- ATARI GLUE compatible IP Core					              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- Atari's ST Glue with all features to reach                   ----
---- ATARI STE compatibility.                                     ----
----                                                              ----
---- Buss error handler / timeout unit.                           ----
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
---- Copyright (C) 2005 - 2008 Wolfgang Foerster                  ----
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
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- Revision 2K9A  2009/06/20 WF
--   Process P1 has now synchronous reset to meet preset requirements.
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25915IP_ERRHANDLE is
	port(
		RESETn	: in bit;
		CLK		: in bit;
		ASn		: in bit;

		BERRn	: out bit
	);
end entity WF25915IP_ERRHANDLE;
	
architecture BEHAVIOR of WF25915IP_ERRHANDLE is
begin
	P1: process
	variable WATCHDOG: std_logic_vector(5 downto 0); -- 6 bit -> 64 steps.
	begin
		wait until CLK = '1' and CLK' event;
		if RESETn = '0' then
			WATCHDOG := (others =>'1'); -- Load the counter.
			BERRn <= '1';
		-- After DTACKn is released by the target, the bus master deasserts
		-- ASn and herewith reloads the watchdog.
		elsif ASn = '1' then
			WATCHDOG := (others =>'1'); -- Load the counter.
		elsif WATCHDOG > "000000" then
			WATCHDOG := WATCHDOG - 1;
		end if;

		-- Error released if there is no response from a target after
		-- 64 clock cycles.
		if WATCHDOG = "000000" then
			BERRn <= '0'; -- No answer after 64 clock periods after request. 
		else
			BERRn <= '1';
		end if;
	end process P1;
end BEHAVIOR;
