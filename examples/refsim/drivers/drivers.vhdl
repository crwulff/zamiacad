-- Run DRIVERS(SINGLE) for 50 ns
-- Run DRIVERS(MULTIPLE) for 100 ns
-- Run DRIVERS(multipleNonresolved) for 30 ns
-- Run DRIVERS(nonoverlappingNonresolved) for 30 ns
-- Run DRIVERS(partiallyOverlappingNonresolved) for 30 ns

library Work;
use Work.SimPackage.all;

library IEEE;
use IEEE.std_logic_1164.all;

entity drivers is
end entity drivers;

architecture single of drivers is 
	
	signal sig : bit_vector( 7 downto 0 );
	
	signal sig2 : std_logic_vector (7 downto 0) ;
	 
begin
  
  process is
  begin

  	wait for 10 ns;

    assert sig = "00000000" report "Initialization of bits to zeros failed. Actual: " & str(sig);
    assert sig2 = "UUUUUUUU" report "Initialization of std_logics to U-s failed. Actual: " & str(sig2);
      	
  	sig(7 downto 0) <= "11111111";
  	sig(3 downto 0) <= "0101";

    wait for 10 ns;
    
    assert sig = "11110101" report "2 subsequent assignments failed. Actual: " & str(sig);

    sig2(7 downto 0) <= "10101010";
    sig2(7 downto 4) <= "0101";
  	
    wait for 10 ns;

    assert sig2 = "01011010" report "Assignment to std_logic-s failed. Actual: " & str(sig2);
    
  	sig(7 downto 0) <= "11111111";
  	sig2(7 downto 0) <= "00000000";
    
    wait for 10 ns;

    assert sig = "11111111" report "Assignment to bits failed. Actual: " & str(sig);
    assert sig2 = "00000000" report "Assignment to std_logic-s failed. Actual: " & str(sig2);
    
  	wait;
  	
  end process;
  
end architecture single;


architecture multiple of drivers is 
  
  signal sig : std_logic_vector( 7 downto 0 );
  signal singleBit : std_logic;

begin

  one : process is
  begin

    wait for 10 ns;

    assert sig = "UUUUUUUU" report "Initialization of std_logics to U-s failed (1). Actual: " & str(sig);

    sig <= "11110000";

    wait for 10 ns;

    assert sig = "11XXX000" report "(1) 2 concurrent assignments (partially overlapping) failed. Actual: " & str(sig);
    wait for 10 ns;

    sig <= "UUUUUUUU";

    wait for 10 ns;

    assert sig = "UUUUUUUU" report "Initialization of std_logics to U-s failed (2). Actual: " & str(sig);

    sig(3 downto 0) <= "1111";

    wait for 10 ns;

    assert sig = "UUUU1X1X" report "(2) 2 concurrent assignments (overlapping range) failed. Actual: " & str(sig);

    sig <= "UUUUUUUU";

    wait for 10 ns;

    assert sig = "UUUUUUUU" report "Initialization of std_logics to U-s failed (3). Actual: " & str(sig);

    sig <= "11110000";

    wait for 10 ns;

    assert sig = "XXXXX0X0" report "(3) 2 concurrent assignments failed. Actual: " & str(sig);

    sig <= "00001000";

    wait for 10 ns;

    assert sig = "00001XXX" report "(4) 2 concurrent assignments failed. Actual: " & str(sig);

    singleBit <= '1';

    wait for 10 ns;

    assert singleBit = 'X' report "(5) 2 concurrent single bit assignments failed. Actual: " & chr(singleBit);

    wait;

  end process one;

  two : process is
  begin

    wait for 10 ns;

    sig(5 downto 2) <= "0010";

    wait for 10 ns;
    wait for 10 ns;
    wait for 10 ns;

    sig(3 downto 0) <= "1010";

    wait for 10 ns;
    wait for 10 ns;

    sig <= "00001010";

    wait for 10 ns;

    sig <= "00001111";

    wait for 10 ns;

    singleBit <= '0';

    wait for 10 ns;

    wait;

  end process two;

end architecture multiple;


architecture multipleNonresolved of drivers is 
  
  signal sig : bit_vector( 7 downto 0 );

begin

  one : process is
  begin

    wait for 10 ns;

    assert sig = "00000000" report "Initialization of bits to zeros failed. Actual: " & str(sig);

    sig <= "11110000";

    wait for 10 ns;

    wait;

  end process one;

  two : process is
  begin

    wait for 10 ns;

    sig <= "00001010";

    wait for 10 ns;

    wait;

  end process two;

end architecture multipleNonresolved;


architecture nonoverlappingNonresolved of drivers is 
  
  signal sig : bit_vector( 7 downto 0 );
     
begin
  
  one : process is
  begin

    wait for 10 ns;

    assert sig = "00000000" report "Initialization of bits to zeros failed. Actual: " & str(sig);
        
    sig(7 downto 4) <= "1111";

    wait for 10 ns;
    
    assert sig = "11110101" report "2 subsequent assignments failed. Actual: " & str(sig);
    
    wait;
    
  end process one;

  two : process is
  begin

    wait for 10 ns;
        
    sig(3 downto 0) <= "0101";

    wait for 10 ns;
    
    wait;
    
  end process two;

end architecture nonoverlappingNonresolved;

architecture partiallyOverlappingNonresolved of drivers is 
  
  signal sig : bit_vector( 7 downto 0 );
     
begin
  
  one : process is
  begin

    wait for 10 ns;

    assert sig = "00000000" report "Initialization of bits to zeros failed. Actual: " & str(sig);
        
    sig(7 downto 4) <= "1111";

    wait for 10 ns;
    
    assert sig = "11110101" report "2 subsequent assignments failed. Actual: " & str(sig);
    
    wait;
    
  end process one;

  two : process is
  begin

    wait for 10 ns;
        
    sig(4 downto 0) <= "10101";

    wait for 10 ns;
    
    wait;
    
  end process two;

end architecture partiallyOverlappingNonresolved;

