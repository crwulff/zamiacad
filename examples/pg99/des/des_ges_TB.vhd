--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designer  : Thomas Stanka              
-- Group     : DES
--------------------------------------------------------------------
-- Design Unit Name : DES_GES
-- Purpose : Part of the DES-module-core for the cryptochip "pg99"
-- 
-- File Name :  ges_ges_TB.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date 25.11.98   | Changes
--      10.01.99   | expand the testbench from ECB encrypt to test en-decrypt 
--                 | both for ECB, EEE3, EDE3, CBC-single
--	21.01.99   | added the Modi for CBC-Tripple DES 
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  contents: Testbench for the entity DES
--
--  the Testbench will test for ECB, EEE3, EDE3,CBC-single encrypt and decrypt.
--  if those mode do well, we can expect, that everything (except OW-Hash)
--  will do well.
--  The Testbench uses one file per Mode and direction 
--  There is no restriction to expand the testbench to use more (or less) testfiles
--------------------------------------------------------------------------  

   library LIB_des;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;
   use IEEE.std_logic_textio.all;
   use std.textio.all;

entity DES_GES is
end DES_GES;

architecture SCHEMATIC of DES_GES is

   type STATETYP is (keyready, keygone,ivready, ivgone, dataready, datagone, getting);
   type TST_MODE  is (ECBe, ECBd, CBCe, CBCd, EEe, EEd, EEEe, EEEd, EDEe, EDEd, HASH,
		      CBC_EEe, CBC_EEd, CBC_EEEe,CBC_EEEd, CBC_EDEe, CBC_EDEd, fertig);
   constant PERIOD : time := 7.5 ns;
   constant delay : time := 1 ns;  	-- to delay the signals after clk
   constant setup : time  := 25 ps;  	-- the time between high and low
   signal DATA_OUT : std_logic_vector(63 downto 0);
   signal  DATA_IN : std_logic_vector(63 downto 0);
   signal    MODUS : std_logic_vector(4 downto 0);
   signal    ERROR : std_logic;
   signal KEY_PARITY : std_logic;
   signal DES_READY : std_logic;
   signal DATA_ACK : std_logic;
   signal    RESET : std_logic;
   signal      CLK : std_logic;
   signal     TEST : std_logic;
   signal BUFFER_FREE : std_logic;
   signal DATA_READY : std_logic;
   signal DATA_IS_KEY : std_logic;
   
   signal test_se : std_logic;
   signal test_si : std_logic;
   signal test_so : std_logic;
   
   signal testmode : TST_MODE;  	-- to test one mode after another
   signal status : STATETYP;  	-- only for debuging
   signal FOREVER : STD_LOGIC;

   component DES_STD_TB
      Port ( BUFFER_FREE : In    std_logic;
                 CLK : In    std_logic;
             DATA_IN : In    std_logic_vector (63 downto 0);
             DATA_IS_KEY : In    std_logic;
             DATA_READY : In    std_logic;
               MODUS : In    std_logic_vector (4 downto 0);
               RESET : In    std_logic;
                TEST : In    std_logic;
             DATA_ACK : Out   std_logic;
             DATA_OUT : Out   std_logic_vector (63 downto 0);
             DES_READY : Out   std_logic;
               ERROR : Out   std_logic;
             KEY_PARITY : Out   std_logic;
	     test_se : in std_logic;
	     test_si : in std_logic;
	     test_so : out std_logic
	     );
   end component;
 
