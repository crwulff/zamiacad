-- komplettes RSA-Modul mit allen Komponenten
-- MIT STEUERWERK!
-- benutzt BEHAVIURAL des Montgomery-Multiplizierers
configuration CFG_RSA_TB_RTL of RSA_TB is
for SCHEMATIC
    for I_1 : RSA
      use configuration WORK.CFG_RSA_RTL;
    end for;
  end for;
end CFG_RSA_TB_RTL;
