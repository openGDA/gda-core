package uk.ac.diamond.daq.scanning;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.builder.CustomNexusEntryModification;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;
import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * Class provides a default implementation which will write any GDA8 scannable to NeXus
 *
 * @author Matthew Gerring, Matthew Dickie
 */
class ScannableNexusWrapper<N extends NXobject> implements IScannable<Object>, INexusDevice<N> {

	/**
	 * The name of the 'scannables' collection. This collection contains all wrapped GDA8
	 * scannables. The reason for this is that unless otherwise specified the nexus object
	 * created for all scannables is an {@link NXpositioner}, even for metadata scannables,
	 * e.g. sample name. This will encourage switching to the proper GDA9 mechanisms, e.g.
	 * adding scan metadata directly to the {@link ScanRequest} instead of using a
	 * metadata scannable, and creating implementations of the GDA9 {@link IScannable} interface.
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

	/**
	 * The attribute name 'units'.
	 */
	public static final String ATTR_NAME_UNITS = "units";

	/**
	 * The field name 'value_demand', used for the requested value of a scannable,
	 * e.g. a motor. Note that this should be a constant in {@link NXpositioner}, but
	 * it hasn't been added yet. When this has happened, the nexus base classes should be
	 * regenerated and the constant from this {@link NXpositioner} used instead.
	 */
	public static final String FIELD_NAME_VALUE_DEMAND = "value_demand";

	/**
	 * The GDA8 scannable being wrapped.
	 */
	private Scannable scannable;

	/**
	 * The writable datasets, ordered by field name with same iteration order as
	 * values in getPositionArray(). The size of this map should be the same as the position
	 * array returned by getPositionArray() where the names come from
	 * getInputNames() + getExtraNames()
	 */
	private LinkedHashMap<String, ILazyWriteableDataset> writableDatasets;

	/**
	 * A list of the <em>destination</em> field names for each field of the position
	 * array as returned by {@link #getPositionArray()}.
	 */
	private List<String> fieldNames = null;

	/**
	 * The Nexus object created.
	 */
	protected N nexusObject = null;

	/**
	 * The writable dataset for the demand value.
	 */
	private ILazyWriteableDataset demandValueDataset;

	private static final Logger logger = LoggerFactory.getLogger(ScannableNexusWrapper.class);

	ScannableNexusWrapper(Scannable scannable) {
		this.scannable = scannable;
		this.fieldNames = calculateFieldNames();
	}

	protected List<String> calculateFieldNames() {
		String[] inputNames = scannable.getInputNames();
		String[] extraNames = scannable.getExtraNames();
		fieldNames = new ArrayList<>(inputNames.length + extraNames.length);
		fieldNames.addAll(Arrays.asList(inputNames));
		fieldNames.addAll(Arrays.asList(extraNames));

		// if we create a positioner, and the scannable has a field with the same name as the scannable itself,
		// then this field should be written to the field name 'value' as specified by the NXDL base class
		// definition for NXpositioner
		final String scannableName = scannable.getName();
		if (getNexusBaseClass() == NexusBaseClass.NX_POSITIONER && fieldNames.contains(scannableName)) {
			int index = fieldNames.indexOf(scannableName);
			fieldNames.set(index, NXpositioner.NX_VALUE);
		}

		return fieldNames;
	}

	public List<String> getFieldNames() {
		return fieldNames;
	}

	@Override
	public NexusObjectProvider<N> getNexusProvider(NexusScanInfo info) throws NexusException {
		nexusObject = createNexusObject(info);
		String defaultDataField = scannable.getInputNames().length > 0 ?
				fieldNames.get(0) : null;
		NexusObjectWrapper<N> nexusDelegate = new NexusObjectWrapper<>(
						scannable.getName(), nexusObject, defaultDataField);
		if (info.getScanRole(getName()) == ScanRole.SCANNABLE) {
			nexusDelegate.setDefaultAxisDataFieldName(FIELD_NAME_VALUE_DEMAND);
		}
		if (hasLocationMapEntry()) {
			nexusDelegate.setCollectionName(COLLECTION_NAME_SCANNABLES);
		}

		return nexusDelegate;
	}

	private boolean hasLocationMapEntry() {
		return NexusDataWriter.getLocationmap().containsKey(scannable.getName());
	}

