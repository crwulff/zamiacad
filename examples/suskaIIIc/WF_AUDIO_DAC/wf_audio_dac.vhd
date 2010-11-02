----------------------------------------------------------------------
----                                                              ----
---- ATARI IP Core peripheral Add-On				              ----
----                                                              ----
---- This file is part of the FPGA-ATARI project.                 ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- This hardware features the control of a serial DAC 'AD5302'  ----
---- from Analog Devices. The original Mega ST hardware was       ----
---- equipped with two ADC0802 parallel DACs. This controller     ----
---- uses the parallel data as the source for the serial device.  ----
----                                                              ----
---- To Do:                                                       ----
---- -                                                            ----
----                                                              ----
---- Author(s):                                                   ----
---- - Wolfgang Foerster, wf@experiment-s.de; wf@inventronik.de   ----
----                                                              ----
----------------------------------------------------------------------
----                                                              ----
---- Copyright (C) 2007 - 2008 Wolfgang Foerster                  ----
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
-- Revision 2K7A  2007/02/03 WF
--   Initial Release.
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF_AUDIO_DAC is
	port (
		CLK				: in bit; -- Use 16MHz.
		RESETn			: in bit;

		SDATA_L			: in bit_vector(7 downto 0);
		SDATA_R			: in bit_vector(7 downto 0);
		
		DAC_SCLK		: out bit;
		DAC_SDATA		: out bit;
		DAC_SYNCn		: out bit;
		DAC_LDACn		: out bit
	);
end entity WF_AUDIO_DAC;

