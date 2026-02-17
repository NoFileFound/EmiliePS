import asyncio
import contextlib
import ctypes
import socket
import ssl
import os
from colorconsole import win
from pathlib import Path

ROUTES = [
    # DEV
    "preapi-os-takumi.hoyoverse.com",
    "pre-sg-data-op.hoyoverse.com",
    "devlog-upload-os.hoyoverse.com",
    "hk4e-uspider.mihoyo.com",
    "hk4e-shadercollect.mihoyo.com",
    
    # SANDBOX
    "sandbox-sdk-os.hoyoverse.com",
    "webstatic-test.hoyoverse.com",
    
    "log-upload-os.hoyoverse.com",
    "dispatchosglobal.yuanshen.com",
    "overseauspider.yuanshen.com",
    "api-os-takumi.hoyoverse.com",
    "sg-public-data-api.hoyoverse.com",
    "webstatic.hoyoverse.com",
    "hk4e-sdk.mihoyo.com",
    "minor-api.mihoyo.com",
    "sdk-static.mihoyo.com",
    "hk4e-sdk-s.mihoyo.com",
    "public-data-api.mihoyo.com"
]

HTTP_PORTS = [80, 8888, 8100, 8998]

T = win.Terminal()

def update_hosts(insert : bool) -> int:
    if not ctypes.windll.shell32.IsUserAnAdmin():
        return -1000
        
    path = Path(r"C:\Windows\System32\drivers\etc\hosts")
    if not path.exists():
        return -1001
    
    content = path.read_text(encoding="utf-8").splitlines()
    start_idx = end_idx = None
    for i, line in enumerate(content):
        if line.strip() == "# Added by Genshin Proxy":
            start_idx = i
            end_idx = i + 1
            while end_idx < len(content):
                l = content[end_idx].strip()
                if any(l == f"127.0.0.1 {h}" for h in ROUTES):
                    end_idx += 1
                else:
                    break
            break

    if start_idx is not None:
        del content[start_idx:end_idx]

    if insert:
        block = ["# Added by Genshin Proxy"]
        block += [f"127.0.0.1 {h}" for h in ROUTES]
        if content and content[-1].strip():
            content.append("")

        content.extend(block)

    path.write_text("\n".join(content) + "\n", encoding="utf-8")
    return 0
    
def load_hosts() -> None:
    result = update_hosts(True)
    if result == -1000:
        T.cprint(12, 0, "[!] You need to run this application as an administrator.")
    elif result == -1001:
        T.cprint(12, 0, "[!] The hosts file was not found. Create it at C:\\Windows\\System32\\drivers\\etc\\hosts")
    else:
        T.cprint(10, 0, "[+] Loaded the routes.")
    
    T.cprint(15, 0, "\n")

async def read_request(reader):
    data = b""
    while b"\r\n\r\n" not in data:
        chunk = await reader.read(4096)
        if not chunk:
            break
        data += chunk
    return data

async def relay(reader, writer):
    try:
        while True:
            data = await reader.read(8192)
            if not data:
                break

            writer.write(data)
            await writer.drain()
    except asyncio.CancelledError:
        pass
    except (ConnectionResetError, BrokenPipeError):
        pass
    except Exception:
        pass
    finally:
        try:
            writer.close()
            await writer.wait_closed()
        except Exception:
            pass

async def handle_https(client_reader, client_writer):
    try:
        data = await read_request(client_reader)
        if not data:
            return

        lines = data.split(b"\r\n")
        request_line = lines[0].decode(errors="ignore")
        parts = request_line.split(" ")
        method = parts[0]
        path = parts[1]
        host = "unknown"
        for line in lines[1:]:
            if line.lower().startswith(b"host:"):
                host = line.split(b":", 1)[1].strip().decode(errors="ignore")
                break

        T.cprint(9, 0,f"[HTTPS] {method} https://{host}{path} -> 127.0.0.1:8881\n")
        try:
            target_reader, target_writer = await asyncio.open_connection("127.0.0.1", 8881)
        except ConnectionRefusedError:
            client_writer.write(b"HTTP/1.1 502 Bad Gateway\r\n" b"Content-Length: 11\r\n\r\n" b"Bad Gateway")
            await client_writer.drain()
            return

        target_writer.write(data)
        await target_writer.drain()
        await asyncio.gather(relay(client_reader, target_writer), relay(target_reader, client_writer),)
    except asyncio.CancelledError:
        pass
    except Exception:
        pass
    finally:
        try:
            client_writer.close()
            await client_writer.wait_closed()
        except Exception:
            pass

async def handle_http(client_reader, client_writer):
    try:
        data = await read_request(client_reader)
        if not data:
            return

        lines = data.split(b"\r\n")
        request_line = lines[0].decode(errors="ignore")
        parts = request_line.split(" ")
        method = parts[0]
        path = parts[1]
        host = "unknown"
        for line in lines[1:]:
            if line.lower().startswith(b"host:"):
                host = line.split(b":", 1)[1].strip().decode(errors="ignore")
                break

        T.cprint(9, 0, f"[HTTP] {method} http://{host}{path} -> 127.0.0.1:8881\n")
        try:
            target_reader, target_writer = await asyncio.open_connection("127.0.0.1", 8881)
        except ConnectionRefusedError:
            client_writer.write(b"HTTP/1.1 502 Bad Gateway\r\n" b"Content-Length: 11\r\n\r\n" b"Bad Gateway")
            await client_writer.drain()
            return

        target_writer.write(data)
        await target_writer.drain()
        await asyncio.gather(relay(client_reader, target_writer), relay(target_reader, client_writer),)
    except asyncio.CancelledError:
        pass
    except Exception:
        pass
    finally:
        try:
            client_writer.close()
            await client_writer.wait_closed()
        except Exception:
            pass

async def start_proxy() -> None:
    ssl_ctx = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
    ssl_ctx.load_cert_chain("cert.pem", "key.pem")
    http_servers = []
    for port in HTTP_PORTS:
        server = await asyncio.start_server(handle_http, host="0.0.0.0", port=port)
        http_servers.append(server)
    T.cprint(10, 0, f"[+] HTTP proxy listening on: {HTTP_PORTS}\n")

    https_server = await asyncio.start_server(handle_https, host="0.0.0.0", port=443, ssl=ssl_ctx)
    T.cprint(10, 0, "[+] HTTPS proxy listening on: 443\n")
    try:
        async with contextlib.AsyncExitStack() as stack:
            for s in http_servers:
                await stack.enter_async_context(s)
            await stack.enter_async_context(https_server)
            await asyncio.gather(
                *(s.serve_forever() for s in http_servers),
                https_server.serve_forever(),
            )

    except asyncio.CancelledError:
        pass
    finally:
        T.cprint(12, 0, "[-] Stopping the proxy.")
        T.cprint(15, 0, "\n")
        for s in http_servers:
            s.close()
            await s.wait_closed()

        https_server.close()
        await https_server.wait_closed()
        update_hosts(False)

if __name__ == "__main__":
    os.system("cls")
    load_hosts()
    asyncio.run(start_proxy())