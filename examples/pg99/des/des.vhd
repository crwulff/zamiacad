-------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Thomas Stanka
-- Group     : DES
--------------------------------------------------------------------
-- Design Unit Name : DES
-- Purpose : The DES-module-core for the cryptochip "pg99"
--          
-- File Name :  des.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date 20.11.98   | Changes 
--      23.12.     | fixed the fuction STD_DES now the DES should work 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  contents :  The DES-Algorithm as we provide it to the chip 
--              Test and error won`t do here, so whenever test is set error get high
--------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- this modul is as is. The Hash could be wrong implementated, but I can`t
-- check for it without testdates.
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- Parity is always set to '1', parity-failure cause an severity note and
-- reports the failure
-------------------------------------------------------------------------------


-------------------------------------------------------------------------------
-- the Handshake:
-- 1. <- DATA_ACK <= 0
-- 2. -> DATA_READY
-- 3. <- DATA_ACK <= 1 after geting DATA_IN
-- 4. repeat 1-3 for each key and the first data
-- 5. Work 
-- 6. -> BUFFER_FREE
-- 7. <- DES_READY <= 1 after put DATA_OUT
--------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_textio.all;
use std.textio.all;

library LIB_des;
-- use LIB_des.PCK_des.all;

architecture BEHAVIORAL of DES is


-------------------------------------------------------------------------
-- the DES as an Function 
-------------------------------------------------------------------------

function STD_DES(DATA_IN : in  STD_LOGIC_VECTOR(63 downto 0);
	 	 KEY_IN  : in  STD_LOGIC_VECTOR(63 downto 0);
		 ENCRYPT : in  STD_LOGIC)			-- High <=> verschluesseln
                 return STD_LOGIC_VECTOR is


--------------------------------------------------------------------------
-- first some subfunctions (every permutation)
--------------------------------------------------------------------------

--------------------------------------------------------------------------
-- IMPORTANT the funktions IP, IPminus1,...
-- are encode as (1 to 64) instead of (63 downto 0) 
--------------------------------------------------------------------------

function IP(INPUT : in STD_LOGIC_VECTOR(1 to 64))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(1 to 64);
	begin
 	O(1):=INPUT(58);
        O(2):=INPUT(50);
        O(3):=INPUT(42);
        O(4):=INPUT(34);
        O(5):=INPUT(26);
        O(6):=INPUT(18);
        O(7):=INPUT(10);
        O(8):=INPUT(2);
        O(9):=INPUT(60);
        O(10):=INPUT(52);
        O(11):=INPUT(44);
        O(12):=INPUT(36);
        O(13):=INPUT(28);
        O(14):=INPUT(20);
        O(15):=INPUT(12);
        O(16):=INPUT(4);
        O(17):=INPUT(62);
        O(18):=INPUT(54);
        O(19):=INPUT(46);
        O(20):=INPUT(38);
        O(21):=INPUT(30);
        O(22):=INPUT(22);
        O(23):=INPUT(14);
        O(24):=INPUT(6);
        O(25):=INPUT(64);
        O(26):=INPUT(56);
        O(27):=INPUT(48);
        O(28):=INPUT(40);
        O(29):=INPUT(32);
        O(30):=INPUT(24);
        O(31):=INPUT(16);
        O(32):=INPUT(8);
        O(33):=INPUT(57);
        O(34):=INPUT(49);
        O(35):=INPUT(41);
        O(36):=INPUT(33);
        O(37):=INPUT(25);
        O(38):=INPUT(17);
        O(39):=INPUT(9);
        O(40):=INPUT(1);
        O(41):=INPUT(59);
        O(42):=INPUT(51);
        O(43):=INPUT(43);
        O(44):=INPUT(35);
        O(45):=INPUT(27);
        O(46):=INPUT(19);
        O(47):=INPUT(11);
        O(48):=INPUT(3);
        O(49):=INPUT(61);
        O(50):=INPUT(53);
        O(51):=INPUT(45);
        O(52):=INPUT(37);
        O(53):=INPUT(29);
        O(54):=INPUT(21);
        O(55):=INPUT(13);
        O(56):=INPUT(5);
        O(57):=INPUT(63);
        O(58):=INPUT(55);
	O(59):=INPUT(47);
        O(60):=INPUT(39);
        O(61):=INPUT(31);
        O(62):=INPUT(23);
        O(63):=INPUT(15);
        O(64):=INPUT(7);
	return O;
	end IP;
--------------------------------------------------------------------------
-- IMPORTANT the funktions IP, IPminus1,...
-- are encode as (1 to 64) instead of (63 downto 0) 
--------------------------------------------------------------------------

function IPminus1(INPUT : in STD_LOGIC_VECTOR(1 to 64))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(1 to 64);
	begin
	O(1):=INPUT(40);
        O(2):=INPUT(8);
        O(3):=INPUT(48);
        O(4):=INPUT(16);
        O(5):=INPUT(56);
        O(6):=INPUT(24);
        O(7):=INPUT(64);
        O(8):=INPUT(32);
        O(9):=INPUT(39);
        O(10):=INPUT(7);
        O(11):=INPUT(47);
        O(12):=INPUT(15);
        O(13):=INPUT(55);
        O(14):=INPUT(23);
        O(15):=INPUT(63);
        O(16):=INPUT(31);
        O(17):=INPUT(38);
        O(18):=INPUT(6);
        O(19):=INPUT(46);
        O(20):=INPUT(14);
        O(21):=INPUT(54);
        O(22):=INPUT(22);
        O(23):=INPUT(62);
        O(24):=INPUT(30);
        O(25):=INPUT(37);
        O(26):=INPUT(5);
        O(27):=INPUT(45);
        O(28):=INPUT(13);
        O(29):=INPUT(53);
        O(30):=INPUT(21);
        O(31):=INPUT(61);
        O(32):=INPUT(29);
        O(33):=INPUT(36);
        O(34):=INPUT(4);
        O(35):=INPUT(44);
        O(36):=INPUT(12);
        O(37):=INPUT(52);
        O(38):=INPUT(20);
        O(39):=INPUT(60);
        O(40):=INPUT(28);
        O(41):=INPUT(35);
        O(42):=INPUT(3);
        O(43):=INPUT(43);
        O(44):=INPUT(11);
        O(45):=INPUT(51);
        O(46):=INPUT(19);
        O(47):=INPUT(59);
        O(48):=INPUT(27);
        O(49):=INPUT(34);
        O(50):=INPUT(2);
        O(51):=INPUT(42);
        O(52):=INPUT(10);
        O(53):=INPUT(50);
        O(54):=INPUT(18);
        O(55):=INPUT(58);
        O(56):=INPUT(26);
        O(57):=INPUT(33);
        O(58):=INPUT(1);
        O(59):=INPUT(41);
        O(60):=INPUT(9);
        O(61):=INPUT(49);
        O(62):=INPUT(17);
        O(63):=INPUT(57);
        O(64):=INPUT(25);
	return O;
	end IPminus1;
