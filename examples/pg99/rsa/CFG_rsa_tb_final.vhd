-- Endgueltige Version mit allem drum und dran
configuration CFG_RSA_TB_FINAL of RSA_TB is
  for SCHEMATIC
    for I_1 : RSA
      use configuration WORK.CFG_RSA_FINAL;
    end for;
  end for;

end CFG_RSA_TB_FINAL;
