library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_1164.all;

-- synopsys translate_off
library std;
use STD.textio.All;
-- synopsys translate_on

package manikconfig is

    constant CONFIG_BAUD_RATE         : integer := 115200;
    constant CONFIG_COUNT_READ        : integer := 1;
    constant CONFIG_COUNT_WRITE       : integer := 0;
    constant CONFIG_RAM_ADDR_W 	      : integer := 18;
    constant CONFIG_ICACHE_ENABLED    : boolean := true;
    constant CONFIG_DCACHE_ENABLED    : boolean := true;
    constant CONFIG_ICACHE_LINE_WORDS : integer := 1;
    constant CONFIG_DCACHE_LINE_WORDS : integer := 1;
    constant CONFIG_ICACHE_ADDR_WIDTH : integer := 10;
    constant CONFIG_DCACHE_ADDR_WIDTH : integer := 10;
    constant CONFIG_UINST_WIDTH       : integer := 32;
    constant CONFIG_TIMER_WIDTH       : integer := 32;
    constant CONFIG_TIMER_CLK_DIV     : integer := 0;
    constant CONFIG_INTR_VECBASE      : integer := 16#00000000#;
    constant CONFIG_INTR_SWIVEC       : integer := 4;
    constant CONFIG_INTR_TMRVEC       : integer := 8;
    constant CONFIG_INTR_EXTVEC       : integer := 12;
    constant CONFIG_BASE_ROW          : integer := 0;
    constant CONFIG_BASE_COL          : integer := 2;
    constant CONFIG_USER_INST         : Boolean := True;
    constant CONFIG_SHIFT_SWIDTH      : integer := 3;
    constant CONFIG_MULT_BWIDTH       : integer := 32;
    constant CONFIG_HW_WPENB          : boolean := false;
    constant CONFIG_HW_BPENB 	      : boolean := false;
    constant CONFIG_FLASH_ADDR_W      : integer := 23;
    constant CONFIG_FLASH_DATA_W      : integer := 16;
    constant CONFIG_FLASH_COUNT_READ  : integer := 11;
    constant CONFIG_FLASH_COUNT_WRITE : integer := 11;
    

     constant USE_ETH  : boolean := true;
 
    -- clock related constants
    constant IN_FREQ_MHZ   : integer := 50;  -- input clock frequency in MHZ
    constant CORE_FREQ_MHZ : integer := 50;  -- core frequency in MHZ
    constant CLK_MULBY     : integer := CORE_FREQ_MHZ/5;
    constant CLK_DIVBY     : integer := IN_FREQ_MHZ/5;
    constant CLKIN_FREQ    : natural := 1000000/IN_FREQ_MHZ; 
    constant CLKIN_PERIOD  : real    := real(1000/IN_FREQ_MHZ);         
    
    -- configuration globals
    constant Technology    : string  := "XILINX";
    constant FPGA_Family : string  := "Virtex2";

--    constant Technology    : string  := "LATTICE";
    constant Lattice_Family : string := "ECP";

    constant USE_DCM       : boolean := True;
    
--    constant Technology    : string := "ALTERA";
--    constant Altera_Family : string  := "Stratix";
    constant Altera_Family : string := "Cyclone II";
    
--    constant Technology   : string := "ACTEL";
--    constant Actel_Family : string := "APA";
    constant Actel_Family : string := "APA3";

--    constant RESET_POS : boolean := false;  -- true when reset active high
    constant RESET_POS : boolean := true;  -- true when reset active high
    
    constant ADDR_WIDTH        : integer := 32;

end manikconfig;
