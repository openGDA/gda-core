/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package org.eclipse.scanning.sequencer;

import static org.eclipse.scanning.sequencer.ScanRequestBuilder.AFTER_SCRIPT;
import static org.eclipse.scanning.sequencer.ScanRequestBuilder.ALWAYS_RUN_AFTER_SCRIPT;
import static org.eclipse.scanning.sequencer.ScanRequestBuilder.BEFORE_SCRIPT;
import static org.eclipse.scanning.sequencer.ScanRequestBuilder.DETECTORS;
import static org.eclipse.scanning.sequencer.ScanRequestBuilder.END_POSITION;
import static org.eclipse.scanning.sequencer.ScanRequestBuilder.FILE_PATH;
import static org.eclipse.scanning.sequencer.ScanRequestBuilder.IGNORE_PREPROCESS;
import static org.eclipse.scanning.sequencer.ScanRequestBuilder.MONITOR_NAMES_PER_POINT;
import static org.eclipse.scanning.sequencer.ScanRequestBuilder.MONITOR_NAMES_PER_SCAN;
import static org.eclipse.scanning.sequencer.ScanRequestBuilder.PROCESSING_REQUEST;
import static org.eclipse.scanning.sequencer.ScanRequestBuilder.SCAN_METADATA;
import static org.eclipse.scanning.sequencer.ScanRequestBuilder.START_POSITION;
import static org.eclipse.scanning.sequencer.ScanRequestBuilder.TEMPLATE_FILE_PATHS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.impl.DataNodeImpl;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.scan.NexusScanConstants;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.sequencer.nexus.SolsticeConstants;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

@SuppressWarnings("restriction")
public class ScanRequestBuilderTest {
	private static final String DATA_DIRECTORY = "/dls_sw/ixx/data/2020/cm-1234/data";
	
	private static MockedStatic<ServiceHolder> holder;
	private IMarshallerService marshaller;
	private INexusFileFactory factory;
	private NexusFile file;
	private DataNode node;

	private IDetectorModel model1;
	private IDetectorModel model2;
	private Map<String, IDetectorModel> detectors;

	private List<String> monitorNamesPerPoint;
	private List<String> monitorNamesPerScan;
	private List<ScanMetadata> scanMetadata;
	private Set<String> templateFiles;
	private IPosition startPosition;
	private IPosition endPosition;
	private ScriptRequest beforeScript;
	private ScriptRequest afterScript;
	private ProcessingRequest processingRequest;
	
	@Before
	public void setUp() {
		holder  = mockStatic(ServiceHolder.class);
		marshaller = mock(IMarshallerService.class);
		factory = mock(INexusFileFactory.class);
		file = mock(NexusFile.class);
		node = new DataNodeImpl(1111);
		
		model1 = mock(IDetectorModel.class);
		model2 = mock(IDetectorModel.class);
		detectors = new HashMap<>();
		detectors.put("Detector1", model1);
		detectors.put("Detector2", model2);

		monitorNamesPerPoint = Arrays.asList("pointmonitor1", "pointmonitor2");
		monitorNamesPerScan = Arrays.asList("scanmonitor1", "scanmonitor2", "scanmonitor3");
		scanMetadata = Arrays.asList(mock(ScanMetadata.class), mock(ScanMetadata.class));
		templateFiles = new HashSet<>(Arrays.asList("template1", "template2"));
		startPosition = mock(IPosition.class);
		endPosition = mock(IPosition.class);
		beforeScript = mock(ScriptRequest.class);
		afterScript = mock(ScriptRequest.class);
		processingRequest = mock(ProcessingRequest.class);
		
		holder.when(ServiceHolder::getMarshallerService).thenReturn(marshaller);
		holder.when(ServiceHolder::getNexusFileFactory).thenReturn(factory);
		when(factory.newNexusFile(anyString())).thenReturn(file);
		node.setString("{\"@type\":\"ScanRequest\",\"compoundModel\":{\"@type\":\"CompoundModel\",\"units\":[\"mm\",\"mm\"],\"alternating\":false,\"continuous\":false,\"models\":[{\"@type\":\"TwoAxisGridStepModel\",\"name\":\"Raster\",\"units\":[\"mm\",\"mm\"],\"alternating\":false,\"continuous\":false,\"xAxisName\":\"stagex\",\"yAxisName\":\"stagey\",\"xAxisUnits\":\"mm\",\"yAxisUnits\":\"mm\",\"boundingBox\":{\"@type\":\"BoundingBox\",\"xAxisName\":\"stage_x\",\"xAxisStart\":0.0,\"xAxisLength\":10.0,\"yAxisName\":\"stage_y\",\"yAxisStart\":0.0,\"yAxisLength\":10.0},\"orientation\":\"HORIZONTAL\",\"alternateBothAxes\":true,\"boundsToFit\":false,\"xAxisStep\":5.0,\"yAxisStep\":5.0}],\"regions\":[{\"@type\":\"ScanRegion\",\"roi\":{\"@type\":\"roi.rectangular\",\"lengths\":[10.0,10.0],\"angle\":0.0,\"point\":[0.0,0.0]},\"scannables\":[\"stagex\",\"stagey\"]}],\"mutators\":[],\"duration\":-1.0},\"detectors\":{},\"monitorNamesPerPoint\":[],\"monitorNamesPerScan\":[],\"templateFilePaths\":[],\"alwaysRunAfterScript\":false,\"ignorePreprocess\":false,\"processingRequest\":{\"@type\":\"ProcessingRequest\"}}");
	}
	
