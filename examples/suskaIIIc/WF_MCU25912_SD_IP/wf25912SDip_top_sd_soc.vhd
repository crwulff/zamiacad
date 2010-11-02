----------------------------------------------------------------------
----                                                              ----
---- SD-RAM memory control unit (MCU).				              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- This MCU is an enhanced version of the original ATARI MCU to ----
---- meet the requirements for modern SD-RAM chips. In detail,    ----
---- this MCU is well suited for the Micron device MT48LC16M16.   ----
---- This controller is systemside compatible to the original     ----
---- Atari MCU CO25912 enhanced with the sound features of STE    ----
---- machines.                                                    ----
----                                                              ----
---- Important Notice concerning the clock system:                ----
---- To use this code in a stand alone MCU chip or in a system    ----
---- on a programmable chip (SOC), the clock frequency must be    ----
---- 16MHz to meet the requirements for the original STs screen   ----
---- resolutions.                                                 ----
---- Affected by the clock selection is the video timing and the  ----
---- DMA sound module (originally in the STE machines).           ----
----                                                              ----
---- To guarantee proper operation of the DMA interchange between ----
---- MCU, GLUE, DMA, the clocks must be well selected. For more   ----
---- information see the Suska top level file for the SOC system  ----
---- or respective documentation for the different original types ----
---- of ST or STE machines.                                       ----
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
---- Copyright (C) 2008 Wolfgang Foerster                         ----
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
-- Revision 2K8A  2008/02/10 WF
--   Initial release.
-- Revision 2K8B  2008/12/24 WF
--   Minor changes.
-- Revision 2K9A  2008/11/29 WF
--   Introduced VIDEO_HIMODE and LINE80_RELOAD.
--   DTACK_MCUn has now synchronous reset to meet preset requirement.
--

use work.wf25912ip_pkg.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25912IP_SD_TOP_SOC is
	port(  
		CLK				: in bit; -- System clock, originally 16MHz, 32 MHz in the core.

		RESET_INn		: in bit;
		RESET_OUTn		: out bit;

		ASn				: in bit; -- Bus control signals.
		LDSn, UDSn		: in bit; -- Bus control signals.
		RWn				: in bit; -- Bus control signals.

		ADR				: in bit_vector(25 downto 1); -- The STs address bus.

		RAMn			: in bit; -- RAM access control.
		DMAn			: in bit; -- DMA access control.
		DEVn			: in bit; -- Device access (A23 downto 16) = x"FF".
		DMA_SYNC		: out bit; -- Synchronizes the GLUE DMA part with the MCU.
		
		VSYNCn			: in bit; -- Vertical sync.
		DE				: in bit; -- Horizontal or vertical data enable.
        VIDEO_HIMODE    : in bit; -- Access the video RAM with double speed.
					
		DCYCn			: out bit; -- Shifter load signal.
		CMPCSn			: out bit; -- Shifter video and sound register control.

		MONO_DETECTn	: in bit; -- Monochrome monitor detector (pin 4 of the 13 pin round video plug).
		EXT_CLKSELn		: in bit; -- Genlock clock select (pin 3 of the 13 pin round video plug, formerly GPO).
		SREQ			: in bit;	-- Sound data request.
		SLOADn			: out bit; 	-- DMA sound load control.
		SINT_TAI		: out bit; 	-- Sound frame interrupt filtered for timer A.
		SINT_IO7		: out bit; 	-- Sound frame interrupt XORed for MFP_IO7

		BA				: out bit_vector(1 downto 0); -- SD-RAM bank select.
		MAD				: out bit_vector(12 downto 0); -- SD-RAM address bus.

		WEn				: out bit; -- SD-RAM write select.

		DQM0H			: out bit; -- SD-RAM output buffer controls.
		DQM0L			: out bit;
		DQM1H			: out bit;
		DQM1L			: out bit;
					
		RAS0n			: out bit; -- SD-RAM row address select.
		RAS1n			: out bit;

		CAS0n			: out bit; -- SD-RAM column address select.
		CAS1n			: out bit;

		RDATn			: out bit; -- Buffer control.
		WDATn			: out bit; -- Buffer control.
		LATCHn			: out bit; -- Buffer control.
			
		DTACKn			: out bit; -- Data acknowledge signal.

		DATA_IN			: in std_logic_vector(7 downto 0);
		DATA_OUT		: out std_logic_vector(7 downto 0);
		DATA_EN			: out bit
	);
