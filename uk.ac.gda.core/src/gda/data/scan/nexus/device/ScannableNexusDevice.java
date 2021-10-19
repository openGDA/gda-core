/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.data.scan.nexus.device;

import static gda.device.Scannable.ATTR_NEXUS_CATEGORY;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.IMultipleNexusDevice;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.NexusRole;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.CustomNexusEntryModification;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.ServiceHolder;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import gda.util.TypeConverters;
import uk.ac.gda.api.scan.IExplicitScanObject;
import uk.ac.gda.api.scan.IImplicitScanObject;
import uk.ac.gda.api.scan.IScanObject;

/**
 * An instance of this type adapts a {@link Scannable} to {@link INexusDevice}.
 *
 * @param <N>
 */
public class ScannableNexusDevice<N extends NXobject> extends AbstractNexusDeviceAdapter<N> implements IMultipleNexusDevice {

	private static final Logger logger = LoggerFactory.getLogger(ScannableNexusDevice.class);

	/**
	 * The name of the 'scannables' collection. This collection contains all wrapped GDA8
	 * scannables. The reason for this is that unless otherwise specified the nexus object
	 * created for all scannables is an {@link NXpositioner}, even for metadata scannables,
	 * e.g. sample name.
	 */
	public static final String COLLECTION_NAME_SCANNABLES = "scannables";

	/**
	 * The field name 'name' used for the name of the scannable.
	 */
	public static final String FIELD_NAME_NAME = "name";

	/**
	 * The attribute name 'local_name', added to datasets.
	 */
	public static final String ATTR_NAME_LOCAL_NAME = "local_name";

	public static final String ATTR_NAME_GDA_SCANNABLE_NAME = "gda_scannable_name";

	public static final String ATTR_NAME_GDA_SCAN_ROLE = "gda_scan_role";

	public static final String ATTR_NAME_GDA_FIELD_NAME = "gda_field_name";

	/**
	 * The attribute name 'units'.
	 */
	public static final String ATTR_NAME_UNITS = "units";

	/**
	 * The field name 'value_set', used for the requested value of a scannable,
	 * e.g. a motor. Note that this should be a constant in {@link NXpositioner}, but
	 * it hasn't been added yet. When this has happened, the nexus base classes should be
	 * regenerated and the constant from this {@link NXpositioner} used instead.
	 */
	public static final String FIELD_NAME_VALUE_SET = NXpositioner.NX_VALUE + "_set";

	/**
	 * The {@link DataNode}s for each field keyed by input/extra name.
	 * This map has the same iteration order as the values in {@link #getPositionArray(Object)}.
	 * Each data node corresponds to the field with the corresponding index position in the result of
	 * concatenating the arrays returned by {@link Scannable#getInputNames()} and {@link Scannable#getExtraNames()}.
	 * If the scannable role in the scan is {@link NexusRole#PER_POINT}, then each data node will contain
	 * an {@link ILazyWriteableDataset}s, otherwise each data node will contain a simple {@link Dataset}.
	 * At each point of the scan the item with the corresponding index in {@link #getPositionArray(Object)}
	 * is written to each dataset.
	 */
	private LinkedHashMap<String, DataNode> fieldDataNodes = null;

	/**
	 * Whether the demand value (i.e. the position the scannable was set to by the scan) should
	 * be written. Should be set to <code>false</code> if this information is not available in the scan.
	 */
	private boolean writeDemandValue = true;

	/**
	 * The data node for the demand value
	 */
	private DataNode demandValueDataNode = null;

	private int primaryDataFieldIndex = 0;

	private IScanObject scanObject = null;

	public ScannableNexusDevice(Scannable scannable) {
		super(scannable);
	}

	protected Scannable getScannable() {
		return super.getDevice();
	}

	public void setWriteDemandValue(boolean writeDemandValue) {
		this.writeDemandValue = writeDemandValue;
	}

