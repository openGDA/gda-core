/*-
 * Copyright © 2010 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.device.detector.countertimer;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.factory.FactoryException;

import org.apache.commons.lang.ArrayUtils;

/**
 * For B18 Gas Micro-Strip detector (GMSD) running in step scans.
 * <p>
 * This detector is driven by TTL inputs from the TFG, but is readout via a Struck scaler driven through an EPICS Scaler
 * template.
 */
public class TFGStruck extends TFGCounterTimer implements Detector {

	private Detector struck;

	public TFGStruck() {
	}

	@Override
	public void configure() throws FactoryException {
		// don't call super.configure()
		setInputNames(new String[]{"time"});
		setExtraNames(new String[]{"gmsd0","gmsd1","gmsd2","gmsd3","gmsd4","gmsd5","gmsd6","gmsd7"}); 
		setOutputFormat(new String[]{"%.2f","%.5g","%.5g","%.5g","%.5g","%.5g","%.5g","%.5g","%.5g"});
	}

	@Override
	public int getStatus() throws DeviceException {
		return timer.getStatus();
	}

	@Override
	public double[] readout() throws DeviceException {
		double[] data =  (double[]) struck.readout(); 
		
		return ArrayUtils.subarray(data, 1, 9);
	}

	@Override
	public void collectData() throws DeviceException {
		if (struck != null) {
			// tell the EpicsScaler record to start collecting - the hardware should be configured to simply wait until
			// the TFG veto signal arrives
			struck.stop();
			struck.collectData();
		}
		if (!slave && timer != null) {
			// if not in slave mode then drive the TFG from here
			timer.countAsync(collectionTime);
		}
	} 


	public Detector getStruck() {
		return struck;
	}

	public void setStruck(Detector struck) {
		this.struck = struck;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Tfg Struck";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Scaler";
	}

	// below methods are for framing and I'll deal with them later...

	@Override
	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

}
