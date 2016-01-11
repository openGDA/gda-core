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

package gda.device.detector.xmap;

import gda.device.DeviceException;
import gda.factory.Findable;
import gda.observable.IObservable;
import gda.observable.IObserver;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;

public class NexusXmapFluorescenceDetectorAdapter implements FluorescenceDetector, InitializingBean, Findable, IObservable {

	private static final Logger logger = LoggerFactory.getLogger(NexusXmapFluorescenceDetectorAdapter.class);

	protected NexusXmap xmap;

	private int numberOfElements;

	private String name;

	private int maximumNumberOfRois = 32;

	public NexusXmapFluorescenceDetectorAdapter(NexusXmap xmap, int numberOfElements) {
		this.xmap = xmap;
		this.numberOfElements = numberOfElements;
	}

	@Override
	public double[][] getMCAData(double time) throws DeviceException {
		double[][] data = new double[numberOfElements][xmap.getNumberOfBins()];
		int[][] xmapData;
		xmap.setCollectionTime(time);
		xmap.collectData();
		xmapData = xmap.getData();
		for (int i = 0; i < numberOfElements; i++) {
			for (int j = 0; j < xmap.getNumberOfBins(); j++) {
				data[i][j] = xmapData[i][j];
			}
		}
		return data;
	}

	@Override
	public int getNumberOfElements() {
		return numberOfElements;
	}

	@Override
	public int getMCASize() {
		try {
			return xmap.getNumberOfBins();
		} catch (DeviceException e) {
			logger.error("Error getting MCA size.", e);
			return 0;
		}
	}

	@Override
	public int getMaxNumberOfRois() {
		return maximumNumberOfRois;
	}

	@Override
	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception {
		if (parameters instanceof VortexParameters) {
			xmap.setParameters((VortexParameters) parameters, true);
			return;
		}
		int roiCount = 0;
		for (DetectorElement element : parameters.getDetectorList()) {
			int size = element.getRegionList().size();
			roiCount = roiCount > size ? roiCount : size;
		}

		final double[][] rois = new double[roiCount][2];
		int idx = 0;
		for (DetectorElement element : parameters.getDetectorList()) {
			int roiIndex = 0;
			for (DetectorROI roi : element.getRegionList()) {
				rois[roiIndex][0] = roi.getRoiStart();
				rois[roiIndex][1] = roi.getRoiEnd();
				roiIndex++;
			}
			xmap.getController().setROI(rois, idx++);
		}

	}

	@Override
	public FluorescenceDetectorParameters getConfigurationParameters() {
		List<DetectorElement> elementList = new ArrayList<DetectorElement>();
		for (int i = 0; i < numberOfElements; i++) {
			DetectorElement paramElement = new DetectorElement();
			paramElement.setName("Element" + i);
			double[][] rois;
			try {
				rois = xmap.getController().getROIParameters(i);
			} catch (DeviceException e) {
				logger.error("Could not get parameters from detector", e);
				rois = new double[][] {};
			}
			for (int j = 0; j < Math.min(maximumNumberOfRois, rois.length); j++) {
				if (rois[j][0] >= rois[j][1]) {
					continue;
				}
				// Index roi names from 1, not 0
				paramElement.addRegion(new DetectorROI("Roi" + (j + 1), (int) rois[j][0], (int) rois[j][1]));
			}
			elementList.add(paramElement);
		}
		VortexParameters parameters = new VortexParameters();
		parameters.setDetectorList(elementList);
		parameters.setDetectorName(getName());
		return parameters;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (xmap == null) {
			logger.error("No NexusXmap provided");
			throw new NullPointerException("No NexusXmap provided");
		}
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addIObserver(IObserver observer) {
	}

	@Override
	public void deleteIObserver(IObserver observer) {
	}

	@Override
	public void deleteIObservers() {
	}

}
