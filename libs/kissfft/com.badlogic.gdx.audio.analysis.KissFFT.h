/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_badlogic_gdx_audio_analysis_KissFFT */

#ifndef _Included_com_badlogic_gdx_audio_analysis_KissFFT
#define _Included_com_badlogic_gdx_audio_analysis_KissFFT
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_badlogic_gdx_audio_analysis_KissFFT
 * Method:    create
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_create
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_badlogic_gdx_audio_analysis_KissFFT
 * Method:    destroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_destroy
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_badlogic_gdx_audio_analysis_KissFFT
 * Method:    spectrum
 * Signature: (J[S[F)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_spectrum
  (JNIEnv *, jclass, jlong, jshortArray, jfloatArray);

/*
 * Class:     com_badlogic_gdx_audio_analysis_KissFFT
 * Method:    getRealPart
 * Signature: (J[S)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_getRealPart
  (JNIEnv *, jclass, jlong, jshortArray);

/*
 * Class:     com_badlogic_gdx_audio_analysis_KissFFT
 * Method:    getImagPart
 * Signature: (J[S)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_analysis_KissFFT_getImagPart
  (JNIEnv *, jclass, jlong, jshortArray);

#ifdef __cplusplus
}
#endif
#endif