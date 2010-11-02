----------------------------------------------------------------------
----                                                              ----
---- ATARI IP Core peripheral Add-On				              ----
----                                                              ----
---- This file is part of the FPGA-ATARI project.                 ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- This hardware provides limeted SCSI functionality. It works  ----
---- as an add-on to the ACSI bus (ACSI-SCSI bridge).             ----
---- Linitations: There are only class 0 SCSI commands possible.  ----
----                                                              ----
---- This moddeling is inspired by a sketch (unknown author) of a ----
---- ACSI to SCSI bridge. This sketch can be found in the docu-   ----
---- mentation of this core under 'ACSI-SCSI-Bridge'. It is also  ----
---- inspired by the original Atari ACSI-SCSI controller. The     ----
---- document is entitled "Atari ACSI/DMA Integration Guide".     ----
---- Thanks to Miroslav Nohaj 'Jookie' which did give me the      ----
---- information to find these documents.                         ----
---- The main difference of this core to all other known approa-  ----
---- ches is it's synchronous design. The core works well with    ----
---- for example 32MHz. This frequency is not necessarily syn-    ----
---- chronous to other system clocks. So use a system clock for   ----
---- it or produce the clock for example from a phase locked loop.----
---- The bridge features initiator identification and parity.     ----
----                                                              ----
---- The SCSI_IDn is a switch to select the initiator ID of the   ----
---- SCSI controller of this core. It is inverted, so use weak    ----
---- pull up resistors for it and connect the switch to GND. In   ----
---- this case (all switches on) the SCSI_IDn of "000" will       ----
---- indicate the highest initiator id of 7.                      ----
----                                                              ----
---- It is possible to use ACSI and SCSI devices together if the  ----
---- SCSI and ACSI switch settings are correct. The adapter usage ----
---- is identical to the original Atari ACSI-SCSI adapters.       ----
----                                                              ----
----   Recommendings for the hardware target concerning the SCSI  ----
----    interface:                                                ----
----     Use for the outputs non inverting buffers ('541).        ----
----     Use for the data in/outputs tri state buffers ('245).    ----
----     Select for the input / output buffers a supply of 3.3V.  ----
----     The VCCIO voltage of the selected FPGAs should also be   ----
----     at 3.3V for the related interface lines.                 ----
----                                                              ----
---- To Do:                                                       ----
---- -                                                            ----
----                                                              ----
---- Author(s):                                                   ----
---- - Wolfgang Foerster, wf@experiment-s.de; wf@inventronik.de   ----
----                                                              ----
----------------------------------------------------------------------
----                                                              ----
---- Copyright (C) 2005 - 2008 Wolfgang Foerster                   ----
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
---------------------------------------------------------------------
----															 ----
----	SCSI connector pinout:									 ----
----	Pin-Nr.		Name		Remarks							 ----
----	  50		I_On										 ----
----	  48		REQn										 ----
----	  46		D_Cn										 ----
----	  44		SELn										 ----
----	  42		MSGn										 ----
----	  40		RSTn										 ----
----	  38		ACKn										 ----
----	  36		BUSYn										 ----
----	  34		reserved	no connection			 		 ----
----	  32		ATNn		Pullup 220 Ohm to VCC			 ----
----	  30		reserved	no connection			 		 ----
----	  28		reserved	no connection			 		 ----
----	  26		TERMPWR		Hardwired to VCC				 ----
----	  24		reserved	no connection			 		 ----
----	  22		reserved	no connection					 ----
----	  20		reserved	no connection					 ----
----	  18		DPn			open drain                       ----
----	  16		SCSI_D7n	open drain                       ----
----	  14		SCSI_D6n	open drain                       ----
----	  12		SCSI_D5n	open drain                       ----
----	  10		SCSI_D4n	open drain                       ----
----	  8			SCSI_D3n	open drain                       ----
----	  6			SCSI_D2n	open drain                       ----
----	  4			SCSI_D1n	open drain                       ----
----	  2			SCSI_D0n	open drain                       ----
----															 ----
----	  25		reserved	no connection			 		 ----
----	  1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 27	GND		 ----
----	  29, 31, 33, 35, 37, 39, 41, 43, 46, 47, 49	GND		 ----
----															 ----
---------------------------------------------------------------------
---- This hardware works with the original ATARI				 ----
---- hard dik driver.											 ----
----------------------------------------------------------------------
-- 
-- Revision History
-- 
-- Revision 1.0  2005/09/10 WF
--   Initial Release.
-- Revision 1.1  2007/01/05 WF
--   Introduced SCSI parity.
--   Introduced Initiator identification.
--   Minor corrections.
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- Revision 2K9A  2009/06/20 WF
--   SCSI_ACKn has now synchronous reset.
--   HDRQn and HDINTn have now synchronous reset to meet preset requirements.
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF_ACSI_SCSI_IF_SOC is
	port (  
		RESETn		: in bit; -- ST's reset signal low active.
		CLK			: in bit; -- 32MHz recommended.

		-- ACSI section:		
		CR_Wn		: in bit;
		CA1			: in bit;
		HDCSn		: in bit;
		HDACKn		: in bit;
		HDINTn		: out bit;
		HDRQn		: out bit;
		ACSI_D_IN	: in bit_vector(7 downto 0);
		ACSI_D_OUT	: out bit_vector(7 downto 0);
		ACSI_D_EN	: out bit;

		-- SCSI section:
		-- Recommendations for the hardware target:
		-- Use for the outputs non inverting buffers ('34).
		-- Use for the data outputs tri state buffers ('540).
		-- Use for the inputs non inverting buffers ('34).
		-- Select for the output buffers a supply of +5V.
		-- Select for the data output buffers a supply of +5V.
		-- Select for the input buffers a supply of VCCIO of the
		--   selected programmable logic device.
		SCSI_BUSYn		: in bit;
		SCSI_MSGn		: in bit;
		SCSI_REQn		: in bit;
		SCSI_DCn		: in bit;
		SCSI_IOn		: in bit;
		SCSI_RSTn		: out bit;
		SCSI_ACKn		: out bit;
		SCSI_SELn		: out bit;
		SCSI_DPn		: out bit;
		SCSI_D_IN		: in bit_vector(7 downto 0);
		SCSI_D_OUT		: out bit_vector(7 downto 0);
		SCSI_D_EN		: out bit;
		SCSI_CTRL_EN	: out bit;

		-- Others:
		SCSI_IDn		: in bit_vector(3 downto 1) -- This is the ID switch.
      );
end WF_ACSI_SCSI_IF_SOC;

architecture BEHAVIOR of WF_ACSI_SCSI_IF_SOC is
type CTRL_STATES is (IDLE, MESG_OUT, SELCT, WAIT_CMD, COMMAND, DATA_IO, STATUS, MESG_IN);
signal CTRL_STATE		: CTRL_STATES;
signal NEXT_CTRL_STATE	: CTRL_STATES;
signal ACSI_D_IN_I		: bit_vector(7 downto 0);
signal SCSI_D_OUT_I		: bit_vector(7 downto 0);
signal TARGET_NO		: bit_vector(2 downto 0);
signal ACSI_ID			: bit_vector(7 downto 0);
signal SCSI_ID			: bit_vector(7 downto 0);
signal RES_I			: bit;
signal CR_Wn_I			: bit;
signal CA1_I			: bit;
signal HDCS_In			: bit;
signal HDACK_In			: bit;
signal SCSI_BUSY_In		: bit;
signal SCSI_MSG_In		: bit;
signal SCSI_REQ_In		: bit;
signal SCSI_DCn_I		: bit;
signal SCSI_IOn_I		: bit;
begin
	P_SYNC: process
	-- This module synchronizes the control signals to the
	-- internal used clock. This is important for the state
	-- machine. The process must work on the negative clock
	-- edge.
	begin
		wait until CLK = '0' and CLK' event;
		RES_I 			<= not RESETn;
		CR_Wn_I 		<= CR_Wn;
		CA1_I			<= CA1;
		HDCS_In			<= HDCSn;
		HDACK_In		<= HDACKn;
		SCSI_BUSY_In	<= SCSI_BUSYn;
		SCSI_MSG_In		<= SCSI_MSGn;
		SCSI_REQ_In		<= SCSI_REQn;
		SCSI_DCn_I		<= SCSI_DCn;
		SCSI_IOn_I		<= SCSI_IOn;
	end process P_SYNC;
	
	CONTROLLER_REG: process (RES_I, CLK)
	-- This is the ACSI-SCSI state machine register.
	begin
		if RES_I = '1' then
			CTRL_STATE <= IDLE;
		elsif CLK = '1' and CLK' event then
			CTRL_STATE <= NEXT_CTRL_STATE;
		end if;
	end process CONTROLLER_REG;

	CONTROLLER_DEC: process (CTRL_STATE, CR_Wn_I, CA1_I, HDCS_In, SCSI_BUSY_In, SCSI_DCn_I, SCSI_MSG_In)
	-- This is the ACSI-SCSI state machine decoder.
	begin
		case CTRL_STATE is
			when IDLE =>
				if CR_Wn_I = '0' and CA1_I = '0' and HDCS_In = '0' then
					NEXT_CTRL_STATE <= MESG_OUT; -- Select.
				else
					NEXT_CTRL_STATE <= IDLE;
				end if;
			when MESG_OUT =>
				-- The SCSI_BUSY_In must be released to guarantee a free bus.
				if CA1_I = '1' and HDCS_In = '1' and SCSI_BUSY_In = '1' then
					NEXT_CTRL_STATE <= SELCT;
				else
					NEXT_CTRL_STATE <= MESG_OUT;
				end if;
			when SELCT =>
				if SCSI_BUSY_In = '0' then
					NEXT_CTRL_STATE <= WAIT_CMD;
				elsif CR_Wn_I = '0' and CA1_I = '0' and HDCS_In = '0' then
					NEXT_CTRL_STATE <= MESG_OUT; -- Reselect.
				else
					NEXT_CTRL_STATE <= SELCT;
				end if;
			when WAIT_CMD => -- Wait until the SCSI_DCn_I becomes active.
				if SCSI_DCn_I = '0' then
					NEXT_CTRL_STATE <= COMMAND;
				else
					NEXT_CTRL_STATE <= WAIT_CMD;
				end if;
			when COMMAND =>
				if SCSI_DCn_I = '1' then
					NEXT_CTRL_STATE <= DATA_IO;
				else
					NEXT_CTRL_STATE <= COMMAND;
				end if;
			when DATA_IO =>
				if SCSI_DCn_I = '0' then
					NEXT_CTRL_STATE <= STATUS;
				else
					NEXT_CTRL_STATE <= DATA_IO;
				end if;
			when STATUS =>
				if SCSI_MSG_In = '0' then
					NEXT_CTRL_STATE <= MESG_IN;
				else
					NEXT_CTRL_STATE <= STATUS;
				end if;
			when MESG_IN => -- Not used by ACSI.
				if SCSI_MSG_In = '1' and SCSI_BUSY_In = '1' then
					NEXT_CTRL_STATE <= IDLE;
				-- The next condition occurs, if a new request from the host
				-- is released during the message phase.
				elsif CR_Wn_I = '0' and CA1_I = '0' and HDCS_In = '0' then
					NEXT_CTRL_STATE <= MESG_OUT; -- Go directly to select.
				else
					NEXT_CTRL_STATE <= MESG_IN;
				end if;
		end case;
	end process CONTROLLER_DEC;

	P_SCSI_D_OUT: process (RES_I, CLK)
	-- This process stores the ACSI data during the ACSI command phase
	-- and is transparent during the data I/O phase.
	begin
		if RES_I = '1' then
			ACSI_D_IN_I <= (others => '0');
			TARGET_NO <= "000";
		elsif CLK = '1' and CLK' event then
			if CTRL_STATE = MESG_OUT and CA1_I = '0' and HDCS_In = '0' then
				ACSI_D_IN_I <= "000" & ACSI_D_IN(4 downto 0); -- Control byte 0, rip the target number.
				TARGET_NO <= ACSI_D_IN(7 downto 5); -- Store the target number separately.
			elsif CTRL_STATE = COMMAND and HDCS_In = '0' then
				ACSI_D_IN_I <= ACSI_D_IN; -- Control byte 1 to 5.
			elsif CTRL_STATE = DATA_IO and HDACK_In = '0' then
				ACSI_D_IN_I <= ACSI_D_IN; -- Data bytes to target.
			end if;
		end if;
	end process P_SCSI_D_OUT;

	HDRQn_CTRL: process
	variable LOCK	: boolean;
	begin
		wait until CLK = '1' and CLK' event;
		if RES_I = '1' then
			HDRQn <= '1';
		elsif CTRL_STATE = DATA_IO and HDACK_In = '0' then
			HDRQn <= '1';
		elsif CTRL_STATE = DATA_IO and SCSI_REQ_In = '0' and LOCK = false then
			HDRQn <= '0';
			LOCK := true;
		elsif CTRL_STATE = DATA_IO and SCSI_REQ_In = '1' then
			LOCK := false;
		end if;
	end process HDRQn_CTRL;		

	SCSI_ACKn_REG: process
	-- This module controls the SCSI acknowledge.
	-- The DELAY affects a release of the SCSI_ACKn for two clock
	-- cycles after the HDACK_In pulse. This gives a good SCSI data
	-- timing against the HDACK_In.
	variable LOCK		: boolean;
	variable DELAY		: bit;
	begin
		wait until CLK = '1' and CLK' event;
		if RES_I = '1' then
			SCSI_ACKn <= '1';
		else
			case CTRL_STATE is
				when IDLE =>
					LOCK := true;
				when MESG_OUT =>
					if HDCS_In = '0' then
						LOCK := false;
					end if;
				when COMMAND =>
					if HDCS_In = '0' then
						LOCK := false;
					elsif SCSI_REQ_In = '0' and LOCK = false then
						SCSI_ACKn <= '0';
						LOCK := true;
					elsif SCSI_REQ_In = '1' then
						SCSI_ACKn <= '1';
					end if;
				when DATA_IO =>
					if HDACK_In = '0' then
						DELAY := '0';
						LOCK := false;
					elsif HDACK_In = '1' and LOCK = false then
						SCSI_ACKn <= '0';
						LOCK := true;
					elsif HDACK_In = '1' and DELAY = '0' then
						DELAY := '1';
					elsif HDACK_In = '1' then
						SCSI_ACKn <= '1';
						LOCK := true;
					end if;
				when STATUS =>				
					if HDCS_In = '1' and SCSI_REQ_In = '1' then
						LOCK := false;
					elsif HDCS_In = '0' and SCSI_REQ_In = '0' and LOCK = false then
						SCSI_ACKn <= '0';
						LOCK := true;
					elsif HDCS_In = '0' and SCSI_REQ_In = '1' then
						SCSI_ACKn <= '1';
					end if;
				when MESG_IN =>
					SCSI_ACKn <= SCSI_REQ_In; -- Simulate the last acknowledge.
				when others => null;
			end case;
		end if;
	end process SCSI_ACKn_REG;		

	INTn_CTRL: process
	-- The LOCK and FIRST registers affects, that the first SCSI_REQ_In
	-- in the command phase does not release an interrupt.
	variable FIRST	: boolean;
	variable LOCK	: boolean;
	begin
		wait until CLK = '1' and CLK' event;
		if RES_I = '1' then
			HDINTn <= '1';
		elsif CTRL_STATE = MESG_OUT then
			FIRST := false;
			LOCK := false;
		elsif CTRL_STATE = COMMAND and SCSI_REQ_In = '0' and FIRST = false then
			FIRST := true;
			LOCK := true;
		elsif CTRL_STATE = COMMAND and SCSI_REQ_In = '0' and FIRST = true and LOCK = false then
			HDINTn <= '0';
			LOCK := true;
		elsif CTRL_STATE = COMMAND and HDCS_In = '0' then
			HDINTn <= '1';
		elsif CTRL_STATE = COMMAND and SCSI_REQ_In = '1' then
			LOCK := false;
		elsif CTRL_STATE = STATUS and SCSI_REQ_In = '1' then
			LOCK := false;
		elsif CTRL_STATE = STATUS and SCSI_REQ_In = '0' and LOCK = false then
			HDINTn <= '0';
			LOCK := true;
		elsif CTRL_STATE = STATUS and HDCS_In = '0' then
			HDINTn <= '1';
		end if;
	end process INTn_CTRL;		

	PARITY: process(SCSI_D_OUT_I)
	-- This process provides the parity checking of the SCSI data.
	-- SCSI uses 'odd parity'.
	variable PAR_VAR : bit;
	begin
	    for i in 1 to 7 loop
            PAR_VAR := SCSI_D_OUT_I(i) xor SCSI_D_OUT_I(i-1);
	    end loop;
		--
		SCSI_DPn <= PAR_VAR;
	end process PARITY;

	-- ACSI target ID:
	with TARGET_NO SELECT
		ACSI_ID <= 	"01111111" when "111",
					"10111111" when "110",
					"11011111" when "101",
					"11101111" when "100",
					"11110111" when "011",
					"11111011" when "010",
					"11111101" when "001",
					"11111110" when "000";

	-- SCSI initiator ID:
	-- The SCSI_IDn switch is inverted.
	with SCSI_IDn SELECT
		SCSI_ID <= 	"01111111" when "000",
					"10111111" when "001",
					"11011111" when "010",
					"11101111" when "011",
					"11110111" when "100",
					"11111011" when "101",
					"11111101" when "110",
					"11111110" when "111";

    ACSI_D_EN <= '1' when SCSI_BUSY_In = '0' and SCSI_IOn_I = '0' and CR_Wn_I = '1' else '0';
	ACSI_D_OUT <= not SCSI_D_IN;

	SCSI_D_OUT <= SCSI_D_OUT_I;
	SCSI_D_OUT_I <= ACSI_ID and SCSI_ID when CTRL_STATE = SELCT else -- Initiator ID and the target selection.
					not ACSI_D_IN_I when CTRL_STATE = WAIT_CMD else -- SCSI COMMAND (class 0).
					not ACSI_D_IN_I when CTRL_STATE = COMMAND else -- SCSI COMMAND (class 0).
					not ACSI_D_IN_I when CTRL_STATE = DATA_IO and SCSI_IOn_I = '1' else (others => '1'); -- SCSI data.
	SCSI_D_EN <= 	'1' when CTRL_STATE = SELCT else
					'1' when CTRL_STATE = WAIT_CMD else
					'1' when CTRL_STATE = COMMAND else
				 	'1' when CTRL_STATE = DATA_IO and SCSI_IOn_I = '1' else '0';

	SCSI_CTRL_EN <= '1' when CTRL_STATE /= IDLE else '0';
	
	SCSI_SELn <= '0' when CTRL_STATE = SELCT else '1';
	SCSI_RSTn <= not RES_I;
end BEHAVIOR;
