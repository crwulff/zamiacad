library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_1164.all;

library UNISIM;
use UNISIM.vcomponents.all;


library std;
use STD.textio.All;
use work.manikconfig.all;
use work.manikpackage.all;

package manikxilinx is
    constant ZERO_INIT : bit_vector(0 to 255) := x"0000000000000000000000000000000000000000000000000000000000000000";
    component XOR2
        port (I0 : in  std_logic;
              I1 : in  std_logic; O  : out std_logic);
    end component;

    component XOR3
        port (I0 : in  std_logic; I1 : in  std_logic;
              I2 : in  std_logic; O  : out std_logic);
    end component;

    component XORCY
        port (LI : in  std_logic;
              CI : in  std_logic; O  : out std_logic);
    end component;

    component MUXCY_L
        port (S  : in  std_logic; DI : in  std_logic;
              CI : in  std_logic; LO : out std_logic);
    end component;

    component MUXCY
        port (S  : in  std_logic; DI : in  std_logic;
              CI : in  std_logic; O  : out std_logic);
    end component;

    component MUXCY_D
        port (S  : in  std_logic;
              DI : in  std_logic; CI : in  std_logic;
              LO : out std_logic; O  : out std_logic);
    end component;

    component LUT2
        generic(INIT : bit_vector);
        port(O  : out STD_ULOGIC;
             I0 : in  STD_ULOGIC; I1 : in  STD_ULOGIC);
    end component;

    component LUT4
        generic(INIT :  bit_vector);
        port(O   :  out   STD_ULOGIC;
             I0  :  in    STD_ULOGIC; I1  :  in    STD_ULOGIC;
             I2  :  in    STD_ULOGIC; I3  :  in    STD_ULOGIC);
    end component;

    component LUT4_L
        generic(INIT :  bit_vector);
        port(LO : out   STD_ULOGIC;
             I0 : in    STD_ULOGIC; I1 : in    STD_ULOGIC;
             I2 : in    STD_ULOGIC; I3 : in    STD_ULOGIC);
    end component;

    component LUT3_L
        generic (INIT : bit_vector);
        port (LO : out STD_ULOGIC;
              I0 : in  STD_ULOGIC;
              I1 : in  STD_ULOGIC;
              I2 : in  STD_ULOGIC);
    end component;
    
    component MUXF5
        port(O  : out   STD_ULOGIC; I0 : in    STD_ULOGIC;
             I1 : in    STD_ULOGIC; S  : in    STD_ULOGIC);
    end component;

    component NOR_VECTOR
        generic (WIDTH : integer;
                 SLICE : integer);

        port (a : in  std_logic_vector (WIDTH-1 downto 0);
              o : out std_logic);
    end component;

    component ROM16X1
        generic (INIT: bit_vector := X"16");
        port (O : out STD_ULOGIC;
              A3 : in STD_ULOGIC; A2 : in STD_ULOGIC;
              A1 : in STD_ULOGIC; A0 : in STD_ULOGIC);
    end component;

    component ROM32X1
        generic (INIT   : bit_vector);
        port (O  : out std_ulogic; A0 : in  std_ulogic;
              A1 : in  std_ulogic; A2 : in  std_ulogic;
              A3 : in  std_ulogic; A4 : in  std_ulogic);
    end component;

    component LUT3
        generic (INIT : bit_vector); 
        port (O  : out STD_ULOGIC; I0 : in  STD_ULOGIC;
              I1 : in  STD_ULOGIC; I2 : in  STD_ULOGIC); 
    end component;

    component FD
        generic (INIT : bit);
        port (Q : out STD_ULOGIC; C : in  STD_ULOGIC; D : in  STD_ULOGIC);
    end component;
    
    component FDR_1
        generic (INIT : bit);
        port (Q : out STD_ULOGIC; C : in  STD_ULOGIC;
              D : in  STD_ULOGIC; R : in  STD_ULOGIC); 
    end component;

    component FDR
        generic (INIT : bit);
        port (Q : out STD_ULOGIC; C : in  STD_ULOGIC;
              D : in  STD_ULOGIC; R : in  STD_ULOGIC); 
    end component;

    component FDRE
        generic (INIT : bit);
        port(Q  : out STD_ULOGIC;
             C  : in  STD_ULOGIC; CE : in  STD_ULOGIC;
             D  : in  STD_ULOGIC; R  : in  STD_ULOGIC);
    end component;

    component FDRE_1
        generic (INIT : bit);
        port (Q  : out std_ulogic; C  : in  std_ulogic;
              CE : in  std_ulogic; D  : in  std_ulogic; R  : in  std_ulogic);
    end component;

    component FD_1
        generic (INIT : bit );
        port (Q : out STD_ULOGIC; C : in STD_ULOGIC; D : in STD_ULOGIC);
    end component;

    component FDE
        generic(INIT : bit);
        port(Q  : out STD_ULOGIC; C  : in  STD_ULOGIC;
             CE : in  STD_ULOGIC; D  : in  STD_ULOGIC);
    end component;

    component MULT_AND
        port(LO : out STD_ULOGIC; I0 : in  STD_ULOGIC; I1 : in  STD_ULOGIC);
    end component;

    component RAMB16_S1
	generic (
		INIT : bit_vector := X"0";
		SRVAL : bit_vector := X"0";
		WRITE_MODE : string := "WRITE_FIRST";
		INIT_00 : bit_vector := X"0000000000000000000000000000000000000000000000000000000000000000";
		INIT_01 : bit_vector := ZERO_INIT; INIT_02 : bit_vector := ZERO_INIT;
		INIT_03 : bit_vector := ZERO_INIT; INIT_04 : bit_vector := ZERO_INIT;
		INIT_05 : bit_vector := ZERO_INIT; INIT_06 : bit_vector := ZERO_INIT;
		INIT_07 : bit_vector := ZERO_INIT; INIT_08 : bit_vector := ZERO_INIT;
		INIT_09 : bit_vector := ZERO_INIT; INIT_0A : bit_vector := ZERO_INIT;
		INIT_0B : bit_vector := ZERO_INIT; INIT_0C : bit_vector := ZERO_INIT;
		INIT_0D : bit_vector := ZERO_INIT; INIT_0E : bit_vector := ZERO_INIT;
		INIT_0F : bit_vector := ZERO_INIT; INIT_10 : bit_vector := ZERO_INIT;
		INIT_11 : bit_vector := ZERO_INIT; INIT_12 : bit_vector := ZERO_INIT;
		INIT_13 : bit_vector := ZERO_INIT; INIT_14 : bit_vector := ZERO_INIT;
		INIT_15 : bit_vector := ZERO_INIT; INIT_16 : bit_vector := ZERO_INIT;
		INIT_17 : bit_vector := ZERO_INIT; INIT_18 : bit_vector := ZERO_INIT;
		INIT_19 : bit_vector := ZERO_INIT; INIT_1A : bit_vector := ZERO_INIT;
		INIT_1B : bit_vector := ZERO_INIT; INIT_1C : bit_vector := ZERO_INIT;
		INIT_1D : bit_vector := ZERO_INIT; INIT_1E : bit_vector := ZERO_INIT;
		INIT_1F : bit_vector := ZERO_INIT; INIT_20 : bit_vector := ZERO_INIT;
		INIT_21 : bit_vector := ZERO_INIT; INIT_22 : bit_vector := ZERO_INIT;
		INIT_23 : bit_vector := ZERO_INIT; INIT_24 : bit_vector := ZERO_INIT;
		INIT_25 : bit_vector := ZERO_INIT; INIT_26 : bit_vector := ZERO_INIT;
		INIT_27 : bit_vector := ZERO_INIT; INIT_28 : bit_vector := ZERO_INIT;
		INIT_29 : bit_vector := ZERO_INIT; INIT_2A : bit_vector := ZERO_INIT;
		INIT_2B : bit_vector := ZERO_INIT; INIT_2C : bit_vector := ZERO_INIT;
		INIT_2D : bit_vector := ZERO_INIT; INIT_2E : bit_vector := ZERO_INIT;
		INIT_2F : bit_vector := ZERO_INIT; INIT_30 : bit_vector := ZERO_INIT;
		INIT_31 : bit_vector := ZERO_INIT; INIT_32 : bit_vector := ZERO_INIT;
		INIT_33 : bit_vector := ZERO_INIT; INIT_34 : bit_vector := ZERO_INIT;
		INIT_35 : bit_vector := ZERO_INIT; INIT_36 : bit_vector := ZERO_INIT;
		INIT_37 : bit_vector := ZERO_INIT; INIT_38 : bit_vector := ZERO_INIT;
		INIT_39 : bit_vector := ZERO_INIT; INIT_3A : bit_vector := ZERO_INIT;
		INIT_3B : bit_vector := ZERO_INIT; INIT_3C : bit_vector := ZERO_INIT;
		INIT_3D : bit_vector := ZERO_INIT; INIT_3E : bit_vector := ZERO_INIT;
		INIT_3F : bit_vector := ZERO_INIT
	);
	port
	(
		DO : out STD_LOGIC_VECTOR (0 downto 0);
		ADDR : in STD_LOGIC_VECTOR (13 downto 0);
		CLK : in STD_ULOGIC;
		DI : in STD_LOGIC_VECTOR (0 downto 0);
		EN : in STD_ULOGIC;
		SSR : in STD_ULOGIC;
		WE : in STD_ULOGIC
	);
