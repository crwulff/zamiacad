--  This file:            Dlx.vhd
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

-- Special features:
-- 
--     - Pipelined, superscalar
--         - Two instructions per clock
--         - Branch-Target buffer
--         - Reorder-Buffer to commit instructions in program order
--         - Precise exception processing
--     - Four execution units with Reservation-Station
--         - Branch-Resolve unit
--         - Arithmetic-Logic unit
--         - Multiply-Divide unit
--         - Load-Store unit
--     - Write-Buffer
--     - 64 byte Instruction-Cache
--     - 64 byte Data-Cache
--     - 4 entry Instruction-Address-Translation-Buffer, page size: 128 byte
--     - 4 entry Data-Address-Translation-Buffer, page size: 128 byte
-- 
--     Note: - The small caches speed up simulation and can be filled up with little
--             test programs.
--           - The small page size makes it possible for little test programs to cross a
--             page boundary and force a translation miss exception.
--             The address translation buffers can be filled up quickly.
-- 
-- Instructions:
-- 
--     - No floating point instructions
-- 
--     - RFE ( return from exception )
--         - No parameter required
--         - Executed if the Reorder-Buffer is empty
--         - Address of next instruction: DP_ReturnFromExceptionReg(31 downto 2) & "00"
--         - New DP_InterruptEnableFlag: DP_ReturnFromExceptionReg(0)
-- 
--     - Trap
--         - A group of special instructions is implemented as trap #
--         - Executed if the Reorder-Buffer is empty
--         - Needs one parameter to determine what to do:
--             - 0x000          Halt processor
--             - 0x001 - 0x100  not used
--             - 0x101          System-Call, Invoke System-Call exception handler
--             - 0x102          No action
--                              Stall the pipeline until the Reorder-Buffer is empty.
--                              This instruction is similar to the sync instruction
--                              of the PowerPC processor by IBM / Motorola.
--             - 0x103          Refetch the instruction that follows the trap instruction.
--                              This instruction is similar to the isync instruction
--                              of the PowerPC processor by IBM / Motorola.
--             - 0x104          Wait until Write-Buffer is empty
--             - 0x105          Invalidate Write-Buffer
--             - 0x106          Invalidate Instruction-Address-Translation-Buffer
--             - 0x107          Invalidate Data-Address-Translation-Buffer
--             - 0x108          Invalidate Instruction-Cache 
--             - 0x109          Invalidate Data-Cache 
--             - 0x10A          Invalidate Branch-Target buffer
--         - All other numbers are not used and have no action. They are retired from the
--           pipeline if the Reorder-Buffer is empty ( like trap 0x102 ).
-- 
--     - Integer multiplication: mult, multu
--         - Four clock cycles for execution
--         - The 32 bit sources are multiplied to form a 64 bit product. If an overflow
--           occurs so that the expected result does not fit in the 32 bit destination
--           register, a Multiply-Divide-Overflow exception is recognized, otherwise
--           the lower 32 bits of the product represent the result.
-- 
--     - Integer division: div, divu
--         - Sixteen clock cycles for execution
--         - The 32 bit dividend is divided by the 32 bit divisor to form the result. If the
--           divisor is zero, a Divide-By-Zero exception is forced. If an overflow occurs
--           ( signed division: 0x80000000 / 0xFFFFFFFF ), a Multiply-Divide-Overflow
--           exception is recognized.
-- 
-- Exception handlers:
-- 
--     Address:    Exception:                 Comment:
-- 
--     0x00000000  Reset:                     External Reset clears the instruction counter.
-- 
--     0x00000100  Store-Transfer-Error:      The external Transfer-Error signal was asserted
--                                            during data store. The data are stored to memory
--                                            from the Write-Buffer that is not part of the
--                                            instruction pipeline. So this exception is not
--                                            precise.
-- 
--     0x00000200  Fetch-Transfer-Error:      The external Transfer-Error signal was asserted
--                                            during instruction fetch.
-- 
--     0x00000300  Load-Transfer-Error:       The external Transfer-Error signal was asserted
--                                            during data load.
-- 
--     0x00000400  Alignment-Error:           - Word access not word-aligned.
--                                            - Half-word access not half-word-aligned.
--                                            - Special-Purpose-Register access not
--                                              word-aligned.
-- 
--     0x00000500  Arithmetic-Error:          Illegal function in Arithmetic-Logic unit.
-- 
--     0x00000600  Illegal-Instruction:       Illegal instruction in program path.
-- 
--     0x00000700  Privilege-Error:           not used
-- 
--     0x00000800  Divide-By-Zero:            Divisor is zero.
-- 
--     0x00000900  Multiply-Divide-Overflow:  Result does not fit in 32 bit register.
-- 
--     0x00000A00  External-Interrupt:        An external interrupt request was taken.
-- 
--     0x00000B00  System-Call:               User programs can invoke the System-Call
--                                            exception handler to call functions of the
--                                            operating system. A defined register contains
--                                            the address of the parameter block. Since
--                                            address translation is disabled for the handler,
--                                            it must use the page table of the calling
--                                            process to translate the given effective address
--                                            to a physical address before reading the block.
-- 
--     0x00000C00  Fetch-Translation-Miss:    The effective address for an instruction fetch
--                                            can not be translated by the instruction address
--                                            translation buffer.
-- 
--     0x00000D00  Load-Translation-Miss:     The effective address for a data load operation
--                                            can not be translated by the data address
--                                            translation buffer.
-- 
--     0x00000E00  Store-Translation-Miss:    The effective address for a data store operation
--                                            can not be translated by the data address
--                                            translation buffer or the modified flag in the
--                                            page table must be set.
-- 
-- Special-Purpose-Registers:
--     All Special-Purpose-Registers are mapped in the upper 128 bytes of the physical address
--     space ( 0xFFFFFF80 to 0xFFFFFFFF ) . They are word-aligned and must be accessed in
--     word mode ( lw / sw ). User programs can access Special-Purpose-Registers if the
--     operating system provides a corresponding entry in the page table.
-- 
--     Interrupt-Enable register:      Address 0xFFFFFF80
--            Bit 0:       0: External interrupt disabled.
--                            If the external interrupt is disabled, the processor runs in
--                            Kernel-Mode. Address translation is disabled in this mode and
--                            the physical address equals the effective address.
--                         1: External interrupt enabled.
--                         When an exception is taken, this bit is cleared.
--                         When rfi is executed, this bit is set to the value
--                         of bit 0 from the Return-From-Exception register.
--            Bit 31 - 1:  Not used.
-- 
--     Return-From-Exception register: Address 0xFFFFFF84
--            When an exception is taken, this register stores the processor status that
--            should be restored when the exception handler returns control to the
--            interrupted program by executing the rfe instruction. So this register holds
--            bit 0 of the Interrupt-Enable register and the address of the instruction that
--            would have been executed next if the exception had not occurred.
--            Bit 0:       Copy of bit 0 from the Interrupt-Enable register.
--            Bit 1:       Not used.
--            Bit 31 - 2:  Upper bits of the address that should be restored by the
--                         rfe instruction.
-- 
--     Process-Identifier register:    Address 0xFFFFFF88
--            The process identification number is like an extension of the effective address.
--            The address translation buffers use this number to translate the effective
--            address of a process to a physical address. An address can be converted if the
--            requested effective address and the content of the Process-Identifier register
--            equal one entry in the translation buffer.
--            The process identification number "0000" is reserved for Kernel-Mode.
--            Address translation is disabled in this mode and the physical address equals
--            the effective address.
--            Bit  3 - 0:  Process identification number.
--            Bit 31 - 4:  Not used.
-- 
--     ITB-Physical-Page register:     Address 0xFFFFFF8C
--            This register is used to write a new entry to the instruction address
--            translation buffer. When a data-word is stored to the register, this word
--            contains the physical page of the new entry. The process identification number
--            and the virtual page of the new entry are always taken from the
--            Virtual-Page register. Since the address translation buffer is implemented as a
--            direct mapped cache, the position of the new entry is given by the virtual page.
--            So it does not make sense to read this register and all load instruction results
--            in zero.
--            Bit 31 - 7:  Physical page of new entry.
--            Bit  6 - 0:  Not used.
-- 
--     DTB-Physical-Page register:     Address 0xFFFFFF90
--            This register is used to write a new entry to the data address translation
--            buffer. When a data-word is stored to the register, this word contains the
--            physical page and the modified flag of the new entry. The process
--            identification number and the virtual page of the new entry are always taken
--            from the Virtual-Page register. Since the address translation buffer is
--            implemented as a direct mapped cache, the position of the new entry is given by
--            the virtual page. So it does not make sense to read this register and all load
--            instruction results in zero.
--            Bit 31 - 7:  Physical page of new entry.
--            Bit  6 - 1:  Not used.
--            Bit  0:      Modified flag of new entry.
-- 
--     Virtual-Page register:          Address 0xFFFFFF94
--            This register contains the process identification number and the virtual page
--            to write a new entry to one of the address translation buffers. When the program
--            wants to write a new entry, it must store the physical page to the
--            ITB-Physical-Page register or DTB-Physical-Page register. The new entry consists
--            of the written physical page and the content of the Virtual-Page register.
--            When an address translation miss exception is taken, the processor writes the
--            missing virtual page and its process identification number to the
--            Virtual-Page register.
--            Bit 31 - 7:  Virtual page of new entry.
--            Bit  6 - 4:  Not used.
--            Bit  3 - 0:  Process identification number.
-- 
-- Name conventions:
-- 
--     All constant expressions are defined in the file 'DlxPackage.vhd' and start with 'c'.
--     Example: cConstant
-- 
--     The design is divided into sub-units. The name of each signal starts with an
--     abbreviation for the sub-unit that generates the signal.
--     Example: IF_AbleToFetchHighWordOnly;  This signal is generated in
--                                           the Instruction-Fetch unit
-- 
--     All names for signals that represent registers of the processor end with 'Reg', 'RegA',
--     'RegB', 'Reg1' or 'Reg2'.
--     Example: IF_InstrCounterReg
-- 
--     All names for signals that represent flags of the processor end with 'Flag', 'FlagA',
--     'FlagB', 'Flag1' or 'Flag2'.
--     Example: IF_ValidFlagA
-- 
--     All signal names that end with 'Input' represent the input of a register or flag of
--     the processor.
--     Example: IF_InstrCounterRegInput
--     
--     All signal names that end with 'Write' represent the write enable of a register or flag
--     of the processor.
--     Example: IF_InstrCounterRegWrite
------------------------------------------------------------------------------------------

library Work;
use Work.DlxPackage.all;

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_bit.all;

entity Dlx is
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
end entity Dlx;

