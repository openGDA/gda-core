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

package gda.data.scan.datawriter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXbending_magnet;
import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;

import gda.factory.FindableBase;
import uk.ac.diamond.daq.scanning.BeamNexusDevice;
import uk.ac.diamond.daq.scanning.InsertionDeviceNexusDevice;
import uk.ac.diamond.daq.scanning.MonochromatorNexusDevice;
import uk.ac.diamond.daq.scanning.SourceNexusDevice;

/**
 * A bean specifying the names of common device types present on most beamlines. This is
 * used to add metadata for these devices to the nexus tree for all scans on the beamline
 * when using {@link NexusScanDataWriter} or new scanning. Typically, each device will
 * be an instance of some class implementing {@link INexusDevice} and be registered
 * with the {@link INexusDeviceService}.
 */
public class CommonBeamlineDevicesConfiguration extends FindableBase {

	private String sourceName;

	private String insertionDeviceName;

	private String bendingMagnetName;

	private String monochromatorName;

	private String beamName;

	/**
	 * Returns the name of the {@link INexusDevice} that will contribute the {@link NXsource} group
	 * to the nexus file. The class {@link SourceNexusDevice} is provided for this purpose.
	 * @return name of source nexus device
	 */
	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	/**
	 * Returns the name of the {@link INexusDevice} that will contribute the {@link NXinsertion_device}
	 * group to the nexus file. The class {@link InsertionDeviceNexusDevice} is provided for this purpose.
	 * Note that only this field or {@link #getBendingMagnetName()} should be set.
	 * @return name of insertion device nexus device
	 */
	public String getInsertionDeviceName() {
		return insertionDeviceName;
	}

	public void setInsertionDeviceName(String insertionDeviceName) {
		this.insertionDeviceName = insertionDeviceName;
	}

	/**
	 * Returns the name of the {@link INexusDevice} that will contribute the {@link NXbending_magnet}
	 * group to the nexus file.
	 * Note that only this field or {@link #getInsertionDeviceName()} should be set.
	 * @return name of the bending magnet nexus device
	 */
	public String getBendingMagnetName() {
		return bendingMagnetName;
	}

	public void setBendingMagnetName(String bendingMagnetName) {
		this.bendingMagnetName = bendingMagnetName;
	}

	/**
	 * Returns the name of the {@link INexusDevice} that will contribute the {@link NXbending_magnet}
	 * group to the nexus file. The class {@link MonochromatorNexusDevice} is provided for this purpose.
	 * @return name of monochromator nexus device
	 */
	public String getMonochromatorName() {
		return monochromatorName;
	}

	public void setMonochromatorName(String monochromatorName) {
		this.monochromatorName = monochromatorName;
	}

	/**
	 * Returns the name of the {@link INexusDevice} that will contribute the {@link NXbeam}
	 * group to the nexus file. The class {@link BeamNexusDevice} is provided for this purpose.
	 * @return name of beam nexus device
	 */
	public String getBeamName() {
		return beamName;
	}

	public void setBeamName(String beamName) {
		this.beamName = beamName;
	}

	public Set<String> getCommonDeviceNames() throws NexusException {
		if (sourceName == null) throw new NexusException("Source device name must be set");
		if (insertionDeviceName == null && bendingMagnetName == null) throw new NexusException("Insertion device or bending magnet name must be set");
		if (insertionDeviceName != null && bendingMagnetName != null) throw new NexusException("Only one of insertion device or bending magnet name can be set");
		if (monochromatorName == null) throw new NexusException("Monochromator name must be set");
		if (beamName == null) throw new NexusException("Beam name must be set");

		// TODO use Set.of when we can use Java 9 source code.
		return new HashSet<>(Arrays.asList(sourceName, insertionDeviceName != null ? insertionDeviceName : bendingMagnetName, monochromatorName, beamName));
	}

}