#include <Windows.h>
#include <iostream>
#include "util.h"

DWORD WINAPI InitDllMain(LPVOID param)
{
	if(!GetConsoleWindow())
    {
        AllocConsole();
    }

    HMODULE hModule = (HMODULE)param;
    DisableVMProtect();
    while(GetModuleHandleA("UserAssembly.dll") == nullptr)
    {
        std::cout << "[!] UserAssembly.dll isn't initialized, retrying..." << std::endl;
        Sleep(1500);
    }

    Sleep(5000);
    AttachHooks();
    return 0;
}

BOOL APIENTRY DllMain(HMODULE hInstance, DWORD reason, LPVOID)
{
    switch (reason)
    {
		case DLL_PROCESS_ATTACH:
		{
			DisableThreadLibraryCalls(hInstance);
			HANDLE hThread = CreateThread(NULL, 0, InitDllMain, hInstance, 0, NULL);
			if(hThread)
				CloseHandle(hThread);

			break;
		}
		case DLL_PROCESS_DETACH:
		{
			DetachHooks();
			break;
		}
    }

    return TRUE;
}