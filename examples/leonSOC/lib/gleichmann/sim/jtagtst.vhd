------------------------------------------------------------------------------
--  This file is a part of the GRLIB VHDL IP LIBRARY
--  Copyright (C) 2003, Gaisler Research
--
--  This program is free software; you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation; either version 2 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program; if not, write to the Free Software
--  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
-----------------------------------------------------------------------------
-- Package:     sim
-- File:        sim.vhd
-- Author:      Edvin Catovic - Gaisler Research
-- Description: JTAG debug link communication test 
------------------------------------------------------------------------------

-- pragma translate_off

library ieee;
use ieee.std_logic_1164.all;
use std.textio.all;
library grlib;
use grlib.stdlib.all;
use grlib.stdio.all;
use grlib.amba.all;

package jtagtst is

  procedure clkj(tmsi, tdii : in std_ulogic; tdoo : out std_ulogic;
                 signal tck, tms, tdi : out std_ulogic;
                 signal tdo           : in std_ulogic;
                 cp                   : in integer);

  procedure shift(dr : in boolean; len : in integer;
                  din : in std_logic_vector; dout : out std_logic_vector;
                  signal tck, tms, tdi : out std_ulogic;
                  signal tdo           : in std_ulogic;
                  cp                   : in integer);                  

  procedure jtagcom(signal tdo           : in std_ulogic;
                    signal tck, tms, tdi : out std_ulogic;
                    cp, start, addr      : in integer;
                                                          -- cp - TCK clock period in ns
                                                          -- start - time in us when JTAG test
                                                          -- is started
                                                          -- addr - read/write operation destination address 
                    haltcpu          : in boolean);
  

end;  


