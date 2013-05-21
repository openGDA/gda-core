/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.vgscienta;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.MotorStatus;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.NXDetectorData;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.impl.ADBaseImpl;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.observable.IObserver;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cosylab.epics.caj.CAJChannel;

@CorbaAdapterClass(DeviceAdapter.class)
@CorbaImplClass(DeviceImpl.class)
public class VGScientaAnalyser extends gda.device.detector.addetector.ADDetector implements MonitorListener, FlexibleFrameDetector, IObserver {
	private static final Logger logger = LoggerFactory.getLogger(VGScientaAnalyser.class);

	private VGScientaController controller;
	private AnalyserCapabilties ac;
	private int[] fixedModeRegion;
	private EpicsController epicsController;
	
	private NDProcess ndProc;
	
	public final static MotorStatus stopped = MotorStatus.READY;
	public final static MotorStatus running = MotorStatus.BUSY;
	private MotorStatus currentstatus = stopped;

	private int[] sweptModeRegion;

	private FlexibleFrameStrategy flex;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		try {
			getNdArray().getPluginBase().enableCallbacks();
			getNdArray().getPluginBase().setNDArrayPort(getNdProc().getPluginBase().getPortName_RBV());
			getNdProc().setEnableFilter(1);
			getNdProc().setAutoResetFilter(0);
			getNdProc().setFilterType(2);
			getNdProc().setNumFilter(1000000);
			getNdProc().getPluginBase().enableCallbacks();
			
			flex = new FlexibleFrameStrategy(getAdBase(), 0., getNdProc()); 
			setCollectionStrategy(flex);
			flex.setMaxNumberOfFrames(1);
			flex.addIObserver(this);
			
			// for updates to GUI
			epicsController = EpicsController.getInstance();
			epicsController.setMonitor(epicsController.createChannel(((ADBaseImpl) getAdBase()).getBasePVName() + ADBase.Acquire_RBV), this);
			
		} catch (Exception e) {
			throw new FactoryException("error setting up areadetector and related listeners ", e);
		}
	}
	
	public AnalyserCapabilties getCapabilities() {
		return ac;
	}

	public void setCapabilities(AnalyserCapabilties ac) {
		this.ac = ac;
	}

	public VGScientaController getController() {
		return controller;
	}

	public void setController(VGScientaController controller) {
		this.controller = controller;
	}

	public int getNumberOfSweeptSteps() throws Exception {
		return controller.getSweepSteps(); 
	}
	
	public double[] getEnergyAxis() throws Exception {
		double start, step;
		int length, startChannel = 0;
		if (controller.getAcquisitionMode().equalsIgnoreCase("Fixed")) {
			int pass = controller.getPassEnergy().intValue();
			start = controller.getCentreEnergy() - (getCapabilities().getEnergyWidthForPass(pass) / 2);
			step = getCapabilities().getEnergyStepForPass(pass) / 1000.0;
			length = getAdBase().getSizeX();
			startChannel = getAdBase().getMinX();
		} else {
			start = controller.getStartEnergy();
			step = controller.getEnergyStep();
			length = getNumberOfSweeptSteps();
		}

		double[] axis = new double[length];
		for (int j = 0; j < length; j++) {
			axis[j] = start + (j+startChannel) * step;
		}
		return axis;
	}

	public double[] getAngleAxis() throws Exception {
		return getCapabilities().getAngleAxis(controller.getLensMode(), getAdBase().getMinY_RBV(),
				getAdBase().getArraySizeY_RBV());
	}

	@Override
	protected void appendDataAxes(NXDetectorData data) throws Exception {
		short state = getAdBase().getDetectorState_RBV();
//		if (currentstatus == running)
//			throw new DeviceException("analyser being read out while acquiring - we do not expect that");
		switch (state) {
			case 6:
				throw new DeviceException("analyser in error state during readout");
			case 1:
				// The IOC can report acquiring for quite a while after being stopped
				logger.debug("analyser status is acquiring during readout although we think it has stopped");
				break;
			case 10:
				logger.warn("analyser in aborted state during readout");
				break;
			default:
				break;
		}
		
		if (getNdProc().getResetFilter_RBV() == 1)
			throw new DeviceException("processing plugin has not seen any data");

		if (firstReadoutInScan) {
			int i = 1;
			String aname = "energies";
			String aunit = "eV";
			double[] axis = getEnergyAxis();

			data.addAxis(getName(), aname, new int[] { axis.length }, NexusFile.NX_FLOAT64, axis, i + 1, 1, aunit,
					false);

			i = 0;
			if ("Transmission".equals(getLensMode())) {
				aname = "location";
				aunit = "mm";
			} else {
				aname = "angles";
				aunit = "degree";
			}
			axis = getAngleAxis();

			data.addAxis(getName(), aname, new int[] { axis.length }, NexusFile.NX_FLOAT64, axis, i + 1, 1, aunit,
					false);

//			<field name="entrance_slit_setting" type="NX_ANY">
//			            <doc>dial setting of the entrance slit</doc>
//			</field>
//			<field name="entrance_slit_size" units="NX_LENGTH">
//			            <doc>size of the entrance slit</doc>
//			</field>

			data.addData(getName(), "lens_mode", new NexusGroupData(getLensMode()), null, null);
			
			data.addData(getName(), "pass_energy", new int[] {1}, NexusFile.NX_INT32, new int[] { getPassEnergy() }, null, null);

			data.addData(getName(), "sensor_size", new int[] {2}, NexusFile.NX_INT32, new int[] { getAdBase().getMaxSizeX_RBV(), getAdBase().getMaxSizeY_RBV() }, null, null);

			data.addData(getName(), "region_origin", new int[] {2}, NexusFile.NX_INT32, new int[] { getAdBase().getMinX_RBV(), getAdBase().getMinY_RBV() }, null, null);

			data.addData(getName(), "region_size", new int[] {2}, NexusFile.NX_INT32, new int[] { getAdBase().getSizeX_RBV(), getAdBase().getSizeY_RBV() }, null, null);
		}
		
		int acquired = flex.getLastAcquired(); 
		data.addData(getName(), "number_of_cycles", new int[] {1}, NexusFile.NX_INT32, new int[] { acquired }, null, null);
	}
	
	@Override
	protected void appendNXDetectorDataFromCollectionStrategy(NXDetectorData data) throws Exception {
			double acquireTime_RBV = getCollectionStrategy().getAcquireTime(); // TODO: PERFORMANCE, cache or listen
			addDoubleItemToNXData(data, "time_per_channel", acquireTime_RBV);
	}
	
	public void setFixedMode(boolean fixed) throws Exception {
		int[] region = fixedModeRegion;
		if (fixed) {
			controller.setAcquisitionMode("Fixed");
		} else {
			controller.setAcquisitionMode("Swept");
			if (sweptModeRegion != null) {
				region = sweptModeRegion;
			}
		}
		getAdBase().setMinX(region[0]);
		getAdBase().setMinY(region[1]);
		getAdBase().setSizeX(region[2]);
		getAdBase().setSizeY(region[3]);
		controller.setSlice(region[3]);
		getAdBase().setImageMode(0);
		getAdBase().setTriggerMode(0);
	}

	public int[] getSWeptModeRegion() {
		return sweptModeRegion;
	}

	public void setSweptModeRegion(int[] sweptModeRegion) {
		this.sweptModeRegion = sweptModeRegion;
	}
	
	public int[] getFixedModeRegion() {
		return fixedModeRegion;
	}

	public void setFixedModeRegion(int[] fixedModeRegion) {
		this.fixedModeRegion = fixedModeRegion;
	}
	
	@Override
	public double getCollectionTime() throws DeviceException {
		try {
			return getAdBase().getAcquireTime();
		} catch (Exception e) {
			throw new DeviceException("error getting collection time", e);
		}
	}
	
	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		try {
			getAdBase().setAcquireTime(collectionTime);
		} catch (Exception e) {
			throw new DeviceException("error setting collection time", e);
		}
	}
	
	public void setLensMode(String value) throws Exception {
		controller.setLensMode(value);
	}
	
	public String getLensMode() throws Exception {
		return controller.getLensMode();
	}
	
	public void setPassEnergy(Integer value) throws Exception {
		controller.setPassEnergy(value);
	}
	
	public Integer getPassEnergy() throws Exception {
			return controller.getPassEnergy();
	}
	
	public void setStartEnergy(Double value) throws Exception {
		controller.setStartEnergy(value);
	}
	
	public Double getStartEnergy() throws Exception {
		return getStartEnergy();
	}

	public void setCentreEnergy(Double value) throws Exception {
		controller.setCentreEnergy(value);
	}
	
	public Double getCentreEnergy() throws Exception {
		return controller.getCentreEnergy();
	}

	public void setEndEnergy(Double value) throws Exception {
		controller.setEndEnergy(value);
	}
	
	public Double getEndEnergy() throws Exception {
		return controller.getEndEnergy();
	}

	public void setEnergyStep(Double value) throws Exception {
		controller.setEnergyStep(value);
	}
	
	public Double getEnergyStep() throws Exception {
		return controller.getEnergyStep();
	}
	
	public void zeroSupplies() throws Exception {
		controller.zeroSupplies();
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		if (((CAJChannel) arg0.getSource()).getName().endsWith(ADBase.Acquire_RBV)) {
			logger.debug("been informed of some sort of change to acquire status");
			DBR_Enum en = (DBR_Enum) arg0.getDBR();
			short[] no = (short[]) en.getValue();
			if (no[0] == 0) {
				logger.info("been informed of a stop");
				currentstatus = stopped;
			} else {
				logger.info("been informed of a start");
				currentstatus = running;
			}
			notifyIObservers(this, currentstatus);
		}
	}

	public NDProcess getNdProc() {
		return ndProc;
	}

	public void setNdProc(NDProcess ndProc) {
		this.ndProc = ndProc;
	}

	@Override
	public int getCurrentFrame() {
		return flex.getCurrentFrame();
	}

	@Override
	public int getMaximumFrame() {
		return flex.getMaxNumberOfFrames();
	}

	@Override
	public void setMaximumFrame(int max) {
		flex.setMaxNumberOfFrames(max);
	}

	@Override
	public void update(Object source, Object arg) {
		if (flex == source) 
			notifyIObservers(source, arg);
	}
}