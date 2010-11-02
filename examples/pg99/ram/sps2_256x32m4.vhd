-- VHDL Model Created from SGE Symbol sps2_256x32m4.sym -- Dec  1 15:37:19 1998

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_misc.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;

architecture BEHAVIORAL of SPS2_256X32M4 is
    type RAM_TYPE is array (0 to 255) of STD_LOGIC_VECTOR(31 downto 0);
    signal RAM : RAM_TYPE;
    signal Q_I : STD_LOGIC_VECTOR(31 downto 0);
begin
       
    write : process (CK)
    begin  -- process write
	-- activities triggered by rising edge of clock
	if CK'event and CK = '1' and CSN='0' and WEN ='0' then
	    RAM(CONV_INTEGER(unsigned(A))) <= D;
	end if;
    end process write;

    read : process (CK)
    begin  -- process read   
	-- activities triggered by rising edge of clock
	if CK'event and CK = '1' then
	    if OEN='0' and CSN='0' then
		Q_I <= RAM(CONV_INTEGER(unsigned(A)));
	    else
		Q_I <= (others => 'X');
	    end if;
	end if;
    end process;

    Q <= Q_I when OEN='0' and CSN='0' else (others => 'Z');
    
end BEHAVIORAL;
