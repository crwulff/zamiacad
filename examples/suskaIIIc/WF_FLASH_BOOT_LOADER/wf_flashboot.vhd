-------------------------------------------------------------------------
----                                                                 ----
---- Suska boot loader unit                                          ----
----                                                                 ----
---- This file is part of the SUSKA ATARI clone project.             ----
---- http://www.experiment-s.de                                      ----
----                                                                 ----
---- Description:                                                    ----
---- This boot loader is written for flash memory type M29W800       ----
---- or similar.                                                     ----
---- The boot loader unit provides the programming of the opera-     ----
---- ting system flash ROM during a boot sequence. The working       ----
---- principle of the boot loader is as follows:                     ----
---- Enter the boot loader by pressing the RESET_INn and then        ----
---- RESET_COREn button. Then release first the RESET_COREn          ----
---- button then the RESET_INn button. The BOOT_LED begins blinking  ----
---- for about 5 seconds. If the RESET_INn button is pressed again   ----
---- during this time, the boot loader routine begins with the       ----
---- erasing of the flash memory. If the RESET_INn is not pressed    ----
---- within these five seconds, the boot loader routine finishes     ----
---- without any action, the system is ready to boot normally.       ----
---- Once entered the flash erase routine, the boot loader erases    ----
---- the complete flash memory, then enters the request address      ----
---- routine in which a start address for the write cyle is re-      ----
---- quested via the SPI interface. This state must be acknow-       ----
---- ledged by the SPI host. If not so, the boot loader hangs and    ----
---- ends up only by a new system reset. The start address is 16     ----
---- bit wide and mactches the upper bits of the flash memory        ----
---- here: (A19 ... A4). After the start address is acknowledged,    ----
---- the boot loader enters the write section and proceeds after     ----
---- it with the read section. The read or write section is          ----
---- controlled by request acknowledge and ends up in a time out     ----
---- of about 1 second means if a request is not acknowledged        ----
---- after the time out, the current state is finished. After the    ----
---- read state finishes, the system is in normal operation.         ----
---- During the write section the boot loader requests data from     ----
---- the SPI interface via BOOT_REQ. If the data is received and     ----
---- the acknowledge BOOT_ACK_S is asserted the data is written to   ----
---- the flash chip and the next data is requested. The address      ----
---- of the flash is incremented automatically. It starts at zero    ----
---- and ends up at the maximum address for the chip which is        ----
---- given by the number of the address lines. The data written      ----
---- is always 16 bit wide which results in shifting in 16 bits      ----
---- for each data via the SPI interface. If the maximum address     ----
---- is acknowledged, the address counter rolls over and starts      ----
---- again with address zero. Be aware, that writing a flash         ----
---- twice with different data normally results in corrupted data.   ----
---- The read process works similar with reading out 16 bits         ----
---- after the request is asserted.                                  ----
---- During the shift operation via the SPI interface, the most      ----
---- significant bit is first shifted out and in.                    ----
---- Be aware, that the data in the shift register is sampled on     ----
---- the negative clock edge and the SPI_CLK should be '0' when      ----
---- inactive.                                                       ----
----                                                                 ----
---- 20071220: This boot loader is prepared to handle 8 bit SPI data ----
---- and 16 bit Flash data and address. The boot controller manages  ----
---- this by requesting two bytes per Request for each flash word or ----
---- for the address offset.                                         ----
----                                                                 ----
---- Author(s):                                                      ----
---- - Wolfgang Foerster, wf@experiment-s.de; wf@inventronik.de      ----
----                                                                 ----
-------------------------------------------------------------------------
----                                                                 ----
---- Copyright (C) 2007 - 2008 Wolfgang Foerster                     ----
----                                                                 ----
---- This source file may be used and distributed without            ----
---- restriction provided that this copyright statement is not       ----
---- removed from the file and that any derivative work contains     ----
---- the original copyright notice and the associated disclaimer.    ----
----                                                                 ----
---- This source file is free software; you can redistribute it      ----
---- and/or modify it under the terms of the GNU Lesser General      ----
---- Public License as published by the Free Software Foundation;    ----
---- either version 2.1 of the License, or (at your option) any      ----
---- later version.                                                  ----
----                                                                 ----
---- This source is distributed in the hope that it will be          ----
---- useful, but WITHOUT ANY WARRANTY; without even the implied      ----
---- warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR         ----
---- PURPOSE. See the GNU Lesser General Public License for more     ----
---- details.                                                        ----
----                                                                 ----
---- You should have received a copy of the GNU Lesser General       ----
---- Public License along with this source; if not, download it      ----
---- from http://www.gnu.org/licenses/lgpl.html                      ----
----                                                                 ----
-------------------------------------------------------------------------
-- 
-- Revision History
-- 
-- Revision 2K7A  2007/01/22 WF
--   Initial Release.
-- Revision 2K7B  2007/12/21 WF
--   Fixed a bug in the boot state machine.
--   Changes to meet 16bit SPI / 8bit MCU interface.
--   Changes to get the controller working with the
--   M29W800 flash.
-- Revision 2K8A  2007/12/31 WF
--   Introduced generic test mode.
--   Adaptions to meet the microcontroller bus timing.
-- Revision 2K9A  2009/06/20 WF
--   SHIFTREG has now synchronous reset to meeet preset behaviour.
--   Modified FLASH_RESETn.
--   Added flash erase only and flash write only modes.
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF_FLASHBOOT is
	-- Use this generic with values greater than zero for testing the boot
	-- loader without a microcontroller. When the NO_OF_TESTPATTERNS is
	-- equal to zero, the microcontroller interface is active. The maximum
	-- value for NO_OF_TESTPATTERNS is x"07FFFF" which is 2^19 words for the
	-- used M29W800 flash device or x"7FFFFF" which is 2^23 words for the 
	-- M29W128 flash device.
	generic(NO_OF_TESTPATTERNS	: std_logic_vector(23 downto 0) := x"000000");
	port(
		CLK				: in bit; -- Use 16MHz.
		PLL_LOCK		: in bit;
		RESET_COREn		: in bit;
		RESET_INn		: in bit;
		RESET_OUTn		: out bit;
		
		-- Bus control:
		ROM_CEn			: in bit;
		
		-- Data and address bus:
		ADR_OUT			: out std_logic_vector(23 downto 0);
		ADR_EN			: out bit;
		DATA_IN			: in std_logic_vector(15 downto 0);
		DATA_OUT		: out std_logic_vector(15 downto 0);
		DATA_EN			: out bit;
		
		-- Flash interface:
		FLASH_RDY		: in bit;
				
		FLASH_RESETn	: out bit;
		FLASH_WEn		: out bit;
		FLASH_OEn		: out bit;
		FLASH_CEn		: out bit;

		-- Microcontroller interface:		
		SPI_CLK			: in bit;
		SPI_DIN			: in bit;
		SPI_DOUT		: out bit;
		
		BOOT_ACK		: in bit;
		BOOT_REQ		: out bit;
		
		-- Status:
		BOOT_LED		: out bit
	);
