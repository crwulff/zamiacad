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
---- The DMA sound is a feature of the STE series. It is          ----
---- originally implemented in the memory controller unit (MCU).  ----
---- Therefore the DMA sound module is also implemented in this   ----
---- MCU core.                                                    ----
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
-- Revision 2K7A  2007/01/02 WF
--   Changes to the clock system and related
--   hardware as sound or video control.
-- Revision 2K8A  2008/07/14 WF
--    Minor changes.

use work.wf25912ip_pkg.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25912IP_DMA_SOUND_SD is
port (  RESETn			: in bit;
		CLK				: in bit;
		
		RWn				: in bit;
		DATA_IN			: in std_logic_vector(7 downto 0);
		DATA_OUT		: out bit_vector(7 downto 0);
		DATA_EN			: out bit;

		MONOMONn		: in bit; -- Monochrome monitor detect.

		DE				: in bit;				-- Horizontal sync.
		MCU_PHASE		: in MCU_PHASE_TYPE;	-- MCU phase (s. control).
		SINTn			: out bit;				-- Interrupt flag.
		SINT_TAI		: out bit; 				-- Interrupt filtered for timer A.
		SINT_IO7		: out bit; 				-- Interrupt XORed for MFP_IO7
		FRAME_CNT_EN	: in bit;				-- Register access enable.
		SREQ			: in bit;				-- SHIFTER sound data request.
		SOUND_REQ		: out boolean;			-- DMA control flag.
		
		SOUND_CTRL_CS				: in bit;
		SOUND_FRAME_START_HI_CS		: in bit;
		SOUND_FRAME_START_MID_CS	: in bit;
		SOUND_FRAME_START_LOW_CS	: in bit;
		SOUND_FRAME_ADR_HI_CS		: in bit;
		SOUND_FRAME_ADR_MID_CS		: in bit;
		SOUND_FRAME_ADR_LOW_CS		: in bit;
		SOUND_FRAME_END_HI_CS		: in bit;
		SOUND_FRAME_END_MID_CS		: in bit;
		SOUND_FRAME_END_LOW_CS		: in bit;
		
		DMA_SOUND_ADR				: out bit_vector(23 downto 1)
      );
end WF25912IP_DMA_SOUND_SD;

