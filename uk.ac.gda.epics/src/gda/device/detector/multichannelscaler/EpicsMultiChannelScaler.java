/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.multichannelscaler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.MCAStatus;
import gda.device.detector.DetectorBase;
import gda.device.enumpositioner.EpicsSimpleMbbinary;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gov.aps.jca.Channel;

/**
 * A Multi-channel scaler class that implements the Detector interface in GDA framework.
 * Apart from implementing framework API, it also contains a map object that maps
 * scaler channel and detector name label to MCA count array. An integer value is
 * used for the map key so the order of detector outputs from totally independent detectors
 * can be easily sorted in output.
 */
public class EpicsMultiChannelScaler extends DetectorBase implements Configurable, Findable, Detector, IObserver,
		EpicsMcsSis3820 {

	private static final Logger logger = LoggerFactory.getLogger(EpicsMultiChannelScaler.class);

	private EpicsDlsMcsSis3820Controller controller;
	private EpicsSimpleMbbinary fastshutter = null;

	public EpicsSimpleMbbinary getFastshutter() {
		return fastshutter;
	}

	public void setFastshutter(EpicsSimpleMbbinary fastshutter) {
		this.fastshutter = fastshutter;
	}

	private String controllerName;

	private double elapsedRealTime = 0;
	private boolean shutterOpened=false;

	public boolean isShutterOpened() {
		return shutterOpened;
	}

	public void setShutterOpened(boolean shutterOpened) {
		this.shutterOpened = shutterOpened;
	}

	// private ArrayList<DataMonitor> dmls = new ArrayList<DataMonitor>();
	private int[][] data;

	int status = Detector.IDLE;

	// DataMonitor[] dm;
	private Map<Integer, Mca> mcaList = new LinkedHashMap<Integer, Mca>();

	/**
	 * @return mcaList
	 */
	public Map<Integer, Mca> getMcaList() {
		return mcaList;
	}

	/**
	 * @param itemlist
	 */
	public void setMcaList(Map<Integer, Mca> itemlist) {
		this.mcaList = itemlist;
	}

	/**
	 *
	 */
	public EpicsMultiChannelScaler() {
		// dm = new DataMonitor[MAX_NUMBER_MCA];
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (controller == null) {
				if ((controller = (EpicsDlsMcsSis3820Controller) Finder.getInstance().find(controllerName)) != null) {
					logger.debug("controller {} found", controllerName);

				} else {
					logger.error("EpicsDlsMcsSis3820Controller {} not found", controllerName);
					throw new FactoryException("EpicsDlsMcsSis3820Controller " + controllerName + " not found");
				}
			}
			controller.addIObserver(this);
			/*
			 * for (int i = 0; i < MAX_NUMBER_MCA; i++) { dmls.set(i, dm[i]); try { controller.addDataMonitor(i, dm[i]); }
			 * catch (DeviceException e) { throw new FactoryException( "Can not add data Monitor to mca", e); } }
			 */
			configured = true;
		}
	}

	@Override
	public void atPointStart() throws DeviceException {
		if(!shutterOpened) {
			openShutter();
		}
	}
	public void openShutter() throws DeviceException {
		if (fastshutter != null) {
			try {
				fastshutter.moveTo("OPEN");
				shutterOpened=true;
			} catch (DeviceException e) {
				throw e;
			}
		}
	}
	@Override
	public void atPointEnd() throws DeviceException {
		if(shutterOpened) {
			closeShutter();
		}
	}
	public void closeShutter() throws DeviceException{
		if (fastshutter != null) {
			try {
				fastshutter.moveTo("CLOSE");
				shutterOpened=false;
			} catch (DeviceException e) {
				throw e;
			}
		}
	}
	@Override
	public void collectData() throws DeviceException {
		JythonServerFacade.getInstance().print("start the detector " + getName());
		if (fastshutter != null && fastshutter.getPosition().toString().equalsIgnoreCase("CLOSE")) {
			fastshutter.moveTo("OPEN");
		}
		controller.eraseStart();

	}

	@Override
	public int getStatus() throws DeviceException {
		return status;
	}

	/**
	 * @return status from epics
	 */
	public int getStatusFromEpics() {
		int ds = -1;
		MCAStatus s = controller.getStatus();
		if (s == MCAStatus.BUSY) {
			ds = Detector.BUSY;
		} else if (s == MCAStatus.FAULT) {
			ds = Detector.FAULT;
		} else if (s == MCAStatus.READY) {
			ds = Detector.IDLE;
		}
		return ds;
	}

	@Override
	public void stop() throws DeviceException {
		controller.stop();
		if (fastshutter != null && fastshutter.getPosition().toString().equalsIgnoreCase("OPEN")) {
			fastshutter.moveTo("CLOSE");
		}
	}

	@Override
	public Object readout() throws DeviceException {
		return controller.getData();
	}

	/**
	 * @param channel
	 * @return data
	 * @throws DeviceException
	 */
	public int[] readout(int channel) throws DeviceException {
		return controller.getData(channel);
	}

	@Override
	public int[][] getData() throws DeviceException {
		return controller.getData();
	}

	@Override
	public int[] getData(int channel) throws DeviceException {
		return controller.getData(channel);
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		int[] dims = { EpicsDlsMcsSis3820Controller.MAXIMUM_NUMBER_OF_MCA,
				EpicsDlsMcsSis3820Controller.MAXIMUM_NUMBER_BINS };
		return dims;
	}
	/**
	 * gets the controller object
	 * @return the controller object
	 */
	public EpicsDlsMcsSis3820Controller getController() {
		return controller;
	}
	/**
	 * sets the controller object
	 * @param controller
	 */
	public void setController(EpicsDlsMcsSis3820Controller controller) {
		this.controller = controller;
	}

	/**
	 * @return Controller Name
	 */
	public String getControllerName() {
		return controllerName;
	}

	/**
	 * @param controllerName
	 */
	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}

	@Override
	public double getElapsedTime() throws DeviceException {
		if (controller.isPollElapsedRealTime()) {
			elapsedRealTime=controller.getRealTime();
		}
		return elapsedRealTime;
	}

	@Override
	public double getElapsedTimeFromEpics() throws DeviceException {
		return controller.getRealTime();
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		controller.stop();
		controller.erase();
		controller.setBinAdv("External");
		controller.setTotalTime(0);
	}


	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved == EpicsDlsMcsSis3820Controller.AcquisitionProperty.ELAPSEDTIME) {
			elapsedRealTime = Double.parseDouble(changeCode.toString());
			//logger.debug("Elapsed Time of {} is {}", getName(), elapsedRealTime);
		} else if (theObserved instanceof Channel) {
			//#TODO remove? - not used as no data monitor is added in configure().
			for (int i = 0; i < EpicsDlsMcsSis3820Controller.MAXIMUM_NUMBER_OF_MCA; i++) {
				if ((Channel) theObserved == controller.getDataChannel(i)) {
					data[i] = (int[]) changeCode;
					logger.debug("data update is called on mca {}", i);
					notifyIObservers(this, data[i]);
				}
			}

		} else if (theObserved == EpicsDlsMcsSis3820Controller.AcquisitionProperty.STATUS) {
			if ((MCAStatus) changeCode == MCAStatus.BUSY) {
				status = Detector.BUSY;
			} else if ((MCAStatus) changeCode == MCAStatus.READY) {
				status = Detector.IDLE;
			} else if ((MCAStatus) changeCode == MCAStatus.FAULT) {
				status = Detector.FAULT;
			}
			//logger.debug("MCS detector {} status update to {}", getName(), status);
		}
	}


	@Override
	public void endCollection() throws DeviceException {
		stop();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "EPICS multi channel scaler";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "EPICS-MCS";
	}

}
