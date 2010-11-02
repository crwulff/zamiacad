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
---- This is the Suska GLUE's IP core top level file.             ----
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
-- Revision History:
--
-- Revision 2K6A  2006/06/03 WF
--   Initial Release.
-- Revision 2K6B	2006/11/05 WF
--   Modified Source to compile with the Xilinx ISE.
-- Revision 2K7A  2007/01/02 WF
--   Changes to the clock system and related
--   hardware as video timing or paddles.
-- Revision 2K8B  2008/12/24 WF
--   Rewritten this top level file as a wrapper for the top_soc file.
--   Introduced EN_RAM_14MB.

library work;
use work.wf25915ip_pkg.all;
library ieee;
use ieee.std_logic_1164.all;

entity WF25915IP_TOP is
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
			 CLKSEL : CLKSEL_TYPE := CLK_8M);
	port (
	    -- Clock system:
		GL_CLK		    : in bit; 
		GL_CLK_2M	    : out bit;
		GL_CLK_0M5	    : out bit;
		
		-- Adress decoder outputs:
		GL_ROM_6n	    : out bit;	-- STE.
		GL_ROM_5n	    : out bit;	-- STE.
		GL_ROM_4n	    : out bit;	-- ST.
		GL_ROM_3n	    : out bit;	-- ST.
		GL_ROM_2n	    : out bit;
		GL_ROM_1n	    : out bit;
		GL_ROM_0n	    : out bit;
		
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
		GL_VPAn	        : out std_logic; -- Open drain.
		GL_VMAn         : in bit;
		
		GL_DEVn         : out bit;
		GL_RAMn         : out bit;
		GL_DMAn         : out bit;
		
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
		GL_BLANKn       : out bit;
		GL_DE           : out bit;
		GL_HSYNCn       : inout std_logic;
		GL_VSYNCn       : inout std_logic;
		
		-- Bus arbitration control:
		GL_RDYn         : inout std_logic;
		GL_BRn          : out bit;
		GL_BGIn         : in bit;
		GL_BGOn         : out bit;
		GL_BGACKn       : inout std_logic; -- Open drain.

		-- Adress and data bus:
		GL_ADDRESS      : in bit_vector(23 downto 1);
		-- ST: put the data bus to 1 downto 0. 
		-- STE: put the data bus to 15 downto 0. 
		GL_DATA         : inout std_logic_vector(15 downto 0);
		
		-- Asynchronous bus control:
		GL_RWn          : inout std_logic;
		GL_ASn          : inout std_logic;
		GL_UDSn         : inout std_logic;
		GL_LDSn         : inout std_logic;
		GL_DTACKn       : inout std_logic; -- Open drain.
		
		-- System control:
		GL_RESETn		: in bit;
		GL_BERRn        : out std_logic; -- Open drain.
		
		-- Processor function codes:
		GL_FC           : in std_logic_vector(2 downto 0);

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
        GL_STE_SCCn		: out bit;	    -- Select signal for the STE or TT SCC chip.
        GL_STE_CPROGn	: out bit	    -- Select signal for the STE's cache processor.
		);
end entity WF25915IP_TOP;
	
architecture STRUCTURE of WF25915IP_TOP is
component WF25915IP_CLOCKS
port (
  CLK_x1	: in bit;
  CLK_x1_4	: out bit;
  CLK_x1_16	: out bit
);
end component;

