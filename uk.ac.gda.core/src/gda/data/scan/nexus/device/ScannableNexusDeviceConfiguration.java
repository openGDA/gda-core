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

import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;

import gda.data.ServiceHolder;
import gda.device.Scannable;

/**
 * An instance of this class can be defined in order to configure the nexus object
 * (a subclass of {@link NXobject}) for a {@link Scannable}. An instance of this class can be
 * instantiated in spring using {@code init-method="register"}.
 */
public class ScannableNexusDeviceConfiguration {

	private String scannableName;

	private NexusBaseClass nexusBaseClass;

	private NexusBaseClass nexusCategory;

	private String collectionName;

	private String[] fieldPaths;

	private String[] units;

	public void register() {
		ServiceHolder.getScannableNexusDeviceConfigurationRegistry().addScannableNexusDeviceConfiguration(this);
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	public void setNexusBaseClass(NexusBaseClass nexusBaseClass) {
		this.nexusBaseClass = nexusBaseClass;
	}

	public void setNexusClass(String nxClass) {
		this.nexusBaseClass = NexusBaseClass.getBaseClassForName(nxClass);
	}

	public NexusBaseClass getNexusBaseClass() {
		return nexusBaseClass;
	}

	public NexusBaseClass getNexusCategory() {
		return nexusCategory;
	}

	public void setNexusCategory(NexusBaseClass nexusCategory) {
		this.nexusCategory = nexusCategory;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public void setFieldPaths(String[] fieldPaths) {
		// using an array allows the spring xml to use a single string in the case of a single element
		this.fieldPaths = fieldPaths;
	}

	public String[] getFieldPaths() {
		return fieldPaths;
	}

	public void setUnits(String[] units) {
		// using an array allows the spring xml to use a single string in the case of a single element
		this.units = units;
	}

	public String[] getUnits() {
		return units;
	}

}
