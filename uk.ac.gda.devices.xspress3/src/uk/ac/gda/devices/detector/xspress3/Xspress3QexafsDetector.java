/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.countertimer.BufferedScaler;

/**
 * Single detector class solution for QEXAFS. This solves some timing issues
 * arising when using three separate detector classes (e.g. Xspress3QexafsDetector &
 * Xspress3BufferedDetectorv2 & BufferedDetector) especially related to readout of data
 * from Xspress3 HDF file.
 */
public class Xspress3QexafsDetector extends DetectorBase implements BufferedDetector {

	private static final String FFI0_LABEL = "FFI0";
	private static final String FF_LABEL = "FF";
	private static final String I0_LABEL = "I0";

	private Xspress3BufferedDetector qexafsXspress3;
	private BufferedScaler qexafsScaler;

	public void setQexafsXspress(Xspress3BufferedDetector qexafsXspress) {
		qexafsXspress3 = qexafsXspress;
	}

	public void setQexafsScaler(BufferedScaler scaler) {
		qexafsScaler = scaler;
	}

	@Override
	public String[] getExtraNames() {
		return (String[]) ArrayUtils.addAll(ArrayUtils.addAll(
			qexafsXspress3.getExtraNames(),
			qexafsScaler.getExtraNames()),
			new String[] {FFI0_LABEL});
	}

	@Override
	public String[] getOutputFormat() {
		return (String[]) ArrayUtils.addAll(ArrayUtils.addAll(
			qexafsXspress3.getOutputFormat(),
			qexafsScaler.getOutputFormat()),
			new String[] { "" });
	}

	@Override
	public void atScanStart() throws DeviceException {
		qexafsXspress3.atScanStart();
		qexafsScaler.atScanStart();
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		qexafsXspress3.prepareForCollection();
	}

	@Override
	public NXDetectorData[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		NXDetectorData[] data = qexafsXspress3.readFrames(startFrame, finalFrame);
		double[][] cTData = (double[][]) qexafsScaler.readFrames(startFrame, finalFrame);
		int i0Index = Arrays.asList(qexafsScaler.getExtraNames()).indexOf(I0_LABEL);

		// Zip all the data together in an NXDetectorData[].
		for (int i = 0; i < data.length; i++) {
			NXDetectorData tfg2Data = new NXDetectorData(
					(String[]) ArrayUtils.addAll(qexafsScaler.getExtraNames(), new String[] {FFI0_LABEL}),
					(String[]) ArrayUtils.addAll(qexafsScaler.getOutputFormat(), new String[] { "%.9f" } ),
					qexafsXspress3.getName());

			INexusTree tfg2Frame = tfg2Data.getDetTree(qexafsXspress3.getName());
			for (int j = 0; j < cTData[i].length; j++) {
				NXDetectorData.addData(
						tfg2Frame,
						qexafsScaler.getExtraNames()[j],
						new NexusGroupData(cTData[i][j]),
						"counts",
						2);

				tfg2Data.setPlottableValue(qexafsScaler.getExtraNames()[j],  cTData[i][j]);
			}

			// Calculate ffI0 using I0 from countertimer and FF from xspress3
			double i0 = cTData[i][i0Index];
			double ff = ((double[]) data[i].getDetTree(qexafsXspress3.getName()).getNode(FF_LABEL).getData().getBuffer())[0];
			double ffI0 = 0;
			if (i0 > 0 && ff > 0) {
				ffI0 = ff / i0;
			}

			NXDetectorData.addData(tfg2Frame, FFI0_LABEL, new NexusGroupData(ffI0), "", 2);
			tfg2Data.setPlottableValue(FFI0_LABEL,  ffI0);

			data[i].mergeIn(tfg2Data);
		}

		return data;
	}


	@Override
	public void collectData() throws DeviceException {
		// Do nothing.
	}


	@Override
	public int getStatus() throws DeviceException {
		return qexafsScaler.getStatus();
	}


	@Override
	public Object readout() throws DeviceException {
		return 1.0;  // Hopefully this isn't too important...
	}


	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}


	@Override
	public void clearMemory() throws DeviceException {
		qexafsScaler.clearMemory();
	}


	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		qexafsScaler.setContinuousMode(on);
		qexafsXspress3.setContinuousMode(on);
	}


	@Override
	public boolean isContinuousMode() throws DeviceException {
		return qexafsScaler.isContinuousMode();
	}


	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		qexafsScaler.setContinuousParameters(parameters);
		qexafsXspress3.setContinuousParameters(parameters);
	}


	@Override
	public ContinuousParameters getContinuousParameters() throws DeviceException {
		return qexafsScaler.getContinuousParameters();
	}


	@Override
	public int getNumberFrames() throws DeviceException {
		return Math.min(qexafsScaler.getNumberFrames(), qexafsXspress3.getNumberFrames());
	}


	@Override
	public Object[] readAllFrames() throws DeviceException {
		Integer lastFrame = getNumberFrames() - 1;
		return readFrames(0, lastFrame);
	}


	@Override
	public int maximumReadFrames() throws DeviceException {
		return Math.min(qexafsScaler.maximumReadFrames(), qexafsXspress3.maximumReadFrames());
	}
}
