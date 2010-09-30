library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity e1 is
  generic (foo : integer);
end;

architecture beh of e1 is
begin
end;


library ieee;
use ieee.std_logic_1164.all;
library grlib;
use grlib.stdlib.all;
--library techmap;
--use techmap.gencomp.all;
--library gaisler;
--use gaisler.mmuconfig.all;
--use gaisler.mmuiface.all;
--use gaisler.libmmu.all;

entity tcTest is
	port( a, b : IN bit; z : OUT bit);
end entity tcTest;

architecture RTL of tcTest is

  constant tlb_type        : integer range 0 to 3 := 1;
  constant M_TLB_TYPE      : integer range 0 to 1 := conv_integer(conv_std_logic_vector(tlb_type,2) and conv_std_logic_vector(1,2));  -- eather split or combined
  constant M_TLB_FASTWRITE : integer range 0 to 3 := conv_integer(conv_std_logic_vector(tlb_type,2) and conv_std_logic_vector(2,2));   -- fast writebuffer

begin

  tlbcomb0: if M_TLB_TYPE = 1 generate

    x : e1 generic map ( foo => M_TLB_TYPE );

  end generate tlbcomb0;
	
end architecture RTL;

