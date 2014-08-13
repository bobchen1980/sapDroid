/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_cal_sapdecjni_sapDecodeJni */

#ifndef _Included_com_sap_spatialex_spatialExJni
#define _Included_com_sap_spatialex_spatialExJni

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jstring JNICALL Java_com_sap_spatialex_spatialExJni_GetVersion
  (JNIEnv *, jobject);

//Starts the decoding from a vorbis bitstream to pcm
JNIEXPORT jint JNICALL Java_com_sap_spatialex_spatialExJni_StartDecoding
  (JNIEnv *, jclass, jobject);

JNIEXPORT jint JNICALL Java_com_sap_spatialex_spatialExJni_SetFileSource
  (JNIEnv *, jclass, jstring);

JNIEXPORT jint JNICALL Java_com_sap_spatialex_spatialExJni_SetSpatialEx
  (JNIEnv *, jclass, jint);

JNIEXPORT jint JNICALL Java_com_sap_spatialex_spatialExJni_getCurPosition
  (JNIEnv *, jclass);

JNIEXPORT jint JNICALL Java_com_sap_spatialex_spatialExJni_StopDecoding
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
