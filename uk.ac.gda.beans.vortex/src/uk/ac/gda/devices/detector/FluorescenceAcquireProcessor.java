/**
 * 
 */
package uk.ac.gda.devices.detector;

import gda.device.DeviceException;
import gda.observable.IObservable;

/**
 * This interface describes the expected functionality of fluorescence detectors
 * outside of scans.
 * <p>
 * TODO implement this interface over all Spectroscopy fluo detectors.
 * 
 */
public interface FluorescenceAcquireProcessor extends IObservable {

	/**
	 * Perform a 'snapshot' data collection and return the MCAs. No file writing
	 * is involved.
	 * 
	 * @param time
	 * @return
	 * @throws DeviceException
	 */
	public Double[][] getMCData(double time) throws DeviceException;

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
