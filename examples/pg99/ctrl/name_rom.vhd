-- VHDL Model Created from SGE Symbol name_rom.sym -- Dec 21 16:07:25 1998

library IEEE;
use IEEE.std_logic_1164.all;

entity NAME_ROM is
      Port (       A : In    std_logic_vector (7 downto 0);
                  CS : In    std_logic;
                 OEN : In    std_logic;
                   Q : Out   std_logic_vector (31 downto 0) );
end NAME_ROM;


architecture BEHAVIORAL of NAME_ROM is
    signal Q_I : STD_LOGIC_VECTOR(4 downto 0);

    constant CHAR_A : STD_LOGIC_VECTOR(4 downto 0) := "00001";
    constant CHAR_B : STD_LOGIC_VECTOR(4 downto 0) := "00010";
    constant CHAR_C : STD_LOGIC_VECTOR(4 downto 0) := "00011";
    constant CHAR_D : STD_LOGIC_VECTOR(4 downto 0) := "00100";
    constant CHAR_E : STD_LOGIC_VECTOR(4 downto 0) := "00101";
    constant CHAR_F : STD_LOGIC_VECTOR(4 downto 0) := "00110";
    constant CHAR_G : STD_LOGIC_VECTOR(4 downto 0) := "00111";
    constant CHAR_H : STD_LOGIC_VECTOR(4 downto 0) := "01000";
    constant CHAR_I : STD_LOGIC_VECTOR(4 downto 0) := "01001";
    constant CHAR_J : STD_LOGIC_VECTOR(4 downto 0) := "01010";
    constant CHAR_K : STD_LOGIC_VECTOR(4 downto 0) := "01011";
    constant CHAR_L : STD_LOGIC_VECTOR(4 downto 0) := "01100";
    constant CHAR_M : STD_LOGIC_VECTOR(4 downto 0) := "01101";
    constant CHAR_N : STD_LOGIC_VECTOR(4 downto 0) := "01110";
    constant CHAR_O : STD_LOGIC_VECTOR(4 downto 0) := "01111";
    constant CHAR_P : STD_LOGIC_VECTOR(4 downto 0) := "10000";
    constant CHAR_Q : STD_LOGIC_VECTOR(4 downto 0) := "10001";
    constant CHAR_R : STD_LOGIC_VECTOR(4 downto 0) := "10010";
    constant CHAR_S : STD_LOGIC_VECTOR(4 downto 0) := "10011";
    constant CHAR_T : STD_LOGIC_VECTOR(4 downto 0) := "10100";
    constant CHAR_U : STD_LOGIC_VECTOR(4 downto 0) := "10101";
    constant CHAR_V : STD_LOGIC_VECTOR(4 downto 0) := "10110";
    constant CHAR_W : STD_LOGIC_VECTOR(4 downto 0) := "10111";
    constant CHAR_X : STD_LOGIC_VECTOR(4 downto 0) := "11000";
    constant CHAR_Y : STD_LOGIC_VECTOR(4 downto 0) := "11001";
    constant CHAR_Z : STD_LOGIC_VECTOR(4 downto 0) := "11010";
    constant CHAR   : STD_LOGIC_VECTOR(4 downto 0) := "11111";

