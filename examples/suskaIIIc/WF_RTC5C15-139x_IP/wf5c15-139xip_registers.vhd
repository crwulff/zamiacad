----------------------------------------------------------------------
----                                                              ----
---- ATARI Real Time Clock (RTC) interface.  		              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- Interface to connect a DS1392 or DS1393 SPI timekeeper chip  ----
---- to the Atari IP core. The interface is on the system side    ----
---- compatible with the original used RP5C15 chip.               ----
----                                                              ----
---- This files is moddeling the RP5C15 relevant registers.       ----
---- The LEPYEAR counter can be written or read but is not        ----
---- serviced by the timekeeper chip because the lepyear cor-     ----
---- rection is included in all modern RTCs.                      ----
---- The ADJ feature of the RP5C15 is also not serviced by the    ----
---- used timekeeper chip and can therefore be used as a register ----
---- with a single bit.                                           ----
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
---- Copyright (C) 2007 - 2008 Wolfgang Foerster                  ----
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
-- Revision 2K7A  2007/01/05 WF
--   Initial Release.
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF5C15_139xIP_REGISTERS is
	port(
		CLK				: in bit;
		RESETn			: in bit;
		ADR				: in bit_vector(3 downto 0);
		DATA_IN			: in std_logic_vector(3 downto 0);
		DATA_OUT		: out std_logic_vector(3 downto 0);
		DATA_EN			: out bit;
		CS, CSn			: in bit;
		WRn, RDn		: in bit;

		DATA_VALID		: out bit;

		SPI_STORE		: in bit;
		SPI_DATASEL		: in bit_vector(3 downto 0);
		SPI_DATA_IN		: in std_logic_vector(7 downto 0);
		SPI_DATA_OUT	: out std_logic_vector(7 downto 0);
		SPI_PENDING		: out bit_vector(10 downto 0)
	);
end WF5C15_139xIP_REGISTERS;

architecture BEHAVIOR of WF5C15_139xIP_REGISTERS is
signal SECONDS			: std_logic_vector(6 downto 0);
signal MINUTES			: std_logic_vector(6 downto 0);
signal HOURS			: std_logic_vector(5 downto 0);
signal DAY				: std_logic_vector(2 downto 0);
signal DATE				: std_logic_vector(5 downto 0);
signal MONTH			: std_logic_vector(4 downto 0);
signal YEAR				: std_logic_vector(7 downto 0);

signal ALARM_MINUTES	: std_logic_vector(6 downto 0);
signal ALARM_HOURS		: std_logic_vector(5 downto 0);
signal ALARM_DAY		: std_logic_vector(2 downto 0);
signal ALARM_DATE		: std_logic_vector(5 downto 0);

signal LEPYEAR			: std_logic_vector(1 downto 0);
signal ADJUST			: std_logic;

