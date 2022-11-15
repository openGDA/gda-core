/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_SCANNABLE_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_SCAN_ROLE;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.COLLECTION_NAME_SCANNABLES;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.FIELD_NAME_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.FIELD_NAME_VALUE_SET;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXcollection;
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
import org.eclipse.scanning.api.points.IPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.ServiceHolder;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;
import gda.device.Scannable;
import gda.device.ScannableMotion;

/**
 * A class that wraps a {@link Scannable} to implement {@link INexusDevice} in order to create one or more
 * nexus objects to add to the nexus tree. This class is the default implementation of {@link INexusDevice}
 * for a {@link Scannable} and will be used if either no {@link ScannableNexusDeviceConfiguration} is defined.
 * <p>
 * The nexus writing framework calls the method {@link #getNexusProviders(NexusScanInfo)} which returns
 * a {@link NexusObjectProvider} for each nexus object to be added to the tree. See the Javadoc for that method
 * for details.
 *
 * @see DefaultScannableNexusDevice#getNexusProviders(NexusScanInfo)
 *
 * @param <N> this type variable is irrelevant in this implementation as {@link #getNexusProvider(NexusScanInfo)}
 *     is not called. Instead {@link #getNexusProviders(NexusScanInfo)} returns multiple {@link NexusObjectProvider}s
 *     which may create different types of nexus object.
 */
public class DefaultScannableNexusDevice<N extends NXobject> extends AbstractScannableNexusDevice<N> {

	private static final Logger logger = LoggerFactory.getLogger(DefaultScannableNexusDevice.class);

	/**
	 * Whether the demand value (i.e. the position the scannable was set to by the scan) should
	 * be written. Should be set to <code>false</code> if this information is not available in the scan.
	 */
	private boolean writeDemandValue = false;

	/**
	 * The data node for the demand value
	 */
	private DataNode demandValueDataNode = null;

	public DefaultScannableNexusDevice(Scannable scannable) {
		super(scannable);
	}

	public DefaultScannableNexusDevice(Scannable scannable, boolean writeDemandValue) {
		super(scannable);
		setWriteDemandValue(writeDemandValue);
	}

	public void setWriteDemandValue(boolean writeDemandValue) {
		this.writeDemandValue = writeDemandValue;
	}

	protected boolean shouldWriteDemandValue(NexusScanInfo scanInfo) {
		final ScanRole scanRole = scanInfo.getScanRole(getName());
		return scanRole == ScanRole.SCANNABLE && writeDemandValue;
	}

	/**
	 * The number and type of these depends on the number of input names and extra names the scannable has, as returned
	 * by {@link Scannable#getInputNames()} and {@link Scannable#getExtraNames()} as follows.
	 *
	 * <ul>
	 * <li>For a scannable with a single input name, a {@link List} containing a single {@link NexusObjectProvider} is
	 * created. This contains an {@link NXpositioner} with a {@link NXpositioner#NX_VALUE} {@link DataNode} for the
	 * single input name, and additional data nodes for each extra name (with that name).</li>
	 * <li>For a scannable with multiple input names, the list will contain multiple {@link NexusObjectProvider}s:
	 * <ul>
	 * <li>A {@link NexusObjectProvider} for each input name, containing an {@link NXpositioner} with a
	 * {@link NXpositioner#NX_VALUE} {@link DataNode} for the input name</li>
	 * <li>A {@link NexusObjectProvider} containing an {@link NXcollection} with the the same {@link DataNode}s for
	 * each input name  ontained in the {@link NXpositioner}s, but here each data name within the collection will be the
	 * input name that it is the data node for. Additionally the collection contains a data node for each extra name (with that name).</li>
	 * </ul>
	 * </li>
	 * </ul>
	 */
	@Override
	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {
		final Scannable scannable = getScannable();
		if (scannable instanceof INexusDevice) {
			@SuppressWarnings("unchecked")
			final INexusDevice<N> nexusDevice = (INexusDevice<N>) getScannable();
			return nexusDevice.getNexusProviders(info);
		}

		// create the DataNodes for the inputNames, extraNames and demand value if applicable
		final ScanRole scanRole = info.getScanRole(getName());
		try {
			createDataNodes(info);
		} catch (NexusException e) {
			if (scanRole == ScanRole.MONITOR_PER_SCAN) {
				logger.error("Could not create nexus object for scannable {}", scannable.getName(), e);
				return emptyList();
			}
			throw e;
		}

		return createNexusObjectProviders(scannable, scanRole, info);
	}

