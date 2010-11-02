----------------------------------------------------------------------
----                                                              ----
---- Atari STE compatible IP Core				                  ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- This model provides the top level file of an STE compatible  ----
---- machine including CPU, Blitter, Shadow, MCU, DMA, FDC,       ----
---- Shifter, GLUE, MFP, SOUND, ACIA and RTC.                     ----
----                                                              ----
---- Important Notice concerning the clock system:                ----
---- The systems of the original ST or STE machines used several  ----
---- clocks which must stand in a fixed relation to each other.   ----
---- This core uses one central system clock of 16MHz. From this  ----
---- clock all required clocks are derived. These are CLK_1,      ----
---- CLK_2 and CLK_4. These are the clocks used for clocking      ----
---- D type flip-flops of the system and are provided by a first  ----
---- phase locked loop circuit.                                   ----
---- The clocks should used as follows:                           ----
---- CLK_1 for the CPU, BLITTER, GLUE, DMA, MFP, SHADOW, 1772,    ----
---- SOUND and the UARTs.                                         ----
---- CLK_2 for the MCU, , SHIFTER, the video RAM component and    ----
---- the external SD-RAMs.                                        ----
---- CLK_4 for the P_AUX_CLOCKS process and the memory data       ----
---- buffer.                                                      ----
---- Beside these 'real' clocks, there are several auxiliary      ----
---- clocks which are processed by one of the above mentioned     ----
---- clocks (CLK_1, CLK_2 and CLK_4). There are two clocks        ----
---- SCLK_6M4 and CLK_24576 which are provided by a second phase  ----
---- locked loop and a counter process. The SCLK_6M4 controls the ----
---- DMA sound module whereas the CLK_24576 is responsible for a  ----
---- correct timing of the MFP. The clocks are outputs of the     ----
---- counter process for their frequency is too low to provide it ----
---- directly by a phase locked loop.                             ----
---- Last but not least there are two clocks CLK_2M0 and CLK_0M5  ----
---- which control the UARTs and the sound chip. These two clocks ----
---- are provided by a counter out of the CLK_4 due to their low  ----
---- frequency.                                                   ----
---- To meet the correct timing requirements for the following    ----
---- units, there must be the correct generic settings in the     ----
---- respective top level _soc files.                             ----
---- The timing critical units are:                               ----
----   - the video timing in the GLUE.                            ----
----   - the paddle counter in the GLUE.                          ----
----   - the video phase control in the MCU.                      ----
----   - the dma sound control in the MCU.                        ----
----                                                              ----
---- The phase locked loops and the video memory for the LCD are  ----
---- customer / hardware specific components and therefore        ----
---- declared in this top level file. This kind of modelling has  ----
---- the advantage, that the migration to other hardware is simple----
---- by modifying only the top level file of the SUSKA core.      ----
----                                                              ----
---- SD-RAM section: This core provides a memory of 64MByte. This ----
----   is achieved by SDRAM types with 4 different RAM banks. The ----
----   memory organisation is 2 main banks consisting of 1 SDRAM  ----
----   chip each. Each chip features four RAM banks with at least ----
----   4M words. So in total we have 2*4*4M words.                ----
----                                                              ----
---- ACSI/SCSI section:                                           ----
----   The SCSI interface of this core is legacy. For more        ----
----     information see the WF_ACSI_SCSI_IF component.           ----
----   The DP5380 SCSI of the TT machines and Falcons is not      ----
----     supported in this IP core. The reason is the lack of     ----
----     hardware today and the upcoming of more convenient mass  ----
----     storage devices as SD cards or CF cards wich will be     ----
----     supported instead of the SCSI devices.                   ----
----     The SCSI_IDn is a switch to select the initiator ID of   ----
----     the SCSI controller of this core. It is inverted, so use ----
----     weak pull up resistors for it and connect the switch to  ----
----     GND. In this case (all switches on) the SCSI_IDn of      ----
----     "000" will indicate the highest initiator id of 7.       ----
----   Recommendings for the hardware target concerning the SCSI  ----
----    interface:                                                ----
----     Use for the outputs non inverting buffers ('541).        ----
----     Use for the data in/outputs tri state buffers ('245).    ----
----     Select for the input / output buffers a supply of 3.3V.  ----
----     The VCCIO voltage of the selected FPGAs should also be   ----
----     at 3.3V for the related interface lines.                 ----
----                                                              ----
---- IDE interface:                                               ----
----   Use a 16 bit wide LVTTL tri state drivers to control the   ----
----   data direction from or to an IDE device.                   ----
----   The IDE_D_EN_INn and IDE_D_EN_OUTn outputs are the         ----
----     respective tri state enables where IDE_D_EN_INn controls ----
----     the tri state for the read operation from an IDE device  ----
----     and IDE_D_EN_OUTn controls the write operation to an     ----
----     IDE device.                                              ----
----   Select for the output buffers a supply of +5V.             ----
----   Select for the input buffers a supply of VCCIO of the      ----
----     selected programmable logic device.                      ----
----															  ----
---- SD card interface:                                           ----
----   The interface is based on the project 'SatanDisk' of       ----
----   Miroslav Nohaj 'Jookie'. Use a clock frequency of 16MHz    ----
----   for this component. Use the same clock frequency for the   ----
----   connected AVR microcontroller.                             ----
----                                                              ----
---- Recommendations for the signal termination:                  ----
----   The following signals should be terminated with a weak     ----
----   pull up resistor (~22K). Use either weak pull up resistors ----
----   of the FPGAs or external ones:                             ----
----     DATA, ADR, FC.                                           ----
----   Use for the following signals strong pull up resistors     ----
----   with a value of about 6K8:                                 ----
----     ASn, RWn, UDSn, LDSn, DTACKn, BGACKn, BRn,               ----
----     EINT3, EINT5, EINT7.                                     ----
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
-- Revision 2K7B  2007/12/24 WF
--   Replaced the external SHADOW video ram by an internal component.
--   Connected the UARTs (CTSn and DCDn) to pins.
-- Revision 2K8A  2008/07/14 WF
--   Replaced the original MCU (25912) by a MCU capable driving the
--   SD-RAMs used in the Suska-III hardware.
--   Changes to run on the Suska hardware platform.
-- Revision 2K8B  2008/12/24 WF
--   Introduced EN_RAM_14MB.
-- Revision 2K9A  2009/06/20 WF
--   Enhancements in the video system to drive modern TFTs or multisyncs.
--   The RESETn pin is not asserted by the RESET_BOOTn any more but by
--     the FLASH_RESETn. This change was necessary because the FLASH's
--     reset is on the series boards connected to the system reset.
--   New: PLL_ARESET logic for resetting the phase locked loops during
--     system startup.
--   New: Clock synchronization in the MCU control file (process TIME_SLICES).
--   New: Clock synchronization in the WF25914IP_CR_SHIFT_REG.
--   Changed LATCHn behaviour in the MCU control file.
--   New: process SLOW_CPU for lowering the CPU speed (compatibility reasons).
--   Fixed interrupt polarity for TA_I and TB_I in the MFP core.
--   Minor improvements in the MFP timer section.
--   Several fixes concerning colour corrections in the Shifter's chroma shift registers.
--   Fixed CPU exception processing to improve system startup.
--   A couple of minor bug fixes.
--

library work;
use work.wf_suska_pkg.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF_SUSKA_TOP is
	port(
		-- System controls:
		RESET_COREn			: in bit; -- FPGA reset.
		RESETn				: inout std_logic; -- System and CPU reset.
		CLK_PLL1			: in std_logic; -- 16 MHz system clock.
		CLK_PLL2			: in std_logic; -- 16 MHz system clock.
		CLK_AUX				: out bit; -- Auxiliary clock.
		TESTPAD				: out bit; -- Auxiliary pin.

		-- Bus status controls:
		FC					: inout std_logic_vector(2 downto 0);
		BERRn				: inout std_logic;
		HALTn				: inout std_logic;

		-- Bus arbitration control:
		BRn					: in bit;
		BGOn				: out bit;
		BGACKn				: inout std_logic; -- Open drain.

		-- Asynchronous bus interface:
		DTACKn				: inout std_logic;
		UDSn				: inout std_logic;
		LDSn				: inout std_logic;
		ASn					: inout std_logic;
		RWn					: inout std_logic;
						
		-- Data and address busses:
		DATA				: inout std_logic_vector(15 downto 0);
		ADR					: inout std_logic_vector(23 downto 1);

		-- Synchronous bus interface:
		VPAn				: in bit; -- Attention: requires at least a weak pull up resistor!
		VMAn				: out std_logic;
		E					: out bit;

		-- OS ROM select lines:
		ROM6n				: out bit;
		ROM5n				: out bit;
		ROM4n				: out bit;
		ROM3n				: out bit;
		ROM2n				: out bit;
		ROM1n				: out bit;
		ROM0n				: out bit;

		-- The SDRAM interface:
		RAM_CLK				: out std_logic;

		RAM_RAS0n			: out bit;
		RAM_CAS0n			: out bit;
		RAM_WEn				: out bit;
		RAM_RAS1n			: out bit;
		RAM_CAS1n			: out bit;

		RAM_DQM0H			: out bit;
		RAM_DQM0L			: out bit;
		RAM_DQM1H			: out bit;
		RAM_DQM1L			: out bit;
		RAM_ADR				: out bit_vector(12 downto 0);
		RAM_BA				: out bit_vector(1 downto 0);
		RAM_DATA			: inout std_logic_vector(15 downto 0);

		-- LCD control:
		UDATA				: out bit_vector(3 downto 0);
		LDATA				: out bit_vector(3 downto 0);
		LFS					: out bit; -- Line frame strobe.
		VDCLK				: out bit; -- Video data clock.
		LLCLK				: out bit; -- Line latch clock.

		-- Video interface:
		-- The CRT_PIN4 is the monochrome detect on STs and is the
		-- monochrome detect or external clock on STEs.
		-- The CRT_PIN3 is the GPO on STs and the external clock
		-- select on STEs (1 = internal clock).
		CRT_PIN3			: in bit;
		CRT_PIN4_CLK1		: in std_logic;
		CRT_PIN4_CLK2		: in std_logic;
		CRT_R				: out bit_vector(3 downto 0);
		CRT_G				: out bit_vector(3 downto 0);
		CRT_B				: out bit_vector(3 downto 0);
		CRT_MONO			: out bit;
		HSYNC				: inout std_logic;
		VSYNC				: inout std_logic;
		GPO					: out bit;

		-- External interrups:
		AVECn				: in bit;
		EINT3n				: in bit;
		EINT5n				: in bit;
		EINT7n				: in bit;

		-- Floppy disk interface:
		FDTYPE				: in bit; -- '1' for HD-, '0' for DD disks.
		FDD_RDn				: in bit;
		FDD_TR00			: in bit;
		FDD_IPn				: in bit;
		FDD_WPn				: in bit;
		FDD_WGn				: out std_logic; -- Open drain.
		FDD_WDn				: out std_logic; -- Open drain.
		FDD_STEPn			: out std_logic; -- Open drain.
		FDD_DIRCn			: out std_logic; -- Open drain.
		FDD_MOn				: out std_logic; -- Open drain.
		FDD_D1SEL			: out bit;
		FDD_D0SEL			: out bit;
		FDD_SDSEL			: out bit;

		-- The ACSI interface:
		CD					: inout std_logic_vector(7 downto 0);
		CA1_OUT				: out bit;
		HDCSn				: out bit;
		HDRQn				: in bit;
		HDACKn				: out bit;
		HDINTn				: in bit;
		ACSI_RDn		    : out bit;
		ACSI_WRn		    : out bit;

		-- The SCSI interface:
		SCSI_RDn		    : out bit;
		SCSI_WRn		    : out bit;
		SCSI_IDn			: in bit_vector(3 downto 1); -- ID of the initiator.
		SCSI_D				: inout std_logic_vector(7 downto 0);
		SCSI_CTRL_ENn		: out bit; -- Tri State control.
		SCSI_BUSYn			: in bit;
		SCSI_MSGn			: in bit;
		SCSI_REQn			: in bit;
		SCSI_DCn			: in bit;
		SCSI_IOn			: in bit;
		SCSI_RSTn			: out bit;
		SCSI_ACKn			: out bit;
		SCSI_SELn			: out bit;
		SCSI_DP				: out bit; -- Inverse polarity due to Suska III schematics.

		-- IDE interface:
		IDE_INTRQ			: in bit;
		IDE_IORDY			: in bit;
		IDE_CS0n			: out bit;
		IDE_CS1n			: out bit;
		IDE_IORDn			: out bit;
		IDE_IOWRn			: out bit;
		IDE_D_EN_INn		: out bit;
		IDE_D_EN_OUTn		: out bit;

		-- SD card interface:
		SDC_AVR_CLK			: out std_logic; -- 16MHz.
		SDC_AVR_DO			: in bit;
		SDC_AVR_PIO_DMAn	: in bit;
		SDC_AVR_RWn			: in bit;
		SDC_AVR_CLR_CMD		: in bit;
		SDC_AVR_DONE		: out bit;
		SDC_AVR_GOT_CMD		: out bit;
		SDC_AVR_D			: inout std_logic_vector(7 downto 0);

		-- Keyboard:
		KEYB_RxD			: in bit;
		KEYB_TxD			: out bit;
		
		-- MIDI:
		UART_MIDI_CTSn		: in bit; -- In original ST machines wired to GND.
		UART_MIDI_DCDn		: in bit; -- In original ST machines wired to GND.
		UART_MIDI_RTSn		: out bit; -- Not used in original ST machines.
		MIDI_OLR			: out std_logic; -- Open drain.
		MIDI_TLR			: out std_logic; -- Open drain.
		MIDI_IN				: in bit;

		-- COM Port:
		COM_RxD				: in bit;
		COM_TxD				: out std_logic;
		COM_RI				: in bit;
		COM_CTS				: in bit;
		COM_DCD				: in bit;
		COM_RTS				: out bit;
		COM_DTR				: out bit;
				
		-- Printer interface:
		LPT_STRB			: out bit;
		LPT_D				: inout std_logic_vector(7 downto 0);
		LPT_BSY				: in bit;
		
		-- Joystick / Paddles / Lightpen:
		JOY_RHn				: out bit;
		JOY_RLn				: out bit;
		JOY_WL				: out bit;
		JOY_WEn				: out bit;
		BUTTONn				: out bit;
		PAD0Xn				: in bit;
		PAD0Yn				: in bit;
		PAD1Xn				: in bit;
		PAD1Yn				: in bit;
		PADRSTn				: out bit;
		PENn				: in bit;

		-- DS1392 RTC:
		DS1392_D			: inout std_logic;
		DS1392_SCL			: out bit;
		DS1392_CSn			: out bit;

		-- Flash controls:
		FLASH_RDY			: in bit;
		FLASH_ADR_19		: out bit;
		FLASH_ADR_18		: out bit;
		FLASH_WEn			: out bit;
		FLASH_OEn			: out bit;
		FLASH_CEn			: out bit;

		-- TCP/IP (DP83848C):
		C83848_MDIO			: in bit; -- Switch to out, if used.
		C83848_MDC			: in bit; -- Switch to out, if used.
		C83848_TxD3			: in bit; -- Switch to out, if used.
		C83848_TxD2			: in bit; -- Switch to out, if used.
		C83848_TxD1			: in bit; -- Switch to out, if used.
		C83848_TxD0			: in bit; -- Switch to out, if used.
		C83848_TX_CLK		: in bit; -- Switch to out, if used.
		C83848_TX_EN		: in bit; -- Switch to out, if used.
		C83848_RxD3			: in bit; -- Switch to out, if used.
		C83848_RxD2			: in bit; -- Switch to out, if used.
		C83848_RxD1			: in bit; -- Switch to out, if used.
		C83848_RxD0			: in bit; -- Switch to out, if used.
		C83848_RX_CLK		: in bit; -- Switch to out, if used.
		C83848_RX_ER		: in bit; -- Switch to out, if used.
		C83848_RX_DV		: in bit; -- Switch to out, if used.
		C83848_CRS_DV		: in bit; -- Switch to out, if used.
		C83848_COL			: in bit; -- Switch to out, if used.
		C83848_INTn			: in bit; -- Switch to out, if used.
		
		-- USB interface (MAX3421):
		C3421_SSn			: in bit; -- Switch to out, if used.
		C3421_INT			: in bit; -- Switch to out, if used.
		C3421_SCLK			: in bit; -- Switch to out, if used.
		C3421_MISO			: in bit; -- Switch to out, if used.
		C3421_MOSI			: in bit; -- Switch to out, if used.

		-- Shadow register bits of the STBook:
		xFF827E_D			: out bit_vector(7 downto 2);

		-- Microwire and sound:
		FCLK				: out bit; -- Frame clock.
		MWK					: out bit;
		MWD					: out bit;
		MWEn				: out bit;
		-- The original parallel DACs are replaced by a
		-- serial controlled twin device AD5302:
		DAC_SCLK			: out bit;
		DAC_SYNCn			: out bit;
		DAC_SDATA			: out bit;
		DAC_LDACn			: out bit;

		-- Sound:
		YM_OUT_A			: out bit;
		YM_OUT_B			: out bit;
		YM_OUT_C			: out bit;

		-- Audio Codec:
		CODEC_SDOUT			: in bit;
		CODEC_SDIN			: in bit; -- Switch to out, if used.
		CODEC_SSYNC			: in bit; -- Switch to out, if used.
		CODEC_SCLK			: in bit; -- Switch to out, if used.
		
		-- Configuration switch:
		-- Use the FPGA's weak pull up feature or external pull up resistors.
		-- Connect the switches to GND.
		CONFIG				: in bit_vector(6 downto 1); -- Configuration switches.

		-- System status:
		PLL_FAULT			: out bit; -- Indicates unlocked PLLs.
		BOOT_LED			: out bit; -- Boot loader active...
		
		-- Power microcontroller interface:
        MC_SPI_CLK			: in bit;
        MC_SPI_DIN			: out bit;
        MC_SPI_DOUT			: in bit;
        BOOT_ACK			: in bit;
        BOOT_REQ			: out bit
	);
end entity WF_SUSKA_TOP;

architecture STRUCTURE of WF_SUSKA_TOP is
-- Hardware specific components:
component cyclone2_vram
	PORT
	(
		address		: IN STD_LOGIC_VECTOR (14 DOWNTO 0);
		clock		: IN STD_LOGIC ;
		data		: IN STD_LOGIC_VECTOR (7 DOWNTO 0);
		wren		: IN STD_LOGIC ;
		q			: OUT STD_LOGIC_VECTOR (7 DOWNTO 0)
	);
end component;

component cyclone2_pll_1
	PORT
	(
		areset		: IN STD_LOGIC  := '0';
		clkswitch	: IN STD_LOGIC  := '0';
		inclk0		: IN STD_LOGIC  := '0';
		inclk1		: IN STD_LOGIC  := '0';
		c0			: OUT STD_LOGIC ;
		c1			: OUT STD_LOGIC ;
		c2			: OUT STD_LOGIC ;
		locked		: OUT STD_LOGIC 
	);
end component;

component cyclone2_pll_2
	PORT
	(
		areset		: IN STD_LOGIC  := '0';
		inclk0		: IN STD_LOGIC  := '0';
		c0			: OUT STD_LOGIC ;
		c1			: OUT STD_LOGIC ;
		c2			: OUT STD_LOGIC ;
		locked		: OUT STD_LOGIC 
	);
end component;

signal RESET_Sn			: bit;
signal RESET_CORE_Sn	: bit;
signal RESET_CPUn		: bit;
signal RESET_INn		: bit;
signal RESET_EN_68K00	: bit;
signal RESET_BOOTn		: bit;
signal RESET_MCUn		: bit;
signal IDE_RES_In		: bit;
signal HALT_INn			: std_logic;
signal PLL1_LOCKED		: std_logic;
signal PLL2_LOCKED		: std_logic;
signal CLK_SEL_PLL		: std_logic;
signal CLK_PLL_IN0		: std_logic;
signal CLK_PLL_IN1		: std_logic;
signal CLK_PLL2_IN0		: std_logic;
signal CLK_PLL_4x		: std_logic;
signal CLK_PLL_2x		: std_logic;
signal CLK_PLL_1x		: std_logic;
signal CLK_PLL_256		: std_logic;
signal CLK_PLL_394		: std_logic;
signal CLK_4			: bit;
signal CLK_2			: bit;
signal CLK_1			: bit;
signal CLK_2M0			: bit;
signal CLK_0M5			: bit;
signal CLK_24576		: bit;
signal SCLK_6M4			: bit;
signal DATA_OUT_68K00	: std_logic_vector(15 downto 0);
signal DATA_EN_68K00	: bit;
signal DATA_OUT_BLT		: std_logic_vector(15 downto 0);
signal DATA_EN_BLT		: bit;
signal DATA_OUT_GLUE	: std_logic_vector(15 downto 0);
signal DATA_EN_GLUE		: bit;
signal DATA_OUT_MCU		: std_logic_vector(7 downto 0);
signal DATA_EN_MCU		: bit;
signal DATA_OUT_DMA		: std_logic_vector(15 downto 0);
signal DATA_EN_DMA		: bit;
signal DATA_OUT_MFP		: std_logic_vector(7 downto 0);
signal DATA_EN_MFP		: bit;
signal DATA_OUT_SOUND	: std_logic_vector(7 downto 0);
signal DATA_EN_SOUND	: bit;
signal DATA_OUT_ACIA_I	: std_logic_vector(7 downto 0);
signal DATA_EN_ACIA_I	: bit;
signal DATA_OUT_ACIA_II	: std_logic_vector(7 downto 0);
signal DATA_EN_ACIA_II	: bit;
signal DATA_OUT_RP5C15	: std_logic_vector(3 downto 0);
signal DATA_EN_RP5C15	: bit;
signal DATA_OUT_BOOT	: std_logic_vector(15 downto 0);
signal DATA_EN_BOOT		: bit;
signal ADR_IN			: bit_vector(23 downto 1);
signal ADR_OUT			: std_logic_vector(23 downto 1);
signal ADR_OUT_68K00	: std_logic_vector(23 downto 1);
signal ADR_EN_68K00		: bit;
signal ADR_OUT_BLT		: std_logic_vector(23 downto 1);
signal ADR_EN_BLT		: bit;
signal ADR_OUT_BOOT		: std_logic_vector(20 downto 1);
signal ADR_EN_BOOT		: bit;
signal MCU_ADR			: bit_vector(25 downto 1);
signal MDAT_BUFFER		: std_logic_vector(15 downto 0);
signal BERR_In			: bit;
signal BERR_GLUEn		: bit;
signal HALT_68K00		: bit;
signal AS_INn			: bit;
signal UDS_INn			: bit;
signal LDS_INn			: bit;
signal RWn_IN			: bit;
signal RWn_OUT_68K00	: bit;
signal RW_OUT_EN_68K00	: bit;
signal AS_OUT_68K00n	: bit;
signal AS_OUT_EN_68K00	: bit;
signal UDS_OUT_68K00n	: bit;
signal UDS_OUT_EN_68K00	: bit;
signal LDS_OUT_68K00n	: bit;
signal LDS_OUT_EN_68K00	: bit;
signal RWn_OUT_BLT		: bit;
signal AS_OUT_BLTn		: bit;
signal UDS_OUT_BLTn		: bit;
signal LDS_OUT_BLTn		: bit;
signal BUSCTRL_EN_BLT	: bit;
signal RWn_OUT_GLUE		: bit;
signal AS_OUT_GLUEn		: bit;
signal UDS_OUT_GLUEn	: bit;
signal LDS_OUT_GLUEn	: bit;
signal BUSCTRL_EN_GLUE	: bit;
signal ROM2_In			: bit;
signal ACIA_CS_I		: bit;
signal MFP_CS_In		: bit;
signal SNDCS_I			: bit;
signal SNDIR_I			: bit;
signal FCS_In			: bit;
signal DMA_SYNC_I		: bit;
signal RAM_In			: bit;
signal DMA_In			: bit;
signal DMA_SRC_SEL_I	: bit_vector(1 downto 0);
signal DEV_In			: bit;
signal FDCS_In			: bit;
signal FC_IN			: bit_vector(2 downto 0);
signal FC_OUT_68K00		: std_logic_vector(2 downto 0);
signal FC_OUT_EN_68K00	: bit;
signal FC_OUT_BLT		: std_logic_vector(2 downto 0);
signal DTACK_INn		: bit;
signal DTACK_OUT_BLTn	: bit;
signal DTACK_OUT_GLUEn	: bit;
signal DTACK_OUT_MCUn	: bit;
signal DTACK_OUT_MFPn	: bit;
signal DTACK_OUT_IDEn	: bit;
signal IPLn				: bit_vector(2 downto 0);
signal E_I				: bit;
signal VPA_INn			: bit;
signal VMA_OUT_68K00n	: bit;
signal VMA_OUT_EN_68K00	: bit;
signal BR_In			: bit;
signal BR_BLTn			: bit;
signal BR_GLUEn			: bit;
signal BG_68K00n		: bit;
signal BG_BLTn			: bit;
signal BGACK_GLUEn		: bit;
signal BGACK_INn		: bit;
signal BGACK_BLTn		: bit;
signal MAD				: bit_vector(9 downto 0);
signal INT_BLTn			: bit;
signal RP5C15_CSn		: bit;
signal RP5C15_WRn		: bit;
signal RP5C15_RDn		: bit;
signal HSYNC_In			: bit;
signal HSYNC_On			: bit;
signal VSYNC_In			: bit;
signal VSYNC_On			: bit;
signal SYNC_EN			: bit;
signal BLANKn			: bit;
signal MONO_CRTn		: bit;
signal AVEC_INn			: bit;
signal AVEC_GLUEn		: bit;
signal FDINT			: bit;
signal MFPINTn			: bit;
signal IACKn			: bit;
signal RDY_DMA			: bit;
signal RDY_GLUE			: bit;
signal DINTn			: bit;
signal DCYCn			: bit;
signal CMPCSn			: bit;
signal WDATn			: bit;
signal RDATn			: bit;
signal LATCHn			: bit;
signal CA2				: bit;
signal CA1				: bit;
signal CR_Wn			: bit;
signal CD_OUT_DMA		: std_logic_vector(7 downto 0);
signal CD_EN_DMA		: bit;
signal CD_OUT_FDC		: std_logic_vector(7 downto 0);
signal CD_EN_FDC		: bit;
signal ACSI_SCSI_D_IN	: bit_vector(7 downto 0);
signal ACSI_SCSI_D_OUT	: bit_vector(7 downto 0);
signal ACSI_SCSI_D_EN	: bit;
signal ACSI_SD_D_OUT	: std_logic_vector(7 downto 0);
signal ACSI_SD_D_EN		: bit;
signal ACSI_CS_DMAn		: bit;
signal SD_CSn			: bit;
signal HDACK_In			: bit;
signal HDRQ_IN			: bit;
signal HDINT_INn		: bit;
signal ACSI_SCSI_HDRQn	: bit;
signal HDINT_ACSI_SCSIn	: bit;
signal HDINT_IDEn		: bit;
signal HDINT_ACSI_SDn	: bit;
signal ACSI_SD_DRQn	    : bit;
signal SCSI_CTRL_EN		: bit;
signal SCSI_D_EN		: bit;
signal SCSI_D_IN		: bit_vector(7 downto 0);
signal SCSI_D_OUT		: bit_vector(7 downto 0);
signal SCSI_DP_In		: bit;
signal SCSI_CSn         : bit;
signal SCSI_CS_DMAn     : bit;
signal FDRQ				: bit;
signal FDD_WG			: bit;
signal FDD_WD			: bit;
signal FDD_STEP			: bit;
signal FDD_DIRC			: bit;
signal FDD_MO			: bit;
signal SINT_IO7			: bit;
signal SINT_TAI			: bit;
signal DATA_SHFT		: std_logic_vector(15 downto 0);
signal DATA_EN_HI_SHFT	: bit;
signal DATA_EN_LO_SHFT	: bit;
signal SREQ				: bit;
signal SLOADn			: bit;
signal TDO				: bit;
signal IO_B_OUT			: bit_vector(7 downto 0);
signal IO_B_EN			: bit;
signal LPT_D_IN			: bit_vector(7 downto 0);
signal YM_OUT_A4		: bit;
signal YM_OUT_A3		: bit;
signal MFP_SO			: bit;
signal MFP_SO_EN		: bit;
signal MIDI_OUT			: bit;
signal IRQ_KEYBDn		: bit;
signal IRQ_MIDIn		: bit;
signal IRQ_ACIAn		: bit;
signal DE_I				: bit;
signal DE_MSYNC			: bit;
signal MULTISYNC_I		: bit_vector(1 downto 0);
signal VIDEO_HIMODE_I   : bit;
signal SHADOW_DATA		: bit_vector(15 downto 0);
signal VPA_GLUE_OUTn	: bit;
signal SHADOW_VRAM_ADR	: bit_vector(14 downto 0);
signal SHADOW_VRAM_WRn	: bit;
signal VRAM_CLK			: std_logic;
signal VRAM_ADR			: std_logic_vector(14 downto 0);
signal VRAM_WEn			: std_logic;
signal VRAM_D_IN		: std_logic_vector(7 downto 0);
signal VRAM_D_OUT		: std_logic_vector(7 downto 0);
signal DS1392_IN		: bit;
signal DS1392_OUT		: bit;
signal DS1392_OUT_EN	: bit;
signal ADC_AVR_D_OUT	: std_logic_vector(7 downto 0);
signal ADC_AVR_D_EN		: bit;
signal PLL_LOCKS		: bit;
signal SDATA_L			: bit_vector(7 downto 0);
signal SDATA_R			: bit_vector(7 downto 0);
signal ROMSEL_FC_E0n    : bit;
signal FLASH_RESET_In	: bit;
signal FLASH_WAITSTATEn : bit;
signal PLL_ARESET       : std_logic;
begin
	-- Clock system:
	CLK_SEL_PLL <= '1' when CRT_PIN3 = '0' else '0';
	CLK_PLL_IN0 <= CLK_PLL1;
	CLK_PLL_IN1 <= CRT_PIN4_CLK1;
	-- CLK_PLL_IN1 <= CRT_PIN4_CLK2; -- For system flexibility.
	CLK_PLL2_IN0 <= CLK_PLL2;
	CLK_4 <= To_Bit(CLK_PLL_4x);
	CLK_2 <= To_Bit(CLK_PLL_2x);
 	CLK_1 <= To_Bit(CLK_PLL_1x);
		
    RAM_CLK <= CLK_PLL_2x; -- Use the MCU clock.

    TESTPAD <= FDINT;

	KEY_SCAN: process
	-- Sample the RESETn and the RESET_COREn buttons
	-- about every 5ms. This provides stability against
	-- push button jitter.
	variable SCAN_TIMER	: std_logic_vector(16 downto 0);
	begin
		wait until CLK_1 = '1' and CLK_1' event;
		if SCAN_TIMER <= '1' & x"3880" then
			SCAN_TIMER := SCAN_TIMER + '1';
		else
			SCAN_TIMER := (others => '0');
			RESET_Sn <= To_Bit(RESETn);
			RESET_CORE_Sn <= RESET_COREn;
		end if;
	end process KEY_SCAN;

	PLL_RESET: process
	-- This process is responsible for resetting the PLLs
	-- during system startp.
	variable LOCK : boolean;
	begin
		wait until CLK_PLL1 = '1' and CLK_PLL1' event;
		if RESET_COREn = '0' and LOCK = false then
            LOCK := true;
            PLL_ARESET <= '1';
        elsif RESET_COREn = '1' then
            LOCK := false;
            PLL_ARESET <= '0';
        else
            PLL_ARESET <= '0';
        end if;
	end process PLL_RESET;

	PLL_LOCK_FLT: process
	-- This process provides a filter for the PLL status
	-- information.
	variable TMP : integer range 0 to 31;
	begin
		wait until CLK_PLL1 = '1' and CLK_PLL1' event;
		if (PLL1_LOCKED = '0' or PLL2_LOCKED = '0') and TMP = 0 then
			PLL_LOCKS <= '0';
			PLL_FAULT <= '1';
		elsif PLL1_LOCKED = '0' or PLL2_LOCKED = '0' then
			TMP := TMP -1;
			PLL_LOCKS <= '1';
			PLL_FAULT <= '0';
		else
			TMP := 31;
			PLL_LOCKS <= '1';
			PLL_FAULT <= '0';
		end if;
	end process PLL_LOCK_FLT;	

	-- The RESETs are as follows:
	-- RESET_Sn is the user's reset button.
	-- The RESET_CORE_Sn is the system's reset button.
	-- The RESET_BOOTn is the bootloader's reset during flash load operation.
	-- The RESET_MCUn is the memory controller's reset during RAM initialisation.
	-- PLL_LOCKS reset the system when the PLLs do not lock.
	-- RESET_EN_68K00 is the CPU reset output.
    RESET_INn <= RESET_Sn and RESET_BOOTn and RESET_MCUn and PLL_LOCKS;
	RESET_CPUn <= RESET_CORE_Sn and RESET_BOOTn and RESET_MCUn and PLL_LOCKS;
    RESETn <= '0' when RESET_EN_68K00 = '1' or FLASH_RESET_In = '0' or RESET_MCUn = '0' or PLL_LOCKS = '0' else 'Z';
	HALT_INn <= '0' when HALTn = '0' or (RESET_INn = '0' and RESET_EN_68K00 = '0') else '1';
	
	-------------------- Hardware specific components --------------------
	----                                                              ----
	---- The following components instantiate the clock phase locked  ----
	---- loops and the video ram. These are cyclone specific and thus ----
	---- an object of change, if other devices are used for this core.----
	----                                                              ----
	I_SYSCLOCKS: cyclone2_pll_1
		port map(
            areset				=> PLL_ARESET,
			clkswitch			=> CLK_SEL_PLL,
			inclk0				=> CLK_PLL_IN0, -- 16MHz.
			inclk1				=> CLK_PLL_IN1, -- External via CRT_PIN4.
			c0					=> CLK_PLL_1x, -- 16MHz.
			c1					=> CLK_PLL_2x, -- 32MHz.
			c2					=> CLK_PLL_4x, -- 64MHz.
			locked				=> PLL1_LOCKED
		);

	I_AUXCLOCKS: cyclone2_pll_2
		port map(
            areset				=> PLL_ARESET,
			inclk0				=> CLK_PLL2_IN0, -- 16MHz.
			c0					=> CLK_PLL_256, -- 25.6MHz.
			c1					=> CLK_PLL_394, -- 39.4MHz.
            c2					=> SDC_AVR_CLK, -- 16.0MHz.
			locked				=> PLL2_LOCKED
		);

    I_VRAM : cyclone2_vram
		port map(
			address				=> VRAM_ADR,
			clock				=> VRAM_CLK,
			data				=> VRAM_D_IN,
			wren				=> not VRAM_WEn,
			q					=> VRAM_D_OUT
		);
	----                                                              ----
	------------------ End hardware specific components ------------------

	P_AUX_CLOCKS: process
	-- The sound wave clock CLK_2M0 and the UART receiver,
	-- UART transmitter and sound clock CLK_0M5 are slow
	-- and therefore not possible to be provided by a PLL.
	-- Therefore this clock divider is adjusted to produce
	-- the required frequencies of 2MHz for the CLK_2M0 and
	-- 500kHz for the CLK_0M5. The Clocks are not used as
	-- clocks for d type flip-flops and therefore allowed
	-- as gated clocks.
	variable TMP: std_logic_vector(5 downto 0);
	begin
		wait until CLK_4 = '1' and CLK_4' event; -- 64MHz.
		TMP := TMP + '1';
		case TMP is
            when "000000" => 
                CLK_0M5 <= not CLK_0M5;
                CLK_2M0 <= not CLK_2M0;
            when "010000" | "100000" | "110000" =>
                CLK_2M0 <= not CLK_2M0;
            when others => null;
		end case;
	end process P_AUX_CLOCKS;

	P_2M4576: process
	-- This process provides the 2.4576MHz clock for the MFP
	-- timer. It is derived from a 39.4MHz PLL clock divided
	-- by 16 which results in a 2.4600 MHz clock.
	variable TMP_2M54: std_logic_vector(3 downto 0);
	begin
		wait until CLK_PLL_394 = '1' and CLK_PLL_394' event;
		TMP_2M54 := TMP_2M54 + '1';
		CLK_24576 <= To_Bit(TMP_2M54(3));
	end process P_2M4576;

	P_6M4: process
	-- This process provides the 6.4000MHz clock for the DMA
	-- sound module. It is derived from a 25.6MHz PLL clock
	-- divided by 4 which results in a 6.4000 MHz clock.
	variable TMP_6M4: std_logic_vector(1 downto 0);
	begin
		wait until CLK_PLL_256 = '1' and CLK_PLL_256' event;
		TMP_6M4 := TMP_6M4 + '1';
		SCLK_6M4 <= To_Bit(TMP_6M4(1));
	end process P_6M4;
 
	MEM_DATA_BUFFER: process(RESET_INn, CLK_4)
	-- This process is the synchronous pendant of the
	-- memory to data bus bridge buffer of the original ST
	-- machine. To work properly, the buffer is driven by
	-- a fast clock.
	begin
		if RESET_INn = '0' then
			MDAT_BUFFER <= (others => '0');
        elsif CLK_4 = '1' and CLK_4' event then
			if LATCHn = '1' then
				MDAT_BUFFER <= RAM_DATA;
			end if;
		end if;
	end process MEM_DATA_BUFFER;

	DATA <= DATA_OUT_BOOT when DATA_EN_BOOT = '1' else
            (others => 'Z') when RESET_BOOTn = '0' else -- Remark1.
            DATA_OUT_68K00 when DATA_EN_68K00 = '1' else
			DATA_OUT_BLT when DATA_EN_BLT = '1' else
			DATA_OUT_GLUE when DATA_EN_GLUE = '1' else
			x"00" & DATA_OUT_MCU when DATA_EN_MCU = '1' else
			DATA_OUT_DMA when DATA_EN_DMA = '1' else
			x"00" & DATA_OUT_MFP when DATA_EN_MFP = '1' else
			DATA_OUT_SOUND & x"00" when DATA_EN_SOUND = '1' else
			DATA_OUT_ACIA_I & x"00" when DATA_EN_ACIA_I = '1' else
			DATA_OUT_ACIA_II & x"00" when DATA_EN_ACIA_II = '1' else
			x"000" & DATA_OUT_RP5C15 when DATA_EN_RP5C15 = '1' else
			RAM_DATA when LATCHn = '1' and RDATn = '0' else -- Remark2.
			MDAT_BUFFER when RDATn = '0' else (others => 'Z');

            -- Remark1: This prioritized logic prevents the system from
            --          any misbehaviour during reading or writing to the
            --          flash memory via the bootloader mechanism.
			-- Remark2: The RAM data is switched directly to the data bus,
			--          when the LATCH is transparent. The reason is the
			--          critical bus timing. Switching the data directly
			--          saves one CLK_4 clock period.

    ADR_IN <= To_BitVector(ADR);
	ADR <= ADR_OUT;
	ADR_OUT <= 	ADR_OUT_68K00 when ADR_EN_68K00 = '1' else
				ADR_OUT_BLT when ADR_EN_BLT = '1' else 
				"000" & ADR_OUT_BOOT when ADR_EN_BOOT = '1' else (others => 'Z');

	FLASH_ADR_19 <= To_Bit(ADR_OUT_BOOT(19)) when ADR_EN_BOOT = '1' else
                    '0' when ROMSEL_FC_E0n = '1' else -- Required to get the old TOS' running. 
					To_Bit(ADR_OUT_68K00(19)) when ADR_EN_68K00 = '1' else
					To_Bit(ADR_OUT_BLT(19)) when ADR_EN_BLT = '1' else '1';
	FLASH_ADR_18 <= To_Bit(ADR_OUT_BOOT(18)) when ADR_EN_BOOT = '1' else
                    '0' when ROMSEL_FC_E0n = '1' else -- Required to get the old TOS' running.
					To_Bit(ADR_OUT_68K00(18)) when ADR_EN_68K00 = '1' else
					To_Bit(ADR_OUT_BLT(18)) when ADR_EN_BLT = '1' else '1';
	
	-- Operating system ROM:
    ROMSEL_FC_E0n <= CONFIG(6); -- Select TOS2.x address space for switch = on.
	ROM2n <= ROM2_In;
	
    -- SD-type-RAM memory section: 
    -- CONFIG(4) = '1' is the ST machines compatibility mode.
    MCU_ADR <= x"0" & ADR_IN(21 downto 1) when CONFIG(4) = '1' else "00" & ADR_IN;

    -- The SHIFTER registers are read via the RAM_DATA bus. See respective ST schematics.
	RAM_DATA <= DATA when WDATn = '0' else 
				DATA_SHFT when DATA_EN_HI_SHFT = '1' and DATA_EN_LO_SHFT = '1' else
				DATA_SHFT(15 downto 8) & x"FF" when DATA_EN_HI_SHFT = '1' else
				x"FF" & DATA_SHFT(7 downto 0) when DATA_EN_LO_SHFT = '1' else (others => 'Z');

	-- Shadow LCD video ram:
	VRAM_CLK <= '1' when CLK_2 = '1' else '0';
	VRAM_WEn <= '1' when SHADOW_VRAM_WRn = '1' else '0';
	VRAM_ADR <= To_StdLogicVector(SHADOW_VRAM_ADR);

	SHADOW_DATA <= To_BitVector(RAM_DATA);

	-- Video configuration:
	MONO_CRTn <= '0' when CRT_PIN4_CLK1 = '0' or CONFIG(3 downto 2) = "00" else '1';
	MULTISYNC_I <= CONFIG(3 downto 2);

	P_DE_COUNT: process
	-- This flip flop provides a bisection of the DE frequency to
	-- meet a correct line counter value of the multifunction port
	-- timer B in case of the hi video modi with line doubling.
	variable LOCK	: boolean;
	begin
		wait until CLK_1 = '1' and CLK_1' event;
		if VIDEO_HIMODE_I = '0' then
			DE_MSYNC <= DE_I;
		elsif VIDEO_HIMODE_I = '1' and DE_I = '1' and LOCK = false then
			DE_MSYNC <= not DE_MSYNC;
			LOCK := true;
		elsif VIDEO_HIMODE_I = '1' and DE_I = '0' then
			LOCK := false;
		end if;
	end process P_DE_COUNT;

	-- Serial port:
	COM_TxD <= 	'1' when MFP_SO = '0' and MFP_SO_EN = '1' else
				'0' when MFP_SO = '1' and MFP_SO_EN = '1' else 'Z';
	COM_DTR <= not YM_OUT_A4;
	COM_RTS <= not YM_OUT_A3;

	-- Line printer port.
	LPT_D_IN <= To_BitVector(LPT_D);
	LPT_D <= To_StdLogicVector(IO_B_OUT) when IO_B_EN = '1' else (others => 'Z');

	-- DMA and ACSI/SCSI/SD section:
	CD <= 	CD_OUT_DMA when CD_EN_DMA = '1' else                                -- DMA controller.
			CD_OUT_FDC when CD_EN_FDC = '1' else                                -- Floppy disk controller.
            To_StdLogicVector(ACSI_SCSI_D_OUT) when ACSI_SCSI_D_EN = '1' else   -- ACSI-SCSI bridge.
            ACSI_SD_D_OUT when ACSI_SD_D_EN = '1' else (others => 'Z');         -- SD card bridge.

	ACSI_SCSI_D_IN <= To_BitVector(CD);
	SCSI_DP <= not SCSI_DP_In; -- Inverse polarity due to Suska III schematics.
	SCSI_D <= To_StdLogicVector(SCSI_D_OUT) when SCSI_D_EN = '1' else (others => 'Z');
	SCSI_D_IN <= To_BitVector(SCSI_D);
	SDC_AVR_D <= ADC_AVR_D_OUT when ADC_AVR_D_EN = '1' else (others => 'Z');
	--
    HDCSn <= ACSI_CS_DMAn when CONFIG(5) = '1' else '1';
	HDACKn <= HDACK_In;
	CA1_OUT	<= CA1;
	--
    HDRQ_IN <= '1' when HDRQn = '0' or ACSI_SCSI_HDRQn = '0' or ACSI_SD_DRQn = '0' else '0';
    HDINT_INn <= HDINTn and HDINT_IDEn and HDINT_ACSI_SCSIn and HDINT_ACSI_SDn;
    --
    ACSI_RDn <= '0' when ACSI_CS_DMAn = '0' and CR_Wn = '1' and CONFIG(5) = '1' else -- Transfer of the Header data.
                '0' when DMA_SRC_SEL_I = "00" and CR_Wn = '1' and CONFIG(5) = '1' else '1'; -- DMA transfer.
    ACSI_WRn <= CR_Wn;
    --
    SCSI_CSn <= SCSI_CS_DMAn when CONFIG(5) = '1' else ACSI_CS_DMAn;
    SCSI_RDn <= '0' when SCSI_CS_DMAn = '0' and CR_Wn = '1' and CONFIG(5) = '1' else -- Transfer of the Header data.
                '0' when ACSI_CS_DMAn = '0' and CR_Wn = '1' and CONFIG(5) = '0' else -- Transfer of the Header data, ACSI enabled.
                '0' when DMA_SRC_SEL_I = "01" and CR_Wn = '1' and CONFIG(5) = '1' else -- DMA transfer.
                '0' when DMA_SRC_SEL_I = "00" and CR_Wn = '1' and CONFIG(5) = '0' else '1'; -- DMA transfer, ACSI enabled.
    SCSI_WRn <= CR_Wn;
	SCSI_CTRL_ENn <= not SCSI_CTRL_EN;

	-- Floppy Tri-States:
	FDD_WGn		<= '0' when FDD_WG = '1' else 'Z';
	FDD_WDn		<= '0' when FDD_WD = '1' else 'Z';
	FDD_STEPn	<= '0' when FDD_STEP = '1' else 'Z';
	FDD_DIRCn	<= '0' when FDD_DIRC = '1' else 'Z';
	FDD_MOn		<= '0' when FDD_MO = '1' else 'Z';

	-- MIDI interface:
	MIDI_OLR <= '0' when MIDI_OUT = '0' else 'Z'; 
	MIDI_TLR <= '0' when MIDI_IN = '0' else 'Z';

	-- Bus controls:
	BERR_In <= To_Bit(BERRn);	
	BERRn <= '0' when BERR_GLUEn = '0' else 'Z';

	HALTn <= '0' when HALT_68K00 = '1' else 'Z';

	UDS_INn <= To_Bit(UDSn);
	UDSn <= 	'1' when UDS_OUT_68K00n = '1' and UDS_OUT_EN_68K00 = '1' else
				'0' when UDS_OUT_68K00n = '0' and UDS_OUT_EN_68K00 = '1' else
				'1' when UDS_OUT_BLTn = '1' and BUSCTRL_EN_BLT = '1' else
				'0' when UDS_OUT_BLTn = '0' and BUSCTRL_EN_BLT = '1' else
				'1' when UDS_OUT_GLUEn = '1' and BUSCTRL_EN_GLUE = '1' else
				'0' when UDS_OUT_GLUEn = '0' and BUSCTRL_EN_GLUE = '1' else 'Z';

	LDS_INn <= To_Bit(LDSn);
	LDSn <= 	'1' when LDS_OUT_68K00n = '1' and LDS_OUT_EN_68K00 = '1' else
				'0' when LDS_OUT_68K00n = '0' and LDS_OUT_EN_68K00 = '1' else
				'1' when LDS_OUT_BLTn = '1' and BUSCTRL_EN_BLT = '1' else
				'0' when LDS_OUT_BLTn = '0' and BUSCTRL_EN_BLT = '1' else
				'1' when LDS_OUT_GLUEn = '1' and BUSCTRL_EN_GLUE = '1' else
				'0' when LDS_OUT_GLUEn = '0' and BUSCTRL_EN_GLUE = '1' else 'Z';

	AS_INn <= To_Bit(ASn);
	 -- The first condition of ASn is important for the GLUE's bus error
	 -- logic. See process FLASH_WS.
    ASn <= 	'1' when FLASH_WAITSTATEn = '0' else
            '1' when AS_OUT_68K00n = '1' and AS_OUT_EN_68K00 = '1' else
			'0' when AS_OUT_68K00n = '0' and AS_OUT_EN_68K00 = '1' else
			'1' when AS_OUT_BLTn = '1' and BUSCTRL_EN_BLT = '1' else
			'0' when AS_OUT_BLTn = '0' and BUSCTRL_EN_BLT = '1' else
			'1' when AS_OUT_GLUEn = '1' and BUSCTRL_EN_GLUE = '1' else
			'0' when AS_OUT_GLUEn = '0' and BUSCTRL_EN_GLUE = '1' else 'Z';

	RWn_IN <= To_Bit(RWn);
	RWn <= 	'1' when RWn_OUT_68K00 = '1' and RW_OUT_EN_68K00 = '1' else
			'0' when RWn_OUT_68K00 = '0' and RW_OUT_EN_68K00 = '1' else
			'1' when RWn_OUT_BLT = '1' and BUSCTRL_EN_BLT = '1' else
			'0' when RWn_OUT_BLT = '0' and BUSCTRL_EN_BLT = '1' else
			'1' when RWn_OUT_GLUE = '1' and BUSCTRL_EN_GLUE = '1' else
			'0' when RWn_OUT_GLUE = '0' and BUSCTRL_EN_GLUE = '1' else 'Z';

	FC_In <= To_BitVector(FC);
	FC <= 	FC_OUT_68K00 when FC_OUT_EN_68K00 = '1' else
			FC_OUT_BLT when BUSCTRL_EN_BLT = '1' else "ZZZ";

    FLASH_WS: process (RESETn, CLK_1)
    -- This process provides a delay of seven clock cycles after the
    -- release of the RESETn. This is important for Suska-III-C because
    -- of the flash memory which is also resetted by the RESETn and is
    -- ready to be read at a minimum of 200ns after the release of RESETn.
    -- Without this logic, the CPU reads too fast from the flash memory
    -- when it releases a RESET_MCUn by itself.
    variable TMP: std_logic_vector(2 downto 0);
    begin
        if RESETn = '0' then
            TMP := "000";
            FLASH_WAITSTATEn <= '0';
        elsif CLK_1 = '1' and CLK_1' event then
            if TMP < "111" then
                TMP := TMP + '1';
                FLASH_WAITSTATEn <= '0';
            else
                FLASH_WAITSTATEn <= '1';
            end if;
        end if;
    end process FLASH_WS;

    SLOW_CPU: process(CLK_1, DTACKn, CONFIG, ROM2_In)
    -- For software compatibility, it is sometimes necessary to
    -- slow down the CPU. This is done by a delay of the DTACK_INn
    -- signal for operating system access. Be aware, that the DTACKn
    -- signal of the MCU may not be affected due to strong timing
    -- constraints. This feature helps to fix issues with NOP delays.
    -- The delay of the DTACK_INn causes the CPU to insert waitstates.
    variable TMP : std_logic_vector(2 downto 0);
    begin
        if CLK_1 = '1' and CLK_1' event then
            if DTACKn = '1' then
                TMP := "000";
            elsif TMP /= "110" then
                TMP := TMP + '1';
            end if;
        end if;
        --
        case CONFIG(1) is
            when '1' => DTACK_INn <= To_Bit(DTACKn); -- Not delayed.
            when others => 
                if ROM2_In = '0' and TMP = "110" then -- Slow down flash memory access.
                    DTACK_INn <= '0';
                elsif ROM2_In = '0' then
                    DTACK_INn <= '1';
                else
                    DTACK_INn <= To_Bit(DTACKn);
                end if;
        end case;
    end process SLOW_CPU;

    DTACKn <= 	'1' when FLASH_WAITSTATEn = '0' else -- After a system reset, see process FLASH_WS.
                '0' when DTACK_OUT_BLTn = '0' or DTACK_OUT_GLUEn = '0' else
				'0' when DTACK_OUT_MCUn = '0' or DTACK_OUT_MFPn = '0' else
				'0' when DTACK_OUT_IDEn = '0' else 'Z';
	
	VPA_INn	<= '0' when VPA_GLUE_OUTn = '0' or VPAn = '0' else '1';
	VMAn 	<= 	'1' when VMA_OUT_68K00n = '1' and VMA_OUT_EN_68K00 = '1' else
				'0' when VMA_OUT_68K00n = '0' and VMA_OUT_EN_68K00 = '1' else 'Z';
	E <= E_I;

	BR_In <= BR_BLTn and BR_GLUEn and BRn;
	BGACK_INn <= '0' when BGACK_BLTn = '0' or BGACKn = '0' else '1';
	BGACKn <= '0' when BGACK_BLTn = '0' else 'Z';
	
	HSYNC_In <= not To_Bit(HSYNC);
    HSYNC <= --'1' when HSYNC_On = '1' and SYNC_EN = '1' and CONFIG(3 downto 2) = "01" else -- Inverted for 72Hz multisync.
             --'0' when HSYNC_On = '0' and SYNC_EN = '1' and CONFIG(3 downto 2) = "01" else -- Inverted for 72Hz multisync.
             '0' when HSYNC_On = '1' and SYNC_EN = '1' else
			 '1' when HSYNC_On = '0' and SYNC_EN = '1' else 'Z';
	VSYNC_In <= not To_Bit(VSYNC);
	VSYNC <= --'1' when VSYNC_On = '1' and SYNC_EN = '1' and CONFIG(3 downto 2) = "01" else -- Inverted for 72Hz multisync.
			 --'0' when VSYNC_On = '0' and SYNC_EN = '1' and CONFIG(3 downto 2) = "01" else -- Inverted for 72Hz multisync.
             '0' when VSYNC_On = '1' and SYNC_EN = '1' else
			 '1' when VSYNC_On = '0' and SYNC_EN = '1' else 'Z';

	-- Interrupt stuff:
    AVEC_INn <= AVECn and AVEC_GLUEn; -- One Low active signal is sufficient.
    IRQ_ACIAn <= IRQ_KEYBDn and IRQ_MIDIn;

	-- DS1392 RTC interface:
	DS1392_IN <= To_Bit(DS1392_D);
	DS1392_D <= '1' when DS1392_OUT = '1' and DS1392_OUT_EN = '1' else
				'0' when DS1392_OUT = '0' and DS1392_OUT_EN = '1' else 'Z';

    I_CPU: WF68K00IP_TOP_SOC -- Use for the WF flavoured CPU.
    --I_CPU: WF68KC00_TOP_SOC -- Use for the prefetch compatible CPU.
        port map(
			CLK					=> CLK_1,
			RESET_COREn			=> RESET_CPUn,

			-- Address and Data:
			ADR_OUT				=> ADR_OUT_68K00,
			ADR_EN				=> ADR_EN_68K00,
			DATA_IN				=> DATA,
			DATA_OUT			=> DATA_OUT_68K00,
			DATA_EN				=> DATA_EN_68K00,

			-- System control:
			BERRn				=> BERR_In,
			RESET_INn			=> RESET_INn,
			RESET_OUT_EN		=> RESET_EN_68K00,
			HALT_INn			=> HALT_INn,
			HALT_OUT_EN			=> HALT_68K00,

			-- Processor status:
			FC_OUT				=> FC_OUT_68K00,
			FC_OUT_EN			=> FC_OUT_EN_68K00,

			-- Interrupt control:
			AVECn				=> AVEC_INn,
			IPLn				=> IPLn,

			-- Aynchronous bus control:
			DTACKn				=> DTACK_INn,
			AS_OUTn				=> AS_OUT_68K00n,
			AS_OUT_EN			=> AS_OUT_EN_68K00,
			RWn_OUT				=> RWn_OUT_68K00,
			RW_OUT_EN			=> RW_OUT_EN_68K00,
			UDS_OUTn			=> UDS_OUT_68K00n,
			UDS_OUT_EN			=> UDS_OUT_EN_68K00,
			LDS_OUTn			=> LDS_OUT_68K00n,
			LDS_OUT_EN			=> LDS_OUT_EN_68K00,

			-- Synchronous peripheral control:
			E					=> E_I,
			VMA_OUTn			=> VMA_OUT_68K00n,
			VMA_OUT_EN			=> VMA_OUT_EN_68K00,
			VPAn				=> VPA_INn,

			-- Bus arbitration control:
			BRn					=> BR_In,
			BGn					=> BG_68K00n,
			BGACKn				=> BGACK_INn
			);

	I_BLITTER: WF101643IP_TOP_SOC
		port map(
			-- System controls:
			CLK					=> CLK_1,
			RESETn				=> RESET_INn,

            AS_INn				=> AS_INn,
			AS_OUTn				=> AS_OUT_BLTn,
			LDS_INn				=> LDS_INn,
			LDS_OUTn			=> LDS_OUT_BLTn,
			UDS_INn				=> UDS_INn,
			UDS_OUTn			=> UDS_OUT_BLTn,
			RWn_IN				=> RWn_IN,
			RWn_OUT				=> RWn_OUT_BLT,
			DTACK_INn			=> DTACK_INn,
			DTACK_OUTn			=> DTACK_OUT_BLTn,
			BERRn				=> BERR_In,
			FC_IN				=> FC_IN,
			FC_OUT				=> FC_OUT_BLT,
			BUSCTRL_EN			=> BUSCTRL_EN_BLT,
			INTn				=> INT_BLTn,

			-- The bus:
			ADR_IN				=> ADR_IN,
			ADR_OUT				=> ADR_OUT_BLT,
			ADR_EN				=> ADR_EN_BLT,
			DATA_IN				=> DATA,
			DATA_OUT			=> DATA_OUT_BLT,
			DATA_EN				=> DATA_EN_BLT,

			-- Bus arbitration:
			BGIn				=> BG_68K00n,
			BGKIn				=> BGACK_GLUEn,
			BRn					=> BR_BLTn,
			BGACK_INn			=> BGACK_INn,
			BGACK_OUTn			=> BGACK_BLTn,
			BGOn				=> BG_BLTn
		);

	I_GLUE: WF25915IP_TOP_V1_SOC
		port map(
			-- Clock system:
			GL_CLK				=> CLK_1,
			GL_CLK_016			=> CLK_0M5,

			-- Adress decoder:
            GL_ROMSEL_FC_E0n    => ROMSEL_FC_E0n,
            EN_RAM_14MB         => not CONFIG(4),
			GL_ROM_6n			=> ROM6n,
			GL_ROM_5n			=> ROM5n,
			GL_ROM_4n			=> ROM4n,
			GL_ROM_3n			=> ROM3n,
			GL_ROM_2n			=> ROM2_In,
			GL_ROM_1n			=> ROM1n,
			GL_ROM_0n			=> ROM0n,

			GL_ACIACS			=> ACIA_CS_I,
			GL_MFPCSn			=> MFP_CS_In,
			-- GL_SNDCSn			=>, -- Not used.
			GL_FCSn				=> FCS_In,

			GL_STE_SNDCS		=> SNDCS_I,
			GL_STE_SNDIR		=> SNDIR_I,

			-- RP5C15 real time clock:
			GL_STE_RTCCSn		=> RP5C15_CSn,
			GL_STE_RTC_WRn		=> RP5C15_WRn,
			GL_STE_RTC_RDn		=> RP5C15_RDn,

			-- 6800 peripheral control:
			GL_VPAn				=> VPA_GLUE_OUTn,
			GL_VMAn				=> VMA_OUT_68K00n,

			GL_DMA_SYNC			=> DMA_SYNC_I,
			GL_DEVn				=> DEV_In,
			GL_RAMn				=> RAM_In,
			GL_DMAn				=> DMA_In,

			-- Interrupt system:
			GL_AVECn			=> AVEC_GLUEn,
			GL_STE_FDINT		=> FDINT,
			GL_STE_HDINTn		=> HDINT_INn,
			GL_MFPINTn			=> MFPINTn,
			GL_STE_EINT3n		=> EINT3n,
			GL_STE_EINT5n		=> EINT5n,
			GL_STE_EINT7n		=> EINT7n,
			GL_STE_DINTn		=> DINTn,
			GL_IACKn			=> IACKn,
			GL_STE_IPL2n		=> IPLn(2),
			GL_STE_IPL1n		=> IPLn(1),
			GL_STE_IPL0n		=> IPLn(0),

			-- Video timing:
			GL_BLANKn			=> BLANKn,
			GL_DE				=> DE_I,
			GL_MULTISYNC 		=> MULTISYNC_I,
            GL_VIDEO_HIMODE     => VIDEO_HIMODE_I,
			GL_HSYNC_INn		=> HSYNC_In,
			GL_HSYNC_OUTn		=> HSYNC_On,
			GL_VSYNC_INn		=> VSYNC_In,
			GL_VSYNC_OUTn		=> VSYNC_On,
			GL_SYNC_OUT_EN		=> SYNC_EN,

			-- Bus arbitration control:
			GL_RDY_INn			=> RDY_DMA,
			GL_RDY_OUTn			=> RDY_GLUE,
			GL_BRn				=> BR_GLUEn,
			GL_BGIn				=> BG_BLTn,
			GL_BGOn				=> BGOn,
			GL_BGACK_INn		=> BGACK_BLTn,
			GL_BGACK_OUTn		=> BGACK_GLUEn,

			-- Adress and data bus:
			GL_ADDRESS			=> ADR_IN,
			GL_DATA_IN			=> DATA(15 downto 8),
			GL_DATA_OUT			=> DATA_OUT_GLUE,
			GL_DATA_EN			=> DATA_EN_GLUE,

			-- Asynchronous bus control:
			GL_RWn_IN			=> RWn_IN,
			GL_RWn_OUT			=> RWn_OUT_GLUE,
			GL_AS_INn			=> AS_INn,
			GL_AS_OUTn			=> AS_OUT_GLUEn,
			GL_UDS_INn			=> UDS_INn,
			GL_UDS_OUTn			=> UDS_OUT_GLUEn,
			GL_LDS_INn			=> LDS_INn,
			GL_LDS_OUTn			=> LDS_OUT_GLUEn,
			GL_DTACK_INn		=> DTACK_INn,
			GL_DTACK_OUTn		=> DTACK_OUT_GLUEn,
			GL_CTRL_EN			=> BUSCTRL_EN_GLUE,

			-- System control:
			GL_RESETn			=> RESET_INn,
			GL_BERRn			=> BERR_GLUEn,

			-- Processor function codes:
			GL_FC				=> FC_IN,

			-- STE enhancements:
			-- GL_STE_FDDS		=>, -- Not used yet.
			GL_STE_FCCLK		=> CLK_AUX,
			GL_STE_JOY_RHn		=> JOY_RHn,
			GL_STE_JOY_RLn		=> JOY_RLn,
			GL_STE_JOY_WL		=> JOY_WL,
			GL_STE_JOY_WEn		=> JOY_WEn,
			GL_STE_BUTTONn		=> BUTTONn,
			GL_STE_PAD0Xn		=> PAD0Xn,
			GL_STE_PAD0Yn		=> PAD0Yn,
			GL_STE_PAD1Xn		=> PAD1Xn,
			GL_STE_PAD1Yn		=> PAD1Yn,
			GL_STE_PADRSTn		=> PADRSTn,
			GL_STE_PENn			=> PENn
			-- GL_STE_SCCn		=>, -- Not used yet.
			-- GL_STE_CPROGn	=> -- Not used yet.
			);

	I_MCU: WF25912IP_SD_TOP_SOC
		port map(  
            CLK					=> CLK_2,
			RESET_INn			=> RESET_CORE_Sn,
			RESET_OUTn			=> RESET_MCUn,
			
			ASn					=> AS_INn,
			LDSn				=> LDS_INn,
			UDSn				=> UDS_INn,
			RWn					=> RWn_IN,

			ADR					=> MCU_ADR, -- (25 downto 1).

			RAMn				=> RAM_In,
			DMAn				=> DMA_In,
			DEVn				=> DEV_In,
			DMA_SYNC			=> DMA_SYNC_I,

			VSYNCn				=> VSYNC_On,
			DE					=> DE_I,
            VIDEO_HIMODE        => VIDEO_HIMODE_I,
			
			DCYCn				=> DCYCn,
			CMPCSn				=> CMPCSn,

			MONO_DETECTn		=> MONO_CRTn,
			EXT_CLKSELn			=> CRT_PIN3,
			SREQ				=> SREQ,
			SLOADn				=> SLOADn,
			SINT_TAI			=> SINT_TAI,
			SINT_IO7			=> SINT_IO7,

			BA					=> RAM_BA,
			MAD					=> RAM_ADR,

			WEn					=> RAM_WEn,

			DQM0H				=> RAM_DQM0H,
			DQM0L				=> RAM_DQM0L,
			DQM1H				=> RAM_DQM1H,
			DQM1L				=> RAM_DQM1L,
			
			RAS0n				=> RAM_RAS0n,
			RAS1n				=> RAM_RAS1n,

			CAS0n				=> RAM_CAS0n,
			CAS1n				=> RAM_CAS1n,

			RDATn				=> RDATn,
			WDATn				=> WDATn,
			LATCHn				=> LATCHn,

			DTACKn				=> DTACK_OUT_MCUn,

			DATA_IN				=> DATA(7 downto 0),
			DATA_OUT			=> DATA_OUT_MCU,
			DATA_EN				=> DATA_EN_MCU
		);

	I_DMA: WF25913IP_TOP_SOC
		port map(
			-- system controls:
			RESETn				=> RESET_INn,
			CLK					=> CLK_1,

			FCSn				=> FCS_In,
			A1				    => ADR_IN(1),
			RWn					=> RWn_IN,
			RDY_INn				=> RDY_GLUE,
			RDY_OUTn			=> RDY_DMA,
			DATA_IN				=> DATA,
			DATA_OUT			=> DATA_OUT_DMA,
			DATA_EN				=> DATA_EN_DMA,

            -- ACSI mode selection:
            DMA_SRC_SEL         => DMA_SRC_SEL_I,         

			-- ACSI section:
			CA2					=> CA2,
			CA1					=> CA1,
			CR_Wn				=> CR_Wn,
			CD_IN				=> CD,
			CD_OUT				=> CD_OUT_DMA,
			CD_EN				=> CD_EN_DMA,

			FDCSn				=> FDCS_In,
			SDCSn				=> SD_CSn,
			SCSICSn				=> SCSI_CS_DMAn,
			HDCSn				=> ACSI_CS_DMAn,
			FDRQ				=> FDRQ,
			HDRQ				=> HDRQ_IN,
			ACKn				=> HDACK_In
		);

	I_FDC: WF1772IP_TOP_SOC
		port map(
            CLK					=> CLK_1,
			RESETn				=> RESET_INn,

			CSn					=> FDCS_In,
			RWn					=> CR_Wn,
			A1					=> CA2,
			A0					=> CA1,
			DATA_IN				=> CD,
			DATA_OUT			=> CD_OUT_FDC,
			DATA_EN				=> CD_EN_FDC,
			RDn					=> FDD_RDn,
			TR00n				=> FDD_TR00,
			IPn					=> FDD_IPn,
			WPRTn				=> FDD_WPn,
			DDEn				=> '0', -- Fixed to MFM.
			HDTYPE				=> FDTYPE,
			MO					=> FDD_MO,
			WG					=> FDD_WG,
			WD					=> FDD_WD,
			STEP				=> FDD_STEP,
			DIRC				=> FDD_DIRC,
			DRQ					=> FDRQ,
			INTRQ				=> FDINT
		);

	I_SHIFTER: WF25914IP_TOP_SOC
		port map(
			CLK					    => CLK_2,
            RESETn				    => RESET_INn,

			SH_A				    => ADR_IN(6 downto 1),
			SH_D_IN				    => RAM_DATA,
			SH_D_OUT			    => DATA_SHFT,
			SH_DATA_HI_EN		    => DATA_EN_HI_SHFT,
			SH_DATA_LO_EN		    => DATA_EN_LO_SHFT,
			SH_RWn				    => RWn_IN,
			SH_CSn				    => CMPCSn,

			MULTISYNC 				=> MULTISYNC_I,
			SH_LOADn			    => DCYCn,
			SH_DE				    => DE_I,
			SH_BLANKn			    => BLANKn,
			-- CR_1512			    =>, -- Not used.
			SH_R				    => CRT_R,
			SH_G				    => CRT_G,
			SH_B				    => CRT_B,
			SH_MONO				    => CRT_MONO,
			-- SH_COLOR			    =>, -- Not used.

			SH_SCLK				    => SCLK_6M4,
			SH_FCLK				    => FCLK,
			SH_SLOADn			    => SLOADn,
			SH_SREQ				    => SREQ,
			SH_SDATA_L			    => SDATA_L,
			SH_SDATA_R			    => SDATA_R,

			SH_MWK				    => MWK,
			SH_MWD				    => MWD,
			SH_MWEn				    => MWEn,

			xFF827E_D(7 downto 2)  => xFF827E_D
			-- xFF827E_D(1 downto 0)  => -- Reserved for future use.
		);

	I_SHADOW: WF_SHD101775IP_TOP_SOC
		port map(
			RESETn				=> RESET_INn,
			CLK					=> CLK_1,

			-- Video control:
			M_DATA				=> SHADOW_DATA,
			DE					=> DE_I,
			LOADn				=> DCYCn,

			R_ADR				=> SHADOW_VRAM_ADR,
			R_DATA_IN			=> VRAM_D_OUT,
			R_DATA_OUT			=> VRAM_D_IN,
            -- R_DATA_EN        =>, -- Not used.
			R_WRn				=> SHADOW_VRAM_WRn,

			-- LCD control:
			UDATA				=> UDATA,
			LDATA				=> LDATA,
			LFS					=> LFS,
			VDCLK				=> VDCLK,
			LLCLK				=> LLCLK
		);

	I_MFP: WF68901IP_TOP_SOC
		port map(  
			-- System control:
			CLK					=> CLK_1,
			RESETn				=> RESET_INn,

			-- Asynchronous bus control:
			DSn					=> LDS_INn,
			CSn					=> MFP_CS_In,
			RWn					=> RWn_IN,
			DTACKn				=> DTACK_OUT_MFPn,
	
			-- Data and Adresses:
			RS					=> ADR_IN(5 downto 1),
			DATA_IN				=> DATA(7 downto 0),
			DATA_OUT			=> DATA_OUT_MFP,
			DATA_EN				=> DATA_EN_MFP,
            GPIP_IN(7)			=> SINT_IO7,
            GPIP_IN(6)			=> not COM_RI,
			GPIP_IN(5)			=> DINTn,
			GPIP_IN(4)			=> IRQ_ACIAn,
			GPIP_IN(3)			=> INT_BLTn,
            GPIP_IN(2)			=> not COM_CTS,
            GPIP_IN(1)			=> not COM_DCD,
            GPIP_IN(0)			=> LPT_BSY,
			-- GPIP_OUT			=>, -- Not used; all GPIPs are direction input.
			-- GPIP_EN			=>, -- Not used; all GPIPs are direction input.
	
			-- Interrupt control:
			IACKn				=> IACKn,
			IEIn				=> '0',
			-- IEOn				=>, -- Not used.
			IRQn				=> MFPINTn,
	
			-- Timers and timer control:
			XTAL1				=> CLK_24576,
			TAI					=> SINT_TAI,
			TBI					=> DE_MSYNC,
            -- TAO				=>, -- Not used.
			-- TBO				=>, -- Not used.
            -- TCO				=>, -- Not used.
			TDO					=> TDO,
	
			-- Serial I/O control:
			RC					=> TDO,
			TC					=> TDO,
			SI					=> COM_RxD,
			SO					=> MFP_SO,
			SO_EN				=> MFP_SO_EN

			-- DMA control:
			-- RRn				=>, -- Not used.
			-- TRn				=> -- Not used.
		);

	I_SOUND: WF2149IP_TOP_SOC
		port map(
			SYS_CLK				=> CLK_1,
			RESETn				=> RESET_INn,

			WAV_CLK				=> CLK_2M0,
			SELn				=> '1',

			BDIR				=> SNDIR_I,
			BC2					=> '1',
			BC1					=> SNDCS_I,

			A9n					=> '0',
			A8					=> '1',
			DA_IN				=> DATA(15 downto 8),
			DA_OUT				=> DATA_OUT_SOUND,
			DA_EN				=> DATA_EN_SOUND,

			IO_A_IN				=> x"00", -- All port pins are dedicated outputs.
			-- IO_A_OUT(7)		=>, -- Not used so far.
			IO_A_OUT(6)			=> GPO,
			IO_A_OUT(5)			=> LPT_STRB,
			IO_A_OUT(4)			=> YM_OUT_A4,
			IO_A_OUT(3)			=> YM_OUT_A3,
			IO_A_OUT(2)			=> FDD_D1SEL,
			IO_A_OUT(1)			=> FDD_D0SEL,
			IO_A_OUT(0)			=> FDD_SDSEL,
			-- IO_A_EN			=>, -- Not required.
			IO_B_IN				=> LPT_D_IN,
			IO_B_OUT			=> IO_B_OUT,
			IO_B_EN				=> IO_B_EN,

			OUT_A				=> YM_OUT_A,
			OUT_B				=> YM_OUT_B,
			OUT_C				=> YM_OUT_C
		);

	I_ACIA_KEYBOARD: WF6850IP_TOP_SOC
	  port map(
			CLK					=> CLK_1,
			RESETn				=> RESET_INn,

			CS2n				=> ADR_IN(2),
			CS1					=> '1',
			CS0					=> ACIA_CS_I,
			E					=> E_I,
			RWn					=> RWn_IN,
			RS					=> ADR_IN(1),

			DATA_IN				=> DATA(15 downto 8),
			DATA_OUT			=> DATA_OUT_ACIA_I,
			DATA_EN				=> DATA_EN_ACIA_I,

			TXCLK				=> CLK_0M5,
			RXCLK				=> CLK_0M5,
			RXDATA				=> KEYB_RxD,

			CTSn				=> '0', -- In original ST machines wired to GND.
			DCDn				=> '0', -- In original ST machines wired to GND.
        
			IRQn				=> IRQ_KEYBDn,
			TXDATA				=> KEYB_TxD
			--RTSn				=> -- Not used.
		);                                              

	I_ACIA_MIDI: WF6850IP_TOP_SOC
		port map(
			CLK					=> CLK_1,
			RESETn				=> RESET_INn,

			CS2n				=> '0',
			CS1					=> ADR_IN(2),
			CS0					=> ACIA_CS_I,
			E					=> E_I,
			RWn					=> RWn_IN,
			RS					=> ADR_IN(1),

			DATA_IN				=> DATA(15 downto 8),
			DATA_OUT			=> DATA_OUT_ACIA_II,
			DATA_EN				=> DATA_EN_ACIA_II,

			TXCLK				=> CLK_0M5,
			RXCLK				=> CLK_0M5,
			RXDATA				=> MIDI_IN,
			CTSn				=> UART_MIDI_CTSn,
			DCDn				=> UART_MIDI_DCDn,
        
			IRQn				=> IRQ_MIDIn,
			TXDATA				=> MIDI_OUT,
			RTSn				=> UART_MIDI_RTSn
	    );                                              

	I_RTC5C15: WF5C15_139xIP_TOP
		port map(
			CLK					=> CLK_1,
			RESETn				=> RESET_INn,

			-- The bus interface:
			ADR					=> ADR_IN(4 downto 1),
			DATA_IN				=> DATA(3 downto 0),
			DATA_OUT			=> DATA_OUT_RP5C15,
			DATA_EN				=> DATA_EN_RP5C15,
			CS					=> '1',
			CSn					=> RP5C15_CSn,
			WRn					=> RP5C15_WRn,
			RDn					=> RP5C15_RDn,

			-- The SPI lines:
			SPI_IN				=> DS1392_IN,
			SPI_OUT				=> DS1392_OUT,
			SPI_ENn				=> DS1392_OUT_EN,
			SPI_SCL				=> DS1392_SCL,
			SPI_CSn				=> DS1392_CSn
		);

	I_ACSI_SCSI: WF_ACSI_SCSI_IF_SOC
		port map(  
            RESETn				=> RESET_INn,
			CLK					=> CLK_1,

			CR_Wn				=> CR_Wn,
			CA1					=> CA1,
			HDCSn				=> SCSI_CSn,
			HDACKn				=> HDACK_In,
			HDINTn				=> HDINT_ACSI_SCSIn,
			HDRQn				=> ACSI_SCSI_HDRQn,
			ACSI_D_IN			=> ACSI_SCSI_D_IN,
            ACSI_D_OUT			=> ACSI_SCSI_D_OUT,
            ACSI_D_EN		    => ACSI_SCSI_D_EN,
			SCSI_BUSYn			=> SCSI_BUSYn,
			SCSI_MSGn			=> SCSI_MSGn,
			SCSI_REQn			=> SCSI_REQn,
			SCSI_DCn			=> SCSI_DCn,
			SCSI_IOn			=> SCSI_IOn,
			SCSI_RSTn			=> SCSI_RSTn,
			SCSI_ACKn			=> SCSI_ACKn,
			SCSI_SELn			=> SCSI_SELn,
			SCSI_DPn			=> SCSI_DP_In,
			SCSI_D_IN			=> SCSI_D_IN,
			SCSI_D_OUT			=> SCSI_D_OUT,
			SCSI_D_EN			=> SCSI_D_EN,
			SCSI_CTRL_EN		=> SCSI_CTRL_EN,
			SCSI_IDn			=> SCSI_IDn
		);

	I_IDE: WF_IDE
		port map(
            RESETn				=> RESET_INn,

			ADR					=> ADR_IN(23 downto 4),
			
			ASn					=> AS_INn,
            LDSn                => LDS_INn,
			RWn					=> RWn_IN,
            DMAn                => DMA_In,
			DTACKn				=> DTACK_OUT_IDEn,
			
			-- Interrupt via ACSI:
			ACSI_HDINTn			=> HDINT_IDEn,
			
			-- IDE section:
			IDE_INTRQ			=> IDE_INTRQ,
			IDE_IORDY			=> IDE_IORDY,
			--IDE_RESn			=> , -- not used.
			CS0n				=> IDE_CS0n,
			CS1n				=> IDE_CS1n,
			IORDn				=> IDE_IORDn,
			IOWRn				=> IDE_IOWRn,
			
			IDE_D_EN_INn		=> IDE_D_EN_INn,
			IDE_D_EN_OUTn		=> IDE_D_EN_OUTn
	      );

	I_SD_CARD: WF_SD_CARD
		port map(
			-- System:
			RESETn				=> RESET_INn,
            CLK					=> CLK_1, -- 16MHz.

			-- ACSI section:		
			ACSI_A1				=> CA1,
			ACSI_CSn		    => SD_CSn,
			ACSI_ACKn			=> HDACK_In,
			ACSI_INTn			=> HDINT_ACSI_SDn,
			ACSI_DRQn			=> ACSI_SD_DRQn,
			ACSI_D_IN			=> CD,
            ACSI_D_OUT			=> ACSI_SD_D_OUT,
            ACSI_D_EN			=> ACSI_SD_D_EN,

			-- Microcontroller interface:
			MC_DO				=> SDC_AVR_DO,
			MC_PIO_DMAn			=> SDC_AVR_PIO_DMAn,
			MC_RWn				=> SDC_AVR_RWn,
			MC_CLR_CMD			=> SDC_AVR_CLR_CMD,
			MC_DONE				=> SDC_AVR_DONE,
			MC_GOT_CMD			=> SDC_AVR_GOT_CMD,
			MC_D_IN				=> SDC_AVR_D,
			MC_D_OUT			=> ADC_AVR_D_OUT,
			MC_D_EN				=> ADC_AVR_D_EN
	      );

	I_FLASHBOOT: WF_FLASHBOOT
		port map(
			CLK						=> CLK_1, -- 16MHz.
			PLL_LOCK				=> PLL_LOCKS,
			RESET_COREn				=> RESET_CORE_Sn,
			RESET_INn				=> RESET_Sn,
			RESET_OUTn				=> RESET_BOOTn,

			ROM_CEn					=> ROM2_In,
			ADR_OUT(19 downto 0)	=> ADR_OUT_BOOT, -- High address bits currently not in use.
			ADR_EN					=> ADR_EN_BOOT,
			DATA_IN					=> DATA,
			DATA_OUT				=> DATA_OUT_BOOT,
			DATA_EN					=> DATA_EN_BOOT,
			FLASH_RDY				=> FLASH_RDY,
			FLASH_RESETn			=> FLASH_RESET_In,
			FLASH_WEn				=> FLASH_WEn,
			FLASH_OEn				=> FLASH_OEn,
			FLASH_CEn				=> FLASH_CEn,
			SPI_CLK					=> MC_SPI_CLK,
			SPI_DIN					=> MC_SPI_DOUT,
			SPI_DOUT				=> MC_SPI_DIN,
			BOOT_ACK				=> BOOT_ACK,
			BOOT_REQ				=> BOOT_REQ,
			BOOT_LED				=> BOOT_LED
		);

	I_AUDIODAC: WF_AUDIO_DAC
		port map(
			CLK					=> CLK_1, -- 16MHz.
			RESETn				=> RESET_INn,

			SDATA_L				=> SDATA_L,
			SDATA_R				=> SDATA_R,
			DAC_SCLK			=> DAC_SCLK,
			DAC_SDATA			=> DAC_SDATA,
			DAC_SYNCn			=> DAC_SYNCn,
			DAC_LDACn			=> DAC_LDACn
		);
end architecture STRUCTURE;