--------------------------------------------------------------------------
-- IMPORTANT the funktions IP, IPminus1,...
-- are encode as (1 to 64) instead of (63 downto 0) 
--------------------------------------------------------------------------


function E(INPUT : in STD_LOGIC_VECTOR(1 to 32))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(1 to 48);
	begin 
	O(1):=INPUT(32);
        O(2):=INPUT(1);
        O(3):=INPUT(2);
        O(4):=INPUT(3);
        O(5):=INPUT(4);
        O(6):=INPUT(5);
        O(7):=INPUT(4);
        O(8):=INPUT(5);
        O(9):=INPUT(6);
        O(10):=INPUT(7);
        O(11):=INPUT(8);
        O(12):=INPUT(9);
        O(13):=INPUT(8);
        O(14):=INPUT(9);
        O(15):=INPUT(10);
        O(16):=INPUT(11);
        O(17):=INPUT(12);
        O(18):=INPUT(13);
        O(19):=INPUT(12);
        O(20):=INPUT(13);
        O(21):=INPUT(14);
        O(22):=INPUT(15);
        O(23):=INPUT(16);
        O(24):=INPUT(17);
        O(25):=INPUT(16);
        O(26):=INPUT(17);
        O(27):=INPUT(18);
        O(28):=INPUT(19);
        O(29):=INPUT(20);
        O(30):=INPUT(21);
        O(31):=INPUT(20);
        O(32):=INPUT(21);
        O(33):=INPUT(22);
        O(34):=INPUT(23);
        O(35):=INPUT(24);
        O(36):=INPUT(25);
        O(37):=INPUT(24);
        O(38):=INPUT(25);
        O(39):=INPUT(26);
        O(40):=INPUT(27);
        O(41):=INPUT(28);
        O(42):=INPUT(29);
        O(43):=INPUT(28);
        O(44):=INPUT(29);
        O(45):=INPUT(30);
        O(46):=INPUT(31);
        O(47):=INPUT(32);
        O(48):=INPUT(1);
	return O;
	end E;

--------------------------------------------------------------------------
-- IMPORTANT the funktions IP, IPminus1,...
-- are encode as (1 to 64) instead of (63 downto 0) 
--------------------------------------------------------------------------

function P(INPUT : in STD_LOGIC_VECTOR(1 to 32))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(1 to 32);
	begin 
        O(1):=INPUT(16);
        O(2):=INPUT(7);
        O(3):=INPUT(20);
        O(4):=INPUT(21);
        O(5):=INPUT(29);
        O(6):=INPUT(12);
        O(7):=INPUT(28);
        O(8):=INPUT(17);
        O(9):=INPUT(1);
        O(10):=INPUT(15);
        O(11):=INPUT(23);
        O(12):=INPUT(26);
        O(13):=INPUT(5);
        O(14):=INPUT(18);
        O(15):=INPUT(31);
        O(16):=INPUT(10);
        O(17):=INPUT(2);
        O(18):=INPUT(8);
        O(19):=INPUT(24);
        O(20):=INPUT(14);
        O(21):=INPUT(32);
        O(22):=INPUT(27);
        O(23):=INPUT(3);
        O(24):=INPUT(9);
        O(25):=INPUT(19);
        O(26):=INPUT(13);
        O(27):=INPUT(30);
        O(28):=INPUT(6);
        O(29):=INPUT(22);
        O(30):=INPUT(11);
        O(31):=INPUT(4);
        O(32):=INPUT(25);
	return O;
	end P; 
--------------------------------------------------------------------------
-- IMPORTANT the funktions IP, IPminus1,...
-- are encode as (1 to 64) instead of (63 downto 0) 
--------------------------------------------------------------------------

function PC1(INPUT : in STD_LOGIC_VECTOR(1 to 64))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(1 to 56);
	begin 
        O(1):=INPUT(57);
        O(2):=INPUT(49);
        O(3):=INPUT(41);
        O(4):=INPUT(33);
        O(5):=INPUT(25);
        O(6):=INPUT(17);
        O(7):=INPUT(9);
        O(8):=INPUT(1);
        O(9):=INPUT(58);
        O(10):=INPUT(50);
        O(11):=INPUT(42);
        O(12):=INPUT(34);
        O(13):=INPUT(26);
        O(14):=INPUT(18);
        O(15):=INPUT(10);
        O(16):=INPUT(2);
        O(17):=INPUT(59);
        O(18):=INPUT(51);
        O(19):=INPUT(43);
        O(20):=INPUT(35);
        O(21):=INPUT(27);
        O(22):=INPUT(19);
        O(23):=INPUT(11);
        O(24):=INPUT(3);
        O(25):=INPUT(60);
        O(26):=INPUT(52);
        O(27):=INPUT(44);
        O(28):=INPUT(36);
        O(29):=INPUT(63);
        O(30):=INPUT(55);
        O(31):=INPUT(47);
        O(32):=INPUT(39);
        O(33):=INPUT(31);
        O(34):=INPUT(23);
        O(35):=INPUT(15);
        O(36):=INPUT(7);
        O(37):=INPUT(62);
        O(38):=INPUT(54);
        O(39):=INPUT(46);
        O(40):=INPUT(38);
        O(41):=INPUT(30);
        O(42):=INPUT(22);
        O(43):=INPUT(14);
        O(44):=INPUT(6);
        O(45):=INPUT(61);
        O(46):=INPUT(53);
        O(47):=INPUT(45);
        O(48):=INPUT(37);
        O(49):=INPUT(29);
        O(50):=INPUT(21);
        O(51):=INPUT(13);
        O(52):=INPUT(5);
        O(53):=INPUT(28);
        O(54):=INPUT(20);
        O(55):=INPUT(12);
        O(56):=INPUT(4);
	return O;
	end PC1;