component WF25915IP_TOP_SOC
	port (
		GL_CLK		    : in bit;
		GL_CLK_016	    : in bit;
		GL_ROM_6n	    : out bit;
		GL_ROM_5n	    : out bit;
		GL_ROM_4n	    : out bit;
		GL_ROM_3n	    : out bit;
		GL_ROM_2n	    : out bit;
		GL_ROM_1n	    : out bit;
		GL_ROM_0n	    : out bit;
        EN_RAM_14MB     : in bit;
		GL_ACIACS	    : out bit;
		GL_MFPCSn	    : out bit;
		GL_SNDCSn	    : out bit;
		GL_FCSn		    : out bit;
		GL_STE_SNDCS	: out bit;
		GL_STE_SNDIR	: out bit;
		GL_STE_RTCCSn	: out bit;
		GL_STE_RTC_WRn	: out bit;
		GL_STE_RTC_RDn	: out bit;
		GL_VPAn	        : out bit;
		GL_VMAn         : in bit;
		GL_DEVn	        : out bit;
		GL_RAMn	        : out bit;
		GL_DMAn	        : out bit;
		GL_AVECn		: out bit;
		GL_STE_FDINT	: in bit;
		GL_STE_HDINTn	: in bit;
		GL_MFPINTn		: in bit;
		GL_STE_EINT3n	: in bit;
		GL_STE_EINT5n	: in bit;
		GL_STE_EINT7n	: in bit;
		GL_STE_DINTn	: out bit;
		GL_IACKn		: out bit;
		GL_GI2n			: out bit;
		GL_GI1n			: out bit;
		GL_STE_IPL2n	: out bit;
		GL_STE_IPL1n	: out bit;
		GL_STE_IPL0n	: out bit;
		GL_BLANKn		: out bit;
		GL_DE			: out bit;
		GL_HSYNC_INn	: in bit;
		GL_HSYNC_OUTn	: out bit;
		GL_VSYNC_INn	: in bit;
		GL_VSYNC_OUTn	: out bit;
		GL_SYNC_OUT_EN	: out bit;
		GL_RDY_INn		: in bit;
		GL_RDY_OUTn		: out bit;
		GL_BRn			: out bit;
		GL_BGIn			: in bit;
		GL_BGOn			: out bit;
		GL_BGACK_INn	: in bit;
		GL_BGACK_OUTn	: out bit;
		GL_ADDRESS		: in bit_vector(23 downto 1);
		GL_DATA_IN		: in std_logic_vector(1 downto 0);
		GL_DATA_OUT		: out std_logic_vector(15 downto 0);
		GL_DATA_EN		: out bit;
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
		GL_RESETn		: in bit;
		GL_BERRn		: out bit;
		GL_FC	        : in std_logic_vector(2 downto 0);
		GL_STE_FDDS		: out bit;
		GL_STE_FCCLK	: out bit;
		GL_STE_JOY_RHn	: out bit;
		GL_STE_JOY_RLn	: out bit;
		GL_STE_JOY_WL	: out bit;
		GL_STE_JOY_WEn	: out bit;
		GL_STE_BUTTONn	: out bit;
		GL_STE_PAD0Xn	: in bit;
		GL_STE_PAD0Yn	: in bit;
		GL_STE_PAD1Xn	: in bit;
		GL_STE_PAD1Yn	: in bit;
		GL_STE_PADRSTn	: out bit;
		GL_STE_PENn		: in bit;
		GL_STE_SCCn		: out bit;
		GL_STE_CPROGn	: out bit
		);
