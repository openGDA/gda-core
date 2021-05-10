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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.rcp.views.AbstractPositionerComposite;
import gda.rcp.views.EnumPositionerComposite;
import gda.rcp.views.NudgePositionerComposite;

public class ScannablePositionerControl extends LiveControlBase {

	private static final Logger logger = LoggerFactory.getLogger(ScannablePositionerControl.class);

	// Use the wrapper classes to allow null ie default if not set.
	private String displayName;
	private String scannableName; // Used by the finder to get the scannable
	private Boolean showStop; // Show stop by default
	private String userUnits = ""; // Use to override the scannable units (if required)
	private boolean unitDisplayOutsideTextBox = false; // control the location of unit
	private Double increment; // The increment to set when then control is created Double allows null i.e. default
	private int incrementTextWidth = 60; // Passed down to NudgePositionerComposite
	private Boolean showIncrement;
	private boolean boldLabel;
	private boolean horizontalLayout = false;
	private int displayNameWidth;
	private boolean nonEditableIncrement;
	private boolean readOnly;

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void setNonEditableIncrement(boolean nonEditableIncrement) {
		this.nonEditableIncrement = nonEditableIncrement;
	}

	public Boolean isShowIncrement() {
		return showIncrement;
	}

	public void setShowIncrement(Boolean showIncrement) {
		this.showIncrement = showIncrement;
	}

	private AbstractPositionerComposite positionerComposite;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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

	public boolean getBoldLabel() {
		return this.boldLabel;
	}

	public void setBoldLabel(boolean boldLabel) {
		this.boldLabel = boldLabel;
	}

	public void setDisplayNameWidth(int displayNameWidth) {
		this.displayNameWidth = displayNameWidth;
	}

	public void setHorizontalLayout(boolean horizontalLayout) {
		this.horizontalLayout = horizontalLayout;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (boldLabel ? 1231 : 1237);
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((increment == null) ? 0 : increment.hashCode());
		result = prime * result + incrementTextWidth;
		result = prime * result + ((positionerComposite == null) ? 0 : positionerComposite.hashCode());
		result = prime * result + ((scannableName == null) ? 0 : scannableName.hashCode());
		result = prime * result + ((showIncrement == null) ? 0 : showIncrement.hashCode());
		result = prime * result + ((showStop == null) ? 0 : showStop.hashCode());
		result = prime * result + ((userUnits == null) ? 0 : userUnits.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScannablePositionerControl other = (ScannablePositionerControl) obj;
		if (boldLabel != other.boldLabel)
			return false;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (increment == null) {
			if (other.increment != null)
				return false;
		} else if (!increment.equals(other.increment))
			return false;
		if (incrementTextWidth != other.incrementTextWidth)
			return false;
		if (positionerComposite == null) {
			if (other.positionerComposite != null)
				return false;
		} else if (!positionerComposite.equals(other.positionerComposite))
			return false;
		if (scannableName == null) {
			if (other.scannableName != null)
				return false;
		} else if (!scannableName.equals(other.scannableName))
			return false;
		if (showIncrement == null) {
			if (other.showIncrement != null)
				return false;
		} else if (!showIncrement.equals(other.showIncrement))
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
		Optional<Scannable> optionalScannable = Finder.findOptionalOfType(getScannableName(), Scannable.class);
		if (optionalScannable.isEmpty()) {
			logger.warn("Could not get scannable '{}' for live control", getScannableName());
			return;
		}
		Scannable scannable = optionalScannable.get();

		int layoutStyle = horizontalLayout ? SWT.HORIZONTAL : SWT.NONE;

		if (readOnly) {
			layoutStyle = SWT.READ_ONLY;
		}

		if (scannable instanceof EnumPositioner) {
			positionerComposite = new EnumPositionerComposite(composite, layoutStyle, unitDisplayOutsideTextBox);
			if (boldLabel) {
				((EnumPositionerComposite) positionerComposite).setLabelToBold();
			}
		} else {
			// Create the NudgePositionerComposite and set the scannable
			positionerComposite = new NudgePositionerComposite(composite, layoutStyle, unitDisplayOutsideTextBox);
			NudgePositionerComposite npc = (NudgePositionerComposite) positionerComposite;

			npc.setUserUnits(StringUtils.defaultString(userUnits));
			if (getIncrement() != null) {
				npc.setIncrement(getIncrement());
			}
			npc.setIncrementTextWidth(incrementTextWidth);
			if (showIncrement != null && !showIncrement) {
				npc.hideIncrementControl();
			}
			if (boldLabel) {
				npc.setLabelToBold();
			}
			if (nonEditableIncrement) {
				npc.setFixedIncrementInput();
			}

		}

		if (displayNameWidth > 0) {
			positionerComposite.setDisplayNameWidth(displayNameWidth);
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

	/**
	 * show or hide increment control composite in the Live Control view
	 */
	public void toggleIncrementControlDisplay() {
		if (positionerComposite instanceof NudgePositionerComposite) {
			NudgePositionerComposite npc = (NudgePositionerComposite) positionerComposite;
			if (npc.isIncrementControlVisible()) {
				npc.hideIncrementControl();
				showIncrement = false;
			} else {
				npc.showIncrementControl();
				showIncrement = true;
			}
		}
	}

	public void toggleShowStop() {
		if (positionerComposite instanceof NudgePositionerComposite) {
			NudgePositionerComposite npc = (NudgePositionerComposite) positionerComposite;
			if (npc.isStopButtonVisible()) {
				npc.hideStopButton();
				showStop = false;
			} else {
				npc.showStopButton();
				showStop = true;
			}
		}
	}


	@Override
	public String toString() {
		return "ScannablePositionerControl [displayName=" + displayName + ", scannableName=" + scannableName
				+ ", showStop=" + showStop + ", userUnits=" + userUnits + ", increment=" + increment
				+ ", incrementTextWidth=" + incrementTextWidth + ", showIncrement=" + showIncrement + ", boldLabel="
				+ boldLabel + ", positionerComposite=" + positionerComposite + "]";
	}

	public void setUnitDisplayOutsideTextBox(boolean unitDisplayOutsideTextBox) {
		this.unitDisplayOutsideTextBox = unitDisplayOutsideTextBox;
	}

}