library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_1164.all;

-- synopsys translate_off
library std;
use STD.textio.All;
-- synopsys translate_on
use work.manikconfig.all;

library UNISIM;
use UNISIM.vcomponents.all;

package manikpackage is

    constant ALLZEROS 	     : std_logic_vector (31 downto 0) := (others => '0');

        
    -- ALU operand A selection code
    constant ALU_ASEL_IMM : std_logic_vector (1 downto 0) := "00";
    constant ALU_ASEL_PC  : std_logic_vector (1 downto 0) := "01";
    constant ALU_ASEL_RD  : std_logic_vector (1 downto 0) := "10";
    
    -- ALU operand B selection code
    constant ALU_BSEL_IMM : std_logic_vector (1 downto 0) := "00";
    constant ALU_BSEL_SFR : std_logic_vector (1 downto 0) := "01";
    constant ALU_BSEL_RB  : std_logic_vector (1 downto 0) := "10";

    -- alu operations
    constant ALUOP_ADD  : std_logic_vector (3 downto 0) := "0001";
    constant ALUOP_SUB  : std_logic_vector (3 downto 0) := "0000";
    constant ALUOP_ADDC : std_logic_vector (3 downto 0) := "0011";
    constant ALUOP_SUBC : std_logic_vector (3 downto 0) := "0010";
    constant ALUOP_MOV  : std_logic_vector (3 downto 0) := "0100";
    constant ALUOP_AND  : std_logic_vector (3 downto 0) := "0101";
    constant ALUOP_OR   : std_logic_vector (3 downto 0) := "0110";
    constant ALUOP_XOR  : std_logic_vector (3 downto 0) := "0111";
    constant ALUOP_SXB  : std_logic_vector (3 downto 0) := "1000";
    constant ALUOP_XHW  : std_logic_vector (3 downto 0) := "1001";
    constant ALUOP_SXH  : std_logic_vector (3 downto 0) := "1010";
    constant ALUOP_ZXH  : std_logic_vector (3 downto 0) := "1011";    
    constant ALUOP_LSL  : std_logic_vector (3 downto 0) := "1100";
    constant ALUOP_LSR  : std_logic_vector (3 downto 0) := "1101";
    constant ALUOP_ASR  : std_logic_vector (3 downto 0) := "1111";
    constant ALUOP_MULT : std_logic_vector (3 downto 0) := "1110";
    
    constant SPEC_IMM_WIDTH  : integer := 8;
    constant COND_IMM_WIDTH  : integer := 4;
    constant IF_IMM_WIDTH    : integer := 10;
    constant DE_IMM_WIDTH    : integer := 9;
    constant RF_IMM_WIDTH    : integer := 13;
    constant INST_WIDTH      : integer := 16;
    constant SWINUM_WIDTH    : integer := 4;
    constant NUM_INTRS       : integer := 6;
    constant SHCOUNT_WIDTH   : integer := 5;
    constant BR_OFFSET_WIDTH : integer := IF_IMM_WIDTH + INST_WIDTH;
    
    constant IMM_SHL_0 : std_logic_vector (1 downto 0) := "00";
    constant IMM_SHL_1 : std_logic_vector (1 downto 0) := "01";
    constant IMM_SHL_2 : std_logic_vector (1 downto 0) := "10";
    constant IMM_ZERO  : std_logic_vector (1 downto 0) := "11";

    -- FLAG offsets in PSW & PSW_WIDTH
    constant SWINUM_LO : integer := 16;  -- swinum starts @ this offset
    constant SWINUM_HI : integer := SWINUM_LO+SWINUM_WIDTH;  -- swinum ends @ this offset
    constant EI5_STAT  : integer := SWINUM_HI+11; -- offset of EI3 Status 31
    constant EI4_STAT  : integer := SWINUM_HI+10; -- offset of EI3 Status 30
    constant EI3_STAT  : integer := SWINUM_HI+9;  -- offset of EI3 Status 29
    constant EI2_STAT  : integer := SWINUM_HI+8;  -- offset of EI2 Status 28
    constant EI1_STAT  : integer := SWINUM_HI+7;  -- offset of EI1 Status 27
    constant EI0_STAT  : integer := SWINUM_HI+6;  -- offset of EI0 Status 26    
    constant EI5_ENB   : integer := SWINUM_HI+5;  -- offset of EI5 Enable 25
    constant EI4_ENB   : integer := SWINUM_HI+4;  -- offset of EI4 Enable 24
    constant EI3_ENB   : integer := SWINUM_HI+3;  -- offset of EI3 Enable 23
    constant EI2_ENB   : integer := SWINUM_HI+2;  -- offset of EI2 Enable 22
    constant EI1_ENB   : integer := SWINUM_HI+1;  -- offset of EI1 Enable 21
    constant EI0_ENB   : integer := SWINUM_HI;    -- offset of EI0 Enable 20
    constant IBE_FLAG  : integer := 15;  -- offset of instruction bus error
    constant DBE_FLAG  : integer := 14;  -- offset of data bus error
    constant SS_FLAG   : integer := 13; -- offset of Single Step flag
    constant TU_FLAG   : integer := 12; -- offset of TIMER Underflow flag (READ ONLY)
    constant TR_FLAG   : integer := 12; -- offset of TIMER reload flag (WRITE ONLY)
    constant II_FLAG   : integer := 11; -- offset of ICache Invalidate flag in PSW
    constant BD_FLAG   : integer := 10; -- offset of DCache bypass flag in PSW
    constant CY_FLAG   : integer := 9;  -- offset of carry flag in PSW
    constant TF_FLAG   : integer := 8;  -- offset of true false flag in PSW
    constant PD_FLAG   : integer := 7;  -- offset of Power Down flag in PSW
    constant BIP_FLAG  : integer := 6;  -- ofset  of Backup IP flag in PSW
    constant IE_FLAG   : integer := 5;  -- offset of Interrupt enable flag
    constant TE_FLAG   : integer := 4;  -- offset of TIMER enable flag
    constant IP_FLAG   : integer := 3;  -- offset of interrupt in process flag
    constant TI_FLAG   : integer := 2;  -- offset of TI (Timer interrupt flag)
    constant EI_FLAG   : integer := 1;  -- offset of EI (External interrupt flag)
    constant SW_FLAG   : integer := 0;  -- offset of SWI (Software interrupt flag)
    constant PSW_WIDTH : integer := 32;

    -- SFR definitions
    constant SFR_PSW   : std_logic_vector(3 downto 0) := X"0";  -- PSW sfr number
    constant SFR_RA    : std_logic_vector(3 downto 0) := X"1";  -- RA (Return address) sfr number
    constant SFR_IPC   : std_logic_vector(3 downto 0) := X"2";  -- IPC (Interrupt PC) sfr number
    constant SFR_TIMER : std_logic_vector(3 downto 0) := X"3";  -- TIMER reload value
    constant SFR_VBASE : std_logic_vector(3 downto 0) := X"4";  -- interrupt vector address base
    constant SFR_USREG : std_logic_vector(3 downto 0) := X"5";  -- user special function register
    constant SFR_HWDBG : std_logic_vector(3 downto 0) := X"6";  -- hardware debug control register
    constant SFR_HWBP0 : std_logic_vector(3 downto 0) := X"7";  -- hardware bp register 0
    constant SFR_HWBP1 : std_logic_vector(3 downto 0) := X"8";  -- hardware bp register 1
    constant SFR_HWWP0 : std_logic_vector(3 downto 0) := X"9";  -- hardware wp register 0
    constant SFR_HWWP1 : std_logic_vector(3 downto 0) := X"a"; -- hardware wp register 1
    
    -- signal monitoring , debugging & tracing related signals
    constant DEBUG_WIDTH       : integer   := 80;
    constant DEBUGSRAMILA      : Boolean   := False;
    constant DEBUGCORE         : Boolean := True;
    signal   debug_out         : std_logic_vector(DEBUG_WIDTH-1 downto 0);
    signal   sfr_expc          : std_logic_vector (ADDR_WIDTH-1 downto 0);
    signal   isexit            : std_logic := '0';
    signal   debug_data_addr   : std_logic_vector(ADDR_WIDTH-1 downto 0);
    signal   debug_data_wr     : std_logic;
    signal   debug_data_rd     : std_logic;
    signal   debug_data        : std_logic_vector(31 downto 0);
    signal   debug_dcache_addr : std_logic_vector(ADDR_WIDTH-1 downto 0);
    signal   debug_dcache_wr   : std_logic;
    signal   debug_dcache_hit  : std_logic;
    signal   debug_dcache_tagv : std_logic;
    signal   debug_dcache_dout : std_logic_vector(31 downto 0);
    signal   debug_dcache_din  : std_logic_vector(31 downto 0);
    signal   debug_rf_data     : std_logic_vector(31 downto 0);
    signal   debug_rf_rd       : std_logic_vector(3 downto 0);
    signal   debug_rf_wr       : std_logic;
    -- some types
    subtype RegType is std_logic_vector (3 downto 0);
    
    -- functions
    function shl 	    (constant arg     : integer;
                  	     constant shcount : integer) return integer;
    
    function boolnot 	    (i : integer) return integer;

    function to_integer     (sig : std_logic_vector) return integer;

    function num_memblocks (cache_size : integer) return integer;

    function replicate_bit (val : std_logic; rwidth : integer) return std_logic_vector;

    function log2(v : in natural) return natural;

    function int_select(s : in boolean; a : in integer; b : in integer) return integer;

    function real_select(s : in boolean; a : in real; b : in real) return real;

    function to_slv (ival : integer; width : integer) return std_logic_vector;

    function and_vect (vect : std_logic_vector; one_bit : std_logic ) return std_logic_vector;
    function or_vect  (vect : std_logic_vector) return std_logic;

    function extend_vect (ivect   : std_logic_vector; ext_len : integer) return std_logic_vector;

    function addr_decode (addr_sig : std_logic_vector;
                          dmask    : std_logic_vector) return boolean;

    function round_div (a : integer; b : integer) return integer;
    
    -- synopsys translate_off
    procedure HWRITE(L:inout LINE; VALUE:in BIT_VECTOR;
                     JUSTIFIED:in SIDE := RIGHT; FIELD:in WIDTH := 0) ;
    function CONV (X :STD_LOGIC_VECTOR (7 downto 0)) return CHARACTER ;
    -- synopsys translate_on
