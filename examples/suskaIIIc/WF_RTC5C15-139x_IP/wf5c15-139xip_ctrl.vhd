----------------------------------------------------------------------
----                                                              ----
---- ATARI Real Time Clock (RTC) interface.  		              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- Interface to connect a DS1392 or DS1393 SPI timekeeper chip  ----
---- to the Atari IP core. The interface is on the system side    ----
---- compatible with the original used RP5C15 chip.               ----
----                                                              ----
---- This files is the control state machine between the RTC's    ----
---- SPI interface and and the registers.                         ----
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
---- Copyright (C) 2007 - 2008 Wolfgang Foerster                  ----
----                                                              ----
---- This source file may be used and distributed without         ----
---- restriction provided that this copyright statement is not    ----
---- removed from the file and that any derivative work contains  ----
---- the original copyright notice and the associated             ----
---- diSPI_SCLaimer.                                              ----
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
-- Revision 2K7A  2007/01/05 WF
-- Initial Release.
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- Revision 2K9A  2009/06/20 WF
--   Process BITCNT has now synchronous reset to meet preset requirements.
--   SPI_ENn has now synchronous reset to meet preset requirement.
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF5C15_139xIP_CTRL is
	port(
		CLK				: in bit;
		RESETn			: in bit;
		SPI_PENDING		: in bit_vector(10 downto 0);
		SPI_STORE		: out bit;
		SPI_DATASEL		: out bit_vector(3 downto 0);
		SPI_DATA_IN		: in std_logic_vector(7 downto 0);
		SPI_DATA_OUT	: out std_logic_vector(7 downto 0);

		DATA_VALID		: in bit;

		-- SPI interface:
		SPI_IN			: in bit;
		SPI_OUT			: out bit;
		SPI_ENn			: out bit;
		SPI_SCL			: out bit;
		SPI_CSn			: out bit
	);
