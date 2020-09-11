/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.scan.servlet;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.process.ProcessingException;
import org.eclipse.scanning.server.servlet.DefaultScanConfiguration;
import org.eclipse.scanning.server.servlet.DefaultScanPreprocessor;
import org.eclipse.scanning.test.utilities.scan.mock.MockDetectorModel;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class DefaultScanPreprocessorTest {

	private DefaultScanPreprocessor preprocessor;

	// Start positions to be put in ScanRequest
	private final Map<String, Object> startPositionMapStages = ImmutableMap.of("stage_x", 0.1, "stage_y", 0.25, "stage_z", 1.3);
	private final Map<String, Object> endPositionMapStages = ImmutableMap.of("stage_z", 0.5);

	// Start positions to be put in DefaultScanConfiguration
	private final Map<String, Object> startPositionMapShutter = ImmutableMap.of("s1", "Open");
	private final Map<String, Object> endPositionMapShutter = ImmutableMap.of("s1", "Closed");

	@Before
	public void setUp() {
		final DefaultScanConfiguration defaultScanConfig = new DefaultScanConfiguration();
		defaultScanConfig.setPerPointMonitorNames(new HashSet<>(Arrays.asList("defpp1", "defpp2", "defpp3")));
		defaultScanConfig.setPerScanMonitorNames(new HashSet<>(Arrays.asList("defaultps1", "defaultps2")));
		defaultScanConfig.setTemplateFilePaths(new HashSet<>(Arrays.asList("foo.yaml", "bar.yaml")));
		defaultScanConfig.setStartPosition(new MapPosition(startPositionMapShutter));
		defaultScanConfig.setEndPosition(new MapPosition(endPositionMapShutter));

		preprocessor = new DefaultScanPreprocessor();
		preprocessor.setDefaultScanConfiguration(defaultScanConfig);
	}

	protected ScanRequest createStepScan() {
		final ScanRequest req = new ScanRequest();
		req.setCompoundModel(new CompoundModel(new AxialStepModel("fred", 0, 9, 1)));

		final MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setName("detector");
		dmodel.setExposureTime(0.001);
		req.putDetector("detector", dmodel);

		return req;
	}

	@Test
	public void testPreprocess() throws Exception {
		final ScanRequest scanRequest = createStepScan();
		scanRequest.setMonitorNamesPerPoint(Arrays.asList("pp1", "pp2"));
		scanRequest.setMonitorNamesPerScan(Arrays.asList("ps1", "ps2", "ps3"));
		scanRequest.setTemplateFilePaths(new HashSet<>(Arrays.asList("fred.yaml")));
		scanRequest.setStartPosition(new MapPosition(startPositionMapStages));
		scanRequest.setEnd(new MapPosition(endPositionMapStages));

		preprocessor.preprocess(scanRequest);

		assertEquals(new HashSet<>(Arrays.asList("pp1", "pp2", "defpp1", "defpp2", "defpp3")),
				scanRequest.getMonitorNamesPerPoint());
		assertEquals(new HashSet<>(Arrays.asList("ps1", "ps2", "ps3", "defaultps1", "defaultps2")),
				scanRequest.getMonitorNamesPerScan());
		assertEquals(new HashSet<>(Arrays.asList("foo.yaml", "bar.yaml", "fred.yaml")),
				scanRequest.getTemplateFilePaths());

		// We expect the start & end positions to be the combination of the position originally in the ScanRequest and
		// the one in the DefaultScanConfiguration
		final Map<String, Object> expectedStartPosMap = ImmutableMap.of(
				"stage_x", 0.1, "stage_y", 0.25, "stage_z", 1.3, "s1", "Open");
		final Map<String, Object> expectedEndPosMap = ImmutableMap.of("stage_z", 0.5, "s1", "Closed");

		assertPositionEquals(expectedStartPosMap, scanRequest.getStartPosition());
		assertPositionEquals(expectedEndPosMap, scanRequest.getEndPosition());
	}

	@Test
	public void testPreprocessEmptyScanRequest() throws Exception {
		ScanRequest scanRequest = createStepScan();
		preprocessor.preprocess(scanRequest);

		assertEquals(new HashSet<>(Arrays.asList("defpp1", "defpp2", "defpp3")),
				scanRequest.getMonitorNamesPerPoint());
		assertEquals(new HashSet<>(Arrays.asList("defaultps1", "defaultps2")),
				scanRequest.getMonitorNamesPerScan());
		assertEquals(new HashSet<>(Arrays.asList("foo.yaml", "bar.yaml")),
				scanRequest.getTemplateFilePaths());
		assertPositionEquals(startPositionMapShutter, scanRequest.getStartPosition());
		assertPositionEquals(endPositionMapShutter, scanRequest.getEndPosition());
}

	@Test
	public void testPreprocessEmptyDefaultScanConfiguration() throws Exception {
		preprocessor.setDefaultScanConfiguration(new DefaultScanConfiguration());

		final ScanRequest scanRequest = createStepScan();
		scanRequest.setMonitorNamesPerPoint(Arrays.asList("pp1", "pp2"));
		scanRequest.setMonitorNamesPerScan(Arrays.asList("ps1", "ps2", "ps3"));
		scanRequest.setTemplateFilePaths(new HashSet<>(Arrays.asList("fred.yaml")));
		scanRequest.setStartPosition(new MapPosition(startPositionMapStages));
		scanRequest.setEnd(new MapPosition(endPositionMapStages));

		preprocessor.preprocess(scanRequest);

		assertEquals(new HashSet<>(Arrays.asList("pp1", "pp2")),
				scanRequest.getMonitorNamesPerPoint());
		assertEquals(new HashSet<>(Arrays.asList("ps1", "ps2", "ps3")),
				scanRequest.getMonitorNamesPerScan());
		assertEquals(new HashSet<>(Arrays.asList("fred.yaml")),
				scanRequest.getTemplateFilePaths());
		assertPositionEquals(startPositionMapStages, scanRequest.getStartPosition());
		assertPositionEquals(endPositionMapStages, scanRequest.getEndPosition());
	}

	@Test
	public void testPreprocessDuplicateScannablesInPositions() throws ProcessingException {
		final ScanRequest scanRequest = createStepScan();
		scanRequest.setStartPosition(new MapPosition(startPositionMapStages));
		scanRequest.setEnd(new MapPosition(endPositionMapStages));

		final Map<String, Object> startPosMap = ImmutableMap.of("stage_x", 0.23, "s1", "Open");
		final Map<String, Object> endPosMap = ImmutableMap.of("stage_x", 0.7, "s1", "Closed");

		final DefaultScanConfiguration scanConfig = new DefaultScanConfiguration();
		scanConfig.setStartPosition(new MapPosition(startPosMap));
		scanConfig.setEndPosition(new MapPosition(endPosMap));

		preprocessor.preprocess(scanRequest);

		// Values for scannables already in the ScanRequest should not be overwritten by the DefaultScanConfiguration
		final Map<String, Object> expectedStartPosMap = ImmutableMap.of("stage_x", 0.1, "stage_y", 0.25, "stage_z", 1.3, "s1", "Open");
		final Map<String, Object> expectedEndPosMap = ImmutableMap.of("stage_z", 0.5, "s1", "Closed");

		assertPositionEquals(expectedStartPosMap, scanRequest.getStartPosition());
		assertPositionEquals(expectedEndPosMap, scanRequest.getEndPosition());
	}

	private void assertPositionEquals(Map<String, Object> expectedMap, IPosition position) {
		assertEquals(expectedMap.size(), position.size());
		for (Map.Entry<String, Object> expectedEntry : expectedMap.entrySet()) {
			assertEquals(expectedEntry.getValue(), position.get(expectedEntry.getKey()));
		}
	}
}
