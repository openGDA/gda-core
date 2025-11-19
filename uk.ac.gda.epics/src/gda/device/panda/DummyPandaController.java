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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.concurrent.Async.ListeningFuture;

public class DummyPandaController implements PandaController {
	private static final Logger logger = LoggerFactory.getLogger(DummyPandaController.class);
	private static final String ONE = "ONE";
	private static final String ZERO = "ZERO";

	private String[] dataNames = {"c1", "c2", "c3"};
	private String[] outputFormat = {"%5.5g", "%5.5g", "%5.5g"};

	/** Map used to store the values of PVs */
	private Map<String, Object> pvValues = new HashMap<>();

	/** Future that holds the thread for the currently executing sequence table
	 * (i.e. runs {@link #processSequenceTableRows()})*/
	private ListeningFuture<?> seqTableFuture;

	/** Flag to signal the sequence table should stop */
	private volatile boolean stopTableProcessing = false;

	private Random randomGenerator = new Random();

	@Override
	public void configure() throws FactoryException {
		setPv(PandaPvName.SEQ_STATE, SeqTableState.WAIT_ENABLE);
		setPv(PandaPvName.SEQ_BITA, ZERO);
		setPv(PandaPvName.PCAP_ARM, 0);
		setPv(PandaPvName.SEQ_LINE_REPEAT, 0);
		setPv(PandaPvName.SEQ_TABLE_LINE, 0);
		setPv(PandaPvName.SEQ_PRESCALE, 1.0);
		sequenceTableTimeUnits = SequenceTableTimeUnits.SECONDS;
	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public void reconfigure() throws FactoryException {
	}

	@Override
	public boolean isConfigureAtStartup() {
		return true;
	}

	@Override
	public void setupHdfWriter(String directory, String filename) throws DeviceException {
		setPv(PandaPvName.HDF_DIRECTORY, directory);
		setPv(PandaPvName.HDF_FILE_NAME, filename);
		setPv(PandaPvName.HDF_FULL_FILE_PATH, Paths.get(directory, filename).toString());
	}

	@Override
	public boolean setHdfCapture(int state) throws DeviceException {
		setPv(PandaPvName.HDF_CAPTURE, state);
		return true;
	}

	@Override
	public int getHdfCapture() throws DeviceException {
		return getPv(PandaPvName.HDF_CAPTURE, Integer.class);
	}

	@Override
	public String getHdfFileFullPath() throws DeviceException {
		return getPv(PandaPvName.HDF_FULL_FILE_PATH, String.class);
	}

	@Override
	public int getHdfNumCaptured() throws DeviceException {
		return getSeqTableLineRepeat();
	}

	@Override
	public boolean waitForHdfNumCaptured(int expectedFrame) throws DeviceException {
		return true;
	}

	private List<SequenceTableRow> sequenceTableRows = Collections.emptyList();
	private SequenceTableTimeUnits sequenceTableTimeUnits = SequenceTableTimeUnits.SECONDS;

	@Override
	public void setSeqTableRows(List<SequenceTableRow> tableRows) throws DeviceException {
		sequenceTableRows = new ArrayList<>(tableRows);
	}

	@Override
	public void setSeqTablePrescale(SequenceTableTimeUnits unit, double value) throws DeviceException {
		sequenceTableTimeUnits = unit;
		setPv(PandaPvName.SEQ_PRESCALE, value);
	}

	@Override
	public void setSeqBitA(String value) throws DeviceException {
		setPv(PandaPvName.SEQ_BITA, value);
	}

	/**
	 * Runs {@link #processSequenceTable()} and catches any exceptions.
	 * @return
	 */
	private boolean processSequenceTableRows() {
		try {
			processSequenceTable();
		}catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}catch(DeviceException e) {
			logger.warn("Problem processing sequence table", e);
			return false;
		}
		return true;
	}

	/**
	 * Simulate running a sequence table in similar manner to a real Panda device,
	 * with SEQ_LINE_REPEAT,SEQ_TABLE_LINE and SEQ_STATE PV values updated as
	 * each row of the table is processed. Each row is repeated several times using following steps :
	 * <li> Increment SEQ_LINE_REPEAT by 1
	 * <li> Set SEQ_STATE=WAIT_TRIGGER and wait for SEQ_BITA='ONE'
	 * <li> Set SEQ_STATE=PHASE_1 and sleep for time1
	 * <li> Set SEQ_STATE=PHASE_2 and sleep for time2
	 * SEQ_TABLE_LINE is incremented before processing a new row.
	 *
	 * The function is run asynchronously by {@link #setPCapArm(int)}
	 *
	 * @throws InterruptedException
	 * @throws DeviceException
	 */
	private void processSequenceTable() throws InterruptedException, DeviceException {
		logger.debug("Started processing sequence table");

		//conversion factor from sequence table units to time in milliseconds
		double timeConv = 1000*sequenceTableTimeUnits.convertToSeconds(1);
		int rowCount = 1;
		stopTableProcessing = false;

		setPv(PandaPvName.SEQ_STATE, SeqTableState.WAIT_ENABLE); //initial state before enabled
		setPv(PandaPvName.SEQ_LINE_REPEAT, 0);

		for(var row : sequenceTableRows) {
			logger.debug("Sequence table row : {}",rowCount);
			setPv(PandaPvName.SEQ_TABLE_LINE, rowCount);

			for (int i = 0; i < row.getRepeats(); i++) {

				logger.debug("Sequence table repeat : {}/{}", i+1, row.getRepeats());
				setPv(PandaPvName.SEQ_LINE_REPEAT, i + 1);
				setPv(PandaPvName.SEQ_STATE, SeqTableState.WAIT_TRIGGER);

				// wait for SEQ_BITA=ONE
				logger.debug("Waiting for SeqBitA = ONE");
				while(!getPv(PandaPvName.SEQ_BITA, String.class).equals(ONE)) {
					Thread.sleep(10L);
					if (stopTableProcessing) {
						return;
					}
				}
				logger.debug("Sleeping for phase1");
				setPv(PandaPvName.SEQ_STATE, SeqTableState.PHASE1);
				Thread.sleep((long) timeConv * row.getTime1());

				logger.debug("Sleeping for phase2");
				setPv(PandaPvName.SEQ_STATE, SeqTableState.PHASE2);
				Thread.sleep((long) timeConv * row.getTime2());

				if (stopTableProcessing) {
					return;
				}
			}
			rowCount++;
		}
		setPv(PandaPvName.SEQ_STATE, SeqTableState.WAIT_ENABLE);
		logger.debug("Finished processing sequence table - setting state to WAIT_ENABLE");
	}

