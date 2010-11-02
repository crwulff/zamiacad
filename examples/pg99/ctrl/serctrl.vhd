--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------

-- Designers :  Dirk Allmendinger 
-- Group     :  CTRL
--------------------------------------------------------------------------

-- Design Unit Name : serielles Steuerwerk
-- Purpose :
-- 
-- File Name : serctrl.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 98
--------------------------------------------------------------------
-- Date		   | Changes 
--	17.11.1998 | 	27.01.1999
--                 | 
--                 |
-----------------------------------------------------------------------



   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity SERCTRL is
      Port (     CLK : In    std_logic;
	  
             DATAVALID : In    std_logic;
               RESET : In    std_logic;
                SCLK : In    std_logic;
             SOUTEMPTY : In    std_logic;
                 SDAIN : In    std_logic;
                SSEL : In    std_logic_vector (2 downto 0);
               START : In    std_logic;
                STOP : In    std_logic;
                MODE : Out   std_logic_vector (1 downto 0);
             SADDROUT : Out   std_logic_vector (2 downto 0);
              SCLOUT : Out   std_logic;
               SREAD : Out   std_logic;
               SEREN : Out   std_logic;
	       SHIFT : Out   std_logic;
             SREGINEN : Out   std_logic;
             SREGOUTEN : Out   std_logic;
              SWRITE : Out   std_logic;
	 STARTSTOPRES : out std_logic );
end SERCTRL;

architecture SCHEMATIC of SERCTRL is


signal BITS : std_logic_vector(2 downto 0);  -- SCHIEBEREGISTERZAEHLER
signal ADDRESSE : std_logic_vector(1 downto 0);  -- SERADDR ZAEHLER
signal ADDR_RESET : std_logic;
signal ADDRRESET : std_logic;
signal ADDR_INC : std_logic;
signal BITS_RESET : std_logic;
signal BITSRESET : std_logic;
signal BITS_INC : std_logic;
signal SER_ADDR : std_logic_vector(2 downto 0);
signal SER_ADR : std_logic;
signal SER_WRITE : std_logic;
signal SER_READ : std_logic;
signal SER_REGINEN : std_logic;
signal SER_REGOUTEN : std_logic;
signal SER_CLK : std_logic;
signal SER_SCLOUT : std_logic;
signal SER_SCLOUT_NEW : std_logic;
signal SER_MODE : std_logic_vector(1 downto 0);
signal SER_NEW_MODE : std_logic_vector(1 downto 0);
signal I2C_NEW_MODE : std_logic_vector(2 downto 0);
signal I2C_MODE : std_logic_vector(2 downto 0);
signal stres : std_logic;
signal WEITER : std_logic;
signal SER_NEW_ADDR : std_logic_vector(2 downto 0);


type STATES is 

    (
	-- INIT & CO
	RESET_1,
	START_1, STOP_1, IDLE, DATA_VALID_WRITE, SOUT_EMPTY_READ, BITS_PLUS,
	-- NEW COMMAND
	ADDR_1, ADDR_2,
	-- STATUS
	STATUS_1,
	-- COMMAND
	COMMAND_1, COMMAND_2,
	-- DATA SCHREIBEN
	DATA_W_1, DATA_W_2, DATA_W_3, DATA_W_4,
	-- DATA LESEN
	DATA_R_1, DATEN_ADDR, DATA_R_2, DATA_R_3, DATA_R_4, 
	-- SCHIEBEN
	SCHIEBEN, SCHIEBEN_1, SCHIEBEN_2, SCHIEBEN_3, SCHIEBEN_35, SCHIEBEN_36,
	SCHIEBEN_4, SCHIEBEN_45, SCHIEBEN_46, SCHIEBEN_5,
	-- ACKN SEND
	ACKN_SEND, ACKN_SEND_1, ACKN_SEND_2, ACKN_SEND_3,
	-- EXTRA ACKN
	ACKN_ADDR, ACKN_ADDR_1,
	-- WAIT ACKN
	ACKN_WAIT, ACKN_WAIT_1, ACKN_WAIT_2, ACKN_WAIT_25,
	-- DATA GET
	DATA_GET, DATA_GET_1, DATA_GET_2,
	-- DATA PUT
	DATA_PUT, DATA_PUT_1, DATA_PUT_2
	);

