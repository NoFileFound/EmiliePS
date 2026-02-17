@echo off
setlocal

set "output_dir=out"
if not exist "%output_dir%" mkdir "%output_dir%"
if not "%~1"=="" (
    set "filename=%~1"
) else (
    set /p filename="Filename: "
)

protoc --java_out=%output_dir% "%filename%"
echo Done!

endlocal
pause