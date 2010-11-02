--------------------------------------------------------------------------------
-- This file is part of the project  MYPROJECTNAME
-- see: MYPROJECTURL
--
-- description: - THIS COMPONENT IS OBSOLETE!!!
-- Generates all roundkeys for the AES algorithm. in each round on key is used
-- to XOR with the round data, e.g. the state. because this is for encryption
-- of multiple plaintext blocks always the same roundkey sequence the keys are
-- stored until a new key is provided.
-- Starting from an initial 128, 192 or 256 Bit key (table of 4,6 or eight
-- columns = i) the sucessive roundkeys are calculated in the following way:
-- 1.) The 1st round is done with the initial key with the dwords dw[0] to
-- dw[i-1]
-- 2.) dw[n*i] is build through rotating dw[i-1] 1 left, Substituting its
-- contents with the Sbox function, the result then is XORed with
-- roundconstant[n] and it is again XORed with dw[(n-1)i].
--
-- Author(s):
--	   Thomas Ruschival -- ruschi@opencores.org (www.ruschival.de)
--
--------------------------------------------------------------------------------
-- Copyright (c) 2009, Authors and opencores.org
-- All rights reserved.
--
-- Redistribution and use in source and binary forms, with or without modification,
-- are permitted provided that the following conditions are met:
--    * Redistributions of source code must retain the above copyright notice,
--    this list of conditions and the following disclaimer.
--    * Redistributions in binary form must reproduce the above copyright notice,
--    this list of conditions and the following disclaimer in the documentation
--    and/or other materials provided with the distribution.
--    * Neither the name of the organization nor the names of its contributors
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


library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_std.all;

library util_lib;
use util_lib.util_pkg.all;

library aes_ecb_lib;
use aes_ecb_lib.aes_ecb_pkg.all;

entity keygenerator is
	generic (
		KEYLENGTH : NATURAL := 128;	 -- Size of keyblock (128, 192, 256 Bits)
		NO_ROUNDS : NATURAL := 10	 -- how many rounds
		);
	port (
		clk		   : in	 STD_LOGIC;		-- system clock
		initialkey : in	 KEYBLOCK;		-- original userkey
		key_stable : in	 STD_LOGIC;		-- signal for enabling write of initial
										-- key and signal valid key
										-- ena=0->blank_key
		round	   : in	 NIBBLE;		-- index for selecting roundkey
		roundkey   : out KEYBLOCK;		-- key for each round
		ready	   : out STD_LOGIC
		);
end entity keygenerator;



architecture arch1 of keygenerator
is
	-- memory for roundkeys
	type   KEYMEM_TYPE is array (0 to 15) of KEYBLOCK;
	signal KEYMEM		 : KEYMEM_TYPE;
	signal keymem_in	 : KEYBLOCK;	-- write port for keymem
	signal ena_w		 : STD_LOGIC;	-- ena port for key memory
	signal to_sbox		 : DWORD;		-- column to be substituted
	signal from_sbox	 : DWORD;		-- substutited column from sbox
	signal roundcnt		 : NATURAL range 0 to 15;  -- address to write key
	signal next_roundcnt : NATURAL range 0 to 15;  -- and cound the expansion steps
	signal current_block : KEYBLOCK;	-- register for the last block written
	-- to memory for cumputation of new block
	signal ready_i		 : STD_LOGIC;

begin
	---------------------------------------------------------------------------
	-- keymemory, RAM should be inferred
	---------------------------------------------------------------------------
	-- purpose: keymemory (implemented in registers)
	-- type	  : sequential
	-- inputs : clk, res_n, ramport
	-- outputs: roundkeys
	keymemory_io : process (clk)
		is
	begin  -- process memory_io
		if rising_edge(clk) then		-- rising clock edge
			if ena_w = '1' then			-- write while keys_are not done
				KEYMEM(roundcnt) <= keymem_in;
			end if;
			-- assign keyblock for round
			roundkey <= KEYMEM(to_integer(UNSIGNED(round)));
		end if;
	end process keymemory_io;


	---------------------------------------------------------------------------
	-- Keygenerate gets its own sboxes to substitute columns to define clear
	-- interface and increase f_max as this was on the critical path while
	-- shared with aes_core_encrypt
	---------------------------------------------------------------------------
	HighWord : sboxM4K
		generic map (
			rominitfile => "sbox.hex")
		port map (
			clk	  => clk,
			address_a => to_sbox(31 downto 24),
			address_b => to_sbox(23 downto 16),
			q_a		  => from_sbox(31 downto 24),
			q_b		  => from_sbox(23 downto 16));
	LowWord : sboxM4K
		generic map (
			rominitfile => "sbox.hex")
		port map (
			clk	  => clk,
			address_a => to_sbox(15 downto 8),
			address_b => to_sbox(7 downto 0),
			q_a		  => from_sbox(15 downto 8),
			q_b		  => from_sbox(7 downto 0));		


	-- purpose: Counts the rounds up until all roundkeys are generated
	-- type	  : sequential
	-- inputs : clk, initkey_done
	-- outputs: keys_ready
	clocked_counter_fsm : process (clk)
		is
	begin  -- process counter_controller
		if rising_edge(clk) then
			roundcnt	  <= next_roundcnt;
			current_block <= keymem_in;
			ready		  <= ready_i;
		end if;
	end process clocked_counter_fsm;


	-- last DWORD of the previous roundkey by 1 to the left
	-- and let it be substituted through the SBOX, sbox takes 1 cycle,
	-- current_block is the registered value of keymem_in
	-- ==> send keymem_in now, sbox will deliver correct result next cycle
	to_sbox <= keymem_in(keymem_in'right)(23 downto 0) & keymem_in(keymem_in'right)(31 downto 24);


	-- purpose: expands the initial key and fills the roundkeys array
	-- type	  : sequential
	-- inputs : current_block, previous_block, from_sbox
	generate_roundkeys : process (current_block, from_sbox, initialkey,
								  key_stable, keymem_in, roundcnt)
		is
	begin  -- process generate_roundkeys
		if key_stable = '1' then
			-- in the 1sr round only write the key to keymem
			if roundcnt = 0 then
				keymem_in	  <= initialkey;
				ena_w		  <= '1';
				next_roundcnt <= roundcnt+1;
				ready_i		  <= '0';
			else
				-- XOR the substituted signal with round constant and the 1st DWORD previous roundkey
				keymem_in(0) <= from_sbox xor (roundconstants(roundcnt) & X"000000") xor current_block(0);
				-- the rest of the roundkey columns are generated by XOR of the
				-- roundkeycolumns of the previuos round with the newly generated
				-- 1st column
				for i in 1 to current_block'right loop
					keymem_in(i) <= current_block(i) xor keymem_in(i-1);
				end loop;  -- i in 1 to NO_KEYCOLUMNS-1

				-- compute next round counter and generate write flags
				if roundcnt <= NO_ROUNDS then
					-- next counter
					next_roundcnt <= roundcnt+1;
					ready_i		  <= '0';
					ena_w		  <= '1';
				else
					next_roundcnt <= roundcnt;
					ready_i		  <= '1';
					ena_w		  <= '0';
				end if;
			end if;
		else
			-- default if ena=0
			ena_w		  <= '0';
			next_roundcnt <= 0;
			ready_i		  <= '0';
			keymem_in	  <= initialkey;
		end if;
	end process generate_roundkeys;
	
end architecture arch1;

