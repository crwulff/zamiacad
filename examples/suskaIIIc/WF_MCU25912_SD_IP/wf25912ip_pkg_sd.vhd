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
---- This is the package file containing the component            ----
---- declarations.                                                ----
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
-- Initial Release.
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- Revision 2K9A  2008/11/29 WF
--   Introduced VIDEO_HIMODE and LINE80_RELOAD.
-- 

library ieee;
use ieee.std_logic_1164.all;

package WF25912IP_PKG is
type MADR_TYPE is (MEM_LOW_ADR, MEM_HI_ADR);
type BANKTYPE is (K128, K512, K2048, K8192);
type MCU_PHASE_TYPE is (IDLE, RAM, VIDEO, SOUND, SHIFTER, REFRESH);
-- Component declarations:
component WF25912IP_CTRL_SD
port (  CLK				: in bit;
        RESETn			: in bit;

		LDSn, UDSn, RWn	: in bit;

		M_ADR			: in bit_vector(23 downto 1);

		CMPCS_REQ		: in bit;
		CMPCSn			: out bit;

		SOUND_REQ		: in boolean;
		FRAME_CNT_EN	: out bit;
		SLOADn			: out bit;

		RAMn			: in bit;
		DMAn			: in bit;
		DMA_SYNC		: out bit;
	
		VSYNCn			: in bit;
		DE				: in bit;
        VIDEO_HIMODE    : in bit;
		DCYCn			: out bit;
					
		MEM_CONFIG_CS	: in bit;
		MCU_PHASE		: out MCU_PHASE_TYPE;

		RAS0n			: out bit;
		CAS0Hn			: out bit;
		CAS0Ln			: out bit;

		WEn				: out bit;

		RAS1n			: out bit;
		CAS1Hn			: out bit;
		CAS1Ln			: out bit;

		RDATn			: out bit;
		WDATn			: out bit;
		LATCHn			: out bit;
			
		REF_EN			: out bit;
		DMA_CNT_EN		: out bit;
		VIDEO_CNT_EN	: out bit;
		VIDEO_CNT_LOAD	: out bit;
		LINE80_RELOAD	: out bit;
			
		MADRSEL			: out MADR_TYPE;

		DTACKn			: out bit;

		DATA_IN			: in std_logic_vector(7 downto 0);
		DATA_OUT		: out bit_vector(7 downto 0);
		DATA_EN			: out bit
	);
end component;

component WF25912IP_DMA_CTRL_SD
port (  CLK				: in bit;
		RESETn			: in bit;

		RWn				: in bit;
		
		DMA_BASE_HI_CS	: in bit;
		DMA_BASE_MID_CS	: in bit;
		DMA_BASE_LOW_CS	: in bit;

		DMA_COUNT_EN	: in bit;

		DMA_ADR			: out bit_vector(23 downto 1);
		
		DATA_IN			: in std_logic_vector(7 downto 0);
		DATA_OUT		: out bit_vector(7 downto 0);
		DATA_EN			: out bit
      );
end component;

component WF25912IP_ADRDEC_SD
port (  ADR					: in bit_vector(15 downto 1);
		ASn					: in bit;
		LDSn				: in bit;
		DEVn				: in bit;
		
		MEM_CONFIG_CS		: out bit;
		
		VIDEO_BASE_HI_CS	: out bit;
		VIDEO_BASE_MID_CS	: out bit;
		VIDEO_BASE_LOW_CS	: out bit;
		
		VIDEO_COUNT_HI_CS	: out bit;
		VIDEO_COUNT_MID_CS	: out bit;
		VIDEO_COUNT_LOW_CS	: out bit;
		
		DMA_BASE_HI_CS		: out bit;
		DMA_BASE_MID_CS		: out bit;
		DMA_BASE_LOW_CS		: out bit;
		
		CMPCS_REQ			: out bit;

		LINEWIDTH_CS		: out bit;

		SOUND_CTRL_CS				: out bit;
		SOUND_FRAME_START_HI_CS		: out bit;
		SOUND_FRAME_START_MID_CS	: out bit;
		SOUND_FRAME_START_LOW_CS	: out bit;
		SOUND_FRAME_ADR_HI_CS		: out bit;
		SOUND_FRAME_ADR_MID_CS		: out bit;
		SOUND_FRAME_ADR_LOW_CS		: out bit;
		SOUND_FRAME_END_HI_CS		: out bit;
		SOUND_FRAME_END_MID_CS		: out bit;
		SOUND_FRAME_END_LOW_CS		: out bit
      );
end component;

component WF25912IP_VIDEO_COUNTER_SD
port (  CLK					: in bit;
		RESETn				: in bit;
		RWn					: in bit;
		
		VIDEO_BASE_HI_CS	: in bit;
		VIDEO_BASE_MID_CS	: in bit;
		VIDEO_BASE_LOW_CS	: in bit;

		VIDEO_COUNT_HI_CS	: in bit;
		VIDEO_COUNT_MID_CS	: in bit;
		VIDEO_COUNT_LOW_CS	: in bit;
		
		DE					: in bit;
		VIDEO_COUNT_EN		: in bit;
		VIDEO_COUNT_LOAD	: in bit;
		LINE80_RELOAD		: in bit;

		LINEWIDTH_CS		: in bit;
		
		VIDEO_ADR			: out bit_vector(23 downto 1);
		
		DATA_IN			: in std_logic_vector(7 downto 0);
		DATA_OUT		: out bit_vector(7 downto 0);
		DATA_EN			: out bit
      );
end component;

component WF25912IP_DMA_SOUND_SD
port (  RESETn			: in bit;
		CLK				: in bit;
		
		RWn				: in bit;
		DATA_IN			: in std_logic_vector(7 downto 0);
		DATA_OUT		: out bit_vector(7 downto 0);
		DATA_EN			: out bit;

		MONOMONn		: in bit;

		DE				: in bit;
		MCU_PHASE		: in MCU_PHASE_TYPE;
		SINTn			: out bit;
		SINT_TAI		: out bit;
		SINT_IO7		: out bit;
		FRAME_CNT_EN	: in bit;
		SREQ			: in bit;
		SOUND_REQ		: out boolean;

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
end component;
end WF25912IP_PKG;