	@Override
	protected void configureNexusWrapper(NexusObjectWrapper<N> nexusWrapper, NexusScanInfo info) throws NexusException {
		nexusWrapper.setCategory(getNexusCategory());

		// add all input fields as axis fields
		final String[] inputFields = getScannable().getInputNames();
		final ScannableNexusDeviceConfiguration config = getScannableNexusDeviceConfiguration().orElseThrow();
		final String[] fieldOutputPaths = config.getFieldPaths();
		final String[] axisFieldNames = new String[inputFields.length];
		for (int fieldIndex = 0; fieldIndex < inputFields.length; fieldIndex++) {
			axisFieldNames[fieldIndex] = fieldIndex < fieldOutputPaths.length ? fieldOutputPaths[fieldIndex] : inputFields[fieldIndex];
		}
		nexusWrapper.addAxisDataFieldNames(axisFieldNames);

		// calculate primary data field name
		primaryDataFieldIndex = calculatePrimaryDataFieldIndex();
		final String primaryDataFieldName = fieldOutputPaths[primaryDataFieldIndex];
		nexusWrapper.setPrimaryDataFieldName(primaryDataFieldName);

		nexusWrapper.setDefaultAxisDataFieldName(shouldWriteDemandValue(info) ?
				FIELD_NAME_VALUE_SET : getPrimaryDataFieldName());
		final String collectionName = getScannableNexusDeviceConfiguration()
				.map(ScannableNexusDeviceConfiguration::getCollectionName)
				.orElseGet(() -> hasLocationMapEntry() ? COLLECTION_NAME_SCANNABLES : null);
		nexusWrapper.setCollectionName(collectionName);
	}

	private int calculatePrimaryDataFieldIndex() {
		if (fieldDataNodes.size() < 2 || scanObject == null) return 0;

		switch (scanObject.getType()) {
			case IMPLICIT:
				return calculatePrimaryDataFieldIndex((IImplicitScanObject) scanObject);
			case EXPLICIT:
				return calculatePrimaryDataFieldIndex((IExplicitScanObject) scanObject);
			default:
				throw new IllegalStateException("Unknown scan object type: " + scanObject.getType());
		}
	}

	private int calculatePrimaryDataFieldIndex(IImplicitScanObject scanObject) {
		if (!scanObject.hasStart() || (!scanObject.hasStop() && !scanObject.hasStep())) {
			return 0; // need start and either stop or step to be specified
		}

		final Object stopStep = scanObject.hasStop() ? scanObject.getStop() : scanObject.getStep();

		final double[] startArray = TypeConverters.toDoubleArray(scanObject.getStart());
		final double[] stopStepArray = TypeConverters.toDoubleArray(stopStep);

		if (startArray.length != stopStepArray.length) {
			throw new IllegalStateException("start and " + (scanObject.hasStop() ? "stop" : "step") +
					" arrays must be of equal length");
		}
		if (startArray.length == 1) return 0;

		return getMaxRangeIndex(startArray, stopStepArray);
	}

	private int calculatePrimaryDataFieldIndex(IExplicitScanObject scanObject) {
		final Iterator<Object> pointIter = scanObject.iterator();
		final double[] firstPoint = TypeConverters.toDoubleArray(pointIter.next());
		final int numFields = firstPoint.length;
		if (numFields < 2) return 0; // if only 1 field, no comparison required

		final double[] minPoints = Arrays.stream(firstPoint).toArray(); // make copies
		final double[] maxPoints = Arrays.stream(firstPoint).toArray();
		while (pointIter.hasNext()) {
			final double[] point = TypeConverters.toDoubleArray(pointIter.next());
			for (int i = 0; i < point.length; i++) { // no nice way to do this with streams
				minPoints[i] = Math.min(minPoints[i], point[i]);
				maxPoints[i] = Math.max(maxPoints[i], point[i]);
			}
		}
		return getMaxRangeIndex(minPoints, maxPoints);
	}

	private int getMaxRangeIndex(double[] first, double[] second) {
		final double[] ranges = IntStream.range(0, first.length)
				.mapToDouble(i -> Math.abs(second[i] - first[i])).toArray();
		// find the index of the maximum range
		return IntStream.range(0, ranges.length).boxed()
				.max(Comparator.comparing(index -> ranges[index]))
				.orElseThrow().intValue();
	}

	@Override
	protected String getPrimaryDataFieldName() {
		return getScannableNexusDeviceConfiguration()
				.map(ScannableNexusDeviceConfiguration::getFieldPaths)
				.filter(fieldPaths -> primaryDataFieldIndex < fieldPaths.length)
				.map(fieldPaths -> fieldPaths[primaryDataFieldIndex])
				.orElse(getScannable().getInputNames()[primaryDataFieldIndex]);
	}

