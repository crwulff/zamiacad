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
---- This files is the top level.                                 ----
----                                                              ----
---- To Do:                                                       ----
---- -                                                            ----
----                                                              ----
---- Author(s):                                                   ----
---- - Wolfgang Foerster, wf@experiment-s.de; wf@inventronik.de   ----
----                                                              ----
----------------------------------------------------------------------
----                                                              ----
---- Copyright (C) 2007 Wolfgang Foerster                         ----
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
-- Initial Release.
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF5C15_139xIP_TOP is
	port(
		CLK			: in bit; -- Use the CPU clock (Originally 8MHz).
		RESETn		: in bit;

		-- The bus interface:
		ADR			: in bit_vector(3 downto 0);
		DATA		: inout std_logic_vector(3 downto 0);
		CS, CSn		: in bit;
		WRn, RDn	: in bit;

		-- The SPI lines:
		SPI_D			: inout std_logic;
		SPI_SCL			: out bit;
		SPI_CSn			: out bit
		);
end entity WF5C15_139xIP_TOP;
	
architecture STRUCTURE of WF5C15_139xIP_TOP is
	component WF5C15_139xIP_REGISTERS
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
	end component;

	component WF5C15_139xIP_CTRL
		port(
			CLK				: in bit;
			RESETn			: in bit;
			SPI_PENDING		: in bit_vector(10 downto 0);
			SPI_STORE		: out bit;
			SPI_DATASEL		: out bit_vector(3 downto 0);
			SPI_DATA_IN		: in std_logic_vector(7 downto 0);
			SPI_DATA_OUT	: out std_logic_vector(7 downto 0);

			DATA_VALID		: in bit;

			-- SPI interface:
			SPI_IN			: in bit;
			SPI_OUT			: out bit;
			SPI_ENn			: out bit;
			SPI_SCL			: out bit;
			SPI_CSn			: out bit
		);
	end component;

	signal DATA_OUT				: std_logic_vector(3 downto 0);
	signal DATA_EN				: bit;
	signal DATA_VALID			: bit;

	signal SPI_PENDING			: bit_vector(10 downto 0);
	signal SPI_STORE			: bit;
	signal SPI_DATASEL			: bit_vector(3 downto 0);
	signal REG_DATA_OUT			: std_logic_vector(7 downto 0);
	signal SPI_DATA_OUT			: std_logic_vector(7 downto 0);

	signal SPI_IN				: bit;
	signal SPI_OUT				: bit;
	signal SPI_ENn				: bit;
	begin
		-- Data bus:
		DATA <= DATA_OUT when DATA_EN = '1' else (others => 'Z');

		-- SPI port:
		SPI_IN <= 	To_Bit(SPI_D);
		SPI_D 	<= 	'1' when SPI_OUT = '1' and SPI_ENn = '0' else
					'0' when SPI_OUT = '0' and SPI_ENn = '0' else 'Z';

		I_REGISTERS: WF5C15_139xIP_REGISTERS
		port map(
			CLK				=> CLK,
			RESETn			=> RESETn,

			ADR				=> ADR,
			DATA_IN			=> DATA,
			DATA_OUT		=> DATA_OUT,
			DATA_EN			=> DATA_EN,
			CS				=> CS,
			CSn				=> CSn,
			RDn				=> RDn,
			WRn				=> WRn,

			DATA_VALID		=> DATA_VALID,

			SPI_STORE		=> SPI_STORE,
			SPI_DATASEL		=> SPI_DATASEL,
			SPI_DATA_IN		=> SPI_DATA_OUT,
			SPI_DATA_OUT	=> REG_DATA_OUT,
			SPI_PENDING		=> SPI_PENDING
			);

		I_CTRL: WF5C15_139xIP_CTRL
		port map(
			CLK				=> CLK,
			RESETn			=> RESETn,
			SPI_PENDING		=> SPI_PENDING,
			SPI_STORE		=> SPI_STORE,
			SPI_DATASEL		=> SPI_DATASEL,
			SPI_DATA_IN		=> REG_DATA_OUT,
			SPI_DATA_OUT	=> SPI_DATA_OUT,
			
			DATA_VALID		=> DATA_VALID,

			SPI_IN			=> SPI_IN,
			SPI_OUT			=> SPI_OUT,
			SPI_ENn			=> SPI_ENn,
			SPI_SCL			=> SPI_SCL,
			SPI_CSn			=> SPI_CSn
		);
end STRUCTURE;
