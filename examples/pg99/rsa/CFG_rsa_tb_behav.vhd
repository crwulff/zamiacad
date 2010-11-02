-- Benutzt rsa_BEHAV.vhd, RSA-Verschluesselung wird komplett
-- emuliert, nach der herkoemmlichen Methode.
configuration CFG_RSA_TB_BEHAV of RSA_TB is
  for SCHEMATIC
    for I_1 : RSA
      use entity WORK.RSA(BEHAV);
    end for;
  end for;

end CFG_RSA_TB_BEHAV;
