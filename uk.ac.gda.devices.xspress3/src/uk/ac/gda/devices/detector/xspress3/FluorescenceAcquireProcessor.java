package uk.ac.gda.devices.detector.xspress3;
import gda.device.DeviceException;

public interface FluorescenceAcquireProcessor {
	public void clearAndStart() throws DeviceException;
	
	public void stop() throws DeviceException;
	
	public double[][] getMCData(double time)throws DeviceException; 
	
	public int[][] getData() throws DeviceException;
	
	public Object getCountRates() throws DeviceException;
	
	public String getConfigFileName();
	
	public void setConfigFileName(String configFileName);
	
	public void loadConfigurationFromFile() throws Exception;
}