signal BANK				: std_logic;
signal TIMER_EN			: std_logic;
signal ALARM_EN			: std_logic;
signal TIMER_RESET		: std_logic;
signal ALARM_RESET		: std_logic;
signal EN_1Hz			: std_logic;
signal EN_16Hz			: std_logic;
signal TEST				: std_logic_vector(3 downto 0); -- Useable as general purpose register.
signal CLKSEL			: std_logic_vector(2 downto 0); -- Clock output select of the RP5C15 chip.
signal CLKSEL_DS139x	: std_logic_vector(1 downto 0); -- Clock output select of the DS1337 chip.
signal FORMAT_12n_24	: std_logic;
begin
	-- Not supported RP5C15 registers / flags:
	-- 1. The leap year counter is not supported by the DS139x which has built in calendar and does not
	--    require leap year corrections until 2100.
	-- 2. The ADJUST flag is not necessary in the DS139x.
	-- 4. The TIMER_RESET flag is not supported by the DS139x.
	-- 5. The ALARM_EN and the ALARM_RESET flags are not supported by the DS139x.
	-- 6. The EN_1Hz and the EN_16Hz flags are not supported by the DS139x.
	--  All not used register bits can be used as general purpose flags.
	
	-- The following clock frequency translation is necessary because the DS1337 does not support as many
	-- choices as the RP5C15. Especially the High impedant pin of the RP5C15 and the low output are not
	-- supported.
	with CLKSEL select
		CLKSEL_DS139x <= "11" when "001", -- 16384Hz of the RP5C15 translates to 32768Hz of the DS139x.
						 "10" when "010", -- 1024Hz of the RP5C15 translates to 8192Hz of the DS139x.
						 "01" when "011", -- 128Hz of the RP5C15 translates to 4096Hz of the DS139x.
						 "00" when "101", -- 1Hz of the RP5C15 translates to 1Hz of the DS139x.
						 "00" when others; -- Default is 1Hz.

	-- The data is declared valid if there is no bus access.
	DATA_VALID <= '1' when CS = '0' or CSn = '1' else '0';

	P_PENDING: process(RESETn, CLK)
	-- This process detects, if there is a need to write registers contents to the
	-- RTC chip. This happens, if the registers are updated from the databus. There
	-- is a flag for each register to indicate the register which needs an update.
	variable PENDING	: bit_vector(10 downto 0);
	begin
		if RESETn = '0' then
			PENDING := (others => '0');
		elsif CLK = '1' and CLK' event then
			if CS = '1' and CSn = '0' and WRn = '0' then
				if BANK = '0' then
					case ADR is
						when x"0" => PENDING(0) := '1'; -- Seconds.
						when x"1" => PENDING(0) := '1'; -- Seconds.
						when x"2" => PENDING(1) := '1'; -- Minutes.
						when x"3" => PENDING(1) := '1'; -- Minutes.
						when x"4" => PENDING(2) := '1'; -- Hours.
						when x"5" => PENDING(2) := '1'; -- Hours.
						when x"6" => PENDING(3) := '1'; -- Day.
						when x"7" => PENDING(4) := '1'; -- Date.
						when x"8" => PENDING(4) := '1'; -- Date.
						when x"9" => PENDING(5) := '1'; -- Month.
						when x"A" => PENDING(5) := '1'; -- Month.
						when x"B" => PENDING(6) := '1'; -- Year.
						when x"C" => PENDING(6) := '1'; -- Year.
						when x"D" => PENDING(10) := '1'; -- Control register.
						when x"E" => null; -- No action required.
						when x"F" => null; -- No action required.
					end case;
				else
					case ADR is
						when x"0" => PENDING(10) := '1'; -- Control register.
						when x"2" => PENDING(7) := '1'; -- Alarm minutes.
						when x"3" => PENDING(7) := '1'; -- Alarm minutes.
						when x"4" => PENDING(8) := '1'; -- Alarm hours.
						when x"5" => PENDING(8) := '1'; -- Alarm hours.
						when x"6" => PENDING(9) := '1'; -- Alarm day.
						when x"7" => PENDING(9) := '1'; -- Alarm date.
						when x"8" => PENDING(9) := '1'; -- Alarm date.
						when x"A" => PENDING(2) := '1'; -- Time format.
						when x"D" => PENDING(10) := '1'; -- Control register.
						when others => null; -- No action required.
					end case;
				end if;
			elsif SPI_STORE = '1' then
				case SPI_DATASEL is
					when x"1" => PENDING(0) := '0'; -- Seconds.
					when x"2" => PENDING(1) := '0'; -- Minutes.
					when x"3" => PENDING(2) := '0'; -- Hour.
					when x"4" => PENDING(3) := '0'; -- Day.
					when x"5" => PENDING(4) := '0'; -- Date.
					when x"6" => PENDING(5) := '0'; -- Month.
					when x"7" => PENDING(6) := '0'; -- Year.
					when x"A" => PENDING(7) := '0'; -- Alarm minutes.
					when x"B" => PENDING(8) := '0'; -- Alarm hours.
					when x"C" => PENDING(9) := '0'; -- Alarm date.
					when x"D" => PENDING(10) := '0'; -- Control register.
					when others => null;
				end case;
			end if;			
		end if;
		SPI_PENDING <= PENDING;
	end process P_PENDING;

	WRITEREGS: process(RESETn, CLK)
	begin
		if RESETn = '0' then
			SECONDS	<= (others => '0');
			MINUTES <= (others => '0');
			HOURS	<= (others => '0');
			DAY		<= (others => '0');
			DATE 	<= (others => '0');
			MONTH 	<= (others => '0');
			YEAR 	<= (others => '0');

			ALARM_MINUTES 	<= (others => '0');
			ALARM_HOURS		<= (others => '0');
			ALARM_DAY		<= (others => '0');
			ALARM_DATE 		<= (others => '0');

			BANK			<= '0';
			TIMER_EN		<= '0';
			ALARM_EN		<= '0';
			TIMER_RESET		<= '0';
			ALARM_RESET		<= '0';
			EN_1Hz			<= '0';
			EN_16Hz			<= '0';
			TEST			<= (others => '0');
			CLKSEL			<= (others => '0');
			FORMAT_12n_24	<= '0';
		elsif CLK = '1' and CLK' event then
			-- Store data coming from the SPI register with lower priority than store MC data.
			-- The reason for this is not to loose data if the MC controller writes to any registers
			-- during the write access from the SPI interface.
			if SPI_STORE = '1' then
				case SPI_DATASEL is
					when x"0" => null; -- There are no hundredths of seconds in the RP5C15.
					when x"1" => SECONDS <= SPI_DATA_IN (6 downto 0);
					when x"2" => MINUTES <= SPI_DATA_IN (6 downto 0);
					when x"3" => 
						HOURS <= SPI_DATA_IN (5 downto 0);
						FORMAT_12n_24 <= SPI_DATA_IN (6);
					when x"4" => DAY <= SPI_DATA_IN (2 downto 0);
					when x"5" => DATE <= SPI_DATA_IN (5 downto 0);
					when x"6" => MONTH <= SPI_DATA_IN (4 downto 0);
					when x"7" => YEAR <= SPI_DATA_IN (7 downto 0);
					when x"8" => null; -- There are no hundredths of alarm seconds in the RP5C15.
					when x"9" => null; -- There are no alarm seconds in the RP5C15.
					when x"A" => ALARM_MINUTES <= SPI_DATA_IN (6 downto 0);
					when x"B" => ALARM_HOURS <= SPI_DATA_IN (5 downto 0);
					when x"C" => 
						if SPI_DATA_IN(6) = '1' then -- Day of the week.
							ALARM_DAY <= SPI_DATA_IN (2 downto 0);
						else -- Date of the month.
							ALARM_DATE <= SPI_DATA_IN (5 downto 0);
						end if;
					when x"D" => -- Control register.
						TIMER_EN <= SPI_DATA_IN (7);
						case SPI_DATA_IN (4 downto 3) is -- Rate select flags.
							when "11" => CLKSEL <= "001"; -- Translations see above.
							when "10" => CLKSEL <= "010";
							when "01" => CLKSEL <= "011";
							when "00" => CLKSEL <= "101";
							when others => null;
						end case;
						-- Other flags are not used.
					when x"E" => null; -- Status register, not used.
					when x"F" => null; -- Trickle charger, not used.
				end case;
			end if;

			if ADR = x"0" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				SECONDS(3 downto 0) <= DATA_IN;
			elsif ADR = x"1" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				SECONDS(6 downto 4) <= DATA_IN(2 downto 0);
			elsif ADR = x"2" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				MINUTES(3 downto 0) <= DATA_IN;
			elsif ADR = x"3" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				MINUTES(6 downto 4) <= DATA_IN(2 downto 0);
			elsif ADR = x"4" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				HOURS(3 downto 0) <= DATA_IN;
			elsif ADR = x"5" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				HOURS(5 downto 4) <= DATA_IN(1 downto 0);
			elsif ADR = x"6" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				DAY <= DATA_IN(2 downto 0);
			elsif ADR = x"7" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				DATE(3 downto 0) <= DATA_IN;
			elsif ADR = x"8" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				DATE(5 downto 4) <= DATA_IN(1 downto 0);
			elsif ADR = x"9" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				MONTH(3 downto 0) <= DATA_IN;
			elsif ADR = x"A" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				MONTH(4) <= DATA_IN(0);
			elsif ADR = x"B" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				YEAR(3 downto 0) <= DATA_IN;
			elsif ADR = x"C" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				YEAR(7 downto 4) <= DATA_IN;
			elsif ADR = x"D" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				TIMER_EN	<= DATA_IN(3);
				ALARM_EN	<= DATA_IN(2);
				BANK		<= DATA_IN(0);
			elsif ADR = x"E" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				TEST <= DATA_IN;
			elsif ADR = x"F" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '0' then
				EN_1Hz		<= DATA_IN(3);
				EN_16Hz		<= DATA_IN(2);
				TIMER_RESET	<= DATA_IN(1);
				ALARM_RESET	<= DATA_IN(0);
			elsif ADR = x"0" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				CLKSEL <= DATA_IN(2 downto 0);
			elsif ADR = x"1" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				ADJUST <= DATA_IN(0); -- Not serviced, see also fileheader.
			elsif ADR = x"2" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				ALARM_MINUTES(3 downto 0) <= DATA_IN;
			elsif ADR = x"3" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				ALARM_MINUTES(6 downto 4) <= DATA_IN(2 downto 0);
			elsif ADR = x"4" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				ALARM_HOURS(3 downto 0) <= DATA_IN;
			elsif ADR = x"5" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				ALARM_HOURS(5 downto 4) <= DATA_IN(1 downto 0);
			elsif ADR = x"6" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				ALARM_DAY <= DATA_IN(2 downto 0);
			elsif ADR = x"7" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				ALARM_DATE(3 downto 0) <= DATA_IN;
			elsif ADR = x"8" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				ALARM_DATE(5 downto 4) <= DATA_IN(1 downto 0);
			elsif ADR = x"9" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				null; -- Not used in RP5C15.
			elsif ADR = x"A" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				FORMAT_12n_24 <= DATA_IN(0);
			elsif ADR = x"B" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				LEPYEAR <= DATA_IN(1 downto 0); -- Not serviced, see also fileheader.
			elsif ADR = x"C" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				null; -- Not used in RP5C15.
			elsif ADR = x"D" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				TIMER_EN	<= DATA_IN(3);
				ALARM_EN	<= DATA_IN(2);
				BANK		<= DATA_IN(0);
			elsif ADR = x"E" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				TEST <= DATA_IN;
			elsif ADR = x"F" and CS = '1' and CSn = '0' and WRn = '0' and BANK = '1' then
				EN_1Hz			<= DATA_IN(3);
				EN_16Hz			<= DATA_IN(2);
				TIMER_RESET		<= DATA_IN(1);
				ALARM_RESET		<= DATA_IN(0);
			end if;
		end if;
	end process WRITEREGS;

	-- Read from the registers:
	-- Note that not existing register bits read back as zero.
	with ADR & To_Bit(BANK) & CS & CSn & RDn select
		DATA_OUT <=	SECONDS(3 downto 0) 							when x"04",
					'0' & SECONDS(6 downto 4) 						when x"14",
					MINUTES(3 downto 0) 							when x"24",
					'0' & MINUTES(6 downto 4) 						when x"34",
					HOURS(3 downto 0) 								when x"44",
					"00" & HOURS(5 downto 4) 						when x"54",
					'0' & DAY 										when x"64",
					DATE(3 downto 0) 								when x"74",
					"00" & DATE(5 downto 4) 						when x"84",
					MONTH(3 downto 0) 								when x"94",
					"000" & MONTH(4) 								when x"A4",
					YEAR(3 downto 0) 								when x"B4",
					YEAR(7 downto 4) 								when x"C4",
					TIMER_EN & ALARM_EN & '0' & BANK 				when x"D4",
					TEST 											when x"E4",
					TIMER_RESET &  ALARM_RESET & EN_1Hz & EN_16Hz	when x"F4",
					'0' & CLKSEL 									when x"0C",
					"000" & ADJUST 									when x"1C",
					ALARM_MINUTES(3 downto 0) 						when x"2C",
					'0' & ALARM_MINUTES(6 downto 4) 				when x"3C",
					ALARM_HOURS(3 downto 0) 						when x"4C",
					"00" & ALARM_HOURS(5 downto 4) 					when x"5C",
					'0' & ALARM_DAY 								when x"6C",
					ALARM_DATE(3 downto 0) 							when x"7C",
					"00" & ALARM_DATE(5 downto 4)					when x"8C",
					-- when x"9D", not used.
					"000" & FORMAT_12n_24 							when x"AD",
					"00" & LEPYEAR	 								when x"BD",
					-- when x"CD", not used.
					TIMER_EN & ALARM_EN & '0' & BANK 				when x"DD",
					TEST 											when x"ED",
					TIMER_RESET &  ALARM_RESET & EN_1Hz & EN_16Hz 	when x"FD",
					x"0" 											when others;
	
	DATA_EN <= '1' when CS = '1' and CSn = '0' and RDn = '0' else '0';

	with SPI_DATASEL select
		SPI_DATA_OUT <=	'0' & SECONDS						when x"1",
						'0' & MINUTES 						when x"2",
						'0' & FORMAT_12n_24 & HOURS 		when x"3",
						"00000" & DAY 						when x"4",
						"00" & DATE 						when x"5",
						"000" & MONTH 						when x"6",
						YEAR 								when x"7",
						'0' & ALARM_MINUTES 				when x"A",
						'0' & FORMAT_12n_24 & ALARM_HOURS 	when x"B",
						"00" & ALARM_DATE 					when x"C",
						TIMER_EN & "00" & CLKSEL_DS139x(1 downto 0) & "000" when x"D", -- Control register.
						-- when x"E" -- Status register is read only and not used.
						-- when x"F" -- Trickle charger is not used.
						x"00" 								when others;
end architecture BEHAVIOR;
