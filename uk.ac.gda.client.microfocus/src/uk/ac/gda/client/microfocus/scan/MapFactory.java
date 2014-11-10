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

import gda.device.CounterTimer;
import gda.device.Scannable;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.LineRepeatingBeamMonitor;
import gda.device.scannable.RealPositionReader;
import gda.jython.scriptcontroller.ScriptControllerBase;
import uk.ac.gda.server.exafs.scan.SpectroscopyScanFactory;

public class MapFactory extends SpectroscopyScanFactory {

	private CounterTimer counterTimer;
	private Scannable xScan;
	private Scannable yScan;
	private Scannable zScan;
	private ScriptControllerBase elementListScriptController;
	private RasterMapDetectorPreparer rasterMapDetectorPreparer;
	private ContinuouslyScannable trajectoryMotor;
	private RealPositionReader positionReader;
	private LineRepeatingBeamMonitor trajectoryBeamMonitor;

	public MapFactory() {
	}

	public StepMap createStepMap() {
		checkSharedObjectsNonNull();
		checkDefined(xScan, "xScan");
		checkDefined(yScan, "yScan");
		checkDefined(zScan, "zScan");
		checkDefined(elementListScriptController, "elementListScriptController");

		StepMap newMap = new StepMap(beamlinePreparer, detectorPreparer, samplePreparer, outputPreparer,
				commandQueueProcessor, XASLoggingScriptController, datawriterconfig, original_header, energyScannable,
				metashop, includeSampleNameInNexusName, counterTimer, xScan, yScan, zScan, elementListScriptController);

		return newMap;
	}

	public RasterMap createRasterMap() {

		checkObjectsDefined();

		RasterMap newMap = new RasterMap(beamlinePreparer, rasterMapDetectorPreparer, samplePreparer, outputPreparer,
				commandQueueProcessor, XASLoggingScriptController, datawriterconfig, original_header, energyScannable,
				metashop, includeSampleNameInNexusName, trajectoryMotor, positionReader, yScan, zScan,
				trajectoryBeamMonitor, elementListScriptController);

		return newMap;
	}

	protected void checkObjectsDefined() {
		checkSharedObjectsNonNull();
		checkDefined(xScan, "xScan");
		checkDefined(yScan, "yScan");
		checkDefined(zScan, "zScan");
		checkDefined(trajectoryMotor, "trajectoryMotor");
		checkDefined(positionReader, "positionReader");
		checkDefined(trajectoryBeamMonitor, "trajectoryBeamMonitor");
	}

	public FasterRasterMap createFasterRasterMap() {

		checkObjectsDefined();

		FasterRasterMap newMap = new FasterRasterMap(beamlinePreparer, rasterMapDetectorPreparer, samplePreparer,
				outputPreparer, commandQueueProcessor, XASLoggingScriptController, datawriterconfig, original_header,
				energyScannable, metashop, includeSampleNameInNexusName, trajectoryMotor, positionReader, yScan, zScan,
				trajectoryBeamMonitor, elementListScriptController);

		return newMap;
	}

	public Scannable getxScan() {
		return xScan;
	}

	public void setxScan(Scannable xScan) {
		this.xScan = xScan;
	}

	public Scannable getyScan() {
		return yScan;
	}

	public void setyScan(Scannable yScan) {
		this.yScan = yScan;
	}

	public Scannable getzScan() {
		return zScan;
	}

	public void setzScan(Scannable zScan) {
		this.zScan = zScan;
	}

	public ScriptControllerBase getElementListScriptController() {
		return elementListScriptController;
	}

	public void setElementListScriptController(ScriptControllerBase elementListScriptController) {
		this.elementListScriptController = elementListScriptController;
	}

	public CounterTimer getCounterTimer() {
		return counterTimer;
	}

	public void setCounterTimer(CounterTimer counterTimer) {
		this.counterTimer = counterTimer;
	}

	public RasterMapDetectorPreparer getRasterMapDetectorPreparer() {
		return rasterMapDetectorPreparer;
	}

	public void setRasterMapDetectorPreparer(RasterMapDetectorPreparer rasterMapDetectorPreparer) {
		this.rasterMapDetectorPreparer = rasterMapDetectorPreparer;
	}

	public ContinuouslyScannable getTrajectoryMotor() {
		return trajectoryMotor;
	}

	public void setTrajectoryMotor(ContinuouslyScannable trajectoryMotor) {
		this.trajectoryMotor = trajectoryMotor;
	}

	public RealPositionReader getPositionReader() {
		return positionReader;
	}

	public void setPositionReader(RealPositionReader positionReader) {
		this.positionReader = positionReader;
	}

	public LineRepeatingBeamMonitor getTrajectoryBeamMonitor() {
		return trajectoryBeamMonitor;
	}

	public void setTrajectoryBeamMonitor(LineRepeatingBeamMonitor trajectoryBeamMonitor) {
		this.trajectoryBeamMonitor = trajectoryBeamMonitor;
	}

}
