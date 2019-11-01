/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.event.scan;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.scanning.api.IModelProvider;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.ScanMode;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDetectorInfo;
import org.eclipse.scanning.api.malcolm.MalcolmVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Information about a given device. It may be an IRunnableDevice or an IScannableDevice.
 *
 * The type of information is for displaying the device in the UI.
 *
 * @author Matthew Gerring
 *
 */
public class DeviceInformation<T> implements IModelProvider<T> {
	private static Logger logger = LoggerFactory.getLogger(DeviceInformation.class);

	/**
	 * The device state, for instance, IDLE, READY, PAUSED, FAULT etc.
	 */
	private DeviceState state;

	/**
	 * Device name used in scan
	 */
	private String name;

	/**
	 * Level device will be run at
	 */
	private int level;

	/**
	 * Label visible in UI table
	 */
	private String label;

	/**
	 * Id used to identify the device
	 */
	private String id;

	/**
	 * Human readable note on what the device is. For instance 'Device which computes and mandelbrot set and images it.'
	 */
	private String description;

	/**
	 * The path to the icon, including bundle. The user interface will then attempt to
	 * load this bundle (if it exists in the UI product) and the icon from it.
	 * <p>
	 * Form: bundle:subdir(s)/image name
	 * <p>
	 * Example: org.eclipse.scanning.example:icons/mandelbrot.png
	 * <p>
	 * If org.eclipse.scanning.example is not available on the client then a default icon or no icon will be
	 * used depending on the UI connecting.
	 */
	private String icon;

	/**
	 * The model which the detector is currently using. Or if
	 * the detector does not have a model, null.
	 */
	private T model;

	/**
	 * The unit for the device, if any.
	 * This field has the same value as {@link IScannable#getUnit()}
	 */
	private String unit;

	/**
	 * The upper limit or null if none exists.
	 * This field is only used for {@link IScannable#getMaximum()}
	 */
	private Object upper;

	/**
	 * The lower limit or null if none exists.
	 * This field is only used for {@link IScannable}s.
	 */
	private Object lower;

	/**
	 * Allowed values if value has discrete options
	 * This field is only used for {@link IScannable}s.
	 */
	private Object[] permittedValues;

	/**
	 * Holds activated state of device, if any.
	 * Activated devices are used when a scan is constructed
	 * and the state is saved.
	 */
	private boolean activated = false;

	/**
	 * The device health, free text to describe the current status of the device.
	 * This field is only used for {@link IRunnableDevice}s
	 */
	private String health;

	/**
	 * Whether the device is busy or not.
	 * This field is only used for {@link IRunnableDevice}s
	 */
	private boolean busy;

	/**
	 * The role of the device in the scan.
	 * This field is only used for {@link IRunnableDevice}s
	 */
	private DeviceRole deviceRole;

	/**
	 * The {@link ScanMode}s supported by this device.
	 * This field is only used for {@link IRunnableDevice}s
	 */
	private Set<ScanMode> supportedScanModes;

	/**
	 * A device is 'alive' if it exists and is responding.
	 */
	private boolean alive = true;

	/**
	 * The available axes of the malcolm device.
	 * This field is only used for {@link IMalcolmDevice}s.
	 */
	private List<String> availableAxes;

	/**
	 * The version of the malcolm device. Naturally, this field is only set for malcolm devices.
	 */
	private MalcolmVersion malcolmVersion;

	/**
	 * A list of {@link MalcolmDetectorInfo}s describing the detectors controlled by the malcolm device.
	 * This field is only used for {@link IMalcolmDevice}s
	 */
	private List<MalcolmDetectorInfo> malcolmDetectorInfos;

	/**
	 * Indicates whether the detector should be shown by default (i.e. after a client reset) in the detectors section of
	 * the mapping experiment view,
	 */
	private boolean shownByDefault = true;

	public DeviceInformation() {

	}

	public DeviceInformation(String name) {
	    this.name = name;
	}

