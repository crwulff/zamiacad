-- Benutzt rsa_BEHAV_MONT.vhd, welches den Montgomery-Multiplizierer
-- direkt mit Daten fuettert (KEIN STEUERWERK!)
-- benutzt BEHAVIOURAL des Montgomery-Multiplizierers
-- recht flott
configuration CFG_RSA_TB_BEHAV_MONT of RSA_TB is
for SCHEMATIC
    for I_1 : RSA
      use configuration WORK.CFG_RSA_BEHAV_MONT;
    end for;
  end for;
end CFG_RSA_TB_BEHAV_MONT;
