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
---- Video timing.                                                ----
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
-- Revision 2K6B	2006/11/05 WF
--   Modified Source to compile with the Xilinx ISE.
-- Revision 2K7A  2007/01/02 WF
--   Changes to the clock system and related
--   hardware as video timing or paddles.
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- Revision 2K9A  2008/06/29 WF
--   Changes concerning the SH_MOD for multisync compatibility.
--   Enhancements for the multisync compatible video modi.
--   Modofied the HSYNCn, VSYNCn, DEn and BLANKn comparators
--   resulting in registered controls. This is a bugfix to
--   make the core compatible with the original GLUE concerning
--   software overscanning etc. Thanks to Lyndon Amsdon for the
--   information.
--   SYNCMODE has now synchronous reset to meet preset requirement.
--   Changed reset condition for the SHIFTMODE_REG to let multisyncs
--     with older TOS versions.
-- 

library work;
use work.wf25915ip_pkg.all;
library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25915IP_VIDEO_TIMING is
	port(
		RESETn			: in bit;
		CLK				: in bit;
		CLKSEL			: in CLKSEL_TYPE;
		DATA_IN			: in std_logic_vector(7 downto 0);
		DATA_OUT		: out std_logic_vector(1 downto 0);
		DATA_EN			: out bit;
		RWn				: in bit;
		SYNCMODE_CSn	: in bit;
		SHIFTMODE_CSn	: in bit;
		DE				: out bit;
		MULTISYNC		: in bit_vector(1 downto 0);
		VIDEO_HIMODE    : out bit;
		BLANKn			: out bit;
		VSYNC_INn		: in bit;
		HSYNC_INn		: in bit;
		VSYNC_OUTn		: out bit;
		HSYNC_OUTn		: out bit;
		SYNC_OUT_EN		: out bit
	);
end entity WF25915IP_VIDEO_TIMING;
	
