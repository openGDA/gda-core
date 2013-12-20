/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.timer.Etfg;
import gda.device.timer.Tfg;
import gda.scan.ScanBase;
import gda.scan.ScanInformation;


public class TFG2NXCollectionStrategy extends HardwareTriggeredStandard {

	private Etfg etfg;
	// The port value used to trigger the camera in live mode
	private short exposeTriggerOutVal = 64; // TFG2 USER6 PCO TriggerIn

	public short getExposeTriggerOutVal() {
		return exposeTriggerOutVal;
	}

	public void setExposeTriggerOutVal(short exposeTriggerOutVal) {
		this.exposeTriggerOutVal = exposeTriggerOutVal;
	}
	
	
	public TFG2NXCollectionStrategy(ADBase adBase, double readoutTime, Etfg etfg) throws Exception {
		super(adBase, readoutTime);
		this.etfg = etfg;
		if( etfg == null)
			throw new Exception("etfg==null");
	}
	
	
	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		getAdBase().stopAcquiring();
		
		etfg.stop();
		etfg.getDaServer().sendCommand("tfg setup-trig ttl0 debounce 1.0e-6");
		etfg.setAttribute(Tfg.EXT_START_ATTR_NAME, false);
		etfg.setAttribute(Tfg.EXT_INHIBIT_ATTR_NAME, false);
		etfg.setAttribute(Tfg.VME_START_ATTR_NAME, true);
		etfg.setAttribute(Tfg.AUTO_CONTINUE_ATTR_NAME, false);
		etfg.setAttribute(Tfg.AUTO_REARM_ATTR_NAME, false);

		etfg.clearFrameSets(); 
		etfg.addFrameSet(1, 0.0, 0.0, 0, 0, 0, 8); //leave dead time on ttl0
		etfg.addFrameSet(1, 0.0, collectionTime * 1000., 0, exposeTriggerOutVal, 0, 0); //in live output trigger to camera
//		etfg.addFrameSet(1, 0.0, 0.0, 0, 0, 0, noLongerBusyTriggerInVal); // wait for PCo Busy out
		etfg.setCycles(2);
		etfg.loadFrameSets();		
		etfg.start();
		while (etfg.getStatus() != 2) {
			Thread.sleep(50);
			ScanBase.checkForInterrupts();
		}			
		super.prepareForCollection(collectionTime, numImages,scanInfo);
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		getAdBase().setAcquirePeriod(0.0);
		getAdBase().setAcquireTime(collectionTime);
	}	
	
	@Override
	public void collectData() throws Exception {
		super.collectData();
		Thread.sleep(2000); // without this the first trigger seems to be ignored			
	}	

}
