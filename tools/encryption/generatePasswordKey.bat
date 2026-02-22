openssl genpkey -algorithm RSA -out passwordKeyPrivate.pem -pkeyopt rsa_keygen_bits:2048
openssl pkcs8 -topk8 -inform PEM -outform DER -in passwordKeyPrivate.pem -nocrypt -out passwordKeyPrivate.der
openssl rsa -pubout -in passwordKeyPrivate.pem -out passwordKeyPublic.pem