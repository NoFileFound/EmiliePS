import ctypes
import socket
from pathlib import Path

URLS = [
    "testing-abtest-api-data-sg.mihoyo.com",
    "devlog-upload-os.hoyoverse.com"
]

ROUTES = []

def resolve_ip(domain: str) -> str:
    try:
        return socket.gethostbyname(domain)
    except socket.gaierror:
        return None

def get_base_domain(domain: str) -> str:
    if domain.endswith("mihoyo.com"):
        return "mihoyo.com"
        
    elif domain.endswith("hoyoverse.com"):
        return "hoyoverse.com"
        
    return None

def generate_hosts(urls):
    mihoyo_ip = resolve_ip("mihoyo.com")
    hoyoverse_ip = resolve_ip("hoyoverse.com")
    lines = []
    for domain in urls:
        base = get_base_domain(domain)
        if not base:
            continue

        ip = mihoyo_ip if base == "mihoyo.com" else hoyoverse_ip
        if ip:
            lines.append(f"{ip} {domain}")

    return "\n".join(lines)

def prepare_routes():
    global ROUTES
    mihoyo_ip = resolve_ip("mihoyo.com")
    hoyoverse_ip = resolve_ip("hoyoverse.com")
    if not mihoyo_ip or not hoyoverse_ip:
        raise RuntimeError("Could not resolve mihoyo.com or hoyoverse.com")

    ROUTES = []
    for domain in URLS:
        base = get_base_domain(domain)
        if not base:
            continue
        ip = mihoyo_ip if base == "mihoyo.com" else hoyoverse_ip
        ROUTES.append(f"{ip} {domain}")

def update_hosts(insert: bool) -> int:
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
                if any(l == route for route in ROUTES):
                    end_idx += 1
                else:
                    break
            break

    if start_idx is not None:
        del content[start_idx:end_idx]
    if insert:
        block = ["# Added by Genshin Proxy"]
        block += ROUTES
        if content and content[-1].strip():
            content.append("")
        content.extend(block)

    path.write_text("\n".join(content) + "\n", encoding="utf-8")
    return 0

if __name__ == "__main__":
    try:
        prepare_routes()
        print("Generated routes:")
        print("\n".join(ROUTES))
    except RuntimeError as e:
        print("Error:", e)
        exit(1)

    action = input("Add hosts entries? (y/n): ").strip().lower()
    success = update_hosts(action == "y")
    if success == 0:
        print("Hosts file updated successfully.")
    elif success == -1000:
        print("Run this script as Administrator.")
    elif success == -1001:
        print("Hosts file not found.")