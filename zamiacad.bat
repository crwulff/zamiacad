@echo off

:setlocal avoids the classpath inflation when zamiacad.bat is invoked recurrently, many times
setlocal

IF not DEFINED ZAMIA_HOME (
	set ZAMIA_HOME=%~dp0
)

set PYTHONPATH=%ZAMIA_HOME%\share\python\Lib

set CLASSPATH=%CLASSPATH%;%ZAMIA_HOME%bin;%ZAMIA_HOME%share;%ZAMIA_HOME%share\jars\*

echo This will start python interpreter within zamia project. You may run a script by zamia_source("your_script.py") or use %~nx0 --help for other options.
java -Xmx1424m -Xms768m -Xss4m -server org.zamia.cli.Zamia %*

endlocal