--------------------------------------------------------------------------
-- IMPORTANT the funktions IP, IPminus1,...
-- are encode as (64 downto 1) instead of (63 downto 0) 
--------------------------------------------------------------------------

function PC2(INPUT : in STD_LOGIC_VECTOR(1 to 56))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(1 to 48);
	begin 
        O(1):=INPUT(14);
        O(2):=INPUT(17);
        O(3):=INPUT(11);
        O(4):=INPUT(24);
        O(5):=INPUT(1);
        O(6):=INPUT(5);
        O(7):=INPUT(3);
        O(8):=INPUT(28);
        O(9):=INPUT(15);
        O(10):=INPUT(6);
        O(11):=INPUT(21);
        O(12):=INPUT(10);
        O(13):=INPUT(23);
        O(14):=INPUT(19);
        O(15):=INPUT(12);
        O(16):=INPUT(4);
        O(17):=INPUT(26);
        O(18):=INPUT(8);
        O(19):=INPUT(16);
        O(20):=INPUT(7);
        O(21):=INPUT(27);
        O(22):=INPUT(20);
        O(23):=INPUT(13);
        O(24):=INPUT(2);
        O(25):=INPUT(41);
        O(26):=INPUT(52);
        O(27):=INPUT(31);
        O(28):=INPUT(37);
        O(29):=INPUT(47);
        O(30):=INPUT(55);
        O(31):=INPUT(30);
        O(32):=INPUT(40);
        O(33):=INPUT(51); 
        O(34):=INPUT(45);
        O(35):=INPUT(33);
        O(36):=INPUT(48);
        O(37):=INPUT(44);
        O(38):=INPUT(49);
        O(39):=INPUT(39);
        O(40):=INPUT(56);
        O(41):=INPUT(34);
        O(42):=INPUT(53);
        O(43):=INPUT(46);
        O(44):=INPUT(42);
        O(45):=INPUT(50);
        O(46):=INPUT(36);
        O(47):=INPUT(29);
        O(48):=INPUT(32);
	return O;
	end PC2;

--------------------------------------------------------------------------
-- IMPORTANT the funktions IP, IPminus1,...
-- are encode as (1 to 64) instead of (63 downto 0) 
--------------------------------------------------------------------------

function S1(INPUT : in STD_LOGIC_VECTOR(5 downto 0))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(3 downto 0);
	begin 
  case INPUT is
      when "000000" => O := "1110";
      when "000010" => O := "0100";
      when "000100" => O := "1101";
      when "000110" => O := "0001";
      when "001000" => O := "0010";
      when "001010" => O := "1111";
      when "001100" => O := "1011";
      when "001110" => O := "1000";
      when "010000" => O := "0011";
      when "010010" => O := "1010";
      when "010100" => O := "0110";
      when "010110" => O := "1100";
      when "011000" => O := "0101";
      when "011010" => O := "1001";
      when "011100" => O := "0000";
      when "011110" => O := "0111";
      when "000001" => O := "0000";
      when "000011" => O := "1111";
      when "000101" => O := "0111";
      when "000111" => O := "0100";
      when "001001" => O := "1110";
      when "001011" => O := "0010";
      when "001101" => O := "1101";
      when "001111" => O := "0001";
      when "010001" => O := "1010";
      when "010011" => O := "0110";
      when "010101" => O := "1100";
      when "010111" => O := "1011";
      when "011001" => O := "1001";
      when "011011" => O := "0101";
      when "011101" => O := "0011";
      when "011111" => O := "1000";
      when "100000" => O := "0100";
      when "100010" => O := "0001";
      when "100100" => O := "1110";
      when "100110" => O := "1000";
      when "101000" => O := "1101";
      when "101010" => O := "0110";
      when "101100" => O := "0010";
      when "101110" => O := "1011";
      when "110000" => O := "1111";
      when "110010" => O := "1100";
      when "110100" => O := "1001";
      when "110110" => O := "0111";
      when "111000" => O := "0011";
      when "111010" => O := "1010";
      when "111100" => O := "0101";
      when "111110" => O := "0000";
      when "100001" => O := "1111";
      when "100011" => O := "1100";
      when "100101" => O := "1000";
      when "100111" => O := "0010";
      when "101001" => O := "0100";
      when "101011" => O := "1001";
      when "101101" => O := "0001";
      when "101111" => O := "0111";
      when "110001" => O := "0101";
      when "110011" => O := "1011";
      when "110101" => O := "0011";
      when "110111" => O := "1110";
      when "111001" => O := "1010";
      when "111011" => O := "0000";
      when "111101" => O := "0110";
      when "111111" => O := "1101";
      when Others =>null;
    end case;
    return O;
	end S1;

function S2(INPUT : in STD_LOGIC_VECTOR(5 downto 0))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(3 downto 0);
	begin 
    case INPUT is
      when "000000" => O := "1111";
      when "000010" => O := "0001";
      when "000100" => O := "1000";
      when "000110" => O := "1110";
      when "001000" => O := "0110";
      when "001010" => O := "1011";
      when "001100" => O := "0011";
      when "001110" => O := "0100";
      when "010000" => O := "1001";
      when "010010" => O := "0111";
      when "010100" => O := "0010";
      when "010110" => O := "1101";
      when "011000" => O := "1100";
      when "011010" => O := "0000";
      when "011100" => O := "0101";
      when "011110" => O := "1010";
      when "000001" => O := "0011";
      when "000011" => O := "1101";
      when "000101" => O := "0100";
      when "000111" => O := "0111";
      when "001001" => O := "1111";
      when "001011" => O := "0010";
      when "001101" => O := "1000";
      when "001111" => O := "1110";
      when "010001" => O := "1100";
      when "010011" => O := "0000";
      when "010101" => O := "0001";
      when "010111" => O := "1010";
      when "011001" => O := "0110";
      when "011011" => O := "1001";
      when "011101" => O := "1011";
      when "011111" => O := "0101";
      when "100000" => O := "0000";
      when "100010" => O := "1110";
      when "100100" => O := "0111";
      when "100110" => O := "1011";
      when "101000" => O := "1010";
      when "101010" => O := "0100";
      when "101100" => O := "1101";
      when "101110" => O := "0001";
      when "110000" => O := "0101";
      when "110010" => O := "1000";
      when "110100" => O := "1100";
      when "110110" => O := "0110";
      when "111000" => O := "1001";
      when "111010" => O := "0011";
      when "111100" => O := "0010";
      when "111110" => O := "1111";
      when "100001" => O := "1101";
      when "100011" => O := "1000";
      when "100101" => O := "1010";
      when "100111" => O := "0001";
      when "101001" => O := "0011";
      when "101011" => O := "1111";
      when "101101" => O := "0100";
      when "101111" => O := "0010";
      when "110001" => O := "1011";
      when "110011" => O := "0110";
      when "110101" => O := "0111";
      when "110111" => O := "1100";
      when "111001" => O := "0000";
      when "111011" => O := "0101";
      when "111101" => O := "1110";
      when "111111" => O := "1001";
      when Others =>null;
    end case;
    return O;
	end S2;