    /**
     * Merges in any non-null fields.
     * @param info
     */
	public void merge(DeviceInformation<T> info) {
		Field[] wfields = DeviceInformation.class.getDeclaredFields();
		for (Field field : wfields) {
			try {
				Object value = field.get(info);
				if (value!=null) field.set(this, value);
			} catch (Exception ne) {
				ne.printStackTrace();
			}
		}
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @deprecated the id is no longer needed. It is just a fall back
	 * for when the name is not available.
	 */
	@Deprecated
	public String getId() {
		return id;
	}

	/**
	 * @deprecated the id is no longer needed. It is just a fall back
	 * for when the name is not available.
	 */
	@Deprecated
	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public DeviceRole getDeviceRole() {
		return deviceRole;
	}

	public void setDeviceRole(DeviceRole deviceRole) {
		this.deviceRole = deviceRole;
	}

	public Set<ScanMode> getSupportedScanModes() {
		return supportedScanModes;
	}

	public void setSupportedScanModes(Set<ScanMode> supportedScanModes) {
		this.supportedScanModes = supportedScanModes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (activated ? 1231 : 1237);
		result = prime * result + (alive ? 1231 : 1237);
		result = prime * result + ((availableAxes == null) ? 0 : availableAxes.hashCode());
		result = prime * result + (busy ? 1231 : 1237);
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((deviceRole == null) ? 0 : deviceRole.hashCode());
		result = prime * result + ((health == null) ? 0 : health.hashCode());
		result = prime * result + ((malcolmVersion == null) ? 0 : malcolmVersion.hashCode());
		result = prime * result + ((icon == null) ? 0 : icon.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + level;
		result = prime * result + ((lower == null) ? 0 : lower.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(permittedValues);
		result = prime * result + (shownByDefault ? 1231 : 1237);
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((supportedScanModes == null) ? 0 : supportedScanModes.hashCode());
		result = prime * result + ((malcolmDetectorInfos == null) ? 0: malcolmDetectorInfos.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((upper == null) ? 0 : upper.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeviceInformation<?> other = (DeviceInformation<?>) obj;
		if (activated != other.activated)
			return false;
		if (alive != other.alive)
			return false;
		if (availableAxes == null) {
			if (other.availableAxes != null)
				return false;
		} else if (!availableAxes.equals(other.availableAxes))
			return false;
		if (busy != other.busy)
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (deviceRole != other.deviceRole)
			return false;
		if (health == null) {
			if (other.health != null)
				return false;
		} else if (!health.equals(other.health))
			return false;
		if (malcolmVersion == null) {
			if (other.malcolmVersion != null)
				return false;
		} else if (!malcolmVersion.equals(other.malcolmVersion))
			return false;
		if (icon == null) {
			if (other.icon != null)
				return false;
		} else if (!icon.equals(other.icon))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (level != other.level)
			return false;
		if (lower == null) {
			if (other.lower != null)
				return false;
		} else if (!lower.equals(other.lower))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(permittedValues, other.permittedValues))
			return false;
		if (shownByDefault != other.shownByDefault)
			return false;
		if (state != other.state)
			return false;
		if (supportedScanModes == null) {
			if (other.supportedScanModes != null)
				return false;
		} else if (!supportedScanModes.equals(other.supportedScanModes))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		if (upper == null) {
			if (other.upper != null)
				return false;
		} else if (!upper.equals(other.upper))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DeviceInformation [name=" + name + ", label=" + label + "]";
	}

	@Override
	public T getModel() {
		return model;
	}

	@Override
	public void setModel(T model) {
		this.model = model;
	}

	public DeviceState getState() {
		return state;
	}

	public void setState(DeviceState state) {
		this.state = state;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Object getUpper() {
		return upper;
	}

	public void setUpper(Object upper) {
		this.upper = upper;
	}

	public Object getLower() {
		return lower;
	}

	public void setLower(Object lower) {
		this.lower = lower;
	}

	public Object[] getPermittedValues() {
		return permittedValues;
	}

	public void setPermittedValues(Object[] permittedValues) {
		this.permittedValues = permittedValues;
	}

	public boolean isActivated() {
		return activated;
	}

	public boolean setActivated(boolean activated) {
		logger.trace("setActivated({}) was {} ({})", activated, this.activated, this);
		boolean wasactivated = this.activated;
		this.activated = activated;
		return wasactivated;
	}

	public String getHealth() {
		return health;
	}

	public void setHealth(String health) {
		this.health = health;
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public List<String> getAvailableAxes() {
		return availableAxes;
	}

	public void setAvailableAxes(List<String> list) {
		this.availableAxes = list;
	}

	public boolean isShownByDefault() {
		return shownByDefault;
	}

	public void setShownByDefault(boolean shownByDefault) {
		this.shownByDefault = shownByDefault;
	}

	public MalcolmVersion getMalcolmVersion() {
		return malcolmVersion;
	}

	public void setMalcolmVersion(MalcolmVersion malcolmVersion) {
		this.malcolmVersion = malcolmVersion;
	}

	public List<MalcolmDetectorInfo> getMalcolmDetectorInfos() {
		return malcolmDetectorInfos;
	}

	public void setMalcolmDetectorInfos(List<MalcolmDetectorInfo> malcolmDetectorInfos) {
		this.malcolmDetectorInfos = malcolmDetectorInfos;
	}

}
