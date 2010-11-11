PACKAGE mypkg IS

    TYPE foo IS ( 'U','X','0','1','Z','W','L','H','-');

    FUNCTION myf ( s : foo ) RETURN foo;

END mypkg;
