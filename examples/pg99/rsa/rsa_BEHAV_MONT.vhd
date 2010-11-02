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




architecture BEHAV_MONT of rsa is

  constant NumBits : integer := 768;

  component MONTGOMERY
  port( AI     : in std_logic;
        B      : in std_logic_vector ((NumBits-1) downto 0);
        M      : in std_logic_vector ((NumBits-1) downto 0);
        SRTREM : in std_logic;
        CLK    : in std_logic;
        RESET  : in std_logic; 
	ENABLE : in std_logic;
	RSAVE  : out std_logic_vector ((NumBits-1) downto 0);
	RCARRY : out std_logic_vector ((NumBits-1) downto 0)
      );
  end component;
  
  type TState is (Eingabe,Rechnung,Ausgabe);
  signal Y : std_logic_vector((NumBits-1) downto 0);
  signal Z : std_logic_vector((NumBits-1) downto 0);
  signal X : std_logic_vector((NumBits-1) downto 0);
  signal M : std_logic_vector((NumBits-1) downto 0);
  signal R : std_logic_vector((NumBits-1) downto 0);
  signal E : std_logic_vector(31 downto 0);
  signal EGO : std_logic := '0';
  signal ERDY : std_logic := '0';
--  signal A : std_logic_vector((NumBits-1) downto 0);
  signal B : std_logic_vector((NumBits-1) downto 0);
--  signal MULTGO : std_logic := '0';
--  signal MULTRDY : std_logic := '0';
  signal EXPGO : std_logic := '0';
  signal EXPRDY : std_logic := '0';

  signal AI     : std_logic;
--  signal B      : std_logic_vector ((NumBits-1) downto 0);
--  signal M      : std_logic_vector ((NumBits-1) downto 0);
  signal SRTREM : std_logic;
--  signal CLK    : std_logic := '0';
  signal RESET_MONT : std_logic;
  signal ENABLE : std_logic;
  signal RSAVE  : std_logic_vector ((NumBits-1) downto 0);
  signal RCARRY : std_logic_vector ((NumBits-1) downto 0);

  
begin
    UM1: MONTGOMERY port map (AI,B,M,SRTREM,CLK,RESET_MONT,ENABLE,RSAVE,RCARRY);

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
  variable A : std_logic_vector((NumBits-1) downto 0);
  variable tmp : std_logic_vector((NumBits-1) downto 0);
  variable tmp1 : std_logic_vector((NumBits-1) downto 0);
  variable tmp2 : std_logic_vector((NumBits-1) downto 0);
--  variable mults_Y,mults_Z : integer;
  begin
    -- Warten auf Potenzierauftrag
    wait until EXPGO'event and EXPGO='1';
    EXPRDY <= '0';
    FIRSTONE := '1';
--    mults_Y := 0;
--    mults_Z := 0;
    wait until CLK'event and CLK='1';
    -- erstes Exponentenwort holen
    EGO <= '1';

    -- SRT-Division fuer Konvertierungsfaktor
    A := conv_std_logic_vector(0,NumBits);
    AI <= '0';
    tmp := '0'&M(NumBits-1 downto 1);
    B <= not(tmp);
    SRTREM <= '1';
    RESET_MONT <= '1';
    ENABLE <= '0';
    wait until CLK'event and CLK='1';
    RESET_MONT <= '0';
    for i in 0 to NumBits+4
    loop
      ENABLE <= '0';
      RESET_MONT <= '0';
      wait until CLK'event and CLK='1';
      ENABLE <= '1';
      RESET_MONT <= '1';
      wait until CLK'event and CLK='1';
    end loop;
    RESET_MONT <= '0';
    wait until CLK'event and CLK='1';
    ENABLE <= '0';
    SRTREM <= '0';
    wait until CLK'event and CLK='1';
    tmp1 := RSAVE;
    tmp2 := RCARRY;
    tmp := tmp1 + tmp2;
    if (tmp(NumBits-1) = '0') then
      Z <= tmp;
    else
      Z <= tmp + M;
    end if;
    wait until CLK'event and CLK='1';

    -- X in Montgomery-Residue konvertieren
    B <= X;
    A := Z;
    SRTREM <= '0';
    RESET_MONT <= '1';
    ENABLE <= '0';
    AI <= A(0);
    wait until CLK='1' and CLK'event;
    RESET_MONT <= '0';
    ENABLE <= '1';
    for i in 1 to NumBits-1
    loop
      AI <= A(i);
      wait until CLK'event and CLK='1';
    end loop;
    AI <= '0';
    wait until CLK'event and CLK='1';
    wait until CLK'event and CLK='1';
    wait until CLK'event and CLK='1';
    ENABLE <= '0';
    wait until CLK'event and CLK='1';
    tmp1 := RSAVE;
    tmp2 := RCARRY;
    tmp := tmp1 + tmp2;
    Z <= tmp;
    -- Y mit EINS initialisieren
    Y <= conv_std_logic_vector(1,NumBits);
