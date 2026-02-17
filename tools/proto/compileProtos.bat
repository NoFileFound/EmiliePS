@echo off
setlocal enabledelayedexpansion

set "output_dir=out"
if not exist "%output_dir%" mkdir "%output_dir%"
for %%f in (*.proto) do (
    echo Processing %%f...
    protoc --java_out="%output_dir%" %%f
)

echo Done.
endlocal

pause