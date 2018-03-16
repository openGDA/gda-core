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
import gda.jython.scriptcontroller.ScriptControllerBase;
import uk.ac.gda.server.exafs.scan.SpectroscopyScanFactory;

public class MapFactory extends SpectroscopyScanFactory {

	private CounterTimer counterTimer;
	private Scannable xScan;
	private Scannable yScan;
	private Scannable zScan;
	private ScriptControllerBase elementListScriptController;
	private Scannable energyWithGapScannable;
	private Scannable energyNoGapScannable;

	public MapFactory() {
	}

	public StepMap createStepMap() {
		checkSharedObjectsNonNull();
		checkDefined(energyWithGapScannable, "energy");
		checkDefined(energyNoGapScannable, "energy_nogap");
		checkDefined(xScan, "xScan");
		checkDefined(yScan, "yScan");
		checkDefined(zScan, "zScan");
		checkDefined(elementListScriptController, "elementListScriptController");

		StepMap newMap = new StepMap();
		newMap.setBeamlinePreparer(beamlinePreparer);
		newMap.setDetectorPreparer(detectorPreparer);
		newMap.setOutputPreparer(outputPreparer);
		newMap.setSamplePreparer(samplePreparer);
		newMap.setLoggingScriptController(loggingScriptController);
		newMap.setDatawriterconfig(datawriterconfig);

		newMap.setEnergyWithGap(energyWithGapScannable);
		newMap.setEnergyNoGap(energyNoGapScannable);
		newMap.setMetashop(metashop);
		newMap.setIncludeSampleNameInNexusName(includeSampleNameInNexusName);
		newMap.setScanName(scanName);
		newMap.setCounterTimer(counterTimer);
		newMap.setxScan(xScan);
		newMap.setyScan(yScan);
		newMap.setzScan(zScan);
		newMap.setElementListScriptController(elementListScriptController);
		return newMap;
	}

	protected void checkObjectsDefined() {
		checkSharedObjectsNonNull();
		checkDefined(xScan, "xScan");
		checkDefined(yScan, "yScan");
		checkDefined(zScan, "zScan");
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

	// public LineRepeatingBeamMonitor getTrajectoryBeamMonitor() {
	// return trajectoryBeamMonitor;
	// }
	//
	// public void setTrajectoryBeamMonitor(LineRepeatingBeamMonitor trajectoryBeamMonitor) {
	// this.trajectoryBeamMonitor = trajectoryBeamMonitor;
	// }

	public Scannable getEnergyWithGapScannable() {
		return energyWithGapScannable;
	}

	public void setEnergyWithGapScannable(Scannable energyWithGapScannable) {
		this.energyWithGapScannable = energyWithGapScannable;
	}

	public Scannable getEnergyNoGapScannable() {
		return energyNoGapScannable;
	}

	public void setEnergyNoGapScannable(Scannable energyNoGapScannable) {
		this.energyNoGapScannable = energyNoGapScannable;
	}
}
