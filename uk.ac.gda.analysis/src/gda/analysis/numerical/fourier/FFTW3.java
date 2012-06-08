// copyright unknown

package gda.analysis.numerical.fourier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Basic class for Java access to the fftw3 library to take Fourier Transform This class set Java package defaults,
 * manages the wisdom information and loads the sharable library. Actual numerical use of this package is via the
 * extending classes.
 * 
 * @author Will Hossack, The University of Edinburgh
 * @version 1.0 Altered by Paul Quinn DLS. I'm using the jfftw lib of Will Hossack as a basis and aim to improve its
 *          basic functionality. For example currently only FFTW.Estimate works as a plan flag The jni interface needs
 *          rewriting to get the other flags to work. I've done this for FFTWRealImp.c nativeOneDimensionalForward and
 *          will get around to implementing and testing this for the other methods. I will also look at rewritting the
 *          interface to store plans to improve performance.
 */

public abstract class FFTW3 extends Object {

	/**
	 * Flag for forward FFT
	 */
	public final static int FORWARD = 1;

	/**
	 * Flag for backward (on inverse) FFT
	 */
	public final static int BACKWARD = -1;

	/**
	 * Flag for exhaustive plan search (very slow)
	 */
	public final static int EXHAUSTIVE = 8; // (1<<3)

	/**
	 * 
	 */
	public final static int MEASURE = 0;

	/**
	 * Flag for patient plan search (medium slow)
	 */
	public final static int PATIENT = 32;// (1 << 5);

	/**
	 * Flag for quick and dirty plan search (fast, but no optimal) Default for jttfw but NOT normnal FFTW default
	 */
	public final static int ESTIMATE = 64;// (1 << 6);

	/**
	 * 
	 */
	public final static int ESTIMATE_PATIENT = 128;// (1 << 6);

	private static int planFlag = FFTW3.ESTIMATE;

	private String systemWisdomFile = "wisdom";

	/**
	 * Method to set the plan flag
	 * 
	 * @param flag
	 *            the plan flag
	 */
	public static void setPlanFlag(int flag) {
		planFlag = flag;
	}

	/**
	 * Method to add to the plan flag
	 * 
	 * @param addition
	 *            the addition to add
	 */
	public static void addPlanFlag(int addition) {
		planFlag += addition;
	}

	/**
	 * Method to get the plan flag
	 * 
	 * @return int the plan flag
	 */
	public static int getPlanFlag() {
		return planFlag;
	}

	/**
	 * Method to reset the system widsom file location. Note this does not load the file.
	 * 
	 * @param fileName
	 *            name of default widsom file
	 */
	public void setSystemWisdom(String fileName) {
		systemWisdomFile = new String(fileName);
	}

	/**
	 * Boolean method to load system widsom file
	 * 
	 * @return boolean true for success, false for failure
	 */
	public boolean loadWisdom() {
		return loadWisdom(new File(systemWisdomFile));
	}

	/**
	 * Boolean method to load wisdom file from specifed File
	 * 
	 * @param file
	 *            the wisdom file
	 * @return boolean true for sucess, false for failure
	 */
	public boolean loadWisdom(File file) {
		return nativeLoadWisdomFromFile(file.getAbsolutePath());
	}

	/**
	 * Boolean method to load (or add) wisdom from a String.
	 * 
	 * @param wisdom
	 *            the wisdom String
	 * @return boolean true for success, false for failure
	 */
	public boolean loadWisdom(String wisdom) {
		return nativeLoadWisdomFromString(wisdom);
	}

	/**
	 * Private native method load wisdom from file
	 * 
	 * @param fileName
	 * @return boolean
	 */
	private native boolean nativeLoadWisdomFromFile(String fileName);

	/**
	 * Private native method load wisdom from String
	 * 
	 * @param wisdom
	 * @return boolean
	 */
	private native boolean nativeLoadWisdomFromString(String wisdom);

	/**
	 * Void method to clear the wisdom information
	 */
	public void clearWisdom() {
		nativeClearWisdom();
	}

	/**
	 * Private native to acually clear the wisdom information
	 */
	private native void nativeClearWisdom();

	/**
	 * Boolean method to export the current wisdom to a file
	 * 
	 * @param file
	 *            the output file
	 * @return boolean true if successful, else false
	 */

	public boolean exportWisdom(File file) {
		return nativeExportWisdomToFile(file.getAbsolutePath());
	}

	/**
	 * Private native method to export wther wisdom to a file
	 * 
	 * @param fileName
	 * @return boolean
	 */
	private native boolean nativeExportWisdomToFile(String fileName);

	/**
	 * @return Method to get the wisdom information as a String
	 */
	public String getWisdom() {
		return nativeGetWisdom();
	}

	/**
	 * Private native method to get the wisdom as a Java string
	 * 
	 * @return Wisdom as a String.
	 */
	private native String nativeGetWisdom();

	/**
	 * Static method to read a Wisdom file into a String
	 * 
	 * @param file
	 *            the wisdom file
	 * @return String the wisdom as a String
	 */
	public static String readWisdom(File file) {

		StringBuilder buffer = new StringBuilder();
		String line;
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			while ((line = in.readLine()) != null) {
				buffer.append(line); // Append the line
				buffer.append('\n'); // Append newline
			}
			in.close();
		} catch (Exception e) {
			System.err.println("FFTW.readWisdom: failed to read : " + file.getName());
		}
		return buffer.toString();
	}

	/**
	 * Static method to write wisdom String to a file
	 * 
	 * @param wisdom
	 *            the wisdom String
	 * @param file
	 *            the output file
	 * @return boolean true is succecssful otherwise false
	 */
	public static boolean writeWisdom(String wisdom, File file) {

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write(wisdom, 0, wisdom.length());
			out.close();
		} catch (Exception e) {
			System.err.println("FFTW.writeWisdom: failed to write : " + file.getName());
			return false;
		}
		return true;
	}

	/**
	 * Force the shared library to be loaded here
	 */

	static {
		System.loadLibrary("jfftw3");
	}
}
