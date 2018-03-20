/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.Detector;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DataDimension;
import gda.device.detector.NXDetectorData;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;
import uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem;
import uk.ac.gda.server.ncd.meta.INcdMetaProvider;

public class NcdSubDetector extends DeviceBase implements INcdSubDetector {

	private static final String SCALING_FACTOR = "scaling_factor";

	private static final Logger logger = LoggerFactory.getLogger(NcdSubDetector.class);

	protected Detector detector;
	protected String detectorType;
	protected double pixelSize = 0.0;
	private ArrayList<DataDimension> supportedDimensions = null;
	protected Map<String, Object> attributeMap = new HashMap<String, Object>();
	protected String description;
	protected DoubleDataset mask = null;
	protected String interpretation = null;
	private DetectorProperties dp = null;
	protected FileConfiguration configuration;
	private String serialNumber;

	protected Collection<INcdMetaProvider> metaProviders = new ArrayList<>();

	protected void restoreAttributeMap() {
		if (configuration != null) {
			return;
		}
		logger.trace("{} - configuration null - reloading", getName());
		try {
			configuration = LocalParameters.getXMLConfiguration(this.getClass().getCanonicalName()+":"+getName());
			for (Iterator<String> iterator = configuration.getKeys(); iterator.hasNext();) {
				String name = iterator.next();
				if (attributeMap.containsKey(name)) {
					logger.trace("{}.restore - {} already in map, ignoring later value ({})", getName(), name, attributeMap.get(name));
					continue; // don't overwrite
				}
				try {
					Object property = configuration.getProperty(name);
					if (property instanceof List) { // I get doubles I put in back in lists...
						property = ((List) property).get(0);
						logger.trace("{}.restore - {} returned list ({}) - using first value", getName(), name, property);
					}
					try {
						property = Double.parseDouble(property.toString());
						logger.trace("{}.restore - {} converted to double", getName(), name);
					} catch (Exception e) {
						// was worth a try (literally)
					}
					attributeMap.put(name, property);
					logger.trace("{}.restoring - {}: {}", getName(), name, configuration.getString(name));
				} catch (Exception e) {
					logger.info("Error restoring attribute '{}' for detector {}", name, getName());
				}
			}
			configuration.setAutoSave(true);
		} catch (Exception e) {
			logger.error("{} - error restoring attributes from LocalParameters", getName(), e);
		}
	}

	public Detector getDetector() {
		return detector;
	}

	public void setDetector(Detector detector) {
		this.detector = detector;
	}

	public String getTreeName() throws DeviceException {
		if (NcdDetectorSystem.SAXS_DETECTOR.equals(getDetectorType())) {
			return "detector";
		}
		return getName();
	}

