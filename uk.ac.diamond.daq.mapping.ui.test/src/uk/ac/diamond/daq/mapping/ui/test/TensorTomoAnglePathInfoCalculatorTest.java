/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsArray;
import org.hamcrest.number.IsCloseTo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.PathInfoCalculationException;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoAnglePathInfoCalculator;
import uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoPathInfo;
import uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoPathInfo.StepSizes;
import uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoPathRequest;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class TensorTomoAnglePathInfoCalculatorTest {

	private static final String X_POS_NAME = "xPos";
	private static final String Y_POS_NAME = "yPos";
	private static final String ANGLE_1_NAME = "omega";
	private static final String ANGLE_2_NAME = "phi";

	private static final int INNER_POINT_COUNT = 25;
	private static final double TOLERANCE = 1e-5;

	private static final TwoAxisGridPointsModel MAP_PATH_MODEL = new TwoAxisGridPointsModel(X_POS_NAME, Y_POS_NAME, 5, 5);
	private static final RectangularMappingRegion MAP_REGION = new RectangularMappingRegion(0.0, 10.0, 0.0, 5.00);

	private static TensorTomoAnglePathInfoCalculator<?> tomoAnglePathCalculator;

	private static double[] expectedXPositions;
	private static double[] expectedYPositions;

	@BeforeClass
	public static void setUpClass() throws Exception {
		final IPointGeneratorService pointGenService = new PointGeneratorService();

		ServiceProvider.setService(IPointGeneratorService.class, pointGenService);
		ServiceProvider.setService(IValidatorService.class, new ValidatorService());

		new org.eclipse.scanning.device.ui.ServiceHolder().setGeneratorService(pointGenService);
		new org.eclipse.scanning.sequencer.ServiceHolder().setGeneratorService(pointGenService);

		tomoAnglePathCalculator = new TensorTomoAnglePathInfoCalculator<>(pointGenService);

		final IPointGenerator<?> pointGen = pointGenService.createGenerator(MAP_PATH_MODEL, List.of(MAP_REGION.toROI()));
		final List<IPosition> mapPoints = pointGen.createPoints();
		expectedXPositions = mapPoints.stream().map(pos -> pos.get(X_POS_NAME)).mapToDouble(Double.class::cast).toArray();
		expectedYPositions = mapPoints.stream().map(pos -> pos.get(Y_POS_NAME)).mapToDouble(Double.class::cast).toArray();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		ServiceProvider.reset();
	}

	@Test
	public void testArrayModelArrayModel_limitedRange() throws Exception {
		testArrayModelArrayModel(new AxialArrayModel(ANGLE_1_NAME, 0, 2, 5, 15, 30, 55));
	}

	@Test
	public void testArrayModelArrayModel_fullRange() throws Exception {
		testArrayModelArrayModel(new AxialArrayModel(ANGLE_1_NAME, -180, -135, -90, -45, 0, 45, 90, 135, 180));
	}

	private void testArrayModelArrayModel(AxialArrayModel angle1Model) throws Exception {
		// AxialArrayModels for both angles, same points for every angle1 line, selectedArea
		final IAxialModel angle2Model = new AxialArrayModel(ANGLE_2_NAME, 0, 10, 25, 60);

		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.none());
	}

	@Test
	public void testArrayModelPointsModel_limitedRange() throws Exception {
		final AxialArrayModel angle1Model = new AxialArrayModel(ANGLE_1_NAME,  0, 1, 2, 5, 13, 21, 34, 45, 60);
		final double[] expectedAngle2StepSizes = { 22.5, 30.0, 30.0, 30.0, 30.0, 30.0, 30.0, 45.0, 45.0 };
		testArrayModelPointsModel(angle1Model, expectedAngle2StepSizes);
	}

	@Test
	public void testArrayModelPointsModel_fullRange() throws Exception {
		final AxialArrayModel angle1Model = new AxialArrayModel(ANGLE_1_NAME, -180, -135, -90, -45, 0, 45, 90, 135, 180);
		final double[] expectedAngle2StepSizes = { 22.5, 30.0, 180.0, 45.0, 22.5, 45.0, 180.0, 30.0, 22.5 };
		testArrayModelPointsModel(angle1Model, expectedAngle2StepSizes);
	}

	private void testArrayModelPointsModel(AxialArrayModel angle1Model, double[] expectedAngle2StepSizes) throws Exception {
		final IAxialModel angle2Model = new AxialPointsModel(ANGLE_2_NAME, 0, 90, 5);
		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.forSingleStepSizes(expectedAngle2StepSizes));
	}

	@Test
	public void testArrayModelStepModel_limitedRange() throws Exception {
		final AxialArrayModel angle1Model = new AxialArrayModel(ANGLE_1_NAME,  0, 1, 2, 5, 13, 21, 34, 55);
		final double[] expectedAngle2StepSizes = { 22.5, 30.0, 30.0, 30.0, 30.0, 30.0, 30.0, 45.0 };
		testArrayModelStepModel(angle1Model, expectedAngle2StepSizes);
	}

	@Test
	public void testArrayModelStepModel_fullRange() throws Exception {
		final AxialArrayModel angle1Model = new AxialArrayModel(ANGLE_1_NAME, -180, -135, -90, -45, 0, 45, 90, 135, 180);
		final double[] expectedAngle2StepSizes = { 22.5, 30.0, 180.0, 45.0, 22.5, 45.0, 180.0, 30.0, 22.5 };
		testArrayModelStepModel(angle1Model, expectedAngle2StepSizes);
	}

	private void testArrayModelStepModel(AxialArrayModel angle1Model, double[] expectedAngle2StepSizes) throws Exception {
		final IAxialModel angle2Model = new AxialStepModel(ANGLE_2_NAME, 0, 90, 20);
		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.forSingleStepSizes(expectedAngle2StepSizes));
	}

	@Test
	public void testStepModelArrayModel_limitedRange() throws Exception {
		testStepModelArrayModel(new AxialStepModel(ANGLE_1_NAME, 0, 50, 10));
	}

	@Test
	public void testStepModelArrayModel_fullRange() throws Exception {
		testStepModelArrayModel(new AxialStepModel(ANGLE_1_NAME, -180, 180, 30));
	}

	private void testStepModelArrayModel(AxialStepModel angle1Model) throws Exception {
		final IAxialModel angle2Model = new AxialArrayModel(ANGLE_2_NAME, new double[] { 0, 10, 25, 60 });

		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.none());
	}

	@Test
	public void testStepModelPointsModel_limitedRange() throws Exception {
		final AxialStepModel angle1Model = new AxialStepModel(ANGLE_1_NAME, 0, 50, 10);
		final double[] expectedAngle2StepSizes = { 6.428571428571429, 6.923076923076923, 6.923076923076923, 7.5, 9.0, 11.25 };
		testStepModelPointsModel(angle1Model, expectedAngle2StepSizes);
	}

	@Test
	public void testStepModelPointsModel_fullRange() throws Exception {
		final AxialStepModel angle1Model = new AxialStepModel(ANGLE_1_NAME, -180, 180, 30);
		final double[] expectedAngle2StepSizes = { 6.428571428571429, 6.923076923076923, 12.857142857142858, 180.0, 12.857142857142858,
				7.5, 6.428571428571429, 7.5, 12.857142857142858, 180.0, 12.857142857142858, 6.923076923076923, 6.428571428571429 };
		testStepModelPointsModel(angle1Model, expectedAngle2StepSizes);
	}

	private void testStepModelPointsModel(AxialStepModel angle1Model, double[] expectedAngle2StepSizes) throws Exception {
		final IAxialModel angle2Model = new AxialPointsModel(ANGLE_2_NAME, 30, 120, 15);

		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.forSingleStepSizes(expectedAngle2StepSizes));
	}

	@Test
	public void testStepModelMultiStepModel_limitedRange() throws Exception {
		final AxialStepModel angle1Model = new AxialStepModel(ANGLE_1_NAME, 0, 45, 10);
		final double[][] expectedAngle2StepSizes = {
				{ 30.0, 1.0, 5.0 },
				{ 45.0, 1.25, 6.25 },
				{ 45.0, 1.25, 6.25 },
				{ 45.0, 1.25, 6.25 },
				{ 45.0, 1.6666666666666667, 8.333333333333334 }
		};
		testStepModelMultiStepModel(angle1Model, expectedAngle2StepSizes);
	}

	@Test
	public void testStepModelMultiStepModel_fullRange() throws Exception {
		final AxialStepModel angle1Model = new AxialStepModel(ANGLE_1_NAME, -180, 180, 30);
		final double[][] expectedAngle2StepSizes = {
				{ 30.0, 1.0, 5.0 },
				{ 30.0, 1.0, 5.0 },
				{ 45.0, 1.6666666666666667, 8.333333333333334 },
				{ 180.0, 10.0, 50.0 },
				{ 90.0, 2.5, 12.5 },
				{ 45.0, 1.25, 6.25 },
				{ 30.0, 1.0, 5.0 },
				{ 45.0, 1.25, 6.25 },
				{ 90.0, 2.5, 12.5 },
				{ 180.0, 10.0, 50.0 },
				{ 45.0, 1.6666666666666667, 8.333333333333334 },
				{ 30.0, 1.0, 5.0 },
				{ 30.0, 1.0, 5.0 }
			};
		testStepModelMultiStepModel(angle1Model, expectedAngle2StepSizes);
	}

	private void testStepModelMultiStepModel(AxialStepModel angle1Model, double[][] expectedAngle2StepSizes) throws Exception {
		final IAxialModel angle2Model = new AxialMultiStepModel(ANGLE_2_NAME, List.of(
				new AxialStepModel(ANGLE_2_NAME, 0, 90, 30),
				new AxialStepModel(ANGLE_2_NAME, 90, 95, 1),
				new AxialStepModel(ANGLE_2_NAME, 95, 120, 5)));
		angle2Model.setContinuous(false);
		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.forMultiStepSizes(expectedAngle2StepSizes));
	}

	@Test
	public void testPointsModelArrayModel_limitedRange() throws Exception {
		testPointsModelArrayModel(new AxialPointsModel(ANGLE_1_NAME, 0, 50, 10));
	}

	@Test
	public void testPointsModelArrayModel_fullRange() throws Exception {
		testPointsModelArrayModel(new AxialPointsModel(ANGLE_1_NAME, -90, 90, 7));
	}

	private void testPointsModelArrayModel(AxialPointsModel angle1Model) throws Exception {
		final IAxialModel angle2Model = new AxialArrayModel(ANGLE_2_NAME, new double[] { 0, 10, 25, 60 });

		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.none());
	}

	@Test
	public void testPointsModelPointsModel_limitedRange() throws Exception {
		final AxialPointsModel angle1Model = new AxialPointsModel(ANGLE_1_NAME, 0, 45, 10);
		final double[] expectedAngle2StepSizes = { 8.571428571428571, 9.23076923076923, 9.23076923076923,
				9.23076923076923, 9.23076923076923, 10.0, 10.0, 10.909090909090908, 12.0, 13.333333333333334 };
		testPointsModelPointsModel(angle1Model, expectedAngle2StepSizes);
	}

	@Test
	public void testPointsModelPointsModel_fullRange() throws Exception {
		final AxialPointsModel angle1Model =  new AxialPointsModel(ANGLE_1_NAME, -90, 90, 9);
		final double[] expectedAngle2StepSizes = { 240.0, 24.0, 13.333333333333334, 10.0, 8.571428571428571, 10.0, 13.333333333333334, 24.0, 240.0 };
		testPointsModelPointsModel(angle1Model, expectedAngle2StepSizes);
	}

	private void testPointsModelPointsModel(AxialPointsModel angle1Model, double[] expectedAngle2StepSizes) throws Exception {
		final IAxialModel angle2Model = new AxialPointsModel(ANGLE_2_NAME, 0, 120, 15);

		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.forSingleStepSizes(expectedAngle2StepSizes));
	}

	@Test
	public void testPointsModelMultiStepModel_limitedRange() throws Exception {
		final AxialPointsModel angle1Model = new AxialPointsModel(ANGLE_1_NAME, 0, 50, 10);
		final double[][] expectedAngle2StepSizes = {
				{ 30.0, 1.0, 5.0 },
				{ 45.0, 1.25, 6.25 },
				{ 45.0, 1.25, 6.25},
				{ 45.0, 1.25, 6.25},
				{ 45.0, 1.25, 6.25},
				{ 45.0, 1.25, 6.25},
				{ 45.0, 1.25, 6.25},
				{ 45.0, 1.6666666666666667, 8.333333333333334 },
				{ 45.0, 1.6666666666666667, 8.333333333333334 },
				{ 90.0, 1.6666666666666667, 8.333333333333334}
		};

		testPointsModelMultiStepModel(angle1Model, expectedAngle2StepSizes);
	}

	@Test
	public void testPointsModelMultiStepModel_fullRange() throws Exception {
		final AxialPointsModel angle1Model = new AxialPointsModel(ANGLE_1_NAME, -90, 90, 7);
		final double[][] expectedAngle2StepSizes = {
				{ 180.0, 10.0, 50.0 },
				{ 90.0, 2.5, 12.5 },
				{ 45.0, 1.25, 6.25 },
				{ 30.0, 1.0, 5.0 },
				{ 45.0, 1.25, 6.25 },
				{ 90.0, 2.5, 12.5 },
				{ 180.0, 10.0, 50.0 }
		};
		testPointsModelMultiStepModel(angle1Model, expectedAngle2StepSizes);
	}

	private void testPointsModelMultiStepModel(AxialPointsModel angle1Model, double[][] expectedAngle2StepSizes) throws Exception {
		final IAxialModel angle2Model = new AxialMultiStepModel(ANGLE_2_NAME, List.of(
				new AxialStepModel(ANGLE_2_NAME, 0, 90, 30),
				new AxialStepModel(ANGLE_2_NAME, 90, 95, 1),
				new AxialStepModel(ANGLE_2_NAME, 95, 120, 5)));
		angle2Model.setContinuous(false);

		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.forMultiStepSizes(expectedAngle2StepSizes));
	}

	@Test
	public void testMultiStepModelArrayModel_limitedRange() throws Exception {
		testMultiStepModelArrayModel(new AxialMultiStepModel(ANGLE_1_NAME, List.of(
				new AxialStepModel(ANGLE_1_NAME, 0, 45, 15),
				new AxialStepModel(ANGLE_1_NAME, 45, 50, 1))));
	}

	@Test
	public void testMultiStepModelArrayModel_fullRange() throws Exception {
		testMultiStepModelArrayModel(new AxialMultiStepModel(ANGLE_1_NAME, List.of(
				new AxialStepModel(ANGLE_1_NAME, -90, 0, 30),
				new AxialStepModel(ANGLE_1_NAME, 0, 30, 10),
				new AxialStepModel(ANGLE_1_NAME, 30, 90, 30))));
	}

	private void testMultiStepModelArrayModel(AxialMultiStepModel angle1Model) throws Exception {
		angle1Model.setContinuous(false);
		final IAxialModel angle2Model = new AxialArrayModel(ANGLE_2_NAME, 0, 10, 20, 25, 40, 52, 75);

		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.none());
	}

	@Test
	public void testMultiStepModelPointsModel_limitedRange() throws Exception {
		final AxialMultiStepModel angle1Model = new AxialMultiStepModel(ANGLE_1_NAME, List.of(
				new AxialStepModel(ANGLE_1_NAME, 0, 45, 15),
				new AxialStepModel(ANGLE_1_NAME, 45, 50, 1)));
		final double[] expectedAngle2StepSizes = { 10.0, 11.25, 12.857142857142858, 15.0, 15.0, 15.0, 15.0, 15.0, 18.0, 18.0 };
		testMultiStepModelPointsModel(angle1Model, expectedAngle2StepSizes);
	}

	@Test
	public void testMultiStepModelPointsModel_fullRange() throws Exception {
		final AxialMultiStepModel angle1Model = new AxialMultiStepModel(ANGLE_1_NAME, List.of(
				new AxialStepModel(ANGLE_1_NAME, -90, 0, 30),
				new AxialStepModel(ANGLE_1_NAME, 0, 30, 10),
				new AxialStepModel(ANGLE_1_NAME, 30, 90, 30)));
		final double[] expectedAngle2StepSizes = { 180.0, 22.5, 12.857142857142858, 10.0, 10.0,
				11.25, 11.25, 12.857142857142858, 12.857142857142858, 22.5, 180.0 };
		testMultiStepModelPointsModel(angle1Model, expectedAngle2StepSizes);
	}

	private void testMultiStepModelPointsModel(AxialMultiStepModel angle1Model, double[] expectedAngle2StepSizes) throws Exception {
		angle1Model.setContinuous(false);
		final IAxialModel angle2Model = new AxialPointsModel(ANGLE_2_NAME, 0, 90, 10);

		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.forSingleStepSizes(expectedAngle2StepSizes));
	}

	@Test
	public void testMultiStepModelStepModel_limitedRange() throws Exception {
		testMultiStepModelStepModel(new AxialMultiStepModel(ANGLE_1_NAME, List.of(
				new AxialStepModel(ANGLE_1_NAME, 0, 45, 15),
				new AxialStepModel(ANGLE_1_NAME, 45, 50, 1))));
	}

	@Test
	public void testMultiStepModelStepModel_fullRange() throws Exception {
		testMultiStepModelArrayModel(new AxialMultiStepModel(ANGLE_1_NAME, List.of(
				new AxialStepModel(ANGLE_1_NAME, -90, 0, 30),
				new AxialStepModel(ANGLE_1_NAME, 0, 30, 10),
				new AxialStepModel(ANGLE_1_NAME, 30, 90, 30))));
	}

	private void testMultiStepModelStepModel(AxialMultiStepModel angle1Model) throws Exception {
		angle1Model.setContinuous(false);
		final IAxialModel angle2Model = new AxialStepModel(ANGLE_2_NAME, 30, 120, 15);
		final double[] expectedAngle2StepSizes = { 15.0, 18.0, 18.0, 22.5, 22.5, 22.5, 22.5, 22.5, 30.0, 30.0 };

		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.forSingleStepSizes(expectedAngle2StepSizes));
	}

	@Test
	public void testMultiStepModelMultiStepModel_limitedRange() throws Exception {
		final AxialMultiStepModel angle1Model = new AxialMultiStepModel(ANGLE_1_NAME, List.of(
				new AxialStepModel(ANGLE_1_NAME, 0, 15, 3),
				new AxialStepModel(ANGLE_1_NAME, 15, 90, 15)));
		final double[][] expectedAngle2StepSizes = {
				{ 30.0, 5.0, 30.0 },
				{ 45.0, 6.0, 60.0 },
				{ 45.0, 6.0, 60.0 },
				{ 45.0, 6.0, 60.0 },
				{ 45.0, 6.0, 60.0 },
				{ 45.0, 6.0, 60.0 },
				{ 45.0, 6.0, 60.0 },
				{ 45.0, 6.0, 60.0 },
				{ 45.0, 7.5, 60.0 },
				{ 90.0, 10.0, 60.0 },
				{ 180.0, 30.0, 120.0 },
				{ 180.0, 60.0, 120.0 }
		};
		testMultiStepModelMultiStepModel(angle1Model, expectedAngle2StepSizes);
	}

	@Test
	public void testMultiStepModelMultiStepModel_fullRange() throws Exception {
		final AxialMultiStepModel angle1Model = new AxialMultiStepModel(ANGLE_1_NAME, List.of(
				new AxialStepModel(ANGLE_1_NAME, -90, 0, 30),
				new AxialStepModel(ANGLE_1_NAME, 0, 30, 10),
				new AxialStepModel(ANGLE_1_NAME, 30, 90, 30)));
		final double[][] expectedAngle2StepSizes = {
				{ 180.0, 60.0, 120.0 },
				{ 90.0, 10.0, 60.0 },
				{ 45.0, 6.0, 60.0 },
				{ 30.0, 5.0, 30.0 },
				{ 30.0, 5.0, 30.0 },
				{ 45.0, 6.0, 60.0 },
				{ 45.0, 6.0, 60.0 },
				{ 45.0, 6.0, 60.0 },
				{ 45.0, 6.0, 60.0 },
				{ 90.0, 10.0, 60.0 },
				{ 180.0, 60.0, 120.0 }
		};
		testMultiStepModelMultiStepModel(angle1Model, expectedAngle2StepSizes);
	}

	private void testMultiStepModelMultiStepModel(AxialMultiStepModel angle1Model, double[][] expectedAngle2StepSizes) throws Exception {
		angle1Model.setContinuous(false);

		final IAxialModel angle2Model = new AxialMultiStepModel(ANGLE_2_NAME, List.of(
				new AxialStepModel(ANGLE_2_NAME, 0, 90, 30),
				new AxialStepModel(ANGLE_2_NAME, 90, 120, 5),
				new AxialStepModel(ANGLE_2_NAME, 120, 180, 30)));
		angle2Model.setContinuous(false);

		testTomoPathCalculation(angle1Model, angle2Model, StepSizes.forMultiStepSizes(expectedAngle2StepSizes));
	}

	@Test
	public void testCalculateTomoPathAngle_stepModel() throws Exception {
		// a particular set of results from Tim Snow
		final IAxialModel angle1Model = new AxialStepModel(ANGLE_1_NAME, 0, 45, 11.25); // 0, 11.25, 22.5, 33.75, 45
		final IAxialModel angle2Model = new AxialStepModel(ANGLE_2_NAME, 0, 360, 11.25);

		final TensorTomoPathInfo info = calculateTomoPathInfo(angle1Model, angle2Model);

		final double[] expectedAngle1Positions = { 0.0, 11.25, 22.5, 33.75, 45 };
		assertThat(info.getAngle1Positions(), isArrayCloseTo(expectedAngle1Positions));

		final double[] expectedAngle2StepSizes = { 11.25, 11.61290323, 12.4137931, 13.84615385, 16.36363636 };
		assertThat(info.getAngle2StepSizes().getOneDStepSizes(), isArrayCloseTo(expectedAngle2StepSizes));

		final double[][] expectedAngle2Positions = Arrays.stream(info.getAngle2StepSizes().getOneDStepSizes())
			.mapToObj(stepSize -> new AxialStepModel(ANGLE_2_NAME, 0, 360, stepSize))
			.map(model -> getPositions(model))
			.toArray(double[][]::new);
		assertThat(info.getAngle2Positions(), is2DArrayCloseTo(expectedAngle2Positions));
	}

	@Test
	public void testCalculateTomoPathAngle_pointsModel() throws Exception {
		// a particular set of results from Tim Snow, adapted to use a points model
		final IAxialModel angle1Model = new AxialStepModel(ANGLE_1_NAME, 0, 45, 11.25); // 0, 11.25, 22.5, 33.75, 45
		final IAxialModel angle2Model = new AxialPointsModel(ANGLE_2_NAME, 0, 360, 33);

		final TensorTomoPathInfo info = calculateTomoPathInfo(angle1Model, angle2Model);

		final double[] expectedAngle1Positions = { 0, 11.25, 22.5, 33.75, 45 };
		assertThat(info.getAngle1Positions(), isArrayCloseTo(expectedAngle1Positions));

		final double[] expectedAngle2StepSizes = { 11.25, 11.61290323, 12.4137931, 13.84615385, 16.36363636 };
		assertThat(info.getAngle2StepSizes().getOneDStepSizes(), isArrayCloseTo(expectedAngle2StepSizes));

		final double[][] expectedAngle2Positions = Arrays.stream(info.getAngle2StepSizes().getOneDStepSizes())
				.mapToObj(stepSize -> new AxialStepModel(ANGLE_2_NAME, 0, 360, stepSize))
				.map(model -> getPositions(model))
				.toArray(double[][]::new);
		assertThat(info.getAngle2Positions(), is2DArrayCloseTo(expectedAngle2Positions));
	}

	@Test
	public void testFormulaDirectly() throws Exception {
		// a test that the formula produces the same result as the previous script, below

		//	rot_angles = [0, 11.25, 22.5, 33.75, 45] #- full range, rerunning lesser range
		//	number_of_projections = 32 # 360 divided by step in primary rotation angle
		//
		//	for rot_angle in rot_angles:
		//	    if (rot_angle == 0):
		//	        prot_step_size = 11
		//	        prot_angles = np.arange(0, 181, prot_step_size)
		//	    else:
		//	        prot_step_size = 360 / (np.floor(number_of_projections * np.cos(rot_angle * (np.pi / 180))))
		//	        prot_angles = np.arange(0, 361, prot_step_size)

		final double[] angle1Positions = { 0, 11.25, 22.5, 33.75, 45, 90 };
		final int numPoints = 32; // angle2 points for angle1Pos = 0 ?

		final double[] expectedStepSizes = { 11.25, 11.61290323, 12.4137931, 13.84615385, 16.36363636, 720.0 };
		final double[] calculatedStepSizes = Arrays.stream(angle1Positions)
				.map(x -> calculateStepSize(x, 360, numPoints)).toArray();
		System.err.println("expected:   " + Arrays.toString(expectedStepSizes));
		System.err.println("calculated: " + Arrays.toString(calculatedStepSizes));

		assertThat(calculatedStepSizes, isArrayCloseTo(expectedStepSizes));
	}

	private double calculateStepSize(double angle1Pos, double angle2Range, int numPoints) {
		// the formula to get the angle2 (phi) step size for a particular angle1 (omega) position and step size.
		final double result = angle2Range / Math.floor(numPoints * Math.cos(angle1Pos * (Math.PI / 180.0)));
		return Double.isInfinite(result) ? angle2Range * 2 : Math.abs(result); // if an infinite step size is produced, use 2 * range. This will produce 1 point
	}

	private void testTomoPathCalculation(IAxialModel angle1Model, IAxialModel angle2Model,
			StepSizes expectedAngle2StepSizes) throws Exception {
		final TensorTomoPathInfo info = calculateTomoPathInfo(angle1Model, angle2Model);
		assertThat(info.getAngle1Positions(), isArrayCloseTo(getPositions(angle1Model)));
		final double[][] expectedAngle2Positions = getExpectedAngle2Positions(angle2Model,
				expectedAngle2StepSizes, info.getAngle1Positions().length);
		final int expectedNumOuterPoints = Arrays.stream(expectedAngle2Positions)
				.mapToInt(arr -> arr.length).sum();

		assertThat(info.getAngle2StepSizes(), isStepSizesCloseTo(expectedAngle2StepSizes));
		assertThat(info.getAngle2Positions(), is2DArrayCloseTo(expectedAngle2Positions));

		checkMapInfo(info, expectedNumOuterPoints);
	}

	private double[] getPositions(IAxialModel axisPathModel) {
		final String axisName = axisPathModel.getAxisName();
		try {
			return ServiceProvider.getService(IPointGeneratorService.class)
					.createGenerator(axisPathModel).createPoints().stream()
					.mapToDouble(p -> (Double) p.get(axisName))
					.toArray();
		} catch (GeneratorException e) {
			throw new RuntimeException(e);
		}
	}

	private TensorTomoPathInfo calculateTomoPathInfo(IAxialModel angle1Model, IAxialModel angle2Model) throws PathInfoCalculationException {
		final TensorTomoPathRequest request = TensorTomoPathRequest.builder()
				.withMapPathModel(new TwoAxisGridPointsModel("xPos", "yPos", 5, 5))
				.withMapRegion(new RectangularMappingRegion(0.0, 10.0, 0.0, 5.00).toROI())
				.withAngle1PathModel(angle1Model)
				.withAngle2PathModel(angle2Model)
				.build();
		return tomoAnglePathCalculator.calculatePathInfo(request);
	}

	private void checkMapInfo(TensorTomoPathInfo info, int numOuterSteps) {
		assertThat(info.getInnerPointCount(), is(INNER_POINT_COUNT));
		final int totalPointCount = info.getTotalPointCount();
		final int outerPointCount = totalPointCount / INNER_POINT_COUNT;
		assertThat(outerPointCount, is(numOuterSteps));
		assertThat(info.getTotalPointCount(), is(numOuterSteps * INNER_POINT_COUNT));
		assertThat(info.getSmallestXStep(), is(closeTo(2.5, 1e-15)));
		assertThat(info.getSmallestYStep(), is(closeTo(1.25, 1e-15)));
		assertThat(info.getXCoordinates(), is(equalTo(expectedXPositions)));
		assertThat(info.getYCoordinates(), is(equalTo(expectedYPositions)));
		assertThat(info.getSmallestAbsStep(), is(closeTo(0.0, 1e-15)));
	}

	private double[][] getExpectedAngle2Positions(IAxialModel angle2Model,
			StepSizes expectedAngle2StepSizes, int numAngle1Positions) {
		if (angle2Model instanceof AxialPointsModel) {
			final double start = ((AxialPointsModel) angle2Model).getStart();
			final double stop = ((AxialPointsModel) angle2Model).getStop();
			return getExpectedAngle2Positions(start, stop, expectedAngle2StepSizes.getOneDStepSizes());
		} else if (angle2Model instanceof AxialStepModel) {
			final double start = ((AxialStepModel) angle2Model).getStart();
			final double stop = ((AxialStepModel) angle2Model).getStop();
			return getExpectedAngle2Positions(start, stop, expectedAngle2StepSizes.getOneDStepSizes());
		} else if (angle2Model instanceof AxialArrayModel) {
			// return a clone of the array positions for each point
			final double[] angle2Positions = ((AxialArrayModel) angle2Model).getPositions();
			return IntStream.range(0, numAngle1Positions).mapToObj(i -> angle2Positions.clone()).toArray(double[][]::new);
		} else if (angle2Model instanceof AxialMultiStepModel) {
			return getExpectedAngle2Positions((AxialMultiStepModel) angle2Model, expectedAngle2StepSizes.getTwoDStepSizes());
		}

		throw new IllegalArgumentException("Unknown model type " + angle2Model.getClass());
	}

	private double[][] getExpectedAngle2Positions(double angle2Start, double angle2Stop, double[] angle2StepSizes) {
		return Arrays.stream(angle2StepSizes)
				.mapToObj(stepSize -> new AxialStepModel(ANGLE_2_NAME, angle2Start, angle2Stop, stepSize))
				.map(stepModel -> getPositions(stepModel))
				.toArray(double[][]::new);
	}

	private double[][] getExpectedAngle2Positions(AxialMultiStepModel angle2Model, double[][] angle2StepSizes) {
		return Arrays.stream(angle2StepSizes)
				.map(stepSizesForAngle1Pos -> createMultiStepModelForStepSizes(angle2Model, stepSizesForAngle1Pos))
				.map(this::getPositions)
				.toArray(double[][]::new);
	}

	private AxialMultiStepModel createMultiStepModelForStepSizes(AxialMultiStepModel initialMultiStepModel,
			double[] angle2StepSizesForAngle1Pos) {
		final List<AxialStepModel> stepModels = initialMultiStepModel.getModels();
		final List<AxialStepModel> newStepModels = IntStream.range(0, stepModels.size())
					.mapToObj(modelIndex -> createAxialStepModel(stepModels.get(modelIndex), angle2StepSizesForAngle1Pos[modelIndex]))
					.collect(toList());
		final AxialMultiStepModel newMultiStepModel = new AxialMultiStepModel(initialMultiStepModel.getAxisName(), newStepModels);
		newMultiStepModel.setContinuous(false);
		return newMultiStepModel;
	}

	private AxialStepModel createAxialStepModel(AxialStepModel oldModel, double newStepSize) {
		return new AxialStepModel(oldModel.getAxisName(), oldModel.getStart(), oldModel.getStop(), newStepSize);
	}

	private static Matcher<StepSizes> isStepSizesCloseTo(StepSizes expected) {
		return new CustomTypeSafeMatcher<StepSizes>("step sizes " + expected.toString()) {

			@Override
			protected boolean matchesSafely(StepSizes actual) {
				if (actual.getRank() != expected.getRank()) return false;
				return switch (actual.getRank()) {
					case 0 -> true;
					case 1 -> isArrayCloseTo(expected.getOneDStepSizes()).matches(actual.getOneDStepSizes());
					case 2 -> is2DArrayCloseTo(expected.getTwoDStepSizes()).matches(actual.getTwoDStepSizes());
					default -> throw new IllegalArgumentException("Unexpected rank " + actual.getRank());
				};
			}

			@Override
			protected void describeMismatchSafely(StepSizes actual, Description mismatchDescription) {
				mismatchDescription.appendText("step sizes with rank " + actual.getRank() + " and values: ");
				switch (actual.getRank()) {
					case 1 -> isArrayCloseTo(expected.getOneDStepSizes()).describeMismatch(actual.getOneDStepSizes(), mismatchDescription);
					case 2 -> is2DArrayCloseTo(expected.getTwoDStepSizes()).describeMismatch(actual.getTwoDStepSizes(), mismatchDescription);
					default -> throw new IllegalArgumentException("Unexpected rank " + actual.getRank());
				}
			}

		};
	}

	private static Matcher<double[][]> is2DArrayCloseTo(double[][] expected) {
		@SuppressWarnings("unchecked")
		final Matcher<double[]>[] matchers = Arrays.stream(expected).map(arr -> isArrayCloseTo(arr)).toArray(Matcher[]::new);

		return arrayContaining(matchers);
	}

	private static Matcher<double[]> isArrayCloseTo(double[] expected) {
		@SuppressWarnings("unchecked")
		final Matcher<Double>[] matchers = Arrays.stream(expected).mapToObj(d -> new IsCloseTo(d, TOLERANCE)).toArray(Matcher[]::new);

		return new CustomTypeSafeMatcher<double[]>("array that is close to " + Arrays.toString(expected)) {

			@Override
			protected boolean matchesSafely(double[] actual) {
				return new IsArray<Double>(matchers).matchesSafely(Arrays.stream(actual).boxed().toArray(Double[]::new));
	        }

			@Override
			protected void describeMismatchSafely(double[] actual, Description mismatchDescription) {
				new IsArray<Double>(matchers).describeMismatchSafely(Arrays.stream(actual).boxed().toArray(Double[]::new), mismatchDescription);
			}

		};
	}

}
