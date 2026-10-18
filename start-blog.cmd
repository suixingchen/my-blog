@echo off
chcp 65001 >nul
title Blog - port 8081

rem ===== always run inside the project folder =====
rem (so relative paths like ./uploads point to the project, not wherever you clicked)
cd /d "%~dp0"
rem =================================================

set "PORT=8081"
set "JAVA=D:\tools\jdk-17.0.20.1+1\bin\java.exe"
set "JAR=%~dp0target\my-blog-0.0.1-SNAPSHOT.jar"
set "TEMPDIR=%~dp0.run-tmp"
set "MYSQLD=D:\tools\mysql-8.0.28-winx64\bin\mysqld.exe"
set "MYSQLINI=D:\tools\mysql-8.0.28-winx64\my.ini"

if not exist "%JAVA%" goto NOJAVA
if not exist "%JAR%" goto NOJAR
if not exist "%TEMPDIR%" mkdir "%TEMPDIR%"

rem ===== step 1: make sure MySQL is running (port 3306) =====
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul
if not errorlevel 1 goto MYSQL_OK

echo MySQL is not running, starting it ...
start "MySQL 8.0" /min "%MYSQLD%" --defaults-file="%MYSQLINI%"
echo Waiting for MySQL (about 10 seconds) ...
timeout /t 10 /nobreak >nul
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul
if errorlevel 1 goto NO_MYSQL

:MYSQL_OK
echo ==================================================
echo   Blog starting (project: my-blog)
echo   Working dir : %CD%
echo   Database    : MySQL localhost:3306/blog
echo.
echo   Home : http://localhost:%PORT%
echo   Admin: http://localhost:%PORT%/admin
echo ==================================================
echo.

start "Blog :%PORT%" /min "%JAVA%" "-Djava.io.tmpdir=%TEMPDIR%" -jar "%JAR%" --server.port=%PORT%

echo Waiting for the blog to start (about 12 seconds) ...
timeout /t 12 /nobreak >nul
start "" "http://localhost:%PORT%"

echo.
echo Started. You can close THIS window - the server keeps running.
echo The minimized window "Blog :%PORT%" IS the server: closing it stops the blog.
echo Or double-click stop-blog.cmd
echo.
pause
exit /b 0

:NO_MYSQL
echo [ERROR] MySQL did not start. Check: D:\tools\mysql-data\mysql-error.log
pause
exit /b 1

:NOJAVA
echo [ERROR] Java not found: %JAVA%
pause
exit /b 1

:NOJAR
echo [ERROR] jar not found: %JAR%
echo         Build it first:
echo             cd /d D:\c++\my-blog
echo             "D:\tools\apache-maven-3.9.16\bin\mvn.cmd" -s "D:\c++\.m2\settings.xml" clean package -DskipTests
pause
exit /b 1
