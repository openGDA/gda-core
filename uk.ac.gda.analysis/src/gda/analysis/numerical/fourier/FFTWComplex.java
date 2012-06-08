// copyright unknown

package gda.analysis.numerical.fourier;

/**
 * Class to implement simple one-off Complex to Complex Fourier transforms uwing jfftw as a native library. A new plan
 * is created for each FFT. This is sub-optimal for a large number of FFTs, but reasonable for ``one-off''.
 * 
 * @author Will Hossack, The Univesrity of Edinburgh
 * @version 1.0
 */
public class FFTWComplex extends FFTW3 {

	/**
	 * Method to take one-dimensional Complex FFT with the data supplied in a one-dimensional double array with real
	 * parts in even elements, and imaginary in the odd.
	 * 
	 * @param data
	 *            the data to be transformed
	 * @param dirn
	 *            direction +1 for forward, -1 to backward
	 * @param overwrite
	 *            if true data is overwritten with FFT, else a new array is retunded.
	 * @return double[] the FFTed array (if overwrite = true, this will be the same at the input data array.
	 */
	public static double[] oneDimensional(double data[], int dirn, boolean overwrite) {
		double out[] = null; // The output array
		if (overwrite) { // Inplace transform
			out = data;
		} else { // Make new array of same length
			out = new double[data.length];
		}

		nativeOneDimensional(data, out, dirn, getPlanFlag()); // Call native
		// method to do
		// work
		return out; // Return output
	}

	/**
	 * nativeOneDimensional method to do the work
	 * 
	 * @param in
	 * @param out
	 * @param dirn
	 * @param flag
	 */
	private static native void nativeOneDimensional(double in[], double out[], int dirn, int flag);

	/**
	 * Method to take two-dimensional Complex FFT with the data supplied in a one-dimensional double array with real
	 * parts in even elements, and imaginary in the odd. Element i,j is located at Real part 2*(j*width + i) Imag part
	 * 2*(j*width + i) + 1
	 * 
	 * @param width
	 *            the width of the image data
	 * @param height
	 *            the heigh of the image data
	 * @param data
	 *            the data to be transformed
	 * @param dirn
	 *            direction +1 for forward, -1 to backward
	 * @param overwrite
	 *            if true data is overwritten with FFT, else a new array is retunded.
	 * @return double[] the FFTed array (if overwrite = true, this will be the same at the input data array.
	 */

	public static double[] twoDimensional(int width, int height, double data[], int dirn, boolean overwrite) {
		double out[] = null; // The output array
		if (overwrite) { // In place transform
			out = data;
		} else {
			out = new double[data.length]; // Make new output array
		}
		nativeTwoDimensional(width, height, data, out, dirn, getPlanFlag());
		return out;
	}

	/**
	 * nativeTwoDimensional method to do the work
	 * 
	 * @param width
	 * @param height
	 * @param in
	 * @param out
	 * @param dirn
	 * @param flag
	 */
	private static native void nativeTwoDimensional(int width, int height, double in[], double out[], int dirn, int flag);

	/**
	 * Method to take three-dimensional Complex FFT with the data supplied in a one-dimensional double array with real
	 * parts in even elements, and imaginary in the odd. Element (i,j,k) is located at: RealPart 2*(k*width*height +
	 * j*width + i) ImagPart 2*(k*width*height + j*width + i) + 1
	 * 
	 * @param width
	 *            the width of the image data
	 * @param height
	 *            the heigh of the image data
	 * @param depth
	 * @param data
	 *            the data to be transformed
	 * @param dirn
	 *            direction +1 for forward, -1 to backward
	 * @param overwrite
	 *            if true data is overwritten with FFT, else a new array is retunded.
	 * @return double[] the FFTed array (if overwrite = true, this will be the same at the input data array.
	 */

	public static double[] threeDimensional(int width, int height, int depth, double data[], int dirn, boolean overwrite) {
		double out[] = null; // The output array
		if (overwrite) { // In place transform
			out = data;
		} else {
			out = new double[data.length]; // Make new output array
		}
		nativeThreeDimensional(width, height, depth, data, out, dirn, getPlanFlag());
		return out;
	}

	/**
	 * nativeTwoDimensional method to do the work
	 * 
	 * @param width
	 * @param height
	 * @param depth
	 * @param in
	 * @param out
	 * @param dirn
	 * @param flag
	 */
	private static native void nativeThreeDimensional(int width, int height, int depth, double in[], double out[],
			int dirn, int flag);

}
