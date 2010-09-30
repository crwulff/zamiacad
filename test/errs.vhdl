library ieee;
use ieee.std_LOGIC_1164.all;

entity FUNE  is
  port (
	CLOCK   : in    std_logic;                     
	RSTN    : in    std_logic;                     
	BR      : in    std_logic_vector(3 downto 0);  
	ID      : in    std_logic_vector(15 downto 0); 
	BRC     : out   std_logic_vector(7 downto 0)
  );
end FUNE;

architecture a1 of FUNE is

  constant cvsRevConst : integer_vector(0 to lm_cvsRevLen(CVS_REV)-1) :=
                                               lm_cvsRevEnc(CVS_REV);


  signal foo : std_ulogic;

begin


end architecture;
