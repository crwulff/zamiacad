library IEEE;
use IEEE.std_logic_1164.all;

library IEEE;
use IEEE.VITAL_Timing.all;
use IEEE.VITAL_Primitives.all;

library STD;
use STD.TEXTIO.all;

entity X_DCM is
  generic (
    InstancePath : string := "*";

    CLKDV_DIVIDE : real := 2.0;
    );

  port (
    RST : in std_ulogic := '0'
    );

end X_DCM;

architecture X_DCM_V of X_DCM is

  PROCEDURE GenericValueCheckMessage (
    CONSTANT HeaderMsg      : IN STRING := " Attribute Syntax Error ";
    CONSTANT GenericName : IN STRING := "";
    CONSTANT EntityName : IN STRING := "";
    CONSTANT InstanceName : IN STRING := "";
    CONSTANT GenericValue : IN STRING := "";
    Constant Unit : IN STRING := "";
    Constant ExpectedValueMsg : IN STRING := "";
    Constant ExpectedGenericValue : IN STRING := "";
    CONSTANT TailMsg      : IN STRING;

    CONSTANT MsgSeverity    : IN SEVERITY_LEVEL := WARNING

    ) IS
    VARIABLE Message : LINE;
  BEGIN

    Write ( Message, HeaderMsg );
    Write ( Message, STRING'(" The attribute ") );
    Write ( Message, GenericName );
    Write ( Message, STRING'(" on ") );
    Write ( Message, EntityName );
    Write ( Message, STRING'(" instance ") );
    Write ( Message, InstanceName );
    Write ( Message, STRING'(" is set to  ") );
    Write ( Message, GenericValue );
    Write ( Message, Unit );
    Write ( Message, '.' & LF );
    Write ( Message, ExpectedValueMsg );
    Write ( Message, ExpectedGenericValue );
    Write ( Message, Unit );
    Write ( Message, TailMsg );

    ASSERT FALSE REPORT Message.ALL SEVERITY MsgSeverity;

    DEALLOCATE (Message);
  END GenericValueCheckMessage;

  PROCEDURE GenericValueCheckMessage (
    CONSTANT HeaderMsg      : IN STRING := " Attribute Syntax Error ";
    CONSTANT GenericName : IN STRING := "";
    CONSTANT EntityName : IN STRING := "";
    CONSTANT InstanceName : IN STRING := "";
    CONSTANT GenericValue : IN INTEGER;
    CONSTANT Unit : IN STRING := "";
    CONSTANT ExpectedValueMsg : IN STRING := "";
    CONSTANT ExpectedGenericValue : IN INTEGER;
    CONSTANT TailMsg      : IN STRING;

    CONSTANT MsgSeverity    : IN SEVERITY_LEVEL := WARNING

    ) IS
    VARIABLE Message : LINE;
  BEGIN

    Write ( Message, HeaderMsg );
    Write ( Message, STRING'(" The attribute ") );
    Write ( Message, GenericName );
    Write ( Message, STRING'(" on ") );
    Write ( Message, EntityName );
    Write ( Message, STRING'(" instance ") );
    Write ( Message, InstanceName );
    Write ( Message, STRING'(" is set to  ") );
    Write ( Message, GenericValue );
    Write ( Message, Unit );
    Write ( Message, '.' & LF );
    Write ( Message, ExpectedValueMsg );
    Write ( Message, ExpectedGenericValue );
    Write ( Message, Unit );
    Write ( Message, TailMsg );

    ASSERT FALSE REPORT Message.ALL SEVERITY MsgSeverity;

    DEALLOCATE (Message);
  END GenericValueCheckMessage;

  PROCEDURE GenericValueCheckMessage (
    CONSTANT HeaderMsg      : IN STRING := " Attribute Syntax Error ";
    CONSTANT GenericName : IN STRING := "";
    CONSTANT EntityName : IN STRING := "";
    CONSTANT InstanceName : IN STRING := "";
    CONSTANT GenericValue : IN BOOLEAN;
    Constant Unit : IN STRING := "";
    CONSTANT ExpectedValueMsg : IN STRING := "";
    CONSTANT ExpectedGenericValue : IN STRING := "";
    CONSTANT TailMsg      : IN STRING;

    CONSTANT MsgSeverity    : IN SEVERITY_LEVEL := WARNING

    ) IS
    VARIABLE Message : LINE;
  BEGIN

    Write ( Message, HeaderMsg );
    Write ( Message, STRING'(" The attribute ") );
    Write ( Message, GenericName );
    Write ( Message, STRING'(" on ") );
    Write ( Message, EntityName );
    Write ( Message, STRING'(" instance ") );
    Write ( Message, InstanceName );
    Write ( Message, STRING'(" is set to  ") );
    Write ( Message, GenericValue );
    Write ( Message, Unit );
    Write ( Message, '.' & LF );
    Write ( Message, ExpectedValueMsg );
    Write ( Message, ExpectedGenericValue );
    Write ( Message, Unit );
    Write ( Message, TailMsg );

    ASSERT FALSE REPORT Message.ALL SEVERITY MsgSeverity;

    DEALLOCATE (Message);
  END GenericValueCheckMessage;

  PROCEDURE GenericValueCheckMessage (
    CONSTANT HeaderMsg      : IN STRING := " Attribute Syntax Error ";
    CONSTANT GenericName : IN STRING := "";
    CONSTANT EntityName : IN STRING := "";
    CONSTANT InstanceName : IN STRING := "";
    CONSTANT GenericValue : IN INTEGER;
    CONSTANT Unit : IN STRING := "";
    CONSTANT ExpectedValueMsg : IN STRING := "";
    CONSTANT ExpectedGenericValue : IN STRING := "";
    CONSTANT TailMsg      : IN STRING;

    CONSTANT MsgSeverity    : IN SEVERITY_LEVEL := WARNING

    ) IS
    VARIABLE Message : LINE;
  BEGIN

    Write ( Message, HeaderMsg );
    Write ( Message, STRING'(" The attribute ") );
    Write ( Message, GenericName );
    Write ( Message, STRING'(" on ") );
    Write ( Message, EntityName );
    Write ( Message, STRING'(" instance ") );
    Write ( Message, InstanceName );
    Write ( Message, STRING'(" is set to  ") );
    Write ( Message, GenericValue );
    Write ( Message, Unit );
    Write ( Message, '.' & LF );
    Write ( Message, ExpectedValueMsg );
    Write ( Message, ExpectedGenericValue );
    Write ( Message, Unit );
    Write ( Message, TailMsg );

    ASSERT FALSE REPORT Message.ALL SEVERITY MsgSeverity;

    DEALLOCATE (Message);
  END GenericValueCheckMessage;
  PROCEDURE GenericValueCheckMessage (
    CONSTANT HeaderMsg      : IN STRING := " Attribute Syntax Error ";
    CONSTANT GenericName : IN STRING := "";
    CONSTANT EntityName : IN STRING := "";
    CONSTANT InstanceName : IN STRING := "";
    CONSTANT GenericValue : IN REAL;
    CONSTANT Unit : IN STRING := "";
    CONSTANT ExpectedValueMsg : IN STRING := "";
    CONSTANT ExpectedGenericValue : IN STRING := "";
    CONSTANT TailMsg      : IN STRING;

    CONSTANT MsgSeverity    : IN SEVERITY_LEVEL := WARNING

    ) IS
    VARIABLE Message : LINE;
  BEGIN

    Write ( Message, HeaderMsg );
    Write ( Message, STRING'(" The attribute ") );
    Write ( Message, GenericName );
    Write ( Message, STRING'(" on ") );
    Write ( Message, EntityName );
    Write ( Message, STRING'(" instance ") );
    Write ( Message, InstanceName );
    Write ( Message, STRING'(" is set to  ") );
    Write ( Message, GenericValue );
    Write ( Message, Unit );
    Write ( Message, '.' & LF );
    Write ( Message, ExpectedValueMsg );
    Write ( Message, ExpectedGenericValue );
    Write ( Message, Unit );
    Write ( Message, TailMsg );

    ASSERT FALSE REPORT Message.ALL SEVERITY MsgSeverity;

    DEALLOCATE (Message);
  END GenericValueCheckMessage;



begin
  INITPROC : process
  begin
      GenericValueCheckMessage
        (HeaderMsg => "Attribute Syntax Error",
         GenericName => "CLKDV_DIVIDE",
         EntityName => "X_DCM",
         InstanceName => InstancePath,
         GenericValue => CLKDV_DIVIDE,
         Unit => "",
         ExpectedValueMsg => "Legal Values for this attribute are 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, or 16.0",
         ExpectedGenericValue => "",
         TailMsg => "",
         MsgSeverity => error
         );

    wait;
  end process INITPROC;

end X_DCM_V;




