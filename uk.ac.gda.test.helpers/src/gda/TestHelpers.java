/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda;

import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ShapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.nexus.INeXusInfoWriteable;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.factory.ConfigurableBase;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.FindableConfigurableBase;
import gda.jython.InterfaceProvider;
import gda.jython.MockJythonServerFacade;
import gda.observable.IObserver;
import gda.util.TestUtils;

/**
 * Collection of utility functions to assist testing.
 */
public class TestHelpers {

	private TestHelpers() {
		// prevent instantiation
	}

	/**
	 * Sets up of environment for the a test Set property so that output is to Nexus format file
	 * Uses MockJythonServerFacade and MockJythonServer to configure InterfaceProvider
	 *
	 * @param testClass
	 *            e.g. gda.data.nexus.ScanToNexusTest
	 * @param nameOfTest
	 *            name of test method which the testClass e.g. testCreateScanFile
	 * @param makedir
	 *            if true the scratch dir is deleted and constructed
	 * @return The directory into which output will be sent
	 * @throws Exception
	 *             if setup fails
	 */
	public static String setUpTest(Class<?> testClass, String nameOfTest, boolean makedir) throws Exception {

		String testScratchDirectoryName = TestUtils.setUpTest(testClass, nameOfTest, makedir);

		MockJythonServerFacade mockJythonServerFacade = new MockJythonServerFacade();
		InterfaceProvider.setCommandRunnerForTesting(mockJythonServerFacade);
		InterfaceProvider.setCurrentScanControllerForTesting(mockJythonServerFacade);
		InterfaceProvider.setTerminalPrinterForTesting(mockJythonServerFacade);
		InterfaceProvider.setScanStatusHolderForTesting(mockJythonServerFacade);
		InterfaceProvider.setJythonNamespaceForTesting(mockJythonServerFacade);
		InterfaceProvider.setAuthorisationHolderForTesting(mockJythonServerFacade);
		InterfaceProvider.setScriptControllerForTesting(mockJythonServerFacade);
		InterfaceProvider.setPanicStopForTesting(mockJythonServerFacade);
		InterfaceProvider.setCurrentScanInformationHolderForTesting(mockJythonServerFacade);
		InterfaceProvider.setJythonServerNotiferForTesting(mockJythonServerFacade);
		InterfaceProvider.setDefaultScannableProviderForTesting(mockJythonServerFacade);
		InterfaceProvider.setScanDataPointProviderForTesting(mockJythonServerFacade);
		InterfaceProvider.setBatonStateProviderForTesting(mockJythonServerFacade);
		InterfaceProvider.setJSFObserverForTesting(mockJythonServerFacade);
		LocalProperties.set(LocalProperties.GDA_DATA, testScratchDirectoryName + "/Data");
		LocalProperties.set(LocalProperties.GDA_VAR_DIR, testScratchDirectoryName + "/Data");
		LocalProperties.set(LocalProperties.GDA_LOGS_DIR, testScratchDirectoryName + "/Data");
		LocalProperties.set(LocalProperties.GDA_DATAWRITER_DIR, testScratchDirectoryName + "/Data");
		LocalProperties.set("gda.data.scan.datawriter.setTime0", "True");
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "NexusDataWriter");
		LocalProperties.set("gda.nexus.instrumentApi", "True");

