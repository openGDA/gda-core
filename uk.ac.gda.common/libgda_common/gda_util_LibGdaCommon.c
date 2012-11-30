#include "gda_util_LibGdaCommon.h"
#include <stdio.h>
#include <errno.h>
#include <pwd.h>

JNIEXPORT jstring JNICALL Java_gda_util_LibGdaCommon__1getFullNameOfUser(JNIEnv *env, jclass clazz, jstring username) {
	
	// null username? - return null
	if (username == NULL) {
		return NULL;
	}
	
	const char *username_chars = (*env)->GetStringUTFChars(env, username, NULL);
	
	errno = 0;
	struct passwd *p = getpwnam(username_chars);
	
	if (p == NULL && errno > 0) {
		perror("getFullNameOfUser: unable to get user information");
	}
	
	(*env)->ReleaseStringUTFChars(env, username, username_chars);
	
	if (p == NULL) {
		// matching entry not found, or an error occurred
		return NULL;
	}
	
	// NewStringUTF works OK if pw_gecos is NULL
	jstring fullname = (*env)->NewStringUTF(env, p->pw_gecos);
	return fullname;
}
