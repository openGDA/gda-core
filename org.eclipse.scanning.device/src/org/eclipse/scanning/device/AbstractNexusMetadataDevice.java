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

package org.eclipse.scanning.device;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNexusMetadataDevice<N extends NXobject> implements INexusDevice<N> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractNexusMetadataDevice.class);

	private String name;

	private NexusBaseClass nexusClass = null;

	private NexusBaseClass nexusCategory = null;

	/**
	 * A map of the fields in this device, keyed by name.
	 */
	private Map<String, MetadataField> fields = new HashMap<>();

	protected AbstractNexusMetadataDevice(NexusBaseClass nexusBaseClass) {
		nexusClass = nexusBaseClass;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addField(MetadataField field) {
		fields.put(field.getName(), field);
	}

	public void addScannableField(String fieldName, String scannableName) {
		final ScannableField field = new ScannableField(); // TODO or create a constructor?
		field.setName(fieldName);
		field.setScannableName(scannableName);
		addField(field);
	}

	public void addScalarField(String fieldName, Object fieldValue) {
		final ScalarField field = new ScalarField();
		field.setName(fieldName);
		field.setValue(fieldValue);
		addField(field);
	}

	@Override
	public void register() {
		INexusDevice.super.register();
		checkPropertiesSet();
	}

	protected void checkPropertiesSet() {
		for (Field field : getClass().getFields()) {
			field.setAccessible(true);
			try {
				if (field.get(this) == null) {
					logger.warn("property {} not set for {} ''{}''", field.getName(), getClass().getSimpleName(), getName());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error("TODO put description of error here", e);
			}
		}
	}

	@Override
	public NexusObjectProvider<N> getNexusProvider(NexusScanInfo info) throws NexusException {
		final N nexusObject = createNexusObject(info);
		writeFields(nexusObject);
		final NexusObjectWrapper<N> nexusWrapper = createAndConfigureNexusWrapper(nexusObject);
		return nexusWrapper;
	}

	protected NexusObjectWrapper<N> createAndConfigureNexusWrapper(final N nexusObject) {
		final NexusObjectWrapper<N> nexusWrapper = new NexusObjectWrapper<>(getName(), nexusObject);
		nexusWrapper.setCategory(getCategory());
		return nexusWrapper;
	}

	public void setCategory(NexusBaseClass nexusCategory) {
		this.nexusCategory = nexusCategory;
	}

	public NexusBaseClass getCategory() {
		return nexusCategory;
	}

	@SuppressWarnings("unchecked")
	protected N createNexusObject(@SuppressWarnings("unused") NexusScanInfo info) {
		return (N) NexusNodeFactory.createNXobjectForClass(getNexusBaseClass());
	}

	public final NexusBaseClass getNexusBaseClass() {
		return nexusClass;
	}

	protected void writeFields(N nxObject) throws NexusException {
		for (MetadataField field : fields.values()) {
			final String fieldName = field.getName();
			if (field instanceof ScannableField) {
				final ScannableField scannableField = (ScannableField) field;
				writeScannableValue(nxObject, fieldName, scannableField.getScannableName());
			}
			if (field instanceof ScalarField) { // TODO note: to be refactored in a later commit
				final ScalarField scalarField = (ScalarField) field;
				writeFieldValue(nxObject, fieldName, scalarField.getValue());
			}
		}
	}

	protected <T> IScannable<T> getScannable(String scannableName) throws NexusException {
		try {
			return Services.getScannableDeviceService().getScannable(scannableName);
		} catch (ScanningException e) {
			throw new NexusException("Could not find scannable with name: " + scannableName);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T getScannableValue(String scannableName) throws ScanningException, NexusException {
		return (T) getScannable(scannableName).getPosition();
	}

	protected void writeScannableValue(NXobject nxObject, String fieldName, String scannableName) throws NexusException {
		if (scannableName == null) return; // property not set, ignore

		try {
			final Object scannableValue = getScannable(scannableName).getPosition();
			nxObject.setField(fieldName, scannableValue);
			// TODO write units?
		} catch (ScanningException e) {
			throw new NexusException("Could not find scannable with name: " + scannableName);
		}
	}

	protected void writeFieldValue(NXobject nxObject, String fieldName, Object value) {
		if (value != null) {
			nxObject.setField(fieldName, value);
		}
	}

}
