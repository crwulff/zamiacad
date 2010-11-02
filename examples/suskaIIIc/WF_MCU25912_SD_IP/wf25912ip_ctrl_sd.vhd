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
---- This controller meets the requirements for SD-RAMs. See the  ----
---- top level file for more information. This controller is in   ----
---- comparision to the original ATARI controller enhanced by the ----
---- 8192KWords SD-RAM size. This is selected for the respective  ----
---- memory bank by the bits 3 downto 0 of the memory config      ----
---- register. See the coding for the BANK0_TYPE, the BANK_SWITCH ----
---- and the DTACKn in this file for more information.            ----
----                                                              ----
---- Cotrol file for the different MCU units like registers       ----
---- multiplexers, refresh counter DMA counter sound module etc.  ----
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
--   Modified the D-RAM control scheme to work with SD-RAM.
--   Introduced the 8MB SD-RAM size to the memory config register (effective 7MB).
--   Introduced the 8MB SD-RAM size to DTACKn and the BANK0_TYPE (effective 7MB).
--   Modified video timing (DCYCn) to meet the requirements for the ip core.
-- Revision 2K8B  2008/12/24 WF
--   Introduced VIDEO_HIMODE.
--   Minor changes concerning DMA_SYNC.
-- Revision 2K9A  2008/12/24 WF
--   Introduced multisync compatibility modes (s. P_LINEDOUBLING).
--   Introduced clock phase synchronization in the process TIME_SLICES.
--   LATCHn is now enable during RAM access and not during VIDEO and SOUND.
--

use work.wf25912ip_pkg.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use ieee.std_logic_arith.all;

entity WF25912IP_CTRL_SD is
	port (  CLK				: in bit;
			RESETn			: in bit;

			LDSn, UDSn, RWn	: in bit; -- Bus control signals.

			M_ADR			: in bit_vector(23 downto 1); -- Non multiplexed DRAM addresses.
			
			CMPCS_REQ		: in bit; 	-- Request for the shifter register access.
			CMPCSn			: out bit; 	-- Control for the shifter register access.

			SOUND_REQ		: in boolean;
			FRAME_CNT_EN	: out bit; -- Count enable for the sound DMA address counter.
			SLOADn			: out bit;
			
			RAMn			: in bit; -- RAM access control.
			DMAn			: in bit; -- DMA access control.
			DMA_SYNC		: out bit; -- Synchronizes the GLUE DMA part with the MCU.

			MEM_CONFIG_CS	: in bit; -- Memory config register control.
			MCU_PHASE		: out MCU_PHASE_TYPE;
			
			VSYNCn			: in bit; -- Vertival sync signal.
			DE				: in bit; -- Horizontal or vertical data enable.
            VIDEO_HIMODE    : in bit; -- Access the video RAM with double speed.
			DCYCn			: out bit; -- Shifter load signal.
					
			RAS0n			: out bit; -- Memory bank 1 row address strobe.
			CAS0Hn			: out bit; -- Memory bank 1 column address strobe.
			CAS0Ln			: out bit; -- Memory bank 1 column address strobe.

			WEn				: out bit; -- Memory write control, low active.

			RAS1n			: out bit; -- Memory bank 2 row address strobe.
			CAS1Hn			: out bit; -- Memory bank 2 column address strobe.
			CAS1Ln			: out bit; -- Memory bank 2 column address strobe.

			RDATn			: out bit; -- Buffer control.
			WDATn			: out bit; -- Buffer control.
			LATCHn			: out bit; -- Buffer control.
			
			REF_EN			: out bit; -- Refresh counter enable.
			DMA_CNT_EN		: out bit; -- DMA control.
			VIDEO_CNT_EN	: out bit; -- Video control.
			VIDEO_CNT_LOAD	: out bit; -- Video control.
			LINE80_RELOAD	: out bit; -- Video control for multisync compatible mode.
			
			MADRSEL			: out MADR_TYPE; -- Address multiplexer control.

			DTACKn			: out bit; -- Data acknowledge signal.

			DATA_IN			: in std_logic_vector(7 downto 0);
			DATA_OUT		: out bit_vector(7 downto 0);
			DATA_EN			: out bit
	);
end WF25912IP_CTRL_SD;

