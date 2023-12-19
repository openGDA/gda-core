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

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXbending_magnet;
import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FindableBase;
import gda.factory.Finder;

/**
 * A bean specifying the names of common device types present on most beamlines. This is
 * used to add metadata for these devices to the nexus tree for all scans on the beamline
 * when using NexusScanDataWriter or new scanning. Typically, each device will
 * be an instance of some class implementing {@link INexusDevice} and be registered
 * with the {@link INexusDeviceService}.
 */
public class CommonBeamlineDevicesConfiguration extends FindableBase {

	private static Logger logger = LoggerFactory.getLogger(CommonBeamlineDevicesConfiguration.class);

	private String sourceName;

	private String insertionDeviceName;

	private String bendingMagnetName;

	private String monochromatorName;

	private String beamName;

	private String userDeviceName;

	private Set<String> additionalDeviceNames = new HashSet<>();

	private Set<String> disabledDeviceNames = new HashSet<>();

	private Set<String> mandatoryDeviceNames = Collections.emptySet();

	private boolean enforceMandatoryDeviceNames = true;

	private static CommonBeamlineDevicesConfiguration instance = null;

	/**
	 * Returns the configured {@link CommonBeamlineDevicesConfiguration} instance if one has been configured.
	 * In GDA this will normally be configured via spring. Test code should call
	 * {@link #setInstance(CommonBeamlineDevicesConfiguration)}.
	 * Not thread-safe.
	 * @return the instance of this bean class
	 */
	public static CommonBeamlineDevicesConfiguration getInstance() {
		if (instance == null) {
			instance = Finder.findOptionalSingleton(CommonBeamlineDevicesConfiguration.class).orElse(null);
		}
		return instance;
	}

	/**
	 * Sets the {@link CommonBeamlineDevicesConfiguration}.
	 * <em>Note: For use in test code only! Not thread-safe</em>
	 */
	public static void setInstance(CommonBeamlineDevicesConfiguration newInstance) {
		instance = newInstance;
	}

	/**
	 * Do not call this constructor directly except in test code.
	 * It should declared in spring and accessed via {@link #getInstance()}.
	 * <em>For use in test code only!</em>
	 */
	public CommonBeamlineDevicesConfiguration() {
		// nothing to do
	}

	public boolean isEnforceMandatoryDeviceNames() {
		return enforceMandatoryDeviceNames;
	}

	public void setEnforceMandatoryDeviceNames(boolean enforceMandatoryDeviceNames) {
		this.enforceMandatoryDeviceNames = enforceMandatoryDeviceNames;
	}

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

	public String getUserDeviceName() {
		return userDeviceName;
	}

	public void setUserDeviceName(String userDeviceName) {
		this.userDeviceName = userDeviceName;
	}

	public void setAdditionalDeviceNames(Set<String> additionalDeviceNames) {
		this.additionalDeviceNames = requireNonNull(additionalDeviceNames);
	}

	/**
	 * Returns the set of additional metadata device names (i.e. additional to those with specific
	 * fields in this class) to be added to all scans.
	 * @return additional device names
	 */
	public Set<String> getAdditionalDeviceNames() {
		return additionalDeviceNames;
	}

	public void addAdditionalDeviceName(String deviceName) {
		additionalDeviceNames.add(deviceName);
	}

	public void addAdditionalDeviceNames(Collection<String> deviceNames) {
		additionalDeviceNames.addAll(deviceNames);
	}

	public void removeAdditionalDeviceName(String deviceName) {
		additionalDeviceNames.remove(deviceName);
		disabledDeviceNames.remove(deviceName);
	}

	public void setDisabledDeviceNames(Set<String> disabledDeviceNames) {
		this.disabledDeviceNames = requireNonNull(disabledDeviceNames);
	}

	public Set<String> getMandatoryDeviceNames() {
		return mandatoryDeviceNames;
	}

	public void setMandatoryDeviceNames(Set<String> mandatoryDeviceNames) {
		this.mandatoryDeviceNames = mandatoryDeviceNames;
	}

	public boolean isMandatoryDeviceName(String deviceName) {
		return mandatoryDeviceNames.contains(deviceName);
	}

	public Set<String> getDisabledDeviceNames() {
		return disabledDeviceNames;
	}

	public void enableDevice(String deviceName) {
		boolean wasRemoved = disabledDeviceNames.remove(deviceName);
		if (!wasRemoved) {
			logger.warn("Could not disable nexus device ''{}'' as it was not disabled.", deviceName);
		}
	}

	public void enableDevices(String... deviceNames) {
		for (String deviceName : deviceNames) {
			enableDevice(deviceName);
		}
	}

	public void disableDevice(String deviceName) {
		if (enforceMandatoryDeviceNames && mandatoryDeviceNames.contains(deviceName)) {
			logger.warn("Cannot disable nexus device ''{}''. This device is mandatory.", deviceName);
		} else {
			disabledDeviceNames.add(deviceName);
		}
	}

	public void disableDevices(String... deviceNames) {
		for (String deviceName : deviceNames) {
			disableDevice(deviceName);
		}
	}

	public Set<String> getCommonDeviceNames() {
		if (sourceName == null) logger.warn("Source device name must be set");
		if (insertionDeviceName == null && bendingMagnetName == null) logger.warn("Insertion device or bending magnet name must be set");
		if (insertionDeviceName != null && bendingMagnetName != null) logger.warn("Only one of insertion device or bending magnet name can be set");
		if (monochromatorName == null) logger.warn("Monochromator name must be set");
		if (beamName == null) logger.warn("Beam name must be set");
		if (userDeviceName == null) logger.warn("User device name must be set");

		final List<String> commonDeviceNames = Arrays.asList(sourceName, insertionDeviceName, bendingMagnetName,
				monochromatorName, beamName, userDeviceName);

		return (additionalDeviceNames == null ? commonDeviceNames.stream() :
			Stream.of(commonDeviceNames.stream(), getAdditionalDeviceNames().stream()).flatMap(Function.identity()))
				.filter(Objects::nonNull)
				.filter(not(getDisabledDeviceNames()::contains))
				.collect(toSet());
	}

}
