/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.client.livecontrol;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.factory.FindableBase;
import gda.factory.Finder;
import gda.rcp.views.InputTextComposite;
/**
 * A Spring configure bean for {@link LiveControl} used to populate items in {@link LiveControlsView}
 * It just has 3 parts: a display name, a group name, and input data unit if exist which must be
 * respected as there is no unit conversion built in.
 */
public class ScannableValueControl extends FindableBase implements LiveControl {

	private static final Logger logger = LoggerFactory.getLogger(ScannableValueControl.class);

	// Use the wrapper classes to allow null i.e. default if not set.
	private String displayName;
	private String group;
	private String scannableName; // Used by the finder to get the scannable
	private String userUnits; // Use to override the scannable units (if required)
	private Integer textWidth; // If set, passed down to InputTextComposite
	private Boolean readOnly=false;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	public String getUserUnits() {
		return userUnits;
	}

	public void setUserUnits(String userUnits) {
		this.userUnits = userUnits;
	}

	public Integer getTextWidth() {
		return textWidth;
	}

	public void setTextWidth(Integer incrementTextWidth) {
		this.textWidth = incrementTextWidth;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((textWidth == null) ? 0 : textWidth.hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((scannableName == null) ? 0 : scannableName.hashCode());
		result = prime * result + ((userUnits == null) ? 0 : userUnits.hashCode());
		result = prime * result + ((readOnly == null) ? 0 : readOnly.hashCode());

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
		ScannableValueControl other = (ScannableValueControl) obj;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (textWidth == null) {
			if (other.textWidth != null)
				return false;
		} else if (!textWidth.equals(other.textWidth))
			return false;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		if (scannableName == null) {
			if (other.scannableName != null)
				return false;
		} else if (!scannableName.equals(other.scannableName))
			return false;
		if (userUnits == null) {
			if (other.userUnits != null)
				return false;
		} else if (!userUnits.equals(other.userUnits))
			return false;
		if (readOnly == null) {
			if (other.readOnly != null)
				return false;
		} else if (!readOnly.equals(other.readOnly))
			return false;
		return true;
	}

	@Override
	public void createControl(Composite composite) {
		// Get the scannable with the finder
		final Scannable scannable = Finder.getInstance().find(getScannableName());
		if (scannable == null) {
			logger.warn("Could not get scannable '{}' for live control", getScannableName());
			return;
		}

		// Create the InputTextComposite and set the scannable
		final InputTextComposite itc = new InputTextComposite(composite, SWT.NONE);
		if (readOnly != null) { // must be set before setScannable as this is called by it
			itc.setReadOnly(readOnly.booleanValue());
		}
		itc.setScannable(scannable);

		if (getDisplayName() != null) {
			itc.setDisplayName(getDisplayName());
		}
		if (getUserUnits() != null) {
			itc.setUserUnit(getUserUnits());
		}
		if (textWidth != null) {
			itc.setTextWidth(textWidth);
		}
	}

	@Override
	public String toString() {
		return "ScannablePositionerControl [name=" + getName() + ", displayName=" + displayName + ", group=" + group
				+ ", scannableName=" + scannableName + ", userUnits=" + userUnits
				+ ", textWidth=" + textWidth + "]";
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

}