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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyString;
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
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

public class NXMetaDataProvider extends FindableBase implements NexusTreeAppender, Map<String, Object> {

	private static final String KEY_SEPARATOR = ".";
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

	// These fields are public, so can be overwritten by client code, including jython scripts
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

	private static final String ATTRIBUTE_KEY_FOR_UNITS = "units";
	private static final String ATTRIBUTE_KEY_FOR_FORMAT = "format";

	private static final String ATTRIBUTE_KEY_FOR_METADATA_TYPE = "metadata_type";
	private static final String ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SUPPLIED = "text";
	private static final String ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SCANNABLE = "scannable";
	private static final String ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SCANNABLE_GROUP = ATTRIBUTE_VALUE_FOR_METADATA_TYPE_SCANNABLE;

	private static final String ATTRIBUTE_KEY_FOR_FIELD_TYPE = "field_type";
	private static final String ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT = "input";
	private static final String ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA = "extra";

	private static final Set<String> SPECIAL_ATTRIBUTE_NAMES = Set.of(
			ATTRIBUTE_KEY_FOR_UNITS, ATTRIBUTE_KEY_FOR_FORMAT,
			ATTRIBUTE_KEY_FOR_FIELD_TYPE, ATTRIBUTE_KEY_FOR_METADATA_TYPE);

	// the old Map<String, Object>, kept for backward compatibility of deprecated Map methods
	private Map<String, Object> metaTextualMap;

	private Map<String, ValueWithUnits> valueWithUnitsMap;

	private List<String> dynamicScannables = Collections.synchronizedList(new ArrayList<>());

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(NXMetaDataProvider.class);

	public NXMetaDataProvider() {
		super();
		reset();
	}

	@Override
	public void appendToTopNode(INexusTree topNode) {
		for (Entry<String, ValueWithUnits> entry : valueWithUnitsMap.entrySet()) {
			topNode.addChildNode(createChildNodeForTextualMetaEntry(topNode,
					entry.getKey(), entry.getValue().value(), entry.getValue().units()));
		}
	}