function S3(INPUT : in STD_LOGIC_VECTOR(5 downto 0))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(3 downto 0);
	begin 
  case INPUT is
      when "000000" => O := "1010";
      when "000010" => O := "0000";
      when "000100" => O := "1001";
      when "000110" => O := "1110";
      when "001000" => O := "0110";
      when "001010" => O := "0011";
      when "001100" => O := "1111";
      when "001110" => O := "0101";
      when "010000" => O := "0001";
      when "010010" => O := "1101";
      when "010100" => O := "1100";
      when "010110" => O := "0111";
      when "011000" => O := "1011";
      when "011010" => O := "0100";
      when "011100" => O := "0010";
      when "011110" => O := "1000";
      when "000001" => O := "1101";
      when "000011" => O := "0111";
      when "000101" => O := "0000";
      when "000111" => O := "1001";
      when "001001" => O := "0011";
      when "001011" => O := "0100";
      when "001101" => O := "0110";
      when "001111" => O := "1010";
      when "010001" => O := "0010";
      when "010011" => O := "1000";
      when "010101" => O := "0101";
      when "010111" => O := "1110";
      when "011001" => O := "1100";
      when "011011" => O := "1011";
      when "011101" => O := "1111";
      when "011111" => O := "0001";
      when "100000" => O := "1101";
      when "100010" => O := "0110";
      when "100100" => O := "0100";
      when "100110" => O := "1001";
      when "101000" => O := "1000";
      when "101010" => O := "1111";
      when "101100" => O := "0011";
      when "101110" => O := "0000";
      when "110000" => O := "1011";
      when "110010" => O := "0001";
      when "110100" => O := "0010";
      when "110110" => O := "1100";
      when "111000" => O := "0101";
      when "111010" => O := "1010";
      when "111100" => O := "1110";
      when "111110" => O := "0111";
      when "100001" => O := "0001";
      when "100011" => O := "1010";
      when "100101" => O := "1101";
      when "100111" => O := "0000";
      when "101001" => O := "0110";
      when "101011" => O := "1001";
      when "101101" => O := "1000";
      when "101111" => O := "0111";
      when "110001" => O := "0100";
      when "110011" => O := "1111";
      when "110101" => O := "1110";
      when "110111" => O := "0011";
      when "111001" => O := "1011";
      when "111011" => O := "0101";
      when "111101" => O := "0010";
      when "111111" => O := "1100";
      when Others =>null;
    end case;
    return O;
end S3;

function S4(INPUT : in STD_LOGIC_VECTOR(5 downto 0))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(3 downto 0);
	begin 
    case INPUT is
      when "000000" => O := "0111";
      when "000010" => O := "1101";
      when "000100" => O := "1110";
      when "000110" => O := "0011";
      when "001000" => O := "0000";
      when "001010" => O := "0110";
      when "001100" => O := "1001";
      when "001110" => O := "1010";
      when "010000" => O := "0001";
      when "010010" => O := "0010";
      when "010100" => O := "1000";
      when "010110" => O := "0101";
      when "011000" => O := "1011";
      when "011010" => O := "1100";
      when "011100" => O := "0100";
      when "011110" => O := "1111";
      when "000001" => O := "1101";
      when "000011" => O := "1000";
      when "000101" => O := "1011";
      when "000111" => O := "0101";
      when "001001" => O := "0110";
      when "001011" => O := "1111";
      when "001101" => O := "0000";
      when "001111" => O := "0011";
      when "010001" => O := "0100";
      when "010011" => O := "0111";
      when "010101" => O := "0010";
      when "010111" => O := "1100";
      when "011001" => O := "0001";
      when "011011" => O := "1010";
      when "011101" => O := "1110";
      when "011111" => O := "1001";
      when "100000" => O := "1010";
      when "100010" => O := "0110";
      when "100100" => O := "1001";
      when "100110" => O := "0000";
      when "101000" => O := "1100";
      when "101010" => O := "1011";
      when "101100" => O := "0111";
      when "101110" => O := "1101";
      when "110000" => O := "1111";
      when "110010" => O := "0001";
      when "110100" => O := "0011";
      when "110110" => O := "1110";
      when "111000" => O := "0101";
      when "111010" => O := "0010";
      when "111100" => O := "1000";
      when "111110" => O := "0100";
      when "100001" => O := "0011";
      when "100011" => O := "1111";
      when "100101" => O := "0000";
      when "100111" => O := "0110";
      when "101001" => O := "1010";
      when "101011" => O := "0001";
      when "101101" => O := "1101";
      when "101111" => O := "1000";
      when "110001" => O := "1001";
      when "110011" => O := "0100";
      when "110101" => O := "0101";
      when "110111" => O := "1011";
      when "111001" => O := "1100";
      when "111011" => O := "0111";
      when "111101" => O := "0010";
      when "111111" => O := "1110";
      when Others =>null;
    end case;
    return O;
end s4;