end component;

component RAMB16_S2
	generic
	(
		INIT : bit_vector := X"0";
		SRVAL : bit_vector := X"0";
		WRITE_MODE : string := "WRITE_FIRST";
		INIT_00 : bit_vector := ZERO_INIT;
		INIT_01 : bit_vector := ZERO_INIT;
		INIT_02 : bit_vector := ZERO_INIT;
		INIT_03 : bit_vector := ZERO_INIT;
		INIT_04 : bit_vector := ZERO_INIT;
		INIT_05 : bit_vector := ZERO_INIT;
		INIT_06 : bit_vector := ZERO_INIT;
		INIT_07 : bit_vector := ZERO_INIT;
		INIT_08 : bit_vector := ZERO_INIT;
		INIT_09 : bit_vector := ZERO_INIT;
		INIT_0A : bit_vector := ZERO_INIT;
		INIT_0B : bit_vector := ZERO_INIT;
		INIT_0C : bit_vector := ZERO_INIT;
		INIT_0D : bit_vector := ZERO_INIT;
		INIT_0E : bit_vector := ZERO_INIT;
		INIT_0F : bit_vector := ZERO_INIT;
		INIT_10 : bit_vector := ZERO_INIT;
		INIT_11 : bit_vector := ZERO_INIT;
		INIT_12 : bit_vector := ZERO_INIT;
		INIT_13 : bit_vector := ZERO_INIT;
		INIT_14 : bit_vector := ZERO_INIT;
		INIT_15 : bit_vector := ZERO_INIT;
		INIT_16 : bit_vector := ZERO_INIT;
		INIT_17 : bit_vector := ZERO_INIT;
		INIT_18 : bit_vector := ZERO_INIT;
		INIT_19 : bit_vector := ZERO_INIT;
		INIT_1A : bit_vector := ZERO_INIT;
		INIT_1B : bit_vector := ZERO_INIT;
		INIT_1C : bit_vector := ZERO_INIT;
		INIT_1D : bit_vector := ZERO_INIT;
		INIT_1E : bit_vector := ZERO_INIT;
		INIT_1F : bit_vector := ZERO_INIT;
		INIT_20 : bit_vector := ZERO_INIT;
		INIT_21 : bit_vector := ZERO_INIT;
		INIT_22 : bit_vector := ZERO_INIT;
		INIT_23 : bit_vector := ZERO_INIT;
		INIT_24 : bit_vector := ZERO_INIT;
		INIT_25 : bit_vector := ZERO_INIT;
		INIT_26 : bit_vector := ZERO_INIT;
		INIT_27 : bit_vector := ZERO_INIT;
		INIT_28 : bit_vector := ZERO_INIT;
		INIT_29 : bit_vector := ZERO_INIT;
		INIT_2A : bit_vector := ZERO_INIT;
		INIT_2B : bit_vector := ZERO_INIT;
		INIT_2C : bit_vector := ZERO_INIT;
		INIT_2D : bit_vector := ZERO_INIT;
		INIT_2E : bit_vector := ZERO_INIT;
		INIT_2F : bit_vector := ZERO_INIT;
		INIT_30 : bit_vector := ZERO_INIT;
		INIT_31 : bit_vector := ZERO_INIT;
		INIT_32 : bit_vector := ZERO_INIT;
		INIT_33 : bit_vector := ZERO_INIT;
		INIT_34 : bit_vector := ZERO_INIT;
		INIT_35 : bit_vector := ZERO_INIT;
		INIT_36 : bit_vector := ZERO_INIT;
		INIT_37 : bit_vector := ZERO_INIT;
		INIT_38 : bit_vector := ZERO_INIT;
		INIT_39 : bit_vector := ZERO_INIT;
		INIT_3A : bit_vector := ZERO_INIT;
		INIT_3B : bit_vector := ZERO_INIT;
		INIT_3C : bit_vector := ZERO_INIT;
		INIT_3D : bit_vector := ZERO_INIT;
		INIT_3E : bit_vector := ZERO_INIT;
		INIT_3F : bit_vector := ZERO_INIT
	);
	port
	(
		DO : out STD_LOGIC_VECTOR (1 downto 0);
		ADDR : in STD_LOGIC_VECTOR (12 downto 0);
		CLK : in STD_ULOGIC;
		DI : in STD_LOGIC_VECTOR (1 downto 0);
		EN : in STD_ULOGIC;
		SSR : in STD_ULOGIC;
		WE : in STD_ULOGIC
	);
end component;

component RAMB16_S4
	generic
	(
		INIT : bit_vector := X"0";
		SRVAL : bit_vector := X"0";
		WRITE_MODE : string := "WRITE_FIRST";
		INIT_00 : bit_vector := ZERO_INIT;
		INIT_01 : bit_vector := ZERO_INIT;
		INIT_02 : bit_vector := ZERO_INIT;
		INIT_03 : bit_vector := ZERO_INIT;
		INIT_04 : bit_vector := ZERO_INIT;
		INIT_05 : bit_vector := ZERO_INIT;
		INIT_06 : bit_vector := ZERO_INIT;
		INIT_07 : bit_vector := ZERO_INIT;
		INIT_08 : bit_vector := ZERO_INIT;
		INIT_09 : bit_vector := ZERO_INIT;
		INIT_0A : bit_vector := ZERO_INIT;
		INIT_0B : bit_vector := ZERO_INIT;
		INIT_0C : bit_vector := ZERO_INIT;
		INIT_0D : bit_vector := ZERO_INIT;
		INIT_0E : bit_vector := ZERO_INIT;
		INIT_0F : bit_vector := ZERO_INIT;
		INIT_10 : bit_vector := ZERO_INIT;
		INIT_11 : bit_vector := ZERO_INIT;
		INIT_12 : bit_vector := ZERO_INIT;
		INIT_13 : bit_vector := ZERO_INIT;
		INIT_14 : bit_vector := ZERO_INIT;
		INIT_15 : bit_vector := ZERO_INIT;
		INIT_16 : bit_vector := ZERO_INIT;
		INIT_17 : bit_vector := ZERO_INIT;
		INIT_18 : bit_vector := ZERO_INIT;
		INIT_19 : bit_vector := ZERO_INIT;
		INIT_1A : bit_vector := ZERO_INIT;
		INIT_1B : bit_vector := ZERO_INIT;
		INIT_1C : bit_vector := ZERO_INIT;
		INIT_1D : bit_vector := ZERO_INIT;
		INIT_1E : bit_vector := ZERO_INIT;
		INIT_1F : bit_vector := ZERO_INIT;
		INIT_20 : bit_vector := ZERO_INIT;
		INIT_21 : bit_vector := ZERO_INIT;
		INIT_22 : bit_vector := ZERO_INIT;
		INIT_23 : bit_vector := ZERO_INIT;
		INIT_24 : bit_vector := ZERO_INIT;
		INIT_25 : bit_vector := ZERO_INIT;
		INIT_26 : bit_vector := ZERO_INIT;
		INIT_27 : bit_vector := ZERO_INIT;
		INIT_28 : bit_vector := ZERO_INIT;
		INIT_29 : bit_vector := ZERO_INIT;
		INIT_2A : bit_vector := ZERO_INIT;
		INIT_2B : bit_vector := ZERO_INIT;
		INIT_2C : bit_vector := ZERO_INIT;
		INIT_2D : bit_vector := ZERO_INIT;
		INIT_2E : bit_vector := ZERO_INIT;
		INIT_2F : bit_vector := ZERO_INIT;
		INIT_30 : bit_vector := ZERO_INIT;
		INIT_31 : bit_vector := ZERO_INIT;
		INIT_32 : bit_vector := ZERO_INIT;
		INIT_33 : bit_vector := ZERO_INIT;
		INIT_34 : bit_vector := ZERO_INIT;
		INIT_35 : bit_vector := ZERO_INIT;
		INIT_36 : bit_vector := ZERO_INIT;
		INIT_37 : bit_vector := ZERO_INIT;
		INIT_38 : bit_vector := ZERO_INIT;
		INIT_39 : bit_vector := ZERO_INIT;
		INIT_3A : bit_vector := ZERO_INIT;
		INIT_3B : bit_vector := ZERO_INIT;
		INIT_3C : bit_vector := ZERO_INIT;
		INIT_3D : bit_vector := ZERO_INIT;
		INIT_3E : bit_vector := ZERO_INIT;
		INIT_3F : bit_vector := ZERO_INIT
	);
	port
	(
		DO : out STD_LOGIC_VECTOR (3 downto 0);
		ADDR : in STD_LOGIC_VECTOR (11 downto 0);
		CLK : in STD_ULOGIC;
		DI : in STD_LOGIC_VECTOR (3 downto 0);
		EN : in STD_ULOGIC;
		SSR : in STD_ULOGIC;
		WE : in STD_ULOGIC
	);
