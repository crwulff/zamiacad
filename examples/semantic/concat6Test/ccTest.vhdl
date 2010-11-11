
entity ccTest is
  port( a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y : IN bit; z: out bit );
end entity ccTest;

architecture RTL of ccTest is 


   procedure myproc (p1 : bit_vector) is
   begin
   end myproc;

begin

  myproc(a&b&c&d&e&f&g&h&i&j&k&l&m&n&o&p&q&r&s&t&u&v&w&x&y);

end architecture RTL;

