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

package uk.ac.gda.server.ncd.calibration;

import static org.eclipse.january.dataset.DatasetFactory.createFromList;
import static uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter.fitPeaks;
import static uk.ac.gda.server.ncd.calibration.CentroidScanParameters.symmetric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.IMetadataEntry;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.scan.ConcurrentScan;
import gda.scan.DataPointCache;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;

/**
 * Wrapper around two scannables to allow one to be optimised with respect to the other.
 * To optimise, a scan is run over the configurable parameters and the readback is fitted
 * to a gaussian. The driven scannable is then moved to the position of the peak.
 */
public class GaussianOptimisingScan {
	private static final Logger logger = LoggerFactory.getLogger(GaussianOptimisingScan.class);
	private final Scannable positioner;
	private final Scannable readback;

	private final double range;
	private final double centre;
	private final double step;

	private IMetadataEntry scanTitle;

	public GaussianOptimisingScan(Scannable positioner, Scannable readback, double centre, double range, double step) {
		this.positioner = positioner;
		this.readback = readback;
		this.centre = centre;
		this.range = range;
		this.step = step;
	}

	public void optimise() throws Exception {
		logger.debug("Beginning optimisation of {} against {}", readback.getName(), positioner.getName());
		if (scanTitle != null) {
			scanTitle.setValue("Optimisation scan for " + readback.getName() + " against " + positioner.getName());
		}
		var scan = new ConcurrentScan(new Object[] {positioner, symmetric(centre, range, step), readback});
		scan.runScan();
		var cache = Finder.findSingleton(DataPointCache.class);
		var readings = cache.getPositionsFor(readback.getName());
		var positions = cache.getPositionsFor(positioner.getName());

		var result = fitPeaks(createFromList(positions), createFromList(readings), Gaussian.class, 1);
		if (result.isEmpty()) {
			throw new IllegalStateException("No peaks found for optisation scan");
		}
		var peak = result.get(0);
		logger.info("Optimise scan found peak: {}", peak);
		positioner.moveTo(peak.getPosition());
	}

	public IMetadataEntry getScanTitle() {
		return scanTitle;
	}

	public void setScanTitle(IMetadataEntry scanTitle) {
		this.scanTitle = scanTitle;
	}
}
