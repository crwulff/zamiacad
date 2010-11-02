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
---- Top level file for use in systems on programmable chips.     ----
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
--   Top level file provided for SOC (systems on programmable chips).
-- Revision 2K8A  2008/07/14 WF
--   Introduced DMA_SRC_SEL as a bit vector.
--   Some further (minor) changes.
-- 

library work;
use work.WF25913IP_PKG.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25913IP_TOP_SOC is
	port (
		-- system controls:
	    RESETn		: in bit;	-- Master reset.
		CLK			: in bit; 	-- Clock system.
		FCSn	    : in bit;	-- Adress select.
		A1  		: in bit;	-- Adress select.
		RWn			: in bit;	-- Read write control.
		RDY_INn		: in std_logic;	-- Data acknowlege control (GLUE-DMA).
		RDY_OUTn	: out bit;	-- Data acknowlege control (GLUE-DMA).
		DATA_IN		: in std_logic_vector(15 downto 0);	-- System data.
		DATA_OUT	: out std_logic_vector(15 downto 0);	-- System data.
		DATA_EN		: out bit;

        -- DMA-Configuration:
        DMA_SRC_SEL : out bit_vector(1 downto 0);

		-- ACSI section:
		CA2			: out bit;	-- ACSI adress.
		CA1			: out bit;	-- ACSI adress.
		CR_Wn		: out bit;	-- ACSI read write control.
		CD_IN		: in std_logic_vector(7 downto 0);	-- ACSI data.
		CD_OUT		: out std_logic_vector(7 downto 0);	-- ACSI data.
		CD_EN		: out bit; 	-- CD data enable.
		FDCSn		: out bit;	-- FLOPPY select.
        SDCSn       : out bit;  -- SD card select.
        SCSICSn     : out bit;  -- SCSI device select.
		HDCSn		: out bit;	-- ACSI drive select.
		FDRQ		: in bit;	-- FLOPPY request.
		HDRQ		: in bit; 	-- ACSI drive request.
		ACKn		: out bit	-- ACSI data acknowledge.
		);
end entity WF25913IP_TOP_SOC;
	
architecture STRUCTURE of WF25913IP_TOP_SOC is
signal DMA_SRC_SEL_I		: bit_vector(1 downto 0);
signal DMA_RWn_I			: bit;
signal CLR_In				: bit; --FIFO clear.
signal FIFO_DATA_OUT_I		: bit_vector(15 downto 0);
signal FIFO_DATA_IN_I		: bit_vector(15 downto 0);

signal FDCS_CTRL_REG_In		: bit;
signal FDCS_DMA_ACC_In		: bit;
signal ACSI_DATA_REQ_I		: bit;
signal SECTOR_CNT_EN_I		: bit;
signal DMA_RD_EN_I			: bit;
signal DMA_WR_EN_I			: bit;
signal CTRL_ACC_I			: bit;

signal FIFO_WR_ENA_I		: bit;
signal FIFO_RD_ENA_I		: bit;
signal FIFO_FULL_I			: bit;
signal FIFO_HI_I			: bit;
signal FIFO_LOW_I			: bit;
signal FIFO_EMPTY_I			: bit;
signal FIFO_ERR_I			: bit;

signal DATA_OUT_REG			: std_logic_vector(15 downto 0);
signal DATA_OUT_MUX			: std_logic_vector(15 downto 0);
signal DATA_EN_REG			: bit;
signal DATA_EN_MUX			: bit;

