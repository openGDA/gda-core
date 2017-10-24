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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.data.PlottableDetectorData;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeAppender;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.Findable;
import gda.jython.InterfaceProvider;

public class NXMetaDataProvider implements NexusTreeAppender, Map<String, Object>, Findable {

	private String name;


	private static final String GROUP_ITEM_SEPARATOR = ".";		// single dot
	private static final String FIELD_ITEM_SEPARATOR = ".";		// single dot
	private static final String PREAMBLE = "meta:\n";
	private static final String LS_NEXT_ITEM_SEPARATOR = "\n";	// single new line
	private static final String LL_NEXT_ITEM_SEPARATOR = "\n";	// single new line
	private static final String LL_MID_CONNECTOR = " = ";
	private static final String LL_UNITS_SEPARATOR = " ";		// single white space

	private static final String LL_ARRAY_OPEN = "[";
	private static final String LL_ARRAY_CLOSE = "]";
	private static final String LL_ARRAY_ITEM_SEPARATOR = ", ";	// single coma followed by single white space
	private static final String LL_FLOAT_ARRAY_FORMAT = "%5.3f";
	private static final String LL_INT_ARRAY_FORMAT = "%d";

	public String groupItemSeparator = GROUP_ITEM_SEPARATOR;
	public String fieldItemSeparator = FIELD_ITEM_SEPARATOR;
	public String preamble = PREAMBLE;
	public String lsNextItemSeparator = LS_NEXT_ITEM_SEPARATOR;
	public String llNextItemSeparator = LL_NEXT_ITEM_SEPARATOR;
	public String llMidConnector = LL_MID_CONNECTOR;
	public String llUnitsSeparator = LL_UNITS_SEPARATOR;

	public String llArrayOpen = LL_ARRAY_OPEN;
	public String llArrayClose = LL_ARRAY_CLOSE;
	public String llArrayItemSeparator = LL_ARRAY_ITEM_SEPARATOR;
	public String llFloatArrayFormat = LL_FLOAT_ARRAY_FORMAT;
	public String llIntArrayFormat = LL_INT_ARRAY_FORMAT;

	private static final Map<String, String> defaultFormattingMap = new HashMap<String, String>();
	private Map<String, String> formattingMap;

	private static final String ATTRIBUTE_KEY_FOR_UNITS = "units";
	private static final String ATTRIBUTE_KEY_FOR_FORMAT = "format";

	private static final String ATTRIBUTE_KEY_FOR_METADATA_TYPE = "metadata_type";
	private static final String ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SUPPLIED = "text";
	private static final String ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SCANNABLE = "scannable";
	private static final String ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SCANNABLE_GROUP = ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SCANNABLE;

	private static final String ATTRIBUTE_KEY_FOR_FIELD_TYPE = "field_type";
	private static final String ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT = "input";
	private static final String ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA = "extra";


	Map<String, Object> metaTextualMap;
	//List<Scannable> metaScannables = new Vector<Scannable>();

	private boolean withScannables = false;

	private Collection<String> dynamicScannables = new Vector<String>();

	private static final Logger logger = LoggerFactory.getLogger(NXMetaDataProvider.class);

	public NXMetaDataProvider() {
		super();
		defaultFormattingMap.put("PREAMBLE", "meta:\n");			//
		defaultFormattingMap.put("LS_NEXT_ITEM_SEPARATOR", "\n");	// single new line
		defaultFormattingMap.put("LL_MID_CONNECTOR", " = ");
		defaultFormattingMap.put("LL_NEXT_ITEM_SEPARATOR", "\n");	// single new line
		defaultFormattingMap.put("LL_UNITS_SEPARATOR", " ");		// single space
		defaultFormattingMap.put("LL_ARRAY_OPEN", "[");
		defaultFormattingMap.put("LL_ARRAY_CLOSE", "]");
		defaultFormattingMap.put("LL_ARRAY_ITEM_SEPARATOR", ", ");	// single coma followed by single space
		defaultFormattingMap.put("LL_FLOAT_ARRAY_FORMAT", "%5.3f");
		defaultFormattingMap.put("LL_INT_ARRAY_FORMAT", "%d");

		reset();
	}

