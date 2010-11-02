--------------------------------------------------------------------------------
--  Project:         LEON-ARC
--  Entity:          jtag_ext
--  Architecture(s): behav
--  Author:          tame@msc-ge.com
--  Company:         Gleichmann Electronics
--
--  Description:
--    This file contains a simple module that listens to the JTAG signals TCK,
--    TMS, TDI and TDO.
--    If enabled, the logger prints the current value of the 4 pins mentioned
--    above into a log file whenever they change.
--
--------------------------------------------------------------------------------


library ieee;
use ieee.std_logic_1164.all;
use std.textio.all;

library work;
use work.txt_util.all;


entity jtag_ext is
  generic (
    logfile_name : string := "logfile_jtag";
    t_delay      : time   := 5 ns);
  port (
    resetn    : in  std_logic;
    -- logging enable signal
    log_en    : in  std_logic := '1';
    -- current cycle number
    cycle_num : in  integer;
    tck       : in std_logic;
    tms       : in std_logic;
    tdi       : in  std_logic;
    tdo       : in  std_logic);
end entity;


architecture behav of jtag_ext is
  file logfile              : text open write_mode is logfile_name;
  shared variable logline   : line;
  shared variable logstring : string(1 to 80);
begin

  log_start : process is
  begin
    if log_en = '1' then
      print(logfile, "#");
      print(logfile, "# CYCLE_NUMBER TCK TMS TDI TDO");
      print(logfile, "#");
    end if;
    wait;
  end process;

  -- note: cycle number shall not be on sensitivity list
  log_loop : process (log_en, tck, tdi, tdo, tms) is
  begin
    -- suspend process as soon as log enable is deasserted
    if log_en = '0' then
      -- wait;
    elsif (log_en = '1') and (cycle_num >= 0) then
      print(logfile,
            str(cycle_num) & " " &
            str(tck) & " " &
            str(tms) & " " &
            str(tdi) & " " &
            str(tdo));
    end if;
  end process;

end architecture;
