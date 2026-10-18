@echo off
chcp 65001 >nul
title Stop blog

set "PORT=8081"
set "FOUND="

echo Looking for the blog process on port %PORT% ...
echo.

for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%PORT%" ^| findstr "LISTENING"') do call :KILLONE %%p

if defined FOUND goto DONE
echo No blog process found on port %PORT% (already stopped).
goto END

:KILLONE
echo   killing PID %1
taskkill /F /PID %1 >nul 2>&1
set "FOUND=1"
goto :eof

:DONE
echo Blog stopped.
goto END

:END
echo.
pause
exit /b 0