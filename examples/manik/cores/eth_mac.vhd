-------------------------------------------------------------------------------
-- Title      : Easy Ethernet MAC 10BaseT - Full Duplex
-- Project    : MANIK-II
-------------------------------------------------------------------------------
-- File       : eth_mac.vhd
-- Author     : Sandeep Dutta
-- Company    : NkTech Inc
-- Created    : 2006-03-11
-- Last update: 2006-10-11
-- Platform   : 
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: Easy Ethernet Mac, is a small footprint ethernet MAC, with
--              2KB Input & 2KB output buffer (built-in)
-------------------------------------------------------------------------------
-- Copyright (c) 2006 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2006-03-11  1.0      Sandeep	Created
-------------------------------------------------------------------------------

-----------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;

package eth_package is

    -- polynomial: (0 1 2 4 5 7 8 10 11 12 16 22 23 26 32)
    -- data width: 4
    -- convention: the first serial data bit is D(3)
    function nextCRC (Data:  std_logic_vector(3 downto 0);
                      CRC :  std_logic_vector(31 downto 0);
                      Enb : std_logic) return std_logic_vector;

end eth_package;

library IEEE;
use IEEE.std_logic_1164.all;

package body eth_package is
    -- polynomial: (0 1 2 4 5 7 8 10 11 12 16 22 23 26 32)
    -- data width: 4
    -- convention: the first serial data bit is D(3)
    function nextCRC (Data : std_logic_vector(3 downto 0);
                      CRC  : std_logic_vector(31 downto 0);
                      Enb  : std_logic) return std_logic_vector is

        variable NewCRC : std_logic_vector(31 downto 0);

    begin

        NewCRC(0) := Enb and (Data(0) xor CRC(28)); 
        NewCRC(1) := Enb and (Data(1) xor Data(0) xor CRC(28) xor CRC(29)); 
        NewCRC(2) := Enb and (Data(2) xor Data(1) xor Data(0) xor CRC(28) xor CRC(29) xor CRC(30)); 
        NewCRC(3) := Enb and (Data(3) xor Data(2) xor Data(1) xor CRC(29) xor CRC(30) xor CRC(31)); 
        NewCRC(4) := (Enb and (Data(3) xor Data(2) xor Data(0) xor CRC(28) xor CRC(30) xor CRC(31))) xor CRC(0); 
        NewCRC(5) := (Enb and (Data(3) xor Data(1) xor Data(0) xor CRC(28) xor CRC(29) xor CRC(31))) xor CRC(1); 
        NewCRC(6) := (Enb and (Data(2) xor Data(1) xor CRC(29) xor CRC(30))) xor CRC( 2); 
        NewCRC(7) := (Enb and (Data(3) xor Data(2) xor Data(0) xor CRC(28) xor CRC(30) xor CRC(31))) xor CRC(3); 
        NewCRC(8) := (Enb and (Data(3) xor Data(1) xor Data(0) xor CRC(28) xor CRC(29) xor CRC(31))) xor CRC(4); 
        NewCRC(9) := (Enb and (Data(2) xor Data(1) xor CRC(29) xor CRC(30))) xor CRC(5); 
        NewCRC(10) := (Enb and (Data(3) xor Data(2) xor Data(0) xor CRC(28) xor CRC(30) xor CRC(31))) xor CRC(6); 
        NewCRC(11) := (Enb and (Data(3) xor Data(1) xor Data(0) xor CRC(28) xor CRC(29) xor CRC(31))) xor CRC(7); 
        NewCRC(12) := (Enb and (Data(2) xor Data(1) xor Data(0) xor CRC(28) xor CRC(29) xor CRC(30))) xor CRC(8); 
        NewCRC(13) := (Enb and (Data(3) xor Data(2) xor Data(1) xor CRC(29) xor CRC(30) xor CRC(31))) xor CRC(9); 
        NewCRC(14) := (Enb and (Data(3) xor Data(2) xor CRC(30) xor CRC(31))) xor CRC(10); 
        NewCRC(15) := (Enb and (Data(3) xor CRC(31))) xor CRC(11); 
        NewCRC(16) := (Enb and (Data(0) xor CRC(28))) xor CRC(12); 
        NewCRC(17) := (Enb and (Data(1) xor CRC(29))) xor CRC(13); 
        NewCRC(18) := (Enb and (Data(2) xor CRC(30))) xor CRC(14); 
        NewCRC(19) := (Enb and (Data(3) xor CRC(31))) xor CRC(15); 
        NewCRC(20) := CRC(16); 
        NewCRC(21) := CRC(17); 
        NewCRC(22) := (Enb and (Data(0) xor CRC(28))) xor CRC(18); 
        NewCRC(23) := (Enb and (Data(1) xor Data(0) xor CRC(29) xor CRC(28))) xor CRC(19); 
        NewCRC(24) := (Enb and (Data(2) xor Data(1) xor CRC(30) xor CRC(29))) xor CRC(20); 
        NewCRC(25) := (Enb and (Data(3) xor Data(2) xor CRC(31) xor CRC(30))) xor CRC(21); 
        NewCRC(26) := (Enb and (Data(3) xor Data(0) xor CRC(31) xor CRC(28))) xor CRC(22); 
        NewCRC(27) := (Enb and (Data(1) xor CRC(29))) xor CRC(23); 
        NewCRC(28) := (Enb and (Data(2) xor CRC(30))) xor CRC(24); 
        NewCRC(29) := (Enb and (Data(3) xor CRC(31))) xor CRC(25); 
        NewCRC(30) := CRC(26); 
        NewCRC(31) := CRC(27); 

        return NewCRC;

    end nextCRC;

end eth_package;

-------------------------------------------------------------------------------
--    eth_dpram - generate dual ported. Used for buffering data             ---
-------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;

entity eth_dpram is
    
    generic (ETH_ADDR_WIDTH : integer := 11;
             ETH_DATA_WIDTH : integer := 8);

    port (raddr    : in  std_logic_vector (ETH_ADDR_WIDTH-1 downto 0);
          renb	   : in  std_logic;
          rclk     : in  std_logic;
          data_out : out std_logic_vector (ETH_DATA_WIDTH-1 downto 0);
          waddr    : in  std_logic_vector (ETH_ADDR_WIDTH-1 downto 0);
          wenb     : in  std_logic;
          wclk     : in  std_logic;
          data_in  : in  std_logic_vector (ETH_DATA_WIDTH-1 downto 0));
end eth_dpram;

architecture rtl of eth_dpram is
    type mem_type is array ((2**ETH_ADDR_WIDTH)-1 downto 0) of std_logic_vector (ETH_DATA_WIDTH-1 downto 0);

    signal memory : mem_type;

    signal raddr_reg : std_logic_vector (ETH_ADDR_WIDTH-1 downto 0) := (others => '0');
