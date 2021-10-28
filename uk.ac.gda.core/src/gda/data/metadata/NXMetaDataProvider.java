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

import static java.util.stream.Collectors.joining;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.data.PlottableDetectorData;
import gda.data.ServiceHolder;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeAppender;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.scannablegroup.IScannableGroup;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.FindableBase;
import gda.jython.InterfaceProvider;

public class NXMetaDataProvider extends FindableBase implements NexusTreeAppender, Map<String, Object> {

	private static final String GROUP_ITEM_SEPARATOR = "."; // single dot
	private static final String FIELD_ITEM_SEPARATOR = "."; // single dot
	private static final String PREAMBLE = "meta:\n";
	private static final String LS_NEXT_ITEM_SEPARATOR = "\n"; // single new line
	private static final String LL_NEXT_ITEM_SEPARATOR = "\n"; // single new line
	private static final String LL_MID_CONNECTOR = " = ";
	private static final String LL_UNITS_SEPARATOR = " "; // single white space

	private static final String LL_ARRAY_OPEN = "[";
	private static final String LL_ARRAY_CLOSE = "]";
	private static final String LL_ARRAY_ITEM_SEPARATOR = ", "; // single coma followed by single white space
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

	private Map<String, Object> metaTextualMap;

	private boolean withScannables = false;

	private List<String> dynamicScannables = Collections.synchronizedList(new ArrayList<>());

	private static final Logger logger = LoggerFactory.getLogger(NXMetaDataProvider.class);

	public NXMetaDataProvider() {
		super();
		defaultFormattingMap.put("PREAMBLE", "meta:\n"); //
		defaultFormattingMap.put("LS_NEXT_ITEM_SEPARATOR", "\n"); // single new line
		defaultFormattingMap.put("LL_MID_CONNECTOR", " = ");
		defaultFormattingMap.put("LL_NEXT_ITEM_SEPARATOR", "\n"); // single new line
		defaultFormattingMap.put("LL_UNITS_SEPARATOR", " "); // single space
		defaultFormattingMap.put("LL_ARRAY_OPEN", "[");
		defaultFormattingMap.put("LL_ARRAY_CLOSE", "]");
		defaultFormattingMap.put("LL_ARRAY_ITEM_SEPARATOR", ", "); // single coma followed by single space
		defaultFormattingMap.put("LL_FLOAT_ARRAY_FORMAT", "%5.3f");
		defaultFormattingMap.put("LL_INT_ARRAY_FORMAT", "%d");

		reset();
	}

	@Override
	public void appendToTopNode(INexusTree topNode) {
		for (Entry<String, Object> entry : metaTextualMap.entrySet()) {
			final INexusTree childNode = createChildNodeForTextualMetaEntry(entry, topNode);
			if (childNode != null) {
				topNode.addChildNode(childNode);
			} else {
				logger.debug("Nexus tree child node is null for {}", entry.getKey());
			}
		}

		if (withScannables) {
			final List<Scannable> metaScannableList = new ArrayList<>();
			final Set<String> metaScannableSet = ServiceHolder.getNexusDataWriterConfiguration()
					.getMetadataScannables();
			for (String scannableName : metaScannableSet) {
				Scannable scannable = (Scannable) InterfaceProvider.getJythonNamespace()
						.getFromJythonNamespace(scannableName);
				if (scannable == null) {
					throw new IllegalStateException(
							"could not find scannable '" + scannableName + "' in Jython namespace.");
				}
				metaScannableList.add(scannable);
			}

			for (Scannable scn : metaScannableList) {
				try {
					final Map<String, Object> scannableMap = createMetaScannableMap(scn);
					final INexusTree childNode = createChildNodeForScannableMetaEntry(scn, topNode, scannableMap); // TODO
																													// Change
																													// name
					if (childNode != null) {
						topNode.addChildNode(childNode);
					} else {
						logger.debug("Nexus tree child node is null for {}", scn.getName());
					}
				} catch (DeviceException e1) {
					logger.error("Error creating metadata for scannable {}", scn.getName(), e1);
				}

			}
		}
	}