end entity WF_FLASHBOOT;

architecture BEHAVIOR of WF_FLASHBOOT is
type FLASH_CMD_A is array (1 to 3, 0 to 5) of bit_vector(11 downto 0);
type FLASH_CMD_D is array (1 to 3, 0 to 5) of bit_vector(7 downto 0);
constant FLASH_CMDS_A : FLASH_CMD_A := 
	((x"555", x"2AA", x"555", x"555", x"2AA", x"555"), -- Erase entire chip.
	 (x"555", x"2AA", x"555", x"000", x"000", x"000"), --  Program.
	 (x"555", x"2AA", x"555", x"000", x"000", x"000") -- Chip reset.
	 -- (x"555", x"2AA", x"555", x"555", x"2AA", BA) -- Block erase (BA = block address);
	);
constant FLASH_CMDS_D : FLASH_CMD_D := 
	((x"AA", x"55", x"80", x"AA", x"55", x"10"), -- Erase entire chip.
	 (x"AA", x"55", x"A0", x"00", x"00", x"00"), -- Program.
	 (x"AA", x"55", x"F0", x"00", x"00", x"00") -- Chip reset.
	 -- (x"AA", x"55", x"80", x"AA", x"55", x"30") -- Block erase.
	);
type BOOT_STATES is (WAIT_PLL_LOCK, ATTENTION, WAIT_KEYRELEASE,
                    ACTIVATE, IDLE, FLASH_ERASE_1,FLASH_ERASE_2, ADR_REQ, CMD_REQ, WAIT_MC_1, BOOT_WR, WAIT_MC_2, BOOT_RD);
