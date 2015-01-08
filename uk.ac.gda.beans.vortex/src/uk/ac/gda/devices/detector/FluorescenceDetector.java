package uk.ac.gda.devices.detector;

import gda.device.Detector;
import gda.device.DeviceException;

public interface FluorescenceDetector extends Detector {

	/**
	 * Perform a 'snapshot' data collection and return the MCAs. No file writing
	 * is involved.
	 * 
	 * @param time
	 * @return
	 * @throws DeviceException
	 */
	public double[][] getMCData(double time) throws DeviceException;

	public int[][] getData() throws DeviceException;

	public Object getCountRates() throws DeviceException;

	public String getConfigFileName();

	/**
	 * Give the detector the XML file containing the parameters it should load.
	 * 
	 * @param configFileName
	 */
	public void setConfigFileName(String configFileName);

	/**
	 * Configure the detector using the parameters in the XML file given though
	 * the {@link #setConfigFileName(String)} method.
	 * 
	 * @throws Exception
	 */
	public void loadConfigurationFromFile() throws Exception;
}
