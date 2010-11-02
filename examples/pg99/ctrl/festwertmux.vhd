--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------

-- Designers :  Dirk Allmendinger 
-- Group     :  CTRL
--------------------------------------------------------------------------

-- Design Unit Name : Festwertmultiplexer
-- Purpose :
-- 
-- File Name : festwertmux.vhd
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

entity FESTWERTMUX is
      Port (    REIN : In    std_logic;
                 SEL : In    std_logic;
                   Q : Out   std_logic );
end FESTWERTMUX;

architecture BEHAVIORAL of FESTWERTMUX is

   begin

    Q <= REIN when SEL='0' else
	 '0';
    
end BEHAVIORAL;

configuration CFG_FESTWERTMUX_BEHAVIORAL of FESTWERTMUX is
   for BEHAVIORAL

   end for;

end CFG_FESTWERTMUX_BEHAVIORAL;
