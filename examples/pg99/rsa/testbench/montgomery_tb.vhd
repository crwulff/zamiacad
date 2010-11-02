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

  constant NumBits : integer := 128;
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
  signal RSAVE  : std_logic_vector ((NumBits-1) downto 0);
  signal RCARRY : std_logic_vector ((NumBits-1) downto 0);
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
    -- 129704486008636688941282055427159511661
    M <= "01100001100101000010110011100011111001110110101110110100001101101101100000000110111100111001011111111011011001000101001001101101";
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
      wait until CLK'event and CLK='1';
      ENABLE <= '1';
      RESET <= '1';
      wait until CLK'event and CLK='1';
    end loop;
    RESET<='0';
    wait until CLK'event and CLK='1';
    ENABLE <= '0';
    SRTREM <= '0';
    wait until CLK'event and CLK='1';
    tmp1 := RSAVE;
    tmp2 := RCARRY;
    tmp := tmp1 + tmp2;
    if (tmp(NumBits-1) = '0') then
      ERG <= tmp;
    else
      ERG <= tmp + M;
    end if;
    wait until CLK'event and CLK='1';

    -- 67997478154067278089904667755293456870
    tmp1 := "00110011001001111101011010000010101101011011011001111101000101100011011101100011011100010011011011101000100010000011100111100110";
    assert ERG = tmp1 report "1:Divisionsrest stimmt nicht ueberein!" severity note;
	    
    -- 818516125428351639931171946524031200482
    -- assert ERG = "00111101100101000000110000010010101000010010001101111001011000001111100111011100010101100001100110110111110101000011011110110000"
    -- report "1:Divisionsrest stimmt nicht ueberein!"
    -- severity note;

    -- Konvertierung in Montogomery-Residue:
    -- 20942013734238707863332050835473887016
    VGL := "00001111110000010100100011111110100101011010110101111110001011011101100010000110001101000000111011111011110001101110111100101000";
    B <= VGL + M;
    A := ERG + M;
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
      wait until CLK'event and CLK='1';
    end loop;
    AI <= '0';
    wait until CLK'event and CLK='1';
    wait until CLK'event and CLK='1';
    ENABLE <= '0';
    wait until CLK'event and CLK='1';
    tmp := RSAVE+RCARRY;
    ERG <= tmp;
    wait until CLK'event and CLK='1';

    -- 82342642240356753376500790824009949722
    tmp1 := "00111101111100101001110110110100101111000111110100011100001111110001000000001010000100001111100101110111011000110110001000011010";
    assert ((ERG = tmp1) or ((ERG - M) = tmp1)) report "1:Montgomery-Residue stimmt nicht ueberein!" severity note;
    -- 41171321120178376688250395412004974861
    -- assert ERG = "00011110111110010100111011011010010111100011111010001110000111111000100000000101000010000111110010111011101100011011000100001101"
    -- report "1:Montgomery-Residue stimmt nicht ueberein!"
    -- severity note;


    B <= conv_std_logic_vector(1,NumBits) + M;
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
      wait until CLK'event and CLK='1';
    end loop;
    AI <= '0';
    wait until CLK'event and CLK='1';
    wait until CLK'event and CLK='1';
    ENABLE <= '0';
    wait until CLK'event and CLK='1';
    tmp := RSAVE+RCARRY;
    ERG <= tmp;
    wait until CLK'event and CLK='1';

    assert ((ERG = VGL) or ((ERG - M) = VGL))
    report "1:Ergebnis stimmt nicht ueberein!"
    severity note;
    --******************************************************



    --******************************************************
    -- 112039511392576241661236892716606671569
    M <= "01010100010010100000011001100110000010101110110100100011011010000111110111100000110000011101101010111001000010111100101011010001";
    A := conv_std_logic_vector(0,NumBits);
    AI <= '0';
    B <= not(M);
    SRTREM <= '1';
    RESET <= '1';
    wait until CLK='1';
    wait until CLK='0';
    wait until CLK='1';
    wait until CLK='0';
    RESET <= '0';
    for i in 0 to 2*(NumBits+1)-1
    loop
      wait until CLK='1';
      wait until CLK='0';
    end loop;
    ERG <= RSAVE;

    -- 9135305641520080125531762263608148617
    assert RSAVE = "00000110110111110110010101010111101001010011101110110101100101100110111100110001000111111010000010011001001001100100001010001001"
    report "2:Divisionsrest stimmt nicht ueberein!"
    severity note;

    wait until CLK='1';
    -- Konvertierung in Montogomery-Residue:
    -- 20942013734238707863332050835473887016
    VGL := "00001111110000010100100011111110100101011010110101111110001011011101100010000110001101000000111011111011110001101110111100101000";
    B <= VGL;
    A := ERG;
    SRTREM <= '0';
    RESET <= '1' after 2 ns;
    wait until CLK='0';
    wait until CLK='1';
    RESET <= '0' after 2 ns;
    for i in 0 to NumBits-1
    loop
      AI <= A(i) after 2 ns;
      wait until CLK='0';
      wait until CLK='1';
    end loop;    
    wait until CLK='0';
    wait until CLK='1';
    ERG <= RSAVE;
    wait until CLK='0';
    wait until CLK='1';

    -- 102725995216394443427632197132673117873 
    assert ERG = "01001101010010000100111010011001001101111101100010001011111000101110101011111010111110010100100000001101101011000110111010110001"
    report "2:Montgomery-Residue stimmt nicht ueberein!"
    severity note;


    A := ERG;
    B <= conv_std_logic_vector(1,NumBits);
    SRTREM <= '0';
    RESET <= '1' after 2 ns;
    wait until CLK='0';
    wait until CLK='1';
    RESET <= '0' after 2 ns;
    for i in 0 to NumBits-1
    loop
      AI <= A(i) after 2 ns;
      wait until CLK='0';
      wait until CLK='1';
    end loop;    
    wait until CLK='0';
    wait until CLK='1';
    ERG <= RSAVE;
    wait until CLK='0';
    wait until CLK='1';

    assert ERG = VGL
    report "2:Ergebnis stimmt nicht ueberein!"
    severity note;
    --******************************************************




    --******************************************************
    -- 132880664368196813867343473297703974633
    M <= "01100011111101111110001010010010111000001011101001010001100101010010011101001001001011101010101110101001010001100011011011101001";
    A := conv_std_logic_vector(0,NumBits);
    AI <= '0';
    B <= not(M);
    SRTREM <= '1';
    RESET <= '1';
    wait until CLK='1';
    wait until CLK='0';
    wait until CLK='1';
    wait until CLK='0';
    RESET <= '0';
    for i in 0 to 2*(NumBits+1)-1
    loop
      wait until CLK='1';
      wait until CLK='0';
    end loop;
    ERG <= RSAVE;

    -- 11214182385977440052090651724427360372
    assert RSAVE = "00001000011011111100010111100001000101111101001000110001001110011111101101011001110111101110100111010101000101001111100001110100"
    report "3:Divisionsrest stimmt nicht ueberein!"
    severity note;

    -- Konvertierung in Montogomery-Residue:
    -- 20942013734238707863332050835473887016
    VGL := "00001111110000010100100011111110100101011010110101111110001011011101100010000110001101000000111011111011110001101110111100101000";
    B <= VGL;
    A := ERG;
    SRTREM <= '0';
    RESET <= '1' after 2 ns;
    wait until CLK='0';
    wait until CLK='1';
    RESET <= '0' after 2 ns;
    for i in 0 to NumBits-1
    loop
      AI <= A(i) after 2 ns;
      wait until CLK='0';
      wait until CLK='1';
    end loop;    
    wait until CLK='0';
    wait until CLK='1';
    ERG <= RSAVE;
    wait until CLK='0';
    wait until CLK='1';

    -- 77834828776114217980670712846935340200
    assert ERG = "00111010100011100111000101010111110001101101101000101111001001110100100101110101011110001011101001001110010011110011010010101000"
    report "3:Montgomery-Residue stimmt nicht ueberein!"
    severity note;


    A := ERG;
    B <= conv_std_logic_vector(1,NumBits);
    SRTREM <= '0';
    RESET <= '1' after 2 ns;
    wait until CLK='0';
    wait until CLK='1';
    RESET <= '0' after 2 ns;
    for i in 0 to NumBits-1
    loop
      AI <= A(i) after 2 ns;
      wait until CLK='0';
      wait until CLK='1';
    end loop;    
    wait until CLK='0';
    wait until CLK='1';
    ERG <= RSAVE;
    wait until CLK='0';
    wait until CLK='1';

    assert ERG = VGL
    report "3:Ergebnis stimmt nicht ueberein!"
    severity note;
    --******************************************************




    --******************************************************
    -- 243372909875318195748307425039703758277
    M <= "01110111000101111110101011011111110110010010011010000000110110100010100011110011010100101110111111000000101010100101010111000101";
    A := conv_std_logic_vector(0,NumBits);
    AI <= '0';
    B <= not(M);
    SRTREM <= '1';
    RESET <= '1';
    wait until CLK='1';
    wait until CLK='0';
    wait until CLK='1';
    wait until CLK='0';
    RESET <= '0';
    for i in 0 to 2*(NumBits+1)-1
    loop
      wait until CLK='1';
      wait until CLK='0';
    end loop;
    ERG <= RSAVE;

    -- 155682075190203837126929865063933882128
    assert RSAVE = "00101010111111001111101100000000111010011000111110010101101111000001110001000010011011011011100101100111100001110111110101011111"
    report "4:Divisionsrest stimmt nicht ueberein!"
    severity note;

    -- Konvertierung in Montogomery-Residue:
    -- 20942013734238707863332050835473887016
    VGL := "00001111110000010100100011111110100101011010110101111110001011011101100010000110001101000000111011111011110001101110111100101000";
    B <= VGL;
    A := ERG;
    SRTREM <= '0';
    RESET <= '1' after 2 ns;
    wait until CLK='0';
    wait until CLK='1';
    RESET <= '0' after 2 ns;
    for i in 0 to NumBits-1
    loop
      AI <= A(i) after 2 ns;
      wait until CLK='0';
      wait until CLK='1';
    end loop;    
    wait until CLK='0';
    wait until CLK='1';
    ERG <= RSAVE;
    wait until CLK='0';
    wait until CLK='1';

    -- 14644193065166999570229304528454718929
    assert ERG = "00001011000001000101111001110110010111001010010110100011011111010001111001000111000110011001101011111000111101101001010111010001"
    report "4:Montgomery-Residue stimmt nicht ueberein!"
    severity note;


    A := ERG;
    B <= conv_std_logic_vector(1,NumBits);
    SRTREM <= '0';
    RESET <= '1' after 2 ns;
    wait until CLK='0';
    wait until CLK='1';
    RESET <= '0' after 2 ns;
    for i in 0 to NumBits-1
    loop
      AI <= A(i) after 2 ns;
      wait until CLK='0';
      wait until CLK='1';
    end loop;    
    wait until CLK='0';
    wait until CLK='1';
    ERG <= RSAVE;
    wait until CLK='0';
    wait until CLK='1';

    assert ERG = VGL
    report "4:Ergebnis stimmt nicht ueberein!"
    severity note;
    --******************************************************




    --******************************************************
    -- 124083663204198763645739988658092170157
    M <= "01011101010110011010010011101110000011011111000010110110110010101110010111001000101000010010001111111011001110101101101110101101";
    A := conv_std_logic_vector(0,NumBits);
    AI <= '0';
    B <= not(M);
    SRTREM <= '1';
    RESET <= '1';
    wait until CLK='1';
    wait until CLK='0';
    wait until CLK='1';
    wait until CLK='0';
    RESET <= '0';
    for i in 0 to 2*(NumBits+1)-1
    loop
      wait until CLK='1';
      wait until CLK='0';
    end loop;
    ERG <= RSAVE;

    -- 28449992193427433105846919517389969871
    assert RSAVE = "00010101011001110100010011101001100001100001100000101000110101101100000110101011001101000111011000000011000000111101100111001111"
    report "5:Divisionsrest stimmt nicht ueberein!"
    severity note;

    -- Konvertierung in Montogomery-Residue:
    -- 20942013734238707863332050835473887016
    VGL := "00001111110000010100100011111110100101011010110101111110001011011101100010000110001101000000111011111011110001101110111100101000";
    B <= VGL;
    A := ERG;
    SRTREM <= '0';
    RESET <= '1' after 2 ns;
    wait until CLK='0';
    wait until CLK='1';
    RESET <= '0' after 2 ns;
    for i in 0 to NumBits-1
    loop
      AI <= A(i) after 2 ns;
      wait until CLK='0';
      wait until CLK='1';
    end loop;    
    wait until CLK='0';
    wait until CLK='1';
    ERG <= RSAVE;
    wait until CLK='0';
    wait until CLK='1';

    -- 112424289365555780030688158800717944604
    assert ERG = "01010100100101000010000101101010110110000000011101000011111000111010110101010001110001111011110110000010000100100001011100011100"
    report "5:Montgomery-Residue stimmt nicht ueberein!"
    severity note;


    A := ERG;
    B <= conv_std_logic_vector(1,NumBits);
    SRTREM <= '0';
    RESET <= '1' after 2 ns;
    wait until CLK='0';
    wait until CLK='1';
    RESET <= '0' after 2 ns;
    for i in 0 to NumBits-1
    loop
      AI <= A(i) after 2 ns;
      wait until CLK='0';
      wait until CLK='1';
    end loop;    
    wait until CLK='0';
    wait until CLK='1';
    ERG <= RSAVE;
    wait until CLK='0';
    wait until CLK='1';

    assert ERG = VGL
    report "5:Ergebnis stimmt nicht ueberein!"
    severity note;
    --******************************************************


    --******************************************************
    A := conv_std_logic_vector(167,NumBits);
    B <= conv_std_logic_vector(167,NumBits);
    M <= conv_std_logic_vector(221,NumBits);
    RESET <= '1';
    wait until CLK='1';
    wait until CLK='0';
    wait until CLK='1';
    wait until CLK='0';
    RESET <= '0';
    wait until CLK='1';
    wait until CLK='0';
    SRTREM <= '0';
    for i in 0 to 8
    loop
      AI <= A(i);
      wait until CLK='1';
      wait until CLK='0';
    end loop;
    ERG <= RSAVE;
  end process;

UM1: MONTGOMERY port map (AI,B,M,SRTREM,CLK,RESET,ENABLE,RSAVE,RCARRY);

end BEHAVIOURAL;


configuration CFG_MONTGOMERY_TB of MONTGOMERY_TB is
  for BEHAVIOURAL
    for all : MONTGOMERY
      use entity WORK.MONTGOMERY(BEHAVIOURAL);
--      use configuration WORK.CFG_MONTGOMERY;
    end for;
  end for;

end CFG_MONTGOMERY_TB;
