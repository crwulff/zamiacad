----------------------------------------------------------------------
----                                                              ----
---- ATARI DMA compatible IP Core					              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- ATARI ST and STE compatible DMA controller IP core.          ----
----                                                              ----
---- This file contains the complete DMA control state machine    ----
---- which handles all DMA internal control signals for the FIFO, ----
---- the registers and also for the port control signals.         ----
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
-- Revision 2K8A  2008/07/14 WF
-- Revision 2K8B  2008/12/24 WF
--   Introduced DMA_SRC_SEL as a bit vector.
--   Further (minor) changes.
-- Revision 2K9B  2009/06/20 WF
--   Process P_DATA_ENA has now synchronous reset to meet preset requirements.
--   Process FIFO_RD_CTRL has now synchronous reset to meet preset requirements.
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25913IP_CTRL is
	port (
		CLK				: in bit;
		RESETn			: in bit;

		FCSn			: in bit;
		RDY_INn			: in std_logic;
		DMA_RD_EN		: in bit;
		DMA_WR_EN		: in bit;

		CTRL_ACC		: in bit;

		DMA_RWn			: in bit;
		DMA_SRC_SEL		: in bit_vector(1 downto 0);
		HDRQ			: in bit;
		FDCRQ			: in bit;
		
		FIFO_FULL		: in bit;
		FIFO_HI			: in bit;
		FIFO_LOW		: in bit;
		FIFO_EMPTY		: in bit;

		CLRn			: buffer bit;

		FIFO_RD_ENA		: out bit;
		FIFO_WR_ENA		: out bit;

		DATA_EN			: out bit;
		
		CD_HIBUF_EN		: out bit;
		CD_RD_HIn		: out bit;
		CD_RD_LOWn		: out bit;

		ACSI_DATA_REQ	: out bit;
		SECTOR_CNT_EN	: out bit;

		FDCS_DMA_ACCn	: out bit;		
		HD_ACKn			: out bit;
		RDY_OUTn		: out bit
	);
end entity WF25913IP_CTRL;

architecture BEHAVIOR of WF25913IP_CTRL is
type DMA_PHASES is (IDLE, READ, WRITE);
type ACSI_STATES is (IDLE_BYTE1, IDLE_BYTE2, IDLE_WR_HI, IDLE_WR_LOW, IDLE_RD_HI,
					 IDLE_RD_LOW, WRITE_HI, WRITE_LOW, READ_HI, READ_LOW);
