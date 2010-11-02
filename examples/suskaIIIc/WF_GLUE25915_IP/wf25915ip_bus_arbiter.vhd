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
---- Direct memory access (DMA) control state machine.            ----
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
---- Copyright (C) 2005 Wolfgang Foerster                         ----
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
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25915IP_BUS_ARBITER is
port (  RESn	: in bit; -- ST's reset signal.
		CLK		: in bit; -- the ST's 8MHz clock.

		-- D8 is used for the DMA mode register.
		D8		: in std_logic; -- Data bus bit 8.
		
		DTACKn	: in bit; -- DTACKn gives information of the MMU state.
		DE		: in bit; -- Sync signal for the slice counter.		

		-- ASn, LDSn, UDSn and RWn are asserted by the arbiter during 
		-- DMA transfer.
		AS_INn			: in bit;
		RWn_IN			: in bit;
		AS_OUTn			: out bit;
		RWn_OUT			: out bit;
		LDS_OUTn		: out bit;
		UDS_OUTn		: out bit;
		CTRL_EN			: out bit;

		-- Chip select signal for the DMA mode register. This signal
		-- is generated in the adress decoder.
	    DMA_MODE_CSn	: in bit;

		-- RDYn is asserted by the arbiter during DMA transfer.
		-- The other signals are used for DMA transfer control.
		RDY_INn		: in bit; -- DMA unit ready signal.
		RDY_OUTn	: out bit; -- DMA unit ready signal.
		RDY_EN		: out bit; -- DMA unit ready signal enable.
		BGACK_INn	: in bit; -- Bus grant acknowledge input.
		BGACK_OUTn	: out bit; -- Bus grant acknowledge output.
		BRn			: out bit; -- Bus request (open drain).
		BGIn		: in bit; -- Bus grant input.
		BGOn		: out bit; -- Bus grant output.
		DMAn		: out bit -- DMA select signal for the MMU.
      );
end WF25915IP_BUS_ARBITER;