end manikpackage;

package body manikpackage is

    -- purpose: return 1 if 0 else 0
    function boolnot (i : integer) return integer is
    begin  -- boolnot
        if i = 0 then
            return 1;
        else
            return 0;
        end if;
    end boolnot;
    
    -- purpose: shifts ARG left shcount bits
    function shl (constant arg     : integer;
                  constant shcount : integer) return integer is
        variable retv           : unsigned(31 downto 0);
    begin  -- shl
        retv := shl(conv_unsigned(arg,32),conv_unsigned(shcount,32));
        return conv_integer(retv);
    end shl;

    function to_integer(sig : std_logic_vector) return integer is
        variable num : integer := 0;  -- descending sig as integer
    begin
        for i in sig'range loop
            if sig(i)='1' then
                num := num*2+1;
            else  -- use anything other than '1' as '0'
                num := num*2;
            end if;
        end loop;  -- i
        return num;
    end function to_integer;

    -- purpose: create std_logic_vector with input value
    function replicate_bit (val    : std_logic;
                            rwidth : integer) return std_logic_vector is
        variable rval : std_logic_vector(rwidth-1 downto 0);        
    begin
        for i in 0 to rwidth-1 loop
            rval(i) := val;
        end loop;  -- i
        return rval;
    end function replicate_bit;
        
    -- purpose: returns number of memory blocks needed for cache
    function num_memblocks (cache_size : integer) return integer is
        variable rval : integer := 0;        
    begin  -- num_memblocks        
        if Technology = "XILINX" then
            if cache_size <= 4096*1 then
                rval := 2;
            elsif cache_size <= 4096*2 then
                rval := 4;
            elsif cache_size <= 4096*4 then
                rval := 8;                    
            elsif cache_size <= 4096*8 then
                rval := 16;
            end if;
            if FPGA_Family /= "Virtex2" then
                rval := rval * 2;
            end if;
        elsif Technology = "ACTEL" then
            if cache_size <= 1024 then
                rval := 4;
            elsif cache_size <= 2048 then
                rval := 8;
            elsif cache_size <= 4096 then
                rval := 16;
            end if;
        end if;
        return rval;
    end num_memblocks;

      -- purpose : return the base 2 logarithm of a number
    function log2(v : in natural) return natural is
        variable n    :    natural;
        variable logn :    natural;
    begin
        n      := 1;
        for i in 0 to 128 loop
            logn := i;
            exit when (n >= v);
            n    := n * 2;
        end loop;
        return logn;
    end function log2;

    -- purpose : select one of two integers based on a Boolean
    function int_select(s : in boolean; a : in integer; b : in integer) return integer is
    begin
        if s then
            return a;
        else
            return b;
        end if;
        return a;
    end function int_select;

    -- select one of two reals based on a Boolean
    function real_select(s : in boolean; a : in real; b : in real) return real is
    begin
        if s then
            return a;
        else
            return b;
        end if;
        return a;
    end function real_select;

    -- convert integer to std_logic_vector of given length
    function to_slv (ival : integer; width : integer)
        return std_logic_vector is
        variable tival : integer;
        variable rval : std_logic_vector(width-1 downto 0) := (others => '0');
    begin  -- to_slv
        tival := ival;
        for i in 0 to width-1 loop
            if tival mod 2 /= 0 then
                rval(i) := '1';
            else
                rval(i) := '0';
            end if;
            tival := tival / 2;
        end loop;  -- i
        return rval;
    end to_slv;

    -- purpose: and vector with a bit
    function and_vect (vect : std_logic_vector; one_bit : std_logic) return std_logic_vector is
        variable ret_vect : std_logic_vector(vect'length-1 downto 0) := (others => '0');
    begin  -- and_vect
        for i in vect'range loop
            ret_vect(i) := vect(i) and one_bit;
        end loop;  -- i
        return ret_vect;
    end and_vect;

    -- purpose: or all bits of a vector 
    function or_vect (vect : std_logic_vector) return std_logic is
        variable retv : std_logic := '0';
    begin  -- and_vect
        for i in vect'range loop
            retv := retv or vect(i);
        end loop;  -- i
        return retv;
    end or_vect;

    function extend_vect (ivect   : std_logic_vector; ext_len : integer) return std_logic_vector is
        variable ret_vect : std_logic_vector(ext_len-1 downto 0) := (others => '0');
    begin
        for i in 0 to ext_len-1 loop
            if i >= ivect'length then
                ret_vect(i) := '0';
            else
                ret_vect(i) := ivect(i);
            end if;
        end loop;  -- i
        return ret_vect;
    end extend_vect;

    -- purpose: decode address
    function addr_decode (addr_sig : std_logic_vector;
                          dmask    : std_logic_vector) return boolean is
        variable lbit : integer := 0;
    begin  -- addr_decode
        -- find highest one (skip highest order bit)
        -- till 8
        for i in dmask'length-2 downto 8 loop
            lbit := i;
            exit  when dmask(i) = '1';
        end loop;  -- i        
        return addr_sig(addr_sig'length-1 downto lbit) =
                  dmask(addr_sig'length-1 downto lbit);
    end addr_decode;

    -- round-up division of two integers
    function round_div (a : integer; b : integer) return integer is
        variable rv : integer := a/b;
        variable rrv : real := (real(a)/real(b))+0.5;
    begin
        if a mod b = 0 then
            return a/b;
        end if;
        return integer(rrv);
    end round_div;

    -- synopsys translate_off
    procedure HWRITE(L:inout LINE; VALUE:in BIT_VECTOR;
    JUSTIFIED:in SIDE := RIGHT; FIELD:in WIDTH := 0) is
        variable quad: bit_vector(0 to 3);
        constant ne:   integer := value'length/4;
        variable bv:   bit_vector(0 to value'length-1) := value;
        variable s:    string(1 to ne);
    begin
        if value'length mod 4 /= 0 then
            assert FALSE report 
                "HRITE Error: Trying to write vector " &
                "with an odd (non multiple of 4) length";
            return;
        end if;

        for i in 0 to ne-1 loop
            quad := bv(4*i to 4*i+3);
            case quad is
                when x"0" => s(i+1) := '0';
                when x"1" => s(i+1) := '1';
                when x"2" => s(i+1) := '2';
                when x"3" => s(i+1) := '3';
                when x"4" => s(i+1) := '4';
                when x"5" => s(i+1) := '5';
                when x"6" => s(i+1) := '6';
                when x"7" => s(i+1) := '7';
                when x"8" => s(i+1) := '8';
                when x"9" => s(i+1) := '9';
                when x"A" => s(i+1) := 'A';
                when x"B" => s(i+1) := 'B';
                when x"C" => s(i+1) := 'C';
                when x"D" => s(i+1) := 'D';
                when x"E" => s(i+1) := 'E';
                when x"F" => s(i+1) := 'F';
            end case;
        end loop;
        write(L, s, JUSTIFIED, FIELD);
    end HWRITE;

    ------------------------------------------------------------------------------
    -- From STD_LOGIC_VECTOR to CHARACTER converter
    ------------------------------------------------------------------------------
    function CONV (X :STD_LOGIC_VECTOR (7 downto 0)) return CHARACTER is
        constant XMAP :INTEGER :=0;
        variable TEMP :INTEGER :=0;
    begin
        for i in X'RANGE loop
            TEMP:=TEMP*2;
            case X(i) is
                when '0' | 'L'  => null;
                when '1' | 'H'  => TEMP :=TEMP+1;
                when others     => TEMP :=TEMP+XMAP;
            end case;
        end loop;
        return CHARACTER'VAL(TEMP);
    end CONV;
    -- synopsys translate_on
    
end manikpackage;


