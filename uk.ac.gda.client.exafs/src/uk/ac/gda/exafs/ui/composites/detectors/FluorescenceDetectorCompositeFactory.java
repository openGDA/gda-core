/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites.detectors;

import gda.device.DeviceException;
import gda.factory.Finder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.exafs.ui.composites.detectors.internal.FluoDetectorCompositeController;

public class FluorescenceDetectorCompositeFactory {

	private static final Logger logger = LoggerFactory.getLogger(FluorescenceDetectorCompositeFactory.class);

	public static FluorescenceDetectorComposite createNewXspress3Composite(Composite parent) {

		FluorescenceDetectorComposite x3Composite = null;
		try {
			FluorescenceDetector x3Detector = (FluorescenceDetector) Finder.getInstance().find("xspress3");
			FluorescenceDetectorParameters x3Parameters = x3Detector.getConfigurationParameters();
			if(x3Parameters == null){
				x3Parameters = createDefaultXspress3Parameters(x3Detector);
			}
			FluoDetectorCompositeController controller = new FluoDetectorCompositeController(parent, x3Parameters,
					x3Detector);
			x3Composite = controller.getFluorescenceDetectorComposite();
		} catch (DeviceException de) {
			logger.error("Error connecting to Xspress3 detector while creating Xspress3 configuration composite", de);
		} catch (Exception ex) {
			logger.error("Internal error while creating Xspress3 configuration composite", ex);
		}
		return x3Composite;
	}

	private static Xspress3Parameters createDefaultXspress3Parameters(FluorescenceDetector detector)
			throws DeviceException {

		Xspress3Parameters xspress3Parameters = new Xspress3Parameters();

		xspress3Parameters.setCollectionTime(1000.0);
		int numberOfElements = detector.getNumberOfChannels();
		DetectorROI[] regions = detector.getRegionsOfInterest();

		if (regions == null || regions.length == 0) {
			// create a default
			regions = new DetectorROI[1];
			regions[0] = new DetectorROI();
			regions[0].setRoiName("ROI 1");
			regions[0].setRoiStart(700);
			regions[0].setRoiEnd(800);
		}

		// TODO this is forced to use vortex DetectorElements instead of xspress DetectorElements
		// fix by overriding methods in VortexParameters? Or join types? Or use an interface?
		List<DetectorElement> detElements = new ArrayList<DetectorElement>(numberOfElements);

		for (int index = 0; index < numberOfElements; index++) {
			DetectorElement thisElement = new DetectorElement();
			for (DetectorROI region : regions) {
				thisElement.addRegion(region);
			}
			thisElement.setNumber(index);
			thisElement.setName("element" + index);
			detElements.add(thisElement);
		}
		xspress3Parameters.setDetectorList(detElements);

		return xspress3Parameters;
	}

	public static FluorescenceDetectorComposite createNewXspress2Composite(Composite parent) {

		FluorescenceDetectorComposite x2Composite = null;
		try {
			FluorescenceDetector x2Detector = (FluorescenceDetector) Finder.getInstance().find("xspress2system");
			FluorescenceDetectorParameters x2Parameters = x2Detector.getConfigurationParameters();
			FluoDetectorCompositeController controller = new FluoDetectorCompositeController(parent, x2Parameters,
					x2Detector);
			x2Composite = controller.getFluorescenceDetectorComposite();
		} catch (DeviceException de) {
			logger.error("Error connecting to Xspress2 detector while creating Xspress2 configuration composite", de);
		} catch (Exception ex) {
			logger.error("Internal error while creating Xspress2 configuration composite", ex);
		}
		return x2Composite;
	}
}
