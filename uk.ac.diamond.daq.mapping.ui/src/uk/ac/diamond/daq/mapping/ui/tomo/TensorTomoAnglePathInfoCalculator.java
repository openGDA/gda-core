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

package uk.ac.diamond.daq.mapping.ui.tomo;

import java.util.Arrays;
import java.util.UUID;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.IAxialModel;

import uk.ac.diamond.daq.mapping.api.IPathInfoCalculator;
import uk.ac.diamond.daq.mapping.api.PathInfoCalculationException;
import uk.ac.diamond.daq.mapping.api.document.scanpath.MappingPathInfo;
import uk.ac.diamond.daq.mapping.api.document.scanpath.MappingPathInfoRequest;
import uk.ac.diamond.daq.mapping.ui.path.MappingPathInfoCalculator;
import uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoPathInfo.StepSizes;

/**
 * Implementation of {@link IPathInfoCalculator} that uses {@link IPointGenerator} to
 * generate the primary angle positions, and the step sizes for the secondary angle
 * positions for tensor tomography scans.
 *
 * @param <M> the type of the secondary angle path model
 */
public class TensorTomoAnglePathInfoCalculator<M extends IAxialModel>
		implements IPathInfoCalculator<TensorTomoPathRequest, TensorTomoPathInfo> {

	/**
	 * Delegate most work to a wrapped mapping path calculator
	 */
	private final MappingPathInfoCalculator mappingPathCalculator;

	private final IPointGeneratorService pointGenService;

	public TensorTomoAnglePathInfoCalculator(IPointGeneratorService pointGenService) {
		mappingPathCalculator = new MappingPathInfoCalculator(pointGenService);
		this.pointGenService = pointGenService;
	}

	@Override
	public TensorTomoPathInfo calculatePathInfo(TensorTomoPathRequest request) throws PathInfoCalculationException {
		try {
			// first use the wrapped mapping path calculator
			final MappingPathInfoRequest mapRequest = toMappingRequest(request);
			final MappingPathInfo mappingPathInfo = mappingPathCalculator.calculatePathInfo(mapRequest);

			final double[] angle1Positions = getPositions(request.getAngle1PathModel());
			@SuppressWarnings("unchecked")
			final M angle2Model = (M) request.getAngle2PathModel(); // convert to points if necessary

			return calculatePathInfo(mappingPathInfo, angle1Positions, angle2Model);
		} catch (Exception e) {
			throw new PathInfoCalculationException("Cannot calculate path information", e);
		}
	}

	private TensorTomoPathInfo calculatePathInfo(final MappingPathInfo mappingPathInfo,
			final double[] angle1Positions, final M angle2Model) {
		final TomoSecondaryAngleCalculator<M> angle2Calculator = TomoSecondaryAngleCalculator.forModel(angle2Model);
		final StepSizes angle2StepSizes = angle2Calculator.calculateAngle2StepSizes(angle1Positions, angle2Model);
		final double[][] angle2Positions = angle2Calculator.calculateAngle2Positions(angle1Positions, angle2Model, angle2StepSizes);
		final int outerPoints = Arrays.stream(angle2Positions).mapToInt(points -> points.length).sum();

		return createInfo(mappingPathInfo, angle1Positions, angle2Positions, angle2StepSizes, outerPoints);
	}

	private double[] getPositions(final IAxialModel pathModel) {
		try {
			return getPositions(pointGenService.createGenerator(pathModel));
		} catch (GeneratorException e) {
			throw new RuntimeException(e); // NOSONAR wrap in runtime exception so this method can be used with streams
		}
	}

	private TensorTomoPathInfo createInfo(final MappingPathInfo mappingPathInfo, final double[] angle1Positions,
			final double[][] angle2Positions, final StepSizes angle2StepSizes, final int outerPoints) {
		return TensorTomoPathInfo.builder()
				.withSourceId(TensorTomoScanSetupView.ID)
				.withInnerPointCount(mappingPathInfo.getInnerPointCount())
				.withOuterPointCount(outerPoints)
				.withSmallestXStep(mappingPathInfo.getSmallestXStep())
				.withSmallestYStep(mappingPathInfo.getSmallestYStep())
				.withXCoordinates(mappingPathInfo.getXCoordinates())
				.withYCoordinates(mappingPathInfo.getYCoordinates())
				.withAngle1Positions(angle1Positions)
				.withAngle2Positions(angle2Positions)
				.withAngle2StepSizes(angle2StepSizes)
				.build();
	}

	private MappingPathInfoRequest toMappingRequest(TensorTomoPathRequest request) {
		return MappingPathInfoRequest.builder()
				.withEventId(UUID.randomUUID())
				.withSourceId(TensorTomoScanSetupView.ID)
				.withScanPathModel(request.getMapPathModel())
				.withScanRegion(request.getMapRegion())
				.build();
	}

	private double[] getPositions(IPointGenerator<? extends IAxialModel> pointGen) {
		final String axisName = pointGen.getModel().getAxisName();
		return pointGen.createPoints().stream()
				.map(p -> p.get(axisName))
				.map(Double.class::cast)
				.mapToDouble(Double::doubleValue)
				.toArray();
	}

}