	private Optional<ScannableNexusDeviceConfiguration> getScannableNexusDeviceConfiguration() {
		if (ServiceHolder.getScannableNexusDeviceConfigurationRegistry() == null) {
			return Optional.empty();
		}

		return Optional.ofNullable(ServiceHolder.getScannableNexusDeviceConfigurationRegistry()
				.getScannableNexusDeviceConfiguration(getName()));
	}

	private NexusBaseClass getNexusCategory() throws NexusException {
		try {
			final Optional<NexusBaseClass> optCategory = getScannableNexusDeviceConfiguration().map(ScannableNexusDeviceConfiguration::getNexusCategory);
			if (optCategory.isPresent()) {
				return optCategory.get();
			}

			final Object categoryStr = getScannable().getScanMetadataAttribute(ATTR_NEXUS_CATEGORY);
			if (categoryStr instanceof String) {
				return NexusBaseClass.getBaseClassForName((String) categoryStr);
			}
			return null;
		} catch (Exception e) { // DeviceException, ClassCastException or IllegalArgumentException (from Enum.valueOf)
			throw new NexusException("Error getting Nexus category for device: " + getName(), e);
		}
	}

	public DataNode getFieldDataNode(String outputFieldPath) {
		return fieldDataNodes.get(outputFieldPath);
	}

	@Override
	public NexusObjectProvider<N> getNexusProvider(NexusScanInfo info) throws NexusException {
		// this class now returns multiple nexus providers for scannables with multiple input fields
		throw new UnsupportedOperationException("getNexusProviders() should be called instead of this method");
	}

	@Override
	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {
		final Scannable scannable = getScannable();
		if (scannable instanceof IMultipleNexusDevice) {
			return ((IMultipleNexusDevice)scannable).getNexusProviders(info);
		} else if (scannable instanceof INexusDevice) {
			@SuppressWarnings("unchecked")
			final INexusDevice<N> nexusDevice = (INexusDevice<N>) getScannable();
			return Arrays.asList(nexusDevice.getNexusProvider(info));
		}

		// create the DataNodes for the inputNames, extraNames and demand value if applicable
		createDataNodes(info);

		if (getScannableNexusDeviceConfiguration().isPresent()) {
			// if there is a configuration, create a single nexus object (see DAQ-3761)
			final N nexusObject = createConfiguredNexusObject(info);
			return List.of(createNexusProvider(nexusObject, info));
		}

		// default behaviour - create an NXpositioner for each field
		final List<NexusObjectProvider<?>> nexusProviders = new ArrayList<>();
		final String[] inputNames = scannable.getInputNames();
		final ScanRole scanRole = info.getScanRole(getName());
		for (int i = 0; i < inputNames.length; i++) {
			final boolean isSingleInputField = i == 0 && inputNames.length == 1;
			final N positioner = createPositionerForInputName(inputNames[i], i, scanRole, isSingleInputField);
			nexusProviders.add(createNexusProviderForInputField(inputNames[i], positioner, isSingleInputField));
		}

		// for scannables with multiple (or zero) input fields, create an NXcollection with links and extra fields
		// TODO is this the expected behaviour for no-input field scannables.
		if (scannable.getInputNames().length != 1) { // will also cover no input field case
			nexusProviders.add(createCollection(info));
		}

		return nexusProviders;
	}

