/*
 *            Set of macros to simplify the jni interface calls.
 *            Note they assume the first two paramters of the call are:
 *            JNIEnv *env,
 *            jobject obj,
 */
#ifndef _Jni_Macros
#define _Jni_Macros
#define ArrayLength(a) (*env)->GetArrayLength(env , a)
#define GetDoubleArray(a) (*env)->GetDoubleArrayElements(env , a , 0)
#define ReleaseDoubleArray(a,aptr) (*env)->ReleaseDoubleArrayElements(env,a,aptr,0)
#define GetString(a) (*env)->GetStringUTFChars(env, a, NULL)
#define NewString(a) (*env)->NewStringUTF( env, a )
#define ReleaseString(jstring,string) (*env)->ReleaseStringUTFChars(env, jstring, string)
#endif