end component;
        
    component RAMB16_S9
        generic (INIT       : bit_vector := X"000";
                 SRVAL      : bit_vector := X"000";
                 write_mode : string     := "WRITE_FIRST";
                 
                 INIT_00  : bit_vector := ZERO_INIT; INIT_01  : bit_vector := ZERO_INIT;
                 INIT_02  : bit_vector := ZERO_INIT; INIT_03  : bit_vector := ZERO_INIT;
                 INIT_04  : bit_vector := ZERO_INIT; INIT_05  : bit_vector := ZERO_INIT;
                 INIT_06  : bit_vector := ZERO_INIT; INIT_07  : bit_vector := ZERO_INIT;
                 INIT_08  : bit_vector := ZERO_INIT; INIT_09  : bit_vector := ZERO_INIT;
                 INIT_0A  : bit_vector := ZERO_INIT; INIT_0B  : bit_vector := ZERO_INIT;
                 INIT_0C  : bit_vector := ZERO_INIT; INIT_0D  : bit_vector := ZERO_INIT;
                 INIT_0E  : bit_vector := ZERO_INIT; INIT_0F  : bit_vector := ZERO_INIT;
                 INIT_10  : bit_vector := ZERO_INIT; INIT_11  : bit_vector := ZERO_INIT;
                 INIT_12  : bit_vector := ZERO_INIT; INIT_13  : bit_vector := ZERO_INIT;
                 INIT_14  : bit_vector := ZERO_INIT; INIT_15  : bit_vector := ZERO_INIT;
                 INIT_16  : bit_vector := ZERO_INIT; INIT_17  : bit_vector := ZERO_INIT;
                 INIT_18  : bit_vector := ZERO_INIT; INIT_19  : bit_vector := ZERO_INIT;
                 INIT_1A  : bit_vector := ZERO_INIT; INIT_1B  : bit_vector := ZERO_INIT;
                 INIT_1C  : bit_vector := ZERO_INIT; INIT_1D  : bit_vector := ZERO_INIT;
                 INIT_1E  : bit_vector := ZERO_INIT; INIT_1F  : bit_vector := ZERO_INIT;
                 INIT_20  : bit_vector := ZERO_INIT; INIT_21  : bit_vector := ZERO_INIT;
                 INIT_22  : bit_vector := ZERO_INIT; INIT_23  : bit_vector := ZERO_INIT;
                 INIT_24  : bit_vector := ZERO_INIT; INIT_25  : bit_vector := ZERO_INIT;
                 INIT_26  : bit_vector := ZERO_INIT; INIT_27  : bit_vector := ZERO_INIT;
                 INIT_28  : bit_vector := ZERO_INIT; INIT_29  : bit_vector := ZERO_INIT;
                 INIT_2A  : bit_vector := ZERO_INIT; INIT_2B  : bit_vector := ZERO_INIT;
                 INIT_2C  : bit_vector := ZERO_INIT; INIT_2D  : bit_vector := ZERO_INIT;
                 INIT_2E  : bit_vector := ZERO_INIT; INIT_2F  : bit_vector := ZERO_INIT;
                 INIT_30  : bit_vector := ZERO_INIT; INIT_31  : bit_vector := ZERO_INIT;
                 INIT_32  : bit_vector := ZERO_INIT; INIT_33  : bit_vector := ZERO_INIT;
                 INIT_34  : bit_vector := ZERO_INIT; INIT_35  : bit_vector := ZERO_INIT;
                 INIT_36  : bit_vector := ZERO_INIT; INIT_37  : bit_vector := ZERO_INIT;
                 INIT_38  : bit_vector := ZERO_INIT; INIT_39  : bit_vector := ZERO_INIT;
                 INIT_3A  : bit_vector := ZERO_INIT; INIT_3B  : bit_vector := ZERO_INIT;
                 INIT_3C  : bit_vector := ZERO_INIT; INIT_3D  : bit_vector := ZERO_INIT;
                 INIT_3E  : bit_vector := ZERO_INIT; INIT_3F  : bit_vector := ZERO_INIT;
                 
                 INITP_00 : bit_vector := ZERO_INIT; INITP_01 : bit_vector := ZERO_INIT;
                 INITP_02 : bit_vector := ZERO_INIT; INITP_03 : bit_vector := ZERO_INIT;
                 INITP_04 : bit_vector := ZERO_INIT; INITP_05 : bit_vector := ZERO_INIT;
                 INITP_06 : bit_vector := ZERO_INIT; INITP_07 : bit_vector := ZERO_INIT);
        port (DO      : out std_logic_vector (7 downto 0); DOP     : out std_logic_vector (0 downto 0);
              ADDR    : in  std_logic_vector (10 downto 0);
              CLK     : in  std_ulogic;
              DI      : in  std_logic_vector (7 downto 0);
              DIP     : in  std_logic_vector (0 downto 0) := "0";
              EN      : in  std_ulogic;
              SSR     : in  std_ulogic;
              WE      : in  std_ulogic);        
    end component;
    
    component RAMB16_S18
	generic (INIT : bit_vector := X"00000";
		SRVAL : bit_vector := X"00000";
		write_mode : string := "WRITE_FIRST";
		INITP_00 : bit_vector := ZERO_INIT; INITP_01 : bit_vector := ZERO_INIT;
		INITP_02 : bit_vector := ZERO_INIT; INITP_03 : bit_vector := ZERO_INIT;
		INITP_04 : bit_vector := ZERO_INIT; INITP_05 : bit_vector := ZERO_INIT;
		INITP_06 : bit_vector := ZERO_INIT; INITP_07 : bit_vector := ZERO_INIT;
		INIT_00 : bit_vector := ZERO_INIT; INIT_01 : bit_vector := ZERO_INIT;
		INIT_02 : bit_vector := ZERO_INIT; INIT_03 : bit_vector := ZERO_INIT;
		INIT_04 : bit_vector := ZERO_INIT; INIT_05 : bit_vector := ZERO_INIT;
		INIT_06 : bit_vector := ZERO_INIT; INIT_07 : bit_vector := ZERO_INIT;
		INIT_08 : bit_vector := ZERO_INIT; INIT_09 : bit_vector := ZERO_INIT;
		INIT_0A : bit_vector := ZERO_INIT; INIT_0B : bit_vector := ZERO_INIT;
		INIT_0C : bit_vector := ZERO_INIT; INIT_0D : bit_vector := ZERO_INIT;
		INIT_0E : bit_vector := ZERO_INIT; INIT_0F : bit_vector := ZERO_INIT;
		INIT_10 : bit_vector := ZERO_INIT; INIT_11 : bit_vector := ZERO_INIT;
		INIT_12 : bit_vector := ZERO_INIT; INIT_13 : bit_vector := ZERO_INIT;
		INIT_14 : bit_vector := ZERO_INIT; INIT_15 : bit_vector := ZERO_INIT;
		INIT_16 : bit_vector := ZERO_INIT; INIT_17 : bit_vector := ZERO_INIT;
		INIT_18 : bit_vector := ZERO_INIT; INIT_19 : bit_vector := ZERO_INIT;
		INIT_1A : bit_vector := ZERO_INIT; INIT_1B : bit_vector := ZERO_INIT;
		INIT_1C : bit_vector := ZERO_INIT; INIT_1D : bit_vector := ZERO_INIT;
		INIT_1E : bit_vector := ZERO_INIT; INIT_1F : bit_vector := ZERO_INIT;
		INIT_20 : bit_vector := ZERO_INIT; INIT_21 : bit_vector := ZERO_INIT;
		INIT_22 : bit_vector := ZERO_INIT; INIT_23 : bit_vector := ZERO_INIT;
		INIT_24 : bit_vector := ZERO_INIT; INIT_25 : bit_vector := ZERO_INIT;
		INIT_26 : bit_vector := ZERO_INIT; INIT_27 : bit_vector := ZERO_INIT;
		INIT_28 : bit_vector := ZERO_INIT; INIT_29 : bit_vector := ZERO_INIT;
		INIT_2A : bit_vector := ZERO_INIT; INIT_2B : bit_vector := ZERO_INIT;
		INIT_2C : bit_vector := ZERO_INIT; INIT_2D : bit_vector := ZERO_INIT;
		INIT_2E : bit_vector := ZERO_INIT; INIT_2F : bit_vector := ZERO_INIT;
		INIT_30 : bit_vector := ZERO_INIT; INIT_31 : bit_vector := ZERO_INIT;
		INIT_32 : bit_vector := ZERO_INIT; INIT_33 : bit_vector := ZERO_INIT;
		INIT_34 : bit_vector := ZERO_INIT; INIT_35 : bit_vector := ZERO_INIT;
		INIT_36 : bit_vector := ZERO_INIT; INIT_37 : bit_vector := ZERO_INIT;
		INIT_38 : bit_vector := ZERO_INIT; INIT_39 : bit_vector := ZERO_INIT;
		INIT_3A : bit_vector := ZERO_INIT; INIT_3B : bit_vector := ZERO_INIT;
		INIT_3C : bit_vector := ZERO_INIT; INIT_3D : bit_vector := ZERO_INIT;
		INIT_3E : bit_vector := ZERO_INIT; INIT_3F : bit_vector := ZERO_INIT);
	port (DO : out std_logic_vector (15 downto 0);
              DOP : out std_logic_vector (1 downto 0);
              ADDR : in std_logic_vector (9 downto 0);
              CLK : in std_ulogic;
              DI : in std_logic_vector (15 downto 0);
              DIP : in std_logic_vector (1 downto 0);
              EN : in std_ulogic;
              SSR : in std_ulogic;
              WE : in std_ulogic
	);      
    end component;
