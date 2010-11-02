----------------------------------------------------------------------
----                                                              ----
---- ATARI SHADOW compatible IP Core    			              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- This is the SUSKA SHADOW IP core control file.               ----
----                                                              ----
---- Description:                                                 ----
---- This is a Stacy or STBook compatible LCD video controller.   ----
---- The controller is modeled to drive a standard VGA LCD module ----
---- with a resolution of 640x480 dots. The original LCD with a   ----
---- resolution of 640x400 dots is withdrawn from the market and  ----
---- therefore not intended for use with this core.               ----
----                                                              ----
---- The LCD video timing of this file is prooven for the         ----
---- LCD modules:                                                 ----
---- SHARP LM64P803 (VGA monochrome)                              ---- 
---- SHARP LM641542 (VGA monochrome)                              ---- 
----                                                              ----
---- In the original chip there was a register at x"FF827E"       ----
---- It controls the Stacy or STBook relevant power management    ----
---- functions. This 8 bit register is not implemented here but   ----
---- can be found in the SHIFTER IP Core.                         ----
----                                                              ----
---- To Do:                                                       ----
---- -                                                            ----
----                                                              ----
---- Author(s):                                                   ----
---- - Wolfgang Foerster, wf@experiment-s.de; wf@inventronik.de   ----
----                                                              ----
----------------------------------------------------------------------
----                                                              ----
---- Copyright (C) 2006 Wolfgang Foerster                         ----
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
-- Revision 1.0  2006/04/27 WF
-- Initial Release.
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF_SHD101775IP_CTRL is
	port (
		RESETn	: in bit;
		CLK		: in bit; -- 16MHz, same as MCU clock.

		-- Video control inputs:
		DE			: in bit; -- Video Data enable.
		LOAD_STRB	: in bit; -- Video data load control.

		-- VIDEO RAM:
		R_ADR		: out bit_vector(14 downto 0);
		R_DATA_EN	: out bit;
		R_OEn		: out bit;
		R_WRn		: out bit;
		R_D_SEL		: out bit;
		
		-- FIFO controls:
		UDS_FIFO_EMPTY	: in bit;
		UDS_FIFO_FULL	: in bit;
		LDS_FIFO_EMPTY	: in bit;
		LDS_FIFO_FULL	: in bit;
		U_FIFO_WR		: out bit;
		L_FIFO_WR		: out bit;
		U_FIFO_RD		: out bit;
		L_FIFO_RD		: out bit;
		LCD_DATASEL		: out bit;
		LCD_UD_EN		: out bit;
		LCD_LD_EN		: out bit;
		
		-- LCD controls:
		LCD_S	: out bit; -- Line frame strobe.
		LCD_CP2	: out bit; -- Video data clock.
		LCD_CP1	: out bit -- Line latch clock.
	);
end WF_SHD101775IP_CTRL;

architecture BEHAVIOR of WF_SHD101775IP_CTRL is
type RAM_STATES is (IDLE, R_INIT_LOW, R_WR_LOW, R_INIT_HI, R_WR_HI, U_FIFO_INIT, 
												FIFO_U_WR, L_FIFO_INIT, FIFO_L_WR);
