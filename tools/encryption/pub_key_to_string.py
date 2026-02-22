from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.backends import default_backend
import base64

with open("pub.pem", "rb") as f:
    key = serialization.load_pem_public_key(f.read(), backend=default_backend())

numbers = key.public_numbers()
modulus = base64.b64encode(numbers.n.to_bytes((numbers.n.bit_length()+7)//8, 'big')).decode()
exponent = base64.b64encode(numbers.e.to_bytes((numbers.e.bit_length()+7)//8, 'big')).decode()
print(f"<RSAKeyValue><Modulus>{modulus}</Modulus><Exponent>{exponent}</Exponent></RSAKeyValue>")