architecture BEHAVIOR of WF25912IP_DMA_SOUND_SD is
signal SOUND_CONTROL		: std_logic_vector(7 downto 0);
signal SOUND_FRAME_START	: std_logic_vector(23 downto 0);
signal FRAME_START_BUFFER	: std_logic_vector(23 downto 0);
signal SOUND_FRAME_ADR		: std_logic_vector(23 downto 0);
signal SOUND_FRAME_END		: std_logic_vector(23 downto 0);
signal FRAME_END_BUFFER		: std_logic_vector(23 downto 0);
signal SINT_In				: bit;
signal DMA_OFF				: boolean;
signal STREAMING			: boolean;
signal FRAME_REPEAT			: boolean;
signal SOUND_ACTIVE			: boolean;
begin
	SINTn <= SINT_In;
	SINT_In <= '0' when STREAMING = true else '1';
	SINT_IO7 <= not SINT_In xor MONOMONn;
	DMA_OFF	<= true	when SOUND_CONTROL(0) = '0' else false;
	FRAME_REPEAT <= true when SOUND_CONTROL(1) = '1' else false;
	SOUND_ACTIVE <= true when DMA_OFF <= false and FRAME_REPEAT = true else
					true when DMA_OFF <= false and FRAME_REPEAT = false 
							and SOUND_FRAME_ADR < FRAME_END_BUFFER else false;

	P_SINT: process(RESETn, CLK)
	-- This process provides a filter for the SINTn signal. In the original
	-- machine there were 8 shift stages working on a 2MHz clock. Here 64 stages
	-- with 16MHz are used.
	variable TMP : bit_vector(63 downto 0);
	begin
		if RESETn = '0' then
			TMP := (others => '0');
		elsif CLK = '1' and CLK' event then
			if SINT_In = '1' then
				TMP := (others => '0');
			else
				TMP := TMP(62 downto 0) & '1'; -- Left shift.
			end if;
		end if;
		SINT_TAI <= TMP(63);
	end process P_SINT;

	SOUND_STATUS: process(RESETn, CLK)
	-- This process is responsible, that per horizontal sync.
	-- exactly one DMA sound sample is transfered to the SHIFTER.
	variable HSYNC_LOCK : boolean;
	begin
		if RESETn = '0' then
			SOUND_REQ <= false; -- DMA sound off.
			HSYNC_LOCK := false;
		elsif CLK = '1' and CLK' event then
			if DE = '0' and HSYNC_LOCK = false then
				HSYNC_LOCK := true;
				if 	SREQ = '1' and SOUND_ACTIVE = true then
					SOUND_REQ <= true;
				end if;
			elsif MCU_PHASE = SOUND then
				SOUND_REQ <= false;
			elsif DE = '1' then
				HSYNC_LOCK := false;
			end if;
		end if;
	end process SOUND_STATUS;

	SOUND_REGS: process(RESETn, CLK)
	-- This process contains the DMA sound relevant registers.
	begin
		if RESETn = '0' then
			SOUND_CONTROL 		<= (others => '0');
			SOUND_FRAME_START 	<= (others => '0');
			SOUND_FRAME_ADR 	<= (others => '0');
			SOUND_FRAME_END 	<= (others => '0');
			FRAME_START_BUFFER	<= (others => '0');
			FRAME_END_BUFFER	<= (others => '0');
		elsif CLK = '1' and CLK' event then
			-- Write to registers; SOUND_FRAME_ADR is read only.
			if SOUND_CTRL_CS = '1' and RWn = '0' then
				SOUND_CONTROL <= DATA_IN;
			elsif SOUND_FRAME_START_HI_CS = '1' and RWn = '0' then
				SOUND_FRAME_START(23 downto 16) <= DATA_IN;
			elsif SOUND_FRAME_START_MID_CS = '1' and RWn = '0' then
				SOUND_FRAME_START(15 downto 8) <= DATA_IN;
			elsif SOUND_FRAME_START_LOW_CS = '1' and RWn = '0' then
				SOUND_FRAME_START(7 downto 0) <= DATA_IN;
			elsif SOUND_FRAME_END_HI_CS = '1' and RWn = '0' then
				SOUND_FRAME_END(23 downto 16) <= DATA_IN;
			elsif SOUND_FRAME_END_MID_CS = '1' and RWn = '0' then
				SOUND_FRAME_END(15 downto 8) <= DATA_IN;
			elsif SOUND_FRAME_END_LOW_CS = '1' and RWn = '0' then
				SOUND_FRAME_END(7 downto 0) <= DATA_IN;
			end if;
			
			if STREAMING = false then
				FRAME_START_BUFFER <= SOUND_FRAME_START;
				FRAME_END_BUFFER <= SOUND_FRAME_END;
			end if;
			
			if DMA_OFF = true then -- Switched off.
				SOUND_FRAME_ADR <= FRAME_START_BUFFER;
				STREAMING <= false;
			elsif SOUND_FRAME_ADR < FRAME_END_BUFFER then
				if FRAME_CNT_EN = '1' then
					SOUND_FRAME_ADR <= SOUND_FRAME_ADR + '1'; -- Count.
				end if;
				STREAMING <= true;
			else -- Frame end -> reload start buffer.
				if FRAME_CNT_EN = '1' then
					SOUND_FRAME_ADR <= FRAME_START_BUFFER;
				end if;
				STREAMING <= false;
			end if;
		end if;
	end process SOUND_REGS;

	-- Read registers:
    -- No output for SOUND_CTRL because it is a shadow register of the shifter DMA sound module.
	DATA_OUT <=	To_BitVector(SOUND_FRAME_START(23 downto 16)) when SOUND_FRAME_START_HI_CS = '1' and RWn = '1' else
				To_BitVector(SOUND_FRAME_START(15 downto 8)) when SOUND_FRAME_START_MID_CS = '1' and RWn = '1' else
				To_BitVector(SOUND_FRAME_START(7 downto 0)) when SOUND_FRAME_START_LOW_CS = '1' and RWn = '1' else
				To_BitVector(SOUND_FRAME_ADR(23 downto 16)) when SOUND_FRAME_ADR_HI_CS = '1' and RWn = '1' else
				To_BitVector(SOUND_FRAME_ADR(15 downto 8)) when SOUND_FRAME_ADR_MID_CS = '1' and RWn = '1' else
				To_BitVector(SOUND_FRAME_ADR(7 downto 0)) when SOUND_FRAME_ADR_LOW_CS = '1' and RWn = '1' else
				To_BitVector(SOUND_FRAME_END(23 downto 16)) when SOUND_FRAME_END_HI_CS = '1' and RWn = '1' else
				To_BitVector(SOUND_FRAME_END(15 downto 8)) when SOUND_FRAME_END_MID_CS = '1' and RWn = '1' else
				To_BitVector(SOUND_FRAME_END(7 downto 0)) when SOUND_FRAME_END_LOW_CS = '1' and RWn = '1' else (others => '0');

	DATA_EN <= 	(SOUND_FRAME_START_HI_CS or SOUND_FRAME_START_MID_CS or
				SOUND_FRAME_START_LOW_CS or SOUND_FRAME_ADR_HI_CS or SOUND_FRAME_ADR_MID_CS or
				SOUND_FRAME_ADR_LOW_CS or SOUND_FRAME_END_HI_CS or SOUND_FRAME_END_MID_CS or
				SOUND_FRAME_END_LOW_CS) and RWn;
	
	DMA_SOUND_ADR <= To_BitVector(SOUND_FRAME_ADR(23 downto 1));
end architecture BEHAVIOR;
