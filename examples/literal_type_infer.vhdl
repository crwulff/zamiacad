library ieee;
use ieee.std_logic_1164.all;

entity lti is
   port(
      low_water			 : in std_logic;
      fifo_size          : in std_logic_vector(0 to 7);
      resid_fe_len	     : in std_logic_vector(0 to 31);
      wr_cmd             : in std_logic;
      data_request_wr    : out std_logic;
      blubb	   : buffer std_logic
       );
end;

architecture rtl of lti is
begin

  data_request_wr <= wr_cmd WHEN low_water='1' OR (x"00" & fifo_size) >= resid_fe_len(0 TO 15) ELSE '0'; -- @fgat

end;

