--------------------------------------------------------------------------------
-- This file is part of the project  MYPROJECTNAME
-- see: MYPROJECTURL
--
-- description:
-- Simple testbench for the avalon interface avs_aes together with aes_core.
--
-- Todo:  a lot! make it look nicer, more generic, maybe read data from file
--
-- Author(s):
--	   Thomas Ruschival -- ruschi@opencores.org (www.ruschival.de)
--
--------------------------------------------------------------------------------
-- Copyright (c) 2009, Thomas Ruschival
-- All rights reserved.
--
-- Redistribution and use in source and binary forms, with or without modification,
-- are permitted provided that the following conditions are met:
--    * Redistributions of source code must retain the above copyright notice,
--    this list of conditions and the following disclaimer.
--    * Redistributions in binary form must reproduce the above copyright notice,
--    this list of conditions and the following disclaimer in the documentation
--    and/or other materials provided with the distribution.
--    * Neither the name of the  nor the names of its contributors
--    may be used to endorse or promote products derived from this software without
--    specific prior written permission.
-- THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
-- AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
-- IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
-- ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
-- LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
-- OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
-- SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
-- INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
-- CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
-- ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
-- THE POSSIBILITY OF SUCH DAMAGE
-------------------------------------------------------------------------------
-- version management:
-- $Author$
-- $Date$
-- $Revision$			
-------------------------------------------------------------------------------


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
library aes_ecb_lib;
use aes_ecb_lib.aes_ecb_pkg.all;

-------------------------------------------------------------------------------

entity avs_aes_tb is

end entity avs_aes_tb;

-------------------------------------------------------------------------------

architecture arch1 of avs_aes_tb is

	-- component ports
	signal clk				  : STD_LOGIC					  := '0';  -- avalon bus clock
	signal reset			  : STD_LOGIC					  := '0';  -- avalon bus reset
	signal writedata		  : STD_LOGIC_VECTOR(31 downto 0) := (others => '0');  -- data write port
	signal address			  : STD_LOGIC_VECTOR(4 downto 0)  := (others => '0');  -- slave address space offset
	signal write			  : STD_LOGIC					  := '0';  -- write enable
	signal read				  : STD_LOGIC					  := '0';  -- read request form avalon
	signal irq				  : STD_LOGIC;	-- interrupt to signal completion
	signal readdata			  : STD_LOGIC_VECTOR(31 downto 0);	-- result read port
	signal chipselect		  : STD_LOGIC;	-- enable component
	signal keyexp_done		  : STD_LOGIC;
	signal avs_s1_waitrequest : STD_LOGIC;


---- purpose: write something to the bus
--procedure avalon_write 
--	signal	 mywritedata : in DWORD;
--	signal	 myaddress   : in NATURAL;
--begin  -- procedure avalon_write
--	wait until clk = '1';
--	write	  <= '1';
--	address	  <= STD_LOGIC_VECTOR(to_unsigned(address, 5));
--	writedata <= mywritedata;
--	wait until clk = '1';
--	write	  <= '0';
--end procedure avalon_write;


begin  -- architecture arch1

	avs_aes_1 : entity aes_ecb_lib.avs_aes
		generic map (
			KEYLENGTH  => 256,			-- AES key length
			DECRYPTION => true)			-- With decrypt or encrypt only
		port map (
			clk				   => clk,	-- avalon bus clock
			reset			   => reset,	-- avalon bus reset
			avs_s1_chipselect  => chipselect,		   -- enable component
			avs_s1_writedata   => writedata,		   -- data write port
			avs_s1_address	   => address,	-- slave address space offset
			avs_s1_write	   => write,	-- write enable
			avs_s1_read		   => read,		-- read request form avalon
			avs_s1_irq		   => irq,	-- interrupt to signal completion
			avs_s1_waitrequest => avs_s1_waitrequest,  -- stall operations
			avs_s1_readdata	   => readdata);		   -- result read port
	-- clock generation
	Clk <= not Clk after 10 ns;

	-- waveform generation
	WaveGen_Proc : process
	begin
		-- insert signal assignments here
		reset	   <= '1';
		write	   <= '0';
		read	   <= '0';
		wait for 25 ns;
		reset	   <= '0';
		chipselect <= '1';

-------------------------------------------------------------------------------
--		256 BIT key
-- 603DEB10 15CA71BE 2B73AEF0 857D7781 1F352C07 3B6108D7 2D9810A3 0914DFF4
-- 603DEB1015CA71BE2B73AEF0857D77811F352C073B6108D72D9810A30914DFF4
-------------------------------------------------------------------------------
		wait until clk = '1';
		-- write key
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(0, 5));
		writedata <= X"603DEB10";		--1st word
			--2nd word
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(1, 5));
		writedata <= X"15CA71BE";
		--3rd word
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(2, 5));
		writedata <= X"2B73AEF0";
		--4th word
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(3, 5));
		writedata <= X"857D7781";
		--5th word
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(4, 5));
		writedata <= X"1F352C07";
		--6th word
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(5, 5));
		writedata <= X"3B6108D7";
		--7th word
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(6, 5));
		writedata <= X"2D9810A3";
		--8th word
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(7, 5));
		writedata <= X"0914DFF4";
