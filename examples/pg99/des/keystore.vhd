--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designer  : Thomas Stanka              
-- Group     : DES
--------------------------------------------------------------------
-- Design Unit Name : KEYSTORE
-- Purpose : Part of the DES-module-core for the cryptochip "pg99"
-- 
-- File Name :  keystore.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date 16.11.98   | Changes 16.12.   change code to perfom better synthesis
--                 |         02.02.99 reducred the stored key from 64 bit to 56Bit 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  contents 3x56 FF to store 3 keys if necessary
--  
--------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;

entity KEYSTORE is
  port( KEY_IN  : in  STD_LOGIC_VECTOR(55 downto 0); 
	KEY_OUT : out STD_LOGIC_VECTOR(55 downto 0);
	SEL	: in  STD_LOGIC_VECTOR(1 downto 0); 	-- select 1 of 3 stores
	ENABLE  : in  STD_LOGIC;                        -- take KEY_IN in storeSELECT 
	RESET   : in  STD_LOGIC; 
	CLK	: in  STD_LOGIC				-- the clock-signal
      );				
end KEYSTORE;

architecture BEHAVIORAL of KEYSTORE is
    signal REG1, REG2, REG3 :STD_LOGIC_VECTOR(55 downto 0); -- the 3 register
begin
    process(CLK)		
       begin
	if RESET='1' then 				-- reset the registers
	   REG1 <= (others => '0');
	   REG2 <= (others => '0');
	   REG3 <= (others => '0');
	else 
	    if ENABLE='1' then
		if clk='1' and clk'event then
		    case SEL is
				when "00" =>REG1<=KEY_IN;
				when "01" =>REG2<=KEY_IN;
				when "10" =>REG3<=KEY_IN;
				when others => null;
		    end case;
		end if;
	    end if;
        end if;
      end process;	
	imux: with SEL select
		  KEY_OUT <= 
		  REG1 when "00",
		  REG2 when "01",
		  REG3 when "10",
		  REG1 when others;

end BEHAVIORAL;	
