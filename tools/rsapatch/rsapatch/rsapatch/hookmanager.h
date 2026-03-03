#pragma once

#include <Windows.h>
#include "detours/detours.h"
#include <map>

class HookManager
{
	public:
		template<typename Fn>
		static void Install(Fn* target, Fn handler)
		{
			DetourTransactionBegin();
			DetourUpdateThread(GetCurrentThread());
			DetourAttach((PVOID*)target, handler);
			DetourTransactionCommit();
			HolderMap()[reinterpret_cast<PVOID>(handler)] = reinterpret_cast<PVOID>(*target);
		}

		template <typename Fn>
		static Fn GetInstance(Fn handler)
		{
			std::map<void*, void*>& map = HolderMap();
			std::map<void*, void*>::iterator it = map.find(reinterpret_cast<void*>(handler));
			if (it == map.end())
				return NULL;

			return reinterpret_cast<Fn>(it->second);
		}

		template <typename Fn>
		static void Detach(Fn handler)
		{
			Fn origin = GetInstance(handler);
			if(!origin) 
				return;

			DetourTransactionBegin();
			DetourUpdateThread(GetCurrentThread());
			DetourDetach(&(PVOID&)origin, handler);
			DetourTransactionCommit();
			HolderMap().erase(reinterpret_cast<void*>(handler));
		}
	private:
		static std::map<void*, void*>& HolderMap()
		{
			static std::map<void*, void*> instance;
			return instance;
		}
};