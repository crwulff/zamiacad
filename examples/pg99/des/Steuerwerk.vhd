--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Markus Busch
-- Group     : DES
--------------------------------------------------------------------
-- Design Unit Name : Steuerwerk_des
-- Purpose :
-- 
-- File Name : Steuerwerk.vhd 
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
--   03 Dez. 98    | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  contents: Hier wird ein FSM implementiert der die komplette
--	      DES steuert und die einzelnen komponenten zur richtigen
--	      Zeit ansteuert.
--  
--------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;
use STD.STANDARD.all;

entity Steuerwerk is
    
    port (reset : in std_logic;
	  clk : in std_logic;
	  data_ready : in std_logic;			-- es werden daten bereitgestellt 
	  Buffer_free : in std_logic;			-- Wegschreiben der Ergebnisse
	  Modus : in std_logic_vector(4 downto 0);	-- Auswahl der DES Modien
          Test : in std_logic;
	  data_is_key : in std_logic;
	  DES_ready1 : out std_logic;			-- Fertig mit einer Blockverschluesselung
	  data_ack1 : out std_logic;			-- Wir haben die Daten
							-- ordungsgemaess uebernommen
	  en_decrypt_1 : out std_logic;			-- Schieberichtung
							-- fuer Ver- und Entschluesselung
	  control_1 : out std_logic_vector(1 downto 0);	-- Auswahl der Anzahl
							-- der Schiebungen 
	  write_en_1 : out std_logic;			-- Daten einlesen ins Schieberegister
	  latch_enIV : out std_logic;			-- Daten ins 64-Bit Register Laden
	  latch_en164 : out std_logic;			-- Daten ins 64 Bit Register laden
	  latch_en32RL : out std_logic;			-- Daten ins rechte 32-Bit
							-- Register  Laden  
	  Mux_sel_1 : out std_logic;			-- select-Leitung des Mux1 
	  Mux_sel_2 : out std_logic;			-- select-Leitung des Mux2
	  Mux_sel_3 : out std_logic;			-- select-Leitung des Mux3
	  Mux_sel_4 : out std_logic;			-- select-Leitung des Mux4
	  Mux_sel_5 : out std_logic;			-- select-Leitung des Mux5
	  Mux_sel_6 : out std_logic;			-- select-Leitung des Mux6
          Mux_sel_7 : out std_logic;			-- select Leitung fuer 3-Des
	  keystEen : out std_logic;			-- Schluessel Speichern im Register
	  keystSel : out std_logic_vector(1 downto 0)); -- Auswahl des Schluessels
							-- der verwendet wird
end Steuerwerk;


