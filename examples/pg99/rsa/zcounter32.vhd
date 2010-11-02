--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : Z32 
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  zcounter32.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 14.12.98        | 14.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Es wir ein Zaehler inplementiert. Dieser wird durch das signal Z32_INC
--  Incrementiert, und gibt nach genau 32 Zyklen READY aus
--------------------------------------------------------------------------


library ieee;
  use ieee.std_logic_1164.all;
  use ieee.std_logic_unsigned.all;

  
entity Z32 is
 port(CLK,RESET: in  std_logic;
      INC      : in  std_logic;
      READY    : out std_logic);
end Z32;



architecture BEHAVIOUR of Z32 is

begin
 process(CLK,RESET,INC)
 variable counter : std_logic_vector(4 downto 0) := (others => '0');
 begin
  READY <= '0';   
  if reset='1' then
    counter := (others => '0');
  elsif clk'event and clk='1' and inc = '1' then
    counter := counter + "00001";
  end if;
  if conv_integer(counter) = 31 then
      READY <= '1';
  end if;
 end process;
end BEHAVIOUR;



architecture RTL of Z32 is

    signal l1, l2 : std_logic_vector(4 downto 0);
    
    component INC4i
	port ( a	: In     std_logic_vector(4 downto 0);
	       b	: Out    std_logic_vector(4 downto 0);
	       inc      : in     std_logic
	       );
    end component;

    component REG4
	port (clk,reset	: in  std_logic;
	      d_in	: in  std_logic_vector(4 downto 0);
	      d_out	: out std_logic_vector(4 downto 0)
	      );
    end component;

    for INCR : INC4i use entity WORK.INC4i(RTL);
    for REGI : REG4 use entity WORK.REG4(BEHAV);
    
begin  -- RTL

    INCR : INC4i
	Port Map (L1, L2, INC);
    REGI : REG4
	port map (CLK, RESET, L2, L1);

    READY <= '1' when conv_integer(L1)=31 else
		'0';
    
end RTL;