end component;
--
signal CLK_x1           : bit;
signal CLK_x1_4         : bit;
signal CLK_x1_16        : bit;
signal DATA_OUT         : std_logic_vector(15 downto 0);
signal DATA_EN          : bit;
signal BGACK_INn        : bit;
signal BGACK_OUTn       : bit;
signal VPA_In           : bit;
signal GL_CTRL_EN       : bit;
signal AS_INn           : bit;
signal AS_OUTn          : bit;
signal RWn_IN           : bit;
signal RWn_OUT          : bit;
signal LDS_INn          : bit;
signal LDS_OUTn         : bit;
signal UDS_INn          : bit;
signal UDS_OUTn         : bit;
signal HSYNC_INn        : bit;
signal HSYNC_OUTn       : bit;
signal VSYNC_INn        : bit;
signal VSYNC_OUTn       : bit;
signal SYNC_OUT_EN      : bit;
signal RDY_INn          : bit;
signal RDY_OUTn         : bit;
signal DTACK_INn        : bit;
signal DTACK_OUTn       : bit;
signal BERR_In          : bit;
begin
    GL_CLK_2M <= CLK_x1_4;
    GL_CLK_0M5 <= CLK_x1_16;

    GL_DATA <= DATA_OUT when DATA_EN = '1' else (others => 'Z');

    AS_INn <= To_Bit(GL_ASn);
    RWn_IN <= To_Bit(GL_RWn);
    UDS_INn <= To_Bit(GL_UDSn);
    LDS_INn <= To_Bit(GL_LDSn);
    HSYNC_INn <= To_Bit(GL_HSYNCn);
    VSYNC_INn <= To_Bit(GL_VSYNCn);
    RDY_INn <= To_Bit(GL_RDYn);
    DTACK_INn <= To_Bit(GL_DTACKn);

    GL_ASn <= '0' when AS_OUTn = '0' and GL_CTRL_EN = '1' else
              '1' when AS_OUTn = '1' and GL_CTRL_EN = '1' else'Z';
    GL_RWn <= '0' when RWn_OUT = '0' and GL_CTRL_EN = '1' else
              '1' when RWn_OUT = '1' and GL_CTRL_EN = '1' else'Z';
    GL_UDSn <= '0' when UDS_OUTn = '0' and GL_CTRL_EN = '1' else
               '1' when UDS_OUTn = '1' and GL_CTRL_EN = '1' else'Z';
    GL_LDSn <= '0' when LDS_OUTn = '0' and GL_CTRL_EN = '1' else
               '1' when LDS_OUTn = '1' and GL_CTRL_EN = '1' else'Z';

    BGACK_INn <= To_Bit(GL_BGACKn);
    GL_BGACKn <= '0' when BGACK_OUTn = '0' else 'Z';
    GL_VPAn <= '0' when VPA_In = '0' else 'Z';

    GL_HSYNCn <= '0' when HSYNC_OUTn = '0' and SYNC_OUT_EN = '1' else
                 '1' when HSYNC_OUTn = '1' and SYNC_OUT_EN = '1' else'Z';
    GL_VSYNCn <= '0' when VSYNC_OUTn = '0' and SYNC_OUT_EN = '1' else
                 '1' when VSYNC_OUTn = '1' and SYNC_OUT_EN = '1' else'Z';
 
    GL_RDYn <= '0' when RDY_OUTn = '0' else 'Z';
    GL_DTACKn <= '0' when DTACK_OUTn = '0' else 'Z';
 
    GL_BERRn <= '0' when BERR_In = '0' else 'Z'; -- Open drain.
