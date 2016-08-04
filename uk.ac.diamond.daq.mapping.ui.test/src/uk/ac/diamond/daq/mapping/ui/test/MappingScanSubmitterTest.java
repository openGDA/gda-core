/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.impl.DetectorModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingAxisManager;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.experiment.MappingScanSubmitter;

public class MappingScanSubmitterTest {

	private static final String X_AXIS_NAME = "testing_stage_x";
	private static final String Y_AXIS_NAME = "testing_stage_y";

	private MappingScanSubmitter mappingScanSubmitter;
	private MappingAxisManager mappingAxisManager;
	private MappingExperimentBean experimentBean;
	private GridModel scanPath;

	@Before
	public void setUp() throws Exception {
		mappingAxisManager = new MappingAxisManager();
		mappingAxisManager.setActiveFastScanAxis(X_AXIS_NAME);
		mappingAxisManager.setActiveSlowScanAxis(Y_AXIS_NAME);

		mappingScanSubmitter = new MappingScanSubmitter();
		mappingScanSubmitter.setMappingAxisManager(mappingAxisManager);

		// Set up the experiment bean with some sensible defaults
		experimentBean = new MappingExperimentBean();

		scanPath = new GridModel();
		experimentBean.getScanDefinition().getMappingScanRegion().setScanPath(scanPath);

		IMappingScanRegionShape scanRegion = new RectangularMappingRegion();
		experimentBean.getScanDefinition().getMappingScanRegion().setRegion(scanRegion);

		experimentBean.setDetectorParameters(Collections.emptyList());
	}

	@After
	public void tearDown() throws Exception {
		mappingAxisManager = null;
		mappingScanSubmitter = null;
	}

	@Test
	public void testDetectorIsIncludedCorrectly() {
		String detName = "det1";
		IDetectorModel detModel = new MandelbrotModel();
		experimentBean.setDetectorParameters(Arrays.asList(new DetectorModelWrapper(detName, detModel, true)));

		ScanBean scanBean = mappingScanSubmitter.convertToScanBean(experimentBean);
		ScanRequest<?> scanRequest = scanBean.getScanRequest();

		assertEquals(scanRequest.getDetectors().get(detName), detModel);
	}

	@Test
	public void testDetectorIsExcludedCorrectly() {
		String detName = "det1";
		IDetectorModel detModel = new MandelbrotModel();
		experimentBean.setDetectorParameters(Arrays.asList(new DetectorModelWrapper(detName, detModel, false)));

		ScanBean scanBean = mappingScanSubmitter.convertToScanBean(experimentBean);
		ScanRequest<?> scanRequest = scanBean.getScanRequest();

		// This test relies on the implementation of ScanRequest, which lazily initialises its detectors field only
		// when a detector is added. If this fails in future because getDetectors() returns an empty map, this test
		// will need to be updated to match.
		assertThat(scanRequest.getDetectors(), is(nullValue()));
	}

	@Test
	public void testScanPathIsIncluded() {
		ScanBean scanBean = mappingScanSubmitter.convertToScanBean(experimentBean);
		ScanRequest<?> scanRequest = scanBean.getScanRequest();

		assertEquals(scanRequest.getCompoundModel().getModels().get(0), scanPath);
	}

	@Test
	public void testStageNamesAreSetCorrectly() {
		assertThat(scanPath.getFastAxisName(), is(not(equalTo(X_AXIS_NAME))));
		assertThat(scanPath.getSlowAxisName(), is(not(equalTo(Y_AXIS_NAME))));

		mappingScanSubmitter.convertToScanBean(experimentBean);

		assertThat(scanPath.getFastAxisName(), is(equalTo(X_AXIS_NAME)));
		assertThat(scanPath.getSlowAxisName(), is(equalTo(Y_AXIS_NAME)));
	}

}
