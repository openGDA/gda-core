package uk.ac.gda.devices.detector;

import gda.device.DeviceException;
import gda.factory.Findable;

public interface FluorescenceDetector extends Findable {

	/**
	 * Perform a 'snapshot' data collection and return the MCA data. No file writing is involved.
	 *
	 * @param time The collection time in milliseconds
	 * @return the MCA data as a double array: [detector element][MCA channel]
	 * @throws DeviceException
	 */
	public double[][] getMCAData(double time) throws DeviceException;

	/**
	 * @return The number of detector elements (sometimes also called channels but this risks confusion with the
	 * channels which make up the MCA)
	 */
	public int getNumberOfElements();

	/**
	 * @return The number of channels in the MCA (Multi-Channel Analyser)
	 */
	public int getMCASize();

	/**
	 * @return The maximum number of ROIs per channel
	 */
	public int getMaxNumberOfRois();

	/**
	 * Configure the detector using the given parameters
	 *
	 * @param parameters
	 * @throws Exception
	 */
	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception;

	/**
	 * @return The current detector configuration
	 */
	public FluorescenceDetectorParameters getConfigurationParameters();

}
