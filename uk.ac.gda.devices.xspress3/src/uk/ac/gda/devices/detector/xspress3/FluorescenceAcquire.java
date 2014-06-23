package uk.ac.gda.devices.detector.xspress3;

import gda.device.Detector;
import gda.device.DeviceException;

public interface FluorescenceAcquire extends Detector{

	public void clearAndStart() throws DeviceException;
	
	public void stop() throws DeviceException;

	public int[][] getData() throws DeviceException;
	
	public Object getCountRates() throws DeviceException;
	
}