	private List<NexusObjectProvider<?>> createNexusObjectProviders(final Scannable scannable, final ScanRole scanRole,
			NexusScanInfo info) throws NexusException {
		final List<NexusObjectProvider<?>> nexusProviders = new ArrayList<>();

		// for scannables with multiple (or zero) input fields, create an NXcollection with links and extra fields
		final String[] inputNames = scannable.getInputNames();
		if (scannable.getInputNames().length != 1) { // will also cover no input field case
			nexusProviders.add(createCollectionProvider(info));
		}

		// default behaviour - create an NXpositioner for each field
		for (int fieldIndex = 0; fieldIndex < inputNames.length; fieldIndex++) {
			final boolean isSingleInputField = fieldIndex == 0 && inputNames.length == 1;
			if (getFieldDataNode(inputNames[fieldIndex]) != null) {
				nexusProviders.add(createNexusProviderForInputField(inputNames[fieldIndex], fieldIndex, scanRole, isSingleInputField));
			}
		}

		return nexusProviders;
	}

	@Override
	protected void createDataNodes(NexusScanInfo scanInfo) throws NexusException {
		super.createDataNodes(scanInfo);

		if (shouldWriteDemandValue(scanInfo)) {
			demandValueDataNode = createDemandValueField();
		}
	}

	protected NexusObjectProvider<N> createNexusProviderForInputField(String inputName, int fieldIndex,
			ScanRole scanRole, boolean isSingleField) throws NexusException {
		final N nexusObject = createPositionerForInputName(inputName, fieldIndex, scanRole, isSingleField);
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
			nexusWrapper.setDefaultAxisDataFieldName(demandValueDataNode != null ? FIELD_NAME_VALUE_SET : NXpositioner.NX_VALUE);
		}