end entity WF25912IP_SD_TOP_SOC;

architecture STRUCTURE of WF25912IP_SD_TOP_SOC is
signal RESET_In				: bit;
signal DTACK_In				: bit;
signal DTACK_LOCK			: boolean;
signal DTACK_DMAn			: bit;
signal DTACK_MCUn			: bit;
signal DATA_OUT_CTRL		: bit_vector(7 downto 0);
signal DATA_OUT_DMA_CTRL	: bit_vector(7 downto 0);
signal DATA_OUT_VCNT		: bit_vector(7 downto 0);
signal DATA_OUT_DMASND		: bit_vector(7 downto 0);
signal DATA_EN_CTRL			: bit;
signal DATA_EN_DMA_CTRL		: bit;
signal DATA_EN_VCNT			: bit;
signal DATA_EN_DMASND		: bit;

signal RAS1_CTRLn			: bit;
signal CAS1H_CTRLn			: bit;
signal CAS1L_CTRLn			: bit;

signal RAS0_CTRLn			: bit;
signal CAS0H_CTRLn			: bit;
signal CAS0L_CTRLn			: bit;

signal WE_CTRLn				: bit;

signal CMPCS_REQ_I			: bit;
signal DMA_CNT_EN_I			: bit;
signal VIDEO_CNT_EN_I		: bit;
signal VIDEO_CNT_LOAD_I		: bit;
signal LINE80_RELOAD_I		: bit;

signal MADRSEL_I			: MADR_TYPE;

signal RAM_ADR_I			: bit_vector(12 downto 0);
signal M_ADR_I				: bit_vector(23 downto 1);
signal DMA_ADR_I			: bit_vector(23 downto 1);
signal VIDEO_ADR_I			: bit_vector(23 downto 1);

signal MEM_CONFIG_CS_I		: bit;
signal MCU_PHASE_I			: MCU_PHASE_TYPE;
					
signal VIDEO_BASE_HI_CS_I	: bit;
signal VIDEO_BASE_MID_CS_I	: bit;
signal VIDEO_BASE_LOW_CS_I	: bit;
		
signal VIDEO_COUNT_HI_CS_I	: bit;
signal VIDEO_COUNT_MID_CS_I	: bit;
signal VIDEO_COUNT_LOW_CS_I	: bit;
		
signal DMA_BASE_HI_CS_I		: bit;
signal DMA_BASE_MID_CS_I	: bit;
signal DMA_BASE_LOW_CS_I	: bit;
		
signal LINEWIDTH_CS_I		: bit;

signal REF_EN_I				: bit;
signal SOUND_REQ_I			: boolean;

signal MONOMON_In			: bit;

signal SOUND_CTRL_CS_I				: bit;
signal SOUND_FRAME_START_HI_CS_I	: bit;
signal SOUND_FRAME_START_MID_CS_I	: bit;
signal SOUND_FRAME_START_LOW_CS_I	: bit;
signal SOUND_FRAME_ADR_HI_CS_I		: bit;
signal SOUND_FRAME_ADR_MID_CS_I		: bit;
signal SOUND_FRAME_ADR_LOW_CS_I		: bit;
signal SOUND_FRAME_END_HI_CS_I		: bit;
signal SOUND_FRAME_END_MID_CS_I		: bit;
signal SOUND_FRAME_END_LOW_CS_I		: bit;
signal FRAME_CNT_EN_I				: bit;

