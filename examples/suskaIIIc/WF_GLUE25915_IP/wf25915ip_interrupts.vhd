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
---- Interrupt control system.                                    ----
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
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- Revision 2K9A  2009/06/20 WF
--   Enhancements for the multisync compatible video modi.
--   Process IPL1n_CONTROL has now synchronous reset to meet preset requirements.
-- 

library ieee;
use ieee.std_logic_1164.all;

entity WF25915IP_INTERRUPTS is
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
		GI2n			: out bit; -- ST GLUE.
		GI1n			: out bit; -- ST GLUE.
		IPLn			: out bit_vector(2 downto 0) -- STE GLUE.
	);
end entity WF25915IP_INTERRUPTS;
	
architecture BEHAVIOR of WF25915IP_INTERRUPTS is
signal GI_In		: bit_vector(2 downto 1);
signal HSYNC_INTn	: bit;
signal VSYNC_INTn	: bit;
begin
	IPL1n_CONTROL: process
	-- Process for storing interrupt information of the vertical- and 
	-- horizontal blanking interrupt. This process is enhanced for the
	-- video hi modes in a way, that only every second horizontal
	-- synchronisation pulse causes an interrupt. This is for software
	-- timing compatibility reasons.
	variable EDGE_LOCK_H, EDGE_LOCK_V : boolean;
	variable SECOND	: boolean;
	begin
		wait until CLK = '1' and CLK' event;
		if RESETn = '0' then
			HSYNC_INTn <= '1';  -- Initial conditions.
			VSYNC_INTn <= '1';
			SECOND := false;
		elsif HSYNCn = '0' and EDGE_LOCK_H = false and VIDEO_HIMODE = '0' then
			HSYNC_INTn <= '0';  -- Interrupt request.
			EDGE_LOCK_H := true;
		elsif HSYNCn = '0' and EDGE_LOCK_H = false and VIDEO_HIMODE = '1' and SECOND = true then
			SECOND := false;
			HSYNC_INTn <= '0';  -- Interrupt request.
			EDGE_LOCK_H := true;
		elsif HSYNCn = '0' and EDGE_LOCK_H = false and VIDEO_HIMODE = '1' then
			SECOND := true;
			EDGE_LOCK_H := true;
		elsif ASn = '0' and FC = "111" and ADR_HI = "1111" and 
			ADR_LO = "010" then
			HSYNC_INTn <= '1';  -- Interrupt request reset.
		elsif HSYNCn = '1' then
			EDGE_LOCK_H := false;
		end if;
		--
		if VSYNCn = '0' and EDGE_LOCK_V = false then
			VSYNC_INTn <= '0';  -- Interrupt request.
			EDGE_LOCK_V := true;
		elsif ASn = '0' and FC = "111" and ADR_HI = "1111" and 
			ADR_LO = "100" then
			VSYNC_INTn <= '1';  -- Interrupt request reset.
		elsif VSYNCn = '1' then
			EDGE_LOCK_V := false;
		end if;
	end process IPL1n_CONTROL;
	
	GI_In <= "00" when MFPINTn = '0' else
			 "01" when VSYNC_INTn = '0' else
			 "10" when HSYNC_INTn = '0' else
			 "11"; -- No interrupts.

	GI2n <= GI_In(2);
	GI1n <= GI_In(1);

	-- The following two statements generate the AVECn signal for the auto vectoring 
	-- interrupts. In the ST these are H-Blank and V-Blank. The MFP is not auto
	-- vectoring and do not need AVECn.
	-- Other IRQ-levels: 7: external, 6: MFP, 5: external, 3: external, 1: none, 0: none
	-- 7 is the highest IRQ-level.
	-- Do not assert interrupt acknowledge in DMA mode. This is forbidden by DMA_LOCKn = '1'.
	AVECn <= '0' when -- V-Blank IRQ-level 2.
				DMA_LOCKn = '1' and ASn = '0' and FC = "111" and ADR_HI = "1111" and ADR_LO = "010" else
			 '0' when -- H-Blank IRQ-level 4.
				DMA_LOCKn = '1' and ASn = '0' and FC = "111" and ADR_HI = "1111" and ADR_LO = "100" else '1';
	IACKn <= '0' when DMA_LOCKn = '1' and FC = "111" and ADR_HI = "1111" and ADR_LO = "110" else '1';

	PRIODECODER: process(EINT3n, EINT5n, EINT7n, GI_In)
	begin
		if EINT7n = '0' then -- Highest priority.
			IPLn <= "000";
		elsif GI_In = "00" then -- MFPINT.
			IPLn <= "001";
		elsif EINT5n = '0' then
			IPLn <= "010";
		elsif GI_In = "01" then -- V-Blank.
			IPLn <= "011";
		elsif EINT3n = '0' then
			IPLn <= "100";
		elsif GI_In = "10" then -- H-Blank.
			IPLn <= "101";
		else
			IPLn <= "111";
		end if;		
	end process PRIODECODER;
end BEHAVIOR;