architecture BEHAVIOR of WF_AUDIO_DAC is
signal DAC_TX				: bit_vector(16 downto 0);
signal DAC_SH_CLK			: bit;
signal SHIFT_READY			: boolean;
signal SHIFT_EN				: boolean;
-- DAC state machine:
type DAC_CTRL_STATES is (IDLE, LOAD_L, LOAD_R, SHIFT_L, SHIFT_R, LOAD_DAC);
signal DAC_CTRL_STATE		: DAC_CTRL_STATES;
signal NEXT_DAC_CTRL_STATE	: DAC_CTRL_STATES;
begin
	DAC_SDATA 	<= DAC_TX(16); -- The MSB is shifted first.
	DAC_SCLK 	<= '1' when DAC_SH_CLK = '1' and SHIFT_EN = true else '0';
	DAC_SYNCn 	<= '0' when SHIFT_EN = true else '1';
	DAC_LDACn   <= '0' when DAC_CTRL_STATE = LOAD_DAC else '1';

	DAC_CLOCK: process(CLK, RESETn)
	-- This process generates a DAC_SH_CLK signal with a sixteenth of the
	-- frequency of CLK.
	variable DAC_SH_CLK_COUNT: std_logic_vector(3 downto 0);
	begin
		if RESETn = '0' then
			DAC_SH_CLK_COUNT := (others => '0');
		elsif CLK = '1' and CLK' event then
			DAC_SH_CLK_COUNT := DAC_SH_CLK_COUNT + '1'; 
		end if;
		DAC_SH_CLK <= To_Bit(DAC_SH_CLK_COUNT(3)); 	-- 1/16 of CLK.
	end process DAC_CLOCK;

	TX_SHIFT: process(RESETn, CLK)
	variable LOCK	: boolean;
	begin
		if RESETn = '0' then
			DAC_TX	 	<= '0' & x"0000";	-- 16 bit.
			LOCK		:= false;
		elsif CLK = '1' and CLK' event then
			-- The 16 DAC bits are as follows:
			-- 15: Select '0' for channel A and '1' for channel B.
			-- 14: Select '0' for unbuffered reference and '1' for buffered.
			-- 13, 12: Power down bits. See data sheet for mor information.
			-- 11 ... 4: This are the data bits.
			-- 3 ... 0: not used bits for the AD5302, don't care.
			if DAC_CTRL_STATE = LOAD_L then
				DAC_TX <=  '0' & x"0" & SDATA_L & x"0";
			elsif DAC_CTRL_STATE = LOAD_R then
				DAC_TX <=  '0' & x"8" & SDATA_R & x"0";
			elsif SHIFT_EN = true then -- Shift has priority.
				-- The shift register operates on the falling edge of DAC_SH_CLK
				-- due to the sampling by the DAC with the negative edge of DAC_SH_CLK.
				if DAC_SH_CLK = '1' and LOCK = false then -- Sampling on positive clock edge.
					LOCK := true; -- Operate on rising edge of DAC_SH_CLK.
					DAC_TX <= DAC_TX(15 downto 0) & DAC_TX(16); -- Rotate left, MSB first.
				elsif DAC_SH_CLK = '0' then
					LOCK := false;
				end if;
			end if;
		end if;
	end process TX_SHIFT;

	BITCNT: process(RESETn, CLK, DAC_SH_CLK)
	variable BIT_CNT 	: std_logic_vector(4 downto 0); 
	variable LOCK		: boolean;
	begin
		if RESETn = '0' then
			BIT_CNT	 	:= (others => '0');
			LOCK		:= false;
		elsif CLK = '1' and CLK' event then
			if SHIFT_EN = false then
				BIT_CNT := "10000";	-- Load 16 bit.
			elsif DAC_SH_CLK = '0' then
				LOCK := false;
			elsif DAC_SH_CLK = '1' and LOCK = false then
				LOCK := true; -- Operate on rising edge of DAC_SH_CLK.
				BIT_CNT := BIT_CNT - '1';
			end if;
			--
			-- Wait until DAC_SH_CLK pulse finished in count state zero:
			if BIT_CNT = "00000" and DAC_SH_CLK = '0' then
				SHIFT_READY <= true;
			else
				SHIFT_READY <= false;
			end if;
		end if;
	end process BITCNT;

	DAC_CTRL_STATE_REGISTER: process(RESETn, CLK)
	-- The DAC control state machine is slowed down to a quarter of the
	-- input frequency. This improves the electrical behavior of the
	-- DAC control signals which have lower electromagnetic interference
	-- using lower frequencies.
	variable TMP : std_logic_vector(1 downto 0);
	begin
		if RESETn = '0' then
			DAC_CTRL_STATE <= IDLE;
		elsif CLK = '1' and CLK' event then
			if TMP = "11" then
				DAC_CTRL_STATE <= NEXT_DAC_CTRL_STATE;
				TMP := "00";
			else
				TMP := TMP + '1';
			end if;
		end if;
	end process DAC_CTRL_STATE_REGISTER;

	DAC_CTRL_STATE_DECODER: process (DAC_CTRL_STATE, SHIFT_READY, DAC_SH_CLK)
	begin
		SHIFT_EN <= false; -- Default.
		case DAC_CTRL_STATE is
			when IDLE =>
				NEXT_DAC_CTRL_STATE <= LOAD_L;
			when LOAD_L =>
				-- Start when the shift clock is not active.
				if DAC_SH_CLK = '0' then
					NEXT_DAC_CTRL_STATE <= SHIFT_L;
				else
					NEXT_DAC_CTRL_STATE <= LOAD_L;
				end if;
			when SHIFT_L =>
				if SHIFT_READY = true then
					NEXT_DAC_CTRL_STATE <= LOAD_R;
				else
					NEXT_DAC_CTRL_STATE <= SHIFT_L;
				end if;
				SHIFT_EN <= true;
			when LOAD_R =>
				-- Start when the shift clock is not active.
				if DAC_SH_CLK = '0' then
					NEXT_DAC_CTRL_STATE <= SHIFT_R;
				else
					NEXT_DAC_CTRL_STATE <= LOAD_R;
				end if;
			when SHIFT_R =>
				if SHIFT_READY = true then
					NEXT_DAC_CTRL_STATE <= LOAD_DAC;
				else
					NEXT_DAC_CTRL_STATE <= SHIFT_R;
				end if;
				SHIFT_EN <= true;
			when LOAD_DAC =>
				NEXT_DAC_CTRL_STATE <= LOAD_L;
		end case;
	end process DAC_CTRL_STATE_DECODER;
end architecture BEHAVIOR;
