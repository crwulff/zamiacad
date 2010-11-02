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
--   Initial Release.
-- Revision 2K7A  2007/01/02 WF
--   Changes to the clock system and related
--   hardware as sound or video control.
-- 

use work.wf25912ip_pkg.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25912IP_CTRL is
	port (  CLK_x2			: in bit;
			CLK_x1			: in bit; -- Clock (strobe) for the SLICES_SYNC process.
			CLKSEL			: in CLKSEL_TYPE;
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

			MEM_CONFIG_CS	: in bit; -- Memory config register control.
			BANK0_TYPE 		: buffer BANKTYPE; -- Memory type indicator.
			MCU_PHASE		: out MCU_PHASE_TYPE;
			
			VSYNCn			: in bit; -- Vertival sync signal.
			DE				: in bit; -- Horizontal or vertical sync.
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
			
			REF_CNT_EN		: out bit; -- Refresh counter enable.
			DMA_CNT_EN		: out bit; -- DMA control.
			VIDEO_CNT_EN	: out bit; -- Video control.
			VIDEO_CNT_LOAD	: out bit; -- Video control.
			
			MADRSEL			: out MADR_TYPE; -- Address multiplexer control.

			DTACKn			: out bit; -- Data acknowledge signal.

			DATA_IN			: in std_logic_vector(7 downto 0);
			DATA_OUT		: out bit_vector(7 downto 0);
			DATA_EN			: out bit
	);
end WF25912IP_CTRL;

architecture BEHAVIOR of WF25912IP_CTRL is
type BANKS is (BANK1, BANK0);
type MATRIX_ELEMENTS is array (1 to 14, 1 to 16) of bit;
type SLICES_SYNC_TYPE is (STOP, WAIT_D1, SYNC, RUN);
constant TIME_MATRIX : MATRIX_ELEMENTS := 
	(('0','0','0','0','0','1','1','0','0','0','0','0','0','1','1','0'),		-- RASn.
	 ('1','0','0','0','0','0','0','1','1','0','0','0','0','0','0','1'),		-- CASHn.
	 ('1','0','0','0','0','0','0','1','1','0','0','0','0','0','0','1'),		-- CASLn.
	 ('0','0','0','0','0','1','1','1','1','1','1','1','1','1','1','0'),		-- WEn.
	 ('0','0','0','0','0','0','0','1','1','1','1','1','1','1','1','0'),		-- RDATn.
	 ('0','0','0','0','0','1','1','1','1','1','1','1','1','1','1','0'),		-- WDATn.
	 ('0','1','1','1','1','0','0','0','0','0','0','0','0','0','0','0'),		-- LATCHn.
	 ('1','0','0','0','0','1','1','1','1','1','1','1','1','1','1','1'),		-- CMPCSn.
	 ('1','1','1','1','1','1','1','1','0','0','0','0','0','0','1','1'),		-- DCYCn, SLOADn (video and sound data).
	 ('0','0','0','0','0','1','1','0','0','0','0','0','0','0','0','0'),		-- REF_CNT_EN.
	 ('0','0','0','0','0','1','1','0','0','0','0','0','0','0','0','0'),		-- DMA_CNT_EN.
	 ('0','0','0','0','0','0','0','0','0','0','0','0','0','1','1','0'),		-- DMA_CNT_EN, FRAME_CNT_EN (video and sound).
	 ('0','1','1','1','1','1','1','0','0','1','1','1','1','1','1','0'),		-- MADRSEL.
	 ('0','0','0','0','0','0','0','1','1','1','1','1','1','1','1','0'));	-- DTACK_MASKn.
signal MCU_PHASE_I			: MCU_PHASE_TYPE;
signal SLICES_PHASE			: SLICES_SYNC_TYPE;
signal SLICES_NEXT_PHASE	: SLICES_SYNC_TYPE;
signal MADRSEL_I			: bit; -- Control signal for the high low data multiplexer.
signal MEMCONFIG			: std_logic_vector(7 downto 0);
signal TIME_SLICE_CNT		: std_logic_vector(2 downto 0);
signal SLICE_NUMBER			: integer range 1 to 16;
signal BANK_SWITCH			: BANKS;
signal M_ADR_I 				: bit_vector(23 downto 0);

