-- alu_tb.vhdl created on 2:45  2006.5.23
 

library IEEE;
--library HAPRA;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;
use IEEE.std_logic_textio.all;
--use IEEE.math_real.all;
use STD.textio.all;
--use HAPRA.txt_util.all;

entity alu_testbench is
end alu_testbench;

architecture test of alu_testbench is
  component alu
    port (
      s    : in  std_logic_vector(2 downto 0);
      a    : in  std_logic_vector(31 downto 0);
      b    : in  std_logic_vector(31 downto 0);
      q    : out std_logic_vector(31 downto 0);
      zero : out std_logic
      );
  end component;

  signal s    : std_logic_vector(2 downto 0);
  signal a    : std_logic_vector(31 downto 0);
  signal b    : std_logic_vector(31 downto 0);
  signal q    : std_logic_vector(31 downto 0);
  signal zero : std_logic;
 
--  for behavior : alu use entity work.alu(behavioral);

begin

  behavior : alu port map (
    s    => s,
    a    => a,
    b    => b,
    q    => q,
    zero => zero
    );

  tests : process

    variable test_a, test_b : std_logic_vector                                 (31 downto 0);
    variable erg_soll       : std_logic_vector                                 (31 downto 0);

  begin
    -- zero testen, werden auch falsche Signale mitgeprüft?
    a <= (others => '1');
    b <= (others => '0');
    s <= (others => '1');
    wait for 100 ns;
    assert zero = '1' report "b = 0 aber Null wurde nicht erkannt" severity error;

    -- werden alle Signale von B geprüft?
    for i in 0 to 31 loop
      a     <= (others => '0');
      b     <= (others => '0');
      b (i) <= '1';
      s     <= (others => '0');
      wait for 100 ns;
      assert zero = '0' report "b/=0 aber Null wurde erkannt" severity error;
    end loop;

-- Rechts Shift funktoniert?
-- Eine 1 nach rechts durchschieben. Erwartetes Ergebnis: Lauter 1en.

	s <= "111";
	a <= "10000000000000000000000000000000";
	for i in 0 to 31 loop
		wait for 100 ns;
		a <= q;
	end loop;
	assert q = "11111111111111111111111111111111" report "Rechts Shift mit 1en fehlgeschlagen!!!" severity error;

-- Eine 0 nach rechts durchschieben. Erwartetes Ergebnis: Lauter 0en.

	s <= "111";
	a <= "01111111111111111111111111111111";
	for i in 0 to 31 loop
		wait for 100 ns;
		a <= q;
	end loop;
	assert q = 0 report "Rechts Shift 0en fehlgeschlagen!!!" severity error;

-- Links Shift funktoniert?
-- Eine 1 nach links durchschieben. Erwartetes Ergebnis: eine 1 ganz links.

	s <= "110";
	a <= "00000000000000000000000000000001";
	for i in 0 to 30 loop -- nicht 31! sonst fällt die 1 raus.
		wait for 100 ns;
		a <= q;
	end loop;
	assert q = "10000000000000000000000000000000" report "Links Shift mit 1 fehlgeschlagen!!!" severity error;

-- Überlauf beim Addieren.

	s <= "000";
	a <= (others => '1');
	b <= conv_std_logic_vector (1, 32); -- Umwandeln einer Integer Zahl (1) in ein Vektor (32Bit lang)
	wait for 100 ns;
	assert q = conv_std_logic_vector(0,32) report "Ueberlauf bei der Addition irgendwie nicht korrekt!" severity error;

-- Die zwei Zahlen müssen zusammen IMMER 100 ergeben!

	for i in 0 to 100 loop
		a <= conv_std_logic_vector(i,32);
		b <= conv_std_logic_vector(100-i,32);
		wait for 100 ns;
		assert q = conv_std_logic_vector(100,32) report "Falsche gerechnet bei der Addition, Ergebnis != 100" severity error;
	end loop;
	
-- Subtraktion "Überlauf"

	s <= "001";
	a <= "10000000000000000000000000000000";
	b <= conv_std_logic_vector (1,32);
	wait for 100 ns;
	assert q = "01111111111111111111111111111111" report "Ueberlauf bei der Subtraktion tut nicht!" severity error;

-- Subtraktion zweier gleichgrosser Zahlen sollte 0 ergeben, d.h. X - X = 0;
	a <= (others => '1');
	b <= (others => '1');
	wait for 100 ns;
	assert q = conv_std_logic_vector(0,32) report "Subtraktion sollte 0 ergeben, tut's aber nicht!" severity error;

-- B = 0 Test

	s <= "000"; -- irgendwas, egal!
	a <= (others => '1');
	b <= (others => '0');
	wait for 100 ns;
	assert zero = '1' report "b = 0, wurde aber nicht erkannt!" severity error;

-- B != 1 Test

	s <= "000"; -- irgendwas, egal!
	a <= (others => '1');
	b <= conv_std_logic_vector(1235,32); -- Die zahl ist egal, hauptsache nicht 0!
	wait for 100 ns;
	assert zero = '0' report "b != 0, wurde aber faelschlicherweise als 0 erkannt!" severity error;

-- Funktioniert Copy ???

	s <= "100"; -- copy
	a <= conv_std_logic_vector(666, 32); -- Zahl wieder egal. Hauptsache 32 Bit lang!
	b <= conv_std_logic_vector(123, 32);
	wait for 100 ns;
	assert q = conv_std_logic_vector(666, 32) report "Copy tut nicht!!!!!!" severity error;

-- AND Test
	s <= "010"; -- and
	a <= "10101010101010101010101010101010";
	b <= "01010101010101010101010101010101";
	wait for 100 ns;
	assert q = conv_std_logic_vector(0,32) report "AND ist kaputt! sollte 0 sein, ist es aber nicht." severity error;

-- OR Test
	s <= "011"; -- or
--	a <= "10101010101010101010101010101010";
--	b <= "01010101010101010101010101010101";

    wait for 100 ns;
	assert q = 1 report "OR ist kaputt! sollte 1 sein, ist es aber nicht." severity error;

-- NOT Test
	s <= "101"; -- not
--	a <= "10101010101010101010101010101010";
--	b <= "01010101010101010101010101010101";
	wait for 100 ns;
	assert q = b report "NOT ist kaputt! sollte A sein, ist es aber nicht." severity error;
     wait;
  end process tests;
end test;