begin  -- rtl

    -- note:- some synthesis tools will
    -- ignore the renb 
    process (rclk)
    begin
        if rising_edge(rclk) then
            if renb = '1' then
                raddr_reg <= raddr;
            end if;
        end if;
    end process ;

    data_out <= memory(conv_integer(raddr_reg));

    process (wclk)
    begin
        if rising_edge(wclk) then
            if wenb = '1' then
                memory(conv_integer(waddr)) <= data_in;
            end if;
        end if;
    end process ;

end rtl;

-------------------------------------------------------------------------------
--    eth_sram - generate  sync ram. Used for buffering data                ---
-------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;
use work.manikaltera.all;
use work.manikactel.all;

entity eth_sram is
    generic (ETH_ADDR_WIDTH : integer := 11;
             ETH_DATA_WIDTH : integer := 8);
    port (addr     : in  std_logic_vector (ETH_ADDR_WIDTH-1 downto 0);  -- address
          data_in  : in  std_logic_vector (ETH_DATA_WIDTH-1 downto 0);
          data_out : out std_logic_vector (ETH_DATA_WIDTH-1 downto 0);
          we       : in  std_logic;
          clk      : in  std_logic;
          reset    : in  std_logic);

end eth_sram;

architecture rtl of eth_sram is
    constant DEPTH : integer := 2**ETH_ADDR_WIDTH;
begin  -- rtl

    generic_tech: if Technology /= "XILINX" generate
        type ram_type is array (DEPTH-1 downto 0) of std_logic_vector (ETH_DATA_WIDTH-1 downto 0);
        signal MEM_ARRAY : ram_type := (others => ALLZEROS(ETH_DATA_WIDTH-1 downto 0));
        signal raddr_reg : std_logic_vector(ETH_ADDR_WIDTH-1 downto 0) := (others => '0');        
    begin        
        process (clk)
        begin
            if rising_edge(clk) then
                if we = '1' then
                    MEM_ARRAY(conv_integer(addr)) <= data_in;
                end if;
            end if;
        end process;

        process (clk)
        begin
            if rising_edge(clk) then
                raddr_reg <= addr;
            end if;
        end process ;
            
        data_out <= MEM_ARRAY(conv_integer(raddr_reg));
    end generate generic_tech;

    xilinx_tech: if Technology = "XILINX" generate
        virtex2_mem: if FPGA_Family = "Virtex2" generate            
            eth_mem : xilspram_v2
                generic map (MEM_DATA_WIDTH => ETH_DATA_WIDTH,
                             MEM_ADDR_WIDTH => ETH_ADDR_WIDTH)
                port map (clk    => clk,
                          addr   => addr,
                          data_i => data_in,
                          enb    => '1',
                          rst    => reset,
                          we     => we,
                          data_o => data_out);
        end generate virtex2_mem;
        virtex_mem: if FPGA_Family = "Virtex" generate            
            eth_mem : xilspram
                generic map (MEM_DATA_WIDTH => ETH_DATA_WIDTH,
                             MEM_ADDR_WIDTH => ETH_ADDR_WIDTH)
                port map (clk    => clk,
                          addr   => addr,
                          data_i => data_in,
                          enb    => '1',
                          rst    => reset,
                          we     => we,
                          data_o => data_out);
        end generate virtex_mem;
    end generate xilinx_tech;

end rtl;

-------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;

entity macaddress is
    generic (DEFAULT_MAC_ADDR : std_logic_vector (47 downto 0) := (others => '0'));
    port (clk        : in  std_logic;
          reset      : in  std_logic;
          rst_ptr    : in  std_logic;
          wmaddr     : in  std_logic;
          rmaddr     : in  std_logic;
          maddr_in   : in  std_logic_vector (7 downto 0);
          maddr_out  : out std_logic_vector (7 downto 0);
          maddr_full : out std_logic_vector (47 downto 0));
end macaddress;

architecture rtl of macaddress is
    signal maddress : std_logic_vector (47 downto 0) := DEFAULT_MAC_ADDR;

    signal mptr     : std_logic_vector (2 downto 0) := "000";
    signal mptr_int : integer range 0 to 6          := 0;
    
begin  -- rtl

    process (clk, reset, rst_ptr)
    begin
        if reset = '1' or rst_ptr = '1' then
            mptr <= "000";
        elsif rising_edge(clk) then
            if wmaddr = '1' or rmaddr = '1' then
                mptr <= mptr + 1;
            end if;
        end if;
    end process ;

    mptr_int <= conv_integer(mptr);

    process (clk)
    begin
        if rising_edge(clk) then
            if wmaddr = '1' then
                case mptr_int is
                    when 5 => maddress(47 downto 40) <= maddr_in;
                    when 4 => maddress(39 downto 32) <= maddr_in;
                    when 3 => maddress(31 downto 24) <= maddr_in;
                    when 2 => maddress(23 downto 16) <= maddr_in;
                    when 1 => maddress(15 downto  8) <= maddr_in;
                    when others => maddress( 7 downto  0) <= maddr_in;
                end case;                
            end if;
        end if;
    end process ;
        
    maddr_full <= maddress;

    process (mptr_int, maddress)
    begin
        case mptr_int is
            when 5 => maddr_out <= maddress(47 downto 40);
            when 4 => maddr_out <= maddress(39 downto 32);
            when 3 => maddr_out <= maddress(31 downto 24);
            when 2 => maddr_out <= maddress(23 downto 16);
            when 1 => maddr_out <= maddress(15 downto  8);
            when others => maddr_out <= maddress( 7 downto  0);
        end case;                
    end process ;
end rtl;

-------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.eth_package.all;

entity eth_send is
  
    generic (ETH_ADDR_WIDTH : integer := 11);

    port (clk         : in  std_logic;
          reset       : in  std_logic;
          waddr       : in  std_logic_vector (ETH_ADDR_WIDTH-1 downto 0);
          xmit_start  : in  std_logic;
          dupl_mode   : in  std_logic;
          xmit_data   : in  std_logic_vector ( 7 downto 0);
          maddr_full  : in  std_logic_vector (47 downto 0);
          waddr_next  : out std_logic;
          waddr_reset : out std_logic;
          xmit_err    : out std_logic;
          xmit_busy   : out std_logic;

          -- phy interface
          phy_tx_clk : in    std_logic;
          phy_rx_col : in    std_logic;
          phy_rx_crs : in    std_logic;
          phy_txd    : out   std_logic_vector (3 downto 0);
          phy_tx_en  : out   std_logic;
          phy_tx_er  : out   std_logic);
    
end eth_send;

