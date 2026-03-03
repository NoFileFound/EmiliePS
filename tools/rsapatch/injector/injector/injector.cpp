#include <Windows.h>

void myPrint(const char* msg)
{
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    if(hConsole == INVALID_HANDLE_VALUE)
        return;

    DWORD written = 0;
    WriteConsoleA(hConsole, msg, lstrlenA(msg), &written, NULL);
}

bool InjectDLL(HANDLE hProcess, const char* dllPath)
{
    LPVOID remoteBuffer = VirtualAllocEx(hProcess, NULL, lstrlenA(dllPath) + 1, MEM_COMMIT | MEM_RESERVE, PAGE_READWRITE);
    if(!remoteBuffer)
	{
		myPrint("VirtualAllocEx failed");
		return false;
	}

    if(!WriteProcessMemory(hProcess, remoteBuffer, dllPath, lstrlenA(dllPath) + 1, NULL))
    {
		myPrint("WriteProcessMemory failed");
        VirtualFreeEx(hProcess, remoteBuffer, 0, MEM_RELEASE);
        return false;
    }

    HMODULE hKernel32 = GetModuleHandleA("kernel32.dll");
    if(!hKernel32)
	{
		myPrint("GetModuleHandleA failed");
		return false;
	}

    LPVOID loadLibrary = GetProcAddress(hKernel32, "LoadLibraryA");
    if(!loadLibrary)
	{
		myPrint("GetProcAddress failed");
		return false;
	}

    HANDLE hThread = CreateRemoteThread(hProcess, NULL, 0, (LPTHREAD_START_ROUTINE)loadLibrary, remoteBuffer, 0, NULL);
    if(!hThread)
    {
        VirtualFreeEx(hProcess, remoteBuffer, 0, MEM_RELEASE);
		myPrint("CreateRemoteThread failed");
        return false;
    }

    WaitForSingleObject(hThread, INFINITE);
    CloseHandle(hThread);
    VirtualFreeEx(hProcess, remoteBuffer, 0, MEM_RELEASE);
    return true;
}

int main(int argc, CHAR* argv[])
{
    STARTUPINFOA si;
    PROCESS_INFORMATION pi;
    ZeroMemory(&si, sizeof(si));
    ZeroMemory(&pi, sizeof(pi));
    si.cb = sizeof(si);
    const char* targets[] = {"Genshinimpact.exe", "Yuanshen.exe"};
    const char* chosenExe = NULL;
	if(argc < 2 || argv[1] == nullptr)
	{
		myPrint("[!] Missing DLL path argument\n");
		return EXIT_FAILURE;
	}

    for(int i = 0; i < 2; i++)
    {
        if(GetFileAttributesA(targets[i]) != INVALID_FILE_ATTRIBUTES)
        {
            chosenExe = targets[i];
            break;
        }
    }

    if(!chosenExe)
    {
        myPrint("[!] The game executable is not found. \n");
        return EXIT_FAILURE;
    }

    if(!CreateProcessA(chosenExe, NULL, NULL, NULL, FALSE, 0, NULL, NULL, &si, &pi))
    {
        myPrint("[!] Failed to create process\n");
        return EXIT_FAILURE;
    }

    if(!InjectDLL(pi.hProcess, argv[1]))
    {
        myPrint("[!] DLL injection failed\n");
        TerminateProcess(pi.hProcess, 1);
        return EXIT_FAILURE;
    }

    ResumeThread(pi.hThread);
    CloseHandle(pi.hThread);
    CloseHandle(pi.hProcess);
    return EXIT_SUCCESS;
}