	private N createPositionerForInputName(String inputName, int fieldIndex, ScanRole scanRole,
			boolean isSingleInputField) throws NexusException {
		// only honour NexusBaseClass (from Scannable.NX_CLASS attribute) for single field, otherwise the collection uses this.
		// TODO: should still support nexus scan attributes such as NX_CLASS? (DAQ-3776)
		final NexusBaseClass nexusBaseClass = isSingleInputField ? getNexusBaseClass() : NexusBaseClass.NX_POSITIONER;
		final String scannableName = getName();

		@SuppressWarnings("unchecked")
		final N positioner = (N) NexusNodeFactory.createNXobjectForClass(nexusBaseClass);

		positioner.setField(FIELD_NAME_NAME, isSingleInputField ? scannableName : scannableName + "." + inputName);
		// Attributes to identify the scannables so that the nexus file can be reverse engineered
		positioner.setAttribute(null, ATTR_NAME_GDA_SCANNABLE_NAME, scannableName);
		positioner.setAttribute(null, ATTR_NAME_GDA_SCAN_ROLE, scanRole.toString().toLowerCase());

		if (positioner instanceof NXpositioner) {
			writeLimits((NXpositioner) positioner, fieldIndex);
		}

		// create the 'value' data node for this input field
		final String dataNodeName = positioner instanceof NXpositioner ? NXpositioner.NX_VALUE : inputName;
		final DataNode dataNode = fieldDataNodes.get(inputName);
		positioner.addDataNode(dataNodeName, dataNode);

		// create the 'value_set' (demand value) data node if applicable (new scanning only)
		if (isSingleInputField && demandValueDataNode != null) {
			positioner.addDataNode(FIELD_NAME_VALUE_SET, demandValueDataNode);
		}

		if (isSingleInputField) {
			// we don't create an NXcollection for a single field, so add extra fields and attributes here
			addExtraNameFields(positioner);
			registerAttributes(positioner);
		}

		return positioner;
	}

	private void addExtraNameFields(final NXobject nexusObject) {
		// extra name fields are added to the only NXpositioner if there is a single input field, otherwise to an NXcollection
		addFields(getScannable().getExtraNames(), nexusObject);
	}

	private void addAllFields(final NXobject nexusObject) {
		addFields(getFieldNames(), nexusObject);
	}

	private void addFields(String[] fieldNames, NXobject nexusObject) {
		for (String fieldName : fieldNames) {
			final DataNode dataNode = fieldDataNodes.get(fieldName);
			Objects.nonNull(dataNode); // sanity check
			nexusObject.addDataNode(fieldName, dataNode);
		}
	}

	private NexusObjectProvider<N> createNexusProviderForInputField(String inputName,
			N nexusObject, boolean isSingleField) throws NexusException {
		final String groupName = isSingleField ? getName() : getName() + "." + inputName;
		final NexusObjectWrapper<N> nexusWrapper = new NexusObjectWrapper<>(groupName, nexusObject);
		nexusWrapper.setCategory(getNexusCategory());
		if (hasLocationMapEntry()) {
			nexusWrapper.setCollectionName(COLLECTION_NAME_SCANNABLES);
		}

		if (isSingleField) {
			// In this single input field case, this is the only nexus object provider, so we need to configure it so that
			// the NXdata group correctly links to the data nodes for value and target value (if present)
			nexusWrapper.setPrimaryDataFieldName(NXpositioner.NX_VALUE);
			nexusWrapper.addAxisDataFieldName(NXpositioner.NX_VALUE);
			nexusWrapper.setDefaultAxisDataFieldName(demandValueDataNode != null ?
					FIELD_NAME_VALUE_SET : NXpositioner.NX_VALUE);
		}

		return nexusWrapper;
	}

	private NexusObjectProvider<?> createCollection(NexusScanInfo info) throws NexusException {
		final String scannableName = getName();

		// honour NexusBaseClass for no-input field scannables (DAQ-3776)
		final NexusBaseClass nexusBaseClass = getScannable().getInputNames().length == 0 ?
				getNexusBaseClass() : NexusBaseClass.NX_COLLECTION;
		final NXobject nexusObject = NexusNodeFactory.createNXobjectForClass(nexusBaseClass);
		nexusObject.setAttribute(null, ATTR_NAME_GDA_SCANNABLE_NAME, scannableName);
		nexusObject.setAttribute(null, ATTR_NAME_GDA_SCAN_ROLE, info.getScanRole(scannableName).toString().toLowerCase());
		nexusObject.setField(FIELD_NAME_NAME, scannableName);

		// add links to input fields and extra fields
		addAllFields(nexusObject);
		// add extra name fields and attributes
		registerAttributes(nexusObject);

		// create and configure the NexusObjectProvider for the collection.
		final NexusObjectWrapper<?> nexusProvider = new NexusObjectWrapper<>(scannableName, nexusObject);
		if (hasLocationMapEntry()) {
			nexusProvider.setCollectionName(COLLECTION_NAME_SCANNABLES);
		}
		final NexusBaseClass category = getNexusCategory();
		nexusProvider.setCategory(category == null ? NexusBaseClass.NX_INSTRUMENT : category); // collection would be added to NXentry not NXinstrument by default

		// as the NXcollection has the same name as the device, this is the NexusObjectProvider that will be used as the default axis
		final String[] inputNames = getScannable().getInputNames();
		if (inputNames.length > 0) {
			nexusProvider.setPrimaryDataFieldName(inputNames[0]);
			primaryDataFieldIndex = calculatePrimaryDataFieldIndex();
			final String primaryDataFieldName = getPrimaryDataFieldName();
			nexusProvider.setPrimaryDataFieldName(primaryDataFieldName);
			nexusProvider.addAxisDataFieldNames(inputNames);
			nexusProvider.setDefaultAxisDataFieldName(primaryDataFieldName);
		}
		return nexusProvider;
	}