	@Override
	public void appendToTopNode(INexusTree topNode) {
		for (Entry<String, Object> e : metaTextualMap.entrySet()) {
			INexusTree childNode = createChildNodeForTextualMetaEntry(e, topNode);
			if (childNode != null) {
				topNode.addChildNode(childNode);
			} else {
				logger.debug("Nexus tree child node is null for " + e.getKey());
			}
		}

		if (withScannables) {
			//for (Scannable scn : metaScannables) {
			List<Scannable> metaScannableList = new Vector<Scannable>();
			Set<String> metaScannableSet = NexusDataWriter.getMetadatascannables();
			for (String scannableName : metaScannableSet) {
				Scannable scannable = (Scannable) InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
				if (scannable == null) {
					throw new IllegalStateException("could not find scannable '" + scannableName + "' in Jython namespace.");
				}
				metaScannableList.add(scannable);
			}
			for (Scannable scn : metaScannableList) {
//				System.out.println("getNexusTree: scannable = " + scn.getName());
				try {
					Map<String, Object> scannableMap = createMetaScannableMap(scn);
					//System.out.println("\t scannableMap = " + scannableMap.toString());
					INexusTree childNode = createChildNodeForScannableMetaEntry(scn, topNode, scannableMap); //TODO Change name
					if (childNode != null) {
						topNode.addChildNode(childNode);
					} else {
						logger.debug("Nexus tree child node is null for " + scn.getName());
					}
				} catch (DeviceException e1) {
					logger.error("Error creating metadata for scannable" + scn.getName(), e1);
				}

			}
		}
	}

	public void reset() {
		this.metaTextualMap = new HashMap<String, Object>();
		this.formattingMap = new HashMap<String, String>();

		formattingMap.put("preamble", defaultFormattingMap.get("PREAMBLE"));
		formattingMap.put("lsNextItemSeparator", defaultFormattingMap.get("LS_NEXT_ITEM_SEPARATOR"));
		formattingMap.put("llMidConnector", defaultFormattingMap.get("LL_MID_CONNECTOR"));
		formattingMap.put("llNextItemSeparator", defaultFormattingMap.get("LL_NEXT_ITEM_SEPARATOR"));
		formattingMap.put("llUnitsSeparator", defaultFormattingMap.get("LL_UNITS_SEPARATOR"));
		formattingMap.put("llArrayOpen", defaultFormattingMap.get("LL_ARRAY_OPEN"));
		formattingMap.put("llArrayClose", defaultFormattingMap.get("LL_ARRAY_CLOSE"));
		formattingMap.put("llArrayItemSeparator", defaultFormattingMap.get("LL_ARRAY_ITEM_SEPARATOR"));
		formattingMap.put("llFloatArrayFormat", defaultFormattingMap.get("LL_FLOAT_ARRAY_FORMAT"));
		formattingMap.put("llIntArrayFormat", defaultFormattingMap.get("LL_INT_ARRAY_FORMAT"));

		//modifyFormattingMap(defaultFormattingMap);
	}

	/**
	 * Removes all scannables added with NXMetaDataProvider.add(..).
	 * Does not restore anything that was removed or overwritten.
	 */
	public void clearDynamicScannableMetadata() {
		for (String name : dynamicScannables) {
			NexusDataWriter.getMetadatascannables().remove(name);
		}
		dynamicScannables.clear();
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
		//System.out.println("adding user-supplied given 3 args");
	}

