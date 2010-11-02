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
---- Address decoder file.                                        ----
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
---- Public License asFCS published by the Free Software Foundation; ----
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
-- Revision 2K8A  2008/02/13 WF
--   The decoder for RAMn now uses the ASn signal too.
--   The ROM2n is extended to detect also 512K ROMs for emuTOS.

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25915IP_ADRDEC is
	port (  ADR				: in bit_vector(23 downto 1); --Adress inputs.
			RWn				: in bit; -- Read write control.
			
			RESETn			: in bit; -- System reset.

			TOS_CONFIG		: in integer range 0 to 7; -- Selects the decoding of the ROMs.
            ROMSEL_FC_E0n   : in bit; -- '1' for TOS 1.x, '0' for TOS 2.x address space (core only).
            EN_RAM_14MB     : in bit; -- '1' enables 14MB RAM address space, '0' is 4MB.

			LDSn			: in bit; -- Lower data strobe; not used so far.
			UDSn			: in bit; -- Upper data strobe.
			
		    ASn				: in bit; -- Adress strobe signal indicates valid adress.
	        VPAn			: out bit; -- Valid peripheral adress.
			VMAn			: in bit; -- Valid memory adress; for 6850 (ACIA) access.

			FC				: in bit_vector(2 downto 0); -- Processor function codes.

			DMAn			: in bit; -- Control signal for the DMA transfer.
			DMA_LOCKn		: in bit; -- Control for locking during DMA access.

		    ROM_0n			: out bit; -- Adress select bit for ROM 0, (compatibility to ST/STE hardware), active low.
		    ROM_1n			: out bit; -- Adress select bit for ROM 1, (compatibility to ST/STE hardware), active low.
		    ROM_2n			: out bit; -- Adress select bit for ROM 2, (compatibility to ST/STE hardware), active low.
		    ROM_3n			: out bit; -- Adress select bit for ROM 3, (compatibility to ST/STE hardware), active low.
		    ROM_4n			: out bit; -- Adress select bit for ROM 4, (compatibility to ST/STE hardware), active low.
		    ROM_5n			: out bit; -- Adress select bit for ROM 3, (compatibility to ST/STE hardware), active low.
		    ROM_6n			: out bit; -- Adress select bit for ROM 4, (compatibility to ST/STE hardware), active low.
			PATCHn			: out bit; -- Dummy if the patched TOS is installed.
	        ACIACS			: out bit; -- Select signal for the ACIA.
	        MFPCSn			: out bit; -- Select signal for the MFP.
	        SNDCSn			: out bit; -- Select signal for the SOUND.
	        FCSn			: out bit; -- Select signal for harddrive / floppy via DMA.
	        SCCn			: out bit; -- Select signal for the STE or TT SCC chip.
			CPROGn			: out bit; -- Select signal for the STE's cache processor.
			HD_REG_CSn		: out bit; -- Select signal for the high density floppy control register.
	        RTCCSn			: out bit; -- Select signal for the real time clock.
		    SYNCMODE_CSn	: out bit; -- Select signal for the GLUE internal sync mode register.
			SHIFTMODE_CSn	: out bit; -- Select signal for the shift mode mirror register.
		    DMA_MODE_CSn	: out bit; -- Chip select of the mirror register of the DMA unit.
	        DEVn			: out bit; -- Peripheral select signal.
	        RAMn			: out bit; -- Ram select signal.
			JOY_CS			: out bit; -- Joystick read and write register chip select.
			PAD0X_CS		: out bit; -- Paddle counter register chip select.
			PAD0Y_CS		: out bit; -- Paddle counter register chip select.
			PAD1X_CS		: out bit; -- Paddle counter register chip select.
			PAD1Y_CS		: out bit; -- Paddle counter register chip select.
			BUTTON_CS		: out bit; -- Button register chip select.
			XPEN_REG_CS		: out bit; -- Light pen register.
			YPEN_REG_CS		: out bit -- Light pen register.
	      );
end WF25915IP_ADRDEC;

