entity test_b04 is
end test_b04;

architecture testbench of test_b04 is
   component b04 is
     port( RESTART  : in bit;
           AVERAGE  : in bit;
           ENABLE   : in bit;
           DATA_IN  : in integer range 127 downto -128;
           DATA_OUT : out integer range 127 downto -128;
           RESET    : in bit;
           CLOCK    : in bit
                              );
   end component;
   signal t_restart,t_average, t_enable, t_reset, t_clock: bit:='0';
   signal t_data_in, t_data_out: integer range 127 downto -128;
begin
   b04U1: b04 port map(restart=>t_restart, average=>t_average,
                 enable=>t_enable, data_in=>t_data_in, 
                 data_out=>t_data_out, reset=>t_reset, clock=>t_clock);
   
   t_clock<=not t_clock after 10 ns;
   
   process 
       begin
         --first vector
         wait on t_clock until t_clock='1';
         t_restart<='0';
         t_average<='0';
         t_enable<='0';
         t_data_in<=0;
         t_reset<='0';
         
         --2 vector
         wait on t_clock until t_clock='1';
         t_restart<='0';
         t_average<='0';
         t_enable<='0';
         t_data_in<=0;
         t_reset<='1';
         
         --3 vector
         wait on t_clock until t_clock='1';
         t_restart<='0';
         t_average<='0';
         t_enable<='0';
         t_data_in<=-1;
         t_reset<='0';
         
         --4 vector
         wait on t_clock until t_clock='1';
         t_restart<='0';
         t_average<='0';
         t_enable<='1';
         t_data_in<=7;
         t_reset<='0';
         
         --5 vector
         wait on t_clock until t_clock='1';
         t_restart<='1';
         t_average<='0';
         t_enable<='1';
         t_data_in<=-1;
         t_reset<='0';
         
         --6 vector
         wait on t_clock until t_clock='1';
         t_restart<='0';
         t_average<='0';
         t_enable<='0';
         t_data_in<=0;
         t_reset<='0';
         
         --7 vector
         wait on t_clock until t_clock='1';
         t_restart<='0';
         t_average<='0';
         t_enable<='1';
         t_data_in<=7;
         t_reset<='0';
         
         --8 vector
         wait on t_clock until t_clock='1';
         t_restart<='0';
         t_average<='0';
         t_enable<='1';
         t_data_in<=2;
         t_reset<='0';
         
         --9 vector
         wait on t_clock until t_clock='1';
         t_restart<='0';
         t_average<='0';
         t_enable<='1';
         t_data_in<=0;
         t_reset<='0';
         
         --10 vector
         wait on t_clock until t_clock='1';
         t_restart<='0';
         t_average<='1';
         t_enable<='1';
         t_data_in<=0;
         t_reset<='0';
         
         --11 vector
         wait on t_clock until t_clock='1';
         t_restart<='0';
         t_average<='0';
         t_enable<='1';
         t_data_in<=0;
         t_reset<='0';
        
        
         wait;  
   end process;
end testbench;