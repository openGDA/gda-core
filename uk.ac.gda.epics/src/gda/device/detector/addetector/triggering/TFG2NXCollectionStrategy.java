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

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.timer.Etfg;
import gda.device.timer.Tfg;
import gda.scan.ScanBase;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.List;


public class TFG2NXCollectionStrategy implements NXCollectionStrategyPlugin {

	private static final ArrayList<String> EMPTY_LIST = new ArrayList<String>();
	private Etfg etfg;
	// The port value used to trigger the camera in live mode
	private short exposeTriggerOutVal = 64; // TFG2 USER6 PCO TriggerIn
	private ADBase adBase;
	private String name;

	public short getExposeTriggerOutVal() {
		return exposeTriggerOutVal;
	}

	public void setExposeTriggerOutVal(short exposeTriggerOutVal) {
		this.exposeTriggerOutVal = exposeTriggerOutVal;
	}
	
	
	public TFG2NXCollectionStrategy(ADBase adBase, Etfg etfg) throws Exception {
		this.adBase = adBase;
		if( adBase == null)
			throw new Exception("adBase==null");
		this.etfg = etfg;
		if( etfg == null)
			throw new Exception("etfg==null");
	}
	
	
	public void setName(String name) {
		this.name = name;
	}

	

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		return;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean willRequireCallbacks() {
		return false; //not EPICS areaDetector plugin
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		
	}

	@Override
	public void prepareForLine() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void completeLine() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void completeCollection() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void atCommandFailure() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getInputStreamNames() {
		return EMPTY_LIST;
	}

	@Override
	public List<String> getInputStreamFormats() {
		return EMPTY_LIST;
	}



	@Override
	public double getAcquireTime() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getStatus() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setGenerateCallbacks(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isGenerateCallbacks() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	@Deprecated
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void collectData() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		adBase.stopAcquiring();
		
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
		}			
		adBase.setNumImages(numberImagesPerCollection);
		adBase.setAcquireTime(collectionTime);
		adBase.setAcquirePeriod(collectionTime);
		
	}	

}
