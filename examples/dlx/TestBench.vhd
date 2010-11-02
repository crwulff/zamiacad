--  This file:            TestBench.vhd
--  All files of design:  DlxPackage.vhd, Dlx.vhd, Environment.vhd, TestBench.vhd

------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------
----                                                                                  ----
----                       Technical University of Darmstadt                          ----
----                                                                                  ----
----                         Institute of Computer Systems                            ----
----                                                                                  ----
----                                                                                  ----
----                 VHDL design of the DLX processor described in                    ----
----                   "John L. Hennessy and David A. Patterson,                      ----
----                 Computer Architecture: A Quantitative Approach"                  ----
----                                                                                  ----
----                                                                                  ----
----                              Author: Joachim Horch                               ----
----                                    June 1997                                     ----
----                                                                                  ----
------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------

library Work;
use Work.DlxPackage.all;

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_bit.all;

entity TestBench is
end TestBench;

architecture Behavior of TestBench is

  component Environment is
	port (	BusClock : in bit;
			AddressBus : in TypeWord;
			DataBus : inout TypeBidirectionalDataBus;
			ByteEnable : in unsigned( 7 downto 0 );
			TransferStart : in bit;
			WriteEnable : in bit;
			TransferError : out bit;
			TransferAcknowledge : out bit;
			CacheInhibit : out bit;
			InterruptRequest : out bit;
			ForceInterrupt : in bit );
  end component Environment;

  component DLX is
	port (	IncomingClock : in bit;
			BusClock : out bit;
			AddressBus : out TypeWord;
			DataBus : inout TypeBidirectionalDataBus;
			ByteEnable : out unsigned( 7 downto 0 );
			TransferStart : out bit;
			WriteEnable : out bit;
			TransferError : in bit;
			TransferAcknowledge : in bit;
			InterruptRequest : in bit;
			CacheInhibit : in bit;
			Reset : in bit;
			Halt : out bit );
  end component DLX;

	signal Clock : bit;
	signal BusClock : bit;
	signal AddressBus : TypeWord;
	signal DataBus : TypeBidirectionalDataBus;
	signal Reset : bit;
	signal Halt : bit;
	signal ByteEnable : unsigned( 7 downto 0 );
	signal TransferStart : bit;
	signal WriteEnable : bit;
	signal TransferError : bit;
	signal TransferAcknowledge : bit;
	signal InterruptRequest : bit;
	signal ForceInterrupt : bit;
	signal CacheInhibit : bit;

begin

	Reset <= '1', '0' after 1000 ns;
	Clock <= not Clock after 500 ns;

	ForceInterrupt <= '0', 	'1' after 80 us,
							'0' after 81 us,
							'1' after 96 us,
							'0' after 97 us;

	EnvironmentOfDLX : component Environment
    port map ( BusClock => BusClock,
	        AddressBus => AddressBus, DataBus => DataBus,
	        ByteEnable => ByteEnable, TransferStart => TransferStart, WriteEnable => WriteEnable,
	        TransferError => TransferError, TransferAcknowledge => TransferAcknowledge,
			CacheInhibit => CacheInhibit, InterruptRequest => InterruptRequest,
			ForceInterrupt => ForceInterrupt );

	Processor : component DLX
    port map ( IncomingClock => Clock, BusClock => BusClock, 
	        AddressBus => AddressBus, DataBus => DataBus,
	        ByteEnable => ByteEnable, TransferStart => TransferStart, WriteEnable => WriteEnable,
	        TransferError => TransferError, TransferAcknowledge => TransferAcknowledge,
			InterruptRequest => InterruptRequest, CacheInhibit => CacheInhibit,
			Reset => Reset, Halt => Halt );

end architecture Behavior;
