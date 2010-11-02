-- based on leon uart, but a much simpler interface

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned."+";
use IEEE.std_logic_unsigned."-";

entity uart is
  port (
    reset        : in  std_logic;
    clk          : in  std_logic;

-- uart_out_type
    uarto_rtsn   : out std_logic;
    uarto_txd    : out std_logic;

-- uart_in_type
    uarti_ctsn   : in  std_logic;
    uarti_rxd    : in  std_logic;

-- transmit data
    txdata       : in  std_logic_vector (7 downto 0);
    te           : in  std_logic;       -- transmitter enable
    txdone       : out std_logic;       -- data transfer finished
    txnd         : in  std_logic;       -- transmitter take new data
    
-- receive data:       
    rdata        : out std_logic_vector (7 downto 0);
    rdready      : out std_logic;
    rdack        : in  std_logic;

-- debug output - monitor internal state of uart quaak
    dbg          : out std_logic_vector (7 downto 0)
  );

end; 

architecture rtl of uart is


  type rxfsmtype is (idle, startbit, data, stopbit);
  type txfsmtype is (idle, data, stopbit);


  type uartregs is record
    rxen   	:  std_logic;	-- receiver enabled
    txen   	:  std_logic;	-- transmitter enabled
    dready    	:  std_logic;	-- data ready
    rtsn      	:  std_logic;	-- request to send
    rsempty   	:  std_logic;	-- receiver shift register empty (internal)
    tsempty   	:  std_logic;	-- transmitter shift register empty
    thempty   	:  std_logic;	-- transmitter hold register empty
    -- Errors
    break  	:  std_logic;	-- break detected
    ovf    	:  std_logic;	-- receiver overflow
    frame     	:  std_logic;	-- framing error
    -- internal Registers
    rhold 	:  std_logic_vector(7 downto 0);
    rshift	:  std_logic_vector(7 downto 0);
    tshift	:  std_logic_vector(10 downto 0);
    thold 	:  std_logic_vector(7 downto 0);
    txdone      :  std_logic;
    txstate	:  txfsmtype;
    
    txclk 	:  std_logic_vector(2 downto 0);  -- tx clock divider
    txtick     	:  std_logic;	-- tx clock (internal)
    
    
    rxstate	:  rxfsmtype;
    
    rxclk 	:  std_logic_vector(2 downto 0); -- rx clock divider
    rxdb  	:  std_logic;   -- rx data filtering buffer
    rxtick     	:  std_logic;	-- rx clock (internal)
    
    tick     	:  std_logic;	-- rx clock (internal)
    scaler	:  std_logic_vector(11 downto 0);
  end record;


  signal r, rin : uartregs;
  
begin

  uartop : process(reset, r, uarti_rxd, uarti_ctsn, txdata, te, txnd, rdack )

    variable scaler       : std_logic_vector(11 downto 0);
    variable rxclk, txclk : std_logic_vector( 2 downto 0);
    variable rxd          : std_logic;
    variable v            : uartregs;
    variable brate        : std_logic_vector (11 downto 0);
    
  begin

    v := r;

    v.txtick := '0';
    v.rxtick := '0';
    v.tick   := '0';

    -- 25 MHz (tested)
    -- brate := "000001001111";  -- 79
    -- 10 MHz (tested)
    -- brate := "000000100000";  -- 32
    -- 6,67 MHz (simulated, measured and tested, works fine)
    brate := "000000010100";
    
--  scaler
    scaler := r.scaler - 1;

    if (r.rxen or r.txen) = '1' then
      v.scaler   := scaler;
      v.tick     := scaler(11) and not r.scaler(11);
      if v.tick = '1' then
        v.scaler := brate;
      end if;
    end if;

-- read data
     
    if rdack = '1' then
      v.dready := '0';
    end if;

    rdata <= r.rhold;

-- uart status
    
    v.frame    := '0';
    v.ovf      := '0';
    v.break    := '0';

-- uart control
    
    v.txen     := te;
--    v.txen     := '1';
    v.rxen     := '1';

-- tx clock

    txclk      := r.txclk + 1;
    if r.tick = '1' then
      v.txclk  := txclk;
      v.txtick := r.txclk(2) and not txclk(2);
    end if;

-- rx clock

    rxclk      := r.rxclk + 1;
    if r.tick = '1' then
      v.rxclk  := rxclk;
      v.rxtick := r.rxclk(2) and not rxclk(2);
    end if;

-- filter rx data

    v.rxdb := uarti_rxd;
    rxd    := r.rxdb;

