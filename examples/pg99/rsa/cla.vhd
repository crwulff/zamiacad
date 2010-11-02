--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : Carry Lookahead Addierer
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : cla.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 10.11.98        | 10.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Ein 16 Bit CLA Addierer
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;



entity CLA_16 is
  port(	A     : In 	std_logic_vector (15 downto 0); 
	B     : In	std_logic_vector (15 downto 0);
	Q     : Out	std_logic_vector (15 downto 0);
	C_OUT : Out     STD_LOGIC;
	C_IN  : in	STD_LOGIC);
end CLA_16;



architecture BEHAV of CLA_16 is

  begin

    operate_CLA: process(A,B, C_IN)
	variable carry_in : std_logic_vector(15 downto 0) := (others => '0');
    begin
      if C_IN = '1' then
	  carry_in := (0 => '1', others => '0');
      else
	  carry_in := (others => '0');
      end if;
      Q <= A + B + carry_in;
      C_OUT <= '0';
      if ( conv_integer(A) + conv_integer(B) + conv_integer(carry_in) ) > 65535 then
        C_OUT <= '1';
      end if;
    end process operate_CLA;

  end BEHAV;



-- purpose: Synthetisierbare Beschreibung des 16 Bit CLA
architecture RTL of CLA_16 is

    constant	BR	: integer := 16;  -- Die Bitbreite des CLA
    signal	carry   : unsigned (BR-1 DOWNTO 0);
    
begin  -- RTL
    
   cla: process(A,B,C_IN)
        -- Dieser Process stellt quasi eine Carry-Look-Ahead Einheit dar und
        -- berechnet aus den eingegebenen Binaerzahlen A und B und dem ein-
        -- gegebenen Uebertrag (C_IN) parallel die Uebertraege (Variable C)
        -- fuer die einzelnen Stufen und weist diese dem Signal carry bzw.
        -- carry_out zu.
 
           variable p   : unsigned ((BR-1) DOWNTO 0);
                          -- "propagate"
           variable g   : unsigned ((BR-1) DOWNTO 0);
                          -- "generate"
           variable c   : unsigned ( BR    DOWNTO 0);
                          -- Uebertraege der einzelnen Stufen
        begin
           for i in 0 to (BR-1) loop
              g(i) := A(i) and B(i);
              p(i) := A(i) or B(i);
           end loop;

           c(0) := C_IN ;

           for i in 0 to (BR-1) loop
              c(i+1) := g(i) or (p(i) and c(i));
           end loop;

           for i in 0 to (BR-1) loop
               carry(i) <= c(i);
           end loop;
           
           C_OUT <= c(BR);

        end process;

   sum: process(A,B,carry)
        -- eigentliche Addition
        begin
           for i in 0 to (BR-1) loop
              Q(i) <= A(i) xor B(i) xor carry(i);
           end loop;
        end process;

end RTL;



