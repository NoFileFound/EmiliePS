@echo off
setlocal EnableDelayedExpansion

set "proto_dir=proto"
set "out_dir=out"

if exist "%out_dir%" (
    rmdir /s /q "%out_dir%"
)
mkdir "%out_dir%"

for %%f in ("%proto_dir%\*.proto") do (
    if exist "%%f" (
        echo Processing root %%~nxf...
        protoc --java_out="%out_dir%" "%%f"
    )
)

for /d %%d in ("%proto_dir%\*") do (
    pushd "%%d"
    for %%f in (*.proto) do (
        echo Processing subdir %%f...
        protoc --java_out="..\\..\\%out_dir%" "%%f"
    )
    popd
)

endlocal
pause