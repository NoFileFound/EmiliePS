import base64
import json

h5logKey = "F#ju0q8I9HbmH8PMpJzzBee&p0b5h@Yb"
data = ""

def decode_h5_log(data: str):
    encoded = base64.b64decode(data)
    result = bytearray(len(encoded))
    s = list(range(256))
    j = 0
    key_bytes = h5logKey.encode("utf-8")
    key_len = len(key_bytes)

    for i in range(256):
        j = (j + s[i] + key_bytes[i % key_len]) & 0xFF
        s[i], s[j] = s[j], s[i]

    i = 0
    j = 0
    for y in range(len(encoded)):
        i = (i + 1) & 0xFF
        j = (j + s[i]) & 0xFF
        s[i], s[j] = s[j], s[i]
        k = s[(s[i] + s[j]) & 0xFF]
        result[y] = encoded[y] ^ k

    return json.loads(result.decode("utf-8"))
    
print(decode_h5_log(data))