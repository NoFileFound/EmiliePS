#include "util.h"
#include "hookmanager.h"
#include <iostream>

#define IL2CPP_STRING_NEW 0x02C6D350 
#define SYSTEM_TEXT_ENCODINGHELPER_GETDEFAULTENCODING 0x09FB09D0 
#define SYSTEM_TEXT_ENCODING_GETBYTES 0x0A15A100 
#define MIHOYO_SDK_SDKUTIL_RSAENCRYPT 0x0A5E94B0 
#define MOLEMOLE_MOLEMOLESECURITY_GETPUBLICRSAKEY 0x04F05820 
#define MOLEMOLE_MOLEMOLESECURITY_GETPRIVATERSAKEY 0x04F03FB0
static PVOID(*orig_System__Text__EncodingHelper_GetDefaultEncoding)() = NULL;
static PVOID(*orig_System__Text__Encoding_GetBytes)(PVOID, PVOID) = NULL;
static PVOID(*orig_MiHoYo__SDK__SDKUtil_RSAEncrypt)(PVOID, PVOID) = NULL;
static PVOID(*orig_MoleMole__MoleMoleSecurity_GetPublicRSAKey)() = NULL;
static PVOID(*orig_MoleMole__MoleMoleSecurity_GetPrivateRSAKey)() = NULL;
static PVOID(*il2cpp_string_new)(LPCSTR) = NULL;

#define RSA_PASSWORD_PUBLIC "<RSAKeyValue><Modulus>q6R44Ob1RZ02POtkvvfxnkdcNqJpu8Fkhoag1/xZK3/qPvYwffumP/HQoVyoRHnJRK4ExkN71iNA2NQkgC52LEqKz8IhrxcuVfZVI6CLmi3EjaV2zTHqvt8Pjj2igdOqvs5okpFEjL2DvFQ/umazf4QQXNJgXZGxV9menVR2C24ZVkdDUCvBp3TvD6qGQKmMv/5YRRlRa/oXTRz5lFOjO6WNJpyXncAzUoJ1J9WxoWKeH4d6I7AI1QeYDqMuSAXbPyExdCH0hS4JOrhtvTUkcdXzecIaCC4yZpTlqTyC1Z2L/55FbEHNv8PLX1DknjYRSHOsoDJE9Rgnypy/dXh7tQ==</Modulus><Exponent>AQAB</Exponent></RSAKeyValue>"
#define RSA_DISPATCH_PUBLIC "<RSAKeyValue><Modulus>xbbx2m1feHyrQ7jP+8mtDF/pyYLrJWKWAdEv3wZrOtjOZzeLGPzsmkcgncgoRhX4dT+1itSMR9j9m0/OwsH2UoF6U32LxCOQWQD1AMgIZjAkJeJvFTrtn8fMQ1701CkbaLTVIjRMlTw8kNXvNA/A9UatoiDmi4TFG6mrxTKZpIcTInvPEpkK2A7Qsp1E4skFK8jmysy7uRhMaYHtPTsBvxP0zn3lhKB3W+HTqpneewXWHjCDfL7Nbby91jbz5EKPZXWLuhXIvR1Cu4tiruorwXJxmXaP1HQZonytECNU/UOzP6GNLdq0eFDE4b04Wjp396551G99YiFP2nqHVJ5OMQ==</Modulus><Exponent>AQAB</Exponent></RSAKeyValue>"
#define RSA_DISPATCH_PRIVATE ""

// =======================
// HOOKS
// =======================
PVOID hk_MiHoYo__SDK__SDKUtil_RSAEncrypt(PVOID publicKey, PVOID content)
{
    const char* key = RSA_PASSWORD_PUBLIC;
    if(!key)
        return orig_MiHoYo__SDK__SDKUtil_RSAEncrypt(publicKey, content);

	std::cout << "[+] MiHoYo__SDK__SDKUtil_RSAEncrypt patched." << std::endl;
    return orig_MiHoYo__SDK__SDKUtil_RSAEncrypt(il2cpp_string_new(key), content);
}