signal DMA_PHASE		: DMA_PHASES;
signal ACSI_STATE		: ACSI_STATES;
signal ACSI_NEXT_STATE	: ACSI_STATES;
signal HDRQ_I			: bit;
signal DATAREQ			: bit; -- Data request from the ACSI bus or Floppy disk.
signal FIFO_ACSI_RD		: bit;
signal FIFO_ACSI_WR		: bit;
signal FIFO_SYS_RD		: bit;
signal FIFO_SYS_WR		: bit;
signal FIFO_WR_CTRL		: bit;
signal WORDCNT_EN		: bit;
signal PKG_READY		: boolean;
begin
	P_HDRQ: process
	-- The ACSI devices work in their own clock domain. Therefore it is
	-- necessary to synchronize the incoming handshake signal to the DMA
	-- controller's clock domain. Otherwise there will result unpredictable
	-- behavior. It is recommended to use the falling clock edge for this
	-- process.
	begin
	wait until CLK = '0' and CLK' event;
		HDRQ_I <= HDRQ;
	end process P_HDRQ;

	DATAREQ <= FDCRQ when DMA_SRC_SEL = "10" else HDRQ_I;
	ACSI_DATA_REQ <= DATAREQ;

	with DMA_RWn select
		FIFO_RD_ENA <= 	FIFO_ACSI_RD when '0', -- Write to target.
						FIFO_SYS_RD when '1'; -- Read from target.
	with DMA_RWn select
		FIFO_WR_ENA <= 	FIFO_SYS_WR when '0', -- Write to target.
						FIFO_ACSI_WR when '1'; -- Read from target.

	CLEAR_DETECT: process(CLK, RESETn)
	-- This process detects any toggling of the DMA_RWn signal
	-- and releases a FIFO clear.
	variable LOCK	: boolean;
	begin
		-- Positive or negative edge detector.
		if RESETn = '0' then
			CLRn <= '1';
		elsif CLK = '1' and CLK' event then
			if DMA_RWn = '0' and LOCK = false then
				LOCK := true;
				CLRn <= '0';
			elsif DMA_RWn = '1' and LOCK = true then
				LOCK := false;
				CLRn <= '0';
			else
				CLRn <= '1';
			end if;
		end if;
	end process CLEAR_DETECT;

	PACKAGE_CNT: process (CLK)
	-- If there are more than 16 bytes (8 words) during a read process
	-- in the FIFO, the DMA is started. During this time the FIFO is read and written
	-- the same time. This has an unpredictable behavior on how many bytes are in the
	-- FIFO at the end of a sector. Therefore the DMA read process is controlled by
	-- PKG_READY so that at any DMA a portion of 16 bytes are read. This has the effect,
	-- that the 512 bytes boundary is met and the last byte package of the FIFO is
	-- correctly transfered by the DMA.
	variable CNT : std_logic_vector(3 downto 0);
	begin
		if CLK = '1' and CLK' event then
			if DMA_PHASE = IDLE then
				CNT := "0000";
			elsif FIFO_SYS_RD = '1' then
				CNT := CNT + 1;
			end if;
			case CNT is
				when "1000" => PKG_READY <= true;
				when others => PKG_READY <= false;
			end case;
		end if;
	end process PACKAGE_CNT;

	P_DMA_STATE: process(RESETn, CLK)
	begin
		if RESETn = '0' then -- DMA initialisation.
			DMA_PHASE <= IDLE; -- Initial IDLE condition.
		elsif CLK = '1' and CLK' event then
			case DMA_PHASE is
				when IDLE =>
					-- Start in read from disk mode after the FIFO is half filled.
					if DMA_RWn = '1' and FIFO_HI = '1' and DMA_RD_EN = '1' then
						DMA_PHASE <= READ; -- For read from target.
					-- Start in write to disk mode if the FIFO is less than half full.
					elsif DMA_RWn = '0' and FIFO_LOW = '1' and DMA_WR_EN = '1' then
						DMA_PHASE <= WRITE; -- For write to target.
					else
						DMA_PHASE <= IDLE;
					end if;
				when READ =>
					-- RDYn locking necessary because the data must be valid
					-- for a short time after FIFO_EMPTY.
					if PKG_READY = true and RDY_INn = '1' then
						DMA_PHASE <= IDLE;
					else
						DMA_PHASE <= READ;
					end if;
				when WRITE =>
					-- Interrupt by FIFO full.
					if FIFO_FULL = '1' or DMA_WR_EN = '0' then
						DMA_PHASE <= IDLE;
					else
						DMA_PHASE <= WRITE;
					end if;
			end case;
		end if;
	end process P_DMA_STATE;				

	P_RDYOUT: process(CLK, RESETn, DMA_PHASE, FIFO_FULL, CTRL_ACC)
	-- The RDYn signal has two different functions. On the one hand, in non DMA mode it is the data
	-- acknowledge signal for the DMA register access. On the other hand, in DMA mode, it controls the
	-- DMA machine and interrupts the transfer when necessary.
	-- The timing of the RDY_OUTn must be correlated with the CTRL_MASK timing in the register file!
	variable TMP : std_logic_vector(1 downto 0);
	begin
		if RESETn = '0' then
			TMP := "00";
		elsif CLK = '1' and CLK' event then
			if FCSn = '0' then
				if TMP < "11" then
					TMP := TMP + '1';
				end if;
			else
				TMP := "00";
			end if;
		end if;
		--
		if DMA_PHASE = WRITE and FIFO_FULL = '1' then
			RDY_OUTn <= '0';
		elsif DMA_PHASE = IDLE and TMP < "01" and CTRL_ACC = '0' then -- DMA register access timing.
			RDY_OUTn <= '0'; -- Active hi.
		elsif DMA_PHASE = IDLE and TMP < "11" and CTRL_ACC = '1' then -- Controller access timing.
			RDY_OUTn <= '0'; -- Active hi.
		else
			RDY_OUTn <= '1';
		end if;
	end process P_RDYOUT;

	-- Counting on the read cycles guarantees the correct end of operation time if 
	-- there are slow peripheral components connected to the ASCI bus.
	WORDCNT_EN <= 	'1' when FIFO_SYS_RD = '1' else -- SYS read.
					'1' when ACSI_STATE = IDLE_WR_LOW and DATAREQ = '0' else '0'; -- End of ACSI read.

	WORD_CNT: process (CLRn, CLK)
	-- This process counts the transferred double-bytes. The counter
	-- releases the SECTOR_CNT_EN when it counts 256 words (512 bytes).
	variable WORDCNT : std_logic_vector (8 downto 0);
	begin
		if CLRn = '0' then -- During DMA initialisation ...
			WORDCNT := (others => '0');
		elsif CLK = '1' and CLK' event then
			if WORDCNT_EN = '1' and WORDCNT < "100000000" then
				WORDCNT := WORDCNT + 1;
			elsif WORDCNT = "100000000" then
				WORDCNT := (others => '0');
			end if;
		end if;
		case WORDCNT is
			when "100000000" => SECTOR_CNT_EN <= '1';
			when others => SECTOR_CNT_EN <= '0';
		end case;
	end process WORD_CNT;

	P_DATA_ENA: process
	-- This process provides control of the data multiplexer during read from disk mode.
	-- The data is enabled right after the first falling edge of RDYn and stays in this
	-- condition until the READ state finishes.
	-- The process works on the negative clock edge.
	variable LOCK : boolean;
	begin
		wait until CLK = '0' and CLK' event;
		if RESETn = '0' then 
			DATA_EN <= '0';
			LOCK := true;
		elsif DMA_PHASE = READ and RDY_INn = '1' then
			LOCK := false;
		elsif DMA_PHASE = READ and RDY_INn = '0' and LOCK = false then
			DATA_EN <= '1';
		elsif DMA_PHASE = IDLE then
			DATA_EN <= '0';
			LOCK := true;
		end if;
	end process P_DATA_ENA;

	FIFO_RD_CTRL: process
	-- To achieve correct read timing, this process must operate on the
	-- falling clock edge!
	variable LOCK : boolean;
	begin
		wait until CLK = '0' and CLK' event;
		if RESETn = '0' then 
			FIFO_SYS_RD <= '0';
			LOCK := true;
		elsif DMA_PHASE = READ and RDY_INn = '1' then
			LOCK := false;
		elsif DMA_PHASE = READ and RDY_INn = '0' and LOCK = false then
			FIFO_SYS_RD <= '1'; -- Bring new data right after the fallinge edge of RDYn.
			LOCK := true;
		else 
			FIFO_SYS_RD <= '0';
			LOCK := true;
		end if;
	end process FIFO_RD_CTRL;

	P_FIFO_WR_CTRL: process(RESETn, CLK)
	-- SYS_STATE_OUTLOGIC: ... is responsible to control the system side write process (memory via DMA to
	-- peripheral components). To achieve correct read timing, this process must operate on the rising 
	-- clock edge!
	variable STARTLOCK : boolean;
	begin
		if RESETn = '0' then 
			FIFO_WR_CTRL <= '0';
		elsif CLK = '1'and CLK' event then
			-- The DMA begins with RDYn = '1'. But the data is valid for the first time
			-- after the first RDYn GAP. Therefore the first FIFO_WR_CTRL is delayed 
			-- using the STARTLOCK.
			if DMA_PHASE = WRITE and RDY_INn = '0' and STARTLOCK = false then
				STARTLOCK := true;
			elsif DMA_PHASE = WRITE and STARTLOCK = true then
				if RDY_INn = '1' then
					FIFO_WR_CTRL <= '0';
				else
					FIFO_WR_CTRL <= '1';
				end if;
			elsif DMA_PHASE = IDLE or DMA_PHASE = READ then
				FIFO_WR_CTRL <= '0';
				STARTLOCK := false;
			end if;
		end if;
	end process P_FIFO_WR_CTRL;

	FIFO_SYS_WR <= '1' when RDY_INn = '1' and FIFO_WR_CTRL = '1' else '0';

	ACSI_STATE_MEM: process(RESETn, CLK)
	-- State machine register of the ACSI side state machine.
	begin
		if RESETn = '0' then -- DMA initialisation.
			ACSI_STATE <= IDLE_BYTE1;
		elsif CLK = '1' and CLK' event then
			-- Normally there is no need for clearing the ASCI state machine. But in case of
			-- a bad DATAREQ the machine can hang. The CLRn does initialize it every time the
			-- FIFO is cleared.
			if CLRn = '0' then
				ACSI_STATE <= IDLE_BYTE1;
			else
						ACSI_STATE <= ACSI_NEXT_STATE;
			end if;
		end if;
	end process ACSI_STATE_MEM;
	
	ACSI_STATE_LOGIC: process(ACSI_STATE, DMA_RWn, FIFO_FULL, FIFO_EMPTY, DATAREQ, DMA_PHASE, CLRn)
	begin
		case ACSI_STATE is
		-------------------------------------
		-- Section wait for start conditions:
		-------------------------------------
			-- The ACSI bus is 8 bit wide where the FIFO is 16 bit. Therefore two read
			-- or write cycles are at least possible. This is the reason for the
			-- FIFO_EMPTY and FIFO_FULL regarded only during IDLE_BYTE1.
			when IDLE_BYTE1 =>
				-- Transfer data from FIFO to target if FIFO is not empty.
				if DMA_RWn = '0' and FIFO_EMPTY = '0' and DATAREQ = '1' then
					ACSI_NEXT_STATE <= WRITE_HI;
				-- Transfer data from target to FIFO if it is not full.
				elsif DMA_RWn = '1' and FIFO_FULL = '0' and DATAREQ = '1' then
					ACSI_NEXT_STATE <= READ_HI;
				else
					ACSI_NEXT_STATE <= IDLE_BYTE1;
				end if;
			when IDLE_BYTE2 =>
				if DMA_RWn = '0' and DATAREQ = '1' then
					ACSI_NEXT_STATE <= WRITE_LOW;
				elsif DMA_RWn = '1' and DATAREQ = '1' then
					ACSI_NEXT_STATE <= READ_LOW;
				else
					ACSI_NEXT_STATE <= IDLE_BYTE2;
				end if;
		--------------------------------
		-- Section write data to target:
		--------------------------------
			when WRITE_HI =>
				ACSI_NEXT_STATE <= IDLE_WR_HI;
			when IDLE_WR_HI =>
 				if DATAREQ = '0' then		
					ACSI_NEXT_STATE <= IDLE_BYTE2;
				else
					ACSI_NEXT_STATE <= IDLE_WR_HI;
				end if;
			when WRITE_LOW =>
				ACSI_NEXT_STATE <= IDLE_WR_LOW;
			when IDLE_WR_LOW =>
 				if DATAREQ = '0' then
					ACSI_NEXT_STATE <= IDLE_BYTE1;
				else
					ACSI_NEXT_STATE <= IDLE_WR_LOW;
				end if;
		---------------------------------
		-- Section read data from target:
		---------------------------------
			when READ_HI =>
				ACSI_NEXT_STATE <= IDLE_RD_HI;
			when IDLE_RD_HI =>
				if DATAREQ = '0' then		
					ACSI_NEXT_STATE <= IDLE_BYTE2;
				else
					ACSI_NEXT_STATE <= IDLE_RD_HI;
				end if;
			when READ_LOW =>
				ACSI_NEXT_STATE <= IDLE_RD_LOW;
			when IDLE_RD_LOW =>
				if DATAREQ = '0' then		
					ACSI_NEXT_STATE <= IDLE_BYTE1;
 				else
					ACSI_NEXT_STATE <= IDLE_RD_LOW;
				end if;
		end case;
	end process ACSI_STATE_LOGIC;

	-- ACSI_STATE_OUTLOGIC:
	FDCS_DMA_ACCn <= '0' when ACSI_STATE = IDLE_WR_HI and DMA_SRC_SEL = "10" else
					 '0' when ACSI_STATE = IDLE_WR_LOW and DMA_SRC_SEL = "10" else
					  -- The data of the floppy disk controller is switched to the ACSI
					  -- bus during FDCS_DMAn = '0'. The data is transfered in the ACSI
					  -- states READ_LOW and READ_HI.
					 '0' when ACSI_STATE = READ_HI and DMA_SRC_SEL = "10" else
					 '0' when ACSI_STATE = READ_LOW and DMA_SRC_SEL = "10" else '1';
	HD_ACKn 	  <= '0' when ACSI_STATE = IDLE_WR_HI and DMA_SRC_SEL /= "10" else
					 '0' when ACSI_STATE = IDLE_WR_LOW and DMA_SRC_SEL /= "10" else
					 '0' when ACSI_STATE = IDLE_RD_HI and DMA_SRC_SEL /= "10" else
					 '0' when ACSI_STATE = IDLE_RD_LOW and DMA_SRC_SEL /= "10" else '1';

	-- Read from target:
	CD_HIBUF_EN <= '1' when ACSI_STATE = READ_HI else '0'; -- Sample.
	FIFO_ACSI_WR <= '1' when ACSI_STATE = READ_LOW else '0';

	-- Write to target:
	FIFO_ACSI_RD <= '1' when ACSI_STATE = WRITE_HI else '0';
	CD_RD_HIn 	<= 	'0' when ACSI_STATE = IDLE_WR_HI else '1';
	CD_RD_LOWn 	<= 	'0' when ACSI_STATE = IDLE_WR_LOW else '1';
end architecture BEHAVIOR;
