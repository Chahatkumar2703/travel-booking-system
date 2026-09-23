@echo off
echo =========================================================
echo Pushing VoyageQuest to GitHub (Chahatkumar2703/travel-booking-system)...
echo =========================================================

git push -u origin main

echo.
if %ERRORLEVEL% equ 0 (
    echo =========================================================
    echo Successfully pushed to https://github.com/Chahatkumar2703/travel-booking-system !
    echo =========================================================
) else (
    echo.
    echo If the repository does not exist yet, make sure you created
    echo 'travel-booking-system' on GitHub at https://github.com/new
)

pause
