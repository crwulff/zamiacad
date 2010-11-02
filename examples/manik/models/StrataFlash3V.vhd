
------------------------------------------------------------------------
--                  Intel Proprietary
--                  Copyright 2000 Intel
--                  All rights reserved
------------------------------------------------------------------------
-- VHDL Code Name    : EclipseCR_128MB_eas.vhd
-- Date Updated      : May 30, 2002, rev22
-- Author            : Eric Magnusson
------------------------------------------------------------------------
-- Description : basic package for EAS parameters, for global use
-- This eas file contains specifications for J3 flash.
------------------------------------------------------------------------


library ieee;
library work;
use ieee.std_logic_1164.all;
use std.textio.all;



package eas_parameters is

--==========================================================================
--==   Set device Density    1 = 32Mb    2 = 64Mb     3 = 128Mb  
--==========================================================================
CONSTANT density : INTEGER := 3;


--The initialization file must be named "initfile.dat" and have the following 
--format: 6 Hex address charactors, a space and 4 hex data charactors (word aligned)
--The first line of the file is ignored
--There must NOT be any white spaces at the end of the file
--Example: (ignoring the "--")
--
--AAAAAA BBBB
--EEEEEE CCCC
--  This would program BBBB at address AAAAAAh and CCCC at address EEEEEEh

-- To use the array initialization file, this variable MUST be TRUE,
-- if you do not wish to utilize the initialization file, it must be FALSE

  CONSTANT FileInitialize: BOOLEAN := False;

  --==========================================================================
  --=   Set BFM spec. timings  0 = Device Timings    1 = Short Timings
  --= WARNING: SETTING FAST TIMINGS WILL NOT ACCURATELY REPRESENT DEVICE SPECIFICATIONS
  --==========================================================================
  Constant FastTimes : INTEGER :=  1;       -- speeds up Program & Erase Times for simulation

  Constant Verbose   : BOOLEAN :=  false;   -- enables MUCH state output
  CONSTANT PollingDelay  : TIME    := 0.5 ns; 


--==========================================================================
-----------------------------------------------------------------------------
-- Voltage settings
-----------------------------------------------------------------------------
  -- switch to logic
  CONSTANT Vhh            : std_logic := 'H';
  CONSTANT Vil            : std_logic := '0';
  CONSTANT VilMax         : real      := 0.2; 
  CONSTANT Vih            : std_logic := '1';
  CONSTANT VihMin         : real      := 2.5; 
  CONSTANT Vpenlk	  : real      := 2.0; 

-- These are the voltage ranges for 3 volt VCC specs.
  CONSTANT Vcc1Min      : REAL  := 2.7; 
  CONSTANT Vcc1Max      : REAL  := 3.6; 
  
-- These are the voltage ranges for VCCQ specs.
-- All spec are included but VccqMin and Max covers the whole range
  CONSTANT Vccq1Min      : REAL  := 2.7;
  CONSTANT Vccq1Max      : REAL  := 3.6;
  CONSTANT Vccq2Min      : REAL  := 2.7;  
  CONSTANT Vccq2Max      : REAL  := 3.6;  
  CONSTANT Vccqlko       : REAL  := 1.30;  -- QN: Vccq lock out voltage (1/10)

-- block locking voltages
  CONSTANT VhhMin        : REAL  := 11.4;  
  CONSTANT VhhMax        : REAL  := 12.6;  

-- VCC Lock out voltages
  CONSTANT Vlko1        : REAL  := 2.0;  --Vcc Lockout 
  CONSTANT Vlko2        : REAL  := 1.3;  --Vccq Lockout 
  CONSTANT Vpplko       : REAL  := 1.3; -- Vpen Lockout for K3 

-- These are the voltage ranges for 3 and 12 volt VPP specs.
-- Commented ones are left in there for later use if needed
  CONSTANT VpplMin      : REAL  := 0.9;  
  CONSTANT VpplMax      : REAL  := 1.95;  
  CONSTANT VpphMin      : REAL  := 11.4; 
  CONSTANT VpphMax      : REAL  := 12.6; 
  CONSTANT VpenMin      : REAL  := 2.7;
  CONSTANT VpenMax      : REAL  := 3.6;

-----------------------------------------------------------------------------
-- Read Characteristics
-----------------------------------------------------------------------------
  --- Asynchronous Specifications
  TYPE density_time IS ARRAY(1 TO 3) OF TIME;     -- DO NOT CHANGE
  CONSTANT ReadAccessTimes : density_time := ( 110 ns, 120 ns, 150 ns);
  CONSTANT RSTOutputTimes  : density_time := ( 150 ns, 180 ns, 210 ns);

  CONSTANT tAVAV    : Time := ReadAccessTimes(density);	-- Read cycle time           R1 -- Density
  CONSTANT tAVQV    : Time := ReadAccessTimes(density); 	-- Address to output delay   R2 -- Density
  CONSTANT tELQV    : Time := ReadAccessTimes(density);	-- CE# low to output delay   R3 -- Density
  CONSTANT tPHQV    : Time := RSTOutputTimes(density);	-- RST# high to output delay R5 -- Density

  CONSTANT tGLQV    : Time := 25 ns;	-- OE# low to output delay 		R4/R16
  CONSTANT tELQX    : Time := 0 ns;	-- CE# low to output in low-Z 		R6
  CONSTANT tGLQX    : Time := 0 ns;	-- OE# low to output in low-Z 		R7
  CONSTANT tEHQZ    : Time := 55 ns;	-- CE# high to output in high-Z 	R8
  CONSTANT tGHQZ    : Time := 15 ns;	-- OE# high to output in high-Z	R9
  CONSTANT tOH      : Time := 0 ns;	-- Output hold from first ocurring	R10
					-- addres, CE# or OE# change
					
  CONSTANT tEHEL    : Time := 0 ns;	-- CE# high to CE# low			R11
  CONSTANT tELTL    : Time := 25 ns;	-- CE# low to WAIT low			R12
  CONSTANT tAPA     : time := 25 ns;	-- Page Address Access			R108

  constant tELFL    : time := 10 ns;  -- CEx Low to BYTE# Low / High  R13
  constant tFLQV    : time := 1000 ns;  -- BYTEx to Output  R14

  
-----------------------------------------------------------------------------
-- Write Characteristics
-----------------------------------------------------------------------------  
  constant tPHWL   : time := 1 us;	-- RST# high recovery to WE# low		W1

  constant tELWL   : time := 0 ns;	-- CE#(WE#) setup to WE#(CE#) low		W2
  constant tWLEL   : time := tELWL;

  constant tWLWH   : time := 70 ns; 	-- CE#(WE#) write pulse width low		W3

  constant tDVWH   : time := 50 ns;	-- Data setup to WE#(CE#) going high		W4
  constant tDVEH   : time := tDVWH;
 
  constant tAVWH   : time := 55 ns;	-- Address setup to WE#(CE#) high		W5
  constant tAVEH   : time := tAVWH;

  constant tWHEH   : time := 0 ns;      -- CE#(WE#) hold from WE#(CE#) high 		W6
  constant tEHWH   : time := tWHEH;

  constant tWHDX   : time := 0 ns;      -- Data hold from WE#(CE#) high			W7
  constant tEHDX   : time := tWHDX;

  constant tWHAX   : time := 0 ns;      -- Address hold from WE#(CE#) high		W8
  constant tEHAX   : time := tWHAX;

  constant tWHWL   : time := 30 ns;	-- WE# pulse width high				W9 

  constant tVPWH   : time := 0 ns;	-- Vpen setup to WE#(CE#) going high		W10
  constant tVPEH   : time := tVPWH;

  constant tQVVL   : time := 0 ns;	-- Vpen hold from valid SRD, STS high		W11

  constant tWHGL   : time := 35 ns;	-- Write recovery before read			W14
  constant tEHGL   : time := tWHGL;

  constant tWHQV   : time := tAVQV + 40 ns;	-- WE# high to data valid		W16 
  constant tEHQV   : time := tWHQV;

  constant tWHRL   : time :=500 ns; 	-- WE#(CE#) high to STS going low		W17/W13
  constant tEHRL   : time :=tWHRL;


-------------------------------------------------------------
-- Block Erase / Program / Lock Timings
-------------------------------------------------------------
  TYPE fastTime_type IS ARRAY(0 TO 1) OF TIME;     -- DO NOT CHANGE
  constant tSTS	    : time := 250 ns; --STS Config time

  CONSTANT ProgWordTimes   : fastTime_type := ( 210 us, 20 us);
  constant tWHQV1_2 : time := ProgWordTimes(FastTimes); --Program Word Time 3V 

  CONSTANT BuffProgTimes   : fastTime_type := ( 218 us, 21 us);
  constant tWHQV3_3 : time := BuffProgTimes(FastTimes); -- Buffered Program

  CONSTANT EraseTimes      : fastTime_type := ( 1000 ms, 100 us);
  constant tWHQV3_2 : time := EraseTimes(FastTimes); --Erase Main Block Time 3v

  CONSTANT PSuspTimes    : fastTime_type := ( 25 us, 2 us);
  constant tWHRH1_2 : time := PSuspTimes(FastTimes);   --Program Suspend Latency 3v 

  CONSTANT ESuspTimes    : fastTime_type := ( 26 us, 3 us);
  constant tWHRH2_2 : time := ESuspTimes(FastTimes);   --Erase Suspend Latency 3v 

  CONSTANT SetLockTimes    : fastTime_type := ( 64 us, 10 us);
  constant tWHQV5   : time := SetLockTimes(FastTimes);  --Set lock bit Time 3V  

  CONSTANT ClearLockTimes  : fastTime_type := ( 500 ms, 50 us);
  constant tWHQV6   : time := ClearLockTimes(FastTimes);  --Clear lock bit Time 3V  

--============================================================================
--============================================================================
-- PARAMETERS FOR BFM OPERATION
-- 
-- DO NOT CHANGE ANY PARAMETERS BELOW THIS LINE
--============================================================================
--============================================================================

-------------------------------------------------------------
-- Unapplicable Timings
-------------------------------------------------------------
  --- Latching Specifications
  CONSTANT tAVVH    : Time := 7 ns; 	-- Address setup to ADV# High 		R101
  CONSTANT tELVH    : time := 7 ns;	-- CE# low to ADV# high			R102  
  CONSTANT tVLQV    : Time := 110 ns;	-- ADV# low to output delay		R103 
  CONSTANT tVLVH    : time := 10 ns;	-- ADV# Pulse Width low			R104
  CONSTANT tVHVL    : Time := 10 ns;	-- ADV# pulse width high		R105
  CONSTANT tVHAX    : time := 8 ns;	-- Address hold from ADV# high		R106
  --- Clock Specifications
  CONSTANT tCLK     : time := 15 ns;	-- CLK period				R201
--CONSTANT tVHGL    : time := 20 ns;
  --- Synchronous Specifications
  CONSTANT tAVCH    : Time := 7 ns;	-- Address valid setup to CLK		R301
  CONSTANT tVLCH    : Time := 7 ns;	-- ADV# low setup to CLK		R302
  CONSTANT tELCH    : Time := 7 ns;	-- CE# low setup to CLK			R303 
  CONSTANT tCHQV    : time := 13 ns;	-- CLK to output delay			R304
  CONSTANT tCHQX    : time := 5 ns;	-- Output hold from CLK			R305
  CONSTANT tCHAX    : time := 8 ns;	-- Address hold from CLK		R306
  CONSTANT tCHTLH   : time := 13 ns;	-- CLK to WAIT delay			R307

  constant tQVBL   : time := 0 ns;	-- WP# hold from Status read			W12
  constant tBHWH   : time := 200 ns;	-- WP# setup to WE# high			W13
  constant tWHQV2_2 : time := 300000 us; --Erase Parameter Block Time 3(DOES NOT APPLY)
  constant tEFPLat1 : time := 1500 ns; --EFP Ramp times(Does not Apply for K3)
  constant tEFPLat2 : time := 320 us; --EFP Program Time(Made equal to the Buffered program time)
  constant tEFPLat3 : time := 0.5 us; --EFP Verify Time(Does not Apply for K3)

-------------------------------------------------------------
-- Data types
-------------------------------------------------------------
  SUBTYPE BYTE           IS std_logic_vector (7 downto 0);
  SUBTYPE WORD           IS std_logic_vector (15 downto 0);
  SUBTYPE StateType      IS std_logic_vector (6 downto 0);