-- validate key
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(31, 5));
		writedata <= X"00000080";
-- wait until fully expanded
	--	wait until avs_s1_waitrequest = '0';

-- DATA: AA221133 11441155 11661177 21212121
		-- write data#1
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(8, 5));
		writedata <= X"AA221133";
		-- write data#2
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(9, 5));
		writedata <= X"11441155";
		-- write data#3
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(10, 5));
		writedata <= X"11661177";
		-- write data#4
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(11, 5));
		writedata <= X"21212121";
-------------------------------------------------------------------------------
		-- write control
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(31, 5));
-- data stable, key_stable irq_ena
		writedata <= X"000000C1";
		wait until clk = '1';
		write	  <= '0';
-- do the calc
		wait until irq = '1';
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16#10#, 5));
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16#11#, 5));
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16#12#, 5));
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16#13#, 5));
		wait until clk = '1';
		read	  <= '0';
-- RESULT(encrypt):
-- 256 bit key --> D7C71AF7 76F04439 1A07623A 8E6E197B
-- 192 bit key --> 87870FD6 C27D944F C83EBA16 C5DB0D63
-- 128 bit key --> 5A287C9F CDBC6D35 F3D2679C 4CB2F5B0



-------------------------------------------------------------------------------
-- decrypt the the same data under the given key
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(31, 5));
		writedata <= X"000000C2";
		wait until clk = '1';
		write	  <= '0';
		-- do the calc
		wait until irq = '1';
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16#10#, 5));
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16#11#, 5));
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16#12#, 5));
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16#13#, 5));
		wait until clk = '1';
		read	  <= '0';
-- RESULT(decrypt):
-- 256 bit key --> 63B72B79 EA1F444B 8A1AD035 CAE6B024
-- 192 bit key --> 4343EB7A 79A14922 CC18A1D6 C5D00B70
-- 128 bit key --> 02985DF8 8209EAA2 652E4125 11C98F9F		


-------------------------------------------------------------------------------
-- Encrypt again
		-- write control
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(31, 5));
-- data stable, key_stable irq_ena
		writedata <= X"000000C1";
		wait until clk = '1';
		write	  <= '0';
-- do the calc
		wait until irq = '1';
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16#10#, 5));
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16#11#, 5));
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16#12#, 5));
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16#13#, 5));
		wait until clk = '1';
		read	  <= '0';
-- RESULT(encrypt):
-- 256 bit key --> D7C71AF7 76F04439 1A07623A 8E6E197B
-- 192 bit key --> 87870FD6 C27D944F C83EBA16 C5DB0D63
-- 128 bit key --> 5A287C9F CDBC6D35 F3D2679C 4CB2F5B0


		

		
-------------------------------------------------------------------------------
-- load new data, same key
-- DATA:	AA2211CC 11440055 11001177 2121BBBB
-- RESULT:	14BD6636 9AAEBF2B 45FB0032 7FA6EBDA
		-- write data#1
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(8, 5));
		writedata <= X"AA2211CC";
		-- write data#2
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(9, 5));
		writedata <= X"11440055";
		-- write data#3
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(10, 5));
		writedata <= X"11001177";
		-- write data#4
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(11, 5));
		writedata <= X"2121BBBB";
		-- data stable, key_stable irq_ena
		wait until clk = '1';
		write	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(31, 5));
		writedata <= X"000000C1";
		wait until clk = '1';
		write	  <= '0';
		-- do calc
		wait until irq = '1';
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(16, 5));
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(17, 5));
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(18, 5));
		wait until clk = '1';
		read	  <= '1';
		address	  <= STD_LOGIC_VECTOR(to_unsigned(19, 5));
		wait until clk = '1';
		read	  <= '0';
-- RESULT:	14BD6636 9AAEBF2B 45FB0032 7FA6EBDA

-------------------------------------------------------------------------------
-- Start calc again but dirctly after start, load next data block
-- DATA:	CDEF5577 55005588 55CC5500 0000BBBB
-- RESULT:	AA1119CD 77A2F3D8 38ECA4DD 0A47975B
-------------------------------------------------------------------------------
--		wait until clk = '1';
--		write	  <= '1';
--		address	  <= X"F";
--		writedata <= X"000000C1";		-- restart calculation
--		wait until clk = '1';
--		address	  <= X"4";				-- load data
--		writedata <= X"CDEF5577";
--		-- write data#2
--		wait until clk = '1';
--		write	  <= '1';
--		address	  <= X"5";
--		writedata <= X"55005588";
--		-- write data#3
--		wait until clk = '1';
--		write	  <= '1';
--		address	  <= X"6";
--		writedata <= X"55CC5500";
--		-- write data#4
--		wait until clk = '1';
--		write	  <= '1';
--		address	  <= X"7";
--		writedata <= X"0000BBBB";
--		--deassert write, wait for completion
--		wait until clk = '1';
--		write	  <= '0';
--		wait until irq = '1';
--		wait until clk = '1';
--		-- restart with newly loaded block
--		write	  <= '1';
--		address	  <= X"F";
--		writedata <= X"00000081";		--clear IRQ, restart calculation
--		wait until clk = '1';
--		write	  <= '0';
		wait;
	end process WaveGen_Proc;

	

end architecture arch1;
