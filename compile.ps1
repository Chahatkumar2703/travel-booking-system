# PowerShell Compile Script
Write-Host "Compiling Online Travel Booking System..." -ForegroundColor Cyan

if (!(Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

javac -d bin -cp "lib/mysql-connector-j-8.3.0.jar;src" src/util/*.java src/model/*.java src/dao/*.java src/service/*.java src/view/*.java src/Main.java

if ($LASTEXITCODE -eq 0) {
    Copy-Item "src/db.properties" -Destination "bin/" -Force
    Write-Host "Compilation successful! Output files in bin/" -ForegroundColor Green
} else {
    Write-Host "Compilation failed. Check errors above." -ForegroundColor Red
}
