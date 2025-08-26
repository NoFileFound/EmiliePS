@echo off
setlocal EnableDelayedExpansion

set "proto_dir=proto"
set "out_dir=out"
if exist "%out_dir%" (
    echo Deleting old %out_dir%...
    rmdir /s /q "%out_dir%"
)

mkdir "%out_dir%"
for /d %%d in ("%proto_dir%\*") do (
    pushd "%%d"
    for %%f in (*.proto) do (
        echo Processing %%f...
        protoc --java_out="..\\..\\%out_dir%" %%f
    )
    popd
)

endlocal
pause