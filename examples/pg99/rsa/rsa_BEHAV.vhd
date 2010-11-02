--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Thomas Schwarz
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : RSA-Modul
-- Purpose :  RSA-module-core for the cryptochip "pg99", behavioural
-- 
-- File Name :  montgomery.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Simuliert das Verhalten des RSA-Moduls  zur Aussenwelt hin. Diese
--  architecture ist NICHT synthetisierbar.
--  Das RSA-Modul implementiert eine modulare Potenzierung, wie sie fuer
--  das RSA-Verfahren notwendig ist.
--  DATAIN:  Eingabebus
--  GO:      alle Daten fuer die Berechnung (M und X) wurden uebertragen,
--  	     Berechnung kann beginnen.
--  RESET:   Das RSA Modul muss vor JEDER Verschluesselung in den Grund-
--           zustand versetzt werden.
--  SELDATA: Gibt die Art der Daten am Eingabebus DATAIN an:
--           "00" : Modulus M
--           "01" : zu verschluesselndes Datenwort X
--           "10" : Exponent E
--           "11" : Ergebnis wird ausgelesen (DATAOUT)
--  VALACC:  Beim Lesen: gibt an, dass gueltige Daten am Eingabebus
--                       anliegen. Bei der Uebertragung vom M und X muessen
--                       die Daten 2 Takte lang am Eingabebus anliegen, erst
--                       danach duerfen neue Daten angelegt werden.
--       Beim Schreiben: gibt an, dass die Daten am Ausgabebus DATAOUT
--                       verarbeitet wurden, und dass jetzt neue Daten
--                       angelegt werden koennen. Nachdem VALACC auf '1'
--                       gegangen ist, stehen die naechsten Daten erst
--                       im uebernaechsten Takt zur Verfuegung!
--  DATAOUT: Ausgabebus
--  NEXTEXP: Der naechste Teil des Exponenten soll auf den Eingabebus
--           gelegt werden
--  READY:   die Verschluesselung ist beendet, die Daten koennen jetzt
--           32bit weise vom Ausgabebus geholt werden
-- Bei allen Datenuebertragungen muss das niederwertigste Bit zuerst ueber-
-- tragen werden. (least significant bit first!)
--------------------------------------------------------------------------


library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;




architecture BEHAV of rsa is

  constant NumBits : integer := 768;

  type TState is (Eingabe,Rechnung,Ausgabe);
  signal Y : std_logic_vector((NumBits-1) downto 0);
  signal Z : std_logic_vector((NumBits-1) downto 0);
  signal X : std_logic_vector((NumBits-1) downto 0);
  signal M : std_logic_vector((NumBits-1) downto 0);
  signal R : std_logic_vector((NumBits-1) downto 0);
  signal E : std_logic_vector(31 downto 0);
  signal EGO : std_logic := '0';
  signal ERDY : std_logic := '0';
  signal A : std_logic_vector((NumBits-1) downto 0);
  signal B : std_logic_vector((NumBits-1) downto 0);
  signal MULTGO : std_logic := '0';
  signal MULTRDY : std_logic := '0';
  signal EXPGO : std_logic := '0';
  signal EXPRDY : std_logic := '0';

