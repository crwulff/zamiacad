--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------

-- Designers :  Dirk Allmendinger 
-- Group     :  CTRL
--------------------------------------------------------------------------

-- Design Unit Name : serielles Schieberegister 
-- Purpose :
-- 
-- File Name : sershiftreg.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 98
--------------------------------------------------------------------
-- Date		   | Changes 
--	17.11.1998 | 	27.01.1999
--                 | 
--                 |
-----------------------------------------------------------------------




   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;


entity SERSHIFTREG is
      Port (    MODE : In    std_logic;
               RESET : In    std_logic;
                CLK : In    std_logic;
	       SHIFT : In  std_logic;
		SERINEN : in std_logic;
	   
               SDAIN : In    std_logic;
              SPARIN : In    std_logic_vector (7 downto 0);
             SREGINEN : In    std_logic;
             SREGOUTEN : In    std_logic;
              SDAOUT : Out   std_logic;
             SPAROUT : Out   std_logic_vector (7 downto 0);
                SSEL : Out   std_logic_vector (2 downto 0) );
end SERSHIFTREG;

architecture SCHEMATIC of SERSHIFTREG is
signal reg : std_logic_vector(7 downto 0);  -- Zwischenspeicher Schiebereg
signal regzw : std_logic_vector(7 downto 0);  -- Zwischenspeicher Schiebereg
-- signal regkill : std_logic_vector(6 downto 0);
signal SAUS : std_logic;  		-- SDAOUT Zwischenspeicher
signal bindran : std_logic;  		-- ADRESS VALID
signal serialadrress : std_logic_vector(5 downto 0);  -- serielle Adresse
signal SCLK_ZW : std_logic;
signal SIN : std_logic;
-- signal notsclk : std_logic;

begin


    serialadrress(5 downto 0) <= "010000";  -- Serielle Adresse festlegen
    
    
    regzw(7 downto 0) <= "00000000";

-- purpose: Schieberegister
-- type:    memorizing
-- inputs:  clk, reset
-- outputs: 
schieben : process (clk, reset, shift)
    
begin  -- process shift
    -- activities triggered by asynchronous reset (active low)
--     if reset = '1' then
-- 	reg(7 downto 0) <= "00000000";
-- 	SIN <= '0';
    -- activities triggered by rising edge of clock
--     else
	if clk'event and clk = '1' then
          if reset = '1' then
	      SIN <= '0';
	      reg <= "00000000";
	  else
	      
	    if shift ='1'  then
	    reg(7 downto 1) <= reg(6 downto 0);
	    reg(0) <= SIN;
-- 	    SAUS <= reg(7);
	    if reg(7 downto 2) = serialadrress(5 downto 0)  then
		bindran <= '1';
	    else
		bindran <= '0';
	    end if;
	  else
	    reg <= reg;
	    SIN <= SIN;
	    if reg(7 downto 2) = serialadrress(5 downto 0)  then
		bindran <= '1';
	    else
		bindran <= '0';
	    end if;
	  end if;
	end if;
        if serinen = '1'  then
	    SIN <= SDAIN;
        end if;
	if SREGINEN = '1' then
	   reg <= SPARIN;
	end if;
      
    end if;
    
    
end process schieben;
    
SDAOUT <= reg(7) when mode='1' else
	  '1';

SPAROUT(7 downto 0) <= reg(7 downto 0) when SREGOUTEN='1' else
		       "00000000";

SSEL(0) <= '1' when bindran='1' else
	   '0';

SSEL(1) <= reg(0);
SSEL(2) <= reg(1);

end SCHEMATIC;
 

configuration CFG_SERSHIFTREG_SCHEMATIC of SERSHIFTREG is

   for SCHEMATIC
   end for;

end CFG_SERSHIFTREG_SCHEMATIC;
