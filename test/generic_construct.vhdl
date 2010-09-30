-- This code is taken directly from the following book:
-- Yalamanchili, S. (2001) "Introductory VHDL: From  Simulation to
-- Synthesis", Xilinx Design Series, Prentice Hall Inc.

library IEEE;
use IEEE.std_logic_1164.all;

entity xor2 is
    generic (
        GateDelay:    Time := 2 ns
    );
    port (
        In1:    in    std_logic;
        In2:    in    std_logic;
        z:        out    std_logic
    );
end entity xor2;

architecture Behavioural of xor2 is
begin
    z <= (In1 xor In2) after GateDelay;
end architecture Behavioural;