-- type MODUS is 
--   (
--       ADDR, STATUS, CTRL, DATAR, DATAW
--       );


signal STATE : STATES;
signal NEWSTATE : STATES;
-- signal I2C_MODE : MODUS;


begin

 -- purpose: speicherung des zustands, reset ,comando
    process(CLK,RESET)
    begin
	if RESET='1' then
	    STATE <= RESET_1;
	elsif CLK'event AND CLK='1' then
            if start='1' then
                
		STATE <= START_1;
		
	    elsif stop='1' then
                
		STATE <= STOP_1;
		
	    else
	    STATE <= NEWSTATE;
	    end if;
	end if;
    end process;



    
-- purpose: Serieller Verzweigungsbaum Wurzel
-- type:    memoryless
-- inputs:  
-- outputs: 
sermain : process     (STATE, START, STOP, SCLK, SDAIN, SOUTEMPTY,
		       DATAVALID, SSEL, BITS, I2C_MODE, I2C_NEW_MODE, SER_MODE,
		       SER_NEW_MODE, ADDRESSE, SER_SCLOUT_NEW, SER_ADDR)
    
begin  -- process sermain
    
BITS_RESET <= '0';
BITS_INC <= '0';
ADDR_INC <= '0';
ADDR_RESET <= '0';
SER_REGINEN <= '0';
SER_REGOUTEN <= '0';
STRES <= '0';
SER_WRITE <= '0';
SER_SCLOUT <= SER_SCLOUT_NEW;
SER_READ <= '0';
-- SER_ADDR(2 downto 0) <=  SER_ADRESSE_2 & ADDRESSE(1 downto 0);
-- SER_ADR <= SER_ADRESSE_2
SER_NEW_ADDR <= SER_ADDR;
SER_CLK <= '0';
WEITER <= '0';
SER_NEW_MODE <= SER_MODE;
I2C_NEW_MODE <= I2C_MODE;



case STATE is
    when RESET_1 =>

	SER_SCLOUT <= '1';
	STRES <= '1';
	SER_NEW_MODE <= "00";
        SER_NEW_ADDR <= "100";
        SER_CLK <= '0';
	SER_READ <= '0';
	SER_WRITE <= '0';
        STRES <= '0';
	NEWSTATE <= IDLE;

    when IDLE =>

	SER_SCLOUT <= '1';
	STRES <= '0';
	NEWSTATE <= IDLE;

-- NEW COMMAND
	
    when START_1 =>
	
        STRES <= '1';
	I2C_NEW_MODE <= "000";
	SER_NEW_MODE <= "00";
	SER_NEW_ADDR <= "000";
	NEWSTATE <= SCHIEBEN;

    when ADDR_1 =>

	if SSEL(0)='1' then
	    NEWSTATE <= ACKN_ADDR;
	else
	    NEWSTATE <= IDLE;  		-- spaeter auf RESET
	end if;

    when ADDR_2 =>

	SER_SCLOUT <= '0';  		-- Verzoegerung der Clock Leitung beginnen
	
	case SSEL(2 downto 1) is
	    
	    when "10"  => NEWSTATE <= COMMAND_1;
	    when "01"  => NEWSTATE <= DATA_R_1;
	    when "00"  => NEWSTATE <= DATA_W_1;
	    when others   => NEWSTATE <= STATUS_1;
			  
	end case;

-- STATUS
	
    when STATUS_1 =>

        I2C_NEW_MODE <= "011";
	SER_NEW_ADDR(2 downto 0) <= "100";
	SER_NEW_MODE <= "01";
	NEWSTATE <= DATA_GET;

-- COMMAND
	
    when COMMAND_1 =>
	I2C_NEW_MODE <= "010";
	SER_NEW_MODE <= "00";
        NEWSTATE <= SCHIEBEN;

    when COMMAND_2 =>

	SER_NEW_ADDR(2 downto 0) <= "100";
	NEWSTATE <= DATA_PUT;
	

