--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 16 Bit 48-zu-1 Multiplexer
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : mux768to16.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 17.11.98        | 17.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Es wird ein 16 Bit 48  zu 1 Multiplexer implementiert
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
--  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;



entity MUX768to16 is
  port(	A     : In 	std_logic_vector (767 downto 0); 
        Sel   : In	std_logic_vector (5 downto 0);
	Q     : Out     std_logic_vector (15 downto 0));
end MUX768to16;



architecture BEHAV of MUX768to16 is

    signal temp0 : std_logic_vector(47 downto 0);
    signal temp1 : std_logic_vector(47 downto 0);
    signal temp2 : std_logic_vector(47 downto 0);
    signal temp3 : std_logic_vector(47 downto 0);
    signal temp4 : std_logic_vector(47 downto 0);
    signal temp5 : std_logic_vector(47 downto 0);
    signal temp6 : std_logic_vector(47 downto 0);
    signal temp7 : std_logic_vector(47 downto 0);
    signal temp8 : std_logic_vector(47 downto 0);
    signal temp9 : std_logic_vector(47 downto 0);
    signal temp10 : std_logic_vector(47 downto 0);
    signal temp11 : std_logic_vector(47 downto 0);
    signal temp12 : std_logic_vector(47 downto 0);
    signal temp13 : std_logic_vector(47 downto 0);
    signal temp14 : std_logic_vector(47 downto 0);
    signal temp15 : std_logic_vector(47 downto 0);
    
  begin
      TempGenerate: for i in 0 to 47 generate
	  temp0(i) <= A(i*16 +0);
	  temp1(i) <= A(i*16 +1);
	  temp2(i) <= A(i*16 +2);
	  temp3(i) <= A(i*16 +3);
	  temp4(i) <= A(i*16 +4);
	  temp5(i) <= A(i*16 +5);
	  temp6(i) <= A(i*16 +6);
	  temp7(i) <= A(i*16 +7);
	  temp8(i) <= A(i*16 +8);
	  temp9(i) <= A(i*16 +9);
	  temp10(i) <= A(i*16 +10);
	  temp11(i) <= A(i*16 +11);
	  temp12(i) <= A(i*16 +12);
	  temp13(i) <= A(i*16 +13);
	  temp14(i) <= A(i*16 +14);
	  temp15(i) <= A(i*16 +15);
      end generate;

      Q(0) <= temp0(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(1) <= temp1(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(2) <= temp2(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(3) <= temp3(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(4) <= temp4(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(5) <= temp5(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(6) <= temp6(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(7) <= temp7(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(8) <= temp8(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(9) <= temp9(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(10) <= temp10(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(11) <= temp11(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(12) <= temp12(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(13) <= temp13(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(14) <= temp14(conv_integer(Sel)) when conv_integer(Sel)<48 else
	      '-';
      Q(15) <= temp15(conv_integer(Sel)) when conv_integer(Sel)<48 else
	       '-';

--Q <= A( ((16*(conv_integer(Sel)))+15) downto (16*(conv_integer(Sel))) ) when conv_integer(SEL)<48 else
--         conv_std_logic_vector(0,16);

  end BEHAV;



