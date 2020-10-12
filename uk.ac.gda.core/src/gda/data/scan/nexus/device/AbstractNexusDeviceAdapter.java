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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.dawnsci.nexus.scan.NexusScanFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.IWritableNexusDevice;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * Abstract superclass of classes that adapt {@link Scannable}s (including {@link Detector}s)
 * to {@link INexusDevice}, so that they can be used by the {@link NexusScanFileService}.
 *
 * @param <N>
 */
public abstract class AbstractNexusDeviceAdapter<N extends NXobject> implements IWritableNexusDevice<N> {

	private static final String PROPERTY_NAME_FLOAT_FILL_VALUE = "gda.nexus.floatfillvalue";

	private static final List<String> SPECIAL_ATTRIBUTES =
			Collections.unmodifiableList(Arrays.asList(Scannable.ATTR_NX_CLASS, Scannable.ATTR_NEXUS_CATEGORY));

	private static Logger logger = LoggerFactory.getLogger(AbstractNexusDeviceAdapter.class);

	private final Scannable device;

	private N nexusObject = null;

	public AbstractNexusDeviceAdapter(final Scannable device) {
		this.device = device;
	}

	@Override
	public String getName() {
		return device.getName();
	}

	protected Scannable getDevice() {
		return device;
	}

	/**
	 * Add the attributes for the given attribute container into the given nexus object.
	 * TODO: consider removing this and providing an easier mechanism. see DAQ-3186.
	 *
	 * @throws NexusException if the attributes could not be added for any reason
	 */
	protected void registerAttributes(NXobject nexusObject) throws NexusException {
		// We create the attributes, if any
		try {
			final Set<String> attributeNames = getDevice().getScanMetadataAttributeNames();
			for (String attrName : attributeNames) {
				addAttribute(nexusObject, getDevice(), attrName);
			}
		} catch (DeviceException e) {
			throw new NexusException("Could not get attributes of device: " + getName());
		}
	}

	private void addAttribute(NXobject nexusObject, final Scannable scannable, String attrName) throws NexusException {
		if (!SPECIAL_ATTRIBUTES.contains(attrName)) {
			try {
				nexusObject.setField(attrName, scannable.getScanMetadataAttribute(attrName));
			} catch (Exception e) {
				throw new NexusException(
						MessageFormat.format("An exception occurred attempting to get the value of the attribute ''{0}'' for the device ''{1}''",
								scannable.getName(), attrName));
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public NexusObjectProvider<N> getNexusProvider(NexusScanInfo info) throws NexusException {
		logger.debug("Creating nexus object for device {}", getName());
		if (getDevice() instanceof INexusDevice<?>) {
			return ((INexusDevice<N>) getDevice()).getNexusProvider(info);
		}

		nexusObject = createNexusObject(info);
		return createNexusProvider(nexusObject, info);
	}

	protected abstract N createNexusObject(NexusScanInfo info) throws NexusException;

	protected NexusObjectProvider<N> createNexusProvider(N nexusObject, NexusScanInfo info) throws NexusException {
		final NexusObjectWrapper<N> nexusWrapper = new NexusObjectWrapper<N>(getName(), nexusObject);
		configureNexusWrapper(nexusWrapper, info);
		return nexusWrapper;
	}

	/**
	 * Configure the nexus wrapper, an object that contains the nexus object and contains additional information
	 * describing how the nexus object should be added to the nexus tree - in particular to {@link NXdata} groups.
	 *
	 * @param nexusWrapper
	 * @param info
	 * @throws NexusException
	 */
	protected void configureNexusWrapper(NexusObjectWrapper<N> nexusWrapper, @SuppressWarnings("unused") NexusScanInfo info) throws NexusException {
		nexusWrapper.setPrimaryDataFieldName(getPrimaryDataFieldName());
	}

	protected String getPrimaryDataFieldName() {
		return null;
	}

	protected static Object getFillValue(Class<?> clazz) {
		if (clazz.equals(Double.class)) {
			String floatFill = LocalProperties.get(PROPERTY_NAME_FLOAT_FILL_VALUE, "nan");
			return floatFill.equalsIgnoreCase("nan") ? Double.NaN : Double.parseDouble(floatFill);
		} else if (clazz.equals(Float.class)) {
			String floatFill = LocalProperties.get(PROPERTY_NAME_FLOAT_FILL_VALUE, "nan");
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



}
