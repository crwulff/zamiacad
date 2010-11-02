----------------------------------------------------------------------
----                                                              ----
---- Atari STE compatible IP Core				                  ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- This model provides the top level package  file of an STE    ----
---- compatible machine including CPU, Blitter, Shadow, MCU, DMA, ----
---- FDC, Shifter, GLUE, MFP, SOUND, ACIA and RTC.                ----
----                                                              ----
----                                                              ----
----                                                              ----
----                                                              ----
----                                                              ----
---- Author(s):                                                   ----
---- - Wolfgang Foerster, wf@experiment-s.de; wf@inventronik.de   ----
----                                                              ----
----------------------------------------------------------------------
----                                                              ----
---- Copyright (C) 2006 - 2008 Wolfgang Foerster                  ----
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
-- Revision 2K6B  2006/12/24 WF
--   Initial Release.
-- Revision 2K8A  2008/07/14 WF
--   Changes to run on the Suska hardware platform.
-- Revision 2K9A  2008/06/29 WF
--   Changes due to changes in other modules.
-- 

library ieee;
use ieee.std_logic_1164.all;

package WF_SUSKA_PKG is
	component WF68K00IP_TOP_SOC -- CPU.
		port (
			CLK				: in bit;
			RESET_COREn		: in bit; -- Core reset.
			
			-- Address and data:
			ADR_OUT			: out std_logic_vector(23 downto 1);
			ADR_EN			: out bit;
			DATA_IN			: in std_logic_vector(15 downto 0);
			DATA_OUT		: out std_logic_vector(15 downto 0);
			DATA_EN			: out bit;

			-- System control:
			BERRn			: in bit;
			RESET_INn		: in bit;
			RESET_OUT_EN	: out bit; -- Open drain.
			HALT_INn		: in std_logic;
			HALT_OUT_EN		: out bit; -- Open drain.
			
			-- Processor status:
			FC_OUT			: out std_logic_vector(2 downto 0);
			FC_OUT_EN		: out bit;
			
			-- Interrupt control:
			AVECn			: in bit;
			IPLn			: in bit_vector(2 downto 0);

			-- Aynchronous bus control:
			DTACKn			: in bit;
			AS_OUTn			: out bit;
			AS_OUT_EN		: out bit;
			RWn_OUT			: out bit;
			RW_OUT_EN		: out bit;
			UDS_OUTn		: out bit;
			UDS_OUT_EN		: out bit;
			LDS_OUTn		: out bit;
			LDS_OUT_EN		: out bit;
			
			-- Synchronous peripheral control:
			E				: out bit;
			VMA_OUTn		: out bit;
			VMA_OUT_EN		: out bit;
			VPAn			: in bit;
			
			-- Bus arbitration control:
			BRn				: in bit;
			BGn				: out bit;
			BGACKn			: in bit
			);
	end component WF68K00IP_TOP_SOC;

	component WF68KC00_TOP_SOC -- CPU.
		port (
			CLK				: in bit;
			RESET_COREn		: in bit; -- Core reset.
			
			-- Address and data:
			ADR_OUT			: out std_logic_vector(23 downto 1);
			ADR_EN			: out bit;
			DATA_IN			: in std_logic_vector(15 downto 0);
			DATA_OUT		: out std_logic_vector(15 downto 0);
			DATA_EN			: out bit;

			-- System control:
			BERRn			: in bit;
			RESET_INn		: in bit;
			RESET_OUT_EN	: out bit; -- Open drain.
			HALT_INn		: in std_logic;
			HALT_OUT_EN		: out bit; -- Open drain.
			
			-- Processor status:
			FC_OUT			: out std_logic_vector(2 downto 0);
			FC_OUT_EN		: out bit;
			
			-- Interrupt control:
			AVECn			: in bit;
			IPLn			: in bit_vector(2 downto 0);

			-- Aynchronous bus control:
			DTACKn			: in bit;
			AS_OUTn			: out bit;
			AS_OUT_EN		: out bit;
			RWn_OUT			: out bit;
			RW_OUT_EN		: out bit;
			UDS_OUTn		: out bit;
			UDS_OUT_EN		: out bit;
			LDS_OUTn		: out bit;
			LDS_OUT_EN		: out bit;
			
			-- Synchronous peripheral control:
			E				: out bit;
			VMA_OUTn		: out bit;
			VMA_OUT_EN		: out bit;
			VPAn			: in bit;
			
			-- Bus arbitration control:
			BRn				: in bit;
			BGn				: out bit;
			BGACKn			: in bit
			);
	end component WF68KC00_TOP_SOC;

	component WF101643IP_TOP_SOC -- Blitter.
		port (
			-- System controls:
			CLK			: in bit;
			RESETn		: in bit;
			AS_INn		: in bit;
			AS_OUTn		: out bit;
			LDS_INn		: in bit;
			LDS_OUTn	: out bit;
			UDS_INn		: in bit;
			UDS_OUTn	: out bit;
			RWn_IN		: in bit;
			RWn_OUT		: out bit;
			DTACK_INn	: in bit;
			DTACK_OUTn	: out bit;
			BERRn		: in bit;
			FC_IN		: in bit_vector(2 downto 0);
			FC_OUT		: out std_logic_vector(2 downto 0);
			BUSCTRL_EN	: out bit;
			INTn		: out bit;

			-- The bus:
			ADR_IN		: in bit_vector(23 downto 1);
			ADR_OUT		: out std_logic_vector(23 downto 1);
			ADR_EN		: out bit;
			DATA_IN		: in std_logic_vector(15 downto 0);
			DATA_OUT	: out std_logic_vector(15 downto 0);
			DATA_EN		: out bit;

			-- Bus arbitration:
			BGIn		: in bit;
			BGKIn		: in bit;
			BRn			: out bit;
			BGACK_INn	: in bit;
			BGACK_OUTn	: out bit;
			BGOn		: out bit
		);
	end component WF101643IP_TOP_SOC;

	component WF25915IP_TOP_SOC -- GLUE.
		port (
		    -- Clock system:
			GL_CLK		        : in bit; -- Originally 8MHz.
			GL_CLK_016	        : in bit; -- One sixteenth of GL_CLK.
			
			-- Adress decoder outputs:
			GL_ROM_6n	    : out bit;	-- STE.
			GL_ROM_5n	    : out bit;	-- STE.
			GL_ROM_4n	    : out bit;	-- ST.
			GL_ROM_3n	    : out bit;	-- ST.
			GL_ROM_2n	    : out bit;
			GL_ROM_1n	    : out bit;
			GL_ROM_0n	    : out bit;
			
            EN_RAM_14MB     : in bit;

			GL_ACIACS	    : out bit;
			GL_MFPCSn	    : out bit;
			GL_SNDCSn	    : out bit;
			GL_FCSn		    : out bit;

			GL_STE_SNDCS	: out bit; 	-- STE: Sound chip select.
			GL_STE_SNDIR	: out bit; 	-- STE: Data flow direction control.

			GL_STE_RTCCSn	: out bit; 	--STE only.
			GL_STE_RTC_WRn	: out bit; 	--STE only.
			GL_STE_RTC_RDn	: out bit;	--STE only.

			-- 6800 peripheral control, 
			GL_VPAn	: out bit;
			GL_VMAn : in bit;
			
			GL_DEVn	: out bit;
			GL_RAMn	: out bit;
			GL_DMAn	: out bit;
			
			-- Interrupt system:
			-- Comment out GL_AVECn for CPUs which do not provide the VMAn signal.
			GL_AVECn		: out bit;
			GL_STE_FDINT	: in bit; 	-- Floppy disk interrupt; STE only.
			GL_STE_HDINTn	: in bit; 	-- Hard disk interrupt; STE only.
			GL_MFPINTn		: in bit; 	-- ST.
			GL_STE_EINT3n	: in bit; 	--STE only.
			GL_STE_EINT5n	: in bit; 	--STE only.
			GL_STE_EINT7n	: in bit; 	--STE only.
			GL_STE_DINTn	: out bit; 	-- Disk interrupt (floppy or hard disk); STE only.
			GL_IACKn		: out bit; 	-- ST.
			GL_STE_IPL2n	: out bit; 	--STE only.
			GL_STE_IPL1n	: out bit; 	--STE only.
			GL_STE_IPL0n	: out bit; 	--STE only.
			
			-- Video timing:
			GL_BLANKn		: out bit;
			GL_DE			: out bit;
			GL_HSYNC_INn	: in bit;
			GL_HSYNC_OUTn	: out bit;
			GL_VSYNC_INn	: in bit;
			GL_VSYNC_OUTn	: out bit;
			GL_SYNC_OUT_EN	: out bit;
			
			-- Bus arbitration control:
			GL_RDY_INn		: in bit;
			GL_RDY_OUTn		: out bit;
			GL_BRn			: out bit;
			GL_BGIn			: in bit;
			GL_BGOn			: out bit;
			GL_BGACK_INn	: in bit;
			GL_BGACK_OUTn	: out bit;

			-- Adress and data bus:
			GL_ADDRESS		: in bit_vector(23 downto 1);
			-- ST: put the data bus to 1 downto 0. 
			-- STE: put the data out bus to 15 downto 0. 
			GL_DATA_IN		: in std_logic_vector(1 downto 0);
			GL_DATA_OUT		: out std_logic_vector(15 downto 0);
			GL_DATA_EN		: out bit;
			
			-- Asynchronous bus control:
			GL_RWn_IN		: in bit;
			GL_RWn_OUT		: out bit;
			GL_AS_INn		: in bit;
			GL_AS_OUTn		: out bit;
			GL_UDS_INn		: in bit;
			GL_UDS_OUTn		: out bit;
			GL_LDS_INn		: in bit;
			GL_LDS_OUTn		: out bit;
			GL_DTACK_INn	: in bit;
			GL_DTACK_OUTn	: out bit;
			GL_CTRL_EN		: out bit;
			
			-- System control:
			GL_RESETn		: in bit;
			GL_BERRn		: out bit;
			
			-- Processor function codes:
			GL_FC	: in bit_vector(2 downto 0);

			-- STE enhancements:
			GL_STE_FDDS		: out bit; 		-- Floppy type select (HD or DD).
			GL_STE_FCCLK	: out bit; 		-- Floppy controller clock select.
			GL_STE_JOY_RHn	: out bit; 		-- Read only FF9202 high byte.
			GL_STE_JOY_RLn	: out bit; 		-- Read only FF9202 low byte.
			GL_STE_JOY_WL	: out bit; 		-- Write only FF9202 low byte.
			GL_STE_JOY_WEn	: out bit; 		-- Write only FF9202 output enable.
			GL_STE_BUTTONn	: out bit; 		-- Read only FF9000 low byte.		
			GL_STE_PAD0Xn	: in bit; 		-- Counter input for the Paddle 0X.
			GL_STE_PAD0Yn	: in bit; 		-- Counter input for the Paddle 0Y.
			GL_STE_PAD1Xn	: in bit; 		-- Counter input for the Paddle 1X.
			GL_STE_PAD1Yn	: in bit; 		-- Counter input for the Paddle 1Y.
			GL_STE_PADRSTn	: out bit; 		-- Paddle monoflops reset.
			GL_STE_PENn		: in bit; 		-- Input of the light pen.
			GL_STE_SCCn		: out bit;	-- Select signal for the STE or TT SCC chip.
			GL_STE_CPROGn	: out bit	-- Select signal for the STE's cache processor.
			);
	end component WF25915IP_TOP_SOC;

	component WF25915IP_TOP_V1_SOC -- GLUE.
		port (
		    -- Clock system:
			GL_CLK		: in bit; -- Originally 8MHz.
			GL_CLK_016	: in bit; -- One sixteenth of GL_CLK.
			
            -- Core address select:
            GL_ROMSEL_FC_E0n    : in bit;
            EN_RAM_14MB         : in bit;
			-- Adress decoder outputs:
			GL_ROM_6n	: out bit;	-- STE.
			GL_ROM_5n	: out bit;	-- STE.
			GL_ROM_4n	: out bit;	-- ST.
			GL_ROM_3n	: out bit;	-- ST.
			GL_ROM_2n	: out bit;
			GL_ROM_1n	: out bit;
			GL_ROM_0n	: out bit;
			
			GL_ACIACS	: out bit;
			GL_MFPCSn	: out bit;
			GL_SNDCSn	: out bit;
			GL_FCSn		: out bit;

			GL_STE_SNDCS	: out bit; 	-- STE: Sound chip select.
			GL_STE_SNDIR	: out bit; 	-- STE: Data flow direction control.

			GL_STE_RTCCSn	: out bit; 	--STE only.
			GL_STE_RTC_WRn	: out bit; 	--STE only.
			GL_STE_RTC_RDn	: out bit;	--STE only.

			-- 6800 peripheral control, 
			GL_VPAn			: out bit;
			GL_VMAn 		: in bit;
			
			GL_DMA_SYNC		: in bit;
			GL_DEVn			: out bit;
			GL_RAMn			: out bit;
			GL_DMAn			: out bit;
			
			-- Interrupt system:
			-- Comment out GL_AVECn for CPUs which do not provide the VMAn signal.
			GL_AVECn		: out bit;
			GL_STE_FDINT	: in bit; 	-- Floppy disk interrupt; STE only.
			GL_STE_HDINTn	: in bit; 	-- Hard disk interrupt; STE only.
			GL_MFPINTn		: in bit; 	-- ST.
			GL_STE_EINT3n	: in bit; 	--STE only.
			GL_STE_EINT5n	: in bit; 	--STE only.
			GL_STE_EINT7n	: in bit; 	--STE only.
			GL_STE_DINTn	: out bit; 	-- Disk interrupt (floppy or hard disk); STE only.
			GL_IACKn		: out bit; 	-- ST.
			GL_STE_IPL2n	: out bit; 	--STE only.
			GL_STE_IPL1n	: out bit; 	--STE only.
			GL_STE_IPL0n	: out bit; 	--STE only.
			
			-- Video timing:
			GL_BLANKn		: out bit;
			GL_DE			: out bit;
			GL_MULTISYNC	: in bit_vector(3 downto 2);
            GL_VIDEO_HIMODE : out bit;
			GL_HSYNC_INn	: in bit;
			GL_HSYNC_OUTn	: out bit;
			GL_VSYNC_INn	: in bit;
			GL_VSYNC_OUTn	: out bit;
			GL_SYNC_OUT_EN	: out bit;
			
			-- Bus arbitration control:
			GL_RDY_INn		: in bit;
			GL_RDY_OUTn		: out bit;
			GL_BRn			: out bit;
			GL_BGIn			: in bit;
			GL_BGOn			: out bit;
			GL_BGACK_INn	: in bit;
			GL_BGACK_OUTn	: out bit;

			-- Adress and data bus:
			GL_ADDRESS		: in bit_vector(23 downto 1);
			-- ST: put the data bus to 1 downto 0. 
			-- STE: put the data out bus to 15 downto 0. 
			GL_DATA_IN		: in std_logic_vector(7 downto 0);
			GL_DATA_OUT		: out std_logic_vector(15 downto 0);
			GL_DATA_EN		: out bit;
			
			-- Asynchronous bus control:
			GL_RWn_IN		: in bit;
			GL_RWn_OUT		: out bit;
			GL_AS_INn		: in bit;
			GL_AS_OUTn		: out bit;
			GL_UDS_INn		: in bit;
			GL_UDS_OUTn		: out bit;
			GL_LDS_INn		: in bit;
			GL_LDS_OUTn		: out bit;
			GL_DTACK_INn	: in bit;
			GL_DTACK_OUTn	: out bit;
			GL_CTRL_EN		: out bit;
			
			-- System control:
			GL_RESETn		: in bit;
			GL_BERRn		: out bit;
			
			-- Processor function codes:
			GL_FC	: in bit_vector(2 downto 0);

			-- STE enhancements:
			GL_STE_FDDS		: out bit; 		-- Floppy type select (HD or DD).
			GL_STE_FCCLK	: out bit; 		-- Floppy controller clock select.
			GL_STE_JOY_RHn	: out bit; 		-- Read only FF9202 high byte.
			GL_STE_JOY_RLn	: out bit; 		-- Read only FF9202 low byte.
			GL_STE_JOY_WL	: out bit; 		-- Write only FF9202 low byte.
			GL_STE_JOY_WEn	: out bit; 		-- Write only FF9202 output enable.
			GL_STE_BUTTONn	: out bit; 		-- Read only FF9000 low byte.		
			GL_STE_PAD0Xn	: in bit; 		-- Counter input for the Paddle 0X.
			GL_STE_PAD0Yn	: in bit; 		-- Counter input for the Paddle 0Y.
			GL_STE_PAD1Xn	: in bit; 		-- Counter input for the Paddle 1X.
			GL_STE_PAD1Yn	: in bit; 		-- Counter input for the Paddle 1Y.
			GL_STE_PADRSTn	: out bit; 		-- Paddle monoflops reset.
			GL_STE_PENn		: in bit; 		-- Input of the light pen.
			GL_STE_SCCn		: out bit;	-- Select signal for the STE or TT SCC chip.
			GL_STE_CPROGn	: out bit	-- Select signal for the STE's cache processor.
			);
	end component WF25915IP_TOP_V1_SOC;

	component WF25912IP_SD_TOP_SOC
		port(  
			CLK				: in bit;

			RESET_INn		: in bit;
			RESET_OUTn		: out bit;

			ASn				: in bit;
			LDSn, UDSn		: in bit;
			RWn				: in bit;
			ADR				: in bit_vector(25 downto 1);
			RAMn			: in bit;
			DMAn			: in bit;
			DEVn			: in bit;
			DMA_SYNC		: out bit;
			VSYNCn			: in bit;
			DE				: in bit;
			VIDEO_HIMODE    : in bit;
			DCYCn			: out bit;
			CMPCSn			: out bit;
			MONO_DETECTn	: in bit;
			EXT_CLKSELn		: in bit;
			SREQ			: in bit;
			SLOADn			: out bit;
			SINT_TAI		: out bit;
			SINT_IO7		: out bit;
			BA				: out bit_vector(1 downto 0);
			MAD				: out bit_vector(12 downto 0);
			WEn				: out bit;
			DQM0H			: out bit;
			DQM0L			: out bit;
			DQM1H			: out bit;
			DQM1L			: out bit;
			RAS0n			: out bit;
			RAS1n			: out bit;
			CAS0n			: out bit;
			CAS1n			: out bit;
			RDATn			: out bit;
			WDATn			: out bit;
			LATCHn			: out bit;
			DTACKn			: out bit;
			DATA_IN			: in std_logic_vector(7 downto 0);
			DATA_OUT		: out std_logic_vector(7 downto 0);
			DATA_EN			: out bit
		);
	end component;

	component WF25913IP_TOP_SOC -- DMA.
		port (
			-- system controls:
		    RESETn		: in bit;	-- Master reset.
			CLK			: in bit; 	-- Clock system.
			FCSn    	: in bit;	-- Adress select.
            A1  		: in bit;	-- Adress select.
			RWn			: in bit;	-- Read write control.
			RDY_INn		: in bit;	-- Data acknowlege control (GLUE-DMA).
			RDY_OUTn	: out bit;	-- Data acknowlege control (GLUE-DMA).
			DATA_IN		: in std_logic_vector(15 downto 0);	-- System data.
			DATA_OUT	: out std_logic_vector(15 downto 0);	-- System data.
			DATA_EN		: out bit;

            -- DMA-Configuration:
            DMA_SRC_SEL : out bit_vector(1 downto 0);

			-- ACSI section:
			CA2			: out bit;	-- ACSI adress.
			CA1			: out bit;	-- ACSI adress.
			CR_Wn		: out bit;	-- ACSI read write control.
			CD_IN		: in std_logic_vector(7 downto 0);	-- ACSI data.
			CD_OUT		: out std_logic_vector(7 downto 0);	-- ACSI data.
			CD_EN		: out bit; 	-- CD data enable.
            FDCSn		: out bit;	-- FLOPPY select.
            SDCSn       : out bit;  -- SD card select.
            SCSICSn     : out bit;  -- SCSI device select.
            HDCSn		: out bit;	-- ACSI drive select.
			FDRQ		: in bit;	-- FLOPPY request.
			HDRQ		: in bit;	-- ACSI drive request.
			ACKn		: out bit	-- ACSI data acknowledge.
			);
	end component WF25913IP_TOP_SOC;

	component WF1772IP_TOP_SOC -- FDC.
		port (
			CLK			: in bit; -- 16MHz clock!
			RESETn		: in bit;
			CSn			: in bit;
			RWn			: in bit;
			A1, A0		: in bit;
			DATA_IN		: in std_logic_vector(7 downto 0);
			DATA_OUT	: out std_logic_vector(7 downto 0);
			DATA_EN		: out bit;
			RDn			: in bit;
			TR00n		: in bit;
			IPn			: in bit;
			WPRTn		: in bit;
			DDEn		: in bit;
			HDTYPE		: in bit; -- '0' = DD disks, '1' = HD disks.
			MO			: out bit;
			WG			: out bit;
			WD			: out bit;
			STEP		: out bit;
			DIRC		: out bit;
			DRQ			: out bit;
			INTRQ		: out bit
		);
	end component WF1772IP_TOP_SOC;

	component WF25914IP_TOP_SOC -- Shifter.
		port (
			CLK				: in bit; -- Originally 32MHz in the ST machines.
		    RESETn		    : in bit; -- Master reset.
			SH_A			: in bit_vector(6 downto 1); -- Adress bus (without base adress).
			SH_D_IN			: in std_logic_vector(15 downto 0); -- Data bus input.
			SH_D_OUT		: out std_logic_vector(15 downto 0); -- Data bus output.
			SH_DATA_HI_EN	: out bit; -- Data output enable for the high byte.
			SH_DATA_LO_EN	: out bit; -- Data output enable for the low byte.
			SH_RWn			: in bit; -- Write to registers is low active.
			SH_CSn			: in bit; -- Base adress of the shifter is 0xFF82xx.

			MULTISYNC		: in bit_vector(3 downto 2); -- Select multisync compatible video modi.
			SH_LOADn		: in bit; -- Load signal for the shift registers.
			SH_DE			: in bit; -- Shift switch for the shift registers.
			SH_BLANKn		: in bit; -- Blanking input.
			CR_1512		: out bit_vector(3 downto 0); -- Hi nibble of the chroma out.
			SH_R			: out bit_vector(3 downto 0); -- Red video output.
			SH_G			: out bit_vector(3 downto 0); -- Green video output.
			SH_B			: out bit_vector(3 downto 0); -- Blue video output.
			SH_MONO			: out bit; -- Monochrome video output.
			SH_COLOR		: out bit; -- COMP_SYNC signal of the ST.
			
			SH_SCLK			: in bit; -- Sample clock, 1.6021226 MHz.
			SH_FCLK			: out bit; -- Frame clock.
			SH_SLOADn		: in bit; -- DMA load control.
			SH_SREQ			: out bit; -- DMA load request.
			SH_SDATA_L		: out bit_vector(7 downto 0); -- Left audio data.
			SH_SDATA_R		: out bit_vector(7 downto 0); -- Right audio data.

			SH_MWK			: out bit; -- Microwire interface, clock.
			SH_MWD			: out bit; -- Microwire interface, data.
			SH_MWEn			: out bit; -- Microwire interface, enable.
			
			xFF827E_D		: out bit_vector(7 downto 0)
		);
	end component WF25914IP_TOP_SOC;

	component WF_SHD101775IP_TOP_SOC -- Shadow.
		port (
			RESETn		: in bit;
			CLK			: in bit; -- 16MHz, same as MCU clock.

			-- Video control:
			M_DATA		: in bit_vector(15 downto 0); -- Data of the shared system RAM.
			DE			: in bit; -- Video Data enable.
			LOADn		: in bit; -- Video data load control.
			
			-- VIDEO RAM:
			-- The core is written for use of a KM681000 SRAM.
			-- If smaller ones are used, do not connect A16,
			-- A15 and if not necessary CS and CSn.
			R_ADR		: out bit_vector(14 downto 0);
			R_DATA_IN	: in std_logic_vector(7 downto 0);
			R_DATA_OUT	: out std_logic_vector(7 downto 0);
            R_DATA_EN   : out bit;
			R_WRn		: out bit;
			
			-- LCD control:
			UDATA		: out bit_vector(3 downto 0);
			LDATA		: out bit_vector(3 downto 0);
			LFS			: out bit; -- Line frame strobe.
			VDCLK		: out bit; -- Video data clock.
			LLCLK		: out bit -- Line latch clock.
		);
	end component WF_SHD101775IP_TOP_SOC;

	component WF68901IP_TOP_SOC -- MFP.
		port (  -- System control:
				CLK			: in bit;
				RESETn		: in bit;
				
				-- Asynchronous bus control:
				DSn			: in bit;
				CSn			: in bit;
				RWn			: in bit;
				DTACKn		: out bit;
				
				-- Data and Adresses:
				RS			: in bit_vector(5 downto 1);
				DATA_IN		: in std_logic_vector(7 downto 0);
				DATA_OUT	: out std_logic_vector(7 downto 0);
				DATA_EN		: out bit;
				GPIP_IN		: in bit_vector(7 downto 0);
				GPIP_OUT	: out bit_vector(7 downto 0);
				GPIP_EN		: out bit_vector(7 downto 0);
				
				-- Interrupt control:
				IACKn		: in bit;
				IEIn		: in bit;
				IEOn		: out bit;
				IRQn		: out bit;
				
				-- Timers and timer control:
				XTAL1		: in bit; -- Use an oszillator instead of a quartz.
				TAI			: in bit;
				TBI			: in bit;
				TAO			: out bit;			
				TBO			: out bit;			
				TCO			: out bit;			
				TDO			: out bit;			
				
				-- Serial I/O control:
				RC			: in bit;
				TC			: in bit;
				SI			: in bit;
				SO			: out bit;
				SO_EN		: out bit;
				
				-- DMA control:
				RRn			: out bit;
				TRn			: out bit			
		);
	end component WF68901IP_TOP_SOC;

	component WF2149IP_TOP_SOC -- Sound.
		port(
			
			SYS_CLK		: in bit; -- Read the inforation in the header!
			RESETn   	: in bit;

			WAV_CLK		: in bit; -- Read the inforation in the header!
			SELn		: in bit;
			
			BDIR		: in bit;
			BC2, BC1	: in bit;

			A9n, A8		: in bit;
			DA_IN		: in std_logic_vector(7 downto 0);
			DA_OUT		: out std_logic_vector(7 downto 0);
			DA_EN		: out bit;
			
			IO_A_IN		: in bit_vector(7 downto 0);
			IO_A_OUT	: out bit_vector(7 downto 0);
			IO_A_EN		: out bit;
			IO_B_IN		: in bit_vector(7 downto 0);
			IO_B_OUT	: out bit_vector(7 downto 0);
			IO_B_EN		: out bit;

			OUT_A		: out bit; -- Analog (PWM) outputs.
			OUT_B		: out bit;
			OUT_C		: out bit
		);
	end component WF2149IP_TOP_SOC;

	component WF6850IP_TOP_SOC -- ACIA.
	  port (
			CLK					: in bit;
	        RESETn				: in bit;

	        CS2n, CS1, CS0		: in bit;
	        E		       		: in bit;   
	        RWn              	: in bit;
	        RS					: in bit;

	        DATA_IN		        : in std_logic_vector(7 downto 0);   
	        DATA_OUT	        : out std_logic_vector(7 downto 0);   
			DATA_EN				: out bit;

	        TXCLK				: in bit;
	        RXCLK				: in bit;
	        RXDATA				: in bit;
	        CTSn				: in bit;
	        DCDn				: in bit;
	        
	        IRQn				: out bit;
	        TXDATA				: out bit;   
	        RTSn				: out bit
	       );                                              
	end component WF6850IP_TOP_SOC;

	component WF5C15_139xIP_TOP -- RP5C15_DS1392 RTC bridge.
		port(
			CLK			: in bit;
			RESETn		: in bit;

			ADR			: in bit_vector(3 downto 0);
			DATA_IN		: in std_logic_vector(3 downto 0);
			DATA_OUT	: out std_logic_vector(3 downto 0);
			DATA_EN		: out bit;
			CS, CSn		: in bit;
			WRn, RDn	: in bit;

			SPI_IN			: in bit;
			SPI_OUT			: out bit;
			SPI_ENn			: out bit;
			SPI_SCL			: out bit;
			SPI_CSn			: out bit
			);
	end component WF5C15_139xIP_TOP;

	component WF_ACSI_SCSI_IF_SOC
		port (  
			RESETn			: in bit;
			CLK				: in bit;
			CR_Wn			: in bit;
			CA1				: in bit;
			HDCSn			: in bit;
			HDACKn			: in bit;
			HDINTn			: out bit;
			HDRQn			: out bit;
			ACSI_D_IN		: in bit_vector(7 downto 0);
			ACSI_D_OUT		: out bit_vector(7 downto 0);
			ACSI_D_EN		: out bit;
			SCSI_BUSYn		: in bit;
			SCSI_MSGn		: in bit;
			SCSI_REQn		: in bit;
			SCSI_DCn		: in bit;
			SCSI_IOn		: in bit;
			SCSI_RSTn		: out bit;
			SCSI_ACKn		: out bit;
			SCSI_SELn		: out bit;
			SCSI_DPn		: out bit;
			SCSI_D_IN		: in bit_vector(7 downto 0);
			SCSI_D_OUT		: out bit_vector(7 downto 0);
			SCSI_D_EN		: out bit;
			SCSI_CTRL_EN	: out bit;
			SCSI_IDn		: in bit_vector(3 downto 1)
		);
	end component WF_ACSI_SCSI_IF_SOC;

	component WF_IDE
		port (
			RESETn			: in bit;
			ADR				: in bit_vector(23 downto 4);
			ASn				: in bit;
            LDSn            : in bit;
            RWn				: in bit;
            DMAn            : in bit;
            DTACKn			: out bit;
			ACSI_HDINTn		: out bit;
			IDE_INTRQ		: in bit;
			IDE_IORDY		: in bit;
			IDE_RESn		: out bit;
			CS0n			: out bit;
			CS1n			: out bit;
			IORDn			: out bit;
			IOWRn			: out bit;
			IDE_D_EN_INn	: out bit;
			IDE_D_EN_OUTn	: out bit
	      );
	end component WF_IDE;

	component WF_SD_CARD
		port (
			RESETn			: in bit;
			CLK				: in bit;
			ACSI_A1			: in bit;
			ACSI_CSn		: in bit;
			ACSI_ACKn		: in bit;
			ACSI_INTn		: out bit;
			ACSI_DRQn		: out bit;
			ACSI_D_IN		: in std_logic_vector(7 downto 0);
			ACSI_D_OUT		: out std_logic_vector(7 downto 0);
			ACSI_D_EN		: out bit;
			MC_DO			: in bit;
			MC_PIO_DMAn		: in bit;
			MC_RWn			: in bit;
			MC_CLR_CMD		: in bit;
			MC_DONE			: out bit;
			MC_GOT_CMD		: out bit;
			MC_D_IN			: in std_logic_vector(7 downto 0);
			MC_D_OUT		: out std_logic_vector(7 downto 0);
			MC_D_EN			: out bit
	      );
	end component WF_SD_CARD;

	component WF_FLASHBOOT
		port(
			CLK				: in bit;
			PLL_LOCK		: in bit;
			RESET_COREn		: in bit;
			RESET_INn		: in bit;
			RESET_OUTn		: out bit;
			ROM_CEn			: in bit;
			ADR_OUT			: out std_logic_vector(23 downto 0);
			ADR_EN			: out bit;
			DATA_IN			: in std_logic_vector(15 downto 0);
			DATA_OUT		: out std_logic_vector(15 downto 0);
			DATA_EN			: out bit;
			FLASH_RDY		: in bit;
			FLASH_RESETn	: out bit;
			FLASH_WEn		: out bit;
			FLASH_OEn		: out bit;
			FLASH_CEn		: out bit;
			SPI_CLK			: in bit;
			SPI_DIN			: in bit;
			SPI_DOUT		: out bit;
			BOOT_ACK		: in bit;
			BOOT_REQ		: out bit;
			BOOT_LED		: out bit
		);
	end component WF_FLASHBOOT;

	component WF_AUDIO_DAC
		port (
			CLK				: in bit;
			RESETn			: in bit;
			SDATA_L			: in bit_vector(7 downto 0);
			SDATA_R			: in bit_vector(7 downto 0);
			DAC_SCLK		: out bit;
			DAC_SDATA		: out bit;
			DAC_SYNCn		: out bit;
			DAC_LDACn		: out bit
		);
	end component WF_AUDIO_DAC;
end WF_SUSKA_PKG;
