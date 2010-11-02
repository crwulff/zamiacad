
-- VHDL Model Created from SGE Schematic chip_tb.sch -- Nov 26 14:17:56 1998
--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Tobias Enge
-- Group     : CTRL/RSA
--------------------------------------------------------------------
-- Design Unit Name : Testbench chip
-- Purpose :
-- 
-- File Name :  chip_tb.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
--      06.05.99   |		Anpassung an Gatterebene (Jens Kuenzer) 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Testbench fuer den Cryptochip "PG99"
--  liest Daten aus einer Datei ein und schickt diese an den Chip
--  liest Sollergebnisse aus dieser Datei vergleicht und prueft
--  ob korrekt.
--  Aufbau der Datei:
--  Zeilenweise:
--
--  Header immer gleich(zur Grundbelegung des RAM)
--
--
--  IO-Mode
--  Public Key
--  Private Key
--  Modulus der Keys
--  Zertifikat
--  Public Key der Zertifizierungsstelle
--  Modulus der Zertifizierungsstelle
--  Temporaerer RSA-Key (bei Initialisierung natuerlich beliebig)
--  PIN
--  DES - Key (bei Initialisierung beliebig)
--  Benutzerdaten
--
--  
--
--
---------------------------------------------------------------------------------
--  verschluesseln mit RSA
-------------------------------------------------------------------------------
--  Kommando (ENCRYPT_RSA)
--  Key                      
--  Modulus
--  Daten
--  verschluesselte Daten (zur Kontrolle)
-------------------------------------------------------------------------------
--  entschluesseln mit RSA
-------------------------------------------------------------------------------
--  Kommando (DECRYPT_RSA)
--  Daten
--  entschluesselte Daten (zur Kontrolle)
--
-------------------------------------------------------------------------------
--  Ver/Entschluesseln Single-DES
-------------------------------------------------------------------------------
--  Kommando (SINGLE_DES)
--  Schluessel
--  Daten
--  Status
--  Daten (Kontrolle)
--     . . .  
--  Daten
--  Status
--  Daten (Kontrolle)
--  Stop-Kommando
-------------------------------------------------------------------------------
--  Digitale Unterschrift
-------------------------------------------------------------------------------
--  Kommando (SIGN___MESS)
--  Daten
--   . . . 
--  Daten
--  Stop-Kommando
--  Unterschrift (zur Kontrolle)
--
-------------------------------------------------------------------------------
--  Unterschrift Pruefen
-------------------------------------------------------------------------------
--  Kommando (VERIFY_SIGN)
--  Daten
--  . . . 
--  Daten
--  Status (Zur Kontrolle)
-------------------------------------------------------------------------------
--  Authentifizieren  
-------------------------------------------------------------------------------
--  Kommando (AUTHENTICAT)
--  Daten (zu Kontrolle)
--  Daten
--  Daten (zur Kontrolle)
-------------------------------------------------------------------------------
--  Authentifizierung pruefen
-------------------------------------------------------------------------------
--  Kommando (VERIFY_AUTH)
--  Daten
--  Daten (zur Kontrolle)
--  Schluessel RSA
--  Modulus
--  Daten
--  Status zur Kontrolle
-------------------------------------------------------------------------------
--  Pin aendern
-------------------------------------------------------------------------------
--  Kommando (CHANGE_PIN)
--  Daten (neue PIN)
-------------------------------------------------------------------------------
--  Schluessel von Zertifizierungstelle eingeben
-------------------------------------------------------------------------------
--  Kommando (CHANGE_CERT)
--  Schluessel
--  Modulus
-------------------------------------------------------------------------------
--  Public Key und Zertifikat auslesen
-------------------------------------------------------------------------------
--  Kommando
--  Schluessel (zur Kontrolle)
--  Modulus   ( == )
--  Schluessel ( == )
--  Modulus   ( == )
-------------------------------------------------------------------------------
--  Daten auslesen
-------------------------------------------------------------------------------
--  Kommando (READ_PERS_DATA)
--  Daten (zur Kontrolle)
--   . . .
--  Daten (zur Kontrolle)
-------------------------------------------------------------------------------
--  
--  
--
--  
-- Aa


--   library LIB_GAT;

   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;
   use IEEE.math_real.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_textio.all;
   use std.textio.all;


entity CHIP_TB is
    generic( filename : string := "testdaten.txt" ) ;
end CHIP_TB;

architecture BEHAVIOUR of CHIP_TB is

   signal     D_IN : std_logic_vector(31 downto 0);
   signal    D_OUT : std_logic_vector(31 downto 0);
   signal  IO_MODE : std_logic_vector(1 downto 0);
   signal        A : std_logic_vector(2 downto 0);
   signal  SCL_OUT : std_logic;
   signal   SCL_IN : std_logic;
   signal  SDA_OUT : std_logic;
   signal   SDA_IN : std_logic;
   signal D_OUT_EN : std_logic;
   signal       OE : std_logic;
   signal       WR : std_logic;
   signal       CE : std_logic;
   signal     BUSY : std_logic;
   signal      INT : std_logic;
   signal CARDCHANGE : std_logic;
   signal      RES : std_logic;
   signal      CLK : std_logic;
   signal SER_ACK : std_logic;
   signal ENABLE_TEST : std_logic;  	-- dummy

   signal TEST_SI : std_logic := '0';  	-- Jens
   signal TEST_SE : std_logic := '0';
   signal TEST_SO : std_logic;
   
   constant PERIOD : time := 80 ns;  -- <Periodendauer der PAR_CLK>
   constant SPERIOD : time := 500 ns;  -- Periodendauer der CLK fuer I2C
   constant CLK_PERIOD : time := 20 ns;
   constant Bitbreite  : integer := 768;
   constant writeout : boolean := false;  -- write RSA encrypted data to file
					  -- for generating test-data?
   component OUTHER_CHIP
      Port (       A : In    std_logic_vector (2 downto 0);
             CARDCHANGE : In    std_logic;
                  CE : In    std_logic;
                 CLK : In    std_logic;
                D_IN : In    std_logic_vector (31 downto 0);
             IO_MODE : In    std_logic_vector (1 downto 0);
                  OE : In    std_logic;
                 RES : In    std_logic;
              SCL_IN : In    std_logic;
              SDA_IN : In    std_logic;
                  WR : In    std_logic;
                BUSY : Out   std_logic;
               D_OUT : Out   std_logic_vector (31 downto 0);
            D_OUT_EN : Out   std_logic;
                 INT : Out   std_logic;
             SCL_OUT : Out   std_logic;
             SDA_OUT : Out   std_logic;
	 ENABLE_TEST : out   std_logic;
	     TEST_SE : In    std_logic;
	     TEST_SI : In    std_logic;
	     TEST_SO : out   std_logic  );
  end component;

begin

-- main program ---------------------------------------------------------------
                
    
--generate_reset:
  --   RES <= '1','0' after 32*PERIOD;

