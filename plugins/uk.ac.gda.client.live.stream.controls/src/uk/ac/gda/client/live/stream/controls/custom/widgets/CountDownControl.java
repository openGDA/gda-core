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

package uk.ac.gda.client.live.stream.controls.custom.widgets;

import java.util.Observable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gda.factory.FindableBase;
import uk.ac.gda.client.livecontrol.LiveControl;
import uk.ac.gda.client.livecontrol.LiveControlsView;
/**
 * A Spring configure bean for {@link LiveControl} used to populate items in {@link LiveControlsView}.
 * It has 5 properties: a group name, a display name, an {@link Observable} to providing count down time, a time unit, and text width.
 * Among these, only the {@link Observable} instance is essential.
 */
public class CountDownControl extends FindableBase implements LiveControl {

	// Use the wrapper classes to allow null i.e. default if not set.
	private String group;
	private String displayName;
	private Observable observable; // this observable providing count down time data
	private String userUnits; // the unit for the displayed data.
	private Integer textWidth; // If set, passed down to CountDawnComposite

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
		result = prime * result + ((getObservable() == null) ? 0 : getObservable().hashCode());
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
		CountDownControl other = (CountDownControl) obj;
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
		if (getObservable() == null) {
			if (other.getObservable() != null)
				return false;
		} else if (!getObservable().equals(other.getObservable()))
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

		final CountDownComposite itc = new CountDownComposite(composite, SWT.NONE);
		itc.setObservable(getObservable());

		if (getDisplayName() != null) {
			itc.setDisplayName(getDisplayName());
		}
		if (getUserUnits() != null) {
			itc.setUserUnit(getUserUnits());
		}
		if (getTextWidth() != null) {
			itc.setTextWidth(textWidth);
		}
	}
	
	public void init() {
		if (getObservable() == null) {
			throw new IllegalStateException("observable is not set.");
		} 
	}
	
	@Override
	public String toString() {
		return "ScannablePositionerControl [name=" + getName() + ", displayName=" + displayName + ", group=" + group
				+ ", observable=" + getObservable().toString() + ", userUnits=" + userUnits
				+ ", textWidth=" + textWidth + "]";
	}

	public Observable getObservable() {
		return observable;
	}

	public void setObservable(Observable observable) {
		this.observable = observable;
	}

}