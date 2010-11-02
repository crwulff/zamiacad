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
---- This is the package file containing the component            ----
---- declarations.                                                ----
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
-- 

library ieee;
use ieee.std_logic_1164.all;

package WF25913IP_PKG is
-- Component declarations:
component WF25913IP_REGISTERS
	port (
		CLK				: in bit;
		RESETn			: in bit;
		FCSn			: in bit;
		RWn				: in bit;
		A1  			: in bit;

		FIFO_ERROR		: in bit;
		ACSI_DATA_REQ	: in bit;
		SECTOR_CNT_EN	: in bit;
		
	    DATA_IN			: in std_logic_vector (8 downto 0);
	    DATA_OUT		: out std_logic_vector (15 downto 0);
		DATA_EN			: out bit;
		CD_IN			: in std_logic_vector (7 downto 0);
		CD_OUT			: out std_logic_vector (7 downto 0);
		CD_EN			: out bit;
		
		DMA_SRC_SEL		: out bit_vector(1 downto 0);
		DMA_RD_EN		: out bit;
		DMA_WR_EN		: out bit;

		CR_Wn			: out bit;
		DMA_RWn			: out bit;
        HDCSn			: out bit;
        SCSICSn         : out bit;
        SDCSn           : out bit;
        FDCSn			: out bit;
		CA				: out bit_vector(1 downto 0);
		CTRL_ACC		: out bit
    );
end component;

component WF25913IP_FIFO
	port(
		CLK			: in bit;

		CLRn 		: in bit;

		WR_ENA 		: in bit;
	    DATA_IN		: in bit_vector (15 downto 0);
		DATA_OUT 	: out bit_vector(15 downto 0);
		RD_ENA 		: in bit;

		FIFO_FULL 	: out bit;
		FIFO_HI		: out bit;
		FIFO_LOW	: out bit;
		FIFO_EMPTY 	: out bit;
		ERR			: out bit
	);
end component;

component WF25913IP_FIFO_UNIT
	port(
		CLK		: in bit;
		CLRn	: in bit;
		IN_A	: in bit_vector(15 downto 0);
		IN_B	: in bit_vector(15 downto 0);
		SEL 	: in bit;
		ENA		: in bit;
		D_OUT 	: out bit_vector(15 downto 0)
	);
end component;

component WF25913IP_FIFO_CTRL
	port(
		CLK 		: in bit; 
		CLRn 		: in bit;
		WR_ENA 		: in bit;
		RD_ENA 		: in bit;
		FIFO_FULL	: out bit;
		FIFO_HI 	: out bit;
		FIFO_LOW	: out bit;
		FIFO_EMPTY	: out bit;
		FIFO_ERR	: out bit;
		SEL 		: out bit_vector(16 downto 1);
		ENA 		: out bit_vector(16 downto 1)
	);
end component;

component WF25913IP_FIFO_DATAMUX
	port(
		CLK, CLRn		: in bit;
	    DATA_IN			: in std_logic_vector (15 downto 0);
	    DATA_OUT		: out std_logic_vector (15 downto 0);
		CD_IN			: in std_logic_vector (7 downto 0);
		CD_OUT			: out std_logic_vector (7 downto 0);
		FIFO_DATA_OUT	: in bit_vector(15 downto 0);
		FIFO_DATA_IN	: out bit_vector(15 downto 0);
		DATA_EN			: in bit;
		DMA_RWn			: in bit;
		CD_HIBUF_EN		: in bit;
		CD_RD_HIn		: in bit;
		CD_RD_LOWn		: in bit
 	);
end component;

component WF25913IP_CTRL
	port (
		CLK				: in bit;
		RESETn			: in bit;

		RDY_INn			: in std_logic;
		FCSn			: in bit;
		DMA_RD_EN		: in bit;
		DMA_WR_EN		: in bit;
		CTRL_ACC		: in bit;

		DMA_RWn 		: in bit;
		DMA_SRC_SEL		: in bit_vector(1 downto 0);
		HDRQ			: in bit;
		FDCRQ			: in bit;
		
		FIFO_FULL		: in bit;
		FIFO_HI			: in bit;
		FIFO_LOW		: in bit;
		FIFO_EMPTY		: in bit;

		CLRn			: out bit;

		FIFO_RD_ENA		: out bit;
		FIFO_WR_ENA		: out bit;

		DATA_EN			: out bit;
		
		CD_HIBUF_EN		: out bit;
		CD_RD_HIn		: out bit;
		CD_RD_LOWn		: out bit;

		ACSI_DATA_REQ	: out bit;
		SECTOR_CNT_EN	: out bit;

		FDCS_DMA_ACCn	: out bit;
		HD_ACKn			: out bit;
		RDY_OUTn		: out bit
	);
end component;
end WF25913IP_PKG;