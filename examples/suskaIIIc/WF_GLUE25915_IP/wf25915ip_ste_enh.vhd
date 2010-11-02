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
---- This file contains the enhancements of the STE over the ST.  ----
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
-- Revision 2K7A  2007/01/02 WF
--   Changes to the clock system and related
--   hardware as video timing or paddles.
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- 

library work;
use work.wf25915ip_pkg.all;
library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25915IP_STE_ENH is
	port(
		CLK, CLK_x1_16	: in bit;
		CLKSEL			: in CLKSEL_TYPE;
		RESETn			: in bit;
		RWn				: in bit;
		DATA_IN			: in std_logic_vector(1 downto 0);
		DATA_OUT		: out std_logic_vector(15 downto 0);
		DATA_EN			: out bit;

		HD_REG_CSn		: in bit; -- Select signal for the high density floppy control register.
		FDDS			: out bit; -- Floppy type select.
		FCCLK			: out bit; -- Floppy controller frequency select.
		
		PAD0X_CS		: in bit; -- Paddle counter register chip select.
		PAD0Y_CS		: in bit; -- Paddle counter register chip select.
		PAD1X_CS		: in bit; -- Paddle counter register chip select.
		PAD1Y_CS		: in bit; -- Paddle counter register chip select.
		
		PAD0X_INHn		: in bit; -- counter input for the Paddle 0X.
		PAD0Y_INHn		: in bit; -- counter input for the Paddle 0Y.
		PAD1X_INHn		: in bit; -- counter input for the Paddle 1X.
		PAD1Y_INHn		: in bit; -- counter input for the Paddle 1Y.
		PADRSTn			: out bit; -- Paddle monoflops reset.

		XPEN_REG_CS		: in bit; -- Pen register access.
		YPEN_REG_CS		: in bit; -- Pen register access.
		
		HSYNCn			: in bit;
		VSYNCn			: in bit;
		DE				: in bit;
		
		PENn			: in bit -- Light pen input.
	);
end entity WF25915IP_STE_ENH;

architecture BEHAVIOR of WF25915IP_STE_ENH is
signal HDREG		: std_logic_vector(1 downto 0);
signal PAD0X_REG	: std_logic_vector(7 downto 0);
signal PAD0Y_REG	: std_logic_vector(7 downto 0);
signal PAD1X_REG	: std_logic_vector(7 downto 0);
signal PAD1Y_REG	: std_logic_vector(7 downto 0);
signal RESET_CNT	: std_logic_vector(7 downto 0);
signal COUNT_RESET	: bit;
signal XPEN_REG		: std_logic_vector(15 downto 0);
signal YPEN_REG		: std_logic_vector(15 downto 0);
signal XPEN_CNT		: std_logic_vector(16 downto 0);
signal YPEN_CNT		: std_logic_vector(15 downto 0);

