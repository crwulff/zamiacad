----------------------------------------------------------------------
----                                                              ----
---- ATARI SHADOW compatible IP Core    			              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- This is the SUSKA SHADOW IP core top level file.             ----
---- Top level file for use in systems on programmable chips.     ----
----                                                              ----
---- Description:                                                 ----
---- This is a Stacy or STBook compatible LCD video controller.   ----
---- The controller is modeled to drive a standard VGA LCD module ----
---- with a resolution of 640x480 dots. The original LCD with a   ----
---- resolution of 640x400 dots is withdrawn from the market and  ----
---- therefore not intended for use with this core.               ----
----                                                              ----
---- For a list of the prooven LCD modules see the header of the  ----
---- "wf101775ip_ctrl.vhd" file.                                  ----
----                                                              ----
---- In the original chip there was a register at x"FF827E"       ----
---- It controls the Stacy or STBook relevant power management    ----
---- functions. This 8 bit register is not implemented here but   ----
---- can be found in the SHIFTER IP Core.                         ----
----                                                              ----
---- To Do:                                                       ----
---- -                                                            ----
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
-- Revision 2K6A  2006/06/03 WF
  -- Initial Release.
	-- Tested static RAMs: KM681000.
   	-- LCD Sharp LM641542 is working.
    -- LCD Sharp LM64P803 is working.
-- Revision 2K6B  2006/11/06 WF
--   Modified Source to compile with the Xilinx ISE.
--   Top level file provided for SOC (systems on programmable chips).
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- Revision 2K9A  2009/06/20 WF
--   Process STARTUP has now synchronous reset to provide preset.
--   Process LOAD_CTRL has now synchronous reset to provide preset.
-- 


library work;
use work.WF_SHD101775IP_PKG.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF_SHD101775IP_TOP_SOC is
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
end WF_SHD101775IP_TOP_SOC;