architecture BEHAVIOR of WF25915IP_BUS_ARBITER is
type DMA_PHASES is (IDLE, START, SAMPLE, RUN);
type SLICE_SYNC_TYPE is (STOP, WAIT_D1, SYNC, RUN);
signal DMA_PHASE		: DMA_PHASES;
signal DMA_MODE			: bit; -- One bit register.
signal BR_In			: bit;
signal DMA_STARTFLAG	: boolean;
signal SLICE_CNT		: std_logic_vector(1 downto 0);
signal SLICE_NUMBER		: integer range 1 to 8;
signal SYNC_PHASE		: SLICE_SYNC_TYPE;
signal NEXT_SYNC_PHASE	: SLICE_SYNC_TYPE;
signal SLICECNT_SYNC	: boolean;
begin
	BRn <= BR_In;
	BGOn <= '0' when BGIn = '0' and BR_In = '1' else '1';
	BGACK_OUTn <= '1' when DMA_PHASE = IDLE else '0';

	DMAn <= '0' when DMA_PHASE = SAMPLE or DMA_PHASE = RUN else '1';

	DMA_MODE_REG: process(RESn, CLK)
	-- This is the DMA mode register in the GLUE. it is a mirror of the 
	-- original DMA mode register (the original is 8 bit wide), located
	-- in the DMA unit. This register stores the information about the
	-- DMA data transfer direction. The STs adress of this register is
	-- FF8606. The register select signal DMA_MODE_CSn is decoded in the 
	-- adress decoder.
	begin
		if RESn = '0' then
			DMA_MODE <= '0'; -- Default is read from ACSI / Floppy.
		elsif CLK = '1' and CLK' event then
			if DMA_MODE_CSn = '0' and RWn_IN = '0' then
				DMA_MODE <= To_Bit(D8);
			end if;
		end if;
	end process DMA_MODE_REG;

	BRn_LOGIC: process(CLK, RESn)
	variable RDY_COUNT: std_logic_vector(1 downto 0);
	begin
		if RESn = '0' then
			BR_In <= '1';
		elsif CLK = '1' and CLK' event then
			if RDY_INn = '1' and RDY_COUNT /= "11" then
				RDY_COUNT := RDY_COUNT + '1'; -- RDYn condition detected.
			elsif RDY_INn = '0' then -- Restart the counter.
				RDY_COUNT := "00";
			end if;
			case RDY_COUNT is
				when "11" => BR_In <= '0'; -- DMA condition detected.
				when others => BR_In <= '1';
			end case;
		end if;
	end process BRn_LOGIC;

	START_CONDITION: process
	-- To prevent the DMA_CTRL state machine of unpredictable behavior,
	-- due to the poor rising edges of DTACKn and ASn the start condition 
	-- is done here with one flip flop (D type).
	begin
		wait until CLK = '1' and CLK' event;
		if BR_In = '0' and BGIn = '0' and BGACK_INn = '1' and AS_INn = '1' and DTACKn = '1' then
			if DMA_MODE = '0' and SLICE_NUMBER =  8 then -- Write to targets.
				DMA_STARTFLAG <= true;
			elsif DMA_MODE = '1' and SLICE_NUMBER = 4 then -- Read from targets.
				DMA_STARTFLAG <= true;
			else
				DMA_STARTFLAG <= false;
			end if;
		else
			DMA_STARTFLAG <= false;
		end if;
	end process START_CONDITION;

	DMA_CTRL: process(CLK, RESn, DMA_PHASE, DTACKn)
	-- State machine for DMA sequence detection. During the DMA sequence, the
	-- signals LDSn, UDSn, ASn, RDYn and RWn are controlled via this arbiter.
	-- 'ON' condition: the machine starts if there is a bus request from this
	-- arbiter, the bus grant input is asserted by the CPU (BGIn = '0'), the 
	-- bus request came from the GLUE (BR_In = '0'), the bus is free(ASn and
	-- DTACKn are not asserted) and there is no bus access by other devices
	-- (BGACKn = '1').
	-- 'OFF' condition: the machine stops, if the RDYn is asserted by the DMA
	-- chip during the ASn is controlled high by this arbiter.
	-- This process must work on the positive clock edge to work correct in
	-- conjunction with START_CONDITION and SLICECNT working on the respective 
	-- clock edges!
	begin
		if RESn = '0' then
			DMA_PHASE <= IDLE;
		elsif CLK = '1' and CLK' event then
			case DMA_PHASE is
				when IDLE =>
					if DMA_STARTFLAG = true then
						DMA_PHASE <= START;
					else
						DMA_PHASE <= IDLE;
					end if;
				when START =>
					DMA_PHASE <= SAMPLE; -- Setup default values for the controls.
				when SAMPLE =>
					if DTACKn = '0' then
						DMA_PHASE <= RUN;
					else
						DMA_PHASE <= SAMPLE;
					end if;
				when RUN =>
					if AS_INn = '1' and RDY_INn = '0' then
						DMA_PHASE <= IDLE;
					else
						DMA_PHASE <= RUN;
					end if;
			end case;
		end if;
	end process DMA_CTRL;

	SLICES_SYNC_REG: process(RESn, CLK)
	begin
		if RESn = '0' then
			SYNC_PHASE <= STOP;
		elsif CLK = '1' and CLK' event then
			SYNC_PHASE <= NEXT_SYNC_PHASE;
		end if;
	end process SLICES_SYNC_REG;

	SLICES_SYNC_DEC: process(SYNC_PHASE, DE)
	-- This process is necessary to synchronize the time
	-- slice counter with the MCU's time slice counter.
	-- During DMA access it provides exact DMA start timing
	-- to achieve proper DMA operation.
	begin
		case SYNC_PHASE is
			when STOP =>
				if DE = '0' then
					NEXT_SYNC_PHASE <= WAIT_D1;
				else
					NEXT_SYNC_PHASE <= STOP;
				end if;
				SLICECNT_SYNC <= false;
			when WAIT_D1 =>
				if DE = '1' then
					NEXT_SYNC_PHASE <= SYNC;
				else
					NEXT_SYNC_PHASE <= WAIT_D1;
				end if;
				SLICECNT_SYNC <= false;
			when SYNC =>
				NEXT_SYNC_PHASE <= RUN;
				SLICECNT_SYNC <= true;	-- Syncing.
			when RUN =>
				NEXT_SYNC_PHASE <= RUN; -- Run infinite.
				SLICECNT_SYNC <= false;
		end case;
	end process SLICES_SYNC_DEC;

	TIME_SLICES: process
	begin
		wait until CLK = '1' and CLK' event;
		if SLICECNT_SYNC = true then
			SLICE_CNT <= "10"; 	-- Resync.
		else
			SLICE_CNT <= SLICE_CNT + '1';
		end if;
	end process TIME_SLICES;

	-- Although all choices are covered, there is a default assignment not
	-- to let the compiler create latches.
	SLICE_NUMBER <= 1 when SLICE_CNT = "00" and CLK = '1' else
				    2 when SLICE_CNT = "00" and CLK = '0' else
				    3 when SLICE_CNT = "01" and CLK = '1' else
				    4 when SLICE_CNT = "01" and CLK = '0' else
				    5 when SLICE_CNT = "10" and CLK = '1' else
				    6 when SLICE_CNT = "10" and CLK = '0' else
				    7 when SLICE_CNT = "11" and CLK = '1' else
				    8 when SLICE_CNT = "11" and CLK = '0' else 1;

	RDY_EN <= 	'0' when DMA_PHASE = IDLE else 
				'0' when DMA_PHASE = RUN and SLICE_NUMBER = 4 else
				'0' when DMA_PHASE = RUN and SLICE_NUMBER = 5 else
				'0' when DMA_PHASE = RUN and SLICE_NUMBER = 6 else '1';

	RDY_OUTn <= '1' when DMA_PHASE = START else
				'0' when DMA_PHASE = SAMPLE else '0';
				-- The 'Z' states are controlled via RDY_EN.
				--'Z' when DMA_PHASE = RUN and  SLICE_NUMBER = 4 else
				--'Z' when DMA_PHASE = RUN and  SLICE_NUMBER = 5 else
				--'Z' when DMA_PHASE = RUN and  SLICE_NUMBER = 6 else '0';

	CTRL_EN <= 	'1' when DMA_PHASE /= IDLE else '0';
				
	AS_OUTn <= 	'1' when DMA_PHASE = START else
				'0' when DMA_PHASE = SAMPLE else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 4 else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 5 else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 6 else '0';

	UDS_OUTn <= '1' when DMA_PHASE = START else
				'0' when DMA_PHASE = SAMPLE else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 4 else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 5 else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 6 else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 7  and RWn_IN = '0' else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 8  and RWn_IN = '0' else '0';

	LDS_OUTn <= '1' when DMA_PHASE = START else
				'0' when DMA_PHASE = SAMPLE else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 4 else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 5 else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 6 else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 7  and RWn_IN = '0' else
				'1' when DMA_PHASE = RUN and  SLICE_NUMBER = 8  and RWn_IN = '0' else '0';

	RWn_OUT <= 	'0' when DMA_PHASE /= IDLE and DMA_MODE = '0' else
				'1' when DMA_PHASE /= IDLE and DMA_MODE = '1' else '1';
end BEHAVIOR;