function S5(INPUT : in STD_LOGIC_VECTOR(5 downto 0))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(3 downto 0);
	begin 
   case INPUT is
      when "000000" => O := "0010";
      when "000010" => O := "1100";
      when "000100" => O := "0100";
      when "000110" => O := "0001";
      when "001000" => O := "0111";
      when "001010" => O := "1010";
      when "001100" => O := "1011";
      when "001110" => O := "0110";
      when "010000" => O := "1000";
      when "010010" => O := "0101";
      when "010100" => O := "0011";
      when "010110" => O := "1111";
      when "011000" => O := "1101";
      when "011010" => O := "0000";
      when "011100" => O := "1110";
      when "011110" => O := "1001";
      when "000001" => O := "1110";
      when "000011" => O := "1011";
      when "000101" => O := "0010";
      when "000111" => O := "1100";
      when "001001" => O := "0100";
      when "001011" => O := "0111";
      when "001101" => O := "1101";
      when "001111" => O := "0001";
      when "010001" => O := "0101";
      when "010011" => O := "0000";
      when "010101" => O := "1111";
      when "010111" => O := "1010";
      when "011001" => O := "0011";
      when "011011" => O := "1001";
      when "011101" => O := "1000";
      when "011111" => O := "0110";
      when "100000" => O := "0100";
      when "100010" => O := "0010";
      when "100100" => O := "0001";
      when "100110" => O := "1011";
      when "101000" => O := "1010";
      when "101010" => O := "1101";
      when "101100" => O := "0111";
      when "101110" => O := "1000";
      when "110000" => O := "1111";
      when "110010" => O := "1001";
      when "110100" => O := "1100";
      when "110110" => O := "0101";
      when "111000" => O := "0110";
      when "111010" => O := "0011";
      when "111100" => O := "0000";
      when "111110" => O := "1110";
      when "100001" => O := "1011";
      when "100011" => O := "1000";
      when "100101" => O := "1100";
      when "100111" => O := "0111";
      when "101001" => O := "0001";
      when "101011" => O := "1110";
      when "101101" => O := "0010";
      when "101111" => O := "1101";
      when "110001" => O := "0110";
      when "110011" => O := "1111";
      when "110101" => O := "0000";
      when "110111" => O := "1001";
      when "111001" => O := "1010";
      when "111011" => O := "0100";
      when "111101" => O := "0101";
      when "111111" => O := "0011";
      when Others =>null;
    end case;
    return O;
end s5;

function S6(INPUT : in STD_LOGIC_VECTOR(5 downto 0))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(3 downto 0);
	begin 
   case INPUT is
      when "000000" => O := "1100";
      when "000010" => O := "0001";
      when "000100" => O := "1010";
      when "000110" => O := "1111";
      when "001000" => O := "1001";
      when "001010" => O := "0010";
      when "001100" => O := "0110";
      when "001110" => O := "1000";
      when "010000" => O := "0000";
      when "010010" => O := "1101";
      when "010100" => O := "0011";
      when "010110" => O := "0100";
      when "011000" => O := "1110";
      when "011010" => O := "0111";
      when "011100" => O := "0101";
      when "011110" => O := "1011";
      when "000001" => O := "1010";
      when "000011" => O := "1111";
      when "000101" => O := "0100";
      when "000111" => O := "0010";
      when "001001" => O := "0111";
      when "001011" => O := "1100";
      when "001101" => O := "1001";
      when "001111" => O := "0101";
      when "010001" => O := "0110";
      when "010011" => O := "0001";
      when "010101" => O := "1101";
      when "010111" => O := "1110";
      when "011001" => O := "0000";
      when "011011" => O := "1011";
      when "011101" => O := "0011";
      when "011111" => O := "1000";
      when "100000" => O := "1001";
      when "100010" => O := "1110";
      when "100100" => O := "1111";
      when "100110" => O := "0101";
      when "101000" => O := "0010";
      when "101010" => O := "1000";
      when "101100" => O := "1100";
      when "101110" => O := "0011";
      when "110000" => O := "0111";
      when "110010" => O := "0000";
      when "110100" => O := "0100";
      when "110110" => O := "1010";
      when "111000" => O := "0001";
      when "111010" => O := "1101";
      when "111100" => O := "1011";
      when "111110" => O := "0110";
      when "100001" => O := "0100";
      when "100011" => O := "0011";
      when "100101" => O := "0010";
      when "100111" => O := "1100";
      when "101001" => O := "1001";
      when "101011" => O := "0101";
      when "101101" => O := "1111";
      when "101111" => O := "1010";
      when "110001" => O := "1011";
      when "110011" => O := "1110";
      when "110101" => O := "0001";
      when "110111" => O := "0111";
      when "111001" => O := "0110";
      when "111011" => O := "0000";
      when "111101" => O := "1000";
      when "111111" => O := "1101";
      when Others =>null;
    end case;
    return O;
end S6;

function S7(INPUT : in STD_LOGIC_VECTOR(5 downto 0))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(3 downto 0);
	begin 
   case INPUT is
      when "000000" => O := "0100";
      when "000010" => O := "1011";
      when "000100" => O := "0010";
      when "000110" => O := "1110";
      when "001000" => O := "1111";
      when "001010" => O := "0000";
      when "001100" => O := "1000";
      when "001110" => O := "1101";
      when "010000" => O := "0011";
      when "010010" => O := "1100";
      when "010100" => O := "1001";
      when "010110" => O := "0111";
      when "011000" => O := "0101";
      when "011010" => O := "1010";
      when "011100" => O := "0110";
      when "011110" => O := "0001";
      when "000001" => O := "1101";
      when "000011" => O := "0000";
      when "000101" => O := "1011";
      when "000111" => O := "0111";
      when "001001" => O := "0100";
      when "001011" => O := "1001";
      when "001101" => O := "0001";
      when "001111" => O := "1010";
      when "010001" => O := "1110";
      when "010011" => O := "0011";
      when "010101" => O := "0101";
      when "010111" => O := "1100";
      when "011001" => O := "0010";
      when "011011" => O := "1111";
      when "011101" => O := "1000";
      when "011111" => O := "0110";
      when "100000" => O := "0001";
      when "100010" => O := "0100";
      when "100100" => O := "1011";
      when "100110" => O := "1101";
      when "101000" => O := "1100";
      when "101010" => O := "0011";
      when "101100" => O := "0111";
      when "101110" => O := "1110";
      when "110000" => O := "1010";
      when "110010" => O := "1111";
      when "110100" => O := "0110";
      when "110110" => O := "1000";
      when "111000" => O := "0000";
      when "111010" => O := "0101";
      when "111100" => O := "1001";
      when "111110" => O := "0010";
      when "100001" => O := "0110";
      when "100011" => O := "1011";
      when "100101" => O := "1101";
      when "100111" => O := "1000";
      when "101001" => O := "0001";
      when "101011" => O := "0100";
      when "101101" => O := "1010";
      when "101111" => O := "0111";
      when "110001" => O := "1001";
      when "110011" => O := "0101";
      when "110101" => O := "0000";
      when "110111" => O := "1111";
      when "111001" => O := "1110";
      when "111011" => O := "0010";
      when "111101" => O := "0011";
      when "111111" => O := "1100";
      when Others =>null;
    end case;
    return O;
end s7;

