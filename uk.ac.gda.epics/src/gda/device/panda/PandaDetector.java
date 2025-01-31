/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package gda.device.panda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.dawnsci.nexus.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.panda.PandaController.SeqTableState;
import gda.device.panda.PandaController.SequenceTableRow;
import gda.device.panda.PandaController.SequenceTableTimeUnits;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;

public class PandaDetector extends DetectorBase {
	private final Logger logger = LoggerFactory.getLogger(PandaDetector.class);
	private PandaController controller;
	private static final String ZERO = "ZERO";
	private static final String ONE = "ONE";

	private boolean useHdfWriter;
	private boolean readSwmrFile;

	private PandaHdfReader hdfReader = new PandaHdfReader();

	private String hdfFilenameTemplate = "panda_capture.hdf";

	private SequenceTableTimeUnits tablePrescaleUnits = SequenceTableTimeUnits.MILLISEC;
	private double tablePrescaleValue = 1.0;

	// List of number of repetitons, frame times to be used
	private List<Entry<Integer,Double>> customFrameLengths = Collections.emptyList();

	private SequenceTableRow tableRowTemplate = new SequenceTableRow();
	private int numScanPoints;
	private double triggerSwitchTimeSecs = 0.2;
	private int totalFramesCollected;

	public PandaDetector() {
		setInputNames(new String[] {});
	}

	@Override
	public void configure() {
		Objects.requireNonNull(controller, "Controller has not been set on "+getName());
		if (!controller.isConfigured()) {
			configure();
		}
		setConfigured(true);
	}

	@Override
	public void reconfigure() throws FactoryException {
		setConfigured(false);
		controller.reconfigure();
		configure();
	}

	@Override
	public void atScanStart() throws DeviceException {
		// get the shape of the scan
		numScanPoints = 1;
		var scanInfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		if (scanInfo != null) {
			numScanPoints = scanInfo.getNumberOfPoints();
		}
		controller.setSeqBitA(ZERO);
		controller.setPCapArm(0);

		if (useHdfWriter) {
			String directory = InterfaceProvider.getPathConstructor().getDefaultDataDir();
			String fileName = hdfFilenameTemplate;
			if (scanInfo != null) {
				int scanNumber = scanInfo.getScanNumber();
				fileName = String.format(hdfFilenameTemplate, scanNumber);
			}
			controller.setHdfCapture(0);
			controller.setupHdfWriter(directory, fileName);
		}

		controller.setSeqTablePrescale(tablePrescaleUnits, tablePrescaleValue);

		// List specifying collection times to be used
		// key=number of repetitions, value=time (seconds)
		List< Entry<Integer, Double> > repsTimes = new ArrayList<>();

		// default behaviour : frame length is same for each point in the scan
		repsTimes.add(Map.entry(numScanPoints, getCollectionTime()));

		// see if custom frame lengths have been set and are suitable to use :
		if (!customFrameLengths.isEmpty()) {
			int totNumFrames = customFrameLengths.stream().mapToInt(Entry::getKey).sum();
			if (totNumFrames != numScanPoints) {
				logger.warn("Number of specified frames does not match number of scan points");
			}

			// too many frames is ok, we just don't use the extra ones
			if (totNumFrames >= numScanPoints) {
				logger.info("Using specified frame times for scan");
				repsTimes = customFrameLengths;
			}
		}

		List<SequenceTableRow> seqTableRows = new ArrayList<>();
		for( var timeRep : repsTimes) {
			SequenceTableRow tableRow = new SequenceTableRow(tableRowTemplate);
			int time = (int) (tablePrescaleUnits.convertFromSeconds(timeRep.getValue())); // convert time seconds into sequence table time units
			tableRow.setTime1(time);
			tableRow.setRepeats(timeRep.getKey());
			seqTableRows.add(tableRow);
		}
		controller.setSeqTableRows(seqTableRows);

		controller.setHdfCapture(1);

		controller.setPCapArm(1);

		totalFramesCollected = 0;
	}

	@Override
	public void atScanEnd() throws DeviceException {

		if (useHdfWriter && controller.getHdfCapture() == 1) {
			logger.info("Waiting for Hdf writer to finish");
			if (!controller.waitForHdfNumCaptured(numScanPoints)) {
				logger.warn("Problem waiting for Hdf writer frames to be flushed : expected {} frames, but received {}",
						numScanPoints, controller.getHdfNumCaptured());
			}
			logger.info("Stopping Hdf writer");
			controller.setHdfCapture(0);
		}

		logger.info("Stopping position capture");
		controller.setPCapArm(0);
	}

	@Override
	public void stop() throws DeviceException {
		// stop the hdf writer (don't wait for frame to be flushed)
		if (useHdfWriter) {
			controller.setHdfCapture(0);
		}
		atScanEnd();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		stop();
	}

	@Override
	public void collectData() throws DeviceException {
		controller.setSeqBitA(ONE);
		try {
			Thread.sleep((long)(1000*triggerSwitchTimeSecs));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Interrupted while sleeping between trigger switches", e);
		}
		controller.setSeqBitA(ZERO);
	}

	@Override
	public int getStatus() throws DeviceException {
		SeqTableState tableState = controller.getSeqTableState();
		logger.debug("Sequence table state : {}", tableState);
		if (tableState == SeqTableState.WAIT_ENABLE || tableState == SeqTableState.WAIT_TRIGGER) {
			return Detector.IDLE;
		}
		return Detector.BUSY;
	}

