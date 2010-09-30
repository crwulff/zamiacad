library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;

package libcache is
  
type log2arr is array(0 to 512) of integer;
constant log2   : log2arr := (
0,0,1,2,2,3,3,3,3,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
  6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
  8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,
  8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,
  8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,
  8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,
  others => 9);
constant log2x  : log2arr := (
0,1,1,2,2,3,3,3,3,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
  6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
  8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,
  8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,
  8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,
  8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,
  others => 9);

  function cache_cfg(repl, sets, linesize, setsize, lock, snoop,
    lram, lramsize, lramstart, mmuen : integer) return std_logic_vector;

  function orv(d : std_logic_vector) return std_ulogic;

end;

package body libcache is

  function cache_cfg(repl, sets, linesize, setsize, lock, snoop,
      lram, lramsize, lramstart, mmuen : integer) return std_logic_vector is 
  variable cfg : std_logic_vector(31 downto 0);
  begin
    cfg := (others => '0');
    cfg(31 downto 31) := conv_std_logic_vector(lock, 1);
    cfg(30 downto 28) := conv_std_logic_vector(repl+1, 3);
    if snoop /= 0 then cfg(27) := '1'; end if;
    --cfg(27 downto 27) := conv_std_logic_vector(snoop, 1);
    cfg(26 downto 24) := conv_std_logic_vector(sets-1, 3);
    cfg(23 downto 20) := conv_std_logic_vector(log2(setsize), 4);
    cfg(19 downto 19) := conv_std_logic_vector(lram, 1);
    cfg(18 downto 16) := conv_std_logic_vector(log2(linesize), 3);
    cfg(15 downto 12) := conv_std_logic_vector(log2(lramsize), 4);
    cfg(11 downto  4) := conv_std_logic_vector(lramstart, 8);
    cfg(3  downto  3) := conv_std_logic_vector(mmuen, 1);
    return(cfg);
  end;

function orv(d : std_logic_vector) return std_ulogic is
variable tmp : std_ulogic;
begin
  tmp := '0';
  for i in d'range loop tmp := tmp or d(i); end loop;
  return(tmp);
end;


end;
