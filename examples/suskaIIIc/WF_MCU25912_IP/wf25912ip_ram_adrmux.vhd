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
---- This file contains moddeling for tha RAM address             ----
---- multiplexer.                                                 ----
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
-- 

use work.wf25912ip_pkg.all;

entity WF25912IP_RAM_ADRMUX is
port (  ADR			: in bit_vector(23 downto 1); -- ST address bus.
		VIDEO_ADR	: in bit_vector(23 downto 1); -- Video address bus.
		SOUND_ADR	: in bit_vector(23 downto 1); -- Video address bus.
		DMA_ADR		: in bit_vector(23 downto 1); -- DMA address bus.
		REF_ADR		: in bit_vector(9 downto 0); -- Refresh adress bus.

		M_ADR		: buffer bit_vector(23 downto 1); -- Non multiplexed DRAM adresses.
		RAM_ADR		: out bit_vector(9 downto 0); -- Multiplexed address bus for the DRAMs.

		MADRSEL		: in MADR_TYPE; -- Control signal for high or low MADR signals.
		BANK0_TYPE 	: in BANKTYPE; -- Control signal, which RAM is installed in BANK0.
		MCU_PHASE	: in MCU_PHASE_TYPE; -- See control.vhd.
		DMAn		: in bit -- Control to distinguish between DMA or CPU RAM access.
      );
end WF25912IP_RAM_ADRMUX;

architecture BEHAVIOR of WF25912IP_RAM_ADRMUX is
begin
	M_ADR <= 
		ADR when MCU_PHASE = RAM and DMAn = '1' else
		DMA_ADR when MCU_PHASE = RAM and DMAn = '0' else
		VIDEO_ADR when MCU_PHASE = VIDEO else
		SOUND_ADR when MCU_PHASE = SOUND else
		"0000000000000" & REF_ADR; -- Refresh cycles.

	RAM_ADR <= 	
		M_ADR(20 downto 11) when MADRSEL = MEM_HI_ADR and BANK0_TYPE = K2048 else
		M_ADR(10 downto 1) when MADRSEL = MEM_LOW_ADR and BANK0_TYPE = K2048 else
		M_ADR(19 downto 10) when MADRSEL = MEM_HI_ADR and BANK0_TYPE = K512 else
		'0' & M_ADR(9 downto 1) when MADRSEL = MEM_LOW_ADR and BANK0_TYPE = K512 else
		M_ADR(18 downto 9) when MADRSEL = MEM_HI_ADR and BANK0_TYPE = K128 else
		"00" & M_ADR(8 downto 1) when MADRSEL = MEM_LOW_ADR and BANK0_TYPE = K128 else
		(others => '1');
end architecture BEHAVIOR;
