package libcache is
  
type arr1dim is array(3 downto 0) of integer;

type arrOff is array(42 downto 39) of bit;

type arr2dim is array(3 downto 0, 3 downto 0) of integer;

type rtype is record
	bar: integer;
	mil: arr1dim;
	mil2: arr2dim;
end record;

end;

use work.libcache.all;

entity SPT is 
  port (clk : in bit;
        S   : out rtype);
end;

architecture RTL of SPT is

signal s1 : integer;
signal s2 : arr1dim;
signal s3 : arr2dim;
signal s4 : arrOff;
signal s5 : integer;

begin

   myproc: process is
      
      variable counter : integer := 0;

      procedure p1 is
      begin
         counter := counter + 1;
         if counter > 42 then
            counter := 0;
         end if;
      end procedure p1;

      procedure p2 ( a1: in integer, a2: in integer)is
      begin

      end procedure p2;

   begin
      wait until clk'event and clk='1';
      p2(a1 => 42, a2 => 23);
      p2(1,2);
      p1;

   end process;

end;