	@Override
	public void configure() throws FactoryException {
		if (detector != null) {
			detector.reconfigure();
			setConfigured(((DeviceBase) detector).isConfigured());
		} else {
			setConfigured(false);
			throw new FactoryException("no detector configured!");
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		setConfigured(false);
		logger.debug("Reconfiguring " + getName());
		configure();
	}

	@Override
	public void clear() throws DeviceException {
		// N/A
	}

	@Override
	public void close() throws DeviceException {
		// N/A
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return detector.getDataDimensions();
	}

	@Override
	public String getDetectorType() {
		return detectorType;
	}

	@Override
	public int getMemorySize() throws DeviceException {
		return 0;
	}

	/**
	 * The supported dimensions are obtained firstly from the XML Spring configuration and if not there will try a
	 * detector attribute "DataDimensions" else it will fallback to the currently selected data dimensions.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<DataDimension> getSupportedDimensions() throws DeviceException {
		if (supportedDimensions == null) {
			supportedDimensions = (ArrayList<DataDimension>) getAttribute("SupportedDimensions");
			if (supportedDimensions == null) {
				int[] d = getDataDimensions();
				supportedDimensions = new ArrayList<DataDimension>();
				supportedDimensions.add(new DataDimension(d[0], d[1]));
			}
		}
		return supportedDimensions;
	}

	@Override
	public void setDataDimensions(int[] detectorSize) throws DeviceException {
		detector.setAttribute("DataDimensions", detectorSize);
		dp = null;
	}

	/**
	 * @param detectorType
	 *            the detectorType to set
	 */
	public void setDetectorType(String detectorType) {
		for (String type : NcdDetectorSystem.detectorTypes) {
			if (type.equalsIgnoreCase(detectorType)) {
				this.detectorType = detectorType;
				return;
			}
		}
		throw new IllegalArgumentException("Attempt to set unrecognised detector type " + detectorType);
	}

	@Override
	public void start() throws DeviceException {
		detector.collectData();
	}

	@Override
	public void stop() throws DeviceException {
		detector.stop();
	}

	public void setCollectionTime(double time) throws DeviceException {
		detector.setCollectionTime(time);
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		dp = null;
		logger.trace("{}.setAttribute - Setting {} to '{}'", getName(), attributeName, value);
		restoreAttributeMap();
		logger.trace("{}.setAttribute - Attribute map restored", getName());
		if (descriptionLabel.equals(attributeName)) {
			logger.trace("{}.setAttribute - Setting {} - not using xml", getName(), descriptionLabel);
			description = (String) value;
		} else if (value != null) {
			attributeMap.put(attributeName, value);
			if (configuration != null) {
				logger.trace("{}.setAttribute - setting xml {} to {}", getName(), attributeName, value);
				configuration.setProperty(attributeName, value);
			}
		} else if (attributeMap.containsKey(attributeName)) {
			logger.trace("{}.setAttribute - value is null, removing {} from attributes", getName(), attributeName);
			attributeMap.remove(attributeName);
			if (configuration != null) {
				logger.trace("{} - value is null, removing from xml configuration", getName());
				configuration.clearProperty(attributeName);
			}
		}
		logger.trace("{}.setAttribute - After set {} is {} in map, {} in configuration", getName(),
				attributeName,
				attributeMap.getOrDefault(attributeName, null),
				configuration == null ? "n/a" : configuration.getProperty(attributeName));
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		logger.trace("{}.getAttribute - getting {}", getName(), attributeName);
		restoreAttributeMap();
		if (descriptionLabel.equals(attributeName)) {
			return description;
		} else if (attributeMap.containsKey(attributeName)) {
			Object value = attributeMap.get(attributeName);
			logger.trace("{}.getAttribute - in map returning {}", getName(), value);
			logger.trace("{}.getAttribute - not using value in xml ({})",
					getName(),
					configuration == null ? "n/a" : configuration.getProperty(attributeName));
			return value;
		}
		logger.trace("{}.getAttribute - {} not present, returning null", getName(), attributeName);
		return null;
	}

	@Override
	public void writeout(int frames, NXDetectorData dataTree) throws DeviceException {
		int[] datadims = getDataDimensions();
		NexusGroupData ngd;
		datadims = ArrayUtils.add(datadims, 0, frames);

		Object data = detector.readout();
		if (data instanceof NexusGroupData) {
			ngd = (NexusGroupData) data;
		} else if (data instanceof double[]) {
			ngd = new NexusGroupData(datadims, (double[]) data);
		} else if (data instanceof float[]) {
			ngd = new NexusGroupData(datadims, (float[]) data);
		} else if (data instanceof int[]) {
			ngd = new NexusGroupData(datadims, (int[]) data);
		} else if (data instanceof short[]) {
			ngd = new NexusGroupData(datadims, (short[]) data);
		} else if (data instanceof byte[]) {
			ngd = new NexusGroupData(datadims, (byte[]) data);
		} else {
			throw new DeviceException("Detector readout type not supported: " + data);
		}
		ngd.isDetectorEntryData = true;
		dataTree.addData(getTreeName(), ngd, "counts", 1, getInterpretation());
		addMetadata(dataTree);
	}

	protected void addMetadata(NXDetectorData nxdata) throws DeviceException {
		NexusGroupData ngd;
		INexusTree detTree = nxdata.getDetTree(getTreeName());

		if (getName() != null) {
			ngd = new NexusGroupData(getName());
			ngd.isDetectorEntryData = false;

			NexusTreeNode type_node = new NexusTreeNode("name", NexusExtractor.SDSClassName, null, ngd);
			type_node.setIsPointDependent(false);

			detTree.addChildNode(type_node);
		}

		if (getDetectorType() != null) {
			ngd = new NexusGroupData(getDetectorType());
			ngd.isDetectorEntryData = false;

			NexusTreeNode type_node = new NexusTreeNode("sas_type", NexusExtractor.SDSClassName, null, ngd);
			type_node.setIsPointDependent(false);

			detTree.addChildNode(type_node);
		}

		if (description != null) {
			ngd = new NexusGroupData(description);
			ngd.isDetectorEntryData = false;

			NexusTreeNode type_node = new NexusTreeNode(descriptionLabel, NexusExtractor.SDSClassName, null, ngd);
			type_node.setIsPointDependent(false);

			detTree.addChildNode(type_node);
		}

//		String calibration = (String) getAttribute("calibrationFile");
//		if (calibration != null && !calibration.isEmpty()) {
//			linkCalibrationFile(nxdata, calibration);
//		}

		for (INcdMetaProvider meta : metaProviders) {
			try {
				meta.writeout(nxdata, getTreeName());
			} catch (Exception e) {
				//Catch everything so that metadata doesn't stop file being written
				logger.error("Could not write metadata from {}", meta.getName(), e);
			}
		}

		if (getSerialNumber() != null) {
			ngd = new NexusGroupData(getSerialNumber());
			ngd.isDetectorEntryData = false;

			NexusTreeNode serialNode = new NexusTreeNode(SERIAL_NUMBER_LABEL, NexusExtractor.SDSClassName, null, ngd);
			serialNode.setIsPointDependent(false);

			detTree.addChildNode(serialNode);
		}

		if (attributeMap.containsKey(SCALING_FACTOR)) {
			try {
				ngd = new NexusGroupData((Double) attributeMap.get(SCALING_FACTOR));
				ngd.isDetectorEntryData = true;

				NexusTreeNode type_node = new NexusTreeNode(SCALING_FACTOR, NexusExtractor.SDSClassName, null, ngd);
				type_node.setIsPointDependent(false);

				type_node.addChildNode(new NexusTreeNode("units", NexusExtractor.AttrClassName, type_node, null));

				detTree.addChildNode(type_node);
			} catch (Exception e) {
				logger.warn("{} - Error writing scaling factor", getName(), e);
			}
		}

	}

	@Override
	public double getPixelSize() throws DeviceException {
		return pixelSize;
	}

	@SuppressWarnings("unused") // subclasses require the throws
	public void setPixelSize(double pixelSize) throws DeviceException{
		this.pixelSize = pixelSize;
	}

	public Dataset getMask() {
		return mask;
	}

	public void setMask(DoubleDataset mask) {
		try {
			if (mask == null || mask.getShape() == getDataDimensions()) {
				this.mask = mask;
				return;
			}
		} catch (Exception e) {
			//
		}
		logger.error("{} - cannot set mask due to dimensions problem", getName());
	}

	@Override
	public void atScanStart() throws DeviceException {
	}

	@Override
	public void atScanEnd() throws DeviceException {
	}

	@Override
	public void setTimer(Timer timer) {
	}

	public String getInterpretation() {
		return interpretation;
	}

	public void setInterpretation(String interpretation) {
		this.interpretation = interpretation;
	}

	@Override
	public DetectorProperties getDetectorProperties() throws DeviceException {
		if (dp == null) {
			if (getPixelSize() == 0.0 || getAttribute("distance") == null || getAttribute("beam_center_x") == null || getAttribute("beam_center_y") == null) {
				dp = new DetectorProperties();
			} else {
				dp = new DetectorProperties(((Double) getAttribute("distance"))*1000, ((Double) getAttribute("beam_center_x"))*getPixelSize()*1000, ((Double) getAttribute("beam_center_y"))*getPixelSize()*1000, getDataDimensions()[0], getDataDimensions()[1], getPixelSize()*1000, getPixelSize()*1000);
			}
		}
		return dp;
	}

	public void setMetaProviders(Collection<INcdMetaProvider> meta) {
		this.metaProviders = meta;
	}

	@Override
	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serial) {
		serialNumber = serial;
	}
}