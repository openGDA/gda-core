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

package uk.ac.gda.devices;

import gda.device.detector.addetector.triggering.SimpleAcquire;
import gda.device.detector.areadetector.v17.ADBase;
import gda.epics.CAClient;
import gda.scan.ScanInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcaliburDacScanCollectionStrategy extends SimpleAcquire{
	private static final Logger logger = LoggerFactory.getLogger(ExcaliburDacScanCollectionStrategy.class);

	String pvPrefix;
	private int dacNumber;
	private int scanStart;
	private int scanStop;
	private int scanStep;
	private boolean started;
	
		public String getPvPrefix() {
		return pvPrefix;
	}
	
	public void setPvPrefix(String pvPrefix) {
		this.pvPrefix = pvPrefix;
	}
	

	
	public int getDacNumber() {
		return dacNumber;
	}

	public void setDacNumber(int dacNumber) {
		this.dacNumber = dacNumber;
	}



	public int getScanStart() {
		return scanStart;
	}

	public void setScanStart(int scanStart) {
		this.scanStart = scanStart;
	}

	public int getScanStop() {
		return scanStop;
	}

	public void setScanStop(int scanStop) {
		this.scanStop = scanStop;
	}

	public int getScanStep() {
		return scanStep;
	}

	public void setScanStep(int scanStep) {
		this.scanStep = scanStep;
	}

	public ExcaliburDacScanCollectionStrategy(ADBase adBase, String pvPrefix) {
		super(adBase, 0.);
		this.pvPrefix = pvPrefix;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		CAClient.put(pvPrefix +":CONFIG:HDF:NumCapture", 0);
		CAClient.put(pvPrefix +":CONFIG:HDF:Capture", 0);
		
		super.prepareForCollection(collectionTime, numImages, scanInfo);
        CAClient.put(pvPrefix +":CONFIG:ACQUIRE:OperationMode","DAC Scan");
        CAClient.put(pvPrefix +":CONFIG:ACQUIRE:ScanDac",dacNumber);
        CAClient.put(pvPrefix +":CONFIG:ACQUIRE:ScanStart",scanStart);
        CAClient.put(pvPrefix +":CONFIG:ACQUIRE:ScanStop",scanStop);
        CAClient.put(pvPrefix +":CONFIG:ACQUIRE:ScanStep",scanStep);
        //ensure divisors of readoutNodes are all set to 1
        CAClient.put(pvPrefix +":1:MASTER:FrameDivisor", 1);
        CAClient.put(pvPrefix +":2:MASTER:FrameDivisor", 1);
        started=false;
	}
        
    @Override
	public void collectData() throws Exception {
		if( !started)
			super.collectData();
		started = true;
	}
    
	@Override
	public void stop() throws Exception {
		logger.error("Stop called for Excalibur but not possible as it will lead to a fault in the FEM comms");
	}
    
}