-- DATA SCHREIBEN

    when DATA_W_1 =>

	I2C_NEW_MODE <= "100";
	SER_NEW_MODE <= "00";
	ADDR_RESET <= '1';
	NEWSTATE <= SCHIEBEN;

    when DATA_W_2 =>

	SER_SCLOUT <= '0';
	SER_NEW_ADDR(1 downto 0) <= ADDRESSE(1 downto 0);
	SER_NEW_ADDR(2) <= '0';
	NEWSTATE <= DATA_PUT;

    when DATA_W_3 =>

	if ADDRESSE(1 downto 0) = "11" then
	    NEWSTATE <= DATA_VALID_WRITE;
	else
	    NEWSTATE <= DATA_W_4;
	end if;

    when DATA_W_4 =>
	ADDR_INC <= '1';
	NEWSTATE <= SCHIEBEN;

    when DATA_VALID_WRITE =>

	if DATAVALID = '0' then
	    NEWSTATE <= DATA_W_1;
	else
	    NEWSTATE <= DATA_VALID_WRITE;
	end if;

-- DATA LESEN


    when DATA_R_1 =>

	I2C_NEW_MODE <= "101";
	ADDR_RESET <= '1';
	SER_NEW_MODE <= "01";
	SER_ADR <= '0';
	NEWSTATE <= DATEN_ADDR;

    when DATEN_ADDR =>

	SER_NEW_ADDR(1 downto 0) <= ADDRESSE(1 downto 0);
	
	SER_NEW_MODE <= "01";
	NEWSTATE <= DATA_GET;

    when DATA_R_2 =>

	SER_SCLOUT <= '0';  		-- Verzoegern
        NEWSTATE <= DATA_R_3;

    when DATA_R_3 =>
	
	if ADDRESSE(1 downto 0) = "11" then
	    NEWSTATE <= SOUT_EMPTY_READ;
	else
	    NEWSTATE <= DATA_R_4;
	end if;

    when DATA_R_4 =>

	ADDR_INC <= '1';
	NEWSTATE <= DATEN_ADDR;

    when SOUT_EMPTY_READ =>
	SER_SCLOUT <= '1';
	if SOUTEMPTY= '0' then
	    NEWSTATE <= DATA_R_1;
	else
	    NEWSTATE <= SOUT_EMPTY_READ;
	end if;

-- SCHIEBEN
	
    when SCHIEBEN =>
        STRES <= '0';
	SER_SCLOUT <= '1';
	if SCLK = '0' then
	    NEWSTATE <= SCHIEBEN_1;
	else
	    NEWSTATE <= SCHIEBEN;
	end if;

    when SCHIEBEN_1 =>

	SER_CLK <= '0';
	SER_SCLOUT <= '1';
	BITS_RESET <= '1';
	NEWSTATE <= SCHIEBEN_2;

    when SCHIEBEN_2 =>
        BITS_RESET <= '0';
	BITS_INC <= '0';
	if SCLK = '1' then
	    NEWSTATE <= SCHIEBEN_35;
	else
	    NEWSTATE <= SCHIEBEN_2;  	-- wait for SCLK = 1
	end if;

    when SCHIEBEN_35 =>
	SER_CLK <= '1';
	NEWSTATE <= SCHIEBEN_36;
	
    when SCHIEBEN_36 =>
	SER_CLK <= '0';
	NEWSTATE <= SCHIEBEN_3;
	
    when SCHIEBEN_3 =>
        
	if SCLK = '0' then
	    NEWSTATE <= SCHIEBEN_45;
	else
	    NEWSTATE <= SCHIEBEN_3;
	end if;

    when SCHIEBEN_45 =>
	WEITER <= '1';
	NEWSTATE <= SCHIEBEN_46;

    when SCHIEBEN_46 =>
	WEITER <= '0';
	NEWSTATE <= SCHIEBEN_4;

    when SCHIEBEN_4 =>

	if BITS = "111" then
	    NEWSTATE <= SCHIEBEN_5;
	else
	    NEWSTATE <= BITS_PLUS;
	end if;

    when BITS_PLUS =>

	BITS_INC <= '1';
	NEWSTATE <= SCHIEBEN_2;
 
    when SCHIEBEN_5 =>
        SER_CLK <= '0';
	if I2C_MODE = "000" then
	    NEWSTATE <= ADDR_1;
	else
	   case SER_MODE(1 downto 0) is
	       when "00" => NEWSTATE <= ACKN_SEND;
	       when others => NEWSTATE <= ACKN_WAIT;
	   end case;
        end if;
