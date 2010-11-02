----------------------------------------------------------------------
----                                                              ----
---- ATARI SHADOW compatible IP Core    			              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- This is the SUSKA SHADOW IP core package file.               ----
----                                                              ----
---- Description:                                                 ----
---- Controller to connect a LCD panel with VGA solution to the   ----
---- STE machine.                                                 ----
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

package WF_SHD101775IP_PKG is
-- Component declarations:
component WF_SHD101775IP_CTRL
	port (
		RESETn			: in bit;
		CLK				: in bit;
		DE				: in bit;
		LOAD_STRB		: in bit;
		R_ADR			: out bit_vector(14 downto 0);
		R_DATA_EN		: out bit;
		R_OEn			: out bit;
		R_WRn			: out bit;
		R_D_SEL			: out bit;
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
		LCD_S			: out bit;
		LCD_CP2			: out bit;
		LCD_CP1			: out bit
	);
end component;

component WF_SHD101775IP_FIFO
	port(
		CLK			: in bit;
		CLRn 		: in bit;
		WR_ENA 		: in bit;
		DATA_IN 	: in bit_vector(7 downto 0);
		DATA_OUT 	: out bit_vector(7 downto 0);
		RD_ENA 		: in bit;
		FIFO_FULL	: out bit;
		FIFO_EMPTY	: out bit;
		ERR			: out bit
	);
end component;
end WF_SHD101775IP_PKG;