	public void reset() {
		this.metaTextualMap = new HashMap<>();
		this.formattingMap = new HashMap<>();

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
	}

	/**
	 * Removes all scannables added with NXMetaDataProvider.add(..). Does not restore anything that was removed or
	 * overwritten.
	 */
	public void clearDynamicScannableMetadata() {
		for (String name : dynamicScannables) {
			ServiceHolder.getNexusDataWriterConfiguration().removeMetadataScannable(name);
		}
		dynamicScannables.clear();
	}

	public String listAsString(String format, String delimiter) {
		final boolean useDefaultFormat = StringUtils.countOccurrencesOf(format, "%s") != 2;
		final String actualFormat = useDefaultFormat ? "%s:%s" : format;
		final String actualDelimiter = useDefaultFormat ? "," : delimiter;
		return entrySet().stream().map(entry -> String.format(actualFormat, entry.getKey(), entry.getValue()))
				.collect(joining(actualDelimiter));
	}

	public void setMetaTexts(Map<String, String> metaTexts) {
		for (Map.Entry<String, String> entry : metaTexts.entrySet()) {
			add(entry.getKey(), entry.getValue());
		}
	}

	public Map<String, Object> getMetaTexts() {
		return new HashMap<>(metaTextualMap);
	}

	public void add(String key, Object value, String units) {
		final Pair<Object, String> valueWithUnits = new Pair<>(value, units);
		put(key, valueWithUnits);
	}

	public void add(MetaDataUserSuppliedItem userSupplied) {
		final Pair<Object, String> valueWithUnits = new Pair<>(userSupplied.getValue(), userSupplied.getUnits());
		put(userSupplied.getKey(), valueWithUnits);
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
		for (Scannable scn : metaScannables) {
			ServiceHolder.getNexusDataWriterConfiguration().addMetadataScannable(scn.getName());
		}
	}

