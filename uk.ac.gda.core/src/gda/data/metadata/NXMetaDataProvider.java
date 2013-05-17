/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.data.metadata;

import gda.data.PlottableDetectorData;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.Findable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import org.nexusformat.NexusFile;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyList;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class NXMetaDataProvider implements NexusTreeProvider, Map<String, Object>, Findable {

	private String name;

	private static final Logger logger = LoggerFactory.getLogger(NXMetaDataProvider.class);

	public NXMetaDataProvider() {
		super();
		reset();
	}

	@Override
	public INexusTree getNexusTree() {
		INexusTree nexusTree = new NexusTreeNode("before_scan", NexusExtractor.NXCollectionClassName, null);
		for (Entry<String, Object> e : metaTextualMap.entrySet()) {
			INexusTree childNode = createChildNodeForTextualMetaEntry(e, nexusTree);
			if (childNode != null) {
				nexusTree.addChildNode(childNode);
			} else {
				logger.debug("Nexus tree child node is null for " + e.getKey());
			}
		}

		for (Scannable scn : metaScannables) {
			System.out.println("scannable = " + scn.getName());
			try {
				Map<String, Object> scannableMap = createMetaScannableMap(scn);
				INexusTree childNode = createChildNodeForScannableMetaEntry_TEST(scn, nexusTree, scannableMap); //TODO Change name
				if (childNode != null) {
					nexusTree.addChildNode(childNode);
				} else {
					logger.debug("Nexus tree child node is null for " + scn.getName());
				}
			} catch (DeviceException e1) {
				logger.error("Error creaating metadata for scannable" + scn.getName(), e1);
			}

		}
		return nexusTree;
	}

	Map<String, Object> metaTextualMap;

	public void reset() {
		this.metaTextualMap = new HashMap<String, Object>();
	}

	public String listAsString(String fmt, String delimiter) {
		// TODO Auto-generated method stub
		String total = "";
		for (Entry<String, Object> e : metaTextualMap.entrySet()) {
			total += "," + e.getKey() + ":" + e.getValue();
		}
		// String x = concatenateKeyAndValueForListAsString("%s:%x", ",");
		String x = concatenateKeyAndValueForListAsString(fmt, delimiter);
		System.out.println("***x = " + x);
		total = x;
		return total;
	}

	public String concatenateKeyAndValueForListAsString(String fmt, String delimiter) {
		String concatenated = "";
		int sanityCheck = StringUtils.countOccurrencesOf(fmt, "%s");
		System.out.println("***sanityCheck = " + sanityCheck);
		if (sanityCheck == 2) {
			for (Entry<String, Object> e : metaTextualMap.entrySet()) {
				// concatenated += "," + e.getKey() + ":" + e.getValue();
				concatenated += String.format(fmt, e.getKey(), e.getValue());
				concatenated += delimiter;
			}
			// remove the unnecessary last delimiter
			concatenated = concatenated.substring(0, concatenated.length() - 1);
		} else {
			String defaultFmt = "%s:%s";
			String defaultDelimiter = ",";
			logger.warn("Bad input format: " + "\"" + fmt + "\"" + " is " + "replaced by default format: " + "\""
					+ defaultFmt + "\"");
			concatenated = concatenateKeyAndValueForListAsString(defaultFmt, defaultDelimiter);
		}
		// remove the unnecessary last delimiter, if present
		// if ( concatenated.lastIndexOf(delimiter)==(concatenated.length()-1)){
		// concatenated = concatenated.substring(0, concatenated.length()-1);
		// }
		return concatenated;
	}

	public void setMetaTexts(Map<String, String> metaTexts) {
		for (String key : metaTexts.keySet()) {
			add(key, metaTexts.get(key));
		}
	}

	public Map<String, Object> getMetaTexts() {
		Map<String, Object> outMap = new HashMap<String, Object>(metaTextualMap);
		return outMap;
	}

	public void add(String key, Object value, String units) {
		Pair<Object, String> valueWithUnits = new Pair<Object, String>(value, units);
		put(key, valueWithUnits);
	}

	@Override
	public int size() {
		return metaTextualMap.size();
	}

	@Override
	public boolean isEmpty() {
		return metaTextualMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return metaTextualMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return metaTextualMap.containsValue(value);
	}

	@Override
	public Object get(Object key) {
		return metaTextualMap.get(key);
	}

	@Override
	public Object put(String key, Object value) {
		return metaTextualMap.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		String msg = "remove key = " + key;
		logger.debug(msg);
		System.out.println(msg);
		return metaTextualMap.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		metaTextualMap.putAll(m);
	}

	@Override
	public void clear() {
		metaTextualMap.clear();
	}

	@Override
	public Set<String> keySet() {
		return metaTextualMap.keySet();
	}

	@Override
	public Collection<Object> values() {
		return metaTextualMap.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return metaTextualMap.entrySet();
	}

	@Override
	public boolean equals(Object o) {
		return metaTextualMap.equals(o);
	}

	@Override
	public int hashCode() {
		return metaTextualMap.hashCode();
	}

	List<Scannable> metaScannables = new Vector<Scannable>();

	public void setMetaScannables(List<Scannable> metaScannables) {
		this.metaScannables.addAll(metaScannables);
	}

	public List<Scannable> getMetaScannables() {
		List<Scannable> outLst = new Vector<Scannable>(this.metaScannables);
		return outLst;
	}

	/*
	 * To be called by meata_ls command
	 */
	public NexusTreeStringDump list(boolean withValues) {
		return new NexusTreeStringDump(getNexusTree());
	}

	public void add(Object... args) {
		if (args[0] instanceof Scannable && args.length == 1) {
			add((Scannable) args[0]);
		} else if (args[0] instanceof String && args.length == 2) {
			add((String) args[0], args[1], null);
		} else if (args[0] instanceof String && args.length == 3 && args[2] instanceof String) {
			add((String) args[0], args[1], (String) args[2]);
		} else {
			throw new IllegalArgumentException("Invalid arguments");
		}
	}

	public void add(Scannable scannable) {
		logger.debug("add scannable = " + scannable.getName());
		System.out.println("***add scannable = " + scannable.getName());
		metaScannables.add(scannable);
	}

	public void remove(ScannableBase scannable) {
		logger.debug("***remove scannable = " + scannable.getName());
		System.out.println("***remove scannable = " + scannable.getName());
		metaScannables.remove(scannable);
	}

	public void createMetaScannableMap() throws DeviceException {
		for (Scannable scn : metaScannables) {
			List<ScannableMetaEntry> metas = new Vector<ScannableMetaEntry>();

			List<String> scnNames = new Vector<String>();

			int len = scn.getInputNames().length;
			String[] inputNames = scn.getInputNames();
			for (int i = 0; i < len; i++) {
				scnNames.add(inputNames[i]);
			}

			len = scn.getExtraNames().length;
			String[] extraNames = scn.getExtraNames();
			for (int i = 0; i < len; i++) {
				scnNames.add(extraNames[i]);
			}

			List<String> scnFormats = new Vector<String>();
			len = scn.getOutputFormat().length;
			String[] outFormats = scn.getOutputFormat();
			for (int i = 0; i < len; i++) {
				scnFormats.add(outFormats[i]);
			}

			String[] formattedCurrentPositionArray = ScannableUtils.getFormattedCurrentPositionArray(scn);
			System.out.println("formattedCurrentPositionArray.length = " + formattedCurrentPositionArray.length);
			for (int i = 0; i < formattedCurrentPositionArray.length; i++) {
				System.out.println(formattedCurrentPositionArray[i]);
			}

			List<String> scannableFieldNames = getScannableFieldNames(scn);
			for (int i = 0; i < scannableFieldNames.size(); i++) {
				System.out.println(">>> " + scannableFieldNames.get(i));
			}

			for (int i = 0; i < scannableFieldNames.size(); i++) {
				metas.add(new ScannableMetaEntry(scannableFieldNames.get(i), formattedCurrentPositionArray[i]));
			}

			for (ScannableMetaEntry e : metas) {
				System.out.println(">>>key = " + e.key);
				System.out.println(">>>val = " + e.value);
			}
		}
	}

	public Map<String, Object> createMetaScannableMap(Scannable scn) throws DeviceException {
		Map<String, Object> metaScannableMapObj = new HashMap<String, Object>();

		List<ScannableMetaEntryObj> metasObj = new Vector<ScannableMetaEntryObj>();

		Object scnPos = null;
		try {
			scnPos = scn.getPosition();
		} catch (PyException e) {
			throw new DeviceException("Error on calling getPosition for scannable " + scn.getName() + ":"
					+ e.toString());
		} catch (Exception e) {
			throw new DeviceException("Error on calling getPosition for scannable " + scn.getName() + ": "
					+ e.getMessage(), e);
		}

		Object[] elementalGetPosObjects = separateGetPositionOutputIntoElementalPosObjects(scnPos);

		List<String> scannableFieldNames = getScannableFieldNames(scn);

		for (int i = 0; i < scannableFieldNames.size(); i++) {
			metasObj.add(new ScannableMetaEntryObj(scannableFieldNames.get(i), elementalGetPosObjects[i]));
		}

		for (ScannableMetaEntryObj e : metasObj) {
			System.out.println(">>> keyObj = " + e.key);
			System.out.println(">>> valObj = " + e.value.toString());
			metaScannableMapObj.put(e.key, e.value);
		}

		return metaScannableMapObj;
	}

	class ScannableMetaEntry {
		public String key;
		public String value;

		public ScannableMetaEntry(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

	}

	class ScannableMetaEntryObj {
		public String key;
		public Object value;

		public ScannableMetaEntryObj(String key, Object value) {
			super();
			this.key = key;
			this.value = value;
		}

	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		if ((this.name == null || this.name.isEmpty())) {
			logger.warn("getName() called on NXMetaDataProvider when the name has not been set. This may cause problems in the system and should be fixed.");
		}
		return this.name;
	}

	public INexusTree getBeforeScanMetaData() {
		return getNexusTree();

	}

	public List<String> getScannableFieldNames(Scannable scannable) {
		Vector<String> fieldNames = new Vector<String>();

		// if detector the inputNames are not returned in ScanDataPoint so do not add
		String[] extraNames = scannable.getExtraNames();
		if (scannable instanceof Detector) {
			if (extraNames.length > 0) {
				fieldNames.addAll(Arrays.asList(extraNames));
			} else {
				fieldNames.add(scannable.getName());
			}
		} else {
			fieldNames.addAll(Arrays.asList(scannable.getInputNames()));
			fieldNames.addAll(Arrays.asList(extraNames));
		}
		return fieldNames;
	}

	public INexusTree createChildNodeForTextualMetaEntry(Entry<String, Object> e, INexusTree parentNode) {

		String nxClass = NexusExtractor.SDSClassName;
		String childNodeName = e.getKey();
		// Pair<Object, String> valueWithUnits = (Pair<Object, String>) e.getValue();
		// Object object = valueWithUnits.getFirst();
		// String units = valueWithUnits.getSecond();
		// System.out.println(">>> >>> units = " + units);
		Object object = e.getValue();
		String units = "placeholder units";
		if (object instanceof Pair) {
			// Pair<Object, String> valueWithUnits = (Pair<Object, String>) e.getValue();
			Pair<?, ?> valueWithUnits = (Pair<?, ?>) e.getValue();
			System.out.println(">>> >>> >>> object = " + valueWithUnits.getFirst());
			System.out.println(">>> >>> >>> units = " + valueWithUnits.getSecond());
			object = valueWithUnits.getFirst();
			units = (String) valueWithUnits.getSecond();
			System.out.println(">>> >>> >>> again units = " + units);
		}
		NexusGroupData groupData = null;
		groupData = createNexusGroupData(object);

		// INexusTree childNode = new NexusTreeNode(childNodeName, nxClass, parentNode, groupData);
		INexusTree childNode = new NexusTreeNode(childNodeName, nxClass, parentNode, groupData);
		if (units != null) {
			childNode.addChildNode(new NexusTreeNode("units", NexusExtractor.AttrClassName, childNode,
					new NexusGroupData(units)));
		}
		return childNode;
	}

	public INexusTree createChildNodeForScannableMetaEntry(Entry<String, Object> e, INexusTree parentNode) {

		String nxClass = NexusExtractor.SDSClassName;
		String childNodeName = e.getKey();
		// Pair<Object, String> valueWithUnits = (Pair<Object, String>) e.getValue();
		// Object object = valueWithUnits.getFirst();
		// String units = valueWithUnits.getSecond();
		// System.out.println(">>> >>> units = " + units);
		Object object = e.getValue();
		String units = "placeholder units";
		if (object instanceof Pair) {
			// Pair<Object, String> valueWithUnits = (Pair<Object, String>) e.getValue();
			Pair<?, ?> valueWithUnits = (Pair<?, ?>) e.getValue();
			System.out.println(">>> >>> >>> object = " + valueWithUnits.getFirst());
			System.out.println(">>> >>> >>> units = " + valueWithUnits.getSecond());
			object = valueWithUnits.getFirst();
			units = (String) valueWithUnits.getSecond();
			System.out.println(">>> >>> >>> again units = " + units);
		}
		NexusGroupData groupData = null;
		groupData = createNexusGroupData(object);

		// INexusTree childNode = new NexusTreeNode(childNodeName, nxClass, parentNode, groupData);
		INexusTree childNode = new NexusTreeNode(childNodeName, nxClass, parentNode, groupData);
		if (units != null) {
			childNode.addChildNode(new NexusTreeNode("units", NexusExtractor.AttrClassName, childNode,
					new NexusGroupData(units)));
		}
		return childNode;
	}

	public INexusTree createChildNodeForScannableMetaEntry_TEST(Scannable scn, INexusTree parentNode,
			Map<String, Object> scannableMap) {
		INexusTree childNode = null;

		List<String> fieldNames = ScannableUtils.getScannableFieldNames( Arrays.asList(new Scannable[]{scn}));
		
		if (scn instanceof ScannableGroup) {
			System.out.println("***Got scannable group = " + scn.getName());

			childNode = new NexusTreeNode(scn.getName(), NexusExtractor.NXCollectionClassName, parentNode);
			// parentNode = childNode;
			for (Scannable s : ((ScannableGroup) scn).getGroupMembers()) {
				System.out.println("***In scannable group got scannable = " + s.getName());
				INexusTree sNode = createChildNodeForScannableMetaEntry_TEST(s, childNode, scannableMap);
				if (sNode != null) {
					childNode.addChildNode(sNode);
				}
			}
		} else if (fieldNames.size() > 1){
			childNode = new NexusTreeNode(scn.getName(), NexusExtractor.NXCollectionClassName, parentNode);
			for( String s : fieldNames){
				String key = s;
				Object posObj = scannableMap.get(key);
				String units = null;

				if (posObj != null) {
					try {
						units = getScannableUnit(scn);
					} catch (DeviceException e1) {
						// TODO Auto-generated catch block
						logger.error("TODO put description of error here", e1);
					}

					System.out.println("***NEW key = " + key);
					System.out.println("***NEW obj = " + posObj.toString());

					if (units != null) {
						System.out.println("***NEW units = " + units);
					}

					NexusGroupData groupData = null;
					groupData = createNexusGroupData(posObj);
					if (groupData != null) {
						NexusTreeNode fieldNode = new NexusTreeNode(s, NexusExtractor.SDSClassName, childNode, groupData);
						if (units != null) {
							fieldNode.addChildNode(new NexusTreeNode("units", NexusExtractor.AttrClassName, fieldNode,
									new NexusGroupData(units)));
						}
						childNode.addChildNode(fieldNode);
					} else {
						System.out.println("***NEW goupData is null!");
					}
				}
				
			}
			
		} else {
			String key = scn.getName();
			Object posObj = scannableMap.get(key);
			String units = null;

			if (posObj != null) {
				try {
					units = getScannableUnit(scn);
				} catch (DeviceException e1) {
					// TODO Auto-generated catch block
					logger.error("TODO put description of error here", e1);
				}

				System.out.println("***NEW key = " + key);
				System.out.println("***NEW obj = " + posObj.toString());

				if (units != null) {
					System.out.println("***NEW units = " + units);
				}

				NexusGroupData groupData = null;
				groupData = createNexusGroupData(posObj);
				if (groupData != null) {
					// childNode.addChildNode(new NexusTreeNode(key, "NX" + key, childNode, new
					// NexusGroupData(posObj.toString())));
					childNode = new NexusTreeNode(scn.getName(), NexusExtractor.SDSClassName, parentNode, groupData);
					if (units != null && units.length()>0) {
						childNode.addChildNode(new NexusTreeNode("units", NexusExtractor.AttrClassName, childNode,
								new NexusGroupData(units)));
					}
				} else {
					System.out.println("***NEW goupData is null!");
				}
			}
		}
		return childNode;
	}

	public NexusGroupData createNexusGroupData(Object object) {
		NexusGroupData groupData = null;

		if (object instanceof String) {
			groupData = new NexusGroupData((String) object);
		} else if (object instanceof Integer) {
			groupData = new NexusGroupData((Integer) object);
		} else if (object instanceof Long) {
			Double dblValue = ((Number) object).doubleValue();
			double[] dblData = new double[] { dblValue };
			int[] dims = new int[] { dblData.length };
			int type = NexusFile.NX_FLOAT64;
			groupData = new NexusGroupData(dims, type, dblData);
		} else if (object instanceof Number) {
			Double dblValue = ((Number) object).doubleValue();
			double[] dblData = new double[] { dblValue };
			int[] dims = new int[] { dblData.length };
			int type = NexusFile.NX_FLOAT64;
			groupData = new NexusGroupData(dims, type, dblData);
		} else if (object instanceof double[]) {
			double[] data = (double[]) object;
			int[] dims = new int[] { data.length };
			int type = NexusFile.NX_FLOAT64;
			groupData = new NexusGroupData(dims, type, data);
		} else if (object instanceof int[]) {
			int[] data = (int[]) object;
			int[] dims = new int[] { data.length };
			int type = NexusFile.NX_INT32;
			groupData = new NexusGroupData(dims, type, data);
		} else if (object instanceof PyFloat) {
			double [] data = new double[]{((PyFloat) object).asDouble()};
			int[] dims = new int[] { 1 };
			int type = NexusFile.NX_FLOAT64;
			groupData = new NexusGroupData(dims, type, data);
		} else if (object instanceof long[]) {
			long[] data = (long[]) object;
			int dataLen = data.length;
			double[] dblData = new double[dataLen];
			for (int i = 0; i < dataLen; i++) {
				dblData[i] = data[i];
			}
			int[] dims = new int[] { dblData.length };
			int type = NexusFile.NX_FLOAT64;
			groupData = new NexusGroupData(dims, type, data);
		} else if (object instanceof Number[]) {
			Number[] data = (Number[]) object;
			int dataLen = data.length;
			double[] dblData = new double[dataLen];
			for (int i = 0; i < dataLen; i++) {
				dblData[i] = data[i].doubleValue();
			}
			int[] dims = new int[] { dblData.length };
			int type = NexusFile.NX_FLOAT64;
			groupData = new NexusGroupData(dims, type, dblData);
		} else if (object instanceof PyList) {
			// coerce PyList into double array.
			int dataLen = ((PyList) object).__len__();
			double[] dblData = new double[dataLen];
			for (int i = 0; i < dataLen; i++) {
				// dblData[i] = Double.valueOf(((PyList) object).__getitem__(i).toString());
				PyObject item = ((PyList) object).__finditem__(i);

				if (item instanceof PyNone) {
					dblData[i] = Double.NaN;
				} else {
					dblData[i] = Double.valueOf(item.toString());
				}
			}
		} else if (object instanceof PySequence) {
			// coerce PySequence into double array.
			int dataLen = ((PySequence) object).__len__();
			double[] dblData = new double[dataLen];
			for (int i = 0; i < dataLen; i++) {
				// dblData[i] = Double.valueOf(((PySequence) object).__getitem__(i).toString());
				PyObject item = ((PySequence) object).__finditem__(i);

				if (item instanceof PyNone) {
					dblData[i] = Double.NaN;
				} else {
					dblData[i] = Double.valueOf(item.toString());
				}
			}
		} else {
			logger.error("unhandled data type: " + object.getClass().getName()
					+ " - this dataset might not have been written correctly to Nexus file.");
			groupData = new NexusGroupData(object.toString());
		}
		return groupData;
	}

	public Object[] separateGetPositionOutputIntoElementalPosObjects(Object scannableGetPositionOut) {
		if (scannableGetPositionOut == null)
			return new Object[] {};

		Object[] elements = new Object[] { scannableGetPositionOut };
		if (scannableGetPositionOut instanceof Object[]) {
			elements = (Object[]) scannableGetPositionOut;
		} else if (scannableGetPositionOut instanceof PySequence) {
			PySequence seq = (PySequence) scannableGetPositionOut;
			int len = seq.__len__();
			elements = new Object[len];
			for (int i = 0; i < len; i++) {
				elements[i] = seq.__finditem__(i);
			}
		} else if (scannableGetPositionOut instanceof PyList) {
			PyList seq = (PyList) scannableGetPositionOut;
			int len = seq.__len__();
			elements = new Object[len];
			for (int i = 0; i < len; i++) {
				elements[i] = seq.__finditem__(i);
			}
		} else if (scannableGetPositionOut.getClass().isArray()) {
			int len = ArrayUtils.getLength(scannableGetPositionOut);
			elements = new Object[len];
			for (int i = 0; i < len; i++) {
				elements[i] = Array.get(scannableGetPositionOut, i);
			}
		} else if (scannableGetPositionOut instanceof PlottableDetectorData) {
			elements = ((PlottableDetectorData) scannableGetPositionOut).getDoubleVals();
		}
		return elements;
	}

	public static String getScannableUnit(Scannable s) throws DeviceException {
		String unit = null;
		if (s instanceof ScannableMotionUnits) {
			unit = ((ScannableMotionUnits) s).getUserUnits();
		} else {
			Object attribute = s.getAttribute(ScannableMotionUnits.USERUNITS);
			if (attribute != null){
				unit = attribute.toString();
			}
		}
		return unit;
	}

	public INexusTree createChildNodeForMetaTextEntry(String name, String nxClass, INexusTree parentNode,
			NexusGroupData groupData) {

		INexusTree outTree = new NexusTreeNode(name, nxClass, parentNode, groupData);
		return outTree;
	}
}

class NexusTreeStringDump {
	INexusTree tree;

	public NexusTreeStringDump(INexusTree tree) {
		super();
		this.tree = tree;
	}

	@Override
	public String toString() {
		return "NexusTreeStringDump [tree=" + tree + "]";
	}

}