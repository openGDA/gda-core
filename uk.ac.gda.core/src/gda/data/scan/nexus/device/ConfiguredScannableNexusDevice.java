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

import java.util.List;
import java.util.Objects;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.DatasetFactory;

import gda.device.Scannable;
import gda.device.ScannableMotion;

/**
 * An instance of this class is created to implement {@link INexusDevice} for a scannable for which
 * a {@link ScannableNexusDeviceConfiguration} bean with the same name exist. The type of nexus object
 * created, the names of its fields are created as specified in the configuration bean, which is normally
 * configured in spring.
 *
 * @param <N> the type of nexus device created
 */
public class ConfiguredScannableNexusDevice<N extends NXobject> extends AbstractScannableNexusDevice<N> {

	private final ScannableNexusDeviceConfiguration config;

	public ConfiguredScannableNexusDevice(Scannable scannable, ScannableNexusDeviceConfiguration config) {
		super(scannable);
		this.config = config;
	}

	@Override
	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {
		createDataNodes(info);

		final N nexusObject = createConfiguredNexusObject(info);
		return List.of(createNexusProvider(nexusObject, info));
	}

	@Override
	protected void configureNexusWrapper(NexusObjectWrapper<N> nexusWrapper, NexusScanInfo info) throws NexusException {
		nexusWrapper.setCategory(getNexusCategory());

		// add all input fields as axis fields
		final String[] inputFields = getScannable().getInputNames();
		final String[] fieldOutputPaths = config.getFieldPaths();
		final String[] axisFieldNames = new String[inputFields.length];
		for (int fieldIndex = 0; fieldIndex < inputFields.length; fieldIndex++) {
			axisFieldNames[fieldIndex] = fieldIndex < fieldOutputPaths.length ? fieldOutputPaths[fieldIndex] : inputFields[fieldIndex];
		}
		nexusWrapper.addAxisDataFieldNames(axisFieldNames);

		// calculate primary data field name
		final int primaryDataFieldIndex = getPrimaryDataFieldIndex();
		final String primaryDataFieldName = fieldOutputPaths[primaryDataFieldIndex];
		nexusWrapper.setPrimaryDataFieldName(primaryDataFieldName);

		nexusWrapper.setDefaultAxisDataFieldName(primaryDataFieldName);
		nexusWrapper.setCollectionName(config.getCollectionName());
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

		final String[] fieldOutputPaths = config.getFieldPaths();
		int fieldIndex = 0;
		for (String fieldName : getFieldNames()) {
			final DataNode dataNode = getFieldDataNode(fieldName);
			Objects.nonNull(dataNode); // sanity check
			final String fieldOutputPath = fieldIndex < fieldOutputPaths.length ? fieldOutputPaths[fieldIndex] : fieldName;
			NexusUtils.addDataNode(nexusObject, dataNode, fieldOutputPath);
			fieldIndex++;
		}

		return nexusObject;
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

	@Override
	protected String getPrimaryDataFieldName() {
		final int primaryDataFieldIndex = getPrimaryDataFieldIndex();
		final String[] fieldPaths = config.getFieldPaths();
		if (fieldPaths != null && primaryDataFieldIndex < fieldPaths.length) {
			return fieldPaths[primaryDataFieldIndex];
		}
		return super.getPrimaryDataFieldName();
	}

	@Override
	protected NexusBaseClass getNexusCategory() throws NexusException {
		if (config.getNexusBaseClass() != null) {
			return config.getNexusCategory();
		}

		return super.getNexusCategory();
	}

	@Override
	protected NexusBaseClass getNexusBaseClass() throws NexusException {
		if (config.getNexusBaseClass() != null) {
			return config.getNexusBaseClass();
		}

		return super.getNexusBaseClass();
	}

	@Override
	protected String getFieldUnits(int fieldIndex) {
		if (config.getUnits() != null && fieldIndex < config.getUnits().length) {
			return config.getUnits()[fieldIndex];
		}
		return super.getFieldUnits(fieldIndex);
	}


}
