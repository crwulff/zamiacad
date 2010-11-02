--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Thomas Schwarz
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : Montgomery Multiplizierer
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  montgomery_tb.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
--                 | 
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Testbench fuer montgomery.vhd
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;


entity MONTGOMERY_TB is
end MONTGOMERY_TB;


architecture BEHAVIOURAL of MONTGOMERY_TB is

  constant NumBits : integer := 768;
  component MONTGOMERY
  port( AI     : in std_logic;
        B      : in std_logic_vector ((NumBits-1) downto 0);
        M      : in std_logic_vector ((NumBits-1) downto 0);
        SRTREM : in std_logic;
        CLK    : in std_logic;
        RESET  : in std_logic; 
	ENABLE : in std_logic;
	RSAVE  : out std_logic_vector ((NumBits-1) downto 0);
	RCARRY : out std_logic_vector ((NumBits-1) downto 0)
      );
  end component;

  signal AI     : std_logic;
  signal B      : std_logic_vector ((NumBits-1) downto 0);
  signal M      : std_logic_vector ((NumBits-1) downto 0);
  signal SRTREM : std_logic;
  signal CLK    : std_logic := '0';
  signal RESET  : std_logic;
  signal ENABLE : std_logic;
  signal RSAVE1 : std_logic_vector ((NumBits-1) downto 0);
  signal RCARRY1: std_logic_vector ((NumBits-1) downto 0);
  signal RSAVE2 : std_logic_vector ((NumBits-1) downto 0);
  signal RCARRY2: std_logic_vector ((NumBits-1) downto 0);
  signal ERG    : std_logic_vector ((NumBits-1) downto 0);

begin
  takter : process
  begin
    CLK <= '1';
    wait for 10 ns;
    CLK <= '0';
    wait for 10 ns;
  end process;


  lalal : process
  variable A : std_logic_vector((NumBits-1) downto 0);
  variable VGL : std_logic_vector((NumBits-1) downto 0);
  variable tmp : std_logic_vector((NumBits-1) downto 0);
  variable tmp1 : std_logic_vector((NumBits-1) downto 0);
  variable tmp2 : std_logic_vector((NumBits-1) downto 0);
  begin
    wait until CLK'event and CLK='1';
    --******************************************************
    M <= "011000010001011001111100101100010100001000100101101011101000001111101001100101110110011110110001111001011001011001000010011000110100110000110101110100001111001110011001011100101110010111110010011000111101100010000001010110010011010111110110001000010011010001110100110000001000010010011111111011100011011000010010011101100101101110011101001011111011101000010101011111001100000100011001001100111001000100010110101001101000101001110001101101000000111101110010000111110111111101110000111101101111111001101001101010111010010111110011010110111101010010111110101010011100011010111011110101001000110101100110000011101001110110111001100101000000111110100111101110011101001110000001100000100010001000100011001001000110001100001101100110010010111010100110101101101001011011000101";

    wait until CLK'event and CLK='1';  	-- damit M geladen ist!
    A := conv_std_logic_vector(0,NumBits);
    AI <= '0';
    tmp := '0'&M(NumBits-1 downto 1);
    B <= not(tmp);
    SRTREM <= '1';
    RESET <= '1';
    ENABLE <= '0';
    wait until CLK'event and CLK='1';
    RESET <= '0';
    for i in 0 to NumBits+2
    loop
      ENABLE <= '0';
      RESET <= '0';
      assert (RSAVE1=RSAVE2) report "RSAVE ERROR!" severity error;
      assert (RCARRY1=RCARRY2) report "RCARRY ERROR!" severity error;
      wait until CLK'event and CLK='1';
      ENABLE <= '1';
      RESET <= '1';
      assert (RSAVE1=RSAVE2) report "RSAVE ERROR!" severity error;
      assert (RCARRY1=RCARRY2) report "RCARRY ERROR!" severity error;
      wait until CLK'event and CLK='1';
    end loop;
    RESET<='0';
    assert (RSAVE1=RSAVE2) report "RSAVE ERROR!" severity error;
    assert (RCARRY1=RCARRY2) report "RCARRY ERROR!" severity error;
    wait until CLK'event and CLK='1';
    ENABLE <= '0';
    SRTREM <= '0';
    assert (RSAVE1=RSAVE2) report "RSAVE ERROR!" severity error;
    assert (RCARRY1=RCARRY2) report "RCARRY ERROR!" severity error;
    wait until CLK'event and CLK='1';
    tmp1 := RSAVE2;
    tmp2 := RCARRY2;
    tmp := tmp1 + tmp2;
    if (tmp(NumBits-1) = '0') then
      ERG <= tmp;
    else
      ERG <= tmp + M;
    end if;
    wait until CLK'event and CLK='1';