architecture rtl of eth_send is

    signal curr_crc, new_crc : std_logic_vector (31 downto 0) := (others => '1');
    signal crc_reset, crc_shift : std_logic := '0';
    
    signal rev_nibl : std_logic_vector (3 downto 0) := (others => '0');
    signal nibl_oreg, nibl_ocomb : std_logic_vector (3 downto 0) := (others => '0');
    
    signal coldet_comb, coldet_reg : std_logic := '0';
    signal txen_comb, txen_reg     : std_logic := '0';
    signal rstwa_comb, rstwa_reg   : std_logic := '0';
    signal xbusy_comb, xbusy_reg   : std_logic := '0';
    signal nbyte_comb, nbyte_reg   : std_logic := '0';
    signal nnibl_comb, nnibl_reg   : std_logic := '0';
    
    signal bcnt_reg             : std_logic_vector (ETH_ADDR_WIDTH-1 downto 0) := (others => '0');
    signal bcnt_int             : integer                                      := 0;
    signal bcnt_inc, bcnt_reset : std_logic                                    := '0';
    
    signal xmit_len  : std_logic_vector (ETH_ADDR_WIDTH-1 downto 0) := (others => '0');
    signal xmit_pend, xmit_done : std_logic := '0';
    
    signal tclk0, tclk1, tclk2 : std_logic := '0';
    signal rcol0, rcol1        : std_logic := '0';
    signal rcrs0, rcrs1        : std_logic := '0';
    
    signal collision : std_logic := '0';
    signal tck_fedge, tck_redge : std_logic := '0';
    
    type send_states is (send_idle,
                         send_jam, send_jredge,
                         send_preamble, send_preamble_wre,
                         send_crc, send_credge, send_data,
                         send_lnib, send_lredge, send_hnib, send_hredge,
                         send_defer, send_ifdelay, send_ifdelay_wre);
    signal send_cstate, send_nstate : send_states := send_idle;
    
begin  -- rtl
    -- out signals
    phy_txd      <= nibl_oreg;
    phy_tx_en    <= txen_reg;
    phy_tx_er	 <= '0';
    waddr_next   <= nbyte_reg;
    waddr_reset  <= rstwa_reg;
    xmit_err     <= '0';
    xmit_busy    <= xmit_pend;
    --
    -- latch length and xmit start
    --
    process (clk, reset)
    begin
        if reset = '1' then
            xmit_pend <= '0';
            xmit_len  <= (others => '0');
        elsif rising_edge(clk) then
            if xmit_done = '1' then
                xmit_pend <= '0';
                xmit_len <= (others => '0');
            elsif xmit_pend = '0' then
                xmit_pend <= xmit_start;
                xmit_len  <= waddr - 1;
            end if;
        end if;
    end process;
    --
    -- synchronize phy signals
    --
    process (clk, reset)
    begin
        if reset = '1' then
            tclk0 <= '0';
            tclk1 <= '0';
            tclk2 <= '0';
            rcol0 <= '0';
            rcol1 <= '0';
            rcrs0 <= '0';
            rcrs1 <= '0';
        elsif rising_edge(clk) then
            tclk0 <= phy_tx_clk;
            tclk1 <= tclk0;
            tclk2 <= tclk1;
            rcol0 <= phy_rx_col;
            rcol1 <= rcol0;
            rcrs0 <= phy_rx_crs;
            rcrs1 <= rcrs0;
        end if;
    end process ;

    --
    -- detect collisions
    --
    process (clk, reset)
    begin
        if reset = '1' then
            collision <= '0';
        elsif rising_edge(clk) then
            if coldet_reg = '1' then
                collision <= rcol1;
            end if;
        end if;
    end process;
            
    --
    -- crc process
    --
    rev_nibl (0) <= nibl_oreg(3) when nnibl_reg = '1' else '0';
    rev_nibl (1) <= nibl_oreg(2) when nnibl_reg = '1' else '0';
    rev_nibl (2) <= nibl_oreg(1) when nnibl_reg = '1' else '0';
    rev_nibl (3) <= nibl_oreg(0) when nnibl_reg = '1' else '0';

    new_crc <= nextCRC(rev_nibl,curr_crc,nnibl_reg);
    process (clk)
    begin
        if rising_edge(clk) then
            if reset = '1' or crc_reset = '1' then
                curr_crc <= (others => '1');
            elsif nnibl_reg = '1' or crc_shift = '1' then  -- crc updated when we get a new nibble
                curr_crc <= new_crc;
            end if;
        end if;
    end process ;

    process (clk, reset)
    begin
        if reset = '1' then
            bcnt_reg <= (others => '0');
        elsif rising_edge(clk) then
            if bcnt_reset = '1' then
                bcnt_reg <= (others => '0');
            elsif bcnt_inc = '1' then
                bcnt_reg <= bcnt_reg + 1;                
            end if;
        end if;
    end process ;
    
    bcnt_int  <= conv_integer(bcnt_reg);
    tck_fedge <= '1' when tclk1 = '0' and tclk2 = '1' else '0';
    tck_redge <= '1' when tclk1 = '1' and tclk2 = '0' else '0';
    
    --
    -- the xmit state machine
    --
    process (clk, reset)
    begin
        if reset = '1' then
            send_cstate <= send_idle;
            nibl_oreg   <= (others => '0');
            coldet_reg  <= '0';
            txen_reg    <= '0';
            rstwa_reg	<= '0';
            xbusy_reg	<= '0';
            nbyte_reg   <= '0';
            nnibl_reg   <= '0';
        elsif rising_edge(clk) then
            send_cstate <= send_nstate;
            nibl_oreg   <= nibl_ocomb;
            coldet_reg  <= coldet_comb;
            txen_reg    <= txen_comb;
            rstwa_reg   <= rstwa_comb;
            xbusy_reg   <= xbusy_comb;
            nbyte_reg   <= nbyte_comb;
            nnibl_reg	<= nnibl_comb;
        end if;
    end process ;

    process (send_cstate, bcnt_reg, bcnt_int, nibl_oreg, tclk1, tclk2,
             rcrs1, coldet_reg, txen_reg, xmit_pend, xbusy_reg, collision, 
			 tck_redge, tck_fedge, maddr_full, xmit_data, xmit_len, curr_crc)
    begin
        
        send_nstate <= send_cstate;
        nibl_ocomb  <= nibl_oreg;
        coldet_comb <= coldet_reg;
        txen_comb   <= txen_reg;
        rstwa_comb  <= '0';
        xbusy_comb  <= xbusy_reg;
        nbyte_comb  <= '0';
        nnibl_comb  <= '0';
        bcnt_reset  <= '0';
        bcnt_inc    <= '0';
        crc_reset   <= '0';
        xmit_done   <= '0';
        crc_shift   <= '0';
        
        case send_cstate is
            when send_idle =>
                xbusy_comb <= '0';
                -- if crs then defer
                if rcrs1 = '1' then
                    send_nstate <= send_defer;
                -- if we have data to xmit
                elsif xmit_pend = '1' then
                    bcnt_reset  <= '1';
                    send_nstate <= send_preamble;
                    xbusy_comb  <= '1';
                    coldet_comb <= '1';  -- enable collision detection
                end if;

            when send_preamble =>
                -- send the preamble and sfd
                if tclk1 = '0' and tclk2 = '1' then  -- on the falling edge
                    txen_comb <= '1';
                    if bcnt_int = 15 then
                        nibl_ocomb  <= x"d";
                        bcnt_reset  <= '1';
                        rstwa_comb  <= '1';
                        crc_reset   <= '1';
                        send_nstate <= send_data;
                    else
                        nibl_ocomb  <= x"5";
                        bcnt_inc    <= '1';
                        send_nstate <= send_preamble_wre;
                    end if;
                end if;

            when send_data =>
                coldet_comb <= '0';     -- past the collision point
                -- if collision was detected then defer
                if collision = '1' then
                    bcnt_reset  <= '1';
                    send_nstate <= send_jam;
                else
                    send_nstate <= send_lnib;
                end if;

            when send_preamble_wre =>
                -- wait for rising_edge
                if tck_redge = '1' then  -- on rising edge
                    send_nstate <= send_preamble ;
                end if;

            when send_lnib =>
                if tck_fedge = '1' then  -- on the falling edge
                    nnibl_comb  <= '1';
                    -- patch in source mac address (lower nibbles)
                    case bcnt_int is
                        when 6  => nibl_ocomb <= maddr_full(43 downto 40);  --1st
                        when 7  => nibl_ocomb <= maddr_full(35 downto 32);  --2nd
                        when 8  => nibl_ocomb <= maddr_full(27 downto 24);  --3rd
                        when 9  => nibl_ocomb <= maddr_full(19 downto 16);  --4th
                        when 10 => nibl_ocomb <= maddr_full(11 downto  8);  --5th
                        when 11 => nibl_ocomb <= maddr_full( 3 downto  0);  --6th
                        when others => nibl_ocomb <= xmit_data (3 downto 0);
                    end case;
                    
                    send_nstate <= send_lredge;
                end if;

            when send_lredge =>
                -- wait for rising_edge
                if tck_redge = '1' then  -- on rising edge
                    send_nstate <= send_hnib ;
                end if;

            when send_hnib =>
                if tck_fedge = '1' then  -- on the falling edge
                    nnibl_comb  <= '1';
                    -- patch in source mac address (higher nibbles)
                    case bcnt_int is
                        when 6  => nibl_ocomb <= maddr_full(47 downto 44);  --1st
                        when 7  => nibl_ocomb <= maddr_full(39 downto 36);  --2nd
                        when 8  => nibl_ocomb <= maddr_full(31 downto 28);  --3rd
                        when 9  => nibl_ocomb <= maddr_full(23 downto 20);  --4th
                        when 10 => nibl_ocomb <= maddr_full(15 downto 12);  --5th
                        when 11 => nibl_ocomb <= maddr_full( 7 downto  4);  --6th
                        when others => nibl_ocomb <= xmit_data (7 downto 4);
                    end case;
                    send_nstate <= send_hredge;
                end if;

            when send_hredge =>
                -- wait for rising_edge
                if tck_redge = '1' then  -- on rising edge
                    nbyte_comb <= '1';
                    if bcnt_reg = xmit_len then      -- complete
                        bcnt_reset  <= '1';
                        send_nstate <= send_crc;
                    else
                        bcnt_inc    <= '1';
                        send_nstate <= send_lnib;
                    end if;
                end if;

            when send_crc =>
                if tck_fedge = '1' then  -- on the falling edge                
                    send_nstate <= send_credge;
                    bcnt_inc    <= '1';
                    -- send the crc nibbles
                    nibl_ocomb <= not (curr_crc(28) & curr_crc(29) & curr_crc(30) & curr_crc(31));
                end if;

            when send_credge =>
                if tck_redge = '1' then  -- on rising edge
                    if bcnt_reg /= 8 then
                        crc_shift   <= '1';
                        send_nstate <= send_crc;
                    else
                        xbusy_comb  <= '0';
                        xmit_done   <= '1';
                        send_nstate <= send_defer;
                    end if;
                end if;
                
            when send_defer =>
                -- wait for crs to go down
                txen_comb <= '0';
                if rcrs1 = '0' then
                    bcnt_reset  <= '1';
                    send_nstate <= send_ifdelay;
                end if;

            when send_ifdelay =>
                -- wait for interframe delay of 96 bits or 24 nibbles
                if tck_redge = '1' then  -- on rising edge
                    if bcnt_int = 24 then
                        bcnt_reset  <= '1';
                        send_nstate <= send_idle;
                    else
                        bcnt_inc    <= '1';
                        send_nstate <= send_ifdelay_wre;
                    end if;
                end if;

            when send_ifdelay_wre =>
                -- wait for rising_edge
                if tck_redge = '1' then  -- on rising edge
                    send_nstate <= send_ifdelay;
                end if;

            when send_jam =>
                -- wait for falling edge
                if tck_fedge = '1' then
                    bcnt_inc    <= '1';
                    nibl_ocomb  <= x"9";
                    send_nstate <= send_jredge;
                end if;

            when send_jredge =>
                -- rising edge
                if tck_redge = '1' then
                    if bcnt_int = 3 then
                        bcnt_reset  <= '1';
                        send_nstate <= send_defer;
                    else
                        send_nstate <= send_jam;
                    end if;
                end if;
            when others => null;
        end case;
    end process ;