	public List<Scannable> getMetaScannables() {
		final List<Scannable> metaScannableList = new ArrayList<>();
		final Set<String> metaScannableSet = ServiceHolder.getNexusDataWriterConfiguration().getMetadataScannables();
		for (String scannableName : metaScannableSet) {

			try {
				Scannable scannable = (Scannable) InterfaceProvider.getJythonNamespace()
						.getFromJythonNamespace(scannableName);
				if (scannable == null) {
					logger.warn("Scannable '{}' is not in Jython namespace - it will not be included in metadata",
							scannableName);
				} else {
					metaScannableList.add(scannable);
				}
			} catch (ClassCastException e) {
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
		return concatenateContentsForList(withValues, preamble, lsNextItemSeparator, llMidConnector,
				llNextItemSeparator);
	}

	public String concatenateContentsForList(boolean withValues, String preamble, String lsNextItemSeparator,
			String llMidConnector, String llNextItemSeparator) {
		final INexusTree listTree = new NexusTreeNode("list", NexusExtractor.NXCollectionClassName, null);
		appendToTopNode(listTree);
		final NexusTreeStringDump treeDump = new NexusTreeStringDump(listTree);

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

		final List<DatumForJythonList> alphabeticalOut = new ArrayList<>();
		NexusGroupData ngdFieldType = null;
		for (Pair<String, NexusDumpItem> e : treeDump.getDumpList()) {
			String name = e.getFirst();
			String value = llMidConnectorUsed + e.getSecond().toString() + llNextItemSeparator;
			String fieldType = "";
			ngdFieldType = e.getSecond().getFieldType();
			if (ngdFieldType != null) {
				fieldType = ngdFieldType.toString();
			}
			alphabeticalOut.add(new DatumForJythonList(name, value, fieldType));
		}

		Collections.sort(alphabeticalOut);

		if (withValues) {
			for (DatumForJythonList d : alphabeticalOut) {
				strOut += d.datumName;
				// strOut += llMidConnectorUsed; //already included
				strOut += d.datumValue;
				// strOut += llNextItemSeparatorUsed; //already included
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
					throw new IllegalArgumentException("Invalid argument: " + arg.toString()
							+ " is not a Scannable! Usage: add(String,Object [,String]) or add(Scannable [,Scannable,Scannable...]))");
				}
			}
		}
	}

	public void add(Scannable scannable) {
		final String scannableName = scannable.getName();
		logger.debug("add called on scannable: {}", scannableName);
		dynamicScannables.add(scannableName);
		ServiceHolder.getNexusDataWriterConfiguration().getMetadataScannables().add(scannableName);
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
		String scannableName = scannable.getName();
		logger.debug("remove called on scannable {}", scannableName);
		ServiceHolder.getNexusDataWriterConfiguration().removeMetadataScannable(scannableName);
		dynamicScannables.remove(scannableName);
	}

	public Map<String, Object> createMetaScannableMap(Scannable scannable) throws DeviceException {
		final Map<String, Object> metaScannableMapObj = new HashMap<>();

		Object scnPos = null;
		try {
			scnPos = scannable.getPosition();
		} catch (Exception e) {
			throw new DeviceException("Error calling getPosition on scannable " + scannable.getName(), e);
		}

		if (scnPos == null) {
			// something's wrong in the scannable! log this and call the metadata "null"
			logger.info("Null returned when asking {} for its position", scannable.getName());
			scnPos = "null";
		}

		final Object[] elementalGetPosObjects = separateGetPositionOutputIntoElementalPosObjects(scnPos);
		final List<String> scannableFieldNames = getScannableFieldNames(scannable);

		for (int i = 0; i < scannableFieldNames.size(); i++) {
			metaScannableMapObj.put(scannableFieldNames.get(i), elementalGetPosObjects[i]);
		}

		return metaScannableMapObj;
	}

	@Override
	public String getName() {
		final String name = super.getName();
		if (name == null || name.isEmpty()) {
			logger.warn(
					"getName() called on NXMetaDataProvider when the name has not been set. This may cause problems in the system and should be fixed.");
		}
		return name;
	}

	public List<String> getScannableFieldNames(Scannable scannable) {
		final List<String> fieldNames = new ArrayList<>();

		// if detector the inputNames are not returned in ScanDataPoint so do not add
		final String[] extraNames = scannable.getExtraNames();
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
		return scannable instanceof Detector ? Collections.emptyList() : Arrays.asList(scannable.getInputNames());
	}

	public List<String> getScannableExtraNames(Scannable scannable) {
		final String[] extraNames = scannable.getExtraNames();
		return (scannable instanceof Detector && extraNames.length == 0) ? List.of(scannable.getName())
				: Arrays.asList(extraNames);
	}

	public INexusTree createChildNodeForTextualMetaEntry(Entry<String, Object> entry, INexusTree parentNode) {
		final String childNodeName = entry.getKey();
		Object object = entry.getValue();
		String units = "placeholder units";
		if (object instanceof Pair) {
			Pair<?, ?> valueWithUnits = (Pair<?, ?>) entry.getValue();
			object = valueWithUnits.getFirst();
			units = (String) valueWithUnits.getSecond();
		}

		final NexusGroupData groupData = createNexusGroupData(object);
		final INexusTree node = new NexusTreeNode(childNodeName, NexusExtractor.SDSClassName, parentNode, groupData);

		node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_METADATA_TYPE, NexusExtractor.AttrClassName, node,
				new NexusGroupData(ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SUPPLIED)));

