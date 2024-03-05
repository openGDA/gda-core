/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.scan.nexus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.annotation.scan.FileDeclared;
import org.eclipse.scanning.api.annotation.scan.ScanAbort;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFault;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDatasetModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDetectorModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.junit.jupiter.api.Test;

import uk.ac.gda.analysis.mscan.MalcolmProcessingManager;
import uk.ac.gda.analysis.mscan.MalcolmProcessingManager.Config;
import uk.ac.gda.analysis.mscan.MalcolmSwmrProcessor;
import uk.ac.gda.analysis.mscan.MaxValProc;
import uk.ac.gda.analysis.mscan.MeanProc;
import uk.ac.gda.analysis.mscan.SumProc;

class MalcolmStatsScanTest extends AbstractMalcolmScanTest {

	public static class ProcessingDummyMalcolmDevice extends DummyMalcolmDevice {

		private MalcolmProcessingManager processing = new MalcolmProcessingManager();

		public ProcessingDummyMalcolmDevice() {
			super();
			setPrependScanName(true);
		}

		public void addProcessors(Config config, Collection<MalcolmSwmrProcessor<?>> foo) {
			processing.getProcessorMap().put(config, foo);
		}

		@FileDeclared
		public void startSwmrReader() {
			processing.startSwmrReading();
		}

		@Override
		public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {
			final List<NexusObjectProvider<?>> nexusObjectProviders = super.getNexusProviders(info);
			processing.initialiseForProcessing(nexusObjectProviders, info);
			return nexusObjectProviders;
		}

		@ScanAbort
		@ScanFault
		public void stopSwmr() {
			processing.abortReaders();
		}

		@ScanEnd
		public void closeProc() {
			processing.waitUntilComplete();
		}

	}

	private static final String UNIQUE_KEY_PATH = "/entry/NDAttributes/NDArrayUniqueId";

	private static final String DETECTOR_NAME = "det";

	private static final List<String> STATS_FIELD_NAMES =
			List.of(SumProc.SUM_DATASET_NAME, MeanProc.MEAN_DATASET_NAME, MaxValProc.MAX_VALUE_DATASET_NAME);

	@Override
	protected DummyMalcolmDevice createMalcolmDevice() throws ScanningException {
		final DummyMalcolmModel model = createMalcolmModel();
		final ProcessingDummyMalcolmDevice malcolmDevice = new ProcessingDummyMalcolmDevice();
		configureProcessing(malcolmDevice);
		malcolmDevice.setAvailableAxes(model.getAxesToMove()); // set the available axes to those of the model
		malcolmDevice.configure(model);
		malcolmDevice.setName(model.getName());
		return malcolmDevice;
	}

	private void configureProcessing(ProcessingDummyMalcolmDevice malcolmDevice) {
		final Config config = new Config(2, '-' + DETECTOR_NAME + ".h5",
				"/entry/" + NXdetector.NX_DATA + '/' + NXdetector.NX_DATA,
				UNIQUE_KEY_PATH, DETECTOR_NAME,
				NexusBaseClass.NX_DETECTOR, null);

		malcolmDevice.addProcessors(config,
				List.of(new SumProc(), new MeanProc(), new MaxValProc()));
	}

	@Override
	protected DummyMalcolmModel createMalcolmModel() {
		final DummyMalcolmModel model = new DummyMalcolmModel();
		model.setTimeout(10 * 60); // increased timeout for debugging purposes
		model.setExposureTime(0.1);

		final DummyMalcolmDetectorModel detModel = new DummyMalcolmDetectorModel();
		detModel.setName(DETECTOR_NAME);
		detModel.setFramesPerStep(1);
		detModel.setExposureTime(0.08);

		final DummyMalcolmDatasetModel dataModel = new DummyMalcolmDatasetModel();
		dataModel.setName(NXdetector.NX_DATA);
		dataModel.setRank(2);
		dataModel.setDtype(Double.class);
		detModel.setDatasets(List.of(dataModel));
		model.setDetectorModels(List.of(detModel));

		final List<String> axes = List.of(X_AXIS_NAME, Y_AXIS_NAME);
		model.setAxesToMove(axes);
		model.setPositionerNames(axes);
		model.setMonitorNames(List.of(MONITOR_NAME));
		return model;
	}

	@Test
	void testMalcolmStatsScan() throws Exception {
		final int[] shape = { 8, 5 };
		final IRunnableDevice<ScanModel> scanner = createMalcolmGridScan(malcolmDevice, output, false, shape);
		scanner.run(null);

		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));
		checkNexusFile(scanner, shape);
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, int[] shape) throws Exception {
		checkNexusFile(scanner, false, shape);
		checkStatsDataGroups(getNexusRoot(scanner).getEntry());
	}

	private void checkStatsDataGroups(NXentry entry) {
		for (String statsFieldName : STATS_FIELD_NAMES) {
			final NXdata dataGroup = entry.getData(DETECTOR_NAME + "_" + statsFieldName);
			assertThat(dataGroup, is(notNullValue()));
		}
	}

	@Override
	protected List<String> getExpectedDataGroupNames(final Map<String, List<String>> primaryDataFieldNamesPerDetector) {
		final List<String> dataGroupNames = new ArrayList<>(super.getExpectedDataGroupNames(primaryDataFieldNamesPerDetector));
		dataGroupNames.addAll(STATS_FIELD_NAMES.stream().map(name -> DETECTOR_NAME + "_" + name).toList());
		return dataGroupNames;
	}

	@Override
	protected void checkDetector(NXdetector detector, DummyMalcolmModel dummyMalcolmModel,
			IMalcolmDetectorModel detectorModel, ScanModel scanModel, boolean foldedGrid,
			NXentry entry, List<String> primaryDataFieldNames, int[] sizes) throws Exception {
		super.checkDetector(detector, dummyMalcolmModel, detectorModel, scanModel, foldedGrid,
				entry, primaryDataFieldNames, sizes);

		for (String statFieldName : STATS_FIELD_NAMES) {
			final DataNode dataNode = detector.getDataNode(statFieldName);
			assertThat(dataNode, is(notNullValue()));
		}
	}

	@Override
	protected List<String> getExpectedDetectorDataNodeNames(final String detectorName,
			final List<DummyMalcolmDatasetModel> datasetModels) {
		final List<String> dataNodeNames = super.getExpectedDetectorDataNodeNames(detectorName, datasetModels);
		dataNodeNames.addAll(STATS_FIELD_NAMES);
		return dataNodeNames;
	}

}