signal CD_OUT_REG			: std_logic_vector(7 downto 0);
signal CD_EN_REG			: bit;
signal CD_OUT_MUX			: std_logic_vector(7 downto 0);
signal CD_HIBUF_EN_I		: bit;
signal CD_RD_HI_In			: bit;
signal CD_RD_LOW_In			: bit;
begin
    FDCSn <= '0' when FDCS_CTRL_REG_In = '0' or FDCS_DMA_ACC_In = '0' else '1';

	DATA_EN <= DATA_EN_REG or DATA_EN_MUX;
	DATA_OUT <= DATA_OUT_REG when DATA_EN_REG = '1' else 
				DATA_OUT_MUX when DATA_EN_MUX = '1' else (others => '0');

	CD_EN <= 	'1' when CD_EN_REG = '1' else
				'1' when CD_RD_HI_In = '0' else
				'1' when CD_RD_LOW_In = '0' else '0';

	CD_OUT <=	CD_OUT_REG when CD_EN_REG = '1' else
				CD_OUT_MUX when CD_RD_HI_In = '0' or CD_RD_LOW_In = '0' else (others => '1');

    DMA_SRC_SEL <= DMA_SRC_SEL_I;

	I_DMAREGS: WF25913IP_REGISTERS
		port map(
			CLK				=> CLK,
			RESETn			=> RESETn,
			FCSn			=> FCSn,
			RWn				=> RWn,
			A1				=> A1,
			
			FIFO_ERROR		=> FIFO_ERR_I,

			ACSI_DATA_REQ	=> ACSI_DATA_REQ_I,
			SECTOR_CNT_EN	=> SECTOR_CNT_EN_I,
		
			DATA_IN			=> DATA_IN(8 downto 0),
			DATA_OUT		=> DATA_OUT_REG,
			DATA_EN			=> DATA_EN_REG,
			CD_IN			=> CD_IN,
			CD_OUT			=> CD_OUT_REG,
			CD_EN			=> CD_EN_REG,
			
			DMA_SRC_SEL		=> DMA_SRC_SEL_I,
			DMA_RD_EN		=> DMA_RD_EN_I,
			DMA_WR_EN		=> DMA_WR_EN_I,

			CR_Wn			=> CR_Wn,
			DMA_RWn			=> DMA_RWn_I,
			HDCSn			=> HDCSn,
            SCSICSn         => SCSICSn,
            SDCSn           => SDCSn,
			FDCSn			=> FDCS_CTRL_REG_In,
			CA(1)			=> CA2,
			CA(0)			=> CA1,
			CTRL_ACC		=> CTRL_ACC_I
		);

	I_FIFO: WF25913IP_FIFO
		port map(
			CLK			=> CLK,
			CLRn		=> CLR_In,		
			RD_ENA 		=> FIFO_RD_ENA_I,
			WR_ENA 		=> FIFO_WR_ENA_I,
			DATA_IN 	=> FIFO_DATA_IN_I,
			DATA_OUT 	=> FIFO_DATA_OUT_I,

			FIFO_FULL 	=> FIFO_FULL_I,
			FIFO_HI 	=> FIFO_HI_I,
			FIFO_LOW 	=> FIFO_LOW_I,
			FIFO_EMPTY 	=> FIFO_EMPTY_I,
			ERR 		=> FIFO_ERR_I
		);

	I_DMA_FIFO_DATAMUX: WF25913IP_FIFO_DATAMUX
		port map(
			CLK 			=> CLK,
			CLRn			=> CLR_In,
			DATA_IN			=> DATA_IN,
			DATA_OUT		=> DATA_OUT_MUX,
			CD_IN			=> CD_IN,
			CD_OUT			=> CD_OUT_MUX,
			FIFO_DATA_OUT	=> FIFO_DATA_OUT_I,
			FIFO_DATA_IN	=> FIFO_DATA_IN_I,
			DATA_EN			=> DATA_EN_MUX,
			DMA_RWn			=> DMA_RWn_I,
			CD_HIBUF_EN		=> CD_HIBUF_EN_I,
			CD_RD_HIn		=> CD_RD_HI_In,
			CD_RD_LOWn		=> CD_RD_LOW_In
		 );

	I_DMA_CTRL: WF25913IP_CTRL
		port map (
			CLK 			=> CLK,
			RESETn			=> RESETn,

			RDY_INn			=> RDY_INn,
			FCSn			=> FCSn,
			DMA_RD_EN		=> DMA_RD_EN_I,
			DMA_WR_EN		=> DMA_WR_EN_I,
			CTRL_ACC		=> CTRL_ACC_I,

			DMA_RWn			=> DMA_RWn_I,
			DMA_SRC_SEL		=> DMA_SRC_SEL_I,
			HDRQ			=> HDRQ,
			FDCRQ			=> FDRQ,

			FIFO_FULL 		=> FIFO_FULL_I,
			FIFO_HI 		=> FIFO_HI_I,
			FIFO_LOW 		=> FIFO_LOW_I,
			FIFO_EMPTY 		=> FIFO_EMPTY_I,

			CLRn			=> CLR_In,

			FIFO_RD_ENA 	=> FIFO_RD_ENA_I,
			FIFO_WR_ENA 	=> FIFO_WR_ENA_I,

			DATA_EN			=> DATA_EN_MUX,
			
			CD_HIBUF_EN		=> CD_HIBUF_EN_I,
			CD_RD_HIn		=> CD_RD_HI_In,
			CD_RD_LOWn		=> CD_RD_LOW_In,

			ACSI_DATA_REQ	=> ACSI_DATA_REQ_I,
			SECTOR_CNT_EN	=> SECTOR_CNT_EN_I,

			FDCS_DMA_ACCn	=> FDCS_DMA_ACC_In,
			HD_ACKn			=> ACKn,
			RDY_OUTn		=> RDY_OUTn
	);
end architecture STRUCTURE;