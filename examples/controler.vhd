-- VHDL Model Created from SGE Symbol controler.sym -- Mar 28 20:39:39 2000
-- Changes: Separated transition and output process, process labels, beautify

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_misc.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;

entity CONTROLER is
  port ( CLK       : in  std_logic;
         IR        : in  std_logic_vector (15 downto 12);
         READY     : in  std_logic;
         RES       : in  std_logic;
         Z_OUT     : in  std_logic;
         ADR_SEL   : out std_logic;
         IR_EN     : out std_logic;
         PC_EN     : out std_logic;
         PC_MODE   : out std_logic;
         RD        : out std_logic;
         REGIN_SEL : out std_logic_vector (1 downto 0);
         RF_HE     : out std_logic;
         RF_LE     : out std_logic;
         WR        : out std_logic;
         DIR       : out std_logic);
end CONTROLER;



architecture BEHAVIORAL of CONTROLER is
  type t_state is (s_reset, s_if1, s_if2, s_id, s_alu, s_ldih, s_ldil, s_ld1, s_ld2, s_st1, s_jmp, s_call);
  signal state, next_state : t_state;
begin

  reg : process
  begin
    wait until clk'event and clk = '1';
    state <= next_state;
  end process reg;

  transition : process (state, RES, IR, READY, Z_OUT)
  begin
    next_state                                                          <= state;  -- default
    if res = '1' then
      next_state                                                        <= s_reset;
    else
      case state is
        when s_reset => next_state                                      <= s_if1;
        when s_if1   => if ready = '0' then next_state                  <= s_if2; end if;
        when s_if2   => if ready = '1' then next_state                  <= s_id; end if;
        when s_id    => if IR(15 downto 14) = "00" then next_state      <= s_alu;
                        elsif IR(15 downto 13) = "100" then next_state  <= s_ldih;
                        elsif IR(15 downto 13) = "101" then next_state  <= s_ldil;
                        elsif IR(15 downto 13) = "010" then next_state  <= s_ld1;
                        elsif IR(15 downto 13) = "011" then next_state  <= s_st1;
                        elsif IR(15 downto 12) = "1100" then next_state <= s_jmp;
                        elsif IR(15 downto 12) = "1101" then
                          if z_out = '1' then next_state                <= s_jmp; else next_state <= s_if1; end if;
                        elsif IR(15 downto 12) = "1111" then next_state <= s_call;
                        else next_state                                 <= s_if1;  -- NOP
                        end if;
        when s_alu   => next_state                                      <= s_if1;
        when s_ldih  => next_state                                      <= s_if1;
        when s_ldil  => next_state                                      <= s_if1;
        when s_ld1   => if ready = '1' then next_state                  <= s_ld2; end if;
        when s_ld2   => next_state                                      <= s_if1;
        when s_st1   => if ready = '1' then next_state                  <= s_if1; end if;
        when s_jmp   => next_state                                      <= s_if1;
        when s_call  => next_state                                      <= s_if1;
        when others  => null;
      end case;

    end if;

  end process transition;

  output : process (state)
  begin
    IR_EN     <= '0';
    PC_EN     <= '0';
    PC_MODE   <= '-';
    ADR_SEL   <= '-';
    REGIN_SEL <= "--";
    RF_LE     <= '0';
    RF_HE     <= '0';
    RD        <= '0';
    WR        <= '0';
    DIR       <= '0';

    case state is
      when s_if2  => IR_EN     <= '1'; ADR_SEL <= '0'; RD <= '1';
      when s_id   => PC_EN     <= '1'; PC_MODE <= '0';
      when s_alu  => REGIN_SEL <= "00"; RF_LE <= '1'; RF_HE <= '1';
      when s_ldih => REGIN_SEL <= "10"; RF_HE <= '1';
      when s_ldil => REGIN_SEL <= "10"; RF_LE <= '1';
      when s_ld1  => ADR_SEL   <= '1'; RD <= '1';
      when s_ld2  => ADR_SEL   <= '1'; REGIN_SEL <= "01"; RF_LE <= '1'; RF_HE <= '1'; RD <= '1';
      when s_st1  => ADR_SEL   <= '1'; WR <= '1'; DIR <= '1';
      when s_jmp  => PC_EN     <= '1'; PC_MODE <= '1';
      when s_call => PC_EN     <= '1'; PC_MODE <= '1'; REGIN_SEL <= "11"; RF_LE <= '1'; RF_HE <= '1';
      when others => null;
    end case;

  end process output;

end BEHAVIORAL;


configuration CFG_CONTROLER_BEHAVIORAL of CONTROLER is
  for BEHAVIORAL
  end for;
end CFG_CONTROLER_BEHAVIORAL;
