import json
import base64
import requests
import os
from google.protobuf.json_format import MessageToJson
from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_v1_5
from QueryCurrRegionHttpRsp_pb2 import QueryCurrRegionHttpRsp

DISPATCH_KEYS = {}
URL = ""

def load_rsa_keys() -> None:
    for filename in os.listdir("keys"):
        if not filename.endswith(".pem"):
            continue

        key_id = os.path.splitext(filename)[0]
        filepath = os.path.join("keys", filename)
        try:
            with open(filepath, "rb") as f:
                key = RSA.import_key(f.read())
                DISPATCH_KEYS[key_id] = key
        except Exception as e:
            print(f"[!] Failed to load key {filename}: {e}")

def fetch_dispatch_url(url: str) -> bytes:
    response = requests.get(url)
    return response.content

def decrypt_dispatch(message_b64: str, key_id: str) -> bytes:
    try:
        private_key = DISPATCH_KEYS[key_id]
    except KeyError:
        raise ValueError(f"Unknown decryption key_id={key_id}")

    cipher = PKCS1_v1_5.new(private_key)
    decoded = base64.b64decode(message_b64)
    decrypted = bytearray()
    key_size = private_key.size_in_bytes()
    sentinel = b"\x00"
    for i in range(0, len(decoded), key_size):
        chunk = decoded[i:i + key_size]
        decrypted_chunk = cipher.decrypt(chunk, sentinel)
        if decrypted_chunk == sentinel:
            raise ValueError("RSA decryption failed.")
        decrypted.extend(decrypted_chunk)
        
    return bytes(decrypted)

def get_query_param(url: str, key: str):
    if "?" not in url:
        return None

    query = url.split("?", 1)[1]
    for pair in query.split("&"):
        if "=" in pair:
            k, v = pair.split("=", 1)
            if k == key:
                return v

    return None

if __name__ == "__main__":
    load_rsa_keys()
    curr = QueryCurrRegionHttpRsp()
    try:
        content = fetch_dispatch_url(URL)
    except RuntimeError as e:
        print(f"[!] {e}")
        exit(1)

    key_id = get_query_param(URL, "key_id")
    try:
        if key_id:
            curr.ParseFromString(decrypt_dispatch(json.loads(content)["content"], key_id))
        else:
            curr.ParseFromString(base64.b64decode(content))

        print(MessageToJson(curr, preserving_proto_field_name=True))
    except Exception as e:
        print(f"[!] Failed to process response: {e}")