component RAMB16_S36
	generic
	(
		INIT : bit_vector := X"000000000";
		SRVAL : bit_vector := X"000000000";
		WRITE_MODE : string := "WRITE_FIRST";
		INITP_00 : bit_vector := ZERO_INIT;
		INITP_01 : bit_vector := ZERO_INIT;
		INITP_02 : bit_vector := ZERO_INIT;
		INITP_03 : bit_vector := ZERO_INIT;
		INITP_04 : bit_vector := ZERO_INIT;
		INITP_05 : bit_vector := ZERO_INIT;
		INITP_06 : bit_vector := ZERO_INIT;
		INITP_07 : bit_vector := ZERO_INIT;
		INIT_00 : bit_vector := ZERO_INIT;
		INIT_01 : bit_vector := ZERO_INIT;
		INIT_02 : bit_vector := ZERO_INIT;
		INIT_03 : bit_vector := ZERO_INIT;
		INIT_04 : bit_vector := ZERO_INIT;
		INIT_05 : bit_vector := ZERO_INIT;
		INIT_06 : bit_vector := ZERO_INIT;
		INIT_07 : bit_vector := ZERO_INIT;
		INIT_08 : bit_vector := ZERO_INIT;
		INIT_09 : bit_vector := ZERO_INIT;
		INIT_0A : bit_vector := ZERO_INIT;
		INIT_0B : bit_vector := ZERO_INIT;
		INIT_0C : bit_vector := ZERO_INIT;
		INIT_0D : bit_vector := ZERO_INIT;
		INIT_0E : bit_vector := ZERO_INIT;
		INIT_0F : bit_vector := ZERO_INIT;
		INIT_10 : bit_vector := ZERO_INIT;
		INIT_11 : bit_vector := ZERO_INIT;
		INIT_12 : bit_vector := ZERO_INIT;
		INIT_13 : bit_vector := ZERO_INIT;
		INIT_14 : bit_vector := ZERO_INIT;
		INIT_15 : bit_vector := ZERO_INIT;
		INIT_16 : bit_vector := ZERO_INIT;
		INIT_17 : bit_vector := ZERO_INIT;
		INIT_18 : bit_vector := ZERO_INIT;
		INIT_19 : bit_vector := ZERO_INIT;
		INIT_1A : bit_vector := ZERO_INIT;
		INIT_1B : bit_vector := ZERO_INIT;
		INIT_1C : bit_vector := ZERO_INIT;
		INIT_1D : bit_vector := ZERO_INIT;
		INIT_1E : bit_vector := ZERO_INIT;
		INIT_1F : bit_vector := ZERO_INIT;
		INIT_20 : bit_vector := ZERO_INIT;
		INIT_21 : bit_vector := ZERO_INIT;
		INIT_22 : bit_vector := ZERO_INIT;
		INIT_23 : bit_vector := ZERO_INIT;
		INIT_24 : bit_vector := ZERO_INIT;
		INIT_25 : bit_vector := ZERO_INIT;
		INIT_26 : bit_vector := ZERO_INIT;
		INIT_27 : bit_vector := ZERO_INIT;
		INIT_28 : bit_vector := ZERO_INIT;
		INIT_29 : bit_vector := ZERO_INIT;
		INIT_2A : bit_vector := ZERO_INIT;
		INIT_2B : bit_vector := ZERO_INIT;
		INIT_2C : bit_vector := ZERO_INIT;
		INIT_2D : bit_vector := ZERO_INIT;
		INIT_2E : bit_vector := ZERO_INIT;
		INIT_2F : bit_vector := ZERO_INIT;
		INIT_30 : bit_vector := ZERO_INIT;
		INIT_31 : bit_vector := ZERO_INIT;
		INIT_32 : bit_vector := ZERO_INIT;
		INIT_33 : bit_vector := ZERO_INIT;
		INIT_34 : bit_vector := ZERO_INIT;
		INIT_35 : bit_vector := ZERO_INIT;
		INIT_36 : bit_vector := ZERO_INIT;
		INIT_37 : bit_vector := ZERO_INIT;
		INIT_38 : bit_vector := ZERO_INIT;
		INIT_39 : bit_vector := ZERO_INIT;
		INIT_3A : bit_vector := ZERO_INIT;
		INIT_3B : bit_vector := ZERO_INIT;
		INIT_3C : bit_vector := ZERO_INIT;
		INIT_3D : bit_vector := ZERO_INIT;
		INIT_3E : bit_vector := ZERO_INIT;
		INIT_3F : bit_vector := ZERO_INIT
	);
	port
	(
		DO : out STD_LOGIC_VECTOR (31 downto 0);
		DOP : out STD_LOGIC_VECTOR (3 downto 0);
		ADDR : in STD_LOGIC_VECTOR (8 downto 0);
		CLK : in STD_ULOGIC;
		DI : in STD_LOGIC_VECTOR (31 downto 0);
		DIP : in STD_LOGIC_VECTOR (3 downto 0);
		EN : in STD_ULOGIC;
		SSR : in STD_ULOGIC;
		WE : in STD_ULOGIC
	);