architecture BEHAVIOR of WF25915IP_ADRDEC is
alias ADR_HI	: bit_vector(23 downto 16) is ADR(23 downto 16);
signal ADR_INT	: bit_vector(23 downto 0);
signal CTOS_RD	: bit; -- Core TOS for 512K ROMs.
signal STE_RD	: bit;
signal ST_RD	: bit;
signal ST_P_RD	: bit;
signal R_READ	: bit;
signal SU		: boolean;
begin
	-- Generation of the complete 24 bit wide adress:
	ADR_INT(23 downto 0) <= ADR(23 downto 1) & '0';

	CTOS_RD <= '1' when TOS_CONFIG = 4 and DMA_LOCKn = '1' and ASn = '0' and RWn = '1' else '0';
	STE_RD <= '1' when TOS_CONFIG = 0 and DMA_LOCKn = '1' and ASn = '0' and RWn = '1' else '0';
	ST_RD <= '1' when TOS_CONFIG = 1 and DMA_LOCKn = '1' and ASn = '0' and RWn = '1' else '0';
	ST_P_RD <= '1' when TOS_CONFIG = 2 and DMA_LOCKn = '1' and ASn = '0' and RWn = '1' else '0';
	R_READ <= '1' when DMA_LOCKn = '1' and ASn = '0' and RWn = '1' else '0'; -- For all TOS versions.
	SU <= true when FC = "101" or FC = "110" else false; -- Superuser mode.

 	ROM_6n <= '0' when STE_RD = '1' and ADR_HI >= x"FA" and ADR_HI < x"FC" else
			  '0' when CTOS_RD = '1' and ADR_HI >= x"FA" and ADR_HI < x"FC" else '1';	-- Cartridge ROM (STE).
	ROM_5n <= '0' when STE_RD = '1' and ADR_HI >= x"FE" and ADR_HI < x"FF" else 
			  '0' when CTOS_RD = '1' and ADR_HI >= x"FE" and ADR_HI < x"FF" else '1'; -- Cartridge ROM (STE).
	ROM_4n <= '0' when R_READ = '1' and ADR_HI >= x"FA" and ADR_HI < x"FB" else '1';	-- Cartridge ROM.
	ROM_3n <= '0' when R_READ = '1' and ADR_HI >= x"FB" and ADR_HI < x"FC" else '1';	-- Cartridge ROM.
	ROM_2n <= '0' when STE_RD = '1' and ADR_HI >= x"E0" and ADR_HI < x"E4" else			-- STE TOS complete.
			  '0' when CTOS_RD = '1' and ROMSEL_FC_E0n = '0' and ADR_HI >= x"E0" and ADR_HI < x"E8" else -- 512K TOS ROMs (emutos).
              '0' when CTOS_RD = '1' and ROMSEL_FC_E0n = '1' and ADR_HI >= x"FC" and ADR_HI < x"FF" else -- 192K TOS ROMs.
			  '0' when ST_P_RD = '1' and ADR_HI >= x"FC" and ADR_HI < x"FD" else		-- ST TOS ROM LOW.
			  '0' when ST_RD = '1' and ADR_HI >= x"FC" and ADR_HI < x"FD" else			-- ST TOS ROM LOW.
  	          '0' when R_READ = '1' and ADR_INT < x"000008" else '1'; 					-- TOS mirroring.
	ROM_1n <= '0' when STE_RD = '1' and ADR_HI >= x"E0" and ADR_HI < x"E2" else			-- STE TOS ROM LO.
			  '0' when CTOS_RD = '1' and ADR_HI >= x"E0" and ADR_HI < x"E2" else		-- STE TOS ROM LO for compatibility.
			  '0' when ST_P_RD = '1' and ADR_HI >= x"E0" and ADR_HI < x"E2" else		-- ST TOS ROM MID.
			  '0' when ST_RD = '1' and ADR_HI >= x"FD" and ADR_HI < x"FE" else			-- ST TOS ROM MID.
			  '0' when STE_RD = '1' and ADR_INT < x"000008" else '1'; 					-- TOS mirroring.
	ROM_0n <= '0' when STE_RD = '1' and ADR_HI >= x"E2" and ADR_HI < x"E4" else			-- STE TOS ROM HI.
			  '0' when CTOS_RD = '1' and ADR_HI >= x"E2" and ADR_HI < x"E4" else		-- STE TOS ROM HI for compatibility.
			  '0' when ST_P_RD = '1' and ADR_HI >= x"E2" and ADR_HI < x"E4" else		-- ST TOS ROM HI.
			  '0' when ST_RD = '1' and ADR_HI >= x"FE" and ADR_HI < x"FF" else '1';		-- ST TOS ROM HI.
	PATCHn <= '0' when ST_P_RD = '1' and ADR_HI >= x"FD" and ADR_HI < x"FF" else '1';	-- Dummy for DTACkn.

	-- Memo-mapped chip selects:
	-- DMA_LOCKn prevents the system of asserting chip selects during DMA transfer.
	-- Select ACIA, write access in SU mode:
	ACIACS <= '1' when ASn = '0' and ADR_INT >= x"FFFC00" and ADR_INT < x"FFFC08" and RWn = '0' and SU = true and VMAn = '0' and DMA_LOCKn = '1' else
			  '1' when ASn = '0' and ADR_INT >= x"FFFC00" and ADR_INT < x"FFFC08" and RWn = '1' and VMAn = '0' and DMA_LOCKn = '1' else '0';

	-- Validation for ACIACSn:
	VPAn <= '0' when ASn = '0' and ADR_INT >= x"FFFC00" and ADR_INT < x"FFFC08" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
			'0' when ASn = '0' and ADR_INT >= x"FFFC00" and ADR_INT < x"FFFC08" and RWn = '1' and DMA_LOCKn = '1' else '1';

	-- Select MFP (8 bit access), write access in superuser mode:
	MFPCSn <= 	'0' when ASn = '0' and ADR_INT >= x"FFFA00" and ADR_INT < x"FFFA40" and  RWn = '0' and SU = true and DMA_LOCKn = '1' else
				'0' when ASn = '0' and ADR_INT >= x"FFFA00" and ADR_INT < x"FFFA40" and RWn = '1' and DMA_LOCKn = '1' else '1';

	-- Select Sound (8 bit access), write access in SU mode:
    SNDCSn <= 	'0' when ASn = '0' and (ADR_INT = x"FF8800" or ADR_INT = x"FF8802") and RWn = '0' and SU = true and DMA_LOCKn = '1' else
                '0' when ASn = '0' and ADR_INT = x"FF8800" and  RWn = '1' and DMA_LOCKn = '1' else '1';

	-- Write access only in SU mode:
	SYNCMODE_CSn <= '0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF820A" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
					'0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF820A" and RWn = '1' and DMA_LOCKn = '1' else '1';
	
	-- Write access only in SU mode:
	SHIFTMODE_CSn <= '0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF8260" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
					 '0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF8260" and RWn = '1' and DMA_LOCKn = '1' else '1';
					
	-- FCSn write access only in SU mode:
	FCSn <= '0' when RESETn = '0' else
			'0' when ASn = '0' and ADR_INT = x"FF8604" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
			'0' when ASn = '0' and ADR_INT = x"FF8604" and RWn = '1' and DMA_LOCKn = '1' else
			'0' when ASn = '0' and ADR_INT = x"FF8606" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
			'0' when ASn = '0' and ADR_INT = x"FF8606" and RWn = '1' and DMA_LOCKn = '1' else '1';

	-- Write only register in SU mode:
	DMA_MODE_CSn <= '0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF8606" and RWn = '0' and SU = true and DMA_LOCKn = '1' else '1';
	
	-- High density floppy control register:
	HD_REG_CSn <= '0' when ASn = '0' and LDSn = '0' and ADR_INT = x"FF860E" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
				  '0' when ASn = '0' and LDSn = '0' and ADR_INT = x"FF860E" and RWn = '1' and DMA_LOCKn = '1' else '1';

	-- Write access only in SU mode:
	SCCn <= '0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF8C80" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
			'0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF8C82" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
			'0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF8C84" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
			'0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF8C86" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
			'0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF8C80" and RWn = '1' and DMA_LOCKn = '1' else
			'0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF8C82" and RWn = '1' and DMA_LOCKn = '1' else
			'0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF8C84" and RWn = '1' and DMA_LOCKn = '1' else
			'0' when ASn = '0' and UDSn = '0' and ADR_INT = x"FF8C86" and RWn = '1' and DMA_LOCKn = '1' else '1';

	-- Write access only in SU mode:
	CPROGn <= 	'0' when ASn = '0' and ADR_INT = x"FF8E20" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
				'0' when ASn = '0' and ADR_INT = x"FF8E22" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
				'0' when ASn = '0' and ADR_INT = x"FF8E20" and RWn = '1' and DMA_LOCKn = '1' else
				'0' when ASn = '0' and ADR_INT = x"FF8E22" and RWn = '1' and DMA_LOCKn = '1' else '1';

	-- Read access only for the buttons:
	BUTTON_CS <= '1' when ASn = '0' and LDSn = '0' and ADR_INT = x"FF9200" and RWn = '1' and DMA_LOCKn = '1' else '0'; -- Read only, 16 bit.

	-- Write access only in supervisor mode:
	JOY_CS <= '1' when ASn = '0' and ADR_INT = x"FF9202" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
			  '1' when ASn = '0' and ADR_INT = x"FF9202" and RWn = '1' and DMA_LOCKn = '1' else '0';

	PAD0X_CS <= '1' when ASn = '0' and LDSn = '0' and ADR_INT = x"FF9210" and RWn = '1' and DMA_LOCKn = '1' else '0'; -- Read only
	PAD0Y_CS <= '1' when ASn = '0' and LDSn = '0' and ADR_INT = x"FF9212" and RWn = '1' and DMA_LOCKn = '1' else '0'; -- Read only
	PAD1X_CS <= '1' when ASn = '0' and LDSn = '0' and ADR_INT = x"FF9214" and RWn = '1' and DMA_LOCKn = '1' else '0'; -- Read only
	PAD1Y_CS <= '1' when ASn = '0' and LDSn = '0' and ADR_INT = x"FF9216" and RWn = '1' and DMA_LOCKn = '1' else '0'; -- Read only

	XPEN_REG_CS <= '1' when ASn = '0' and LDSn = '0' and ADR_INT = x"FF9220" and RWn = '1' and DMA_LOCKn = '1' else '0'; -- Read only, 16 bit.
	YPEN_REG_CS <= '1' when ASn = '0' and LDSn = '0' and ADR_INT = x"FF9222" and RWn = '1' and DMA_LOCKn = '1' else '0'; -- Read only, 16 bit.

	-- Select RTC, write access in supervisor mode:
	RTCCSn <= '0' when ASn = '0' and ADR_INT >= x"FFFC20" and ADR_INT < x"FFFC40" and RWn = '0' and SU = true and DMA_LOCKn = '1' else
			  '0' when ASn = '0' and ADR_INT >= x"FFFC20" and ADR_INT < x"FFFC40" and RWn = '1' and DMA_LOCKn = '1' else '1';

	-- Peripheral acess control, not valid during DMA transfer:
	DEVn <= '0' when RESETn = '0' else
            '0' when ADR_INT(23 downto 16) = x"FF" and ASn = '0' and SU = true and DMA_LOCKn = '1' else '1';
			
	-- User RAM: The ASn control signal decoding is done in the MMU where RAMn is associated.
	-- SU RAM in write mode (no ASn decoding):
	RAMn <= '0' when RESETn = '0' else
			'0' when DMAn = '0' else -- DMA access.
            '0' when ADR_INT >= x"000800" and ADR_INT < x"E00000" and ASn = '0' and EN_RAM_14MB = '1' else -- 14 MB RAM.
            '0' when ADR_INT >= x"000800" and ADR_INT < x"400000" and ASn = '0' else -- 4 MB RAM.
			'0' when ADR_INT >= x"000008" and ADR_INT < x"000800" and ASn = '0' and SU = true and RWn = '0' else
			'0' when ADR_INT >= x"000008" and ADR_INT < x"000800" and ASn = '0' and RWn = '1' else '1';
end BEHAVIOR;
