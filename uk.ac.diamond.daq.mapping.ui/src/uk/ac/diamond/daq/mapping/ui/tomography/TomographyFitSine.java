/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.tomography;

import java.util.Arrays;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.TomographyCalibrationData;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Sine;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer;

public class TomographyFitSine {
	private static final Logger logger = LoggerFactory.getLogger(TomographyFitSine.class);

	private static final double GUESS_PHASE = 0;
	private static final double GUESS_FREQ = 0.02;
	private static final double GUESS_AMP = 1;

	private TomographyFitSine() {
		 throw new IllegalStateException("Utility class");
	}
	public static TomographyCalibrationData fitSine(double[] xData, double[] yData) {
		final double guessMean = Arrays.stream(yData).average().orElse(1.0);
		final ApacheOptimizer leastsq = new ApacheOptimizer(ApacheOptimizer.Optimizer.LEVENBERG_MARQUARDT);
		final Sine sineFunction = new Sine(new double[] { GUESS_AMP, GUESS_FREQ, GUESS_PHASE, guessMean });
		final IDataset[] coordinates = { DatasetFactory.createFromList(Arrays.asList(xData)) };
		final IDataset data = DatasetFactory.createFromList(Arrays.asList(yData));
		try {
			leastsq.optimize(coordinates, data, sineFunction);
		} catch (Exception e) {
			logger.error("Error doing fit", e);
		}
		final double[] params = leastsq.getParameterValues();
		return new TomographyCalibrationData(params[0], params[1], params[2], params[3]);
	}

}
