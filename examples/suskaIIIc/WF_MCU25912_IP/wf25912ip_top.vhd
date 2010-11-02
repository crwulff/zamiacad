----------------------------------------------------------------------
----                                                              ----
---- ATARI MCU compatible IP Core					              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- Memory management controller with all features to reach      ----
---- ATARI STE compatibility.                                     ----
----                                                              ----
---- This is the SUSKA MCU IP core top level file.                ----
----                                                              ----
----                                                              ----
---- Important Notice concerning the clock system:                ----
---- To use this code in a stand alone MCU chip or in a system    ----
---- on a programmable chip (SOC), the clock frequency may be     ----
---- selected via the CLKSEL setting. Use CLK_16M for the         ----
---- original MCU frequency (16MHz) or CLK_32M for the 32MHz      ----
---- SOC-GLUE.                                                    ----
---- Affected by the clock selection is the video timing and the  ----
---- DMA sound module (originally in the STE machines).           ----
----                                                              ----
---- To guarantee proper operation of the DMA interchange between ----
---- MCU, GLUE, DMA, the clocks must be well selected. For more   ----
---- information see the Suska top level file for the SOC system  ----
---- or respective documentation for the different original types ----
---- of ST or STE machines.                                       ----
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
--  Initial Release.
-- Revision 2K6B 2006/06/20 WF
--   Enhanced the STEs SINTn logic by the two signals SINT_TAI and SINT_IO7.
-- Revision 2K6B 2006/11/05 WF
--   Modified Source to compile with the Xilinx ISE.
-- Revision 2K7A  2007/01/02 WF
--   Changes to the clock system and related
--   hardware as sound or video control.
-- Revision 2K8B  2008/12/24 WF
--   Rewritten this top level file as a wrapper for the top_soc file.

library work;
use work.wf25912ip_pkg.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25912IP_TOP is
	generic(
		CLKSEL		: CLKSEL_TYPE := CLK_16M
	);
	port (
		CLK_16M		: in bit; -- System clock, originally 16MHz..
		
		ASn			: in bit; -- Bus control signals.
		LDSn, UDSn	: in bit; -- Bus control signals.
		RWn			: in bit; -- Bus control signals.

		ADR			: in bit_vector(23 downto 1); -- The STs address bus.

		RAMn		: in bit; -- RAM access control.
		DMAn		: in bit; -- DMA access control.
		DEVn		: in bit; -- Device access (A23 downto 16) = x"FF".
			
		VSYNCn		: in bit; -- Vertical sync.
		DE			: in bit; -- Horizontal or vertical sync.
					
		DCYCn		: out bit; -- Shifter load signal.
		CMPCSn		: out bit; -- Shifter video and sound register control.

        MONO_DETECT	: in bit; -- Monochrome monitor detector (pin 4 of the 13 pin round video plug).
        EXT_CLKSELn	: in bit; -- Genlock clock select (pin 3 of the 13 pin round video plug, formerly GPO).
        SREQ		: in bit;	-- Sound data request.
        SLOADn	    : out bit; 	-- DMA sound load control.
        SINTn	    : out bit; 	-- Sound frame interrupt signal.
        SINT_TAI	: out bit; 	-- Sound frame interrupt filtered for timer A.
        SINT_IO7	: out bit; 	-- Sound frame interrupt XORed for MFP_IO7

		RAS0n		: out bit; -- memory bank 1 row address strobe.
		CAS0Hn		: out bit; -- memory bank 1 column address strobe.
		CAS0Ln		: out bit; -- memory bank 1 column address strobe.

		WEn			: out bit; -- memory write control, low active.

		RAS1n		: out bit; -- memory bank 2 row address strobe.
		CAS1Hn		: out bit; -- memory bank 2 column address strobe.
		CAS1Ln		: out bit; -- memory bank 2 column address strobe.

        MAD	: out bit_vector(9 downto 0); -- DRAM addressbus.

		RDATn		: out bit; -- buffer control.
		WDATn		: out bit; -- buffer control.
		LATCHn		: out bit; -- buffer control.
			
		CLK_8M		: buffer bit; -- clock out.
		CLK_4M		: out bit; -- clock out.
			
		DTACKn		: out std_logic; -- data acknowledge signal.

		DATA		: inout std_logic_vector(7 downto 0)
	);
