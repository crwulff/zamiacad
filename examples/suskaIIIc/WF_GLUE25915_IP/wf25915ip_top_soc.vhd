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
---- Top level file for use in systems on programmable chips.     ----
---- To guarantee proper operation of the DMA interchange between ----
---- MCU, GLUE, DMA, the 8MHz clock edges must have a small delay ----
---- (one logic element delay) to the clock edges of the 16MHz    ----
---- clock.                                                       ----
----                                                              ----
---- Important Notice concerning the clock system:                ----
---- To use this code in a stand alone GLUE chip or in a system   ----
---- on a programmable chip (SOC), the clock frequency may be     ----
---- selected via the CLKSEL setting. Use CLK_8M for the original ----
---- GLUE frequency (8MHz) or CLK_16M for the 16MHz SOC-GLUE.     ----
---- Affected by the clock selection is the video timing and the  ----
---- paddle counter in the STE enhancements file.                 ----
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
-- Revision 2K6B	2006/11/05 WF
--   Modified Source to compile with the Xilinx ISE.
--   Top level file provided for SOC (systems on programmable chips).
-- Revision 2K7A  2007/01/02 WF
--   Changes to the clock system and related
--   hardware as video timing or paddles.
-- Revision 2K8B  2008/12/24 WF
--   Introduced EN_RAM_14MB.
-- 

library work;
use work.wf25915ip_pkg.all;
library ieee;
use ieee.std_logic_1164.all;