end rtl;

-------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.eth_package.all;

entity eth_recv is
    generic (ETH_ADDR_WIDTH : integer := 11);
    port (clk            : in  std_logic;
          reset          : in  std_logic;
          raddr		 : in  std_logic_vector (ETH_ADDR_WIDTH-1 downto 0);
          maddr_full     : in  std_logic_vector (47 downto 0);
          recv_enb 	 : in  std_logic;
          mread_done     : in  std_logic;
          prom_mode	 : in  std_logic;
          byte_complete  : out std_logic;
          frame_begin	 : out std_logic;
          frame_complete : out std_logic;
          recv_ip	 : out std_logic;
          frame_inv      : out std_logic;
          recv_byte      : out std_logic_vector (7 downto 0);
          recv_len	 : out std_logic_vector (ETH_ADDR_WIDTH-1 downto 0);
          rst_raddr	 : out std_logic;
          crc_error      : out std_logic;
          
          -- phy interface signals
          phy_rxd    : in std_logic_vector (3 downto 0);
          phy_rx_dv  : in std_logic;
          phy_rx_clk : in std_logic);
end eth_recv;

architecture rtl of eth_recv is
    constant maddr_bcast : std_logic_vector (47 downto 0) := (others => '1');
    
    signal rclk0, rclk1, rclk2 : std_logic                     := '0';
    signal rd0, rd1            : std_logic_vector (3 downto 0) := "0000";
    signal rdv0, rdv1          : std_logic                     := '0';

    type recv_states is (recv_idle, recv_lnib, recv_hnib, recv_fedge,
                         recv_bdone, recv_fdone, recv_invalid, recv_sfd_wait);
    
    signal recv_cstate, recv_nstate : recv_states := recv_idle;    
    
    signal dest_mac   : std_logic_vector (47 downto 0) := (others => '0');
    signal mac_valid  : std_logic := '0';

    signal curr_crc, new_crc : std_logic_vector (31 downto 0) := (others => '0');
    signal crc_reset         : std_logic                      := '0';
    signal rev_nibl          : std_logic_vector (3 downto 0)  := (others => '0');
    
    -- used in state machine
    signal sfd_comb, sfd_reg         : std_logic := '0';
    signal umac_comb, umac_reg       : std_logic := '0';
    signal rmac_comb, rmac_reg       : std_logic := '0';
    signal bdone_comb, bdone_reg     : std_logic := '0';
    signal fdone_comb, fdone_reg     : std_logic := '0';
    signal framei_comb, framei_reg   : std_logic := '0';
    signal recvip_comb, recvip_reg   : std_logic := '0';
    signal fbegin_comb, fbegin_reg   : std_logic := '0';
    signal rstaddr_comb, rstaddr_reg : std_logic := '0';
    signal nnibl_comb, nnibl_reg     : std_logic := '0';
    
    signal recv_len_comb, recv_len_reg : std_logic_vector (ETH_ADDR_WIDTH-1 downto 0) := (others => '0');
    signal byte_comb, byte_reg         : std_logic_vector (7 downto 0)                := (others => '0');
    
    signal raddr_int : integer := 0;
