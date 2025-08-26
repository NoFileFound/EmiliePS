@echo off
setlocal

set "proto_dir=.\proto"
set "out_dir=.\archives"

if not exist "%out_dir%" mkdir "%out_dir%"

for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I

set "datetime=%datetime:~0,4%-%datetime:~4,2%-%datetime:~6,2%-%datetime:~8,2%-%datetime:~10,2%-%datetime:~12,2%"
set "output_zip=%out_dir%\proto-%datetime%.zip"
7za a -tzip "%output_zip%" "%proto_dir%\*"

endlocal
pause