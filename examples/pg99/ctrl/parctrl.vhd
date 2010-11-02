--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------

-- Designers :  Dirk Allmendinger 
-- Group     :  CTRL
--------------------------------------------------------------------------

-- Design Unit Name : paralleles Steuerwerk 
-- Purpose :
-- 
-- File Name : parctrl.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 98
--------------------------------------------------------------------
-- Date		   | Changes 
--	17.11.1998 | 	27.01.1999
--                 | 
--                 |
-----------------------------------------------------------------------



library HAPRA_GATE;
   library HAPRA_RTL;
   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

entity PARCTRL is
      Port (    ADDR : In    std_logic_vector (2 downto 0);
                  CE : In    std_logic;
                 CLK : In    std_logic;
             DREGINEN : In    std_logic;
             DREGOUTEN : In    std_logic;
             MODESEL : In    std_logic_vector (1 downto 0);
                  OE : In    std_logic;
               RESET : In    std_logic;
                  RW : In    std_logic;
                SADR : In    std_logic_vector (2 downto 0);
               SREAD : In    std_logic;
              SWRITE : In    std_logic;
                 ADR : Out   std_logic_vector (2 downto 0);
             CTRLCHANGE : Out   std_logic;
             CTRLINEN : Out   std_logic;
             DATAVALID : Out   std_logic;
              DOUTEN : Out   std_logic;
              OE_OUT : Out   std_logic;
             REGINEN : Out   std_logic_vector (3 downto 0);
              SOUTEN : Out   std_logic );
end PARCTRL;

architecture SCHEMATIC of PARCTRL is

type STATES is 

    (
	-- INIT & CO
	RESET_1,
	IDLE, COMMAND_1, COMMAND_2, STATUS_1, DATAREAD_1, DATAREAD_2, DATAREAD_3,
	DATAREAD_4, DATAREAD_5, DATAWRITE_1,
	DATAWRITE_2, SELECT_AKTIVITY
	);



signal STATE_CTRL_OUT : STATES;
signal STATE_CTRL_IN : STATES;
signal NEWSTATE : STATES;
signal NEWSTATE_IN : STATES;
signal DVAL : std_logic_vector(3 downto 0);  -- Signal DATAVALID_SET
signal DATA_OUT, DATA_OUT_ZW : std_logic;  -- Signal DOUTEMPTY_RESET
signal DATAREAD, DATAWRITE, COMMAND, STATUS, AKTIV, CTRLVAL : std_logic;
signal ADRESSE : std_logic_vector(2 downto 0);
signal DOUTVAL_REG, DATAVAL_REG : std_logic_vector(3 downto 0);
signal DATAVAL_OUT, DOUTVAL_OUT : std_logic;

begin

 -- purpose: speicherung des zustands, reset ,comando
    process(CLK,RESET)
    begin
	if RESET='1' then
	    STATE_CTRL_OUT <= RESET_1;
-- 	    STATE_CTRL_IN <= RESET_1;
	elsif CLK'event AND CLK='1' then
--             if S_RES = '1' then
-- 		STATE <= RESET;
-- 	    else
	    STATE_CTRL_OUT <= NEWSTATE;
-- 	    STATE_CTRL_IN  <= NEWSTATE_IN;
-- 	    end if;
	end if;
    end process;

-- purpose: Zustandsuebergangsfunktion
-- type:    memoryless
-- inputs:  
-- outputs: 
CTRL_OUT : process (STATE_CTRL_OUT, DATAREAD, DATAWRITE, COMMAND, STATUS, AKTIV,
		    ADRESSE, MODESEL)
    
begin  -- process CTRL_OUT

DVAL <= "0000";
DATA_OUT <= '0';
CTRLVAL <= '0';

    
case STATE_CTRL_OUT is

    when RESET_1 =>

	DVAL <= "0000";
	DATA_OUT <= '0';
	DATA_OUT_ZW <= '0';
	CTRLVAL <= '0';
	NEWSTATE <= IDLE;
    
    when IDLE =>

--	if AKTIV = '0' then
-- 	    NEWSTATE <= SELECT_AKTIVITY;
-- 	else
-- 	    NEWSTATE <= IDLE;
-- 	end if;

--     when SELECT_AKTIVITY =>

	if DATAREAD = '0' then
	    NEWSTATE <= DATAREAD_1;
	elsif DATAWRITE = '0' then
	    NEWSTATE <= DATAWRITE_1;
	elsif COMMAND = '0' then
	    NEWSTATE <= COMMAND_1;
	elsif STATUS = '0' then
	    NEWSTATE <= STATUS_1;
	else
	    NEWSTATE <= IDLE;
	end if;
	    
    when DATAREAD_1 =>

	case MODESEL is
	    when "10" =>
		if ADRESSE(0) = '0' then
		    NEWSTATE <= DATAREAD_2;
		else
		    NEWSTATE <= DATAREAD_4;
		end if;

	    when "11" =>
		NEWSTATE <= DATAREAD_4;
	    when others =>
		case ADRESSE is
		    when "000" => NEWSTATE <= DATAREAD_2;
		    when "001" => NEWSTATE <= DATAREAD_2;
		    when "010" => NEWSTATE <= DATAREAD_2;
		    when others => NEWSTATE <= DATAREAD_4;
		end case;
	end case;
