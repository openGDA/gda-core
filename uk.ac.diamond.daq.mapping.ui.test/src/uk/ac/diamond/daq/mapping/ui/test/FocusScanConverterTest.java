/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.FocusScanBean;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanConverter;

public class FocusScanConverterTest {

	private MappingStageInfo mappingStageInfo;

	private FocusScanConverter focusScanConverter;

	private FocusScanBean focusScanBean;

	@Before
	public void setUp() throws Exception {
		mappingStageInfo = new MappingStageInfo();
		mappingStageInfo.setActiveFastScanAxis("testing_x_axis");
		mappingStageInfo.setActiveSlowScanAxis("testing_y_axis");
		mappingStageInfo.setAssociatedAxis("zone_plate_axis");

		focusScanConverter = new FocusScanConverter();
		focusScanConverter.setMappingStageInfo(mappingStageInfo);

		focusScanBean = new FocusScanBean();
		focusScanBean.setFocusScannableName("zonePlateZ");
		focusScanBean.setFocusCentre(150.0);
		focusScanBean.setFocusRange(17.5);
		focusScanBean.setNumberOfFocusSteps(25);
		focusScanBean.setNumberOfLinePoints(120);
		focusScanBean.setDetector(new MandelbrotModel());

		final LineMappingRegion lineRegion = new LineMappingRegion();
		lineRegion.setxStart(1.23);
		lineRegion.setxStop(6.78);
		lineRegion.setyStart(0.321);
		lineRegion.setyStop(0.289);

		focusScanBean.setLineRegion(lineRegion);
	}

	@After
	public void tearDown() throws Exception {
		mappingStageInfo = null;
	}

	@Test
	public void testConvertFocusScanBean() {
		final ScanRequest<?> scanRequest = focusScanConverter.convertToScanRequest(focusScanBean);

		// test compound model
		final CompoundModel<?> compoundModel = scanRequest.getCompoundModel();
		assertNotNull(compoundModel);
		final List<Object> models = compoundModel.getModels();
		assertEquals(2, models.size());

		// test outer model - the focus model
		assertThat(models.get(0), is(instanceOf(StepModel.class)));
		final StepModel focusModel = (StepModel) models.get(0);
		assertEquals(1, focusModel.getScannableNames().size());
		assertEquals(focusScanBean.getFocusScannableName(), focusModel.getScannableNames().get(0));
		assertEquals(focusScanBean.getFocusCentre() - focusScanBean.getFocusRange(), focusModel.getStart(), 1e-15);
		assertEquals(focusScanBean.getFocusCentre() + focusScanBean.getFocusRange() + focusModel.getStep() / 100,
				focusModel.getStop(), 1e-15);
		assertEquals(focusScanBean.getFocusRange() * 2 / focusScanBean.getNumberOfFocusSteps(), focusModel.getStep(), 1e-15);

		// test inner model - the line model
		assertThat(models.get(1), is(instanceOf(OneDEqualSpacingModel.class)));
		final OneDEqualSpacingModel lineModel = (OneDEqualSpacingModel) models.get(1);
		assertEquals(2, lineModel.getScannableNames().size());
		assertEquals(mappingStageInfo.getActiveSlowScanAxis(), lineModel.getScannableNames().get(1));
		assertEquals(mappingStageInfo.getActiveFastScanAxis(), lineModel.getScannableNames().get(0));
		assertEquals(focusScanBean.getNumberOfLinePoints(), lineModel.getPoints());
		final BoundingLine boundingLine = lineModel.getBoundingLine();
		assertNotNull(boundingLine);
		final LinearROI expectedRegion = (LinearROI) focusScanBean.getLineRegion().toROI();
		assertEquals(expectedRegion.getPointX(), boundingLine.getxStart(), 1e-15);
		assertEquals(expectedRegion.getPointY(), boundingLine.getyStart(), 1e-15);
		assertEquals(expectedRegion.getAngle(), boundingLine.getAngle(), 1e-15);
		assertEquals(expectedRegion.getLength(), boundingLine.getLength(), 1e-15);

		// test scan regions
		assertEquals(1, compoundModel.getRegions().size());
		final ScanRegion<?> scanRegion = compoundModel.getRegions().iterator().next();
		assertEquals(2, scanRegion.getScannables().size());
		assertEquals(mappingStageInfo.getActiveSlowScanAxis(), scanRegion.getScannables().get(0));
		assertEquals(mappingStageInfo.getActiveFastScanAxis(), scanRegion.getScannables().get(1));
		assertThat(scanRegion.getRoi(), is(instanceOf(LinearROI.class)));
		assertEquals(expectedRegion, scanRegion.getRoi());


		// test detectors
		Map<String, Object> detectors = scanRequest.getDetectors();
		assertNotNull(detectors);
		assertEquals(1, detectors.size());
		assertTrue(detectors.containsKey("mandelbrot"));
		assertThat(detectors.get("mandelbrot"), is(instanceOf(MandelbrotModel.class)));

		// test that the remaining fields have not been set
		assertNull(scanRequest.getMonitorNamesPerPoint());
		assertNull(scanRequest.getMonitorNamesPerScan());
		assertNull(scanRequest.getSampleData());
		assertNull(scanRequest.getScanMetadata());

		assertNull(scanRequest.getAfter());
		assertNull(scanRequest.getBefore());

		assertNull(scanRequest.getStart());
		assertNull(scanRequest.getEnd());
		assertNull(scanRequest.getFilePath());
	}

}
