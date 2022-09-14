/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.xspress4;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;
import gda.device.detector.xspress.xspress2data.Xspress2DeadtimeTools;
import uk.ac.gda.beans.vortex.DetectorDeadTimeElement;
import uk.ac.gda.beans.xspress.XspressDeadTimeParameters;
import uk.ac.gda.devices.detector.xspress4.XspressDataProvider;

public class DeadtimeCorrectionTest extends TestBase {

	@Before
	public void setup() throws Exception {
		setupDetectorObjects();
	}

	private DetectorDeadTimeElement getDeadtimeParameters() {
		double allEventGradient = 1.051e-9;
		double allEventOffset = 6.17815e-8;
		double inWindowGradient = 6.013e-10;
		double inWindowOffset = 6.738e-8;
		return new DetectorDeadTimeElement("Element0", 0, allEventGradient, allEventOffset, inWindowOffset, inWindowGradient);
	}


	/**
	 * Scaler values for each frame : total counts, tfg resets, frame time
	 * /dls/b18/data/2022/cm31142-4/nexus/554162.nxs
	 *
	 * @return
	 */
	private long[][] getScalerValues() {
		return new long[][] {
			{462529,  437446, 79996208},
			{1201816, 1216001, 79996208},
			{1736383, 1855461, 79996208},
			{1899508, 2067024, 79996208},
			{1904596, 2075920, 79996209}};
	}

	private double[] getDtcFactors() {
		return new double[] {1.079147628121705,
				1.2370795965113979,
				1.3859605872286347,
				1.4394586101551174,
				1.4412558901533754};
	}

	@Test
	public void testDtcFactorCalculation() {
		var dtcParameters = getDeadtimeParameters();

		// B18 slit scan 554162.nxs
		// Element0, frame index : 10, 20, 30, 40, 50

		// Scaler values Total counts, tfg resets, frame time
		long[][] allScalerValues = getScalerValues();

		// Expected deadtime correction values (from Epics)
		double[] dtcFactors = getDtcFactors();

		Xspress2DeadtimeTools dtcCalculationTools = new Xspress2DeadtimeTools();
		for(int i=0; i<allScalerValues.length; i++) {
			long[] scalerValues = allScalerValues[i];
			double dtcFactor = dtcCalculationTools.calculateDeadtimeCorrectionFactor(scalerValues[0], scalerValues[1], scalerValues[2], dtcParameters, 10.0);
			assertEquals("DTC factor for frame "+i+" is not correct", dtcFactors[i], dtcFactor, 1e-4);
		}
	}

	@Test
	public void testDtcFactorNexusTree() throws DeviceException {
		XspressDeadTimeParameters dtParams = new XspressDeadTimeParameters();
		dtParams.addDetectorDeadTimeElement(getDeadtimeParameters());
		double dtcEnergyKev = 10.0;

		long[][] scalerVals =getScalerValues();
		int numFrames = scalerVals.length;
		Dataset allScalerDataset = DatasetFactory.createFromObject(DoubleDataset.class, scalerVals, scalerVals.length, scalerVals[0].length);
		List<Dataset> scalerList = new ArrayList<>();
		// total counts (frame time)
		scalerList.add(allScalerDataset.getSlice(new int [] {0,2}, new int[] {numFrames,3}, null));
		// Tfg reset ticks
		scalerList.add(allScalerDataset.getSlice(new int [] {0,1}, new int[] {numFrames,2}, null));
		// Tfg reset counts - needs to be present (to match layout of data returned from real detector) but is not used in the DTC factor calculation
		scalerList.add(null);
		// Total counts
		scalerList.add(allScalerDataset.getSlice(new int [] {0,0}, new int[] {numFrames,1}, null));

		XspressDataProvider dataProvider = new XspressDataProvider();
		DoubleDataset dtcValues = (DoubleDataset) dataProvider.calculateDtcFactors(scalerList, dtParams, dtcEnergyKev);
		double[] expectedDtcFactors = getDtcFactors();
		for(int i=0; i<expectedDtcFactors.length; i++) {
			assertEquals(expectedDtcFactors[i], dtcValues.getDouble(i,0), 1e-4);
		}
	}

	@Test
	public void testDtcFromNexus() throws NexusException, DatasetException, DeviceException {
		String filePath = "testfiles/554163_b18.nxs";
		String groupName = "xspress3X";

		xspress4detector.setDtcEnergyKev(12);
		xspress4detector.loadDeadtimeParametersFromFile("testfiles/Xspress3X_Deadtime_ME7.xml");

		Dataset rawScalerTotal = (Dataset) HelperClasses.getDataset(filePath, groupName, "raw scaler total");
		Dataset tfgResets = (Dataset) HelperClasses.getDataset(filePath, groupName, "tfg resets");
		Dataset clockCycles = (Dataset) HelperClasses.getDataset(filePath, groupName, "tfg clock cycles");
		Dataset dtcFactors = (Dataset) HelperClasses.getDataset(filePath, groupName, "dtc factors");

		List<Dataset> scalerDatasets = new ArrayList<>();
		// total counts (frame time)
		scalerDatasets.add(clockCycles);
		// Tfg reset ticks
		scalerDatasets.add(tfgResets);
		// Tfg reset counts - needs to be present (to match layout of data returned from real detector) but is not used in the DTC factor calculation
		scalerDatasets.add(null);
		// Total counts
		scalerDatasets.add(rawScalerTotal);

		XspressDataProvider dataProvider = new XspressDataProvider();
		DoubleDataset dtcValues = (DoubleDataset) dataProvider.calculateDtcFactors(scalerDatasets, xspress4detector.getDeadtimeParameters(), xspress4detector.getDtcEnergyKev());

		// Use relatively large tolerance, since difference between Epics and
		// GDA DTC factors gets larger as DTC factor gets bigger.
		double tolerance = 0.04;

		int numFrames = rawScalerTotal.getShape()[0];
		int numChannels = rawScalerTotal.getShape()[1];
		for(int channel=0; channel<numChannels; channel++) {
			for(int i=0; i<numFrames; i++) {
				double dtcFactor = dtcValues.getDouble(i, channel);
				double dtcFactorFromNexus = dtcFactors.getDouble(i,channel);

				assertEquals("DTC factor for channel "+channel+" frame "+i+" is not within tolerance of "+tolerance+".", dtcFactorFromNexus, dtcFactor, tolerance);
			}
		}
	}

}