-- transmitter operation

    case r.txstate is
      when idle =>	-- idle state
-- with flow control:
        if (r.txen and (not r.thempty) and r.txtick and (not uarti_ctsn)) = '1' then  
-- without rts/cts flow control:
--        if (r.txen and (not r.thempty) and r.txtick) = '1' then
          v.tshift := "10" & r.thold & '0';
          v.txstate := data; 
          v.thempty := '1';
          v.tsempty := '0';
          v.txclk := "001";
          v.txtick := '0';
        end if;

      when data =>	-- transmitt data frame
        if r.txtick = '1' then
          v.tshift := '1' & r.tshift(10 downto 1);
          if r.tshift(10 downto 1) = "1111111110" then
            v.tshift(0) := '1';
            v.txstate := stopbit;
          end if;
        end if;

      when stopbit =>	-- transmitt stop bit
        if r.txtick = '1' then
          v.tshift := '1' & r.tshift(10 downto 1);
          v.txstate := idle;
          v.tsempty := '1';
          v.txdone  := '1';
        end if;

    end case;

-- writing of tx data register must be done after tx fsm to get correct
-- operation of thempty flag

    if txnd = '1' then
      v.thold := txdata;
      v.thempty := '0';
      v.txdone := '0';
    end if;
    
-- receiver operation

    case r.rxstate is
      when idle =>	-- wait for start bit

--         if ((not r.rsempty) and not r.dready) = '1' then
--           v.rhold := r.rshift;
--           v.rsempty := '1';
--           v.dready := '1';
--         end if;

        if (r.rxen and (not rxd)) = '1' then
          v.rxstate := startbit;
          v.rshift := (others => '1');
          v.rxclk := "100";
          if v.rsempty = '0' then
            v.ovf := '1';
          end if;
          v.rsempty := '0';
          v.rxtick := '0';
        end if;

      when startbit =>	-- check validity of start bit
        if r.rxtick = '1' then
          if rxd = '0' then 
            v.rshift := rxd & r.rshift(7 downto 1);
            v.rxstate := data;
          else
            v.rxstate := idle;
          end if;
        end if;

      when data =>	-- receive data frame
        if r.rxtick = '1' then
          v.rshift := rxd & r.rshift(7 downto 1); 
          if r.rshift(0) = '0' then
            v.rxstate := stopbit; 
          end if;
        end if;

      when stopbit =>	-- receive stop bit
        if r.rxtick = '1' then
          if rxd = '1' then
            v.rsempty := '1';
            v.rhold := r.rshift;
            v.rsempty := '1';
            v.dready := '1';
          else
            if r.rshift = "00000000" then
              v.break := '1'; 		 -- break
            else
              v.frame := '1'; 		 -- framing error
            end if;
            v.rsempty := '1';
          end if;
          v.rxstate := idle;
        end if;

    end case;

    if r.rxtick = '1' then
      v.rtsn := r.dready and not r.rsempty;
    end if;

-- reset operation

    if reset = '1' then 
      v.rxen    := '0';
      v.txen    := '0';
      v.dready  := '0';
      v.rsempty := '1';
      v.tsempty := '1';
      v.thempty := '1';
      v.break   := '0';
      v.ovf     := '0';
      v.frame   := '0';
      v.rtsn    := '1';
      v.rhold   := (others => '0');
      v.rshift  := (others => '0');
      v.tshift  := "00000000001";
      v.thold   := "00000000";
      v.txstate := idle;
      v.txclk   := (others => '0');
      v.txtick  := '0';
      v.rxstate := idle;
      v.rxclk   := (others => '0');
      v.rxdb    := '0';
      v.rxtick  := '0';
      v.tick    := '0';
      v.scaler  := brate;
      v.txdone  := '0';
    end if;

-- update registers

    rin <= v;

-- drive outputs
    uarto_txd  <= r.tshift(0) ;
    uarto_rtsn <= r.rtsn;
-- FIXME:   uarto_txen <= r.txen;
-- FIXME:   uarto_rxen <= r.rxen;
-- FIXME:   apbo.prdata <= rdata;

    txdone <= r.txdone;
    
    rdready <= r.dready;

  end process;

  process(clk)
  begin
    if rising_edge(clk) then
      r <= rin;
    end if;
  end process;

-- debug output

  dbg <= r.rhold(7 downto 0) ;
  
end;

configuration cfg_uart_rtl of uart is
  for rtl
  end for;
end cfg_uart_rtl;

-- FIXME:   apbo.prdata <= rdata;



  