begin  -- rtl

    --
    -- synchronize incoming signals from the phy
    --
    process (clk, reset)
    begin
        if reset = '1' then
            rclk0 <= '0';
            rclk1 <= '0';
            rclk2 <= '0';
            rd0   <= "0000";
            rd1   <= "0000";
            rdv0  <= '0';
            rdv1  <= '0';
        elsif rising_edge(clk) then
            rclk0 <= phy_rx_clk;
            rclk1 <= rclk0;
            rclk2 <= rclk1;
            rd0   <= phy_rxd;
            rd1   <= rd0;
            rdv0  <= phy_rx_dv;
            rdv1  <= rdv0;
        end if;        
    end process;

    raddr_int      <= conv_integer(raddr);
    recv_byte      <= byte_reg;
    recv_ip        <= recvip_reg;
    byte_complete  <= bdone_reg;
    frame_begin    <= fbegin_reg;
    frame_complete <= fdone_reg;
    frame_inv      <= framei_reg;
    recv_len       <= recv_len_reg;
    rst_raddr	   <= rstaddr_reg;
    
    process (clk)
    begin
        if rising_edge(clk) then
            if rmac_reg = '1' or reset = '1' then
                dest_mac <= (others => '0');
            elsif umac_reg = '1' then
                dest_mac <= dest_mac(39 downto 0) & byte_reg;
            end if;
        end if;
    end process ;

    --
    -- mac address if valid if promiscuous mode, or exact match or broadcast frame
    --
    mac_valid <= '1' when prom_mode = '1' or dest_mac = maddr_full or dest_mac = maddr_bcast else '0';

    --
    -- crc
    --
    rev_nibl(3) <= rd1(0);
    rev_nibl(2) <= rd1(1);
    rev_nibl(1) <= rd1(2);
    rev_nibl(0) <= rd1(3);
    
    new_crc <= nextCRC(rev_nibl,curr_crc,nnibl_comb);
    process (clk)
    begin
        if rising_edge(clk) then
            if reset = '1' or crc_reset = '1' then
                curr_crc <= (others => '1');
            elsif nnibl_comb = '1'  and rdv1 = '1' then
                curr_crc <= new_crc;
            end if;
        end if;
    end process ;

    --  CRC not equal to magic number
    crc_error <= '1' when curr_crc /= x"c704dd7b" else '0';

    --
    -- receive statemachine
    --
    process (clk, reset)
    begin
        if reset = '1' then
            recv_cstate  <= recv_idle;
            sfd_reg      <= '0';
            umac_reg     <= '0';
            rmac_reg     <= '0';
            byte_reg     <= (others => '0');
            bdone_reg    <= '0';
            framei_reg   <= '0';
            fdone_reg    <= '0';
            recvip_reg   <= '0';
            fbegin_reg   <= '0';
            recv_len_reg <= (others => '0');
            rstaddr_reg  <= '0';
        elsif rising_edge(clk) then
            recv_cstate  <= recv_nstate;
            sfd_reg      <= sfd_comb;
            umac_reg     <= umac_comb;
            rmac_reg     <= rmac_comb;
            byte_reg     <= byte_comb;
            bdone_reg    <= bdone_comb;
            framei_reg   <= framei_comb;
            fdone_reg    <= fdone_comb;
            recvip_reg   <= recvip_comb;
            fbegin_reg   <= fbegin_comb;
            recv_len_reg <= recv_len_comb;
            rstaddr_reg  <= rstaddr_comb;
        end if;
    end process;

    --
    -- combinatorial part
    --
    process (rclk1, rclk2, rd1, rdv1, recv_cstate, sfd_reg, byte_reg, recv_len_reg, recvip_reg,
             recv_enb, raddr, raddr_int, mac_valid, mread_done)
    begin

        recv_nstate   <= recv_cstate;
        sfd_comb      <= sfd_reg;
        umac_comb     <= '0';
        rmac_comb     <= '0';
        byte_comb     <= byte_reg;
        bdone_comb    <= '0';
        framei_comb   <= '0';
        fdone_comb    <= '0';
        recvip_comb   <= recvip_reg;
        fbegin_comb   <= '0';
        recv_len_comb <= recv_len_reg;
        rstaddr_comb  <= '0';
        nnibl_comb    <= '0';
        crc_reset     <= '0';
        
        case recv_cstate is
            when recv_idle =>
                -- receive data valid
                if rdv1 = '1' then
                    if recv_enb = '1' then
                        -- start receiving nibbles
                        recv_nstate <= recv_sfd_wait;
                        recvip_comb <= '1';
                        fbegin_comb <= '1';
                    else
                        recv_nstate <= recv_invalid;
                    end if;
                else
                    sfd_comb <= '0';
                end if;

            when recv_sfd_wait =>
                -- wait for the SFD
                if rclk1 = '1' and rclk2 = '0' then
                    if rdv1 = '0' then
                        recv_nstate <= recv_idle;
                    elsif rd1 = x"5" then
                        recv_nstate <= recv_sfd_wait;
                    elsif rd1 = x"d" then
                        rmac_comb <= '1';          -- reset the mac address
                        sfd_comb  <= '1';
                        crc_reset <= '1';
                        recv_nstate <= recv_lnib;
                    else
                        recv_nstate <= recv_invalid;
                    end if;
                end if;
                
            when recv_lnib =>
                -- capture low nibble on rising edge
                if rclk1 = '1' and rclk2 = '0' then
                    recv_nstate           <= recv_fedge;
                    nnibl_comb            <= '1';
                    byte_comb(3 downto 0) <= rd1;
                end if;

            when recv_fedge =>
                -- wait for falling edge
                if rclk1 = '0' and rclk2 = '1' then
                    -- if dv goes down then receive complete
                    -- else wait for next rising edge
                    if rdv1 = '0' then
                        recv_len_comb <= raddr;
                        rstaddr_comb  <= '1';
                        recv_nstate   <= recv_fdone;
                    else
                        recv_nstate <= recv_hnib;
                    end if;
                end if;

            when recv_hnib =>
                -- capture high nibble on rising edge
                if rclk1 = '1' and rclk2 = '0' then
                    recv_nstate            <= recv_bdone;
                    nnibl_comb             <= '1';
                    byte_comb (7 downto 4) <= rd1;
                end if;

            when recv_bdone =>
                bdone_comb <= '1';
                -- SFD done : Check mac
                if raddr_int < 6 then
                    umac_comb   <= '1';
                    recv_nstate <= recv_lnib;  -- continue receiving
                elsif mac_valid = '0' then     -- not for us
                    recv_nstate <= recv_invalid;
                else
                    recv_nstate <= recv_lnib;  -- continue receiving
                end if;

            when recv_invalid =>
                -- wait for frame to end
                if rdv1 = '0' then
                    -- restart
                    sfd_comb    <= '0';
                    framei_comb <= '1';
                    recvip_comb <= '0';
                    recv_nstate <= recv_idle;
                end if;

            when recv_fdone =>
                fdone_comb  <= '1';
                recvip_comb <= '0';
                -- frame received completely, wait till host
                -- has finished reading data from memory
                if mread_done = '1' then
                    -- restart
                    sfd_comb    <= '0';
                    recv_nstate <= recv_idle;
                end if;
            when others => null;
        end case;
    end process;
    
