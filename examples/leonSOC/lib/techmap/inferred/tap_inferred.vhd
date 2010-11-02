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
-- Entity:      tap_gen
-- File:        tap_gen_gen.vhd
-- Author:      Edvin Catovic - Gaisler Research
-- Description: Generic JTAG Test Access Port (TAP) Controller 
------------------------------------------------------------------------------


library ieee;
use ieee.std_logic_1164.all;
library grlib;
use grlib.stdlib.all;

entity tap_gen is
  generic (
    irlen  : integer range 2 to 8 := 2;
    idcode : integer range 0 to 255 := 9;
    manf   : integer range 0 to 2047 := 804;
    part   : integer range 0 to 65535 := 0;
    ver    : integer range 0 to 15 := 0;
    trsten : integer range 0 to 1 := 1;
    scantest : integer := 0);
  port (
    trst        : in std_ulogic;
    tckp        : in std_ulogic;
    tckn        : in std_ulogic;
    tms         : in std_ulogic;
    tdi         : in std_ulogic;
    tdo         : out std_ulogic;
    tapi_en1    : in std_ulogic;
    tapi_tdo1   : in std_ulogic;
    tapi_tdo2   : in std_ulogic;
    tapo_tck    : out std_ulogic;
    tapo_tdi    : out std_ulogic;
    tapo_inst   : out std_logic_vector(7 downto 0);
    tapo_rst    : out std_ulogic;
    tapo_capt   : out std_ulogic;
    tapo_shft   : out std_ulogic;
    tapo_upd    : out std_ulogic;
    tapo_xsel1  : out std_ulogic;
    tapo_xsel2  : out std_ulogic;
    testen      : in  std_ulogic := '0';
    testrst     : in  std_ulogic := '1';
    tdoen       : out std_ulogic
    );
end;


architecture rtl of tap_gen is

  type ltap_out_type is record                            
    tck   : std_ulogic;
    tdi   : std_ulogic;
    inst  : std_logic_vector(7 downto 0);
    asel  : std_ulogic;
    dsel  : std_ulogic;
    reset : std_ulogic;
    capt  : std_ulogic;
    shift : std_ulogic;
    upd   : std_ulogic;      
  end record;

  constant BYPASS : std_logic_vector(irlen-1 downto 0) := (others => '1');
  constant IDCODE_I : std_logic_vector(irlen-1 downto 0) := conv_std_logic_vector(idcode, irlen);
  constant ID : std_logic_vector(31 downto 0) := conv_std_logic_vector(ver, 4) &
                                                 conv_std_logic_vector(part, 16) &
                                                 conv_std_logic_vector(manf, 11) & '1';
  
  type state_type is (test_rst, run_idle, select_dr, capture_dr, shift_dr, exit1_dr,
                      pause_dr, exit2_dr, update_dr, select_ir, capture_ir, shift_ir,
                      exit1_ir, pause_ir, exit2_ir, update_ir); 

  type pos_reg_type is record
   state  : state_type;
   shft   : std_logic_vector(31 downto 0);
  end record;

  type neg_reg_type is record
   inst   : std_logic_vector(irlen-1 downto 0);   
   tdo    : std_ulogic;
   reset  : std_ulogic;
   capt   : std_ulogic;
   upd    : std_ulogic;
   shift  : std_ulogic;
   oen    : std_ulogic;
  end record;

  signal rp, rpin : pos_reg_type;
  signal rn, rnin : neg_reg_type;
  signal arst : std_ulogic;
