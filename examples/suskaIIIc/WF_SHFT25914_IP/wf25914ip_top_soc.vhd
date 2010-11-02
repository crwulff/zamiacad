----------------------------------------------------------------------
----                                                              ----
---- ATARI SHIFTER compatible IP Core				              ----
----                                                              ----
---- This file is part of the SUSKA ATARI clone project.          ----
---- http://www.experiment-s.de                                   ----
----                                                              ----
---- Description:                                                 ----
---- ST and STE compatible SHIFTER IP core.                       ----
----                                                              ----
---- Top level file for use in systems on programmable chips.     ----
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
--   Added the Stacy or STBook SHADOW register at address $FF827E.
-- Revision 2K6B  2006/11/06 WF
--   Modified Source to compile with the Xilinx ISE.
--   Top level file provided for SOC (systems on programmable chips).
-- Revision 2K8A  2008/07/14 WF
--   Minor changes.
-- Revision 2K9A  2008/06/29 WF
--   Changes concerning the multisync compatibility.
-- 

library work;
use work.wf25914ip_pkg.all;
library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

entity WF25914IP_TOP_SOC is
	port (
		CLK				: in bit; -- Originally 32MHz in the ST machines.
	    RESETn		    : in bit; -- Master reset.
		SH_A			: in bit_vector(6 downto 1); -- Adress bus (without base adress).
		SH_D_IN			: in std_logic_vector(15 downto 0); -- Data bus input.
		SH_D_OUT		: out std_logic_vector(15 downto 0); -- Data bus output.
		SH_DATA_HI_EN	: out bit; -- Data output enable for the high byte.
		SH_DATA_LO_EN	: out bit; -- Data output enable for the low byte.
		SH_RWn			: in bit; -- Write to registers is low active.
		SH_CSn			: in bit; -- Base adress of the shifter is 0xFF82xx.

		MULTISYNC		: in bit_vector(1 downto 0); -- Select multisync compatible video modi.
		SH_LOADn		: in bit; -- Load signal for the shift registers.
		SH_DE			: in bit; -- Shift switch for the shift registers.
		SH_BLANKn		: in bit; -- Blanking input.
		CR_1512			: out bit_vector(3 downto 0); -- Hi nibble of the chroma out.
		SH_R			: out bit_vector(3 downto 0); -- Red video output.
		SH_G			: out bit_vector(3 downto 0); -- Green video output.
		SH_B			: out bit_vector(3 downto 0); -- Blue video output.
		SH_MONO			: out bit; -- Monochrome video output.
		SH_COLOR		: out bit; -- COMP_SYNC signal of the ST.
		
		SH_SCLK			: in bit; -- Sample clock, 6.4 MHz.
		SH_FCLK			: out bit; -- Frame clock.
		SH_SLOADn		: in bit; -- DMA load control.
		SH_SREQ			: out bit; -- DMA load request.
		SH_SDATA_L		: out bit_vector(7 downto 0); -- Left audio data.
		SH_SDATA_R		: out bit_vector(7 downto 0); -- Right audio data.

		SH_MWK			: out bit; -- Microwire interface, clock.
		SH_MWD			: out bit; -- Microwire interface, data.
		SH_MWEn			: out bit; -- Microwire interface, enable.
		
		-- Port connections of xFF872E_D:
		-- Bit 7 = MTR_POWER_ON (Turns on IDE drive motor).
		-- Bit 6 not further specified.
		-- Bit 5 = RS232_OFF.
		-- Bit 4 = REFRESH_MACHINE.
		-- Bit 3 = LAMP (LCD backlight).
		-- Bit 2 = POWER_OFF.
		-- Bit 1 = SHFT output.
		-- Bit 0 = SHADOW chip off.
		xFF827E_D		: out bit_vector(7 downto 0)
	);
end WF25914IP_TOP_SOC;

