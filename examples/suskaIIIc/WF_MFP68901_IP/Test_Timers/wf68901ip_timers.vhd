----------------------------------------------------------------------
----                                                              ----
---- Test module to verify the timer prescalers.                  ----
----                                                              ----
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
library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity TIMERS is
	port (  -- System control:
			CLK			: in bit;
			RESETn		: in bit;

			-- Timers and timer control:
			XTAL1		: in bit;
			TCDCR		: in bit_vector(5 downto 3);
			C_CNTSTRB	: out bit
	);
end entity TIMERS;

architecture BEHAVIOR of TIMERS is
signal XTAL1_S		: bit;
signal XTAL_STRB	: bit;
begin
	SYNC: process
	-- This process provides a 'clean' XTAL1.
	-- Without this sync, the edge detector for
	-- XTAL_STRB does not work properly.
	begin
		wait until CLK = '1' and CLK' event;
		XTAL1_S <= XTAL1;
	end process SYNC;
	
	XTAL_STROBE: process(RESETn, CLK)
	-- This process provides a strobe with 1 clock cycle
	-- (CLK) length after every rising edge of XTAL1.
	variable LOCK : boolean;
	begin
		if RESETn = '0' then
			XTAL_STRB <= '0';
		elsif CLK = '1' and CLK' event then
			if XTAL1_S = '1' and LOCK = false then
				XTAL_STRB <= '1';
				LOCK := true;
			elsif XTAL1_S = '0' then
				XTAL_STRB <= '0';
				LOCK := false;

			else
				XTAL_STRB <= '0';
			end if;
		end if;
	end process XTAL_STROBE;

	PRESCALE_C: process
	-- The prescalers work even if the RESETn is asserted.
	variable PRESCALE : std_logic_vector(7 downto 0);
	begin
		wait until CLK = '1' and CLK' event;
		C_CNTSTRB <= '0';
		if PRESCALE > x"00" and XTAL_STRB = '1' then
			PRESCALE := PRESCALE - '1';
		elsif XTAL_STRB = '1' then
			case TCDCR(5 downto 3) is
				when "111" => PRESCALE := x"C7"; -- Prescaler = 200.
				when "110" => PRESCALE := x"63"; -- Prescaler = 100.
				when "101" => PRESCALE := x"3F"; -- Prescaler = 64.
				when "100" => PRESCALE := x"31"; -- Prescaler = 50.
				when "011" => PRESCALE := x"0F"; -- Prescaler = 16.
				when "010" => PRESCALE := x"09"; -- Prescaler = 10.
				when "001" => PRESCALE := x"03"; -- Prescaler = 4.
				when "000" => PRESCALE := x"00"; -- Timer stopped.
			end case;
			C_CNTSTRB <= '1';
		end if;
	end process PRESCALE_C;
end architecture BEHAVIOR;