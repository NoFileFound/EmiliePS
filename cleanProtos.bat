@echo off
setlocal

set "webserverprotos_dir=webserver\src\main\java\org\genshinhttpsrv\protobuf"
for %%f in ("%webserverprotos_dir%\*") do (
    if /i not "%%~nxf"=="README.md" (
        del /f /q "%%f"
    )
)

endlocal
pause