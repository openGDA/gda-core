#include "jnimacros.h"
#include "gda_analysis_numerical_fourier_FFTWReal.h"
#include <fftw3.h>
#include <stdio.h>


/*               nativeOneDimensionalForward
 */

JNIEXPORT void JNICALL Java_gda_analysis_numerical_fourier_FFTWReal_nativeOneDimensionalForward
(JNIEnv *env, jobject obj, jdoubleArray in, jdoubleArray out, jint flag){

  
  double *inptr,*inptr2, *outptr;                     // input and output array pointers  
  int length,i;                                 // Transform length
  fftw_plan  plan;                            // the plan

  inptr  = GetDoubleArray(in);                // Get input array
  inptr2 = GetDoubleArray(in);                // Get input array
  outptr = GetDoubleArray(out);               // get output array
  
  length = ArrayLength(in);                  // Length of transform

  plan = fftw_plan_dft_r2c_1d(length,inptr,(fftw_complex*)outptr,
			      (unsigned int)flag);
 
  // pdq plan overwrites the input array if the flag is anything other than measure
  // so you have to reset the data.  Doing a getdoublearray(inptr) or inptr = inptr2
  // doesn't work. So this is a bit wasteful....  
  
  for (i=0; i<length; i++) {
    *inptr++ = *inptr2++;
  }
    

  fftw_execute(plan);                       // Do the FFT
  
    
  ReleaseDoubleArray(in,inptr);             // Release the arrays
  ReleaseDoubleArray(out,outptr);
  // destroy the plan
  fftw_destroy_plan(plan);                  // Clear up
//  fftw_free(inptr);
//  fftw_free(outptr);
}


/*               nativeOneDimensionalBackward
 */

JNIEXPORT void JNICALL Java_gda_analysis_numerical_fourier_FFTWReal_nativeOneDimensionalBackward
(JNIEnv *env, jobject obj, jdoubleArray in, jdoubleArray out, jint flag){

  
  double *inptr, *outptr;                     // input and output array pointers  
  int length;                                 // Transform length
  fftw_plan  plan;                            // the plan

  inptr = GetDoubleArray(in);                // Get input array
  outptr = GetDoubleArray(out);              // get output array
  length = ArrayLength(out);                  // Length of transform (output)

  plan = fftw_plan_dft_c2r_1d(length,(fftw_complex*)inptr,outptr,
			      (unsigned int)flag);

  
  fftw_execute(plan);                       // Do the FFT
  fftw_destroy_plan(plan);                  // Clear up

  ReleaseDoubleArray(in,inptr);             // Release the arrays
  ReleaseDoubleArray(out,outptr);
}

/*                            twoDimensionaForward
 */
JNIEXPORT void JNICALL Java_gda_analysis_numerical_fourier_FFTWReal_nativeTwoDimensionalForward
(JNIEnv *env , jobject obj, jint width, jint height, jdoubleArray in, jdoubleArray out, jint flag){

  
  double *inptr, *outptr;                     // input and output array pointers  
  fftw_plan  plan;                            // the plan

  inptr = GetDoubleArray(in);                // Get input array
  outptr = GetDoubleArray(out);              // get output array

  
  //                    Make the two-d plan
  plan = fftw_plan_dft_r2c_2d(height,width,inptr,
			      (fftw_complex*)outptr,
			      (unsigned int)flag);
  
  
  fftw_execute(plan);                       // Do the FFT
  fftw_destroy_plan(plan);                  // Clear up

  ReleaseDoubleArray(in,inptr);             // Release the arrays
  ReleaseDoubleArray(out,outptr);
}


/*                            twoDimensionaBackward
 */
JNIEXPORT void JNICALL Java_gda_analysis_numerical_fourier_FFTWReal_nativeTwoDimensionalBackward
(JNIEnv *env , jobject obj, jint width, jint height, jdoubleArray in, jdoubleArray out, jint flag){

  
  double *inptr, *outptr;                     // input and output array pointers  
  fftw_plan  plan;                            // the plan

  inptr = GetDoubleArray(in);                // Get input array
  outptr = GetDoubleArray(out);              // get output array

  
  //                    Make the two-d plan
  plan = fftw_plan_dft_c2r_2d(height,width,(fftw_complex*)inptr,
			      outptr,(unsigned int)flag);
  
  
  fftw_execute(plan);                       // Do the FFT
  fftw_destroy_plan(plan);                  // Clear up

  ReleaseDoubleArray(in,inptr);             // Release the arrays
  ReleaseDoubleArray(out,outptr);
}


/*                         threeDimensionalForward
 */
JNIEXPORT void JNICALL Java_gda_analysis_numerical_fourier_FFTWReal_nativeThreeDimensionalForward
(JNIEnv *env, jobject obj, jint width, jint height, jint depth, 
 jdoubleArray in, jdoubleArray out, jint flag) {

  double *inptr, *outptr;                    // input and output array pointers
  fftw_plan plan;                            // the plan

  
  inptr = GetDoubleArray(in);                // Get input array
  outptr = GetDoubleArray(out);              // get output array

  
  //                    Make the three-d plan
  plan = fftw_plan_dft_r2c_3d(depth,height,width,inptr,
			      (fftw_complex*)outptr,
			      (unsigned int)flag);
  
  
  fftw_execute(plan);                       // Do the FFT
  fftw_destroy_plan(plan);                  // Clear up

  ReleaseDoubleArray(in,inptr);             // Release the arrays
  ReleaseDoubleArray(out,outptr);
}

/*                            threeDimensionaBackward
 */
JNIEXPORT void JNICALL Java_gda_analysis_numerical_fourier_FFTWReal_nativeThreeDimensionalBackward
(JNIEnv *env , jobject obj, jint width, jint height, jint depth, jdoubleArray in, jdoubleArray out, jint flag){

  
  double *inptr, *outptr;                     // input and output array pointers  
  fftw_plan  plan;                            // the plan

  inptr = GetDoubleArray(in);                // Get input array
  outptr = GetDoubleArray(out);              // get output array

  
  //                    Make the two-d plan
  plan = fftw_plan_dft_c2r_3d(depth,height,width,(fftw_complex*)inptr,
			      outptr,(unsigned int)flag);
  
  
  fftw_execute(plan);                       // Do the FFT
  fftw_destroy_plan(plan);                  // Clear up

  ReleaseDoubleArray(in,inptr);             // Release the arrays
  ReleaseDoubleArray(out,outptr);
}




