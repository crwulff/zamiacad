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
---- This files is moddeling the DMA relevant registers.          ----
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
--   Introduced the access level register CD_2_DATA.
-- Revision 2K8B  2008/12/24 WF
--   Introduced SCSICSn and SDCSn.
--   Introduced DMA_SRC_SEL as a bit vector.
--   DMA_STATUS_REG has now synchronous reset to meet preset requirement.
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25913IP_REGISTERS is
	port(
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
end WF25913IP_REGISTERS;

architecture BEHAVIOR of WF25913IP_REGISTERS is
signal DMA_STATUS_REG	: bit_vector(2 downto 0);
signal DMA_MODE_REG		: bit_vector(8 downto 0);
signal SECTOR_CNT_REG	: std_logic_vector(7 downto 0);
signal CTRL_ACC_EN		: bit;
signal HDCSn_I			: bit;
signal HDCSn_OUT		: bit;
signal FDCSn_I			: bit;
signal FDCSn_OUT		: bit;
signal SCSICSn_I		: bit;
signal SCSICSn_OUT		: bit;
signal SDCSn_I			: bit;
signal SDCSn_OUT		: bit;
signal CA_I				: bit_vector(1 downto 0);
signal CA_OUT		    : bit_vector(1 downto 0);
signal DMA_RWn_I		: bit;
signal SECT_CNT_ZEROn	: bit;
signal CTRL_MASK		: std_logic_vector(2 downto 0);
signal DMA_EN_I			: bit;
signal CD_2_DATA        : std_logic_vector (7 downto 0);
signal CR_Wn_OUT		: bit;
begin
	CTRL_ACC <= '1' when CTRL_ACC_EN = '1' and FCSn = '0' and A1 = '0' else '0'; -- Controller access.

	DMA_MODE: process(RESETn, CLK)
	-- The DMA mode register is write only. At the same 
	-- adress the DMA status register is read only.
	begin
		if RESETn = '0' then
			DMA_MODE_REG <= (others => '0');
		elsif CLK = '1' and CLK' event then
			if FCSn = '0' and A1 = '1' and RWn = '0' then
                DMA_MODE_REG <= To_BitVector(DATA_IN); -- Write to register.
			end if;
		end if;
	end process DMA_MODE;

	-- Wiring of the DMA mode register ($FF8606|word, W):
	--  DMA mode/status                 BIT 8 7 6 5 4 3 2 1 0
	--  0 - read FDC/HDC, 1 - write --------' | | | | | | | |
	--  0 - DMA with HDC/SCSI, 1 - with FDC/SD' | | | | | | |
	--  0 - DMA on, 1 - no DMA -----------------' | | | | | |
	--  ACSI/SCSI/SD/Floppy see below ------------' | | | | |
	--  0 - FDC reg, 1 - sector count reg ----------' | | | |
	--  ACSI/SCSI/SD/Floppy see below ----------------' | | |
	--  0 - pin CA1 low, 1 - pin CA1 high --------------' | |
	--  0 - pin CA0 low, 1 - pin CA0 high ----------------' |
    --  0 - DMA with HDC/FDC, 1 - DMA with SCSI/SD-Card ----'

    -- Selection for the DMA_SRC_SEL(1) Bit 7 and DMA_SRC_SEL(0) Bit 0 of the DMA mode register:
    -- 00   : DMA with ACSI:
    -- 10   : DMA with FLOPPY:
    -- 01   : DMA with SCSI:
    -- 11   : DMA with SD card.

    -- Drive selection:
    -- This configuration was chosen due to compatibility with old software.
    FDCSn_I <= '0' when DMA_MODE_REG(5) = '0' and DMA_MODE_REG(3) = '0' else '1';
    HDCSn_I <= '0' when DMA_MODE_REG(5) = '0' and DMA_MODE_REG(3) = '1' else '1';
    SDCSn_I <= '0' when DMA_MODE_REG(5) = '1' and DMA_MODE_REG(3) = '0' else '1';
    SCSICSn_I <= '0' when DMA_MODE_REG(5) = '1' and DMA_MODE_REG(3) = '1' else '1';

	DMA_RWn_I <= not DMA_MODE_REG(8);
	DMA_SRC_SEL(1) <= DMA_MODE_REG(7);
	DMA_EN_I <= not DMA_MODE_REG(6);
    CTRL_ACC_EN <= not DMA_MODE_REG(4);
	CA_I <= DMA_MODE_REG(2 downto 1);
	DMA_SRC_SEL(0) <= DMA_MODE_REG(0);

	-- There is no need for complicated read enable control.
	DMA_RD_EN <= '1' when DMA_EN_I = '1' and SECT_CNT_ZEROn = '1' else '0';
	P_DMA_WR_EN: process
	-- The write enable control is some kind of tricky. Therefore it is controlled via
	-- this process. The write enable starts, if there is an access to the sector count
	-- register and the data flow direction is 'write to target'. It ends with the empty
	-- sector count register i.e. all sectors are written.
	begin
		wait until CLK = '1' and CLK' event;
		if FCSn = '0' and A1 = '0' and CTRL_ACC_EN = '0' and DMA_EN_I = '1' and DMA_RWn_I = '0' then
			DMA_WR_EN <= '1';
		elsif SECT_CNT_ZEROn = '0' then 
			DMA_WR_EN <= '0';
		end if;
	end process P_DMA_WR_EN;

	DMA_STATUS: process
	-- The DMA status register is read only. At the same 
	-- adress the DMA mode register is write only.
	begin
		wait until CLK = '1' and CLK' event;
		if RESETn = '0' then
			DMA_STATUS_REG <= "011"; -- Register is low active.
		-- Clear the status register by access (read or write)
		-- to the sector count register:
		elsif FCSn = '0' and A1 = '0' and CTRL_ACC_EN = '0' then
			DMA_STATUS_REG <= "111";	-- Clear.
		elsif FIFO_ERROR = '1' then
			DMA_STATUS_REG(0) <= '0'; -- Store the event.
		else
			DMA_STATUS_REG(2) <= not ACSI_DATA_REQ; -- Update.
			DMA_STATUS_REG(1) <= SECT_CNT_ZEROn; -- Update.
		end if;
	end process DMA_STATUS;
	
	SECTOR_CNT: process(RESETn, CLK, SECTOR_CNT_REG)
	begin
		if RESETn = '0' then
			SECTOR_CNT_REG <= x"00";
		elsif CLK = '1' and CLK' event then
			if FCSn = '0' and A1 = '0' and RWn = '0' and CTRL_ACC_EN = '0' then
				SECTOR_CNT_REG <= DATA_IN(7 downto 0); -- Write to register.
			elsif SECTOR_CNT_EN = '1' and SECTOR_CNT_REG > "00" then
				SECTOR_CNT_REG <= SECTOR_CNT_REG - '1'; -- Count down.
			end if;
		end if;
		case SECTOR_CNT_REG is
			when x"00" => SECT_CNT_ZEROn <= '0';
			when others => SECT_CNT_ZEROn <= '1';
		end case;
	end process SECTOR_CNT;

    CTRL_ACC_REG: process(RESETn, CLK)
    -- This is the controller access register for reading from the ACSI bus.
    -- It is necessary because the chips select timing of the floppy or of
    -- the hard drive (HDCSn and FDCSn) does not meet the asynchronous bus
    -- timing of the main processor. The direction from the main processor
    -- to the ACSI bus meets these requirements and therefore does not
    -- require an extra register.
    begin
        if RESETn = '0' then
            CD_2_DATA <= (others => '0');
        elsif CLK = '1' and CLK' event then
            if (HDCSn_OUT = '0' or FDCSn_OUT = '0' or SCSICSn_OUT = '0' or SDCSn_OUT = '0') and CR_Wn_OUT = '1' then
                CD_2_DATA <= CD_IN;
            end if;
        end if;
    end process CTRL_ACC_REG;
	
	-- Read from register:
	-- In read operation unused pins are read back as '0's:
	DATA_EN <= '1' when FCSn = '0' and RWn = '1' else '0';
	DATA_OUT <=	"0000000000000" & To_StdLogicVector(DMA_STATUS_REG) when FCSn = '0' and A1 = '1' and RWn = '1' else
				x"00" & SECTOR_CNT_REG when FCSn = '0' and A1 = '0' and RWn = '1' and CTRL_ACC_EN = '0' else
				-- Controller access stuff:
                x"00" & CD_2_DATA when FCSn = '0' and A1 = '0' and RWn = '1' and CTRL_ACC_EN = '1' else (others => '0');

    CD_EN <= '1' when CTRL_ACC_EN = '1' and FCSn = '0' and A1 = '0' and RWn = '0' else '0';
    CD_OUT <= DATA_IN(7 downto 0) when CTRL_ACC_EN = '1' and FCSn = '0' and A1 = '0' and RWn = '0' else (others => '0');
	
	CTRL_ACC_TIMING: process (CLK, RESETn, HDCSn_I, FDCSn_I, SCSICSn_I, SDCSn_I)
	-- This process provides the original timing to the WD1772 asserting several control
	-- signals on the ACSI bus. The timing is triggered by an access of the controller
	-- access register and stops after the mask counter ends its count cycle.
	-- This timing must be correlated with the RDY_OUTn timing in the control file!
	variable LOCK : boolean;
	begin
		if RESETn = '0' then
            CTRL_MASK <= "000";
			LOCK := false;
		elsif CLK = '1' and CLK' event then
			if CTRL_ACC_EN = '1' and FCSn = '0' and A1 = '0' and LOCK = false then
                CTRL_MASK <= "111"; -- Start condition immediately after the bus access.
				LOCK := true;
            elsif CTRL_MASK > "000" then
				CTRL_MASK <= CTRL_MASK - 1; -- Active.
            elsif CTRL_MASK = "000" and FCSn = '1' then
				LOCK := false;	-- Reset condition.
			end if;
		end if;
	end process CTRL_ACC_TIMING;

	with CTRL_MASK select  CA_OUT <= CA_I when "111" | "110" | "101" | "100", -- Phases 7, 6, 5, 4.
                                     "11" when others;
    with CTRL_MASK select  CR_Wn_OUT <= RWn when "111" | "110" | "101" | "100", -- Phases 7, 6, 5, 4.
                                        DMA_RWn_I when others;
	with CTRL_MASK select  HDCSn_OUT <= HDCSn_I when "110" | "101", -- Phases 6, 5.
    --with CTRL_MASK select  HDCSn_OUT <= HDCSn_I when "110" | "101" | "100", -- Phases 6, 5, 4.
                                        '1' when others;
	with CTRL_MASK select  SCSICSn_OUT <= SCSICSn_I when "110" | "101", -- Phases 6, 5.
	--with CTRL_MASK select  SCSICSn_OUT <= SCSICSn_I when "110" | "101" | "100", -- Phases 6, 5, 4.
                                          '1' when others;
	with CTRL_MASK select  SDCSn_OUT <= SDCSn_I when "110" | "101", -- Phases 6, 5.
	--with CTRL_MASK select  SDCSn_OUT <= SDCSn_I when "110" | "101" | "100", -- Phases 6, 5, 4.
                                        '1' when others;
	with CTRL_MASK select  FDCSn_OUT <= FDCSn_I when "110" | "101", -- Phases 6, 5.
    --with CTRL_MASK select  FDCSn_OUT <= FDCSn_I when "110" | "101" | "100", -- Phases 6, 5, 4.
                                        '1' when others;

    CA      <= CA_OUT;
    CR_Wn   <= CR_Wn_OUT;
    HDCSn   <= HDCSn_OUT;
    SCSICSn <= SCSICSn_OUT;
    SDCSn   <= SDCSn_OUT;
    FDCSn   <= FDCSn_OUT;
    DMA_RWn <= DMA_RWn_I;
end architecture BEHAVIOR;