architecture STRUCTURE of WF_SHD101775IP_TOP_SOC is
signal DATA_IN			: bit_vector(7 downto 0);
signal STARTUP_BLANK	: boolean;
signal LOAD_STRB_I		: bit;
signal VIDEO_INBUFF		: bit_vector(15 downto 0);
signal FIFO_UDS_D_OUT	: bit_vector(7 downto 0);
signal FIFO_LDS_D_OUT	: bit_vector(7 downto 0);
signal R_DATA_EN_I		: bit;
signal UDS_FIFO_EMTPY_I	: bit;
signal UDS_FIFO_FULL_I	: bit;
signal LDS_FIFO_EMTPY_I	: bit;
signal LDS_FIFO_FULL_I	: bit;
signal R_D_SEL_I		: bit;
signal L_FIFO_WR_I		: bit;
signal U_FIFO_WR_I		: bit;
signal U_FIFO_RD_I		: bit;
signal L_FIFO_RD_I		: bit;
signal LCD_DATASEL_I	: bit;
signal LCD_UD_EN_I		: bit;
signal LCD_LD_EN_I		: bit;
begin
	LOAD_CTRL: process
	-- This process delivers a load strobe pulse from
	-- LOADn input signal.
	variable LOCK : boolean;
	begin
		wait until CLK = '1' and CLK' event;
		if RESETn = '0' then
			LOAD_STRB_I <= '0';
			LOCK := true; -- LOADn starts with '0'.
		elsif LOADn = '0' and LOCK = false then
			LOAD_STRB_I <= '1';
			LOCK := true;
		elsif LOADn = '1' then
			LOCK := false;
			LOAD_STRB_I <= '0';
		else
			LOAD_STRB_I <= '0';
		end if;
	end process LOAD_CTRL;

	VIDEO_IN_BUFFER: process(RESETn, CLK)
	-- This process stores the last video data loaded
	-- by the LOADn control signal.
	begin
		if RESETn = '0' then
			VIDEO_INBUFF <= (others => '0');
		elsif CLK = '1' and CLK' event then
			if LOAD_STRB_I = '1' then
				VIDEO_INBUFF <= M_DATA;
			end if;
		end if;
	end process VIDEO_IN_BUFFER;

	DATA_IN <= To_BitVector(R_DATA_IN);
    R_DATA_EN <= R_DATA_EN_I;
	R_DATA_OUT <= 	To_StdLogicVector(VIDEO_INBUFF(15 downto 8)) when R_D_SEL_I = '1' and R_DATA_EN_I = '1' else
					To_StdLogicVector(VIDEO_INBUFF(7 downto 0)) when R_D_SEL_I = '0' and R_DATA_EN_I = '1' else (others => '0');

	STARTUP: process
	-- This routine blanks out the screen for about one second during startup
	-- or after a system reset.
	variable STARTUP_COUNT: std_logic_vector(24 downto 0);
	begin
		wait until CLK = '1' and CLK' event;
		if RESETn = '0' then
			STARTUP_COUNT := (others => '0');
			STARTUP_BLANK <= true;
		elsif STARTUP_COUNT < '1' & x"E84800" then
			STARTUP_COUNT := STARTUP_COUNT + '1';
		else
			STARTUP_BLANK <= false;
		end if;
	end process STARTUP;

	-- Black out the unused lines with ... else x"F" or blank out with ... else x"0";
	UDATA <= x"0" when STARTUP_BLANK = true else
			 FIFO_UDS_D_OUT(7 downto 4) when LCD_DATASEL_I = '1' and LCD_UD_EN_I = '1' else 
			 FIFO_UDS_D_OUT(3 downto 0) when LCD_DATASEL_I = '0' and LCD_UD_EN_I = '1' else x"F";
	LDATA <= x"0" when STARTUP_BLANK = true else
			 FIFO_LDS_D_OUT(7 downto 4) when LCD_DATASEL_I = '1' and LCD_LD_EN_I = '1' else 
			 FIFO_LDS_D_OUT(3 downto 0) when LCD_DATASEL_I = '0' and LCD_LD_EN_I = '1' else x"F";

	I_CTRL: WF_SHD101775IP_CTRL
		port map(
			RESETn			=> RESETn,
			CLK				=> CLK,
			DE				=> DE,
			LOAD_STRB		=> LOAD_STRB_I,
			R_ADR			=> R_ADR(14 downto 0),
			R_DATA_EN		=> R_DATA_EN_I,
			-- R_OEn			=> , -- Not used.
			R_WRn			=> R_WRn,
			R_D_SEL			=> R_D_SEL_I,
			UDS_FIFO_EMPTY	=> UDS_FIFO_EMTPY_I,
			UDS_FIFO_FULL	=> UDS_FIFO_FULL_I,
			LDS_FIFO_EMPTY	=> LDS_FIFO_EMTPY_I,
			LDS_FIFO_FULL	=> LDS_FIFO_FULL_I,
			U_FIFO_WR		=> U_FIFO_WR_I,
			L_FIFO_WR		=> L_FIFO_WR_I,
			U_FIFO_RD		=> U_FIFO_RD_I,
			L_FIFO_RD		=> L_FIFO_RD_I,
			LCD_DATASEL		=> LCD_DATASEL_I,
			LCD_UD_EN		=> LCD_UD_EN_I,
			LCD_LD_EN		=> LCD_LD_EN_I,
			LCD_S			=> LFS,
			LCD_CP2			=> VDCLK,
			LCD_CP1			=> LLCLK
		);

	I_FIFO_U: WF_SHD101775IP_FIFO
		port map(
			CLK			=> CLK,
			CLRn		=> RESETn,
			WR_ENA		=> U_FIFO_WR_I,
			DATA_IN		=> DATA_IN,
			DATA_OUT	=> FIFO_UDS_D_OUT,
			RD_ENA		=> U_FIFO_RD_I,
			FIFO_FULL	=> UDS_FIFO_FULL_I,
			FIFO_EMPTY	=> UDS_FIFO_EMTPY_I
			--ERR			=> -- Not used.
		);

	I_FIFO_L: WF_SHD101775IP_FIFO
		port map(
			CLK			=> CLK,
			CLRn		=> RESETn,
			WR_ENA		=> L_FIFO_WR_I,
			DATA_IN		=> DATA_IN,
			DATA_OUT	=> FIFO_LDS_D_OUT,
			RD_ENA		=> L_FIFO_RD_I,
			FIFO_FULL	=> LDS_FIFO_FULL_I,
			FIFO_EMPTY	=> LDS_FIFO_EMTPY_I
			--ERR			=> -- Not used.
		);
end architecture STRUCTURE;
