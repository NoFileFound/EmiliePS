@echo off
setlocal enabledelayedexpansion

for %%F in (*.pem) do (
    set "filename=%%~nF"
    openssl rsa -RSAPublicKey_in -in "%%F" -pubout -outform DER -out "!filename!.der" 2>nul
    if exist "!filename!.der" (
        echo Successfully converted %%F
    ) else (
        echo Failed to convert %%F
    )
)

echo Done.
pause