	@After
	public void cleanup() {
		holder.close();
	}

	@Test
	public void testDefaultValues() {
		final ScanRequestBuilder builder = new ScanRequestBuilder(mock(IScanPointGeneratorModel.class));
		final ScanRequest scanRequest = builder.build();
		assertTrue(scanRequest.getDetectors().isEmpty());
		assertTrue(scanRequest.getMonitorNamesPerPoint().isEmpty());
		assertTrue(scanRequest.getMonitorNamesPerScan().isEmpty());
		assertTrue(scanRequest.getScanMetadata().isEmpty());
		assertNull(scanRequest.getFilePath());
		assertTrue(scanRequest.getTemplateFilePaths().isEmpty());
		assertNull(scanRequest.getStartPosition());
		assertNull(scanRequest.getEndPosition());
		assertNull(scanRequest.getBeforeScript());
		assertNull(scanRequest.getAfterScript());
		assertFalse(scanRequest.isAlwaysRunAfterScript());
		assertFalse(scanRequest.isIgnorePreprocess());
		assertNull(scanRequest.getProcessingRequest());
	}

	@Test
	public void testValuesSet() {
		final ScanRequestBuilder builder = new ScanRequestBuilder(mock(IScanPointGeneratorModel.class));
		final ScanRequest scanRequest = builder
			.withDetectors(detectors)
			.withMonitorNamesPerPoint(monitorNamesPerPoint)
			.withMonitorNamesPerScan(monitorNamesPerScan)
			.withScanMetadata(scanMetadata)
			.withFilePath(DATA_DIRECTORY)
			.withTemplateFilePaths(templateFiles)
			.withStartPosition(startPosition)
			.withEndPosition(endPosition)
			.withBeforeScript(beforeScript)
			.withAfterScript(afterScript)
			.alwaysRunAfterScript(true)
			.ignorePreprocess(true)
			.withProcessingRequest(processingRequest)
			.build();
		verifyValuesSet(scanRequest);
	}

	@Test
	public void setValuesFromMap() {
		final Map<String, Object> values = new HashMap<>();
		values.put(DETECTORS, detectors);
		values.put(MONITOR_NAMES_PER_POINT, monitorNamesPerPoint);
		values.put(MONITOR_NAMES_PER_SCAN, monitorNamesPerScan);
		values.put(SCAN_METADATA, scanMetadata);
		values.put(FILE_PATH, DATA_DIRECTORY);
		values.put(TEMPLATE_FILE_PATHS, templateFiles);
		values.put(START_POSITION, startPosition);
		values.put(END_POSITION, endPosition);
		values.put(BEFORE_SCRIPT, beforeScript);
		values.put(AFTER_SCRIPT, afterScript);
		values.put(ALWAYS_RUN_AFTER_SCRIPT, true);
		values.put(IGNORE_PREPROCESS, true);
		values.put(PROCESSING_REQUEST, processingRequest);

		final ScanRequestBuilder builder = new ScanRequestBuilder(mock(IScanPointGeneratorModel.class), values);
		final ScanRequest scanRequest = builder.build();

		verifyValuesSet(scanRequest);
	}