-- ACKN SEND
	
    when ACKN_SEND =>

	SER_NEW_MODE(1 downto 0) <= "10";
	NEWSTATE <= ACKN_SEND_1;

    when ACKN_SEND_1 =>

	if SCLK = '1' then
	    NEWSTATE <= ACKN_SEND_2;
	else
	    NEWSTATE <= ACKN_SEND_1;
	end if;

    when ACKN_SEND_2 =>

	if SCLK = '0' then
	    NEWSTATE <= ACKN_SEND_3;
	else
	    NEWSTATE <= ACKN_SEND_2;
	end if;

    when ACKN_SEND_3 =>

	SER_NEW_MODE(1 downto 0) <= "00";
        
	case I2C_MODE is
	    when "100" => NEWSTATE <= DATA_W_2;
	    when "000" => NEWSTATE <= ADDR_2;
	    when others => NEWSTATE <= COMMAND_2;
	end case;

-- extra ACKN
	
    when ACKN_ADDR =>

	if SSEL(2)='0' then
	    NEWSTATE <= ACKN_ADDR_1;
	else
	    NEWSTATE <= ACKN_SEND;
	end if;


--    when ACKN_ADDR_1 =>
--
--	if SSEL(1)='0' then
--	    NEWSTATE <= ACKN_ADDR_2;
--	else
--	    NEWSTATE <= IDLE;  		-- spaeter RESET
--	end if;

    when ACKN_ADDR_1 =>

	case SSEL(2 downto 1) is
	    
	    when "10"  => NEWSTATE <= ACKN_SEND;
	    when "01"  => if SOUTEMPTY = '0' then
			      NEWSTATE <= ACKN_SEND;
			  else
			      NEWSTATE <= IDLE;  -- spaeter RESET
	                  end if;
	    when "00"  => if DATAVALID = '0' then
			      NEWSTATE <= ACKN_SEND;
			  else
			      NEWSTATE <= IDLE;  -- spaeter RESET
			  end if; 
	    when others   => NEWSTATE <= ACKN_SEND;
			  
	end case;
	-- if DATAVALID='0' then
	--    NEWSTATE <= ACKN_SEND;
	-- else
	--    NEWSTATE <= IDLE;  		-- spaeter RESET
	-- end if;

-- WAIT ACKN

    when ACKN_WAIT =>

	SER_NEW_MODE <= "00";
	if SCLK='1' then
	    NEWSTATE <= ACKN_WAIT_1;
	else
	    NEWSTATE <= ACKN_WAIT;
	end if;

    when ACKN_WAIT_1 =>

	if SDAIN = '0' then
	    NEWSTATE <= ACKN_WAIT_25;
	else
	    NEWSTATE <= IDLE;  		-- NOT ACKN
	end if;

    when ACKN_WAIT_25 =>

	if SCLK = '0'  then
	    NEWSTATE <= ACKN_WAIT_2;
	else
	    NEWSTATE <= ACKN_WAIT_25;
	end if;

    when ACKN_WAIT_2 =>
	case I2C_MODE is
	    when "101" => NEWSTATE <= DATA_R_2;
	    when others => NEWSTATE <= RESET_1;
	end case;

-- DATA get
	
    when DATA_GET =>
	SER_READ <= '1';
	SER_REGINEN <= '1';
	NEWSTATE <= DATA_GET_1;

    when DATA_GET_1 =>

	SER_READ <= '1';
	SER_REGINEN <= '1';
	NEWSTATE <= DATA_GET_2;

    when DATA_GET_2 =>

	SER_REGINEN <= '0';
	SER_READ <= '0';
	NEWSTATE <= SCHIEBEN;