--    mults_Z := 1;
    wait until CLK'event and CLK='1';
--    assert tmp < M report "KONV: Y groesser M" severity note;
    
    -- warten bis der Exponent da ist
    if ERDY/='1' then
	wait until ERDY='1';
    end if;
    EGO <= '0';
    wait until CLK'event and CLK='1';

    -- Potenzierschleife
    for j in 0 to (NumBits-1)
    loop
      if j > 0 then
	if (j mod 32) = 0 then
	    -- neues Exponententeilwort holen
	    EGO <= '1';
	end if;
        -- quadrieren Z = Z * Z
	B <= Z;
	A := Z;
	SRTREM <= '0';
	RESET_MONT <= '1';
	ENABLE <= '0';
	AI <= A(0);
	wait until CLK='1' and CLK'event;
	RESET_MONT <= '0';
	ENABLE <= '1';
	for i in 1 to NumBits-1
	loop
	    AI <= A(i);
	    wait until CLK'event and CLK='1';
	end loop;
	AI <= '0';
	wait until CLK'event and CLK='1';
	wait until CLK'event and CLK='1';
	wait until CLK'event and CLK='1';
	ENABLE <= '0';
	wait until CLK'event and CLK='1';
	tmp1 := RSAVE;
	tmp2 := RCARRY;
	tmp := tmp1 + tmp2;
	Z <= tmp;
--	mults_Z := mults_Z + 1;
	wait until CLK'event and CLK='1';
--	assert tmp < M report "Z*Z: Z groesser M" severity note;

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
--        if FIRSTONE = '1' then
--          -- das erste Mal ist Y=1, d.h. kopieren reicht
--          Y <= Z;
--	FIRSTONE := '0';
--	  wait until CLK='1' and CLK'event;
--	else
	    -- Y = Y * Z
	    B <= Z;
	    A := Y;
	    SRTREM <= '0';
	    RESET_MONT <= '1';
	    ENABLE <= '0';
	    AI <= A(0);
	    wait until CLK='1' and CLK'event;
	    RESET_MONT <= '0';
	    ENABLE <= '1';
	    for i in 1 to NumBits-1
	    loop
		AI <= A(i);
		wait until CLK'event and CLK='1';
	    end loop;
	    AI <= '0';
	    wait until CLK'event and CLK='1';
	    wait until CLK'event and CLK='1';
	    wait until CLK'event and CLK='1';
	    ENABLE <= '0';
	    wait until CLK'event and CLK='1';
	    tmp1 := RSAVE;
	    tmp2 := RCARRY;
	    tmp := tmp1 + tmp2;
	    Y <= tmp;
--	    mults_Y := mults_Y + mults_Z + 1;
	    wait until CLK'event and CLK='1';
--	    assert tmp < M report "Y*Z: Y groesser M" severity note;
--	end if;
      end if;
    end loop;

--    -- Ergebnis in normalen Zahlenbereich zurueckkonvertieren
--    B <= conv_std_logic_vector(1,NumBits);
--    A := Y;
--    SRTREM <= '0';
--    RESET_MONT <= '1';
--    ENABLE <= '0';
--    AI <= A(0);
--    wait until CLK='1' and CLK'event;
--    RESET_MONT <= '0';
--    ENABLE <= '1';
--    for i in 1 to NumBits-1
--    loop
--	AI <= A(i);
--	wait until CLK'event and CLK='1';
--    end loop;
--    AI <= '0';
--    wait until CLK'event and CLK='1';
--    wait until CLK'event and CLK='1';
--    wait until CLK'event and CLK='1';
--    ENABLE <= '0';
--    wait until CLK'event and CLK='1';
--    tmp1 := RSAVE;
--    tmp2 := RCARRY;
--    tmp := tmp1 + tmp2;
    tmp := Y;
    Z <= tmp;
    Y <= tmp - M;
    wait until CLK'event and CLK='1';

    if (Y(NumBits-1) = '1') then
	-- Z ist kleiner als M und Y negativ
	-- -> Z ausgeben 
	Y <= Z;
	wait until CLK'event and CLK='1';
    else
--	assert false report "Ergebnis groesser M!" severity note;
    end if;

    -- Ergebnis ausgeben
    EXPRDY <= '1';
  end process;


end BEHAV_MONT;



configuration CFG_RSA_BEHAV_MONT of RSA is
  for BEHAV_MONT
    for all : MONTGOMERY
      use entity WORK.MONTGOMERY(BEHAVIOURAL);
    end for;
  end for;
end CFG_RSA_BEHAV_MONT;

configuration CFG_RSA_BEHAV_RTL of RSA is
  for BEHAV_MONT
    for all : MONTGOMERY
      use configuration WORK.CFG_MONT_TOPHIER;
    end for;
  end for;
end CFG_RSA_BEHAV_RTL;

