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

package org.eclipse.scanning.test.command;

import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.AFTER_SCRIPT;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.ALWAYS_RUN_AFTER_SCRIPT;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.BEFORE_SCRIPT;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.DETECTORS;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.END_POSITION;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.FILE_PATH;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.IGNORE_PREPROCESS;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.MONITOR_NAMES_PER_POINT;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.MONITOR_NAMES_PER_SCAN;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.PROCESSING_REQUEST;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.SAMPLE_DATA;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.SCAN_METADATA;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.START_POSITION;
import static org.eclipse.scanning.api.event.scan.ScanRequestBuilder.TEMPLATE_FILE_PATHS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.SampleData;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.scan.ScanRequestBuilder;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.junit.Before;
import org.junit.Test;

public class ScanRequestBuilderTest {
	private static final String DATA_DIRECTORY = "/dls_sw/ixx/data/2020/cm-1234/data";

	private IDetectorModel model1;
	private IDetectorModel model2;
	private Map<String, Object> detectors;

	private SampleData sampleData;
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
		model1 = mock(IDetectorModel.class);
		model2 = mock(IDetectorModel.class);
		detectors = new HashMap<>();
		detectors.put("Detector1", model1);
		detectors.put("Detector2", model2);

		sampleData = mock(SampleData.class);
		monitorNamesPerPoint = Arrays.asList("pointmonitor1", "pointmonitor2");
		monitorNamesPerScan = Arrays.asList("scanmonitor1", "scanmonitor2", "scanmonitor3");
		scanMetadata = Arrays.asList(mock(ScanMetadata.class), mock(ScanMetadata.class));
		templateFiles = new HashSet<>(Arrays.asList("template1", "template2"));
		startPosition = mock(IPosition.class);
		endPosition = mock(IPosition.class);
		beforeScript = mock(ScriptRequest.class);
		afterScript = mock(ScriptRequest.class);
		processingRequest = mock(ProcessingRequest.class);
	}

	@Test
	public void testDefaultValues() {
		final ScanRequestBuilder builder = new ScanRequestBuilder(mock(IScanPointGeneratorModel.class));
		final ScanRequest scanRequest = builder.build();
		assertTrue(scanRequest.getDetectors().isEmpty());
		assertTrue(scanRequest.getMonitorNamesPerPoint().isEmpty());
		assertTrue(scanRequest.getMonitorNamesPerScan().isEmpty());
		assertNull(scanRequest.getSampleData());
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
			.withSampleData(sampleData)
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
		values.put(SAMPLE_DATA, sampleData);
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
		final Map<String, Object> requestDetectors = scanRequest.getDetectors();
		assertTrue(requestDetectors.get("Detector1") == model1);
		assertTrue(requestDetectors.get("Detector2") == model2);

		assertTrue(scanRequest.getMonitorNamesPerPoint() == monitorNamesPerPoint);
		assertTrue(scanRequest.getMonitorNamesPerScan() == monitorNamesPerScan);
		assertTrue(scanRequest.getSampleData() == sampleData);
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
}
