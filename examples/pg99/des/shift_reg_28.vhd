--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Markus Busch, Thomas Stanka
-- Group     : DES
--------------------------------------------------------------------
-- Design Unit Name : Schieberegister 28 Bit 
-- Purpose :
-- 
-- File Name :  shift_reg_28.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
--   24.11.98      |   01.12.98
--   16.12.98      |   changed the code for better synthesis.
--		   |   now the design uses only one FF. Thomas Stanka
--------------------------------------------------------------------------
--  28 bit Schiebe Register
--
--  Funktion CONTROL :
--     
--     00	 Einlesen der Daten
--     01	 1_Bit schieben
--     10	 0_bit schieben
--     11	 2_Bit schieben
--
-- en_decrypt :  dient zur Steuerung der Schieberichtung
--	         beim Ver-/Entschluesseln, d.h. Linksschieben beim
--		 Verschluesseln und Rechtsschieben beim Entschluesseln
--------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;

entity shift_reg_28 is
    
    port (d_in : in std_logic_vector(27 downto 0);
	  d_out : out std_logic_vector(27 downto 0);
	  write_en : in std_logic;  	-- Im Register speichern
	  en_decrypt : in std_logic;  	-- Schieberichtung
	  Control : in std_logic_vector(1 downto 0);  -- Anzahl der Schiebungen
	  reset : in std_logic;
	  clk : in std_logic);

end shift_reg_28;

-- purpose: 28 Bit Schieberegister mit rechts links schiebe Moeglichkeit
architecture behav of shift_reg_28 is
    signal speicher : STD_LOGIC_VECTOR(27 downto 0);  	-- der Speicher
begin  -- shift_reg_28
    process
    begin  -- process
	-- activities triggered by asynchronous reset (active low)
	if reset = '1' then
	    speicher <= (others => '0');
	    wait until clk'event and clk = '1';
	-- activities triggered by rising edge of clock
	else
	    if en_decrypt = '0' then  	-- verschlueseln
		case control is
		    when "00" => null;
		    when "01" => speicher(0) <= speicher(27);
				 speicher(27 downto 1) <= speicher(26 downto 0);
		    when "11" => speicher(1 downto 0) <= speicher(27 downto 26);
				 speicher(27 downto 2) <= speicher(25 downto 0);
		    when others => null;
		end case;
	    else 
		case control is
		    when "00" => null;
		    when "01" => speicher(26 downto 0) <= speicher(27 downto 1);
				 speicher(27) <= speicher(0);
		    when "11" => speicher(25 downto 0) <= speicher(27 downto 2);
				 speicher(27 downto 26) <= speicher(1 downto 0);
		    when others => null;
		end case;
	    end if;
	    if write_en = '1' then
		speicher <= d_in;
	    end if;
	     wait until clk'event and clk = '1';
        end if;
    end process;
    d_out <=speicher;
end behav;
