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
import gda.data.scan.datawriter.TwoDScanRowReverser;
import gda.data.scan.datawriter.XasAsciiNexusDataWriter;
import gda.data.scan.datawriter.XasAsciiNexusDatapointCompletingDataWriter;
import gda.device.Scannable;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.LineRepeatingBeamMonitor;
import gda.device.scannable.RealPositionReader;
import gda.device.scannable.ScannableUtils;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.logging.LoggingScriptController;

import java.util.ArrayList;
import java.util.List;

import uk.ac.gda.server.exafs.scan.BeamlinePreparer;
import uk.ac.gda.server.exafs.scan.OutputPreparer;
import uk.ac.gda.server.exafs.scan.SampleEnvironmentPreparer;

public class FasterRasterMap extends RasterMap {

	// TODO builder design pattern
	public FasterRasterMap(BeamlinePreparer beamlinePreparer, RasterMapDetectorPreparer detectorPreparer,
			SampleEnvironmentPreparer samplePreparer, OutputPreparer outputPreparer, Processor commandQueueProcessor,
			LoggingScriptController XASLoggingScriptController, AsciiDataWriterConfiguration datawriterconfig,
			ArrayList<AsciiMetadataConfig> original_header, Scannable energy_scannable, NXMetaDataProvider metashop,
			boolean includeSampleNameInNexusName, ContinuouslyScannable trajectoryMotor,
			RealPositionReader positionReader, Scannable yMotor, Scannable zMotor,
			LineRepeatingBeamMonitor trajectoryBeamMonitor, ScriptControllerBase elementListScriptController) {
		super(beamlinePreparer, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor,
				XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, metashop,
				includeSampleNameInNexusName, trajectoryMotor, positionReader, yMotor, zMotor, trajectoryBeamMonitor,
				elementListScriptController);
	}

	@Override
	public String getScanType() {
		return "Faster (two-way) Raster Map";
	}

	@Override
	protected DataWriter createAndConfigureDataWriter(String sampleName, List<String> descriptions) throws Exception {

		int nx = calculateNumberXPoints();
		int ny = ScannableUtils.getNumberSteps(yMotor, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
				mapScanParameters.getYStepSize()) + 1;
		
		TwoDScanRowReverser rowR = new TwoDScanRowReverser();
		rowR.setNoOfColumns(nx);
		rowR.setNoOfRows(ny);
		rowR.setReverseOdd(true);

		XasAsciiNexusDatapointCompletingDataWriter twoDWriter = new XasAsciiNexusDatapointCompletingDataWriter();
		twoDWriter.setIndexer(rowR);

		DataWriter underlyingDataWriter = twoDWriter.getDatawriter();
		underlyingDataWriter.addDataWriterExtender(mfd);
		if (underlyingDataWriter instanceof XasAsciiNexusDataWriter) {
			setupXasAsciiNexusDataWriter(sampleName, descriptions, (XasAsciiNexusDataWriter) underlyingDataWriter);
		}
		
		return twoDWriter;
	}
}
