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

import gda.device.Scannable;
import gda.device.detector.countertimer.BufferedScaler;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.RealPositionReader;
import gda.jython.InterfaceProvider;

import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PySequence;

import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.client.microfocus.scan.datawriter.MicroFocusWriterExtender;
import uk.ac.gda.server.exafs.scan.preparers.I18BeamlinePreparer;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * The single point of access in the jython environment for map scans. This redirects calls based on its configuration
 * and attributes.
 * <p>
 * Currently only used by I18. This could be applied to other mapping beamlines with a bit of refactoring. It would be
 * better to refactor this than for other beamlines to use another way to keep consistency.
 */
public class MapSelector {

	private final StepMap non_raster;
	private final RasterMap raster;
	private final FasterRasterMap faster_raster;

	private RasterMap raster_mode;
	private boolean currentMapIsRaster = false;
	private ContinuouslyScannable stage1TrajMotor;
	private ContinuouslyScannable stage3TrajMotor;
	private RealPositionReader stage1PositionReader;
	private RealPositionReader stage3PositionReader;
	private I18BeamlinePreparer beamlinePreparer;
	private Scannable stage1X;
	private Scannable stage1Y;
	private Scannable stage1Z;
	private Scannable stage3X;
	private Scannable stage3Y;
	private Scannable stage3Z;
	private BufferedScaler ionChambers;

	public MapSelector(I18BeamlinePreparer beamlinePreparer, StepMap non_raster, RasterMap raster, FasterRasterMap faster_raster,
			ContinuouslyScannable stage1TrajMotor, ContinuouslyScannable stage3TrajMotor,
			RealPositionReader stage1PositionReader, RealPositionReader stage3PositionReader, BufferedScaler ionChambers) {
		this.beamlinePreparer = beamlinePreparer;
		this.non_raster = non_raster;
		this.raster = raster;
		this.faster_raster = faster_raster;
		raster_mode = raster;
		this.stage1TrajMotor = stage1TrajMotor;
		this.stage3TrajMotor = stage3TrajMotor;
		this.stage1PositionReader = stage1PositionReader;
		this.stage3PositionReader = stage3PositionReader;
		this.ionChambers = ionChambers;
	}

	public PyObject __call__(PyObject pyArgs) throws Exception {
		String sampleFileName = ((PySequence) pyArgs).__finditem__(0).asString();
		String scanFileName = ((PySequence) pyArgs).__finditem__(1).asString();
		String detectorFileName = ((PySequence) pyArgs).__finditem__(2).asString();
		String outputFileName = ((PySequence) pyArgs).__finditem__(3).asString();
		String folderName = ((PySequence) pyArgs).__finditem__(4).asString() + "/";
		int numRepetitions = ((PySequence) pyArgs).__finditem__(5).asInt();

		// it has to be a MicroFocusScanParameters object or its not a map
		MicroFocusScanParameters scanBean = (MicroFocusScanParameters) XMLHelpers.getBeanObject(folderName,
				scanFileName);
		currentMapIsRaster = scanBean.isRaster();
		if (currentMapIsRaster) {
			raster_mode.doCollection(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName,
					numRepetitions);
		} else {
			non_raster.doCollection(sampleFileName, scanFileName, detectorFileName, outputFileName, folderName,
					numRepetitions);
		}
		return new PyInteger(0);
	}

	public MicroFocusWriterExtender getMFD() {
		if (currentMapIsRaster) {
			return raster_mode.getMFD();
		}
		return non_raster.getMFD();
	}

	public void enableFasterRaster() {
		raster_mode = faster_raster;
	}

	public void disableFasterRaster() {
		raster_mode = raster;
	}

	public void setStage(int stageNumber) {

		switch (stageNumber) {
		case 1:
			non_raster.setxScan(stage1X);
			non_raster.setyScan(stage1Y);
			non_raster.setzScan(stage1Z);
			raster.setxScan(stage1X);
			raster.setyScan(stage1Y);
			raster.setzScan(stage1Z);
			faster_raster.setxScan(stage1X);
			faster_raster.setyScan(stage1Y);
			faster_raster.setzScan(stage1Z);

			raster.setTrajectoryMotor(stage1TrajMotor);
			raster.setPositionReader(stage1PositionReader);
			faster_raster.setTrajectoryMotor(stage1TrajMotor);
			faster_raster.setPositionReader(stage1PositionReader);
			
//			ionChambers.setTtlSocket(1);
			
			break;
		case 3:
			non_raster.setxScan(stage3X);
			non_raster.setyScan(stage3Y);
			non_raster.setzScan(stage3Z);
			raster.setxScan(stage3X);
			raster.setyScan(stage3Y);
			raster.setzScan(stage3Z);
			faster_raster.setxScan(stage3X);
			faster_raster.setyScan(stage3Y);
			faster_raster.setzScan(stage3Z);
			
			raster.setTrajectoryMotor(stage3TrajMotor);
			raster.setPositionReader(stage3PositionReader);
			faster_raster.setTrajectoryMotor(stage3TrajMotor);
			faster_raster.setPositionReader(stage3PositionReader);
			
//			ionChambers.setTtlSocket(1);

			break;
		default:
			InterfaceProvider.getTerminalPrinter().print("only stages 1 or 3 may be selected");
		}
	}

	/**
	 * Normal running conditions
	 */
	public void enableUseIDGap() {
		non_raster.setUseWithGapEnergy();
		raster.setUseWithGapEnergy();
		faster_raster.setUseWithGapEnergy();
		beamlinePreparer.setUseWithGapEnergy();
	}

	/**
	 * For shutdown and machine-dev days when there is no control of the ID gap
	 */
	public void disableUseIDGap() {
		non_raster.setUseNoGapEnergy();
		raster.setUseNoGapEnergy();
		faster_raster.setUseNoGapEnergy();
		beamlinePreparer.setUseNoGapEnergy();
	}

	public void enableRealPositions() {
		raster.setIncludeRealPositionReader(true);
		faster_raster.setIncludeRealPositionReader(true);
	}

	public void disableRealPositions() {
		raster.setIncludeRealPositionReader(false);
		faster_raster.setIncludeRealPositionReader(false);
	}

	public void setStage1X(Scannable stage1x) {
		stage1X = stage1x;
	}

	public void setStage1Y(Scannable stage1y) {
		stage1Y = stage1y;
	}

	public void setStage1Z(Scannable stage1z) {
		stage1Z = stage1z;
	}

	public void setStage3X(Scannable stage3x) {
		stage3X = stage3x;
	}

	public void setStage3Y(Scannable stage3y) {
		stage3Y = stage3y;
	}

	public void setStage3Z(Scannable stage3z) {
		stage3Z = stage3z;
	}

}
