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
---- Moddeling of the chroma shift mode register.                 ----
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
--   Changed reset condition for the SHIFTMODE_REG to let multisyncs
--     with older TOS versions.
-- 

library ieee;
use ieee.std_logic_1164.all;
entity WF25914IP_SHMOD_REG is
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
end WF25914IP_SHMOD_REG;

-------------------------------------------------------------------------------

architecture BEHAVIOUR of WF25914IP_SHMOD_REG is
signal SH_MOD_REG	: std_logic_vector(7 downto 0);
signal xFF827E_REG	: std_logic_vector(7 downto 0);
signal ADR_I		: bit_vector(7 downto 0);
begin
	ADR_I <= '0' & ADR & '0';
  	REGISTERS: process
  	begin
		wait until CLK = '1' and CLK' event;
		if RESETn = '0' then
			case MULTISYNC is
                when "01" => SH_MOD_REG <= x"10";
                when "10" => SH_MOD_REG <= x"20";
                when others => SH_MOD_REG <= x"00";
            end case;
        elsif MULTISYNC = "10" and CSn = '0' and RWn = '0' and ADR_I = x"60" and DATA_IN = x"01" then
            SH_MOD_REG <= x"21"; -- 640x200x2 in 72Hz multisync compatible mode.
        elsif MULTISYNC = "10" and CSn = '0' and RWn = '0' and ADR_I = x"60" and DATA_IN = x"00" then
            SH_MOD_REG <= x"20"; -- 320x200x4 in 72Hz multisync compatible mode.
        elsif MULTISYNC = "01" and CSn = '0' and RWn = '0' and ADR_I = x"60" and DATA_IN = x"01" then
            SH_MOD_REG <= x"11"; -- 640x200x2 in 50/60Hz multisync compatible mode.
        elsif MULTISYNC = "01" and CSn = '0' and RWn = '0' and ADR_I = x"60" and DATA_IN = x"00" then
            SH_MOD_REG <= x"10"; -- 320x200x4 in 50/60Hz multisync compatible mode.
        elsif MULTISYNC = "00" and CSn = '0' and RWn = '0' and ADR_I = x"60" then
                SH_MOD_REG <= x"32";-- Multisync 72Hz monochrome mode.
        elsif CSn = '0' and RWn = '0' and ADR_I = x"60" then
            SH_MOD_REG <= DATA_IN; -- Legacy video modi.
        elsif CSn = '0' and RWn = '0' and ADR_I = x"7E" then
            xFF827E_REG <= DATA_IN; -- Stacy's and STBook's power management register.
		end if;
	end process REGISTERS;
	DATA_OUT <= SH_MOD_REG when CSn = '0' and RWn = '1' and ADR_I = x"60" and MULTISYNC = "11" else -- Read back the software controlled video modes.
				x"0" & SH_MOD_REG(3 downto 0) when CSn = '0' and RWn = '1' and ADR_I = x"60" else -- Read back the MULTISYNC controlled legacy video modes.
				xFF827E_REG when CSn = '0' and RWn = '1' and ADR_I = x"7E" else (others =>'0');
	DATA_EN <= 	'1' when CSn = '0' and RWn = '1' and ADR_I = x"60" else
				'1' when CSn = '0' and RWn = '1' and ADR_I = x"7E" else '0';

	SH_MOD <= To_BitVector(SH_MOD_REG);
	xFF827E <= To_BitVector(xFF827E_REG);
end BEHAVIOUR;