end rtl;

-------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;

entity eth_mac is
    
    generic (ETH_HALF_DUPLEX  : boolean  := true;
             ETH_ADDR_WIDTH   : integer  := 11;
             ADDR_WIDTH	      : integer  := 32;
             WIDTH            : integer  := 32;
             DEFAULT_MAC_ADDR : std_logic_vector (47 downto 0) := (others => '0'));
    
    port (reset : in std_logic;
          clk   : in std_logic;

          -- Wishbone slave interface
          WBS_ADR_I : in  std_logic_vector (ADDR_WIDTH-1 downto 0);
          WBS_SEL_I : in  std_logic_vector (3 downto 0);
          WBS_DAT_I : in  std_logic_vector (WIDTH-1 downto 0);
          WBS_WE_I  : in  std_logic;
          WBS_STB_I : in  std_logic;
          WBS_CYC_I : in  std_logic;
          WBS_CTI_I : in  std_logic_vector (2 downto 0);
          WBS_BTE_I : in  std_logic_vector (1 downto 0);
          WBS_DAT_O : out std_logic_vector (WIDTH-1 downto 0);
          WBS_ACK_O : out std_logic;
          WBS_ERR_O : out std_logic;
          eth_intr  : out std_logic;

          -- PHY Interface signals
          phy_resetn : out   std_logic;
          phy_mdio   : inout std_logic;
          phy_tx_clk : in    std_logic;
          phy_rx_clk : in    std_logic;
          phy_rxd    : in    std_logic_vector(3 downto 0);
          phy_rx_dv  : in    std_logic;
          phy_rx_er  : in    std_logic;
          phy_rx_col : in    std_logic;
          phy_rx_crs : in    std_logic;
          phy_txd    : out   std_logic_vector(3 downto 0);
          phy_tx_en  : out   std_logic;
          phy_tx_er  : out   std_logic;
          phy_mdc    : out   std_logic);

end eth_mac;

