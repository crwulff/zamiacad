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
---- MCU register address decoder.                                ----
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
-- Initial Release.
-- Added Stacy and STBook SHADOW chip register at address $FF827E

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25912IP_ADRDEC is
port (  ADR		: in bit_vector(15 downto 1); -- Address inputs.
		ASn		: in bit; -- Address select, low active.
		LDSn	: in bit; -- Lower data strobe, low active.
		DEVn	: in bit; -- Device select, low active (A23 downto A16 = x"FF").
		
		MEM_CONFIG_CS	: out bit; -- Select for the memory configuration register.
		
		VIDEO_BASE_HI_CS	: out bit; -- Select for the video base register.
		VIDEO_BASE_MID_CS	: out bit; -- Select for the video base register.
		VIDEO_BASE_LOW_CS	: out bit; -- Select for the video base register.
		
		VIDEO_COUNT_HI_CS	: out bit; -- Select for the video address counter.
		VIDEO_COUNT_MID_CS	: out bit; -- Select for the video address counter.
		VIDEO_COUNT_LOW_CS	: out bit; -- Select for the video address counter.
		
		DMA_BASE_HI_CS	: out bit; -- Select for the DMA base counter.
		DMA_BASE_MID_CS	: out bit; -- Select for the DMA base counter.
		DMA_BASE_LOW_CS	: out bit; -- Select for the DMA base counter.
		
		CMPCS_REQ		: out bit; -- Color monitor processor video register request.

		-- DMA-sound enhancements of the STE:
		SOUND_CTRL_CS				: out bit;	-- DMA sound control.
		SOUND_FRAME_START_HI_CS		: out bit;	-- DMA frame start high byte.
		SOUND_FRAME_START_MID_CS	: out bit;	-- DMA frame start mid byte.
		SOUND_FRAME_START_LOW_CS	: out bit;	-- DMA frame start low byte.
		SOUND_FRAME_ADR_HI_CS		: out bit;	-- DMA frame address counter high byte.
		SOUND_FRAME_ADR_MID_CS		: out bit;	-- DMA frame address counter mid byte.
		SOUND_FRAME_ADR_LOW_CS		: out bit;	-- DMA frame address counter low byte.
		SOUND_FRAME_END_HI_CS		: out bit;	-- DMA frame end high byte.
		SOUND_FRAME_END_MID_CS		: out bit;	-- DMA frame end mid byte.
		SOUND_FRAME_END_LOW_CS		: out bit;	-- DMA frame end low byte.
		
		LINEWIDTH_CS	: out bit -- Select for the STEs linewidth register.
      );
end WF25912IP_ADRDEC;

-- MCU register addresses (access in superuser mode):
-- CMPCS			: x"FF8240" to x"FF825E" -- SHIFTER's chroma registers.
-- CMPCS			: x"FF8260" -- SHIFTER's shift mode register.
-- CMPCS			: x"FF8265" -- SHIFTER's HSCROLL, STE only.

-- VIDEO_BASE_HI	: x"FF8201" -- read-write.
-- VIDEO_BASE_MID	: x"FF8203" -- read-write.
-- VIDEO_BASE_LOW	: x"FF820C" -- read-write, STE only.

-- VIDEO_COUNT_HI	: x"FF8205" -- ST: read, STE (implemented): read-write.
-- VIDEO_COUNT_MID	: x"FF8207" -- ST: read, STE (implemented): read-write.
-- VIDEO_COUNT_LOW	: x"FF8209" -- ST: read, STE (implemented): read-write.

-- LINEWIDTH		: x"FF820F" -- read-write, STE only.

-- DMA_BASE_HI		: x"FF8609" -- read-write.
-- DMA_BASE_MID		: x"FF860B" -- read-write.
-- DMA_BASE_LOW		: x"FF860D" -- read-write.

-- MEM_CONFIG		: x"FF8001" -- read-write.

-- SOUND_CTRL				: x"8900", DMA sound register, read/write.
-- SOUND_FRAME_START_HI		: x"8902", DMA sound register, read/write.
-- SOUND_FRAME_START_MID	: x"8904", DMA sound register, read/write.
-- SOUND_FRAME_START_LOW	: x"8906", DMA sound register, read/write.
-- SOUND_FRAME_ADR_HI		: x"8908", DMA sound register, read only.
-- SOUND_FRAME_ADR_MID		: x"890A", DMA sound register, read only.
-- SOUND_FRAME_ADR_LOW		: x"890C", DMA sound register, read only.
-- SOUND_FRAME_END_HI		: x"890E", DMA sound register, read/write.
-- SOUND_FRAME_END_MID		: x"8910", DMA sound register, read/write.
-- SOUND_FRAME_END_LOW		: x"8912", DMA sound register, read/write.
-- SOUND_MODE_CTRL			: x"8920", DMA sound register, read/write.
-- MW_DATA					: x"8922", Microwire data register, read/write.
-- MW_MASK					: x"8924", Microwire mask register, read/write.

