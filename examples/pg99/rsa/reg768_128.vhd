--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : 768-Bit Register
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name : reg768.vhd 
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 16.11.98        | 17.11.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  768-Bit Register
--------------------------------------------------------------------------

library IEEE;
  use IEEE.std_logic_1164.all;
  use IEEE.std_logic_arith.all;
  use IEEE.std_logic_unsigned.all;


entity REG_768_128 is
  port(	DIn   : In 	std_logic_vector (15 downto 0); 
	ENABLE: In      STD_LOGIC;
        CLK   : In	STD_LOGIC;
	DOut  : Out	std_logic_vector (127 downto 0)
       );
end REG_768_128;



architecture RTL of REG_768_128 is
  

    component REG_768_16 port(
	DIn   : In 	std_logic_vector (15 downto 0); 
	ENABLE: In      STD_LOGIC;
        CLK   : In	STD_LOGIC;
	DOut  : Out	std_logic_vector (15 downto 0)
       );
    end component;

    for all : REG_768_16 use entity WORK.REG_768_16(RTL);

    signal save : std_logic_vector(127 downto 0);

  begin

UR1: REG_768_16 port map (
    DIn(15 downto 0) => DIn(15 downto 0),
    ENABLE => ENABLE,
    CLK => CLK,
    DOut(15 downto 0) => save(127 downto 112)
    );
UR2: REG_768_16 port map (
    DIn(15 downto 0) => save(127 downto 112),
    ENABLE => ENABLE,
    CLK => CLK,
    DOut(15 downto 0) => save(111 downto 96)
    );
UR3: REG_768_16 port map (
    DIn(15 downto 0) => save(111 downto 96),
    ENABLE => ENABLE,
    CLK => CLK,
    DOut(15 downto 0) => save(95 downto 80)
    );
UR4: REG_768_16 port map (
    DIn(15 downto 0) => save(95 downto 80),
    ENABLE => ENABLE,
    CLK => CLK,
    DOut(15 downto 0) => save(79 downto 64)
    );
UR5: REG_768_16 port map (
    DIn(15 downto 0) => save(79 downto 64),
    ENABLE => ENABLE,
    CLK => CLK,
    DOut(15 downto 0) => save(63 downto 48)
    );
UR6: REG_768_16 port map (
    DIn(15 downto 0) => save(63 downto 48),
    ENABLE => ENABLE,
    CLK => CLK,
    DOut(15 downto 0) => save(47 downto 32)
    );
UR7: REG_768_16 port map (
    DIn(15 downto 0) => save(47 downto 32),
    ENABLE => ENABLE,
    CLK => CLK,
    DOut(15 downto 0) => save(31 downto 16)
    );
UR8: REG_768_16 port map (
    DIn(15 downto 0) => save(31 downto 16),
    ENABLE => ENABLE,
    CLK => CLK,
    DOut(15 downto 0) => save(15 downto 0)
    );

DOut <= save;
    
end RTL;

