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

package gda.mscan.element;


import static gda.mscan.element.AreaScanpath.GRID;
import static gda.mscan.element.AreaScanpath.LISSAJOUS;
import static gda.mscan.element.AreaScanpath.RASTER;
import static gda.mscan.element.AreaScanpath.SPIRAL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.LissajousModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import gda.device.Scannable;

@RunWith(MockitoJUnitRunner.class)
public class AreaScanpathTest {

	private static Map<AreaScanpath, Double[]> emptyPathData = new EnumMap<>(AreaScanpath.class);
	private static Map<AreaScanpath, Double[]> correctLengthPathData = new EnumMap<>(AreaScanpath.class);
	private static Double[] blankArray = new Double[]{};

	private List<Scannable> scannables;
	private List<Number> pathParams;
	private List<Number> bboxParams;
	private List<Mutator> mutators;

	@Mock
	private Scannable scannable1;

	@Mock
	private Scannable scannable2;

	@BeforeClass
	public static void setupClass() {
		for (AreaScanpath scanpath : AreaScanpath.values()) {
			emptyPathData.put(scanpath, new Double[] {});
		}
		correctLengthPathData.put(GRID, new Double[] {3.0, 3.0});
		correctLengthPathData.put(RASTER, new Double[] {4.0, 4.0});
		correctLengthPathData.put(SPIRAL, new Double[] {5.0});
		correctLengthPathData.put(LISSAJOUS, new Double[] {6.0, 6.0, 6.0, 6.0, 6.0});
	}

	@Before
	public void setUp() throws Exception {
		when(scannable1.getName()).thenReturn("name1");
		when(scannable2.getName()).thenReturn("name2");

		scannables = Arrays.asList(scannable1, scannable2);
		pathParams = new ArrayList<>();
		bboxParams = Arrays.asList(1.0, 2.0, 3.0, 4.0);
		mutators = new ArrayList<>();
	}

	@Test
	public void checkValueCounts() throws Exception {
		assertThat(GRID.valueCount(), is(2));
		assertThat(RASTER.valueCount(), is(2));
		assertThat(SPIRAL.valueCount(), is(1));
		assertThat(LISSAJOUS.valueCount(), is(5));
	}

	@Test
	public void checkModelTypes() throws Exception {
		assertTrue(GRID.modelType().equals(GridModel.class));
		assertTrue(RASTER.modelType().equals(RasterModel.class));
		assertTrue(SPIRAL.modelType().equals(SpiralModel.class));
		assertTrue(LISSAJOUS.modelType().equals(LissajousModel.class));
	}

	@Test
	public void createModelRejectsTooManyScannablesForAllInstances() throws Exception {
		scannables = Arrays.asList(scannable1, scannable2, scannable2);
		assertCreatingAllInstancesFailsIfWrongNoOfParams(emptyPathData, blankArray);
	}

	@Test
	public void createGridModelRejectsTooFewScannables() throws Exception {
		scannables = Arrays.asList(scannable1);
		assertCreatingAllInstancesFailsIfWrongNoOfParams(emptyPathData, blankArray);
	}

	@Test
	public void createModelRejectsTooManyPathParamsForAllInstances() throws Exception {
		Map<AreaScanpath, Double[]> tooMany = new EnumMap<>(AreaScanpath.class);
		tooMany.put(GRID, new Double[] {3.0, 3.0, 3.0});
		tooMany.put(RASTER, new Double[] {4.0, 4.0, 4.0});
		tooMany.put(SPIRAL, new Double[] {5.0, 5.0});
		tooMany.put(LISSAJOUS, new Double[] {6.0, 6.0, 6.0, 6.0, 6.0, 6.0});

		assertCreatingAllInstancesFailsIfWrongNoOfParams(tooMany, blankArray);
	}

	@Test
	public void createModelRejectsTooFewPathParamsForAllInstances() throws Exception {
		Map<AreaScanpath, Double[]> tooFew = new EnumMap<>(AreaScanpath.class);
		tooFew.put(GRID, new Double[] {3.0});
		tooFew.put(RASTER, new Double[] {4.0});
		tooFew.put(SPIRAL, new Double[] {});
		tooFew.put(LISSAJOUS, new Double[] {6.0, 6.0, 6.0, 6.0});

		assertCreatingAllInstancesFailsIfWrongNoOfParams(tooFew, blankArray);
	}

	@Test
	public void createModelRejectsTooManyBBoxParamsForAllInstances() throws Exception {
		Double[] bboxData = new Double[] {1.0, 2.0, 3.0, 4.0, 5.0};          // Too many path params for rectangle (4)
		assertCreatingAllInstancesFailsIfWrongNoOfParams(correctLengthPathData, bboxData);
	}