-- clock generator -----------------------------------------------------------
     
	   -- generiert den extern angelegten Takt fuer den Chip
   clock : process
--	variable i : integer;
	begin
	    L1:  loop  		     
		CLK <= '0';
		wait for CLK_PERIOD / 2;
                CLK <= '1';
       	        wait FOR CLK_PERIOD / 2;

       end loop L1;
    end process;




   
       
	      -- purpose: Simulation of the complete chip
	      -- 
   simulate : process 
		  

       file testdaten : text is in filename ;
       file ausgabe   : text is out "ausgabe_sign.txt";
       type modetype is (ser,par8,par16,par32);
       variable l : line;
       variable outl : line;
       variable testline : line;
       variable command              : bit_vector (7 downto 0);
       variable publickey            : bit_vector ((Bitbreite - 1) downto 0);
       variable privatekey           : bit_vector ((Bitbreite - 1) downto 0);
       variable privatekeymod        : bit_vector ((Bitbreite - 1) downto 0);
       variable Zertifikat           : bit_vector ((Bitbreite - 1) downto 0);
       variable Zertpubkey           : bit_vector ((Bitbreite - 1) downto 0);
       variable Zertmodul            : bit_vector ((Bitbreite - 1) downto 0);
       variable temprsakey           : bit_vector ((Bitbreite - 1) downto 0);
       variable temprsakeymod        : bit_vector ((Bitbreite - 1) downto 0);
       variable daten768             : bit_vector ((BitBreite - 1) downto 0);
       variable kontrolldaten768     : bit_vector ((BitBreite - 1) downto 0);
       variable daten32		     : bit_vector(31 downto 0);
       variable kontrolldaten32      : bit_vector(31 downto 0);
       variable daten64              : bit_vector (63 downto 0);
       variable kontrolldaten64      : bit_vector (63 downto 0);
       variable status               : bit_vector (15 downto 0) ;
       variable kontrollstatus       : bit_vector (15 downto 0);
       variable pin                  : bit_vector (31  downto 0) ;
       variable deskey               : bit_vector (63 downto 0);
       variable deskey2              : bit_vector (63 downto 0);
       variable deskey3              : bit_vector (63 downto 0);
       variable good                 : boolean;
       variable memoryrest           : bit_vector(2783 downto 0);
       variable mode : modetype;
       variable modzw : modetype;
       variable memblock : bit_vector((BitBreite-1) downto 0);
       variable t : String(1 to 11);
       variable tstatus : String(1 to 8);
       variable modezw : std_logic_vector(1 downto 0);


       
       -- purpose: Writes 32 Bits of data to the chip
       procedure writedata32 (CONSTANT  data : bit_vector ( 31 downto 0 )) is
	   
       begin  -- writedata32
	   if  busy = '1' then 
	      wait UNTIL busy = '0';
	       
	   end if;
	   assert false report "WRITE 32" severity note;
	   A <= "000" after 1 ns;
	   D_IN <= to_stdlogicvector(data(31 downto 0));
    	   wait for PERIOD/2;
	   CE <= '0' after 1 ns;
	   WR  <= '0' after 1 ns;
	   wait for 2*PERIOD;
	   CE <= '1' after 1 ns;
	   WR <= '1' after 1 ns; --  after PERIOD/2;
	   wait for 2*PERIOD;
       end writedata32;

       -- purpose: Writes 32 Bits of data to the chip in 2 16 Bit Steps 
       procedure writedata16 (CONSTANT data : bit_vector (31 downto 0)) is
	
       begin  -- writedata16
	   
	     if busy = '1' then
	         wait UNTIL busy = '0';
	     end if;
	     assert false report "WRITE 16" severity note;
	     A <= "000" after 1 ns;
	     D_IN(15 downto 0) <= to_stdlogicvector(data (15 downto 0));
	     wait for PERIOD;
	     CE <= '0' after 1 ns;
	     WR <= '0' after 1 ns;
	     wait for PERIOD;
	     CE <= '1'  after 1 ns;
	     WR <= '1'  after 1 ns;
	     wait for 2*PERIOD;
             if busy = '1' then
	         wait UNTIL busy = '0';
	     end if;
	     A <= "001" after 1 ns;
	     D_IN(15 downto 0) <= to_stdlogicvector(data (31 downto 16));
	     wait for PERIOD;
	     CE <= '0' after 1 ns;
	     WR <= '0' after 1 ns;
	     wait for PERIOD;
	     CE <= '1'  after 1 ns;
	     WR <= '1'  after 1 ns;
	     wait for 2*PERIOD;
       end writedata16;


  -- purpose: generates the Start Condition which initiates any Transmission
   --          on the I2C-Bus
   procedure StartI2C is
       
   begin  -- StartI2C
       assert false report "Erzeuge Start Condition" severity note;
      
       SDA_IN <= '1' after 1 ns;
       SCL_IN <= '1' after 1 ns;
       wait for SPERIOD;  		-- / 2 weggemacht, Dirk.
       SDA_IN <= '0' after 1 ns;
       wait for SPERIOD;
       SCL_IN <= '0' after 1 ns;
       wait for SPERIOD;
   end StartI2C;

   -- purpose: generates the Stop Condition which ends Transmissions
   -- on the I2C-Bus
   procedure StopI2C is
       
   begin  -- StopI2C
       assert false report "Erzeuge Stop Condition" severity note;
       SDA_IN <= '0' after 1 ns;
       SCL_IN <= '0' after 1 ns;
       wait for SPERIOD ;
       SCL_IN <= '1' after 1 ns;
       wait for SPERIOD ;
       SDA_IN <= '1' after 1 ns;
       wait for SPERIOD ;
   end StopI2C;


  -- note: The Start Condition is generated by proc. StartI2C
   -- purpose: writes 8 Bit of data serial to the Chip
   procedure writeserial (CONSTANT data : bit_vector(7 downto 0))  is
   variable i : integer;
   variable temp : std_logic_vector(7 downto 0);

   
   begin  -- writeserial
       assert false report "Writeserial 8 Bit" severity note;
       temp := to_stdlogicvector(data);
       wait for SPERIOD;
       wait until clk = '0';
       SDA_IN <= '0' after 2 ns;
       SCL_IN <= '0' after 2 ns;
       SER_ACK <= '0' after 2 ns;
       for i in 7 downto 0 loop
	   SDA_IN <= temp(i);
	   wait for SPERIOD / 4;
	   wait until clk='0';
	   SCL_IN <= '1' after 2 ns;
	   if SCL_OUT = '0' then
	       wait until SCL_OUT = '1';
	   end if;
	   wait FOR SPERIOD / 2;
	   wait until clk='0';
	   SCL_IN <= '0' after 2 ns;
	  
	   wait for SPERIOD /4;
       end loop;  -- i
       assert false report "WRITE SERIAL" severity note;
       
       -- SDA_IN <= '0'; -- here should be the Acknoledgement
       wait for SPERIOD / 4;
       wait until clk = '0';
       SCL_IN <= '1' after 2 ns;  -- but i don't know how in the moment
       if SCL_OUT = '0' then
	       wait until SCL_OUT = '1';
       end if;
       wait for SPERIOD /4;
       wait until clk = '0';
       SER_ACK <= not SDA_OUT;
       wait for SPERIOD / 4;
       wait until clk = '0';
       SCL_IN <= '0' after 2 ns;
       wait for SPERIOD / 4;
       
   end writeserial;



  

		
       -- Writes 32 Bits of data to the chip in 4 8 Bit Steps
       procedure writedata8 (CONSTANT  data : bit_vector (31 downto 0)) is
	   
       begin  -- writedata8
	   if busy = '1' then
	       wait UNTIL busy = '0';
	   end if;
	   assert false report "WRITE 08" severity note;
	   A <= "000" after 1 ns;
	   D_IN(7 downto 0) <= to_stdlogicvector(data(7 downto 0));
	   
	   wait for PERIOD;
	   CE <= '0' after 1 ns;
	   WR <= '0' after 1 ns;
	   wait for PERIOD;
	   CE <= '1'  after 1 ns;
	   WR <= '1'  after 1 ns;
	   wait for 2*PERIOD;
	   if busy = '1' then
	       wait UNTIL busy = '0';
	   end if;
	   A <= "001" after 1 ns;
	   D_IN(7 downto 0) <= to_stdlogicvector(data(15 downto 8));
	   
	   wait for PERIOD;
	   CE <= '0' after 1 ns;
	   WR <= '0' after 1 ns;
	   wait for PERIOD;
	   CE <= '1'  after 1 ns;
	   WR <= '1'  after 1 ns;
	    wait for 2*PERIOD;
	   if busy = '1' then
	       wait UNTIL busy = '0';
	   end if;
	   A <= "010" after 1 ns;
	   D_IN(7 downto 0) <= to_stdlogicvector(data(23 downto 16));
            
	   wait for PERIOD;
	   CE <= '0' after 1 ns;
	   WR <= '0' after 1 ns;
	   wait for PERIOD;
	   CE <= '1'  after 1 ns;
	   WR <= '1'  after 1 ns;
	   wait for 2*PERIOD;
	   if busy = '1' then
	       wait UNTIL busy = '0';
	   end if;
	   A <= "011" after 1 ns;
	   D_IN(7 downto 0) <= to_stdlogicvector(data(31 downto 24));
            
	   wait for PERIOD;
	   CE <= '0' after 1 ns;
	   WR <= '0' after 1 ns;
	   wait for PERIOD;
	   CE <= '1' after 1 ns;
	   WR <= '1' after 1 ns;
	   wait for 2*PERIOD;
       end writedata8;

      -- purpose: write 32 Bits of data to the chip over I2C
   procedure writeserial32 (CONSTANT data : bit_vector(31 downto 0)) is
   variable temp: bit_vector(7 downto 0)  ;
   variable i  : integer;
   begin  -- writeserial32
       assert false report "Schreibe 32 Bit seriell" severity note;
       temp := "01000000";
       StartI2C;
       writeserial(temp);
       for i in 0 to 3 loop
	   writeserial(data((i*8)+7 downto (8*i)));
       end loop ;
       StopI2C;
   end writeserial32;


