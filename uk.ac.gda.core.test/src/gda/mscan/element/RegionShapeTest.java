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

import static gda.mscan.element.RegionShape.CENTRED_RECTANGLE;
import static gda.mscan.element.RegionShape.CIRCLE;
import static gda.mscan.element.RegionShape.LINE;
import static gda.mscan.element.RegionShape.POINT;
import static gda.mscan.element.RegionShape.POLYGON;
import static gda.mscan.element.RegionShape.RECTANGLE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RegionShapeTest {

	private static Map<RegionShape, List<Number>> correctLengthRegionShapeData = new EnumMap<>(RegionShape.class);

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void setupClass() {
		correctLengthRegionShapeData.put(RECTANGLE, Arrays.asList(5.0, 6.0, 7.0, 8.0));
		correctLengthRegionShapeData.put(CENTRED_RECTANGLE, Arrays.asList(5.0, 6.0, 7.0, 8.0));
		correctLengthRegionShapeData.put(CIRCLE, Arrays.asList(5.0, 6.0, 7.0));
		correctLengthRegionShapeData.put(POLYGON, Arrays.asList(5.0, 6.0, 7.0, 8.0, 9.0, 10.0));
		correctLengthRegionShapeData.put(LINE, Arrays.asList(5.0, 6.0, 7.0, 8.0));
		correctLengthRegionShapeData.put(POINT, Arrays.asList(5.0, 6.0));
	}

	@Test
	public void valueCount() throws Exception {
		assertThat(RECTANGLE.valueCount(), is(4));
		assertThat(CENTRED_RECTANGLE.valueCount(), is(4));
		assertThat(CIRCLE.valueCount(), is(3));
		assertThat(POLYGON.valueCount(), is(6));
		assertThat(LINE.valueCount(), is(4));
		assertThat(POINT.valueCount(), is(2));
	}

	@Test
	public void roiType() throws Exception {
		assertTrue(RECTANGLE.roiType().equals(RectangularROI.class));
		assertTrue(CENTRED_RECTANGLE.roiType().equals(RectangularROI.class));
		assertTrue(CIRCLE.roiType().equals(CircularROI.class));
		assertTrue(POLYGON.roiType().equals(PolygonalROI.class));
		assertTrue(LINE.roiType().equals(LinearROI.class));
		assertTrue(POINT.roiType().equals(PointROI.class));
	}

	@Test
	public void createRegionShapeRejectsTooManyParamsForAllInstancesExceptPoly() throws Exception {
		Map<RegionShape, Double[]> tooMany = new EnumMap<>(RegionShape.class);
		tooMany.put(RECTANGLE, new Double[] {1.0, 2.0, 3.0, 4.0, 5.0});
		tooMany.put(CENTRED_RECTANGLE, new Double[] {1.0, 2.0, 3.0, 4.0, 5.0});
		tooMany.put(CIRCLE, new Double[] {1.0, 2.0, 3.0, 4.0});
		tooMany.put(POLYGON, new Double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0});  // can have up to Integer.MAX_VALUE
		tooMany.put(LINE, new Double[] {1.0, 2.0, 3.0, 4.0, 5.0});
		tooMany.put(POINT, new Double[] {1.0, 2.0, 3.0});

		List<RegionShape> rejected = assertCreatingAllInstancesFailsIfWrongNoOfParams(tooMany);
		assertThat(rejected, contains(RECTANGLE, CENTRED_RECTANGLE, CIRCLE, LINE, POINT));
	}

	@Test
	public void RegionShapeRejectsTooFewParamsForAllInstances() throws Exception {
		Map<RegionShape, Double[]> tooFew = new EnumMap<>(RegionShape.class);
		tooFew.put(RECTANGLE, new Double[] {1.0, 2.0, 3.0});
		tooFew.put(CENTRED_RECTANGLE, new Double[] {1.0, 2.0, 3.0});
		tooFew.put(CIRCLE, new Double[] {1.0, 2.0,});
		tooFew.put(POLYGON, new Double[] {1.0, 2.0, 3.0, 4.0});
		tooFew.put(LINE, new Double[] {1.0, 2.0, 3.0});
		tooFew.put(POINT, new Double[] {1.0});

		List<RegionShape> rejected = assertCreatingAllInstancesFailsIfWrongNoOfParams(tooFew);
		assertThat(rejected, contains(RECTANGLE, CENTRED_RECTANGLE, CIRCLE, POLYGON, LINE, POINT));
	}

	private List<RegionShape> assertCreatingAllInstancesFailsIfWrongNoOfParams(Map<RegionShape, Double[]> params) throws Exception {
		ArrayList<RegionShape> rejected = new ArrayList<>();
		for (RegionShape shape: RegionShape.values()) {
			try {
				shape.createIROI(Arrays.asList(params.get(shape)));
				if (shape.hasFixedValueCount() || params.get(shape).length < shape.valueCount()) {
					fail("ROI created from path " + shape + " when it shouldn't be possible");
				}
			} catch (IllegalArgumentException e) {
				// should always go in here except for rois with no upper limit on params
				rejected.add(shape);
			}
		}
		return rejected;
	}

	@Test
	public void createPolygonRoiRejectsOddNumbersOfParameters() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("PolygonalROI requires an even number of params");
		POLYGON.createIROI(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)) ;
	}

	@Test
	public void createRoiRejectsSidesOfZeroWidthForRectangle() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Rectangle sides must have non-zero length");
		RECTANGLE.createIROI(Arrays.asList(1.0, 2.0, 1.0, 4.0));
	}

	@Test
	public void createRoiRejectsSidesOfZeroHeightForRectangle() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Rectangle sides must have non-zero length");
		RECTANGLE.createIROI(Arrays.asList(1.0, 2.0, 3.0, 2.0));
	}

	@Test
	public void createRoiCreatesCorrectIROIForRectangle() throws Exception {
		IROI roi = RECTANGLE.createIROI(correctLengthRegionShapeData.get(RECTANGLE));
		assertThat(roi, instanceOf(RectangularROI.class));
		RectangularROI rRoi = (RectangularROI)roi;
		assertThat(roi.getPointX(), is(5.0));
		assertThat(roi.getPointY(), is(6.0));
		assertThat(rRoi.getLengths()[0], is(2.0));
		assertThat(rRoi.getLengths()[1], is(2.0));
		assertThat(rRoi.getAngle(), is(closeTo(0, 1e-10)));
	}

	@Test
	public void createRoiCreatesCorrectIROIForRectangleWithNegativeSideLength() throws Exception {
		List<Number> coords = correctLengthRegionShapeData.get(RECTANGLE);
		coords.set(2, 1 - coords.get(2).doubleValue());
		IROI roi = RECTANGLE.createIROI(correctLengthRegionShapeData.get(RECTANGLE));
		assertThat(roi, instanceOf(RectangularROI.class));
		RectangularROI rRoi = (RectangularROI)roi;
		assertThat(roi.getPointX(), is(-6.0));
		assertThat(roi.getPointY(), is(6.0));
		assertThat(rRoi.getLengths()[0], is(11.0));
		assertThat(rRoi.getLengths()[1], is(2.0));
		assertThat(rRoi.getAngle(), is(closeTo(0, 1e-10)));
	}

	@Test
	public void createRoiRejectsSidesOfZeroWidthForCentredRectangle() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Centred Rectangle must have positive width/height dimensions");
		CENTRED_RECTANGLE.createIROI(Arrays.asList(1.0, 2.0, 0, 4.0));
	}

	@Test
	public void createRoiRejectsSidesOfZeroHeightForCentredRectangle() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Centred Rectangle must have positive width/height dimensions");
		CENTRED_RECTANGLE.createIROI(Arrays.asList(1.0, 2.0, 2.0, 0));
	}

	@Test
	public void createRoiRejectsSidesOfNegativeWidthForCentredRectangle() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Centred Rectangle must have positive width/height dimensions");
		CENTRED_RECTANGLE.createIROI(Arrays.asList(1.0, 2.0, -20, 4.0));
	}

	@Test
	public void createRoiRejectsSidesOfNegativeHeightForCentredRectangle() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Centred Rectangle must have positive width/height dimensions");
		CENTRED_RECTANGLE.createIROI(Arrays.asList(1.0, 2.0, 2.0, -10));
	}

	@Test
	public void createRoiCreatesCorrectIROIForCentredRectangle() throws Exception {
		IROI roi = CENTRED_RECTANGLE.createIROI(correctLengthRegionShapeData.get(CENTRED_RECTANGLE));
		assertThat(roi, instanceOf(RectangularROI.class));
		RectangularROI rRoi = (RectangularROI)roi;
		assertThat(roi.getPointX(), is(1.5));
		assertThat(roi.getPointY(), is(2.0));
		assertThat(rRoi.getLengths()[0], is(7.0));
		assertThat(rRoi.getLengths()[1], is(8.0));
		assertThat(rRoi.getAngle(), is(closeTo(0, 1e-10)));
	}

	@Test
	public void createRoiRejectsSidesOfZeroWidthForCircle() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Circle must have a positive radius");
		CIRCLE.createIROI(Arrays.asList(1.0, 2.0, 0));
	}
	@Test
	public void createRoiRejectsSidesOfNegativeWidthForCircle() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Circle must have a positive radius");
		CIRCLE.createIROI(Arrays.asList(1.0, 2.0, -6));
	}

	@Test
	public void createRoiCreatesCorrectIROIForCircle() throws Exception {
		IROI roi = CIRCLE.createIROI(correctLengthRegionShapeData.get(CIRCLE));
		assertThat(roi, instanceOf(CircularROI.class));
		CircularROI cRoi = (CircularROI)roi;
		assertThat(roi.getPointX(), is(5.0));
		assertThat(roi.getPointY(), is(6.0));
		assertThat(cRoi.getRadius(), is(7.0));
	}

	@Test
	public void createRoiCreatesCorrectIROIForPolygon() throws Exception {
		IROI roi = POLYGON.createIROI(correctLengthRegionShapeData.get(POLYGON));
		assertThat(roi, instanceOf(PolygonalROI.class));
		PolygonalROI pRoi = (PolygonalROI)roi;
		assertThat(pRoi.getPoints().get(0).getPointX(), is(5.0));
		assertThat(pRoi.getPoints().get(0).getPointY(), is(6.0));
		assertThat(pRoi.getPoints().get(1).getPointX(), is(7.0));
		assertThat(pRoi.getPoints().get(1).getPointY(), is(8.0));
		assertThat(pRoi.getPoints().get(2).getPointX(), is(9.0));
		assertThat(pRoi.getPoints().get(2).getPointY(), is(10.0));
	}

	@Test
	public void createRoiRejectsZeroLengthForLine() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Linear Roi must have non-zero length");
		LINE.createIROI(Arrays.asList(1.0, 1.0, 1.0, 1.0));
	}

	@Test
	public void createRoiCreatesCorrectIROIForLine() throws Exception {
		IROI roi = LINE.createIROI(correctLengthRegionShapeData.get(LINE));
		assertThat(roi, instanceOf(LinearROI.class));
		LinearROI lRoi = (LinearROI)roi;
		assertThat(roi.getPointX(), is(5.0));
		assertThat(roi.getPointY(), is(6.0));
		assertThat(lRoi.getEndPoint()[0], is(7.0));
		assertThat(lRoi.getEndPoint()[1], is(8.0));
	}

	@Test
	public void createRoiAcceptsXAxisOnlyLengthForLine() throws Exception {
		IROI roi = LINE.createIROI(Arrays.asList(1.0, 1.0, 5.0, 1.0));
		assertThat(roi, instanceOf(LinearROI.class));
		LinearROI lRoi = (LinearROI)roi;
		assertThat(roi.getPointX(), is(1.0));
		assertThat(roi.getPointY(), is(1.0));
		assertThat(lRoi.getEndPoint()[0], is(5.0));
		assertThat(lRoi.getEndPoint()[1], is(1.0));
	}

	@Test
	public void createRoiAcceptsYAxisOnlyLengthForLine() throws Exception {
		IROI roi = LINE.createIROI(Arrays.asList(1.0, 1.0, 1.0, 5.0));
		assertThat(roi, instanceOf(LinearROI.class));
		LinearROI lRoi = (LinearROI)roi;
		assertThat(roi.getPointX(), is(1.0));
		assertThat(roi.getPointY(), is(1.0));
		assertThat(lRoi.getEndPoint()[0], is(1.0));
		assertThat(lRoi.getEndPoint()[1], is(5.0));
	}

	@Test
	public void createRoiCreatesCorrectIROIForPoint() throws Exception {
		IROI roi = POINT.createIROI(correctLengthRegionShapeData.get(POINT));
		assertThat(roi, instanceOf(PointROI.class));
		PointROI pRoi = (PointROI)roi;
		assertThat(pRoi.getPointX(), is(5.0));
		assertThat(pRoi.getPointY(), is(6.0));
	}
}
