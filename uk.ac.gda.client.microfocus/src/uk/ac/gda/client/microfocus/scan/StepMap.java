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

import gda.commandqueue.Processor;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.XasAsciiNexusDataWriter;
import gda.data.scan.datawriter.XasAsciiNexusDatapointCompletingDataWriter;
import gda.device.CounterTimer;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.logging.LoggingScriptController;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.client.microfocus.scan.datawriter.MicroFocusWriterExtender;
import uk.ac.gda.server.exafs.scan.BeamlinePreparer;
import uk.ac.gda.server.exafs.scan.DetectorPreparer;
import uk.ac.gda.server.exafs.scan.OutputPreparer;
import uk.ac.gda.server.exafs.scan.SampleEnvironmentPreparer;
import uk.ac.gda.server.exafs.scan.XasScanBase;

public class StepMap extends XasScanBase implements MappingScan {

	protected final Scannable xMotor;
	protected final Scannable yMotor;
	protected final Scannable zMotor;

	private final CounterTimer counterTimer01;

	protected ScriptControllerBase elementListScriptController;
	protected MicroFocusWriterExtender mfd;
	protected MicroFocusScanParameters mapScanParameters;

	StepMap(BeamlinePreparer beamlinePreparer, DetectorPreparer detectorPreparer,
			SampleEnvironmentPreparer samplePreparer, OutputPreparer outputPreparer, Processor commandQueueProcessor,
			LoggingScriptController XASLoggingScriptController, AsciiDataWriterConfiguration datawriterconfig,
			ArrayList<AsciiMetadataConfig> original_header, Scannable energy_scannable, NXMetaDataProvider metashop,
			boolean includeSampleNameInNexusName, CounterTimer counterTimer01, Scannable xScan, Scannable yScan,
			Scannable zScannable, ScriptControllerBase elementListScriptController) {

		super(beamlinePreparer, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor,
				XASLoggingScriptController, datawriterconfig, original_header, energy_scannable,
				includeSampleNameInNexusName, metashop);

		this.counterTimer01 = counterTimer01;
		this.xMotor = xScan;
		this.yMotor = yScan;
		this.zMotor = zScannable;
		this.elementListScriptController = elementListScriptController;
	}

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
		Object[] args = new Object[] { yMotor, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
				mapScanParameters.getYStepSize(), xMotor, mapScanParameters.getXStart(), mapScanParameters.getXEnd(),
				mapScanParameters.getXStepSize(), zMotor };		
		boolean useFrames = LocalProperties.check("gda.microfocus.scans.useFrames");
		log("Using frames: " + useFrames);
		if (detectorBean.getExperimentType().equals("Fluorescence") && useFrames) {
			args = ArrayUtils.addAll(args, detectorList);
			counterTimer01.clearFrameSets();
			log("Frame collection time: " + mapScanParameters.getCollectionTime());
			int nx = calculateNumberXPoints();
			counterTimer01.addFrameSet(nx, 1.0E-4, mapScanParameters.getCollectionTime() * 1000.0, 0, 7, -1, 0);
		} else {
			for (Detector detector : detectorList) {
				args = ArrayUtils.add(args, detector);
				args = ArrayUtils.add(args, mapScanParameters.getCollectionTime());
			}
		}
		return args;
	}
	
	protected int calculateNumberXPoints() throws Exception {
		return ScannableUtils.getNumberSteps(xMotor, mapScanParameters.getXStart(), mapScanParameters.getXEnd(),
				mapScanParameters.getXStepSize()) + 1;
	}

	protected void moveEnergyAndZBeforeMap() throws DeviceException {
		double energy = mapScanParameters.getEnergy();
		double zScannablePos = mapScanParameters.getZValue();

		log("Energy: " + energy);
		energy_scannable.moveTo(energy);
		mfd.setEnergyValue(energy);

		log("Moving " + zMotor.getName() + " to " + zScannablePos);
		zMotor.moveTo(zScannablePos);
		mfd.setZValue(zScannablePos);
	}

	protected void createMFD(Detector[] detectorList) throws Exception {
		int nx = calculateNumberXPoints();
		int ny = ScannableUtils.getNumberSteps(yMotor, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
				mapScanParameters.getYStepSize()) + 1;
		log("Number x points: " + nx);
		log("Number y points: " + ny);
		mfd = new MicroFocusWriterExtender(nx, ny, mapScanParameters.getXStepSize(), mapScanParameters.getYStepSize(), detectorConfigurationBean, detectorList);
		// this updates the Elements view in the Mircofocus UI with the list of elements
		if (elementListScriptController != null){
			String fluoConfigFileName = this.experimentFullPath + detectorBean.getFluorescenceParameters().getConfigFileName();
			elementListScriptController.update(this,fluoConfigFileName);
		}
	}

	@Override
	protected DataWriter createAndConfigureDataWriter(String sampleName, List<String> descriptions)
			throws Exception {
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

}
