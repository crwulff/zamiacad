------------------------------------------------------------------------------
--
--              ModelVersion : 2.1.4
--
--              Release Date : Dec 03, 1996
--
------------------------------------------------------------------------------
LIBRARY IEEE;                   USE IEEE.STD_LOGIC_1164.ALL;


ENTITY  SPS2_256x32m4 IS

    PORT (
        Q : OUT std_logic_vector(31 DOWNTO 0);
        D : IN std_logic_vector(31 DOWNTO 0);
        A : IN std_logic_vector(7 DOWNTO 0);
        CK : IN std_logic;
        CSN : IN std_logic;
        WEN : IN std_logic;
        OEN : IN std_logic
    );

END SPS2_256x32m4;