function S8(INPUT : in STD_LOGIC_VECTOR(5 downto 0))return STD_LOGIC_VECTOR is
	variable O:STD_LOGIC_VECTOR(3 downto 0);
	begin 
   case INPUT is
      when "000000" => O := "1101";
      when "000010" => O := "0010";
      when "000100" => O := "1000";
      when "000110" => O := "0100";
      when "001000" => O := "0110";
      when "001010" => O := "1111";
      when "001100" => O := "1011";
      when "001110" => O := "0001";
      when "010000" => O := "1010";
      when "010010" => O := "1001";
      when "010100" => O := "0011";
      when "010110" => O := "1110";
      when "011000" => O := "0101";
      when "011010" => O := "0000";
      when "011100" => O := "1100";
      when "011110" => O := "0111";
      when "000001" => O := "0001";
      when "000011" => O := "1111";
      when "000101" => O := "1101";
      when "000111" => O := "1000";
      when "001001" => O := "1010";
      when "001011" => O := "0011";
      when "001101" => O := "0111";
      when "001111" => O := "0100";
      when "010001" => O := "1100";
      when "010011" => O := "0101";
      when "010101" => O := "0110";
      when "010111" => O := "1011";
      when "011001" => O := "0000";
      when "011011" => O := "1110";
      when "011101" => O := "1001";
      when "011111" => O := "0010";
      when "100000" => O := "0111";
      when "100010" => O := "1011";
      when "100100" => O := "0100";
      when "100110" => O := "0001";
      when "101000" => O := "1001";
      when "101010" => O := "1100";
      when "101100" => O := "1110";
      when "101110" => O := "0010";
      when "110000" => O := "0000";
      when "110010" => O := "0110";
      when "110100" => O := "1010";
      when "110110" => O := "1101";
      when "111000" => O := "1111";
      when "111010" => O := "0011";
      when "111100" => O := "0101";
      when "111110" => O := "1000";
      when "100001" => O := "0010";
      when "100011" => O := "0001";
      when "100101" => O := "1110";
      when "100111" => O := "0111";
      when "101001" => O := "0100";
      when "101011" => O := "1010";
      when "101101" => O := "1000";
      when "101111" => O := "1101";
      when "110001" => O := "1111";
      when "110011" => O := "1100";
      when "110101" => O := "1001";
      when "110111" => O := "0000";
      when "111001" => O := "0011";
      when "111011" => O := "0101";
      when "111101" => O := "0110";
      when "111111" => O := "1011";
      when others =>null;
    end case;
    return O;
end s8;
function S(INPUT : in STD_LOGIC_VECTOR(47 downto 0))return STD_LOGIC_VECTOR is
	variable DATA_OUT :STD_LOGIC_VECTOR(31 downto 0); 
	begin
		DATA_OUT(31 downto 28):=S1(INPUT(47 downto 42));
		DATA_OUT(27 downto 24):=S2(INPUT(41 downto 36));
		DATA_OUT(23 downto 20):=S3(INPUT(35 downto 30));
		DATA_OUT(19 downto 16):=S4(INPUT(29 downto 24));
		DATA_OUT(15 downto 12):=S5(INPUT(23 downto 18));
		DATA_OUT(11 downto  8):=S6(INPUT(17 downto 12));
		DATA_OUT(7  downto  4):=S7(INPUT(11 downto 6));
		DATA_OUT(3  downto  0):=S8(INPUT(5 downto 0));
		return DATA_OUT;
	end S;

function rot(INPUT : in STD_LOGIC_VECTOR(27 downto 0); WOHIN : in integer)return STD_LOGIC_VECTOR is
                variable result:STD_LOGIC_VECTOR(27 downto 0); 
                begin
			case WOHIN is
				when 1 => result(27 downto 1):=INPUT(26 downto 0);  
                        		  result(0):=INPUT(27);
				when 2 => result(27 downto 2):=INPUT(25 downto 0);
					  result(1 downto 0):=INPUT(27 downto 26);
				when 3 => result(26 downto 0):=INPUT(27 downto 1);
					  result(27):=INPUT(0);
				when 4 => result(25 downto 0):=INPUT(27 downto 2);
					  result(27 downto 26):=INPUT(1 downto 0); 
				when others => null;
			end case;
                        return result;
                end rot;

		
-------------------------------------------------------------------------------
-- here starts the Function STD_DES
-------------------------------------------------------------------------------

		
     variable DATA : STD_LOGIC_VECTOR(63 downto 0);
     variable KEY1 : STD_LOGIC_VECTOR(55 downto 0);
     variable KEY2 : STD_LOGIC_VECTOR(55 downto 0);
     variable L, R : STD_LOGIC_VECTOR(31 downto 0);
     variable help : STD_LOGIC_VECTOR(31 downto 0);
     variable help2 : STD_LOGIC_VECTOR(31 downto 0);
     variable C, D : STD_LOGIC_VECTOR(27 downto 0);
     variable txtline : line;  		-- hilfsvariable zum debuging
     variable breite : width := 33;  	-- textbreite
		file lrtxt :text is out "lr.txt";
		    
     begin
	DATA:=IP(DATA_IN);
	L:=DATA(63 downto 32);
	R:=DATA(31 downto 0);
	KEY1:=PC1(KEY_IN);
	C:=KEY1(55 downto 28);
	D:=KEY1(27 downto 0);
	for i in 1 to 16 loop
	        -- write(txtline,L,left,breite);
		-- write(txtline,R,right,breite);
		if ENCRYPT='0' then		-- entschluesseln, keys nach links schieben
			case i is
				when 1|2|9|16 => C:=rot(C,1); 	-- 1 nach links
						 D:=rot(D,1);
				when others =>   C:=rot(C,2); 	-- 2 nach links
					         D:=rot(D,2);
			end case;
		else				-- verschluesseln keys nach rechts schieben
			case i is
				when 1=> null;			-- nix schieben
				when 2|9|16 => C:=rot(C,3);     -- 1 nach rechts
					       D:=rot(D,3);
				when others => C:=rot(C,4);	-- 2 nach rechts
					       D:=rot(D,4);
			end case;
		end if;
		KEY2(55 downto 28):=C;
		KEY2(27 downto 0):=D;
 		help:=R;
		R:=(L xor P(S(PC2(KEY2) xor E(help))));
		L:=help;
		write(txtline, C, right, breite);
	        write(txtline, D, right, breite);
		writeline(lrtxt,txtline);
		write(txtline, L, left, breite);