type ERASE_STATES is (IDLE, FLASH_INIT, WRITE_CMD, READY);
type WRITE_STATES is(IDLE, FLASH_INIT, WRITE_CMD, WAIT_ACK, WRITE_DATA, WRITE_END);
type READ_STATES is(IDLE, FLASH_INIT, WRITE_CMD, WAIT_ACK, DATA_LOAD);
signal BOOT_STATE		: BOOT_STATES;
signal NEXT_BOOT_STATE	: BOOT_STATES;
signal ERASE_STATE		: ERASE_STATES;
signal NEXT_ERASE_STATE	: ERASE_STATES;
signal WRITE_STATE		: WRITE_STATES;
signal NEXT_WRITE_STATE	: WRITE_STATES;
signal READ_STATE		: READ_STATES;
signal NEXT_READ_STATE	: READ_STATES;
signal ADR_OUT_I		: bit_vector(23 downto 0);
signal DATA_OUT_I		: bit_vector(15 downto 0);
signal D_SHIFTREG		: bit_vector(15 downto 0);
signal ADR_REG			: std_logic_vector(23 downto 0);
signal TIME_5			: boolean;
signal T_DELAY			: boolean;
signal ACK_TIMEOUT		: boolean;
signal CMD_PNTR			: integer range 0 to 7;
signal INIT_RDY			: boolean;
signal SPI_CLK_S		: bit;
signal SPI_DIN_S		: bit;
signal BOOT_ACK_I		: bit;
signal BOOT_ACK_S		: bit;
signal MC_WAITSTATE		: bit;
signal STARTUP          : boolean;
begin
	P_SYNC: process
	-- This synchronizing filter is important due to the different
	-- clock domains of the system microcontroller and the FPGA.
	variable SPI_CLK_TMP	: integer range 0 to 3;
	variable SPI_DIN_TMP	: integer range 0 to 3;
	variable BOOT_ACK_TMP	: integer range 0 to 3;
	variable LOCK: boolean;
	begin
		wait until CLK = '1' and CLK' event;
		if SPI_CLK = '1' and SPI_CLK_TMP < 3 then
			SPI_CLK_TMP := SPI_CLK_TMP + 1;
		elsif SPI_CLK = '1' then
			SPI_CLK_S <= '1';
		elsif SPI_CLK = '0' and SPI_CLK_TMP > 0 then
			SPI_CLK_TMP := SPI_CLK_TMP - 1;
		elsif SPI_CLK = '0' then
			SPI_CLK_S <= '0';
		end if;
		--
		if SPI_DIN = '1' and SPI_DIN_TMP < 3 then
			SPI_DIN_TMP := SPI_DIN_TMP + 1;
		elsif SPI_DIN = '1' then
			SPI_DIN_S <= '1';
		elsif SPI_DIN = '0' and SPI_DIN_TMP > 0 then
			SPI_DIN_TMP := SPI_DIN_TMP - 1;
		elsif SPI_DIN = '0' then
			SPI_DIN_S <= '0';
		end if;
		--
		if BOOT_ACK = '1' and BOOT_ACK_TMP < 3 then
			BOOT_ACK_TMP := BOOT_ACK_TMP + 1;
			BOOT_ACK_I <= '0';
		elsif BOOT_ACK = '1' and LOCK = false then
			BOOT_ACK_I <= '1';
			LOCK := true;
		elsif BOOT_ACK = '1' then
			BOOT_ACK_I <= '0';
		elsif BOOT_ACK = '0' and BOOT_ACK_TMP > 0 then
			BOOT_ACK_TMP := BOOT_ACK_TMP - 1;
			BOOT_ACK_I <= '0';
		elsif BOOT_ACK = '0' then
			BOOT_ACK_I <= '0';
			LOCK := false;
		end if;
	end process P_SYNC;

	-- Be aware of the testmode for NO_OF_TESTPATTERNS > x"000000".
	BOOT_ACK_S <= BOOT_ACK_I when NO_OF_TESTPATTERNS = x"000000" else
				  '1' when BOOT_STATE = ADR_REQ else
				  '1' when BOOT_STATE = CMD_REQ else
				  '1' when BOOT_STATE = BOOT_WR and ADR_REG < NO_OF_TESTPATTERNS else
				  '1' when READ_STATE = WAIT_ACK and DATA_IN = x"5A5A" and ADR_REG < NO_OF_TESTPATTERNS else '0';

	P_STARTUP: process(RESET_COREn, CLK)
	-- This process provides a timeout for the 
	-- detection of the boot sequence initiated
	-- by RESET_INn to prevent the start of the
	-- boot loader after power up sequence.
	variable TMP : std_logic_vector(19 downto 0);
	begin
		if RESET_COREn = '0' then
			STARTUP <= false;
			TMP := (others => '0');
		elsif CLK = '1' and CLK' event then
            if TMP < x"FFFFF" then
                STARTUP <= false;
                TMP := TMP + '1';
            else
                STARTUP <= true;
            end if;
		end if;
	end process P_STARTUP;

	STATE_REGs: process(RESET_COREn, CLK)
	begin
		if RESET_COREn = '0' then
			BOOT_STATE <= WAIT_PLL_LOCK;
			ERASE_STATE <= IDLE;
			WRITE_STATE <= IDLE;
			READ_STATE <= IDLE;
		elsif CLK = '1' and CLK' event then
			BOOT_STATE <= NEXT_BOOT_STATE;
			ERASE_STATE <= NEXT_ERASE_STATE;
			WRITE_STATE <= NEXT_WRITE_STATE;
			READ_STATE <= NEXT_READ_STATE;
		end if;
	end process STATE_REGs;

	BOOT_STATE_DEC: process(BOOT_STATE, PLL_LOCK, RESET_INn, TIME_5, ERASE_STATE, BOOT_ACK_S,
							D_SHIFTREG, ACK_TIMEOUT, WRITE_STATE, READ_STATE, MC_WAITSTATE, STARTUP)
	begin
		case BOOT_STATE is
			when WAIT_PLL_LOCK => -- PLLs must be locked first.
				if PLL_LOCK = '1' and STARTUP = true then
					NEXT_BOOT_STATE <= ATTENTION;
				else
					NEXT_BOOT_STATE <= WAIT_PLL_LOCK;
				end if;
			when ATTENTION =>
				if RESET_INn = '1' then -- No boot mode.
					NEXT_BOOT_STATE <= IDLE;
				else -- enter boot mode.
					NEXT_BOOT_STATE <= WAIT_KEYRELEASE;
				end if;
			when WAIT_KEYRELEASE =>
				if RESET_INn = '1' then
					NEXT_BOOT_STATE <= ACTIVATE;
				else
					NEXT_BOOT_STATE <= WAIT_KEYRELEASE;
				end if;
			when ACTIVATE => -- A second RESET_INn enters the Flash routine.
				if RESET_INn = '0' then
					NEXT_BOOT_STATE <= ADR_REQ;
				elsif TIME_5 = true then
					NEXT_BOOT_STATE <= IDLE;
				else
					NEXT_BOOT_STATE <= ACTIVATE;
				end if;
			when ADR_REQ =>
				if BOOT_ACK_S = '1' then
					NEXT_BOOT_STATE <= WAIT_MC_1;
				else
					NEXT_BOOT_STATE <= ADR_REQ;
				end if;
			when WAIT_MC_1 =>
				if MC_WAITSTATE = '1' then
					NEXT_BOOT_STATE <= CMD_REQ;
				else
					NEXT_BOOT_STATE <= WAIT_MC_1;
				end if;
			when CMD_REQ =>
				if NO_OF_TESTPATTERNS > x"000000" then
					NEXT_BOOT_STATE <= FLASH_ERASE_2;
				elsif BOOT_ACK_S = '1' and D_SHIFTREG(7 downto 0) = x"10" then -- Erase only.
					NEXT_BOOT_STATE <= FLASH_ERASE_1;
				elsif BOOT_ACK_S = '1' and D_SHIFTREG(7 downto 0) = x"17" then -- Read only.
					NEXT_BOOT_STATE <= WAIT_MC_2;
				elsif BOOT_ACK_S = '1' and D_SHIFTREG(7 downto 0) = x"20" then -- Program, read.
					NEXT_BOOT_STATE <= BOOT_WR;
				elsif BOOT_ACK_S = '1' and D_SHIFTREG(7 downto 0) = x"23" then -- Erase, program, read.
					NEXT_BOOT_STATE <= FLASH_ERASE_2;
				elsif BOOT_ACK_S = '1' then -- Wrong command.
					NEXT_BOOT_STATE <= IDLE;
				else
					NEXT_BOOT_STATE <= CMD_REQ;
				end if;
			when FLASH_ERASE_1 =>
				if ERASE_STATE = IDLE then
					NEXT_BOOT_STATE <= IDLE;
				else
					NEXT_BOOT_STATE <= FLASH_ERASE_1;
				end if;
			when FLASH_ERASE_2 =>
				if ERASE_STATE = IDLE then
					NEXT_BOOT_STATE <= BOOT_WR;
				else
					NEXT_BOOT_STATE <= FLASH_ERASE_2;
				end if;
			when BOOT_WR =>
				if WRITE_STATE = IDLE then -- Finished writing, go on.
					NEXT_BOOT_STATE <= WAIT_MC_2;
				else
					NEXT_BOOT_STATE <= BOOT_WR;
				end if;
			when WAIT_MC_2 =>
				if MC_WAITSTATE = '1' then
					NEXT_BOOT_STATE <= BOOT_RD;
				else
					NEXT_BOOT_STATE <= WAIT_MC_2;
				end if;
			when BOOT_RD =>
				if READ_STATE = IDLE then -- Finished reading, go on.
					NEXT_BOOT_STATE <= IDLE;
				else
					NEXT_BOOT_STATE <= BOOT_RD;
				end if;
			when IDLE =>
				NEXT_BOOT_STATE <= IDLE; -- Bootmode finished, stay here.
		end case;
	end process BOOT_STATE_DEC;

	ERASE_DEC: process(ERASE_STATE, BOOT_STATE, NEXT_BOOT_STATE, INIT_RDY, FLASH_RDY, BOOT_ACK_S, T_DELAY)
	begin
		case ERASE_STATE is
			when IDLE =>
				if BOOT_STATE /= FLASH_ERASE_1 and NEXT_BOOT_STATE = FLASH_ERASE_1 then
					NEXT_ERASE_STATE <= FLASH_INIT;
				elsif BOOT_STATE /= FLASH_ERASE_2 and NEXT_BOOT_STATE = FLASH_ERASE_2 then
					NEXT_ERASE_STATE <= FLASH_INIT;
				else
					NEXT_ERASE_STATE <= IDLE;
				end if;
			when FLASH_INIT =>
				-- Be aware, that the chip erase time can last upto 60s!
				if INIT_RDY = true and FLASH_RDY = '1' then
					NEXT_ERASE_STATE <= READY;
				elsif FLASH_RDY = '1' then
					NEXT_ERASE_STATE <= WRITE_CMD;
				else
					NEXT_ERASE_STATE <= FLASH_INIT;
				end if;
			when WRITE_CMD =>
				NEXT_ERASE_STATE <= FLASH_INIT;
			when READY =>
				if T_DELAY = true then
					NEXT_ERASE_STATE <= IDLE;
				else
					NEXT_ERASE_STATE <= READY;
				end if;
		end case;
	end process ERASE_DEC;
	
	WRITE_DEC: process(WRITE_STATE, BOOT_STATE, NEXT_BOOT_STATE, INIT_RDY, FLASH_RDY, BOOT_ACK_S, ACK_TIMEOUT, T_DELAY)
	begin
		case WRITE_STATE is
			when IDLE =>
				if BOOT_STATE /= BOOT_WR and NEXT_BOOT_STATE = BOOT_WR then
					NEXT_WRITE_STATE <= WAIT_ACK;
				else
					NEXT_WRITE_STATE <= IDLE;
				end if;
			when WAIT_ACK =>
				if ACK_TIMEOUT = true then
					NEXT_WRITE_STATE <= WRITE_END;
				elsif BOOT_ACK_S = '1' then
					NEXT_WRITE_STATE <= FLASH_INIT;
				else
					NEXT_WRITE_STATE <= WAIT_ACK;
				end if;
			when FLASH_INIT => -- Send PROGRAM command.
				if INIT_RDY = true and FLASH_RDY = '1' then
					NEXT_WRITE_STATE <= WRITE_DATA;
				elsif FLASH_RDY = '1' then
					NEXT_WRITE_STATE <= WRITE_CMD;
				else
					NEXT_WRITE_STATE <= FLASH_INIT;
				end if;
			when WRITE_CMD =>
				NEXT_WRITE_STATE <= FLASH_INIT;
			when WRITE_DATA =>
				NEXT_WRITE_STATE <= WAIT_ACK;
			when WRITE_END =>
				if T_DELAY = true then
					NEXT_WRITE_STATE <= IDLE;
				else
					NEXT_WRITE_STATE <= WRITE_END;
				end if;
		end case;
	end process WRITE_DEC;

	READ_DEC: process(READ_STATE, BOOT_STATE, NEXT_BOOT_STATE, ACK_TIMEOUT, BOOT_ACK_S, INIT_RDY, FLASH_RDY)
	begin
		case READ_STATE is
			when IDLE =>
				if BOOT_STATE /= BOOT_RD and NEXT_BOOT_STATE = BOOT_RD then
					NEXT_READ_STATE <= FLASH_INIT;
				else
					NEXT_READ_STATE <= IDLE;
				end if;
			when FLASH_INIT => -- Send READ/RESET command.
				if INIT_RDY = true and FLASH_RDY = '1' then
					NEXT_READ_STATE <= DATA_LOAD;
				elsif FLASH_RDY = '1' then
					NEXT_READ_STATE <= WRITE_CMD;
				else
					NEXT_READ_STATE <= FLASH_INIT;
				end if;
			when WRITE_CMD =>
				NEXT_READ_STATE <= FLASH_INIT;
			when DATA_LOAD =>
				NEXT_READ_STATE <= WAIT_ACK;
			when WAIT_ACK =>
				if ACK_TIMEOUT = true then
					NEXT_READ_STATE <= IDLE;
				elsif BOOT_ACK_S = '1' then
					NEXT_READ_STATE <= DATA_LOAD;
				else
					NEXT_READ_STATE <= WAIT_ACK;
				end if;
		end case;
	end process READ_DEC;

	LED_BLNK: process
	-- During the ACTIVATE state the LED blinks with
	-- a frequency of 1Hz.
	variable TMP : std_logic_vector(23 downto 0);
	begin
		wait until CLK = '1' and CLK' event;
		case BOOT_STATE is
			when ACTIVATE | ADR_REQ | CMD_REQ | FLASH_ERASE_1 | FLASH_ERASE_2 | BOOT_WR | BOOT_RD =>
				TMP := TMP + '1';
			when others =>
				TMP := (others => '0');
		end case;
		-- 
		case BOOT_STATE is
			when ACTIVATE | ADR_REQ | CMD_REQ =>
				BOOT_LED <= To_Bit(TMP(22));
			when FLASH_ERASE_1 | FLASH_ERASE_2 =>
				BOOT_LED <= To_Bit(TMP(23));
			when BOOT_WR =>
				BOOT_LED <= To_Bit(TMP(21));
			when BOOT_RD =>
				BOOT_LED <= To_Bit(TMP(20));
			when others =>
				BOOT_LED <= '0';
		end case; 
	end process LED_BLNK;
	
	TIMER_5: process
	-- This process provides a delay of 5 seconds.
	variable TMP : std_logic_vector(25 downto 0);
	begin
		wait until CLK = '1' and CLK' event;
		TIME_5 <= false;
		if BOOT_STATE /= ACTIVATE then
			TMP := (others => '0');
		elsif TMP < "11" & x"FFFFFF" then
			TMP := TMP + '1';
		else
			TIME_5 <= true;
		end if;
	end process TIMER_5;

	P_MC_WAIT: process
	-- This process provides a delay of about 2 microseconds.
	-- During this time, the BOOT_REQ is released to insure
	-- proper communication between boot loader and MC.
	variable TMP : std_logic_vector(4 downto 0);
	begin
		wait until CLK = '1' and CLK' event;
		if (BOOT_STATE = WAIT_MC_1 or BOOT_STATE = WAIT_MC_2) and TMP < "11111" then
			TMP := TMP + '1';
			MC_WAITSTATE <= '0';
		elsif BOOT_STATE = WAIT_MC_1 or BOOT_STATE = WAIT_MC_2 then
			MC_WAITSTATE <= '1';	
		else
			TMP := "00000";
			MC_WAITSTATE <= '0';
		end if;
	end process P_MC_WAIT;

	P_TIMEOUT: process
	-- This process provides a delay of about 1 second
	-- in the WAIT_ACK states, the timeout occurs after
	-- this time. In other states the timeout timer
	-- is disabled
	variable TMP : std_logic_vector(23 downto 0);
	begin
		wait until CLK = '1' and CLK' event;
		ACK_TIMEOUT <= false;
		if NO_OF_TESTPATTERNS > x"000000" then -- Test stuff.
			if WRITE_STATE = WAIT_ACK and ADR_REG = NO_OF_TESTPATTERNS then
				ACK_TIMEOUT <= true;
			elsif READ_STATE = WAIT_ACK and ADR_REG = NO_OF_TESTPATTERNS then
				ACK_TIMEOUT <= true;
			end if;
		elsif WRITE_STATE = WAIT_ACK and TMP < x"FFFFFF" then
			TMP := TMP + '1';
		elsif READ_STATE = WAIT_ACK and TMP < x"FFFFFF" then
			TMP := TMP + '1';
		elsif WRITE_STATE = WAIT_ACK then
			ACK_TIMEOUT <= true;
		elsif READ_STATE = WAIT_ACK then
			ACK_TIMEOUT <= true;
		else
			TMP := (others => '0');
		end if;
	end process P_TIMEOUT;

	P_DELAY: process
	-- This delay improves the timing of the flash controls after
	-- a single pulse program mode and after the erase procedure.
	-- The delay is 16 clock cycles.
	variable TMP : std_logic_vector(3 downto 0);
	begin
		wait until CLK = '1' and CLK' event;
		T_DELAY <= false;
		if ERASE_STATE = READY and TMP < x"F" then
			TMP := TMP + '1';
		elsif ERASE_STATE = READY then
			T_DELAY <= true;
		elsif WRITE_STATE = WRITE_END and TMP < x"F" then
			TMP := TMP + '1';
		elsif WRITE_STATE = WRITE_END then
			T_DELAY <= true;
		else
			TMP := (others => '0');
		end if;
	end process P_DELAY;

	SEQ_CNT: process
	-- This process controls during the respective states the
	-- writing of the correct number of commands to the flash device.
	variable CNT : std_logic_vector(2 downto 0);
	begin
		wait until CLK = '1' and CLK' event;
		if BOOT_STATE = ACTIVATE or BOOT_STATE = ADR_REQ then
			CNT := "000";
		elsif WRITE_STATE = WRITE_DATA or WRITE_STATE = WRITE_END then
			CNT := "000";
		elsif ERASE_STATE = WRITE_CMD or WRITE_STATE = WRITE_CMD or READ_STATE = WRITE_CMD then
			CNT := CNT + '1';
		end if;
		--
		if BOOT_STATE = FLASH_ERASE_1 and CNT = "110" then
			INIT_RDY <= true; -- 6 command words for erasing the chip.
		elsif BOOT_STATE = FLASH_ERASE_2 and CNT = "110" then
			INIT_RDY <= true; -- 6 command words for erasing the chip.
		elsif BOOT_STATE = BOOT_WR and CNT = "011" then
			INIT_RDY <= true; -- 3 command words for writing data.
		elsif BOOT_STATE = BOOT_RD and CNT = "011" then
			INIT_RDY <= true; -- 3 command words switching to read.
		else
			INIT_RDY <= false;
		end if;
		--
		CMD_PNTR <= conv_integer(CNT);
	end process SEQ_CNT;
	
	ADR_COUNTER: process(RESET_COREn, CLK)
	variable BASE_ADR_REG : std_logic_vector(23 downto 8);
	begin
		if RESET_COREn = '0' then
			ADR_REG <= x"000000";
		elsif CLK = '1' and CLK' event then
			if BOOT_STATE = ADR_REQ and BOOT_ACK_S = '1' then
				if NO_OF_TESTPATTERNS > x"000000" then
					BASE_ADR_REG := x"0000"; -- Store the start address.
				else
					BASE_ADR_REG := To_StdLogicVector(D_SHIFTREG); -- Store the start address.
				end if;
				ADR_REG <= BASE_ADR_REG & x"00"; -- Init.
            elsif BOOT_STATE /= BOOT_WR and NEXT_BOOT_STATE = BOOT_WR then
                ADR_REG <= BASE_ADR_REG & x"00"; -- Init.
			elsif BOOT_STATE /= BOOT_RD and NEXT_BOOT_STATE = BOOT_RD then
				ADR_REG <= BASE_ADR_REG & x"00"; -- Init.
			elsif WRITE_STATE = WRITE_DATA then
				ADR_REG <= ADR_REG + '1';
			elsif READ_STATE = DATA_LOAD then
				ADR_REG <= ADR_REG + '1';
			end if;			
		end if;
	end process ADR_COUNTER;

	SHFT: process
	-- This is the SPI receiver transmitter shift
	-- register.
	variable LOCK : boolean;
	begin
		wait until CLK = '1' and CLK' event;
		if RESET_COREn = '0' then
			D_SHIFTREG <= x"55AA"; -- Initial stamp.
		elsif NO_OF_TESTPATTERNS > x"000000" then
			D_SHIFTREG <= x"5A5A";
		elsif READ_STATE = DATA_LOAD then
			D_SHIFTREG <= To_BitVector(DATA_IN); -- Load flash data.
			--D_SHIFTREG <= To_BitVector(ADR_REG(15 downto 0)); -- Test Stuff.
			--D_SHIFTREG <= x"a75b"; -- Further test stuff.
		elsif SPI_CLK_S = '0' and LOCK = false then
			D_SHIFTREG <= D_SHIFTREG(14 downto 0) & SPI_DIN_S;
			LOCK := true;
		elsif SPI_CLK_S = '1' then
			LOCK := false;
		end if;			
	end process SHFT;

	SPI_DOUT <= D_SHIFTREG(15); -- MSB first out.

	ADR_EN <= '1' when BOOT_STATE = FLASH_ERASE_1 or BOOT_STATE = FLASH_ERASE_2 or BOOT_STATE = BOOT_WR or BOOT_STATE = BOOT_RD else '0';
	ADR_OUT_I <=	x"000" & FLASH_CMDS_A(1, CMD_PNTR) when ERASE_STATE = FLASH_INIT else
					x"000" & FLASH_CMDS_A(1, CMD_PNTR) when ERASE_STATE = WRITE_CMD and INIT_RDY = false else
					x"000" & FLASH_CMDS_A(2, CMD_PNTR) when WRITE_STATE = FLASH_INIT else
					x"000" & FLASH_CMDS_A(2, CMD_PNTR) when WRITE_STATE = WRITE_CMD and INIT_RDY = false else
					x"000" & FLASH_CMDS_A(3, CMD_PNTR) when READ_STATE = FLASH_INIT else
					x"000" & FLASH_CMDS_A(3, CMD_PNTR) when READ_STATE = WRITE_CMD and INIT_RDY = false else To_BitVector(ADR_REG);

	DATA_EN <= '1' when BOOT_STATE = FLASH_ERASE_1 or BOOT_STATE = FLASH_ERASE_2 or BOOT_STATE = BOOT_WR or READ_STATE = FLASH_INIT or READ_STATE = WRITE_CMD else '0';
	DATA_OUT <= To_StdLogicVector(DATA_OUT_I);
	DATA_OUT_I <= 	x"00" & FLASH_CMDS_D(1, CMD_PNTR) when ERASE_STATE = FLASH_INIT else
					x"00" & FLASH_CMDS_D(1, CMD_PNTR) when ERASE_STATE = WRITE_CMD and INIT_RDY = false else
					x"00" & FLASH_CMDS_D(2, CMD_PNTR) when WRITE_STATE = FLASH_INIT else
					x"00" & FLASH_CMDS_D(2, CMD_PNTR) when WRITE_STATE = WRITE_CMD and INIT_RDY = false else
					x"00" & FLASH_CMDS_D(3, CMD_PNTR) when READ_STATE = FLASH_INIT else
					x"00" & FLASH_CMDS_D(3, CMD_PNTR) when READ_STATE = WRITE_CMD and INIT_RDY = false else D_SHIFTREG;
	
	ADR_OUT <= To_StdLogicVector(ADR_OUT_I);

	BOOT_REQ <= '1' when BOOT_STATE = ADR_REQ else
				'1' when BOOT_STATE = CMD_REQ else
				'1' when WRITE_STATE = WAIT_ACK else
				'1' when READ_STATE = WAIT_ACK else '0';

	RESET_OUTn <= '0' when BOOT_STATE = ADR_REQ or BOOT_STATE = CMD_REQ or BOOT_STATE = FLASH_ERASE_1 or BOOT_STATE = FLASH_ERASE_2 else
				  '0' when BOOT_STATE = BOOT_WR or BOOT_STATE = BOOT_RD else
				  '0' when BOOT_STATE = WAIT_MC_1 or BOOT_STATE = WAIT_MC_2 else '1'; -- System reset during boot sequence.

    FLASH_RESETn <= '0' when BOOT_STATE = ADR_REQ or BOOT_STATE = WAIT_MC_1 else '1';

	FLASH_WEn <= '0' when ERASE_STATE = WRITE_CMD else
				 '0' when WRITE_STATE = WRITE_CMD else
				 '0' when WRITE_STATE = WRITE_DATA else
				 '0' when READ_STATE = WRITE_CMD else '1'; -- Do never write in normal operation.

	FLASH_OEn <= ROM_CEn when BOOT_STATE = IDLE else -- Only Read enabled in normal operation.
				 '0' when READ_STATE = WAIT_ACK else
				 '0' when READ_STATE = DATA_LOAD else '1';

	FLASH_CEn <= ROM_CEn when BOOT_STATE = IDLE else
				 '0' when BOOT_STATE = FLASH_ERASE_1 else
				 '0' when BOOT_STATE = FLASH_ERASE_2 else
				 '0' when BOOT_STATE = BOOT_WR else
				 '0' when BOOT_STATE = BOOT_RD else '1';
end architecture BEHAVIOR;
