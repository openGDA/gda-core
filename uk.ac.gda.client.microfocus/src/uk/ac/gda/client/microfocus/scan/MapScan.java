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
import gda.data.scan.datawriter.DummyDataWriter;
import gda.data.scan.datawriter.XasAsciiNexusDatapointCompletingDataWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.detector.countertimer.TfgScalerWithFrames;
import gda.device.scannable.ScannableUtils;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.scan.Scan;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.client.microfocus.scan.datawriter.MicroFocusWriterExtender;
import uk.ac.gda.server.exafs.scan.BeamlinePreparer;
import uk.ac.gda.server.exafs.scan.DetectorPreparer;
import uk.ac.gda.server.exafs.scan.ExafsScan;
import uk.ac.gda.server.exafs.scan.OutputPreparer;
import uk.ac.gda.server.exafs.scan.SampleEnvironmentPreparer;

public class MapScan extends ExafsScan {

	private final TfgScalerWithFrames counterTimer01;
	private final Scannable xScan;
	private final Scannable yScan;
	private final Scannable zScannable;

	private MicroFocusWriterExtender mfd;
	private MicroFocusScanParameters mapScanParameters;

	public MapScan(BeamlinePreparer beamlinePreparer, DetectorPreparer detectorPreparer,
			SampleEnvironmentPreparer samplePreparer, OutputPreparer outputPreparer, Processor commandQueueProcessor,
			LoggingScriptController XASLoggingScriptController, AsciiDataWriterConfiguration datawriterconfig,
			ArrayList<AsciiMetadataConfig> original_header, Scannable energy_scannable, NXMetaDataProvider metashop,
			boolean includeSampleNameInNexusName, TfgScalerWithFrames counterTimer01, Scannable xScan, Scannable yScan,
			Scannable zScannable) {

		super(beamlinePreparer, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor,
				XASLoggingScriptController, datawriterconfig, original_header, energy_scannable,
				includeSampleNameInNexusName, metashop);

		this.counterTimer01 = counterTimer01;
		this.xScan = xScan;
		this.yScan = yScan;
		this.zScannable = zScannable;
	}

	@Override
	protected String getScanType() {
		return "Step Map";
	}

	public MicroFocusWriterExtender getMFD() {
		return mfd;
	}

	@Override
	protected Object[] createScanArguments(String sampleName, List<String> descriptions) throws Exception {
		
		mapScanParameters = (MicroFocusScanParameters) scanBean;

		Detector[] detectorList = getDetectors();

		int nx = ScannableUtils.getNumberSteps(xScan, mapScanParameters.getXStart(), mapScanParameters.getXEnd(),
				mapScanParameters.getXStepSize()) + 1;
		int ny = ScannableUtils.getNumberSteps(yScan, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
				mapScanParameters.getYStepSize()) + 1;
		log("Number x points: " + nx);
		log("Number y points: " + ny);

		double energy = mapScanParameters.getEnergy();
		double zScannablePos = mapScanParameters.getZValue();

		createMFD(nx, ny, mapScanParameters.getXStepSize(), mapScanParameters.getYStepSize(), detectorList);

		log("Energy: " + energy);
		energy_scannable.moveTo(energy);
		mfd.setEnergyValue(energy);

		log("Moving " + zScannable.getName() + " to " + zScannablePos);
		zScannable.moveTo(zScannablePos);
		mfd.setZValue(zScannablePos);

		Object[] args = new Object[] { yScan, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
				mapScanParameters.getYStepSize(), xScan, mapScanParameters.getXStart(), mapScanParameters.getXEnd(),
				mapScanParameters.getXStepSize(), zScannable };		
		boolean useFrames = LocalProperties.check("gda.microfocus.scans.useFrames");
		log("Using frames: " + useFrames);
		if (detectorBean.getExperimentType().equals("Fluorescence") && useFrames) {
			args = ArrayUtils.addAll(args, detectorList);
			counterTimer01.clearFrameSets();
			log("Frame collection time: " + mapScanParameters.getCollectionTime());
			counterTimer01.addFrameSet(nx, 1.0E-4, mapScanParameters.getCollectionTime() * 1000.0, 0, 7, -1, 0);
		} else {
			for (Detector detector : detectorList) {
				args = ArrayUtils.add(args, detector);
				args = ArrayUtils.add(args, mapScanParameters.getCollectionTime());
			}
		}

		return args;
	}

	private void createMFD(int nx, int ny, double xStepSize, double yStepSize, Detector[] detectorList) {
		mfd = new MicroFocusWriterExtender(nx, ny, xStepSize, yStepSize, detectorConfigurationBean, detectorList);
	}

	protected Scan createAndConfigureDataWriter(Scan thisscan, String sampleName, List<String> descriptions)
			throws Exception {

		DataWriter datawriter = super.createAndConfigureDataWriter(sampleName, descriptions);

		// for unit testing of this class
		if (datawriter instanceof DummyDataWriter) {
			thisscan.setDataWriter(datawriter);
			return thisscan;
		}

		XasAsciiNexusDatapointCompletingDataWriter twoDWriter = new XasAsciiNexusDatapointCompletingDataWriter();
		twoDWriter.addDataWriterExtender(mfd);
		twoDWriter.setDatawriter(datawriter);
		thisscan.setDataWriter(twoDWriter);
		return thisscan;
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
