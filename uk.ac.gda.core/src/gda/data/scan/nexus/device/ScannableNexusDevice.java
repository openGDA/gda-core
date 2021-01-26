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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
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
import org.eclipse.dawnsci.nexus.builder.CustomNexusEntryModification;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
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
	 * The writable datasets, ordered by field name with same iteration order as
	 * values in getPositionArray(). The size of this map should be the same as the position
	 * array returned by getPositionArray() where the names come from
	 * getInputNames() + getExtraNames()
	 */
	private LinkedHashMap<String, ILazyWriteableDataset> writableDatasets = null;

	/**
	 * Whether the demand value (i.e. the position the scannable was set to by the scan) should
	 * be written. Should be set to <code>false</code> if this information is not available in the scan.
	 */
	private boolean writeDemandValue = true;

	/**
	 * The writable dataset for the demand value.
	 */
	private ILazyWriteableDataset demandValueDataset = null;

	/**
	 * A list of the <em>original</em> field names for each field of the position.
	 */
	private List<String> inputFieldNames = null;

	/**
	 * A list of the <em>destination</em> field names for each field of the position
	 * array as returned by {@link #getPositionArray()}.
	 */
	private List<String> outputFieldNames = null;

	public ScannableNexusDevice(Scannable scannable) {
		super(scannable);
	}

	protected Scannable getScannable() {
		return super.getDevice();
	}

	public void setWriteDemandValue(boolean writeDemandValue) {
		this.writeDemandValue = writeDemandValue;
	}

	protected void calculateFieldNames() {
		final Scannable scannable = getScannable();
		final String[] inputNames = scannable.getInputNames();
		final String[] extraNames = scannable.getExtraNames();
		inputFieldNames = new ArrayList<>(inputNames.length + extraNames.length);
		inputFieldNames.addAll(Arrays.asList(inputNames));
		inputFieldNames.addAll(Arrays.asList(extraNames));

		outputFieldNames = new ArrayList<>(inputFieldNames);

		// if we create a positioner, and the scannable has a field with the same name as the scannable itself,
		// then this field should be written to the field name 'value' as specified by the NXDL base class
		// definition for NXpositioner
		final String scannableName = scannable.getName();
		try {
			if (getNexusBaseClass() == NexusBaseClass.NX_POSITIONER && outputFieldNames.contains(scannableName)) {
				int index = outputFieldNames.indexOf(scannableName);
				outputFieldNames.set(index, NXpositioner.NX_VALUE);
			}
		} catch (NexusException e) {
			logger.error("Error getting nexus base class", e);
		}
	}

	@Override
	protected void configureNexusWrapper(NexusObjectWrapper<N> nexusWrapper, NexusScanInfo info) throws NexusException {
		super.configureNexusWrapper(nexusWrapper, info);

		if (!outputFieldNames.isEmpty()) {
			nexusWrapper.setDefaultAxisDataFieldName(outputFieldNames.get(0));
		}

		try {
			final Object categoryStr = getDevice().getScanMetadataAttribute(Scannable.ATTR_NEXUS_CATEGORY);
			if (categoryStr instanceof String) {
				nexusWrapper.setCategory(NexusBaseClass.getBaseClassForName((String) categoryStr));
			}
		} catch (Exception e) { // DeviceException, ClassCastException or IllegalArgumentException (from Enum.valueOf)
			throw new NexusException("Error getting Nexus category for device: " + getName(), e);
		}

		if (info.getScanRole(getName()) == ScanRole.SCANNABLE) {
			nexusWrapper.setDefaultAxisDataFieldName(writeDemandValue ? FIELD_NAME_VALUE_SET : getPrimaryDataFieldName());
		}
		if (hasLocationMapEntry()) {
			nexusWrapper.setCollectionName(COLLECTION_NAME_SCANNABLES);
		}
	}

	@Override
	protected String getPrimaryDataFieldName() {
		return outputFieldNames.isEmpty() ? null : outputFieldNames.get(0);
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
		calculateFieldNames();

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

			return new LegacyNexusWriter<N>(this, (SingleScannableWriter) writer);
		} else {
			logger.warn("Cannot use NexusDataWriter location map to write device {}. "
					+ "Writer is not an instanceof of {}", getName(), SingleScannableWriter.class);
			return null;
		}
	}

	public List<String> getInputFieldNames() {
		if (inputFieldNames == null) {
			calculateFieldNames();
		}

		return inputFieldNames;
	}

	public List<String> getOutputFieldNames() {
		if (outputFieldNames == null) {
			calculateFieldNames();
		}

		return outputFieldNames;
	}

	private NexusBaseClass getNexusBaseClass() throws NexusException {
		try {
			Object nxClass = getScannable().getScanMetadataAttribute(Scannable.ATTR_NX_CLASS);
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
	 * @param info
	 * @param nexusObject
	 * @throws NexusException
	 */
	private void createDataFields(NexusScanInfo info, final N nexusObject) throws NexusException {
		final Object[] positionArray = getPositionArray(null);
		final int scanRank = info.getRank();
		final int[] chunks = info.createChunk(1);

		final ScanRole scanRole = info.getScanRole(getName());
		final NexusRole nexusRole = scanRole.getNexusRole();
		if (nexusRole == NexusRole.PER_POINT) {
			// cache the lazy writeable datasets we create so we can write to them later
			writableDatasets = new LinkedHashMap<>();
			if (scanRole == ScanRole.SCANNABLE && writeDemandValue) {
				// create the demand value dataset (name 'value_set'). Note dataset is not added to the
				// writeableDatasets map as the order of entries in that map corresponds to elements in
				// getPositionArray, which does not include the demand value
				demandValueDataset = createLazyWritableDataset(nexusObject, FIELD_NAME_VALUE_SET,
						Double.class, 1, new int[] { 1 });
				nexusObject.setAttribute(FIELD_NAME_VALUE_SET, ATTR_NAME_LOCAL_NAME,
						getName() + "." + FIELD_NAME_VALUE_SET);
			}
		}

		final String unitsStr = getScannable() instanceof ScannableMotionUnits ?
				((ScannableMotionUnits) getScannable()).getUserUnits() : null;

		// create the dataset for each field
		final List<String> inputFieldNames = getInputFieldNames();
		final List<String> outputFieldNames = getOutputFieldNames();
		for (int i = 0; i < outputFieldNames.size(); i++) {
			final String inputFieldName = inputFieldNames.get(i);
			final String outputFieldName = outputFieldNames.get(i);
			if (i >= positionArray.length) {
				logger.warn("Field {} from scannable '{}' ({}/{}) missing from positionArray {}",
						i, getName(), inputFieldName, outputFieldName, positionArray);
				continue;
			}
			final Object value = positionArray[i];
			if (nexusRole == NexusRole.PER_SCAN) {
				// simply set the field to the current value
				nexusObject.setField(outputFieldName, value);
			} else if (nexusRole == NexusRole.PER_POINT) {
				// otherwise create a lazy writable dataset of the appropriate type
				final ILazyWriteableDataset dataset = createLazyWritableDataset(nexusObject,
						outputFieldName, value.getClass(), scanRank, chunks);
				writableDatasets.put(outputFieldName, dataset);
			}

			// set 'local_name' attribute
			nexusObject.setAttribute(outputFieldName, ATTR_NAME_LOCAL_NAME, getName() + "." + outputFieldName);
			// set field name attribute so we can recreate the scannable position from the nexus file
			nexusObject.setAttribute(outputFieldName, ATTR_NAME_GDA_FIELD_NAME, inputFieldName);

			// set units attribute
			if (unitsStr != null) {
				nexusObject.setAttribute(outputFieldName, ATTR_NAME_UNITS, unitsStr);
			}
		}
	}

	private ILazyWriteableDataset createLazyWritableDataset(final N nexusObject, String fieldName,
			Class<?> clazz, final int rank, final int[] chunking) {
		// create a lazy writable dataset with the given name and same rank as the scan
		// (here the value is just used to get the dataset type, the scan hasn't started yet)
		ILazyWriteableDataset lazyWritableDataset = nexusObject.initializeLazyDataset(
				fieldName, rank, clazz);
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

	// TODO move this method to ScannableNexusWrapper??
	public Object writePosition(Object demandPosition, IPosition scanPosition) throws Exception {
		if (writableDatasets == null) return null; // TODO, this should never happen??

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
		if (positionArray.length < writableDatasets.size()) {
			throw new NexusException(MessageFormat.format("getPosition() of ''{0}'' must be an array of length at least: {1}",
					getName(), writableDatasets.size()));
		}

		// write the actual position (potentially multi-valued)
		int fieldIndex = 0;
		for (ILazyWriteableDataset dataset : writableDatasets.values()) {
			// we rely on predictable iteration order for LinkedHashSet of writable datasets
			final IDataset value = DatasetFactory.createFromObject(positionArray[fieldIndex]);
			dataset.setSlice(null, value, scanSlice);
			fieldIndex++;
		}
	}

	private void writeDemandPosition(Object demandPosition, int index) throws NexusException, DatasetException {
		// write the demand position to the demand dataset
		if (demandPosition != null && demandValueDataset != null) {
			if (index < 0) {
				throw new NexusException("Incorrect data index for scan for value of '" + getName() + "'. The index is " + index);
			}
			final int[] startPos = new int[] { index };
			final int[] stopPos = new int[] { index + 1 };

			// write demand position
			final IDataset newDemandPositionData = DatasetFactory.createFromObject(demandPosition);
			demandValueDataset.setSlice(null, newDemandPositionData, startPos, stopPos, null);
		}
	}

	/**
	 * Return the slice to set. The same slice works for all
	 * lazy writable datasets as they all have the same shape, i.e. the scan shape
	 * @param pos
	 * @return
	 */
	private SliceND getSliceForPosition(IPosition pos) {
		String firstFieldName = getOutputFieldNames().get(0);
		ILazyWriteableDataset dataset = writableDatasets.get(firstFieldName);
		IScanSlice scanSlice = IScanRankService.getScanRankService().createScanSlice(pos);

		return new SliceND(dataset.getShape(), dataset.getMaxShape(),
				scanSlice.getStart(), scanSlice.getStop(), scanSlice.getStep());
	}

	/**
	 * Returns the {@link DataNode} for the given field name.
	 * @param fieldName field name
	 * @return data node for given field name, or <code>null</code> if no such data node exists
	 */
	public DataNode getDataNode(String fieldName) {
		return nexusObject.getDataNode(fieldName);
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
		inputFieldNames = null;
		outputFieldNames = null;

		nexusObject = null;
		demandValueDataset = null;
		writableDatasets = null;
	}

}
