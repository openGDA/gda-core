package uk.ac.gda.devices.detector;

import gda.device.Detector;
import gda.device.DeviceException;
import uk.ac.gda.beans.DetectorROI;

public interface FluorescenceDetector extends Detector {

	/**
	 * Perform a 'snapshot' data collection and return the MCAs. No file writing
	 * is involved.
	 * 
	 * @param time
	 * @return
	 * @throws DeviceException
	 */
	public int[][] getMCData(double time) throws DeviceException;

//	public int[][] getData() throws DeviceException;

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
	
	public DetectorROI[] getRegionsOfInterest() throws DeviceException;
	
	public void setRegionsOfInterest(DetectorROI[] regionList) throws DeviceException;

	/**
	 * @return The number of elements/detector channels
	 */
	public int getNumberOfChannels();

	public int getMCASize();

	/**
	 * Configure the detector using the given parameters object.
	 * 
	 * @param parameters
	 * @throws Exception
	 */
	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception;
	
	public Class <? extends FluorescenceDetectorParameters> getConfigurationParametersClass();
	
	public FluorescenceDetectorParameters getConfigurationParameters();

}
