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

package gda.data.scan.nexus.device;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;

import gda.device.Scannable;

public final class GDADeviceNexusConstants {

	/**
	 * A boolean property that, if set to {@code true} causes an {@link Attribute} named {@code decimals} to be added
	 * to {@link DataNode}s for scannables, where the value of the attribute is the number of decimal places
	 * (i.e. precision) specified in the output format for that field, as determined by
	 * {@link Scannable#getOutputFormat()}. E.g. for the format string {@code "%5.3f"} the value will be {@code 3}.
	 */
	public static final String PROPERTY_VALUE_WRITE_DECIMALS = "gda.nexus.scannable.writeDecimals";

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
	 * The attribute name 'target', added to datasets that occur in multiple places
	 * in the nexus tree. The value is the path of first location in the file that the
	 * dataset was found.
	 */
	public static final String ATTRIBUTE_NAME_TARGET = "target";

	/**
	 * The attribute name 'local_name', added to datasets. The value is
	 * the device name and field name, separated by a '.'.
	 */
	public static final String ATTRIBUTE_NAME_LOCAL_NAME = "local_name";

	/**
	 * The attribute name 'gda_scannable_name', added to the group node (typically {@link NXpositioner})
	 * for a scannable. The value is the name of the scannable.
	 */
	public static final String ATTRIBUTE_NAME_GDA_SCANNABLE_NAME = "gda_scannable_name";

	/**
	 * The attribute name 'gda_scan_role' added to the group node for a device.
	 * The value is the {@link ScanRole} of the device in the scan, as a lower-case string.
	 */
	public static final String ATTRIBUTE_NAME_GDA_SCAN_ROLE = "gda_scan_role";

	/**
	 * The attribute name 'gda_field_name' added to datasets. The value is the name of the scannable field
	 * that this dataset is for.
	 */
	public static final String ATTRIBUTE_NAME_GDA_FIELD_NAME = "gda_field_name";

	/**
	 * The attribute name 'decimals'. The value is the number of decimals that should be used
	 * to display the value(s) of the dataset.
	 */
	public static final String ATTRIBUTE_NAME_DECIMALS = "decimals";

	/**
	 * The attribute name 'units' added to datasets. The value is the units for the dataset.
	 */
	public static final String ATTRIBUTE_NAME_UNITS = "units";

	/**
	 * The field name 'value_set', used for the requested value of a scannable,
	 * e.g. a motor. Note that this should be a constant in {@link NXpositioner}, but
	 * it hasn't been added yet. When this has happened, the nexus base classes should be
	 * regenerated and the constant from this {@link NXpositioner} used instead.
	 */
	public static final String FIELD_NAME_VALUE_SET = NXpositioner.NX_VALUE + "_set";

	private GDADeviceNexusConstants() {
		// private constructor to prevent instantiation
	}

}
