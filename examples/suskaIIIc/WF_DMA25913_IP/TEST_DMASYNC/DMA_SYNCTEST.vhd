library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity DMA_SYNCTEST is
port (  RESETn	: in bit;
		CLK_16M	: in bit;

		CLK_8M	: out bit;
		DE		: in bit;

		GLUE_SLICE_CNT	: buffer std_logic_vector(1 downto 0);
		MCU_SLICE_CNT	: buffer std_logic_vector(2 downto 0)
      );
end DMA_SYNCTEST;

architecture BEHAVIOR of DMA_SYNCTEST is
type SLICE_SYNC_TYPE is (STOP, WAIT_D1, SYNC, RUN);
signal GLUE_SYNC_PHASE			: SLICE_SYNC_TYPE;
signal GLUE_NEXT_SYNC_PHASE		: SLICE_SYNC_TYPE;
signal GLUE_SLICECNT_SYNC		: boolean;
signal MCU_SYNC_PHASE			: SLICE_SYNC_TYPE;
signal MCU_NEXT_SYNC_PHASE		: SLICE_SYNC_TYPE;
signal MCU_SLICECNT_SYNC		: boolean;
signal CLK_8M_I					: bit;
signal CLKTMP					: std_logic_vector(0 downto 0);
begin
	P1: process (CLK_16M)
    begin
      	if CLK_16M = '1' and CLK_16M' event then
        	CLKTMP <= CLKTMP + '1';
      	end if;
	end process P1;
	CLK_8M_I <= To_Bit(not CLKTMP(0));
	CLK_8M <= CLK_8M_I;

	GLUE_SLICES_SYNC_REG: process(RESETn, CLK_8M_I)
	begin
		if RESETn = '0' then
			GLUE_SYNC_PHASE <= STOP;
		elsif CLK_8M_I = '1' and CLK_8M_I' event then
			GLUE_SYNC_PHASE <= GLUE_NEXT_SYNC_PHASE;
		end if;
	end process GLUE_SLICES_SYNC_REG;

	GLUE_SLICES_SYNC_DEC: process(GLUE_SYNC_PHASE, DE)
	-- This process is necessary to synchronize the time
	-- slice counter with the MCU's time slice counter.
	-- During DMA access it provides exact DMA start timing
	-- to achieve proper DMA operation.
	begin
		case GLUE_SYNC_PHASE is
			when STOP =>
				if DE = '0' then
					GLUE_NEXT_SYNC_PHASE <= WAIT_D1;
				else
					GLUE_NEXT_SYNC_PHASE <= STOP;
				end if;
				GLUE_SLICECNT_SYNC <= false;
			when WAIT_D1 =>
				if DE = '1' then
					GLUE_NEXT_SYNC_PHASE <= SYNC;
				else
					GLUE_NEXT_SYNC_PHASE <= WAIT_D1;
				end if;
				GLUE_SLICECNT_SYNC <= false;
			when SYNC =>
				GLUE_NEXT_SYNC_PHASE <= RUN;
				GLUE_SLICECNT_SYNC <= true;	-- Syncing.
			when RUN =>
				GLUE_NEXT_SYNC_PHASE <= RUN; -- Run infinite.
				GLUE_SLICECNT_SYNC <= false;
		end case;
	end process GLUE_SLICES_SYNC_DEC;

	GLUE_TIME_SLICES: process(RESETn, CLK_8M_I)
	begin
		if RESETn = '0' then
			GLUE_SLICE_CNT <= "10";
		elsif CLK_8M_I = '1' and CLK_8M_I' event then
			if GLUE_SLICECNT_SYNC = true then
				GLUE_SLICE_CNT <= "10"; 	-- Resync.
			else
				GLUE_SLICE_CNT <= GLUE_SLICE_CNT + '1';
			end if;
		end if;
	end process GLUE_TIME_SLICES;

	MCU_SLICES_SYNC_MEM: process(RESETn, CLK_16M)
	begin
		if RESETn = '0' then
			MCU_SYNC_PHASE <= STOP;
		elsif CLK_16M = '1' and CLK_16M' event then
			MCU_SYNC_PHASE <= MCU_NEXT_SYNC_PHASE;
		end if;
	end process MCU_SLICES_SYNC_MEM;
	
	MCU_SLICES_SYNC_CTRL: process(MCU_SYNC_PHASE, DE, CLK_8M_I)
	-- This process is necessary to synchronize the time
	-- slice counter with the video DE frequency and the
	-- GLUE's DMA time slice counter.
	-- In case of video it provides exact video data timing
	-- between RAM and SHIFTER to achieve video output 
	-- without erroneous lateral shift on the screen.
	-- During DMA access it provides exact DMA start timing
	-- to achieve proper DMA operation. Due to the slowly
	-- clocked GLUE and DMA unit, it is necessary, that this
	-- process reacts on the 8 MHz clock cycle of the GLUE.
	begin
		case MCU_SYNC_PHASE is
			when STOP =>
				if CLK_8M_I = '0' and DE = '0' then
					MCU_NEXT_SYNC_PHASE <= WAIT_D1;
				else
					MCU_NEXT_SYNC_PHASE <= STOP;
				end if;
				MCU_SLICECNT_SYNC <= false;
			when WAIT_D1 =>
				if CLK_8M_I = '0' and DE = '1' then
					MCU_NEXT_SYNC_PHASE <= SYNC;
				else
					MCU_NEXT_SYNC_PHASE <= WAIT_D1;
				end if;
				MCU_SLICECNT_SYNC <= false;
			when SYNC =>
				if CLK_8M_I = '0' then
					MCU_NEXT_SYNC_PHASE <= RUN;
				else
					MCU_NEXT_SYNC_PHASE <= SYNC;
				end if;
				MCU_SLICECNT_SYNC <= true;	-- Syncing.
			when RUN =>
				MCU_NEXT_SYNC_PHASE <= RUN; -- Run infinite.
				MCU_SLICECNT_SYNC <= false;
		end case;
	end process MCU_SLICES_SYNC_CTRL;
	
	MCU_TIME_SLICES: process
	-- The process counts 8 states like the 68000 bus states.
	-- This counter may not have a reset control because it has to
	-- Produce refresh timing during reset.
	begin
		wait until CLK_16M = '1' and CLK_16M' event;
		if MCU_SLICECNT_SYNC = true then
			MCU_SLICE_CNT <= "100"; 	-- Resync.
		else
			MCU_SLICE_CNT <= MCU_SLICE_CNT + '1';
		end if;
	end process MCU_TIME_SLICES;

end architecture BEHAVIOR;