-- THE FOLLOWING ARE NOT USER CONFIGURABLE
  TYPE density_int  IS ARRAY(1 TO 3) OF INTEGER;   -- DO NOT CHANGE
  CONSTANT AddrLineRange : density_int := ( 21, 22, 23);  -- Max address line (ext., byte aligned)
  CONSTANT MaxAddrRange  : density_int := ( 16#1FFFFF#, 16#3FFFFF#, 16#7FFFFF#);  -- Max address line (ext., byte aligned)
  CONSTANT MaxBlockRange : density_int := ( 31, 63, 127);  -- Max block number

   -- MaxAddrLine is the number of the highest-order address line, when the 
   -- byte-order line is numbered A0.  
  CONSTANT MaxAddrLine_x  : INTEGER := AddrLineRange(density);
  CONSTANT MaxAddress     : INTEGER := MaxAddrRange(density);
  CONSTANT MaxBlockNum    : INTEGER := MaxBlockRange(density);
  CONSTANT DataBusWidth   : INTEGER := 16;          -- X16 device
  CONSTANT MaxDataLine    : INTEGER := DataBusWidth - 1;          -- Maximum data line
  CONSTANT ParamBlockSize : INTEGER := 16#1#;                     -- Parameter block size = 4KW
  CONSTANT MainBlockSize  : INTEGER := 16#10000#;                 -- Main Block Size = 64KW
  CONSTANT BlkAddrDecodeBit : INTEGER := 16;                      -- 2**BlkAddrDecodeBit = MainBlockSize -- sq 2/6/01 
  CONSTANT PartitionSize  : INTEGER := 16#FFFFFFF#; 
  CONSTANT ParamBlockNum  : INTEGER := 0;                         -- Number of Parameter blocks
  CONSTANT MainBlockNum   : INTEGER := MaxBlockNum;               -- Number of Main blocks
  CONSTANT MaxAddrLine    : INTEGER := 23;         -- Maximum address line (internal, word aligned)

  CONSTANT MaxPartitionNum: INTEGER := 0;                         -- Maximum partition num 
  CONSTANT TopBoot        : INTEGER := 2;                         -- 0 => bottom boot; 1 => top boot; 2 => No boot  
  CONSTANT ReadWhileWrite : BIT     :='0';                        -- '0' signifies non-RWW
  CONSTANT PageSize       : INTEGER := 4;                         -- 4 word page for ECR  
 
  CONSTANT SyncEnable     : INTEGER := 0;                         -- Disable Synchronous reads  ECR
  CONSTANT MaxCRSize      : INTEGER := 15;                        -- Size of Config Register
  CONSTANT MaxSRSize      : INTEGER := 7;                         -- Size of Status Register
  CONSTANT ManufacturerID : INTEGER := 16#89#;
  CONSTANT DeviceIDrange  : density_int := ( 16#16#, 16#17#, 16#18#);
  CONSTANT DeviceID       : INTEGER := DeviceIDrange(density);

  CONSTANT MaxOTPAddr  	  : INTEGER := 16#88#;                    --16#109#;  ECR
  CONSTANT MinOTPAddr     : INTEGER := 16#89#;                    -- ECR
  CONSTANT MaxOTPBlockLine: INTEGER := 17;                        -- For OTP Lock bit Array Size
  CONSTANT WordPerOTPBlock: INTEGER := 16#8#;

  CONSTANT MaxBuffSize 	  : INTEGER := 16;                        -- ECR
  CONSTANT No_ofAddrLine  : INTEGER := 4;                         -- 2**No_ofAddrLine = MaxBuffSize sq 2/6/01 ECR
  CONSTANT MaxBuffLine    : INTEGER := 15;                        -- ECR
 
  CONSTANT MaxIdLine      : INTEGER := 3;                         -- Maximum ID line for FWH
  CONSTANT MaxLadLine     : INTEGER := 3;                         -- Maximum Lad line for FWH
  CONSTANT MaxAddrCycleNum: INTEGER := 6;                         -- There are 7 address cycles for FWH
  CONSTANT MaxDataCycleNum: INTEGER := 1;                         -- There are 2 data cycles for FWH
  CONSTANT MaxTargetCycleNum : INTEGER := 1;                      -- There are 2 target cycles for FWH
  CONSTANT MaxWaitCycleNum: INTEGER := 2;                         -- There are 3 wait cycles for FWH
  
  CONSTANT MinLatency	  : INTEGER := 2;                         -- Minimum Allowable Latency in the CR
  CONSTANT MaxLatency	  : INTEGER := 10;                        -- Maximum Allowable Latency in the CR
  CONSTANT Multicycledatahold 	: Integer := 2;                   -- Number of clocks data could be held
  CONSTANT Barrelshifterwidth 	: Integer := 16;                  -- Width of the Barrelshifter-for EOWL conditions
  CONSTANT Wrap_Around_MaxAddress : Bit := '0';                   -- Parameter for bursting after Maximum addressable space
  CONSTANT ldenable : Boolean := FALSE;                   

-- THESE COMMANDS FORM THE INTEL STANDARD COMMAND SET
        -- Read commands
  CONSTANT ReadArrayCmd       : INTEGER := 16#FF# ;
  CONSTANT ReadIDCmd          : INTEGER := 16#90# ;  
  CONSTANT ReadQueryCmd       : INTEGER := 16#98# ;
  CONSTANT ReadSRCmd          : INTEGER := 16#70# ;
  CONSTANT ClearSRCmd         : INTEGER := 16#50# ;
	-- Program commands  
  CONSTANT ProgramCmd         : INTEGER := 16#40# ;
  CONSTANT Program2Cmd        : INTEGER := 16#10# ;
  CONSTANT Program2BuffCmd    : INTEGER := 16#E8# ;
  CONSTANT ProgramEFPCmd      : INTEGER := 16#30# ;
  CONSTANT ProgramBEFPCmd     : INTEGER := 16#80# ;

	-- Erase commands  
  CONSTANT EraseBlockCmd      : INTEGER := 16#20# ;
  CONSTANT PartitionEraseCmd  : INTEGER := 16#A0# ;
  CONSTANT ConfirmCmd         : INTEGER := 16#D0# ;
        -- Suspend/Resume commands
  CONSTANT SuspendCmd         : INTEGER := 16#B0# ;	  
  CONSTANT ResumeCmd          : INTEGER := 16#D0# ;  
	-- Set Config Register commands
  CONSTANT ConfigLockSetupCmd : INTEGER := 16#60# ;
  CONSTANT ConfigWriteCmd     : INTEGER := 16#3# ;
        -- STS Configuration Commands
  CONSTANT STSConfigSetupCmd  : INTEGER := 16#B8# ;
  CONSTANT STSConfigCode00Cmd : INTEGER := 16#00# ;
  CONSTANT STSConfigCode01Cmd : INTEGER := 16#01# ;
  CONSTANT STSConfigCode10Cmd : INTEGER := 16#02# ;
  CONSTANT STSConfigCode11Cmd : INTEGER := 16#03# ;
        -- Block Locking commands
  CONSTANT BlockLockCmd       : INTEGER := 16#01# ;
  CONSTANT BlockUnlockCmd     : INTEGER := 16#D0# ;
  CONSTANT BlockLockDownCmd   : INTEGER := 16#2f# ;
  CONSTANT BlockUnlockDownCmd : INTEGER := 16#2E# ;--Added on 01/09/2001 - RRD
  CONSTANT EclBlockSetCmd     : INTEGER := 16#01# ; -- ECR
  CONSTANT EclBlockClearCmd   : INTEGER := 16#D0# ; -- ECR
  CONSTANT EclBlockSetMstrLkCmd   : INTEGER := 16#F1# ; -- ECR
        -- Protection Regisger commands
  CONSTANT ReadPRCmd          : INTEGER := 16#90# ;
  CONSTANT ProgramPRCmd       : INTEGER := 16#C0# ;
  CONSTANT LockPR0Cmd         : INTEGER := 16#FFFD# ;
  CONSTANT OpenRegisterCmd    : INTEGER := 16#91# ;
  CONSTANT CloseRegisterCmd   : INTEGER := 16#ff# ; 

   
  -- This command is specific to the burst flash device.
  CONSTANT ReadRCRCmd         : INTEGER := 16#60# ;
  CONSTANT ReadRCRCmd2        : INTEGER := 16#03# ;

-- THESE ARE VALUES FOR THE WriteType and WUR_Operation SIGNAL 
  CONSTANT WriteCmd           : INTEGER := 0;
  CONSTANT WriteIdle          : INTEGER := 0;
  CONSTANT WriteProgram       : INTEGER := 1;
  CONSTANT WriteBlockErase    : INTEGER := 2;
  CONSTANT WriteProtectionReg : INTEGER := 3;
  CONSTANT WriteOTP           : INTEGER := 4;
  CONSTANT WritePartitionErase: INTEGER := 5;
  CONSTANT WriteProgramEFP    : INTEGER := 6;
  CONSTANT WriteEFPData       : INTEGER := 7;  
  CONSTANT WriteSuspend       : INTEGER := 8;
  CONSTANT WriteEFPVerify     : INTEGER := 9;  
  CONSTANT WriteBlockLock     : INTEGER :=10; 
  CONSTANT WriteLockDown      : INTEGER :=11;
  CONSTANT WriteClearSR       : INTEGER :=12;
  CONSTANT WriteBuffer        : INTEGER :=13; 
  CONSTANT WriteRdConfigLkSetup: INTEGER := 14;
  CONSTANT WriteUnlock        : INTEGER := 15;
  CONSTANT WriteConfigReg     : INTEGER :=16;
  CONSTANT WriteProgramBEFP   : INTEGER := 17;
  CONSTANT WriteSTSconfig     : INTEGER := 18;
  CONSTANT WriteBufferData    : INTEGER := 19;
  CONSTANT WriteSRBotch       : INTEGER := 20;
  CONSTANT WriteBufferConfirm : INTEGER := 21;
  CONSTANT WriteBufferBotch   : INTEGER := 22;
  CONSTANT WriteBEFP          : INTEGER := 23;
 --  CONSTANT WriteBEFPConfirm   : INTEGER := 24;
  CONSTANT WriteBEFPData      : INTEGER := 25;
  CONSTANT WriteBufferProg    : INTEGER := 13;
  CONSTANT WriteRegisterSpace : INTEGER := 26;
  CONSTANT WriteUnlockDown      : INTEGER :=24;

  CONSTANT WriteSetBlockLock  : INTEGER := 27;   
  CONSTANT WriteClearBlockLock : INTEGER := 28; 
  CONSTANT WriteSetMasterLock  : INTEGER := 29;
 
  CONSTANT MasterLockIndex  : INTEGER := 0;      --ECR index into lockdown() to store master lock bit

   
  
  
-- THESE VALUES ARE FOR THE WUR_Running SIGNAL FROM THE WSM MODULE
-- ALL POSSIBLE OPERATIONS OF THE WSM AT ANY INSTANCE OF TIME
-- SHOULD BE LISTED HERE
  CONSTANT  ProgramSetup   : INTEGER := 1;
  CONSTANT  Programing     : INTEGER := 2;
  CONSTANT  EraseSetup     : INTEGER := 3;
  CONSTANT  BlockErasing   : INTEGER := 4;
  CONSTANT  PRProgram      : INTEGER := 5;
  CONSTANT  OTPProgram     : INTEGER := 6;
  CONSTANT  PartitionErase : INTEGER := 7;
  CONSTANT  EFP_Program    : INTEGER := 8;
  CONSTANT  EFP_Data       : INTEGER := 9;
  CONSTANT  EFP_Verify     : INTEGER := 10;
  CONSTANT  Susp_Latency   : INTEGER := 11;
  CONSTANT  BufferWriting  : INTEGER := 12;
  CONSTANT  Suspended      : INTEGER := 14;
  CONSTANT  BEFPWriting    : INTEGER := 15;
  CONSTANT  WSM_Idle       : INTEGER := 0;
  CONSTANT  Data_Streaming : INTEGER := 16;
  CONSTANT  BufferCountLoading :  INTEGER := 17;       -- QN:  Added to accomodate change on XSR 1/15/01
  CONSTANT  LockClearing   : INTEGER := 18;
  CONSTANT  LockSetting    : INTEGER := 19;

    
-- THESE CONSTANTS ARE VALUES OF THE STATUS REGISTER 
-- AT DIFFERENT MODES
  CONSTANT SRReady        : BYTE := "10000000";
  CONSTANT SREraseSusp    : BYTE := "01000000";
  CONSTANT SREraseFail    : BYTE := "00100000";
  CONSTANT SRProgramFail  : BYTE := "00010000";
  CONSTANT SRVppStatError : BYTE := "00001000";
  CONSTANT SRProgramSusp  : BYTE := "00000100";
  CONSTANT SRDeviceProtect: BYTE := "00000010"; --Block Lock Status=BLS
  CONSTANT SRPartition    : BYTE := "00000000"; --dummy anchor
  CONSTANT SRDeviceSusp   : BYTE := "01000100";
  CONSTANT ClearStatReg   : BYTE := "11000101";
  CONSTANT ForceClearMask : BYTE := "11100001";
  CONSTANT SRBotch        : BYTE := "00110000";
  CONSTANT ZERO           : BYTE := "00000000";
  CONSTANT SRReadySusp    : BYTE := "10000100";
  CONSTANT SREFPReady     : BYTE := "00000000";
  CONSTANT SREFPBusy      : BYTE := "00000001";
  CONSTANT XSRBuffFree	  : BYTE := "10000000";
  CONSTANT XSRBuffNotFree : BYTE := "00000000";

-- THESE VALUES ARE FOR THE UR_ReadType SIGNAL
  CONSTANT ReadArray     : INTEGER := 1;
  CONSTANT ReadIDCodes   : INTEGER := 2;
  CONSTANT ReadStatusReg : INTEGER := 3;
  CONSTANT ReadQuery     : INTEGER := 4; 


------------------------------------------------------------------------------------------
-- These are the values for state and new-state.  They help us keep track of 
-- what's happening on the bus, and what sigs and transitions we're looking at.
------------------------------------------------------------------------------------------
  constant PowerUp        : INTEGER := 1;
  constant CEActive       : INTEGER := 2;
  constant PowerDown      : INTEGER := 3;
  constant ReadCycleZ     : INTEGER := 4;
  constant ReadCycleXAsync: INTEGER := 5;
  constant ReadCycleVAsync: INTEGER := 6;
  constant ReadCycleX     : INTEGER := 7;
  constant ReadCycleV     : INTEGER := 8;
  constant WriteCycle     : INTEGER := 9;
  constant WEActive       : INTEGER := 10;
  constant ReadCycleXSync : INTEGER := 11;
  constant ReadCycleVSync : INTEGER := 12;
  constant DeepPowerDown  : INTEGER := 13;
  constant InputDisable   : INTEGER := 14;
--------------------------------------------------------------------------------
-- states for testmode
--------------------------------------------------------------------------------
-- Initial state
  CONSTANT Idle     : StateType := "0000000";
  CONSTANT TargetMainArray: Integer := 1;
  CONSTANT TargetOTP      : Integer := 2;
  CONSTANT DQX :std_logic_vector(MaxDataLine downto 0) := "XXXXXXXXXXXXXXXX";
  CONSTANT DQZ :std_logic_vector(MaxDataLine downto 0) := "ZZZZZZZZZZZZZZZZ";
 

---------------------------------------------------------------------
-- commented out to simplify data structure for both modes
---------------------------------------------------------------------
   TYPE  BlockStructType IS ARRAY(0 TO MaxBlockNum, 0 TO 3) OF INTEGER;
  

----------------------------------------------------------------------------
-- This table exists to calculate the Intel burst offsets on the fly.
----------------------------------------------------------------------------
  TYPE BurstTableType IS ARRAY (0 TO 15, 0 TO 15) OF INTEGER;
   CONSTANT IntelBurst : BurstTableType :=
    ((0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
     (1, 0, 3, 2, 5, 4, 7, 6, 9, 8, 11, 10, 13, 12, 15, 14),
     (2, 3, 0, 1, 6, 7, 4, 5, 10, 11, 8, 9, 14, 15, 12, 13),
     (3, 2, 1, 0, 7, 6, 5, 4, 11, 10, 9, 8, 15, 14, 13, 12),
     (4, 5, 6, 7, 0, 1, 2, 3, 12, 13, 14, 15, 8, 9, 10, 11),
     (5, 4, 7, 6, 1, 0, 3, 2, 13, 12, 15, 14, 9, 8, 11, 10),
     (6, 7, 4, 5, 2, 3, 0, 1, 14, 15, 12, 13, 10, 11, 8, 9),
     (7, 6, 5, 4, 3, 2, 1, 0, 15, 14, 13, 12, 11, 10, 9, 8),
     (8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7),
     (9, 8, 11, 10, 13, 12, 15, 14, 1, 0, 3, 2, 5, 4, 7, 6),
     (10, 11, 8, 9, 14, 15, 12, 13, 2, 3, 0, 1, 6, 7, 4, 5),
     (11, 10, 9, 8, 15, 14, 13, 12, 3, 2, 1, 0, 7, 6, 5, 4),
     (12, 13, 14, 15, 8, 9, 10, 11, 4, 5, 6, 7, 0, 1, 2, 3),
     (13, 12, 15, 14, 9, 8, 11, 10, 5, 4, 7, 6, 1, 0, 3, 2),
     (14, 15, 12, 13, 10, 11, 8, 9, 6, 7, 4, 5, 2, 3, 0, 1),
     (15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0));

----------------------------------------------------------------------------
-- memory array
----------------------------------------------------------------------------
  TYPE element_rec;                                    --  Incomplete Type to be memory array
  TYPE element_ptr IS ACCESS element_rec;              --  Pointer to element_rec
  TYPE element_rec IS                              --  Record of element_rec to hold data, address
    RECORD                                             --  and a pointer to the last stored value
      data    :  word;                                 --  This creates a record with the variable
      FACSdata    :  std_logic_vector(17 downto 0);   --  This creates a record with the variable
      address : integer;                               --  nxt always pointing to the element_rec
      nxt     :  element_ptr;                          --  that was previously stored
    END RECORD;


----------------------------------------------------------------------------
  --  Stores Lock Block Info =1 if block is locked
----------------------------------------------------------------------------
  TYPE LockConfigType IS ARRAY(MaxBlockNum downto 0) OF BIT;
 
----------------------------------------------------------------------------
  --  Store Lock Down Info =1 if block is locked down
----------------------------------------------------------------------------
  TYPE LockDownType   IS ARRAY(MaxBlockNum downto 0) OF BIT;
  
 ----------------------------------------------------------------------------
  --  Store Unlock Down Info =1 if block is Unlocked down
----------------------------------------------------------------------------
  TYPE UnlockDownType IS ARRAY(MaxBlockNum downto 0) OF BIT; 
 
----------------------------------------------------------------------------
  --  Stores OTP value
----------------------------------------------------------------------------
  TYPE PRType IS ARRAY(136 downto 128) OF word;
  TYPE OTPType IS ARRAY(3 downto 0) OF word;
 
----------------------------------------------------------------------------
  --  Records which blocks have been OTP'ed
----------------------------------------------------------------------------
  TYPE OTP_LockType IS ARRAY(3 downto 0) OF BOOLEAN;
  TYPE OTP_LockArray IS ARRAY(MaxOTPBlockLine downto 0) OF Bit;

-- MaxPartitionNum is 0 and we can't easily translate this to verilog -- sq
-- TYPE ReadTypeArray IS Array(MaxPartitionNum downto 0) OF INTEGER; sq 1/30/01
-- ReadTypeArray could've been defined as Integer, but that required more edits in other files --sq
-- SUBTYPE ReadTypeArray IS INTEGER range 0 to integer'high;

TYPE linkedarray_type IS ARRAY(0 TO MaxBlockNum) OF element_ptr;

TYPE LowZtype IS ARRAY (MaxDataLine downto 0) OF std_logic;
TYPE HighZtype IS ARRAY (MaxDataLine downto 0) OF std_logic;
TYPE AddrBuffer IS ARRAY (0 to MaxBuffLine) OF std_logic_vector (MaxAddrLine downto 0);
TYPE DataBuffer IS ARRAY (0 to MaxBuffLine) OF std_logic_vector (MaxDataLine downto 0);
end eas_parameters;

library ieee;
library work;

use std.textio.all;
use ieee.std_logic_1164.all;
use work.eas_parameters.all;

entity StrataFlash3V is 
   port (
	A0     : in std_logic;
	A      : in std_logic_vector(MaxAddrLine_x downto 1);
        DQ     : inout std_logic_vector(MaxDataLine downto 0);
	CEb    : in std_logic;  
	OEb    : in std_logic;  
      	WEb    : in std_logic;  
        RPb    : in std_logic;  
	BYTEb  : in std_logic;  
	RPb_a  : in real;  
	VCC    : in real;
	VCCQ   : in real;
    	VPEN   : in real;
        GND    : in real;
	VSSQ   : in real; 
	STS    : out Bit
	      );
end StrataFlash3V;


--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX


LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE std.textio.ALL;


PACKAGE io_pkg IS

	CONSTANT var_172      : integer := 80;

	SUBTYPE var_258  IS std_logic_vector(255 DOWNTO 0);
	SUBTYPE var_254  IS std_logic_vector(127 DOWNTO 0);
	SUBTYPE var_259   IS std_logic_vector(31 DOWNTO 0);
	SUBTYPE var_257   IS std_logic_vector(23 DOWNTO 0);
	SUBTYPE var_255   IS std_logic_vector(15 DOWNTO 0);
	SUBTYPE var_260    IS std_logic_vector(7 DOWNTO 0);
	SUBTYPE slv4    IS std_logic_vector(3 DOWNTO 0);
	SUBTYPE var_256    IS std_logic_vector(1 DOWNTO 0);

	TYPE lput_radix_t IS (var_37, var_189, var_82, hex);
	SUBTYPE hex_t IS integer RANGE 0 TO 15;

        FILE var_199   : text IS OUT "STD_OUTPUT"; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXX
   PROCEDURE sysreset (
      CONSTANT t        : IN integer;
      SIGNAL var_232        : INOUT std_logic
      );

	PROCEDURE echoint (
		CONSTANT str		: IN integer
	);

	PROCEDURE echo (
		CONSTANT str 		: IN string
	);

	PROCEDURE echont (
		CONSTANT str 		: IN string
	);
	PROCEDURE compare_bv(
		var_10				: IN std_logic_vector;
		var_113 				: IN std_logic_vector
	);

   PROCEDURE compare_bv32(
      var_10            : IN std_logic_vector;
      var_113           : IN std_logic_vector
   );


	FUNCTION vtoh (
		CONSTANT v 		: IN std_logic_vector
	) RETURN string;


--XXXXXXXXXXXXXXXXXXX
	
	FUNCTION ctov4 (
		CONSTANT c		: IN character
	) RETURN slv4;

	FUNCTION ctob (
		CONSTANT s			: IN character
	) RETURN std_logic;
	
--XXXXXXXXXXXXXXXXXXXX
	FUNCTION vtoi (
		CONSTANT v		: IN std_logic_vector
	) RETURN integer;
--XXXXXXXXXXXXXXXXXXXX
	FUNCTION htov (
		CONSTANT s	: IN string;
		l 				: integer
	) RETURN std_logic_vector;

	FUNCTION htoi (
		CONSTANT s			: IN string
	) RETURN integer;

	FUNCTION ctoi (
		CONSTANT c			: IN character
	) RETURN integer;

	FUNCTION itov (
		CONSTANT int		: IN integer;
		l    					: integer
	) RETURN std_logic_vector;

--XXXXXXXXXXXXXXXXXXXXX
	FUNCTION tochar (
		CONSTANT x			: std_logic
	) RETURN character;

	FUNCTION tochar (
		CONSTANT x			: std_logic_vector
	) RETURN character;

	FUNCTION tochar (
		CONSTANT x			: hex_t
	) RETURN character;

	PROCEDURE lput ( 
		VARIABLE l			: INOUT line;
		CONSTANT x			: IN std_logic;
		CONSTANT b			: IN lput_radix_t := var_37
	);

	PROCEDURE lput ( 
		VARIABLE l			: INOUT line;
		CONSTANT x			: IN std_logic_vector;
		CONSTANT b			: IN lput_radix_t := var_37
	);

	PROCEDURE lput ( 
		VARIABLE l			: INOUT line;
		CONSTANT x			: IN integer;
		CONSTANT b			: IN lput_radix_t := var_37
	);

	FUNCTION compare (	
		CONSTANT vectora	: IN std_logic;
		CONSTANT var_347	: IN std_logic
	) RETURN boolean;

 	FUNCTION compare (	
 		CONSTANT vectora		: IN std_logic_vector;
 		CONSTANT var_347		: IN std_logic_vector
 	) RETURN boolean;

	PROCEDURE data_fail (
		CONSTANT var_391			: IN string;
		CONSTANT data			: IN std_logic;
		CONSTANT var_112 		: IN std_logic;
		var_116    				: IN boolean
	);

	PROCEDURE data_fail (
		CONSTANT var_391			: IN string;
		CONSTANT data			: IN std_logic_vector;
		CONSTANT var_112 		: IN std_logic_vector;
		var_116    				: IN boolean
	);

	PROCEDURE data_fail (
		CONSTANT var_391		: IN string;
		CONSTANT addr		: IN std_logic_vector;
		CONSTANT data		: IN std_logic_vector;
		CONSTANT var_112 	: IN std_logic_vector;
		var_116    			: IN boolean
	);

	PROCEDURE int2string (
		CONSTANT v 				: IN integer; 
		VARIABLE t 				: INOUT string
	);

        PROCEDURE int2word    (
                CONSTANT v                              : IN integer;
                VARIABLE t              : INOUT std_logic_vector (15 DOWNTO 0)
        );

  FUNCTION string2int (
		CONSTANT s 				: IN string
	) RETURN integer;

	FUNCTION isspace (
		CONSTANT c 				: IN character
	) RETURN boolean;


	PROCEDURE strtok (
		VARIABLE l 				: IN line;
		CONSTANT var_318	 		: IN string;
		VARIABLE var_169			: INOUT integer;
		VARIABLE var_232			: INOUT string
	);

	PROCEDURE strtok (
		CONSTANT l, var_318 		: IN string;
		VARIABLE var_169			: INOUT integer;
		VARIABLE var_232			: INOUT string
	);

	FUNCTION char2bit (
		CONSTANT c 				: IN character
	) RETURN std_logic;

	PROCEDURE string2bitv (
		CONSTANT s 				: IN string; 
		VARIABLE r 				: INOUT std_logic_vector
	);

	FUNCTION bit2char (
		CONSTANT b 				: IN std_logic
	) RETURN character;

	PROCEDURE bitv2string (
		CONSTANT s 				: IN std_logic_vector; 
		VARIABLE r 				: INOUT string
	);

  FUNCTION streq (
		CONSTANT s, t 			: IN string
	) RETURN boolean;


	PROCEDURE echom (
		CONSTANT str 		: IN string;
		CONSTANT var_253		: IN std_logic_vector
	);

	PROCEDURE echom (
		CONSTANT str 		: IN string;
		CONSTANT var_253		: IN integer
	);



--XXXXXXXXXXXXXXXXXXXX
END io_pkg;


PACKAGE BODY io_pkg IS

--XXXXXXXXXXXXXXXXXXXX
	 PROCEDURE sysreset (
      CONSTANT t        : IN integer;
      SIGNAL var_232        : INOUT std_logic
   ) IS
   BEGIN
      var_232 <= '1';
      FOR i IN 1 TO t LOOP
         WAIT FOR 30 ns;      --XXXXXXXXXXXXXXX
      END LOOP;
      var_232 <= '0';
   END sysreset;

   PROCEDURE echoint(
	      CONSTANT str : IN integer
	     ) IS
	VARIABLE l: line;
	BEGIN
	   write(l, str);
	   writeline(var_199   ,l); --XX
	END echoint;


	PROCEDURE echo(
	   CONSTANT str : IN string
	    ) IS
	 VARIABLE l: line;
	BEGIN
	   write(l, string'("<"));
	   write(l, time'(now));
	   write(l, string'("> "));
	   write(l, str);
	   writeline(var_199   ,l); --XX
	END echo;
	

	PROCEDURE echont(
	   CONSTANT str : IN string
	    ) IS
	 VARIABLE l: line;
	BEGIN
	   write(l, str);
	   writeline(var_199   ,l); --XX
	END echont;
	
	
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	
	PROCEDURE compare_bv(
	        var_10	  	 : IN std_logic_vector;
	        var_113 	 : IN std_logic_vector
	                ) IS
	
	  VARIABLE l : line;
	  VARIABLE var_233 : boolean := true;
	  VARIABLE var_110 : string(1 TO 4);
	  VARIABLE var_6  : string(1 TO 4);
	  VARIABLE var_246 : string(1 TO 40);
	
	BEGIN
	
	  IF (var_10'length = var_113 'length) THEN
	    FOR i IN var_10'RANGE LOOP
	       IF ((var_10(i) = var_113 (i)) OR (var_113 (i) = 'X')) THEN
	         var_233 := true;   
	       ELSE
	         var_233 := false;
	          var_110 := vtoh(var_113 );
	          var_6  := vtoh(var_10);
	         var_246 := "data miscompare - actual= " & var_6  & " exp= " & var_110;
	         EXIT;
	       END IF;
	    END LOOP;
	    
	  ELSE       --XXXXXXXXXXXXXXX
	      var_246 := "* bitvectors length not equal           " ; 
	      var_233 := false;
	  END IF;
	
	        ASSERT var_233 
	          REPORT var_246 
	          SEVERITY warning;
	
	END compare_bv;


   PROCEDURE compare_bv32(
           var_10     : IN std_logic_vector;
           var_113    : IN std_logic_vector
                   ) IS
 
     VARIABLE l : line;
     VARIABLE var_233 : boolean := true;
     VARIABLE var_110 : string(1 TO 8);
     VARIABLE var_6  : string(1 TO 8);
     VARIABLE var_246 : string(1 TO 48);
 
   BEGIN
 
     IF (var_10'length = var_113 'length) THEN
       FOR i IN var_10'RANGE LOOP
          IF ((var_10(i) = var_113 (i)) OR (var_113 (i) = 'X')) THEN
            var_233 := true;
          ELSE
            var_233 := false;
             var_110 := vtoh(var_113 );
             var_6  := vtoh(var_10);
            var_246 := "data miscompare - actual= " & var_6  & " exp= " & var_110;
            EXIT;
          END IF;
       END LOOP;
 
       ELSE       --XXXXXXXXXXXXXXX
         var_246 := "* bitvectors length not equal                   " ;
         var_233 := false;
       END IF;
 
           ASSERT var_233
             REPORT var_246
             SEVERITY warning;
 
     END compare_bv32;

	
	FUNCTION vtoh(
				CONSTANT v	: IN std_logic_vector
		) RETURN string IS 
	
	  CONSTANT var_261 	: integer := v'length;
	  VARIABLE var_282		: string(1 TO 4) ;
	  VARIABLE hex_len	: natural := var_261 / 4 + 1;
	  VARIABLE var_135	: integer;
	  VARIABLE var_125	: string(1 TO var_261 / 4 + 1);
	  VARIABLE var_157	: integer;
	  VARIABLE var_253		: std_logic_vector(1 TO var_261) := v; 
	  VARIABLE var_162	: string(1 TO var_261);
	
	BEGIN 
	
	  var_135 := 0;
	
	  FOR i IN var_261 DOWNTO 1 LOOP
	    IF (var_253(i) = 'H') THEN 
	      var_162(i) := '1';
	    ELSIF (var_253(i) = 'L') THEN
	      var_162(i) := '0';
	    ELSIF (var_253(i) = '1') THEN
	      var_162(i) := '1';
	    ELSIF (var_253(i) = '0') THEN
	      var_162(i) := '0';
	    ELSIF (var_253(i) = 'U') THEN
	      var_162(i) := 'U';
	    ELSIF (var_253(i) = 'X') THEN
	      var_162(i) := 'X';
	    ELSIF (var_253(i) = 'Z') THEN
	      var_162(i) := 'Z';
	    ELSIF (var_253(i) = 'W') THEN
	      var_162(i) := 'W';
	    ELSIF (var_253(i) = '-') THEN
	      var_162(i) := '-';
	    ELSE 
	      var_162(i) := '?';
	      ASSERT false
		REPORT "unrecognizable std_logic character"
		SEVERITY warning;
	 
	    END IF;
	  END LOOP;
		
	
	  var_157 := var_261 MOD 4;
	  IF (var_157 = 0) THEN 
	    hex_len := var_261 / 4;
	  ELSE
	    hex_len := var_261 / 4 + 1;
	  END IF;
	
	  var_135 := var_261;
	
	
	  FOR i IN hex_len DOWNTO 1 LOOP
	    IF (i = 1) AND (var_157 /= 0) THEN
	      var_282(1 TO 4-var_157) := (OTHERS => '0');
	      var_282(4 - var_157 + 1 TO 4) := var_162(1 TO var_157);
	    ELSE
	      var_282 := var_162(var_135 - 3 TO var_135); 
	    END IF;
	
	    CASE var_282 IS
		WHEN "0000" => var_125(i) := '0';
		WHEN "0001" => var_125(i) := '1';
		WHEN "0010" => var_125(i) := '2';
		WHEN "0011" => var_125(i) := '3';
		WHEN "0100" => var_125(i) := '4';
		WHEN "0101" => var_125(i) := '5';
		WHEN "0110" => var_125(i) := '6';
		WHEN "0111" => var_125(i) := '7';
		WHEN "1000" => var_125(i) := '8';
		WHEN "1001" => var_125(i) := '9';
		WHEN "1010" => var_125(i) := 'a';
		WHEN "1011" => var_125(i) := 'b';
		WHEN "1100" => var_125(i) := 'c';
		WHEN "1101" => var_125(i) := 'd';
		WHEN "1110" => var_125(i) := 'e';
		WHEN "1111" => var_125(i) := 'f';
		WHEN "UUUU" => var_125(i) := 'U';
		WHEN "XXXX" => var_125(i) := 'X';
		WHEN "ZZZZ" => var_125(i) := 'Z';
		WHEN "WWWW" => var_125(i) := 'W';
		WHEN "----" => var_125(i) := '-';
		WHEN OTHERS => var_125(i) := '?';
	    END CASE;	
	    var_135 := var_135 - 4;
	  END LOOP;
	  RETURN var_125(1 TO hex_len);
	END vtoh;
	
--XXXXXXXXXXXXXXXXXXXX

	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

	FUNCTION ctov4 (
	             CONSTANT c : IN character
	            ) RETURN slv4 IS
	    VARIABLE r : std_logic_vector(3 DOWNTO 0);
	  BEGIN
	    CASE c IS
	      WHEN '0' => r := "0000";
	      WHEN '1' => r := "0001";
	      WHEN '2' => r := "0010";
	      WHEN '3' => r := "0011";
	      WHEN '4' => r := "0100";
	      WHEN '5' => r := "0101";
	      WHEN '6' => r := "0110";
	      WHEN '7' => r := "0111";
	      WHEN '8' => r := "1000";
	      WHEN '9' => r := "1001";
	      WHEN 'a'|'A' => r := "1010";
	      WHEN 'b'|'B' => r := "1011";
	      WHEN 'c'|'C' => r := "1100";
	      WHEN 'd'|'D' => r := "1101";
	      WHEN 'e'|'E' => r := "1110";
	      WHEN 'f'|'F' => r := "1111";
	      WHEN 'z'|'Z' => r := "ZZZZ";
	      WHEN 'x'|'X' => r := "XXXX";
	    WHEN OTHERS => r:= "UUUU";
	  END CASE;
	  RETURN r;
	END;


	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	
	FUNCTION ctob (
		CONSTANT s			: IN character
	) RETURN std_logic IS
		VARIABLE r			: std_logic;
	BEGIN
		CASE s IS
			WHEN 'U' => r := 'U';
			WHEN 'X' => r := 'X';
			WHEN '0' => r := '0';
			WHEN '1' => r := '1';
			WHEN 'H' => r := 'H';
			WHEN 'L' => r := 'L';
			WHEN 'Z' => r := 'Z';
			WHEN OTHERS =>
				ASSERT false REPORT "ctob: decode input is not a bit-value" 
				SEVERITY failure;
		END CASE;
		RETURN r;
	END ctob;

	
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXX
	FUNCTION vtoi(
			CONSTANT v	: IN std_logic_vector
	) RETURN integer IS 
	
	    VARIABLE var_7   : integer := 0;
	    VARIABLE var_115  : integer := 0;
	BEGIN 
		ASSERT v'length <= 32 REPORT 
		"vtoi: input vector too large to be represented as an integer"
		SEVERITY error;
		FOR i IN v'right TO v'left LOOP
			IF ( var_115 = 0 ) THEN 
				var_115 := 1;
			ELSE 
				var_115 := var_115 * 2;
			END IF;
			IF (v(i) = '1') THEN
				var_7 := var_7 + var_115;
			END IF;
		END LOOP;    
		RETURN var_7;
	END vtoi;
--XXXXXXXXXXXXXXXXXXX

	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

	FUNCTION htov (
		CONSTANT s			: IN string; 
		l						: integer
	) RETURN std_logic_vector IS
		VARIABLE var_343			: std_logic_vector(3 DOWNTO 0);
		VARIABLE r			: std_logic_vector(l-1 DOWNTO 0);
	BEGIN
		ASSERT s'left <= s'right
		REPORT "htov: string range must be descending" SEVERITY failure;

		IF l < 1 THEN
			ASSERT false
			REPORT "htov: length must be positive" SEVERITY failure;
		END IF;

		r := (OTHERS => '0');
		FOR i IN s'RANGE LOOP
			EXIT WHEN s(i) = nul;
			var_343 := ctov4(s(i));
			IF l > 4 THEN 
				r := r(l-5 DOWNTO 0) & var_343;
			ELSIF l = 4 THEN 
				r:= var_343;
			ELSE 
				r:= var_343(l-1 DOWNTO 0);
			END IF;
		END LOOP;
		RETURN r;
	END;

	
	FUNCTION itov(
		CONSTANT int		: IN integer; 
		l						:integer
	) RETURN std_logic_vector IS
		VARIABLE j : integer; 
		VARIABLE n : integer;
		VARIABLE var_232 : std_logic_vector(l-1 DOWNTO 0);
	BEGIN
		j := int;
		n := -1;
		var_232 := ( OTHERS => '0');
		IF( ( j < 0) OR ( l <1)  ) THEN 
			ASSERT false 
			REPORT "int must be > zero"
			SEVERITY failure;
		END IF;
	
		WHILE (j /= 0) LOOP
			n:= n + 1;
			IF ( n >= l) THEN 
				EXIT; 
			END IF;
			IF( j REM 2 /= 0) THEN 
				var_232(n) := '1';
			ELSE 
				var_232(n) := '0';
			END IF;
			j := j /2;
		END LOOP;
		RETURN var_232;
	END; 


	FUNCTION htoi (
		CONSTANT s			: IN string
	) RETURN integer IS
		VARIABLE i			: integer;
		VARIABLE r 			: integer;
	BEGIN
		ASSERT s'left <= s'right
		REPORT "htoi: assert 2" SEVERITY failure;
		r := 0;
		FOR i IN s'RANGE LOOP
			EXIT WHEN s(i) = nul;
			r := r * 16;
			IF s(i) <= '9' AND s(i) >= '0' THEN
				r := r + ctoi(s(i));
			ELSIF s(i) <= 'F' AND s(i) >= 'A' THEN
				r := r + ctoi(s(i));
			ELSIF s(i) <= 'f' AND s(i) >= 'a' THEN
				r := r + ctoi(s(i));
			ELSE
				ASSERT false REPORT "htoi: illegal character" 
				SEVERITY failure;
			END IF;
		END LOOP;
		RETURN r;
	END;


	FUNCTION ctoi (
		CONSTANT c			: IN character
	) RETURN integer IS
		VARIABLE r 			: integer;
	BEGIN
		CASE c IS
			WHEN '0' => r := 0;
			WHEN '1' => r := 1;
			WHEN '2' => r := 2;
			WHEN '3' => r := 3;
			WHEN '4' => r := 4;
			WHEN '5' => r := 5;
			WHEN '6' => r := 6;
			WHEN '7' => r := 7;
			WHEN '8' => r := 8;
			WHEN '9' => r := 9;
			WHEN 'a'|'A' => r := 10;
			WHEN 'b'|'B' => r := 11;
			WHEN 'c'|'C' => r := 12;
			WHEN 'd'|'D' => r := 13;
			WHEN 'e'|'E' => r := 14;
			WHEN 'f'|'F' => r := 15;
			WHEN OTHERS => r:= 0;
		END CASE;
		RETURN r;
	END;

--XXXXXXXXXXXXXXXXXXXX
	FUNCTION tochar (CONSTANT x : hex_t) RETURN character
	IS
		VARIABLE var_232		: character;
	BEGIN
		IF x < 10 THEN
			var_232 := character'val(character'pos('0') + x);
		ELSE
			var_232 := character'val(character'pos('A') + (x-10));
		END IF;
		RETURN var_232;
	END tochar;


	FUNCTION tochar (CONSTANT x : std_logic) RETURN character
	IS
		VARIABLE var_232		: character;
	BEGIN
		CASE x IS
			WHEN '0' => var_232 := '0';
			WHEN '1' => var_232 := '1';
			WHEN 'X' => var_232 := 'X';
			WHEN 'Z' => var_232 := 'Z';
			WHEN 'L' => var_232 := 'L';
			WHEN 'H' => var_232 := 'H';
			WHEN 'W' => var_232 := 'W';
			WHEN 'U' => var_232 := 'U';
			WHEN '-' => var_232 := '-';
			WHEN OTHERS =>
				ASSERT false REPORT "toChar: unknown bit value" SEVERITY error;
		END CASE;
		RETURN var_232;
	END tochar;

	FUNCTION tochar (CONSTANT x : std_logic_vector) RETURN character
	IS
		VARIABLE var_232			: character;
		VARIABLE var_94, i		: natural;
		VARIABLE var_155			: boolean;
		ALIAS r					: std_logic_vector (x'length-1 DOWNTO 0) IS x;
	BEGIN
		ASSERT x'length <= 4 REPORT "toChar: argument must be <= 4 bits"
		SEVERITY error;
		var_94 := 0;
		i := 1;
		var_155 := false;

		FOR j IN r'right TO r'left LOOP
			CASE x(j) IS
				WHEN '0' | 'L' => NULL;
				WHEN '1' | 'H' => var_94 := var_94 + i;
				WHEN OTHERS => var_155 := true;
			END CASE;
			EXIT WHEN var_155;
			i := i * 2;
		END LOOP;

		IF var_155 THEN
			var_232 := 'X';
		ELSE
			ASSERT var_94 <= 15 REPORT "toChar: argument is > 16" SEVERITY error;
			var_232 := tochar (var_94);
		END IF;
		RETURN var_232;
	END tochar;


	PROCEDURE lput (
		VARIABLE l		: INOUT line; 
		CONSTANT x		: IN integer; 
		CONSTANT b		: IN lput_radix_t := var_37 
	) IS
		VARIABLE var_31, var_166, var_156, var_427 : integer;
	BEGIN
		IF b = var_82 THEN write (l, x);
		ELSIF x=0 THEN write (l, x);
		ELSE
	      --
	      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	      --
			var_427 := x;
			IF var_427 < 0 THEN
				write (l, character' ('-'));
				var_427 := -var_427;
			END IF;
			CASE b IS
				WHEN var_37 => var_31 := 2;
				WHEN hex => var_31 := 16;
				WHEN var_189 => var_31 := 8;
				WHEN OTHERS =>
				ASSERT false REPORT "lput: unknown radix" SEVERITY error;
			END CASE;
			--
			--XXXXXXXXXXXXXXXXXXXXX
			--
			var_156 := 1;
			var_166 := var_31;
			WHILE var_166 <= var_427 LOOP
				var_156 := var_166;
				var_166 := var_166 * var_31;
			END LOOP; 
			WHILE var_156 > 0 LOOP
				var_166 := var_427 / var_156;
				write (l, character'(tochar(var_166)));
				var_427 := var_427 - (var_166 * var_156);
				var_156 := var_156 / var_31;
			END LOOP;
		END IF;
	END lput;


  PROCEDURE lput (
		VARIABLE l		: INOUT line; 
		CONSTANT x		: IN std_logic;
		CONSTANT b		: IN lput_radix_t := var_37 
	) IS
	BEGIN
		--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		--
		write (l, tochar(x)); 
	END lput;


  PROCEDURE lput (
		VARIABLE l		: INOUT line; 
		CONSTANT x		: IN std_logic_vector; 
		CONSTANT b		: IN lput_radix_t := var_37 
	) IS
		VARIABLE bs				: std_logic_vector (3 DOWNTO 0);
		CONSTANT zero			: std_logic_vector (3 DOWNTO 0) := "0000";
		VARIABLE var_31				: std_logic;
		ALIAS var_425					: std_logic_vector (x'length-1 DOWNTO 0) IS x;
	 	VARIABLE var_47    	: integer; 
		VARIABLE var_183 		: integer;
		VARIABLE var_127 ,j	: integer;
	BEGIN
		IF b=var_37 THEN
			FOR i IN x'RANGE LOOP
				var_31 := x(i); 
				lput (l, var_31);
			END LOOP;
		ELSE
			CASE b IS
				WHEN var_189 => var_47     := 3;
				WHEN hex => var_47     := 4;
				WHEN OTHERS =>
					ASSERT false REPORT "lput: unsupported radix" SEVERITY error;
			END CASE;
			var_183  := x'length / var_47    ;
			var_127  := x'length REM var_47    ;
			j := var_425'left;
			--
			--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
			--
			IF var_127  > 0 THEN
				bs := zero;
				FOR i IN var_127 -1 DOWNTO 0 LOOP
					bs(i) := var_425(j);
					j := j-1;
				END LOOP;
				write (l, tochar(bs));
			END IF;
			WHILE j >= 0 LOOP
				FOR i IN var_47    -1 DOWNTO 0 LOOP
					bs(i) := var_425(j);
					j := j - 1;
				END LOOP;
				write (l, tochar(bs));
			END LOOP;
		END IF;
	END lput;


	FUNCTION compare (	
		CONSTANT vectora	: IN std_logic;	--XXXXXXX
		CONSTANT var_347	: IN std_logic		--XXXXXXXXX
	) RETURN boolean IS
		VARIABLE var_316	: std_logic;
		VARIABLE var_317	: std_logic;
		VARIABLE var_111 	: boolean;
	BEGIN
		var_111 := false;
		CASE vectora IS
			WHEN 'H' => var_316 := '1';
			WHEN 'L' => var_316 := '0';
			WHEN OTHERS => var_316 := vectora;
		END CASE;
		CASE var_347 IS
			WHEN 'H' => var_317 := '1';
			WHEN 'L' => var_317 := '0';
			WHEN OTHERS => var_317 := var_347;
		END CASE;
		IF ((var_317 /= 'X') AND (var_316 /= var_317)) THEN
			var_111 := true;
		END IF;
		RETURN var_111;
	END compare;


	FUNCTION compare (	
		CONSTANT vectora	: IN std_logic_vector;	--XXXXXXX
		CONSTANT var_347	: IN std_logic_vector	--XXXXXXXXX
	) RETURN boolean IS
		VARIABLE var_316	: std_logic_vector((vectora'length-1) DOWNTO 0);
		VARIABLE var_317	: std_logic_vector((var_347'length-1) DOWNTO 0);
		VARIABLE var_111 	: boolean;
		VARIABLE b			: integer;
	BEGIN
		var_111 := false;
		IF (vectora'length) /= (var_347'length) THEN
			ASSERT false REPORT "logic_compare: vectors not same size"
			SEVERITY failure;
		END IF;
		b := 0;
		FOR i IN vectora'RANGE LOOP
			CASE vectora(i) IS
				WHEN 'H' => var_316(b) := '1';
				WHEN 'L' => var_316(b) := '0';
				WHEN OTHERS => var_316(b) := vectora(i);
			END CASE;
			b := b + 1;
		END LOOP;
		b := 0;
		FOR i IN var_347'RANGE LOOP
			CASE var_347(i) IS
				WHEN 'H' => var_317(b) := '1';
				WHEN 'L' => var_317(b) := '0';
				WHEN OTHERS => var_317(b) := var_347(i);
			END CASE;
			b := b + 1;
		END LOOP;
		var_262 : FOR i IN 0 TO vectora'length-1 LOOP
			IF ((var_317(i) /= 'X') AND (var_316(i) /= var_317(i))) THEN
				var_111 := true;
				EXIT var_262 ;
			END IF;
		END LOOP var_262 ;
		RETURN var_111;
	END compare;


	PROCEDURE data_fail (
		CONSTANT var_391		: IN string;
		CONSTANT data		: IN std_logic;
		CONSTANT var_112 	: IN std_logic;
		var_116    			: IN boolean
	) IS
		VARIABLE l			: line;
	BEGIN
		write(l, string'("<"));
		write(l, time'(now));
		write(l, string'("> "));
		write(l, var_391);
		write(l, string'(", expected: ")); 
		lput(l, var_112 , hex);
		write( l, string'("  actual: ")); 
		lput(l, data, hex);
		writeline(var_199   , l); --XX
		IF (var_116    ) THEN
			ASSERT false REPORT "data miscompare" SEVERITY failure;
		ELSE
			ASSERT false REPORT "data miscompare" SEVERITY error;
		END IF;
	END;


	PROCEDURE data_fail (
		CONSTANT var_391		: IN string;
		CONSTANT data		: IN std_logic_vector;
		CONSTANT var_112 	: IN std_logic_vector;
		var_116    			: IN boolean
	) IS
		VARIABLE l			: line;
	BEGIN
		write(l, string'("<"));
		write(l, time'(now));
		write(l, string'("> "));
		write(l, var_391);
		write(l, string'(", expected: ")); 
		lput(l, var_112 , hex);
		write( l, string'("  actual: ")); 
		lput(l, data, hex);
		writeline(var_199   , l); --XX
		IF (var_116    ) THEN
			ASSERT false REPORT "data miscompare" SEVERITY failure;
		ELSE
			ASSERT false REPORT "data miscompare" SEVERITY error;
		END IF;
	END;

	PROCEDURE data_fail (
		CONSTANT var_391		: IN string;
		CONSTANT addr		: IN std_logic_vector;
		CONSTANT data		: IN std_logic_vector;
		CONSTANT var_112 	: IN std_logic_vector;
		var_116    			: IN boolean
	) IS
		VARIABLE l			: line;
	BEGIN
		write(l, string'("<"));
		write(l, time'(now));
		write(l, string'("> "));
		write(l, var_391);
		write(l, string'(", address: "));
		lput(l, addr, hex);
		write(l, string'(" expected: ")); 
		lput(l, var_112 , hex);
		write(l, string'("  actual: ")); 
		lput(l, data, hex);
		writeline(var_199   , l); --XX
		IF (var_116    ) THEN
			ASSERT false REPORT "data miscompare" SEVERITY failure;
		ELSE
			ASSERT false REPORT "data miscompare" SEVERITY error;
		END IF;
	END;


	PROCEDURE int2string (
		CONSTANT v				: IN integer; 
		VARIABLE t 				: INOUT string
	) IS
		VARIABLE i, j, r : integer;
 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		VARIABLE var_48  : string (var_172      DOWNTO 1);  --XX
	BEGIN
		ASSERT t'left <= t'right 
		REPORT "int2string: assert 1" SEVERITY failure;
		i := 1;
		r := ABS v;
		var_48 (i) := '0';  --XX
		WHILE r > 0 LOOP
			CASE (r REM 10) IS
				WHEN 0 => var_48 (i) := '0';  --XX
				WHEN 1 => var_48 (i) := '1';  --XX
				WHEN 2 => var_48 (i) := '2';  --XX
				WHEN 3 => var_48 (i) := '3';  --XX
				WHEN 4 => var_48 (i) := '4';  --XX
				WHEN 5 => var_48 (i) := '5';  --XX
				WHEN 6 => var_48 (i) := '6';  --XX
				WHEN 7 => var_48 (i) := '7';  --XX
				WHEN 8 => var_48 (i) := '8';  --XX
				WHEN 9 => var_48 (i) := '9';  --XX
				WHEN OTHERS => ASSERT false
					REPORT "int2string: assert 4" SEVERITY failure;
			END CASE;
			r := r / 10;
			i := i + 1;
			ASSERT i < var_172     
			REPORT "int2string: assert 2" SEVERITY failure;
		END LOOP;
		IF i > 1 THEN 
			i := i - 1; 
		END IF;
		ASSERT t'length > i  
		REPORT "int2string: assert 3" SEVERITY failure;
		j := t'left;
		WHILE i >= 1 LOOP
			t(j) := var_48 (i);  --XX
			j := j + 1; i := i - 1;
		END LOOP;
		t(j) := nul;
	END int2string;


	FUNCTION string2int (
		CONSTANT s 					: IN string
	) RETURN integer IS
		VARIABLE i, r, m			: integer;
		VARIABLE c					: character;
	BEGIN
		ASSERT s'left <= s'right
		REPORT "string2int: assert 2" SEVERITY failure;
		--XXXXXXXXX
		i := s'left;
		WHILE s(i) /= nul LOOP
			i := i + 1;
		END LOOP;
		i := i - 1; 
		r := 0;
		m := 1;
		WHILE i >= s'left LOOP
			c := s(i);
			r := r + (m * (character'pos(c)-character'pos('0')));
			m := m * 10;
			i := i - 1;
		END LOOP;
		RETURN r;
	END string2int;

        PROCEDURE int2word (
                CONSTANT v                              : IN integer;
                VARIABLE t                              : INOUT std_logic_vector (15 DOWNTO 0)
        ) IS
                VARIABLE i, j, r : integer;
        BEGIN
                i := 1;
                r := ABS v;
                t(i) := '0';
                WHILE r > 0 LOOP
                        CASE (r REM 2) IS
                                WHEN 0 => t(i) := '0';
                                WHEN 1 => t(i) := '1';
                                WHEN OTHERS => ASSERT false
                                        REPORT "int2word: assert 4" SEVERITY failure;
                        END CASE;
                        r := r / 2;
                        i := i + 1;
                        ASSERT i < 16
                        REPORT "int2word: assert 2" SEVERITY failure;
                END LOOP;
        END int2word;


	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	FUNCTION isspace (
		CONSTANT c 				: IN character
	) RETURN boolean IS
		VARIABLE r : boolean := false;
	BEGIN
		IF (c = ' ') OR (c = ht) OR (c = cr) OR (c = lf) THEN 
			r := true; 
		END IF;
		RETURN r;
	END isspace;


	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXX

	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--
	--XXXXXXXXX
	--
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--
	--XXXXXXXXXXXXXXXXXXXXXXXXX
	--
	--XXXXXXXXXXXXXXXXXXXXXXXXXX
	--
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   
	PROCEDURE strtok (
		CONSTANT l, var_318 			: IN string;
		VARIABLE var_169				: INOUT integer;
		VARIABLE var_232				: INOUT string
	) IS
		VARIABLE i, j 				: integer;
		VARIABLE var_252, var_274	: boolean;
		VARIABLE c					: character;
	BEGIN
		ASSERT (l'left <= l'right AND var_232'left <= var_232'right 
		AND var_318'left <= var_318'right)
		REPORT "strtok: strings do not equate to left <= right" 
		SEVERITY failure;

		var_252 := true;
		IF var_169 < 1 THEN 
			var_169 := l'left; 
		END IF;
		i := var_232'left;
		var_274 := true;

		var_114: WHILE var_169 <= l'right AND var_274 LOOP
			c := l(var_169);
			j := var_318'left;
			WHILE j <= var_318'right AND var_274 LOOP
				IF c=var_318(j) THEN 
					var_274 := false; 
				END IF;
				j := j + 1;
			END LOOP;
			IF var_274 THEN 
				var_252 := false;
				var_232(i) := c; i := i + 1; 
				--XXXXXXXXXXXXXXXXXXXXXX
				--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
				IF NOT (i < var_232'right) THEN
					var_169 := var_169 - 1;
					EXIT var_114;
				END IF;
			END IF;
			var_169 := var_169 + 1;
			--XXXXXXXXXXXXXXXXXXXX
			IF var_274=false AND var_252=true THEN 
				var_274:=true; 
			END IF;
		END LOOP var_114;
		var_232(i) := nul;
	END strtok;

   
	PROCEDURE strtok (
		VARIABLE l	 				: IN line;
		CONSTANT var_318 				: IN string;
		VARIABLE var_169				: INOUT integer;
		VARIABLE var_232				: INOUT string
	) IS
		VARIABLE i, j 				: integer;
		VARIABLE var_252, var_274	: boolean;
		VARIABLE c					: character;
	BEGIN
		ASSERT (l'left <= l'right AND var_232'left <= var_232'right 
		AND var_318'left <= var_318'right)
		REPORT "strtok: strings do not equate to left <= right" 
		SEVERITY failure;

		var_252 := true;
		IF var_169 < 1 THEN 
			var_169 := l'left; 
		END IF;
		i := var_232'left;
		var_274 := true;

		var_114: WHILE var_169 <= l'right AND var_274 LOOP
			c := l(var_169);
			j := var_318'left;
			WHILE j <= var_318'right AND var_274 LOOP
				IF c=var_318(j) THEN 
					var_274 := false; 
				END IF;
				j := j + 1;
			END LOOP;
			IF var_274 THEN 
				var_252 := false;
				var_232(i) := c; i := i + 1; 
				--XXXXXXXXXXXXXXXXXXXXXX
				--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
				IF NOT (i < var_232'right) THEN
					var_169 := var_169 - 1;
					EXIT var_114;
				END IF;
			END IF;
			var_169 := var_169 + 1;
			--XXXXXXXXXXXXXXXXXXXX
			IF var_274=false AND var_252=true THEN 
				var_274:=true; 
			END IF;
		END LOOP var_114;
		var_232(i) := nul;
	END strtok;


	FUNCTION char2bit (
		CONSTANT c 				: IN character
	) RETURN std_logic IS
		VARIABLE r				: std_logic;
	BEGIN
		IF c='1' THEN 
			r := '1';
		ELSIF c='0' THEN 
			r := '0';
		ELSIF c='z' OR c='Z' THEN 
			r := 'Z';
		ELSE 
			r := 'X';
		END IF;
		RETURN r;
	END char2bit;


	PROCEDURE string2bitv (
		CONSTANT s 				: IN string; 
		VARIABLE r				: INOUT std_logic_vector
	) IS
		VARIABLE i, j			: integer;
		VARIABLE c 				: character;
	BEGIN
		ASSERT s'left <= s'right AND r'left >= r'right
		REPORT "string2bitv: assert 1" SEVERITY failure;
		i := s'left;
		WHILE s(i) /= nul LOOP
			i := i + 1;
		END LOOP;
		IF i > s'left THEN 
			i := i - 1; 
		END IF;
		ASSERT r'length >= 1+(i-s'left)
		REPORT "string2bitv: assert 2" SEVERITY failure;

		FOR j IN r'right TO r'left LOOP
			IF i >= s'left THEN 
				c := s(i); 
				i := i - 1; 
			ELSE 
				c := '0'; 
			END IF;
			r(j) := char2bit(c);
		END LOOP;
	END string2bitv;
 

	FUNCTION bit2char (
		CONSTANT b 				: IN std_logic
	) RETURN character IS
		VARIABLE c 				: character;
	BEGIN
		IF b='1' THEN 
			c := '1'; 
		ELSIF b='0' THEN 
			c := '0';
		ELSIF b='Z' THEN 
			c := 'Z';
		ELSE 
			c := 'X';
		END IF;
		RETURN c;
	END bit2char;


	PROCEDURE bitv2string (
		CONSTANT s 				: IN std_logic_vector; 
		VARIABLE r 				: INOUT string
	) IS
		VARIABLE i 				: integer;
	BEGIN
		ASSERT r'left <= r'right 
		REPORT "bitv2string: assert 1" SEVERITY failure;

		i := r'left;
		ASSERT r'length > s'length 
		REPORT "bitv2string: assert 2" SEVERITY failure;

		FOR j IN s'RANGE LOOP
			r(i) := bit2char(s(j));
			i := i + 1;
		END LOOP;
		r(i) := nul;
	END bitv2string;


	--XXXXXXXXXXXXXXXXXXXX
	FUNCTION streq (
		CONSTANT s, t			: IN string
	) RETURN boolean IS
		VARIABLE r				: boolean;
		VARIABLE i, j			: integer;
	BEGIN
		ASSERT s'left <= s'right AND t'left <= t'right 
		REPORT "streq: assert 1" SEVERITY failure;

		r := true;
		i := s'left; j := t'left;
		WHILE r AND s(i) /= nul AND j <= t'right LOOP
			IF s(i) /= t(j) THEN 
				r := false; 
			END IF;
			i := i + 1; 
			j := j + 1;
		END LOOP;
		IF s(i) /= nul OR j /= t'right+1 THEN
			r := false;
		END IF;
		RETURN r;
	END streq;


	PROCEDURE echom (
	   CONSTANT str 		: IN string;
		CONSTANT var_253		: IN std_logic_vector
	    ) IS
	 VARIABLE l: line;
	BEGIN
	   write(l, string'("<"));
	   write(l, time'(now));
	   write(l, string'("> "));
	   write(l, str);
		lput(l, var_253, hex);
	   writeline(var_199   ,l); --XX
	END echom;

	
	PROCEDURE echom (
	   CONSTANT str 		: IN string;
		CONSTANT var_253		: IN integer
	    ) IS
	 VARIABLE l: line;
	BEGIN
	   write(l, string'("<"));
	   write(l, time'(now));
	   write(l, string'("> "));
	   write(l, str);
		lput(l, var_253, var_82);
	   writeline(var_199   ,l); --XX
	END echom;


--XXXXXXXXXXXXXXXXXXX
END io_pkg;


--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXX
--X
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--X
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXX
--X
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XX
--XXXXXX
--XXXX
--X
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXX
--X
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--X
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXX
--
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX


--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
LIBRARY work;
USE work.eas_parameters.ALL;
USE work.io_pkg.ALL;
USE std.textio.ALL;


PACKAGE bfm_pkg IS

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   PROCEDURE deallocate(                      --XX
      VARIABLE var_175 : IN element_ptr );    --XX


   PROCEDURE calcblocknum (
     VARIABLE address : IN std_logic_vector (maxaddrline DOWNTO 0);
     VARIABLE var_281    : IN integer;
     VARIABLE var_41  : OUT integer;
     VARIABLE var_206     : OUT integer;
     VARIABLE var_45        : OUT integer;
     VARIABLE var_149    : OUT integer;
     VARIABLE var_39     : OUT bit;
     VARIABLE var_154     : OUT bit);

   PROCEDURE findpowerof2 (
     VARIABLE var_322   : IN integer;
     VARIABLE n : OUT integer);

   PROCEDURE getblockheader (
     VARIABLE var_143    : INOUT linkedarray_type;
     VARIABLE var_41   : IN integer;
     VARIABLE var_185   : OUT element_ptr);

   PROCEDURE assignblockheader (
     VARIABLE var_143    : INOUT linkedarray_type;
     VARIABLE var_41   : IN integer;
     VARIABLE var_185   : IN element_ptr);

   PROCEDURE readarrayproc (
     VARIABLE var_281    : IN integer;
     VARIABLE var_144 : IN std_logic_vector (maxaddrline DOWNTO 0);
     VARIABLE readarray : INOUT linkedarray_type;
     VARIABLE data    : OUT std_logic_vector (maxdataline DOWNTO 0) ); 

   PROCEDURE writearrayproc (
     VARIABLE var_281    : IN integer;     
     VARIABLE var_153 : IN std_logic_vector (maxaddrline DOWNTO 0);
     VARIABLE var_399    : INOUT linkedarray_type;
     VARIABLE var_383   : IN std_logic_vector (maxdataline DOWNTO 0) ); 

   PROCEDURE eraseablockwnum (
      VARIABLE var_170   : INOUT linkedarray_type;
      VARIABLE var_95    : IN integer );

   PROCEDURE hex_to_slv (
      VARIABLE var_12   : IN string(1 TO 6);
      VARIABLE var_77   : IN string(1 TO 5);
      VARIABLE var_137  : OUT std_logic_vector(maxaddrline DOWNTO 0);
      VARIABLE var_138  : OUT std_logic_vector(maxdataline DOWNTO 0) 
	);
	

END bfm_pkg;

PACKAGE BODY bfm_pkg IS

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
PROCEDURE deallocate(                     --XX
      VARIABLE var_175 : IN element_ptr ) IS  --XX
 
BEGIN                                         --XX
 
END deallocate;                               --XX
 


--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
PROCEDURE calcblocknum (
  VARIABLE address : IN std_logic_vector (maxaddrline DOWNTO 0);
  VARIABLE var_281    : IN integer;
  VARIABLE var_41  : OUT integer;
  VARIABLE var_206     : OUT integer;  
  VARIABLE var_45        : OUT integer;
  VARIABLE var_149     : OUT integer;
  VARIABLE var_39     : OUT bit;
  VARIABLE var_154     : OUT bit
  )IS

  VARIABLE var_43       : integer;
  VARIABLE var_148 : integer;  --XXXXXXXXXXXXXXXXXXXXXXXXX
  VARIABLE var_194  : std_logic_vector (5 DOWNTO 0);
  VARIABLE var_75      :  integer;
  VARIABLE var_270  : integer;
  VARIABLE var_99 : integer;
  VARIABLE var_40          : bit :='1';
  VARIABLE var_205         : integer;
  VARIABLE var_207         : integer;
  
  
  BEGIN
    IF (var_281    = targetmainarray) THEN
    --XXXXXXXXXX
      var_148 := vtoi(address);
     IF (var_148 <= maxaddress) THEN  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      IF (topboot = 0) THEN --XXXXXXXXXXXXX
        var_75       := (paramblocksize * paramblocknum);
        IF (var_148 < var_75      ) THEN  --XXXXXXXXXXXXXXXXXXXXXXXXXXX
          var_43       := var_148 / paramblocksize;
          var_45         := var_43       * paramblocksize;
          var_154      := '1';
        ELSIF (var_148 >= var_75      ) THEN
          var_43       := paramblocknum + ((var_148 - var_75      )/mainblocksize);
          var_45         := (var_43       - (paramblocknum - 1)) * mainblocksize;
          var_154      := '0';
        END IF;
      ELSIF (topboot = 1) THEN --XXXXXXXXX
        var_75       := maxaddress - (paramblocksize * paramblocknum);
        IF (var_148 > var_75      ) THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXX
          var_43       := mainblocknum + ((var_148 - var_75      ) / paramblocksize);
          var_45         := ((var_43       - mainblocknum) * paramblocksize)+(mainblocknum * mainblocksize);       
          var_154      := '1';
        ELSE 
          var_43       := var_148 / mainblocksize;
          var_45         := var_43       * mainblocksize;
          var_154      := '0';
        END IF;
      ELSIF (topboot = 2) THEN --XXXXXXXXXXXXXXXXXXXXXXX
        var_43       := var_148/mainblocksize;
        var_45         := var_43       * mainblocksize;
        var_154      := '0';
      END IF;
      var_206      := var_148 / partitionsize;  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_41   := var_43      ;
      var_149     := var_148;
     ELSE
      var_40          := '0';
      var_39      := var_40         ;
     END IF;
    
    --XXXXXXXXXX
    ELSIF (var_281    = targetotp) THEN
      var_148 := vtoi(address); 
      IF (var_148 = 16#80#) THEN
        var_41   := 0;    
      ELSIF (var_148 >= 16#81#) AND (var_148  <= 16#84#) THEN
        var_41   := 1;
      ELSIF (var_148  >= 16#85#) AND (var_148 <= 16#88#) THEN
        var_41   := 2;
      ELSIF (var_148 = minotpaddr) THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        var_41   := 3;
      ELSIF ((var_148 > minotpaddr) AND (var_148 <= maxotpaddr)) THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        var_41   := ((var_148 - minotpaddr) / wordperotpblock) + 4;          
      END IF;
      var_206      := var_207         ;
      var_149     := var_148;
    END IF;
    
    IF (var_40          = '0') THEN  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_41   := 0;
      ASSERT (0>1)
        REPORT "Address is way out of wack (memory map)"
        SEVERITY error;
    END IF;      
  END calcblocknum;


--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

PROCEDURE findpowerof2( 
  VARIABLE var_322   : IN integer;
  VARIABLE n : OUT integer
  ) IS

  VARIABLE var_74 : integer:= 1;
  VARIABLE var_126 : integer;
  BEGIN
    var_126 := var_322  ;
    var_251  :WHILE (var_126 > 2) LOOP
      var_126 := var_126/2;
      var_74 := var_74 + 1;
    END LOOP;
    n := var_74;
  END findpowerof2;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
PROCEDURE getblockheader (
  VARIABLE var_143    : INOUT linkedarray_type;
  VARIABLE var_41   : IN integer;
  VARIABLE var_185   : OUT element_ptr) IS
      
  BEGIN
        var_185 := var_143   (var_41  );
  END getblockheader;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
PROCEDURE assignblockheader (
  VARIABLE var_143    : INOUT linkedarray_type;
  VARIABLE var_41   : IN integer;
  VARIABLE var_185   : IN element_ptr) IS
      
  BEGIN
        var_143   (var_41  ) := var_185;
END assignblockheader;


PROCEDURE readarrayproc (
     VARIABLE var_281    : IN integer;
     VARIABLE var_144 : IN std_logic_vector (maxaddrline DOWNTO 0);
     VARIABLE readarray : INOUT linkedarray_type;
     VARIABLE data    : OUT std_logic_vector (maxdataline DOWNTO 0) )IS 

     VARIABLE var_11     : integer;
     VARIABLE var_175 : element_ptr;
     VARIABLE var_160 : line;
     VARIABLE var_41   : integer;
     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     VARIABLE var_206      : integer;
     VARIABLE var_45         : integer;
     VARIABLE var_149     : integer;
     VARIABLE var_39      : bit;
     VARIABLE var_154      : bit;

BEGIN
     
    IF (verbose =true) THEN
	write (var_160,string '("Readarrayproc: calling Calcblocnum raddr ="));
	lput (var_160, var_144,hex);
	writeline(var_199   ,var_160); --XX
    END IF; 
 
    calcblocknum(var_144, var_281   , var_41  , var_206     , var_45        , 
     	         var_149    , var_39     , var_154     );
    var_11   := vtoi(var_144);
    getblockheader(readarray, var_41  , var_175);
    IF (var_175 = NULL) THEN  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      data := "1111111111111111";
    ELSE
      var_227   : WHILE (var_175 /= NULL) LOOP  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_175.address = var_11  ) THEN   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          data:=var_175.data;            --XXXXXXXXXXXXXXXXXXXXXXXXXXXX
          EXIT var_227  ;
        ELSIF (var_175.nxt /= NULL) THEN
          var_175 := var_175.nxt;        --XXXXXXXXXXXXXXXXXXXXXXX
        ELSE
          data := "1111111111111111";  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          EXIT var_227  ;
        END IF;
      END LOOP var_227  ;
    END IF;
 
END readarrayproc;


--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXX
PROCEDURE writearrayproc (
     VARIABLE var_281    : IN integer;     
     VARIABLE var_153 : IN std_logic_vector (maxaddrline DOWNTO 0);
     VARIABLE var_399    : INOUT linkedarray_type;
     VARIABLE var_383   : IN std_logic_vector (maxdataline DOWNTO 0) )IS 

     VARIABLE var_175  : element_ptr;
     VARIABLE var_41   : integer;
     VARIABLE var_141             : std_logic_vector (maxdataline DOWNTO 0); 
     VARIABLE var_11   : integer;
     VARIABLE var_160 : line;
     VARIABLE var_198  : line;
     
     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     VARIABLE var_206      : integer;
     VARIABLE var_45         : integer;
     VARIABLE var_149     : integer;
     VARIABLE var_39      : bit;
     VARIABLE var_154      : bit;
BEGIN
    var_11   := vtoi(var_153);
    calcblocknum(var_153,var_281   ,var_41  ,var_206     ,var_45        ,var_149    , var_39     ,var_154     );
    getblockheader(var_399   , var_41  , var_175);
    IF (verbose = true) THEN
       write(var_198  ,string'("WriteArrayProc: Calling ReadArrayProc for address = "));
       lput(var_198  , var_153, hex);
       writeline(var_199   ,var_198  ); --XX
     END IF;
     readarrayproc(var_281   , var_153, var_399   ,var_141           ) ;
     IF (var_141           /= "1111111111111111") THEN  --XXXXXXXXXXXXXXXXXXXXXXX
        var_216      : WHILE (var_175 /= NULL) LOOP
          --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          IF (var_175.address=var_11  ) THEN
            var_215        : FOR i IN 0 TO maxdataline LOOP
              --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_175.data(i) := var_175.data(i) AND var_383(i);
            END LOOP var_215        ;
            EXIT var_216     ;
          END IF;
          var_175 := var_175.nxt;
        END LOOP var_216     ;
     ELSE
        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        --XXXXXXXXXXXXXX
        var_175 := NEW element_rec;
        var_175.data:=var_383;
        var_175.address:=var_11  ;

	IF (verbose = true) THEN
   	  write(var_160,string'("WriteArrayProc: Write to new block"));
   	  write(var_160,integer'(var_41  ));
   	  writeline(var_199   ,var_160); --XX
	END IF;

        getblockheader(var_399   , var_41  , var_175.nxt);
        assignblockheader(var_399   , var_41  , var_175);
     END IF;
     
END writearrayproc;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
PROCEDURE eraseablockwnum (
      VARIABLE var_170   : INOUT linkedarray_type;
      VARIABLE var_95    : IN integer ) IS
      
      VARIABLE var_175           : element_ptr;
      VARIABLE var_315           : element_ptr;
      VARIABLE var_160                : line;
BEGIN
      getblockheader(var_170  , var_95   , var_175);
      IF (verbose = true) THEN
         write(var_160,string'("Erase Block loop "));
         writeline(var_199   ,var_160); --XX
      END IF;
      var_106    : WHILE (var_175 /= NULL) LOOP
         var_315 := var_175.nxt;
         deallocate(var_175);                  --XXXXXXXX
         var_175 := var_315;
      END LOOP var_106   ;
      assignblockheader(var_170  , var_95   , var_175);
END eraseablockwnum;


--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

PROCEDURE hex_to_slv (
   VARIABLE var_12   : IN string(1 TO 6);
   VARIABLE var_77   : IN string(1 TO 5);
   VARIABLE var_137  : OUT std_logic_vector(maxaddrline DOWNTO 0);
   VARIABLE var_138  : OUT std_logic_vector(maxdataline DOWNTO 0) 
	) IS
   VARIABLE max : integer;  
   VARIABLE min : integer; 
   VARIABLE c   : character; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   VARIABLE y   : integer;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   VARIABLE var_24    : std_logic_vector(23 DOWNTO 0);
   VARIABLE var_142   : string(1 TO 6);
BEGIN

FOR i IN 1 TO 2 LOOP
   IF i = 1 THEN 
     max := 6 ; min := 1;
     var_142   := var_12  ;
   ELSIF i = 2 THEN 
     max := 5; min := 2;
     FOR j IN 1 TO 5 LOOP
        var_142  (j) := var_77  (j); END LOOP; 
   END IF;
 
   y:=-1;
   FOR x IN max DOWNTO min LOOP
       y:=y+4;                 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
       c := var_142  (x);       --XXXXXXXXXXX
       CASE c IS
         WHEN '0'        =>   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		var_24   (y)  :='0';  --XXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_24   (y-1):='0';
                var_24   (y-2):='0';
                var_24   (y-3):='0';
 
         WHEN '1'        =>
                var_24   (y)  :='0';
                var_24   (y-1):='0';
                var_24   (y-2):='0';
                var_24   (y-3):='1';

         WHEN '2'        =>
                var_24   (y)  :='0';
                var_24   (y-1):='0';
                var_24   (y-2):='1';
                var_24   (y-3):='0';

         WHEN '3'        =>
                var_24   (y)  :='0';
                var_24   (y-1):='0';
                var_24   (y-2):='1';
                var_24   (y-3):='1';

         WHEN '4'        =>
                var_24   (y)  :='0';
                var_24   (y-1):='1';
                var_24   (y-2):='0';
                var_24   (y-3):='0';

         WHEN '5'        =>
                var_24   (y)  :='0';
                var_24   (y-1):='1';
                var_24   (y-2):='0';
                var_24   (y-3):='1';

         WHEN '6'        =>
                var_24   (y)  :='0';
                var_24   (y-1):='1';
                var_24   (y-2):='1';
                var_24   (y-3):='0';

         WHEN '7'        =>
                var_24   (y)  :='0';
                var_24   (y-1):='1';
                var_24   (y-2):='1';
                var_24   (y-3):='1';

         WHEN '8'        =>
                var_24   (y)  :='1';
                var_24   (y-1):='0';
                var_24   (y-2):='0';
                var_24   (y-3):='0';

         WHEN '9'        =>
                var_24   (y)  :='1';
                var_24   (y-1):='0';
                var_24   (y-2):='0';
                var_24   (y-3):='1';

         WHEN 'A' | 'a'       =>
                var_24   (y)  :='1';
                var_24   (y-1):='0';
                var_24   (y-2):='1';
                var_24   (y-3):='0';
         WHEN 'B' | 'b'      =>
                var_24   (y)  :='1';
                var_24   (y-1):='0';
                var_24   (y-2):='1';
                var_24   (y-3):='1';
         WHEN 'C' | 'c'      =>
                var_24   (y)  :='1';
                var_24   (y-1):='1';
                var_24   (y-2):='0';
                var_24   (y-3):='0';
         WHEN 'D' | 'd'      =>
                var_24   (y)  :='1';
                var_24   (y-1):='1';
                var_24   (y-2):='0';
                var_24   (y-3):='1';
         WHEN 'E' | 'e'      =>
                var_24   (y)  :='1';
                var_24   (y-1):='1';
                var_24   (y-2):='1';
                var_24   (y-3):='0';
         WHEN 'F' | 'f'      =>
                var_24   (y)  :='1';
                var_24   (y-1):='1';
                var_24   (y-2):='1';
                var_24   (y-3):='1';
	 WHEN OTHERS     =>
       END CASE;
   END LOOP; --XXXXXXX

   IF i= 1 THEN
      var_137 (maxaddrline DOWNTO 0) := var_24   (maxaddrline DOWNTO 0);
   ELSIF i=2 THEN
      FOR j IN maxdataline DOWNTO 0 LOOP
          var_138 (j) := var_24   (j); END LOOP;
   END IF;
END LOOP; --XXXXXXX
END hex_to_slv;

END bfm_pkg;





--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX



LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE std.textio.ALL;
LIBRARY work;
USE work.io_pkg.ALL;
USE work.eas_parameters.ALL;
USE work.bfm_pkg.ALL;

ENTITY var_329 IS
PORT (addr   : IN std_logic_vector (maxaddrline DOWNTO 0);
      dq     : IN std_logic_vector(maxdataline DOWNTO 0);--XXXXXXXXXXXXXXXXXXX
      ceb    : IN std_logic;
      oeb    : IN std_logic; 
      web    : IN std_logic;
      rpb    : IN std_logic;
      var_397    : IN std_logic;
      var_18   : IN std_logic;
      var_69    : IN std_logic;
      var_344  : IN real;
--XXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXX
      vpen   : IN real;
      vccq   : IN real;
--XXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXX
      var_420    : IN bit;
      var_418          : IN integer;    --XXXXXXXXXXXXXXXXXXXXXXXXX
      var_417          : IN bit;        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_412          : IN bit;        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_409          : IN bit;        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_416          : IN std_logic_vector (maxaddrline DOWNTO 0);    --XXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_411          : IN integer;    --XXXXXXXXXXXXXXXXXXXXX
      var_407          : IN integer;    --XXXXXXXXXXXXXXXXXXXXXXXX
      var_338          : OUT integer := writecmd; 
      var_340          : OUT std_logic_vector (maxaddrline DOWNTO 0);  --XXXXXXXXXXXXXXXXXXXXXXXXX
      var_339          : OUT std_logic_vector (maxdataline DOWNTO 0) := "1111111111111111";  --XXXXXXXXXXXXXXXX
      var_337          : OUT integer;   --XXXXXXXXXX
      var_334          : OUT integer;   --XXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_346           : OUT bit := '0';       --XXXXXXXXXXXXXXXXXX
      var_265	       : IN std_logic_vector (maxsrsize DOWNTO 0);
      var_426              : IN std_logic_vector (maxsrsize DOWNTO 0);
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      cr               : OUT std_logic_vector (maxcrsize DOWNTO 0) := (OTHERS => '1');--XXXXXXXXXXX
      var_341           : OUT std_logic := '0'
      );
END var_329;

ARCHITECTURE var_36   OF var_329 IS

FILE var_199   : text IS OUT "STD_OUTPUT"; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
SIGNAL var_186    : integer := powerup;
SIGNAL var_271      : integer := powerup;
SIGNAL var_272    : time      := 0 ns;



SIGNAL var_247           : bit     := '0';
 
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

BEGIN

var_219  : PROCESS (var_247)
 BEGIN
   var_247 <= NOT (var_247) AFTER pollingdelay;
 END PROCESS var_219  ;
 

var_273    :PROCESS (var_186   )
    VARIABLE var_160 : line;
  BEGIN
    IF (var_271 /= var_186   ) THEN
     var_272      <= now;
     var_271        <= var_186   ;
    END IF;
  END PROCESS var_273    ;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXX
--XXXXXXXXXXXXXXXXX
--XXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXX
  
timingcheck: PROCESS (addr,dq,ceb,oeb,web,rpb,var_397,var_18) --XXXXXX
  VARIABLE var_288     :time := 0 ns;
  VARIABLE var_289     :time := 0 ns;
  VARIABLE var_296     :time := 0 ns;
  VARIABLE var_297     :time := 0 ns;
  VARIABLE var_310     :time := 0 ns;
  VARIABLE var_311     :time := 0 ns;
  VARIABLE var_301     :time := 0 ns;
  VARIABLE var_302     :time := 0 ns;
  VARIABLE var_284     :time := 0 ns;
  VARIABLE var_285     :time := 0 ns;
  VARIABLE var_314     :time := 0 ns;
  VARIABLE var_313     :time := 0 ns;
  VARIABLE var_304     :time := 0 ns;
  VARIABLE var_305     :time := 0 ns;    
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  VARIABLE var_293    :time := 0 ns; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  VARIABLE var_283   :time := 0 ns; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  VARIABLE var_309   :time := 0 ns; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  VARIABLE var_312  :time := 0 ns; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  BEGIN 

  IF vpen'event THEN --XX
    var_309  :=now;  --XX
  END IF;            --XX

  IF var_397'event THEN --XX
    var_312 :=now;  --XX
  END IF;            --XX

--XXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF ((ceb = '0') AND (ceb'last_value = '1') AND (ceb'event)) THEN  --XXXXXXXXXXXXXXXXXXXXXX
    var_289  := now;
    --XXXXXXXXX
    ASSERT ((now - var_310 ) >= twlel)
    REPORT "UI: Timing violation: WE# setup to CE# low."
    SEVERITY error;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    ELSIF ((web = '0') AND (ceb = '1') AND (ceb'last_value = '0') AND (ceb'event)) THEN  --XXXXXXXXXXXXXXXXXXXXX
    var_288  := now;
    --XXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    ASSERT ((var_288  - var_293) >= tdveh) --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    REPORT "UI Timing violation:  Data setup to CE# going high."
    SEVERITY error;
    --XXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    ASSERT ((var_288  - var_283 ) >= taveh) --XX
    REPORT "UI: Timing violation:  Address setup to CE# going high."
    SEVERITY error;
    --XXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    ASSERT ((var_288  - var_309 ) >= tvpeh) --XX
    REPORT "UI: Timing violation:  Vpen setup to CE# going high."
    SEVERITY error;
    --XXXXXXXXX
    ASSERT ((var_288  - var_310 ) >= twheh)
    REPORT "UI: Timing violation: CE# hold from WE# going high."
    SEVERITY error;
  END IF;  
   
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF ((ceb = '0') AND (web = '0') AND (web'last_value = '1') AND (web'event)) THEN  --XXXXXXXXXXXXXXXXXXXXXX
    var_311  := now;
    --XXXXXXXXXXXXXXXX
    ASSERT ((now - var_301 ) >= tphwl)
    REPORT "UI: Timing violation: RST# high recovery to WE# low."
    SEVERITY error;    
    --XXXXXXXXXXXXXXXX
    ASSERT ((now - var_289 ) >= telwl)
    REPORT "UI: Timing violation: CE# setup to WE# low."
    SEVERITY error;
    --XXXXXXXXX
    ASSERT ((now - var_310 ) >= twhwl)
    REPORT "UI: Timing violation:  WE# pulse width high."
    SEVERITY error;  
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  ELSIF ((ceb = '0') AND (web = '1') AND (web'last_value = '0') AND (web'event)) THEN  --XXXXXXXXXXXXXXXXXXXXX
    var_310  := now;
    --XXXXXXXXX
    ASSERT ((now - var_311 ) >= twlwh)
    REPORT "UI: Timing violation: WE# write pulse width low."
    SEVERITY error;
    --XXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    ASSERT ((var_310  - var_293) >= tdvwh) --XX
    REPORT "UI: Timing violation:  Data setup to WE# going high."
    SEVERITY error;
   --XXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    ASSERT ((var_310  - var_283 ) >= tavwh) --XX
    REPORT "UI: Timing violation:  Address setup to WE# going high."
    SEVERITY error;
    --XXXXXXXXX
    ASSERT ((var_310  - var_288 ) >= tehwh)
    REPORT "UI: Timing violation: WE# hold from CE# going high."
    SEVERITY error;
    --XXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    ASSERT ((var_310  - var_309 ) >= tvpwh) --XX
    REPORT "UI: Timing violation:  Vpen setup to WE# going high."
    SEVERITY error;
    --XXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    ASSERT ((var_310  - var_312) >= tbhwh) --XX
    REPORT "UI: Timing violation: WP# setup to WE# high."
    SEVERITY error;
  END IF;   

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF ((rpb = '0') AND (rpb'last_value = '1') AND (rpb'event)) THEN  --XXXXXXXXXXXXXXXXXXXXXX
    var_302  := now;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  ELSIF ((rpb = '1') AND (rpb'last_value = '0') AND (rpb'event)) THEN  --XXXXXXXXXXXXXXXXXXXXX
    var_301  := now;
  END IF;  

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF ((var_397 = '0') AND (var_397'last_value = '1') AND (var_397'event)) THEN  --XXXXXXXXXXXXXXXXXXXXXX
    var_314  := now;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  ELSIF ((var_397 = '1') AND (var_397'last_value = '0') AND (var_397'event)) THEN  --XXXXXXXXXXXXXXXXXXXXX
    var_313  := now;
  END IF; 

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF ((var_18 = '0') AND (var_18'last_value = '1') AND (var_18'event)) THEN  --XXXXXXXXXXXXXXXXXXXXXXX
    var_285   := now;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  ELSIF ((var_18 = '1') AND (var_18'last_value = '0') AND (var_18'event)) THEN  --XXXXXXXXXXXXXXXXXXXXX
    var_284   := now;
  END IF;   

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF ((oeb = '0') AND (oeb'last_value = '1') AND (oeb'event)) THEN  --XXXXXXXXXXXXXXXXXXXXXX
    var_297  := now;
    --XXXXXXXXXX
    ASSERT ((var_297  - var_310 ) >= twhgl)
    REPORT "UI: Timing violation:  Write recovery before read (WE# high)."
    SEVERITY error;

    ASSERT ((var_297  - var_288 ) >= tehgl)
    REPORT "UI: Timing violation:  Write recovery before read (CE# high)."
    SEVERITY error;

     
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  ELSIF ((oeb = '1') AND (oeb'last_value = '0') AND (oeb'event)) THEN  --XXXXXXXXXXXXXXXXXXXXX
    var_296  := now;
  END IF;
 
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF ((var_420 = '0') AND (var_420'last_value = '1') AND (var_420'event)) THEN  --XXXXXXXXXXXXXXXXXXXXXX
    var_305  := now;
    --XXXXXXXXXX
    ASSERT ((var_305  - var_310 ) <= twhrl)   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    REPORT "UI: Timing violation:  WE# high to STS going low."
    SEVERITY error;

    ASSERT ((var_305  - var_288 ) <= tehrl)   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    REPORT "UI: Timing violation:  CE# high to STS going low."
    SEVERITY error;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  ELSIF ((var_420 = '1') AND (var_420'last_value = '0') AND (var_420'event)) THEN  --XXXXXXXXXXXXXXXXXXXXX
    var_304  := now;
  END IF;

      --XXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF dq'event THEN --XX
    var_293 := now; --XXX
    ASSERT ((var_293 - var_310 ) >= twhdx) --XX
    REPORT "UI: Timing violation:  Data hold from WE# high."
    SEVERITY error;


 END IF;          --XX


    --XXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF addr'event THEN --XX
    var_283  := now; --XX
    ASSERT ((var_283  - var_310 ) >= twhax) --XX
    REPORT "UI: Timing violation:  Address hold from WE# high."
    SEVERITY error;

  END IF;            --XX


  END PROCESS timingcheck;



--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
var_335    : PROCESS(rpb,oeb,web,ceb,var_397,var_272   ,var_69,var_18,
                     var_271,vpen,var_344,addr)

    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_20        : std_logic_vector (maxaddrline DOWNTO 0); --XXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_21        : integer;                                 --XXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_387       : std_logic_vector (maxaddrline DOWNTO 0); --XXXXXXXXXXXXXXXXXXXX
    VARIABLE var_388       : integer;                                 --XXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_389       : word;                                    --XXXXXXXXXXXXXXXXX
    VARIABLE var_390       : integer;                                 --XXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_83        : std_logic_vector (7 DOWNTO 0);           --XXXXXXXXXXXXXXXXX
    VARIABLE var_84        : integer;                                 --XXXXXXXXXXXXXXXXXXXXXXXXXX

    VARIABLE var_98        : integer;
    VARIABLE var_35        : integer;
    VARIABLE var_400                 : integer := 0; 
    VARIABLE var_401          : integer := 0;
    VARIABLE var_402   :integer := writecmd;
    VARIABLE var_32       : std_logic_vector (maxaddrline DOWNTO 0);--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_87            : std_logic := '0';
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
       
    VARIABLE var_19          : std_logic ;
    VARIABLE var_198  : line;
    VARIABLE var_128    : integer;
    VARIABLE var_161 : bit :='0' ;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE address        : std_logic_vector (maxaddrline DOWNTO 0);
    VARIABLE var_281        : integer := 1;
    VARIABLE var_41         : integer := 0;
    VARIABLE var_206        : integer := 0;
    VARIABLE var_45         : integer := 0;
    VARIABLE var_149        : integer := 0;
    VARIABLE var_39         : bit     := '1';
    VARIABLE var_154        : bit     := '1';

   
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

  VARIABLE var_300  :time := 0 ns; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

BEGIN
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF (var_161 = '0') THEN
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_334     <= readarray; --XXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXX
    var_161 := '1';
  END IF; --XXXXXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXX
IF rpb'event THEN  --XX
  var_300 := now;  --XX
END IF;            --XX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXX
  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   IF (var_344 >= vcc1min) THEN  --XXXX
    IF (var_344 <= vcc1max) THEN
      var_346 <= '1';
    ELSE 
      var_346 <= '0';
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      --XXXXXXXXXXXXXXXXXXXXXX
    END IF;     
  ELSIF (now >= pollingdelay) THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    var_346 <= '0';
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXX
  END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF ((vccq < vccqlko) AND(now >= pollingdelay)) THEN  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    ASSERT false
    REPORT "UI: Vccq < Vccq Lockout Voltage - Data read is not guaranteed"
    SEVERITY warning;
  END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXX
      IF (var_18 = '0') THEN
        var_20  (maxaddrline DOWNTO 0) := addr(maxaddrline DOWNTO 0);  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        var_21      := vtoi(var_20  );
      END IF;
--XXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    IF ((web = vil) AND  (ceb = vil)) THEN       
        var_387(maxaddrline DOWNTO 0) := var_20  (maxaddrline DOWNTO 0);  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        var_388    := vtoi(var_387);
        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        var_389(maxdataline DOWNTO 0) := dq(maxdataline DOWNTO 0);
        var_390    := vtoi(var_389);
        var_83    (7 DOWNTO 0) := dq(7 DOWNTO 0);
        var_84        := vtoi(var_83    );                  
    END IF;
--XXXXXXXXX

IF (((web' event) AND (web = vih)) OR ((ceb' event) AND (ceb = vih))) THEN
    var_83    (7 DOWNTO 0) := dq(7 DOWNTO 0);
    var_84        := vtoi(var_83    ); --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
END IF;                 
 
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  CASE var_271 IS
  
  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  WHEN powerup =>
    
    IF (verbose = true ) THEN
      write(var_198  ,string'("We are in state PowerUp... "));
      writeline(var_199   ,var_198  );  --XX
    END IF;    
    IF (rpb'event) AND (rpb = vil) THEN      
      var_186    <= powerdown;         
    ELSIF (ceb = vil) THEN
      var_186    <= ceactive ;
    ELSIF (web = vil) THEN
      var_186    <= weactive ;
    ELSIF (oeb = vil) THEN
      var_186    <= inputdisable;
    ELSE
      var_186    <= powerup;
    END IF;


  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   WHEN inputdisable =>
     IF (verbose = true) THEN
       write(var_198  ,string'("We are in state InputDisable... "));
       writeline(var_199   ,var_198  );  --XX
     END IF;     
     IF (rpb = vil) THEN
       var_186    <= powerdown;
     ELSIF (oeb = vih) THEN
       IF ((ceb = vil) AND (web = vil)) THEN
         var_186    <= writecycle; 
         var_338      <= writeidle;   
       ELSIF (ceb = vil) THEN
         var_186    <= ceactive;
       ELSIF (web = vil) THEN
         var_186    <= weactive;
       END IF;
     END IF;
  

     
  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  WHEN ceactive =>
    IF (verbose = true) THEN
      write(var_198  ,string'("We are in state CEActive... "));
      writeline(var_199   ,var_198  );  --XX
    END IF;	 
    IF (rpb = vil) THEN                  
      var_186   <= powerdown;         
    ELSIF (ceb = vih) THEN 	    
      var_186   <= powerup;
    ELSIF (oeb = vil) THEN
      var_186    <= inputdisable;           
    ELSIF (web = vil) THEN	                     
      var_186   <= writecycle;
      var_338      <= writeidle;        
    END IF;


  --XXXXXXXXXXXXXXXXXXXXXXXXXXXX
  WHEN weactive =>        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    IF (verbose = true) THEN
      write(var_198  ,string'("We are in state WEActive.. "));
      writeline(var_199   ,var_198  );  --XX
    END IF;    
    IF (rpb = vil) THEN            
      var_186   <= powerdown;
    ELSIF (oeb = vil) THEN
      var_186    <= inputdisable;    
    ELSIF (web = vih) THEN         	     
      var_186   <= powerup;     
    ELSIF (ceb = vil) THEN         
      var_186   <= writecycle;            --XXXXXXXXXXXXXXXXXXX
      var_338      <= writeidle;      
    ELSE
      var_186   <= weactive;              --XXXXXXXXXXXXXXXXX
    END IF;


  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  WHEN writecycle =>   
    IF (verbose = true) THEN
      write(var_198  ,string'("We are in state WriteCycle "));
      writeline(var_199   ,var_198  );  --XX
    END IF;  
    IF (web = vih) AND (web'event) THEN                 
      IF (verbose = true) THEN
        write(var_198  ,string'("WriteCycle: WE controlled write ")); 
        writeline(var_199   ,var_198  );  --XX
      END IF;
      var_186   <= ceactive;    
    ELSIF (ceb = vih) THEN                   
      IF (verbose = true) THEN
        write(var_198  ,string'("WriteCycle: CE controlled write ")); 
        writeline(var_199   ,var_198  );  --XX
      END IF;
      var_186   <= weactive;            
    END IF;   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    
    IF (rpb = vil) THEN                
      var_186   <= powerdown;
    ELSIF (oeb = vil) THEN
      var_186    <= inputdisable;         
    ELSIF ((web = vih) OR (ceb = vih)) THEN               
      var_281    := targetmainarray;
      address := addr;
      calcblocknum(address,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,
                   var_154     );
      
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      CASE var_402   IS 
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        WHEN writecmd =>            
          IF (verbose = true ) THEN
             write(var_198  ,string'("Flash is in WriteCmd"));
             writeline(var_199   ,var_198  );		       --XX
          END IF;
          --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          CASE var_84        IS

            --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    WHEN readarraycmd =>	--XXXXXXXXXXX
              IF (verbose = true) THEN
                write(var_198  ,string'("Flash is doing Read Array command"));
                writeline(var_199   ,var_198  );	  --XX
              END IF; 
              IF (readwhilewrite = '0') THEN
                IF (var_418     = 0) THEN 
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                    var_334     <= readarray; --XXXXXXXXXXXX
                 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                   --XXXXX
                --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                 --XXXXXXXXXX
                 --XXXXXXXX
                ELSE
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  var_334     <= readstatusreg;  --XXXXXXXXXXX
                END IF;
              ELSIF (readwhilewrite = '1') THEN
                --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                --XXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  var_334     <= readarray;  --XXXXXXXXXXX
               --XXXXXXXX
              END IF;
              var_338      <= writeidle;

	    WHEN readidcmd =>
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing Read ID Codes command"));
                 writeline(var_199   ,var_198  );  --XX
              END IF;
              IF (var_418     = otpprogram) THEN                
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_334     <= readstatusreg;  --XXXXXXXXXXX
                ASSERT false
                REPORT "UI: Invalid command (ReadIDCmd) while device is OTP programing"
                SEVERITY error;
              ELSIF (readwhilewrite = '0') THEN
                IF (var_418     = 0) THEN 
                 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                    var_334     <= readidcodes;  --XXXXXXXXXXX
                --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXX
                   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXX
                  --XXXXXXX
                ELSE
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  var_334     <= readstatusreg;  --XXXXXXXXXXX
                END IF;
              ELSIF (readwhilewrite = '1') THEN
                --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
               --XXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  var_334     <= readidcodes;  --XXXXXXXXXXX
               --XXXXXXXX
              END IF;
              var_338      <= writeidle;
            
              
	    WHEN readquerycmd =>
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing Read Query command"));
                 writeline(var_199   ,var_198  );  --XX
              END IF;
              IF (var_418     = otpprogram) THEN                
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_334     <= readstatusreg;  --XXXXXXXXXXX
                ASSERT false
                REPORT "UI: Invalid command (ReadQueryCmd) while device is OTP programing"
                SEVERITY error;
              ELSIF (readwhilewrite = '0') THEN
                IF (var_418     = 0) THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
               --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                    var_334     <= readquery;  --XXXXXXXXXXX
                 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXX
                   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                   --XXXXXXXX
                 --XXXXXXXX
                ELSE
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  var_334     <= readstatusreg;  --XXXXXXXXXXX
                END IF;
              ELSIF (readwhilewrite = '1') THEN
                --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
               --XXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  var_334     <= readquery;  --XXXXXXXXXXX
               --XXXXXXXX
              END IF;
              var_338      <= writeidle;

	    WHEN readsrcmd => 
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing ReadStatus command"));
                 writeline(var_199   ,var_198  );  --XX
              END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_334     <= readstatusreg;  --XXXXXXXXXXX
              var_338      <= writeidle;
          
            WHEN clearsrcmd =>  --XXXXXXXXXXXXXXXXXXXXXX
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing Clear Status Register command"));
                 writeline(var_199   ,var_198  );   --XX
              END IF;
              IF (var_418     = 0 ) THEN
                var_338      <= writeclearsr;
                var_334     <= readarray;
              ELSE 
                ASSERT false
                REPORT "UI: Invalid command (ClearSRCmd) while device is busy"
                SEVERITY error;
                     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_334     <= readarray;
              END IF;
              --XXXXXXXXXXXXXXXXXXXXXXXXXX
            --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    WHEN programcmd =>	 --XXXXXXXXXXXXXXXX
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing Program command"));
                 writeline(var_199   ,var_198  );  --XX
              END IF;
              IF ((var_418     = 0) AND (var_417   = '0' AND var_409      = '0')) THEN     
                var_402   := writeprogram;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_334     <= readstatusreg;  --XXXXXXXXXXX
              ELSE                  
                ASSERT false 
                REPORT "UI: Invalid Command (ProgramCmd) Sequence While Device is busy"
                SEVERITY error;
              END IF;
              var_338      <= writeidle;

            WHEN program2cmd =>  --XXXXXXXXXXXXXXXXXXXXXXXXXX
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing Alternate Program command"));
                 writeline(var_199   ,var_198  );  --XX
              END IF;
              IF ((var_418     = 0) AND (var_417   = '0' AND var_409      = '0')) THEN     
                var_402   := writeprogram;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_334     <= readstatusreg;  --XXXXXXXXXXX
              ELSE                  
                ASSERT false 
                REPORT "UI: Invalid Command (Program2Cmd) Sequence While Device is busy"
                SEVERITY error;
              END IF;
              var_338      <= writeidle;

            WHEN program2buffcmd =>
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing Program to Buffer command"));
                 writeline(var_199   ,var_198  );  --XX
              END IF;
              IF ((var_418     = 0) AND (var_417  ='0' AND var_409     ='0' ) AND (var_265(4)='0' AND var_265(5)='0')) THEN     
                var_402   := writebuffer;
               --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_401          := 0;
                var_340         <= var_387;
		var_341    <= '0';          
              ELSE  
	        var_341    <= '1'; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                ASSERT false 
                REPORT "UI: Invalid Command (Program2BuffCmd) Sequence While Device is busy"
                SEVERITY error;
                var_402   := writecmd;    --XXXXX
              END IF;
              var_338      <= writeidle;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	      var_334     <= readstatusreg;  --XXXXXXXXXXX
	      

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
               
            --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            WHEN eraseblockcmd =>   --XXXXXXXXXXXXXXXXXXXXXXXXXXX
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing Block Erase command"));
                 writeline(var_199   ,var_198  );  --XX
              END IF;
              IF (var_418     = 0) THEN
             --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_402   := writeblockerase;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_334     <= readstatusreg;  --XXXXXXXXXXX
              ELSE                  
                ASSERT false 
                REPORT "UI: Invalid Command (EraseBlockCmd) Sequence While Device is busy"
                SEVERITY error;
              END IF;
              var_338      <= writeidle;

            --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            WHEN suspendcmd =>
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing Suspend command"));
                 writeline(var_199   ,var_198  );  --XX
              END IF;
              IF (var_418     /= 0) THEN
                var_338      <= writesuspend; --XXXXXXXXXXXXXXXXXXX
                var_402   := writecmd;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_334     <= readstatusreg;  --XXXXXXXXXXX
              ELSE 
                ASSERT false 
                REPORT "UI: Invalid Command (SuspendCmd) Sequence.  No operation is running"
                SEVERITY error;               
              END IF;                            
           
            WHEN resumecmd =>
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing Resume command"));
                 writeline(var_199   ,var_198  );  --XX
              END IF;
              IF (var_418     = 0) THEN
                IF (var_417   = '1') OR (var_409      = '1') THEN
                  var_338      <= writeprogram;
                  var_402   := writecmd;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  var_334     <= readstatusreg;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                ELSIF (var_412   = '1') THEN
                  var_338      <= writeblockerase;
                  var_402   := writecmd;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  var_334     <= readstatusreg;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                ELSE 
                  ASSERT false                   
                  REPORT "UI: Invalid Command (ResumeCmd) Sequence.  No operation is suspended"
                  SEVERITY error;
                END IF;
              ELSE
                ASSERT false 
                REPORT "UI: Invalid Command (ResumeCmd) Sequence.  Device is busy"
                SEVERITY error;
              END IF;
            --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            WHEN configlocksetupcmd =>
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing Configuration/Lock Setup command"));
                 writeline(var_199   ,var_198  );  --XX
              END IF;
              IF (((var_418     = 0) OR (var_412   = '1')) AND (var_417   /= '1' AND var_409     /='1')) THEN
                var_402   := writerdconfiglksetup;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_334     <= readstatusreg;  --XXXXXXXXXXX
              ELSE
                ASSERT false 
                REPORT "UI: Invalid Command (BlockLockSetupCmd) Sequence.  Device is busy"
                SEVERITY error;
              END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_334     <= readstatusreg;  --XXXXXXXXXXX
              var_338      <= writeidle;

            --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            WHEN programprcmd =>
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing Program Protection Register command"));
                 writeline(var_199   ,var_198  );  --XX
              END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_334     <= readstatusreg;  --XXXXXXXXXXX
              IF ((var_418     = 0) AND ((var_417   /= '1') AND (var_412   /= '1') AND (var_409      /= '1'))) THEN
                var_402   := writeprotectionreg;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

              ELSE
                ASSERT false 
                REPORT "UI: Invalid Command (ProgramPRCmd) Sequence.  Device is busy"
                SEVERITY error;
              END IF; 
              var_338      <= writeidle;

            --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            WHEN stsconfigsetupcmd =>
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                 write(var_198  ,string'("Flash is doing STS Configuration Setup command"));
                 writeline(var_199   ,var_198  );                             --XX
              END IF;
           --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	      IF (var_418     = 0) THEN --XXXXXXXXXXXXXXXXXXXXX
                var_402   := writestsconfig;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_334     <= readstatusreg;  --XXXXXXXXXXX
              ELSE
                ASSERT false 
                REPORT "UI: Invalid Command (STSConfigSetCmd) Sequence.  Device is busy"
                SEVERITY error;
              END IF; 
 
            --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            WHEN OTHERS =>
              ASSERT false 
              REPORT "UI: Unrecognized command written"
              SEVERITY error;
              IF (var_418     = 0) THEN
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_334     <= readarray;  --XXXXXXXXXXX
              END IF;
          END CASE;  --XXXXXXXXXXXXXXXXXXXXXXXXXX
       
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        WHEN writeprogram =>  
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                write(var_198  ,string'("Flash is in WriteType = WriteProgram "));
                writeline(var_199   ,var_198  );  --XX
              END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          var_334     <= readstatusreg;  --XXXXXXXXXXX
          var_338      <= writeprogram;
          var_340         <= var_387;
          var_339        <= var_389;
          var_402   := writecmd;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        WHEN writeblockerase =>
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
                write(var_198  ,string'("Flash is in WriteType = WriteBlockErase "));
                writeline(var_199   ,var_198  );  --XX
              END IF;
          IF (var_84        /= confirmcmd) THEN
            ASSERT false 
            REPORT "UI: Invalid Command (ConfirmCmd) Sequence."
            SEVERITY error;
            var_338      <= writesrbotch;
            var_402   := writecmd;
          ELSE 
            IF ((var_417   /= '1') AND (var_412   /= '1')) THEN
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_334     <= readstatusreg;   --XXXXXXXXXXX
              var_338      <= writeblockerase;
              var_340         <= var_387;
              var_402   := writecmd;
            ELSIF ((var_417   = '1') OR (var_412   = '1')) THEN
              ASSERT false
              REPORT "UI: Invalid Command (ConfirmCmd) Sequence."
              SEVERITY error;
              var_402   := writecmd;
            END IF;
          END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        WHEN writerdconfiglksetup =>
          IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
             write(var_198  ,string'("Flash is in WriteType = WriteRdConfigLkSetup"));
             writeline(var_199   ,var_198  );  --XX
              END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          CASE var_84        IS
            WHEN eclblocksetcmd =>
              var_338      <= writesetblocklock;
              var_340         <= var_387;
            WHEN eclblockclearcmd =>
              var_338      <= writeclearblocklock;
              var_340         <= var_387;
            WHEN eclblocksetmstrlkcmd =>
              IF (ldenable = true) THEN
                 var_338      <= writesetmasterlock;
                 var_340         <= var_387;
              ELSE
                 ASSERT false 
                 REPORT "UI: Unrecognized command written"
                 SEVERITY error;
              END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            WHEN OTHERS =>
              ASSERT false 
              REPORT "UI: Unrecognized command written"
              SEVERITY error;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          END CASE;
	  var_402   := writecmd;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        WHEN writeprotectionreg =>
          IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
             write(var_198  ,string'("Flash is in WriteType = WriteProtectionReg "));
             writeline(var_199   ,var_198  );  --XX
              END IF;
          var_338      <= writeprotectionreg;
          var_340         <= var_387;
	  var_339        <= var_389;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          var_334     <= readstatusreg;  --XXXXXXXXXXX
          var_402   := writecmd;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        WHEN writebuffer =>
          IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
             write(var_198  ,string'("Flash is in WriteType = WriteBuffer "));
             writeline(var_199   ,var_198  );  --XX
              END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          var_334     <= readstatusreg;  --XXXXXXXXXXX
	  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          IF (var_426(7) = '0') THEN
            ASSERT false
            REPORT "UI: Buffer is not available."
            SEVERITY error;
            var_402   := writecmd;
          ELSIF (var_426(7) /= '0') THEN
            var_400             := var_41  ;
            var_401          := var_84        + 1;  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_402   := writebufferdata;
          --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    var_338      <= writebuffer;
            var_339        <= itov(var_401         ,databuswidth);
          END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        WHEN writebufferdata =>
          IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
             write(var_198  ,string'("Flash is in WriteType = WriteBufferData "));
             writeline(var_199   ,var_198  );            --XX
              END IF;
          IF (var_418     = bufferwriting) THEN
            ASSERT false
            REPORT "UI: Invalid Write Command Sequence.  Device is in Buffer Writing."
            SEVERITY error;
          ELSIF (var_418     /= bufferwriting) THEN
            IF (var_401          > 1) THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              IF (var_41   = var_400            ) THEN
                var_340         <= var_387;
                var_339        <= var_389;
		var_338      <= writebufferdata;
                var_401          := var_401          - 1;
              ELSIF (var_41   /= var_400            ) THEN
                var_338      <= writebufferbotch;
		var_341    <= '1';
		var_402   := writecmd;--XXXXXXXXXXXXXXXXXXXXX
              END IF;
            ELSE --XXXXXXXXXXXXXXXXXXXXX
	     IF (var_41   = var_400            ) THEN
	      var_340         <= var_387;--XXXXXXXXXXXXXXXXXXXXX
              var_339        <= var_389;--XXXXXXXXXXXXXXXXXXXXX
              var_338      <= writebufferdata;--XXXXXXXXXXXXXXXXXXXXX
	      var_402   := writebufferconfirm;
	     ELSIF (var_41   /= var_400            ) THEN--XXXXXXXXXXXXXXXXXXXXX
                var_338      <= writebufferbotch;--XXXXXXXXXXXXXXXXXXXXX
		var_402   := writecmd;--XXXXXXXXXXXXXXXXXXXXX
		var_341    <= '1';
	     END IF;--XXXXXXXXXXXXXXXXXXXXX
            END IF;
          END IF;           
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        WHEN writebufferconfirm =>
          IF (verbose = true) THEN
            write(var_198  ,string'("Flash is in WriteType = WriteBufferConfirm "));
            writeline(var_199   ,var_198  );  --XX
          END IF;          
          IF (var_418     = bufferwriting) THEN
             IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
	       write(var_198  ,string'("Flash is in WriteType = WriteBufferConfirm, WUR_Running = BufferWriting "));
               writeline(var_199   ,var_198  );  --XX
              END IF;
            var_338      <= writeprogram;
            var_402   := writecmd;
          ELSIF (var_418     /= bufferwriting) THEN 
              IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
	        write(var_198  ,string'("Flash is in WriteType = WriteBufferConfirm, WUR_Running /= BufferWriting "));
                writeline(var_199   ,var_198  );        --XX
              END IF;
            IF (var_84        = confirmcmd)THEN
              IF (var_417   /= '1')THEN            --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                IF (verbose = true ) THEN --XXXXXXXXXXXXXXX
	              write(var_198  ,string'("Flash is in WriteType = WriteBufferConfirm, WUR_Running /= BufferWriting "));
                      writeline(var_199   ,var_198  );  --XX
              END IF;
                var_338      <= writeprogram;
                var_402   := writecmd;
              ELSIF (var_417   = '1') THEN          --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_338      <= writesrbotch;       --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_402   := writecmd;              --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                ASSERT false                        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                REPORT "UI:  Invalid Command (ConfirmCmd) Sequence.  A program is already suspended."
                SEVERITY error;                     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              END IF;
            ELSE
              var_338      <= writesrbotch;
              var_402   := writecmd; 
            END IF; 
          END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        WHEN writebefp =>
          IF (verbose = true) THEN
            write(var_198  ,string'("Flash is in WriteType = WriteBEFP "));
            writeline(var_199   ,var_198  );  --XX
          END IF;          
          IF (var_418     = bufferwriting) THEN
            ASSERT false
            REPORT "UI: Invalid Write Command Sequence.  Device is in BEFP Writing."
            SEVERITY error;
          ELSIF (var_418     /= bufferwriting) THEN         
            IF (var_84        = confirmcmd) THEN
              IF (var_417   /= '1') THEN
                var_338      <= writebefp;
                var_340         <= var_387;
                var_35       := var_41  ;
                var_402   := writebefpdata;
	        var_32       := var_387;
	        var_87            := '1';
              ELSIF (var_417   = '1') THEN          --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_338      <= writesrbotch;       --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_402   := writecmd;              --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                ASSERT false                        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                REPORT "UI:  Invalid Command (ConfirmCmd) Sequence.  A program is already suspended."
                SEVERITY error;                     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              END IF;      
            ELSE
              var_338      <= writesrbotch;
              var_402   := writecmd;
            END IF;
          END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        WHEN writebefpdata =>
          IF (verbose = true) THEN
            write(var_198  ,string'("Flash is in WriteType = WriteBEFPData "));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          IF (var_418     = bufferwriting) THEN
            var_338      <= writeprogram;
          ELSIF (var_418     /= bufferwriting) THEN  
            IF (var_41   /= var_35      ) THEN
              var_338      <= writebufferbotch;
              var_340         <= var_387;    --XXXXXXXX
              var_402   := writecmd;
            ELSE
	     IF (var_87            = '1') THEN--XXXXXXXXXXXXXXXXXX
	      var_87            := '0';--XXXXXXXXXXXXXX
	      var_338      <= writebefpdata;
	      var_339        <= var_389;
	     ELSE  --XXXXXXXXXXXXXX
              var_338      <= writebefpdata;
              var_339        <= var_389;
	      var_32       := itov(vtoi(var_32      ) + 1,maxaddrline+1); --XXXXXXXXXXXXXXXXXXXX
	      var_340         <=  var_32      ;--XXXXXXXXXXXXXX
	     END IF; --XXXXXXXXXXXXXX
            END IF;
          END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        WHEN writestsconfig =>
          IF (verbose = true) THEN
            write(var_198  ,string'("Flash is in WriteType = WriteSTSConfig "));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          CASE var_84        IS
	   WHEN stsconfigcode00cmd =>			  			  
            var_338      <= writestsconfig;
	    var_339        <= var_389;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
           WHEN stsconfigcode01cmd =>			  			  
            var_338      <= writestsconfig;
	    var_339        <= var_389;
           WHEN stsconfigcode10cmd =>			  			  
            var_338      <= writestsconfig;
	    var_339        <= var_389;
           WHEN stsconfigcode11cmd =>			  			  
            var_338      <= writestsconfig;
	    var_339        <= var_389;
           WHEN OTHERS =>
            var_338      <= writesrbotch;
            ASSERT false
            REPORT "UI: Unrecognized STS Configuration Code."
            SEVERITY error;
           END CASE;
          var_402   := writecmd;
          						    			 
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	WHEN OTHERS => --XXXXXXXXXXXXXXXXXXXXXXXXXXXX
          ASSERT false
          REPORT "UI: Unrecognized WriteType achieved."   
          SEVERITY failure;            
      END CASE;   --XXXXXXXXXXXXXXXXXXXX
    END IF; --XXXXXXXXXXXXXXX


  WHEN powerdown =>
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

    var_334     <= readarray; 
    var_338      <= writeidle;   

    IF ((rpb = vih) AND (now - var_300 >= tphqv)) THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_186    <= powerup;
    ELSE
      var_186    <= powerdown;
    END IF;

  WHEN OTHERS =>                        --XXXXXXXXXXXXXXXXXXXXXXXXX
    ASSERT false                     --XXXXXXXXXXXXXXXXXXXXXXX
    REPORT "UI: Unrecognized main state achieved"         --XXXXXXXXXXXXXX
    SEVERITY failure;
END CASE;   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXX

--XXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXX
var_337      <= var_402  ;


END PROCESS var_335    ;

END var_36  ;

CONFIGURATION var_330 OF var_329 IS
   FOR var_36   END FOR;
END var_330; 














--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX


LIBRARY ieee;
LIBRARY work;
USE ieee.std_logic_1164.ALL;
USE work.eas_parameters.ALL;
USE work.bfm_pkg.ALL;
USE work.io_pkg.ALL;
USE std.textio.ALL;

ENTITY var_403 IS
   PORT ( 
         a		 : IN std_logic_vector(maxaddrline DOWNTO 0); 
	 var_339       	 : IN std_logic_vector(maxdataline DOWNTO 0);
	 var_415     	 : INOUT std_logic_vector (maxaddrline DOWNTO 0);
	 var_368    	 : INOUT std_logic_vector (maxdataline DOWNTO 0);
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 var_67     	 : IN std_logic;
	 var_384              : IN std_logic;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 var_193  	 : IN integer;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 var_242    	 : IN std_logic;
 	 var_235  	         : IN real;
	 var_396     	 : IN std_logic;
	 var_346          : IN bit; --XXXXXXXXXXXX
 	 vpen	         : IN real;
         vccq            : IN real;  --XXXXXXXXXXXXXXXXXXXXXX
         var_239             : IN std_logic_vector(maxaddrline DOWNTO 0); --XXXXXXXXX
	 var_265		 : INOUT std_logic_vector(maxsrsize DOWNTO 0):=srready;
	 var_426		 : INOUT std_logic_vector(maxsrsize DOWNTO 0):=xsrbufffree;
	 var_418    	 : INOUT integer;
	 var_163         : INOUT lockconfigtype; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 var_164         : INOUT lockdowntype ; --XXXXXXXXXX
	 var_331         : INOUT unlockdowntype; --XXXXXXXXXXXXXXXXXXXXXXXXXX
         var_382                   : OUT std_logic_vector (maxaddrline DOWNTO 0); --XXXXXXXXX
         var_380       	 : OUT std_logic_vector (maxaddrline DOWNTO 0); --XXXXXXXXX
         var_381         : OUT std_logic_vector (maxaddrline DOWNTO 0); --XXXXXXXXXX
	 var_410     	 : OUT integer;
	 var_8           : OUT integer;
	 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 var_412  	 : INOUT bit;
	 var_417  	 : INOUT bit;
	 var_409     	 : INOUT bit;
	 var_276 	 : OUT bit;
	 var_341         : IN std_logic;         
         var_225      : IN integer;  					--XX
         var_241  : IN bit;						--XX
         var_224     : IN std_logic_vector(maxaddrline DOWNTO 0);	--XX
         var_22      : OUT std_logic_vector(maxdataline DOWNTO 0)	--XX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 );
	 
END var_403;

ARCHITECTURE var_405     OF var_403 IS
	
FILE var_199   : text IS OUT "STD_OUTPUT"; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
FILE  var_78    : text IS IN "initfile.dat";   --XX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   SIGNAL var_70         : bit := '0';
   SIGNAL var_73     	 : time;
   SIGNAL var_348         : std_logic;
   SIGNAL var_345        : std_logic;   --XXXXXXXXXXXXXXXXXXXXXXXX
   SIGNAL var_196     : std_logic_vector(17 DOWNTO 0);   --XX
   SIGNAL var_23          :  integer; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   SIGNAL var_406         :  bit := '0';   --XXXXXXXXXXXXXXXXXXXX
   SIGNAL var_364    		: databuffer; --XXXXXXXXX
   SIGNAL var_379     	        : addrbuffer; --XXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
TYPE addrmaxblkbuffer IS ARRAY(0 TO maxbuffsize) OF std_logic_vector(maxaddrline DOWNTO  blkaddrdecodebit); --XX
TYPE addrblockbuffer IS ARRAY(0 TO maxbuffsize) OF std_logic_vector(blkaddrdecodebit - 1 DOWNTO no_ofaddrline); --XXX
TYPE addrindexbuffer IS ARRAY(0 TO maxbuffsize) OF std_logic_vector(no_ofaddrline - 1 DOWNTO 0); --XX

BEGIN


var_50               : PROCESS(var_239            )
BEGIN
  var_381        <= var_379     (vtoi(var_239            (no_ofaddrline - 1 DOWNTO 0))); --XXXXXXXXX
END PROCESS var_50              ;

  var_403 : PROCESS(var_193  , vpen, var_346, var_242, var_67, var_235, var_396, var_384,var_70, var_418    )
    VARIABLE var_41   : integer;      --XXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_107    : integer; 
    VARIABLE var_217         : integer;
    VARIABLE var_108:boolean;
    VARIABLE var_243       : integer := -1;
    VARIABLE var_355              : time;
    VARIABLE var_353              : time;
    VARIABLE var_352              : time;
    VARIABLE var_354              : time;
    VARIABLE var_351              : time;
    VARIABLE var_349                : time;
    VARIABLE var_350                   : time;
    VARIABLE var_96             : time;
    VARIABLE var_97               : time;
    VARIABLE var_198  : line;
    VARIABLE var_206      : integer :=0;
    VARIABLE var_109            : integer := -1; --XXXXXXXXXXXXXXXXXXXX
    VARIABLE var_220            : integer := -1; --XXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_230  : integer;
    VARIABLE var_9          : bit_vector (maxpartitionnum DOWNTO 0); --XXXXXXXXXXXXXXXX
    VARIABLE var_281        : integer := targetotp;
    VARIABLE var_402   :integer := writecmd;
    VARIABLE var_85       : boolean:=false;
    VARIABLE var_209: bit := '0'; --XXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE rpb : std_logic;
    VARIABLE addr: std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE data: word;
    VARIABLE var_45        : integer;
    VARIABLE var_149    : integer;
    VARIABLE var_39     : bit;
    VARIABLE var_154     : bit;
    VARIABLE var_74 : integer;
    VARIABLE var_51     : integer;
    VARIABLE var_52    : integer;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE start_addr: std_logic_vector (maxaddrline DOWNTO 0);
    VARIABLE var_49: word;
    VARIABLE var_360            :addrmaxblkbuffer;  --XXXXXXXXX
    VARIABLE var_358           :addrblockbuffer;    --XXXXXXXXX
    VARIABLE var_359           :addrindexbuffer;    --XXXXXXXXXX
    VARIABLE var_357      :  addrbuffer;
    VARIABLE var_367      : databuffer;
    VARIABLE var_308      : time := 0 ns;
    VARIABLE var_181    : bit;
    VARIABLE var_428 : std_logic_vector (10 DOWNTO 0);
    VARIABLE var_192 : std_logic_vector (maxdataline DOWNTO 0);
    VARIABLE var_38   : integer:= 0;
    VARIABLE var_295        : time := 0 ns;
    VARIABLE var_294         :time := 0 ns;
    VARIABLE var_212          : time := 0 ns;
    VARIABLE var_299       : time := 0 ns;
    VARIABLE var_298        :time := 0 ns;
    VARIABLE var_213          : time:= 0 ns;
    VARIABLE var_210          : std_logic_vector (maxaddrline DOWNTO 0);
    VARIABLE var_211          : std_logic_vector (maxdataline DOWNTO 0);
    VARIABLE var_105          : std_logic_vector (maxaddrline DOWNTO 0);
    VARIABLE var_398  : integer :=0;
    VARIABLE var_134  : integer :=0;
    VARIABLE var_53        : bit;
    VARIABLE var_44    : integer;
    VARIABLE var_161 : bit := '0';
    VARIABLE var_42              : integer;
    VARIABLE var_245 :bit;
    VARIABLE var_222        : bit;
    VARIABLE var_173         : integer := maxbuffsize;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_221      : bit;
    VARIABLE var_306    : time := 0 ns;
    VARIABLE var_307     : time := 0 ns;
    VARIABLE var_214     : std_logic_vector (1 DOWNTO 0);
    VARIABLE var_33         : bit;
    VARIABLE var_275       :time := 0 ns;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_171           : integer := mainblocksize;	  --XXX
    VARIABLE var_34      : integer := 0;  --XXX
    VARIABLE var_266 : std_logic_vector(maxsrsize DOWNTO 0):= (OTHERS => '0');  --XXX
 
BEGIN

  rpb :=var_242;
  addr := a; 
  data:=var_339       ;	

  IF (var_161 = '0') OR (var_242 = '0') THEN 
    var_348 <= '1';
    var_418     <= wsm_idle;
    var_265 <= srready;
    var_245 := '1';
    var_161 := '1';
    var_276 <= '1';

     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXX
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        --XXXXXXXXXXXXXXXXXXXXX
        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      --XXXXXXXXXXXXXXXXXXXXXXXXXXX
  END IF;  
     
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF (vpen >= vpenlk) THEN    --XXXXXXXXX
    var_348 <= '1';
  ELSE
    var_348 <= '0';
    IF (verbose=true) THEN
       write(var_198  ,string'("Vpen is: "));
       write(var_198  ,real'(vpen));
       writeline(var_199   ,var_198  );  --XX
    END IF;
  END IF;
  
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF (vccq >= vccqlko) THEN   --XXXXXXXXXXXXXXX
    var_345 <= '1';
  ELSE 
    var_345 <= '0';
    IF (verbose=true) THEN
       write(var_198  , string'("Vccq is:  "));
       write(var_198  , real'(vccq));
       writeline(var_199   ,var_198  );
    END IF;
  END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF var_73     = 0 ns THEN
    var_406 <= '0';
    var_23   <= wsm_idle;
  END IF; 

  IF (var_242 = vil) THEN
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXX

--XXXXXXX
--XXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXX
--XXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXX
--XXXXXXXX
--XXXXXXXXXXX
  END IF;  	    
		
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF (var_41   >= 0 AND var_41   <= maxblocknum) THEN  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    IF (var_242=vih) AND (var_396=vil) AND (var_164 (var_41  )='1') THEN
      var_163   (var_41  )<='1';
    ELSIF (var_242=vih) AND (var_396=vil) AND (var_331   (var_41  )='1') THEN
      var_163   (var_41  )<='0';
    END IF;
  END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF var_193  'event THEN  			
    CASE var_193   IS    
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN wsm_idle => 
        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writeprogram => 
        IF (verbose = true) THEN
          write(var_198  ,string'("We're in WriteProgram."));
          writeline(var_199   ,var_198  );  --XX
        END IF;
        IF (var_53         = '0') THEN
          var_281    :=targetmainarray;
          calcblocknum(addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
        END IF;
        var_108 := false;  --XXXXXXXXXXXXXXXXXXXX

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_345 = '0') THEN
          ASSERT false
          REPORT "WSM: Insufficient Vccq for Programing"
          SEVERITY error;
          var_108 := true;
        END IF;        
       
        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_412   = '1') AND (var_41   = var_107   ) THEN   
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteProgram: in ESusp AND (BlockNum = ErasingBlk)."));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          var_265 <= var_265 OR srprogramfail;
          var_108:=true;
        END IF;        
               
        --XXXXXXXXXXXXXXXXXXXXX
           --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_348 = '0') THEN 
          ASSERT false
          REPORT "Attempt to program with vpen < vpenlk"
          SEVERITY error;                 
          var_265 <= var_265 OR srprogramfail OR srvppstaterror;
          var_108:=true;

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        ELSIF (var_163   (var_41  )='1' AND ((var_235 < vhhmin) OR (var_235 > vhhmax))) 
              AND var_417   = '0' THEN
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteProgram:(LockConfig(BlockNum)='1') ."));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          var_265 <= var_265 OR srprogramfail OR srdeviceprotect;
          var_108:=true;
        END IF;

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_242 = vil) OR (var_346 = '0') THEN
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteProgram:(rpb = vil) OR (VccSig = '0') ."));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          var_265 <= var_265 OR srprogramfail;
          var_108:=true;
        END IF;        

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_108 = false) THEN
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteProgram: ErrStat = false, everything is OK ."));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          IF var_245 = '1' THEN			
	    var_276 <= '0'; 	--XXXXXXXXXXXXXXXXXXXXXXX
          END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                           
          IF (var_53         = '1') THEN			
	    IF (var_409      ='1') AND (var_181    ='0') THEN		--XXXXXXX
	      var_23   <= writebufferprog;
              var_299       := now; 
              var_213          := twhqv3_3 - var_298       ;
              var_73     <= now + var_298       ;
              var_275       := now + var_298        - (tsts + 50 ns);
              var_418     <= bufferwriting;
              var_409      <= '0';
              var_281    := targetmainarray;
              var_265 <= var_265 AND (NOT srprogramsusp) AND (NOT srready);
              var_426 <= var_426 AND xsrbuffnotfree;
            ELSIF (var_409      = '1') AND (var_181    = '1') THEN	--XXXXXXX
              var_23   <= writebufferprog;
              var_299       := now; 		--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_213          := ((2*twhqv3_3) - var_298       ); --XXXXXXX
              IF (verbose = true) THEN
                write(var_198  ,string'("checking for time tWHQV:"));
                write(var_198  , time'(twhqv3_3));
                write(var_198  ,string'("checking for time : tWHQV * 2"));
                write(var_198  , time'(2*twhqv3_3));
                writeline(var_199   ,var_198  );  --XX
              END IF;
              var_73     <= now + var_298       ;	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_275       := now + var_298        - (tsts + 50 ns);
              var_418     <= bufferwriting;
              var_409      <= '0';			--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_379      <= var_357     ;		--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_364     <= var_367     ;
              var_265 <= var_265 AND (NOT srprogramsusp) AND (NOT srready);
              var_426 <= var_426 AND xsrbuffnotfree;		--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            ELSIF (var_181    = '1') THEN				--XXXXXXX
              var_23   <= writebufferprog;		
              var_299       := now;
              var_73     <= now + (2*twhqv3_3); --XXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_275       := now + (2*twhqv3_3) - (tsts + 50 ns);
              var_281    :=targetmainarray;
              calcblocknum(addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
              var_8        <= var_206     ;
              var_418     <= bufferwriting;
              var_379      <= var_357     ;
              var_364     <= var_367     ;
              var_265 <= var_265 AND (NOT srprogramsusp) AND (NOT srready);
              var_426 <= var_426 AND xsrbuffnotfree; 
            ELSIF (var_181    ='0') THEN				--XXXXXXX
	      var_23   <= writebufferprog;
	      var_299       := now;
              var_73     <= now + twhqv3_3; --XXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_275       := now + twhqv3_3 - (tsts + 50 ns);
              var_281    :=targetmainarray;
              calcblocknum(addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
              var_8        <= var_206     ;
              var_418     <= bufferwriting;
              var_379      <= var_357     ;
              var_364     <= var_367     ;
              var_265 <= var_265 AND (NOT srprogramsusp) AND (NOT srready);
              var_426 <= var_426 AND xsrbuffnotfree; 
            END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

          ELSIF (var_417   = '1') THEN				--XXXXXXX
	    var_23   <= writeprogram;
	    var_299       := now; 
	    var_213          := twhqv1_2  - var_298       ; --XXXXXXX
	    var_73     <= now + var_298       ;
            var_275       := now + var_298        - (tsts + 50 ns);
	    var_418     <= programing;
	    var_417   <= '0';
	    var_415      <= var_210          ;
	    var_368     <= var_211          ;
	    var_265 <= var_265 AND (NOT srprogramsusp) AND (NOT srready);
          ELSE							--XXXXXXX
            var_23   <= writeprogram;
            var_299       := now;
            var_73     <= now + twhqv1_2; --XXXXXXX
            var_275       := now + twhqv1_2 - (tsts + 50 ns);
            var_281    := targetmainarray;
	    calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
          --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_8        <= var_206     ;		
            var_243       := var_206     ;
            var_418     <= programing;
            var_210           := a;
            var_211           :=var_339       ;
            var_415      <= a;
            var_368     <= var_339       ;
            var_265 <= var_265 AND (NOT srready);
          END IF;
        END IF; --XXXXXXXXXXXXXXXXXXXXXX
               
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writeblockerase => 
        var_281    := targetmainarray;
        calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
        IF (verbose = true) THEN
          write(var_198  ,string'("We're in WriteBlkErase."));
          writeline(var_199   ,var_198  );  --XX
        END IF;
        var_108:=false;

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_345 = '0') THEN
          ASSERT false
          REPORT "WSM: Insufficient Vccq for Erasing"
          SEVERITY error;
        END IF;

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF  (var_348 = '0') THEN
          ASSERT false
          REPORT "Attempt to Erase block with Vpen<Vpenlk"
          SEVERITY error;  
          var_265 <= var_265 OR srerasefail OR srvppstaterror;
          var_108:=true;
        --XXXXXXXXXXXXXXXXXXXXXXX
        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        ELSIF (var_163   (var_41  )='1' AND ((var_235 < vhhmin) OR (var_235 > vhhmax))) 
             AND var_412   = '0' THEN
          IF (verbose = true) THEN
          write(var_198  ,string'("WriteBlkErase: looks like block is locked blocknum = "));
          write(var_198  , integer'(var_41  ));
          writeline(var_199   ,var_198  );  --XX
          END IF;
          var_265 <= var_265 OR srerasefail OR srdeviceprotect;
          var_108:=true;
        END IF;

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (rpb = vil) OR (var_346='0') THEN
          ASSERT false
          REPORT "WSM:  Attempt to Erase block in Powerdown mode"
          SEVERITY error;  
          var_265 <= var_265 OR srerasefail;
          var_108:=true;
        END IF;
 
        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF NOT (var_108) THEN
          IF var_245 = '1' THEN			
	    writeline(var_199   ,var_198  );  --XX
	    var_276 <= '0'; 	--XXXXXXXXXXXXXXXXXXXXXXX
	  END IF;
          IF (verbose = true) THEN
          write(var_198  ,string'("WriteBlkErase: ErrStat = False and we are calling EraseABlock for Blocknum."));
          write(var_198  , integer'(var_41  ));
          writeline(var_199   ,var_198  );  --XX
          END IF;
          IF (var_412   = '1') THEN
            var_23   <= writeblockerase;
	    var_295        := now; 
	    var_212           := twhqv3_2 - var_294        ;  --XXXXXXX
            var_73     <= now + var_294        ;
            var_275       := now + var_294         - (tsts + 50 ns);
            var_418     <= blockerasing;
            var_412   <= '0';
            var_415      <= var_105          ;
            var_265 <= var_265 AND (NOT srerasesusp) AND (NOT srready);
          ELSE
	    var_281    := targetmainarray;
	    calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );   

            --XXXXXXXXXXXXXXXXXXXXXXXXXXXX
            --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_164 (var_41  ) <= '0'; --XXX
            --XXXXXXXXXXXXXXXXXXXXXXXXXXXX

            var_107    := var_41  ;
	    var_410      <=var_41  ;
            var_23   <= writeblockerase;
            var_295        := now;
            var_73     <= now + twhqv3_2;
            var_275       := now + twhqv3_2 - (tsts + 50 ns);
            var_281    := targetmainarray;
            calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
            var_243       := var_206      ;
            var_8        <= var_206     ;
            var_418     <= blockerasing;
            var_105           := a;
            var_415      <= a;
            IF (verbose = true) THEN
              write(var_198  ,string'("WriteBlkErase: SR:= SR AND (NOT SRREADY) at time: "));
              write(var_198  , time'(now));
              writeline(var_199   ,var_198  );  --XX
            END IF;
            var_265 <= var_265 AND (NOT srready);
            IF (verbose = true) THEN
              write(var_198  ,string'("Completion = "));
              write(var_198  ,time'(var_73    ));
              write(var_198  ,string'("  NOW = "));
              write(var_198  ,time'(now));
              writeline(var_199   ,var_198  );  --XX
            END IF;
          END IF; 
        END IF;  --XXXXXXXXXXXXXXXXXXX
                
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     
      WHEN writesetmasterlock  => 
                      var_266 := var_265;
                      var_108:=false;

                      IF  (var_348 = '0') THEN
                        --XXXXXXXXXXXXXXXXXXXXXX
                        --XXXXXXXXXXXXXXXXXXXXX
                        --XXXXXXXXXXX
                          var_266 := var_266 OR srprogramfail OR srvppstaterror;
                          IF (verbose = true) THEN
                             write(var_198  ,string'("WriteSetMaster: looks like vpen is too low and errtat = true"));
                             writeline(var_199   ,var_198  );
                          END IF;
                          var_108:=true;

                      ELSIF ( (var_235 >= vhhmin) AND (var_235 <= vhhmax)) THEN
                           var_164 (masterlockindex) <= '1';  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                      ELSE 
				ASSERT (false)
   	                        REPORT "Attempted Set Master Lock with RST_a low"
				SEVERITY error;		      
			  var_266 := var_266 OR srprogramfail OR srerasefail OR srdeviceprotect;
                          var_108:=true;
                      END IF;
                      IF (var_108 = false) THEN
                           IF (verbose = true) THEN
                             write(var_198  ,string'("SetLockMaster: Errstat = false, everything is OK ."));
                             writeline(var_199   ,var_198  );
                           END IF;
                           --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                           var_73     <= now + twhqv5;
                           var_418     <= locksetting;
                           var_266 := var_266 AND (NOT srready);
                      END IF;

                      var_265 <= var_266;
                
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     
      WHEN writeclearblocklock  => 
	  var_281    := targetmainarray;

                      var_266 := var_265;
                      var_108:=false;

                      IF  (var_348 = '0') THEN
                        --XXXXXXXXXXXXXXXXXXXXXX
                        --XXXXXXXXXXXXXXXXXXXXX
                        --XXXXXXXXXXX
                        var_266 := var_266 OR srerasefail OR srvppstaterror;
                        IF (verbose = true) THEN
                           write(var_198  ,string'("WriteBlkClear: looks like vpen is too low and errstat = true"));
                           writeline(var_199   ,var_198  );
                        END IF;
                         var_108:=true;
                      END IF;

		      IF (var_164 (masterlockindex) ='0') THEN
                             FOR i IN 0 TO maxblocknum LOOP   --XXXXX
                              var_163   (i) <= '0';   --XXXXX
                             END LOOP;   --XXXXX
		  	ELSE
			IF (var_235>=vhhmin AND var_235<=vhhmax) THEN
                             FOR i IN 0 TO maxblocknum LOOP   --XXXXX
                              var_163   (i) <= '0';   --XXXXX
                             END LOOP;   --XXXXX
			ELSE 
				ASSERT(0>1)
   	                        REPORT "Attempted Block Clear with LockDown set and RST_a low"
				SEVERITY error;		      
			 var_266 := var_266 OR srerasefail OR srdeviceprotect;
                         var_108:=true;
			END IF;
		      END IF;	

                      IF (var_108 = false) THEN
                           IF (verbose = true) THEN
                             write(var_198  ,string'("ClearLockBlock: errstat = false, everything is OK ."));
                             writeline(var_199   ,var_198  );
                           END IF;
                           --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                           var_73     <= now + twhqv6;
                           var_418     <= lockclearing;
                           var_266 := var_266 AND (NOT srready);
                      END IF;

                      var_265 <= var_266;
			ASSERT(var_235<=vcc1max OR var_235>=vhhmin)
                            REPORT "Attempted Block Clear with unstable RST# level (Vih < RST# < VhhMin)"
                            SEVERITY error;		      
                
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     
      WHEN writesetblocklock  => 
	  var_281    := targetmainarray;
	  calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
                      var_266 := var_265;
                      var_108:=false;

                      IF  (var_348 = '0') THEN
                        --XXXXXXXXXXXXXXXXXXXXXX
                        --XXXXXXXXXXXXXXXXXXXXX
                        --XXXXXXXXXXX
                        var_266 := var_266 OR srerasefail OR srvppstaterror;
                        IF (verbose = true) THEN
                           write(var_198  ,string'("WriteBlkClear: looks like vpen is too low and errstat = true"));
                           writeline(var_199   ,var_198  );
                        END IF;
                         var_108:=true;
                      END IF;

                      IF (verbose =true) THEN
		         write(var_198  ,string'("We are doing SetBlockLock"));
		         write(var_198  ,integer'(var_41  ));
                         write(var_198  ,string'(" is "));                      
                         write(var_198  ,string'(" and LockConfig at the same blocknum is: "));
                         write(var_198  ,bit'(var_163   (var_41  )));
                         writeline(var_199   ,var_198  );
                      END IF;
		      IF (var_164 (masterlockindex) = '0') THEN 
			var_163   (var_41  ) <= '1';
   		      ELSE
                         IF (var_235>=vhhmin AND var_235<=vhhmax) THEN
			  var_163   (var_41  ) <='1';
		         ELSE 	                        
			   ASSERT(0>1)
                            REPORT "Attempted Block Set with MasterLock set and RST_a at invalid levels"
                            SEVERITY error;		      
			   var_266 := var_266 OR srprogramfail OR srdeviceprotect;
                           var_108:=true;
		         END IF;
		      END IF;

			ASSERT(var_235<=vcc1max OR var_235>=vhhmin)
                            REPORT "Attempted Block Set with unstable RST_a level (Vih < RST_a < VhhMin)"
                            SEVERITY error;		      

                      IF (var_108 = false) THEN
                           IF (verbose = true) THEN
                             write(var_198  ,string'("SetLockBlock: errstat = false, everything is OK ."));
                             writeline(var_199   ,var_198  );
                           END IF;
                           --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                           var_73     <= now + twhqv5;
                           var_418     <= locksetting;
                           var_266 := var_266 AND (NOT srready);
                      END IF;

                      var_265 <= var_266;
                
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writeblocklock  => 
	var_281    := targetmainarray;
	calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
        IF (verbose =true) THEN
          write(var_198  ,string'("We are doing BlockLock"));
          write(var_198  ,integer'(var_41  ));
          write(var_198  ,string'(" is "));                      
          write(var_198  ,string'(" and LockConfig at the same blocknum is: "));
          write(var_198  ,bit'(var_163   (var_41  )));
          writeline(var_199   ,var_198  );  --XX
        END IF;
        IF (var_396=vil) AND (var_331   (var_41  )='0')  THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	  var_163   (var_41  ) <= '1';
        ELSIF (var_396=vih) THEN
          var_163   (var_41  ) <='1';
        ELSE 	                        
          ASSERT false
          REPORT "Attempted Block Lock with UnlockDown set and WP low"
          SEVERITY error;		      
        END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writelockdown  =>
	var_281    := targetmainarray;
	calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
	IF (var_331   (var_41  )='0') THEN
          var_164 (var_41  ) <= '1';
	  var_163   (var_41  ) <= '1';
        ELSE
          ASSERT false
          REPORT "Attempted Lock down of an unlocked down block "
          SEVERITY error;	 
        END IF;
			
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writeunlockdown  =>		--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	var_281    := targetmainarray;
	calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
	IF (var_164 (var_41  )='0') THEN
          var_331   (var_41  ) <= '1';
          var_163   (var_41  ) <= '0';
        ELSE
          ASSERT false
          REPORT "Attempted Unlock down of a locked down block "
          SEVERITY error;	 
        END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writeunlock =>
        var_281    := targetmainarray;
	calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
	IF (var_396=vil) AND (var_164 (var_41  ) ='0') THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	  var_163   (var_41  ) <= '0';
	ELSIF (var_396=vih) THEN
	  var_163   (var_41  ) <='0';
	ELSE 
	  ASSERT false
          REPORT "Attempted Block Clear with LockDown set and WP low"
          SEVERITY error;		      
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	END IF;
      
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writebuffer =>
	var_51     := vtoi(var_339       );
	var_74 := vtoi(var_339       );
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	addr := a;
	var_281    := targetmainarray;
	calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
        IF (var_163   (var_41  )='1' AND ((var_235 < vhhmin) OR (var_235 > vhhmax))) THEN
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteProgram:(LockConfig(BlockNum)='1') ."));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          --XXXXXXXXXXXXXXXXXX
          var_265 <= var_265 OR srprogramfail OR srdeviceprotect;
          var_108:=true;
	END IF;
	var_117    : 
          FOR j IN 0 TO maxdataline LOOP  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    var_192(j) := '1';
	  END LOOP var_117    ;
	var_119      : 
          FOR i IN 0 TO maxbuffline LOOP
            var_367     (i) := var_192;
	  END LOOP var_119      ;
	var_418     <= buffercountloading;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writebufferdata =>		
        IF (verbose = true) THEN
          write(var_198  ,string'("We're in WriteBufferProgram."));
          writeline(var_199   ,var_198  );  --XX
        END IF;
        var_108:=false;

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_345 = '0') THEN
          ASSERT false
          REPORT "WSM: Insufficient Vccq for Buffer Data Streaming"
          SEVERITY error;
          var_108 := true;
        END IF;

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_348 = '0') THEN
          ASSERT false
          REPORT "Attempt to do buffered programming with vpen < vpenlk"
          SEVERITY error;  
          var_265 <= var_265 OR srprogramfail OR srvppstaterror;
          var_108:=true;
        END IF;

   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_412   = '1') AND (var_41   = var_107   ) THEN
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteBufferProgram: in ESusp AND (BlockNum = ErasingBlk)."));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          var_265 <= var_265 OR srprogramfail;
          var_108:=true;
        END IF;
        
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXX
        
        IF (var_242 = vil) OR (var_346 = '0') THEN
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteProgram:(rpb = vil) OR (VccSig = '0') ."));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          var_265 <= var_265 OR srprogramfail;
          var_108 := true;
        END IF;    

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_108 = false) THEN
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteBufferProgram: ErrStat = false, everything is OK ."));
            writeline(var_199   ,var_198  );  --XX
          END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXX
          IF (var_74 = var_51    ) THEN
            var_428 := "00000000000";  --XXXXXXXXXXXXXXXXXXXXXXX
            start_addr := a;         --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_134  := (vtoi(start_addr(maxaddrline DOWNTO no_ofaddrline)) + 1);
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_140                :
              FOR i IN 0 TO (maxbuffsize - 1) LOOP
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_360           (i)      --XXXXXXXXX
                        := start_addr(maxaddrline DOWNTO blkaddrdecodebit);
              END LOOP var_140                ;
            
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_358          (vtoi(a(no_ofaddrline - 1 DOWNTO 0)))  --XXXXXXXXX
                        := start_addr(blkaddrdecodebit - 1 DOWNTO no_ofaddrline);
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_359          (vtoi(a(no_ofaddrline - 1 DOWNTO 0))):= a(no_ofaddrline - 1 DOWNTO 0);
            var_367     (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) := var_367     (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) AND var_339       ;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    var_357     (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) := var_360           (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) & var_358          (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) & var_359          (vtoi(a(no_ofaddrline - 1 DOWNTO 0))); --XXXXXXXXX
            IF (start_addr(no_ofaddrline - 1 DOWNTO 0) = var_428(no_ofaddrline - 1 DOWNTO 0)) THEN
              var_181    := '0';
            END IF;
              var_51     := var_51     - 1;
          ELSIF ((var_51     < var_74) AND (var_51     >= 1)) THEN
            IF ((vtoi(a(no_ofaddrline - 1 DOWNTO 0))) < (vtoi(start_addr(no_ofaddrline - 1 DOWNTO 0)))) THEN
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_358          (vtoi(a(no_ofaddrline - 1 DOWNTO 0)))  --XXXXXXXXX
                         := itov(var_134 , (blkaddrdecodebit -  no_ofaddrline));
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_359          (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) := a(no_ofaddrline - 1 DOWNTO 0);  --XXXXXXXXX
              var_367     (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) := var_367     (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) AND var_339       ;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    var_357     (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) := var_360           (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) & var_358          (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) & var_359          (vtoi(a(no_ofaddrline - 1 DOWNTO 0))); --XXXXXXXXX
              var_181    := '1'; 
            ELSIF ((vtoi(a(no_ofaddrline - 1 DOWNTO 0))) >= (vtoi(start_addr(no_ofaddrline - 1 DOWNTO 0)))) THEN
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_358          (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) --XXXXXXXXX
                        := start_addr(blkaddrdecodebit - 1 DOWNTO no_ofaddrline);
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_359          (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) := a(no_ofaddrline - 1 DOWNTO 0); --XXXXXXXXX
              var_367     (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) := var_367     (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) AND var_339       ;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    var_357     (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) := var_360           (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) & var_358          (vtoi(a(no_ofaddrline - 1 DOWNTO 0))) & var_359          (vtoi(a(no_ofaddrline - 1 DOWNTO 0))); --XXXXXXXXX

            END IF;
            IF (var_51     = 1) THEN               
              var_53         := '1';
              var_52    :=vtoi(a(no_ofaddrline-1 DOWNTO 0));
              var_418     <= wsm_idle;
            ELSIF (var_51     /= 1) THEN
              var_52    := vtoi(a(no_ofaddrline - 1 DOWNTO 0));
              --XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
              var_418     <= data_streaming;
            END IF;
              var_51     := var_51     - 1;
          END IF;
        END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXX



--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXX
--
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXX
     
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writeprotectionreg =>
        IF (verbose = true) THEN
          write(var_198  ,string'("We're in WriteOTP."));
          writeline(var_199   ,var_198  );  --XX
        END IF;
        var_108:=false;

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_345 = '0') THEN
          ASSERT false          
          REPORT "WSM: Insufficient Vccq for Programing Protection Register"
          SEVERITY error;
          var_108 := true;
        END IF;

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_348 = '0') THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          var_265 <= var_265 OR srprogramfail OR srvppstaterror;
          var_108:=true;
          ASSERT false
          REPORT "WSM: Attempt to program the protection Register with vpen < vpenlk"
          SEVERITY error;
        END IF;

        --XXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_242 = vil) OR (var_346 = '0') THEN
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteOTP:(rpb = vil) OR (VccSig = '0') ."));
            writeline(var_199   ,var_198  );  --XX
          END IF;          
          var_265 <= var_265 OR srprogramfail;
          var_108:=true;
          ASSERT false
          REPORT "WSM: Attemp to program the Protection Register in Power Down "
          SEVERITY error;
        END IF;

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (vtoi(a(maxaddrline DOWNTO 0)) > maxotpaddr) OR (vtoi(a(maxaddrline DOWNTO 0)) < 128) THEN --XXXXXXXXXXXXXXXXXXXXX
	  var_265 <=var_265 OR srprogramfail;
	  var_108 :=true;
          ASSERT false
          REPORT "WSM: Attempt to program OTP address which is out of range (80h-109h)"
          SEVERITY error;
   	  IF (verbose = true) THEN
	    write(var_198  ,string'("WriteOTP: OTP out of range."));
	    writeline(var_199   ,var_198  );  --XX
	  END IF;
        ELSE
          --XXXXXXXXXXXXXXXXXXXXXXXXXXXX
          var_281    := targetotp;
	  calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
	  IF (var_41   /= 0) AND (var_41   /= 3) THEN	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    IF (var_41   = 1) THEN			--XXXXXXXXXXXXXXXXXXXXXXXXX
	      var_44     := 0;		--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    ELSIF (var_41   = 2) THEN		--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	      var_44     := 1;
	    ELSIF (var_41   > 2) OR (var_41   < 20) THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	      var_44     := var_41  -2;
	    END IF;		
	  --XXXXXXXXXXXXXXXXXXXXX
	    IF (var_196(var_44    ) = '0') THEN              
              var_265 <= var_265 OR srprogramfail OR srdeviceprotect;
	      var_108 :=true;
              ASSERT false
              REPORT "WSM: Attempt to program a LOCKED Protection Register"
              SEVERITY error;
	      IF (verbose = true) THEN
  	        write(var_198  ,string'("WriteOTP: OTP Block is locked."));
	        writeline(var_199   ,var_198  );  --XX
	      END IF;
	    END IF;	
	  END IF;
        END IF; 
   
	
        IF (var_108 = false) THEN
          IF (verbose = true) THEN
	    write(var_198  ,string'("WriteOTP: ErrStat = false, everything is OK ."));
	    writeline(var_199   ,var_198  );  --XX
	  END IF;
	    IF var_245 = '1' THEN			
	      var_276 <= '0'; 	--XXXXXXXXXXXXXXXXXXXXXXX
	  END IF;
          --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	  var_281    := targetotp;
	  calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
          var_23   <= writeotp;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          var_415      <=  a;	
	  var_368     <= var_339       ;	 
	  var_73     <= now + twhqv1_2;	
          var_275       := now + twhqv1_2 - (tsts + 50 ns);	
          var_418     <= otpprogram;
          var_265 <= var_265 AND (NOT srready);
        END IF; 

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writesuspend => 
	IF (var_418     = blockerasing) THEN
	  var_294         := twhqv3_2  - (now - var_295       ) - var_212          ; --XXXXXXXXXXXXXXXXXXXXXXXXXX
          var_42              := var_41  ;		
	  IF (var_73     /= 0 ns) THEN
	    var_418     <= susp_latency;	--XXXXXXXXXXXXXXXX
	    var_307     := now;		--XXXXXXXXXXXXXXXXXXXXXXXXXX
	    var_214     := "01";		
	  END IF;
	END IF;

	IF (var_418     = programing) THEN
          var_298        := twhqv1_2 - (now - var_299      ) - var_213         ;  --XXXXXXXXXXXXXXXXXXXXXXXXXXX
          IF (var_73     /= 0 ns) THEN
            var_418     <= susp_latency;	
	    var_307     := now;
            var_214     := "00";
	  END IF;
	END IF;

	IF (var_418     = bufferwriting) AND (var_181    = '1')THEN
          var_298        := ((2*twhqv3_3) - (now - var_299      ) - var_213         );
	  IF (var_73     /= 0 ns) THEN
	    var_418     <= susp_latency;
	    var_307     := now;
            var_214     := "10";
          --XXXXXXXXXXXXXXXXXXXXXXXXXX
          --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          END IF;
  	END IF;
	
	IF (var_418     = bufferwriting) AND (var_181    = '0')THEN
	  var_298        := twhqv3_3  - (now - var_299      ) - var_213         ;
	  IF (var_73     /= 0 ns) THEN
          --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          --XXXXXXXXXXXXXXXXXXXXXXXXXX
            var_418     <= susp_latency;
            var_307     := now;			
	  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_214     := "10";
	  END IF;
  	END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writebefp =>
        var_268               : 
          FOR i IN 0 TO (maxbuffsize-1) LOOP
            var_357     (i) := itov ((vtoi(a(maxaddrline DOWNTO 0))+i), maxaddrline+1);
          END LOOP var_268               ;         
        var_118     : 
          FOR j IN 0 TO maxdataline LOOP  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_192(j) := '1';
          END LOOP var_118     ;
        var_120       : 
          FOR i IN 0 TO maxbuffline LOOP
            var_367     (i) := var_192;
          END LOOP var_120       ;
        var_418     <= wsm_idle;
        var_51     := maxbuffsize;
        var_281    := targetmainarray;
        calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );
        var_33          := '0';
        var_265 <= srefpready;       
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        var_34      := vtoi(a(maxaddrline DOWNTO blkaddrdecodebit));  --XXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writebefpdata =>
        IF (verbose = true) THEN
          write(var_198  ,string'("We're in WriteBEFP."));
          writeline(var_199   ,var_198  );  --XX
        END IF;
        var_108:=false;

        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_345 = '0') THEN
          ASSERT false
          REPORT "WSM: Insufficient Vccq for BEFP Data streaming"
          SEVERITY error;
          var_108 := true;
        END IF;

        IF (var_412   = '1') AND (var_41   = var_107   ) THEN--XXXXXXXXXXXXXXXXXXXXXXXXXX
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteBEFP: in ESusp AND (BlockNum = ErasingBlk)."));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          var_265 <= var_265 OR srprogramfail;
          var_108:=true;
        END IF;
        
        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        IF (var_348 = '0') THEN 
          ASSERT false
          REPORT "WSM: Attempt to do Buffered EFP with (vpen < vpenlk)"
          SEVERITY error;
          var_265 <= var_265 OR srprogramfail OR srvppstaterror;
          var_108:=true;
        ELSIF (var_163   (var_41  )='1') THEN  --XXXXXXXXXXXXXXXXXX
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteProgram:(LockConfig(BlockNum)='1') ."));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          ASSERT false
          REPORT "WSM: Attempt to do Buffered EFP in a LOCKED block)"
          SEVERITY error;
          var_265 <= var_265 OR srprogramfail OR srdeviceprotect;
          var_108:=true;
        END IF;

        IF (var_418     = befpwriting) THEN
	  var_108 := true;
        END IF;
                
        IF (var_242 = vil) OR (var_346 = '0') THEN --XXXXXXXXXXXXXXXXXXXXXXX
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteBEFP:(rpb = vil) OR (VccSig = '0') ."));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          ASSERT false
          REPORT "WSM: Attempt to do Buffered EFP in a powerdown mode)"
          SEVERITY error;
          var_265 <= var_265 OR srprogramfail;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          var_108:=true;
          END IF;
               
        IF (var_108 = false) THEN
          
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          IF (verbose = true) THEN
            write(var_198  ,string'("WriteBEFP: ErrStat = false, everything is OK ."));
            writeline(var_199   ,var_198  );  --XX
          END IF;
          IF (var_51     = maxbuffsize) THEN
            IF (var_33          = '1') THEN
              IF (var_34      = vtoi(a(maxaddrline DOWNTO blkaddrdecodebit))) THEN
                start_addr := itov ((vtoi(var_357     (maxbuffsize-1))), maxaddrline + 1); --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                var_269              : 
                  FOR i IN 0 TO (maxbuffsize-1) LOOP --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                    var_357     (i) := start_addr(maxaddrline DOWNTO 16) & itov ((vtoi(start_addr(15 DOWNTO 0))+i+1), 16);
                  END LOOP var_269              ;
              ELSIF (var_34      /= vtoi(a(maxaddrline DOWNTO blkaddrdecodebit))) THEN
                IF (verbose = true) THEN
                  write(var_198  ,string'("WriteBEFP: Block address is changed during BEFP"));
                  writeline(var_199   ,var_198  );  --XX
                END IF;
                var_418     <= wsm_idle;
                var_265 <= srready;
              END IF;
            END IF;
          END IF;
  
          IF (var_51     /= 0) THEN
            var_52    :=vtoi(a(no_ofaddrline-1 DOWNTO 0)); 
            var_367     (var_52   ) := var_339       ;
            var_51     := var_51    -1;
            var_418     <= data_streaming;
            var_265 <= srefpready;
          END IF;		
          IF (var_51     = 0) AND (var_73     = 0 ns) THEN
            IF var_245 = '1' THEN			
              var_276 <= '0'; 	--XXXXXXXXXXXXXXXXXXXXXXX
            END IF;	
            var_379      <= var_357     ;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_364     <= var_367     ;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_23   <= writebuffer;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_73     <= now + tefplat1;
            var_275       := now + tefplat1 - (tsts + 50 ns);
            var_426 <= var_426 AND xsrbuffnotfree; 
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_243       := var_206      ;
            var_8        <= var_206     ;
            var_418     <= befpwriting;
	    var_265 <= srefpbusy;
	  END IF;
        END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writestsconfig =>
        IF (var_339       (1 DOWNTO 0) = "00") THEN
          var_245 := '1';
          var_221       := '0';
          var_222         := '0';
        ELSIF (var_339       (1 DOWNTO 0) = "01") THEN
          var_222         := '0';
          var_245 := '0';
          var_221       := '1';
        ELSIF (var_339       (1 DOWNTO 0) = "10") THEN
          var_221       := '0';
          var_245 := '0';
          var_222         := '1';
	ELSIF (var_339       (1 DOWNTO 0) = "11") THEN
	  var_222         := '1';
          var_221       := '1';
	  var_245 := '0';
	END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writesrbotch => 
        var_265 <= var_265 OR srbotch;	

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN writeclearsr => 
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          var_265 <= var_265 AND clearstatreg;
      --XXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

      WHEN writebufferbotch =>
          var_265 <= srready;
          var_418     <= wsm_idle;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN OTHERS => 
    END CASE;
  END IF; --XXXXXXXXXXXXX
  
  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF (var_418     /= wsm_idle) THEN  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_70 <= NOT(var_70) AFTER (pollingdelay * 20);--XXXXXXXXXXXXXXXXXXXX
  END IF; 


--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF ((now >= var_275      ) AND (var_275       /= 0 ns)) THEN
    IF ((var_418     = programing) OR (var_418     = blockerasing) OR 
        (var_418     = otpprogram) OR (var_418     = bufferwriting) OR 
        (var_418     = befpwriting))THEN
      IF (var_245 = '1') THEN
	var_276 <='1';
      END IF;
      IF ((var_418     =programing) AND (var_222         = '1')) THEN
        var_276 <= '0';
        var_306    := now;
      ELSIF ((var_418     = blockerasing) AND (var_221       = '1')) THEN
	var_276 <= '0';
        var_306    := now;	
      ELSIF ((var_418     = bufferwriting) AND (var_222         = '1')) THEN
        var_276 <= '0';
        var_306    := now;		
      ELSIF ((var_418     = befpwriting) AND (var_222         = '1')) THEN
        var_276 <= '0';
        var_306    := now; 
      ELSIF ((var_418     = otpprogram) AND (var_222         = '1')) THEN
        var_276 <= '0';
        var_306    := now; 
      END IF;	
      var_275       := 0 ns;
    END IF;
  END IF;

  IF (now - var_306    >= tsts) AND (var_222         ='1' OR var_221       ='1') THEN
    var_276 <= '1';
  END IF;

  IF (now >= var_73    ) AND (var_73     /= 0 ns) THEN
    IF (var_418     = programing) OR (var_418     = blockerasing) OR 
       (var_418     = otpprogram) OR (var_418     = bufferwriting) OR 
       (var_418     = locksetting) OR (var_418     = lockclearing) OR  --XXXX
       (var_418     = befpwriting) THEN
      var_406 <= '1';
      IF (var_418     =programing) THEN
        var_213          := 0 ns;
        var_418     <= wsm_idle;
        var_265 <= var_265 OR  srready;--XXXXXXXXXXXXXXXXXX
        IF var_245 = '1' THEN
	  var_276 <= '1';
        END IF;
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     --XXXXXXXXXXXXXXX
     --XXXXXXXXXXXXXXXXXXX
   --XXXXXXXXX
      ELSIF (var_418     = blockerasing) THEN
        var_212           := 0 ns;
        var_418     <= wsm_idle;
        var_265 <= srready;   --XXX
        IF var_245 = '1' THEN
          var_276 <='1';
	END IF;
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
       --XXXXXXXXXXXXXXX
       --XXXXXXXXXXXXXXXXXXXX
     --XXXXXXXX
      ELSIF (var_418     = bufferwriting) THEN
        var_418     <= wsm_idle;
        var_213          := 0 ns;
        var_53         := '0';
        var_134  := 0;
        var_398   := 0;
        var_265 <= var_265 OR srready;
        var_426 <= var_426 OR xsrbufffree; 
        IF var_245 = '1' THEN
          var_276 <= '1';
        END IF;
     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
       --XXXXXXXXXXXXXXX
       --XXXXXXXXXXXXXXXXXXX
     --XXXXXXXXX
      ELSIF (var_418     = befpwriting) THEN
        var_418     <= data_streaming;
        var_426 <= var_426 OR xsrbufffree;
        var_265 <= srefpready;
        var_51     := maxbuffsize;	
        var_33          := '1';
        IF var_245 = '1' THEN
          var_276 <= '1';
	END IF;
     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
       --XXXXXXXXXXXXXXX
       --XXXXXXXXXXXXXXXXXXX
     --XXXXXXXXX
      ELSIF (var_418     = otpprogram) THEN
        var_418     <= wsm_idle; 
        var_265 <= var_265 OR srready;
        IF var_245 = '1' THEN
          var_276 <= '1';
        END IF;
     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
       --XXXXXXXXXXXXXXX
       --XXXXXXXXXXXXXXXXXXX
     --XXXXXXXXX
      ELSIF (var_418     = locksetting) THEN
         var_418     <= wsm_idle; 
         var_265 <= var_265 OR srready;
      ELSIF (var_418     = lockclearing) THEN
         var_418     <= wsm_idle; 
         var_265 <= var_265 OR srready;
      END IF;	
    END IF;
    IF (verbose = true) THEN
      write(var_198  ,string'("Completion = "));
      write(var_198  ,time'(var_73    ));
      write(var_198  ,string'("  NOW = "));
      write(var_198  ,time'(now));
      writeline(var_199   ,var_198  );  --XX
    END IF;        
    var_73     <= 0 ns;
  --XXXXXXXXXXXXXXXXXXXXXXXX
    var_8        <= 0; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  END IF;

  IF var_418     = susp_latency AND (now -var_307     >= twhrh1_2) THEN
    var_418     <= wsm_idle;
    IF (var_214     = "01") THEN
      var_265 <= var_265 OR srerasesusp OR srready;
      var_412   <= '1';
      var_276 <= '1';
    ELSIF (var_214     = "00") THEN
      var_265 <= var_265 OR srprogramsusp OR srready;
      var_417   <= '1';
      var_276 <= '1';
    ELSIF (var_214     = "10") THEN
    var_265 <= var_265 OR srprogramsusp OR srready;
    var_426 <= var_426 AND xsrbuffnotfree; 
 --XXXXXXXXXXXXXXXXXX
    var_409      <= '1';--XXXXXXXXXXXXXXXXXXXXXXXXX
    var_276 <= '1';
    END IF;
  END IF;

  IF (var_341    = '1') THEN
    var_426 <= xsrbuffnotfree;
  ELSE
    var_426 <= xsrbufffree;
  END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  var_382                  <=  var_379     (maxbuffline); --XXXXXXXXX
  var_380        <=  var_379     (0); --XXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

     --XXXXXXXXXXXXXXXXXXXXXXXXXX
     --XXXXXXXXXXXXXXXXXXXXXXXXXXXX
     --XXXXXXXXXXXXXXXXXXXXXXXXXX
     IF (var_346 = '0') AND (var_418     = blockerasing) THEN --XXX
        var_281    := targetmainarray; --XXX
        calcblocknum (addr,var_281   ,var_41  ,var_206     ,var_45        ,var_149    ,var_39     ,var_154     );    --XXX
        var_164 (var_41  ) <= '1'; --XXX
     END IF; --XXX
     --XXXXXXXXXXXXXXXXXXXXXXXXXX

END PROCESS var_403;

--XXXXXXXXXXXXXXXXXXXXXXXXXXX
 
var_176 : PROCESS(var_23  , var_406, var_241)
    VARIABLE var_160 : line;
    VARIABLE var_175   : element_ptr;
    VARIABLE var_174       : element_ptr;
 
    VARIABLE var_170   : linkedarray_type;
    VARIABLE var_195  : linkedarray_type;
    VARIABLE var_392     : std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE var_393     : std_logic_vector(maxdataline DOWNTO 0);
 
    VARIABLE var_363       : addrbuffer;
    VARIABLE var_365         : databuffer ;
 
    VARIABLE var_161 : bit := '0';
    VARIABLE var_184   : line;
    VARIABLE var_12    : string(1 TO 6);
    VARIABLE var_77    : string(1 TO 5);
    VARIABLE var_137  : std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE var_138  : std_logic_vector(maxdataline DOWNTO 0);
 
    VARIABLE var_234         : std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE var_182 : std_logic_vector(maxdataline DOWNTO 0);
    VARIABLE var_197        : otp_lockarray ; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_41   : integer ;
    VARIABLE var_281    : integer ;
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_206      : integer;
    VARIABLE var_45         : integer;
    VARIABLE var_149     : integer;
    VARIABLE var_39      : bit;
    VARIABLE var_154      : bit;
 
BEGIN
var_392     := var_415     ;  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
var_393     := var_368    ;   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
var_363       := var_379     ;   --XXXXXXXXXXXXX
var_365         := var_364    ;
 
var_234         := var_224    ;
 
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 
IF (var_161 = '0') THEN
 --XXXXXXXXXXXXXXXXXXXXXXXXX
  var_281    := targetotp;
  var_137 := itov(16#80#, maxaddrline+1);
  var_138 := itov(16#FFFE#, maxdataline+1);
  writearrayproc(var_281   , var_137 , var_195 , var_138 );
 
  IF fileinitialize THEN
    var_281    := targetmainarray;
    var_136    : WHILE (NOT endfile(var_78  )) LOOP
                     readline(var_78  ,var_184);
                     read(var_184,var_12  );
                     read(var_184,var_77  );
                     hex_to_slv(var_12  , var_77  , var_137 , var_138 );
                     IF (verbose =true) THEN
                        write (var_160,string '("calling WAP with addr= "));
                        lput (var_160, var_137 ,hex);
                        write(var_160,string'(" data= "));
                        lput (var_160, var_138 ,hex);
                        writeline(var_199   ,var_160);  --XX
                     END IF;
                     writearrayproc(var_281   , var_137 , var_170  , var_138 );
                  END LOOP var_136   ;
  END IF;
 
  var_161:= '1';
END IF;
 
IF var_23  'event OR var_406'event THEN
    CASE var_23   IS
     WHEN writeprogram =>  --XXXXXXXXXXXXXXXXXXXXX
        IF var_406 = '1' AND var_406'event THEN
           var_281    := targetmainarray;
           writearrayproc(var_281   , var_392    , var_170  , var_393    );
        END IF;
     WHEN writeotp=> --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        var_281    := targetotp;
        IF var_406 = '1' AND var_406'event THEN
           var_281    := targetotp;
           writearrayproc(var_281   , var_392    , var_195 , var_393    );
        END IF;
     WHEN writeblockerase => --XXXXXXXXXXXXXX
        IF var_406 = '1' AND var_406'event THEN
           var_281    := targetmainarray;
           calcblocknum(var_392    ,var_281   ,var_41  ,var_206     ,var_45        ,var_149    , var_39     ,var_154     );
           eraseablockwnum(var_170  ,var_41  );
        END IF;
     WHEN writebufferprog =>
        IF var_406 = '1' AND var_406'event THEN
           var_281    := targetmainarray;
           FOR j IN 0 TO maxbuffline LOOP
             writearrayproc(var_281   , var_363      (j), var_170  , var_365        (j));
           END LOOP;
        END IF;
     WHEN OTHERS =>
    END CASE;
END IF;
 
 
IF var_241'event AND var_241='1' THEN
    IF var_225      = targetmainarray THEN
        var_281    := targetmainarray;
        readarrayproc(var_281   , var_234        , var_170  , var_182);
    ELSIF  var_225      = targetotp THEN
        var_281    := targetotp;
        readarrayproc(var_281   , var_234        , var_195 , var_182);
    ELSE
       IF (verbose =true) THEN
          write (var_160,string '("  Hey RSM - GOT you !!! "));
          writeline(var_199   ,var_160);  --XX
       END IF;
    END IF;
    var_22      <= var_182;  --XXXXXXXXXXXXXXXXXXXXXXXXXX
END IF;
 
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  var_281    := targetotp;
  var_137  := itov(16#80#,maxaddrline+1 );
  readarrayproc(var_281   , var_137 , var_195 , var_138 );
  var_196( 1 DOWNTO 0 ) <= var_138 (1 DOWNTO 0);
  var_137  := itov(16#89#, maxaddrline+1);
  readarrayproc(var_281   , var_137 , var_195 , var_138 );
  var_196( 17 DOWNTO 2 ) <= var_138 (15 DOWNTO 0);
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 
 END PROCESS var_176;


END var_405    ;

CONFIGURATION var_404 OF var_403 IS
   FOR var_405     END FOR;
END var_404; 




--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX


LIBRARY ieee;
LIBRARY work;
USE ieee.std_logic_1164.ALL;
USE work.eas_parameters.ALL;
USE work.io_pkg.ALL;
USE work.bfm_pkg.ALL;
USE std.textio.ALL;

ENTITY var_237 IS
   PORT (
       vccq             : IN real;    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
       var_190 		: IN std_logic;
       var_67 		: IN std_logic;
       var_242 		: IN std_logic;
       var_384 		: IN std_logic;
       a  		: IN std_logic_vector(maxaddrline DOWNTO 0);
       var_15   		: IN std_logic;
       var_69 		: IN std_logic;
       cr 		: IN std_logic_vector(maxcrsize DOWNTO 0);
       var_334     	: IN integer;
       var_22      	: IN std_logic_vector(databuswidth-1 DOWNTO 0);
       var_418    	: IN integer;
       var_408               	: IN integer;
       var_410          : IN integer;
       var_415     	: IN std_logic_vector(maxaddrline DOWNTO 0);
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
       var_382                  : IN std_logic_vector (maxaddrline DOWNTO 0);--XXXXXXXXXX
       var_380       	: IN std_logic_vector (maxaddrline DOWNTO 0);--XXXXXXXXXX
       var_381       	: IN std_logic_vector (maxaddrline DOWNTO 0);--XXXXXXXXX
       var_412  	: IN bit;
       var_417  	: IN bit;
       var_409     	: IN bit;
       var_413       	: IN lockconfigtype;
       var_414     	: IN lockdowntype;
       var_421       	: IN unlockdowntype;
       var_341          : IN std_logic;
       var_419		: IN std_logic_vector(maxsrsize DOWNTO 0);
       var_422		: IN std_logic_vector(maxsrsize DOWNTO 0);
       var_226     	: INOUT bit;
       var_239             : OUT std_logic_vector(maxaddrline DOWNTO 0); --XXXXXXXXX
       dq	  	: OUT std_logic_vector(databuswidth-1 DOWNTO 0);
       var_374  		: OUT std_logic;
       var_224     	: OUT std_logic_vector(maxaddrline DOWNTO 0);
       var_225     	: OUT integer);
END var_237;

ARCHITECTURE var_36   OF var_237 IS
FILE var_199   : text IS OUT "STD_OUTPUT"; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
SIGNAL var_70 : bit 		:= '0';
CONSTANT var_180    : integer 	:= 16#000000#;
CONSTANT idle : integer := 0;
SIGNAL var_186    : integer := idle;
SIGNAL var_271      : integer := idle;
SIGNAL var_46             : integer;
SIGNAL var_147         : integer;
BEGIN


var_218    : PROCESS (var_67,var_70) 
          --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
             VARIABLE var_290 : time := 0 ns;
          BEGIN
            IF var_67='1' AND var_67'event  THEN
              var_290 := now;
            END IF;  

            IF (to_x01(var_67)='0') OR (to_x01(var_190)='0') OR ( (now-var_290) <= tehqz) THEN 
               var_70 <= NOT(var_70) AFTER pollingdelay; --XXXXXXXXXXXXXXXXX
            ELSE
               --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
               --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
               var_70 <= '0';
            END IF;
          END PROCESS var_218    ; 


var_25     :PROCESS (var_186   )
    VARIABLE var_160 : line;
BEGIN
    IF (var_271 /= var_186   ) THEN
      IF (verbose = true) THEN
        write(var_160,time'(now));
        write(var_160,string'("new state "));
	lput(var_160,var_186   ,hex);
        writeline(var_199   ,var_160); --XX
      END IF;
      var_271        <= var_186   ;
    END IF;
END PROCESS var_25     ;



--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

var_229     : PROCESS(var_242,var_190,var_67,var_69,var_70,var_271,a,var_15,var_334    ,cr,var_384)
    VARIABLE var_228  : bit :='0';
    VARIABLE var_198  : line;
    VARIABLE var_277 : bit;
    VARIABLE var_203      : integer;
    VARIABLE var_287  : time := 0 ns; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_286  : time := 0 ns; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_310  : time := 0 ns; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_291   : time := 0 ns; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_204      : integer;
    VARIABLE var_17          : bit := '0';
    VARIABLE var_16           : bit := '0';
    VARIABLE targetmainarray : integer := 1;
    VARIABLE var_280        : integer := 2;
    VARIABLE var_206      : integer;
    VARIABLE var_279           : integer;
    VARIABLE var_41     : integer;
    VARIABLE var_45        : integer;
    VARIABLE var_39     :bit;
    VARIABLE var_148 : integer;
    VARIABLE var_154     :  bit;
    VARIABLE var_26       : std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE var_158         : std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE var_4 : std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE var_159              : std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE var_14                 : std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE var_244 : bit := '0';
    VARIABLE var_281    : integer;
    VARIABLE var_303 : time := 0 ns; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_123           : bit;
    VARIABLE var_202          : bit := '0';
    VARIABLE var_133 : std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE var_146     : integer;
    VARIABLE var_267     : std_logic_vector(maxsrsize DOWNTO 0);--XXXXXXXXXXXXXXXXXXX
    VARIABLE var_292 : time := 0 ns; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_121              : integer := 0;
    VARIABLE var_122                    : integer := 0;
    VARIABLE var_30                   : integer := 0;--XXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_201      : integer := 0;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_200         : bit := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_59      : integer;
    VARIABLE var_58     : integer := 0;
    VARIABLE var_139             : bit := '0';
    VARIABLE var_29                 : bit := '1';
    VARIABLE var_124                : bit := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_72         : std_logic;
    VARIABLE var_373       : std_logic;
    VARIABLE var_278            : bit := '0';
    VARIABLE var_375             : bit := '0';
    VARIABLE var_376             : bit := '0';
    VARIABLE var_377           : bit := '0';
    VARIABLE var_378           : bit := '0';
    VARIABLE var_248  : bit := '0';
    VARIABLE var_249             : bit := '0';
    VARIABLE var_250       : bit := '0';
    VARIABLE var_76            : integer;--XXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_61          : bit := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_56              : integer:= 16#0#;
    VARIABLE var_104      : integer;
    VARIABLE var_187           : integer;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_102            : integer:=0;--XXXXXXXXXXXXX
    VARIABLE var_103   : bit := '0';
    VARIABLE var_54     : bit := '0';
    VARIABLE var_150                   : integer;
    VARIABLE var_151           : integer;
    VARIABLE var_100 : bit := '0';
    VARIABLE var_101          : integer := 0;
    VARIABLE var_81    : bit := '0';
    VARIABLE var_131   : bit := '0';
    VARIABLE var_13              : integer;
    VARIABLE var_60        : integer;
    VARIABLE var_152          : bit := '0';
    VARIABLE var_240      : integer;
    VARIABLE var_231      : std_logic := '0';
    VARIABLE var_188              : std_logic := '0';
    VARIABLE var_385   : std_logic := '0';
    VARIABLE var_89 : std_logic := '0';
    VARIABLE var_90  : std_logic := '0';
    VARIABLE var_91  : std_logic := '0';
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_173         :integer := maxbuffsize;  --XXXXXXXXXX
    CONSTANT var_208         : integer := 15;
    ALIAS var_372     : std_logic IS cr(10);
    ALIAS var_80     : std_logic IS cr(9);
    ALIAS var_371               : std_logic IS cr(8);
    ALIAS var_55         : std_logic IS cr(7);
    ALIAS var_71       : std_logic IS cr(6);
    ALIAS var_57     : std_logic IS cr(3);
        
BEGIN

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
IF (pagesize > 1) THEN   
 var_204     :=pagesize;  
 findpowerof2(var_204     , var_203     );
END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
IF (syncenable > 0) THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 IF (cr(15) = '1') THEN
  var_277 := '0';
 ELSIF (cr(15) = '0') THEN
  var_277 := '1'; 
 END IF;
ELSE
  var_277 := '0';
END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 IF (var_384='1') AND (var_384'event) THEN
   var_310  := now;
 END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
IF (var_277 = '1') THEN
 var_121              := vtoi(cr(14 DOWNTO 11)); 
 IF ((var_121              < minlatency) OR (var_121              > maxlatency)) THEN
  ASSERT false
    REPORT "rsm: Latency Count Not Covered in the part"
  SEVERITY error;
 ELSIF (tavqv > (var_121             *tclk) + tavch + tchqv) THEN
  var_90  := '1';
  ASSERT false
    REPORT "rsm: Latency Setting Insufficient for Access Time"
  SEVERITY error;
 ELSIF (tavqv <= (var_121             *tclk) + tavch + tchqv) THEN 
  var_90  := '0';
 END IF; 
 IF (cr(2 DOWNTO 0) = "001") THEN
   var_59      := 4;
 ELSIF (cr(2 DOWNTO 0) = "010") THEN 
   var_59      := 8;
 ELSIF  (cr(2 DOWNTO 0) = "011") THEN
   var_59      := 16;
 ELSIF  (cr(2 DOWNTO 0) = "111") THEN
   var_59      := 99;
 ELSE
   ASSERT false
     REPORT "Invalid Burst Length Configuration"
   SEVERITY error;
 END IF;      
 IF (var_71       = '0') THEN
  var_72         := NOT(var_69);
 ELSIF (var_71       = '1') THEN
  var_72         := var_69;
 END IF; 
 IF (var_80     = '1') AND (var_371               = '1') THEN
   var_375             := '1';
   var_377           := '0';
   var_376             := '0';
   var_378           := '0'; 
 ELSIF (var_80     = '1') AND (var_371               = '0') THEN
   var_375             := '0';
   var_377           := '1'; 
   var_376             := '0';
   var_378           := '0'; 
 ELSIF (var_80     = '0') AND (var_371               = '1') THEN
   var_375             := '0';
   var_377           := '0';   
   var_376             := '1';
   var_378           := '0'; 
 ELSIF (var_80     = '0') AND (var_371               = '0') THEN
   var_375             := '0';
   var_377           := '0';   
   var_376             := '0';
   var_378           := '1'; 
 END IF; 
 IF (var_57     = '1' AND var_55         = '0') OR ((var_59      = 99 AND (var_55         = '0' OR var_57     = '0')))
     OR (var_59      = 4 OR var_59      = 99)  THEN
   var_152          := '1';
   ASSERT false
     REPORT "rsm: Invalid Configuration Register Combination"
   SEVERITY error;
 ELSE 
   var_152          := '0';  
 END IF; 
--XXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXX


 IF (var_190='0' AND var_190'event) THEN
  IF (var_286  < var_310 ) AND (var_231      ='1') THEN
    var_91  := '1';
  ELSE
    var_91  := '0';
  END IF;
 END IF;  

 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 IF  (var_384='1' AND var_384'last_event<2*pollingdelay) AND (var_334    /=var_240     )  THEN --XXXXXXXXXX
   var_231       := '1';
  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  --XXXXXXXXXXXXXXXXXXXXXXXXXXXX
 ELSIF (var_384='0' AND var_384'event) THEN
   var_231       := '0';
 END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
END IF;--XXXXXXXXXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
IF (var_15 = '0') THEN 
 IF (var_277 = '0') THEN
   IF (pagesize = 1) THEN
    var_26        := a;
    var_133 := var_26       ;
    var_146     := vtoi(var_133);
    var_224     <= var_26       ;
    calcblocknum(var_26       ,targetmainarray,var_41  ,var_206     ,var_45        , var_148, var_39     ,var_154     );
   ELSIF (pagesize > 1) THEN
    IF (a'event AND var_4(maxaddrline DOWNTO var_203     ) /= a(maxaddrline DOWNTO var_203     )) THEN
     var_123           := '1'; 
    END IF; 
     var_158         := a;
     var_239             <= a; --XXXXXXXXXX
     var_133 := var_158        ;
     var_146     := vtoi(var_133);
     var_224     <= var_158        ;
     calcblocknum(var_158        ,targetmainarray,var_41  ,var_206     ,var_45        , var_148, var_39     ,var_154     );
   END IF;
 ELSIF (var_277 = '1') THEN
   --XXXXXXXXXXXXXXXXXXXXXXXXXXX
 END IF;
END IF;       
 
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
IF (pagesize > 1) THEN 
 IF (var_15='0') AND (var_15'event) THEN --XXXXXXXXXXXXXXXXXXXXXX
  var_287  := now;
  var_16           := '1';
  var_17          := '0';
  var_122                    := 0;
  var_139             := '0';
  var_58     := 0;
  var_201      := 0;
  var_200         := '0';
  var_76            := 0;
  var_30                   := 0;
  var_278            := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  var_81    := '0';
  var_54     := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  var_248  := '0';
  var_250       := '0';
  var_249             := '0'; 
  var_89 := '0';  
  var_91  := '0';  --XX
  IF (var_67='0') THEN
  ASSERT ((now - var_286 ) >= tvhvl) --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   REPORT "rsm: ADV Pulse Width violation (R105 - tVHVL)"
   SEVERITY error;
  END IF;--XXXXXXXXXXXXXXXXXXXX
 ELSIF (var_15='1') AND (var_15'event) THEN--XXXXXXXXXXXXXXXXXXXXXXX
  var_286  := now;
  var_17          := '1';
  var_16           := '0';
  IF (var_67='0') THEN
   ASSERT (a'last_event >= tavvh)--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   REPORT "rsm: Address Setup to ADV high violation (R101 - tAVVH)"
   SEVERITY error;

 
   ASSERT ((now - var_287 ) >= tvlvh)--XXXXXXXXXXXXXXXXXXXXXXXX
   REPORT "rsm: ADV Pulse Width violation (R104 - tVLVH)"
   SEVERITY error;
 
   ASSERT ((var_67 /= '0') OR (var_67'last_event >= telvh))--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   REPORT "rsm: CE Low to ADV High Setup Time violation (R102 - tELVH)"
   SEVERITY error; 
  END IF;--XXXXXXXXXXXXXXXXXXXX
 END IF;--XXXXXXXXXXXXXXXXXXX
  IF (var_67='0') AND (a'event) AND (now-var_286 <=tvhax) AND (var_15 = '1' OR var_15 = '0') THEN --XXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   ASSERT false
   REPORT "rsm: Address hold from adv rising edge violation (R106 - tVHAX)"
   SEVERITY error;
  END IF;
END IF; 

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXX

IF (var_384 = '1' AND var_384'event) THEN   --XXXXXXXXXXXXXXXXXXXXXXX
  IF (var_190 = '0') THEN              --XXXXXXXXXXXXXXXXXXXXXXX
      var_385   := '1';           --XXXXXXXXXXXXXXXXXXXXXXX
  ELSE        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    var_385   := '0';             --XXXXXXXXXXXXXXXXXXXXXXX
  END IF;                         --XXXXXXXXXXXXXXXXXXXXXXX
END IF;                           --XXXXXXXXXXXXXXXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
IF (var_277 = '1') THEN --XXXXXXXXXXXXXXXXXX
 IF (var_67='0') AND (var_72         ='1' AND var_69'event)  THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     var_292 := now;--XXXXXXXXXXXXXXXXXXXXXXXX
     IF var_15 = '0' THEN  
	ASSERT (a'last_event >= tavch)--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        REPORT "rsm: Address Setup to CLK  violation (tAVCH -- R301)"
        SEVERITY error;
      END IF;
        ASSERT ((now - var_287 ) >= tvlch)--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        REPORT "rsm: ADV setup to clock violation (tVLCH--R302) this will cause the address to be latched on next clock"     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        SEVERITY error;   

        ASSERT ((var_67 /= '0') OR (var_67'last_event >= telch))--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        REPORT "rsm: CE Low to CLK Setup Time violation (tELCH--R303)"
        SEVERITY error;
 END IF; 
      IF var_67='0' AND a'event THEN	
        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	ASSERT  (now-var_292 >= tchax OR a'last_event < var_292)  --XXXXXXXXXXXXXXXXXXXXXXXXXXXX
	REPORT "rsm: Address Hold from Clock Edge Violation (tCHAX--R306)"
	SEVERITY error;
      END IF;
      
      IF (var_67='0' AND var_67'event) THEN
          var_291   := now;
          ASSERT (now - var_67'last_event > tehel)    --XXXXXXXXXXXXXXXXXXXXXXXXX
          REPORT "rsm: CE# High between Synchronous Reads violation (tEHEL--R11)"
          SEVERITY error; 
      END IF;    
END IF;   
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
IF (var_67='0' AND var_67'event) THEN
  var_291   := now;--XXXXXXXXXXXXX
END IF;  
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXX
 --XXXXXXXXXXXXXXXXX
 --XXXXXXXXXXXXXXXXXXXXX
 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXX
 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXX
 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXX
--XXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    
IF (var_277 = '0') AND (pagesize = 1) THEN
 var_26        := a;
 IF (maxpartitionnum>0) THEN
  var_133(var_208         DOWNTO 0) := var_26       (var_208         DOWNTO 0);
 ELSIF (maxpartitionnum=0) THEN
  var_133(maxaddrline DOWNTO 0) := var_26       (maxaddrline DOWNTO 0);
 ELSE
   ASSERT false
    REPORT "rsm: Check the value of MaxpartitionNum in eas_parameters!!!"
   SEVERITY failure;
 END IF; 
 var_146    := vtoi(var_133);  
 var_224     <= var_26       ;
 calcblocknum(var_26       ,targetmainarray,var_41  ,var_206     ,var_45        , var_148, var_39     ,var_154     ); 
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 IF (var_334     = readarray) THEN --XXXXXXXXXXX
  var_225      <= targetmainarray;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 ELSIF (var_334     = readidcodes) AND ((var_146    >=16#80#) AND (var_146    <=maxotpaddr)) THEN --XXXXXXXXXX
  var_225      <= var_280       ;
 END IF; 
ELSIF (var_277 = '0')  AND (pagesize>1) THEN
 IF (var_15 = '1' AND var_15'event) THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXX
  var_158        (maxaddrline DOWNTO var_203     ) := a(maxaddrline DOWNTO var_203     );
  var_158        (var_203     -1 DOWNTO 0) := a(var_203     -1 DOWNTO 0);
  var_239             <=  var_158        ; --XXXXXXXXXX
  var_159              := var_158        ;
  var_13              := vtoi(var_159             );
  var_278            := '1';
  IF (maxpartitionnum>0) THEN
   var_133(var_208         DOWNTO var_203     ) := var_158        (var_208         DOWNTO var_203     );
   var_133(var_203     -1 DOWNTO 0) := var_158        (var_203     -1 DOWNTO 0);
  ELSIF (maxpartitionnum=0) THEN
   var_133(maxaddrline DOWNTO var_203     ) := var_158        (maxaddrline DOWNTO var_203     );
   var_133(var_203     -1 DOWNTO 0) := var_158        (var_203     -1 DOWNTO 0);    
  ELSE
   ASSERT false
    REPORT "rsm: Check the value of MaxpartitionNum in eas_parameters!!!"
   SEVERITY failure;
  END IF;
  var_146    := vtoi(var_133);       
  var_224     <= var_158        ;
  calcblocknum(var_158        ,targetmainarray,var_41  ,var_206     ,var_45        , var_148, var_39     ,var_154     );
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  IF (var_334     = readarray) THEN --XXXXXXXXXX
   var_225      <= targetmainarray;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  ELSIF (var_334     = readidcodes) AND ((var_146    >=16#80#) AND (var_146    <=maxotpaddr)) THEN --XXXXXXXXXX
   var_225      <= var_280       ;
  END IF;  
  var_123           := '1';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 END IF;  
ELSIF (var_277 = '1') THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 IF ((var_15 = '1' AND var_15'event) OR ((var_72         = '1') AND (var_69'event) AND (var_15 = '0') AND (now-var_287 >=tvlch))) AND
     (var_278            = '0') THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   var_159              := a;--XXXXXXXXXXXXXXXX
  IF (maxpartitionnum>0) THEN
   var_133(var_208         DOWNTO 0) := var_159             (var_208         DOWNTO 0);
  ELSIF (maxpartitionnum=0) THEN
   var_133(maxaddrline DOWNTO 0) := var_159             (maxaddrline DOWNTO 0);    
  ELSE
   ASSERT false
    REPORT "rsm: Check the value of MaxpartitionNum in eas_parameters!!!"
   SEVERITY failure;
  END IF;
   var_146    := vtoi(var_133);       
   var_56              := vtoi(var_159             );
   var_13              := vtoi(var_159             );
   IF (var_55         = '0') THEN
    var_150                   := vtoi(var_159             );
   END IF; 
   calcblocknum(var_159             ,targetmainarray,var_41  ,var_206     ,var_45        , var_148, var_39     ,var_154     );
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   IF (var_334     = readarray) THEN --XXXXXXXXXX
     var_225      <= targetmainarray;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   ELSIF (var_334     = readidcodes) AND ((var_146    >=16#80#) AND (var_146    <=maxotpaddr)) THEN --XXXXXXXXXX
     var_225      <= var_280       ;
   END IF;       
   var_279           := var_206     ;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   var_240      := var_334    ; --XXXXXXXXXX
   var_278            := '1';
   var_122                    := -1;
   var_103   := '0';
 END IF;
END IF; 

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

IF(var_190='0' AND var_190'event) OR (var_67='0' AND var_67'event) THEN
  IF (var_419(maxsrsize) = '0') THEN					--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    IF ((var_418     = befpwriting) OR (var_418     = data_streaming)) THEN  
      var_267    := var_419;                                             --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_267   (maxsrsize-1 DOWNTO 1) := dqz(maxsrsize-1 DOWNTO 1);    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    ELSE   
      var_267    := var_419;                                             --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_267   (maxsrsize-1 DOWNTO 0) := dqz(maxsrsize-1 DOWNTO 0);
    END IF;                                                             --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  ELSE                                                                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    var_267    := var_419;                                               --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  END IF;                                                               --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
END IF;                                                                 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
IF (var_277='1') AND (var_152         ='0')THEN--XXXXXXXXXXXXXXXXXXXX
 IF (var_67='0') AND (var_72        ='1' AND var_69'event)  THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    ASSERT (now - var_69'last_event >= tclk) --XXXXXXXXXXXXXXXXXXXX
     REPORT "rsm: Clock period violation (R1 - tCLK)"
    SEVERITY error;
    
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    IF (var_334    /=readarray) AND (var_67 = '0' AND var_67'last_event >= telqv-tchqv) AND  --XXXXXXXXXX
       (var_242 = '1' AND var_242'last_event >= tphqv-tchqv) AND (var_190 = '0' AND var_190'last_event >= tglqv-tchqv) AND 
       (var_384 = '1' AND var_384'last_event >= twhqv-tchqv) AND (now-var_287 >=tavqv-tchqv) THEN
         var_188              := '1';
    ELSE
         var_188              := '0';
    END IF;   
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   
   IF (var_139             = '0' AND var_278            = '1') THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     var_54     := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     var_122                    := var_122                    + 1;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     IF (((var_378           = '1') OR (var_378           = '1')) AND (var_122                    < var_121             )) OR
        ((var_376             = '1') AND (var_122                    < var_121             -1)) OR
	((var_375             = '1') AND (var_122                    < var_121             -2)) THEN 
            var_373       := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     END IF;
     IF (var_122                    = var_121             -1) THEN
       var_139             := '1';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
       var_124                := '1';
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   
   ELSIF (var_139             = '1' AND var_278            = '1') THEN
    IF (var_80     = '1') AND (var_124               ='0') AND (var_100='0') AND (var_61         ='0') THEN--XXXXXXXXXXXXXXXXXXXXXXXXX
     var_76            := var_76           +1;
     IF (var_76            = multicycledatahold) THEN
      var_76            := 0;
     END IF; 
    END IF;
    IF (var_131  ='1') AND (var_76            = 0)  THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     var_131   := '0';
    END IF; 
    
    IF (var_54     = '0') THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      IF (((var_80    ='0') AND (var_59     >pagesize) AND (var_121              > pagesize)) OR
         ((var_80    ='1') AND (var_59     >pagesize) AND (var_121              > 2*pagesize) AND (maxlatency>2*pagesize)))
         AND (var_200        ='1' AND (var_80    ='0' OR (var_80    ='1' AND var_76           =0))) THEN--XXXXXXXXXX
	var_30                   := var_30                   + 1;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	IF (var_376             = '1') THEN
	 IF (var_30                   <= var_121             -pagesize-1) THEN
	  var_249             := '0';
	 END IF;
	ELSIF (var_375             = '1') THEN  
	 IF (var_30                   <= var_121             -pagesize-2) THEN
	  var_249             := '0';
	 END IF;
	END IF;
	IF (var_30                   <= var_121             -pagesize) THEN
	  var_61          := '1';
	  var_29                 := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	  var_81   :='0';--XXXXXXXXXXXXXXXXXXXXXXXX
	ELSE
	  var_61          := '0';
	  var_29                 := '1';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	  var_30                   := 0;--XXXXXXXXXXX
	  var_76            := 0;
	END IF; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	IF  (tavqv > (var_121             *tclk + tchqv)) THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	   var_90  := '1';--XXXXXXXXXXXXXXXX
	   ASSERT false
	     REPORT "rsm: Background Access Clocks Insufficient to meet Access time requirement!!!"
	   SEVERITY error;
	ELSE
	   var_90  := '0';
	END IF;      
      END IF;--XXXXXXXXXXXXXXXXXXXXXXXXXXX

     IF ((var_80     = '1') AND (var_76            = 0)) OR (var_80     = '0') THEN       
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      IF (var_124                = '0') AND (var_29                ='1') AND (var_100='0') THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	var_13              := var_13              + 1;
      ELSIF (var_124                = '1') THEN 
	var_124                := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      END IF;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      IF (var_29                 = '1') AND (var_100 = '0') THEN
       var_58     := var_58     + 1;--XXXXXXXXXXXXXXXXXXXXX
       IF (var_376            ='1') OR (var_375            ='1')  THEN
        IF (var_58     = var_59     ) THEN
	   var_248  := '1';
	 ELSE 
	   var_248  := '0';  
	 END IF;
       END IF;
       IF (var_201      = pagesize-1) THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	var_201      := 0;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	var_200         := '1';
       ELSE 
	var_201      := var_201     +1;
	IF (var_376            ='1') THEN
	 IF (var_201      = pagesize-1) AND (var_59     >pagesize) AND (var_121             > pagesize) THEN
	   var_249             := '1';
	 ELSE 
	   var_249             := '0'; 
	 END IF;  
	ELSIF (var_375            ='1') THEN
	 IF (var_201      = pagesize-2) AND (var_59     >pagesize) AND (var_121              > 2*pagesize) THEN
	   var_249             := '1';
	 ELSE 
	   var_249             := '0';  
	 END IF;  
	END IF; 
	var_200         := '0';
	var_30                   := 0;
       END IF;--XXXXXXXXXXXXXXXXXXX
       var_81    := '1';--XXXXXXXXXXX
       var_76            := 0;
       IF (var_59      /= 99) AND (var_58     > var_59     ) THEN--XXXXXXXXXXXXXXXXXXXXXX
        var_54     := '1';--XXXXXXXXXXXXXXXXXX
	var_139             := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXX
	var_278            := '0';
	var_58     := 0;
	var_81    := '0';
       ELSIF (var_59      = 99) THEN
        IF (var_13              = maxaddress) AND (wrap_around_maxaddress='0') THEN
	ELSIF (var_13              = maxaddress) AND (wrap_around_maxaddress='1') THEN 
	  var_13             := var_180   ;
	END IF;
       END IF;--XXXXXXXXXXXXXXXXXXXXXX
      ELSIF (var_29                 = '0') OR (var_100 = '0') THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 var_81    := '0';--XXXXXXXXXXXXXXXXXXXXXXX
      END IF; 

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

       IF (((var_56              MOD barrelshifterwidth) + pagesize) > barrelshifterwidth) THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	  var_104      := var_56              MOD barrelshifterwidth;
       ELSIF (((var_56              MOD barrelshifterwidth) + var_59     ) > barrelshifterwidth) THEN
        IF (var_200         = '1') THEN
          var_104      := (var_13              + 1) MOD barrelshifterwidth;
	END IF;
       ELSE 
         var_104      := 0;   	
       END IF;
       IF (var_104      + pagesize  > barrelshifterwidth) THEN--XXXXXXXXXXXXXX
        var_187           := var_121              - (barrelshifterwidth-var_104     );--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	IF (var_187          <0) THEN
	 var_187           := 0;
	END IF;
       ELSE
        var_187           := 0;	 
       END IF; 
       
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
       IF (var_187          /=0) AND (var_376            ='1' OR var_375            ='1') THEN
        IF ((var_13             +1) MOD barrelshifterwidth = 0) THEN
	  var_250       := '1';
	END IF; 
       ELSE
         var_250       := '0'; 
       END IF;      	 
               
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      IF (var_57    ='1') THEN	 
       IF (var_13              MOD barrelshifterwidth = 0) AND (var_187           /= 0) AND (var_103   = '0')  THEN
         var_100 := '1';--XXXXXXXXXXXXX
	 var_81    := '0';--XXXXXXXXXXXXXXXXXXXXX
       ELSE
         var_100 := '0';--XXXXXXXXXX
       END IF;   
     ELSIF (var_57    ='0') AND (var_59     /=99) THEN 
      var_100 := '0';  
      var_250       := '0';
      IF (var_55         = '1') THEN
       IF (var_13             /=vtoi(var_159             )) AND (var_13              MOD var_59      = 0) AND (var_13             >=var_59     ) THEN
         var_13              := var_13              - var_59     ;	 
       END IF;	 
      ELSIF (var_55        ='0') AND (var_54    ='0') THEN
        var_151           := var_150                   - (var_150                   MOD var_59     );
	var_60        := var_150                   MOD var_59     ;
        var_13              := var_151           + intelburst(var_60       , var_58    -1);
      END IF;	
     END IF;--XXXXXXXXXXXXXXXXXXX
     END IF;--XXXXXXXXXXXXXXXXXXXXXXX
    END IF;--XXXXXXXXXXXXXXXXXXXXXXXXX
   END IF;--XXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      IF (var_100 = '1') THEN
       var_102            := var_102            + 1;--XXXXXXXXXXXXXXXXXXXXXXXXXXX
       IF (var_376             = '1') THEN
        IF (var_102            = var_187          ) THEN
	 var_250       := '0';
	END IF;
       ELSIF (var_375             = '1') THEN
        IF (var_102            = var_187          -1) THEN
	 var_250       := '0';
	END IF;
       ELSE
         var_250       := '1';
       END IF;	 	 	 
       IF (var_102            = var_187          ) THEN
         var_100 := '0';--XXXXXXXXXXXXXXX
	 var_103   := '1';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 var_102            := 0;--XXXXXXXXXXXXXXXXXXXXXXXX
	 var_124                := '1';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 var_201      := 0;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 var_58     := var_58     - 1;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 var_76            := 0;
       END IF;
       IF (tavqv > (var_58     + var_187          )*tclk + tchqv) THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          var_90  := '1';
	  ASSERT false
	     REPORT "rsm: EOWL Clocks Insufficient to meet Access time requirement"
	   SEVERITY error;
       ELSE
          var_90  := '0';
       END IF;	  
      END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   var_14                 := itov(var_13             ,maxaddrline + 1);
   var_224     <= var_14                ;
   calcblocknum(var_14                ,targetmainarray,var_41  ,var_206     ,var_45        ,var_148,var_39     ,var_154     );     	
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   IF (var_334     = readarray) THEN --XXXXXXXXXX
     var_225      <= targetmainarray;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   ELSIF (var_334     = readidcodes) AND ((var_146    >=16#80#) AND (var_146    <=maxotpaddr)) THEN --XXXXXXXXXX
     var_225      <= var_280       ;
   END IF; 
   
   IF (var_418    =0) AND ((var_417  ='1' AND var_415     =var_14                ) 
      OR (var_412  ='1' AND var_410     =var_41  )
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      OR (var_409     ='1' AND (var_14                >=var_380        AND --XXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		 var_14                <=var_382                 ))) THEN  --XXXXXXXXXX
     var_89 := '1';
   END IF;
     
 END IF;--XXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 IF (var_139             = '0') AND (var_278            = '1') THEN
  IF (var_376             = '1') AND (var_122                    = var_121              - 1) AND (now-var_292>=tchtlh) THEN
     var_373       := '1';
  ELSIF (var_375             = '1') AND (var_122                    = var_121              - 2) AND
        (now-var_292>=tchtlh) THEN
     var_373       := '1';
  ELSIF ((var_377          ='1') OR (var_378          ='1')) AND (var_122                    = var_121             )AND (now-var_292>=tchtlh) THEN
     var_373       := '1';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  END IF;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 END IF;--XXXXXXXXXXXXXXXXXXXXX
END IF;--XXXXXXXXXXXXXXXX

CASE var_271 IS
      WHEN idle =>
       	IF (var_242 = vil) OR (var_67=vih) THEN    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    var_303 := now;       --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_186   <= idle;
	    var_374 <= 'Z'; 
	    dq <= dqz; 
	   IF (var_277 = '1') THEN 
	    var_122                    := 0;
            var_139             := '0';
            var_58     := 0;
            var_201      := 0;
            var_76            := 0;
            var_30                   := 0;
            var_278            := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_81    := '0';
            var_54     := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            var_248  := '0';
            var_250       := '0';
            var_249             := '0';
	   END IF;     
	ELSIF (var_242=vih) AND (var_67=vil) AND (var_190='1') THEN 
	    var_186    <= idle; 
	    dq <= dqz;
	    IF (now - var_291   >= teltl) THEN
	     var_373       := '0';
	    END IF;
	ELSIF (var_190='0') AND var_190'event  THEN                 
	   var_186   <= readcyclez;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	   IF (var_277 = '0') OR (var_334     /= readarray) THEN --XXXXXXXXXX
	     var_373       := '0';
	   END IF;  
        END IF;
       	

      WHEN readcyclez =>
	IF (verbose = true) THEN   
  	 write(var_198  ,string'("We are in state ReadCycleZ... "));
   	 writeline(var_199   ,var_198  );  --XX
	END IF;
	  dq <= dqz;   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	  IF var_242'last_event >= tphqv AND ((var_67 = vil AND var_67'last_event >= telqx) AND   
		 (var_190 = vil AND var_190'last_event >= tglqx)) THEN
	    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    --XXXXXXXXXXXXXXXXXX
	    var_186    <= readcyclex;
	  ELSIF (var_67 = vih) OR (var_190 = vih) THEN
	    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    var_186   <= idle;
	  ELSE 
	    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    var_186   <= readcyclez;
	  END IF; 
	  
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     WHEN readcyclex =>
	--XXXXXXXXXXXXXXXXXXXXXXXXX
   	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXX
	  dq <= dqx;     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	IF (var_242 = '0') THEN	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		--XXXXXXXXXXXXXXX
		var_186    <= idle;
	ELSIF (var_277 = '0') THEN  
	  IF ((var_242 = vih) AND (var_242'last_event >= tphqv)) AND      
	    ((var_67 = vil) AND (var_67'last_event >= telqv)) AND
	    ((var_190 = vil) AND (var_190'last_event >= tglqv)) AND
	    (((var_384 = vih) AND (var_384'last_event >= twhqv)) OR (var_385   = '1')) AND 
	    (((pagesize = 1 OR (var_123           = '1' AND pagesize > 1)) AND (a'last_event >= tavqv) AND
	    (now-var_287 >=tvlqv)) OR 
	    ((pagesize > 1 AND var_123           = '0') AND (a'last_event >= tapa)))  THEN
	      var_186   <= readcyclev;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	     IF  (var_334     = readarray) THEN --XXXXXXXXXX
	       var_225      <= targetmainarray;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	     ELSIF (var_334     = readidcodes) THEN --XXXXXXXXXX
	       var_225      <= var_280       ; 
	     END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	     IF (var_334     = readarray) OR ((var_334     = readidcodes) AND  --XXXXXXXXXX
	        ((var_146    >=16#80#) AND (var_146    <=maxotpaddr))) THEN
	      var_226    <= '1';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	     END IF; 
	  ELSIF (var_190 = vih AND var_190'last_event >= tghqz)   
	      OR (var_67 = vih AND var_67'last_event >= tehqz) THEN
	    var_186   <= readcyclez;
	  ELSE 
	    var_186   <= readcyclex;      --XXXXXXXXXXXXXXXXXXXX
	    IF (verbose = true ) THEN
   	          write(var_198  ,string'("Nothing Happened "));
   	         writeline(var_199   ,var_198  );  --XX
	    END IF;	    
	  END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 ELSIF(var_277 = '1') THEN
	  IF ((var_54    ='1') OR (var_29                ='0') OR (var_81   ='0')) AND (var_378          ='1' OR
	       var_377          ='1') AND (now-var_292>=tchtlh) THEN
	       	var_373       := '0';
	   END IF; 
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	  IF (var_139             = '0') AND (var_278            = '1') AND (var_334    =readarray) THEN  --XXXXXXXXXX
	   IF ((var_190 = '1' AND var_190'last_event >= tghqz) OR (var_67 = '1' AND var_67'last_event >= tehqz)) THEN
	     var_186    <= readcyclez;--XXXXXXXXXXXXXXXX
	   ELSE
	     var_186    <= readcyclex;	 
	   END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	  ELSIF (var_139             = '1') OR (var_334    /=readarray) THEN --XXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	   IF (var_190=vil) AND ((var_375            ='1') OR (var_376            ='1')) AND (var_334     = readarray) THEN --XXXXXXXXXX
	    IF (var_250       = '0' AND var_249             = '0' AND var_248  = '0') AND (now-var_292>=tchtlh) THEN
	     var_373       := '1';
	    ELSIF (var_250       = '1' OR var_249             = '1' OR var_248  = '1') AND (now-var_292>=tchtlh) THEN
	     var_373       := '0';
	    END IF;
	   END IF; 
	     IF (var_67 = '0' AND var_67'last_event >= telqv) AND 
	        (var_242='1' AND var_242'last_event >= tphqv) AND 
		(var_190='0' AND var_190'last_event >= tglqv) AND 
		((var_384='1' AND var_384'last_event >= twhqv) OR var_334    'last_event >=twhqv) AND
		(now-var_292>=tchqv) AND (((var_81    = '1') AND (var_152         = '0') AND
		(var_334    =readarray)) OR  (var_334    /=readarray AND var_188              = '1')) AND  --XXXXXXXXXX
		(now-var_287 >=tavqv) THEN
		     var_186    <= readcyclev;
		     var_131   := '1';
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	       IF  (var_334      = readarray) OR ((var_334      = readidcodes) AND  --XXXXXXXXXX
	          ((var_146    >=16#80#) AND (var_146    <=maxotpaddr))) THEN
	            var_226    <= '1';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		    IF ((var_377          ='1') OR (var_378          ='1')) AND (var_334      = readarray) THEN --XXXXXXXXXX
		      var_373       := '1';   
		    END IF;  
	       END IF; 
	     ELSIF ((var_190 = '1' AND var_190'last_event >= tghqz)--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		   OR (var_67 = '1' AND var_67'last_event >= tehqz)) THEN--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		var_186    <= readcyclez;		--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	     ELSE 
	        var_186    <= readcyclex;		--XXXXXXXXXXXXXXXXXXX
		--XXXXXXXXXXXXXXXXXXXXXX
	     END IF;
	  END IF;   
	 END IF;      
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      WHEN readcyclev =>   
	IF (verbose = true) THEN
  	 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  	 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	END IF;
	IF (vccq >= vccqlko) THEN                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        CASE var_334      IS            --XXXXXXXXXX
	    WHEN readarray => 
              IF (maxpartitionnum>0) AND ((var_418     /= 0) AND (var_206      = var_408               )) THEN
    		 dq <= dqx; 
	      ELSIF(maxpartitionnum=0) AND (var_418     /= 0) THEN
	         dq <= dqx;                  
	      ELSIF (var_277 = '0') AND (pagesize = 1) THEN 
		var_224     <= var_26       ;
		var_225      <= targetmainarray;
	        IF (var_418    =0) AND ((var_412  ='1' AND var_41  =var_410     ) 
		   OR (var_417  ='1' AND var_26       =var_415     ) 
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		   OR (var_409     ='1' AND (var_158        >=var_380        AND --XXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		   var_158        <=var_382                 ))) THEN   --XXXXXXXXXX
		   dq <= dqx; 
		ELSE
 		   dq(maxdataline DOWNTO 0) <= var_22     (maxdataline DOWNTO 0);
		END IF;
	      ELSIF (var_277 = '0') AND (pagesize > 1) THEN
	        var_224     <= var_158        (maxaddrline DOWNTO var_203     ) & a(var_203      - 1 DOWNTO 0);
		var_225      <= targetmainarray;
		dq(maxdataline DOWNTO 0) <= var_22     (maxdataline DOWNTO 0);
		IF (var_418    =0) AND ((var_412  ='1' AND var_41  =var_410     ) 
		   OR (var_417  ='1' AND var_158        =var_415     )  
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		   OR (var_409     ='1' AND --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		   var_158         = var_381       )) THEN  --XXXXXXXXX
		   dq <= dqx;  
		ELSE
 		   dq(maxdataline DOWNTO 0) <= var_22     (maxdataline DOWNTO 0);
		END IF; 
	      ELSIF (var_277 = '1') THEN
	        var_224     <= var_14                ;
		var_225      <= targetmainarray;
		dq(maxdataline DOWNTO 0) <= var_22     (maxdataline DOWNTO 0);    	  
		IF ((var_418    =0) AND ((var_417  ='1' OR var_412  ='1' OR var_409     ='1') AND (var_89 = '1'))) OR 
		   (var_90  = '1') OR (var_91  = '1')THEN  
		  dq <= dqx; 
		ELSE
 		   dq(maxdataline DOWNTO 0) <= var_22     (maxdataline DOWNTO 0);  
		END IF;
	      END IF; 
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    WHEN readidcodes =>
	      IF (var_418     = otpprogram) THEN --XXXXXXXXXXXXXXXXXX
	      
	      ELSIF (var_146    =0) THEN
		    dq <= itov(manufacturerid, databuswidth);--XXXXXXXXXXXXXXXXXXXXX
	      ELSIF (var_146    =1) THEN    
	      	    dq <= itov(deviceid,databuswidth);--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		                    --XXXXXXXXXXXXXXXXXXXXXXXXXXX
	      ELSIF (var_146    =var_45        +2) 	THEN
	          dq<=itov(16#0#,databuswidth);
	          IF(var_413       (var_41  )='1') THEN
		    dq(0)<='1';
		  ELSE
		    dq(0)<='0';
		  END IF;
		  IF(var_414     (var_41  )='1') THEN
		    dq(1)<='1';
		  ELSE
		    dq(1)<='0';
		  END IF;
		  IF(var_421       (var_41  )='1') THEN
		    dq(2)<='1';
		  ELSE
		    dq(2)<='0';
		  END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXX
	      ELSIF ((var_146    >=16#80#) AND (var_146    <=maxotpaddr)) THEN
	         var_224     <= a;
	         var_225      <= var_280       ;
		 dq <= var_22     ;
	      ELSE 
	         ASSERT false
	         REPORT "rsm: ReadIDCodes: Outside the known world."
                 SEVERITY error;
		 dq <= dqx;	 
	      END IF;	 
	      var_373       := '0';
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    WHEN readstatusreg => 
	       IF (verbose = true) THEN
                 write(var_198  ,string'("ReadStatReg"));
                 writeline(var_199   ,var_198  );  --XXXXXXXX
               END IF;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	       IF(databuswidth < maxsrsize) THEN
		  write(var_198  ,string'("ReadStatusReg: None of the products support MaxSRSize>Databuswidth now"));
                  writeline(var_199   ,var_198  );  --XXXXX
		  dq <= dqx;
	       ELSIF(databuswidth >= maxsrsize) THEN
		  dq(maxdataline DOWNTO maxsrsize+1) <= itov(16#0#,maxdataline-maxsrsize);    
	       END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	        IF (var_418     /= 0) THEN
	         IF(maxpartitionnum>0) THEN
	            dq(maxsrsize DOWNTO 1)<=var_267   (maxsrsize DOWNTO 1);
		   IF(var_206      = var_408               ) THEN
		     dq(0) <= '0';
		   ELSIF (var_206      /= var_408               ) THEN
                     dq(0) <= '1';
		   ELSIF (var_418     = befpwriting) THEN--XXXXXXXX
		     dq(maxsrsize DOWNTO 0)<=var_267   (maxsrsize DOWNTO 0);
		   END IF; 
		 ELSIF(maxpartitionnum=0) THEN
		   IF (var_418     = buffercountloading) OR (var_341    = '1') THEN  --XXXXXXXXXX
		    dq(maxsrsize DOWNTO 0) <= var_422;
		   ELSE
		    dq(maxsrsize DOWNTO 0)<=var_267   (maxsrsize DOWNTO 0);
		   END IF;  
		 ELSE
		     ASSERT false
		      REPORT("Check EAS Parameters File and assign a proper value to MaxPartitionNum ")
		      SEVERITY warning;
		 END IF;    
	       ELSE 
	         IF (var_341   ='1') THEN
		  dq(maxsrsize DOWNTO 0) <= var_422;
		 ELSE
		  dq(maxsrsize DOWNTO 0)<=var_267   (maxsrsize DOWNTO 0); 
		 END IF; 	  
               END IF; 
	       var_373       := '0';
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            WHEN readquery =>
	      IF(var_418    =otpprogram) THEN--XXXXXXXXXXXXXXXXXXXX
	        dq <= dqx;
	      ELSE 
	       IF (pagesize>1) AND (var_277='0') THEN
	        IF(maxpartitionnum=0) THEN
	         var_133 := var_133(maxaddrline DOWNTO var_203     ) & a(var_203     -1 DOWNTO 0);
		ELSIF (maxpartitionnum>0) THEN 
		 var_133(var_208         DOWNTO 0) := var_133(var_208         DOWNTO var_203     ) & a(var_203     -1 DOWNTO 0);
		END IF;
		var_146     := vtoi(var_133);
	       END IF;	
	        CASE var_146     IS
		  WHEN 16#10# => dq <= itov(16#51#,databuswidth);	 --XXXXXXXXX
		  WHEN 16#11# => dq <= itov(16#52#,databuswidth);	 --XXXXXXXXX
		  WHEN 16#12# => dq <= itov(16#59#,databuswidth);	 --XXXXXXXXX
		  WHEN 16#13# => dq <= itov(16#01#,databuswidth);	 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#14# => dq <= itov(16#00#,databuswidth);	 --XXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#15# => dq <= itov(16#31#,databuswidth);	 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#16# => dq <= itov(16#00#,databuswidth);	 --XXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#17# => dq <= itov(16#00#,databuswidth);	 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#18# => dq <= itov(16#00#,databuswidth);	 --XXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#19# => dq <= itov(16#00#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#1A# => dq <= itov(16#00#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#1B# => dq <= itov(16#27#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#1C# => dq <= itov(16#36#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#1D# => dq <= itov(16#00#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#1E# => dq <= itov(16#00#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#1F# => dq <= itov(16#08#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

		  WHEN 16#20# => dq <= itov(16#08#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#21# => dq <= itov(16#0B#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#22# => dq <= itov(16#00#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#23# => dq <= itov(16#02#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#24# => dq <= itov(16#02#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#25# => dq <= itov(16#03#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#26# => dq <= itov(16#00#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#27# => dq <= itov(16#18#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#28# => dq <= itov(16#02#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#29# => dq <= itov(16#00#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#2A# => dq <= itov(16#05#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#2B# => dq <= itov(16#00#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 		  WHEN 16#2C# => dq <= itov(16#01#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#2D# => dq <= itov(16#7F#,databuswidth);	 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#2E# => dq <= itov(16#00#,databuswidth); 	 --XXXXXXXXXXX
		  WHEN 16#2F# => dq <= itov(16#00#,databuswidth);			 --XXXX

		  WHEN 16#30# => dq <= itov(16#02#,databuswidth);						 --XXXX
		  WHEN 16#31# => dq <= itov(16#50#,databuswidth);	 --XXXXXXXXXXXXXXXX
		  WHEN 16#32# => dq <= itov(16#52#,databuswidth);	 --XXXXXXXXXXXXXXXX
		  WHEN 16#33# => dq <= itov(16#49#,databuswidth);	 --XXXXXXXXXXXXXXXX
		  WHEN 16#34# => dq <= itov(16#31#,databuswidth);	 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#35# => dq <= itov(16#31#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#36# => dq <= itov(16#CE#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#37# => dq <= itov(16#00#,databuswidth); 	 --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#38# => dq <= itov(16#00#,databuswidth); --XXXX
		  WHEN 16#39# => dq <= itov(16#00#,databuswidth);         --XXXX
		  WHEN 16#3A# => dq <= itov(16#01#,databuswidth);         --XXXX
		  WHEN 16#3B# => dq <= itov(16#01#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#3C# => dq <= itov(16#00#,databuswidth);         --XXXX
		  WHEN 16#3D# => dq <= itov(16#33#,databuswidth);         --XXXX
		  WHEN 16#3E# => dq <= itov(16#00#,databuswidth);         --XXXX
		  WHEN 16#3F# => dq <= itov(16#01#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		
		  WHEN 16#40# => dq <= itov(16#80#,databuswidth);        --XXXX
		  WHEN 16#41# => dq <= itov(16#00#,databuswidth);        --XXXX
		  WHEN 16#42# => dq <= itov(16#03#,databuswidth);        --XXXX
		  WHEN 16#43# => dq <= itov(16#03#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

		  WHEN 16#44# => dq <= itov(16#03#,databuswidth);        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN 16#45# => dq <= itov(16#00#,databuswidth);        --XXXXXXXXXXXXXXX

                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  WHEN OTHERS  => dq <= dqx;	
	      	END CASE;
		  var_373       := '0';  
	      END IF;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	    WHEN OTHERS =>
	      ASSERT false 
	        REPORT("Not a Valid ReadType")
		SEVERITY warning;                  
	  END CASE;--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          ELSIF (vccq < vccqlko) THEN                            --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            dq <= dqx;                                           --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            ASSERT false                                         --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            REPORT "RSM:  Vccq < Vccq Lockout Voltage.  Data is not guaranteed" --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            SEVERITY warning;                                    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          END IF;                                                --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	
	 IF (pagesize>1) AND (var_123          ='1') AND (a'event) THEN
	  IF (var_15='0') THEN
	   IF (a(maxaddrline DOWNTO var_203     )=var_4(maxaddrline DOWNTO var_203     )) AND 
	      (a(var_203     -1 DOWNTO 0)/=var_4(var_203     -1 DOWNTO 0)) 
                AND (var_334    =readarray) THEN --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	      var_202          := '1';
	      var_123           := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	   ELSE
	      var_202          := '0';
	      var_123           := '1'; 
	   END IF;
	  ELSIF (var_15='1') THEN
	   IF var_158        (var_203     -1 DOWNTO 0)/=a(var_203     -1 DOWNTO 0) AND
	   (var_334    =readarray) THEN  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	     var_202          := '1';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	     var_123           := '0';--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	   ELSE 
	     var_202          := '0'; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	     var_123           := '1';    
	   END IF;
	  END IF;
	 END IF; 
	
	IF (var_242 = vil) THEN--XXXXXXXXXXXXXXX
		--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		var_186   <= idle;         
	ELSIF (var_277 = '0') THEN 
	   IF (var_190 = vih AND var_190'last_event >= toh) OR      
            (var_67 = vih AND var_67'last_event >= toh) OR 
	    (a'event) THEN  
           --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  var_186   <= readcyclex; 
		  var_202          := '0';	  
	   END IF; 	  
        ELSIF (var_277 = '1')  THEN
	 IF (var_376             = '1' OR var_375             = '1') THEN
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	   IF (var_249             = '1' OR var_250       = '1' OR var_248  = '1') AND (now-var_292>=tchtlh) AND (var_334    =readarray) THEN --XXXXXXXXXX
	     var_373       := '0';
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	   ELSIF (var_249             = '0' AND var_250       = '0' AND var_248  = '0') AND (now-var_292>=tchtlh) AND (var_334    =readarray)  THEN --XXXXXXXXXX
	     var_373       := '1';
	   END IF;
	 END IF;
	    
	   IF (var_190 = vih AND var_190'last_event >= toh) OR --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	      (var_67 = '1' AND var_67'last_event >= toh) OR
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	      ((var_334    =readarray) AND (var_131  ='0' OR var_54    ='1')  AND (now-var_292>=tchqx)) OR --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	      (var_287  = now) THEN	--XXXXXXXXXXXXXXXXXXXXXXXXX
		var_186    <= readcyclex;
		var_188              := '0';
		var_89 := '0';
	     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	       --XXXXXXXXXXXXXXXXXXXXXXXXXXX
	       	--XXXXXXXXXXXXXXXXXXXXX
	      --XXXXXXXXXXX
	   ELSE 
		var_186    <= readcyclev;	--XXXXXXXXXXXXXXXXXXXXX
	   END IF;        
        ELSE 
	 	    --XXXXXXXXXXXXXXXXXXXXX
		    var_186   <= readcyclev;            
	END IF;--XXXXXXXXXXXXXXXXXXXXXXXXXX
	
	 
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXX
	

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	
	WHEN OTHERS =>   
      END CASE; --XXXXXXXXXXXXXXXXXXXXXXX
      
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   IF (var_91 ='1') THEN
     var_373       := '0';
   END IF; 
   
   IF (vccq < vccqlko) THEN   --XXXXXXXXXX
     var_373       := '0';    --XXX
   END IF;                    --XXXX
  
   IF (syncenable > 0) THEN
    IF (var_372    ='1') THEN
     var_374 <= NOT(var_373      );
    ELSIF (var_372    ='0') THEN
     var_374 <= var_373      ;
    END IF;
   END IF;  
   IF (var_226    = '1') AND (var_226   'last_event > pollingdelay) THEN
	    var_226    <='0';
   END IF; 
   var_4 := a;  
   var_147         <= var_146    ;
   var_46             <= var_45        ;
   
END PROCESS var_229    ;

END var_36  ;

CONFIGURATION var_238 OF var_237 IS
   FOR var_36   END FOR;
END var_238; 

















--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
LIBRARY ieee;
LIBRARY work;
USE ieee.std_logic_1164.ALL;
USE work.eas_parameters.ALL;
USE work.io_pkg.ALL;
USE work.bfm_pkg.ALL;
USE std.textio.ALL;

ENTITY var_177  IS
   PORT ( 
       var_356 	: IN std_logic_vector(maxaddrline DOWNTO 0);
       var_366 	: IN std_logic_vector(maxdataline DOWNTO 0);
       var_361     : IN integer;
       var_362     : IN addrbuffer;
       var_364     : IN databuffer;
       var_406  : IN bit;
       var_223          : OUT std_logic_vector(17 DOWNTO 0) ;
       
       var_225      : IN integer;  --XXXXXXXXX
       var_241  : IN bit;
       var_224     : IN std_logic_vector(maxaddrline DOWNTO 0);
       var_22      : OUT std_logic_vector(maxdataline DOWNTO 0)
       ); 
END var_177 ;

ARCHITECTURE var_179     OF var_177  IS

FILE var_199   : text IS OUT "STD_OUTPUT"; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
FILE  var_78   	: text IS IN "initfile.dat";	

BEGIN
var_176 : PROCESS(var_361    , var_406, var_241)
    VARIABLE var_160 : line;
    VARIABLE var_175   : element_ptr;
    VARIABLE var_174       : element_ptr;

    VARIABLE var_170   : linkedarray_type;
    VARIABLE var_195  : linkedarray_type;
    VARIABLE var_392     : std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE var_393     : std_logic_vector(maxdataline DOWNTO 0);

    VARIABLE var_363       : addrbuffer;
    VARIABLE var_365         : databuffer ;

    VARIABLE var_161 : bit := '0';
    VARIABLE var_184   : line;
    VARIABLE var_12    : string(1 TO 6);
    VARIABLE var_77    : string(1 TO 5);   
    VARIABLE var_137  : std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE var_138  : std_logic_vector(maxdataline DOWNTO 0);

    VARIABLE var_234         : std_logic_vector(maxaddrline DOWNTO 0);
    VARIABLE var_182 : std_logic_vector(maxdataline DOWNTO 0);
    VARIABLE var_197        : otp_lockarray ; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_41   : integer ;
    VARIABLE var_281    : integer ;
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    VARIABLE var_206      : integer;
    VARIABLE var_45         : integer;
    VARIABLE var_149     : integer;
    VARIABLE var_39      : bit;
    VARIABLE var_154      : bit;
    
BEGIN
var_392     := var_356;  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
var_393     := var_366; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
var_363       := var_362  ;
var_365         := var_364    ;

var_234         := var_224    ;

    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

IF (var_161 = '0') THEN
 --XXXXXXXXXXXXXXXXXXXXXXXXX
  var_281    := targetotp;
  var_137 := itov(16#80#, maxaddrline+1);
  var_138 := itov(16#FFFE#, maxdataline+1);
  writearrayproc(var_281   , var_137 , var_195 , var_138 );
   
  IF fileinitialize THEN
    var_281    := targetmainarray;  
    var_136    : WHILE (NOT endfile(var_78  )) LOOP
                     readline(var_78  ,var_184);
                     read(var_184,var_12  );                        
                     read(var_184,var_77  );
		     hex_to_slv(var_12  , var_77  , var_137 , var_138 );
   		     IF (verbose =true) THEN
			write (var_160,string '("calling WAP with addr= "));
			lput (var_160, var_137 ,hex);
			write(var_160,string'(" data= "));
			lput (var_160, var_138 ,hex);			
			writeline(var_199   ,var_160);  --XX
		     END IF; 
                     writearrayproc(var_281   , var_137 , var_170  , var_138 );
                  END LOOP var_136   ;
  END IF;
      
  var_161:= '1';
END IF;

IF var_361    'event OR var_406'event THEN
    CASE var_361     IS
     WHEN writeprogram =>  --XXXXXXXXXXXXXXXXXXXXX
        IF var_406 = '1' AND var_406'event THEN
           var_281    := targetmainarray;
           writearrayproc(var_281   , var_392    , var_170  , var_393    );
	END IF;
     WHEN writeotp=> --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	var_281    := targetotp; 
        IF var_406 = '1' AND var_406'event THEN
           var_281    := targetotp;
           writearrayproc(var_281   , var_392    , var_195 , var_393    );
	END IF;
     WHEN writeblockerase => --XXXXXXXXXXXXXX
        IF var_406 = '1' AND var_406'event THEN
           var_281    := targetmainarray;    
	   calcblocknum(var_392    ,var_281   ,var_41  ,var_206     ,var_45        ,var_149    , var_39     ,var_154     );
	   eraseablockwnum(var_170  ,var_41  );
	END IF;
     WHEN writebufferprog =>
        IF var_406 = '1' AND var_406'event THEN         
	   var_281    := targetmainarray;    
           FOR j IN 0 TO maxbuffline LOOP
              writearrayproc(var_281   , var_363      (j), var_170  , var_365        (j));           
	   END LOOP;
	END IF;       
     WHEN OTHERS =>
    END CASE;
END IF;
 

IF var_241'event AND var_241='1' THEN
    IF var_225      = targetmainarray THEN 
        var_281    := targetmainarray;
	readarrayproc(var_281   , var_234        , var_170  , var_182);
    ELSIF  var_225      = targetotp THEN 
	var_281    := targetotp; 
        readarrayproc(var_281   , var_234        , var_195 , var_182); 
    ELSE
       IF (verbose =true) THEN
	  write (var_160,string '("  Hey RSM - GOT you !!! "));
	  writeline(var_199   ,var_160);  --XX
       END IF; 
    END IF;   	 
    var_22      <= var_182;  --XXXXXXXXXXXXXXXXXXXXXXXXXXX
END IF;

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  var_281    := targetotp;
  var_137  := itov(16#80#,maxaddrline+1 );
  readarrayproc(var_281   , var_137 , var_195 , var_138 );
  var_223         ( 1 DOWNTO 0 ) <= var_138 (1 DOWNTO 0);
  var_137  := itov(16#89#, maxaddrline+1);
  readarrayproc(var_281   , var_137 , var_195 , var_138 );
  var_223         ( 17 DOWNTO 2 ) <= var_138 (15 DOWNTO 0);
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

 END PROCESS var_176;
END var_179    ;
 
CONFIGURATION var_178      OF var_177  IS
   FOR var_179     END FOR;
END var_178     ;















LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
LIBRARY work;
USE work.eas_parameters.ALL;
USE work.bfm_pkg.ALL;
USE work.io_pkg.ALL;



ENTITY var_423 IS
PORT (var_65     : IN std_logic;
      a      : IN std_logic_vector(maxaddrline_x DOWNTO 1);
      ceb    : IN std_logic;
      oeb    : IN std_logic;
      byteb  : IN std_logic; 
      dq     : INOUT std_logic_vector(maxdataline DOWNTO 0);
      var_2  : OUT std_logic_vector(maxaddrline DOWNTO 0);
      var_88 : INOUT std_logic_vector(maxdataline DOWNTO 0) ;
      var_264        : IN integer;
      var_263       : IN integer

     );
END var_423;

ARCHITECTURE var_36   OF var_423 IS

CONSTANT   var_66  : std_logic_vector(7 DOWNTO 0) := (OTHERS => 'Z');
CONSTANT   var_62  : std_logic_vector(7 DOWNTO 0) := (OTHERS => '1');
CONSTANT   var_395  : std_logic_vector(maxdataline DOWNTO 0) := (OTHERS => 'Z');
CONSTANT   var_394  : std_logic_vector(maxdataline DOWNTO 0) := (OTHERS => '1');

SIGNAL   var_145          : std_logic := '1';

ALIAS   var_129       : std_logic_vector(7 DOWNTO 0) IS dq(dq'high DOWNTO (dq'high - 7)) ;
ALIAS   var_130       : std_logic_vector(7 DOWNTO 0) IS var_88(var_88'high DOWNTO (var_88'high - 7)) ;
ALIAS   var_167       : std_logic_vector(7 DOWNTO 0) IS dq((dq'low + 7) DOWNTO dq'low) ;
ALIAS   var_168       : std_logic_vector(7 DOWNTO 0) IS var_88((var_88'low + 7) DOWNTO var_88'low) ;

BEGIN

var_423 : PROCESS(var_88, dq, byteb, ceb, oeb, var_145        )
   BEGIN
   
   IF ( (ceb = vil) AND (oeb = vil) ) THEN

      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      --XXXXX
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      IF (byteb = vil) THEN
         var_129      <= var_66;
         IF (var_145         = '1') THEN
            var_167     <= var_130     ;
         ELSE
            var_167     <= var_168    ;
         END IF;
      ELSE 
         var_129      <= var_130     ;
         var_167      <= var_168    ;
      END IF;
      var_88 <= var_395;

   ELSE 
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      --XXXXXX
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      IF (byteb = vil) THEN
         IF (var_145         = '1') THEN
            var_130      <= var_167    ;
            var_168     <= var_62;
         ELSE 
            var_130      <= var_62;
            var_168     <= var_167    ;
         END IF;
      ELSE 
         var_88 <= dq;
      END IF;
      dq <= var_395;
   END IF;

   END PROCESS var_423;


var_86                 : PROCESS (ceb, oeb, a, var_65    , var_263    , var_264     )
   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

    VARIABLE var_132 : integer;


   BEGIN

   var_132 := vtoi(a);

   IF ( (ceb = vil) AND (oeb = vil) ) THEN
 
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      --XXXXX
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      IF ((var_263     = readarray) OR 
         ((var_263     = readidcodes) AND (var_132 >= 16#80#) AND (var_132 <= maxotpaddr)) ) THEN
         --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         var_145         <= var_65    ;
      ELSE
         --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         var_145         <= '0';
      END IF;
 
   ELSE
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      --XXXXXX
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      IF ( (var_264      = writeprogram) 
           OR (var_264      = writebufferdata)
           OR (var_264      = writeprotectionreg)
         ) THEN
         --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         var_145         <= var_65    ;
      ELSE
         --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         var_145         <= '0';
      END IF;
   END IF;


   END PROCESS var_86                ;

var_1               : PROCESS
   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

    FUNCTION addrdensity( CONSTANT a_in: IN  std_logic_vector(maxaddrline_x DOWNTO 1) )
                        RETURN std_logic_vector IS
          VARIABLE var_5 : std_logic_vector(maxaddrline DOWNTO 0);
        BEGIN
              var_5 := (OTHERS => '0');
              var_5((maxaddrline_x - 1) DOWNTO 0) := a_in(maxaddrline_x DOWNTO 1);
              RETURN var_5;
        END addrdensity;

   BEGIN
 
   WAIT ON var_65    , a;
 
   IF (var_65    'event AND (byteb = vil)) THEN
 
      var_2 <= addrdensity(a);


      IF (oeb = vih) THEN
         NULL; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      ELSIF (
              (oeb = vil) 
              AND (var_263     = readidcodes) 
              AND (maxotpaddr = vtoi(a))
        ) THEN
         var_2(3 DOWNTO 0) <= (OTHERS => '0');
         WAIT FOR pollingdelay;
         var_2 <= addrdensity(a);

      ELSE
         var_2(0) <= NOT a(1);
         WAIT FOR pollingdelay;
         var_2(0) <= a(1);
      END IF;

   ELSE
      --XXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_2 <= addrdensity(a);
   END IF;
 
   END PROCESS var_1              ;
  


var_64       : PROCESS(byteb)
   BEGIN

   IF (ceb = vil) THEN

   ASSERT (ceb'last_event <= telfl)
   REPORT "BYTE# not stable after tELFL/tELFH"
   SEVERITY error; 

   END IF;

   END PROCESS var_64      ;
   

END var_36  ;

CONFIGURATION var_424   OF var_423 IS
   FOR var_36   END FOR;
END var_424  ; 


--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX



LIBRARY ieee;
LIBRARY work;

USE std.textio.ALL;
USE ieee.std_logic_1164.ALL;
USE work.bfm_pkg.ALL;
USE work.eas_parameters.ALL;


ENTITY var_320  IS 
   PORT (
	a      : IN std_logic_vector(maxaddrline DOWNTO 0);
        dq     : INOUT std_logic_vector(maxdataline DOWNTO 0);
	var_67     : IN std_logic;
	var_190     : IN std_logic;
      	var_384     : IN std_logic;
        var_242    : IN std_logic;
        var_396     : IN std_logic;
	var_15    : IN std_logic;
	var_69    : IN std_logic;
	var_235     : IN real;
	vcc    : IN real;
	vccq   : IN real;
    	vpen    : IN real;
        gnd    : IN real;
	vssq   : IN real; 
	var_374  : OUT std_logic;
	sts    : OUT bit;
        var_337      : OUT integer;
        var_336     : OUT integer
	      );
END var_320 ;

ARCHITECTURE var_319  OF var_320  IS

--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

SIGNAL 	var_332 			: std_logic_vector(maxcrsize DOWNTO 0);
SIGNAL	var_413             	:  lockconfigtype; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
SIGNAL	var_414            	:  lockdowntype ; --XXXXXXXXXX
SIGNAL	var_421              	:  unlockdowntype ;
SIGNAL	var_418     		:  integer:=0;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
SIGNAL	var_419  			: std_logic_vector(maxsrsize DOWNTO 0):= srready;
SIGNAL  var_422 		: std_logic_vector(maxsrsize DOWNTO 0) := xsrbufffree;
SIGNAL	var_334     		:  integer;
SIGNAL	var_338      		: integer;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
SIGNAL  var_342    		: bit ;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
SIGNAL  var_226     		:  bit ;   --XXXXXXXXXXXXXX
SIGNAL  var_224     		:  std_logic_vector(maxaddrline DOWNTO 0);
SIGNAL  var_22      		:  std_logic_vector(maxdataline DOWNTO 0);
--XXXXXXXXXXXXXXXXXXXXXXXXXX
SIGNAL var_417  		: bit;
SIGNAL var_412  		: bit;
SIGNAL var_409     		: bit;
SIGNAL var_416        		: std_logic_vector(maxaddrline DOWNTO 0);
SIGNAL var_411       		: integer;
SIGNAL var_407       		: integer;
SIGNAL var_420			: bit;
SIGNAL var_340        		: std_logic_vector (maxaddrline DOWNTO 0);
SIGNAL var_339       		: std_logic_vector (maxdataline DOWNTO 0);  --XXXXXXXXXXXXXXXX
SIGNAL var_333      		: bit;
SIGNAL var_370     		: std_logic_vector (maxaddrline DOWNTO 0);
SIGNAL var_369       		: std_logic_vector (maxdataline DOWNTO 0);
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
SIGNAL var_380          	 : std_logic_vector (maxaddrline DOWNTO 0);  --XXXXXXXXX
SIGNAL var_382                 	 : std_logic_vector (maxaddrline DOWNTO 0); --XXXXXXXXX
SIGNAL var_381          	 : std_logic_vector (maxaddrline DOWNTO 0); --XXXXXXXXX
SIGNAL var_239                   : std_logic_vector(maxaddrline DOWNTO 0); --XXXXXXXXX
SIGNAL var_225     		: integer;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
SIGNAL var_341                   : std_logic;
		

COMPONENT var_329      
PORT (addr   		: IN std_logic_vector (maxaddrline DOWNTO 0);
      dq     		: IN std_logic_vector(maxdataline DOWNTO 0);
      ceb    		: IN std_logic;
      oeb    		: IN std_logic; 
      web    		: IN std_logic;
      rpb    		: IN std_logic;
      var_397    		: IN std_logic;
      var_18   		: IN std_logic;
      var_69    		: IN std_logic;
      var_344  		: IN real;
      vpen   		: IN real;
      vccq              : IN real;
      var_420    	: IN bit;
      var_418          	: IN integer;    --XXXXXXXXXXXXXXXXXXXXXXXXX
      var_417          	: IN bit;        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_412          	: IN bit;        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_409     	: IN bit;        --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_416          	: IN std_logic_vector (maxaddrline DOWNTO 0);    --XXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_411          	: IN integer;    --XXXXXXXXXXXXXXXXXXXXX
      var_407          	: IN integer;    --XXXXXXXXXXXXXXXXXXXXXXXX
      var_338          	: OUT integer;   --XXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_340          	: OUT std_logic_vector (maxaddrline DOWNTO 0);  --XXXXXXXXXXXXXXXXXXXXXXXXX
      var_339          	: OUT std_logic_vector (maxdataline DOWNTO 0);  --XXXXXXXXXXXXXXXX
      var_337           : OUT integer;   --XXXXXXXXXX
      var_334          	: OUT integer;   --XXXXXXXXXX
      var_346           	: OUT bit := '0';       --XXXXXXXXXXXXXXXXXX
      var_265		: IN std_logic_vector(maxsrsize DOWNTO 0);
      var_426              	: IN std_logic_vector(maxsrsize DOWNTO 0);
      cr		: OUT std_logic_vector (maxcrsize DOWNTO 0);
     --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
      var_341            : OUT std_logic
    );
END COMPONENT;

--XXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXX
--XXXXXXXXXXXXXXXXX

COMPONENT var_403
   PORT ( 
         a      	 : IN std_logic_vector(maxaddrline DOWNTO 0); 
         var_339         : IN std_logic_vector(maxdataline DOWNTO 0);
         var_415         : INOUT std_logic_vector (maxaddrline DOWNTO 0);
         var_368         : INOUT std_logic_vector (maxdataline DOWNTO 0);
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         var_67              : IN std_logic;
         var_384              : IN std_logic;
         var_193         : IN integer;
         var_242             : IN std_logic;
         var_235              : IN real;
         var_396              : IN std_logic;
         var_346          : IN bit; --XXXXXXXXXXXX
         vpen            : IN real;
         vccq            : IN real;
         var_239             : IN std_logic_vector(maxaddrline DOWNTO 0); --XXXXXXXXX
	 var_265              : INOUT std_logic_vector(maxsrsize DOWNTO 0);
         var_426             : INOUT std_logic_vector(maxsrsize DOWNTO 0);
         var_418         : INOUT integer;
         var_163         : INOUT lockconfigtype; --XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         var_164         : INOUT lockdowntype ; --XXXXXXXXXX
	 var_331         : INOUT unlockdowntype ;
         var_382                   : OUT std_logic_vector (maxaddrline DOWNTO 0); --XXXXXXXXX
         var_380       	 : OUT std_logic_vector (maxaddrline DOWNTO 0); --XXXXXXXXX
         var_381       	 : OUT std_logic_vector (maxaddrline DOWNTO 0); --XXXXXXXXX
	 var_410         : OUT integer;
         var_8           : OUT integer;
         --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         var_412         : INOUT bit;
         var_417         : INOUT bit;
	 var_409     	 : INOUT bit;
	 var_276		 : OUT bit;
	 var_341         : IN std_logic;
	 var_225         : IN integer;  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         var_241         : IN bit;                                              --XX
         var_224         : IN std_logic_vector(maxaddrline DOWNTO 0);           --XX
         var_22          : OUT std_logic_vector(maxdataline DOWNTO 0)		--XX
         );        
END COMPONENT;


COMPONENT var_237 
   PORT (
       vccq                     : IN real; 
       var_190               	: IN std_logic;
       var_67               	: IN std_logic;
       var_242              	: IN std_logic;
       var_384               	: IN std_logic;
       a                	: IN std_logic_vector(maxaddrline DOWNTO 0);
       var_15              	: IN std_logic;
       var_69              	: IN std_logic;
       cr               	: INOUT std_logic_vector(maxcrsize DOWNTO 0);
       var_334          	: IN integer;
       var_22           	: IN std_logic_vector(databuswidth-1 DOWNTO 0);
       var_418          	: IN integer;
       var_408                  : IN integer;
       var_410          	: IN integer;
       var_415          	: IN std_logic_vector(maxaddrline DOWNTO 0);
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
       var_382                  : IN std_logic_vector (maxaddrline DOWNTO 0);
       var_380       	        : IN std_logic_vector (maxaddrline DOWNTO 0);
       var_381       	        : IN std_logic_vector (maxaddrline DOWNTO 0);
       var_412          	: IN bit;
       var_417          	: IN bit;
       var_409             	: IN bit;
       var_413          	: IN lockconfigtype;
       var_414          	: IN lockdowntype;
       var_421            	: IN unlockdowntype;
       var_341                  : IN std_logic;
       var_419            	: INOUT std_logic_vector(maxsrsize DOWNTO 0);
       var_422			: INOUT std_logic_vector(maxsrsize DOWNTO 0);
       var_226          	: INOUT bit;
       var_239                  : OUT std_logic_vector(maxaddrline DOWNTO 0); --XXXXXXXXX
       dq               	: OUT std_logic_vector(databuswidth-1 DOWNTO 0);
       var_374            	: OUT std_logic;
       var_224          	: OUT std_logic_vector(maxaddrline DOWNTO 0);
       var_225           	: OUT integer
       );
END COMPONENT;


FOR var_323: var_329 USE CONFIGURATION work.var_330;
FOR var_324: var_403 USE CONFIGURATION work.var_404;
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
FOR var_325: var_237 USE CONFIGURATION work.var_238;

BEGIN

var_336     <= var_334    ;

var_323: var_329 
    PORT MAP(a, dq, var_67, var_190, var_384, var_242, var_396, var_15, 
	 var_69, vcc, vpen,vccq, var_420, var_418    , var_417  , var_412  , var_409     , var_416        , var_411       , var_407       ,
	 var_338     ,var_340        , var_339       , var_337     , var_334    , var_342   , var_419, var_422, var_332,
	 var_341   );

var_324: var_403  
    PORT MAP(var_340        , var_339       , var_416        , var_369       ,
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         --XXXXXXXXXXXXXXXXXXXXX
         var_67, var_384, var_338     , var_242, var_235, var_396, 
         var_342   , vpen, vccq, var_239            , var_419, var_422, var_418    , var_413       , var_414     , var_421       ,
         var_382                 , var_380       , var_381       , var_411       , var_407       , --XXXXXXXXX
         --XXXXXXXXXXXXXXXXXXXXXXXXXXX
	 var_412  , var_417  , var_409     , var_420, var_341   ,
         var_225     , var_226   , var_224    , var_22     );  --XX

--XXXXXXXXXXXXXXX
--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    
var_325: var_237 
    PORT MAP(vccq, var_190, var_67, var_242, var_384, a, var_15, var_69, var_332, var_334    , var_22     , var_418    , var_407       , var_411       ,
    var_416        , 
--XXXXXXXXXXXXXXXXXXXXXXX
var_382                 , var_380       , var_381       , --XXXXXXXXX
var_412  , var_417  , var_409     , var_413       , var_414     , var_421       ,
    var_341   , var_419, var_422, var_226   , var_239            , dq,var_374, var_224    ,
    var_225     );
    
sts <= var_420;    
END var_319 ;

CONFIGURATION var_321      OF var_320  IS
   FOR var_319  END FOR;
END var_321     ; 





LIBRARY ieee;
LIBRARY work;

USE std.textio.ALL;
USE ieee.std_logic_1164.ALL;
USE work.bfm_pkg.ALL;
USE work.eas_parameters.ALL;

ARCHITECTURE var_36   OF strataflash3v IS

SIGNAL      var_93     : std_logic := vih;
SIGNAL      var_92     : std_logic := vil;
SIGNAL      var_328    : std_logic;
SIGNAL      var_327    : integer;
SIGNAL      var_326    : std_logic;
SIGNAL      var_79     : std_logic_vector(maxdataline DOWNTO 0);
SIGNAL      var_3      : std_logic_vector(maxaddrline DOWNTO 0);
SIGNAL      var_68     : std_logic;
SIGNAL      var_191    : std_logic;
SIGNAL      var_386    : std_logic;
SIGNAL      var_236    : std_logic;
SIGNAL      var_63       : std_logic;
SIGNAL      var_263     : integer;
SIGNAL      var_264      : integer;

COMPONENT var_320 
PORT (  a      : IN std_logic_vector(maxaddrline DOWNTO 0);
        dq     : INOUT std_logic_vector(maxdataline DOWNTO 0);
	var_67     : IN std_logic;
	var_190     : IN std_logic;
      	var_384     : IN std_logic;
        var_242    : IN std_logic;
        var_396     : IN std_logic;
	var_15    : IN std_logic;
	var_69    : IN std_logic;
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	var_235     : IN real;  --XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	vcc    : IN real;
	vccq   : IN real;
    	vpen   : IN real;
        gnd    : IN real;
	vssq   : IN real; 
	var_374  : OUT std_logic;
	sts    : OUT bit;
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        var_337      : OUT integer;
        var_336     : OUT integer

	      );
END COMPONENT;

COMPONENT var_423 
PORT (var_65       : IN std_logic;
      a      : IN std_logic_vector(maxaddrline_x DOWNTO 1);
      ceb    : IN std_logic;
      oeb    : IN std_logic;
      byteb  : IN std_logic; 
      dq     : INOUT std_logic_vector(maxdataline DOWNTO 0);
      var_2  : OUT std_logic_vector(maxaddrline DOWNTO 0);
      var_88 : INOUT std_logic_vector(maxdataline DOWNTO 0) ;
      var_264      : IN integer;
      var_263     : IN integer
     );
END COMPONENT;



FOR var_27: var_320  USE CONFIGURATION work.var_321     ;
FOR var_28: var_423    USE CONFIGURATION work.var_424  ;

BEGIN

var_68     <= to_x01(ceb);
var_191    <= to_x01(oeb);
var_386    <= to_x01(web);
var_236    <= to_x01(rpb);
var_63       <= to_x01(byteb);

var_27 : var_320 
     PORT MAP(
        a => var_3     ,
        dq => var_79 ,
	var_67    => var_68    ,
	var_190    => var_191   ,
      	var_384    => var_386   ,
        var_242    => var_236   ,
        var_396    => var_93,
	var_15    => var_92,
	var_69    => var_92,
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	var_235    => rpb_a,
	vcc    => vcc,
	vccq    => vccq,
    	vpen    => vpen,
        gnd    => gnd,
	vssq    => vssq,
	var_374    => var_326 ,
	sts    => sts,
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	--XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        var_337      => var_264     ,
        var_336     => var_263    
       );
     
var_28 : var_423
     PORT MAP(
        var_65       => a0, 
        a  => a,
        ceb    => var_68    ,
        oeb    => var_191   ,
        byteb  => var_63      ,
        dq     => dq,
        var_2  => var_3     ,
        var_88 => var_79 ,
        var_264      => var_264     ,
        var_263     => var_263    
       );

var_165     : PROCESS (var_68    , var_191   , var_386   , a)
    BEGIN

       IF (ldenable AND var_236    = vih) THEN
           ASSERT (rpb_a >= vihmin)
              REPORT "RPb and RPb_a signals do not correlate"
              SEVERITY error;
       END IF;

       IF (ldenable AND var_236    = vil) THEN
           ASSERT (rpb_a <= vilmax)
              REPORT "RPb and RPb_a signals do not correlate"
              SEVERITY error;
       END IF;

    END PROCESS;



END var_36  ; 
	
