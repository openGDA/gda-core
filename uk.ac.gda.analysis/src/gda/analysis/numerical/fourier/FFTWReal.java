// copyright unknown

package gda.analysis.numerical.fourier;

/**
 * Class to implement simple one-off Real to Complex and Complex to RealFourier transforms uwing jfftw as a native
 * library. A new plan is created for each FFT. This is sub-optimal for a large number of FFTs, but reasonable for
 * ``one-off''.
 * 
 * @author Will Hossack, The Univesrity of Edinburgh
 * @version 1.0
 */
public class FFTWReal extends FFTW3 {

	/**
	 * Method to take a one dimensional Forward DFT of a real data array of n elements. The output is Complex, held in a
	 * double array with alternative real/imaginary parts with a total of n/2+1 complex pairs. Note the input and output
	 * arrays are of different length. This method inplements out-of-place transfroms.
	 * 
	 * @param realArray
	 *            the input real array (not changed)
	 * @return double[] holding the DFT
	 */
	public static double[] oneDimensionalForward(double realArray[]) {

		// Make output array of the correct length
		double complexArray[] = new double[2 * (realArray.length / 2 + 1)];
		for (int i = 0; i < complexArray.length; i++) {

		}
		System.out.println("plan flag\t" + getPlanFlag());
		nativeOneDimensionalForward(realArray, complexArray, getPlanFlag());
		return complexArray;
	}

	/**
	 * Private native implement the oneDimensionalForward
	 * 
	 * @param in
	 * @param out
	 * @param flag
	 */
	private static native void nativeOneDimensionalForward(double in[], double out[], int flag);

	/**
	 * Method to take a one dimensional Backward DFT of a complex hermition array to give Real output. Th Complex input
	 * is held as double array with alternative real/imaginary parts with a total of n/2+1 complex pairs. The output is
	 * a double array of n elements. Note the input and output arrays are of different length. This method inplements
	 * out-of-place transfroms.
	 * 
	 * @param complexArray
	 *            array of n/2+1 complex paris
	 * @return double[] real array of n real values
	 */
	public static double[] oneDimensionalBackward(double complexArray[]) {

		// Make real array of the correct length
		double realArray[] = new double[2 * (complexArray.length / 2 - 1)];

		nativeOneDimensionalBackward(complexArray, realArray, getPlanFlag());
		return realArray;
	}

	/**
	 * Private native implement the oneDimensionalForward
	 * 
	 * @param in
	 * @param out
	 * @param flag
	 */
	private static native void nativeOneDimensionalBackward(double in[], double out[], int flag);

	/**
	 * Method to take the two domensional Foward DFT of a real image held in one dimensional double array. the i,j pixel
	 * of the real image is located in array element j*width + i The Complex DFT is returned in a double array with
	 * real/imag parts in even/odd elements, with the i,j Complex components in Real part 2*(j*wft + i) Imag part
	 * 2*(j*wft + i) + 1 where wft = width/2 + 1
	 * 
	 * @param width
	 *            the image width
	 * @param height
	 *            the image height
	 * @param realArray
	 *            the image data in double[] of length width*height
	 * @return double[] DFT packed into one dimensional array
	 */
	public static double[] twoDimensionalForward(int width, int height, double realArray[]) {
		int number = 2 * height * (width / 2 + 1);
		double complexArray[] = new double[number];
		nativeTwoDimensionalForward(width, height, realArray, complexArray, getPlanFlag());
		return complexArray;
	}

	/**
	 * Private native implement the twoDimensionalForward
	 * 
	 * @param width
	 * @param height
	 * @param realArray
	 * @param complexArray
	 * @param flag
	 */
	private static native void nativeTwoDimensionalForward(int width, int height, double realArray[],
			double complexArray[], int flag);

	/**
	 * Method to take the two domensional backword DFT of a hermition complex DFT held in a double array with real/imag
	 * parts in even/odd elements, with the i,j Complex components in Real part 2*(j*wft + i) Imag part 2*(j*wft + i) +
	 * 1 where wft = width/2 + 1 The transformd real data is returned in a one-dimensioal double array with pixel i,j
	 * located at element j*width + i.
	 * 
	 * @param width
	 *            the image width
	 * @param height
	 *            the image height
	 * @param complexArray
	 * @return double[] DFT packed into one dimensional array
	 */
	public static double[] twoDimensionalBackward(int width, int height, double complexArray[]) {
		double realArray[] = new double[width * height];
		nativeTwoDimensionalBackward(width, height, complexArray, realArray, getPlanFlag());
		return realArray;
	}

	/**
	 * Private native implement the twoDimensionalBackward
	 * 
	 * @param width
	 * @param height
	 * @param complexArray
	 * @param realArray
	 * @param flag
	 */
	private static native void nativeTwoDimensionalBackward(int width, int height, double complexArray[],
			double realArray[], int flag);

	/**
	 * Method to take the three dimensional Foward DFT of a real cube held in one dimensional double array. the i,j,k
	 * pixel of the real cube is located in array element k*width*height + j*width + i The Complex DFT is returned in a
	 * double array with real/imag parts in even/odd elements, with the i,j Complex components in : * Real part
	 * 2*(k*wft*height + j*wft + i) Imag part 2*(k*wft*height + j*wft + i) + 1 where wft = width/2 + 1
	 * 
	 * @param width
	 *            the cube width
	 * @param height
	 *            the cube height
	 * @param depth
	 *            the cube depth
	 * @param realArray
	 *            the cube data in double[] of length width*height*depth
	 * @return double[] DFT packed into one dimensional array
	 */
	public static double[] threeDimensionalForward(int width, int height, int depth, double realArray[]) {
		int number = 2 * depth * height * (width / 2 + 1);
		double complexArray[] = new double[number];
		nativeThreeDimensionalForward(width, height, depth, realArray, complexArray, getPlanFlag());
		return complexArray;
	}

	/**
	 * Private native method to implement the three-dimensional FFT
	 * 
	 * @param width
	 * @param height
	 * @param depth
	 * @param realArray
	 * @param complexArray
	 * @param flag
	 */
	private static native void nativeThreeDimensionalForward(int width, int height, int depth, double realArray[],
			double complexArray[], int flag);

	/**
	 * Method to take the three dimensional backword DFT of a hermition complex DFT held in a double array with
	 * real/imag parts in even/odd elements, with the i,j Complex components in : * Real part 2*(k*wft*height + j*wft +
	 * i) Imag part 2*(k*wft*height + j*wft + i) + 1 where wft = width/2 + 1 The transformd real data is returned in a
	 * one-dimensioal double array with pixel i,j located at element k*width*height + j*width + i.
	 * 
	 * @param width
	 *            the cube width
	 * @param height
	 *            the cube height
	 * @param depth
	 *            the cube depth
	 * @param complexArray
	 * @return double[] DFT packed into one dimensional array
	 */
	public static double[] threeDimensionalBackward(int width, int height, int depth, double complexArray[]) {
		int number = depth * height * width;
		double realArray[] = new double[number];
		nativeThreeDimensionalBackward(width, height, depth, complexArray, realArray, getPlanFlag());
		return realArray;
	}

	/**
	 * Private native method to implement the three-dimensional FFT
	 * 
	 * @param width
	 * @param height
	 * @param depth
	 * @param complexArray
	 * @param realArray
	 * @param flag
	 */
	private static native void nativeThreeDimensionalBackward(int width, int height, int depth, double complexArray[],
			double realArray[], int flag);

}