--	NEWSTATE <= DATAREAD_2;
	
    when DATAREAD_2 =>

	
	if DATAREAD = '1' then
	    NEWSTATE <= DATAREAD_3;
	else
	    NEWSTATE <= DATAREAD_2;
	end if;

    when DATAREAD_3 =>

	DATA_OUT <= '0';
	NEWSTATE <= IDLE;
	
    when DATAREAD_4 =>

	
	if DATAREAD = '1' then
	    NEWSTATE <= DATAREAD_5;
	else
	    NEWSTATE <= DATAREAD_4;
	end if;

    when DATAREAD_5 =>

	DATA_OUT <= '1';
	NEWSTATE <= IDLE;

    when STATUS_1 =>

	if STATUS = '1'  then
	    NEWSTATE <= IDLE;
	else
	    NEWSTATE <= STATUS_1;
	end if;

    when DATAWRITE_1 =>

	case MODESEL is
	    when "11" => DVAL <= "1111";
	    when "10" =>
		if ADRESSE(0) = '0' then
		    DVAL <= "0011";
		else
		    DVAL <= "1100";
		end if;
	    when others =>
		case ADRESSE is
		    when "000" => DVAL <= "0001";
		    when "001" => DVAL <= "0010";
		    when "010" => DVAL <= "0100";
		    when others => DVAL <= "1000";
		end case;
		
	end case;
	NEWSTATE <= DATAWRITE_2;

    when DATAWRITE_2 =>

	DVAL <= "0000";
	if DATAWRITE = '1' then
	    NEWSTATE <= IDLE;
	else
	    NEWSTATE <= DATAWRITE_2;
	end if;

    when COMMAND_1 =>

	CTRLVAL <= '1';
	NEWSTATE <= COMMAND_2;

    when COMMAND_2 =>

	CTRLVAL <= '0';
	if COMMAND = '1' then
	    NEWSTATE <= IDLE;
	else
	    NEWSTATE <= COMMAND_2;
	end if;

    when others =>

	NEWSTATE <= RESET_1;
       
end case;
    
end process CTRL_OUT;

-- purpose: FF FUER DATAVALID
-- type:    memorizing
-- inputs:  CLK, RESET
-- outputs: 
DATA_VALID_CREATE : process (CLK, RESET)
    
begin  -- process DATA_VALID_CREATE
    -- activities triggered by asynchronous reset (active high)
    if RESET = '1' then
	DATAVAL_REG <= "0000";
	DATAVAL_OUT <= '0';
    -- activities triggered by rising edge of clock
    elsif CLK'event and CLK = '1' then
	if DVAL(3)= '1' then
	    DATAVAL_OUT <= '1';
	end if;
	if DREGINEN ='1' then
	    DATAVAL_OUT <= '0';
	end if;
    end if;
end process DATA_VALID_CREATE;

DOUTEMPTY_CREATE : process (CLK, RESET)
    
begin  -- process DOUTEMPTY_CREATE
    -- activities triggered by asynchronous reset (active high)
    if RESET = '1' then
	DOUTVAL_OUT <= '1';
    -- activities triggered by rising edge of clock
    elsif CLK'event and CLK = '1' then
	if DATA_OUT ='1' then
	    DOUTVAL_OUT <= '1';
	end if;
	if DREGOUTEN = '1' then
	    DOUTVAL_OUT <= '0';
	end if;
    end if;
end process DOUTEMPTY_CREATE;

DATAREAD <= not(SREAD and not SADR(2)) when MODESEL = "00" else 
            (CE or OE or ADDR(2));

DATAWRITE <= not(SWRITE and not SADR(2)) when MODESEL = "00" else
	     (CE or RW or ADDR(2));

COMMAND <= not(SWRITE and SADR(2)) when MODESEL = "00" else
	   (CE or RW or not ADDR(2));

STATUS <=  not(SREAD and SADR(2)) when MODESEL = "00" else
	   (CE or OE or not ADDR(2));
 
AKTIV <= (DATAREAD and DATAWRITE and COMMAND and STATUS);

OE_OUT <= (CE or OE);

ADRESSE <= SADR when MODESEL = "00" else
	   ADDR;

ADR <= ADRESSE;

DATAVALID <= DATAVAL_OUT;

CTRLCHANGE <= CTRLVAL;

DOUTEN <= DOUTVAL_OUT;
SOUTEN <= DOUTVAL_OUT;

REGINEN <= DVAL;
CTRLINEN <= CTRLVAL;

end SCHEMATIC;

configuration CFG_PARCTRL_SCHEMATIC of PARCTRL is

   for SCHEMATIC
   end for;

end CFG_PARCTRL_SCHEMATIC;
