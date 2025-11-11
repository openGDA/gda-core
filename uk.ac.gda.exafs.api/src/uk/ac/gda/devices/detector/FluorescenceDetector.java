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
	double[][] getMCAData(double time) throws DeviceException;

	/**
	 * @return The number of detector elements (sometimes also called channels but this risks confusion with the
	 * channels which make up the MCA)
	 */
	int getNumberOfElements();

	/**
	 * @return The number of channels in the MCA (Multi-Channel Analyser)
	 */
	int getMCASize();

	/**
	 * @return The maximum number of ROIs per channel
	 */
	int getMaxNumberOfRois();

	/**
	 * Configure the detector using the given parameters
	 *
	 * @param parameters
	 * @throws Exception
	 */
	void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception;

	/**
	 * @return The current detector configuration
	 */
	FluorescenceDetectorParameters getConfigurationParameters();

	/** @return true if detector creates an HDF5 file during a scan.
	 * This is normally created by Epics via the HDF plugin of the area detector.
	 */
	boolean isWriteHDF5Files();

	/**
	 * Set to true to configure the detector to write an HDF5 file during a scan.
	 * @param writeHdfFiles
	 */
	void setWriteHDF5Files(boolean writeHdfFiles);

	/**
	 *
	 * @return An array of 'deadtime correction' factor values (one for each element of the detector)
	 * @throws DeviceException
	 */
	double[] getDeadtimeCorrectionFactors() throws DeviceException;
}
