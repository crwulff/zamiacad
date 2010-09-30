entity X_DCM is
end X_DCM;

architecture X_DCM_V of X_DCM is

  function GB2 (input : natural) return natural is
  begin
    case (input) is
      when 0           => return 42;
      when 1 to 2      => return 23;
      when 3 to 4      => return 5;
      when others      => return 0;
    end case;
  end GB2;

  constant c : natural := GB2(2);


begin

end X_DCM_V;




