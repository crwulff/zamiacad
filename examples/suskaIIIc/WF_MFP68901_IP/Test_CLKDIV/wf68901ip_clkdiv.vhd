----------------------------------------------------------------------
----                                                              ----
---- ATARI MFP compatible IP Core					              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- Teststuff: clock divider.                                    ----
----                                                              ----
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
---- Copyright (C) 2006 Wolfgang Foerster                         ----
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

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity TEST_CLKDIV is
  port (
		CLK			: in bit;
		TXCLK		: in bit;
        CLK_MODE	: in bit;
		CLK_STRB	: out bit;
		CLK_2_STRB	: out bit

       );                                              
end entity TEST_CLKDIV;

architecture BEHAVIOR of TEST_CLKDIV is
begin
	CLKDIV: process
	variable CLK_LOCK	: boolean;
	variable STRB_LOCK	: boolean;
	variable CLK_DIVCNT	: std_logic_vector(4 downto 0);
	begin
		wait until CLK = '1' and CLK' event;
		if CLK_MODE = '0' then -- Divider off.
			if TXCLK = '0' and STRB_LOCK = false then  -- Works on negative TXCLK edge.
				CLK_STRB <= '1';
				STRB_LOCK := true;
			elsif TXCLK = '1' then
				CLK_STRB <= '0';
				STRB_LOCK := false;
			else
				CLK_STRB <= '0';
			end if;
			CLK_2_STRB <= '0'; -- No 1 1/2 stop bits in no div by 16 mode.
		else
			CLK_STRB <= '0'; -- Default.
			CLK_2_STRB <= '0'; -- Default.
			-- Works on negative TXCLK edge:
			if CLK_DIVCNT > "00000" and TXCLK = '0' and CLK_LOCK = false then
				CLK_DIVCNT := CLK_DIVCNT - '1';
				CLK_LOCK := true;
				if CLK_DIVCNT = "01000" then
					-- This strobe is asserted at half of the clock cycle.
					-- It is used for the stop bit timing.
					CLK_2_STRB <= '1';
				end if;
			elsif CLK_DIVCNT = "00000" then
				CLK_DIVCNT := "10000"; -- Div by 16 mode.
				if STRB_LOCK = false then
					STRB_LOCK := true;
					CLK_STRB <= '1';
				end if;
			elsif TXCLK = '1' then
				CLK_LOCK := false;
				STRB_LOCK := false;
			end if;
		end if;
	end process CLKDIV;
end architecture BEHAVIOR;

