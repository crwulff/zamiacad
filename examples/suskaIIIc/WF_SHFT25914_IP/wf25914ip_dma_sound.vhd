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
---- DMA sound module. it is an ST enhancement of the STE.        ----
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
--   Minor changes.
-- Revision 2K9A  2009/06/20 WF
--   GRP_A and GRP_B have now synchronous reset to meet preset requirement.
--   AUDIO_LATCH_L and AUDIO_LATCH_R have now synchronous reset to meet preset requirement.
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25914IP_DMASOUND is
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
		
		SCLK		: in bit; -- 6.4 MHz.
		FCLK		: out bit;

		SDATA_L		: out bit_vector(7 downto 0); -- Buffers implemented here.
		SDATA_R		: out bit_vector(7 downto 0) -- Buffers implemented here.
		);
end entity WF25914IP_DMASOUND;		

architecture BEHAVIOR of WF25914IP_DMASOUND is
type GROUP_TYPE is (FULL, EMPTY);
type FIFO_STATUS_TYPE is (FULL, MID, EMPTY);
type FIFOTYPE is array(1 to 4) of bit_vector(7 downto 0);
type DMA_SOUND_PHASE_TYPE is(STOP, IDLE, STEREO_LEFT_OUT, STEREO_RIGHT_OUT,
							 MONO_OUT_S1, MONO_OUT_S2);