	private N createNexusObject(NexusScanInfo info) throws NexusException {
		NexusBaseClass nexusBaseClass = getNexusBaseClass();
		@SuppressWarnings("unchecked")
		N nexusObject = (N) NexusNodeFactory.createNXobjectForClass(nexusBaseClass);
		nexusObject.setField(FIELD_NAME_NAME, scannable.getName());

		// add fields for attributes, e.g. name, description (a.k.a. metadata)
		registerAttributes(nexusObject);

		// create the data fields. These are the fields read from the scannables position.
		createDataFields(info, nexusObject);

		this.nexusObject = nexusObject;
		return nexusObject;
	}

	@Override
	public CustomNexusEntryModification getCustomNexusModification() {
		final String name = scannable.getName();
		if (hasLocationMapEntry()) {
			ScannableWriter writer = NexusDataWriter.getLocationmap().get(name);
			if (writer instanceof SingleScannableWriter) {
				if (!writer.getClass().equals(SingleScannableWriter.class)) {
					logger.warn("NexusDataWriter location map entry for device {} is not fully supported: {}",  name, writer.getClass());
				}

				return new LegacyNexusWriter<N>(this, (SingleScannableWriter) writer);
			} else {
				logger.warn("Cannot use NexusDataWriter location map to write device {}. "
						+ "Writer is not an instanceof of {}", name, SingleScannableWriter.class);
			}
		}

		return null;
	}

	@Override
	public void setLevel(int level) {
		scannable.setLevel(level);
	}

	@Override
	public int getLevel() {
		return scannable.getLevel();
	}

	@Override
	public String getName() {
		return scannable.getName();
	}

	@Override
	public void setName(String name) {
		scannable.setName(name);
	}

	@Override
	public Object getPosition() throws Exception {
		return scannable.getPosition();
	}

	@Override
	public void setPosition(Object value) throws Exception {
		scannable.moveTo(value);
	}

	@Override
	public void setPosition(Object value, IPosition scanPosition) throws Exception {
		scannable.moveTo(value);

		if (scanPosition != null) {
			write(value, getPositionArray(), scanPosition);
		}
	}

