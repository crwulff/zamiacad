
--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------

-- Designers :  Dirk Allmendinger 
-- Group     :  CTRL
--------------------------------------------------------------------------

-- Design Unit Name : synchrone parallele Eingabe (8,16,32 Bit auf 32 bit)
-- Purpose :
-- 
-- File Name : parein.vhd
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
 
entity PAREIN is
      Port (     CLK : In    std_logic;
             MODESEL : In    std_logic_vector (1 downto 0);
                  OE : In    std_logic;
	    CTRLINEN : In    std_logic;
               PARIN : In    std_logic_vector (31 downto 0);
               REGEN : In    std_logic_vector (3 downto 0);
               RESET : In    std_logic;
               SERIN : In    std_logic_vector (7 downto 0);
                   Y : InOut std_logic_vector (31 downto 0);
	     CTRLOUT : Out   std_logic_vector (7 downto 0));
end PAREIN;

architecture SCHEMATIC of PAREIN is
signal y_out, reg :  std_logic_vector (31 downto 0);
signal CTRL, CTRL_OUT : std_logic_vector(7 downto 0);

begin

  reg <= serin & serin & serin & serin when modesel="00" else
	 parin(7 downto 0) & parin(7 downto 0) &
	 parin(7 downto 0) & parin(7 downto 0) when modesel="01" else
	 parin(15 downto 0) & parin(15 downto 0) when modesel="10" else
	 parin(31 downto 0);


  CTRL <= reg(7 downto 0);
  
  process (clk, reset)
    begin
      if reset='1' then
	   y_out(31 downto 0) <= "00000000000000000000000000000000";
      else
	  if clk'event and clk = '1' then
	      if regen(0)='1' then
		  y_out(7 downto 0) <= reg(7 downto 0);
	      end if;

	      if regen(1)='1' then
		  y_out(15 downto 8) <= reg(15 downto 8);
	      end if;

	      if regen(2)='1' then
		  y_out(23 downto 16) <= reg(23 downto 16);
	      end if;

	      if regen(3)='1' then
		  y_out(31 downto 24) <= reg(31 downto 24);
	      end if;
          end if;
      end if;

 end process;

 y <= y_out when oe='1' else (others => 'Z');

 stausreg : process (CLK, RESET)
     
 begin  -- process stausreg
     -- activities triggered by asynchronous reset (active high)
     if RESET = '1' then
	 CTRL_OUT <= "00000000";
     -- activities triggered by rising edge of clock
     elsif CLK'event and CLK = '1' then
	 if CTRLINEN = '1' then
	     CTRL_OUT <= CTRL;
	 end if;
     end if;
 end process stausreg;

CTRLOUT <= CTRL_OUT;
      
end SCHEMATIC;



configuration CFG_PAREIN_SCHEMATIC of PAREIN is

   for SCHEMATIC
   end for;

end CFG_PAREIN_SCHEMATIC;



