@echo off

setlocal
set /p filename="PrivateKey: "

openssl genpkey -algorithm RSA -out %filename% -aes256
openssl req -new -key %filename% -out csr.pem
openssl x509 -req -in csr.pem -signkey %filename% -out certificate.pem
openssl pkcs12 -export -out localhost.p12 -inkey %filename% -in certificate.pem
endlocal