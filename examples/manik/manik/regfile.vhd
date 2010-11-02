-------------------------------------------------------------------------------
-- Title      : Register File implementation
-- Project    : MANIK-2
-------------------------------------------------------------------------------
-- File       : regfile.vhd
-- Author     : Sandeep Dutta
-- Company    : NikTech.com
-- Created    : 2002-11-13
-- Last update: 2006-09-10
-- Platform   : 
-------------------------------------------------------------------------------
-- Description: Register file
-------------------------------------------------------------------------------
-- Copyright (c) 2002 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author  Description
-- 2002-11-13  1.0      sandeep	Created
-------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

library UNISIM;
use UNISIM.vcomponents.all;

use work.manikconfig.all;
use work.manikpackage.all;
use work.manikxilinx.all;

entity regfile is
    
    generic (WIDTH : integer := 32);

    port (DE_rd    : in  std_logic_vector (3 downto 0);
          rb       : in  std_logic_vector (3 downto 0);
          rdw      : in  std_logic_vector (3 downto 0);
          rfwe     : in  std_logic;
          rdstallu : in  std_logic;
          rbstallu : in  std_logic;
          rd_in    : in  std_logic_vector(WIDTH-1 downto 0);
          clk      : in  std_logic;
          rd_out   : out std_logic_vector (WIDTH-1 downto 0);
          rb_out   : out std_logic_vector (WIDTH-1 downto 0));
    
end regfile;

architecture Behavioral of regfile is 
    
    signal rd_wr : std_logic_vector (3 downto 0);
begin
    ---------------------------------------------------------------------------
    -- 			GENERIC
    ---------------------------------------------------------------------------
    generic_tech: if Technology /= "XILINX" generate        
        type ram_type is array (15 downto 0) of std_logic_vector (WIDTH-1 downto 0);
        
        signal RAM_rd : ram_type := (others => ALLZEROS);
        signal RAM_rb : ram_type := (others => ALLZEROS);

        signal nclk : std_logic := '0';
        signal rbeq, rdeq : std_logic;
        signal rdout_tmp : std_logic_vector (WIDTH-1 downto 0);
        signal rbout_tmp,tt : std_logic_vector (WIDTH-1 downto 0);
    begin
        nclk <= not clk;

        regfile_rd : process (clk)
        begin
            if falling_edge(clk) then
                if rfwe = '1' then
                    RAM_rd (conv_integer(rdw)) <= rd_in;                    
                end if;
                rdout_tmp <= RAM_rd(conv_integer(DE_rd));
            end if;
        end process regfile_rd;
        
        regfile_rb : process (clk)
        begin
            if falling_edge(clk) then
                if rfwe = '1' then
                    RAM_rb (conv_integer(rdw)) <= rd_in;                    
                end if;
                rbout_tmp <= RAM_rb(conv_integer(rb));
            end if;
        end process regfile_rb;
        
        --
        -- Forward write value if reading & writing the same register
        --
        rdeq   <= '1' when DE_rd = rdw and rfwe = '1' else '0';
        rd_out <= rd_in when rdeq = '1' or rdstallu = '1' else rdout_tmp after 1 ns;

        rbeq   <= '1' when rb = rdw and rfwe = '1' else '0';
        rb_out <= rd_in when rbeq = '1' or rbstallu = '1' else rbout_tmp after 1 ns;
    end generate generic_tech;

    ---------------------------------------------------------------------------
    -- 			XILINX
    ---------------------------------------------------------------------------
    xilinx_tech: if Technology = "XILINX" generate
        signal rdout_tmp : std_logic_vector (WIDTH-1 downto 0);
    
--        attribute RLOC of RDWR0_MUX0 : label is rloc_string(WIDTH/2,1,true,-(WIDTH/2)-1,1);
--        attribute RLOC of RDWR0_MUX1 : label is rloc_string(WIDTH/2,1,true,-(WIDTH/2)-1,1);
        
    begin -- XILINX_virtex

        RDWR0_MUX0 : SPECMUX_NOTOR_VECTOR
            generic map (WIDTH => 2, SLICE => 1)
            port map (SEL0 => clk, SEL1 => '0',
                      A => DE_rd(1 downto 0), B => rdw(1 downto 0), O => rd_wr(1 downto 0));
        RDWR0_MUX1 : SPECMUX_NOTOR_VECTOR
            generic map (WIDTH => 2, SLICE => 0)
            port map (SEL0 => clk, SEL1 => '0',
                      A => DE_rd(3 downto 2), B => rdw(3 downto 2), O => rd_wr(3 downto 2));

        regfile_ram: for i in 0 to WIDTH-1 generate
            constant row    : integer := (WIDTH/2)-(i/2)-1;
            constant slice  : natural := i mod 2;
            constant v2adj  : integer := boolnot(i mod 2)+2;
            attribute INIT of regfile_ram_inst : label is X"0000";
            attribute RLOC of regfile_ram_inst : label is rloc_string(row,1,slice,WIDTH/2,true,0,v2adj);
        begin
            regfile_ram_inst : RAM16X1D_1
                generic map (INIT => X"0000")
                port map (DPO   => rb_out(i), SPO    => rdout_tmp(i),
                          A0    => rd_wr(0),   A1    => rd_wr(1),
                          A2    => rd_wr(2),   A3    => rd_wr(3),
                          D     => rd_in(i),
                          DPRA0 => rb(0),     DPRA1 => rb(1),
                          DPRA2 => rb(2),     DPRA3 => rb(3),
                          WCLK  => clk,       WE    => rfwe);            
        end generate regfile_ram;        
        rd_out <= rd_in when rdstallu = '1' else rdout_tmp after 1 ns;
    end generate xilinx_tech;

end Behavioral;