architecture STRUCTURE of WF25914IP_TOP_SOC is
signal DATA_OUT_SHMOD_REG	: std_logic_vector(7 downto 0);
signal DATA_OUT_SHIFT_REG	: std_logic_vector(7 downto 0);
signal DATA_OUT_CR_REG		: std_logic_vector(15 downto 0);
signal DATA_OUT_MICROWIRE	: std_logic_vector(15 downto 0);
signal DATA_OUT_DMASOUND	: std_logic_vector(15 downto 0);
signal DATA_EN_SHMOD_REG	: bit;
signal DATA_EN_SHIFT_REG	: bit;
signal DATA_EN_CR_REG		: bit;
signal DATA_EN_MICROWIRE	: bit;
signal DATA_EN_DMASOUND		: bit;
signal RESET_In				: bit; 
signal SH_MOD_I				: bit_vector(7 downto 0);
signal SR_I					: bit_vector(3 downto 0);
signal MONO_INV_I			: bit;
signal CHROMA_BUS_I			: bit_vector(15 downto 0);
begin
	-- This reset is a relict of the original chipset and replaced by the RESETn input.
	--RESET_In <= '0' when SH_CSn = '0' and SH_LOADn = '0' else '1';
	RESET_In <= RESETn;
	SH_COLOR <= '0' when SH_BLANKn = '0' or SH_DE = '0' else '1'; -- The ST's COMP_SYNC signal.

	-- Three state data bus:
	SH_D_OUT(15 downto 8) <= DATA_OUT_SHMOD_REG when DATA_EN_SHMOD_REG = '1' else -- Hi byte decoding!
						 	 DATA_OUT_CR_REG(15 downto 8) when DATA_EN_CR_REG = '1' else -- This is the HSCROLL reg.
						 	 DATA_OUT_MICROWIRE(15 downto 8) when DATA_EN_MICROWIRE = '1' else
						 	 DATA_OUT_DMASOUND(15 downto 8) when DATA_EN_DMASOUND = '1' else (others => '0');
	SH_D_OUT(7 downto 0) <= DATA_OUT_SHIFT_REG when DATA_EN_SHIFT_REG = '1' else
							DATA_OUT_CR_REG(7 downto 0) when DATA_EN_CR_REG = '1' else
							DATA_OUT_MICROWIRE(7 downto 0) when DATA_EN_MICROWIRE = '1' else
							DATA_OUT_DMASOUND(7 downto 0) when DATA_EN_DMASOUND = '1' else (others => '0');

	SH_DATA_HI_EN <= DATA_EN_SHMOD_REG or DATA_EN_CR_REG or DATA_EN_MICROWIRE or DATA_EN_DMASOUND;
	SH_DATA_LO_EN <= DATA_EN_SHIFT_REG or DATA_EN_CR_REG or DATA_EN_MICROWIRE or DATA_EN_DMASOUND;

  	I_SHMODREG: WF25914IP_SHMOD_REG
		port map(
			CLK 				=> CLK,
			RESETn 				=> RESET_In,
			ADR					=> SH_A,
			CSn					=> SH_CSn,
        	RWn					=> SH_RWn,
			DATA_IN				=> SH_D_IN(15 downto 8), -- High byte only.
			DATA_OUT			=> DATA_OUT_SHMOD_REG,
			DATA_EN				=> DATA_EN_SHMOD_REG,
			MULTISYNC			=> MULTISYNC,
			SH_MOD			 	=> SH_MOD_I,
			xFF827E				=> xFF827E_D
		);

  	I_CRSHIFT: WF25914IP_CR_SHIFT_REG
    	port map(
			CLK		 		=> CLK, 
			RESETn 			=> RESET_In,
			ADR				=> SH_A,
			CSn				=> SH_CSn,
        	RWn				=> SH_RWn,
			LOADn 			=> SH_LOADn,
			DE 				=> SH_DE,
	  		SH_MOD 			=> SH_MOD_I,
      		DATA_IN 		=> SH_D_IN,
			DATA_OUT		=> DATA_OUT_SHIFT_REG,
			DATA_EN			=> DATA_EN_SHIFT_REG,
      		SR 				=> SR_I
    	);

  	I_REGS: WF25914IP_CR_REGISTERS
    	port map(
			CLK 			=> CLK,
			RESETn 			=> RESET_In,
			ADR				=> SH_A,
			CSn				=> SH_CSn,
			RWn				=> SH_RWn,
			DATA_IN			=> SH_D_IN,
			DATA_OUT		=> DATA_OUT_CR_REG,
			DATA_EN			=> DATA_EN_CR_REG,
			SH_MOD			=> SH_MOD_I,
			SR				=> SR_I,			
    		MONO_INV 		=> MONO_INV_I,
    		CHROMA			=> CHROMA_BUS_I
    	);

  	I_CROUT: WF25914IP_CR_OUT
		port map(
			CLK 			=> CLK,
			RESETn 			=> RESET_In,
			MONO_INV 		=> MONO_INV_I,
			BLANKn 			=> SH_BLANKn,
			DE 				=> SH_DE,
			LOADn 			=> SH_LOADn,
			SH_MOD1 		=> SH_MOD_I(1),
			SR0 			=> SR_I(0),
			CHROMA 			=> CHROMA_BUS_I,
			CR_1512		    => CR_1512,
			R 				=> SH_R,
			G 				=> SH_G,
			B 				=> SH_B,
			MONO 			=> SH_MONO
		);

	I_MICROWIRE: WF25914IP_MICROWIRE
  		port map(
			RESETn 			=> RESET_In,
			CLK		 		=> CLK,
			
			RWn 			=> SH_RWn,
			CMPCSn 			=> SH_CSn,
			ADR 			=> SH_A,
		    DATA_IN			=> SH_D_IN,
			DATA_OUT		=> DATA_OUT_MICROWIRE,
			DATA_EN			=> DATA_EN_MICROWIRE,

			MWK 			=> SH_MWK,
			MWD 			=> SH_MWD,
			MWEn 			=> SH_MWEn
  		);

	I_DMASOUND: WF25914IP_DMASOUND
		port map(
			RESETn 			=> RESET_In,
			CLK		 		=> CLK,
			ADR				=> SH_A,
			CSn				=> SH_CSn,
	       	RWn				=> SH_RWn,
			DATA_IN			=> SH_D_IN,
			DATA_OUT		=> DATA_OUT_DMASOUND,
			DATA_EN			=> DATA_EN_DMASOUND,

			DE 				=> SH_DE,

			SLOADn 			=> SH_SLOADn,
			SREQ 			=>	SH_SREQ,
					
			SCLK 			=> SH_SCLK,
			FCLK 			=>	SH_FCLK,

			SDATA_L 		=> SH_SDATA_L,
			SDATA_R 		=> SH_SDATA_R
		);
end STRUCTURE;
