@echo off

setlocal
set "source_dir=archive"
set "output_zip=archive.zip"
7za a -tzip "%output_zip%" "%source_dir%\*"
endlocal
pause