	public void add(MetaDataUserSuppliedItem userSupplied) {
		Pair<Object, String> valueWithUnits = new Pair<Object, String>(userSupplied.getValue(), userSupplied.getUnits());
		put(userSupplied.getKey(), valueWithUnits);
		//System.out.println("adding user-supplied given 1 MetaDataUserSuppliedItem arg");
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
		//String msg = "remove key = " + key;
		//logger.debug(msg);
		//System.out.println(msg);
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

	public void setMetaScannables(List<Scannable> metaScannables) {
		//this.metaScannables.addAll(metaScannables);
		for (Scannable scn : metaScannables) {
			// FIXME: why is NXMetaDataProvider is fiddling with NexusDataWriter?
			NexusDataWriter.getMetadatascannables().add(scn.getName());
		}
	}

	public List<Scannable> getMetaScannables() {
		//List<Scannable> outLst = new Vector<Scannable>(this.metaScannables);
		//return outLst;
		List<Scannable> metaScannableList = new Vector<Scannable>();
		Set<String> metaScannableSet = NexusDataWriter.getMetadatascannables();
		for (String scannableName : metaScannableSet) {

			try {
				Scannable scannable = (Scannable) InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
				metaScannableList.add(scannable);
			} catch (Exception e) {
				throw new RuntimeException("Error converting " + scannableName + " to a scannable", e);
			}
		}
		return metaScannableList;
	}


	/*
	 * To be called by meata_ls command
	 */
	public String list(boolean withValues) {
		withScannables = true;
		return concatenateContentsForList(withValues, preamble, lsNextItemSeparator, llMidConnector, llNextItemSeparator);
	}

	public String concatenateContentsForList(boolean withValues, String preamble, String lsNextItemSeparator, String llMidConnector, String llNextItemSeparator) {

		INexusTree listTree = new NexusTreeNode("list", NexusExtractor.NXCollectionClassName, null);
		appendToTopNode(listTree);
		NexusTreeStringDump treeDump = new NexusTreeStringDump(listTree);

		String strOut = "";
		if (preamble == null) {
			strOut += PREAMBLE;
		} else {
			strOut += preamble;
		}
		String lsNextItemSeparatorUsed = "";
		if (lsNextItemSeparator == null) {
			lsNextItemSeparatorUsed += LS_NEXT_ITEM_SEPARATOR;
		} else {
			lsNextItemSeparatorUsed += lsNextItemSeparator;
		}

		String llMidConnectorUsed = "";
		if (llMidConnector == null) {
			llMidConnectorUsed += LL_MID_CONNECTOR;
		} else {
			llMidConnectorUsed += llMidConnector;
		}

		String llNextItemSeparatorUsed = "";
		if (llNextItemSeparator == null) {
			llNextItemSeparatorUsed += LL_NEXT_ITEM_SEPARATOR;
		} else {
			llNextItemSeparatorUsed += llNextItemSeparator;
		}

		List<DatumForJythonList> alphabeticalOut = new Vector<DatumForJythonList>();


		NexusGroupData ngdFieldType = null;
		for (Pair<String, NexusDumpItem> e : treeDump.getDumpList()) {
			String name = e.getFirst();
			String value = llMidConnectorUsed + e.getSecond().toString() + llNextItemSeparator;
			String field_type = "";
			ngdFieldType = e.getSecond().getFieldType();
			if (ngdFieldType != null ) {
				field_type = ngdFieldType.toString();
			}
			alphabeticalOut.add( new DatumForJythonList(name, value, field_type));
		}

		Collections.sort(alphabeticalOut, new Comparator<DatumForJythonList>() {
		@Override
		public int compare(final DatumForJythonList o1, final DatumForJythonList o2) {

			int out = 0;
			String name1 = o1.datumName;
			String fieldType1 = o1.datumFieldType;

			String name2 = o2.datumName;
			String fieldType2 = o2.datumFieldType;

			String splitOn = FIELD_ITEM_SEPARATOR;
			if (splitOn.equals(".")) {
				splitOn = "\\" + splitOn;
			}
			String[] split1 = name1.split("\\.",-1);
			String[] split2 = name2.split("\\.",-1);
			int count1 = split1.length-1;
			int count2 = split2.length-1;

			String root1 = name1;
			int lastIdx1 = name1.lastIndexOf("\\.");
			if (lastIdx1 >= 0) {
				root1 = name1.substring(0, lastIdx1);
			}

			String root2 = name2;
			int lastIdx2 = name2.lastIndexOf("\\.");
			if (lastIdx2 >= 0) {
				root2 = name2.substring(0, lastIdx2);
			}

			if (count1 != count2) {
				out = name1.toLowerCase().compareTo(name2.toLowerCase());
				//System.out.println("count1 != count2: out = " + out );
			}
			else
			{
				// if belong to the same scannable
				if (root1.equals(root2)) {
					//System.out.println("CASE split1[count1] == split2[count2]: out = " + out );
					if (fieldType1 == ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT && fieldType2 == ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA) {
						// input before extra
						out = 1;
						//System.out.println("input before extra: out = " + out );
					} else if (fieldType1 == ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA && fieldType2 == ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT) {
						// input before extra
						out = -1;
						//System.out.println("extra after input: out = " + out );
					} else {
						out = name1.toLowerCase().compareTo(name2.toLowerCase());
						//System.out.println("same field types: out = " + out);
					}
				} else {
					out = name1.toLowerCase().compareTo(name2.toLowerCase());
					//System.out.println("CASE split1[count1] != split2[count2]: out = " + out );
				}
			}
			//System.out.println("END: out = " + out );
			return out;
			}
		} );

		if (withValues) {
			for (DatumForJythonList d : alphabeticalOut) {
				strOut += d.datumName;
				//strOut += llMidConnectorUsed;			//already included
				strOut += d.datumValue;
				//strOut += llNextItemSeparatorUsed;	//already included
			}
			int substringLen = strOut.length() - llNextItemSeparatorUsed.length();
			if (substringLen >= 0) {
				strOut = strOut.substring(0, substringLen);
			}

			return strOut;
		}


		for (DatumForJythonList d : alphabeticalOut) {
			strOut += d.datumName + lsNextItemSeparatorUsed;
		}
		int strOutLen = strOut.length();
		int lsNextItemSeparatorUsedLen = lsNextItemSeparatorUsed.length();
		if (strOutLen >= lsNextItemSeparatorUsedLen) {
			strOut = strOut.substring(0, strOutLen - lsNextItemSeparatorUsedLen);
		}
		withScannables = false;
		return strOut;
	}

	public void add(Object... args) {
		if (args[0] instanceof Scannable && args.length == 1) {
			add((Scannable) args[0]);
		} else if (args[0] instanceof String && args.length == 2) {
			add((String) args[0], args[1], null);
		} else if (args[0] instanceof String && args.length == 3 && args[2] instanceof String) {
			add((String) args[0], args[1], (String) args[2]);
		} else {
			for (Object arg : args) {
				if (arg instanceof Scannable) {
					add((Scannable) arg);
				} else {
					throw new IllegalArgumentException("Invalid argument: " + arg.toString() + " is not a Scannable! Usage: add(String,Object [,String]) or add(Scannable [,Scannable,Scannable...]))");
				}
			}
		}
	}

	public void add(Scannable scannable) {
		logger.debug("add called on scannable = " + scannable.getName());
		String scannableName = scannable.getName();
		while (NexusDataWriter.getMetadatascannables().contains(scannableName)) {
			NexusDataWriter.getMetadatascannables().remove(scannableName);
		}
		dynamicScannables.add(scannableName);
		NexusDataWriter.getMetadatascannables().add(scannableName);
	}

	public void remove(Object... args) {
		for (Object arg : args) {
			if (arg instanceof Scannable) {
				remove((Scannable) arg);
			} else if (arg instanceof String) {
				remove(arg);
			} else {
				throw new IllegalArgumentException("Invalid arguments");
			}
		}
	}

	public void remove(Scannable scannable) {
		String name = scannable.getName();
		logger.debug("remove called on scannable = " + name);
		//metaScannables.remove(scannable);
		NexusDataWriter.getMetadatascannables().remove(name);
		dynamicScannables.remove(name);
	}


	public Map<String, Object> createMetaScannableMap(Scannable scn) throws DeviceException {
		Map<String, Object> metaScannableMapObj = new HashMap<String, Object>();

		List<ScannableMetaEntryObj> metasObj = new Vector<ScannableMetaEntryObj>();

		Object scnPos = null;
		try {
			scnPos = scn.getPosition();
		} catch (Exception e) {
			throw new DeviceException("Error calling getPosition on scannable " + scn.getName(), e);
		}

		if (scnPos == null){
			// something's wrong in the scannable! log this and call the metadata "null"
			logger.info("Null returned when asking " + scn.getName() + " for its position");
			scnPos = "null";
		}

		Object[] elementalGetPosObjects = separateGetPositionOutputIntoElementalPosObjects(scnPos);

		List<String> scannableFieldNames = getScannableFieldNames(scn);

		for (int i = 0; i < scannableFieldNames.size(); i++) {
			metasObj.add(new ScannableMetaEntryObj(scannableFieldNames.get(i), elementalGetPosObjects[i]));
		}

		for (ScannableMetaEntryObj e : metasObj) {
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

	public List<String> getScannableInputNames(Scannable scannable) {
		Vector<String> outNames = new Vector<String>();

		// if detector the inputNames are not returned in ScanDataPoint so do not add
		if (!(scannable instanceof Detector)) {
			outNames.addAll(Arrays.asList(scannable.getInputNames()));
		}
		return outNames;
	}

	public List<String> getScannableExtraNames(Scannable scannable) {
		Vector<String> outNames = new Vector<String>();

		String[] extraNames = scannable.getExtraNames();
		if (scannable instanceof Detector) {
			if (extraNames.length > 0) {
				outNames.addAll(Arrays.asList(extraNames));
			} else {
				outNames.add(scannable.getName());
			}
		} else {
			outNames.addAll(Arrays.asList(extraNames));
		}
		return outNames;
	}

	public INexusTree createChildNodeForTextualMetaEntry(Entry<String, Object> e, INexusTree parentNode) {
		String nxClass = NexusExtractor.SDSClassName;
		String childNodeName = e.getKey();
		Object object = e.getValue();
		String units = "placeholder units";
		if (object instanceof Pair) {
			Pair<?, ?> valueWithUnits = (Pair<?, ?>) e.getValue();
			object = valueWithUnits.getFirst();
			units = (String) valueWithUnits.getSecond();
		}
		NexusGroupData groupData = null;
		groupData = createNexusGroupData(object);

		INexusTree node = new NexusTreeNode(childNodeName, nxClass, parentNode, groupData);

		node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_METADATA_TYPE, NexusExtractor.AttrClassName, node,
				new NexusGroupData(ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SUPPLIED)));

		if (units != null) {
			node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_UNITS, NexusExtractor.AttrClassName, node,
					new NexusGroupData(units)));
		}
		return node;
	}


	public INexusTree createChildNodeForScannableMetaEntry(Scannable scn, INexusTree parentNode,
			Map<String, Object> scannableMap) {
		INexusTree node = null;

		List<String> fieldNames = ScannableUtils.getScannableFieldNames( Arrays.asList(new Scannable[]{scn}));
		List<String> inputNames = new Vector<String>();
		inputNames = getScannableInputNames(scn);

		List<String> extraNames = new Vector<String>();
		extraNames = getScannableExtraNames(scn);

		int inputSize = inputNames.size();
		int extraSize = extraNames.size();
		int fieldSize = fieldNames.size();

		if (inputSize + extraSize != fieldSize ) {
			String msg = "input names + extra names != field names (" + Integer.toString(inputSize) + " + " + Integer.toString(extraSize) + " != " + Integer.toString(fieldSize);

			System.out.println(msg);
			//throw new DeviceException("input names + extra names != field names ("inputNames.size());
		}

		if (scn instanceof ScannableGroup) {

			node = new NexusTreeNode(scn.getName(), NexusExtractor.NXCollectionClassName, parentNode);
			node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_METADATA_TYPE, NexusExtractor.AttrClassName, node,
					new NexusGroupData(ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SCANNABLE_GROUP)));

			for (Scannable s : ((ScannableGroup) scn).getGroupMembers()) {
				INexusTree sNode = createChildNodeForScannableMetaEntry(s, node, scannableMap);
				if (sNode != null) {
					node.addChildNode(sNode);
				}
			}
		} else if (hasGenuineMultipleFieldNames(scn)) {
			node = new NexusTreeNode(scn.getName(), NexusExtractor.NXCollectionClassName, parentNode);

			node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_METADATA_TYPE, NexusExtractor.AttrClassName, node,
					new NexusGroupData(ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SCANNABLE)));

			String[] outputFormat = null;
			outputFormat = scn.getOutputFormat();

			int fieldIdx = 0;
			for( String field : inputNames){
				String key = field;
				Object posObj = scannableMap.get(key);
				String units = null;

				if (posObj != null) {
					try {
						units = getScannableUnit(scn);
					} catch (DeviceException e1) {
						logger.error("Error getting scannable unit", e1);
					}

					NexusGroupData groupData = null;
					groupData = createNexusGroupData(posObj);
					if (groupData != null) {
						NexusTreeNode fieldNode = new NexusTreeNode(field, NexusExtractor.SDSClassName, node, groupData);
						fieldNode.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_FIELD_TYPE, NexusExtractor.AttrClassName, fieldNode,
								new NexusGroupData(ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT)));

						if (units != null) {
							fieldNode.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_UNITS, NexusExtractor.AttrClassName, fieldNode,
									new NexusGroupData(units)));
						}
						if (outputFormat != null && outputFormat[fieldIdx] != null) {
							fieldNode.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_FORMAT, NexusExtractor.AttrClassName, fieldNode,
									new NexusGroupData(outputFormat[fieldIdx])));
						}
						node.addChildNode(fieldNode);
					} else {
						logger.warn("GroupData is null!");
					}
				}
				fieldIdx += 1;
			}

			for( String field : extraNames){
				String key = field;
				Object posObj = scannableMap.get(key);
				String units = null;

				if (posObj != null) {
					try {
						units = getScannableUnit(scn);
					} catch (DeviceException e1) {
						// TODO Auto-generated catch block
						logger.error("TODO put description of error here", e1);
					}

					NexusGroupData groupData = null;
					groupData = createNexusGroupData(posObj);
					if (groupData != null) {
						NexusTreeNode fieldNode = new NexusTreeNode(field, NexusExtractor.SDSClassName, node, groupData);
						fieldNode.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_FIELD_TYPE, NexusExtractor.AttrClassName, fieldNode,
								new NexusGroupData(ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA)));
						if (units != null) {
							fieldNode.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_UNITS, NexusExtractor.AttrClassName, fieldNode,
									new NexusGroupData(units)));
						}
						if (outputFormat != null && outputFormat[fieldIdx] != null) {
							fieldNode.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_FORMAT, NexusExtractor.AttrClassName, fieldNode,
									new NexusGroupData(outputFormat[fieldIdx])));
						}
						node.addChildNode(fieldNode);
					} else {
						logger.warn("GroupData is null!");
					}
				}
				fieldIdx += 1;
			}

		} else {
			String key = null;
			int fieldIdx = 0;
			String whoami = "";
			if (inputSize==1) {
				key = inputNames.get(fieldIdx);
				whoami = "input";
			} else if (extraSize == 1){
				key = extraNames.get(fieldIdx);
				whoami = "extra";
			}
			else {
				key = scn.getName();
			}

			String[] outputFormat = null;
			outputFormat = scn.getOutputFormat();

			Object posObj = scannableMap.get(key);
			String units = null;

			if (posObj != null) {
				try {
					units = getScannableUnit(scn);
				} catch (DeviceException e1) {
					logger.error("TODO put description of error here", e1);
				}

				NexusGroupData groupData = null;
				groupData = createNexusGroupData(posObj);
				if (groupData != null) {
					node = new NexusTreeNode(key, NexusExtractor.SDSClassName, parentNode, groupData);

					if (parentNode.getAttribute(ATTRIBUTE_KEY_FOR_METADATA_TYPE) == null ) {
						node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_METADATA_TYPE, NexusExtractor.AttrClassName, node,
							new NexusGroupData(ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SCANNABLE)));
					}
					//else
					//{
					//	System.out.println("Metadata type already set on the parent");
					//}

					if (whoami.equals("input")) {
						node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_FIELD_TYPE, NexusExtractor.AttrClassName, node,
								new NexusGroupData(ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT)));
					} else if (whoami.equals("extra")) {
						node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_FIELD_TYPE, NexusExtractor.AttrClassName, node,
								new NexusGroupData(ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA)));
					}

					if (units != null && units.length()>0) {
						node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_UNITS, NexusExtractor.AttrClassName, node,
								new NexusGroupData(units)));

						if (outputFormat != null && outputFormat[fieldIdx] != null) {
							//System.out.println("\t\t output format = " + outputFormat[fieldIdx]);
							node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_FORMAT, NexusExtractor.AttrClassName, node,
									new NexusGroupData(outputFormat[fieldIdx])));
						}

					}
				} else {
					System.out.println("***NEW goupData is null!");
				}
			} else {
				System.out.println("NOT FOUND!!! key = " + key);
				System.out.println("\t scannableMap = " + scannableMap.toString());
			}
		}
		return node;
	}

	public boolean hasGenuineMultipleFieldNames(Scannable scn) {
		List<String> inputNames = new Vector<String>();
		inputNames = getScannableInputNames(scn);
		List<String> extraNames = new Vector<String>();
		extraNames = getScannableExtraNames(scn);

		int inputSize = inputNames.size();
		int extraSize = extraNames.size();

		boolean hasRedundantSingleInputName = (inputSize == 1 && extraSize == 0 && (scn.getName().equals(inputNames.get(0)) || inputNames.get(0).equals(Scannable.DEFAULT_INPUT_NAME)));
		boolean hasRedundanSingleExtraName = (inputSize == 0 && extraSize == 1 && (scn.getName().equals(extraNames.get(0)) || extraNames.get(0).equals(Scannable.DEFAULT_INPUT_NAME)));
		boolean hasRedundantSingleFieldName = (hasRedundantSingleInputName || hasRedundanSingleExtraName);

		return !hasRedundantSingleFieldName;
	}

	public NexusGroupData createNexusGroupData(Object object) {
		NexusGroupData groupData = null;

		if (object instanceof String) {
			groupData = new NexusGroupData((String) object);
		} else if (object instanceof Integer) {
			groupData = new NexusGroupData((Integer) object);
//		} else if (object instanceof Long) {
//			Double dblValue = ((Number) object).doubleValue();
//			double[] dblData = new double[] { dblValue };
//			groupData = new NexusGroupData(dblData);
		} else if (object instanceof Number) {
			groupData = new NexusGroupData(((Number) object).doubleValue());
		} else if (object instanceof double[]) {
			groupData = new NexusGroupData((double[]) object);
		} else if (object instanceof int[]) {
			groupData = new NexusGroupData((int[]) object);
		} else if (object instanceof PyFloat) {
			groupData = new NexusGroupData(((PyFloat) object).asDouble());
		} else if (object instanceof PyInteger) {
			//store as NX_FLOAT64 since a lot of things may pass an int for an expect a double on readback
			groupData = new NexusGroupData( (double) ( (PyInteger) object).getValue() );
		} else if (object instanceof long[]) {
			long[] data = (long[]) object;
			int dataLen = data.length;
			double[] dblData = new double[dataLen];
			for (int i = 0; i < dataLen; i++) {
				dblData[i] = data[i];
			}
			groupData = new NexusGroupData(dblData);
		} else if (object instanceof Number[]) {
			Number[] data = (Number[]) object;
			int dataLen = data.length;
			double[] dblData = new double[dataLen];
			for (int i = 0; i < dataLen; i++) {
				dblData[i] = data[i].doubleValue();
			}
			groupData = new NexusGroupData(dblData);
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
			groupData = new NexusGroupData(dblData);
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
			groupData = new NexusGroupData(dblData);
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

	public void modifyFormattingMap(Map<String, String> modificationsMap) {
		//this.formattingMap = formattingMap;
		for (Entry<String, String> e : modificationsMap.entrySet()) {
			this.formattingMap.put(e.getKey(), e.getValue());
			//System.out.println("modifyFormattingMap: key = " + e.getKey() + ", val = " + e.getValue());
		}
	}

}

/**
 * class to create a map of entries from the nexus tree
 *
 * A key is a concatenation of nexus groups names, separated by a dot, followed by the item name
 * the entry is the SDS item  plus the format attribute as a String
 */
class NexusTreeStringDump {
	private static final String KEY_SEPARATOR = ".";
	INexusTree tree;
	private Map<String, NexusDumpItem> dumpMap;
	private List<Pair<String, NexusDumpItem>> dumpList;

	public NexusTreeStringDump(INexusTree tree) {
		super();
		this.tree = tree;
		this.dumpMap = new HashMap<String, NexusDumpItem>();
		this.dumpList = new Vector<Pair<String, NexusDumpItem>>();
		Traverse();
	}

	@Override
	public String toString() {
		return "NexusTreeStringDump [tree=" + tree + "]";
	}

	private void Traverse() {
		if (this.tree != null) {
			int nNodes = this.tree.getNumberOfChildNodes();
			String key = "";
			for (int i = 0; i < nNodes; i++) {
				INexusTree node = this.tree.getChildNode(i);
				Traverse(node, key);
			}
		}
	}


	private void Traverse(INexusTree tree, String key) {
		if (tree != null) {
			if (isToBeTraversed(tree)) {
				int nNodes = tree.getNumberOfChildNodes();
				key += tree.getName() + KEY_SEPARATOR;
				if (nNodes > 0) {
					for (int i = 0; i < nNodes; i++) {
						INexusTree node = tree.getChildNode(i);
						Traverse(node, key);
					}
				}
			} else {
				if (isToBeHarvested(tree)) {
					key += tree.getName();
					int nNodes = tree.getNumberOfChildNodes();

					NexusGroupData ngdData = tree.getData();

					Map<String, NexusGroupData> ngdMap = new HashMap<String, NexusGroupData>();
					for (int i = 0; i < nNodes; i++) {
						INexusTree node = tree.getChildNode(i);
						ngdMap.put(node.getName(), node.getData());
					}

					NexusGroupData ngdUnits = ngdMap.get("units");
					NexusGroupData ngdFormat = ngdMap.get("format");
					NexusGroupData ngdFieldType = ngdMap.get("field_type");

					NexusDumpItem item = new NexusDumpItem(ngdData, ngdUnits, ngdFormat, ngdFieldType);
					dumpMap.put(key, new NexusDumpItem(ngdData, ngdUnits, ngdFormat, ngdFieldType));
					Pair<String, NexusDumpItem> e = new Pair<String, NexusDumpItem>(key,item);
					dumpList.add(e);
				}
			}
		}
	}
	public Map<String, NexusDumpItem> getDumpMap() {
		return dumpMap;
	}

	public List<Pair<String, NexusDumpItem>> getDumpList() {
		return dumpList;
	}

	public boolean isToBeTraversed(INexusTree tree) {
		int nNodes = tree.getNumberOfChildNodes();
		boolean out = (nNodes > 0);
		Map<String, Serializable> attributes = tree.getAttributes();
		if (attributes != null && attributes.size() == nNodes ) {

			Serializable units = attributes.get("units");
			Serializable format = attributes.get("format");
			Serializable field_t = attributes.get("field_type");
			Serializable metadata_t = attributes.get("metadata_type");

			int nodesToBeTraversed = nNodes;
			if (units != null) {
				nodesToBeTraversed -= 1;
			}
			if (format != null) {
				nodesToBeTraversed -= 1;
			}
			if (field_t != null) {
				nodesToBeTraversed -= 1;
			}
			if (metadata_t != null) {
				nodesToBeTraversed -= 1;
			}
			out = (nodesToBeTraversed > 0);
		}
		return out;
	}

	public boolean isToBeHarvested(INexusTree tree) {
		int nNodes = tree.getNumberOfChildNodes();
		boolean out = (nNodes > 0);
		Map<String, Serializable> attributes = tree.getAttributes();
		if (attributes != null && attributes.size() == nNodes ) {

			Serializable units = attributes.get("units");
			Serializable format = attributes.get("format");
			Serializable field_t = attributes.get("field_type");
			Serializable metadata_t = attributes.get("metadata_type");

			int nodesRemaining = nNodes;
			if (units != null) {
				nodesRemaining -= 1;
			}
			if (format != null) {
				nodesRemaining -= 1;
			}
			if (field_t != null) {
				nodesRemaining -= 1;
			}
			if (metadata_t != null) {
				nodesRemaining -= 1;
			}
			out = (nodesRemaining == 0);
		}
		return out;
	}

}


class DatumForJythonList {
	String datumName;			// ls part
	String datumValue;			// ll extension
	String datumFieldType;		// input or extra (for scannable)

	public DatumForJythonList(String name, String value, String field_type) {
		super();
		this.datumName = name;
		this.datumValue = value;
		this.datumFieldType = field_type;
	}
}


/*
 * Class to create a user friendly string representation of an item
 */
class NexusDumpItem {
	NexusGroupData data;
	NexusGroupData units;
	NexusGroupData format;
	private NexusGroupData field_type;

	@Override
	public String toString() {
		Object targetVal = null;

		String out = "";
		if (data != null) {
			Object val = data.dimensions.length==1 && data.dimensions[0]==1 ? data.getFirstValue() : data.getBuffer();
			if (format !=null) {
				out = String.format(format.dataToTxt(false, true, false), val);
			} else {
				String defaultFormat = "";
				if (val instanceof Integer) {
					defaultFormat = "%d";
					targetVal = val;
				}
				else if (val instanceof Double) {
					defaultFormat = "%5.3f";
					targetVal = val;
				}
				else if (val instanceof String) {
					defaultFormat = "%s";
					targetVal = val;
				}
				else if (val instanceof byte[]) {
					defaultFormat = "%s";
					String sVal = new String( (byte[]) val);
					targetVal = sVal;
				} else if (val instanceof int[]) {
					int[] intVal = (int[]) val;
					int intValLen = intVal.length;
					Integer[] intTargetVal = new Integer[intValLen];
					intTargetVal = new Integer[intValLen];
					for (int i = 0; i < intValLen; i++) {
						intTargetVal[i] = intVal[i];
					}
					val = targetVal;

					defaultFormat = createIntArrayFormat((Object[])intTargetVal);
					targetVal = intTargetVal;
				} else if (val instanceof double[]) {
					double[] dblVal = (double[]) val;
					int dblValLen = dblVal.length;
					Double[] dblTargetVal = new Double[dblValLen];
					dblTargetVal = new Double[dblValLen];
					for (int i = 0; i < dblValLen; i++) {
						dblTargetVal[i] = dblVal[i];
					}

					val = targetVal;

					defaultFormat = createFloatArrayFormat((Object[])dblTargetVal);
					targetVal = dblTargetVal;
				}

				if (targetVal instanceof Object[]) {
					out = String.format(defaultFormat, (Object[])targetVal);
				} else {
					out = String.format(defaultFormat, targetVal);
				}
			}
		}

		if (units != null) {
			out += units.dataToTxt(false, true, false);
		}
		return out;
	}

	public NexusDumpItem(NexusGroupData data, NexusGroupData units, NexusGroupData format, NexusGroupData field_type) {
		super();
		this.data = data;
		this.units = units;
		this.format = format;
		this.field_type = field_type;
	}

	public static String formatIntArray(Object... args) {

		String itemSep = ", ";
		String itemFormat = "%d" + itemSep;
		String format = new String(new char[args.length]).replace("\0", itemFormat);
		format = "[" + format;

		int formatLen = format.length();
		int itemSepLen = itemSep.length();
		if (formatLen >= itemSepLen) {
			format = format.substring(0, formatLen - itemSepLen);
		}
		format += "]";
		return String.format(format, args);
	}

	public static String createIntArrayFormat(Object... args) {

		String itemSep = ", ";
		String itemFormat = "%d" + itemSep;
		String format = new String(new char[args.length]).replace("\0", itemFormat);
		format = "[" + format;

		int formatLen = format.length();
		int itemSepLen = itemSep.length();
		if (formatLen >= itemSepLen) {
			format = format.substring(0, formatLen - itemSepLen);
		}
		format += "]";
		return format;
	}

	public static String createArrayFormat(Object... args) {
		String itemSep = ", ";
		String itemFormat = "";
		if (args instanceof Double[]) {
			itemFormat = "%5.3f";
		} else if (args instanceof Integer[]) {
			itemFormat = "%d";
		}
		itemFormat += itemSep;

		String format = new String(new char[args.length]).replace("\0", itemFormat);
		format = "[" + format;

		int formatLen = format.length();
		int itemSepLen = itemSep.length();
		if (formatLen >= itemSepLen) {
			format = format.substring(0, formatLen - itemSepLen);
		}
		format += "]";
		return format;
	}
	public static String formatFloatArray(Object... args) {
		String itemSep = ", ";
		String itemFormat = "%5.3f" + itemSep;
		String format = new String(new char[args.length]).replace("\0", itemFormat);
		format = "[" + format;

		int formatLen = format.length();
		int itemSepLen = itemSep.length();
		if (formatLen >= itemSepLen) {
			format = format.substring(0, formatLen - itemSepLen);
		}
		format += "]";
		//System.out.println("formatFloatArray: format used = " + format);
		return String.format(format, args);
	}

	public static String formatArray(Object... args) {
		String itemSep = ", ";
		String itemFormat = "";
		if (args instanceof Double[]) {
			itemFormat = "%5.3f";
		} else if (args instanceof Integer[]) {
			itemFormat = "%d";
		}
		itemFormat += itemSep;
		String format = new String(new char[args.length]).replace("\0", itemFormat);
		format = "[" + format;

		int formatLen = format.length();
		int itemSepLen = itemSep.length();
		if (formatLen >= itemSepLen) {
			format = format.substring(0, formatLen - itemSepLen);
		}
		format += "]";
		return String.format(format, args);
	}

	public static String createFloatArrayFormat(Object... args) {
		String itemSep = ", ";
		String itemFormat = "%5.3f" + itemSep;
		String format = new String(new char[args.length]).replace("\0", itemFormat);
		format = "[" + format;

		int formatLen = format.length();
		int itemSepLen = itemSep.length();
		if (formatLen >= itemSepLen) {
			format = format.substring(0, formatLen - itemSepLen);
		}
		format += "]";
		return format;
	}

	public NexusGroupData getFieldType() {
		return field_type;
	}

	public void setFieldType(NexusGroupData field_type) {
		this.field_type = field_type;
	}
}