	@Override
	protected N createNexusObject(NexusScanInfo info) throws NexusException {
		throw new UnsupportedOperationException("This method should not be called");
	}

	private boolean hasLocationMapEntry() {
		return NexusDataWriter.getLocationmap().containsKey(getName());
	}

	private boolean shouldWriteDemandValue(NexusScanInfo scanInfo) {
		final ScanRole scanRole = scanInfo.getScanRole(getName());
		return scanRole == ScanRole.SCANNABLE && writeDemandValue;
	}

	private N createConfiguredNexusObject(NexusScanInfo info) throws NexusException {
		final NexusBaseClass nexusBaseClass = getNexusBaseClass();
		final String scannableName = getName();

		@SuppressWarnings("unchecked")
		final N nexusObject = (N) NexusNodeFactory.createNXobjectForClass(nexusBaseClass);
		nexusObject.setField(FIELD_NAME_NAME, scannableName);
		// Attributes to identify the GD8 scannable so that the nexus file can be reverse engineered
		nexusObject.setAttribute(null, ATTR_NAME_GDA_SCANNABLE_NAME, scannableName);
		nexusObject.setAttribute(null, ATTR_NAME_GDA_SCAN_ROLE,
				info.getScanRole(scannableName).toString().toLowerCase());

		// add fields for attributes, e.g. name, description (a.k.a. metadata)
		registerAttributes(nexusObject);

		if (nexusObject instanceof NXpositioner && getScannable() instanceof ScannableMotion) {
			writeLimits((NXpositioner) nexusObject);
		}

		final String[] fieldOutputPaths = getScannableNexusDeviceConfiguration().get().getFieldPaths();
		int fieldIndex = 0;
		for (Map.Entry<String, DataNode> fieldDataNodeEntry : fieldDataNodes.entrySet()) {
			final String fieldName = fieldDataNodeEntry.getKey();
			final DataNode dataNode = fieldDataNodeEntry.getValue();
			final String fieldOutputPath = fieldIndex < fieldOutputPaths.length ? fieldOutputPaths[fieldIndex] : fieldName;
			NexusUtils.addDataNode(nexusObject, dataNode, fieldOutputPath);
			fieldIndex++;
		}

		return nexusObject;
	}

	@Override
	public CustomNexusEntryModification getCustomNexusModification() {
		if (!hasLocationMapEntry()) return null;

		final ScannableWriter writer = ServiceHolder.getNexusDataWriterConfiguration().getLocationMap().get(getName());
		if (writer instanceof SingleScannableWriter) {
			if (!writer.getClass().equals(SingleScannableWriter.class)) {
				logger.warn("NexusDataWriter location map entry for device {} is not fully supported: {}", getName(), writer.getClass());
			}

			return new ScannableLocationMapWriter<N>(this, (SingleScannableWriter) writer);
		} else {
			logger.warn("Cannot use NexusDataWriter location map to write device {}. "
					+ "Writer is not an instanceof of {}", getName(), SingleScannableWriter.class);
			return null;
		}
	}

	/**
	 * Set the {@link IScanObject} describing the movement of this scannable in the scan.
	 * @param scanObject
	 */
	public void setScanObject(IScanObject scanObject) {
		this.scanObject = scanObject;
	}