-- Writes 32 Bits of data to the chip selecting the right I/O-Mode 
   procedure writedata (CONSTANT data : bit_vector(31 downto 0)) is
       
   begin  -- writedata
       if mode = par32  then
	   writedata32(data(31 downto 0));
       end if;
       if mode = par16 then
	   writedata16(data(31 downto 0));
       end if;
       if mode = par8 then
	   writedata8(data(31 downto 0));
       end if;
       if mode = ser  then
	   writeserial32(data(31 downto 0));
       end if;
   end writedata;


  
		
   -- Writes 768 Bits of data to the chip in 24 32 Bit Steps
   procedure writedata768 (CONSTANT data : bit_vector(767 downto 0)) is
   variable i : integer := 0;  		-- Laufvariable    
   begin  -- writedata768
       for i in 0 to 23 loop
	   writedata(data((i*32+31) downto (i*32)));
       end loop;  -- i
       
   end writedata768;

   procedure writedata2784 (CONSTANT data : bit_vector(2783 downto 0)) is
   variable i : integer := 0;  		-- Laufvariable    
   begin  -- writedata2784
       for i in 0 to 86 loop
	   writedata(data((i*32+31) downto (i*32)));
       end loop;  -- i
       
   end writedata2784;
		
   -- Writes 64 Bits of Data to the Chip
   procedure writedata64  (constant data  : bit_vector(63 downto 0)) is
       
   begin  -- writedata64 
       writedata(data(31 downto 0));
       writedata(data(63 downto 32));
   end writedata64 ;

 

  
		
 

   -- note: The Start Condition for this Operation is generated by StartI2C
   -- purpose: reads 8 Bist of data from the chip
   procedure readserial (VARIABLE data : out  bit_vector(7 downto 0)) is
    
   begin  -- readserial
       assert false report "Lese jetzt 8 Bit seriell " severity note;
       wait until clk='0';
       SDA_IN <= 'H' after 1 ns;  			-- neu
       SCL_IN <= '1' after 1 ns;
       SER_ACK <= '0' after 1 ns;
       wait for SPERIOD / 4;
       if SCL_OUT ='0' then  		-- kleiner Test wegen des Wartens auf
					-- den chip
	   wait until SCL_OUT = '1';
	   wait until clk='0';
       end if;
       for i in 7 downto 0 loop
	   SCL_IN <= '1' after 1 ns;
           if SCL_OUT = '0' then
	       wait until SCL_OUT = '1';
	       wait until clk='0';
	   end if;	   
	   wait for SPERIOD / 4;
	   wait until clk='0';
	   data(i) := to_bit(SDA_OUT); -- after PERIOD / 4;
	   wait FOR SPERIOD / 4;
	   wait until clk='0';
	   SCL_IN <= '0';
	   wait for SPERIOD / 2;
	   wait until clk='0';
       end loop;  -- i
       assert false report "READ SERIAL" severity note;
       wait until clk='0';
       SDA_IN <= '0' after 1 ns;   -- here should be the Acknoledgement
       wait for SPERIOD / 4;
       wait until clk='0';
       SCL_IN <= '1' after 1 ns;  		       
       wait for SPERIOD / 4; 
       wait until clk='0';
        if SDA_OUT = '1' then
  	 assert false report "AKN failed" severity note;   
        end if;
       wait for SPERIOD / 4;
       wait until clk='0';
       SCL_IN <= '0' after 1 ns;
       wait for SPERIOD / 8;
       wait until clk='0';
       SDA_IN <= '1' after 1 ns;
       wait for SPERIOD /8;
       wait until clk='0';
   end readserial;




   -- note: The Start Condition for this Operation is generated by StartI2C
   -- purpose: reads the status  from the chip
   procedure read_status_serial (VARIABLE status : out bit_vector(15 downto 0)) is
   variable temp  : bit_vector(7 downto 0);
   begin  -- read_status_serial
      temp := "01000011";  		
      StartI2C;
      writeserial(temp);
      readserial(status(7 downto 0));
      --readserial(status(15 downto 8));
      StopI2C;
   end read_status_serial;

