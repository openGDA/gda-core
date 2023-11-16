/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import java.lang.reflect.Array;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.nexus.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;

/**
 * A {@link MetadataAttribute} that creates an {@link Attribute} node whose value
 * is the value of a particular component of a {@link Scannable}.
 * <br><br>
 * The component to be used is the element of the position array for the
 * scannable at the index determined as follows:
 * <ul>
 * <li>If the property {@code componentName} is set, the index of that
 * name in the array that is the concatenation of
 * {@link Scannable#getInputNames()} and {@link Scannable#getExtraNames()}
 * </li>
 * <li>Otherwise, if {@code componentIndex} is set, then that index will be used.</li>
 * <li>If neither or both are set in Spring, a BeanCreationException will be raised.</li>
 * </ul>
 */
public class ScannableComponentMetadataAttribute extends AbstractMetadataAttribute implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(ScannableComponentMetadataAttribute.class);
	private String scannableName = null;
	private String componentName = null;
	private int componentIndex = -1;

	// Properties for Spring

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public int getComponentIndex() {
		return componentIndex;
	}

	public void setComponentIndex(int componentIndex) {
		this.componentIndex = componentIndex;
	}

	// InitializingBean interface

	@Override
	public void afterPropertiesSet() throws Exception {
		final var scannableNameAvailable = scannableName != null && !scannableName.isBlank();
		final var componentNameAvailable = componentName != null && !componentName.isBlank();
		final var componentIndexAvailable = componentIndex >= 0;

		if (!scannableNameAvailable) {
			throw new BeanCreationException(getName(), "A scannableName is required");
		}

		if (componentIndexAvailable == componentNameAvailable) {
			logger.error("Either componentName ({}) or componentIndex ({}) must be specified for scannable '{}' in '{}'",
				componentName, componentIndex, scannableName, getName());
			throw new BeanCreationException(getName(), String.format(
				"Either componentName or componentIndex must be specified for scannable '%s'", scannableName));
		}
	}

	// Class functions

	private int getComponentIndexToUse(Scannable scannable) throws NexusException {
		if (componentName != null) {
			final int inputNameIndex = ArrayUtils.indexOf(scannable.getInputNames(), componentName);
			if (inputNameIndex != -1) {
				return inputNameIndex;
			}

			final int extraNameIndex = ArrayUtils.indexOf(scannable.getExtraNames(), componentName);
			if (extraNameIndex != -1) {
				return extraNameIndex + scannable.getInputNames().length;
			}

			throw new NexusException("Scannable '" + scannableName + "' has no such component '" + componentName);
		} else if (componentIndex < 0) {
			throw new NexusException("One of componentName or componentValue must be defined for " + ScannableComponentField.class.getSimpleName());
		} else {
			return componentIndex;
		}
	}

	private Object getComponentValue() throws NexusException {
		final Scannable scannable = getScannable();
		final int componentIndexToUse = getComponentIndexToUse(scannable);
		final Object[] positionArray = getPositionArray(scannable);
		return positionArray[componentIndexToUse];
	}

	/**
	 * Converts the position of the given {@link Scannable} (as returned by
	 * {@link Scannable#getPosition()}) as an array.
	 * <ul>
	 *   <li>The position is not an array, just an object of some kind.
	 *   	A single-valued array is returned containing that object;</li>
	 *   <li>The position is a primitive array. It is converted to an array of Objects, each
	 *      element of which is a wrapper of the primitive at that index of primitive array;</li>
	 *   <li>The position is already an object array. It is returned as is.</li>
	 * </ul>
	 *
	 * TODO: copied from AbstractScannableNexusDevice. Refactor to some common location?
	 * @return position as an array
	 * @throws NexusException
	 */
	private Object[] getPositionArray(Scannable scannable) throws NexusException {
		try {
			final Object position = scannable.getPosition();

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

	private Scannable getScannable() throws NexusException {
		Object scannableObj = Finder.find(scannableName);
		if (!(scannableObj instanceof Scannable)) {
			scannableObj = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
		}

		if (scannableObj instanceof Scannable scannable) {
			return scannable;
		}
		throw new NexusException("Cannot find scannable with name: " + scannableName);
	}

	// AbstractMetadataAttribute interface

	@Override
	public Object getValue() throws NexusException {
		return getComponentValue();
	}
}