--    -- 67997478154067278089904667755293456870
--    tmp1 := "00110011001001111101011010000010101101011011011001111101000101100011011101100011011100010011011011101000100010000011100111100110";
--    assert ERG = tmp1 report "1:Divisionsrest stimmt nicht ueberein!" severity note;
	    

    -- Konvertierung in Montogomery-Residue:
    -- 20942013734238707863332050835473887016
    VGL := "010110011101100101101011001011001110111001100001110101111011011101100010000111111010100101101011010011110010110011000010101001111110110010001101001000111001110000010110011101100110111110001100011101000110101110100110011010001000000111101111100111011100111110110001000111101100100110111000000110111011010110111011101010110101110110111001001000100000101110100100000110010011000110000010000010000101100000011000101100001000101100000010111100100110011101011011001111110111101000100011000100110001011001101010001011011001001010010001110000000110100111000111000001011100100110011111100010001010011010101111001001100101111001011011100100100011110101010101100000001110000011101000001101011000001001101101011110000100100111100111001001111101000111010111000110101011000111000000";
    B <= VGL;
    A := ERG;
    SRTREM <= '0';
    RESET <= '1';
    ENABLE <= '0';
    AI <= A(0);
    wait until CLK='1' and CLK'event;
    RESET <= '0';
    ENABLE <= '1';
    for i in 1 to NumBits-1
    loop
      AI <= A(i);
      assert (RSAVE1=RSAVE2) report "RSAVE ERROR!" severity error;
      assert (RCARRY1=RCARRY2) report "RCARRY ERROR!" severity error;
      wait until CLK'event and CLK='1';
    end loop;
    AI <= '0';
    assert (RSAVE1=RSAVE2) report "RSAVE ERROR!" severity error;
    assert (RCARRY1=RCARRY2) report "RCARRY ERROR!" severity error;
    wait until CLK'event and CLK='1';
    assert (RSAVE1=RSAVE2) report "RSAVE ERROR!" severity error;
    assert (RCARRY1=RCARRY2) report "RCARRY ERROR!" severity error;
    wait until CLK'event and CLK='1';
    ENABLE <= '0';
    assert (RSAVE1=RSAVE2) report "RSAVE ERROR!" severity error;
    assert (RCARRY1=RCARRY2) report "RCARRY ERROR!" severity error;
    wait until CLK'event and CLK='1';
    tmp := RSAVE2+RCARRY2;
    ERG <= tmp;
    wait until CLK'event and CLK='1';

--    -- 82342642240356753376500790824009949722
--    tmp1 := "00111101111100101001110110110100101111000111110100011100001111110001000000001010000100001111100101110111011000110110001000011010";
--    assert ((ERG = tmp1) or ((ERG - M) = tmp1)) report "1:Montgomery-Residue stimmt nicht ueberein!" severity note;



    B <= conv_std_logic_vector(1,NumBits);
    A := ERG;
    SRTREM <= '0';
    RESET <= '1';
    ENABLE <= '0';
    AI <= A(0);
    wait until CLK='1' and CLK'event;
    RESET <= '0';
    ENABLE <= '1';
    for i in 1 to NumBits-1
    loop
      AI <= A(i);
      assert (RSAVE1=RSAVE2) report "RSAVE ERROR!" severity error;
      assert (RCARRY1=RCARRY2) report "RCARRY ERROR!" severity error;
      wait until CLK'event and CLK='1';
    end loop;
    AI <= '0';
    assert (RSAVE1=RSAVE2) report "RSAVE ERROR!" severity error;
    assert (RCARRY1=RCARRY2) report "RCARRY ERROR!" severity error;
    wait until CLK'event and CLK='1';
    assert (RSAVE1=RSAVE2) report "RSAVE ERROR!" severity error;
    assert (RCARRY1=RCARRY2) report "RCARRY ERROR!" severity error;
    wait until CLK'event and CLK='1';
    ENABLE <= '0';
    assert (RSAVE1=RSAVE2) report "RSAVE ERROR!" severity error;
    assert (RCARRY1=RCARRY2) report "RCARRY ERROR!" severity error;
    wait until CLK'event and CLK='1';
    tmp := RSAVE2+RCARRY2;
    ERG <= tmp;
    wait until CLK'event and CLK='1';

    assert ((ERG = VGL) or ((ERG - M) = VGL))
    report "1:Ergebnis stimmt nicht ueberein!"
    severity note;
    --******************************************************


    assert false report "!!!ENDE!!!" severity failure;
  end process;

UM1: MONTGOMERY port map (AI,B,M,SRTREM,CLK,RESET,ENABLE,RSAVE1,RCARRY1);
UM2: MONTGOMERY port map (AI,B,M,SRTREM,CLK,RESET,ENABLE,RSAVE2,RCARRY2);
  
end BEHAVIOURAL;


--configuration CFG_MONTGOMERY_TB of MONTGOMERY_TB is
--  for BEHAVIOURAL
--    for all : MONTGOMERY
--      use entity WORK.MONTGOMERY(BEHAVIOURAL);
--      use configuration WORK.CFG_MONT_TOPHIER;
--      use configuration WORK.CFG_MONTGOMERY;
--    end for;
--  end for;
--end CFG_MONTGOMERY_TB;

configuration CFG_MONTGOMERY_TB_HIER of MONTGOMERY_TB is
  for BEHAVIOURAL
    for UM2 : MONTGOMERY
      use configuration WORK.CFG_MONT_TOPHIER;
    end for;
    for UM1 : MONTGOMERY
      use configuration WORK.CFG_MONTGOMERY;
    end for;
  end for;
end CFG_MONTGOMERY_TB_HIER;