begin

   DES : DES_STD_TB
      Port Map ( BUFFER_FREE=>BUFFER_FREE, CLK=>CLK,
                 DATA_IN(63 downto 0)=>DATA_IN(63 downto 0),
                 DATA_IS_KEY=>DATA_IS_KEY, DATA_READY=>DATA_READY,
                 MODUS(4 downto 0)=>MODUS(4 downto 0), RESET=>RESET,
                 TEST=>TEST, DATA_ACK=>DATA_ACK,
                 DATA_OUT(63 downto 0)=> DATA_OUT(63 downto 0),
                 DES_READY=>DES_READY, ERROR=>ERROR,
                 KEY_PARITY=>KEY_PARITY,
		 test_se => test_se, test_si => test_si, test_so => test_so);

   generate_reset : RESET <= '1', '0' after 4.4*PERIOD;

   -- purpose: Clock Generator
   -- type:    memoryle
   -- outputs: CLK
   generate_clock : process
       
   begin  -- process generate_clock
       for i in 1 to 100000 loop
       CLK <= '0'; --  'X', '0' after setup ;
       wait for PERIOD;
       CLK <= '1'; -- 'X', '1' after setup;
       wait for PERIOD;
       end loop; -- i
       wait on FOREVER;
   end process generate_clock;

   -- purpose: generates signals to test the DES-MODUL-CORE
   -- type:    memorizing
   -- inputs:  CLK, RESET, DATA_ACK, DES_READY, KEY_PARITY, ERROR
   -- outputs: DATA_IN, MODUS, DATA_IS_KEY, DATA_READY, BUFFER_FREE, TEST

   generate_test : process

       ------------------------------------------------------------------------
       -- the Testdata for encryption are without number, the testdata for
       -- decryption with number '1' for .txt 		    
       ------------------------------------------------------------------------
       
       file testdaten1 : text is in "ecbdaten.txt" ;
       file testdaten2 : text is in "cbcdaten.txt" ;
       file testdaten3 : text is in "EEEdaten.txt" ;
       file testdaten4 : text is in "EDEdaten.txt" ;
       file testdaten5 : text is in "ecbdaten1.txt" ;
       file testdaten6 : text is in "cbcdaten1.txt" ;
       file testdaten7 : text is in "EEEdaten1.txt" ;
       file testdaten8 : text is in "EDEdaten1.txt" ;       
       file testdaten9 : text is in "EEdaten.txt" ;
       file testdaten0 : text is in "EEdaten1.txt" ;
       file testdaten11 : text is in "hashdaten.txt";
       file testdaten12 : text is in "cbcEEdat.txt";
       file testdaten13 : text is in "cbcEEdat1.txt";
       file testdaten14 : text is in "cbcEEEdat.txt";
       file testdaten15 : text is in "cbcEEEdat1.txt";
       file testdaten16 : text is in "cbcEDEdat.txt";
       file testdaten17 : text is in "cbcEDEdat1.txt";
      
       file ausgabe : text is out "ergebnis_gat_66Mhz.txt";  -- hier kommen die Ergebnisse als Bitvektor
       variable STATE : STATETYP;  		-- state of the Controllunit
       variable l : line;
       variable breite : width := 32;  	-- textwidth of the output
       variable IS_GOOD : boolean;  	-- needed for the read command, never tested
       variable desdata : STD_LOGIC_VECTOR(63 downto 0);  -- variable to read from the file
       variable keycount : integer;  	-- keycounter for EEE-2/3 and EDE
       variable changemode : boolean;
       
   begin  -- process generate_test
       wait until CLK'event;
       wait for delay;
       -- activities triggered by asynchronous reset (active high)
       if RESET = '1' then
	   assert CLK='1' report "reset aktiv" severity note;
	   STATE:=keyready;
	   testmode <= ECBe;  		-- start with ECB encryption
	   BUFFER_FREE <='0';
	   DATA_READY <='0';
	   TEST <='0';
	   MODUS <="00000";
	   keycount:=0;
	   changemode:=false;
	   DATA_IS_KEY <='0';
	   DATA_IN <= (others => '0');
	   
	   test_si <='0';
	   test_se <='0';
	   
       -- activities triggered by rising edge of clock
       elsif CLK = '1' then	   
	   if STATE=keyready and DATA_ACK='0' and testmode=ECBe then -- ECBencrypt
	       readline(testdaten1,l);
	       read(l,desdata,IS_GOOD); 
	       if desdata(1)='U'  then        -- EOF
		   desdata:=(others => '0');  -- then close file with a row of 0 
		   testmode <= CBC_EEe;  	      -- next do ECB decrypt
		   changemode:=true;
	       end if;
	       assert false report "ECB encrypt start" severity note;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X','1' after setup;
	       DATA_IS_KEY <= 'X','1' after setup;
	       MODUS <="XXXXX","10000" after setup;  	-- do single-DES in ECB-mode encryption
	       STATE:=keygone;
	   end if;

	   if STATE=keygone and DATA_ACK='1' then  -- simulate time to load the register new
	       DATA_READY <='0';	       
	       case MODUS(1 downto 0) is
		   when "00" => STATE:=dataready;  -- only one key needed 
		   when "01" => if keycount=1 then  -- two keys needed EEE2
				    STATE:=dataready;
				else
				    STATE:=keyready;
				end if;
		   when "10"|"11" =>if keycount=2 then  -- three keys needed EEE3|EDE
				    STATE:=dataready;
				else
				    STATE:=keyready;
				end if;
		   when others => null;
	       end case;
	       keycount:=keycount+1;
	       if MODUS(2)='1' and STATE=dataready then    -- CBC, load IV first
		   STATE:=ivready;
	       end if;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=ECBe then
	       readline(testdaten1,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= ECBd;  	-- next do ECB decrypt
		   changemode:=true;
	       end if;
	       
	       DATA_IN <= desdata;
	       DATA_READY <= 'X','1' after setup;
	       DATA_IS_KEY <= 'X','0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

	   if STATE=ivgone and DATA_ACK='1' then
	       DATA_READY <= 'X','0' after setup;
               keycount:=0;
	       STATE:=dataready;
	   end if;

	   if STATE=datagone and DATA_ACK='1' then
	       DATA_READY <= 'X', '0' after setup;
	       BUFFER_FREE <= 'X', '1' after setup;
	       keycount:=0;
	       STATE:=getting;
	   end if;
	   

	   --------------------------------------------------------------------
           -- Now follow the parts ECB decrypt, EEE, EDE and CBC en-decrypt
	   -- similar to the parts ECBe above
           --------------------------------------------------------------------
           
	   --------------------------------------------------------------------
           -- ECB decrypt
           --------------------------------------------------------------------
           
	   if STATE=keyready and DATA_ACK='0' and testmode=ECBd then -- ECB decrypt
	       readline(testdaten5,l);
	       read(l,desdata,IS_GOOD); 
	       if desdata(1)='U'  then        -- EOF
		   desdata:=(others => '0');  -- dann mit 0. .0 abschliesen 
		   testmode <= EEe;	      -- next EEE2 encrypt
		   changemode:=true;
	       end if;
	       assert false report "ECB decrypt start" severity note;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="11000";  	-- do single-DES in ECB-mode decryption
	       STATE:=keygone;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=ECBd then
	       readline(testdaten5,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <=EEe;  	-- next do EEE encrypt
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

           ------------------------------------------------------------------------
           -- CBC encrypt
           ------------------------------------------------------------------------

	   if STATE=keyready and DATA_ACK='0' and testmode=CBCe then  -- CBC encrypt
	       readline(testdaten2,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= CBCd;
		   changemode:=true;
	       end if;
	       assert false report "CBC encrypt start" severity note;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="10100";  	-- do single-DES in CBC-mode encryption
	       STATE:=keygone;
	   end if;

	   if STATE=ivready and DATA_ACK='0' and testmode=CBCe then
	       readline(testdaten2,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= CBCd;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=ivgone;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=CBCe then
	       readline(testdaten2,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <=CBCd;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

           --------------------------------------------------------------------
           -- CBC decrypt
           --------------------------------------------------------------------
           
	   if STATE=keyready and DATA_ACK='0' and testmode=CBCd then  -- CBC encrypt
	       readline(testdaten6,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= HASH;
		   changemode:=true;
	       end if;
	       assert false report "CBC decrypt start" severity note;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="11100";  	-- do single-DES in CBC-mode decryption
	       STATE:=keygone;
	   end if;

	   if STATE=ivready and DATA_ACK='0' and testmode=CBCd then
	       readline(testdaten6,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= HASH;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=ivgone;
	   end if;
	   
	   if STATE=dataready and DATA_ACK='0' and testmode=CBCd then
	       readline(testdaten6,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <=HASH;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

	   --------------------------------------------------------------------
           -- EEE2 encrypt
           --------------------------------------------------------------------
           
	   if STATE=keyready and DATA_ACK='0' and testmode=EEe then
	       readline(testdaten9,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= EEd;  
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="10001";  	-- do EEE2 in ECB-mode encryption
	       STATE:=keygone;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=EEe then
	       readline(testdaten9,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= EEd;  
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

	   --------------------------------------------------------------------
           -- EEE2 decrypt
           --------------------------------------------------------------------
           
	   if STATE=keyready and DATA_ACK='0' and testmode=EEd then
	       readline(testdaten0,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= EEEe;  	
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="11001";  	-- do EEE2 in ECB-mode decryption
	       STATE:=keygone;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=EEd then
	       readline(testdaten0,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= EEEe;  
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

	   --------------------------------------------------------------------
           -- EEE3 encrypt
           --------------------------------------------------------------------
           
	   if STATE=keyready and DATA_ACK='0' and testmode=EEEe then
	       readline(testdaten3,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= EEEd;  
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="10010";  	-- do EEE3 in ECB-mode encryption
	       STATE:=keygone;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=EEEe then
	       readline(testdaten3,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= EEEd;  
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

	   --------------------------------------------------------------------
           -- EEE3 decrypt
           --------------------------------------------------------------------
           
	   if STATE=keyready and DATA_ACK='0' and testmode=EEEd then
	       readline(testdaten7,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= EDEe;  	
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="11010";  	-- do EEE3 in ECB-mode decryption
	       STATE:=keygone;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=EEEd then
	       readline(testdaten7,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= EDEe;  
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

	   --------------------------------------------------------------------
           -- EDE3 encrypt
           --------------------------------------------------------------------
           
	   if STATE=keyready and DATA_ACK='0' and testmode=EDEe then
	       readline(testdaten4,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= EDEd;  	-- test beendet
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="10011";  	-- do EDE3 in ECB-mode encryption
	       STATE:=keygone;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=EDEe then
	       readline(testdaten4,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= EDEd;   
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

	   --------------------------------------------------------------------
           -- EDE3 decrypt
           --------------------------------------------------------------------
           
	   if STATE=keyready and DATA_ACK='0' and testmode=EDEd then
	       readline(testdaten8,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= CBCe;  	
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="11011";  	-- do EDE3 in ECB-mode decryption
	       STATE:=keygone;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=EDEd then
	       readline(testdaten8,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= CBCe;  	
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

	   --------------------------------------------------------------------
           --  OW-Hash
           --------------------------------------------------------------------
           
	   if STATE=keyready and DATA_ACK='0' and testmode=HASH then
	       readline(testdaten11,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
                   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= CBC_EEEe;  	
	       end if;
	       assert false report "hash start" severity note;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="00000";  	-- do OW-Hash
	       STATE:=datagone;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=HASH then
	       readline(testdaten11,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   changemode:=true;
		   testmode <= CBC_EEEe;  	
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  	       
	   end if;
	   


	   --------------------------------------------------------------------
           -- CBC EEE2 encrypt
           --------------------------------------------------------------------

	   if STATE=keyready and DATA_ACK='0' and testmode=CBC_EEe then  -- CBC encrypt
	       readline(testdaten12,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= CBC_EEd;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="10101";  	-- do double-DES EE2 in CBC-mode encryption
	       STATE:=keygone;
	   end if;

	   if STATE=ivready and DATA_ACK='0' and testmode=CBC_EEe then
	       readline(testdaten12,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= CBC_EEd;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=ivgone;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=CBC_EEe then
	       readline(testdaten12,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <=CBC_EEd;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

	   --------------------------------------------------------------------
           -- CBC EEE2 decrypt
           --------------------------------------------------------------------
           
	   if STATE=keyready and DATA_ACK='0' and testmode=CBC_EEd then  -- CBC encrypt
	       readline(testdaten13,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= CBC_EEEe;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="11110";  	-- do EEE3 in CBC-mode decryption
	       STATE:=keygone;
	   end if;

	   if STATE=ivready and DATA_ACK='0' and testmode=CBC_EEd then
	       readline(testdaten13,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= CBC_EEEe;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=ivgone;
	   end if;
	  
	   if STATE=dataready and DATA_ACK='0' and testmode=CBC_EEd then
	       readline(testdaten13,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <=CBC_EEEe;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;
	   
	   --------------------------------------------------------------------
           -- CBC EEE3 encrypt
           --------------------------------------------------------------------

	   if STATE=keyready and DATA_ACK='0' and testmode=CBC_EEEe then  -- CBC encrypt
	       readline(testdaten14,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= CBC_EEEd;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="10101";  	-- do double-DES EEE3 in CBC-mode encryption
	       STATE:=keygone;
	   end if;

	   if STATE=ivready and DATA_ACK='0' and testmode=CBC_EEEe then
	       readline(testdaten14,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= CBC_EEEd;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=ivgone;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=CBC_EEEe then
	       readline(testdaten14,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <=CBC_EEEd;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

	   --------------------------------------------------------------------
           -- CBC EEE3 decrypt
           --------------------------------------------------------------------
           
	   if STATE=keyready and DATA_ACK='0' and testmode=CBC_EEEd then  -- CBC encrypt
	       readline(testdaten15,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= CBC_EDEe;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="11110";  	-- do EEE3 in CBC-mode decryption
	       STATE:=keygone;
	   end if;

	   if STATE=ivready and DATA_ACK='0' and testmode=CBC_EEEd then
	       readline(testdaten15,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= CBC_EDEe;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=ivgone;
	   end if;
	   
	   if STATE=dataready and DATA_ACK='0' and testmode=CBC_EEEd then
	       readline(testdaten15,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <=CBC_EDEe;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

	   --------------------------------------------------------------------
           -- CBC EDE3 encrypt
           --------------------------------------------------------------------

	   if STATE=keyready and DATA_ACK='0' and testmode=CBC_EDEe then  -- CBC encrypt
	       readline(testdaten16,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= CBC_EDEd;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="10111";  	-- do double-DES EDE3 in CBC-mode encryption
	       STATE:=keygone;
	   end if;

	   if STATE=ivready and DATA_ACK='0' and testmode=CBC_EDEe then
	       readline(testdaten16,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= CBC_EDEd;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=ivgone;
	   end if;

	   if STATE=dataready and DATA_ACK='0' and testmode=CBC_EDEe then
	       readline(testdaten16,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <=CBC_EDEd;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;

	   --------------------------------------------------------------------
           -- CBC EDE3 decrypt
           --------------------------------------------------------------------
           
	   if STATE=keyready and DATA_ACK='0' and testmode=CBC_EDEd then  -- CBC encrypt
	       readline(testdaten17,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= fertig;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '1' after setup;
	       MODUS <="11111";  	-- do EDE3 in CBC-mode decryption
	       STATE:=keygone;
	   end if;

	   if STATE=ivready and DATA_ACK='0' and testmode=CBC_EDEd then
	       readline(testdaten17,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <= fertig;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=ivgone;
	   end if;
	   
	   if STATE=dataready and DATA_ACK='0' and testmode=CBC_EDEd then
	       readline(testdaten17,l);
	       read(l,desdata,IS_GOOD);
	       if desdata(1)='U'  then
		   desdata:=(others => '0');
		   testmode <=fertig;
		   changemode:=true;
	       end if;
	       DATA_IN <= desdata;
	       DATA_READY <= 'X', '1' after setup;
	       DATA_IS_KEY <= 'X', '0' after setup;
	       STATE:=datagone;  		--now the key and data are loaded
	   end if;
	   
	   if STATE=getting then
	       if DES_READY='1'  then
		   BUFFER_FREE <= '0' after 2*period;  	-- now we got the cipher 
		   write(l, DATA_OUT,left, breite);	-- write output in file
		   writeline(ausgabe, l);		-- "ergebnis.txt" as Bitvektor
		   if testmode=fertig then
		       assert false report "Simulation Erfolgreich beendet(66MHz)" severity failure;
		   end if;
		   if changemode=true then
		       STATE:=keyready;  	-- next mode
		       write(l, MODUS, right, breite);
		       writeline(ausgabe, l);
		       changemode:=false;
		   else
		       STATE:=dataready;  	-- start from the beginning    
		   end if;
	       else
		   BUFFER_FREE <= 'X', '1' after setup;
	       end if;
	   end if;
       end if;
       STATUS <= STATE;
   end process generate_test;
   
end SCHEMATIC;



configuration CFG_DES_TB_GAT of DES_GES is

   for SCHEMATIC
      for DES: DES_STD_TB
	  -- use configuration LIB_des.CFG_DES_SCHEMATIC;        -- The structure
	   use entity LIB_des.DES(GAT);
      end for;
   end for;

end CFG_DES_TB_GAT;




