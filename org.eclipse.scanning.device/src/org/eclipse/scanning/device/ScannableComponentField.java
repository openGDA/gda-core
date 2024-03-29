/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.device.utils.ScannableUtils;

import gda.device.Scannable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;

/**
 * A {@link MetadataNode} that creates a {@link DataNode} whose value
 * is the value of a particular component of a {@link Scannable}, *
 *
 * The component to be used is the element of the position array for the
 * scannable at the index determined as follows:
 * <ul>
 * <li>If the property {@code componentName} is set, the index of that
 * name in the array that is the contatenation of
 * {@link Scannable#getInputNames()} and {@link Scannable#getExtraNames()}
 * </li>
 * <li>if {@code componentIndex}, then that index will be used.</li>
 * </ul>
 */
public class ScannableComponentField extends AbstractMetadataField {

	private String scannableName;

	private String componentName;

	private int componentIndex;

	public ScannableComponentField() {
		// no-arg constructor for spring initialization
	}

	public ScannableComponentField(String fieldName, String scannableName, String componentName) {
		super(fieldName);
		setScannableName(scannableName);
		setComponentName(componentName);
	}

	public ScannableComponentField(String fieldName, String scannableName, int componentIndex) {
		super(fieldName);
		setScannableName(scannableName);
		setComponentIndex(componentIndex);
	}

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
		final Object[] positionArray = ScannableUtils.getPositionArray(scannable);
		return positionArray[componentIndexToUse];
	}


	@Override
	protected String getLocalName() throws NexusException {
		final String fieldName = getFieldName();
		return fieldName == null ? null : scannableName + "." + fieldName;
	}

	private String getFieldName() throws NexusException {
		if (componentName != null) {
			return componentName;
		}

		// use the index
		final Scannable scannable = getScannable();
		final int componentIndexToUse = getComponentIndexToUse(scannable);
		final String[] inputNames = scannable.getInputNames();
		final String[] extraNames = scannable.getExtraNames();
		if (componentIndexToUse < inputNames.length) {
			return inputNames[componentIndexToUse];
		} else {
			return extraNames[componentIndexToUse - inputNames.length];
		}
	}

	private Scannable getScannable() throws NexusException {
		Object scannableObj = Finder.find(scannableName);
		if (scannableObj == null) {
			scannableObj = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
		}

		if (scannableObj instanceof Scannable scannable) {
			return scannable;
		}
		throw new NexusException("Cannot find scannable with name: " + scannableName);
	}

	@Override
	protected Object getFieldValue() throws NexusException {
		return getComponentValue();
	}

}
