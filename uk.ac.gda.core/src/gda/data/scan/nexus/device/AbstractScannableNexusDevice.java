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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.NexusRole;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
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

import gda.device.ControllerRecord;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.util.TypeConverters;
import uk.ac.gda.api.scan.IExplicitScanObject;
import uk.ac.gda.api.scan.IImplicitScanObject;
import uk.ac.gda.api.scan.IScanObject;

/**
 * An instance of this type adapts a {@link Scannable} to {@link INexusDevice}.
 * There are two concrete subclasses:<ul>
 * <li>{@link ConfiguredScannableNexusDevice} is used where a {@link ScannableNexusDeviceConfiguration}
 * is defined (normally created in spring) with the same name;</li>
 * <li>{@link DefaultScannableNexusDevice} is used otherwise</li>
 * </ul>
 *
 * @param <N>
 */
public abstract class AbstractScannableNexusDevice<N extends NXobject> extends AbstractNexusDeviceAdapter<N> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractScannableNexusDevice.class);

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
	 * The field name 'value.set', used for the requested value of a scannable,
	 * e.g. a motor. Note that this should be a constant in {@link NXpositioner}, but
	 * it hasn't been added yet. When this has happened, the nexus base classes should be
	 * regenerated and the constant from this {@link NXpositioner} used instead.
	 */
	public static final String FIELD_NAME_VALUE_SET = NXpositioner.NX_VALUE + NexusConstants.DATA_AXESSET_SUFFIX;

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

	private int primaryDataFieldIndex = -1;

	private IScanObject scanObject = null;

	protected AbstractScannableNexusDevice(Scannable scannable) {
		super(scannable);
	}

	protected Scannable getScannable() {
		return super.getDevice();
	}

	protected int getPrimaryDataFieldIndex() {
		if (primaryDataFieldIndex == -1) {
			primaryDataFieldIndex = calculatePrimaryDataFieldIndex();
		}
		return primaryDataFieldIndex;
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
		final int primaryDataFieldIndex = getPrimaryDataFieldIndex();
		final String[] inputNames = getScannable().getInputNames();
		if (primaryDataFieldIndex < inputNames.length) {
			return inputNames[primaryDataFieldIndex];
		}
		return getScannable().getExtraNames()[primaryDataFieldIndex - inputNames.length];
	}

	protected NexusBaseClass getNexusCategory() throws NexusException {
		try {
			final Object categoryStr = getScannable().getScanMetadataAttribute(ATTR_NEXUS_CATEGORY);
			if (categoryStr instanceof String) {
				return NexusBaseClass.getBaseClassForName((String) categoryStr);
			}
			return null;
		} catch (Exception e) { // DeviceException, ClassCastException or IllegalArgumentException (from Enum.valueOf)
			throw new NexusException("Error getting Nexus category for device: " + getName(), e);
		}
	}

	public DataNode getFieldDataNode(String fieldName) {
		return fieldDataNodes.get(fieldName);
	}

	@Override
	public NexusObjectProvider<N> getNexusProvider(NexusScanInfo info) throws NexusException {
		// this class now returns multiple nexus providers for scannables with multiple input fields
		throw new UnsupportedOperationException("getNexusProviders() should be called instead of this method");
	}

	protected void addFields(String[] fieldNames, NXobject nexusObject) {
		for (String fieldName : fieldNames) {
			final DataNode dataNode = fieldDataNodes.get(fieldName);
			Objects.nonNull(dataNode); // sanity check
			nexusObject.addDataNode(fieldName, dataNode);
		}
	}

	@Override
	protected N createNexusObject(NexusScanInfo info) throws NexusException {
		throw new UnsupportedOperationException("This method should not be called");
	}

	/**
	 * Set the {@link IScanObject} describing the movement of this scannable in the scan.
	 * @param scanObject
	 */
	public void setScanObject(IScanObject scanObject) {
		this.scanObject = scanObject;
	}

	protected NexusBaseClass getNexusBaseClass() throws NexusException {
		try {
			final Object nxClass = getScannable().getScanMetadataAttribute(Scannable.ATTR_NX_CLASS);
			if (nxClass != null) {
				return NexusBaseClass.getBaseClassForName((String) nxClass);
			}
		} catch (Exception e) { // DeviceException, ClassCastException or IllegalArgumentException (from Enum.valueOf)
			throw new NexusException("Error getting NXclass for scannable: " + getName(), e);
		}

		return getScannable().getInputNames().length == 0 ? NexusBaseClass.NX_COLLECTION : NexusBaseClass.NX_POSITIONER;
	}

	/**
	 * Creates the data fields for the nexus object. These are the {@link DataNode}s - and the
	 * datasets that they contain - for each of the fields returned by
	 * {@link Scannable#getInputNames()} and {@link Scannable#getExtraNames()}.
	 * If the wrapped scannable is a metadata scannable these datasets will be
	 * {@link IDataset}s containing a single scalar value from the position
	 * returned by {@link Scannable#getPosition()} (turned into an array by
	 * {#getPositionArray()}, otherwise they will be {@link ILazyWriteableDataset}s,
	 * which will be written into during each call to {@link #writePosition(Object, IPosition)}}
	 * Create the DataNodes for the {@link Scannable}.
	 *
	 * @param scanInfo
	 * @throws NexusException
	 */
	protected void createDataNodes(NexusScanInfo scanInfo) throws NexusException {
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
		dataNode.addAttribute(TreeFactory.createAttribute(ATTR_NAME_LOCAL_NAME, getName() + NexusConstants.FIELD_SEPERATOR + inputFieldName));
		// set field name attribute so we can recreate the scannable position from the nexus file (is this needed if its the same as above)?
		dataNode.addAttribute(TreeFactory.createAttribute(ATTR_NAME_GDA_FIELD_NAME, inputFieldName));

		// set units attribute
		if (unitsStr != null) {
			dataNode.addAttribute(TreeFactory.createAttribute(ATTR_NAME_UNITS, unitsStr));
		}

		// add the data node to the parent group
		return dataNode;
	}

	protected String getFieldUnits(@SuppressWarnings("unused") int fieldIndex) {
		return getScannable() instanceof ScannableMotionUnits? ((ScannableMotionUnits) getScannable()).getUserUnits() : null;
	}

	protected ILazyWriteableDataset createLazyWritableDataset(String fieldName, Class<?> clazz,
			final int rank, final int[] chunking) {
		// create a lazy writable dataset with the given name and same rank as the scan
		// (here the value is just used to get the dataset type, the scan hasn't started yet)
		int[] shape = new int[rank];
		int[] maxShape = new int[rank];
		Arrays.fill(maxShape, ILazyWriteableDataset.UNLIMITED);
		final ILazyWriteableDataset lazyWritableDataset = new LazyWriteableDataset(fieldName, clazz,
				shape, maxShape, null, null);
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

	protected void writeControllerRecordName(final NXpositioner positioner) {
		if (getScannable() instanceof ControllerRecord) { // controller record name is PV name for EPICS
			final String recordName = ((ControllerRecord) getScannable()).getControllerRecordName();
			if (recordName != null) {
				positioner.setController_recordScalar(recordName);
			}
		}
	}

	public Object writePosition(@SuppressWarnings("unused") Object demandPosition, IPosition scanPosition) throws Exception {
		if (fieldDataNodes == null) return null; // TODO, this should never happen??

		final Object actualPosition = getScannable().getPosition();
		final SliceND sliceND = getSliceForPosition(scanPosition); // the location in each dataset to write to
		writeActualPosition(actualPosition, sliceND);
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

		fieldDataNodes = null;
	}

	@Override
	public String toString() {
		return "ScannableNexusDevice [scannable=" + getName() + "]";
	}

}