	private void stopSequenceTable() {
		if (seqTableFuture == null) {
			return;
		}
		logger.debug("Stopping the sequence table");
		stopTableProcessing = true;
		seqTableFuture.cancel(true);
		try {
			Thread.sleep(500L);
			// future was already finished when cancel was called
			if (!seqTableFuture.isCancelled()) {
				var res = seqTableFuture.get();
				logger.debug("Result from thread : {}", res);
			}
		}catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			logger.error("Problem getting result from thread", e);
		} finally {
			logger.debug("Sequence table stopped, setting state to WAIT_ENABLE");
			setPv(PandaPvName.SEQ_STATE, SeqTableState.WAIT_ENABLE);
		}
	}

	@Override
	public int getSeqTableLine() throws DeviceException {
		return getPv(PandaPvName.SEQ_TABLE_LINE, Integer.class);
	}

	@Override
	public int getSeqTableLineRepeat() throws DeviceException {
		return getPv(PandaPvName.SEQ_LINE_REPEAT, Integer.class);
	}

	@Override
	public SeqTableState getSeqTableState() throws DeviceException {
		return getPv(PandaPvName.SEQ_STATE, SeqTableState.class);
	}

	/**
	 * Sets position capture state to specified value.
	 * If state = 1 , it also starts sequence table execution asynchronously.
	 * If state = 0 it stops the currently running sequence table.
	 *
	 */
	@Override
	public boolean setPCapArm(int state) throws DeviceException {
		int oldState = getPCapArm();
		setPv(PandaPvName.PCAP_ARM, state);
		if (state == 1 && state != oldState) {
			// arm also enables the sequence table
			stopSequenceTable();
			seqTableFuture = Async.call(this::processSequenceTableRows);
		} else if (state == 0) {
			// disarm also stops sequence table
			stopSequenceTable();
		}
		return true;
	}

	@Override
	public int getPCapArm() throws DeviceException {
		return getPv(PandaPvName.PCAP_ARM, Integer.class);
	}

	private <T> void setPv(PandaPvName pvName, T value) {
		setPv(pvName.toString(), value);
	}

	private <T> void setPv(String pvName, T value) {
		logger.debug("Setting PV value : {} = {}", pvName, value);
		pvValues.put(pvName, value);
	}

	private <T> T getPv(PandaPvName pvName, Class<T> clazz) throws DeviceException {
		String pvNameString = pvName.toString();
		if (pvValues.containsKey(pvNameString)) {
			return clazz.cast(pvValues.get(pvNameString));
		}
		throw new DeviceException("Not PV found for "+pvNameString+" in "+EpicsPandaController.class.getSimpleName()+". Have the PVs been created?");
	}

	@Override
	public double[] readData(int frameIndex) throws DeviceException {
		double[] outputs = new double[dataNames.length];
		for(int i=0; i<outputs.length; i++) {
			outputs[i] = 1+i+randomGenerator.nextDouble();
		}
		return outputs;
	}

	@Override
	public double[][] readData(int startFrame, int endFrame) throws DeviceException {
		logger.info("ReadData : {} - {}", startFrame, endFrame);
		int numFrames = endFrame - startFrame + 1;
		double[][] dataFrames = new double[numFrames][];
		for(int i=0; i<numFrames; i++) {
			dataFrames[i] =  readData(i+startFrame);
		}
		return dataFrames;
	}

	@Override
	public String[] getOutputFormat() {
		return outputFormat;
	}

	@Override
	public String[] getOutputNames() {
		return dataNames;
	}

	public void setDataNames(String[] dataNames) {
		this.dataNames = dataNames;
	}

	public void setOutputFormat(String[] outputFormat) {
		this.outputFormat = outputFormat;
	}

	@Override
	public void putPvValue(String pvName, Object value) throws DeviceException {
		setPv(pvName, value);
	}

	public void setRandomSeed(long seed) {
		randomGenerator.setSeed(seed);
	}
}
