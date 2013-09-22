-- test.vhdl created on 5:43  2006.3.20


entity test is
  port (
        irq_activate : out bit_vector(1 downto 0)
        );
end;

architecture behavioral of test is
  signal ctrl0     : bit_vector(30 downto 0);
  constant I : integer := 0;
begin
  irq_activate (1) <= ctrl0(I) = '1';
end;