architecture BEHAVIOR of WF25912IP_CTRL_SD is
type BANKS is (BANK1, BANK0);
type MATRIX_ELEMENTS is array (1 to 14, 1 to 8) of bit;
constant TIME_MATRIX : MATRIX_ELEMENTS := 
	(('0','1','1','1','0','1','1','1'),		-- RASn.
	 ('1','0','0','1','1','0','0','1'),		-- CASHn.
	 ('1','0','0','1','1','0','0','1'),		-- CASLn.
	 ('1','0','0','1','1','1','1','1'),		-- WEn.
     ('0','0','0','0','1','1','1','1'),		-- RDATn.
     ('0','0','0','0','1','1','1','1'),		-- WDATn.
	 ('0','0','1','1','0','0','0','0'),		-- LATCHn.
	 ('1','0','0','0','1','1','1','1'),		-- CMPCSn.
	 --('1','1','1','1','0','0','0','1'),	-- DCYCn, SLOADn (video and sound data), original chip timing.
	 ('1','1','1','1','1','1','1','0'),		-- DCYCn, SLOADn (video and sound data).
	 ('1','0','1','0','0','0','0','0'),		-- REF_EN.
	 ('0','0','0','1','0','0','0','0'),		-- DMA_CNT_EN.
	 ('0','0','0','0','0','0','0','1'),		-- VIDEO_CNT_EN, FRAME_CNT_EN (video and sound).
     ('1','0','0','0','1','1','1','1'),		-- DTACK_MASKn.
	 ('1','0','0','0','1','0','0','0'));	-- MADRSEL.

signal MCU_PHASE_I			: MCU_PHASE_TYPE;
signal BANK0_TYPE 			: BANKTYPE; -- Memory type indicator.
signal MADRSEL_I			: bit; -- Control signal for the high low data multiplexer.
signal MEMCONFIG			: std_logic_vector(7 downto 0);
signal SLICE_NUMBER			: integer range 1 to 8;
signal BANK_SWITCH			: BANKS;
signal M_ADR_I 				: bit_vector(23 downto 0);

signal RASn					: bit;
signal CASLn				: bit; 
signal CASHn				: bit;

signal DTACK_MASK_In 		: bit;

signal CMPCS_In				: bit;
signal DCYC_In				: bit;