		return testScratchDirectoryName;
	}

	public static Scannable createTestScannable(String name, Object position, String[] extraNames, String[] inputNames,
			int level, String[] outputFormat, String[] units) {
		return new SimpleScannable(name, position, extraNames, inputNames, level, outputFormat, units);
	}

	public static Scannable createTestScannableGaussianXY(String name, Double xPosition, String xName, String yName,
			Double center, Double width, Double magnitude, Double pcNoise, int level, String outputFormatx,
			String outputFormaty, String unitsx, String unitsy) {
		return new GaussianScannableXY(name, xPosition, xName, yName, center, width, magnitude, pcNoise, level,
				outputFormatx, outputFormaty, unitsx, unitsy);
	}

	public static Scannable createTestScannableSine(String name, Double xPosition, Double magnitude, Double pcNoise,
			String xName, String yName, int level, String outputFormatx, String outputFormaty, String unitsx,
			String unitsy) {
		return new SineScannable(name, xPosition, magnitude, pcNoise, xName, yName, level, outputFormatx,
				outputFormaty, unitsx, unitsy);
	}

	public static Detector createTestDetector(String name, Object position, String[] extraNames, String[] inputNames,
			int level, String[] outputFormat, NexusGroupData data, String filename, String description,
			String detectorID, String detectorType) {
		SimpleDetector det = new SimpleDetector(name, position, extraNames, inputNames, level, outputFormat, data,
				filename, description, detectorID, detectorType);
		return det;
	}

	public static Detector createTestCounterTimer(String name, Object position, String[] extraNames,
			String[] inputNames, int level, String[] outputFormat, NexusGroupData data, String filename,
			String description, String detectorID, String detectorType, String[] channelNames) {
		return new SimpleCounterTimer(name, position, extraNames, inputNames, level, outputFormat, data, filename,
				description, detectorID, detectorType, channelNames);
	}

	public static Detector createTestFileDetector(String name, int level, String fileNameFormat, int[] dim,
			String description, String detectorID, String detectorType) {
		return new FileDetector(name, null, new String[] { name }, new String[0], level, new String[] { "%s" },
				fileNameFormat, dim, description, detectorID, detectorType);
	}

	public static NexusDetector createTestISubDetector(String name, Object position, String[] extraNames,
			String[] inputNames, int level, String[] outputFormat, NexusGroupData data, String filename,
			String description, String detectorID, String detectorType) {
		return new SimpleSubDetector(name, position, extraNames, inputNames, level, outputFormat, data, filename,
				description, detectorID, detectorType);
	}

	public static NexusGroupData createTestNexusGroupData(Class<? extends Dataset> clazz, int[] shape,
			boolean useSuperToString) {
		int size = ShapeUtils.calcSize(shape);
		Dataset dataset = DatasetFactory.createRange(clazz, size);
		dataset.setShape(shape);
		NexusGroupData ngd = new Data(dataset, useSuperToString);
		ngd.isDetectorEntryData = true;
		return ngd;
	}

	/**
	 * @param name - Not used
	 * @return Factory implementation that can be used for testing - simply add findables and add to Finder instance
	 * @deprecated replace with {@link #createTestFactory()} i.e. no name specified
	 */
	@Deprecated
	public static Factory createTestFactory(@SuppressWarnings("unused") String name) {
		return new TestFactory();
	}

	/**
	 * @return Factory implementation that can be used for testing - simply add findables and add to Finder instance
	 */
	public static Factory createTestFactory() {
		return new TestFactory();
	}
}

class SimpleScannable extends FindableConfigurableBase implements Scannable, INeXusInfoWriteable {

	private static final Logger logger = LoggerFactory.getLogger(SimpleScannable.class);

	private Object position;
	private String[] extraNames;
	private String[] inputNames;
	private int level;
	private String[] outputFormat;
	private String[] units;

	SimpleScannable(String name, Object position, String[] extraNames, String[] inputNames, int level,
			String[] outputFormat, String[] units) {
		super.setName(name);
		this.position = position;
		this.extraNames = extraNames;
		this.inputNames = inputNames;
		this.level = level;
		this.outputFormat = outputFormat;
		this.units = units;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		this.position = position;
	}

	@Override
	public void atPointEnd() throws DeviceException {
	}

	@Override
	public void atPointStart() throws DeviceException {
	}

	@Override
	public void atScanEnd() throws DeviceException {
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
	}

	@Override
	public void atScanLineStart() throws DeviceException {
	}

	@Override
	public void atScanStart() throws DeviceException {
	}