procedure readserial32 (VARIABLE  data : out bit_vector(31 downto 0)) is
  variable temp : bit_vector(7 downto 0);
  variable i  : integer := 0;
begin  -- readserial32
    temp := "01000001";
    assert false report "readserial 32 gestartet" severity note;
    
    SER_ACK <= '0';
    wait for PERIOD;
    while SER_ACK  /= '1' loop
	StartI2C;
	writeserial(temp);
    end loop;
    assert false report "Adresse acknoledged" severity note;
    for i in 0 to 3 loop
	readserial(data((i*8+7) downto (i*8)));
    end loop;  -- i
    StopI2C;
end readserial32;


		
   

 
		
    -- Reads 32 Bits of Data from the chip in 4 8 Bit Steps
   procedure readdata8 (VARIABLE data : out bit_vector(31 downto 0)) is

  
   begin  -- readdata8
     if busy = '1' then
	 wait UNTIL busy ='0';
     end if;
     if INT = '0' then
	wait UNTIL INT = '1'; 
     end if;
     assert false report "READ 08" severity note;
     A <= "000" after 1 ns;
     CE <= '0' after 1 ns;
     OE <= '0'  after 1 ns;
     wait FOR 2*PERIOD;
     data(7 downto 0) := to_bitvector(D_OUT(7 downto 0));
     CE <= '1' after 1 ns;
     OE <= '1' after 1 ns;
     wait for 2*PERIOD;
     if busy = '1' then
	 wait UNTIL busy ='0';
     end if;
      
     A <= "001" after 1 ns;
     CE <= '0' after 1 ns;
     OE <= '0'  after 1 ns;
     wait FOR 2*PERIOD;
     data(15 downto 8) := to_bitvector(D_OUT(7 downto 0));
     CE <= '1' after 1 ns;
     OE <= '1' after 1 ns;
      wait for 2*PERIOD;
     if busy = '1' then
	 wait UNTIL busy ='0';
     end if;
     
     A <= "010" after 1 ns;
     CE <= '0' after 1 ns;
     OE <= '0'  after 1 ns;
     wait FOR 2*PERIOD;
     data(23 downto 16) := to_bitvector(D_OUT(7 downto 0));
     CE <= '1' after 1 ns;
     OE <= '1' after 1 ns;
      wait for 2*PERIOD;
     if busy = '1' then
	 wait UNTIL busy ='0';
     end if;
     
     A <= "011" after 1 ns;
     CE <= '0' after 1 ns;
     OE <= '0'  after 1 ns;
     wait FOR 2*PERIOD;
     data(31 downto 24) := to_bitvector(D_OUT(7 downto 0));
     CE <= '1' after 1 ns;
     OE <= '1' after 1 ns;
      wait for 2*PERIOD;
   end readdata8;

   -- purpose: Reads 32 Bist of data from the Chip in 2 16 Bit Steps
   procedure readdata16 (VARIABLE data : out bit_vector(31 downto 0) ) is
       
   begin  -- readdata16
       if busy = '1'  then
	   wait UNTIL busy = '0';
       end if;
       if INT ='0' then
	   wait UNTIL INT = '1'; 
       end if;
       assert false report "READ 16" severity note;
       A <= "000" after 1 ns;
       CE <= '0' after 1 ns;
       OE <= '0' after 1 ns;
       wait For 2*PERIOD;
       data(15 downto 0) := to_bitvector(D_OUT(15 downto 0));
       CE <= '1' after 1 ns;
       OE <= '1' after 1 ns;
        wait for 2*PERIOD;
       if busy = '1'  then
	   wait UNTIL busy = '0';
       end if;
      
       A <= "001" after 1 ns;
       CE <= '0' after 1 ns;
       OE <= '0'  after 1 ns;
       wait For 2*PERIOD;
       data(31 downto 16) := to_bitvector(D_OUT(15 downto 0));
       CE <= '1' after 1 ns;
       OE <= '1' after 1 ns;
        wait for 2*PERIOD;
   end readdata16;

   -- Reads 32 Bits of Data from the Chip
   procedure readdata32  (VARIABLE data : OUT bit_vector(31 downto 0)) is
       
   begin  -- readdata32 
       if busy = '1'  then
	   wait UNTIL busy = '0';
       end if;
       if INT = '0' then
	  wait UNTIL  INT = '1';  	  
       end if;
       assert false report "READ 32" severity note;
       A <= "000" after 1 ns;
       wait for PERIOD/2;
       CE <= '0' after 1 ns;
       OE <= '0' after 1 ns; -- after PERIOD /2;
       wait For 2 * PERIOD;
       data(31 downto 0) := to_bitvector(D_OUT(31 downto 0));
       CE <= '1' after 1 ns;
       OE <= '1' after 1 ns; -- after PERIOD/2;
       wait for 2* PERIOD;
   end readdata32 ;

  -- Reads 32 Bits of Data from Chip taking the actual I/O - Mode

 

		
   procedure readdata (VARIABLE data : out bit_vector(31 downto 0)) is
   variable temp : bit_vector(31 downto 0);
   begin  -- readdata
       if mode = par32  then
	   readdata32(temp(31 downto 0));
       end if;
       if mode = par16 then
	   readdata16(temp(31 downto 0));
       end if;
       if mode = par8 then
	   readdata8(temp(31 downto 0));
       end if;
       if mode = ser then
	   readserial32(temp(31 downto 0));
       end if;
       data := temp;
   end readdata;

   -- Reads 768 Bits of Data from the Chip
   procedure readdata768 (VARIABLE data : out bit_vector(767 downto 0)) is
   variable i : integer := 0;  		-- Laufvariable
   variable temp : bit_vector(767 downto 0);
   begin  -- readdata768
       for i in 0 to 23 loop
	   readdata(temp((i*32+31) downto (i*32)));
       end loop;  -- i
       data := temp;
   end readdata768;
 
    -- purpose: Reads 64 Bits of Data from the Chip
   procedure readdata64 (VARIABLE data : out bit_vector(63 downto 0)) is
   variable temp  : bit_vector(63 downto 0);
   begin  -- readdata64
       readdata(temp(31 downto 0));
       readdata(temp(63 downto 32));
       data := temp;
   end readdata64;


   -- purpose: Writes an Command-Code to the Chip
   procedure writecommand (CONSTANT cmd : bit_vector(4 downto 0)) is
       variable temp    : std_logic_vector(7 downto 0) := "10000000";
       variable sertemp : bit_vector(7 downto 0) := "00000000";
   begin  -- writecommand
       wait for period;
       if mode = ser then
	   sertemp := "01000010";  	-- Adress for the command-word
	   StartI2C;
	   writeserial(sertemp);
	   sertemp:= "10000000";
	   sertemp(4 downto 0) := cmd(4 downto 0);
	   writeserial(sertemp);
	   StopI2C;
	   
       else   
       temp(4 downto 0) := to_stdlogicvector(cmd(4 downto 0)); 
       if  busy = '1' then 
	      wait UNTIL busy = '0';
	       
	   end if;
	   A <= "100" after 1 ns;
	   D_IN(7 downto 0) <= temp;
	   CE <= '0' after 1 ns;
	   WR <= '0' after 1 ns;
	   wait for 2*PERIOD;
	   CE <= '1' after 1 ns;
	   WR <= '1' after 1 ns;
	   wait for 2*PERIOD;
        end if;
   end writecommand;

		 -- purpose: Writes an Command-Code to the Chip
   procedure writecommand_ramtest (CONSTANT cmd : bit_vector(6 downto 0)) is
       variable temp    : std_logic_vector(7 downto 0) := "10000000";
       variable sertemp : bit_vector(7 downto 0) := "00000000";
   begin  -- writecommand
       wait for period;
       if mode = ser then
	   sertemp := "01000010";  	-- Adress for the command-word
	   StartI2C;
	   writeserial(sertemp);
	   sertemp:= "10000000";
	   sertemp(4 downto 0) := cmd(4 downto 0);
	   writeserial(sertemp);
	   StopI2C;
	   
       else   
       temp(6 downto 0) := to_stdlogicvector(cmd(6 downto 0)); 
       if  busy = '1' then 
	      wait UNTIL busy = '0';
	       
	   end if;
	   A <= "100";
	   D_IN(7 downto 0) <= temp;
	   CE <= '0';
	   WR <= '0';
	   wait for 2*PERIOD;
	   CE <= '1' ;
	   WR <= '1' ;
	   wait for 2*PERIOD;
        end if;
   end writecommand_ramtest;
		
   -- purpose: Reads the Status-Register
   procedure readstatus (VARIABLE status : out bit_vector(15 downto 0)) is
       
   begin  -- readstatus
     if (mode = par32) or (mode = par16) then
       if busy = '1'  then
	   wait UNTIL busy = '0';
       end if;
       A <= "100" after 1 ns;
       CE <= '0'after 1 ns;
       OE <= '0' after PERIOD /2 + 1 ns;
       wait For 2*PERIOD;
       status(15 downto 0) := to_bitvector(D_OUT(15 downto 0));
       CE <= '1' after 1 ns;
       OE <= '1' after 1 ns;
        wait for 2*PERIOD;
     end if;
     
     if mode = par8 then
       if busy = '1'  then
	   wait UNTIL busy = '0';
       end if;
       A <= "100" after 1 ns;
       CE <= '0' after 1 ns;
       OE <= '0' after PERIOD /2 + 1 ns;
       wait For 2*PERIOD;
       status(7 downto 0) := to_bitvector(D_OUT(7 downto 0));
       CE <= '1' after 1 ns;
       OE <= '1' after 1 ns;
       wait for 2*PERIOD;
       if busy = '1'  then
	   wait UNTIL busy = '0';
       end if;
       A <= "101" after 1 ns;
       CE <= '0' after 1 ns;
       OE <= '0' after PERIOD /2+ 1 ns;
       wait For 2*PERIOD;
       status(15 downto 8) := to_bitvector(D_OUT(7 downto 0));
       CE <= '1' after 1 ns;
       OE <= '1' after 1 ns;
       wait for 2*PERIOD;
     end if;
     if mode = ser then
	 read_status_serial(status);
     end if;
   end readstatus;
   
   -- purpose: Tests the RSA-Encryption
   procedure encrypt_rsa is
       
   begin  -- encrypt_rsa
       
       readline(testdaten,l);
       read (l,temprsakey, good);
       readline(testdaten,l);
       read (l,temprsakeymod, good);
       readline (testdaten,l);
       read (l,daten768,good);
       readline (testdaten,l);
       read (l,kontrolldaten768,good);
       writecommand("00001");
       writedata768(temprsakey);
       writedata768(temprsakeymod);
       writedata768(daten768);
       readdata768(daten768);
       write(l,daten768);
       writeline(ausgabe,l);
       assert false  report "wrote encrypted data to file ausgabe.txt" severity note;
       if writeout then
	   write(l,daten768);
	   writeline(ausgabe,l);
	   assert false  report "wrote encrypted data to file ausgabe.txt" severity note;
       else
           assert not(daten768 = kontrolldaten768) report "RSA-Encryption successfull" severity note;
           assert daten768 = kontrolldaten768 report "RSA-Encryption failed" severity failure;
       end if;
   end encrypt_rsa;

   -- purpose: Tests the Decryption with RSA
   procedure decrypt_rsa is
       
   begin  -- decrypt_rsa
       readline(testdaten,l);
       read(l,daten768,good);
       readline(testdaten,l);
       read(l,kontrolldaten768,good);
       writecommand("01001");
       writedata768(daten768);
       readdata768(daten768);
       assert not (daten768 = kontrolldaten768) report "RSA-Decryption successfull!" severity note;
       assert daten768 = kontrolldaten768  report "RSA-Decryption failed!" severity failure;
   end decrypt_rsa;

   -- purpose: Tests the Encryption with DES, all Modi
   procedure encrypt_des is
       
   begin  -- encrypt_des
       --read(l,t,good);
       case t is
	 when "EN_SING_ECB"           => writecommand("10000");
				               readline(testdaten,l);
				               read(l,deskey,good);
				               writedata64(deskey);
	 when "EN_SING_CBC"           => writecommand("10100");
					       readline(testdaten,l);
					       read(l,deskey,good);
					       writedata64(deskey);
			                       readline(testdaten,l);
					       read(l,deskey,good);
					       writedata64(deskey);
	 when "EN_EEE2_ECB"      => writecommand("10001");
					       readline(testdaten,l);
					       read(l,deskey,good);
					       readline(testdaten,l);
					       read(l,deskey2,good);
					       writedata64(deskey);
					       writedata64(deskey2);
	 when "EN_EEE2_CBC"      => writecommand("10101");
					       readline(testdaten,l);
					       read(l,deskey,good);
					       readline(testdaten,l);
					       read(l,deskey2,good);
					       writedata64(deskey);
					       writedata64(deskey2);
	 when "EN_EEE3_ECB"      => writecommand("10010");
					       readline(testdaten,l);
					       read(l,deskey,good);
					       readline(testdaten,l);
					       read(l,deskey2,good);
					       readline(testdaten,l);
					       read(l,deskey3,good);
       					       writedata64(deskey);
					       writedata64(deskey2);
					       writedata64(deskey3);
	 when  "EN_EEE3_CBC"     => writecommand("10110");
					       readline(testdaten,l);
					       read(l,deskey,good);
					       readline(testdaten,l);
					       read(l,deskey2,good);
					       readline(testdaten,l);
					       read(l,deskey3,good);
       					       writedata64(deskey);
					       writedata64(deskey2);
					       writedata64(deskey3);
	 when "EN_EDE3_ECB"      => writecommand("10011");
					       readline(testdaten,l);
					       read(l,deskey,good);
					       readline(testdaten,l);
					       read(l,deskey2,good);
					       readline(testdaten,l);
					       read(l,deskey3,good);
       					       writedata64(deskey);
					       writedata64(deskey2);
					       writedata64(deskey3);
	 when "EN_EDE3_CBC"      => writecommand("10111");
			  		       readline(testdaten,l);
					       read(l,deskey,good);
					       readline(testdaten,l);
					       read(l,deskey2,good);
					       readline(testdaten,l);
					       read(l,deskey3,good);
       					       writedata64(deskey);
					       writedata64(deskey2);
					       writedata64(deskey3);
	 when others => assert false report "Ungueltiges Kommando" severity failure;
				      
     end case;
     readline(testdaten,l);			
     read(l,t,good);
     while t /= "STOP_DES_CH" loop
	 readline(testdaten,l);
	 read(l,daten64,good);
	 --readline(testdaten,l);
	 --read(l,kontrollstatus,good);
	 readline(testdaten,l);
	 read(l,kontrolldaten64,good);
	 writedata64(daten64);
	 --readstatus(status);
	 readdata64(daten64);
	 --assert not(status = kontrollstatus) report "DES-Status incorrect!" severity failure;
	 assert daten64 = kontrolldaten64  report "DES-Encryption failed!" severity failure;
	 assert not(daten64 = kontrolldaten64) report "DES-Encryption successfull" severity note;
	 readline(testdaten,l);
	 read(l,t,good);    
     end loop;
   end encrypt_des;





     -- purpose: Tests the Decryption with DES, all Modi
   procedure decrypt_des is
       
   begin  -- decrypt_des
       --read (l,t,good);
       case t is
	 when "DE_SING_ECB"           => writecommand("11000");
				               readline(testdaten,l);
				               read(l,deskey,good);
				               writedata64(deskey);
	 when "DE_SING_CBC"           => writecommand("11001");
					       readline(testdaten,l);
					       read(l,deskey,good);
					       writedata64(deskey);
	 when "DE_EEE2_ECB"      => writecommand("11010");
					       readline(testdaten,l);
					       read(l,deskey,good);
					       readline(testdaten,l);
					       read(l,deskey2,good);
					       writedata64(deskey);
					       writedata64(deskey2);
	 when "DE_EEE2_CBC"      => writecommand("11011");
					       readline(testdaten,l);
					       read(l,deskey,good);
					       readline(testdaten,l);
					       read(l,deskey2,good);
					       writedata64(deskey);
					       writedata64(deskey2);
	 when "DE_EEE3_ECB"      => writecommand("11100");
					       readline(testdaten,l);
					       read(l,deskey,good);
					       readline(testdaten,l);
					       read(l,deskey2,good);
					       readline(testdaten,l);
					       read(l,deskey3,good);
       					       writedata64(deskey);
					       writedata64(deskey2);
					       writedata64(deskey3);
	 when  "DE_EEE3_CBC"     => writecommand("11101");
					       readline(testdaten,l);
					       read(l,deskey,good);
					       readline(testdaten,l);
					       read(l,deskey2,good);
					       readline(testdaten,l);
					       read(l,deskey3,good);
       					       writedata64(deskey);
					       writedata64(deskey2);
					       writedata64(deskey3);
	 when "DE_EDE3_ECB"      => writecommand("11101");
					       readline(testdaten,l);
					       read(l,deskey,good);
					       readline(testdaten,l);
					       read(l,deskey2,good);
					       readline(testdaten,l);
					       read(l,deskey3,good);
       					       writedata64(deskey);
					       writedata64(deskey2);
					       writedata64(deskey3);
	 when "DE_EDE3_CBC"      => writecommand("11111");
			  		       readline(testdaten,l);
					       read(l,deskey,good);
					       readline(testdaten,l);
					       read(l,deskey2,good);
					       readline(testdaten,l);
					       read(l,deskey3,good);
       					       writedata64(deskey);
					       writedata64(deskey2);
					       writedata64(deskey3);
	 when others => assert false  report "Invaild Command" severity failure; 
				      
     end case;
     readline(testdaten,l);
     read(l,t,good);
     while t /= "STOP_DES_CH" loop
	 read(l,daten64,good);
	 readline(testdaten,l);
	 read(l,kontrollstatus,good);
	 readline(testdaten,l);
	 read(l,kontrolldaten64,good);
	 writedata64(daten64);
	 readstatus(status);
	 readdata64(daten64);
	 assert not(status = kontrollstatus) report "DES-Status incorrect!" severity failure;
	 assert daten64 = kontrolldaten64  report "DES-Decryption failed!" severity failure;
	 assert not(daten64 = kontrolldaten64) report "DES-Decryption successfully" severity note;
	 readline(testdaten,l);
	 read(l,t,good);    
     end loop;
   end decrypt_des;


   -- purpose: Tests the digital Signature
   procedure sign is
       
   begin  -- sign
       writecommand("01011");
       readline(testdaten,l);
       read(l,t,good);
       while t /= "STP_SIGNING" loop
	  readline(testdaten,l); 
	  read(l,daten64,good);
	 
	  writedata64(daten64);
	 
	  readline(testdaten,l);
	  read(l,t,good);
       end loop;
       readdata(daten32);
       readline(testdaten,l);
       read(l,kontrolldaten768,good);
       readdata768(daten768);
       write(outl,daten768);
       writeline(ausgabe,outl);
       assert not (daten768 = kontrolldaten768) report "Data successfully signed!" severity note;
       assert daten768 = kontrolldaten768 report "Digital signing failed!" severity failure;
	   
   end sign;


   -- purpose: Tests the Verification of an digital Signature
   procedure verify_sign is
     variable fertig : boolean := false;
   begin  -- verify_sign
       writecommand("00011");
        readline(testdaten,l);
       read(l,temprsakey,good);  	-- Schluessel und
       readline(testdaten,l);
       read(l,temprsakeymod,good);  	-- Modul einlesen
       writedata768(temprsakey);
       assert false  report "<stExponent des Keys uebertragen" severity note;
       writedata768(temprsakeymod);
       assert false report "Modulo des Keys uebertragen" severity note;
       readline(testdaten,l);
       read(l,daten768,good);
       writedata768(daten768);  	-- Signatur uebertragen
       assert false report "Signatur uebertragen" severity note;
       
        readline(testdaten,l);
        read(l,t,good);
       while t /= "STP_SIGNING" loop
	  readline(testdaten,l); 
	  read(l,daten64,good);
	 
	  writedata64(daten64);
	 
	  readline(testdaten,l);
	  read(l,t,good);
       end loop;
       readdata(daten32);  		-- Chip mitteilen, das jetzt die daten
					-- komplett sind
       assert false report "Daten fuer Signatur wurden uebertragen" severity note;
       
       fertig := false;
       while not fertig loop
	   readstatus(status);
	   if (status(2) = '1') or (status(7) ='1') then 
	       fertig := true;
	   end if;
       end loop;
       assert not (status(2) = '1') report "Signature verified successfully" severity note;
       assert status(2) = '1'  report "Verifying the digital Signature failed" severity failure;
   end verify_sign;



   -- purpose: Tests the Authentication Mechanism
   procedure authenticate is
       variable temp64 : bit_vector(63 downto 0);
   begin  -- authenticate
       writecommand("00010");
       readline(testdaten,l);
       read(l,daten64,good);
       writedata64(daten64);  		-- RANDOM Wert zum Chip uebertragen
       
      
       readline(testdaten,l);
       read(l,kontrolldaten768,good);  	-- Hash mit Private Key des Chips
       		                        -- verschluesselt einlesen
       readdata768(daten768);  		-- selbiges vom Chip lesen
       
       assert daten768 = kontrolldaten768 report "Authentication successfully" severity note;
       assert not (daten768 = kontrolldaten768) report "Authentication failed" severity failure;
       
   end authenticate;    


   -- purpose: Tests the Verification of an Authentication
   procedure verify_auth is
      variable fertig : BOOLEAN := false;
   begin  -- verify_auth
       writecommand("00000");
       readline(testdaten,l);
       read(l,kontrolldaten64,good);  	-- RANDOM-Wert aus Datei einlesen
       readdata64(daten64);  		-- RANDOM-Wert vom Chip lesen
       write(outl,daten64);
       writeline(ausgabe,outl);
       assert kontrolldaten64 = daten64 report "Random stimmt nich ueberein" severity failure;
       assert not (kontrolldaten64 = daten64) report "Random Wert ok" severity note;
       readline(testdaten,l);
       read(l,temprsakey,good);
       writedata768(temprsakey);
       assert false report "Public Key wurde uebertragen" severity note;
       readline(testdaten,l);
       read(l,temprsakeymod,good);
       writedata768(temprsakeymod);
       assert false report "Modulo des Public Keys wurde uebertragen" severity note;
       
       
      
       readline(testdaten,l);
       read(l,daten768,good);  		-- verschluesselten Hash-Wert aus
					-- Datei lesen
       
       writedata768(daten768);  	-- verschluesselten Hash an Chip schreiben
       assert false report "verschluesselter Hash wurde uebertragen" severity note;
       readline(testdaten,l);
       read(l,Zertifikat,good);
       writedata768(Zertifikat);  	-- Zertifikat uebertragen
       assert false report "Zertifikat wurde uebertragen" severity note;
       fertig := false;
       while not fertig loop
	   wait for 50 * PERIOD;
	   readstatus(status);
	   if (status(3) = '1') or (status(7) = '1')   then
	      fertig := true; 
	   end if;
       end loop;
       assert not (status(3) = '1') report "auth ok!!!" severity note;
       assert     (status(3) = '1') report "auth inkorrekt!!!!" severity failure;
       assert false report "auth durchgelaufen" severity note;
       
   end verify_auth;



   -- purpose: Tests the PIN changing Function
   procedure change_pin is
       
   begin  -- pin_aendern
       writecommand("01100");
       readline (testdaten,l);
       read(l,pin,good);
       writedata(pin);
       assert false report "Pin was changed" severity note;
   end change_pin;



   -- purpose: Tests changing the Certification Organisation
   procedure change_cert is
      
       
   begin  -- change_cert
      writecommand("01101");
       readline(testdaten,l);
       read(l,temprsakey,good);
       readline(testdaten,l);
       read(l,temprsakeymod,good);
       writedata768 (temprsakey);
       writedata768 (temprsakeymod);
       assert false report "New key for the Cert-Organisation was written" severity note;  
   end change_cert;


   -- purpose: Tests the access on the personal data
   procedure read_public is
       
   begin  -- read_public
       writecommand("00110");
       readline(testdaten,l);
       read(l,kontrolldaten768);
       
       readdata768(daten768);
       
       assert (daten768 = kontrolldaten768) report "Public Key im RAM ist nicht korrekt" severity failure;
       assert not (daten768 = kontrolldaten768) report "Public ist korrekt im RAM gespeichert!" severity note;
       readline(testdaten,l);
       read(l,kontrolldaten768);
       readdata768(daten768);
       assert (daten768 = kontrolldaten768) report "Modul im RAM ist nicht korrekt" severity failure;
       assert not (daten768 = kontrolldaten768) report "Modul ist korrekt im RAM gespeichert!" severity note;
       
       readline(testdaten,l);
       read(l,kontrolldaten768);
       readdata768(daten768);
       assert (daten768 = kontrolldaten768) report "Zertifikat im RAM ist nicht korrekt" severity failure;
       assert not (daten768 = kontrolldaten768) report "Zertifikat ist korrekt im RAM gespeichert!" severity note;
   end read_public;

   -- purpose: Tests the card-change detection
   procedure card_change is
       
   begin  -- card_change
       if busy = '1' then
	   wait until busy = '0';
       end if;
       CARDCHANGE <= '0';
       wait for 2*PERIOD;
       CARDCHANGE <= '1';
       assert false report "sended CARD-CHANGE Signal" severity note;
   end card_change;


   -- purpose: Tests the Input of PIN 
   procedure check_pin is
       
   begin  -- check_pin
     readline (testdaten,l);
     read(l,pin);
     writedata(pin);
     readstatus(status);
     assert status(7) = '1' report "Falsche PIN eingegeben!!" severity note;
     assert status(7) = '0' report "Richtige PIN eingegeben!!" severity note;
     write(l,status(7 downto 0));
     read(l,t,good);
     assert false  report t severity note;    
	 
   end check_pin;
		
   -- purpose: <description>
   procedure do_ram_test is
       
   begin  -- do_ram_test
     writecommand_ramtest("0001010");
     writedata("11111111111111111111111111111111");
     wait until busy = '0';  
     writecommand_ramtest("0001010");
     readline(testdaten,l);
     read(l,pin);
     writedata(pin);
     readline(testdaten,l);
     read(l,pin);
     writedata(pin);
     readstatus(status);
     while status(7) /= '1' loop
	 readstatus(status);
     end loop ;
     assert status(2) = '1'  report "ramtest(1) fehler" severity note;
     assert not status(2) = '1' report "ramtest(1) ok!!" severity note;
     writecommand_ramtest("1001010");
     readline(testdaten,l);
     read(l,pin);
     writedata(pin);
     readline(testdaten,l);
     read(l,pin);
     writedata(pin);
     readstatus(status);
     while status(7) /= '1' loop
	 readstatus(status);
     end loop ;
     assert status(2) = '1'  report "ramtest(2) fehler" severity failure;
     assert not status(2) = '1' report "ramtest(2) ok!!" severity note;
   end do_ram_test;        

   -- purpose: liest die Namen der Entwickler vom Chip
   procedure read_names is
       
   begin  -- read_names
       writecommand("01000");
       for i in 0 to 255 loop
	  
	  readdata(daten32);
	  write(outl, daten32);
	  writeline(ausgabe,outl);
	  
	  
       end loop;  -- i 
   end read_names;

   -- purpose: Liest die persoenlichen Daten aus dem Ram
   procedure read_pers_data is
       
   begin  -- read_pers_data
       writecommand("01111");
       for i in 0 to 84 loop
	  readline(testdaten,l);
	  read(l,kontrolldaten32,good);
	  readdata(daten32);
	  assert daten32=kontrolldaten32 report "auslesen der pers. Daten gescheiter" severity failure;
	  assert not (daten32=kontrolldaten32)  report "auslese der per. Daten korrekt" severity note;
       end loop;  -- i
       
       
   end read_pers_data;

          
   begin
       CARDCHANGE <= '1';
       IO_MODE <= "00";
       WR <= '1' after 1 ns;
       OE <= '1' after 1 ns;
       CE <= '1' after 1 ns;
       A  <= "000" after 1 ns;
       SCL_IN <='1' after 1 ns;  			-- AENDERUNGEN WEGEN SERIO
       SDA_IN <= '1' after 1 ns;  			-- VON DIRK GEAENDERT.
       wait for period /4;
       assert False report "Test started" severity note;
       RES <= '1';
       wait for 32*PERIOD;
       RES <= '0';
       assert False report "Chip resetted" severity note;

       readline (testdaten, l);
       read     (l,t, good);
       case t is
	   when "PARALLEL_32" => mode := par32;
				 IO_MODE <= "11";
	   when "PARALLEL_16" => mode := par16;
				 IO_MODE <= "10";
	   when "PARALLEL_08" => mode := par8;
				 IO_MODE <= "01";
	   when "SERIAL__I2C" => mode := ser;
				 IO_MODE <= "00";
	   when others => assert false report "Invalid Input Mode" severity failure;
       end case;
       --modezw := IO_MODE;
       --modzw := mode;
     --IO_MODE <= "11";
      --mode := par32;
       --readstatus(status);
       --write(l,status(7 downto 0));
       --read(l,tstatus);
       assert false  report t severity note;
       Init : for j in 0 to 6 loop
	  readline (testdaten,l);
	  read     (l,memblock);
	  writedata768(memblock);  
       end loop Init;
       -- write the PIN to the chip
       readline (testdaten,l);
       read (l,pin);
       writedata(pin);
       -- write the last 63 32-Bit Memblocks 
       
       readline (testdaten,l);
       read (l,memoryrest);

       writedata2784(memoryrest);
       --writedata768(memblock); -- Filling the last 2 768-Bit  Memory-Words
       --writedata768(memblock);

       readline   (testdaten,l);  	-- Write the Initial Value of the LSFR
       read       (l,daten64);
       writedata64(daten64);
       assert false report "RAM on Chip initialized" severity note;
        --IO_MODE <= modezw;
        --mode := modzw;
       while not endfile(testdaten) loop
	  
         readline (testdaten,l);
	 read(l,t,good);
	 case t  is
	     when "ENCRYPT_RSA"             => encrypt_rsa;
	     when "DECRYPT_RSA"             => decrypt_rsa;
	     when "EN_SING_ECB"       => encrypt_des;
	     when "EN_SING_CBC"       => encrypt_des;
	     when "EN_EEE2_ECB"  => encrypt_des;
	     when "EN_EEE2_CBC"  => encrypt_des;
	     when "EN_EEE3_ECB"  => encrypt_des;
	     when "EN_EEE3_CBC"  => encrypt_des;
	     when "EN_EDE3_ECB"  => encrypt_des;
	     when "EN_EDE3_CBC"  => encrypt_des;
	     when "DE_SING_ECB"       => decrypt_des;
	     when "DE_SING_CBC"       => decrypt_des;
	     when "DE_EEE2_ECB"  => decrypt_des;
	     when "DE_EEE2_CBC"  => decrypt_des;
	     when "DE_EEE3_ECB"  => decrypt_des;
	     when "DE_EEE3_CBC"  => decrypt_des;
	     when "DE_EDE3_ECB"  => decrypt_des;
	     when "DE_EDE3_CBC"  => decrypt_des;			       
	     when "SIGN___MESS"                    => sign;
	     when "VERIFY_SIGN"             => verify_sign;
	     when "AUTHENTICAT"            => authenticate;
	     when "VERIFY_AUTH"             => verify_auth;
	     when "CHANGE_PIN_"              => change_pin;
	     when "CHANGE_CERT"             => change_cert;
	     when "READ_PUBLIC"             => read_public;
	     when "CARD_CHANGE"             => card_change;
	     when "CHECK_PINNR"             => check_pin;
	     when "READ_P_DATA"          => NULL; --  read_pers_data;
	     when others => assert false report "Invaild Command" severity failure;
         end case;
       end loop;
       
       wait;
   end process;

	
   I_1 : OUTHER_CHIP
      Port Map ( A(2 downto 0)=>A(2 downto 0), CARDCHANGE=>CARDCHANGE,
                 CE=>CE, CLK=>CLK, D_IN(31 downto 0)=>D_IN(31 downto 0),
                 IO_MODE(1 downto 0)=>IO_MODE(1 downto 0), OE=>OE,
                 RES=>RES, SCL_IN=>SCL_IN, SDA_IN=>SDA_IN, WR=>WR,
                 BUSY=>BUSY, D_OUT(31 downto 0)=>D_OUT(31 downto 0),
                 D_OUT_EN=>D_OUT_EN, INT=>INT, SCL_OUT=>SCL_OUT,
                 SDA_OUT=>SDA_OUT,
		 TEST_SE => TEST_SE,
		 TEST_SI => TEST_SI,
		 TEST_SO => TEST_SO  );

end BEHAVIOUR;

entity CHIP_TB_SUB is
end CHIP_TB_SUB;

architecture TEST of CHIP_TB_SUB is
    component CHIP_TB
    end component;
begin 
   I_CHIP_TB : CHIP_TB;
end TEST;
