--  This file:            Environment.vhd
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
use std.textio.all;

entity Environment is
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

end Environment;

architecture Behavior of Environment is

	signal MEM_WriteEnableFlag : bit;
	signal MEM_ByteEnableFlag : unsigned( 7 downto 0 );

	signal REG_BlockStartAddress : TypeWord;
	signal REG_DumpNumber : TypeWord;

begin
	process
		constant cLoadFileName : string := "dlx.out";
		constant cStoreFileName : string := "dlx.dump";

		constant cMemorySize : positive := 16384;
	    constant cHighAddress : natural := cMemorySize - 1;
		constant cHighAddress_unsigned : unsigned := X"0000_3FFF";

		constant cLowRegisterAddr : TypeWord := X"ffff_ff00";
		constant cHighRegisterAddr : TypeWord := X"ffff_ff7f";

		variable MEM_DataBus : TypeDoubleWord;
		variable MEM_Addr_natural : natural;
		variable MEM_TransferStarted : bit;

		variable REG_RegisterAddress : natural;
		variable REG_InterruptRequest : bit;

		variable DataFromExternalRegister : TypeDoubleWord;

		variable DoubleWordHelp : TypeDoubleWord;
		variable Memory : TypeArrayDoubleWord( 0 to cHighAddress/8 ) :=
				(others => X"0000000000000000");

	procedure LoadFile is
		file BinaryFile : text open read_mode is cLoadFileName;
		variable OneLine : line;
		variable ch : character;
		variable LineNumber : natural := 0;
		variable MemoryAddr : natural;
		variable Word : TypeWord;
		variable DoubleWordHelp : TypeDoubleWord;

		function HexDigitToBitVector( Digit : natural ) return TypeDigit is
		begin
			if    Digit =  0 then return "0000";
			elsif Digit =  1 then return "0001";
			elsif Digit =  2 then return "0010";
			elsif Digit =  3 then return "0011";
			elsif Digit =  4 then return "0100";
			elsif Digit =  5 then return "0101";
			elsif Digit =  6 then return "0110";
			elsif Digit =  7 then return "0111";
			elsif Digit =  8 then return "1000";
			elsif Digit =  9 then return "1001";
			elsif Digit = 10 then return "1010";
			elsif Digit = 11 then return "1011";
			elsif Digit = 12 then return "1100";
			elsif Digit = 13 then return "1101";
			elsif Digit = 14 then return "1110";
			elsif Digit = 15 then return "1111";
			end if;

			assert false report
			"No valid digit."
			severity failure;
			return "0000";
		end;

		procedure ReadHexNatural( OneLine : inout line; n : out natural ) is
			variable Result : natural := 0;
		begin
			for i in 1 to 8 loop
				read( OneLine, ch );
				if '0' <= ch and ch <= '9' then
					Result := Result*16 + character'pos(ch) - character'pos('0');
				elsif 'A' <= ch and ch <= 'F' then
					Result := Result*16 + character'pos(ch) - character'pos('A') + 10;
				elsif 'a' <= ch and ch <= 'f' then
					Result := Result*16 + character'pos(ch) - character'pos('a') + 10;
				else
					report "Format error in file " & cLoadFileName
							& " on line " & integer'image( LineNumber ) severity error;
				end if;
			end loop;
			n := Result;
		end ReadHexNatural;

		procedure ReadHexWord( OneLine : inout line;  Word : out TypeWord ) is
			variable Digit : natural;
			variable r : integer := 31;
		begin
			for i in 1 to 8 loop
				read( OneLine, ch );
				if '0' <= ch and ch <= '9' then
					Digit := character'pos(ch) - character'pos('0');
				elsif 'A' <= ch and ch <= 'F' then
					Digit := character'pos(ch) - character'pos('A') + 10;
				elsif 'a' <= ch and ch <= 'f' then
					Digit := character'pos(ch) - character'pos('a') + 10;
				else
					report "Format error in file " & cLoadFileName
						& " on line " & integer'image( LineNumber )
					severity error;
				end if;
				Word( r downto r-3 ) := HexDigitToBitVector( digit );
				r := r - 4;
			end loop;
		end ReadHexWord;

		begin
			while not endfile( BinaryFile ) loop
				readline( BinaryFile, OneLine );
				LineNumber := LineNumber + 1;
		        ReadHexNatural( OneLine, MemoryAddr );
				read( OneLine, ch );  -- the space between addr and data
				ReadHexWord( OneLine, Word );
				
				DoubleWordHelp := Memory( MemoryAddr / 8 );
				if ( MemoryAddr mod 8 ) = 0 then
					DoubleWordHelp( 63 downto 32 ) := Word;
				else
					DoubleWordHelp( 31 downto 0 ) := Word;
				end if;
				Memory( MemoryAddr / 8 ) := DoubleWordHelp;
			end loop;
    end LoadFile;

	procedure MemoryDump( StartAddress, NumberOfElements : TypeWord; ElementSize : natural ) is

		file DumpFile : text open write_mode is cStoreFileName;
		variable OneString : string( 1 to 18 );
		variable OneLine : line;
		variable Word : TypeWord;
		variable DoubleWordHelp : TypeDoubleWord;
		variable Address : natural;

		function NumberToDigit( Number : natural ) return character is
		begin
			if (Number >= 0) and (Number <= 9) then
				return character'val( character'pos('0') + Number );
			elsif (Number >= 10) and (Number <= 15) then
				return character'val( character'pos('A') - 10 + Number );
			else
				report "Invalid Hex-Number"
				severity error;
				return '0';
			end if;
		end NumberToDigit;

		function NaturalToString( Number : natural  ) return string is
			variable StringResult : string(1 to 8);
			variable WorkNumber : natural := Number;
		begin
			for i in 8 downto 1 loop
				StringResult( i ) := NumberToDigit( WorkNumber mod 16 );
				WorkNumber := WorkNumber / 16;
			end loop;
			return StringResult;
		end NaturalToString;

		function WordToString( Word : unsigned ) return string is
			variable StringResult : string(1 to 8);
			variable Digit : unsigned( 3 downto 0 );
		begin
			for i in 1 to 8 loop
				Digit := Word( 4*(8-i)+3 downto 4*(8-i) );
				StringResult( i ) := NumberToDigit( natural( To_Integer( Digit ) ) ) ;
			end loop;
			return StringResult;
		end WordToString;

	begin
		Address := To_integer( StartAddress(31 downto 2) & "00" ); 	-- align to word

		for ActLine in 1 to To_integer( NumberOfElements ) loop

			DoubleWordHelp := Memory( Address / 8 );

			if ( Address mod 8 ) = 0 then
				Word := DoubleWordHelp( 63 downto 32 );
			else
				Word := DoubleWordHelp( 31 downto 0 );
			end if;

			OneString(1 to 8) := NaturalToString( Address );

			OneString(9 to 10) := "  ";

			if ( Address mod 8 ) <= 3 then
				Word := DoubleWordHelp( 63 downto 32 );
			else
				Word := DoubleWordHelp( 31 downto 0 );
			end if;

			if ElementSize = 4 then
				OneString(11 to 18) := WordToString( Word );
			elsif ElementSize = 2 then
				case Address mod 4 is
					when 0 =>
							OneString(11 to 14) := WordToString( Word )(1 to 4);
					when 2 =>
							OneString(11 to 14) := WordToString( Word )(5 to 8);
					when others =>
						report "Memory dump: Half word not aligned.";
				end case;
			else
				case Address mod 4 is
					when 0 =>
							OneString(11 to 12) := WordToString( Word )(1 to 2);
					when 1 =>
							OneString(11 to 12) := WordToString( Word )(3 to 4);
					when 2 =>
							OneString(11 to 12) := WordToString( Word )(5 to 6);
					when 3 =>
							OneString(11 to 12) := WordToString( Word )(7 to 8);
					when others =>
							null;
					end case;
			end if;

			Write( OneLine, OneString );
			WriteLine( DumpFile , OneLine );

			Address := Address + ElementSize;

		end loop;
	end MemoryDump;