	@Override
	public void atLevelMoveStart() {
	}

	@Override
	public void atLevelStart() {
	}

	@Override
	public void atLevelEnd() {
	}

	@Override
	public String[] getExtraNames() {
		return extraNames;
	}

	@Override
	public String[] getInputNames() {
		return inputNames;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public String[] getOutputFormat() {
		return outputFormat;
	}

	@Override
	public Object getPosition() throws DeviceException {
		return position;
	}

	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		return positionToTest.equals(getPosition());
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public String checkPositionValid(Object position) {
		return null;
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		asynchronousMoveTo(position);
	}

	@Override
	public void setExtraNames(String[] names) {
		this.extraNames = names;
	}

	@Override
	public void setInputNames(String[] names) {
		inputNames = names;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public void setOutputFormat(String[] names) {
		outputFormat = names;
	}

	@Override
	public void stop() throws DeviceException {
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
	}

	@Override
	public void close() throws DeviceException {
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		return null;
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return 0;
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
	}

	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException {
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
	}

	@Override
	public void deleteIObservers() {
	}

	@Override
	public String toString() {
		return "SimpleScannable:" + getName();
	}

	@Override
	public void writeNeXusInformation(NexusFile file, Node node) throws NexusException {
		try {
			if (units != null && units.length > 0) {
				Dataset ad = DatasetFactory.createFromObject(units[0]);
				ad.setName(NexusConstants.UNITS);
				Attribute a = file.createAttribute(ad);
				file.addAttribute(node, a);
			}
		} catch (NexusException e) {
			logger.debug("Problem writing additional info to NeXus file.");
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
	}

	@Override
	public String toFormattedString() {
		return getName() + " : " + position.toString();
	}
}

class GaussianScannableXY extends SimpleScannable {
	Double center, width, magnitude, pcNoise;

	GaussianScannableXY(String name, Double xPosition, String xName, String yName, Double center, Double width,
			Double magnitude, Double pcNoise, int level, String outputFormatx, String outputFormaty, String unitsx,
			String unitsy) {
		super(name, xPosition, new String[] { yName }, new String[] { xName }, level, new String[] { outputFormatx,
				outputFormaty }, new String[] { unitsx, unitsy });
		this.center = center;
		this.width = width;
		this.magnitude = magnitude;
		this.pcNoise = pcNoise;
	}

	@Override
	public Object getPosition() throws DeviceException {
		// we assume the position is a double - it is only for testing
		Double xval = (Double) super.getPosition();
		Double noise = pcNoise;
		return new Double[] {
				xval,
				magnitude * (1 + (0.01 * noise * (Math.random() - 0.5)))
						* Math.exp(-((xval - center) * (xval - center)) / width) };
	}

	@Override
	public String toString() {
		return "GaussianScannableXY:" + getName();
	}
}

class SineScannable extends SimpleScannable {
	Double magnitude;
	Double noise;

	public SineScannable(String name, Double position, Double magnitude, Double pcNoise, String xName, String yName,
			int level, String outputFormatx, String outputFormaty, String unitsx, String unitsy) {
		super(name, position, new String[] { yName }, new String[] { xName }, level, new String[] { outputFormatx,
				outputFormaty }, new String[] { unitsx, unitsy });
		this.magnitude = magnitude;
		this.noise = pcNoise;
	}

	@Override
	public Object getPosition() throws DeviceException {
		Double xval = (Double) super.getPosition();
		Double noiseVal = 0.01 * noise * magnitude * Math.random();
		return new Double[] { xval, magnitude * Math.sin(xval) + noiseVal };
	}

	@Override
	public String toString() {
		return "SineScannable:" + getName();
	}
}

class SimpleDetector extends ConfigurableBase implements Detector {
	String name;
	Object position;
	String[] extraNames;
	String[] inputNames;
	int level;
	String[] outputFormat;
	String description;
	String detectorID;
	String detectorType;
	NexusGroupData data;
	String filename;

	SimpleDetector(String name, Object position, String[] extraNames, String[] inputNames, int level,
			String[] outputFormat, NexusGroupData data, String filename, String description, String detectorID,
			String detectorType) {
		this.name = name;
		this.position = position;

		// extraNames should be the length of the dimensions if defined
		if (data != null) {
			int lengthOfData = 1;
			for (int i = 0; i < data.dimensions.length; i++) {
				lengthOfData *= data.dimensions[i];
			}

			if (extraNames.length != lengthOfData) {

				if (lengthOfData == 1) {
					this.extraNames = new String[] { name };
				} else {

					this.extraNames = new String[lengthOfData];

					for (int i = 0; i < lengthOfData; i++) {
						this.extraNames[i] = name + "_" + Integer.toString(i);
					}
				}
			}
		} else {
			this.extraNames = extraNames;
		}

		this.inputNames = inputNames;
		this.level = level;
		this.outputFormat = outputFormat;
		this.data = data;
		this.description = description;
		this.detectorID = detectorID;
		this.detectorType = detectorType;
		this.filename = filename;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		this.position = position;

	}

	@Override
	public void atPointEnd() throws DeviceException {
	}

	@Override
	public void atPointStart() throws DeviceException {
	}

	@Override
	public void atScanEnd() throws DeviceException {
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
	}

	@Override
	public void atScanLineStart() throws DeviceException {
	}

	@Override
	public void atScanStart() throws DeviceException {
	}

	@Override
	public String[] getExtraNames() {
		return extraNames;
	}

	@Override
	public String[] getInputNames() {
		return inputNames;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public String[] getOutputFormat() {
		return outputFormat;
	}

	@Override
	public Object getPosition() throws DeviceException {
		return position;
	}

	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		return positionToTest.equals(getPosition());
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public String checkPositionValid(Object position) {
		return null;
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		asynchronousMoveTo(position);
	}

	@Override
	public void setExtraNames(String[] names) {
		this.extraNames = names;
	}

	@Override
	public void setInputNames(String[] names) {
		inputNames = names;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public void setOutputFormat(String[] names) {
		outputFormat = names;
	}

	@Override
	public void stop() throws DeviceException {
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
	}

	@Override
	public void close() throws DeviceException {
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		return null;
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return 0;
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
	}

	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException {
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
	}

	@Override
	public void deleteIObservers() {
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void collectData() throws DeviceException {
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return filename != null;
	}

	@Override
	public void endCollection() throws DeviceException {
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return data.dimensions;
	}

	@Override
	public String getDescription() throws DeviceException {
		return description;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return detectorID;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return detectorType;
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public void prepareForCollection() throws DeviceException {
	}

	@Override
	public Object readout() throws DeviceException {
		return filename != null ? filename : data.getBuffer();
	}

	@Override
	public void setCollectionTime(double time) throws DeviceException {
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		return 0.;
	}

	@Override
	public String toString() {
		return "SimpleDetector:" + getName();
	}

	@Override
	public void atLevelMoveStart() {

	}

	@Override
	public void atLevelStart() {
	}

	@Override
	public void atLevelEnd() {
	}

	@Override
	public void atCommandFailure() throws DeviceException {
	}

	@Override
	public String toFormattedString() {
		return name + " : " + position.toString();
	}
}

class FileDetector extends SimpleDetector {
	private final String filenameFormatString;
	private int fileNumber = 0;
	private final int[] dim;

	FileDetector(String name, Object position, String[] extraNames, String[] inputNames, int level,
			String[] outputFormat, String filenameFormatString, int[] dim, String description, String detectorID,
			String detectorType) {
		super(name, position, extraNames, inputNames, level, outputFormat, null, "", description, detectorID,
				detectorType);
		this.filenameFormatString = filenameFormatString;
		this.dim = dim;
	}

	@Override
	public Object readout() throws DeviceException {
		fileNumber += 1;
		return String.format(filenameFormatString, fileNumber);
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return dim;
	}
}

class SimpleCounterTimer extends SimpleDetector implements Detector {

	String[] channelNames;

	SimpleCounterTimer(String name, Object position, String[] extraNames, String[] inputNames, int level,
			String[] outputFormat, NexusGroupData data, String filename, String description, String detectorID,
			String detectorType, String[] channelNames) {
		super(name, position, extraNames, inputNames, level, outputFormat, data, filename, description, detectorID,
				detectorType);
		this.channelNames = channelNames;
		this.extraNames = channelNames;
	}

	// @Override
	// public double[] readChans() throws DeviceException {
	// return (double[]) data.getBuffer();
	// }

}

class SimpleSubDetector extends SimpleDetector implements NexusDetector {
	SimpleSubDetector(String name, Object position, String[] extraNames, String[] inputNames, int level,
			String[] outputFormat, NexusGroupData data, String filename, String description, String detectorID,
			String detectorType) {
		super(name, position, extraNames, inputNames, level, outputFormat, data, filename, description, detectorID,
				detectorType);
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		NXDetectorData nxdetData = new TestNXDetectorData();
		if (filename != null) {
			nxdetData.addData(name, new NexusGroupData(new int[19]));
			nxdetData.addFileName(name, filename);
			INexusTree detTree = nxdetData.getDetTree(name);
			{
				NexusTreeNode data = new NexusTreeNode("description", NexusExtractor.SDSClassName, null,
						new NexusGroupData("Generic GDA Detector - External Files"));
				data.setIsPointDependent(false);
				detTree.addChildNode(data);
			}
			{
				NexusTreeNode data = new NexusTreeNode("type", NexusExtractor.SDSClassName, null, new NexusGroupData(
						"Detector"));
				data.setIsPointDependent(false);
				detTree.addChildNode(data);
			}

		} else {
			nxdetData.addData(name, data, null, null);
		}
		return nxdetData;
	}
}

class TestNXDetectorData extends NXDetectorData {
	@Override
	public String toString() {
		NexusGroupData data = getNexusTree().getChildNode("data", NexusExtractor.SDSClassName).getData();
		return data.toString();
	}

}

class Data extends NexusGroupData {
	boolean useSuperToString;

	Data(Dataset dataset, boolean useSuperToString) {
		super(dataset);
		this.useSuperToString = useSuperToString;
	}

	@Override
	public String toString() {
		if (useSuperToString) {
			return super.toString();
		}
		Serializable s = getBuffer();
		if (s instanceof byte[]) {
			String sb = "";
			for (byte b : (byte[]) s) {
				sb += Byte.toString(b) + ",";
			}
			return sb.toString();
		}
		return s.toString();
	}
}

class TestFactory implements Factory {
	private final Map<String, Findable> findables = new HashMap<>();

	@Override
	public void addFindable(Findable findable) {
		findables.put(findable.getName(), findable);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Findable> T getFindable(String name) throws FactoryException {
		return (T) findables.get(name);
	}

	@Override
	public List<String> getFindableNames() {
		return new ArrayList<>(findables.keySet());
	}

	@Override
	public List<Findable> getFindables() {
		return new ArrayList<>(findables.values());
	}

	@SuppressWarnings("unchecked") // This is safe because we have just ensured the object is an instance of T
	@Override
	public <T extends Findable> Map<String, T> getFindablesOfType(Class<T> clazz) {
		return findables.entrySet().stream()
		.filter(entry -> clazz.isInstance(entry.getValue()))
		.collect(toMap(Entry::getKey, entry -> (T) entry.getValue()));
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	@Override
	public void configure() throws FactoryException {
		throw new FactoryException("TestFactory does not support configuration");
	}

	@Override
	public void reconfigure() throws FactoryException {
		throw new FactoryException("TestFactory does not support reconfiguration");
	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public boolean isConfigureAtStartup() {
		return true;
	}
}
