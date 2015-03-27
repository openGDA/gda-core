/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.subdetector;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DataDimension;
import gda.device.detector.NXDetectorData;
import gda.device.detector.xmap.EpicsXmapController;
import gda.device.detector.xmap.XmapController;
import gda.factory.FactoryException;
import gda.factory.Finder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem;

/**
 * class for integrating a single vortex detector via xmap into the ncddetector system
 */
public class NcdXmapDetector extends NcdSubDetector  {

	private static final Logger logger = LoggerFactory.getLogger(NcdXmapDetector.class);

	private XmapController xmapcontroller;
	private String xmapControllerName;
	boolean clear = false;

	@Override
	public void clear() throws DeviceException {
		stop();
		clear = true;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return NcdDetectorSystem.FLUORESCENCE_DETECTOR;
	}

	@Override
	public int getMemorySize() throws DeviceException {
		return 0;
	}

	@Override
	public List<DataDimension> getSupportedDimensions() throws DeviceException {
		return null;
	}

	@Override
	public void setDataDimensions(int[] detectorSize) throws DeviceException {
	}

	@Override
	public void start() throws DeviceException {
		if (clear)
			xmapcontroller.clearAndStart();
		else 
			xmapcontroller.start();
		clear = false;
		
		int count = 0;
		while (xmapcontroller.getStatus() != Detector.BUSY) {
			if (count > 20) 
				throw new DeviceException("detector does not act on start request");
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new DeviceException("Interrupted while waiting for detector to become busy.");
			}
		}
	}

	@Override
	public void stop() throws DeviceException {
		xmapcontroller.stop();
	}

	@Override
	public void writeout(int frames, NXDetectorData dataTree) throws DeviceException {
		xmapcontroller.stop();
		int[][] detectorData = xmapcontroller.getData();
		
		NexusGroupData ngd = new NexusGroupData(detectorData[0]);
		ngd.isDetectorEntryData = true;
		dataTree.addData(getTreeName(), ngd, "counts", 1);

		if (xmapcontroller instanceof EpicsXmapController) {
			EpicsXmapController emc = (EpicsXmapController) xmapcontroller;
			double[] energyaxis = emc.getEnergyBins()[0];
			NexusGroupData angd = new NexusGroupData(energyaxis);
			dataTree.addAxis(getTreeName(), "energy", angd, 1, 1, "keV", false);
		}
		
		addMetadata(dataTree);
	}

	@Override
	public void configure() throws FactoryException {
		if (xmapControllerName == null) throw new FactoryException("no controller defined");
		if (xmapcontroller == null) {
			if ((xmapcontroller = (XmapController) Finder.getInstance().find(xmapControllerName)) != null)
				logger.debug("controller {} found", xmapControllerName);
			else {
				logger.error("EpicsXmapController {} not found", xmapControllerName);
				throw new FactoryException("EpicsXmapController " + xmapControllerName + " not found");
			}
		}
	}

	public String getXmapControllerName() {
		return xmapControllerName;
	}

	public void setXmapControllerName(String xmapControllerName) {
		this.xmapControllerName = xmapControllerName;
	}
	
	public XmapController getXmapController() {
		return xmapcontroller;
	}

	public void setXmapController(XmapController xmapcontroller) {
		this.xmapcontroller = xmapcontroller;
	}
}