signal ADR_I				: bit_vector(7 downto 0);
signal FCLK_I				: bit;
signal DMA_SOUND_PHASE		: DMA_SOUND_PHASE_TYPE; 
signal DMA_SOUND_NEXT_PHASE	: DMA_SOUND_PHASE_TYPE; 
signal SOUND_DMA_CTRL		: bit_vector(7 downto 0);
signal SOUND_MODE_CTRL		: bit_vector(7 downto 0);
signal AUDIO_DATA			: bit_vector(7 downto 0);
signal AUDIO_LATCH_L		: bit_vector(7 downto 0);
signal AUDIO_LATCH_R		: bit_vector(7 downto 0);
signal LDn, RDn				: bit;
signal FIFO_STATUS			: FIFO_STATUS_TYPE;
signal GRP_A, GRP_B 		: GROUP_TYPE;
signal FIFO_REG				: FIFOTYPE; 
begin
	ADR_I <= '0' & ADR & '0';
	FCLK <= FCLK_I;	-- Frame clock.
	SDATA_L <= AUDIO_LATCH_L;	
	SDATA_R <= AUDIO_LATCH_R;

	-- FIFO and data request control:
	SREQ <= '1' when (GRP_A = EMPTY or GRP_B = EMPTY) and DMA_SOUND_PHASE /= STOP else '0';
	FIFO_STATUS <= 	FULL when GRP_A = FULL and GRP_B = FULL else
					EMPTY when GRP_A = EMPTY and GRP_B = EMPTY else
					MID;
	
	REGISTERS: process(RESETn, CLK)
	begin
		if RESETn = '0' then
			SOUND_DMA_CTRL <= (others => '0');
			SOUND_MODE_CTRL <= (others => '0');
		elsif CLK = '1' and CLK' event then
			if CSn = '0' and RWn = '0' and ADR_I = x"00" then
				SOUND_DMA_CTRL <= To_BitVector(DATA_IN(7 downto 0));
			elsif CSn = '0' and RWn = '0' and ADR_I = x"20" then
				SOUND_MODE_CTRL <= To_BitVector(DATA_IN(7 downto 0));
			end if;
		end if;
	end process REGISTERS;
	DATA_OUT <= x"00" & To_StdLogicVector(SOUND_DMA_CTRL) when CSn = '0' and RWn = '1' and ADR_I = x"00" else
				x"00" & To_StdLogicVector(SOUND_MODE_CTRL) when CSn = '0' and RWn = '1' and ADR_I = x"20" else (others => '0');
	DATA_EN <= '1' when CSn = '0' and RWn = '1' and (ADR_I = x"00" or ADR_I = x"20") else '0';
	
	SAMPLECLOCKS: process(RESETn, CLK, SOUND_MODE_CTRL)
	variable TEMP		: std_logic_vector(10 downto 0);
	variable LOCK		: boolean;
	begin
		if RESETn = '0' then
			TEMP := (others => '0');
			LOCK := false;
		elsif CLK = '1' and CLK' event then
			if SCLK = '1' and LOCK = false then
				LOCK := true;
				TEMP := TEMP + '1';
			elsif SCLK = '0' then
				LOCK := false;
			end if;
		end if;
		case SOUND_MODE_CTRL(1 downto 0) is
			when "00" => FCLK_I <= To_Bit(TEMP(10)); -- Sample frequency 06258Hz.
			when "01" => FCLK_I <= To_Bit(TEMP(9)); -- Sample frequency 12517Hz.
			when "10" => FCLK_I <= To_Bit(TEMP(8)); -- Sample frequency 25033Hz.
			when "11" => FCLK_I <= To_Bit(TEMP(7)); -- Sample frequency 50066Hz.
		end case;
	end process SAMPLECLOCKS;

	AUDIO_OUT: process
	variable FCLK_LOCK : boolean;
	begin
		wait until CLK = '1' and CLK' event;
		if RESETn = '0' then
			AUDIO_LATCH_L <= "10000000"; -- 2's complement...
			AUDIO_LATCH_R <= "10000000"; -- ...of nothing :-)
			FCLK_LOCK := false;
		elsif FCLK_I = '1' and FCLK_LOCK = false then
			FCLK_LOCK := true;
			if DMA_SOUND_PHASE = STOP then
				AUDIO_LATCH_L <= "10000000"; -- 2's complement.
			elsif LDn = '0' then
				AUDIO_LATCH_L(6 downto 0) <= AUDIO_DATA(6 downto 0);
				AUDIO_LATCH_L(7) <= not AUDIO_DATA(7); -- 2's complement.
			end if;
			if DMA_SOUND_PHASE = STOP then
				AUDIO_LATCH_R <= "10000000"; -- Of nothing :-)
			elsif RDn = '0' then
				AUDIO_LATCH_R(6 downto 0) <= AUDIO_DATA(6 downto 0);
				AUDIO_LATCH_R(7) <= not AUDIO_DATA(7); -- 2's complement.
			end if;
		elsif FCLK_I = '0' then
			FCLK_LOCK := false;
		end if;
	end process AUDIO_OUT;

	PHASE_MEM: process(RESETn, CLK)
	variable LOCK : boolean;
	begin
		if RESETn = '0' then
			DMA_SOUND_PHASE <= STOP;
			LOCK := false;
		elsif CLK = '1' and CLK' event then
			if FCLK_I = '1' and LOCK = false then
				LOCK := true;
				DMA_SOUND_PHASE <= DMA_SOUND_NEXT_PHASE;
			elsif FCLK_I = '0' then
				LOCK := false;
			end if;
		end if;
	end process PHASE_MEM;
	
	PHASE_CTRL: process(DMA_SOUND_PHASE, SOUND_DMA_CTRL(0), SOUND_MODE_CTRL(7), FIFO_STATUS)
	begin
		case DMA_SOUND_PHASE is
			when STOP =>
				if SOUND_DMA_CTRL(0) = '1' then
					DMA_SOUND_NEXT_PHASE <= IDLE;
				else
					DMA_SOUND_NEXT_PHASE <= STOP;
				end if;
				LDn <= '1'; 
				RDn <= '1'; 
			when IDLE =>  -- Wait for full FIFO.
				if SOUND_DMA_CTRL(0) = '1' and SOUND_MODE_CTRL(7) = '0' and FIFO_STATUS = FULL then
					DMA_SOUND_NEXT_PHASE <= STEREO_LEFT_OUT;
				elsif SOUND_DMA_CTRL(0) = '1' and SOUND_MODE_CTRL(7) = '1' and FIFO_STATUS = FULL then
					DMA_SOUND_NEXT_PHASE <= MONO_OUT_S1;
				elsif SOUND_DMA_CTRL(0) = '0' then
					DMA_SOUND_NEXT_PHASE <= STOP;
				else
					DMA_SOUND_NEXT_PHASE <= IDLE;
				end if;
				LDn <= '1'; 
				RDn <= '1'; 
			when STEREO_LEFT_OUT 	=>
				if FIFO_STATUS = EMPTY then
					DMA_SOUND_NEXT_PHASE <= STOP;
				elsif SOUND_DMA_CTRL(0) = '1' and SOUND_MODE_CTRL(7) = '0' then
					DMA_SOUND_NEXT_PHASE <= STEREO_RIGHT_OUT;
				else
					DMA_SOUND_NEXT_PHASE <= STOP;
				end if;
				-- The audio data register delays the audio output stream by
				-- one FCLK cycle. Therefore the LDn, RDn control signals
				-- are vice versa at this place.
				LDn <= '1'; 
				RDn <= '0'; 
			when STEREO_RIGHT_OUT 	=>
				if FIFO_STATUS = EMPTY then
					DMA_SOUND_NEXT_PHASE <= STOP;
				elsif SOUND_DMA_CTRL(0) = '1' and SOUND_MODE_CTRL(7) = '0' then
					DMA_SOUND_NEXT_PHASE <= STEREO_LEFT_OUT;
				else
					DMA_SOUND_NEXT_PHASE <= STOP;
				end if;
				-- The audio data register delays the audio output stream by
				-- one FCLK cycle. Therefore the LDn, RDn control signals
				-- are vice versa at this place.
				LDn <= '0'; 
				RDn <= '1'; 
			when MONO_OUT_S1 	=>
				if FIFO_STATUS = EMPTY then
					DMA_SOUND_NEXT_PHASE <= STOP;
				elsif SOUND_DMA_CTRL(0) = '1' and SOUND_MODE_CTRL(7) = '1' then
					DMA_SOUND_NEXT_PHASE <= MONO_OUT_S2;
				else
					DMA_SOUND_NEXT_PHASE <= STOP;
				end if;
				LDn <= '0'; 
				RDn <= '0'; 
			when MONO_OUT_S2	=>
				if FIFO_STATUS = EMPTY then
					DMA_SOUND_NEXT_PHASE <= STOP;
				elsif SOUND_DMA_CTRL(0) = '1' and SOUND_MODE_CTRL(7) = '1' then
					DMA_SOUND_NEXT_PHASE <= MONO_OUT_S1;
				else
					DMA_SOUND_NEXT_PHASE <= STOP;
				end if;
				LDn <= '0'; 
				RDn <= '0'; 
		end case;
	end process PHASE_CTRL;

	FIFO: process
	type FIFO_REG_SEL_TYPE is (GROUP_A, GROUP_B);
	variable FIFO_REG_SEL 		: FIFO_REG_SEL_TYPE;
	variable DE_LOCK : boolean;
	variable FCLK_LOCK : boolean;
	begin
		wait until CLK = '1' and CLK' event;
		if RESETn = '0' then
			for i in 1 to 4 loop
				FIFO_REG(i) <= (others => '0');
			end loop;
			GRP_A <= EMPTY;
			GRP_B <= EMPTY;
			DE_LOCK := false;
			FCLK_LOCK := false;
			FIFO_REG_SEL := GROUP_A;
		-- ###### FIFO sound data input ###### --
		elsif DMA_SOUND_PHASE = STOP then 
			for i in 1 to 4 loop
				FIFO_REG(i) <= (others => '0');
			end loop;
			GRP_A <= EMPTY;
			GRP_B <= EMPTY;
			FIFO_REG_SEL := GROUP_A;
		-- Load one word per HSYNC:
		elsif SLOADn = '0' and DE = '0' and GRP_A = EMPTY and DE_LOCK = false then
			FIFO_REG(4) <= 	To_BitVector(DATA_IN(15 downto 8));	-- Left channel, if stero.
			FIFO_REG(3) <= 	To_BitVector(DATA_IN(7 downto 0)); 	-- Right channel, if stero.
			GRP_A <= FULL;
			DE_LOCK := true; -- Only one sample per HSYNC (DE = '0').
		elsif SLOADn = '0' and DE = '0' and GRP_B = EMPTY  and DE_LOCK = false then
			FIFO_REG(2) <= 	To_BitVector(DATA_IN(15 downto 8)); 	-- Left channel, if stero.
			FIFO_REG(1) <= 	To_BitVector(DATA_IN(7 downto 0)); 	-- Right channel, if stero.
			GRP_B <= FULL;
			DE_LOCK := true; -- Only one sample per HSYNC (DE = '0').
		elsif DE = '1' then
			DE_LOCK := false;
		end if;
		-- ###### FIFO sound data output ###### --
		-- Reload one Byte per FCLK:
		if FCLK_I = '1' and FCLK_LOCK = false then
			FCLK_LOCK := true;
			if DMA_SOUND_PHASE = STOP or DMA_SOUND_PHASE = IDLE then
				AUDIO_DATA <= (others => '0');
			elsif DMA_SOUND_PHASE = STEREO_LEFT_OUT  and FIFO_REG_SEL = GROUP_A then
				AUDIO_DATA <= FIFO_REG(4);
			elsif DMA_SOUND_PHASE = STEREO_RIGHT_OUT and FIFO_REG_SEL = GROUP_A then
				AUDIO_DATA <= FIFO_REG(3);
				GRP_A <= EMPTY;
				FIFO_REG_SEL := GROUP_B;
			elsif DMA_SOUND_PHASE = STEREO_LEFT_OUT  and FIFO_REG_SEL = GROUP_B then
				AUDIO_DATA <= FIFO_REG(2);
			elsif DMA_SOUND_PHASE = STEREO_RIGHT_OUT and FIFO_REG_SEL = GROUP_B then
				AUDIO_DATA <= FIFO_REG(1);
				GRP_B <= EMPTY;
				FIFO_REG_SEL := GROUP_A;
			elsif DMA_SOUND_PHASE = MONO_OUT_S1 and FIFO_REG_SEL = GROUP_A then
				AUDIO_DATA <= FIFO_REG(4);
			elsif DMA_SOUND_PHASE = MONO_OUT_S2 and FIFO_REG_SEL = GROUP_A then
				AUDIO_DATA <= FIFO_REG(3);
				GRP_A <= EMPTY;
				FIFO_REG_SEL := GROUP_B;
			elsif DMA_SOUND_PHASE = MONO_OUT_S1 and FIFO_REG_SEL = GROUP_B then
				AUDIO_DATA <= FIFO_REG(2);
			elsif DMA_SOUND_PHASE = MONO_OUT_S2 and FIFO_REG_SEL = GROUP_B then
				AUDIO_DATA <= FIFO_REG(1);
				GRP_B <= EMPTY;
				FIFO_REG_SEL := GROUP_A;
			end if;
		elsif FCLK_I = '0' then
			FCLK_LOCK := false;
		end if;
	end process FIFO;
end architecture BEHAVIOR;