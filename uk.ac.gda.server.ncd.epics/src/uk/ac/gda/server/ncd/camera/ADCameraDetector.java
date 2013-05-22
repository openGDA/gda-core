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

package uk.ac.gda.server.ncd.camera;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.IPVProvider;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDOverlay;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.gda.server.ncd.subdetector.LastImageProvider;

/**
 * we assume the camera is in constant acquire mode, so we can just grab an image from the array plugin
 */
public class ADCameraDetector extends DetectorBase implements InitializingBean, NexusDetector, LastImageProvider {

	static final Logger logger = LoggerFactory.getLogger(ADCameraDetector.class);

	// Values internal to the object for Channel Access
	private final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();
	
	// Variables to hold the spring settings
	private ADBase areaDetector;
	private String basePVName = null;
	private IPVProvider pvProvider;
	private NDArray array;
	private NDOverlay draw;

	protected Map<String, Object> attributeMap = new HashMap<String, Object>();

	@Override
	public void configure() throws FactoryException {
		try {
			array.getPluginBase().enableCallbacks();
		} catch (Exception e) {
//			throw new FactoryException("enabling array callbacks", e);
		}
	}
	
	// Getters and Setters for Spring
	public ADBase getAreaDetector() {
		return areaDetector;
	}

	public void setAreaDetector(ADBase areaDetector) {
		this.areaDetector = areaDetector;
	}
	
	public NDOverlay getDraw() {
		return draw;
	}

