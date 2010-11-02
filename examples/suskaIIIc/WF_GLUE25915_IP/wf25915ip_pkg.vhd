----------------------------------------------------------------------
----                                                              ----
---- ATARI GLUE compatible IP Core					              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- Atari's ST GLUE with all features to reach                   ----
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
--   Initial Release.
-- Revision 2K6B	2006/11/05 WF
--   Modified Source to compile with the Xilinx ISE.
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- Revision 2K9A  2008/12/08 WF
--   Enhancements for the multisync compatible video modi.
-- 

library ieee;
use ieee.std_logic_1164.all;

package WF25915IP_PKG is
type CLKSEL_TYPE is (CLK_16M, CLK_8M);
-- Component declarations:
component WF25915IP_INTERRUPTS
	port(
		RESETn			: in bit;
		CLK				: in bit;
		ADR_HI			: in bit_vector(19 downto 16);
		ADR_LO			: in bit_vector(3 downto 1);
		FC				: in bit_vector(2 downto 0);
		ASn				: in bit;
		DMA_LOCKn		: in bit;
		EINT3n			: in bit; -- STE GLUE.
		EINT5n			: in bit; -- STE GLUE.
		EINT7n			: in bit; -- STE GLUE.
		MFPINTn			: in bit;
		HSYNCn			: in bit;
		VSYNCn			: in bit;
		VIDEO_HIMODE    : in bit;
		AVECn			: out bit;
		IACKn			: out bit;
		GI2n			: out bit;
		GI1n			: out bit;
		IPLn			: out bit_vector(2 downto 0) -- STE GLUE.
	);
end component;

component WF25915IP_ADRDEC
	port (
 		ADR				: in bit_vector(23 downto 1);
		RWn				: in bit;

		RESETn			: in bit;

		TOS_CONFIG		: in integer range 0 to 7;
        ROMSEL_FC_E0n   : in bit;
        EN_RAM_14MB     : in bit;

		LDSn			: in bit;
		UDSn			: in bit;

	    ASn				: in bit;
        VPAn			: out bit;
		VMAn			: in bit;

		FC				: in bit_vector(2 downto 0);

		DMAn			: in bit;
		DMA_LOCKn		: in bit;
		
	    ROM_0n			: out bit;
	    ROM_1n			: out bit;
	    ROM_2n			: out bit;
	    ROM_3n			: out bit;
	    ROM_4n			: out bit;
	    ROM_5n			: out bit;
	    ROM_6n			: out bit;
		PATCHn			: out bit;
        ACIACS			: out bit;
        MFPCSn			: out bit;
        SNDCSn			: out bit;
        FCSn			: out bit;
        SCCn			: out bit;
		CPROGn			: out bit;
		HD_REG_CSn		: out bit;
        RTCCSn			: out bit;
	    SYNCMODE_CSn	: out bit;
		SHIFTMODE_CSn	: out bit;
	    DMA_MODE_CSn	: out bit;
        DEVn			: out bit;
        RAMn			: out bit;
		JOY_CS			: out bit;
		PAD0X_CS		: out bit;
		PAD0Y_CS		: out bit;
		PAD1X_CS		: out bit;
		PAD1Y_CS		: out bit;
		BUTTON_CS		: out bit;
		XPEN_REG_CS		: out bit;
		YPEN_REG_CS		: out bit
	);
end component;

component WF25915IP_VIDEO_TIMING
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
end component;

component WF25915IP_CLOCKS
	port (
		CLK_x1			: in bit;
	
		CLK_x1_4		: out bit;
		CLK_x1_16		: out bit
	);
end component;

component WF25915IP_ERRHANDLE
	port(
		RESETn			: in bit;
		CLK				: in bit;
		ASn				: in bit;

		BERRn			: out bit
	);
end component;

component WF25915IP_BUS_ARBITER
	port ( 
		RESn			: in bit;
		CLK				: in bit;

		D8				: in std_logic;
		DE				: in bit;

		DTACKn			: in bit;
		AS_INn			: in bit;
		RWn_IN			: in bit;
		AS_OUTn			: out bit;
		RWn_OUT			: out bit;
		LDS_OUTn		: out bit;
		UDS_OUTn		: out bit;
		CTRL_EN			: out bit;
		RDY_EN			: out bit;

		RDY_INn			: in bit;
		RDY_OUTn		: out bit;
		BGACK_INn		: in bit;
		BGACK_OUTn		: out bit;
		DMA_MODE_CSn	: in bit;
		BGIn			: in bit;
		BRn				: out bit;
		BGOn			: out bit;
		DMAn			: out bit
	);
end component;

component WF25915IP_BUS_ARBITER_V1
	port ( 
		RESn			: in bit;
		CLK				: in bit;

		D8				: in std_logic;
		DMA_SYNC		: in bit;

		DTACKn			: in bit;
		AS_INn			: in bit;
		RWn_IN			: in bit;
		AS_OUTn			: out bit;
		RWn_OUT			: out bit;
		LDS_OUTn		: out bit;
		UDS_OUTn		: out bit;
		CTRL_EN			: out bit;
		RDY_EN			: out bit;

		RDY_INn			: in bit;
		RDY_OUTn		: out bit;
		BGACK_INn		: in bit;
		BGACK_OUTn		: out bit;
		DMA_MODE_CSn	: in bit;
		BGIn			: in bit;
		BRn				: out bit;
		BGOn			: out bit;
		DMAn			: out bit
	);
end component;

component WF25915IP_STE_ENH
	port(
		CLK, CLK_x1_16	: in bit;
		CLKSEL			: in CLKSEL_TYPE;
		RESETn			: in bit;
		RWn				: in bit;
		DATA_IN			: in std_logic_vector(1 downto 0);
		DATA_OUT		: out std_logic_vector(15 downto 0);
		DATA_EN			: out bit;

		HD_REG_CSn		: in bit;
		FDDS			: out bit;
		FCCLK			: out bit;

		PAD0X_CS		: in bit;
		PAD0Y_CS		: in bit;
		PAD1X_CS		: in bit;
		PAD1Y_CS		: in bit;
		
		PAD0X_INHn		: in bit;
		PAD0Y_INHn		: in bit;
		PAD1X_INHn		: in bit;
		PAD1Y_INHn		: in bit;
		PADRSTn			: out bit;

		XPEN_REG_CS		: in bit; -- Pen register access.
		YPEN_REG_CS		: in bit; -- Pen register access.
		
		HSYNCn			: in bit;
		VSYNCn			: in bit;
		DE				: in bit;
		
		PENn			: in bit -- Light pen input.
	);
end component;
end WF25915IP_PKG;