end WF5C15_139xIP_CTRL;
architecture BEHAVIOR of WF5C15_139xIP_CTRL is
type CTRL_STATES is (TEST_PENDING, SPI_WR, STORE, SPI_RD, UPDT_PND);
signal CTRL_STATE		: CTRL_STATES;
signal NEXT_CTRL_STATE	: CTRL_STATES;
signal SPI_DATASELECT	: bit_vector(3 downto 0);
signal REG_PNT			: integer range 0 to 10;
signal SPI_RDY			: bit;
signal SPI_TX			: bit_vector(16 downto 0);
signal SPI_RX			: bit_vector(7 downto 0);
signal SPI_SCLK			: bit;
signal PHASE_75			: bit;
signal BIT_CNT 			: std_logic_vector(4 downto 0);
begin
	------------------------------- Control Section ----------------------------------	
	REG_POINTER:
	process(RESETn, CLK)
	-- The REG_PNT is a pointer which indicates a register to be read or written.
	-- This pointer is incremented after a SPI read or write operation. This mechanism
	-- is used for setting up the correct write address to the SPI interface and the
	-- correct read address for reading out registers to the SPI interface. Additionally
	-- it is used to check the pending register in the register file for necessity of
	-- write data to the SPI. It is important, that the REG_PNT, the process P_PENDING
	-- in the register file and the SPI_DATASELECT correlate to handle the appropriate
	-- registers.
	begin
		if RESETn = '0' then
			REG_PNT <= 0;
		elsif CLK = '1' and CLK' event then
			if CTRL_STATE = STORE or CTRL_STATE = UPDT_PND then
				if REG_PNT < 10 then
					REG_PNT <= REG_PNT + 1;
				else
					REG_PNT <= 0;
				end if;
			end if;
		end if;
	end process REG_POINTER;

	with REG_PNT select
		SPI_DATASELECT <= 	x"1" when 0, -- Seconds.
						x"2" when 1, -- Minutes.
						x"3" when 2, -- Hours.
						x"4" when 3, -- Day.
						x"5" when 4, -- Date.
						x"6" when 5, -- Month.
						x"7" when 6, -- Year.
						x"A" when 7, -- Alarm minutes.
						x"B" when 8, -- Alarm hours.
						x"C" when 9, -- Alarm day/date.
						x"D" when 10, -- Control.
						x"0" when others;

	SPI_DATASEL <= SPI_DATASELECT;
	SPI_STORE <= '1' when CTRL_STATE = STORE else '0';

	CTRL_REG: process(RESETn, CLK)
	begin
		if RESETn = '0' then
			CTRL_STATE <= TEST_PENDING;
		elsif CLK = '1' and CLK' event then
			CTRL_STATE <= NEXT_CTRL_STATE;
		end if;
	end process CTRL_REG;

	CTRL_DEC: process(CTRL_STATE, SPI_PENDING, DATA_VALID, REG_PNT, SPI_RDY)
	-- The control decoder works together with the SPI decoder in a way,
	-- that one data is read or written from or to the SPI interface.
	-- No multiple byte mode of the SPI is required/supported.
	begin
		case CTRL_STATE is
			when TEST_PENDING =>
				if SPI_PENDING(REG_PNT) = '1' and DATA_VALID = '1' then
					NEXT_CTRL_STATE <= SPI_WR;
				else
					NEXT_CTRL_STATE <= SPI_RD;
				end if;
			when SPI_WR =>
				if SPI_RDY = '1' then
					NEXT_CTRL_STATE <= STORE;
				else
					NEXT_CTRL_STATE <= SPI_WR; -- Wait.
				end if;
			when STORE =>
				NEXT_CTRL_STATE <= TEST_PENDING;
			when SPI_RD =>
				if SPI_RDY = '1' then
					NEXT_CTRL_STATE <= UPDT_PND;
				else
					NEXT_CTRL_STATE <= SPI_RD; -- Wait.
				end if;
			when UPDT_PND =>
				NEXT_CTRL_STATE <= TEST_PENDING;
		end case;
	end process CTRL_DEC;

	------------------------------- SPI Section ----------------------------------	
	SPI_CLOCK: process(CLK, RESETn)
	-- This process generates SPI_SCL with a sixteenth of the clock frequency.
	-- This results in 500kHz for 8MHz CPU clock and 1MHz for 16MHz CPU clock.
	-- The PHASE_75 indicates, that a high period or a low period of the SPI_SCLK
	-- has passed 75%.
	variable CLK_COUNT: std_logic_vector(3 downto 0);
	begin
		if RESETn = '0' then
			CLK_COUNT := x"0";
		elsif CLK = '1' and CLK' event then
			if CTRL_STATE = SPI_WR or CTRL_STATE = SPI_RD then
				CLK_COUNT := CLK_COUNT + '1';
			else
				CLK_COUNT := x"0";			
			end if;
		end if;
		SPI_SCLK <= To_Bit(CLK_COUNT(3)); 	-- 1/16 of CLK.
		PHASE_75 <= To_Bit(CLK_COUNT(2)) and To_Bit(CLK_COUNT(1));
	end process SPI_CLOCK;

	SPI_SCL <= SPI_SCLK;
	SPI_CSn <= '0' when CTRL_STATE = SPI_WR or CTRL_STATE = SPI_RD else '1';

	BITCNT: process(CLK, BIT_CNT, PHASE_75)
	-- This process provides information about the already transmitted or received
	-- SPI bits.
	variable LOCK	: boolean;
	begin
		if CLK = '1' and CLK' event then
			if RESETn = '0' then
				BIT_CNT <= "00000";
				LOCK := true;
			elsif CTRL_STATE = SPI_WR and SPI_SCLK = '0' and LOCK = false then
				LOCK := true;
				BIT_CNT <= BIT_CNT + '1';
			elsif CTRL_STATE = SPI_RD and SPI_SCLK = '0' and LOCK = false then
				LOCK := true;
				BIT_CNT <= BIT_CNT + '1';
			elsif CTRL_STATE /= SPI_WR and CTRL_STATE /= SPI_RD then
				BIT_CNT <= "00000";
				LOCK := true;
			elsif SPI_SCLK = '1' then
				LOCK := false;
			end if;
		end if;
		--
		-- Break during the 16th SPI_SCL pulse but wait
		-- until the negative SPI_SCL edge occured:
		if BIT_CNT = "10000" and PHASE_75 = '1' then
			SPI_RDY <= '1';
		else
			SPI_RDY <= '0';
		end if;
	end process BITCNT;

	TX_SHIFT: process(RESETn, CLK, SPI_TX)
	-- Be aware, that the transmitter sends in read or write mode.
	-- The transmitter sends in any case 16 bits which are masked
	-- during read access by the SPI_ENn signal.
	variable LOCK	: boolean;
	begin
		if RESETn = '0' then
			SPI_TX <= (others => '0');
			LOCK := false;
		elsif CLK = '1' and CLK' event then
			if CTRL_STATE /= SPI_WR and CTRL_STATE /= SPI_RD then
				-- The transmitted data format is as follows:
				-- MSB is '0' due to the shift mechanism (one dummy bit is shifted first).
				-- x"8" is the write address high nibble.
				-- SPI_DATASELECT is the write address low nibble.
				-- SPI_IN is the data to be transmitted.
				SPI_TX <=  '0' & x"8" & SPI_DATASELECT & To_BitVector(SPI_DATA_IN);
				LOCK := false;
			elsif CTRL_STATE = SPI_WR or CTRL_STATE = SPI_RD then
				-- The shift register operates on the falling edge of SPI_SCLK
				-- due to the sampling of the SPI device on the negative edge of SPI_SCLK.
				if SPI_SCLK = '1' and LOCK = false then -- Shift out on the positive clock edge.
					LOCK := true;
					SPI_TX <= SPI_TX(15 downto 0) & '0'; -- Shift left, MSB first.
				elsif SPI_SCLK = '0' then
					LOCK := false;
				end if;
			end if;
		end if;
		--
		SPI_OUT <= 	SPI_TX(16);
	end process TX_SHIFT;

	SPI_DATA_EN: process
	-- To control the transmitter data enable during write or read
	-- SPI data, several different enable/disable conditions are
	-- required ...
	begin
		wait until CLK = '1' and CLK' event;
		if RESETn = '0' then
			SPI_ENn <= '1';
		elsif CTRL_STATE /= SPI_WR and CTRL_STATE /= SPI_RD then
			SPI_ENn <= '1'; -- Disable during non read and non write cycles.
		elsif CTRL_STATE = SPI_RD and BIT_CNT = "01000" and PHASE_75 = '1' then
			SPI_ENn <= '1'; -- Disable in the second half of the read cycle.
		elsif CTRL_STATE = SPI_RD and BIT_CNT < "01000" then
			SPI_ENn <= '0'; -- Enable in the first half of the read cycle.
		elsif CTRL_STATE = SPI_WR then
			SPI_ENn <= '0'; -- Enable during the write cycle.
		end if;
	end process SPI_DATA_EN;
		
	RX_SHIFT: process(RESETn, CLK)
	-- The receiver shift register is only 8 bit wide. This is sufficient
	-- for the data not required is shifted through the register. the last
	-- 8 bits are the interesting data. These are stored in the receiver
	-- shift register until the next read cycle starts.
	variable LOCK	: boolean;
	begin
		if RESETn = '0' then
			SPI_RX <= x"00";
			LOCK := false;
		elsif CLK = '1' and CLK' event then
			if CTRL_STATE /= SPI_RD then
				LOCK := false;
			elsif CTRL_STATE = SPI_RD then
				if SPI_SCLK = '0' and LOCK = false then -- Sampling on the negative clock edge.
					LOCK := true; -- Operate on rising edge of DAC_SH_CLK.
					SPI_RX <= SPI_RX(6 downto 0) & SPI_IN; -- Shift left, MSB first.
				elsif SPI_SCLK = '1' then
					LOCK := false;
				end if;
			end if;
		end if;
	end process RX_SHIFT;

	SPI_DATA_OUT <= To_StdLogicVector(SPI_RX);
end architecture BEHAVIOR;
