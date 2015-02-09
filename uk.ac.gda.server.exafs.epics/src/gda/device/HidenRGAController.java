/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device;

import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.io.IOException;
import java.util.Set;

/**
 * Interacts with the Hiden RGA EPICS layer. This is separated from the Hiden Scannable class for testing purposes.
 */
public class HidenRGAController implements IObservable, InitializationListener {

	private class ScanCycleListener implements MonitorListener {
		boolean first = true;
		private int previousValue = 0;

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			if (first) {
				first = false;
				return;
			}
			DBR dbr = arg0.getDBR();

			if (dbr.isINT()) {
				int newValue = ((DBR_Int) dbr).getIntValue()[0];
				if (newValue != previousValue) {
					previousValue = newValue;
					observableComponent.notifyIObservers(this, newValue);
				}
			}
		}

	}

	private final int numberOfMassChannels = 21;

	private String epicsPrefix;
	private ObservableComponent observableComponent = new ObservableComponent();
	private EpicsChannelManager channelManager;
	
	private PV<Integer> writeToRGAPv;
	private PV<Integer> scanCyclesTypePV;
	private PV<Integer> startScanPV;
	private PV<Integer> stopScanPV;
	private PV<Double> currentValvePV;
	private PV<Double> currentTempPV;

	private PV<Integer>[] massSetPVs;
	private PV<Double>[] massReadbackPVs;
	private PV<Double>[] massCountsPerSecPVs;
	private PV<Integer>[] disableMassPVs;

	private int numberOfMasses;
	
	public HidenRGAController(String epicsPrefix) {
		this.epicsPrefix = epicsPrefix;
	}

	@SuppressWarnings("unchecked")
	public void connect() throws CAException {
		// monitoring the scan cycle PV, so do not use Lazy PVs
		channelManager = new EpicsChannelManager(this);
		String scanCyclePVName = generatePVName(":CYC_RBV");
		channelManager.createChannel(scanCyclePVName, new ScanCycleListener(), MonitorType.NATIVE, false);
		
		// rest of connections via LazyPVs
		
		writeToRGAPv = LazyPVFactory.newIntegerPV(generatePVName(":MID:UPLOAD.PROC"));   // set 1 to run procedure
		scanCyclesTypePV = LazyPVFactory.newIntegerPV(generatePVName(":MID:CYC:CONT"));  // set 1 for continuous
		startScanPV = LazyPVFactory.newIntegerPV(generatePVName(":MID:START.PROC"));	// set 1 to run procedure
		stopScanPV = LazyPVFactory.newIntegerPV(generatePVName(":ABORT.PROC"));		// set 1 to run procedure
		currentValvePV = LazyPVFactory.newDoublePV(generatePVName(":P:MIDAUX1-I"));
		currentTempPV = LazyPVFactory.newDoublePV(generatePVName(":P:MIDAUX2-I"));
		
		massSetPVs = new PV[numberOfMassChannels];
		massReadbackPVs = new PV[numberOfMassChannels];
		massCountsPerSecPVs = new PV[numberOfMassChannels];
		disableMassPVs = new PV[numberOfMassChannels-1];  // the first channel cannot be disabled
		
		for (int chan = 1; chan <= numberOfMassChannels; chan++){
			massSetPVs[chan - 1] = LazyPVFactory.newIntegerPV(generatePVName(String.format(":MID:%d:M_SP",chan)));
			massReadbackPVs[chan - 1] = LazyPVFactory.newDoublePV(generatePVName(String.format(":MID:%d:M_RBV",chan)));
			massCountsPerSecPVs[chan - 1] = LazyPVFactory.newDoublePV(generatePVName(String.format(":P:MID%d-I",chan)));
			if (chan > 1){
				disableMassPVs[chan - 2] = LazyPVFactory.newIntegerPV(generatePVName(String.format(":MID:%d:DISABLE",chan)));
			}
		}
	}
	
	private String generatePVName(String suffix) {
		return epicsPrefix + suffix;
	}

	public void setMasses(Set<Integer> masses) throws IOException {

		numberOfMasses = masses.size();
		
		int chan = 1;
		for (Integer mass : masses){
			// for each mass, set the value and enable the channel
			massSetPVs[chan - 1].putNoWait(mass);
			if (chan > 1){
				disableMassPVs[chan - 2].putNoWait(0);
			}
			chan++;
		}
		
		// disable all the other channels
		for(;chan <= numberOfMassChannels; chan++){
			disableMassPVs[chan - 2].putNoWait(1);
		}
	}
	
	protected void setContinuousCycles() throws IOException{
		scanCyclesTypePV.putWait(1);
	}
	
	protected void writeToRGA() throws IOException{
		writeToRGAPv.putWait(1);
	}

	protected void startScan() throws IOException{
		startScanPV.putWait(1);
	}
	
	public void stopScan() throws IOException {
		stopScanPV.putWait(1);
	}

	public double readValve() throws IOException {
		return currentValvePV.get();
	}

	public double readtemp() throws IOException {
		return currentTempPV.get();
	}

	public double[] readout() throws DeviceException {
		try {
			double[] results = new double[numberOfMasses];
			for (int chan = 1; chan <= numberOfMasses; chan++){
				results[chan - 1] = massCountsPerSecPVs[chan - 1].get();
			}
			return results;
		} catch (IOException e) {
			throw new DeviceException("IOException reading Hiden RGA" ,e);
		}
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		// 
	}

}