architecture RTL of Steuerwerk is

  type TState is (s_reset,awaitdata,keyload,keyload2,keyload3,HashDKload,Regload_Hash,getdate_hash,hash,
		  datetokey,busySingle_en,busySingle_de,single_en,single_de,busyEEE2_en,busyEEE2_de,
		  eee2_en,eee2_de,keychangeEEE2_en,keychangeEEE2_de,busyEEE3_en,busyEEE3_de,eee3_en,eee3_de,
		  keychangeEEE3_en,keychangeEEE3_de,busyEDE3_en,busyEDE3_de,ede3_de,ede3_en,keychangeEDE3_en,
		  keychangeEDE3_de,IVload_en,getdate_en,cbc_en,IVload_de,firstdate,getdate_de,await_cbcdata,
		  Regload,cbc_de,readyCBC_en,readyQuit_cbcen,readyCBC_de,readyQuit_cbcde,readyHash,readyQuit_hash,
		  ready,ready_quit,await_cbcdata_de,keyback ,cbceee3_en,cbceee3_de,cbcede3_en,cbcede3_de,
		  IVloadcbceee2en,fd_cbceee2, cbceee2_en, keycbceee2_en, getdatecbceee2_en,readyCBCEEE2_en,
		  readyQuitcbceee2_en, awaitcbceee2data,IVloadcbceee2_de,RegloadCBCeee2,getdatecbceee2_de,
		  cbceee2_de,keycbcEEE2_de,readyCBCEEE2_de, readyQuitcbceee2_de,awaitcbceee2data_de,
		  IVloadcbceee3en,fd_cbceee3,keycbceee3_en, getdatecbceee3_en,readyCBCEEE3_en,
		  readyQuitcbceee3_en, awaitcbceee3data,IVloadcbceee3_de,RegloadCBCeee3,getdatecbceee3_de,
		  keycbcEEE3_de,readyCBCEEE3_de, readyQuitcbceee3_de,awaitcbceee3data_de,
		  IVloadcbcede3en,fd_cbcede3,keycbcede3_en, getdatecbcede3_en,readyCBCEdE3_en,
		  readyQuitcbcede3_en, awaitcbcede3data,IVloadcbcede3_de,RegloadCBCede3,getdatecbcede3_de,
		  keycbcEdE3_de,readyCBCEdE3_de, readyQuitcbcede3_de,awaitcbcede3data_de);
  
  type count is range 0 to 2;
  type count2 is range 0 to 15;
  signal countsig : std_logic;
  signal countsig3 : std_logic;  	-- signal fuer den 2 bit zaehler
  signal key_sig : std_logic;
  signal keysigres : std_logic;
  signal key_no : count;
  signal counter : count2;  	-- zaehler fuer Schleifendurchlaeufe
  signal counter3 : count;  -- zaehler fuer die
			    -- Anzahl der DES Verschluesselungen
  signal state, nextState : TState;
  
  attribute state_vector : string;
  attribute state_vector of RTL : architecture is "state";
  
    begin
	
	-- purpose: Zustandsaenderung
	state_switch : process (clk, reset)           
	begin  -- process state_switch
	       -- activities triggered by asynchronous reset (active high)
	    if RESET = '1' then
		    STATE <= S_RESET;
	    elsif (CLK'event and CLK = '1') then
		    state <= nextState;
	    end if;
	end process state_switch;     
	     

	count_switch : process (clk, reset)           
	begin  -- process count_switch
	       -- activities triggered by asynchronous reset (active high)
	    if reset='1' then
		counter <= 0;
	    elsif (CLK'event and CLK = '1') then
		if countsig = '1' then
		    case counter is
			when 0 => counter <= 1;
			when 1 => counter <= 2;
			when 2 => counter <= 3;
			when 3 => counter <= 4;
			when 4 => counter <= 5;
			when 5 => counter <= 6;
			when 6 => counter <= 7;
			when 7 => counter <= 8;
			when 8 => counter <= 9;
			when 9 => counter <= 10;
			when 10 => counter <= 11;
			when 11 => counter <= 12;
			when 12 => counter <= 13;
			when 13 => counter <= 14;
			when 14 => counter <= 15;
			when 15 => counter <= 0;
			when others => counter <= 0;
		    end case;
		end if;
	    end if;
	end process count_switch;

	counter_switch : process (clk, reset)           
	begin  -- process counter_switch
	       -- activities triggered by asynchronous reset (active high)
	    if reset='1' then
		counter3 <=0;
	    elsif (CLK'event and CLK = '1') then
		if countsig3 ='1' then
		   case counter3 is
			when 0 => counter3 <= 1;
			when 1 => counter3 <= 2;
			when 2 => counter3 <= 0;
			when others => counter3 <= 0;
		    end case; 
		end if;
	    end if;
	end process counter_switch;

	key_switch : process (clk, reset)           
	begin  -- process key_switch
	       -- activities triggered by asynchronous reset (active high)
	    if reset = '1' then
		key_no <= 0;
	    elsif(CLK'event and CLK = '1') then
		if key_sig = '1' then
		    case key_no is
			when 0 => key_no <= 1;
			when 1 => key_no <= 2; 
			when 2 => key_no <= 0;
			when others => key_no <= 0;
		    end case;
		elsif keysigres ='1' then
		    key_no <= 0;
		end if;
	    end if;
	end process key_switch; 

	next_state_logic : process (clk,reset) -- state,Modus ,data_ready,data_is_key,Buffer_free,
				    --counter,counter3,key_no)
			 	     
	begin  -- process next_state_logic

	 Mux_sel_1 <= '0';
	 Mux_sel_2 <= '0';
	 Mux_sel_3 <= '0';
	 Mux_sel_4 <= '0';
	 Mux_sel_5 <= '0';
	 Mux_sel_6 <= '0';
	 Mux_sel_7 <= '0';
	 keystSel <= "00";
	 en_decrypt_1 <= '0';
	 write_en_1 <= '0';
	 latch_enIV <= '0';
         latch_en32RL <= '0';
         latch_en164 <= '0';
	 DES_ready1 <= '0';
	 data_ack1 <= '0';
	 keystEen <= '0';
	 control_1 <= "00";
	 countsig3 <= '0';  	       
	 countsig <= '0';
	 key_sig <= '0';
	 keysigres <= '0';
	 
	 case state is  		-- auswahl der FSM logic Zustaende
	    when s_reset  => Des_ready1 <= '0';
                             en_decrypt_1 <= '0';
                             control_1 <= "00";
                             write_en_1 <= '0';
                             latch_enIV <= '0';
                             latch_en32RL <= '1';
                             latch_en164 <= '0';
                             Mux_sel_1 <= '0';
			     Mux_sel_2 <= '0';
                             Mux_sel_3 <= '0';
                             Mux_sel_4 <= '0';
                             Mux_sel_5 <= '0';
                             Mux_sel_6 <= '0';
			     Mux_sel_7 <= '0';
			     keystSel <= "00";
                             keystEen <= '0';
			     countsig <= '0';
			     countsig3 <= '0';
			     key_sig <= '0';
			     keysigres <= '0';
			     nextState <= awaitdata;
			     
	    when awaitdata => if (data_is_key = '1' and data_ready = '1') then
                                  if Modus(4) = '0'  then
				      latch_en32RL <= '1';
				      keystEen <= '1';
				      latch_enIV <= '1';
				      nextState <= HashDKload;
				  else
				      case key_no is
					  when 0 => keystEen <= '1';
						    nextState <= keyload;
					  when 1 => keystEen <= '1';
						    keystSel <="01";
						    nextState <= keyload2;
					  when others => keystEen <= '1';
							 keystSel <= "10";
							 nextState <= keyload3;
				      end case;
				  end if;
	                      elsif data_ready = '1' then
				  case Modus(3 downto 0) is
				      when "0000" => write_en_1 <= '1';
					              nextState <= busySingle_en;
				      when "1000" =>  nextState <= busySingle_de;
				      when "0001" => write_en_1 <='1';
					              nextState <= busyEEE2_en;
				      when "1001" => nextState <= busyEEE2_de;
				      when "0010" => write_en_1 <= '1';
					             nextState <= busyEEE3_en;
				      when "1010" => nextState <= busyEEE3_de;
				      when "0011" => write_en_1 <= '1';
					             nextState <= busyEDE3_en;
				      when "1011" => nextState <= busyEDE3_de;
				      when "0100" => latch_enIV <= '1';
				                     nextState <= IVload_en;
				      when "1100" => latch_enIV <= '1';
					             nextState <= IVload_de;

				      when "0101" => latch_enIV <= '1';
					             nextState <= IVloadcbceee2en;
				      when "1101" => latch_enIV <= '1';
					             nextState <= IVloadcbceee2_de;
				      when "0110" => latch_enIV <= '1';
					             nextState <= IVloadcbceee3en;
				      when "1110" => latch_enIV <= '1';
					             nextState <= IVloadcbceee3_de;
				      when "0111" => latch_enIV <= '1';
					             nextState <= IVloadcbcede3en;
				      when "1111" => latch_enIV <= '1';
					             nextState <= IVloadcbcede3_de;
				      when others => null;
				  end case;
				  if Modus(4) = '0' then  -- hash fortsetzen
				      latch_enIV <= '1';
				      Mux_sel_4 <= '1';
				      Mux_sel_3 <= '1';
				      Mux_sel_2 <= '1';
				      keystEen <= '1';
				      nextState <= datetokey;
				  end if;
			      else
				  nextState <= awaitdata;  
			      end if;

	     when keyload => Mux_sel_4 <= '0';
			     keystEen <= '1';
			     keystSel <= "00";
			     data_ack1 <= '1';
			     if (Modus(1 downto 0)="00") then
				 nextState <= keyback;
			     else
				 key_sig <= '1';
				 nextState <= awaitdata;
			     end if;
			   
	     when keyload2 => keystEen <= '1';
			      keystSel <= "01";
			      data_ack1 <= '1';
			      if Modus(1 downto 0)="01" then
				  nextState <= keyback;
			      else
				  key_sig <= '1';
				  nextState <= awaitdata;
			      end if;
			      
	     when keyload3 => keystEen <= '1';
			      keystSel <= "10";
			      data_ack1 <= '1';
		              nextState <=keyback;

	     when keyback => keysigres <= '1';
			     nextState <= awaitdata;

	     when busySingle_en => control_1 <= "01";
			           latch_en32RL <= '1';
				   data_ack1 <= '1';	      
				   nextState <= single_en;
				   
             when busySingle_de => write_en_1 <= '1';
				   en_decrypt_1 <= '1';
	                           latch_en32RL <= '1';
				   data_ack1 <= '1';	      
				   nextState <= single_de;

	     when single_en => countsig <= '1';
			       latch_en32RL <= '1';
			       case counter is
				   when 0 | 7 | 14 => control_1 <= "01";
				   when 1 | 2 | 3 | 4 | 5 | 6 | 8 |
				     9 | 10 | 11 | 12 | 13 => control_1 <= "11";
				   when 15 => control_1 <= "00";
				   when others => null;
			       end case;
				  Mux_sel_5 <= '1';
				  Mux_sel_6 <= '1';
				  if counter < 15  then
				      nextState <= single_en;
				  else		       	
				      nextState <= ready;
				  end if;

	    when single_de => countsig <= '1';
			      en_decrypt_1 <= '1';  
			      latch_en32RL <= '1';
			      case counter is
				  when 0 | 7 | 14 => control_1 <= "01";
				  when 1 | 2 | 3 | 4 | 5 | 6 | 8 |
				       9 | 10 | 11 | 12 | 13 => control_1 <= "11";
				  when 15 => control_1 <= "00";
				  when others => null;
			      end case;
			      Mux_sel_5 <= '1';
			      Mux_sel_6 <= '1';
			      if counter < 15  then
				  nextState <= single_de;
			      else		       	
				  nextState <= ready;
			      end if;		

	    when busyEEE2_en => Mux_sel_4 <= '1';
				control_1 <= "01";
				latch_en32RL <= '1';
				data_ack1 <= '1';
				nextState <= eee2_en;

	    when busyEEE2_de => Mux_sel_4 <= '1';
				en_decrypt_1 <= '1';   
				latch_en32RL <= '1';
				write_en_1 <= '1';  
				data_ack1 <= '1';
				nextState <= eee2_de;

	    when eee2_en => countsig <= '1';
			    latch_en32RL <= '1';
			    if counter3 = 0 then
				keystSel <= "01";
			    elsif counter3 = 1 then
				keystSel <= "00";
			    end if;
			    case counter is
				when 0 | 7 | 14 => control_1 <= "01";
				when 1 to 6 | 8 to 13  => control_1 <= "11";
				when 15 => control_1 <= "00";
			       when others => null;
			    end case;
			    Mux_sel_5 <= '1';
			    Mux_sel_6 <= '1';
			    if (counter < 15) then
				nextState <= eee2_en;
			    elsif counter3 < 2 then
				countsig3 <= '1';
				write_en_1 <= '1';
				nextState <= keychangeEEE2_en;
			    else
				countsig3 <= '1';
				nextState <= ready;
			 end if;

	    when eee2_de => countsig <= '1';
			    en_decrypt_1 <= '1'; 	
			    latch_en32RL <= '1';	
			    if counter3 = 0 then
				keystSel <= "01";
			    elsif counter3 = 1 then
				keystSel <= "00";
			    end if;
			    case counter is
				when 0 | 7 | 14  => control_1 <= "01";
				when 1 to 6 | 8 to 13 => control_1 <= "11";
				when 15 => control_1 <= "00";
				when others => null;	
			    end case;
			    Mux_sel_5 <= '1';
			    Mux_sel_6 <= '1';
			    if (counter < 15) then
				nextState <= eee2_de;
			    elsif counter3 < 2 then
				countsig3 <= '1';
				write_en_1 <= '1';
				nextState <= keychangeEEE2_de;
			    else
				countsig3 <= '1';
				nextState <= ready;
			 end if;			

	    when keychangeEEE2_en => Mux_sel_7 <= '1';
				     Mux_sel_5 <= '1';
				     latch_en32RL <= '1';
				     control_1 <= "01";
				     nextState <= eee2_en;
				
	    when keychangeEEE2_de => Mux_sel_7 <= '1';
				     Mux_sel_5 <= '1';
				     latch_en32RL <= '1';
				     nextState <= eee2_de;

            when busyEEE3_en => Mux_sel_4 <= '1';
				control_1 <= "01";     
				latch_en32RL <= '1';
				data_ack1 <= '1';
				nextState <= eee3_en;

	    when busyEEE3_de =>	Mux_sel_4 <= '1';
				en_decrypt_1 <= '1';   
				latch_en32RL <= '1';
				write_en_1 <= '1';  
				data_ack1 <= '1';
				nextState <= eee3_de;			

	    when eee3_en => countsig <= '1';
			    latch_en32RL <= '1';
			    if counter3 = 0 then
				keystSel <= "01";
			    elsif counter3 = 1 then
				keystSel <= "10";
			    end if;
			    case counter is
				when 0 | 7 | 14 => control_1 <= "01";
				when 1 to 6| 8 to 13 => control_1 <= "11";
				when 15 => control_1 <= "00";
				when others => null;
			    end case;
			    Mux_sel_5 <= '1';
			    Mux_sel_6 <= '1';
			    if (counter < 15)  then
				nextState <= eee3_en;  
			    elsif counter3 < 2 then
				countsig3 <= '1';
				write_en_1 <='1';
				nextState <= keychangeEEE3_en;
			    else
				countsig3 <= '1';
				nextState <= ready;
			    end if;

	    when keychangeEEE3_en => Mux_sel_7 <= '1';
			             Mux_sel_5 <= '1';
				     latch_en32RL <= '1';
				     control_1 <= "01";
				     nextState <= eee3_en;

	    when eee3_de => countsig <= '1';
			    en_decrypt_1 <= '1'; 	   
			    latch_en32RL <= '1';
			    if counter3 = 0 then
				keystSel <= "01";
			    elsif counter3 = 1 then
				keystSel <= "10";
			    end if;
			    case counter is
				when 0 | 7 | 14 => control_1 <= "01";
				when 1 to 6 | 8 to 13 => control_1 <= "11";
				when 15 => control_1 <= "00";
				when others => null;
			    end case;
			    Mux_sel_5 <= '1';
			    Mux_sel_6 <= '1';
			    if (counter < 15)  then
				nextState <= eee3_de;  
			    elsif counter3 < 2 then
				countsig3 <= '1';
				write_en_1 <='1';
				nextState <= keychangeEEE3_de;
			    else
				countsig3 <= '1';
				nextState <= ready;
			    end if;

	    when keychangeEEE3_de => Mux_sel_7 <= '1';
			             Mux_sel_5 <= '1';
				     latch_en32RL <= '1';
				     -- control_1 <= "01";
				     nextState <= eee3_de;

            when busyEDE3_en => Mux_sel_4 <= '1';
				control_1 <= "01";     
				latch_en32RL <= '1';
				data_ack1 <= '1';
				nextState <= ede3_en;

	    when busyEDE3_de =>	Mux_sel_4 <= '1';
				en_decrypt_1 <= '1'; 
				latch_en32RL <= '1';
				write_en_1 <= '1';  
				data_ack1 <= '1';
				nextState <= ede3_de;

            when ede3_en => countsig <= '1';
			    latch_en32RL <= '1';
			    if counter3 = 0 then
				keystSel <= "01";
			    elsif counter3 = 1 then
				keystSel <= "10";
			    end if;
			    case counter3 is
				when 0 => en_decrypt_1 <='0';
				when 1 => en_decrypt_1 <='1';
				when 2 => en_decrypt_1 <='0';
				when others => en_decrypt_1 <= '0';
			    end case;
			    case counter is
				when 0 | 7 | 14 => control_1 <= "01";
				when 1 to 6 | 8 to 13 => control_1 <= "11";
				when 15 => control_1 <= "00";
				when others => null;
			    end case;
			    Mux_sel_5 <= '1';
			    Mux_sel_6 <= '1';
			    if (counter < 15) then
				nextState <= ede3_en;
			    elsif counter3 < 2 then
				countsig3 <= '1';
				write_en_1 <= '1';
				nextState <= keychangeEDE3_en;
			    else
				countsig3 <= '1';
				nextState <= ready;
			    end if;

            when keychangeEDE3_en => Mux_sel_7 <= '1';
			             Mux_sel_5 <= '1';
				     latch_en32RL <= '1';
				     if counter3 = 1 then
					control_1 <= "00";
				     else
					 control_1 <= "01";
				     end if;
				     nextState <= ede3_en;
				     
             when ede3_de => countsig <= '1';
			     latch_en32RL <= '1';
			     en_decrypt_1 <= '1';
			     if counter3 = 0 then
				 keystSel <= "01";
			     elsif counter3 = 1 then
				 keystSel <= "10";
			     end if;
			     case counter3 is
				 when 0 => en_decrypt_1 <='1';
				 when 1 => en_decrypt_1 <='0';
				 when 2 => en_decrypt_1 <='1';
				 when others => en_decrypt_1 <= '1';
			     end case;
			     case counter is
				 when 0 | 7 | 14 => control_1 <= "01";
				 when 1 to 6 | 8 to 13 => control_1 <= "11";
				 when 15 => control_1 <= "00";
				 when others => null;
			     end case;
			     Mux_sel_5 <= '1';
			     Mux_sel_6 <= '1';
			     if (counter < 15) then
				 nextState <= ede3_de;
			     elsif counter3 < 2 then
				 countsig3 <= '1';
				 write_en_1 <= '1';
				 nextState <= keychangeEDE3_de;
			     else
				 countsig3 <= '1';
				 nextState <= ready;
			     end if;

            when keychangeEDE3_de => Mux_sel_7 <= '1';
			             Mux_sel_5 <= '1';
				     latch_en32RL <= '1';
				     if counter3 = 1 then
					control_1 <= "01"; 
				     else
					 control_1 <= "00";
				     end if;
				     nextState <= ede3_de;

            when IVload_en => latch_enIV <= '1';
                              Mux_sel_2 <= '1';
                              Mux_sel_1 <= '1';
                              write_en_1 <= '1';
                              data_ack1 <= '1';
                              nextState <= firstdate;

	    when firstdate => latch_en32RL <= '1';
                              Mux_sel_1 <= '1';
			      Mux_sel_2 <= '1';
                              if (data_is_key = '0' and data_ready = '1') then
                                  control_1 <= "01";
				  data_ack1 <= '1';
			          nextState <= cbc_en;
			      elsif data_is_key = '1' then
			          nextState <= awaitdata;
			      else
			          nextState <= firstdate;
			      end if;

            when getdate_en => latch_en32RL <= '1';
                               Mux_sel_1 <= '1';	   
                               if (data_is_key = '0' and data_ready = '1') then
				   control_1 <= "01";
				   data_ack1 <= '1';
				   nextState <= cbc_en;
			       elsif data_is_key = '1' then
				   nextState <= awaitdata;
			       else
				   nextState <= getdate_en;
			       end if;
                              
           when cbc_en => countsig <= '1';
			  latch_en32RL <= '1';
                          control_1 <= "01";
			  Mux_sel_5 <= '1';
		          Mux_sel_6 <= '1';
			  case counter is
			      when 0 | 7 | 14 => control_1 <= "01";
			      when 1 to 6 | 8 to 13 => control_1 <= "11";
			      when 15 => control_1 <= "00";
			      when others => null;
			  end case;
		          if counter < 15 then
			      nextState <= cbc_en;
		          else
			      nextState <= readyCBC_en;
		          end if;

	    -- when readyCBCen_pre => nextstate <= readyCBC_en;  
			     
            when readyCBC_en => if Buffer_Free = '1' then
				    Des_ready1<= '1';
				    nextstate <= readyQuit_cbcen;
				else
				    nextstate <= readyCBC_en;
				end if;

            when readyQuit_cbcen =>  des_ready1 <= '1';
				     if Buffer_free = '0' then
					des_ready1 <= '0';
					if (data_ready = '1' and data_is_key = '0') then
					    Mux_sel_1 <= '1';
					    nextState <= getdate_en;
					else			    
					    nextState <= await_cbcdata;
					end if; 
	                             else
					 nextState <= readyQuit_cbcen;   
				     end if;
				     
	    when await_cbcdata =>des_ready1 <= '0';
				 nextState <=await_cbcdata;
				 if (data_ready = '1' and data_is_key = '0') then
				     Mux_sel_1 <= '1';
				     nextState <= getdate_en;
				 elsif data_ready = '1' and data_is_key = '1' then			    
				     nextState <= awaitdata;
				 end if;
				 
            when IVload_de => latch_enIV <= '1';
                              Mux_sel_2 <= '1';
                              latch_en164 <= '1';
                              data_ack1 <= '1';
                              nextState <= Regload;

            when Regload => latch_enIV <= '1';
			    latch_en164 <= '1';
			    Mux_sel_3 <= '1';
			    Mux_sel_2 <= '1';
			    write_en_1 <= '1';
			    nextState <= getdate_de;

            when getdate_de => latch_en32RL <= '1';
			       latch_enIV <= '1';
			       latch_en164 <= '1';
			       Mux_sel_3 <= '1';
			       Mux_sel_2 <= '1';  
                               if (data_is_key = '0' and data_ready = '1') then
				   data_ack1 <= '1';
				   en_decrypt_1 <= '1';
				   nextState <= cbc_de;
			       elsif data_is_key = '1' then
				   nextState <= awaitdata;
			       else
				   nextState <= getdate_de;
			       end if;
                              
           when cbc_de => countsig <= '1';
			  en_decrypt_1 <= '1'; 
			  latch_en32RL <= '1';
			  Mux_sel_3 <= '1';
			  Mux_sel_2 <= '1';
			  Mux_sel_5 <= '1';
		          Mux_sel_6 <= '1';
			  case counter is
			      when 0 | 7 | 14 => control_1 <= "01";
			      when 1 to 6 | 8 to 13 => control_1 <= "11";
			      when 15 => control_1 <= "00";
					 when others => null;
			  end case;
		          if counter < 15 then
			      nextState <= cbc_de;
		          else
			      nextState <= readyCBC_de;
		          end if;                   

	    -- when readyCBCde_pre => nextstate <= readyCBC_de;	     

	    when readyCBC_de => Mux_sel_3 <= '1';
				Mux_sel_2 <= '1';
				en_decrypt_1 <= '1';
				if Buffer_Free = '1' then
				    Des_ready1<= '1';
				    nextstate <= readyQuit_cbcde;
				else
				    nextstate <= readyCBC_de;
				end if;

            when readyQuit_cbcde => Mux_sel_3 <= '1';
				    en_decrypt_1 <= '1';
				    if Buffer_free = '0' then
					des_ready1 <= '0';
					if (data_ready = '1' and data_is_key = '0') then
                                            latch_enIV <= '1';
					    latch_en164 <= '1';
					    Mux_sel_3 <= '1';
					    write_en_1 <= '1';
					    nextState <= getdate_de;
					else
					    nextState <= await_cbcdata_de;
					end if; 
	                             else
					 nextState <= readyQuit_cbcde;   
				     end if;

	    when await_cbcdata_de => Mux_sel_3 <= '1';
				     des_ready1 <= '0';
				     en_decrypt_1 <= '1';
				     control_1 <= "01";
				     nextState <=await_cbcdata_de;
				     if (data_ready = '1' and data_is_key = '0') then
					 nextState <= getdate_de;
				     elsif data_ready = '1' and data_is_key = '1' then			    
					 nextState <= awaitdata;
				     end if;			

            when IVloadcbceee2en => latch_enIV <= '1';
                                    Mux_sel_2 <= '1';
                                    Mux_sel_1 <= '1';
                                    write_en_1 <= '1';
                                    data_ack1 <= '1';
                                    nextState <= fd_cbceee2;

	    when fd_cbceee2 => latch_en32RL <= '1';
                               Mux_sel_1 <= '1';
			       Mux_sel_2 <= '1';
                               if (data_is_key = '0' and data_ready = '1') then
                                  control_1 <= "01";
				  data_ack1 <= '1';
			          nextState <= cbceee2_en;
			       elsif data_is_key = '1' then
			          nextState <= awaitdata;
			       else
			          nextState <= fd_cbceee2;
			       end if;

	    when cbceee2_en => countsig <= '1';
			       latch_en32RL <= '1';
			       -- control_1 <= "01";
			       if counter3 = 0 then
				   keystSel <= "01";
			       elsif counter3 = 1 then
				   keystSel <= "00";
			       end if;
			       case counter is
				   when 0 | 7 | 14 => control_1 <= "01";
				   when 1 to 6 | 8 to 13  => control_1 <= "11";
				   when 15 => control_1 <= "00";
					      when others => null;
			       end case;
			       Mux_sel_5 <= '1';
			       Mux_sel_6 <= '1';
			       if (counter < 15) then
				   nextState <= cbceee2_en;
			       elsif counter3 < 2 then
				   countsig3 <= '1';
				   write_en_1 <= '1';
				   nextState <= keycbceee2_en;
			       else
				   countsig3 <= '1';
				   nextState <= readycbceee2_en;
			       end if;

	    when keycbceee2_en => Mux_sel_7 <= '1';
				  Mux_sel_5 <= '1';
				  latch_en32RL <= '1';
				  control_1 <= "01";
				  nextState <= cbceee2_en;
				
            when getdatecbceee2_en => latch_en32RL <= '1';
                                      Mux_sel_1 <= '1';	   
                                      if (data_is_key = '0' and data_ready = '1') then
					  control_1 <= "01";
					  data_ack1 <= '1';
					  nextState <= cbceee2_en;
				      elsif data_is_key = '1' then
					  nextState <= awaitdata;
				      else
					  nextState <= getdatecbceee2_en;
				      end if;

	    when readyCBCEEE2_en => if Buffer_Free = '1' then
					Des_ready1<= '1';
					nextstate <= readyQuitcbceee2_en;
				    else
					nextstate <= readyCBCEEE2_en;
				    end if;

            when readyQuitcbceee2_en =>  des_ready1 <= '1';
				         if Buffer_free = '0' then
					     des_ready1 <= '0';
					     if (data_ready = '1' and data_is_key = '0') then
						 Mux_sel_1 <= '1';
						 write_en_1 <= '1';
						 nextState <= getdatecbceee2_en;
					     else			    
						 nextState <= awaitcbceee2data;
					     end if; 
					 else
					     nextState <= readyQuitcbceee2_en;   
					 end if;
					 				     
	    when awaitcbceee2data => des_ready1 <= '0';
				     nextState <=awaitcbceee2data;
				 if (data_ready = '1' and data_is_key = '0') then
				     Mux_sel_1 <= '1';
				     write_en_1 <= '1';
				     nextState <= getdatecbceee2_en;
				 elsif data_ready = '1' and data_is_key = '1' then			    
				     nextState <= awaitdata;
				 end if;


            when IVloadcbceee2_de => latch_enIV <= '1';
                                     Mux_sel_2 <= '1';
                                     latch_en164 <= '1';
                                     data_ack1 <= '1';
                                     nextState <= RegloadCBCeee2;

            when RegloadCBCeee2 => latch_enIV <= '1';
			           latch_en164 <= '1';
			           Mux_sel_3 <= '1';
			           Mux_sel_2 <= '1';
			           write_en_1 <= '1';
			           nextState <= getdatecbceee2_de;

            when getdatecbceee2_de => latch_en32RL <= '1';
			              latch_enIV <= '1';
			              latch_en164 <= '1';
			              Mux_sel_3 <= '1';
			              Mux_sel_2 <= '1';  
                                      if (data_is_key = '0' and data_ready = '1') then
					  data_ack1 <= '1';
					  en_decrypt_1 <= '1';
					  nextState <= cbceee2_de;
				      elsif data_is_key = '1' then
					  nextState <= awaitdata;
				      else
					  nextState <= getdatecbceee2_de;
				      end if;
				      
            when cbceee2_de => countsig <= '1';
			       en_decrypt_1 <= '1'; 	
			       latch_en32RL <= '1';
                               Mux_sel_3 <= '1';
			       Mux_sel_2 <= '1';    
			       if counter3 = 0 then
				   keystSel <= "01";
			       elsif counter3 = 1 then
				   keystSel <= "00";
			       end if;
			       case counter is
				   when 0 | 7 | 14  => control_1 <= "01";
				   when 1 to 6 | 8 to 13 => control_1 <= "11";
				   when 15 => control_1 <= "00";
					      when others => null;
			       end case;
			       Mux_sel_5 <= '1';
			       Mux_sel_6 <= '1';
			       if (counter < 15) then
				   nextState <= cbceee2_de;
			       elsif counter3 < 2 then
				   countsig3 <= '1';
				   write_en_1 <= '1';
				   nextState <= keycbcEEE2_de;
			       else
				   countsig3 <= '1';
				   nextState <= readycbceee2_de;
			       end if;

 	     when keycbcEEE2_de => Mux_sel_7 <= '1';
				   Mux_sel_5 <= '1';
				   Mux_sel_3 <= '1';
				   latch_en32RL <= '1';
				   en_decrypt_1 <= '1';
				   nextState <= cbceee2_de;

            when readyCBCEEE2_de => Mux_sel_3 <= '1';
				    Mux_sel_2 <= '1';
				    en_decrypt_1 <= '1';
		                    if Buffer_Free = '1' then
					Des_ready1<= '1';
					nextstate <= readyQuitcbceee2_de;
				    else
					nextstate <= readyCBCEEE2_de;
				    end if;

            when readyQuitcbceee2_de => des_ready1 <= '1';
					Mux_sel_3 <= '1';
					en_decrypt_1 <= '1';
				        if Buffer_free = '0' then
					    des_ready1 <= '0';
					    if (data_ready = '1' and data_is_key = '0') then
						Mux_sel_1 <= '1';
						write_en_1 <= '1';
						nextState <= getdatecbceee2_de;
					    else			    
						nextState <= awaitcbceee2data_de;
					    end if; 
					else
					    nextState <= readyQuitcbceee2_de;   
					end if;
				     
	    when awaitcbceee2data_de => des_ready1 <= '0';
					Mux_sel_3 <= '1';
				        nextState <=awaitcbceee2data_de;
				        if (data_ready = '1' and data_is_key = '0') then
					    Mux_sel_1 <= '1';
					    write_en_1 <= '1';
					    nextState <= getdatecbceee2_de;
				        elsif data_ready = '1' and data_is_key = '1' then			    
					    nextState <= awaitdata;
					end if;

	    when IVloadcbceee3en => latch_enIV <= '1';
                                    Mux_sel_2 <= '1';
                                    Mux_sel_1 <= '1';
                                    write_en_1 <= '1';
                                    data_ack1 <= '1';
                                    nextState <= fd_cbceee3;

	    when fd_cbceee3 => latch_en32RL <= '1';
                               Mux_sel_1 <= '1';
			       Mux_sel_2 <= '1';
                               if (data_is_key = '0' and data_ready = '1') then
                                  control_1 <= "01";
				  data_ack1 <= '1';
			          nextState <= cbceee3_en;
			       elsif data_is_key = '1' then
			          nextState <= awaitdata;
			       else
			          nextState <= fd_cbceee3;
			       end if;

	    when cbceee3_en => countsig <= '1';
			       latch_en32RL <= '1';
			       -- control_1 <= "01";
			       if counter3 = 0 then
				   keystSel <= "01";
			       elsif counter3 = 1 then
				   keystSel <= "10";
			       end if;
			       case counter is
				   when 0 | 7 | 14 => control_1 <= "01";
				   when 1 to 6 | 8 to 13  => control_1 <= "11";
				   when 15 => control_1 <= "00";
					      when others => null;
			       end case;
			       Mux_sel_5 <= '1';
			       Mux_sel_6 <= '1';
			       if (counter < 15) then
				   nextState <= cbceee3_en;
			       elsif counter3 < 2 then
				   countsig3 <= '1';
				   write_en_1 <= '1';
				   nextState <= keycbceee3_en;
			       else
				   countsig3 <= '1';
				   nextState <= readycbceee3_en;
			       end if;

	    when keycbceee3_en => Mux_sel_7 <= '1';
				  Mux_sel_5 <= '1';
				  latch_en32RL <= '1';
				  control_1 <= "01";
				  nextState <= cbceee3_en;
				
            when getdatecbceee3_en => latch_en32RL <= '1';
                                      Mux_sel_1 <= '1';	   
                                      if (data_is_key = '0' and data_ready = '1') then
					  control_1 <= "01";
					  data_ack1 <= '1';
					  nextState <= cbceee3_en;
				      elsif data_is_key = '1' then
					  nextState <= awaitdata;
				      else
					  nextState <= getdatecbceee3_en;
				      end if;

	    when readyCBCEEE3_en => if Buffer_Free = '1' then
					Des_ready1<= '1';
					nextstate <= readyQuitcbceee3_en;
				    else
					nextstate <= readyCBCEEE3_en;
				    end if;

            when readyQuitcbceee3_en =>  des_ready1 <= '1';
				         if Buffer_free = '0' then
					     des_ready1 <= '0';
					     if (data_ready = '1' and data_is_key = '0') then
						 Mux_sel_1 <= '1';
						 write_en_1 <= '1';
						 nextState <= getdatecbceee3_en;
					     else			    
						 nextState <= awaitcbceee3data;
					     end if; 
					 else
					     nextState <= readyQuitcbceee3_en;   
					 end if;
					 				     
	    when awaitcbceee3data => des_ready1 <= '0';
				     nextState <=awaitcbceee3data;
				 if (data_ready = '1' and data_is_key = '0') then
				     Mux_sel_1 <= '1';
				     write_en_1 <= '1';
				     nextState <= getdatecbceee3_en;
				 elsif data_ready = '1' and data_is_key = '1' then			    
				     nextState <= awaitdata;
				 end if;


            when IVloadcbceee3_de => latch_enIV <= '1';
                                     Mux_sel_2 <= '1';
                                     latch_en164 <= '1';
                                     data_ack1 <= '1';
                                     nextState <= RegloadCBCeee3;

            when RegloadCBCeee3 => latch_enIV <= '1';
			           latch_en164 <= '1';
			           Mux_sel_3 <= '1';
			           Mux_sel_2 <= '1';
			           write_en_1 <= '1';
			           nextState <= getdatecbceee3_de;

            when getdatecbceee3_de => latch_en32RL <= '1';
			              latch_enIV <= '1';
			              latch_en164 <= '1';
			              Mux_sel_3 <= '1';
			              Mux_sel_2 <= '1';  
                                      if (data_is_key = '0' and data_ready = '1') then
					  data_ack1 <= '1';
					  en_decrypt_1 <= '1';
					  nextState <= cbceee3_de;
				      elsif data_is_key = '1' then
					  nextState <= awaitdata;
				      else
					  nextState <= getdatecbceee3_de;
				      end if;
				      
            when cbceee3_de => countsig <= '1';
			       en_decrypt_1 <= '1'; 	
			       latch_en32RL <= '1';
                               Mux_sel_3 <= '1';
			       Mux_sel_2 <= '1';    
			       if counter3 = 0 then
				   keystSel <= "01";
			       elsif counter3 = 1 then
				   keystSel <= "10";
			       end if;
			       case counter is
				   when 0 | 7 | 14  => control_1 <= "01";
				   when 1 to 6 | 8 to 13 => control_1 <= "11";
				   when 15 => control_1 <= "00";
					      when others => null;
			       end case;
			       Mux_sel_5 <= '1';
			       Mux_sel_6 <= '1';
			       if (counter < 15) then
				   nextState <= cbceee3_de;
			       elsif counter3 < 2 then
				   countsig3 <= '1';
				   write_en_1 <= '1';
				   nextState <= keycbcEEE3_de;
			       else
				   countsig3 <= '1';
				   nextState <= readycbceee3_de;
			       end if;

 	     when keycbcEEE3_de => Mux_sel_7 <= '1';
				   Mux_sel_5 <= '1';
				   Mux_sel_3 <= '1';
				   latch_en32RL <= '1';
				   en_decrypt_1 <= '1';
				   nextState <= cbceee3_de;

            when readyCBCEEE3_de => Mux_sel_3 <= '1';
				    Mux_sel_2 <= '1';
				    en_decrypt_1 <= '1';
		                    if Buffer_Free = '1' then
					Des_ready1<= '1';
					nextstate <= readyQuitcbceee3_de;
				    else
					nextstate <= readyCBCEEE3_de;
				    end if;

            when readyQuitcbceee3_de => des_ready1 <= '1';
					Mux_sel_3 <= '1';
					en_decrypt_1 <= '1';
				        if Buffer_free = '0' then
					    des_ready1 <= '0';
					    if (data_ready = '1' and data_is_key = '0') then
						Mux_sel_1 <= '1';
						write_en_1 <= '1';
						nextState <= getdatecbceee3_de;
					    else			    
						nextState <= awaitcbceee3data_de;
					    end if; 
					else
					    nextState <= readyQuitcbceee3_de;   
					end if;
				     
	    when awaitcbceee3data_de => des_ready1 <= '0';
					Mux_sel_3 <= '1';
				        nextState <=awaitcbceee3data_de;
				        if (data_ready = '1' and data_is_key = '0') then
					    Mux_sel_1 <= '1';
					    write_en_1 <= '1';
					    nextState <= getdatecbceee3_de;
				        elsif data_ready = '1' and data_is_key = '1' then			    
					    nextState <= awaitdata;
					end if;

	     when IVloadcbcede3en => latch_enIV <= '1';
                                    Mux_sel_2 <= '1';
                                    Mux_sel_1 <= '1';
                                    write_en_1 <= '1';
                                    data_ack1 <= '1';
                                    nextState <= fd_cbcede3;

	    when fd_cbcede3 => latch_en32RL <= '1';
                               Mux_sel_1 <= '1';
			       Mux_sel_2 <= '1';
                               if (data_is_key = '0' and data_ready = '1') then
                                  control_1 <= "01";
				  data_ack1 <= '1';
			          nextState <= cbcede3_en;
			       elsif data_is_key = '1' then
			          nextState <= awaitdata;
			       else
			          nextState <= fd_cbcede3;
			       end if;

	    when cbcede3_en => countsig <= '1';
			       latch_en32RL <= '1';
			       -- control_1 <= "01";
			       if counter3 = 0 then
				   keystSel <= "01";
			       elsif counter3 = 1 then
				   keystSel <= "10";
			       end if;
			       case counter3 is
				   when 0 => en_decrypt_1 <='0';
				   when 1 => en_decrypt_1 <='1';
				   when 2 => en_decrypt_1 <='0';
				   when others => en_decrypt_1 <= '0';
			       end case;
			       case counter is
				   when 0 | 7 | 14 => control_1 <= "01";
				   when 1 to 6 | 8 to 13  => control_1 <= "11";
				   when 15 => control_1 <= "00";
				   when others => null;					      
			       end case;
			       Mux_sel_5 <= '1';
			       Mux_sel_6 <= '1';
			       if (counter < 15) then
				   nextState <= cbcede3_en;
			       elsif counter3 < 2 then
				   countsig3 <= '1';
				   write_en_1 <= '1';
				   nextState <= keycbcede3_en;
			       else
				   countsig3 <= '1';
				   nextState <= readycbcede3_en;
			       end if;

	    when keycbcede3_en => Mux_sel_7 <= '1';
				  Mux_sel_5 <= '1';
				  latch_en32RL <= '1';
				  if counter3 = 1 then
					control_1 <= "00";
				  else
				      control_1 <= "01";
				  end if;
				  nextState <= cbcede3_en;
				
            when getdatecbcede3_en => latch_en32RL <= '1';
                                      Mux_sel_1 <= '1';	   
                                      if (data_is_key = '0' and data_ready = '1') then
					  control_1 <= "01";
					  data_ack1 <= '1';
					  nextState <= cbcede3_en;
				      elsif data_is_key = '1' then
					  nextState <= awaitdata;
				      else
					  nextState <= getdatecbcede3_en;
				      end if;

	    when readyCBCEdE3_en => if Buffer_Free = '1' then
					Des_ready1<= '1';
					nextstate <= readyQuitcbcede3_en;
				    else
					nextstate <= readyCBCEde3_en;
				    end if;

            when readyQuitcbcede3_en =>  des_ready1 <= '1';
				         if Buffer_free = '0' then
					     des_ready1 <= '0';
					     if (data_ready = '1' and data_is_key = '0') then
						 Mux_sel_1 <= '1';
						 write_en_1 <= '1';
						 nextState <= getdatecbcede3_en;
					     else			    
						 nextState <= awaitcbcede3data;
					     end if; 
					 else
					     nextState <= readyQuitcbcede3_en;   
					 end if;
					 				     
	    when awaitcbcede3data => des_ready1 <= '0';
				     nextState <=awaitcbcede3data;
				 if (data_ready = '1' and data_is_key = '0') then
				     Mux_sel_1 <= '1';
				     write_en_1 <= '1';
				     nextState <= getdatecbcede3_en;
				 elsif data_ready = '1' and data_is_key = '1' then			    
				     nextState <= awaitdata;
				 end if;


            when IVloadcbcede3_de => latch_enIV <= '1';
                                     Mux_sel_2 <= '1';
                                     latch_en164 <= '1';
                                     data_ack1 <= '1';
                                     nextState <= RegloadCBCede3;

            when RegloadCBCede3 => latch_enIV <= '1';
			           latch_en164 <= '1';
			           Mux_sel_3 <= '1';
			           Mux_sel_2 <= '1';
			           write_en_1 <= '1';
			           nextState <= getdatecbcede3_de;

            when getdatecbcede3_de => latch_en32RL <= '1';
			              latch_enIV <= '1';
			              latch_en164 <= '1';
			              Mux_sel_3 <= '1';
			              Mux_sel_2 <= '1';  
                                      if (data_is_key = '0' and data_ready = '1') then
					  data_ack1 <= '1';
					  en_decrypt_1 <= '1';
					  nextState <= cbcede3_de;
				      elsif data_is_key = '1' then
					  nextState <= awaitdata;
				      else
					  nextState <= getdatecbcede3_de;
				      end if;
				      
            when cbcede3_de => countsig <= '1';
			       en_decrypt_1 <= '1'; 	
			       latch_en32RL <= '1';
                               Mux_sel_3 <= '1';
			       Mux_sel_2 <= '1';    
			       if counter3 = 0 then
				   keystSel <= "01";
			       elsif counter3 = 1 then
				   keystSel <= "10";
			       end if;
			       case counter3 is
				   when 0 => en_decrypt_1 <='1';
				   when 1 => en_decrypt_1 <='0';
				   when 2 => en_decrypt_1 <='1';
				   when others => en_decrypt_1 <= '1';
			       end case;
			       case counter is
				   when 0 | 7 | 14  => control_1 <= "01";
				   when 1 to 6 | 8 to 13 => control_1 <= "11";
				   when 15 => control_1 <= "00";
					      when others => null;
			       end case;
			       Mux_sel_5 <= '1';
			       Mux_sel_6 <= '1';
			       if (counter < 15) then
				   nextState <= cbcede3_de;
			       elsif counter3 < 2 then
				   countsig3 <= '1';
				   write_en_1 <= '1';
				   case counter3 is
				       when 0 => en_decrypt_1 <='0';
				       when 1 => en_decrypt_1 <='1';
				       when 2 => en_decrypt_1 <='0';
				       when others => en_decrypt_1 <= '0';
				   end case;
				   nextState <= keycbcEde3_de;
			       else
				   countsig3 <= '1';
				   nextState <= readycbcede3_de;
			       end if;

 	     when keycbcEdE3_de => Mux_sel_7 <= '1';
				   Mux_sel_5 <= '1';
				   Mux_sel_3 <= '1';
				   latch_en32RL <= '1';
				   if counter3 = 1 then
					control_1 <= "01"; 
				   else
				       en_decrypt_1 <= '1';
				       control_1 <= "00";
				   end if;
				   nextState <= cbcede3_de;

            when readyCBCEdE3_de => Mux_sel_3 <= '1';
				    Mux_sel_2 <= '1';
				    en_decrypt_1 <= '1';
		                    if Buffer_Free = '1' then
					Des_ready1<= '1';
					nextstate <= readyQuitcbcede3_de;
				    else
					nextstate <= readyCBCEdE3_de;
				    end if;

            when readyQuitcbcede3_de => des_ready1 <= '1';
					Mux_sel_3 <= '1';
					en_decrypt_1 <= '1';
				        if Buffer_free = '0' then
					    des_ready1 <= '0';
					    if (data_ready = '1' and data_is_key = '0') then
						Mux_sel_1 <= '1';
						write_en_1 <= '1';
						nextState <= getdatecbcede3_de;
					    else			    
						nextState <= awaitcbcede3data_de;
					    end if; 
					else
					    nextState <= readyQuitcbcede3_de;   
					end if;
				     
	    when awaitcbcede3data_de => des_ready1 <= '0';
					Mux_sel_3 <= '1';
				        nextState <=awaitcbcede3data_de;
				        if (data_ready = '1' and data_is_key = '0') then
					    Mux_sel_1 <= '1';
					    write_en_1 <= '1';
					    nextState <= getdatecbcede3_de;
				        elsif data_ready = '1' and data_is_key = '1' then			    
					    nextState <= awaitdata;
					end if;  				
 
	    when HashDKload => latch_en32RL <= '1';
		 	       latch_enIV <= '1';
			       keystEen <= '1';
			       Mux_sel_2 <= '1';
			       latch_en164 <= '1';
			       write_en_1 <= '1';
			       data_ack1 <= '1';
			       control_1 <= "01";
			       Mux_sel_3 <= '1';
			       nextState <= Regload_Hash;

            when Regload_Hash => -- latch_en164 <= '1';
				 Mux_sel_3 <= '1';
				 Mux_sel_2 <= '1';
				 -- write_en_1 <= '1';
				 control_1 <= "01";
				 nextState <= hash;

            when getdate_hash => latch_en32RL <= '1';
				 Mux_sel_2 <= '1';
			         latch_en164 <= '1';
				 Mux_sel_3 <= '1';
                                 write_en_1 <= '1';
				 if (data_is_key = '0' and data_ready = '1') then
				     control_1 <= "01";
				     data_ack1 <= '1';
				     nextState <= Regload_Hash;
				 elsif (data_is_key = '1' and data_ready = '1') then
				     nextState <= awaitdata;
				 else
				     nextState <= getdate_hash;
				 end if;
                              
           when hash => countsig <= '1';
			latch_en32RL <= '1';
			Mux_sel_2 <= '1';
			Mux_sel_3 <= '1';
			Mux_sel_5 <= '1';
			Mux_sel_6 <= '1';
			case counter is
			    when 0 | 7 | 14 => control_1 <= "01";
			    when 1 to 6 | 8 to 13 => control_1 <= "11";
			    when 15 => control_1 <= "00";
				       when others => null;
			end case;
			if counter < 15 then
			    nextState <= hash;
			else
			    nextState <= readyHASH;
			end if;

	   --  when readyHASH_pre => Mux_sel_3 <= '1';
	--		          Mux_sel_2 <= '1';
	--		          nextstate <= readyHash;
			       
			     
            when readyHash => Mux_sel_3 <= '1';
			      Mux_sel_2 <= '1';
			      if Buffer_Free = '1' then
				  Des_ready1<= '1';
				  Mux_sel_3 <= '1';
				  nextstate <= readyQuit_hash;
				else
				    nextstate <= readyHash;
				end if;

            when readyQuit_hash => Mux_sel_3 <='1';
				   if Buffer_free = '0' then
					des_ready1 <= '0';
					if (data_ready = '1' and data_is_key = '0') then
                                            latch_enIV <= '1';
					    Mux_sel_4 <= '1';
					    Mux_sel_3 <= '1';
					    Mux_sel_2 <= '1';
					    keystEen <= '1';
					    nextState <= datetokey;
					else
					    nextState <= awaitdata;
					end if;
	                             else
					 nextState <= readyQuit_hash;   
				     end if;

	    when datetokey => keystEen <= '1';
			      Mux_sel_2 <= '1';	
			      Mux_sel_4 <= '1';
			      Mux_sel_3 <= '1';
			      latch_enIV <= '1';
			      --write_en_1 <= '1';
			      nextState <= getdate_hash;
           
          --  when ready_pre => nextState <= ready;

	    when ready => if Buffer_Free = '1' then
         		      Des_ready1<= '1';
			      nextstate <= ready_quit;
			    else
				nextstate <= ready;
			    end if;
			    

	    when ready_quit => if Buffer_free = '0' then
				   des_ready1 <= '0';
				   nextState <= awaitdata;
			       else
				   nextState <= ready_quit;   
			       end if;
            
	    when others => nextState <= S_RESET;
      end case;
   end process next_state_logic;
end RTL;













