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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gda.device.detector.DummyDAServer;
import gda.device.timer.Etfg;
import gda.factory.FactoryException;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.DummyXspress3Controller;

/**
 * Tests for Xspress3 detector to check that :
 * <li>Controller is configured correctly from parameter bean by {@link Xspress3Detector#applyConfigurationParameters(FluorescenceDetectorParameters)}.
 * <li>{@link Xspress3Detector#getConfigurationParameters()} returns a parameter bean consistent with settings on the detector.
 *
 */
public class Xspress3ConfigurationParametersTest {

	private Xspress3Detector xspress3detector;

	@Before
	public void setupXSpress3() throws FactoryException {
		DummyDAServer daserver = new DummyDAServer();
		daserver.configure();

		Etfg tfg = new Etfg();
		tfg.setDaServer(daserver);
		tfg.configure();

		DummyXspress3Controller controllerForDetector = new DummyXspress3Controller(tfg, daserver);
		controllerForDetector.setName("controllerForDetector");
		controllerForDetector.setNumFramesToAcquire(1);
		controllerForDetector.setNumberOfChannels(4); //number of detector elements
		controllerForDetector.configure();
		controllerForDetector.setSimulationFileName("/scratch/testfile");
		controllerForDetector.configure();

		xspress3detector = new Xspress3Detector();
		xspress3detector.setName("xspress3detector");
		xspress3detector.setController(controllerForDetector);
		xspress3detector.setNumTrackerExtension("");
		xspress3detector.configure();
	}

	/**
	 * Set parameters object for Xspress3.
	 * Note that scaler window range for each element is NOT stored in the file, only the ROIs.
	 * Window range for scaler 1 and scaler 2 are set on hardware using ranges of ROI1, ROI2.
	 * @return Xspress3Parameters object
	 */
	private Xspress3Parameters getParameters() {
		Xspress3Parameters params = new Xspress3Parameters();
		params.setDetectorName(xspress3detector.getName());
		for(int i=0; i<xspress3detector.getNumberOfElements(); i++) {
			DetectorElement newElement = new DetectorElement();
			newElement.setName("Element"+i);
			newElement.setNumber(i);
			List<DetectorROI> regions = Arrays.asList(new DetectorROI("ROI0", 11, 22));
			newElement.setRegionList(regions);

			params.addDetectorElement(newElement);
		}
		return params;
	}

	@Test
	public void testParamsSetOnControllerCorrectly() throws Exception {
		Xspress3Parameters parameters = getParameters();
		xspress3detector.applyConfigurationParameters(parameters);

		Xspress3Controller controller = xspress3detector.getController();

		for(int i=0; i<xspress3detector.getNumberOfElements(); i++) {
			DetectorElement paramForElement = parameters.getDetector(i);

			assertEquals(paramForElement.isExcluded(), !controller.isChannelEnabled(i));

			// Check ROI1 range
			Integer[] roiFromController = controller.getROILimits(i, 0);
			assertEquals((Integer) paramForElement.getRegionList().get(0).getRoiStart(), roiFromController[0]);
			assertEquals((Integer) paramForElement.getRegionList().get(0).getRoiEnd(), roiFromController[1]);

			// Window range for scaler 1 should match range of ROI1
			Integer[] scalerWindow = controller.getWindows(i, 0);
			assertEquals((Integer) paramForElement.getRegionList().get(0).getRoiStart(), scalerWindow[0]);
			assertEquals((Integer) paramForElement.getRegionList().get(0).getRoiEnd(), scalerWindow[1]);
		}
	}

	@Test
	public void testGeneratedParamsAreCorrect() throws Exception {
		Xspress3Parameters parameters = getParameters();
		xspress3detector.applyConfigurationParameters(parameters);
		FluorescenceDetectorParameters parametersFromDetector = xspress3detector.getConfigurationParameters();
		assertEquals(parameters, parametersFromDetector);
	}

}