architecture rtl of eth_mac is

    component eth_sram
        generic (ETH_ADDR_WIDTH : integer;
                 ETH_DATA_WIDTH : integer);
        port (addr     : in  std_logic_vector (ETH_ADDR_WIDTH-1 downto 0);
              data_in  : in  std_logic_vector (ETH_DATA_WIDTH-1 downto 0);
              data_out : out std_logic_vector (ETH_DATA_WIDTH-1 downto 0);
              we       : in  std_logic;
              clk      : in  std_logic;
              reset    : in  std_logic);
    end component;
    
    component macaddress
        generic (DEFAULT_MAC_ADDR : std_logic_vector (47 downto 0));
        port (clk        : in  std_logic;
              reset      : in  std_logic;
              rst_ptr    : in  std_logic;
              wmaddr     : in  std_logic;
              rmaddr     : in  std_logic;
              maddr_in   : in  std_logic_vector (7 downto 0);
              maddr_out  : out std_logic_vector (7 downto 0);
              maddr_full : out std_logic_vector (47 downto 0));
    end component;
    
    component eth_recv
        generic (ETH_ADDR_WIDTH : integer);
        port (clk            : in  std_logic;
              reset          : in  std_logic;
              raddr          : in  std_logic_vector (ETH_ADDR_WIDTH-1 downto 0);
              maddr_full     : in  std_logic_vector (47 downto 0);
              recv_enb	     : in  std_logic;
              mread_done     : in  std_logic;
              prom_mode      : in  std_logic;
              byte_complete  : out std_logic;
              frame_begin    : out std_logic;
              frame_complete : out std_logic;
              recv_ip        : out std_logic;
              frame_inv      : out std_logic;
              recv_byte      : out std_logic_vector (7 downto 0);
              recv_len       : out std_logic_vector (ETH_ADDR_WIDTH-1 downto 0);
              rst_raddr      : out std_logic;
              crc_error	     : out std_logic;
              phy_rxd        : in  std_logic_vector (3 downto 0);
              phy_rx_dv      : in  std_logic;
              phy_rx_clk     : in  std_logic);
    end component;

    component eth_send
        generic (ETH_ADDR_WIDTH : integer);
        port (clk         : in  std_logic;
              reset       : in  std_logic;
              waddr       : in  std_logic_vector (ETH_ADDR_WIDTH-1 downto 0);
              xmit_start  : in  std_logic;
              dupl_mode   : in  std_logic;
              xmit_data   : in  std_logic_vector (7 downto 0);
              maddr_full  : in  std_logic_vector (47 downto 0);
              waddr_next  : out std_logic;
              waddr_reset : out std_logic;
              xmit_err    : out std_logic;
              xmit_busy   : out std_logic;
              phy_tx_clk  : in  std_logic;
              phy_rx_col  : in  std_logic;
              phy_rx_crs  : in  std_logic;
              phy_txd     : out std_logic_vector (3 downto 0);
              phy_tx_en   : out std_logic;
              phy_tx_er   : out std_logic);
    end component;
    
    constant ETH_DATA_WIDTH  : integer := 8;  -- data access width fixed to byte for now
    
    constant ETH_CONTROL_REG : std_logic_vector(7 downto 0) := x"00";  -- control register offset
    constant ETH_RW_DATA     : std_logic_vector(7 downto 0) := x"04";  -- read data register
    constant ETH_MAC_ADDR    : std_logic_vector(7 downto 0) := x"08";  -- write mach address
    constant ETH_COUNTERS    : std_logic_vector(7 downto 0) := x"10";  -- read counters
    
    constant ZEROS : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    
    signal raddr, xaddr : std_logic_vector (ETH_ADDR_WIDTH-1 downto 0) := (others => '0');
    signal rdata, xdata : std_logic_vector (ETH_DATA_WIDTH-1 downto 0) := (others => '0');
    signal recv_len     : std_logic_vector (ETH_ADDR_WIDTH-1 downto 0) := (others => '0');
    signal eth_xlen     : std_logic_vector (ETH_ADDR_WIDTH-1 downto 0) := (others => '0');
    
    signal xaddr_reset, raddr_reset : std_logic := '0';
    signal xaddr_inc, raddr_inc     : std_logic := '0';
    signal rst_raddr : std_logic := '0';
    
    signal control_wr, control_rd  : std_logic                     := '0';
    signal data_wr, data_rd        : std_logic                     := '0';
    signal maddr_rd, maddr_wr      : std_logic                     := '0';
    signal counter_rd              : std_logic                     := '0';
    signal recv_byte               : std_logic_vector (7 downto 0) := (others => '0');
    
    signal rint_enb, xint_enb, prom_mode, recv_enb : std_logic := '0';
    signal waddr_reset, waddr_next                 : std_logic := '0';
    signal xmit_busy, ack_o                        : std_logic := '0';
    
    signal maddr_out  : std_logic_vector (7 downto 0);
    signal maddr_full : std_logic_vector (47 downto 0);
    
    signal ctrl_reset_xaddr, ctrl_reset_mptr : std_logic := '0';
    signal ctrl_start_xmit, ctrl_read_done   : std_logic := '0';
    signal ctrl_reset_counter                : std_logic := '0';

    signal byte_complete, frame_begin, frame_complete, frame_inv : std_logic := '0';
    signal recv_ip, crc_error : std_logic := '0';

    signal iframe_counter : std_logic_vector ((WIDTH/2)-1 downto 0) := (others => '0');
    
    signal wb_ack : std_logic := '0';

    type wbsmach_states is (wbmach_idle,   -- idle state
                            wbmach_wctrl,  -- write control register
                            wbmach_rctrl,  -- read control register
                            wbmach_wmaddr, -- write mac address
                            wbmach_rmaddr, -- read mac address
                            wbmach_rcounter,  -- read the counters
                            wbmach_nowrite,  -- ignore write
                            wbmach_wdata,  -- write data
                            wbmach_rdata); -- read data
    
    signal wbsmach_cstate, wbsmach_nstate : wbsmach_states := wbmach_idle;

    signal control_data : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal read_data    : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal maddr_data   : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    signal counter_data : std_logic_vector (WIDTH-1 downto 0) := (others => '0');
    
