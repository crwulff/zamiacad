entity arrayTest is
  port( a : in bit; z : out bit);
end entity arrayTest;

architecture RTL of arrayTest is 

  CONSTANT OFFSET : NATURAL := 1;
  CONSTANT WIDTH  : POSITIVE := 6;

  constant v2 : bit_vector(offset to offset + width -1 ) := "010101";
  constant v3 : bit_vector(offset to offset + width -1 ) := "101010";

  CONSTANT v1 : bit_vector(OFFSET to OFFSET + WIDTH - 1) := 
 
     v2(OFFSET to OFFSET + WIDTH - 1)  or 
     v3(OFFSET to OFFSET + WIDTH - 1);

  function f1
    (p1   : bit_vector;
     p2   : string := "" ;
     p3   : string := "" ) return bit  is
  begin
    return '0';
  end f1 ;

constant c : boolean :=
  f1(v1(offset to offset + width - 1) & '1') = '1';


begin

end architecture RTL;

