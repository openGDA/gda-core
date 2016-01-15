/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.scan;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import gda.device.detector.BufferedDetector;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.RealPositionReader;
import gda.scan.ContinuousScan;
import uk.ac.diamond.daq.microfocus.api.RasterMapDetectorPreparer;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;

/**
 * Performs raster maps by running a ContinuousScan as the innermost dimension in a 2D ConcurrentScan.
 * <p>
 * Requires a ContinuouslyScannable to be operated as the x axis and a RealPositionReader to return the actual motor
 * positions.
 */
public class RasterMap extends StepMap implements MappingScan {

	protected ContinuouslyScannable trajectoryMotor;
	private RasterMapDetectorPreparer bufferedDetectorPreparer;
	protected RealPositionReader positionReader;
	private boolean includeRealPositionReader = true;

	@Override
	public String getScanType() {
		return "Raster Map";
	}

	@Override
	protected Object[] createScanArguments(String sampleName, List<String> descriptions) throws Exception {
		mapScanParameters = (MicroFocusScanParameters) scanBean;
		bufferedDetectorPreparer = (RasterMapDetectorPreparer) detectorPreparer;
		BufferedDetector[] detectorList = bufferedDetectorPreparer.getRasterMapDetectors();

		createMFD(detectorList);

		moveEnergyAndZBeforeMap();

		Object[] args = buildListOfArguments(detectorList);

		return args;
	}

	protected Object[] buildListOfArguments(BufferedDetector[] detectorList) {
		ContinuousScan cs = new ContinuousScan(trajectoryMotor, mapScanParameters.getXStart(),
				mapScanParameters.getXEnd(), calculateNumberXPoints(), mapScanParameters.getRowTime(), detectorList);

		// TODO have not done the custom settings for raster maps for the monitor objects

		Object[] args = new Object[] { yScan, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
				mapScanParameters.getYStepSize(), cs };

		// add a Scannable, if defined, which fetches the motor readback values from the Epics Trajectory template after
		// the trajectory completes.
		if (positionReader != null && includeRealPositionReader) {
			args = ArrayUtils.add(args, positionReader);
		}

		return args;
	}

	@Override
	protected int calculateNumberXPoints() {
		return (int) (Math.abs(mapScanParameters.getXEnd() - mapScanParameters.getXStart())
				/ mapScanParameters.getXStepSize() + 1);
	}

	public ContinuouslyScannable getTrajectoryMotor() {
		return trajectoryMotor;
	}

	public void setTrajectoryMotor(ContinuouslyScannable trajectoryMotor) {
		this.trajectoryMotor = trajectoryMotor;
	}

	public void setPositionReader(RealPositionReader positionReader) {
		this.positionReader = positionReader;
	}

	public void setIncludeRealPositionReader(boolean include) {
		includeRealPositionReader = include;
	}
}
