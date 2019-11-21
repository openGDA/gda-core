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

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.server.servlet.DefaultScanConfiguration;
import org.eclipse.scanning.server.servlet.DefaultScanPreprocessor;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.junit.Before;
import org.junit.Test;

public class DefaultScanPreprocessorTest {

	private DefaultScanPreprocessor preprocessor;

	@Before
	public void setUp() {
		DefaultScanConfiguration defaultScanConfig = new DefaultScanConfiguration();
		defaultScanConfig.setPerPointMonitorNames(new HashSet<>(Arrays.asList("defpp1", "defpp2", "defpp3")));
		defaultScanConfig.setPerScanMonitorNames(new HashSet<>(Arrays.asList("defaultps1", "defaultps2")));
		defaultScanConfig.setTemplateFilePaths(new HashSet<>(Arrays.asList("foo.yaml", "bar.yaml")));

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
		ScanRequest scanRequest = createStepScan();
		scanRequest.setMonitorNamesPerPoint(Arrays.asList("pp1", "pp2"));
		scanRequest.setMonitorNamesPerScan(Arrays.asList("ps1", "ps2", "ps3"));
		scanRequest.setTemplateFilePaths(new HashSet<>(Arrays.asList("fred.yaml")));

		preprocessor.preprocess(scanRequest);

		assertEquals(new HashSet<>(Arrays.asList("pp1", "pp2", "defpp1", "defpp2", "defpp3")),
				scanRequest.getMonitorNamesPerPoint());
		assertEquals(new HashSet<>(Arrays.asList("ps1", "ps2", "ps3", "defaultps1", "defaultps2")),
				scanRequest.getMonitorNamesPerScan());
		assertEquals(new HashSet<>(Arrays.asList("foo.yaml", "bar.yaml", "fred.yaml")),
				scanRequest.getTemplateFilePaths());
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
	}

	@Test
	public void testPreprocessEmptyDefaultScanConfiguration() throws Exception {
		preprocessor.setDefaultScanConfiguration(new DefaultScanConfiguration());

		ScanRequest scanRequest = createStepScan();
		scanRequest.setMonitorNamesPerPoint(Arrays.asList("pp1", "pp2"));
		scanRequest.setMonitorNamesPerScan(Arrays.asList("ps1", "ps2", "ps3"));
		scanRequest.setTemplateFilePaths(new HashSet<>(Arrays.asList("fred.yaml")));

		preprocessor.preprocess(scanRequest);

		assertEquals(new HashSet<>(Arrays.asList("pp1", "pp2")),
				scanRequest.getMonitorNamesPerPoint());
		assertEquals(new HashSet<>(Arrays.asList("ps1", "ps2", "ps3")),
				scanRequest.getMonitorNamesPerScan());
		assertEquals(new HashSet<>(Arrays.asList("fred.yaml")),
				scanRequest.getTemplateFilePaths());
	}

}