-- DATA put

    when DATA_PUT =>

	SER_REGOUTEN <= '1';
	SER_WRITE <= '1';
	NEWSTATE <= DATA_PUT_1;

    when DATA_PUT_1 =>

	SER_REGOUTEN <= '1';
	SER_WRITE <= '1';
	NEWSTATE <= DATA_PUT_2;
	
    when DATA_PUT_2 =>

	SER_REGOUTEN <= '0';
	SER_WRITE <= '0';
	case I2C_MODE is
	    when "100" => NEWSTATE <= DATA_W_3;
	    when others => NEWSTATE <= IDLE;  		-- spaeter RESET
	end case;

    when STOP_1 =>
        STRES <= '1';
	NEWSTATE <= RESET_1;
	
end case;


end process sermain;


-- purpose: ADDRESS ZAEHLER
-- type:    memorizing
-- inputs:  clk, addr_reset, <signal names>
ADDR_ZAEHLER : process (clk, addrreset, ADDR_INC, RESET)
    
begin  -- process ADDR_ZAEHLER
    -- activities triggered by asynchronous reset (active high)
    if reset = '1' then
	ADDRESSE(1 downto 0) <= "00";
	
    -- activities triggered by rising edge of clock
    elsif clk'event and clk = '1' then
	if addr_inc = '1' then
	    ADDRESSE(1 downto 0) <= ADDRESSE(1 downto 0) + "01";
	end if;
        if ADDR_RESET = '1' then
	    ADDRESSE <= "00";
	end if;
    end if;
end process ADDR_ZAEHLER;


-- purpose: 8 Bit zaehlen
-- type:    memorizing
-- inputs:  clk, BITS_RESET, BITS_PLUS
-- outputs: 
BIT_ZAEHLER : process (clk, BITSRESET, BITS_INC, RESET)
    
begin  -- process BIT_ZAEHLER
    -- activities triggered by asynchronous reset (active HIGH)
    if RESET = '1' then
	BITS(2 downto 0) <= "000";
	
    -- activities triggered by rising edge of clock
    elsif clk'event and clk = '1' then
	if BITS_INC ='1' then
	    BITS <= BITS + "001";
	end if;
        if BITS_RESET = '1' then
	    BITS <= "000";
	end if;
    end if;
end process BIT_ZAEHLER;

-- purpose: SER_MODE FF
-- type:    memorizing
-- inputs:  clk, RESET
-- outputs: 
SM : process (clk, RESET)
    
 begin  -- process SM
    -- activities triggered by asynchronous reset (active low)
     if RESET = '1' then
 	SER_MODE <= "00";
    -- activities triggered by rising edge of clock
     elsif clk'event and clk = '1' then
	SER_MODE <= SER_NEW_MODE;
     end if;
end process SM;

-- purpose: I2C_MODE FF
-- type:    memorizing
-- inputs:  clk, RESET
-- outputs: 
IM : process (clk, RESET)
    
begin  -- process IM
    -- activities triggered by asynchronous reset (active low)
    if RESET = '1' then
	I2C_MODE <= "000";
	SER_SCLOUT_NEW <= '1';
    -- activities triggered by rising edge of clock
    elsif clk'event and clk = '1' then
	I2C_MODE <= I2C_NEW_MODE;
	SER_SCLOUT_NEW <= SER_SCLOUT;
    end if;
end process IM;

ser_adresse_bit2 : process (clk, reset)
    
begin  -- process ser_adresse_bit2
    -- activities triggered by asynchronous reset (active low)
    if reset = '1' then
	SER_ADDR <= "000";
    -- activities triggered by rising edge of clock
    elsif clk'event and clk = '1' then
	SER_ADDR <= SER_NEW_ADDR;
    end if;
end process ser_adresse_bit2;

SHIFT <= WEITER;
SREAD <= SER_READ;
SWRITE <= SER_WRITE;
SREGINEN <= SER_REGINEN;
SREGOUTEN <= SER_REGOUTEN;
SEREN <= SER_CLK;
SADDROUT(2 downto 0) <= SER_ADDR(2 downto 0);
-- SADDROUT(2) <= SER_ADRESSE_2;
SCLOUT <= SER_SCLOUT_NEW;
MODE(1 downto 0) <= SER_MODE(1 downto 0);
STARTSTOPRES <= STRES;
end SCHEMATIC;

configuration CFG_SERCTRL_SCHEMATIC of SERCTRL is

   for SCHEMATIC
   end for;

end CFG_SERCTRL_SCHEMATIC;
