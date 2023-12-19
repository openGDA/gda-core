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
import java.util.stream.IntStream;
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
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeAppender;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.scan.datawriter.NexusDataWriterConfiguration;
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

	private static final String ATTRIBUTE_KEY_FOR_UNITS = "units";
	private static final String ATTRIBUTE_KEY_FOR_FORMAT = "format";

	private static final String KEY_SEPARATOR = ".";
	private static final String DEFAULT_PREAMBLE = "meta:\n";
	private static final String DEFAULT_GROUP_ITEM_SEPARATOR = ".";
	private static final String DEFAULT_FIELD_ITEM_SEPARATOR = ".";
	private static final String DEFAULT_ITEM_SEPARATOR = "\n";
	private static final String DEFAULT_NAME_VALUE_SEPARATOR = " = ";
	private static final String DEFAULT_UNITS_SEPARATOR = " ";
	private static final String DEFAULT_ARRAY_OPEN = "[";
	private static final String DEFAULT_ARRAY_CLOSE = "]";
	private static final String DEFAULT_ARRAY_ITEM_SEPARATOR = ", ";
	private static final String DEFAULT_FLOAT_ARRAY_FORMAT = "%5.3f";
	private static final String DEFAULT_INT_ARRAY_FORMAT = "%d";

	private String preamble = DEFAULT_PREAMBLE;
	private String withValueItemSeparator = DEFAULT_ITEM_SEPARATOR; // separates each metadata item from next
	private String namesOnlyItemSeparator = DEFAULT_ITEM_SEPARATOR;
	private String nameValueSeparator = DEFAULT_NAME_VALUE_SEPARATOR; // separates item name from value

	// TODO: fields below not used in this class, they should be removed
	private String groupItemSeparator = DEFAULT_GROUP_ITEM_SEPARATOR;
	private String fieldItemSeparator = DEFAULT_FIELD_ITEM_SEPARATOR;
	private String unitsSeparator = DEFAULT_UNITS_SEPARATOR;
	private String arrayOpen = DEFAULT_ARRAY_OPEN;
	private String arrayClose = DEFAULT_ARRAY_CLOSE;
	private String arrayItemSeparator = DEFAULT_ARRAY_ITEM_SEPARATOR;
	private String floatArrayFormat = DEFAULT_FLOAT_ARRAY_FORMAT;
	private String intArrayFormat = DEFAULT_INT_ARRAY_FORMAT;

	// the old Map<String, Object>, kept for backward compatibility of deprecated Map methods
	private Map<String, Object> metaTextualMap;
	private Map<String, ValueWithUnits> valueWithUnitsMap;

	private List<String> dynamicScannables = Collections.synchronizedList(new ArrayList<>());

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(NXMetaDataProvider.class);

	public NXMetaDataProvider() {
		super();
		reset();
	}

	/*
	 * The setter methods below set properties related to customising how metadata is
	 * converted to string format. It was formerly accessed via public fields.
	 * Most of these were not actually used and have been removed.
	 * For properties that are used, the fields are now private.
	 * The setter methods can replace the public fields are these fields are not set
	 * from Java code, and where they may be set via Jython, Jython can handle this
	 * by automatically calling the new setter method instead.
	 *
	 * All of these methods are deprecated including, as it is unlikely that the ability to
	 * these properties are used, and they make this class unnecessarily complicated.
	 */

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public void setGroupItemSeparator(@SuppressWarnings("unused") String groupItemSeparator) {
		logger.deprecatedMethod("setGroupItemSeparator(String)", "GDA 9.32", null);
		this.groupItemSeparator = groupItemSeparator;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public String getGroupItemSeparator() {
		logger.deprecatedMethod("getGroupItemSeparator()", "GDA 9.32", null);
		return groupItemSeparator;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public void setFieldItemSeparator(String fieldItemSeparator) {
		logger.deprecatedMethod("setFieldItemSeparator(String)", "GDA 9.32", null);
		this.fieldItemSeparator = fieldItemSeparator;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public String getFieldItemSeparator() {
		logger.deprecatedMethod("getFieldItemSeparator()", "GDA 9.32", null);
		return fieldItemSeparator;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public void setPreamble(String preamble) {
		logger.deprecatedMethod("setPreamble(String)", "GDA 9.32", null);
		this.preamble = preamble;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public String getPreamble() {
		logger.deprecatedMethod("getPreamble()", "GDA 9.32", null);
		return preamble;
	}

	@Deprecated(since = "GDA 9.30", forRemoval =  true)
	public void setLsNextItemSeparator(String lsNextItemSeparator) {
		logger.deprecatedMethod("setLsNextItemSeparator(String)", "GDA 9.32", null);
		this.namesOnlyItemSeparator = lsNextItemSeparator;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public String getLsNextItemSeparator() {
		logger.deprecatedMethod("getLsNextItemSeparator()", "GDA 9.32", null);
		return namesOnlyItemSeparator;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public void setLlNextItemSeparator(@SuppressWarnings("unused") String llNextItemSeparator) {
		logger.deprecatedMethod("setLlNextItemSeparator(String)", "GDA 9.32", null);
		withValueItemSeparator = llNextItemSeparator;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public String getLlNextItemSeparator() {
		logger.deprecatedMethod("getLlNextItemSeparator()", "GDA 9.32", null);
		return withValueItemSeparator;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public void setLlMidConnector(String llMidConnector) {
		logger.deprecatedMethod("setLlMidConnector(String)", "GDA 9.32", null);
		this.nameValueSeparator = llMidConnector;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public void setLlUnitsSeparator(String llUnitsSeparator) {
		logger.deprecatedMethod("setLlUnitsSeparator(String)", "GDA 9.32", null);
		this.unitsSeparator = llUnitsSeparator;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public String getLlUnitsSeparator() {
		logger.deprecatedMethod("getLlUnitsSeparator()", "GDA 9.32", null);
		return unitsSeparator;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public void setLlArrayOpen(String llArrayOpen) {
		logger.deprecatedMethod("setLlArrayOpen(String)", "GDA 9.32", null);
		this.arrayOpen = llArrayOpen;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public String getLlArrayOpen() {
		logger.deprecatedMethod("getLlArrayOpen()", "GDA 9.32", null);
		return arrayOpen;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public void setLlArrayClose(String llArrayClose) {
		logger.deprecatedMethod("setLlArrayClose(String)", "GDA 9.32", null);
		this.arrayClose = llArrayClose;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public String getLlArrayClose() {
		logger.deprecatedMethod("getLlArrayClose()", "GDA 9.32", null);
		return arrayClose;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public void setLlArrayItemSeparator(String llArrayItemSeparator) {
		logger.deprecatedMethod("setLlArrayItemSeparator(String)", "GDA 9.32", null);
		this.arrayItemSeparator = llArrayItemSeparator;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public String getLlArrayItemSeparator() {
		logger.deprecatedMethod("getLlArrayItemSeparator()", "GDA 9.32", null);
		return arrayItemSeparator;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public void setFloatArrayFormat(String llFloatArrayFormat) {
		logger.deprecatedMethod("setFloatArrayFormat(String)", "GDA 9.32", null);
		this.floatArrayFormat = llFloatArrayFormat;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public String getFloatArrayFormat() {
		logger.deprecatedMethod("getFloatArrayFormat()", "GDA 9.32", null);
		return floatArrayFormat;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public void setIntArrayFormat(String llIntArrayFormat) {
		logger.deprecatedMethod("setIntArrayFormat(String)", "GDA 9.32", null);
		this.intArrayFormat = llIntArrayFormat;
	}

	@Deprecated(since = "GDA 9.30", forRemoval = true)
	public String getIntArrayFormat() {
		logger.deprecatedMethod("getIntArrayFormat()", "GDA 9.32", null);
		return intArrayFormat;
	}

	@Override
	public void appendToTopNode(INexusTree topNode) {
		for (Entry<String, ValueWithUnits> entry : valueWithUnitsMap.entrySet()) {
			topNode.addChildNode(createTreeNodeForMetadataEntry(entry.getKey(),
					entry.getValue().value(), entry.getValue().units()));
		}
	}

	private List<MetadataStringItem> metaTextualMapToItemList() {
		return valueWithUnitsMap.entrySet().stream()
				.map(entry -> createMetadataItemForTextualMetaEntry(entry.getKey(),
						entry.getValue().value(), entry.getValue().units()))
				.toList();
	}

	private List<MetadataStringItem> createMetadataItemsForScannables() {
		final Set<String> metaScannableNames = NexusDataWriterConfiguration.getInstance().getMetadataScannables();

		return metaScannableNames.stream()
				.map(this::getScannableThrowIfNotFound)
				.map(this::createMetadataItemForScannable)
				.flatMap(List::stream).toList();
	}

	private List<MetadataStringItem> createMetadataItemForScannable(Scannable scannable) {
		try {
			final Map<String, Object> scannableMap = createMetaScannableMap(scannable);
			return createMetadataItemsForScannable(scannable, "", scannableMap);
		} catch (DeviceException e) {
			logger.error("Error creating metadata for scannable {}", scannable.getName(), e);
			return Collections.emptyList();
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
			NexusDataWriterConfiguration.getInstance().removeMetadataScannable(name);
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
		logger.deprecatedMethod("add(MetaDataUserSuppliedItem)", "GDA 9.32", "add(String, Object, String)");
		final ValueWithUnits valueWithUnits = new ValueWithUnits(userSupplied.getValue(), userSupplied.getUnits());
		doPut(userSupplied.getKey(), valueWithUnits);
	}

	@Override
	@Deprecated(forRemoval = false, since = "GDA 9.30")
	public int size() {
		logger.deprecatedMethod("size()");
		return valueWithUnitsMap.size();
	}

	@Override
	@Deprecated(forRemoval = false, since = "GDA 9.30")
	public boolean isEmpty() {
		logger.deprecatedMethod("isEmpty()");
		return valueWithUnitsMap.isEmpty();
	}

	@Override
	@Deprecated(forRemoval = false, since = "GDA 9.30")
	public boolean containsKey(Object key) {
		logger.deprecatedMethod("containsKey(Object)");
		return valueWithUnitsMap.containsKey(key);
	}

	// TODO: These map methods still use metaTextualMap for backward compatibility. Methods that
	// return a value would have different value if the new valueAndUnitsMap was used. Those method
	// are marked deprecated, with deprecation logging. Once we are satisfied that they are not
	// used in a way that would be broken by using the new map, we should make that change,
	// and remove the metaTextualMap field.

	@Override
	@Deprecated(forRemoval = false, since = "GDA 9.30")
	public boolean containsValue(Object value) {
		logger.deprecatedMethod("containsValue(Object)");
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
		logger.deprecatedMethod("remove()");
		// TODO: once we are sure that this method is not called externally, switch to using valueAndUnitsMap
		valueWithUnitsMap.remove(key);
		return metaTextualMap.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		m.entrySet().forEach(entry -> put(entry.getKey(), entry.getValue()));
	}

	@Override
	@Deprecated(forRemoval = false, since = "GDA 9.30")
	public void clear() {
		metaTextualMap.clear();
		valueWithUnitsMap.clear();
	}

	@Override
	@Deprecated(forRemoval = false, since = "GDA 9.30")
	public Set<String> keySet() {
		return valueWithUnitsMap.keySet();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Deprecated(forRemoval =  false, since = "GDA 9.30")
	public Collection<Object> values() {
		// TODO: once we're sure this method is not called externally, switch to using valueAndUnitsMap
		logger.deprecatedMethod("values()");
		return (Collection<Object>) (Collection<?>) metaTextualMap.values();
//		valueAndUnitsMap.values().stream().map(ValueWithUnits::value).toList();
	}

	@SuppressWarnings("unchecked")
	@Deprecated(forRemoval = false, since = "GDA 9.30")
	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		return (Set<Map.Entry<String, Object>>) (Set<?>) metaTextualMap.entrySet();
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
			NexusDataWriterConfiguration.getInstance().addMetadataScannable(scn.getName());
		}
	}

	public List<Scannable> getMetaScannables() {
		final Set<String> metaScannableSet = NexusDataWriterConfiguration.getInstance().getMetadataScannables();
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
		final String itemSeparator = withValues ? withValueItemSeparator : namesOnlyItemSeparator;
		return concatenateContentsForList(withValues, preamble, itemSeparator, nameValueSeparator);
	}

	/**
	 * @deprecated client code should not call this method directly, instead call {@link #list(boolean)}
	 */
	@SuppressWarnings("unused")
	@Deprecated(forRemoval = true, since = "GDA 9.30")
	public String concatenateContentsForList(boolean withValues, String preamble, String lsNextItemSeparator,
			String llMidConnector, String llNextItemSeparator) {
		logger.deprecatedMethod("concatenateContentsForList(boolean, String, String, String, String)", "GDA 9.32", "list(boolean)");
		// Note: when this method is removed, move content of public concatenateContentsForList method directly into list(boolean)
		// and use the fields for the separators directly
		return list(withValues);
	}

	private String concatenateContentsForList(boolean withValues, String preamble, String itemSeparator, String nameValueSeparator) {
		final List<MetadataStringItem> metadataStringItems = new ArrayList<>();
		metadataStringItems.addAll(metaTextualMapToItemList());
		metadataStringItems.addAll(createMetadataItemsForScannables());

		return preamble + metadataStringItems.stream()
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
		NexusDataWriterConfiguration.getInstance().getMetadataScannables().add(scannableName);
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
		NexusDataWriterConfiguration.getInstance().removeMetadataScannable(scannableName);
		dynamicScannables.remove(scannableName);
	}

	@Deprecated(forRemoval = true, since = "GDA 9.30")
	public Map<String, Object> createMetaScannableMap(Scannable scannable) throws DeviceException {
		logger.deprecatedMethod("createMetaScannableMap(Scannable)", "GDA 9.32", null);
		return createPositionMapForScannable(scannable);
	}

	private Map<String, Object> createPositionMapForScannable(Scannable scannable) throws DeviceException {
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

		final Object[] elementalGetPosObjects = ScannableUtils.toObjectArray(scnPos);
		final List<String> fieldNames = getScannableFieldNamesInternal(scannable);

		final Map<String, Object> metaScannableMapObj = new HashMap<>();
		for (int i = 0; i < fieldNames.size(); i++) {
			metaScannableMapObj.put(fieldNames.get(i), elementalGetPosObjects[i]);
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

	@Deprecated(forRemoval = false, since = "GDA 9.30")
	public List<String> getScannableFieldNames(Scannable scannable) {
		// TODO: once we are sure this method is never called, it should be made private and merged with getScannableFieldNamesInternal
		logger.deprecatedMethod("getScannableFieldNames(Scannable)", "GDA 9.32", null);
		return getScannableFieldNamesInternal(scannable);
	}

	private List<String> getScannableFieldNamesInternal(Scannable scannable) {
		return Stream.of(getScannableInputNamesInternal(scannable), getScannableExtraNamesInternal(scannable)).flatMap(List::stream).toList();
	}

	@Deprecated(forRemoval = false, since = "GDA 9.30")
	public List<String> getScannableInputNames(Scannable scannable) {
		// once we are sure this method is never called, it should be made private and merged with getScannableFieldNamesInternal
		logger.deprecatedMethod("getScannableInputNames(Scannable)", "GDA 9.32", null);
		return getScannableInputNamesInternal(scannable);
	}

	private List<String> getScannableInputNamesInternal(Scannable scannable) {
		return scannable instanceof Detector ? Collections.emptyList() : Arrays.asList(scannable.getInputNames());
	}

	@Deprecated(forRemoval = false, since = "GDA 9.30")
	public List<String> getScannableExtraNames(Scannable scannable) {
		// once we are sure this method is never called, it should be made private and merged with getScannableExtraNamesInternal
		logger.deprecatedMethod("getScannableExtraNames(Scannable)", "GDA 9.32", null);
		return getScannableExtraNamesInternal(scannable);
	}

	private List<String> getScannableExtraNamesInternal(Scannable scannable) {
		final String[] extraNames = scannable.getExtraNames();
		return (scannable instanceof Detector && extraNames.length == 0) ? List.of(scannable.getName())
				: Arrays.asList(extraNames);
	}

	private INexusTree createTreeNodeForMetadataEntry(String name, Object value, String units) {
		final NexusGroupData groupData = createNexusGroupDataInternal(value);
		final INexusTree node = new NexusTreeNode(name, NexusExtractor.SDSClassName, null, groupData);

		if (units != null) {
			node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_UNITS, NexusExtractor.AttrClassName, node,
					new NexusGroupData(units)));
		}
		return node;
	}

	@Deprecated(forRemoval = true, since = "GDA 9.30")
	public INexusTree createChildNodeForTextualMetaEntry(INexusTree parentNode, String name, Object value, String units) {
		logger.deprecatedMethod("createChildNodeForTextualMetaEntry(INexusTree, String, Object, String)", "GDA 9.32", null);
		final NexusGroupData groupData = createNexusGroupData(value);
		final INexusTree node = new NexusTreeNode(name, NexusExtractor.SDSClassName, parentNode, groupData);
		if (units != null) {
			node.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_UNITS, NexusExtractor.AttrClassName, node,
					new NexusGroupData(units)));
		}
		return node;
	}

	private MetadataStringItem createMetadataItemForTextualMetaEntry(String name, Object value, String units) {
		final String valueStr = toValueString(value, (String) null) + (units == null ? "" : units);
		return new MetadataStringItem(name, valueStr);
	}

	private List<MetadataStringItem> createMetadataItemsForScannable(Scannable scannable, String key, Map<String, Object> scannableMap) {
		final List<String> fieldNames = ScannableUtils.getScannableFieldNames(scannable);

		final List<MetadataStringItem> metadataItems = new ArrayList<>();
		if (scannable instanceof ScannableGroup scannableGroup) {
			metadataItems.addAll(scannableGroup.getGroupMembers().stream()
				.map(childScannable -> createMetadataItemsForScannable(childScannable, key + scannable.getName() + KEY_SEPARATOR, scannableMap))
				.flatMap(List::stream).toList());
		} else {
			final String scannableKey = hasGenuineMultipleFieldNames(scannable) ? key + scannable.getName() + KEY_SEPARATOR : key;
			final String[] outputFormat = scannable.getOutputFormat();
			return IntStream.range(0, fieldNames.size())
					.mapToObj(fieldIndex -> createMetadataItemForScannableField(scannableKey, // parent name
							fieldNames.get(fieldIndex), // field name
							scannableMap.get(fieldNames.get(fieldIndex)), // position
							getUnitsForScannable(scannable), // units
							outputFormat == null ? null : outputFormat[fieldIndex])) // output format
					.filter(Objects::nonNull).toList();
		}

		return metadataItems;
	}

	@Deprecated(forRemoval = true, since = "GDA 9.30")
	public INexusTree createChildNodeForScannableMetaEntry(Scannable scannable, INexusTree parentNode, Map<String, Object> scannableMap) {
		// This internal helper method should never have been public. It is no longer used internally and can be
		// removed, along with its private helper method createFieldNode below.
		logger.deprecatedMethod("createNexusTreeForScannable", "GDA 9.32", null);
		final List<String> fieldNames = ScannableUtils.getScannableFieldNames(scannable);
		INexusTree node = null;
		if (scannable instanceof IScannableGroup) {
			node = new NexusTreeNode(scannable.getName(), NexusExtractor.NXCollectionClassName, parentNode);
			for (Scannable childScannable : ((ScannableGroup) scannable).getGroupMembersAsArray()) {
				final INexusTree scannableNode = createChildNodeForScannableMetaEntry(childScannable, node, scannableMap);
				if (scannableNode != null) {
					node.addChildNode(scannableNode);
				}
			}
		} else {
			INexusTree nodeToAddTo = parentNode;
			if (hasGenuineMultipleFieldNames(scannable)) {
				node = new NexusTreeNode(scannable.getName(), NexusExtractor.NXCollectionClassName, parentNode);
				nodeToAddTo = node;
			}
			String[] outputFormat = scannable.getOutputFormat();
			int fieldIndex = 0;
			for (String fieldName : fieldNames) {
				Object fieldValue = scannableMap.get(fieldName);
				if (fieldValue != null) {
					String units = getUnitsForScannable(scannable);
					String fieldOutputFormat = outputFormat == null ? null : outputFormat[fieldIndex];
					NexusTreeNode fieldNode = createFieldNode(node, fieldName, fieldValue, units, fieldOutputFormat);
					nodeToAddTo.addChildNode(fieldNode);
				}
				fieldIndex++;
			}
		}
		return node;
	}

	private String getUnitsForScannable(Scannable scannable) {
		String units = null;
		try {
			units = getScannableUnit(scannable);
		} catch (DeviceException e1) {
			logger.error("Could not get units for scannable {}", scannable.getName());
		}
		return units;
	}

	private NexusTreeNode createFieldNode(INexusTree parentNode, String name, Object value, String units, String format) {
		final NexusGroupData groupData = createNexusGroupData(value);
		final NexusTreeNode fieldNode = new NexusTreeNode(name, NexusExtractor.SDSClassName, parentNode, groupData);
		if (units != null && !units.isEmpty()) {
			fieldNode.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_UNITS, NexusExtractor.AttrClassName, fieldNode,
					new NexusGroupData(units)));
		}
		if (format != null) {
			fieldNode.addChildNode(new NexusTreeNode(ATTRIBUTE_KEY_FOR_FORMAT, NexusExtractor.AttrClassName, fieldNode,
					new NexusGroupData(format)));
		}
		return fieldNode;
	}

	public boolean hasGenuineMultipleFieldNames(Scannable scannable) {
		// if there are multiple field names, or the single field name is neither the scannable name nor 'value'
		final List<String> fieldNames = getScannableFieldNamesInternal(scannable);
		return fieldNames.size() != 1 || !(fieldNames.get(0).equals(scannable.getName()) || fieldNames.get(0).equals(Scannable.DEFAULT_INPUT_NAME));
	}

	private MetadataStringItem createMetadataItemForScannableField(String parentName, String name, Object value, String units, String format) {
		if (value == null) return null;

		final String valueStr = toValueString(value, units, format);
		return new MetadataStringItem(parentName + name, valueStr);
	}


	@Deprecated(forRemoval = false, since = "GDA 9.30")
	public NexusGroupData createNexusGroupData(Object object) {
		// once we are sure this method is never called, it should be made private and merged with getNexusGroupDataInternal
		logger.deprecatedMethod("createNexusGroupData(Object)", "GDA 9.32", null);
		return createNexusGroupDataInternal(object);
	}

	public NexusGroupData createNexusGroupDataInternal(Object object) {
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

	@Deprecated(forRemoval = true, since = "GDA 9.30")
	public Object[] separateGetPositionOutputIntoElementalPosObjects(Object scannableGetPositionOut) {
		logger.deprecatedMethod("separateGetPositionOutputIntoElementalPosObjects(Object)",
				"GDA 9.32", "ScannableUtils.toObjectArray(Object)");
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

	@Deprecated(forRemoval = true, since = "GDA 9.30")
	public INexusTree createChildNodeForMetaTextEntry(String name, String nxClass, INexusTree parentNode, NexusGroupData groupData) {
		// not called from java code
		logger.deprecatedMethod("createChildNodeForMetaTextEntry(String, String, INexusTree)", "GDA 9.32", null);
		return new NexusTreeNode(name, nxClass, parentNode, groupData);
	}

	/**
	 * @param modificationsMap
	 * @deprecated the map (formattingMap) that we modified here was never read. It has been deleted.
	 */
	@Deprecated(forRemoval = true, since = "GDA 9.30")
	public void modifyFormattingMap(@SuppressWarnings("unused") Map<String, String> modificationsMap) {
		// not called from java code
		logger.deprecatedMethod("modifyFormattingMap(Map<String, String>)", "GDA 9.32", null);
	}

	/**
	 * A simple record to store a value together with its associated units.
	 */
	public record ValueWithUnits(Object value, String units) {
		// no content required (yet)
	}

	private record MetadataStringItem(String name, String value) implements Comparable<MetadataStringItem> {

		@Override
		public int compareTo(MetadataStringItem other) {
			return this.name().toLowerCase().compareTo(other.name().toLowerCase()); // sort by name only
		}

	}

	private static String toValueString(Object value, String units, String format) {
		return toValueString(value, format) + (units == null ? "" : units);
	}

	private static String toValueString(Object value, String format) {
		if (value == null) {
			return "";
		} else if (format != null) {
			return String.format(format, value);
		} else if (value instanceof Integer intValue) {
			return intValue.toString();
		} else if (value instanceof Double doubleValue) {
			return String.format(DEFAULT_FLOAT_ARRAY_FORMAT, doubleValue);
		} else if (value instanceof String stringValue) {
			return stringValue;
		} else if (value instanceof byte[] byteArr) {
			return new String(byteArr);
		} else if (value instanceof int[] intArr) {
			return Arrays.stream(intArr)
					.mapToObj(Integer::toString)
					.collect(joining(DEFAULT_ARRAY_ITEM_SEPARATOR, "[", "]"));
		} else if (value instanceof double[] doubleArr) {
			return Arrays.stream(doubleArr)
					.mapToObj(d -> String.format(DEFAULT_FLOAT_ARRAY_FORMAT, d))
					.collect(joining(DEFAULT_ARRAY_ITEM_SEPARATOR, "[", "]"));
		} else if (value instanceof Object[]) {
			return ""; // for consistency with previous implementation, not used when called internally
		} else {
			return value.toString();
		}
	}

}