-- 		writeline(lrtxt,txtline);
	        write(txtline, R, left, breite);
		writeline(lrtxt,txtline);
		assert not(L=R) report "L=R" severity note;
		
	end loop;
	DATA(63 downto 32):=R;  	
	DATA(31 downto 0):=L;
	return IPminus1(DATA);
end STD_DES;



-- purpose: creates KEY per cuting off the Paritybits 
function PARITY (KEY_IN : in STD_LOGIC_VECTOR) return STD_LOGIC_VECTOR is
    variable PARITY_CHECK : STD_LOGIC_VECTOR(8 downto 0):="000000000";  -- Bit 8 is the
									-- overall parity,
									-- Bit 7-0 are the block-parities
    variable KEY_OUT : STD_LOGIC_VECTOR(55 downto 0);
    
begin  -- PARITY
         -- first check for Parity and set a report
    
         PARITY_CHECK(0):= KEY_IN(0) xor KEY_IN(1) xor KEY_IN(2) xor KEY_IN(3) xor KEY_IN(4) xor KEY_IN(5) xor KEY_IN(6);
          PARITY_CHECK(1):= KEY_IN(8) xor KEY_IN(9) xor KEY_IN(10) xor KEY_IN(11) xor KEY_IN(12) xor KEY_IN(13) xor KEY_IN(14);
          PARITY_CHECK(2):= KEY_IN(16) xor KEY_IN(17) xor KEY_IN(18) xor KEY_IN(19) xor KEY_IN(20) xor KEY_IN(21) xor KEY_IN(22);
          PARITY_CHECK(3):= KEY_IN(24) xor KEY_IN(25) xor KEY_IN(26) xor KEY_IN(27) xor KEY_IN(28) xor KEY_IN(29) xor KEY_IN(30);
          PARITY_CHECK(4):= KEY_IN(32) xor KEY_IN(33) xor KEY_IN(34) xor KEY_IN(35) xor KEY_IN(36) xor KEY_IN(37) xor KEY_IN(38);
          PARITY_CHECK(5):= KEY_IN(40) xor KEY_IN(41) xor KEY_IN(42) xor KEY_IN(43) xor KEY_IN(44) xor KEY_IN(45) xor KEY_IN(46);
          PARITY_CHECK(6):= KEY_IN(48) xor KEY_IN(49) xor KEY_IN(50) xor KEY_IN(51) xor KEY_IN(52) xor KEY_IN(53) xor KEY_IN(54);
          PARITY_CHECK(7):= KEY_IN(56) xor KEY_IN(57) xor KEY_IN(58) xor KEY_IN(59) xor KEY_IN(60) xor KEY_IN(61) xor KEY_IN(62);
     
	  PARITY_CHECK(8):=(PARITY_CHECK(0) xor KEY_IN(7)) or (PARITY_CHECK(1) xor KEY_IN(15)) or (PARITY_CHECK(2) xor KEY_IN(23)) 
               or (PARITY_CHECK(3) xor KEY_IN(31)) or (PARITY_CHECK(4) xor KEY_IN(39)) or (PARITY_CHECK(5) xor KEY_IN(47))
               or (PARITY_CHECK(6) xor KEY_IN(55)) or (PARITY_CHECK(7) xor KEY_IN(63));   

	 assert PARITY_CHECK(8)='0' report "Parity-error" severity note;
    
          KEY_OUT( 6 downto  0) := KEY_IN( 6 downto  0);
          KEY_OUT(13 downto  7) := KEY_IN(14 downto  8);
          KEY_OUT(20 downto 14) := KEY_IN(22 downto 16);
          KEY_OUT(27 downto 21) := KEY_IN(30 downto 24);
          KEY_OUT(34 downto 28) := KEY_IN(38 downto 32);
          KEY_OUT(41 downto 35) := KEY_IN(46 downto 40);
          KEY_OUT(48 downto 42) := KEY_IN(54 downto 48);
          KEY_OUT(55 downto 49) := KEY_IN(62 downto 56);  
	  return KEY_OUT;
