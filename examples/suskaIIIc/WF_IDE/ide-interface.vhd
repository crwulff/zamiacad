----------------------------------------------------------------------
----                                                              ----
---- ATARI compatible IP Core	    				              ----
----                                                              ----
---- This file is part of the FPGA-ATARI project.                 ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
----------------------------------------------------------------------
---- Description:                                                 ----
---- This VHDL model is based on PERA Putnik's IDE interface	  ----
---- interface (version 1998-12-23) but has full decoding of the  ----
---- respective address lines and is valid for word access only.  ----
---- Further the DMAn control signal is taken into account.       ----
---- Use external bus drivers for the connection of the IDE data  ----
---- lines as follows:                                            ----
---- Use a 16 bit wide LVTTL tri state drivers to control the     ----
---- data direction from or to an IDE device.                     ----
---- The IDE_D_EN_INn and IDE_D_EN_OUTn outputs are the respective----
---- tri state enables where IDE_D_EN_INn controls the tri state  ----
---- for the read operation from an IDE device and IDE_D_EN_OUTn  ----
---- controls the write operation to an IDE device.               ----
---- Select for the output buffers a supply of +5V.               ----
---- Select for the input buffers a supply of VCCIO of the        ----
---- selected programmable logic device.                          ----
----															  ----
---- Be aware, that only TOS 2.06 or above operating system		  ----
---- versions check for IDE drives during boot process.			  ----
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
----															  ----
----	IDE connector pinout:									  ----
----	Pin-Nr.		Name		Remarks							  ----
----	  40		GND											  ----
----	  38		SELSn										  ----
----	  36		IDE_A2		Hardwired to ATARI adress bit A4  ----
----	  34		PDIAG										  ----
----	  32		reseved										  ----
----	  30		GND											  ----
----	  28		CSEL		Cable select, hardwired to GND	  ----
----	  26		GND											  ----
----	  24		GND											  ----
----	  22		GND											  ----
----	  20		keypin		No connection				 	  ----
----	  18		IDE_D15										  ----
----	  16		IDE_D14										  ----
----	  14		IDE_D13										  ----
----	  12		IDE_D12										  ----
----	  10		IDE_D11										  ----
----	  8			IDE_D10										  ----
----	  6			IDE_D9										  ----
----	  4			IDE_D8										  ----
----	  2			GND											  ----
----	  39		DASP		LED-Cathode, host's output		  ----
----	  37		SELPn										  ----
----	  35		IDE_A0		Hardwired to ATARI adress bit A2  ----
----	  33		IDE_A1		Hardwired to ATARI adress bit A3  ----
----	  31		INTRQ										  ----
----	  29		DMACKn		Wire via 100 Ohm to VCC			  ----
----	  27		IDE_IORDYn								      ----
----	  25		IORDn										  ----
----	  23		IOWRn										  ----
----	  21		DMARQ										  ----
----	  19		GND											  ----
----	  17		IDE_D0										  ----
----	  15		IDE_D1										  ----
----	  13		IDE_D2										  ----
----	  11		IDE_D3										  ----
----	  9			IDE_D4										  ----
----	  7			IDE_D5										  ----
----	  5			IDE_D6										  ----
----	  3			IDE_D7										  ----
----	  1			IDE_RESn									  ----
----															  ----
----------------------------------------------------------------------
-- 
-- Revision History
-- 
-- Revision 1.0  2005/09/10 WF
--   Initial Release.
-- Revision 2K8B  2008/12/24 WF
--   Bug fixes to get the thing working.
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF_IDE is
	port ( 
		RESETn	        : in bit;

		ADR		        : in bit_vector(23 downto 4);
		
		ASn		        : in std_logic;
        UDSn, LDSn      : in bit;
		RWn		        : in std_logic;
		DTACKn	        : out std_logic;
		
		-- Interrupt via ACSI:
		ACSI_HDINTn	    : out bit;
		
		-- IDE section:
		IDE_INTRQ	    : in bit;
		IDE_IORDY	    : in bit;
		-- PDIAG	    : in bit; -- Not used so far.
		-- DASP		    : in bit; -- See pinout above.
		-- DMARQ	    : in bit; -- Not used so far.
		-- DMACKn	    : out bit; -- See pinout above.
		IDE_RESn	    : out std_logic; -- Open drain.
		SELPn		    : out bit;
		SELSn		    : out bit;
		IORDn		    : out bit;
		IOWRn		    : out bit;
		
		LINEAn		    : out bit
      );
end WF_IDE;

architecture BEHAVIOR of WF_IDE is
begin
	DTACKn <= '0' when ASn = '0' and UDSn = '0' and LDSn = '0' and DMAn = '1' and ADR >= x"F0000" and ADR < x"F0004" else 
              '0' when IDE_IORDY = '0' else 'Z';

    IOWRn <= '0' when ASn = '0' and UDSn = '0' and LDSn = '0' and DMAn = '1' and ADR = x"F0000" and RWn = '0' else
             '0' when ASn = '0' and UDSn = '0' and LDSn = '0' and DMAn = '1' and ADR = x"F0001" and RWn = '0' else
             '0' when ASn = '0' and UDSn = '0' and LDSn = '0' and DMAn = '1' and ADR = x"F0002" and RWn = '0' else
             '0' when ASn = '0' and UDSn = '0' and LDSn = '0' and DMAn = '1' and ADR = x"F0003" and RWn = '0' else '1';

    IORDn <= '0' when ASn = '0' and UDSn = '0' and LDSn = '0' and DMAn = '1' and ADR = x"F0000" and RWn = '1' else
             '0' when ASn = '0' and UDSn = '0' and LDSn = '0' and DMAn = '1' and ADR = x"F0001" and RWn = '1' else
             '0' when ASn = '0' and UDSn = '0' and LDSn = '0' and DMAn = '1' and ADR = x"F0002" and RWn = '1' else
             '0' when ASn = '0' and UDSn = '0' and LDSn = '0' and DMAn = '1' and ADR = x"F0003" and RWn = '1' else '1';

    SELPn <= '0' when ASn = '0' and DMAn = '1' and ADR = x"F0000" else
             '0' when ASn = '0' and DMAn = '1' and ADR = x"F0001" else '1';

    SELSn <= '0' when ASn = '0' and DMAn = '1' and ADR = x"F0002" else
             '0' when ASn = '0' and DMAn = '1' and ADR = x"F0003" else '1';

    LINEAn <= '0' when ASn = '0' and DMAn = '1' and ADR = x"F0000" else
              '0' when ASn = '0' and DMAn = '1' and ADR = x"F0001" else
              '0' when ASn = '0' and DMAn = '1' and ADR = x"F0002" else
              '0' when ASn = '0' and DMAn = '1' and ADR = x"F0003" else '1';

	IDE_RESn <= '0' when RESETn = '0' else 'Z';
	ACSI_HDINTn <= '0' when IDE_INTRQ = '1' else '1';

end BEHAVIOR;
