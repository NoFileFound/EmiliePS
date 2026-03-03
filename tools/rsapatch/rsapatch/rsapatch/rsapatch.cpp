// rsapatch.cpp : Defines the exported functions for the DLL application.
//

#include "stdafx.h"
#include "rsapatch.h"


// This is an example of an exported variable
RSAPATCH_API int nrsapatch=0;

// This is an example of an exported function.
RSAPATCH_API int fnrsapatch(void)
{
	return 42;
}

// This is the constructor of a class that has been exported.
// see rsapatch.h for the class definition
Crsapatch::Crsapatch()
{
	return;
}