begin
    DATAIO : process
    variable EGOzeroCheck : std_logic := '0';
    variable READY2 : std_logic;      -- damit ich das Signal selber lesen kann
    variable zustand : TState;
    begin
      	wait until CLK'event and CLK='1';
	READY <= '0';
	if RESET='1' then
	    EXPGO <= '0';
	    READY <= '0';
    	    READY2 := '0';
	    NEXTEXP <= '0';
	    ERDY <= '0';
	    zustand := Eingabe;
	else
	    if EXPRDY='1' and zustand=Rechnung then
		X((NumBits-33) downto 0) <= Y((NumBits-1) downto 32);
		READY <= '1';
		READY2 := '1';
		DATAOUT <= Y(31 downto 0);
		EXPGO <= '0';
		zustand := Ausgabe;
	    end if;
	    if GO='1' and zustand=Eingabe then
		EXPGO <= '1';
		zustand := Rechnung;
	    end if;
	    if VALACC='1' then
		case SELDATA is
		    when "00" =>
			M((NumBits-33) downto 0) <= M((NumBits-1) downto 32); 
			M((NumBits-1) downto (NumBits-32)) <= DATAIN;
			EXPGO <= '0';  -- implizites Reset!
			READY <= '0';
			READY2 := '0';
			NEXTEXP <= '0';
			ERDY <= '0';
			zustand := Eingabe;
			wait until CLK'event and CLK='1';
			READY <= '1';
		    when "01" =>
			X((NumBits-33) downto 0) <= X((NumBits-1) downto 32); 
			X((NumBits-1) downto (NumBits-32)) <= DATAIN;
			zustand := Eingabe;
			wait until CLK'event and CLK='1';
			READY <= '1';
		    when "10" =>
			E <= DATAIN;
			ERDY <= '1';
			zustand := Rechnung;
			NEXTEXP <= '0';
		    when "11" =>
			DATAOUT <= X(31 downto 0);
			X((NumBits-33) downto 0) <= X((NumBits-1) downto 32);
			READY <= '1';
			zustand := Ausgabe;
			wait until CLK'event and CLK='1';
			READY <= '0';
		    when others => NULL;
		end case;
	    end if;
	    if EGO='1' then
		if EGOzeroCheck='0' then
		    NEXTEXP <= '1';
		    ERDY <= '0';
		    EGOzeroCheck:='1';
		end if;
	    elsif EGOzeroCheck='1' then
		EGOzeroCheck:='0';
	    end if;
	end if;
    end process;



  EXP : process
  variable FIRSTONE : std_logic;
  begin
    -- Warten auf Potenzierauftrag
    wait until EXPGO'event and EXPGO='1';
    EXPRDY <= '0';
    FIRSTONE := '1';
    MULTGO <= '0';
    Z <= X;
    wait for 10 ns;
    EGO <= '1';
    wait until CLK'event and CLK='1';
    wait until CLK'event and CLK='1' and ERDY='1';
    EGO <= '0';

    -- Potenzierschleife
    for j in 0 to (NumBits-1)
    loop
      if j > 0 then
	if (j mod 32) = 0 then
	    -- neues Exponententeilwort holen
	    EGO <= '1';
	end if;
        -- quadrieren Z = Z * Z
        A <= Z;
        B <= Z;
        MULTGO <= '1';
        wait until MULTRDY'event and MULTRDY='1';

        Z <= R;
        MULTGO <= '0';
        wait for 10 ns;
	if (j mod 32) = 0 then
	    -- warten bis der Exponent da ist
	    if ERDY/='1' then
		wait until ERDY='1';
	    end if;
	    EGO <= '0';
	end if;
      end if;
      if E(j mod 32) = '1' then
        -- Z auf Y draufmultiplizieren
        if FIRSTONE = '1' then
          -- das erste Mal ist Y=1, d.h. kopieren reicht
          Y <= Z;
          FIRSTONE := '0';
          wait for 10 ns;
        else
        -- Y = Y * Z
          A <= Y;
          B <= Z;
          MULTGO <= '1';
          wait until MULTRDY'event and MULTRDY='1';

          Y <= R;
          MULTGO <= '0';
          wait for 10 ns;
        end if;
      end if;
    end loop;

    -- Ergebnis ausgeben
    wait for 10 ns;
    EXPRDY <= '1';
  end process;



  MULT : process
  variable REST : std_logic_vector(NumBits downto 0);
  variable TEMP : std_logic_vector(NumBits downto 0);
  begin
    -- Warten auf Multiplikationsauftrag
    wait until MULTGO'event and MULTGO='1';
    MULTRDY <= '0';
    REST := conv_std_logic_vector(0,NumBits+1);
    R <= REST((NumBits-1) downto 0);
    wait for 10 ns;

    for i in (NumBits-1) downto 0
    loop
      -- schieben
      REST := REST((NumBits-1) downto 0) & '0';
      TEMP := REST - ('0'&M);
      if TEMP(NumBits)='0' then
        -- TEMP > 0 => REST > M => M konnte abgezogen werden
        REST := TEMP;
      end if;
      if A(i)='1' then
        -- B dazuaddieren und wieder auf M pruefen
        REST := REST + ('0'&B);
        TEMP := REST - ('0'&M);
        if TEMP(NumBits)='0' then
          -- TEMP > 0 => REST > M => M konnte abgezogen werden
          REST := TEMP;
        end if;
      end if;
    end loop;

    -- Ergebnis ausgeben
    wait for 10 ns;
    R <= REST((NumBits-1) downto 0);
    wait for 10 ns;
    MULTRDY <= '1';
  end process;

end BEHAV;