signal DMA_SOUND_ADR_I				: bit_vector(23 downto 1);

signal INIT_STATE			: std_logic_vector(11 downto 0);
begin
	RESET_FILTER: process(CLK)
	-- To avoid malfunction to the MCU, the reset signal, which is 
	-- generated out of DMAn and RAMn is filtered. The reason is the
	-- asynchromous generation of DMAn and RAMn in the GLUE chip.
	-- Remark: Not really required for SOC-Reset.
	variable TMP: std_logic_vector(1 downto 0);
	begin
		if CLK = '1' and CLK' event then
			if 	DEVn = '0' and RAMn = '0' and TMP < "10" then
				TMP := TMP + '1';
			elsif DEVn = '1' or RAMn = '1' then
				TMP := "00";
			end if;
		end if;
        --
        if TMP = "10" then
			RESET_In <= '0';
		else
            RESET_In <= '1';
		end if;
	end process RESET_FILTER;

	P_SDINIT: process(RESET_INn, CLK)
	-- This process provides the control for the initialisation of the SD-RAM.
	-- Since it is clocked by a 32MHz clock, the period is 31.25ns. There is a
	-- predivider, so that the INIT_STATE increments every eigths clock, means
	-- every 250ns. To meet the requirement of a 100us idle period, the INIT_STATE
	-- requires a value of 400. All other init steps work on the 250ns time step.
	-- The initialisation of the command respective to the INI_STATE works as
	-- follows:
		-- <= x"190" (400): IDLE.
		-- x"191" (401): PRECHARGE_ALL command.
		-- x"192" (402): NOP command.
		-- x"193" (403): AUTO_REFRESH command.
		-- x"194" (404): NOP command.
		-- x"195" (405): AUTO_REFRESH command.
		-- x"196" (404): NOP command.
		-- x"197" (406): Write to the mode register.
		-- x"198" (407): NOP command.
		-- x"199" (408): Stay in this mode, normal SD-RAM operation.
    variable TMP : std_logic_vector(2 downto 0);
	begin
		if RESET_INn = '0' then
			INIT_STATE <= (others => '0');
            TMP := "000"; -- Init ready, do nothing.
		elsif CLK = '1' and CLK' event then
			if TMP = "111" then
				INIT_STATE <= INIT_STATE + '1';
                TMP := "000";
			elsif INIT_STATE = x"199" then
                TMP := "000"; -- Init ready, do nothing.
			else
                TMP := TMP + '1';
			end if;
		end if;
	end process P_SDINIT;

	RESET_OUTn <= '1' when INIT_STATE = x"199" else '0'; -- This reset controls the CPU.
	
	M_ADR_I <= 
		ADR(23 downto 1) when MCU_PHASE_I = RAM and DMAn = '1' else
		DMA_ADR_I when MCU_PHASE_I = RAM and DMAn = '0' else
		VIDEO_ADR_I when MCU_PHASE_I = VIDEO else
		DMA_SOUND_ADR_I when MCU_PHASE_I = SOUND else (others => '0');

    -- Select column and bank with MADRSEL_I = MEM_HI_ADR:
	RAM_ADR_I <= x"0" & M_ADR_I(22 downto 14) when MADRSEL_I = MEM_HI_ADR else M_ADR_I(13 downto 1);

	BA <= "00" when INIT_STATE /= x"199" else ADR(25 downto 24);
	MAD <= '0' & x"220" when INIT_STATE = x"197" else -- Command: CAS latency = 2, single location access, burst length = 1.
           '0' & x"220" when INIT_STATE = x"198" else -- Command: CAS latency = 2, single location access, burst length = 1.
           '0' & x"400" when INIT_STATE /= x"199" else -- Used for PRECHARGE_ALL (A10 must be high).
		   RAM_ADR_I when RAS1_CTRLn = '0' or RAS0_CTRLn = '0' else -- Row address programming.
		   x"2" & RAM_ADR_I(8 downto 0); -- Select auto precharge and column address.
	
	WEn <= '0' when INIT_STATE = x"191" else -- PRECHARGE_ALL.
		   '0' when INIT_STATE = x"197" else -- Write mode register.
		   '0' when WE_CTRLn = '0' and INIT_STATE = x"199" else '1';

	RAS0n <= '0' when INIT_STATE = x"193" else -- Auto refresh.
			 '0' when INIT_STATE = x"195" else -- Auto refresh.
			 '0' when INIT_STATE = x"197" else -- Write mode register.
             '0' when REF_EN_I = '1' and INIT_STATE = x"199" else -- Auto refresh.
			 '0' when RAS0_CTRLn = '0' and INIT_STATE = x"199" else '1';
	RAS1n <= '0' when INIT_STATE = x"193" else -- Auto refresh.
			 '0' when INIT_STATE = x"195" else -- Auto refresh.
			 '0' when INIT_STATE = x"197" else -- Write mode register.
             '0' when REF_EN_I = '1' and INIT_STATE = x"199" else -- Auto refresh.
			 '0' when RAS1_CTRLn = '0' and INIT_STATE = x"199" else '1';

	CAS0n <= '0' when INIT_STATE = x"193" else -- Auto refresh.
			 '0' when INIT_STATE = x"195" else -- Auto refresh.
			 '0' when INIT_STATE = x"197" else -- Write mode register.
             '0' when REF_EN_I = '1' and INIT_STATE = x"199" else -- Auto refresh.
			 '0' when (CAS0H_CTRLn = '0' or CAS0L_CTRLn = '0') and INIT_STATE = x"199" else '1';
	CAS1n <= '0' when INIT_STATE = x"193" else -- Auto refresh.
			 '0' when INIT_STATE = x"195" else -- Auto refresh.
			 '0' when INIT_STATE = x"197" else -- Write mode register.
             '0' when REF_EN_I = '1' and INIT_STATE = x"199" else -- Auto refresh.
			 '0' when (CAS1H_CTRLn = '0' or CAS1L_CTRLn = '0') and INIT_STATE = x"199" else '1';

	DQM0H <= '0' when CAS0H_CTRLn = '0' and INIT_STATE = x"199" else '1';
	DQM0L <= '0' when CAS0L_CTRLn = '0' and INIT_STATE = x"199" else '1';
	DQM1H <= '0' when CAS1H_CTRLn = '0' and INIT_STATE = x"199" else '1';
	DQM1L <= '0' when CAS1L_CTRLn = '0' and INIT_STATE = x"199" else '1';

	DATA_EN <= '0' when INIT_STATE /= x"199"  else
               DATA_EN_CTRL or DATA_EN_DMA_CTRL or DATA_EN_VCNT or DATA_EN_DMASND;
	DATA_OUT <= To_StdLogicVector(DATA_OUT_CTRL) when DATA_EN_CTRL = '1' else
				To_StdLogicVector(DATA_OUT_DMA_CTRL) when DATA_EN_DMA_CTRL = '1' else
				To_StdLogicVector(DATA_OUT_VCNT) when DATA_EN_VCNT = '1' else
				To_StdLogicVector(DATA_OUT_DMASND) when DATA_EN_DMASND = '1' else (others => '0');

	-- Do not assert DTACKn for SOUND_CONTROL register x"8900"
	-- because it is a mirror register of the SHIFTER and the DTACKn
	-- is done via SHIFTER register control.
	DTACK_In <= '0' when MEM_CONFIG_CS_I = '1'				else
				'0' when VIDEO_BASE_HI_CS_I = '1' 			else
				'0' when VIDEO_BASE_MID_CS_I = '1' 			else
				'0' when VIDEO_BASE_LOW_CS_I = '1' 			else
				'0' when VIDEO_COUNT_HI_CS_I = '1' 			else
				'0' when VIDEO_COUNT_MID_CS_I = '1' 		else
				'0' when VIDEO_COUNT_LOW_CS_I = '1' 		else
				'0' when DMA_BASE_HI_CS_I = '1' 			else
				'0' when DMA_BASE_MID_CS_I = '1'			else
				'0' when DMA_BASE_LOW_CS_I = '1' 			else
                '0' when LINEWIDTH_CS_I = '1' 				else
				'0' when SOUND_FRAME_START_HI_CS_I = '1'	else 
				'0' when SOUND_FRAME_START_MID_CS_I = '1' 	else 
				'0' when SOUND_FRAME_START_LOW_CS_I = '1'	else 
				'0' when SOUND_FRAME_ADR_HI_CS_I = '1' 		else 
				'0' when SOUND_FRAME_ADR_MID_CS_I = '1'		else 
				'0' when SOUND_FRAME_ADR_LOW_CS_I = '1'		else 
				'0' when SOUND_FRAME_END_HI_CS_I = '1' 		else
				'0' when SOUND_FRAME_END_MID_CS_I = '1'		else
				'0' when SOUND_FRAME_END_LOW_CS_I = '1'		else '1';

	P_DTACK_LOCK: process
	-- This process releases a data acknowledge detect, one rising clock
	-- edge after the DTACK_In occured. This is necessary to ensure write
	-- data to registers for there is one rising clock edge required.
	begin
		wait until CLK = '1' and CLK' event;
		if DTACK_In = '0' then
			DTACK_LOCK <= false;
		else
			DTACK_LOCK <= true;
		end if;
	end process P_DTACK_LOCK;

	DTACK_OUT: process
	-- The DTACKn port pin is released on the falling clock edge after the data
	-- acknowledge detect (DTACK_LOCK) is asserted. The DTACKn is deasserted
	-- immediately when there is no further register access DTACK_In = '1';
	begin
		wait until CLK = '0' and CLK' event;
		if RESET_In = '0' then
			DTACK_MCUn <= '1';
		elsif DTACK_In = '1' then
			DTACK_MCUn <= '1';
		elsif DTACK_LOCK = false then
			DTACK_MCUn <= '0';
		end if;
	end process DTACK_OUT;

	-- The DMA relevant data acknowlege works instantaneous.
	DTACKn <= 	'0' when DTACK_DMAn = '0' else 
				'0' when DTACK_MCUn = '0' else '1';

	MONOMON_In <= MONO_DETECTn when EXT_CLKSELn = '1' else '1';

	I_CONTROL: WF25912IP_CTRL_SD
		port map(
			CLK 			=> CLK,
			RESETn 			=> RESET_In,

			LDSn 			=> LDSn,
			UDSn 			=> UDSn,
			RWn 			=> RWn,
			
			M_ADR 			=> M_ADR_I,

			CMPCS_REQ 		=> CMPCS_REQ_I,
			CMPCSn 			=> CMPCSn,

			SOUND_REQ		=> SOUND_REQ_I,
			FRAME_CNT_EN	=> FRAME_CNT_EN_I,
			SLOADn			=> SLOADn,

			RAMn 			=> RAMn,
			DMAn 			=> DMAn,
			DMA_SYNC		=> DMA_SYNC,

			MEM_CONFIG_CS 	=> MEM_CONFIG_CS_I,

			VSYNCn 			=> VSYNCn,
			DE 				=> DE,
            VIDEO_HIMODE    => VIDEO_HIMODE,
			DCYCn 			=> DCYCn,

			RAS0n 			=> RAS0_CTRLn,
			CAS0Hn 			=> CAS0H_CTRLn,
			CAS0Ln 			=> CAS0L_CTRLn,

			WEn 			=> WE_CTRLn,

			RAS1n 			=> RAS1_CTRLn,
			CAS1Hn 			=> CAS1H_CTRLn,
			CAS1Ln 			=> CAS1L_CTRLn,

			RDATn 			=> RDATn,
			WDATn 			=> WDATn,
			LATCHn 			=> LATCHn,
			
			REF_EN 			=> REF_EN_I,
			DMA_CNT_EN 		=> DMA_CNT_EN_I,
			VIDEO_CNT_EN 	=> VIDEO_CNT_EN_I,
			VIDEO_CNT_LOAD 	=> VIDEO_CNT_LOAD_I,
			LINE80_RELOAD	=> LINE80_RELOAD_I,
			
			MADRSEL			=> MADRSEL_I,
			MCU_PHASE 		=> MCU_PHASE_I,
			
			DTACKn			=> DTACK_DMAn,
			
			DATA_IN			=> DATA_IN,
			DATA_OUT		=> DATA_OUT_CTRL,
			DATA_EN			=> DATA_EN_CTRL
		);

	I_DMA: WF25912IP_DMA_CTRL_SD
		port map(
			CLK				=> CLK,
			RESETn 			=> RESET_In,
			
			RWn 			=> RWn,
		
			DMA_BASE_HI_CS 	=> DMA_BASE_HI_CS_I,
			DMA_BASE_MID_CS => DMA_BASE_MID_CS_I,
			DMA_BASE_LOW_CS => DMA_BASE_LOW_CS_I,

			DMA_COUNT_EN 	=> DMA_CNT_EN_I,

			DMA_ADR 		=> DMA_ADR_I,
		
			DATA_IN			=> DATA_IN,
			DATA_OUT		=> DATA_OUT_DMA_CTRL,
			DATA_EN			=> DATA_EN_DMA_CTRL
     	 );

	I_ADRDEC: WF25912IP_ADRDEC_SD
		port map(
			ADR 	=> ADR(15 downto 1),
			ASn 	=> ASn,
			LDSn 	=> LDSn,			
			DEVn 	=> DEVn,
		
			MEM_CONFIG_CS => MEM_CONFIG_CS_I,
		
			VIDEO_BASE_HI_CS 	=> VIDEO_BASE_HI_CS_I,
			VIDEO_BASE_MID_CS 	=> VIDEO_BASE_MID_CS_I,
			VIDEO_BASE_LOW_CS 	=> VIDEO_BASE_LOW_CS_I,
		
			VIDEO_COUNT_HI_CS 	=> VIDEO_COUNT_HI_CS_I,
			VIDEO_COUNT_MID_CS 	=> VIDEO_COUNT_MID_CS_I,
			VIDEO_COUNT_LOW_CS 	=> VIDEO_COUNT_LOW_CS_I,
		
			DMA_BASE_HI_CS 	    => DMA_BASE_HI_CS_I,
			DMA_BASE_MID_CS     => DMA_BASE_MID_CS_I,
			DMA_BASE_LOW_CS     => DMA_BASE_LOW_CS_I,
		
			CMPCS_REQ 		    => CMPCS_REQ_I,
		
			LINEWIDTH_CS	    => LINEWIDTH_CS_I,

			SOUND_CTRL_CS 				=>SOUND_CTRL_CS_I,
			SOUND_FRAME_START_HI_CS 	=> SOUND_FRAME_START_HI_CS_I,
			SOUND_FRAME_START_MID_CS 	=> SOUND_FRAME_START_MID_CS_I,
			SOUND_FRAME_START_LOW_CS 	=> SOUND_FRAME_START_LOW_CS_I,
			SOUND_FRAME_ADR_HI_CS 		=> SOUND_FRAME_ADR_HI_CS_I,
			SOUND_FRAME_ADR_MID_CS 		=> SOUND_FRAME_ADR_MID_CS_I,
			SOUND_FRAME_ADR_LOW_CS 		=> SOUND_FRAME_ADR_LOW_CS_I,
			SOUND_FRAME_END_HI_CS 		=> SOUND_FRAME_END_HI_CS_I,
			SOUND_FRAME_END_MID_CS 		=> SOUND_FRAME_END_MID_CS_I,
			SOUND_FRAME_END_LOW_CS 		=> SOUND_FRAME_END_LOW_CS_I
      	);

	I_VIDEO: WF25912IP_VIDEO_COUNTER_SD
		port map(
			CLK		            => CLK,
			RESETn 		        => RESET_In,

			RWn			        => RWn,
		
			VIDEO_BASE_HI_CS 	=> VIDEO_BASE_HI_CS_I,
			VIDEO_BASE_MID_CS 	=> VIDEO_BASE_MID_CS_I,
			VIDEO_BASE_LOW_CS 	=> VIDEO_BASE_LOW_CS_I,

			VIDEO_COUNT_HI_CS 	=> VIDEO_COUNT_HI_CS_I,
			VIDEO_COUNT_MID_CS 	=> VIDEO_COUNT_MID_CS_I,
			VIDEO_COUNT_LOW_CS 	=> VIDEO_COUNT_LOW_CS_I,
		
			DE 					=> DE,
			VIDEO_COUNT_EN 		=> VIDEO_CNT_EN_I,
			VIDEO_COUNT_LOAD 	=> VIDEO_CNT_LOAD_I,
			LINE80_RELOAD		=> LINE80_RELOAD_I,

			LINEWIDTH_CS 		=> LINEWIDTH_CS_I,
		
			VIDEO_ADR 			=> VIDEO_ADR_I,
		
			DATA_IN		        => DATA_IN,
			DATA_OUT	        => DATA_OUT_VCNT,
			DATA_EN		        => DATA_EN_VCNT
      	);

	I_DMASOUND: WF25912IP_DMA_SOUND_SD
		port map(
			CLK 			=> CLK,
			RESETn 			=> RESET_In,
			RWn 			=> RWn,
			DATA_IN			=> DATA_IN,
			DATA_OUT		=> DATA_OUT_DMASND,
			DATA_EN			=> DATA_EN_DMASND,
			
			MONOMONn		=> MONOMON_In,

			DE				=> DE,
			FRAME_CNT_EN 	=> FRAME_CNT_EN_I,
			-- SINTn		=> , -- Not required due to the use of SINT_IO7.
			SINT_TAI		=> SINT_TAI,
			SINT_IO7		=> SINT_IO7,
			MCU_PHASE 		=> MCU_PHASE_I,
			SREQ 			=> SREQ,
			SOUND_REQ 		=> SOUND_REQ_I,
			
			SOUND_CTRL_CS 				=> SOUND_CTRL_CS_I,
			SOUND_FRAME_START_HI_CS 	=> SOUND_FRAME_START_HI_CS_I,
			SOUND_FRAME_START_MID_CS	=> SOUND_FRAME_START_MID_CS_I,
			SOUND_FRAME_START_LOW_CS 	=> SOUND_FRAME_START_LOW_CS_I,
			SOUND_FRAME_ADR_HI_CS 		=> SOUND_FRAME_ADR_HI_CS_I,
			SOUND_FRAME_ADR_MID_CS 		=> SOUND_FRAME_ADR_MID_CS_I,
			SOUND_FRAME_ADR_LOW_CS 		=> SOUND_FRAME_ADR_LOW_CS_I,
			SOUND_FRAME_END_HI_CS 		=> SOUND_FRAME_END_HI_CS_I,
			SOUND_FRAME_END_MID_CS	 	=> SOUND_FRAME_END_MID_CS_I,
			SOUND_FRAME_END_LOW_CS 		=> SOUND_FRAME_END_LOW_CS_I,
			DMA_SOUND_ADR 				=> DMA_SOUND_ADR_I
      	);
end architecture STRUCTURE;