		if (units != null) {
			node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_UNITS, NexusExtractor.AttrClassName, node,
					new NexusGroupData(units)));
		}
		return node;
	}

	public INexusTree createChildNodeForScannableMetaEntry(Scannable scannable, INexusTree parentNode,
			Map<String, Object> scannableMap) {
		INexusTree node = null;

		final List<String> fieldNames = ScannableUtils.getScannableFieldNames(List.of(scannable));
		final List<String> inputNames = getScannableInputNames(scannable);
		final List<String> extraNames = getScannableExtraNames(scannable);

		if (inputNames.size() + extraNames.size() != fieldNames.size()) {
			logger.error("Field names size was {}, expected {} (sum of input names size {} and extra name size {}",
					fieldNames.size(), inputNames.size() + extraNames.size(), inputNames.size(), extraNames.size());
		}

		if (scannable instanceof IScannableGroup) {
			node = new NexusTreeNode(scannable.getName(), NexusExtractor.NXCollectionClassName, parentNode);
			node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_METADATA_TYPE, NexusExtractor.AttrClassName, node,
					new NexusGroupData(ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SCANNABLE_GROUP)));

			for (Scannable childScannable : ((ScannableGroup) scannable).getGroupMembersAsArray()) {
				final INexusTree scannableNode = createChildNodeForScannableMetaEntry(childScannable, node,
						scannableMap);
				if (scannableNode != null) {
					node.addChildNode(scannableNode);
				}
			}
		} else if (hasGenuineMultipleFieldNames(scannable)) {
			node = new NexusTreeNode(scannable.getName(), NexusExtractor.NXCollectionClassName, parentNode);

			node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_METADATA_TYPE, NexusExtractor.AttrClassName, node,
					new NexusGroupData(ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SCANNABLE)));

			String[] outputFormat = null;
			outputFormat = scannable.getOutputFormat();

			int fieldIndex = 0;
			for (String field : inputNames) {
				Object posObj = scannableMap.get(field);
				String units = null;

				if (posObj != null) {
					try {
						units = getScannableUnit(scannable);
					} catch (DeviceException e1) {
						logger.error("Error getting scannable unit", e1);
					}

					NexusGroupData groupData = null;
					groupData = createNexusGroupData(posObj);
					if (groupData != null) {
						NexusTreeNode fieldNode = new NexusTreeNode(field, NexusExtractor.SDSClassName, node,
								groupData);
						fieldNode.addChildNode(
								new NexusTreeNode(ATTRIBUTE_KEY_FOR_FIELD_TYPE, NexusExtractor.AttrClassName, fieldNode,
										new NexusGroupData(ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT)));

						if (units != null) {
							fieldNode.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_UNITS,
									NexusExtractor.AttrClassName, fieldNode, new NexusGroupData(units)));
						}
						if (outputFormat != null && outputFormat[fieldIndex] != null) {
							fieldNode.addChildNode(
									new NexusTreeNode(ATTRIBUTE_KEY_FOR_FORMAT, NexusExtractor.AttrClassName, fieldNode,
											new NexusGroupData(outputFormat[fieldIndex])));
						}
						node.addChildNode(fieldNode);
					} else {
						logger.warn("GroupData is null!");
					}
				}
				fieldIndex += 1;
			}

			for (String field : extraNames) {
				String key = field;
				Object posObj = scannableMap.get(key);
				String units = null;

				if (posObj != null) {
					try {
						units = getScannableUnit(scannable);
					} catch (DeviceException e1) {
						// TODO Auto-generated catch block
						logger.error("TODO put description of error here", e1);
					}

					NexusGroupData groupData = null;
					groupData = createNexusGroupData(posObj);
					if (groupData != null) {
						NexusTreeNode fieldNode = new NexusTreeNode(field, NexusExtractor.SDSClassName, node,
								groupData);
						fieldNode.addChildNode(
								new NexusTreeNode(ATTRIBUTE_KEY_FOR_FIELD_TYPE, NexusExtractor.AttrClassName, fieldNode,
										new NexusGroupData(ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA)));
						if (units != null) {
							fieldNode.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_UNITS,
									NexusExtractor.AttrClassName, fieldNode, new NexusGroupData(units)));
						}
						if (outputFormat != null && outputFormat[fieldIndex] != null) {
							fieldNode.addChildNode(
									new NexusTreeNode(ATTRIBUTE_KEY_FOR_FORMAT, NexusExtractor.AttrClassName, fieldNode,
											new NexusGroupData(outputFormat[fieldIndex])));
						}
						node.addChildNode(fieldNode);
					} else {
						logger.warn("GroupData is null!");
					}
				}
				fieldIndex += 1;
			}

		} else {
			String key = null;
			int fieldIndex = 0;
			String whoami = "";
			if (inputNames.size() == 1) {
				key = inputNames.get(fieldIndex);
				whoami = "input";
			} else if (extraNames.size() == 1) {
				key = extraNames.get(fieldIndex);
				whoami = "extra";
			} else {
				key = scannable.getName();
			}

			final String[] outputFormat = scannable.getOutputFormat();
			final Object posObj = scannableMap.get(key);
			if (posObj != null) {
				String units = null;
				try {
					units = getScannableUnit(scannable);
				} catch (DeviceException e1) {
					logger.error("Could not get units for scannable {}", scannable.getName());
				}

				final NexusGroupData groupData = createNexusGroupData(posObj);
				if (groupData != null) {
					node = new NexusTreeNode(key, NexusExtractor.SDSClassName, parentNode, groupData);

					if (parentNode.getAttribute(ATTRIBUTE_KEY_FOR_METADATA_TYPE) == null) {
						node.addChildNode(
								new NexusTreeNode(ATTRIBUTE_KEY_FOR_METADATA_TYPE, NexusExtractor.AttrClassName, node,
										new NexusGroupData(ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SCANNABLE)));
					}

					if (whoami.equals("input")) {
						node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_FIELD_TYPE, NexusExtractor.AttrClassName,
								node, new NexusGroupData(ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT)));
					} else if (whoami.equals("extra")) {
						node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_FIELD_TYPE, NexusExtractor.AttrClassName,
								node, new NexusGroupData(ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA)));
					}

					if (units != null && units.length() > 0) {
						node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_UNITS, NexusExtractor.AttrClassName, node,
								new NexusGroupData(units)));

						if (outputFormat != null && outputFormat[fieldIndex] != null) {
							node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_FORMAT, NexusExtractor.AttrClassName,
									node, new NexusGroupData(outputFormat[fieldIndex])));
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

	public boolean hasGenuineMultipleFieldNames(Scannable scannable) {
		final List<String> inputNames = getScannableInputNames(scannable);
		final List<String> extraNames = getScannableExtraNames(scannable);

		final int inputSize = inputNames.size();
		final int extraSize = extraNames.size();

		boolean hasRedundantSingleInputName = (inputSize == 1 && extraSize == 0
				&& (scannable.getName().equals(inputNames.get(0))
						|| inputNames.get(0).equals(Scannable.DEFAULT_INPUT_NAME)));
		boolean hasRedundanSingleExtraName = (inputSize == 0 && extraSize == 1
				&& (scannable.getName().equals(extraNames.get(0))
						|| extraNames.get(0).equals(Scannable.DEFAULT_INPUT_NAME)));
		boolean hasRedundantSingleFieldName = (hasRedundantSingleInputName || hasRedundanSingleExtraName);

		return !hasRedundantSingleFieldName;
	}

	public NexusGroupData createNexusGroupData(Object object) {
		NexusGroupData groupData = null;

		if (object instanceof String) {
			groupData = new NexusGroupData((String) object);
		} else if (object instanceof PyString) {
			groupData = new NexusGroupData(((PyString) object).getString());
		} else if (object instanceof Integer) {
			groupData = new NexusGroupData((Integer) object);
			// } else if (object instanceof Long) {
			// Double dblValue = ((Number) object).doubleValue();
			// double[] dblData = new double[] { dblValue };
			// groupData = new NexusGroupData(dblData);
		} else if (object instanceof Number) {
			groupData = new NexusGroupData(((Number) object).doubleValue());
		} else if (object instanceof double[]) {
			groupData = new NexusGroupData((double[]) object);
		} else if (object instanceof int[]) {
			groupData = new NexusGroupData((int[]) object);
		} else if (object instanceof PyFloat) {
			groupData = new NexusGroupData(((PyFloat) object).asDouble());
		} else if (object instanceof PyInteger) {
			// store as NX_FLOAT64 since a lot of things may pass an int for an expect a double on readback
			groupData = new NexusGroupData((double) ((PyInteger) object).getValue());
		} else if (object instanceof long[]) {
			final long[] data = (long[]) object;
			final int dataLen = data.length;
			final double[] dblData = new double[dataLen];
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
			final int dataLen = ((PyList) object).__len__();
			final double[] dblData = new double[dataLen];
			for (int i = 0; i < dataLen; i++) {
				final PyObject item = ((PyList) object).__finditem__(i);
				if (item instanceof PyNone) {
					dblData[i] = Double.NaN;
				} else {
					dblData[i] = Double.valueOf(item.toString());
				}
			}
			groupData = new NexusGroupData(dblData);
		} else if (object instanceof PySequence) {
			// coerce PySequence into double array.
			final int dataLen = ((PySequence) object).__len__();
			final double[] dblData = new double[dataLen];
			for (int i = 0; i < dataLen; i++) {
				final PyObject item = ((PySequence) object).__finditem__(i);
				if (item instanceof PyNone) {
					dblData[i] = Double.NaN;
				} else {
					dblData[i] = Double.valueOf(item.toString());
				}
			}
			groupData = new NexusGroupData(dblData);
		} else {
			logger.error("unhandled data type: {} - this dataset might not have been written correctly to Nexus file.",
					object.getClass().getName());
			groupData = new NexusGroupData(object.toString());
		}
		return groupData;
	}

	public Object[] separateGetPositionOutputIntoElementalPosObjects(Object scannableGetPositionOut) {
		if (scannableGetPositionOut == null)
			return new Object[] {};

		final Object[] elements;
		if (scannableGetPositionOut instanceof Object[]) {
			elements = (Object[]) scannableGetPositionOut;
		} else if (scannableGetPositionOut instanceof PySequence) {
			final PySequence seq = (PySequence) scannableGetPositionOut;
			final int len = seq.__len__();
			elements = new Object[len];
			for (int i = 0; i < len; i++) {
				elements[i] = seq.__finditem__(i);
			}
		} else if (scannableGetPositionOut.getClass().isArray()) {
			final int len = ArrayUtils.getLength(scannableGetPositionOut);
			elements = new Object[len];
			for (int i = 0; i < len; i++) {
				elements[i] = Array.get(scannableGetPositionOut, i);
			}
		} else if (scannableGetPositionOut instanceof PlottableDetectorData) {
			elements = ((PlottableDetectorData) scannableGetPositionOut).getDoubleVals();
		} else {
			elements = new Object[] { scannableGetPositionOut };
		}
		return elements;
	}

	public static String getScannableUnit(Scannable s) throws DeviceException {
		if (s instanceof ScannableMotionUnits) {
			return ((ScannableMotionUnits) s).getUserUnits();
		}

		final Object attribute = s.getAttribute(ScannableMotionUnits.USERUNITS);
		if (attribute != null) {
			return attribute.toString();
		}

		return null;
	}

	public INexusTree createChildNodeForMetaTextEntry(String name, String nxClass, INexusTree parentNode,
			NexusGroupData groupData) {
		return new NexusTreeNode(name, nxClass, parentNode, groupData);
	}

	public void modifyFormattingMap(Map<String, String> modificationsMap) {
		for (Entry<String, String> e : modificationsMap.entrySet()) {
			this.formattingMap.put(e.getKey(), e.getValue());
		}
	}

	/**
	 * class to create a map of entries from the nexus tree
	 *
	 * A key is a concatenation of nexus groups names, separated by a dot, followed by the item name the entry is the
	 * SDS item plus the format attribute as a String
	 */
	private static class NexusTreeStringDump {
		private static final String KEY_SEPARATOR = ".";
		private final INexusTree tree;
		private final Map<String, NexusDumpItem> dumpMap;
		private final List<Pair<String, NexusDumpItem>> dumpList;

		public NexusTreeStringDump(INexusTree tree) {
			super();
			this.tree = tree;
			this.dumpMap = new HashMap<>();
			this.dumpList = new ArrayList<>();
			traverse();
		}

		@Override
		public String toString() {
			return "NexusTreeStringDump [tree=" + tree + "]";
		}

		private void traverse() {
			if (this.tree != null) {
				final int nNodes = this.tree.getNumberOfChildNodes();
				for (int i = 0; i < nNodes; i++) {
					final INexusTree node = this.tree.getChildNode(i);
					traverse(node, "");
				}
			}
		}

		private void traverse(INexusTree tree, String key) {
			if (tree == null) {
				return;
			}

			if (isToBeTraversed(tree)) {
				final int nNodes = tree.getNumberOfChildNodes();
				key += tree.getName() + KEY_SEPARATOR;
				if (nNodes > 0) {
					for (int i = 0; i < nNodes; i++) {
						final INexusTree node = tree.getChildNode(i);
						traverse(node, key);
					}
				}
			} else if (isToBeHarvested(tree)) {
				key += tree.getName();
				final int nNodes = tree.getNumberOfChildNodes();

				final NexusGroupData ngdData = tree.getData();
				final Map<String, NexusGroupData> ngdMap = new HashMap<>();
				for (int i = 0; i < nNodes; i++) {
					INexusTree node = tree.getChildNode(i);
					ngdMap.put(node.getName(), node.getData());
				}

				final NexusGroupData ngdUnits = ngdMap.get("units");
				final NexusGroupData ngdFormat = ngdMap.get("format");
				final NexusGroupData ngdFieldType = ngdMap.get("field_type");

				final NexusDumpItem item = new NexusDumpItem(ngdData, ngdUnits, ngdFormat, ngdFieldType);
				dumpMap.put(key, new NexusDumpItem(ngdData, ngdUnits, ngdFormat, ngdFieldType));
				final Pair<String, NexusDumpItem> e = new Pair<>(key, item);
				dumpList.add(e);
			}
		}

		public List<Pair<String, NexusDumpItem>> getDumpList() {
			return dumpList;
		}

		public boolean isToBeTraversed(INexusTree tree) {
			final int numChildNodes = tree.getNumberOfChildNodes();
			final Map<String, Serializable> attributes = tree.getAttributes();
			if (attributes != null && attributes.size() == numChildNodes) {
				Serializable units = attributes.get("units");
				Serializable format = attributes.get("format");
				Serializable field_t = attributes.get("field_type");
				Serializable metadata_t = attributes.get("metadata_type");

				int nodesToBeTraversed = numChildNodes;
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
				return (nodesToBeTraversed > 0);
			}

			return numChildNodes > 0;
		}

		public boolean isToBeHarvested(INexusTree tree) {
			final int numChildNodes = tree.getNumberOfChildNodes();
			final Map<String, Serializable> attributes = tree.getAttributes();
			if (attributes != null && attributes.size() == numChildNodes) {
				Serializable units = attributes.get("units");
				Serializable format = attributes.get("format");
				Serializable field_t = attributes.get("field_type");
				Serializable metadata_t = attributes.get("metadata_type");

				int nodesRemaining = numChildNodes;
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
				return nodesRemaining == 0;
			}
			return numChildNodes > 0;
		}

	}

	private static class DatumForJythonList implements Comparable<DatumForJythonList> {
		private final String datumName; // ls part
		private final String datumValue; // ll extension
		private final String datumFieldType; // input or extra (for scannable)

		public DatumForJythonList(String name, String value, String fieldType) {
			super();
			this.datumName = name;
			this.datumValue = value;
			this.datumFieldType = fieldType;
		}

		@Override
		public int compareTo(DatumForJythonList other) {
			final String thisName = this.datumName;
			final String otherName = other.datumName;
			final String thisType = this.datumFieldType;
			final String otherType = other.datumFieldType;

			final String[] thisSegments = thisName.split("\\.", -1);
			final String[] otherSegments = otherName.split("\\.", -1);

			String thisRoot = thisName;
			final int lastIdx1 = thisName.lastIndexOf("\\.");
			if (lastIdx1 >= 0) {
				thisRoot = thisName.substring(0, lastIdx1);
			}

			String otherRoot = otherName;
			int lastIdx2 = otherName.lastIndexOf("\\.");
			if (lastIdx2 >= 0) {
				otherRoot = otherName.substring(0, lastIdx2);
			}

			if (thisSegments.length != otherSegments.length) {
				return thisName.toLowerCase().compareTo(otherName.toLowerCase());
			}

			// if belong to the same scannable
			if (thisRoot.equals(otherRoot)) {
				if (thisType.equals(NXMetaDataProvider.ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT)
						&& otherType.equals(NXMetaDataProvider.ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA)) {
					// input before extra
					return 1;
				} else if (thisType.equals(NXMetaDataProvider.ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA)
						&& otherType.equals(NXMetaDataProvider.ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT)) {
					// input before extra
					return -1;
				} else {
					return thisName.toLowerCase().compareTo(otherName.toLowerCase());
				}
			}

			return thisName.toLowerCase().compareTo(otherName.toLowerCase());
		}

	}

	/*
	 * Class to create a user friendly string representation of an item
	 */
	private static class NexusDumpItem {
		private final NexusGroupData data;
		private final NexusGroupData units;
		private final NexusGroupData format;
		private final NexusGroupData fieldType;

		public NexusDumpItem(NexusGroupData data, NexusGroupData units, NexusGroupData format,
				NexusGroupData fieldType) {
			super();
			this.data = data;
			this.units = units;
			this.format = format;
			this.fieldType = fieldType;
		}

		@Override
		public String toString() {
			Object targetVal = null;

			String out = "";
			if (data != null) {
				Object val = data.dimensions.length == 1 && data.dimensions[0] == 1 ? data.getFirstValue()
						: data.getBuffer();
				if (format != null) {
					out = String.format(format.dataToTxt(false, true, false), val);
				} else {
					String defaultFormat = "";
					if (val instanceof Integer) {
						defaultFormat = "%d";
						targetVal = val;
					} else if (val instanceof Double) {
						defaultFormat = "%5.3f";
						targetVal = val;
					} else if (val instanceof String) {
						defaultFormat = "%s";
						targetVal = val;
					} else if (val instanceof byte[]) {
						defaultFormat = "%s";
						final String stringVal = new String((byte[]) val);
						targetVal = stringVal;
					} else if (val instanceof int[]) {
						final int[] intVal = (int[]) val;
						final Integer[] intTargetVal = Arrays.stream(intVal).mapToObj(Integer::valueOf)
								.toArray(Integer[]::new);
						defaultFormat = createIntArrayFormat((Object[]) intTargetVal);
						targetVal = intTargetVal;
					} else if (val instanceof double[]) {
						double[] doubleVal = (double[]) val;
						final Double[] doubleTargetVal = Arrays.stream(doubleVal).mapToObj(Double::valueOf)
								.toArray(Double[]::new);
						defaultFormat = createFloatArrayFormat((Object[]) doubleTargetVal);
						targetVal = doubleTargetVal;
					}

					if (targetVal instanceof Object[]) {
						out = String.format(defaultFormat, (Object[]) targetVal);
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

		public String createIntArrayFormat(Object... args) {
			final String itemSep = ", ";
			final String itemFormat = "%d" + itemSep;
			String format = new String(new char[args.length]).replace("\0", itemFormat);
			format = "[" + format;

			final int formatLen = format.length();
			final int itemSepLen = itemSep.length();
			if (formatLen >= itemSepLen) {
				format = format.substring(0, formatLen - itemSepLen);
			}
			format += "]";
			return format;
		}

		public String createFloatArrayFormat(Object... args) {
			final String itemSep = ", ";
			final String itemFormat = "%5.3f" + itemSep;
			String format = new String(new char[args.length]).replace("\0", itemFormat);
			format = "[" + format;

			final int formatLen = format.length();
			final int itemSepLen = itemSep.length();
			if (formatLen >= itemSepLen) {
				format = format.substring(0, formatLen - itemSepLen);
			}
			format += "]";
			return format;
		}

		public NexusGroupData getFieldType() {
			return fieldType;
		}
	}

}
