---------------------------------------------------------------------
-- TITLE: Plasma (CPU core with memory)
-- AUTHOR: Steve Rhoads (rhoadss@yahoo.com)
-- DATE CREATED: 6/4/02
-- FILENAME: plasma.vhd
-- PROJECT: Plasma CPU core
-- COPYRIGHT: Software placed into the public domain by the author.
--    Software 'as is' without warranty.  Author liable for nothing.
-- DESCRIPTION:
--    This entity combines the CPU core with memory and a UART.
---------------------------------------------------------------------
library ieee;
use ieee.std_logic_1164.all;
use work.mlite_pack.all;

entity plasma is
   port(-- the usual reset an clock stuff
        BOARD_CLK        : in std_logic;
        BOARD_RES        : in std_logic;

	-- external connections for serial interface
	BOARD_TXD        : out std_logic;
	BOARD_RTS        : out std_logic;
	BOARD_CTS        : in std_logic;
	BOARD_RXD        : in std_logic;

        -- connections to the onboard sram
        sram_we_hi       : out std_logic;
	sram_we_lo       : out std_logic;
	sram_ce_hi       : out std_logic;
	sram_ce_lo       : out std_logic;
	sram_oe_hi       : out std_logic;
	sram_oe_lo       : out std_logic;
	sram_address_hi  : out std_logic_vector(18 downto 0);
	sram_address_lo  : out std_logic_vector(18 downto 0);
	sram_data_hi     : inout std_logic_vector(15 downto 0);
	sram_data_lo     : inout std_logic_vector(15 downto 0);
	-- board leds (mainly for debug output)
        BOARD_LEDS       : out std_logic_vector(7 downto 0));
end; --entity plasma

architecture logic of plasma is
   signal reset          : std_logic;
   signal mem_address    : std_logic_vector(31 downto 0);
   signal mem_data_r     : std_logic_vector(31 downto 0);
   signal mem_data_w     : std_logic_vector(31 downto 0);
   signal mem_byte_sel   : std_logic_vector(3 downto 0);
   signal mem_write      : std_logic;
   signal mem_pause      : std_logic;
   signal sram_pause     : std_logic;
   signal uart_pause     : std_logic;
   signal ZERO           : std_logic;

begin  --architecture

   -- pause cpu?
   mem_pause <= sram_pause or uart_pause;

   -- generate a ZERO
   ZERO <= '0';

   -- the board has an inversed reset
   reset <= not BOARD_RES;

   u1_cpu: mlite_cpu 
      PORT MAP (
         clk          => BOARD_CLK,
         reset_in     => reset,
         intr_in      => ZERO,
         -- connections to memory
         mem_address  => mem_address,
         mem_data_w   => mem_data_w,
         mem_data_r   => mem_data_r,
         mem_byte_sel => mem_byte_sel,
         mem_write    => mem_write,
	 -- this pauses the cpu while waiting for i/o
         mem_pause    => mem_pause);

   u2_conv: mlite2sram
      PORT MAP (
         clk             => BOARD_CLK,
	 -- connections to cpu core
	 mem_byte_sel    => mem_byte_sel,
	 mem_write       => mem_write,
	 mem_address     => mem_address,
	 mem_data_w      => mem_data_w,
	 mem_data_r      => mem_data_r,
	 mem_pause       => sram_pause,
	 -- connections to board memory
	 sram_we_hi      => sram_we_hi,
	 sram_we_lo      => sram_we_lo,
	 sram_ce_hi      => sram_ce_hi,
	 sram_ce_lo      => sram_ce_lo,
	 sram_oe_hi      => sram_oe_hi,
	 sram_oe_lo      => sram_oe_lo,
	 sram_address_hi => sram_address_hi,
	 sram_address_lo => sram_address_lo,
	 sram_data_hi    => sram_data_hi,
	 sram_data_lo    => sram_data_lo);
	 
   u3_uart: mlite2uart
      PORT MAP (
         clk          => BOARD_CLK,
	 reset        => reset,
	 -- connections to cpu core
	 mem_address  => mem_address,
	 mem_data_w   => mem_data_w,
	 mem_data_r   => mem_data_r,
	 mem_byte_sel => mem_byte_sel,
	 mem_write    => mem_write,
	 mem_pause    => uart_pause,
	 -- connections to board uart
         TXD          => BOARD_TXD,
	 RTS          => BOARD_RTS,
         CTS          => BOARD_CTS,
	 RXD          => BOARD_RXD,
	 -- debugging leds
	 dbg          => BOARD_LEDS);

end; --architecture logic
