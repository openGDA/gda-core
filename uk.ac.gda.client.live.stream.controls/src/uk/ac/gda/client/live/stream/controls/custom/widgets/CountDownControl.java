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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gda.observable.IObservable;
import uk.ac.gda.client.livecontrol.LiveControl;
import uk.ac.gda.client.livecontrol.LiveControlBase;
import uk.ac.gda.client.livecontrol.LiveControlsView;
/**
 * A Spring configure bean for {@link LiveControl} used to populate items in {@link LiveControlsView}.
 * It has 5 properties: a group name, a display name, an {@link IObservable} to providing count down time, a time unit, and text width.
 * Among these, only the {@link IObservable} instance is essential.
 */
public class CountDownControl extends LiveControlBase {

	// Use the wrapper classes to allow null i.e. default if not set.
	private String displayName;
	private IObservable observable; // this observable providing count down time data
	private String userUnits; // the unit for the displayed data.
	private Integer textWidth; // If set, passed down to CountDawnComposite

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
		int result = super.hashCode();
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((observable == null) ? 0 : observable.hashCode());
		result = prime * result + ((textWidth == null) ? 0 : textWidth.hashCode());
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
		CountDownControl other = (CountDownControl) obj;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (observable == null) {
			if (other.observable != null)
				return false;
		} else if (!observable.equals(other.observable))
			return false;
		if (textWidth == null) {
			if (other.textWidth != null)
				return false;
		} else if (!textWidth.equals(other.textWidth))
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
		return "CountDownControl [displayName=" + displayName + ", observable=" + observable + ", userUnits="
				+ userUnits + ", textWidth=" + textWidth + "]";
	}

	public IObservable getObservable() {
		return observable;
	}

	public void setObservable(IObservable observable) {
		this.observable = observable;
	}

}