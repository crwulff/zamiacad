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
--   Initial Release.
--   Added the Stacy or STBook SHADOW register at address $FF827E.
-- Revision 2K6B  2006/11/06 WF
--   Modified Source to compile with the Xilinx ISE.
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- Revision 2K9A  2008/06/29 WF
--   Changes concerning the SH_MOD for multisync compatibility.
-- 

library ieee;
use ieee.std_logic_1164.all;

package WF25914IP_PKG is
-- Component declarations:
component WF25914IP_SH_CLOCKS
	port (
		CLK_32M: in bit;
		CLK_16M: out bit
	);
end component;

component WF25914IP_SHMOD_REG
	port (
		CLK			: in bit;
		RESETn		: in bit;
		ADR			: in bit_vector (6 downto 1);
		CSn			: in bit;
        RWn			: in bit;
		DATA_IN		: in std_logic_vector(7 downto 0); -- Data.
		DATA_OUT	: out std_logic_vector(7 downto 0);
		DATA_EN		: out bit;
		MULTISYNC	: in bit_vector(1 downto 0);
		SH_MOD		: out bit_vector(7 downto 0);	-- Register output.
		xFF827E		: out bit_vector(7 downto 0)	-- Register output.
	);
end component;

component WF25914IP_CR_SHIFT_REG
	port(
		CLK				: in bit;
		RESETn			: in bit;
		ADR				: in bit_vector (6 downto 1);
		CSn				: in bit;
        RWn				: in bit;
		LOADn, DE		: in bit;
		SH_MOD			: in bit_vector(7 downto 0);
		DATA_IN			: in std_logic_vector(15 downto 0); -- Data.
		DATA_OUT		: out std_logic_vector(7 downto 0);
		DATA_EN			: out bit;
		SR				: out bit_vector(3 downto 0)
	);
end component;

component WF25914IP_CR_REGISTERS
	port(
		CLK, RESETn	: in bit;
		ADR			: in bit_vector (6 downto 1);
		CSn			: in bit;
		RWn			: in bit;
		DATA_IN		: in std_logic_vector(15 downto 0); -- Data.
		DATA_OUT	: out std_logic_vector(15 downto 0);
		DATA_EN		: out bit;
	    SH_MOD		: in bit_vector (7 downto 0);
    	SR			: in bit_vector (3 downto 0);
		MONO_INV	: out bit;
		CHROMA		: out bit_vector(15 downto 0)
	);
end component;

component WF25914IP_CR_OUT
	port (
		CLK			: in bit;
		RESETn		: in bit;
		MONO_INV	: in bit; -- Inversion control bit.
		BLANKn		: in bit;	-- Blanking signal.
		DE			: in bit; -- Blanking signal for the monochrome mode.
		LOADn		: in bit; -- Load control for the shift registers.
		SH_MOD1		: in bit;  -- Monochrome switch.
		SR0			: in bit;	-- Monochrome information.
		CHROMA		: in bit_vector(15 downto 0);	-- Chroma bus.
		CR_1512		: out bit_vector(3 downto 0); -- Hi nibble of the chroma out.
		R			: out bit_vector(3 downto 0);	-- Red video output.
		G			: out bit_vector(3 downto 0);	-- Green video output.
		B			: out bit_vector(3 downto 0);	-- Blue video output.
		MONO		: out bit				-- Monochrome video output.
	);
end component;

component WF25914IP_MICROWIRE
	port(
		RESETn		: in bit;
		CLK			: in bit;
		RWn			: in bit;
		CMPCSn		: in bit;
		ADR			: in bit_vector (6 downto 1);
		DATA_IN		: in std_logic_vector(15 downto 0); -- Data.
		DATA_OUT	: out std_logic_vector(15 downto 0);
		DATA_EN		: out bit;
		MWK			: out bit;		-- Microwire clock (1MHz).
		MWD			: out bit;		-- Microwire data.
		MWEn		: out bit	-- Microwire enable (low active).
	);
end component;

component WF25914IP_DMASOUND
	port (
		RESETn		: in bit;
		CLK			: in bit;
		ADR			: in bit_vector (6 downto 1);
		CSn			: in bit;
		RWn			: in bit;
		DATA_IN		: in std_logic_vector(15 downto 0); -- Data.
		DATA_OUT	: out std_logic_vector(15 downto 0);
		DATA_EN		: out bit;
		DE			: in bit;
		SLOADn		: in bit;
		SREQ		: out bit;
		SCLK		: in bit;
		FCLK		: out bit;
		SDATA_L		: out bit_vector(7 downto 0); -- Buffers implemented here.
		SDATA_R		: out bit_vector(7 downto 0) -- Buffers implemented here.
		);
end component;		

end WF25914IP_PKG;