		return nexusWrapper;
	}

	private N createPositionerForInputName(String inputName, int fieldIndex, ScanRole scanRole,
			boolean isSingleInputField) throws NexusException {
		final NexusBaseClass nexusBaseClass = isSingleInputField ? getNexusBaseClass() : NexusBaseClass.NX_POSITIONER;
		final String scannableName = getName();

		@SuppressWarnings("unchecked") // the default nexus class of NXpositioner can be overridden
		final N positioner = (N) NexusNodeFactory.createNXobjectForClass(nexusBaseClass);

		positioner.setField(FIELD_NAME_NAME, isSingleInputField ? scannableName : scannableName + "." + inputName);
		// Attributes to identify the scannables so that the nexus file can be reverse engineered
		positioner.setAttribute(null, ATTRIBUTE_NAME_GDA_SCANNABLE_NAME, scannableName);
		positioner.setAttribute(null, ATTRIBUTE_NAME_GDA_SCAN_ROLE, scanRole.toString().toLowerCase());

		if (positioner instanceof NXpositioner) {
			final NXpositioner nxPositioner = (NXpositioner) positioner;
			writeLimits(nxPositioner, fieldIndex);
			writeControllerRecordName(nxPositioner);
		}

		// create the 'value' data node for this input field
		final String dataNodeName = positioner instanceof NXpositioner ? NXpositioner.NX_VALUE : inputName;
		final DataNode dataNode = getFieldDataNode(inputName);
		if (dataNode != null) { // can be the case for a null-valued field for metadata scannables
			positioner.addDataNode(dataNodeName, dataNode);

			if (isSingleInputField) {
				// we don't create an NXcollection for a single field, so add extra fields and attributes here
				addExtraNameFields(positioner);
				registerAttributes(positioner);
				// create the 'value_set' (demand value) data node if applicable (new scanning only)
				if (demandValueDataNode != null) {
					positioner.addDataNode(FIELD_NAME_VALUE_SET, demandValueDataNode);
				}
			}
		}

		return positioner;
	}

	private void addAllFields(final NXobject nexusObject) {
		addFields(getFieldNames(), nexusObject);
	}

	private void addExtraNameFields(final NXobject nexusObject) {
		addFields(getScannable().getExtraNames(), nexusObject);
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

	private DataNode createDemandValueField() {
		// create the demand value dataset (name 'value_set'). Note dataset is not added to the
		// writeableDatasets map as the order of entries in that map corresponds to elements in
		// getPositionArray, which does not include the demand value
		final ILazyWriteableDataset demandValueDataset = createLazyWritableDataset(FIELD_NAME_VALUE_SET,
				Double.class, 1, new int[] { 1 });
		final DataNode dataNode = NexusNodeFactory.createDataNode();
		dataNode.setDataset(demandValueDataset);
		dataNode.addAttribute(TreeFactory.createAttribute(ATTRIBUTE_NAME_LOCAL_NAME, getName() + "." + FIELD_NAME_VALUE_SET));
		return dataNode;
	}

	@Override
	public Object writePosition(Object demandPosition, IPosition scanPosition) throws Exception {
		if(nexusRole!=null && nexusRole!=NexusRole.PER_POINT) {
			return demandPosition;
		}
		final Object position = super.writePosition(demandPosition, scanPosition);
		writeDemandPosition(demandPosition, scanPosition.getIndex(getName()));
		return position;
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

	private NexusObjectProvider<?> createCollectionProvider(NexusScanInfo info) throws NexusException {
		final NXobject nexusObject = createCollection(info);

		// create and configure the NexusObjectProvider for the collection.
		final NexusObjectWrapper<?> nexusProvider = new NexusObjectWrapper<>(getName(), nexusObject);
		if (hasLocationMapEntry()) {
			nexusProvider.setCollectionName(COLLECTION_NAME_SCANNABLES);
		}
		final NexusBaseClass category = getNexusCategory();
		nexusProvider.setCategory(category == null ? NexusBaseClass.NX_INSTRUMENT : category); // collection would be added to NXentry not NXinstrument by default

		// as the NXcollection has the same name as the device, this is the NexusObjectProvider that will be used as the default axis
		final String[] inputNames = getScannable().getInputNames();
		if (inputNames.length == 1) throw new IllegalStateException(); // sanity check, no NXcollection for single input field case
		final String primaryDataFieldName = getPrimaryDataFieldName();
		nexusProvider.setPrimaryDataFieldName(primaryDataFieldName);
		nexusProvider.addAxisDataFieldNames(inputNames.length > 0 ? inputNames : new String[] { getScannable().getExtraNames()[0] });
		nexusProvider.setDefaultAxisDataFieldName(primaryDataFieldName);
		return nexusProvider;
	}

	private NXobject createCollection(NexusScanInfo info) throws NexusException {
		// honour NexusBaseClass for no-input field scannables (DAQ-3776)
		final NexusBaseClass nexusBaseClass = getScannable().getInputNames().length == 0 ?
				getNexusBaseClass() : NexusBaseClass.NX_COLLECTION;
		final NXobject nexusObject = NexusNodeFactory.createNXobjectForClass(nexusBaseClass);
		final String scannableName = getName();
		nexusObject.setAttribute(null, ATTRIBUTE_NAME_GDA_SCANNABLE_NAME, scannableName);
		nexusObject.setAttribute(null, ATTRIBUTE_NAME_GDA_SCAN_ROLE, info.getScanRole(scannableName).toString().toLowerCase());
		nexusObject.setField(FIELD_NAME_NAME, scannableName);

		// add links to input fields and extra fields
		addAllFields(nexusObject);
		// add extra name fields and attributes
		registerAttributes(nexusObject);
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

			return ScannableLocationMapWriter.createScannableLocationMapWriter(this, writer);
		} else {
			logger.warn("Cannot use NexusDataWriter location map to write device {}. "
					+ "Writer is not an instanceof of {}", getName(), SingleScannableWriter.class);
			return null;
		}
	}

	private boolean hasLocationMapEntry() {
		return ServiceHolder.getNexusDataWriterConfiguration().getLocationMap().containsKey(getName());
	}

	@Override
	public void scanEnd() throws NexusException {
		super.scanEnd();
		demandValueDataNode = null;
	}

}
