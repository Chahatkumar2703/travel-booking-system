@echo off
echo ===================================================
echo Compiling Online Travel Booking System...
echo ===================================================

if not exist bin mkdir bin

javac -d bin -cp "lib\mysql-connector-j-8.3.0.jar;src" src\util\*.java src\model\*.java src\dao\*.java src\service\*.java src\view\*.java src\Main.java

if %ERRORLEVEL% equ 0 (
    copy /Y src\db.properties bin\ >nul
    echo Compilation successful! Output files placed in bin\
) else (
    echo Compilation failed. Please check the errors above.
)
pause
