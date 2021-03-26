/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.calibration;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.math3.linear.RealVector;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;

import uk.ac.gda.client.live.stream.calibration.CalibratedAxesProvider;
import uk.ac.gda.client.live.stream.calibration.PixelCalibration;
import uk.ac.gda.client.properties.camera.CameraToBeamMap;
/**
 * Create axes datasets for a data stream.
 *
 * <p>
 *  This class represents a substitute for the {@link PixelCalibration} because if no {@code Supplier<CameraToBeamMap>} in the constructor,
 *  its logic is equivalent to {@code PixelCalibration}
 * </p>
 *
 *
 * @author Maurizio Nagni
 */
public class MappedCameraAxesProvider implements CalibratedAxesProvider {

	private final Supplier<CameraToBeamMap> cameraToBeamMap;
	private final Supplier<ILazyDataset> dataset;
	private IDataset xAxisDataset;
	private IDataset yAxisDataset;

	/**
	 * Allows to create/recreate axes
	 */
	/**
	 * @param dataset
	 * @param cameraToBeamMap
	 */
	public MappedCameraAxesProvider(Supplier<ILazyDataset> dataset, Supplier<CameraToBeamMap> cameraToBeamMap) {
		this.dataset = dataset;
		this.cameraToBeamMap = cameraToBeamMap;
	}

	/**
	 * Associates this provider with a {@code Supplier<ILazyDataset>}.
	 * <p>
	 * If a dataset is provided the axes are calculated
	 * on the first call of {@link #getXAxisDataset()} or {@link #getYAxisDataset()}
	 * </p>
	 * @param dataset
	 */
	public MappedCameraAxesProvider(Supplier<ILazyDataset> dataset) {
		this(dataset, null);
	}

	@Override
	public void connect() {
		// no-op
	}

	@Override
	public void disconnect() {
		// no-op
	}


	@Override
	public IDataset getXAxisDataset() {
		if (xAxisDataset == null) {
			getDataset().ifPresent(d -> {
				updateAxis(this::setxAxisDataset, 0, d.getShape()[1], d.getShape()[1]);
				if (getCameraToBeamMap().isPresent()) {
					getXAxisDataset().setName("X (Transformed)");
				} else {
					getXAxisDataset().setName("X (Pixels)");
				}
			});
		}
		return xAxisDataset;
	}

	@Override
	public IDataset getYAxisDataset() {
		if (yAxisDataset == null) {
			getDataset().ifPresent(d -> {
				updateAxis(this::setyAxisDataset, 0, d.getShape()[0], d.getShape()[0]);
				if (getCameraToBeamMap().isPresent()) {
					getYAxisDataset().setName("Y (Transformed)");
				} else {
					getYAxisDataset().setName("Y (Pixels)");
				}
			});
		}
		return yAxisDataset;
	}

	@Override
	public void resizeStream(int[] newShape) {
		RealVector pixelOrigin = MappedCalibrationUtils.getRealVector(0, 0);
		RealVector pixelXRange = MappedCalibrationUtils.getRealVector(newShape[1], 0);
		RealVector pixelYRange = MappedCalibrationUtils.getRealVector(0, newShape[0]);

		RealVector axesOrigin = transformPixelsToBeamDrivers(pixelOrigin);
		RealVector axesXRange = transformPixelsToBeamDrivers(pixelXRange);
		RealVector axesYRange = transformPixelsToBeamDrivers(pixelYRange);

		updateAxis(this::setxAxisDataset, axesOrigin.getEntry(0), axesXRange.getEntry(0), newShape[1]);
		updateAxis(this::setyAxisDataset, axesOrigin.getEntry(1), axesYRange.getEntry(1), newShape[0]);
	}



	private void updateAxis(Consumer<IDataset> axisSetter, double start, double stop, int length) {
		Optional.ofNullable(DatasetFactory.createLinearSpace(DoubleDataset.class, start, stop, length))
			.ifPresent(axisSetter);
	}

	private Optional<ILazyDataset> getDataset() {
		return Optional.ofNullable(dataset)
				.map(Supplier::get);
	}

	private void setxAxisDataset(IDataset xAxisDataset) {
		this.xAxisDataset = xAxisDataset;
	}

	private void setyAxisDataset(IDataset yAxisDataset) {
		this.yAxisDataset = yAxisDataset;
	}

	/**
	 * Transform a vector from the pixel space to the one defined by {@link #cameraToBeamMap},
	 * if present otherwise return the same vector.
	 *
	 * @param pixel a dataset point
	 * @return the trasformed vector
	 */
	private RealVector transformPixelsToBeamDrivers(RealVector pixel) {
		return getCameraToBeamMap()
				.map(c -> MappedCalibrationUtils.pixelToBeam(c, pixel))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.orElse(pixel);
	}

	private Optional<CameraToBeamMap> getCameraToBeamMap() {
		return Optional.ofNullable(cameraToBeamMap)
				.map(Supplier::get);
	}
}