architecture BEHAVIOR of WF25912IP_ADRDEC is
signal ADR_I: bit_vector(15 downto 0); -- 16 bit adress bus
begin
	ADR_I <= ADR & '0';
	
 	MEM_CONFIG_CS <= '1' when DEVn = '0' and ADR_I = x"8000" 		-- Via LDSn x"FF8001".
						  and ASn = '0' and LDSn = '0' else '0';
	VIDEO_BASE_HI_CS <= '1' when DEVn = '0' and ADR_I = x"8200" 	-- Via LDSn x"FF8201".
						  and ASn = '0' and LDSn = '0' else '0';
	VIDEO_BASE_MID_CS <= '1' when DEVn = '0' and ADR_I = x"8202"  	-- Via LDSn x"FF8203".
						  and ASn = '0' and LDSn = '0' else '0';
	VIDEO_BASE_LOW_CS <= '1' when DEVn = '0' and ADR_I = x"820C" 	-- Via LDSn x"FF820D".
						  and ASn = '0' and LDSn = '0' else '0';
	VIDEO_COUNT_HI_CS <= '1' when DEVn = '0' and ADR_I = x"8204"  	-- Via LDSn x"FF8205".
						  and ASn = '0' and LDSn = '0' else '0';
	VIDEO_COUNT_MID_CS <= '1' when DEVn = '0' and ADR_I = x"8206"  	-- Via LDSn x"FF8207".
						  and ASn = '0' and LDSn = '0' else '0';
	VIDEO_COUNT_LOW_CS <= '1' when DEVn = '0' and ADR_I = x"8208"  	-- Via LDSn x"FF8209".
						  and ASn = '0' and LDSn = '0' else '0';
	LINEWIDTH_CS <= '1' when DEVn = '0' and ADR_I = x"820E"  		-- Via LDSn x"FF820F".
						  and ASn = '0' and LDSn = '0' else '0';

	-- Video and DMA sound register access in the SHIFTER.
	-- Pay attention!
	-- UDSn decoding for the SHIFTMODE register does not work. 
	-- The reason is probably the interference with the mirror shift 
	-- mode register in the GLUE, where also a DTACKn is generated,
	-- if shiftmode register access appears.
	CMPCS_REQ <= 	'1' when DEVn = '0' and ASn = '0' and ADR_I(15 downto 4) = x"824" else -- Range x"FF8240" to x"FF824E".
					'1' when DEVn = '0' and ASn = '0' and ADR_I(15 downto 4) = x"825" else	-- Range x"FF8250" to x"FF825E".
					'1' when DEVn = '0' and ASn = '0' and ADR_I = x"8260" else
					'1' when DEVn = '0' and ASn = '0' and ADR_I = x"8264" else
					'1' when DEVn = '0' and ASn = '0' and ADR_I = x"827E" else -- STACY SHADOW chip register.
                    '1' when DEVn = '0' and ASn = '0' and LDSn = '0' and ADR_I = x"8900" else -- x"8901"
                    '1' when DEVn = '0' and ASn = '0' and LDSn = '0' and ADR_I = x"8920" else -- x"8921"
					'1' when DEVn = '0' and ASn = '0' and ADR_I = x"8922" else
					'1' when DEVn = '0' and ASn = '0' and ADR_I = x"8924"
					 	else '0';

	DMA_BASE_HI_CS <= '1' when DEVn = '0' and ADR_I = x"8608"  		-- Via LDSn x"FF8609".
							and ASn = '0' and LDSn = '0' else '0';
	DMA_BASE_MID_CS <= '1' when DEVn = '0' and ADR_I = x"860A"  	-- Via LDSn x"FF860B".
							and ASn = '0' and LDSn = '0' else '0';
	DMA_BASE_LOW_CS <= '1' when DEVn = '0' and ADR_I = x"860C"  	-- Via LDSn x"FF860D".
							and ASn = '0' and LDSn = '0' else '0';

	-- All DMA sound registers are 8 bit wide and selected via LDSn on the
	-- low data word.
	SOUND_CTRL_CS 				<= '1' when DEVn = '0' and ADR_I = x"8900" -- x"FF8901".
									and ASn = '0' and LDSn = '0' else '0';
	SOUND_FRAME_START_HI_CS 	<= '1' when DEVn = '0' and ADR_I = x"8902" -- x"FF8903".
									and ASn = '0' and LDSn = '0' else '0';
	SOUND_FRAME_START_MID_CS 	<= '1' when DEVn = '0' and ADR_I = x"8904" -- x"FF8905".
									and ASn = '0' and LDSn = '0' else '0';
	SOUND_FRAME_START_LOW_CS 	<= '1' when DEVn = '0' and ADR_I = x"8906" -- x"FF8907".
									and ASn = '0' and LDSn = '0' else '0';
	SOUND_FRAME_ADR_HI_CS 		<= '1' when DEVn = '0' and ADR_I = x"8908" -- x"FF8909".
									and ASn = '0' and LDSn = '0' else '0';
	SOUND_FRAME_ADR_MID_CS 		<= '1' when DEVn = '0' and ADR_I = x"890A" -- x"FF890B".
									and ASn = '0' and LDSn = '0' else '0';
	SOUND_FRAME_ADR_LOW_CS 		<= '1' when DEVn = '0' and ADR_I = x"890C" -- x"FF890D".
									and ASn = '0' and LDSn = '0' else '0';
	SOUND_FRAME_END_HI_CS 		<= '1' when DEVn = '0' and ADR_I = x"890E" -- x"FF890E".
									and ASn = '0' and LDSn = '0' else '0';
	SOUND_FRAME_END_MID_CS 		<= '1' when DEVn = '0' and ADR_I = x"8910" -- x"FF8911".
									and ASn = '0' and LDSn = '0' else '0';
	SOUND_FRAME_END_LOW_CS 		<= '1' when DEVn = '0' and ADR_I = x"8912" -- x"FF8913".
									and ASn = '0' and LDSn = '0' else '0';

end architecture BEHAVIOR;