	@Test(expected = ClassCastException.class)
	public void setValuesFromMapInvalid() {
		final Map<String, Object> values = new HashMap<>();
		values.put(ScanRequestBuilder.DETECTORS, "Detectors");
		final ScanRequestBuilder builder = new ScanRequestBuilder(mock(IScanPointGeneratorModel.class), values);
		builder.build();
	}

	private void verifyValuesSet(ScanRequest scanRequest) {
		// We expect the builder to pass object data by reference
		final Map<String, IDetectorModel> requestDetectors = scanRequest.getDetectors();
		assertTrue(requestDetectors.get("Detector1") == model1);
		assertTrue(requestDetectors.get("Detector2") == model2);

		assertTrue(scanRequest.getMonitorNamesPerPoint() == monitorNamesPerPoint);
		assertTrue(scanRequest.getMonitorNamesPerScan() == monitorNamesPerScan);
		assertTrue(scanRequest.getScanMetadata() == scanMetadata);
		assertEquals(DATA_DIRECTORY, scanRequest.getFilePath());
		assertTrue(scanRequest.getTemplateFilePaths() == templateFiles);
		assertTrue(scanRequest.getStartPosition() == startPosition);
		assertTrue(scanRequest.getEndPosition() == endPosition);
		assertTrue(scanRequest.getBeforeScript() == beforeScript);
		assertTrue(scanRequest.getAfterScript() == afterScript);
		assertTrue(scanRequest.isAlwaysRunAfterScript());
		assertTrue(scanRequest.isIgnorePreprocess());
		assertTrue(scanRequest.getProcessingRequest() == processingRequest);
	}
	
	@Test
	public void loadsSolsticeScanScanRequestIfPresent() throws NexusException, Exception {
		when(file.isPathValid("/" + SolsticeConstants.DEFAULT_ENTRY_NAME)).thenReturn(true);
		when(file.isPathValid("/" + SolsticeConstants.DEFAULT_ENTRY_NAME + "/" + ScanRequestBuilder.ORIGINAL_SCAN_NAME)).thenReturn(true);
		when(file.getData(anyString())).thenReturn(node);
		when(marshaller.unmarshal(node.toString(), ScanRequest.class)).thenReturn(new ScanRequest());
		
		Optional<ScanRequest> result = ScanRequestBuilder.buildFromNexusFile("afile");
		assert(result.isPresent());
	}
	
	@Test
	public void loadsDiamondScanScanRequestIfPresent() throws NexusException, Exception {
		when(file.isPathValid("/" + SolsticeConstants.DEFAULT_ENTRY_NAME)).thenReturn(true);
		when(file.isPathValid("/" + SolsticeConstants.DEFAULT_ENTRY_NAME + "/" + NexusScanConstants.GROUP_NAME_DIAMOND_SCAN)).thenReturn(true);
		when(file.getData(anyString())).thenReturn(node);
		when(marshaller.unmarshal(node.toString(), ScanRequest.class)).thenReturn(new ScanRequest());
		
		Optional<ScanRequest> result = ScanRequestBuilder.buildFromNexusFile("afile");
		assert(result.isPresent());
	}
	
	@Test
	public void loadsDiamondScanScanRequestWithCustomEntryNameIfPresent() throws NexusException, Exception {
		String nxEntryName = "banana";
		System.setProperty(SolsticeConstants.SYSTEM_PROPERTY_NAME_ENTRY_NAME, nxEntryName);
		when(file.isPathValid("/" + nxEntryName)).thenReturn(true);
		when(file.isPathValid("/" + SolsticeConstants.DEFAULT_ENTRY_NAME + "/" + NexusScanConstants.GROUP_NAME_DIAMOND_SCAN)).thenReturn(true);
		when(file.getData(anyString())).thenReturn(node);
		when(marshaller.unmarshal(node.toString(), ScanRequest.class)).thenReturn(new ScanRequest());
		
		Optional<ScanRequest> result = ScanRequestBuilder.buildFromNexusFile("afile");
		assert(result.isPresent());
	}
	
	@Test
	public void failsToLoadWithUnknownEntryName() throws NexusException, Exception {
		
		Optional<ScanRequest> result = ScanRequestBuilder.buildFromNexusFile("afile");
		assert(result.isEmpty());
	}
}