PVOID hk_MoleMole__MoleMoleSecurity_GetPublicRSAKey()
{
    const char* key = RSA_DISPATCH_PUBLIC;
    if (!key)
        return orig_MoleMole__MoleMoleSecurity_GetPublicRSAKey();

    std::cout << "[+] MoleMole__MoleMoleSecurity_GetPublicRSAKey patched." << std::endl;
    return orig_System__Text__Encoding_GetBytes(orig_System__Text__EncodingHelper_GetDefaultEncoding(), il2cpp_string_new(key));
}

PVOID hk_MoleMole__MoleMoleSecurity_GetPrivateRSAKey()
{
    const char* key = RSA_DISPATCH_PRIVATE;
    if(!key)
        return orig_MoleMole__MoleMoleSecurity_GetPrivateRSAKey();

	std::cout << "[+] MoleMole__MoleMoleSecurity_GetPrivateRSAKey patched." << std::endl;
    return orig_System__Text__Encoding_GetBytes(orig_System__Text__EncodingHelper_GetDefaultEncoding(), il2cpp_string_new(key));
}

// =======================
// HOOK INSTALLATION
// =======================
VOID DisableVMProtect()
{
	auto hNtdll = GetModuleHandleA("ntdll.dll");
	auto hNtProtectVirtualMemory = (BYTE*)GetProcAddress(hNtdll, "NtProtectVirtualMemory");
	BYTE bRestore[] = {0x4C, 0x8B, 0xD1, 0xB8, (BYTE)(((BYTE*)GetProcAddress(hNtdll, "NtQuerySection"))[4] - 1)};
	DWORD oldProtect = 0;
	VirtualProtect(hNtProtectVirtualMemory, sizeof(bRestore), PAGE_EXECUTE_READWRITE, &oldProtect);
	memcpy(hNtProtectVirtualMemory, bRestore, sizeof(bRestore));
	VirtualProtect(hNtProtectVirtualMemory, sizeof(bRestore), oldProtect, &oldProtect);
	std::cout << "[+] VMProtect disabled." << std::endl;
}

VOID AttachHooks()
{
    uintptr_t base = (uintptr_t)GetModuleHandleA("UserAssembly.dll");

    // resolve functions
    il2cpp_string_new = (PVOID(*)(LPCSTR))(base + IL2CPP_STRING_NEW);

    orig_System__Text__EncodingHelper_GetDefaultEncoding = (PVOID(*)())(base + SYSTEM_TEXT_ENCODINGHELPER_GETDEFAULTENCODING);
    orig_System__Text__Encoding_GetBytes = (PVOID(*)(PVOID, PVOID))(base + SYSTEM_TEXT_ENCODING_GETBYTES);
    orig_MiHoYo__SDK__SDKUtil_RSAEncrypt = (PVOID(*)(PVOID, PVOID))(base + MIHOYO_SDK_SDKUTIL_RSAENCRYPT);
    orig_MoleMole__MoleMoleSecurity_GetPublicRSAKey = (PVOID(*)())(base + MOLEMOLE_MOLEMOLESECURITY_GETPUBLICRSAKEY);
    orig_MoleMole__MoleMoleSecurity_GetPrivateRSAKey = (void* (*)())(base + MOLEMOLE_MOLEMOLESECURITY_GETPRIVATERSAKEY);

    // install hooks
	HookManager::Install(&orig_MiHoYo__SDK__SDKUtil_RSAEncrypt, hk_MiHoYo__SDK__SDKUtil_RSAEncrypt);
    HookManager::Install(&orig_MoleMole__MoleMoleSecurity_GetPublicRSAKey, hk_MoleMole__MoleMoleSecurity_GetPublicRSAKey);
    ///HookManager::Install(orig_MoleMole__MoleMoleSecurity_GetPrivateRSAKey, hk_MoleMole__MoleMoleSecurity_GetPrivateRSAKey);
}

VOID DetachHooks()
{
	HookManager::Detach(hk_MiHoYo__SDK__SDKUtil_RSAEncrypt);
    HookManager::Detach(hk_MoleMole__MoleMoleSecurity_GetPublicRSAKey);
    HookManager::Detach(hk_MoleMole__MoleMoleSecurity_GetPrivateRSAKey);
}