entity tc2Test is
	port( a, b : IN bit; z : OUT bit);
end entity tc2Test;

architecture RTL of tc2Test is

    function round_div (a : integer; b : integer) return integer is
        variable rv : integer := a/b;
        variable rrv : real := (real(a)/real(b))+0.5;
    begin
        if a mod b = 0 then
            return a/b;
        end if;
        return integer(rrv);
    end round_div;

    constant MEM_DATA_WIDTH : integer := 32;
    constant nblks : integer := round_div(MEM_DATA_WIDTH,9);
    signal   s : bit_vector((nblks*9)-1 downto 0) := (others => '0');

begin

end architecture RTL;

