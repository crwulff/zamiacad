--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer, Thomas Stanka
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : CFG_des
-- Purpose : Konfiguration die von CTRL benutzt wird um DES einzubinden !
-- 
-- File Name : CFG_des.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
--	17.12.98   |	changed the configuration to use RTL
--                 |	
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Created by Jens Kuenzer and Thomas Stanka for the first behavioral
--  
--------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;

library LIB_des;

 configuration CFG_des of des is
    use LIB_des.all;
--     for BEHAVIORAL  			-- the BEHAVIORAL - not synthetisable
     for RTL  				-- the version which is used for synthesis
     end for;  				
 end CFG_des;

library LIB_des;

configuration CFG_des_RTL of des is
    use LIB_des.all;  			-- RTL
     for RTL  				-- the version which is used for synthesis
     end for;
 end CFG_des_RTL;