	private NexusBaseClass getNexusBaseClass() throws NexusException {
		try {
			final Optional<NexusBaseClass> optNexusClass = getScannableNexusDeviceConfiguration().map(ScannableNexusDeviceConfiguration::getNexusBaseClass);
			if (optNexusClass.isPresent()) {
				return optNexusClass.get();
			}

			final Object nxClass = getScannable().getScanMetadataAttribute(Scannable.ATTR_NX_CLASS);
			if (nxClass != null) {
				return NexusBaseClass.getBaseClassForName((String) nxClass);
			}
		} catch (Exception e) { // DeviceException, ClassCastException or IllegalArgumentException (from Enum.valueOf)
			throw new NexusException("Error getting NXclass for scannable: " + getName(), e);
		}

		return NexusBaseClass.NX_POSITIONER;
	}

	private void writeLimits(NXpositioner positioner) {
		if (!(getScannable() instanceof ScannableMotion)) return;
		final ScannableMotion scannableMotion = (ScannableMotion) getScannable();

		final Double[] lowerLimits = scannableMotion.getLowerGdaLimits();
		if (lowerLimits == null || lowerLimits.length == 0) {
			// do nothing
		} else if (lowerLimits.length == 1) {
			positioner.setSoft_limit_minScalar(lowerLimits[0]);
		} else {
			positioner.setSoft_limit_min(DatasetFactory.createFromObject(lowerLimits));
		}

		final Double[] upperLimits = scannableMotion.getUpperGdaLimits();
		if (upperLimits == null || upperLimits.length == 0) {
			// do nothing
		} else if (upperLimits.length == 1) {
			positioner.setSoft_limit_maxScalar(upperLimits[0]);
		} else {
			positioner.setSoft_limit_max(DatasetFactory.createFromObject(upperLimits));
		}
	}

	private void writeLimits(NXpositioner positioner, int fieldIndex) {
		if (!(getScannable() instanceof ScannableMotion)) return;
		final Double[] lowerLimits = ((ScannableMotion) getScannable()).getLowerGdaLimits();
		if (lowerLimits != null && lowerLimits.length > fieldIndex) {
			positioner.setSoft_limit_minScalar(lowerLimits[fieldIndex]);
		}
		final Double[] upperLimits = ((ScannableMotion) getScannable()).getUpperGdaLimits();
		if (upperLimits != null && upperLimits.length > fieldIndex) {
			positioner.setSoft_limit_maxScalar(upperLimits[fieldIndex]);
		}
	}

	/**
	 * Creates the data fields for the nexus object. These are the {@link DataNode}s - and the
	 * datasets that they contain - for each of the fields returned by
	 * {@link Scannable#getInputNames()} and {@link Scannable#getExtraNames()}.
	 * If the wrapped scannable is a metadata scannable these datasets will be
	 * {@link IDataset}s containing a single scalar value from the position
	 * returned by {@link Scannable#getPosition()} (turned into an array by
	 * {#getPositionArray()}, otherwise they will be {@link ILazyWriteableDataset}s,
	 * which will be written into during each call to {@link #setPosition(Object)}.
	 * Create the DataNodes for the {@link Scannable}.
	 *
	 * @param scanInfo
	 * @param nexusObject
	 * @throws NexusException
	 */
	private void createDataNodes(NexusScanInfo scanInfo) throws NexusException {
		final Object[] positionArray = getPositionArray(null);
		final Scannable scannable = getScannable();
		final String[] fieldNames = Stream.concat(Arrays.stream(scannable.getInputNames()),
				Arrays.stream(scannable.getExtraNames())).toArray(String[]::new);

		// create the datasets for each field
		fieldDataNodes = new LinkedHashMap<>(fieldNames.length);
		final NexusRole nexusRole = scanInfo.getScanRole(getName()).getNexusRole();
		for (int fieldIndex = 0; fieldIndex < fieldNames.length; fieldIndex++) {
			if (fieldIndex >= positionArray.length) {
				logger.warn("Field {} from scannable '{}' ({}) missing from positionArray {}", fieldIndex, getName(), fieldNames[fieldIndex], positionArray);
			} else {
				final String unitsStr = getFieldUnits(fieldIndex);
				final DataNode dataNode = createDataField(scanInfo, nexusRole, fieldNames[fieldIndex],
						unitsStr, positionArray[fieldIndex]);
				fieldDataNodes.put(fieldNames[fieldIndex], dataNode);
			}
		}

		if (shouldWriteDemandValue(scanInfo)) {
			demandValueDataNode = createDemandValueField();
		}
	}

