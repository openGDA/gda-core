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

package org.eclipse.scanning.device;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ScanningException;

import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * A {@link MetadataNode} field that creates a {@link DataNode} the value of which is
 * the position of a scannable identified by the {@code scannableName}.
 * If the units property is not specified in this object, and the scannable has units,
 * then these are written instead. See ({@link IScannable#getUnit()} {@link ScannableMotionUnits#getUserUnits()}.
 */
public class ScannableField extends AbstractMetadataField {

	private String scannableName;

	public ScannableField() {
		// no-arg constructor for spring initialization
	}

	public ScannableField(String fieldName, String scannableName) {
		super(fieldName);
		setScannableName(scannableName);
	}

	public ScannableField(String fieldName, String scannableName, boolean failOnError) {
		super(fieldName);
		setScannableName(scannableName);
		setFailOnError(failOnError);
	}

	public ScannableField(String fieldName, String scannableName, String units) {
		super(fieldName);
		setScannableName(scannableName);
		setUnits(units);
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	@Override
	public String getUnits() throws NexusException {
		String units = super.getUnits();
		if (units == null) {
			units = getScannable().getUnit();
		}

		return units;
	}

	@Override
	protected String getLocalName() throws NexusException {
		final String fieldName = getFieldName();
		return scannableName + (fieldName == null ? "" : "." + fieldName);
	}

	private String getFieldName() {
		Object scannableObj = Finder.find(scannableName);
		if (scannableObj == null) {
			scannableObj = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
		}

		if (scannableObj instanceof Scannable scannable) {
			final String[] inputNames = scannable.getInputNames();
			final String[] extraNames = scannable.getExtraNames();
			if (inputNames.length + extraNames.length != 1) {
				// a ScannableField can use a scannable with multiple fields, in which case the dataset
				// will be 1d with size = inputNames.length + extraNames.length
				return null;
			}
			return inputNames.length == 1 ? inputNames[0] : extraNames[0];
		}

		return null;
	}

	private IScannable<?> getScannable() throws NexusException {
		try {
			return ServiceProvider.getService(IScannableDeviceService.class).getScannable(getScannableName());
		} catch (ScanningException e) {
			throw new NexusException("Could not find scannable with name: " + scannableName);
		}
	}

	private Object getScannableValue() throws NexusException {
		try {
			return getScannable().getPosition();
		} catch (ScanningException e) {
			throw new NexusException("Could not get position for scannable with name: " + scannableName);
		}
	}

	@Override
	protected Object getFieldValue() throws NexusException {
		return getScannableValue();
	}

}