--GL_BRn			        => GL_BRn,


    I_GLUECLOCKS: WF25915IP_CLOCKS
    port map(CLK_x1         => GL_CLK,
             CLK_x1_4       => CLK_x1_4,
             CLK_x1_16      => CLK_x1_16
    );

    I_GLUE: WF25915IP_TOP_SOC
        port map(GL_CLK		            => GL_CLK,
                 GL_CLK_016		        => CLK_x1_16,
                 GL_ROM_6n			    => GL_ROM_6n,
                 GL_ROM_5n	    		=> GL_ROM_5n,
                 GL_ROM_4n	    		=> GL_ROM_4n,
                 GL_ROM_3n	    		=> GL_ROM_3n,
                 GL_ROM_2n	    		=> GL_ROM_2n,
                 GL_ROM_1n	    		=> GL_ROM_1n,
                 GL_ROM_0n	    		=> GL_ROM_0n,
                 EN_RAM_14MB            => '0', -- Set to 4MB RAM address space.
                 GL_ACIACS	    		=> GL_ACIACS,
                 GL_MFPCSn	    		=> GL_MFPCSn,
                 GL_SNDCSn	    		=> GL_SNDCSn,
                 GL_FCSn		 		=> GL_FCSn,
                 GL_STE_SNDCS			=> GL_STE_SNDCS,
                 GL_STE_SNDIR	        => GL_STE_SNDIR,
                 GL_STE_RTCCSn	        => GL_STE_RTCCSn,
                 GL_STE_RTC_WRn	        => GL_STE_RTC_WRn,
                 GL_STE_RTC_RDn	        => GL_STE_RTC_RDn,
                 GL_VPAn	            => VPA_In,
                 GL_VMAn                => GL_VMAn,
                 GL_DEVn	            => GL_DEVn,
                 GL_RAMn	            => GL_RAMn,
                 GL_DMAn	            => GL_DMAn,
                 GL_AVECn		        => GL_AVECn,
                 GL_STE_FDINT	        => GL_STE_FDINT,
                 GL_STE_HDINTn	        => GL_STE_HDINTn,
                 GL_MFPINTn		        => GL_MFPINTn,
                 GL_STE_EINT3n	        => GL_STE_EINT3n,
                 GL_STE_EINT5n	        => GL_STE_EINT5n,
                 GL_STE_EINT7n	        => GL_STE_EINT7n,
                 GL_STE_DINTn	        => GL_STE_DINTn,
                 GL_IACKn		        => GL_IACKn,
                 GL_GI2n                => GL_GI2n,
                 GL_GI1n                => GL_GI1n,
                 GL_STE_IPL2n	        => GL_STE_IPL2n,
                 GL_STE_IPL1n	        => GL_STE_IPL1n,
                 GL_STE_IPL0n	        => GL_STE_IPL0n,
                 GL_BLANKn		        => GL_BLANKn,
                 GL_DE			        => GL_DE,
                 GL_HSYNC_INn	        => HSYNC_INn,
                 GL_HSYNC_OUTn	        => HSYNC_OUTn,
                 GL_VSYNC_INn	        => VSYNC_INn,
                 GL_VSYNC_OUTn	        => VSYNC_OUTn,
                 GL_SYNC_OUT_EN	        => SYNC_OUT_EN,
                 GL_RDY_INn		        => RDY_INn,
                 GL_RDY_OUTn	        => RDY_OUTn,
                 GL_BRn			        => GL_BRn,
                 GL_BGIn		        => GL_BGIn,
                 GL_BGOn		        => GL_BGOn,
                 GL_BGACK_INn	        => BGACK_INn,
                 GL_BGACK_OUTn	        => BGACK_OUTn,
                 GL_ADDRESS		        => GL_ADDRESS,
                 GL_DATA_IN		        => GL_DATA(1 downto 0),
                 GL_DATA_OUT	        => DATA_OUT,
                 GL_DATA_EN		        => DATA_EN,
                 GL_RWn_IN		        => RWn_IN,
                 GL_RWn_OUT		        => RWn_OUT,
                 GL_AS_INn		        => AS_INn,
                 GL_AS_OUTn		        => AS_OUTn,
                 GL_UDS_INn		        => UDS_INn,
                 GL_UDS_OUTn	        => UDS_OUTn,
                 GL_LDS_INn		        => LDS_INn,
                 GL_LDS_OUTn	        => LDS_OUTn,
                 GL_DTACK_INn	        => DTACK_INn,
                 GL_DTACK_OUTn	        => DTACK_OUTn,
                 GL_CTRL_EN		        => GL_CTRL_EN,
                 GL_RESETn		        => GL_RESETn,
                 GL_BERRn		        => BERR_In,
                 GL_FC	                => GL_FC,
                 GL_STE_FDDS	        => GL_STE_FDDS,
                 GL_STE_FCCLK	        => GL_STE_FCCLK,
                 GL_STE_JOY_RHn	        => GL_STE_JOY_RHn,
                 GL_STE_JOY_RLn	        => GL_STE_JOY_RLn,
                 GL_STE_JOY_WL	        => GL_STE_JOY_WL,
                 GL_STE_JOY_WEn	        => GL_STE_JOY_WEn,
                 GL_STE_BUTTONn	        => GL_STE_BUTTONn,
                 GL_STE_PAD0Xn	        => GL_STE_PAD0Xn,
                 GL_STE_PAD0Yn	        => GL_STE_PAD0Yn,
                 GL_STE_PAD1Xn	        => GL_STE_PAD1Xn,
                 GL_STE_PAD1Yn	        => GL_STE_PAD1Yn,
                 GL_STE_PADRSTn	        => GL_STE_PADRSTn,
                 GL_STE_PENn	        => GL_STE_PENn,
                 GL_STE_SCCn	        => GL_STE_SCCn,
                 GL_STE_CPROGn	        => GL_STE_CPROGn
            );                         
end STRUCTURE;