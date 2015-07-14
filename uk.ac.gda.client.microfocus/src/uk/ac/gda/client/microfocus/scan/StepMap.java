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

import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.XasAsciiNexusDataWriter;
import gda.data.scan.datawriter.XasAsciiNexusDatapointCompletingDataWriter;
import gda.device.CounterTimer;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.jython.scriptcontroller.ScriptControllerBase;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.client.microfocus.scan.datawriter.MicroFocusWriterExtender;
import uk.ac.gda.server.exafs.scan.XasScanBase;

public class StepMap extends XasScanBase implements MappingScan {

	protected Scannable xScan;
	protected Scannable yScan;
	protected Scannable zScan;
	protected Scannable energyNoGap;
	protected Scannable energyWithGap;
	protected Scannable energyInUse;

	private CounterTimer counterTimer;

	protected ScriptControllerBase elementListScriptController;
	protected MicroFocusWriterExtender mfd;
	protected MicroFocusScanParameters mapScanParameters;

	@Override
	public String getScanType() {
		return "Step Map";
	}

	@Override
	public MicroFocusWriterExtender getMFD() {
		return mfd;
	}

	@Override
	protected Object[] createScanArguments(String sampleName, List<String> descriptions) throws Exception {

		mapScanParameters = (MicroFocusScanParameters) scanBean;

		Detector[] detectorList = getDetectors();

		createMFD(detectorList);

		moveEnergyAndZBeforeMap();

		Object[] args = buildListOfArguments(detectorList);

		return args;
	}

	protected Object[] buildListOfArguments(Detector[] detectorList) throws DeviceException, Exception {
		Object[] args = new Object[] { yScan, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
				mapScanParameters.getYStepSize(), xScan, mapScanParameters.getXStart(), mapScanParameters.getXEnd(),
				mapScanParameters.getXStepSize(), zScan };
		boolean useFrames = LocalProperties.check("gda.microfocus.scans.useFrames");
		log("Using frames: " + useFrames);
		if (detectorBean.getExperimentType().equals("Fluorescence") && useFrames) {
			args = ArrayUtils.addAll(args, detectorList);
			counterTimer.clearFrameSets();
			log("Frame collection time: " + mapScanParameters.getCollectionTime());
			int nx = calculateNumberXPoints();
			counterTimer.addFrameSet(nx, 1.0E-4, mapScanParameters.getCollectionTime() * 1000.0, 0, 7, -1, 0);
		} else {
			for (Detector detector : detectorList) {
				args = ArrayUtils.add(args, detector);
				args = ArrayUtils.add(args, mapScanParameters.getCollectionTime());
			}
		}
		return args;
	}

	protected int calculateNumberXPoints() throws Exception {
		return ScannableUtils.getNumberSteps(xScan, mapScanParameters.getXStart(), mapScanParameters.getXEnd(),
				mapScanParameters.getXStepSize()) + 1;
	}

	protected void moveEnergyAndZBeforeMap() throws DeviceException {
		double energy = mapScanParameters.getEnergy();
		double zScannablePos = mapScanParameters.getZValue();

		log("Energy: " + energy);
		energyInUse.moveTo(energy);
		mfd.setEnergyValue(energy);

		log("Moving " + zScan.getName() + " to " + zScannablePos);
		zScan.moveTo(zScannablePos);
		mfd.setZValue(zScannablePos);
	}

	protected void createMFD(Detector[] detectorList) throws Exception {
		int nx = calculateNumberXPoints();
		int ny = ScannableUtils.getNumberSteps(yScan, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
				mapScanParameters.getYStepSize()) + 1;
		log("Number x points: " + nx);
		log("Number y points: " + ny);
		mfd = new MicroFocusWriterExtender(nx, ny, mapScanParameters.getXStepSize(), mapScanParameters.getYStepSize(),
				detectorConfigurationBean, detectorList);
		// this updates the Elements view in the Mircofocus UI with the list of elements
		if (elementListScriptController != null) {
			String fluoConfigFileName = this.experimentFullPath
					+ detectorBean.getFluorescenceParameters().getConfigFileName();
			elementListScriptController.update(this, fluoConfigFileName);
		}
	}

	@Override
	protected DataWriter createAndConfigureDataWriter(String sampleName, List<String> descriptions) throws Exception {
		XasAsciiNexusDatapointCompletingDataWriter twoDWriter = new XasAsciiNexusDatapointCompletingDataWriter();
		DataWriter underlyingDataWriter = twoDWriter.getDatawriter();
		underlyingDataWriter.addDataWriterExtender(mfd);
		if (underlyingDataWriter instanceof XasAsciiNexusDataWriter) {
			setupXasAsciiNexusDataWriter(sampleName, descriptions, (XasAsciiNexusDataWriter) underlyingDataWriter);
		}
		return twoDWriter;
	}

	@Override
	protected void finishRepetitions() throws Exception {
		super.finishRepetitions();
		if (mfd != null) {
			try {
				mfd.closeWriter();
			} catch (Throwable e) {
				throw new Exception(e.getMessage());
			}
		}
	}

	public void setElementListScriptController(ScriptControllerBase elementListScriptController) {
		this.elementListScriptController = elementListScriptController;
	}

	public CounterTimer getCounterTimer() {
		return counterTimer;
	}

	public void setxScan(Scannable xScan) {
		this.xScan = xScan;
	}

	public void setyScan(Scannable yScan) {
		this.yScan = yScan;
	}

	public void setzScan(Scannable zScan) {
		this.zScan = zScan;
	}

	public void setCounterTimer(CounterTimer counterTimer) {
		this.counterTimer = counterTimer;
	}

	public void setEnergyNoGap(Scannable energyNoGap) {
		this.energyNoGap = energyNoGap;
	}

	public void setEnergyWithGap(Scannable energyWithGap) {
		this.energyWithGap = energyWithGap;
		this.energyInUse = energyWithGap; // default
	}

	public void setUseNoGapEnergy() {
		this.energyInUse = this.energyNoGap;
	}

	public void setUseWithGapEnergy() {
		this.energyInUse = this.energyWithGap;
	}

}