	@Override
	public String[] getExtraNames() {
		if (readSwmrFile && useHdfWriter) {
			return hdfReader.getOutputNames();
		} else {
			return new String[]{"frame_number"};
		}
	}

	@Override
	public String[] getOutputFormat() {
		if (readSwmrFile && useHdfWriter) {
			return hdfReader.getOutputFormat();
		} else {
			return new String[] {"%d"};
		}
	}

	@Override
	public Object readout() throws DeviceException {
		totalFramesCollected++;
		if (useHdfWriter && readSwmrFile) {
			return readHdfData(totalFramesCollected);
		}
		// Return total frames collected
		return totalFramesCollected;
	}

	private double[] readHdfData(int frameNumber) throws DeviceException {
		if (!controller.waitForHdfNumCaptured(frameNumber)) {
			logger.warn("problem waiting for Hdf writer flush frames : expected {} frames, but received {}", frameNumber, controller.getHdfNumCaptured());
		}

		if (frameNumber==1) {
			//setup swmr filereader
			String filename=controller.getHdfFileFullPath();
			logger.info("Setting up Swmr file reader for {}", filename);
			hdfReader.setFilename(filename);
			hdfReader.connect();
		}
		try {
			return hdfReader.readData(frameNumber);
		}catch(NexusException e) {
			throw new DeviceException("Problem read data from "+hdfReader.getFilename(), e);
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	public PandaController getController() {
		return controller;
	}

	public void setController(PandaController controller) {
		this.controller = controller;
	}

	public boolean isUseHdfWriter() {
		return useHdfWriter;
	}

	public void setUseHdfWriter(boolean useHdfWriter) {
		this.useHdfWriter = useHdfWriter;
	}

	public boolean isReadSwmrFile() {
		return readSwmrFile;
	}

	public void setReadSwmrFile(boolean readSwmrFile) {
		this.readSwmrFile = readSwmrFile;
	}

	public PandaHdfReader getPandaHdfReader() {
		return hdfReader;
	}

	public void setPandaHdfReader(PandaHdfReader pandaHdfReader) {
		this.hdfReader = pandaHdfReader;
	}

	public String getHdfFilenameTemplate() {
		return hdfFilenameTemplate;
	}

	public void setHdfFilenameTemplate(String hdfFilenameTemplate) {
		this.hdfFilenameTemplate = hdfFilenameTemplate;
	}

	public SequenceTableTimeUnits getTablePrescaleUnits() {
		return tablePrescaleUnits;
	}

	public void setTablePrescaleUnits(SequenceTableTimeUnits tablePrescaleUnits) {
		this.tablePrescaleUnits = tablePrescaleUnits;
	}

	public double getTablePrescaleValue() {
		return tablePrescaleValue;
	}

	public void setTablePrescaleValue(double tablePrescaleValue) {
		this.tablePrescaleValue = tablePrescaleValue;
	}

	public SequenceTableRow getTableRowTemplate() {
		return tableRowTemplate;
	}

	public void setTableRowTemplate(SequenceTableRow tableRowTemplate) {
		this.tableRowTemplate = tableRowTemplate;
	}

	public double getTriggerSwitchTimeSecs() {
		return triggerSwitchTimeSecs;
	}

	public void setTriggerSwitchTimeSecs(double triggerSwitchTimeSecs) {
		this.triggerSwitchTimeSecs = triggerSwitchTimeSecs;
	}

	public List<Entry<Integer, Double>> getCustomFrameLengths() {
		return customFrameLengths;
	}

	public void setCustomFrameLengths(List<Double> frameTimes) {
		customFrameLengths = convertTimeList(frameTimes);
	}

	public void setCustomFrameLengths(List<Integer> repeats, List<Double> times) {
		customFrameLengths.clear();
		for(int i=0; i<Math.min(repeats.size(), times.size()); i++) {
			customFrameLengths.add(Map.entry(repeats.get(i), times.get(i)));
		}
	}

	/**
	 * Transform list of values of into list of [number of values, value].
	 * e.g. [1,1,2,2,2,3,4,4,1,1,1] is converted to [ [2,1], [3,2], [1,3], [2,4], [3,1] ]
	 *
	 * @param timeList
	 * @return list of (num frames, frame length) values
	 */
	public List<Entry<Integer, Double>> convertTimeList(List<Double> timeList) {
		if (timeList == null || timeList.isEmpty()) {
			return Collections.emptyList();
		}

		List<Entry<Integer, Double>> convertedTimes = new ArrayList<>();

		Double val = timeList.get(0);
		int counts = 1;
		for(int i=1; i<timeList.size(); i++) {
			Double newVal = timeList.get(i);
			if (Math.abs(newVal-val) < 1e-6 && i < timeList.size()) {
				// increment if current value is same as last value
				counts++;
			} else {
				// add to list if current value is different from last
				Entry<Integer,Double> ent = Map.entry(counts, val);
				convertedTimes.add(ent);
				val = newVal;
				counts=1;
			}
		}

		// add the final value count to list
		Entry<Integer,Double> ent = Map.entry(counts, val);
		convertedTimes.add(ent);

		return convertedTimes;
	}
}