end entity WF25912IP_TOP;

architecture STRUCTURE of WF25912IP_TOP is
component WF25912IP_CLOCKS
port (
  CLK_x2	: in bit;
  CLK_x1	: out bit;
  CLK_x05	: out bit
);
end component;
--
component WF25912IP_TOP_SOC
	generic(
		CLKSEL		: CLKSEL_TYPE := CLK_32M
	);
	port(  
		CLK_x2		: in bit;
		CLK_x1		: in bit;
		ASn			: in bit;
		LDSn, UDSn	: in bit;
		RWn			: in bit;
		ADR			: in bit_vector(23 downto 1);
		RAMn		: in bit;
		DMAn		: in bit;
		DEVn		: in bit;
		VSYNCn		: in bit;
		DE			: in bit;
		DCYCn		: out bit;
		CMPCSn		: out bit;
		MONO_DETECT	: in bit;
		EXT_CLKSELn	: in bit;
		SREQ		: in bit;
		SLOADn		: out bit;
		SINT_TAI	: out bit;
		SINT_IO7	: out bit;
		RAS0n		: out bit;
		CAS0Hn		: out bit;
		CAS0Ln		: out bit;
		WEn			: out bit;
		RAS1n		: out bit;
		CAS1Hn		: out bit;
		CAS1Ln		: out bit;
		MAD			: out bit_vector(9 downto 0);
		RDATn		: out bit;
		WDATn		: out bit;
		LATCHn		: out bit;
		DTACKn		: out bit;
		DATA_IN		: in std_logic_vector(7 downto 0);
		DATA_OUT	: out std_logic_vector(7 downto 0);
		DATA_EN		: out bit
	);
end component;
signal DATA_OUT : std_logic_vector(7 downto 0);
signal DATA_EN  : bit;
signal DTACK_In : bit;
begin
    DATA <= DATA_OUT when DATA_EN = '1' else (others => 'Z');
    DTACKn <= '0' when DTACK_In = '0' else 'Z'; -- Open drain.
    SINTn <= '1';

    I_CLOCKS: WF25912IP_CLOCKS
    port map(
      CLK_x2        => CLK_16M,
      CLK_x1        => CLK_8M,
      CLK_x05       => CLK_4M
    );

    I_MCU: WF25912IP_TOP_SOC
--        generic map(
--            CLKSEL          => CLKSEL
--        );
        port map(  
            CLK_x2          => CLK_16M,
            CLK_x1          => CLK_8M,
            ASn             => ASn,
            LDSn            => UDSn,
            UDSn            => LDSn,
            RWn             => RWn,
            ADR             => ADR,
            RAMn            => RAMn,
            DMAn            => DMAn,
            DEVn            => DEVn,
            VSYNCn          => VSYNCn,
            DE              => DE,
            DCYCn           => DCYCn,
            CMPCSn          => CMPCSn,
            MONO_DETECT     => MONO_DETECT,
            EXT_CLKSELn     => EXT_CLKSELn,
            SREQ            => SREQ,
            SLOADn          => SLOADn,
            SINT_TAI        => SINT_TAI,
            SINT_IO7        => SINT_IO7,
            RAS0n           => RAS0n,
            CAS0Hn          => CAS0Hn,
            CAS0Ln          => CAS0Ln,
            WEn             => WEn,
            RAS1n           => RAS1n,
            CAS1Hn          => CAS1Hn,
            CAS1Ln          => CAS1Ln,
            MAD             => MAD,
            RDATn           => RDATn,
            WDATn           => WDATn,
            LATCHn          => LATCHn,
            DTACKn          => DTACK_In,
            DATA_IN         => DATA,
            DATA_OUT        => DATA_OUT,
            DATA_EN         => DATA_EN
        );
end architecture STRUCTURE;