architecture BehaviorPipelined of Dlx is

	signal Clock : bit;

	--------------------------------------------------------------------------
	--							Instruction-Fetch							--
	--------------------------------------------------------------------------

	-- Instruction-Fetch; registers
	signal IF_InstrCounterReg : TypeWord;

	signal IF_InstrRegA : TypeWord;
	signal IF_InstrRegB : TypeWord;
	signal IF_InstrAddrRegA : TypeWord;
	signal IF_InstrAddrRegB : TypeWord;
	signal IF_NextInstrAddrRegA : TypeWord;
	signal IF_NextInstrAddrRegB : TypeWord;

	signal IF_ValidFlagA : bit;
	signal IF_ValidFlagB : bit;
	signal IF_PredictedBranchFlagA : bit;
	signal IF_PredictedBranchFlagB : bit;
	signal IF_TransferErrorFlagA : bit;
	signal IF_TransferErrorFlagB : bit;
	signal IF_AddressTranslationMissFlagA : bit;
	signal IF_AddressTranslationMissFlagB : bit;

	-- Instruction-Fetch; internal signals
	signal IF_InstrCounterRegInput : TypeWord;

	signal IF_InstrRegA_Input : TypeWord;
	signal IF_InstrRegB_Input : TypeWord;
	signal IF_InstrAddrRegA_Input : TypeWord;
	signal IF_InstrAddrRegB_Input : TypeWord;
	signal IF_NextInstrAddrRegA_Input : TypeWord;
	signal IF_NextInstrAddrRegB_Input : TypeWord;

	signal IF_ValidFlagA_Input : bit;
	signal IF_ValidFlagB_Input : bit;
	signal IF_PredictedBranchFlagA_Input : bit;
	signal IF_PredictedBranchFlagB_Input : bit;
	signal IF_TransferErrorFlagA_Input : bit;
	signal IF_TransferErrorFlagB_Input : bit;
	signal IF_AddressTranslationMissFlagA_Input : bit;
	signal IF_AddressTranslationMissFlagB_Input : bit;

	signal IF_InstrCounterRegWrite : bit;
	signal IF_StageA_Write : bit;
	signal IF_StageB_Write : bit;
	signal IF_ValidFlagA_Write : bit;
	signal IF_ValidFlagB_Write : bit;

	signal IF_NextDoubleWordAddr : TypeWord;
	signal IF_TakeBTB_Prediction : bit;
	signal IF_LoadStageA_WithStageB : bit;
	signal IF_IncrementInstrCounter : bit;
	signal IF_BothStagesAvailable : bit;
	signal IF_AbleToFetchInstrB : bit;
	signal IF_AbleToFetchHighWordOnly : bit;

	-- Instruction-Fetch; output signals
	signal IF_CacheRequestIC : bit;

	--------------------------------------------------------------------------
	--							Branch-Target-Buffer						--
	--------------------------------------------------------------------------

	-- Branch-Target-Buffer; internal registers
	signal BTB_DestinationReg : TypeArrayBranchTargetBufferDestination( 3 downto 0 );
	signal BTB_TagReg : TypeArrayBranchTargetBufferTag( 3 downto 0 );
	signal BTB_ValidFlag : unsigned( 3 downto 0 );

	-- Branch-Target-Buffer; internal signals
	signal BTB_DestinationRegInput : TypeBranchTargetBufferDestination;
	signal BTB_TagRegInput : TypeBranchTargetBufferTag;
	signal BTB_CacheWrite : unsigned( 3 downto 0 );
	signal BTB_ValidFlagInput : bit;
	signal BTB_SelectedTag : TypeBranchTargetBufferTag;
	signal BTB_SelectedValid : bit;
	signal BTB_Hit : bit;
	signal BTB_InstrAddrForCacheActivity : TypeWord;
	signal BTB_SelectedWriteEntryIndex : unsigned( 1 downto 0 );
	signal BTB_InvalidateOneEntry : bit;
	signal BTB_WriteOneEntry : bit;
	signal BTB_Invalidate_All : bit;

	-- Branch-Target-Buffer; output signals
	signal BTB_Destination : TypeWord;
	signal BTB_PredictBranchHighWord : bit;
	signal BTB_PredictBranchLowWord : bit;

	--------------------------------------------------------------------------
	--							Dispatcher									--
	--------------------------------------------------------------------------

	-- Dispatcher; register
	signal DP_ReturnFromExceptionReg : TypeWord;
	signal DP_ProcessIdentifierReg : unsigned(3 downto 0);
	signal DP_InterruptEnableFlag : bit;
	signal DP_HaltFlag : bit;

	-- Dispatcher; Instruction-Decoder
	signal DP_InstructionDecodeA : TypeInstructionDecode;
	alias DP_InstrTypeNopA		: bit is DP_InstructionDecodeA( cInstrTypeIndex_nop );
	alias DP_InstrTypeTrapA		: bit is DP_InstructionDecodeA( cInstrTypeIndex_trap );
	alias DP_InstrTypeRfeA		: bit is DP_InstructionDecodeA( cInstrTypeIndex_rfe );
	alias DP_InstrTypeJ_A		: bit is DP_InstructionDecodeA( cInstrTypeIndex_j );
	alias DP_InstrTypeJrA		: bit is DP_InstructionDecodeA( cInstrTypeIndex_jr );
	alias DP_InstrTypeJalA		: bit is DP_InstructionDecodeA( cInstrTypeIndex_jal );
	alias DP_InstrTypeJalrA		: bit is DP_InstructionDecodeA( cInstrTypeIndex_jalr );
	alias DP_InstrTypeBranchA	: bit is DP_InstructionDecodeA( cInstrTypeIndex_branch );
	alias DP_InstrTypeAluA		: bit is DP_InstructionDecodeA( cInstrTypeIndex_alu );
	alias DP_InstrTypeMduA		: bit is DP_InstructionDecodeA( cInstrTypeIndex_mdu );
	alias DP_InstrTypeLsuA		: bit is DP_InstructionDecodeA( cInstrTypeIndex_lsu );
	alias DP_InstrTypeIllegalA	: bit is DP_InstructionDecodeA( cInstrTypeIndex_illegal );
	alias DP_AluFunctionA		: TypeAluFunction is DP_InstructionDecodeA( 16 downto 12 );
	alias DP_MduFunctionA		: TypeMduFunction is DP_InstructionDecodeA( 13 downto 12 );
	alias DP_ByteEnableA		: TypeByteEnable is DP_InstructionDecodeA( 15 downto 12 );
	alias DP_DecodedConditionA	: bit is DP_InstructionDecodeA( cConditionIndex );
	alias DP_WriteBackA			: bit is DP_InstructionDecodeA( cCommitActionIndex );
	alias DP_OperandImmediateA	: bit is DP_InstructionDecodeA( cSecondOperandImmediateIndex );
	alias DP_SignExtImmediateA	: bit is DP_InstructionDecodeA( cImmediateSignExtensionIndex );
	alias DP_SignExtLoadA		: bit is DP_InstructionDecodeA( cLoadSignExtensionIndex );

	signal DP_InstructionDecodeB : TypeInstructionDecode;
	alias DP_InstrTypeNopB		: bit is DP_InstructionDecodeB( cInstrTypeIndex_nop );
	alias DP_InstrTypeTrapB		: bit is DP_InstructionDecodeB( cInstrTypeIndex_trap );
	alias DP_InstrTypeRfeB		: bit is DP_InstructionDecodeB( cInstrTypeIndex_rfe );
	alias DP_InstrTypeJ_B		: bit is DP_InstructionDecodeB( cInstrTypeIndex_j );
	alias DP_InstrTypeJrB		: bit is DP_InstructionDecodeB( cInstrTypeIndex_jr );
	alias DP_InstrTypeJalB		: bit is DP_InstructionDecodeB( cInstrTypeIndex_jal );
	alias DP_InstrTypeJalrB		: bit is DP_InstructionDecodeB( cInstrTypeIndex_jalr );
	alias DP_InstrTypeBranchB	: bit is DP_InstructionDecodeB( cInstrTypeIndex_branch );
	alias DP_InstrTypeAluB		: bit is DP_InstructionDecodeB( cInstrTypeIndex_alu );
	alias DP_InstrTypeMduB		: bit is DP_InstructionDecodeB( cInstrTypeIndex_mdu );
	alias DP_InstrTypeLsuB		: bit is DP_InstructionDecodeB( cInstrTypeIndex_lsu );
	alias DP_InstrTypeIllegalB	: bit is DP_InstructionDecodeB( cInstrTypeIndex_illegal );
	alias DP_AluFunctionB		: TypeAluFunction is DP_InstructionDecodeB( 16 downto 12 );
	alias DP_MduFunctionB		: TypeMduFunction is DP_InstructionDecodeB( 13 downto 12 );
	alias DP_ByteEnableB		: TypeByteEnable is DP_InstructionDecodeB( 15 downto 12 );
	alias DP_DecodedConditionB	: bit is DP_InstructionDecodeB( cConditionIndex );
	alias DP_WriteBackB			: bit is DP_InstructionDecodeB( cCommitActionIndex );
	alias DP_OperandImmediateB	: bit is DP_InstructionDecodeB( cSecondOperandImmediateIndex );
	alias DP_SignExtImmediateB	: bit is DP_InstructionDecodeB( cImmediateSignExtensionIndex );
	alias DP_SignExtLoadB		: bit is DP_InstructionDecodeB( cLoadSignExtensionIndex );

	-- Dispatcher; data for execution units, branches and jump register
	signal DP_ImmediateDataA : TypeWord;
	signal DP_ImmediateDataB : TypeWord;
	signal DP_DataA1 : TypeWord;
	signal DP_DataA2 : TypeWord;
	signal DP_DataB1 : TypeWord;
	signal DP_DataB2 : TypeWord;
	signal DP_DataValidA1 : bit;
	signal DP_DataValidA2 : bit;
	signal DP_DataValidB1 : bit;
	signal DP_DataValidB2 : bit;
	signal DP_ForwardReorderBufferA1 : TypePointerToReorderBuffer;
	signal DP_ForwardReorderBufferA2 : TypePointerToReorderBuffer;
	signal DP_ForwardReorderBufferB1 : TypePointerToReorderBuffer;
	signal DP_ForwardReorderBufferB2 : TypePointerToReorderBuffer;
	signal DP_SourceB1IsDestinationOfInstrA : bit;
	signal DP_SourceB2IsDestinationOfInstrA : bit;

	-- Dispatcher; parameters for instruction issue
	signal DP_IssueToReorderBufferA : bit;
	signal DP_IssueToReorderBufferB : bit;
	signal DP_AllocateReorderBufferPointerA : TypePointerToReorderBuffer;
	signal DP_AllocateReorderBufferPointerB : TypePointerToReorderBuffer;
	signal DP_SpeculativeA : bit;
	signal DP_SpeculativeB : bit;
	signal DP_RegisterDestinationA : TypeDlxRegister;
	signal DP_RegisterDestinationB : TypeDlxRegister;
	signal DP_TypeR_InstrA : bit;
	signal DP_TypeR_InstrB : bit;
	signal DP_DataOrExceptionToIssue : TypeWord;
	signal DP_DataForReorderBufferAreReadyA : bit;
	signal DP_DataForReorderBufferAreReadyB : bit;
	signal DP_IssueExceptionRequest : bit;
	signal DP_ReorderBufferAbleToTakeB : bit;
	signal DP_InstrValidA : bit;
	signal DP_InstrValidB : bit;
	signal DP_ExecuteOrIssueInstrA : bit;
	signal DP_ExecuteOrIssueInstrB : bit;

	-- Dispatcher; special issue
	signal DP_IssueAddressTranslationMiss : bit;
	signal DP_IssueTransferError : bit;
	signal DP_IssueIllegalInstrError : bit;

	signal DP_NoOperation : bit;

	-- Dispatcher; ALU, MDU and LSU instructions
	signal DP_IssueAluA : bit;
	signal DP_IssueAluB : bit;
	signal DP_IssueMduA : bit;
	signal DP_IssueMduB : bit;
	signal DP_IssueLsuA : bit;
	signal DP_IssueLsuB : bit;

	-- Dispatcher; branches
	signal DP_BranchDestinationA : TypeWord;
	signal DP_AlternativePathA : TypeWord;
	signal DP_ResolvedConditionA : bit;
	signal DP_BranchResolvedA : bit;
	signal DP_BranchUnresolvedA : bit;
	signal DP_IssueBranchToBRU_A : bit;
	signal DP_PathConditionA : bit;
	signal DP_BranchResolvedAsCorrectA : bit;
	signal DP_BranchResolvedAsFalseA : bit;
	signal DP_TakeBranchPredictedFalseA : bit;
	signal DP_TakeBranchNotPredictedA : bit;
	signal DP_ExecuteOrIssueBranchA : bit;

	signal DP_BranchDestinationB : TypeWord;
	signal DP_AlternativePathB : TypeWord;
	signal DP_ResolvedConditionB : bit;
	signal DP_BranchResolvedB : bit;
	signal DP_BranchUnresolvedB : bit;
	signal DP_IssueBranchToBRU_B : bit;
	signal DP_PathConditionB : bit;
	signal DP_BranchResolvedAsCorrectB : bit;
	signal DP_BranchResolvedAsFalseB : bit;
	signal DP_TakeBranchPredictedFalseB : bit;
	signal DP_TakeBranchNotPredictedB : bit;
	signal DP_ExecuteOrIssueBranchB : bit;

	-- Dispatcher; jump instructions
	signal DP_JumpDestinationA : TypeWord;
	signal DP_JumpDestinationB : TypeWord;
	signal DP_ExecuteJumpA : bit;
	signal DP_ExecuteJumpB : bit;
	signal DP_ExecuteJumpAndLinkA : bit;
	signal DP_ExecuteJumpAndLinkB : bit;
	signal DP_ExecuteJumpRegisterA : bit;
	signal DP_ExecuteJumpRegisterB : bit;
	signal DP_ExecuteJumpAndLinkRegisterA : bit;
	signal DP_ExecuteJumpAndLinkRegisterB : bit;
	signal DP_IssueJumpAndLinkA : bit;
	signal DP_IssueJumpAndLinkB : bit;
	signal DP_JumpRelativeA : bit;
	signal DP_JumpRelativeB : bit;

	-- Dispatcher; trap instruction, special instruction
	signal DP_AbleToExecuteTrap : bit;
	signal DP_ExecuteTrap : bit;

	signal DP_HaltDlx : bit;
	signal DP_SystemCall : bit;
	signal DP_SynchroniseRefetch : bit;
	signal DP_WaitUntilWriteBufferIsEmpty : bit;
	signal DP_InvalidateWriteBuffer : bit;
	signal DP_InvalidateInstructionAddressTranslationBuffer : bit;
	signal DP_InvalidateDataAddressTranslationBuffer : bit;
	signal DP_InvalidateInstructionCache : bit;
	signal DP_InvalidateDataCache : bit;
	signal DP_InvalidateBranchTargetBuffer : bit;

	-- Dispatcher; RFE instruction
	signal DP_ExecuteRfe : bit;

	-- Dispatcher; control path
	signal DP_NewPathA : TypeWord;
	signal DP_NewPathB : TypeWord;
	signal DP_NewPath : TypeWord;
	signal DP_AnyPreviousInstrChangesPathA : bit;
	signal DP_AnyPreviousInstrChangesPathB : bit;
	signal DP_ChangePathA : bit;
	signal DP_ChangePathB : bit;
	signal DP_ChangePath : bit;

	-- Dispatcher; Exception processing
	signal DP_ExceptionHandlerAddr : TypeWord;
	signal DP_ReturnFromExceptionRegInput : TypeWord;
	signal DP_ResumeAfterExceptionAddr : TypeWord;
	signal DP_TakeException : bit;
	signal DP_TakeExternalInterrupt : bit;
	signal DP_ExceptionNumber : unsigned(4 downto 0);
	signal DP_ReturnFromExceptionRegWrite : bit;
	signal DP_InterruptEnableFlagInput : bit;
	signal DP_InterruptEnableFlagWrite : bit;
	signal DP_TakeExceptionInvalidateBranchTargetBuffer : bit;
	signal DP_RfeInvalidateBranchTargetBuffer : bit;

	-- Dispatcher; Miscellaneous
	signal DP_ProcessIdentifierRegInput : unsigned(3 downto 0);
	signal DP_ProcessIdentifierRegWrite : bit;
	signal DP_InvalidateFetchStage : bit;
	signal DP_KernelMode : bit;

	--------------------------------------------------------------------------
	--							Reorder-Buffer								--
	--------------------------------------------------------------------------

	-- Reorder-Buffer; internal registers
	signal RB_DataReg : TypeArrayWord( 0 to 4 );
	signal RB_ReadyFlag : unsigned( 0 to 4 );
	signal RB_ExceptionFlag : unsigned( 0 to 4 );

	signal RB_InstrAddrReg : TypeArrayWord( 0 to 4 );
	signal RB_RegisterDestinationReg : TypeArrayDlxRegister( 0 to 4 );
	signal RB_WriteBackFlag : unsigned( 0 to 4 );

	signal RB_ValidFlag : unsigned( 0 to 4 );
	signal RB_SpeculativeFlag : unsigned( 0 to 4 );

	-- Reorder-Buffer; load entry; internal signals
	signal RB_DataRegInput : TypeArrayWord( 0 to 4 );
	signal RB_ReadyFlagInput : unsigned( 0 to 4 );
	signal RB_ExceptionFlagInput : unsigned( 0 to 4 );

	signal RB_InstrAddrRegInput : TypeArrayWord( 0 to 4 );
	signal RB_RegisterDestinationRegInput : TypeArrayDlxRegister( 0 to 4 );
	signal RB_WriteBackFlagInput : unsigned( 0 to 4 );

	signal RB_ValidFlagInput : unsigned( 0 to 4 );
	signal RB_SpeculativeFlagInput : unsigned( 0 to 4 );

	signal RB_DataFromExeUnit : TypeArrayWord( 0 to 4 );
	signal RB_AllocateEntry : unsigned( 0 to 4 );
	signal RB_CommitInstr : unsigned( 0 to 4 );
	signal RB_ExceptionFromExeUnit : unsigned( 0 to 4 );
	signal RB_LoadFromALU : unsigned( 0 to 4 );
	signal RB_LoadFromMDU : unsigned( 0 to 4 );
	signal RB_LoadFromLSU : unsigned( 0 to 4 );
	signal RB_DataPartWrite : unsigned( 0 to 4 );
	signal RB_SpeculativeFlagWrite : unsigned( 0 to 4 );
	signal RB_ValidFlagWrite : unsigned( 0 to 4 );

	-- Reorder-Buffer; forward data; internal signals
	signal RB_RegisterRequest : TypeArrayDlxRegister( 0 to 3 );
	signal RB_MatchEntry : TypeReorderBufferForwardControlSignals;
	signal RB_ForwardEntry : TypeReorderBufferForwardControlSignals;

	signal RB_Hit : unsigned( 0 to 3 );
	signal RB_ForwardData : TypeArrayWord( 0 to 3 );
	signal RB_ForwardDataReady : unsigned( 0 to 3 );
	signal RB_ForwardReorderBuffer : TypeArrayPointerToReorderBuffer( 0 to 3 );

	-- Reorder-Buffer; select next instructions to commit
	signal RB_NextInstrToCommitA : TypeReorderBufferLine;
	signal RB_NextInstrToCommitB : TypeReorderBufferLine;
	alias  RB_ValidCommitA : bit is RB_NextInstrToCommitA( 73 );
	alias  RB_SpeculativeCommitA : bit is RB_NextInstrToCommitA( 72 );
	alias  RB_InstrAddrCommitA : TypeWord is RB_NextInstrToCommitA( 71 downto 40 );
	alias  RB_RegisterDestinationCommitA : TypeDlxRegister is RB_NextInstrToCommitA(39 downto 35);
	alias  RB_WriteBackCommitA : bit is RB_NextInstrToCommitA( 34 );
	alias  RB_ExceptionCommitA : bit is RB_NextInstrToCommitA( 33 );
	alias  RB_ReadyCommitA : bit is RB_NextInstrToCommitA( 32 );
	alias  RB_DataCommitA : TypeWord is RB_NextInstrToCommitA( 31 downto 0 );

	alias  RB_ValidCommitB : bit is RB_NextInstrToCommitB( 73 );
	alias  RB_SpeculativeCommitB : bit is RB_NextInstrToCommitb( 72 );
	alias  RB_InstrAddrCommitB : TypeWord is RB_NextInstrToCommitB( 71 downto 40 );
	alias  RB_RegisterDestinationCommitB : TypeDlxRegister is RB_NextInstrToCommitB(39 downto 35);
	alias  RB_WriteBackCommitB : bit is RB_NextInstrToCommitB( 34 );
	alias  RB_ExceptionCommitB : bit is RB_NextInstrToCommitB( 33 );
	alias  RB_ReadyCommitB : bit is RB_NextInstrToCommitB( 32 );
	alias  RB_DataCommitB : TypeWord is RB_NextInstrToCommitB( 31 downto 0 );

	-- Reorder-Buffer; evaluate fill pointer;
	signal RB_EntryAvailable : TypePointerToReorderBuffer;
	signal RB_AvailablePointerA : TypePointerToReorderBuffer;
	signal RB_AvailablePointerB : TypePointerToReorderBuffer;
	signal RB_AbleToTakeA : bit;
	signal RB_AbleToTakeB : bit;

	-- Reorder-Buffer; output signals
	signal RB_ReorderBufferIsEmpty : bit;
	signal RB_AnyExceptionInsideReorderBuffer : bit;
	alias RB_ForwardDataA1 : TypeWord is RB_ForwardData( 0 );
	alias RB_ForwardDataA2 : TypeWord is RB_ForwardData( 1 );
	alias RB_ForwardDataB1 : TypeWord is RB_ForwardData( 2 );
	alias RB_ForwardDataB2 : TypeWord is RB_ForwardData( 3 );
	alias RB_ForwardReorderBufferA1 : TypePointerToReorderBuffer is RB_ForwardReorderBuffer( 0 );
	alias RB_ForwardReorderBufferA2 : TypePointerToReorderBuffer is RB_ForwardReorderBuffer( 1 );
	alias RB_ForwardReorderBufferB1 : TypePointerToReorderBuffer is RB_ForwardReorderBuffer( 2 );
	alias RB_ForwardReorderBufferB2 : TypePointerToReorderBuffer is RB_ForwardReorderBuffer( 3 );
	alias RB_HitA1 : bit is RB_Hit( 0 );
	alias RB_HitA2 : bit is RB_Hit( 1 );
	alias RB_HitB1 : bit is RB_Hit( 2 );
	alias RB_HitB2 : bit is RB_Hit( 3 );
	alias RB_ForwardDataReadyA1 : bit is RB_ForwardDataReady( 0 );
	alias RB_ForwardDataReadyA2 : bit is RB_ForwardDataReady( 1 );
	alias RB_ForwardDataReadyB1 : bit is RB_ForwardDataReady( 2 );
	alias RB_ForwardDataReadyB2 : bit is RB_ForwardDataReady( 3 );

	--------------------------------------------------------------------------
	--							Branch-Resolve-Unit							--
	--------------------------------------------------------------------------

	-- Branch-Resolve-Unit; internal registers
	signal BRU_ValidFlag : bit;
	signal BRU_DataToCompareReg : TypeWord;
	signal BRU_DataToCompareValidFlag : bit;
	signal BRU_ForwardReorderBufferReg : TypePointerToReorderBuffer;
	signal BRU_SpeculativePathConditionFlag : bit;
	signal BRU_PredictionHitFlag : bit;

	-- Branch-Resolve-Unit; output registers
	signal BRU_InstrAddrReg : TypeWord;
	signal BRU_AlternativePathReg : TypeWord;

	-- Branch-Resolve-Unit; internal signals
	signal BRU_Issue : bit;
	signal BRU_NoOutstandingSpeculativeBranch : bit;
	signal BRU_AbleToTakeInstrA : bit;
	signal BRU_AbleToTakeInstrB : bit;
	signal BRU_DataToCompareRegInput : TypeWord;
	signal BRU_DataToCompareValidFlagInput : bit;
	signal BRU_ValidFlagInput : bit;
	signal BRU_ValidFlagWrite : bit;
	signal BRU_ForwardReorderBufferRegInput : TypePointerToReorderBuffer;
	signal BRU_ForwardTestPointer : TypePointerToReorderBuffer;
	signal BRU_SpeculativePathConditionFlagInput : bit;
	signal BRU_PredictionHitFlagInput : bit;
	signal BRU_InstrAddrRegInput : TypeWord;
	signal BRU_AlternativePathRegInput : TypeWord;
	signal BRU_ForwardFromALU : bit;
	signal BRU_ForwardFromMDU : bit;
	signal BRU_ForwardFromLSU : bit;
	signal BRU_PathCorrect : bit;

	-- Branch-Resolve-Unit; output signals
	signal BRU_TakeBranchNotPredicted : bit;
	signal BRU_ChangePath : bit;
	signal BRU_SpeculationCorrect : bit;
	signal BRU_TakeBranchPredictedFalse : bit;
	signal BRU_ReadyExecution : bit;

	--------------------------------------------------------------------------
	--							Arithmetic-Logic-Unit						--
	--------------------------------------------------------------------------

	-- Arithmetic-Logic-Unit; Reservation-Station; internal registers
	signal ALU_SourceDataReg1 : TypeWord;
	signal ALU_SourceDataReg2 : TypeWord;
	signal ALU_SourceDataValidFlag1 : bit;
	signal ALU_SourceDataValidFlag2 : bit;
	signal ALU_ForwardReorderBufferReg1 : TypePointerToReorderBuffer;
	signal ALU_ForwardReorderBufferReg2 : TypePointerToReorderBuffer;

	-- Arithmetic-Logic-Unit; Core; internal registers
	signal ALU_DecoderInfoReg : TypeAluFunction;
	signal ALU_SpeculativeFlag : bit;
	signal ALU_ValidFlag : bit;

	-- Arithmetic-Logic-Unit; Core; output registers
	signal ALU_AllocatedReorderBufferReg : TypePointerToReorderBuffer;

	-- Arithmetic-Logic-Unit; Reservation-Station; internal signals
	signal ALU_SourceDataReg1Input : TypeWord;
	signal ALU_SourceDataReg2Input : TypeWord;
	signal ALU_SourceDataValidFlag1Input : bit;
	signal ALU_SourceDataValidFlag2Input : bit;
	signal ALU_ForwardReorderBufferReg1Input : TypePointerToReorderBuffer;
	signal ALU_ForwardReorderBufferReg2Input : TypePointerToReorderBuffer;
	signal ALU_ForwardReorderBuffer1 : TypePointerToReorderBuffer;
	signal ALU_ForwardReorderBuffer2 : TypePointerToReorderBuffer;
	signal ALU_ForwardedDataFromExeUnit1 : TypeWord;
	signal ALU_ForwardedDataFromExeUnit2 : TypeWord;
	signal ALU_ForwardTestPointer1 : TypePointerToReorderBuffer;
	signal ALU_ForwardTestPointer2 : TypePointerToReorderBuffer;
	signal ALU_AcceptDataA1 : bit;
	signal ALU_AcceptDataA2 : bit;
	signal ALU_AcceptDataB1 : bit;
	signal ALU_AcceptDataB2 : bit;
	signal ALU_ForwardFromALU1 : bit;
	signal ALU_ForwardFromALU2 : bit;
	signal ALU_ForwardFromMDU1 : bit;
	signal ALU_ForwardFromMDU2 : bit;
	signal ALU_ForwardFromLSU1 : bit;
	signal ALU_ForwardFromLSU2 : bit;
	signal ALU_ForwardLoad1 : bit;
	signal ALU_ForwardLoad2 : bit;
	signal ALU_LoadData1 : bit;
	signal ALU_LoadData2 : bit;

	-- Arithmetic-Logic-Unit; Core; internal signals
	signal ALU_AluOutput : unsigned( 32 downto 0 );
	alias  ALU_AluOutputData : TypeWord is ALU_AluOutput( 31 downto  0 );
	alias  ALU_AluOutputError : bit is ALU_AluOutput( 32  );
	signal ALU_Issue : bit;
	signal ALU_Available : bit;
	signal ALU_AbleToTakeInstrA : bit;
	signal ALU_AbleToTakeInstrB : bit;
	signal ALU_SpeculativeFlagInput : bit;
	signal ALU_SpeculativeFlagWrite : bit;
	signal ALU_ValidFlagInput : bit;
	signal ALU_ValidFlagWrite : bit;
	signal ALU_AllocatedReorderBufferRegInput : TypePointerToReorderBuffer;
	signal ALU_DecoderInfoRegInput : TypeAluFunction;

	-- Arithmetic-Logic-Unit; Core; output signals
	signal ALU_DataOrExceptionOut : TypeWord;
	signal ALU_Ready : bit;
	signal ALU_Forward : bit;
	signal ALU_Exception : bit;

	--------------------------------------------------------------------------
	--							Multiply-Divide-Unit						--
	--------------------------------------------------------------------------

	-- Multiply-Divide-Unit; Reservation-Station; internal registers
	signal MDU_SourceDataReg1 : TypeWord;
	signal MDU_SourceDataReg2 : TypeWord;
	signal MDU_SourceDataValidFlag1 : bit;
	signal MDU_SourceDataValidFlag2 : bit;
	signal MDU_ForwardReorderBufferReg1 : TypePointerToReorderBuffer;
	signal MDU_ForwardReorderBufferReg2 : TypePointerToReorderBuffer;

	-- Multiply-Divide-Unit; Core; internal registers
	signal MDU_DecoderInfoReg : TypeMduFunction;
	signal MDU_ClockCounterReg : unsigned( 3 downto 0 );
	signal MDU_SpeculativeFlag : bit;
	signal MDU_ValidFlag : bit;

	-- Multiply-Divide-Unit; Core; output registers
	signal MDU_AllocatedReorderBufferReg : TypePointerToReorderBuffer;

	-- Multiply-Divide-Unit; Reservation-Station; internal signals
	signal MDU_SourceDataReg1Input : TypeWord;
	signal MDU_SourceDataReg2Input : TypeWord;
	signal MDU_SourceDataValidFlag1Input : bit;
	signal MDU_SourceDataValidFlag2Input : bit;
	signal MDU_ForwardReorderBufferReg1Input : TypePointerToReorderBuffer;
	signal MDU_ForwardReorderBufferReg2Input : TypePointerToReorderBuffer;
	signal MDU_ForwardReorderBuffer1 : TypePointerToReorderBuffer;
	signal MDU_ForwardReorderBuffer2 : TypePointerToReorderBuffer;
	signal MDU_ForwardedDataFromExeUnit1 : TypeWord;
	signal MDU_ForwardedDataFromExeUnit2 : TypeWord;
	signal MDU_ForwardTestPointer1 : TypePointerToReorderBuffer;
	signal MDU_ForwardTestPointer2 : TypePointerToReorderBuffer;
	signal MDU_AcceptDataA1 : bit;
	signal MDU_AcceptDataA2 : bit;
	signal MDU_AcceptDataB1 : bit;
	signal MDU_AcceptDataB2 : bit;
	signal MDU_ForwardFromALU1 : bit;
	signal MDU_ForwardFromALU2 : bit;
	signal MDU_ForwardFromMDU1 : bit;
	signal MDU_ForwardFromMDU2 : bit;
	signal MDU_ForwardFromLSU1 : bit;
	signal MDU_ForwardFromLSU2 : bit;
	signal MDU_ForwardLoad1 : bit;
	signal MDU_ForwardLoad2 : bit;
	signal MDU_LoadData1 : bit;
	signal MDU_LoadData2 : bit;

	-- Multiply-Divide-Unit; Core; internal signals
	signal MDU_MduOutput : unsigned( 38 downto 0 );
	alias  MDU_MduOutputData : TypeWord is MDU_MduOutput( 31 downto  0 );
	alias  MDU_ClockCounterRegInput : unsigned( 3 downto 0 ) is
															MDU_MduOutput( 35 downto 32 );
	alias  MDU_DivideByZero : bit is MDU_MduOutput( 36 );
	alias  MDU_MultiplyDivideOverflow : bit is MDU_MduOutput( 37 );
	alias  MDU_ResultReady : bit is MDU_MduOutput( 38 );
	signal MDU_Issue : bit;
	signal MDU_DataChanged : bit;
	signal MDU_Available : bit;
	signal MDU_AbleToTakeInstrA : bit;
	signal MDU_AbleToTakeInstrB : bit;
	signal MDU_SpeculativeFlagInput : bit;
	signal MDU_SpeculativeFlagWrite : bit;
	signal MDU_ValidFlagInput : bit;
	signal MDU_ValidFlagWrite : bit;
	signal MDU_AllocatedReorderBufferRegInput : TypePointerToReorderBuffer;
	signal MDU_DecoderInfoRegInput : TypeMduFunction;

	-- Multiply-Divide-Unit; Core; output signals
	signal MDU_DataOrExceptionOut : TypeWord;
	signal MDU_Ready : bit;
	signal MDU_Forward : bit;
	signal MDU_Exception : bit;

	--------------------------------------------------------------------------
	--							Load-Store-Unit								--
	--------------------------------------------------------------------------

	-- Load-Store-Unit; Reservation-Station; internal registers
	signal LSU_SourceDataReg1 : TypeWord;
	signal LSU_SourceDataReg2 : TypeWord;
	signal LSU_SourceDataValidFlag1 : bit;
	signal LSU_SourceDataValidFlag2 : bit;
	signal LSU_ForwardReorderBufferReg1 : TypePointerToReorderBuffer;
	signal LSU_ForwardReorderBufferReg2 : TypePointerToReorderBuffer;

	-- Load-Store-Unit; Data stage; internal registers
	signal LSU_AllocatedReorderBufferReg : TypePointerToReorderBuffer;
	signal LSU_OffsetReg : unsigned( 15 downto 0 );
	signal LSU_ByteEnableFlag : unsigned( 3 downto 0 );
	signal LSU_InstructionLoadFlag : bit;
	signal LSU_SignExtensionFlag : bit;
	signal LSU_SpeculativeFlag : bit;
	signal LSU_ValidFlag : bit;

	-- Load-Store-Unit; Effective address stage; internal registers
	signal LSU_EA_AddrReg : TypeWord;
	signal LSU_EA_DataToStoreReg : TypeWord;
	signal LSU_EA_ByteEnableFlag : unsigned( 3 downto 0 );
	signal LSU_EA_InstructionLoadFlag : bit;
	signal LSU_EA_SignExtensionFlag : bit;
	signal LSU_EA_SpeculativeFlag : bit;
	signal LSU_EA_ValidFlag : bit;

	-- Load-Store-Unit; Effective address stage; output registers
	signal LSU_EA_AllocatedReorderBufferReg : TypePointerToReorderBuffer;

	-- Load-Store-Unit; Special-Purpose-Register stage; internal registers
	signal LSU_SPR_NumberReg : unsigned( 4 downto 0 );
	signal LSU_SPR_SpeculativeFlag : bit;
	signal LSU_SPR_ValidFlag : bit;

	-- Load-Store-Unit; Special-Purpose-Register stage; output registers
	signal LSU_SPR_AllocatedReorderBufferReg : TypePointerToReorderBuffer;

	-- Load-Store-Unit; Reservation-Station; internal signals
	signal LSU_SourceDataReg1Input : TypeWord;
	signal LSU_SourceDataReg2Input : TypeWord;
	signal LSU_SourceDataValidFlag1Input : bit;
	signal LSU_SourceDataValidFlag2Input : bit;
	signal LSU_ForwardReorderBufferReg1Input : TypePointerToReorderBuffer;
	signal LSU_ForwardReorderBufferReg2Input : TypePointerToReorderBuffer;
	signal LSU_ForwardReorderBuffer1 : TypePointerToReorderBuffer;
	signal LSU_ForwardReorderBuffer2 : TypePointerToReorderBuffer;
	signal LSU_ForwardedDataFromExeUnit1 : TypeWord;
	signal LSU_ForwardedDataFromExeUnit2 : TypeWord;
	signal LSU_ForwardTestPointer1 : TypePointerToReorderBuffer;
	signal LSU_ForwardTestPointer2 : TypePointerToReorderBuffer;
	signal LSU_AcceptDataA1 : bit;
	signal LSU_AcceptDataA2 : bit;
	signal LSU_AcceptDataB1 : bit;
	signal LSU_AcceptDataB2 : bit;
	signal LSU_ForwardFromALU1 : bit;
	signal LSU_ForwardFromALU2 : bit;
	signal LSU_ForwardFromMDU1 : bit;
	signal LSU_ForwardFromMDU2 : bit;
	signal LSU_ForwardFromLSU1 : bit;
	signal LSU_ForwardFromLSU2 : bit;
	signal LSU_ForwardLoad1 : bit;
	signal LSU_ForwardLoad2 : bit;
	signal LSU_LoadData1 : bit;
	signal LSU_LoadData2 : bit;

	-- Load-Store-Unit; Data stage; internal signals
	signal LSU_Issue : bit;
	signal LSU_Available : bit;
	signal LSU_AbleToTakeInstrA : bit;
	signal LSU_AbleToTakeInstrB : bit;
	signal LSU_OffsetRegInput : unsigned( 15 downto 0 );
	signal LSU_ByteEnableFlagInput : unsigned( 3 downto 0 );
	signal LSU_InstructionLoadFlagInput : bit;
	signal LSU_SignExtensionFlagInput : bit;
	signal LSU_SpeculativeFlagInput : bit;
	signal LSU_SpeculativeFlagWrite : bit;
	signal LSU_ValidFlagInput : bit;
	signal LSU_ValidFlagWrite : bit;
	signal LSU_AllocatedReorderBufferRegInput : TypePointerToReorderBuffer;
	signal LSU_Ready : bit;

	-- Load-Store-Unit; Effective address stage; internal signals
	signal LSU_EA_AddrRegInput : TypeWord;
	signal LSU_EA_RequestedData : TypeWord;
	signal LSU_EA_ExtendedData : TypeWord;
	signal LSU_EA_SpecialPurposeRegisterAccess : bit;
	signal LSU_EA_StageAvaliable : bit;
	signal LSU_EA_SpeculativeFlagInput : bit;
	signal LSU_EA_SpeculativeFlagWrite : bit;
	signal LSU_EA_ValidFlagInput : bit;
	signal LSU_EA_ValidFlagWrite : bit;
	signal LSU_EA_AlignmentError : bit;
	signal LSU_EA_CacheRequestDC : bit;
	signal LSU_EA_LoadByte3To0 : bit;
	signal LSU_EA_LoadByte2To0 : bit;
	signal LSU_EA_LoadByte1To0 : bit;
	signal LSU_EA_LoadByte3To1 : bit;
	signal LSU_EA_LoadByte1To1 : bit;
	signal LSU_EA_DataToStore : TypeWord;
	signal LSU_EA_ByteEnable : unsigned( 3 downto 0 );
	signal LSU_EA_StoreByte0To3 : bit;
	signal LSU_EA_StoreByte1To3 : bit;
	signal LSU_EA_StoreByte0To2 : bit;
	signal LSU_EA_StoreByte0To1 : bit;
	signal LSU_EA_StoreByte1To1 : bit;
	signal LSU_EA_StoreByte0To0 : bit;
	signal LSU_EA_DataFromSPR : TypeWord;
	signal LSU_EA_ReadyAccessSPR : bit;
	signal LSU_EA_ForwardWriteSPR : bit;

	-- Load-Store-Unit; Effective address stage; output signals
	signal LSU_EA_DataToStoreFanout : TypeDoubleWord;
	signal LSU_EA_ByteEnableFanout : unsigned(7 downto 0);
	signal LSU_EA_DataOrExceptionOut : TypeWord;
	signal LSU_EA_Ready : bit;
	signal LSU_EA_Exception : bit;
	signal LSU_EA_Forward : bit;

	-- Load-Store-Unit; Special-Purpose-Register stage; internal signals
	signal LSU_SPR_StageAvailable : bit;
	signal LSU_SPR_SpeculativeFlagInput : bit;
	signal LSU_SPR_SpeculativeFlagWrite : bit;
	signal LSU_SPR_ValidFlagInput : bit;
	signal LSU_SPR_ValidFlagWrite : bit;

	--------------------------------------------------------------------------
	--							Commit-Unit									--
	--------------------------------------------------------------------------

	-- Commit-Unit; registers
	signal CU_VirtualPageReg : TypeWord;
	signal CU_NextCommitPointerReg : TypePointerToReorderBuffer := "10000";

	-- Commit-Unit; internal signals
	signal CU_VirtualPageRegInput : TypeWord;
	signal CU_NextCommitPointerRegInput : TypePointerToReorderBuffer;
	signal CU_NextCommitPointerRegWrite : bit;
	signal CU_SPR_StoreA : bit;
	signal CU_SPR_StoreB : bit;
	signal CU_CommitStore : bit;
	signal CU_StoreToCache : bit;
	signal CU_Inhibit : bit;
	signal CU_TakeAddressTranslationMissException : bit;
	signal CU_VirtualPageRegWrite : bit;

	-- Commit-Unit; output signals
	signal CU_CommitPointerA : TypePointerToReorderBuffer;
	signal CU_CommitPointerB : TypePointerToReorderBuffer;
	signal CU_CommitInstrA : bit;
	signal CU_CommitInstrB : bit;
	signal CU_TakeException : bit;
	signal CU_RegisterWriteEnableA : bit;
	signal CU_RegisterWriteEnableB : bit;
	signal CU_WriteToSPR : bit;

	--------------------------------------------------------------------------
	--							Register-File								--
	--------------------------------------------------------------------------

	-- Register-File; internal registers
	signal RF_Reg : TypeArrayWord( 31 downto 1 );

	-- Register-File; internal signals 
	signal RF_RegInput	: TypeArrayWord( 31 downto 1 );
	signal RF_RegWrite : unsigned( 31 downto 0 );
	signal RF_WriteEnableA : unsigned( 31 downto 0 );
	signal RF_WriteEnableB : unsigned( 31 downto 0 );

	-- Register-File; output signals 
	signal RF_DataA1 : TypeWord;
	signal RF_DataA2 : TypeWord;
	signal RF_DataB1 : TypeWord;
	signal RF_DataB2 : TypeWord;


	--------------------------------------------------------------------------
	--					Instruction-Address-Translation-Buffer				--
	--------------------------------------------------------------------------

	-- Instruction-Address-Translation-Buffer; internal registers
	signal ITB_PhysicalPageReg : TypeArrayPhysicalPage( 3 downto 0 );
	signal ITB_VirtualTagReg : TypeArrayVirtualTag( 3 downto 0 );
	signal ITB_ProcessIdentifierReg : TypeArrayPID( 3 downto 0 );
	signal ITB_ValidFlag : unsigned( 3 downto 0 );

	-- Instruction-Address-Translation-Buffer; internal signals
	signal ITB_SelectedPhysicalPage : TypePhysicalPage;
	signal ITB_SelectedVirtualTag : TypeVirtualTag;
	signal ITB_SelectedProcessIdentifier : TypePID;
	signal ITB_SelectedValid : bit;
	signal ITB_PhysicalPageRegInput : TypePhysicalPage;
	signal ITB_VirtualTagRegInput : TypeVirtualTag;
	signal ITB_ProcessIdentifierRegInput : TypePID;
	signal ITB_ValidFlagInput : bit;
	signal ITB_CacheWrite : unsigned( 3 downto 0 );
	signal ITB_WriteNewEntry : bit;
	signal ITB_Hit : bit;

	-- Instruction-Address-Translation-Buffer; output signals
	signal ITB_PpageOut : TypePhysicalPage;
	signal ITB_Miss : bit;

	--------------------------------------------------------------------------
	--							Instruction-Cache							--
	--------------------------------------------------------------------------

	-- Instruction-Cache; internal registers
	signal IC_BlockReg : TypeArrayDoubleWord( 7 downto 0 );
	signal IC_TagReg : TypeArrayInstrCacheTag( 7 downto 0 );
	signal IC_ValidFlag : unsigned( 7 downto 0 );

	-- Instruction-Cache; internal signals
	signal IC_SelectedBlock : TypeDoubleWord;
	signal IC_SelectedTag : TypeInstrCacheTag;
	signal IC_SelectedValid : bit;
	signal IC_Hit : bit;
	signal IC_TagRegInput : TypeInstrCacheTag;
	signal IC_BlockRegInput : TypeDoubleWord;
	signal IC_ValidFlagInput : bit;
	signal IC_CacheWrite : unsigned( 7 downto 0 );

	-- Instruction-Cache; output signals
	signal IC_DataOut : TypeDoubleWord;
	alias  IC_InstrA : TypeWord is IC_DataOut( 63 downto 32 );
	alias  IC_InstrB : TypeWord is IC_DataOut( 31 downto  0 );
	signal IC_DataValid : bit;
	signal IC_FetchRequest : bit;

	--------------------------------------------------------------------------
	--						Data-Address-Translation-Buffer					--
	--------------------------------------------------------------------------

	-- Data-Address-Translation-Buffer; internal registers
	signal DTB_PhysicalPageReg : TypeArrayPhysicalPage( 3 downto 0 );
	signal DTB_VirtualTagReg : TypeArrayVirtualTag( 3 downto 0 );
	signal DTB_ProcessIdentifierReg : TypeArrayPID( 3 downto 0 );
	signal DTB_ModifiedFlag : unsigned( 3 downto 0 );
	signal DTB_ValidFlag : unsigned( 3 downto 0 );

	-- Data-Address-Translation-Buffer; internal signals
	signal DTB_SelectedPhysicalPage : TypePhysicalPage;
	signal DTB_SelectedVirtualTag : TypeVirtualTag;
	signal DTB_SelectedProcessIdentifier : TypePID;
	signal DTB_SelectedModified : bit;
	signal DTB_SelectedValid : bit;
	signal DTB_PhysicalPageRegInput : TypePhysicalPage;
	signal DTB_VirtualTagRegInput : TypeVirtualTag;
	signal DTB_ProcessIdentifierRegInput : TypePID;
	signal DTB_ModifiedFlagInput : bit;
	signal DTB_ValidFlagInput : bit;
	signal DTB_CacheWrite : unsigned( 3 downto 0 );
	signal DTB_WriteNewEntry : bit;
	signal DTB_Hit : bit;

	-- Data-Address-Translation-Buffer; output signals
	signal DTB_PpageOut : TypePhysicalPage;
	signal DTB_LoadMiss : bit;
	signal DTB_StoreMiss : bit;

	--------------------------------------------------------------------------
	--							Data-Cache									--
	--------------------------------------------------------------------------

	-- Data-Cache; internal registers
	signal DC_BlockReg : TypeArrayDoubleWord( 7 downto 0 );
	signal DC_TagReg : TypeArrayDataCacheTag( 7 downto 0 );
	signal DC_ValidFlag : unsigned( 7 downto 0 );

	-- Data-Cache; internal signals
	signal DC_SelectedBlock : TypeDoubleWord;
	signal DC_SelectedTag : TypeInstrCacheTag;
	signal DC_SelectedValid : bit;
	signal DC_Hit : bit;
	signal DC_DoubleWordData : TypeDoubleWord;
	signal DC_CacheHitData : TypeDoubleWord;
	signal DC_ForwardOnCacheHit : bit;
	signal DC_ForwardOnCacheMissByteEnable : unsigned( 3 downto 0 );
	signal DC_ByteNotRequiredOrForwarded : unsigned( 3 downto 0 );
	signal DC_AllRequiredBytesValidOnCacheMiss : bit;
	signal DC_BlockRegInput : TypeDoubleWord;
	signal DC_TagRegInput : TypeDataCacheTag;
	signal DC_ValidFlagInput : bit;
	signal DC_CacheWrite : unsigned( 7 downto 0 );

	-- Data-Cache; output signals
	signal DC_DataOut : TypeWord;
	signal DC_DataValid : bit;
	signal DC_LoadRequest : bit;

	--------------------------------------------------------------------------
	--							Write-Buffer								--
	--------------------------------------------------------------------------

	-- Write-Buffer; Entrance registers
	signal WB_EntranceAddrReg : TypeWord;
	signal WB_EntranceDataReg : TypeArrayByte( 7 downto 0 );
	signal WB_EntranceByteEnableFlag : unsigned( 3 downto 0 );
	signal WB_EntranceValidFlag : bit;
	signal WB_EntranceSpeculativeFlag : bit;
	signal WB_EntranceCommitFlag : bit;
	signal WB_EntranceWriteToCacheFlag : bit;

	-- Write-Buffer; Queue registers
	signal WB_AddrReg : TypeArrayWord(2 downto 0);
	signal WB_DataReg : TypeBytesInWriteBuffer;
	signal WB_ByteEnableFlag : TypeWriteBufferControlSignals;
	signal WB_ValidFlag : unsigned(2 downto 0);

	-- Write-Buffer; Entrance signals
	signal WB_AbleToTakeStore : bit;
	signal WB_BypassEntranceOnLoadToCache : bit;

	signal WB_EntranceAddrRegInput : TypeWord;
	signal WB_EntranceDataRegInput : TypeArrayByte( 7 downto 0 );
	signal WB_EntranceByteEnableFlagInput : unsigned( 3 downto 0 );
	signal WB_EntranceWriteToCacheFlagInput : bit;
	signal WB_EntranceValidFlagInput : bit;
	signal WB_EntranceSpeculativeFlagInput : bit;
	signal WB_EntranceCommitFlagInput : bit;
	signal WB_EntranceByteEnableFanout : unsigned( 7 downto 0 );

	signal WB_EntranceDataRegWrite : unsigned( 7 downto 0 );
	signal WB_EntranceWriteToCacheFlagWrite : bit;
	signal WB_EntranceByteEnableFlagWrite : bit;
	signal WB_EntranceValidFlagWrite : bit;
	signal WB_EntranceSpeculativeFlagWrite : bit;
	signal WB_EntranceCommitFlagWrite : bit;

	signal WB_EntranceData : TypeDoubleWord;
	signal WB_WriteBufferIsEmpty : bit;

	-- Write-Buffer; Queue signals
	signal WB_AddrRegInput : TypeArrayWord(2 downto 0);
	signal WB_DataRegInput : TypeBytesInWriteBuffer;
	signal WB_ByteEnableFlagInput : TypeWriteBufferControlSignals;
	signal WB_ValidFlagInput : unsigned(2 downto 0);

	signal WB_StageWrite : unsigned(2 downto 0);
	signal WB_StageAbleToMerge : unsigned(2 downto 0);
	signal WB_ByteWrite : TypeWriteBufferControlSignals;
	signal WB_MergeByte : TypeWriteBufferControlSignals;

	-- Write-Buffer; Forward signals
	signal WB_DataOut : TypeDoubleWord;
	signal WB_QueueDataOut : TypeDoubleWord;
	signal WB_ForwardByteEnableOnReadMiss : unsigned( 7 downto 0 );
	signal WB_ForwardEntrance : bit;
	signal WB_ForwardStage : unsigned(2 downto 0);

	signal WB_DataForStore : TypeDoubleWord;	
	signal WB_ByteEnableForStore : unsigned(7 downto 0);

	--------------------------------------------------------------------------
	--							Bus-Interface-Unit							--
	--------------------------------------------------------------------------

	-- Bus-Interface-Unit; internal registers
	signal BIU_ActiveLoadFlag : bit;
	signal BIU_ActiveFetchFlag : bit;
	signal BIU_ActiveStoreFlag : bit;
	signal BIU_FirstBusClockOfActiveCycleFlag : bit;

	-- Bus-Interface-Unit; internal signals
	signal BIU_CancelActiveCycle : bit;
	signal BIU_AbleToStartNewAccess : bit;
	signal BIU_ActiveAccessFlagWrite : bit;
	signal BIU_FirstBusClockOfActiveCycleFlagInput : bit;
	signal BIU_FirstBusClockOfActiveCycleFlagWrite : bit;
	signal BIU_ActiveLoadFlagInput : bit;
	signal BIU_ActiveFetchFlagInput : bit;
	signal BIU_ActiveStoreFlagInput : bit;
	signal BIU_DriveDataOnBus : bit;

	-- Bus-Interface-Unit; output signals to dlx
	signal BIU_IncomingData : TypeDoubleWord;
	signal BIU_CacheInhibit : bit;
	signal BIU_TransferAcknowledgeLoad : bit;
	signal BIU_TransferAcknowledgeFetch : bit;
	signal BIU_TransferAcknowledgeStore : bit;
	signal BIU_TransferErrorLoad : bit;
	signal BIU_TransferErrorFetch : bit;
	signal BIU_TransferErrorStore : bit;
	signal BIU_BusClock : bit;

