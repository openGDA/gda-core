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

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
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
	public static final String FIELD_NAME_VALUE_SET = "value_set";

	/**
	 * The {@link DataNode}s for each field keyed by field path with the same iteration order as the values in {@link #getPositionArray(Object)}.
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
	private DataNode demandValueDatanode = null;

	private String primaryAxisFieldPath = null;

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
		super.configureNexusWrapper(nexusWrapper, info);

		final NexusBaseClass category = getNexusCategory();
		nexusWrapper.setCategory(category);

		if (info.getScanRole(getName()) == ScanRole.SCANNABLE && writeDemandValue) {
			nexusWrapper.addAxisDataFieldName(getPrimaryDataFieldName());
			nexusWrapper.setDefaultAxisDataFieldName(FIELD_NAME_VALUE_SET);
		} else {
			nexusWrapper.setDefaultAxisDataFieldName(getPrimaryDataFieldName());
		}

		final String collectionName = getScannableNexusDeviceConfiguration()
				.map(ScannableNexusDeviceConfiguration::getCollectionName)
				.orElseGet(() -> hasLocationMapEntry() ? COLLECTION_NAME_SCANNABLES : null);
		nexusWrapper.setCollectionName(collectionName);
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

			final Object categoryStr = getDevice().getScanMetadataAttribute(Scannable.ATTR_NEXUS_CATEGORY);
			if (categoryStr instanceof String) {
				return NexusBaseClass.getBaseClassForName((String) categoryStr);
			}
			return null;
		} catch (Exception e) { // DeviceException, ClassCastException or IllegalArgumentException (from Enum.valueOf)
			throw new NexusException("Error getting Nexus category for device: " + getName(), e);
		}
	}

	@Override
	protected String getPrimaryDataFieldName() {
		return primaryAxisFieldPath;
	}

	/**
	 * Returns the output field paths for each field
	 * @return output field paths
	 */
	public String[] getOutputFieldPaths() {
		// since a LinkedHashMap is used the iteration order of the keyset matches the insertion order
		return fieldDataNodes.keySet().stream().toArray(String[]::new);
	}

	public DataNode getFieldDataNodes(String outputFieldPath) {
		return fieldDataNodes.get(outputFieldPath);
	}

	@Override
	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {
		final Scannable scannable = getScannable();
		if (scannable instanceof IMultipleNexusDevice) {
			return ((IMultipleNexusDevice)scannable).getNexusProviders(info);
		}
		return Collections.<NexusObjectProvider<?>>emptyList();
	}

	private boolean hasLocationMapEntry() {
		return NexusDataWriter.getLocationmap().containsKey(getName());
	}

	@Override
	protected N createNexusObject(NexusScanInfo info) throws NexusException {
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

		if (getScannable() instanceof ScannableMotion && nexusBaseClass == NexusBaseClass.NX_POSITIONER) {
			writeLimits((NXpositioner) nexusObject);
		}

		// create the data fields. These are the fields read from the scannables position.
		createDataFields(info, nexusObject);

		this.nexusObject = nexusObject;
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

	/**
	 * Creates the data fields for the nexus object. These are the {@link DataNode}s - and the
	 * datasets that they contain - for each of the fields returned by
	 * {@link Scannable#getInputNames()} and {@link Scannable#getExtraNames()}.
	 * If the wrapped scannable is a metadata scannable these datasets will be
	 * {@link IDataset}s containing a single scalar value from the position
	 * returned by {@link Scannable#getPosition()} (turned into an array by
	 * {#getPositionArray()}, otherwise they will be {@link ILazyWriteableDataset}s,
	 * which will be written into during each call to {@link #setPosition(Object)}.
	 * @param scanInfo
	 * @param nexusObject
	 * @throws NexusException
	 */
	private void createDataFields(NexusScanInfo scanInfo, final N nexusObject) throws NexusException {
		final Object[] positionArray = getPositionArray(null);
		final ScanRole scanRole = scanInfo.getScanRole(getName());
		final NexusRole nexusRole = scanRole.getNexusRole();
		final Scannable scannable = getScannable();
		final String[] fieldNames = Stream.concat(Arrays.stream(scannable.getInputNames()),
				Arrays.stream(scannable.getExtraNames())).toArray(String[]::new);

		fieldDataNodes = new LinkedHashMap<>(fieldNames.length);

		if (nexusRole == NexusRole.PER_POINT && scanRole == ScanRole.SCANNABLE && writeDemandValue) {
			// create the demand value dataset (name 'value_set'). Note dataset is not added to the
			// writeableDatasets map as the order of entries in that map corresponds to elements in
			// getPositionArray, which does not include the demand value
			final ILazyWriteableDataset demandValueDataset = createLazyWritableDataset(FIELD_NAME_VALUE_SET,
					Double.class, 1, new int[] { 1 });
			demandValueDatanode = NexusNodeFactory.createDataNode();
			demandValueDatanode.setDataset(demandValueDataset);
			nexusObject.addDataNode(FIELD_NAME_VALUE_SET, demandValueDatanode);
			nexusObject.setAttribute(FIELD_NAME_VALUE_SET, ATTR_NAME_LOCAL_NAME,
					getName() + "." + FIELD_NAME_VALUE_SET);
		}

		// create the datasets for each field
		for (int fieldIndex = 0; fieldIndex < fieldNames.length; fieldIndex++) {
			if (fieldIndex >= positionArray.length) {
				logger.warn("Field {} from scannable '{}' ({}) missing from positionArray {}", fieldIndex, getName(), fieldNames[fieldIndex], positionArray);
			} else {
				final String unitsStr = getFieldUnits(fieldIndex);
				final String outputFieldPath = getOutputFieldPath(fieldNames[fieldIndex], fieldIndex);
				final DataNode dataNode = createDataField(nexusObject, scanInfo, nexusRole, fieldNames[fieldIndex],
						outputFieldPath, unitsStr, positionArray[fieldIndex]);
				fieldDataNodes.put(outputFieldPath, dataNode);

				if (fieldIndex == 0) {
					primaryAxisFieldPath = outputFieldPath;
				}
			}
		}
	}

	private DataNode createDataField(final N nexusObject, NexusScanInfo scanInfo, final NexusRole nexusRole,
			String inputFieldName, String outputFieldPath, String unitsStr, Object value) throws NexusException {
		final DataNode dataNode = NexusNodeFactory.createDataNode();
		if (nexusRole == NexusRole.PER_SCAN) {
			// simply set the field to the current value
			dataNode.setDataset(DatasetFactory.createFromObject(value));
		} else if (nexusRole == NexusRole.PER_POINT) {
			// otherwise create a lazy writable dataset of the appropriate type
			final String outputFieldName = getLastSegment(outputFieldPath);
			dataNode.setDataset(createLazyWritableDataset(outputFieldName,
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
		NexusUtils.addDataNode(nexusObject, dataNode, outputFieldPath);
		return dataNode;
	}

	private String getOutputFieldPath(String inputFieldName, int fieldIndex) throws NexusException {
		return getScannableNexusDeviceConfiguration()
				.map(ScannableNexusDeviceConfiguration::getFieldPaths)
				.filter(paths -> paths.length > fieldIndex)
				.map(paths -> paths[fieldIndex])
				.orElse(getNexusBaseClass() == NexusBaseClass.NX_POSITIONER && inputFieldName.equals(getName()) ?
						NXpositioner.NX_VALUE : inputFieldName);
		// if nexus object is NXpositioner and field name is same as scannable name, use 'value' instead,
		// in order to comply with nexus standard for NXpositioner
	}

	private String getLastSegment(String path) {
		final int lastSeparatorIndex = path.lastIndexOf(Node.SEPARATOR);
		return lastSeparatorIndex == -1 ? path : path.substring(lastSeparatorIndex + 1);
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
		if (demandPosition != null && demandValueDatanode != null) {
			if (index < 0) {
				throw new NexusException("Incorrect data index for scan for value of '" + getName() + "'. The index is " + index);
			}
			final int[] startPos = new int[] { index };
			final int[] stopPos = new int[] { index + 1 };

			// write demand position
			final IDataset newDemandPositionData = DatasetFactory.createFromObject(demandPosition);
			demandValueDatanode.getWriteableDataset().setSlice(null, newDemandPositionData, startPos, stopPos, null);
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

	protected boolean isNexusObjectCreated() {
		return nexusObject != null;
	}

	@Override
	public String toString() {
		return "ScannableNexusDevice [scannable=" + getName() + "]";
	}

	@Override
	public void scanEnd() throws NexusException {
		super.scanEnd();

		nexusObject = null;
		demandValueDatanode = null;
		fieldDataNodes = null;
		primaryAxisFieldPath = null;
	}

}
