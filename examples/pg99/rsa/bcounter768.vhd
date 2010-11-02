--------------------------------------------------------------------------
--  Crypto Chip
--  Copyright (C) 1999, Projektgruppe WS98/99
--  University of Stuttgart / Department of Computer Science / IFI-RA
--------------------------------------------------------------------------
-- Designers : Arno Wacker
-- Group     : RSA
--------------------------------------------------------------------
-- Design Unit Name : B768 
-- Purpose :  Part of the RSA-module-core for the cryptochip "pg99"
-- 
-- File Name :  bcounter768.vhd
--------------------------------------------------------------------
-- Simulator : SYNOPSYS VHDL System Simulator (VSS) Version 3.2.a
--------------------------------------------------------------------
-- Date            | Changes
-- 14.12.98        | 14.12.98
--                 |
-----------------------------------------------------------------------

--------------------------------------------------------------------------
--  Was implementiert wird
--  Es wird ein binaer Zaehler implementiert. der Zaehler zaehlt von 0 bis 768
--  (769 Zyklen). Sobald die 769 Zyklen abgelaufen sind wird die READY Ltg auf
--  Eins gezogen. Mit RESET wird er wieder zurueckgesetzt.
--------------------------------------------------------------------------


library ieee;
  use ieee.std_logic_1164.all;
  use ieee.std_logic_unsigned.all;

entity B768 is
 port(CLK,RESET: in  std_logic;
      READY    : out std_logic;
      D_OUT    : out std_logic_vector(9 downto 0));
end B768;

architecture BEHAVIOUR of B768 is

begin
 process(CLK,RESET)
 variable counter : std_logic_vector(9 downto 0) := (others => '0');
 begin
  READY <= '0';   
  if reset='1' then
    counter := (others => '0');
  elsif clk'event and clk='1' then
    counter := counter + "0000000001";
  end if;
  if conv_integer(counter) = 770 then
      READY <= '1';
  end if;
 D_OUT <= counter;
 end process;
end BEHAVIOUR;




architecture RTL of B768 is

    signal l1, l2 : std_logic_vector(9 downto 0);
    
    component INC10
	port ( a	: In     std_logic_vector(9 downto 0);
	       b	: Out    std_logic_vector(9 downto 0)
	       );
    end component;

    component REG10
	port (clk,reset	: in  std_logic;
	      d_in	: in  std_logic_vector(9 downto 0);
	      d_out	: out std_logic_vector(9 downto 0)
	      );
    end component;

    for INC : INC10 use entity WORK.INC10(RTL);
    for REG : REG10 use entity WORK.REG10(BEHAV);
    
begin  -- RTL

    INC : INC10
	Port Map (L1, L2);
    REG : REG10
	port map (CLK, RESET, L2, L1);
    
    READY <= '1' when conv_integer(L1)=770 else
		'0';
    D_OUT <= L1;
    
end RTL;