signal VIDEO_CNT_EN_I		: bit;
begin

	MEMCONFIG_REG: process
	begin
		-- The MEMCONFIG must start up with x"0A" indicating virtual 4MB RAM.
		-- Otherwise the RAM test routine will hang due to no DTACKn signal.
		-- The value of x"0A" is written immediately after system startup by
		-- the CPU. This register may not have a clear via RESETn because the
		-- operating system does not initialise it after an interrupt like
		-- monochrome detect (level 15).
		wait until CLK = '1' and CLK' event;
		if MEM_CONFIG_CS = '1' and RWn = '0' then
			MEMCONFIG <= DATA_IN; -- Write to register.
		end if;
	end process MEMCONFIG_REG;

	-- Read memory config register:
	DATA_OUT <= To_BitVector(MEMCONFIG) when MEM_CONFIG_CS = '1' and RWn = '1' else (others => '0');
	DATA_EN <= MEM_CONFIG_CS and RWn;

	-- Address control:
	MADRSEL <= MEM_LOW_ADR when MADRSEL_I = '1' else MEM_HI_ADR;
	M_ADR_I <= M_ADR & '0';

    -- The working principle of DMA_SYNC is explained in the wf25915ip_bus_arbiter_V1 file.
    DMA_SYNC <= '1' when SLICE_NUMBER = 1 and CLK = '1' else '0';
	MCU_PHASE <= MCU_PHASE_I;

	-- Bits 3 and 2 of the MEMCONFIG select the bank 0 and the bits
	-- 1 and 0 are for the selection of bank 1.
	with MEMCONFIG(3 downto 2) select
		BANK0_TYPE <= K8192 when "11",
					  K2048 when "10",
					  K512  when "01",
					  K128 when others;

	-- SD-RAM control signals:
	RAS0n <= RASn when BANK_SWITCH = BANK0 else '1';
	RAS1n <= RASn when BANK_SWITCH = BANK1 else '1';
	CAS0Ln <= CASLn when BANK_SWITCH = BANK0 else '1';
	CAS1Ln <= CASLn when BANK_SWITCH = BANK1 else '1';
	CAS0Hn <= CASHn when BANK_SWITCH = BANK0 else '1';
	CAS1Hn <= CASHn when BANK_SWITCH = BANK1 else '1';

	-- Shifter stuff (sound and video, DMA).
	VIDEO_CNT_LOAD <= '1' when VSYNCn = '0' else '0';
    -- The following two lines are a relict of the original chipsets and are now obsolete due to the
    -- RESETn pin of the SHIFTER.
    --	CMPCSn 	<= 	'0'	when CMPCS_In = '0' or RESETn = '0' else '1'; -- Reset of the original ST MMU.
    --	DCYCn 	<= 	'0' when DCYC_In = '0' 	or RESETn = '0' else '1'; -- Reset of the original ST MMU.
	CMPCSn 	<= 	'0'	when CMPCS_In = '0' else '1';
	DCYCn 	<= 	'0' when DCYC_In = '0' 	else '1';

	SLOADn 	<= DCYC_In;

	-- Bus, memory and system control signals:
	RASn	<= TIME_MATRIX(1, SLICE_NUMBER) when MCU_PHASE_I = REFRESH or MCU_PHASE_I = RAM else
			   TIME_MATRIX(1, SLICE_NUMBER) when MCU_PHASE_I = VIDEO or MCU_PHASE_I = SOUND else '1';
	CASHn 	<= TIME_MATRIX(2, SLICE_NUMBER) when MCU_PHASE_I = RAM and UDSn = '0' else
			   TIME_MATRIX(2, SLICE_NUMBER) when MCU_PHASE_I = VIDEO or MCU_PHASE_I = SOUND else '1';
	CASLn	<= TIME_MATRIX(3, SLICE_NUMBER) when MCU_PHASE_I = RAM and LDSn = '0' else
			   TIME_MATRIX(3, SLICE_NUMBER) when MCU_PHASE_I = VIDEO or MCU_PHASE_I = SOUND else '1';
	WEn		<= TIME_MATRIX(4, SLICE_NUMBER) when MCU_PHASE_I = RAM and RWn = '0' else '1'; -- Write is valid in RAM mode.

	RDATn	<= TIME_MATRIX(5, SLICE_NUMBER) when (MCU_PHASE_I = RAM or MCU_PHASE_I = SHIFTER) and RWn = '1' else '1'; -- Read to bus only in RAM mode.
	WDATn	<= TIME_MATRIX(6, SLICE_NUMBER) when (MCU_PHASE_I = RAM or MCU_PHASE_I = SHIFTER) and RWn = '0' else '1';
    LATCHn	<= TIME_MATRIX(7, SLICE_NUMBER) when (MCU_PHASE_I = RAM or MCU_PHASE_I = SHIFTER) else '1';

	CMPCS_In	<= TIME_MATRIX(8, SLICE_NUMBER) when MCU_PHASE_I = SHIFTER else '1';
	DCYC_In		<= TIME_MATRIX(9, SLICE_NUMBER) when MCU_PHASE_I = VIDEO else '1';

	REF_EN		<= TIME_MATRIX(10, SLICE_NUMBER) when MCU_PHASE_I = REFRESH else '0';
	
	DMA_CNT_EN		<= TIME_MATRIX(11, SLICE_NUMBER) when MCU_PHASE_I = RAM and DMAn = '0' else '0'; -- DMA access.
	FRAME_CNT_EN	<= TIME_MATRIX(12, SLICE_NUMBER) when MCU_PHASE_I = SOUND else '0'; -- Sound data.
	VIDEO_CNT_EN_I 	<= TIME_MATRIX(12, SLICE_NUMBER) when MCU_PHASE_I = VIDEO else '0'; -- Video data.

	VIDEO_CNT_EN <= VIDEO_CNT_EN_I;

	P_LINEDOUBLING: process
	-- This logic controls the video counter in the multisync colour modes.
	-- In the STs 640x200 mid colour resolution, the line consists of 80 words 
	-- with 2 words per 16 pixels means 4 colours per pixel. In this mode,
	-- every line is written twice to get 400 lines.
	-- In the STs 320x200 low colour resolution, the line consists of 80 words 
	-- with 4 words per 16 pixels means 16 colours per pixel.
	variable SECOND_LINE	: bit;
	variable VIDEO_WORD_CNT	: std_logic_vector(6 downto 0);
	begin
		wait until CLK = '1' and CLK' event;
		--
		if VSYNCn = '0' or VIDEO_HIMODE = '0' then
			SECOND_LINE := '0';
			VIDEO_WORD_CNT := "0000000";
		elsif VIDEO_CNT_EN_I = '1' and VIDEO_WORD_CNT < "1001111" then
			VIDEO_WORD_CNT := VIDEO_WORD_CNT + '1';
		elsif VIDEO_CNT_EN_I = '1' and SECOND_LINE = '0' then
			VIDEO_WORD_CNT := "0000000";
			SECOND_LINE := '1';
		elsif VIDEO_CNT_EN_I = '1' then
			VIDEO_WORD_CNT := "0000000";
			SECOND_LINE := '0';
		end if;
		--
		if VIDEO_WORD_CNT = "1001111" and SECOND_LINE = '0' then
			LINE80_RELOAD <= '1';
		else
			LINE80_RELOAD <= '0';
		end if;
	end process P_LINEDOUBLING;

    DTACK_MASK_In	<= TIME_MATRIX(13, SLICE_NUMBER) when MCU_PHASE_I = RAM or MCU_PHASE_I = SHIFTER else '1'; -- RAM, SHIFTER register access.
	MADRSEL_I 		<= TIME_MATRIX(14, SLICE_NUMBER);

	-- RAM bank select logic. It is dependant on the equiped memory.
	-- The BANK0 is the lower one in the ATARI's address space.
	BANK_SWITCH <= 
				-- 128Kwords in bank 0:
				BANK0 when BANK0_TYPE = K128  and M_ADR_I < x"020000" else
				-- 512Kwords in bank 0:
				BANK0 when BANK0_TYPE = K512  and M_ADR_I < x"080000" else
				-- 2048Kwords in bank 0:
				BANK0 when BANK0_TYPE = K2048 and M_ADR_I < x"200000" else
				-- 8192Kwords in bank 0:
				BANK0 when BANK0_TYPE = K8192 and M_ADR_I < x"700000" else BANK1; -- 7MB of 8MB  are useable.

	-- Data acknowledge logic:
	DTACKn <= -- The following conditions are valid for RAM and DMA mode: 
			'0' when MEMCONFIG(3 downto 0) = x"0" and -- 128K in bank 0 and 128K in bank 1.
		 			 M_ADR_I <= x"03FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"1" and -- 128K in bank 0 and 512K in bank 1.
		 			 M_ADR_I <= x"09FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"2" and -- 128K in bank 0 and 2MB in bank 1.
		 			 M_ADR_I <= x"21FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"3" and -- 128K in bank 0 and 7MB in bank 1.
		 			 M_ADR_I <= x"71FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"4" and -- 512K in bank 0 and 128K in bank 1.
		 			 M_ADR_I <= x"09FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"5" and -- 512K in bank 0 and 512K in bank 1.
		 			 M_ADR_I <= x"0FFFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"6" and -- 512K in bank 0 and 2MB in bank 1.
		 			 M_ADR_I <= x"27FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"7" and -- 512K in bank 0 and 7MB in bank 1.
		 			 M_ADR_I <= x"77FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"8" and -- 2MB in bank 0 and 128K in bank 1.
		 			 M_ADR_I <= x"21FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"9" and -- 2MB in bank 0 and 512K in bank 1.
		 			 M_ADR_I <= x"27FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"A" and -- 2MB in bank 0 and 2MB in bank 1.
		 			 M_ADR_I <= x"3FFFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"B" and -- 2MB in bank 0 and 7MB in bank 1.
		 			 M_ADR_I <= x"8FFFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"C" and -- 7MB in bank 0 and 128K in bank 1.
		 			 M_ADR_I <= x"71FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"D" and -- 7MB in bank 0 and 512K in bank 1.
		 			 M_ADR_I <= x"77FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"E" and -- 7MB in bank 0 and 2MB in bank 1.
		 			 M_ADR_I <= x"8FFFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"F" and -- 7MB in bank 0 and 8MB in bank 1.
		 			 M_ADR_I <= x"DFFFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			-- And this one for SHIFTER access mode:
			-- That means HSCROLL register; SHIFTMODE register and PALETTE registers.
			'0' when MCU_PHASE_I = SHIFTER and DTACK_MASK_In = '0' else
			'0' when DMAn = '0' and DTACK_MASK_In = '0' else '1';

	MCU_PHASE_SWITCH: process(RESETn, CLK)
	-- AD MCU_PHASE_TYPES: SHIFTER is foreseen to  access the shifter
	-- registers; VIDEO transfers video data from RAM to shifter; RAM 
	-- is the CPU or DMA to RAM access.
	-- The REFRESH cycle is foreseen to hold the data in the SD-RAMs.
	-- This process controls the type of data transfer in the second period
	-- of the MCU cycle (250ns ... 500ns). While the first half of the MCU
	-- cycle is reserved for data transfer to the shifter, the second one
	-- shares data transfer between DMA, CPU and RAM and is foreseen for the 
	-- RAM REFRESH process and the data transfer to the shifter registers 
	-- (MCU_PHASE = SHIFTER).
    -- The TMP variable controls the video access every second phase in
	-- case of a fast clocked MCU (double clock rate than the original MCU)
	-- when the VIDEO_HIMODE is not active. The VIDEO_HIMODE enables double
	-- speed video access to provide video data for multisync monitors.
	variable TMP    : bit;
	begin
		if RESETn = '0' then
            MCU_PHASE_I <= REFRESH; -- REFRESH during reset keeps data alive.
			TMP := '0';
		elsif CLK = '1' and CLK' event then
			if SLICE_NUMBER = 4 then
                TMP := not TMP; -- To achieve 500ns period with a 16MHz clock.
				-- Pay attention here! The DMA sound transfer must happen
				-- Right after the falling edge of DE. Otherwise there might
				-- Occur synchronisation problems with trash video output.
				-- The DMA sound module must look itself for this correct
				-- timing taking the DE status into account.
                if DE = '1' and VIDEO_HIMODE = '1' then
                    MCU_PHASE_I <= VIDEO; -- Video data out for an originally clocked MCU.
                elsif DE = '1' and TMP = '1' then
                    MCU_PHASE_I <= VIDEO; -- Video data out for an originally clocked MCU.
                elsif DE = '0' and SOUND_REQ = true and TMP = '1' then
					MCU_PHASE_I <= SOUND; -- DMA sound data out.
				else
					MCU_PHASE_I <= IDLE; -- Do nothing, wait for one of the data cycles.
				end if;
			elsif SLICE_NUMBER = 8 then
                if RAMn = '0' then
					MCU_PHASE_I <= RAM;
				elsif CMPCS_REQ = '1' then
					MCU_PHASE_I <= SHIFTER;
				else -- REFRESH, if no data transfer is required.
					MCU_PHASE_I <= REFRESH;
				end if;
			end if;
		end if;
	end process MCU_PHASE_SWITCH;

	TIME_SLICES: process
	-- This counter may not have a reset control because it has to
	-- Produce refresh timing during reset. The process must operate
	-- on the positive clock edge to synchronize correctly with the
	-- GLUE DMA part.
    -- Important enhancement! The first access, the RAMn signal
	-- synchronizes the MCU clock with the phase of the CPU clock. 
	-- This is very important for correct system startup concerning
	-- the DTACKn detection in the CPU and it's waitstate generation
	-- A wrong clock phase terminates the CPU access too early 
	-- resulting in malfunction.
	variable TIME_SLICE_CNT : std_logic_vector(2 downto 0);
    variable LOCK   : boolean;
	begin
		wait until CLK = '1' and CLK' event;
        if RESETn = '0' then
            LOCK := false;
        elsif RAMn = '0' and LOCK = false then -- Sync once!
            LOCK := true;
            TIME_SLICE_CNT := "111";
        else
            TIME_SLICE_CNT := TIME_SLICE_CNT + '1';
            SLICE_NUMBER <= conv_integer(TIME_SLICE_CNT) + 1;
        end if;
	end process TIME_SLICES;
end architecture BEHAVIOR;
