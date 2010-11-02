--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Thomas Schwarz
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


--entity REG_768 is
--  port(	DIn   : In 	std_logic_vector (15 downto 0); 
--	ENABLE: In      STD_LOGIC;
--        CLK   : In	STD_LOGIC;
--	DOut  : Out	std_logic_vector (767 downto 0)
--       );
--end REG_768;



architecture RTL_HIER of REG_768 is


    component REG_768_128 port(
	DIn   : In 	std_logic_vector (15 downto 0); 
	ENABLE: In      STD_LOGIC;
        CLK   : In	STD_LOGIC;
	DOut  : Out	std_logic_vector (127 downto 0)
       );
    end component;

    for all : REG_768_128 use entity WORK.REG_768_128(RTL);
   
    
  signal save : std_logic_vector(767 downto 0);
  signal inreg : std_logic_vector(15 downto 0);
  signal compena : std_logic;

  begin

    DOut <= save;
    inreg(15 downto 0) <= DIn (15 downto 0);
    compena <= ENABLE;
    
UR1:REG_768_128 port map (
    DIn (15 downto 0) => inreg(15 downto 0),
    ENABLE => compena,
    CLK => CLK,
    DOut (127 downto 0) => save(767 downto 640)
    );
UR2:REG_768_128 port map (
    DIn (15 downto 0) => save(655 downto 640),
    ENABLE => compena,
    CLK => CLK,
    DOut (127 downto 0) => save(639 downto 512)
    );
UR3:REG_768_128 port map (
    DIn (15 downto 0) => save(527 downto 512),
    ENABLE => compena,
    CLK => CLK,
    DOut (127 downto 0) => save(511 downto 384)
    );
UR4:REG_768_128 port map (
    DIn (15 downto 0) => save(399 downto 384),
    ENABLE => compena,
    CLK => CLK,
    DOut (127 downto 0) => save(383 downto 256)
    );
UR5:REG_768_128 port map (
    DIn (15 downto 0) => save(271 downto 256),
    ENABLE => compena,
    CLK => CLK,
    DOut (127 downto 0) => save(255 downto 128)
    );
UR6:REG_768_128 port map (
    DIn (15 downto 0) => save(143 downto 128),
    ENABLE => compena,
    CLK => CLK,
    DOut (127 downto 0) => save(127 downto 0)
    );
    


  end RTL_HIER;

