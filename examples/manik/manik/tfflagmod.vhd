  
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;

use UNISIM.vcomponents.all;
library UNISIM;

entity tfflagmod is
    generic (SLICE : integer := 0);
    port (flagop : in  std_logic_vector (2 downto 0);
          alu_zf : in  std_logic;
          alu_co : in  std_logic;
          alu_nf : in  std_logic;
          tfflag : out std_logic);
    
end tfflagmod;

architecture Behavioral of tfflagmod is
    attribute BEL : string;
begin  -- Behavioral

    generic_tech: if Technology /= "XILINX" generate
        tf_proc: process (flagop, alu_zf, alu_co, alu_nf)
        begin  -- process tf_proc
            case flagop is
                -- cmpeqi
                when "000"  => tfflag <= alu_zf;
                               -- cmpeq
                when "001"  => tfflag <= alu_zf;
                               -- cmplt signed <                
                when "100"  => tfflag <= alu_nf ;
                               -- cmplti signed <                
                when "110"  => tfflag <= alu_nf ;
                               -- cmpgt signed >
                when "101"  => tfflag <= (not alu_zf) and (not alu_nf);
                               -- cmpgti signed >
                when "111"  => tfflag <= (not alu_zf) and (not alu_nf);
                               -- cmphs unsigned >=
                when "010"  => tfflag <= not alu_co or alu_zf;
                               -- cmpls unsigned <=
                when "011"  => tfflag <=     alu_co or alu_zf;
                when others => tfflag <= '0';
            end case;
        end process tf_proc;        
    end generate generic_tech;

    xilinx_tech: if Technology = "XILINX" generate
-------------------------------------------------------------------------------
--                            Input mapping for LUT3
-------------------------------------------------------------------------------
--      I0 - alu_nf
--      I1 - alu_zf
--      I2 - flagop(0)
-------------------------------------------------------------------------------
--      Truth table for LUT3
-------------------------------------------------------------------------------
--      I2      I1      I0              Output
-------------------------------------------------------------------------------
--      0       X       0               INIT[0,2]->0
--      0       X       1               INIT[1,3]->1
--      1       0       0               INIT[4]  ->1
--      1       X       1               INIT[5,7]->0
--      1       1       0               INIT[6]  ->0
-------------------------------------------------------------------------------
--      INIT            7 6 5 4 - 3 2 1 0
--                      0 0 0 1 - 1 0 1 0 -> X"1A"
-------------------------------------------------------------------------------
--                            Input mapping for LUT4
-------------------------------------------------------------------------------
--      I0 - alu_co
--      I1 - alu_zf
--      I2 - flagop(0)
--      I3 - flagop(1)
-------------------------------------------------------------------------------
--      Truth table for LUT3
-------------------------------------------------------------------------------
--      I3      I2      I1      I0      Output
-------------------------------------------------------------------------------
--      0       0       0       X       INIT[0,1] -> 0
--      0       0       1       X       INIT[2,3] -> 1
--      0       1       0       X       INIT[4,5] -> 0
--      0       1       1       X       INIT[6,7] -> 1
--      1       0       1       X       INIT[A,B] -> 1
--      1       0       0       0       INIT[8]   -> 1
--      1       0       0       1       INIT[9]   -> 0
--      1       1       1       X       INIT[E,F] -> 1
--      1       1       0       0       INIT[C]   -> 0
--      1       1       0       1       INIT[D],  -> 1
-------------------------------------------------------------------------------
--      INIT            F E D C - B A 9 8 - 7 6 5 4 - 3 2 1 0
--                      1 1 1 0 - 1 1 0 1 - 1 1 0 0 - 1 1 0 0 - X"EDCC"
-------------------------------------------------------------------------------
        
        signal sel0 : std_logic;
        signal sel1 : std_logic;
        
        attribute INIT of TF_SEL1 : label is "1A";
        attribute INIT of TF_SEL0 : label is "EDCC";

        attribute RLOC of sel0_1 : label is rloc_string(0,0,SLICE);
        
    begin  -- XILINX_virtex

        TF_SEL1: LUT3
            generic map (INIT => X"1A")
            port map (O  => sel1,  I0 => alu_nf,
                      I1 => alu_zf,I2 => flagop(0));

        TF_SEL0 : LUT4
            generic map (INIT => X"EDCC")
            port map (O  => sel0,  I0 => alu_co,
                      I1 => alu_zf,I2 => flagop(0),
                      I3 => flagop(1));
        
        sel0_1 : MUXF5
            port map (O  => tfflag, I0 => sel0,
                      I1 => sel1,   S  => flagop(2));
    end generate xilinx_tech;
end Behavioral;
