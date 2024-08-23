/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.mbs;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import uk.ac.diamond.daq.pes.api.AcquisitionMode;
import uk.ac.diamond.daq.pes.api.IElectronAnalyser;
import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;

public class MbsAnalyserClientLiveDataDispatcher extends FindableConfigurableBase implements IObserver,IObservable{
	private static final Logger logger = LoggerFactory.getLogger(MbsAnalyserClientLiveDataDispatcher.class);
	private final ObservableComponent observableComponent = new ObservableComponent();
	private final  EpicsController epicsController = EpicsController.getInstance();
	private LiveDataPlotUpdate dataUpdate = new LiveDataPlotUpdate();
	private IElectronAnalyser analyser;
	private AcquisitionMode acquisitionMode = AcquisitionMode.FIXED;

	private String arrayPV;
	private String frameNumberPV;
	private String acquirePV;
	private String acquisitionModePV;
	private String numScansPV;
	private String progressCounterPV;
	private String numStepsPV;
	private String currentStepPV;

	private Channel numScansChannel;
	private Channel progressCounterPVChannel;
	private Channel arrayChannel;
	private Channel acquisitionModeChannel;
	private Channel numStepsPVChannel;
	private Channel currentStepPVChannel;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		try {
			arrayChannel = epicsController.createChannel(arrayPV);

			final Channel frameNumber = epicsController.createChannel(frameNumberPV);
			epicsController.setMonitor(frameNumber, this::updatedFrameReceived);

			final Channel acquireChannel = epicsController.createChannel(acquirePV);
			epicsController.setMonitor(acquireChannel, this::acquireStatusChanged);

			acquisitionModeChannel = epicsController.createChannel(acquisitionModePV);

			numScansChannel = epicsController.createChannel(numScansPV);
			progressCounterPVChannel = epicsController.createChannel(progressCounterPV);

			numStepsPVChannel = epicsController.createChannel(numStepsPV);
			currentStepPVChannel = epicsController.createChannel(currentStepPV);

		} catch (Exception e) {
			logger.error("Error setting up analyser live visualisation", e);
			throw new FactoryException("Cannot set up monitoring of arrays", e);
		}
		setConfigured(true);
	}

	private void updatedFrameReceived(final MonitorEvent event) {
		try {
			IDataset xAxis = getXAxis();
			IDataset yAxis = getYAxis();
			IDataset ds = getArrayAsDataset(xAxis.getShape()[0], yAxis.getShape()[0]);

			dataUpdate.resetLiveDataUpdate();

			int totalScans = epicsController.cagetInt(numScansChannel);
			int progressScans = epicsController.cagetInt(progressCounterPVChannel);

			switch (acquisitionMode) {
				case AcquisitionMode.SWEPT -> {
					int totalSteps = epicsController.cagetInt(numStepsPVChannel);
					int currentStep = epicsController.cagetInt(currentStepPVChannel);
					dataUpdate.setUpdateSameFrame((progressScans!=0) || (currentStep!=1));

					logger.debug("SWEPT mode");
					logger.debug("ProgressScans: {}, TotalScans: {}",progressScans,totalScans);
					logger.debug("UPDATING SAME FRAME? {}", (progressScans!=0) || (currentStep!=1));
					logger.debug("CurrentStep: {}, TotalSteps: {}",currentStep,totalSteps);
				}
				case AcquisitionMode.FIXED -> {
					dataUpdate.setUpdateSameFrame(progressScans != 1); // this is a bug fix that sometimes instead of max counter ioc returns 0.

					logger.debug("FIXED mode");
					logger.debug("ProgressScans: {}, TotalScans: {}",progressScans,totalScans);
					logger.debug("UPDATING SAME FRAME? {}", (progressScans!=1));
				}
				case AcquisitionMode.DITHER -> {
					dataUpdate.setUpdateSameFrame(progressScans != 1);

					logger.debug("DITHER mode");
					logger.debug("ProgressScans: {}, TotalScans: {}",progressScans,totalScans);
					logger.debug("UPDATING SAME FRAME? {}", (progressScans!=1));
				}
			}

			dataUpdate.setxAxis(xAxis);
			dataUpdate.setyAxis(yAxis);
			dataUpdate.setData(ds);
			dataUpdate.setAcquisitionMode(acquisitionMode);

			notifyListeners(dataUpdate);

		} catch (RejectedExecutionException ree) {
			logger.debug("Plot jobs are queueing up, as expected in certain circumstances, so this one got skipped");
			logger.trace("Exception for rejected execution", ree);
		} catch (Exception e) {
			logger.error("Exception caught preparing analyser live plot", e);
		}
	}

	private void acquireStatusChanged(final MonitorEvent event) {
		logger.trace("Received change of acquire state: {}", event);

		DBR_Enum enumeration = (DBR_Enum) event.getDBR();
		short[] values = (short[]) enumeration.getValue();

		// check mode if acquire started
		if (values[0] == 1) {
			checkAcquisitionMode();
		}
	}

	private void checkAcquisitionMode() {
		try {
			String acquisitionModeString = epicsController.cagetString(acquisitionModeChannel);
			acquisitionMode = AcquisitionMode.valueOf(acquisitionModeString.toUpperCase());
		} catch (TimeoutException | CAException exception) {
			logger.error("Error while checking acquisition mode.", exception);
		} catch (InterruptedException exception) {
			logger.error("Checking acquisition mode was interrupted.", exception);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			logger.error("Error while checking acquisition mode", e);
		}
	}

	private IDataset getArrayAsDataset(int x, int y) throws Exception {
		int arraySize = x*y;
		if (arraySize < 1) {
			throw new IllegalArgumentException(String.format("arraySize was less than 1. x=%d y=%d", x, y));
		}
		// Get as float[] not double[] for performance
		double[] array = epicsController.cagetDoubleArray(arrayChannel, arraySize);
		Dataset newData = DatasetFactory.createFromObject(array, y, x);
		// Flip the data across the 0 position of the Y axis I05-221
		return DatasetUtils.flipUpDown(newData);
	}

	private IDataset getXAxis() throws Exception {
		double[] xdata = analyser.getEnergyAxis();
		IDataset xAxis = DatasetFactory.createFromObject(xdata);
		xAxis.setName("energies (eV)");
		return xAxis;
	}

	private IDataset getYAxis() throws Exception {
		double[] ydata = analyser.getAngleAxis();

		// Flips the Y scale I05-221
		IDataset yAxis = DatasetFactory.createFromObject(ydata);
		IDataset yAxisRev = yAxis.getSliceView(new Slice(null,null,-1));

		if (analyser.getLensMode().toLowerCase().contains("spat")) {
			yAxisRev.setName("location (mm)");
		} else
			yAxisRev.setName("angles (deg)");
		return yAxisRev;
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
	public void update(Object source, Object arg) {
	}

	private void notifyListeners(Object evt) {
		observableComponent.notifyIObservers(this, evt);
	}

	public IElectronAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IElectronAnalyser analyser) {
		this.analyser = analyser;
	}

	public String getArrayPV() {
		return arrayPV;
	}

	public void setArrayPV(String arrayPV) {
		this.arrayPV = arrayPV;
	}

	public String getFrameNumberPV() {
		return frameNumberPV;
	}

	public void setFrameNumberPV(String frameNumberPV) {
		this.frameNumberPV = frameNumberPV;
	}

	public Channel getArrayChannel() {
		return arrayChannel;
	}

	public void setArrayChannel(Channel arrayChannel) {
		this.arrayChannel = arrayChannel;
	}

	public String getAcquirePV() {
		return acquirePV;
	}

	public void setAcquirePV(String acquirePV) {
		this.acquirePV = acquirePV;
	}

	public String getAcquisitionModePV() {
		return acquisitionModePV;
	}

	public void setAcquisitionModePV(String acquisitionModePv) {
		this.acquisitionModePV = acquisitionModePv;
	}

	public String getNumScansPV() {
		return numScansPV;
	}

	public void setNumScansPV(String numScansPV) {
		this.numScansPV = numScansPV;
	}

	public String getProgressCounterPV() {
		return progressCounterPV;
	}

	public void setProgressCounterPV(String progressCounterPV) {
		this.progressCounterPV = progressCounterPV;
	}

	public String getNumStepsPV() {
		return numStepsPV;
	}

	public void setNumStepsPV(String numStepsPV) {
		this.numStepsPV = numStepsPV;
	}

	public String getCurrentStepPV() {
		return currentStepPV;
	}

	public void setCurrentStepPV(String currentStepPV) {
		this.currentStepPV = currentStepPV;
	}

}
