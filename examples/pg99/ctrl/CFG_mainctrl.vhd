
library LIB_GENERATORS;

configuration CFG_MAINCTRL of MAINCTRL is

   for SCHEMATIC
      for I_FSM: FSM
         use entity WORK.FSM(BEHAVIORAL);
--	  for BEHAVIORAL
--	      for I_RAM_COUNT
--	        use entity WORK.RAM_COUNT(BEHAVIORAL);
--	      end for;
--	  end for;
      end for;
      for I_RIP1, I_RIP2: RIP
         use entity WORK.RIP(BEHAVIORAL);
      end for;
      for I_DEMUX1, I_DEMUX2: DEMUX
         use entity WORK.DEMUX(BEHAVIORAL);
      end for;
      for I_ROM: NAME_ROM
         use entity WORK.NAME_ROM(BEHAVIORAL);
      end for;
      for I_RAM: SPS2_256X32M4
         use entity LIB_GENERATORS.SPS2_256X32M4(BEHAVIORAL);
      end for;
      for I_REG1, I_REG2, I_REG3, I_REG4, I_REG5: D_REG
         use entity WORK.D_REG(BEHAVIORAL);
      end for;
      for I_BUFF1, I_BUFF2, I_BUFF3: BIDI_BUF
         use entity WORK.BIDI_BUF(BEHAVIORAL);
      end for;
      for I_LFSR: LFSR
         use entity WORK.LFSR(BEHAVIORAL);
      end for;
      for I_EQ: EQ
         use entity WORK.EQ(BEHAVIORAL);
      end for;
      for I_DES_IN, I_DES_OUT: REG_CTRL
         use entity WORK.REG_CTRL(BEHAVIORAL);
      end for;
      for I_BUSY: SYNC_FF
         use entity WORK.SYNC_FF(BEHAVIORAL);
      end for;
   end for;

end CFG_MAINCTRL;
