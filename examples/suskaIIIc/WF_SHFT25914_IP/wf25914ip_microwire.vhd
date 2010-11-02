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
---- STE's microwire interface to operate the LMC1992.            ----
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
-- Revision 2K6B  2006/11/06 WF
--   Modified Source to compile with the Xilinx ISE.
-- Revision 2K8A  2008/05/29 WF
--   Changed the rotating shift registers for MW_DATA and MW_MASK.
-- Revision 2K9A  2009/06/20 WF
--   MWD has now synchronous reset to meet preset requirement.
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25914IP_MICROWIRE is
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
	MWEn		: buffer bit	-- Microwire enable (low active).
  );
end WF25914IP_MICROWIRE;

architecture BEHAVIOUR of WF25914IP_MICROWIRE is
type MW_STATUSTYPE is (IDLE, RUN);
signal MW_STATUS	: MW_STATUSTYPE; -- Locks MW registers during shift operation.
signal MW_DATA		: std_logic_vector(15 downto 0);  -- Data register $FF8922.
signal MW_MASK		: std_logic_vector(15 downto 0);  -- Mask register $FF8924.
signal MWK_MASK		: bit;  -- Mask for the microwire clock.
signal SHIFTCLK		: bit;
begin
	-- Microwire clock is active when MWEn is asserted
	-- -> see microwire specification. Microwire starts when
	-- MWEn is asserted during MWK = '0';
	-- the MWK_MASK is somewhat ATARI specific. It enables the
	-- microwire clock only for valid Mask register bits. So
	-- it is possible to send don't care data.
	MWK <= SHIFTCLK when MWEn = '0' and MWK_MASK = '1' else '0';

	PRESCALER: process(RESETn, CLK)
	variable TMP : std_logic_vector(4 downto 0);
	begin
		if RESETn = '0' then
			TMP := "00000";
		elsif CLK = '1' and CLK' event then -- 32MHz clock.
			TMP := TMP + '1';
			if TMP <= "01111" then
				SHIFTCLK <= '0'; -- 1MHz.
			else
				SHIFTCLK <= '1';
			end if;
		end if;
	end process PRESCALER;

	MICROWIRE_OUT: process
	-- The MWD and MWEn are SHIFTCLK-synchronous
	-- shifted out in this process. Also the
	-- MWK_MASK is generated with one shift 
	-- clock delay.
	variable LOCK : boolean;
	begin
		wait until CLK = '1' and CLK' event;
		if RESETn = '0' then
			MWEn <= '1';
			MWD <= '1';
			LOCK := false;
		elsif SHIFTCLK = '0' and LOCK = false then
			LOCK := true;
			MWK_MASK <= To_Bit(MW_MASK(15));
			case MW_STATUS is
				when RUN 	=> 	MWEn <= '0';
								MWD <= To_Bit(MW_DATA(15));
				when IDLE 	=> 	MWEn <= '1';
								MWD <= '1';
			end case;
		elsif SHIFTCLK = '1' then
			LOCK := false;
		end if;
	end process MICROWIRE_OUT;

	MW_REGISTERS: process(RESETn, CLK)
	variable LOCK	: boolean;
	variable BITCNT : std_logic_vector(4 downto 0);
	begin
		if RESETn = '0' then
			MW_DATA <= (others => '0');
			MW_MASK <= (others => '0');
			BITCNT := "00000";
			LOCK := false;
		elsif CLK = '1' and CLK' event then
			-- So write access if microwire interface is active (MW_STATUS = RUN).
			if ADR = "010001" and CMPCSn = '0' and RWn = '0' and MW_STATUS = IDLE then
				MW_DATA <= DATA_IN; -- Write to register.
				MW_STATUS <= RUN; -- start transmission after register write
			elsif ADR = "010010" and CMPCSn = '0' and RWn = '0' and MW_STATUS = IDLE then
				MW_MASK <= DATA_IN; -- Write to register.
			-- Shift operations here on positive SHIFTCLK edge.
			elsif MW_STATUS = RUN and SHIFTCLK = '1' and LOCK = false then
				LOCK := true; -- Shift with 1MHz.
				-- Do not rotate the shift registers but clear the respective LSB.
                -- MW_DATA <= MW_DATA(14 downto 0) & MW_DATA(15); -- Rotate left.
                -- MW_MASK <= MW_MASK(14 downto 0) & MW_MASK(15); -- Rotate left.
                MW_DATA <= MW_DATA(14 downto 0) & '0'; -- Shift left, clear right.
                MW_MASK <= MW_MASK(14 downto 0) & '0'; -- shift left, clear right.
				BITCNT := BITCNT + '1'; -- Supervision of the the rotation.
			elsif SHIFTCLK = '0' then
				LOCK := false;
			elsif MW_STATUS = IDLE then
				BITCNT := "00000";
				-- LOCK := true means: wait when entering RUN modus
				-- for the next rising clock edge of SHIFTCLK.
				LOCK := true;
			end if;
			if BITCNT = "10000" then
				MW_STATUS <= IDLE; -- Stop the shift process after 16 bits.
			end if;
		end if;
	end process MW_REGISTERS;
	-- Register read access is possible, even if microwire interface is active.
	DATA_OUT <= MW_DATA when ADR = "010001" and CMPCSn = '0' and RWn = '1' else
				MW_MASK when ADR = "010010" and CMPCSn = '0' and RWn = '1' else (others => '0'); -- Read.
	DATA_EN <= 	'1' when ADR = "010001" and CMPCSn = '0' and RWn = '1' else
				'1' when ADR = "010010" and CMPCSn = '0' and RWn = '1' else '0';
end BEHAVIOUR;