	private DataNode createDataField(NexusScanInfo scanInfo, final NexusRole nexusRole,
			String inputFieldName, String unitsStr, Object value) {
		final DataNode dataNode = NexusNodeFactory.createDataNode();
		if (nexusRole == NexusRole.PER_SCAN) {
			// simply set the field to the current value
			dataNode.setDataset(DatasetFactory.createFromObject(value));
		} else if (nexusRole == NexusRole.PER_POINT) {
			// otherwise create a lazy writable dataset of the appropriate type
			dataNode.setDataset(createLazyWritableDataset(inputFieldName,
					value.getClass(), scanInfo.getRank(), scanInfo.createChunk(1)));
		}

		// set 'local_name' attribute to the scannable + input field name
		dataNode.addAttribute(TreeFactory.createAttribute(ATTR_NAME_LOCAL_NAME, getName() + "." + inputFieldName));
		// set field name attribute so we can recreate the scannable position from the nexus file (is this needed if its the same as above)?
		dataNode.addAttribute(TreeFactory.createAttribute(ATTR_NAME_GDA_FIELD_NAME, inputFieldName));

		// set units attribute
		if (unitsStr != null) {
			dataNode.addAttribute(TreeFactory.createAttribute(ATTR_NAME_UNITS, unitsStr));
		}

		// add the data node to the parent group
		return dataNode;
	}

	private DataNode createDemandValueField() {
		// create the demand value dataset (name 'value_set'). Note dataset is not added to the
		// writeableDatasets map as the order of entries in that map corresponds to elements in
		// getPositionArray, which does not include the demand value
		final ILazyWriteableDataset demandValueDataset = createLazyWritableDataset(FIELD_NAME_VALUE_SET,
				Double.class, 1, new int[] { 1 });
		final DataNode dataNode = NexusNodeFactory.createDataNode();
		dataNode.setDataset(demandValueDataset);
		dataNode.addAttribute(TreeFactory.createAttribute(ATTR_NAME_LOCAL_NAME, getName() + "." + FIELD_NAME_VALUE_SET));
		return dataNode;
	}

	private String getFieldUnits(int fieldIndex) {
		return getScannableNexusDeviceConfiguration()
				.map(ScannableNexusDeviceConfiguration::getUnits)
				.filter(units -> units.length > fieldIndex)
				.map(units -> units[fieldIndex])
				.orElse(getScannable() instanceof ScannableMotionUnits? ((ScannableMotionUnits) getScannable()).getUserUnits() : null);
	}

	private ILazyWriteableDataset createLazyWritableDataset(String fieldName, Class<?> clazz,
			final int rank, final int[] chunking) {
		// create a lazy writable dataset with the given name and same rank as the scan
		// (here the value is just used to get the dataset type, the scan hasn't started yet)
		int[] maxShape = new int[rank];
		Arrays.fill(maxShape, ILazyWriteableDataset.UNLIMITED);
		final ILazyWriteableDataset lazyWritableDataset = new LazyWriteableDataset(fieldName, clazz,
				maxShape, null, null, null);
		lazyWritableDataset.setFillValue(getFillValue(clazz));
		lazyWritableDataset.setChunking(chunking);

		return lazyWritableDataset;
	}

	/**
	 * Converts the position of the wrapped {@link Scannable} (as returned by
	 * {@link Scannable#getPosition()}) as an array.
	 * <ul>
	 *   <li>The position is not an array, just an object of some kind.
	 *   	A single-valued array is returned containing that object;</li>
	 *   <li>The position is a primitive array. It is converted to an array of Objects, each
	 *      element of which is a wrapper of the primitive at that index of primitive array;</li>
	 *   <li>The position is already an object array. It is returned as is.</li>
	 * </ul>
	 *
	 * @return position as an array
	 * @throws NexusException
	 */
	private Object[] getPositionArray(Object position) throws NexusException {
		try {
			if (position==null) position = getScannable().getPosition();

			if (position instanceof List) {
				final List<?> positionList = (List<?>)position;
				return positionList.toArray();
			}
			if (!position.getClass().isArray()) {
				// position is not an array (i.e. is a double) return array with position as single element
				return new Object[] { position };
			}

			if (position.getClass().getComponentType().isPrimitive()) {
				// position is a primitive array
				final int size = Array.getLength(position);
				Object[] outputArray = new Object[size];
				for (int i = 0; i < size; i++) {
					outputArray[i] = Array.get(position, i);
				}
				return outputArray;
			}

			// position is already an object array
			return (Object[]) position;
		} catch (DeviceException | NullPointerException e) {
			throw new NexusException("Could not get position of device: " + getName(), e);
		}
	}