begin
	Clock <= IncomingClock and not DP_HaltFlag;

	--------------------------------------------------------------------------
	--							Instruction-Fetch							--
	--------------------------------------------------------------------------

	-- Two instructions can be simultaneously fetched from the instruction cache
	-- if the address of the first instruction in program order (IF_InstrCounterReg) is
	-- double word aligned (8 byte aligned).
	-- The word order within a double word is big endian.
	-- Example:
	-- Addr 0x00000000: high word, first instruction in program order, DoubleWord( 63 downto 32 )
	-- Addr 0x00000004: low word, second instruction in program order, DoubleWord( 31 downto  0 )
	-- An alias is used to name the high word IC_InstrA and the low word IC_InstrB.
	-- There are two groups of registers to store the fetched data. One group is called
	-- stage A and represents the first instruction in program order, stage B the second.
	-- Both stages can accept an instruction from the high word or the low word. However if a
	-- low word is fetched into stage A, stage B becomes invalid.
	-- If there is a attempt to fetch two instructions from a 8 byte aligned address but only
	-- one stage (stage B in this case) is available, the high word is fetched into stage B
	-- and the instruction counter is incremented 4 bytes by setting bit 2;

	IF_NextDoubleWordAddr <= IF_InstrCounterReg +
		( ( 31 downto 4 => '0' ) & not IF_InstrCounterReg(2) & IF_InstrCounterReg(2) & "00" );

	IF_InstrCounterRegWrite <= DP_ChangePath or IF_IncrementInstrCounter;
	IF_InstrCounterRegInput <=
				DP_ExceptionHandlerAddr when DP_TakeException = '1' else
						DP_NewPath when DP_ChangePath = '1' else
							BTB_Destination when IF_TakeBTB_Prediction = '1' else
								IF_InstrCounterReg( 31 downto 3 ) & "100" when
														IF_AbleToFetchHighWordOnly = '1' else
									IF_NextDoubleWordAddr;

	-- If the low word is fetched to stage A in this clock, the IF_InstrCounterReg was incremented
	-- to the low word address during last fetch, because only stage B was available to fetch
	-- the high word.
	IF_InstrRegA_Input <= IF_InstrRegB when IF_LoadStageA_WithStageB = '1' else
							-- fetch low word only							fetch both words
								IC_InstrB when IF_InstrCounterReg( 2 ) = '1' else IC_InstrA;

	IF_InstrRegB_Input <= IC_InstrB when IF_AbleToFetchInstrB = '1' else IC_InstrA;

	IF_InstrAddrRegA_Input <= IF_InstrAddrRegB when IF_LoadStageA_WithStageB = '1' else
									IF_InstrCounterReg;

	IF_InstrAddrRegB_Input <= IF_InstrCounterReg( 31 downto 3 ) &
															IF_AbleToFetchInstrB & "00";

	IF_NextInstrAddrRegA_Input <=
				IF_NextInstrAddrRegB when IF_LoadStageA_WithStageB = '1' else
					IF_NextDoubleWordAddr when IF_InstrCounterReg( 2 ) = '1' -- fetch low word
						else IF_InstrCounterReg( 31 downto 3 ) & "100";		 -- fetch high word

	IF_NextInstrAddrRegB_Input <=
				IF_NextDoubleWordAddr when IF_AbleToFetchInstrB = '1'		 -- fetch low word
					else IF_InstrCounterReg( 31 downto 3 ) & "100";			 -- fetch high word

	IF_PredictedBranchFlagA_Input <=
						IF_PredictedBranchFlagB when IF_LoadStageA_WithStageB = '1' else
							BTB_PredictBranchLowWord when IF_InstrCounterReg( 2 ) = '1'
								else BTB_PredictBranchHighWord;

	IF_PredictedBranchFlagB_Input <=
						BTB_PredictBranchLowWord when IF_AbleToFetchInstrB = '1' else
							BTB_PredictBranchHighWord;

	IF_TransferErrorFlagA_Input <=
				(     IF_LoadStageA_WithStageB and IF_TransferErrorFlagB ) or
				( not IF_LoadStageA_WithStageB and BIU_TransferErrorFetch );
	IF_TransferErrorFlagB_Input <= BIU_TransferErrorFetch;

	IF_AddressTranslationMissFlagA_Input <= 
				(     IF_LoadStageA_WithStageB and IF_AddressTranslationMissFlagB ) or
				( not IF_LoadStageA_WithStageB and ITB_Miss );
	IF_AddressTranslationMissFlagB_Input <= ITB_Miss;

	IF_ValidFlagA_Input <= not DP_InvalidateFetchStage and
			( IF_LoadStageA_WithStageB or	-- Stage B is valid in this case
			  ( not IF_LoadStageA_WithStageB and
			  		( IC_DataValid or BIU_TransferErrorFetch or ITB_Miss ) ) );

	IF_ValidFlagB_Input <=
			( IC_DataValid or BIU_TransferErrorFetch or ITB_Miss ) and
			not ( ( IF_BothStagesAvailable and
						-- Fetch low word to stage A -> invalidate stage B
						( IF_InstrCounterReg( 2 ) or
						-- Predicted branch in stage A -> invalidate stage B
						  BTB_PredictBranchHighWord
							)
				  ) or
				  DP_InvalidateFetchStage ); 

	IF_StageA_Write <= not IF_ValidFlagA or DP_ExecuteOrIssueInstrA or DP_IssueExceptionRequest or
								DP_InvalidateFetchStage;

	IF_LoadStageA_WithStageB <=		-- Shift instruction of stage B to stage A.
				IF_ValidFlagB and not( DP_ExecuteOrIssueInstrB or DP_InvalidateFetchStage );

					-- Stage is free	or	becomes free
	IF_StageB_Write <= not IF_ValidFlagB or IF_StageA_Write;

	IF_BothStagesAvailable <= IF_StageA_Write and not IF_LoadStageA_WithStageB;

						-- Fetch instruction B only
	IF_AbleToFetchInstrB <= IF_InstrCounterReg( 2 ) or IF_BothStagesAvailable;

	-- To take a BTB-Prediction, the word caused the hit (means the branch) must be fetched.
	IF_TakeBTB_Prediction <= ( BTB_PredictBranchHighWord and not IF_InstrCounterReg( 2 ) ) or
			( BTB_PredictBranchLowWord and IF_AbleToFetchInstrB );

	-- Scenario: The fetch starts at a double word address and only the high word is fetched.
	IF_AbleToFetchHighWordOnly <= not IF_InstrCounterReg(2) and not IF_BothStagesAvailable;

	-- It is not necessary to increment for BIU_TransferErrorFetch or ITB_Miss.
	-- These events will cause an exception and change the path.
	IF_IncrementInstrCounter <= IC_DataValid and ( IF_StageB_Write or IF_StageA_Write );

	IF_CacheRequestIC <=
		not( IF_ValidFlagA and ( IF_TransferErrorFlagA or IF_AddressTranslationMissFlagA ) ) and
		not( IF_ValidFlagB and ( IF_TransferErrorFlagB or IF_AddressTranslationMissFlagB ) ) and
		not DP_ChangePath and
		not RB_AnyExceptionInsideReorderBuffer;	-- This allows the Write-Buffer to drain.

	--------------------------------------------------------------------------
	--							Branch-Target-Buffer						--
	--------------------------------------------------------------------------

	-- The Branch-Target-Buffer is indexed by IF_InstrCounterReg( 4 downto 3 ). So all
	-- entries represent 8 byte aligned addresses. IF_InstrCounterReg(2) is stored in
	-- an extra bit within the tag field. This bit is necessary to distinguish between
	-- a branch of the high word or the low word.

	-- Branch-Target-Buffer; look for a matching entry
	BTB_Destination <=
				BTB_DestinationReg( To_Integer( IF_InstrCounterReg( 4 downto 3 ) ) );
	BTB_SelectedTag <= BTB_TagReg( To_Integer( IF_InstrCounterReg( 4 downto 3 ) ) );
	BTB_SelectedValid <= BTB_ValidFlag( To_Integer( IF_InstrCounterReg( 4 downto 3 ) ) );

	BTB_Hit <= Equal( BTB_SelectedValid & BTB_SelectedTag( 31 downto 5 ),
													'1' & IF_InstrCounterReg( 31 downto 5 ) );

	-- The least significant bit of the tag represents bit 2 of the IF_InstrCounterReg.
	-- It is used to distinguish between branches on the low word or the high word.
	BTB_PredictBranchHighWord <= BTB_Hit and not BTB_SelectedTag( cBTB_TagIndexToInstrAddrBit2 );
	BTB_PredictBranchLowWord <= BTB_Hit and BTB_SelectedTag( cBTB_TagIndexToInstrAddrBit2 );

	-- Branch-Target-Buffer; write or delete entry
	BTB_DestinationRegInput <=
						BRU_AlternativePathReg when BRU_ChangePath = '1' else
							DP_NewPathA when DP_ChangePathA = '1' else
								DP_NewPathB;

	BTB_InstrAddrForCacheActivity <= BRU_InstrAddrReg when BRU_ChangePath = '1' else
										IF_InstrAddrRegA when DP_ChangePathA = '1' else
											IF_InstrAddrRegB;

	-- Store BTB_InstrAddrForCacheActivity( 2 ) to distinguish between
	-- high word and low word prediction.
	BTB_TagRegInput <= BTB_InstrAddrForCacheActivity( 31 downto 5 ) &
													BTB_InstrAddrForCacheActivity( 2 );

	-- Each action to the Branch-Target-Buffer is done strictly in program order and is
	-- not speculative. This means that there is not more than one action at the same time.
	postponed assert 1 >= CountHowManyBitsAreSet
			( BRU_TakeBranchPredictedFalse & DP_TakeBranchPredictedFalseA &
				DP_TakeBranchPredictedFalseB &
				BRU_TakeBranchNotPredicted & DP_TakeBranchNotPredictedA &
				DP_TakeBranchNotPredictedB )
		report "In the Branch-Target-Buffer is more than one action at the same time."
		severity failure;
	BTB_InvalidateOneEntry <= BRU_TakeBranchPredictedFalse or DP_TakeBranchPredictedFalseA or
											DP_TakeBranchPredictedFalseB;
	BTB_WriteOneEntry <= BRU_TakeBranchNotPredicted or
								DP_TakeBranchNotPredictedA or DP_TakeBranchNotPredictedB;

	BTB_SelectedWriteEntryIndex <= BTB_InstrAddrForCacheActivity( 4 downto 3 );

	BTB_Invalidate_All <= DP_InvalidateBranchTargetBuffer or DP_RfeInvalidateBranchTargetBuffer or
							DP_TakeExceptionInvalidateBranchTargetBuffer;

	BTB_ValidFlagInput <= not ( BTB_Invalidate_All or BTB_InvalidateOneEntry );

	BTB_CacheWrite <= ( 3 downto 0 => BTB_Invalidate_All ) or
							( BinaryDecode( BTB_SelectedWriteEntryIndex ) and
								( 3 downto 0 => BTB_InvalidateOneEntry or BTB_WriteOneEntry ) );

	--------------------------------------------------------------------------
	--							Dispatcher									--
	--------------------------------------------------------------------------

	-- The dispatcher is the central control unit of the processor.
	-- List of all sub-units within the dispatcher:
	--		Decode instructions
	--		Compute branch and jump destinations
	--		Evaluate data for execution units, branch conditions and jump register
	--		Evaluate parameters for instruction issue
	--		Special issue to Reorder-Buffer:
	--									- JumpAndLink instructions
	--									- illegal instructions
	--									- Translation miss
	--									- Fetch errors
	--		Execute or issue all types of instructions:
	--									- Illegal instruction, stage A only
	--									- Nop instruction, stage A only
	--									- ALU, MDU and LSU instructions
	--									- Branch instructions
	--									- JumpAndLink and JumpAndLink-Register instructions
	--									- Jump and Jump-Register instructions
	--									- RFE instruction
	--									- Trap instruction
	--		Enable the Instruction-Fetch unit to clear or overwrite instructions
	--		Control path
	--		Exception processing
	--		Miscellaneous

	-- Dispatcher; Decode instructions
	DP_InstructionDecodeA <= InstructionDecode( IF_InstrRegA );
	DP_InstructionDecodeB <= InstructionDecode( IF_InstrRegB );

	-- Dispatcher; Compute branch and jump destinations
	DP_BranchDestinationA <= IF_NextInstrAddrRegA +
					( ( 31 downto 16 => IF_InstrRegA( 15 ) ) & IF_InstrRegA( 15 downto 0 ) );
	DP_BranchDestinationB <= IF_NextInstrAddrRegB +
					( ( 31 downto 16 => IF_InstrRegB( 15 ) ) & IF_InstrRegB( 15 downto 0 ) );

	DP_JumpDestinationA <= IF_NextInstrAddrRegA +
					( ( 31 downto 26 => IF_InstrRegA( 25 ) ) & IF_InstrRegA( 25 downto 0 ) );
	DP_JumpDestinationB <= IF_NextInstrAddrRegB +
					( ( 31 downto 26 => IF_InstrRegB( 25 ) ) & IF_InstrRegB( 25 downto 0 ) );


	-- Dispatcher; Evaluate data for execution units, branch conditions and jump register
	DP_ImmediateDataA <= ( 31 downto 16 => DP_SignExtImmediateA and IF_InstrRegA( 15 ) ) &
												IF_InstrRegA( 15 downto 0 );
	DP_ImmediateDataB <= ( 31 downto 16 => DP_SignExtImmediateB and IF_InstrRegB( 15 ) ) &
												IF_InstrRegB( 15 downto 0 );

	-- The Reorder-Buffer never set the Hit signal for r0 as source.
	DP_DataValidA1 <= not RB_HitA1 or RB_ForwardDataReadyA1;
	DP_ForwardReorderBufferA1 <= "00000" when DP_DataValidA1 ='1' else RB_ForwardReorderBufferA1;
	DP_DataA1 <= RB_ForwardDataA1 when RB_HitA1 = '1' else RF_DataA1;

	DP_DataValidA2 <= DP_OperandImmediateA or not RB_HitA2 or RB_ForwardDataReadyA2;
	DP_ForwardReorderBufferA2 <= "00000" when DP_DataValidA2 ='1' else RB_ForwardReorderBufferA2;
	DP_DataA2 <= DP_ImmediateDataA when DP_OperandImmediateA = '1' else
					RB_ForwardDataA2 when RB_HitA2 = '1' else RF_DataA2;

	-- Check dependence of instruction B to instruction A.
	-- It is not necessary to check for jump and link instructions in stage A. Instruction B
	-- is never executed if instruction A changes the program path.
	DP_SourceB1IsDestinationOfInstrA <=
			Equal( IF_InstrRegB( 25 downto 21 ), DP_RegisterDestinationA ) and
			not Equal( DP_RegisterDestinationA, "00000" ) and
			( DP_InstrTypeAluA or DP_InstrTypeMduA or ( DP_InstrTypeLsuA and DP_WriteBackA ) );
	DP_SourceB2IsDestinationOfInstrA <=
			Equal( IF_InstrRegB( 20 downto 16 ), DP_RegisterDestinationA ) and
			not Equal( DP_RegisterDestinationA, "00000" ) and
			( DP_InstrTypeAluA or DP_InstrTypeMduA or ( DP_InstrTypeLsuA and DP_WriteBackA ) );

	DP_DataValidB1 <=  ( not RB_HitB1 or RB_ForwardDataReadyB1 ) and
														not DP_SourceB1IsDestinationOfInstrA;
	DP_ForwardReorderBufferB1 <=
				"00000" when DP_DataValidB1 = '1' else
					DP_AllocateReorderBufferPointerA when DP_SourceB1IsDestinationOfInstrA = '1'
						else RB_ForwardReorderBufferB1;
	DP_DataB1 <= RB_ForwardDataB1 when RB_HitB1 = '1' else RF_DataB1;


	DP_DataValidB2 <= DP_OperandImmediateB or
					( ( not RB_HitB2 or RB_ForwardDataReadyB2 ) and
														not DP_SourceB2IsDestinationOfInstrA );
	DP_ForwardReorderBufferB2 <=
				"00000" when DP_DataValidB2 = '1' else
					DP_AllocateReorderBufferPointerA when DP_SourceB2IsDestinationOfInstrA = '1'
						else RB_ForwardReorderBufferB2;
	DP_DataB2 <= DP_ImmediateDataB when DP_OperandImmediateB = '1' else
					RB_ForwardDataB2 when RB_HitB2 = '1'else RF_DataB2;

	-- Dispatcher; Evaluate parameters for instruction issue
	DP_TypeR_InstrA <= DP_InstrTypeMduA or ( DP_InstrTypeAluA and not DP_OperandImmediateA );
	DP_TypeR_InstrB <= DP_InstrTypeMduB or ( DP_InstrTypeAluB and not DP_OperandImmediateB );

	DP_RegisterDestinationA <= cLinkRegister when DP_IssueJumpAndLinkA = '1' else
								IF_InstrRegA( 15 downto 11 ) when DP_TypeR_InstrA = '1' else
									IF_InstrRegA( 20 downto 16 );
	DP_RegisterDestinationB <= cLinkRegister when DP_IssueJumpAndLinkB = '1' else
								IF_InstrRegB( 15 downto 11 ) when DP_TypeR_InstrB = '1' else
									IF_InstrRegB( 20 downto 16 );

	DP_SpeculativeA <= not BRU_NoOutstandingSpeculativeBranch;
	DP_SpeculativeB <= not BRU_NoOutstandingSpeculativeBranch or DP_IssueBranchToBRU_A;

	DP_IssueToReorderBufferA <= DP_IssueIllegalInstrError or DP_IssueTransferError or
								DP_IssueAddressTranslationMiss or
								DP_IssueAluA or DP_IssueMduA or	DP_IssueLsuA or
								DP_IssueJumpAndLinkA;
	DP_IssueToReorderBufferB <= DP_IssueAluB or DP_IssueMduB or	DP_IssueLsuB or
								DP_IssueJumpAndLinkB;

	DP_AllocateReorderBufferPointerA <= RB_AvailablePointerA;
	-- All valid entries within the Reorder-Buffer must by in succession.
	DP_AllocateReorderBufferPointerB <= RB_AvailablePointerB when DP_IssueToReorderBufferA = '1'
											else RB_AvailablePointerA;

	DP_ReorderBufferAbleToTakeB <= RB_AbleToTakeB or
									( RB_AbleToTakeA and not DP_IssueToReorderBufferA );

	-- Special issue to Reorder-Buffer:
	-- In the Reorder-Buffer the ReadyFlag, ExceptionFlag and DataReg are written
	-- by the dispatcher for JumpAndLink instructions, illegal instructions,
	-- translation miss or fetch errors.
	-- The dispatcher issues only one of these events in the same clock because they
	-- change the program path.
	-- Errors force the dispatcher to issue an exception request to the Reorder-Buffer.
	-- Only stage A can issue an exception request.
	DP_IssueExceptionRequest <= DP_IssueTransferError or DP_IssueAddressTranslationMiss or
									DP_IssueIllegalInstrError;

	DP_DataForReorderBufferAreReadyA <= DP_IssueJumpAndLinkA or DP_IssueExceptionRequest;
	DP_DataForReorderBufferAreReadyB <= DP_IssueJumpAndLinkB;

	-- The data passed to the Reorder-Buffer depend on the instruction- or error-type.
	-- JumpAndLink: Next instruction address
	-- Translation-Miss error: Effective-Address(31 downto 5) & Exception-Number
	-- Fetch-Error, Illegal instruction: Exception-Number
 	DP_DataOrExceptionToIssue(31 downto 5) <=
		IF_InstrCounterReg(31 downto 5) when DP_IssueAddressTranslationMiss = '1' else
			IF_NextInstrAddrRegA(31 downto 5) when DP_IssueJumpAndLinkA = '1' else
				IF_NextInstrAddrRegB(31 downto 5);
 	DP_DataOrExceptionToIssue(4 downto 0) <=
			cFetchTranslationMissExceptionNumber when DP_IssueAddressTranslationMiss = '1' else
				cFetchTransferErrorExceptionNumber when DP_IssueTransferError = '1' else
					cIllegalInstrExceptionNumber when DP_IssueIllegalInstrError = '1' else
						IF_NextInstrAddrRegA(4 downto 0) when DP_IssueJumpAndLinkA = '1' else
							IF_NextInstrAddrRegB(4 downto 0);

	--------------------------------------------------------------
	-- Dispatcher; Execute or issue all types of instructions	--
	--------------------------------------------------------------
	DP_IssueTransferError <= IF_ValidFlagA and IF_TransferErrorFlagA and
									not DP_AnyPreviousInstrChangesPathA and RB_AbleToTakeA;
	DP_IssueAddressTranslationMiss <= IF_ValidFlagA and IF_AddressTranslationMissFlagA and
									not DP_AnyPreviousInstrChangesPathA and RB_AbleToTakeA;

	DP_InstrValidA <= IF_ValidFlagA and
							not ( IF_TransferErrorFlagA or IF_AddressTranslationMissFlagA );
	DP_InstrValidB <= IF_ValidFlagB and
							not ( IF_TransferErrorFlagB or IF_AddressTranslationMissFlagB );

	-- Dispatcher; illegal instruction, stage A only
	DP_IssueIllegalInstrError <= DP_InstrValidA and DP_InstrTypeIllegalA and
									not DP_AnyPreviousInstrChangesPathA and RB_AbleToTakeA;

	-- Dispatcher; nop instruction, stage A only
	DP_NoOperation <= DP_InstrValidA and DP_InstrTypeNopA;

	-- Dispatcher; ALU, MDU and LSU instructions
	DP_IssueAluA <= DP_InstrValidA and DP_InstrTypeAluA and ALU_AbleToTakeInstrA and
					RB_AbleToTakeA and not DP_AnyPreviousInstrChangesPathA;
	DP_IssueAluB <= DP_InstrValidB and DP_InstrTypeAluB and ALU_AbleToTakeInstrB and
					DP_ReorderBufferAbleToTakeB and not DP_AnyPreviousInstrChangesPathB and
					DP_ExecuteOrIssueInstrA;

	DP_IssueMduA <= DP_InstrValidA and DP_InstrTypeMduA and MDU_AbleToTakeInstrA and
					RB_AbleToTakeA and not DP_AnyPreviousInstrChangesPathA;
	DP_IssueMduB <= DP_InstrValidB and DP_InstrTypeMduB and MDU_AbleToTakeInstrB and
					DP_ReorderBufferAbleToTakeB and not DP_AnyPreviousInstrChangesPathB and
					DP_ExecuteOrIssueInstrA;

	DP_IssueLsuA <= DP_InstrValidA and DP_InstrTypeLsuA and LSU_AbleToTakeInstrA and
					RB_AbleToTakeA and not DP_AnyPreviousInstrChangesPathA;
	DP_IssueLsuB <= DP_InstrValidB and DP_InstrTypeLsuB and LSU_AbleToTakeInstrB and
					DP_ReorderBufferAbleToTakeB and not DP_AnyPreviousInstrChangesPathB and
					DP_ExecuteOrIssueInstrA;

	-- Dispatcher; branch instructions
	-- Branch A
	DP_BranchResolvedA <= DP_InstrValidA and DP_InstrTypeBranchA and DP_DataValidA1;
	DP_BranchUnresolvedA <= DP_InstrValidA and DP_InstrTypeBranchA and not DP_DataValidA1;
	DP_IssueBranchToBRU_A <= DP_BranchUnresolvedA and BRU_NoOutstandingSpeculativeBranch and
								not DP_AnyPreviousInstrChangesPathA;

	-- The Path-Condition is used by the Branch-Resolve-Unit to determine whether the
	-- fetched path is correct or false.
	-- beqz, Decoded-Condition = 0:
	--		Predict taken, 		PredictedBranchFlag = 1 : Path correct if resolved condition = 0
	--		Predict not taken,	PredictedBranchFlag = 0 : Path correct if resolved condition = 1
	-- bnez, Decoded-Condition = 1:
	--		Predict taken, 		PredictedBranchFlag = 1 : Path correct if resolved condition = 1
	--		Predict not taken,	PredictedBranchFlag = 0 : Path correct if resolved condition = 0
	DP_PathConditionA <= IF_PredictedBranchFlagA xnor DP_DecodedConditionA;

	DP_ResolvedConditionA <= not Equal( DP_DataA1, X"0000_0000" );

	-- None Branch-Target-Buffer action is forced if the branch is resolved as taken and
	-- predicted taken, or not taken and predicted not taken (means not predicted).
	-- Note: If a taken branch was predicted correct, the path was changed in the
	--       Instruction-Fetch unit and the dispatcher does not change the path.
	DP_BranchResolvedAsCorrectA <= DP_BranchResolvedA and
										-- take branch						predicted as taken
			( ( DP_ResolvedConditionA xnor DP_DecodedConditionA ) xnor IF_PredictedBranchFlagA );

	DP_BranchResolvedAsFalseA <= DP_BranchResolvedA and
										-- take branch						predicted as taken
			( ( DP_ResolvedConditionA xnor DP_DecodedConditionA ) xor IF_PredictedBranchFlagA );

	-- A taken branch that was not predicted forces the Branch-Target-Buffer to store
	-- this branch. If the branch occurs in program flow again, it will be predicted.
	-- A predicted as taken branch that is not taken deletes its entry out of the
	-- Branch-Target-Buffer. If the branch occurs in program flow again,
	-- it will not be predicted (means predict not taken).
	-- Each action to the Branch-Target-Buffer is done strictly in program order and is
	-- not speculative. Therefore the dispatcher checks if there is an outstanding (unresolved)
	-- branch or if a previous instruction changes the path in this clock cycle.
	DP_TakeBranchPredictedFalseA <= DP_BranchResolvedAsFalseA and IF_PredictedBranchFlagA and
					BRU_NoOutstandingSpeculativeBranch and not DP_AnyPreviousInstrChangesPathA;
	DP_TakeBranchNotPredictedA <= DP_BranchResolvedAsFalseA and not IF_PredictedBranchFlagA and
					BRU_NoOutstandingSpeculativeBranch and not DP_AnyPreviousInstrChangesPathA;

	DP_ExecuteOrIssueBranchA <= DP_IssueBranchToBRU_A or DP_BranchResolvedAsCorrectA or
							DP_TakeBranchPredictedFalseA or DP_TakeBranchNotPredictedA;

	-- Branch B
	DP_BranchResolvedB <= DP_InstrValidB and DP_InstrTypeBranchB and DP_DataValidB1;
	DP_BranchUnResolvedB <= DP_InstrValidB and DP_InstrTypeBranchB and not DP_DataValidB1;
	DP_IssueBranchToBRU_B <= DP_BranchUnresolvedB and BRU_NoOutstandingSpeculativeBranch and
								not DP_AnyPreviousInstrChangesPathB and
								not DP_IssueBranchToBRU_A and DP_ExecuteOrIssueInstrA;
	DP_PathConditionB <= IF_PredictedBranchFlagB xnor DP_DecodedConditionB;
	DP_ResolvedConditionB <= not Equal( DP_DataB1, X"0000_0000" );
	DP_BranchResolvedAsCorrectB <= DP_BranchResolvedB and
			( ( DP_ResolvedConditionB xnor DP_DecodedConditionB ) xnor IF_PredictedBranchFlagB );
	DP_BranchResolvedAsFalseB <= DP_BranchResolvedB and
			( ( DP_ResolvedConditionB xnor DP_DecodedConditionB ) xor IF_PredictedBranchFlagB );
	DP_TakeBranchPredictedFalseB <= DP_BranchResolvedAsFalseB and IF_PredictedBranchFlagB and
					BRU_NoOutstandingSpeculativeBranch and not DP_AnyPreviousInstrChangesPathB and
					not DP_IssueBranchToBRU_A and DP_ExecuteOrIssueInstrA;
	DP_TakeBranchNotPredictedB <= DP_BranchResolvedAsFalseB and not IF_PredictedBranchFlagB and
					BRU_NoOutstandingSpeculativeBranch and not DP_AnyPreviousInstrChangesPathB and
					not DP_IssueBranchToBRU_A and DP_ExecuteOrIssueInstrA;
	DP_ExecuteOrIssueBranchB <= DP_IssueBranchToBRU_B or DP_BranchResolvedAsCorrectB or
							DP_TakeBranchPredictedFalseB or DP_TakeBranchNotPredictedB;

	DP_AlternativePathA <= IF_NextInstrAddrRegA when IF_PredictedBranchFlagA = '1' else
																DP_BranchDestinationA;
	DP_AlternativePathB <= IF_NextInstrAddrRegB when IF_PredictedBranchFlagB = '1' else
																DP_BranchDestinationB;

	-- Dispatcher; JumpAndLink and JumpAndLink-Register instructions
	DP_ExecuteJumpAndLinkA <= DP_InstrValidA and DP_InstrTypeJalA and
							RB_AbleToTakeA and not DP_AnyPreviousInstrChangesPathA;
	DP_ExecuteJumpAndLinkB <= DP_InstrValidB and DP_InstrTypeJalB and DP_ExecuteOrIssueInstrA and
							DP_ReorderBufferAbleToTakeB and not DP_AnyPreviousInstrChangesPathB;

	DP_ExecuteJumpAndLinkRegisterA <= DP_InstrValidA and DP_DataValidA1 and DP_InstrTypeJalrA and
									RB_AbleToTakeA and not DP_AnyPreviousInstrChangesPathA;
							
    DP_ExecuteJumpAndLinkRegisterB <= DP_InstrValidB and DP_DataValidB1 and DP_InstrTypeJalrB and
									DP_ReorderBufferAbleToTakeB and DP_ExecuteOrIssueInstrA and
									not DP_AnyPreviousInstrChangesPathB;

	DP_IssueJumpAndLinkA <= DP_ExecuteJumpAndLinkA or DP_ExecuteJumpAndLinkRegisterA;
	DP_IssueJumpAndLinkB <= DP_ExecuteJumpAndLinkB or DP_ExecuteJumpAndLinkRegisterB;

	-- Dispatcher; Jump and Jump-Register instructions
	-- The new path is forced by the very first instruction that changes the path and
	-- Jump or Jump-Register instructions do not more than changing the path.
	-- For this reason it is not necessary to check if any previous instruction changes the path.
	DP_ExecuteJumpA <= DP_InstrValidA and DP_InstrTypeJ_A;
	DP_ExecuteJumpB <= DP_InstrValidB and DP_InstrTypeJ_B and DP_ExecuteOrIssueInstrA;

	DP_ExecuteJumpRegisterA <= DP_InstrValidA and DP_DataValidA1 and DP_InstrTypeJrA;
	DP_ExecuteJumpRegisterB <= DP_InstrValidB and DP_DataValidB1 and DP_InstrTypeJrB and
								DP_ExecuteOrIssueInstrA;

	DP_JumpRelativeA <= DP_ExecuteJumpA or DP_ExecuteJumpAndLinkA;
	DP_JumpRelativeB <= DP_ExecuteJumpB or DP_ExecuteJumpAndLinkB;

	-- Dispatcher; RFE instruction
	-- If the Reorder-Buffer is empty, only an external interrupt or BIU_TransferErrorStore
	-- may change the path before the RFE instruction. It is possible to execute RFE and
	-- take an external interrupt at the same time.
	DP_ExecuteRfe <= DP_InstrValidA and DP_InstrTypeRfeA and RB_ReorderBufferIsEmpty and
					not BIU_TransferErrorStore;

	-- Dispatcher; Trap instruction
	DP_AbleToExecuteTrap <= DP_InstrValidA and DP_InstrTypeTrapA and RB_ReorderBufferIsEmpty and
							not DP_TakeExternalInterrupt and not BIU_TransferErrorStore;

	DP_WaitUntilWriteBufferIsEmpty <=
						Equal( IF_InstrRegA(8 downto 0), cTrapActionWaitUntilWriteBufferIsEmpty );

	DP_HaltDlx <= DP_AbleToExecuteTrap and
						Equal( IF_InstrRegA(8 downto 0), cTrapActionHalt );
	DP_SystemCall <= DP_AbleToExecuteTrap and
						Equal( IF_InstrRegA(8 downto 0), cTrapActionSystemCall );
	DP_SynchroniseRefetch <= DP_AbleToExecuteTrap and
						Equal( IF_InstrRegA(8 downto 0), cTrapActionRefetch );
	DP_InvalidateWriteBuffer <= DP_AbleToExecuteTrap and
						Equal( IF_InstrRegA(8 downto 0), cTrapActionInvalidateWriteBuffer );
	DP_InvalidateInstructionAddressTranslationBuffer <= DP_AbleToExecuteTrap and
						Equal( IF_InstrRegA(8 downto 0), cTrapActionInvalidateITB );
	DP_InvalidateDataAddressTranslationBuffer <= DP_AbleToExecuteTrap and
						Equal( IF_InstrRegA(8 downto 0), cTrapActionInvalidateDTB );
	DP_InvalidateInstructionCache <= DP_AbleToExecuteTrap and
						Equal( IF_InstrRegA(8 downto 0), cTrapActionInvalidateIC );
	DP_InvalidateDataCache <= DP_AbleToExecuteTrap and
						Equal( IF_InstrRegA(8 downto 0), cTrapActionInvalidateDC );
	DP_InvalidateBranchTargetBuffer <= DP_AbleToExecuteTrap and
						Equal( IF_InstrRegA(8 downto 0), cTrapActionInvalidateBTB );

	-- All other Trap-Numbers have no special action. They are retired from the pipeline if
	-- the Reorder-Buffer is empty.
	DP_ExecuteTrap <=( not DP_WaitUntilWriteBufferIsEmpty and DP_AbleToExecuteTrap ) or
		( DP_WaitUntilWriteBufferIsEmpty and DP_AbleToExecuteTrap and WB_WriteBufferIsEmpty );


	-- Dispatcher; Enable the Instruction-Fetch unit to clear or overwrite instructions
	DP_ExecuteOrIssueInstrA <= DP_NoOperation or DP_ExecuteTrap or
								DP_ChangePathA or DP_ExecuteOrIssueBranchA or
								DP_IssueAluA or DP_IssueMduA or DP_IssueLsuA;
	DP_ExecuteOrIssueInstrB <=
								DP_ChangePathB or DP_ExecuteOrIssueBranchB or
								DP_IssueAluB or DP_IssueMduB or DP_IssueLsuB;

	--------------------------------------------------
	-- Dispatcher; Control path						--
	--------------------------------------------------
	-- Note: If a taken branch was predicted correct, the path was changed in the
	--       Instruction-Fetch unit and the dispatcher does not change the path.
	DP_ChangePathA <= DP_SynchroniseRefetch or DP_ExecuteRfe or
					DP_TakeBranchPredictedFalseA or DP_TakeBranchNotPredictedA or
					DP_JumpRelativeA or DP_ExecuteJumpRegisterA or DP_ExecuteJumpAndLinkRegisterA;
	DP_ChangePathB <= DP_TakeBranchPredictedFalseB or DP_TakeBranchNotPredictedB or
					DP_JumpRelativeB or DP_ExecuteJumpRegisterB or DP_ExecuteJumpAndLinkRegisterB;

	DP_AnyPreviousInstrChangesPathA <= BRU_ChangePath or DP_TakeExternalInterrupt or
													CU_TakeException or BIU_TransferErrorStore;
	DP_AnyPreviousInstrChangesPathB <= DP_AnyPreviousInstrChangesPathA or DP_ChangePathA;

	DP_ChangePath <= DP_AnyPreviousInstrChangesPathB or DP_ChangePathB or DP_TakeException;

	DP_NewPathA <=
			IF_NextInstrAddrRegA when DP_SynchroniseRefetch = '1' else
				DP_ReturnFromExceptionReg(31 downto 2) & "00" when DP_ExecuteRfe = '1' else
					DP_AlternativePathA when DP_ExecuteOrIssueBranchA = '1' else
						DP_JumpDestinationA when DP_JumpRelativeA = '1' else
							RF_DataA1;

	DP_NewPathB <= DP_AlternativePathB when DP_ExecuteOrIssueBranchB = '1' else
						DP_JumpDestinationB when DP_JumpRelativeB = '1' else
							RF_DataB1;

	DP_NewPath <= BRU_AlternativePathReg when BRU_ChangePath = '1' else
					DP_NewPathA when DP_ChangePathA = '1' else DP_NewPathB;

	--------------------------------------------------
	-- Dispatcher; Exception processing				--
	--------------------------------------------------

	-- If an RFE instruction enables an interrupt request, the external interrupt is taken.
	DP_TakeExternalInterrupt <= InterruptRequest and
								( DP_InterruptEnableFlag or
									( DP_ExecuteRfe and DP_ReturnFromExceptionReg(0) ) );

	DP_TakeException <= DP_SystemCall or DP_TakeExternalInterrupt or
							CU_TakeException or BIU_TransferErrorStore;

	-- Evaluate Address of handler
	DP_ExceptionNumber <=
		cStoreTransferErrorExceptionNumber when BIU_TransferErrorStore = '1' else
			cExternalInterruptExceptionNumber when DP_TakeExternalInterrupt = '1' else
				RB_DataCommitA(4 downto 0) when CU_TakeException  = '1' else
					cSystemCallExceptionNumber;

	DP_ExceptionHandlerAddr <=		-- Reserve 256 Bytes per handler
					(31 downto 13 => '0' ) & DP_ExceptionNumber & (7 downto 0 => '0' );

	-- Evaluate Address to resume after exception handling is done
	DP_ReturnFromExceptionRegInput <=
					RB_DataCommitA when CU_WriteToSPR = '1' else
						DP_ResumeAfterExceptionAddr(31 downto 2) & '0' & DP_InterruptEnableFlag;

	-- The Commit-Unit does not commit an instruction during any exception is taken.
	DP_ResumeAfterExceptionAddr <=
					RB_InstrAddrCommitA when RB_ValidCommitA = '1' else
						IF_NextInstrAddrRegA when DP_SystemCall = '1' else
							IF_InstrAddrRegA when IF_ValidFlagA = '1' else
								IF_InstrAddrRegB when IF_ValidFlagB = '1' else
									IF_InstrCounterReg;
		
	-- If an RFE instruction enables an interrupt request, the DP_ReturnFromExceptionReg is
	-- not changed.
	DP_ReturnFromExceptionRegWrite <= ( DP_TakeException and not DP_ExecuteRfe ) or
		( CU_WriteToSPR and Equal( LSU_SPR_NumberReg, cSPRnumberReturnFromExceptionRegister ) );

	DP_InterruptEnableFlagInput <= not DP_TakeException and
				( (       CU_WriteToSPR and RB_DataCommitA(0) ) or				-- Write to SPR
					( not CU_WriteToSPR and DP_ReturnFromExceptionReg(0) ) );	-- Execute RFE

	DP_InterruptEnableFlagWrite <= DP_TakeException or DP_ExecuteRfe or
			( CU_WriteToSPR and Equal( LSU_SPR_NumberReg, cSPRnumberInterruptEnableRegister ) );

	-- The Branch-Target-Buffer uses virtual addresses as tags. The buffer must be cleared
	-- if the context of address translation changes.
	DP_TakeExceptionInvalidateBranchTargetBuffer <= DP_TakeException and DP_InterruptEnableFlag;

	DP_RfeInvalidateBranchTargetBuffer <= DP_ExecuteRfe and
					( DP_ReturnFromExceptionReg(0) xor DP_InterruptEnableFlag );

	----------------------------------
	-- Dispatcher; Miscellaneous	--
	----------------------------------
	DP_InvalidateFetchStage <= DP_ChangePath or DP_HaltDlx;

	DP_KernelMode <= not DP_InterruptEnableFlag or Equal( DP_ProcessIdentifierReg, "0000" );

	DP_ProcessIdentifierRegInput <= RB_DataCommitA(3 downto 0);
	DP_ProcessIdentifierRegWrite <=
			( CU_WriteToSPR and Equal( LSU_SPR_NumberReg, cSPRnumberProcessIdentifierRegister ) );

	--------------------------------------------------------------------------
	--							Reorder-Buffer								--
	--------------------------------------------------------------------------

	-- The Reorder-Buffer is a circular queue. It allows to commit instructions and
	-- take exceptions in program order.
	-- The Reorder-Buffer is described in
	-- "John L. Hennessy and David A. Patterson, Computer Architecture: A Quantitative Approach",
	-- second edition, page 308-312.

	RB_AnyExceptionInsideReorderBuffer <=
				( RB_ValidFlag(0) and RB_ExceptionFlag(0) ) or
				( RB_ValidFlag(1) and RB_ExceptionFlag(1) ) or
				( RB_ValidFlag(2) and RB_ExceptionFlag(2) ) or
				( RB_ValidFlag(3) and RB_ExceptionFlag(3) ) or
				( RB_ValidFlag(4) and RB_ExceptionFlag(4) );

	-- Reorder-Buffer; Load entry
	RB_AllocateEntry <=
			( DP_AllocateReorderBufferPointerA and ( 0 to 4 => DP_IssueToReorderBufferA ) ) or
			( DP_AllocateReorderBufferPointerB and ( 0 to 4 => DP_IssueToReorderBufferB ) );
	RB_CommitInstr <= ( CU_CommitPointerA and ( 0 to 4 => CU_CommitInstrA ) ) or
					  ( CU_CommitPointerB and ( 0 to 4 => CU_CommitInstrB ) );

	LoadReorderBufferEntries : for i in RB_AllocateEntry'range generate
		-- Load Reorder-Buffer entries; Receive data or exception from execution units
		RB_LoadFromALU(i) <= 
			Equal( cReorderBufferPointer(i) & '1', ALU_AllocatedReorderBufferReg & ALU_Ready );
		RB_LoadFromMDU(i) <= 
			Equal( cReorderBufferPointer(i) & '1', MDU_AllocatedReorderBufferReg & MDU_Ready );
		RB_LoadFromLSU(i) <= 
			Equal( cReorderBufferPointer(i) & '1',
												LSU_EA_AllocatedReorderBufferReg & LSU_EA_Ready );

		postponed assert '0' = ( ( RB_ValidFlag(i) or RB_AllocateEntry(i) ) and
							( ( RB_LoadFromALU(i) and RB_LoadFromMDU(i) ) or
				 			  ( RB_LoadFromMDU(i) and RB_LoadFromLSU(i) ) or
							  ( RB_LoadFromALU(i) and RB_LoadFromLSU(i) ) ) )
				report "More than one matching Execution-Unit."
				severity failure;

		RB_DataFromExeUnit(i) <=
				ALU_DataOrExceptionOut when RB_LoadFromALU(i) = '1' else
				MDU_DataOrExceptionOut when RB_LoadFromMDU(i) = '1' else
				LSU_EA_DataOrExceptionOut;
		RB_ExceptionFromExeUnit(i) <=
				ALU_Exception when RB_LoadFromALU(i) = '1' else
				MDU_Exception when RB_LoadFromMDU(i) = '1' else
				LSU_EA_Exception;

		-- Load Reorder-Buffer entries; Control registers and flags
		-- The data part contains the RB_ReadyFlag, RB_ExceptionFlag and RB_DataReg.
		RB_DataPartWrite(i) <= RB_AllocateEntry(i) or
				 RB_LoadFromALU(i) or RB_LoadFromMDU(i) or RB_LoadFromLSU(i);
		postponed assert '0' = ( RB_ValidFlag(i) and RB_ReadyFlag(i) and
						( RB_LoadFromALU(i) or RB_LoadFromMDU(i) or RB_LoadFromLSU(i) ) )
			report
			"It is not possible to load data from execution units that are valid in registers."
			severity failure;

		-- After allocation the RB_ReadyFlag is always set if RB_DataPartWrite = '1' .
		RB_ReadyFlagInput(i) <= not RB_AllocateEntry(i) or		-- After allocation
			(  	  DP_AllocateReorderBufferPointerB(i) and DP_DataForReorderBufferAreReadyB ) or
			( not DP_AllocateReorderBufferPointerB(i) and DP_DataForReorderBufferAreReadyA );

		RB_ExceptionFlagInput(i) <=
						( not RB_AllocateEntry(i) and RB_ExceptionFromExeUnit(i) ) or
						(     RB_AllocateEntry(i) and DP_IssueExceptionRequest   );

		-- Only stage A of the dispatcher can issue data or exceptions.
		RB_DataRegInput(i) <= DP_DataOrExceptionToIssue when RB_AllocateEntry(i) = '1'
									else RB_DataFromExeUnit(i);

		RB_InstrAddrRegInput(i) <= IF_InstrAddrRegB when DP_AllocateReorderBufferPointerB(i) = '1'
											else IF_InstrAddrRegA;
		RB_RegisterDestinationRegInput(i) <=
							DP_RegisterDestinationB when DP_AllocateReorderBufferPointerB(i) = '1'
								else DP_RegisterDestinationA;

		RB_WriteBackFlagInput(i) <= DP_WriteBackB when DP_AllocateReorderBufferPointerB(i) = '1'
											else DP_WriteBackA;

		RB_SpeculativeFlagWrite(i) <= RB_AllocateEntry(i) or BRU_SpeculationCorrect;
		-- The dispatcher does not issue any instruction while BRU_ChangePath is active.
		-- If RB_SpeculativeFlag is written after allocation, speculation was correct and
		-- the flag is cleared.
		-- For this reason the RB_SpeculativeFlag can be set only during allocation.
		RB_SpeculativeFlagInput(i) <= RB_AllocateEntry(i) and
						( (     DP_AllocateReorderBufferPointerB(i) and DP_SpeculativeB ) or
						  ( not DP_AllocateReorderBufferPointerB(i) and DP_SpeculativeA ) ); 

		
		RB_ValidFlagWrite(i) <= RB_CommitInstr(i) or DP_TakeException or RB_AllocateEntry(i) or
								( BRU_ChangePath and RB_SpeculativeFlag(i) );
		-- The RB_ValidFlag can be set only during allocation.
		RB_ValidFlagInput(i) <= RB_AllocateEntry(i);
	end generate;

	-- Reorder-Buffer; forward data
	RB_RegisterRequest(0) <= IF_InstrRegA( 25 downto 21 );
	RB_RegisterRequest(1) <= IF_InstrRegA( 20 downto 16 );
	RB_RegisterRequest(2) <= IF_InstrRegB( 25 downto 21 );
	RB_RegisterRequest(3) <= IF_InstrRegB( 20 downto 16 );

	ForwardDataForAllRequestedRegisters :
									for reg in RB_RegisterRequest'range generate
		CheckAllReorderBufferEntriesForRequestedRegister :
										for entry in RB_RegisterDestinationReg'range generate

			RB_MatchEntry( reg, entry ) <=
						RB_ValidFlag( entry ) and RB_WriteBackFlag( entry ) and
						Equal( RB_RegisterDestinationReg( entry ), RB_RegisterRequest( reg ) );

			------------------------------
			-- Select entry to forward	--
			------------------------------
			-- It is necessary to forward the last matching entry in program order. If this
			-- entry is not ready (data not valid), the dispatcher forwards the number of the
			-- Reorder-Buffer entry to the Reservation-Station.
			-- Note: - The Reorder-Buffer is a circular queue.
			--       - CU_NextCommitPointerReg is a pointer to the next instruction to commit.
			--         This is the first instruction in program order within the Reorder-Buffer.
			-- The following logic evaluates a pointer to the entry to forward. Each bit within
			-- this pointer is determined independently to the other bits. There are four
			-- requested registers and five entries of the Reorder-Buffer. So twenty bits must
			-- be computed.

			-- Strategy of selection <=> evaluate forward-bit of actual entry:

			-- Question:  Does the actual entry match ?
			--       No:  Do not forward actual entry.
			--      Yes:
			--         Question:  Does CU_NextCommitPointerReg point to the following
			--                    entry in the queue ( (entry+1) mod 5 ) ?
			--              Yes:  The actual matching entry is the last instruction in program
			--                    order within the Reorder-Buffer. Forward actual entry.
			--               No:
			--                 Question:  Does the following entry in the queue
			--                            ( (entry+1) mod 5 ) match the requested register ?
			--                      Yes:  There is a later instruction with the same
			--                            destination register. This inhibits the selection of
			--                            the actual entry. Do not forward actual entry.
			--                       No:
			--                            Repeat the last two questions until a decision
			--                            is possible.

			-- The following logic results from this strategy:

			RB_ForwardEntry( reg, entry ) <= RB_MatchEntry( reg, entry ) and
				( CU_NextCommitPointerReg( (entry+1) mod 5 ) or
					( not RB_MatchEntry( reg, (entry+1) mod 5 ) and
						( CU_NextCommitPointerReg( (entry+2) mod 5 ) or
							( not RB_MatchEntry( reg, (entry+2) mod 5 ) and
								( CU_NextCommitPointerReg( (entry+3) mod 5 ) or
									( not RB_MatchEntry( reg, (entry+3) mod 5 ) and
										( CU_NextCommitPointerReg( (entry+4) mod 5 ) or
											not RB_MatchEntry( reg, (entry+4) mod 5 )
				)	)	)	)	)	)	);

		end generate;

		RB_Hit( reg ) <= not Equal( RB_RegisterRequest( reg ), "00000" ) and 
					( RB_MatchEntry(reg, 0) or RB_MatchEntry(reg, 1) or RB_MatchEntry(reg, 2) or
						RB_MatchEntry(reg, 3) or RB_MatchEntry(reg, 4) );

		postponed assert ForwardSelectLogicWorksWell( reg, RB_Hit, CU_NextCommitPointerReg,
																RB_MatchEntry, RB_ForwardEntry )
			report "Forward-Select logic does not work well."
			severity failure;


		-- Read selected entry.
		RB_ForwardReorderBuffer(reg) <=
					cReorderBufferPointer(0) when RB_ForwardEntry( reg, 0 ) = '1' else
					cReorderBufferPointer(1) when RB_ForwardEntry( reg, 1 ) = '1' else
					cReorderBufferPointer(2) when RB_ForwardEntry( reg, 2 ) = '1' else
					cReorderBufferPointer(3) when RB_ForwardEntry( reg, 3 ) = '1' else
					cReorderBufferPointer(4);

		RB_ForwardData(reg) <=
					RB_DataReg(0) when RB_ForwardEntry( reg, 0 ) = '1' else
					RB_DataReg(1) when RB_ForwardEntry( reg, 1 ) = '1' else
					RB_DataReg(2) when RB_ForwardEntry( reg, 2 ) = '1' else
					RB_DataReg(3) when RB_ForwardEntry( reg, 3 ) = '1' else
					RB_DataReg(4);

		RB_ForwardDataReady(reg) <=
					RB_ReadyFlag(0) when RB_ForwardEntry( reg, 0 ) = '1' else
					RB_ReadyFlag(1) when RB_ForwardEntry( reg, 1 ) = '1' else
					RB_ReadyFlag(2) when RB_ForwardEntry( reg, 2 ) = '1' else
					RB_ReadyFlag(3) when RB_ForwardEntry( reg, 3 ) = '1' else
					RB_ReadyFlag(4);
	end generate;


	-- Reorder-Buffer; Read entry of next two instructions to commit
	postponed assert ValidReorderBufferEntriesAreInSuccession( RB_ValidFlag )
		report "Valid Reorder-Buffer entries are not in succession."
		severity failure;

	RB_NextInstrToCommitA <=
			RB_ValidFlag(0) & RB_SpeculativeFlag(0) & RB_InstrAddrReg(0) &
			RB_RegisterDestinationReg(0) & RB_WriteBackFlag(0) & RB_ExceptionFlag(0) &
			RB_ReadyFlag(0) & RB_DataReg(0)
				when CU_CommitPointerA(0) = '1' else
			RB_ValidFlag(1) & RB_SpeculativeFlag(1) & RB_InstrAddrReg(1) &
			RB_RegisterDestinationReg(1) & RB_WriteBackFlag(1) & RB_ExceptionFlag(1) &
			RB_ReadyFlag(1) & RB_DataReg(1)
				when CU_CommitPointerA(1) = '1' else
			RB_ValidFlag(2) & RB_SpeculativeFlag(2) & RB_InstrAddrReg(2) &
			RB_RegisterDestinationReg(2) & RB_WriteBackFlag(2) & RB_ExceptionFlag(2) &
			RB_ReadyFlag(2) & RB_DataReg(2)
				when CU_CommitPointerA(2) = '1' else
			RB_ValidFlag(3) & RB_SpeculativeFlag(3) & RB_InstrAddrReg(3) &
			RB_RegisterDestinationReg(3) & RB_WriteBackFlag(3) & RB_ExceptionFlag(3) &
			RB_ReadyFlag(3) & RB_DataReg(3)
				when CU_CommitPointerA(3) = '1' else
			RB_ValidFlag(4) & RB_SpeculativeFlag(4) & RB_InstrAddrReg(4) &
			RB_RegisterDestinationReg(4) & RB_WriteBackFlag(4) & RB_ExceptionFlag(4) &
			RB_ReadyFlag(4) & RB_DataReg(4);

	RB_NextInstrToCommitB <=
			RB_ValidFlag(0) & RB_SpeculativeFlag(0) & RB_InstrAddrReg(0) &
			RB_RegisterDestinationReg(0) & RB_WriteBackFlag(0) & RB_ExceptionFlag(0) &
			RB_ReadyFlag(0) & RB_DataReg(0)
				when CU_CommitPointerB(0) = '1' else
			RB_ValidFlag(1) & RB_SpeculativeFlag(1) & RB_InstrAddrReg(1) &
			RB_RegisterDestinationReg(1) & RB_WriteBackFlag(1) & RB_ExceptionFlag(1) &
			RB_ReadyFlag(1) & RB_DataReg(1)
				when CU_CommitPointerB(1) = '1' else
			RB_ValidFlag(2) & RB_SpeculativeFlag(2) & RB_InstrAddrReg(2) &
			RB_RegisterDestinationReg(2) & RB_WriteBackFlag(2) & RB_ExceptionFlag(2) &
			RB_ReadyFlag(2) & RB_DataReg(2)
				when CU_CommitPointerB(2) = '1' else
			RB_ValidFlag(3) & RB_SpeculativeFlag(3) & RB_InstrAddrReg(3) &
			RB_RegisterDestinationReg(3) & RB_WriteBackFlag(3) & RB_ExceptionFlag(3) &
			RB_ReadyFlag(3) & RB_DataReg(3)
				when CU_CommitPointerB(3) = '1' else
			RB_ValidFlag(4) & RB_SpeculativeFlag(4) & RB_InstrAddrReg(4) &
			RB_RegisterDestinationReg(4) & RB_WriteBackFlag(4) & RB_ExceptionFlag(4) &
			RB_ReadyFlag(4) & RB_DataReg(4);

	RB_ReorderBufferIsEmpty <= not RB_ValidCommitA;		-- No instruction to commit
	postponed assert '0' = ( RB_ReorderBufferIsEmpty and
					( RB_ValidFlag(0) or RB_ValidFlag(1) or 
						RB_ValidFlag(2) or RB_ValidFlag(3) or RB_ValidFlag(4) ) )
		report "The Reorder-Buffer is not empty but there is no instruction to commit."
		severity failure;

	-- Reorder-Buffer; Evaluate fill pointers
	RB_EntryAvailable <= not RB_ValidFlag or RB_CommitInstr;

	SearchEntriesToFill : for entry in RB_ValidFlag'range generate
		RB_AvailablePointerA( entry ) <=
			( not RB_ValidFlag( entry ) and
				-- 	ReorderBuffer is empty		   previous entry not free
				( CU_NextCommitPointerReg(entry) or RB_ValidFlag( (entry+4) mod 5 ) ) ) or
				-- 	Entry becomes available and previous entry not available
			( RB_CommitInstr( entry ) and not RB_EntryAvailable( (entry+4) mod 5 ) );

		postponed assert '0' = ( not RB_ValidFlag( entry ) and RB_CommitInstr( entry ) )
			report "It is not allowed to commit invalid entries."
			severity failure;

		RB_AvailablePointerB( entry ) <=
					RB_AvailablePointerA( (entry+4) mod 5 ) and RB_EntryAvailable( entry );
	end generate;

	RB_AbleToTakeA <= not Equal( RB_AvailablePointerA, "00000" );
	RB_AbleToTakeB <= not Equal( RB_AvailablePointerB, "00000" );

	--------------------------------------------------------------------------
	--							Branch-Resolve-Unit							--
	--------------------------------------------------------------------------

	-- Branch-Resolve-Unit; Get data to compare
	-- This is a simplified implementation of a Reservation-Station.
	-- For documentation see Arithmetic-Logic-Unit, Reservation-Station, Source 1.

	-- Valid data are used in this cycle and are not needed later. For this reason
	-- the BRU_DataToCompareValidFlag and BRU_DataToCompareReg are written in each clock.

	BRU_ForwardReorderBufferRegInput <=
					DP_ForwardReorderBufferA1 when BRU_AbleToTakeInstrA = '1'
						else DP_ForwardReorderBufferB1;
	BRU_DataToCompareValidFlagInput <=
							BRU_ForwardFromALU or BRU_ForwardFromMDU or BRU_ForwardFromLSU;
	postponed assert '0' = ( BRU_DataToCompareValidFlagInput and BRU_Issue and
					( ( DP_DataValidA1 and BRU_AbleToTakeInstrA ) or
					  ( DP_DataValidB1 and BRU_AbleToTakeInstrB ) ) )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;

	BRU_ForwardTestPointer <= BRU_ForwardReorderBufferRegInput when BRU_Issue = '1'
								else BRU_ForwardReorderBufferReg;
	BRU_ForwardFromALU <= 
		Equal( BRU_ForwardTestPointer & '1', ALU_AllocatedReorderBufferReg & ALU_Forward );
	BRU_ForwardFromMDU <= 
		Equal( BRU_ForwardTestPointer & '1', MDU_AllocatedReorderBufferReg & MDU_Forward );
	BRU_ForwardFromLSU <= 
		Equal( BRU_ForwardTestPointer & '1', LSU_EA_AllocatedReorderBufferReg & LSU_EA_Forward );
	postponed assert '0' = ( BRU_ValidFlag and ( ( BRU_ForwardFromALU and BRU_ForwardFromMDU ) or
										( BRU_ForwardFromMDU and BRU_ForwardFromLSU ) or
										( BRU_ForwardFromALU and BRU_ForwardFromLSU ) ) )
			report "More than one forwarding Execution-Unit."
			severity failure;

	BRU_DataToCompareRegInput <=
				ALU_DataOrExceptionOut when BRU_ForwardFromALU = '1' else
				MDU_DataOrExceptionOut when BRU_ForwardFromMDU = '1' else
				LSU_EA_DataOrExceptionOut;

	-- Branch-Resolve-Unit; Core
	BRU_NoOutstandingSpeculativeBranch <= not BRU_ValidFlag or BRU_ReadyExecution;
	BRU_AbleToTakeInstrA <= DP_BranchUnresolvedA and BRU_NoOutstandingSpeculativeBranch;
	BRU_AbleToTakeInstrB <= DP_BranchUnresolvedB and BRU_NoOutstandingSpeculativeBranch and
							not BRU_AbleToTakeInstrA;

	BRU_Issue <= ( ( BRU_AbleToTakeInstrA and DP_IssueBranchToBRU_A ) or
					( BRU_AbleToTakeInstrB and DP_IssueBranchToBRU_B ) );

	BRU_ValidFlagInput <= BRU_Issue;		-- No issue during path changes.
	BRU_ValidFlagWrite <= DP_TakeException or BRU_Issue or BRU_ReadyExecution;

	BRU_SpeculativePathConditionFlagInput <= DP_PathConditionA when BRU_AbleToTakeInstrA = '1'
													else DP_PathConditionB;

	BRU_PredictionHitFlagInput <= IF_PredictedBranchFlagA when BRU_AbleToTakeInstrA = '1'
										else IF_PredictedBranchFlagB;

	BRU_InstrAddrRegInput <= IF_InstrAddrRegA when BRU_AbleToTakeInstrA = '1'
									else IF_InstrAddrRegB;

	BRU_AlternativePathRegInput <= DP_AlternativePathA when BRU_AbleToTakeInstrA = '1'
										else DP_AlternativePathB;

	BRU_PathCorrect <= BRU_SpeculativePathConditionFlag xor	-- comes from: xnor not Equal
									Equal( BRU_DataToCompareReg, X"0000_0000" );

	BRU_ReadyExecution <= BRU_ValidFlag and BRU_DataToCompareValidFlag;

	BRU_ChangePath <= not BRU_PathCorrect and BRU_ReadyExecution;
	BRU_TakeBranchNotPredicted <= BRU_ChangePath and not BRU_PredictionHitFlag;
	BRU_TakeBranchPredictedFalse <= BRU_ChangePath and BRU_PredictionHitFlag;

	BRU_SpeculationCorrect <= BRU_PathCorrect and BRU_ReadyExecution;

	--------------------------------------------------------------------------
	--							Arithmetic-Logic-Unit						--
	--------------------------------------------------------------------------

	-- Arithmetic-Logic-Unit; Reservation-Station
	-- Source 1
	-- The dispatcher tries to issue valid data to the Reservation-Stations. If the
	-- Reorder-Buffer contains an instruction with the requested source register as destination,
	-- the Register-File is not up to date. If the data within the Reorder-Buffer are ready,
	-- they are passed to the Reservation-Station. If not, a pointer to the Reorder-Buffer entry
	-- that will receive the data is passed to the Reservation-Station. This pointer is compared
	-- with the allocated Reorder-Buffer register of the Execution-Units. If there is a match and
	-- the Execution-Unit forwards data, the Reservation-Station writes them into its internal
	-- register, sets the ALU_SourceDataValidFlag1 and clears ALU_ForwardReorderBufferReg1.

	ALU_ForwardReorderBufferReg1Input <=
							"00000" when ALU_SourceDataValidFlag1Input = '1' else
									ALU_ForwardReorderBuffer1;

	ALU_SourceDataReg1Input <= DP_DataA1 when ALU_AcceptDataA1 = '1' else
									DP_DataB1 when ALU_AcceptDataB1 = '1' else
											ALU_ForwardedDataFromExeUnit1;

	ALU_SourceDataValidFlag1Input <= ( DP_DataValidA1 and ALU_AbleToTakeInstrA ) or	
									 ( DP_DataValidB1 and ALU_AbleToTakeInstrB ) or
									 ALU_ForwardLoad1;
	postponed assert '0' = ( ALU_ForwardLoad1 and ALU_Issue and
					( ( DP_DataValidA1 and ALU_AbleToTakeInstrA ) or
					  ( DP_DataValidB1 and ALU_AbleToTakeInstrB ) ) )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;

	ALU_ForwardReorderBuffer1 <= DP_ForwardReorderBufferA1 when ALU_AbleToTakeInstrA = '1' else
										DP_ForwardReorderBufferB1;

	ALU_AcceptDataA1 <= ALU_Issue and DP_DataValidA1 and ALU_AbleToTakeInstrA;
	ALU_AcceptDataB1 <= ALU_Issue and DP_DataValidB1 and ALU_AbleToTakeInstrB;

	-- The required data may become valid during issue.
	ALU_ForwardTestPointer1 <= ALU_ForwardReorderBuffer1 when ALU_Issue = '1'
								else ALU_ForwardReorderBufferReg1;
	ALU_ForwardFromALU1 <= 
		Equal( ALU_ForwardTestPointer1 & '1', ALU_AllocatedReorderBufferReg & ALU_Forward );
	ALU_ForwardFromMDU1 <= 
		Equal( ALU_ForwardTestPointer1 & '1', MDU_AllocatedReorderBufferReg & MDU_Forward );
	ALU_ForwardFromLSU1 <= 
		Equal( ALU_ForwardTestPointer1 & '1', LSU_EA_AllocatedReorderBufferReg & LSU_EA_Forward );
	postponed assert ( ALU_ValidFlag = '0' ) or ( 1 >= CountHowManyBitsAreSet
						( ALU_ForwardFromALU1 & ALU_ForwardFromMDU1 & ALU_ForwardFromLSU1 ) )
			report "More than one forwarding Execution-Unit."
			severity failure;

	ALU_ForwardedDataFromExeUnit1 <=
				ALU_DataOrExceptionOut when ALU_ForwardFromALU1 = '1' else
				MDU_DataOrExceptionOut when ALU_ForwardFromMDU1 = '1' else
				LSU_EA_DataOrExceptionOut;

	ALU_ForwardLoad1 <= ALU_ForwardFromALU1 or ALU_ForwardFromMDU1 or ALU_ForwardFromLSU1;
	postponed assert '0' = ( ALU_ValidFlag and not ALU_Issue and
									ALU_ForwardLoad1 and ALU_SourceDataValidFlag1 )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;

	-- Write registers: ALU_ForwardReorderBufferReg1, ALU_SourceDataValidFlag1, ALU_SourceDataReg1
	ALU_LoadData1 <= ALU_Issue or ALU_ForwardLoad1;

	-- Source 2
	-- For documentation see Source 1.
	ALU_ForwardReorderBufferReg2Input <=
							"00000" when ALU_SourceDataValidFlag2Input = '1' else
									ALU_ForwardReorderBuffer2;

	ALU_SourceDataReg2Input <= DP_DataA2 when ALU_AcceptDataA2 = '1' else
									DP_DataB2 when ALU_AcceptDataB2 = '1' else
										ALU_ForwardedDataFromExeUnit2;

	ALU_SourceDataValidFlag2Input <= ( DP_DataValidA2 and ALU_AbleToTakeInstrA ) or
										( DP_DataValidB2 and ALU_AbleToTakeInstrB ) or
										ALU_ForwardLoad2;
	postponed assert '0' = ( ALU_ForwardLoad2 and ALU_Issue and
					( ( DP_DataValidA2 and ALU_AbleToTakeInstrA ) or
					  ( DP_DataValidB2 and ALU_AbleToTakeInstrB ) ) )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;

	ALU_ForwardReorderBuffer2 <= DP_ForwardReorderBufferA2 when ALU_AbleToTakeInstrA = '1' else
										DP_ForwardReorderBufferB2;

	ALU_AcceptDataA2 <= ALU_Issue and DP_DataValidA2 and ALU_AbleToTakeInstrA;
	ALU_AcceptDataB2 <= ALU_Issue and DP_DataValidB2 and ALU_AbleToTakeInstrB;

	-- The required data may become valid during issue.
	ALU_ForwardTestPointer2 <= ALU_ForwardReorderBuffer2 when ALU_Issue = '1'
								else ALU_ForwardReorderBufferReg2;
	ALU_ForwardFromALU2 <= 
		Equal( ALU_ForwardTestPointer2 & '1', ALU_AllocatedReorderBufferReg & ALU_Forward );
	ALU_ForwardFromMDU2 <= 
		Equal( ALU_ForwardTestPointer2 & '1', MDU_AllocatedReorderBufferReg & MDU_Forward );
	ALU_ForwardFromLSU2 <= 
		Equal( ALU_ForwardTestPointer2 & '1', LSU_EA_AllocatedReorderBufferReg & LSU_EA_Forward );
	postponed assert ( ALU_ValidFlag = '0' ) or ( 1 >= CountHowManyBitsAreSet
						( ALU_ForwardFromALU2 & ALU_ForwardFromMDU2 & ALU_ForwardFromLSU2 ) )
			report "More than one forwarding Execution-Unit."
			severity failure;

	ALU_ForwardedDataFromExeUnit2 <=
				ALU_DataOrExceptionOut when ALU_ForwardFromALU2 = '1' else
				MDU_DataOrExceptionOut when ALU_ForwardFromMDU2 = '1' else
				LSU_EA_DataOrExceptionOut;

	ALU_ForwardLoad2 <= ALU_ForwardFromALU2 or ALU_ForwardFromMDU2 or ALU_ForwardFromLSU2;
	postponed assert '0' = ( ALU_ValidFlag and not ALU_Issue and
									ALU_ForwardLoad2 and ALU_SourceDataValidFlag2 )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;
	ALU_LoadData2 <= ALU_Issue or ALU_ForwardLoad2;

	-- Arithmetic-Logic-Unit; Core
	ALU_Available <= not ALU_ValidFlag or ALU_Ready;
	ALU_AbleToTakeInstrA <= DP_InstrTypeAluA and ALU_Available;
	ALU_AbleToTakeInstrB <= DP_InstrTypeAluB and ALU_Available and not ALU_AbleToTakeInstrA;

	ALU_Issue <= ( ( ALU_AbleToTakeInstrA and DP_IssueAluA ) or
				   ( ALU_AbleToTakeInstrB and DP_IssueAluB ) );

	ALU_SpeculativeFlagWrite <= ALU_Issue or BRU_SpeculationCorrect;
	-- If ALU_SpeculativeFlag is written after issue, speculation was correct and
	-- the flag is cleared.
	-- For this reason the ALU_SpeculativeFlag can be set only during issue.
	ALU_SpeculativeFlagInput <= ALU_Issue and
					( ( DP_SpeculativeA and ALU_AbleToTakeInstrA ) or
					  ( DP_SpeculativeB and ALU_AbleToTakeInstrB ) );

	-- The dispatcher does not issue any instruction while DP_ChangePath is active.
	-- The ALU_ValidFlag can be set only during issue.
	ALU_ValidFlagInput <= ALU_Issue;
	ALU_ValidFlagWrite <= DP_TakeException or ALU_Issue or ALU_Ready or
							( BRU_ChangePath and ALU_SpeculativeFlag );

	ALU_AllocatedReorderBufferRegInput <=
							DP_AllocateReorderBufferPointerA when ALU_AbleToTakeInstrA = '1'
								else DP_AllocateReorderBufferPointerB;
	ALU_DecoderInfoRegInput <= DP_AluFunctionA when ALU_AbleToTakeInstrA = '1'
									else DP_AluFunctionB;

	ALU_AluOutput <= Alu( ALU_SourceDataReg1, ALU_SourceDataReg2, ALU_DecoderInfoReg );

	ALU_DataOrExceptionOut(31 downto 5) <= ALU_AluOutputData(31 downto 5);
	ALU_DataOrExceptionOut(4 downto 0) <=
							cArithmeticErrorExceptionNumber when ALU_AluOutputError = '1'
								else ALU_AluOutputData(4 downto 0);

	ALU_Ready <= ALU_SourceDataValidFlag1 and ALU_SourceDataValidFlag2 and ALU_ValidFlag;
	ALU_Forward <= ALU_Ready and not ALU_AluOutputError;
	ALU_Exception <= ALU_AluOutputError;

	--------------------------------------------------------------------------
	--							Multiply-Divide-Unit						--
	--------------------------------------------------------------------------

	-- Multiply-Divide-Unit; Reservation-Station
	-- Source 1
	-- For documentation see Arithmetic-Logic-Unit, Reservation-Station, Source 1.
	MDU_ForwardReorderBufferReg1Input <=
							"00000" when MDU_SourceDataValidFlag1Input = '1' else
									MDU_ForwardReorderBuffer1;

	MDU_SourceDataReg1Input <= DP_DataA1 when MDU_AcceptDataA1 = '1' else
									DP_DataB1 when MDU_AcceptDataB1 = '1' else
										MDU_ForwardedDataFromExeUnit1;

	MDU_SourceDataValidFlag1Input <= ( DP_DataValidA1 and MDU_AbleToTakeInstrA ) or
										( DP_DataValidB1 and MDU_AbleToTakeInstrB ) or
										MDU_ForwardLoad1;
	postponed assert '0' = ( MDU_ForwardLoad1 and MDU_Issue and
					( ( DP_DataValidA1 and MDU_AbleToTakeInstrA ) or
					  ( DP_DataValidB1 and MDU_AbleToTakeInstrB ) ) )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;

	MDU_ForwardReorderBuffer1 <= DP_ForwardReorderBufferA1 when MDU_AbleToTakeInstrA = '1' else
										DP_ForwardReorderBufferB1;

	MDU_AcceptDataA1 <= MDU_Issue and DP_DataValidA1 and MDU_AbleToTakeInstrA;
	MDU_AcceptDataB1 <= MDU_Issue and DP_DataValidB1 and MDU_AbleToTakeInstrB;

	-- The required data may become valid during issue.
	MDU_ForwardTestPointer1 <= MDU_ForwardReorderBuffer1 when MDU_Issue = '1'
								else MDU_ForwardReorderBufferReg1;
	MDU_ForwardFromALU1 <= 
		Equal( MDU_ForwardTestPointer1 & '1', ALU_AllocatedReorderBufferReg & ALU_Forward );
	MDU_ForwardFromMDU1 <= 
		Equal( MDU_ForwardTestPointer1 & '1', MDU_AllocatedReorderBufferReg & MDU_Forward );
	MDU_ForwardFromLSU1 <= 
		Equal( MDU_ForwardTestPointer1 & '1', LSU_EA_AllocatedReorderBufferReg & LSU_EA_Forward );
	postponed assert ( MDU_ValidFlag = '0' ) or ( 1 >= CountHowManyBitsAreSet
						( MDU_ForwardFromALU1 & MDU_ForwardFromMDU1 & MDU_ForwardFromLSU1 ) )
			report "More than one forwarding Execution-Unit."
			severity failure;

	MDU_ForwardedDataFromExeUnit1 <=
				ALU_DataOrExceptionOut when MDU_ForwardFromALU1 = '1' else
				MDU_DataOrExceptionOut when MDU_ForwardFromMDU1 = '1' else
				LSU_EA_DataOrExceptionOut;

	MDU_ForwardLoad1 <= MDU_ForwardFromALU1 or MDU_ForwardFromMDU1 or MDU_ForwardFromLSU1;
	postponed assert '0' = ( MDU_ValidFlag and not MDU_Issue and
									MDU_ForwardLoad1 and MDU_SourceDataValidFlag1 )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;
	MDU_LoadData1 <= MDU_Issue or MDU_ForwardLoad1;

	-- Source 2
	-- For documentation see Arithmetic-Logic-Unit, Reservation-Station, Source 1.
	MDU_ForwardReorderBufferReg2Input <=
							"00000" when MDU_SourceDataValidFlag2Input = '1' else
									MDU_ForwardReorderBuffer2;

	MDU_SourceDataReg2Input <= DP_DataA2 when MDU_AcceptDataA2 = '1' else
									DP_DataB2 when MDU_AcceptDataB2 = '1' else
										MDU_ForwardedDataFromExeUnit2;

	MDU_SourceDataValidFlag2Input <= ( DP_DataValidA2 and MDU_AbleToTakeInstrA ) or
										( DP_DataValidB2 and MDU_AbleToTakeInstrB ) or
										MDU_ForwardLoad2;
	postponed assert '0' = ( MDU_ForwardLoad2 and MDU_Issue and
					( ( DP_DataValidA2 and MDU_AbleToTakeInstrA ) or
					  ( DP_DataValidB2 and MDU_AbleToTakeInstrB ) ) )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;

	MDU_ForwardReorderBuffer2 <= DP_ForwardReorderBufferA2 when MDU_AbleToTakeInstrA = '1' else
										DP_ForwardReorderBufferB2;

	MDU_AcceptDataA2 <= MDU_Issue and DP_DataValidA2 and MDU_AbleToTakeInstrA;
	MDU_AcceptDataB2 <= MDU_Issue and DP_DataValidB2 and MDU_AbleToTakeInstrB;

	-- The required data may become valid during issue.
	MDU_ForwardTestPointer2 <= MDU_ForwardReorderBuffer2 when MDU_Issue = '1'
								else MDU_ForwardReorderBufferReg2;
	MDU_ForwardFromALU2 <= 
		Equal( MDU_ForwardTestPointer2 & '1', ALU_AllocatedReorderBufferReg & ALU_Forward );
	MDU_ForwardFromMDU2 <= 
		Equal( MDU_ForwardTestPointer2 & '1', MDU_AllocatedReorderBufferReg & MDU_Forward );
	MDU_ForwardFromLSU2 <= 
		Equal( MDU_ForwardTestPointer2 & '1', LSU_EA_AllocatedReorderBufferReg & LSU_EA_Forward );
	postponed assert ( MDU_ValidFlag = '0' ) or ( 1 >= CountHowManyBitsAreSet
						( MDU_ForwardFromALU2 & MDU_ForwardFromMDU2 & MDU_ForwardFromLSU2 ) )
			report "More than one forwarding Execution-Unit."
			severity failure;

	MDU_ForwardedDataFromExeUnit2 <=
				ALU_DataOrExceptionOut when MDU_ForwardFromALU2 = '1' else
				MDU_DataOrExceptionOut when MDU_ForwardFromMDU2 = '1' else
				LSU_EA_DataOrExceptionOut;

	MDU_ForwardLoad2 <= MDU_ForwardFromALU2 or MDU_ForwardFromMDU2 or MDU_ForwardFromLSU2;
	postponed assert '0' = ( MDU_ValidFlag and not MDU_Issue and
									MDU_ForwardLoad2 and MDU_SourceDataValidFlag2 )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;
	MDU_LoadData2 <= MDU_Issue or MDU_ForwardLoad2;

	-- Multiply-Divide-Unit; Core
	MDU_Available <= not MDU_ValidFlag or MDU_Ready;
	MDU_AbleToTakeInstrA <= DP_InstrTypeMduA and MDU_Available;
	MDU_AbleToTakeInstrB <= DP_InstrTypeMduB and MDU_Available and not MDU_AbleToTakeInstrA;

	MDU_Issue <= ( ( MDU_AbleToTakeInstrA and DP_IssueMduA ) or
				   ( MDU_AbleToTakeInstrB and DP_IssueMduB ) );

	-- The MDU-function of the Dlx-Package controls a clock counter. If the data for the
	-- MDU logic change, the counter is cleared otherwise it is incremented.
	-- MDU_ResultReady of the MDU-function is set if the counter reaches the delay of the
	-- requested function.
	MDU_DataChanged <= MDU_Issue or MDU_LoadData1 or MDU_LoadData2;

	MDU_SpeculativeFlagWrite <= MDU_Issue or BRU_SpeculationCorrect;
	-- If MDU_SpeculativeFlag is written after issue, speculation was correct and
	-- the flag is cleared.
	-- For this reason the MDU_SpeculativeFlag can be set only during issue.
	MDU_SpeculativeFlagInput <= MDU_Issue and
					( ( DP_SpeculativeA and MDU_AbleToTakeInstrA ) or
					  ( DP_SpeculativeB and MDU_AbleToTakeInstrB ) );
              
	-- The dispatcher does not issue any instruction while DP_ChangePath is active.
	-- The MDU_ValidFlag can be set only during issue.
	MDU_ValidFlagInput <= MDU_Issue;
	MDU_ValidFlagWrite <= DP_TakeException or MDU_Issue or MDU_Ready or
							( BRU_ChangePath and MDU_SpeculativeFlag );

	MDU_AllocatedReorderBufferRegInput <=
							DP_AllocateReorderBufferPointerA when MDU_AbleToTakeInstrA = '1'
								else DP_AllocateReorderBufferPointerB;
	MDU_DecoderInfoRegInput <= DP_MduFunctionA when MDU_AbleToTakeInstrA = '1'
									else DP_MduFunctionB;

	MDU_MduOutput <= Mdu( MDU_SourceDataReg1, MDU_SourceDataReg2, MDU_DecoderInfoReg,
													MDU_ClockCounterReg, MDU_DataChanged );

	MDU_DataOrExceptionOut(31 downto 5) <= MDU_MduOutputData(31 downto 5);
	MDU_DataOrExceptionOut(4 downto 0) <=
			cDivideByZeroExceptionNumber when MDU_DivideByZero = '1' else
				cMultiplyDivideOverflowExceptionNumber when MDU_MultiplyDivideOverflow = '1' else
						MDU_MduOutputData(4 downto 0);

	MDU_Ready <= MDU_SourceDataValidFlag1 and MDU_SourceDataValidFlag2 and MDU_ValidFlag and
							MDU_ResultReady;

	MDU_Forward <= MDU_Ready and not MDU_Exception;
	MDU_Exception <= MDU_DivideByZero or MDU_MultiplyDivideOverflow;

	--------------------------------------------------------------------------
	--							Load-Store-Unit								--
	--------------------------------------------------------------------------

	-- Load-Store-Unit; Reservation-Station
	-- Source 1
	-- For documentation see Arithmetic-Logic-Unit, Reservation-Station, Source 1.
	LSU_ForwardReorderBufferReg1Input <=
							"00000" when LSU_SourceDataValidFlag1Input = '1' else
									LSU_ForwardReorderBuffer1;

	LSU_SourceDataReg1Input <= DP_DataA1 when LSU_AcceptDataA1 = '1' else
									DP_DataB1 when LSU_AcceptDataB1 = '1' else
										LSU_ForwardedDataFromExeUnit1;

	LSU_SourceDataValidFlag1Input <= ( DP_DataValidA1 and LSU_AbleToTakeInstrA ) or
										( DP_DataValidB1 and LSU_AbleToTakeInstrB ) or
										LSU_ForwardLoad1;
	postponed assert '0' = ( LSU_ForwardLoad1 and LSU_Issue and
					( ( DP_DataValidA1 and LSU_AbleToTakeInstrA ) or
					  ( DP_DataValidB1 and LSU_AbleToTakeInstrB ) ) )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;

	LSU_ForwardReorderBuffer1 <= DP_ForwardReorderBufferA1 when LSU_AbleToTakeInstrA = '1' else
										DP_ForwardReorderBufferB1;

	LSU_AcceptDataA1 <= LSU_Issue and DP_DataValidA1 and LSU_AbleToTakeInstrA;
	LSU_AcceptDataB1 <= LSU_Issue and DP_DataValidB1 and LSU_AbleToTakeInstrB;

	-- The required data may become valid during issue.
	LSU_ForwardTestPointer1 <= LSU_ForwardReorderBuffer1 when LSU_Issue = '1'
								else LSU_ForwardReorderBufferReg1;
	LSU_ForwardFromALU1 <= 
		Equal( LSU_ForwardTestPointer1 & '1', ALU_AllocatedReorderBufferReg & ALU_Forward );
	LSU_ForwardFromMDU1 <= 
		Equal( LSU_ForwardTestPointer1 & '1', MDU_AllocatedReorderBufferReg & MDU_Forward );
	LSU_ForwardFromLSU1 <= 
		Equal( LSU_ForwardTestPointer1 & '1', LSU_EA_AllocatedReorderBufferReg & LSU_EA_Forward );
	postponed assert ( LSU_ValidFlag = '0' ) or ( 1 >= CountHowManyBitsAreSet
						( LSU_ForwardFromALU1 & LSU_ForwardFromMDU1 & LSU_ForwardFromLSU1 ) )
			report "More than one forwarding Execution-Unit."
			severity failure;

	LSU_ForwardedDataFromExeUnit1 <=
				ALU_DataOrExceptionOut when LSU_ForwardFromALU1 = '1' else
				MDU_DataOrExceptionOut when LSU_ForwardFromMDU1 = '1' else
				LSU_EA_DataOrExceptionOut;

	LSU_ForwardLoad1 <= LSU_ForwardFromALU1 or LSU_ForwardFromMDU1 or LSU_ForwardFromLSU1;
	postponed assert '0' = ( LSU_ValidFlag and not LSU_Issue and
									LSU_ForwardLoad1 and LSU_SourceDataValidFlag1 )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;
	LSU_LoadData1 <= LSU_Issue or LSU_ForwardLoad1;

	-- Source 2
	-- For documentation see Arithmetic-Logic-Unit, Reservation-Station, Source 1.
	LSU_ForwardReorderBufferReg2Input <=
							"00000" when LSU_SourceDataValidFlag2Input = '1' else
									LSU_ForwardReorderBuffer2;

	LSU_SourceDataReg2Input <= DP_DataA2 when LSU_AcceptDataA2 = '1' else
									DP_DataB2 when LSU_AcceptDataB2 = '1' else
										LSU_ForwardedDataFromExeUnit2;

	LSU_SourceDataValidFlag2Input <= ( DP_DataValidA2 and LSU_AbleToTakeInstrA ) or
										( DP_DataValidB2 and LSU_AbleToTakeInstrB ) or
										LSU_ForwardLoad2;
	postponed assert '0' = ( LSU_ForwardLoad2 and LSU_Issue and
					( ( DP_DataValidA2 and LSU_AbleToTakeInstrA ) or
					  ( DP_DataValidB2 and LSU_AbleToTakeInstrB ) ) )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;

	LSU_ForwardReorderBuffer2 <= DP_ForwardReorderBufferA2 when LSU_AbleToTakeInstrA = '1' else
										DP_ForwardReorderBufferB2;

	LSU_AcceptDataA2 <= LSU_Issue and DP_DataValidA2 and LSU_AbleToTakeInstrA;
	LSU_AcceptDataB2 <= LSU_Issue and DP_DataValidB2 and LSU_AbleToTakeInstrB;

	-- The required data may become valid during issue.
	LSU_ForwardTestPointer2 <= LSU_ForwardReorderBuffer2 when LSU_Issue = '1'
								else LSU_ForwardReorderBufferReg2;
	LSU_ForwardFromALU2 <= 
		Equal( LSU_ForwardTestPointer2 & '1', ALU_AllocatedReorderBufferReg & ALU_Forward );
	LSU_ForwardFromMDU2 <= 
		Equal( LSU_ForwardTestPointer2 & '1', MDU_AllocatedReorderBufferReg & MDU_Forward );
	LSU_ForwardFromLSU2 <= 
		Equal( LSU_ForwardTestPointer2 & '1', LSU_EA_AllocatedReorderBufferReg & LSU_EA_Forward );
	postponed assert ( LSU_ValidFlag = '0' ) or ( 1 >= CountHowManyBitsAreSet
						( LSU_ForwardFromALU2 & LSU_ForwardFromMDU2 & LSU_ForwardFromLSU2 ) )
			report "More than one forwarding Execution-Unit."
			severity failure;

	LSU_ForwardedDataFromExeUnit2 <=
				ALU_DataOrExceptionOut when LSU_ForwardFromALU2 = '1' else
				MDU_DataOrExceptionOut when LSU_ForwardFromMDU2 = '1' else
				LSU_EA_DataOrExceptionOut;

	LSU_ForwardLoad2 <= LSU_ForwardFromALU2 or LSU_ForwardFromMDU2 or LSU_ForwardFromLSU2;
	postponed assert '0' = ( LSU_ValidFlag and not LSU_Issue and
									LSU_ForwardLoad2 and LSU_SourceDataValidFlag2 )
	report "It is not possible to forward data from Execution-Unit that are valid in registers."
			severity failure;
	LSU_LoadData2 <= LSU_Issue or LSU_ForwardLoad2;

	----------------------------------
	-- Load-Store-Unit; Data stage	--
	----------------------------------
	LSU_Available <= not LSU_ValidFlag or ( LSU_Ready and LSU_EA_StageAvaliable );
	LSU_AbleToTakeInstrA <= DP_InstrTypeLsuA and LSU_Available;
	LSU_AbleToTakeInstrB <= DP_InstrTypeLsuB and LSU_Available and not LSU_AbleToTakeInstrA;

	LSU_Issue <= ( ( LSU_AbleToTakeInstrA and DP_IssueLsuA ) or
				   ( LSU_AbleToTakeInstrB and DP_IssueLsuB ) );

	LSU_SpeculativeFlagWrite <= LSU_Issue or BRU_SpeculationCorrect;
	-- If LSU_SpeculativeFlag is written after issue, speculation was correct and
	-- the flag is cleared.
	-- For this reason the LSU_SpeculativeFlag can be set only during issue.
	LSU_SpeculativeFlagInput <= LSU_Issue and
					( ( DP_SpeculativeA and LSU_AbleToTakeInstrA ) or
					  ( DP_SpeculativeB and LSU_AbleToTakeInstrB ) );

	-- The dispatcher does not issue any instruction while DP_ChangePath is active.
	-- The LSU_ValidFlag can be set only during issue.
	LSU_ValidFlagInput <= LSU_Issue;
	LSU_ValidFlagWrite <= DP_TakeException or LSU_Issue or
			( LSU_Ready and LSU_EA_StageAvaliable ) or ( BRU_ChangePath and LSU_SpeculativeFlag );

	LSU_AllocatedReorderBufferRegInput <=
							DP_AllocateReorderBufferPointerA when LSU_AbleToTakeInstrA = '1'
								else DP_AllocateReorderBufferPointerB;
	LSU_InstructionLoadFlagInput <= DP_WriteBackA when LSU_AbleToTakeInstrA = '1'
										else DP_WriteBackB;
	LSU_OffsetRegInput <= IF_InstrRegA( 15 downto 0 ) when LSU_AbleToTakeInstrA = '1'
								else IF_InstrRegB( 15 downto 0 );
	LSU_ByteEnableFlagInput <= DP_ByteEnableA when LSU_AbleToTakeInstrA = '1'
									else DP_ByteEnableB;
	LSU_SignExtensionFlagInput <= DP_SignExtLoadA  when LSU_AbleToTakeInstrA = '1'
									else DP_SignExtLoadB;

	LSU_Ready <= LSU_SourceDataValidFlag1 and						 -- Source1 always required
			( LSU_SourceDataValidFlag2 or LSU_InstructionLoadFlag ); -- Source2 required for store

	--------------------------------------------------
	-- Load-Store-Unit; Effective address stage		--
	--------------------------------------------------
	-- Load-Store-Unit; Effective address stage; Control
	LSU_EA_AddrRegInput <= LSU_SourceDataReg1 +
						( ( 31 downto 16 => LSU_OffsetReg(15) ) & LSU_OffsetReg(15 downto 0) );

	-- Special-Purpose-Registers are mapped in the upper 128 bytes (one page) in
	-- physical address space.
	LSU_EA_SpecialPurposeRegisterAccess <= Equal( DTB_PpageOut, ( 31 downto 7 => '1' ) );

	LSU_EA_StageAvaliable <= not LSU_EA_ValidFlag or LSU_EA_Ready;

	LSU_EA_ReadyAccessSPR <= LSU_EA_SpecialPurposeRegisterAccess and
		( LSU_EA_AlignmentError or
			( LSU_EA_InstructionLoadFlag and ( not LSU_EA_ForwardWriteSPR or CU_WriteToSPR ) ) or
			( not LSU_EA_InstructionLoadFlag and LSU_SPR_StageAvailable ) );

	LSU_EA_Ready <= LSU_EA_ValidFlag and
		( LSU_EA_ReadyAccessSPR or
			( not LSU_EA_SpecialPurposeRegisterAccess and
				( LSU_EA_Exception or
					( DC_DataValid and LSU_EA_InstructionLoadFlag ) or				-- Load ready
					( WB_AbleToTakeStore and not LSU_EA_InstructionLoadFlag ))));	-- Store ready

	LSU_EA_SpeculativeFlagInput <= LSU_SpeculativeFlag and not BRU_SpeculationCorrect;
	LSU_EA_SpeculativeFlagWrite <= LSU_EA_StageAvaliable or BRU_SpeculationCorrect;

	LSU_EA_ValidFlagInput <= not DP_TakeException and
								LSU_ValidFlag and LSU_Ready and
								not ( LSU_SpeculativeFlag and BRU_ChangePath );
								
	LSU_EA_ValidFlagWrite <= DP_TakeException or LSU_EA_StageAvaliable or LSU_EA_Ready or
							( BRU_ChangePath and LSU_EA_SpeculativeFlag );

	LSU_EA_AlignmentError <=
		( Equal( LSU_EA_ByteEnableFlag,"0011") and LSU_EA_AddrReg(0) ) or
		( Equal( LSU_EA_ByteEnableFlag,"1111") and (LSU_EA_AddrReg(1) or LSU_EA_AddrReg(0) ) ) or
		( LSU_EA_SpecialPurposeRegisterAccess and not Equal( LSU_EA_ByteEnableFlag,"1111") );

	LSU_EA_CacheRequestDC <= LSU_EA_ValidFlag and not LSU_EA_AlignmentError and
								not LSU_EA_SpecialPurposeRegisterAccess;

	LSU_EA_Exception <= LSU_EA_AlignmentError or BIU_TransferErrorLoad or
						DTB_LoadMiss or DTB_StoreMiss;

	LSU_EA_Forward <= LSU_EA_Ready and not LSU_EA_Exception and LSU_EA_InstructionLoadFlag;

	-- Load-Store-Unit; Effective address stage; Access
	-- Shift and extend incoming data
	LSU_EA_LoadByte3To0 <=
			not ( LSU_EA_AddrReg(1) or LSU_EA_AddrReg(0) or LSU_EA_ByteEnableFlag(1) );
	LSU_EA_LoadByte2To0 <=
			( not LSU_EA_AddrReg(1) and LSU_EA_AddrReg(0) and not LSU_EA_ByteEnableFlag(1) ) or
					( not LSU_EA_AddrReg(1) and not LSU_EA_AddrReg(0) and
									not LSU_EA_ByteEnableFlag(2) and LSU_EA_ByteEnableFlag(1) );
	LSU_EA_LoadByte1To0 <=
				not ( LSU_EA_AddrReg(0) or LSU_EA_ByteEnableFlag(1) ) and LSU_EA_AddrReg(1);

	LSU_EA_LoadByte3To1 <= LSU_EA_ByteEnableFlag(1) and
						not ( LSU_EA_AddrReg(1) or LSU_EA_AddrReg(0) or LSU_EA_ByteEnableFlag(2) );
	LSU_EA_LoadByte1To1 <= LSU_EA_ByteEnableFlag(2) or
					( LSU_EA_AddrReg(1) and not LSU_EA_AddrReg(0) and LSU_EA_ByteEnableFlag(1) );

	LSU_EA_ExtendedData( 7 downto 0) <=
				DC_DataOut(31 downto 24) when LSU_EA_LoadByte3To0 = '1' else
				DC_DataOut(23 downto 16) when LSU_EA_LoadByte2To0 = '1' else
				DC_DataOut(15 downto  8) when LSU_EA_LoadByte1To0 = '1' else
				DC_DataOut( 7 downto  0);
	LSU_EA_ExtendedData(15 downto 8) <=
				DC_DataOut(31 downto 24) when LSU_EA_LoadByte3To1 = '1' else
				DC_DataOut(15 downto  8) when LSU_EA_LoadByte1To1 = '1' else
					(15 downto 8 => LSU_EA_SignExtensionFlag and LSU_EA_ExtendedData(7) );
	LSU_EA_ExtendedData(31 downto 16) <=
				DC_DataOut(31 downto 16) when LSU_EA_ByteEnableFlag(2) = '1' else
					(31 downto 16 => LSU_EA_SignExtensionFlag and LSU_EA_ExtendedData(15) );

	-- Shift and expand data to store, used by Write-Buffer
	LSU_EA_StoreByte0To3 <= LSU_EA_LoadByte3To0;
	LSU_EA_StoreByte1To3 <= LSU_EA_LoadByte3To1;

	LSU_EA_StoreByte0To2 <= LSU_EA_LoadByte2To0;

	LSU_EA_StoreByte0To1 <= LSU_EA_LoadByte1To0;
	LSU_EA_StoreByte1To1 <= LSU_EA_LoadByte1To1;

	LSU_EA_StoreByte0To0 <= LSU_EA_StoreByte1To1 or
			( LSU_EA_AddrReg(1) and LSU_EA_AddrReg(0) and not LSU_EA_ByteEnableFlag(1) );

	LSU_EA_DataToStore( 7 downto 0 ) <= LSU_EA_DataToStoreReg( 7 downto 0 );
	LSU_EA_DataToStore( 15 downto 8 ) <=
				LSU_EA_DataToStoreReg( 7 downto 0 ) when LSU_EA_StoreByte0To1 = '1' else
				LSU_EA_DataToStoreReg( 15 downto 8 );
	LSU_EA_DataToStore( 23 downto 16 ) <=
				LSU_EA_DataToStoreReg( 7 downto 0 ) when LSU_EA_StoreByte0To2 = '1' else
				LSU_EA_DataToStoreReg( 23 downto 16 );
	LSU_EA_DataToStore( 31 downto 24 ) <=
				LSU_EA_DataToStoreReg( 7 downto 0 ) when LSU_EA_StoreByte0To3 = '1' else
				LSU_EA_DataToStoreReg( 15 downto 8 ) when LSU_EA_StoreByte1To3 = '1' else
				LSU_EA_DataToStoreReg( 31 downto 24 );

	LSU_EA_ByteEnable(3) <=
				LSU_EA_StoreByte0To3 or LSU_EA_StoreByte1To3 or LSU_EA_ByteEnableFlag(2);
	LSU_EA_ByteEnable(2) <=
				LSU_EA_StoreByte0To2 or LSU_EA_ByteEnableFlag(2);
	LSU_EA_ByteEnable(1) <=
				LSU_EA_StoreByte0To1 or LSU_EA_StoreByte1To1 or LSU_EA_ByteEnableFlag(2);
	LSU_EA_ByteEnable(0) <=
				LSU_EA_StoreByte0To0;

	LSU_EA_DataToStoreFanout <= LSU_EA_DataToStore & LSU_EA_DataToStore;
	LSU_EA_ByteEnableFanout <=
			( LSU_EA_ByteEnable and ( 7 downto 4 => not LSU_EA_AddrReg(2) ) ) &
			( LSU_EA_ByteEnable and ( 3 downto 0 =>     LSU_EA_AddrReg(2) ) );

	-- Load from Special-Purpose-Register
	LSU_EA_ForwardWriteSPR <=
					LSU_EA_SpecialPurposeRegisterAccess and LSU_EA_InstructionLoadFlag and
					LSU_SPR_ValidFlag and
							-- A SPR-store waits to write in the requested SP-register
					Equal ( LSU_EA_AddrReg(6 downto 2), LSU_SPR_NumberReg ) and
							-- ITB_PhysicalPageRegister and DTB_PhysicalPageRegister are write
							-- only registers in programmer's view.
							-- Reading this registers results in X"0000_0000".
					not ( Equal ( LSU_SPR_NumberReg, cSPRnumberITB_PhysicalPageRegister ) or
							Equal ( LSU_SPR_NumberReg, cSPRnumberDTB_PhysicalPageRegister ) );

	LSU_EA_DataFromSPR <= RB_DataCommitA when LSU_EA_ForwardWriteSPR = '1'
		else (31 downto 1 => '0') & DP_InterruptEnableFlag
			when Equal( LSU_EA_AddrReg(6 downto 2), cSPRnumberInterruptEnableRegister ) = '1'
		else DP_ReturnFromExceptionReg
			when Equal( LSU_EA_AddrReg(6 downto 2), cSPRnumberReturnFromExceptionRegister  ) = '1'
		else (31 downto 4 => '0') & DP_ProcessIdentifierReg
			when Equal( LSU_EA_AddrReg(6 downto 2), cSPRnumberProcessIdentifierRegister ) = '1'
		else CU_VirtualPageReg
				when Equal( LSU_EA_AddrReg(6 downto 2), cSPRnumberVirtualPageRegister ) = '1'
		else (31 downto 0 => '0');


	-- Data for Reorder-Buffer:
	-- Exception: Effective-Address(31 downto 5) & Exception-Number
	-- No Exception, Load: Requested data
	-- No Exception, Store: Data to store, used for write to SPR
	LSU_EA_RequestedData <= LSU_EA_DataFromSPR when LSU_EA_SpecialPurposeRegisterAccess = '1'
									else LSU_EA_ExtendedData;

	LSU_EA_DataOrExceptionOut(31 downto 5) <=
			LSU_EA_AddrReg(31 downto 5) when LSU_EA_Exception = '1' else
				LSU_EA_RequestedData(31 downto 5) when LSU_EA_InstructionLoadFlag = '1' else
					LSU_EA_DataToStoreReg(31 downto 5);				-- Used by store to SPR
	LSU_EA_DataOrExceptionOut(4 downto 0) <=
		cAlignErrorExceptionNumber when LSU_EA_AlignmentError = '1' else
			cLoadTransferErrorExceptionNumber when BIU_TransferErrorLoad = '1' else
				cLoadTranslationMissExceptionNumber when DTB_LoadMiss = '1' else
					cStoreTranslationMissExceptionNumber when DTB_StoreMiss = '1' else
						LSU_EA_RequestedData(4 downto 0) when LSU_EA_InstructionLoadFlag = '1'
							else LSU_EA_DataToStoreReg(4 downto 0);	-- Used by store to SPR

	--------------------------------------------------------------
	-- Load-Store-Unit; Special-Purpose-Register write stage	--
	--------------------------------------------------------------
	-- Load-Store-Unit; Special-Purpose-Register write stage; wait until commit
	LSU_SPR_StageAvailable <= not LSU_SPR_ValidFlag or CU_WriteToSPR;

	LSU_SPR_SpeculativeFlagInput <= LSU_EA_SpeculativeFlag and not BRU_SpeculationCorrect;
	LSU_SPR_SpeculativeFlagWrite <= LSU_SPR_StageAvailable or BRU_SpeculationCorrect;

	LSU_SPR_ValidFlagInput  <= not DP_TakeException and
								LSU_EA_ValidFlag and LSU_EA_SpecialPurposeRegisterAccess and
								not LSU_EA_InstructionLoadFlag and
								not ( LSU_EA_SpeculativeFlag and BRU_ChangePath );

	LSU_SPR_ValidFlagWrite <= DP_TakeException or LSU_SPR_StageAvailable or CU_WriteToSPR or
							( BRU_ChangePath and LSU_SPR_SpeculativeFlag );

	postponed assert '0' =
			( CU_WriteToSPR and ( LSU_SPR_SpeculativeFlag or not LSU_SPR_ValidFlag ) )
		report "There is no valid SPR access to commit."
		severity failure;

	postponed assert '0' =
			( RB_ReorderBufferIsEmpty and LSU_SPR_ValidFlag )
		report "There is no instruction within the Reorder-Buffer but an access " &
					" to an internal register waits for execution."
		severity failure;

	--------------------------------------------------------------------------
	--							Commit-Unit									--
	--------------------------------------------------------------------------

	-- The Commit-Unit is able to commit up to two instructions out of the
	-- Reorder-Buffer per clock. Commit an instruction means to complete its processing
	-- within the pipeline and write back the result in the Register-File. So the
	-- instruction is retired from the pipeline.
	-- Since the Reorder-Buffer is implemented as a circular queue, the
	-- CU_NextCommitPointerReg is a pointer to the next instruction to commit. This
	-- pointer rotates right when an entry of the Reorder-Buffer is retired.
	-- The Commit-Unit provides two pointers that determine the next two instructions
	-- to commit. Instruction A is the first instruction in program order,
	-- instruction B the second.
	-- All entries in the Reorder-Buffer are handled in program order. So, if
	-- instruction A is not ready to commit, instruction B stalls too.
	-- To avoid conflicts, exception and stores ( Memory, Special-Purpose-Registers )
	-- are processed by stage A only. This is no bottleneck. See:
	-- "John L. Hennessy and David A. Patterson, Computer Architecture: A Quantitative Approach",
	-- second edition, page 348.
	
	CU_NextCommitPointerRegWrite <= CU_CommitInstrA or CU_CommitInstrB;

	-- Pointer rotates right
	CU_NextCommitPointerRegInput <=
				CU_CommitPointerB(4) & CU_CommitPointerB(0 to 3) when CU_CommitInstrB = '1'
						else CU_CommitPointerB;

	-- Pointers to read the next two entries in Reorder-Buffer
	CU_CommitPointerA <= CU_NextCommitPointerReg;
	CU_CommitPointerB <= CU_NextCommitPointerReg(4) & CU_NextCommitPointerReg(0 to 3);

	-- Special events
	CU_Inhibit	<= DP_TakeExternalInterrupt or BIU_TransferErrorStore;
	CU_StoreToCache <= not RB_WriteBackCommitA and WB_EntranceWriteToCacheFlag;
	CU_SPR_StoreA <= LSU_SPR_ValidFlag and
									Equal( CU_CommitPointerA, LSU_SPR_AllocatedReorderBufferReg );
	CU_SPR_StoreB <= LSU_SPR_ValidFlag and
									Equal( CU_CommitPointerB, LSU_SPR_AllocatedReorderBufferReg );

	postponed assert '0' = ( RB_ValidCommitA and RB_ReadyCommitA and RB_SpeculativeCommitA )
		report "There is a speculative executed instruction to commit but no previous " &
				"instruction to resolve speculation."
		severity failure;

	-- Instruction A
	CU_TakeException <= RB_ValidCommitA and RB_ReadyCommitA and RB_ExceptionCommitA and
							not CU_Inhibit;

	CU_CommitInstrA <= RB_ValidCommitA and RB_ReadyCommitA and not RB_ExceptionCommitA and
						not CU_Inhibit and
	   					-- only one write port to data cache -> stall
				not ( CU_StoreToCache and BIU_TransferAcknowledgeLoad and not BIU_CacheInhibit );

	CU_CommitStore <= CU_CommitInstrA and not RB_WriteBackCommitA and not CU_SPR_StoreA;
	CU_WriteToSPR <= CU_CommitInstrA and not RB_WriteBackCommitA and CU_SPR_StoreA;

	postponed assert '0' = ( RB_ValidCommitB and RB_ReadyCommitB and
										RB_SpeculativeCommitB and not BRU_SpeculationCorrect )
		report "There is a speculative executed instruction to commit but no previous " &
				"instruction to resolve speculation."
		severity failure;

	-- Instruction B
	-- The following actions are done by stage A only. This helps to avoid data-conflicts.
	--       Take exception
	--       Commit store instructions ( Memory, Special-Purpose-Registers )
	CU_CommitInstrB <= CU_CommitInstrA and
							RB_ValidCommitB and RB_ReadyCommitB and
							not RB_ExceptionCommitB and
							RB_WriteBackCommitB;

	CU_RegisterWriteEnableA <= CU_CommitInstrA and RB_WriteBackCommitA;
	CU_RegisterWriteEnableB <= CU_CommitInstrB;		-- Write back only

	-- Commit-Unit; Virtual-Page register
	-- For address translation miss exceptions the actual DP_ProcessIdentifierReg
	-- and the upper bits of the missed effective address are copied from
	-- the Reorder-Buffer to CU_VirtualPageReg.
	-- If the exception handler wants to write a new entry to one of the
	-- address translation buffers, it must store the physical page to
	-- ITB_PhysicalPageRegister or DTB_PhysicalPageRegister. The new entry consists
	-- of the written physical page and the content of CU_VirtualPageReg.
	-- In the case that the handler wants to load the translation buffer with a
	-- virtual page different to the CU_VirtualPageReg ( page-fault, process-switch ),
	-- this register must be loaded with the new virtual page before.

	CU_VirtualPageRegInput(31 downto 4) <= RB_DataCommitA(31 downto 4);
	CU_VirtualPageRegInput(3 downto 0) <=
			DP_ProcessIdentifierReg when CU_TakeAddressTranslationMissException = '1' else
					RB_DataCommitA(3 downto 0);		-- Write to SPR

	CU_TakeAddressTranslationMissException <= CU_TakeException and
				( Equal( RB_DataCommitA(4 downto 0), cStoreTranslationMissExceptionNumber ) or
				  Equal( RB_DataCommitA(4 downto 0), cFetchTranslationMissExceptionNumber ) or
				  Equal( RB_DataCommitA(4 downto 0), cLoadTranslationMissExceptionNumber  ) );

	CU_VirtualPageRegWrite <= CU_TakeAddressTranslationMissException or
			( CU_WriteToSPR and Equal( LSU_SPR_NumberReg, cSPRnumberVirtualPageRegister ) );

	--------------------------------------------------------------------------
	--							Register-File								--
	--------------------------------------------------------------------------

	-- It is not necessary to forward data that are written in this cycle.
	-- They are forwarded by the Reorder-Buffer.
	RF_DataA1 <= X"0000_0000" when IF_InstrRegA( 25 downto 21 ) = "00000" else
							RF_Reg( To_Integer( IF_InstrRegA( 25 downto 21 ) ) );
	RF_DataA2 <= X"0000_0000" when IF_InstrRegA( 20 downto 16 ) = "00000" else
							RF_Reg( To_Integer( IF_InstrRegA( 20 downto 16 ) ) );
	RF_DataB1 <= X"0000_0000" when IF_InstrRegB( 25 downto 21 ) = "00000" else
							RF_Reg( To_Integer( IF_InstrRegB( 25 downto 21 ) ) );
	RF_DataB2 <= X"0000_0000" when IF_InstrRegB( 20 downto 16 ) = "00000" else
							RF_Reg( To_Integer( IF_InstrRegB( 20 downto 16 ) ) );

	RF_WriteEnableA <= BinaryDecode( RB_RegisterDestinationCommitA ) and
					( 31 downto 0 => CU_RegisterWriteEnableA );
	RF_WriteEnableB <= BinaryDecode( RB_RegisterDestinationCommitB ) and
					( 31 downto 0 => CU_RegisterWriteEnableB );

	RF_RegWrite <= RF_WriteEnableA or RF_WriteEnableB;

	RF_EvaluateNewEntries : for i in RF_Reg'range generate                                 
		-- If both ports write to the same register, the value of port B is selected.
		RF_RegInput(i) <= RB_DataCommitB when RF_WriteEnableB(i) = '1' else RB_DataCommitA; 
	end generate;

	--------------------------------------------------------------------------
	--					Instruction-Address-Translation-Buffer				--
	--------------------------------------------------------------------------

	ITB_SelectedPhysicalPage <=
					ITB_PhysicalPageReg( To_Integer( IF_InstrCounterReg( 8 downto 7 ) ) );
	ITB_SelectedVirtualTag <=
					ITB_VirtualTagReg( To_Integer( IF_InstrCounterReg( 8 downto 7 ) ) );
	ITB_SelectedProcessIdentifier <=
					ITB_ProcessIdentifierReg( To_Integer( IF_InstrCounterReg( 8 downto 7 ) ) );
	ITB_SelectedValid <=
					ITB_ValidFlag( To_Integer( IF_InstrCounterReg( 8 downto 7 ) ) );

	ITB_Hit <= ITB_SelectedValid and
					Equal( ITB_SelectedProcessIdentifier & ITB_SelectedVirtualTag,
								DP_ProcessIdentifierReg & IF_InstrCounterReg( 31 downto 9 ) );

	ITB_Miss <= not DP_KernelMode and not ITB_Hit;
	ITB_PpageOut <= IF_InstrCounterReg( 31 downto 7 ) when DP_KernelMode = '1'
						else ITB_SelectedPhysicalPage;

	-- Write to cache
	ITB_WriteNewEntry <=
			( CU_WriteToSPR and Equal( LSU_SPR_NumberReg, cSPRnumberITB_PhysicalPageRegister ) );
	ITB_CacheWrite <=
			( 3 downto 0 => DP_InvalidateInstructionAddressTranslationBuffer ) or
			( ( 3 downto 0 => ITB_WriteNewEntry ) and
				BinaryDecode( CU_VirtualPageReg( 8 downto 7 ) ) );

	ITB_ValidFlagInput <= not DP_InvalidateInstructionAddressTranslationBuffer;
	ITB_PhysicalPageRegInput <= RB_DataCommitA( 31 downto 7 );
	ITB_VirtualTagRegInput <= CU_VirtualPageReg( 31 downto 9 );
	ITB_ProcessIdentifierRegInput <= CU_VirtualPageReg( 3 downto 0 );

	--------------------------------------------------------------------------
	--							Instruction-Cache							--
	--------------------------------------------------------------------------

	IC_SelectedBlock <= IC_BlockReg( To_Integer( IF_InstrCounterReg( 5 downto 3 ) ) );
	IC_SelectedTag <= IC_TagReg( To_Integer( IF_InstrCounterReg( 5 downto 3 ) ) );
	IC_SelectedValid <= IC_ValidFlag( To_Integer( IF_InstrCounterReg( 5 downto 3 ) ) );

	IC_Hit <= Equal( IC_SelectedValid & IC_SelectedTag,
											'1' & ITB_PpageOut & IF_InstrCounterReg( 6 ) );
	IC_DataOut <= IC_SelectedBlock when IC_Hit = '1' else BIU_IncomingData;
	IC_DataValid <= ( IC_Hit or BIU_TransferAcknowledgeFetch ) and
						IF_CacheRequestIC and not ITB_Miss;

	IC_FetchRequest <= not IC_Hit and not ITB_Miss and IF_CacheRequestIC;

	-- The memory access is canceled in the BIU if the IF_InstrCounterReg changes.
	-- BIU_TransferAcknowledgeFetch is never set for a canceled fetch.
	IC_CacheWrite <= ( 7 downto 0 => DP_InvalidateInstructionCache ) or 
								( ( 7 downto 0 => BIU_TransferAcknowledgeFetch ) and
									BinaryDecode( IF_InstrCounterReg( 5 downto 3 ) ) );
	IC_ValidFlagInput <= not DP_InvalidateInstructionCache;
	IC_TagRegInput <= ITB_PpageOut & IF_InstrCounterReg( 6 );
	IC_BlockRegInput <= BIU_IncomingData;

	--------------------------------------------------------------------------
	--						Data-Address-Translation-Buffer					--
	--------------------------------------------------------------------------

	DTB_SelectedPhysicalPage <=
					DTB_PhysicalPageReg( To_Integer( LSU_EA_AddrReg( 8 downto 7 ) ) );
	DTB_SelectedVirtualTag <=
					DTB_VirtualTagReg( To_Integer( LSU_EA_AddrReg( 8 downto 7 ) ) );
	DTB_SelectedProcessIdentifier <=
					DTB_ProcessIdentifierReg( To_Integer( LSU_EA_AddrReg( 8 downto 7 ) ) );
	DTB_SelectedModified <=
					DTB_ModifiedFlag( To_Integer( LSU_EA_AddrReg( 8 downto 7 ) ) );
	DTB_SelectedValid <=
					DTB_ValidFlag( To_Integer( LSU_EA_AddrReg( 8 downto 7 ) ) );

	DTB_Hit <= DTB_SelectedValid and
					Equal( DTB_SelectedProcessIdentifier & DTB_SelectedVirtualTag,
								DP_ProcessIdentifierReg & LSU_EA_AddrReg( 31 downto 9 ) );

	DTB_LoadMiss <= LSU_EA_InstructionLoadFlag and not DP_KernelMode and not DTB_Hit;
	DTB_StoreMiss <= not LSU_EA_InstructionLoadFlag and not DP_KernelMode and
						( not DTB_SelectedModified or not DTB_Hit ); 
	DTB_PpageOut <= LSU_EA_AddrReg( 31 downto 7 ) when DP_KernelMode = '1'
						else DTB_SelectedPhysicalPage;

	-- Write to cache
	DTB_WriteNewEntry <=
			( CU_WriteToSPR and Equal( LSU_SPR_NumberReg, cSPRnumberDTB_PhysicalPageRegister ) );
	DTB_CacheWrite <=
			( 3 downto 0 => DP_InvalidateDataAddressTranslationBuffer ) or
			( ( 3 downto 0 => DTB_WriteNewEntry ) and
				BinaryDecode( CU_VirtualPageReg( 8 downto 7 ) ) );

	DTB_ValidFlagInput <= not DP_InvalidateDataAddressTranslationBuffer;
	DTB_PhysicalPageRegInput <= RB_DataCommitA( 31 downto 7 );
	DTB_VirtualTagRegInput <= CU_VirtualPageReg( 31 downto 9 );
	DTB_ProcessIdentifierRegInput <= CU_VirtualPageReg( 3 downto 0 );
	DTB_ModifiedFlagInput <= RB_DataCommitA( 0 );

	--------------------------------------------------------------------------
	--							Data-Cache									--
	--------------------------------------------------------------------------

	DC_SelectedBlock <= DC_BlockReg( To_Integer( LSU_EA_AddrReg( 5 downto 3 ) ) );
	DC_SelectedTag <= DC_TagReg( To_Integer( LSU_EA_AddrReg( 5 downto 3 ) ) );
	DC_SelectedValid <= DC_ValidFlag( To_Integer( LSU_EA_AddrReg( 5 downto 3 ) ) );

	DC_Hit <= Equal( DC_SelectedValid & DC_SelectedTag,
						 '1' & DTB_PpageOut & LSU_EA_AddrReg( 6 ) );	-- Page size: 128 Byte

	-- Data-Cache; Hit
	-- There may be an outstanding store that caused a write hit. So it is possible that the
	-- data in the cache are not up to date on a read hit.
	DC_ForwardOnCacheHit <= WB_ForwardEntrance and not WB_EntranceCommitFlag;

	-- On Cache-Hit and DC_ForwardOnCacheHit all bytes in the Write-Buffer entrance are valid:
	--   - The write was a Write-Hit.
	--   - The write was a Write-Miss but a load wrote the data to cache. So now it is a Read-Hit.
	postponed assert '0' = ( DC_ForwardOnCacheHit and DC_Hit and LSU_EA_CacheRequestDC and
								( not WB_EntranceWriteToCacheFlag or 
										not Equal( WB_EntranceByteEnableFlag, "1111" ) ) )
		report "The Write-Buffer entrance is forwarded on a Cache-Hit but not all " &
						"bytes are valid."
		severity failure;

	DataOnCacheHit : for i in WB_EntranceDataReg'range generate
				DC_CacheHitData( 8*i+7 downto 8*i ) <=
						WB_EntranceDataReg(i) when DC_ForwardOnCacheHit = '1'
								else DC_SelectedBlock( 8*i+7 downto 8*i );
	end generate;

	-- Data-Cache; Miss
	-- There may be a cache miss but all or some required bytes are within the Write-Buffer.
	-- No load request is sent to the Bus-Interface-Unit if all required bytes are
	-- forwarded by the Write-Buffer.
	-- The incoming data from memory are merged with matching data bytes in the Write-Buffer.
	DC_ForwardOnCacheMissByteEnable <=
				WB_ForwardByteEnableOnReadMiss(3 downto 0) when LSU_EA_AddrReg(2) = '1' else
						WB_ForwardByteEnableOnReadMiss(7 downto 4);

	DC_ByteNotRequiredOrForwarded <= not LSU_EA_ByteEnableFlag or DC_ForwardOnCacheMissByteEnable;

	DC_AllRequiredBytesValidOnCacheMiss <= Equal( DC_ByteNotRequiredOrForwarded, "1111" );

	DC_LoadRequest <= not DC_Hit and not DC_AllRequiredBytesValidOnCacheMiss and
						LSU_EA_CacheRequestDC and LSU_EA_InstructionLoadFlag and not DTB_LoadMiss;

	-- Data-Cache; output signals
	-- The incoming data from memory are merged with matching data bytes in the Write-Buffer.
	DC_DoubleWordData <= DC_CacheHitData when DC_Hit = '1' else WB_DataOut;
							
	DC_DataOut <= DC_DoubleWordData(31 downto 0) when LSU_EA_AddrReg(2) = '1' else
							DC_DoubleWordData(63 downto 32);

	DC_DataValid <= LSU_EA_CacheRequestDC and not DTB_LoadMiss and 
				( DC_Hit or BIU_TransferAcknowledgeLoad or DC_AllRequiredBytesValidOnCacheMiss );

	-- Data-Cache; Write to cache
	-- The memory access is canceled in the BIU if the LSU_EA_AddrReg changes.
	-- BIU_TransferAcknowledgeLoad is never set for a canceled load.
	-- Note that the Commit-Unit never commit a store with WB_EntranceWriteToCacheFlag = '1'
	-- while the BIU sends BIU_TransferAcknowledgeLoad ( the cache has only one write port ).
	DC_CacheWrite <= ( 7 downto 0 => DP_InvalidateDataCache ) or 
		( ( 7 downto 0 => BIU_TransferAcknowledgeLoad and not BIU_CacheInhibit ) and	-- Load
							BinaryDecode( LSU_EA_AddrReg( 5 downto 3 ) ) )
			or
		( ( 7 downto 0 => CU_CommitStore and WB_EntranceWriteToCacheFlag ) and	-- Write to cache
							BinaryDecode( WB_EntranceAddrReg( 5 downto 3 ) ) );

	DC_ValidFlagInput <= not DP_InvalidateDataCache;

	DC_TagRegInput <=
		DTB_PpageOut & LSU_EA_AddrReg(6) when BIU_TransferAcknowledgeLoad = '1'
							else WB_EntranceAddrReg(31 downto 6);	-- Write to cache

	-- The incoming data from memory are merged with matching data bytes in the Write-Buffer.
	DC_BlockRegInput <= WB_QueueDataOut when WB_BypassEntranceOnLoadToCache = '1' else
							WB_DataOut when BIU_TransferAcknowledgeLoad = '1' else
								WB_EntranceData;	-- Write to cache, all bytes are valid

	--------------------------------------------------------------------------
	--							Write-Buffer								--
	--------------------------------------------------------------------------

	WB_WriteBufferIsEmpty <= not WB_EntranceValidFlag and Equal( WB_ValidFlag, "000" );

	-- Write-Buffer; Entrance
	WB_AbleToTakeStore <= not WB_EntranceValidFlag or
			( ( WB_StageWrite(2) or WB_StageAbleToMerge(2) ) and	-- Able to leave entrance
				( WB_EntranceCommitFlag or CU_CommitStore ) );		-- Ready

	-- All data in the Write-Buffer queue are ready to store. The data in the
	-- Write-Buffer entrance may not by ready to store because the store instruction is not
	-- retired from the pipeline (the Reorder-Buffer). So they must not be merged with the
	-- incoming date of memory and of the queue. If the processor would do this and write
	-- the merged result to cache, the data within the cache are not correct if the store
	-- is canceled by any exception or wrong speculation.
	-- Solution:
	-- The data from the Write-Buffer queue are bypassed the entrance and written to cache.
	-- At the same time the data in the entrance are merged with the data of the queue and
	-- the WB_EntranceWriteToCacheFlag is set. So all bytes in the entrance are valid and
	-- can be written to the cache on CU_CommitStore.
	WB_BypassEntranceOnLoadToCache <=
			WB_ForwardEntrance and	-- Address of entrance equals address of load
			BIU_TransferAcknowledgeLoad and not BIU_CacheInhibit and	-- Load to cache
			not WB_EntranceCommitFlag and not CU_CommitStore;	-- Store not retired

	-- The data to store are merged with the data of the cache.
	-- On a Write-Hit this allows to write back the WB_EntranceDataReg as one block.
	MergeDataOfEntrance : for i in LSU_EA_ByteEnableFanout'range generate
		WB_EntranceDataRegInput(i) <=
			-- After execution of store, address of load matches address of entrance:
			--      overwrite invalid bytes in entrance with bytes of queue or memory
			WB_QueueDataOut( 8*i+7 downto 8*i ) when WB_BypassEntranceOnLoadToCache = '1' else
				-- while store is executed, data are passed to the Write-Buffer
				LSU_EA_DataToStoreFanout( 8*i+7 downto 8*i ) when LSU_EA_ByteEnableFanout(i) = '1'
					else DC_CacheHitData( 8*i+7 downto 8*i );
	end generate;

	WB_EntranceDataRegWrite <=
		( 7 downto 0 => WB_AbleToTakeStore ) or		-- while store is executed
		-- Address of load matches address of entrance:
		--      overwrite invalid bytes in entrance with bytes of queue or memory
		( ( 7 downto 0 => WB_BypassEntranceOnLoadToCache ) and not WB_EntranceByteEnableFanout );

	WB_EntranceWriteToCacheFlagInput <=
				( WB_AbleToTakeStore and DC_Hit ) or WB_BypassEntranceOnLoadToCache;
	WB_EntranceByteEnableFlagInput <=
				LSU_EA_ByteEnable or ( 3 downto 0 => DC_Hit or WB_BypassEntranceOnLoadToCache );

	WB_EntranceWriteToCacheFlagWrite <= WB_AbleToTakeStore or WB_BypassEntranceOnLoadToCache;
	WB_EntranceByteEnableFlagWrite <= WB_AbleToTakeStore or WB_BypassEntranceOnLoadToCache;

	-- On a Write-Hit all bytes of the block are enabled to write in memory.
	-- This may(?) help to keep the memory up to date.
	WB_EntranceByteEnableFanout <=
			( ( ( 7 downto 4 => not WB_EntranceAddrReg(2) ) and WB_EntranceByteEnableFlag ) &
			  ( ( 3 downto 0 =>     WB_EntranceAddrReg(2) ) and WB_EntranceByteEnableFlag ) ) or
							( 7 downto 0 => WB_EntranceWriteToCacheFlag );

	WB_EntranceValidFlagInput <= LSU_EA_CacheRequestDC and
									not LSU_EA_InstructionLoadFlag and not DP_TakeException and
									not DTB_StoreMiss and not DP_InvalidateWriteBuffer and
									not ( LSU_EA_SpeculativeFlag and BRU_ChangePath );
	WB_EntranceValidFlagWrite <= WB_AbleToTakeStore or DP_InvalidateWriteBuffer or
								( WB_EntranceSpeculativeFlag and BRU_ChangePath ) or
								( DP_TakeException and not WB_EntranceCommitFlag );

	WB_EntranceAddrRegInput <= DTB_PpageOut & LSU_EA_AddrReg( 6 downto 0 );

	postponed assert
			'0' = ( CU_CommitStore and ( not WB_EntranceValidFlag or WB_EntranceCommitFlag ) )
		report "There is no outstanding store in the Write-Buffer entrance to commit."
		severity failure;

	postponed assert '0' = ( CU_CommitStore and WB_EntranceSpeculativeFlag )
		report "It is not allowed to commit a speculative executed store."
		severity failure;


	WB_EntranceSpeculativeFlagInput <= WB_AbleToTakeStore and
									LSU_EA_SpeculativeFlag and not BRU_SpeculationCorrect;
	WB_EntranceSpeculativeFlagWrite <= WB_AbleToTakeStore or BRU_SpeculationCorrect;

	WB_EntranceCommitFlagInput <= not WB_AbleToTakeStore;
	WB_EntranceCommitFlagWrite <= WB_AbleToTakeStore or CU_CommitStore;


	-- Write-Buffer; Queue
	WB_StageWrite(2) <= not WB_ValidFlag(2) or WB_StageWrite(1);	-- Head of queue
	WB_StageWrite(1) <= not WB_ValidFlag(1) or WB_StageWrite(0);
	WB_StageWrite(0) <= not WB_ValidFlag(0) or						-- Tail of queue
						BIU_TransferAcknowledgeStore or BIU_TransferErrorStore or
						DP_InvalidateWriteBuffer;

	WB_AddrRegInput(2) <= WB_EntranceAddrReg;
	WB_AddrRegInput(1) <= WB_AddrReg(2);
	WB_AddrRegInput(0) <= WB_AddrReg(1);

	-- WB_StageWrite(i) enables the processor to write WB_ValidFlag(i).
	-- Head of queue, stage(2):
	-- Stage 2 does not become valid if the data from the entrance can be merged with
	-- stage 1 or stage 0.
	--     - If the data can be merged with stage 2, and stage 2 is NOT shifted to stage 1,
	--       the WB_ValidFlag(2) is not enabled for write and WB_ValidFlagInput(2) has no
	--       meaning.
	--     - If the data can be merged with stage 2, and stage 2 is shifted to stage 1, the
	--       WB_ValidFlag(2) is enabled for write and must be cleared from WB_ValidFlagInput(2).
	--       This means to shift and merge in one clock.
	WB_ValidFlagInput(2) <= 
			not DP_InvalidateWriteBuffer and WB_EntranceValidFlag and 
			( WB_EntranceCommitFlag or CU_CommitStore ) and				-- Ready
				-- Not able to merge
			not ( WB_StageAbleToMerge(0) or WB_StageAbleToMerge(1) or WB_StageAbleToMerge(2) );
	WB_ValidFlagInput(1) <= not DP_InvalidateWriteBuffer and WB_ValidFlag(2);
	WB_ValidFlagInput(0) <= not DP_InvalidateWriteBuffer and WB_ValidFlag(1);

	-- Write-Buffer; Queue; shift and/or merge
	EvaluateAllSignalsForWriteBufferQueue : for stage in WB_StageAbleToMerge'range generate

			MergeButtomStage : if stage = 0 generate
				-- Do not merge data in bottom stage if a store is active.
				WB_StageAbleToMerge( stage ) <= WB_ValidFlag( stage ) and CU_CommitStore and
						not BIU_ActiveStoreFlag and
						Equal( WB_AddrReg(stage)(31 downto 3), WB_EntranceAddrReg(31 downto 3) );
			end generate;
			MergeOtherStages : if stage /= 0 generate
				WB_StageAbleToMerge( stage ) <= WB_ValidFlag( stage ) and CU_CommitStore and
						Equal( WB_AddrReg(stage)(31 downto 3), WB_EntranceAddrReg(31 downto 3) );
			end generate;

		EvaluateSignalsForBytes : for byte in 7 downto 0 generate
			WB_MergeByte( stage, byte ) <=
							WB_StageAbleToMerge( stage ) and WB_EntranceByteEnableFanout( byte );

			WB_ByteWrite( stage, byte ) <= WB_StageWrite( stage ) or WB_MergeByte( stage, byte );

			DataTopStage : if stage = 2 generate
				WB_DataRegInput( stage, byte ) <= WB_EntranceDataReg( byte );
				WB_ByteEnableFlagInput( stage, byte ) <= WB_EntranceByteEnableFanout( byte );
			end generate;
			DataOtherStages : if stage /= 2 generate
				-- The Write-Buffer is able to shift and merge at the same time.	
				-- Source of new byte:
				--       Shift and merge byte in upper stage: Byte from WB_EntranceDataReg
				--       Shift, no merge in upper stage: Byte from upper stage
				--       No shift, merge in this stage: Byte from WB_EntranceDataReg
				WB_DataRegInput( stage, byte ) <=
					WB_EntranceDataReg( byte )
							when ( WB_MergeByte(stage, byte) or WB_MergeByte(stage+1, byte) ) = '1'
					else WB_DataReg( stage+1, byte );

				WB_ByteEnableFlagInput( stage, byte ) <=
					WB_EntranceByteEnableFanout( byte )
							when ( WB_MergeByte(stage, byte) or WB_MergeByte(stage+1, byte) ) = '1'
					else WB_ByteEnableFlag( stage+1, byte );
			end generate;
		end generate;
	end generate;

	-- Do not merge data in bottom stage if a store is active (BIU_ActiveStoreFlag).
	-- In all other cases the data should be merged if their Double-Word address match.
	postponed assert '0' =
			( ( Equal( WB_AddrReg(0)(31 downto 3), WB_AddrReg(1)(31 downto 3) ) and
							WB_ValidFlag(0) and WB_ValidFlag(1) and not BIU_ActiveStoreFlag ) or
			  ( Equal( WB_AddrReg(0)(31 downto 3), WB_AddrReg(2)(31 downto 3) ) and
							WB_ValidFlag(0) and WB_ValidFlag(2) and not BIU_ActiveStoreFlag ) or
			  ( Equal( WB_AddrReg(1)(31 downto 3), WB_AddrReg(2)(31 downto 3) ) and
							WB_ValidFlag(1) and WB_ValidFlag(2) ) )
		report "There are two entries in the Write-Buffer that should be merged."
		severity failure;

	-- Write-Buffer; Forward
	WB_ForwardEntrance <= WB_EntranceValidFlag and
					Equal( LSU_EA_AddrReg(31 downto 3), WB_EntranceAddrReg(31 downto 3) );

	CheckIfLoadAddressMatchesQueueAddress : for stage in WB_ForwardStage'range generate
		WB_ForwardStage( stage ) <= WB_ValidFlag( stage ) and
					Equal( WB_AddrReg(stage)(31 downto 3), LSU_EA_AddrReg(31 downto 3) );
	end generate;

	EvaluateDataAndByteEnable : for byte in WB_ForwardByteEnableOnReadMiss'range generate
		-- Forward data from queue
		WB_QueueDataOut( 8*byte+7 downto 8*byte ) <=
				WB_DataReg( 2, byte )
						when (WB_ForwardStage(2) and WB_ByteEnableFlag(2,byte)) = '1'
					else WB_DataReg( 1, byte )
							when (WB_ForwardStage(1) and WB_ByteEnableFlag(1,byte)) = '1'
						else WB_DataReg( 0, byte )
								when (WB_ForwardStage(0) and WB_ByteEnableFlag(0,byte)) = '1'
							else BIU_IncomingData( 8*byte+7 downto 8*byte );

		-- Forward data from queue and entrance
		WB_DataOut( 8*byte+7 downto 8*byte ) <=
			WB_EntranceDataReg(byte)
					when (WB_ForwardEntrance and WB_EntranceByteEnableFanout(byte)) = '1'
				else WB_QueueDataOut( 8*byte+7 downto 8*byte );

		-- The Data-Cache does not request a load from memory on a miss if all required
		-- bytes are within the Write-Buffer. Evaluate all valid bytes:
		WB_ForwardByteEnableOnReadMiss(byte) <=
				( WB_ForwardEntrance and WB_EntranceByteEnableFanout(byte) ) or
				( WB_ForwardStage(2) and WB_ByteEnableFlag(2,byte) ) or
				( WB_ForwardStage(1) and WB_ByteEnableFlag(1,byte) ) or
				( WB_ForwardStage(0) and WB_ByteEnableFlag(0,byte) );
	end generate;

	-- Write-Buffer; Convert format
	ConvertFormat : for byte in WB_ByteEnableForStore'range generate
		WB_ByteEnableForStore( byte ) <= WB_ByteEnableFlag( 0, byte );
		WB_DataForStore( 8*byte+7 downto 8*byte ) <= WB_DataReg( 0, byte );
		WB_EntranceData( 8*byte+7 downto 8*byte ) <= WB_EntranceDataReg( byte );
	end generate;

	--------------------------------------------------------------------------
	--							Bus-Interface-Unit							--
	--------------------------------------------------------------------------

	-- Bus-Interface-Unit;
	-- All accesses are started with the leading edge of BIU_BusClock.
	-- Both caches do not terminate an access and request for a new access in one clock.
	BIU_ActiveLoadFlagInput <= not BIU_BusClock and DC_LoadRequest and not BIU_ActiveLoadFlag;
	BIU_ActiveFetchFlagInput <= not BIU_BusClock and IC_FetchRequest and
								not BIU_ActiveFetchFlag and not BIU_ActiveLoadFlagInput;

	-- When a store is terminated in this clock, the interface can start a new store if
	-- there are stores left in the Write-Buffer. The memory takes the address of a cycle
	-- on the next leading edge of BusClock when TransferStart is active. So it is no
	-- failure that the address of the new cycle is driven to the bus in the next
	-- clock of the processor.
	BIU_ActiveStoreFlagInput <= not BIU_BusClock and
				( WB_ValidFlag(1) or ( WB_ValidFlag(0) and not BIU_ActiveStoreFlag ) ) and
					not ( BIU_ActiveLoadFlagInput or BIU_ActiveFetchFlagInput );

	BIU_CancelActiveCycle <= ( not BIU_BusClock and TransferError ) or
					( LSU_EA_StageAvaliable and BIU_ActiveLoadFlag ) or			-- Address changes
						( IF_InstrCounterRegWrite and BIU_ActiveFetchFlag );	-- Address changes

	BIU_ActiveAccessFlagWrite <= BIU_CancelActiveCycle or BIU_AbleToStartNewAccess;

	BIU_AbleToStartNewAccess <=
		( not BIU_BusClock and			-- Synchronise BusClock
			( TransferAcknowledge or BIU_CancelActiveCycle or	-- Bus available in next clock
						-- Bus idle
				not ( BIU_ActiveLoadFlag or BIU_ActiveFetchFlag or BIU_ActiveStoreFlag ) ) );

	BIU_FirstBusClockOfActiveCycleFlagInput <= BIU_AbleToStartNewAccess and
			( BIU_ActiveLoadFlagInput or BIU_ActiveFetchFlagInput or BIU_ActiveStoreFlagInput );

	BIU_FirstBusClockOfActiveCycleFlagWrite <=
			( not BIU_BusClock and		-- Synchronise BusClock
					( BIU_FirstBusClockOfActiveCycleFlag or BIU_AbleToStartNewAccess  ) ) or
			( BIU_BusClock and BIU_CancelActiveCycle );

	BIU_DriveDataOnBus <= BIU_ActiveStoreFlag and not BIU_FirstBusClockOfActiveCycleFlag;

	-- Bus-Interface-Unit; output
	Halt <= DP_HaltFlag;
	BusClock <= BIU_BusClock;

	TransferStart <= BIU_FirstBusClockOfActiveCycleFlag and not DP_HaltFlag;
	WriteEnable <= BIU_ActiveStoreFlag;
	AddressBus <=
		DTB_PpageOut & LSU_EA_AddrReg( 6 downto 0 ) when BIU_ActiveLoadFlag = '1' else
			ITB_PpageOut & IF_InstrCounterReg( 6 downto 0 ) when BIU_ActiveFetchFlag = '1'
				else WB_AddrReg(0);

	ByteEnable <= WB_ByteEnableForStore;

	-- Bus-Interface-Unit; input/output from/to memory
	DataBus <= To_StdLogicVector( bit_vector(WB_DataForStore) ) when BIU_DriveDataOnBus = '1'
					else ( 63 downto 0 => 'Z' );

	-- Bus-Interface-Unit; control processor
	BIU_CacheInhibit <= CacheInhibit;
	BIU_TransferAcknowledgeLoad <= TransferAcknowledge and BIU_ActiveLoadFlag
																	and not BIU_BusClock;
	BIU_TransferAcknowledgeFetch <= TransferAcknowledge and BIU_ActiveFetchFlag
																	and not BIU_BusClock;
	BIU_TransferAcknowledgeStore <= TransferAcknowledge and BIU_ActiveStoreFlag
																	and not BIU_BusClock;
	BIU_TransferErrorLoad <= TransferError and BIU_ActiveLoadFlag and not BIU_BusClock;
	BIU_TransferErrorFetch <= TransferError and BIU_ActiveFetchFlag and not BIU_BusClock;
	BIU_TransferErrorStore <= TransferError and BIU_ActiveStoreFlag and not BIU_BusClock;

	BIU_IncomingData <= unsigned( To_bitvector( DataBus ) );
		

	--------------------------------------------------------
	--------------------------------------------------------
	----												----
	----			Write registers and flags			----
	----												----
	--------------------------------------------------------
	--------------------------------------------------------
	process
	begin
		wait on Clock until Clock = '1';

		----------------------------------------------------------------------
		--						Instruction-Fetch							--
		----------------------------------------------------------------------

		if IF_InstrCounterRegWrite = '1' then
			IF_InstrCounterReg <= IF_InstrCounterRegInput;
		end if;

		if IF_StageA_Write = '1' then
			IF_ValidFlagA <= IF_ValidFlagA_Input;
			IF_InstrRegA <= IF_InstrRegA_Input;
			IF_InstrAddrRegA <= IF_InstrAddrRegA_Input;
			IF_NextInstrAddrRegA <= IF_NextInstrAddrRegA_Input;
			IF_PredictedBranchFlagA <= IF_PredictedBranchFlagA_Input;
			IF_TransferErrorFlagA <= IF_TransferErrorFlagA_Input;
			IF_AddressTranslationMissFlagA <= IF_AddressTranslationMissFlagA_Input;
		end if;

		if IF_StageB_Write = '1' then
			IF_ValidFlagB <= IF_ValidFlagB_Input;
			IF_InstrRegB <= IF_InstrRegB_Input;
			IF_InstrAddrRegB <= IF_InstrAddrRegB_Input;
			IF_NextInstrAddrRegB <= IF_NextInstrAddrRegB_Input;
			IF_PredictedBranchFlagB <= IF_PredictedBranchFlagB_Input;
			IF_TransferErrorFlagB <= IF_TransferErrorFlagB_Input;
			IF_AddressTranslationMissFlagB <= IF_AddressTranslationMissFlagB_Input;
		end if;

		----------------------------------------------------------------------
		--						Branch-Target-Buffer						--
		----------------------------------------------------------------------

		for i in BTB_CacheWrite'range loop
			if BTB_CacheWrite(i) = '1' then
				BTB_DestinationReg(i) <= BTB_DestinationRegInput;
				BTB_TagReg(i) <= BTB_TagRegInput;
				BTB_ValidFlag(i) <= BTB_ValidFlagInput;
			end if;
		end loop;

		----------------------------------------------------------------------
		--						Dispatcher									--
		----------------------------------------------------------------------

		if DP_HaltDlx = '1' then
			DP_HaltFlag <= '1';
		end if;

		if DP_InterruptEnableFlagWrite = '1' then
			DP_InterruptEnableFlag <= DP_InterruptEnableFlagInput;
		end if;

		if DP_ReturnFromExceptionRegWrite = '1' then
			DP_ReturnFromExceptionReg <= DP_ReturnFromExceptionRegInput;
		end if;

		if DP_ProcessIdentifierRegWrite = '1' then
			DP_ProcessIdentifierReg <= DP_ProcessIdentifierRegInput;
		end if;

		----------------------------------------------------------------------
		--						Reorder-Buffer								--
		----------------------------------------------------------------------

		for i in RB_ValidFlag'range loop
			if RB_ValidFlagWrite(i) = '1' then
				RB_ValidFlag(i) <= RB_ValidFlagInput(i);
			end if;

			if RB_SpeculativeFlagWrite(i) = '1' then
				RB_SpeculativeFlag(i) <= RB_SpeculativeFlagInput(i);
			end if;

			if RB_AllocateEntry(i) = '1' then
				RB_InstrAddrReg(i) <= RB_InstrAddrRegInput(i);
				RB_RegisterDestinationReg(i) <= RB_RegisterDestinationRegInput(i);
				RB_WriteBackFlag(i) <= RB_WriteBackFlagInput(i);
			end if;

			if RB_DataPartWrite(i) = '1' then
				RB_ReadyFlag(i) <= RB_ReadyFlagInput(i);
				RB_DataReg(i) <= RB_DataRegInput(i);
				RB_ExceptionFlag(i) <= RB_ExceptionFlagInput(i);
			end if;
		end loop;

		----------------------------------------------------------------------
		--						Branch-Resolve-Unit							--
		----------------------------------------------------------------------

		-- Valid data are used in this cycle and are not needed later.
		BRU_DataToCompareValidFlag <= BRU_DataToCompareValidFlagInput;
		BRU_DataToCompareReg <= BRU_DataToCompareRegInput;

		if BRU_ValidFlagWrite = '1' then
			BRU_ValidFlag <= BRU_ValidFlagInput;
		end if;

		if BRU_Issue = '1' then
			BRU_ForwardReorderBufferReg <= BRU_ForwardReorderBufferRegInput;
			BRU_SpeculativePathConditionFlag <= BRU_SpeculativePathConditionFlagInput;
			BRU_PredictionHitFlag <= BRU_PredictionHitFlagInput;
			BRU_InstrAddrReg <= BRU_InstrAddrRegInput;
			BRU_AlternativePathReg <= BRU_AlternativePathRegInput;
		end if;

		----------------------------------------------------------------------
		--						Arithmetic-Logic-Unit						--
		----------------------------------------------------------------------

		-- Arithmetic-Logic-Unit; Reservation-Station
		if ALU_LoadData1 = '1' then
			ALU_ForwardReorderBufferReg1 <= ALU_ForwardReorderBufferReg1Input;
			ALU_SourceDataValidFlag1 <= ALU_SourceDataValidFlag1Input;
			ALU_SourceDataReg1 <= ALU_SourceDataReg1Input;
		end if;

		if ALU_LoadData2 = '1' then
			ALU_ForwardReorderBufferReg2 <= ALU_ForwardReorderBufferReg2Input;
			ALU_SourceDataValidFlag2 <= ALU_SourceDataValidFlag2Input;
			ALU_SourceDataReg2 <= ALU_SourceDataReg2Input;
		end if;

		-- Arithmetic-Logic-Unit; Core
		if ALU_Issue = '1' then
			ALU_AllocatedReorderBufferReg <= ALU_AllocatedReorderBufferRegInput;
			ALU_DecoderInfoReg <= ALU_DecoderInfoRegInput;
		end if;

		if ALU_ValidFlagWrite = '1' then
			ALU_ValidFlag <= ALU_ValidFlagInput;
		end if;

		if ALU_SpeculativeFlagWrite = '1' then
			ALU_SpeculativeFlag <= ALU_SpeculativeFlagInput;
		end if;

		----------------------------------------------------------------------
		-- 						Multiply-Divide-Unit						--
		----------------------------------------------------------------------

		-- Multiply-Divide-Unit; Reservation-Station
		if MDU_LoadData1 = '1' then
			MDU_ForwardReorderBufferReg1 <= MDU_ForwardReorderBufferReg1Input;
			MDU_SourceDataValidFlag1 <= MDU_SourceDataValidFlag1Input;
			MDU_SourceDataReg1 <= MDU_SourceDataReg1Input;
		end if;

		if MDU_LoadData2 = '1' then
			MDU_ForwardReorderBufferReg2 <= MDU_ForwardReorderBufferReg2Input;
			MDU_SourceDataValidFlag2 <= MDU_SourceDataValidFlag2Input;
			MDU_SourceDataReg2 <= MDU_SourceDataReg2Input;
		end if;

		-- Multiply-Divide-Unit; Core
		MDU_ClockCounterReg <= MDU_ClockCounterRegInput;

		if MDU_Issue = '1' then
			MDU_AllocatedReorderBufferReg <= MDU_AllocatedReorderBufferRegInput;
			MDU_DecoderInfoReg <= MDU_DecoderInfoRegInput;
		end if;

		if MDU_ValidFlagWrite = '1' then
			MDU_ValidFlag <= MDU_ValidFlagInput;
		end if;

		if MDU_SpeculativeFlagWrite = '1' then
			MDU_SpeculativeFlag <= MDU_SpeculativeFlagInput;
		end if;

		----------------------------------------------------------------------
		-- 						Load-Store-Unit								--
		----------------------------------------------------------------------

		-- Load-Store-Unit; Reservation-Station
		if LSU_LoadData1 = '1' then
			LSU_ForwardReorderBufferReg1 <= LSU_ForwardReorderBufferReg1Input;
			LSU_SourceDataValidFlag1 <= LSU_SourceDataValidFlag1Input;
			LSU_SourceDataReg1 <= LSU_SourceDataReg1Input;
		end if;

		if LSU_LoadData2 = '1' then
			LSU_ForwardReorderBufferReg2 <= LSU_ForwardReorderBufferReg2Input;
			LSU_SourceDataValidFlag2 <= LSU_SourceDataValidFlag2Input;
			LSU_SourceDataReg2 <= LSU_SourceDataReg2Input;
		end if;

		-- Load-Store-Unit; Data stage
		if LSU_Issue = '1' then
			LSU_AllocatedReorderBufferReg <= LSU_AllocatedReorderBufferRegInput;
			LSU_OffsetReg <= LSU_OffsetRegInput;
			LSU_ByteEnableFlag <= LSU_ByteEnableFlagInput;
			LSU_InstructionLoadFlag <= LSU_InstructionLoadFlagInput;
			LSU_SignExtensionFlag <= LSU_SignExtensionFlagInput;
		end if;

		if LSU_ValidFlagWrite = '1' then
			LSU_ValidFlag <= LSU_ValidFlagInput;
		end if;

		if LSU_SpeculativeFlagWrite = '1' then
			LSU_SpeculativeFlag <= LSU_SpeculativeFlagInput;
		end if;

		-- Load-Store-Unit; Effective address stage
		if LSU_EA_StageAvaliable = '1' then
			LSU_EA_AddrReg <= LSU_EA_AddrRegInput;
			LSU_EA_DataToStoreReg <= LSU_SourceDataReg2;
			LSU_EA_ByteEnableFlag <= LSU_ByteEnableFlag;
			LSU_EA_InstructionLoadFlag <= LSU_InstructionLoadFlag;
			LSU_EA_SignExtensionFlag <= LSU_SignExtensionFlag;
			LSU_EA_AllocatedReorderBufferReg <= LSU_AllocatedReorderBufferReg;
		end if;

		if LSU_EA_ValidFlagWrite = '1' then
			LSU_EA_ValidFlag <= LSU_EA_ValidFlagInput;
		end if;

		if LSU_EA_SpeculativeFlagWrite = '1' then
			LSU_EA_SpeculativeFlag <= LSU_EA_SpeculativeFlagInput;
		end if;

		-- Load-Store-Unit; Special-Purpose-Register write stage
		if LSU_SPR_StageAvailable = '1' then
			LSU_SPR_NumberReg <= LSU_EA_AddrReg(6 downto 2);
			LSU_SPR_AllocatedReorderBufferReg <= LSU_EA_AllocatedReorderBufferReg;
		end if;

		if LSU_SPR_SpeculativeFlagWrite = '1' then
			LSU_SPR_SpeculativeFlag <= LSU_SPR_SpeculativeFlagInput;
		end if;

		if LSU_SPR_ValidFlagWrite = '1' then
			LSU_SPR_ValidFlag <= LSU_SPR_ValidFlagInput;
		end if;

		----------------------------------------------------------------------
		--						Commit-Unit									--
		----------------------------------------------------------------------

		if CU_NextCommitPointerRegWrite = '1' then
			CU_NextCommitPointerReg <= CU_NextCommitPointerRegInput;
		end if;

		if CU_VirtualPageRegWrite = '1' then
			CU_VirtualPageReg <= CU_VirtualPageRegInput;
		end if;

		----------------------------------------------------------------------
		--						Register-File								--
		----------------------------------------------------------------------

		for i in RF_Reg'range loop
			if RF_RegWrite(i) = '1' then
				RF_Reg(i) <= RF_RegInput(i); 
			end if;
		end loop;

		----------------------------------------------------------------------
		--				Instruction-Address-Translation-Buffer				--
		----------------------------------------------------------------------

		for i in ITB_CacheWrite'range loop
			if ITB_CacheWrite(i) = '1' then
				ITB_ValidFlag(i) <= ITB_ValidFlagInput;
				ITB_PhysicalPageReg(i) <= ITB_PhysicalPageRegInput;
				ITB_VirtualTagReg(i) <= ITB_VirtualTagRegInput;
				ITB_ProcessIdentifierReg(i) <= ITB_ProcessIdentifierRegInput;
			end if;
		end loop;

		----------------------------------------------------------------------
		--						Instruction-Cache							--
		----------------------------------------------------------------------

		for i in IC_CacheWrite'range loop
			if IC_CacheWrite(i) = '1' then
				IC_BlockReg(i) <= IC_BlockRegInput;
				IC_TagReg(i) <= IC_TagRegInput;
				IC_ValidFlag(i) <= IC_ValidFlagInput;
			end if;
		end loop;

		----------------------------------------------------------------------
		--					Data-Address-Translation-Buffer					--
		----------------------------------------------------------------------

		for i in DTB_CacheWrite'range loop
			if DTB_CacheWrite(i) = '1' then
				DTB_ValidFlag(i) <= DTB_ValidFlagInput;
				DTB_PhysicalPageReg(i) <= DTB_PhysicalPageRegInput;
				DTB_VirtualTagReg(i) <= DTB_VirtualTagRegInput;
				DTB_ProcessIdentifierReg(i) <= DTB_ProcessIdentifierRegInput;
				DTB_ModifiedFlag(i) <= DTB_ModifiedFlagInput;
			end if;
		end loop;

		----------------------------------------------------------------------
		--						Data-Cache									--
		----------------------------------------------------------------------

		for i in DC_CacheWrite'range loop
			if DC_CacheWrite(i) = '1' then
				DC_BlockReg(i) <= DC_BlockRegInput;
				DC_TagReg(i) <= DC_TagRegInput;
				DC_ValidFlag(i) <= DC_ValidFlagInput;
			end if;
		end loop;

		----------------------------------------------------------------------
		-- 						Write-Buffer								--
		----------------------------------------------------------------------

		-- Write-Buffer; Entrance
		if WB_AbleToTakeStore = '1' then
			 WB_EntranceAddrReg <= WB_EntranceAddrRegInput;
		end if;

		if WB_EntranceWriteToCacheFlagWrite = '1' then
			WB_EntranceWriteToCacheFlag <= WB_EntranceWriteToCacheFlagInput;
		end if;

		if WB_EntranceByteEnableFlagWrite = '1' then
			WB_EntranceByteEnableFlag <= WB_EntranceByteEnableFlagInput;
		end if;

		for byte in WB_EntranceDataRegWrite'range loop
			if WB_EntranceDataRegWrite( byte ) = '1' then
				WB_EntranceDataReg( byte ) <= WB_EntranceDataRegInput( byte );
			end if;
		end loop;

		if WB_EntranceValidFlagWrite = '1' then
			 WB_EntranceValidFlag <= WB_EntranceValidFlagInput;
		end if;

		if WB_EntranceSpeculativeFlagWrite = '1' then
			 WB_EntranceSpeculativeFlag <= WB_EntranceSpeculativeFlagInput;
		end if;

		if WB_EntranceCommitFlagWrite = '1' then
			 WB_EntranceCommitFlag <= WB_EntranceCommitFlagInput;
		end if;

		-- Write-Buffer; Queue
		for stage in WB_StageWrite'range loop
			if WB_StageWrite( stage ) = '1' then
				WB_AddrReg( stage ) <= WB_AddrRegInput( stage );
				WB_ValidFlag( stage ) <= WB_ValidFlagInput( stage );
			end if;

			for byte in 7 downto 0 loop
				if WB_ByteWrite( stage, byte ) = '1' then
					WB_DataReg( stage, byte ) <= WB_DataRegInput( stage, byte );
					WB_ByteEnableFlag( stage, byte ) <= WB_ByteEnableFlagInput( stage, byte );
				end if;
			end loop;
		end loop;

		----------------------------------------------------------------------
		--						Bus-Interface-Unit							--
		----------------------------------------------------------------------

		if BIU_ActiveAccessFlagWrite = '1' then
			BIU_ActiveLoadFlag <= BIU_ActiveLoadFlagInput;
			BIU_ActiveFetchFlag <= BIU_ActiveFetchFlagInput;
			BIU_ActiveStoreFlag <= BIU_ActiveStoreFlagInput;
		end if;

		if BIU_FirstBusClockOfActiveCycleFlagWrite = '1' then
			BIU_FirstBusClockOfActiveCycleFlag <= BIU_FirstBusClockOfActiveCycleFlagInput;
		end if;

		----------------------------------------------------------------------
		--						External RESET								--
		----------------------------------------------------------------------

		if Reset = '1' then
			IF_ValidFlagA <= '0';
			IF_ValidFlagB <= '0';
			BTB_ValidFlag <= ( others => '0' );
			DP_HaltFlag <= '0';
			DP_InterruptEnableFlag <= '0';
			DP_ProcessIdentifierReg <= ( others => '0' );
			RB_ValidFlag <= ( others => '0' );
			BRU_ValidFlag <= '0';
			ALU_ValidFlag <= '0';
			MDU_ValidFlag <= '0';
			LSU_ValidFlag <= '0';
			LSU_EA_ValidFlag <= '0';
			LSU_SPR_ValidFlag <= '0';
			CU_NextCommitPointerReg <= "10000";		-- Initialize pointer
			ITB_ValidFlag <= ( others => '0' );
			IC_ValidFlag <= ( others => '0' );
			DTB_ValidFlag <= ( others => '0' );
			DC_ValidFlag <= ( others => '0' );
			WB_EntranceValidFlag <= '0';
			WB_ValidFlag <= ( others => '0' );
			BIU_ActiveLoadFlag <= '0';
			BIU_ActiveFetchFlag <= '0';
			BIU_ActiveStoreFlag <= '0';
			BIU_FirstBusClockOfActiveCycleFlag <= '0';
		end if;

	end process;

	process
	begin
		-- BusClock continues while DLX is halted.
		wait on IncomingClock until IncomingClock = '1';

		BIU_BusClock <= not BIU_BusClock;
	end process;

end architecture BehaviorPipelined;


--            Try to have fun !!!

