----------------------------------------------------------------------
----                                                              ----
---- ATARI SHADOW compatible IP Core    			              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- This is the SUSKA SHADOW IP core top level file.             ----
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
---- Copyright (C) 2006 Wolfgang Foerster                         ----
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
-- Revision 2K8B  2008/12/24 WF
--   Rewritten this top level file as a wrapper for the top_soc file.
-- 


library work;
use work.WF_SHD101775IP_PKG.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF_SHD101775IP_TOP is
	port (
		RESETn	: in bit;
		CLK		: in bit; -- 16MHz, same as MCU clock.

		-- Video control:
		M_DATA	: in bit_vector(15 downto 0); -- Data of the shared system RAM.
		DE		: in bit; -- Video Data enable.
		LOADn	: in bit; -- Video data load control.
		
		-- VIDEO RAM:
		-- The core is written for use of a KM681000 SRAM.
		-- If smaller ones are used, do not connect A16,
		-- A15 and if not necessary CS and CSn.
		R_ADR	: out bit_vector(16 downto 0);
		R_DATA	: inout std_logic_vector(7 downto 0);
		R_CSn	: out bit;
		R_CS	: out bit;
		R_OEn	: out bit;
		R_WRn	: out bit;
		
		-- LCD control:
		UDATA	: out bit_vector(3 downto 0);
		LDATA	: out bit_vector(3 downto 0);
		LFS		: out bit; -- Line frame strobe.
		VDCLK	: out bit; -- Video data clock.
		LLCLK	: out bit -- Line latch clock.
	);
end WF_SHD101775IP_TOP;

architecture STRUCTURE of WF_SHD101775IP_TOP is
component WF_SHD101775IP_TOP_SOC
	port (
		RESETn		: in bit;
		CLK			: in bit;
		M_DATA		: in bit_vector(15 downto 0);
		DE			: in bit;
		LOADn		: in bit;
		R_ADR		: out bit_vector(14 downto 0);
		R_DATA_IN	: in std_logic_vector(7 downto 0);
		R_DATA_OUT	: out std_logic_vector(7 downto 0);
        R_DATA_EN   : out bit;
		R_WRn		: out bit;
		UDATA		: out bit_vector(3 downto 0);
		LDATA		: out bit_vector(3 downto 0);
		LFS			: out bit;
		VDCLK		: out bit;
		LLCLK		: out bit
	);
end component;
--
signal R_DATA_OUT   : std_logic_vector(7 downto 0);
signal R_DATA_EN    : bit;
begin
	R_CSn <= '0';
	R_CS <= '1';
	R_OEn <= '0';
    R_DATA <= R_DATA_OUT when R_DATA_EN = '1' else (others => 'Z');
    R_ADR(16 downto 15) <= "00";

    I_SHADOW: WF_SHD101775IP_TOP_SOC
        port map(RESETn          => RESETn,
                 CLK             => CLK,
                 M_DATA          => M_DATA,
                 DE              => DE,
                 LOADn           => LOADn,
                 R_ADR           => R_ADR(14 downto 0),
                 R_DATA_IN       => R_DATA,
                 R_DATA_OUT      => R_DATA_OUT,
                 R_DATA_EN       => R_DATA_EN,
                 R_WRn           => R_WRn,
                 UDATA           => UDATA,
                 LDATA           => LDATA,
                 LFS             => LFS,
                 VDCLK           => VDCLK,
                 LLCLK           => LLCLK
        );
end architecture STRUCTURE;
