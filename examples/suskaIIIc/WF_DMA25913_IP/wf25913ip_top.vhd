----------------------------------------------------------------------
----                                                              ----
---- ATARI DMA compatible IP Core					              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- ATARI ST and STE compatible DMA controller IP core.          ----
----                                                              ----
---- This is the Suska DMA IP core top level file.                ----
---- For a correct function of this code it is required, that the ----
---- rising edge of the 8MHz clock is in phase with the MCU's     ----
---- rising edge of the 16MHz clock. Otherwise the arbiter does   ----
---- not work properly concerning the DMA access timing.          ----
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
-- Revision 2K6A  2006/06/03 WF
--   Initial Release.
-- Revision 2K6B  2006/11/06 WF
--   Modified Source to compile with the Xilinx ISE.
-- Revision 2K8B  2008/12/24 WF
--   Rewritten this top level file as a wrapper for the top_soc file.
-- 

library work;
use work.WF25913IP_PKG.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25913IP_TOP is
	port (
		-- system controls:
	    RESETn		: in bit;	-- Master reset.
		CLK			: in bit; 	-- Clock must be the same as for the GLUE.
		FCSn    	: in bit;	-- Adress select.
		A1          : in bit;   -- Adress select.
		RWn			: in bit;	-- Read write control.
		RDYn		: inout std_logic;	-- Data acknowlege control (GLUE-DMA), open drain..
		DATA		: inout std_logic_vector(15 downto 0);	-- System data.

		-- ACSI section:
		CA2			: out bit;	-- ACSI adress.
		CA1			: out bit;	-- ACSI adress.
		CR_Wn		: out bit;	-- ACSI read write control.
		CD			: inout std_logic_vector(7 downto 0);	-- ACSI data.
		FDCSn		: out bit;			-- FLOPPY select.
		HDCSn		: out bit;			-- ACSI drive select.
		FDRQ		: in bit;			-- FLOPPY request.
		HDRQ		: in bit;			-- ACSI drive request.
		ACKn		: out bit			-- ACSI data acknowledge.
		);
end entity WF25913IP_TOP;
	
architecture STRUCTURE of WF25913IP_TOP is
component WF25913IP_TOP_SOC
	port (
	    RESETn		: in bit;
		CLK			: in bit;
		FCSn	    : in bit;
		A1  		: in bit;
		RWn			: in bit;
		RDY_INn		: in std_logic;
		RDY_OUTn	: out bit;
		DATA_IN		: in std_logic_vector(15 downto 0);
		DATA_OUT	: out std_logic_vector(15 downto 0);
		DATA_EN		: out bit;
        DMA_SRC_SEL : out bit_vector(1 downto 0);
		CA2			: out bit;
		CA1			: out bit;
		CR_Wn		: out bit;
		CD_IN		: in std_logic_vector(7 downto 0);
		CD_OUT		: out std_logic_vector(7 downto 0);
		CD_EN		: out bit;
		FDCSn		: out bit;
        SDCSn       : out bit;
        SCSICSn     : out bit;
		HDCSn		: out bit;
		FDRQ		: in bit;
		HDRQ		: in bit;
		ACKn		: out bit
		);
end component;
--
signal RDY_INn      : bit;
signal RDY_OUTn     : bit;
signal DATA_OUT     : std_logic_vector(15 downto 0);
signal DATA_EN      : bit;
signal CD_OUT       : std_logic_vector(7 downto 0);
signal CD_EN        : bit;
begin
    DATA <= DATA_OUT when DATA_EN = '1' else (others => 'Z');
    CD <= CD_OUT when CD_EN = '1' else (others => 'Z');
    RDYn <= '0' when RDY_OUTn = '0' else 'Z';

    I_DMA: WF25913IP_TOP_SOC
        port map(RESETn         => RESETn,
                 CLK            => CLK,
                 FCSn           => FCSn,
                 A1             => A1,
                 RWn            => RWn,
                 RDY_INn        => RDYn,
                 RDY_OUTn       => RDY_OUTn,
                 DATA_IN        => DATA,
                 DATA_OUT       => DATA_OUT,
                 DATA_EN        => DATA_EN,
                 --DMA_SRC_SEL  => Not used.
                 CA2            => CA2,
                 CA1            => CA1,
                 CR_Wn          => CR_Wn,
                 CD_IN          => CD,
                 CD_OUT         => CD_OUT,
                 CD_EN          => CD_EN,
                 FDCSn          => FDCSn,
                 -- SDCSn       =>, -- Not used.
                 -- SCSICSn     =>, -- Not used.
                 HDCSn          => HDCSn,
                 FDRQ           => FDRQ,
                 HDRQ           => HDRQ,
                 ACKn           => ACKn
            );
end architecture STRUCTURE;