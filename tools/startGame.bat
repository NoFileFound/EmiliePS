@echo off
net session >nul 2>&1
if %errorlevel% neq 0 (
    powershell -Command "Start-Process '%~f0' -Verb RunAs"
    exit /b
)

D:
cd D:\genshinimpact_dev\
injector.exe "rsapatch.dll"