--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : Z48 
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  zcounter48.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 15.12.98        | 15.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Es wir ein Zaehler inplementiert. Dieser wird durch das signal INC
--  Incrementiert, und gibt nach genau 48 Zyklen READY aus
--------------------------------------------------------------------------


library ieee;
  use ieee.std_logic_1164.all;
  use ieee.std_logic_unsigned.all;



entity Z48 is
 port(CLK,RESET: in  std_logic;
      INC      : in  std_logic;
      READY48  : out std_logic;
      READY24  : out std_logic);
end Z48;



architecture BEHAVIOUR of Z48 is

begin
 process(CLK,RESET,INC)
 variable counter : std_logic_vector(5 downto 0) := (others => '0');
 begin
  READY24 <= '0';
  READY48 <= '0';
  if reset='1' then
    counter := (others => '0');
  elsif clk'event and clk='1' and inc = '1' then
    counter := counter + "000001";
  end if;
  if conv_integer(counter) = 24 then
      READY24 <= '1';
  end if;  
  if conv_integer(counter) = 47 then
      READY48 <= '1';
  end if;
 end process;
end BEHAVIOUR;




architecture RTL of Z48 is

    signal l1, l2 : std_logic_vector(5 downto 0);
    
    component INC5i
	port ( a	: In     std_logic_vector(5 downto 0);
	       b	: Out    std_logic_vector(5 downto 0);
	       inc      : in     std_logic
	       );
    end component;

    component REG5
	port (clk,reset	: in  std_logic;
	      d_in	: in  std_logic_vector(5 downto 0);
	      d_out	: out std_logic_vector(5 downto 0)
	      );
    end component;

    for INCR : INC5i use entity WORK.INC5i(RTL);
    for REGI : REG5 use entity WORK.REG5(BEHAV);
    
begin  -- RTL

    INCR : INC5i
	Port Map (L1, L2, INC);
    REGI : REG5
	port map (CLK, RESET, L2, L1);

    READY24 <= '1' when conv_integer(L1)=24 else
		'0';
    READY48 <= '1' when conv_integer(L1)=47 else
		'0';
    
end RTL;
