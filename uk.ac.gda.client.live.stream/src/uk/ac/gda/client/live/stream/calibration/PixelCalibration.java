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

package uk.ac.gda.client.live.stream.calibration;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IntegerDataset;
/**
 * Create axes datasets for a data stream displaying camera pixel numbers on axes.
 *
 * @author Fajin Yuan
 * @author Maurizio Nagni
 */
public class PixelCalibration implements CalibratedAxesProvider {

	private final Supplier<ILazyDataset> dataset;
	private IDataset xAxisDataset;
	private IDataset yAxisDataset;

	/**
	 * Allows to create/recreate axes
	 */
	public PixelCalibration() {
		this(null);
	}

	/**
	 * Associates this provider with a {@code Supplier<ILazyDataset>}.
	 * <p>
	 * If a dataset is provided the axes are calculated
	 * on the first call of {@link #getXAxisDataset()} or {@link #getYAxisDataset()}
	 * </p>
	 * @param dataset
	 */
	public PixelCalibration(Supplier<ILazyDataset> dataset) {
		this.dataset = dataset;
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
				getXAxisDataset().setName("X (Pixels)");
			});
		}
		return xAxisDataset;
	}

	@Override
	public IDataset getYAxisDataset() {
		if (yAxisDataset == null) {
			getDataset().ifPresent(d -> {
				updateAxis(this::setyAxisDataset, 0, d.getShape()[0], d.getShape()[0]);
				getYAxisDataset().setName("Y (Pixels)");
			});
		}
		return yAxisDataset;
	}

	@Override
	public void resizeStream(int[] newShape) {
		updateAxis(this::setxAxisDataset, 0, newShape[1], newShape[1]);
		updateAxis(this::setyAxisDataset, 0, newShape[0], newShape[0]);
	}

	private void updateAxis(Consumer<IDataset> axisSetter, int start, int stop, int length) {
		Optional.ofNullable(DatasetFactory.createLinearSpace(IntegerDataset.class, start, stop, length))
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
}
