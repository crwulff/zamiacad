--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : main_FSM
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : main_FSM.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 10.12.98        | 10.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Der globale Automat der RSA-Gruppe. Von hier aus werden die anderen
--  beiden FSM's gesteuert, und die Steuerlkeitungen nach aussen gesetzt
--------------------------------------------------------------------------


library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;


entity main_FSM is
    
    port (CLK, RESET				: in std_logic;
	  VALACC, GO, Z48RDY, B48RDY, B48RDY47,
	  B768RDY, PrecalcRDY, MSB,
	  GetNextExp_RDY, E0, FINISHED		: in std_logic;
	  SELDATA				: in std_logic_vector(1 downto 0);
	  Z48RESET, Z48_INC, B48RESET,
	  B768RESET				: out std_logic;  -- Zaehlersteuerung
	  IN_LOHI, C_REG_M, C_OUT, C_REG_A ,ASel,
	  C_CLA_M, C_CLA_B, C_CLACARRY, Vz_M,
	  C_M_OUT_SEL, CARRY_IN, C_AB_CLA	: out std_logic;  -- Kontrollpunkte
	  EnSh_A, EnSh_B, EnSh_M		: out std_logic;  -- Register Enable-Ltg.
	  RESET_MONT, En_MONT			: out std_logic;  -- Montgomery Steuerung
	  Precalc_GO				: out std_logic;  -- SRT-Division
	  READY					: out std_logic;  -- Schnittstelle nach aussen
	  GetNextExp_GO, GetNextExp_Bit		: out std_logic;  -- Schnittstelle zu GetNextExp_fsm
	  C_REG_IN				: out std_logic_vector(1 downto 0)
	  );

end main_FSM;




-- purpose: Synthetisierbares Steuerwerk
architecture RTL of main_FSM is
    
    type TState is (idle, Read_M_Lo, Read_M_Hi, Read_M_Wait, Precalc_Init, Precalc_Init2,
		    Precalc, Precalc_Delay1, Precalc_Delay2, Read_X_Lo, Read_X_Hi,
		    Read_X_Wait, Wait_Precalc, Const_To_B_Init1, Const_To_B_Init2,
		    Const_To_B, M_To_B_Delay, MR_Conv_Init, NormB_Init1, NormB_Init2,
		    NormB, NormB_Delay1, NormB_Delay2, MR_Conv, Add_R_Init1, Add_R_Init2,
		    Add_R, Add_R_Delay, OneToA_Init1, OneToA_Init2, OneToA, InitLoop,
		    YMalZ_Init, ExpBitNull, YMalZ, Add_RA_Init1, Add_RA_Init2,
		    Add_RA, Add_RA_Delay, ZMalZ_Init, ZMalZ, Add_RB_Init1, Add_RB_Init2,
		    Add_RB, Add_RB_Delay ,SubPrepareCounter,
		    Sub_Erg_Init1, Sub_Erg_Init2, Sub_Erg, Sub_Erg_Delay1, Sub_Erg_Delay2,
		    Write_A, Write_A_Sh1, Write_A_Sh2,
		    Write_B, Write_B_Sh1, Write_B_Sh2);
    signal state, nextState : TState;  	-- Zustandsvariable
    attribute state_vector: string;
    attribute state_vector of RTL : architecture is "state";

