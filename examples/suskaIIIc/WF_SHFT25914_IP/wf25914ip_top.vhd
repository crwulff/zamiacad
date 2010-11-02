----------------------------------------------------------------------
----                                                              ----
---- ATARI SHIFTER compatible IP Core				              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- ST and STE compatible SHIFTER IP core.                       ----
----                                                              ----
---- This is the top level file.                                  ----
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
--   Initial Release.
--   Added the Stacy or STBook SHADOW register at address $FF827E.
-- Revision 2K6B  2006/11/06 WF
--   Modified Source to compile with the Xilinx ISE.
-- Revision 2K8B  2008/12/24 WF
--   Rewritten this top level file as a wrapper for the top_soc file.
-- 

library work;
use work.wf25914ip_pkg.all;
library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25914IP_TOP is
	port (
		SH_CLK_32M		: in bit;
		SH_CLK_16M		: out bit;
        RESETn          : in bit;
		SH_A			: in bit_vector(6 downto 1);		    -- Adress bus (without base adress).
		SH_D			: inout std_logic_vector(15 downto 0);	-- Data bus.
		SH_RWn			: in bit;			                    -- Write to registers is low active.
		SH_CSn			: in bit;			                    -- Base adress of the shifter is 0xFF82xx.

		SH_LOADn		: in bit;			                    -- Load signal for the shift registers.
		SH_DE			: in bit;			                    -- Shift switch for the shift registers.
		SH_BLANKn		: in bit;			                    -- Blanking input.
		SH_R			: out bit_vector(3 downto 0);	        -- Red video output.
		SH_G			: out bit_vector(3 downto 0);	        -- Green video output.
		SH_B			: out bit_vector(3 downto 0);	        -- Blue video output.
		SH_MONO			: out bit;			                    -- Monochrome video output.
        SH_COLOR		: out bit;			                    -- COMP_SYNC signal of the ST.
		
        SH_SCLK			: in bit; 	                            -- Sample clock, 6.4 MHz.
        SH_FCLK			: out bit;	                            -- Frame clock.
        SH_SLOADn		: in bit;	                            -- DMA load control.
        SH_SREQ			: out bit;	                            -- DMA load request.
        SH_SDATA_L		: out bit_vector(7 downto 0);           -- Left audio data.
        SH_SDATA_R		: out bit_vector(7 downto 0);           -- Right audio data.

		SH_MWK			: out bit;	                            -- Microwire interface, clock.
		SH_MWD			: out bit;	                            -- Microwire interface, data.
		SH_MWEn			: out bit;	                            -- Microwire interface, enable.
		
		-- Port connections of xFF872E_D:
		-- Bit 7 = MTR_POWER_ON (Turns on IDE rive motor).
		-- Bit 6 not further specified.
		-- Bit 5 = RS232_OFF.
		-- Bit 4 = REFRESH_MACHINE.
		-- Bit 3 = LAMP (LCD backlight).
		-- Bit 2 = POWER_OFF.
		-- Bit 1 = SHFT output.
		-- Bit 0 = SHADOW chip off.
		xFF827E_D		: out bit_vector(7 downto 0)
	);
end WF25914IP_TOP;

architecture STRUCTURE of WF25914IP_TOP is
component WF25914IP_SH_CLOCKS
	port (
		CLK_32M     : in bit;	
		CLK_16M     : out bit
	);
end component;  

component WF25914IP_TOP_SOC
	generic(
		-- This value is rather critical for signal conditioning. Play around
		-- with small values around 6, if there is a blank video screen or a
		-- noisy picture.
		CTRL_DELAYS     : natural -- := 6 -- 1 delay is 31.25ns.
	);

	port (
		CLK				: in bit;
		RESETn          : in bit;
		SH_A			: in bit_vector(6 downto 1);
		SH_D_IN			: in std_logic_vector(15 downto 0);
		SH_D_OUT		: out std_logic_vector(15 downto 0);
		SH_DATA_HI_EN	: out bit;
		SH_DATA_LO_EN	: out bit;
		SH_RWn			: in bit;
		SH_CSn			: in bit;
		SH_LOADn		: in bit;
		MULTISYNC		: in bit;
		SH_DE			: in bit;
		SH_BLANKn		: in bit;
		CR_1512			: out bit_vector(3 downto 0);
		SH_R			: out bit_vector(3 downto 0);
		SH_G			: out bit_vector(3 downto 0);
		SH_B			: out bit_vector(3 downto 0);
		SH_MONO			: out bit;
		SH_COLOR		: out bit;
		SH_SCLK			: in bit;
		SH_FCLK			: out bit;
		SH_SLOADn		: in bit;
		SH_SREQ			: out bit;
		SH_SDATA_L		: out bit_vector(7 downto 0);
		SH_SDATA_R		: out bit_vector(7 downto 0);
		SH_MWK			: out bit;
		SH_MWD			: out bit;
		SH_MWEn			: out bit;
		xFF827E_D		: out bit_vector(7 downto 0)
	);
end component;
signal SH_D_OUT         : std_logic_vector(15 downto 0);
signal SH_DATA_HI_EN    : bit;
signal SH_DATA_LO_EN    : bit;
begin
    SH_D(15 downto 8) <= SH_D_OUT(15 downto 8) when SH_DATA_HI_EN = '1' else (others => 'Z');
    SH_D(7 downto 0) <= SH_D_OUT(7 downto 0) when SH_DATA_LO_EN = '1' else (others => 'Z');

    I_SHCLOCKS: WF25914IP_SH_CLOCKS
        port map(CLK_32M        => SH_CLK_32M,
                 CLK_16M        => SH_CLK_16M
        );

    I_SHIFTER: WF25914IP_TOP_SOC
        generic map(CTRL_DELAYS     => 6
        )

        port map(CLK                => SH_CLK_32M,
                 RESETn             => RESETn,
                 SH_A               => SH_A,
                 SH_D_IN            => SH_D,
                 SH_D_OUT           => SH_D_OUT,
                 SH_DATA_HI_EN      => SH_DATA_HI_EN,
                 SH_DATA_LO_EN      => SH_DATA_LO_EN,
                 SH_RWn             => SH_RWn,
                 SH_CSn             => SH_CSn,
                 MULTISYNC			=> '0', -- Not used.
                 SH_LOADn           => SH_LOADn,
                 SH_DE              => SH_DE,
                 SH_BLANKn          => SH_BLANKn,
                 -- CR_1512         =>, -- Not used.
                 SH_R               => SH_R,
                 SH_G               => SH_G,
                 SH_B               => SH_B,
                 SH_MONO            => SH_MONO,
                 SH_COLOR           => SH_COLOR,
                 SH_SCLK            => SH_SCLK,
                 SH_FCLK            => SH_FCLK,
                 SH_SLOADn          => SH_SLOADn,
                 SH_SREQ            => SH_SREQ,
                 SH_SDATA_L         => SH_SDATA_L,
                 SH_SDATA_R         => SH_SDATA_R,
                 SH_MWK             => SH_MWK,
                 SH_MWD             => SH_MWD,
                 SH_MWEn            => SH_MWEn,
                 xFF827E_D          => xFF827E_D
        );
end STRUCTURE;
