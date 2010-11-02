--  This file:            DlxPackage.vhd
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

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_bit.all;

package DlxPackage is

	subtype	TypeByteEnableMemory is unsigned( 7 downto 0 );
	subtype	TypeDigit is unsigned( 3 downto 0 );
	type	TypeArrayMemoryDoubleWord is array (natural range <>) of bit_vector( 63 downto 0 );
	subtype	TypeBidirectionalDataBus is std_logic_vector( 63 downto 0 );

	subtype TypeByte is unsigned( 7 downto 0 );
	type    TypeArrayByte is array (natural range <>) of unsigned( 7 downto 0 );

	subtype TypeWord is unsigned( 31 downto 0 );
	type    TypeArrayWord is array (natural range <>) of unsigned( 31 downto 0 );

	subtype TypeDoubleWord is unsigned( 63 downto 0 );
	type    TypeArrayDoubleWord is array (natural range <>) of unsigned( 63 downto 0 );

	subtype TypeDlxRegister is unsigned(4 downto 0);
	type    TypeArrayDlxRegister is array (natural range <>) of unsigned(4 downto 0);

	subtype TypePhysicalPage is unsigned( 31 downto 7 );
	type	TypeArrayPhysicalPage is array (natural range <>) of unsigned( 31 downto 7 );

	subtype TypeVirtualTag is unsigned( 31 downto 9 );
	type	TypeArrayVirtualTag is array (natural range <>) of unsigned( 31 downto 9 );

	subtype	TypePID is unsigned( 3 downto 0 );
	type	TypeArrayPID is array (natural range <>) of unsigned( 3 downto 0 );

	subtype TypeInstrCacheTag is unsigned( 31 downto 6 );
	type	TypeArrayInstrCacheTag is array (natural range <>) of unsigned(31 downto 6);
	subtype TypeDataCacheTag is unsigned( 31 downto 6 );
	type	TypeArrayDataCacheTag is array (natural range <>) of unsigned(31 downto 6);

	subtype TypeBranchTargetBufferTag is unsigned( 31 downto 4 );
	type	TypeArrayBranchTargetBufferTag is array (natural range <>)
														of unsigned(31 downto 4);
	constant cBTB_TagIndexToInstrAddrBit2 : integer := 4;


	subtype TypeBranchTargetBufferDestination is unsigned( 31 downto 0 );
	type	TypeArrayBranchTargetBufferDestination is array (natural range <>)
														of unsigned(31 downto 0);
	subtype TypePointerToReorderBuffer is unsigned(0 to 4);
	type    TypeArrayPointerToReorderBuffer is array (natural range <>)
														of unsigned(4 downto 0);
	subtype TypeReorderBufferLine is unsigned(73 downto 0);
	type    TypeReorderBufferForwardControlSignals is array (0 to 3, 0 to 4) of bit;
	constant	cReorderBufferPointer : TypeArrayPointerToReorderBuffer(4 downto 0)
											:= ( "00001", "00010", "00100", "01000", "10000" );

	type	TypeWriteBufferControlSignals is array ( 2 downto 0, 7 downto 0 ) of bit;

	type	TypeBytesInWriteBuffer is array ( 2 downto 0, 7 downto 0 ) of TypeByte;


	-- Number of Special-Purpose-Registers
	constant cSPRnumberInterruptEnableRegister		: unsigned(4 downto 0) := "00000";
	constant cSPRnumberReturnFromExceptionRegister	: unsigned(4 downto 0) := "00001";
	constant cSPRnumberProcessIdentifierRegister	: unsigned(4 downto 0) := "00010";
	constant cSPRnumberITB_PhysicalPageRegister		: unsigned(4 downto 0) := "00011";
	constant cSPRnumberDTB_PhysicalPageRegister		: unsigned(4 downto 0) := "00100";
	constant cSPRnumberVirtualPageRegister			: unsigned(4 downto 0) := "00101";

	-- Exception-Number
	constant cResetExceptionNumber					: unsigned(4 downto 0) := "00000";
	constant cStoreTransferErrorExceptionNumber		: unsigned(4 downto 0) := "00001";
	constant cFetchTransferErrorExceptionNumber		: unsigned(4 downto 0) := "00010";
	constant cLoadTransferErrorExceptionNumber		: unsigned(4 downto 0) := "00011";
	constant cAlignErrorExceptionNumber				: unsigned(4 downto 0) := "00100";
	constant cArithmeticErrorExceptionNumber		: unsigned(4 downto 0) := "00101";
	constant cIllegalInstrExceptionNumber			: unsigned(4 downto 0) := "00110";
	constant cPrivilegeErrorExceptionNumber			: unsigned(4 downto 0) := "00111";
	constant cDivideByZeroExceptionNumber			: unsigned(4 downto 0) := "01000";
	constant cMultiplyDivideOverflowExceptionNumber	: unsigned(4 downto 0) := "01001";
	constant cExternalInterruptExceptionNumber		: unsigned(4 downto 0) := "01010";
	constant cSystemCallExceptionNumber				: unsigned(4 downto 0) := "01011";
	constant cFetchTranslationMissExceptionNumber	: unsigned(4 downto 0) := "01100";
	constant cLoadTranslationMissExceptionNumber	: unsigned(4 downto 0) := "01101";
	constant cStoreTranslationMissExceptionNumber	: unsigned(4 downto 0) := "01110";

	-- Special instructions to control the processor are implemented as Trap instructions.
	-- Other DLX implementations use Trap 1 to 255 for some subroutine calls.
	-- For this reason other numbers are used ( Trap #0 is 'HALT' in all implementations ).
	constant cTrapActionHalt						: unsigned(8 downto 0) := "000000000";
	constant cTrapAction_not_used					: unsigned(8 downto 0) := "100000000";
	constant cTrapActionSystemCall					: unsigned(8 downto 0) := "100000001";
	constant cTrapActionNoAction_JustWait			: unsigned(8 downto 0) := "100000010";
	constant cTrapActionRefetch						: unsigned(8 downto 0) := "100000011";
	constant cTrapActionWaitUntilWriteBufferIsEmpty	: unsigned(8 downto 0) := "100000100";
	constant cTrapActionInvalidateWriteBuffer		: unsigned(8 downto 0) := "100000101";
	constant cTrapActionInvalidateITB				: unsigned(8 downto 0) := "100000110";
	constant cTrapActionInvalidateDTB				: unsigned(8 downto 0) := "100000111";
	constant cTrapActionInvalidateIC				: unsigned(8 downto 0) := "100001000";
	constant cTrapActionInvalidateDC				: unsigned(8 downto 0) := "100001001";
	constant cTrapActionInvalidateBTB				: unsigned(8 downto 0) := "100001010";



	-- decode instructions
	subtype TypeDlxOpcode is unsigned(5 downto 0);
	subtype TypeDlxFunc is unsigned(5 downto 0);

	constant cOpcode_alu		: TypeDlxOpcode := "000000";
	constant cOpcode_mdu		: TypeDlxOpcode := "000001";
	constant cOpcode_j			: TypeDlxOpcode := "000010";
	constant cOpcode_jal		: TypeDlxOpcode := "000011";
	constant cOpcode_beqz		: TypeDlxOpcode := "000100";
	constant cOpcode_bnez		: TypeDlxOpcode := "000101";
	constant cOpcode_undef_06	: TypeDlxOpcode := "000110";
	constant cOpcode_undef_07	: TypeDlxOpcode := "000111";
	constant cOpcode_addi		: TypeDlxOpcode := "001000";
	constant cOpcode_addui		: TypeDlxOpcode := "001001";
	constant cOpcode_subi		: TypeDlxOpcode := "001010";
	constant cOpcode_subui		: TypeDlxOpcode := "001011";
	constant cOpcode_andi		: TypeDlxOpcode := "001100";
	constant cOpcode_ori		: TypeDlxOpcode := "001101";
	constant cOpcode_xori		: TypeDlxOpcode := "001110";
	constant cOpcode_lhi		: TypeDlxOpcode := "001111";

	constant cOpcode_rfe		: TypeDlxOpcode := "010000";
	constant cOpcode_trap		: TypeDlxOpcode := "010001";
	constant cOpcode_jr			: TypeDlxOpcode := "010010";
	constant cOpcode_jalr		: TypeDlxOpcode := "010011";
	constant cOpcode_slli		: TypeDlxOpcode := "010100";
	constant cOpcode_undef_15	: TypeDlxOpcode := "010101";
	constant cOpcode_srli		: TypeDlxOpcode := "010110";
	constant cOpcode_srai		: TypeDlxOpcode := "010111";
	constant cOpcode_seqi		: TypeDlxOpcode := "011000";
	constant cOpcode_snei		: TypeDlxOpcode := "011001";
	constant cOpcode_slti		: TypeDlxOpcode := "011010";
	constant cOpcode_sgti		: TypeDlxOpcode := "011011";
	constant cOpcode_slei		: TypeDlxOpcode := "011100";
	constant cOpcode_sgei		: TypeDlxOpcode := "011101";
	constant cOpcode_undef_1E	: TypeDlxOpcode := "011110";
	constant cOpcode_undef_1F	: TypeDlxOpcode := "011111";

	constant cOpcode_lb			: TypeDlxOpcode := "100000";
	constant cOpcode_lh			: TypeDlxOpcode := "100001";
	constant cOpcode_undef_22	: TypeDlxOpcode := "100010";
	constant cOpcode_lw			: TypeDlxOpcode := "100011";
	constant cOpcode_lbu		: TypeDlxOpcode := "100100";
	constant cOpcode_lhu		: TypeDlxOpcode := "100101";
	constant cOpcode_undef_26	: TypeDlxOpcode := "100110";
	constant cOpcode_undef_27	: TypeDlxOpcode := "100111";
	constant cOpcode_sb			: TypeDlxOpcode := "101000";
	constant cOpcode_sh			: TypeDlxOpcode := "101001";
	constant cOpcode_undef_2A	: TypeDlxOpcode := "101010";
	constant cOpcode_sw			: TypeDlxOpcode := "101011";
	constant cOpcode_undef_2C	: TypeDlxOpcode := "101100";
	constant cOpcode_undef_2D	: TypeDlxOpcode := "101101";
	constant cOpcode_undef_2E	: TypeDlxOpcode := "101110";
	constant cOpcode_undef_2F	: TypeDlxOpcode := "101111";

	constant cOpcode_sequi		: TypeDlxOpcode := "110000";
	constant cOpcode_sneui		: TypeDlxOpcode := "110001";
	constant cOpcode_sltui		: TypeDlxOpcode := "110010";
	constant cOpcode_sgtui		: TypeDlxOpcode := "110011";
	constant cOpcode_sleui		: TypeDlxOpcode := "110100";
	constant cOpcode_sgeui		: TypeDlxOpcode := "110101";
	constant cOpcode_undef_36	: TypeDlxOpcode := "110110";
	constant cOpcode_undef_37	: TypeDlxOpcode := "110111";
	constant cOpcode_undef_38	: TypeDlxOpcode := "111000";
	constant cOpcode_undef_39	: TypeDlxOpcode := "111001";
	constant cOpcode_undef_3A	: TypeDlxOpcode := "111010";
	constant cOpcode_undef_3B	: TypeDlxOpcode := "111011";
	constant cOpcode_undef_3C	: TypeDlxOpcode := "111100";
	constant cOpcode_undef_3D	: TypeDlxOpcode := "111101";
	constant cOpcode_undef_3E	: TypeDlxOpcode := "111110";
	constant cOpcode_undef_3F	: TypeDlxOpcode := "111111";
			     

	constant cAluFunc_nop		: TypeDlxFunc := "000000";
	constant cAluFunc_undef_01	: TypeDlxFunc := "000001";
	constant cAluFunc_undef_02	: TypeDlxFunc := "000010";
	constant cAluFunc_undef_03	: TypeDlxFunc := "000011";
	constant cAluFunc_sll		: TypeDlxFunc := "000100";
	constant cAluFunc_undef_05	: TypeDlxFunc := "000101";
	constant cAluFunc_srl		: TypeDlxFunc := "000110";
	constant cAluFunc_sra		: TypeDlxFunc := "000111";
	constant cAluFunc_undef_08	: TypeDlxFunc := "001000";
	constant cAluFunc_undef_09	: TypeDlxFunc := "001001";
	constant cAluFunc_undef_0A	: TypeDlxFunc := "001010";
	constant cAluFunc_undef_0B	: TypeDlxFunc := "001011";
	constant cAluFunc_undef_0C	: TypeDlxFunc := "001100";
	constant cAluFunc_undef_0D	: TypeDlxFunc := "001101";
	constant cAluFunc_undef_0E	: TypeDlxFunc := "001110";
	constant cAluFunc_undef_0F	: TypeDlxFunc := "001111";
					 
	constant cAluFunc_sequ		: TypeDlxFunc := "010000";
	constant cAluFunc_sneu		: TypeDlxFunc := "010001";
	constant cAluFunc_sltu		: TypeDlxFunc := "010010";
	constant cAluFunc_sgtu		: TypeDlxFunc := "010011";
	constant cAluFunc_sleu		: TypeDlxFunc := "010100";
	constant cAluFunc_sgeu		: TypeDlxFunc := "010101";
	constant cAluFunc_undef_16	: TypeDlxFunc := "010110";
	constant cAluFunc_undef_17	: TypeDlxFunc := "010111";
	constant cAluFunc_undef_18	: TypeDlxFunc := "011000";
	constant cAluFunc_undef_19	: TypeDlxFunc := "011001";
	constant cAluFunc_undef_1A	: TypeDlxFunc := "011010";
	constant cAluFunc_undef_1B	: TypeDlxFunc := "011011";
	constant cAluFunc_undef_1C	: TypeDlxFunc := "011100";
	constant cAluFunc_undef_1D	: TypeDlxFunc := "011101";
	constant cAluFunc_undef_1E	: TypeDlxFunc := "011110";
	constant cAluFunc_undef_1F	: TypeDlxFunc := "011111";
					 
	constant cAluFunc_add		: TypeDlxFunc := "100000";
	constant cAluFunc_addu		: TypeDlxFunc := "100001";
	constant cAluFunc_sub		: TypeDlxFunc := "100010";
	constant cAluFunc_subu		: TypeDlxFunc := "100011";
	constant cAluFunc_and		: TypeDlxFunc := "100100";
	constant cAluFunc_or		: TypeDlxFunc := "100101";
	constant cAluFunc_xor		: TypeDlxFunc := "100110";
	constant cAluFunc_undef_27	: TypeDlxFunc := "100111";
	constant cAluFunc_seq		: TypeDlxFunc := "101000";
	constant cAluFunc_sne		: TypeDlxFunc := "101001";
	constant cAluFunc_slt		: TypeDlxFunc := "101010";
	constant cAluFunc_sgt		: TypeDlxFunc := "101011";
	constant cAluFunc_sle		: TypeDlxFunc := "101100";
	constant cAluFunc_sge		: TypeDlxFunc := "101101";
	constant cAluFunc_undef_2E	: TypeDlxFunc := "101110";
	constant cAluFunc_undef_2F	: TypeDlxFunc := "101111";

	constant cMduFunc_mult		: TypeDlxFunc := "001110";
	constant cMduFunc_div		: TypeDlxFunc := "001111";
	constant cMduFunc_multu		: TypeDlxFunc := "010110";
	constant cMduFunc_divu		: TypeDlxFunc := "010111";

	-- ALU ---
	subtype TypeAluFunction is unsigned( 4 downto 0 );

	constant cAlu_add	: TypeAluFunction := "00000";
	constant cAlu_sub	: TypeAluFunction := "00001";

	constant cAlu_and	: TypeAluFunction := "00010";
	constant cAlu_or	: TypeAluFunction := "00011";
	constant cAlu_xor	: TypeAluFunction := "00100";

	constant cAlu_sll	: TypeAluFunction := "00101";
	constant cAlu_srl	: TypeAluFunction := "00110";
	constant cAlu_sra	: TypeAluFunction := "00111";

	constant cAlu_seq	: TypeAluFunction := "01000";
	constant cAlu_sne	: TypeAluFunction := "01001";

	constant cAlu_sge	: TypeAluFunction := "01010";
	constant cAlu_sgt	: TypeAluFunction := "01011";
	constant cAlu_sle	: TypeAluFunction := "01100";
	constant cAlu_slt	: TypeAluFunction := "01101";

	constant cAlu_sgeu	: TypeAluFunction := "01110";
	constant cAlu_sgtu	: TypeAluFunction := "01111";
	constant cAlu_sleu	: TypeAluFunction := "10000";
	constant cAlu_sltu	: TypeAluFunction := "10001";

	constant cAlu_lhi	: TypeAluFunction := "10010";

	-- MDU --
	subtype TypeMduFunction is unsigned( 1 downto 0 );

	constant cMdu_mult	: TypeMduFunction := "00";
	constant cMdu_multu	: TypeMduFunction := "01";
	constant cMdu_div	: TypeMduFunction := "10";
	constant cMdu_divu	: TypeMduFunction := "11";

	constant cAdditionalDelay_mult	: natural := 3;
	constant cAdditionalDelay_multu	: natural := 3;
	constant cAdditionalDelay_div	: natural := 15;
	constant cAdditionalDelay_divu	: natural := 15;
	
	-- Instruction decoder
	subtype TypeInstructionDecode is unsigned( 19 downto 0 );
	subtype TypeInstructionType is unsigned( 11 downto 0 );

	constant cInstrType_nop		: TypeInstructionType := "000000000001";
	constant cInstrType_trap	: TypeInstructionType := "000000000010";
	constant cInstrType_rfe		: TypeInstructionType := "000000000100";
	constant cInstrType_j		: TypeInstructionType := "000000001000";
	constant cInstrType_jr		: TypeInstructionType := "000000010000";
	constant cInstrType_jal		: TypeInstructionType := "000000100000";
	constant cInstrType_jalr	: TypeInstructionType := "000001000000";
	constant cInstrType_branch	: TypeInstructionType := "000010000000";
	constant cInstrType_alu		: TypeInstructionType := "000100000000";
	constant cInstrType_mdu		: TypeInstructionType := "001000000000";
	constant cInstrType_lsu		: TypeInstructionType := "010000000000";
	constant cInstrType_illegal	: TypeInstructionType := "100000000000";

	constant cInstrTypeIndex_nop			: integer :=  0;
	constant cInstrTypeIndex_trap			: integer :=  1;
	constant cInstrTypeIndex_rfe			: integer :=  2;
	constant cInstrTypeIndex_j				: integer :=  3;
	constant cInstrTypeIndex_jr				: integer :=  4;
	constant cInstrTypeIndex_jal			: integer :=  5;
	constant cInstrTypeIndex_jalr			: integer :=  6;
	constant cInstrTypeIndex_branch			: integer :=  7;
	constant cInstrTypeIndex_alu			: integer :=  8;
	constant cInstrTypeIndex_mdu			: integer :=  9;
	constant cInstrTypeIndex_lsu			: integer := 10;
	constant cInstrTypeIndex_illegal		: integer := 11;
	constant cConditionIndex				: integer := 12;
	-- ALU, MDU function, byte enable
	constant cLoadSignExtensionIndex		: integer := 16;
	constant cSecondOperandImmediateIndex	: integer := 17;
	constant cImmediateSignExtensionIndex	: integer := 18;
	constant cCommitActionIndex				: integer := 19;

	subtype TypeCommitAction is bit;
	constant cCommitAction_store		: TypeCommitAction := '0';
	constant cCommitAction_write_back	: TypeCommitAction := '1';

	subtype TypeSecondOperandImmediate is bit;
	constant cSecondOperand_register		: TypeSecondOperandImmediate := '0';
	constant cSecondOperand_immediate		: TypeSecondOperandImmediate := '1';

	subtype TypeSignation is bit;
	constant cUnsigned					: TypeSignation := '0';
	constant cSigned					: TypeSignation := '1';

	subtype TypeByteEnable is unsigned( 3 downto 0);
	constant cByte						: TypeByteEnable := "0001";
	constant cHalfWord					: TypeByteEnable := "0011";
	constant cWord						: TypeByteEnable := "1111";

	subtype TypeCondition is bit;
	constant cConditionZero				: TypeCondition := '0';
	constant cConditionNotZero			: TypeCondition := '1';

	constant cLinkRegister				: unsigned(4 downto 0) := "11111";

	-- Declare functions
	function Equal( L,R : unsigned ) return bit;
	function BinaryDecode( L : unsigned ) return unsigned;
	function Alu( Source1, Source2 : TypeWord; AluFunction: TypeAluFunction ) return unsigned;
	function Mdu( Source1, Source2 : TypeWord; MduFunction: TypeMduFunction;
								ClockCounter : unsigned; ChangeData : bit ) return unsigned;

	function InstructionDecode( Instruction : TypeWord ) return unsigned;

	function CountHowManyBitsAreSet( TestVector : unsigned ) return natural;
	function ValidReorderBufferEntriesAreInSuccession( ValidVector : unsigned ) return boolean;

	function ForwardSelectLogicWorksWell( reg : natural; Hit : unsigned( 0 to 3 );
			CommitPointer : TypePointerToReorderBuffer;
			MatchMatrix, ForwardMatrix : TypeReorderBufferForwardControlSignals ) return boolean;

end package DlxPackage;

	---------------
	-- FUNCTIONS --
	---------------
package body DlxPackage is

function Equal( L,R : unsigned ) return bit is
	alias LV : unsigned ( 1 to L'length ) is L;
	alias RV : unsigned ( 1 to R'length ) is R;
	variable Result : bit := '1';
begin
	if( L'length /= R'length ) then
		assert false
		report "arguments of 'Equal' function are not of the same length"
		severity failure;
	else
		for i in LV'range loop
			if Result = '1' then
				if LV(i) /= RV(i) then
					Result := '0';
				end if;
			else return '0'; end if;
		end loop;
	end if;
	return Result;
end Equal;

function BinaryDecode( L : unsigned ) return unsigned is
	variable Result : unsigned ( (2 ** L'length)-1 downto  0 );
begin
	for i in Result'range loop
		if i = To_Integer( L ) then
			Result (i) := '1';
		else
			Result (i) := '0';	end if;
	end loop;
	return Result;
end BinaryDecode;

function Alu( Source1, Source2 : TypeWord; AluFunction: TypeAluFunction )
													return unsigned is
	variable Result	: TypeWord;
	variable Error	: bit;

begin
	case AluFunction is
		when cAlu_add => Result := Source1 + Source2;
		when cAlu_sub => Result := Source1 - Source2;

		when cAlu_and => Result := Source1 and Source2;
		when cAlu_or  => Result := Source1 or Source2;
		when cAlu_xor => Result := Source1 xor Source2;

		when cAlu_sll => Result := Source1 sll To_Integer( Source2( 4 downto 0 ) );
		when cAlu_srl => Result := Source1 srl To_Integer( Source2( 4 downto 0 ) );
		when cAlu_sra => Result := Source1 sra To_Integer( Source2( 4 downto 0 ) );

		when cAlu_seq => if Source1 = Source2 then
							Result := X"0000_0001"; else Result := X"0000_0000"; end if;
		when cAlu_sne => if Source1 /= Source2 then
							Result := X"0000_0001"; else Result := X"0000_0000"; end if;

		when cAlu_sge => if signed( Source1 ) >= signed( Source2 ) then
							Result := X"0000_0001"; else Result := X"0000_0000"; end if;
		when cAlu_sgt => if signed( Source1 ) >  signed( Source2 ) then
							Result := X"0000_0001"; else Result := X"0000_0000"; end if;
		when cAlu_sle => if signed( Source1 ) <= signed( Source2 ) then
							Result := X"0000_0001"; else Result := X"0000_0000"; end if;
		when cAlu_slt => if signed( Source1 ) <  signed( Source2 ) then
							Result := X"0000_0001"; else Result := X"0000_0000"; end if;

		when cAlu_sgeu => if Source1 >= Source2 then
							Result := X"0000_0001"; else Result := X"0000_0000"; end if;
		when cAlu_sgtu => if Source1 > Source2 then
							Result := X"0000_0001"; else Result := X"0000_0000"; end if;
		when cAlu_sleu => if Source1 <= Source2 then
							Result := X"0000_0001"; else Result := X"0000_0000"; end if;
		when cAlu_sltu => if Source1 < Source2 then
							Result := X"0000_0001"; else Result := X"0000_0000"; end if;

		when cAlu_lhi => Result := Source2( 15 downto 0 ) & X"0000";

		when others => Error := '1';

	end case;

	return Error & Result;

end Alu;

function Mdu( Source1, Source2 : TypeWord; MduFunction: TypeMduFunction;
					ClockCounter : unsigned; ChangeData : bit )
													return unsigned is
	variable Result : TypeDoubleWord;
	variable ClockCounterNewValue : unsigned( 3 downto 0 );
	variable DivideByZero : bit;
	variable MultiplyDivideOverflow : bit;
	variable Ready : bit;

begin
	if ChangeData = '1' then
		ClockCounterNewValue := "0000";
	else
		ClockCounterNewValue := ClockCounter + 1;
	end if;

	case MduFunction is
		when cMdu_mult  =>
					if ClockCounter = cAdditionalDelay_mult then
						Ready := '1';
						Result := unsigned( signed(Source1) * signed(Source2) );
						if ( Result(63 downto 31) /= (63 downto 31 => '0' ) ) and
									( Result(63 downto 31) /= (63 downto 31 => '1' ) ) then
							MultiplyDivideOverflow := '1';
						end if;
					end if;
		when cMdu_multu =>
					if ClockCounter = cAdditionalDelay_multu then
						Ready := '1';
						Result := Source1 * Source2;
						if Result(63 downto 32) /= X"00000000" then
							MultiplyDivideOverflow := '1';
						end if;
					end if;

		when cMdu_div   =>
					if Source2 = X"00000000" then
						DivideByZero := '1';
						Ready := '1';
					elsif ( ( Source1 = X"80000000" ) and ( Source2 = X"FFFFFFFF" ) ) then
						MultiplyDivideOverflow := '1';
						Ready := '1';
					else
						if ClockCounter = cAdditionalDelay_div then
							Ready := '1';
							Result(31 downto 0) := unsigned( signed(Source1) / signed(Source2) );
						end if;
					end if;
		when cMdu_divu  =>
					if Source2 = X"00000000" then
						DivideByZero := '1';
						Ready := '1';
					elsif ClockCounter = cAdditionalDelay_divu then
						Ready := '1';
						Result(31 downto 0) := Source1 / Source2;
					end if;

	end case;

	return Ready & MultiplyDivideOverflow & DivideByZero &
												ClockCounterNewValue & Result(31 downto 0);

end Mdu;

function InstructionDecode( Instruction : TypeWord ) return unsigned is
	alias InstructionOpcode : TypeDlxOpcode  is Instruction( 31 downto 26 );
	alias InstructionFunction : TypeDlxFunc is Instruction( 5 downto 0 );

	variable Result					: TypeInstructionDecode;

	alias InstructionType			: TypeInstructionType is Result( 11 downto 0 );
	alias SecondOperandImmediate	: TypeSecondOperandImmediate is Result( 17 );
	alias SignedImmediate			: TypeSignation is Result( 18 );
	alias CommitAction				: TypeCommitAction is Result( 19 );

	-- ALU and MDU Instructions
	alias AluFunction				: TypeAluFunction is Result( 16 downto 12 );
	alias MduFunction				: TypeMduFunction is Result( 13 downto 12 );

	-- Load and Store Instructions
	alias ByteEnable				: TypeByteEnable is Result( 15 downto 12 );
	alias SignedLoad				: TypeSignation is Result( 16 );

	-- Branch Instructions
	alias Condition					: TypeCondition is Result( 12 );

	-- Jump Instructions
	-- no more parameters

begin
	CommitAction := cCommitAction_write_back;		-- default

	case InstructionOpcode is
		when cOpcode_alu  =>			-- ALU, Integer-Unit; NoOperation
				InstructionType := cInstrType_alu;		-- default
				SecondOperandImmediate := cSecondOperand_register;			-- default
				case InstructionFunction is
					when cAluFunc_nop =>
							InstructionType := cInstrType_nop;	-- overwrite default

					when cAluFunc_add =>
							AluFunction := cAlu_add;
					when cAluFunc_addu =>
							AluFunction := cAlu_add;
					when cAluFunc_sub =>
							AluFunction := cAlu_sub;
					when cAluFunc_subu =>
							AluFunction := cAlu_sub;
					when cAluFunc_and =>
							AluFunction := cAlu_and;
					when cAluFunc_or =>
							AluFunction := cAlu_or;
					when cAluFunc_xor =>
							AluFunction := cAlu_xor;
					when cAluFunc_sll =>
							AluFunction := cAlu_sll;
					when cAluFunc_srl =>
							AluFunction := cAlu_srl;
					when cAluFunc_sra =>
							AluFunction := cAlu_sra;

					when cAluFunc_seq =>
							AluFunction := cAlu_seq;
					when cAluFunc_sne =>
							AluFunction := cAlu_sne;
					when cAluFunc_slt =>
							AluFunction := cAlu_slt;
					when cAluFunc_sgt =>
							AluFunction := cAlu_sgt;
					when cAluFunc_sle =>
							AluFunction := cAlu_sle;
					when cAluFunc_sge =>
							AluFunction := cAlu_sge;

					when cAluFunc_sequ =>
							AluFunction := cAlu_seq;
					when cAluFunc_sneu =>
							AluFunction := cAlu_sne;
					when cAluFunc_sltu =>
							AluFunction := cAlu_sltu;
					when cAluFunc_sgtu =>
							AluFunction := cAlu_sgtu;
					when cAluFunc_sleu =>
							AluFunction := cAlu_sleu;
					when cAluFunc_sgeu =>
							AluFunction := cAlu_sgeu;

					when others =>
							InstructionType := cInstrType_illegal;	-- overwrite default
				end case;

		when cOpcode_mdu =>			-- Mul/Div-Unit
				InstructionType := cInstrType_mdu;
				SecondOperandImmediate := cSecondOperand_register;
				case InstructionFunction is
					when cMduFunc_mult =>
							MduFunction := cMdu_mult;
					when cMduFunc_multu =>
							MduFunction := cMdu_multu;
					when cMduFunc_div =>
							MduFunction := cMdu_div;
					when cMduFunc_divu =>
							MduFunction := cMdu_divu;

					when others =>
							InstructionType := cInstrType_illegal;	-- overwrite default
				end case;

		when cOpcode_addi =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_add;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cSigned;
		when cOpcode_addui =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_add;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_subi =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sub;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cSigned;
		when cOpcode_subui =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sub;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_andi =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_and;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_ori =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_or;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_xori =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_xor;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_lhi =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_lhi;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_slli =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sll;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_srli =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_srl;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_srai =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sra;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;

		when cOpcode_seqi =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_seq;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cSigned;
		when cOpcode_snei =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sne;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cSigned;
		when cOpcode_slti =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_slt;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cSigned;
		when cOpcode_sgti =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sgt;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cSigned;
		when cOpcode_slei =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sle;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cSigned;
		when cOpcode_sgei =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sge;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cSigned;

		when cOpcode_sequi =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_seq;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_sneui =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sne;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_sltui =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sltu;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_sgtui =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sgtu;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_sleui =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sleu;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;
		when cOpcode_sgeui =>
				InstructionType := cInstrType_alu;
				AluFunction := cAlu_sgeu;
				SecondOperandImmediate := cSecondOperand_immediate;
				SignedImmediate := cUnsigned;

		when cOpcode_beqz =>
				InstructionType := cInstrType_branch;
				SignedImmediate := cSigned;
				Condition := cConditionZero;
		when cOpcode_bnez =>
				InstructionType := cInstrType_branch;
				SignedImmediate := cSigned;
				Condition := cConditionNotZero;

		when cOpcode_j =>
				InstructionType := cInstrType_j;
		when cOpcode_jal =>
				InstructionType := cInstrType_jal;
		when cOpcode_jalr =>
				InstructionType := cInstrType_jalr;
		when cOpcode_jr =>
				InstructionType := cInstrType_jr;

		when cOpcode_rfe =>
				InstructionType := cInstrType_rfe;
		when cOpcode_trap =>
				InstructionType := cInstrType_trap;

		when cOpcode_lb =>
				InstructionType := cInstrType_lsu;
				SignedImmediate := cSigned;
				SignedLoad := cSigned;
				ByteEnable := cByte;
		when cOpcode_lbu =>
				InstructionType := cInstrType_lsu;
				SignedImmediate := cSigned;
				SignedLoad := cUnsigned;
				ByteEnable := cByte;
		when cOpcode_lh =>
				InstructionType := cInstrType_lsu;
				SignedImmediate := cSigned;
				SignedLoad := cSigned;
				ByteEnable := cHalfWord;
		when cOpcode_lhu =>
				InstructionType := cInstrType_lsu;
				SignedImmediate := cSigned;
				SignedLoad := cUnsigned;
				ByteEnable := cHalfWord;
		when cOpcode_lw =>
				InstructionType := cInstrType_lsu;
				SignedImmediate := cSigned;
				ByteEnable := cWord;
		when cOpcode_sb =>
				InstructionType := cInstrType_lsu;
				SecondOperandImmediate := cSecondOperand_register;
				SignedImmediate := cSigned;
				ByteEnable := cByte;
				CommitAction := cCommitAction_store;	-- overwrite default
		when cOpcode_sh =>
				InstructionType := cInstrType_lsu;
				SecondOperandImmediate := cSecondOperand_register;
				SignedImmediate := cSigned;
				ByteEnable := cHalfWord;
				CommitAction := cCommitAction_store;	-- overwrite default
		when cOpcode_sw =>
				InstructionType := cInstrType_lsu;
				SecondOperandImmediate := cSecondOperand_register;
				SignedImmediate := cSigned;
				ByteEnable := cWord;
				CommitAction := cCommitAction_store;	-- overwrite default

		when others =>
				InstructionType := cInstrType_illegal;	-- overwrite default
	end case;

	return Result;

end InstructionDecode;

function CountHowManyBitsAreSet( TestVector : unsigned ) return natural is
	variable Counter : natural := 0;
begin
	for i in TestVector'range loop
		if TestVector(i) = '1' then
			Counter := Counter + 1;
		end if;
	end loop;
	return Counter;
end CountHowManyBitsAreSet;

function ValidReorderBufferEntriesAreInSuccession( ValidVector : unsigned ) return boolean is
	variable CounterChanges : natural := 0;
begin
	for i in 0 to 3 loop
		if ValidVector(i) /= ValidVector(i+1) then
			CounterChanges := CounterChanges + 1;
		end if;
	end loop;

	if CounterChanges < 2 then
		return true;
	else
		if CounterChanges = 2 then
			if ValidVector(0) = ValidVector(4) then
	    		return true;
	    	else
	    		return false;
	    	end if;
		else
			return false;
		end if;
	end if;
end ValidReorderBufferEntriesAreInSuccession;

function ForwardSelectLogicWorksWell( reg : natural; Hit : unsigned( 0 to 3 );
		CommitPointer : TypePointerToReorderBuffer;
		MatchMatrix, ForwardMatrix : TypeReorderBufferForwardControlSignals ) return boolean is

	variable Index : natural;
	variable WorkPointer : TypePointerToReorderBuffer;
	variable FoundPointer : TypePointerToReorderBuffer := "00000";
	variable ForwardVector : TypePointerToReorderBuffer := "00000";
begin
	if Hit(reg) = '0' then 
		return true;	-- don't care, the forwarded value is not used
	end if;

	for i in CommitPointer'range loop
		if CommitPointer(i) = '1' then
			Index := i;
		end if;
		if ForwardMatrix( reg, i ) = '1' then
			ForwardVector( i ) := '1';
		end if;
	end loop;

	if ForwardVector = "00000" then
		return false;
	end if;

	WorkPointer := CommitPointer;
	for i in CommitPointer'range loop
		if MatchMatrix( reg, (Index+i) mod 5 ) = '1' then
			-- match found
			FoundPointer := WorkPointer;	-- save last match
		end if;
		WorkPointer := WorkPointer ror 1;
	end loop;

	if ForwardVector = FoundPointer then
		return true;
	end if;

	return false;

end ForwardSelectLogicWorksWell;

end package body DlxPackage;