signal RAM_STATE, RAM_NEXTSTATE		: RAM_STATES;
signal NEWDATA	: boolean;
signal VSYNC	: boolean;
begin
	V_SYNC : process(RESETn, CLK)
	-- The vertical syncronisation signal is used to initialise the address
	-- counter for the RAM. The sync can be detected by missing DE which is
	-- zero for a short time during a horizontal sync and fo a longer time
	-- during a vertical sync. The detection time is 256us. For more infor-
	-- mation look in the GLUE's video timing file in the process related
	-- to the DE control.
	variable TMP : std_logic_vector(11 downto 0);
	variable LOCK : boolean;
	begin
		if RESETn = '0' then
			VSYNC <= false;
			TMP := (others => '1');
			LOCK := false;
		elsif CLK = '1' and CLK' event then
			if DE = '0' and TMP > x"000" then
				TMP := TMP - '1';
			elsif DE = '1' then
				TMP := (others => '1');
			end if;
			
			if TMP = x"000" and LOCK = false then
				VSYNC <= true;
				LOCK := true;
			elsif TMP /= x"000" then
				VSYNC <= false;
				LOCK := false;
			else
				VSYNC <= false;
			end if;
		end if;
	end process V_SYNC;

	NEW_DATA : process(RESETn, CLK)
	-- This process detects new loaded data to the input buffer. Right after the
	-- data is loaded, the RAM state machine controls the writing of the input
	-- buffer to the RAM. Although the LOAD control is a strobe, it is locked
	-- due to the high clock frequency of this shadow chip.
	variable LOCK : boolean;
	begin
		if RESETn = '0' then
			NEWDATA <= false;
			LOCK := false;
		elsif CLK = '1' and CLK' event then
			if LOAD_STRB = '1' and LOCK = false then
				NEWDATA <= true;
				LOCK := true;
			elsif RAM_STATE = R_INIT_HI or RAM_STATE = R_INIT_LOW then
				NEWDATA <= false; -- Data writing in progress.
			elsif LOAD_STRB = '0' then
				LOCK := false;
			end if;
		end if;
	end process NEW_DATA;

	RAMADR_PNTR: process(RESETn, CLK)
	-- This piece of hardware is important to store the addresses for read
	-- access or write access from or to the video ram. Since the addresses
	-- are not the same, this controller stores the correct values in the
	-- related ram control phases.
	variable WRITEPNTR: std_logic_vector(14 downto 0);
	variable RD_UDATA_PTR: std_logic_vector(14 downto 0);
	variable RD_LDATA_PTR: std_logic_vector(14 downto 0);
	begin
		if RESETn = '0' then
			WRITEPNTR := "111" & x"CFF";
			RD_UDATA_PTR := (others => '0');
			RD_LDATA_PTR := (others => '0');
		elsif CLK = '1' and CLK' event then
			if VSYNC = true then
				-- Do not initialize the FIFO addresses which are not directly
				-- correlated with the vertical CRT synchronization. The write
				-- control for the RAM comes with the counter enables. Therefore
				-- it is necessary to initialise the counter on zero minus one.
				-- This kind of implementation requires RAM chips with no address
				-- setup time against the WEn control input. An example for a
				-- well suited external static RAM ist the KM681000.
				-- For further information see the simulation of this file.
				WRITEPNTR := "111" & x"CFF";
			else
				case RAM_STATE is
					when R_WR_HI | R_WR_LOW =>
						if WRITEPNTR < "111" & x"CFF" then
							WRITEPNTR := WRITEPNTR + '1';
						else -- Reload after x7D00-1 increments.
							WRITEPNTR := (others => '0');
						end if;
					when others => null;
				end case;
			end if;
			case RAM_STATE is				
				-- Be aware, that the both counters for the upper and the lower screen segment
				-- are treated in a different way. The counter enable comes later in case of
				-- RD_UDATA_PTR. The reason for this is the modelling of U_FIFO_RD and L_FIFO_RD.
				-- These controls have not exactly the same timing due to U_FIFO_RD starts after 
				-- the 40 dead lines and L_FIFO_RD starts right at the beginning of the lower
				-- screen segment.
				when FIFO_U_WR =>
					if RD_UDATA_PTR < "011" & x"E7F" then
						RD_UDATA_PTR := RD_UDATA_PTR + '1';
					else  -- Reload at x3E80.
						RD_UDATA_PTR := (others => '0');
					end if;
				when L_FIFO_INIT  =>
					if RD_LDATA_PTR < "011" & x"E7F" then
						RD_LDATA_PTR := RD_LDATA_PTR + '1';
					else  -- Reload at x3E80.
						RD_LDATA_PTR := (others => '0');
					end if;
				when others => null;
			end case;
			-- RAM address multiplexer:
			case RAM_STATE is
				when R_INIT_LOW | R_WR_LOW | R_INIT_HI | R_WR_HI  => R_ADR <= To_BitVector(WRITEPNTR);
				when U_FIFO_INIT | FIFO_U_WR => R_ADR <= To_BitVector(RD_UDATA_PTR);
				when L_FIFO_INIT | FIFO_L_WR => R_ADR <= To_BitVector("11111010000000" + RD_LDATA_PTR); -- Offset is x3E80
				when others => R_ADR <= (others => '0'); -- IDLE.
			end case;
		end if;
	end process RAMADR_PNTR;

	CONTROL_P: process(RESETn, CLK)
	begin
		if RESETn = '0' then
			R_WRn <= '1';
			R_D_SEL <= '0';
			R_OEn <= '1';
			R_DATA_EN <= '0';
		elsif CLK = '1' and CLK' event then
			-- Data controls:
			case RAM_STATE is
				when R_WR_HI | R_WR_LOW => R_WRn <= '0';
				when others => R_WRn <= '1';
			end case;
			case RAM_STATE is
				when R_INIT_HI | R_WR_HI => R_D_SEL <= '1';
				when others => R_D_SEL <= '0';
			end case;

			-- RAM controls:
			case RAM_STATE is
				when U_FIFO_INIT | FIFO_U_WR | L_FIFO_INIT | FIFO_L_WR => R_OEn <= '0'; -- Read from RAM.
				when others => R_OEn <= '1';
			end case;
			-- RAM controls:
			case RAM_STATE is
				when R_INIT_LOW | R_WR_LOW | R_INIT_HI | R_WR_HI => R_DATA_EN <= '1'; -- Write to RAM.
				when others => R_DATA_EN <= '0';
			end case;
		end if;
	end process CONTROL_P;

	CONTROL_N: process(RESETn, CLK)
	-- The FIFO write controls are triggered on the negative clock edge.
	-- The write pulse thus occurs in the middle of the stable cycle of the
	-- RAM read address.
	begin
		if RESETn = '0' then
			L_FIFO_WR <= '0';
			U_FIFO_WR <= '0';
		elsif CLK = '0' and CLK' event then
			-- FIFO write controls:
			case RAM_STATE is
				when FIFO_L_WR => L_FIFO_WR <= '1';
				when others => L_FIFO_WR <= '0';
			end case;
			case RAM_STATE is
				when FIFO_U_WR => U_FIFO_WR <= '1';
				when others => U_FIFO_WR <= '0';
			end case;
		end if;
	end process CONTROL_N;

	RAM_STATEREG: process(RESETn, CLK)
	begin
		if RESETn = '0' then
			RAM_STATE <= IDLE;
		elsif CLK = '1' and CLK' event then
			RAM_STATE <= RAM_NEXTSTATE;
		end if;
	end process RAM_STATEREG;

	RAM_STATEDEC: process(RAM_STATE, LOAD_STRB, NEWDATA, UDS_FIFO_FULL, LDS_FIFO_FULL)
	-- This is the RAM state machine decoder. It works in conjunction with the shared
	-- video RAM of the ST machines. It requires a clock frequency of 16MHz. Therefore
	-- the 8 states of the state machine have a total cycle time of 500ns which is
	-- conform with the MCU timing. The FIFO status is checked only by the LDS FIFO
	-- because both FIFOs are controlled simultaneously in the same way.
	begin
		case RAM_STATE is
			when IDLE =>
				if NEWDATA = true then
					RAM_NEXTSTATE <= R_INIT_HI;
				elsif UDS_FIFO_FULL = '0' then
					RAM_NEXTSTATE <= U_FIFO_INIT;
				elsif LDS_FIFO_FULL = '0' then
					RAM_NEXTSTATE <= L_FIFO_INIT;
				else
					RAM_NEXTSTATE <= IDLE;
				end if;
			when R_INIT_HI =>
				RAM_NEXTSTATE <= R_WR_HI;
			when R_WR_HI =>
				RAM_NEXTSTATE <= R_INIT_LOW;
			when R_INIT_LOW =>
				RAM_NEXTSTATE <= R_WR_LOW;
			when R_WR_LOW =>
				if UDS_FIFO_FULL = '0' then
					RAM_NEXTSTATE <= U_FIFO_INIT;
				elsif LDS_FIFO_FULL = '0' then
					RAM_NEXTSTATE <= L_FIFO_INIT;
				else
					RAM_NEXTSTATE <= IDLE;
				end if;
			when U_FIFO_INIT =>
				RAM_NEXTSTATE <= FIFO_U_WR;
			when FIFO_U_WR =>
				if NEWDATA = true then
					RAM_NEXTSTATE <= R_INIT_HI;
				elsif LDS_FIFO_FULL = '0' then
					RAM_NEXTSTATE <= L_FIFO_INIT;
				else
					RAM_NEXTSTATE <= IDLE;
				end if;
			when L_FIFO_INIT =>
				RAM_NEXTSTATE <= FIFO_L_WR;
			when FIFO_L_WR =>
				if NEWDATA = true then
					RAM_NEXTSTATE <= R_INIT_HI; -- New data, go on.
				elsif UDS_FIFO_FULL = '0' then
					RAM_NEXTSTATE <= U_FIFO_INIT; -- Load FIFO.
				else
					RAM_NEXTSTATE <= IDLE; -- Nothing to do.
				end if;
		end case;
	end process RAM_STATEDEC;

	LCD_TIMING: process(RESETn, CLK) -- CLK is 16MHz.
	-- This video timing is based on a 16MHz clock. The colom strobe has a period of 250ns.
	-- For the VGA display there results a refresh period of 9.6ms respective 104,2 Hz.
	variable CLK_DIV	: std_logic_vector(1 downto 0);
	variable ROW_CNT	: std_logic_vector(7 downto 0);
	variable COL_CNT	: std_logic_vector(7 downto 0); -- 160x4 = 640.
	variable COL_LOCK 	: boolean;
	variable ROW_LOCK 	: boolean;
	begin
		if RESETn = '0' then
			LCD_DATASEL <= '1'; -- Start with upper data.
			CLK_DIV 	:= "00";
			COL_CNT 	:= (others => '0');
			ROW_CNT 	:= (others => '0');
			COL_LOCK 	:= false;
			ROW_LOCK 	:= false;
		elsif CLK = '1' and CLK' event then
			if UDS_FIFO_EMPTY = '0' and LDS_FIFO_EMPTY = '0' then
				-- Do not release any timing pulses if one of the FIFOs is empty.
				CLK_DIV := CLK_DIV + '1';
			elsif (UDS_FIFO_EMPTY = '1' or LDS_FIFO_EMPTY = '1') and CLK_DIV > "00" then
				-- Finish one cycle before locking the machine.
				CLK_DIV := CLK_DIV + '1';
			end if;
			
			if CLK_DIV(1) = '1' and COL_CNT < x"A0" and COL_LOCK = false then
				COL_CNT := COL_CNT + '1'; -- Count 640/4 colums; 4MHz counter frequency.
				COL_LOCK := true;
			elsif CLK_DIV(1) = '1' and COL_LOCK = false then
				COL_CNT := (others => '0'); -- Re-initialize.
				COL_LOCK := true;
			elsif CLK_DIV(1) = '0' then	
				COL_LOCK := false;
			end if;

			if COL_CNT = x"A0" and ROW_CNT < x"EF" and ROW_LOCK = false then
				ROW_CNT := ROW_CNT + '1'; -- Count 240 rows.
				ROW_LOCK := true;
			elsif COL_CNT = x"A0" and ROW_LOCK = false then
				ROW_CNT := (others => '0'); -- Re-initialize.
				ROW_LOCK := true;
			elsif COL_CNT = x"00" then	
				ROW_LOCK := false;
			end if;

			-- LCD clocks and control:
			if CLK_DIV(1) = '1' and COL_CNT < x"A0" then LCD_CP2 <= '1'; else LCD_CP2 <= '0'; end if;
			if CLK_DIV(1) = '1' and COL_CNT = x"A0" then LCD_CP1 <= '1'; else LCD_CP1 <= '0'; end if;
			-- The LCD_S pulse is centred between the first and the second line.
			if ROW_CNT = x"01" and COL_CNT = x"A0" then	LCD_S <= '1'; else LCD_S <= '0'; end if;

			-- LCD data controls:
			if ROW_CNT > x"27" then LCD_UD_EN <= '1'; else LCD_UD_EN <= '0'; end if; -- Uppest 40 lines off.
			if ROW_CNT < x"C8" then LCD_LD_EN <= '1'; else LCD_LD_EN <= '0'; end if; -- Lowest 40 lines off.
			LCD_DATASEL <= not To_Bit(COL_CNT(0)); -- Select high or low nibble.

			-- FIFO controls:
			-- The FIFO delivers the video information for the upper or the lower
			-- display segment. Therefore there are 80 Bytes (160 Nibbles) per row
			-- necessary. The Byte control is taken from the least significant bit of
			-- the column counter. This control signal is well adjusted to the LCD_CP2 pulse.
			-- For a detailed understanding use the control file's simulation.
			if CLK_DIV = "10" and COL_CNT(0) = '0' and COL_CNT < x"A0" and ROW_CNT > x"27" then
				U_FIFO_RD <= '1'; else U_FIFO_RD <= '0';
			end if;
			if CLK_DIV = "10" and COL_CNT(0) = '0' and COL_CNT < x"A0" and ROW_CNT < x"C8" then
				L_FIFO_RD <= '1'; else L_FIFO_RD <= '0';
			end if;
		end if;
	end process LCD_TIMING;
end architecture BEHAVIOR;