begin  -- RTL

    -- purpose: Zustandsregister
    Zustandsuebergang : process
    begin  -- process Zustandsuebergang
	wait until CLK'event and CLK = '1';
	state <= nextState;
    end process Zustandsuebergang;


    Uebergangsfunktion: process(state, RESET,VALACC, GO, Z48RDY, B48RDY, B48RDY47,
				B768RDY, PrecalcRDY, MSB, GetNextExp_RDY, E0,
				FINISHED, SELDATA)
    begin  -- process Uebergangsfunktion

	Z48RESET <= '0';  		-- Alle Signale auf Null setzen!
	Z48_INC <= '0';
	B48RESET <= '0';
	B768RESET <= '0';
	IN_LOHI <= '0';
	C_REG_M <= '0';
	C_OUT <= '0';
	C_REG_A <= '0';
	C_AB_CLA <= '0';
	C_CLA_M <= '0';
	C_CLA_B <= '0';
	C_CLACARRY <= '0'; 
	EnSh_A <= '0';
	EnSh_B <= '0';
	EnSh_M <= '0';
	RESET_MONT <= '0';
	En_MONT <= '0';
	READY <= '0';
	GetNextExp_GO <= '0';
	GetNextExp_Bit <= '0';
	C_REG_IN <= (others => '0');
	ASel <= '0';
	C_M_OUT_SEL <= '0';
	PreCalc_GO <= '0';
	CARRY_IN <= '0';
	Vz_M <= '0';
	nextState <= state;  		-- Auch den Zustand initialisieren!!

	
	if RESET='1' then
	    nextState<=idle;
	    Z48RESET <= '-';  		-- Alle Signale auf Don't Care  setzen!
	    Z48_INC <= '-';
	    B48RESET <= '-';
	    B768RESET <= '-';
	    IN_LOHI <= '-';
	    C_REG_M <= '-';
	    C_OUT <= '-';
	    C_REG_A <= '-';
	    C_AB_CLA <= '-';
	    C_CLA_M <= '-';
	    C_CLA_B <= '-';
	    C_CLACARRY <= '-'; 
	    EnSh_A <= '-';
	    EnSh_B <= '-';
	    EnSh_M <= '-';
	    RESET_MONT <= '-';
	    En_MONT <= '-';
	    READY <= '-';
	    GetNextExp_GO <= '-';
	    GetNextExp_Bit <= '-';
	    C_REG_IN <= (others => '-');
	    ASel <= '-';
	    C_M_OUT_SEL <= '-';
	    PreCalc_GO <= '-';
	    CARRY_IN <= '-';
	    Vz_M <= '-';
	else
	    case state is
		when idle =>  		-- Hier wird auf Anfragen gewartet
		    Z48RESET <= '1';
		    if VALACC = '1' and SELDATA ="00" then
			nextState <= Read_M_Lo;
		    elsif VALACC = '1' and SELDATA ="01" then
			nextState <= Precalc_Init;
		    end if;
		when Read_M_Lo =>  	-- M einlesen
		    EnSh_M <= '1';
		    EnSh_B <= '1';
		    C_REG_IN <= "10";
		    nextState <= Read_M_Hi;
		when Read_M_Hi =>
		    READY <= '1';
		    EnSh_M <= '1';
		    IN_LOHI <= '1';
		    EnSh_B <= '1';
		    C_REG_IN <= "10";
		    nextState <= Read_M_Wait;
		when Read_M_Wait =>  	-- Warten auf naechste 32 Bit von M
					-- oder M bereits vollstaendig
		    Z48RESET <= '1';
		    if VALACC ='1' and SELDATA = "01" then
			nextState <= Precalc_Init;
		    elsif VALACC = '1' and SELDATA = "00" then
			nextState <= Read_M_Lo;
		    end if;
		when Precalc_Init =>  	-- -M wird nach B geschrieben und M
					-- dabei einmal durchrotiert, also
					-- steht danach in B -M und in M immer
					-- noch M!
		    Z48_INC <= '1';
		    C_REG_M <= '1';
		    EnSh_M <= '1';
		    C_M_OUT_SEL <= '1';
		    Vz_M <= '1';
		    C_CLA_M <= '1';
		    C_CLA_B <= '1';
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    nextState <= Precalc_Init2;
		when Precalc_Init2 =>  	-- Ab jetzt wird C_CLACARRY
		    Z48_INC <= '1';
		    C_REG_M <= '1';
		    EnSh_M <= '1';
		    Vz_M <= '1';
		    C_M_OUT_SEL <= '1';		    
		    C_CLA_M <= '1';
		    C_CLA_B <= '1';
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    C_CLACARRY <= '1';
		    nextState <= Precalc;		    
		when Precalc =>  	-- C_CLACARRY wird jetzt wieder
					-- geloescht und 48 Zyklen spaeter
					-- gehts weiter
		    Z48_INC <= '1';
		    C_REG_M <= '1';
		    EnSh_M <= '1';
		    Vz_M <= '1';
		    C_M_OUT_SEL <= '1';		    
		    C_CLA_M <= '1';
		    C_CLA_B <= '1';
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    if Z48RDY = '1' then
			nextState <= Precalc_Delay1;
		    end if;
		when Precalc_Delay1 =>  -- Pipeline-Zustand 1
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    nextState <= Precalc_Delay2;
		when Precalc_Delay2 =>  -- Pipeline-Zustand 2. Die Division
					-- wird hier durch Precalc_GO angeworfen
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    Precalc_GO <= '1';
		    nextState <= Read_X_Lo;		    
		when Read_X_Lo =>  	-- Die Daten werden gelesen. Die wird
					-- nur von der Kontrolle
					-- gesteuert (heisst wir zaehlen nicht mit)
		    EnSh_A <= '1';
		    C_REG_IN <= "00";
		    nextState <= Read_X_Hi;
		when Read_X_Hi =>
		    EnSh_A <= '1';
		    C_REG_IN <= "00";
		    IN_LOHI <= '1';
		    READY <= '1';  	-- wegen Moore kann READY bereits hier
					-- gesetzt werden
		    nextState <= Read_X_Wait;
		when Read_X_Wait =>  	-- mit dem Setzen von GO wird vom
					-- Dateneinlesen zum Arbeiten
					-- umgeschaltet. Abhaengig davon ob
					-- die  Division bereits fertig ist
					-- muessen wir darauf warten oder nicht
		    B48RESET <= '1';
		    if VALACC = '1' and SELDATA = "01" then
			nextState <= Read_X_Lo;
		    elsif GO = '1' and PrecalcRDY = '0' then
			nextState <= Wait_Precalc;
		    elsif GO = '1' and PrecalcRDY = '1' then
			nextState <= Const_To_B_Init1;
		    end if;
		when Wait_Precalc =>  	-- Falls die Division oben noch nicht
					-- fertig war, so wird hier darauf gewartet
		    B48RESET <= '1';
		    if PrecalcRDY = '1' then
			nextState <= Const_To_B_Init1;
		    end if;
		when Const_To_B_Init1 =>  -- Das Ergebnis der Division wird
					  -- jetzt nach B geschrieben
		    EnSh_B <= '1';
		    C_REG_IN <= "01";
		    GetNextExp_GO <= '1';  -- Hier wird der GetNextExp-Automat
					   -- angeworfen. Dieser sorgt von nun
					   -- an parallel dafuer dass immer
					   -- das richtige Exponentenbit auf
					   -- E0 liegt
		    nextState <= Const_To_B_Init2;
		when Const_To_B_Init2 =>
		    EnSh_B <= '1';
		    C_REG_IN <= "01";
		    C_CLACARRY <= '1';
		    nextState <= Const_To_B;
		when Const_To_B =>  	-- nach 48 Zyklen ist B voll
		    EnSh_B <= '1';
		    C_REG_IN <= "01";
		    if B48RDY = '1'  then
			nextState <= M_To_B_Delay;
		    end if;
		when M_To_B_Delay =>  	-- ein weiterer Pipeline-Zustand
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    B48RESET <= '1';
		    B768RESET <= '1';
		    if MSB = '1' then  	-- B war negativ => Normaliesieren
			nextState <= NormB_Init1;
		    elsif MSB = '0' then  -- B war OK, d.h. weiter Konv. vonA
			nextState <= MR_Conv_Init;
		    end if;
		when NormB_Init1 =>  	-- Normalisierung von B (B:=B+M)
		    assert false report "M_To_B_Delay: MSB = '1'! NormB_Init1" severity note;
		    C_REG_M <= '1';
		    EnSh_M <= '1';
		    C_CLA_M <= '1';
		    C_CLA_B <= '1';
		    EnSh_B <= '1';
		    C_REG_IN <= "01";
		    nextState <= NormB_Init2;
		when NormB_Init2 =>
		    C_REG_M <= '1';
		    EnSh_M <= '1';
		    C_CLA_M <= '1';
		    C_CLA_B <= '1';
		    EnSh_B <= '1';
		    C_REG_IN <= "01";
		    C_CLACARRY <= '1';  -- !!
		    nextState <= NormB;
		when NormB =>
		    C_REG_M <= '1';
		    EnSh_M <= '1';
		    C_CLA_M <= '1';
		    C_CLA_B <= '1';
		    EnSh_B <= '1';
		    C_REG_IN <= "01";
		    if B48RDY47 = '1' then  -- Es darf nur 47!!! Zyklen ein
					    -- EnSh_M geben, da sonst M zuweit
					    -- rotiert wird
			nextState <= NormB_Delay1;
		    end if;
		when NormB_Delay1 =>  	-- EnSh_M muss hier abgeschaltet werden
		    C_REG_M <= '1';
		    C_CLA_M <= '1';
		    C_CLA_B <= '1';
		    EnSh_B <= '1';
		    C_REG_IN <= "01";
		    nextState <= NormB_Delay2;
		when NormB_Delay2 =>
		    EnSh_B <= '1';
		    C_REG_IN <= "01";
		    B768RESET <= '1';
		    nextState <= MR_Conv_Init;
		when MR_Conv_Init =>  	-- hier gehts weiter wenn B OK war. A
					-- wird nun in die Montgomery-Residue
					-- konvertiert, d.h. B := B*A
		    assert false report "M_To_B_Delay: MSB = '0'! MR_Conv_Init" severity note;
		    RESET_MONT <= '1';
		    nextState <= MR_Conv;		    
		when MR_Conv =>
		    En_MONT <= '1';
		    B48RESET <= '1';
		    if B768RDY = '1' then
			nextState <= Add_R_Init1;
		    end if;
		when Add_R_Init1 =>  	-- Konvertierung fertig, nur noch
					-- Ergebnis nach B schreiben (48 Zyklen)
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    nextState <= Add_R_Init2;
		when Add_R_Init2 =>
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    C_CLACARRY <= '1';  	-- !!
		    nextState <= Add_R;		    
		when Add_R =>
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    if B48RDY = '1' then
			nextState <= Add_R_Delay;
		    end if;		    
		when Add_R_Delay =>  	-- Pipeline-Zustand
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    B48RESET <= '1';
		    nextState <= OneToA_Init1;
		when OneToA_Init1 =>  	-- Die fuer die Potenzierung notwendig
					-- Eins wird jetzt nach A geladen
		    C_REG_IN <= "11";
		    EnSh_A <= '1';
		    nextState <= OneToA_Init2;
		when OneToA_Init2 =>  	-- Die Eins wird zweimal
					-- reingeschrieben, weil einmal zu oft
					-- geschoben wird
		    C_REG_IN <= "11";
		    EnSh_A <= '1';
		    nextState <= OneToA;		    
		when OneToA =>  	-- Die oberen Bits werden mit Null gefuellt
		    C_REG_IN <= "10";
		    EnSh_A <= '1';
		    if B48RDY = '1' then
			nextState <= InitLoop;
		    end if;
		when InitLoop =>  	-- Potenzierschleife
		    B768RESET <= '1';
		    if GetNextExp_RDY = '1' and E0 = '1' then
			nextState <= YMalZ_Init;  -- Z:= Y*Z, danach Z:=Z*Z
		    elsif GetNextExp_RDY = '1' and E0 = '0' then
			nextState <= ExpBitNull;  -- weiter mit Z:=Z*Z
		    end if;
		when ExpBitNull =>  	-- Das naechste Bit wird angefordert
		    GetNextExp_Bit <= '1';
		    B768RESET <= '1';
		    nextState <= ZMalZ_Init;
		when YMalZ_Init =>  	-- Y*Z
		    RESET_MONT <= '1';
		    GetNextExp_Bit <= '1';  -- Das naechste Bit wird angefordert
		    nextState <= YMalZ;
		when YMalZ =>  		-- nach 768 Takten ist Y*Z berechnet
		    En_MONT <= '1';
		    B48RESET <= '1';
		    if B768RDY = '1' then
			nextState <= Add_RA_Init1;
		    end if;
		when Add_RA_Init1 =>  	-- Ergebnis wird nach A geschrieben
		    C_REG_IN <= "01";
		    EnSh_A <= '1';
		    nextState <= Add_RA_Init2;
		when Add_RA_Init2 =>
		    C_REG_IN <= "01";
		    EnSh_A <= '1';
		    C_CLACARRY <= '1';
		    nextState <= Add_RA;		    
		when Add_RA =>
		    C_REG_IN <= "01";
		    EnSh_A <= '1';
		    if B48RDY = '1' then
			nextState <= Add_RA_Delay;
		    end if;		    
		when Add_RA_Delay =>  	--Pipeline-Zustand
		    C_REG_IN <= "01";
		    EnSh_A <= '1';
		    B768RESET <= '1';
		    nextState <= ZMalZ_Init;		    
		when ZMalZ_Init =>  	-- Z:=Z*Z
		    RESET_MONT <= '1';
		    B48RESET <= '1';
		    ASel <= '1';
		    if FINISHED = '0' then
			nextState <= ZMalZ;  -- weiter im loop
		    elsif FINISHED = '1' then
			nextState <= SubPrepareCounter;  -- Fertig? dann weiter mit Ergebnisausgabe
		    end if;
		when ZMalZ =>
		    En_MONT <= '1';
		    B48RESET <= '1';
		    ASel <= '1';
		    if B768RDY = '1' then
			nextState <= Add_RB_Init1;
		    end if;
		when Add_RB_Init1 =>  	-- Ergebnis wird nach B
					-- geschrieben
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    nextState <= Add_RB_Init2;
		when Add_RB_Init2 =>
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    C_CLACARRY <= '1';
		    nextState <= Add_RB;	    
		when Add_RB =>
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    if B48RDY = '1' then
			nextState <= Add_RB_Delay;
		    end if;		    
		when Add_RB_Delay =>  	-- Pipeline-Zustand
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    nextState <= InitLoop;  -- Ende der Schleife
		when SubPrepareCounter =>  -- Der Z48 Zaehler wird resetet.
					   -- Dafuer ist ein extra zustand
					   -- notwendig, da der Zaehler im
					   -- vorherigen Zustand gebraucht wird
		    Z48RESET <= '1';
		    nextState <= Sub_Erg_Init1;		    
		when Sub_Erg_Init1 =>  	-- Ergebniskonvertierung, A kann
					-- groesser als M sein! 768-Bit
					-- Vergleicher nicht sinnvoll, deshalb
					-- A-M nach B ausrechnen
		    Z48_INC <= '1';
		    C_REG_M <= '1';
		    EnSh_M <= '1';
		    Vz_M <= '1';
		    CARRY_IN <= '1';
		    C_CLA_M <= '1';
		    C_CLA_B <= '1';
		    C_REG_IN <= "01";
		    EnSh_A <= '1';
		    EnSh_B <= '1';  	-- A wird einmal rotiert
		    C_REG_A <= '1';  	-- damit der Wert erhalten bleibt
		    C_AB_CLA <= '1';
		    nextState <= Sub_Erg_Init2;
		when Sub_Erg_Init2 =>
		    Z48_INC <= '1';
		    C_REG_M <= '1';
		    EnSh_M <= '1';
		    Vz_M <= '1';
		    CARRY_IN <= '1';		    
		    C_CLA_M <= '1';
		    C_CLA_B <= '1';
		    C_REG_IN <= "01";
		    EnSh_A <= '1';
		    EnSh_B <= '1';  	-- A wird einmal rotiert
		    C_REG_A <= '1';  	-- damit der Wert erhalten bleibt
		    C_AB_CLA <= '1';		    
		    C_CLACARRY <= '1';	-- !!
		    nextState <= Sub_Erg;		    
		when Sub_Erg =>
		    Z48_INC <= '1';
		    C_REG_M <= '1';
		    EnSh_M <= '1';
		    Vz_M <= '1';
		    CARRY_IN <= '1';		    
		    C_CLA_M <= '1';
		    C_CLA_B <= '1';
		    C_REG_IN <= "01";
		    EnSh_A <= '1';
		    EnSh_B <= '1';  	-- A wird einmal rotiert
		    C_REG_A <= '1';  	-- damit der Wert erhalten bleibt
		    C_AB_CLA <= '1';		    
		    if Z48RDY = '1' then
			nextState <= Sub_Erg_Delay1;
		    end if;		    
		when Sub_Erg_Delay1 =>
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    Z48RESET <= '1';
		    nextState <= Sub_Erg_Delay2;
		when Sub_Erg_Delay2 =>
		    C_REG_IN <= "01";
		    EnSh_B <= '1';
		    if MSB = '1' then
			nextState <= Write_A;  -- A war kleiner, also A ausgeben
		    elsif MSB = '0' then
			nextState <= Write_B;  -- A war groesser, also B ausgeben
		    end if;
		when Write_A =>  	-- Ausgabe A
		    assert false report "Sub_Erg_Delay2: MSB = '1'! Write_A" severity note;
		    C_OUT <= '1';
		    READY <= '1';
		    if VALACC = '1' and SELDATA = "11" then
			nextState <= Write_A_Sh1;
		    elsif VALACC = '1' and SELDATA = "00" then
			nextState <= Read_M_Lo;
		    elsif VALACC = '1' and SELDATA = "01" then
			nextState <= Precalc_Init;			
		    end if;
		when Write_A_Sh1 =>
		    EnSh_A <= '1';
		    Z48_INC <= '1';
		    if VALACC = '1' and SELDATA = "00" then
			nextState <= Read_M_Lo;
		    elsif VALACC = '1' and SELDATA = "01" then
			nextState <= Precalc_Init;			
		    else
			nextState <= Write_A_Sh2;  -- Weitere 32 Bit ausgeben
		    end if;
		when Write_A_Sh2 =>
		    EnSh_A <= '1';
		    Z48_INC <= '1';
		    if VALACC = '1' and SELDATA = "00" then
			nextState <= Read_M_Lo;
		    elsif VALACC = '1' and SELDATA = "01" then
			nextState <= Precalc_Init;			
		    else
			if Z48RDY = '1' then
			    nextState <= idle;         -- Fertig! Zurueck zum nichts tun!
			elsif Z48RDY = '0' then
			    nextState <= Write_A;      -- Weitere 32 Bit ausgeben
			end if;		
		    end if;		    
	        when Write_B =>  	-- Ausgabe B
		    assert false report "Sub_Erg_Delay2: MSB = '0'! Write_B" severity note;
		    READY <= '1';
		    if VALACC = '1' and SELDATA = "11" then
			nextState <= Write_B_Sh1;
		    elsif VALACC = '1' and SELDATA = "00" then
			nextState <= Read_M_Lo;
		    elsif VALACC = '1' and SELDATA = "01" then
			nextState <= Precalc_Init;
		    end if;		    
		when Write_B_Sh1 =>
		    EnSh_B <= '1';
		    Z48_INC <= '1';
		    if VALACC = '1' and SELDATA = "00" then
			nextState <= Read_M_Lo;
		    elsif VALACC = '1' and SELDATA = "01" then
			nextState <= Precalc_Init;			
		    else		    
			nextState <= Write_B_Sh2; -- Weitere 32 Bit ausgeben
		    end if;
		when Write_B_Sh2 =>
		    EnSh_B <= '1';
		    Z48_INC <= '1';
		    if VALACC = '1' and SELDATA = "00" then
			nextState <= Read_M_Lo;
		    elsif VALACC = '1' and SELDATA = "01" then
			nextState <= Precalc_Init;			
		    else		    
			if Z48RDY = '1' then
			    nextState <= idle;	 -- Fertig! Zurueck zum nichts tun!
			elsif Z48RDY = '0' then		    
			    nextState <= Write_B;    -- Weitere 32 Bit ausgeben
			end if;
		    end if;
	    end case;
	end if;
    end process Uebergangsfunktion;
    
end RTL;