	private void appendScannables(INexusTree topNode) {
		final Set<String> metaScannableNames = ServiceHolder.getNexusDataWriterConfiguration().getMetadataScannables();
		final List<Scannable> metaScannables = metaScannableNames.stream().map(this::getScannableThrowIfNotFound).toList();

		for (Scannable scn : metaScannables) {
			try {
				final Map<String, Object> scannableMap = createMetaScannableMap(scn);
				final INexusTree childNode = createChildNodeForScannableMetaEntry(scn, topNode, scannableMap);
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

	public void reset() {
		this.metaTextualMap = new HashMap<>();
		this.valueWithUnitsMap = new HashMap<>();
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

	@Deprecated(forRemoval = true, since = "GDA 9.30")
	public Map<String, Object> getMetaTexts() {
		logger.deprecatedMethod("getMetaTexts()", "GDA 9.32", "getMetadataValuesAndUnits()");
		return new HashMap<>(metaTextualMap);
	}

	public Map<String, ValueWithUnits> getMetadataValuesAndUnits() {
		return new HashMap<>(valueWithUnitsMap);
	}

	public void add(String key, Object value, String units) {
		final ValueWithUnits valueWithUnits = new ValueWithUnits(value, units);
		doPut(key, valueWithUnits);
	}

	@Deprecated(forRemoval = true, since = "GDA 9.30")
	public void add(MetaDataUserSuppliedItem userSupplied) {
		logger.deprecatedMethod(ATTRIBUTE_KEY_FOR_FIELD_TYPE, "GDA 9.32", "add(String, Object, String)");
		final ValueWithUnits valueWithUnits = new ValueWithUnits(userSupplied.getValue(), userSupplied.getUnits());
		doPut(userSupplied.getKey(), valueWithUnits);
	}

	// TODO: These map methods still use metaTextualMap for backward compatibility. Methods that
	// return a value would have different value if the new valueAndUnitsMap was used. Those method
	// are marked deprecated, with deprecation logging. Once we are satisfied that they are not
	// used in a way that would be broken by using the new map, we should make that change,
	// and remove the metaTextualMap field.

	@Override
	public int size() {
		return valueWithUnitsMap.size();
	}

	@Override
	public boolean isEmpty() {
		return valueWithUnitsMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return valueWithUnitsMap.containsKey(key);
	}

	@Override
	@Deprecated(forRemoval = false, since = "GDA 9.30")
	public boolean containsValue(Object value) {
		logger.deprecatedMethod("containsValue");
		return metaTextualMap.containsValue(value);
		// TODO: once we are sure that this method is not called externally, switch to using valueAndUnitsMap
//		return valueAndUnitsMap.containsValue(value);
	}

	@Override
	@Deprecated(forRemoval = false, since="GDA 9.30")
	public Object get(Object key) {
		logger.deprecatedMethod("get(Object)");
		return metaTextualMap.get(key);
		// TODO: once we are sure that this method is not called externally, switch to using valueAndUnitsMap
//		return valueAndUnitsMap.get(value);
	}

	@Override
	@Deprecated(forRemoval = false, since="GDA 9.30")
	public Object put(String key, Object value) {
		logger.deprecatedMethod("put(Object)");
		return doPut(key, value);
		// TODO: once we are sure that this method is not called externally, switch to using valueAndUnitsMap
//		return valueWithUnitsMap.put(key, value instanceof ValueWithUnits valueWithUnits ?
//				valueWithUnits : new ValueWithUnits(value, null));
	}

	private Object doPut(String key, Object value) {
		// TODO: remove when metaTextualMap is removed and call put directly instead
		// returns result of metaTextualMap.put() for backward compatibility
		if (value instanceof Pair<?, ?> pair) {
			valueWithUnitsMap.put(key, new ValueWithUnits(pair.getFirst(), (String) pair.getSecond()));
			return metaTextualMap.put(key, value);
		} else if (value instanceof ValueWithUnits valueWithUnitsRec) {
			valueWithUnitsMap.put(key, valueWithUnitsRec);
			return metaTextualMap.put(key, new Pair<>(valueWithUnitsRec.value(), valueWithUnitsRec.units()));
		} else {
			valueWithUnitsMap.put(key, new ValueWithUnits(value, null));
			return metaTextualMap.put(key, value);
		}
	}

	@Override
	@Deprecated(forRemoval = false, since="GDA 9.30")
	public Object remove(Object key) {
		logger.deprecatedMethod(ATTRIBUTE_KEY_FOR_FIELD_TYPE);
		// TODO: once we are sure that this method is not called externally, switch to using valueAndUnitsMap
		valueWithUnitsMap.remove(key);
		return metaTextualMap.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		m.entrySet().forEach(entry -> put(entry.getKey(), entry.getValue()));
	}

	@Override
	public void clear() {
		metaTextualMap.clear();
		valueWithUnitsMap.clear();
	}

	@Override
	public Set<String> keySet() {
		return valueWithUnitsMap.keySet();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Deprecated(forRemoval =  false, since = "GDA 9.30")
	public Collection<Object> values() {
		// TODO: once we're sure this method is not called externally, switch to using valueAndUnitsMap
		return (Collection<Object>) (Collection<?>) metaTextualMap.values();
//		valueAndUnitsMap.values().stream().map(ValueWithUnits::value).toList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		return (Set<Map.Entry<String, Object>>) (Set<?>) metaTextualMap.entrySet();
	}

	@Override
	@Deprecated(forRemoval = false, since = "GDA 9.30")
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
		final Set<String> metaScannableSet = ServiceHolder.getNexusDataWriterConfiguration().getMetadataScannables();
		return metaScannableSet.stream().map(this::getScannable).filter(Objects::nonNull).toList();
	}

	private Scannable getScannable(String scannableName) {
		return getScannable(scannableName, false);
	}

	private Scannable getScannableThrowIfNotFound(String scannableName) {
		return getScannable(scannableName, true);
	}

	private Scannable getScannable(String scannableName, boolean throwIfNotFound) {
		try {
			final Scannable scannable = (Scannable) InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
			if (scannable == null) {
				if (throwIfNotFound) {
					throw new IllegalStateException("could not find scannable '" + scannableName + "' in Jython namespace.");
				}
				logger.warn("Scannable '{}' is not in Jython namespace - it will not be included in metadata", scannableName);
			}
			return scannable;
		} catch (ClassCastException e) {
			throw new RuntimeException("Error converting " + scannableName + " to a scannable", e);
		}
	}

	/*
	 * To be called by meta_ls command
	 */
	public String list(boolean withValues) {
		final String itemSeparator = withValues ? llNextItemSeparator : lsNextItemSeparator;
		return concatenateContentsForList(withValues, preamble, itemSeparator, llMidConnector);
	}

	/**
	 * @deprecated client code should not call this method directly, instead call {@link #list(boolean)}
	 */
	@SuppressWarnings("unused")
	@Deprecated(forRemoval = true, since = "GDA 9.30")
	public String concatenateContentsForList(boolean withValues, String preamble, String lsNextItemSeparator,
			String llMidConnector, String llNextItemSeparator) {
		logger.deprecatedMethod("concatenateContentsForList(boolean, String, String, String, String)", "GDA 9.30", "list(boolean)");
		// Note: when this method is removed, move content of public concatenateContentsForListImpl method directly into list(boolean)
		// and use the fields for the separators directly
		return list(withValues);
	}

	private String concatenateContentsForList(boolean withValues, String preamble, String itemSeparator, String nameValueSeparator) {
		final INexusTree listTree = new NexusTreeNode("list", NexusExtractor.NXCollectionClassName, null);
		appendToTopNode(listTree);
		appendScannables(listTree);

		return preamble + nexusTreeToItemList(listTree).stream()
				.sorted()
				.map(d -> d.name + (withValues ? nameValueSeparator + d.value : ""))
				.collect(joining(itemSeparator));
	}

	public void add(Object... args) {
		if (args[0] instanceof Scannable scannable && args.length == 1) {
			add(scannable);
		} else if (args[0] instanceof String string && args.length == 2) {
			add(string, args[1], null);
		} else if (args[0] instanceof String string1 && args.length == 3 && args[2] instanceof String string2) {
			add(string1, args[1], string2);
		} else {
			for (Object arg : args) {
				if (arg instanceof Scannable scannable) {
					add(scannable);
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
			if (arg instanceof Scannable scannable) {
				remove(scannable);
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
		return Stream.of(getScannableInputNames(scannable), getScannableExtraNames(scannable)).flatMap(List::stream).toList();
	}

	public List<String> getScannableInputNames(Scannable scannable) {
		return scannable instanceof Detector ? Collections.emptyList() : Arrays.asList(scannable.getInputNames());
	}

	public List<String> getScannableExtraNames(Scannable scannable) {
		final String[] extraNames = scannable.getExtraNames();
		return (scannable instanceof Detector && extraNames.length == 0) ? List.of(scannable.getName())
				: Arrays.asList(extraNames);
	}

	public INexusTree createChildNodeForTextualMetaEntry(INexusTree parentNode, String name, Object value, String units) {
		final String childNodeName = name;

		final NexusGroupData groupData = createNexusGroupData(value);
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
					} catch (DeviceException e) {
						logger.error("Could not get units for scannable: {}", scannable.getName(), e);
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
				whoami = ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT;
			} else if (extraNames.size() == 1) {
				key = extraNames.get(fieldIndex);
				whoami = ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA;
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

					if (whoami.equals(ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT)) {
						node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_FIELD_TYPE, NexusExtractor.AttrClassName,
								node, new NexusGroupData(ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT)));
					} else if (whoami.equals(ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA)) {
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
		// if there are multiple field names, or the single field name is neither the scannable name nor 'value'
		final List<String> fieldNames = getScannableFieldNames(scannable);
		return fieldNames.size() != 1 || !(fieldNames.get(0).equals(scannable.getName()) || fieldNames.get(0).equals(Scannable.DEFAULT_INPUT_NAME));
	}

	public NexusGroupData createNexusGroupData(Object object) {
		if (object instanceof String string) {
			return new NexusGroupData(string);
		} else if (object instanceof PyString pyString) {
			return new NexusGroupData(pyString.getString());
		} else if (object instanceof Integer integer) {
			return new NexusGroupData(integer);
		} else if (object instanceof Number number) {
			return new NexusGroupData(number.doubleValue());
		} else if (object instanceof double[] doubleArr) {
			return new NexusGroupData(doubleArr);
		} else if (object instanceof int[] intArr) {
			return new NexusGroupData(intArr);
		} else if (object instanceof PyFloat pyFloat) {
			return new NexusGroupData(pyFloat.asDouble());
		} else if (object instanceof PyInteger) {
			// store as NX_FLOAT64 since a lot of things may pass an int for an expect a double on readback
			return new NexusGroupData((double) ((PyInteger) object).getValue());
		} else if (object instanceof long[] data) {
			final int dataLen = data.length;
			final double[] dblData = new double[dataLen];
			System.arraycopy(data, 0, dblData, 0, dataLen);
			return new NexusGroupData(dblData);
		} else if (object instanceof Number[] data) {
			int dataLen = data.length;
			double[] dblData = new double[dataLen];
			for (int i = 0; i < dataLen; i++) {
				dblData[i] = data[i].doubleValue();
			}
			return new NexusGroupData(dblData);
		} else if (object instanceof PySequence pySeq) {
			// coerce PySequence into double array.
			if(pySeq.__len__() == 0) {
				return new NexusGroupData();
			} else if(pySeq.__finditem__(0) instanceof PySequence) {
				final int dataLen = pySeq.__len__();
				int dataHeight = pySeq.__finditem__(0).__len__();
				final double[][] dblData = new double[dataLen][dataHeight];
				for (int i = 0; i < dataLen; i++) {
					dblData[i] = getSequenceAsArray((PySequence) pySeq.__finditem__(i));
				}
				return new NexusGroupData(dblData);
			} else {
				return new NexusGroupData(getSequenceAsArray(pySeq));
			}
		}

		logger.error("unhandled data type: {} - this dataset might not have been written correctly to Nexus file.",
				object.getClass().getName());
		return new NexusGroupData(object.toString());
	}

	private double[] getSequenceAsArray(PySequence pySeq) {
		final int dataLen = pySeq.__len__();
		final double[] dblData = new double[dataLen];
		for (int i = 0; i < dataLen; i++) {
			final PyObject item = pySeq.__finditem__(i);
			if (item instanceof PyNone) {
				dblData[i] = Double.NaN;
			} else {
				dblData[i] = Double.valueOf(item.toString());
			}
		}
		return dblData;
	}

	public Object[] separateGetPositionOutputIntoElementalPosObjects(Object scannableGetPositionOut) {
		if (scannableGetPositionOut == null)
			return new Object[] {};

		final Object[] elements;
		if (scannableGetPositionOut instanceof Object[] objectArr) {
			elements = objectArr;
		} else if (scannableGetPositionOut instanceof PySequence pySeq) {
			final int len = pySeq.__len__();
			elements = new Object[len];
			for (int i = 0; i < len; i++) {
				elements[i] = pySeq.__finditem__(i);
			}
		} else if (scannableGetPositionOut.getClass().isArray()) {
			final int len = ArrayUtils.getLength(scannableGetPositionOut);
			elements = new Object[len];
			for (int i = 0; i < len; i++) {
				elements[i] = Array.get(scannableGetPositionOut, i);
			}
		} else if (scannableGetPositionOut instanceof PlottableDetectorData plottableData) {
			elements = plottableData.getDoubleVals();
		} else {
			elements = new Object[] { scannableGetPositionOut };
		}
		return elements;
	}

	public static String getScannableUnit(Scannable scannable) throws DeviceException {
		if (scannable instanceof ScannableMotionUnits scannableMotionUnits) {
			return scannableMotionUnits.getUserUnits();
		}

		final Object attribute = scannable.getAttribute(ScannableMotionUnits.USERUNITS);
		if (attribute != null) {
			return attribute.toString();
		}

		return null;
	}

	public INexusTree createChildNodeForMetaTextEntry(String name, String nxClass, INexusTree parentNode,
			NexusGroupData groupData) {
		return new NexusTreeNode(name, nxClass, parentNode, groupData);
	}

	/**
	 * @param modificationsMap
	 * @deprecated the map (formattingMap) that we modified here was never read. It has been deleted.
	 */
	@Deprecated(forRemoval = true, since = "GDA9.30")
	public void modifyFormattingMap(@SuppressWarnings("unused") Map<String, String> modificationsMap) {
		logger.deprecatedMethod("modifyFormattingMap(Map<String, String>)", "GDA 9.31", null);
	}

	/**
	 * A simple record to store a value together with its associated units.
	 */
	public record ValueWithUnits(Object value, String units) {
		// no content required (yet)
	}

	/**
	 * Traverses the nexus tree to produce a list of items.
	 *
	 * Name is a concatenation of nexus groups names, separated by a dot, followed by the item name the entry is the
	 * SDS item plus the format attribute as a String
	 */
	private List<MetadataStringItem> nexusTreeToItemList(INexusTree tree) {
		final int nNodes = tree.getNumberOfChildNodes();
		final List<MetadataStringItem> items = new ArrayList<>();
		for (int i = 0; i < nNodes; i++) {
			final INexusTree node = tree.getChildNode(i);
			traverse(node, "", items);
		}
		return items;
	}

	private void traverse(INexusTree tree, String key, List<MetadataStringItem> itemList) {
		if (tree == null) {
			return;
		}

		if (shouldTraverse(tree)) {
			key += tree.getName() + KEY_SEPARATOR;
			for (int i = 0; i < tree.getNumberOfChildNodes(); i++) {
				final INexusTree node = tree.getChildNode(i);
				traverse(node, key, itemList);
			}
		} else if (shouldHarvest(tree)) {
			itemList.add(createMetadataItem(tree, key));
		}
	}

	private MetadataStringItem createMetadataItem(INexusTree tree, String key) {
		final NexusGroupData ngdData = tree.getData();
		final Map<String, NexusGroupData> ngdMap = new HashMap<>();
		for (int i = 0; i < tree.getNumberOfChildNodes(); i++) {
			INexusTree node = tree.getChildNode(i);
			ngdMap.put(node.getName(), node.getData());
		}

		final NexusGroupData ngdUnits = ngdMap.get(ATTRIBUTE_KEY_FOR_UNITS);
		final NexusGroupData ngdFormat = ngdMap.get(ATTRIBUTE_KEY_FOR_FORMAT);
		final NexusGroupData ngdFieldType = ngdMap.get(ATTRIBUTE_KEY_FOR_FIELD_TYPE);

		final String valueStr = toValueString(ngdData, ngdUnits, ngdFormat);
		final FieldType fieldType = toFieldType(ngdFieldType);
		return new MetadataStringItem(key + tree.getName(), valueStr, fieldType);
	}

	private FieldType toFieldType(NexusGroupData ngdFieldType) {
		if (ngdFieldType == null) return FieldType.NONE;
		return switch (ngdFieldType.toString()) {
			case NXMetaDataProvider.ATTRIBUTE_VALUE_FOR_FIELD_TYPE_INPUT -> FieldType.INPUT;
			case NXMetaDataProvider.ATTRIBUTE_VALUE_FOR_FIELD_TYPE_EXTRA -> FieldType.EXTRA;
			default -> FieldType.NONE;
		};
	}

	private boolean shouldTraverse(INexusTree tree) {
		final int numChildNodes = tree.getNumberOfChildNodes();
		final Map<String, Serializable> attributes = tree.getAttributes();
		if (!attributes.isEmpty() && attributes.size() == numChildNodes) {
			return numChildNodes - getNumSpecialAttributes(tree) > 0;
		}

		return numChildNodes > 0;
	}

	private boolean shouldHarvest(INexusTree tree) {
		final int numChildNodes = tree.getNumberOfChildNodes();
		final Map<String, Serializable> attributes = tree.getAttributes();

		if (!attributes.isEmpty() && attributes.size() == numChildNodes) {
			return numChildNodes - getNumSpecialAttributes(tree) == 0;
		}
		return numChildNodes > 0;
	}

	private int getNumSpecialAttributes(INexusTree tree) {
		return (int) tree.getAttributes().keySet().stream().filter(SPECIAL_ATTRIBUTE_NAMES::contains).count();
	}

	private enum FieldType {
		NONE,
		INPUT,
		EXTRA
	}

	private record MetadataStringItem(String name, String value, FieldType type) implements Comparable<MetadataStringItem> {

		@Override
		public int compareTo(MetadataStringItem other) {
			final String thisName = this.name.toLowerCase();
			final String otherName = other.name.toLowerCase();

			if (!getParentName().equals(other.getParentName()) && this.type != other.type) {
				// order by field type within the same scannable
				return this.type.compareTo(other.type);
			}

			// default to standard (lexicographical) string ordering
			return thisName.compareTo(otherName);
		}

		private String getParentName() {
			final int thisLastDotIndex = name().lastIndexOf("\\.");
			return thisLastDotIndex >= 0 ? name().substring(0, thisLastDotIndex) : name();
		}

	}

	private static String toValueString(NexusGroupData data, NexusGroupData units, NexusGroupData format) {
		return toValueString(getValue(data), format) + (units == null ? "" : units.dataToTxt(false, true, false));
	}

	private static Object getValue(NexusGroupData data) {
		if (data == null) return null;
		return data.dimensions.length == 1 && data.dimensions[0] == 1 ?
				data.getFirstValue() : data.getBuffer();
	}

	private static String toValueString(Object value, NexusGroupData format) {
		if (value == null) return "";
		if (format != null) return String.format(format.dataToTxt(false, true, false), value);

		Object targetVal = null;
		String defaultFormat = "";
		if (value instanceof Integer) {
			defaultFormat = "%d";
			targetVal = value;
		} else if (value instanceof Double) {
			defaultFormat = LL_FLOAT_ARRAY_FORMAT;
			targetVal = value;
		} else if (value instanceof String) {
			defaultFormat = "%s";
			targetVal = value;
		} else if (value instanceof byte[] byteArr) {
			defaultFormat = "%s";
			final String stringVal = new String(byteArr);
			targetVal = stringVal;
		} else if (value instanceof int[] intArr) {
			final Integer[] intTargetVal = Arrays.stream(intArr).mapToObj(Integer::valueOf)
					.toArray(Integer[]::new);
			defaultFormat = createIntArrayFormat((Object[]) intTargetVal);
			targetVal = intTargetVal;
		} else if (value instanceof double[] doubleArr) {
			final Double[] doubleTargetVal = Arrays.stream(doubleArr).mapToObj(Double::valueOf)
					.toArray(Double[]::new);
			defaultFormat = createFloatArrayFormat((Object[]) doubleTargetVal);
			targetVal = doubleTargetVal;
		}

		if (targetVal instanceof Object[] objectArr) {
			return String.format(defaultFormat, objectArr);
		} else {
			return String.format(defaultFormat, targetVal);
		}
	}

	private static String createIntArrayFormat(Object... args) {
		return Collections.nCopies(args.length, "%d").stream()
				.collect(joining(LL_ARRAY_ITEM_SEPARATOR, "[", "]"));
	}

	private static String createFloatArrayFormat(Object... args) {
		return Collections.nCopies(args.length, LL_FLOAT_ARRAY_FORMAT).stream()
				.collect(joining(LL_ARRAY_ITEM_SEPARATOR, "[", "]"));
	}

}