package body jtagtst is

  
  procedure clkj(tmsi, tdii : in std_ulogic; tdoo : out std_ulogic;
                 signal tck, tms, tdi : out std_ulogic;
                 signal tdo           : in std_ulogic;
                 cp                   : in integer) is
  begin
    tdi <= tdii;
    tck <= '0'; tms <= tmsi;
    wait for 2 * cp * 1 ns;
    tck <= '1'; tdoo := tdo;
    wait for 2 * cp * 1 ns;
  end;        

  procedure shift(dr : in boolean; len : in integer;
                  din : in std_logic_vector; dout : out std_logic_vector;
                  signal tck, tms, tdi : out std_ulogic;
                  signal tdo           : in std_ulogic;
                  cp                   : in integer) is
    variable dc : std_ulogic;
  begin
    clkj('0', '-', dc, tck, tms, tdi, tdo, cp);
    clkj('1', '-', dc, tck, tms, tdi, tdo, cp);
    if (not dr) then clkj('1', '-', dc, tck, tms, tdi, tdo, cp); end if;
    clkj('0', '-', dc, tck, tms, tdi, tdo, cp);  -- capture     
    clkj('0', '-', dc, tck, tms, tdi, tdo, cp);  -- shift (state)
    for i in 0 to len-2 loop
      clkj('0', din(i), dout(i), tck, tms, tdi, tdo, cp);  
    end loop;        
    clkj('1', din(len-1), dout(len-1), tck, tms, tdi, tdo, cp);  -- end shift, goto exit1
    clkj('1', '-', dc, tck, tms, tdi, tdo, cp);  -- update ir/dr
    clkj('0', '-', dc, tck, tms, tdi, tdo, cp);  -- run_test/idle                                                      
  end;

  procedure jtagcom(signal tdo : in std_ulogic;
                    signal tck, tms, tdi : out std_ulogic;
                    cp, start, addr  : in integer;
                    haltcpu          : in boolean) is
    variable inst: std_logic_vector(5 downto 0);
    variable dc : std_ulogic;
    variable dr : std_logic_vector(32 downto 0);
    variable dr2 : std_logic_vector(34 downto 0);
    variable tmp : std_logic_vector(32 downto 0);
    variable tmp2 : std_logic_vector(34 downto 0);
    variable hsize : std_logic_vector(1 downto 0);    
  begin

    tck <= '0'; tms <= '0'; tdi <= '0';

    wait for start * 1 us;    
    print("AHB JTAG TEST");    
    for i in 1 to 5 loop     -- reset
      clkj('1', '0', dc, tck, tms, tdi, tdo, cp);
    end loop;        
    clkj('0', '-', dc, tck, tms, tdi, tdo, cp);

    hsize := "10";
    --read IDCODE
    wait for 10 * cp * 1 ns;
    shift(true, 32, conv_std_logic_vector(0, 32), dr, tck, tms, tdi, tdo, cp);        
    print("JTAG TAP ID:" & tost(dr(31 downto 0)));
   
    wait for 10 * cp * 1 ns;
    shift(false, 6, conv_std_logic_vector(63, 6), dr, tck, tms, tdi, tdo, cp);    -- BYPASS

    --shift data through BYPASS reg
    shift(true, 32, conv_std_logic_vector(16#AAAA#, 16) & conv_std_logic_vector(16#AAAA#, 16), dr,
          tck, tms, tdi, tdo, cp);                

    -- put CPUs in debug mode
    if haltcpu then
    wait for 10 * cp * 1 ns;
    shift(false, 6, conv_std_logic_vector(2, 6), dr, tck, tms, tdi, tdo, cp);  -- inst = addrreg
    wait for 5 * cp * 1 ns;
    tmp2 := '1' & hsize & X"90000000"; --conv_std_logic_vector_signed(16#90000000#, 32);
    shift(true, 35, tmp2, dr2, tck, tms, tdi, tdo, cp); -- write addreg
    wait for 5 * cp * 1 ns;
    shift(false, 6, conv_std_logic_vector(3, 6), dr, tck, tms, tdi, tdo, cp);  -- inst = datareg
    wait for 5 * cp * 1 ns;
    tmp := '0' & conv_std_logic_vector(4, 32);
    shift(true, 33, tmp, dr, tck, tms, tdi, tdo, cp); -- write data
    wait for 10 * cp * 1 ns;
    shift(false, 6, conv_std_logic_vector(2, 6), dr, tck, tms, tdi, tdo, cp);  -- inst = addrreg
    wait for 5 * cp * 1 ns;
    tmp2 := '1' & hsize & X"90000020"; --conv_std_logic_vector_signed(16#90000020#, 32);
    shift(true, 35, tmp2, dr2, tck, tms, tdi, tdo, cp); -- write addreg
    wait for 5 * cp * 1 ns;
    shift(false, 6, conv_std_logic_vector(3, 6), dr, tck, tms, tdi, tdo, cp);  -- inst = datareg
    wait for 5 * cp * 1 ns;
    tmp := '0' & conv_std_logic_vector(16#ffff#, 32);
    shift(true, 33, tmp, dr, tck, tms, tdi, tdo, cp); -- write data
    print("JTAG: Putting CPU in debug mode");
    end if;

    wait for 10 * cp * 1 ns;
    shift(false, 6, conv_std_logic_vector(2, 6), dr, tck, tms, tdi, tdo, cp);  -- inst = addrreg
    wait for 5 * cp * 1 ns;
    tmp2 := '1' & hsize & conv_std_logic_vector(addr, 32);    
    shift(true, 35, tmp2, dr2, tck, tms, tdi, tdo, cp); -- write addreg
    wait for 5 * cp * 1 ns;
    shift(false, 6, conv_std_logic_vector(3, 6), dr, tck, tms, tdi, tdo, cp);  -- inst = datareg
    wait for 5 * cp * 1 ns;
    tmp := '1' & conv_std_logic_vector(16#10#, 32);
    shift(true, 33, tmp, dr, tck, tms, tdi, tdo, cp); -- write data
    print("JTAG WRITE " &  tost(conv_std_logic_vector(addr, 32)) & ":" &
          tost(conv_std_logic_vector(16#10#, 32)));        
    wait for 5 * cp * 1 ns;
    tmp := '1' & conv_std_logic_vector(16#11#,32);
    shift(true, 33, tmp, dr, tck, tms, tdi, tdo, cp); -- write data
    print("JTAG WRITE " &  tost(conv_std_logic_vector(addr+4, 32)) & ":" &
          tost(conv_std_logic_vector(16#11#, 32)));            
    wait for 5 * cp * 1 ns;
    tmp :=  '0' & conv_std_logic_vector(16#12#,32);
    print("JTAG WRITE " &  tost(conv_std_logic_vector(addr+8, 32)) & ":" &
          tost(conv_std_logic_vector(16#12#, 32)));            
    shift(true, 33, tmp, dr, tck, tms, tdi, tdo, cp); -- write data    

    wait for 10 * cp * 1 ns;
    shift(false, 6, conv_std_logic_vector(2, 6), dr, tck, tms, tdi, tdo, cp);  -- inst = addrreg
    wait for 5 * cp * 1 ns;    
    tmp2 := '0' & hsize & conv_std_logic_vector(addr, 32);    
    shift(true, 35, tmp2, dr2, tck, tms, tdi, tdo, cp); -- write addreg
    wait for 5 * cp * 1 ns;
    shift(false, 6, conv_std_logic_vector(3, 6), dr, tck, tms, tdi, tdo, cp);  -- inst = datareg
    wait for 5 * cp * 1 ns;
    tmp := (others => '0'); tmp(32) := '1'; 
    shift(true, 33, tmp, dr, tck, tms, tdi, tdo, cp); -- read data
    print("JTAG READ  " & tost(conv_std_logic_vector(addr, 32)) & ":" & tost(dr(31 downto 0)));
    assert dr(31 downto 0) = X"00000010" 
	report "JTAG read failed" severity failure;
    wait for 5 * cp * 1 ns;
    shift(true, 33, tmp, dr, tck, tms, tdi, tdo, cp); -- read data
    print("JTAG READ  " & tost(conv_std_logic_vector(addr+4, 32)) & ":" & tost(dr(31 downto 0)));
    assert dr(31 downto 0) = X"00000011" 
	report "JTAG read failed" severity failure;
    wait for 5 * cp * 1 ns;
    tmp(32) := '0';
    shift(true, 33, tmp, dr, tck, tms, tdi, tdo, cp); -- read data
    print("JTAG READ  " & tost(conv_std_logic_vector(addr+8, 32)) & ":" & tost(dr(31 downto 0)));    
    assert dr(31 downto 0) = X"00000012" 
      report "JTAG read failed" severity failure;

    -- JTAG test passed
    assert false report "JTAG test passed, halting with failure." severity note;
  end procedure;
    
end;

-- pragma translate_on

