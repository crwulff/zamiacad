library IEEE;
use IEEE.std_logic_1164.all;

entity AND_GATE is
  port(
    A : in std_logic;
    B : in std_logic;
    Y : out std_logic
  );
end;

architecture DATAFLOW of AND_GATE is
begin
  -- your code here
  Y <= reject 1 ns inertial A and B after 3 ns ;
end DATAFLOW;


library IEEE;
use IEEE.std_logic_1164.all;

entity AND_K is
  generic(K : POSITIVE);
  port(
    X : in std_logic_vector(1 to K);
    Y : out std_logic
  );
end;

architecture RECURSIVE of AND_K is

  component AND_K
    generic(K : POSITIVE);
    port(
      X : in std_logic_vector(1 to K);
      Y : out std_logic
    );
  end component;
  
  component AND_GATE
    port(
      A : in std_logic;
      B : in std_logic;
      Y : out std_logic
    );
  end component;

  signal S, T : std_logic;
  
begin

  EVEN : if ((K mod 2) = 0) and not (K = 2) generate
    I1 : AND_K generic map(K/2) port map(X(1 to K/2), S);
    I2 : AND_K generic map(K/2) port map(X(K/2+1 to K), T);
    I3 : AND_GATE port map(S, T, Y);
  end generate;

  ODD  : if ((K mod 2) = 1) and not (K = 3) generate
    I4 : AND_K generic map((K+1)/2) port map(X(1 to (K+1)/2), S);
    I5 : AND_K generic map((K-1)/2) port map(X((K+1)/2+1 to K), T);
    I6 : AND_GATE port map(S, T, Y);
  end generate;

  THREE : if K = 3 generate
    I7 : AND_GATE port map(X(1), X(2), S);
    I8 : AND_GATE port map(S, X(3), Y);
  end generate;

  TWO : if K = 2 generate
    I9 : AND_GATE port map(X(1), X(2), Y);
  end generate;

end RECURSIVE;


library IEEE;
use IEEE.std_logic_1164.all;

entity OR_K is
  generic(K : POSITIVE);
  port(
    X : in std_logic_vector(1 to K);
    Y : out std_logic
  );
end;

architecture RECURSIVE of OR_K is

  component OR_K
    generic(K : POSITIVE);
    port(
      X : in std_logic_vector(1 to K);
      Y : out std_logic
    );
  end component;
  
  component OR_GATE
    port(
      A : in std_logic;
      B : in std_logic;
      Y : out std_logic
    );
  end component;

  signal S, T : std_logic;
  
begin

  EVEN : if ((K mod 2) = 0) and not (K = 2) generate
    I1 : OR_K generic map(K/2) port map(X(1 to K/2), S);
    I2 : OR_K generic map(K/2) port map(X(K/2+1 to K), T);
    I3 : OR_GATE port map(S, T, Y);
  end generate;

  ODD  : if ((K mod 2) = 1) and not (K = 3) generate
    I4 : OR_K generic map((K+1)/2) port map(X(1 to (K+1)/2), S);
    I5 : OR_K generic map((K-1)/2) port map(X((K+1)/2+1 to K), T);
    I6 : OR_GATE port map(S, T, Y);
  end generate;

  THREE : if K = 3 generate
    I7 : OR_GATE port map(X(1), X(2), S);
    I8 : OR_GATE port map(S, X(3), Y);
  end generate;

  TWO : if K = 2 generate
    I9 : OR_GATE port map(X(1), X(2), Y);
  end generate;

end RECURSIVE;

-- test toplevel

library IEEE;
use IEEE.std_logic_1164.all;

entity AND_4 is
  port(
    X : in std_logic_vector(1 to 4);
    Y : out std_logic
  );
end;

architecture ARCH of AND_4 is

  component AND_K
    generic(K : POSITIVE);
    port(
      X : in std_logic_vector(1 to K);
      Y : out std_logic
    );
  end component;
  
begin

  I1 : AND_K generic map(4) port map(X(1 to 4), Y);

end ARCH;