	private NexusBaseClass getNexusBaseClass() {
		try {
			Object nxClass = scannable.getScanMetadataAttribute(Scannable.ATTR_NX_CLASS);
			if (nxClass != null && nxClass instanceof String) {
				return NexusBaseClass.getBaseClassForName((String) nxClass);
			}
		} catch (IllegalArgumentException e) {
			logger.error("Error getting NXclass for device: " + getName(), e);
			throw new RuntimeException("Error getting NXclass for device: " + getName(), e);
		} catch (DeviceException e) {
			logger.error("Error getting NXclass for device: " + getName(), e);
			throw new RuntimeException("Error getting NXclass for device: " + getName(), e);
		}

		return NexusBaseClass.NX_POSITIONER;
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
		final Object[] positionArray = getPositionArray();
		final int scanRank = info.getRank();
		final int[] chunks = info.createChunk(1); // TODO Might be slow, need to check this

		final ScanRole role = info.getScanRole(getName());
		if (role != ScanRole.METADATA) {
			// cache the lazy writeable datasets we create so we can write to them later
			writableDatasets = new LinkedHashMap<>();
			if (role == ScanRole.SCANNABLE) {
				// create the 'value_demand' dataset (can't use writeableDatasets map as the
				// order of entries in that map corresponds to elements in getPositionArray, which
				// does not include the demand value
				demandValueDataset = createLazyWritableDataset(nexusObject, FIELD_NAME_VALUE_DEMAND,
						Double.class, 1, new int[] { 1 });
				nexusObject.setAttribute(FIELD_NAME_VALUE_DEMAND, ATTR_NAME_LOCAL_NAME,
						getName() + "." + FIELD_NAME_VALUE_DEMAND);
			}
		}

		// create the dataset for each field
		for (int i = 0; i < fieldNames.size(); i++) {
			String fieldName = fieldNames.get(i);
			if (fieldName.equals(getName()) && nexusObject.getNexusBaseClass() == NexusBaseClass.NX_POSITIONER) {
				fieldName = NXpositioner.NX_VALUE;
			}

			final Object value = positionArray[i];
			if (role == ScanRole.METADATA) {
				// simply set the field to the current value
				nexusObject.setField(fieldName, value);
			} else {
				// create a lazy writable dataset of the appropriate type
				writableDatasets.put(fieldName,
						createLazyWritableDataset(nexusObject, fieldName, value.getClass(), scanRank, chunks));
			}
			// set 'local_name' attribute
			nexusObject.setAttribute(fieldName, ATTR_NAME_LOCAL_NAME, getName() + "." + fieldName);
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

	private static Object getFillValue(Class<?> clazz) {
		if (clazz.equals(Double.class)) {
			String floatFill = LocalProperties.get("gda.nexus.floatfillvalue", "nan");
			return floatFill.equalsIgnoreCase("nan") ? Double.NaN : Double.parseDouble(floatFill);
		} else if (clazz.equals(Float.class)) {
			String floatFill = LocalProperties.get("gda.nexus.floatfillvalue", "nan");
			return floatFill.equalsIgnoreCase("nan") ? Float.NaN : Float.parseFloat(floatFill);
		} else if (clazz.equals(Byte.class)) {
			return (byte) 0;
		} else if (clazz.equals(Short.class)) {
			return (short) 0;
		} else if (clazz.equals(Integer.class)) {
			return 0;
		} else if (clazz.equals(Long.class)) {
			return (long) 0;
		}
		return null;
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
	private Object[] getPositionArray() throws NexusException {
		try {
			final Object position = scannable.getPosition();
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
		} catch (DeviceException e) {
			throw new NexusException("Could not get position of device: " + scannable.getName(), e);
		}
	}

	/**
	 * Add the attributes for the given attribute container into the given nexus object.
	 *
	 * @param positioner
	 * @param container
	 * @throws NexusException
	 *             if the attributes could not be added for any reason
	 * @throws DeviceException
	 */
	private void registerAttributes(NXobject nexusObject) throws NexusException {
		// We create the attributes, if any
		nexusObject.setField("name", scannable.getName());

		try {
			Set<String> attributeNames = scannable.getScanMetadataAttributeNames();
			for (String attrName : attributeNames) {
				try {
					nexusObject.setField(attrName, scannable.getScanMetadataAttribute(attrName));
				} catch (Exception e) {
					throw new NexusException(
							MessageFormat.format("An exception occurred attempting to get the value of the attribute ''{0}'' for the device ''{1}''",
									scannable.getName(), attrName));
				}
			}
		} catch (DeviceException e) {
			throw new NexusException("Could not get attributes of device: " + getName());
		}
	}

	/**
	 * Return the slice to set. The same slice works for all
	 * lazy writable datasets as they all have the same shape, i.e. the scan shape
	 * @param pos
	 * @return
	 */
	private SliceND getSliceForPosition(IPosition pos) {
		String firstFieldName = getFieldNames().get(0);
		ILazyWriteableDataset dataset = writableDatasets.get(firstFieldName);
		IScanSlice scanSlice = IScanRankService.getScanRankService().createScanSlice(pos);

		return new SliceND(dataset.getShape(), dataset.getMaxShape(),
				scanSlice.getStart(), scanSlice.getStop(), scanSlice.getStep());
	}

	/**
	 * Write the given positions of the {@link Scannable}
	 * @param demandPosition the demand value of the scannable (assumed to be a single value)
	 * @param actualPosition the actual position of the scannable
	 * @param scanPosition the position of the overall scan
	 * @throws Exception if the position cannot be written for any reason
	 */
	private void write(Object demandPosition, Object[] actualPosition, IPosition scanPosition) throws Exception {
		if (actualPosition.length < writableDatasets.size()) {
			throw new NexusException(MessageFormat.format("getPosition() of scannable ''{0}'' must be an array of length at least: {1}",
					getName(), writableDatasets.size()));
		}

		// write the actual position (potentially multi-valued)
		int fieldIndex = 0;
		SliceND sliceND = getSliceForPosition(scanPosition); // the location in each dataset to write to
		for (String fieldName : writableDatasets.keySet()) {
			// we rely on predictable iteration order for LinkedHashSet of writable datasets
			final IDataset value = DatasetFactory.createFromObject(actualPosition[fieldIndex]);
			ILazyWriteableDataset dataset = writableDatasets.get(fieldName);
			dataset.setSlice(null, value, sliceND);
			fieldIndex++;
		}

		// write the demand position to the demand dataset
		if (demandPosition != null && demandValueDataset != null) {
			int index = scanPosition.getIndex(getName());
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
	 * Returns the {@link DataNode} for the given field name.
	 * @param fieldName field name
	 * @return data node for given field name, or <code>null</code> if no such data node exists
	 */
	public DataNode getDataNode(String fieldName) {
		return nexusObject.getDataNode(fieldName);
	}

}