begin
    LoadFile;  -- read binary memory image into memory array
    -- initialize outputs
    TransferError <= '0';


    -- process memory cycles
    loop

		MEM_DataBus := unsigned( To_bitvector( DataBus ) );

		if AddressBus <= cHighAddress_unsigned then			-- normal access
			
			MEM_Addr_natural := To_Integer( AddressBus );

			TransferAcknowledge <= MEM_TransferStarted;
			TransferError <= '0';
			CacheInhibit <= '0';

			DoubleWordHelp := Memory( MEM_Addr_natural / 8 );

			-- Asynchronous bus driver
			if WriteEnable = '1' then
				DataBus <= ( 63 downto 0 => 'Z' );
			else
				DataBus <= To_StdLogicVector( bit_vector( DoubleWordHelp ) );
			end if;

			if MEM_WriteEnableFlag = '1' then
				DataBus <= ( 63 downto 0 => 'Z' );
				for byte in MEM_ByteEnableFlag'range loop
					if MEM_ByteEnableFlag(byte) = '1' then
						DoubleWordHelp( 8*byte+7 downto 8*byte ) := 
												MEM_DataBus( 8*byte+7 downto 8*byte );
					end if;
				end loop;
				Memory( MEM_Addr_natural / 8 ) := DoubleWordHelp;
			end if;
		elsif  (( AddressBus >= cLowRegisterAddr ) and
										( AddressBus <= cHighRegisterAddr )) then

			-- Access to external register

			TransferAcknowledge <= MEM_TransferStarted;
			TransferError <= '0';

			REG_RegisterAddress := natural( To_Integer(AddressBus - cLowRegisterAddr));

			if WriteEnable = '0' then
				if  TransferStart = '1' then
					CacheInhibit <= '1';
					case REG_RegisterAddress is
						when 64 =>
							REG_InterruptRequest := '0';
							DataFromExternalRegister := ( 63 downto 0 => '0' );
						when others =>
							TransferError <= MEM_TransferStarted;
							TransferAcknowledge <= '0';
					end case;
				end if;
			else
				-- WriteEnable = '1'
				case REG_RegisterAddress is
					when 0 =>
						if MEM_WriteEnableFlag = '1' then
							REG_BlockStartAddress <= MEM_DataBus( 63 downto 32 );
						end if;
					when 4 =>
						if MEM_WriteEnableFlag = '1' then
							REG_DumpNumber <= MEM_DataBus( 31 downto 0 );
						end if;
					when 8 =>
						if MEM_WriteEnableFlag = '1' then
							MemoryDump( REG_BlockStartAddress, REG_DumpNumber, 4 );
						end if;
					when 12 =>
						if MEM_WriteEnableFlag = '1' then
							MemoryDump( REG_BlockStartAddress, REG_DumpNumber, 2 );
						end if;
					when 16 =>
						if MEM_WriteEnableFlag = '1' then
							MemoryDump( REG_BlockStartAddress, REG_DumpNumber, 1 );
						end if;
					when others =>
						TransferError <= MEM_TransferStarted;
						TransferAcknowledge <= '0';
				end case;
			end if;

			-- Asynchronous bus driver
			if WriteEnable = '1' then
				DataBus <= ( 63 downto 0 => 'Z' );
			else
				DataBus <= To_StdLogicVector( bit_vector( DataFromExternalRegister ) );
			end if;

		else
			TransferError <= MEM_TransferStarted;
			TransferAcknowledge <= '0';

			if WriteEnable = '1' then
				DataBus <= (63 downto 0 => 'Z' );
			end if;
		end if;
		
		wait on BusClock until BusClock = '1';

		CacheInhibit <= '0';

		if TransferStart = '1' then
			MEM_WriteEnableFlag <= WriteEnable;
			MEM_ByteEnableFlag <= ByteEnable;
 			MEM_TransferStarted := '1';
		else
			MEM_TransferStarted := '0';
			MEM_WriteEnableFlag <= '0';
		end if;

		if ForceInterrupt = '1' then
			REG_InterruptRequest := '1';
		end if;

		InterruptRequest <= REG_InterruptRequest;

    end loop;

end process;

end Behavior;

