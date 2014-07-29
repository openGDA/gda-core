/**
 * 
 */
package uk.ac.gda.devices.detector.xspress3;

import gda.device.DeviceException;
import gda.observable.IObservable;

/**
 * @author dfq16044
 *
 */
public interface FluorescenceAcquireProcessor extends IObservable {
/*
	public void clearAndStart() throws DeviceException;
	
	public void stop() throws DeviceException;*/
	
	public Double[][] getMCData(double time)throws DeviceException; 

	public int[][] getData() throws DeviceException;
	
	public Object getCountRates() throws DeviceException;
	
}
