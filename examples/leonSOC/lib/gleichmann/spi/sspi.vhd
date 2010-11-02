library ieee;
use ieee.std_logic_1164.all;

library grlib;
use grlib.amba.all;
library gaisler;
use gaisler.misc.all;

package sspi is
  component spi_oc is
    generic (
      pindex : integer := 0;            -- Leon-Index
      paddr  : integer := 0;            -- Leon-Address
      pmask  : integer := 16#FFF#;      -- Leon-Mask
      pirq   : integer := 0             -- Leon-IRQ
      );
    port (
      rstn    : in  std_ulogic;         -- global Reset, active low
      clk     : in  std_ulogic;         -- global Clock
      apbi    : in  apb_slv_in_type;    -- APB-Input
      apbo    : out apb_slv_out_type;   -- APB-Output
      spi_in  : in  spi_in_type;        -- MultIO-Inputs
      spi_out : out spi_out_type        -- Spi-Outputs
      );
  end component spi_oc;

end package sspi;
