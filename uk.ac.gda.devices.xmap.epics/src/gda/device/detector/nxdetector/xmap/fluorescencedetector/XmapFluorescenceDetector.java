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

package gda.device.detector.nxdetector.xmap.fluorescencedetector;

import gda.device.DeviceException;
import gda.device.detector.HardwareTriggeredNXDetector;
import gda.device.detector.nxdetector.NXPluginBase;
import gda.device.detector.nxdetector.plugin.areadetector.ADRoiStatsPair;
import gda.device.detector.nxdetector.xmap.collectionStrategy.XmapCollectionStrategyBeanInterface;
import gda.device.detector.nxdetector.xmap.controller.XmapMappingModeEpicsLayer;
import gda.device.detector.nxdetector.xmap.controller.XmapModes.CollectionModeEnum;
import gda.device.detector.nxdetector.xmap.controller.XmapModes.PresetMode;
import gda.device.detector.xmap.NexusXmap;
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


public class XmapFluorescenceDetector implements FluorescenceDetector, InitializingBean, IObservable {
	// I08 uses 2 different EPICs interface for Xmap detector (one for step scan and one for raster scan)
	// For now, still use both interfaces
	//TODO: get the interface for Xmap used in raster scan working with step scan.
	private NexusXmap edxdInterface;
	private HardwareTriggeredNXDetector nxdetectorInterface;
	// For i08 4 Elements appear in EPICs but only one is used, set up in spring
	private final int numberOfElements;
	private final List<ADRoiStatsPair> nxdetectorROI;
	private String name;
	private static final Logger logger = LoggerFactory.getLogger(NexusXmap.class);
	// Hardcoded as I08 TwoDScanPlotter does not work properly when the roi name changes
	private final List<String> roiNameList;

	public XmapFluorescenceDetector(int numberOfElements) {
		this.numberOfElements = numberOfElements;
		nxdetectorROI = new ArrayList<ADRoiStatsPair>();
		roiNameList = new ArrayList<String>();
		for (int i = 1; i < 8; i++) {
			roiNameList.add("roi" + i);
		}

	}

	public final List<ADRoiStatsPair> updateNxdetectorROIList() {
		nxdetectorROI.clear();
		for (NXPluginBase plugin : nxdetectorInterface.getAdditionalPluginList()) {
			if (plugin instanceof ADRoiStatsPair)
				nxdetectorROI.add((ADRoiStatsPair) plugin);
		}
		return nxdetectorROI;
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
	public double[][] getMCAData(double time) throws DeviceException {
		XmapCollectionStrategyBeanInterface xmapCollectionStrategy = null;
		double[][] MCAdata = null;
		xmapCollectionStrategy = (XmapCollectionStrategyBeanInterface) (nxdetectorInterface.getCollectionStrategy());
		try {
			MCAdata = new double[numberOfElements][xmapCollectionStrategy.getXmap().getNbins()];
			// follow this logic due to a bug in EPICs
			xmapCollectionStrategy.getXmap().setCollectMode(CollectionModeEnum.MCA_MAPPING);
			XmapMappingModeEpicsLayer mappingXmap = (XmapMappingModeEpicsLayer) (xmapCollectionStrategy.getXmap().getCollectionMode());
			mappingXmap.setIgnoreGate(true);
			xmapCollectionStrategy.getXmap().setCollectMode(CollectionModeEnum.MCA_SPECTRA);
			xmapCollectionStrategy.getXmap().setPresetMode(PresetMode.REAL_TIME);
			xmapCollectionStrategy.getXmap().setAquisitionTime(time / 1000);
			logger.info("Acquisition time set");
			edxdInterface.collectData();
			logger.info("Collecting data");
			for (int i = 0; i < numberOfElements; i++) {
				// in EDXD interface, the getData already contains a waitWhileBusy()
				for (int j = 0; j < edxdInterface.getData()[i].length; j++)
					MCAdata[i][j] = (double) (edxdInterface.getData(i)[j]);
			}
		} catch (Exception e) {
			logger.error("Error getting MCA data.", e);
			throw new DeviceException("Error getting MCA data:" + e);
		}

		return MCAdata;
	}

	@Override
	public int getNumberOfElements() {
		return numberOfElements;
	}

	@Override
	public int getMCASize() {
		int numberOfBins = 0;
		try {
			numberOfBins = edxdInterface.getNumberOfBins();
		} catch (DeviceException e) {
			logger.error("Error getting MCA size.", e);
		}
		return numberOfBins;
	}

	@Override
	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception {
		List<DetectorROI> roisList;
		int numberOfRois = getMaxNumberOfRois(); // updates ROIs list as a side effect
		final double[][] rois = new double[numberOfRois][2];
		int roiIndex;

		for (int i = 0; i < numberOfElements; i++) {
			roisList = parameters.getDetector(i).getRegionList();
			roiIndex = 0;
			for (DetectorROI detectorROI : roisList) {
				rois[roiIndex][0] = detectorROI.getRoiStart();
				rois[roiIndex][1] = detectorROI.getRoiEnd();
				// By using an area detector only use X dimensions, the Y dimension is related to the number of elements
				if (numberOfRois != 0) {
					nxdetectorROI.get(roiIndex).setRoi(detectorROI.getRoiStart(), 0, (detectorROI.getRoiEnd() - detectorROI.getRoiStart() + 1), 1,
							roiNameList.get(roiIndex));
				} else {
					logger.warn("NXDetector should contain at least one ROI.");
				}
				roiIndex++;
			}
			edxdInterface.getController().setROI(rois, i);
		}
	}

	@Override
	public VortexParameters getConfigurationParameters() {
		if (edxdInterface.getVortexParameters().getRois() == null) {
			edxdInterface.getVortexParameters().getRois().add(new DetectorROI());
		}

		List<DetectorElement> detectorList = new ArrayList<DetectorElement>();

		for (int i = 0; i < numberOfElements; i++) {
			DetectorElement thisElement = new DetectorElement();
			for (DetectorROI region : edxdInterface.getVortexParameters().getRois()) {
				thisElement.addRegion(region);
			}
			detectorList.add(thisElement);
		}

		VortexParameters parameters = new VortexParameters();
		parameters.setDetectorList(detectorList);
		parameters.setDetectorName(getName());

		return parameters;
	}

	public NexusXmap getEdxdInterface() {
		return edxdInterface;
	}

	public void setEdxdInterface(NexusXmap edxdInterface) {
		this.edxdInterface = edxdInterface;
	}

	public HardwareTriggeredNXDetector getNxdetectorInterface() {
		return nxdetectorInterface;
	}

	public void setNxdetectorInterface(HardwareTriggeredNXDetector nxdetectorInterface) {
		this.nxdetectorInterface = nxdetectorInterface;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (edxdInterface == null){
			logger.error("NexusMap does not exist.");
			throw new NullPointerException("NexusMap does not exist.");
		}
		if (nxdetectorInterface == null){
			logger.error("HardwareTriggeredNXDetector does not exist.");
			throw new NullPointerException("HardwareTriggeredNXDetector does not exist.");
		}
		if (name == null) {
			logger.error("XmapFluorescenceDetector name must be set.");
			throw new NullPointerException("XmapFluorescenceDetector name must be set.");
		}
		if (getMaxNumberOfRois() == 0) {
			logger.error("NXDetector should contain at least one ROI.");
			throw new Exception("NXDetector should contain at least one ROI.");
		}

	}

	// Maximum number of ROIs should be given by NXdetector because EDXD will always contain more ROIs
	@Override
	public int getMaxNumberOfRois() {
		updateNxdetectorROIList();
		return nxdetectorROI.size();
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
