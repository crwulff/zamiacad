-- Benutzt rsa_BEHAV_MONT.vhd, welches den Montgomery-Multiplizierer
-- direkt mit Daten fuettert (KEIN STEUERWERK!)
-- benutzt RTL des Montgomery-Multiplizierers
-- sehr lahm!
configuration CFG_RSA_TB_BEHAV_MONT_RTL of RSA_TB is
for SCHEMATIC
    for I_1 : RSA
      use configuration WORK.CFG_RSA_BEHAV_RTL;
    end for;
  end for;
end CFG_RSA_TB_BEHAV_MONT_RTL;