begin  -- rtl

    -- control signals
    ctrl_reset_xaddr   <= WBS_DAT_I(0)  and control_wr;
    ctrl_start_xmit    <= WBS_DAT_I(2)  and control_wr;
    ctrl_reset_mptr    <= WBS_DAT_I(3)  and control_wr;
    ctrl_read_done     <= WBS_DAT_I(4)  and control_wr;
    ctrl_reset_counter <= WBS_DAT_I(10) and control_wr;
    
    -- phy output signals
    phy_resetn <= not reset;
    
    process (clk)
    begin
        if rising_edge(clk) then
            -- xmit memory pointer reset
            if xaddr_reset = '1' then
                xaddr_reset <= '0' ;
            else
                xaddr_reset <= ctrl_reset_xaddr or waddr_reset;
            end if;
            
            -- latch the pointer into length when start xmit
            if (control_wr = '1' and ctrl_start_xmit = '1') then 
                eth_xlen <= xaddr ;
            end if;

            -- latch interrupt enables & modes
            if (control_wr = '1') then
                rint_enb  <= WBS_DAT_I(5);
                xint_enb  <= WBS_DAT_I(6);
                prom_mode <= WBS_DAT_I(7);
                recv_enb  <= WBS_DAT_I(1);
            end if;

        end if;
    end process;

    -- recv / xmit memory pointer management
    xaddr_inc <= waddr_next or data_wr;
    
    process (clk, reset, xaddr_reset)
    begin
        -- xmit pointer is reset or incremented
        if reset = '1' or xaddr_reset = '1' then
            xaddr <= (others => '0');
        elsif rising_edge(clk) then
            if xaddr_inc = '1' then
                xaddr <= xaddr + 1;
            end if;
        end if;
    end process ;

    raddr_reset <= frame_begin or rst_raddr;
    raddr_inc   <= data_rd or byte_complete;
    
    process (clk, reset, raddr_reset)
    begin
        -- recv pointer is reset or incremented
        if reset = '1' or raddr_reset = '1' then
            raddr <= (others => '0');
        elsif rising_edge(clk) then
            if raddr_inc = '1' then
                raddr <= raddr + 1;
            end if;
        end if;
    end process ;

    --
    -- xmit memory instance
    --
    eth_xmit_mem : eth_sram
        generic map (ETH_ADDR_WIDTH => ETH_ADDR_WIDTH,
                     ETH_DATA_WIDTH => ETH_DATA_WIDTH)
        port map (addr     => xaddr,
                  data_in  => WBS_DAT_I(ETH_DATA_WIDTH-1 downto 0),
                  data_out => xdata,
                  we       => data_wr,
                  clk      => clk,
                  reset    => reset);
    --
    -- recv memory instance
    --
    eth_recv_mem : eth_sram
        generic map (ETH_ADDR_WIDTH => ETH_ADDR_WIDTH,
                     ETH_DATA_WIDTH => ETH_DATA_WIDTH)
        port map (addr     => raddr,
                  data_in  => recv_byte,
                  data_out => rdata,
                  we       => byte_complete,
                  clk      => clk,
                  reset    => reset);

    --
    -- instantiate mac address module
    --
    macaddress_inst: macaddress
        generic map (DEFAULT_MAC_ADDR => DEFAULT_MAC_ADDR)
        port map (clk        => clk,
                  reset	     => reset,
                  rst_ptr    => ctrl_reset_mptr,
                  wmaddr     => maddr_wr,
                  rmaddr     => maddr_rd,
                  maddr_in   => WBS_DAT_I(7 downto 0),
                  maddr_out  => maddr_out,
                  maddr_full => maddr_full);
    
    --
    process (clk, reset)
    begin
        if reset = '1' then
            ack_o <= '0';
        elsif rising_edge(clk) then
            ack_o <= wb_ack;
        end if;
    end process ;

    process (clk, reset, ctrl_reset_counter)
    begin
        if reset = '1' or ctrl_reset_counter = '1' then
            iframe_counter <= (others => '0');
        elsif rising_edge(clk) then
            if frame_inv = '1' then
                iframe_counter <= iframe_counter + 1;
            end if;
        end if;
    end process ;
    
    control_data <= ZEROS(WIDTH-1 downto ETH_ADDR_WIDTH+16) & recv_len & ZEROS(15 downto 11) &
                    '0' &               -- reset counters BIT 10
                    crc_error &         -- crc error 	  BIT 9
                    frame_complete &    -- frame in fifo  BIT 8
                    prom_mode &         -- promiscuous    BIT 7
                    xint_enb &          -- xmit intr enb  BIT 6
                    rint_enb &          -- recv intr enb  BIT 5
                    "00" &              -- always 0       BIT 4
                    xmit_busy &         -- xmit in progress BIT 3
                    recv_enb &          -- receive enb    BIT 1
                    '0';                -- always 0       BIT 0
    maddr_data   <= ZEROS(WIDTH-1 downto 8) & maddr_out;
    read_data	 <= ZEROS(WIDTH-1 downto 8) & rdata;
    counter_data <= ZEROS(WIDTH-1 downto WIDTH/2) & iframe_counter;
    
    WBS_DAT_O <= control_data when control_rd = '1' else
                 maddr_data   when maddr_rd   = '1' else
                 counter_data when counter_rd = '1' else
                 read_data;    
	WBS_ERR_O <= '0';
    WBS_ACK_O <= ack_o;
    eth_intr  <= (rint_enb and frame_complete)  or -- received a frame
                 (xint_enb and not xmit_busy);     -- transmitter is idle
    
    -- The main state machine; driven by the WishBone bus
    -- read/write to control registers.
    process (clk, reset)
    begin
        if reset = '1' then
            wbsmach_cstate <= wbmach_idle ;
        elsif rising_edge(clk) then
            wbsmach_cstate <= wbsmach_nstate ;
        end if;
    end process ;

    process (WBS_STB_I, WBS_ADR_I, WBS_WE_I, wbsmach_cstate, recv_ip, ack_o)
    begin
        
        wbsmach_nstate <= wbsmach_cstate;
        wb_ack         <= '0';
        control_wr     <= '0';
        control_rd     <= '0';
        data_wr	       <= '0';
        data_rd        <= '0';
        maddr_wr       <= '0';
        maddr_rd       <= '0';
        counter_rd     <= '0';
        
        case wbsmach_cstate is
            when wbmach_idle =>
                if WBS_STB_I = '1' and ack_o = '0' then 
                    case WBS_ADR_I(7 downto 0) is
                        when ETH_CONTROL_REG =>
                            if WBS_WE_I = '1' then
                                wb_ack         <= '1';
                                wbsmach_nstate <= wbmach_wctrl;
                            else
                                wb_ack         <= '1';
                                wbsmach_nstate <= wbmach_rctrl;
                            end if;

                        when ETH_MAC_ADDR =>
                            if WBS_WE_I = '1' then
                                wb_ack         <= '1';
                                wbsmach_nstate <= wbmach_wmaddr;
                            else
                                wb_ack         <= '1';
                                wbsmach_nstate <= wbmach_rmaddr;                                
                            end if;

                        when ETH_RW_DATA =>
                            if WBS_WE_I = '1' then
                                wb_ack         <= '1';
                                wbsmach_nstate <= wbmach_wdata;
                            else
                                if recv_ip = '0' then
                                    wb_ack         <= '1';
                                    wbsmach_nstate <= wbmach_rdata;                                    
                                end if;
                            end if;

                        when ETH_COUNTERS =>
                            if WBS_WE_I = '1' then
                                wb_ack         <= '1';
                                -- cannot write to counters
                                wbsmach_nstate <= wbmach_nowrite; 
                            else
                                wb_ack         <= '1';
                                wbsmach_nstate <= wbmach_rcounter;                                    
                            end if;
                            
                        when others => null;
                    end case;
                end if;

            when wbmach_wctrl =>
                control_wr     <= '1';
                wbsmach_nstate <= wbmach_idle;

            when wbmach_rctrl =>
                control_rd     <= '1';
                wbsmach_nstate <= wbmach_idle;

            when wbmach_wmaddr =>
                maddr_wr       <= '1';
                wbsmach_nstate <= wbmach_idle;
                
            when wbmach_rmaddr =>
                maddr_rd       <= '1';
                wbsmach_nstate <= wbmach_idle;

            when wbmach_rdata =>
                data_rd        <= '1';
                wbsmach_nstate <= wbmach_idle;
                
            when wbmach_wdata =>
                data_wr        <= '1';
                wbsmach_nstate <= wbmach_idle;

            when wbmach_nowrite =>
                wbsmach_nstate <= wbmach_idle;

            when wbmach_rcounter =>
                counter_rd <= '1';
                wbsmach_nstate <= wbmach_idle;
                
            when others => null;
        end case;
    end process ;

    
    receiver : eth_recv
        generic map (ETH_ADDR_WIDTH => ETH_ADDR_WIDTH)
        port map (clk            => clk,
                  reset          => reset,
                  raddr          => raddr,
                  maddr_full     => maddr_full,
                  recv_enb	 => recv_enb,
                  mread_done     => ctrl_read_done,
                  prom_mode      => prom_mode,
                  byte_complete  => byte_complete,
                  frame_begin    => frame_begin,
                  frame_complete => frame_complete,
                  recv_ip	 => recv_ip,
                  frame_inv      => frame_inv,
                  recv_byte      => recv_byte,
                  recv_len       => recv_len,
                  rst_raddr	 => rst_raddr,
                  crc_error      => crc_error,
                  phy_rxd        => phy_rxd,
                  phy_rx_dv      => phy_rx_dv,
                  phy_rx_clk     => phy_rx_clk);

    sender : eth_send
        generic map (ETH_ADDR_WIDTH => ETH_ADDR_WIDTH)
        port map (clk         => clk,
                  reset       => reset,
                  waddr       => xaddr,
                  xmit_start  => ctrl_start_xmit,
                  dupl_mode   => '0',
                  xmit_data   => xdata,
                  maddr_full  => maddr_full,
                  waddr_next  => waddr_next,
                  waddr_reset => waddr_reset,
                  xmit_err    => open,
                  xmit_busy   => xmit_busy,
                  phy_tx_clk  => phy_tx_clk,
                  phy_rx_col  => phy_rx_col,
                  phy_rx_crs  => phy_rx_crs,
                  phy_txd     => phy_txd,
                  phy_tx_en   => phy_tx_en,
                  phy_tx_er   => phy_tx_er);
    
end rtl;
