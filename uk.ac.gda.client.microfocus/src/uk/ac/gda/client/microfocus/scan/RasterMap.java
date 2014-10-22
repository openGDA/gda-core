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
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.XasAsciiNexusDatapointCompletingDataWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.BufferedDetector;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.LineRepeatingBeamMonitor;
import gda.device.scannable.RealPositionReader;
import gda.device.scannable.ScannableUtils;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.scan.ContinuousScan;

import java.util.ArrayList;
import java.util.List;

import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.client.microfocus.scan.datawriter.MicroFocusWriterExtender;
import uk.ac.gda.server.exafs.scan.BeamlinePreparer;
import uk.ac.gda.server.exafs.scan.ExafsScan;
import uk.ac.gda.server.exafs.scan.OutputPreparer;
import uk.ac.gda.server.exafs.scan.SampleEnvironmentPreparer;

/**
 * Performs raster maps by running a ContinuousScan as the innermost dimension in a 2D ConcurrentScan.
 * <p>
 * Requires a ContinuouslyScannable to be operated as the x axis and a RealPositionReader to return the actual motor positions.
 */
public class RasterMap extends ExafsScan implements MappingScan {

	private ContinuouslyScannable trajectoryMotor;
	private MicroFocusWriterExtender mfd;
	private MicroFocusScanParameters mapScanParameters;
	private RasterMapDetectorPreparer bufferedDetectorPreparer;
	private RealPositionReader positionReader;
	private Scannable yMotor;
	private Scannable zMotor;
	private LineRepeatingBeamMonitor trajectoryBeamMonitor;
	
	public RasterMap(BeamlinePreparer beamlinePreparer, RasterMapDetectorPreparer detectorPreparer,
			SampleEnvironmentPreparer samplePreparer, OutputPreparer outputPreparer, Processor commandQueueProcessor,
			LoggingScriptController XASLoggingScriptController, AsciiDataWriterConfiguration datawriterconfig,
			ArrayList<AsciiMetadataConfig> original_header, Scannable energy_scannable,
			boolean includeSampleNameInNexusName, NXMetaDataProvider metashop,
			ContinuouslyScannable trajectoryMotor, RealPositionReader positionReader, 
			Scannable yMotor,Scannable zMotor, LineRepeatingBeamMonitor trajectoryBeamMonitor
			) {
		super(beamlinePreparer, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor,
				XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, includeSampleNameInNexusName,
				metashop);
		
		this.bufferedDetectorPreparer = detectorPreparer;
		this.positionReader = positionReader;
		this.yMotor = yMotor;
		this.zMotor = zMotor;
		this.trajectoryBeamMonitor = trajectoryBeamMonitor;
		this.setTrajectoryMotor(trajectoryMotor);
	}
	
	@Override
	protected String getScanType() {
		return "Raster Map";
	}
	
	@Override
	public MicroFocusWriterExtender getMFD() {
		return mfd;
	}
	
	@Override
	protected Object[] createScanArguments(String sampleName, List<String> descriptions) throws Exception {
		mapScanParameters = (MicroFocusScanParameters) scanBean;

		BufferedDetector[] detectorList = bufferedDetectorPreparer.getRasterMapDetectors();

		createMFD(detectorList);

		moveEnergyAndZBeforeMap();

		ContinuousScan cs = new ContinuousScan(trajectoryMotor, mapScanParameters.getXStart(), mapScanParameters.getXEnd(), calculateNumberXPoints(), mapScanParameters.getRowTime(), detectorList) ;
		
		//TODO have not done the custom settings for raster maps for the monitor objects
		
		Object[] args = new Object[] {yMotor, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),  mapScanParameters.getYStepSize(), trajectoryBeamMonitor, cs, positionReader};
		
		return args;
	}

	private int calculateNumberXPoints() {
		return (int) (Math.abs(mapScanParameters.getXEnd()- mapScanParameters.getXStart())/mapScanParameters.getXStepSize() + 1);
	}

	private void moveEnergyAndZBeforeMap() throws DeviceException {
		double energy = mapScanParameters.getEnergy();
		double zScannablePos = mapScanParameters.getZValue();

		log("Energy: " + energy);
		energy_scannable.moveTo(energy);
		mfd.setEnergyValue(energy);

		log("Moving " + zMotor.getName() + " to " + zScannablePos);
		zMotor.moveTo(zScannablePos);
		mfd.setZValue(zScannablePos);
	}

	private void createMFD(Detector[] detectorList) throws Exception {
		int nx = calculateNumberXPoints();
		int ny = ScannableUtils.getNumberSteps(yMotor, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
				mapScanParameters.getYStepSize()) + 1;
		log("Number x points: " + nx);
		log("Number y points: " + ny);
		mfd = new MicroFocusWriterExtender(nx, ny, mapScanParameters.getXStepSize(), mapScanParameters.getYStepSize(), detectorConfigurationBean, detectorList);
	}

	@Override
	protected DataWriter createAndConfigureDataWriter(String sampleName, List<String> descriptions)
			throws Exception {
		DataWriter datawriter = super.createAndConfigureDataWriter(sampleName, descriptions);
		XasAsciiNexusDatapointCompletingDataWriter twoDWriter = new XasAsciiNexusDatapointCompletingDataWriter();
		twoDWriter.addDataWriterExtender(mfd);
		twoDWriter.setDatawriter(datawriter);
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

	public ContinuouslyScannable getTrajectoryMotor() {
		return trajectoryMotor;
	}

	public void setTrajectoryMotor(ContinuouslyScannable trajectoryMotor) {
		this.trajectoryMotor = trajectoryMotor;
	}

	public void setPositionReader(RealPositionReader positionReader) {
		this.positionReader = positionReader;
	}
}