	public void setDraw(NDOverlay draw) {
		this.draw = draw;
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	public void resetAll() throws Exception {
		areaDetector.reset();
		array.reset();
	}

	public int getTriggerMode() throws Exception {
		return areaDetector.getTriggerMode();
	}
	
	public void setTriggerMode(int mode) throws Exception {
		areaDetector.setTriggerMode((short) mode);
	}

	public void setExposures(int numberOfExposures) throws Exception {
		areaDetector.setNumExposures(numberOfExposures);
	}

	public int getExposures() throws Exception {
		return areaDetector.getNumExposures();
	}

	public void setNumImages(int numberOfExposures) throws Exception {
		areaDetector.setNumImages(numberOfExposures);
	}

	public int getNumImages() throws Exception {
		return areaDetector.getNumImages();
	}
	
	public void setAcquireTime(double time) throws Exception {
		areaDetector.setAcquireTime(time);
	}
	
	public double getAcquireTime() throws Exception {
		return areaDetector.getAcquireTime();
	}

	private Channel getChannel(String pvPostFix) throws Exception {
		String fullPvName;
		if (pvProvider != null) {
			fullPvName = pvProvider.getPV(pvPostFix);
		} else {
			fullPvName = basePVName + pvPostFix;
		}
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			channel = EPICS_CONTROLLER.createChannel(fullPvName);
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	public IPVProvider getPvProvider() {
		return pvProvider;
	}

	public void setPvProvider(IPVProvider pvProvider) {
		this.pvProvider = pvProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null && pvProvider == null) {
			throw new IllegalArgumentException("'basePVName' or pvProvider needs to be declared");
		}

		if (areaDetector == null) {
			throw new IllegalArgumentException("'areaDetector' needs to be declared");
		}
		
		if (array == null) {
			throw new IllegalArgumentException("'array' needs to be declared");
		}
	}

	public void acquire() throws Exception {
		areaDetector.startAcquiring();
	}

	public short getDetectorState() throws Exception {
		return areaDetector.getDetectorState_RBV();
	}

	public int getAcquireState() throws Exception {
		return areaDetector.getAcquireState();
	}

	public int getArrayCounter() throws Exception {
		return areaDetector.getArrayCounter_RBV();
	}

	public void setAcquirePeriod(double acquirePeriod) throws Exception {
		areaDetector.setAcquirePeriod(acquirePeriod);
	}

	public void stopAcquiring() throws Exception {
		areaDetector.stopAcquiring();
	}

	public void setImageMode(int imageMode) throws Exception {
		areaDetector.setImageMode((short) imageMode);
	}

	public AreaDetectorROI getAreaDetectorROI() throws Exception {
		return areaDetector.getAreaDetectorROI();
	}

	public int getImageMode() throws Exception {
		return areaDetector.getImageMode_RBV();
	}

	public int getCaptureState() throws Exception {
		return areaDetector.getAcquireState();
	}

	public float[] getCurrentArray() throws Exception {
		return array.getFloatArrayData();
	}

	public void setArray(NDArray array) {
		this.array = array;
	}

	public NDArray getArray() {
		return array;
	}

	public void resetCounters() throws Exception {
		areaDetector.setArrayCounter(0);
	}

	@Override
	public void collectData() throws DeviceException {
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "GigE Camera";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "GigE Camera";
	}

	@Override
	public int getStatus() throws DeviceException {
		return Detector.IDLE;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		NXDetectorData ndd = new NXDetectorData();
		IntegerDataset image = readLastImage();
		ndd.addData(getName(), image.getShape(), NexusFile.NX_INT32, image.getData(), null, null);
		try {
			ndd.setDoubleVals(new Double[] {new Double(array.getPluginBase().getArrayCounter_RBV())});
		} catch (Exception e) {
			throw new DeviceException(e);
		}
		addMetadata(ndd);
		return ndd;
	}

	protected void addMetadata(NXDetectorData nxdata) throws DeviceException {
		NexusGroupData ngd;
		INexusTree detTree = nxdata.getDetTree(getName());

		// length metadata
		for (String label : new String[] { "x_pixel_size", "y_pixel_size" }) {
			if (attributeMap.containsKey(label)) {
				try {
					ngd = new NexusGroupData(new int[] { 1 }, NexusFile.NX_FLOAT64,
							new double[] { (Double) attributeMap.get(label) });
					ngd.isDetectorEntryData = false;

					NexusTreeNode type_node = new NexusTreeNode(label, NexusExtractor.SDSClassName, null, ngd);
					type_node.setIsPointDependent(false);

					type_node.addChildNode(new NexusTreeNode("units", NexusExtractor.AttrClassName, type_node,  new NexusGroupData("m")));

					detTree.addChildNode(type_node);
				} catch (Exception e) {
					logger.warn("Error writing metadata " + label + ": ", e);
				}
			}
		}
		
		// unitless metadata
		for (String label : new String[] { "beam_center_x", "beam_center_y" }) {
			if (attributeMap.containsKey(label)) {
				try {
					ngd = new NexusGroupData(new int[] { 1 }, NexusFile.NX_FLOAT64,
							new double[] { (Double) attributeMap.get(label) });
					ngd.isDetectorEntryData = false;

					NexusTreeNode type_node = new NexusTreeNode(label, NexusExtractor.SDSClassName, null, ngd);
					type_node.setIsPointDependent(false);

					type_node.addChildNode(new NexusTreeNode("units", NexusExtractor.AttrClassName, type_node, null));

					detTree.addChildNode(type_node);
				} catch (Exception e) {
					logger.warn("Error writing metadata " + label + ": ", e);
				}
			}
		}
	}
	
	@Override
	public IntegerDataset readLastImage() throws DeviceException {
		try {
			int[] arrayData = cagetArrayUnsigned();
			int[] shape = getDataDimensions();
			if (arrayData.length > shape[0]*shape[1])
				arrayData = Arrays.copyOf(arrayData, shape[0] * shape[1]);
			IntegerDataset image = new IntegerDataset(arrayData, shape);
			return image;
		} catch (Exception e) {
			throw new DeviceException("error reading image", e);
		}
	}
	
	@Override
	public int[] getDataDimensions() throws DeviceException {
		try {
			return new int[] { array.getPluginBase().getArraySize1_RBV(), array.getPluginBase().getArraySize0_RBV() };
		} catch (Exception e) {
			throw new DeviceException("error getting camera dimensions", e);
		}
	}
	
	public int[] cagetArrayUnsigned() throws Exception {
		byte[] values = array.getByteArrayData();;
		int[] uvalues = new int[values.length];
		for(int i=0; i<values.length; i++){
			uvalues[i] = values[i]&0xff;
		}
		return uvalues;
	}
	
	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		if (value != null) {
			attributeMap.put(attributeName, value);
		} else if (attributeMap.containsKey(attributeName)) {
			attributeMap.remove(attributeName);
		}
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if (attributeMap.containsKey(attributeName)) {
			return attributeMap.get(attributeName);
		}
		return null;
	}
}