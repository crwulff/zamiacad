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
---- Linitations: There are only class 0 SCSI COMMANDs possible.  ----
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
---- The SCSI_IDn is a switch to SELCT the initiator ID of the   ----
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
----     Use for the data outputs tri state buffers ('541).       ----
----     Use for the data inputs tri state buffers ('541).        ----
----     SELCT for the output buffers a supply of +5V.           ----
----     SELCT for the data output buffers a supply of +5V.      ----
----     SELCT for the input buffers a supply of VCCIO of the    ----
----       SELCTed programmable logic device.                    ----
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
-- Revision 2K8B 2008/12/24 WF
--   Rewritten this top level file as a wrapper for the top_soc file.
-- 


library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF_ACSI_SCSI_IF is
port (  RESETn	: in bit; -- ST's reset signal low active.
		CLK		: in bit; -- 32MHz recommended.

		-- ACSI section:		
		CR_Wn		: in bit;
		CA1			: in bit;
		HDCSn		: in bit;
		HDACKn		: in bit;
		HDINTn		: out bit;
		HDRQn		: out bit;
		ACSI_D		: inout std_logic_vector(7 downto 0);
		ACSI_DIR	: out bit;
		
		-- SCSI section:
		SCSI_BUSYn	: in bit;
		SCSI_MSGn	: in bit;
		SCSI_REQn	: in bit;
		SCSI_DCn	: in bit;
		SCSI_IOn	: in bit;
		SCSI_RSTn	: out bit;
		SCSI_ACKn	: out bit;
		SCSI_SELn	: out bit;
		SCSI_DPn	: out bit;
		SCSI_D_IN	: in bit_vector(7 downto 0);
		SCSI_D_OUT	: out bit_vector(7 downto 0);
		SCSI_D_ENn	: out bit; -- Enable for the external data output buffer.

		-- Others:
		SCSI_IDn	: in bit_vector(3 downto 1) -- This is the ID switch.
      );
end WF_ACSI_SCSI_IF;

architecture BEHAVIOR of WF_ACSI_SCSI_IF is
component acsi_scsi_pll
	port
	(
		inclk0	: IN STD_LOGIC  := '0';
		c0		: OUT STD_LOGIC 
	);
--
end component;
--
component WF_ACSI_SCSI_IF_SOC
	port (  
		RESETn		    : in bit;
		CLK			    : in bit;
		CR_Wn		    : in bit;
		CA1			    : in bit;
		HDCSn		    : in bit;
		HDACKn		    : in bit;
		HDINTn		    : out bit;
		HDRQn		    : out bit;
		ACSI_D_IN	    : in bit_vector(7 downto 0);
		ACSI_D_OUT	    : out bit_vector(7 downto 0);
		ACSI_D_EN	    : out bit;
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
		SCSI_IDn		: in bit_vector(3 downto 1)
      );
end component;
--
signal CLK_I			: bit;
signal PLL_CLK_IN		: std_logic;
signal PLL_CLK_OUT		: std_logic;
signal ACSI_D_IN	    : bit_vector(7 downto 0);
signal ACSI_D_OUT	    : bit_vector(7 downto 0);
signal ACSI_D_EN        : bit;
signal SCSI_D_EN_I      : bit;
begin
	-- In this core a phase locked loop is used for
	-- purpose of testing several system frequencies.
	-- This component is hardware specific and thus
	-- a matter of change.
	PLL_CLK_IN <= '1' when CLK = '1' else '0';
	CLK_I <= To_Bit(PLL_CLK_OUT);

	I_CLKPLL: acsi_scsi_pll
	port map(
		inclk0	=> PLL_CLK_IN,
		c0		=> PLL_CLK_OUT
	);

    ACSI_DIR <= ACSI_D_EN;
    ACSI_D_IN <= To_BitVector(ACSI_D);
    ACSI_D <= To_StdLogicVector(ACSI_D_OUT) when ACSI_D_EN = '1' else (others => 'Z');

    SCSI_D_ENn <= not SCSI_D_EN_I;

    I_ACSI_SCSI: WF_ACSI_SCSI_IF_SOC
        port map(RESETn		        => RESETn,
                 CLK			    => CLK_I,
                 CR_Wn		        => CR_Wn,
                 CA1			    => CA1,
                 HDCSn		        => HDCSn,
                 HDACKn		        => HDACKn,
                 HDINTn		        => HDINTn,
                 HDRQn		        => HDRQn,
                 ACSI_D_IN	        => ACSI_D_IN,
                 ACSI_D_OUT	        => ACSI_D_OUT,
                 ACSI_D_EN	        => ACSI_D_EN,
                 SCSI_BUSYn		    => SCSI_BUSYn,
                 SCSI_MSGn		    => SCSI_MSGn,
                 SCSI_REQn		    => SCSI_REQn,
                 SCSI_DCn		    => SCSI_DCn,
                 SCSI_IOn		    => SCSI_IOn,
                 SCSI_RSTn		    => SCSI_RSTn,
                 SCSI_ACKn		    => SCSI_ACKn,
                 SCSI_SELn		    => SCSI_SELn,
                 SCSI_DPn		    => SCSI_DPn,
                 SCSI_D_IN		    => SCSI_D_IN,
                 SCSI_D_OUT		    => SCSI_D_OUT,
                 SCSI_D_EN		    => SCSI_D_EN_I,
                 --SCSI_CTRL_EN	    => -- Not used.
                 SCSI_IDn		    => SCSI_IDn
          );
end BEHAVIOR;