begin
	FDDS <= '1' when HDREG(1) = '1' else '0'; -- Floppy type select.
	FCCLK <= '1' when HDREG(0) = '1' else '0'; -- Floppy controller frequency select.
	
	HD_REG: process(RESETn, CLK)
	-- The HD register stores the information wether a DD or a HD floppy type is selected.
	-- In original STEs the selection is for example done in the format control box.
	-- Originally (in STEs) the HD floppies are selected via two bits. The FCCLK is a control
	-- pin which switches the floppy controller frequency from 8MHz in DD floppy mode to 16MHz
	-- in HD mode. The FDDS signal tells the floppy, that DD or HD mode is selected.
	-- The IP core of the floppy controller does not work with a gated frequency. It selects
	-- HD or DD floppy mode also by using the FDDS signal. Therefore the FCCLK is not really
	-- required.
	begin
		if RESETn = '0' then
			HDREG <= "00";
		elsif CLK = '1' and CLK' event then
			if HD_REG_CSn = '0' and RWn = '0' then
				HDREG <= DATA_IN;
			end if;
		end if;
	end process HD_REG;

	COUNTER_RESET: process(CLK, RESET_CNT, RESETn)
	variable EDGE_LOCK: boolean;
	begin
		if RESETn = '0' then
			RESET_CNT <= x"00";
			EDGE_LOCK := false;
		elsif CLK = '1' and CLK' event then
			if CLK_x1_16 = '1' and EDGE_LOCK = false then
				RESET_CNT <= RESET_CNT + '1';
				EDGE_LOCK := true;
			elsif CLK_x1_16 = '0' then
				EDGE_LOCK := false;
			end if;
		end if;
	end process COUNTER_RESET;
		
	PADDLECOUNTER: process (CLK, RESETn)
	variable EDGE_LOCK: boolean;
	begin
		if RESETn = '0' then
			PAD0X_REG <= x"00";
			PAD0Y_REG <= x"00";
			PAD1X_REG <= x"00";
			PAD1Y_REG <= x"00";
			EDGE_LOCK := false;
		elsif CLK = '1' and CLK' event then
			if CLK_x1_16 = '1' and EDGE_LOCK = false then
				EDGE_LOCK := true;
				if COUNT_RESET = '1' then
					PAD0X_REG <= x"00";
				elsif 	PAD0X_INHn = '1' and PAD0X_REG < x"FF" then -- Stop at x"FF".
					PAD0X_REG <= PAD0X_REG + '1';			
				end if;
				if COUNT_RESET = '1' then
					PAD0Y_REG <= x"00";
				elsif 	PAD0Y_INHn = '1' and PAD0Y_REG < x"FF" then -- Stop at x"FF".
					PAD0Y_REG <= PAD0Y_REG + '1';			
				end if;
				if COUNT_RESET = '1' then
					PAD1X_REG <= x"00";
				elsif 	PAD1X_INHn = '1' and PAD1X_REG < x"FF" then -- Stop at x"FF".
					PAD1X_REG <= PAD1X_REG + '1';			
				end if;
				if COUNT_RESET = '1' then
					PAD1Y_REG <= x"00";
				elsif 	PAD1Y_INHn = '1' and PAD1Y_REG < x"FF" then -- Stop at x"FF".
					PAD1Y_REG <= PAD1Y_REG + '1';			
				end if;
			elsif CLK_x1_16 = '0' then
				EDGE_LOCK := false;
			end if;
		end if;
	end process PADDLECOUNTER;

	X_PEN_CNT: process(CLK, RESETn)
	 -- The counter works with 8MHz or with 16MHz.
	begin
		if RESETn = '0' then
			XPEN_CNT <= (others => '0');
		elsif CLK = '1' and CLK' event then
			if DE = '1' then
				XPEN_CNT <= XPEN_CNT + '1'; -- 8MHz or 16MHz.
			else
				XPEN_CNT <= (others => '0'); -- Erase counter during horizontal sync.
			end if;			
		end if;			
	end process X_PEN_CNT;
	
	Y_PEN_CNT: process(CLK, RESETn)
	variable EDGE_LOCK: boolean;
	begin
		if RESETn = '0' then
			YPEN_CNT <= x"0000";
			EDGE_LOCK := false;
		elsif CLK = '1' and CLK' event then -- Counter counts the lines.
			if HSYNCn = '1' and EDGE_LOCK = false then -- Counter counts the lines.
				EDGE_LOCK := true;
				if VSYNCn = '1' then
					YPEN_CNT <= YPEN_CNT + '1';
				else
					YPEN_CNT <= x"0000"; -- Erase counter in vertical sync.
				end if;			
			elsif HSYNCn = '0' then
				EDGE_LOCK := false;
			end if;
		end if;			
	end process Y_PEN_CNT;

	PEN_REGS: process(CLK, RESETn)
	begin
		if RESETn = '0' then
			XPEN_REG <= x"0000";
			YPEN_REG <= x"0000";
		elsif CLK = '1' and CLK' event then
			if PENn = '0' then
				case CLKSEL is
					when CLK_16M => XPEN_REG <= XPEN_CNT(16 downto 1);
					when CLK_8M => XPEN_REG <= XPEN_CNT(15 downto 0);
				end case;
				YPEN_REG <= YPEN_CNT;
			end if;			
		end if;			
	end process PEN_REGS;

	-- Controls:
	COUNT_RESET <= '1' when RESET_CNT = x"FF" else '0'; -- Reset after about 0.52ms.
	PADRSTn <= '1' when RESET_CNT = x"FF" else '0'; -- Reset after about 0.52ms.

	-- Read registers:
	-- Unused bits read back as '0'.
	DATA_OUT <= "00000000000000" & HDREG when HD_REG_CSn = '0' and RWn = '1' else
				x"00" & PAD0X_REG when PAD0X_CS = '1' else
				x"00" & PAD0Y_REG when PAD0Y_CS = '1' else
				x"00" & PAD1X_REG when PAD1X_CS = '1' else
				x"00" & PAD1Y_REG when PAD1Y_CS = '1' else
				XPEN_REG when XPEN_REG_CS = '1' else
				YPEN_REG when YPEN_REG_CS = '1' else (others => '0');
	DATA_EN <=  '1' when HD_REG_CSn = '0' and RWn = '1' else
				'1' when PAD0X_CS = '1' or PAD0Y_CS = '1' else
				'1' when PAD1X_CS = '1' or PAD1Y_CS = '1' else
				'1' when XPEN_REG_CS = '1' or YPEN_REG_CS = '1' else '0';
end architecture BEHAVIOR;