	@Test
	public void createModelRejectsTooFewBBoxParamsForAllInstances() throws Exception {
		assertCreatingAllInstancesFailsIfWrongNoOfParams(correctLengthPathData, blankArray);
	}

	private void assertCreatingAllInstancesFailsIfWrongNoOfParams(Map<AreaScanpath, Double[]> pathParams,
															Double[] bboxParams) throws Exception {
		for (AreaScanpath path: AreaScanpath.values()) {
			try {
				path.createModel(scannables, Arrays.asList(pathParams.get(path)), Arrays.asList(bboxParams), mutators);
				fail("Model created from path " + path + " when it shouldn't be possible");
			} catch (IllegalArgumentException e) {
				// should always go in here
			}
		}
	}

	@Test
	public void createModelCreatesCorrectModelForGrid() throws Exception {
		pathParams = Arrays.asList(5, 6);
		IScanPathModel model = GRID.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(GridModel.class)));
		GridModel gModel = (GridModel)model;
		assertThat(gModel.getScannableNames(), contains("name1", "name2"));
		assertThat(gModel.getBoundingBox().getFastAxisStart(), is(1.0));
		assertThat(gModel.getBoundingBox().getSlowAxisStart(), is(2.0));
		assertThat(gModel.getBoundingBox().getFastAxisLength(), is(3.0));
		assertThat(gModel.getBoundingBox().getSlowAxisLength(), is(4.0));
		assertThat(gModel.getFastAxisPoints(), is(5));
		assertThat(gModel.getSlowAxisPoints(), is(6));
		assertThat(gModel.getBoundingBox().getSlowAxisStart(), is(2.0));
		assertThat(gModel.isSnake(), is(false));
	}

	@Test
	public void createModelCreatesCorrectModelForRaster() throws Exception {
		pathParams = Arrays.asList(5.0, 6.0);
		IScanPathModel model = RASTER.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(RasterModel.class)));
		RasterModel rModel = (RasterModel)model;
		assertThat(rModel.getScannableNames(), contains("name1", "name2"));
		assertThat(rModel.getBoundingBox().getFastAxisStart(), is(1.0));
		assertThat(rModel.getBoundingBox().getSlowAxisStart(), is(2.0));
		assertThat(rModel.getBoundingBox().getFastAxisLength(), is(3.0));
		assertThat(rModel.getBoundingBox().getSlowAxisLength(), is(4.0));
		assertThat(rModel.getFastAxisStep(), is(5.0));
		assertThat(rModel.getSlowAxisStep(), is(6.0));
		assertThat(rModel.getBoundingBox().getSlowAxisStart(), is(2.0));
		assertThat(rModel.isSnake(), is(false));
	}

	@Test
	public void createModelCreatesCorrectModelForSpiral() throws Exception {
		pathParams = Arrays.asList(5.0);
		IScanPathModel model = SPIRAL.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(SpiralModel.class)));
		SpiralModel sModel = (SpiralModel)model;
		assertThat(sModel.getScannableNames(), contains("name1", "name2"));
		assertThat(sModel.getBoundingBox().getFastAxisStart(), is(1.0));
		assertThat(sModel.getBoundingBox().getSlowAxisStart(), is(2.0));
		assertThat(sModel.getBoundingBox().getFastAxisLength(), is(3.0));
		assertThat(sModel.getBoundingBox().getSlowAxisLength(), is(4.0));
		assertThat(sModel.getScale(), is(5.0));
		assertThat(sModel.getBoundingBox().getSlowAxisStart(), is(2.0));
	}

	@Test
	public void createModelsCorrectModelForLissajous() throws Exception {
		pathParams = Arrays.asList(5.0, 6.0, 7.0, 8.0, 9);
		IScanPathModel model = LISSAJOUS.createModel(scannables, pathParams, bboxParams, mutators);
		assertThat(model, is(instanceOf(LissajousModel.class)));
		LissajousModel lModel = (LissajousModel)model;
		assertThat(lModel.getScannableNames(), contains("name1", "name2"));
		assertThat(lModel.getBoundingBox().getFastAxisStart(), is(1.0));
		assertThat(lModel.getBoundingBox().getSlowAxisStart(), is(2.0));
		assertThat(lModel.getBoundingBox().getFastAxisLength(), is(3.0));
		assertThat(lModel.getBoundingBox().getSlowAxisLength(), is(4.0));
		assertThat(lModel.getA(), is(5.0));
		assertThat(lModel.getB(), is(6.0));
		assertThat(lModel.getDelta(), is(7.0));
		assertThat(lModel.getThetaStep(), is(8.0));
		assertThat(lModel.getPoints(), is(9));
		assertThat(lModel.getBoundingBox().getSlowAxisStart(), is(2.0));
	}
}