architecture BEHAVIOR of WF25915IP_VIDEO_TIMING is
signal SYNCMODE			: bit_vector(1 downto 0);
signal SHIFTMODE		: bit_vector(7 downto 0);
signal HTEMP			: std_logic_vector(8 downto 0);
signal VTEMP			: std_logic_vector(8 downto 0);
signal HSYNC_In			: bit; -- SYNCn asserted for horizontal deflection.
signal VSYNC_In			: bit; -- SYNCn asserted for vertical deflection.
signal VBLANKn			: bit; -- BLANKn asserted by vertical deflection.
signal HBLANKn			: bit; -- BLANKn asserted by horizontal deflection.
signal VDE				: bit; -- DE asserted by vertical deflection.
signal HDE				: bit; -- DE asserted by horizontal deflection.
begin
	-- Read back the SYNCMODE register. The SHIFTMODE is write only in the GLUE.
	DATA_OUT <= To_StdLogicVector(SYNCMODE) when SYNCMODE_CSn = '0' and RWn = '1' else "00";
    DATA_EN <= '1' when SYNCMODE_CSn = '0' and RWn = '1' else '0';

	SYNCMODE_REG: process
	begin
		wait until CLK = '1' and CLK' event;
		if RESETn = '0' then
			SYNCMODE <= "10"; -- Default is 50Hz, internal video.
		elsif SYNCMODE_CSn = '0' and RWn = '0' then
			SYNCMODE <= To_BitVector(DATA_IN(1 downto 0));
		end if;
	end process SYNCMODE_REG;

	SHIFTMODE_REG: process
	-- This is the shift mode register. It is located in this GLUE (as mirror) and in
	-- the SHIFTER. At this place it is responsible for switching the correct VSYNC
	-- and HSYNC frequencies. This register is write only in the GLUE. The read
	-- access happens via the SHIFTER chip.
	begin
		wait until CLK = '1' and CLK' event;
        if RESETn = '0' then
            case MULTISYNC is
                when "01" => SHIFTMODE <= x"10";
                when "10" => SHIFTMODE <= x"20";
                when others => SHIFTMODE <= x"00"; -- Default is 320x200.
            end case;
        elsif MULTISYNC = "10" and SHIFTMODE_CSn = '0' and RWn = '0' and DATA_IN = x"01" then
            SHIFTMODE <= x"21"; -- 640x200x2 in 72Hz multisync compatible mode.
        elsif MULTISYNC = "10" and SHIFTMODE_CSn = '0' and RWn = '0' and DATA_IN = x"00" then
            SHIFTMODE <= x"20"; -- 320x200x4 in 72Hz multisync compatible mode.
        elsif MULTISYNC = "01" and SHIFTMODE_CSn = '0' and RWn = '0' and DATA_IN = x"01" then
            SHIFTMODE <= x"11"; -- 640x200x2 in 72Hz multisync compatible mode.
        elsif MULTISYNC = "01" and SHIFTMODE_CSn = '0' and RWn = '0' and DATA_IN = x"00" then
            SHIFTMODE <= x"10"; -- 320x200x4 in 72Hz multisync compatible mode.
        elsif MULTISYNC = "00" and SHIFTMODE_CSn = '0' and RWn = '0' then
            SHIFTMODE <= x"32";-- Multisync 72Hz monochrome mode.
        elsif SHIFTMODE_CSn = '0' and RWn = '0' then
            SHIFTMODE <= To_BitVector(DATA_IN);
        end if;
	end process SHIFTMODE_REG;

	with SHIFTMODE select
		VIDEO_HIMODE <= '1' when x"21" | x"20" | x"11" | x"10", -- Multisync compatible colour video modes.
						'0' when others;

	HSYNC_COUNT: process(CLK, RESETn, SHIFTMODE_CSn)
	-- This process must work on the positive clock edge to provide proper operation
	-- of the SYNC process of the time slice counter in the MCU's control file!
	variable TMP	: bit;
    begin
        if RESETn = '0' then
            HTEMP <= (others => '0');
        elsif CLK = '1' and CLK' event then
			TMP := not TMP; -- Toggle function.
			if CLKSEL = CLK_8M and (SHIFTMODE = x"32" or SHIFTMODE = x"02") and HTEMP < "011011111" then
				HTEMP <= HTEMP + '1'; -- 28us period, monochrome mode.
			elsif CLKSEL = CLK_8M and (SHIFTMODE = x"21" or SHIFTMODE = x"20") and HTEMP < "011011111" then
				HTEMP <= HTEMP + '1'; -- 28us period, multisync compatible colour modi.
			elsif CLKSEL = CLK_8M and (SHIFTMODE = x"11" or SHIFTMODE = x"10") and HTEMP < "011111111" then
				HTEMP <= HTEMP + '1'; -- 32us period, multisync compatible colour modi.
			elsif CLKSEL = CLK_8M and (SHIFTMODE = x"01" or SHIFTMODE = x"00") and HTEMP < "111111111" then
				HTEMP <= HTEMP + '1'; -- 64us period.
			elsif CLKSEL = CLK_16M and TMP = '1' and (SHIFTMODE = x"32" or SHIFTMODE = x"02") and HTEMP < "011011111" then
				HTEMP <= HTEMP + '1'; -- 28us period, monochrome mode.
			elsif CLKSEL = CLK_16M and TMP = '1' and (SHIFTMODE = x"21" or SHIFTMODE = x"20") and HTEMP < "011011111" then
				HTEMP <= HTEMP + '1'; -- 28us period, multisync compatible colour modi.
			elsif CLKSEL = CLK_16M and TMP = '1' and (SHIFTMODE = x"11" or SHIFTMODE = x"10") and HTEMP < "011111111" then
				HTEMP <= HTEMP + '1'; -- 32us period, multisync compatible colour modi.
			elsif CLKSEL = CLK_16M and TMP = '1' and (SHIFTMODE = x"01" or SHIFTMODE = x"00") and HTEMP < "111111111" then
				HTEMP <= HTEMP + '1'; -- 64us period.
			elsif CLKSEL = CLK_16M and TMP = '1' then
				HTEMP <= (others => '0');
			elsif CLKSEL = CLK_8M then
				HTEMP <= (others => '0');
			end if;
    	end if;
	end process HSYNC_COUNT;

	VSYNC_COUNT: process(CLK, SHIFTMODE, SYNCMODE(1))
	-- This process must work on the positive clock edge to provide proper operation
	-- of the SYNC process of the time slice counter in the MCU's control file!
	variable LINES: std_logic_vector(8 downto 0);
	variable EDGE_LOCK: boolean;
	variable SECOND_LINE	: boolean;
    begin
		if SHIFTMODE = x"32" or SHIFTMODE = x"02" then
			LINES := "111110001"; -- 71.96Hz, 497 lines.
		elsif SHIFTMODE = x"21" or SHIFTMODE = x"20" then -- 35.714 kHz multisync compatible modi.
			LINES := "111110001"; -- 71.96Hz, 497 lines.
		elsif (SHIFTMODE = x"11" or SHIFTMODE = x"10") and SYNCMODE(1) = '1' then
			LINES := "100111011";  -- 50.06Hz, 315 lines.
		elsif (SHIFTMODE = x"11" or SHIFTMODE = x"10") and SYNCMODE(1) = '0' then
			LINES := "100000111";  -- 59.96Hz, 263 lines.
		elsif (SHIFTMODE = x"01" or SHIFTMODE = x"00") and SYNCMODE(1) = '1' then
			LINES := "100111011";  -- 50.06Hz, 315 lines.
		elsif (SHIFTMODE = x"01" or SHIFTMODE = x"00") and SYNCMODE(1) = '0' then
			LINES := "100000111";  -- 59.96Hz, 263 lines.
		else
			LINES := "111111111";  -- Default.
		end if;
		--
		if CLK = '1' and CLK' event then
			if HSYNC_In = '0' and EDGE_LOCK = false and (SHIFTMODE = x"11" or SHIFTMODE = x"10") and SECOND_LINE = false then -- Multisync 50/60Hz.
				SECOND_LINE := true;
				EDGE_LOCK := true; -- Count on falling edge of HSYNC_In.
			elsif VTEMP < LINES and HSYNC_In = '0' and EDGE_LOCK = false and (SHIFTMODE = x"11" or SHIFTMODE = x"10") then -- Multisync 50/60Hz.
				SECOND_LINE := false;
				VTEMP <= VTEMP + '1'; -- Count every second HSYNC in video hi modes.
				EDGE_LOCK := true; -- Count on falling edge of HSYNC_In.
			elsif VTEMP < LINES and HSYNC_In = '0' and EDGE_LOCK = false and (SHIFTMODE /= x"11" and SHIFTMODE /= x"10") then -- Other modes.
				VTEMP <= VTEMP + '1';
				EDGE_LOCK := true; -- Count on falling edge of HSYNC_In.
			elsif VTEMP >= LINES then -- Counter reset.
				VTEMP <= (others => '0');
			elsif HSYNC_In = '1' then
				EDGE_LOCK := false;
			end if;
   		end if;
	end process VSYNC_COUNT;

	SYNC_CTRL: process(CLK)
	begin
		if CLK = '1' and CLK' event then
			-- Horizontal synchronisation:
            if SHIFTMODE = x"32" or SHIFTMODE = x"02" then -- 35.714 kHz.
                if HTEMP = "001000000" then
					HSYNC_In <= '0'; -- 3us low.
				elsif HTEMP = "001011000" then
					HSYNC_In <= '1'; -- 25us high.
				end if;
			elsif SHIFTMODE = x"21" or SHIFTMODE = x"20" then -- 35.714 kHz multisync compatible modi.
				if HTEMP = "001000000" then
					HSYNC_In <= '0'; -- 3us low.
				elsif HTEMP = "001011000" then
					HSYNC_In <= '1'; -- 25us high.
				end if;
			elsif SHIFTMODE = x"11" or SHIFTMODE = x"10" then -- 31.25 kHz multisync compatible modi.
				if HTEMP = "010000000" then
					HSYNC_In <= '0'; -- 3us low.
				elsif HTEMP = "010010100" then
					HSYNC_In <= '1'; -- 25us high.
				end if;
			else -- 15.625 kHz.
				if HTEMP = "100000000" then
					HSYNC_In <= '0'; -- 5us low.
				elsif HTEMP = "100101000"  then
					HSYNC_In <= '1'; -- 59us high.
				end if;
			end if;
			-- Vertical synchronisation:
			if SHIFTMODE = x"32" or SHIFTMODE = x"02" then -- 72Hz.
				if VTEMP = "100000000" then
					VSYNC_In <= '0'; -- 1 line low.
				elsif VTEMP = "100000001" then
					VSYNC_In <= '1';
				end if;
			elsif SHIFTMODE = x"21" or SHIFTMODE = x"20" then -- 72.00Hz, Multisync compatible.
				if VTEMP = "100000000" then
					VSYNC_In <= '0'; -- 1 line low.
				elsif VTEMP = "100000001" then
					VSYNC_In <= '1';
				end if;
			elsif (SHIFTMODE = x"11" or SHIFTMODE = x"10") and SYNCMODE(1) = '0' then -- 60.00Hz, Multisync compatible.
				if VTEMP = "001000000" then
					VSYNC_In <= '0'; -- 3 lines low.
				elsif VTEMP = "001000011" then
					VSYNC_In <= '1'; 
				end if;
			elsif (SHIFTMODE = x"11" or SHIFTMODE = x"10") and SYNCMODE(1) = '1' then -- 50.00Hz, Multisync compatible.
				if VTEMP = "001001111" then
					VSYNC_In <= '0'; -- 3 lines low.
				elsif VTEMP = "001010010" then
					VSYNC_In <= '1'; 
				end if;
			elsif (SHIFTMODE = x"01" or SHIFTMODE = x"00") and SYNCMODE(1) = '0' then -- 60.00Hz.
				if VTEMP = "001000000" then
					VSYNC_In <= '0'; -- 3 lines low.
				elsif VTEMP = "001000011" then
					VSYNC_In <= '1'; 
				end if;
			elsif (SHIFTMODE = x"01" or SHIFTMODE = x"00") and SYNCMODE(1) = '1' then -- 50.00Hz.
				if VTEMP = "001001111" then
					VSYNC_In <= '0'; -- 3 lines low.
				elsif VTEMP = "001010010" then
					VSYNC_In <= '1'; 
				end if;
			else
				VSYNC_In <= '1';
			end if;
		end if;
	end process SYNC_CTRL;

	DE_CTRL: process(CLK, RESETn)
	-- This are the sync control flags. Be aware, that the counter values are
	-- correlated to the respective HTEMP values for the HSYNCn.
    begin
		if RESETn = '0' then
			HDE <= '0'; -- Blanking out.
			VDE <= '0'; -- Blanking out.
		elsif CLK = '1' and CLK' event then
			-- Horizontal controls:
			if SHIFTMODE = x"32" then -- 35.714 kHz multisync monochrome mode.
                if HTEMP = "000100010" then
					HDE <= '0'; -- 8us low, 3.75 before and 1.25 after HSYNC.
                elsif HTEMP = "001100010" then
					HDE <= '1';
				end if;
			elsif SHIFTMODE = x"21" or SHIFTMODE = x"20" then -- 35.714 kHz multisync compatible modi.
                if HTEMP = "000100010" then
					HDE <= '0'; -- 8us low, 3.75 before and 1.25 after HSYNC.
                elsif HTEMP = "001100010" then
					HDE <= '1';
				end if;
			elsif SHIFTMODE = x"11" or SHIFTMODE = x"10" then -- 31.25 kHz multisync compatible modi.
				if HTEMP = "001010100" then
					HDE <= '0'; -- 8us low, 4.0 before and 1.0 after HSYNC.
				elsif HTEMP = "010110100" then
					HDE <= '1';
				end if;
			elsif SHIFTMODE = x"02" then -- 35.714 kHz monochrome mode.
                if HTEMP = "000110000" then
					HDE <= '0'; -- 8us low, 2.0 before and 3.0 after HSYNC.
				elsif HTEMP = "001110000" then
					HDE <= '1';
				end if;
			else -- 15.625 kHz.
				if HTEMP = "010110100" then
					HDE <= '0'; -- 24us low, 14 before and 5 after HSYNC.
				elsif HTEMP = "101110100" then
					HDE <= '1';
				end if;
			end if;

			-- Vertical controls:
			if SHIFTMODE = x"32" or SHIFTMODE = x"02" then -- 72Hz.
				if VTEMP = "011010001" then
					VDE <= '0'; -- 97 lines low, 47 before and 49 after VSYNC.
				elsif VTEMP = "100110010" then
					VDE <= '1';
				end if;
			elsif SHIFTMODE = x"21" or SHIFTMODE = x"20" then -- 35.714 kHz multisync compatible modi.
				if VTEMP = "011010001" then
					VDE <= '0'; -- 97 lines low, 47 before and 49 after VSYNC.
				elsif VTEMP = "100110010" then
					VDE <= '1';
				end if;
			elsif (SHIFTMODE = x"11" or SHIFTMODE = x"10") and SYNCMODE(1) = '0' then -- 60.00Hz, Multisync compatible.
				if VTEMP = "000011000" then
					VDE <= '0'; -- 63 lines low, 40 before and 20 after VSYNC.
				elsif VTEMP = "001010111" then
					VDE <= '1';
				end if;
			elsif (SHIFTMODE = x"11" or SHIFTMODE = x"10") and SYNCMODE(1) = '1' then -- 50.00Hz, Multisync compatible.
				if VTEMP = "000000111" then
					VDE <= '0'; -- 115 lines low, 72 before and 40 after VSYNC.
				elsif VTEMP = "001111010" then
					VDE <= '1';
				end if;
			elsif (SHIFTMODE = x"01" or SHIFTMODE = x"00") and SYNCMODE(1) = '0' then -- 60.00Hz.
				if VTEMP = "000011000" then
					VDE <= '0'; -- 63 lines low, 40 before and 20 after VSYNC.
				elsif VTEMP = "001010111" then
					VDE <= '1';
				end if;
			elsif (SHIFTMODE = x"01" or SHIFTMODE = x"00") and SYNCMODE(1) = '1' then -- 50.00Hz.
				if VTEMP = "000000111" then
					VDE <= '0'; -- 115 lines low, 72 before and 40 after VSYNC.
				elsif VTEMP = "001111010" then
					VDE <= '1';
				end if;
			else
				VDE <= '1';
			end if;
		end if;
	end process DE_CTRL;

	BLANK_CTRL: process(CLK, RESETn)
    begin
		if RESETn = '0' then
			HBLANKn <= '0'; -- Blanking out.
			VBLANKn <= '0'; -- Blanking out.
		elsif CLK = '1' and CLK' event then
			-- Horizontal controls:
			if SHIFTMODE = x"32" or SHIFTMODE = x"02" then -- 35.714 kHz.
                if HTEMP = "000110000" then
                    HBLANKn <= '0'; -- 6.5us low, 2.0 before and 1.5 after HSYNC.
				elsif HTEMP = "001100100" then
					HBLANKn <= '1';
				end if;
			elsif SHIFTMODE = x"21" or SHIFTMODE = x"20" then -- 35.714 kHz multisync compatible modi.
				if HTEMP = "000110000" then
					HBLANKn <= '0'; -- 6.5us low, 2.0 before and 1.5 after HSYNC.
				elsif HTEMP = "001100100" then
					HBLANKn <= '1';
				end if;
			elsif SHIFTMODE = x"11" or SHIFTMODE = x"10" then -- 31.25 kHz multisync compatible modi.
				if HTEMP = "001111100" then
					HBLANKn <= '0'; -- 6.5us low, 2.0 before and 1.5 after HSYNC.
				elsif HTEMP = "010101100" then
					HBLANKn <= '1';
				end if;
			else -- 15.625 kHz
				if HTEMP = "011111000" then
					HBLANKn <= '0'; -- 12us low,1 before and 6 after HSYNC.
				elsif HTEMP = "101011000" then
					HBLANKn <= '1';
				end if;
			end if;
			-- Vertical controls:
			if SHIFTMODE = x"32" or SHIFTMODE = x"02" then -- 72Hz.
				if VTEMP = "011101000" then
					VBLANKn <= '0'; -- 49 lines low, 12 before and 36 after VSYNC.
				elsif VTEMP = "100100101" then
					VBLANKn <= '1';
				end if;
			elsif SHIFTMODE = x"21" or SHIFTMODE = x"20" then -- 35.714 kHz multisync compatible modi.
				if VTEMP = "011101000" then
					VBLANKn <= '0'; -- 49 lines low, 12 before and 36 after VSYNC.
				elsif VTEMP = "100100101" then
					VBLANKn <= '1';
				end if;
			elsif (SHIFTMODE = x"11" or SHIFTMODE = x"10") and SYNCMODE(1) = '0' then -- 60.00Hz, Multisync compatible.
				if VTEMP = "000101110" then
					VBLANKn <= '0'; -- 31 lines low,18 before and 10 after VSYNC.
				elsif VTEMP = "001001101" then
					VBLANKn <= '1';
				end if;
			elsif (SHIFTMODE = x"11" or SHIFTMODE = x"10") and SYNCMODE(1) = '1' then -- 50.00Hz, Multisync compatible.
				if VTEMP = "000101100" then
					VBLANKn <= '0'; -- 60 lines low,35 before and 20 after VSYNC.
				elsif VTEMP = "001100110" then
					VBLANKn <= '1';
				end if;
			elsif (SHIFTMODE = x"01" or SHIFTMODE = x"00") and SYNCMODE(1) = '0' then -- 60.00Hz.
				if VTEMP = "000101110" then
					VBLANKn <= '0'; -- 31 lines low,18 before and 10 after VSYNC.
				elsif VTEMP = "001001101" then
					VBLANKn <= '1';
				end if;
			elsif (SHIFTMODE = x"01" or SHIFTMODE = x"00") and SYNCMODE(1) = '1' then -- 50.00Hz.
				if VTEMP = "000101100" then
					VBLANKn <= '0'; -- 60 lines low,35 before and 20 after VSYNC.
				elsif VTEMP = "001100110" then
					VBLANKn <= '1';
				end if;
			else
				VBLANKn <= '1';
			end if;
		end if;
	end process BLANK_CTRL;

	CLK_OUT: process
	-- The horizontal and vertical synchronisation signals are working also during
	-- a system reset. Therefore the process has no sensitivity list and is modeled
	-- with a wait statement.
	begin
		wait until CLK = '1' and CLK' event;
		case SYNCMODE(0) is
			when '0' =>
				-- Internal syncs.
				HSYNC_OUTn <= HSYNC_In;
				VSYNC_OUTn <= VSYNC_In;
			when '1' =>
				-- GENLOCK syncs.
				HSYNC_OUTn <= '1';
				VSYNC_OUTn <= '1';
		end case;

		if VBLANKn = '0' or HBLANKn = '0' then
			BLANKn <= '0';
		else
			BLANKn <= '1';
		end if;
	end process CLK_OUT;

	SYNC_OUT_EN <= '1' when SYNCMODE(0) = '0' else '0';

	DE_OUT: process(RESETn, CLK)
	-- The DE (data enable) signal is besides the video data enable function also
	-- responsible for some synchronization mechanisms in the MCU and the GLUE. To
	-- ensure correct behavior, the DE is delayed after a reset of the system or 
	-- during a first system startup. The delay is about 8ms for a 8MHz clock and
	-- 4ms for a 16MHz clock.
	variable TMP: std_logic_vector(15 downto 0);
	begin
		if RESETn = '0' then
			DE <= '0';
			TMP := (others => '0');
		elsif CLK = '1' and CLK' event then
			if TMP < x"FFFF" then
				TMP := TMP + '1'; -- Startup delay.
			end if;
			case SYNCMODE(0) is
				when '0' => -- Internal syncs.
					if TMP = x"FFFF" then
						DE <= VDE and HDE;
					else
						DE <= '0';
					end if;
				when '1' => -- GENLOCK syncs.
					if VSYNC_INn = '1' and HSYNC_INn = '1' then
						DE <= '1';
					else
						DE <= '0';
					end if;
			end case;
		end if;
	end process DE_OUT;
end BEHAVIOR;