end component;

    component RAMB16_S9_S9 
        generic (INIT_00 : bit_vector := ZERO_INIT; INIT_01 : bit_vector := ZERO_INIT;
                 INIT_02 : bit_vector := ZERO_INIT; INIT_03 : bit_vector := ZERO_INIT;
                 INIT_04 : bit_vector := ZERO_INIT; INIT_05 : bit_vector := ZERO_INIT;
                 INIT_06 : bit_vector := ZERO_INIT; INIT_07 : bit_vector := ZERO_INIT;
                 INIT_08 : bit_vector := ZERO_INIT; INIT_09 : bit_vector := ZERO_INIT;
                 INIT_0A : bit_vector := ZERO_INIT; INIT_0B : bit_vector := ZERO_INIT;
                 INIT_0C : bit_vector := ZERO_INIT; INIT_0D : bit_vector := ZERO_INIT;
                 INIT_0E : bit_vector := ZERO_INIT; INIT_0F : bit_vector := ZERO_INIT;
                 INIT_10 : bit_vector := ZERO_INIT; INIT_11 : bit_vector := ZERO_INIT;
                 INIT_12 : bit_vector := ZERO_INIT; INIT_13 : bit_vector := ZERO_INIT;
                 INIT_14 : bit_vector := ZERO_INIT; INIT_15 : bit_vector := ZERO_INIT;
                 INIT_16 : bit_vector := ZERO_INIT; INIT_17 : bit_vector := ZERO_INIT;
                 INIT_18 : bit_vector := ZERO_INIT; INIT_19 : bit_vector := ZERO_INIT;
                 INIT_1A : bit_vector := ZERO_INIT; INIT_1B : bit_vector := ZERO_INIT;
                 INIT_1C : bit_vector := ZERO_INIT; INIT_1D : bit_vector := ZERO_INIT;
                 INIT_1E : bit_vector := ZERO_INIT; INIT_1F : bit_vector := ZERO_INIT;
                 INIT_20 : bit_vector := ZERO_INIT; INIT_21 : bit_vector := ZERO_INIT;
                 INIT_22 : bit_vector := ZERO_INIT; INIT_23 : bit_vector := ZERO_INIT;
                 INIT_24 : bit_vector := ZERO_INIT; INIT_25 : bit_vector := ZERO_INIT;
                 INIT_26 : bit_vector := ZERO_INIT; INIT_27 : bit_vector := ZERO_INIT;
                 INIT_28 : bit_vector := ZERO_INIT; INIT_29 : bit_vector := ZERO_INIT;
                 INIT_2A : bit_vector := ZERO_INIT; INIT_2B : bit_vector := ZERO_INIT;
                 INIT_2C : bit_vector := ZERO_INIT; INIT_2D : bit_vector := ZERO_INIT;
                 INIT_2E : bit_vector := ZERO_INIT; INIT_2F : bit_vector := ZERO_INIT;
                 INIT_30 : bit_vector := ZERO_INIT; INIT_31 : bit_vector := ZERO_INIT;
                 INIT_32 : bit_vector := ZERO_INIT; INIT_33 : bit_vector := ZERO_INIT;
                 INIT_34 : bit_vector := ZERO_INIT; INIT_35 : bit_vector := ZERO_INIT;
                 INIT_36 : bit_vector := ZERO_INIT; INIT_37 : bit_vector := ZERO_INIT;
                 INIT_38 : bit_vector := ZERO_INIT; INIT_39 : bit_vector := ZERO_INIT;
                 INIT_3A : bit_vector := ZERO_INIT; INIT_3B : bit_vector := ZERO_INIT;
                 INIT_3C : bit_vector := ZERO_INIT; INIT_3D : bit_vector := ZERO_INIT;
                 INIT_3E : bit_vector := ZERO_INIT; INIT_3F : bit_vector := ZERO_INIT;

                 INITP_00 : bit_vector := ZERO_INIT; INITP_01 : bit_vector := ZERO_INIT;
                 INITP_02 : bit_vector := ZERO_INIT; INITP_03 : bit_vector := ZERO_INIT;
                 INITP_04 : bit_vector := ZERO_INIT; INITP_05 : bit_vector := ZERO_INIT;
                 INITP_06 : bit_vector := ZERO_INIT; INITP_07 : bit_vector := ZERO_INIT;


                 INIT_A : bit_vector  := X"000";
                 INIT_B : bit_vector  := X"000";
                 SRVAL_A : bit_vector  := X"000";
                 SRVAL_B : bit_vector  := X"000";

                 WRITE_MODE_A : string := "WRITE_FIRST";
                 WRITE_MODE_B : string := "WRITE_FIRST"
                 );

        port (DOA   : out std_logic_vector(7 downto 0);
              DOB   : out std_logic_vector(7 downto 0);
              DOPA  : out std_logic_vector(0 downto 0);
              DOPB  : out std_logic_vector(0 downto 0);
              ADDRA : in  std_logic_vector(10 downto 0);
              ADDRB : in  std_logic_vector(10 downto 0);
              CLKA  : in  std_ulogic; CLKB  : in  std_ulogic;
              DIA   : in  std_logic_vector(7 downto 0);
              DIB   : in  std_logic_vector(7 downto 0);
              DIPA  : in  std_logic_vector(0 downto 0);
              DIPB  : in  std_logic_vector(0 downto 0);
              ENA   : in  std_ulogic; ENB   : in  std_ulogic;
              SSRA  : in  std_ulogic; SSRB  : in  std_ulogic;
              WEA   : in  std_ulogic; WEB   : in  std_ulogic);
    end component;
    
    component RAMB16_S18_S18
        generic (INIT_00 : bit_vector := ZERO_INIT; INIT_01 : bit_vector := ZERO_INIT;
                 INIT_02 : bit_vector := ZERO_INIT; INIT_03 : bit_vector := ZERO_INIT;
                 INIT_04 : bit_vector := ZERO_INIT; INIT_05 : bit_vector := ZERO_INIT;
                 INIT_06 : bit_vector := ZERO_INIT; INIT_07 : bit_vector := ZERO_INIT;
                 INIT_08 : bit_vector := ZERO_INIT; INIT_09 : bit_vector := ZERO_INIT;
                 INIT_0A : bit_vector := ZERO_INIT; INIT_0B : bit_vector := ZERO_INIT;
                 INIT_0C : bit_vector := ZERO_INIT; INIT_0D : bit_vector := ZERO_INIT;
                 INIT_0E : bit_vector := ZERO_INIT; INIT_0F : bit_vector := ZERO_INIT;
                 INIT_10 : bit_vector := ZERO_INIT; INIT_11 : bit_vector := ZERO_INIT;
                 INIT_12 : bit_vector := ZERO_INIT; INIT_13 : bit_vector := ZERO_INIT;
                 INIT_14 : bit_vector := ZERO_INIT; INIT_15 : bit_vector := ZERO_INIT;
                 INIT_16 : bit_vector := ZERO_INIT; INIT_17 : bit_vector := ZERO_INIT;
                 INIT_18 : bit_vector := ZERO_INIT; INIT_19 : bit_vector := ZERO_INIT;
                 INIT_1A : bit_vector := ZERO_INIT; INIT_1B : bit_vector := ZERO_INIT;
                 INIT_1C : bit_vector := ZERO_INIT; INIT_1D : bit_vector := ZERO_INIT;
                 INIT_1E : bit_vector := ZERO_INIT; INIT_1F : bit_vector := ZERO_INIT;
                 INIT_20 : bit_vector := ZERO_INIT; INIT_21 : bit_vector := ZERO_INIT;
                 INIT_22 : bit_vector := ZERO_INIT; INIT_23 : bit_vector := ZERO_INIT;
                 INIT_24 : bit_vector := ZERO_INIT; INIT_25 : bit_vector := ZERO_INIT;
                 INIT_26 : bit_vector := ZERO_INIT; INIT_27 : bit_vector := ZERO_INIT;
                 INIT_28 : bit_vector := ZERO_INIT; INIT_29 : bit_vector := ZERO_INIT;
                 INIT_2A : bit_vector := ZERO_INIT; INIT_2B : bit_vector := ZERO_INIT;
                 INIT_2C : bit_vector := ZERO_INIT; INIT_2D : bit_vector := ZERO_INIT;
                 INIT_2E : bit_vector := ZERO_INIT; INIT_2F : bit_vector := ZERO_INIT;
                 INIT_30 : bit_vector := ZERO_INIT; INIT_31 : bit_vector := ZERO_INIT;
                 INIT_32 : bit_vector := ZERO_INIT; INIT_33 : bit_vector := ZERO_INIT;
                 INIT_34 : bit_vector := ZERO_INIT; INIT_35 : bit_vector := ZERO_INIT;
                 INIT_36 : bit_vector := ZERO_INIT; INIT_37 : bit_vector := ZERO_INIT;
                 INIT_38 : bit_vector := ZERO_INIT; INIT_39 : bit_vector := ZERO_INIT;
                 INIT_3A : bit_vector := ZERO_INIT; INIT_3B : bit_vector := ZERO_INIT;
                 INIT_3C : bit_vector := ZERO_INIT; INIT_3D : bit_vector := ZERO_INIT;
                 INIT_3E : bit_vector := ZERO_INIT; INIT_3F : bit_vector := ZERO_INIT;

                 INITP_00 : bit_vector := ZERO_INIT; INITP_01 : bit_vector := ZERO_INIT;
                 INITP_02 : bit_vector := ZERO_INIT; INITP_03 : bit_vector := ZERO_INIT;
                 INITP_04 : bit_vector := ZERO_INIT; INITP_05 : bit_vector := ZERO_INIT;
                 INITP_06 : bit_vector := ZERO_INIT; INITP_07 : bit_vector := ZERO_INIT;


                 INIT_A : bit_vector  := X"000";
                 INIT_B : bit_vector  := X"000";
                 SRVAL_A : bit_vector  := X"000";
                 SRVAL_B : bit_vector  := X"000";

                 WRITE_MODE_A : string := "WRITE_FIRST";
                 WRITE_MODE_B : string := "WRITE_FIRST"
                 );

        port (DOA   : out std_logic_vector(15 downto 0);
              DOB   : out std_logic_vector(15 downto 0);
              DOPA  : out std_logic_vector(1 downto 0);
              DOPB  : out std_logic_vector(1 downto 0);
              ADDRA : in  std_logic_vector(9 downto 0);
              ADDRB : in  std_logic_vector(9 downto 0);
              CLKA  : in  std_ulogic; CLKB  : in  std_ulogic;
              DIA   : in  std_logic_vector(15 downto 0);
              DIB   : in  std_logic_vector(15 downto 0);
              DIPA  : in  std_logic_vector(1 downto 0);
              DIPB  : in  std_logic_vector(1 downto 0);
              ENA   : in  std_ulogic; ENB   : in  std_ulogic;
              SSRA  : in  std_ulogic; SSRB  : in  std_ulogic;
              WEA   : in  std_ulogic; WEB   : in  std_ulogic);
    end component; 

    component RAMB4_S4
        generic (
            INIT_00 : bit_vector;
            INIT_01 : bit_vector;
            INIT_02 : bit_vector;
            INIT_03 : bit_vector;
            INIT_04 : bit_vector;
            INIT_05 : bit_vector;
            INIT_06 : bit_vector;
            INIT_07 : bit_vector;
            INIT_08 : bit_vector;
            INIT_09 : bit_vector;
            INIT_0A : bit_vector;
            INIT_0B : bit_vector;
            INIT_0C : bit_vector;
            INIT_0D : bit_vector;
            INIT_0E : bit_vector;
            INIT_0F : bit_vector);
        port (
            DO   : out STD_LOGIC_VECTOR (3 downto 0);
            ADDR : in  STD_LOGIC_VECTOR (9 downto 0);
            CLK  : in  STD_ULOGIC;
            DI   : in  STD_LOGIC_VECTOR (3 downto 0);
            EN   : in  STD_ULOGIC;
            RST  : in  STD_ULOGIC;
            WE   : in  STD_ULOGIC);
    end component;
    
    component VCC port (p : out std_ulogic); end component;
    
    component SRL16
        generic (INIT   : bit_vector);
        port (Q   : out STD_ULOGIC;
              A0  : in  STD_ULOGIC; A1  : in  STD_ULOGIC;
              A2  : in  STD_ULOGIC; A3  : in  STD_ULOGIC;
              CLK : in  STD_ULOGIC; D   : in  STD_ULOGIC);
    end component;

    component FDE_VECTOR
        generic (WIDTH : integer; SLICE : integer);
        port (CLK : in  std_logic; CE  : in  std_logic;
              D   : in  std_logic_vector (WIDTH-1 downto 0);
              Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component FDE_1
        generic (INIT : bit);
        port (Q  : out std_ulogic; C  : in  std_ulogic;
              CE : in  std_ulogic; D  : in  std_ulogic);
    end component;

    component FDCE
        generic (INIT : bit);
        port (Q   : out STD_ULOGIC; C : in  STD_ULOGIC;
              CE  : in  STD_ULOGIC; D : in  STD_ULOGIC;
              CLR : in  STD_ULOGIC);
    end component; 

    component FDC
        generic (INIT : bit);
        port (Q   : out STD_ULOGIC; C : in  STD_ULOGIC;
              D : in  STD_ULOGIC; CLR : in  STD_ULOGIC);
    end component; 

    component RAM16X1D
        generic (INIT : bit_vector(15 downto 0));
        port (DPO   : out std_ulogic; SPO   : out std_ulogic;
              A0    : in  std_ulogic; A1    : in  std_ulogic;
              A2    : in  std_ulogic; A3    : in  std_ulogic;
              D     : in  std_ulogic;
              DPRA0 : in  std_ulogic; DPRA1 : in  std_ulogic;
              DPRA2 : in  std_ulogic; DPRA3 : in  std_ulogic;
              WCLK  : in  std_ulogic;
              WE    : in  std_ulogic);
    end component;
    
    component RAM16X1D_1
        generic (INIT : bit_vector);
        port (DPO   : out std_ulogic; SPO   : out std_ulogic;
              A0    : in std_ulogic; A1    : in std_ulogic;
              A2    : in std_ulogic; A3    : in std_ulogic;        
              D     : in std_ulogic;
              DPRA0 : in std_ulogic; DPRA1 : in std_ulogic;
              DPRA2 : in std_ulogic; DPRA3 : in std_ulogic;        
              WCLK  : in std_ulogic; WE    : in std_ulogic);
    end component;


    ---------------------------------------------------------------------------
    -- 				MANIKLIB
    ---------------------------------------------------------------------------
    component FDC_VECTOR
        generic (WIDTH : integer; SLICE : integer);
        port (CLK : in  std_logic; CLR : in  std_logic;
              D   : in  std_logic_vector (WIDTH-1 downto 0);
              Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component FDCE_VECTOR
        generic (WIDTH : integer; SLICE : integer);
        port (CLK : in  std_logic;
              CE  : in  std_logic; CLR : in  std_logic;
              D   : in  std_logic_vector (WIDTH-1 downto 0);
              Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component SPECMUX_AND_VECTOR
        generic (WIDTH : integer; SLICE : integer);
        port (SEL0 : in  std_logic; SEL1 : in  std_logic;
              A    : in  std_logic_vector (WIDTH-1 downto 0);
              B    : in  std_logic_vector (WIDTH-1 downto 0);
              O    : out std_logic_vector (WIDTH-1 downto 0));
    end component;

    component FDCE_ALT2_VECTOR
        generic (WIDTH : integer; SLICE : integer; XORY : integer);
        port (CLK : in  std_logic; CE : in std_logic; CLR : in std_logic;
              D   : in  std_logic_vector (WIDTH-1 downto 0);
              Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component FDRE_VECTOR
        generic (WIDTH : integer; SLICE : integer);
        port (CLK : in  std_logic; CE : in std_logic; R : in std_logic;
              D   : in  std_logic_vector (WIDTH-1 downto 0);
              Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component FDRSE_VECTOR
        generic (
            WIDTH : integer;
            SLICE : integer);
        port (
            CLK : in  std_logic;
            CE  : in  std_logic;
            SET : in  std_logic;
            D   : in  std_logic_vector (WIDTH-1 downto 0);
            Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component CMPEQ_4C
        generic (SLICE : integer);
        port (A : in  std_logic_vector (3 downto 0);
              B : in  std_logic_vector (3 downto 0);
              C : in  std_logic; O : out std_logic);
    end component;

    component CMPEQ_4
      generic (SLICE : integer);
      port (A : in  std_logic_vector (3 downto 0);
            B : in  std_logic_vector (3 downto 0);
            O : out std_logic);
    end component;
    
    component CMPEQ_4ALTTWO
        generic (SLICE : integer; SPLIT : boolean; ANDBEL: integer);
        port (A : in  std_logic_vector (3 downto 0);
              B : in  std_logic_vector (3 downto 0);
              C : in  std_logic := '1';
              D : in  std_logic := '1';
              O : out std_logic);
    end component;

    component CMPEQ_4ALT
        generic (SPLIT_SLICE : boolean; AND_CD : boolean);
        port (A : in  std_logic_vector (3 downto 0);
              B : in  std_logic_vector (3 downto 0);
              C : in  std_logic := '1';
              D : in  std_logic := '1';
              O : out std_logic); 
    end component;

    component MULT_2_VECTOR
        generic (WIDTH : integer;
                 SLICE : integer);
        port (A    : in  std_logic_vector (WIDTH-1 downto 0);
              B    : in  std_logic_vector (1 downto 0);
              MOUT : out std_logic_vector (WIDTH-1 downto 0));
    end component;        

    component MUX_ADD_VECTOR
        generic (WIDTH : integer; SLICE : integer);
        port (A   : in  std_logic_vector (WIDTH-1 downto 0);
              B   : in  std_logic_vector (WIDTH-1 downto 0);
              C   : in  std_logic_vector (WIDTH-1 downto 0);
              ADD : in  std_logic;
              O   : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component ADD_MUX_VECTOR
        generic (WIDTH : integer; SLICE : integer);
        port (a   : in  std_logic_vector (WIDTH-1 downto 0);
              b1  : in  std_logic_vector (WIDTH-1 downto 0);
              b2  : in  std_logic_vector (WIDTH-1 downto 0);
              sel : in  std_logic;
              c   : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component MUX4DO_1E_VECTOR
        generic (WIDTH : integer;
                 ADJ   : integer); 
        port (S0   : in  std_logic;
              S0_D : in  std_logic;
              S1   : in  std_logic;
              EN   : in  std_logic;
              V0   : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              V1   : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              V2   : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              V3   : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              O    : out std_logic_vector (WIDTH-1 downto 0);
              DO   : out std_logic_vector (WIDTH-1 downto 0)); 
    end component;

    component AND2_VECT_BIT
        generic (WIDTH : integer; SLICE : integer);
        port (a : in  std_logic_vector (WIDTH-1 downto 0);
              b : in  std_logic;
              c : out std_logic_vector (WIDTH-1 downto 0));
    end component;

    component PRIO_MUX3_VECTOR
        generic (WIDTH : integer; ROWDIV : integer; SLICE : integer);
        port (ASEL : in  std_logic := '0'; BSEL : in  std_logic  := '0';
              A    : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              B    : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              C    : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              O    : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component SPECMUX1_VECTOR
        generic (WIDTH : integer; SLICE : integer);
        port (EN : in  std_logic := '1'; S : in  std_logic;
              A  : in  std_logic_vector (WIDTH-1 downto 0);
              B  : in  std_logic_vector (WIDTH-1 downto 0);
              O  : out std_logic_vector (WIDTH-1 downto 0));
    end component;

    component FDRE_ALT2_VECTOR
        generic (WIDTH : integer; SLICE : integer; XORY : integer);
        port (CLK : in  std_logic; CE : in  std_logic; R : in  std_logic;
              D   : in  std_logic_vector (WIDTH-1 downto 0);
              Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component MUX4_1E_VECTOR
        generic (WIDTH : integer; ROWDIV : integer ; SLICE : integer);
        port (S0 : in  std_logic; S1 : in  std_logic; EN : in  std_logic;
              V0 : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              V1 : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              V2 : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              V3 : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              O  : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component PRIO_MUX4_VECTOR
        generic (WIDTH : integer);
        port (S0  : in  std_logic;
              S1  : in  std_logic;
              S2  : in  std_logic;
              S3  : in  std_logic;
              M5S : in  std_logic;
              V0  : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              V1  : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              V2  : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              V3  : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              O   : out std_logic_vector (WIDTH-1 downto 0));
    end component;    

    component PRIO_MUX4_VECTOR_SS
        generic (WIDTH : integer);
        port (S0  : in  std_logic; S1  : in  std_logic;
              S2  : in  std_logic; S3  : in  std_logic;
              M5S : in  std_logic;
              V0  : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              V1  : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              V2  : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              V3  : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
              O   : out std_logic_vector (WIDTH-1 downto 0));
    end component;
        
    component MUX4_1E_VECTOR_SS
        generic (WIDTH      : integer);
        port    (S0         : in  std_logic;
                 S1         : in  std_logic;
                 EN         : in  std_logic;
                 V0         : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
                 V1         : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
                 V2         : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
                 V3         : in  std_logic_vector (WIDTH-1 downto 0) := (others => '0');
                 O          : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component ADDER_VECTOR
        generic (WIDTH : integer;
                 SLICE : integer);
        port (A   : in  std_logic_vector (WIDTH-1 downto 0);
              B   : in  std_logic_vector (WIDTH-1 downto 0);
              SUM : out std_logic_vector (WIDTH-1 downto 0));
    end component;

    component SPECMUX2_VECTOR
        generic (WIDTH : integer;
                 SLICE : integer); 
        port (ASEL : in  std_logic; BSEL : in  std_logic;
              A    : in  std_logic_vector (WIDTH-1 downto 0);
              B    : in  std_logic_vector (WIDTH-1 downto 0);
              O    : out std_logic_vector (WIDTH-1 downto 0)); 
    end component;

    component SPECMUX_NOTOR_VECTOR
        generic (WIDTH : integer; SLICE : integer);
        port (SEL0 : in  std_logic; SEL1 : in  std_logic;
              A    : in  std_logic_vector (WIDTH-1 downto 0);
              B    : in  std_logic_vector (WIDTH-1 downto 0);
              O    : out std_logic_vector (WIDTH-1 downto 0));
    end component;

    component FD_VECTOR
        generic (WIDTH : integer; SLICE : integer); 
        port (CLK : in  std_logic;
              D   : in  std_logic_vector (WIDTH-1 downto 0);
              Q   : out std_logic_vector (WIDTH-1 downto 0)); 
    end component;

    component FD_ALT_VECTOR
      generic (WIDTH : integer; XORY : integer; ADJ : integer);
      port (CLK : in  std_logic;
            D   : in  std_logic_vector (WIDTH-1 downto 0);
            Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;

    component FD_ALT_VECTOR_SS
        generic (WIDTH : integer; XORY  : integer);
        port (CLK : in  std_logic;
              D   : in  std_logic_vector (WIDTH-1 downto 0);
              Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;

    component FDRE_ALT_VECTOR
        generic (WIDTH : integer; XORY  : integer);
        port (CLK : in  std_logic; CE  : in  std_logic;
              R   : in  std_logic;
              D   : in  std_logic_vector (WIDTH-1 downto 0);
              Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;

    component FDE_ALT2_VECTOR
        generic (WIDTH : integer; SLICE : integer; XORY  : integer);
        port (CLK : in  std_logic; CE  : in  std_logic;
              D   : in  std_logic_vector (WIDTH-1 downto 0);
              Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;

    component FDE_ALT_VECTOR
        generic (WIDTH : integer; XORY  : integer; SPLIT : boolean);
        port (CLK : in  std_logic; CE  : in  std_logic;
              D   : in  std_logic_vector (WIDTH-1 downto 0);
              Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;

    component FDCE_ALT_VECTOR
        generic (WIDTH : integer; XORY  : integer; SPLIT : boolean);
        port (CLK : in  std_logic; CE  : in  std_logic;
              CLR : in  std_logic;
              D   : in  std_logic_vector (WIDTH-1 downto 0);
              Q   : out std_logic_vector (WIDTH-1 downto 0));
    end component;
    
    component LNOT
        generic (SLICE : integer; GBEL : integer);
        port (I : in std_logic; O : out std_logic);
    end component;

    component xpll
        port (iclk : in  std_logic; oclk : out std_logic);
    end component;

    component xilspram
        generic (MEM_DATA_WIDTH : integer;
                 MEM_ADDR_WIDTH : integer);
        port (clk    : in  std_logic;
              addr   : in  std_logic_vector (MEM_ADDR_WIDTH-1 downto 0);
              data_i : in  std_logic_vector (MEM_DATA_WIDTH-1 downto 0);
              enb    : in  std_logic;
              rst    : in  std_logic;
              we     : in  std_logic;
              data_o : out std_logic_vector (MEM_DATA_WIDTH-1 downto 0));
    end component;
    
    component xilspram_v2
        generic (MEM_DATA_WIDTH : integer;
                 MEM_ADDR_WIDTH : integer);
        port (clk    : in  std_logic;
              addr   : in  std_logic_vector(MEM_ADDR_WIDTH-1 downto 0);
              data_i : in  std_logic_vector(MEM_DATA_WIDTH-1 downto 0);
              enb    : in  std_logic;
              rst    : in  std_logic;
              we     : in  std_logic;
              data_o : out std_logic_vector(MEM_DATA_WIDTH-1 downto 0));
    end component;
    
    -- attributes
    attribute RLOC : string;
    attribute INIT : string;
    attribute BEL  : string;
    attribute KEEP : string;
    attribute USELOWSKEWLINES : string;

    attribute INIT_00 : string;attribute INIT_01 : string;attribute INIT_02 : string;attribute INIT_03 : string;
    attribute INIT_04 : string;attribute INIT_05 : string;attribute INIT_06 : string;attribute INIT_07 : string;
    attribute INIT_08 : string;attribute INIT_09 : string;attribute INIT_0A : string;attribute INIT_0B : string;
    attribute INIT_0C : string;attribute INIT_0D : string;attribute INIT_0E : string;attribute INIT_0F : string;
    attribute INIT_10 : string;attribute INIT_11 : string;attribute INIT_12 : string;attribute INIT_13 : string;
    attribute INIT_14 : string;attribute INIT_15 : string;attribute INIT_16 : string;attribute INIT_17 : string;
    attribute INIT_18 : string;attribute INIT_19 : string;attribute INIT_1A : string;attribute INIT_1B : string;
    attribute INIT_1C : string;attribute INIT_1D : string;attribute INIT_1E : string;attribute INIT_1F : string;
    attribute INIT_20 : string;attribute INIT_21 : string;attribute INIT_22 : string;attribute INIT_23 : string;
    attribute INIT_24 : string;attribute INIT_25 : string;attribute INIT_26 : string;attribute INIT_27 : string;
    attribute INIT_28 : string;attribute INIT_29 : string;attribute INIT_2A : string;attribute INIT_2B : string;
    attribute INIT_2C : string;attribute INIT_2D : string;attribute INIT_2E : string;attribute INIT_2F : string;
    attribute INIT_30 : string;attribute INIT_31 : string;attribute INIT_32 : string;attribute INIT_33 : string;
    attribute INIT_34 : string;attribute INIT_35 : string;attribute INIT_36 : string;attribute INIT_37 : string;
    attribute INIT_38 : string;attribute INIT_39 : string;attribute INIT_3A : string;attribute INIT_3B : string;
    attribute INIT_3C : string;attribute INIT_3D : string;attribute INIT_3E : string;attribute INIT_3F : string;

    -- functions
    function rloc_string (row : integer; col : integer; slice : integer; rrev : integer) return string;

    function rloc_string    (row  : integer; col : integer; slice : integer;
                             rrev : integer; adj : boolean; radj  : integer; cadj : integer) return string;

    function rloc_string    (row : integer; col : integer; slice : integer) return string;

    function rloc_string    (row : integer; col : integer; adj : boolean;
                             radj: integer; cadj  : integer) return string;
        
    function rloc_string    (row : integer; col : integer) return string;
    
    function bel_string     (constant i : integer) return string;
    function bel_string_ff  (constant i : integer) return string;
    function bel_string_slv (constant i : std_logic_vector (1 downto 0)) return string;
    

end manikxilinx;

package body manikxilinx is

    -- purpose: return relative location in "RnCn.S[0|1]" form or "XnYn" form
    function rloc_string (row : integer; col : integer; slice : integer; rrev : integer) return string is
        constant arow : integer := - (row - rrev + 1) ;
    begin
        if FPGA_Family /= "Virtex2" then
            return "R"  & integer'image(row) &
                   "C"  & integer'image(col) &
                   ".S" & integer'image(slice);
        else
            return "X" & integer'image(col+boolnot(slice)) &
                   "Y" & integer'image(arow);
        end if;
    end rloc_string;

    function rloc_string (row : integer; col : integer; slice : integer;
                          rrev  : integer; adj : boolean; radj : integer;
                          cadj  : integer) return string is
        constant arow : integer := - (row - rrev + 1) ;
    begin
        if FPGA_Family /= "Virtex2" then
            return "R"  & integer'image(row) &
                   "C"  & integer'image(col) &
                   ".S" & integer'image(slice);
        elsif adj = true then
            return "X" & integer'image(col+boolnot(slice)+cadj) &
                   "Y" & integer'image(arow+radj);
        else
            return "X" & integer'image(col+boolnot(slice)) &
                   "Y" & integer'image(arow);
        end if;
    end rloc_string;

    -- purpose: return relative location in "RnCn.S[0|1]" form or "XnYn" form
    function rloc_string (row : integer; col : integer; slice : integer) return string is
    begin
        if FPGA_Family /= "Virtex2" then
            return "R"  & integer'image(row) &
                   "C"  & integer'image(col) &
                   ".S" & integer'image(slice);
        else
            return "X" & integer'image(col+boolnot(slice)) &
                   "Y" & integer'image(row);
        end if;
    end rloc_string;

    -- purpose: return relative location in "RnCn" form (without slice)
    function rloc_string (row : integer;
                          col : integer;
                          adj : boolean;
                          radj: integer;
                          cadj: integer) return string is
    begin
        if FPGA_Family /= "Virtex2" then
            return "R" & integer'image(row) &
                   "C" & integer'image(col);
        elsif adj = true then                        
            return "X" & integer'image(col+cadj) &
                   "Y" & integer'image(row+radj);
        else
            return "X" & integer'image(col) &
                   "Y" & integer'image(row);
        end if;
    end rloc_string;
    
    -- purpose: return relative location in "RnCn" form (without slice)
    function rloc_string (row : integer; col : integer) return string is
    begin
        if FPGA_Family /= "Virtex2" then
            return "R" & integer'image(row) &
                   "C" & integer'image(col);
        else
            return "X" & integer'image(col) &
                   "Y" & integer'image(row);
        end if;
    end rloc_string;
    
    -- purpose: return BEL attribute for LUT primitive
    function bel_string_slv (constant i : std_logic_vector (1 downto 0)) return string is
    begin  -- bel_string_slv
        if i(1) = '1' then
            return "F";
        else
            return "G";
        end if;
    end bel_string_slv;
    
    -- purpose: returns BEL attribute for a LUT primitive 
    function bel_string (constant i : integer) return string is
    begin  -- bel_string
        if (i mod 2) = 0 then
            return "F";
        else
            return "G";
        end if;
    end bel_string;

    -- purpose: return BEL attribute for a FF
    function bel_string_ff (constant i : integer) return string is
    begin  -- bel_string_ff
        if (i mod 2) = 0 then
            return "FFX";
        else
            return "FFY";
        end if;
    end bel_string_ff;
end manikxilinx;
