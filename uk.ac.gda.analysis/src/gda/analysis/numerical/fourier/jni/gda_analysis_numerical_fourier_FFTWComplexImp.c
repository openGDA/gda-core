#include "jnimacros.h"
#include "gda_analysis_numerical_fourier_FFTWComplex.h"
#include <fftw3.h>
#include <stdio.h>

 
/*           nativeOneDimensional
 */

JNIEXPORT void JNICALL Java_gda_analysis_numerical_fourier_FFTWComplex_nativeOneDimensional
(JNIEnv *env, jobject obj, jdoubleArray in, jdoubleArray out, jint dirn, jint flag){

  
  double *inptr, *outptr;                     // input and output array pointers
  int length;                                 // Transform length
  fftw_plan  plan;                            // the plan

  inptr = GetDoubleArray(in);                // Get input array
  outptr = GetDoubleArray(out);              // get output array
  length = ArrayLength(in)/2;                // Half length needed

  //          Make a simple plan using flag (default to ESTIMATE)
  plan = fftw_plan_dft_1d(length, (fftw_complex*)inptr,
                          (fftw_complex*)outptr,dirn,
                          (unsigned int)flag);

  fftw_execute(plan);                       // Do the FFT
  fftw_destroy_plan(plan);                  // Clear up
  
  ReleaseDoubleArray(in,inptr);             // Release the arrays
  ReleaseDoubleArray(out,outptr);
}


/*        nativeTwoDimensional
 */
JNIEXPORT void JNICALL Java_gda_analysis_numerical_fourier_FFTWComplex_nativeTwoDimensional
(JNIEnv *env, jobject obj, jint width, jint height, jdoubleArray in, jdoubleArray out, jint dirn, jint flag){


  double *inptr, *outptr;                     // input and output array pointers
  fftw_plan  plan;                            // the plan

  inptr = GetDoubleArray(in);                // Get input array
  outptr = GetDoubleArray(out);              // get output array

  //          Make a simple plan using flag (normall ESTIMATE)
  plan = fftw_plan_dft_2d(height,width, (fftw_complex*)inptr,
                          (fftw_complex*)outptr,dirn,
                          (unsigned int)flag);

  fftw_execute(plan);                       // Do the FFT
  fftw_destroy_plan(plan);                  // Clear up

  ReleaseDoubleArray(in,inptr);             // Release the arrays
  ReleaseDoubleArray(out,outptr);
}


/*     nativeThreeDimensional
 */
JNIEXPORT void JNICALL Java_gda_analysis_numerical_fourier_FFTWComplex_nativeThreeDimensional
  (JNIEnv *env, jobject obj, jint width, jint height, jint depth, 
   jdoubleArray in, jdoubleArray out, jint dirn, jint flag){


  double *inptr, *outptr;                     // input and output array pointers
  fftw_plan  plan;                            // the plan

  inptr = GetDoubleArray(in);                // Get input array
  outptr = GetDoubleArray(out);              // get output array

  
  //          Make a simple plan using flag (normall ESTIMATE)
  plan = fftw_plan_dft_3d(depth,height,width, (fftw_complex*)inptr,
                          (fftw_complex*)outptr,dirn,(unsigned int)flag);

  fftw_execute(plan);                       // Do the FFT
  fftw_destroy_plan(plan);                  // Clear up

  ReleaseDoubleArray(in,inptr);             // Release the arrays
  ReleaseDoubleArray(out,outptr);
}
