--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : Test Bench fuer das vollstaendige RSA-Modul
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : rsa_tb.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 14.11.98        | 14.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Testbench fuer RSA-Modul
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;
  use IEEE.math_real.all;
  use IEEE.std_logic_misc.all;
  use IEEE.std_logic_textio.all;
  use std.textio.all;


entity RSA_TB is     			--keine Ein-/Ausgangssignale
end RSA_TB;



architecture SCHEMATIC of RSA_TB is

    signal CLK		: std_logic := '0';
    signal DATAIN	: std_logic_vector (31 downto 0);
    signal GO		: std_logic;
    signal RESET	: std_logic;
    signal SELDATA	: std_logic_vector (1 downto 0);
    signal VALACC	: std_logic;
    signal DATAOUT	: std_logic_vector (31 downto 0);
    signal NEXTEXP	: std_logic;
    signal READY	: std_logic;

  constant TPW  :  time := 5 ns ;
  constant NumBits : integer := 768;  	-- Anzahl der Bits

  component RSA
    port (	CLK	: In    std_logic;
		DATAIN	: In    std_logic_vector (31 downto 0);
		GO	: In    std_logic;
		RESET	: In    std_logic;
		SELDATA : In    std_logic_vector (1 downto 0);
		VALACC	: In    std_logic;
		DATAOUT : Out   std_logic_vector (31 downto 0);
		NEXTEXP : Out   std_logic;
		READY	: Out   std_logic
         );
  end component;


begin

  I_1 : RSA
    Port Map (CLK, DATAIN, GO, RESET, SELDATA, VALACC, DATAOUT, NEXTEXP, READY);

  CLK <= not CLK after TPW;       -- Taktgenerator mit Periodendauer 2*TPW (hier 10 ns)


  testmuster : process

   FILE testdatei : text IS IN "testdata.txt" ;  
   variable l : line;
   variable n : integer := 1;  		-- Zaehlvariable
   variable p : integer := 1;  		-- Zaehlvariable
   variable daten : bit_vector ((NumBits-1) downto 0);
   variable good : boolean;
   variable A : bit_vector((NumBits-1) downto 0);
   variable M : bit_vector((NumBits-1) downto 0);
   variable E : bit_vector((NumBits-1) downto 0);
   variable Soll : bit_vector((NumBits-1) downto 0);
   variable Ist : bit_vector((NumBits-1) downto 0);
   
  begin

      
      RESET<='1';
      wait for 2*TPW;
      RESET<='0';
      wait for 2*TPW;


      assert False report "Reset durchgefuehrt, Testdaten werden nun aus testdata.txt gelesen. " severity note;

      
      readline ( testdatei, l ) ;  	-- Die zu verschluesselnde Daten werden gelesen
      read ( l, daten,good );
      A := daten;

      readline ( testdatei, l ) ;  	-- Der Modulowert wird gelesen
      read ( l, daten,good );
      M := daten;

      readline ( testdatei, l ) ;  	-- Der Exponent wird gelesen
      read ( l, daten,good );
      E := daten;     

      readline ( testdatei, l ) ;  	-- Das Soll-Ergebnis  wird gelesen
      read ( l, daten,good );
      Soll := daten;


      assert False report "Testdaten eingelesen." severity note;

            
      for i in 0 to 23 loop
	  DATAIN <= to_stdlogicvector( M(31 downto 0) ); --Konvertierung
							 --bit_vector -> std_logic_vector
	  VALACC <= '1';
	  SELDATA <= "00";
	  M((NumBits-33) downto 0) := M((NumBits-1) downto 32);
	  M ((NumBits-1) downto (NumBits-32)):= (others => '0');
	  wait for 4*TPW;
      end loop;  -- i
      VALACC <= '0';

      
      assert False report "Der Modulowert wurden uebergeben." severity note;      

      
      for i in 0 to 23 loop
	  DATAIN <= to_stdlogicvector ( A(31 downto 0) );
	  VALACC <= '1';
	  SELDATA <= "01";
	  A((NumBits-33) downto 0) := A((NumBits-1) downto 32);
	  A((NumBits-1) downto (NumBits-32)) := (others => '0');
	  wait for 4*TPW;
      end loop;  -- i
      VALACC <= '0';
      

      assert False report "Die zu verschluesselnde Daten wurden uebergeben. Exponent folgt. Los gehts .." severity note;

      
      GO <= '1';
      wait for 4*TPW;
--      wait until NEXTEXP='1';

      while READY='0' loop
	  wait until NEXTEXP='1' or READY='1';
	  if NEXTEXP='1' then
	      DATAIN <= to_stdlogicvector ( E(31 downto 0) );
	      VALACC <= '1';
	      SELDATA <= "10";
	      E((NumBits-33) downto 0) := E((NumBits-1) downto 32);
	      E((NumBits-1) downto (NumBits-32)) := (others => '0');
	      assert False report " .te 32Bit wurden uebergeben." severity note;
	      n := n+1;
	      wait for 2*TPW;
	      VALACC<='0';
	      wait for 2*TPW;
	  end if;
      end loop;

      
      for i in 0 to 23 loop
	  Ist((NumBits-33) downto 0) := Ist((NumBits-1) downto 32);	  
	  Ist ((NumBits-1) downto (NumBits-32)) := to_bitvector (DATAOUT);
	  VALACC <= '1';
	  wait for 4*TPW;
      end loop;  -- i

      assert False report "Testsatz   fertig! Nur noch vergleichen." severity note;
      assert Ist = Soll report "Falsches Ergebnis, Verschluesselung falsch";

      p := p+1;
      wait;
      
   end  process testmuster;

end SCHEMATIC;



configuration CFG_RSA_TB of RSA_TB is
  for SCHEMATIC
    for I_1 : RSA
      use entity WORK.RSA(BEHAV);
    end for;
  end for;

end CFG_RSA_TB;