end PARITY;

  begin
     process
	type STATETYP is (awaitingdata, ready, busy, datagone);
	type KEYCOUNT is range 1 to 3;  -- for keymanagement by tripple-DES
	variable KEY1		: STD_LOGIC_VECTOR(63 downto 0);
	variable KEY2		: STD_LOGIC_VECTOR(63 downto 0);
	variable KEY3		: STD_LOGIC_VECTOR(63 downto 0);
	variable GUILTY_KEY	: STD_LOGIC_VECTOR(55 downto 0);
	variable   DATA		: STD_LOGIC_VECTOR(63 downto 0);
	variable DES_KEY	: STD_LOGIC_VECTOR(1 to 64);  -- the DES needs the key in 1 to 56 bit !!
	variable DES_DATA	: STD_LOGIC_VECTOR(1 to 64);  -- the DES needs the data 1 to 64 bit !!
	variable STATE		: STATETYP;
	variable KEY_NO         : KEYCOUNT;	-- which key is used for tripple-DES
	variable iv		: KEYCOUNT;  	-- the initialvector counter, used for CBC 
	
       begin
	if RESET='1' then
		DATA_ACK <= '0';
		ERROR <='0';
		KEY_PARITY <= '0';
		DES_READY <= '0';
		STATE:=awaitingdata;
		KEY_NO:=1;
		iv:=1;
		wait until CLK'event;
	else
	        assert RESET='0' report "Reset violation" severity note;
		wait until CLK'event and CLK='1';
		DATA_ACK <='0';   
		if TEST='1' then
			ERROR <='1';
			assert TEST='0'	report "Test aktiv" severity note;
		end if;
		if BUFFER_FREE='0' then
		    DES_READY <= '0';
		end if;
		if STATE=busy then
		    assert not(DATA(0)='U') report "DATA uninitialized" severity note;
		    if MODUS(4)='0' then  -- one way hash
			for i in 1 to 16 loop				-- one way hash
			    wait until CLK'event and CLK='1';		-- simulate 16 clockcycles
			end loop;					-- untill DES has finished its work
			DES_DATA(1 to 64):=DATA(63 downto 0);
			DES_KEY(1 to 64):=KEY1(63 downto 0);
			DES_KEY(62 to 63):="10";
			DATA:=STD_DES(DES_DATA, DES_KEY, MODUS(3)) xor DES_DATA;	-- now do the DES
			STATE:=ready;
			assert STATE=busy report "One-Way_hash done" severity note;
		    else
			-- no one-way-hash
			case MODUS(2 downto 0) is
			    when "000" =>for i in 1 to 16 loop				-- ECB-single
					    wait until CLK'event and CLK='1';		-- simulate 16 clockcycles
					end loop;
			                DES_DATA(1 to 64):=DATA(63 downto 0);
					DES_KEY(1 to 64):=KEY1(63 downto 0);
					DATA:=STD_DES(DES_DATA, DES_KEY, MODUS(3));	-- now do the DES
					STATE:=ready;
					assert STATE=busy report "ECB-Single done" severity note;
			    when "001" =>for i in 1 to 16 loop				-- ECB-EEE2
					    wait until CLK'event and CLK='1';		-- simulate 16 clockcycles
					end loop;
					DES_DATA(1 to 64):=DATA(63 downto 0);
					DES_KEY(1 to 64):=KEY1(63 downto 0);
					DATA:=STD_DES(DES_DATA, DES_KEY, MODUS(3));	-- now do the DES
					DES_KEY(1 to 64):=KEY2(63 downto 0);
					DES_DATA(1 to 64):=DATA(63 downto 0);
					DATA:=STD_DES(DES_DATA, DES_KEY, MODUS(3));
					DES_DATA(1 to 64):=DATA(63 downto 0);				     
					DES_KEY(1 to 64):=KEY1(63 downto 0);
					DATA:=STD_DES(DES_DATA, DES_KEY, MODUS(3));
					STATE:=ready;
					assert STATE=busy report "ECB-EEE2 done" severity note;
			    when "010" =>for i in 1 to 16 loop				-- ECB-EEE3
					    wait until CLK'event and CLK='1';		-- simulate 16 clockcycles
					end loop;
					DES_DATA(1 to 64):=DATA(63 downto 0);
					DES_KEY(1 to 64):=KEY1(63 downto 0);
					DATA:=STD_DES(DES_DATA, DES_KEY, MODUS(3));	-- now do the DES
					DES_KEY(1 to 64):=KEY2(63 downto 0);
					DES_DATA(1 to 64):=DATA(63 downto 0);
					DATA:=STD_DES(DES_DATA, DES_KEY, MODUS(3));
					DES_DATA(1 to 64):=DATA(63 downto 0);				     
					DES_KEY(1 to 64):=KEY3(63 downto 0);
					DATA:=STD_DES(DES_DATA, DES_KEY, MODUS(3));
					STATE:=ready;
					assert STATE=busy report "ECB-EEE3 done" severity note;
			    when "011" =>for i in 1 to 16 loop				-- ECB-EDE3
					    wait until CLK'event and CLK='1';		-- simulate 16 clockcycles
					end loop;
					DES_DATA(1 to 64):=DATA(63 downto 0);
					DES_KEY(1 to 64):=KEY1(63 downto 0);
					DATA:=STD_DES(DES_DATA, DES_KEY, MODUS(3));	-- now do the DES
					DES_KEY(1 to 64):=KEY2(63 downto 0);
					DES_DATA(1 to 64):=DATA(63 downto 0);
					DATA:=STD_DES(DES_DATA, DES_KEY, not(MODUS(3)));
					DES_DATA(1 to 64):=DATA(63 downto 0);				     
					DES_KEY(1 to 64):=KEY3(63 downto 0);
					DATA:=STD_DES(DES_DATA, DES_KEY, MODUS(3));
					STATE:=ready;
					assert STATE=busy report "ECB-EDE3 done" severity note;
			    when others => STATE:=ready;  -- hier fehlt noch CBC
					   assert STATE=busy report "CBC tut nicht" severity note;
						   
			end case;
		    end if;
		end if;
		if STATE=awaitingdata and DATA_READY='1' then
			if DATA_IS_KEY='0' then
			    if MODUS(4)='0' then  -- OWhash
				DATA_ACK <='1';
				KEY1:=DATA;
				DATA:=DATA_IN;
		                STATE:=busy;
				KEY_NO:=1;
			    elsif MODUS(2)='1' then  -- CBC
				if iv=1 then  -- noch kein IV, dann iv laden.
				    DATA:=DATA_IN;   -- lieber gepfuscht, als
				    DATA_ACK <='1';  -- keine Ausgabe
				    STATE:=awaitingdata;
				    assert STATE=busy report "iv got" severity note;
				    iv:=2;
				    KEY_NO:=1;
				else
				    DATA:=DATA_IN;
				    DATA_ACK <= '1';
				    assert STATE=busy report "cbcdata got" severity note;
				    STATE:=busy;
				end if;
			    else    
				DATA:=DATA_IN;		-- Daten uebernehmen
				DATA_ACK <= '1';	-- und bestaetigen
				STATE:=busy;
				KEY_NO:=1;
				--assert DATA_IS_KEY='1' report "got data" severity note;
			    end if;
			else
			    iv:=1;
			    if MODUS(4)='0' then  -- OW-Hash, get Data & Key in
						  -- 1 CLK-Cycle
				    KEY1:=DATA_IN;
				    DATA:=DATA_IN;
				    STATE:=busy;
				    --assert STATE=awaitingdata report "got key & data" severity note;
			    else
			        case MODUS(1 downto 0) is
				    when "00" => KEY1:=DATA_IN;		-- only one key
				    when "01" => if KEY_NO=1 then	-- two keys
						     KEY1:=DATA_IN;
						     KEY_NO:=2;
						 else
						     KEY2:=DATA_IN;
						 end if;
				    when others => if KEY_NO=1 then	-- three keys
						       KEY1:=DATA_IN;
						       KEY_NO:=2;
						   elsif KEY_NO=2 then
						       KEY2:=DATA_IN;
						       KEY_NO:=3;
						   else
						       KEY3:=DATA_IN;						       
						   end if; 
				end case;
				--assert DATA_IS_KEY='0' report "got key" severity note;
			    end if;
			    DATA_ACK <= '1';			-- Schluesseluebernahme bestaetigen
			end if;
		end if;
                if STATE=ready and BUFFER_FREE='1' then  -- We're ready and have the permission to send data
		    DATA_OUT <= DATA;
		    DES_READY <= '1';
		    STATE:=datagone;
		end if;
	 	if state=datagone and BUFFER_FREE='0' then
	 	    STATE:=awaitingdata;
	 	end if;    
	end if;
       end process;
end BEHAVIORAL;
