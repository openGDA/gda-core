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

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.factory.FindableBase;
import gda.factory.Finder;
import gda.rcp.views.AbstractPositionerComposite;
import gda.rcp.views.EnumPositionerComposite;
import gda.rcp.views.NudgePositionerComposite;

public class ScannablePositionerControl extends FindableBase implements LiveControl {

	private static final Logger logger = LoggerFactory.getLogger(ScannablePositionerControl.class);

	// Use the wrapper classes to allow null ie default if not set.
	private String displayName;
	private String group;
	private String scannableName; // Used by the finder to get the scannable
	private Boolean showStop; // Show stop by default
	private String userUnits; // Use to override the scannable units (if required)
	private Double increment; // The increment to set when then control is created Double allows null i.e. default
	private int incrementTextWidth = 60; // Passed down to NudgePositionerComposite

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

	public Boolean getShowStop() {
		return showStop;
	}

	public void setShowStop(Boolean showStop) {
		this.showStop = showStop;
	}

	public String getUserUnits() {
		return userUnits;
	}

	public void setUserUnits(String userUnits) {
		this.userUnits = userUnits;
	}

	public Double getIncrement() {
		return increment;
	}

	public void setIncrement(Double increment) {
		this.increment = increment;
	}

	public Integer getIncrementTextWidth() {
		return incrementTextWidth;
	}

	public void setIncrementTextWidth(Integer incrementTextWidth) {
		this.incrementTextWidth = incrementTextWidth;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((increment == null) ? 0 : increment.hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((scannableName == null) ? 0 : scannableName.hashCode());
		result = prime * result + ((showStop == null) ? 0 : showStop.hashCode());
		result = prime * result + ((userUnits == null) ? 0 : userUnits.hashCode());
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
		ScannablePositionerControl other = (ScannablePositionerControl) obj;
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
		if (increment == null) {
			if (other.increment != null)
				return false;
		} else if (!increment.equals(other.increment))
			return false;
		if (incrementTextWidth != other.incrementTextWidth) {
			return false;
		}
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
		if (showStop == null) {
			if (other.showStop != null)
				return false;
		} else if (!showStop.equals(other.showStop))
			return false;
		if (userUnits == null) {
			if (other.userUnits != null)
				return false;
		} else if (!userUnits.equals(other.userUnits))
			return false;
		return true;
	}

	@Override
	public void createControl(Composite composite) {
		// Get the scannable with the finder
		final Optional<Scannable> optionalScannable = Finder.getInstance().findOptional(getScannableName());
		if (!optionalScannable.isPresent()) {
			logger.warn("Could not get scannable '{}' for live control", getScannableName());
			return;
		}
		final Scannable scannable = optionalScannable.get();

		AbstractPositionerComposite positionerComposite;
		if (scannable instanceof EnumPositioner) {
			positionerComposite = new EnumPositionerComposite(composite, SWT.NONE);
		} else {
		// Create the NudgePositionerComposite and set the scannable
		positionerComposite = new NudgePositionerComposite(composite, SWT.NONE);
		NudgePositionerComposite npc = (NudgePositionerComposite) positionerComposite;
			if (getUserUnits() != null) {
				npc.setUserUnits(getUserUnits());
			}
			if (getIncrement() != null) {
				npc.setIncrement(getIncrement());
			}
			npc.setIncrementTextWidth(incrementTextWidth);
		}
		positionerComposite.setScannable(scannable);

		// Configure the NPC with additional settings if provided
		if (getDisplayName() != null) {
			positionerComposite.setDisplayName(getDisplayName());
		}

		if (showStop != null && !showStop) {
			positionerComposite.hideStopButton();
		}
	}

	@Override
	public String toString() {
		return "ScannablePositionerControl [name=" + getName() + ", displayName=" + displayName + ", group=" + group
				+ ", scannableName=" + scannableName + ", showStop=" + showStop + ", userUnits=" + userUnits
				+ ", increment=" + increment + ", incrementTextWidth=" + incrementTextWidth + "]";
	}

}