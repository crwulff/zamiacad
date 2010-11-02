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
---- Here are the shift registers for the monochrome and the      ----
---- colour mode                                                  ----
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
-- Revision 2K9A  2008/06/29 WF
--   Enhancements for the multisync compatible video modi.
--   Synchronized the counter in the process SH_CLK with the
--     LOADn signal once at system startup. This fixes the
--     white screens when starting up with the wrong counter
--     phase in the colour modes.
--   Several fixes concerning colour corrections.
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25914IP_CR_SHIFT_REG is
	port(
		CLK				: in bit;
		RESETn			: in bit;
		ADR				: in bit_vector (6 downto 1);
		CSn				: in bit;
        RWn				: in bit;
		LOADn, DE		: in bit;
		SH_MOD			: in bit_vector(7 downto 0);
		DATA_IN			: in std_logic_vector(15 downto 0); -- Data.
		DATA_OUT		: out std_logic_vector(7 downto 0);
		DATA_EN			: out bit;
		SR				: out bit_vector (3 downto 0)
	);
end WF25914IP_CR_SHIFT_REG;

architecture BEHAVIOUR of WF25914IP_CR_SHIFT_REG is
type LOAD_TYPES is (C4, C3, C2, C1);
signal ADR_I		: bit_vector(7 downto 0);
signal LOAD_CYCLE	: LOAD_TYPES;
signal SHFT_STRB	: bit;
signal HSCROLL		: std_logic_vector(7 downto 0);
signal DATA_I		: std_logic_vector(15 downto 0);
signal YINT_A		: bit_vector(79 downto 0);  -- Shift register 1.
signal YINT_B		: bit_vector(31 downto 0);  -- Shift register 2.
signal YINT_C		: bit_vector(23 downto 0);  -- Shift register 3.
signal YINT_D		: bit_vector(15 downto 0);  -- Shift register 4.
begin
	ADR_I <= '0' & ADR & '0';

	INBUFFER: process(RESETn, CLK)
	-- The video data is valid shortly before the rising edge of
	-- LOADn (see MCU timing). Therefore the video data is sampled,
	-- when LOADn is asserted. A pipelining of one clock does
	-- not take any effect to the video out. 
	begin
		if RESETn = '0' then
			DATA_I <= x"0000";
		elsif CLK = '1' and CLK' event then 
			if LOADn = '0' then
				DATA_I <= DATA_IN;
			end if;
		end if;
	end process INBUFFER;

	SH_CLK: process (CLK, SH_MOD)
	variable TEMP: std_logic_vector(1 downto 0);
    variable LOCK: boolean;
    begin
    	if CLK = '1' and CLK' event then
            if RESETn = '0' then
                TEMP := "00";
                LOCK := false;
            elsif LOADn = '0' and LOCK = false then
                TEMP := "00"; -- This is a one time sync.
                LOCK := true;
            else
                TEMP := TEMP + 1;
            end if;
            --
			case SH_MOD is
				when x"32" | x"02" => SHFT_STRB <= '1'; -- High resolution.
				when x"11" | x"21" => -- 32MHz, medium resolution, multisync compatibility mode.
					SHFT_STRB <= '1';
				when x"01" => -- 16MHz, medium resolution.
					case TEMP is
						when "00" => SHFT_STRB <= '0';
						when "01" => SHFT_STRB <= '1';
						when "10" => SHFT_STRB <= '0';
						when "11" => SHFT_STRB <= '1';
						when others => SHFT_STRB <= '0'; -- Covers U, X, Z, W, H, L, -.
					end case;
				when x"10" | x"20" => -- 16MHz, low resolution, multisync compatibility mode.
					case TEMP is
						when "00" => SHFT_STRB <= '0';
						when "01" => SHFT_STRB <= '1';
						when "10" => SHFT_STRB <= '0';
						when "11" => SHFT_STRB <= '1';
						when others => SHFT_STRB <= '0'; -- Covers U, X, Z, W, H, L, -.
					end case;
				when x"00" => -- 8MHz, low resolution.
					case TEMP is
						when "00" => SHFT_STRB <= '0';
						when "01" => SHFT_STRB <= '0';
						when "10" => SHFT_STRB <= '0';
						when "11" => SHFT_STRB <= '1';
						when others => SHFT_STRB <= '0'; -- Covers U, X, Z, W, H, L, -.
					end case;
				when others => SHFT_STRB <= '0'; -- Default.
			end case;
    	end if;
  	end process SH_CLK;

	SYNC: process
	-- This process is responsible to synchronise the load cycle during
	-- the horizontal sync pulse and to control the cycle during video
	-- data transfer from the MMU to the SHIFTER (DE = 1).
	variable LOCK: boolean;
    begin
    	wait until CLK = '1' and CLK' event;
		if DE = '0' and SHFT_STRB = '1' then
            LOAD_CYCLE <= C4;
		elsif SHFT_STRB = '1' then
            if LOADn = '0' and LOCK = false then
				LOCK := true; -- Act on LOADn edges.
				case LOAD_CYCLE is
					when C1 => LOAD_CYCLE <= C2;
					when C2 => LOAD_CYCLE <= C3;
					when C3 => LOAD_CYCLE <= C4;
					when C4 => LOAD_CYCLE <= C1;
				end case;
            elsif LOADn = '1' then
				LOCK := false;
			end if;
		end if;
  	end process SYNC;

	HSCROLL_REG: process(RESETn, CLK)
	begin
		if RESETn = '0' then
			HSCROLL <= (others => '0');
		elsif CLK = '1' and CLK' event then
			if CSn = '0' and RWn = '0' and ADR_I = x"64" then
				HSCROLL <= DATA_IN(7 downto 0); -- Write to register.
			end if;
		end if;
	end process HSCROLL_REG;
	DATA_OUT <= HSCROLL when CSn = '0' and RWn = '1' and ADR_I = x"64" else (others => '0'); -- Read.
	DATA_EN <= '1' when CSn = '0' and RWn = '1' and ADR_I = x"64" else '0';

	SYN_LOAD_SHIFT: process (RESETn, CLK)
	variable LOCK_LOAD: boolean;
	begin
		if RESETn = '0' then
			YINT_A <= (others => '0');
			YINT_B <= (others => '0');
			YINT_C <= (others => '0');
			YINT_D <= (others => '0');
    	elsif CLK = '1' and CLK' event then
      		----------Low resolution operation------------
			if (SH_MOD = x"00" or SH_MOD = x"10" or SH_MOD = x"20") and SHFT_STRB = '1' then
				-- Sample the data in the moment of the rising edge of LOADn.
				-- This is mandatory because the MMU controls the timing in
				-- this way.
				if LOADn = '1' and LOCK_LOAD = false and LOAD_CYCLE = C4 then
					LOCK_LOAD := true;
          			YINT_D(15 downto 0) <= To_BitVector(DATA_I);
					YINT_A <= YINT_A(78 downto 0) & '0'; -- Shift during register load.
					YINT_B <= YINT_B(30 downto 0) & '0'; -- Shift during register load.
					YINT_C <= YINT_C(22 downto 0) & '0'; -- Shift during register load.
				elsif LOADn = '1' and LOCK_LOAD = false and LOAD_CYCLE = C3 then
 					LOCK_LOAD := true;
          			YINT_C(15 downto 0) <= To_BitVector(DATA_I);
					YINT_C(23 downto 16) <= YINT_C(22 downto 15);
					YINT_A <= YINT_A(78 downto 0) & '0'; -- Shift during register load.
					YINT_B <= YINT_B(30 downto 0) & '0'; -- Shift during register load.
					YINT_D <= YINT_D(14 downto 0) & '0'; -- Shift during register load.
				elsif LOADn = '1' and LOCK_LOAD = false and LOAD_CYCLE = C2 then
					LOCK_LOAD := true;
         			YINT_B(15 downto 0) <= To_BitVector(DATA_I);
					YINT_B(31 downto 16) <= YINT_B(30 downto 15);
					YINT_A <= YINT_A(78 downto 0) & '0'; -- Shift during register load.
					YINT_C <= YINT_C(22 downto 0) & '0'; -- Shift during register load.
					YINT_D <= YINT_D(14 downto 0) & '0'; -- Shift during register load.
				elsif LOADn = '1' and LOCK_LOAD = false and LOAD_CYCLE = C1 then
					LOCK_LOAD := true;
          			YINT_A(15 downto 0) <= To_BitVector(DATA_I);
					YINT_A(79 downto 16) <= YINT_A(78 downto 15);
					YINT_B <= YINT_B(30 downto 0) & '0'; -- Shift during register load.
					YINT_C <= YINT_C(22 downto 0) & '0'; -- Shift during register load.
					YINT_D <= YINT_D(14 downto 0) & '0'; -- Shift during register load.
				elsif LOADn = '0' then
					LOCK_LOAD := false;
					YINT_A <= YINT_A(78 downto 0) & '0';
					YINT_B <= YINT_B(30 downto 0) & '0';
					YINT_C <= YINT_C(22 downto 0) & '0';
					YINT_D <= YINT_D(14 downto 0) & '0';
				elsif LOCK_LOAD = true then
					YINT_A <= YINT_A(78 downto 0) & '0';
					YINT_B <= YINT_B(30 downto 0) & '0';
					YINT_C <= YINT_C(22 downto 0) & '0';
					YINT_D <= YINT_D(14 downto 0) & '0';
				end if;
			---------------Medium resolution--------------	
			elsif (SH_MOD = x"01" or SH_MOD = x"11" or SH_MOD = x"21") and SHFT_STRB = '1' then
				-- Sample the data in the moment of the rising edge of LOADn.
				-- This is mandatory because the MMU controls the timing in
				-- this way.
				if LOADn = '1' and LOCK_LOAD = false and (LOAD_CYCLE = C2 or LOAD_CYCLE = C4) then
					LOCK_LOAD := true;
          			YINT_B(15 downto 0) <= To_BitVector(DATA_I);
					YINT_B(31 downto 16) <= YINT_B(30 downto 15);
					YINT_A <= YINT_A(78 downto 0) & '0'; -- Shift during A loads.
				elsif LOADn = '1' and LOCK_LOAD = false and (LOAD_CYCLE = C1 or LOAD_CYCLE = C3) then
					LOCK_LOAD := true;
          			YINT_A(15 downto 0) <= To_BitVector(DATA_I);
					YINT_A(79 downto 16) <= YINT_A(78 downto 15);
					YINT_B <= YINT_B(30 downto 0) & '0'; -- Shift during B loads.
				elsif LOADn = '0' then
					LOCK_LOAD := false;
					YINT_A <= YINT_A(78 downto 0) & '0';
					YINT_B <= YINT_B(30 downto 0) & '0';
				elsif LOCK_LOAD = true then
					YINT_A <= YINT_A(78 downto 0) & '0';
					YINT_B <= YINT_B(30 downto 0) & '0';
				end if;
      		-------High resolution, monochrome mode-------
	  		elsif (SH_MOD = x"32" or SH_MOD = x"02") and SHFT_STRB = '1' then
				-- Sample the data in the moment of the rising edge of LOADn.
				-- This is mandatory because the MMU controls the timing in
				-- this way.
				if LOADn = '1' and LOCK_LOAD = false then
					LOCK_LOAD := true;
                    YINT_A(15 downto 0) <= To_BitVector(DATA_I);
					YINT_A(79 downto 16) <= YINT_A(78 downto 15);
				elsif LOADn = '0' then
					LOCK_LOAD := false;
					YINT_A <= YINT_A(78 downto 0) & '0';
				elsif LOCK_LOAD = true then
					YINT_A <= YINT_A(78 downto 0) & '0';
				end if;
			else
				null; -- Nothing to do.
			end if;
    	end if;
	end process SYN_LOAD_SHIFT;

	SHIFT_OUT: process (SH_MOD, YINT_A, YINT_B, YINT_C, YINT_D, HSCROLL)
	-- The H_SHIFT is the pointer for horizontal finescrolling. For more
	-- information see ATARI technical descriptions ...
	-- The bit positions for SR(3) ... SR(0), the range of the register
	-- loading above and the range of connecting SRx to YINTx interfere
	-- with each other. In this file a first working version with
	-- correct colors and video out is given. Pay attention changing one
	-- of the topics mentioned here will cause a change to all!
    variable H_SHIFT	: integer range 0 to 15;
	begin
        H_SHIFT := conv_integer(HSCROLL(3 downto 0));
		if SH_MOD = x"00" or SH_MOD = x"10" or SH_MOD = x"20" then -- Low resolution.
            SR(3) <= YINT_D(15 - H_SHIFT);
            SR(2) <= YINT_C(19 - H_SHIFT);
            SR(1) <= YINT_B(23 - H_SHIFT);
            SR(0) <= YINT_A(27 - H_SHIFT);
		elsif SH_MOD = x"01" or SH_MOD = x"11" or SH_MOD = x"21" then -- Medium resolution.
			SR(3) <= '0';  -- Not used.
			SR(2) <= '0';  -- Not used.
            SR(1) <= YINT_B(31 - H_SHIFT);
            SR(0) <= YINT_A(39 - H_SHIFT);
		elsif SH_MOD = x"32" then -- High resolution.
			SR(3) <= '0';  -- Not used.
			SR(2) <= '0';  -- Not used.
			SR(1) <= '0';  -- Not used.
            SR(0) <= YINT_A(38 - H_SHIFT); -- Monochrome for Multisyncs.
		elsif SH_MOD = x"02" then -- High resolution.
			SR(3) <= '0';  -- Not used.
			SR(2) <= '0';  -- Not used.
			SR(1) <= '0';  -- Not used.
			SR(0) <= YINT_A(79 - H_SHIFT); -- Monochrome legacy.
		else -- Reserved.
			SR(3) <= '0';  -- Not used.
			SR(2) <= '0';  -- Not used.
			SR(1) <= '0';  -- Not used.
			SR(0) <= '0';  -- Not used.
		end if;
	end process SHIFT_OUT;
end BEHAVIOUR;