	public Object writePosition(Object demandPosition, IPosition scanPosition) throws Exception {
		if (fieldDataNodes == null) return null; // TODO, this should never happen??

		final Object actualPosition = getScannable().getPosition();
		final SliceND sliceND = getSliceForPosition(scanPosition); // the location in each dataset to write to
		writeActualPosition(actualPosition, sliceND);
		writeDemandPosition(demandPosition, scanPosition.getIndex(getName()));
		return actualPosition;
	}

	/**
	 * Write the given position at the given slice.
	 * @param position the position to write
	 * @param scanSlice the scan slice, i.e. where to write in the datasets
	 * @throws NexusException if the position cannot be written for any reason
	 */
	@Override
	public void writePosition(Object position, SliceND scanSlice) throws NexusException {
		try {
			writeActualPosition(position, scanSlice);
		} catch(NexusException e) {
			throw e;
		} catch (Exception e) {
			throw new NexusException("Could not write position " + Arrays.toString(scanSlice.getStart()), e);
		}
	}

	public String[] getFieldNames() {
		return fieldDataNodes.keySet().toArray(String[]::new);
	}

	/**
	 * Write the given {@link SliceND} at the given point.
	 * @param actualPosition the actual position of the scannable
	 * @param scanSlice the scan slice, i.e. where to write in the datasets
	 * @throws Exception if the position cannot be written for any reason
	 */
	private void writeActualPosition(Object actualPosition, SliceND scanSlice) throws Exception {
		final Object[] positionArray = getPositionArray(actualPosition);
		if (positionArray.length < fieldDataNodes.size()) {
			throw new NexusException(MessageFormat.format("getPosition() of ''{0}'' must be an array of length at least: {1}",
					getName(), fieldDataNodes.size()));
		}

		// write the actual position (potentially multi-valued)
		final Iterator<DataNode> iter = fieldDataNodes.values().iterator();
		int fieldIndex = 0;
		while (iter.hasNext()) {
			final ILazyWriteableDataset writeableDataset = iter.next().getWriteableDataset();
			final IDataset value = DatasetFactory.createFromObject(positionArray[fieldIndex]);
			writeableDataset.setSlice(null, value, scanSlice);
			fieldIndex++;
		}
	}

	private void writeDemandPosition(Object demandPosition, int index) throws NexusException, DatasetException {
		// write the demand position to the demand dataset
		if (demandPosition != null && demandValueDataNode != null) {
			if (index < 0) {
				throw new NexusException("Incorrect data index for scan for value of '" + getName() + "'. The index is " + index);
			}
			final int[] startPos = new int[] { index };
			final int[] stopPos = new int[] { index + 1 };

			// write demand position
			final IDataset newDemandPositionData = DatasetFactory.createFromObject(demandPosition);
			demandValueDataNode.getWriteableDataset().setSlice(null, newDemandPositionData, startPos, stopPos, null);
		}
	}

	/**
	 * Return the slice to set. The same slice works for all
	 * lazy writable datasets as they all have the same shape, i.e. the scan shape
	 * @param pos
	 * @return
	 */
	private SliceND getSliceForPosition(IPosition pos) {
		// just use the first dataset as they all have the same shape
		final ILazyWriteableDataset dataset = fieldDataNodes.values().iterator().next().getWriteableDataset();
		final IScanSlice scanSlice = IScanRankService.getScanRankService().createScanSlice(pos);
		return new SliceND(dataset.getShape(), dataset.getMaxShape(),
				scanSlice.getStart(), scanSlice.getStop(), scanSlice.getStep());
	}

	public boolean isNexusObjectCreated() {
		return fieldDataNodes != null;
	}

	@Override
	public void scanEnd() throws NexusException {
		super.scanEnd();

		demandValueDataNode = null;
		fieldDataNodes = null;
	}

	@Override
	public String toString() {
		return "ScannableNexusDevice [scannable=" + getName() + "]";
	}

}