entity WF25915IP_TOP_SOC is
	-- TOS operating system configuration:
	-- TOS_CONFIG = 0 for TOS 2.05 or higher in STE machines.
	-- TOS_CONFIG = 1 for TOS 1.62 or lower in ST machines.
	-- TOS_CONFIG = 2 for TOS 2.05 or higher (patched version in 6 512kB EPROMS) in ST machines.
		-- Explanation for the patched mode: the TOS 2.05 or 2.06 is installed on older ST machines
		-- in four 512 MBit EPROMS. Additionally there is a part of the older TOS 1.62 or lower in
		-- two 512 MBit EPROMS. It is patched. To get the machine working, it is necessary to control
		-- the DTACKn in a way, that it is asserted in the old TOS RAM space and in the new one. This
		-- behavior is controlled via the PATCHn. For further information, the TOS adaption is
		-- described in detail in c't 1992 Heft1; "Das zweite Gesicht" and in c't 1993 Heft 1
		-- "Teile und rueste auf".
	-- TOS_CONFIG = 3 or higher: reserved do not select this choices; all chip selects disabled.
	generic (TOS_CONFIG : integer range 0 to 7 := 0;
             ROMSEL_FC_E0n   : in bit := '0'; -- '1' for TOS 1.x, '0' for TOS 2.x address space (core only).
			 CLKSEL : CLKSEL_TYPE := CLK_16M);
	port (
	    -- Clock system:
		GL_CLK		: in bit; -- Originally 8MHz.
		GL_CLK_016	: in bit; -- One sixteenth of GL_CLK.
		
		-- Adress decoder outputs:
		GL_ROM_6n	: out bit;	-- STE.
		GL_ROM_5n	: out bit;	-- STE.
		GL_ROM_4n	: out bit;	-- ST.
		GL_ROM_3n	: out bit;	-- ST.
		GL_ROM_2n	: out bit;
		GL_ROM_1n	: out bit;
		GL_ROM_0n	: out bit;
		
        EN_RAM_14MB : in bit; -- '1' = 14MB RAM address space.

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
		GL_GI2n			: out bit; 	-- ST.
		GL_GI1n			: out bit; 	-- ST.
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
		GL_DATA_IN		: in std_logic_vector(1 downto 0);
		-- ST: put the data out bus to 1 downto 0. 
		-- STE: put the data out bus to 15 downto 0. 
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
		GL_FC	: in std_logic_vector(2 downto 0);

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
end entity WF25915IP_TOP_SOC;
	
architecture STRUCTURE of WF25915IP_TOP_SOC is
signal DATA_OUT_VT		: std_logic_vector(1 downto 0);
signal DATA_OUT_ENH		: std_logic_vector(15 downto 0);
signal DATA_EN_VT		: bit;
signal DATA_EN_ENH		: bit;
signal ROM_6_In			: bit;
signal ROM_5_In			: bit;
signal ROM_4_In			: bit;
signal ROM_3_In			: bit;
signal ROM_2_In			: bit;
signal ROM_1_In			: bit;
signal ROM_0_In			: bit;
signal PATCH_In			: bit;
signal SNDCS_In			: bit;
signal FCS_In			: bit;
signal HD_REG_CS_In		: bit;
signal SYNCMODE_CS_In	: bit;
signal SHIFTMODE_CS_In	: bit;
signal DMA_MODE_CS_In	: bit;
signal DMA_In			: bit;
signal BERR_In 			: bit;
signal VPA_In			: bit; -- VPAn is used also for autovectoring (AVECn).
signal VMA_In			: bit;
signal AVEC_In			: bit; -- The newer MC68EC000 use this signal instead of VPAn.
signal DE_I				: bit;
signal FC_I				: bit_vector(2 downto 0);
signal CTRL_EN_I		: bit;
signal AS_OUTn			: bit;
signal RWn_OUT			: bit;
signal UDS_OUTn			: bit;
signal LDS_OUTn			: bit;
signal STE_EINT3n		: bit;
signal STE_EINT5n		: bit;
signal STE_EINT7n		: bit;
signal RTCCS_In			: bit;
signal JOY_CS_I			: bit;
signal PAD0X_CS_I		: bit;
signal PAD0Y_CS_I		: bit;
signal PAD1X_CS_I		: bit;
signal PAD1Y_CS_I		: bit;
signal BUTTON_CS_I		: bit;
signal XPEN_REG_CS_I	: bit;
signal YPEN_REG_CS_I	: bit;
signal STE_PAD0Xn		: bit;
signal STE_PAD0Yn		: bit;
signal STE_PAD1Xn		: bit;
signal STE_PAD1Yn		: bit;
signal STE_PENn			: bit;
signal GL_STE_SCC_In	: bit;
signal GL_STE_CPROG_In	: bit;
signal BR_In			: bit;
begin
	-- TOS configuration:
	-- The configuration of the TOS operating system is done in the
	-- wf25915ip_adrdec file via a generic statement.
	-- Have a look at the beginning of the entity in this file.
	
	-- Configuration:
	--------------------------------------------
	-- Comment these lines out for ST features:
--	STE_EINT3n 	<= '1';	
--	STE_EINT5n 	<= '1';
--	STE_EINT7n 	<= '1';
--	STE_PAD0Xn 	<= '1';
--	STE_PAD0Yn 	<= '1';
--	STE_PAD1Xn 	<= '1';
--	STE_PAD1Yn 	<= '1';
--	STE_PENn 	<= '1';
--	GL_DATA_OUT <= DATA_OUT_VT;
--	GL_DATA_EN 	<= DATA_EN_VT;
	--------------------------------------------
	--------------------------------------------
	-- Comment these lines out for STE features:
	STE_EINT3n 		<= GL_STE_EINT3n;
	STE_EINT5n 		<= GL_STE_EINT5n;
	STE_EINT7n 		<= GL_STE_EINT7n;
	STE_PENn 		<= GL_STE_PENn;
	STE_PAD0Xn 		<= GL_STE_PAD0Xn;
	STE_PAD0Yn 		<= GL_STE_PAD0Yn;
	STE_PAD1Xn 		<= GL_STE_PAD1Xn;
	STE_PAD1Yn 		<= GL_STE_PAD1Yn;
	GL_DATA_OUT(15 downto 2) <= DATA_OUT_ENH(15 downto 2) when DATA_EN_ENH = '1' else (others => '0');
	GL_DATA_OUT(1 downto 0)  <= DATA_OUT_ENH(1 downto 0) when DATA_EN_ENH = '1' else
								DATA_OUT_VT when DATA_EN_VT = '1' else "00";
	GL_DATA_EN 	<= DATA_EN_ENH or DATA_EN_VT;
	--------------------------------------------
	-- End configuration
	FC_I <= To_BitVector(GL_FC);

	GL_DE 		<= DE_I;
	
	GL_BRn <= BR_In;

	GL_BERRn <= BERR_In;
	GL_AVECn <= AVEC_In;
	-- Use the following statement for CPUs which do provide the AVECn signal.
	GL_VPAn <= 	'0' when VPA_In = '0' else '1';
	-- Use the following statement for CPUs not providing the AVECn signal:
--	GL_VPAn <= 	'0' when VPA_In = '0' else
--				'0' when AVEC_In = '0' else '1';

	VMA_In <= GL_VMAn;

	GL_ROM_6n <= ROM_6_In;
	GL_ROM_5n <= ROM_5_In;
	GL_ROM_4n <= ROM_4_In;
	GL_ROM_3n <= ROM_3_In;
	GL_ROM_2n <= ROM_2_In;
	GL_ROM_1n <= ROM_1_In;
	GL_ROM_0n <= ROM_0_In;
	
	GL_SNDCSn 	<= SNDCS_In;
	GL_FCSn 	<= FCS_In;
	GL_DMAn 	<= DMA_In;

	-- STE features:
	GL_STE_DINTn 	<= '1' when GL_STE_HDINTn = '1' and GL_STE_FDINT = '0' else '0';
	GL_STE_SNDCS 	<= '1' when SNDCS_In = '0' and GL_ADDRESS(1) = '0' else '0';
	GL_STE_SNDIR 	<= '1' when GL_RWn_IN = '0' and SNDCS_In = '0' else '0';
	GL_STE_RTCCSn 	<= RTCCS_In;
	GL_STE_RTC_RDn 	<= '0' when GL_RWn_IN = '1' and GL_LDS_INn = '0' and VMA_In = '0' else '1';
	GL_STE_RTC_WRn 	<= '0' when GL_RWn_IN = '0' and GL_LDS_INn = '0' and VMA_In = '0' else '1';
	GL_STE_JOY_RHn 	<= '0' when JOY_CS_I = '1' and GL_RWn_IN = '1' and GL_UDS_INn = '0' else '1'; 
	GL_STE_JOY_RLn 	<= '0' when JOY_CS_I = '1' and GL_RWn_IN = '1' and GL_LDS_INn = '0' else '1'; 
	GL_STE_JOY_WL 	<= '1' when JOY_CS_I = '1' and GL_RWn_IN = '0' and GL_LDS_INn = '0' else '0'; 
	GL_STE_JOY_WEn 	<= '1' when JOY_CS_I = '1' and GL_RWn_IN = '1' and GL_LDS_INn = '0' else '0'; 
	GL_STE_BUTTONn 	<= '0' when BUTTON_CS_I = '1' else '1'; 	

	-- Comment out for STE:
	-- There are no DTACKn for SHIFTMODE register x"8260" and DMA_MODE
	-- register x"8606" necessary, because SHIFTMODE is a mirror 
	-- register of the SHIFTER and DMA_MODE is a shadow of the DMA chip
	-- register. The DTACKn is done for the SHIFTMODE via SHIFTER register
	-- control and for the DMA_MODE via the DMA's RDYn control signal.
	-- During FDC access RDYn indicates DTACKn.
	GL_DTACK_OUTn <= 	'0' when ROM_6_In = '0' 		else
						'0' when ROM_5_In = '0' 		else
						'0' when ROM_4_In = '0' 		else
						'0' when ROM_3_In = '0' 		else
						'0' when ROM_2_In = '0' 		else
						'0' when ROM_1_In = '0' 		else
						'0' when ROM_0_In = '0' 		else
						'0' when PATCH_In = '0'			else
						'0' when SNDCS_In = '0' 		else
						'0' when SYNCMODE_CS_In = '0' 	else
						'0' when RTCCS_In = '0' 		else
						'0' when HD_REG_CS_In = '0'		else
						'0' when GL_STE_SCC_In = '0' 	else
						'0' when GL_STE_CPROG_In = '0' 	else
						'0' when JOY_CS_I = '1' 		else
						'0' when PAD0X_CS_I = '1' 		else
						'0' when PAD0Y_CS_I = '1' 		else
						'0' when PAD1X_CS_I = '1' 		else
						'0' when PAD1Y_CS_I = '1' 		else
						'0' when BUTTON_CS_I = '1' 		else
						'0' when XPEN_REG_CS_I = '1' 	else
						'0' when YPEN_REG_CS_I = '1' 	else
						-- RDYn indicates FDC ok:
						'0' when FCS_In = '0' and GL_RDY_INn = '1' else '1';

	-- Comment out for ST:
	-- There are no DTACKn for SHIFTMODE register x"8260" and DMA_MODE
	-- register x"8606" necessary, because SHIFTMODE is a mirror 
	-- register of the SHIFTER and DMA_MODE is a shadow of the DMA chip
	-- register. The DTACKn is done for the SHIFTMODE via SHIFTER register
	-- control and for the DMA_MODE via the DMA's RDYn control signal.
	-- During FDC access RDYn indicates DTACKn.
--	GL_DTACK_OUTn <=	'0' when ROM_4_In = '0' 		else
--						'0' when ROM_3_In = '0' 		else
--						'0' when ROM_2_In = '0' 		else
--						'0' when ROM_1_In = '0' 		else
--						'0' when ROM_0_In = '0' 		else
--						'0' when PATCH_In = '0' 		else
--						'0' when SNDCS_In = '0' 		else
--						'0' when SYNCMODE_CS_In = '0' 	else
--						'0' when RTCCS_In = '0' 		else
						-- RDYn indicates FDC ok:
--						'0' when FCS_In = '0' and GL_RDY_INn = '1' else '1';

	-- Bus controls (three state):
	GL_AS_OUTn 	<=  AS_OUTn when CTRL_EN_I = '1' else '0';
	GL_RWn_OUT 	<=  RWn_OUT when CTRL_EN_I = '1' else '0';
	GL_UDS_OUTn <=  UDS_OUTn when CTRL_EN_I = '1' else '0';
	GL_LDS_OUTn <=  LDS_OUTn when CTRL_EN_I = '1' else '0';
	GL_CTRL_EN	<= CTRL_EN_I;

	-- STE stuff:
	GL_STE_SCCn <= GL_STE_SCC_In;
	GL_STE_CPROGn <=GL_STE_CPROG_In;

	I_INTERRUPT: WF25915IP_INTERRUPTS
	port map(
		RESETn		=> GL_RESETn,
		CLK 		=> GL_CLK,
		ADR_HI 		=> GL_ADDRESS(19 downto 16),
		ADR_LO 		=> GL_ADDRESS(3 downto 1),
		FC 			=> FC_I,
		ASn 		=> GL_AS_INn,
		DMA_LOCKn	=> DMA_In,
		EINT3n		=> STE_EINT3n, 		-- STE GLUE.
		EINT5n		=> STE_EINT5n, 		-- STE GLUE.
		EINT7n		=> STE_EINT7n, 		-- STE GLUE.
		MFPINTn 	=> GL_MFPINTn,
		HSYNCn		=> GL_HSYNC_INn,
		VSYNCn 		=> GL_VSYNC_INn,
		AVECn 		=> AVEC_In,
		IACKn 		=> GL_IACKn,
		GI2n 		=> GL_GI2n,
		GI1n 		=> GL_GI1n,
		IPLn(2)		=> GL_STE_IPL2n, 	-- STE GLUE.
		IPLn(1)		=> GL_STE_IPL1n, 	-- STE GLUE.
 		IPLn(0)		=> GL_STE_IPL0n 	-- STE GLUE.
	);
			
	I_ADRDEC: WF25915IP_ADRDEC
	port map(
		ADR 			=> GL_ADDRESS,
		RWn 			=> GL_RWn_IN,

		RESETn 			=> GL_RESETn,

        TOS_CONFIG		=> TOS_CONFIG,
        ROMSEL_FC_E0n   => ROMSEL_FC_E0n,
        EN_RAM_14MB     => EN_RAM_14MB,

		LDSn 			=> GL_LDS_INn,
		UDSn 			=> GL_UDS_INn,
				
		ASn 			=> GL_AS_INn,
		VPAn 			=> VPA_In,
		VMAn 			=> VMA_In,	

		FC 				=> FC_I,

		DMAn 			=> DMA_In,
		DMA_LOCKn		=> DMA_In,
		
		ROM_0n 			=> ROM_0_In,
		ROM_1n 			=> ROM_1_In,
		ROM_2n 			=> ROM_2_In,
		ROM_3n 			=> ROM_3_In,
		ROM_4n 			=> ROM_4_In,
		ROM_5n 			=> ROM_5_In,
		ROM_6n 			=> ROM_6_In,
		PATCHn			=> PATCH_In,

		ACIACS 			=> GL_ACIACS,
		MFPCSn 			=> GL_MFPCSn,
		SNDCSn 			=> SNDCS_In,
		FCSn 			=> FCS_In,
		SCCn			=> GL_STE_SCC_In,
		CPROGn			=> GL_STE_CPROG_In,
		HD_REG_CSn		=> HD_REG_CS_In,
		RTCCSn			=> RTCCS_In,
		SYNCMODE_CSn 	=> SYNCMODE_CS_In,
		SHIFTMODE_CSn	=> SHIFTMODE_CS_In,
    	DMA_MODE_CSn 	=> DMA_MODE_CS_In,

		JOY_CS	 		=> JOY_CS_I,

		PAD0X_CS	 	=> PAD0X_CS_I,
		PAD0Y_CS 		=> PAD0Y_CS_I,
		PAD1X_CS 		=> PAD1X_CS_I,
		PAD1Y_CS 		=> PAD1Y_CS_I,

		BUTTON_CS 		=> BUTTON_CS_I,

		XPEN_REG_CS 	=> XPEN_REG_CS_I,
		YPEN_REG_CS		=> YPEN_REG_CS_I,
		
		DEVn 			=> GL_DEVn,
		RAMn 			=> GL_RAMn
	);

	I_VIDEO: WF25915IP_VIDEO_TIMING
	port map(
		RESETn			        => GL_RESETn,
		CLK 			        => GL_CLK,
		CLKSEL			        => CLKSEL,

		DATA_IN(7 downto 2) 	=> "000000",
		DATA_IN(1 downto 0) 	=> GL_DATA_IN(1 downto 0),
		DATA_OUT	            => DATA_OUT_VT,
		DATA_EN			        => DATA_EN_VT,
		RWn 			        => GL_RWn_IN,
		SYNCMODE_CSn	        => SYNCMODE_CS_In,
		SHIFTMODE_CSn 	        => SHIFTMODE_CS_In,
		DE 				        => DE_I,
		BLANKn 			        => GL_BLANKn,

		VSYNC_INn		        => GL_VSYNC_INn,
		HSYNC_INn		        => GL_HSYNC_INn,
		VSYNC_OUTn		        => GL_VSYNC_OUTn,
		HSYNC_OUTn		        => GL_HSYNC_OUTn,
		SYNC_OUT_EN		        => GL_SYNC_OUT_EN
	);

	I_ErrorHandler: WF25915IP_ERRHANDLE
	port map(
		RESETn		=> GL_RESETn,
		CLK			=> GL_CLK,
		ASn			=> GL_AS_INn,
		BERRn		=> BERR_In
	);

	I_Arbitration: WF25915IP_BUS_ARBITER
	port map(
	 	RESn 			=> GL_RESETn,
		CLK 			=> GL_CLK,

		D8 				=> GL_DATA_IN(0), -- Input only.

		DTACKn 			=> GL_DTACK_INn,
		DE				=> DE_I,

		AS_INn 			=> GL_AS_INn,
		RWn_IN 			=> GL_RWn_IN,
		AS_OUTn			=> AS_OUTn,
		RWn_OUT			=> RWn_OUT,
		LDS_OUTn		=> LDS_OUTn,
		UDS_OUTn		=> UDS_OUTn,
		CTRL_EN			=> CTRL_EN_I,
		-- RDY_EN			=>, -- Not used.

	   	DMA_MODE_CSn	=> DMA_MODE_CS_In,
	
		RDY_INn 		=> GL_RDY_INn,
		RDY_OUTn		=> GL_RDY_OUTn,
		BGACK_INn		=> GL_BGACK_INn,
		BGACK_OUTn		=> GL_BGACK_OUTn,
		BRn 			=> BR_In,
		BGIn			=> GL_BGIn,
		BGOn 			=> GL_BGOn,
		DMAn 			=> DMA_In
	);

	I_STE_ENHANCEMENTS: WF25915IP_STE_ENH
	port map(
		CLK 			=> GL_CLK,
		CLK_x1_16 		=> GL_CLK_016,
		CLKSEL			=> CLKSEL,
		RESETn 			=> GL_RESETn,
		
		RWn				=> GL_RWn_IN,
		DATA_IN 		=> GL_DATA_IN(1 downto 0),
		DATA_OUT		=> DATA_OUT_ENH,
		DATA_EN			=> DATA_EN_ENH,

		HD_REG_CSn		=> HD_REG_CS_In,
		FDDS			=> GL_STE_FDDS,
		FCCLK			=> GL_STE_FCCLK,

		PAD0X_CS 		=> PAD0X_CS_I,
		PAD0Y_CS 		=> PAD0Y_CS_I,
		PAD1X_CS 		=> PAD1X_CS_I,
		PAD1Y_CS 		=> PAD1Y_CS_I,
		
		PAD0X_INHn 		=> STE_PAD0Xn,
		PAD0Y_INHn 		=> STE_PAD0Yn,
		PAD1X_INHn 		=> STE_PAD1Xn,
		PAD1Y_INHn 		=> STE_PAD1Yn,
		PADRSTn 		=> GL_STE_PADRSTn,

		XPEN_REG_CS 	=> XPEN_REG_CS_I,
		YPEN_REG_CS		=> YPEN_REG_CS_I,
		
		HSYNCn 			=> GL_HSYNC_INn,
		VSYNCn 			=> GL_VSYNC_INn,
		DE 				=> DE_I,
		
		PENn 			=> STE_PENn
	);
end STRUCTURE;