begin  

  arst <= testrst  when (scantest = 1) and (testen = '1') else trst;
  
  comb : process(tckp, tms, tdi, tapi_en1, tapi_tdo1, tapi_tdo2, rp, rn)    
    variable vp : pos_reg_type;
    variable vn : neg_reg_type;    
    variable vtapo : ltap_out_type;
    variable oen : std_ulogic;
  begin
    
    vp := rp; vn := rn; oen := '0';
    if (scantest = 1) and (testen = '1') then vtapo.tck := rn.inst(0);
    else vtapo.tck := tckp; end if;
    vtapo.reset := rn.reset; vtapo.tdi := tdi;
    vtapo.inst := (others => '0'); vtapo.inst(irlen-1 downto 0) := rn.inst;
    vtapo.capt := rn.capt; vtapo.upd := rn.upd; vtapo.shift := rn.shift; vtapo.asel := '0'; vtapo.dsel := '0';
    vn.capt := '0'; vn.upd := '0'; vn.shift := '0'; vn.reset := '0'; vn.oen := '0';
    if (rn.inst = IDCODE_I) or (rn.inst = BYPASS) then vn.tdo := rp.shft(0);
    elsif tapi_en1 = '1' then vn.tdo := tapi_tdo1;
    else vn.tdo := tapi_tdo2; end if;    
                                                     
    case rp.state is
      when test_rst   => if tms = '0' then vp.state := run_idle; end if;
      when run_idle   => if tms = '1' then vp.state := select_dr; end if;
      when select_dr  => if tms = '0' then vp.state := capture_dr; else vp.state := select_ir; end if;
      when capture_dr => if tms = '0' then vp.state := shift_dr; else vp.state := exit1_dr; end if;
      when shift_dr   => if tms = '1' then vp.state := exit1_dr; end if;
      when exit1_dr   => if tms = '0' then vp.state := pause_dr; else vp.state := update_dr; end if;
      when pause_dr   => if tms = '1' then vp.state := exit2_dr; end if;
      when exit2_dr   => if tms = '0' then vp.state := shift_dr; else vp.state := update_dr; end if;
      when update_dr  => if tms = '0' then vp.state := run_idle; else vp.state := select_dr; end if;
      when select_ir  => if tms = '0' then vp.state := capture_ir; else vp.state := test_rst; end if;
      when capture_ir => if tms = '0' then vp.state := shift_ir; else vp.state := exit1_ir; end if;
      when shift_ir   => if tms = '1' then vp.state := exit1_ir; end if;
      when exit1_ir   => if tms = '0' then vp.state := pause_ir; else vp.state := update_ir; end if;
      when pause_ir   => if tms = '1' then vp.state := exit2_ir; end if;
      when exit2_ir   => if tms = '0' then vp.state := shift_ir; else vp.state := update_ir; end if;
      when others  => if tms = '0' then vp.state := run_idle; else vp.state := select_dr; end if;                         
    end case;
      
    case rp.state is
      when test_rst =>
        vn.reset := '1'; vn.inst := IDCODE_I;
      when capture_dr =>
        vn.capt := '1';
         if rn.inst = BYPASS then vp.shft(0) := '0'; end if;        
         if rn.inst = IDCODE_I then vp.shft := ID; end if;
      when shift_dr   =>
        vn.shift := '1';
        if rn.inst = BYPASS then vp.shft(0) := tdi; end if;
        if rn.inst = IDCODE_I then vp.shft := tdi & rp.shft(31 downto 1); end if;
      when update_dr  =>
        vn.upd := '1';
      when capture_ir => vp.shft(irlen-1 downto 2) := rn.inst(irlen-1 downto 2); vp.shft(1 downto 0) := "01";
      when shift_ir   => vp.shft(irlen-1 downto 0) := tdi & rp.shft(irlen-1 downto 1);
      when update_ir  => vn.inst := rp.shft(irlen-1 downto 0);                         
      when others => 
    end case;

    if (rp.state = shift_dr) or (rp.state = shift_ir) then vn.oen := '1'; end if;
    
    rpin <= vp; rnin <= vn; tdo <= rn.tdo; tdoen <= rn.oen;
    tapo_tck <= vtapo.tck; tapo_tdi <= tdi; tapo_inst <= vtapo.inst; tapo_rst <= vtapo.reset;
    tapo_capt <= vtapo.capt; tapo_shft <= vtapo.shift; tapo_upd <= vtapo.upd;
    tapo_xsel1 <= '0'; tapo_xsel2 <= '0';    
  end process;

  async : if trsten = 1 generate
  posreg : process(tckp, arst)
  begin
    if arst = '0' then
      rp.state <= test_rst;    
    elsif rising_edge(tckp) then
      rp <= rpin;
    end if;
  end process;

  negreg : process(tckn, arst)
  begin
    if arst = '0' then
      rn.inst <= IDCODE_I;
      rn.reset <= '0';
      rn.capt  <= '0';
      rn.upd   <= '0';
      rn.shift <= '0';
      rn.oen   <= '0';   
    elsif rising_edge(tckn) then
      rn <= rnin;
    end if;
  end process;
  end generate;

  sync : if trsten = 0 generate
    posreg2 : process(tckp)
    begin
      if rising_edge(tckp) then rp <= rpin; end if;
    end process;

    negreg2 : process(tckn)
    begin
      if rising_edge(tckn) then rn <= rnin; end if;
    end process;    
  end generate;    
  
end;  