begin

    Q_I <= CHAR_A when A = "00000000" else
	   CHAR_R when A = "00000001" else
	   CHAR_N when A = "00000010" else
	   CHAR_O when A = "00000011" else
	   CHAR   when A = "00000100" else
	   CHAR_W when A = "00000101" else
	   CHAR_A when A = "00000110" else
	   CHAR_C when A = "00000111" else
	   CHAR_K when A = "00001000" else
	   CHAR_E when A = "00001001" else
	   CHAR_R when A = "00001010" else
	   CHAR   when A = "00001011" else
	   CHAR   when A = "00011111" else
	   CHAR_T when A = "00100000" else
	   CHAR_H when A = "00100001" else
	   CHAR_O when A = "00100010" else
	   CHAR_M when A = "00100011" else
	   CHAR_A when A = "00100100" else
	   CHAR_S when A = "00100101" else
	   CHAR   when A = "00100110" else
	   CHAR_S when A = "00100111" else
	   CHAR_T when A = "00101000" else
	   CHAR_A when A = "00101001" else
	   CHAR_N when A = "00101010" else
	   CHAR_K when A = "00101011" else
	   CHAR_A when A = "00101100" else
	   CHAR   when A = "00101101" else
	   CHAR   when A = "00111111" else
	   CHAR_T when A = "01000000" else
	   CHAR_H when A = "01000001" else
	   CHAR_O when A = "01000010" else
	   CHAR_M when A = "01000011" else
	   CHAR_A when A = "01000100" else
	   CHAR_S when A = "01000101" else
	   CHAR   when A = "01000110" else
	   CHAR_S when A = "01000111" else
	   CHAR_C when A = "01001000" else
	   CHAR_H when A = "01001001" else
	   CHAR_W when A = "01001010" else
	   CHAR_A when A = "01001011" else
	   CHAR_R when A = "01001100" else
	   CHAR_Z when A = "01001101" else
	   CHAR   when A = "01001110" else
	   CHAR   when A = "01011111" else
	   CHAR_J when A = "01100000" else
	   CHAR_E when A = "01100001" else
	   CHAR_N when A = "01100010" else
	   CHAR_S when A = "01100011" else
	   CHAR   when A = "01100100" else
	   CHAR_K when A = "01100101" else
	   CHAR_U when A = "01100110" else
	   CHAR_E when A = "01100111" else
	   CHAR_N when A = "01101000" else
	   CHAR_Z when A = "01101001" else
	   CHAR_E when A = "01101010" else
	   CHAR_R when A = "01101011" else
	   CHAR   when A = "01101100" else
	   CHAR   when A = "01111111" else
	   CHAR_J when A = "10000000" else
	   CHAR_O when A = "10000001" else
	   CHAR_E when A = "10000010" else
	   CHAR_R when A = "10000011" else
	   CHAR_G when A = "10000100" else
	   CHAR   when A = "10000101" else
	   CHAR_H when A = "10000110" else
	   CHAR_O when A = "10000111" else
	   CHAR_L when A = "10001000" else
	   CHAR_Z when A = "10001001" else
	   CHAR_H when A = "10001010" else
	   CHAR_A when A = "10001011" else
	   CHAR_U when A = "10001100" else
	   CHAR_E when A = "10001101" else
	   CHAR_R when A = "10001110" else
	   CHAR   when A = "10001111" else
	   CHAR   when A = "10011111" else
	   CHAR_T when A = "10100000" else
	   CHAR_O when A = "10100001" else
	   CHAR_B when A = "10100010" else
	   CHAR_I when A = "10100011" else
	   CHAR_A when A = "10100100" else
	   CHAR_S when A = "10100101" else
	   CHAR   when A = "10100110" else
	   CHAR_E when A = "10100111" else
	   CHAR_N when A = "10101000" else
	   CHAR_G when A = "10101001" else
	   CHAR_E when A = "10101010" else
	   CHAR   when A = "10101011" else
	   CHAR   when A = "10111111" else
	   CHAR_M when A = "11000000" else
	   CHAR_A when A = "11000001" else
	   CHAR_R when A = "11000010" else
	   CHAR_K when A = "11000011" else
	   CHAR_U when A = "11000100" else
	   CHAR_S when A = "11000101" else
	   CHAR   when A = "11000110" else
	   CHAR_B when A = "11000111" else
	   CHAR_U when A = "11001000" else
	   CHAR_S when A = "11001001" else
	   CHAR_C when A = "11001010" else
	   CHAR_H when A = "11001011" else
	   CHAR   when A = "11001100" else
	   CHAR   when A = "11011111" else
	   CHAR_D when A = "11100000" else
	   CHAR_I when A = "11100001" else
	   CHAR_R when A = "11100010" else
	   CHAR_K when A = "11100011" else
	   CHAR   when A = "11100100" else
	   CHAR_A when A = "11100101" else
	   CHAR_L when A = "11100110" else
	   CHAR_L when A = "11100111" else
	   CHAR_M when A = "11101000" else
	   CHAR_E when A = "11101001" else
	   CHAR_N when A = "11101010" else
	   CHAR_D when A = "11101011" else
	   CHAR_I when A = "11101100" else
	   CHAR_N when A = "11101101" else
	   CHAR_G when A = "11101110" else
	   CHAR_E when A = "11101111" else
	   CHAR_R when A = "11110000" else
	   CHAR   when A = "11110001" else
	   "-----";
	   
    Q <= "000000000000000000000000010" & Q_I when ((not OEN) and CS)='1' else
	 (others => 'Z');

end BEHAVIORAL;
