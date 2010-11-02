--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : CFG_IO
-- Purpose : Configuration used by CFG_chip
-- 
-- File Name : CFG_io.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
--	07.12.98   |	Created
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  
--  
--------------------------------------------------------------------------

configuration CFG_IO of IO is

   for SCHEMATIC
      for I_2: SERIO
         use configuration WORK.CFG_SERIO_SCHEMATIC;
      end for;
      for I_1: PARIO
         use configuration WORK.CFG_PARIO_SCHEMATIC;
      end for;
   end for;

end CFG_IO;
