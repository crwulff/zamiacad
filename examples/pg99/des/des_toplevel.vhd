-- VHDL Model Created from SGE Schematic des_toplevel.sch -- Dec 28 19:16:24 1998


   library LIB_des;

   library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_unsigned.all;

architecture RTL of DES is

   component DES_GESAMT_JH
      Port ( BUFFER_FREE : In    std_logic;
                 CLK : In    std_logic;
             DATA_IN : In    std_logic_vector (1 to 64);
             DATA_IS_KEY : In    std_logic;
             DATA_READY : In    std_logic;
               MODUS : In    std_logic_vector (4 downto 0);
               RESET : In    std_logic;
                TEST : In    std_logic;
             DATA_ACK : Out   std_logic;
             DATA_OUT : Out   std_logic_vector (1 to 64);
             DES_READY : Out   std_logic;
               ERROR : Out   STD_LOGIC;
             KEY_PARITY : Out   std_logic );
   end component;

begin

   I_1 : DES_GESAMT_JH
      Port Map ( BUFFER_FREE=>BUFFER_FREE, CLK=>CLK,
                 DATA_IN(1 to 64)=>DATA_IN(63 downto 0),
                 DATA_IS_KEY=>DATA_IS_KEY, DATA_READY=>DATA_READY,
                 MODUS(4 downto 0)=>MODUS(4 downto 0), RESET=>RESET,
                 TEST=>TEST, DATA_ACK=>DATA_ACK,
                 DATA_OUT(1 to 64)=>DATA_OUT(63 downto 0),
                 DES_READY=>DES_READY, ERROR=>ERROR,
                 KEY_PARITY=>KEY_PARITY );

end RTL;

configuration CFG_DES_SCHEMATIC of DES is

   for RTL
       for I_1: DES_GESAMT_JH
         use configuration LIB_des.CFG_DES_GESAMT_JH_SCHEMATIC;
      end for;
   end for;

end CFG_DES_SCHEMATIC;
