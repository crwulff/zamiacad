--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Jens Kuenzer
-- Group     : CTRL
--------------------------------------------------------------------
-- Design Unit Name : CFG_CHIP
-- Purpose : configuration 
-- 
-- File Name : CFG_chip.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date  30.11.98  | Changes : Created
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Configuration des gesammten Kryptochips ohne Testbench
--  
--------------------------------------------------------------------------

library LIB_CTRL;
library LIB_DES;
library LIB_RSA;
configuration CFG_CHIP of CHIP is

   for SCHEMATIC
     for I_7: RSA_RESET_GEN
         use entity LIB_CTRL.RSA_RESET_GEN(BEHAVIORAL);
      end for;
      for I_IO: IO
         use configuration LIB_CTRL.CFG_IO;
      end for;
      for I_DES: DES_ENT
         use configuration LIB_DES.CFG_DES_RTL;
      end for;
      for I_MAINCTRL: MAINCTRL
         use configuration LIB_CTRL.CFG_MAINCTRL;
      end for;
      for I_RSA: RSA_ENT
         use configuration LIB_RSA.CFG_RSA;
      end for;
   end for;

end CFG_CHIP;


library LIB_CTRL;
library LIB_DES;
library LIB_RSA;
configuration CFG_CHIP_RSARTL of CHIP is

   for SCHEMATIC
      for I_7: RSA_RESET_GEN
         use entity LIB_CTRL.RSA_RESET_GEN(BEHAVIORAL);
      end for;
      for I_IO: IO
         use configuration LIB_CTRL.CFG_IO;
      end for;
      for I_DES: DES_ENT
         use configuration LIB_DES.CFG_DES;
      end for;
      for I_MAINCTRL: MAINCTRL
         use configuration LIB_CTRL.CFG_MAINCTRL;
      end for;
      for I_RSA: RSA_ENT
         use configuration LIB_RSA.CFG_RSA_RTL;
      end for;
   end for;

end CFG_CHIP_RSARTL;


library LIB_CTRL;
library LIB_DES;
library LIB_RSA;
configuration CFG_CHIP_RTL of CHIP is

   for SCHEMATIC
      for I_7: RSA_RESET_GEN
         use entity LIB_CTRL.RSA_RESET_GEN(BEHAVIORAL);
      end for;
      for I_IO: IO
         use configuration LIB_CTRL.CFG_IO;
      end for;
      for I_DES: DES_ENT
         use configuration LIB_DES.CFG_DES_RTL;
      end for;
      for I_MAINCTRL: MAINCTRL
         use configuration LIB_CTRL.CFG_MAINCTRL;
      end for;
      for I_RSA: RSA_ENT
         use configuration LIB_RSA.CFG_RSA_RTL;
      end for;
   end for;

end CFG_CHIP_RTL;


library LIB_CTRL;
configuration CFG_PADS of PADS is

   for SCHEMATIC
      for I_3, I_4: I2C_BUF
         use entity WORK.I2C_BUF(BEHAVIORAL);
      end for;
      for I_1: BIDI_BUF
         use entity LIB_CTRL.BIDI_BUF(BEHAVIORAL);
      end for;
      for I_2: CHIP
         use configuration WORK.CFG_CHIP;
      end for;
   end for;

end CFG_PADS;

configuration CFG_CHIP_PADS of CHIP is

   for BEHAVPADS
       for I_1: PADS
	   use configuration WORK.CFG_PADS;
       end for;
   end for;

end CFG_CHIP_PADS;
