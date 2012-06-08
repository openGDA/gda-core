/*          C implementation for FFT.java natives
 */
#include "jnimacros.h"
#include "gda_analysis_numerical_fourier_FFTW.h"
#include <fftw3.h>
#include <stdio.h>

/*          nativeLoadWisdomFromFile method
 */
JNIEXPORT jboolean JNICALL Java_gda_analysis_numerical_fourier_FFTW_nativeLoadWisdomFromFile
(JNIEnv *env, jobject obj, jstring jfilename) {

  const char *filename;           // Filename in c format
  FILE *file;                     // The input file
  jboolean status = 0;            // status flag
  
  filename = GetString(jfilename);        // Get string in C format

  // printf("Wisdom file is %s\n",filename);
  
  file = fopen(filename,"r");             // Try and open file
  if (file != NULL) {                     // Try to read wisdom
    status = (jboolean)fftw_import_wisdom_from_file(file);
    fclose(file);
  }

  ReleaseString(jfilename,filename);      // release file
  return status;                          // Return status
  }

/*          nativeLoadWisdomFromString
 */
JNIEXPORT jboolean JNICALL Java_gda_analysis_numerical_fourier_FFTW_nativeLoadWisdomFromString
(JNIEnv *env, jobject obj, jstring jwisdom) {
  
  const char *wisdom = GetString(jwisdom);              // Get string in C format
  jboolean status  = (jboolean)fftw_import_wisdom_from_string(wisdom);
  ReleaseString(jwisdom,wisdom);
  return status;
}




/*          nativeClearWisdom method
 */
JNIEXPORT void JNICALL Java_gda_analysis_numerical_fourier_FFTW_nativeClearWisdom
(JNIEnv *env, jobject obj){
  fftw_forget_wisdom();                 // Simple void call
}


/*          nativeExportWisdomToFile method
 */

JNIEXPORT jboolean JNICALL Java_gda_analysis_numerical_fourier_FFTW_nativeExportWisdomToFile
(JNIEnv *env, jobject obj, jstring jfilename) {
  
  const char *filename;           // Filename in c format
  FILE *file;                     // The input file
  jboolean status = 0;            // status flag
  
  filename = GetString(jfilename);        // Get string in C format

  // printf("Wisdom output file is %s\n",filename);
  
  file = fopen(filename,"w");            // Try and open file
  if (file != NULL) {
    fftw_export_wisdom_to_file(file);    // Do the write
    fclose(file);
    status = 1;                          // Set statue
  }

  
  ReleaseString(jfilename,filename);      // release file
  return status;                          // Return status
  }


/*             nativeGetWisdom
 */

JNIEXPORT jstring JNICALL Java_gda_analysis_numerical_fourier_FFTW_nativeGetWisdom
(JNIEnv *env, jobject obj){
  
  char *wisdom = fftw_export_wisdom_to_string();   // Get the wisdom

  jstring jwisdom = NewString(wisdom);             // Make new java string
  fftw_free(wisdom );                              // Deallocate wisdom

  return jwisdom;                                  // return j staring
}



  
