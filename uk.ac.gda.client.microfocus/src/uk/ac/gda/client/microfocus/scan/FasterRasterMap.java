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

import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.TwoDScanRowReverser;
import gda.data.scan.datawriter.XasAsciiNexusDataWriter;
import gda.data.scan.datawriter.XasAsciiNexusDatapointCompletingDataWriter;
import gda.device.detector.BufferedDetector;
import gda.device.scannable.ScannableUtils;
import gda.scan.ContinuousScan;

public class FasterRasterMap extends RasterMap {

	@Override
	public String getScanType() {
		return "Faster (two-way) Raster Map";
	}

	@Override
	protected Object[] buildListOfArguments(BufferedDetector[] detectorList) {
		Object[] args = super.buildListOfArguments(detectorList);

		for (Object arg : args){
			if (arg instanceof ContinuousScan){
				((ContinuousScan)arg).setBiDirectional(true);
			}
		}

		return args;

//		ContinuousScan cs = new ContinuousScan(trajectoryMotor, mapScanParameters.getXStart(), mapScanParameters.getXEnd(), calculateNumberXPoints(), mapScanParameters.getRowTime(), detectorList) ;
//		cs.setBiDirectional(true);
//
//		//TODO have not done the custom settings for raster maps for the monitor objects
//
//		Object[] args = new Object[] {yMotor, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),  mapScanParameters.getYStepSize(), trajectoryBeamMonitor, cs};
//
//		// add a Scannable, if defined, which fetches the motor readback values from the Epics Trajectory template after the trajectory completes.
//		if (positionReader != null){
//			args = ArrayUtils.add(args, positionReader);
//		}
//
//		return args;


//		// TODO test this on beamline - I am really not sure if this will work first time - the constructor is Object[]
//		// and the dummy version does not work, so can only test on the beamline... yikes.
//
//		ScanPositionsTwoWay sptw = new ScanPositionsTwoWay(trajectoryMotor, mapScanParameters.getXStart(),
//				mapScanParameters.getXEnd(), mapScanParameters.getXStepSize());
//
//		Double dwellTime = mapScanParameters.getRowTime() / calculateNumberXPoints();
//		Object[] innerScanArgs = new Object[] { trajectoryMotor, sptw };
//		innerScanArgs = ArrayUtils.addAll(innerScanArgs, detectorList);
//		innerScanArgs = ArrayUtils.add(innerScanArgs, dwellTime);
//
//		TrajectoryScanLine tsl = new TrajectoryScanLine(innerScanArgs);
//		tsl.setScanDataPointQueueLength(10000);
//		tsl.setPositionCallableThreadPoolSize(10);
//
//		Object[] outerScanArgs = new Object[] { yMotor, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
//				mapScanParameters.getYStepSize(), trajectoryBeamMonitor, tsl };
//
//		// add a Scannable, if defined, which fetches the motor readback values from the Epics Trajectory template after
//		// the trajectory completes.
//		if (positionReader != null) {
//			outerScanArgs = ArrayUtils.add(outerScanArgs, positionReader);
//		}
//
//
////		I think that we could try using the ContinuousScan here - it would simply need modifying to accept a ScanPositionProvider instead of start. stop step but it should work....a
////
////		if it does then both I18 raster maps could use ContinuousScan instead of faster raster using TrajectoryScanLine  :)
//
//		return outerScanArgs;
	}

	@Override
	protected DataWriter createAndConfigureDataWriter(String sampleName, List<String> descriptions) throws Exception {

		int nx = calculateNumberXPoints();
		int ny = ScannableUtils.getNumberSteps(yScan, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
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