signal RAS_Pn, RAS_Nn		: bit;
signal CASL_Pn, CASL_Nn		: bit; 
signal CASH_Pn, CASH_Nn		: bit;
signal WE_Pn, WE_Nn			: bit;

signal RDAT_Pn				: bit;
signal WDAT_Pn, WDAT_Nn		: bit;
signal LATCH_Nn				: bit;

signal DTACK_MASK_In 		: bit;
signal DTACK_MASK_Pn 		: bit;

signal CMPCS_In				: bit;
signal DCYC_Pn, DCYC_Nn		: bit;
signal DCYC_In				: bit;

signal REF_CNT_EN_N			: bit;
signal DMA_CNT_EN_N			: bit;
signal V_DMA_CNT_EN_N		: bit; -- Video DMA.
signal S_DMA_CNT_EN_N		: bit; -- Sound DMA.

signal SLICECNT_SYNC		: boolean;
begin
	M_ADR_I <= M_ADR & '0';
	VIDEO_CNT_LOAD <= '1' when VSYNCn = '0' else '0';

	-- Bits 3 and 2 of the MEMCONFIG select the bank 0 and the bits
	-- 1 and 0 are for the selection of bank 1.
	BANK0_TYPE <= 	K2048 when MEMCONFIG(3 downto 2) = "10" else
					K512  when MEMCONFIG(3 downto 2) = "01" else
					K128; -- 128K is default.
	
	-- ########################################################################### --
	-- Control signals generated by the two processes SYNC_P and SYNC_N or         --
	-- asynchronous. There are four different kinds of generation:                 --
	-- 1. Signals with timings between two different clock edges (e.g. start at    --
	-- rising edge of CLK_x2 and end at falling edge of CLK_x2 or vice versa)    --
	-- are generated by both processes SYNC_P and SYNC_N with logical OR. These    --
	-- signals are RAS1n, RAS0n, CAS1Hn, CAS1Ln, CAS0Hn, CAS0Ln, WEn, and WDATn.   --
	-- 2. Signals embedded between two rising edges of CLK_x2 are controlled      --
	-- by the process SYNC_P. These signals are RDATn, DTACKn and MADR_SEL.        --
	-- 3. Signals embedded between two falling edges of CLK_x2 are controlled     --
	-- by the process SYNC_N. These signals are LATCHn, CMPCSn and the three        --
	-- counter enables DMA_CNT_EN, REF_CNT_EN and VIDEO_CNT_EN.                    --
	-- 4. Signals with a length of only half a period of CLK_x2 are controlled    --
	-- by both processes and generated by logic AND.                               --
	-- These signals are REF_CNT_EN, DMA_CNT_EN and VIDEO_CNT_EN.                  --
	-- The timing of all these signals is taken from the timing table TIME_MATRIX. --
	-- ########################################################################### --
	
	-- DRAM control signals:
	RAS0n <= '0' when (RAS_Pn = '0' and RAS_Nn = '0' and BANK_SWITCH = BANK0) else
			 '0' when (MCU_PHASE_I = REFRESH and RAS_Pn = '0' and RAS_Nn = '0') else '1';
	RAS1n <= '0' when (RAS_Pn = '0' and RAS_Nn = '0' and BANK_SWITCH = BANK1) else
			 '0' when (MCU_PHASE_I = REFRESH and RAS_Pn = '0' and RAS_Nn = '0') else '1';
	CAS0Ln <= '0' when CASL_Pn = '0' and CASL_Nn = '0' and BANK_SWITCH = BANK0 else '1';
	CAS1Ln <= '0' when CASL_Pn = '0' and CASL_Nn = '0' and BANK_SWITCH = BANK1 else '1';
	CAS0Hn <= '0' when CASH_Pn = '0' and CASH_Nn = '0' and BANK_SWITCH = BANK0 else '1';
	CAS1Hn <= '0' when CASH_Pn = '0' and CASH_Nn = '0' and BANK_SWITCH = BANK1 else '1';
	WEn <= WE_Pn or WE_Nn;

	-- The main data switch controls:
	RDATn <= RDAT_Pn;				-- Data from RAM to bus.
	WDATn <= WDAT_Pn or WDAT_Nn;	-- Data from bus to RAM.
	LATCHn <= LATCH_Nn;				-- Data (LATCH) from RAM to bus.

	-- Address high byte low byte control:
	MADRSEL <= MEM_LOW_ADR when MADRSEL_I = '0' else MEM_HI_ADR;

	-- Shifter stuff (sound and video DMA).
	DCYC_In <= DCYC_Pn or DCYC_Nn;
	CMPCSn 	<= 	'0'	when CMPCS_In = '0' or RESETn = '0' else '1'; -- Reset of the original ST MMU.
	DCYCn 	<= 	'0' when DCYC_In = '0' 	or RESETn = '0' else '1'; -- Reset of the original ST MMU.

	SLOADn 	<= '0'	when DCYC_In = '0' else '1';

	-- Address counter controls.
	DMA_CNT_EN 		<= DMA_CNT_EN_N;		-- DMA for ACSI and Floppy.
	VIDEO_CNT_EN 	<= V_DMA_CNT_EN_N;		-- DMA for video data.
	FRAME_CNT_EN 	<= S_DMA_CNT_EN_N;		-- DMA for sound data.
	REF_CNT_EN 		<= REF_CNT_EN_N;		-- Refresh counter.

	-- Data acknowledge logic:
	DTACK_MASK_In <= DTACK_MASK_Pn;
	DTACKn <= -- The following conditions are valid for RAM and DMA mode: 
			'0' when MEMCONFIG(3 downto 0) = x"0" and -- 128K in bank 0 and 128K in bank 1.
		 			 M_ADR_I <= x"03FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"1" and -- 128K in bank 0 and 512K in bank 1.
		 			 M_ADR_I <= x"09FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"2" and -- 128K in bank 0 and 2MB in bank 1.
		 			 M_ADR_I <= x"21FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"4" and -- 512K in bank 0 and 128K in bank 1.
		 			 M_ADR_I <= x"09FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"5" and -- 512K in bank 0 and 512K in bank 1.
		 			 M_ADR_I <= x"0FFFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"6" and -- 512K in bank 0 and 2MB in bank 1.
		 			 M_ADR_I <= x"27FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"8" and -- 2MB in bank 0 and 128K in bank 1.
		 			 M_ADR_I <= x"21FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"9" and -- 2MB in bank 0 and 512K in bank 1.
		 			 M_ADR_I <= x"27FFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			'0' when MEMCONFIG(3 downto 0) = x"A" and -- 2MB in bank 0 and 2MB in bank 1.
		 			 M_ADR_I <= x"3FFFFE" and M_ADR_I > x"000007" and DTACK_MASK_In = '0'	else
			-- And this one for SHIFTER access mode:
			-- That means HSCROLL register; SHIFTMODE register and PALETTE registers.
			'0' when MCU_PHASE_I = SHIFTER and DTACK_MASK_In = '0' else
			'0' when DMAn = '0' and DTACK_MASK_In = '0' else '1';

	-- RAM bank select logic. It is dependant on the equiped memory.
	-- The BANK0 is the lower one in the ATARI's address space.
	BANK_SWITCH <= 
				-- 128Kwords in bank 0:
				BANK0 when BANK0_TYPE = K128  and M_ADR_I <= x"01FFFE" else
				-- 512Kwords in bank 0:
				BANK0 when BANK0_TYPE = K512  and M_ADR_I <= x"07FFFE" else
				-- 2048Kwords in bank 0:
				BANK0 when BANK0_TYPE = K2048 and M_ADR_I <= x"1FFFFE" else
				BANK1;

	MCU_PHASE_CONDITIONING: process(RESETn, CLK_x2)
	-- To adjust the timing of MCU_PHASE with the RAM control signals etc.,
	-- It is necessary to delay the MCU_PHASE_I half a period of CLK_x2.
	-- This delay is done in this process.
	begin
		if RESETn = '0' then
			MCU_PHASE <= REFRESH;
		elsif CLK_x2 = '1' and CLK_x2' event then
			MCU_PHASE <= MCU_PHASE_I;
		end if;
	end process MCU_PHASE_CONDITIONING;

	SYNC_P: process
	-- The RASn and CASn multiplexers are implemented in this process as also
	-- the synchronization of the RAM relevant control signals RAS and CAS and WEn.
	-- The BANK_SEL synchronization is not possible due to the fact, that the
	-- BANK_SEL signal appears one CLK cycle too late.
	begin
		wait until CLK_x2 = '1' and CLK_x2' event;
		case MCU_PHASE_I is
			when REFRESH | RAM | VIDEO | SOUND => RAS_Pn <= TIME_MATRIX(1, SLICE_NUMBER);
			when others => RAS_Pn <= '1';
		end case;
		if 	(MCU_PHASE_I = RAM and UDSn = '0') or
			 MCU_PHASE_I = VIDEO or
			 MCU_PHASE_I = SOUND then
				CASH_Pn <= TIME_MATRIX(2, SLICE_NUMBER);		
		else
				CASH_Pn <= '1';
		end if;
		if 	(MCU_PHASE_I = RAM and LDSn = '0') or
			 MCU_PHASE_I = SOUND or
			 MCU_PHASE_I = VIDEO then
				CASL_Pn <= TIME_MATRIX(3, SLICE_NUMBER);		
		else
				CASL_Pn <= '1';
		end if;
		if	MCU_PHASE_I = RAM and RWn = '0' then
				WE_Pn <= TIME_MATRIX(4, SLICE_NUMBER); -- write only In RAM mode.
		else
				WE_Pn <= '1';
		end if;
		if	(MCU_PHASE_I = RAM or MCU_PHASE_I = SHIFTER) and RWn = '1' then
			RDAT_Pn <= TIME_MATRIX(5, SLICE_NUMBER); -- Read to bus only in RAM mode.
		else
			RDAT_Pn <= '1';
		end if;
		if	(MCU_PHASE_I = RAM or MCU_PHASE_I = SHIFTER) and RWn = '0' then
			WDAT_Pn <= TIME_MATRIX(6, SLICE_NUMBER);		
		else
			WDAT_Pn <= '1';
		end if;
		if	MCU_PHASE_I = VIDEO then
			-- DE is required here due to asynchronous control.
			DCYC_Pn <= TIME_MATRIX(9, SLICE_NUMBER);		
		else
			DCYC_Pn <= '1';
		end if;
		if MCU_PHASE_I = RAM or MCU_PHASE_I = SHIFTER then
			DTACK_MASK_Pn <= '0';  -- Normal RAM access.
		else
			DTACK_MASK_Pn <= '1';
		end if;
		MADRSEL_I <= TIME_MATRIX(13, SLICE_NUMBER);
	end process SYNC_P;

	SYNC_N: process
	begin
		wait until CLK_x2 = '0' and CLK_x2' event;
		case MCU_PHASE_I is
			when REFRESH | RAM | VIDEO | SOUND => RAS_Nn <= TIME_MATRIX(1, SLICE_NUMBER);
			when others => RAS_Nn <= '1';
		end case;
		if 	(MCU_PHASE_I = RAM and UDSn = '0') or
			 MCU_PHASE_I = SOUND or
			 MCU_PHASE_I = VIDEO then
				CASH_Nn <= TIME_MATRIX(2, SLICE_NUMBER);		
		else
				CASH_Nn <= '1';
		end if;
		if 	(MCU_PHASE_I = RAM and LDSn = '0') or
			 MCU_PHASE_I = SOUND or
			 MCU_PHASE_I = VIDEO then
				CASL_Nn <= TIME_MATRIX(3, SLICE_NUMBER);		
		else
			CASL_Nn <= '1';
		end if;
		if	MCU_PHASE_I = RAM and RWn = '0' then
			WE_Nn <= TIME_MATRIX(4, SLICE_NUMBER);		
		else
			WE_Nn <= '1';
		end if;
		if	(MCU_PHASE_I = RAM or MCU_PHASE_I = SHIFTER) and RWn = '0' then
			WDAT_Nn <= TIME_MATRIX(6, SLICE_NUMBER);		
		else
			WDAT_Nn <= '1';
		end if;
		if	MCU_PHASE_I = RAM or MCU_PHASE_I = SHIFTER then
			LATCH_Nn <= TIME_MATRIX(7, SLICE_NUMBER);		
		else
			LATCH_Nn <= '0';
		end if;
		if	MCU_PHASE_I = SHIFTER then 
			CMPCS_In <= TIME_MATRIX(8, SLICE_NUMBER);		
		else
			CMPCS_In <= '1';
		end if;
		if	MCU_PHASE_I = VIDEO then
			-- DE is required here due to asynchronous control.
			DCYC_Nn <= TIME_MATRIX(9, SLICE_NUMBER);		
		else
			DCYC_Nn <= '1';
		end if;
		if	MCU_PHASE_I = REFRESH then
			REF_CNT_EN_N <= TIME_MATRIX(10, SLICE_NUMBER);		
		else
			REF_CNT_EN_N <= '0';
		end if;
		if	MCU_PHASE_I = RAM and DMAn = '0' then -- DMA access.
			DMA_CNT_EN_N <= TIME_MATRIX(11, SLICE_NUMBER);		
		else
			DMA_CNT_EN_N <= '0';
		end if;
		if	MCU_PHASE_I = VIDEO then
			V_DMA_CNT_EN_N <= TIME_MATRIX(12, SLICE_NUMBER);		
		else
			V_DMA_CNT_EN_N <= '0';
		end if;
		if	MCU_PHASE_I = SOUND then
			S_DMA_CNT_EN_N <= TIME_MATRIX(12, SLICE_NUMBER);		
		else
			S_DMA_CNT_EN_N <= '0';
		end if;
	end process SYNC_N;

	MEMCONFIG_REG: process
	begin
		-- The MEMCONFIG must start up with x"0A" indicating virtual 4MB RAM.
		-- Otherwise the RAM test routine will hang due to no DTACKn signal.
		-- The value of x"0A" is written immediately after system startup by
		-- the CPU. This register may not have a clear via RESETn because the
		-- operating system does not initialise it after an interrupt like
		-- monochrome detect (level 15).
		wait until CLK_x2 = '1' and CLK_x2' event;
		if MEM_CONFIG_CS = '1' and RWn = '0' then
			MEMCONFIG <= DATA_IN; -- write to register
		end if;
	end process MEMCONFIG_REG;
	-- Read:
	DATA_OUT <= To_BitVector(MEMCONFIG) when MEM_CONFIG_CS = '1' and RWn = '1' else (others => '0');
	DATA_EN <= MEM_CONFIG_CS and RWn;

	MCU_PHASE_SWITCH: process(RESETn, CLK_x2)
	-- AD MCU_PHASE_TYPES: SHIFTER is foreseen to  access the shifter
	-- registers; VIDEO transfers video data from RAM to shifter; RAM 
	-- is the CPU or DMA to RAM access.
	-- The REFRESH cycle is foreseen to hold the data in the dynamic RAMs.
	-- This process controls the type of data transfer in the second period
	-- of the MCU cycle (250ns ... 500ns). While the first half of the MCU
	-- cycle is reserved for data transfer to the shifter, the second one
	-- shares data transfer between DMA, CPU and RAM and is foreseen for the 
	-- RAM REFRESH process and the data transfer to the shifter registers 
	-- (MCU_PHASE = SHIFTER).
	-- The TMP variable controls the video access every second phase 7 in
	-- case of a fast clocked MCU (double clock rate than the original MCU).
	variable TMP : bit;
	begin
		if RESETn = '0' then
			MCU_PHASE_I <= REFRESH; -- REFRESH during reset keeps data alive.
		elsif CLK_x2 = '0' and CLK_x2' event then
			TMP := not TMP; -- Toggle function.
			if SLICE_NUMBER = 7 then
				-- Pay attention here! The DMA sound transfer must happen
				-- Right after the falling edge of DE. Otherwise there might
				-- Occur synchronisation problems with trash video output.
				-- The DMA sound module must look itself for this correct
				-- timing taking the DE status into account.
				if DE = '1' and CLKSEL = CLK_16M then
					MCU_PHASE_I <= VIDEO; -- Video data out for a originally clocked MCU.
				elsif DE = '1' and CLKSEL = CLK_32M and TMP = '1' then
					MCU_PHASE_I <= VIDEO; -- Video data out for a fast clocked MCU.
				elsif DE = '0' and SOUND_REQ = true then
					MCU_PHASE_I <= SOUND; -- DMA sound data out.
				else
					MCU_PHASE_I <= IDLE; -- Do nothing, wait for one of the data cycles.
				end if;
			elsif SLICE_NUMBER = 15 then
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

	SLICES_SYNC_MEM: process(RESETn, CLK_x2)
	begin
		if RESETn = '0' then
			SLICES_PHASE <= STOP;
		elsif CLK_x2 = '1' and CLK_x2' event then
			SLICES_PHASE <= SLICES_NEXT_PHASE;
		end if;
	end process SLICES_SYNC_MEM;
	
	SLICES_SYNC_CTRL: process(SLICES_PHASE, DE, CLK_x1)
	-- This process is necessary to synchronize the time
	-- slice counter with the video DE frequency and the
	-- GLUE's DMA time slice counter.
	-- In case of video it provides exact video data timing
	-- between RAM and SHIFTER to achieve video output 
	-- without erroneous lateral shift on the screen.
	-- During DMA access it provides exact DMA start timing
	-- to achieve proper DMA operation. This process must
	-- work with the same frequency as the DMA control unit
	-- in the GLUE.
	begin
		case SLICES_PHASE is
			when STOP =>
				if CLK_x1 = '0' and DE = '0' then
					SLICES_NEXT_PHASE <= WAIT_D1;
				else
					SLICES_NEXT_PHASE <= STOP;
				end if;
				SLICECNT_SYNC <= false;
			when WAIT_D1 =>
				if CLK_x1 = '0' and DE = '1' then
					SLICES_NEXT_PHASE <= SYNC;
				else
					SLICES_NEXT_PHASE <= WAIT_D1;
				end if;
				SLICECNT_SYNC <= false;
			when SYNC =>
				if CLK_x1 = '0' then
					SLICES_NEXT_PHASE <= RUN;
				else
					SLICES_NEXT_PHASE <= SYNC;
				end if;
				SLICECNT_SYNC <= true;	-- Syncing.
			when RUN =>
				SLICES_NEXT_PHASE <= RUN; -- Run infinite.
				SLICECNT_SYNC <= false;
		end case;
	end process SLICES_SYNC_CTRL;

	TIME_SLICES: process
	-- The process counts 8 states like the 68000 bus states.
	-- This counter may not have a reset control because it has to
	-- Produce refresh timing during reset.
	begin
		wait until CLK_x2 = '1' and CLK_x2' event;
		if SLICECNT_SYNC = true then
			TIME_SLICE_CNT <= "100"; 	-- Resync.
		else
			TIME_SLICE_CNT <= TIME_SLICE_CNT + '1';
		end if;
	end process TIME_SLICES;

	-- Although all choices are covered, there is a default assignment not
	-- to let the compiler create latches.
	SLICE_NUMBER <= 1 when TIME_SLICE_CNT = "000" and CLK_x2 = '1' else
				    2 when TIME_SLICE_CNT = "000" and CLK_x2 = '0' else
				    3 when TIME_SLICE_CNT = "001" and CLK_x2 = '1' else
				    4 when TIME_SLICE_CNT = "001" and CLK_x2 = '0' else
				    5 when TIME_SLICE_CNT = "010" and CLK_x2 = '1' else
				    6 when TIME_SLICE_CNT = "010" and CLK_x2 = '0' else
				    7 when TIME_SLICE_CNT = "011" and CLK_x2 = '1' else
				    8 when TIME_SLICE_CNT = "011" and CLK_x2 = '0' else
				    9 when TIME_SLICE_CNT = "100" and CLK_x2 = '1' else
				    10 when TIME_SLICE_CNT = "100" and CLK_x2 = '0' else
				    11 when TIME_SLICE_CNT = "101" and CLK_x2 = '1' else
				    12 when TIME_SLICE_CNT = "101" and CLK_x2 = '0' else
				    13 when TIME_SLICE_CNT = "110" and CLK_x2 = '1' else
				    14 when TIME_SLICE_CNT = "110" and CLK_x2 = '0' else
				    15 when TIME_SLICE_CNT = "111" and CLK_x2 = '1' else
				    16 when TIME_SLICE_CNT = "111" and CLK_x2 = '0' else 16;
end architecture BEHAVIOR;
